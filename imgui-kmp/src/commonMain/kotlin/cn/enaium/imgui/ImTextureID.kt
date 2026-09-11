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

package cn.enaium.imgui

/**
 * An opaque texture identifier understood by the renderer backend, mirroring
 * Dear ImGui's `ImTextureID` (`ImU64`).
 *
 * The imgui core bindings never interpret this value: it is produced by a
 * renderer backend (e.g. the SDL backends' [ImGuiTextureIdProvider]s) and
 * consumed verbatim by [ImGui.image], [ImGui.imageButton], [ImGui.imageWithBg]
 * and the draw list image APIs.
 *
 * ```kotlin
 * val texture = sdlRendererBackend.toImTextureID(sdlTexture)
 * ImGui.image(texture, ImVec2(400f, 300f))
 * ```
 */
expect value class ImTextureID(val value: ULong) {
    companion object {
        /** Creates an id from the raw backend handle (bit-identical to `ImTextureID`). */
        fun fromLong(value: Long): ImTextureID
    }
}

/**
 * Implemented by renderer-owned texture handles so [ImTextureID]s can be
 * obtained without the imgui core knowing about any concrete renderer API.
 *
 * The SDL backends provide extension functions that convert their texture
 * types, e.g. `texture.toImTextureID()` for an `SDLTexture` or
 * `SDLGPUTexture` (plus its sampler).
 */
interface ImGuiTextureIdProvider {
    fun toImTextureID(): ImTextureID
}