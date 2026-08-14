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

#include "implot.h"

#include <cstdlib>
#include <cstring>

#include "implot_c.h"

namespace {
    // Builds an ImPlotSpec from the C struct, only overriding the fields the
    // caller explicitly set (each *_set flag == 1).
    ImPlotSpec to_implot_spec(const implot_spec* spec) {
        ImPlotSpec out;
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
            out.Marker = (ImPlotMarker)spec->marker;
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
        if (spec->size_set) {
            out.Size = spec->size;
        }
        if (spec->offset_set) {
            out.Offset = spec->offset;
        }
        if (spec->stride_set) {
            out.Stride = spec->stride;
        }
        if (spec->flags_set) {
            out.Flags = (ImPlotItemFlags)spec->flags;
        }
        return out;
    }
}

extern "C" {

// =========================================================================
// Contexts
// =========================================================================

implot_context* implot_create_context(void) {
    return (implot_context*)ImPlot::CreateContext();
}

void implot_destroy_context(implot_context* ctx) {
    ImPlot::DestroyContext((ImPlotContext*)ctx);
}

implot_context* implot_get_current_context(void) {
    return (implot_context*)ImPlot::GetCurrentContext();
}

void implot_set_im_gui_context(imgui_context* ctx) {
    ImPlot::SetImGuiContext((ImGuiContext*)ctx);
}

void implot_show_demo_window(bool* p_open) {
    ImPlot::ShowDemoWindow(p_open);
}

// =========================================================================
// Begin/End plot + setup
// =========================================================================

bool implot_begin_plot(const char* title_id, imgui_vec2 size, int flags) {
    return ImPlot::BeginPlot(title_id, ImVec2(size.x, size.y), flags);
}

void implot_end_plot(void) {
    ImPlot::EndPlot();
}

void implot_setup_axes(const char* x_label, const char* y_label, int x_flags, int y_flags) {
    ImPlot::SetupAxes(x_label, y_label, x_flags, y_flags);
}

void implot_setup_axes_limits(double x_min, double x_max, double y_min, double y_max, int cond) {
    ImPlot::SetupAxesLimits(x_min, x_max, y_min, y_max, (ImPlotCond)cond);
}

void implot_setup_axis_limits(int axis, double v_min, double v_max, int cond) {
    ImPlot::SetupAxisLimits((ImAxis)axis, v_min, v_max, (ImPlotCond)cond);
}

void implot_setup_legend(int location, int flags) {
    ImPlot::SetupLegend((ImPlotLocation)location, flags);
}

void implot_setup_finish(void) {
    ImPlot::SetupFinish();
}

void implot_setup_axis(int axis, const char* label, int flags) {
    ImPlot::SetupAxis((ImAxis)axis, label, flags);
}

void implot_setup_axis_format(int axis, const char* fmt) {
    ImPlot::SetupAxisFormat((ImAxis)axis, fmt);
}

void implot_setup_axis_limits_constraints(int axis, double v_min, double v_max) {
    ImPlot::SetupAxisLimitsConstraints((ImAxis)axis, v_min, v_max);
}

void implot_setup_axis_zoom_constraints(int axis, double z_min, double z_max) {
    ImPlot::SetupAxisZoomConstraints((ImAxis)axis, z_min, z_max);
}

void implot_setup_axis_links(int axis, double* link_min, double* link_max) {
    // ImPlot holds these pointers until EndPlot(), so the buffers must outlive
    // the setup call. Persist them per-axis in C buffers (no STL: Kotlin/Native
    // static linking can't resolve libstdc++ runtime symbols).
    static double buffers[8][2];  // ImAxis has 8 axes (X1..X4, Y1..Y4)
    double* buf = buffers[axis & 7];
    if (link_min != nullptr) {
        buf[0] = link_min[0];
    }
    if (link_max != nullptr) {
        buf[1] = link_max[0];
    }
    double* min_ptr = link_min != nullptr ? &buf[0] : nullptr;
    double* max_ptr = link_max != nullptr ? &buf[1] : nullptr;
    ImPlot::SetupAxisLinks((ImAxis)axis, min_ptr, max_ptr);
}

void implot_setup_axis_scale(int axis, int scale) {
    ImPlot::SetupAxisScale((ImAxis)axis, (ImPlotScale)scale);
}

void implot_setup_axis_ticks(int axis, const double* values, int tick_count, const char* const* labels, bool keep_default) {
    ImPlot::SetupAxisTicks((ImAxis)axis, values, tick_count, labels, keep_default);
}

void implot_setup_mouse_text(int location, int flags) {
    ImPlot::SetupMouseText((ImPlotLocation)location, flags);
}

// =========================================================================
// SetNext
// =========================================================================

void implot_set_next_axes_limits(double x_min, double x_max, double y_min, double y_max, int cond) {
    ImPlot::SetNextAxesLimits(x_min, x_max, y_min, y_max, (ImPlotCond)cond);
}

void implot_set_next_axis_limits(int axis, double v_min, double v_max, int cond) {
    ImPlot::SetNextAxisLimits((ImAxis)axis, v_min, v_max, (ImPlotCond)cond);
}

// =========================================================================
// Plot items
// =========================================================================

void implot_plot_line(const char* label_id, const float* xs, const float* ys, int count, const implot_spec* spec) {
    ImPlot::PlotLine(label_id, xs, ys, count, to_implot_spec(spec));
}

void implot_plot_line_values(const char* label_id, const float* values, int count, double xscale, double xstart, const implot_spec* spec) {
    ImPlot::PlotLine(label_id, values, count, xscale, xstart, to_implot_spec(spec));
}

void implot_plot_scatter(const char* label_id, const float* xs, const float* ys, int count, const implot_spec* spec) {
    ImPlot::PlotScatter(label_id, xs, ys, count, to_implot_spec(spec));
}

void implot_plot_scatter_values(const char* label_id, const float* values, int count, double xscale, double xstart, const implot_spec* spec) {
    ImPlot::PlotScatter(label_id, values, count, xscale, xstart, to_implot_spec(spec));
}

void implot_plot_stairs(const char* label_id, const float* xs, const float* ys, int count, const implot_spec* spec) {
    ImPlot::PlotStairs(label_id, xs, ys, count, to_implot_spec(spec));
}

void implot_plot_bars(const char* label_id, const float* xs, const float* ys, int count, double bar_size, const implot_spec* spec) {
    ImPlot::PlotBars(label_id, xs, ys, count, bar_size, to_implot_spec(spec));
}

void implot_plot_bars_values(const char* label_id, const float* values, int count, double bar_size, double shift, const implot_spec* spec) {
    ImPlot::PlotBars(label_id, values, count, bar_size, shift, to_implot_spec(spec));
}

double implot_plot_histogram(const char* label_id, const float* values, int count, int bins, double bar_scale, double range_min, double range_max, const implot_spec* spec) {
    ImPlotRange range = (range_min == 0.0 && range_max == 0.0) ? ImPlotRange() : ImPlotRange(range_min, range_max);
    return ImPlot::PlotHistogram(label_id, values, count, bins, bar_scale, range, to_implot_spec(spec));
}

void implot_plot_inf_lines(const char* label_id, const float* values, int count, const implot_spec* spec) {
    ImPlot::PlotInfLines(label_id, values, count, to_implot_spec(spec));
}

void implot_plot_shaded(const char* label_id, const float* xs, const float* ys, int count, double yref, const implot_spec* spec) {
    ImPlot::PlotShaded(label_id, xs, ys, count, yref, to_implot_spec(spec));
}

void implot_plot_text(const char* text, double x, double y, imgui_vec2 pix_offset) {
    ImPlot::PlotText(text, x, y, ImVec2(pix_offset.x, pix_offset.y));
}

void implot_plot_dummy(const char* label_id, const implot_spec* spec) {
    ImPlot::PlotDummy(label_id, to_implot_spec(spec));
}

void implot_plot_bar_groups(const char* const* label_ids, const float* values, int item_count, int group_count, double group_size, double shift, const implot_spec* spec) {
    ImPlot::PlotBarGroups(label_ids, values, item_count, group_count, group_size, shift, to_implot_spec(spec));
}

void implot_plot_error_bars(const char* label_id, const float* xs, const float* ys, const float* neg, const float* pos, int count, const implot_spec* spec) {
    ImPlot::PlotErrorBars(label_id, xs, ys, neg, pos, count, to_implot_spec(spec));
}

void implot_plot_stems(const char* label_id, const float* xs, const float* ys, int count, double ref, const implot_spec* spec) {
    ImPlot::PlotStems(label_id, xs, ys, count, ref, to_implot_spec(spec));
}

void implot_plot_heatmap(const char* label_id, const float* values, int rows, int cols, double scale_min, double scale_max, const char* label_fmt, double bounds_x_min, double bounds_y_min, double bounds_x_max, double bounds_y_max, const implot_spec* spec) {
    ImPlot::PlotHeatmap(label_id, values, rows, cols, scale_min, scale_max, label_fmt, ImPlotPoint(bounds_x_min, bounds_y_min), ImPlotPoint(bounds_x_max, bounds_y_max), to_implot_spec(spec));
}

double implot_plot_histogram_2d(const char* label_id, const float* xs, const float* ys, int count, int x_bins, int y_bins, double range_x_min, double range_x_max, double range_y_min, double range_y_max, const implot_spec* spec) {
    ImPlotRect range = (range_x_min == 0.0 && range_x_max == 0.0 && range_y_min == 0.0 && range_y_max == 0.0) ? ImPlotRect() : ImPlotRect(range_x_min, range_x_max, range_y_min, range_y_max);
    return ImPlot::PlotHistogram2D(label_id, xs, ys, count, x_bins, y_bins, range, to_implot_spec(spec));
}

void implot_plot_digital(const char* label_id, const float* xs, const float* ys, int count, const implot_spec* spec) {
    ImPlot::PlotDigital(label_id, xs, ys, count, to_implot_spec(spec));
}

void implot_plot_pie_chart(const char* const* label_ids, const float* values, int count, double x, double y, double radius, const char* label_fmt, double angle0, const implot_spec* spec) {
    ImPlot::PlotPieChart(label_ids, values, count, x, y, radius, label_fmt, angle0, to_implot_spec(spec));
}

void implot_plot_bubbles(const char* label_id, const float* xs, const float* ys, const float* sizes, int count, const implot_spec* spec) {
    ImPlot::PlotBubbles(label_id, xs, ys, sizes, count, to_implot_spec(spec));
}

void implot_plot_polygon(const char* label_id, const float* xs, const float* ys, int count, const implot_spec* spec) {
    ImPlot::PlotPolygon(label_id, xs, ys, count, to_implot_spec(spec));
}

void implot_plot_image(const char* label_id, uint64_t tex_id, double x_min, double y_min, double x_max, double y_max, imgui_vec2 uv_min, imgui_vec2 uv_max, imgui_vec4 tint_col, const implot_spec* spec) {
    ImPlot::PlotImage(label_id, (ImTextureID)tex_id, ImPlotPoint(x_min, y_min), ImPlotPoint(x_max, y_max), ImVec2(uv_min.x, uv_min.y), ImVec2(uv_max.x, uv_max.y), ImVec4(tint_col.x, tint_col.y, tint_col.z, tint_col.w), to_implot_spec(spec));
}

// =========================================================================
// Subplots
// =========================================================================

bool implot_begin_subplots(const char* title_id, int rows, int cols, imgui_vec2 size, int flags) {
    return ImPlot::BeginSubplots(title_id, rows, cols, ImVec2(size.x, size.y), (ImPlotSubplotFlags)flags);
}

void implot_end_subplots(void) {
    ImPlot::EndSubplots();
}

// =========================================================================
// Drag tools / annotations / tags
// =========================================================================

bool implot_drag_point(int id, double* x, double* y, imgui_vec4 col, float size, int flags) {
    return ImPlot::DragPoint(id, x, y, ImVec4(col.x, col.y, col.z, col.w), size, (ImPlotDragToolFlags)flags);
}

bool implot_drag_line_x(int id, double* x, imgui_vec4 col, float thickness, int flags) {
    return ImPlot::DragLineX(id, x, ImVec4(col.x, col.y, col.z, col.w), thickness, (ImPlotDragToolFlags)flags);
}

bool implot_drag_line_y(int id, double* y, imgui_vec4 col, float thickness, int flags) {
    return ImPlot::DragLineY(id, y, ImVec4(col.x, col.y, col.z, col.w), thickness, (ImPlotDragToolFlags)flags);
}

bool implot_drag_rect(int id, double* x_min, double* y_min, double* x_max, double* y_max, imgui_vec4 col, int flags) {
    return ImPlot::DragRect(id, x_min, y_min, x_max, y_max, ImVec4(col.x, col.y, col.z, col.w), (ImPlotDragToolFlags)flags);
}

void implot_annotation(double x, double y, imgui_vec4 col, imgui_vec2 pix_offset, bool clamp, bool round, const char* fmt) {
    ImVec4 c(col.x, col.y, col.z, col.w);
    ImVec2 off(pix_offset.x, pix_offset.y);
    if (fmt == nullptr) {
        ImPlot::Annotation(x, y, c, off, clamp, round);
    } else {
        ImPlot::Annotation(x, y, c, off, clamp, "%s", fmt);
    }
}

void implot_tag_x(double x, imgui_vec4 col, bool round, const char* fmt) {
    ImVec4 c(col.x, col.y, col.z, col.w);
    if (fmt == nullptr) {
        ImPlot::TagX(x, c, round);
    } else {
        ImPlot::TagX(x, c, "%s", fmt);
    }
}

void implot_tag_y(double y, imgui_vec4 col, bool round, const char* fmt) {
    ImVec4 c(col.x, col.y, col.z, col.w);
    if (fmt == nullptr) {
        ImPlot::TagY(y, c, round);
    } else {
        ImPlot::TagY(y, c, "%s", fmt);
    }
}

// =========================================================================
// Style
// =========================================================================

void implot_push_style_color_vec4(int idx, imgui_vec4 color) {
    ImPlot::PushStyleColor((ImPlotCol)idx, ImVec4(color.x, color.y, color.z, color.w));
}

void implot_push_style_color_u32(int idx, uint32_t color) {
    ImPlot::PushStyleColor((ImPlotCol)idx, color);
}

void implot_pop_style_color(int count) {
    ImPlot::PopStyleColor(count);
}

void implot_push_style_var_float(int idx, float val) {
    ImPlot::PushStyleVar((ImPlotStyleVar)idx, val);
}

void implot_push_style_var_int(int idx, int val) {
    ImPlot::PushStyleVar((ImPlotStyleVar)idx, val);
}

void implot_push_style_var_vec2(int idx, imgui_vec2 val) {
    ImPlot::PushStyleVar((ImPlotStyleVar)idx, ImVec2(val.x, val.y));
}

void implot_pop_style_var(int count) {
    ImPlot::PopStyleVar(count);
}

void implot_push_colormap(int cmap) {
    ImPlot::PushColormap((ImPlotColormap)cmap);
}

void implot_pop_colormap(int count) {
    ImPlot::PopColormap(count);
}

int implot_get_colormap_count(void) {
    return ImPlot::GetColormapCount();
}

const char* implot_get_colormap_name(int cmap) {
    return ImPlot::GetColormapName((ImPlotColormap)cmap);
}

imgui_vec4 implot_get_colormap_color(int idx, int cmap) {
    const ImVec4& v = ImPlot::GetColormapColor(idx, (ImPlotColormap)cmap);
    imgui_vec4 out;
    out.x = v.x;
    out.y = v.y;
    out.z = v.z;
    out.w = v.w;
    return out;
}

imgui_vec4 implot_sample_colormap(float t, int cmap) {
    const ImVec4& v = ImPlot::SampleColormap(t, (ImPlotColormap)cmap);
    imgui_vec4 out;
    out.x = v.x;
    out.y = v.y;
    out.z = v.z;
    out.w = v.w;
    return out;
}

imgui_vec4 implot_next_colormap_color(void) {
    const ImVec4& v = ImPlot::NextColormapColor();
    imgui_vec4 out;
    out.x = v.x;
    out.y = v.y;
    out.z = v.z;
    out.w = v.w;
    return out;
}

bool implot_colormap_button(const char* label, imgui_vec2 size, int cmap) {
    return ImPlot::ColormapButton(label, ImVec2(size.x, size.y), (ImPlotColormap)cmap);
}

void implot_colormap_scale(const char* label, double scale_min, double scale_max, imgui_vec2 size, const char* fmt, int flags, int cmap) {
    ImPlot::ColormapScale(label, scale_min, scale_max, ImVec2(size.x, size.y), fmt, (ImPlotColormapScaleFlags)flags, (ImPlotColormap)cmap);
}

bool implot_colormap_slider(const char* label, float* t, imgui_vec4* out, const char* fmt, int cmap) {
    ImVec4 out_val;
    ImVec4* out_ptr = out != nullptr ? &out_val : nullptr;
    bool changed = ImPlot::ColormapSlider(label, t, out_ptr, fmt, (ImPlotColormap)cmap);
    if (out_ptr != nullptr) {
        out->x = out_val.x;
        out->y = out_val.y;
        out->z = out_val.z;
        out->w = out_val.w;
    }
    return changed;
}

void implot_colormap_icon(int cmap) {
    ImPlot::ColormapIcon((ImPlotColormap)cmap);
}

// =========================================================================
// Color maps (misc)
// =========================================================================

int implot_add_colormap(const char* name, const float* cols, int col_count) {
    // No STL here: Kotlin/Native static linking can't resolve libstdc++ runtime
    // symbols, so build the ImVec4 array with plain malloc.
    ImVec4* vec4s = (ImVec4*)malloc(sizeof(ImVec4) * col_count);
    if (vec4s == nullptr) {
        return -1;
    }
    for (int i = 0; i < col_count; i++) {
        vec4s[i] = ImVec4(cols[i * 3], cols[i * 3 + 1], cols[i * 3 + 2], 1.0f);
    }
    int result = (int)ImPlot::AddColormap(name, vec4s, col_count, true);
    free(vec4s);
    return result;
}

void implot_item_icon(uint32_t col) {
    ImPlot::ItemIcon(col);
}

uint32_t implot_get_last_item_color(void) {
    return ImGui::ColorConvertFloat4ToU32(ImPlot::GetLastItemColor());
}

// =========================================================================
// Plot utils
// =========================================================================

void implot_set_axis(int axis) {
    ImPlot::SetAxis((ImAxis)axis);
}

void implot_set_axes(int x_axis, int y_axis) {
    ImPlot::SetAxes((ImAxis)x_axis, (ImAxis)y_axis);
}

void implot_get_plot_selection(double* out_x_min, double* out_y_min, double* out_x_max, double* out_y_max) {
    ImPlotRect r = ImPlot::GetPlotSelection();
    *out_x_min = r.X.Min;
    *out_y_min = r.Y.Min;
    *out_x_max = r.X.Max;
    *out_y_max = r.Y.Max;
}

void implot_push_plot_clip_rect(float expand) {
    ImPlot::PushPlotClipRect(expand);
}

void implot_pop_plot_clip_rect(void) {
    ImPlot::PopPlotClipRect();
}

// =========================================================================
// Drag and drop
// =========================================================================

bool implot_begin_drag_drop_source_plot(int flags) {
    return ImPlot::BeginDragDropSourcePlot(flags);
}

bool implot_begin_drag_drop_source_axis(int axis, int flags) {
    return ImPlot::BeginDragDropSourceAxis((ImAxis)axis, flags);
}

bool implot_begin_drag_drop_source_item(const char* label_id, int flags) {
    return ImPlot::BeginDragDropSourceItem(label_id, flags);
}

void implot_end_drag_drop_source(void) {
    ImPlot::EndDragDropSource();
}

bool implot_begin_drag_drop_target_plot(void) {
    return ImPlot::BeginDragDropTargetPlot();
}

bool implot_begin_drag_drop_target_axis(int axis) {
    return ImPlot::BeginDragDropTargetAxis((ImAxis)axis);
}

bool implot_begin_drag_drop_target_legend(void) {
    return ImPlot::BeginDragDropTargetLegend();
}

void implot_end_drag_drop_target(void) {
    ImPlot::EndDragDropTarget();
}

// =========================================================================
// Legend popup
// =========================================================================

bool implot_begin_legend_popup(const char* label_id, int mouse_button) {
    return ImPlot::BeginLegendPopup(label_id, (ImGuiMouseButton)mouse_button);
}

void implot_end_legend_popup(void) {
    ImPlot::EndLegendPopup();
}

// =========================================================================
// Input mapping / tools
// =========================================================================

void* implot_get_input_map(void) {
    return (void*)&ImPlot::GetInputMap();
}

bool implot_show_input_map_selector(const char* label) {
    return ImPlot::ShowInputMapSelector(label);
}

void implot_show_metrics_window(bool* p_open) {
    ImPlot::ShowMetricsWindow(p_open);
}

void implot_show_style_editor(void) {
    ImPlot::ShowStyleEditor(nullptr);
}

bool implot_show_style_selector(const char* label) {
    return ImPlot::ShowStyleSelector(label);
}

bool implot_show_colormap_selector(const char* label) {
    return ImPlot::ShowColormapSelector(label);
}

// =========================================================================
// Queries
// =========================================================================

bool implot_is_plot_hovered(void) {
    return ImPlot::IsPlotHovered();
}

bool implot_is_plot_selected(void) {
    return ImPlot::IsPlotSelected();
}

bool implot_is_axis_hovered(int axis) {
    return ImPlot::IsAxisHovered((ImAxis)axis);
}

imgui_vec2 implot_get_plot_pos(void) {
    const ImVec2& v = ImPlot::GetPlotPos();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

imgui_vec2 implot_get_plot_size(void) {
    const ImVec2& v = ImPlot::GetPlotSize();
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

void implot_get_plot_limits(double* out_x_min, double* out_y_min, double* out_x_max, double* out_y_max) {
    ImPlotRect r = ImPlot::GetPlotLimits();
    *out_x_min = r.X.Min;
    *out_y_min = r.Y.Min;
    *out_x_max = r.X.Max;
    *out_y_max = r.Y.Max;
}

void implot_get_plot_mouse_pos(double* out_x, double* out_y) {
    ImPlotPoint p = ImPlot::GetPlotMousePos();
    *out_x = p.x;
    *out_y = p.y;
}

void implot_pixels_to_plot(float pix_x, float pix_y, double* out_x, double* out_y) {
    ImPlotPoint p = ImPlot::PixelsToPlot(pix_x, pix_y);
    *out_x = p.x;
    *out_y = p.y;
}

imgui_vec2 implot_plot_to_pixels(double x, double y) {
    const ImVec2& v = ImPlot::PlotToPixels(x, y);
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

imgui_draw_list* implot_get_plot_draw_list(void) {
    return (imgui_draw_list*)ImPlot::GetPlotDrawList();
}

} // extern "C"
