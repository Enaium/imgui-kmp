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

package cn.enaium.imgui.extensions.implot3d

import cn.enaium.imgui.ImDrawList
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import cn.enaium.imgui.JniImDrawList

// =========================================================================
// JNI bridge
// =========================================================================


// =========================================================================
// JVM/Android actual implementations
// =========================================================================

internal class JniImPlot3DContext(internal val ptr: Long) : ImPlot3DContext {
    override fun close() {
        Jni.destroyContext(ptr)
    }
}

// ImPlot3DSpec float-array encoding (34 floats, identical in jni_bridge.cpp):
// for each of the 11 optional groups a "set" flag followed by its value
// slots (vec4 groups = 4 slots). Order: line_color(4), line_weight(1),
// fill_color(4), fill_alpha(1), marker(1), marker_size(1),
// marker_line_color(4), marker_fill_color(4), offset(1), stride(1), flags(1).
internal fun encodeSpec3d(spec: ImPlot3DSpec): FloatArray? {
    if (spec == ImPlot3DSpec()) return null

    // [setFlag, value..., setFlag, value..., ...]
    val out = ArrayList<Float>(34)
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
    out.add(if (spec.offset != null) 1f else 0f); out.add((spec.offset ?: 0).toFloat())
    out.add(if (spec.stride != null) 1f else 0f); out.add((spec.stride ?: 0).toFloat())
    out.add(if (spec.flags != null) 1f else 0f); out.add((spec.flags ?: 0).toFloat())
    return out.toFloatArray()
}

// ImPlot3DStyle float-array layout (76 floats, identical in jni_bridge.cpp):
// lineWeight, marker, markerSize, fillAlpha,
// plotDefaultSize.x/y, plotMinSize.x/y, plotPadding.x/y, labelPadding.x/y,
// legendPadding.x/y, legendInnerPadding.x/y, legendSpacing.x/y,
// viewScaleFactor, colors[14] (x/y/z/w interleaved), colormap.
private const val STYLE_FLOAT_COUNT = 76

internal fun decodeStyle3d(data: FloatArray): ImPlot3DStyle {
    var i = 0
    val lineWeight = data[i++]
    val marker = data[i++].toInt()
    val markerSize = data[i++]
    val fillAlpha = data[i++]
    fun readVec2() = ImVec2(data[i++], data[i++])
    val plotDefaultSize = readVec2()
    val plotMinSize = readVec2()
    val plotPadding = readVec2()
    val labelPadding = readVec2()
    val legendPadding = readVec2()
    val legendInnerPadding = readVec2()
    val legendSpacing = readVec2()
    val viewScaleFactor = data[i++]
    val colors = Array(ImPlot3DCol.COUNT) { ImVec4(data[i++], data[i++], data[i++], data[i++]) }
    val colormap = data[i].toInt()
    return ImPlot3DStyle(
        lineWeight,
        marker,
        markerSize,
        fillAlpha,
        plotDefaultSize,
        plotMinSize,
        plotPadding,
        labelPadding,
        viewScaleFactor,
        legendPadding,
        legendInnerPadding,
        legendSpacing,
        colors,
        colormap,
    )
}

internal fun encodeStyle3d(style: ImPlot3DStyle): FloatArray {
    val out = FloatArray(STYLE_FLOAT_COUNT)
    var i = 0
    out[i++] = style.lineWeight
    out[i++] = style.marker.toFloat()
    out[i++] = style.markerSize
    out[i++] = style.fillAlpha
    fun writeVec2(v: ImVec2) {
        out[i++] = v.x
        out[i++] = v.y
    }
    writeVec2(style.plotDefaultSize)
    writeVec2(style.plotMinSize)
    writeVec2(style.plotPadding)
    writeVec2(style.labelPadding)
    writeVec2(style.legendPadding)
    writeVec2(style.legendInnerPadding)
    writeVec2(style.legendSpacing)
    out[i++] = style.viewScaleFactor
    for (c in style.colors) {
        out[i++] = c.x
        out[i++] = c.y
        out[i++] = c.z
        out[i++] = c.w
    }
    out[i] = style.colormap.toFloat()
    return out
}

private fun decodePoint3d(v: DoubleArray): ImPlot3DPoint = ImPlot3DPoint(v[0], v[1], v[2])

private fun decodeQuat3d(v: DoubleArray): ImPlot3DQuat = ImPlot3DQuat(v[0], v[1], v[2], v[3])

private fun decodePoints3d(v: DoubleArray): Array<ImPlot3DPoint> =
    Array(v.size / 3) { ImPlot3DPoint(v[it * 3], v[it * 3 + 1], v[it * 3 + 2]) }

