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

#include "implot3d.h"

#include <cmath>
#include <cstring>

#include "implot3d_c.h"

namespace {
    // Builds an ImPlot3DSpec from the C struct, only overriding the fields the
    // caller explicitly set (each *_set flag == 1).
    ImPlot3DSpec to_implot3d_spec(const implot3d_spec* spec) {
        ImPlot3DSpec out;
        if (spec == nullptr) {
            return out;
        }
        if (spec->line_color_set) {
            out.LineColor = ImVec4(spec->line_color[0], spec->line_color[1], spec->line_color[2], spec->line_color[3]);
        }
        if (spec->line_weight_set) {
            out.LineWeight = spec->line_weight;
        }
        if (spec->fill_color_set) {
            out.FillColor = ImVec4(spec->fill_color[0], spec->fill_color[1], spec->fill_color[2], spec->fill_color[3]);
        }
        if (spec->fill_alpha_set) {
            out.FillAlpha = spec->fill_alpha;
        }
        if (spec->marker_set) {
            out.Marker = (ImPlot3DMarker)spec->marker;
        }
        if (spec->marker_size_set) {
            out.MarkerSize = spec->marker_size;
        }
        if (spec->marker_line_color_set) {
            out.MarkerLineColor = ImVec4(spec->marker_line_color[0], spec->marker_line_color[1], spec->marker_line_color[2], spec->marker_line_color[3]);
        }
        if (spec->marker_fill_color_set) {
            out.MarkerFillColor = ImVec4(spec->marker_fill_color[0], spec->marker_fill_color[1], spec->marker_fill_color[2], spec->marker_fill_color[3]);
        }
        if (spec->offset_set) {
            out.Offset = spec->offset;
        }
        if (spec->stride_set) {
            out.Stride = spec->stride;
        }
        if (spec->flags_set) {
            out.Flags = (ImPlot3DItemFlags)spec->flags;
        }
        return out;
    }

    ImPlot3DStyle to_implot3d_style(const implot3d_style* style) {
        ImPlot3DStyle out;
        if (style == nullptr) {
            return out;
        }
        out.LineWeight = style->line_weight;
        out.Marker = style->marker;
        out.MarkerSize = style->marker_size;
        out.FillAlpha = style->fill_alpha;
        out.PlotDefaultSize = ImVec2(style->plot_default_size.x, style->plot_default_size.y);
        out.PlotMinSize = ImVec2(style->plot_min_size.x, style->plot_min_size.y);
        out.PlotPadding = ImVec2(style->plot_padding.x, style->plot_padding.y);
        out.LabelPadding = ImVec2(style->label_padding.x, style->label_padding.y);
        out.ViewScaleFactor = style->view_scale_factor;
        out.LegendPadding = ImVec2(style->legend_padding.x, style->legend_padding.y);
        out.LegendInnerPadding = ImVec2(style->legend_inner_padding.x, style->legend_inner_padding.y);
        out.LegendSpacing = ImVec2(style->legend_spacing.x, style->legend_spacing.y);
        for (int i = 0; i < ImPlot3DCol_COUNT && i < 14; i++) {
            out.Colors[i] = ImVec4(style->colors[i].x, style->colors[i].y, style->colors[i].z, style->colors[i].w);
        }
        out.Colormap = style->colormap;
        return out;
    }

