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

import cn.enaium.imgui.ImTextureID
import cn.enaium.sdl.SDLGPUTexture
import cn.enaium.sdl.SDLTexture

/**
 * Converts an SDL 2D renderer texture into an [ImTextureID] for use with
 * [cn.enaium.imgui.ImGui.image] & co. The texture must be registered with
 * the renderer backend ([ImGuiSdlRendererBackend.registerTexture]) so draw
 * commands can resolve it; the backend owns its lifetime.
 */
fun SDLTexture.toImTextureID(): ImTextureID = ImTextureID.fromLong(ptr)

/**
 * Converts an SDL GPU texture into an [ImTextureID] for use with
 * [cn.enaium.imgui.ImGui.image] & co. The texture must be registered with
 * the GPU backend ([ImGuiSdlGpuBackend.registerTexture]) so draw commands
 * can resolve it; the backend owns its lifetime.
 */
fun SDLGPUTexture.toImTextureID(): ImTextureID = ImTextureID.fromLong(ptr)