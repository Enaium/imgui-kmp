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

// JNI bridge
// =========================================================================

internal object Jni {
    init {
        cn.enaium.imgui.NativeLoader.load()
    }

    external fun create(): Long
    external fun destroy(ptr: Long)

    // Text
    external fun setText(ptr: Long, text: String)
    external fun getText(ptr: Long): String
    external fun clearText(ptr: Long)
    external fun isEmpty(ptr: Long): Boolean
    external fun getLineCount(ptr: Long): Long
    external fun getLineText(ptr: Long, line: Long): String

    // Rendering
    external fun render(ptr: Long, title: String, sizeX: Float, sizeY: Float, childFlags: Int, windowFlags: Int): Boolean
    external fun setFocus(ptr: Long)

    // Configuration
    external fun setTabSize(ptr: Long, value: Long)
    external fun getTabSize(ptr: Long): Long
    external fun setInsertSpacesOnTabs(ptr: Long, value: Boolean)
    external fun isInsertSpacesOnTabs(ptr: Long): Boolean
    external fun setLineSpacing(ptr: Long, value: Float)
    external fun getLineSpacing(ptr: Long): Float
    external fun setWordWrapEnabled(ptr: Long, value: Boolean)
    external fun isWordWrapEnabled(ptr: Long): Boolean
    external fun setReadOnlyEnabled(ptr: Long, value: Boolean)
    external fun isReadOnlyEnabled(ptr: Long): Boolean
    external fun setCaretsVisible(ptr: Long, value: Boolean)
    external fun isCaretsVisible(ptr: Long): Boolean
    external fun setAutoIndentEnabled(ptr: Long, value: Boolean)
    external fun isAutoIndentEnabled(ptr: Long): Boolean
    external fun setShowWhitespacesEnabled(ptr: Long, value: Boolean)
    external fun isShowWhitespacesEnabled(ptr: Long): Boolean
    external fun setShowLineNumbersEnabled(ptr: Long, value: Boolean)
    external fun isShowLineNumbersEnabled(ptr: Long): Boolean
    external fun setShowMiniMapEnabled(ptr: Long, value: Boolean)
    external fun isShowMiniMapEnabled(ptr: Long): Boolean
    external fun setShowMatchingBrackets(ptr: Long, value: Boolean)
    external fun isShowingMatchingBrackets(ptr: Long): Boolean
    external fun setCompletePairedGlyphs(ptr: Long, value: Boolean)
    external fun isCompletingPairedGlyphs(ptr: Long): Boolean
    external fun setLineFoldingEnabled(ptr: Long, value: Boolean)
    external fun isLineFoldingEnabled(ptr: Long): Boolean
    external fun setOverwriteEnabled(ptr: Long, value: Boolean)
    external fun isOverwriteEnabled(ptr: Long): Boolean
    external fun setMiddleMouseScrollMode(ptr: Long)
    external fun setMiddleMousePanMode(ptr: Long)
    external fun isMiddleMousePanMode(ptr: Long): Boolean
    external fun setTextLeftMargin(ptr: Long, value: Long)
    external fun getTextLeftMargin(ptr: Long): Long

    // Language & palette
    external fun setLanguage(ptr: Long, language: Int)
    external fun getLanguageName(ptr: Long): String
    external fun getPaletteColor(ptr: Long, color: Int): Int
    external fun setPaletteColor(ptr: Long, color: Int, value: Int)
    external fun setDefaultDarkPalette(ptr: Long)
    external fun setDefaultLightPalette(ptr: Long)

    // Clipboard / undo
    external fun cut(ptr: Long)
    external fun copy(ptr: Long)
    external fun paste(ptr: Long)
    external fun undo(ptr: Long)
    external fun redo(ptr: Long)
    external fun canUndo(ptr: Long): Boolean
    external fun canRedo(ptr: Long): Boolean

    // Selection
    external fun selectAll(ptr: Long)
    external fun selectLine(ptr: Long, line: Long)
    external fun anyCursorHasSelection(ptr: Long): Boolean
    external fun getSelectedText(ptr: Long): String?

    // Find / replace
    external fun selectFirstOccurrenceOf(ptr: Long, text: String, caseSensitive: Boolean, wholeWord: Boolean)
    external fun selectNextOccurrenceOf(ptr: Long, text: String, caseSensitive: Boolean, wholeWord: Boolean)
    external fun replaceTextInCurrentCursor(ptr: Long, text: String)
    external fun replaceTextInAllCursors(ptr: Long, text: String)

    // Cursor / scrolling
    external fun getNumberOfCursors(ptr: Long): Long
    external fun getCursorText(ptr: Long, cursor: Long): String
    external fun scrollToLine(ptr: Long, line: Long, alignment: Int)
    external fun setCursorPos(ptr: Long, line: Long, index: Long)
    external fun getCursorPos(ptr: Long): LongArray
    external fun getLineHeight(ptr: Long): Float
    external fun getGlyphWidth(ptr: Long): Float

    // Markers
    external fun addMarker(ptr: Long, line: Long, lineNumberColor: Int, textColor: Int, lineNumberTooltip: String?, textTooltip: String?)
    external fun clearMarkers(ptr: Long)
    external fun hasMarkers(ptr: Long): Boolean

