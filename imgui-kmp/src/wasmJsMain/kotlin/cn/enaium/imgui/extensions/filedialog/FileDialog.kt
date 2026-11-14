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

actual class FileDialogInstance(): AutoCloseable {
    actual override fun close() {
        throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    }
}

actual object FileDialog {
    actual fun create(): FileDialogInstance = throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    actual fun destroy(dialog: FileDialogInstance?) {
        throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    }
    actual fun openDialog(dialog: FileDialogInstance, key: String, title: String, filters: String?, config: FileDialogConfig) {
        throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    }
    actual fun displayDialog(dialog: FileDialogInstance, key: String, windowFlags: Int, minSize: ImVec2, maxSize: ImVec2): Boolean = throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    actual fun closeDialog(dialog: FileDialogInstance, key: String) {
        throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    }
    actual fun isOk(dialog: FileDialogInstance): Boolean = throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    actual fun wasKeyOpenedThisFrame(dialog: FileDialogInstance, key: String): Boolean = throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    actual fun wasOpenedThisFrame(dialog: FileDialogInstance): Boolean = throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    actual fun isKeyOpened(dialog: FileDialogInstance, key: String): Boolean = throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    actual fun isOpened(dialog: FileDialogInstance): Boolean = throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    actual fun getSelection(dialog: FileDialogInstance, mode: Int): List<Pair<String, String>> = throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    actual fun getFilePathName(dialog: FileDialogInstance, mode: Int): String = throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    actual fun getCurrentFileName(dialog: FileDialogInstance, mode: Int): String = throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    actual fun getCurrentPath(dialog: FileDialogInstance): String = throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    actual fun getCurrentFilter(dialog: FileDialogInstance): String = throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    actual fun setFileStyle(dialog: FileDialogInstance, flags: Int, filter: String?, color: ImVec4, icon: String?) {
        throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    }
    actual fun getFileStyle(dialog: FileDialogInstance, flags: Int, filter: String?, outColor: FloatArray): String? = throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    actual fun clearFilesStyle(dialog: FileDialogInstance) {
        throw UnsupportedOperationException("FileDialog is not available on the wasm target")
    }
}
