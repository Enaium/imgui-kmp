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
    external fun getIO(): Long
    external fun getStyle(): Long
    external fun newFrame()
    external fun render()
    external fun getDrawData(): Long
    external fun getVersion(): String
    external fun showDemoWindow(pOpen: BooleanArray?)

    // ---- Windows ----
    external fun begin(name: String, pOpen: BooleanArray?, flags: Int): Boolean
    external fun end()
    external fun beginChild(strId: String, sizeX: Float, sizeY: Float, childFlags: Int, windowFlags: Int): Boolean
    external fun endChild()
    external fun setNextWindowPos(x: Float, y: Float, cond: Int, pivotX: Float, pivotY: Float)
    external fun setNextWindowSize(w: Float, h: Float, cond: Int)
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
    external fun beginCombo(label: String, previewValue: String, flags: Int): Boolean
    external fun endCombo()

    // ---- Widgets ----
    external fun text(text: String)
    external fun textColored(r: Float, g: Float, b: Float, a: Float, text: String)
    external fun textDisabled(text: String)
    external fun labelText(label: String, text: String)
    external fun bulletText(text: String)
    external fun bullet()
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
    external fun checkbox(label: String, v: BooleanArray): Boolean
    external fun sliderFloat(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String): Boolean
    external fun sliderInt(label: String, v: IntArray, vMin: Int, vMax: Int, format: String): Boolean
    external fun inputText(label: String, buf: String, flags: Int): String?
    external fun combo(label: String, currentItem: IntArray, items: Array<String>): Boolean
    external fun selectable(label: String, selected: Boolean, flags: Int, w: Float, h: Float): Boolean
    external fun radioButton(label: String, active: Boolean): Boolean
    external fun progressBar(fraction: Float, w: Float, h: Float, overlay: String?)
    external fun collapsingHeader(label: String, flags: Int): Boolean
    external fun treeNode(label: String): Boolean
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

    // ---- Tables ----
    external fun beginTable(strId: String, column: Int, flags: Int, outerW: Float, outerH: Float, innerWidth: Float): Boolean
    external fun endTable()
    external fun tableNextRow(minRowHeight: Int, flags: Int)
    external fun tableNextColumn(): Boolean
    external fun tableSetColumnIndex(columnN: Int): Boolean
    external fun tableSetupColumn(label: String, flags: Int, initWidthOrWeight: Float, userId: Int)
    external fun tableSetupScrollFreeze(cols: Int, rows: Int)
    external fun tableHeadersRow()

    // ---- Style ----
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
        get() = error("displaySize is write-only; set it before NewFrame")
        set(value) = Jni.ioSetDisplaySize(ptr, value.x, value.y)

    override var displayFramebufferScale: ImVec2
        get() = error("displayFramebufferScale is write-only; set it before NewFrame")
        set(value) = Jni.ioSetDisplayFramebufferScale(ptr, value.x, value.y)

    override var deltaTime: Float
        get() = error("deltaTime is write-only; set it before NewFrame")
        set(value) = Jni.ioSetDeltaTime(ptr, value)

    override var configFlags: Int
        get() = error("configFlags is write-only; set it before NewFrame")
        set(value) = Jni.ioSetConfigFlags(ptr, value)

    override var backendFlags: Int
        get() = error("backendFlags is write-only; set it before NewFrame")
        set(value) = Jni.ioSetBackendFlags(ptr, value)

    override var iniFilename: String?
        get() = error("iniFilename is write-only")
        set(value) = Jni.ioSetIniFilename(ptr, value)

    override var fontGlobalScale: Float
        get() = error("fontGlobalScale is write-only")
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

    actual fun newFrame() = Jni.newFrame()
    actual fun render() = Jni.render()
    actual fun getDrawData(): ImDrawData = JvmImDrawData(Jni.getDrawData())
    actual fun getIO(): ImGuiIO = JvmImGuiIO(Jni.getIO())
    actual fun getStyle(): ImGuiStyle = JvmImGuiStyle(Jni.getStyle())
    actual fun getVersion(): String = Jni.getVersion()
    actual fun showDemoWindow(pOpen: BooleanArray?) = Jni.showDemoWindow(pOpen)

    // ---- Windows ----
    actual fun begin(name: String, pOpen: BooleanArray?, flags: Int): Boolean = Jni.begin(name, pOpen, flags)
    actual fun end() = Jni.end()
    actual fun beginChild(id: String, size: ImVec2, childFlags: Int, windowFlags: Int): Boolean =
        Jni.beginChild(id, size.x, size.y, childFlags, windowFlags)

    actual fun endChild() = Jni.endChild()
    actual fun setNextWindowPos(pos: ImVec2, cond: Int, pivot: ImVec2?) =
        Jni.setNextWindowPos(pos.x, pos.y, cond, pivot?.x ?: 0f, pivot?.y ?: 0f)

    actual fun setNextWindowSize(size: ImVec2, cond: Int) = Jni.setNextWindowSize(size.x, size.y, cond)
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
    actual fun beginCombo(label: String, previewValue: String, flags: Int): Boolean = Jni.beginCombo(label, previewValue, flags)
    actual fun endCombo() = Jni.endCombo()

    // ---- Widgets ----
    actual fun text(text: String) = Jni.text(text)
    actual fun textColored(color: ImVec4, text: String) = Jni.textColored(color.x, color.y, color.z, color.w, text)
    actual fun textDisabled(text: String) = Jni.textDisabled(text)
    actual fun labelText(label: String, text: String) = Jni.labelText(label, text)
    actual fun bulletText(text: String) = Jni.bulletText(text)
    actual fun bullet() = Jni.bullet()
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
    actual fun checkbox(label: String, v: BooleanArray): Boolean = Jni.checkbox(label, v)
    actual fun sliderFloat(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String): Boolean =
        Jni.sliderFloat(label, v, vMin, vMax, format)

    actual fun sliderInt(label: String, v: IntArray, vMin: Int, vMax: Int, format: String): Boolean =
        Jni.sliderInt(label, v, vMin, vMax, format)

    actual fun inputText(label: String, buf: String, flags: Int): String? = Jni.inputText(label, buf, flags)
    actual fun combo(label: String, currentItem: IntArray, items: Array<String>): Boolean =
        Jni.combo(label, currentItem, items)

    actual fun selectable(label: String, selected: Boolean, flags: Int, size: ImVec2): Boolean =
        Jni.selectable(label, selected, flags, size.x, size.y)

    actual fun radioButton(label: String, active: Boolean): Boolean = Jni.radioButton(label, active)
    actual fun progressBar(fraction: Float, size: ImVec2, overlay: String?) =
        Jni.progressBar(fraction, size.x, size.y, overlay)

    actual fun collapsingHeader(label: String, flags: Int): Boolean = Jni.collapsingHeader(label, flags)
    actual fun treeNode(label: String): Boolean = Jni.treeNode(label)
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

    // ---- Style ----
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
}
