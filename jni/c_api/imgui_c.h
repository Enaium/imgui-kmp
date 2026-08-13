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

#ifndef IMGUI_C_H
#define IMGUI_C_H

#include <stdbool.h>
#include <stdint.h>
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

// =========================================================================
// Types
// =========================================================================

// Opaque handles (implemented as the real imgui C++ types in imgui_c.cc)
typedef struct imgui_context imgui_context;
typedef struct imgui_io imgui_io;
typedef struct imgui_style imgui_style;
typedef struct imgui_draw_data imgui_draw_data;
typedef struct imgui_draw_list imgui_draw_list;
typedef struct imgui_draw_cmd imgui_draw_cmd;
typedef struct imgui_font imgui_font;
typedef struct imgui_font_atlas imgui_font_atlas;
typedef struct imgui_plot_context imgui_plot_context;

// Value types (layout-compatible with ImVec2/ImVec4/ImDrawVert)
typedef struct imgui_vec2 { float x; float y; } imgui_vec2;
typedef struct imgui_vec4 { float x, y, z, w; } imgui_vec4;

typedef struct imgui_draw_vert {
    float pos_x, pos_y;
    float uv_x, uv_y;
    uint32_t col; // RGBA, packed as ImU32 (0xRRGGBBAA)
} imgui_draw_vert;

// =========================================================================
// Context / main frame
// =========================================================================

imgui_context* imgui_create_context(void);
void imgui_destroy_context(imgui_context* ctx);
imgui_context* imgui_get_current_context(void);
imgui_io* imgui_get_io(void);
imgui_style* imgui_get_style(void);
const char* imgui_get_version(void);
void imgui_new_frame(void);
void imgui_render(void);
imgui_draw_data* imgui_get_draw_data(void);
void imgui_show_demo_window(bool* p_open);

// =========================================================================
// Windows
// =========================================================================

bool imgui_begin(const char* name, bool* p_open, int flags);
void imgui_end(void);
bool imgui_begin_child(const char* str_id, imgui_vec2 size, int child_flags, int window_flags);
void imgui_end_child(void);
void imgui_set_next_window_pos(imgui_vec2 pos, int cond, imgui_vec2 pivot);
void imgui_set_next_window_size(imgui_vec2 size, int cond);
void imgui_set_next_window_bg_alpha(float alpha);
void imgui_begin_disabled(bool disabled);
void imgui_end_disabled(void);
bool imgui_begin_main_menu_bar(void);
void imgui_end_main_menu_bar(void);
bool imgui_begin_menu_bar(void);
void imgui_end_menu_bar(void);
bool imgui_begin_menu(const char* label, bool enabled);
void imgui_end_menu(void);
bool imgui_menu_item(const char* label, const char* shortcut, bool selected, bool enabled);
bool imgui_begin_tab_bar(const char* str_id, int flags);
void imgui_end_tab_bar(void);
bool imgui_begin_tab_item(const char* label, bool* p_open, int flags);
void imgui_end_tab_item(void);
bool imgui_begin_tooltip(void);
void imgui_end_tooltip(void);
void imgui_set_tooltip(const char* text);
void imgui_open_popup(const char* str_id, int popup_flags);
bool imgui_begin_popup(const char* str_id, int flags);
bool imgui_begin_popup_modal(const char* name, bool* p_open, int flags);
void imgui_end_popup(void);
void imgui_close_current_popup(void);
bool imgui_begin_combo(const char* label, const char* preview_value, int flags);
void imgui_end_combo(void);

// =========================================================================
// Widgets
// =========================================================================

void imgui_text(const char* text);
void imgui_text_colored(imgui_vec4 color, const char* text);
void imgui_text_disabled(const char* text);
void imgui_label_text(const char* label, const char* text);
void imgui_bullet_text(const char* text);
void imgui_bullet(void);
void imgui_separator(void);
void imgui_separator_text(const char* text);
void imgui_same_line(float offset_from_start_x, float spacing);
void imgui_new_line(void);
void imgui_spacing(void);
void imgui_dummy(imgui_vec2 size);
void imgui_indent(float indent_w);
void imgui_unindent(float indent_w);
bool imgui_button(const char* label, imgui_vec2 size);
bool imgui_small_button(const char* label);
bool imgui_checkbox(const char* label, bool* v);
bool imgui_slider_float(const char* label, float* v, float v_min, float v_max, const char* format);
bool imgui_slider_int(const char* label, int* v, int v_min, int v_max, const char* format);
bool imgui_input_text(const char* label, char* buf, int buf_size, int flags);
bool imgui_combo(const char* label, int* current_item, const char** items, int items_count);
bool imgui_selectable(const char* label, bool selected, int flags, imgui_vec2 size);
bool imgui_radio_button(const char* label, bool active);
void imgui_progress_bar(float fraction, imgui_vec2 size, const char* overlay);
bool imgui_collapsing_header(const char* label, int flags);
bool imgui_tree_node(const char* label);
void imgui_tree_pop(void);
bool imgui_invisible_button(const char* str_id, imgui_vec2 size, int flags);
void imgui_begin_group(void);
void imgui_end_group(void);
void imgui_set_cursor_pos(imgui_vec2 local_pos);
void imgui_push_id(const char* str_id);
void imgui_pop_id(void);
bool imgui_is_item_hovered(int flags);
bool imgui_is_item_active(void);
bool imgui_is_item_clicked(int mouse_button);
bool imgui_is_window_hovered(int flags);
bool imgui_is_window_focused(int flags);

