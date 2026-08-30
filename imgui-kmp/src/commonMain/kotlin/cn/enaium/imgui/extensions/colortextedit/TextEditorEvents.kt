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

/**
 * A single text edit inside a transaction (mirrors TextEditor::Change).
 * [insert] is true for an insert, false for a delete. Positions are
 * zero-based (line/index) DocPos values; [text] is the inserted/deleted
 * UTF-8 text.
 */
data class TextChange(
    val insert: Boolean,
    val startLine: Long,
    val startIndex: Long,
    val endLine: Long,
    val endIndex: Long,
    val text: String,
)

/** A set of changes belonging to one transaction. */
data class ChangeBatch(val changes: List<TextChange>)

/** Position data for popup/hover callbacks (mirrors TextEditor::PopupData). */
data class PopupData(
    val line: Long,
    val index: Long,
)

/**
 * Kotlin callback hooks for TextEditor's event/async APIs — the surface a
 * language-server integration needs. Not an expect 'object': these functions
 * take and return values directly.
 */
expect object TextEditorEvents {
    // ==================== Change / transaction callbacks ====================

    /**
     * Registers a per-transaction change callback: invoked on every edit
     * (keystroke, delete, cut, paste, undo, redo) with the batch of changes.
     * Pass null to deactivate. [onTransaction] runs synchronously during the
     * edit.
     */
    fun setTransactionCallback(editor: ColorTextEditEditor, onTransaction: ((ChangeBatch) -> Unit)?)

    /**
     * Registers a summary change callback, optionally debounced by
     * [delayMs]. Pass null to deactivate.
     */
    fun setChangeCallback(editor: ColorTextEditEditor, onChanged: (() -> Unit)?, delayMs: Int = 0)

    // ==================== Async autocomplete ====================

    /**
     * Injects suggestions produced asynchronously (e.g. by an LSP lookup
     * thread). Must be called from the rendering thread.
     */
    fun setAutoCompleteSuggestions(editor: ColorTextEditEditor, suggestions: List<String>)

    // ==================== Popup / hover callbacks ====================

    fun setLineNumberContextMenuCallback(editor: ColorTextEditEditor, onPopup: ((PopupData) -> Unit)?)
    fun setTextContextMenuCallback(editor: ColorTextEditEditor, onPopup: ((PopupData) -> Unit)?)
    fun setTextHoverCallback(editor: ColorTextEditEditor, onPopup: ((PopupData) -> Unit)?)

    // ==================== Mouse queries ====================

    fun isMousePosOverGlyph(editor: ColorTextEditEditor, x: Float, y: Float): Boolean
    fun getDocPosAtMousePos(editor: ColorTextEditEditor, x: Float, y: Float): Pair<Long, Long>
}