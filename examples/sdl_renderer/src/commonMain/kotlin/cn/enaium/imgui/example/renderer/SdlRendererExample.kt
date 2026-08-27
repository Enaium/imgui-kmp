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
import cn.enaium.imgui.example.common.DemoUi
import cn.enaium.imgui.example.common.SdlRendererApp
import cn.enaium.imgui.extensions.implot.ImPlot
import cn.enaium.imgui.extensions.implot.ImPlotContext

/**
 * Dear ImGui + ImPlot rendered through the SDL3 2D renderer.
 *
 * Run with `./gradlew :examples:sdl_renderer:jvmRun` (JVM) or the per-target
 * native binaries. Pass `--frames N` to exit after N frames (useful for
 * headless CI runs). The SDL bootstrap lives in [SdlRendererApp].
 */
fun runSdlRendererExample(frames: Int = Int.MAX_VALUE) {
    var demoUi: DemoUi? = null
    var plotContext: ImPlotContext? = null
    SdlRendererApp.run(
        title = "imgui-kmp renderer example",
        frames = frames,
        init = { fontTextureId ->
            demoUi = DemoUi().apply {
                this.fontTextureId = fontTextureId
            }
            plotContext = ImPlot.createContext()
            ImPlot.setImGuiContext(ImGui.getCurrentContext() ?: error("no imgui context"))
        },
        draw = { frame -> demoUi?.draw(frame) },
        close = {
            demoUi?.close()
            plotContext?.let { ImPlot.destroyContext(it) }
        },
    )
}
