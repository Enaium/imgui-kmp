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

internal expect object Jni {

    fun createContext(): Long
    fun destroyContext(ptr: Long)
    fun getCurrentContext(): Long
    fun setImGuiContext(ctx: Long)
    fun showDemoWindow(pOpen: BooleanArray?)

    fun beginPlot(titleId: String, sizeX: Float, sizeY: Float, flags: Int): Boolean
    fun endPlot()
    fun setupAxes(xLabel: String?, yLabel: String?, xFlags: Int, yFlags: Int)
    fun setupAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, cond: Int)
    fun setupAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int)
    fun setupLegend(location: Int, flags: Int)
    fun setupFinish()
    fun setupAxis(axis: Int, label: String?, flags: Int)
    fun setupAxisFormat(axis: Int, fmt: String)
    fun setupAxisLimitsConstraints(axis: Int, vMin: Double, vMax: Double)
    fun setupAxisZoomConstraints(axis: Int, zMin: Double, zMax: Double)
    fun setupAxisLinks(axis: Int, linkMin: DoubleArray?, linkMax: DoubleArray?)
    fun setupAxisScale(axis: Int, scale: Int)
    fun setupAxisTicks(axis: Int, values: DoubleArray, labels: Array<String>?, tickCount: Int, keepDefault: Boolean)
    fun setupMouseText(location: Int, flags: Int)
    fun setNextAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, cond: Int)
    fun setNextAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int)

    fun plotLine(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    fun plotLineValues(labelId: String, values: FloatArray, count: Int, xscale: Double, xstart: Double, spec: FloatArray?)
    fun plotScatter(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    fun plotScatterValues(labelId: String, values: FloatArray, count: Int, xscale: Double, xstart: Double, spec: FloatArray?)
    fun plotStairs(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    fun plotBars(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, barSize: Double, spec: FloatArray?)
    fun plotBarsValues(labelId: String, values: FloatArray, count: Int, barSize: Double, shift: Double, spec: FloatArray?)
    fun plotHistogram(labelId: String, values: FloatArray, count: Int, bins: Int, barScale: Double, rangeMin: Double, rangeMax: Double, spec: FloatArray?): Double
    fun plotInfLines(labelId: String, values: FloatArray, count: Int, spec: FloatArray?)
    fun plotShaded(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, yref: Double, spec: FloatArray?)
    fun plotText(text: String, x: Double, y: Double, pixX: Float, pixY: Float)
    fun plotDummy(labelId: String, spec: FloatArray?)

    fun plotBarGroups(labels: Array<String>, values: FloatArray, itemCount: Int, groupCount: Int, groupSize: Double, shift: Double, spec: FloatArray?)
    fun plotErrorBars(labelId: String, xs: FloatArray, ys: FloatArray, neg: FloatArray, pos: FloatArray, count: Int, spec: FloatArray?)
    fun plotStems(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, ref: Double, spec: FloatArray?)
    fun plotHeatmap(labelId: String, values: FloatArray, rows: Int, cols: Int, scaleMin: Double, scaleMax: Double, labelFormat: String, boundsMinX: Double, boundsMinY: Double, boundsMaxX: Double, boundsMaxY: Double, spec: FloatArray?)
    fun plotHistogram2D(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, xBins: Int, yBins: Int, rangeXMin: Double, rangeXMax: Double, rangeYMin: Double, rangeYMax: Double, spec: FloatArray?): Double
    fun plotDigital(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    fun plotPieChart(labels: Array<String>, values: FloatArray, count: Int, x: Double, y: Double, radius: Double, labelFormat: String, angle0: Double, spec: FloatArray?)
    fun plotBubbles(labelId: String, xs: FloatArray, ys: FloatArray, sizes: FloatArray, count: Int, spec: FloatArray?)
    fun plotPolygon(labelId: String, xs: FloatArray, ys: FloatArray, count: Int, spec: FloatArray?)
    fun plotImage(labelId: String, texId: Long, xMin: Double, yMin: Double, xMax: Double, yMax: Double, uvMinX: Float, uvMinY: Float, uvMaxX: Float, uvMaxY: Float, tintR: Float, tintG: Float, tintB: Float, tintA: Float, spec: FloatArray?)

    fun beginSubplots(titleId: String, rows: Int, cols: Int, sizeX: Float, sizeY: Float, flags: Int): Boolean
    fun endSubplots()

    fun dragPoint(id: Int, x: DoubleArray, y: DoubleArray, colR: Float, colG: Float, colB: Float, colA: Float, size: Float, flags: Int): Boolean
    fun dragLineX(id: Int, x: DoubleArray, colR: Float, colG: Float, colB: Float, colA: Float, thickness: Float, flags: Int): Boolean
    fun dragLineY(id: Int, y: DoubleArray, colR: Float, colG: Float, colB: Float, colA: Float, thickness: Float, flags: Int): Boolean
    fun dragRect(id: Int, xMin: DoubleArray, yMin: DoubleArray, xMax: DoubleArray, yMax: DoubleArray, colR: Float, colG: Float, colB: Float, colA: Float, flags: Int): Boolean
    fun annotation(x: Double, y: Double, colR: Float, colG: Float, colB: Float, colA: Float, pixX: Float, pixY: Float, clamp: Boolean, round: Boolean, fmt: String?)
    fun tagX(x: Double, colR: Float, colG: Float, colB: Float, colA: Float, round: Boolean, fmt: String?)
    fun tagY(y: Double, colR: Float, colG: Float, colB: Float, colA: Float, round: Boolean, fmt: String?)

    fun getPlotLimits(): DoubleArray
    fun getPlotMousePos(): DoubleArray
    fun pixelsToPlot(pixX: Float, pixY: Float): DoubleArray
    fun plotToPixels(x: Double, y: Double): FloatArray
    fun getPlotDrawList(): Long
    fun nextColormapColor(): FloatArray

    fun getColormapCount(): Int
    fun getColormapName(idx: Int): String
    fun getColormapColor(idx: Int, cmap: Int): FloatArray
    fun sampleColormap(t: Float, cmap: Int): FloatArray
    fun colormapButton(label: String, sizeX: Float, sizeY: Float, cmap: Int): Boolean
    fun colormapScale(label: String, scaleMin: Double, scaleMax: Double, sizeX: Float, sizeY: Float, fmt: String, flags: Int, cmap: Int)
    fun colormapSlider(label: String, t: FloatArray, out: FloatArray?, fmt: String, cmap: Int): Boolean
    fun colormapIcon(cmap: Int)

    fun addColormap(name: String, cols: FloatArray): Int
    fun itemIcon(col: Int)
    fun getLastItemColor(): Int

    fun setAxis(axis: Int)
    fun setAxes(xAxis: Int, yAxis: Int)
    fun getPlotSelection(): DoubleArray
    fun pushPlotClipRect(expand: Float)
    fun popPlotClipRect()

    fun beginDragDropSourcePlot(flags: Int): Boolean
    fun beginDragDropSourceAxis(axis: Int, flags: Int): Boolean
    fun beginDragDropSourceItem(labelId: String, flags: Int): Boolean
    fun endDragDropSource()
    fun beginDragDropTargetPlot(): Boolean
    fun beginDragDropTargetAxis(axis: Int): Boolean
    fun beginDragDropTargetLegend(): Boolean
    fun endDragDropTarget()

    fun beginLegendPopup(labelId: String, mouseButton: Int): Boolean
    fun endLegendPopup()

    fun getInputMap(): Long
    fun showInputMapSelector(label: String): Boolean
    fun showMetricsWindow(pOpen: BooleanArray?)
    fun showStyleEditor()
    fun showStyleSelector(label: String): Boolean
    fun showColormapSelector(label: String): Boolean

    fun pushStyleColorVec4(idx: Int, r: Float, g: Float, b: Float, a: Float)
    fun pushStyleColorU32(idx: Int, color: Int)
    fun popStyleColor(count: Int)
    fun pushStyleVarFloat(idx: Int, value: Float)
    fun pushStyleVarInt(idx: Int, value: Int)
    fun pushStyleVarVec2(idx: Int, x: Float, y: Float)
    fun popStyleVar(count: Int)
    fun pushColormap(cmap: Int)
    fun popColormap(count: Int)

    fun isPlotHovered(): Boolean
    fun isPlotSelected(): Boolean
    fun isAxisHovered(axis: Int): Boolean
    fun getPlotPos(): FloatArray
    fun getPlotSize(): FloatArray
}
