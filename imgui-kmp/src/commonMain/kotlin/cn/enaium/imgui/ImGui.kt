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

/** A 2D vector, mirroring Dear ImGui's ImVec2. */
data class ImVec2(val x: Float, val y: Float)

/** A 4-component vector, mirroring Dear ImGui's ImVec4. */
data class ImVec4(val x: Float, val y: Float, val z: Float, val w: Float)

/** Raw vertex data copied out of an [ImDrawList] vertex buffer. */
class ImDrawVertData {
    /** Interleaved x/y positions, 2 floats per vertex. */
    val positions = ArrayList<Float>()

    /** Interleaved u/v texture coordinates, 2 floats per vertex. */
    val uvs = ArrayList<Float>()

    /** Packed 0xRRGGBBAA colors, 1 int per vertex. */
    val colors = ArrayList<Int>()
}

/** The font atlas pixel data uploaded by a renderer backend. */
data class FontTexData(
    val pixels: ByteArray,
    val width: Int,
    val height: Int,
    val bytesPerPixel: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FontTexData) return false
        return pixels.contentEquals(other.pixels) &&
            width == other.width &&
            height == other.height &&
            bytesPerPixel == other.bytesPerPixel
    }

    override fun hashCode(): Int {
        var result = pixels.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + bytesPerPixel
        return result
    }
}

/** A single draw command within an [ImDrawList]. */
interface ImDrawCmd {
    /** Clipping rectangle (x1, y1, x2, y2) in display coordinates. */
    val clipRect: ImVec4

    /** The backend texture id the command samples. */
    val texId: Long

    /** Start offset in the draw list's vertex buffer. */
    val vtxOffset: Int

    /** Start offset in the draw list's index buffer. */
    val idxOffset: Int

    /** Number of indices (multiple of 3) to render. */
    val elemCount: Int

    /** Whether the command carries a user callback instead of draw data. */
    val hasUserCallback: Boolean
}

/** A single mesh builder, one per window. */
interface ImDrawList {
    val vtxCount: Int
    val idxCount: Int
    val cmdCount: Int

    fun cmd(index: Int): ImDrawCmd

    /**
     * Copies [count] vertices starting at [vtxOffset] into the returned
     * buffer. Used by renderer backends; vertices are 20 bytes each
     * (pos 2x float, uv 2x float, color uint32).
     */
    fun copyVtx(vtxOffset: Int, count: Int): ImDrawVertData

    /** Copies [count] 16-bit indices starting at [idxOffset] into an IntArray. */
    fun copyIdx(idxOffset: Int, count: Int): IntArray

    // ==================== Draw primitives ====================
    fun DrawLine(p1: ImVec2, p2: ImVec2, col: Int, thickness: Float = 1f)
    fun DrawRect(pMin: ImVec2, pMax: ImVec2, col: Int, rounding: Float = 0f, flags: Int = 0, thickness: Float = 1f)
    fun DrawRectFilled(pMin: ImVec2, pMax: ImVec2, col: Int, rounding: Float = 0f, flags: Int = 0)
    fun DrawCircle(center: ImVec2, radius: Float, col: Int, numSegments: Int = 0, thickness: Float = 1f)
    fun DrawCircleFilled(center: ImVec2, radius: Float, col: Int, numSegments: Int = 0)
    fun DrawText(pos: ImVec2, text: String, col: Int = 0xFFFFFFFF.toInt())
    fun DrawQuad(p1: ImVec2, p2: ImVec2, p3: ImVec2, p4: ImVec2, col: Int, thickness: Float = 1f)
    fun DrawTriangle(p1: ImVec2, p2: ImVec2, p3: ImVec2, col: Int, thickness: Float = 1f)
    fun DrawPolyline(points: Array<ImVec2>, col: Int, closed: Boolean = false, thickness: Float = 1f)
}

/** All draw data of a rendered frame, obtained after [ImGui.render]. */
interface ImDrawData {
    val displayPos: ImVec2
    val displaySize: ImVec2
    val framebufferScale: ImVec2
    val cmdListsCount: Int

    fun cmdList(index: Int): ImDrawList
}

/** A font loaded into the font atlas. */
interface ImFont

/**
 * Configuration for adding a font to an [ImFontAtlas]. Mirrors the
 * relevant fields of Dear ImGui's `ImFontConfig`. Defaults match the
 * C++ struct (0 / false / 1.0), with [sizePixels] <= 0 meaning "use the
 * font's default" (13f for the built-in default font).
 */
class ImFontConfig(
    /** Font name, mainly for debugging; null lets imgui pick one. */
    var name: String? = null,
    /** Merge into the previous font (combine multiple sources in one font). */
    var mergeMode: Boolean = false,
    /** Align every glyph advance to pixel boundaries. */
    var pixelSnapH: Boolean = false,
    /** Rasterization oversampling (0 = auto). */
    var oversampleH: Int = 0,
    var oversampleV: Int = 0,
    /** Output size in logical pixels; <= 0 uses the font default (13f). */
    var sizePixels: Float = 0f,
    /** Offset (px) applied to all glyphs of this source. */
    var glyphOffsetX: Float = 0f,
    var glyphOffsetY: Float = 0f,
    /** Minimum / maximum advance (px); set both for a monospace font. */
    var glyphMinAdvanceX: Float = 0f,
    var glyphMaxAdvanceX: Float = Float.MAX_VALUE,
    /** Linearly brighten (>1) or darken (<1) the rasterized output. */
    var rasterizerMultiply: Float = 1f,
    /** DPI density multiplier for rasterization: the atlas is baked at
     *  `sizePixels * rasterizerDensity` physical px while the logical
     *  metrics stay [sizePixels] (keeps text crisp on high-DPI displays). */
    var rasterizerDensity: Float = 1f,
    /** Extra rasterizer scale over [sizePixels]. */
    var extraSizeScale: Float = 1f,
)

interface ImFontAtlas {
    /**
     * Adds the embedded default font (ProggyClean) with default config.
     * See [addFontDefault] for the config-driven variant.
     */
    fun addFontDefault(): ImFont = addFontDefault(ImFontConfig())

    /**
     * Adds the embedded default font using [config]. Pass
     * `config.sizePixels = 13f * dpi, config.rasterizerDensity = 2f` to
     * bake a crisp high-DPI atlas without shipping a TTF.
     */
    fun addFontDefault(config: ImFontConfig): ImFont

    /** Convenience for [addFontFromFileTTF] with rasterizer density 1.0. */
    fun addFontFromFileTTF(path: String, sizePx: Float): ImFont =
        addFontFromFileTTF(path, ImFontConfig().apply { sizePixels = sizePx })

    /** Convenience: [addFontFromFileTTF] with only size + density set. */
    fun addFontFromFileTTF(path: String, sizePx: Float, rasterizerDensity: Float): ImFont =
        addFontFromFileTTF(path, ImFontConfig().apply {
            this.sizePixels = sizePx
            this.rasterizerDensity = rasterizerDensity
        })

    /** Loads [path] with the full [ImFontConfig]. */
    fun addFontFromFileTTF(path: String, config: ImFontConfig): ImFont

    fun build(): Boolean
    fun getTexDataAsRGBA32(): FontTexData
    fun setTexID(id: Long)
}

/** The per-context IO structure; carries input events and display settings. */
interface ImGuiIO {
    var displaySize: ImVec2
    var displayFramebufferScale: ImVec2
    var deltaTime: Float
    var configFlags: Int

