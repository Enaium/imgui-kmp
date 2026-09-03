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

internal object Jni {
    init {
        NativeLoader.load()
    }

    // ---- Context / frame ----
    external fun createContext(): Long
    external fun destroyContext(ptr: Long)
    external fun getCurrentContext(): Long
    external fun setCurrentContext(ptr: Long)
    external fun getIO(): Long
    external fun getStyle(): Long
    external fun newFrame()
    external fun render()
    external fun getDrawData(): Long
    external fun getVersion(): String
    external fun showDemoWindow(pOpen: BooleanArray?)
    external fun showAboutWindow(pOpen: BooleanArray?)
    external fun showMetricsWindow(pOpen: BooleanArray?)
    external fun showDebugLogWindow(pOpen: BooleanArray?)
    external fun showUserGuide()
    external fun showIDStackToolWindow(pOpen: BooleanArray?)

    // ---- Windows ----
    external fun begin(name: String, pOpen: BooleanArray?, flags: Int): Boolean
    external fun end()
    external fun beginChild(strId: String, sizeX: Float, sizeY: Float, childFlags: Int, windowFlags: Int): Boolean
    external fun endChild()
    external fun dockSpace(id: Int, sizeX: Float, sizeY: Float, flags: Int): Int
    external fun setNextWindowDockID(dockId: Int, cond: Int)
    external fun dockBuilderAddNode(nodeId: Int, flags: Int): Int
    external fun dockBuilderRemoveNode(nodeId: Int)
    external fun dockBuilderSplitNode(nodeId: Int, splitDir: Int, ratio: Float): Long
    external fun dockBuilderDockWindow(windowName: String, nodeId: Int)
    external fun dockBuilderFinish(nodeId: Int)
    external fun setNextWindowPos(x: Float, y: Float, cond: Int, pivotX: Float, pivotY: Float)
    external fun setNextWindowSize(w: Float, h: Float, cond: Int)
    external fun setWindowSize(w: Float, h: Float, cond: Int)
    external fun setNextWindowBgAlpha(alpha: Float)
    external fun beginDisabled(disabled: Boolean)
    external fun endDisabled()
    external fun beginMainMenuBar(): Boolean
    external fun endMainMenuBar()
    external fun beginMenuBar(): Boolean
    external fun endMenuBar()
    external fun beginMenu(label: String, enabled: Boolean): Boolean
    external fun endMenu()
    external fun menuItem(label: String, shortcut: String, selected: Boolean, enabled: Boolean): Boolean
    external fun beginTabBar(strId: String, flags: Int): Boolean
    external fun endTabBar()
    external fun beginTabItem(label: String, pOpen: BooleanArray?, flags: Int): Boolean
    external fun endTabItem()
    external fun beginTooltip(): Boolean
    external fun endTooltip()
    external fun setTooltip(text: String)
    external fun openPopup(strId: String, popupFlags: Int)
    external fun beginPopup(strId: String, flags: Int): Boolean
    external fun beginPopupModal(name: String, pOpen: BooleanArray?, flags: Int): Boolean
    external fun endPopup()
    external fun closeCurrentPopup()
    external fun beginPopupContextItem(strId: String?, popupFlags: Int): Boolean
    external fun beginPopupContextWindow(strId: String?, popupFlags: Int): Boolean
    external fun beginItemTooltip(): Boolean
    external fun openPopupOnItemClick(strId: String?, popupFlags: Int)
    external fun beginCombo(label: String, previewValue: String, flags: Int): Boolean
    external fun endCombo()

    // ---- Drag and drop ----
    external fun beginDragDropSource(flags: Int): Boolean
    external fun setDragDropPayload(type: String, data: ByteArray, cond: Int): Boolean
    external fun endDragDropSource()
    external fun beginDragDropTarget(): Boolean
    external fun acceptDragDropPayload(type: String, flags: Int): ByteArray?
    external fun endDragDropTarget()
    external fun getDragDropPayload(): String?

    // ---- Images ----
    external fun image(texId: Long, sizeX: Float, sizeY: Float, uv0X: Float, uv0Y: Float, uv1X: Float, uv1Y: Float, tintR: Float, tintG: Float, tintB: Float, tintA: Float, borderR: Float, borderG: Float, borderB: Float, borderA: Float)
    external fun imageButton(texId: Long, sizeX: Float, sizeY: Float, uv0X: Float, uv0Y: Float, uv1X: Float, uv1Y: Float, framePadding: Int, bgR: Float, bgG: Float, bgB: Float, bgA: Float, tintR: Float, tintG: Float, tintB: Float, tintA: Float): Boolean
    external fun imageWithBg(texId: Long, sizeX: Float, sizeY: Float, bgR: Float, bgG: Float, bgB: Float, bgA: Float, uv0X: Float, uv0Y: Float, uv1X: Float, uv1Y: Float)

    // ---- ListBox ----
    external fun beginListBox(label: String, w: Float, h: Float): Boolean
    external fun endListBox()
    external fun listBox(label: String, currentItem: IntArray, items: Array<String>): Boolean

    // ---- MultiSelect ----
    external fun beginMultiSelect(flags: Int, selectionSize: Int, itemsCount: Int): Long
    external fun endMultiSelect(): Long

    // ---- Logging ----
    external fun logToClipboard(autoOpenDepth: Int)
    external fun logToFile(autoOpenDepth: Int, filename: String?)
    external fun logToTTY(autoOpenDepth: Int)
    external fun logFinish()
    external fun logText(text: String)

    // ---- .ini settings ----
    external fun saveIniSettingsToDisk(iniFilename: String?)
    external fun loadIniSettingsFromDisk(iniFilename: String?)
    external fun saveIniSettingsToMemory(): String?
    external fun loadIniSettingsFromMemory(iniData: String)

    // ---- Scissor rect / text wrapping ----
    external fun pushClipRect(minX: Float, minY: Float, maxX: Float, maxY: Float, intersectWithCurrentClipRect: Boolean)
    external fun popClipRect()
    external fun pushTextWrapPos(wrapLocalPosX: Float)
    external fun popTextWrapPos()

