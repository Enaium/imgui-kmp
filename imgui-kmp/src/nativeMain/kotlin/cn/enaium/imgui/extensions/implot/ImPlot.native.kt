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

package cn.enaium.imgui.extensions.implot

import cn.enaium.imgui.ImGuiContext
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import cn.enaium.imgui.NativeImGuiContext
import imgui.*
import kotlinx.cinterop.*

// =========================================================================
// Native (cinterop) actual implementations
// =========================================================================

internal class NativeImPlotContext(internal val ptr: CPointer<implot_context>?) : ImPlotContext {
    override fun close() {
        implot_destroy_context(ptr)
    }
}

/** Fills an implot_spec from the Kotlin [ImPlotSpec]; passes null when empty. */
private inline fun <T> withSpec(spec: ImPlotSpec, block: (CPointer<implot_spec>?) -> T): T {
    if (spec == ImPlotSpec()) return block(null)
    return memScoped {
        val c = alloc<implot_spec>()
        spec.lineColor?.let {
            c.line_color[0] = it.x
            c.line_color[1] = it.y
            c.line_color[2] = it.z
            c.line_color[3] = it.w
            c.line_color_set = 1
        }
        spec.lineWeight?.let {
            c.line_weight = it
            c.line_weight_set = 1
        }
        spec.fillColor?.let {
            c.fill_color[0] = it.x
            c.fill_color[1] = it.y
            c.fill_color[2] = it.z
            c.fill_color[3] = it.w
            c.fill_color_set = 1
        }
        spec.fillAlpha?.let {
            c.fill_alpha = it
            c.fill_alpha_set = 1
        }
        spec.marker?.let {
            c.marker = it
            c.marker_set = 1
        }
        spec.markerSize?.let {
            c.marker_size = it
            c.marker_size_set = 1
        }
        spec.markerLineColor?.let {
            c.marker_line_color[0] = it.x
            c.marker_line_color[1] = it.y
            c.marker_line_color[2] = it.z
            c.marker_line_color[3] = it.w
            c.marker_line_color_set = 1
        }
        spec.markerFillColor?.let {
            c.marker_fill_color[0] = it.x
            c.marker_fill_color[1] = it.y
            c.marker_fill_color[2] = it.z
            c.marker_fill_color[3] = it.w
            c.marker_fill_color_set = 1
        }
        spec.size?.let {
            c.size = it
            c.size_set = 1
        }
        spec.offset?.let {
            c.offset = it
            c.offset_set = 1
        }
        spec.stride?.let {
            c.stride = it
            c.stride_set = 1
        }
        spec.flags?.let {
            c.flags = it
            c.flags_set = 1
        }
        block(c.ptr)
    }
}

private inline fun <T> withVec2(value: ImVec2, block: (CValue<imgui_vec2>) -> T): T = memScoped {
    val v = alloc<imgui_vec2>()
    v.x = value.x
    v.y = value.y
    block(v.readValue())
}

private inline fun <T> withVec4(value: ImVec4, block: (CValue<imgui_vec4>) -> T): T = memScoped {
    val v = alloc<imgui_vec4>()
    v.x = value.x
    v.y = value.y
    v.z = value.z
    v.w = value.w
    block(v.readValue())
}

private inline fun <T> withTwoArrays(
    xs: FloatArray,
    ys: FloatArray,
    block: (CPointer<FloatVar>?, CPointer<FloatVar>?, Int) -> T,
): T {
    val count = minOf(xs.size, ys.size)
    if (count == 0) {
        return block(null, null, 0)
    }
    return memScoped {
        val xArr = allocArray<FloatVar>(count)
        val yArr = allocArray<FloatVar>(count)
        for (i in 0 until count) {
            xArr[i] = xs[i]
            yArr[i] = ys[i]
        }
        block(xArr, yArr, count)
    }
}

private inline fun <T> withArray(values: FloatArray, block: (CPointer<FloatVar>?, Int) -> T): T {
    if (values.isEmpty()) {
        return block(null, 0)
    }
    return memScoped {
        val arr = allocArray<FloatVar>(values.size)
        for (i in values.indices) {
            arr[i] = values[i]
        }
        block(arr, values.size)
    }
}

// =========================================================================
// actual object
// =========================================================================

