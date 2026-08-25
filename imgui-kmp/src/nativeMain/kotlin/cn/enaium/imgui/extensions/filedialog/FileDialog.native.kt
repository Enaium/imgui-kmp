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

@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlin.experimental.ExperimentalNativeApi::class,
)

package cn.enaium.imgui.extensions.filedialog

import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import imgui.*
import kotlinx.cinterop.*
import platform.posix.free
// =========================================================================
// Native (cinterop) actual implementations
// =========================================================================

/** Copies a malloc'ed C string into a Kotlin [String] and releases it. */
private fun takeAndFreeCString(ptr: CPointer<ByteVar>?): String {
    if (ptr == null) return ""
    val s = ptr.toKString()
    free(ptr)
    return s
}

/** Converts a Kotlin [ImVec2] to an `imgui_vec2` by-value struct for cinterop calls. */
private inline fun <T> withVec2(value: ImVec2, block: (CValue<imgui_vec2>) -> T): T = memScoped {
    val v = alloc<imgui_vec2>()
    v.x = value.x
    v.y = value.y
    block(v.readValue())
}

actual class FileDialogInstance internal constructor(internal val ptr: CPointer<igfd_dialog>?) : AutoCloseable {
    actual override fun close() {
        igfd_destroy(ptr)
    }
}

actual object FileDialog {
    actual fun create(): FileDialogInstance {
        val ptr = igfd_create()
            ?: error("igfd_create returned null")
        return FileDialogInstance(ptr)
    }

    actual fun destroy(dialog: FileDialogInstance?) {
        if (dialog != null) {
            igfd_destroy(dialog.ptr)
        } else {
            igfd_destroy(null)
        }
    }

    actual fun openDialog(
        dialog: FileDialogInstance,
        key: String,
        title: String,
        filters: String?,
        config: FileDialogConfig,
    ) = igfd_open_dialog(
        dialog.ptr,
        key,
        title,
        filters,
        config.path,
        config.fileName,
        config.filePathName,
        config.countSelectionMax,
        config.flags,
    )

    actual fun displayDialog(
        dialog: FileDialogInstance,
        key: String,
        windowFlags: Int,
        minSize: ImVec2,
        maxSize: ImVec2,
    ): Boolean = withVec2(minSize) { min ->
        withVec2(maxSize) { max ->
            igfd_display_dialog(dialog.ptr, key, windowFlags, min, max)
        }
    }

    actual fun closeDialog(dialog: FileDialogInstance, key: String) = igfd_close_dialog(dialog.ptr, key)

    actual fun isOk(dialog: FileDialogInstance): Boolean = igfd_is_ok(dialog.ptr)

    actual fun wasKeyOpenedThisFrame(dialog: FileDialogInstance, key: String): Boolean =
        igfd_was_key_opened_this_frame(dialog.ptr, key)

    actual fun wasOpenedThisFrame(dialog: FileDialogInstance): Boolean = igfd_was_opened_this_frame(dialog.ptr)

    actual fun isKeyOpened(dialog: FileDialogInstance, key: String): Boolean =
        igfd_is_key_opened(dialog.ptr, key)

    actual fun isOpened(dialog: FileDialogInstance): Boolean = igfd_is_opened(dialog.ptr)

    actual fun getSelection(dialog: FileDialogInstance, mode: Int): List<Pair<String, String>> = memScoped {
        val countVar = alloc<IntVar>()
        val table = igfd_get_selection(dialog.ptr, mode, countVar.ptr)
            ?: return@memScoped emptyList()
        val count = countVar.value
        try {
            val result = ArrayList<Pair<String, String>>(count)
            for (i in 0 until count) {
                val fileName = table[i]?.toKString() ?: ""
                val filePathName = table[i + 1]?.toKString() ?: ""
                result.add(fileName to filePathName)
            }
            result
        } finally {
            igfd_selection_free(table, countVar.value)
        }
    }

    actual fun getFilePathName(dialog: FileDialogInstance, mode: Int): String =
        takeAndFreeCString(igfd_get_file_path_name(dialog.ptr, mode))

    actual fun getCurrentFileName(dialog: FileDialogInstance, mode: Int): String =
        takeAndFreeCString(igfd_get_current_file_name(dialog.ptr, mode))

    actual fun getCurrentPath(dialog: FileDialogInstance): String =
        takeAndFreeCString(igfd_get_current_path(dialog.ptr))

    actual fun getCurrentFilter(dialog: FileDialogInstance): String =
        takeAndFreeCString(igfd_get_current_filter(dialog.ptr))

    actual fun setFileStyle(dialog: FileDialogInstance, flags: Int, filter: String?, color: ImVec4, icon: String?) =
        igfd_set_file_style(
            dialog.ptr,
            flags.toUInt(),
            filter,
            color.x,
            color.y,
            color.z,
            color.w,
            icon,
        )

    actual fun getFileStyle(dialog: FileDialogInstance, flags: Int, filter: String?, outColor: FloatArray): String? = memScoped {
        val colorArr = allocArray<FloatVar>(4)
        val iconPtr = alloc<CPointerVar<ByteVar>>()
        val found = igfd_get_file_style(
            dialog.ptr,
            flags.toUInt(),
            filter,
            colorArr,
            iconPtr.ptr,
        )
        if (!found) return@memScoped null
        for (i in 0 until minOf(4, outColor.size)) {
            outColor[i] = colorArr[i]
        }
        val icon = iconPtr.value
        if (icon != null) {
            val s = icon.toKString()
            free(icon)
            s
        } else {
            null
        }
    }

    actual fun clearFilesStyle(dialog: FileDialogInstance) = igfd_clear_files_style(dialog.ptr)
}
