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

// JNI bridge
// =========================================================================

internal object Jni {
    init {
        cn.enaium.imgui.NativeLoader.load()
    }

    external fun create(): Long
    external fun destroy(ptr: Long)
    external fun openDialog(ptr: Long, key: String, title: String, filters: String?, path: String, fileName: String, filePathName: String, countSelectionMax: Int, flags: Int)
    external fun displayDialog(ptr: Long, key: String, windowFlags: Int, minSizeX: Float, minSizeY: Float, maxSizeX: Float, maxSizeY: Float): Boolean
    external fun closeDialog(ptr: Long, key: String)
    external fun isOk(ptr: Long): Boolean
    external fun wasKeyOpenedThisFrame(ptr: Long, key: String): Boolean
    external fun wasOpenedThisFrame(ptr: Long): Boolean
    external fun isKeyOpened(ptr: Long, key: String): Boolean
    external fun isOpened(ptr: Long): Boolean

    /** Flat array [name0, path0, name1, path1, ...]; null when the selection is empty. */
    external fun getSelection(ptr: Long, mode: Int): Array<String>?
    external fun getFilePathName(ptr: Long, mode: Int): String?
    external fun getCurrentFileName(ptr: Long, mode: Int): String?
    external fun getCurrentPath(ptr: Long): String?
    external fun getCurrentFilter(ptr: Long): String?
    external fun setFileStyle(ptr: Long, flags: Int, filter: String?, r: Float, g: Float, b: Float, a: Float, icon: String?)
    external fun getFileStyle(ptr: Long, flags: Int, filter: String?, outColor: FloatArray): String?
    external fun clearFilesStyle(ptr: Long)
}

// =========================================================================
// JVM/Android actual implementations
// =========================================================================

actual class FileDialogInstance internal constructor(internal val ptr: Long) : AutoCloseable {
    actual override fun close() {
        Jni.destroy(ptr)
    }
}

actual object FileDialog {
    actual fun create(): FileDialogInstance = FileDialogInstance(Jni.create())

    actual fun destroy(dialog: FileDialogInstance?) {
        if (dialog != null) {
            dialog.close()
        } else {
            Jni.destroy(0L)
        }
    }

    actual fun openDialog(
        dialog: FileDialogInstance,
        key: String,
        title: String,
        filters: String?,
        config: FileDialogConfig,
    ) {
        Jni.openDialog(
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
    }

    actual fun displayDialog(
        dialog: FileDialogInstance,
        key: String,
        windowFlags: Int,
        minSize: ImVec2,
        maxSize: ImVec2,
    ): Boolean = Jni.displayDialog(dialog.ptr, key, windowFlags, minSize.x, minSize.y, maxSize.x, maxSize.y)

    actual fun closeDialog(dialog: FileDialogInstance, key: String) = Jni.closeDialog(dialog.ptr, key)

    actual fun isOk(dialog: FileDialogInstance): Boolean = Jni.isOk(dialog.ptr)

    actual fun wasKeyOpenedThisFrame(dialog: FileDialogInstance, key: String): Boolean =
        Jni.wasKeyOpenedThisFrame(dialog.ptr, key)

    actual fun wasOpenedThisFrame(dialog: FileDialogInstance): Boolean = Jni.wasOpenedThisFrame(dialog.ptr)

    actual fun isKeyOpened(dialog: FileDialogInstance, key: String): Boolean =
        Jni.isKeyOpened(dialog.ptr, key)

    actual fun isOpened(dialog: FileDialogInstance): Boolean = Jni.isOpened(dialog.ptr)

    actual fun getSelection(dialog: FileDialogInstance, mode: Int): List<Pair<String, String>> {
        val flat = Jni.getSelection(dialog.ptr, mode) ?: return emptyList()
        val result = ArrayList<Pair<String, String>>(flat.size / 2)
        var i = 0
        while (i + 1 < flat.size) {
            result.add(flat[i] to flat[i + 1])
            i += 2
        }
        return result
    }

    actual fun getFilePathName(dialog: FileDialogInstance, mode: Int): String =
        Jni.getFilePathName(dialog.ptr, mode) ?: ""

    actual fun getCurrentFileName(dialog: FileDialogInstance, mode: Int): String =
        Jni.getCurrentFileName(dialog.ptr, mode) ?: ""

    actual fun getCurrentPath(dialog: FileDialogInstance): String =
        Jni.getCurrentPath(dialog.ptr) ?: ""

    actual fun getCurrentFilter(dialog: FileDialogInstance): String =
        Jni.getCurrentFilter(dialog.ptr) ?: ""

    actual fun setFileStyle(dialog: FileDialogInstance, flags: Int, filter: String?, color: ImVec4, icon: String?) {
        Jni.setFileStyle(dialog.ptr, flags, filter, color.x, color.y, color.z, color.w, icon)
    }

    actual fun getFileStyle(dialog: FileDialogInstance, flags: Int, filter: String?, outColor: FloatArray): String? =
        Jni.getFileStyle(dialog.ptr, flags, filter, outColor)

    actual fun clearFilesStyle(dialog: FileDialogInstance) = Jni.clearFilesStyle(dialog.ptr)
}
