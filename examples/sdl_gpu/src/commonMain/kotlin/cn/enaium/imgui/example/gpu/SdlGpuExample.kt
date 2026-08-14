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

package cn.enaium.imgui.example.gpu

import cn.enaium.imgui.ImDrawData
import cn.enaium.imgui.ImGui
import cn.enaium.imgui.example.common.DemoUi
import cn.enaium.imgui.example.common.ImGuiSdlBackend
import cn.enaium.sdl.SDL
import cn.enaium.sdl.SDLColor
import cn.enaium.sdl.SDLGPU
import cn.enaium.sdl.SDLGPUBuffer
import cn.enaium.sdl.SDLGPUBufferCreateInfo
import cn.enaium.sdl.SDLGPUBufferUsage
import cn.enaium.sdl.SDLGPUColorTargetDescription
import cn.enaium.sdl.SDLGPUColorTargetInfo
import cn.enaium.sdl.SDLGPUGraphicsPipelineCreateInfo
import cn.enaium.sdl.SDLGPUIndexElementSize
import cn.enaium.sdl.SDLGPUGraphicsPipeline
import cn.enaium.sdl.SDLGPUPrimitiveType
import cn.enaium.sdl.SDLGPUShader
import cn.enaium.sdl.SDLGPUShaderFormat
import cn.enaium.sdl.SDLGPUShaderStage
import cn.enaium.sdl.SDLGPUTexture
import cn.enaium.sdl.SDLGPUTextureCreateInfo
import cn.enaium.sdl.SDLGPUTextureUsage
import cn.enaium.sdl.SDLGPUVertexAttribute
import cn.enaium.sdl.SDLGPUVertexBufferDescription
import cn.enaium.sdl.SDLGPUVertexElementFormat
import cn.enaium.sdl.SDLGPUVertexInputRate
import cn.enaium.sdl.SDLGPUVertexInputState
import cn.enaium.sdl.SDLGPUViewport
import cn.enaium.sdl.SDLInitFlags
import cn.enaium.sdl.SDLWindow
import cn.enaium.sdl.SDLWindowFlags
import kotlin.math.max
import kotlin.math.min