    // Autocomplete config
    external fun setAutoCompleteConfig(ptr: Long, activate: Boolean, triggerOnTyping: Boolean, triggerOnShortcut: Boolean, triggerInComments: Boolean, triggerInStrings: Boolean, autoInsertSingleSuggestions: Boolean, triggerDelayMs: Int, suggestionWidth: Long)

    // Additional text queries and edits
    external fun getSectionText(ptr: Long, startLine: Long, startIndex: Long, endLine: Long, endIndex: Long): String
    external fun replaceSectionText(ptr: Long, startLine: Long, startIndex: Long, endLine: Long, endIndex: Long, text: String)
    external fun selectionToLowerCase(ptr: Long)
    external fun selectionToUpperCase(ptr: Long)
    external fun stripTrailingWhitespaces(ptr: Long)
    external fun tabsToSpaces(ptr: Long)
    external fun spacesToTabs(ptr: Long)
    external fun indentLines(ptr: Long)
    external fun deindentLines(ptr: Long)
    external fun moveUpLines(ptr: Long)
    external fun moveDownLines(ptr: Long)
    external fun toggleComments(ptr: Long)

    // Additional selection / cursor API
    external fun selectLines(ptr: Long, start: Long, end: Long)
    external fun selectRegion(ptr: Long, startLine: Long, startIndex: Long, endLine: Long, endIndex: Long)
    external fun selectToBrackets(ptr: Long, includeBrackets: Boolean)
    external fun growSelections(ptr: Long)
    external fun shrinkSelections(ptr: Long)
    external fun addNextOccurrence(ptr: Long, wholeWord: Boolean)
    external fun selectAllOccurrences(ptr: Long, wholeWord: Boolean)
    external fun clearCursors(ptr: Long)
    external fun getCursorPosition(ptr: Long, cursor: Long): LongArray
    external fun getCursorSelection(ptr: Long, cursor: Long): LongArray

    // Word / find query
    external fun getWordAtMousePos(ptr: Long, x: Float, y: Float): String
    external fun findWordStart(ptr: Long, line: Long, index: Long, wholeWord: Boolean): LongArray
    external fun findWordEnd(ptr: Long, line: Long, index: Long, wholeWord: Boolean): LongArray
    external fun hasFindString(ptr: Long): Boolean
    external fun findNext(ptr: Long)
    external fun findAll(ptr: Long)
    external fun openFindReplaceWindow(ptr: Long)
    external fun closeFindReplaceWindow(ptr: Long)
    external fun setFindButtonLabel(ptr: Long, label: String)
    external fun setFindAllButtonLabel(ptr: Long, label: String)
    external fun setReplaceButtonLabel(ptr: Long, label: String)
    external fun setReplaceAllButtonLabel(ptr: Long, label: String)

    // Visibility / folding
    external fun isMousePosOverTextArea(ptr: Long, x: Float, y: Float): Boolean
    external fun isDocPosVisible(ptr: Long, line: Long, index: Long): Boolean
    external fun isLineFoldable(ptr: Long, line: Long): Boolean
    external fun isLineFolded(ptr: Long, line: Long): Boolean
    external fun isLineVisible(ptr: Long, line: Long): Boolean
    external fun isLineHidden(ptr: Long, line: Long): Boolean
    external fun foldAroundLine(ptr: Long, line: Long)
    external fun unfoldAroundLine(ptr: Long, line: Long)
    external fun toggleAtLine(ptr: Long, line: Long)
    external fun unfoldAll(ptr: Long)
    external fun getFirstVisibleRow(ptr: Long): Long
    external fun getFirstVisibleColumn(ptr: Long): Long
    external fun getLastVisibleRow(ptr: Long): Long
    external fun getLastVisibleColumn(ptr: Long): Long

    // Coordinate transforms
    external fun docPosToVisPos(ptr: Long, line: Long, index: Long): LongArray
    external fun visPosToDocPos(ptr: Long, row: Long, column: Long): LongArray

    // Undo state
    external fun getUndoIndex(ptr: Long): Long

    // Static configuration (no editor instance needed)
    external fun setDefaultPalette(text: Int, keyword: Int, number: Int, string: Int, comment: Int, background: Int, cursor: Int, selection: Int)
    external fun getDefaultPalette(): LongArray
    external fun setImGuiContext(imGuiContext: Long)

    // Remaining configuration toggles
    external fun setShowSpacesEnabled(ptr: Long, value: Boolean)
    external fun isShowSpacesEnabled(ptr: Long): Boolean
    external fun setShowTabsEnabled(ptr: Long, value: Boolean)
    external fun isShowTabsEnabled(ptr: Long): Boolean
    external fun setShowScrollbarMiniMapEnabled(ptr: Long, value: Boolean)
    external fun isShowScrollbarMiniMapEnabled(ptr: Long): Boolean
    external fun setShowPanScrollIndicatorEnabled(ptr: Long, value: Boolean)
    external fun isShowPanScrollIndicatorEnabled(ptr: Long): Boolean
    external fun setMiniMapColumns(ptr: Long, value: Long)
    external fun getMiniMapColumns(ptr: Long): Long
    external fun setLineNumberLeftMargin(ptr: Long, value: Long)
    external fun getLineNumberLeftMargin(ptr: Long): Long
    external fun setDecorationLeftMargin(ptr: Long, value: Long)
    external fun getDecorationLeftMargin(ptr: Long): Long
    external fun setLineBreakConfig(ptr: Long, breakAfter: String, breakBefore: String, useUnicodeAnnex14: Boolean)

