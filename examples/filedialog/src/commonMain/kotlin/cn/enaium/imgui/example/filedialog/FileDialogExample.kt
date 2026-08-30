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

package cn.enaium.imgui.example.filedialog

import cn.enaium.imgui.ImGui
import cn.enaium.imgui.ImGuiCond
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import cn.enaium.imgui.example.common.SdlRendererApp
import cn.enaium.imgui.extensions.filedialog.FileDialog
import cn.enaium.imgui.extensions.filedialog.FileDialogConfig
import cn.enaium.imgui.extensions.filedialog.FileDialogInstance
import cn.enaium.imgui.extensions.filedialog.IgfdFlags
import cn.enaium.imgui.extensions.filedialog.IgfdResultMode

/**
 * The ImGuiFileDialog bindings, demonstrated together:
 *
 * - a single "open file" dialog with a source-image filter collection,
 * - a "save file" dialog (with default-extension handling),
 * - a directory picker,
 * - per-extension file styles (color + icon) via [FileDialog.setFileStyle],
 * - the selection reported by the open dialog.
 *
 * The dialog instance is created once up front and destroyed on close. Run
 * with `./gradlew :examples:filedialog:jvmRun` (JVM) or the per-target native
 * binaries; pass `--frames N` / `IMGUI_KMP_FRAMES=N` to exit after N frames
 * (headless CI runs). The dialogs are modal, so the headless runs simply draw
 * the demo window without interaction.
 */
fun runFileDialogExample(frames: Int = Int.MAX_VALUE) {
    var demo: FileDialogDemo? = null
    SdlRendererApp.run(
        title = "imgui-kmp filedialog example",
        frames = frames,
        init = { demo = FileDialogDemo() },
        draw = { frame -> demo?.draw(frame) },
        close = { demo?.close() },
    )
}

private class FileDialogDemo {

    // A single ImGuiFileDialog instance lives for the whole app; every dialog
    // key is registered on this instance.
    private val dialog: FileDialogInstance = FileDialog.create()

    // "All files" first so the dialog shows every file on open; the collection
    // filters then narrow it down like the upstream demo's filter strings.
    private val openFilter = "All files{.*},Source files (*.cpp *.h *.hpp){.cpp,.h,.hpp},Image files (*.png *.jpg){.png,.jpg}"
    private val saveFilter = "Text files (*.txt){.txt},All files{.*}"

    // Last reported selection
    private var lastSelection: List<Pair<String, String>> = emptyList()
    private var lastSavePath: String = ""

    init {
        // Style .cpp/.h source files green with a "{}" icon, images magenta.
        FileDialog.setFileStyle(dialog, IgfdFlags.NONE, ".cpp", ImVec4(0.2f, 0.9f, 0.3f, 1f), "{}")
        FileDialog.setFileStyle(dialog, IgfdFlags.NONE, ".h", ImVec4(0.2f, 0.9f, 0.3f, 1f), "{}")
        FileDialog.setFileStyle(dialog, IgfdFlags.NONE, ".png", ImVec4(0.9f, 0.2f, 0.8f, 1f), "img")
    }

    fun draw(frame: Int) {
        ImGui.setNextWindowPos(ImVec2(60f, 60f), ImGuiCond.FIRST_USE_EVER)
        ImGui.setNextWindowSize(ImVec2(520f, 380f), ImGuiCond.FIRST_USE_EVER)
        ImGui.begin("imgui-kmp filedialog example")

        ImGui.text("ImGuiFileDialog bindings")
        ImGui.separator()

        ImGui.text("Actions:")
        if (ImGui.button("Open file...")) {
            FileDialog.openDialog(
                dialog,
                key = "open",
                title = "Choose a file",
                filters = openFilter,
                config = FileDialogConfig(
                    path = ".",
                    fileName = "main.cpp",
                    countSelectionMax = 3,
                    // Open dialogs must not ask "overwrite?" even though an
                    // existing file matches the pre-filled name. CONFIRM_OVERWRITE
                    // is for the save flow below; keeping the other defaults.
                    flags = IgfdFlags.MODAL or IgfdFlags.HIDE_COLUMN_TYPE,
                ),
            )
        }
        if (ImGui.button("Save file...")) {
            FileDialog.openDialog(
                dialog,
                key = "save",
                title = "Save file",
                filters = saveFilter,
                config = FileDialogConfig(path = ".", fileName = "untitled.txt"),
            )
        }

        if (ImGui.button("Pick directory...")) {
            FileDialog.openDialog(
                dialog,
                key = "dir",
                title = "Choose a directory",
                filters = null, // directory mode
                config = FileDialogConfig(path = "."),
            )
        }

        ImGui.separator()
        ImGui.text("Open selection (last):")
        if (lastSelection.isEmpty()) {
            ImGui.textDisabled("(none)")
        } else {
            lastSelection.forEach { (name, path) ->
                ImGui.text("$name")
                ImGui.textDisabled("  -> $path")
            }
        }

        ImGui.text("Save path (last):")
        ImGui.textDisabled(if (lastSavePath.isEmpty()) "(none)" else lastSavePath)
        ImGui.text("frame: $frame")
        ImGui.end()

        // ---- open dialog ----
        if (FileDialog.displayDialog(dialog, key = "open", minSize = ImVec2(480f, 320f))) {
            if (FileDialog.isOk(dialog)) {
                lastSelection = FileDialog.getSelection(dialog, IgfdResultMode.KEEP_INPUT_FILE)
                FileDialog.closeDialog(dialog, "open")
            } else {
                FileDialog.closeDialog(dialog, "open")
            }
        }

        // ---- save dialog ----
        if (FileDialog.displayDialog(dialog, key = "save", minSize = ImVec2(480f, 320f))) {
            if (FileDialog.isOk(dialog)) {
                lastSavePath = FileDialog.getFilePathName(dialog)
                FileDialog.closeDialog(dialog, "save")
            } else {
                FileDialog.closeDialog(dialog, "save")
            }
        }

        // ---- directory dialog ----
        if (FileDialog.displayDialog(dialog, key = "dir", minSize = ImVec2(480f, 320f))) {
            if (FileDialog.isOk(dialog)) {
                lastSelection = FileDialog.getSelection(dialog, IgfdResultMode.KEEP_INPUT_FILE)
                FileDialog.closeDialog(dialog, "dir")
            } else {
                FileDialog.closeDialog(dialog, "dir")
            }
        }
    }

    fun close() {
        dialog.close()
    }
}