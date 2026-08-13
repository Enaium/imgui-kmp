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

import cn.enaium.imgui.ImGuiContext
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
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

    actual fun pushStyleColor(idx: Int, color: ImVec4) = Jni.pushStyleColorVec4(idx, color.x, color.y, color.z, color.w)
    actual fun popStyleColor(count: Int) = Jni.popStyleColor(count)
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
