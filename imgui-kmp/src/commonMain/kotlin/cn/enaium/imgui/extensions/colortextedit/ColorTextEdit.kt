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

/**
 * A TextEditor instance wrapping ImGuiColorTextEdit's TextEditor;
 * close() calls [ColorTextEdit.destroy].
 */
interface ColorTextEditEditor : AutoCloseable

/**
 * Kotlin bindings for ImGuiColorTextEdit (Johan A. Goossens),
 * inside the cn.enaium.imgui.extensions.colortextedit package.
 */
expect object ColorTextEdit {
    fun create(): ColorTextEditEditor
    fun destroy(editor: ColorTextEditEditor? = null)

    // ==================== Text ====================
    fun setText(editor: ColorTextEditEditor, text: String)
    fun getText(editor: ColorTextEditEditor): String
    fun clearText(editor: ColorTextEditEditor)
    fun isEmpty(editor: ColorTextEditEditor): Boolean
    fun getLineCount(editor: ColorTextEditEditor): Long
    fun getLineText(editor: ColorTextEditEditor, line: Long): String

    // ==================== Rendering ====================
    fun render(
        editor: ColorTextEditEditor,
        title: String,
        size: ImVec2 = ImVec2(0f, 0f),
        childFlags: Int = 0,
        windowFlags: Int = 0,
    ): Boolean
    fun setFocus(editor: ColorTextEditEditor)

    // ==================== Configuration ====================
    fun setTabSize(editor: ColorTextEditEditor, value: Long)
    fun getTabSize(editor: ColorTextEditEditor): Long
    fun setInsertSpacesOnTabs(editor: ColorTextEditEditor, value: Boolean)
    fun isInsertSpacesOnTabs(editor: ColorTextEditEditor): Boolean
    fun setLineSpacing(editor: ColorTextEditEditor, value: Float)
    fun getLineSpacing(editor: ColorTextEditEditor): Float
    fun setWordWrapEnabled(editor: ColorTextEditEditor, value: Boolean)
    fun isWordWrapEnabled(editor: ColorTextEditEditor): Boolean
    fun setReadOnlyEnabled(editor: ColorTextEditEditor, value: Boolean)
    fun isReadOnlyEnabled(editor: ColorTextEditEditor): Boolean
    fun setCaretsVisible(editor: ColorTextEditEditor, value: Boolean)
    fun isCaretsVisible(editor: ColorTextEditEditor): Boolean
    fun setAutoIndentEnabled(editor: ColorTextEditEditor, value: Boolean)
    fun isAutoIndentEnabled(editor: ColorTextEditEditor): Boolean
    fun setShowWhitespacesEnabled(editor: ColorTextEditEditor, value: Boolean)
    fun isShowWhitespacesEnabled(editor: ColorTextEditEditor): Boolean
    fun setShowLineNumbersEnabled(editor: ColorTextEditEditor, value: Boolean)
    fun isShowLineNumbersEnabled(editor: ColorTextEditEditor): Boolean
    fun setShowMiniMapEnabled(editor: ColorTextEditEditor, value: Boolean)
    fun isShowMiniMapEnabled(editor: ColorTextEditEditor): Boolean
    fun setShowMatchingBrackets(editor: ColorTextEditEditor, value: Boolean)
    fun isShowingMatchingBrackets(editor: ColorTextEditEditor): Boolean
    fun setCompletePairedGlyphs(editor: ColorTextEditEditor, value: Boolean)
    fun isCompletingPairedGlyphs(editor: ColorTextEditEditor): Boolean
    fun setLineFoldingEnabled(editor: ColorTextEditEditor, value: Boolean)
    fun isLineFoldingEnabled(editor: ColorTextEditEditor): Boolean
    fun setOverwriteEnabled(editor: ColorTextEditEditor, value: Boolean)
    fun isOverwriteEnabled(editor: ColorTextEditEditor): Boolean
    fun setMiddleMouseScrollMode(editor: ColorTextEditEditor)
    fun setMiddleMousePanMode(editor: ColorTextEditEditor)
    fun isMiddleMousePanMode(editor: ColorTextEditEditor): Boolean
    fun setTextLeftMargin(editor: ColorTextEditEditor, value: Long)
    fun getTextLeftMargin(editor: ColorTextEditEditor): Long

    // ==================== Language & palette ====================
    fun setLanguage(editor: ColorTextEditEditor, language: Int)
    fun getLanguageName(editor: ColorTextEditEditor): String
    fun getPaletteColor(editor: ColorTextEditEditor, color: Int): Int
    fun setPaletteColor(editor: ColorTextEditEditor, color: Int, value: Int)
    fun setDefaultDarkPalette(editor: ColorTextEditEditor)
    fun setDefaultLightPalette(editor: ColorTextEditEditor)

    // ==================== Clipboard / undo ====================
    fun cut(editor: ColorTextEditEditor)
    fun copy(editor: ColorTextEditEditor)
    fun paste(editor: ColorTextEditEditor)
    fun undo(editor: ColorTextEditEditor)
    fun redo(editor: ColorTextEditEditor)
    fun canUndo(editor: ColorTextEditEditor): Boolean
    fun canRedo(editor: ColorTextEditEditor): Boolean

    // ==================== Selection ====================
    fun selectAll(editor: ColorTextEditEditor)
    fun selectLine(editor: ColorTextEditEditor, line: Long)
    fun anyCursorHasSelection(editor: ColorTextEditEditor): Boolean
    fun getSelectedText(editor: ColorTextEditEditor): String?

    // ==================== Find / replace ====================
    fun selectFirstOccurrenceOf(editor: ColorTextEditEditor, text: String, caseSensitive: Boolean = true, wholeWord: Boolean = false)
    fun selectNextOccurrenceOf(editor: ColorTextEditEditor, text: String, caseSensitive: Boolean = true, wholeWord: Boolean = false)
    fun replaceTextInCurrentCursor(editor: ColorTextEditEditor, text: String)
    fun replaceTextInAllCursors(editor: ColorTextEditEditor, text: String)

    // ==================== Cursor / scrolling ====================
    fun getNumberOfCursors(editor: ColorTextEditEditor): Long
    fun getCursorText(editor: ColorTextEditEditor, cursor: Long): String
    fun scrollToLine(editor: ColorTextEditEditor, line: Long, alignment: Int)
    fun setCursorPos(editor: ColorTextEditEditor, line: Long, index: Long)
    fun getCursorPos(editor: ColorTextEditEditor): Pair<Long, Long>
    fun getLineHeight(editor: ColorTextEditEditor): Float
    fun getGlyphWidth(editor: ColorTextEditEditor): Float

    // ==================== Markers ====================
    fun addMarker(editor: ColorTextEditEditor, line: Long, lineNumberColor: Int, textColor: Int, lineNumberTooltip: String? = null, textTooltip: String? = null)
    fun clearMarkers(editor: ColorTextEditEditor)
    fun hasMarkers(editor: ColorTextEditEditor): Boolean
}