    /** Renderer capabilities, see [ImGuiBackendFlags]. */
    var backendFlags: Int

    /** The .ini settings path, or null to disable persistence. */
    var iniFilename: String?
    var fontGlobalScale: Float

    val fonts: ImFontAtlas

    fun addMousePosEvent(x: Float, y: Float)
    fun addMouseButtonEvent(button: Int, down: Boolean)
    fun addMouseWheelEvent(wheelX: Float, wheelY: Float)
    fun addKeyEvent(key: Int, down: Boolean)
    fun addInputCharacter(c: UInt)
    fun addInputCharactersUTF8(text: String)

    val wantCaptureMouse: Boolean
    val wantCaptureKeyboard: Boolean
    val wantTextInput: Boolean
}

/** The style of the current context. */
interface ImGuiStyle {
    fun getColor(idx: Int): ImVec4
    fun setColor(idx: Int, color: ImVec4)
}

/** An ImGui context; close() calls [ImGui.destroyContext]. */
interface ImGuiContext : AutoCloseable

/** Kotlin bindings for Dear ImGui's core API. */
expect object ImGui {
    fun createContext(): ImGuiContext
    fun destroyContext(context: ImGuiContext? = null)
    fun getCurrentContext(): ImGuiContext?
    fun setCurrentContext(context: ImGuiContext)
    fun newFrame()
    fun render()
    fun getDrawData(): ImDrawData
    fun getIO(): ImGuiIO
    fun getStyle(): ImGuiStyle
    fun getVersion(): String
    fun showDemoWindow(pOpen: BooleanArray? = null)
    fun showAboutWindow(pOpen: BooleanArray? = null)
    fun showMetricsWindow(pOpen: BooleanArray? = null)
    fun showDebugLogWindow(pOpen: BooleanArray? = null)
    fun showUserGuide()
    fun showIDStackToolWindow(pOpen: BooleanArray? = null)

    // ==================== Windows ====================
    fun begin(name: String, pOpen: BooleanArray? = null, flags: Int = 0): Boolean
    fun end()
    fun beginChild(id: String, size: ImVec2 = ImVec2(0f, 0f), childFlags: Int = 0, windowFlags: Int = 0): Boolean
    fun endChild()
    fun setNextWindowPos(pos: ImVec2, cond: Int = ImGuiCond.ALWAYS, pivot: ImVec2? = null)
    fun setNextWindowSize(size: ImVec2, cond: Int = ImGuiCond.ALWAYS)
    fun setWindowSize(size: ImVec2, cond: Int = ImGuiCond.ALWAYS)
    fun setNextWindowBgAlpha(alpha: Float)
    fun beginDisabled(disabled: Boolean = true)
    fun endDisabled()
    fun beginMainMenuBar(): Boolean
    fun endMainMenuBar()
    fun beginMenuBar(): Boolean
    fun endMenuBar()
    fun beginMenu(label: String, enabled: Boolean = true): Boolean
    fun endMenu()
    fun menuItem(label: String, shortcut: String = "", selected: Boolean = false, enabled: Boolean = true): Boolean
    fun beginTabBar(id: String, flags: Int = 0): Boolean
    fun endTabBar()
    fun beginTabItem(label: String, pOpen: BooleanArray? = null, flags: Int = 0): Boolean
    fun endTabItem()
    fun beginTooltip(): Boolean
    fun endTooltip()
    fun setTooltip(text: String)
    fun openPopup(id: String, popupFlags: Int = 0)
    fun beginPopup(id: String, flags: Int = 0): Boolean
    fun beginPopupModal(name: String, pOpen: BooleanArray? = null, flags: Int = 0): Boolean
    fun endPopup()
    fun closeCurrentPopup()
    fun beginPopupContextItem(strId: String? = null, popupFlags: Int = ImGuiPopupFlags.MOUSE_BUTTON_RIGHT): Boolean
    fun beginPopupContextWindow(strId: String? = null, popupFlags: Int = ImGuiPopupFlags.MOUSE_BUTTON_RIGHT): Boolean
    fun beginItemTooltip(): Boolean
    fun openPopupOnItemClick(strId: String? = null, popupFlags: Int = ImGuiPopupFlags.MOUSE_BUTTON_RIGHT)

    // ==================== Drag and drop ====================
    fun beginDragDropSource(flags: Int = 0): Boolean
    fun setDragDropPayload(type: String, data: ByteArray, cond: Int = ImGuiCond.ONCE): Boolean
    fun endDragDropSource()
    fun beginDragDropTarget(): Boolean
    fun acceptDragDropPayload(type: String, flags: Int = 0): ByteArray?
    fun endDragDropTarget()
    fun getDragDropPayload(): String?

    // ==================== Images ====================
    fun image(
        texId: Long,
        size: ImVec2,
        uv0: ImVec2 = ImVec2(0f, 0f),
        uv1: ImVec2 = ImVec2(1f, 1f),
        tintColor: ImVec4 = ImVec4(1f, 1f, 1f, 1f),
        borderColor: ImVec4 = ImVec4(0f, 0f, 0f, 0f),
    )

    fun imageWithBg(
        texId: Long,
        size: ImVec2,
        bgColor: ImVec4,
        uv0: ImVec2 = ImVec2(0f, 0f),
        uv1: ImVec2 = ImVec2(1f, 1f),
    )

    fun imageButton(
        texId: Long,
        size: ImVec2,
        uv0: ImVec2 = ImVec2(0f, 0f),
        uv1: ImVec2 = ImVec2(1f, 1f),
        framePadding: Int = -1,
        bgColor: ImVec4 = ImVec4(0f, 0f, 0f, 0f),
        tintColor: ImVec4 = ImVec4(1f, 1f, 1f, 1f),
    ): Boolean

    // ==================== ListBox ====================
    fun beginListBox(label: String, size: ImVec2 = ImVec2(0f, 0f)): Boolean
    fun endListBox()
    fun listBox(label: String, currentItem: IntArray, items: Array<String>): Boolean

    // ==================== MultiSelect ====================
    fun beginMultiSelect(flags: Int, selectionSize: Int = -1, itemsCount: Int = -1): Long
    fun endMultiSelect(): Long

    // ==================== Logging ====================
    fun logToClipboard(autoOpenDepth: Int = -1)
    fun logToFile(autoOpenDepth: Int = -1, filename: String? = null)
    fun logToTTY(autoOpenDepth: Int = -1)
    fun logFinish()
    fun logText(text: String)

    // ==================== .ini settings ====================
    fun saveIniSettingsToDisk(iniFilename: String? = null)
    fun loadIniSettingsFromDisk(iniFilename: String? = null)
    fun saveIniSettingsToMemory(): String?
    fun loadIniSettingsFromMemory(iniData: String)

    // ==================== Scissor rect / text wrapping ====================
    fun pushClipRect(clipRectMin: ImVec2, clipRectMax: ImVec2, intersectWithCurrentClipRect: Boolean = false)
    fun popClipRect()
    fun pushTextWrapPos(wrapLocalPosX: Float = 0f)
    fun popTextWrapPos()

    // ==================== Widgets ====================
    fun text(text: String)
    fun textWrapped(text: String)
    fun textUnformatted(text: String)
    fun textLink(text: String): Boolean
    fun textLinkOpenURL(label: String, url: String? = null): Boolean
    fun textColored(color: ImVec4, text: String)
    fun textDisabled(text: String)
    fun labelText(label: String, text: String)
    fun bulletText(text: String)
    fun bullet()
    fun alignTextToFramePadding()
    fun separator()
    fun separatorText(text: String)
    fun sameLine(offsetFromStartX: Float = 0f, spacing: Float = -1f)
    fun newLine()
    fun spacing()
    fun dummy(size: ImVec2)
    fun indent(indentW: Float = 0f)
    fun unindent(indentW: Float = 0f)
    fun button(label: String, size: ImVec2 = ImVec2(0f, 0f)): Boolean
    fun smallButton(label: String): Boolean
    fun arrowButton(strId: String, dir: Int): Boolean
    fun checkbox(label: String, v: BooleanArray): Boolean
    fun checkboxFlags(label: String, flags: IntArray, flagsValue: Int): Boolean
    fun pushItemFlag(flag: Int, enabled: Boolean)
    fun popItemFlag()
    fun shortcut(keyChord: Int, flags: Int = 0): Boolean
    fun sliderFloat(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String = "%.3f"): Boolean
    fun sliderInt(label: String, v: IntArray, vMin: Int, vMax: Int, format: String = "%d"): Boolean
    fun dragFloat(label: String, v: FloatArray, vSpeed: Float = 1f, vMin: Float = 0f, vMax: Float = 0f, format: String = "%.3f", flags: Int = 0): Boolean
    fun dragFloat2(label: String, v: FloatArray, vSpeed: Float = 1f, vMin: Float = 0f, vMax: Float = 0f, format: String = "%.3f", flags: Int = 0): Boolean
    fun dragFloat3(label: String, v: FloatArray, vSpeed: Float = 1f, vMin: Float = 0f, vMax: Float = 0f, format: String = "%.3f", flags: Int = 0): Boolean
    fun dragFloat4(label: String, v: FloatArray, vSpeed: Float = 1f, vMin: Float = 0f, vMax: Float = 0f, format: String = "%.3f", flags: Int = 0): Boolean
    fun dragFloatRange2(label: String, vCurrentMin: FloatArray, vCurrentMax: FloatArray, vSpeed: Float = 1f, vMin: Float = 0f, vMax: Float = 0f, format: String = "%.3f", formatMax: String? = null, flags: Int = 0): Boolean
    fun dragInt(label: String, v: IntArray, vSpeed: Float = 1f, vMin: Int = 0, vMax: Int = 0, format: String = "%d", flags: Int = 0): Boolean
    fun dragInt2(label: String, v: IntArray, vSpeed: Float = 1f, vMin: Int = 0, vMax: Int = 0, format: String = "%d", flags: Int = 0): Boolean
    fun dragInt3(label: String, v: IntArray, vSpeed: Float = 1f, vMin: Int = 0, vMax: Int = 0, format: String = "%d", flags: Int = 0): Boolean
    fun dragInt4(label: String, v: IntArray, vSpeed: Float = 1f, vMin: Int = 0, vMax: Int = 0, format: String = "%d", flags: Int = 0): Boolean
    fun dragIntRange2(label: String, vCurrentMin: IntArray, vCurrentMax: IntArray, vSpeed: Float = 1f, vMin: Int = 0, vMax: Int = 0, format: String = "%d", formatMax: String? = null, flags: Int = 0): Boolean
    fun sliderFloat2(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String = "%.3f", flags: Int = 0): Boolean
    fun sliderFloat3(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String = "%.3f", flags: Int = 0): Boolean
    fun sliderFloat4(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String = "%.3f", flags: Int = 0): Boolean
    fun sliderInt2(label: String, v: IntArray, vMin: Int, vMax: Int, format: String = "%d", flags: Int = 0): Boolean
    fun sliderInt3(label: String, v: IntArray, vMin: Int, vMax: Int, format: String = "%d", flags: Int = 0): Boolean
    fun sliderInt4(label: String, v: IntArray, vMin: Int, vMax: Int, format: String = "%d", flags: Int = 0): Boolean
    fun sliderAngle(label: String, vRad: FloatArray, vDegreesMin: Float = -360f, vDegreesMax: Float = 360f, format: String = "%.0f deg", flags: Int = 0): Boolean
    fun vSliderFloat(label: String, size: ImVec2, v: FloatArray, vMin: Float, vMax: Float, format: String = "%.3f", flags: Int = 0): Boolean
    fun vSliderInt(label: String, size: ImVec2, v: IntArray, vMin: Int, vMax: Int, format: String = "%d", flags: Int = 0): Boolean
    fun sliderScalar(label: String, dataType: Int, v: LongArray, vMin: LongArray, vMax: LongArray, format: String = "%f"): Boolean
    fun dragScalar(label: String, dataType: Int, v: LongArray, vSpeed: Float, vMin: LongArray, vMax: LongArray, format: String = "%.3f"): Boolean
    fun inputFloat(label: String, v: FloatArray, step: Float = 0f, stepFast: Float = 0f, format: String = "%.3f", flags: Int = 0): Boolean
    fun inputFloat2(label: String, v: FloatArray, format: String = "%.3f", flags: Int = 0): Boolean
    fun inputFloat3(label: String, v: FloatArray, format: String = "%.3f", flags: Int = 0): Boolean
    fun inputFloat4(label: String, v: FloatArray, format: String = "%.3f", flags: Int = 0): Boolean
    fun inputInt(label: String, v: IntArray, step: Int = 1, stepFast: Int = 100, flags: Int = 0): Boolean
    fun inputInt2(label: String, v: IntArray, flags: Int = 0): Boolean
    fun inputInt3(label: String, v: IntArray, flags: Int = 0): Boolean
    fun inputInt4(label: String, v: IntArray, flags: Int = 0): Boolean
    fun inputDouble(label: String, v: DoubleArray, step: Double = 0.0, stepFast: Double = 0.0, format: String = "%.6f", flags: Int = 0): Boolean
    fun colorEdit3(label: String, col: FloatArray, flags: Int = 0): Boolean
    fun colorEdit4(label: String, col: FloatArray, flags: Int = 0): Boolean
    fun colorPicker3(label: String, col: FloatArray, flags: Int = 0): Boolean
    fun colorPicker4(label: String, col: FloatArray, flags: Int = 0): Boolean
    fun colorButton(descId: String, col: ImVec4, flags: Int = 0, size: ImVec2 = ImVec2(0f, 0f)): Boolean
    fun setColorEditOptions(flags: Int)
    fun colorConvertFloat4ToU32(`in`: ImVec4): Int
    fun colorConvertU32ToFloat4(`in`: Int): ImVec4
    fun colorConvertRGBtoHSV(r: Float, g: Float, b: Float, outH: FloatArray, outS: FloatArray, outV: FloatArray)
    fun colorConvertHSVtoRGB(h: Float, s: Float, v: Float, outR: FloatArray, outG: FloatArray, outB: FloatArray)

    /**
     * Editable text field. Returns the current buffer content; compare it
     * against [buf] to detect edits.
     */
    fun inputText(label: String, buf: String, flags: Int = 0): String?
    fun inputTextMultiline(label: String, buf: String, size: ImVec2 = ImVec2(0f, 0f), flags: Int = 0): String?
    fun inputTextWithHint(label: String, hint: String, buf: String, flags: Int = 0): String?
    fun combo(label: String, currentItem: IntArray, items: Array<String>): Boolean
    fun selectable(label: String, selected: Boolean = false, flags: Int = 0, size: ImVec2 = ImVec2(0f, 0f)): Boolean
    fun radioButton(label: String, active: Boolean): Boolean
    fun progressBar(fraction: Float, size: ImVec2 = ImVec2(-1f, 0f), overlay: String? = null)
    fun collapsingHeader(label: String, flags: Int = 0): Boolean
    fun treeNode(label: String): Boolean
    fun treeNodeEx(label: String, flags: Int = 0): Boolean
    fun treeNodeGetOpen(label: String): Boolean
    fun treePush(strId: String? = null)
    fun treePop()
    fun invisibleButton(id: String, size: ImVec2, flags: Int = 0): Boolean
    fun beginGroup()
    fun endGroup()
    fun setCursorPos(pos: ImVec2)
    fun pushId(id: String)
    fun popId()
    fun beginCombo(label: String, previewValue: String, flags: Int = 0): Boolean
    fun endCombo()

    // ==================== Item queries ====================
    fun isItemHovered(flags: Int = 0): Boolean
    fun isItemActive(): Boolean
    fun isItemClicked(mouseButton: Int = 0): Boolean
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
    fun getItemRectMin(): ImVec2
    fun getItemRectMax(): ImVec2
    fun getItemRectSize(): ImVec2

    // ==================== Window state ====================
    fun isWindowHovered(flags: Int = 0): Boolean
    fun isWindowFocused(flags: Int = 0): Boolean
    fun isWindowAppearing(): Boolean
    fun isWindowCollapsed(): Boolean
    fun isRectVisible(size: ImVec2): Boolean
    fun isPopupOpen(strId: String, flags: Int = 0): Boolean
    fun getWindowPos(): ImVec2
    fun getWindowSize(): ImVec2
    fun getWindowWidth(): Float
    fun getWindowHeight(): Float
    fun getWindowContentRegionMax(): ImVec2
    fun getWindowContentRegionMin(): ImVec2
    fun getWindowDrawList(): ImDrawList
    fun getForegroundDrawList(): ImDrawList
    fun getBackgroundDrawList(): ImDrawList

    // ==================== Keyboard / mouse ====================
    fun isKeyDown(key: Int): Boolean
    fun isKeyPressed(key: Int, repeat: Boolean = true): Boolean
    fun isKeyReleased(key: Int): Boolean
    fun isMouseDown(button: Int): Boolean
    fun isMouseClicked(button: Int, repeat: Boolean = false): Boolean
    fun isMouseReleased(button: Int): Boolean
    fun isMouseDoubleClicked(button: Int): Boolean
    fun isMouseDragging(button: Int = 0, lockThreshold: Float = -1f): Boolean
    fun isAnyMouseDown(): Boolean
    fun isMousePosValid(mousePos: ImVec2? = null): Boolean
    fun getMousePos(): ImVec2
    fun getMouseDragDelta(button: Int = 0, lockThreshold: Float = -1f): ImVec2
    fun resetMouseDragDelta(button: Int = 0)
    fun getMouseCursor(): Int
    fun setMouseCursor(cursor: Int)
    fun setKeyboardFocusHere(offset: Int = 0)
    fun setNextFrameWantCaptureKeyboard(wantCaptureKeyboard: Boolean)
    fun setNextFrameWantCaptureMouse(wantCaptureMouse: Boolean)
    fun setClipboardText(text: String)
    fun getClipboardText(): String?

    // ==================== Cursor / scroll / layout ====================
    fun getCursorPos(): ImVec2
    fun getCursorScreenPos(): ImVec2
    fun getCursorStartPos(): ImVec2
    fun setCursorPosX(localX: Float)
    fun setCursorScreenPos(pos: ImVec2)
    fun getContentRegionAvail(): ImVec2
    fun getScrollX(): Float
    fun getScrollY(): Float
    fun getScrollMaxX(): Float
    fun getScrollMaxY(): Float
    fun setScrollHereX(centerXRatio: Float = 0.5f)
    fun setScrollHereY(centerYRatio: Float = 0.5f)
    fun setScrollFromPosX(localX: Float, centerXRatio: Float = 0.5f)
    fun setScrollFromPosY(localY: Float, centerYRatio: Float = 0.5f)
    fun setScrollX(scrollX: Float)
    fun setScrollY(scrollY: Float)

    // ==================== Other queries ====================
    fun getFrameCount(): Int
    fun getFrameHeight(): Float
    fun getFrameHeightWithSpacing(): Float
    fun getFontSize(): Float
    fun getFont(): Long
    fun getMainViewport(): Long
    fun getStyleColorVec4(idx: Int): ImVec4
    fun getCursorPosX(): Float
    fun getKeyName(key: Int): String
    fun getTextLineHeight(): Float
    fun getTextLineHeightWithSpacing(): Float
    fun getID(strId: String): Int
    fun getColorU32(idx: Int, alphaMul: Float = 1f): Int
    fun getStyleColorName(idx: Int): String
    fun calcTextSize(text: String, hideTextAfterDoubleHash: Boolean = false, wrapWidth: Float = -1f): ImVec2
    fun calcItemWidth(): Float
    fun getTime(): Double

    // ==================== Columns (legacy multi-column layout) ====================
    fun columns(count: Int = 1, id: String? = null, border: Boolean = true)
    fun nextColumn()
    fun getColumnIndex(): Int
    fun getColumnOffset(columnIndex: Int = -1): Float
    fun setColumnOffset(columnIndex: Int, offsetX: Float)
    fun getColumnWidth(columnIndex: Int = -1): Float
    fun setColumnWidth(columnIndex: Int, width: Float)
    fun getColumnsCount(): Int

    // ==================== Tables ====================
    fun beginTable(id: String, column: Int, flags: Int = 0, outerSize: ImVec2 = ImVec2(0f, 0f), innerWidth: Float = 0f): Boolean
    fun endTable()
    fun tableNextRow(minRowHeight: Int = 0, flags: Int = 0)
    fun tableNextColumn(): Boolean
    fun tableSetColumnIndex(columnIndex: Int): Boolean
    fun tableSetupColumn(label: String, flags: Int = 0, initWidthOrWeight: Float = 0f, userId: Int = 0)
    fun tableSetupScrollFreeze(cols: Int, rows: Int)
    fun tableHeadersRow()
    fun tableHeader(label: String)
    fun tableAngledHeadersRow()
    fun tableGetColumnCount(): Int
    fun tableGetColumnFlags(columnN: Int = -1): Int
    fun tableGetColumnIndex(): Int
    fun tableGetRowIndex(): Int
    fun tableGetColumnName(columnN: Int = -1): String
    fun tableGetSortSpecs(): Long
    fun tableSetBgColor(target: Int, color: Int, columnN: Int = -1)
    fun tabItemButton(label: String, flags: Int = 0): Boolean

    // ==================== Style ====================
    fun styleColorsDark()
    fun styleColorsLight()
    fun styleColorsClassic()
    fun showStyleSelector(label: String): Boolean
    fun showFontSelector(label: String)
    fun showStyleEditor()
    fun pushStyleColor(idx: Int, color: ImVec4)
    fun popStyleColor(count: Int = 1)
    fun pushStyleVarFloat(idx: Int, value: Float)
    fun pushStyleVarVec2(idx: Int, value: ImVec2)
    fun popStyleVar(count: Int = 1)
    fun pushFont(font: ImFont)
    fun popFont()
    fun pushItemWidth(width: Float)
    fun popItemWidth()
    fun setNextItemWidth(width: Float)

    // ==================== SetNext* layout / item state ====================
    fun setNextItemOpen(isOpen: Boolean, cond: Int = ImGuiCond.ONCE)
    fun setNextItemAllowOverlap()
    fun setNextItemSelectionUserData(selectionUserData: Long)
    fun setNextItemShortcut(keyChord: Int, flags: Int = 0)
    fun setNextWindowCollapsed(collapsed: Boolean, cond: Int = ImGuiCond.ALWAYS)
    fun setNextWindowContentSize(size: ImVec2)
    fun setNextWindowFocus()
    fun setNextWindowScroll(scroll: ImVec2)
    fun setNextWindowSizeConstraints(sizeMin: ImVec2, sizeMax: ImVec2, customCallback: (() -> Unit)? = null)
    fun setItemTooltip(text: String)
    fun setItemDefaultFocus()
    fun setTabItemClosed(tabOrDockedWindowLabel: String)
}

