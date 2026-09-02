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

#include <cstdarg>
#include <cstdio>
#include <cstring>
#include <string>

#include "imgui_c.h"

// glibc 2.38+ (Ubuntu 24.04) redirects the stdio formatted-I/O functions to
// __isoc23_* variants in its headers (via __REDIRECT asm labels, so #undef
// cannot undo them). Kotlin/Native's bundled Linux libc predates those
// symbols, so the final link fails with "undefined symbol: __isoc23_*".
//
// Every __isoc23_* name that the redirected headers make the code call is
// implemented here. The implementations route through helper names whose
// asm-labels point at the classic glibc exports, which exist in every glibc
// version - this avoids both the missing symbols and the infinite recursion
// that calling the redirected names would cause.
#if defined(__GLIBC__) && defined(__GNUC__)

#include <stdio.h>

extern "C" int imgui_kmp_vsscanf(const char*, const char*, va_list) __asm__("vsscanf");
extern "C" int imgui_kmp_vscanf(const char*, va_list) __asm__("vscanf");
extern "C" int imgui_kmp_vfscanf(FILE*, const char*, va_list) __asm__("vfscanf");
extern "C" int imgui_kmp_vsnprintf(char*, size_t, const char*, va_list) __asm__("vsnprintf");
extern "C" int imgui_kmp_vprintf(const char*, va_list) __asm__("vprintf");
extern "C" int imgui_kmp_vfprintf(FILE*, const char*, va_list) __asm__("vfprintf");

extern "C" __attribute__((weak)) int __isoc23_sscanf(const char* str, const char* fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    int result = imgui_kmp_vsscanf(str, fmt, ap);
    va_end(ap);
    return result;
}

extern "C" __attribute__((weak)) int __isoc23_vsscanf(const char* str, const char* fmt, va_list ap) {
    return imgui_kmp_vsscanf(str, fmt, ap);
}

extern "C" __attribute__((weak)) int __isoc23_scanf(const char* fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    int result = imgui_kmp_vscanf(fmt, ap);
    va_end(ap);
    return result;
}

extern "C" __attribute__((weak)) int __isoc23_vscanf(const char* fmt, va_list ap) {
    return imgui_kmp_vscanf(fmt, ap);
}

extern "C" __attribute__((weak)) int __isoc23_fscanf(FILE* stream, const char* fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    int result = imgui_kmp_vfscanf(stream, fmt, ap);
    va_end(ap);
    return result;
}

extern "C" __attribute__((weak)) int __isoc23_vfscanf(FILE* stream, const char* fmt, va_list ap) {
    return imgui_kmp_vfscanf(stream, fmt, ap);
}

extern "C" __attribute__((weak)) int __isoc23_snprintf(char* str, size_t size, const char* fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    int result = imgui_kmp_vsnprintf(str, size, fmt, ap);
    va_end(ap);
    return result;
}

extern "C" __attribute__((weak)) int __isoc23_vsnprintf(char* str, size_t size, const char* fmt, va_list ap) {
    return imgui_kmp_vsnprintf(str, size, fmt, ap);
}

extern "C" __attribute__((weak)) int __isoc23_printf(const char* fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    int result = imgui_kmp_vprintf(fmt, ap);
    va_end(ap);
    return result;
}

extern "C" __attribute__((weak)) int __isoc23_vprintf(const char* fmt, va_list ap) {
    return imgui_kmp_vprintf(fmt, ap);
}

extern "C" __attribute__((weak)) int __isoc23_fprintf(FILE* stream, const char* fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    int result = imgui_kmp_vfprintf(stream, fmt, ap);
    va_end(ap);
    return result;
}

// glibc 2.32+ exports `__libc_single_threaded` and GCC 9+ libstdc++ exports
// `std::__throw_bad_array_new_length`. Code compiled by a modern host
// toolchain (Ubuntu 24.04 g++ 12, glibc 2.39) references both, but
// Kotlin/Native's bundled Linux toolchain (glibc 2.19, GCC 8.3) provides
// neither, so linking linuxX64 binaries fails. Provide weak definitions:
// the real glibc/libstdc++ strong symbols win when present, and the shims
// keep old toolchains linking.
extern "C" __attribute__((weak)) int __libc_single_threaded = 1;

namespace std {
__attribute__((weak)) void __throw_bad_array_new_length() {
    __builtin_abort();
}
}  // namespace std

#endif // __GLIBC__

// casts them to the real imgui types (both sides are incomplete types, so
// no definition is required here).

// ImGui keeps the io.IniFilename pointer; store the string somewhere stable.
// C++'s std::string would drag the C++ runtime into every consumer of
// libimgui.a, so use a fixed-size char buffer instead. Filenames longer
// than this are silently truncated, which is fine for an ini path.
static thread_local char g_ini_filename[4096];
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

