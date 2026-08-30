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
}