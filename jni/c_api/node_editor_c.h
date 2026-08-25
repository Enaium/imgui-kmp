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

#ifndef NODE_EDITOR_C_H
#define NODE_EDITOR_C_H

#include <stdbool.h>
#include <stdint.h>

#include "imgui_c.h"

#ifdef __cplusplus
extern "C" {
#endif

// =========================================================================
// Types
// =========================================================================

typedef struct ne_context ne_context;

// Node/Pin/Link ids are uintptr_t inside imgui-node-editor; we surface them as
// int64_t value types (0 == invalid).
typedef int64_t ne_node_id;
typedef int64_t ne_pin_id;
typedef int64_t ne_link_id;

enum { ne_pin_kind_input = 0, ne_pin_kind_output = 1 };

// =========================================================================
// Context
// =========================================================================

ne_context* ne_create_editor(void);
void ne_destroy_editor(ne_context* ctx);
ne_context* ne_get_current_editor(void);
void ne_set_current_editor(ne_context* ctx);

const char* ne_get_style_color_name(int color_index);

// =========================================================================
// Style
// =========================================================================

void ne_push_style_color(int color_index, imgui_vec4 color);
void ne_pop_style_color(int count);
void ne_push_style_var_float(int var_index, float value);
void ne_push_style_var_vec2(int var_index, float x, float y);
void ne_push_style_var_vec4(int var_index, float x, float y, float z, float w);
void ne_pop_style_var(int count);

// =========================================================================
// Begin/End editor + nodes/pins
// =========================================================================

void ne_begin(const char* id, float size_x, float size_y);
void ne_end(void);

void ne_begin_node(ne_node_id id);
void ne_begin_pin(ne_pin_id id, int kind);
void ne_end_pin(void);
void ne_end_node(void);
void ne_group(float size_x, float size_y);

void ne_pin_rect(float a_x, float a_y, float b_x, float b_y);
void ne_pin_pivot_rect(float a_x, float a_y, float b_x, float b_y);
void ne_pin_pivot_size(float w, float h);
void ne_pin_pivot_scale(float sx, float sy);
void ne_pin_pivot_alignment(float ax, float ay);

bool ne_begin_group_hint(ne_node_id node_id);
imgui_vec2 ne_get_group_min(void);
imgui_vec2 ne_get_group_max(void);
imgui_draw_list* ne_get_hint_foreground_draw_list(void);
imgui_draw_list* ne_get_hint_background_draw_list(void);
void ne_end_group_hint(void);

imgui_draw_list* ne_get_node_background_draw_list(ne_node_id node_id);

// =========================================================================
// Links
// =========================================================================

bool ne_link(ne_link_id id, ne_pin_id start_pin_id, ne_pin_id end_pin_id, imgui_vec4 color, float thickness);
void ne_flow(ne_link_id link_id, int direction);

// =========================================================================
// Create action
// =========================================================================

bool ne_begin_create(imgui_vec4 color, float thickness);
bool ne_query_new_link(ne_pin_id* out_start_id, ne_pin_id* out_end_id);
bool ne_query_new_link_styled(ne_pin_id* out_start_id, ne_pin_id* out_end_id, imgui_vec4 color, float thickness);
bool ne_query_new_node(ne_pin_id* out_pin_id);
bool ne_query_new_node_styled(ne_pin_id* out_pin_id, imgui_vec4 color, float thickness);
bool ne_accept_new_item(void);
bool ne_accept_new_item_ex(imgui_vec4 color, float thickness);
void ne_reject_new_item(void);
void ne_reject_new_item_ex(imgui_vec4 color, float thickness);
void ne_end_create(void);

// =========================================================================
// Delete action
// =========================================================================

bool ne_begin_delete(void);
bool ne_query_deleted_link(ne_link_id* out_link_id, ne_pin_id* out_start_id, ne_pin_id* out_end_id);
bool ne_query_deleted_node(ne_node_id* out_node_id);
bool ne_accept_deleted_item(bool delete_dependencies);
void ne_reject_deleted_item(void);
void ne_end_delete(void);

// =========================================================================
// Node state
// =========================================================================

void ne_set_node_position(ne_node_id node_id, float x, float y);
void ne_set_group_size(ne_node_id node_id, float x, float y);
imgui_vec2 ne_get_node_position(ne_node_id node_id);
imgui_vec2 ne_get_node_size(ne_node_id node_id);
void ne_center_node_on_screen(ne_node_id node_id);
void ne_set_node_z_position(ne_node_id node_id, float z);
float ne_get_node_z_position(ne_node_id node_id);

// =========================================================================
// Suspension / activity
// =========================================================================

void ne_suspend(void);
void ne_resume(void);
bool ne_is_suspended(void);
bool ne_is_active(void);

// =========================================================================
// Selection
// =========================================================================

bool ne_has_selection_changed(void);
int ne_get_selected_object_count(void);
// Fills up to `capacity` ids; returns the actual number written via out_count.
void ne_get_selected_nodes(int64_t* out_ids, int capacity, int* out_count);
void ne_get_selected_links(int64_t* out_ids, int capacity, int* out_count);
bool ne_is_node_selected(ne_node_id node_id);
bool ne_is_link_selected(ne_link_id link_id);
void ne_clear_selection(void);
void ne_select_node(ne_node_id node_id, bool append);
void ne_select_link(ne_link_id link_id, bool append);
void ne_deselect_node(ne_node_id node_id);
void ne_deselect_link(ne_link_id link_id);

// =========================================================================
// Deletion by id / links
// =========================================================================

bool ne_delete_node(ne_node_id node_id);
bool ne_delete_link(ne_link_id link_id);

bool ne_has_any_links(int64_t id); // works for both node and pin ids
bool ne_has_any_links_pin(ne_pin_id pin_id);
int ne_break_links(int64_t id); // works for both node and pin ids
int ne_break_links_pin(ne_pin_id pin_id);
bool ne_pin_had_any_links(ne_pin_id pin_id);
bool ne_get_link_pins(ne_link_id link_id, ne_pin_id* out_start_id, ne_pin_id* out_end_id);

// =========================================================================
// Navigation
// =========================================================================

void ne_navigate_to_content(float duration);
void ne_navigate_to_selection(bool zoom_in, float duration);

// =========================================================================
// Context menus
// =========================================================================

bool ne_show_node_context_menu(ne_node_id* inout_node_id);
bool ne_show_pin_context_menu(ne_pin_id* inout_pin_id);
bool ne_show_link_context_menu(ne_link_id* inout_link_id);
bool ne_show_background_context_menu(void);

// =========================================================================
// Shortcuts
// =========================================================================

void ne_enable_shortcuts(bool enable);
bool ne_are_shortcuts_enabled(void);
bool ne_begin_shortcut(void);
bool ne_accept_cut(void);
bool ne_accept_copy(void);
bool ne_accept_paste(void);
bool ne_accept_duplicate(void);
bool ne_accept_create_node(void);
int ne_get_action_context_size(void);
void ne_get_action_context_nodes(int64_t* out_ids, int capacity, int* out_count);
void ne_get_action_context_links(int64_t* out_ids, int capacity, int* out_count);
void ne_end_shortcut(void);

float ne_get_current_zoom(void);

// =========================================================================
// Hover / click queries
// =========================================================================

ne_node_id ne_get_hovered_node(void);
ne_pin_id ne_get_hovered_pin(void);
ne_link_id ne_get_hovered_link(void);
ne_node_id ne_get_double_clicked_node(void);
ne_pin_id ne_get_double_clicked_pin(void);
ne_link_id ne_get_double_clicked_link(void);
bool ne_is_background_clicked(void);
bool ne_is_background_double_clicked(void);
int ne_get_background_click_button_index(void);
int ne_get_background_double_click_button_index(void);

// =========================================================================
// Coordinates / ordering
// =========================================================================

imgui_vec2 ne_get_screen_size(void);
imgui_vec2 ne_screen_to_canvas(float x, float y);
imgui_vec2 ne_canvas_to_screen(float x, float y);
int ne_get_node_count(void);
void ne_get_ordered_node_ids(int64_t* out_ids, int capacity, int* out_count);

#ifdef __cplusplus
}
#endif

#endif // NODE_EDITOR_C_H
