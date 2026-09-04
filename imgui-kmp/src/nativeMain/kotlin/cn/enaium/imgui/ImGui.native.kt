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

@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlin.experimental.ExperimentalNativeApi::class,
)

package cn.enaium.imgui

import imgui.*
import kotlinx.cinterop.*

// =========================================================================
// Native (cinterop) actual implementations
// =========================================================================

internal class NativeImGuiContext(internal val ptr: CPointer<imgui_context>?) : ImGuiContext {
    override fun close() {
        imgui_destroy_context(ptr)
    }
}

internal class NativeImGuiIO(internal val ptr: CPointer<imgui_io>?) : ImGuiIO {
    override var displaySize: ImVec2
        get() = imgui_io_get_display_size(ptr).useContents { ImVec2(x, y) }
        set(value) = imgui_io_set_display_size(ptr, value.x, value.y)

    override var displayFramebufferScale: ImVec2
        get() = imgui_io_get_display_framebuffer_scale(ptr).useContents { ImVec2(x, y) }
        set(value) = imgui_io_set_display_framebuffer_scale(ptr, value.x, value.y)

    override var deltaTime: Float
        get() = imgui_io_get_delta_time(ptr)
        set(value) = imgui_io_set_delta_time(ptr, value)

    override var configFlags: Int
        get() = imgui_io_get_config_flags(ptr)
        set(value) = imgui_io_set_config_flags(ptr, value)

    override var backendFlags: Int
        get() = imgui_io_get_backend_flags(ptr)
        set(value) = imgui_io_set_backend_flags(ptr, value)

    override var iniFilename: String?
        get() = imgui_io_get_ini_filename(ptr)?.toKString()
        set(value) = imgui_io_set_ini_filename(ptr, value)

    override var fontGlobalScale: Float
        get() = imgui_io_get_font_global_scale(ptr)
        set(value) = imgui_io_set_font_global_scale(ptr, value)

    override val fonts: ImFontAtlas
        get() = NativeImFontAtlas(imgui_io_get_fonts(ptr))

    override fun addMousePosEvent(x: Float, y: Float) = imgui_io_add_mouse_pos_event(ptr, x, y)
    override fun addMouseButtonEvent(button: Int, down: Boolean) = imgui_io_add_mouse_button_event(ptr, button, down)
    override fun addMouseWheelEvent(wheelX: Float, wheelY: Float) = imgui_io_add_mouse_wheel_event(ptr, wheelX, wheelY)
    override fun addKeyEvent(key: Int, down: Boolean) = imgui_io_add_key_event(ptr, key, down)
    override fun addInputCharacter(c: UInt) = imgui_io_add_input_character(ptr, c)
    override fun addInputCharactersUTF8(text: String) {
        // Iterate UTF-16 code units, combining surrogate pairs into code points.
        var i = 0
        while (i < text.length) {
            val unit = text[i]
            var codePoint = unit.code
            if (unit.isHighSurrogate() && i + 1 < text.length && text[i + 1].isLowSurrogate()) {
                codePoint = ((unit.code - 0xD800) shl 10) + (text[i + 1].code - 0xDC00) + 0x10000
                i++
            }
            imgui_io_add_input_character(ptr, codePoint.convert())
            i++
        }
    }

    override val wantCaptureMouse: Boolean
        get() = imgui_io_want_capture_mouse(ptr)

    override val wantCaptureKeyboard: Boolean
        get() = imgui_io_want_capture_keyboard(ptr)

    override val wantTextInput: Boolean
        get() = imgui_io_want_text_input(ptr)
}

internal class NativeImGuiStyle(internal val ptr: CPointer<imgui_style>?) : ImGuiStyle {
    override fun getColor(idx: Int): ImVec4 =
        imgui_style_get_color(ptr, idx).useContents { ImVec4(x, y, z, w) }

    override fun setColor(idx: Int, color: ImVec4) = memScoped {
        val c = alloc<imgui_vec4>()
        c.x = color.x
        c.y = color.y
        c.z = color.z
        c.w = color.w
        imgui_style_set_color(ptr, idx, c.readValue())
    }
}

internal class NativeImFont(internal val ptr: CPointer<imgui_font>?) : ImFont

internal class NativeImFontAtlas(internal val ptr: CPointer<imgui_font_atlas>?) : ImFontAtlas {
    override fun addFontFromFileTTF(path: String, sizePx: Float): ImFont =
        NativeImFont(imgui_font_atlas_add_font_from_file_ttf(ptr, path, sizePx))

    override fun addFontDefault(): ImFont =
        NativeImFont(imgui_font_atlas_add_font_default(ptr))

    override fun addFontDefault(config: ImFontConfig): ImFont =
        NativeImFont(
            imgui_font_atlas_add_font_default_cfg(
                ptr, config.name, config.mergeMode, config.pixelSnapH,
                config.oversampleH, config.oversampleV,
                config.sizePixels, config.glyphOffsetX, config.glyphOffsetY,
                config.glyphMinAdvanceX, config.glyphMaxAdvanceX,
                config.rasterizerMultiply, config.rasterizerDensity, config.extraSizeScale,
            ),
        )

    override fun addFontFromFileTTF(path: String, config: ImFontConfig): ImFont {
        val ranges = config.glyphRanges
        if (ranges == null) {
            return NativeImFont(
                imgui_font_atlas_add_font_from_file_ttf_cfg(
                    ptr, path, config.name, config.mergeMode, config.pixelSnapH,
                    config.oversampleH, config.oversampleV,
                    config.sizePixels, config.glyphOffsetX, config.glyphOffsetY,
                    config.glyphMinAdvanceX, config.glyphMaxAdvanceX,
                    config.rasterizerMultiply, config.rasterizerDensity, config.extraSizeScale,
                ),
            )
        }
        // Flat {first, last} pairs -> 0-terminated ImWchar list.
        val buf = UShortArray(ranges.size + 1)
        for (i in ranges.indices) buf[i] = ranges[i].toUShort()
        buf[ranges.size] = 0u
        return NativeImFont(
            imgui_font_atlas_add_font_from_file_ttf_ranges(
                ptr, path, config.name, config.mergeMode, config.pixelSnapH,
                config.oversampleH, config.oversampleV,
                config.sizePixels, config.glyphOffsetX, config.glyphOffsetY,
                config.glyphMinAdvanceX, config.glyphMaxAdvanceX,
                config.rasterizerMultiply, config.rasterizerDensity, config.extraSizeScale,
                buf.refTo(0),
            ),
        )
    }

    override fun build(): Boolean = imgui_font_atlas_build(ptr)

    override fun getTexDataAsRGBA32(): FontTexData = memScoped {
        val pixels = alloc<CPointerVar<UByteVar>>()
        val width = alloc<IntVar>()
        val height = alloc<IntVar>()
        val bpp = alloc<IntVar>()
        imgui_font_atlas_get_tex_data_as_rgba32(ptr, pixels.ptr, width.ptr, height.ptr, bpp.ptr)
        val size = width.value * height.value * bpp.value
        val data = ByteArray(size) { i -> pixels.value!![i].toByte() }
        FontTexData(data, width.value, height.value, bpp.value)
    }

    override fun setTexID(id: Long) {
        imgui_font_atlas_set_tex_id(ptr, id.convert())
    }
}

internal class NativeImDrawCmd(internal val ptr: CPointer<imgui_draw_cmd>?) : ImDrawCmd {
    override val clipRect: ImVec4
        get() = imgui_draw_cmd_get_clip_rect(ptr).useContents { ImVec4(x, y, z, w) }

    override val texId: Long
        get() = imgui_draw_cmd_get_tex_id(ptr).toLong()

    override val vtxOffset: Int
        get() = imgui_draw_cmd_get_vtx_offset(ptr).toInt()

    override val idxOffset: Int
        get() = imgui_draw_cmd_get_idx_offset(ptr).toInt()

    override val elemCount: Int
        get() = imgui_draw_cmd_get_elem_count(ptr).toInt()

