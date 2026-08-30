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

package cn.enaium.imgui.extensions.colortextedit

import kotlinx.cinterop.*
import imgui.*

// =========================================================================
// Per-editor callback registries, keyed by the raw `te_editor` pointer value.
// The C side stores the key as opaque user_data; the static trampolines
// below look the Kotlin callback up here. File-private so both the actual
// object and the trampolines in this file can reach them.
// =========================================================================
private val transactionCallbacks = mutableMapOf<Long, ((ChangeBatch) -> Unit)?>()
private val changeCallbacks = mutableMapOf<Long, (() -> Unit)?>()
private val lineNumberPopups = mutableMapOf<Long, ((PopupData) -> Unit)?>()
private val textPopups = mutableMapOf<Long, ((PopupData) -> Unit)?>()
private val hoverPopups = mutableMapOf<Long, ((PopupData) -> Unit)?>()

/** Casts [editor] to its native implementation and returns the raw `te_editor` pointer. */
private fun ptr(editor: ColorTextEditEditor): CPointer<te_editor>? =
    (editor as NativeColorTextEditEditor).ptr

/** Uses the raw `te_editor` pointer value as the registry key. */
private fun key(editor: ColorTextEditEditor): Long =
    ptr(editor)?.rawValue?.toLong() ?: 0L

/** Decodes the editor key from the opaque user_data token. */
private fun keyOf(userData: COpaquePointer?): Long =
    userData?.rawValue?.toLong() ?: 0L

// =========================================================================
// C trampolines (static; they dispatch through the registries above)
// =========================================================================

/**
 * Transaction callback: converts the C-owned `te_change_batch` into Kotlin
 * [ChangeBatch] and MUST release the native memory with [te_change_batch_free].
 */
private fun transactionTrampoline(batch: CPointer<te_change_batch>?, userData: COpaquePointer?) {
    val result = ChangeBatch(
        buildList {
            if (batch != null) {
                val changes = batch.pointed.changes
                for (i in 0uL until batch.pointed.count) {
                    val change = changes!![i.toLong()]
                    add(
                        TextChange(
                            insert = change.insert,
                            startLine = change.start_line.toLong(),
                            startIndex = change.start_index.toLong(),
                            endLine = change.end_line.toLong(),
                            endIndex = change.end_index.toLong(),
                            text = change.text?.toKString() ?: "",
                        )
                    )
                }
            }
        }
    )
    // The C side owns the batch (array + per-change text); the binding frees it.
    te_change_batch_free(batch)
    transactionCallbacks[keyOf(userData)]?.invoke(result)
}

private fun changeTrampoline(userData: COpaquePointer?) {
    changeCallbacks[keyOf(userData)]?.invoke()
}

/** Reads the stack-local popup data synchronously (valid only inside the callback). */
private fun readPopup(popup: CPointer<te_popup_data>?): PopupData {
    val data = popup?.pointed ?: return PopupData(0L, 0L)
    return PopupData(data.line.toLong(), data.index.toLong())
}

private fun lineNumberPopupTrampoline(popup: CPointer<te_popup_data>?, userData: COpaquePointer?) {
    lineNumberPopups[keyOf(userData)]?.invoke(readPopup(popup))
}

private fun textPopupTrampoline(popup: CPointer<te_popup_data>?, userData: COpaquePointer?) {
    textPopups[keyOf(userData)]?.invoke(readPopup(popup))
}

private fun hoverPopupTrampoline(popup: CPointer<te_popup_data>?, userData: COpaquePointer?) {
    hoverPopups[keyOf(userData)]?.invoke(readPopup(popup))
}

// =========================================================================
// Native (cinterop) actual implementations
// =========================================================================
actual object TextEditorEvents {
    actual fun setTransactionCallback(editor: ColorTextEditEditor, onTransaction: ((ChangeBatch) -> Unit)?) {
        val p = ptr(editor)
        val k = key(editor)
        if (onTransaction != null) {
            transactionCallbacks[k] = onTransaction
            te_set_transaction_callback(p, staticCFunction(::transactionTrampoline), k.toCPointer<ByteVar>())
        } else {
            transactionCallbacks.remove(k)
            te_set_transaction_callback(p, null, null)
        }
    }

    actual fun setChangeCallback(editor: ColorTextEditEditor, onChanged: (() -> Unit)?, delayMs: Int) {
        val p = ptr(editor)
        val k = key(editor)
        if (onChanged != null) {
            changeCallbacks[k] = onChanged
            te_set_change_callback(p, staticCFunction(::changeTrampoline), delayMs, k.toCPointer<ByteVar>())
        } else {
            changeCallbacks.remove(k)
            te_set_change_callback(p, null, 0, null)
        }
    }

    actual fun setAutoCompleteSuggestions(editor: ColorTextEditEditor, suggestions: List<String>) = memScoped {
        val values = allocArray<CPointerVar<ByteVar>>(suggestions.size)
        suggestions.forEachIndexed { i, s -> values[i] = s.cstr.ptr }
        te_set_auto_complete_suggestions(ptr(editor), values, suggestions.size.toULong())
    }

    actual fun setLineNumberContextMenuCallback(editor: ColorTextEditEditor, onPopup: ((PopupData) -> Unit)?) {
        val p = ptr(editor)
        val k = key(editor)
        if (onPopup != null) {
            lineNumberPopups[k] = onPopup
            te_set_line_number_context_menu_callback(p, staticCFunction(::lineNumberPopupTrampoline), k.toCPointer<ByteVar>())
        } else {
            lineNumberPopups.remove(k)
            te_clear_line_number_context_menu_callback(p)
        }
    }

    actual fun setTextContextMenuCallback(editor: ColorTextEditEditor, onPopup: ((PopupData) -> Unit)?) {
        val p = ptr(editor)
        val k = key(editor)
        if (onPopup != null) {
            textPopups[k] = onPopup
            te_set_text_context_menu_callback(p, staticCFunction(::textPopupTrampoline), k.toCPointer<ByteVar>())
        } else {
            textPopups.remove(k)
            te_clear_text_context_menu_callback(p)
        }
    }

    actual fun setTextHoverCallback(editor: ColorTextEditEditor, onPopup: ((PopupData) -> Unit)?) {
        val p = ptr(editor)
        val k = key(editor)
        if (onPopup != null) {
            hoverPopups[k] = onPopup
            te_set_text_hover_callback(p, staticCFunction(::hoverPopupTrampoline), k.toCPointer<ByteVar>())
        } else {
            hoverPopups.remove(k)
            te_clear_text_hover_callback(p)
        }
    }

    actual fun isMousePosOverGlyph(editor: ColorTextEditEditor, x: Float, y: Float): Boolean =
        te_is_mouse_pos_over_glyph(ptr(editor), x, y)

    actual fun getDocPosAtMousePos(editor: ColorTextEditEditor, x: Float, y: Float): Pair<Long, Long> = memScoped {
        val line = alloc<ULongVar>()
        val index = alloc<ULongVar>()
        te_get_doc_pos_at_mouse_pos(ptr(editor), x, y, line.ptr, index.ptr)
        line.value.toLong() to index.value.toLong()
    }
}