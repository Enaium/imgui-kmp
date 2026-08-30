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

/*
 * C API for ImPlot3D (https://github.com/brenocq/implot3d).
 *
 * Exposes the subset of ImPlot3D's public API that is useful from Kotlin.
 * Value types (point/ray/plane/box/range/quat) are mirrored as POD structs;
 * math helpers (Dot, Cross, Length, Normalize, Slerp, ...) are exposed as
 * free functions taking/returning those structs. Plot items take `double`
 * data arrays (the library converts everything to double internally).
 */

#ifndef IMPLOT3D_C_H
#define IMPLOT3D_C_H

#include <stdbool.h>
#include <stdint.h>

#include "imgui_c.h"

#ifdef __cplusplus
extern "C" {
#endif

// =========================================================================
// Types
// =========================================================================

typedef struct implot3d_context implot3d_context;

// 3D point / vector
typedef struct implot3d_point {
    double x;
    double y;
    double z;
} implot3d_point;

// Ray with an origin and a direction (not necessarily normalized)
typedef struct implot3d_ray {
    implot3d_point origin;
    implot3d_point direction;
} implot3d_ray;

// Plane defined by a point and a normal
typedef struct implot3d_plane {
    implot3d_point point;
    implot3d_point normal;
} implot3d_plane;

// Axis-aligned bounding box
typedef struct implot3d_box {
    implot3d_point min;
    implot3d_point max;
} implot3d_box;

// 1D range
typedef struct implot3d_range {
    double min;
    double max;
} implot3d_range;

// Quaternion
typedef struct implot3d_quat {
    double x;
    double y;
    double z;
    double w;
} implot3d_quat;

// Mirrors the C++ ImPlot3DSpec fields that are useful from Kotlin. A *_set
// flag of 0 keeps the library default for the corresponding field.
typedef struct implot3d_spec {
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
    int offset;
    int offset_set;
    int stride;
    int stride_set;
    int flags;
    int flags_set;
} implot3d_spec;

// Mirrors ImPlot3DStyle (colors + colormap index).
typedef struct implot3d_style {
    float line_weight;
    int marker;
    float marker_size;
    float fill_alpha;
    imgui_vec2 plot_default_size;
    imgui_vec2 plot_min_size;
    imgui_vec2 plot_padding;
    imgui_vec2 label_padding;
    float view_scale_factor;
    imgui_vec2 legend_padding;
    imgui_vec2 legend_inner_padding;
    imgui_vec2 legend_spacing;
    imgui_vec4 colors[14]; // ImPlot3DCol_COUNT
    int colormap;
} implot3d_style;

// =========================================================================
// Context
// =========================================================================

implot3d_context* implot3d_create_context(void);
void implot3d_destroy_context(implot3d_context* ctx);
implot3d_context* implot3d_get_current_context(void);
void implot3d_set_current_context(implot3d_context* ctx);

// =========================================================================
// Begin/End plot
// =========================================================================

bool implot3d_begin_plot(const char* title_id, imgui_vec2 size, int flags);
void implot3d_end_plot(void);

// =========================================================================
// Setup
// =========================================================================

void implot3d_setup_axis(int axis, const char* label, int flags);
void implot3d_setup_axis_limits(int axis, double v_min, double v_max, int cond);
void implot3d_setup_axis_ticks_values(int axis, const double* values, int n_ticks, const char* const* labels, bool keep_default);
void implot3d_setup_axis_ticks_limits(int axis, double v_min, double v_max, int n_ticks, const char* const* labels, bool keep_default);
void implot3d_setup_axis_scale(int axis, int scale);
void implot3d_setup_axis_limits_constraints(int axis, double v_min, double v_max);
void implot3d_setup_axis_zoom_constraints(int axis, double zoom_min, double zoom_max);
void implot3d_setup_axes(const char* x_label, const char* y_label, const char* z_label, int x_flags, int y_flags, int z_flags);
void implot3d_setup_axes_limits(double x_min, double x_max, double y_min, double y_max, double z_min, double z_max, int cond);
void implot3d_setup_box_rotation_angles(double elevation, double azimuth, bool animate, int cond);
void implot3d_setup_box_rotation_quat(implot3d_quat rotation, bool animate, int cond);
void implot3d_setup_box_initial_rotation_angles(double elevation, double azimuth);
void implot3d_setup_box_initial_rotation_quat(implot3d_quat rotation);
void implot3d_setup_box_scale(double x, double y, double z);
void implot3d_setup_legend(int location, int flags);

// =========================================================================
// Plot items (double data arrays)
// =========================================================================

void implot3d_plot_scatter(const char* label_id, const double* xs, const double* ys, const double* zs, int count, const implot3d_spec* spec);
void implot3d_plot_line(const char* label_id, const double* xs, const double* ys, const double* zs, int count, const implot3d_spec* spec);
void implot3d_plot_triangle(const char* label_id, const double* xs, const double* ys, const double* zs, int count, const implot3d_spec* spec);
void implot3d_plot_quad(const char* label_id, const double* xs, const double* ys, const double* zs, int count, const implot3d_spec* spec);
void implot3d_plot_surface(const char* label_id, const double* xs, const double* ys, const double* zs, int x_count, int y_count, double scale_min, double scale_max, const implot3d_spec* spec);
void implot3d_plot_mesh(const char* label_id, const double* vtx_xs, const double* vtx_ys, const double* vtx_zs, const unsigned int* idxs, int vtx_count, int idx_count, const implot3d_spec* spec);
void implot3d_plot_text(const char* text, double x, double y, double z, double angle, imgui_vec2 pix_offset);
void implot3d_plot_dummy(const char* label_id, const implot3d_spec* spec);

// =========================================================================
// Plot utils
// =========================================================================

imgui_vec2 implot3d_plot_to_pixels_point(implot3d_point point);
imgui_vec2 implot3d_plot_to_pixels_xyz(double x, double y, double z);
implot3d_ray implot3d_pixels_to_plot_ray_vec2(imgui_vec2 pix);
implot3d_ray implot3d_pixels_to_plot_ray_xy(double x, double y);
implot3d_point implot3d_pixels_to_plot_plane_vec2(imgui_vec2 pix, int plane, bool mask);
implot3d_point implot3d_pixels_to_plot_plane_xy(double x, double y, int plane, bool mask);
imgui_vec2 implot3d_get_plot_rect_pos(void);
imgui_vec2 implot3d_get_plot_rect_size(void);
imgui_draw_list* implot3d_get_plot_draw_list(void);

// =========================================================================
// Style
// =========================================================================

void implot3d_get_style(implot3d_style* out);
void implot3d_set_style(const implot3d_style* style);
void implot3d_style_colors_auto(implot3d_style* dst);
void implot3d_style_colors_dark(implot3d_style* dst);
void implot3d_style_colors_light(implot3d_style* dst);
void implot3d_style_colors_classic(implot3d_style* dst);
void implot3d_push_style_color_vec4(int idx, imgui_vec4 col);
void implot3d_push_style_color_u32(int idx, uint32_t col);
void implot3d_pop_style_color(int count);
void implot3d_push_style_var_float(int idx, float val);
void implot3d_push_style_var_int(int idx, int val);
void implot3d_push_style_var_vec2(int idx, imgui_vec2 val);
void implot3d_pop_style_var(int count);
imgui_vec4 implot3d_get_style_color_vec4(int idx);
uint32_t implot3d_get_style_color_u32(int idx);
int implot3d_next_marker(void);

// =========================================================================
// Colormaps
// =========================================================================

int implot3d_add_colormap_vec4(const char* name, const imgui_vec4* cols, int size, bool qual);
int implot3d_add_colormap_u32(const char* name, const uint32_t* cols, int size, bool qual);
int implot3d_get_colormap_count(void);
const char* implot3d_get_colormap_name(int cmap);
int implot3d_get_colormap_index(const char* name);
void implot3d_push_colormap(int cmap);
void implot3d_push_colormap_name(const char* name);
void implot3d_pop_colormap(int count);
imgui_vec4 implot3d_next_colormap_color(void);
int implot3d_get_colormap_size(int cmap);
imgui_vec4 implot3d_get_colormap_color(int idx, int cmap);
imgui_vec4 implot3d_sample_colormap(float t, int cmap);

// =========================================================================
// Demo
// =========================================================================

void implot3d_show_demo_window(bool* p_open);
void implot3d_show_all_demos(void);
void implot3d_show_style_editor(void);
bool implot3d_show_style_selector(const char* label);
bool implot3d_show_colormap_selector(const char* label);
void implot3d_show_metrics_window(bool* p_open);
void implot3d_show_about_window(bool* p_open);

// =========================================================================
// Built-in meshes (sphere / cube / duck)
// =========================================================================

const implot3d_point* implot3d_cube_vtx(void);
const unsigned int* implot3d_cube_idx(void);
const implot3d_point* implot3d_sphere_vtx(void);
const unsigned int* implot3d_sphere_idx(void);
const implot3d_point* implot3d_duck_vtx(void);
const unsigned int* implot3d_duck_idx(void);

int implot3d_cube_vtx_count(void);
int implot3d_cube_idx_count(void);
int implot3d_sphere_vtx_count(void);
int implot3d_sphere_idx_count(void);
int implot3d_duck_vtx_count(void);
int implot3d_duck_idx_count(void);

// =========================================================================
// Point math
// =========================================================================

implot3d_point implot3d_point_make(double x, double y, double z);
implot3d_point implot3d_point_add(implot3d_point a, implot3d_point b);
implot3d_point implot3d_point_sub(implot3d_point a, implot3d_point b);
implot3d_point implot3d_point_mul(implot3d_point a, implot3d_point b);
implot3d_point implot3d_point_div(implot3d_point a, implot3d_point b);
implot3d_point implot3d_point_mul_double(implot3d_point a, double rhs);
implot3d_point implot3d_point_div_double(implot3d_point a, double rhs);
implot3d_point implot3d_point_neg(implot3d_point a);
double implot3d_point_dot(implot3d_point a, implot3d_point b);
implot3d_point implot3d_point_cross(implot3d_point a, implot3d_point b);
double implot3d_point_length(implot3d_point a);
double implot3d_point_length_squared(implot3d_point a);
void implot3d_point_normalize(implot3d_point* a);
implot3d_point implot3d_point_normalized(implot3d_point a);
bool implot3d_point_is_nan(implot3d_point a);
bool implot3d_point_eq(implot3d_point a, implot3d_point b);

// =========================================================================
// Quat math
// =========================================================================

implot3d_quat implot3d_quat_make(double x, double y, double z, double w);
implot3d_quat implot3d_quat_from_angle_axis(double angle, implot3d_point axis);
implot3d_quat implot3d_quat_from_two_vectors(implot3d_point v0, implot3d_point v1);
implot3d_quat implot3d_quat_from_el_az(double elevation, double azimuth);
implot3d_quat implot3d_quat_mul(implot3d_quat a, implot3d_quat b);
implot3d_point implot3d_quat_rotate_point(implot3d_quat q, implot3d_point p);
implot3d_quat implot3d_quat_normalized(implot3d_quat q);
implot3d_quat implot3d_quat_conjugate(implot3d_quat q);
implot3d_quat implot3d_quat_inverse(implot3d_quat q);
double implot3d_quat_length(implot3d_quat q);
void implot3d_quat_normalize(implot3d_quat* q);
double implot3d_quat_dot(implot3d_quat a, implot3d_quat b);
implot3d_quat implot3d_quat_slerp(implot3d_quat q1, implot3d_quat q2, double t);
bool implot3d_quat_eq(implot3d_quat a, implot3d_quat b);

// =========================================================================
// Box / range math
// =========================================================================

void implot3d_box_expand(implot3d_box* box, implot3d_point point);
bool implot3d_box_contains(implot3d_box box, implot3d_point point);
void implot3d_range_expand(implot3d_range* range, double value);
bool implot3d_range_contains(implot3d_range range, double value);
double implot3d_range_size(implot3d_range range);

#ifdef __cplusplus
}
#endif

#endif // IMPLOT3D_C_H