// =========================================================================
// Enums (values match TextEditor.h)
// =========================================================================

object TeLanguage {
    const val C = 0
    const val CPP = 1
    const val CS = 2
    const val ANGEL_SCRIPT = 3
    const val LUA = 4
    const val PYTHON = 5
    const val GLSL = 6
    const val HLSL = 7
    const val JSON = 8
    const val MARKDOWN = 9
    const val SQL = 10
}

object TeColor {
    const val TEXT = 0
    const val KEYWORD = 1
    const val DECLARATION = 2
    const val NUMBER = 3
    const val STRING = 4
    const val PUNCTUATION = 5
    const val PREPROCESSOR = 6
    const val IDENTIFIER = 7
    const val KNOWN_IDENTIFIER = 8
    const val COMMENT = 9
    const val BACKGROUND = 10
    const val CURSOR = 11
    const val SELECTION = 12
    const val WHITESPACE = 13
    const val MATCHING_BRACKET_BACKGROUND = 14
    const val MATCHING_BRACKET_ACTIVE = 15
    const val MATCHING_BRACKET_LEVEL1 = 16
    const val MATCHING_BRACKET_LEVEL2 = 17
    const val MATCHING_BRACKET_LEVEL3 = 18
    const val MATCHING_BRACKET_ERROR = 19
    const val LINE_NUMBER = 20
    const val CURRENT_LINE_NUMBER = 21
}

object TeScrollAlign {
    const val TOP = 0
    const val MIDDLE = 1
    const val BOTTOM = 2
}