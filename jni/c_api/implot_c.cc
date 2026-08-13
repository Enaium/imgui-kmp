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

} // extern "C"