    // Line data hooks
    external fun setInsertor(ptr: Long, activate: Boolean)
    external fun setDeletor(ptr: Long, activate: Boolean)
    external fun setUserData(ptr: Long, line: Long, data: Long)
    external fun getUserData(ptr: Long, line: Long): Long
    external fun iterateUserData(ptr: Long, activate: Boolean)
    external fun setCustomTokenizer(ptr: Long, activate: Boolean)

    // TrieAutoComplete
    external fun autocompleteCreate(): Long
    external fun autocompleteDestroy(ac: Long)
    external fun autocompleteConnect(ac: Long, editorPtr: Long)
    external fun autocompleteDisconnect(ac: Long)
    external fun autocompleteIsConnected(ac: Long): Boolean

    // Notifications
    external fun notificationsCreate(): Long
    external fun notificationsDestroy(notifications: Long)
    external fun notificationsAdd(notifications: Long, type: Int, message: String, dismissTimeMs: Int)
    external fun notificationsRender(notifications: Long, posX: Float, posY: Float)

    // TextEditor events
    external fun setTransactionCallback(ptr: Long, activate: Boolean)
    external fun setChangeCallback(ptr: Long, activate: Boolean, delayMs: Int)
    external fun setAutoCompleteSuggestions(ptr: Long, values: Array<String>)
    external fun setLineNumberContextMenuCallback(ptr: Long, activate: Boolean)
    external fun setTextContextMenuCallback(ptr: Long, activate: Boolean)
    external fun setTextHoverCallback(ptr: Long, activate: Boolean)
    external fun isMousePosOverGlyph(ptr: Long, x: Float, y: Float): Boolean
    external fun getDocPosAtMousePos(ptr: Long, x: Float, y: Float, out: LongArray)
}

// Per-editor callback registries, keyed by the raw editor pointer. Callbacks
// are stored in Kotlin and dispatched from ColorTextEditJvmBridge when the C
// trampolines fire. Entries are removed when a callback is deactivated so
// released editors do not leak.
private val autocompleteRegistries = mutableMapOf<Long, (AutocompleteState) -> AutocompleteResult>()
private val insertorRegistries = mutableMapOf<Long, (Long) -> Long>()
private val deletorRegistries = mutableMapOf<Long, (Long, Long) -> Unit>()
private val iterateRegistries = mutableMapOf<Long, (Long, Long) -> Unit>()

/**
 * Static callbacks invoked from the C trampolines in jni_bridge.cpp
 * (JVM class: cn.enaium.imgui.extensions.colortextedit.ColorTextEditJvmBridge).
 * Dispatches to the per-editor registries keyed by the editor pointer.
 */
internal object ColorTextEditJvmBridge {
    @JvmStatic
    fun notifyAutocomplete(
        editorPtr: Long,
        searchTerm: String,
        searchTermStartLine: Long,
        searchTermStartIndex: Long,
        searchTermEndLine: Long,
        searchTermEndIndex: Long,
        inIdentifier: Boolean,
        inNumber: Boolean,
        inComment: Boolean,
        inString: Boolean,
    ): AutocompleteResult {
        val callback = autocompleteRegistries[editorPtr] ?: return AutocompleteResult()
        return callback(
            AutocompleteState(
                searchTerm = searchTerm,
                searchTermStartLine = searchTermStartLine,
                searchTermStartIndex = searchTermStartIndex,
                searchTermEndLine = searchTermEndLine,
                searchTermEndIndex = searchTermEndIndex,
                inIdentifier = inIdentifier,
                inNumber = inNumber,
                inComment = inComment,
                inString = inString,
            )
        )
    }

    @JvmStatic
    fun notifyInsertor(editorPtr: Long, line: Long): Long =
        insertorRegistries[editorPtr]?.invoke(line) ?: 0L

    @JvmStatic
    fun notifyDeletor(editorPtr: Long, line: Long, data: Long) {
        deletorRegistries[editorPtr]?.invoke(line, data)
    }

    @JvmStatic
    fun notifyIterateUserData(editorPtr: Long, line: Long, data: Long) {
        iterateRegistries[editorPtr]?.invoke(line, data)
    }
}

// =========================================================================
// JVM/Android actual implementations
// =========================================================================

internal class JvmColorTextEditEditor(internal val ptr: Long) : ColorTextEditEditor {
    override fun close() {
        Jni.destroy(ptr)
    }
}

