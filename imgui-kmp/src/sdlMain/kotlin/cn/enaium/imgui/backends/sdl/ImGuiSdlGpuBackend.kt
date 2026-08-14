/*
 * Copyright (c) 2026 Enaium
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cn.enaium.imgui.backends.sdl

import cn.enaium.imgui.ImDrawData
import cn.enaium.imgui.ImDrawVertData
import cn.enaium.imgui.ImGui
import cn.enaium.imgui.ImVec2
import cn.enaium.sdl.SDL
import cn.enaium.sdl.SDLColor
import cn.enaium.sdl.SDLGPUBlendFactor
import cn.enaium.sdl.SDLGPUBlendOp
import cn.enaium.sdl.SDLGPUBlendState
import cn.enaium.sdl.SDLGPUBuffer
import cn.enaium.sdl.SDLGPUBufferCreateInfo
import cn.enaium.sdl.SDLGPUBufferUsage
import cn.enaium.sdl.SDLGPUColorTargetDescription
import cn.enaium.sdl.SDLGPUColorTargetInfo
import cn.enaium.sdl.SDLGPUCommandBuffer
import cn.enaium.sdl.SDLGPUDevice
import cn.enaium.sdl.SDLGPUFilter
import cn.enaium.sdl.SDLGPUGraphicsPipeline
import cn.enaium.sdl.SDLGPUGraphicsPipelineCreateInfo
import cn.enaium.sdl.SDLGPUIndexElementSize
import cn.enaium.sdl.SDLGPUPrimitiveType
import cn.enaium.sdl.SDLGPUSampler
import cn.enaium.sdl.SDLGPUSamplerAddressMode
import cn.enaium.sdl.SDLGPUSamplerCreateInfo
import cn.enaium.sdl.SDLGPUSamplerMipmapMode
import cn.enaium.sdl.SDLGPUShader
import cn.enaium.sdl.SDLGPUShaderFormat
import cn.enaium.sdl.SDLGPUShaderStage
import cn.enaium.sdl.SDLGPUTexture
import cn.enaium.sdl.SDLGPUTextureCreateInfo
import cn.enaium.sdl.SDLGPUTextureFormat
import cn.enaium.sdl.SDLGPUTextureUsage
import cn.enaium.sdl.SDLGPUVertexAttribute
import cn.enaium.sdl.SDLGPUVertexBufferDescription
import cn.enaium.sdl.SDLGPUVertexElementFormat
import cn.enaium.sdl.SDLGPUVertexInputRate
import cn.enaium.sdl.SDLGPUVertexInputState
import cn.enaium.sdl.SDLGPUViewport
import cn.enaium.sdl.SDLWindow
import kotlin.math.max
import kotlin.math.min

/** ImDrawVert layout expanded for portability: pos (2f) + uv (2f) + col (4f) = 32 bytes. */
private const val VERTEX_STRIDE = 32

private fun floatBitsToBytes(value: Float, out: ByteArray, offset: Int) {
    val bits = value.toBits()
    out[offset] = (bits ushr 0).toByte()
    out[offset + 1] = (bits ushr 8).toByte()
    out[offset + 2] = (bits ushr 16).toByte()
    out[offset + 3] = (bits ushr 24).toByte()
}

/**
 * Renders the imgui draw data with the SDL3 GPU API. Mirrors
 * imgui_impl_sdlgpu3.cpp: one vertex/index buffer pair (reused and resized
 * as needed) is filled per frame and drawn with a single pipeline.
 */