// =========================================================================
// Flags and enums (values match Dear ImGui's imgui.h)
// =========================================================================

object ImGuiCol {
    const val TEXT = 0
    const val TEXT_DISABLED = 1
    const val WINDOW_BG = 2
    const val CHILD_BG = 3
    const val POPUP_BG = 4
    const val BORDER = 5
    const val BORDER_SHADOW = 6
    const val FRAME_BG = 7
    const val FRAME_BG_HOVERED = 8
    const val FRAME_BG_ACTIVE = 9
    const val TITLE_BG = 10
    const val TITLE_BG_ACTIVE = 11
    const val TITLE_BG_COLLAPSED = 12
    const val SCROLLBAR_BG = 13
    const val CHECK_MARK = 14
    const val CHECKBOX_SELECTED_BG = 15
    const val SLIDER_GRAB = 16
    const val SLIDER_GRAB_ACTIVE = 17
    const val BUTTON = 18
    const val BUTTON_HOVERED = 19
    const val BUTTON_ACTIVE = 20
    const val HEADER = 21
    const val HEADER_HOVERED = 22
    const val HEADER_ACTIVE = 23
    const val SEPARATOR = 24
    const val SEPARATOR_HOVERED = 25
    const val SEPARATOR_ACTIVE = 26
    const val INPUT_TEXT_CURSOR = 27
    const val TAB_HOVERED = 28
    const val TAB = 29
    const val TAB_SELECTED = 30
    const val TAB_SELECTED_OVERLINE = 31
    const val TAB_DIMMED = 32
    const val TAB_DIMMED_SELECTED = 33
    const val PLOT_LINES = 34
    const val PLOT_LINES_HOVERED = 35
    const val PLOT_HISTOGRAM = 36
    const val PLOT_HISTOGRAM_HOVERED = 37
    const val TABLE_HEADER_BG = 38
    const val TABLE_BORDER_STRONG = 39
    const val TABLE_BORDER_LIGHT = 40
    const val TABLE_ROW_BG = 41
    const val TABLE_ROW_BG_ALT = 42
    const val TEXT_LINK = 43
    const val TEXT_SELECTED_BG = 44
    const val DRAG_DROP_TARGET = 45
    const val NAV_HIGHLIGHT = 46
    const val NAV_WINDOWING_HIGHLIGHT = 47
    const val NAV_WINDOWING_DIM_BG = 48
    const val MODAL_WINDOW_DIM_BG = 49
    const val DOCKING_PREVIEW = 50
    const val DOCKING_EMPTY_BG = 51
}

