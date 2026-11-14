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

import cn.enaium.imgui.ImVec2

actual object ColorTextEdit {
    actual fun create(): ColorTextEditEditor = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun destroy(editor: ColorTextEditEditor?) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setText(editor: ColorTextEditEditor, text: String) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getText(editor: ColorTextEditEditor): String = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun clearText(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isEmpty(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun getLineCount(editor: ColorTextEditEditor): Long = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun getLineText(editor: ColorTextEditEditor, line: Long): String = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun render(editor: ColorTextEditEditor, title: String, size: ImVec2, childFlags: Int, windowFlags: Int): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setFocus(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setTabSize(editor: ColorTextEditEditor, value: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getTabSize(editor: ColorTextEditEditor): Long = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setInsertSpacesOnTabs(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isInsertSpacesOnTabs(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setLineSpacing(editor: ColorTextEditEditor, value: Float) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getLineSpacing(editor: ColorTextEditEditor): Float = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setWordWrapEnabled(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isWordWrapEnabled(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setReadOnlyEnabled(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isReadOnlyEnabled(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setCaretsVisible(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isCaretsVisible(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setAutoIndentEnabled(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isAutoIndentEnabled(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setShowWhitespacesEnabled(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isShowWhitespacesEnabled(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setShowLineNumbersEnabled(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isShowLineNumbersEnabled(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setShowMiniMapEnabled(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isShowMiniMapEnabled(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setShowMatchingBrackets(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isShowingMatchingBrackets(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setCompletePairedGlyphs(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isCompletingPairedGlyphs(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setLineFoldingEnabled(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isLineFoldingEnabled(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setOverwriteEnabled(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isOverwriteEnabled(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setMiddleMouseScrollMode(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setMiddleMousePanMode(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isMiddleMousePanMode(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setTextLeftMargin(editor: ColorTextEditEditor, value: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getTextLeftMargin(editor: ColorTextEditEditor): Long = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setLanguage(editor: ColorTextEditEditor, language: Int) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getLanguageName(editor: ColorTextEditEditor): String = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun getPaletteColor(editor: ColorTextEditEditor, color: Int): Int = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setPaletteColor(editor: ColorTextEditEditor, color: Int, value: Int) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setDefaultDarkPalette(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setDefaultLightPalette(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun cut(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun copy(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun paste(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun undo(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun redo(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun canUndo(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun canRedo(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun selectAll(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun selectLine(editor: ColorTextEditEditor, line: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun anyCursorHasSelection(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun getSelectedText(editor: ColorTextEditEditor): String? = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun selectFirstOccurrenceOf(editor: ColorTextEditEditor, text: String, caseSensitive: Boolean, wholeWord: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun selectNextOccurrenceOf(editor: ColorTextEditEditor, text: String, caseSensitive: Boolean, wholeWord: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun replaceTextInCurrentCursor(editor: ColorTextEditEditor, text: String) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun replaceTextInAllCursors(editor: ColorTextEditEditor, text: String) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getNumberOfCursors(editor: ColorTextEditEditor): Long = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun getCursorText(editor: ColorTextEditEditor, cursor: Long): String = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun scrollToLine(editor: ColorTextEditEditor, line: Long, alignment: Int) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setCursorPos(editor: ColorTextEditEditor, line: Long, index: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getCursorPos(editor: ColorTextEditEditor): Pair<Long, Long> = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun getLineHeight(editor: ColorTextEditEditor): Float = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun getGlyphWidth(editor: ColorTextEditEditor): Float = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun addMarker(editor: ColorTextEditEditor, line: Long, lineNumberColor: Int, textColor: Int, lineNumberTooltip: String?, textTooltip: String?) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun clearMarkers(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun hasMarkers(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setAutoCompleteConfig(editor: ColorTextEditEditor, onSuggestions: ((AutocompleteState) -> AutocompleteResult)?, triggerOnTyping: Boolean, triggerOnShortcut: Boolean, triggerInComments: Boolean, triggerInStrings: Boolean, autoInsertSingleSuggestions: Boolean, triggerDelayMs: Int, suggestionWidth: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getSectionText(editor: ColorTextEditEditor, startLine: Long, startIndex: Long, endLine: Long, endIndex: Long): String = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun replaceSectionText(editor: ColorTextEditEditor, startLine: Long, startIndex: Long, endLine: Long, endIndex: Long, text: String) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun selectionToLowerCase(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun selectionToUpperCase(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun stripTrailingWhitespaces(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun tabsToSpaces(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun spacesToTabs(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun indentLines(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun deindentLines(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun moveUpLines(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun moveDownLines(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun toggleComments(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun selectLines(editor: ColorTextEditEditor, start: Long, end: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun selectRegion(editor: ColorTextEditEditor, startLine: Long, startIndex: Long, endLine: Long, endIndex: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun selectToBrackets(editor: ColorTextEditEditor, includeBrackets: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun growSelections(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun shrinkSelections(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun addNextOccurrence(editor: ColorTextEditEditor, wholeWord: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun selectAllOccurrences(editor: ColorTextEditEditor, wholeWord: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun clearCursors(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getCursorPosition(editor: ColorTextEditEditor, cursor: Long): Pair<Long, Long> = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun getCursorSelection(editor: ColorTextEditEditor, cursor: Long): LongArray = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun getWordAtMousePos(editor: ColorTextEditEditor, x: Float, y: Float): String = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun findWordStart(editor: ColorTextEditEditor, line: Long, index: Long, wholeWord: Boolean): Pair<Long, Long> = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun findWordEnd(editor: ColorTextEditEditor, line: Long, index: Long, wholeWord: Boolean): Pair<Long, Long> = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun hasFindString(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun findNext(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun findAll(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun openFindReplaceWindow(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun closeFindReplaceWindow(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setFindButtonLabel(editor: ColorTextEditEditor, label: String) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setFindAllButtonLabel(editor: ColorTextEditEditor, label: String) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setReplaceButtonLabel(editor: ColorTextEditEditor, label: String) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setReplaceAllButtonLabel(editor: ColorTextEditEditor, label: String) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isMousePosOverTextArea(editor: ColorTextEditEditor, x: Float, y: Float): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun isDocPosVisible(editor: ColorTextEditEditor, line: Long, index: Long): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun isLineFoldable(editor: ColorTextEditEditor, line: Long): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun isLineFolded(editor: ColorTextEditEditor, line: Long): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun isLineVisible(editor: ColorTextEditEditor, line: Long): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun isLineHidden(editor: ColorTextEditEditor, line: Long): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun foldAroundLine(editor: ColorTextEditEditor, line: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun unfoldAroundLine(editor: ColorTextEditEditor, line: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun toggleAtLine(editor: ColorTextEditEditor, line: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun unfoldAll(editor: ColorTextEditEditor) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getFirstVisibleRow(editor: ColorTextEditEditor): Long = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun getFirstVisibleColumn(editor: ColorTextEditEditor): Long = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun getLastVisibleRow(editor: ColorTextEditEditor): Long = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun getLastVisibleColumn(editor: ColorTextEditEditor): Long = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun docPosToVisPos(editor: ColorTextEditEditor, line: Long, index: Long): Pair<Long, Long> = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun visPosToDocPos(editor: ColorTextEditEditor, row: Long, column: Long): Pair<Long, Long> = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun getUndoIndex(editor: ColorTextEditEditor): Long = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setDefaultPalette(text: Int, keyword: Int, number: Int, string: Int, comment: Int, background: Int, cursor: Int, selection: Int) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getDefaultPalette(): LongArray = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setImGuiContext(imGuiContext: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setShowSpacesEnabled(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isShowSpacesEnabled(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setShowTabsEnabled(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isShowTabsEnabled(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setShowScrollbarMiniMapEnabled(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isShowScrollbarMiniMapEnabled(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setShowPanScrollIndicatorEnabled(editor: ColorTextEditEditor, value: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun isShowPanScrollIndicatorEnabled(editor: ColorTextEditEditor): Boolean = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setMiniMapColumns(editor: ColorTextEditEditor, value: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getMiniMapColumns(editor: ColorTextEditEditor): Long = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setLineNumberLeftMargin(editor: ColorTextEditEditor, value: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getLineNumberLeftMargin(editor: ColorTextEditEditor): Long = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setDecorationLeftMargin(editor: ColorTextEditEditor, value: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getDecorationLeftMargin(editor: ColorTextEditEditor): Long = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun setLineBreakConfig(editor: ColorTextEditEditor, breakAfter: String, breakBefore: String, useUnicodeAnnex14: Boolean) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setInsertor(editor: ColorTextEditEditor, insertor: ((Long) -> Long)?) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setDeletor(editor: ColorTextEditEditor, deletor: ((Long, Long) -> Unit)?) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setUserData(editor: ColorTextEditEditor, line: Long, data: Long) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun getUserData(editor: ColorTextEditEditor, line: Long): Long = throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    actual fun iterateUserData(editor: ColorTextEditEditor, iterate: ((Long, Long) -> Unit)?) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
    actual fun setCustomTokenizer(editor: ColorTextEditEditor, tokenizer: ((line: Long, offset: Long, text: String) -> Int)?) {
        throw UnsupportedOperationException("ColorTextEdit is not available on the wasm target")
    }
}
