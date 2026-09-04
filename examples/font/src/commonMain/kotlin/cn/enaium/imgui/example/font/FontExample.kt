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

package cn.enaium.imgui.example.font

import cn.enaium.imgui.ImFont
import cn.enaium.imgui.ImFontConfig
import cn.enaium.imgui.ImFontGlyphRanges
import cn.enaium.imgui.ImFontGlyphRangesBuilder
import cn.enaium.imgui.ImGui
import cn.enaium.imgui.ImGuiCond
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.example.common.SdlRendererApp

/**
 * Demonstrates the imgui-style font API:
 *
 * - [ImFontGlyphRangesBuilder] accumulates codepoints (`addChar`,
 *   `addText`, `addRanges`) and produces the `{first, last}` range list
 *   for `ImFontConfig.glyphRanges`.
 * - `io.Fonts->AddFontFromFileTTF(path, size_pixels, &config)` is exposed
 *   as [cn.enaium.imgui.ImFontAtlas.addFontFromFileTTF] taking the config
 *   (with the glyph ranges) — no suffix-style overloads.
 *
 * The example loads a system CJK font with a compact glyph range set
 * covering the scripts of the sample strings, so the atlas stays small
 * while Latin, Cyrillic, Greek, Japanese kana and CJK ideographs all
 * render (no tofu / '?').
 *
 * Run with `./gradlew :examples:font:jvmRun` (JVM) or the per-target
 * native binaries; pass `--frames N` / `IMGUI_KMP_FRAMES=N` to exit after N
 * frames (headless CI runs).
 */
fun runFontExample(frames: Int = Int.MAX_VALUE) {
    var regular: ImFont? = null
    var large: ImFont? = null

    SdlRendererApp.run(
        title = "imgui-kmp font example",
        frames = frames,
        fontSetup = { fonts, density ->
            val fontPath = findSystemFont()
            if (fontPath != null) {
                val ranges = buildExampleGlyphRanges()
                fun _inRanges(cp: Int): Boolean {
                    var i = 0
                    while (i + 1 < ranges.size && ranges[i] != 0) {
                        if (cp in ranges[i]..ranges[i + 1]) return true
                        i += 2
                    }
                    return false
                }
                // Cap the rasterizer density: the japanese+korean presets add
                // ~32k glyphs; at Retina density 2 the atlas would balloon
                // past GPU texture limits and drop glyphs (renders '?').
                // 1.25x keeps text crisp while the atlas stays well inside
                // 4096x4096.
                val rd = minOf(density, 1.25f)
                regular = fonts.addFontFromFileTTF(
                    fontPath,
                    ImFontConfig(
                        sizePixels = 18f,
                        rasterizerDensity = rd,
                        glyphRanges = ranges,
                    ),
                )
                large = fonts.addFontFromFileTTF(
                    fontPath,
                    ImFontConfig(
                        sizePixels = 32f,
                        rasterizerDensity = rd,
                        glyphRanges = ranges,
                    ),
                )
            }
        },
        init = { },
        draw = { _ ->
            ImGui.setNextWindowPos(ImVec2(40f, 40f), ImGuiCond.ALWAYS)
            ImGui.setNextWindowSize(ImVec2(880f, 620f), ImGuiCond.ALWAYS)
            ImGui.begin(
                "Font example",
                flags = cn.enaium.imgui.ImGuiWindowFlags.NO_RESIZE or
                    cn.enaium.imgui.ImGuiWindowFlags.NO_COLLAPSE,
            )

            val font = regular
            if (font != null) ImGui.pushFont(font)
            ImGui.textWrapped(
                "Latin: The quick brown fox jumps over the lazy dog. 0123456789 " +
                    "A@B#C\u00a9D\u00e9E\u00fcF\u00df",
            )
            if (font != null) ImGui.popFont()

            ImGui.spacing()
            ImGui.text("Cyrillic/Greek: \u0417\u0434\u0440\u0430\u0432\u0441\u0442\u0432\u0443\u0439 \u03ba\u03b1\u03bb\u03b7\u03bc\u03ad\u03c1\u03b1")
            ImGui.text("CJK: \u4e2d\u6587\u65e5\u672c\u8a9e\u97d3\u56fd\u8a9e")

            val lf = large
            if (lf != null) ImGui.pushFont(lf)
            ImGui.text("\u4e2d\u6587\u65e5\u672c\u8a9e \u2014 large font")
            if (lf != null) ImGui.popFont()

            ImGui.spacing()
            ImGui.text("Sample strings cover the scripts below; only those codepoints")
            ImGui.text("were added to the atlas via ImFontGlyphRangesBuilder:")
            ImGui.bulletText("Basic Latin + Latin-1 (ImFontGlyphRanges.default)")
            ImGui.bulletText("Cyrillic + Greek (ImFontGlyphRanges.cyrillic)")
            ImGui.bulletText("Japanese kana + CJK (ImFontGlyphRanges.japanese)")
            ImGui.bulletText("Korean Hangul (ImFontGlyphRanges.korean)")
            ImGui.end()
        },
        close = { },
    )
}

