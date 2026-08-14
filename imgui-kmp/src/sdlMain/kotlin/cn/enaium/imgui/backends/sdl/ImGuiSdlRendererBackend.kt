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
import cn.enaium.imgui.ImGui
import cn.enaium.sdl.SDLColor
import cn.enaium.sdl.SDLFloatPoint
import cn.enaium.sdl.SDLPixelFormat
import cn.enaium.sdl.SDLPoint
import cn.enaium.sdl.SDLRect
import cn.enaium.sdl.SDLRenderer
import cn.enaium.sdl.SDLTexture
import cn.enaium.sdl.SDLTextureAccess
import cn.enaium.sdl.SDLVertex
import kotlin.math.max
import kotlin.math.min

/**
 * Renders the imgui draw data with the SDL3 2D renderer (SDL_RenderGeometry).
 * Mirrors imgui_impl_sdlrenderer3.cpp.
 */
class ImGuiSdlRendererBackend(private val renderer: SDLRenderer) {

    private val textures = mutableMapOf<Long, SDLTexture>()

    /** The renderer output size in pixels, used for the framebuffer scale. */
    val outputSize: SDLPoint
        get() = renderer.outputSize

    /**
     * Creates the font texture from the imgui font atlas pixels and returns
     * the texture id to hand to [cn.enaium.imgui.ImGuiIO.fonts.setTexID].
     */
    fun uploadFontTexture(pixels: ByteArray, width: Int, height: Int): Long {
        // imgui's GetTexDataAsRGBA32() returns an in-memory R,G,B,A byte
        // array, so the texture must use SDL_PIXELFORMAT_RGBA32 (ABGR8888 on
        // little-endian). RGBA8888 would swap the channels and break the
        // alpha, making glyphs look thick and blocky with colored backs.
        val texture = renderer.createTexture(
            format = SDLPixelFormat.RGBA32,
            access = SDLTextureAccess.STATIC,
            width = width,
            height = height,
        )
        texture.update(
            rect = null,
            pixels = pixels,
            pitch = width * 4,
        )
        // Match imgui_impl_sdlrenderer3.cpp: the font atlas needs alpha
        // blending (otherwise glyphs render as solid blocks) and linear
        // scaling (otherwise glyphs look chunky/aliased when scaled).
        texture.blendMode = cn.enaium.sdl.SDLBlendMode.BLEND
        texture.scaleMode = cn.enaium.sdl.SDLScaleMode.LINEAR
        textures[texture.ptr] = texture
        return texture.ptr
    }

    /** Issues the actual draw calls for the frame. */
    fun renderDrawData(drawData: ImDrawData) {
        // SDL3's renderer works in logical coordinates and maps them to the
        // physical framebuffer itself (Retina et al.), so vertices are passed
        // unscaled; only the framebuffer size and clip rects are in pixels.
        // The scale comes from the renderer's actual output size vs the
        // logical display size, not from drawData.framebufferScale (which may
        // be stale/wrong on some SDL builds).
        val scaleX = outputSize.x.toFloat() / drawData.displaySize.x
        val scaleY = outputSize.y.toFloat() / drawData.displaySize.y
        val displayW = outputSize.x
        val displayH = outputSize.y

        renderer.clipRect = null
        for (listIndex in 0 until drawData.cmdListsCount) {
            val list = drawData.cmdList(listIndex)
            if (list.vtxCount == 0) continue
            val verts = list.copyVtx(0, list.vtxCount)
            val indices = list.copyIdx(0, list.idxCount)

            for (cmdIndex in 0 until list.cmdCount) {
                val cmd = list.cmd(cmdIndex)
                if (cmd.hasUserCallback) continue

                val texture = textures[cmd.texId] ?: continue
                val clip = cmd.clipRect

                // Project the clipping rectangle into framebuffer space and
                // clamp it to the render target.
                val clipX1 = max(0, ((clip.x - drawData.displayPos.x) * scaleX).toInt())
                val clipY1 = max(0, ((clip.y - drawData.displayPos.y) * scaleY).toInt())
                val clipX2 = min(displayW, ((clip.z - drawData.displayPos.x) * scaleX).toInt())
                val clipY2 = min(displayH, ((clip.w - drawData.displayPos.y) * scaleY).toInt())
                if (clipX2 <= clipX1 || clipY2 <= clipY1) continue
                renderer.clipRect = SDLRect(clipX1, clipY1, clipX2 - clipX1, clipY2 - clipY1)

                val vtxOffset = cmd.vtxOffset
                // The command's indices reference vertices relative to
                // VtxOffset, spanning the rest of the list's vertex buffer.
                val vtxCount = list.vtxCount - vtxOffset
                val vertexList = ArrayList<SDLVertex>(vtxCount)
                for (i in 0 until vtxCount) {
                    val color = verts.colors[vtxOffset + i]
                    vertexList.add(
                        SDLVertex(
                            position = SDLFloatPoint(
                                x = verts.positions[(vtxOffset + i) * 2] - drawData.displayPos.x,
                                y = verts.positions[(vtxOffset + i) * 2 + 1] - drawData.displayPos.y,
                            ),
                            color = SDLColor(
                                // ImDrawVert::col is packed as 0xAABBGGRR
                                // (IM_COL32 default, not IMGUI_USE_BGRA_PACKED_COLOR).
                                r = color and 0xFF,
                                g = (color shr 8) and 0xFF,
                                b = (color shr 16) and 0xFF,
                                a = (color shr 24) and 0xFF,
                            ),
                            texCoord = SDLFloatPoint(
                                x = verts.uvs[(vtxOffset + i) * 2],
                                y = verts.uvs[(vtxOffset + i) * 2 + 1],
                            ),
                        ),
                    )
                }
                val cmdIndices = IntArray(cmd.elemCount) { i -> indices[cmd.idxOffset + i] }
                renderer.renderGeometry(texture, vertexList, cmdIndices)
            }
        }
        renderer.clipRect = null
    }

    fun close() {
        textures.values.forEach { it.close() }
        textures.clear()
    }
}