actual object ColorTextEdit {
    actual fun create(): ColorTextEditEditor {
        val ptr = Jni.create()
        require(ptr != 0L) { "te_create returned null" }
        return JvmColorTextEditEditor(ptr)
    }

    actual fun destroy(editor: ColorTextEditEditor?) {
        if (editor != null) {
            Jni.destroy((editor as JvmColorTextEditEditor).ptr)
        }
    }

    // ==================== Text ====================
    actual fun setText(editor: ColorTextEditEditor, text: String) {
        Jni.setText((editor as JvmColorTextEditEditor).ptr, text)
    }

    actual fun getText(editor: ColorTextEditEditor): String =
        Jni.getText((editor as JvmColorTextEditEditor).ptr)

    actual fun clearText(editor: ColorTextEditEditor) {
        Jni.clearText((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun isEmpty(editor: ColorTextEditEditor): Boolean =
        Jni.isEmpty((editor as JvmColorTextEditEditor).ptr)

    actual fun getLineCount(editor: ColorTextEditEditor): Long =
        Jni.getLineCount((editor as JvmColorTextEditEditor).ptr)

    actual fun getLineText(editor: ColorTextEditEditor, line: Long): String =
        Jni.getLineText((editor as JvmColorTextEditEditor).ptr, line)

    // ==================== Rendering ====================
    actual fun render(
        editor: ColorTextEditEditor,
        title: String,
        size: ImVec2,
        childFlags: Int,
        windowFlags: Int,
    ): Boolean = Jni.render((editor as JvmColorTextEditEditor).ptr, title, size.x, size.y, childFlags, windowFlags)

    actual fun setFocus(editor: ColorTextEditEditor) {
        Jni.setFocus((editor as JvmColorTextEditEditor).ptr)
    }

    // ==================== Configuration ====================
    actual fun setTabSize(editor: ColorTextEditEditor, value: Long) {
        Jni.setTabSize((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun getTabSize(editor: ColorTextEditEditor): Long =
        Jni.getTabSize((editor as JvmColorTextEditEditor).ptr)

    actual fun setInsertSpacesOnTabs(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setInsertSpacesOnTabs((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isInsertSpacesOnTabs(editor: ColorTextEditEditor): Boolean =
        Jni.isInsertSpacesOnTabs((editor as JvmColorTextEditEditor).ptr)

    actual fun setLineSpacing(editor: ColorTextEditEditor, value: Float) {
        Jni.setLineSpacing((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun getLineSpacing(editor: ColorTextEditEditor): Float =
        Jni.getLineSpacing((editor as JvmColorTextEditEditor).ptr)

    actual fun setWordWrapEnabled(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setWordWrapEnabled((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isWordWrapEnabled(editor: ColorTextEditEditor): Boolean =
        Jni.isWordWrapEnabled((editor as JvmColorTextEditEditor).ptr)

    actual fun setReadOnlyEnabled(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setReadOnlyEnabled((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isReadOnlyEnabled(editor: ColorTextEditEditor): Boolean =
        Jni.isReadOnlyEnabled((editor as JvmColorTextEditEditor).ptr)

    actual fun setCaretsVisible(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setCaretsVisible((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isCaretsVisible(editor: ColorTextEditEditor): Boolean =
        Jni.isCaretsVisible((editor as JvmColorTextEditEditor).ptr)

    actual fun setAutoIndentEnabled(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setAutoIndentEnabled((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isAutoIndentEnabled(editor: ColorTextEditEditor): Boolean =
        Jni.isAutoIndentEnabled((editor as JvmColorTextEditEditor).ptr)

    actual fun setShowWhitespacesEnabled(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setShowWhitespacesEnabled((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isShowWhitespacesEnabled(editor: ColorTextEditEditor): Boolean =
        Jni.isShowWhitespacesEnabled((editor as JvmColorTextEditEditor).ptr)

    actual fun setShowLineNumbersEnabled(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setShowLineNumbersEnabled((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isShowLineNumbersEnabled(editor: ColorTextEditEditor): Boolean =
        Jni.isShowLineNumbersEnabled((editor as JvmColorTextEditEditor).ptr)

    actual fun setShowMiniMapEnabled(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setShowMiniMapEnabled((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isShowMiniMapEnabled(editor: ColorTextEditEditor): Boolean =
        Jni.isShowMiniMapEnabled((editor as JvmColorTextEditEditor).ptr)

    actual fun setShowMatchingBrackets(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setShowMatchingBrackets((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isShowingMatchingBrackets(editor: ColorTextEditEditor): Boolean =
        Jni.isShowingMatchingBrackets((editor as JvmColorTextEditEditor).ptr)

    actual fun setCompletePairedGlyphs(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setCompletePairedGlyphs((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isCompletingPairedGlyphs(editor: ColorTextEditEditor): Boolean =
        Jni.isCompletingPairedGlyphs((editor as JvmColorTextEditEditor).ptr)

    actual fun setLineFoldingEnabled(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setLineFoldingEnabled((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isLineFoldingEnabled(editor: ColorTextEditEditor): Boolean =
        Jni.isLineFoldingEnabled((editor as JvmColorTextEditEditor).ptr)

    actual fun setOverwriteEnabled(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setOverwriteEnabled((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isOverwriteEnabled(editor: ColorTextEditEditor): Boolean =
        Jni.isOverwriteEnabled((editor as JvmColorTextEditEditor).ptr)

    actual fun setMiddleMouseScrollMode(editor: ColorTextEditEditor) {
        Jni.setMiddleMouseScrollMode((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun setMiddleMousePanMode(editor: ColorTextEditEditor) {
        Jni.setMiddleMousePanMode((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun isMiddleMousePanMode(editor: ColorTextEditEditor): Boolean =
        Jni.isMiddleMousePanMode((editor as JvmColorTextEditEditor).ptr)

    actual fun setTextLeftMargin(editor: ColorTextEditEditor, value: Long) {
        Jni.setTextLeftMargin((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun getTextLeftMargin(editor: ColorTextEditEditor): Long =
        Jni.getTextLeftMargin((editor as JvmColorTextEditEditor).ptr)

    // ==================== Language & palette ====================
    actual fun setLanguage(editor: ColorTextEditEditor, language: Int) {
        Jni.setLanguage((editor as JvmColorTextEditEditor).ptr, language)
    }

    actual fun getLanguageName(editor: ColorTextEditEditor): String =
        Jni.getLanguageName((editor as JvmColorTextEditEditor).ptr)

    actual fun getPaletteColor(editor: ColorTextEditEditor, color: Int): Int =
        Jni.getPaletteColor((editor as JvmColorTextEditEditor).ptr, color)

    actual fun setPaletteColor(editor: ColorTextEditEditor, color: Int, value: Int) {
        Jni.setPaletteColor((editor as JvmColorTextEditEditor).ptr, color, value)
    }

    actual fun setDefaultDarkPalette(editor: ColorTextEditEditor) {
        Jni.setDefaultDarkPalette((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun setDefaultLightPalette(editor: ColorTextEditEditor) {
        Jni.setDefaultLightPalette((editor as JvmColorTextEditEditor).ptr)
    }

    // ==================== Clipboard / undo ====================
    actual fun cut(editor: ColorTextEditEditor) {
        Jni.cut((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun copy(editor: ColorTextEditEditor) {
        Jni.copy((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun paste(editor: ColorTextEditEditor) {
        Jni.paste((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun undo(editor: ColorTextEditEditor) {
        Jni.undo((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun redo(editor: ColorTextEditEditor) {
        Jni.redo((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun canUndo(editor: ColorTextEditEditor): Boolean =
        Jni.canUndo((editor as JvmColorTextEditEditor).ptr)

    actual fun canRedo(editor: ColorTextEditEditor): Boolean =
        Jni.canRedo((editor as JvmColorTextEditEditor).ptr)

    // ==================== Selection ====================
    actual fun selectAll(editor: ColorTextEditEditor) {
        Jni.selectAll((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun selectLine(editor: ColorTextEditEditor, line: Long) {
        Jni.selectLine((editor as JvmColorTextEditEditor).ptr, line)
    }

    actual fun anyCursorHasSelection(editor: ColorTextEditEditor): Boolean =
        Jni.anyCursorHasSelection((editor as JvmColorTextEditEditor).ptr)

    actual fun getSelectedText(editor: ColorTextEditEditor): String? =
        Jni.getSelectedText((editor as JvmColorTextEditEditor).ptr)

    // ==================== Find / replace ====================
    actual fun selectFirstOccurrenceOf(editor: ColorTextEditEditor, text: String, caseSensitive: Boolean, wholeWord: Boolean) {
        Jni.selectFirstOccurrenceOf((editor as JvmColorTextEditEditor).ptr, text, caseSensitive, wholeWord)
    }

    actual fun selectNextOccurrenceOf(editor: ColorTextEditEditor, text: String, caseSensitive: Boolean, wholeWord: Boolean) {
        Jni.selectNextOccurrenceOf((editor as JvmColorTextEditEditor).ptr, text, caseSensitive, wholeWord)
    }

    actual fun replaceTextInCurrentCursor(editor: ColorTextEditEditor, text: String) {
        Jni.replaceTextInCurrentCursor((editor as JvmColorTextEditEditor).ptr, text)
    }

    actual fun replaceTextInAllCursors(editor: ColorTextEditEditor, text: String) {
        Jni.replaceTextInAllCursors((editor as JvmColorTextEditEditor).ptr, text)
    }

    // ==================== Cursor / scrolling ====================
    actual fun getNumberOfCursors(editor: ColorTextEditEditor): Long =
        Jni.getNumberOfCursors((editor as JvmColorTextEditEditor).ptr)

    actual fun getCursorText(editor: ColorTextEditEditor, cursor: Long): String =
        Jni.getCursorText((editor as JvmColorTextEditEditor).ptr, cursor)

    actual fun scrollToLine(editor: ColorTextEditEditor, line: Long, alignment: Int) {
        Jni.scrollToLine((editor as JvmColorTextEditEditor).ptr, line, alignment)
    }

    actual fun setCursorPos(editor: ColorTextEditEditor, line: Long, index: Long) {
        Jni.setCursorPos((editor as JvmColorTextEditEditor).ptr, line, index)
    }

    actual fun getCursorPos(editor: ColorTextEditEditor): Pair<Long, Long> {
        val pos = Jni.getCursorPos((editor as JvmColorTextEditEditor).ptr)
        return pos[0] to pos[1]
    }

    actual fun getLineHeight(editor: ColorTextEditEditor): Float =
        Jni.getLineHeight((editor as JvmColorTextEditEditor).ptr)

    actual fun getGlyphWidth(editor: ColorTextEditEditor): Float =
        Jni.getGlyphWidth((editor as JvmColorTextEditEditor).ptr)

    // ==================== Markers ====================
    actual fun addMarker(
        editor: ColorTextEditEditor,
        line: Long,
        lineNumberColor: Int,
        textColor: Int,
        lineNumberTooltip: String?,
        textTooltip: String?,
    ) {
        Jni.addMarker((editor as JvmColorTextEditEditor).ptr, line, lineNumberColor, textColor, lineNumberTooltip, textTooltip)
    }

    actual fun clearMarkers(editor: ColorTextEditEditor) {
        Jni.clearMarkers((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun hasMarkers(editor: ColorTextEditEditor): Boolean =
        Jni.hasMarkers((editor as JvmColorTextEditEditor).ptr)

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
        val ptr = (editor as JvmColorTextEditEditor).ptr
        if (onSuggestions != null) {
            autocompleteRegistries[ptr] = onSuggestions
        } else {
            autocompleteRegistries.remove(ptr)
        }
        Jni.setAutoCompleteConfig(ptr, onSuggestions != null, triggerOnTyping, triggerOnShortcut, triggerInComments, triggerInStrings, autoInsertSingleSuggestions, triggerDelayMs, suggestionWidth)
    }

    // ==================== Additional text queries and edits ====================
    actual fun getSectionText(editor: ColorTextEditEditor, startLine: Long, startIndex: Long, endLine: Long, endIndex: Long): String =
        Jni.getSectionText((editor as JvmColorTextEditEditor).ptr, startLine, startIndex, endLine, endIndex)

    actual fun replaceSectionText(editor: ColorTextEditEditor, startLine: Long, startIndex: Long, endLine: Long, endIndex: Long, text: String) {
        Jni.replaceSectionText((editor as JvmColorTextEditEditor).ptr, startLine, startIndex, endLine, endIndex, text)
    }

    actual fun selectionToLowerCase(editor: ColorTextEditEditor) {
        Jni.selectionToLowerCase((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun selectionToUpperCase(editor: ColorTextEditEditor) {
        Jni.selectionToUpperCase((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun stripTrailingWhitespaces(editor: ColorTextEditEditor) {
        Jni.stripTrailingWhitespaces((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun tabsToSpaces(editor: ColorTextEditEditor) {
        Jni.tabsToSpaces((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun spacesToTabs(editor: ColorTextEditEditor) {
        Jni.spacesToTabs((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun indentLines(editor: ColorTextEditEditor) {
        Jni.indentLines((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun deindentLines(editor: ColorTextEditEditor) {
        Jni.deindentLines((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun moveUpLines(editor: ColorTextEditEditor) {
        Jni.moveUpLines((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun moveDownLines(editor: ColorTextEditEditor) {
        Jni.moveDownLines((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun toggleComments(editor: ColorTextEditEditor) {
        Jni.toggleComments((editor as JvmColorTextEditEditor).ptr)
    }

    // ==================== Additional selection / cursor API ====================
    actual fun selectLines(editor: ColorTextEditEditor, start: Long, end: Long) {
        Jni.selectLines((editor as JvmColorTextEditEditor).ptr, start, end)
    }

    actual fun selectRegion(editor: ColorTextEditEditor, startLine: Long, startIndex: Long, endLine: Long, endIndex: Long) {
        Jni.selectRegion((editor as JvmColorTextEditEditor).ptr, startLine, startIndex, endLine, endIndex)
    }

    actual fun selectToBrackets(editor: ColorTextEditEditor, includeBrackets: Boolean) {
        Jni.selectToBrackets((editor as JvmColorTextEditEditor).ptr, includeBrackets)
    }

    actual fun growSelections(editor: ColorTextEditEditor) {
        Jni.growSelections((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun shrinkSelections(editor: ColorTextEditEditor) {
        Jni.shrinkSelections((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun addNextOccurrence(editor: ColorTextEditEditor, wholeWord: Boolean) {
        Jni.addNextOccurrence((editor as JvmColorTextEditEditor).ptr, wholeWord)
    }

    actual fun selectAllOccurrences(editor: ColorTextEditEditor, wholeWord: Boolean) {
        Jni.selectAllOccurrences((editor as JvmColorTextEditEditor).ptr, wholeWord)
    }

    actual fun clearCursors(editor: ColorTextEditEditor) {
        Jni.clearCursors((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun getCursorPosition(editor: ColorTextEditEditor, cursor: Long): Pair<Long, Long> {
        val pos = Jni.getCursorPosition((editor as JvmColorTextEditEditor).ptr, cursor)
        return Pair(pos[0], pos[1])
    }

    actual fun getCursorSelection(editor: ColorTextEditEditor, cursor: Long): LongArray =
        Jni.getCursorSelection((editor as JvmColorTextEditEditor).ptr, cursor)

    // ==================== Word / find query ====================
    actual fun getWordAtMousePos(editor: ColorTextEditEditor, x: Float, y: Float): String =
        Jni.getWordAtMousePos((editor as JvmColorTextEditEditor).ptr, x, y)

    actual fun findWordStart(editor: ColorTextEditEditor, line: Long, index: Long, wholeWord: Boolean): Pair<Long, Long> {
        val pos = Jni.findWordStart((editor as JvmColorTextEditEditor).ptr, line, index, wholeWord)
        return Pair(pos[0], pos[1])
    }

    actual fun findWordEnd(editor: ColorTextEditEditor, line: Long, index: Long, wholeWord: Boolean): Pair<Long, Long> {
        val pos = Jni.findWordEnd((editor as JvmColorTextEditEditor).ptr, line, index, wholeWord)
        return Pair(pos[0], pos[1])
    }

    actual fun hasFindString(editor: ColorTextEditEditor): Boolean =
        Jni.hasFindString((editor as JvmColorTextEditEditor).ptr)

    actual fun findNext(editor: ColorTextEditEditor) {
        Jni.findNext((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun findAll(editor: ColorTextEditEditor) {
        Jni.findAll((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun openFindReplaceWindow(editor: ColorTextEditEditor) {
        Jni.openFindReplaceWindow((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun closeFindReplaceWindow(editor: ColorTextEditEditor) {
        Jni.closeFindReplaceWindow((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun setFindButtonLabel(editor: ColorTextEditEditor, label: String) {
        Jni.setFindButtonLabel((editor as JvmColorTextEditEditor).ptr, label)
    }

    actual fun setFindAllButtonLabel(editor: ColorTextEditEditor, label: String) {
        Jni.setFindAllButtonLabel((editor as JvmColorTextEditEditor).ptr, label)
    }

    actual fun setReplaceButtonLabel(editor: ColorTextEditEditor, label: String) {
        Jni.setReplaceButtonLabel((editor as JvmColorTextEditEditor).ptr, label)
    }

    actual fun setReplaceAllButtonLabel(editor: ColorTextEditEditor, label: String) {
        Jni.setReplaceAllButtonLabel((editor as JvmColorTextEditEditor).ptr, label)
    }

    // ==================== Visibility / folding ====================
    actual fun isMousePosOverTextArea(editor: ColorTextEditEditor, x: Float, y: Float): Boolean =
        Jni.isMousePosOverTextArea((editor as JvmColorTextEditEditor).ptr, x, y)

    actual fun isDocPosVisible(editor: ColorTextEditEditor, line: Long, index: Long): Boolean =
        Jni.isDocPosVisible((editor as JvmColorTextEditEditor).ptr, line, index)

    actual fun isLineFoldable(editor: ColorTextEditEditor, line: Long): Boolean =
        Jni.isLineFoldable((editor as JvmColorTextEditEditor).ptr, line)

    actual fun isLineFolded(editor: ColorTextEditEditor, line: Long): Boolean =
        Jni.isLineFolded((editor as JvmColorTextEditEditor).ptr, line)

    actual fun isLineVisible(editor: ColorTextEditEditor, line: Long): Boolean =
        Jni.isLineVisible((editor as JvmColorTextEditEditor).ptr, line)

    actual fun isLineHidden(editor: ColorTextEditEditor, line: Long): Boolean =
        Jni.isLineHidden((editor as JvmColorTextEditEditor).ptr, line)

    actual fun foldAroundLine(editor: ColorTextEditEditor, line: Long) {
        Jni.foldAroundLine((editor as JvmColorTextEditEditor).ptr, line)
    }

    actual fun unfoldAroundLine(editor: ColorTextEditEditor, line: Long) {
        Jni.unfoldAroundLine((editor as JvmColorTextEditEditor).ptr, line)
    }

    actual fun toggleAtLine(editor: ColorTextEditEditor, line: Long) {
        Jni.toggleAtLine((editor as JvmColorTextEditEditor).ptr, line)
    }

    actual fun unfoldAll(editor: ColorTextEditEditor) {
        Jni.unfoldAll((editor as JvmColorTextEditEditor).ptr)
    }

    actual fun getFirstVisibleRow(editor: ColorTextEditEditor): Long =
        Jni.getFirstVisibleRow((editor as JvmColorTextEditEditor).ptr)

    actual fun getFirstVisibleColumn(editor: ColorTextEditEditor): Long =
        Jni.getFirstVisibleColumn((editor as JvmColorTextEditEditor).ptr)

    actual fun getLastVisibleRow(editor: ColorTextEditEditor): Long =
        Jni.getLastVisibleRow((editor as JvmColorTextEditEditor).ptr)

    actual fun getLastVisibleColumn(editor: ColorTextEditEditor): Long =
        Jni.getLastVisibleColumn((editor as JvmColorTextEditEditor).ptr)

    // ==================== Coordinate transforms ====================
    actual fun docPosToVisPos(editor: ColorTextEditEditor, line: Long, index: Long): Pair<Long, Long> {
        val pos = Jni.docPosToVisPos((editor as JvmColorTextEditEditor).ptr, line, index)
        return Pair(pos[0], pos[1])
    }

    actual fun visPosToDocPos(editor: ColorTextEditEditor, row: Long, column: Long): Pair<Long, Long> {
        val pos = Jni.visPosToDocPos((editor as JvmColorTextEditEditor).ptr, row, column)
        return Pair(pos[0], pos[1])
    }

    // ==================== Undo state ====================
    actual fun getUndoIndex(editor: ColorTextEditEditor): Long =
        Jni.getUndoIndex((editor as JvmColorTextEditEditor).ptr)

    // ==================== Static configuration ====================
    actual fun setDefaultPalette(text: Int, keyword: Int, number: Int, string: Int, comment: Int, background: Int, cursor: Int, selection: Int) {
        Jni.setDefaultPalette(text, keyword, number, string, comment, background, cursor, selection)
    }

    actual fun getDefaultPalette(): LongArray = Jni.getDefaultPalette()

    actual fun setImGuiContext(imGuiContext: Long) {
        Jni.setImGuiContext(imGuiContext)
    }

    // ==================== Remaining configuration toggles ====================
    actual fun setShowSpacesEnabled(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setShowSpacesEnabled((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isShowSpacesEnabled(editor: ColorTextEditEditor): Boolean =
        Jni.isShowSpacesEnabled((editor as JvmColorTextEditEditor).ptr)

    actual fun setShowTabsEnabled(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setShowTabsEnabled((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isShowTabsEnabled(editor: ColorTextEditEditor): Boolean =
        Jni.isShowTabsEnabled((editor as JvmColorTextEditEditor).ptr)

    actual fun setShowScrollbarMiniMapEnabled(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setShowScrollbarMiniMapEnabled((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isShowScrollbarMiniMapEnabled(editor: ColorTextEditEditor): Boolean =
        Jni.isShowScrollbarMiniMapEnabled((editor as JvmColorTextEditEditor).ptr)

    actual fun setShowPanScrollIndicatorEnabled(editor: ColorTextEditEditor, value: Boolean) {
        Jni.setShowPanScrollIndicatorEnabled((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun isShowPanScrollIndicatorEnabled(editor: ColorTextEditEditor): Boolean =
        Jni.isShowPanScrollIndicatorEnabled((editor as JvmColorTextEditEditor).ptr)

    actual fun setMiniMapColumns(editor: ColorTextEditEditor, value: Long) {
        Jni.setMiniMapColumns((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun getMiniMapColumns(editor: ColorTextEditEditor): Long =
        Jni.getMiniMapColumns((editor as JvmColorTextEditEditor).ptr)

    actual fun setLineNumberLeftMargin(editor: ColorTextEditEditor, value: Long) {
        Jni.setLineNumberLeftMargin((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun getLineNumberLeftMargin(editor: ColorTextEditEditor): Long =
        Jni.getLineNumberLeftMargin((editor as JvmColorTextEditEditor).ptr)

    actual fun setDecorationLeftMargin(editor: ColorTextEditEditor, value: Long) {
        Jni.setDecorationLeftMargin((editor as JvmColorTextEditEditor).ptr, value)
    }

    actual fun getDecorationLeftMargin(editor: ColorTextEditEditor): Long =
        Jni.getDecorationLeftMargin((editor as JvmColorTextEditEditor).ptr)

    actual fun setLineBreakConfig(editor: ColorTextEditEditor, breakAfter: String, breakBefore: String, useUnicodeAnnex14: Boolean) {
        Jni.setLineBreakConfig((editor as JvmColorTextEditEditor).ptr, breakAfter, breakBefore, useUnicodeAnnex14)
    }

    // ==================== Line data hooks ====================
    actual fun setInsertor(editor: ColorTextEditEditor, insertor: ((Long) -> Long)?) {
        val ptr = (editor as JvmColorTextEditEditor).ptr
        if (insertor != null) {
            insertorRegistries[ptr] = insertor
        } else {
            insertorRegistries.remove(ptr)
        }
        Jni.setInsertor(ptr, insertor != null)
    }

    actual fun setDeletor(editor: ColorTextEditEditor, deletor: ((Long, Long) -> Unit)?) {
        val ptr = (editor as JvmColorTextEditEditor).ptr
        if (deletor != null) {
            deletorRegistries[ptr] = deletor
        } else {
            deletorRegistries.remove(ptr)
        }
        Jni.setDeletor(ptr, deletor != null)
    }

    actual fun setUserData(editor: ColorTextEditEditor, line: Long, data: Long) {
        Jni.setUserData((editor as JvmColorTextEditEditor).ptr, line, data)
    }

    actual fun getUserData(editor: ColorTextEditEditor, line: Long): Long =
        Jni.getUserData((editor as JvmColorTextEditEditor).ptr, line)

    actual fun iterateUserData(editor: ColorTextEditEditor, iterate: ((Long, Long) -> Unit)?) {
        val ptr = (editor as JvmColorTextEditEditor).ptr
        if (iterate != null) {
            iterateRegistries[ptr] = iterate
        } else {
            iterateRegistries.remove(ptr)
        }
        Jni.iterateUserData(ptr, iterate != null)
    }

    actual fun setCustomTokenizer(editor: ColorTextEditEditor, tokenizer: ((line: Long, offset: Long, text: String) -> Int)?) {
        val ptr = (editor as JvmColorTextEditEditor).ptr
        if (tokenizer != null) {
            tokenizerRegistries[ptr] = tokenizer
        } else {
            tokenizerRegistries.remove(ptr)
            tokenizerState.remove(ptr)
        }
        Jni.setCustomTokenizer(ptr, tokenizer != null)
    }
}

// =========================================================================
// Custom tokenizer bridge (dispatched from the C trampoline)
// =========================================================================

private val tokenizerRegistries = mutableMapOf<Long, (line: Long, offset: Long, text: String) -> Int>()
private class TokenizerState { var line = -1L; var offset = 0L }

internal object LanguageTokenizerJvmBridge {
    @JvmStatic
    fun tokenize(ptr: Long, line: Long, text: String): Int {
        val cb = tokenizerRegistries[ptr] ?: return -1
        val state = tokenizerState.getOrPut(ptr) { TokenizerState() }
        if (line != state.line) {
            state.line = line
            state.offset = 0
        }
        val index = cb(line, state.offset, text)
        state.offset += text.codePointCount(0, text.length).toLong()
        return index
    }
}

private val tokenizerState = mutableMapOf<Long, TokenizerState>()