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

package cn.enaium.imgui.extensions.memoryeditor

/**
 * A memory editor instance wrapping imgui_club's MemoryEditor;
 * close() calls [MemoryEditor.destroy].
 */
interface MemoryEditorInstance : AutoCloseable

/**
 * Kotlin bindings for imgui_club's MemoryEditor, inside the
 * cn.enaium.imgui.extensions.memoryeditor package.
 */
expect object MemoryEditor {
    fun create(): MemoryEditorInstance
    fun destroy(e: MemoryEditorInstance? = null)

    /** Standalone memory editor window ([isOpen] becomes false when the window is closed). */
    fun drawWindow(e: MemoryEditorInstance, title: String, data: ByteArray, baseAddr: Long = 0)

    /** Memory editor contents only, for embedding inside an existing window. */
    fun drawContents(e: MemoryEditorInstance, data: ByteArray, baseAddr: Long = 0)

    // ==================== Settings ====================

    /** set to false when [drawWindow] was closed. Ignored if not using [drawWindow]. */
    fun isOpen(e: MemoryEditorInstance): Boolean
    fun setOpen(e: MemoryEditorInstance, value: Boolean)

    /** disable any editing. */
    fun isReadOnly(e: MemoryEditorInstance): Boolean
    fun setReadOnly(e: MemoryEditorInstance, value: Boolean)

    /** number of columns to display. */
    fun getCols(e: MemoryEditorInstance): Int
    fun setCols(e: MemoryEditorInstance, value: Int)

    /**
     * display options button/context menu. When disabled, options will be locked unless you
     * provide your own UI for them.
     */
    fun isOptShowOptions(e: MemoryEditorInstance): Boolean
    fun setOptShowOptions(e: MemoryEditorInstance, value: Boolean)

    /** display a footer previewing the decimal/binary/hex/float representation of the currently selected bytes. */
    fun isOptShowDataPreview(e: MemoryEditorInstance): Boolean
    fun setOptShowDataPreview(e: MemoryEditorInstance, value: Boolean)

    /**
     * display values in HexII representation instead of regular hexadecimal:
     * hide null/zero bytes, ascii values as ".X".
     */
    fun isOptShowHexII(e: MemoryEditorInstance): Boolean
    fun setOptShowHexII(e: MemoryEditorInstance, value: Boolean)

    /** display ASCII representation on the right side. */
    fun isOptShowAscii(e: MemoryEditorInstance): Boolean
    fun setOptShowAscii(e: MemoryEditorInstance, value: Boolean)

    /** display null/zero bytes using the TextDisabled color. */
    fun isOptGreyOutZeroes(e: MemoryEditorInstance): Boolean
    fun setOptGreyOutZeroes(e: MemoryEditorInstance, value: Boolean)

    /** display hexadecimal values as "FF" instead of "ff". */
    fun isOptUpperCaseHex(e: MemoryEditorInstance): Boolean
    fun setOptUpperCaseHex(e: MemoryEditorInstance, value: Boolean)

    /** set to 0 to disable extra spacing between every mid-cols. */
    fun getOptMidColsCount(e: MemoryEditorInstance): Int
    fun setOptMidColsCount(e: MemoryEditorInstance, value: Int)

    /** number of addr digits to display (default calculated based on maximum displayed addr). */
    fun getOptAddrDigitsCount(e: MemoryEditorInstance): Int
    fun setOptAddrDigitsCount(e: MemoryEditorInstance, value: Int)

    /** space to reserve at the bottom of the widget to add custom widgets. */
    fun getOptFooterExtraHeight(e: MemoryEditorInstance): Float
    fun setOptFooterExtraHeight(e: MemoryEditorInstance, value: Float)

    /** background color of highlighted bytes (packed IM_COL32). */
    fun getHighlightColor(e: MemoryEditorInstance): Int
    fun setHighlightColor(e: MemoryEditorInstance, value: Int)

    // ==================== Public read-only data ====================

    /** true when the mouse is hovering a value (valid after the last draw call). */
    fun isMouseHovered(e: MemoryEditorInstance): Boolean

    /** the address currently being hovered if [isMouseHovered] is true. */
    fun mouseHoveredAddr(e: MemoryEditorInstance): Long
}

// =========================================================================
// Enums (values match imgui_memory_editor.h)
// =========================================================================

object MeDataFormat {
    const val BIN: Int = 0
    const val DEC: Int = 1
    const val HEX: Int = 2
}
