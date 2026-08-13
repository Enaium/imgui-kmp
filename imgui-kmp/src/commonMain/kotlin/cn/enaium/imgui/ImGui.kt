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

/** The font atlas owned by [ImGuiIO]; holds all fonts and the shared texture. */
interface ImFontAtlas {
    fun addFontFromFileTTF(path: String, sizePx: Float): ImFont
    fun addFontDefault(): ImFont
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
    fun newFrame()
    fun render()
    fun getDrawData(): ImDrawData
    fun getIO(): ImGuiIO
    fun getStyle(): ImGuiStyle
    fun getVersion(): String
    fun showDemoWindow(pOpen: BooleanArray? = null)

    // ==================== Windows ====================
    fun begin(name: String, pOpen: BooleanArray? = null, flags: Int = 0): Boolean
    fun end()
    fun beginChild(id: String, size: ImVec2 = ImVec2(0f, 0f), childFlags: Int = 0, windowFlags: Int = 0): Boolean
    fun endChild()
    fun setNextWindowPos(pos: ImVec2, cond: Int = ImGuiCond.ALWAYS, pivot: ImVec2? = null)
    fun setNextWindowSize(size: ImVec2, cond: Int = ImGuiCond.ALWAYS)
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

    // ==================== Widgets ====================
    fun text(text: String)
    fun textColored(color: ImVec4, text: String)
    fun textDisabled(text: String)
    fun labelText(label: String, text: String)
    fun bulletText(text: String)
    fun bullet()
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
    fun checkbox(label: String, v: BooleanArray): Boolean
    fun sliderFloat(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String = "%.3f"): Boolean
    fun sliderInt(label: String, v: IntArray, vMin: Int, vMax: Int, format: String = "%d"): Boolean

    /**
     * Editable text field. Returns the current buffer content; compare it
     * against [buf] to detect edits.
     */
    fun inputText(label: String, buf: String, flags: Int = 0): String?
    fun combo(label: String, currentItem: IntArray, items: Array<String>): Boolean
    fun selectable(label: String, selected: Boolean = false, flags: Int = 0, size: ImVec2 = ImVec2(0f, 0f)): Boolean
    fun radioButton(label: String, active: Boolean): Boolean
    fun progressBar(fraction: Float, size: ImVec2 = ImVec2(-1f, 0f), overlay: String? = null)
    fun collapsingHeader(label: String, flags: Int = 0): Boolean
    fun treeNode(label: String): Boolean
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
    fun isWindowHovered(flags: Int = 0): Boolean
    fun isWindowFocused(flags: Int = 0): Boolean

    // ==================== Tables ====================
    fun beginTable(id: String, column: Int, flags: Int = 0, outerSize: ImVec2 = ImVec2(0f, 0f), innerWidth: Float = 0f): Boolean
    fun endTable()
    fun tableNextRow(minRowHeight: Int = 0, flags: Int = 0)
    fun tableNextColumn(): Boolean
    fun tableSetColumnIndex(columnIndex: Int): Boolean
    fun tableSetupColumn(label: String, flags: Int = 0, initWidthOrWeight: Float = 0f, userId: Int = 0)
    fun tableSetupScrollFreeze(cols: Int, rows: Int)
    fun tableHeadersRow()

    // ==================== Style ====================
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
