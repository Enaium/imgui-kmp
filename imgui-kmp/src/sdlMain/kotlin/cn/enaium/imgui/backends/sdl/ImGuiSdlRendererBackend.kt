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
import cn.enaium.imgui.ImTextureID
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
     * the [ImTextureID] to hand to [cn.enaium.imgui.ImGuiIO.fonts.setTexID].
     */
    fun uploadFontTexture(pixels: ByteArray, width: Int, height: Int): ImTextureID {
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
        registerTexture(texture)
        return texture.toImTextureID()
    }

    /**
     * Registers a texture under its [ImTextureID] so draw commands
     * referencing it can render. The backend takes ownership: [close]
     * releases every registered texture.
     */
    fun registerTexture(texture: SDLTexture) {
        textures[texture.ptr] = texture
    }

    /** Releases a previously registered texture (no-op if unknown). */
    fun unregisterTexture(texture: SDLTexture) {
        if (textures.remove(texture.ptr) != null) {
            texture.close()
        }
    }

    /** Issues the actual draw calls for the frame. */
    fun renderDrawData(drawData: ImDrawData) {
        // ImGui draws in LOGICAL units where `io.displaySize` == the SDL
        // window's logical size (e.g. 1280x720). SDL3's 2D renderer does NOT
        // automatically map those to the physical framebuffer for
        // `SDL_RenderGeometry`: geometry is scaled only by the renderer's
        // `SDL_RenderSetScale` value (default 1.0), NOT by its internal
        // dpi_scale. So on a high-DPI display (Retina, or any display where
        // `window.sizeInPixels` > `window.size`) ImGui's logical vertices
        // must be explicitly projected to framebuffer pixels, otherwise the
        // UI renders at 1/scale size in the top-left corner and the cursor
        // (hit-tested in logical space) no longer lines up with the widgets.
        //
        // The projection factor is physical render output / logical display
        // size (== drawData.framebufferScale when the backend sets it
        // correctly; deriving it from the renderer's own output size keeps it
        // from drifting). The same factor is applied to the vertices below
        // and to the clip rects, so both live in framebuffer-pixel space and
        // SDL renders the whole frame 1:1.
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

                val texture = textures[cmd.textureId.value.toLong()] ?: continue
                val clip = cmd.clipRect

                // Project the clipping rectangle into framebuffer space and
                // clamp it to the render target.
                val clipX1 = max(0, ((clip.x - drawData.displayPos.x) * scaleX).toInt())
                val clipY1 = max(0, ((clip.y - drawData.displayPos.y) * scaleY).toInt())
                val clipX2 = min(displayW, ((clip.z - drawData.displayPos.x) * scaleX).toInt())
                val clipY2 = min(displayH, ((clip.w - drawData.displayPos.y) * scaleY).toInt())
                if (clipX2 <= clipX1 || clipY2 <= clipY1) continue
                renderer.clipRect = SDLRect(clipX1, clipY1, clipX2 - clipX1, clipY2 - clipY1)

                // Only the vertices this command actually references are
                // copied, and they are rebased so the indices stay valid.
                //
                // ImGui keeps its indices 16-bit: the values in the index
                // buffer are relative to `VtxOffset`, which is the absolute
                // base of the vertex block the command draws from. It is 0
                // for a normal draw list and only becomes non-zero once a
                // list exceeds 64K vertices (large meshes), so both the
                // offset and the referenced range have to be taken into
                // account. Spanning from `VtxOffset` instead - the values are
                // what the buffer holds, not the vertices they point at -
                // copies everything up to this command again for every
                // command, which is O(n^2) in vertices and stalls a frame
                // with only a few thousand of them.
                val range = referencedVertices(indices, cmd.idxOffset, cmd.elemCount, cmd.vtxOffset) ?: continue
                val vtxFirst = range.first - cmd.vtxOffset
                val vtxCount = range.last - range.first + 1
                val vertexList = ArrayList<SDLVertex>(vtxCount)
                for (i in 0 until vtxCount) {
                    val vertex = range.first + i
                    val color = verts.colors[vertex]
                    vertexList.add(
                        SDLVertex(
                            // Project the ImGui logical vertex position into
                            // framebuffer pixels (same factor as the clip rect).
                            position = SDLFloatPoint(
                                x = (verts.positions[vertex * 2] - drawData.displayPos.x) * scaleX,
                                y = (verts.positions[vertex * 2 + 1] - drawData.displayPos.y) * scaleY,
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
                                x = verts.uvs[vertex * 2],
                                y = verts.uvs[vertex * 2 + 1],
                            ),
                        ),
                    )
                }
                val cmdIndices = IntArray(cmd.elemCount) { i -> indices[cmd.idxOffset + i] - vtxFirst }
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

/**
 * The absolute range of vertices [cmd]'s indices reference.
 *
 * The index buffer holds 16-bit values relative to `vtxOffset`, the absolute
 * base of the vertex block the command draws from, so the referenced vertices
 * are `vtxOffset + index`. Only that range has to be copied - copying from
 * `vtxOffset` up to the last referenced vertex instead re-copies everything
 * before the command for every command, which is O(n^2) in the number of
 * commands and stalls a frame that only draws a few thousand vertices.
 *
 * Returns `null` when the command draws nothing.
 */
internal fun referencedVertices(
    indices: IntArray,
    idxOffset: Int,
    elemCount: Int,
    vtxOffset: Int,
): IntRange? {
    var first = Int.MAX_VALUE
    var last = -1
    for (i in idxOffset until idxOffset + elemCount) {
        val v = indices[i]
        if (v < first) first = v
        if (v > last) last = v
    }
    if (last < first) return null
    return (vtxOffset + first)..(vtxOffset + last)
}
