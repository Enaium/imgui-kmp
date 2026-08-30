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

package cn.enaium.imgui.example.colortextedit

import cn.enaium.imgui.ImGui
import cn.enaium.imgui.ImGuiCond
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.example.common.SdlRendererApp
import cn.enaium.imgui.extensions.colortextedit.ColorTextEdit
import cn.enaium.imgui.extensions.colortextedit.ColorTextEditEditor
import cn.enaium.imgui.extensions.colortextedit.TeColor
import cn.enaium.imgui.extensions.colortextedit.TeLanguage

/**
 * The ImGuiColorTextEdit bindings, demonstrated with a single editor showing
 * a C++ snippet:
 *
 * - language switch (C++ / C / Lua / Python / ... re-colors the text),
 * - dark / light palette toggle,
 * - a toolbar: undo/redo, cut/copy/paste, select all, line numbers,
 *   minimap, word wrap, read-only, whitespace, auto-indent,
 * - find/replace (next occurrence + replace current/all),
 * - a marker (line highlight), demonstrating [ColorTextEdit.addMarker],
 * - live status (line count, cursor position, undo/redo availability).
 *
 * The TextEditor instance is created once up front and destroyed on close.
 * Run with `./gradlew :examples:colortextedit:jvmRun` (JVM) or the per-target
 * native binaries; pass `--frames N` / `IMGUI_KMP_FRAMES=N` to exit after N
 * frames (headless CI runs).
 */
fun runColorTextEditExample(frames: Int = Int.MAX_VALUE) {
    var demo: ColorTextEditDemo? = null
    SdlRendererApp.run(
        title = "imgui-kmp colortextedit example",
        frames = frames,
        init = { demo = ColorTextEditDemo() },
        draw = { frame -> demo?.draw(frame) },
        close = { demo?.close() },
    )
}

private class ColorTextEditDemo {

    private val editor: ColorTextEditEditor = ColorTextEdit.create()

    // ---- toolbar state ----
    private val currentLanguage = IntArray(1) { TeLanguage.CPP }
    private val useDarkPalette = BooleanArray(1) { true }
    private val showLineNumbers = BooleanArray(1) { true }
    private val showMiniMap = BooleanArray(1) { false }
    private val wordWrap = BooleanArray(1) { false }
    private val readOnly = BooleanArray(1) { false }
    private val showWhitespaces = BooleanArray(1) { true }
    private val autoIndent = BooleanArray(1) { true }

    // ---- find / replace buffer ----
    private var findText = "class"
    private var replaceText = "struct"

    // ---- marker state (1-based slider, editor uses 0-based lines) ----
    private val markedLine = IntArray(1) { 4 }

    init {
        ColorTextEdit.setLanguage(editor, TeLanguage.CPP)
        ColorTextEdit.setDefaultDarkPalette(editor)
        ColorTextEdit.setTabSize(editor, 4)
        ColorTextEdit.setShowLineNumbersEnabled(editor, true)
        ColorTextEdit.setText(
            editor,
            """
            |// A small C++ snippet rendered by ImGuiColorTextEdit.
            |#include <iostream>
            |#include <vector>
            |
            |template <typename T>
            |T sum(const std::vector<T>& values) {
            |    T total{};
            |    for (const auto& v : values) {
            |        total += v;
            |    }
            |    return total;
            |}
            |
            |int main() {
            |    std::vector<int> data = {1, 2, 3, 4, 5};
            |    std::cout << "sum = " << sum(data) << '\n';
            |    return 0;
            |}
            |""".trimMargin(),
        )
    }

