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

package cn.enaium.imgui.extensions.filedialog

import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4

/**
 * Bit flags for [FileDialog.openDialog], mirroring ImGuiFileDialogFlags_ from ImGuiFileDialog.
 */
object IgfdFlags {
    const val NONE = 0
    const val CONFIRM_OVERWRITE = 1 shl 0
    const val DONT_SHOW_HIDDEN_FILES = 1 shl 1
    const val DISABLE_CREATE_DIRECTORY_BUTTON = 1 shl 2
    const val HIDE_COLUMN_TYPE = 1 shl 3
    const val HIDE_COLUMN_SIZE = 1 shl 4
    const val HIDE_COLUMN_DATE = 1 shl 5
    const val NO_DIALOG = 1 shl 6
    const val READ_ONLY_FILE_NAME_FIELD = 1 shl 7
    const val CASE_INSENSITIVE_EXTENTION_FILTERING = 1 shl 8
    const val MODAL = 1 shl 9
    const val DISABLE_THUMBNAIL_MODE = 1 shl 10
    const val DISABLE_PLACE_MODE = 1 shl 11
    const val DISABLE_QUICK_PATH_SELECTION = 1 shl 12
    const val SHOW_DEVICES_BUTTON = 1 shl 13
    const val NATURAL_SORTING = 1 shl 14
    const val OPTIONAL_FILE_NAME = 1 shl 15

    /** Default behavior when no flags are defined. */
    const val DEFAULT = CONFIRM_OVERWRITE or MODAL or HIDE_COLUMN_TYPE
}

/**
 * Result mode for [FileDialog.getSelection] / [FileDialog.getFilePathName] /
 * [FileDialog.getCurrentFileName], mirroring IGFD_ResultMode_ from ImGuiFileDialog.
 */
object IgfdResultMode {
    /** Add the file ext only if there is no file ext (default). */
    const val ADD_IF_NO_FILE_EXT = 0

    /** Overwrite the file extention by the current filter (behavior pre IGFD v0.6.6). */
    const val OVERWRITE_FILE_EXT = 1

    /** Keep the input file => no modification. */
    const val KEEP_INPUT_FILE = 2
}

/**
 * Open-dialog configuration, mirroring IGFD_FileDialog_Config (scalar fields only).
 */
data class FileDialogConfig(
    val path: String = ".",
    val fileName: String = "",
    val filePathName: String = "",
    val countSelectionMax: Int = 1,
    val flags: Int = IgfdFlags.DEFAULT,
)

/** A file dialog context; close() calls [FileDialog.destroy]. */
expect class FileDialogInstance : AutoCloseable {
    override fun close()
}

/**
 * Kotlin bindings for ImGuiFileDialog (aiekick), inside the cn.enaium.imgui.extensions.filedialog package.
 *
 * Filter syntax ([filters] parameter):
 * - simple filters: `"*.cpp,*.h,*.hpp"` (comma separated, null for directory mode)
 * - filter collections: `"Source files (*.cpp *.h){.cpp,.h},Image files (*.png){.png}"`
 */
expect object FileDialog {
    fun create(): FileDialogInstance
    fun destroy(dialog: FileDialogInstance? = null)

    /** Opens the dialog registered under [key]; [filters] = null for directory mode. */
    fun openDialog(
        dialog: FileDialogInstance,
        key: String,
        title: String,
        filters: String? = null,
        config: FileDialogConfig = FileDialogConfig(),
    )

    /** Displays the dialog under [key]; returns true while the dialog is open. */
    fun displayDialog(
        dialog: FileDialogInstance,
        key: String,
        windowFlags: Int = 0,
        minSize: ImVec2 = ImVec2(0f, 0f),
        maxSize: ImVec2 = ImVec2(Float.MAX_VALUE, Float.MAX_VALUE),
    ): Boolean

    fun closeDialog(dialog: FileDialogInstance, key: String)

    /** True => dialog closed with Ok result; false => closed with cancel. */
    fun isOk(dialog: FileDialogInstance): Boolean

    /** Say if the dialog [key] was already opened this frame. */
    fun wasKeyOpenedThisFrame(dialog: FileDialogInstance, key: String): Boolean

    /** Say if the dialog was already opened this frame. */
    fun wasOpenedThisFrame(dialog: FileDialogInstance): Boolean

    /** Say if the dialog [key] is opened. */
    fun isKeyOpened(dialog: FileDialogInstance, key: String): Boolean

    /** Say if the dialog is opened somewhere. */
    fun isOpened(dialog: FileDialogInstance): Boolean

    /**
     * Open File behavior: returns the selection as (fileName, filePathName) pairs.
     * [mode] is one of [IgfdResultMode].
     */
    fun getSelection(dialog: FileDialogInstance, mode: Int = IgfdResultMode.KEEP_INPUT_FILE): List<Pair<String, String>>

    /**
     * Save File behavior: content of the filename field with current filter extention and current path.
     * [mode] is one of [IgfdResultMode].
     */
    fun getFilePathName(dialog: FileDialogInstance, mode: Int = IgfdResultMode.ADD_IF_NO_FILE_EXT): String

    /** Save File behavior: content of the filename field with current filter extention. */
    fun getCurrentFileName(dialog: FileDialogInstance, mode: Int = IgfdResultMode.ADD_IF_NO_FILE_EXT): String

    fun getCurrentPath(dialog: FileDialogInstance): String
    fun getCurrentFilter(dialog: FileDialogInstance): String

    /** Custom display (color + optional icon text) for files matching [filter] under [flags]. */
    fun setFileStyle(dialog: FileDialogInstance, flags: Int, filter: String?, color: ImVec4, icon: String? = null)

    /** Retrieves the style color into [outColor] (>= 4 floats); returns the icon text or null. */
    fun getFileStyle(dialog: FileDialogInstance, flags: Int, filter: String?, outColor: FloatArray): String?

    fun clearFilesStyle(dialog: FileDialogInstance)
}
