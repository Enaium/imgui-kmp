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

// =========================================================================
// Per-editor callback registries for the completion bindings, keyed by the
// raw `te_editor` pointer value. The C side stores the key as opaque
// user_data; the static trampolines below look the Kotlin callbacks up here.
// =========================================================================
private val autocompleteCallbacks = mutableMapOf<Long, ((AutocompleteState) -> AutocompleteResult)?>()
private val insertorCallbacks = mutableMapOf<Long, ((Long) -> Long)?>()
private val deletorCallbacks = mutableMapOf<Long, ((Long, Long) -> Unit)?>()
private val iterateUserDataCallbacks = mutableMapOf<Long, ((Long, Long) -> Unit)?>()

/** Uses the raw `te_editor` pointer value as the registry key. */
private fun key(editor: ColorTextEditEditor): Long =
    ptr(editor)?.rawValue?.toLong() ?: 0L

/** Decodes the registry key from the opaque user_data token. */
private fun keyOf(userData: COpaquePointer?): Long =
    userData?.rawValue?.toLong() ?: 0L

// =========================================================================
// C trampolines (static; they dispatch through the registries above)
// =========================================================================

/**
 * Autocomplete callback: converts the C-owned `te_autocomplete_state` into a
 * Kotlin [AutocompleteState], invokes the registered callback and fills the
 * C-owned `te_autocomplete_result` with the returned suggestions. The C side
 * copies the suggestions back as soon as this returns, so the pointer array
 * and the string buffers are allocated on the native heap (never freed —
 * mirroring the C side, which does not free `out.suggestions` either).
 */
private fun autocompleteTrampoline(statePtr: CPointer<te_autocomplete_state>?, resultPtr: CPointer<te_autocomplete_result>?) {
    val state = statePtr?.pointed ?: return
    val callback = autocompleteCallbacks[keyOf(state.user_data)] ?: return
    val result = callback(
        AutocompleteState(
            searchTerm = state.search_term?.toKString() ?: "",
            searchTermStartLine = state.search_term_start_line.toLong(),
            searchTermStartIndex = state.search_term_start_index.toLong(),
            searchTermEndLine = state.search_term_end_line.toLong(),
            searchTermEndIndex = state.search_term_end_index.toLong(),
            inIdentifier = state.in_identifier,
            inNumber = state.in_number,
            inComment = state.in_comment,
            inString = state.in_string,
        )
    )
    val out = resultPtr?.pointed ?: return
    out.suggestions_promise = result.suggestionsPromise
    val suggestions = result.suggestions
    out.suggestion_count = suggestions.size.toUInt()
    if (suggestions.isNotEmpty()) {
        val array = nativeHeap.allocArray<CPointerVar<ByteVar>>(suggestions.size)
        suggestions.forEachIndexed { i, s ->
            // The C callback copies suggestions synchronously after our return,
            // so the buffers must outlive the trampoline: no memScoped here.
            val bytes = s.encodeToByteArray()
            val mem = nativeHeap.allocArray<ByteVar>(bytes.size + 1)
            bytes.forEachIndexed { j, b -> mem[j] = b.toByte() }
            mem[bytes.size] = 0
            array[i] = mem
        }
        out.suggestions = array
    } else {
        out.suggestions = null
    }
}

/** Insertor callback: reports the inserted line and returns the opaque data token. */
private fun insertorTrampoline(line: ULong, userData: COpaquePointer?): COpaquePointer? {
    val callback = insertorCallbacks[keyOf(userData)] ?: return null
    val token = callback(line.toLong())
    return if (token == 0L) null else token.toCPointer<ByteVar>()
}

/** Deletor callback: reports the removed line and its opaque data token. */
private fun deletorTrampoline(line: ULong, data: COpaquePointer?, userData: COpaquePointer?) {
    deletorCallbacks[keyOf(userData)]?.invoke(line.toLong(), data?.rawValue?.toLong() ?: 0L)
}