    override val hasUserCallback: Boolean
        get() = imgui_draw_cmd_has_user_callback(ptr)
}

internal class NativeImDrawList(internal val ptr: CPointer<imgui_draw_list>?) : ImDrawList {
    override val vtxCount: Int
        get() = imgui_draw_list_get_vtx_count(ptr)

    override val idxCount: Int
        get() = imgui_draw_list_get_idx_count(ptr)

    override val cmdCount: Int
        get() = imgui_draw_list_get_cmd_count(ptr)

    override fun cmd(index: Int): ImDrawCmd = NativeImDrawCmd(imgui_draw_list_get_cmd(ptr, index))

    override fun copyVtx(vtxOffset: Int, count: Int): ImDrawVertData {
        val data = ImDrawVertData()
        data.positions.ensureCapacity(count * 2)
        data.uvs.ensureCapacity(count * 2)
        data.colors.ensureCapacity(count)
        val verts = imgui_draw_list_get_vtx_data(ptr) ?: return data
        for (i in 0 until count) {
            val v = verts[vtxOffset + i]
            data.positions.add(v.pos_x)
            data.positions.add(v.pos_y)
            data.uvs.add(v.uv_x)
            data.uvs.add(v.uv_y)
            data.colors.add(v.col.toInt())
        }
        return data
    }

    override fun copyIdx(idxOffset: Int, count: Int): IntArray {
        val idx = imgui_draw_list_get_idx_data(ptr) ?: return IntArray(count)
        return IntArray(count) { i -> idx[idxOffset + i].toInt() }
    }

    override fun DrawLine(p1: ImVec2, p2: ImVec2, col: Int, thickness: Float) = memScoped {
        val a = alloc<imgui_vec2>()
        a.x = p1.x
        a.y = p1.y
        val b = alloc<imgui_vec2>()
        b.x = p2.x
        b.y = p2.y
        imgui_draw_list_add_line(ptr, a.readValue(), b.readValue(), col.toUInt(), thickness)
    }

    override fun DrawRect(pMin: ImVec2, pMax: ImVec2, col: Int, rounding: Float, flags: Int, thickness: Float) = memScoped {
        val min = alloc<imgui_vec2>()
        min.x = pMin.x
        min.y = pMin.y
        val max = alloc<imgui_vec2>()
        max.x = pMax.x
        max.y = pMax.y
        imgui_draw_list_add_rect(ptr, min.readValue(), max.readValue(), col.toUInt(), rounding, flags, thickness)
    }

    override fun DrawRectFilled(pMin: ImVec2, pMax: ImVec2, col: Int, rounding: Float, flags: Int) = memScoped {
        val min = alloc<imgui_vec2>()
        min.x = pMin.x
        min.y = pMin.y
        val max = alloc<imgui_vec2>()
        max.x = pMax.x
        max.y = pMax.y
        imgui_draw_list_add_rect_filled(ptr, min.readValue(), max.readValue(), col.toUInt(), rounding, flags)
    }

    override fun DrawCircle(center: ImVec2, radius: Float, col: Int, numSegments: Int, thickness: Float) = memScoped {
        val c = alloc<imgui_vec2>()
        c.x = center.x
        c.y = center.y
        imgui_draw_list_add_circle(ptr, c.readValue(), radius, col.toUInt(), numSegments, thickness)
    }

    override fun DrawCircleFilled(center: ImVec2, radius: Float, col: Int, numSegments: Int) = memScoped {
        val c = alloc<imgui_vec2>()
        c.x = center.x
        c.y = center.y
        imgui_draw_list_add_circle_filled(ptr, c.readValue(), radius, col.toUInt(), numSegments)
    }

    override fun DrawText(pos: ImVec2, text: String, col: Int) = memScoped {
        val p = alloc<imgui_vec2>()
        p.x = pos.x
        p.y = pos.y
        imgui_draw_list_add_text(ptr, p.readValue(), col.toUInt(), text)
    }

    override fun DrawQuad(p1: ImVec2, p2: ImVec2, p3: ImVec2, p4: ImVec2, col: Int, thickness: Float) = memScoped {
        val a = alloc<imgui_vec2>()
        a.x = p1.x
        a.y = p1.y
        val b = alloc<imgui_vec2>()
        b.x = p2.x
        b.y = p2.y
        val c = alloc<imgui_vec2>()
        c.x = p3.x
        c.y = p3.y
        val d = alloc<imgui_vec2>()
        d.x = p4.x
        d.y = p4.y
        imgui_draw_list_add_quad(ptr, a.readValue(), b.readValue(), c.readValue(), d.readValue(), col.toUInt(), thickness)
    }

    override fun DrawTriangle(p1: ImVec2, p2: ImVec2, p3: ImVec2, col: Int, thickness: Float) = memScoped {
        val a = alloc<imgui_vec2>()
        a.x = p1.x
        a.y = p1.y
        val b = alloc<imgui_vec2>()
        b.x = p2.x
        b.y = p2.y
        val c = alloc<imgui_vec2>()
        c.x = p3.x
        c.y = p3.y
        imgui_draw_list_add_triangle(ptr, a.readValue(), b.readValue(), c.readValue(), col.toUInt(), thickness)
    }

    override fun DrawPolyline(points: Array<ImVec2>, col: Int, closed: Boolean, thickness: Float) = memScoped {
        val pts = allocArray<imgui_vec2>(points.size)
        points.forEachIndexed { i, p ->
            pts[i].x = p.x
            pts[i].y = p.y
        }
        imgui_draw_list_add_polyline(ptr, pts, points.size, col.toUInt(), closed, thickness)
    }
}

internal class NativeImDrawData(internal val ptr: CPointer<imgui_draw_data>?) : ImDrawData {
    override val displayPos: ImVec2
        get() = imgui_draw_data_get_display_pos(ptr).useContents { ImVec2(x, y) }

    override val displaySize: ImVec2
        get() = imgui_draw_data_get_display_size(ptr).useContents { ImVec2(x, y) }

    override val framebufferScale: ImVec2
        get() = imgui_draw_data_get_framebuffer_scale(ptr).useContents { ImVec2(x, y) }

    override val cmdListsCount: Int
        get() = imgui_draw_data_get_cmd_lists_count(ptr)

    override fun cmdList(index: Int): ImDrawList = NativeImDrawList(imgui_draw_data_get_cmd_list(ptr, index))
}

// =========================================================================
// InputText buffers must outlive the call while the item is being edited.
// =========================================================================

private val inputBuffers = mutableMapOf<String, Pair<CPointer<ByteVar>, Int>>()

// =========================================================================
// Out-parameter helpers (the C API mutates these values in place)
// =========================================================================

private inline fun <T> withBoolVar(v: BooleanArray?, block: (CPointer<BooleanVar>?) -> T): T = memScoped {
    if (v == null) {
        block(null)
    } else {
        val b = alloc<BooleanVar>()
        b.value = v[0]
        val result = block(b.ptr)
        v[0] = b.value
        result
    }
}

private inline fun <T> withFloatVar(v: FloatArray, block: (CPointer<FloatVar>) -> T): T = memScoped {
    val f = alloc<FloatVar>()
    f.value = v[0]
    val result = block(f.ptr)
    v[0] = f.value
    result
}

private inline fun <T> withIntVar(v: IntArray, block: (CPointer<IntVar>) -> T): T = memScoped {
    val i = alloc<IntVar>()
    i.value = v[0]
    val result = block(i.ptr)
    v[0] = i.value
    result
}

// =========================================================================
// Platform clipboard bridge (installed by ImGui.setClipboardFunctions)
// =========================================================================
private var clipboardBridgeSetText: ((String) -> Unit)? = null
private var clipboardBridgeGetText: (() -> String?)? = null
private var clipboardGetCache: CPointer<ByteVar>? = null

/** Forwards to the Kotlin setter installed by the platform backend. */
private fun clipboardSetTrampoline(text: CPointer<ByteVar>?) {
    clipboardBridgeSetText?.invoke(text?.toKString() ?: "")
}

