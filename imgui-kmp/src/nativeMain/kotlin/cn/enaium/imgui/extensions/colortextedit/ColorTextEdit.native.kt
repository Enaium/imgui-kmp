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

import cn.enaium.imgui.ImVec2
import kotlinx.cinterop.*
import imgui.*

// =========================================================================
// Native (cinterop) actual implementations
// =========================================================================
internal class NativeColorTextEditEditor(internal val ptr: CPointer<te_editor>?) : ColorTextEditEditor {
    override fun close() {
        te_destroy(ptr)
    }
}

/** Casts [editor] to its native implementation and returns the raw `te_editor` pointer. */
private fun ptr(editor: ColorTextEditEditor): CPointer<te_editor>? =
    (editor as NativeColorTextEditEditor).ptr

/**
 * Takes ownership of a `char*` returned by a te_* function: copies it into a Kotlin
 * [String] and releases the native buffer with [te_string_free]. A null pointer
 * (e.g. no selection) is returned unchanged as an empty string.
 */
private fun takeString(p: CPointer<ByteVarOf<Byte>>?): String {
    if (p == null) return ""
    return p.toKString().also { te_string_free(p) }
}

actual object ColorTextEdit {
    actual fun create(): ColorTextEditEditor {
        val p = te_create() ?: error("te_create returned null")
        return NativeColorTextEditEditor(p)
    }

    actual fun destroy(editor: ColorTextEditEditor?) {
        if (editor != null) {
            te_destroy(ptr(editor))
        }
    }

    actual fun setText(editor: ColorTextEditEditor, text: String) =
        te_set_text(ptr(editor), text)

    actual fun getText(editor: ColorTextEditEditor): String =
        takeString(te_get_text(ptr(editor)))

    actual fun clearText(editor: ColorTextEditEditor) =
        te_clear_text(ptr(editor))

    actual fun isEmpty(editor: ColorTextEditEditor): Boolean =
        te_is_empty(ptr(editor))

    actual fun getLineCount(editor: ColorTextEditEditor): Long =
        te_get_line_count(ptr(editor)).toLong()

    actual fun getLineText(editor: ColorTextEditEditor, line: Long): String =
        takeString(te_get_line_text(ptr(editor), line.toULong()))

    actual fun render(
        editor: ColorTextEditEditor,
        title: String,
        size: ImVec2,
        childFlags: Int,
        windowFlags: Int,
    ): Boolean =
        te_render(ptr(editor), title, size.x, size.y, childFlags, windowFlags)

    actual fun setFocus(editor: ColorTextEditEditor) =
        te_set_focus(ptr(editor))

    actual fun setTabSize(editor: ColorTextEditEditor, value: Long) =
        te_set_tab_size(ptr(editor), value.toULong())

    actual fun getTabSize(editor: ColorTextEditEditor): Long =
        te_get_tab_size(ptr(editor)).toLong()

    actual fun setInsertSpacesOnTabs(editor: ColorTextEditEditor, value: Boolean) =
        te_set_insert_spaces_on_tabs(ptr(editor), value)

    actual fun isInsertSpacesOnTabs(editor: ColorTextEditEditor): Boolean =
        te_is_insert_spaces_on_tabs(ptr(editor))

    actual fun setLineSpacing(editor: ColorTextEditEditor, value: Float) =
        te_set_line_spacing(ptr(editor), value)

    actual fun getLineSpacing(editor: ColorTextEditEditor): Float =
        te_get_line_spacing(ptr(editor))

    actual fun setWordWrapEnabled(editor: ColorTextEditEditor, value: Boolean) =
        te_set_word_wrap_enabled(ptr(editor), value)

    actual fun isWordWrapEnabled(editor: ColorTextEditEditor): Boolean =
        te_is_word_wrap_enabled(ptr(editor))

    actual fun setReadOnlyEnabled(editor: ColorTextEditEditor, value: Boolean) =
        te_set_read_only_enabled(ptr(editor), value)

    actual fun isReadOnlyEnabled(editor: ColorTextEditEditor): Boolean =
        te_is_read_only_enabled(ptr(editor))

    actual fun setCaretsVisible(editor: ColorTextEditEditor, value: Boolean) =
        te_set_carets_visible(ptr(editor), value)

    actual fun isCaretsVisible(editor: ColorTextEditEditor): Boolean =
        te_is_carets_visible(ptr(editor))

    actual fun setAutoIndentEnabled(editor: ColorTextEditEditor, value: Boolean) =
        te_set_auto_indent_enabled(ptr(editor), value)

    actual fun isAutoIndentEnabled(editor: ColorTextEditEditor): Boolean =
        te_is_auto_indent_enabled(ptr(editor))

    actual fun setShowWhitespacesEnabled(editor: ColorTextEditEditor, value: Boolean) =
        te_set_show_whitespaces_enabled(ptr(editor), value)

    actual fun isShowWhitespacesEnabled(editor: ColorTextEditEditor): Boolean =
        te_is_show_whitespaces_enabled(ptr(editor))

    actual fun setShowLineNumbersEnabled(editor: ColorTextEditEditor, value: Boolean) =
        te_set_show_line_numbers_enabled(ptr(editor), value)

    actual fun isShowLineNumbersEnabled(editor: ColorTextEditEditor): Boolean =
        te_is_show_line_numbers_enabled(ptr(editor))

    actual fun setShowMiniMapEnabled(editor: ColorTextEditEditor, value: Boolean) =
        te_set_show_minimap_enabled(ptr(editor), value)

    actual fun isShowMiniMapEnabled(editor: ColorTextEditEditor): Boolean =
        te_is_show_minimap_enabled(ptr(editor))

    actual fun setShowMatchingBrackets(editor: ColorTextEditEditor, value: Boolean) =
        te_set_show_matching_brackets(ptr(editor), value)

    actual fun isShowingMatchingBrackets(editor: ColorTextEditEditor): Boolean =
        te_is_showing_matching_brackets(ptr(editor))

    actual fun setCompletePairedGlyphs(editor: ColorTextEditEditor, value: Boolean) =
        te_set_complete_paired_glyphs(ptr(editor), value)

    actual fun isCompletingPairedGlyphs(editor: ColorTextEditEditor): Boolean =
        te_is_completing_paired_glyphs(ptr(editor))

    actual fun setLineFoldingEnabled(editor: ColorTextEditEditor, value: Boolean) =
        te_set_line_folding_enabled(ptr(editor), value)

    actual fun isLineFoldingEnabled(editor: ColorTextEditEditor): Boolean =
        te_is_line_folding_enabled(ptr(editor))

    actual fun setOverwriteEnabled(editor: ColorTextEditEditor, value: Boolean) =
        te_set_overwrite_enabled(ptr(editor), value)

    actual fun isOverwriteEnabled(editor: ColorTextEditEditor): Boolean =
        te_is_overwrite_enabled(ptr(editor))

    actual fun setMiddleMouseScrollMode(editor: ColorTextEditEditor) =
        te_set_middle_mouse_scroll_mode(ptr(editor))

    actual fun setMiddleMousePanMode(editor: ColorTextEditEditor) =
        te_set_middle_mouse_pan_mode(ptr(editor))

    actual fun isMiddleMousePanMode(editor: ColorTextEditEditor): Boolean =
        te_is_middle_mouse_pan_mode(ptr(editor))

    actual fun setTextLeftMargin(editor: ColorTextEditEditor, value: Long) =
        te_set_text_left_margin(ptr(editor), value.toULong())

    actual fun getTextLeftMargin(editor: ColorTextEditEditor): Long =
        te_get_text_left_margin(ptr(editor)).toLong()

    actual fun setLanguage(editor: ColorTextEditEditor, language: Int) =
        te_set_language(ptr(editor), language)

    actual fun getLanguageName(editor: ColorTextEditEditor): String =
        takeString(te_get_language_name(ptr(editor)))

    actual fun getPaletteColor(editor: ColorTextEditEditor, color: Int): Int = memScoped {
        val out = alloc<UIntVar>()
        te_get_palette_color(ptr(editor), color, out.ptr)
        out.value.toInt()
    }

    actual fun setPaletteColor(editor: ColorTextEditEditor, color: Int, value: Int) =
        te_set_palette_color(ptr(editor), color, value.toUInt())

    actual fun setDefaultDarkPalette(editor: ColorTextEditEditor) =
        te_set_default_dark_palette(ptr(editor))

    actual fun setDefaultLightPalette(editor: ColorTextEditEditor) =
        te_set_default_light_palette(ptr(editor))

    actual fun cut(editor: ColorTextEditEditor) =
        te_cut(ptr(editor))

    actual fun copy(editor: ColorTextEditEditor) =
        te_copy(ptr(editor))

    actual fun paste(editor: ColorTextEditEditor) =
        te_paste(ptr(editor))

    actual fun undo(editor: ColorTextEditEditor) =
        te_undo(ptr(editor))

    actual fun redo(editor: ColorTextEditEditor) =
        te_redo(ptr(editor))

    actual fun canUndo(editor: ColorTextEditEditor): Boolean =
        te_can_undo(ptr(editor))

    actual fun canRedo(editor: ColorTextEditEditor): Boolean =
        te_can_redo(ptr(editor))

    actual fun selectAll(editor: ColorTextEditEditor) =
        te_select_all(ptr(editor))

    actual fun selectLine(editor: ColorTextEditEditor, line: Long) =
        te_select_line(ptr(editor), line.toULong())

    actual fun anyCursorHasSelection(editor: ColorTextEditEditor): Boolean =
        te_any_cursor_has_selection(ptr(editor))

    actual fun getSelectedText(editor: ColorTextEditEditor): String? {
        val p = te_get_selected_text(ptr(editor))
        return if (p == null) null else takeString(p)
    }

    actual fun selectFirstOccurrenceOf(editor: ColorTextEditEditor, text: String, caseSensitive: Boolean, wholeWord: Boolean) =
        te_select_first_occurrence_of(ptr(editor), text, caseSensitive, wholeWord)

    actual fun selectNextOccurrenceOf(editor: ColorTextEditEditor, text: String, caseSensitive: Boolean, wholeWord: Boolean) =
        te_select_next_occurrence_of(ptr(editor), text, caseSensitive, wholeWord)

    actual fun replaceTextInCurrentCursor(editor: ColorTextEditEditor, text: String) =
        te_replace_text_in_current_cursor(ptr(editor), text)

    actual fun replaceTextInAllCursors(editor: ColorTextEditEditor, text: String) =
        te_replace_text_in_all_cursors(ptr(editor), text)

    actual fun getNumberOfCursors(editor: ColorTextEditEditor): Long =
        te_get_number_of_cursors(ptr(editor)).toLong()

    actual fun getCursorText(editor: ColorTextEditEditor, cursor: Long): String =
        takeString(te_get_cursor_text(ptr(editor), cursor.toULong()))

    actual fun scrollToLine(editor: ColorTextEditEditor, line: Long, alignment: Int) =
        te_scroll_to_line(ptr(editor), line.toULong(), alignment)

    actual fun setCursorPos(editor: ColorTextEditEditor, line: Long, index: Long) =
        te_set_cursor_pos(ptr(editor), line.toULong(), index.toULong())

    actual fun getCursorPos(editor: ColorTextEditEditor): Pair<Long, Long> = memScoped {
        val line = alloc<ULongVar>()
        val index = alloc<ULongVar>()
        te_get_cursor_pos(ptr(editor), line.ptr, index.ptr)
        line.value.toLong() to index.value.toLong()
    }

    actual fun getLineHeight(editor: ColorTextEditEditor): Float =
        te_get_line_height(ptr(editor))

    actual fun getGlyphWidth(editor: ColorTextEditEditor): Float =
        te_get_glyph_width(ptr(editor))

    actual fun addMarker(
        editor: ColorTextEditEditor,
        line: Long,
        lineNumberColor: Int,
        textColor: Int,
        lineNumberTooltip: String?,
        textTooltip: String?,
    ) = te_add_marker(
        ptr(editor),
        line.toULong(),
        lineNumberColor.toUInt(),
        textColor.toUInt(),
        lineNumberTooltip,
        textTooltip,
    )

    actual fun clearMarkers(editor: ColorTextEditEditor) =
        te_clear_markers(ptr(editor))

    actual fun hasMarkers(editor: ColorTextEditEditor): Boolean =
        te_has_markers(ptr(editor))
}