object ImGuiWindowFlags {
    const val NONE = 0
    const val NO_TITLE_BAR = 1 shl 0
    const val NO_RESIZE = 1 shl 1
    const val NO_MOVE = 1 shl 2
    const val NO_SCROLLBAR = 1 shl 3
    const val NO_SCROLL_WITH_MOUSE = 1 shl 4
    const val NO_COLLAPSE = 1 shl 5
    const val ALWAYS_AUTO_RESIZE = 1 shl 6
    const val NO_BACKGROUND = 1 shl 7
    const val NO_SAVED_SETTINGS = 1 shl 8
    const val NO_MOUSE_INPUTS = 1 shl 9
    const val MENU_BAR = 1 shl 10
    const val HORIZONTAL_SCROLLBAR = 1 shl 11
    const val NO_FOCUS_ON_APPEARING = 1 shl 12
    const val NO_BRING_TO_FRONT_ON_FOCUS = 1 shl 13
    const val ALWAYS_VERTICAL_SCROLLBAR = 1 shl 14
    const val NO_NAV_INPUTS = 1 shl 16
    const val NO_NAV_FOCUS = 1 shl 17
    const val UNSAVED_DOCUMENT = 1 shl 18

    const val NO_DECORATION = NO_TITLE_BAR or NO_RESIZE or NO_SCROLLBAR
    const val NO_INPUTS = NO_MOUSE_INPUTS or NO_NAV_INPUTS
}

