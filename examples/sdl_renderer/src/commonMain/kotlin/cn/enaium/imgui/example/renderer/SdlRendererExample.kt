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

package cn.enaium.imgui.example.renderer

import cn.enaium.imgui.ImGui
import cn.enaium.imgui.backends.sdl.ImGuiSdlBackend
import cn.enaium.imgui.backends.sdl.ImGuiSdlRendererBackend
import cn.enaium.imgui.example.common.DemoUi
import cn.enaium.sdl.SDL
import cn.enaium.sdl.SDLColor
import cn.enaium.sdl.SDLInitFlags
import cn.enaium.sdl.SDLWindowFlags

/**
 * Dear ImGui + ImPlot rendered through the SDL3 2D renderer.
 *
 * Run with `./gradlew :examples:run` (JVM) or the per-target native binaries.
 * Pass `--frames N` to exit after N frames (useful for headless CI runs).
 */
fun runSdlRendererExample(frames: Int = Int.MAX_VALUE) {
    SDL.setMainReady()
    // Fall back to the dummy video driver (headless CI runners, SSH
    // sessions) like sdl-kmp's own examples do.
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

    SDL.createWindow(
        title = "imgui-kmp renderer example",
        width = 1280,
        height = 800,
        flags = SDLWindowFlags.RESIZABLE,
    ).use { window ->
        SDL.createRenderer(window).use { renderer ->
            val context = ImGui.createContext()
            try {
                val imgui = ImGuiSdlBackend(window)
                val backend = ImGuiSdlRendererBackend(renderer)
                imgui.init()

                // Build the font atlas, then create the renderer texture from
                // the resulting pixels and hand the texture id to imgui.
                val fonts = ImGui.getIO().fonts
                fonts.addFontDefault()
                check(fonts.build()) { "font atlas build failed" }
                val texData = fonts.getTexDataAsRGBA32()
                val fontTextureId = backend.uploadFontTexture(texData.pixels, texData.width, texData.height)
                fonts.setTexID(fontTextureId)

                val plotContext = cn.enaium.imgui.extensions.implot.ImPlot.createContext()
                cn.enaium.imgui.extensions.implot.ImPlot.setImGuiContext(context)
                val demoUi = DemoUi().apply {
                    this.fontTextureId = fontTextureId
                }

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

                    imgui.newFrame()
                    demoUi.draw(frameCount)
                    ImGui.render()

                    renderer.drawColor = SDLColor(18, 18, 24, 255)
                    renderer.clear()
                    backend.renderDrawData(ImGui.getDrawData())
                    renderer.present()
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
