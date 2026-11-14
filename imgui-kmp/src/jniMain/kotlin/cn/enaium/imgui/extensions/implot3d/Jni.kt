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

internal expect object Jni {

    // ==================== Context ====================
    fun createContext(): Long
    fun destroyContext(ptr: Long)
    fun getCurrentContext(): Long
    fun setCurrentContext(ctx: Long)

    // ==================== Begin/End plot ====================
    fun beginPlot(titleId: String, sizeX: Float, sizeY: Float, flags: Int): Boolean
    fun endPlot()

    // ==================== Setup ====================
    fun setupAxis(axis: Int, label: String?, flags: Int)
    fun setupAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int)
    fun setupAxisTicksValues(axis: Int, values: DoubleArray, labels: Array<String>?, tickCount: Int, keepDefault: Boolean)
    fun setupAxisTicksLimits(axis: Int, vMin: Double, vMax: Double, tickCount: Int, labels: Array<String>?, keepDefault: Boolean)
    fun setupAxisScale(axis: Int, scale: Int)
    fun setupAxisLimitsConstraints(axis: Int, vMin: Double, vMax: Double)
    fun setupAxisZoomConstraints(axis: Int, zoomMin: Double, zoomMax: Double)
    fun setupAxes(xLabel: String?, yLabel: String?, zLabel: String?, xFlags: Int, yFlags: Int, zFlags: Int)
    fun setupAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, zMin: Double, zMax: Double, cond: Int)
    fun setupBoxRotationAngles(elevation: Double, azimuth: Double, animate: Boolean, cond: Int)
    fun setupBoxRotationQuat(x: Double, y: Double, z: Double, w: Double, animate: Boolean, cond: Int)
    fun setupBoxInitialRotationAngles(elevation: Double, azimuth: Double)
    fun setupBoxInitialRotationQuat(x: Double, y: Double, z: Double, w: Double)
    fun setupBoxScale(x: Double, y: Double, z: Double)
    fun setupLegend(location: Int, flags: Int)

    // ==================== Plot items (double data arrays) ====================
    fun plotScatter(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, count: Int, spec: FloatArray?)
    fun plotLine(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, count: Int, spec: FloatArray?)
    fun plotTriangle(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, count: Int, spec: FloatArray?)
    fun plotQuad(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, count: Int, spec: FloatArray?)
    fun plotSurface(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, xCount: Int, yCount: Int, scaleMin: Double, scaleMax: Double, spec: FloatArray?)
    fun plotMesh(labelId: String, vtxXs: DoubleArray, vtxYs: DoubleArray, vtxZs: DoubleArray, idxs: IntArray, spec: FloatArray?)
    fun plotText(text: String, x: Double, y: Double, z: Double, angle: Double, pixX: Float, pixY: Float)
    fun plotDummy(labelId: String, spec: FloatArray?)

    // ==================== Plot utils ====================
    fun plotToPixelsPoint(x: Double, y: Double, z: Double): FloatArray
    fun plotToPixelsXyz(x: Double, y: Double, z: Double): FloatArray
    fun pixelsToPlotRayVec2(pixX: Float, pixY: Float): DoubleArray
    fun pixelsToPlotRayXy(x: Double, y: Double): DoubleArray
    fun pixelsToPlotPlaneVec2(pixX: Float, pixY: Float, plane: Int, mask: Boolean): DoubleArray
    fun pixelsToPlotPlaneXy(x: Double, y: Double, plane: Int, mask: Boolean): DoubleArray
    fun getPlotRectPos(): FloatArray
    fun getPlotRectSize(): FloatArray
    fun getPlotDrawList(): Long

    // ==================== Style ====================
    fun getStyle(): FloatArray
    fun setStyle(style: FloatArray)
    fun styleColorsAuto(dst: FloatArray?): FloatArray?
    fun styleColorsDark(dst: FloatArray?): FloatArray?
    fun styleColorsLight(dst: FloatArray?): FloatArray?
    fun styleColorsClassic(dst: FloatArray?): FloatArray?
    fun pushStyleColorVec4(idx: Int, r: Float, g: Float, b: Float, a: Float)
    fun pushStyleColorU32(idx: Int, color: Int)
    fun popStyleColor(count: Int)
    fun pushStyleVarFloat(idx: Int, value: Float)
    fun pushStyleVarInt(idx: Int, value: Int)
    fun pushStyleVarVec2(idx: Int, x: Float, y: Float)
    fun popStyleVar(count: Int)
    fun getStyleColor(idx: Int): FloatArray
    fun getStyleColorU32(idx: Int): Int
    fun nextMarker(): Int

    // ==================== Colormaps ====================
    fun addColormapVec4(name: String, cols: FloatArray, qual: Boolean): Int
    fun addColormapU32(name: String, cols: IntArray, qual: Boolean): Int
    fun getColormapCount(): Int
    fun getColormapName(cmap: Int): String
    fun getColormapIndex(name: String): Int
    fun pushColormap(cmap: Int)
    fun pushColormapName(name: String)
    fun popColormap(count: Int)
    fun nextColormapColor(): FloatArray
    fun getColormapSize(cmap: Int): Int
    fun getColormapColor(idx: Int, cmap: Int): FloatArray
    fun sampleColormap(t: Float, cmap: Int): FloatArray

    // ==================== Demo ====================
    fun showDemoWindow(pOpen: BooleanArray?)
    fun showAllDemos()
    fun showStyleEditor()
    fun showStyleSelector(label: String): Boolean
    fun showColormapSelector(label: String): Boolean
    fun showMetricsWindow(pOpen: BooleanArray?)
    fun showAboutWindow(pOpen: BooleanArray?)

    // ==================== Built-in meshes ====================
    fun cubeVtx(): DoubleArray
    fun cubeIdx(): IntArray
    fun sphereVtx(): DoubleArray
    fun sphereIdx(): IntArray
    fun duckVtx(): DoubleArray
    fun duckIdx(): IntArray

    // ==================== Point math ====================
    fun pointAdd(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): DoubleArray
    fun pointSub(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): DoubleArray
    fun pointMul(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): DoubleArray
    fun pointDiv(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): DoubleArray
    fun pointMulScalar(x: Double, y: Double, z: Double, scalar: Double): DoubleArray
    fun pointDivScalar(x: Double, y: Double, z: Double, scalar: Double): DoubleArray
    fun pointNeg(x: Double, y: Double, z: Double): DoubleArray
    fun pointDot(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): Double
    fun pointCross(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): DoubleArray
    fun pointLength(x: Double, y: Double, z: Double): Double
    fun pointLengthSquared(x: Double, y: Double, z: Double): Double
    fun pointNormalized(x: Double, y: Double, z: Double): DoubleArray
    fun pointIsNaN(x: Double, y: Double, z: Double): Boolean
    fun pointEq(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): Boolean

    // ==================== Quat math ====================
    fun quatFromAngleAxis(angle: Double, ax: Double, ay: Double, az: Double): DoubleArray
    fun quatFromTwoVectors(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): DoubleArray
    fun quatFromElAz(elevation: Double, azimuth: Double): DoubleArray
    fun quatMul(ax: Double, ay: Double, az: Double, aw: Double, bx: Double, by: Double, bz: Double, bw: Double): DoubleArray
    fun quatRotatePoint(qx: Double, qy: Double, qz: Double, qw: Double, px: Double, py: Double, pz: Double): DoubleArray
    fun quatNormalized(x: Double, y: Double, z: Double, w: Double): DoubleArray
    fun quatConjugate(x: Double, y: Double, z: Double, w: Double): DoubleArray
    fun quatInverse(x: Double, y: Double, z: Double, w: Double): DoubleArray
    fun quatLength(x: Double, y: Double, z: Double, w: Double): Double
    fun quatDot(ax: Double, ay: Double, az: Double, aw: Double, bx: Double, by: Double, bz: Double, bw: Double): Double
    fun quatSlerp(ax: Double, ay: Double, az: Double, aw: Double, bx: Double, by: Double, bz: Double, bw: Double, t: Double): DoubleArray
    fun quatEq(ax: Double, ay: Double, az: Double, aw: Double, bx: Double, by: Double, bz: Double, bw: Double): Boolean
}
