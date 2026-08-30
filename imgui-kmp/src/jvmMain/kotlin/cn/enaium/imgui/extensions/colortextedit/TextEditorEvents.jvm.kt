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

// Per-editor callback registries, keyed by the raw editor pointer. Callbacks
// are stored in Kotlin and dispatched from TextEditorEventsJvmBridge when the
// C trampolines fire. Entries are removed when a callback is deactivated so
// released editors do not leak.
private val transactionRegistries = mutableMapOf<Long, (ChangeBatch) -> Unit>()
private val changeRegistries = mutableMapOf<Long, () -> Unit>()
private val lineNumberPopupRegistries = mutableMapOf<Long, (PopupData) -> Unit>()
private val textPopupRegistries = mutableMapOf<Long, (PopupData) -> Unit>()
private val hoverPopupRegistries = mutableMapOf<Long, (PopupData) -> Unit>()

/**
 * Static callbacks invoked from the C trampolines in jni_bridge.cpp
 * (JVM class: cn.enaium.imgui.extensions.colortextedit.TextEditorEventsJvmBridge).
 * Dispatches to the per-editor registries keyed by the editor pointer.
 */
internal object TextEditorEventsJvmBridge {
    @JvmStatic
    fun notifyTransaction(
        editorPtr: Long,
        inserts: BooleanArray,
        startLines: LongArray,
        startIndexes: LongArray,
        endLines: LongArray,
        endIndexes: LongArray,
        texts: Array<String>,
    ) {
        val callback = transactionRegistries[editorPtr] ?: return
        val changes = ArrayList<TextChange>(inserts.size)
        for (i in inserts.indices) {
            changes.add(
                TextChange(
                    insert = inserts[i],
                    startLine = startLines[i],
                    startIndex = startIndexes[i],
                    endLine = endLines[i],
                    endIndex = endIndexes[i],
                    text = texts[i],
                )
            )
        }
        callback(ChangeBatch(changes))
    }

    @JvmStatic
    fun notifyChange(editorPtr: Long) {
        changeRegistries[editorPtr]?.invoke()
    }

    @JvmStatic
    fun notifyLineNumberPopup(editorPtr: Long, line: Long, index: Long) {
        lineNumberPopupRegistries[editorPtr]?.invoke(PopupData(line, index))
    }

    @JvmStatic
    fun notifyTextPopup(editorPtr: Long, line: Long, index: Long) {
        textPopupRegistries[editorPtr]?.invoke(PopupData(line, index))
    }

    @JvmStatic
    fun notifyHoverPopup(editorPtr: Long, line: Long, index: Long) {
        hoverPopupRegistries[editorPtr]?.invoke(PopupData(line, index))
    }
}

// =========================================================================
// JVM/Android actual implementations
// =========================================================================

actual object TextEditorEvents {
    // ==================== Change / transaction callbacks ====================

    actual fun setTransactionCallback(editor: ColorTextEditEditor, onTransaction: ((ChangeBatch) -> Unit)?) {
        val ptr = (editor as JvmColorTextEditEditor).ptr
        if (onTransaction != null) {
            transactionRegistries[ptr] = onTransaction
        } else {
            transactionRegistries.remove(ptr)
        }
        Jni.setTransactionCallback(ptr, onTransaction != null)
    }

    actual fun setChangeCallback(editor: ColorTextEditEditor, onChanged: (() -> Unit)?, delayMs: Int) {
        val ptr = (editor as JvmColorTextEditEditor).ptr
        if (onChanged != null) {
            changeRegistries[ptr] = onChanged
        } else {
            changeRegistries.remove(ptr)
        }
        Jni.setChangeCallback(ptr, onChanged != null, delayMs)
    }

    // ==================== Async autocomplete ====================

    actual fun setAutoCompleteSuggestions(editor: ColorTextEditEditor, suggestions: List<String>) {
        Jni.setAutoCompleteSuggestions((editor as JvmColorTextEditEditor).ptr, suggestions.toTypedArray())
    }

    // ==================== Popup / hover callbacks ====================

    actual fun setLineNumberContextMenuCallback(editor: ColorTextEditEditor, onPopup: ((PopupData) -> Unit)?) {
        val ptr = (editor as JvmColorTextEditEditor).ptr
        if (onPopup != null) {
            lineNumberPopupRegistries[ptr] = onPopup
        } else {
            lineNumberPopupRegistries.remove(ptr)
        }
        Jni.setLineNumberContextMenuCallback(ptr, onPopup != null)
    }

    actual fun setTextContextMenuCallback(editor: ColorTextEditEditor, onPopup: ((PopupData) -> Unit)?) {
        val ptr = (editor as JvmColorTextEditEditor).ptr
        if (onPopup != null) {
            textPopupRegistries[ptr] = onPopup
        } else {
            textPopupRegistries.remove(ptr)
        }
        Jni.setTextContextMenuCallback(ptr, onPopup != null)
    }

    actual fun setTextHoverCallback(editor: ColorTextEditEditor, onPopup: ((PopupData) -> Unit)?) {
        val ptr = (editor as JvmColorTextEditEditor).ptr
        if (onPopup != null) {
            hoverPopupRegistries[ptr] = onPopup
        } else {
            hoverPopupRegistries.remove(ptr)
        }
        Jni.setTextHoverCallback(ptr, onPopup != null)
    }

    // ==================== Mouse queries ====================

    actual fun isMousePosOverGlyph(editor: ColorTextEditEditor, x: Float, y: Float): Boolean =
        Jni.isMousePosOverGlyph((editor as JvmColorTextEditEditor).ptr, x, y)

    actual fun getDocPosAtMousePos(editor: ColorTextEditEditor, x: Float, y: Float): Pair<Long, Long> {
        val pos = LongArray(2)
        Jni.getDocPosAtMousePos((editor as JvmColorTextEditEditor).ptr, x, y, pos)
        return Pair(pos[0], pos[1])
    }
}