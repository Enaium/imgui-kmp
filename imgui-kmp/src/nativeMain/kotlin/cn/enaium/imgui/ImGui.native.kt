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
        get() = error("displaySize is write-only; set it before NewFrame")
        set(value) = imgui_io_set_display_size(ptr, value.x, value.y)

    override var displayFramebufferScale: ImVec2
        get() = error("displayFramebufferScale is write-only; set it before NewFrame")
        set(value) = imgui_io_set_display_framebuffer_scale(ptr, value.x, value.y)

    override var deltaTime: Float
        get() = error("deltaTime is write-only; set it before NewFrame")
        set(value) = imgui_io_set_delta_time(ptr, value)

    override var configFlags: Int
        get() = error("configFlags is write-only; set it before NewFrame")
        set(value) = imgui_io_set_config_flags(ptr, value)

    override var backendFlags: Int
        get() = error("backendFlags is write-only; set it before NewFrame")
        set(value) = imgui_io_set_backend_flags(ptr, value)

    override var iniFilename: String?
        get() = error("iniFilename is write-only")
        set(value) = imgui_io_set_ini_filename(ptr, value)

    override var fontGlobalScale: Float
        get() = error("fontGlobalScale is write-only")
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

    actual fun newFrame() = imgui_new_frame()
    actual fun render() = imgui_render()
    actual fun getDrawData(): ImDrawData = NativeImDrawData(imgui_get_draw_data())
    actual fun getIO(): ImGuiIO = NativeImGuiIO(imgui_get_io())
    actual fun getStyle(): ImGuiStyle = NativeImGuiStyle(imgui_get_style())
    actual fun getVersion(): String = imgui_get_version()?.toKString() ?: ""
    actual fun showDemoWindow(pOpen: BooleanArray?) = withBoolVar(pOpen) {
        imgui_show_demo_window(it)
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
    actual fun beginCombo(label: String, previewValue: String, flags: Int): Boolean = imgui_begin_combo(label, previewValue, flags)
    actual fun endCombo() = imgui_end_combo()

    // ---- Widgets ----
    actual fun text(text: String) = imgui_text(text)
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
    actual fun checkbox(label: String, v: BooleanArray): Boolean = withBoolVar(v) {
        imgui_checkbox(label, it)
    }

    actual fun sliderFloat(label: String, v: FloatArray, vMin: Float, vMax: Float, format: String): Boolean = withFloatVar(v) {
        imgui_slider_float(label, it, vMin, vMax, format)
    }

    actual fun sliderInt(label: String, v: IntArray, vMin: Int, vMax: Int, format: String): Boolean = withIntVar(v) {
        imgui_slider_int(label, it, vMin, vMax, format)
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
    actual fun isWindowHovered(flags: Int): Boolean = imgui_is_window_hovered(flags)
    actual fun isWindowFocused(flags: Int): Boolean = imgui_is_window_focused(flags)

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

    // ---- Style ----
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
}