    void from_implot3d_style(implot3d_style* out, const ImPlot3DStyle& style) {
        if (out == nullptr) {
            return;
        }
        out->line_weight = style.LineWeight;
        out->marker = style.Marker;
        out->marker_size = style.MarkerSize;
        out->fill_alpha = style.FillAlpha;
        out->plot_default_size.x = style.PlotDefaultSize.x;
        out->plot_default_size.y = style.PlotDefaultSize.y;
        out->plot_min_size.x = style.PlotMinSize.x;
        out->plot_min_size.y = style.PlotMinSize.y;
        out->plot_padding.x = style.PlotPadding.x;
        out->plot_padding.y = style.PlotPadding.y;
        out->label_padding.x = style.LabelPadding.x;
        out->label_padding.y = style.LabelPadding.y;
        out->view_scale_factor = style.ViewScaleFactor;
        out->legend_padding.x = style.LegendPadding.x;
        out->legend_padding.y = style.LegendPadding.y;
        out->legend_inner_padding.x = style.LegendInnerPadding.x;
        out->legend_inner_padding.y = style.LegendInnerPadding.y;
        out->legend_spacing.x = style.LegendSpacing.x;
        out->legend_spacing.y = style.LegendSpacing.y;
        for (int i = 0; i < ImPlot3DCol_COUNT && i < 14; i++) {
            out->colors[i].x = style.Colors[i].x;
            out->colors[i].y = style.Colors[i].y;
            out->colors[i].z = style.Colors[i].z;
            out->colors[i].w = style.Colors[i].w;
        }
        out->colormap = style.Colormap;
    }

    implot3d_point to_point(const ImPlot3DPoint& p) {
        implot3d_point out;
        out.x = p.x;
        out.y = p.y;
        out.z = p.z;
        return out;
    }

    ImPlot3DPoint from_point(implot3d_point p) {
        return ImPlot3DPoint(p.x, p.y, p.z);
    }

    implot3d_quat to_quat(const ImPlot3DQuat& q) {
        implot3d_quat out;
        out.x = q.x;
        out.y = q.y;
        out.z = q.z;
        out.w = q.w;
        return out;
    }

    ImPlot3DQuat from_quat(implot3d_quat q) {
        return ImPlot3DQuat(q.x, q.y, q.z, q.w);
    }
}

