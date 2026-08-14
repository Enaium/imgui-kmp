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

import cn.enaium.imgui.ImGui
import cn.enaium.imgui.backends.sdl.ImGuiSdlBackend
import cn.enaium.imgui.backends.sdl.ImGuiSdlGpuBackend
import cn.enaium.imgui.example.common.DemoUi
import cn.enaium.sdl.SDL
import cn.enaium.sdl.SDLGPU
import cn.enaium.sdl.SDLInitFlags
import cn.enaium.sdl.SDLWindowFlags

/**
 * Dear ImGui + ImPlot rendered through the SDL3 GPU API.
 *
 * Run with `./gradlew :examples:sdl_gpu:run` (JVM) or the per-target native binaries.
 * Pass `--frames N` to exit after N frames (useful for headless CI runs).
 */
fun runSdlGpuExample(frames: Int = Int.MAX_VALUE) {
    SDL.setMainReady()
    // Fall back to the dummy video driver (headless CI runners, SSH
    // sessions) like the renderer example does, so SDL_Init itself never
    // fails. SDL_GPU still needs a real device though: creating one will
    // fail below with a clear message in that case.
    if (!SDL.init(SDLInitFlags.VIDEO or SDLInitFlags.EVENTS)) {
        SDL.setHint("SDL_VIDEO_DRIVER", "dummy")
        if (SDL.init(SDLInitFlags.VIDEO or SDLInitFlags.EVENTS)) {
            println("video init fell back to the dummy driver — running headless")
            println(
                "WARNING: no video device is available in this session (headless/SSH). " +
                    "SDL_GPU cannot create a device without one, so this example will fail below; " +
                    "run it from a desktop session instead.",
            )
        } else {
            error("SDL_Init failed: ${SDL.error()}")
        }
    }
    println("SDL ${SDL.version()} (${SDL.revision()})")
    println("Video driver: ${SDL.getCurrentVideoDriver()}")
    println("GPU drivers: ${SDLGPU.drivers}")

    val device = SDLGPU.createDevice()
        ?: error(
            "SDL_CreateGPUDevice failed: ${SDL.error()}. " +
                "The GPU example requires a real GPU and a video device; " +
                "run it from a desktop session (not headless/SSH).",
        )
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