/**
 * Finds a system font file that covers all scripts the example renders
 * (Latin, Cyrillic, Greek, Japanese kana, CJK ideographs and Korean
 * Hangul). The glyph ranges built by [ImFontGlyphRangesBuilder] are a
 * whitelist: ImGui only rasterizes codepoints from the ranges, but a
 * glyph the font file itself lacks still falls back to '?'. That is why
 * plain CJK fonts (PingFang/STHeiti/MSYH) are listed after fonts with
 * full Unicode coverage. Returns null when nothing matches (the example
 * then falls back to the built-in default font).
 */
internal fun findSystemFont(): String? {
    val candidates = when {
        isMac() -> listOf(
            // Full coverage incl. Korean Hangul.
            "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
            // CJK-only (no Hangul); listed last so the demo shows every
            // sample string instead of '?' for Hangul.
            "/System/Library/Fonts/PingFang.ttc",
            "/System/Library/Fonts/STHeiti Light.ttc",
            "/System/Library/Fonts/Hiragino Sans GB.ttc",
        )
        isWindows() -> listOf(
            // Malgun Gothic covers Hangul, YaHei covers CJK; neither covers
            // everything, so prefer a full Unicode font when installed.
            "C:\\Windows\\Fonts\\ArialUni.ttf",
            "C:\\Windows\\Fonts\\malgun.ttf",
            "C:\\Windows\\Fonts\\msyh.ttc",
            "C:\\Windows\\Fonts\\simhei.ttf",
        )
        else -> listOf(
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/noto-cjk/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/truetype/unfonts/UnBatang.ttf",
            "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
            "/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf",
        )
    }
    return candidates.firstOrNull { fileExists(it) }
}

/** Builds the compact glyph range list used by the example fonts. */
internal fun buildExampleGlyphRanges(): IntArray = ImFontGlyphRangesBuilder()
    .addText("The quick brown fox jumps over the lazy dog 0123456789")
    .addText("\u0417\u0434\u0440\u0430\u0432\u0441\u0442\u0432\u0443\u0439 \u03ba\u03b1\u03bb\u03b7\u03bc\u03ad\u03c1\u03b1")
    .addText("\u4e2d\u6587\u65e5\u672c\u8a9e\u97d3\u56fd\u8a9e")
    .addRanges(ImFontGlyphRanges.default)
    .addRanges(ImFontGlyphRanges.cyrillic)
    .addRanges(ImFontGlyphRanges.japanese)
    .addRanges(ImFontGlyphRanges.korean)
    .addChar('\n'.code)
    .addChar('\t'.code)
    .buildRanges()

internal expect fun isMac(): Boolean
internal expect fun isWindows(): Boolean
internal expect fun fileExists(path: String): Boolean
