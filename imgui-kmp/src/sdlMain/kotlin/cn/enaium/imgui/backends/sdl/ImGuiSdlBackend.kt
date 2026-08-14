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
 * time). Rendering is handled separately by [ImGuiSdlRendererBackend] or
 * [ImGuiSdlGpuBackend].
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

    /**
     * The ratio between physical and logical pixels (e.g. 2.0 on Retina).
     * Matches imgui 1.92+'s expectation: [displaySize] holds logical units
     * and this scale drives the font rasterizer density, keeping text crisp.
     */
    val framebufferScale: ImVec2
        get() {
            val logical = window.size
            val physical = window.sizeInPixels
            if (logical.x <= 0 || logical.y <= 0) return ImVec2(1f, 1f)
            return ImVec2(
                physical.x.toFloat() / logical.x.toFloat(),
                physical.y.toFloat() / logical.y.toFloat(),
            )
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
                updateKeyModifiers(event.modifiers)
                if (!event.repeat) {
                    io.addKeyEvent(sdlKeycodeToImguiKey(event.keycode), event.down)
                }
            }

            is SDLEvent.TextInput -> io.addInputCharactersUTF8(event.text)

            else -> Unit
        }
    }

    /** Synchronizes the modifier state (Ctrl/Shift/Alt/Super) from the SDL bitmask. */
    private fun updateKeyModifiers(modifiers: Int) {
        io.addKeyEvent(ImGuiKey.MOD_CTRL, modifiers and cn.enaium.sdl.SDLKeymod.CTRL != 0)
        io.addKeyEvent(ImGuiKey.MOD_SHIFT, modifiers and cn.enaium.sdl.SDLKeymod.SHIFT != 0)
        io.addKeyEvent(ImGuiKey.MOD_ALT, modifiers and cn.enaium.sdl.SDLKeymod.ALT != 0)
        io.addKeyEvent(ImGuiKey.MOD_SUPER, modifiers and cn.enaium.sdl.SDLKeymod.GUI != 0)
    }

    /** Starts a new imgui frame. Call once per render loop iteration. */
    fun newFrame() {
        // imgui 1.92+ expects DisplaySize in LOGICAL units and
        // DisplayFramebufferScale = physical/logical ratio; the font
        // rasterizer density (antialiasing) is derived from the latter.
        val logical = window.size
        val scale = framebufferScale
        io.displaySize = ImVec2(logical.x.toFloat(), logical.y.toFloat())
        io.displayFramebufferScale = scale
        io.deltaTime = computeDeltaTime()

        // Toggle SDL text input to match imgui's need (typing in a widget).
        // Keeps SDL_TextInput events flowing while an edit box is focused.
        val wantText = io.wantTextInput
        val active = cn.enaium.sdl.SDL.textInputActive(window.id)
        if (wantText && !active) {
            cn.enaium.sdl.SDL.startTextInput(window.id)
        }

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

        // Editing / navigation
        SDLKeycode.INSERT -> ImGuiKey.INSERT
        SDLKeycode.HOME -> ImGuiKey.HOME
        SDLKeycode.END -> ImGuiKey.END
        SDLKeycode.PAGEUP -> ImGuiKey.PAGE_UP
        SDLKeycode.PAGEDOWN -> ImGuiKey.PAGE_DOWN
        SDLKeycode.LEFT -> ImGuiKey.LEFT_ARROW
        SDLKeycode.RIGHT -> ImGuiKey.RIGHT_ARROW
        SDLKeycode.UP -> ImGuiKey.UP_ARROW
        SDLKeycode.DOWN -> ImGuiKey.DOWN_ARROW

        // Printable punctuation (ASCII keycodes)
        SDLKeycode.APOSTROPHE -> ImGuiKey.APOSTROPHE
        SDLKeycode.COMMA -> ImGuiKey.COMMA
        SDLKeycode.MINUS -> ImGuiKey.MINUS
        SDLKeycode.PERIOD -> ImGuiKey.PERIOD
        SDLKeycode.SLASH -> ImGuiKey.SLASH
        SDLKeycode.SEMICOLON -> ImGuiKey.SEMICOLON
        SDLKeycode.EQUALS -> ImGuiKey.EQUAL
        SDLKeycode.LEFTBRACKET -> ImGuiKey.LEFT_BRACKET
        SDLKeycode.BACKSLASH -> ImGuiKey.BACKSLASH
        SDLKeycode.RIGHTBRACKET -> ImGuiKey.RIGHT_BRACKET
        SDLKeycode.GRAVE -> ImGuiKey.GRAVE_ACCENT

        // Number row
        in SDLKeycode.KEY_0_START..SDLKeycode.KEY_0_END -> ImGuiKey.KEY_0 + (keycode - SDLKeycode.KEY_0_START)

        // Letters
        in SDLKeycode.A..SDLKeycode.Z -> ImGuiKey.A + (keycode - SDLKeycode.A)

        // Function keys
        in SDLKeycode.F1..SDLKeycode.F12 -> ImGuiKey.F1 + (keycode - SDLKeycode.F1)
        in SDLKeycode.F13..SDLKeycode.F24 -> ImGuiKey.F13 + (keycode - SDLKeycode.F13)

        // Lock / system keys
        SDLKeycode.CAPSLOCK -> ImGuiKey.CAPS_LOCK
        SDLKeycode.SCROLLLOCK -> ImGuiKey.SCROLL_LOCK
        SDLKeycode.NUMLOCKCLEAR -> ImGuiKey.NUM_LOCK
        SDLKeycode.PRINTSCREEN -> ImGuiKey.PRINT_SCREEN
        SDLKeycode.PAUSE -> ImGuiKey.PAUSE
        SDLKeycode.APPLICATION -> ImGuiKey.MENU

        // Modifier keys
        SDLKeycode.LCTRL -> ImGuiKey.LEFT_CTRL
        SDLKeycode.LSHIFT -> ImGuiKey.LEFT_SHIFT
        SDLKeycode.LALT -> ImGuiKey.LEFT_ALT
        SDLKeycode.LGUI -> ImGuiKey.LEFT_SUPER
        SDLKeycode.RCTRL -> ImGuiKey.RIGHT_CTRL
        SDLKeycode.RSHIFT -> ImGuiKey.RIGHT_SHIFT
        SDLKeycode.RALT -> ImGuiKey.RIGHT_ALT
        SDLKeycode.RGUI -> ImGuiKey.RIGHT_SUPER

        // Keypad
        SDLKeycode.KP_0 -> ImGuiKey.KEYPAD_0
        SDLKeycode.KP_1 -> ImGuiKey.KEYPAD_1
        SDLKeycode.KP_2 -> ImGuiKey.KEYPAD_2
        SDLKeycode.KP_3 -> ImGuiKey.KEYPAD_3
        SDLKeycode.KP_4 -> ImGuiKey.KEYPAD_4
        SDLKeycode.KP_5 -> ImGuiKey.KEYPAD_5
        SDLKeycode.KP_6 -> ImGuiKey.KEYPAD_6
        SDLKeycode.KP_7 -> ImGuiKey.KEYPAD_7
        SDLKeycode.KP_8 -> ImGuiKey.KEYPAD_8
        SDLKeycode.KP_9 -> ImGuiKey.KEYPAD_9
        SDLKeycode.KP_PERIOD -> ImGuiKey.KEYPAD_DECIMAL
        SDLKeycode.KP_DIVIDE -> ImGuiKey.KEYPAD_DIVIDE
        SDLKeycode.KP_MULTIPLY -> ImGuiKey.KEYPAD_MULTIPLY
        SDLKeycode.KP_MINUS -> ImGuiKey.KEYPAD_SUBTRACT
        SDLKeycode.KP_PLUS -> ImGuiKey.KEYPAD_ADD
        SDLKeycode.KP_ENTER -> ImGuiKey.KEYPAD_ENTER
        SDLKeycode.KP_EQUALS -> ImGuiKey.KEYPAD_EQUAL

        else -> 0 // ImGuiKey_None
    }
}