object ImGuiChildFlags {
    const val NONE = 0
    const val BORDERS = 1 shl 0
    const val ALWAYS_USE_WINDOW_PADDING = 1 shl 1
    const val NAV_FLATTENED = 1 shl 2
    const val FRAME_STYLE = 1 shl 3
    const val AUTO_RESIZE_X = 1 shl 4
    const val AUTO_RESIZE_Y = 1 shl 5
    const val RESIZE_X = 1 shl 6
    const val RESIZE_Y = 1 shl 7
}

object ImGuiCond {
    const val NONE = 0
    const val ALWAYS = 1 shl 0
    const val ONCE = 1 shl 1
    const val FIRST_USE_EVER = 1 shl 2
    const val APPEARING = 1 shl 3
}

object ImGuiBackendFlags {
    const val NONE = 0
    const val HAS_GAMEPAD = 1 shl 0
    const val HAS_MOUSE_CURSORS = 1 shl 1
    const val HAS_SET_MOUSE_POS = 1 shl 2
    const val RENDERER_HAS_VTX_OFFSET = 1 shl 3
    const val RENDERER_HAS_TEXTURES = 1 shl 4
}

object ImGuiConfigFlags {
    const val NONE = 0
    const val NAV_ENABLE_KEYBOARD = 1 shl 0
    const val NAV_ENABLE_GAMEPAD = 1 shl 1
    const val NAV_ENABLE_SET_MOUSE_POS = 1 shl 2
    const val NAV_NO_CAPTURE_KEYBOARD = 1 shl 3
    const val NO_MOUSE = 1 shl 4
    const val NO_MOUSE_CURSOR_CHANGE = 1 shl 5
    const val NO_KEYBOARD = 1 shl 6
    const val IS_SRGB = 1 shl 20
    const val IS_TOUCH_SCREEN = 1 shl 21
}

object ImGuiMouseButton {
    const val LEFT = 0
    const val RIGHT = 1
    const val MIDDLE = 2
}

object ImGuiDir {
    const val NONE = -1
    const val LEFT = 0
    const val RIGHT = 1
    const val UP = 2
    const val DOWN = 3
}

