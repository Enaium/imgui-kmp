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

package cn.enaium.imgui.example.markdown

import cn.enaium.imgui.ImGui
import cn.enaium.imgui.ImGuiCond
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.example.common.SdlRendererApp
import cn.enaium.imgui.extensions.markdown.Markdown
import cn.enaium.imgui.extensions.markdown.MarkdownConfigHandle
import cn.enaium.imgui.extensions.markdown.MarkdownLinkData
import cn.enaium.imgui.extensions.markdown.MdFormatFlags

/**
 * The imgui_markdown bindings, demonstrated by rendering a README-style
 * document into an ImGui window:
 *
 * - headings H1-H3 with separators,
 * - lists, blockquotes, inline code and fenced code blocks,
 * - a clickable link (a tooltip shows the target),
 * - bold/emphasis,
 * - a trailing "markdown version" line.
 *
 * The Markdown config is created once up front and destroyed on close.
 * Run with `./gradlew :examples:markdown:jvmRun` (JVM) or the per-target
 * native binaries; pass `--frames N` / `IMGUI_KMP_FRAMES=N` to exit after N
 * frames (headless CI runs).
 */
fun runMarkdownExample(frames: Int = Int.MAX_VALUE) {
    var demo: MarkdownDemo? = null
    SdlRendererApp.run(
        title = "imgui-kmp markdown example",
        frames = frames,
        init = { demo = MarkdownDemo() },
        draw = { frame -> demo?.draw(frame) },
        close = { demo?.close() },
    )
}

private class MarkdownDemo {

    private val config: MarkdownConfigHandle = Markdown.create()

    init {
        Markdown.setLinkIcon(config, "\uD83D\uDD17") // 🔗
        Markdown.setFormatFlags(config, MdFormatFlags.COMMON_MARK_ALL)
    }

    fun draw(frame: Int) {
        ImGui.setNextWindowPos(ImVec2(60f, 40f), ImGuiCond.FIRST_USE_EVER)
        ImGui.setNextWindowSize(ImVec2(760f, 700f), ImGuiCond.FIRST_USE_EVER)
        ImGui.begin("imgui-kmp markdown example")

        Markdown.render(
            config,
            """
            |# imgui-kmp
            |
            |**Kotlin Multiplatform** bindings for [Dear ImGui](https://github.com/ocornut/imgui)
            |and the ecosystem around it.
            |
            |## Features
            |
            |- ImGui core windowing, widgets and input
            |- [ImPlot](https://github.com/epezent/implot) plotting
            |- [ImPlot3D](https://github.com/brenocq/implot3d) 3D plotting
            |- SDL3 renderer / GPU backends
            |- *Markdown rendering* via imgui_markdown
            |
            |## Code
            |
            |```kotlin
            |fun main() {
            |    println("Hello imgui-kmp")
            |}
            |```
            |
            |> Markdown is rendered directly into an ImGui window, with headings,
            |> lists, inline code and clickable links.
            |
            |### Links
            |
            |Click the link below — a tooltip shows the target URL.
            |
            |[Read the docs](https://github.com/Enaium/imgui-kmp)
            |
            |---
            |
            |frame: $frame
            |""".trimMargin(),
        )

        ImGui.separator()
        ImGui.text("Markdown bound through imgui_markdown (enkisoftware)")
        ImGui.end()
    }

    fun close() {
        config.close()
    }
}