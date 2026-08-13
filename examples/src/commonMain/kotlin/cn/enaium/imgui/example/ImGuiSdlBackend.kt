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

package cn.enaium.imgui.example

import cn.enaium.imgui.ImGui
import cn.enaium.imgui.ImGuiKey
import cn.enaium.imgui.ImVec2
import cn.enaium.sdl.SDLEvent
import cn.enaium.sdl.SDLKeycode
import cn.enaium.sdl.SDLWindow

/**
 * The Dear ImGui platform backend, implemented in Kotlin on top of sdl-kmp.
 *
 * Mirrors what imgui_impl_sdl3.cpp does in C: it feeds SDL events into the
 * imgui input queue and sets the per-frame IO values (display size, delta
 * time). Rendering is handled separately by a [ImGuiSdlRenderer] or an
 * [ImGuiSdlGpuRenderer].
 */
class ImGuiSdlBackend(private val window: SDLWindow) {

    private val io = ImGui.getIO()
    private var lastTime: ULong = 0uL

    /** The display size in pixels (backing scale applied). */
    val sizeInPixels: ImVec2
        get() {
            val size = window.sizeInPixels
            return ImVec2(size.x.toFloat(), size.y.toFloat())
        }

    fun init() {
        // Let imgui remember widget layout across runs in a file next to the app.
        io.iniFilename = null
        io.configFlags = cn.enaium.imgui.ImGuiConfigFlags.NAV_ENABLE_KEYBOARD
        lastTime = cn.enaium.sdl.SDL.getTicks()
    }

    /** Feeds one SDL event into the imgui input queue. */
    fun processEvent(event: SDLEvent) {
        when (event) {
            is SDLEvent.MouseMotion -> io.addMousePosEvent(event.x, event.y)

            is SDLEvent.MouseButton -> {
                val button = when (event.button) {
                    1 -> 0 // left
                    2 -> 1 // middle
                    3 -> 2 // right
                    else -> return
                }
                io.addMouseButtonEvent(button, event.down)
                io.addMousePosEvent(event.x, event.y)
            }

            is SDLEvent.MouseWheel -> io.addMouseWheelEvent(event.x, event.y)

            is SDLEvent.Key -> {
                if (!event.repeat) {
                    io.addKeyEvent(sdlKeycodeToImguiKey(event.keycode), event.down)
                    // Modifiers (SDL_KMOD_CTRL = 0x000C, SHIFT = 0x0003, ALT = 0x0030, GUI = 0x00C0)
                    io.addKeyEvent(ImGuiKey.MOD_CTRL, event.modifiers and 0x000C != 0)
                    io.addKeyEvent(ImGuiKey.MOD_SHIFT, event.modifiers and 0x0003 != 0)
                    io.addKeyEvent(ImGuiKey.MOD_ALT, event.modifiers and 0x0030 != 0)
                    io.addKeyEvent(ImGuiKey.MOD_SUPER, event.modifiers and 0x00C0 != 0)
                }
            }

            is SDLEvent.TextInput -> io.addInputCharactersUTF8(event.text)

            else -> Unit
        }
    }

    /** Starts a new imgui frame. Call once per render loop iteration. */
    fun newFrame() {
        val size = sizeInPixels
        io.displaySize = size
        io.deltaTime = computeDeltaTime()
        ImGui.newFrame()
    }

    private fun computeDeltaTime(): Float {
        val now = cn.enaium.sdl.SDL.getTicks()
        var delta = (now - lastTime).toDouble().toFloat() / 1000f
        lastTime = now
        // imgui asserts dt > 0; clamp tiny values away.
        if (delta <= 0f) delta = 1f / 60f
        return delta
    }

    private fun sdlKeycodeToImguiKey(keycode: Int): Int = when (keycode) {
        SDLKeycode.RETURN -> ImGuiKey.ENTER
        SDLKeycode.ESCAPE -> ImGuiKey.ESCAPE
        SDLKeycode.BACKSPACE -> ImGuiKey.BACKSPACE
        SDLKeycode.TAB -> ImGuiKey.TAB
        SDLKeycode.SPACE -> ImGuiKey.SPACE
        SDLKeycode.DELETE -> ImGuiKey.DELETE
        SDLKeycode.LEFT -> ImGuiKey.LEFT_ARROW
        SDLKeycode.RIGHT -> ImGuiKey.RIGHT_ARROW
        SDLKeycode.UP -> ImGuiKey.UP_ARROW
        SDLKeycode.DOWN -> ImGuiKey.DOWN_ARROW
        0x4000003a -> ImGuiKey.F1
        0x4000003b -> ImGuiKey.F2
        0x4000003c -> ImGuiKey.F3
        0x4000003d -> ImGuiKey.F4
        0x4000003e -> ImGuiKey.F5
        0x4000003f -> ImGuiKey.F6
        0x40000040 -> ImGuiKey.F7
        0x40000041 -> ImGuiKey.F8
        0x40000042 -> ImGuiKey.F9
        0x40000043 -> ImGuiKey.F10
        0x40000044 -> ImGuiKey.F11
        0x40000045 -> ImGuiKey.F12
        0x40000049 -> ImGuiKey.INSERT
        0x4000004a -> ImGuiKey.HOME
        0x4000004b -> ImGuiKey.PAGE_UP
        0x4000004c -> ImGuiKey.DELETE
        0x4000004d -> ImGuiKey.END
        0x4000004e -> ImGuiKey.PAGE_DOWN
        in 0x30..0x39 -> ImGuiKey.KEY_0 + (keycode - 0x30)
        in 0x61..0x7a -> ImGuiKey.A + (keycode - 0x61)
        else -> 0 // ImGuiKey_None
    }
}