void imgui_set_current_context(imgui_context* ctx) {
    ImGui::SetCurrentContext((ImGuiContext*)ctx);
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

void imgui_show_about_window(bool* p_open) {
    ImGui::ShowAboutWindow(p_open);
}

void imgui_show_metrics_window(bool* p_open) {
    ImGui::ShowMetricsWindow(p_open);
}

void imgui_show_debug_log_window(bool* p_open) {
    ImGui::ShowDebugLogWindow(p_open);
}

void imgui_show_user_guide(void) {
    ImGui::ShowUserGuide();
}

void imgui_show_id_stack_tool_window(bool* p_open) {
    ImGui::ShowIDStackToolWindow(p_open);
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

// =========================================================================
// Docking (requires io.ConfigFlags |= ImGuiConfigFlags_DockingEnable)
// =========================================================================

int imgui_dock_space(int id, float size_x, float size_y, int flags) {
    return (int)ImGui::DockSpace((ImGuiID)id, ImVec2(size_x, size_y), (ImGuiDockNodeFlags)flags);
}

void imgui_set_next_window_dock_id(int dock_id, int cond) {
    ImGui::SetNextWindowDockID((ImGuiID)dock_id, (ImGuiCond)cond);
}

int imgui_dock_builder_add_node(int node_id, int flags) {
    return (int)ImGui::DockBuilderAddNode((ImGuiID)node_id, (ImGuiDockNodeFlags)flags);
}

void imgui_dock_builder_remove_node(int node_id) {
    ImGui::DockBuilderRemoveNode((ImGuiID)node_id);
}

int imgui_dock_builder_split_node(int node_id, int split_dir, float size_ratio_for_node_at_dir, int* out_id_at_dir, int* out_id_at_opposite_dir) {
    ImGuiID id_at_dir = 0;
    ImGuiID id_at_opposite_dir = 0;
    ImGui::DockBuilderSplitNode((ImGuiID)node_id, (ImGuiDir)split_dir, size_ratio_for_node_at_dir, &id_at_dir, &id_at_opposite_dir);
    if (out_id_at_dir != nullptr) {
        *out_id_at_dir = (int)id_at_dir;
    }
    if (out_id_at_opposite_dir != nullptr) {
        *out_id_at_opposite_dir = (int)id_at_opposite_dir;
    }
    return 1;
}

void imgui_dock_builder_dock_window(const char* window_name, int node_id) {
    ImGui::DockBuilderDockWindow(window_name, (ImGuiID)node_id);
}

void imgui_dock_builder_finish(int node_id) {
    ImGui::DockBuilderFinish((ImGuiID)node_id);
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

void imgui_set_window_size(imgui_vec2 size, int cond) {
    ImGui::SetWindowSize(ImVec2(size.x, size.y), (ImGuiCond)cond);
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

bool imgui_begin_item_tooltip(void) {
    return ImGui::BeginItemTooltip();
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

bool imgui_begin_popup_context_item(const char* str_id, int popup_flags) {
    return ImGui::BeginPopupContextItem(str_id, (ImGuiPopupFlags)popup_flags);
}

bool imgui_begin_popup_context_window(const char* str_id, int popup_flags) {
    return ImGui::BeginPopupContextWindow(str_id, (ImGuiPopupFlags)popup_flags);
}

bool imgui_open_popup_on_item_click(const char* str_id, int popup_flags) {
    return ImGui::OpenPopupOnItemClick(str_id, (ImGuiPopupFlags)popup_flags);
}

bool imgui_begin_combo(const char* label, const char* preview_value, int flags) {
    return ImGui::BeginCombo(label, preview_value, flags);
}

void imgui_end_combo(void) {
    ImGui::EndCombo();
}

// =========================================================================
// Drag and drop
// =========================================================================

bool imgui_begin_drag_drop_source(int flags) {
    return ImGui::BeginDragDropSource((ImGuiDragDropFlags)flags);
}

bool imgui_set_drag_drop_payload(const char* type, const void* data, int size, int cond) {
    return ImGui::SetDragDropPayload(type, data, (size_t)size, (ImGuiCond)cond);
}

void imgui_end_drag_drop_source(void) {
    ImGui::EndDragDropSource();
}

bool imgui_begin_drag_drop_target(void) {
    return ImGui::BeginDragDropTarget();
}

const void* imgui_accept_drag_drop_payload(const char* type, int flags, int* out_size) {
    const ImGuiPayload* payload = ImGui::AcceptDragDropPayload(type, (ImGuiDragDropFlags)flags);
    if (payload == nullptr || payload->Data == nullptr || payload->DataSize == 0) {
        if (out_size != nullptr) {
            *out_size = 0;
        }
        return nullptr;
    }
    if (out_size != nullptr) {
        *out_size = (int)payload->DataSize;
    }
    return payload->Data;
}

void imgui_end_drag_drop_target(void) {
    ImGui::EndDragDropTarget();
}

const char* imgui_get_drag_drop_payload_type(void) {
    const ImGuiPayload* payload = ImGui::GetDragDropPayload();
    return payload == nullptr ? nullptr : payload->DataType;
}

// =========================================================================
// Images
// =========================================================================

void imgui_image(uint64_t tex_id, imgui_vec2 size, imgui_vec2 uv0, imgui_vec2 uv1, imgui_vec4 tint_color, imgui_vec4 border_color) {
    ImTextureRef tex_ref((ImTextureID)tex_id);
    ImGui::Image(tex_ref, ImVec2(size.x, size.y), ImVec2(uv0.x, uv0.y), ImVec2(uv1.x, uv1.y), ImVec4(tint_color.x, tint_color.y, tint_color.z, tint_color.w), ImVec4(border_color.x, border_color.y, border_color.z, border_color.w));
}

bool imgui_image_button(uint64_t tex_id, imgui_vec2 size, imgui_vec2 uv0, imgui_vec2 uv1, int frame_padding, imgui_vec4 bg_color, imgui_vec4 tint_color) {
    char id[64];
    snprintf(id, sizeof(id), "##imgui_kmp_image_btn_%llu", (unsigned long long)tex_id);
    ImTextureRef tex_ref((ImTextureID)tex_id);
    if (frame_padding >= 0) {
        ImGui::PushStyleVar(ImGuiStyleVar_FramePadding, ImVec2((float)frame_padding, (float)frame_padding));
    }
    bool result = ImGui::ImageButton(id, tex_ref, ImVec2(size.x, size.y), ImVec2(uv0.x, uv0.y), ImVec2(uv1.x, uv1.y), ImVec4(bg_color.x, bg_color.y, bg_color.z, bg_color.w), ImVec4(tint_color.x, tint_color.y, tint_color.z, tint_color.w));
    if (frame_padding >= 0) {
        ImGui::PopStyleVar();
    }
    return result;
}

void imgui_image_with_bg(uint64_t tex_id, imgui_vec2 size, imgui_vec4 bg_color, imgui_vec2 uv0, imgui_vec2 uv1) {
    ImTextureRef tex_ref((ImTextureID)tex_id);
    ImGui::ImageWithBg(tex_ref, ImVec2(size.x, size.y), ImVec2(uv0.x, uv0.y), ImVec2(uv1.x, uv1.y), ImVec4(bg_color.x, bg_color.y, bg_color.z, bg_color.w));
}

// =========================================================================
// List boxes
// =========================================================================

bool imgui_begin_list_box(const char* label, imgui_vec2 size) {
    return ImGui::BeginListBox(label, ImVec2(size.x, size.y));
}

void imgui_end_list_box(void) {
    ImGui::EndListBox();
}

bool imgui_list_box(const char* label, int* current_item, const char** items, int items_count) {
    return ImGui::ListBox(label, current_item, items, items_count);
}

// =========================================================================
// Multi select
// =========================================================================

void* imgui_begin_multi_select(int flags, int selection_size, int items_count) {
    return (void*)ImGui::BeginMultiSelect((ImGuiMultiSelectFlags)flags, selection_size, items_count);
}

void* imgui_end_multi_select(void) {
    return (void*)ImGui::EndMultiSelect();
}

// =========================================================================
// Logging
// =========================================================================

void imgui_log_to_clipboard(int auto_open_depth) {
    ImGui::LogToClipboard(auto_open_depth);
}

void imgui_log_to_file(int auto_open_depth, const char* filename) {
    ImGui::LogToFile(auto_open_depth, filename);
}

void imgui_log_to_tty(int auto_open_depth) {
    ImGui::LogToTTY(auto_open_depth);
}

void imgui_log_finish(void) {
    ImGui::LogFinish();
}

void imgui_log_text(const char* text) {
    ImGui::LogText("%s", text);
}

// =========================================================================
// .ini settings persistence
// =========================================================================

void imgui_save_ini_settings_to_disk(const char* ini_filename) {
    ImGui::SaveIniSettingsToDisk(ini_filename);
}

void imgui_load_ini_settings_from_disk(const char* ini_filename) {
    ImGui::LoadIniSettingsFromDisk(ini_filename);
}

const char* imgui_save_ini_settings_to_memory(void) {
    return ImGui::SaveIniSettingsToMemory();
}

void imgui_load_ini_settings_from_memory(const char* ini_data) {
    ImGui::LoadIniSettingsFromMemory(ini_data);
}

// =========================================================================
// Scissor rect / text wrapping
// =========================================================================

void imgui_push_clip_rect(imgui_vec2 clip_rect_min, imgui_vec2 clip_rect_max, bool intersect_with_current_clip_rect) {
    ImGui::PushClipRect(ImVec2(clip_rect_min.x, clip_rect_min.y), ImVec2(clip_rect_max.x, clip_rect_max.y), intersect_with_current_clip_rect);
}

void imgui_pop_clip_rect(void) {
    ImGui::PopClipRect();
}

void imgui_push_text_wrap_pos(float wrap_local_pos_x) {
    ImGui::PushTextWrapPos(wrap_local_pos_x);
}

void imgui_pop_text_wrap_pos(void) {
    ImGui::PopTextWrapPos();
}

// =========================================================================
// Widgets
// =========================================================================

void imgui_text(const char* text) {
    ImGui::TextUnformatted(text);
}

void imgui_text_wrapped(const char* text) {
    ImGui::TextWrapped("%s", text);
}

void imgui_text_unformatted(const char* text) {
    ImGui::TextUnformatted(text);
}

bool imgui_text_link(const char* text) {
    return ImGui::TextLink(text);
}

bool imgui_text_link_open_url(const char* label, const char* url) {
    return ImGui::TextLinkOpenURL(label, url);
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

void imgui_align_text_to_frame_padding(void) {
    ImGui::AlignTextToFramePadding();
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

bool imgui_arrow_button(const char* str_id, int dir) {
    return ImGui::ArrowButton(str_id, (ImGuiDir)dir);
}

bool imgui_checkbox(const char* label, bool* v) {
    return ImGui::Checkbox(label, v);
}

bool imgui_checkbox_flags(const char* label, int* flags, int flags_value) {
    return ImGui::CheckboxFlags(label, flags, flags_value);
}

void imgui_push_item_flag(int flag, bool enabled) {
    ImGui::PushItemFlag((ImGuiItemFlags)flag, enabled);
}

void imgui_pop_item_flag(void) {
    ImGui::PopItemFlag();
}

bool imgui_shortcut(int key_chord, int flags) {
    return ImGui::Shortcut(key_chord, (ImGuiInputFlags)flags);
}

bool imgui_slider_float(const char* label, float* v, float v_min, float v_max, const char* format) {
    return ImGui::SliderFloat(label, v, v_min, v_max, format);
}

bool imgui_slider_int(const char* label, int* v, int v_min, int v_max, const char* format) {
    return ImGui::SliderInt(label, v, v_min, v_max, format);
}

bool imgui_drag_float(const char* label, float* v, float v_speed, float v_min, float v_max, const char* format, int flags) {
    return ImGui::DragFloat(label, v, v_speed, v_min, v_max, format, flags);
}

bool imgui_drag_float2(const char* label, float* v, float v_speed, float v_min, float v_max, const char* format, int flags) {
    return ImGui::DragFloat2(label, v, v_speed, v_min, v_max, format, flags);
}

bool imgui_drag_float3(const char* label, float* v, float v_speed, float v_min, float v_max, const char* format, int flags) {
    return ImGui::DragFloat3(label, v, v_speed, v_min, v_max, format, flags);
}

bool imgui_drag_float4(const char* label, float* v, float v_speed, float v_min, float v_max, const char* format, int flags) {
    return ImGui::DragFloat4(label, v, v_speed, v_min, v_max, format, flags);
}

bool imgui_drag_float_range2(const char* label, float* v_current_min, float* v_current_max, float v_speed, float v_min, float v_max, const char* format, const char* format_max, int flags) {
    return ImGui::DragFloatRange2(label, v_current_min, v_current_max, v_speed, v_min, v_max, format, format_max, flags);
}

bool imgui_drag_int(const char* label, int* v, float v_speed, int v_min, int v_max, const char* format, int flags) {
    return ImGui::DragInt(label, v, v_speed, v_min, v_max, format, flags);
}

bool imgui_drag_int2(const char* label, int* v, float v_speed, int v_min, int v_max, const char* format, int flags) {
    return ImGui::DragInt2(label, v, v_speed, v_min, v_max, format, flags);
}

bool imgui_drag_int3(const char* label, int* v, float v_speed, int v_min, int v_max, const char* format, int flags) {
    return ImGui::DragInt3(label, v, v_speed, v_min, v_max, format, flags);
}

bool imgui_drag_int4(const char* label, int* v, float v_speed, int v_min, int v_max, const char* format, int flags) {
    return ImGui::DragInt4(label, v, v_speed, v_min, v_max, format, flags);
}

bool imgui_drag_int_range2(const char* label, int* v_current_min, int* v_current_max, float v_speed, int v_min, int v_max, const char* format, const char* format_max, int flags) {
    return ImGui::DragIntRange2(label, v_current_min, v_current_max, v_speed, v_min, v_max, format, format_max, flags);
}

bool imgui_slider_float2(const char* label, float* v, float v_min, float v_max, const char* format, int flags) {
    return ImGui::SliderFloat2(label, v, v_min, v_max, format, flags);
}

bool imgui_slider_float3(const char* label, float* v, float v_min, float v_max, const char* format, int flags) {
    return ImGui::SliderFloat3(label, v, v_min, v_max, format, flags);
}

bool imgui_slider_float4(const char* label, float* v, float v_min, float v_max, const char* format, int flags) {
    return ImGui::SliderFloat4(label, v, v_min, v_max, format, flags);
}

bool imgui_slider_int2(const char* label, int* v, int v_min, int v_max, const char* format, int flags) {
    return ImGui::SliderInt2(label, v, v_min, v_max, format, flags);
}

bool imgui_slider_int3(const char* label, int* v, int v_min, int v_max, const char* format, int flags) {
    return ImGui::SliderInt3(label, v, v_min, v_max, format, flags);
}

bool imgui_slider_int4(const char* label, int* v, int v_min, int v_max, const char* format, int flags) {
    return ImGui::SliderInt4(label, v, v_min, v_max, format, flags);
}

bool imgui_slider_angle(const char* label, float* v_rad, float v_degrees_min, float v_degrees_max, const char* format, int flags) {
    return ImGui::SliderAngle(label, v_rad, v_degrees_min, v_degrees_max, format, flags);
}

bool imgui_vslider_float(const char* label, imgui_vec2 size, float* v, float v_min, float v_max, const char* format, int flags) {
    return ImGui::VSliderFloat(label, ImVec2(size.x, size.y), v, v_min, v_max, format, flags);
}

bool imgui_vslider_int(const char* label, imgui_vec2 size, int* v, int v_min, int v_max, const char* format, int flags) {
    return ImGui::VSliderInt(label, ImVec2(size.x, size.y), v, v_min, v_max, format, flags);
}

bool imgui_slider_scalar(const char* label, int data_type, int64_t* v, int64_t* v_min, int64_t* v_max, const char* format) {
    switch (data_type) {
        case ImGuiDataType_S32: {
            int vv = (int)*v;
            int mn = (int)*v_min;
            int mx = (int)*v_max;
            bool r = ImGui::SliderScalar(label, ImGuiDataType_S32, &vv, &mn, &mx, format);
            *v = vv;
            return r;
        }
        case ImGuiDataType_U32: {
            unsigned int vv = (unsigned int)*v;
            unsigned int mn = (unsigned int)*v_min;
            unsigned int mx = (unsigned int)*v_max;
            bool r = ImGui::SliderScalar(label, ImGuiDataType_U32, &vv, &mn, &mx, format);
            *v = vv;
            return r;
        }
        case ImGuiDataType_Float: {
            float vv = (float)*v;
            float mn = (float)*v_min;
            float mx = (float)*v_max;
            bool r = ImGui::SliderScalar(label, ImGuiDataType_Float, &vv, &mn, &mx, format);
            *v = (int64_t)vv;
            return r;
        }
        case ImGuiDataType_Double: {
            double vv = (double)*v;
            double mn = (double)*v_min;
            double mx = (double)*v_max;
            bool r = ImGui::SliderScalar(label, ImGuiDataType_Double, &vv, &mn, &mx, format);
            *v = (int64_t)vv;
            return r;
        }
        default:
            return ImGui::SliderScalar(label, (ImGuiDataType)data_type, v, v_min, v_max, format);
    }
}

bool imgui_drag_scalar(const char* label, int data_type, int64_t* v, float v_speed, int64_t* v_min, int64_t* v_max, const char* format) {
    switch (data_type) {
        case ImGuiDataType_S32: {
            int vv = (int)*v;
            int mn = (int)*v_min;
            int mx = (int)*v_max;
            bool r = ImGui::DragScalar(label, ImGuiDataType_S32, &vv, v_speed, &mn, &mx, format);
            *v = vv;
            return r;
        }
        case ImGuiDataType_U32: {
            unsigned int vv = (unsigned int)*v;
            unsigned int mn = (unsigned int)*v_min;
            unsigned int mx = (unsigned int)*v_max;
            bool r = ImGui::DragScalar(label, ImGuiDataType_U32, &vv, v_speed, &mn, &mx, format);
            *v = vv;
            return r;
        }
        case ImGuiDataType_Float: {
            float vv = (float)*v;
            float mn = (float)*v_min;
            float mx = (float)*v_max;
            bool r = ImGui::DragScalar(label, ImGuiDataType_Float, &vv, v_speed, &mn, &mx, format);
            *v = (int64_t)vv;
            return r;
        }
        case ImGuiDataType_Double: {
            double vv = (double)*v;
            double mn = (double)*v_min;
            double mx = (double)*v_max;
            bool r = ImGui::DragScalar(label, ImGuiDataType_Double, &vv, v_speed, &mn, &mx, format);
            *v = (int64_t)vv;
            return r;
        }
        default:
            return ImGui::DragScalar(label, (ImGuiDataType)data_type, v, v_speed, v_min, v_max, format);
    }
}

bool imgui_input_float(const char* label, float* v, float step, float step_fast, const char* format, int flags) {
    return ImGui::InputFloat(label, v, step, step_fast, format, flags);
}

bool imgui_input_float2(const char* label, float* v, const char* format, int flags) {
    return ImGui::InputFloat2(label, v, format, flags);
}

bool imgui_input_float3(const char* label, float* v, const char* format, int flags) {
    return ImGui::InputFloat3(label, v, format, flags);
}

bool imgui_input_float4(const char* label, float* v, const char* format, int flags) {
    return ImGui::InputFloat4(label, v, format, flags);
}

bool imgui_input_int(const char* label, int* v, int step, int step_fast, int flags) {
    return ImGui::InputInt(label, v, step, step_fast, flags);
}

bool imgui_input_int2(const char* label, int* v, int flags) {
    return ImGui::InputInt2(label, v, flags);
}

bool imgui_input_int3(const char* label, int* v, int flags) {
    return ImGui::InputInt3(label, v, flags);
}

bool imgui_input_int4(const char* label, int* v, int flags) {
    return ImGui::InputInt4(label, v, flags);
}

bool imgui_input_double(const char* label, double* v, double step, double step_fast, const char* format, int flags) {
    return ImGui::InputDouble(label, v, step, step_fast, format, flags);
}

bool imgui_color_edit3(const char* label, float* col, int flags) {
    return ImGui::ColorEdit3(label, col, flags);
}

bool imgui_color_edit4(const char* label, float* col, int flags) {
    return ImGui::ColorEdit4(label, col, flags);
}

bool imgui_color_picker3(const char* label, float* col, int flags) {
    return ImGui::ColorPicker3(label, col, flags);
}

bool imgui_color_picker4(const char* label, float* col, int flags) {
    return ImGui::ColorPicker4(label, col, flags);
}

bool imgui_color_button(const char* desc_id, imgui_vec4 col, int flags, imgui_vec2 size) {
    return ImGui::ColorButton(desc_id, ImVec4(col.x, col.y, col.z, col.w), flags, ImVec2(size.x, size.y));
}

void imgui_set_color_edit_options(int flags) {
    ImGui::SetColorEditOptions((ImGuiColorEditFlags)flags);
}

uint32_t imgui_color_convert_float4_to_u32(imgui_vec4 in) {
    return ImGui::ColorConvertFloat4ToU32(ImVec4(in.x, in.y, in.z, in.w));
}

imgui_vec4 imgui_color_convert_u32_to_float4(uint32_t in) {
    const ImVec4& c = ImGui::ColorConvertU32ToFloat4(in);
    imgui_vec4 out;
    out.x = c.x;
    out.y = c.y;
    out.z = c.z;
    out.w = c.w;
    return out;
}

void imgui_color_convert_rgb_to_hsv(float r, float g, float b, float* out_h, float* out_s, float* out_v) {
    ImGui::ColorConvertRGBtoHSV(r, g, b, *out_h, *out_s, *out_v);
}

void imgui_color_convert_hsv_to_rgb(float h, float s, float v, float* out_r, float* out_g, float* out_b) {
    ImGui::ColorConvertHSVtoRGB(h, s, v, *out_r, *out_g, *out_b);
}

bool imgui_input_text(const char* label, char* buf, int buf_size, int flags) {
    return ImGui::InputText(label, buf, (size_t)buf_size, flags);
}

bool imgui_input_text_multiline(const char* label, char* buf, int buf_size, imgui_vec2 size, int flags) {
    return ImGui::InputTextMultiline(label, buf, (size_t)buf_size, ImVec2(size.x, size.y), flags);
}

bool imgui_input_text_with_hint(const char* label, const char* hint, char* buf, int buf_size, int flags) {
    return ImGui::InputTextWithHint(label, hint, buf, (size_t)buf_size, flags);
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

bool imgui_tree_node_ex(const char* label, int flags) {
    return ImGui::TreeNodeEx(label, flags);
}

bool imgui_tree_node_get_open(const char* str_id) {
    return ImGui::TreeNodeGetOpen(ImGui::GetID(str_id));
}

void imgui_tree_push(const char* str_id) {
    ImGui::TreePush(str_id != nullptr ? str_id : "");
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
// State queries
// =========================================================================

bool imgui_is_item_focused(void) {
    return ImGui::IsItemFocused();
}

bool imgui_is_item_visible(void) {
    return ImGui::IsItemVisible();
}

bool imgui_is_item_edited(void) {
    return ImGui::IsItemEdited();
}

bool imgui_is_item_activated(void) {
    return ImGui::IsItemActivated();
}

bool imgui_is_item_deactivated(void) {
    return ImGui::IsItemDeactivated();
}

bool imgui_is_item_deactivated_after_edit(void) {
    return ImGui::IsItemDeactivatedAfterEdit();
}

bool imgui_is_item_toggled_open(void) {
    return ImGui::IsItemToggledOpen();
}

bool imgui_is_item_toggled_selection(void) {
    return ImGui::IsItemToggledSelection();
}

bool imgui_is_any_item_hovered(void) {
    return ImGui::IsAnyItemHovered();
}

bool imgui_is_any_item_active(void) {
    return ImGui::IsAnyItemActive();
}

bool imgui_is_any_item_focused(void) {
    return ImGui::IsAnyItemFocused();
}

int imgui_get_item_id(void) {
    return (int)ImGui::GetItemID();
}

int imgui_get_item_flags(void) {
    return (int)ImGui::GetItemFlags();
}

imgui_vec2 imgui_get_item_rect_min(void) {
    const ImVec2& v = ImGui::GetItemRectMin();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

imgui_vec2 imgui_get_item_rect_max(void) {
    const ImVec2& v = ImGui::GetItemRectMax();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

imgui_vec2 imgui_get_item_rect_size(void) {
    const ImVec2& v = ImGui::GetItemRectSize();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

bool imgui_is_window_appearing(void) {
    return ImGui::IsWindowAppearing();
}

bool imgui_is_window_collapsed(void) {
    return ImGui::IsWindowCollapsed();
}

bool imgui_is_rect_visible(imgui_vec2 size) {
    return ImGui::IsRectVisible(ImVec2(size.x, size.y));
}

bool imgui_is_popup_open(const char* str_id, int flags) {
    return ImGui::IsPopupOpen(str_id, flags);
}

imgui_vec2 imgui_get_window_pos(void) {
    const ImVec2& v = ImGui::GetWindowPos();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

imgui_vec2 imgui_get_window_size(void) {
    const ImVec2& v = ImGui::GetWindowSize();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

float imgui_get_window_width(void) {
    return ImGui::GetWindowWidth();
}

float imgui_get_window_height(void) {
    return ImGui::GetWindowHeight();
}

imgui_vec2 imgui_get_window_content_region_max(void) {
    const ImVec2& v = ImGui::GetWindowContentRegionMax();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

imgui_vec2 imgui_get_window_content_region_min(void) {
    const ImVec2& v = ImGui::GetWindowContentRegionMin();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

imgui_draw_list* imgui_get_window_draw_list(void) {
    return (imgui_draw_list*)ImGui::GetWindowDrawList();
}

imgui_draw_list* imgui_get_foreground_draw_list(void) {
    return (imgui_draw_list*)ImGui::GetForegroundDrawList();
}

imgui_draw_list* imgui_get_background_draw_list(void) {
    return (imgui_draw_list*)ImGui::GetBackgroundDrawList();
}

bool imgui_is_key_down(int key) {
    return ImGui::IsKeyDown((ImGuiKey)key);
}

bool imgui_is_key_pressed(int key, bool repeat) {
    return ImGui::IsKeyPressed((ImGuiKey)key, repeat);
}

bool imgui_is_key_released(int key) {
    return ImGui::IsKeyReleased((ImGuiKey)key);
}

bool imgui_is_mouse_down(int button) {
    return ImGui::IsMouseDown((ImGuiMouseButton)button);
}

bool imgui_is_mouse_clicked(int button, bool repeat) {
    return ImGui::IsMouseClicked((ImGuiMouseButton)button, repeat);
}

bool imgui_is_mouse_released(int button) {
    return ImGui::IsMouseReleased((ImGuiMouseButton)button);
}

bool imgui_is_mouse_double_clicked(int button) {
    return ImGui::IsMouseDoubleClicked((ImGuiMouseButton)button);
}

bool imgui_is_mouse_dragging(int button, float lock_threshold) {
    return ImGui::IsMouseDragging((ImGuiMouseButton)button, lock_threshold);
}

bool imgui_is_any_mouse_down(void) {
    return ImGui::IsAnyMouseDown();
}

bool imgui_is_mouse_pos_valid(imgui_vec2* mouse_pos) {
    if (mouse_pos != nullptr) {
        ImVec2 v(mouse_pos->x, mouse_pos->y);
        return ImGui::IsMousePosValid(&v);
    }
    return ImGui::IsMousePosValid(nullptr);
}

imgui_vec2 imgui_get_mouse_pos(void) {
    const ImVec2& v = ImGui::GetMousePos();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

imgui_vec2 imgui_get_mouse_drag_delta(int button, float lock_threshold) {
    const ImVec2& v = ImGui::GetMouseDragDelta((ImGuiMouseButton)button, lock_threshold);
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

void imgui_reset_mouse_drag_delta(int button) {
    ImGui::ResetMouseDragDelta((ImGuiMouseButton)button);
}

int imgui_get_mouse_cursor(void) {
    return (int)ImGui::GetMouseCursor();
}

void imgui_set_mouse_cursor(int cursor) {
    ImGui::SetMouseCursor((ImGuiMouseCursor)cursor);
}

void imgui_set_keyboard_focus_here(int offset) {
    ImGui::SetKeyboardFocusHere(offset);
}

void imgui_set_next_frame_want_capture_keyboard(bool want_capture_keyboard) {
    ImGui::SetNextFrameWantCaptureKeyboard(want_capture_keyboard);
}

void imgui_set_next_frame_want_capture_mouse(bool want_capture_mouse) {
    ImGui::SetNextFrameWantCaptureMouse(want_capture_mouse);
}

void imgui_set_clipboard_text(const char* text) {
    ImGui::SetClipboardText(text);
}

const char* imgui_get_clipboard_text(void) {
    return ImGui::GetClipboardText();
}

double imgui_get_time(void) {
    return ImGui::GetTime();
}

imgui_vec2 imgui_get_cursor_pos(void) {
    const ImVec2& v = ImGui::GetCursorPos();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

imgui_vec2 imgui_get_cursor_screen_pos(void) {
    const ImVec2& v = ImGui::GetCursorScreenPos();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

imgui_vec2 imgui_get_cursor_start_pos(void) {
    const ImVec2& v = ImGui::GetCursorStartPos();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

void imgui_set_cursor_pos_x(float local_x) {
    ImGui::SetCursorPosX(local_x);
}

void imgui_set_cursor_screen_pos(imgui_vec2 pos) {
    ImGui::SetCursorScreenPos(ImVec2(pos.x, pos.y));
}

imgui_vec2 imgui_get_content_region_avail(void) {
    const ImVec2& v = ImGui::GetContentRegionAvail();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

float imgui_get_scroll_x(void) {
    return ImGui::GetScrollX();
}

float imgui_get_scroll_y(void) {
    return ImGui::GetScrollY();
}

float imgui_get_scroll_max_x(void) {
    return ImGui::GetScrollMaxX();
}

float imgui_get_scroll_max_y(void) {
    return ImGui::GetScrollMaxY();
}

void imgui_set_scroll_here_x(float center_x_ratio) {
    ImGui::SetScrollHereX(center_x_ratio);
}

void imgui_set_scroll_here_y(float center_y_ratio) {
    ImGui::SetScrollHereY(center_y_ratio);
}

void imgui_set_scroll_from_pos_x(float local_x, float center_x_ratio) {
    ImGui::SetScrollFromPosX(local_x, center_x_ratio);
}

void imgui_set_scroll_from_pos_y(float local_y, float center_y_ratio) {
    ImGui::SetScrollFromPosY(local_y, center_y_ratio);
}

void imgui_set_scroll_x(float scroll_x) {
    ImGui::SetScrollX(scroll_x);
}

void imgui_set_scroll_y(float scroll_y) {
    ImGui::SetScrollY(scroll_y);
}

int imgui_get_frame_count(void) {
    return ImGui::GetFrameCount();
}

float imgui_get_frame_height(void) {
    return ImGui::GetFrameHeight();
}

float imgui_get_frame_height_with_spacing(void) {
    return ImGui::GetFrameHeightWithSpacing();
}

float imgui_get_font_size(void) {
    return ImGui::GetFontSize();
}

imgui_font* imgui_get_font(void) {
    return (imgui_font*)ImGui::GetFont();
}

imgui_viewport* imgui_get_main_viewport(void) {
    return (imgui_viewport*)ImGui::GetMainViewport();
}

imgui_vec4 imgui_get_style_color_vec4(int idx) {
    const ImVec4& c = ImGui::GetStyleColorVec4((ImGuiCol)idx);
    imgui_vec4 out;
    out.x = c.x;
    out.y = c.y;
    out.z = c.z;
    out.w = c.w;
    return out;
}

float imgui_get_cursor_pos_x(void) {
    return ImGui::GetCursorPosX();
}

const char* imgui_get_key_name(int key) {
    return ImGui::GetKeyName((ImGuiKey)key);
}

float imgui_get_text_line_height(void) {
    return ImGui::GetTextLineHeight();
}

float imgui_get_text_line_height_with_spacing(void) {
    return ImGui::GetTextLineHeightWithSpacing();
}

int imgui_get_id(const char* str_id) {
    return (int)ImGui::GetID(str_id);
}

int imgui_get_color_u32(int idx, float alpha_mul) {
    return (int)ImGui::GetColorU32((ImGuiCol)idx, alpha_mul);
}

const char* imgui_get_style_color_name(int idx) {
    return ImGui::GetStyleColorName((ImGuiCol)idx);
}

imgui_vec2 imgui_calc_text_size(const char* text, bool hide_text_after_double_hash, float wrap_width) {
    const ImVec2& v = ImGui::CalcTextSize(text, nullptr, hide_text_after_double_hash, wrap_width);
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

float imgui_calc_item_width(void) {
    return ImGui::CalcItemWidth();
}

// =========================================================================
// Columns (legacy multi-column layout)
// =========================================================================

void imgui_columns(int count, const char* id, bool border) {
    ImGui::Columns(count, id, border);
}

void imgui_next_column(void) {
    ImGui::NextColumn();
}

int imgui_get_column_index(void) {
    return ImGui::GetColumnIndex();
}

float imgui_get_column_offset(int column_index) {
    return ImGui::GetColumnOffset(column_index);
}

void imgui_set_column_offset(int column_index, float offset_x) {
    ImGui::SetColumnOffset(column_index, offset_x);
}

float imgui_get_column_width(int column_index) {
    return ImGui::GetColumnWidth(column_index);
}

void imgui_set_column_width(int column_index, float width) {
    ImGui::SetColumnWidth(column_index, width);
}

int imgui_get_columns_count(void) {
    return ImGui::GetColumnsCount();
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

void imgui_table_header(const char* label) {
    ImGui::TableHeader(label);
}

void imgui_table_angled_headers_row(void) {
    ImGui::TableAngledHeadersRow();
}

int imgui_table_get_column_count(void) {
    return ImGui::TableGetColumnCount();
}

int imgui_table_get_column_flags(int column_n) {
    return (int)ImGui::TableGetColumnFlags(column_n);
}

int imgui_table_get_column_index(void) {
    return ImGui::TableGetColumnIndex();
}

int imgui_table_get_row_index(void) {
    return ImGui::TableGetRowIndex();
}

const char* imgui_table_get_column_name(int column_n) {
    return ImGui::TableGetColumnName(column_n);
}

void* imgui_table_get_sort_specs(void) {
    return (void*)ImGui::TableGetSortSpecs();
}

void imgui_table_set_bg_color(int target, uint32_t color, int column_n) {
    ImGui::TableSetBgColor((ImGuiTableBgTarget)target, color, column_n);
}

bool imgui_tab_item_button(const char* label, int flags) {
    return ImGui::TabItemButton(label, (ImGuiTabItemFlags)flags);
}

// =========================================================================
// Style
// =========================================================================

void imgui_style_colors_dark(void) {
    ImGui::StyleColorsDark();
}

void imgui_style_colors_light(void) {
    ImGui::StyleColorsLight();
}

void imgui_style_colors_classic(void) {
    ImGui::StyleColorsClassic();
}

bool imgui_show_style_selector(const char* label) {
    return ImGui::ShowStyleSelector(label);
}

void imgui_show_font_selector(const char* label) {
    ImGui::ShowFontSelector(label);
}

void imgui_show_style_editor(void) {
    ImGui::ShowStyleEditor();
}

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

void imgui_set_next_item_open(bool is_open, int cond) {
    ImGui::SetNextItemOpen(is_open, (ImGuiCond)cond);
}

void imgui_set_next_item_allow_overlap(void) {
    ImGui::SetNextItemAllowOverlap();
}

void imgui_set_next_item_selection_user_data(int64_t selection_user_data) {
    ImGui::SetNextItemSelectionUserData(selection_user_data);
}

void imgui_set_next_item_shortcut(int key_chord, int flags) {
    ImGui::SetNextItemShortcut((ImGuiKeyChord)key_chord, (ImGuiInputFlags)flags);
}

void imgui_set_next_window_collapsed(bool collapsed, int cond) {
    ImGui::SetNextWindowCollapsed(collapsed, (ImGuiCond)cond);
}

void imgui_set_next_window_content_size(imgui_vec2 size) {
    ImGui::SetNextWindowContentSize(ImVec2(size.x, size.y));
}

void imgui_set_next_window_focus(void) {
    ImGui::SetNextWindowFocus();
}

void imgui_set_next_window_scroll(imgui_vec2 scroll) {
    ImGui::SetNextWindowScroll(ImVec2(scroll.x, scroll.y));
}

void imgui_set_next_window_size_constraints(imgui_vec2 size_min, imgui_vec2 size_max) {
    ImGui::SetNextWindowSizeConstraints(ImVec2(size_min.x, size_min.y), ImVec2(size_max.x, size_max.y));
}

void imgui_set_item_tooltip(const char* text) {
    ImGui::SetItemTooltip("%s", text);
}

void imgui_set_item_default_focus(void) {
    ImGui::SetItemDefaultFocus();
}

void imgui_set_tab_item_closed(const char* tab_or_docked_window_label) {
    ImGui::SetTabItemClosed(tab_or_docked_window_label);
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
        g_ini_filename[0] = '\0';
        ((ImGuiIO*)io)->IniFilename = nullptr;
    } else {
        strncpy(g_ini_filename, path, sizeof(g_ini_filename) - 1);
        g_ini_filename[sizeof(g_ini_filename) - 1] = '\0';
        ((ImGuiIO*)io)->IniFilename = g_ini_filename;
    }
}

void imgui_io_set_font_global_scale(imgui_io* io, float scale) {
    ((ImGuiIO*)io)->FontGlobalScale = scale;
}

imgui_vec2 imgui_io_get_display_size(imgui_io* io) {
    ImVec2 v = ((ImGuiIO*)io)->DisplaySize;
    return {v.x, v.y};
}

imgui_vec2 imgui_io_get_display_framebuffer_scale(imgui_io* io) {
    ImVec2 v = ((ImGuiIO*)io)->DisplayFramebufferScale;
    return {v.x, v.y};
}

float imgui_io_get_delta_time(imgui_io* io) {
    return ((ImGuiIO*)io)->DeltaTime;
}

int imgui_io_get_config_flags(imgui_io* io) {
    return ((ImGuiIO*)io)->ConfigFlags;
}

int imgui_io_get_backend_flags(imgui_io* io) {
    return ((ImGuiIO*)io)->BackendFlags;
}

const char* imgui_io_get_ini_filename(imgui_io* io) {
    return ((ImGuiIO*)io)->IniFilename;
}

float imgui_io_get_font_global_scale(imgui_io* io) {
    return ((ImGuiIO*)io)->FontGlobalScale;
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

// Builds an ImFontConfig from the flattened C params.
static ImFontConfig imgui_font_config_from(const char* name, bool merge_mode, bool pixel_snap_h, int oversample_h, int oversample_v,
    float size_pixels, float glyph_offset_x, float glyph_offset_y,
    float glyph_min_advance_x, float glyph_max_advance_x,
    float rasterizer_multiply, float rasterizer_density, float extra_size_scale) {
    ImFontConfig cfg;
    cfg.MergeMode = merge_mode;
    cfg.PixelSnapH = pixel_snap_h;
    cfg.OversampleH = (ImS8)oversample_h;
    cfg.OversampleV = (ImS8)oversample_v;
    cfg.SizePixels = size_pixels;
    cfg.GlyphOffset.x = glyph_offset_x;
    cfg.GlyphOffset.y = glyph_offset_y;
    cfg.GlyphMinAdvanceX = glyph_min_advance_x;
    cfg.GlyphMaxAdvanceX = glyph_max_advance_x;
    cfg.RasterizerMultiply = rasterizer_multiply;
    cfg.RasterizerDensity = rasterizer_density;
    cfg.ExtraSizeScale = extra_size_scale;
    if (name && name[0]) {
        ImStrncpy(cfg.Name, name, IM_COUNTOF(cfg.Name));
    }
    return cfg;
}

imgui_font* imgui_font_atlas_add_font_default_cfg(imgui_font_atlas* atlas,
    const char* name, bool merge_mode, bool pixel_snap_h,
    int oversample_h, int oversample_v,
    float size_pixels, float glyph_offset_x, float glyph_offset_y,
    float glyph_min_advance_x, float glyph_max_advance_x,
    float rasterizer_multiply, float rasterizer_density, float extra_size_scale) {
    ImFontConfig cfg = imgui_font_config_from(name, merge_mode, pixel_snap_h, oversample_h, oversample_v,
        size_pixels, glyph_offset_x, glyph_offset_y, glyph_min_advance_x, glyph_max_advance_x,
        rasterizer_multiply, rasterizer_density, extra_size_scale);
    return (imgui_font*)((ImFontAtlas*)atlas)->AddFontDefault(&cfg);
}

imgui_font* imgui_font_atlas_add_font_from_file_ttf_cfg(imgui_font_atlas* atlas, const char* path,
    const char* name, bool merge_mode, bool pixel_snap_h,
    int oversample_h, int oversample_v,
    float size_pixels, float glyph_offset_x, float glyph_offset_y,
    float glyph_min_advance_x, float glyph_max_advance_x,
    float rasterizer_multiply, float rasterizer_density, float extra_size_scale) {
    ImFontConfig cfg = imgui_font_config_from(name, merge_mode, pixel_snap_h, oversample_h, oversample_v,
        size_pixels, glyph_offset_x, glyph_offset_y, glyph_min_advance_x, glyph_max_advance_x,
        rasterizer_multiply, rasterizer_density, extra_size_scale);
    return (imgui_font*)((ImFontAtlas*)atlas)->AddFontFromFileTTF(path, cfg.SizePixels, &cfg);
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

void imgui_draw_list_add_line(imgui_draw_list* list, imgui_vec2 p1, imgui_vec2 p2, uint32_t col, float thickness) {
    ((ImDrawList*)list)->AddLine(ImVec2(p1.x, p1.y), ImVec2(p2.x, p2.y), col, thickness);
}

void imgui_draw_list_add_rect(imgui_draw_list* list, imgui_vec2 p_min, imgui_vec2 p_max, uint32_t col, float rounding, int flags, float thickness) {
    ((ImDrawList*)list)->AddRect(ImVec2(p_min.x, p_min.y), ImVec2(p_max.x, p_max.y), col, rounding, (ImDrawFlags)flags, thickness);
}

void imgui_draw_list_add_rect_filled(imgui_draw_list* list, imgui_vec2 p_min, imgui_vec2 p_max, uint32_t col, float rounding, int flags) {
    ((ImDrawList*)list)->AddRectFilled(ImVec2(p_min.x, p_min.y), ImVec2(p_max.x, p_max.y), col, rounding, (ImDrawFlags)flags);
}

void imgui_draw_list_add_circle(imgui_draw_list* list, imgui_vec2 center, float radius, uint32_t col, int num_segments, float thickness) {
    ((ImDrawList*)list)->AddCircle(ImVec2(center.x, center.y), radius, col, num_segments, thickness);
}

void imgui_draw_list_add_circle_filled(imgui_draw_list* list, imgui_vec2 center, float radius, uint32_t col, int num_segments) {
    ((ImDrawList*)list)->AddCircleFilled(ImVec2(center.x, center.y), radius, col, num_segments);
}

void imgui_draw_list_add_text(imgui_draw_list* list, imgui_vec2 pos, uint32_t col, const char* text) {
    ((ImDrawList*)list)->AddText(ImVec2(pos.x, pos.y), col, text);
}

void imgui_draw_list_add_quad(imgui_draw_list* list, imgui_vec2 p1, imgui_vec2 p2, imgui_vec2 p3, imgui_vec2 p4, uint32_t col, float thickness) {
    ((ImDrawList*)list)->AddQuad(ImVec2(p1.x, p1.y), ImVec2(p2.x, p2.y), ImVec2(p3.x, p3.y), ImVec2(p4.x, p4.y), col, thickness);
}

void imgui_draw_list_add_triangle(imgui_draw_list* list, imgui_vec2 p1, imgui_vec2 p2, imgui_vec2 p3, uint32_t col, float thickness) {
    ((ImDrawList*)list)->AddTriangle(ImVec2(p1.x, p1.y), ImVec2(p2.x, p2.y), ImVec2(p3.x, p3.y), col, thickness);
}

void imgui_draw_list_add_polyline(imgui_draw_list* list, const imgui_vec2* points, int points_count, uint32_t col, bool closed, float thickness) {
    ImDrawFlags flags = closed ? ImDrawFlags_Closed : ImDrawFlags_None;
    ((ImDrawList*)list)->AddPolyline(reinterpret_cast<const ImVec2*>(points), points_count, col, thickness, flags);
}

} // extern "C"
