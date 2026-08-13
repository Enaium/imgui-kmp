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

#include "imgui.h"
#include "imgui_internal.h"

#include <cstring>
#include <string>

#include "imgui_c.h"

// The opaque C handles are only ever passed as pointers; the implementation
// casts them to the real imgui types (both sides are incomplete types, so
// no definition is required here).

// ImGui keeps the io.IniFilename pointer; store the string somewhere stable.
static thread_local std::string g_ini_filename;

extern "C" {

// =========================================================================
// Context / main frame
// =========================================================================

imgui_context* imgui_create_context(void) {
    return (imgui_context*)ImGui::CreateContext();
}

void imgui_destroy_context(imgui_context* ctx) {
    ImGui::DestroyContext((ImGuiContext*)ctx);
}

imgui_context* imgui_get_current_context(void) {
    return (imgui_context*)ImGui::GetCurrentContext();
}

imgui_io* imgui_get_io(void) {
    return (imgui_io*)&ImGui::GetIO();
}

imgui_style* imgui_get_style(void) {
    return (imgui_style*)&ImGui::GetStyle();
}

const char* imgui_get_version(void) {
    return ImGui::GetVersion();
}

void imgui_new_frame(void) {
    ImGui::NewFrame();
}

void imgui_render(void) {
    ImGui::Render();
}

imgui_draw_data* imgui_get_draw_data(void) {
    return (imgui_draw_data*)ImGui::GetDrawData();
}

void imgui_show_demo_window(bool* p_open) {
    ImGui::ShowDemoWindow(p_open);
}

// =========================================================================
// Windows
// =========================================================================

bool imgui_begin(const char* name, bool* p_open, int flags) {
    return ImGui::Begin(name, p_open, flags);
}

void imgui_end(void) {
    ImGui::End();
}

bool imgui_begin_child(const char* str_id, imgui_vec2 size, int child_flags, int window_flags) {
    return ImGui::BeginChild(str_id, ImVec2(size.x, size.y), child_flags, window_flags);
}

void imgui_end_child(void) {
    ImGui::EndChild();
}

void imgui_set_next_window_pos(imgui_vec2 pos, int cond, imgui_vec2 pivot) {
    ImGui::SetNextWindowPos(ImVec2(pos.x, pos.y), cond, ImVec2(pivot.x, pivot.y));
}

void imgui_set_next_window_size(imgui_vec2 size, int cond) {
    ImGui::SetNextWindowSize(ImVec2(size.x, size.y), cond);
}

void imgui_set_next_window_bg_alpha(float alpha) {
    ImGui::SetNextWindowBgAlpha(alpha);
}

void imgui_begin_disabled(bool disabled) {
    ImGui::BeginDisabled(disabled);
}

void imgui_end_disabled(void) {
    ImGui::EndDisabled();
}

bool imgui_begin_main_menu_bar(void) {
    return ImGui::BeginMainMenuBar();
}

void imgui_end_main_menu_bar(void) {
    ImGui::EndMainMenuBar();
}

bool imgui_begin_menu_bar(void) {
    return ImGui::BeginMenuBar();
}

void imgui_end_menu_bar(void) {
    ImGui::EndMenuBar();
}

bool imgui_begin_menu(const char* label, bool enabled) {
    return ImGui::BeginMenu(label, enabled);
}

void imgui_end_menu(void) {
    ImGui::EndMenu();
}

bool imgui_menu_item(const char* label, const char* shortcut, bool selected, bool enabled) {
    return ImGui::MenuItem(label, shortcut, selected, enabled);
}

bool imgui_begin_tab_bar(const char* str_id, int flags) {
    return ImGui::BeginTabBar(str_id, flags);
}

void imgui_end_tab_bar(void) {
    ImGui::EndTabBar();
}

bool imgui_begin_tab_item(const char* label, bool* p_open, int flags) {
    return ImGui::BeginTabItem(label, p_open, flags);
}

void imgui_end_tab_item(void) {
    ImGui::EndTabItem();
}

bool imgui_begin_tooltip(void) {
    return ImGui::BeginTooltip();
}

void imgui_end_tooltip(void) {
    ImGui::EndTooltip();
}

void imgui_set_tooltip(const char* text) {
    ImGui::SetTooltip("%s", text);
}

void imgui_open_popup(const char* str_id, int popup_flags) {
    ImGui::OpenPopup(str_id, popup_flags);
}

bool imgui_begin_popup(const char* str_id, int flags) {
    return ImGui::BeginPopup(str_id, flags);
}

bool imgui_begin_popup_modal(const char* name, bool* p_open, int flags) {
    return ImGui::BeginPopupModal(name, p_open, flags);
}

void imgui_end_popup(void) {
    ImGui::EndPopup();
}

void imgui_close_current_popup(void) {
    ImGui::CloseCurrentPopup();
}

bool imgui_begin_combo(const char* label, const char* preview_value, int flags) {
    return ImGui::BeginCombo(label, preview_value, flags);
}

void imgui_end_combo(void) {
    ImGui::EndCombo();
}

// =========================================================================
// Widgets
// =========================================================================

void imgui_text(const char* text) {
    ImGui::TextUnformatted(text);
}

void imgui_text_colored(imgui_vec4 color, const char* text) {
    ImGui::TextColored(ImVec4(color.x, color.y, color.z, color.w), "%s", text);
}

void imgui_text_disabled(const char* text) {
    ImGui::TextDisabled("%s", text);
}

void imgui_label_text(const char* label, const char* text) {
    ImGui::LabelText(label, "%s", text);
}

void imgui_bullet_text(const char* text) {
    ImGui::BulletText("%s", text);
}

void imgui_bullet(void) {
    ImGui::Bullet();
}

void imgui_separator(void) {
    ImGui::Separator();
}

void imgui_separator_text(const char* text) {
    ImGui::SeparatorText(text);
}

void imgui_same_line(float offset_from_start_x, float spacing) {
    ImGui::SameLine(offset_from_start_x, spacing);
}

void imgui_new_line(void) {
    ImGui::NewLine();
}

void imgui_spacing(void) {
    ImGui::Spacing();
}

void imgui_dummy(imgui_vec2 size) {
    ImGui::Dummy(ImVec2(size.x, size.y));
}

void imgui_indent(float indent_w) {
    ImGui::Indent(indent_w);
}

void imgui_unindent(float indent_w) {
    ImGui::Unindent(indent_w);
}

bool imgui_button(const char* label, imgui_vec2 size) {
    return ImGui::Button(label, ImVec2(size.x, size.y));
}

bool imgui_small_button(const char* label) {
    return ImGui::SmallButton(label);
}

bool imgui_checkbox(const char* label, bool* v) {
    return ImGui::Checkbox(label, v);
}

bool imgui_slider_float(const char* label, float* v, float v_min, float v_max, const char* format) {
    return ImGui::SliderFloat(label, v, v_min, v_max, format);
}

bool imgui_slider_int(const char* label, int* v, int v_min, int v_max, const char* format) {
    return ImGui::SliderInt(label, v, v_min, v_max, format);
}

bool imgui_input_text(const char* label, char* buf, int buf_size, int flags) {
    return ImGui::InputText(label, buf, (size_t)buf_size, flags);
}

bool imgui_combo(const char* label, int* current_item, const char** items, int items_count) {
    return ImGui::Combo(label, current_item, items, items_count);
}

bool imgui_selectable(const char* label, bool selected, int flags, imgui_vec2 size) {
    return ImGui::Selectable(label, selected, flags, ImVec2(size.x, size.y));
}

bool imgui_radio_button(const char* label, bool active) {
    return ImGui::RadioButton(label, active);
}

void imgui_progress_bar(float fraction, imgui_vec2 size, const char* overlay) {
    ImGui::ProgressBar(fraction, ImVec2(size.x, size.y), overlay);
}

bool imgui_collapsing_header(const char* label, int flags) {
    return ImGui::CollapsingHeader(label, flags);
}

bool imgui_tree_node(const char* label) {
    return ImGui::TreeNode(label);
}

void imgui_tree_pop(void) {
    ImGui::TreePop();
}

bool imgui_invisible_button(const char* str_id, imgui_vec2 size, int flags) {
    return ImGui::InvisibleButton(str_id, ImVec2(size.x, size.y), flags);
}

void imgui_begin_group(void) {
    ImGui::BeginGroup();
}

void imgui_end_group(void) {
    ImGui::EndGroup();
}

void imgui_set_cursor_pos(imgui_vec2 local_pos) {
    ImGui::SetCursorPos(ImVec2(local_pos.x, local_pos.y));
}

void imgui_push_id(const char* str_id) {
    ImGui::PushID(str_id);
}

void imgui_pop_id(void) {
    ImGui::PopID();
}

bool imgui_is_item_hovered(int flags) {
    return ImGui::IsItemHovered(flags);
}

bool imgui_is_item_active(void) {
    return ImGui::IsItemActive();
}

bool imgui_is_item_clicked(int mouse_button) {
    return ImGui::IsItemClicked(mouse_button);
}

bool imgui_is_window_hovered(int flags) {
    return ImGui::IsWindowHovered(flags);
}

bool imgui_is_window_focused(int flags) {
    return ImGui::IsWindowFocused(flags);
}

// =========================================================================
// Tables
// =========================================================================

bool imgui_begin_table(const char* str_id, int column, int flags, imgui_vec2 outer_size, float inner_width) {
    return ImGui::BeginTable(str_id, column, flags, ImVec2(outer_size.x, outer_size.y), inner_width);
}

void imgui_end_table(void) {
    ImGui::EndTable();
}

void imgui_table_next_row(int min_row_height, int flags) {
    ImGui::TableNextRow(flags, min_row_height);
}

bool imgui_table_next_column(void) {
    return ImGui::TableNextColumn();
}

bool imgui_table_set_column_index(int column_n) {
    return ImGui::TableSetColumnIndex(column_n);
}

void imgui_table_setup_column(const char* label, int flags, float init_width_or_weight, int user_id) {
    ImGui::TableSetupColumn(label, flags, init_width_or_weight, (ImGuiID)user_id);
}

void imgui_table_setup_scroll_freeze(int cols, int rows) {
    ImGui::TableSetupScrollFreeze(cols, rows);
}

void imgui_table_headers_row(void) {
    ImGui::TableHeadersRow();
}

// =========================================================================
// Style
// =========================================================================

void imgui_push_style_color_vec4(int idx, imgui_vec4 color) {
    ImGui::PushStyleColor((ImGuiCol)idx, ImVec4(color.x, color.y, color.z, color.w));
}

void imgui_push_style_color_u32(int idx, uint32_t color) {
    ImGui::PushStyleColor((ImGuiCol)idx, color);
}

void imgui_pop_style_color(int count) {
    ImGui::PopStyleColor(count);
}

void imgui_push_style_var_float(int idx, float val) {
    ImGui::PushStyleVar((ImGuiStyleVar)idx, val);
}

void imgui_push_style_var_vec2(int idx, imgui_vec2 val) {
    ImGui::PushStyleVar((ImGuiStyleVar)idx, ImVec2(val.x, val.y));
}

void imgui_pop_style_var(int count) {
    ImGui::PopStyleVar(count);
}

void imgui_push_font(imgui_font* font) {
    ImGui::PushFont((ImFont*)font);
}

void imgui_pop_font(void) {
    ImGui::PopFont();
}

void imgui_push_item_width(float item_width) {
    ImGui::PushItemWidth(item_width);
}

void imgui_pop_item_width(void) {
    ImGui::PopItemWidth();
}

void imgui_set_next_item_width(float item_width) {
    ImGui::SetNextItemWidth(item_width);
}

imgui_vec4 imgui_style_get_color(imgui_style* style, int idx) {
    const ImVec4& c = ((ImGuiStyle*)style)->Colors[idx];
    imgui_vec4 out;
    out.x = c.x;
    out.y = c.y;
    out.z = c.z;
    out.w = c.w;
    return out;
}

void imgui_style_set_color(imgui_style* style, int idx, imgui_vec4 color) {
    ((ImGuiStyle*)style)->Colors[idx] = ImVec4(color.x, color.y, color.z, color.w);
}

// =========================================================================
// IO
// =========================================================================

void imgui_io_set_display_size(imgui_io* io, float w, float h) {
    ((ImGuiIO*)io)->DisplaySize = ImVec2(w, h);
}

void imgui_io_set_display_framebuffer_scale(imgui_io* io, float sx, float sy) {
    ((ImGuiIO*)io)->DisplayFramebufferScale = ImVec2(sx, sy);
}

void imgui_io_set_delta_time(imgui_io* io, float dt) {
    ((ImGuiIO*)io)->DeltaTime = dt;
}

void imgui_io_set_config_flags(imgui_io* io, int flags) {
    ((ImGuiIO*)io)->ConfigFlags = flags;
}

void imgui_io_set_backend_flags(imgui_io* io, int flags) {
    ((ImGuiIO*)io)->BackendFlags = flags;
}

void imgui_io_set_ini_filename(imgui_io* io, const char* path) {
    if (path == nullptr) {
        g_ini_filename.clear();
        ((ImGuiIO*)io)->IniFilename = nullptr;
    } else {
        g_ini_filename = path;
        ((ImGuiIO*)io)->IniFilename = g_ini_filename.c_str();
    }
}

void imgui_io_set_font_global_scale(imgui_io* io, float scale) {
    ((ImGuiIO*)io)->FontGlobalScale = scale;
}

void imgui_io_add_mouse_pos_event(imgui_io* io, float x, float y) {
    ((ImGuiIO*)io)->AddMousePosEvent(x, y);
}

void imgui_io_add_mouse_button_event(imgui_io* io, int button, bool down) {
    ((ImGuiIO*)io)->AddMouseButtonEvent(button, down);
}

void imgui_io_add_mouse_wheel_event(imgui_io* io, float x, float y) {
    ((ImGuiIO*)io)->AddMouseWheelEvent(x, y);
}

void imgui_io_add_key_event(imgui_io* io, int key, bool down) {
    ((ImGuiIO*)io)->AddKeyEvent((ImGuiKey)key, down);
}

void imgui_io_add_input_character(imgui_io* io, uint32_t c) {
    ((ImGuiIO*)io)->AddInputCharacter(c);
}

bool imgui_io_want_capture_mouse(imgui_io* io) {
    return ((ImGuiIO*)io)->WantCaptureMouse;
}

bool imgui_io_want_capture_keyboard(imgui_io* io) {
    return ((ImGuiIO*)io)->WantCaptureKeyboard;
}

bool imgui_io_want_text_input(imgui_io* io) {
    return ((ImGuiIO*)io)->WantTextInput;
}

imgui_font_atlas* imgui_io_get_fonts(imgui_io* io) {
    return (imgui_font_atlas*)((ImGuiIO*)io)->Fonts;
}

// =========================================================================
// Fonts
// =========================================================================

imgui_font* imgui_font_atlas_add_font_from_file_ttf(imgui_font_atlas* atlas, const char* path, float size_px) {
    return (imgui_font*)((ImFontAtlas*)atlas)->AddFontFromFileTTF(path, size_px);
}

imgui_font* imgui_font_atlas_add_font_default(imgui_font_atlas* atlas) {
    return (imgui_font*)((ImFontAtlas*)atlas)->AddFontDefault();
}

bool imgui_font_atlas_build(imgui_font_atlas* atlas) {
    return ((ImFontAtlas*)atlas)->Build();
}

void imgui_font_atlas_get_tex_data_as_rgba32(imgui_font_atlas* atlas, const unsigned char** out_pixels, int* out_width, int* out_height, int* out_bpp) {
    unsigned char* pixels = nullptr;
    int width = 0, height = 0, bpp = 0;
    ((ImFontAtlas*)atlas)->GetTexDataAsRGBA32(&pixels, &width, &height, &bpp);
    if (out_pixels) {
        *out_pixels = pixels;
    }
    if (out_width) {
        *out_width = width;
    }
    if (out_height) {
        *out_height = height;
    }
    if (out_bpp) {
        *out_bpp = bpp;
    }
}

void imgui_font_atlas_set_tex_id(imgui_font_atlas* atlas, uint64_t tex_id) {
    ((ImFontAtlas*)atlas)->SetTexID((ImTextureID)tex_id);
}

// =========================================================================
// Draw data
// =========================================================================

imgui_vec2 imgui_draw_data_get_display_pos(imgui_draw_data* data) {
    const ImVec2& v = ((ImDrawData*)data)->DisplayPos;
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

imgui_vec2 imgui_draw_data_get_display_size(imgui_draw_data* data) {
    const ImVec2& v = ((ImDrawData*)data)->DisplaySize;
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

imgui_vec2 imgui_draw_data_get_framebuffer_scale(imgui_draw_data* data) {
    const ImVec2& v = ((ImDrawData*)data)->FramebufferScale;
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

int imgui_draw_data_get_cmd_lists_count(imgui_draw_data* data) {
    return ((ImDrawData*)data)->CmdLists.Size;
}

imgui_draw_list* imgui_draw_data_get_cmd_list(imgui_draw_data* data, int index) {
    return (imgui_draw_list*)((ImDrawData*)data)->CmdLists[index];
}

int imgui_draw_list_get_vtx_count(imgui_draw_list* list) {
    return ((ImDrawList*)list)->VtxBuffer.Size;
}

int imgui_draw_list_get_idx_count(imgui_draw_list* list) {
    return ((ImDrawList*)list)->IdxBuffer.Size;
}

const imgui_draw_vert* imgui_draw_list_get_vtx_data(imgui_draw_list* list) {
    return reinterpret_cast<const imgui_draw_vert*>(((ImDrawList*)list)->VtxBuffer.Data);
}

const uint16_t* imgui_draw_list_get_idx_data(imgui_draw_list* list) {
    return reinterpret_cast<const uint16_t*>(((ImDrawList*)list)->IdxBuffer.Data);
}

int imgui_draw_list_get_cmd_count(imgui_draw_list* list) {
    return ((ImDrawList*)list)->CmdBuffer.Size;
}

imgui_draw_cmd* imgui_draw_list_get_cmd(imgui_draw_list* list, int index) {
    return (imgui_draw_cmd*)&((ImDrawList*)list)->CmdBuffer[index];
}

imgui_vec4 imgui_draw_cmd_get_clip_rect(imgui_draw_cmd* cmd) {
    const ImVec4& r = ((ImDrawCmd*)cmd)->ClipRect;
    imgui_vec4 out;
    out.x = r.x;
    out.y = r.y;
    out.z = r.z;
    out.w = r.w;
    return out;
}

uint64_t imgui_draw_cmd_get_tex_id(imgui_draw_cmd* cmd) {
    return (uint64_t)((ImDrawCmd*)cmd)->GetTexID();
}

uint32_t imgui_draw_cmd_get_vtx_offset(imgui_draw_cmd* cmd) {
    return ((ImDrawCmd*)cmd)->VtxOffset;
}

uint32_t imgui_draw_cmd_get_idx_offset(imgui_draw_cmd* cmd) {
    return ((ImDrawCmd*)cmd)->IdxOffset;
}

uint32_t imgui_draw_cmd_get_elem_count(imgui_draw_cmd* cmd) {
    return ((ImDrawCmd*)cmd)->ElemCount;
}

bool imgui_draw_cmd_has_user_callback(imgui_draw_cmd* cmd) {
    return ((ImDrawCmd*)cmd)->UserCallback != nullptr;
}

} // extern "C"
