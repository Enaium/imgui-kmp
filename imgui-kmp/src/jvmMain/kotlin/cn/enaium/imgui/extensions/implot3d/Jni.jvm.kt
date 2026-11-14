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

internal actual object Jni {
    init {
        cn.enaium.imgui.NativeLoader.load()
    }

    // ==================== Context ====================
    actual external fun createContext(): Long
    actual external fun destroyContext(ptr: Long)
    actual external fun getCurrentContext(): Long
    actual external fun setCurrentContext(ctx: Long)

    // ==================== Begin/End plot ====================
    actual external fun beginPlot(titleId: String, sizeX: Float, sizeY: Float, flags: Int): Boolean
    actual external fun endPlot()

    // ==================== Setup ====================
    actual external fun setupAxis(axis: Int, label: String?, flags: Int)
    actual external fun setupAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int)
    actual external fun setupAxisTicksValues(axis: Int, values: DoubleArray, labels: Array<String>?, tickCount: Int, keepDefault: Boolean)
    actual external fun setupAxisTicksLimits(axis: Int, vMin: Double, vMax: Double, tickCount: Int, labels: Array<String>?, keepDefault: Boolean)
    actual external fun setupAxisScale(axis: Int, scale: Int)
    actual external fun setupAxisLimitsConstraints(axis: Int, vMin: Double, vMax: Double)
    actual external fun setupAxisZoomConstraints(axis: Int, zoomMin: Double, zoomMax: Double)
    actual external fun setupAxes(xLabel: String?, yLabel: String?, zLabel: String?, xFlags: Int, yFlags: Int, zFlags: Int)
    actual external fun setupAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, zMin: Double, zMax: Double, cond: Int)
    actual external fun setupBoxRotationAngles(elevation: Double, azimuth: Double, animate: Boolean, cond: Int)
    actual external fun setupBoxRotationQuat(x: Double, y: Double, z: Double, w: Double, animate: Boolean, cond: Int)
    actual external fun setupBoxInitialRotationAngles(elevation: Double, azimuth: Double)
    actual external fun setupBoxInitialRotationQuat(x: Double, y: Double, z: Double, w: Double)
    actual external fun setupBoxScale(x: Double, y: Double, z: Double)
    actual external fun setupLegend(location: Int, flags: Int)

    // ==================== Plot items (double data arrays) ====================
    actual external fun plotScatter(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, count: Int, spec: FloatArray?)
    actual external fun plotLine(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, count: Int, spec: FloatArray?)
    actual external fun plotTriangle(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, count: Int, spec: FloatArray?)
    actual external fun plotQuad(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, count: Int, spec: FloatArray?)
    actual external fun plotSurface(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, xCount: Int, yCount: Int, scaleMin: Double, scaleMax: Double, spec: FloatArray?)
    actual external fun plotMesh(labelId: String, vtxXs: DoubleArray, vtxYs: DoubleArray, vtxZs: DoubleArray, idxs: IntArray, spec: FloatArray?)
    actual external fun plotText(text: String, x: Double, y: Double, z: Double, angle: Double, pixX: Float, pixY: Float)
    actual external fun plotDummy(labelId: String, spec: FloatArray?)

    // ==================== Plot utils ====================
    actual external fun plotToPixelsPoint(x: Double, y: Double, z: Double): FloatArray
    actual external fun plotToPixelsXyz(x: Double, y: Double, z: Double): FloatArray
    actual external fun pixelsToPlotRayVec2(pixX: Float, pixY: Float): DoubleArray
    actual external fun pixelsToPlotRayXy(x: Double, y: Double): DoubleArray
    actual external fun pixelsToPlotPlaneVec2(pixX: Float, pixY: Float, plane: Int, mask: Boolean): DoubleArray
    actual external fun pixelsToPlotPlaneXy(x: Double, y: Double, plane: Int, mask: Boolean): DoubleArray
    actual external fun getPlotRectPos(): FloatArray
    actual external fun getPlotRectSize(): FloatArray
    actual external fun getPlotDrawList(): Long

    // ==================== Style ====================
    actual external fun getStyle(): FloatArray
    actual external fun setStyle(style: FloatArray)
    actual external fun styleColorsAuto(dst: FloatArray?): FloatArray?
    actual external fun styleColorsDark(dst: FloatArray?): FloatArray?
    actual external fun styleColorsLight(dst: FloatArray?): FloatArray?
    actual external fun styleColorsClassic(dst: FloatArray?): FloatArray?
    actual external fun pushStyleColorVec4(idx: Int, r: Float, g: Float, b: Float, a: Float)
    actual external fun pushStyleColorU32(idx: Int, color: Int)
    actual external fun popStyleColor(count: Int)
    actual external fun pushStyleVarFloat(idx: Int, value: Float)
    actual external fun pushStyleVarInt(idx: Int, value: Int)
    actual external fun pushStyleVarVec2(idx: Int, x: Float, y: Float)
    actual external fun popStyleVar(count: Int)
    actual external fun getStyleColor(idx: Int): FloatArray
    actual external fun getStyleColorU32(idx: Int): Int
    actual external fun nextMarker(): Int

    // ==================== Colormaps ====================
    actual external fun addColormapVec4(name: String, cols: FloatArray, qual: Boolean): Int
    actual external fun addColormapU32(name: String, cols: IntArray, qual: Boolean): Int
    actual external fun getColormapCount(): Int
    actual external fun getColormapName(cmap: Int): String
    actual external fun getColormapIndex(name: String): Int
    actual external fun pushColormap(cmap: Int)
    actual external fun pushColormapName(name: String)
    actual external fun popColormap(count: Int)
    actual external fun nextColormapColor(): FloatArray
    actual external fun getColormapSize(cmap: Int): Int
    actual external fun getColormapColor(idx: Int, cmap: Int): FloatArray
    actual external fun sampleColormap(t: Float, cmap: Int): FloatArray

    // ==================== Demo ====================
    actual external fun showDemoWindow(pOpen: BooleanArray?)
    actual external fun showAllDemos()
    actual external fun showStyleEditor()
    actual external fun showStyleSelector(label: String): Boolean
    actual external fun showColormapSelector(label: String): Boolean
    actual external fun showMetricsWindow(pOpen: BooleanArray?)
    actual external fun showAboutWindow(pOpen: BooleanArray?)

    // ==================== Built-in meshes ====================
    actual external fun cubeVtx(): DoubleArray
    actual external fun cubeIdx(): IntArray
    actual external fun sphereVtx(): DoubleArray
    actual external fun sphereIdx(): IntArray
    actual external fun duckVtx(): DoubleArray
    actual external fun duckIdx(): IntArray

    // ==================== Point math ====================
    actual external fun pointAdd(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): DoubleArray
    actual external fun pointSub(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): DoubleArray
    actual external fun pointMul(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): DoubleArray
    actual external fun pointDiv(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): DoubleArray
    actual external fun pointMulScalar(x: Double, y: Double, z: Double, scalar: Double): DoubleArray
    actual external fun pointDivScalar(x: Double, y: Double, z: Double, scalar: Double): DoubleArray
    actual external fun pointNeg(x: Double, y: Double, z: Double): DoubleArray
    actual external fun pointDot(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): Double
    actual external fun pointCross(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): DoubleArray
    actual external fun pointLength(x: Double, y: Double, z: Double): Double
    actual external fun pointLengthSquared(x: Double, y: Double, z: Double): Double
    actual external fun pointNormalized(x: Double, y: Double, z: Double): DoubleArray
    actual external fun pointIsNaN(x: Double, y: Double, z: Double): Boolean
    actual external fun pointEq(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): Boolean

    // ==================== Quat math ====================
    actual external fun quatFromAngleAxis(angle: Double, ax: Double, ay: Double, az: Double): DoubleArray
    actual external fun quatFromTwoVectors(ax: Double, ay: Double, az: Double, bx: Double, by: Double, bz: Double): DoubleArray
    actual external fun quatFromElAz(elevation: Double, azimuth: Double): DoubleArray
    actual external fun quatMul(ax: Double, ay: Double, az: Double, aw: Double, bx: Double, by: Double, bz: Double, bw: Double): DoubleArray
    actual external fun quatRotatePoint(qx: Double, qy: Double, qz: Double, qw: Double, px: Double, py: Double, pz: Double): DoubleArray
    actual external fun quatNormalized(x: Double, y: Double, z: Double, w: Double): DoubleArray
    actual external fun quatConjugate(x: Double, y: Double, z: Double, w: Double): DoubleArray
    actual external fun quatInverse(x: Double, y: Double, z: Double, w: Double): DoubleArray
    actual external fun quatLength(x: Double, y: Double, z: Double, w: Double): Double
    actual external fun quatDot(ax: Double, ay: Double, az: Double, aw: Double, bx: Double, by: Double, bz: Double, bw: Double): Double
    actual external fun quatSlerp(ax: Double, ay: Double, az: Double, aw: Double, bx: Double, by: Double, bz: Double, bw: Double, t: Double): DoubleArray
    actual external fun quatEq(ax: Double, ay: Double, az: Double, aw: Double, bx: Double, by: Double, bz: Double, bw: Double): Boolean
}
