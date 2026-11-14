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

package cn.enaium.imgui.example.common

import cn.enaium.imgui.ImFontConfig
import cn.enaium.imgui.ImTextureID
import cn.enaium.imgui.ImGui
import cn.enaium.imgui.backends.sdl.ImGuiSdlBackend
import cn.enaium.imgui.backends.sdl.ImGuiSdlRendererBackend
import cn.enaium.sdl.SDL
import cn.enaium.sdl.SDLColor
import cn.enaium.sdl.SDLInitFlags
import cn.enaium.sdl.SDLWindowFlags

/**
 * Bootstraps an SDL3 window + 2D renderer + the imgui renderer backend and
 * runs the frame loop, delegating the actual UI to a [draw] callback.
 *
 * Shared by the sdl_renderer, node_editor and club examples so each module
 * only carries its own demo UI. The window runs headless (SDL dummy video
 * driver) when no display is available, which is what the CI runs use.
 */
/**
 * Host-owned frame loop: run [step] until it returns false, then call [done].
 * Browsers drive it from requestAnimationFrame; JVM/native pass null.
 */
typealias AppFrameDriver = (step: () -> Boolean, done: () -> Unit) -> Unit

object SdlRendererApp {

    /**
     * Runs the app until the window is closed or [frames] frames were rendered.
     *
     * [init] is called once, after the imgui context and font atlas are ready,
     * with the uploaded font texture id (needed by image demos). [draw] runs
     * every frame. [close] releases per-app resources before the imgui context
     * is destroyed.
     */
    fun run(
        title: String,
        frames: Int,
        init: (fontTextureId: ImTextureID) -> Unit,
        draw: (frame: Int) -> Unit,
        close: () -> Unit,
        fontSetup: (cn.enaium.imgui.ImFontAtlas, density: Float) -> Unit = { _, _ -> },
        /**
         * Host-owned frame loop. Browsers cannot block, so a wasm host drives
         * [step] from requestAnimationFrame and calls [done] when it returns
         * false. When null the loop spins in place (JVM, native).
         */
        driver: AppFrameDriver? = null,
    ) {
        SDL.setMainReady()
        // Fall back to the dummy video driver (headless CI runners, SSH
        // sessions) so SDL_Init itself never fails.
        if (!SDL.init(SDLInitFlags.VIDEO or SDLInitFlags.EVENTS)) {
            SDL.setHint("SDL_VIDEO_DRIVER", "dummy")
            if (SDL.init(SDLInitFlags.VIDEO or SDLInitFlags.EVENTS)) {
                println("video init fell back to the dummy driver — running headless")
            } else {
                error("SDL_Init failed: ${SDL.error()}")
            }
        }
        println("SDL ${SDL.version()} (${SDL.revision()})")
        println("Video driver: ${SDL.getCurrentVideoDriver()}")

        val window = SDL.createWindow(
            title = title,
            width = 1280,
            height = 800,
            flags = SDLWindowFlags.RESIZABLE or SDLWindowFlags.HIGH_PIXEL_DENSITY,
        )
        val renderer = SDL.createRenderer(window)
        val context = ImGui.createContext()
        val imgui = ImGuiSdlBackend(window)
        val backend = ImGuiSdlRendererBackend(renderer)
        var finished = false

        fun finish() {
            if (finished) return
            finished = true
            close()
            backend.close()
            ImGui.destroyContext(context)
            renderer.close()
            window.close()
            SDL.quit()
        }

        try {
            imgui.init()

            // On high-DPI displays (Retina, ...) the atlas is baked at
            // `sizePixels * rasterizerDensity` physical px while keeping
            // logical metrics at `sizePixels`, so text stays crisp when
            // the backend renders at the framebuffer scale.
            val fonts = ImGui.getIO().fonts
            val density = maxOf(imgui.framebufferScale.x, imgui.framebufferScale.y, 1f)
            // Font examples add their own faces (TTF with custom glyph
            // ranges) before the atlas is built.
            fontSetup(fonts, density)
            fonts.addFontDefault(
                ImFontConfig(
                    sizePixels = 13f * density,
                    rasterizerDensity = density,
                ),
            )
            check(fonts.build()) { "font atlas build failed" }
            val texData = fonts.getTexDataAsRGBA32()
            val fontTextureId = backend.uploadFontTexture(texData.pixels, texData.width, texData.height)
            fonts.setTexID(fontTextureId)

            init(fontTextureId)

            var running = true
            var frameCount = 0

            fun step(): Boolean {
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
                draw(frameCount)
                ImGui.render()
                frameCount++

                renderer.drawColor = SDLColor(18, 18, 24, 255)
                renderer.clear()
                backend.renderDrawData(ImGui.getDrawData())
                renderer.present()
                return running && frameCount < frames
            }

            if (driver != null) {
                // The host owns the loop (and calls done() at the end).
                driver(::step, ::finish)
            } else {
                while (step()) {
                }
                finish()
            }
        } catch (t: Throwable) {
            finish()
            throw t
        }
    }
}
