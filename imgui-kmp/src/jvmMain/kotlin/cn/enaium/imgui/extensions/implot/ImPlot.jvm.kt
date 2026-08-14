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

package cn.enaium.imgui.extensions.implot

import cn.enaium.imgui.ImDrawList
import cn.enaium.imgui.ImGuiContext
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import cn.enaium.imgui.JvmImDrawList
import cn.enaium.imgui.JvmImGuiContext

// =========================================================================
// JNI bridge
// =========================================================================

internal object Jni {
    init {
        cn.enaium.imgui.NativeLoader.load()
    }

    external fun createContext(): Long
    external fun destroyContext(ptr: Long)
    external fun getCurrentContext(): Long
    external fun setImGuiContext(ctx: Long)
    external fun showDemoWindow(pOpen: BooleanArray?)

    external fun beginPlot(titleId: String, sizeX: Float, sizeY: Float, flags: Int): Boolean
    external fun endPlot()
    external fun setupAxes(xLabel: String?, yLabel: String?, xFlags: Int, yFlags: Int)
    external fun setupAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, cond: Int)
    external fun setupAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int)
    external fun setupLegend(location: Int, flags: Int)
    external fun setupFinish()
    external fun setupAxis(axis: Int, label: String?, flags: Int)
    external fun setupAxisFormat(axis: Int, fmt: String)
    external fun setupAxisLimitsConstraints(axis: Int, vMin: Double, vMax: Double)
    external fun setupAxisZoomConstraints(axis: Int, zMin: Double, zMax: Double)
    external fun setupAxisLinks(axis: Int, linkMin: DoubleArray?, linkMax: DoubleArray?)
    external fun setupAxisScale(axis: Int, scale: Int)
    external fun setupAxisTicks(axis: Int, values: DoubleArray, labels: Array<String>?, tickCount: Int, keepDefault: Boolean)
    external fun setupMouseText(location: Int, flags: Int)
    external fun setNextAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, cond: Int)
    external fun setNextAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int)

    external fun plotLine(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    external fun plotLineValues(labelId: String, values: FloatArray, count: Int, xscale: Double, xstart: Double, spec: FloatArray?)
    external fun plotScatter(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    external fun plotScatterValues(labelId: String, values: FloatArray, count: Int, xscale: Double, xstart: Double, spec: FloatArray?)
    external fun plotStairs(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    external fun plotBars(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, barSize: Double, spec: FloatArray?)
    external fun plotBarsValues(labelId: String, values: FloatArray, count: Int, barSize: Double, shift: Double, spec: FloatArray?)
    external fun plotHistogram(labelId: String, values: FloatArray, count: Int, bins: Int, barScale: Double, rangeMin: Double, rangeMax: Double, spec: FloatArray?): Double
    external fun plotInfLines(labelId: String, values: FloatArray, count: Int, spec: FloatArray?)
    external fun plotShaded(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, yref: Double, spec: FloatArray?)
    external fun plotText(text: String, x: Double, y: Double, pixX: Float, pixY: Float)
    external fun plotDummy(labelId: String, spec: FloatArray?)

    external fun plotBarGroups(labels: Array<String>, values: FloatArray, itemCount: Int, groupCount: Int, groupSize: Double, shift: Double, spec: FloatArray?)
    external fun plotErrorBars(labelId: String, xs: FloatArray, ys: FloatArray, neg: FloatArray, pos: FloatArray, count: Int, spec: FloatArray?)
    external fun plotStems(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, ref: Double, spec: FloatArray?)
    external fun plotHeatmap(labelId: String, values: FloatArray, rows: Int, cols: Int, scaleMin: Double, scaleMax: Double, labelFormat: String, boundsMinX: Double, boundsMinY: Double, boundsMaxX: Double, boundsMaxY: Double, spec: FloatArray?)
    external fun plotHistogram2D(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, xBins: Int, yBins: Int, rangeXMin: Double, rangeXMax: Double, rangeYMin: Double, rangeYMax: Double, spec: FloatArray?): Double
    external fun plotDigital(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    external fun plotPieChart(labels: Array<String>, values: FloatArray, count: Int, x: Double, y: Double, radius: Double, labelFormat: String, angle0: Double, spec: FloatArray?)
    external fun plotBubbles(labelId: String, xs: FloatArray, ys: FloatArray, sizes: FloatArray, count: Int, spec: FloatArray?)
    external fun plotPolygon(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    external fun plotImage(labelId: String, texId: Long, xMin: Double, yMin: Double, xMax: Double, yMax: Double, uvMinX: Float, uvMinY: Float, uvMaxX: Float, uvMaxY: Float, tintR: Float, tintG: Float, tintB: Float, tintA: Float, spec: FloatArray?)

    external fun beginSubplots(titleId: String, rows: Int, cols: Int, sizeX: Float, sizeY: Float, flags: Int): Boolean
    external fun endSubplots()

    external fun dragPoint(id: Int, x: DoubleArray, y: DoubleArray, colR: Float, colG: Float, colB: Float, colA: Float, size: Float, flags: Int): Boolean
    external fun dragLineX(id: Int, x: DoubleArray, colR: Float, colG: Float, colB: Float, colA: Float, thickness: Float, flags: Int): Boolean
    external fun dragLineY(id: Int, y: DoubleArray, colR: Float, colG: Float, colB: Float, colA: Float, thickness: Float, flags: Int): Boolean
    external fun dragRect(id: Int, xMin: DoubleArray, yMin: DoubleArray, xMax: DoubleArray, yMax: DoubleArray, colR: Float, colG: Float, colB: Float, colA: Float, flags: Int): Boolean
    external fun annotation(x: Double, y: Double, colR: Float, colG: Float, colB: Float, colA: Float, pixX: Float, pixY: Float, clamp: Boolean, round: Boolean, fmt: String?)
    external fun tagX(x: Double, colR: Float, colG: Float, colB: Float, colA: Float, round: Boolean, fmt: String?)
    external fun tagY(y: Double, colR: Float, colG: Float, colB: Float, colA: Float, round: Boolean, fmt: String?)

    external fun getPlotLimits(): DoubleArray
    external fun getPlotMousePos(): DoubleArray
    external fun pixelsToPlot(pixX: Float, pixY: Float): DoubleArray
    external fun plotToPixels(x: Double, y: Double): FloatArray
    external fun getPlotDrawList(): Long
    external fun nextColormapColor(): FloatArray

    external fun getColormapCount(): Int
    external fun getColormapName(idx: Int): String
    external fun getColormapColor(idx: Int, cmap: Int): FloatArray
    external fun sampleColormap(t: Float, cmap: Int): FloatArray
    external fun colormapButton(label: String, sizeX: Float, sizeY: Float, cmap: Int): Boolean
    external fun colormapScale(label: String, scaleMin: Double, scaleMax: Double, sizeX: Float, sizeY: Float, fmt: String, flags: Int, cmap: Int)
    external fun colormapSlider(label: String, t: FloatArray, out: FloatArray?, fmt: String, cmap: Int): Boolean
    external fun colormapIcon(cmap: Int)

    external fun addColormap(name: String, cols: FloatArray): Int
    external fun itemIcon(col: Int)
    external fun getLastItemColor(): Int

    external fun setAxis(axis: Int)
    external fun setAxes(xAxis: Int, yAxis: Int)
    external fun getPlotSelection(): DoubleArray
    external fun pushPlotClipRect(expand: Float)
    external fun popPlotClipRect()

    external fun beginDragDropSourcePlot(flags: Int): Boolean
    external fun beginDragDropSourceAxis(axis: Int, flags: Int): Boolean
    external fun beginDragDropSourceItem(labelId: String, flags: Int): Boolean
    external fun endDragDropSource()
    external fun beginDragDropTargetPlot(): Boolean
    external fun beginDragDropTargetAxis(axis: Int): Boolean
    external fun beginDragDropTargetLegend(): Boolean
    external fun endDragDropTarget()

    external fun beginLegendPopup(labelId: String, mouseButton: Int): Boolean
    external fun endLegendPopup()

    external fun getInputMap(): Long
    external fun showInputMapSelector(label: String): Boolean
    external fun showMetricsWindow(pOpen: BooleanArray?)
    external fun showStyleEditor()
    external fun showStyleSelector(label: String): Boolean
    external fun showColormapSelector(label: String): Boolean

    external fun pushStyleColorVec4(idx: Int, r: Float, g: Float, b: Float, a: Float)
    external fun pushStyleColorU32(idx: Int, color: Int)
    external fun popStyleColor(count: Int)
    external fun pushStyleVarFloat(idx: Int, value: Float)
    external fun pushStyleVarInt(idx: Int, value: Int)
    external fun pushStyleVarVec2(idx: Int, x: Float, y: Float)
    external fun popStyleVar(count: Int)
    external fun pushColormap(cmap: Int)
    external fun popColormap(count: Int)

    external fun isPlotHovered(): Boolean
    external fun isPlotSelected(): Boolean
    external fun isAxisHovered(axis: Int): Boolean
    external fun getPlotPos(): FloatArray
    external fun getPlotSize(): FloatArray
}

// =========================================================================
// JVM/Android actual implementations
// =========================================================================

internal class JvmImPlotContext(internal val ptr: Long) : ImPlotContext {
    override fun close() {
        Jni.destroyContext(ptr)
    }
}

internal fun encodeSpec(spec: ImPlotSpec): FloatArray? {
    if (spec == ImPlotSpec()) return null

    // [setFlag, value..., setFlag, value..., ...]
    val out = ArrayList<Float>(57)
    val lineColor = spec.lineColor
    out.add(if (lineColor != null) 1f else 0f)
    if (lineColor != null) {
        out.add(lineColor.x); out.add(lineColor.y); out.add(lineColor.z); out.add(lineColor.w)
    } else {
        repeat(4) { out.add(0f) }
    }
    out.add(if (spec.lineWeight != null) 1f else 0f); out.add(spec.lineWeight ?: 0f)
    val fillColor = spec.fillColor
    out.add(if (fillColor != null) 1f else 0f)
    if (fillColor != null) {
        out.add(fillColor.x); out.add(fillColor.y); out.add(fillColor.z); out.add(fillColor.w)
    } else {
        repeat(4) { out.add(0f) }
    }
    out.add(if (spec.fillAlpha != null) 1f else 0f); out.add(spec.fillAlpha ?: 0f)
    out.add(if (spec.marker != null) 1f else 0f); out.add((spec.marker ?: 0).toFloat())
    out.add(if (spec.markerSize != null) 1f else 0f); out.add(spec.markerSize ?: 0f)
    val markerLineColor = spec.markerLineColor
    out.add(if (markerLineColor != null) 1f else 0f)
    if (markerLineColor != null) {
        out.add(markerLineColor.x); out.add(markerLineColor.y); out.add(markerLineColor.z); out.add(markerLineColor.w)
    } else {
        repeat(4) { out.add(0f) }
    }
    val markerFillColor = spec.markerFillColor
    out.add(if (markerFillColor != null) 1f else 0f)
    if (markerFillColor != null) {
        out.add(markerFillColor.x); out.add(markerFillColor.y); out.add(markerFillColor.z); out.add(markerFillColor.w)
    } else {
        repeat(4) { out.add(0f) }
    }
    out.add(if (spec.size != null) 1f else 0f); out.add(spec.size ?: 0f)
    out.add(if (spec.offset != null) 1f else 0f); out.add((spec.offset ?: 0).toFloat())
    out.add(if (spec.stride != null) 1f else 0f); out.add((spec.stride ?: 0).toFloat())
    out.add(if (spec.flags != null) 1f else 0f); out.add((spec.flags ?: 0).toFloat())
    return out.toFloatArray()
}

actual object ImPlot {
    actual fun createContext(): ImPlotContext = JvmImPlotContext(Jni.createContext())
    actual fun destroyContext(context: ImPlotContext?) {
        Jni.destroyContext(if (context != null) (context as JvmImPlotContext).ptr else 0L)
    }

    actual fun getCurrentContext(): ImPlotContext? {
        val ptr = Jni.getCurrentContext()
        return if (ptr != 0L) JvmImPlotContext(ptr) else null
    }

    actual fun setImGuiContext(context: ImGuiContext) {
        Jni.setImGuiContext((context as JvmImGuiContext).ptr)
    }

    actual fun showDemoWindow(pOpen: BooleanArray?) = Jni.showDemoWindow(pOpen)

    actual fun beginPlot(titleId: String, size: ImVec2, flags: Int): Boolean =
        Jni.beginPlot(titleId, size.x, size.y, flags)

    actual fun endPlot() = Jni.endPlot()
    actual fun setupAxes(xLabel: String?, yLabel: String?, xFlags: Int, yFlags: Int) =
        Jni.setupAxes(xLabel, yLabel, xFlags, yFlags)

    actual fun setupAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, cond: Int) =
        Jni.setupAxesLimits(xMin, xMax, yMin, yMax, cond)

    actual fun setupAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int) =
        Jni.setupAxisLimits(axis, vMin, vMax, cond)

    actual fun setupLegend(location: Int, flags: Int) = Jni.setupLegend(location, flags)
    actual fun setupFinish() = Jni.setupFinish()
    actual fun setupAxis(axis: Int, label: String?, flags: Int) = Jni.setupAxis(axis, label, flags)
    actual fun setupAxisFormat(axis: Int, fmt: String) = Jni.setupAxisFormat(axis, fmt)
    actual fun setupAxisLimitsConstraints(axis: Int, vMin: Double, vMax: Double) =
        Jni.setupAxisLimitsConstraints(axis, vMin, vMax)

    actual fun setupAxisZoomConstraints(axis: Int, zMin: Double, zMax: Double) =
        Jni.setupAxisZoomConstraints(axis, zMin, zMax)

    actual fun setupAxisLinks(axis: Int, linkMin: DoubleArray?, linkMax: DoubleArray?) =
        Jni.setupAxisLinks(axis, linkMin, linkMax)

    actual fun setupAxisScale(axis: Int, scale: Int) = Jni.setupAxisScale(axis, scale)

    actual fun setupAxisTicks(axis: Int, values: DoubleArray, labels: Array<String>?, tickCount: Int, keepDefault: Boolean) =
        Jni.setupAxisTicks(axis, values, labels, if (tickCount >= 0) tickCount else values.size, keepDefault)

    actual fun setupMouseText(location: Int, flags: Int) = Jni.setupMouseText(location, flags)
    actual fun setNextAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, cond: Int) =
        Jni.setNextAxesLimits(xMin, xMax, yMin, yMax, cond)

    actual fun setNextAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int) =
        Jni.setNextAxisLimits(axis, vMin, vMax, cond)

    actual fun plotLine(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec) =
        Jni.plotLine(labelId, xs, ys, minOf(xs.size, ys.size), encodeSpec(spec))

    actual fun plotLine(labelId: String, values: FloatArray, xScale: Double, xStart: Double, spec: ImPlotSpec) =
        Jni.plotLineValues(labelId, values, values.size, xScale, xStart, encodeSpec(spec))

    actual fun plotScatter(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec) =
        Jni.plotScatter(labelId, xs, ys, minOf(xs.size, ys.size), encodeSpec(spec))

    actual fun plotScatter(labelId: String, values: FloatArray, xScale: Double, xStart: Double, spec: ImPlotSpec) =
        Jni.plotScatterValues(labelId, values, values.size, xScale, xStart, encodeSpec(spec))

    actual fun plotStairs(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec) =
        Jni.plotStairs(labelId, xs, ys, minOf(xs.size, ys.size), encodeSpec(spec))

    actual fun plotBars(labelId: String, xs: FloatArray, ys: FloatArray, barSize: Double, spec: ImPlotSpec) =
        Jni.plotBars(labelId, xs, ys, minOf(xs.size, ys.size), barSize, encodeSpec(spec))

    actual fun plotBars(labelId: String, values: FloatArray, barSize: Double, shift: Double, spec: ImPlotSpec) =
        Jni.plotBarsValues(labelId, values, values.size, barSize, shift, encodeSpec(spec))

    actual fun plotHistogram(
        labelId: String,
        values: FloatArray,
        bins: Int,
        barScale: Double,
        rangeMin: Double,
        rangeMax: Double,
        spec: ImPlotSpec,
    ): Double = Jni.plotHistogram(labelId, values, values.size, bins, barScale, rangeMin, rangeMax, encodeSpec(spec))

    actual fun plotInfLines(labelId: String, values: FloatArray, spec: ImPlotSpec) =
        Jni.plotInfLines(labelId, values, values.size, encodeSpec(spec))

    actual fun plotShaded(labelId: String, xs: FloatArray, ys: FloatArray, yRef: Double, spec: ImPlotSpec) =
        Jni.plotShaded(labelId, xs, ys, minOf(xs.size, ys.size), yRef, encodeSpec(spec))

    actual fun plotText(text: String, x: Double, y: Double, pixOffset: ImVec2) =
        Jni.plotText(text, x, y, pixOffset.x, pixOffset.y)

    actual fun plotDummy(labelId: String, spec: ImPlotSpec) = Jni.plotDummy(labelId, encodeSpec(spec))

    actual fun plotBarGroups(labels: Array<String>, values: FloatArray, itemCount: Int, groupCount: Int, groupSize: Double, shift: Double, spec: ImPlotSpec) =
        Jni.plotBarGroups(labels, values, itemCount, groupCount, groupSize, shift, encodeSpec(spec))

    actual fun plotErrorBars(labelId: String, xs: FloatArray, ys: FloatArray, neg: FloatArray, pos: FloatArray, spec: ImPlotSpec) =
        Jni.plotErrorBars(labelId, xs, ys, neg, pos, minOf(xs.size, ys.size, neg.size, pos.size), encodeSpec(spec))

    actual fun plotStems(labelId: String, xs: FloatArray, ys: FloatArray, ref: Double, spec: ImPlotSpec) =
        Jni.plotStems(labelId, xs, ys, minOf(xs.size, ys.size), ref, encodeSpec(spec))

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
    ) = Jni.plotHeatmap(labelId, values, rows, cols, scaleMin, scaleMax, labelFormat, boundsMinX, boundsMinY, boundsMaxX, boundsMaxY, encodeSpec(spec))

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
    ): Double = Jni.plotHistogram2D(labelId, xs, ys, minOf(xs.size, ys.size), xBins, yBins, rangeXMin, rangeXMax, rangeYMin, rangeYMax, encodeSpec(spec))

    actual fun plotDigital(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec) =
        Jni.plotDigital(labelId, xs, ys, minOf(xs.size, ys.size), encodeSpec(spec))

    actual fun plotPieChart(labels: Array<String>, values: FloatArray, x: Double, y: Double, radius: Double, labelFormat: String, angle0: Double, spec: ImPlotSpec) =
        Jni.plotPieChart(labels, values, values.size, x, y, radius, labelFormat, angle0, encodeSpec(spec))

    actual fun plotBubbles(labelId: String, xs: FloatArray, ys: FloatArray, sizes: FloatArray, spec: ImPlotSpec) =
        Jni.plotBubbles(labelId, xs, ys, sizes, minOf(xs.size, ys.size, sizes.size), encodeSpec(spec))

    actual fun plotPolygon(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec) =
        Jni.plotPolygon(labelId, xs, ys, minOf(xs.size, ys.size), encodeSpec(spec))

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
    ) = Jni.plotImage(labelId, texId, xMin, yMin, xMax, yMax, uvMin.x, uvMin.y, uvMax.x, uvMax.y, tintCol.x, tintCol.y, tintCol.z, tintCol.w, encodeSpec(spec))

    actual fun beginSubplots(titleId: String, rows: Int, cols: Int, size: ImVec2, flags: Int): Boolean =
        Jni.beginSubplots(titleId, rows, cols, size.x, size.y, flags)

    actual fun endSubplots() = Jni.endSubplots()

    actual fun dragPoint(id: Int, x: DoubleArray, y: DoubleArray, col: ImVec4, size: Float, flags: Int): Boolean =
        Jni.dragPoint(id, x, y, col.x, col.y, col.z, col.w, size, flags)

    actual fun dragLineX(id: Int, x: DoubleArray, col: ImVec4, thickness: Float, flags: Int): Boolean =
        Jni.dragLineX(id, x, col.x, col.y, col.z, col.w, thickness, flags)

    actual fun dragLineY(id: Int, y: DoubleArray, col: ImVec4, thickness: Float, flags: Int): Boolean =
        Jni.dragLineY(id, y, col.x, col.y, col.z, col.w, thickness, flags)

    actual fun dragRect(id: Int, xMin: DoubleArray, yMin: DoubleArray, xMax: DoubleArray, yMax: DoubleArray, col: ImVec4, flags: Int): Boolean =
        Jni.dragRect(id, xMin, yMin, xMax, yMax, col.x, col.y, col.z, col.w, flags)

    actual fun annotation(x: Double, y: Double, col: ImVec4, pixOffset: ImVec2, clamp: Boolean, round: Boolean, fmt: String?) =
        Jni.annotation(x, y, col.x, col.y, col.z, col.w, pixOffset.x, pixOffset.y, clamp, round, fmt)

    actual fun tagX(x: Double, col: ImVec4, round: Boolean, fmt: String?) =
        Jni.tagX(x, col.x, col.y, col.z, col.w, round, fmt)

    actual fun tagY(y: Double, col: ImVec4, round: Boolean, fmt: String?) =
        Jni.tagY(y, col.x, col.y, col.z, col.w, round, fmt)

    actual fun getPlotLimits(): DoubleArray = Jni.getPlotLimits()
    actual fun getPlotMousePos(): DoubleArray = Jni.getPlotMousePos()
    actual fun pixelsToPlot(pixX: Float, pixY: Float): DoubleArray = Jni.pixelsToPlot(pixX, pixY)

    actual fun plotToPixels(x: Double, y: Double): ImVec2 {
        val v = Jni.plotToPixels(x, y)
        return ImVec2(v[0], v[1])
    }

    actual fun getPlotDrawList(): ImDrawList = JvmImDrawList(Jni.getPlotDrawList())

    actual fun nextColormapColor(): ImVec4 {
        val v = Jni.nextColormapColor()
        return ImVec4(v[0], v[1], v[2], v[3])
    }

    actual fun getColormapCount(): Int = Jni.getColormapCount()
    actual fun getColormapName(idx: Int): String = Jni.getColormapName(idx)

    actual fun getColormapColor(idx: Int, cmap: Int): ImVec4 {
        val v = Jni.getColormapColor(idx, cmap)
        return ImVec4(v[0], v[1], v[2], v[3])
    }

    actual fun sampleColormap(t: Float, cmap: Int): ImVec4 {
        val v = Jni.sampleColormap(t, cmap)
        return ImVec4(v[0], v[1], v[2], v[3])
    }

    actual fun colormapButton(label: String, size: ImVec2, cmap: Int): Boolean =
        Jni.colormapButton(label, size.x, size.y, cmap)

    actual fun colormapScale(label: String, scaleMin: Double, scaleMax: Double, size: ImVec2, fmt: String, flags: Int, cmap: Int) =
        Jni.colormapScale(label, scaleMin, scaleMax, size.x, size.y, fmt, flags, cmap)

    actual fun colormapSlider(label: String, t: FloatArray, out: FloatArray?, fmt: String, cmap: Int): Boolean =
        Jni.colormapSlider(label, t, out, fmt, cmap)

    actual fun colormapIcon(cmap: Int) = Jni.colormapIcon(cmap)

    actual fun addColormap(name: String, cols: FloatArray): Int = Jni.addColormap(name, cols)
    actual fun itemIcon(col: Int) = Jni.itemIcon(col)
    actual fun getLastItemColor(): Int = Jni.getLastItemColor()

    actual fun setAxis(axis: Int) = Jni.setAxis(axis)
    actual fun setAxes(xAxis: Int, yAxis: Int) = Jni.setAxes(xAxis, yAxis)
    actual fun getPlotSelection(): DoubleArray = Jni.getPlotSelection()
    actual fun pushPlotClipRect(expand: Float) = Jni.pushPlotClipRect(expand)
    actual fun popPlotClipRect() = Jni.popPlotClipRect()

    actual fun beginDragDropSourcePlot(flags: Int): Boolean = Jni.beginDragDropSourcePlot(flags)
    actual fun beginDragDropSourceAxis(axis: Int, flags: Int): Boolean = Jni.beginDragDropSourceAxis(axis, flags)
    actual fun beginDragDropSourceItem(labelId: String, flags: Int): Boolean = Jni.beginDragDropSourceItem(labelId, flags)
    actual fun endDragDropSource() = Jni.endDragDropSource()
    actual fun beginDragDropTargetPlot(): Boolean = Jni.beginDragDropTargetPlot()
    actual fun beginDragDropTargetAxis(axis: Int): Boolean = Jni.beginDragDropTargetAxis(axis)
    actual fun beginDragDropTargetLegend(): Boolean = Jni.beginDragDropTargetLegend()
    actual fun endDragDropTarget() = Jni.endDragDropTarget()

    actual fun beginLegendPopup(labelId: String, mouseButton: Int): Boolean = Jni.beginLegendPopup(labelId, mouseButton)
    actual fun endLegendPopup() = Jni.endLegendPopup()

    actual fun getInputMap(): Long = Jni.getInputMap()
    actual fun showInputMapSelector(label: String): Boolean = Jni.showInputMapSelector(label)
    actual fun showMetricsWindow(pOpen: BooleanArray?) = Jni.showMetricsWindow(pOpen)
    actual fun showStyleEditor() = Jni.showStyleEditor()
    actual fun showStyleSelector(label: String): Boolean = Jni.showStyleSelector(label)
    actual fun showColormapSelector(label: String): Boolean = Jni.showColormapSelector(label)

    actual fun pushStyleColor(idx: Int, color: ImVec4) = Jni.pushStyleColorVec4(idx, color.x, color.y, color.z, color.w)
    actual fun popStyleColor(count: Int) = Jni.popStyleColor(count)
    actual fun pushStyleVar(idx: Int, value: Float) = Jni.pushStyleVarFloat(idx, value)
    actual fun pushStyleVarFloat(idx: Int, value: Float) = Jni.pushStyleVarFloat(idx, value)
    actual fun pushStyleVarInt(idx: Int, value: Int) = Jni.pushStyleVarInt(idx, value)
    actual fun pushStyleVarVec2(idx: Int, value: ImVec2) = Jni.pushStyleVarVec2(idx, value.x, value.y)
    actual fun popStyleVar(count: Int) = Jni.popStyleVar(count)
    actual fun pushColormap(cmap: Int) = Jni.pushColormap(cmap)
    actual fun popColormap(count: Int) = Jni.popColormap(count)

    actual fun isPlotHovered(): Boolean = Jni.isPlotHovered()
    actual fun isPlotSelected(): Boolean = Jni.isPlotSelected()
    actual fun isAxisHovered(axis: Int): Boolean = Jni.isAxisHovered(axis)
    actual fun getPlotPos(): ImVec2 {
        val v = Jni.getPlotPos()
        return ImVec2(v[0], v[1])
    }

    actual fun getPlotSize(): ImVec2 {
        val v = Jni.getPlotSize()
        return ImVec2(v[0], v[1])
    }
}