class ImGuiSdlGpuBackend(
    private val device: SDLGPUDevice,
    private val window: SDLWindow,
) {
    private val vertexShader: SDLGPUShader?
    private val fragmentShader: SDLGPUShader?
    private val pipeline: SDLGPUGraphicsPipeline?
    private val sampler: SDLGPUSampler?
    private val textures = mutableMapOf<Long, SDLGPUTexture>()

    private var vertexBuffer: SDLGPUBuffer? = null
    private var indexBuffer: SDLGPUBuffer? = null
    private var vertexCapacity = 0
    private var indexCapacity = 0

    init {
        val windowFormat = device.getWindowFormat(window)
            ?: error("SDL_GetGPUSwapchainTextureFormat failed: ${SDL.error()}")

        // The sdlgpu3 backend ships precompiled SPIR-V (Vulkan) and MSL
        // (Metal) shaders. Prefer MSL on Metal; SPIR-V is the Vulkan fallback.
        val (vertCode, vertFormat, vertEntry) =
            if ((device.shaderFormats and SDLGPUShaderFormat.MSL) != 0) {
                Triple(VERT_MSL, SDLGPUShaderFormat.MSL, "main0")
            } else {
                Triple(VERT_SPIRV, SDLGPUShaderFormat.SPIRV, "main")
            }
        val (fragCode, fragFormat, fragEntry) =
            if ((device.shaderFormats and SDLGPUShaderFormat.MSL) != 0) {
                Triple(FRAG_MSL, SDLGPUShaderFormat.MSL, "main0")
            } else {
                Triple(FRAG_SPIRV, SDLGPUShaderFormat.SPIRV, "main")
            }

        vertexShader = device.createShader(
            code = vertCode,
            format = vertFormat,
            stage = SDLGPUShaderStage.VERTEX,
            entryPoint = vertEntry,
            numSamplers = 0,
            numStorageTextures = 0,
            numStorageBuffers = 0,
            numUniformBuffers = 1,
        )
        fragmentShader = device.createShader(
            code = fragCode,
            format = fragFormat,
            stage = SDLGPUShaderStage.FRAGMENT,
            entryPoint = fragEntry,
            numSamplers = 1,
            numStorageTextures = 0,
            numStorageBuffers = 0,
            numUniformBuffers = 0,
        )
        check(vertexShader != null && fragmentShader != null) {
            "shader creation failed: ${SDL.error()}"
        }

        pipeline = device.createGraphicsPipeline(
            SDLGPUGraphicsPipelineCreateInfo(
                vertexShader = vertexShader!!,
                fragmentShader = fragmentShader!!,
                vertexInputState = SDLGPUVertexInputState(
                    vertexBufferDescriptions = listOf(
                        SDLGPUVertexBufferDescription(
                            slot = 0,
                            pitch = VERTEX_STRIDE,
                            inputRate = SDLGPUVertexInputRate.VERTEX,
                        ),
                    ),
                    vertexAttributes = listOf(
                        SDLGPUVertexAttribute(location = 0, bufferSlot = 0, format = SDLGPUVertexElementFormat.FLOAT2, offset = 0),
                        SDLGPUVertexAttribute(location = 1, bufferSlot = 0, format = SDLGPUVertexElementFormat.FLOAT2, offset = 8),
                        SDLGPUVertexAttribute(location = 2, bufferSlot = 0, format = SDLGPUVertexElementFormat.FLOAT4, offset = 16),
                    ),
                ),
                primitiveType = SDLGPUPrimitiveType.TRIANGLELIST,
                targetDescriptions = listOf(
                    SDLGPUColorTargetDescription(
                        format = windowFormat,
                        blendState = SDLGPUBlendState(
                            srcColorBlendFactor = SDLGPUBlendFactor.SRC_ALPHA,
                            dstColorBlendFactor = SDLGPUBlendFactor.ONE_MINUS_SRC_ALPHA,
                            colorBlendOp = SDLGPUBlendOp.ADD,
                            srcAlphaBlendFactor = SDLGPUBlendFactor.ONE,
                            dstAlphaBlendFactor = SDLGPUBlendFactor.ONE_MINUS_SRC_ALPHA,
                            alphaBlendOp = SDLGPUBlendOp.ADD,
                        ),
                    ),
                ),
            ),
        )
        check(pipeline != null) { "pipeline creation failed: ${SDL.error()}" }

        sampler = device.createSampler(
            SDLGPUSamplerCreateInfo(
                minFilter = SDLGPUFilter.LINEAR,
                magFilter = SDLGPUFilter.LINEAR,
                mipmapMode = SDLGPUSamplerMipmapMode.LINEAR,
                addressModeU = SDLGPUSamplerAddressMode.CLAMP_TO_EDGE,
                addressModeV = SDLGPUSamplerAddressMode.CLAMP_TO_EDGE,
                addressModeW = SDLGPUSamplerAddressMode.CLAMP_TO_EDGE,
            ),
        )
        check(sampler != null) { "sampler creation failed: ${SDL.error()}" }
    }

    /** Registers an external texture so draw commands referencing its id can render it. */
    fun registerTexture(texture: SDLGPUTexture) {
        textures[texture.ptr] = texture
    }

    /** Uploads the font atlas pixels and returns the texture id. */
    fun uploadFontTexture(pixels: ByteArray, width: Int, height: Int): Long {        val texture = device.createTexture(
            SDLGPUTextureCreateInfo(
                format = SDLGPUTextureFormat.R8G8B8A8_UNORM,
                usage = SDLGPUTextureUsage.SAMPLE,
                width = width,
                height = height,
            ),
        )
        check(texture != null) { "font texture creation failed: ${SDL.error()}" }
        check(texture.upload(pixels, width * 4, 0, 0, width, height)) {
            "font texture upload failed: ${SDL.error()}"
        }
        textures[texture.ptr] = texture
        return texture.ptr
    }

    private fun ensureBuffers(vtxCount: Int, idxCount: Int) {
        if (vtxCount <= 0 && idxCount <= 0) return
        val vtxBytes = vtxCount * VERTEX_STRIDE
        if (vtxBytes > vertexCapacity) {
            vertexBuffer?.close()
            vertexBuffer = device.createBuffer(
                SDLGPUBufferCreateInfo(usage = SDLGPUBufferUsage.VERTEX, size = vtxBytes),
            )
            check(vertexBuffer != null) { "vertex buffer creation failed: ${SDL.error()}" }
            vertexCapacity = vtxBytes
        }
        val idxBytes = idxCount * 2
        if (idxBytes > indexCapacity) {
            indexBuffer?.close()
            indexBuffer = device.createBuffer(
                SDLGPUBufferCreateInfo(usage = SDLGPUBufferUsage.INDEX, size = idxBytes),
            )
            check(indexBuffer != null) { "index buffer creation failed: ${SDL.error()}" }
            indexCapacity = idxBytes
        }
    }

    /** Packs the draw list vertices into the SDL_GPU vertex layout. */
    private fun packVertices(verts: ImDrawVertData, displayPos: ImVec2): ByteArray {
        val count = verts.colors.size
        val out = ByteArray(count * VERTEX_STRIDE)
        for (i in 0 until count) {
            val base = i * VERTEX_STRIDE
            floatBitsToBytes(verts.positions[i * 2] - displayPos.x, out, base)
            floatBitsToBytes(verts.positions[i * 2 + 1] - displayPos.y, out, base + 4)
            floatBitsToBytes(verts.uvs[i * 2], out, base + 8)
            floatBitsToBytes(verts.uvs[i * 2 + 1], out, base + 12)
            val col = verts.colors[i]
            // ImDrawVert::col is packed as 0xAABBGGRR (IM_COL32 default, not
            // IMGUI_USE_BGRA_PACKED_COLOR): R in the least-significant byte.
            // Expanded to 4 normalized floats for portability.
            floatBitsToBytes((col and 0xFF) / 255f, out, base + 16)
            floatBitsToBytes(((col shr 8) and 0xFF) / 255f, out, base + 20)
            floatBitsToBytes(((col shr 16) and 0xFF) / 255f, out, base + 24)
            floatBitsToBytes(((col shr 24) and 0xFF) / 255f, out, base + 28)
        }
        return out
    }

    /** Packs the draw list indices (16-bit) into a byte buffer. */
    private fun packIndices(indices: IntArray): ByteArray {
        val out = ByteArray(indices.size * 2)
        for (i in indices.indices) {
            out[i * 2] = (indices[i] and 0xFF).toByte()
            out[i * 2 + 1] = ((indices[i] shr 8) and 0xFF).toByte()
        }
        return out
    }

    /**
     * Renders one frame into [commandBuffer] against [targetTexture]. The
     * caller owns the command buffer lifecycle (acquire, submit, present).
     *
     * Each draw list is uploaded into its OWN vertex/index buffers through a
     * copy pass inside this [commandBuffer] (before the render pass), and drawn
     * with vertexOffset=0. Two workarounds are combined here for broken SDL 3.4
     * Metal drivers (e.g. Apple M5):
     *  - no cross-command-buffer uploads (unreliable sync), and
     *  - no large vertexOffset in DrawIndexedPrimitives (fails on M5), which a
     *    merged single-buffer approach would require for the later draw lists.
     */
    fun renderFrame(
        commandBuffer: SDLGPUCommandBuffer,
        targetTexture: SDLGPUTexture,
        viewportWidth: Int,
        viewportHeight: Int,
    ) {
        val drawData = ImGui.getDrawData()

        // Upload phase: per-list buffers uploaded through copy passes INSIDE
        // this command buffer (before the render pass - SDL forbids a copy pass
        // and a render pass being active at the same time).
        data class ListBuffers(val vb: SDLGPUBuffer, val ib: SDLGPUBuffer)
        val listBuffers = ArrayList<ListBuffers>(drawData.cmdListsCount)
        for (listIndex in 0 until drawData.cmdListsCount) {
            val list = drawData.cmdList(listIndex)
            if (list.vtxCount == 0 || list.idxCount == 0) continue
            val verts = list.copyVtx(0, list.vtxCount)
            val indices = list.copyIdx(0, list.idxCount)
            val vtxBytes = packVertices(verts, drawData.displayPos)
            val idxBytes = packIndices(indices)
            val vb = device.createBuffer(
                cn.enaium.sdl.SDLGPUBufferCreateInfo(usage = cn.enaium.sdl.SDLGPUBufferUsage.VERTEX, size = vtxBytes.size),
            )
            val ib = device.createBuffer(
                cn.enaium.sdl.SDLGPUBufferCreateInfo(usage = cn.enaium.sdl.SDLGPUBufferUsage.INDEX, size = idxBytes.size),
            )
            if (vb == null || ib == null) continue
            check(commandBuffer.uploadToBuffer(vb, vtxBytes)) { "per-list vertex upload failed: ${SDL.error()}" }
            check(commandBuffer.uploadToBuffer(ib, idxBytes)) { "per-list index upload failed: ${SDL.error()}" }
            listBuffers.add(ListBuffers(vb, ib))
        }
        if (listBuffers.isEmpty()) return

        val pass = commandBuffer.beginRenderPass(
            colorTargets = listOf(
                SDLGPUColorTargetInfo(
                    texture = targetTexture,
                    clearColor = SDLColor(18, 18, 24, 255),
                ),
            ),
        ) ?: return

        // Push the vertex shader UBO (scale/translation). packVertices already
        // subtracts DisplayPos from the vertex positions, so the translation is
        // just the NDC offset; the shader computes gl_Position = pos * scale + translate.
        // DisplaySize is in logical units (imgui 1.92+); the viewport below is
        // physical, so the vertex shader maps logical -> NDC and the GPU maps
        // NDC -> physical viewport.
        val ubo = ByteArray(16)
        floatBitsToBytes(2f / drawData.displaySize.x, ubo, 0)
        floatBitsToBytes(2f / drawData.displaySize.y, ubo, 4)
        floatBitsToBytes(-1f, ubo, 8)
        floatBitsToBytes(-1f, ubo, 12)
        commandBuffer.pushVertexUniformData(0, ubo)

        pass.bindGraphicsPipeline(pipeline!!)
        pass.setViewport(
            SDLGPUViewport(
                x = 0f,
                y = 0f,
                width = viewportWidth.toFloat(),
                height = viewportHeight.toFloat(),
            ),
        )
        pass.setScissor(0, 0, viewportWidth, viewportHeight)

        val scaleX = viewportWidth.toFloat() / drawData.displaySize.x
        val scaleY = viewportHeight.toFloat() / drawData.displaySize.y
        var bufIdx = 0
        for (listIndex in 0 until drawData.cmdListsCount) {
            val list = drawData.cmdList(listIndex)
            if (list.vtxCount == 0 || list.idxCount == 0) continue
            val buffers = listBuffers[bufIdx++]

            pass.bindVertexBuffers(buffers.vb to 0)
            pass.bindIndexBuffer(buffers.ib, SDLGPUIndexElementSize.UINT16)

            for (cmdIndex in 0 until list.cmdCount) {
                val cmd = list.cmd(cmdIndex)
                if (cmd.hasUserCallback) continue
                val texture = textures[cmd.texId] ?: continue

                // Project the clip rect from logical into framebuffer space and
                // clamp to the viewport (SDL_SetGPUScissor rejects out-of-bounds
                // rects). The scale is derived from the actual viewport size vs
                // the logical display size - NOT from drawData.framebufferScale,
                // which may be stale/wrong on some SDL builds (e.g. high-DPI
                // windows where the swapchain is physical but DisplaySize is
                // logical). This guarantees the scissor always matches the
                // viewport we render into.
                val clipX1 = max(0f, (cmd.clipRect.x - drawData.displayPos.x) * scaleX)
                val clipY1 = max(0f, (cmd.clipRect.y - drawData.displayPos.y) * scaleY)
                val clipX2 = min(viewportWidth.toFloat(), (cmd.clipRect.z - drawData.displayPos.x) * scaleX)
                val clipY2 = min(viewportHeight.toFloat(), (cmd.clipRect.w - drawData.displayPos.y) * scaleY)
                if (clipX2 <= clipX1 || clipY2 <= clipY1) continue
                pass.setScissor(clipX1.toInt(), clipY1.toInt(), (clipX2 - clipX1).toInt(), (clipY2 - clipY1).toInt())

                // SDL3's SDL_BindGPUFragmentSamplers requires every binding to
                // carry both a texture and a sampler, so bind them together.
                pass.bindGraphicsTextureSamplers(0, texture to sampler!!)
                pass.drawIndexedPrimitives(cmd.elemCount, 1, cmd.idxOffset, cmd.vtxOffset, 0)
            }
        }
        pass.end()

        // The per-list buffers are only referenced by the command buffer this
        // frame; releasing them after submit would be too early, so they are
        // released on the next frame cycle instead (kept in a pending list).
        for (buffers in listBuffers) {
            pendingBuffers.add(buffers.vb)
            pendingBuffers.add(buffers.ib)
        }
        if (pendingBuffers.size > 256) {
            repeat(64) { pendingBuffers.removeAt(0).close() }
        }
    }

    private val pendingBuffers = ArrayList<SDLGPUBuffer>()

    fun close() {
        pendingBuffers.forEach { it.close() }
        pendingBuffers.clear()
        vertexBuffer?.close()
        vertexBuffer = null
        indexBuffer?.close()
        indexBuffer = null
        textures.values.forEach { it.close() }
        textures.clear()
        sampler?.close()
        pipeline?.close()
        fragmentShader?.close()
        vertexShader?.close()
    }

}