actual object ImPlot3D {
    // ==================== Context ====================
    actual fun createContext(): ImPlot3DContext = JniImPlot3DContext(Jni.createContext())

    actual fun destroyContext(context: ImPlot3DContext?) {
        Jni.destroyContext(if (context != null) (context as JniImPlot3DContext).ptr else 0L)
    }

    actual fun getCurrentContext(): ImPlot3DContext? {
        val ptr = Jni.getCurrentContext()
        return if (ptr != 0L) JniImPlot3DContext(ptr) else null
    }

    actual fun setCurrentContext(context: ImPlot3DContext?) {
        Jni.setCurrentContext(if (context != null) (context as JniImPlot3DContext).ptr else 0L)
    }

    // ==================== Begin/End plot ====================
    actual fun beginPlot(titleId: String, size: ImVec2, flags: Int): Boolean =
        Jni.beginPlot(titleId, size.x, size.y, flags)

    actual fun endPlot() = Jni.endPlot()

    // ==================== Setup ====================
    actual fun setupAxis(axis: Int, label: String?, flags: Int) = Jni.setupAxis(axis, label, flags)

    actual fun setupAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int) =
        Jni.setupAxisLimits(axis, vMin, vMax, cond)

    actual fun setupAxisTicks(axis: Int, values: DoubleArray, labels: Array<String>?, tickCount: Int, keepDefault: Boolean) =
        Jni.setupAxisTicksValues(axis, values, labels, if (tickCount >= 0) tickCount else values.size, keepDefault)

    actual fun setupAxisTicks(axis: Int, vMin: Double, vMax: Double, tickCount: Int, labels: Array<String>?, keepDefault: Boolean) =
        Jni.setupAxisTicksLimits(axis, vMin, vMax, tickCount, labels, keepDefault)

    actual fun setupAxisScale(axis: Int, scale: Int) = Jni.setupAxisScale(axis, scale)

    actual fun setupAxisLimitsConstraints(axis: Int, vMin: Double, vMax: Double) =
        Jni.setupAxisLimitsConstraints(axis, vMin, vMax)

    actual fun setupAxisZoomConstraints(axis: Int, zoomMin: Double, zoomMax: Double) =
        Jni.setupAxisZoomConstraints(axis, zoomMin, zoomMax)

    actual fun setupAxes(xLabel: String?, yLabel: String?, zLabel: String?, xFlags: Int, yFlags: Int, zFlags: Int) =
        Jni.setupAxes(xLabel, yLabel, zLabel, xFlags, yFlags, zFlags)

    actual fun setupAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, zMin: Double, zMax: Double, cond: Int) =
        Jni.setupAxesLimits(xMin, xMax, yMin, yMax, zMin, zMax, cond)

    actual fun setupBoxRotation(elevation: Double, azimuth: Double, animate: Boolean, cond: Int) =
        Jni.setupBoxRotationAngles(elevation, azimuth, animate, cond)

    actual fun setupBoxRotation(rotation: ImPlot3DQuat, animate: Boolean, cond: Int) =
        Jni.setupBoxRotationQuat(rotation.x, rotation.y, rotation.z, rotation.w, animate, cond)

    actual fun setupBoxInitialRotation(elevation: Double, azimuth: Double) =
        Jni.setupBoxInitialRotationAngles(elevation, azimuth)

    actual fun setupBoxInitialRotation(rotation: ImPlot3DQuat) =
        Jni.setupBoxInitialRotationQuat(rotation.x, rotation.y, rotation.z, rotation.w)

    actual fun setupBoxScale(x: Double, y: Double, z: Double) = Jni.setupBoxScale(x, y, z)

    actual fun setupLegend(location: Int, flags: Int) = Jni.setupLegend(location, flags)

    // ==================== Plot items (double data arrays) ====================
    actual fun plotScatter(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, spec: ImPlot3DSpec) =
        Jni.plotScatter(labelId, xs, ys, zs, minOf(xs.size, ys.size, zs.size), encodeSpec3d(spec))

    actual fun plotLine(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, spec: ImPlot3DSpec) =
        Jni.plotLine(labelId, xs, ys, zs, minOf(xs.size, ys.size, zs.size), encodeSpec3d(spec))

    actual fun plotTriangle(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, spec: ImPlot3DSpec) =
        Jni.plotTriangle(labelId, xs, ys, zs, minOf(xs.size, ys.size, zs.size), encodeSpec3d(spec))

    actual fun plotQuad(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, spec: ImPlot3DSpec) =
        Jni.plotQuad(labelId, xs, ys, zs, minOf(xs.size, ys.size, zs.size), encodeSpec3d(spec))

    actual fun plotSurface(
        labelId: String,
        xs: DoubleArray,
        ys: DoubleArray,
        zs: DoubleArray,
        xCount: Int,
        yCount: Int,
        scaleMin: Double,
        scaleMax: Double,
        spec: ImPlot3DSpec,
    ) = Jni.plotSurface(labelId, xs, ys, zs, xCount, yCount, scaleMin, scaleMax, encodeSpec3d(spec))

    actual fun plotMesh(
        labelId: String,
        vtxXs: DoubleArray,
        vtxYs: DoubleArray,
        vtxZs: DoubleArray,
        idxs: IntArray,
        spec: ImPlot3DSpec,
    ) = Jni.plotMesh(labelId, vtxXs, vtxYs, vtxZs, idxs, encodeSpec3d(spec))

    actual fun plotText(text: String, x: Double, y: Double, z: Double, angle: Double, pixOffset: ImVec2) =
        Jni.plotText(text, x, y, z, angle, pixOffset.x, pixOffset.y)

    actual fun plotDummy(labelId: String, spec: ImPlot3DSpec) = Jni.plotDummy(labelId, encodeSpec3d(spec))

    // ==================== Plot utils ====================
    actual fun plotToPixels(point: ImPlot3DPoint): ImVec2 {
        val v = Jni.plotToPixelsPoint(point.x, point.y, point.z)
        return ImVec2(v[0], v[1])
    }

    actual fun plotToPixels(x: Double, y: Double, z: Double): ImVec2 {
        val v = Jni.plotToPixelsXyz(x, y, z)
        return ImVec2(v[0], v[1])
    }

    actual fun pixelsToPlotRay(pix: ImVec2): ImPlot3DRay {
        val v = Jni.pixelsToPlotRayVec2(pix.x, pix.y)
        return ImPlot3DRay(ImPlot3DPoint(v[0], v[1], v[2]), ImPlot3DPoint(v[3], v[4], v[5]))
    }

    actual fun pixelsToPlotRay(x: Double, y: Double): ImPlot3DRay {
        val v = Jni.pixelsToPlotRayXy(x, y)
        return ImPlot3DRay(ImPlot3DPoint(v[0], v[1], v[2]), ImPlot3DPoint(v[3], v[4], v[5]))
    }

    actual fun pixelsToPlotPlane(pix: ImVec2, plane: Int, mask: Boolean): ImPlot3DPoint {
        val v = Jni.pixelsToPlotPlaneVec2(pix.x, pix.y, plane, mask)
        return ImPlot3DPoint(v[0], v[1], v[2])
    }

    actual fun pixelsToPlotPlane(x: Double, y: Double, plane: Int, mask: Boolean): ImPlot3DPoint {
        val v = Jni.pixelsToPlotPlaneXy(x, y, plane, mask)
        return ImPlot3DPoint(v[0], v[1], v[2])
    }

    actual fun getPlotRectPos(): ImVec2 {
        val v = Jni.getPlotRectPos()
        return ImVec2(v[0], v[1])
    }

    actual fun getPlotRectSize(): ImVec2 {
        val v = Jni.getPlotRectSize()
        return ImVec2(v[0], v[1])
    }

    actual fun getPlotDrawList(): ImDrawList = JniImDrawList(Jni.getPlotDrawList())

    // ==================== Style ====================
    actual fun getStyle(): ImPlot3DStyle = decodeStyle3d(Jni.getStyle())

    actual fun setStyle(style: ImPlot3DStyle) = Jni.setStyle(encodeStyle3d(style))

    actual fun styleColorsAuto(dst: ImPlot3DStyle?): ImPlot3DStyle? =
        Jni.styleColorsAuto(dst?.let { encodeStyle3d(it) })?.let { decodeStyle3d(it) }

    actual fun styleColorsDark(dst: ImPlot3DStyle?): ImPlot3DStyle? =
        Jni.styleColorsDark(dst?.let { encodeStyle3d(it) })?.let { decodeStyle3d(it) }

    actual fun styleColorsLight(dst: ImPlot3DStyle?): ImPlot3DStyle? =
        Jni.styleColorsLight(dst?.let { encodeStyle3d(it) })?.let { decodeStyle3d(it) }

    actual fun styleColorsClassic(dst: ImPlot3DStyle?): ImPlot3DStyle? =
        Jni.styleColorsClassic(dst?.let { encodeStyle3d(it) })?.let { decodeStyle3d(it) }

    actual fun pushStyleColor(idx: Int, color: ImVec4) = Jni.pushStyleColorVec4(idx, color.x, color.y, color.z, color.w)

    actual fun pushStyleColor(idx: Int, color: Int) = Jni.pushStyleColorU32(idx, color)

    actual fun popStyleColor(count: Int) = Jni.popStyleColor(count)

    actual fun pushStyleVar(idx: Int, value: Float) = Jni.pushStyleVarFloat(idx, value)

    actual fun pushStyleVar(idx: Int, value: Int) = Jni.pushStyleVarInt(idx, value)

    actual fun pushStyleVar(idx: Int, value: ImVec2) = Jni.pushStyleVarVec2(idx, value.x, value.y)

    actual fun popStyleVar(count: Int) = Jni.popStyleVar(count)

    actual fun getStyleColor(idx: Int): ImVec4 {
        val v = Jni.getStyleColor(idx)
        return ImVec4(v[0], v[1], v[2], v[3])
    }

    actual fun getStyleColorU32(idx: Int): Int = Jni.getStyleColorU32(idx)

    actual fun nextMarker(): Int = Jni.nextMarker()

    // ==================== Colormaps ====================
    actual fun addColormap(name: String, cols: Array<ImVec4>, qual: Boolean): Int {
        val flat = FloatArray(cols.size * 4)
        for (i in cols.indices) {
            flat[i * 4] = cols[i].x
            flat[i * 4 + 1] = cols[i].y
            flat[i * 4 + 2] = cols[i].z
            flat[i * 4 + 3] = cols[i].w
        }
        return Jni.addColormapVec4(name, flat, qual)
    }

    actual fun addColormap(name: String, cols: IntArray, qual: Boolean): Int =
        Jni.addColormapU32(name, cols, qual)

    actual fun getColormapCount(): Int = Jni.getColormapCount()

    actual fun getColormapName(cmap: Int): String = Jni.getColormapName(cmap)

    actual fun getColormapIndex(name: String): Int = Jni.getColormapIndex(name)

    actual fun pushColormap(cmap: Int) = Jni.pushColormap(cmap)

    actual fun pushColormap(name: String) = Jni.pushColormapName(name)

    actual fun popColormap(count: Int) = Jni.popColormap(count)

    actual fun nextColormapColor(): ImVec4 {
        val v = Jni.nextColormapColor()
        return ImVec4(v[0], v[1], v[2], v[3])
    }

    actual fun getColormapSize(cmap: Int): Int = Jni.getColormapSize(cmap)

    actual fun getColormapColor(idx: Int, cmap: Int): ImVec4 {
        val v = Jni.getColormapColor(idx, cmap)
        return ImVec4(v[0], v[1], v[2], v[3])
    }

    actual fun sampleColormap(t: Float, cmap: Int): ImVec4 {
        val v = Jni.sampleColormap(t, cmap)
        return ImVec4(v[0], v[1], v[2], v[3])
    }

    // ==================== Demo ====================
    actual fun showDemoWindow(pOpen: BooleanArray?) = Jni.showDemoWindow(pOpen)

    actual fun showAllDemos() = Jni.showAllDemos()

    actual fun showStyleEditor() = Jni.showStyleEditor()

    actual fun showStyleSelector(label: String): Boolean = Jni.showStyleSelector(label)

    actual fun showColormapSelector(label: String): Boolean = Jni.showColormapSelector(label)

    actual fun showMetricsWindow(pOpen: BooleanArray?) = Jni.showMetricsWindow(pOpen)

    actual fun showAboutWindow(pOpen: BooleanArray?) = Jni.showAboutWindow(pOpen)

    // ==================== Built-in meshes ====================
    actual fun cubeVertices(): Array<ImPlot3DPoint> = decodePoints3d(Jni.cubeVtx())

    actual fun cubeIndices(): IntArray = Jni.cubeIdx()

    actual fun sphereVertices(): Array<ImPlot3DPoint> = decodePoints3d(Jni.sphereVtx())

    actual fun sphereIndices(): IntArray = Jni.sphereIdx()

    actual fun duckVertices(): Array<ImPlot3DPoint> = decodePoints3d(Jni.duckVtx())

    actual fun duckIndices(): IntArray = Jni.duckIdx()

    // ==================== Point math ====================
    actual fun pointAdd(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint =
        decodePoint3d(Jni.pointAdd(a.x, a.y, a.z, b.x, b.y, b.z))

    actual fun pointSub(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint =
        decodePoint3d(Jni.pointSub(a.x, a.y, a.z, b.x, b.y, b.z))

    actual fun pointMul(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint =
        decodePoint3d(Jni.pointMul(a.x, a.y, a.z, b.x, b.y, b.z))

    actual fun pointDiv(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint =
        decodePoint3d(Jni.pointDiv(a.x, a.y, a.z, b.x, b.y, b.z))

    actual fun pointMulScalar(a: ImPlot3DPoint, scalar: Double): ImPlot3DPoint =
        decodePoint3d(Jni.pointMulScalar(a.x, a.y, a.z, scalar))

    actual fun pointDivScalar(a: ImPlot3DPoint, scalar: Double): ImPlot3DPoint =
        decodePoint3d(Jni.pointDivScalar(a.x, a.y, a.z, scalar))

    actual fun pointNeg(a: ImPlot3DPoint): ImPlot3DPoint =
        decodePoint3d(Jni.pointNeg(a.x, a.y, a.z))

    actual fun pointDot(a: ImPlot3DPoint, b: ImPlot3DPoint): Double =
        Jni.pointDot(a.x, a.y, a.z, b.x, b.y, b.z)

    actual fun pointCross(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint =
        decodePoint3d(Jni.pointCross(a.x, a.y, a.z, b.x, b.y, b.z))

    actual fun pointLength(a: ImPlot3DPoint): Double = Jni.pointLength(a.x, a.y, a.z)

    actual fun pointLengthSquared(a: ImPlot3DPoint): Double = Jni.pointLengthSquared(a.x, a.y, a.z)

    actual fun pointNormalized(a: ImPlot3DPoint): ImPlot3DPoint =
        decodePoint3d(Jni.pointNormalized(a.x, a.y, a.z))

    actual fun pointIsNaN(a: ImPlot3DPoint): Boolean = Jni.pointIsNaN(a.x, a.y, a.z)

    actual fun pointEq(a: ImPlot3DPoint, b: ImPlot3DPoint): Boolean =
        Jni.pointEq(a.x, a.y, a.z, b.x, b.y, b.z)

    // ==================== Quat math ====================
    actual fun quatFromAngleAxis(angle: Double, axis: ImPlot3DPoint): ImPlot3DQuat =
        decodeQuat3d(Jni.quatFromAngleAxis(angle, axis.x, axis.y, axis.z))

    actual fun quatFromTwoVectors(v0: ImPlot3DPoint, v1: ImPlot3DPoint): ImPlot3DQuat =
        decodeQuat3d(Jni.quatFromTwoVectors(v0.x, v0.y, v0.z, v1.x, v1.y, v1.z))

    actual fun quatFromElAz(elevation: Double, azimuth: Double): ImPlot3DQuat =
        decodeQuat3d(Jni.quatFromElAz(elevation, azimuth))

    actual fun quatMul(a: ImPlot3DQuat, b: ImPlot3DQuat): ImPlot3DQuat =
        decodeQuat3d(Jni.quatMul(a.x, a.y, a.z, a.w, b.x, b.y, b.z, b.w))

    actual fun quatRotatePoint(q: ImPlot3DQuat, p: ImPlot3DPoint): ImPlot3DPoint =
        decodePoint3d(Jni.quatRotatePoint(q.x, q.y, q.z, q.w, p.x, p.y, p.z))

    actual fun quatNormalized(q: ImPlot3DQuat): ImPlot3DQuat =
        decodeQuat3d(Jni.quatNormalized(q.x, q.y, q.z, q.w))

    actual fun quatConjugate(q: ImPlot3DQuat): ImPlot3DQuat =
        decodeQuat3d(Jni.quatConjugate(q.x, q.y, q.z, q.w))

    actual fun quatInverse(q: ImPlot3DQuat): ImPlot3DQuat =
        decodeQuat3d(Jni.quatInverse(q.x, q.y, q.z, q.w))

    actual fun quatLength(q: ImPlot3DQuat): Double = Jni.quatLength(q.x, q.y, q.z, q.w)

    actual fun quatDot(a: ImPlot3DQuat, b: ImPlot3DQuat): Double =
        Jni.quatDot(a.x, a.y, a.z, a.w, b.x, b.y, b.z, b.w)

    actual fun quatSlerp(q1: ImPlot3DQuat, q2: ImPlot3DQuat, t: Double): ImPlot3DQuat =
        decodeQuat3d(Jni.quatSlerp(q1.x, q1.y, q1.z, q1.w, q2.x, q2.y, q2.z, q2.w, t))

    actual fun quatEq(a: ImPlot3DQuat, b: ImPlot3DQuat): Boolean =
        Jni.quatEq(a.x, a.y, a.z, a.w, b.x, b.y, b.z, b.w)
}