/** ImDrawVert layout: pos (2 floats) + uv (2 floats) + col (u8x4) = 20 bytes. */
private const val VERTEX_STRIDE = 20

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
    private val device: cn.enaium.sdl.SDLGPUDevice,
    private val window: SDLWindow,
) {
    private val vertexShader: SDLGPUShader?
    private val fragmentShader: SDLGPUShader?
    private val pipeline: SDLGPUGraphicsPipeline?
    private val sampler: cn.enaium.sdl.SDLGPUSampler?
    private val textures = mutableMapOf<Long, SDLGPUTexture>()

    private var vertexBuffer: SDLGPUBuffer? = null
    private var indexBuffer: SDLGPUBuffer? = null
    private var vertexCapacity = 0
    private var indexCapacity = 0

    init {
        val windowFormat = device.getWindowFormat(window)
            ?: error("SDL_GetGPUSwapchainTextureFormat failed: ${SDL.error()}")

        // The sdlgpu3 backend ships precompiled SPIR-V (Vulkan) and MSL
        // (Metal) shaders; pick whichever this device supports.
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
                        SDLGPUVertexAttribute(location = 2, bufferSlot = 0, format = SDLGPUVertexElementFormat.UBYTE4_NORM, offset = 16),
                    ),
                ),
                primitiveType = SDLGPUPrimitiveType.TRIANGLELIST,
                targetDescriptions = listOf(
                    SDLGPUColorTargetDescription(
                        format = windowFormat,
                        blendState = cn.enaium.sdl.SDLGPUBlendState(
                            srcColorBlendFactor = cn.enaium.sdl.SDLGPUBlendFactor.SRC_ALPHA,
                            dstColorBlendFactor = cn.enaium.sdl.SDLGPUBlendFactor.ONE_MINUS_SRC_ALPHA,
                            colorBlendOp = cn.enaium.sdl.SDLGPUBlendOp.ADD,
                            srcAlphaBlendFactor = cn.enaium.sdl.SDLGPUBlendFactor.ONE,
                            dstAlphaBlendFactor = cn.enaium.sdl.SDLGPUBlendFactor.ONE_MINUS_SRC_ALPHA,
                            alphaBlendOp = cn.enaium.sdl.SDLGPUBlendOp.ADD,
                        ),
                    ),
                ),
            ),
        )
        check(pipeline != null) { "pipeline creation failed: ${SDL.error()}" }

        sampler = device.createSampler(
            cn.enaium.sdl.SDLGPUSamplerCreateInfo(
                minFilter = cn.enaium.sdl.SDLGPUFilter.LINEAR,
                magFilter = cn.enaium.sdl.SDLGPUFilter.LINEAR,
                mipmapMode = cn.enaium.sdl.SDLGPUSamplerMipmapMode.LINEAR,
                addressModeU = cn.enaium.sdl.SDLGPUSamplerAddressMode.CLAMP_TO_EDGE,
                addressModeV = cn.enaium.sdl.SDLGPUSamplerAddressMode.CLAMP_TO_EDGE,
                addressModeW = cn.enaium.sdl.SDLGPUSamplerAddressMode.CLAMP_TO_EDGE,
            ),
        )
        check(sampler != null) { "sampler creation failed: ${SDL.error()}" }
    }

    /** Uploads the font atlas pixels and returns the texture id. */
    fun uploadFontTexture(pixels: ByteArray, width: Int, height: Int): Long {
        val texture = device.createTexture(
            SDLGPUTextureCreateInfo(
                format = cn.enaium.sdl.SDLGPUTextureFormat.R8G8B8A8_UNORM,
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
    private fun packVertices(verts: cn.enaium.imgui.ImDrawVertData, displayPos: cn.enaium.imgui.ImVec2): ByteArray {
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
            out[base + 16] = (col and 0xFF).toByte() // R
            out[base + 17] = ((col shr 8) and 0xFF).toByte() // G
            out[base + 18] = ((col shr 16) and 0xFF).toByte() // B
            out[base + 19] = ((col shr 24) and 0xFF).toByte() // A
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
     */
    fun renderFrame(
        commandBuffer: cn.enaium.sdl.SDLGPUCommandBuffer,
        targetTexture: SDLGPUTexture,
        viewportWidth: Int,
        viewportHeight: Int,
    ) {
        val pass = commandBuffer.beginRenderPass(
            colorTargets = listOf(
                SDLGPUColorTargetInfo(
                    texture = targetTexture,
                    clearColor = SDLColor(18, 18, 24, 255),
                ),
            ),
        ) ?: return

        val drawData = ImGui.getDrawData()
        var totalVtx = 0
        var totalIdx = 0
        for (i in 0 until drawData.cmdListsCount) {
            val list = drawData.cmdList(i)
            totalVtx += list.vtxCount
            totalIdx += list.idxCount
        }
        ensureBuffers(totalVtx, totalIdx)

        // Push the vertex shader UBO (scale/translation). packVertices already
        // subtracts DisplayPos from the vertex positions, so the translation is
        // just the NDC offset; the shader computes gl_Position = pos * scale + translate.
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

        // Merge every draw list's vertices/indices into one buffer (like the
        // C++ backend) so no list overwrites another mid-frame.
        var totalVtxBytes = 0
        var totalIdxBytes = 0
        for (i in 0 until drawData.cmdListsCount) {
            val list = drawData.cmdList(i)
            totalVtxBytes += list.vtxCount * VERTEX_STRIDE
            totalIdxBytes += list.idxCount * 2
        }
        val mergedVtx = ByteArray(totalVtxBytes)
        val mergedIdx = ByteArray(totalIdxBytes)
        var vtxOffset = 0
        var idxOffset = 0
        var curVtx = 0
        var curIdx = 0
        for (i in 0 until drawData.cmdListsCount) {
            val list = drawData.cmdList(i)
            if (list.vtxCount == 0) continue
            val verts = list.copyVtx(0, list.vtxCount)
            val indices = list.copyIdx(0, list.idxCount)
            val vtxBytes = packVertices(verts, drawData.displayPos)
            val idxBytes = packIndices(indices)
            vtxBytes.copyInto(mergedVtx, curVtx)
            idxBytes.copyInto(mergedIdx, curIdx)
            curVtx += vtxBytes.size
            curIdx += idxBytes.size
            vtxOffset += list.vtxCount
            idxOffset += list.idxCount
        }
        check(vertexBuffer!!.setData(mergedVtx)) { "vertex upload failed: ${SDL.error()}" }
        check(indexBuffer!!.setData(mergedIdx)) { "index upload failed: ${SDL.error()}" }
        pass.bindVertexBuffers(vertexBuffer!! to 0)
        pass.bindIndexBuffer(indexBuffer!!, SDLGPUIndexElementSize.UINT16)

        var globalVtx = 0
        var globalIdx = 0
        for (listIndex in 0 until drawData.cmdListsCount) {
            val list = drawData.cmdList(listIndex)
            if (list.vtxCount == 0) continue

            for (cmdIndex in 0 until list.cmdCount) {
                val cmd = list.cmd(cmdIndex)
                if (cmd.hasUserCallback) continue
                val texture = textures[cmd.texId] ?: continue

                // Project the clip rect into framebuffer space and clamp to the
                // viewport (SDL_SetGPUScissor rejects out-of-bounds rects).
                val clipX1 = max(0f, cmd.clipRect.x - drawData.displayPos.x)
                val clipY1 = max(0f, cmd.clipRect.y - drawData.displayPos.y)
                val clipX2 = min(viewportWidth.toFloat(), cmd.clipRect.z - drawData.displayPos.x)
                val clipY2 = min(viewportHeight.toFloat(), cmd.clipRect.w - drawData.displayPos.y)
                if (clipX2 <= clipX1 || clipY2 <= clipY1) continue
                pass.setScissor(clipX1.toInt(), clipY1.toInt(), (clipX2 - clipX1).toInt(), (clipY2 - clipY1).toInt())

                // SDL3's SDL_BindGPUFragmentSamplers requires every binding to
                // carry both a texture and a sampler, so bind them together.
                pass.bindGraphicsTextureSamplers(0, texture to sampler!!)
                pass.drawIndexedPrimitives(
                    indexCount = cmd.elemCount,
                    firstIndex = cmd.idxOffset + globalIdx,
                    vertexOffset = cmd.vtxOffset + globalVtx,
                )
            }
            globalVtx += list.vtxCount
            globalIdx += list.idxCount
        }
        pass.end()
    }

    fun close() {
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

/**
 * Dear ImGui + ImPlot rendered through the SDL3 GPU API.
 *
 * Run with `./gradlew :examples:sdl_gpu:run` (JVM) or the per-target native binaries.
 * Pass `--frames N` to exit after N frames (useful for headless CI runs).
 */
fun runSdlGpuExample(frames: Int = Int.MAX_VALUE) {
    SDL.setMainReady()
    if (!SDL.init(SDLInitFlags.VIDEO or SDLInitFlags.EVENTS)) {
        error("SDL_Init failed: ${SDL.error()}")
    }
    println("SDL ${SDL.version()} (${SDL.revision()})")
    println("Video driver: ${SDL.getCurrentVideoDriver()}")
    println("GPU drivers: ${SDLGPU.drivers}")

    val device = SDLGPU.createDevice()
        ?: error("SDL_CreateGPUDevice failed: ${SDL.error()}")
    println("GPU shader formats: 0x${device.shaderFormats.toString(16)}")

    SDL.createWindow(
        title = "imgui-kmp gpu example",
        width = 1280,
        height = 800,
        flags = SDLWindowFlags.RESIZABLE,
    ).use { window ->
        device.use {
            check(device.claimWindow(window)) { "SDL_ClaimWindowForGPUDevice failed: ${SDL.error()}" }

            val context = ImGui.createContext()
            try {
                val imgui = ImGuiSdlBackend(window)
                val backend = ImGuiSdlGpuBackend(device, window)
                imgui.init()

                val fonts = ImGui.getIO().fonts
                fonts.addFontDefault()
                check(fonts.build()) { "font atlas build failed" }
                val texData = fonts.getTexDataAsRGBA32()
                fonts.setTexID(backend.uploadFontTexture(texData.pixels, texData.width, texData.height))

                val plotContext = cn.enaium.imgui.extensions.implot.ImPlot.createContext()
                cn.enaium.imgui.extensions.implot.ImPlot.setImGuiContext(context)
                val demoUi = DemoUi()

                var running = true
                var frameCount = 0
                while (running && frameCount < frames) {
                    // ---- events ----
                    while (true) {
                        val event = SDL.pollEvent() ?: break
                        when (event) {
                            is cn.enaium.sdl.SDLEvent.Quit -> running = false
                            is cn.enaium.sdl.SDLEvent.Window ->
                                if (event.type == cn.enaium.sdl.SDLWindowEventType.CLOSE_REQUESTED) running = false
                            else -> imgui.processEvent(event)
                        }
                    }

                    // ---- imgui frame ----
                    imgui.newFrame()
                    demoUi.draw(frameCount)
                    ImGui.render()

                    // ---- GPU frame ----
                    val cmd = device.beginCommandBuffer()
                    if (cmd == null) {
                        running = false
                        break
                    }
                    val windowTexture = device.acquireSwapchainTexture(cmd, window)
                    val targetTexture = windowTexture?.texture
                    val vw = windowTexture?.srcRect?.width ?: window.size.x
                    val vh = windowTexture?.srcRect?.height ?: window.size.y
                    if (targetTexture != null) {
                        backend.renderFrame(cmd, targetTexture, vw, vh)
                    }
                    cmd.end()
                    check(device.submit(cmd)) { "submit failed: ${SDL.error()}" }
                    device.present(window)
                    frameCount++
                }

                demoUi.close()
                cn.enaium.imgui.extensions.implot.ImPlot.destroyContext(plotContext)
                backend.close()
            } finally {
                ImGui.destroyContext(context)
            }
        }
    }
    SDL.quit()
}