extern "C" {

// =========================================================================
// Context
// =========================================================================

implot3d_context* implot3d_create_context(void) {
    return (implot3d_context*)ImPlot3D::CreateContext();
}

void implot3d_destroy_context(implot3d_context* ctx) {
    ImPlot3D::DestroyContext((ImPlot3DContext*)ctx);
}

implot3d_context* implot3d_get_current_context(void) {
    return (implot3d_context*)ImPlot3D::GetCurrentContext();
}

void implot3d_set_current_context(implot3d_context* ctx) {
    ImPlot3D::SetCurrentContext((ImPlot3DContext*)ctx);
}

// =========================================================================
// Begin/End plot
// =========================================================================

bool implot3d_begin_plot(const char* title_id, imgui_vec2 size, int flags) {
    return ImPlot3D::BeginPlot(title_id, ImVec2(size.x, size.y), (ImPlot3DFlags)flags);
}

void implot3d_end_plot(void) {
    ImPlot3D::EndPlot();
}

// =========================================================================
// Setup
// =========================================================================

void implot3d_setup_axis(int axis, const char* label, int flags) {
    ImPlot3D::SetupAxis((ImAxis3D)axis, label, (ImPlot3DAxisFlags)flags);
}

void implot3d_setup_axis_limits(int axis, double v_min, double v_max, int cond) {
    ImPlot3D::SetupAxisLimits((ImAxis3D)axis, v_min, v_max, (ImPlot3DCond)cond);
}

void implot3d_setup_axis_ticks_values(int axis, const double* values, int n_ticks, const char* const* labels, bool keep_default) {
    ImPlot3D::SetupAxisTicks((ImAxis3D)axis, values, n_ticks, labels, keep_default);
}

void implot3d_setup_axis_ticks_limits(int axis, double v_min, double v_max, int n_ticks, const char* const* labels, bool keep_default) {
    ImPlot3D::SetupAxisTicks((ImAxis3D)axis, v_min, v_max, n_ticks, labels, keep_default);
}

void implot3d_setup_axis_scale(int axis, int scale) {
    ImPlot3D::SetupAxisScale((ImAxis3D)axis, (ImPlot3DScale)scale);
}

void implot3d_setup_axis_limits_constraints(int axis, double v_min, double v_max) {
    ImPlot3D::SetupAxisLimitsConstraints((ImAxis3D)axis, v_min, v_max);
}

void implot3d_setup_axis_zoom_constraints(int axis, double zoom_min, double zoom_max) {
    ImPlot3D::SetupAxisZoomConstraints((ImAxis3D)axis, zoom_min, zoom_max);
}

void implot3d_setup_axes(const char* x_label, const char* y_label, const char* z_label, int x_flags, int y_flags, int z_flags) {
    ImPlot3D::SetupAxes(x_label, y_label, z_label, (ImPlot3DAxisFlags)x_flags, (ImPlot3DAxisFlags)y_flags, (ImPlot3DAxisFlags)z_flags);
}

void implot3d_setup_axes_limits(double x_min, double x_max, double y_min, double y_max, double z_min, double z_max, int cond) {
    ImPlot3D::SetupAxesLimits(x_min, x_max, y_min, y_max, z_min, z_max, (ImPlot3DCond)cond);
}

void implot3d_setup_box_rotation_angles(double elevation, double azimuth, bool animate, int cond) {
    ImPlot3D::SetupBoxRotation(elevation, azimuth, animate, (ImPlot3DCond)cond);
}

void implot3d_setup_box_rotation_quat(implot3d_quat rotation, bool animate, int cond) {
    ImPlot3D::SetupBoxRotation(from_quat(rotation), animate, (ImPlot3DCond)cond);
}

void implot3d_setup_box_initial_rotation_angles(double elevation, double azimuth) {
    ImPlot3D::SetupBoxInitialRotation(elevation, azimuth);
}

void implot3d_setup_box_initial_rotation_quat(implot3d_quat rotation) {
    ImPlot3D::SetupBoxInitialRotation(from_quat(rotation));
}

void implot3d_setup_box_scale(double x, double y, double z) {
    ImPlot3D::SetupBoxScale(x, y, z);
}

void implot3d_setup_legend(int location, int flags) {
    ImPlot3D::SetupLegend((ImPlot3DLocation)location, (ImPlot3DLegendFlags)flags);
}

// =========================================================================
// Plot items (double data arrays)
// =========================================================================

void implot3d_plot_scatter(const char* label_id, const double* xs, const double* ys, const double* zs, int count, const implot3d_spec* spec) {
    ImPlot3D::PlotScatter<double>(label_id, xs, ys, zs, count, to_implot3d_spec(spec));
}

void implot3d_plot_line(const char* label_id, const double* xs, const double* ys, const double* zs, int count, const implot3d_spec* spec) {
    ImPlot3D::PlotLine<double>(label_id, xs, ys, zs, count, to_implot3d_spec(spec));
}

void implot3d_plot_triangle(const char* label_id, const double* xs, const double* ys, const double* zs, int count, const implot3d_spec* spec) {
    ImPlot3D::PlotTriangle<double>(label_id, xs, ys, zs, count, to_implot3d_spec(spec));
}

void implot3d_plot_quad(const char* label_id, const double* xs, const double* ys, const double* zs, int count, const implot3d_spec* spec) {
    ImPlot3D::PlotQuad<double>(label_id, xs, ys, zs, count, to_implot3d_spec(spec));
}

void implot3d_plot_surface(const char* label_id, const double* xs, const double* ys, const double* zs, int x_count, int y_count, double scale_min, double scale_max, const implot3d_spec* spec) {
    ImPlot3D::PlotSurface<double>(label_id, xs, ys, zs, x_count, y_count, scale_min, scale_max, to_implot3d_spec(spec));
}

void implot3d_plot_mesh(const char* label_id, const double* vtx_xs, const double* vtx_ys, const double* vtx_zs, const unsigned int* idxs, int vtx_count, int idx_count, const implot3d_spec* spec) {
    ImPlot3D::PlotMesh<double>(label_id, vtx_xs, vtx_ys, vtx_zs, idxs, vtx_count, idx_count, to_implot3d_spec(spec));
}

void implot3d_plot_text(const char* text, double x, double y, double z, double angle, imgui_vec2 pix_offset) {
    ImPlot3D::PlotText(text, x, y, z, angle, ImVec2(pix_offset.x, pix_offset.y));
}

void implot3d_plot_dummy(const char* label_id, const implot3d_spec* spec) {
    ImPlot3D::PlotDummy(label_id, to_implot3d_spec(spec));
}

// =========================================================================
// Plot utils
// =========================================================================

imgui_vec2 implot3d_plot_to_pixels_point(implot3d_point point) {
    const ImVec2 v = ImPlot3D::PlotToPixels(from_point(point));
    imgui_vec2 out = {v.x, v.y};
    return out;
}

imgui_vec2 implot3d_plot_to_pixels_xyz(double x, double y, double z) {
    const ImVec2 v = ImPlot3D::PlotToPixels(x, y, z);
    imgui_vec2 out = {v.x, v.y};
    return out;
}

implot3d_ray implot3d_pixels_to_plot_ray_vec2(imgui_vec2 pix) {
    const ImPlot3DRay r = ImPlot3D::PixelsToPlotRay(ImVec2(pix.x, pix.y));
    implot3d_ray out;
    out.origin = to_point(r.Origin);
    out.direction = to_point(r.Direction);
    return out;
}

implot3d_ray implot3d_pixels_to_plot_ray_xy(double x, double y) {
    const ImPlot3DRay r = ImPlot3D::PixelsToPlotRay(x, y);
    implot3d_ray out;
    out.origin = to_point(r.Origin);
    out.direction = to_point(r.Direction);
    return out;
}

implot3d_point implot3d_pixels_to_plot_plane_vec2(imgui_vec2 pix, int plane, bool mask) {
    return to_point(ImPlot3D::PixelsToPlotPlane(ImVec2(pix.x, pix.y), (ImPlane3D)plane, mask));
}

implot3d_point implot3d_pixels_to_plot_plane_xy(double x, double y, int plane, bool mask) {
    return to_point(ImPlot3D::PixelsToPlotPlane(x, y, (ImPlane3D)plane, mask));
}

imgui_vec2 implot3d_get_plot_rect_pos(void) {
    const ImVec2 v = ImPlot3D::GetPlotRectPos();
    imgui_vec2 out = {v.x, v.y};
    return out;
}

imgui_vec2 implot3d_get_plot_rect_size(void) {
    const ImVec2 v = ImPlot3D::GetPlotRectSize();
    imgui_vec2 out = {v.x, v.y};
    return out;
}

imgui_draw_list* implot3d_get_plot_draw_list(void) {
    return (imgui_draw_list*)ImPlot3D::GetPlotDrawList();
}

// =========================================================================
// Style
// =========================================================================

void implot3d_get_style(implot3d_style* out) {
    from_implot3d_style(out, ImPlot3D::GetStyle());
}

void implot3d_set_style(const implot3d_style* style) {
    ImPlot3D::SetStyle(to_implot3d_style(style));
}

void implot3d_style_colors_auto(implot3d_style* dst) {
    ImPlot3DStyle s = dst != nullptr ? to_implot3d_style(dst) : ImPlot3DStyle();
    ImPlot3D::StyleColorsAuto(dst != nullptr ? &s : nullptr);
    if (dst != nullptr) {
        from_implot3d_style(dst, s);
    }
}

void implot3d_style_colors_dark(implot3d_style* dst) {
    ImPlot3DStyle s = dst != nullptr ? to_implot3d_style(dst) : ImPlot3DStyle();
    ImPlot3D::StyleColorsDark(dst != nullptr ? &s : nullptr);
    if (dst != nullptr) {
        from_implot3d_style(dst, s);
    }
}

void implot3d_style_colors_light(implot3d_style* dst) {
    ImPlot3DStyle s = dst != nullptr ? to_implot3d_style(dst) : ImPlot3DStyle();
    ImPlot3D::StyleColorsLight(dst != nullptr ? &s : nullptr);
    if (dst != nullptr) {
        from_implot3d_style(dst, s);
    }
}

void implot3d_style_colors_classic(implot3d_style* dst) {
    ImPlot3DStyle s = dst != nullptr ? to_implot3d_style(dst) : ImPlot3DStyle();
    ImPlot3D::StyleColorsClassic(dst != nullptr ? &s : nullptr);
    if (dst != nullptr) {
        from_implot3d_style(dst, s);
    }
}

void implot3d_push_style_color_vec4(int idx, imgui_vec4 col) {
    ImPlot3D::PushStyleColor((ImPlot3DCol)idx, ImVec4(col.x, col.y, col.z, col.w));
}

void implot3d_push_style_color_u32(int idx, uint32_t col) {
    ImPlot3D::PushStyleColor((ImPlot3DCol)idx, col);
}

void implot3d_pop_style_color(int count) {
    ImPlot3D::PopStyleColor(count);
}

void implot3d_push_style_var_float(int idx, float val) {
    ImPlot3D::PushStyleVar((ImPlot3DStyleVar)idx, val);
}

void implot3d_push_style_var_int(int idx, int val) {
    ImPlot3D::PushStyleVar((ImPlot3DStyleVar)idx, val);
}

void implot3d_push_style_var_vec2(int idx, imgui_vec2 val) {
    ImPlot3D::PushStyleVar((ImPlot3DStyleVar)idx, ImVec2(val.x, val.y));
}

void implot3d_pop_style_var(int count) {
    ImPlot3D::PopStyleVar(count);
}

imgui_vec4 implot3d_get_style_color_vec4(int idx) {
    const ImVec4 v = ImPlot3D::GetStyleColorVec4((ImPlot3DCol)idx);
    imgui_vec4 out = {v.x, v.y, v.z, v.w};
    return out;
}

uint32_t implot3d_get_style_color_u32(int idx) {
    return ImPlot3D::GetStyleColorU32((ImPlot3DCol)idx);
}

int implot3d_next_marker(void) {
    return (int)ImPlot3D::NextMarker();
}

// =========================================================================
// Colormaps
// =========================================================================

int implot3d_add_colormap_vec4(const char* name, const imgui_vec4* cols, int size, bool qual) {
    // No std::vector: libimgui.a is linked into Kotlin/Native executables
    // which cannot resolve C++ runtime symbols. Colormaps are capped at 256
    // entries (the library's own GetColormapSize/qualitative constraints do
    // not apply here; a generous fixed buffer covers all practical uses).
    ImVec4 c[256];
    if (cols != nullptr) {
        for (int i = 0; i < size && i < 256; i++) {
            c[i] = ImVec4(cols[i].x, cols[i].y, cols[i].z, cols[i].w);
        }
        return (int)ImPlot3D::AddColormap(name, c, size < 256 ? size : 256, qual);
    }
    return (int)ImPlot3D::AddColormap(name, (const ImVec4*)nullptr, size, qual);
}

int implot3d_add_colormap_u32(const char* name, const uint32_t* cols, int size, bool qual) {
    return (int)ImPlot3D::AddColormap(name, (const ImU32*)cols, size, qual);
}

int implot3d_get_colormap_count(void) {
    return ImPlot3D::GetColormapCount();
}

const char* implot3d_get_colormap_name(int cmap) {
    return ImPlot3D::GetColormapName((ImPlot3DColormap)cmap);
}

int implot3d_get_colormap_index(const char* name) {
    return (int)ImPlot3D::GetColormapIndex(name);
}

void implot3d_push_colormap(int cmap) {
    ImPlot3D::PushColormap((ImPlot3DColormap)cmap);
}

void implot3d_push_colormap_name(const char* name) {
    ImPlot3D::PushColormap(name);
}

void implot3d_pop_colormap(int count) {
    ImPlot3D::PopColormap(count);
}

imgui_vec4 implot3d_next_colormap_color(void) {
    const ImVec4 v = ImPlot3D::NextColormapColor();
    imgui_vec4 out = {v.x, v.y, v.z, v.w};
    return out;
}

int implot3d_get_colormap_size(int cmap) {
    return ImPlot3D::GetColormapSize((ImPlot3DColormap)cmap);
}

imgui_vec4 implot3d_get_colormap_color(int idx, int cmap) {
    const ImVec4 v = ImPlot3D::GetColormapColor(idx, (ImPlot3DColormap)cmap);
    imgui_vec4 out = {v.x, v.y, v.z, v.w};
    return out;
}

imgui_vec4 implot3d_sample_colormap(float t, int cmap) {
    const ImVec4 v = ImPlot3D::SampleColormap(t, (ImPlot3DColormap)cmap);
    imgui_vec4 out = {v.x, v.y, v.z, v.w};
    return out;
}

// =========================================================================
// Demo
// =========================================================================

void implot3d_show_demo_window(bool* p_open) {
    ImPlot3D::ShowDemoWindow(p_open);
}

void implot3d_show_all_demos(void) {
    ImPlot3D::ShowAllDemos();
}

void implot3d_show_style_editor(void) {
    ImPlot3D::ShowStyleEditor();
}

bool implot3d_show_style_selector(const char* label) {
    return ImPlot3D::ShowStyleSelector(label);
}

bool implot3d_show_colormap_selector(const char* label) {
    return ImPlot3D::ShowColormapSelector(label);
}

void implot3d_show_metrics_window(bool* p_open) {
    ImPlot3D::ShowMetricsWindow(p_open);
}

void implot3d_show_about_window(bool* p_open) {
    ImPlot3D::ShowAboutWindow(p_open);
}

// =========================================================================
// Built-in meshes (sphere / cube / duck)
// =========================================================================

const implot3d_point* implot3d_cube_vtx(void) {
    return (const implot3d_point*)ImPlot3D::cube_vtx;
}

const unsigned int* implot3d_cube_idx(void) {
    return ImPlot3D::cube_idx;
}

const implot3d_point* implot3d_sphere_vtx(void) {
    return (const implot3d_point*)ImPlot3D::sphere_vtx;
}

const unsigned int* implot3d_sphere_idx(void) {
    return ImPlot3D::sphere_idx;
}

const implot3d_point* implot3d_duck_vtx(void) {
    return (const implot3d_point*)ImPlot3D::duck_vtx;
}

const unsigned int* implot3d_duck_idx(void) {
    return ImPlot3D::duck_idx;
}

int implot3d_cube_vtx_count(void) {
    return ImPlot3D::CUBE_VTX_COUNT;
}

int implot3d_cube_idx_count(void) {
    return ImPlot3D::CUBE_IDX_COUNT;
}

int implot3d_sphere_vtx_count(void) {
    return ImPlot3D::SPHERE_VTX_COUNT;
}

int implot3d_sphere_idx_count(void) {
    return ImPlot3D::SPHERE_IDX_COUNT;
}

int implot3d_duck_vtx_count(void) {
    return ImPlot3D::DUCK_VTX_COUNT;
}

int implot3d_duck_idx_count(void) {
    return ImPlot3D::DUCK_IDX_COUNT;
}

// =========================================================================
// Point math
// =========================================================================

implot3d_point implot3d_point_make(double x, double y, double z) {
    return to_point(ImPlot3DPoint(x, y, z));
}

implot3d_point implot3d_point_add(implot3d_point a, implot3d_point b) {
    return to_point(from_point(a) + from_point(b));
}

implot3d_point implot3d_point_sub(implot3d_point a, implot3d_point b) {
    return to_point(from_point(a) - from_point(b));
}

implot3d_point implot3d_point_mul(implot3d_point a, implot3d_point b) {
    return to_point(from_point(a) * from_point(b));
}

implot3d_point implot3d_point_div(implot3d_point a, implot3d_point b) {
    return to_point(from_point(a) / from_point(b));
}

implot3d_point implot3d_point_mul_double(implot3d_point a, double rhs) {
    return to_point(from_point(a) * rhs);
}

implot3d_point implot3d_point_div_double(implot3d_point a, double rhs) {
    return to_point(from_point(a) / rhs);
}

implot3d_point implot3d_point_neg(implot3d_point a) {
    return to_point(-from_point(a));
}

double implot3d_point_dot(implot3d_point a, implot3d_point b) {
    return from_point(a).Dot(from_point(b));
}

implot3d_point implot3d_point_cross(implot3d_point a, implot3d_point b) {
    return to_point(from_point(a).Cross(from_point(b)));
}

double implot3d_point_length(implot3d_point a) {
    return from_point(a).Length();
}

double implot3d_point_length_squared(implot3d_point a) {
    return from_point(a).LengthSquared();
}

void implot3d_point_normalize(implot3d_point* a) {
    ImPlot3DPoint p = from_point(*a);
    p.Normalize();
    *a = to_point(p);
}

implot3d_point implot3d_point_normalized(implot3d_point a) {
    return to_point(from_point(a).Normalized());
}

bool implot3d_point_is_nan(implot3d_point a) {
    return from_point(a).IsNaN();
}

bool implot3d_point_eq(implot3d_point a, implot3d_point b) {
    return from_point(a) == from_point(b);
}

// =========================================================================
// Quat math
// =========================================================================

implot3d_quat implot3d_quat_make(double x, double y, double z, double w) {
    return to_quat(ImPlot3DQuat(x, y, z, w));
}

implot3d_quat implot3d_quat_from_angle_axis(double angle, implot3d_point axis) {
    return to_quat(ImPlot3DQuat(angle, from_point(axis)));
}

implot3d_quat implot3d_quat_from_two_vectors(implot3d_point v0, implot3d_point v1) {
    return to_quat(ImPlot3DQuat::FromTwoVectors(from_point(v0), from_point(v1)));
}

implot3d_quat implot3d_quat_from_el_az(double elevation, double azimuth) {
    return to_quat(ImPlot3DQuat::FromElAz(elevation, azimuth));
}

implot3d_quat implot3d_quat_mul(implot3d_quat a, implot3d_quat b) {
    return to_quat(from_quat(a) * from_quat(b));
}

implot3d_point implot3d_quat_rotate_point(implot3d_quat q, implot3d_point p) {
    return to_point(from_quat(q) * from_point(p));
}

implot3d_quat implot3d_quat_normalized(implot3d_quat q) {
    return to_quat(from_quat(q).Normalized());
}

implot3d_quat implot3d_quat_conjugate(implot3d_quat q) {
    return to_quat(from_quat(q).Conjugate());
}

implot3d_quat implot3d_quat_inverse(implot3d_quat q) {
    return to_quat(from_quat(q).Inverse());
}

double implot3d_quat_length(implot3d_quat q) {
    return from_quat(q).Length();
}

void implot3d_quat_normalize(implot3d_quat* q) {
    ImPlot3DQuat qu = from_quat(*q);
    qu.Normalize();
    *q = to_quat(qu);
}

double implot3d_quat_dot(implot3d_quat a, implot3d_quat b) {
    return from_quat(a).Dot(from_quat(b));
}

implot3d_quat implot3d_quat_slerp(implot3d_quat q1, implot3d_quat q2, double t) {
    return to_quat(ImPlot3DQuat::Slerp(from_quat(q1), from_quat(q2), t));
}

bool implot3d_quat_eq(implot3d_quat a, implot3d_quat b) {
    return from_quat(a) == from_quat(b);
}

// =========================================================================
// Box / range math
// =========================================================================

void implot3d_box_expand(implot3d_box* box, implot3d_point point) {
    ImPlot3DBox b(from_point(box->min), from_point(box->max));
    b.Expand(from_point(point));
    box->min = to_point(b.Min);
    box->max = to_point(b.Max);
}

bool implot3d_box_contains(implot3d_box box, implot3d_point point) {
    return ImPlot3DBox(from_point(box.min), from_point(box.max)).Contains(from_point(point));
}

void implot3d_range_expand(implot3d_range* range, double value) {
    ImPlot3DRange r(range->min, range->max);
    r.Expand(value);
    range->min = r.Min;
    range->max = r.Max;
}

bool implot3d_range_contains(implot3d_range range, double value) {
    return ImPlot3DRange(range.min, range.max).Contains(value);
}

double implot3d_range_size(implot3d_range range) {
    return ImPlot3DRange(range.min, range.max).Size();
}

} // extern "C"