    // ---- Widgets ----
    external fun text(text: String)
    external fun textWrapped(text: String)
    external fun textUnformatted(text: String)
    external fun textLink(text: String): Boolean
    external fun textLinkOpenURL(label: String, url: String?): Boolean
    external fun textColored(r: Float, g: Float, b: Float, a: Float, text: String)
    external fun textDisabled(text: String)
    external fun labelText(label: String, text: String)
    external fun bulletText(text: String)
    external fun bullet()
    external fun alignTextToFramePadding()
    external fun separator()
    external fun separatorText(text: String)
    external fun sameLine(offsetFromStartX: Float, spacing: Float)
    external fun newLine()
    external fun spacing()
    external fun dummy(w: Float, h: Float)
    external fun indent(indentW: Float)
    external fun unindent(indentW: Float)
    external fun button(label: String, w: Float, h: Float): Boolean
    external fun smallButton(label: String): Boolean
    external fun arrowButton(strId: String, dir: Int): Boolean
    external fun checkbox(label: String, v: BooleanArray): Boolean
    external fun checkboxFlags(label: String, flags: IntArray, flagsValue: Int): Boolean
    external fun pushItemFlag(flag: Int, enabled: Boolean)
    external fun popItemFlag()
    external fun shortcut(keyChord: Int, flags: Int): Boolean
    external fun sliderFloat(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String): Boolean
    external fun sliderInt(label: String, v: IntArray, vMin: Int, vMax: Int, format: String): Boolean
    external fun dragFloat(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    external fun dragFloat2(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    external fun dragFloat3(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    external fun dragFloat4(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    external fun dragFloatRange2(label: String, vCurrentMin: FloatArray, vCurrentMax: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, formatMax: String?, flags: Int): Boolean
    external fun dragInt(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    external fun dragInt2(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    external fun dragInt3(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    external fun dragInt4(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    external fun dragIntRange2(label: String, vCurrentMin: IntArray, vCurrentMax: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, formatMax: String?, flags: Int): Boolean
    external fun sliderFloat2(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    external fun sliderFloat3(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    external fun sliderFloat4(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    external fun sliderInt2(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    external fun sliderInt3(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    external fun sliderInt4(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    external fun sliderAngle(label: String, vRad: FloatArray, vDegreesMin: Float, vDegreesMax: Float, format: String, flags: Int): Boolean
    external fun vSliderFloat(label: String, w: Float, h: Float, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean
    external fun vSliderInt(label: String, w: Float, h: Float, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean
    external fun sliderScalar(label: String, dataType: Int, v: LongArray, vMin: LongArray, vMax: LongArray, format: String): Boolean
    external fun dragScalar(label: String, dataType: Int, v: LongArray, vSpeed: Float, vMin: LongArray, vMax: LongArray, format: String): Boolean
    external fun inputFloat(label: String, v: FloatArray, step: Float, stepFast: Float, format: String, flags: Int): Boolean
    external fun inputFloat2(label: String, v: FloatArray, format: String, flags: Int): Boolean
    external fun inputFloat3(label: String, v: FloatArray, format: String, flags: Int): Boolean
    external fun inputFloat4(label: String, v: FloatArray, format: String, flags: Int): Boolean
    external fun inputInt(label: String, v: IntArray, step: Int, stepFast: Int, flags: Int): Boolean
    external fun inputInt2(label: String, v: IntArray, flags: Int): Boolean
    external fun inputInt3(label: String, v: IntArray, flags: Int): Boolean
    external fun inputInt4(label: String, v: IntArray, flags: Int): Boolean
    external fun inputDouble(label: String, v: DoubleArray, step: Double, stepFast: Double, format: String, flags: Int): Boolean
    external fun colorEdit3(label: String, col: FloatArray, flags: Int): Boolean
    external fun colorEdit4(label: String, col: FloatArray, flags: Int): Boolean
    external fun colorPicker3(label: String, col: FloatArray, flags: Int): Boolean
    external fun colorPicker4(label: String, col: FloatArray, flags: Int): Boolean
    external fun colorButton(descId: String, r: Float, g: Float, b: Float, a: Float, flags: Int, w: Float, h: Float): Boolean
    external fun setColorEditOptions(flags: Int)
    external fun colorConvertFloat4ToU32(r: Float, g: Float, b: Float, a: Float): Int
    external fun colorConvertU32ToFloat4(`in`: Int): FloatArray
    external fun colorConvertRGBtoHSV(r: Float, g: Float, b: Float, outH: FloatArray, outS: FloatArray, outV: FloatArray)
    external fun colorConvertHSVtoRGB(h: Float, s: Float, v: Float, outR: FloatArray, outG: FloatArray, outB: FloatArray)
    external fun inputText(label: String, buf: String, flags: Int): String?
    external fun inputTextMultiline(label: String, buf: String, w: Float, h: Float, flags: Int): String?
    external fun inputTextWithHint(label: String, hint: String, buf: String, flags: Int): String?
    external fun combo(label: String, currentItem: IntArray, items: Array<String>): Boolean
    external fun selectable(label: String, selected: Boolean, flags: Int, w: Float, h: Float): Boolean
    external fun radioButton(label: String, active: Boolean): Boolean
    external fun progressBar(fraction: Float, w: Float, h: Float, overlay: String?)
    external fun collapsingHeader(label: String, flags: Int): Boolean
    external fun treeNode(label: String): Boolean
    external fun treeNodeEx(label: String, flags: Int): Boolean
    external fun treeNodeGetOpen(label: String): Boolean
    external fun treePush(strId: String?)
    external fun treePop()
    external fun invisibleButton(strId: String, w: Float, h: Float, flags: Int): Boolean
    external fun beginGroup()
    external fun endGroup()
    external fun setCursorPos(x: Float, y: Float)
    external fun pushId(strId: String)
    external fun popId()
    external fun isItemHovered(flags: Int): Boolean
    external fun isItemActive(): Boolean
    external fun isItemClicked(mouseButton: Int): Boolean
    external fun isWindowHovered(flags: Int): Boolean
    external fun isWindowFocused(flags: Int): Boolean

    // ---- State queries ----
    external fun isItemFocused(): Boolean
    external fun isItemVisible(): Boolean
    external fun isItemEdited(): Boolean
    external fun isItemActivated(): Boolean
    external fun isItemDeactivated(): Boolean
    external fun isItemDeactivatedAfterEdit(): Boolean
    external fun isItemToggledOpen(): Boolean
    external fun isItemToggledSelection(): Boolean
    external fun isAnyItemHovered(): Boolean
    external fun isAnyItemActive(): Boolean
    external fun isAnyItemFocused(): Boolean
    external fun getItemID(): Int
    external fun getItemFlags(): Int
    external fun getItemRectMin(): FloatArray
    external fun getItemRectMax(): FloatArray
    external fun getItemRectSize(): FloatArray
    external fun isWindowAppearing(): Boolean
    external fun isWindowCollapsed(): Boolean
    external fun isRectVisible(w: Float, h: Float): Boolean
    external fun isPopupOpen(strId: String, flags: Int): Boolean
    external fun getWindowPos(): FloatArray
    external fun getWindowSize(): FloatArray
    external fun getWindowWidth(): Float
    external fun getWindowHeight(): Float
    external fun getWindowContentRegionMax(): FloatArray
    external fun getWindowContentRegionMin(): FloatArray
    external fun getWindowDrawList(): Long
    external fun isKeyDown(key: Int): Boolean
    external fun isKeyPressed(key: Int, repeat: Boolean): Boolean
    external fun isKeyReleased(key: Int): Boolean
    external fun isMouseDown(button: Int): Boolean
    external fun isMouseClicked(button: Int, repeat: Boolean): Boolean
    external fun isMouseReleased(button: Int): Boolean
    external fun isMouseDoubleClicked(button: Int): Boolean
    external fun isMouseDragging(button: Int, lockThreshold: Float): Boolean
    external fun isAnyMouseDown(): Boolean
    external fun isMousePosValid(hasPos: Boolean, x: Float, y: Float): Boolean
    external fun getMousePos(): FloatArray
    external fun getMouseDragDelta(button: Int, lockThreshold: Float): FloatArray
    external fun resetMouseDragDelta(button: Int)
    external fun getMouseCursor(): Int
    external fun setMouseCursor(cursor: Int)
    external fun setKeyboardFocusHere(offset: Int)
    external fun setNextFrameWantCaptureKeyboard(wantCaptureKeyboard: Boolean)
    external fun setNextFrameWantCaptureMouse(wantCaptureMouse: Boolean)
    external fun setClipboardText(text: String)
    external fun getClipboardText(): String?
    external fun setClipboardFunctions()
    external fun getTime(): Double
    external fun getCursorPos(): FloatArray
    external fun getCursorScreenPos(): FloatArray
    external fun getCursorStartPos(): FloatArray
    external fun setCursorPosX(localX: Float)
    external fun setCursorScreenPos(x: Float, y: Float)
    external fun getContentRegionAvail(): FloatArray
    external fun getScrollX(): Float
    external fun getScrollY(): Float
    external fun getScrollMaxX(): Float
    external fun getScrollMaxY(): Float
    external fun setScrollHereX(centerXRatio: Float)
    external fun setScrollHereY(centerYRatio: Float)
    external fun setScrollFromPosX(localX: Float, centerXRatio: Float)
    external fun setScrollFromPosY(localY: Float, centerYRatio: Float)
    external fun setScrollX(scrollX: Float)
    external fun setScrollY(scrollY: Float)
    external fun getFrameCount(): Int
    external fun getFrameHeight(): Float
    external fun getFrameHeightWithSpacing(): Float
    external fun getFontSize(): Float
    external fun getFont(): Long
    external fun getMainViewport(): Long
    external fun getStyleColorVec4(idx: Int): FloatArray
    external fun getCursorPosX(): Float
    external fun getKeyName(key: Int): String
    external fun getTextLineHeight(): Float
    external fun getTextLineHeightWithSpacing(): Float
    external fun getID(strId: String): Int
    external fun getColorU32(idx: Int, alphaMul: Float): Int
    external fun getStyleColorName(idx: Int): String
    external fun calcTextSize(text: String, hideTextAfterDoubleHash: Boolean, wrapWidth: Float): FloatArray
    external fun calcItemWidth(): Float

    // ---- Columns (legacy multi-column layout) ----
    external fun columns(count: Int, id: String?, border: Boolean)
    external fun nextColumn()
    external fun getColumnIndex(): Int
    external fun getColumnOffset(columnIndex: Int): Float
    external fun setColumnOffset(columnIndex: Int, offsetX: Float)
    external fun getColumnWidth(columnIndex: Int): Float
    external fun setColumnWidth(columnIndex: Int, width: Float)
    external fun getColumnsCount(): Int

    // ---- Tables ----
    external fun beginTable(strId: String, column: Int, flags: Int, outerW: Float, outerH: Float, innerWidth: Float): Boolean
    external fun endTable()
    external fun tableNextRow(minRowHeight: Int, flags: Int)
    external fun tableNextColumn(): Boolean
    external fun tableSetColumnIndex(columnN: Int): Boolean
    external fun tableSetupColumn(label: String, flags: Int, initWidthOrWeight: Float, userId: Int)
    external fun tableSetupScrollFreeze(cols: Int, rows: Int)
    external fun tableHeadersRow()
    external fun tableHeader(label: String)
    external fun tableAngledHeadersRow()
    external fun tableGetColumnCount(): Int
    external fun tableGetColumnFlags(columnN: Int): Int
    external fun tableGetColumnIndex(): Int
    external fun tableGetRowIndex(): Int
    external fun tableGetColumnName(columnN: Int): String
    external fun tableGetSortSpecs(): Long
    external fun tableSetBgColor(target: Int, color: Int, columnN: Int)
    external fun tabItemButton(label: String, flags: Int): Boolean

    // ---- Style ----
    external fun styleColorsDark()
    external fun styleColorsLight()
    external fun styleColorsClassic()
    external fun showStyleSelector(label: String): Boolean
    external fun showFontSelector(label: String)
    external fun showStyleEditor()
    external fun pushStyleColorVec4(idx: Int, r: Float, g: Float, b: Float, a: Float)
    external fun pushStyleColorU32(idx: Int, color: Int)
    external fun popStyleColor(count: Int)
    external fun pushStyleVarFloat(idx: Int, value: Float)
    external fun pushStyleVarVec2(idx: Int, x: Float, y: Float)
    external fun popStyleVar(count: Int)
    external fun pushFont(font: Long)
    external fun popFont()
    external fun pushItemWidth(width: Float)
    external fun popItemWidth()
    external fun setNextItemWidth(width: Float)
    external fun setNextItemOpen(isOpen: Boolean, cond: Int)
    external fun setNextItemAllowOverlap()
    external fun setNextItemSelectionUserData(selectionUserData: Long)
    external fun setNextItemShortcut(keyChord: Int, flags: Int)
    external fun setNextWindowCollapsed(collapsed: Boolean, cond: Int)
    external fun setNextWindowContentSize(w: Float, h: Float)
    external fun setNextWindowFocus()
    external fun setNextWindowScroll(x: Float, y: Float)
    external fun setNextWindowSizeConstraints(minW: Float, minH: Float, maxW: Float, maxH: Float)
    external fun setItemTooltip(text: String)
    external fun setItemDefaultFocus()
    external fun setTabItemClosed(tabOrDockedWindowLabel: String)
    external fun styleGetColor(style: Long, idx: Int): FloatArray
    external fun styleSetColor(style: Long, idx: Int, r: Float, g: Float, b: Float, a: Float)

    // ---- IO ----
    external fun ioSetDisplaySize(io: Long, w: Float, h: Float)
    external fun ioSetDisplayFramebufferScale(io: Long, sx: Float, sy: Float)
    external fun ioSetDeltaTime(io: Long, dt: Float)
    external fun ioSetConfigFlags(io: Long, flags: Int)
    external fun ioSetBackendFlags(io: Long, flags: Int)
    external fun ioSetIniFilename(io: Long, path: String?)
    external fun ioSetFontGlobalScale(io: Long, scale: Float)
    external fun ioGetDisplaySize(io: Long): FloatArray
    external fun ioGetDisplayFramebufferScale(io: Long): FloatArray
    external fun ioGetDeltaTime(io: Long): Float
    external fun ioGetConfigFlags(io: Long): Int
    external fun ioGetBackendFlags(io: Long): Int
    external fun ioGetIniFilename(io: Long): String?
    external fun ioGetFontGlobalScale(io: Long): Float
    external fun ioAddMousePosEvent(io: Long, x: Float, y: Float)
    external fun ioAddMouseButtonEvent(io: Long, button: Int, down: Boolean)
    external fun ioAddMouseWheelEvent(io: Long, x: Float, y: Float)
    external fun ioAddKeyEvent(io: Long, key: Int, down: Boolean)
    external fun ioAddInputCharacter(io: Long, c: Int)
    external fun ioWantCaptureMouse(io: Long): Boolean
    external fun ioWantCaptureKeyboard(io: Long): Boolean
    external fun ioWantTextInput(io: Long): Boolean
    external fun ioGetFonts(io: Long): Long

    // ---- Fonts ----
    external fun fontsAddFontFromFileTTF(atlas: Long, path: String, sizePx: Float): Long
    external fun fontsAddFontDefaultConfig(
        atlas: Long, name: String?, mergeMode: Boolean, pixelSnapH: Boolean,
        oversampleH: Int, oversampleV: Int, sizePixels: Float,
        glyphOffsetX: Float, glyphOffsetY: Float, glyphMinAdvanceX: Float, glyphMaxAdvanceX: Float,
        rasterizerMultiply: Float, rasterizerDensity: Float, extraSizeScale: Float,
    ): Long
    external fun fontsAddFontFromFileTTFConfig(
        atlas: Long, path: String, name: String?, mergeMode: Boolean, pixelSnapH: Boolean,
        oversampleH: Int, oversampleV: Int, sizePixels: Float,
        glyphOffsetX: Float, glyphOffsetY: Float, glyphMinAdvanceX: Float, glyphMaxAdvanceX: Float,
        rasterizerMultiply: Float, rasterizerDensity: Float, extraSizeScale: Float,
    ): Long
    external fun fontsAddFontDefault(atlas: Long): Long
    external fun fontsBuild(atlas: Long): Boolean
    external fun fontsGetTexDataAsRGBA32(atlas: Long, outDims: IntArray): ByteArray
    external fun fontsSetTexId(atlas: Long, texId: Long)

    // ---- Draw data ----
    external fun drawDataGetDisplayPos(data: Long): FloatArray
    external fun drawDataGetDisplaySize(data: Long): FloatArray
    external fun drawDataGetFramebufferScale(data: Long): FloatArray
    external fun drawDataGetCmdListsCount(data: Long): Int
    external fun drawDataGetCmdList(data: Long, index: Int): Long
    external fun drawListGetVtxCount(list: Long): Int
    external fun drawListGetIdxCount(list: Long): Int
    external fun drawListGetCmdCount(list: Long): Int
    external fun drawListGetCmd(list: Long, index: Int): Long
    external fun drawListCopyVtx(list: Long, vtxOffset: Int, vtxCount: Int, positions: FloatArray, uvs: FloatArray, colors: IntArray)
    external fun drawListCopyIdx(list: Long, idxOffset: Int, idxCount: Int, out: IntArray)
    external fun drawCmdGetClipRect(cmd: Long): FloatArray
    external fun drawCmdGetTexId(cmd: Long): Long
    external fun drawCmdGetVtxOffset(cmd: Long): Int
    external fun drawCmdGetIdxOffset(cmd: Long): Int
    external fun drawCmdGetElemCount(cmd: Long): Int
    external fun drawCmdHasUserCallback(cmd: Long): Boolean

    // ---- Draw list primitives ----
    external fun drawListAddLine(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, col: Int, thickness: Float)
    external fun drawListAddRect(list: Long, minX: Float, minY: Float, maxX: Float, maxY: Float, col: Int, rounding: Float, flags: Int, thickness: Float)
    external fun drawListAddRectFilled(list: Long, minX: Float, minY: Float, maxX: Float, maxY: Float, col: Int, rounding: Float, flags: Int)
    external fun drawListAddCircle(list: Long, x: Float, y: Float, radius: Float, col: Int, numSegments: Int, thickness: Float)
    external fun drawListAddCircleFilled(list: Long, x: Float, y: Float, radius: Float, col: Int, numSegments: Int)
    external fun drawListAddText(list: Long, x: Float, y: Float, col: Int, text: String)
    external fun drawListAddQuad(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, p4X: Float, p4Y: Float, col: Int, thickness: Float)
    external fun drawListAddTriangle(list: Long, p1X: Float, p1Y: Float, p2X: Float, p2Y: Float, p3X: Float, p3Y: Float, col: Int, thickness: Float)
    external fun drawListAddPolyline(list: Long, points: FloatArray, col: Int, closed: Boolean, thickness: Float)

    // ---- Draw list getters ----
    external fun getForegroundDrawList(): Long
    external fun getBackgroundDrawList(): Long
}

// =========================================================================
// JVM/Android actual implementations
// =========================================================================

internal class JvmImGuiContext(internal val ptr: Long) : ImGuiContext {
    override fun close() {
        Jni.destroyContext(ptr)
    }
}

internal class JvmImGuiIO(internal val ptr: Long) : ImGuiIO {
    override var displaySize: ImVec2
        get() {
            val v = Jni.ioGetDisplaySize(ptr)
            return ImVec2(v[0], v[1])
        }
        set(value) = Jni.ioSetDisplaySize(ptr, value.x, value.y)

    override var displayFramebufferScale: ImVec2
        get() {
            val v = Jni.ioGetDisplayFramebufferScale(ptr)
            return ImVec2(v[0], v[1])
        }
        set(value) = Jni.ioSetDisplayFramebufferScale(ptr, value.x, value.y)

    override var deltaTime: Float
        get() = Jni.ioGetDeltaTime(ptr)
        set(value) = Jni.ioSetDeltaTime(ptr, value)

    override var configFlags: Int
        get() = Jni.ioGetConfigFlags(ptr)
        set(value) = Jni.ioSetConfigFlags(ptr, value)

    override var backendFlags: Int
        get() = Jni.ioGetBackendFlags(ptr)
        set(value) = Jni.ioSetBackendFlags(ptr, value)

    override var iniFilename: String?
        get() = Jni.ioGetIniFilename(ptr)
        set(value) = Jni.ioSetIniFilename(ptr, value)

    override var fontGlobalScale: Float
        get() = Jni.ioGetFontGlobalScale(ptr)
        set(value) = Jni.ioSetFontGlobalScale(ptr, value)

    override val fonts: ImFontAtlas
        get() = JvmImFontAtlas(Jni.ioGetFonts(ptr))

    override fun addMousePosEvent(x: Float, y: Float) = Jni.ioAddMousePosEvent(ptr, x, y)
    override fun addMouseButtonEvent(button: Int, down: Boolean) = Jni.ioAddMouseButtonEvent(ptr, button, down)
    override fun addMouseWheelEvent(wheelX: Float, wheelY: Float) = Jni.ioAddMouseWheelEvent(ptr, wheelX, wheelY)
    override fun addKeyEvent(key: Int, down: Boolean) = Jni.ioAddKeyEvent(ptr, key, down)
    override fun addInputCharacter(c: UInt) = Jni.ioAddInputCharacter(ptr, c.toInt())
    override fun addInputCharactersUTF8(text: String) {
        // Decode the UTF-8 string into code points and queue them one by one.
        var i = 0
        while (i < text.length) {
            val c = text.codePointAt(i)
            Jni.ioAddInputCharacter(ptr, c)
            i += Character.charCount(c)
        }
    }

    override val wantCaptureMouse: Boolean
        get() = Jni.ioWantCaptureMouse(ptr)

    override val wantCaptureKeyboard: Boolean
        get() = Jni.ioWantCaptureKeyboard(ptr)

    override val wantTextInput: Boolean
        get() = Jni.ioWantTextInput(ptr)
}

internal class JvmImGuiStyle(internal val ptr: Long) : ImGuiStyle {
    override fun getColor(idx: Int): ImVec4 {
        val c = Jni.styleGetColor(ptr, idx)
        return ImVec4(c[0], c[1], c[2], c[3])
    }

    override fun setColor(idx: Int, color: ImVec4) {
        Jni.styleSetColor(ptr, idx, color.x, color.y, color.z, color.w)
    }
}

internal class JvmImFont(internal val ptr: Long) : ImFont

internal class JvmImFontAtlas(internal val ptr: Long) : ImFontAtlas {
    override fun addFontFromFileTTF(path: String, sizePx: Float): ImFont =
        JvmImFont(Jni.fontsAddFontFromFileTTF(ptr, path, sizePx))

    override fun addFontDefault(): ImFont =
        JvmImFont(Jni.fontsAddFontDefault(ptr))

    override fun addFontDefault(config: ImFontConfig): ImFont =
        JvmImFont(
            Jni.fontsAddFontDefaultConfig(
                ptr, config.name, config.mergeMode, config.pixelSnapH,
                config.oversampleH, config.oversampleV, config.sizePixels,
                config.glyphOffsetX, config.glyphOffsetY, config.glyphMinAdvanceX, config.glyphMaxAdvanceX,
                config.rasterizerMultiply, config.rasterizerDensity, config.extraSizeScale,
            ),
        )

    override fun addFontFromFileTTF(path: String, config: ImFontConfig): ImFont =
        JvmImFont(
            Jni.fontsAddFontFromFileTTFConfig(
                ptr, path, config.name, config.mergeMode, config.pixelSnapH,
                config.oversampleH, config.oversampleV, config.sizePixels,
                config.glyphOffsetX, config.glyphOffsetY, config.glyphMinAdvanceX, config.glyphMaxAdvanceX,
                config.rasterizerMultiply, config.rasterizerDensity, config.extraSizeScale,
            ),
        )

    override fun build(): Boolean = Jni.fontsBuild(ptr)

    override fun getTexDataAsRGBA32(): FontTexData {
        val dims = IntArray(3)
        val pixels = Jni.fontsGetTexDataAsRGBA32(ptr, dims)
        return FontTexData(pixels, dims[0], dims[1], dims[2])
    }

    override fun setTexID(id: Long) {
        Jni.fontsSetTexId(ptr, id)
    }
}

internal class JvmImDrawCmd(internal val ptr: Long) : ImDrawCmd {
    override val clipRect: ImVec4
        get() {
            val r = Jni.drawCmdGetClipRect(ptr)
            return ImVec4(r[0], r[1], r[2], r[3])
        }

    override val texId: Long
        get() = Jni.drawCmdGetTexId(ptr)

    override val vtxOffset: Int
        get() = Jni.drawCmdGetVtxOffset(ptr)

    override val idxOffset: Int
        get() = Jni.drawCmdGetIdxOffset(ptr)

    override val elemCount: Int
        get() = Jni.drawCmdGetElemCount(ptr)

    override val hasUserCallback: Boolean
        get() = Jni.drawCmdHasUserCallback(ptr)
}

internal class JvmImDrawList(internal val ptr: Long) : ImDrawList {
    override val vtxCount: Int
        get() = Jni.drawListGetVtxCount(ptr)

    override val idxCount: Int
        get() = Jni.drawListGetIdxCount(ptr)

    override val cmdCount: Int
        get() = Jni.drawListGetCmdCount(ptr)

    override fun cmd(index: Int): ImDrawCmd = JvmImDrawCmd(Jni.drawListGetCmd(ptr, index))

    override fun copyVtx(vtxOffset: Int, count: Int): ImDrawVertData {
        val data = ImDrawVertData()
        data.positions.ensureCapacity(count * 2)
        data.uvs.ensureCapacity(count * 2)
        data.colors.ensureCapacity(count)
        if (count == 0) return data

        val positions = FloatArray(count * 2)
        val uvs = FloatArray(count * 2)
        val colors = IntArray(count)
        Jni.drawListCopyVtx(ptr, vtxOffset, count, positions, uvs, colors)
        for (i in 0 until count) {
            data.positions.add(positions[i * 2])
            data.positions.add(positions[i * 2 + 1])
            data.uvs.add(uvs[i * 2])
            data.uvs.add(uvs[i * 2 + 1])
            data.colors.add(colors[i])
        }
        return data
    }

    override fun copyIdx(idxOffset: Int, count: Int): IntArray {
        val out = IntArray(count)
        Jni.drawListCopyIdx(ptr, idxOffset, count, out)
        return out
    }

    override fun DrawLine(p1: ImVec2, p2: ImVec2, col: Int, thickness: Float) =
        Jni.drawListAddLine(ptr, p1.x, p1.y, p2.x, p2.y, col, thickness)

    override fun DrawRect(pMin: ImVec2, pMax: ImVec2, col: Int, rounding: Float, flags: Int, thickness: Float) =
        Jni.drawListAddRect(ptr, pMin.x, pMin.y, pMax.x, pMax.y, col, rounding, flags, thickness)

    override fun DrawRectFilled(pMin: ImVec2, pMax: ImVec2, col: Int, rounding: Float, flags: Int) =
        Jni.drawListAddRectFilled(ptr, pMin.x, pMin.y, pMax.x, pMax.y, col, rounding, flags)

    override fun DrawCircle(center: ImVec2, radius: Float, col: Int, numSegments: Int, thickness: Float) =
        Jni.drawListAddCircle(ptr, center.x, center.y, radius, col, numSegments, thickness)

    override fun DrawCircleFilled(center: ImVec2, radius: Float, col: Int, numSegments: Int) =
        Jni.drawListAddCircleFilled(ptr, center.x, center.y, radius, col, numSegments)

    override fun DrawText(pos: ImVec2, text: String, col: Int) =
        Jni.drawListAddText(ptr, pos.x, pos.y, col, text)

    override fun DrawQuad(p1: ImVec2, p2: ImVec2, p3: ImVec2, p4: ImVec2, col: Int, thickness: Float) =
        Jni.drawListAddQuad(ptr, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y, col, thickness)

    override fun DrawTriangle(p1: ImVec2, p2: ImVec2, p3: ImVec2, col: Int, thickness: Float) =
        Jni.drawListAddTriangle(ptr, p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, col, thickness)

    override fun DrawPolyline(points: Array<ImVec2>, col: Int, closed: Boolean, thickness: Float) {
        val flat = FloatArray(points.size * 2)
        for (i in points.indices) {
            flat[i * 2] = points[i].x
            flat[i * 2 + 1] = points[i].y
        }
        Jni.drawListAddPolyline(ptr, flat, col, closed, thickness)
    }
}

internal class JvmImDrawData(internal val ptr: Long) : ImDrawData {
    override val displayPos: ImVec2
        get() {
            val v = Jni.drawDataGetDisplayPos(ptr)
            return ImVec2(v[0], v[1])
        }

    override val displaySize: ImVec2
        get() {
            val v = Jni.drawDataGetDisplaySize(ptr)
            return ImVec2(v[0], v[1])
        }

    override val framebufferScale: ImVec2
        get() {
            val v = Jni.drawDataGetFramebufferScale(ptr)
            return ImVec2(v[0], v[1])
        }

    override val cmdListsCount: Int
        get() = Jni.drawDataGetCmdListsCount(ptr)

    override fun cmdList(index: Int): ImDrawList = JvmImDrawList(Jni.drawDataGetCmdList(ptr, index))
}

// =========================================================================
// actual object
// =========================================================================

actual object ImGui {
    override fun toString(): String = "cn.enaium.imgui.ImGui (JVM)"

    actual fun createContext(): ImGuiContext = JvmImGuiContext(Jni.createContext())
    actual fun destroyContext(context: ImGuiContext?) {
        if (context != null) {
            Jni.destroyContext((context as JvmImGuiContext).ptr)
        } else {
            Jni.destroyContext(0L)
        }
    }

    actual fun getCurrentContext(): ImGuiContext? {
        val ptr = Jni.getCurrentContext()
        return if (ptr != 0L) JvmImGuiContext(ptr) else null
    }

    actual fun setCurrentContext(context: ImGuiContext) =
        Jni.setCurrentContext((context as JvmImGuiContext).ptr)

    actual fun newFrame() = Jni.newFrame()
    actual fun render() = Jni.render()
    actual fun getDrawData(): ImDrawData = JvmImDrawData(Jni.getDrawData())
    actual fun getIO(): ImGuiIO = JvmImGuiIO(Jni.getIO())
    actual fun getStyle(): ImGuiStyle = JvmImGuiStyle(Jni.getStyle())
    actual fun getVersion(): String = Jni.getVersion()
    actual fun showDemoWindow(pOpen: BooleanArray?) = Jni.showDemoWindow(pOpen)
    actual fun showAboutWindow(pOpen: BooleanArray?) = Jni.showAboutWindow(pOpen)
    actual fun showMetricsWindow(pOpen: BooleanArray?) = Jni.showMetricsWindow(pOpen)
    actual fun showDebugLogWindow(pOpen: BooleanArray?) = Jni.showDebugLogWindow(pOpen)
    actual fun showUserGuide() = Jni.showUserGuide()
    actual fun showIDStackToolWindow(pOpen: BooleanArray?) = Jni.showIDStackToolWindow(pOpen)

    // ---- Windows ----
    actual fun begin(name: String, pOpen: BooleanArray?, flags: Int): Boolean = Jni.begin(name, pOpen, flags)
    actual fun end() = Jni.end()
    actual fun beginChild(id: String, size: ImVec2, childFlags: Int, windowFlags: Int): Boolean =
        Jni.beginChild(id, size.x, size.y, childFlags, windowFlags)

    actual fun endChild() = Jni.endChild()

    // ---- Docking ----
    actual fun dockSpace(id: Int, size: ImVec2, flags: Int): Int = Jni.dockSpace(id, size.x, size.y, flags)
    actual fun setNextWindowDockID(dockId: Int, cond: Int) = Jni.setNextWindowDockID(dockId, cond)
    actual fun dockBuilderAddNode(nodeId: Int, flags: Int): Int = Jni.dockBuilderAddNode(nodeId, flags)
    actual fun dockBuilderRemoveNode(nodeId: Int) = Jni.dockBuilderRemoveNode(nodeId)
    actual fun dockBuilderSplitNode(nodeId: Int, splitDir: Int, sizeRatioForNodeAtDir: Float): Pair<Int, Int> {
        val packed = Jni.dockBuilderSplitNode(nodeId, splitDir, sizeRatioForNodeAtDir)
        return (packed.toInt()) to ((packed ushr 32).toInt())
    }
    actual fun dockBuilderDockWindow(windowName: String, nodeId: Int) = Jni.dockBuilderDockWindow(windowName, nodeId)
    actual fun dockBuilderFinish(nodeId: Int) = Jni.dockBuilderFinish(nodeId)

    actual fun setNextWindowPos(pos: ImVec2, cond: Int, pivot: ImVec2?) =
        Jni.setNextWindowPos(pos.x, pos.y, cond, pivot?.x ?: 0f, pivot?.y ?: 0f)

    actual fun setNextWindowSize(size: ImVec2, cond: Int) = Jni.setNextWindowSize(size.x, size.y, cond)
    actual fun setWindowSize(size: ImVec2, cond: Int) = Jni.setWindowSize(size.x, size.y, cond)
    actual fun setNextWindowBgAlpha(alpha: Float) = Jni.setNextWindowBgAlpha(alpha)
    actual fun beginDisabled(disabled: Boolean) = Jni.beginDisabled(disabled)
    actual fun endDisabled() = Jni.endDisabled()
    actual fun beginMainMenuBar(): Boolean = Jni.beginMainMenuBar()
    actual fun endMainMenuBar() = Jni.endMainMenuBar()
    actual fun beginMenuBar(): Boolean = Jni.beginMenuBar()
    actual fun endMenuBar() = Jni.endMenuBar()
    actual fun beginMenu(label: String, enabled: Boolean): Boolean = Jni.beginMenu(label, enabled)
    actual fun endMenu() = Jni.endMenu()
    actual fun menuItem(label: String, shortcut: String, selected: Boolean, enabled: Boolean): Boolean =
        Jni.menuItem(label, shortcut, selected, enabled)

    actual fun beginTabBar(id: String, flags: Int): Boolean = Jni.beginTabBar(id, flags)
    actual fun endTabBar() = Jni.endTabBar()
    actual fun beginTabItem(label: String, pOpen: BooleanArray?, flags: Int): Boolean = Jni.beginTabItem(label, pOpen, flags)
    actual fun endTabItem() = Jni.endTabItem()
    actual fun beginTooltip(): Boolean = Jni.beginTooltip()
    actual fun endTooltip() = Jni.endTooltip()
    actual fun setTooltip(text: String) = Jni.setTooltip(text)
    actual fun openPopup(id: String, popupFlags: Int) = Jni.openPopup(id, popupFlags)
    actual fun beginPopup(id: String, flags: Int): Boolean = Jni.beginPopup(id, flags)
    actual fun beginPopupModal(name: String, pOpen: BooleanArray?, flags: Int): Boolean = Jni.beginPopupModal(name, pOpen, flags)
    actual fun endPopup() = Jni.endPopup()
    actual fun closeCurrentPopup() = Jni.closeCurrentPopup()
    actual fun beginPopupContextItem(strId: String?, popupFlags: Int): Boolean = Jni.beginPopupContextItem(strId, popupFlags)
    actual fun beginPopupContextWindow(strId: String?, popupFlags: Int): Boolean = Jni.beginPopupContextWindow(strId, popupFlags)
    actual fun beginItemTooltip(): Boolean = Jni.beginItemTooltip()
    actual fun openPopupOnItemClick(strId: String?, popupFlags: Int) = Jni.openPopupOnItemClick(strId, popupFlags)
    actual fun beginCombo(label: String, previewValue: String, flags: Int): Boolean = Jni.beginCombo(label, previewValue, flags)
    actual fun endCombo() = Jni.endCombo()

    // ---- Drag and drop ----
    actual fun beginDragDropSource(flags: Int): Boolean = Jni.beginDragDropSource(flags)
    actual fun setDragDropPayload(type: String, data: ByteArray, cond: Int): Boolean = Jni.setDragDropPayload(type, data, cond)
    actual fun endDragDropSource() = Jni.endDragDropSource()
    actual fun beginDragDropTarget(): Boolean = Jni.beginDragDropTarget()
    actual fun acceptDragDropPayload(type: String, flags: Int): ByteArray? = Jni.acceptDragDropPayload(type, flags)
    actual fun endDragDropTarget() = Jni.endDragDropTarget()
    actual fun getDragDropPayload(): String? = Jni.getDragDropPayload()

    // ---- Images ----
    actual fun image(texId: Long, size: ImVec2, uv0: ImVec2, uv1: ImVec2, tintColor: ImVec4, borderColor: ImVec4) =
        Jni.image(texId, size.x, size.y, uv0.x, uv0.y, uv1.x, uv1.y, tintColor.x, tintColor.y, tintColor.z, tintColor.w, borderColor.x, borderColor.y, borderColor.z, borderColor.w)

    actual fun imageButton(texId: Long, size: ImVec2, uv0: ImVec2, uv1: ImVec2, framePadding: Int, bgColor: ImVec4, tintColor: ImVec4): Boolean =
        Jni.imageButton(texId, size.x, size.y, uv0.x, uv0.y, uv1.x, uv1.y, framePadding, bgColor.x, bgColor.y, bgColor.z, bgColor.w, tintColor.x, tintColor.y, tintColor.z, tintColor.w)

    actual fun imageWithBg(texId: Long, size: ImVec2, bgColor: ImVec4, uv0: ImVec2, uv1: ImVec2) =
        Jni.imageWithBg(texId, size.x, size.y, bgColor.x, bgColor.y, bgColor.z, bgColor.w, uv0.x, uv0.y, uv1.x, uv1.y)

    // ---- ListBox ----
    actual fun beginListBox(label: String, size: ImVec2): Boolean = Jni.beginListBox(label, size.x, size.y)
    actual fun endListBox() = Jni.endListBox()
    actual fun listBox(label: String, currentItem: IntArray, items: Array<String>): Boolean =
        Jni.listBox(label, currentItem, items)

    // ---- MultiSelect ----
    actual fun beginMultiSelect(flags: Int, selectionSize: Int, itemsCount: Int): Long = Jni.beginMultiSelect(flags, selectionSize, itemsCount)
    actual fun endMultiSelect(): Long = Jni.endMultiSelect()

    // ---- Logging ----
    actual fun logToClipboard(autoOpenDepth: Int) = Jni.logToClipboard(autoOpenDepth)
    actual fun logToFile(autoOpenDepth: Int, filename: String?) = Jni.logToFile(autoOpenDepth, filename)
    actual fun logToTTY(autoOpenDepth: Int) = Jni.logToTTY(autoOpenDepth)
    actual fun logFinish() = Jni.logFinish()
    actual fun logText(text: String) = Jni.logText(text)

    // ---- .ini settings ----
    actual fun saveIniSettingsToDisk(iniFilename: String?) = Jni.saveIniSettingsToDisk(iniFilename)
    actual fun loadIniSettingsFromDisk(iniFilename: String?) = Jni.loadIniSettingsFromDisk(iniFilename)
    actual fun saveIniSettingsToMemory(): String? = Jni.saveIniSettingsToMemory()
    actual fun loadIniSettingsFromMemory(iniData: String) = Jni.loadIniSettingsFromMemory(iniData)

    // ---- Scissor rect / text wrapping ----
    actual fun pushClipRect(clipRectMin: ImVec2, clipRectMax: ImVec2, intersectWithCurrentClipRect: Boolean) =
        Jni.pushClipRect(clipRectMin.x, clipRectMin.y, clipRectMax.x, clipRectMax.y, intersectWithCurrentClipRect)

    actual fun popClipRect() = Jni.popClipRect()
    actual fun pushTextWrapPos(wrapLocalPosX: Float) = Jni.pushTextWrapPos(wrapLocalPosX)
    actual fun popTextWrapPos() = Jni.popTextWrapPos()

    // ---- Widgets ----
    actual fun text(text: String) = Jni.text(text)
    actual fun textWrapped(text: String) = Jni.textWrapped(text)
    actual fun textUnformatted(text: String) = Jni.textUnformatted(text)
    actual fun textLink(text: String): Boolean = Jni.textLink(text)
    actual fun textLinkOpenURL(label: String, url: String?): Boolean = Jni.textLinkOpenURL(label, url)
    actual fun textColored(color: ImVec4, text: String) = Jni.textColored(color.x, color.y, color.z, color.w, text)
    actual fun textDisabled(text: String) = Jni.textDisabled(text)
    actual fun labelText(label: String, text: String) = Jni.labelText(label, text)
    actual fun bulletText(text: String) = Jni.bulletText(text)
    actual fun bullet() = Jni.bullet()
    actual fun alignTextToFramePadding() = Jni.alignTextToFramePadding()
    actual fun separator() = Jni.separator()
    actual fun separatorText(text: String) = Jni.separatorText(text)
    actual fun sameLine(offsetFromStartX: Float, spacing: Float) = Jni.sameLine(offsetFromStartX, spacing)
    actual fun newLine() = Jni.newLine()
    actual fun spacing() = Jni.spacing()
    actual fun dummy(size: ImVec2) = Jni.dummy(size.x, size.y)
    actual fun indent(indentW: Float) = Jni.indent(indentW)
    actual fun unindent(indentW: Float) = Jni.unindent(indentW)
    actual fun button(label: String, size: ImVec2): Boolean = Jni.button(label, size.x, size.y)
    actual fun smallButton(label: String): Boolean = Jni.smallButton(label)
    actual fun arrowButton(strId: String, dir: Int): Boolean = Jni.arrowButton(strId, dir)
    actual fun checkbox(label: String, v: BooleanArray): Boolean = Jni.checkbox(label, v)
    actual fun checkboxFlags(label: String, flags: IntArray, flagsValue: Int): Boolean = Jni.checkboxFlags(label, flags, flagsValue)
    actual fun pushItemFlag(flag: Int, enabled: Boolean) = Jni.pushItemFlag(flag, enabled)
    actual fun popItemFlag() = Jni.popItemFlag()
    actual fun shortcut(keyChord: Int, flags: Int): Boolean = Jni.shortcut(keyChord, flags)
    actual fun sliderFloat(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String): Boolean =
        Jni.sliderFloat(label, v, vMin, vMax, format)

    actual fun sliderInt(label: String, v: IntArray, vMin: Int, vMax: Int, format: String): Boolean =
        Jni.sliderInt(label, v, vMin, vMax, format)

    actual fun dragFloat(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        Jni.dragFloat(label, v, vSpeed, vMin, vMax, format, flags)

    actual fun dragFloat2(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        Jni.dragFloat2(label, v, vSpeed, vMin, vMax, format, flags)

    actual fun dragFloat3(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        Jni.dragFloat3(label, v, vSpeed, vMin, vMax, format, flags)

    actual fun dragFloat4(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        Jni.dragFloat4(label, v, vSpeed, vMin, vMax, format, flags)

    actual fun dragFloatRange2(label: String, vCurrentMin: FloatArray, vCurrentMax: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, formatMax: String?, flags: Int): Boolean =
        Jni.dragFloatRange2(label, vCurrentMin, vCurrentMax, vSpeed, vMin, vMax, format, formatMax, flags)

    actual fun dragInt(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        Jni.dragInt(label, v, vSpeed, vMin, vMax, format, flags)

    actual fun dragInt2(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        Jni.dragInt2(label, v, vSpeed, vMin, vMax, format, flags)

    actual fun dragInt3(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        Jni.dragInt3(label, v, vSpeed, vMin, vMax, format, flags)

    actual fun dragInt4(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        Jni.dragInt4(label, v, vSpeed, vMin, vMax, format, flags)

    actual fun dragIntRange2(label: String, vCurrentMin: IntArray, vCurrentMax: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, formatMax: String?, flags: Int): Boolean =
        Jni.dragIntRange2(label, vCurrentMin, vCurrentMax, vSpeed, vMin, vMax, format, formatMax, flags)

    actual fun sliderFloat2(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        Jni.sliderFloat2(label, v, vMin, vMax, format, flags)

    actual fun sliderFloat3(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        Jni.sliderFloat3(label, v, vMin, vMax, format, flags)

    actual fun sliderFloat4(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        Jni.sliderFloat4(label, v, vMin, vMax, format, flags)

    actual fun sliderInt2(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        Jni.sliderInt2(label, v, vMin, vMax, format, flags)

    actual fun sliderInt3(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        Jni.sliderInt3(label, v, vMin, vMax, format, flags)

    actual fun sliderInt4(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        Jni.sliderInt4(label, v, vMin, vMax, format, flags)

    actual fun sliderAngle(label: String, vRad: FloatArray, vDegreesMin: Float, vDegreesMax: Float, format: String, flags: Int): Boolean =
        Jni.sliderAngle(label, vRad, vDegreesMin, vDegreesMax, format, flags)

    actual fun vSliderFloat(label: String, size: ImVec2, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        Jni.vSliderFloat(label, size.x, size.y, v, vMin, vMax, format, flags)

    actual fun vSliderInt(label: String, size: ImVec2, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        Jni.vSliderInt(label, size.x, size.y, v, vMin, vMax, format, flags)

    actual fun sliderScalar(label: String, dataType: Int, v: LongArray, vMin: LongArray, vMax: LongArray, format: String): Boolean =
        Jni.sliderScalar(label, dataType, v, vMin, vMax, format)

    actual fun dragScalar(label: String, dataType: Int, v: LongArray, vSpeed: Float, vMin: LongArray, vMax: LongArray, format: String): Boolean =
        Jni.dragScalar(label, dataType, v, vSpeed, vMin, vMax, format)

    actual fun inputFloat(label: String, v: FloatArray, step: Float, stepFast: Float, format: String, flags: Int): Boolean =
        Jni.inputFloat(label, v, step, stepFast, format, flags)

    actual fun inputFloat2(label: String, v: FloatArray, format: String, flags: Int): Boolean =
        Jni.inputFloat2(label, v, format, flags)

    actual fun inputFloat3(label: String, v: FloatArray, format: String, flags: Int): Boolean =
        Jni.inputFloat3(label, v, format, flags)

    actual fun inputFloat4(label: String, v: FloatArray, format: String, flags: Int): Boolean =
        Jni.inputFloat4(label, v, format, flags)

    actual fun inputInt(label: String, v: IntArray, step: Int, stepFast: Int, flags: Int): Boolean =
        Jni.inputInt(label, v, step, stepFast, flags)

    actual fun inputInt2(label: String, v: IntArray, flags: Int): Boolean =
        Jni.inputInt2(label, v, flags)

    actual fun inputInt3(label: String, v: IntArray, flags: Int): Boolean =
        Jni.inputInt3(label, v, flags)

    actual fun inputInt4(label: String, v: IntArray, flags: Int): Boolean =
        Jni.inputInt4(label, v, flags)

    actual fun inputDouble(label: String, v: DoubleArray, step: Double, stepFast: Double, format: String, flags: Int): Boolean =
        Jni.inputDouble(label, v, step, stepFast, format, flags)

    actual fun colorEdit3(label: String, col: FloatArray, flags: Int): Boolean =
        Jni.colorEdit3(label, col, flags)

    actual fun colorEdit4(label: String, col: FloatArray, flags: Int): Boolean =
        Jni.colorEdit4(label, col, flags)

    actual fun colorPicker3(label: String, col: FloatArray, flags: Int): Boolean =
        Jni.colorPicker3(label, col, flags)

    actual fun colorPicker4(label: String, col: FloatArray, flags: Int): Boolean =
        Jni.colorPicker4(label, col, flags)

    actual fun colorButton(descId: String, col: ImVec4, flags: Int, size: ImVec2): Boolean =
        Jni.colorButton(descId, col.x, col.y, col.z, col.w, flags, size.x, size.y)

    actual fun setColorEditOptions(flags: Int) = Jni.setColorEditOptions(flags)

    actual fun colorConvertFloat4ToU32(`in`: ImVec4): Int =
        Jni.colorConvertFloat4ToU32(`in`.x, `in`.y, `in`.z, `in`.w)

    actual fun colorConvertU32ToFloat4(`in`: Int): ImVec4 {
        val c = Jni.colorConvertU32ToFloat4(`in`)
        return ImVec4(c[0], c[1], c[2], c[3])
    }

    actual fun colorConvertRGBtoHSV(r: Float, g: Float, b: Float, outH: FloatArray, outS: FloatArray, outV: FloatArray) =
        Jni.colorConvertRGBtoHSV(r, g, b, outH, outS, outV)

    actual fun colorConvertHSVtoRGB(h: Float, s: Float, v: Float, outR: FloatArray, outG: FloatArray, outB: FloatArray) =
        Jni.colorConvertHSVtoRGB(h, s, v, outR, outG, outB)

    actual fun inputText(label: String, buf: String, flags: Int): String? = Jni.inputText(label, buf, flags)
    actual fun inputTextMultiline(label: String, buf: String, size: ImVec2, flags: Int): String? =
        Jni.inputTextMultiline(label, buf, size.x, size.y, flags)

    actual fun inputTextWithHint(label: String, hint: String, buf: String, flags: Int): String? =
        Jni.inputTextWithHint(label, hint, buf, flags)
    actual fun combo(label: String, currentItem: IntArray, items: Array<String>): Boolean =
        Jni.combo(label, currentItem, items)

    actual fun selectable(label: String, selected: Boolean, flags: Int, size: ImVec2): Boolean =
        Jni.selectable(label, selected, flags, size.x, size.y)

    actual fun radioButton(label: String, active: Boolean): Boolean = Jni.radioButton(label, active)
    actual fun progressBar(fraction: Float, size: ImVec2, overlay: String?) =
        Jni.progressBar(fraction, size.x, size.y, overlay)

    actual fun collapsingHeader(label: String, flags: Int): Boolean = Jni.collapsingHeader(label, flags)
    actual fun treeNode(label: String): Boolean = Jni.treeNode(label)
    actual fun treeNodeEx(label: String, flags: Int): Boolean = Jni.treeNodeEx(label, flags)
    actual fun treeNodeGetOpen(label: String): Boolean = Jni.treeNodeGetOpen(label)
    actual fun treePush(strId: String?) = Jni.treePush(strId)
    actual fun treePop() = Jni.treePop()
    actual fun invisibleButton(id: String, size: ImVec2, flags: Int): Boolean = Jni.invisibleButton(id, size.x, size.y, flags)
    actual fun beginGroup() = Jni.beginGroup()
    actual fun endGroup() = Jni.endGroup()
    actual fun setCursorPos(pos: ImVec2) = Jni.setCursorPos(pos.x, pos.y)
    actual fun pushId(id: String) = Jni.pushId(id)
    actual fun popId() = Jni.popId()

    // ---- Item queries ----
    actual fun isItemHovered(flags: Int): Boolean = Jni.isItemHovered(flags)
    actual fun isItemActive(): Boolean = Jni.isItemActive()
    actual fun isItemClicked(mouseButton: Int): Boolean = Jni.isItemClicked(mouseButton)
    actual fun isWindowHovered(flags: Int): Boolean = Jni.isWindowHovered(flags)
    actual fun isWindowFocused(flags: Int): Boolean = Jni.isWindowFocused(flags)

    // ---- State queries ----
    actual fun isItemFocused(): Boolean = Jni.isItemFocused()
    actual fun isItemVisible(): Boolean = Jni.isItemVisible()
    actual fun isItemEdited(): Boolean = Jni.isItemEdited()
    actual fun isItemActivated(): Boolean = Jni.isItemActivated()
    actual fun isItemDeactivated(): Boolean = Jni.isItemDeactivated()
    actual fun isItemDeactivatedAfterEdit(): Boolean = Jni.isItemDeactivatedAfterEdit()
    actual fun isItemToggledOpen(): Boolean = Jni.isItemToggledOpen()
    actual fun isItemToggledSelection(): Boolean = Jni.isItemToggledSelection()
    actual fun isAnyItemHovered(): Boolean = Jni.isAnyItemHovered()
    actual fun isAnyItemActive(): Boolean = Jni.isAnyItemActive()
    actual fun isAnyItemFocused(): Boolean = Jni.isAnyItemFocused()
    actual fun getItemID(): Int = Jni.getItemID()
    actual fun getItemFlags(): Int = Jni.getItemFlags()

    actual fun getItemRectMin(): ImVec2 {
        val v = Jni.getItemRectMin()
        return ImVec2(v[0], v[1])
    }

    actual fun getItemRectMax(): ImVec2 {
        val v = Jni.getItemRectMax()
        return ImVec2(v[0], v[1])
    }

    actual fun getItemRectSize(): ImVec2 {
        val v = Jni.getItemRectSize()
        return ImVec2(v[0], v[1])
    }

    actual fun isWindowAppearing(): Boolean = Jni.isWindowAppearing()
    actual fun isWindowCollapsed(): Boolean = Jni.isWindowCollapsed()
    actual fun isRectVisible(size: ImVec2): Boolean = Jni.isRectVisible(size.x, size.y)
    actual fun isPopupOpen(strId: String, flags: Int): Boolean = Jni.isPopupOpen(strId, flags)

    actual fun getWindowPos(): ImVec2 {
        val v = Jni.getWindowPos()
        return ImVec2(v[0], v[1])
    }

    actual fun getWindowSize(): ImVec2 {
        val v = Jni.getWindowSize()
        return ImVec2(v[0], v[1])
    }

    actual fun getWindowWidth(): Float = Jni.getWindowWidth()
    actual fun getWindowHeight(): Float = Jni.getWindowHeight()

    actual fun getWindowContentRegionMax(): ImVec2 {
        val v = Jni.getWindowContentRegionMax()
        return ImVec2(v[0], v[1])
    }

    actual fun getWindowContentRegionMin(): ImVec2 {
        val v = Jni.getWindowContentRegionMin()
        return ImVec2(v[0], v[1])
    }

    actual fun getWindowDrawList(): ImDrawList = JvmImDrawList(Jni.getWindowDrawList())
    actual fun getForegroundDrawList(): ImDrawList = JvmImDrawList(Jni.getForegroundDrawList())
    actual fun getBackgroundDrawList(): ImDrawList = JvmImDrawList(Jni.getBackgroundDrawList())
    actual fun isKeyDown(key: Int): Boolean = Jni.isKeyDown(key)
    actual fun isKeyPressed(key: Int, repeat: Boolean): Boolean = Jni.isKeyPressed(key, repeat)
    actual fun isKeyReleased(key: Int): Boolean = Jni.isKeyReleased(key)
    actual fun isMouseDown(button: Int): Boolean = Jni.isMouseDown(button)
    actual fun isMouseClicked(button: Int, repeat: Boolean): Boolean = Jni.isMouseClicked(button, repeat)
    actual fun isMouseReleased(button: Int): Boolean = Jni.isMouseReleased(button)
    actual fun isMouseDoubleClicked(button: Int): Boolean = Jni.isMouseDoubleClicked(button)
    actual fun isMouseDragging(button: Int, lockThreshold: Float): Boolean = Jni.isMouseDragging(button, lockThreshold)
    actual fun isAnyMouseDown(): Boolean = Jni.isAnyMouseDown()
    actual fun isMousePosValid(mousePos: ImVec2?): Boolean = Jni.isMousePosValid(mousePos != null, mousePos?.x ?: 0f, mousePos?.y ?: 0f)

    actual fun getMousePos(): ImVec2 {
        val v = Jni.getMousePos()
        return ImVec2(v[0], v[1])
    }

    actual fun getMouseDragDelta(button: Int, lockThreshold: Float): ImVec2 {
        val v = Jni.getMouseDragDelta(button, lockThreshold)
        return ImVec2(v[0], v[1])
    }

    actual fun resetMouseDragDelta(button: Int) = Jni.resetMouseDragDelta(button)
    actual fun getMouseCursor(): Int = Jni.getMouseCursor()
    actual fun setMouseCursor(cursor: Int) = Jni.setMouseCursor(cursor)
    actual fun setKeyboardFocusHere(offset: Int) = Jni.setKeyboardFocusHere(offset)
    actual fun setNextFrameWantCaptureKeyboard(wantCaptureKeyboard: Boolean) = Jni.setNextFrameWantCaptureKeyboard(wantCaptureKeyboard)
    actual fun setNextFrameWantCaptureMouse(wantCaptureMouse: Boolean) = Jni.setNextFrameWantCaptureMouse(wantCaptureMouse)
    actual fun setClipboardText(text: String) = Jni.setClipboardText(text)
    actual fun getClipboardText(): String? = Jni.getClipboardText()

    actual fun setClipboardFunctions(setText: ((String) -> Unit)?, getText: (() -> String?)?) {
        clipboardBridgeSetText = setText
        clipboardBridgeGetText = getText
        Jni.setClipboardFunctions()
    }

    actual fun getCursorPos(): ImVec2 {
        val v = Jni.getCursorPos()
        return ImVec2(v[0], v[1])
    }

    actual fun getCursorScreenPos(): ImVec2 {
        val v = Jni.getCursorScreenPos()
        return ImVec2(v[0], v[1])
    }

    actual fun getCursorStartPos(): ImVec2 {
        val v = Jni.getCursorStartPos()
        return ImVec2(v[0], v[1])
    }

    actual fun setCursorPosX(localX: Float) = Jni.setCursorPosX(localX)
    actual fun setCursorScreenPos(pos: ImVec2) = Jni.setCursorScreenPos(pos.x, pos.y)

    actual fun getContentRegionAvail(): ImVec2 {
        val v = Jni.getContentRegionAvail()
        return ImVec2(v[0], v[1])
    }

    actual fun getScrollX(): Float = Jni.getScrollX()
    actual fun getScrollY(): Float = Jni.getScrollY()
    actual fun getScrollMaxX(): Float = Jni.getScrollMaxX()
    actual fun getScrollMaxY(): Float = Jni.getScrollMaxY()
    actual fun setScrollHereX(centerXRatio: Float) = Jni.setScrollHereX(centerXRatio)
    actual fun setScrollHereY(centerYRatio: Float) = Jni.setScrollHereY(centerYRatio)
    actual fun setScrollFromPosX(localX: Float, centerXRatio: Float) = Jni.setScrollFromPosX(localX, centerXRatio)
    actual fun setScrollFromPosY(localY: Float, centerYRatio: Float) = Jni.setScrollFromPosY(localY, centerYRatio)
    actual fun setScrollX(scrollX: Float) = Jni.setScrollX(scrollX)
    actual fun setScrollY(scrollY: Float) = Jni.setScrollY(scrollY)
    actual fun getFrameCount(): Int = Jni.getFrameCount()
    actual fun getFrameHeight(): Float = Jni.getFrameHeight()
    actual fun getFrameHeightWithSpacing(): Float = Jni.getFrameHeightWithSpacing()
    actual fun getFontSize(): Float = Jni.getFontSize()
    actual fun getFont(): Long = Jni.getFont()
    actual fun getMainViewport(): Long = Jni.getMainViewport()
    actual fun getStyleColorVec4(idx: Int): ImVec4 {
        val v = Jni.getStyleColorVec4(idx)
        return ImVec4(v[0], v[1], v[2], v[3])
    }

    actual fun getCursorPosX(): Float = Jni.getCursorPosX()
    actual fun getKeyName(key: Int): String = Jni.getKeyName(key)
    actual fun getTextLineHeight(): Float = Jni.getTextLineHeight()
    actual fun getTextLineHeightWithSpacing(): Float = Jni.getTextLineHeightWithSpacing()
    actual fun getID(strId: String): Int = Jni.getID(strId)
    actual fun getColorU32(idx: Int, alphaMul: Float): Int = Jni.getColorU32(idx, alphaMul)
    actual fun getStyleColorName(idx: Int): String = Jni.getStyleColorName(idx)

    actual fun calcTextSize(text: String, hideTextAfterDoubleHash: Boolean, wrapWidth: Float): ImVec2 {
        val v = Jni.calcTextSize(text, hideTextAfterDoubleHash, wrapWidth)
        return ImVec2(v[0], v[1])
    }

    actual fun calcItemWidth(): Float = Jni.calcItemWidth()
    actual fun getTime(): Double = Jni.getTime()

    // ---- Columns (legacy multi-column layout) ----
    actual fun columns(count: Int, id: String?, border: Boolean) = Jni.columns(count, id, border)
    actual fun nextColumn() = Jni.nextColumn()
    actual fun getColumnIndex(): Int = Jni.getColumnIndex()
    actual fun getColumnOffset(columnIndex: Int): Float = Jni.getColumnOffset(columnIndex)
    actual fun setColumnOffset(columnIndex: Int, offsetX: Float) = Jni.setColumnOffset(columnIndex, offsetX)
    actual fun getColumnWidth(columnIndex: Int): Float = Jni.getColumnWidth(columnIndex)
    actual fun setColumnWidth(columnIndex: Int, width: Float) = Jni.setColumnWidth(columnIndex, width)
    actual fun getColumnsCount(): Int = Jni.getColumnsCount()

    // ---- Tables ----
    actual fun beginTable(id: String, column: Int, flags: Int, outerSize: ImVec2, innerWidth: Float): Boolean =
        Jni.beginTable(id, column, flags, outerSize.x, outerSize.y, innerWidth)

    actual fun endTable() = Jni.endTable()
    actual fun tableNextRow(minRowHeight: Int, flags: Int) = Jni.tableNextRow(minRowHeight, flags)
    actual fun tableNextColumn(): Boolean = Jni.tableNextColumn()
    actual fun tableSetColumnIndex(columnIndex: Int): Boolean = Jni.tableSetColumnIndex(columnIndex)
    actual fun tableSetupColumn(label: String, flags: Int, initWidthOrWeight: Float, userId: Int) =
        Jni.tableSetupColumn(label, flags, initWidthOrWeight, userId)

    actual fun tableSetupScrollFreeze(cols: Int, rows: Int) = Jni.tableSetupScrollFreeze(cols, rows)
    actual fun tableHeadersRow() = Jni.tableHeadersRow()
    actual fun tableHeader(label: String) = Jni.tableHeader(label)
    actual fun tableAngledHeadersRow() = Jni.tableAngledHeadersRow()
    actual fun tableGetColumnCount(): Int = Jni.tableGetColumnCount()
    actual fun tableGetColumnFlags(columnN: Int): Int = Jni.tableGetColumnFlags(columnN)
    actual fun tableGetColumnIndex(): Int = Jni.tableGetColumnIndex()
    actual fun tableGetRowIndex(): Int = Jni.tableGetRowIndex()
    actual fun tableGetColumnName(columnN: Int): String = Jni.tableGetColumnName(columnN)
    actual fun tableGetSortSpecs(): Long = Jni.tableGetSortSpecs()
    actual fun tableSetBgColor(target: Int, color: Int, columnN: Int) = Jni.tableSetBgColor(target, color, columnN)
    actual fun tabItemButton(label: String, flags: Int): Boolean = Jni.tabItemButton(label, flags)

    // ---- Style ----
    actual fun styleColorsDark() = Jni.styleColorsDark()
    actual fun styleColorsLight() = Jni.styleColorsLight()
    actual fun styleColorsClassic() = Jni.styleColorsClassic()
    actual fun showStyleSelector(label: String): Boolean = Jni.showStyleSelector(label)
    actual fun showFontSelector(label: String) = Jni.showFontSelector(label)
    actual fun showStyleEditor() = Jni.showStyleEditor()
    actual fun pushStyleColor(idx: Int, color: ImVec4) = Jni.pushStyleColorVec4(idx, color.x, color.y, color.z, color.w)
    actual fun popStyleColor(count: Int) = Jni.popStyleColor(count)
    actual fun pushStyleVarFloat(idx: Int, value: Float) = Jni.pushStyleVarFloat(idx, value)
    actual fun pushStyleVarVec2(idx: Int, value: ImVec2) = Jni.pushStyleVarVec2(idx, value.x, value.y)
    actual fun popStyleVar(count: Int) = Jni.popStyleVar(count)
    actual fun pushFont(font: ImFont) = Jni.pushFont((font as JvmImFont).ptr)
    actual fun popFont() = Jni.popFont()
    actual fun pushItemWidth(width: Float) = Jni.pushItemWidth(width)
    actual fun popItemWidth() = Jni.popItemWidth()
    actual fun setNextItemWidth(width: Float) = Jni.setNextItemWidth(width)

    // ---- SetNext* layout / item state ----
    actual fun setNextItemOpen(isOpen: Boolean, cond: Int) = Jni.setNextItemOpen(isOpen, cond)
    actual fun setNextItemAllowOverlap() = Jni.setNextItemAllowOverlap()
    actual fun setNextItemSelectionUserData(selectionUserData: Long) = Jni.setNextItemSelectionUserData(selectionUserData)
    actual fun setNextItemShortcut(keyChord: Int, flags: Int) = Jni.setNextItemShortcut(keyChord, flags)
    actual fun setNextWindowCollapsed(collapsed: Boolean, cond: Int) = Jni.setNextWindowCollapsed(collapsed, cond)
    actual fun setNextWindowContentSize(size: ImVec2) = Jni.setNextWindowContentSize(size.x, size.y)
    actual fun setNextWindowFocus() = Jni.setNextWindowFocus()
    actual fun setNextWindowScroll(scroll: ImVec2) = Jni.setNextWindowScroll(scroll.x, scroll.y)
    actual fun setNextWindowSizeConstraints(sizeMin: ImVec2, sizeMax: ImVec2, customCallback: (() -> Unit)?) =
        Jni.setNextWindowSizeConstraints(sizeMin.x, sizeMin.y, sizeMax.x, sizeMax.y)

    actual fun setItemTooltip(text: String) = Jni.setItemTooltip(text)
    actual fun setItemDefaultFocus() = Jni.setItemDefaultFocus()
    actual fun setTabItemClosed(tabOrDockedWindowLabel: String) = Jni.setTabItemClosed(tabOrDockedWindowLabel)
}
