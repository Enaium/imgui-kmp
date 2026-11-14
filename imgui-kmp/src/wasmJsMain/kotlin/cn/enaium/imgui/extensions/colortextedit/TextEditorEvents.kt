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


package cn.enaium.imgui.extensions.colortextedit


actual object TextEditorEvents {
    actual fun setTransactionCallback(editor: ColorTextEditEditor, onTransaction: ((ChangeBatch) -> Unit)?) {
        throw UnsupportedOperationException("TextEditorEvents is not available on the wasm target")
    }
    actual fun setChangeCallback(editor: ColorTextEditEditor, onChanged: (() -> Unit)?, delayMs: Int) {
        throw UnsupportedOperationException("TextEditorEvents is not available on the wasm target")
    }
    actual fun setAutoCompleteSuggestions(editor: ColorTextEditEditor, suggestions: List<String>) {
        throw UnsupportedOperationException("TextEditorEvents is not available on the wasm target")
    }
    actual fun setLineNumberContextMenuCallback(editor: ColorTextEditEditor, onPopup: ((PopupData) -> Unit)?) {
        throw UnsupportedOperationException("TextEditorEvents is not available on the wasm target")
    }
    actual fun setTextContextMenuCallback(editor: ColorTextEditEditor, onPopup: ((PopupData) -> Unit)?) {
        throw UnsupportedOperationException("TextEditorEvents is not available on the wasm target")
    }
    actual fun setTextHoverCallback(editor: ColorTextEditEditor, onPopup: ((PopupData) -> Unit)?) {
        throw UnsupportedOperationException("TextEditorEvents is not available on the wasm target")
    }
    actual fun isMousePosOverGlyph(editor: ColorTextEditEditor, x: Float, y: Float): Boolean = throw UnsupportedOperationException("TextEditorEvents is not available on the wasm target")
    actual fun getDocPosAtMousePos(editor: ColorTextEditEditor, x: Float, y: Float): Pair<Long, Long> = throw UnsupportedOperationException("TextEditorEvents is not available on the wasm target")
}
