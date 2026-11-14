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
import cn.enaium.imgui.ImTextureID
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import cn.enaium.imgui.JniImDrawList
import cn.enaium.imgui.JniImGuiContext

// =========================================================================
// JNI bridge
// =========================================================================

internal actual object Jni {
    init {
        cn.enaium.imgui.NativeLoader.load()
    }

    actual external fun createContext(): Long
    actual external fun destroyContext(ptr: Long)
    actual external fun getCurrentContext(): Long
    actual external fun setImGuiContext(ctx: Long)
    actual external fun showDemoWindow(pOpen: BooleanArray?)

    actual external fun beginPlot(titleId: String, sizeX: Float, sizeY: Float, flags: Int): Boolean
    actual external fun endPlot()
    actual external fun setupAxes(xLabel: String?, yLabel: String?, xFlags: Int, yFlags: Int)
    actual external fun setupAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, cond: Int)
    actual external fun setupAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int)
    actual external fun setupLegend(location: Int, flags: Int)
    actual external fun setupFinish()
    actual external fun setupAxis(axis: Int, label: String?, flags: Int)
    actual external fun setupAxisFormat(axis: Int, fmt: String)
    actual external fun setupAxisLimitsConstraints(axis: Int, vMin: Double, vMax: Double)
    actual external fun setupAxisZoomConstraints(axis: Int, zMin: Double, zMax: Double)
    actual external fun setupAxisLinks(axis: Int, linkMin: DoubleArray?, linkMax: DoubleArray?)
    actual external fun setupAxisScale(axis: Int, scale: Int)
    actual external fun setupAxisTicks(axis: Int, values: DoubleArray, labels: Array<String>?, tickCount: Int, keepDefault: Boolean)
    actual external fun setupMouseText(location: Int, flags: Int)
    actual external fun setNextAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, cond: Int)
    actual external fun setNextAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int)

    actual external fun plotLine(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    actual external fun plotLineValues(labelId: String, values: FloatArray, count: Int, xscale: Double, xstart: Double, spec: FloatArray?)
    actual external fun plotScatter(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    actual external fun plotScatterValues(labelId: String, values: FloatArray, count: Int, xscale: Double, xstart: Double, spec: FloatArray?)
    actual external fun plotStairs(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    actual external fun plotBars(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, barSize: Double, spec: FloatArray?)
    actual external fun plotBarsValues(labelId: String, values: FloatArray, count: Int, barSize: Double, shift: Double, spec: FloatArray?)
    actual external fun plotHistogram(labelId: String, values: FloatArray, count: Int, bins: Int, barScale: Double, rangeMin: Double, rangeMax: Double, spec: FloatArray?): Double
    actual external fun plotInfLines(labelId: String, values: FloatArray, count: Int, spec: FloatArray?)
    actual external fun plotShaded(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, yref: Double, spec: FloatArray?)
    actual external fun plotText(text: String, x: Double, y: Double, pixX: Float, pixY: Float)
    actual external fun plotDummy(labelId: String, spec: FloatArray?)

    actual external fun plotBarGroups(labels: Array<String>, values: FloatArray, itemCount: Int, groupCount: Int, groupSize: Double, shift: Double, spec: FloatArray?)
    actual external fun plotErrorBars(labelId: String, xs: FloatArray, ys: FloatArray, neg: FloatArray, pos: FloatArray, count: Int, spec: FloatArray?)
    actual external fun plotStems(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, ref: Double, spec: FloatArray?)
    actual external fun plotHeatmap(labelId: String, values: FloatArray, rows: Int, cols: Int, scaleMin: Double, scaleMax: Double, labelFormat: String, boundsMinX: Double, boundsMinY: Double, boundsMaxX: Double, boundsMaxY: Double, spec: FloatArray?)
    actual external fun plotHistogram2D(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, xBins: Int, yBins: Int, rangeXMin: Double, rangeXMax: Double, rangeYMin: Double, rangeYMax: Double, spec: FloatArray?): Double
    actual external fun plotDigital(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    actual external fun plotPieChart(labels: Array<String>, values: FloatArray, count: Int, x: Double, y: Double, radius: Double, labelFormat: String, angle0: Double, spec: FloatArray?)
    actual external fun plotBubbles(labelId: String, xs: FloatArray, ys: FloatArray, sizes: FloatArray, count: Int, spec: FloatArray?)
    actual external fun plotPolygon(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    actual external fun plotImage(labelId: String, texId: Long, xMin: Double, yMin: Double, xMax: Double, yMax: Double, uvMinX: Float, uvMinY: Float, uvMaxX: Float, uvMaxY: Float, tintR: Float, tintG: Float, tintB: Float, tintA: Float, spec: FloatArray?)

    actual external fun beginSubplots(titleId: String, rows: Int, cols: Int, sizeX: Float, sizeY: Float, flags: Int): Boolean
    actual external fun endSubplots()

    actual external fun dragPoint(id: Int, x: DoubleArray, y: DoubleArray, colR: Float, colG: Float, colB: Float, colA: Float, size: Float, flags: Int): Boolean
    actual external fun dragLineX(id: Int, x: DoubleArray, colR: Float, colG: Float, colB: Float, colA: Float, thickness: Float, flags: Int): Boolean
    actual external fun dragLineY(id: Int, y: DoubleArray, colR: Float, colG: Float, colB: Float, colA: Float, thickness: Float, flags: Int): Boolean
    actual external fun dragRect(id: Int, xMin: DoubleArray, yMin: DoubleArray, xMax: DoubleArray, yMax: DoubleArray, colR: Float, colG: Float, colB: Float, colA: Float, flags: Int): Boolean
    actual external fun annotation(x: Double, y: Double, colR: Float, colG: Float, colB: Float, colA: Float, pixX: Float, pixY: Float, clamp: Boolean, round: Boolean, fmt: String?)
    actual external fun tagX(x: Double, colR: Float, colG: Float, colB: Float, colA: Float, round: Boolean, fmt: String?)
    actual external fun tagY(y: Double, colR: Float, colG: Float, colB: Float, colA: Float, round: Boolean, fmt: String?)

    actual external fun getPlotLimits(): DoubleArray
    actual external fun getPlotMousePos(): DoubleArray
    actual external fun pixelsToPlot(pixX: Float, pixY: Float): DoubleArray
    actual external fun plotToPixels(x: Double, y: Double): FloatArray
    actual external fun getPlotDrawList(): Long
    actual external fun nextColormapColor(): FloatArray

    actual external fun getColormapCount(): Int
    actual external fun getColormapName(idx: Int): String
    actual external fun getColormapColor(idx: Int, cmap: Int): FloatArray
    actual external fun sampleColormap(t: Float, cmap: Int): FloatArray
    actual external fun colormapButton(label: String, sizeX: Float, sizeY: Float, cmap: Int): Boolean
    actual external fun colormapScale(label: String, scaleMin: Double, scaleMax: Double, sizeX: Float, sizeY: Float, fmt: String, flags: Int, cmap: Int)
    actual external fun colormapSlider(label: String, t: FloatArray, out: FloatArray?, fmt: String, cmap: Int): Boolean
    actual external fun colormapIcon(cmap: Int)

    actual external fun addColormap(name: String, cols: FloatArray): Int
    actual external fun itemIcon(col: Int)
    actual external fun getLastItemColor(): Int

    actual external fun setAxis(axis: Int)
    actual external fun setAxes(xAxis: Int, yAxis: Int)
    actual external fun getPlotSelection(): DoubleArray
    actual external fun pushPlotClipRect(expand: Float)
    actual external fun popPlotClipRect()

    actual external fun beginDragDropSourcePlot(flags: Int): Boolean
    actual external fun beginDragDropSourceAxis(axis: Int, flags: Int): Boolean
    actual external fun beginDragDropSourceItem(labelId: String, flags: Int): Boolean
    actual external fun endDragDropSource()
    actual external fun beginDragDropTargetPlot(): Boolean
    actual external fun beginDragDropTargetAxis(axis: Int): Boolean
    actual external fun beginDragDropTargetLegend(): Boolean
    actual external fun endDragDropTarget()

    actual external fun beginLegendPopup(labelId: String, mouseButton: Int): Boolean
    actual external fun endLegendPopup()

    actual external fun getInputMap(): Long
    actual external fun showInputMapSelector(label: String): Boolean
    actual external fun showMetricsWindow(pOpen: BooleanArray?)
    actual external fun showStyleEditor()
    actual external fun showStyleSelector(label: String): Boolean
    actual external fun showColormapSelector(label: String): Boolean

    actual external fun pushStyleColorVec4(idx: Int, r: Float, g: Float, b: Float, a: Float)
    actual external fun pushStyleColorU32(idx: Int, color: Int)
    actual external fun popStyleColor(count: Int)
    actual external fun pushStyleVarFloat(idx: Int, value: Float)
    actual external fun pushStyleVarInt(idx: Int, value: Int)
    actual external fun pushStyleVarVec2(idx: Int, x: Float, y: Float)
    actual external fun popStyleVar(count: Int)
    actual external fun pushColormap(cmap: Int)
    actual external fun popColormap(count: Int)

    actual external fun isPlotHovered(): Boolean
    actual external fun isPlotSelected(): Boolean
    actual external fun isAxisHovered(axis: Int): Boolean
    actual external fun getPlotPos(): FloatArray
    actual external fun getPlotSize(): FloatArray
}