object ImGuiKey {
    const val TAB = 512
    const val LEFT_ARROW = 513
    const val RIGHT_ARROW = 514
    const val UP_ARROW = 515
    const val DOWN_ARROW = 516
    const val PAGE_UP = 517
    const val PAGE_DOWN = 518
    const val HOME = 519
    const val END = 520
    const val INSERT = 521
    const val DELETE = 522
    const val BACKSPACE = 523
    const val SPACE = 524
    const val ENTER = 525
    const val ESCAPE = 526
    const val LEFT_CTRL = 527
    const val LEFT_SHIFT = 528
    const val LEFT_ALT = 529
    const val LEFT_SUPER = 530
    const val RIGHT_CTRL = 531
    const val RIGHT_SHIFT = 532
    const val RIGHT_ALT = 533
    const val RIGHT_SUPER = 534
    const val MENU = 535
    const val KEY_0 = 536
    const val KEY_1 = 537
    const val KEY_2 = 538
    const val KEY_3 = 539
    const val KEY_4 = 540
    const val KEY_5 = 541
    const val KEY_6 = 542
    const val KEY_7 = 543
    const val KEY_8 = 544
    const val KEY_9 = 545
    const val A = 546
    const val B = 547
    const val C = 548
    const val D = 549
    const val E = 550
    const val F = 551
    const val G = 552
    const val H = 553
    const val I = 554
    const val J = 555
    const val K = 556
    const val L = 557
    const val M = 558
    const val N = 559
    const val O = 560
    const val P = 561
    const val Q = 562
    const val R = 563
    const val S = 564
    const val T = 565
    const val U = 566
    const val V = 567
    const val W = 568
    const val X = 569
    const val Y = 570
    const val Z = 571
    const val F1 = 572
    const val F2 = 573
    const val F3 = 574
    const val F4 = 575
    const val F5 = 576
    const val F6 = 577
    const val F7 = 578
    const val F8 = 579
    const val F9 = 580
    const val F10 = 581
    const val F11 = 582
    const val F12 = 583
    const val F13 = 584
    const val F14 = 585
    const val F15 = 586
    const val F16 = 587
    const val F17 = 588
    const val F18 = 589
    const val F19 = 590
    const val F20 = 591
    const val F21 = 592
    const val F22 = 593
    const val F23 = 594
    const val F24 = 595
    const val APOSTROPHE = 596
    const val COMMA = 597
    const val MINUS = 598
    const val PERIOD = 599
    const val SLASH = 600
    const val SEMICOLON = 601
    const val EQUAL = 602
    const val LEFT_BRACKET = 603
    const val BACKSLASH = 604
    const val RIGHT_BRACKET = 605
    const val GRAVE_ACCENT = 606
    const val CAPS_LOCK = 607
    const val SCROLL_LOCK = 608
    const val NUM_LOCK = 609
    const val PRINT_SCREEN = 610
    const val PAUSE = 611
    const val KEYPAD_0 = 612
    const val KEYPAD_1 = 613
    const val KEYPAD_2 = 614
    const val KEYPAD_3 = 615
    const val KEYPAD_4 = 616
    const val KEYPAD_5 = 617
    const val KEYPAD_6 = 618
    const val KEYPAD_7 = 619
    const val KEYPAD_8 = 620
    const val KEYPAD_9 = 621
    const val KEYPAD_DECIMAL = 622
    const val KEYPAD_DIVIDE = 623
    const val KEYPAD_MULTIPLY = 624
    const val KEYPAD_SUBTRACT = 625
    const val KEYPAD_ADD = 626
    const val KEYPAD_ENTER = 627
    const val KEYPAD_EQUAL = 628
    const val APP_BACK = 629
    const val APP_FORWARD = 630
    const val OEM_102 = 631

    // Modifiers (ImGuiMod_*)
    const val MOD_CTRL = 1 shl 12
    const val MOD_SHIFT = 1 shl 13
    const val MOD_ALT = 1 shl 14
    const val MOD_SUPER = 1 shl 15
}

object ImGuiTableFlags {
    const val NONE = 0
    const val RESIZABLE = 1 shl 0
    const val REORDERABLE = 1 shl 1
    const val HIDEABLE = 1 shl 2
    const val SORTABLE = 1 shl 3
    const val NO_SAVED_SETTINGS = 1 shl 4
    const val CONTEXT_MENU_IN_BODY = 1 shl 5
    const val ROW_BG = 1 shl 6
    const val BORDERS_INNER_H = 1 shl 7
    const val BORDERS_OUTER_H = 1 shl 8
    const val BORDERS_INNER_V = 1 shl 9
    const val BORDERS_OUTER_V = 1 shl 10
    const val BORDERS_H = BORDERS_INNER_H or BORDERS_OUTER_H
    const val BORDERS_V = BORDERS_INNER_V or BORDERS_OUTER_V
    const val BORDERS_INNER = BORDERS_INNER_V or BORDERS_INNER_H
    const val BORDERS_OUTER = BORDERS_OUTER_V or BORDERS_OUTER_H
    const val BORDERS = BORDERS_INNER or BORDERS_OUTER
    const val NO_BORDERS_IN_BODY = 1 shl 11
    const val NO_BORDERS_IN_BODY_UNTIL_RESIZE = 1 shl 12
    const val SIZING_FIXED_FIT = 1 shl 13
    const val SIZING_FIXED_SAME = 2 shl 13
    const val SIZING_STRETCH_PROP = 3 shl 13
    const val SIZING_STRETCH_SAME = 4 shl 13
    const val NO_HOST_EXTEND_X = 1 shl 16
    const val NO_HOST_EXTEND_Y = 1 shl 17
    const val NO_KEEP_COLUMNS_VISIBLE = 1 shl 18
    const val PRECISE_WIDTHS = 1 shl 19
    const val NO_CLIP = 1 shl 20
    const val PAD_OUTER_X = 1 shl 21
    const val NO_PAD_OUTER_X = 1 shl 22
    const val NO_PAD_INNER_X = 1 shl 23
    const val SCROLL_X = 1 shl 24
    const val SCROLL_Y = 1 shl 25
    const val SORT_MULTI = 1 shl 26
    const val SORT_TRISTATE = 1 shl 27
    const val SIZING_MASK = SIZING_FIXED_FIT or SIZING_FIXED_SAME or SIZING_STRETCH_PROP or SIZING_STRETCH_SAME
}

object ImGuiInputTextFlags {
    const val NONE = 0
    const val CHARS_DECIMAL = 1 shl 0
    const val CHARS_HEXADECIMAL = 1 shl 1
    const val CHARS_UPPERCASE = 1 shl 2
    const val CHARS_NO_BLANK = 1 shl 3
    const val AUTO_SELECT_ALL = 1 shl 4
    const val ENTER_RETURNS_TRUE = 1 shl 5
    const val CALLBACK_COMPLETION = 1 shl 6
    const val CALLBACK_HISTORY = 1 shl 7
    const val CALLBACK_ALWAYS = 1 shl 8
    const val CALLBACK_CHAR_FILTER = 1 shl 9
    const val ALLOW_TAB_INPUT = 1 shl 10
    const val CTRL_ENTER_FOR_NEW_LINE = 1 shl 11
    const val NO_HORIZONTAL_SCROLL = 1 shl 12
    const val ALWAYS_OVERWRITE = 1 shl 13
    const val READ_ONLY = 1 shl 14
    const val MERGE_MOVED_CHARS = 1 shl 15
    const val NO_AUTO_SELECT_ALL = 1 shl 17
}

object ImGuiSelectableFlags {
    const val NONE = 0
    const val NO_PADDING = 1 shl 0
    const val SPAN_ALL_COLUMNS = 1 shl 1
    const val ALLOW_DOUBLE_CLICK = 1 shl 2
    const val DISABLED = 1 shl 3
    const val ALLOW_OVERLAPPING_TOOLTIP = 1 shl 5
}

