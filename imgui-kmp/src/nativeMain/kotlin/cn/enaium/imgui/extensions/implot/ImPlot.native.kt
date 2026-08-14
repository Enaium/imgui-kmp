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

import cn.enaium.imgui.ImDrawList
import cn.enaium.imgui.ImGuiContext
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import cn.enaium.imgui.NativeImDrawList
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

private inline fun <T> withStringArray(
    labels: Array<String>,
    block: (CValuesRef<CPointerVarOf<CPointer<ByteVarOf<Byte>>>>?) -> T,
): T {
    if (labels.isEmpty()) {
        return block(null)
    }
    return memScoped {
        val arr = allocArray<CPointerVarOf<CPointer<ByteVarOf<Byte>>>>(labels.size)
        for (i in labels.indices) {
            arr[i] = labels[i].cstr.ptr
        }
        block(arr)
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
    actual fun setupAxis(axis: Int, label: String?, flags: Int) = implot_setup_axis(axis, label, flags)
    actual fun setupAxisFormat(axis: Int, fmt: String) = implot_setup_axis_format(axis, fmt)
    actual fun setupAxisLimitsConstraints(axis: Int, vMin: Double, vMax: Double) =
        implot_setup_axis_limits_constraints(axis, vMin, vMax)

    actual fun setupAxisZoomConstraints(axis: Int, zMin: Double, zMax: Double) =
        implot_setup_axis_zoom_constraints(axis, zMin, zMax)

    actual fun setupAxisLinks(axis: Int, linkMin: DoubleArray?, linkMax: DoubleArray?) = memScoped {
        val minPtr = linkMin?.let {
            val p = allocArray<DoubleVar>(it.size)
            for (i in it.indices) p[i] = it[i]
            p
        }
        val maxPtr = linkMax?.let {
            val p = allocArray<DoubleVar>(it.size)
            for (i in it.indices) p[i] = it[i]
            p
        }
        implot_setup_axis_links(axis, minPtr, maxPtr)
    }

    actual fun setupAxisScale(axis: Int, scale: Int) = implot_setup_axis_scale(axis, scale)

    actual fun setupAxisTicks(axis: Int, values: DoubleArray, labels: Array<String>?, tickCount: Int, keepDefault: Boolean) = memScoped {
        val n = if (tickCount >= 0) tickCount else values.size
        val vArr = allocArray<DoubleVar>(values.size)
        for (i in values.indices) {
            vArr[i] = values[i]
        }
        if (labels != null) {
            val labelArr = allocArray<CPointerVarOf<CPointer<ByteVarOf<Byte>>>>(labels.size)
            for (i in labels.indices) {
                labelArr[i] = labels[i].cstr.ptr
            }
            implot_setup_axis_ticks(axis, vArr, n, labelArr, keepDefault)
        } else {
            implot_setup_axis_ticks(axis, vArr, n, null, keepDefault)
        }
    }

    actual fun setupMouseText(location: Int, flags: Int) = implot_setup_mouse_text(location, flags)
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

    actual fun plotBarGroups(
        labels: Array<String>,
        values: FloatArray,
        itemCount: Int,
        groupCount: Int,
        groupSize: Double,
        shift: Double,
        spec: ImPlotSpec,
    ) = withSpec(spec) { s ->
        withStringArray(labels) { labelArr ->
            withArray(values) { vp, _ ->
                implot_plot_bar_groups(labelArr, vp, itemCount, groupCount, groupSize, shift, s)
            }
        }
    }

    actual fun plotErrorBars(labelId: String, xs: FloatArray, ys: FloatArray, neg: FloatArray, pos: FloatArray, spec: ImPlotSpec) = withSpec(spec) { s ->
        val count = minOf(xs.size, ys.size, neg.size, pos.size)
        if (count == 0) {
            implot_plot_error_bars(labelId, null, null, null, null, 0, s)
        } else {
            memScoped {
                val xArr = allocArray<FloatVar>(count)
                val yArr = allocArray<FloatVar>(count)
                val nArr = allocArray<FloatVar>(count)
                val pArr = allocArray<FloatVar>(count)
                for (i in 0 until count) {
                    xArr[i] = xs[i]
                    yArr[i] = ys[i]
                    nArr[i] = neg[i]
                    pArr[i] = pos[i]
                }
                implot_plot_error_bars(labelId, xArr, yArr, nArr, pArr, count, s)
            }
        }
    }

    actual fun plotStems(labelId: String, xs: FloatArray, ys: FloatArray, ref: Double, spec: ImPlotSpec) = withSpec(spec) { s ->
        withTwoArrays(xs, ys) { xp, yp, count ->
            implot_plot_stems(labelId, xp, yp, count, ref, s)
        }
    }

    actual fun plotHeatmap(
        labelId: String,
        values: FloatArray,
        rows: Int,
        cols: Int,
        scaleMin: Double,
        scaleMax: Double,
        labelFormat: String,
        boundsMinX: Double,
        boundsMinY: Double,
        boundsMaxX: Double,
        boundsMaxY: Double,
        spec: ImPlotSpec,
    ) = withSpec(spec) { s ->
        withArray(values) { vp, _ ->
            implot_plot_heatmap(labelId, vp, rows, cols, scaleMin, scaleMax, labelFormat, boundsMinX, boundsMinY, boundsMaxX, boundsMaxY, s)
        }
    }

    actual fun plotHistogram2D(
        labelId: String,
        xs: FloatArray,
        ys: FloatArray,
        xBins: Int,
        yBins: Int,
        rangeXMin: Double,
        rangeXMax: Double,
        rangeYMin: Double,
        rangeYMax: Double,
        spec: ImPlotSpec,
    ): Double = withSpec(spec) { s ->
        withTwoArrays(xs, ys) { xp, yp, count ->
            implot_plot_histogram_2d(labelId, xp, yp, count, xBins, yBins, rangeXMin, rangeXMax, rangeYMin, rangeYMax, s)
        }
    }

    actual fun plotDigital(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec) = withSpec(spec) { s ->
        withTwoArrays(xs, ys) { xp, yp, count ->
            implot_plot_digital(labelId, xp, yp, count, s)
        }
    }

    actual fun plotPieChart(
        labels: Array<String>,
        values: FloatArray,
        x: Double,
        y: Double,
        radius: Double,
        labelFormat: String,
        angle0: Double,
        spec: ImPlotSpec,
    ) = withSpec(spec) { s ->
        withStringArray(labels) { labelArr ->
            withArray(values) { vp, count ->
                implot_plot_pie_chart(labelArr, vp, count, x, y, radius, labelFormat, angle0, s)
            }
        }
    }

    actual fun plotBubbles(labelId: String, xs: FloatArray, ys: FloatArray, sizes: FloatArray, spec: ImPlotSpec) = withSpec(spec) { s ->
        val count = minOf(xs.size, ys.size, sizes.size)
        if (count == 0) {
            implot_plot_bubbles(labelId, null, null, null, 0, s)
        } else {
            memScoped {
                val xArr = allocArray<FloatVar>(count)
                val yArr = allocArray<FloatVar>(count)
                val sArr = allocArray<FloatVar>(count)
                for (i in 0 until count) {
                    xArr[i] = xs[i]
                    yArr[i] = ys[i]
                    sArr[i] = sizes[i]
                }
                implot_plot_bubbles(labelId, xArr, yArr, sArr, count, s)
            }
        }
    }

    actual fun plotPolygon(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec) = withSpec(spec) { s ->
        withTwoArrays(xs, ys) { xp, yp, count ->
            implot_plot_polygon(labelId, xp, yp, count, s)
        }
    }

    actual fun plotImage(
        labelId: String,
        texId: Long,
        xMin: Double,
        yMin: Double,
        xMax: Double,
        yMax: Double,
        uvMin: ImVec2,
        uvMax: ImVec2,
        tintCol: ImVec4,
        spec: ImPlotSpec,
    ) = withSpec(spec) { s ->
        withVec2(uvMin) { uv0 ->
            withVec2(uvMax) { uv1 ->
                withVec4(tintCol) { tint ->
                    implot_plot_image(labelId, texId.toULong(), xMin, yMin, xMax, yMax, uv0, uv1, tint, s)
                }
            }
        }
    }

    actual fun beginSubplots(titleId: String, rows: Int, cols: Int, size: ImVec2, flags: Int): Boolean = withVec2(size) {
        implot_begin_subplots(titleId, rows, cols, it, flags)
    }

    actual fun endSubplots() = implot_end_subplots()

    actual fun dragPoint(id: Int, x: DoubleArray, y: DoubleArray, col: ImVec4, size: Float, flags: Int): Boolean = withVec4(col) { c ->
        memScoped {
            val xArr = allocArray<DoubleVar>(x.size)
            val yArr = allocArray<DoubleVar>(y.size)
            for (i in x.indices) xArr[i] = x[i]
            for (i in y.indices) yArr[i] = y[i]
            val changed = implot_drag_point(id, xArr, yArr, c, size, flags)
            for (i in x.indices) x[i] = xArr[i]
            for (i in y.indices) y[i] = yArr[i]
            changed
        }
    }

    actual fun dragLineX(id: Int, x: DoubleArray, col: ImVec4, thickness: Float, flags: Int): Boolean = withVec4(col) { c ->
        memScoped {
            val xArr = allocArray<DoubleVar>(x.size)
            for (i in x.indices) xArr[i] = x[i]
            val changed = implot_drag_line_x(id, xArr, c, thickness, flags)
            for (i in x.indices) x[i] = xArr[i]
            changed
        }
    }

    actual fun dragLineY(id: Int, y: DoubleArray, col: ImVec4, thickness: Float, flags: Int): Boolean = withVec4(col) { c ->
        memScoped {
            val yArr = allocArray<DoubleVar>(y.size)
            for (i in y.indices) yArr[i] = y[i]
            val changed = implot_drag_line_y(id, yArr, c, thickness, flags)
            for (i in y.indices) y[i] = yArr[i]
            changed
        }
    }

    actual fun dragRect(
        id: Int,
        xMin: DoubleArray,
        yMin: DoubleArray,
        xMax: DoubleArray,
        yMax: DoubleArray,
        col: ImVec4,
        flags: Int,
    ): Boolean = withVec4(col) { c ->
        memScoped {
            val xMinArr = allocArray<DoubleVar>(xMin.size)
            val yMinArr = allocArray<DoubleVar>(yMin.size)
            val xMaxArr = allocArray<DoubleVar>(xMax.size)
            val yMaxArr = allocArray<DoubleVar>(yMax.size)
            for (i in xMin.indices) xMinArr[i] = xMin[i]
            for (i in yMin.indices) yMinArr[i] = yMin[i]
            for (i in xMax.indices) xMaxArr[i] = xMax[i]
            for (i in yMax.indices) yMaxArr[i] = yMax[i]
            val changed = implot_drag_rect(id, xMinArr, yMinArr, xMaxArr, yMaxArr, c, flags)
            for (i in xMin.indices) xMin[i] = xMinArr[i]
            for (i in yMin.indices) yMin[i] = yMinArr[i]
            for (i in xMax.indices) xMax[i] = xMaxArr[i]
            for (i in yMax.indices) yMax[i] = yMaxArr[i]
            changed
        }
    }

    actual fun annotation(x: Double, y: Double, col: ImVec4, pixOffset: ImVec2, clamp: Boolean, round: Boolean, fmt: String?) =
        withVec4(col) { c ->
            withVec2(pixOffset) { off ->
                implot_annotation(x, y, c, off, clamp, round, fmt)
            }
        }

    actual fun tagX(x: Double, col: ImVec4, round: Boolean, fmt: String?) = withVec4(col) {
        implot_tag_x(x, it, round, fmt)
    }

    actual fun tagY(y: Double, col: ImVec4, round: Boolean, fmt: String?) = withVec4(col) {
        implot_tag_y(y, it, round, fmt)
    }

    actual fun getPlotLimits(): DoubleArray = memScoped {
        val xMin = alloc<DoubleVar>()
        val yMin = alloc<DoubleVar>()
        val xMax = alloc<DoubleVar>()
        val yMax = alloc<DoubleVar>()
        implot_get_plot_limits(xMin.ptr, yMin.ptr, xMax.ptr, yMax.ptr)
        doubleArrayOf(xMin.value, yMin.value, xMax.value, yMax.value)
    }

    actual fun getPlotMousePos(): DoubleArray = memScoped {
        val x = alloc<DoubleVar>()
        val y = alloc<DoubleVar>()
        implot_get_plot_mouse_pos(x.ptr, y.ptr)
        doubleArrayOf(x.value, y.value)
    }

    actual fun pixelsToPlot(pixX: Float, pixY: Float): DoubleArray = memScoped {
        val x = alloc<DoubleVar>()
        val y = alloc<DoubleVar>()
        implot_pixels_to_plot(pixX, pixY, x.ptr, y.ptr)
        doubleArrayOf(x.value, y.value)
    }

    actual fun plotToPixels(x: Double, y: Double): ImVec2 =
        implot_plot_to_pixels(x, y).useContents { ImVec2(this.x, this.y) }

    actual fun getPlotDrawList(): ImDrawList = NativeImDrawList(implot_get_plot_draw_list())

    actual fun nextColormapColor(): ImVec4 =
        implot_next_colormap_color().useContents { ImVec4(x, y, z, w) }

    actual fun getColormapCount(): Int = implot_get_colormap_count()

    actual fun getColormapName(idx: Int): String = implot_get_colormap_name(idx)?.toKString() ?: ""

    actual fun getColormapColor(idx: Int, cmap: Int): ImVec4 =
        implot_get_colormap_color(idx, cmap).useContents { ImVec4(x, y, z, w) }

    actual fun sampleColormap(t: Float, cmap: Int): ImVec4 =
        implot_sample_colormap(t, cmap).useContents { ImVec4(x, y, z, w) }

    actual fun colormapButton(label: String, size: ImVec2, cmap: Int): Boolean = withVec2(size) {
        implot_colormap_button(label, it, cmap)
    }

    actual fun colormapScale(label: String, scaleMin: Double, scaleMax: Double, size: ImVec2, fmt: String, flags: Int, cmap: Int) = withVec2(size) {
        implot_colormap_scale(label, scaleMin, scaleMax, it, fmt, flags, cmap)
    }

    actual fun colormapSlider(label: String, t: FloatArray, out: FloatArray?, fmt: String, cmap: Int): Boolean = memScoped {
        val tArr = allocArray<FloatVar>(1)
        tArr[0] = t[0]
        val outVal = alloc<imgui_vec4>()
        val changed = if (out != null) {
            implot_colormap_slider(label, tArr, outVal.ptr, fmt, cmap)
        } else {
            implot_colormap_slider(label, tArr, null, fmt, cmap)
        }
        t[0] = tArr[0]
        if (out != null) {
            out[0] = outVal.x
            out[1] = outVal.y
            out[2] = outVal.z
            out[3] = outVal.w
        }
        changed
    }

    actual fun colormapIcon(cmap: Int) = implot_colormap_icon(cmap)

    actual fun addColormap(name: String, cols: FloatArray): Int = memScoped {
        val arr = allocArray<FloatVar>(cols.size)
        for (i in cols.indices) arr[i] = cols[i]
        implot_add_colormap(name, arr, cols.size / 3)
    }

    actual fun itemIcon(col: Int) = implot_item_icon(col.toUInt())

    actual fun getLastItemColor(): Int = implot_get_last_item_color().toInt()

    actual fun setAxis(axis: Int) = implot_set_axis(axis)
    actual fun setAxes(xAxis: Int, yAxis: Int) = implot_set_axes(xAxis, yAxis)

    actual fun getPlotSelection(): DoubleArray = memScoped {
        val xMin = alloc<DoubleVar>()
        val yMin = alloc<DoubleVar>()
        val xMax = alloc<DoubleVar>()
        val yMax = alloc<DoubleVar>()
        implot_get_plot_selection(xMin.ptr, yMin.ptr, xMax.ptr, yMax.ptr)
        doubleArrayOf(xMin.value, yMin.value, xMax.value, yMax.value)
    }

    actual fun pushPlotClipRect(expand: Float) = implot_push_plot_clip_rect(expand)
    actual fun popPlotClipRect() = implot_pop_plot_clip_rect()

    actual fun beginDragDropSourcePlot(flags: Int): Boolean = implot_begin_drag_drop_source_plot(flags)
    actual fun beginDragDropSourceAxis(axis: Int, flags: Int): Boolean = implot_begin_drag_drop_source_axis(axis, flags)
    actual fun beginDragDropSourceItem(labelId: String, flags: Int): Boolean = implot_begin_drag_drop_source_item(labelId, flags)
    actual fun endDragDropSource() = implot_end_drag_drop_source()
    actual fun beginDragDropTargetPlot(): Boolean = implot_begin_drag_drop_target_plot()
    actual fun beginDragDropTargetAxis(axis: Int): Boolean = implot_begin_drag_drop_target_axis(axis)
    actual fun beginDragDropTargetLegend(): Boolean = implot_begin_drag_drop_target_legend()
    actual fun endDragDropTarget() = implot_end_drag_drop_target()

    actual fun beginLegendPopup(labelId: String, mouseButton: Int): Boolean = implot_begin_legend_popup(labelId, mouseButton)
    actual fun endLegendPopup() = implot_end_legend_popup()

    actual fun getInputMap(): Long = implot_get_input_map()?.rawValue?.toLong() ?: 0L

    actual fun showInputMapSelector(label: String): Boolean = implot_show_input_map_selector(label)

    actual fun showMetricsWindow(pOpen: BooleanArray?) = memScoped {
        if (pOpen != null) {
            val b = alloc<BooleanVar>()
            b.value = pOpen[0]
            implot_show_metrics_window(b.ptr)
            pOpen[0] = b.value
        } else {
            implot_show_metrics_window(null)
        }
    }

    actual fun showStyleEditor() = implot_show_style_editor()
    actual fun showStyleSelector(label: String): Boolean = implot_show_style_selector(label)
    actual fun showColormapSelector(label: String): Boolean = implot_show_colormap_selector(label)

    actual fun pushStyleColor(idx: Int, color: ImVec4) = withVec4(color) {
        implot_push_style_color_vec4(idx, it)
    }

    actual fun popStyleColor(count: Int) = implot_pop_style_color(count)
    actual fun pushStyleVar(idx: Int, value: Float) = implot_push_style_var_float(idx, value)
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