    fun draw(frame: Int) {
        ImGui.setNextWindowPos(ImVec2(60f, 40f), ImGuiCond.FIRST_USE_EVER)
        ImGui.setNextWindowSize(ImVec2(880f, 660f), ImGuiCond.FIRST_USE_EVER)
        ImGui.begin("imgui-kmp colortextedit example")

        // ==================== Toolbar ====================
        ImGui.text("Language")
        ImGui.sameLine()
        if (ImGui.combo("##Language", currentLanguage, LANGUAGES)) {
            ColorTextEdit.setLanguage(editor, LANGUAGE_IDS[currentLanguage[0]])
        }
        ImGui.sameLine()
        if (ImGui.checkbox("Dark palette", useDarkPalette)) {
            if (useDarkPalette[0]) ColorTextEdit.setDefaultDarkPalette(editor)
            else ColorTextEdit.setDefaultLightPalette(editor)
        }

        ImGui.separatorText("Edit")
        if (ImGui.button("Undo")) ColorTextEdit.undo(editor)
        ImGui.sameLine()
        if (ImGui.button("Redo")) ColorTextEdit.redo(editor)
        ImGui.sameLine()
        if (ImGui.button("Cut")) ColorTextEdit.cut(editor)
        ImGui.sameLine()
        if (ImGui.button("Copy")) ColorTextEdit.copy(editor)
        ImGui.sameLine()
        if (ImGui.button("Paste")) ColorTextEdit.paste(editor)
        ImGui.sameLine()
        if (ImGui.button("Select all")) ColorTextEdit.selectAll(editor)

        ImGui.separatorText("View")
        if (ImGui.checkbox("Line numbers", showLineNumbers)) {
            ColorTextEdit.setShowLineNumbersEnabled(editor, showLineNumbers[0])
        }
        ImGui.sameLine()
        if (ImGui.checkbox("Mini-map", showMiniMap)) {
            ColorTextEdit.setShowMiniMapEnabled(editor, showMiniMap[0])
        }
        ImGui.sameLine()
        if (ImGui.checkbox("Word wrap", wordWrap)) {
            ColorTextEdit.setWordWrapEnabled(editor, wordWrap[0])
        }
        ImGui.sameLine()
        if (ImGui.checkbox("Read-only", readOnly)) {
            ColorTextEdit.setReadOnlyEnabled(editor, readOnly[0])
        }
        ImGui.sameLine()
        if (ImGui.checkbox("Whitespaces", showWhitespaces)) {
            ColorTextEdit.setShowWhitespacesEnabled(editor, showWhitespaces[0])
        }
        ImGui.sameLine()
        if (ImGui.checkbox("Auto-indent", autoIndent)) {
            ColorTextEdit.setAutoIndentEnabled(editor, autoIndent[0])
        }

        ImGui.separatorText("Find / replace")
        ImGui.text("Find")
        ImGui.sameLine()
        findText = ImGui.inputText("##Find", findText) ?: findText
        ImGui.sameLine()
        if (ImGui.button("Next")) {
            ColorTextEdit.selectNextOccurrenceOf(editor, findText, caseSensitive = false)
        }
        ImGui.text("Replace")
        ImGui.sameLine()
        replaceText = ImGui.inputText("##Replace", replaceText) ?: replaceText
        ImGui.sameLine()
        if (ImGui.button("Replace")) {
            ColorTextEdit.selectNextOccurrenceOf(editor, findText, caseSensitive = false)
            if (ColorTextEdit.anyCursorHasSelection(editor)) {
                ColorTextEdit.replaceTextInCurrentCursor(editor, replaceText)
            }
        }
        ImGui.sameLine()
        if (ImGui.button("Replace all")) {
            ColorTextEdit.selectFirstOccurrenceOf(editor, findText, caseSensitive = false)
            ColorTextEdit.replaceTextInAllCursors(editor, replaceText)
        }

        ImGui.separatorText("Markers")
        ImGui.sliderInt("Marked line", markedLine, 1, 8, "%d")
        ImGui.sameLine()
        if (ImGui.button("Toggle marker")) {
            val line = (markedLine[0] - 1).toLong()
            if (ColorTextEdit.hasMarkers(editor)) ColorTextEdit.clearMarkers(editor) else {
                ColorTextEdit.addMarker(
                    editor,
                    line = line,
                    lineNumberColor = 0xFF7A7A7A.toInt(),
                    textColor = 0xFFFF5555.toInt(),
                    lineNumberTooltip = "breakpoint",
                    textTooltip = "line ${line + 1}",
                )
            }
        }

        // ==================== Editor ====================
        ColorTextEdit.render(editor, "##editor", ImVec2(-1f, -1f))

        // ==================== Status ====================
        ImGui.separator()
        val (line, index) = ColorTextEdit.getCursorPos(editor)
        val selection = ColorTextEdit.getSelectedText(editor)
        ImGui.text("lines: ${ColorTextEdit.getLineCount(editor)}  cursors: ${ColorTextEdit.getNumberOfCursors(editor)}")
        ImGui.text("cursor: $line:$index   selected: ${selection?.length ?: 0} chars")
        ImGui.text("language: ${ColorTextEdit.getLanguageName(editor)}")
        ImGui.text("undo: ${ColorTextEdit.canUndo(editor)}   redo: ${ColorTextEdit.canRedo(editor)}")
        ImGui.text("frame: $frame")
        ImGui.end()
    }

    fun close() {
        editor.close()
    }

    private companion object {
        val LANGUAGES = arrayOf("C++", "C", "C#", "Lua", "Python", "GLSL", "HLSL", "JSON", "Markdown", "SQL", "AngelScript")
        val LANGUAGE_IDS = intArrayOf(
            TeLanguage.CPP,
            TeLanguage.C,
            TeLanguage.CS,
            TeLanguage.LUA,
            TeLanguage.PYTHON,
            TeLanguage.GLSL,
            TeLanguage.HLSL,
            TeLanguage.JSON,
            TeLanguage.MARKDOWN,
            TeLanguage.SQL,
            TeLanguage.ANGEL_SCRIPT,
        )
    }
}