/**
 * Forwards to the Kotlin getter and returns a heap buffer that stays alive
 * until the next getter call (imgui's clipboard contract); the buffer is
 * freed on the next call or when the callbacks are uninstalled.
 */
private fun clipboardGetTrampoline(): CPointer<ByteVar>? {
    val text = clipboardBridgeGetText?.invoke() ?: return null
    clipboardGetCache?.let { nativeHeap.free(it) }
    val bytes = text.encodeToByteArray()
    val mem = nativeHeap.allocArray<ByteVar>(bytes.size + 1)
    bytes.forEachIndexed { i, b -> mem[i] = b.toByte() }
    mem[bytes.size] = 0
    clipboardGetCache = mem
    return mem
}

// =========================================================================
// actual object
// =========================================================================

actual object ImGui {
    actual fun createContext(): ImGuiContext {
        val ptr = imgui_create_context()
            ?: error("imgui_create_context returned null")
        return NativeImGuiContext(ptr)
    }

    actual fun destroyContext(context: ImGuiContext?) {
        if (context != null) {
            imgui_destroy_context((context as NativeImGuiContext).ptr)
        } else {
            imgui_destroy_context(null)
        }
    }

    actual fun getCurrentContext(): ImGuiContext? {
        val ptr = imgui_get_current_context()
        return if (ptr != null) NativeImGuiContext(ptr) else null
    }

    actual fun setCurrentContext(context: ImGuiContext) {
        imgui_set_current_context((context as NativeImGuiContext).ptr)
    }

    actual fun newFrame() = imgui_new_frame()
    actual fun render() = imgui_render()
    actual fun getDrawData(): ImDrawData = NativeImDrawData(imgui_get_draw_data())
    actual fun getIO(): ImGuiIO = NativeImGuiIO(imgui_get_io())
    actual fun getStyle(): ImGuiStyle = NativeImGuiStyle(imgui_get_style())
    actual fun getVersion(): String = imgui_get_version()?.toKString() ?: ""
    actual fun showDemoWindow(pOpen: BooleanArray?) = withBoolVar(pOpen) {
        imgui_show_demo_window(it)
    }

    actual fun showAboutWindow(pOpen: BooleanArray?) = withBoolVar(pOpen) {
        imgui_show_about_window(it)
    }

    actual fun showMetricsWindow(pOpen: BooleanArray?) = withBoolVar(pOpen) {
        imgui_show_metrics_window(it)
    }

    actual fun showDebugLogWindow(pOpen: BooleanArray?) = withBoolVar(pOpen) {
        imgui_show_debug_log_window(it)
    }

    actual fun showUserGuide() = imgui_show_user_guide()

    actual fun showIDStackToolWindow(pOpen: BooleanArray?) = withBoolVar(pOpen) {
        imgui_show_id_stack_tool_window(it)
    }

    // ---- Windows ----
    actual fun begin(name: String, pOpen: BooleanArray?, flags: Int): Boolean = withBoolVar(pOpen) {
        imgui_begin(name, it, flags)
    }

    actual fun end() = imgui_end()
    actual fun beginChild(id: String, size: ImVec2, childFlags: Int, windowFlags: Int): Boolean = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_begin_child(id, s.readValue(), childFlags, windowFlags)
    }

    actual fun endChild() = imgui_end_child()

    // ---- Docking ----
    actual fun dockSpace(id: Int, size: ImVec2, flags: Int): Int = imgui_dock_space(id, size.x, size.y, flags)
    actual fun setNextWindowDockID(dockId: Int, cond: Int) = imgui_set_next_window_dock_id(dockId, cond)
    actual fun dockBuilderAddNode(nodeId: Int, flags: Int): Int = imgui_dock_builder_add_node(nodeId, flags)
    actual fun dockBuilderRemoveNode(nodeId: Int) = imgui_dock_builder_remove_node(nodeId)
    actual fun dockBuilderSplitNode(nodeId: Int, splitDir: Int, sizeRatioForNodeAtDir: Float): Pair<Int, Int> = memScoped {
        val idAtDir = alloc<IntVar>()
        val idAtOppositeDir = alloc<IntVar>()
        imgui_dock_builder_split_node(nodeId, splitDir, sizeRatioForNodeAtDir, idAtDir.ptr, idAtOppositeDir.ptr)
        idAtDir.value to idAtOppositeDir.value
    }
    actual fun dockBuilderDockWindow(windowName: String, nodeId: Int) = imgui_dock_builder_dock_window(windowName, nodeId)
    actual fun dockBuilderFinish(nodeId: Int) = imgui_dock_builder_finish(nodeId)

    actual fun setNextWindowPos(pos: ImVec2, cond: Int, pivot: ImVec2?) = memScoped {
        val p = alloc<imgui_vec2>()
        p.x = pos.x
        p.y = pos.y
        val pi = alloc<imgui_vec2>()
        pi.x = pivot?.x ?: 0f
        pi.y = pivot?.y ?: 0f
        imgui_set_next_window_pos(p.readValue(), cond, pi.readValue())
    }

    actual fun setNextWindowSize(size: ImVec2, cond: Int) = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_set_next_window_size(s.readValue(), cond)
    }

    actual fun setWindowSize(size: ImVec2, cond: Int) = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_set_window_size(s.readValue(), cond)
    }

    actual fun setNextWindowBgAlpha(alpha: Float) = imgui_set_next_window_bg_alpha(alpha)
    actual fun beginDisabled(disabled: Boolean) = imgui_begin_disabled(disabled)
    actual fun endDisabled() = imgui_end_disabled()
    actual fun beginMainMenuBar(): Boolean = imgui_begin_main_menu_bar()
    actual fun endMainMenuBar() = imgui_end_main_menu_bar()
    actual fun beginMenuBar(): Boolean = imgui_begin_menu_bar()
    actual fun endMenuBar() = imgui_end_menu_bar()
    actual fun beginMenu(label: String, enabled: Boolean): Boolean = imgui_begin_menu(label, enabled)
    actual fun endMenu() = imgui_end_menu()
    actual fun menuItem(label: String, shortcut: String, selected: Boolean, enabled: Boolean): Boolean =
        imgui_menu_item(label, shortcut.ifEmpty { null }, selected, enabled)

    actual fun beginTabBar(id: String, flags: Int): Boolean = imgui_begin_tab_bar(id, flags)
    actual fun endTabBar() = imgui_end_tab_bar()
    actual fun beginTabItem(label: String, pOpen: BooleanArray?, flags: Int): Boolean = withBoolVar(pOpen) {
        imgui_begin_tab_item(label, it, flags)
    }

    actual fun endTabItem() = imgui_end_tab_item()
    actual fun beginTooltip(): Boolean = imgui_begin_tooltip()
    actual fun endTooltip() = imgui_end_tooltip()
    actual fun setTooltip(text: String) = imgui_set_tooltip(text)
    actual fun openPopup(id: String, popupFlags: Int) = imgui_open_popup(id, popupFlags)
    actual fun beginPopup(id: String, flags: Int): Boolean = imgui_begin_popup(id, flags)
    actual fun beginPopupModal(name: String, pOpen: BooleanArray?, flags: Int): Boolean = withBoolVar(pOpen) {
        imgui_begin_popup_modal(name, it, flags)
    }

    actual fun endPopup() = imgui_end_popup()
    actual fun closeCurrentPopup() = imgui_close_current_popup()
    actual fun beginPopupContextItem(strId: String?, popupFlags: Int): Boolean =
        imgui_begin_popup_context_item(strId, popupFlags)

    actual fun beginPopupContextWindow(strId: String?, popupFlags: Int): Boolean =
        imgui_begin_popup_context_window(strId, popupFlags)

    actual fun beginItemTooltip(): Boolean = imgui_begin_item_tooltip()
    actual fun openPopupOnItemClick(strId: String?, popupFlags: Int) {
        imgui_open_popup_on_item_click(strId, popupFlags)
    }
    actual fun beginCombo(label: String, previewValue: String, flags: Int): Boolean = imgui_begin_combo(label, previewValue, flags)
    actual fun endCombo() = imgui_end_combo()

    // ---- Drag and drop ----
    actual fun beginDragDropSource(flags: Int): Boolean = imgui_begin_drag_drop_source(flags)
    actual fun setDragDropPayload(type: String, data: ByteArray, cond: Int): Boolean =
        data.usePinned { imgui_set_drag_drop_payload(type, it.addressOf(0), data.size, cond) }

    actual fun endDragDropSource() = imgui_end_drag_drop_source()
    actual fun beginDragDropTarget(): Boolean = imgui_begin_drag_drop_target()

    actual fun acceptDragDropPayload(type: String, flags: Int): ByteArray? = memScoped {
        val size = alloc<IntVar>()
        val data = imgui_accept_drag_drop_payload(type, flags, size.ptr)
        if (data == null || size.value <= 0) {
            null
        } else {
            val bytes = data.reinterpret<ByteVar>()
            ByteArray(size.value) { i -> bytes[i] }
        }
    }

    actual fun endDragDropTarget() = imgui_end_drag_drop_target()
    actual fun getDragDropPayload(): String? = imgui_get_drag_drop_payload_type()?.toKString()

    // ---- Images ----
    actual fun image(texId: Long, size: ImVec2, uv0: ImVec2, uv1: ImVec2, tintColor: ImVec4, borderColor: ImVec4) = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        val u0 = alloc<imgui_vec2>()
        u0.x = uv0.x
        u0.y = uv0.y
        val u1 = alloc<imgui_vec2>()
        u1.x = uv1.x
        u1.y = uv1.y
        val tint = alloc<imgui_vec4>()
        tint.x = tintColor.x
        tint.y = tintColor.y
        tint.z = tintColor.z
        tint.w = tintColor.w
        val border = alloc<imgui_vec4>()
        border.x = borderColor.x
        border.y = borderColor.y
        border.z = borderColor.z
        border.w = borderColor.w
        imgui_image(texId.convert(), s.readValue(), u0.readValue(), u1.readValue(), tint.readValue(), border.readValue())
    }

    actual fun imageButton(texId: Long, size: ImVec2, uv0: ImVec2, uv1: ImVec2, framePadding: Int, bgColor: ImVec4, tintColor: ImVec4): Boolean = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        val u0 = alloc<imgui_vec2>()
        u0.x = uv0.x
        u0.y = uv0.y
        val u1 = alloc<imgui_vec2>()
        u1.x = uv1.x
        u1.y = uv1.y
        val bg = alloc<imgui_vec4>()
        bg.x = bgColor.x
        bg.y = bgColor.y
        bg.z = bgColor.z
        bg.w = bgColor.w
        val tint = alloc<imgui_vec4>()
        tint.x = tintColor.x
        tint.y = tintColor.y
        tint.z = tintColor.z
        tint.w = tintColor.w
        imgui_image_button(texId.convert(), s.readValue(), u0.readValue(), u1.readValue(), framePadding, bg.readValue(), tint.readValue())
    }

    actual fun imageWithBg(texId: Long, size: ImVec2, bgColor: ImVec4, uv0: ImVec2, uv1: ImVec2) = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        val bg = alloc<imgui_vec4>()
        bg.x = bgColor.x
        bg.y = bgColor.y
        bg.z = bgColor.z
        bg.w = bgColor.w
        val u0 = alloc<imgui_vec2>()
        u0.x = uv0.x
        u0.y = uv0.y
        val u1 = alloc<imgui_vec2>()
        u1.x = uv1.x
        u1.y = uv1.y
        imgui_image_with_bg(texId.convert(), s.readValue(), bg.readValue(), u0.readValue(), u1.readValue())
    }

    // ---- ListBox ----
    actual fun beginListBox(label: String, size: ImVec2): Boolean = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_begin_list_box(label, s.readValue())
    }

    actual fun endListBox() = imgui_end_list_box()

    actual fun listBox(label: String, currentItem: IntArray, items: Array<String>): Boolean = memScoped {
        val ptrs = allocArray<CPointerVar<ByteVar>>(items.size)
        items.forEachIndexed { i, s ->
            ptrs[i] = s.cstr.ptr
        }
        withIntVar(currentItem) { cur ->
            imgui_list_box(label, cur, ptrs, items.size)
        }
    }

    // ---- MultiSelect ----
    actual fun beginMultiSelect(flags: Int, selectionSize: Int, itemsCount: Int): Long =
        imgui_begin_multi_select(flags, selectionSize, itemsCount)?.rawValue?.toLong() ?: 0L

    actual fun endMultiSelect(): Long =
        imgui_end_multi_select()?.rawValue?.toLong() ?: 0L

    // ---- Logging ----
    actual fun logToClipboard(autoOpenDepth: Int) = imgui_log_to_clipboard(autoOpenDepth)
    actual fun logToFile(autoOpenDepth: Int, filename: String?) = imgui_log_to_file(autoOpenDepth, filename)
    actual fun logToTTY(autoOpenDepth: Int) = imgui_log_to_tty(autoOpenDepth)
    actual fun logFinish() = imgui_log_finish()
    actual fun logText(text: String) = imgui_log_text(text)

    // ---- .ini settings ----
    actual fun saveIniSettingsToDisk(iniFilename: String?) = imgui_save_ini_settings_to_disk(iniFilename)
    actual fun loadIniSettingsFromDisk(iniFilename: String?) = imgui_load_ini_settings_from_disk(iniFilename)
    actual fun saveIniSettingsToMemory(): String? = imgui_save_ini_settings_to_memory()?.toKString()
    actual fun loadIniSettingsFromMemory(iniData: String) = imgui_load_ini_settings_from_memory(iniData)

    // ---- Scissor rect / text wrapping ----
    actual fun pushClipRect(clipRectMin: ImVec2, clipRectMax: ImVec2, intersectWithCurrentClipRect: Boolean) = memScoped {
        val min = alloc<imgui_vec2>()
        min.x = clipRectMin.x
        min.y = clipRectMin.y
        val max = alloc<imgui_vec2>()
        max.x = clipRectMax.x
        max.y = clipRectMax.y
        imgui_push_clip_rect(min.readValue(), max.readValue(), intersectWithCurrentClipRect)
    }

    actual fun popClipRect() = imgui_pop_clip_rect()
    actual fun pushTextWrapPos(wrapLocalPosX: Float) = imgui_push_text_wrap_pos(wrapLocalPosX)
    actual fun popTextWrapPos() = imgui_pop_text_wrap_pos()

    // ---- Widgets ----
    actual fun text(text: String) = imgui_text(text)
    actual fun textWrapped(text: String) = imgui_text_wrapped(text)
    actual fun textUnformatted(text: String) = imgui_text_unformatted(text)
    actual fun textLink(text: String): Boolean = imgui_text_link(text)
    actual fun textLinkOpenURL(label: String, url: String?): Boolean = imgui_text_link_open_url(label, url)
    actual fun textColored(color: ImVec4, text: String) = memScoped {
        val c = alloc<imgui_vec4>()
        c.x = color.x
        c.y = color.y
        c.z = color.z
        c.w = color.w
        imgui_text_colored(c.readValue(), text)
    }

    actual fun textDisabled(text: String) = imgui_text_disabled(text)
    actual fun labelText(label: String, text: String) = imgui_label_text(label, text)
    actual fun bulletText(text: String) = imgui_bullet_text(text)
    actual fun bullet() = imgui_bullet()
    actual fun alignTextToFramePadding() = imgui_align_text_to_frame_padding()
    actual fun separator() = imgui_separator()
    actual fun separatorText(text: String) = imgui_separator_text(text)
    actual fun sameLine(offsetFromStartX: Float, spacing: Float) = imgui_same_line(offsetFromStartX, spacing)
    actual fun newLine() = imgui_new_line()
    actual fun spacing() = imgui_spacing()
    actual fun dummy(size: ImVec2) = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_dummy(s.readValue())
    }

    actual fun indent(indentW: Float) = imgui_indent(indentW)
    actual fun unindent(indentW: Float) = imgui_unindent(indentW)
    actual fun button(label: String, size: ImVec2): Boolean = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_button(label, s.readValue())
    }

    actual fun smallButton(label: String): Boolean = imgui_small_button(label)
    actual fun arrowButton(strId: String, dir: Int): Boolean = imgui_arrow_button(strId, dir)
    actual fun checkbox(label: String, v: BooleanArray): Boolean = withBoolVar(v) {
        imgui_checkbox(label, it)
    }

    actual fun checkboxFlags(label: String, flags: IntArray, flagsValue: Int): Boolean =
        imgui_checkbox_flags(label, flags.usePinned { it.addressOf(0) }, flagsValue)

    actual fun pushItemFlag(flag: Int, enabled: Boolean) = imgui_push_item_flag(flag, enabled)
    actual fun popItemFlag() = imgui_pop_item_flag()

    actual fun shortcut(keyChord: Int, flags: Int): Boolean = imgui_shortcut(keyChord, flags)

    actual fun sliderFloat(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String): Boolean = withFloatVar(v) {
        imgui_slider_float(label, it, vMin, vMax, format)
    }

    actual fun sliderInt(label: String, v: IntArray, vMin: Int, vMax: Int, format: String): Boolean = withIntVar(v) {
        imgui_slider_int(label, it, vMin, vMax, format)
    }

    actual fun dragFloat(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        imgui_drag_float(label, v.usePinned { it.addressOf(0) }, vSpeed, vMin, vMax, format, flags)

    actual fun dragFloat2(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        imgui_drag_float2(label, v.usePinned { it.addressOf(0) }, vSpeed, vMin, vMax, format, flags)

    actual fun dragFloat3(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        imgui_drag_float3(label, v.usePinned { it.addressOf(0) }, vSpeed, vMin, vMax, format, flags)

    actual fun dragFloat4(label: String, v: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        imgui_drag_float4(label, v.usePinned { it.addressOf(0) }, vSpeed, vMin, vMax, format, flags)

    actual fun dragFloatRange2(label: String, vCurrentMin: FloatArray, vCurrentMax: FloatArray, vSpeed: Float, vMin: Float, vMax: Float, format: String, formatMax: String?, flags: Int): Boolean =
        vCurrentMin.usePinned { min ->
            vCurrentMax.usePinned { max ->
                imgui_drag_float_range2(label, min.addressOf(0), max.addressOf(0), vSpeed, vMin, vMax, format, formatMax, flags)
            }
        }

    actual fun dragInt(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        imgui_drag_int(label, v.usePinned { it.addressOf(0) }, vSpeed, vMin, vMax, format, flags)

    actual fun dragInt2(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        imgui_drag_int2(label, v.usePinned { it.addressOf(0) }, vSpeed, vMin, vMax, format, flags)

    actual fun dragInt3(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        imgui_drag_int3(label, v.usePinned { it.addressOf(0) }, vSpeed, vMin, vMax, format, flags)

    actual fun dragInt4(label: String, v: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        imgui_drag_int4(label, v.usePinned { it.addressOf(0) }, vSpeed, vMin, vMax, format, flags)

    actual fun dragIntRange2(label: String, vCurrentMin: IntArray, vCurrentMax: IntArray, vSpeed: Float, vMin: Int, vMax: Int, format: String, formatMax: String?, flags: Int): Boolean =
        vCurrentMin.usePinned { min ->
            vCurrentMax.usePinned { max ->
                imgui_drag_int_range2(label, min.addressOf(0), max.addressOf(0), vSpeed, vMin, vMax, format, formatMax, flags)
            }
        }

    actual fun sliderFloat2(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        imgui_slider_float2(label, v.usePinned { it.addressOf(0) }, vMin, vMax, format, flags)

    actual fun sliderFloat3(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        imgui_slider_float3(label, v.usePinned { it.addressOf(0) }, vMin, vMax, format, flags)

    actual fun sliderFloat4(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean =
        imgui_slider_float4(label, v.usePinned { it.addressOf(0) }, vMin, vMax, format, flags)

    actual fun sliderInt2(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        imgui_slider_int2(label, v.usePinned { it.addressOf(0) }, vMin, vMax, format, flags)

    actual fun sliderInt3(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        imgui_slider_int3(label, v.usePinned { it.addressOf(0) }, vMin, vMax, format, flags)

    actual fun sliderInt4(label: String, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean =
        imgui_slider_int4(label, v.usePinned { it.addressOf(0) }, vMin, vMax, format, flags)

    actual fun sliderAngle(label: String, vRad: FloatArray, vDegreesMin: Float, vDegreesMax: Float, format: String, flags: Int): Boolean =
        imgui_slider_angle(label, vRad.usePinned { it.addressOf(0) }, vDegreesMin, vDegreesMax, format, flags)

    actual fun vSliderFloat(label: String, size: ImVec2, v: FloatArray, vMin: Float, vMax: Float, format: String, flags: Int): Boolean = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_vslider_float(label, s.readValue(), v.usePinned { it.addressOf(0) }, vMin, vMax, format, flags)
    }

    actual fun vSliderInt(label: String, size: ImVec2, v: IntArray, vMin: Int, vMax: Int, format: String, flags: Int): Boolean = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_vslider_int(label, s.readValue(), v.usePinned { it.addressOf(0) }, vMin, vMax, format, flags)
    }

    actual fun sliderScalar(label: String, dataType: Int, v: LongArray, vMin: LongArray, vMax: LongArray, format: String): Boolean =
        v.usePinned { vp ->
            vMin.usePinned { minp ->
                vMax.usePinned { maxp ->
                    imgui_slider_scalar(label, dataType, vp.addressOf(0), minp.addressOf(0), maxp.addressOf(0), format)
                }
            }
        }

    actual fun dragScalar(label: String, dataType: Int, v: LongArray, vSpeed: Float, vMin: LongArray, vMax: LongArray, format: String): Boolean =
        v.usePinned { vp ->
            vMin.usePinned { minp ->
                vMax.usePinned { maxp ->
                    imgui_drag_scalar(label, dataType, vp.addressOf(0), vSpeed, minp.addressOf(0), maxp.addressOf(0), format)
                }
            }
        }

    actual fun inputFloat(label: String, v: FloatArray, step: Float, stepFast: Float, format: String, flags: Int): Boolean =
        imgui_input_float(label, v.usePinned { it.addressOf(0) }, step, stepFast, format, flags)

    actual fun inputFloat2(label: String, v: FloatArray, format: String, flags: Int): Boolean =
        imgui_input_float2(label, v.usePinned { it.addressOf(0) }, format, flags)

    actual fun inputFloat3(label: String, v: FloatArray, format: String, flags: Int): Boolean =
        imgui_input_float3(label, v.usePinned { it.addressOf(0) }, format, flags)

    actual fun inputFloat4(label: String, v: FloatArray, format: String, flags: Int): Boolean =
        imgui_input_float4(label, v.usePinned { it.addressOf(0) }, format, flags)

    actual fun inputInt(label: String, v: IntArray, step: Int, stepFast: Int, flags: Int): Boolean =
        imgui_input_int(label, v.usePinned { it.addressOf(0) }, step, stepFast, flags)

    actual fun inputInt2(label: String, v: IntArray, flags: Int): Boolean =
        imgui_input_int2(label, v.usePinned { it.addressOf(0) }, flags)

    actual fun inputInt3(label: String, v: IntArray, flags: Int): Boolean =
        imgui_input_int3(label, v.usePinned { it.addressOf(0) }, flags)

    actual fun inputInt4(label: String, v: IntArray, flags: Int): Boolean =
        imgui_input_int4(label, v.usePinned { it.addressOf(0) }, flags)

    actual fun inputDouble(label: String, v: DoubleArray, step: Double, stepFast: Double, format: String, flags: Int): Boolean =
        imgui_input_double(label, v.usePinned { it.addressOf(0) }, step, stepFast, format, flags)

    actual fun colorEdit3(label: String, col: FloatArray, flags: Int): Boolean =
        imgui_color_edit3(label, col.usePinned { it.addressOf(0) }, flags)

    actual fun colorEdit4(label: String, col: FloatArray, flags: Int): Boolean =
        imgui_color_edit4(label, col.usePinned { it.addressOf(0) }, flags)

    actual fun colorPicker3(label: String, col: FloatArray, flags: Int): Boolean =
        imgui_color_picker3(label, col.usePinned { it.addressOf(0) }, flags)

    actual fun colorPicker4(label: String, col: FloatArray, flags: Int): Boolean =
        imgui_color_picker4(label, col.usePinned { it.addressOf(0) }, flags)

    actual fun colorButton(descId: String, col: ImVec4, flags: Int, size: ImVec2): Boolean = memScoped {
        val c = alloc<imgui_vec4>()
        c.x = col.x
        c.y = col.y
        c.z = col.z
        c.w = col.w
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_color_button(descId, c.readValue(), flags, s.readValue())
    }

    actual fun setColorEditOptions(flags: Int) = imgui_set_color_edit_options(flags)

    actual fun colorConvertFloat4ToU32(`in`: ImVec4): Int = memScoped {
        val c = alloc<imgui_vec4>()
        c.x = `in`.x
        c.y = `in`.y
        c.z = `in`.z
        c.w = `in`.w
        imgui_color_convert_float4_to_u32(c.readValue()).toInt()
    }

    actual fun colorConvertU32ToFloat4(`in`: Int): ImVec4 =
        imgui_color_convert_u32_to_float4(`in`.convert()).useContents { ImVec4(x, y, z, w) }

    actual fun colorConvertRGBtoHSV(r: Float, g: Float, b: Float, outH: FloatArray, outS: FloatArray, outV: FloatArray) = memScoped {
        val h = alloc<FloatVar>()
        val s = alloc<FloatVar>()
        val v = alloc<FloatVar>()
        imgui_color_convert_rgb_to_hsv(r, g, b, h.ptr, s.ptr, v.ptr)
        outH[0] = h.value
        outS[0] = s.value
        outV[0] = v.value
    }

    actual fun colorConvertHSVtoRGB(h: Float, s: Float, v: Float, outR: FloatArray, outG: FloatArray, outB: FloatArray) = memScoped {
        val r = alloc<FloatVar>()
        val g = alloc<FloatVar>()
        val b = alloc<FloatVar>()
        imgui_color_convert_hsv_to_rgb(h, s, v, r.ptr, g.ptr, b.ptr)
        outR[0] = r.value
        outG[0] = g.value
        outB[0] = b.value
    }

    actual fun inputText(label: String, buf: String, flags: Int): String? {
        val initial = buf.encodeToByteArray()
        val needed = initial.size + 512
        val existing = inputBuffers[label]
        if (existing == null || existing.second < needed) {
            if (existing != null) {
                nativeHeap.free(existing.first)
            }
            inputBuffers[label] = nativeHeap.allocArray<ByteVar>(needed) to needed
        }
        val (ptr, capacity) = inputBuffers[label]!!
        initial.forEachIndexed { i, b -> ptr[i] = b }
        ptr[initial.size] = 0
        imgui_input_text(label, ptr, capacity, flags)
        val out = ptr.toKString()
        if (!imgui_is_item_active()) {
            nativeHeap.free(ptr)
            inputBuffers.remove(label)
        }
        return out
    }

    actual fun inputTextMultiline(label: String, buf: String, size: ImVec2, flags: Int): String? {
        val key = "##imgui_kmp_ml_" + label
        val initial = buf.encodeToByteArray()
        val needed = initial.size + 2048
        val existing = inputBuffers[key]
        if (existing == null || existing.second < needed) {
            if (existing != null) {
                nativeHeap.free(existing.first)
            }
            inputBuffers[key] = nativeHeap.allocArray<ByteVar>(needed) to needed
        }
        val (ptr, capacity) = inputBuffers[key]!!
        initial.forEachIndexed { i, b -> ptr[i] = b }
        ptr[initial.size] = 0
        memScoped {
            val s = alloc<imgui_vec2>()
            s.x = size.x
            s.y = size.y
            imgui_input_text_multiline(label, ptr, capacity, s.readValue(), flags)
        }
        val out = ptr.toKString()
        if (!imgui_is_item_active()) {
            nativeHeap.free(ptr)
            inputBuffers.remove(key)
        }
        return out
    }

    actual fun inputTextWithHint(label: String, hint: String, buf: String, flags: Int): String? {
        val key = "##imgui_kmp_hint_" + label
        val initial = buf.encodeToByteArray()
        val needed = initial.size + 512
        val existing = inputBuffers[key]
        if (existing == null || existing.second < needed) {
            if (existing != null) {
                nativeHeap.free(existing.first)
            }
            inputBuffers[key] = nativeHeap.allocArray<ByteVar>(needed) to needed
        }
        val (ptr, capacity) = inputBuffers[key]!!
        initial.forEachIndexed { i, b -> ptr[i] = b }
        ptr[initial.size] = 0
        imgui_input_text_with_hint(label, hint, ptr, capacity, flags)
        val out = ptr.toKString()
        if (!imgui_is_item_active()) {
            nativeHeap.free(ptr)
            inputBuffers.remove(key)
        }
        return out
    }

    actual fun combo(label: String, currentItem: IntArray, items: Array<String>): Boolean = memScoped {
        val ptrs = allocArray<CPointerVar<ByteVar>>(items.size)
        items.forEachIndexed { i, s ->
            ptrs[i] = s.cstr.ptr
        }
        withIntVar(currentItem) { cur ->
            imgui_combo(label, cur, ptrs, items.size)
        }
    }

    actual fun selectable(label: String, selected: Boolean, flags: Int, size: ImVec2): Boolean = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_selectable(label, selected, flags, s.readValue())
    }

    actual fun radioButton(label: String, active: Boolean): Boolean = imgui_radio_button(label, active)
    actual fun progressBar(fraction: Float, size: ImVec2, overlay: String?) = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_progress_bar(fraction, s.readValue(), overlay)
    }

    actual fun collapsingHeader(label: String, flags: Int): Boolean = imgui_collapsing_header(label, flags)
    actual fun treeNode(label: String): Boolean = imgui_tree_node(label)
    actual fun treeNodeEx(label: String, flags: Int): Boolean = imgui_tree_node_ex(label, flags)
    actual fun treeNodeGetOpen(label: String): Boolean = imgui_tree_node_get_open(label)
    actual fun treePush(strId: String?) = imgui_tree_push(strId)
    actual fun treePop() = imgui_tree_pop()
    actual fun invisibleButton(id: String, size: ImVec2, flags: Int): Boolean = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_invisible_button(id, s.readValue(), flags)
    }

    actual fun beginGroup() = imgui_begin_group()
    actual fun endGroup() = imgui_end_group()
    actual fun setCursorPos(pos: ImVec2) = memScoped {
        val p = alloc<imgui_vec2>()
        p.x = pos.x
        p.y = pos.y
        imgui_set_cursor_pos(p.readValue())
    }

    actual fun pushId(id: String) = imgui_push_id(id)
    actual fun popId() = imgui_pop_id()

    // ---- Item queries ----
    actual fun isItemHovered(flags: Int): Boolean = imgui_is_item_hovered(flags)
    actual fun isItemActive(): Boolean = imgui_is_item_active()
    actual fun isItemClicked(mouseButton: Int): Boolean = imgui_is_item_clicked(mouseButton)
    actual fun isItemFocused(): Boolean = imgui_is_item_focused()
    actual fun isItemVisible(): Boolean = imgui_is_item_visible()
    actual fun isItemEdited(): Boolean = imgui_is_item_edited()
    actual fun isItemActivated(): Boolean = imgui_is_item_activated()
    actual fun isItemDeactivated(): Boolean = imgui_is_item_deactivated()
    actual fun isItemDeactivatedAfterEdit(): Boolean = imgui_is_item_deactivated_after_edit()
    actual fun isItemToggledOpen(): Boolean = imgui_is_item_toggled_open()
    actual fun isItemToggledSelection(): Boolean = imgui_is_item_toggled_selection()
    actual fun isAnyItemHovered(): Boolean = imgui_is_any_item_hovered()
    actual fun isAnyItemActive(): Boolean = imgui_is_any_item_active()
    actual fun isAnyItemFocused(): Boolean = imgui_is_any_item_focused()
    actual fun getItemID(): Int = imgui_get_item_id()
    actual fun getItemFlags(): Int = imgui_get_item_flags()
    actual fun getItemRectMin(): ImVec2 = imgui_get_item_rect_min().useContents { ImVec2(x, y) }
    actual fun getItemRectMax(): ImVec2 = imgui_get_item_rect_max().useContents { ImVec2(x, y) }
    actual fun getItemRectSize(): ImVec2 = imgui_get_item_rect_size().useContents { ImVec2(x, y) }

    // ---- Window state ----
    actual fun isWindowHovered(flags: Int): Boolean = imgui_is_window_hovered(flags)
    actual fun isWindowFocused(flags: Int): Boolean = imgui_is_window_focused(flags)
    actual fun isWindowAppearing(): Boolean = imgui_is_window_appearing()
    actual fun isWindowCollapsed(): Boolean = imgui_is_window_collapsed()
    actual fun isRectVisible(size: ImVec2): Boolean = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_is_rect_visible(s.readValue())
    }

    actual fun isPopupOpen(strId: String, flags: Int): Boolean = imgui_is_popup_open(strId, flags)
    actual fun getWindowPos(): ImVec2 = imgui_get_window_pos().useContents { ImVec2(x, y) }
    actual fun getWindowSize(): ImVec2 = imgui_get_window_size().useContents { ImVec2(x, y) }
    actual fun getWindowWidth(): Float = imgui_get_window_width()
    actual fun getWindowHeight(): Float = imgui_get_window_height()
    actual fun getWindowContentRegionMax(): ImVec2 = imgui_get_window_content_region_max().useContents { ImVec2(x, y) }
    actual fun getWindowContentRegionMin(): ImVec2 = imgui_get_window_content_region_min().useContents { ImVec2(x, y) }
    actual fun getWindowDrawList(): ImDrawList = NativeImDrawList(imgui_get_window_draw_list())
    actual fun getForegroundDrawList(): ImDrawList = NativeImDrawList(imgui_get_foreground_draw_list())
    actual fun getBackgroundDrawList(): ImDrawList = NativeImDrawList(imgui_get_background_draw_list())

    // ---- Keyboard / mouse ----
    actual fun isKeyDown(key: Int): Boolean = imgui_is_key_down(key)
    actual fun isKeyPressed(key: Int, repeat: Boolean): Boolean = imgui_is_key_pressed(key, repeat)
    actual fun isKeyReleased(key: Int): Boolean = imgui_is_key_released(key)
    actual fun isMouseDown(button: Int): Boolean = imgui_is_mouse_down(button)
    actual fun isMouseClicked(button: Int, repeat: Boolean): Boolean = imgui_is_mouse_clicked(button, repeat)
    actual fun isMouseReleased(button: Int): Boolean = imgui_is_mouse_released(button)
    actual fun isMouseDoubleClicked(button: Int): Boolean = imgui_is_mouse_double_clicked(button)
    actual fun isMouseDragging(button: Int, lockThreshold: Float): Boolean = imgui_is_mouse_dragging(button, lockThreshold)
    actual fun isAnyMouseDown(): Boolean = imgui_is_any_mouse_down()
    actual fun isMousePosValid(mousePos: ImVec2?): Boolean = memScoped {
        val p = alloc<imgui_vec2>()
        p.x = mousePos?.x ?: 0f
        p.y = mousePos?.y ?: 0f
        imgui_is_mouse_pos_valid(if (mousePos != null) p.ptr else null)
    }

    actual fun getMousePos(): ImVec2 = imgui_get_mouse_pos().useContents { ImVec2(x, y) }
    actual fun getMouseDragDelta(button: Int, lockThreshold: Float): ImVec2 =
        imgui_get_mouse_drag_delta(button, lockThreshold).useContents { ImVec2(x, y) }

    actual fun resetMouseDragDelta(button: Int) = imgui_reset_mouse_drag_delta(button)
    actual fun getMouseCursor(): Int = imgui_get_mouse_cursor()
    actual fun setMouseCursor(cursor: Int) = imgui_set_mouse_cursor(cursor)
    actual fun setKeyboardFocusHere(offset: Int) = imgui_set_keyboard_focus_here(offset)
    actual fun setNextFrameWantCaptureKeyboard(wantCaptureKeyboard: Boolean) = imgui_set_next_frame_want_capture_keyboard(wantCaptureKeyboard)
    actual fun setNextFrameWantCaptureMouse(wantCaptureMouse: Boolean) = imgui_set_next_frame_want_capture_mouse(wantCaptureMouse)
    actual fun setClipboardText(text: String) = imgui_set_clipboard_text(text)
    actual fun getClipboardText(): String? = imgui_get_clipboard_text()?.toKString()

    actual fun setClipboardFunctions(setText: ((String) -> Unit)?, getText: (() -> String?)?) {
        clipboardBridgeSetText = setText
        clipboardBridgeGetText = getText
        // A getter's returned C string must stay valid until the next getter
        // call, so keep the last result alive in a heap buffer.
        if (setText != null || getText != null) {
            imgui_set_clipboard_callbacks(
                if (setText != null) staticCFunction(::clipboardSetTrampoline) else null,
                if (getText != null) staticCFunction(::clipboardGetTrampoline) else null,
            )
        } else {
            imgui_set_clipboard_callbacks(null, null)
            clipboardGetCache?.let { nativeHeap.free(it) }
            clipboardGetCache = null
        }
    }

    // ---- Cursor / scroll / layout ----
    actual fun getCursorPos(): ImVec2 = imgui_get_cursor_pos().useContents { ImVec2(x, y) }
    actual fun getCursorScreenPos(): ImVec2 = imgui_get_cursor_screen_pos().useContents { ImVec2(x, y) }
    actual fun getCursorStartPos(): ImVec2 = imgui_get_cursor_start_pos().useContents { ImVec2(x, y) }
    actual fun setCursorPosX(localX: Float) = imgui_set_cursor_pos_x(localX)
    actual fun setCursorScreenPos(pos: ImVec2) = memScoped {
        val p = alloc<imgui_vec2>()
        p.x = pos.x
        p.y = pos.y
        imgui_set_cursor_screen_pos(p.readValue())
    }

    actual fun getContentRegionAvail(): ImVec2 = imgui_get_content_region_avail().useContents { ImVec2(x, y) }
    actual fun getScrollX(): Float = imgui_get_scroll_x()
    actual fun getScrollY(): Float = imgui_get_scroll_y()
    actual fun getScrollMaxX(): Float = imgui_get_scroll_max_x()
    actual fun getScrollMaxY(): Float = imgui_get_scroll_max_y()
    actual fun setScrollHereX(centerXRatio: Float) = imgui_set_scroll_here_x(centerXRatio)
    actual fun setScrollHereY(centerYRatio: Float) = imgui_set_scroll_here_y(centerYRatio)
    actual fun setScrollFromPosX(localX: Float, centerXRatio: Float) = imgui_set_scroll_from_pos_x(localX, centerXRatio)
    actual fun setScrollFromPosY(localY: Float, centerYRatio: Float) = imgui_set_scroll_from_pos_y(localY, centerYRatio)
    actual fun setScrollX(scrollX: Float) = imgui_set_scroll_x(scrollX)
    actual fun setScrollY(scrollY: Float) = imgui_set_scroll_y(scrollY)

    // ---- Other queries ----
    actual fun getFrameCount(): Int = imgui_get_frame_count()
    actual fun getFrameHeight(): Float = imgui_get_frame_height()
    actual fun getFrameHeightWithSpacing(): Float = imgui_get_frame_height_with_spacing()
    actual fun getFontSize(): Float = imgui_get_font_size()
    actual fun getFont(): Long = imgui_get_font()?.rawValue?.toLong() ?: 0L
    actual fun getMainViewport(): Long = imgui_get_main_viewport()?.rawValue?.toLong() ?: 0L
    actual fun getStyleColorVec4(idx: Int): ImVec4 =
        imgui_get_style_color_vec4(idx).useContents { ImVec4(x, y, z, w) }

    actual fun getCursorPosX(): Float = imgui_get_cursor_pos_x()
    actual fun getKeyName(key: Int): String = imgui_get_key_name(key)?.toKString() ?: ""
    actual fun getTextLineHeight(): Float = imgui_get_text_line_height()
    actual fun getTextLineHeightWithSpacing(): Float = imgui_get_text_line_height_with_spacing()
    actual fun getID(strId: String): Int = imgui_get_id(strId)
    actual fun getColorU32(idx: Int, alphaMul: Float): Int = imgui_get_color_u32(idx, alphaMul)
    actual fun getStyleColorName(idx: Int): String = imgui_get_style_color_name(idx)?.toKString() ?: ""
    actual fun calcTextSize(text: String, hideTextAfterDoubleHash: Boolean, wrapWidth: Float): ImVec2 =
        imgui_calc_text_size(text, hideTextAfterDoubleHash, wrapWidth).useContents { ImVec2(x, y) }

    actual fun calcItemWidth(): Float = imgui_calc_item_width()
    actual fun getTime(): Double = imgui_get_time()

    // ---- Columns (legacy multi-column layout) ----
    actual fun columns(count: Int, id: String?, border: Boolean) = imgui_columns(count, id, border)
    actual fun nextColumn() = imgui_next_column()
    actual fun getColumnIndex(): Int = imgui_get_column_index()
    actual fun getColumnOffset(columnIndex: Int): Float = imgui_get_column_offset(columnIndex)
    actual fun setColumnOffset(columnIndex: Int, offsetX: Float) = imgui_set_column_offset(columnIndex, offsetX)
    actual fun getColumnWidth(columnIndex: Int): Float = imgui_get_column_width(columnIndex)
    actual fun setColumnWidth(columnIndex: Int, width: Float) = imgui_set_column_width(columnIndex, width)
    actual fun getColumnsCount(): Int = imgui_get_columns_count()

    // ---- Tables ----
    actual fun beginTable(id: String, column: Int, flags: Int, outerSize: ImVec2, innerWidth: Float): Boolean = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = outerSize.x
        s.y = outerSize.y
        imgui_begin_table(id, column, flags, s.readValue(), innerWidth)
    }

    actual fun endTable() = imgui_end_table()
    actual fun tableNextRow(minRowHeight: Int, flags: Int) = imgui_table_next_row(minRowHeight, flags)
    actual fun tableNextColumn(): Boolean = imgui_table_next_column()
    actual fun tableSetColumnIndex(columnIndex: Int): Boolean = imgui_table_set_column_index(columnIndex)
    actual fun tableSetupColumn(label: String, flags: Int, initWidthOrWeight: Float, userId: Int) =
        imgui_table_setup_column(label, flags, initWidthOrWeight, userId)

    actual fun tableSetupScrollFreeze(cols: Int, rows: Int) = imgui_table_setup_scroll_freeze(cols, rows)
    actual fun tableHeadersRow() = imgui_table_headers_row()
    actual fun tableHeader(label: String) = imgui_table_header(label)
    actual fun tableAngledHeadersRow() = imgui_table_angled_headers_row()
    actual fun tableGetColumnCount(): Int = imgui_table_get_column_count()
    actual fun tableGetColumnFlags(columnN: Int): Int = imgui_table_get_column_flags(columnN)
    actual fun tableGetColumnIndex(): Int = imgui_table_get_column_index()
    actual fun tableGetRowIndex(): Int = imgui_table_get_row_index()
    actual fun tableGetColumnName(columnN: Int): String = imgui_table_get_column_name(columnN)?.toKString() ?: ""
    actual fun tableGetSortSpecs(): Long = imgui_table_get_sort_specs()?.rawValue?.toLong() ?: 0L
    actual fun tableSetBgColor(target: Int, color: Int, columnN: Int) =
        imgui_table_set_bg_color(target, color.toUInt(), columnN)

    actual fun tabItemButton(label: String, flags: Int): Boolean = imgui_tab_item_button(label, flags)

    // ---- Style ----
    actual fun styleColorsDark() = imgui_style_colors_dark()
    actual fun styleColorsLight() = imgui_style_colors_light()
    actual fun styleColorsClassic() = imgui_style_colors_classic()
    actual fun showStyleSelector(label: String): Boolean = imgui_show_style_selector(label)
    actual fun showFontSelector(label: String) = imgui_show_font_selector(label)
    actual fun showStyleEditor() = imgui_show_style_editor()
    actual fun pushStyleColor(idx: Int, color: ImVec4) = memScoped {
        val c = alloc<imgui_vec4>()
        c.x = color.x
        c.y = color.y
        c.z = color.z
        c.w = color.w
        imgui_push_style_color_vec4(idx, c.readValue())
    }

    actual fun popStyleColor(count: Int) = imgui_pop_style_color(count)
    actual fun pushStyleVarFloat(idx: Int, value: Float) = imgui_push_style_var_float(idx, value)
    actual fun pushStyleVarVec2(idx: Int, value: ImVec2) = memScoped {
        val v = alloc<imgui_vec2>()
        v.x = value.x
        v.y = value.y
        imgui_push_style_var_vec2(idx, v.readValue())
    }

    actual fun popStyleVar(count: Int) = imgui_pop_style_var(count)
    actual fun pushFont(font: ImFont) = imgui_push_font((font as NativeImFont).ptr)
    actual fun popFont() = imgui_pop_font()
    actual fun pushItemWidth(width: Float) = imgui_push_item_width(width)
    actual fun popItemWidth() = imgui_pop_item_width()
    actual fun setNextItemWidth(width: Float) = imgui_set_next_item_width(width)

    // ---- SetNext* layout / item state ----
    actual fun setNextItemOpen(isOpen: Boolean, cond: Int) = imgui_set_next_item_open(isOpen, cond)
    actual fun setNextItemAllowOverlap() = imgui_set_next_item_allow_overlap()
    actual fun setNextItemSelectionUserData(selectionUserData: Long) = imgui_set_next_item_selection_user_data(selectionUserData)
    actual fun setNextItemShortcut(keyChord: Int, flags: Int) = imgui_set_next_item_shortcut(keyChord, flags)
    actual fun setNextWindowCollapsed(collapsed: Boolean, cond: Int) = imgui_set_next_window_collapsed(collapsed, cond)
    actual fun setNextWindowContentSize(size: ImVec2) = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = size.x
        s.y = size.y
        imgui_set_next_window_content_size(s.readValue())
    }

    actual fun setNextWindowFocus() = imgui_set_next_window_focus()
    actual fun setNextWindowScroll(scroll: ImVec2) = memScoped {
        val s = alloc<imgui_vec2>()
        s.x = scroll.x
        s.y = scroll.y
        imgui_set_next_window_scroll(s.readValue())
    }

    actual fun setNextWindowSizeConstraints(sizeMin: ImVec2, sizeMax: ImVec2, customCallback: (() -> Unit)?) = memScoped {
        val min = alloc<imgui_vec2>()
        min.x = sizeMin.x
        min.y = sizeMin.y
        val max = alloc<imgui_vec2>()
        max.x = sizeMax.x
        max.y = sizeMax.y
        imgui_set_next_window_size_constraints(min.readValue(), max.readValue())
    }

    actual fun setItemTooltip(text: String) = imgui_set_item_tooltip(text)
    actual fun setItemDefaultFocus() = imgui_set_item_default_focus()
    actual fun setTabItemClosed(tabOrDockedWindowLabel: String) = imgui_set_tab_item_closed(tabOrDockedWindowLabel)
}