/** User-data iteration callback: reports each line and its opaque data token. */
private fun iterateUserDataTrampoline(line: ULong, data: COpaquePointer?, userData: COpaquePointer?) {
    iterateUserDataCallbacks[keyOf(userData)]?.invoke(line.toLong(), data?.rawValue?.toLong() ?: 0L)
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
    // ==================== Autocomplete config ====================
    actual fun setAutoCompleteConfig(
        editor: ColorTextEditEditor,
        onSuggestions: ((AutocompleteState) -> AutocompleteResult)?,
        triggerOnTyping: Boolean,
        triggerOnShortcut: Boolean,
        triggerInComments: Boolean,
        triggerInStrings: Boolean,
        autoInsertSingleSuggestions: Boolean,
        triggerDelayMs: Int,
        suggestionWidth: Long,
    ) {
        val k = key(editor)
        if (onSuggestions != null) {
            autocompleteCallbacks[k] = onSuggestions
            te_set_auto_complete_config(
                ptr(editor),
                staticCFunction(::autocompleteTrampoline),
                k.toCPointer<ByteVar>(),
                triggerOnTyping,
                triggerOnShortcut,
                triggerInComments,
                triggerInStrings,
                autoInsertSingleSuggestions,
                triggerDelayMs,
                suggestionWidth.toUInt(),
            )
        } else {
            autocompleteCallbacks.remove(k)
            te_set_auto_complete_config(ptr(editor), null, null, false, false, false, false, false, 0, 0u)
        }
    }

    // ==================== Additional text queries and edits ====================
    actual fun getSectionText(editor: ColorTextEditEditor, startLine: Long, startIndex: Long, endLine: Long, endIndex: Long): String =
        takeString(te_get_section_text(ptr(editor), startLine.toULong(), startIndex.toULong(), endLine.toULong(), endIndex.toULong()))

    actual fun replaceSectionText(editor: ColorTextEditEditor, startLine: Long, startIndex: Long, endLine: Long, endIndex: Long, text: String) =
        te_replace_section_text(ptr(editor), startLine.toULong(), startIndex.toULong(), endLine.toULong(), endIndex.toULong(), text)

    actual fun selectionToLowerCase(editor: ColorTextEditEditor) =
        te_selection_to_lower_case(ptr(editor))

    actual fun selectionToUpperCase(editor: ColorTextEditEditor) =
        te_selection_to_upper_case(ptr(editor))

    actual fun stripTrailingWhitespaces(editor: ColorTextEditEditor) =
        te_strip_trailing_whitespaces(ptr(editor))

    actual fun tabsToSpaces(editor: ColorTextEditEditor) =
        te_tabs_to_spaces(ptr(editor))

    actual fun spacesToTabs(editor: ColorTextEditEditor) =
        te_spaces_to_tabs(ptr(editor))

    actual fun indentLines(editor: ColorTextEditEditor) =
        te_indent_lines(ptr(editor))

    actual fun deindentLines(editor: ColorTextEditEditor) =
        te_deindent_lines(ptr(editor))

    actual fun moveUpLines(editor: ColorTextEditEditor) =
        te_move_up_lines(ptr(editor))

    actual fun moveDownLines(editor: ColorTextEditEditor) =
        te_move_down_lines(ptr(editor))

    actual fun toggleComments(editor: ColorTextEditEditor) =
        te_toggle_comments(ptr(editor))

    // ==================== Additional selection / cursor API ====================
    actual fun selectLines(editor: ColorTextEditEditor, start: Long, end: Long) =
        te_select_lines(ptr(editor), start.toULong(), end.toULong())

    actual fun selectRegion(editor: ColorTextEditEditor, startLine: Long, startIndex: Long, endLine: Long, endIndex: Long) =
        te_select_region(ptr(editor), startLine.toULong(), startIndex.toULong(), endLine.toULong(), endIndex.toULong())

    actual fun selectToBrackets(editor: ColorTextEditEditor, includeBrackets: Boolean) =
        te_select_to_brackets(ptr(editor), includeBrackets)

    actual fun growSelections(editor: ColorTextEditEditor) =
        te_grow_selections(ptr(editor))

    actual fun shrinkSelections(editor: ColorTextEditEditor) =
        te_shrink_selections(ptr(editor))

    actual fun addNextOccurrence(editor: ColorTextEditEditor, wholeWord: Boolean) =
        te_add_next_occurrence(ptr(editor), wholeWord)

    actual fun selectAllOccurrences(editor: ColorTextEditEditor, wholeWord: Boolean) =
        te_select_all_occurrences(ptr(editor), wholeWord)

    actual fun clearCursors(editor: ColorTextEditEditor) =
        te_clear_cursors(ptr(editor))

    actual fun getCursorPosition(editor: ColorTextEditEditor, cursor: Long): Pair<Long, Long> = memScoped {
        val line = alloc<ULongVar>()
        val index = alloc<ULongVar>()
        te_get_cursor_position(ptr(editor), cursor.toULong(), line.ptr, index.ptr)
        line.value.toLong() to index.value.toLong()
    }

    actual fun getCursorSelection(editor: ColorTextEditEditor, cursor: Long): LongArray = memScoped {
        val startLine = alloc<ULongVar>()
        val startIndex = alloc<ULongVar>()
        val endLine = alloc<ULongVar>()
        val endIndex = alloc<ULongVar>()
        te_get_cursor_selection(ptr(editor), cursor.toULong(), startLine.ptr, startIndex.ptr, endLine.ptr, endIndex.ptr)
        longArrayOf(startLine.value.toLong(), startIndex.value.toLong(), endLine.value.toLong(), endIndex.value.toLong())
    }

    // ==================== Word / find query ====================
    actual fun getWordAtMousePos(editor: ColorTextEditEditor, x: Float, y: Float): String =
        takeString(te_get_word_at_mouse_pos(ptr(editor), x, y))

    actual fun findWordStart(editor: ColorTextEditEditor, line: Long, index: Long, wholeWord: Boolean): Pair<Long, Long> = memScoped {
        val outLine = alloc<ULongVar>()
        val outIndex = alloc<ULongVar>()
        te_find_word_start(ptr(editor), line.toULong(), index.toULong(), wholeWord, outLine.ptr, outIndex.ptr)
        outLine.value.toLong() to outIndex.value.toLong()
    }

    actual fun findWordEnd(editor: ColorTextEditEditor, line: Long, index: Long, wholeWord: Boolean): Pair<Long, Long> = memScoped {
        val outLine = alloc<ULongVar>()
        val outIndex = alloc<ULongVar>()
        te_find_word_end(ptr(editor), line.toULong(), index.toULong(), wholeWord, outLine.ptr, outIndex.ptr)
        outLine.value.toLong() to outIndex.value.toLong()
    }

    actual fun hasFindString(editor: ColorTextEditEditor): Boolean =
        te_has_find_string(ptr(editor))

    actual fun findNext(editor: ColorTextEditEditor) =
        te_find_next(ptr(editor))

    actual fun findAll(editor: ColorTextEditEditor) =
        te_find_all(ptr(editor))

    actual fun openFindReplaceWindow(editor: ColorTextEditEditor) =
        te_open_find_replace_window(ptr(editor))

    actual fun closeFindReplaceWindow(editor: ColorTextEditEditor) =
        te_close_find_replace_window(ptr(editor))

    actual fun setFindButtonLabel(editor: ColorTextEditEditor, label: String) =
        te_set_find_button_label(ptr(editor), label)

    actual fun setFindAllButtonLabel(editor: ColorTextEditEditor, label: String) =
        te_set_find_all_button_label(ptr(editor), label)

    actual fun setReplaceButtonLabel(editor: ColorTextEditEditor, label: String) =
        te_set_replace_button_label(ptr(editor), label)

    actual fun setReplaceAllButtonLabel(editor: ColorTextEditEditor, label: String) =
        te_set_replace_all_button_label(ptr(editor), label)

    // ==================== Visibility / folding ====================
    actual fun isMousePosOverTextArea(editor: ColorTextEditEditor, x: Float, y: Float): Boolean =
        te_is_mouse_pos_over_text_area(ptr(editor), x, y)

    actual fun isDocPosVisible(editor: ColorTextEditEditor, line: Long, index: Long): Boolean =
        te_is_doc_pos_visible(ptr(editor), line.toULong(), index.toULong())

    actual fun isLineFoldable(editor: ColorTextEditEditor, line: Long): Boolean =
        te_is_line_foldable(ptr(editor), line.toULong())

    actual fun isLineFolded(editor: ColorTextEditEditor, line: Long): Boolean =
        te_is_line_folded(ptr(editor), line.toULong())

    actual fun isLineVisible(editor: ColorTextEditEditor, line: Long): Boolean =
        te_is_line_visible(ptr(editor), line.toULong())

    actual fun isLineHidden(editor: ColorTextEditEditor, line: Long): Boolean =
        te_is_line_hidden(ptr(editor), line.toULong())

    actual fun foldAroundLine(editor: ColorTextEditEditor, line: Long) =
        te_fold_around_line(ptr(editor), line.toULong())

    actual fun unfoldAroundLine(editor: ColorTextEditEditor, line: Long) =
        te_unfold_around_line(ptr(editor), line.toULong())

    actual fun toggleAtLine(editor: ColorTextEditEditor, line: Long) =
        te_toggle_at_line(ptr(editor), line.toULong())

    actual fun unfoldAll(editor: ColorTextEditEditor) =
        te_unfold_all(ptr(editor))

    actual fun getFirstVisibleRow(editor: ColorTextEditEditor): Long =
        te_get_first_visible_row(ptr(editor)).toLong()

    actual fun getFirstVisibleColumn(editor: ColorTextEditEditor): Long =
        te_get_first_visible_column(ptr(editor)).toLong()

    actual fun getLastVisibleRow(editor: ColorTextEditEditor): Long =
        te_get_last_visible_row(ptr(editor)).toLong()

    actual fun getLastVisibleColumn(editor: ColorTextEditEditor): Long =
        te_get_last_visible_column(ptr(editor)).toLong()

    // ==================== Coordinate transforms ====================
    actual fun docPosToVisPos(editor: ColorTextEditEditor, line: Long, index: Long): Pair<Long, Long> = memScoped {
        val row = alloc<ULongVar>()
        val column = alloc<ULongVar>()
        te_doc_pos_to_vis_pos(ptr(editor), line.toULong(), index.toULong(), row.ptr, column.ptr)
        row.value.toLong() to column.value.toLong()
    }

    actual fun visPosToDocPos(editor: ColorTextEditEditor, row: Long, column: Long): Pair<Long, Long> = memScoped {
        val line = alloc<ULongVar>()
        val index = alloc<ULongVar>()
        te_vis_pos_to_doc_pos(ptr(editor), row.toULong(), column.toULong(), line.ptr, index.ptr)
        line.value.toLong() to index.value.toLong()
    }

    // ==================== Undo state ====================
    actual fun getUndoIndex(editor: ColorTextEditEditor): Long =
        te_get_undo_index(ptr(editor)).toLong()

    // ==================== Static configuration ====================
    actual fun setDefaultPalette(text: Int, keyword: Int, number: Int, string: Int, comment: Int, background: Int, cursor: Int, selection: Int) {
        te_set_default_palette(text.toUInt(), keyword.toUInt(), number.toUInt(), string.toUInt(), comment.toUInt(), background.toUInt(), cursor.toUInt(), selection.toUInt())
    }

    actual fun getDefaultPalette(): LongArray = memScoped {
        val text = alloc<UIntVar>()
        val keyword = alloc<UIntVar>()
        val number = alloc<UIntVar>()
        val string = alloc<UIntVar>()
        val comment = alloc<UIntVar>()
        val background = alloc<UIntVar>()
        val cursor = alloc<UIntVar>()
        val selection = alloc<UIntVar>()
        te_get_default_palette(text.ptr, keyword.ptr, number.ptr, string.ptr, comment.ptr, background.ptr, cursor.ptr, selection.ptr)
        longArrayOf(
            text.value.toLong(), keyword.value.toLong(), number.value.toLong(), string.value.toLong(),
            comment.value.toLong(), background.value.toLong(), cursor.value.toLong(), selection.value.toLong(),
        )
    }

    actual fun setImGuiContext(imGuiContext: Long) {
        te_set_im_gui_context(imGuiContext.toULong())
    }

    // ==================== Remaining configuration toggles ====================
    actual fun setShowSpacesEnabled(editor: ColorTextEditEditor, value: Boolean) =
        te_set_show_spaces_enabled(ptr(editor), value)

    actual fun isShowSpacesEnabled(editor: ColorTextEditEditor): Boolean =
        te_is_show_spaces_enabled(ptr(editor))

    actual fun setShowTabsEnabled(editor: ColorTextEditEditor, value: Boolean) =
        te_set_show_tabs_enabled(ptr(editor), value)

    actual fun isShowTabsEnabled(editor: ColorTextEditEditor): Boolean =
        te_is_show_tabs_enabled(ptr(editor))

    actual fun setShowScrollbarMiniMapEnabled(editor: ColorTextEditEditor, value: Boolean) =
        te_set_show_scrollbar_minimap_enabled(ptr(editor), value)

    actual fun isShowScrollbarMiniMapEnabled(editor: ColorTextEditEditor): Boolean =
        te_is_show_scrollbar_minimap_enabled(ptr(editor))

    actual fun setShowPanScrollIndicatorEnabled(editor: ColorTextEditEditor, value: Boolean) =
        te_set_show_pan_scroll_indicator_enabled(ptr(editor), value)

    actual fun isShowPanScrollIndicatorEnabled(editor: ColorTextEditEditor): Boolean =
        te_is_show_pan_scroll_indicator_enabled(ptr(editor))

    actual fun setMiniMapColumns(editor: ColorTextEditEditor, value: Long) =
        te_set_minimap_columns(ptr(editor), value.toULong())

    actual fun getMiniMapColumns(editor: ColorTextEditEditor): Long =
        te_get_minimap_columns(ptr(editor)).toLong()

    actual fun setLineNumberLeftMargin(editor: ColorTextEditEditor, value: Long) =
        te_set_line_number_left_margin(ptr(editor), value.toULong())

    actual fun getLineNumberLeftMargin(editor: ColorTextEditEditor): Long =
        te_get_line_number_left_margin(ptr(editor)).toLong()

    actual fun setDecorationLeftMargin(editor: ColorTextEditEditor, value: Long) =
        te_set_decoration_left_margin(ptr(editor), value.toULong())

    actual fun getDecorationLeftMargin(editor: ColorTextEditEditor): Long =
        te_get_decoration_left_margin(ptr(editor)).toLong()

    actual fun setLineBreakConfig(editor: ColorTextEditEditor, breakAfter: String, breakBefore: String, useUnicodeAnnex14: Boolean) =
        te_set_line_break_config(ptr(editor), breakAfter, breakBefore, useUnicodeAnnex14)

    // ==================== Line data hooks ====================
    actual fun setInsertor(editor: ColorTextEditEditor, insertor: ((Long) -> Long)?) {
        val k = key(editor)
        if (insertor != null) {
            insertorCallbacks[k] = insertor
            te_set_insertor(ptr(editor), staticCFunction(::insertorTrampoline), k.toCPointer<ByteVar>())
        } else {
            insertorCallbacks.remove(k)
            te_set_insertor(ptr(editor), null, null)
        }
    }

    actual fun setDeletor(editor: ColorTextEditEditor, deletor: ((Long, Long) -> Unit)?) {
        val k = key(editor)
        if (deletor != null) {
            deletorCallbacks[k] = deletor
            te_set_deletor(ptr(editor), staticCFunction(::deletorTrampoline), k.toCPointer<ByteVar>())
        } else {
            deletorCallbacks.remove(k)
            te_set_deletor(ptr(editor), null, null)
        }
    }

    actual fun setUserData(editor: ColorTextEditEditor, line: Long, data: Long) =
        te_set_user_data(ptr(editor), line.toULong(), if (data == 0L) null else data.toCPointer<ByteVar>())

    actual fun getUserData(editor: ColorTextEditEditor, line: Long): Long =
        te_get_user_data(ptr(editor), line.toULong())?.rawValue?.toLong() ?: 0L

    actual fun iterateUserData(editor: ColorTextEditEditor, iterate: ((Long, Long) -> Unit)?) {
        val k = key(editor)
        if (iterate != null) {
            iterateUserDataCallbacks[k] = iterate
            te_iterate_user_data(ptr(editor), staticCFunction(::iterateUserDataTrampoline), k.toCPointer<ByteVar>())
        } else {
            iterateUserDataCallbacks.remove(k)
            te_iterate_user_data(ptr(editor), null, null)
        }
    }
}