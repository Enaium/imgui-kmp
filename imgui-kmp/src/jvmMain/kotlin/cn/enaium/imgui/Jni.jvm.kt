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

package cn.enaium.imgui

// =========================================================================
// JNI bridge – loads the native library and provides external declarations
// =========================================================================

internal actual object Jni {
    init {
        NativeLoader.load()
    }

    // ---- Context / frame ----
    actual external fun createContext(): Long
    actual external fun destroyContext(ptr: Long)
    actual external fun getCurrentContext(): Long
    actual external fun setCurrentContext(ptr: Long)
    actual external fun getIO(): Long
    actual external fun getStyle(): Long
    actual external fun newFrame()
    actual external fun render()
    actual external fun getDrawData(): Long
    actual external fun getVersion(): String
    actual external fun showDemoWindow(pOpen: BooleanArray?)
    actual external fun showAboutWindow(pOpen: BooleanArray?)
    actual external fun showMetricsWindow(pOpen: BooleanArray?)
    actual external fun showDebugLogWindow(pOpen: BooleanArray?)
    actual external fun showUserGuide()
    actual external fun showIDStackToolWindow(pOpen: BooleanArray?)

    // ---- Windows ----
    actual external fun begin(name: String, pOpen: BooleanArray?, flags: Int): Boolean
    actual external fun end()
    actual external fun beginChild(strId: String, sizeX: Float, sizeY: Float, childFlags: Int, windowFlags: Int): Boolean
    actual external fun endChild()
    actual external fun dockSpace(id: Int, sizeX: Float, sizeY: Float, flags: Int): Int
    actual external fun setNextWindowDockID(dockId: Int, cond: Int)
    actual external fun dockBuilderAddNode(nodeId: Int, flags: Int): Int
    actual external fun dockBuilderRemoveNode(nodeId: Int)
    actual external fun dockBuilderSplitNode(nodeId: Int, splitDir: Int, ratio: Float): Long
    actual external fun dockBuilderDockWindow(windowName: String, nodeId: Int)
    actual external fun dockBuilderFinish(nodeId: Int)
    actual external fun setNextWindowPos(x: Float, y: Float, cond: Int, pivotX: Float, pivotY: Float)
    actual external fun setNextWindowSize(w: Float, h: Float, cond: Int)
    actual external fun setWindowSize(w: Float, h: Float, cond: Int)
    actual external fun setNextWindowBgAlpha(alpha: Float)
    actual external fun beginDisabled(disabled: Boolean)
    actual external fun endDisabled()
    actual external fun beginMainMenuBar(): Boolean
    actual external fun endMainMenuBar()
    actual external fun beginMenuBar(): Boolean
    actual external fun endMenuBar()
    actual external fun beginMenu(label: String, enabled: Boolean): Boolean
    actual external fun endMenu()
    actual external fun menuItem(label: String, shortcut: String, selected: Boolean, enabled: Boolean): Boolean
    actual external fun beginTabBar(strId: String, flags: Int): Boolean
    actual external fun endTabBar()
    actual external fun beginTabItem(label: String, pOpen: BooleanArray?, flags: Int): Boolean
    actual external fun endTabItem()
    actual external fun beginTooltip(): Boolean
    actual external fun endTooltip()
    actual external fun setTooltip(text: String)
    actual external fun openPopup(strId: String, popupFlags: Int)
    actual external fun beginPopup(strId: String, flags: Int): Boolean
    actual external fun beginPopupModal(name: String, pOpen: BooleanArray?, flags: Int): Boolean
    actual external fun endPopup()
    actual external fun closeCurrentPopup()
    actual external fun beginPopupContextItem(strId: String?, popupFlags: Int): Boolean
    actual external fun beginPopupContextWindow(strId: String?, popupFlags: Int): Boolean
    actual external fun beginItemTooltip(): Boolean
    actual external fun openPopupOnItemClick(strId: String?, popupFlags: Int)
    actual external fun beginCombo(label: String, previewValue: String, flags: Int): Boolean
    actual external fun endCombo()

    // ---- Drag and drop ----
    actual external fun beginDragDropSource(flags: Int): Boolean
    actual external fun setDragDropPayload(type: String, data: ByteArray, cond: Int): Boolean
    actual external fun endDragDropSource()
    actual external fun beginDragDropTarget(): Boolean
    actual external fun acceptDragDropPayload(type: String, flags: Int): ByteArray?
    actual external fun endDragDropTarget()
    actual external fun getDragDropPayload(): String?

    // ---- Images ----
    actual external fun image(texId: Long, sizeX: Float, sizeY: Float, uv0X: Float, uv0Y: Float, uv1X: Float, uv1Y: Float, tintR: Float, tintG: Float, tintB: Float, tintA: Float, borderR: Float, borderG: Float, borderB: Float, borderA: Float)
    actual external fun imageButton(texId: Long, sizeX: Float, sizeY: Float, uv0X: Float, uv0Y: Float, uv1X: Float, uv1Y: Float, framePadding: Int, bgR: Float, bgG: Float, bgB: Float, bgA: Float, tintR: Float, tintG: Float, tintB: Float, tintA: Float): Boolean
    actual external fun imageWithBg(texId: Long, sizeX: Float, sizeY: Float, bgR: Float, bgG: Float, bgB: Float, bgA: Float, uv0X: Float, uv0Y: Float, uv1X: Float, uv1Y: Float)

    // ---- ListBox ----
    actual external fun beginListBox(label: String, w: Float, h: Float): Boolean
    actual external fun endListBox()
    actual external fun listBox(label: String, currentItem: IntArray, items: Array<String>): Boolean

    // ---- MultiSelect ----
    actual external fun beginMultiSelect(flags: Int, selectionSize: Int, itemsCount: Int): Long
    actual external fun endMultiSelect(): Long

    // ---- Logging ----
    actual external fun logToClipboard(autoOpenDepth: Int)
    actual external fun logToFile(autoOpenDepth: Int, filename: String?)
    actual external fun logToTTY(autoOpenDepth: Int)
    actual external fun logFinish()
    actual external fun logText(text: String)

    // ---- .ini settings ----
    actual external fun saveIniSettingsToDisk(iniFilename: String?)
    actual external fun loadIniSettingsFromDisk(iniFilename: String?)
    actual external fun saveIniSettingsToMemory(): String?
    actual external fun loadIniSettingsFromMemory(iniData: String)

    // ---- Scissor rect / text wrapping ----
    actual external fun pushClipRect(minX: Float, minY: Float, maxX: Float, maxY: Float, intersectWithCurrentClipRect: Boolean)
    actual external fun popClipRect()
    actual external fun pushTextWrapPos(wrapLocalPosX: Float)
    actual external fun popTextWrapPos()

    // ---- Widgets ----
    actual external fun text(text: String)
    actual external fun textWrapped(text: String)
    actual external fun textUnformatted(text: String)
    actual external fun textLink(text: String): Boolean
    actual external fun textLinkOpenURL(label: String, url: String?): Boolean
    actual external fun textColored(r: Float, g: Float, b: Float, a: Float, text: String)
    actual external fun textDisabled(text: String)
    actual external fun labelText(label: String, text: String)
    actual external fun bulletText(text: String)
    actual external fun bullet()
    actual external fun alignTextToFramePadding()
    actual external fun separator()
    actual external fun separatorText(text: String)
    actual external fun sameLine(offsetFromStartX: Float, spacing: Float)
    actual external fun newLine()
    actual external fun spacing()
    actual external fun dummy(w: Float, h: Float)
    actual external fun indent(indentW: Float)
    actual external fun unindent(indentW: Float)
    actual external fun button(label: String, w: Float, h: Float): Boolean
    actual external fun smallButton(label: String): Boolean
    actual external fun arrowButton(strId: String, dir: Int): Boolean
    actual external fun checkbox(label: String, v: BooleanArray): Boolean
    actual external fun checkboxFlags(label: String, flags: IntArray, flagsValue: Int): Boolean
    actual external fun pushItemFlag(flag: Int, enabled: Boolean)
    actual external fun popItemFlag()
    actual external fun shortcut(keyChord: Int, flags: Int): Boolean
    actual external fun sliderFloat(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String): Boolean
    actual external fun sliderInt(label: String, v: IntArray, vMin: Int, vMax: Int, format: String): Boolean
    actual external fun dragFloat(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    actual external fun dragFloat2(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    actual external fun dragFloat3(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    actual external fun dragFloat4(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    actual external fun dragFloatRange2(label: String, vCurrentMin: FloatArray, vCurrentMax: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, formatMax: String?, flags: Int): Boolean
    actual external fun dragInt(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    actual external fun dragInt2(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    actual external fun dragInt3(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    actual external fun dragInt4(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    actual external fun dragIntRange2(label: String, vCurrentMin: IntArray, vCurrentMax: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, formatMax: String?, flags: Int): Boolean
    actual external fun sliderFloat2(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    actual external fun sliderFloat3(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    actual external fun sliderFloat4(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    actual external fun sliderInt2(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    actual external fun sliderInt3(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    actual external fun sliderInt4(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    actual external fun sliderAngle(label: String, vRad: FloatArray, vDegreesMin: Float, vDegreesMax: Float, format: String, flags: Int): Boolean
    actual external fun vSliderFloat(label: String, w: Float, h: Float, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    actual external fun vSliderInt(label: String, w: Float, h: Float, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    actual external fun sliderScalar(label: String, dataType: Int, v: LongArray, vMin: LongArray, vMax: LongArray, format: String): Boolean
    actual external fun dragScalar(label: String, dataType: Int, v: LongArray, vSpeed: Float, vMin: LongArray, vMax: LongArray, format: String): Boolean
    actual external fun inputFloat(label: String, v: FloatArray, step: Float, stepFast: Float, format: String, flags: Int): Boolean
    actual external fun inputFloat2(label: String, v: FloatArray, format: String, flags: Int): Boolean
    actual external fun inputFloat3(label: String, v: FloatArray, format: String, flags: Int): Boolean
    actual external fun inputFloat4(label: String, v: FloatArray, format: String, flags: Int): Boolean
    actual external fun inputInt(label: String, v: IntArray, step: Int, stepFast: Int, flags: Int): Boolean
    actual external fun inputInt2(label: String, v: IntArray, flags: Int): Boolean
    actual external fun inputInt3(label: String, v: IntArray, flags: Int): Boolean
    actual external fun inputInt4(label: String, v: IntArray, flags: Int): Boolean
    actual external fun inputDouble(label: String, v: DoubleArray, step: Double, stepFast: Double, format: String, flags: Int): Boolean
    actual external fun colorEdit3(label: String, col: FloatArray, flags: Int): Boolean
    actual external fun colorEdit4(label: String, col: FloatArray, flags: Int): Boolean
    actual external fun colorPicker3(label: String, col: FloatArray, flags: Int): Boolean
    actual external fun colorPicker4(label: String, col: FloatArray, flags: Int): Boolean
    actual external fun colorButton(descId: String, r: Float, g: Float, b: Float, a: Float, flags: Int, w: Float, h: Float): Boolean
    actual external fun setColorEditOptions(flags: Int)
    actual external fun colorConvertFloat4ToU32(r: Float, g: Float, b: Float, a: Float): Int
    actual external fun colorConvertU32ToFloat4(`in`: Int): FloatArray
    actual external fun colorConvertRGBtoHSV(r: Float, g: Float, b: Float, outH: FloatArray, outS: FloatArray, outV: FloatArray)
    actual external fun colorConvertHSVtoRGB(h: Float, s: Float, v: Float, outR: FloatArray, outG: FloatArray, outB: FloatArray)
    actual external fun inputText(label: String, buf: String, flags: Int): String?
    actual external fun inputTextMultiline(label: String, buf: String, w: Float, h: Float, flags: Int): String?
    actual external fun inputTextWithHint(label: String, hint: String, buf: String, flags: Int): String?
    actual external fun combo(label: String, currentItem: IntArray, items: Array<String>): Boolean
    actual external fun selectable(label: String, selected: Boolean, flags: Int, w: Float, h: Float): Boolean
    actual external fun radioButton(label: String, active: Boolean): Boolean
    actual external fun progressBar(fraction: Float, w: Float, h: Float, overlay: String?)
    actual external fun collapsingHeader(label: String, flags: Int): Boolean
    actual external fun treeNode(label: String): Boolean
    actual external fun treeNodeEx(label: String, flags: Int): Boolean
    actual external fun treeNodeGetOpen(label: String): Boolean
    actual external fun treePush(strId: String?)
    actual external fun treePop()
    actual external fun invisibleButton(strId: String, w: Float, h: Float, flags: Int): Boolean
    actual external fun beginGroup()
    actual external fun endGroup()
    actual external fun setCursorPos(x: Float, y: Float)
    actual external fun pushId(strId: String)
    actual external fun popId()
    actual external fun isItemHovered(flags: Int): Boolean
    actual external fun isItemActive(): Boolean
    actual external fun isItemClicked(mouseButton: Int): Boolean
    actual external fun isWindowHovered(flags: Int): Boolean
    actual external fun isWindowFocused(flags: Int): Boolean

    // ---- State queries ----
    actual external fun isItemFocused(): Boolean
    actual external fun isItemVisible(): Boolean
    actual external fun isItemEdited(): Boolean
    actual external fun isItemActivated(): Boolean
    actual external fun isItemDeactivated(): Boolean
    actual external fun isItemDeactivatedAfterEdit(): Boolean
    actual external fun isItemToggledOpen(): Boolean
    actual external fun isItemToggledSelection(): Boolean
    actual external fun isAnyItemHovered(): Boolean
    actual external fun isAnyItemActive(): Boolean
    actual external fun isAnyItemFocused(): Boolean
    actual external fun getItemID(): Int
    actual external fun getItemFlags(): Int
    actual external fun getItemRectMin(): FloatArray
    actual external fun getItemRectMax(): FloatArray
    actual external fun getItemRectSize(): FloatArray
    actual external fun isWindowAppearing(): Boolean
    actual external fun isWindowCollapsed(): Boolean
    actual external fun isRectVisible(w: Float, h: Float): Boolean
    actual external fun isPopupOpen(strId: String, flags: Int): Boolean
    actual external fun getWindowPos(): FloatArray
    actual external fun getWindowSize(): FloatArray
    actual external fun getWindowWidth(): Float
    actual external fun getWindowHeight(): Float
    actual external fun getWindowContentRegionMax(): FloatArray
    actual external fun getWindowContentRegionMin(): FloatArray
    actual external fun getWindowDrawList(): Long
    actual external fun isKeyDown(key: Int): Boolean
    actual external fun isKeyPressed(key: Int, repeat: Boolean): Boolean
    actual external fun isKeyReleased(key: Int): Boolean
    actual external fun isMouseDown(button: Int): Boolean
    actual external fun isMouseClicked(button: Int, repeat: Boolean): Boolean
    actual external fun isMouseReleased(button: Int): Boolean
    actual external fun isMouseDoubleClicked(button: Int): Boolean
    actual external fun isMouseDragging(button: Int, lockThreshold: Float): Boolean
    actual external fun isAnyMouseDown(): Boolean
    actual external fun isMousePosValid(hasPos: Boolean, x: Float, y: Float): Boolean
    actual external fun getMousePos(): FloatArray
    actual external fun getMouseDragDelta(button: Int, lockThreshold: Float): FloatArray
    actual external fun resetMouseDragDelta(button: Int)
    actual external fun getMouseCursor(): Int
    actual external fun setMouseCursor(cursor: Int)
    actual external fun setKeyboardFocusHere(offset: Int)
    actual external fun setNextFrameWantCaptureKeyboard(wantCaptureKeyboard: Boolean)
    actual external fun setNextFrameWantCaptureMouse(wantCaptureMouse: Boolean)
    actual external fun setClipboardText(text: String)
    actual external fun getClipboardText(): String?
    actual external fun setClipboardFunctions()
    actual external fun getTime(): Double
    actual external fun getCursorPos(): FloatArray
    actual external fun getCursorScreenPos(): FloatArray
    actual external fun getCursorStartPos(): FloatArray
    actual external fun setCursorPosX(localX: Float)
    actual external fun setCursorScreenPos(x: Float, y: Float)
    actual external fun getContentRegionAvail(): FloatArray
    actual external fun getScrollX(): Float
    actual external fun getScrollY(): Float
    actual external fun getScrollMaxX(): Float
    actual external fun getScrollMaxY(): Float
    actual external fun setScrollHereX(centerXRatio: Float)
    actual external fun setScrollHereY(centerYRatio: Float)
    actual external fun setScrollFromPosX(localX: Float, centerXRatio: Float)
    actual external fun setScrollFromPosY(localY: Float, centerYRatio: Float)
    actual external fun setScrollX(scrollX: Float)
    actual external fun setScrollY(scrollY: Float)
    actual external fun getFrameCount(): Int
    actual external fun getFrameHeight(): Float
    actual external fun getFrameHeightWithSpacing(): Float
    actual external fun getFontSize(): Float
    actual external fun getFont(): Long
    actual external fun getMainViewport(): Long
    actual external fun getStyleColorVec4(idx: Int): FloatArray
    actual external fun getCursorPosX(): Float
    actual external fun getKeyName(key: Int): String
    actual external fun getTextLineHeight(): Float
    actual external fun getTextLineHeightWithSpacing(): Float
    actual external fun getID(strId: String): Int
    actual external fun getColorU32(idx: Int, alphaMul: Float): Int
    actual external fun getStyleColorName(idx: Int): String
    actual external fun calcTextSize(text: String, hideTextAfterDoubleHash: Boolean, wrapWidth: Float): FloatArray
    actual external fun calcItemWidth(): Float

    // ---- Columns (legacy multi-column layout) ----
    actual external fun columns(count: Int, id: String?, border: Boolean)
    actual external fun nextColumn()
    actual external fun getColumnIndex(): Int
    actual external fun getColumnOffset(columnIndex: Int): Float
    actual external fun setColumnOffset(columnIndex: Int, offsetX: Float)
    actual external fun getColumnWidth(columnIndex: Int): Float
    actual external fun setColumnWidth(columnIndex: Int, width: Float)
    actual external fun getColumnsCount(): Int

    // ---- Tables ----
    actual external fun beginTable(strId: String, column: Int, flags: Int, outerW: Float, outerH: Float, innerWidth: Float): Boolean
    actual external fun endTable()
    actual external fun tableNextRow(minRowHeight: Int, flags: Int)
    actual external fun tableNextColumn(): Boolean
    actual external fun tableSetColumnIndex(columnN: Int): Boolean
    actual external fun tableSetupColumn(label: String, flags: Int, initWidthOrWeight: Float, userId: Int)
    actual external fun tableSetupScrollFreeze(cols: Int, rows: Int)
    actual external fun tableHeadersRow()
    actual external fun tableHeader(label: String)
    actual external fun tableAngledHeadersRow()
    actual external fun tableGetColumnCount(): Int
    actual external fun tableGetColumnFlags(columnN: Int): Int
    actual external fun tableGetColumnIndex(): Int
    actual external fun tableGetRowIndex(): Int
    actual external fun tableGetColumnName(columnN: Int): String
    actual external fun tableGetSortSpecs(): Long
    actual external fun tableSetBgColor(target: Int, color: Int, columnN: Int)
    actual external fun tabItemButton(label: String, flags: Int): Boolean

    // ---- Style ----
    actual external fun styleColorsDark()
    actual external fun styleColorsLight()
    actual external fun styleColorsClassic()
    actual external fun showStyleSelector(label: String): Boolean
    actual external fun showFontSelector(label: String)
    actual external fun showStyleEditor()
    actual external fun pushStyleColorVec4(idx: Int, r: Float, g: Float, b: Float, a: Float)
    actual external fun pushStyleColorU32(idx: Int, color: Int)
    actual external fun popStyleColor(count: Int)
    actual external fun pushStyleVarFloat(idx: Int, value: Float)
    actual external fun pushStyleVarVec2(idx: Int, x: Float, y: Float)
    actual external fun popStyleVar(count: Int)
    actual external fun pushFont(font: Long)
    actual external fun popFont()
    actual external fun pushItemWidth(width: Float)
    actual external fun popItemWidth()
    actual external fun setNextItemWidth(width: Float)
    actual external fun setNextItemOpen(isOpen: Boolean, cond: Int)
    actual external fun setNextItemAllowOverlap()
    actual external fun setNextItemSelectionUserData(selectionUserData: Long)
    actual external fun setNextItemShortcut(keyChord: Int, flags: Int)
    actual external fun setNextWindowCollapsed(collapsed: Boolean, cond: Int)
    actual external fun setNextWindowContentSize(w: Float, h: Float)
    actual external fun setNextWindowFocus()
    actual external fun setNextWindowScroll(x: Float, y: Float)
    actual external fun setNextWindowSizeConstraints(minW: Float, minH: Float, maxW: Float, maxH: Float)
    actual external fun setItemTooltip(text: String)
    actual external fun setItemDefaultFocus()
    actual external fun setTabItemClosed(tabOrDockedWindowLabel: String)
    actual external fun styleGetColor(style: Long, idx: Int): FloatArray
    actual external fun styleSetColor(style: Long, idx: Int, r: Float, g: Float, b: Float, a: Float)

    // ---- IO ----
    actual external fun ioSetDisplaySize(io: Long, w: Float, h: Float)
    actual external fun ioSetDisplayFramebufferScale(io: Long, sx: Float, sy: Float)
    actual external fun ioSetDeltaTime(io: Long, dt: Float)
    actual external fun ioSetConfigFlags(io: Long, flags: Int)
    actual external fun ioSetBackendFlags(io: Long, flags: Int)
    actual external fun ioSetIniFilename(io: Long, path: String?)
    actual external fun ioSetFontGlobalScale(io: Long, scale: Float)
    actual external fun ioGetDisplaySize(io: Long): FloatArray
    actual external fun ioGetDisplayFramebufferScale(io: Long): FloatArray
    actual external fun ioGetDeltaTime(io: Long): Float
    actual external fun ioGetConfigFlags(io: Long): Int
    actual external fun ioGetBackendFlags(io: Long): Int
    actual external fun ioGetIniFilename(io: Long): String?
    actual external fun ioGetFontGlobalScale(io: Long): Float
    actual external fun ioAddMousePosEvent(io: Long, x: Float, y: Float)
    actual external fun ioAddMouseButtonEvent(io: Long, button: Int, down: Boolean)
    actual external fun ioAddMouseWheelEvent(io: Long, x: Float, y: Float)
    actual external fun ioAddKeyEvent(io: Long, key: Int, down: Boolean)
    actual external fun ioAddInputCharacter(io: Long, c: Int)
    actual external fun ioWantCaptureMouse(io: Long): Boolean
    actual external fun ioWantCaptureKeyboard(io: Long): Boolean
    actual external fun ioWantTextInput(io: Long): Boolean
    actual external fun ioGetFonts(io: Long): Long

    // ---- Fonts ----
    actual external fun fontsAddFontFromFileTTF(atlas: Long, path: String, sizePx: Float): Long
    actual external fun fontsAddFontDefaultConfig(
        atlas: Long, name: String?, mergeMode: Boolean, pixelSnapH: Boolean,
        oversampleH: Int, oversampleV: Int, sizePixels: Float,
        glyphOffsetX: Float, glyphOffsetY: Float, glyphMinAdvanceX: Float, glyphMaxAdvanceX: Float,
        rasterizerMultiply: Float, rasterizerDensity: Float, extraSizeScale: Float,
    ): Long
    actual external fun fontsAddFontFromFileTTFConfig(
        atlas: Long, path: String, name: String?, mergeMode: Boolean, pixelSnapH: Boolean,
        oversampleH: Int, oversampleV: Int, sizePixels: Float,
        glyphOffsetX: Float, glyphOffsetY: Float, glyphMinAdvanceX: Float, glyphMaxAdvanceX: Float,
        rasterizerMultiply: Float, rasterizerDensity: Float, extraSizeScale: Float,
    ): Long

    actual external fun fontsAddFontFromFileTTFRanges(
        atlas: Long, path: String, name: String?, mergeMode: Boolean, pixelSnapH: Boolean,
        oversampleH: Int, oversampleV: Int, sizePixels: Float,
        glyphOffsetX: Float, glyphOffsetY: Float, glyphMinAdvanceX: Float, glyphMaxAdvanceX: Float,
        rasterizerMultiply: Float, rasterizerDensity: Float, extraSizeScale: Float,
        glyphRanges: IntArray,
    ): Long
    actual external fun fontsAddFontDefault(atlas: Long): Long
    actual external fun fontsBuild(atlas: Long): Boolean
    actual external fun fontsGetTexDataAsRGBA32(atlas: Long, outDims: IntArray): ByteArray
    actual external fun fontsSetTexId(atlas: Long, texId: Long)

    // ---- Draw data ----
    actual external fun drawDataGetDisplayPos(data: Long): FloatArray
    actual external fun drawDataGetDisplaySize(data: Long): FloatArray
    actual external fun drawDataGetFramebufferScale(data: Long): FloatArray
    actual external fun drawDataGetCmdListsCount(data: Long): Int
    actual external fun drawDataGetCmdList(data: Long, index: Int): Long
    actual external fun drawListGetVtxCount(list: Long): Int
    actual external fun drawListGetIdxCount(list: Long): Int
    actual external fun drawListGetCmdCount(list: Long): Int
    actual external fun drawListGetCmd(list: Long, index: Int): Long
    actual external fun drawListCopyVtx(list: Long, vtxOffset: Int, vtxCount: Int, positions: FloatArray, uvs: FloatArray, colors: IntArray)
    actual external fun drawListCopyIdx(list: Long, idxOffset: Int, idxCount: Int, out: IntArray)
    actual external fun drawCmdGetClipRect(cmd: Long): FloatArray
    actual external fun drawCmdGetTexId(cmd: Long): Long
    actual external fun drawCmdGetVtxOffset(cmd: Long): Int
    actual external fun drawCmdGetIdxOffset(cmd: Long): Int
    actual external fun drawCmdGetElemCount(cmd: Long): Int
    actual external fun drawCmdHasUserCallback(cmd: Long): Boolean

    // ---- Draw list primitives ----
    actual external fun drawListAddLine(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, col: Int, thickness: Float)
    actual external fun drawListAddRect(list: Long, minX: Float, minY: Float, maxX: Float, maxY: Float, col: Int, rounding: Float, flags: Int, thickness: Float)
    actual external fun drawListAddRectFilled(list: Long, minX: Float, minY: Float, maxX: Float, maxY: Float, col: Int, rounding: Float, flags: Int)
    actual external fun drawListAddCircle(list: Long, x: Float, y: Float, radius: Float, col: Int, numSegments: Int, thickness: Float)
    actual external fun drawListAddCircleFilled(list: Long, x: Float, y: Float, radius: Float, col: Int, numSegments: Int)
    actual external fun drawListAddText(list: Long, x: Float, y: Float, col: Int, text: String)
    actual external fun drawListAddQuad(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, p4X: Float, p4Y: Float, col: Int, thickness: Float)
    actual external fun drawListAddTriangle(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, col: Int, thickness: Float)
    actual external fun drawListAddTriangleFilled(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, col: Int)
    actual external fun drawListAddRectFilledMultiColor(list: Long, pMinX: Float, pMinY: Float, pMaxX: Float, pMaxY: Float, colUprLeft: Int, colUprRight: Int, colBotRight: Int, colBotLeft: Int)
    actual external fun drawListAddQuadFilled(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, p4X: Float, p4Y: Float, col: Int)
    actual external fun drawListAddNgon(list: Long, centerX: Float, centerY: Float, radius: Float, col: Int, numSegments: Int, thickness: Float)
    actual external fun drawListAddNgonFilled(list: Long, centerX: Float, centerY: Float, radius: Float, col: Int, numSegments: Int)
    actual external fun drawListAddEllipse(list: Long, centerX: Float, centerY: Float, radiusX: Float, radiusY: Float, col: Int, rot: Float, numSegments: Int, thickness: Float)
    actual external fun drawListAddEllipseFilled(list: Long, centerX: Float, centerY: Float, radiusX: Float, radiusY: Float, col: Int, rot: Float, numSegments: Int)
    actual external fun drawListAddBezierCubic(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, p4X: Float, p4Y: Float, col: Int, thickness: Float, numSegments: Int)
    actual external fun drawListAddBezierQuadratic(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, col: Int, thickness: Float, numSegments: Int)
    actual external fun drawListAddConvexPolyFilled(list: Long, points: FloatArray, numPoints: Int, col: Int)
    actual external fun drawListAddConcavePolyFilled(list: Long, points: FloatArray, numPoints: Int, col: Int)
    actual external fun drawListAddImage(list: Long, texId: Long, pMinX: Float, pMinY: Float, pMaxX: Float, pMaxY: Float, uvMinX: Float, uvMinY: Float, uvMaxX: Float, uvMaxY: Float, col: Int)
    actual external fun drawListAddDrawCmd(list: Long)
    actual external fun drawListCloneOutput(list: Long): Long
    actual external fun drawListPushClipRect(list: Long, clipRectMinX: Float, clipRectMinY: Float, clipRectMaxX: Float, clipRectMaxY: Float, intersectWithCurrentClipRect: Boolean)
    actual external fun drawListPushClipRectFullScreen(list: Long)
    actual external fun drawListPopClipRect(list: Long)
    actual external fun drawListGetClipRectMin(list: Long): ImVec2
    actual external fun drawListGetClipRectMax(list: Long): ImVec2
    actual external fun drawListPushTextureId(list: Long, texId: Long)
    actual external fun drawListPopTextureId(list: Long)
    actual external fun drawListChannelsSplit(list: Long, count: Int)
    actual external fun drawListChannelsMerge(list: Long)
    actual external fun drawListChannelsSetCurrent(list: Long, n: Int)
    actual external fun drawListPrimReserve(list: Long, idxCount: Int, vtxCount: Int)
    actual external fun drawListGetVtxCurrentIdx(list: Long): Int
    actual external fun getTexUvWhitePixel(): FloatArray
    actual external fun drawListPrimUnreserve(list: Long, idxCount: Int, vtxCount: Int)
    actual external fun drawListPrimRect(list: Long, aX: Float, aY: Float, bX: Float, bY: Float, col: Int)
    actual external fun drawListPrimRectUV(list: Long, aX: Float, aY: Float, bX: Float, bY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float, col: Int)
    actual external fun drawListPrimQuadUV(list: Long, aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, dX: Float, dY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float, uvCX: Float, uvCY: Float, uvDX: Float, uvDY: Float, col: Int)
    actual external fun drawListPrimWriteVtx(list: Long, posX: Float, posY: Float, uvX: Float, uvY: Float, col: Int)
    actual external fun drawListPrimWriteIdx(list: Long, idx: Int)
    actual external fun drawListPrimVtx(list: Long, posX: Float, posY: Float, uvX: Float, uvY: Float, col: Int)
    actual external fun drawListPathClear(list: Long)
    actual external fun drawListPathLineTo(list: Long, x: Float, y: Float)
    actual external fun drawListPathLineToMergeDuplicate(list: Long, x: Float, y: Float)
    actual external fun drawListPathArcTo(list: Long, centerX: Float, centerY: Float, radius: Float, aMin: Float, aMax: Float, numSegments: Int)
    actual external fun drawListPathArcToFast(list: Long, centerX: Float, centerY: Float, radius: Float, aMinOf12: Int, aMaxOf12: Int)
    actual external fun drawListPathEllipticalArcTo(list: Long, centerX: Float, centerY: Float, radiusX: Float, radiusY: Float, rot: Float, aMin: Float, aMax: Float, numSegments: Int)
    actual external fun drawListPathBezierCubicCurveTo(list: Long, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, p4X: Float, p4Y: Float, numSegments: Int)
    actual external fun drawListPathBezierQuadraticCurveTo(list: Long, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, numSegments: Int)
    actual external fun drawListPathRect(list: Long, rectMinX: Float, rectMinY: Float, rectMaxX: Float, rectMaxY: Float, rounding: Float, flags: Int)
    actual external fun drawListPathFillConvex(list: Long, col: Int)
    actual external fun drawListPathFillConcave(list: Long, col: Int)
    actual external fun drawListPathStroke(list: Long, col: Int, thickness: Float, flags: Int)
    actual external fun drawListAddPolyline(list: Long, points: FloatArray, col: Int, closed: Boolean, thickness: Float)

    // ---- Draw list getters ----
    actual external fun getForegroundDrawList(): Long
    actual external fun getBackgroundDrawList(): Long
}