actual object ImPlot {
    actual fun createContext(): ImPlotContext {
        val ptr = implot_create_context()
            ?: error("implot_create_context returned null")
        return NativeImPlotContext(ptr)
    }

    actual fun destroyContext(context: ImPlotContext?) {
        if (context != null) {
            implot_destroy_context((context as NativeImPlotContext).ptr)
        } else {
            implot_destroy_context(null)
        }
    }

    actual fun getCurrentContext(): ImPlotContext? {
        val ptr = implot_get_current_context()
        return if (ptr != null) NativeImPlotContext(ptr) else null
    }

    actual fun setImGuiContext(context: ImGuiContext) {
        implot_set_im_gui_context((context as NativeImGuiContext).ptr)
    }

    actual fun showDemoWindow(pOpen: BooleanArray?) = memScoped {
        if (pOpen != null) {
            val b = alloc<BooleanVar>()
            b.value = pOpen[0]
            implot_show_demo_window(b.ptr)
            pOpen[0] = b.value
        } else {
            implot_show_demo_window(null)
        }
    }

    actual fun beginPlot(titleId: String, size: ImVec2, flags: Int): Boolean = withVec2(size) {
        implot_begin_plot(titleId, it, flags)
    }

    actual fun endPlot() = implot_end_plot()
    actual fun setupAxes(xLabel: String?, yLabel: String?, xFlags: Int, yFlags: Int) =
        implot_setup_axes(xLabel, yLabel, xFlags, yFlags)

    actual fun setupAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, cond: Int) =
        implot_setup_axes_limits(xMin, xMax, yMin, yMax, cond)

    actual fun setupAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int) =
        implot_setup_axis_limits(axis, vMin, vMax, cond)

    actual fun setupLegend(location: Int, flags: Int) = implot_setup_legend(location, flags)
    actual fun setupFinish() = implot_setup_finish()
    actual fun setNextAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, cond: Int) =
        implot_set_next_axes_limits(xMin, xMax, yMin, yMax, cond)

    actual fun setNextAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int) =
        implot_set_next_axis_limits(axis, vMin, vMax, cond)

    actual fun plotLine(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec) = withSpec(spec) { s ->
        withTwoArrays(xs, ys) { xp, yp, count ->
            implot_plot_line(labelId, xp, yp, count, s)
        }
    }

    actual fun plotLine(labelId: String, values: FloatArray, xScale: Double, xStart: Double, spec: ImPlotSpec) = withSpec(spec) { s ->
        withArray(values) { vp, count ->
            implot_plot_line_values(labelId, vp, count, xScale, xStart, s)
        }
    }

    actual fun plotScatter(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec) = withSpec(spec) { s ->
        withTwoArrays(xs, ys) { xp, yp, count ->
            implot_plot_scatter(labelId, xp, yp, count, s)
        }
    }

    actual fun plotScatter(labelId: String, values: FloatArray, xScale: Double, xStart: Double, spec: ImPlotSpec) = withSpec(spec) { s ->
        withArray(values) { vp, count ->
            implot_plot_scatter_values(labelId, vp, count, xScale, xStart, s)
        }
    }

    actual fun plotStairs(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec) = withSpec(spec) { s ->
        withTwoArrays(xs, ys) { xp, yp, count ->
            implot_plot_stairs(labelId, xp, yp, count, s)
        }
    }

    actual fun plotBars(labelId: String, xs: FloatArray, ys: FloatArray, barSize: Double, spec: ImPlotSpec) = withSpec(spec) { s ->
        withTwoArrays(xs, ys) { xp, yp, count ->
            implot_plot_bars(labelId, xp, yp, count, barSize, s)
        }
    }

    actual fun plotBars(labelId: String, values: FloatArray, barSize: Double, shift: Double, spec: ImPlotSpec) = withSpec(spec) { s ->
        withArray(values) { vp, count ->
            implot_plot_bars_values(labelId, vp, count, barSize, shift, s)
        }
    }

    actual fun plotHistogram(
        labelId: String,
        values: FloatArray,
        bins: Int,
        barScale: Double,
        rangeMin: Double,
        rangeMax: Double,
        spec: ImPlotSpec,
    ): Double = withSpec(spec) { s ->
        withArray(values) { vp, count ->
            implot_plot_histogram(labelId, vp, count, bins, barScale, rangeMin, rangeMax, s)
        }
    }

    actual fun plotInfLines(labelId: String, values: FloatArray, spec: ImPlotSpec) = withSpec(spec) { s ->
        withArray(values) { vp, count ->
            implot_plot_inf_lines(labelId, vp, count, s)
        }
    }

    actual fun plotShaded(labelId: String, xs: FloatArray, ys: FloatArray, yRef: Double, spec: ImPlotSpec) = withSpec(spec) { s ->
        withTwoArrays(xs, ys) { xp, yp, count ->
            implot_plot_shaded(labelId, xp, yp, count, yRef, s)
        }
    }

    actual fun plotText(text: String, x: Double, y: Double, pixOffset: ImVec2) = withVec2(pixOffset) {
        implot_plot_text(text, x, y, it)
    }

    actual fun plotDummy(labelId: String, spec: ImPlotSpec) = withSpec(spec) {
        implot_plot_dummy(labelId, it)
    }

    actual fun pushStyleColor(idx: Int, color: ImVec4) = withVec4(color) {
        implot_push_style_color_vec4(idx, it)
    }

    actual fun popStyleColor(count: Int) = implot_pop_style_color(count)
    actual fun pushStyleVarFloat(idx: Int, value: Float) = implot_push_style_var_float(idx, value)
    actual fun pushStyleVarInt(idx: Int, value: Int) = implot_push_style_var_int(idx, value)
    actual fun pushStyleVarVec2(idx: Int, value: ImVec2) = withVec2(value) {
        implot_push_style_var_vec2(idx, it)
    }

    actual fun popStyleVar(count: Int) = implot_pop_style_var(count)
    actual fun pushColormap(cmap: Int) = implot_push_colormap(cmap)
    actual fun popColormap(count: Int) = implot_pop_colormap(count)

    actual fun isPlotHovered(): Boolean = implot_is_plot_hovered()
    actual fun isPlotSelected(): Boolean = implot_is_plot_selected()
    actual fun isAxisHovered(axis: Int): Boolean = implot_is_axis_hovered(axis)
    actual fun getPlotPos(): ImVec2 =
        implot_get_plot_pos().useContents { ImVec2(x, y) }

    actual fun getPlotSize(): ImVec2 =
        implot_get_plot_size().useContents { ImVec2(x, y) }
}