object ImGuiHoveredFlags {
    const val NONE = 0
    const val CHILD_WINDOWS = 1 shl 0
    const val ROOT_WINDOW = 1 shl 1
    const val ANY_WINDOW = 1 shl 2
    const val NO_NAV = 1 shl 3
    const val ALLOW_WHEN_BLOCKED_BY_POPUP = 1 shl 5
    const val ALLOW_WHEN_BLOCKED_BY_ACTIVE_ITEM = 1 shl 7
    const val ALLOW_WHEN_OVERLAPPED = 1 shl 8
    const val ALLOW_WHEN_DISABLED = 1 shl 9
    const val RECT_ONLY = ALLOW_WHEN_BLOCKED_BY_POPUP or ALLOW_WHEN_BLOCKED_BY_ACTIVE_ITEM or ALLOW_WHEN_OVERLAPPED
}

object ImGuiPopupFlags {
    const val NONE = 0
    const val MOUSE_BUTTON_LEFT = 0
    const val MOUSE_BUTTON_RIGHT = 1
    const val MOUSE_BUTTON_MIDDLE = 2
    const val NO_OPEN_OVER_EXISTING_POPUP = 1 shl 5
    const val NO_OPEN_OVER_ITEMS = 1 shl 6
    const val ANY_POPUP_ID = 1 shl 7
    const val ANY_POPUP_LEVEL = 1 shl 8
    const val ANY_POPUP = ANY_POPUP_ID or ANY_POPUP_LEVEL
}

object ImGuiTabItemFlags {
    const val NONE = 0
    const val UNSAVED_DOCUMENT = 1 shl 0
    const val SET_SELECTED = 1 shl 1
    const val NO_CLOSE_WITH_MIDDLE_MOUSE_BUTTON = 1 shl 2
    const val NO_PUSH_ID = 1 shl 3
    const val ALLOW_OVERLAPPING_TOOLTIP = 1 shl 4
    const val NO_REORDER = 1 shl 5
    const val LEADING = 1 shl 6
    const val TRAILING = 1 shl 7
}

object ImGuiTreeNodeFlags {
    const val NONE = 0
    const val SELECTED = 1 shl 0
    const val FRAMED = 1 shl 1
    const val ALLOW_OVERLAPPING_TOOLTIP = 1 shl 2
    const val NO_TREE_PUSH_ON_OPEN = 1 shl 3
    const val NO_AUTO_OPEN_ON_LOG = 1 shl 4
    const val DEFAULT_OPEN = 1 shl 5
    const val OPEN_ON_DOUBLE_CLICK = 1 shl 6
    const val OPEN_ON_ARROW = 1 shl 7
    const val LEAF = 1 shl 8
    const val BULLET = 1 shl 9
    const val FRAME_PADDING = 1 shl 10
    const val SPAN_AVAILABLE_WIDTH = 1 shl 11
    const val SPAN_FULL_WIDTH = 1 shl 12
    const val NO_HIDDEN_FAKE_ROOT = 1 shl 13
    const val COLLAPSING_HEADER = 1 shl 14
    const val ALWAYS_OPEN = 1 shl 15
    const val NO_AUTO_EXPAND_ON_SEARCH = 1 shl 16
    const val NAV_JUMPED = 1 shl 17
    const val NAV_LEFT_JUSTIFIED = 1 shl 18
}

object ImGuiComboFlags {
    const val NONE = 0
    const val POPUP_ALIGN_LEFT = 1 shl 0
    const val HEIGHT_SMALL = 1 shl 1
    const val HEIGHT_REGULAR = 1 shl 2
    const val HEIGHT_LARGE = 1 shl 3
    const val HEIGHT_LARGEST = 1 shl 4
    const val NO_ARROW_BUTTON = 1 shl 5
    const val NO_PREVIEW = 1 shl 6
    const val WIDTH_FIT_PREVIEW = 1 shl 7
    const val HEIGHT_MASK = HEIGHT_SMALL or HEIGHT_REGULAR or HEIGHT_LARGE or HEIGHT_LARGEST
}

object ImGuiSliderFlags {
    const val NONE = 0
    const val ALWAYS_CLAMP = 1 shl 4
    const val LOGARITHMIC = 1 shl 5
    const val NO_ROUND_TO_FORMAT = 1 shl 6
    const val NO_INPUT = 1 shl 7
    const val WRAP_AROUND = 1 shl 10
}

object ImGuiDragDropFlags {
    const val NONE = 0
    const val SOURCE_NO_PREVIEW_TOOLTIP = 1 shl 0
    const val SOURCE_NO_DISABLE_HOVER = 1 shl 1
    const val SOURCE_NO_HOLD_TO_OPEN_OTHERS = 1 shl 2
    const val SOURCE_ALLOW_NULL_ID = 1 shl 3
    const val SOURCE_EXTERN = 1 shl 4
    const val PAYLOAD_AUTO_EXPIRE = 1 shl 5
    const val PAYLOAD_NO_CROSS_CONTEXT = 1 shl 6
    const val PAYLOAD_NO_CROSS_PROCESS = 1 shl 7
    const val ACCEPT_BEFORE_DELIVERY = 1 shl 10
    const val ACCEPT_NO_DRAW_DEFAULT_RECT = 1 shl 11
    const val ACCEPT_NO_PREVIEW_TOOLTIP = 1 shl 12
    const val ACCEPT_DRAW_AS_HOVERED = 1 shl 13
}

object ImGuiMultiSelectFlags {
    const val NONE = 0
    const val SINGLE_SELECT = 1 shl 0
    const val NO_SELECT_ALL = 1 shl 1
    const val NO_RANGE_SELECT = 1 shl 2
    const val NO_AUTO_SELECT = 1 shl 3
    const val NO_AUTO_CLEAR = 1 shl 4
    const val NO_AUTO_CLEAR_ON_RESELECT = 1 shl 5
    const val BOX_SELECT_1D = 1 shl 6
    const val BOX_SELECT_2D = 1 shl 7
    const val BOX_SELECT_NO_SCROLL = 1 shl 8
    const val CLEAR_ON_ESCAPE = 1 shl 9
    const val CLEAR_ON_CLICK_VOID = 1 shl 10
    const val SCOPE_WINDOW = 1 shl 11
    const val SCOPE_RECT = 1 shl 12
    const val SELECT_ON_AUTO = 1 shl 13
    const val SELECT_ON_CLICK_ALWAYS = 1 shl 14
    const val SELECT_ON_CLICK_RELEASE = 1 shl 15
    const val NAV_WRAP_X = 1 shl 16
    const val NO_SELECT_ON_RIGHT_CLICK = 1 shl 17
}

object ImDrawFlags {
    const val NONE = 0
    const val ROUND_CORNERS_TOP_LEFT = 1 shl 4
    const val ROUND_CORNERS_TOP_RIGHT = 1 shl 5
    const val ROUND_CORNERS_BOTTOM_LEFT = 1 shl 6
    const val ROUND_CORNERS_BOTTOM_RIGHT = 1 shl 7
    const val ROUND_CORNERS_NONE = 1 shl 8
    const val ROUND_CORNERS_TOP = ROUND_CORNERS_TOP_LEFT or ROUND_CORNERS_TOP_RIGHT
    const val ROUND_CORNERS_BOTTOM = ROUND_CORNERS_BOTTOM_LEFT or ROUND_CORNERS_BOTTOM_RIGHT
    const val ROUND_CORNERS_LEFT = ROUND_CORNERS_BOTTOM_LEFT or ROUND_CORNERS_TOP_LEFT
    const val ROUND_CORNERS_RIGHT = ROUND_CORNERS_BOTTOM_RIGHT or ROUND_CORNERS_TOP_RIGHT
    const val ROUND_CORNERS_ALL = ROUND_CORNERS_TOP_LEFT or ROUND_CORNERS_TOP_RIGHT or ROUND_CORNERS_BOTTOM_LEFT or ROUND_CORNERS_BOTTOM_RIGHT
    const val CLOSED = 1 shl 9
}