// =========================================================================
// Tables
// =========================================================================

bool imgui_begin_table(const char* str_id, int column, int flags, imgui_vec2 outer_size, float inner_width);
void imgui_end_table(void);
void imgui_table_next_row(int min_row_height, int flags);
bool imgui_table_next_column(void);
bool imgui_table_set_column_index(int column_n);
void imgui_table_setup_column(const char* label, int flags, float init_width_or_weight, int user_id);
void imgui_table_setup_scroll_freeze(int cols, int rows);
void imgui_table_headers_row(void);

// =========================================================================
// Style
// =========================================================================

void imgui_push_style_color_vec4(int idx, imgui_vec4 color);
void imgui_push_style_color_u32(int idx, uint32_t color);
void imgui_pop_style_color(int count);
void imgui_push_style_var_float(int idx, float val);
void imgui_push_style_var_vec2(int idx, imgui_vec2 val);
void imgui_pop_style_var(int count);
void imgui_push_font(imgui_font* font);
void imgui_pop_font(void);
void imgui_push_item_width(float item_width);
void imgui_pop_item_width(void);
void imgui_set_next_item_width(float item_width);
imgui_vec4 imgui_style_get_color(imgui_style* style, int idx);
void imgui_style_set_color(imgui_style* style, int idx, imgui_vec4 color);

// =========================================================================
// IO
// =========================================================================

void imgui_io_set_display_size(imgui_io* io, float w, float h);
void imgui_io_set_display_framebuffer_scale(imgui_io* io, float sx, float sy);
void imgui_io_set_delta_time(imgui_io* io, float dt);
void imgui_io_set_config_flags(imgui_io* io, int flags);
void imgui_io_set_backend_flags(imgui_io* io, int flags);
void imgui_io_set_ini_filename(imgui_io* io, const char* path);
void imgui_io_set_font_global_scale(imgui_io* io, float scale);
void imgui_io_add_mouse_pos_event(imgui_io* io, float x, float y);
void imgui_io_add_mouse_button_event(imgui_io* io, int button, bool down);
void imgui_io_add_mouse_wheel_event(imgui_io* io, float x, float y);
void imgui_io_add_key_event(imgui_io* io, int key, bool down);
void imgui_io_add_input_character(imgui_io* io, uint32_t c);
bool imgui_io_want_capture_mouse(imgui_io* io);
bool imgui_io_want_capture_keyboard(imgui_io* io);
bool imgui_io_want_text_input(imgui_io* io);
imgui_font_atlas* imgui_io_get_fonts(imgui_io* io);

// =========================================================================
// Fonts
// =========================================================================

imgui_font* imgui_font_atlas_add_font_from_file_ttf(imgui_font_atlas* atlas, const char* path, float size_px);
imgui_font* imgui_font_atlas_add_font_default(imgui_font_atlas* atlas);
bool imgui_font_atlas_build(imgui_font_atlas* atlas);
void imgui_font_atlas_get_tex_data_as_rgba32(imgui_font_atlas* atlas, const unsigned char** out_pixels, int* out_width, int* out_height, int* out_bpp);
void imgui_font_atlas_set_tex_id(imgui_font_atlas* atlas, uint64_t tex_id);

// =========================================================================
// Draw data (used by renderers to display the frame)
// =========================================================================

imgui_vec2 imgui_draw_data_get_display_pos(imgui_draw_data* data);
imgui_vec2 imgui_draw_data_get_display_size(imgui_draw_data* data);
imgui_vec2 imgui_draw_data_get_framebuffer_scale(imgui_draw_data* data);
int imgui_draw_data_get_cmd_lists_count(imgui_draw_data* data);
imgui_draw_list* imgui_draw_data_get_cmd_list(imgui_draw_data* data, int index);

int imgui_draw_list_get_vtx_count(imgui_draw_list* list);
int imgui_draw_list_get_idx_count(imgui_draw_list* list);
const imgui_draw_vert* imgui_draw_list_get_vtx_data(imgui_draw_list* list);
const uint16_t* imgui_draw_list_get_idx_data(imgui_draw_list* list);
int imgui_draw_list_get_cmd_count(imgui_draw_list* list);
imgui_draw_cmd* imgui_draw_list_get_cmd(imgui_draw_list* list, int index);

imgui_vec4 imgui_draw_cmd_get_clip_rect(imgui_draw_cmd* cmd);
uint64_t imgui_draw_cmd_get_tex_id(imgui_draw_cmd* cmd);
uint32_t imgui_draw_cmd_get_vtx_offset(imgui_draw_cmd* cmd);
uint32_t imgui_draw_cmd_get_idx_offset(imgui_draw_cmd* cmd);
uint32_t imgui_draw_cmd_get_elem_count(imgui_draw_cmd* cmd);
bool imgui_draw_cmd_has_user_callback(imgui_draw_cmd* cmd);

#ifdef __cplusplus
}
#endif

#endif // IMGUI_C_H
