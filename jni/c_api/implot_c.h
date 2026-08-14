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

#ifndef IMPLOT_C_H
#define IMPLOT_C_H

#include <stdbool.h>
#include <stdint.h>

#include "imgui_c.h"

#ifdef __cplusplus
extern "C" {
#endif

// =========================================================================
// Types
// =========================================================================

typedef struct implot_context implot_context;

// Mirrors the C++ ImPlotSpec fields that are useful from Kotlin. A *_set
// flag of 0 keeps the library default for the corresponding field.
typedef struct implot_spec {
    float line_color[4];
    int line_color_set;
    float line_weight;
    int line_weight_set;
    float fill_color[4];
    int fill_color_set;
    float fill_alpha;
    int fill_alpha_set;
    int marker;
    int marker_set;
    float marker_size;
    int marker_size_set;
    float marker_line_color[4];
    int marker_line_color_set;
    float marker_fill_color[4];
    int marker_fill_color_set;
    float size;
    int size_set;
    int offset;
    int offset_set;
    int stride;
    int stride_set;
    int flags;
    int flags_set;
} implot_spec;

// =========================================================================
// Contexts
// =========================================================================

implot_context* implot_create_context(void);
void implot_destroy_context(implot_context* ctx);
implot_context* implot_get_current_context(void);
void implot_set_im_gui_context(imgui_context* ctx);
void implot_show_demo_window(bool* p_open);

// =========================================================================
// Begin/End plot + setup
// =========================================================================

bool implot_begin_plot(const char* title_id, imgui_vec2 size, int flags);
void implot_end_plot(void);
void implot_setup_axes(const char* x_label, const char* y_label, int x_flags, int y_flags);
void implot_setup_axes_limits(double x_min, double x_max, double y_min, double y_max, int cond);
void implot_setup_axis_limits(int axis, double v_min, double v_max, int cond);
void implot_setup_legend(int location, int flags);
void implot_setup_finish(void);

void implot_setup_axis(int axis, const char* label, int flags);
void implot_setup_axis_format(int axis, const char* fmt);
void implot_setup_axis_limits_constraints(int axis, double v_min, double v_max);
void implot_setup_axis_zoom_constraints(int axis, double z_min, double z_max);
void implot_setup_axis_links(int axis, double* link_min, double* link_max);
void implot_setup_axis_scale(int axis, int scale);
void implot_setup_axis_ticks(int axis, const double* values, int tick_count, const char* const* labels, bool keep_default);
void implot_setup_mouse_text(int location, int flags);

// =========================================================================
// SetNext (call before BeginPlot)
// =========================================================================

void implot_set_next_axes_limits(double x_min, double x_max, double y_min, double y_max, int cond);
void implot_set_next_axis_limits(int axis, double v_min, double v_max, int cond);

// =========================================================================
// Plot items
// =========================================================================

void implot_plot_line(const char* label_id, const float* xs, const float* ys, int count, const implot_spec* spec);
void implot_plot_line_values(const char* label_id, const float* values, int count, double xscale, double xstart, const implot_spec* spec);
void implot_plot_scatter(const char* label_id, const float* xs, const float* ys, int count, const implot_spec* spec);
void implot_plot_scatter_values(const char* label_id, const float* values, int count, double xscale, double xstart, const implot_spec* spec);
void implot_plot_stairs(const char* label_id, const float* xs, const float* ys, int count, const implot_spec* spec);
void implot_plot_bars(const char* label_id, const float* xs, const float* ys, int count, double bar_size, const implot_spec* spec);
void implot_plot_bars_values(const char* label_id, const float* values, int count, double bar_size, double shift, const implot_spec* spec);
double implot_plot_histogram(const char* label_id, const float* values, int count, int bins, double bar_scale, double range_min, double range_max, const implot_spec* spec);
void implot_plot_inf_lines(const char* label_id, const float* values, int count, const implot_spec* spec);
void implot_plot_shaded(const char* label_id, const float* xs, const float* ys, int count, double yref, const implot_spec* spec);
void implot_plot_text(const char* text, double x, double y, imgui_vec2 pix_offset);
void implot_plot_dummy(const char* label_id, const implot_spec* spec);

void implot_plot_bar_groups(const char* const* label_ids, const float* values, int item_count, int group_count, double group_size, double shift, const implot_spec* spec);
void implot_plot_error_bars(const char* label_id, const float* xs, const float* ys, const float* neg, const float* pos, int count, const implot_spec* spec);
void implot_plot_stems(const char* label_id, const float* xs, const float* ys, int count, double ref, const implot_spec* spec);
void implot_plot_heatmap(const char* label_id, const float* values, int rows, int cols, double scale_min, double scale_max, const char* label_fmt, double bounds_x_min, double bounds_y_min, double bounds_x_max, double bounds_y_max, const implot_spec* spec);
double implot_plot_histogram_2d(const char* label_id, const float* xs, const float* ys, int count, int x_bins, int y_bins, double range_x_min, double range_x_max, double range_y_min, double range_y_max, const implot_spec* spec);
void implot_plot_digital(const char* label_id, const float* xs, const float* ys, int count, const implot_spec* spec);
void implot_plot_pie_chart(const char* const* label_ids, const float* values, int count, double x, double y, double radius, const char* label_fmt, double angle0, const implot_spec* spec);
void implot_plot_bubbles(const char* label_id, const float* xs, const float* ys, const float* sizes, int count, const implot_spec* spec);
void implot_plot_polygon(const char* label_id, const float* xs, const float* ys, int count, const implot_spec* spec);
void implot_plot_image(const char* label_id, uint64_t tex_id, double x_min, double y_min, double x_max, double y_max, imgui_vec2 uv_min, imgui_vec2 uv_max, imgui_vec4 tint_col, const implot_spec* spec);

// =========================================================================
// Subplots
// =========================================================================

bool implot_begin_subplots(const char* title_id, int rows, int cols, imgui_vec2 size, int flags);
void implot_end_subplots(void);

// =========================================================================
// Drag tools / annotations / tags
// =========================================================================

bool implot_drag_point(int id, double* x, double* y, imgui_vec4 col, float size, int flags);
bool implot_drag_line_x(int id, double* x, imgui_vec4 col, float thickness, int flags);
bool implot_drag_line_y(int id, double* y, imgui_vec4 col, float thickness, int flags);
bool implot_drag_rect(int id, double* x_min, double* y_min, double* x_max, double* y_max, imgui_vec4 col, int flags);
void implot_annotation(double x, double y, imgui_vec4 col, imgui_vec2 pix_offset, bool clamp, bool round, const char* fmt);
void implot_tag_x(double x, imgui_vec4 col, bool round, const char* fmt);
void implot_tag_y(double y, imgui_vec4 col, bool round, const char* fmt);

// =========================================================================
// Style
// =========================================================================

void implot_push_style_color_vec4(int idx, imgui_vec4 color);
void implot_push_style_color_u32(int idx, uint32_t color);
void implot_pop_style_color(int count);
void implot_push_style_var_float(int idx, float val);
void implot_push_style_var_int(int idx, int val);
void implot_push_style_var_vec2(int idx, imgui_vec2 val);
void implot_pop_style_var(int count);
void implot_push_colormap(int cmap);
void implot_pop_colormap(int count);

int implot_get_colormap_count(void);
const char* implot_get_colormap_name(int cmap);
imgui_vec4 implot_get_colormap_color(int idx, int cmap);
imgui_vec4 implot_sample_colormap(float t, int cmap);
imgui_vec4 implot_next_colormap_color(void);
bool implot_colormap_button(const char* label, imgui_vec2 size, int cmap);
void implot_colormap_scale(const char* label, double scale_min, double scale_max, imgui_vec2 size, const char* fmt, int flags, int cmap);
bool implot_colormap_slider(const char* label, float* t, imgui_vec4* out, const char* fmt, int cmap);
void implot_colormap_icon(int cmap);

// =========================================================================
// Color maps (misc)
// =========================================================================

int implot_add_colormap(const char* name, const float* cols, int col_count);
void implot_item_icon(uint32_t col);
uint32_t implot_get_last_item_color(void);

// =========================================================================
// Plot utils
// =========================================================================

void implot_set_axis(int axis);
void implot_set_axes(int x_axis, int y_axis);
void implot_get_plot_selection(double* out_x_min, double* out_y_min, double* out_x_max, double* out_y_max);
void implot_push_plot_clip_rect(float expand);
void implot_pop_plot_clip_rect(void);

// =========================================================================
// Drag and drop
// =========================================================================

bool implot_begin_drag_drop_source_plot(int flags);
bool implot_begin_drag_drop_source_axis(int axis, int flags);
bool implot_begin_drag_drop_source_item(const char* label_id, int flags);
void implot_end_drag_drop_source(void);
bool implot_begin_drag_drop_target_plot(void);
bool implot_begin_drag_drop_target_axis(int axis);
bool implot_begin_drag_drop_target_legend(void);
void implot_end_drag_drop_target(void);

// =========================================================================
// Legend popup
// =========================================================================

bool implot_begin_legend_popup(const char* label_id, int mouse_button);
void implot_end_legend_popup(void);

// =========================================================================
// Input mapping / tools
// =========================================================================

void* implot_get_input_map(void);
bool implot_show_input_map_selector(const char* label);
void implot_show_metrics_window(bool* p_open);
void implot_show_style_editor(void);
bool implot_show_style_selector(const char* label);
bool implot_show_colormap_selector(const char* label);

// =========================================================================
// Queries
// =========================================================================

bool implot_is_plot_hovered(void);
bool implot_is_plot_selected(void);
bool implot_is_axis_hovered(int axis);
imgui_vec2 implot_get_plot_pos(void);
imgui_vec2 implot_get_plot_size(void);
void implot_get_plot_limits(double* out_x_min, double* out_y_min, double* out_x_max, double* out_y_max);
void implot_get_plot_mouse_pos(double* out_x, double* out_y);
void implot_pixels_to_plot(float pix_x, float pix_y, double* out_x, double* out_y);
imgui_vec2 implot_plot_to_pixels(double x, double y);
imgui_draw_list* implot_get_plot_draw_list(void);

#ifdef __cplusplus
}
#endif

#endif // IMPLOT_C_H
