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

internal expect object Jni {
    // ---- Context / frame ----
    fun createContext(): Long
    fun destroyContext(ptr: Long)
    fun getCurrentContext(): Long
    fun setCurrentContext(ptr: Long)
    fun getIO(): Long
    fun getStyle(): Long
    fun newFrame()
    fun render()
    fun getDrawData(): Long
    fun getVersion(): String
    fun showDemoWindow(pOpen: BooleanArray?)
    fun showAboutWindow(pOpen: BooleanArray?)
    fun showMetricsWindow(pOpen: BooleanArray?)
    fun showDebugLogWindow(pOpen: BooleanArray?)
    fun showUserGuide()
    fun showIDStackToolWindow(pOpen: BooleanArray?)

    // ---- Windows ----
    fun begin(name: String, pOpen: BooleanArray?, flags: Int): Boolean
    fun end()
    fun beginChild(strId: String, sizeX: Float, sizeY: Float, childFlags: Int, windowFlags: Int): Boolean
    fun endChild()
    fun dockSpace(id: Int, sizeX: Float, sizeY: Float, flags: Int): Int
    fun setNextWindowDockID(dockId: Int, cond: Int)
    fun dockBuilderAddNode(nodeId: Int, flags: Int): Int
    fun dockBuilderRemoveNode(nodeId: Int)
    fun dockBuilderSplitNode(nodeId: Int, splitDir: Int, ratio: Float): Long
    fun dockBuilderDockWindow(windowName: String, nodeId: Int)
    fun dockBuilderFinish(nodeId: Int)
    fun setNextWindowPos(x: Float, y: Float, cond: Int, pivotX: Float, pivotY: Float)
    fun setNextWindowSize(w: Float, h: Float, cond: Int)
    fun setWindowSize(w: Float, h: Float, cond: Int)
    fun setNextWindowBgAlpha(alpha: Float)
    fun beginDisabled(disabled: Boolean)
    fun endDisabled()
    fun beginMainMenuBar(): Boolean
    fun endMainMenuBar()
    fun beginMenuBar(): Boolean
    fun endMenuBar()
    fun beginMenu(label: String, enabled: Boolean): Boolean
    fun endMenu()
    fun menuItem(label: String, shortcut: String, selected: Boolean, enabled: Boolean): Boolean
    fun beginTabBar(strId: String, flags: Int): Boolean
    fun endTabBar()
    fun beginTabItem(label: String, pOpen: BooleanArray?, flags: Int): Boolean
    fun endTabItem()
    fun beginTooltip(): Boolean
    fun endTooltip()
    fun setTooltip(text: String)
    fun openPopup(strId: String, popupFlags: Int)
    fun beginPopup(strId: String, flags: Int): Boolean
    fun beginPopupModal(name: String, pOpen: BooleanArray?, flags: Int): Boolean
    fun endPopup()
    fun closeCurrentPopup()
    fun beginPopupContextItem(strId: String?, popupFlags: Int): Boolean
    fun beginPopupContextWindow(strId: String?, popupFlags: Int): Boolean
    fun beginItemTooltip(): Boolean
    fun openPopupOnItemClick(strId: String?, popupFlags: Int)
    fun beginCombo(label: String, previewValue: String, flags: Int): Boolean
    fun endCombo()

    // ---- Drag and drop ----
    fun beginDragDropSource(flags: Int): Boolean
    fun setDragDropPayload(type: String, data: ByteArray, cond: Int): Boolean
    fun endDragDropSource()
    fun beginDragDropTarget(): Boolean
    fun acceptDragDropPayload(type: String, flags: Int): ByteArray?
    fun endDragDropTarget()
    fun getDragDropPayload(): String?

    // ---- Images ----
    fun image(texId: Long, sizeX: Float, sizeY: Float, uv0X: Float, uv0Y: Float, uv1X: Float, uv1Y: Float, tintR: Float, tintG: Float, tintB: Float, tintA: Float, borderR: Float, borderG: Float, borderB: Float, borderA: Float)
    fun imageButton(texId: Long, sizeX: Float, sizeY: Float, uv0X: Float, uv0Y: Float, uv1X: Float, uv1Y: Float, framePadding: Int, bgR: Float, bgG: Float, bgB: Float, bgA: Float, tintR: Float, tintG: Float, tintB: Float, tintA: Float): Boolean
    fun imageWithBg(texId: Long, sizeX: Float, sizeY: Float, bgR: Float, bgG: Float, bgB: Float, bgA: Float, uv0X: Float, uv0Y: Float, uv1X: Float, uv1Y: Float)

    // ---- ListBox ----
    fun beginListBox(label: String, w: Float, h: Float): Boolean
    fun endListBox()
    fun listBox(label: String, currentItem: IntArray, items: Array<String>): Boolean

    // ---- MultiSelect ----
    fun beginMultiSelect(flags: Int, selectionSize: Int, itemsCount: Int): Long
    fun endMultiSelect(): Long

    // ---- Logging ----
    fun logToClipboard(autoOpenDepth: Int)
    fun logToFile(autoOpenDepth: Int, filename: String?)
    fun logToTTY(autoOpenDepth: Int)
    fun logFinish()
    fun logText(text: String)

    // ---- .ini settings ----
    fun saveIniSettingsToDisk(iniFilename: String?)
    fun loadIniSettingsFromDisk(iniFilename: String?)
    fun saveIniSettingsToMemory(): String?
    fun loadIniSettingsFromMemory(iniData: String)

    // ---- Scissor rect / text wrapping ----
    fun pushClipRect(minX: Float, minY: Float, maxX: Float, maxY: Float, intersectWithCurrentClipRect: Boolean)
    fun popClipRect()
    fun pushTextWrapPos(wrapLocalPosX: Float)
    fun popTextWrapPos()

    // ---- Widgets ----
    fun text(text: String)
    fun textWrapped(text: String)
    fun textUnformatted(text: String)
    fun textLink(text: String): Boolean
    fun textLinkOpenURL(label: String, url: String?): Boolean
    fun textColored(r: Float, g: Float, b: Float, a: Float, text: String)
    fun textDisabled(text: String)
    fun labelText(label: String, text: String)
    fun bulletText(text: String)
    fun bullet()
    fun alignTextToFramePadding()
    fun separator()
    fun separatorText(text: String)
    fun sameLine(offsetFromStartX: Float, spacing: Float)
    fun newLine()
    fun spacing()
    fun dummy(w: Float, h: Float)
    fun indent(indentW: Float)
    fun unindent(indentW: Float)
    fun button(label: String, w: Float, h: Float): Boolean
    fun smallButton(label: String): Boolean
    fun arrowButton(strId: String, dir: Int): Boolean
    fun checkbox(label: String, v: BooleanArray): Boolean
    fun checkboxFlags(label: String, flags: IntArray, flagsValue: Int): Boolean
    fun pushItemFlag(flag: Int, enabled: Boolean)
    fun popItemFlag()
    fun shortcut(keyChord: Int, flags: Int): Boolean
    fun sliderFloat(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String): Boolean
    fun sliderInt(label: String, v: IntArray, vMin: Int, vMax: Int, format: String): Boolean
    fun dragFloat(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    fun dragFloat2(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    fun dragFloat3(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    fun dragFloat4(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    fun dragFloatRange2(label: String, vCurrentMin: FloatArray, vCurrentMax: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, formatMax: String?, flags: Int): Boolean
    fun dragInt(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    fun dragInt2(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    fun dragInt3(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    fun dragInt4(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    fun dragIntRange2(label: String, vCurrentMin: IntArray, vCurrentMax: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, formatMax: String?, flags: Int): Boolean
    fun sliderFloat2(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    fun sliderFloat3(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    fun sliderFloat4(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    fun sliderInt2(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    fun sliderInt3(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    fun sliderInt4(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    fun sliderAngle(label: String, vRad: FloatArray, vDegreesMin: Float, vDegreesMax: Float, format: String, flags: Int): Boolean
    fun vSliderFloat(label: String, w: Float, h: Float, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    fun vSliderInt(label: String, w: Float, h: Float, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    fun sliderScalar(label: String, dataType: Int, v: LongArray, vMin: LongArray, vMax: LongArray, format: String): Boolean
    fun dragScalar(label: String, dataType: Int, v: LongArray, vSpeed: Float, vMin: LongArray, vMax: LongArray, format: String): Boolean
    fun inputFloat(label: String, v: FloatArray, step: Float, stepFast: Float, format: String, flags: Int): Boolean
    fun inputFloat2(label: String, v: FloatArray, format: String, flags: Int): Boolean
    fun inputFloat3(label: String, v: FloatArray, format: String, flags: Int): Boolean
    fun inputFloat4(label: String, v: FloatArray, format: String, flags: Int): Boolean
    fun inputInt(label: String, v: IntArray, step: Int, stepFast: Int, flags: Int): Boolean
    fun inputInt2(label: String, v: IntArray, flags: Int): Boolean
    fun inputInt3(label: String, v: IntArray, flags: Int): Boolean
    fun inputInt4(label: String, v: IntArray, flags: Int): Boolean
    fun inputDouble(label: String, v: DoubleArray, step: Double, stepFast: Double, format: String, flags: Int): Boolean
    fun colorEdit3(label: String, col: FloatArray, flags: Int): Boolean
    fun colorEdit4(label: String, col: FloatArray, flags: Int): Boolean
    fun colorPicker3(label: String, col: FloatArray, flags: Int): Boolean
    fun colorPicker4(label: String, col: FloatArray, flags: Int): Boolean
    fun colorButton(descId: String, r: Float, g: Float, b: Float, a: Float, flags: Int, w: Float, h: Float): Boolean
    fun setColorEditOptions(flags: Int)
    fun colorConvertFloat4ToU32(r: Float, g: Float, b: Float, a: Float): Int
    fun colorConvertU32ToFloat4(`in`: Int): FloatArray
    fun colorConvertRGBtoHSV(r: Float, g: Float, b: Float, outH: FloatArray, outS: FloatArray, outV: FloatArray)
    fun colorConvertHSVtoRGB(h: Float, s: Float, v: Float, outR: FloatArray, outG: FloatArray, outB: FloatArray)
    fun inputText(label: String, buf: String, flags: Int): String?
    fun inputTextMultiline(label: String, buf: String, w: Float, h: Float, flags: Int): String?
    fun inputTextWithHint(label: String, hint: String, buf: String, flags: Int): String?
    fun combo(label: String, currentItem: IntArray, items: Array<String>): Boolean
    fun selectable(label: String, selected: Boolean, flags: Int, w: Float, h: Float): Boolean
    fun radioButton(label: String, active: Boolean): Boolean
    fun progressBar(fraction: Float, w: Float, h: Float, overlay: String?)
    fun collapsingHeader(label: String, flags: Int): Boolean
    fun treeNode(label: String): Boolean
    fun treeNodeEx(label: String, flags: Int): Boolean
    fun treeNodeGetOpen(label: String): Boolean
    fun treePush(strId: String?)
    fun treePop()
    fun invisibleButton(strId: String, w: Float, h: Float, flags: Int): Boolean
    fun beginGroup()
    fun endGroup()
    fun setCursorPos(x: Float, y: Float)
    fun pushId(strId: String)
    fun popId()
    fun isItemHovered(flags: Int): Boolean
    fun isItemActive(): Boolean
    fun isItemClicked(mouseButton: Int): Boolean
    fun isWindowHovered(flags: Int): Boolean
    fun isWindowFocused(flags: Int): Boolean

    // ---- State queries ----
    fun isItemFocused(): Boolean
    fun isItemVisible(): Boolean
    fun isItemEdited(): Boolean
    fun isItemActivated(): Boolean
    fun isItemDeactivated(): Boolean
    fun isItemDeactivatedAfterEdit(): Boolean
    fun isItemToggledOpen(): Boolean
    fun isItemToggledSelection(): Boolean
    fun isAnyItemHovered(): Boolean
    fun isAnyItemActive(): Boolean
    fun isAnyItemFocused(): Boolean
    fun getItemID(): Int
    fun getItemFlags(): Int
    fun getItemRectMin(): FloatArray
    fun getItemRectMax(): FloatArray
    fun getItemRectSize(): FloatArray
    fun isWindowAppearing(): Boolean
    fun isWindowCollapsed(): Boolean
    fun isRectVisible(w: Float, h: Float): Boolean
    fun isPopupOpen(strId: String, flags: Int): Boolean
    fun getWindowPos(): FloatArray
    fun getWindowSize(): FloatArray
    fun getWindowWidth(): Float
    fun getWindowHeight(): Float
    fun getWindowContentRegionMax(): FloatArray
    fun getWindowContentRegionMin(): FloatArray
    fun getWindowDrawList(): Long
    fun isKeyDown(key: Int): Boolean
    fun isKeyPressed(key: Int, repeat: Boolean): Boolean
    fun isKeyReleased(key: Int): Boolean
    fun isMouseDown(button: Int): Boolean
    fun isMouseClicked(button: Int, repeat: Boolean): Boolean
    fun isMouseReleased(button: Int): Boolean
    fun isMouseDoubleClicked(button: Int): Boolean
    fun isMouseDragging(button: Int, lockThreshold: Float): Boolean
    fun isAnyMouseDown(): Boolean
    fun isMousePosValid(hasPos: Boolean, x: Float, y: Float): Boolean
    fun getMousePos(): FloatArray
    fun getMouseDragDelta(button: Int, lockThreshold: Float): FloatArray
    fun resetMouseDragDelta(button: Int)
    fun getMouseCursor(): Int
    fun setMouseCursor(cursor: Int)
    fun setKeyboardFocusHere(offset: Int)
    fun setNextFrameWantCaptureKeyboard(wantCaptureKeyboard: Boolean)
    fun setNextFrameWantCaptureMouse(wantCaptureMouse: Boolean)
    fun setClipboardText(text: String)
    fun getClipboardText(): String?
    fun setClipboardFunctions()
    fun getTime(): Double
    fun getCursorPos(): FloatArray
    fun getCursorScreenPos(): FloatArray
    fun getCursorStartPos(): FloatArray
    fun setCursorPosX(localX: Float)
    fun setCursorScreenPos(x: Float, y: Float)
    fun getContentRegionAvail(): FloatArray
    fun getScrollX(): Float
    fun getScrollY(): Float
    fun getScrollMaxX(): Float
    fun getScrollMaxY(): Float
    fun setScrollHereX(centerXRatio: Float)
    fun setScrollHereY(centerYRatio: Float)
    fun setScrollFromPosX(localX: Float, centerXRatio: Float)
    fun setScrollFromPosY(localY: Float, centerYRatio: Float)
    fun setScrollX(scrollX: Float)
    fun setScrollY(scrollY: Float)
    fun getFrameCount(): Int
    fun getFrameHeight(): Float
    fun getFrameHeightWithSpacing(): Float
    fun getFontSize(): Float
    fun getFont(): Long
    fun getMainViewport(): Long
    fun getStyleColorVec4(idx: Int): FloatArray
    fun getCursorPosX(): Float
    fun getKeyName(key: Int): String
    fun getTextLineHeight(): Float
    fun getTextLineHeightWithSpacing(): Float
    fun getID(strId: String): Int
    fun getColorU32(idx: Int, alphaMul: Float): Int
    fun getStyleColorName(idx: Int): String
    fun calcTextSize(text: String, hideTextAfterDoubleHash: Boolean, wrapWidth: Float): FloatArray
    fun calcItemWidth(): Float

    // ---- Columns (legacy multi-column layout) ----
    fun columns(count: Int, id: String?, border: Boolean)
    fun nextColumn()
    fun getColumnIndex(): Int
    fun getColumnOffset(columnIndex: Int): Float
    fun setColumnOffset(columnIndex: Int, offsetX: Float)
    fun getColumnWidth(columnIndex: Int): Float
    fun setColumnWidth(columnIndex: Int, width: Float)
    fun getColumnsCount(): Int

    // ---- Tables ----
    fun beginTable(strId: String, column: Int, flags: Int, outerW: Float, outerH: Float, innerWidth: Float): Boolean
    fun endTable()
    fun tableNextRow(minRowHeight: Int, flags: Int)
    fun tableNextColumn(): Boolean
    fun tableSetColumnIndex(columnN: Int): Boolean
    fun tableSetupColumn(label: String, flags: Int, initWidthOrWeight: Float, userId: Int)
    fun tableSetupScrollFreeze(cols: Int, rows: Int)
    fun tableHeadersRow()
    fun tableHeader(label: String)
    fun tableAngledHeadersRow()
    fun tableGetColumnCount(): Int
    fun tableGetColumnFlags(columnN: Int): Int
    fun tableGetColumnIndex(): Int
    fun tableGetRowIndex(): Int
    fun tableGetColumnName(columnN: Int): String
    fun tableGetSortSpecs(): Long
    fun tableSetBgColor(target: Int, color: Int, columnN: Int)
    fun tabItemButton(label: String, flags: Int): Boolean

    // ---- Style ----
    fun styleColorsDark()
    fun styleColorsLight()
    fun styleColorsClassic()
    fun showStyleSelector(label: String): Boolean
    fun showFontSelector(label: String)
    fun showStyleEditor()
    fun pushStyleColorVec4(idx: Int, r: Float, g: Float, b: Float, a: Float)
    fun pushStyleColorU32(idx: Int, color: Int)
    fun popStyleColor(count: Int)
    fun pushStyleVarFloat(idx: Int, value: Float)
    fun pushStyleVarVec2(idx: Int, x: Float, y: Float)
    fun popStyleVar(count: Int)
    fun pushFont(font: Long)
    fun popFont()
    fun pushItemWidth(width: Float)
    fun popItemWidth()
    fun setNextItemWidth(width: Float)
    fun setNextItemOpen(isOpen: Boolean, cond: Int)
    fun setNextItemAllowOverlap()
    fun setNextItemSelectionUserData(selectionUserData: Long)
    fun setNextItemShortcut(keyChord: Int, flags: Int)
    fun setNextWindowCollapsed(collapsed: Boolean, cond: Int)
    fun setNextWindowContentSize(w: Float, h: Float)
    fun setNextWindowFocus()
    fun setNextWindowScroll(x: Float, y: Float)
    fun setNextWindowSizeConstraints(minW: Float, minH: Float, maxW: Float, maxH: Float)
    fun setItemTooltip(text: String)
    fun setItemDefaultFocus()
    fun setTabItemClosed(tabOrDockedWindowLabel: String)
    fun styleGetColor(style: Long, idx: Int): FloatArray
    fun styleSetColor(style: Long, idx: Int, r: Float, g: Float, b: Float, a: Float)

    // ---- IO ----
    fun ioSetDisplaySize(io: Long, w: Float, h: Float)
    fun ioSetDisplayFramebufferScale(io: Long, sx: Float, sy: Float)
    fun ioSetDeltaTime(io: Long, dt: Float)
    fun ioSetConfigFlags(io: Long, flags: Int)
    fun ioSetBackendFlags(io: Long, flags: Int)
    fun ioSetIniFilename(io: Long, path: String?)
    fun ioSetFontGlobalScale(io: Long, scale: Float)
    fun ioGetDisplaySize(io: Long): FloatArray
    fun ioGetDisplayFramebufferScale(io: Long): FloatArray
    fun ioGetDeltaTime(io: Long): Float
    fun ioGetConfigFlags(io: Long): Int
    fun ioGetBackendFlags(io: Long): Int
    fun ioGetIniFilename(io: Long): String?
    fun ioGetFontGlobalScale(io: Long): Float
    fun ioAddMousePosEvent(io: Long, x: Float, y: Float)
    fun ioAddMouseButtonEvent(io: Long, button: Int, down: Boolean)
    fun ioAddMouseWheelEvent(io: Long, x: Float, y: Float)
    fun ioAddKeyEvent(io: Long, key: Int, down: Boolean)
    fun ioAddInputCharacter(io: Long, c: Int)
    fun ioWantCaptureMouse(io: Long): Boolean
    fun ioWantCaptureKeyboard(io: Long): Boolean
    fun ioWantTextInput(io: Long): Boolean
    fun ioGetFonts(io: Long): Long

    // ---- Fonts ----
    fun fontsAddFontFromFileTTF(atlas: Long, path: String, sizePx: Float): Long
    fun fontsAddFontDefaultConfig(
        atlas: Long, name: String?, mergeMode: Boolean, pixelSnapH: Boolean,
        oversampleH: Int, oversampleV: Int, sizePixels: Float,
        glyphOffsetX: Float, glyphOffsetY: Float, glyphMinAdvanceX: Float, glyphMaxAdvanceX: Float,
        rasterizerMultiply: Float, rasterizerDensity: Float, extraSizeScale: Float,
    ): Long
    fun fontsAddFontFromFileTTFConfig(
        atlas: Long, path: String, name: String?, mergeMode: Boolean, pixelSnapH: Boolean,
        oversampleH: Int, oversampleV: Int, sizePixels: Float,
        glyphOffsetX: Float, glyphOffsetY: Float, glyphMinAdvanceX: Float, glyphMaxAdvanceX: Float,
        rasterizerMultiply: Float, rasterizerDensity: Float, extraSizeScale: Float,
    ): Long

    fun fontsAddFontFromFileTTFRanges(
        atlas: Long, path: String, name: String?, mergeMode: Boolean, pixelSnapH: Boolean,
        oversampleH: Int, oversampleV: Int, sizePixels: Float,
        glyphOffsetX: Float, glyphOffsetY: Float, glyphMinAdvanceX: Float, glyphMaxAdvanceX: Float,
        rasterizerMultiply: Float, rasterizerDensity: Float, extraSizeScale: Float,
        glyphRanges: IntArray,
    ): Long
    fun fontsAddFontDefault(atlas: Long): Long
    fun fontsBuild(atlas: Long): Boolean
    fun fontsGetTexDataAsRGBA32(atlas: Long, outDims: IntArray): ByteArray
    fun fontsSetTexId(atlas: Long, texId: Long)

    // ---- Draw data ----
    fun drawDataGetDisplayPos(data: Long): FloatArray
    fun drawDataGetDisplaySize(data: Long): FloatArray
    fun drawDataGetFramebufferScale(data: Long): FloatArray
    fun drawDataGetCmdListsCount(data: Long): Int
    fun drawDataGetCmdList(data: Long, index: Int): Long
    fun drawListGetVtxCount(list: Long): Int
    fun drawListGetIdxCount(list: Long): Int
    fun drawListGetCmdCount(list: Long): Int
    fun drawListGetCmd(list: Long, index: Int): Long
    fun drawListCopyVtx(list: Long, vtxOffset: Int, vtxCount: Int, positions: FloatArray, uvs: FloatArray, colors: IntArray)
    fun drawListCopyIdx(list: Long, idxOffset: Int, idxCount: Int, out: IntArray)
    fun drawCmdGetClipRect(cmd: Long): FloatArray
    fun drawCmdGetTexId(cmd: Long): Long
    fun drawCmdGetVtxOffset(cmd: Long): Int
    fun drawCmdGetIdxOffset(cmd: Long): Int
    fun drawCmdGetElemCount(cmd: Long): Int
    fun drawCmdHasUserCallback(cmd: Long): Boolean

    // ---- Draw list primitives ----
    fun drawListAddLine(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, col: Int, thickness: Float)
    fun drawListAddRect(list: Long, minX: Float, minY: Float, maxX: Float, maxY: Float, col: Int, rounding: Float, flags: Int, thickness: Float)
    fun drawListAddRectFilled(list: Long, minX: Float, minY: Float, maxX: Float, maxY: Float, col: Int, rounding: Float, flags: Int)
    fun drawListAddCircle(list: Long, x: Float, y: Float, radius: Float, col: Int, numSegments: Int, thickness: Float)
    fun drawListAddCircleFilled(list: Long, x: Float, y: Float, radius: Float, col: Int, numSegments: Int)
    fun drawListAddText(list: Long, x: Float, y: Float, col: Int, text: String)
    fun drawListAddQuad(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, p4X: Float, p4Y: Float, col: Int, thickness: Float)
    fun drawListAddTriangle(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, col: Int, thickness: Float)
    fun drawListAddTriangleFilled(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, col: Int)
    fun drawListAddRectFilledMultiColor(list: Long, pMinX: Float, pMinY: Float, pMaxX: Float, pMaxY: Float, colUprLeft: Int, colUprRight: Int, colBotRight: Int, colBotLeft: Int)
    fun drawListAddQuadFilled(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, p4X: Float, p4Y: Float, col: Int)
    fun drawListAddNgon(list: Long, centerX: Float, centerY: Float, radius: Float, col: Int, numSegments: Int, thickness: Float)
    fun drawListAddNgonFilled(list: Long, centerX: Float, centerY: Float, radius: Float, col: Int, numSegments: Int)
    fun drawListAddEllipse(list: Long, centerX: Float, centerY: Float, radiusX: Float, radiusY: Float, col: Int, rot: Float, numSegments: Int, thickness: Float)
    fun drawListAddEllipseFilled(list: Long, centerX: Float, centerY: Float, radiusX: Float, radiusY: Float, col: Int, rot: Float, numSegments: Int)
    fun drawListAddBezierCubic(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, p4X: Float, p4Y: Float, col: Int, thickness: Float, numSegments: Int)
    fun drawListAddBezierQuadratic(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, col: Int, thickness: Float, numSegments: Int)
    fun drawListAddConvexPolyFilled(list: Long, points: FloatArray, numPoints: Int, col: Int)
    fun drawListAddConcavePolyFilled(list: Long, points: FloatArray, numPoints: Int, col: Int)
    fun drawListAddImage(list: Long, texId: Long, pMinX: Float, pMinY: Float, pMaxX: Float, pMaxY: Float, uvMinX: Float, uvMinY: Float, uvMaxX: Float, uvMaxY: Float, col: Int)
    fun drawListAddDrawCmd(list: Long)
    fun drawListCloneOutput(list: Long): Long
    fun drawListPushClipRect(list: Long, clipRectMinX: Float, clipRectMinY: Float, clipRectMaxX: Float, clipRectMaxY: Float, intersectWithCurrentClipRect: Boolean)
    fun drawListPushClipRectFullScreen(list: Long)
    fun drawListPopClipRect(list: Long)
    fun drawListGetClipRectMin(list: Long): ImVec2
    fun drawListGetClipRectMax(list: Long): ImVec2
    fun drawListPushTextureId(list: Long, texId: Long)
    fun drawListPopTextureId(list: Long)
    fun drawListChannelsSplit(list: Long, count: Int)
    fun drawListChannelsMerge(list: Long)
    fun drawListChannelsSetCurrent(list: Long, n: Int)
    fun drawListPrimReserve(list: Long, idxCount: Int, vtxCount: Int)
    fun drawListGetVtxCurrentIdx(list: Long): Int
    fun getTexUvWhitePixel(): FloatArray
    fun drawListPrimUnreserve(list: Long, idxCount: Int, vtxCount: Int)
    fun drawListPrimRect(list: Long, aX: Float, aY: Float, bX: Float, bY: Float, col: Int)
    fun drawListPrimRectUV(list: Long, aX: Float, aY: Float, bX: Float, bY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float, col: Int)
    fun drawListPrimQuadUV(list: Long, aX: Float, aY: Float, bX: Float, bY: Float, cX: Float, cY: Float, dX: Float, dY: Float, uvAX: Float, uvAY: Float, uvBX: Float, uvBY: Float, uvCX: Float, uvCY: Float, uvDX: Float, uvDY: Float, col: Int)
    fun drawListPrimWriteVtx(list: Long, posX: Float, posY: Float, uvX: Float, uvY: Float, col: Int)
    fun drawListPrimWriteIdx(list: Long, idx: Int)
    fun drawListPrimVtx(list: Long, posX: Float, posY: Float, uvX: Float, uvY: Float, col: Int)
    fun drawListPathClear(list: Long)
    fun drawListPathLineTo(list: Long, x: Float, y: Float)
    fun drawListPathLineToMergeDuplicate(list: Long, x: Float, y: Float)
    fun drawListPathArcTo(list: Long, centerX: Float, centerY: Float, radius: Float, aMin: Float, aMax: Float, numSegments: Int)
    fun drawListPathArcToFast(list: Long, centerX: Float, centerY: Float, radius: Float, aMinOf12: Int, aMaxOf12: Int)
    fun drawListPathEllipticalArcTo(list: Long, centerX: Float, centerY: Float, radiusX: Float, radiusY: Float, rot: Float, aMin: Float, aMax: Float, numSegments: Int)
    fun drawListPathBezierCubicCurveTo(list: Long, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, p4X: Float, p4Y: Float, numSegments: Int)
    fun drawListPathBezierQuadraticCurveTo(list: Long, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, numSegments: Int)
    fun drawListPathRect(list: Long, rectMinX: Float, rectMinY: Float, rectMaxX: Float, rectMaxY: Float, rounding: Float, flags: Int)
    fun drawListPathFillConvex(list: Long, col: Int)
    fun drawListPathFillConcave(list: Long, col: Int)
    fun drawListPathStroke(list: Long, col: Int, thickness: Float, flags: Int)
    fun drawListAddPolyline(list: Long, points: FloatArray, col: Int, closed: Boolean, thickness: Float)

    // ---- Draw list getters ----
    fun getForegroundDrawList(): Long
    fun getBackgroundDrawList(): Long
}