object ImGuiDataType {
    const val S8 = 0
    const val U8 = 1
    const val S16 = 2
    const val U16 = 3
    const val S32 = 4
    const val U32 = 5
    const val S64 = 6
    const val U64 = 7
    const val FLOAT = 8
    const val DOUBLE = 9
}

object ImGuiItemFlags {
    const val NONE = 0
    const val NO_TAB_STOP = 1 shl 0
    const val NO_NAV = 1 shl 1
    const val NO_NAV_DEFAULT_FOCUS = 1 shl 2
    const val BUTTON_REPEAT = 1 shl 3
    const val AUTO_CLOSE_POPUPS = 1 shl 4
    const val ALLOW_DUPLICATE_ID = 1 shl 5
    const val DISABLED = 1 shl 6
}

object ImGuiTableBgTarget {
    const val NONE = 0
    const val ROW_BG0 = 1
    const val ROW_BG1 = 2
    const val CELL_BG = 3
}

object ImGuiTableRowFlags {
    const val NONE = 0
    const val HEADERS = 1 shl 0
}

object ImGuiTableColumnFlags {
    const val NONE = 0
    const val DISABLED = 1 shl 0
    const val DEFAULT_HIDE = 1 shl 1
    const val DEFAULT_SORT = 1 shl 2
    const val WIDTH_STRETCH = 1 shl 3
    const val WIDTH_FIXED = 1 shl 4
    const val NO_RESIZE = 1 shl 5
    const val NO_REORDER = 1 shl 6
    const val NO_HIDE = 1 shl 7
    const val NO_CLIP = 1 shl 8
    const val NO_SORT = 1 shl 9
    const val NO_SORT_ASCENDING = 1 shl 10
    const val NO_SORT_DESCENDING = 1 shl 11
    const val NO_HEADER_LABEL = 1 shl 12
    const val NO_HEADER_WIDTH = 1 shl 13
    const val PREFER_SORT_ASCENDING = 1 shl 14
    const val PREFER_SORT_DESCENDING = 1 shl 15
    const val INDENT_ENABLE = 1 shl 16
    const val INDENT_DISABLE = 1 shl 17
    const val ANGLED_HEADER = 1 shl 18

    // Output status flags (read via ImGui.tableGetColumnFlags)
    const val IS_ENABLED = 1 shl 24
    const val IS_VISIBLE = 1 shl 25
    const val IS_SORTED = 1 shl 26
    const val IS_HOVERED = 1 shl 27
}

object ImGuiStyleVar {
    const val ALPHA = 0
    const val DISABLED_ALPHA = 1
    const val WINDOW_PADDING = 2
    const val WINDOW_ROUNDING = 3
    const val WINDOW_BORDER_SIZE = 4
    const val WINDOW_MIN_SIZE = 5
    const val WINDOW_TITLE_ALIGN = 6
    const val CHILD_ROUNDING = 7
    const val CHILD_BORDER_SIZE = 8
    const val POPUP_ROUNDING = 9
    const val POPUP_BORDER_SIZE = 10
    const val FRAME_PADDING = 11
    const val FRAME_ROUNDING = 12
    const val FRAME_BORDER_SIZE = 13
    const val ITEM_SPACING = 14
    const val ITEM_INNER_SPACING = 15
    const val INDENT_SPACING = 16
    const val CELL_PADDING = 17
    const val SCROLLBAR_SIZE = 18
    const val SCROLLBAR_ROUNDING = 19
    const val SCROLLBAR_PADDING = 20
    const val GRAB_MIN_SIZE = 21
    const val GRAB_ROUNDING = 22
    const val IMAGE_ROUNDING = 23
    const val IMAGE_BORDER_SIZE = 24
    const val TAB_ROUNDING = 25
    const val TAB_BORDER_SIZE = 26
    const val TAB_MIN_WIDTH_BASE = 27
    const val TAB_MIN_WIDTH_SHRINK = 28
    const val TAB_BAR_BORDER_SIZE = 29
    const val TAB_BAR_OVERLINE_SIZE = 30
    const val TABLE_ANGLED_HEADERS_ANGLE = 31
    const val TABLE_ANGLED_HEADERS_TEXT_ALIGN = 32
    const val TREE_LINES_SIZE = 33
    const val TREE_LINES_ROUNDING = 34
    const val MENU_ITEM_ROUNDING = 35
    const val SELECTABLE_ROUNDING = 36
    const val DRAG_DROP_TARGET_ROUNDING = 37
    const val BUTTON_TEXT_ALIGN = 38
    const val SELECTABLE_TEXT_ALIGN = 39
    const val SEPARATOR_SIZE = 40
    const val SEPARATOR_TEXT_BORDER_SIZE = 41
    const val SEPARATOR_TEXT_ALIGN = 42
    const val SEPARATOR_TEXT_PADDING = 43
}

object ImGuiColorEditFlags {
    const val NONE = 0
    const val NO_ALPHA = 1 shl 1
    const val NO_PICKER = 1 shl 2
    const val NO_OPTIONS = 1 shl 3
    const val NO_SMALL_PREVIEW = 1 shl 4
    const val NO_INPUTS = 1 shl 5
    const val NO_TOOLTIP = 1 shl 6
    const val NO_LABEL = 1 shl 7
    const val NO_SIDE_PREVIEW = 1 shl 8
    const val NO_DRAG_DROP = 1 shl 9
    const val NO_BORDER = 1 shl 10
    const val NO_COLOR_MARKERS = 1 shl 11
    const val ALPHA_OPAQUE = 1 shl 12
    const val ALPHA_NO_BG = 1 shl 13
    const val ALPHA_PREVIEW_HALF = 1 shl 14
    const val ALPHA_BAR = 1 shl 18
    const val HDR = 1 shl 19
    const val DISPLAY_RGB = 1 shl 20
    const val DISPLAY_HSV = 1 shl 21
    const val DISPLAY_HEX = 1 shl 22
    const val UINT8 = 1 shl 23
    const val FLOAT = 1 shl 24
    const val PICKER_HUE_BAR = 1 shl 25
    const val PICKER_HUE_WHEEL = 1 shl 26
    const val PICKER_NO_ROTATE = 1 shl 27
    const val INPUT_RGB = 1 shl 28
    const val INPUT_HSV = 1 shl 29
}

object ImGuiMouseCursor {
    const val NONE = -1
    const val ARROW = 0
    const val TEXT_INPUT = 1
    const val RESIZE_ALL = 2
    const val RESIZE_NS = 3
    const val RESIZE_EW = 4
    const val RESIZE_NESW = 5
    const val RESIZE_NWSE = 6
    const val HAND = 7
    const val WAIT = 8
    const val PROGRESS = 9
    const val NOT_ALLOWED = 10
}
