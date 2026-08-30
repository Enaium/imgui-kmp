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

/**
 * A plot item style specification, mirroring ImPlot3D's ImPlot3DSpec.
 * A null field keeps the library default.
 */
data class ImPlot3DSpec(
    val lineColor: ImVec4? = null,
    val lineWeight: Float? = null,
    val fillColor: ImVec4? = null,
    val fillAlpha: Float? = null,
    val marker: Int? = null,
    val markerSize: Float? = null,
    val markerLineColor: ImVec4? = null,
    val markerFillColor: ImVec4? = null,
    val offset: Int? = null,
    val stride: Int? = null,
    val flags: Int? = null,
)

/** 3D point / vector. */
data class ImPlot3DPoint(val x: Double, val y: Double, val z: Double)

/** Ray with an origin and a direction. */
data class ImPlot3DRay(val origin: ImPlot3DPoint, val direction: ImPlot3DPoint)

/** ImPlot3D style snapshot. */
data class ImPlot3DStyle(
    val lineWeight: Float = 1f,
    val marker: Int = ImPlot3DMarker.AUTO,
    val markerSize: Float = 4f,
    val fillAlpha: Float = 1f,
    val plotDefaultSize: ImVec2 = ImVec2(-1f, 0f),
    val plotMinSize: ImVec2 = ImVec2(0f, 0f),
    val plotPadding: ImVec2 = ImVec2(10f, 10f),
    val labelPadding: ImVec2 = ImVec2(5f, 5f),
    val viewScaleFactor: Float = 1f,
    val legendPadding: ImVec2 = ImVec2(10f, 10f),
    val legendInnerPadding: ImVec2 = ImVec2(5f, 5f),
    val legendSpacing: ImVec2 = ImVec2(0f, 5f),
    val colors: Array<ImVec4> = Array(ImPlot3DCol.COUNT) { ImVec4(0f, 0f, 0f, 1f) },
    val colormap: Int = ImPlot3DColormap.DEEP,
)

/** An ImPlot3D context; close() calls [ImPlot3D.destroyContext]. */
interface ImPlot3DContext : AutoCloseable

/**
 * Kotlin bindings for ImPlot3D, inside the cn.enaium.imgui.extensions.implot3d package.
 * Mirrors ImPlot3D's namespace API (https://github.com/brenocq/implot3d).
 */
expect object ImPlot3D {
    // ==================== Context ====================
    fun createContext(): ImPlot3DContext
    fun destroyContext(context: ImPlot3DContext? = null)
    fun getCurrentContext(): ImPlot3DContext?
    fun setCurrentContext(context: ImPlot3DContext?)

    // ==================== Begin/End plot ====================
    fun beginPlot(titleId: String, size: ImVec2 = ImVec2(-1f, 0f), flags: Int = 0): Boolean
    fun endPlot()

    // ==================== Setup ====================
    fun setupAxis(axis: Int, label: String? = null, flags: Int = 0)
    fun setupAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int = ImPlot3DCond.ONCE)
    fun setupAxisTicks(axis: Int, values: DoubleArray, labels: Array<String>? = null, tickCount: Int = -1, keepDefault: Boolean = false)
    fun setupAxisTicks(axis: Int, vMin: Double, vMax: Double, tickCount: Int, labels: Array<String>? = null, keepDefault: Boolean = false)
    fun setupAxisScale(axis: Int, scale: Int)
    fun setupAxisLimitsConstraints(axis: Int, vMin: Double, vMax: Double)
    fun setupAxisZoomConstraints(axis: Int, zoomMin: Double, zoomMax: Double)
    fun setupAxes(xLabel: String?, yLabel: String?, zLabel: String?, xFlags: Int = 0, yFlags: Int = 0, zFlags: Int = 0)
    fun setupAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, zMin: Double, zMax: Double, cond: Int = ImPlot3DCond.ONCE)
    fun setupBoxRotation(elevation: Double, azimuth: Double, animate: Boolean = false, cond: Int = ImPlot3DCond.ONCE)
    fun setupBoxRotation(rotation: ImPlot3DQuat, animate: Boolean = false, cond: Int = ImPlot3DCond.ONCE)
    fun setupBoxInitialRotation(elevation: Double, azimuth: Double)
    fun setupBoxInitialRotation(rotation: ImPlot3DQuat)
    fun setupBoxScale(x: Double, y: Double, z: Double)
    fun setupLegend(location: Int, flags: Int = 0)

    // ==================== Plot items (double data arrays) ====================
    fun plotScatter(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, spec: ImPlot3DSpec = ImPlot3DSpec())
    fun plotLine(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, spec: ImPlot3DSpec = ImPlot3DSpec())
    fun plotTriangle(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, spec: ImPlot3DSpec = ImPlot3DSpec())
    fun plotQuad(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, spec: ImPlot3DSpec = ImPlot3DSpec())
    fun plotSurface(
        labelId: String,
        xs: DoubleArray,
        ys: DoubleArray,
        zs: DoubleArray,
        xCount: Int,
        yCount: Int,
        scaleMin: Double = 0.0,
        scaleMax: Double = 0.0,
        spec: ImPlot3DSpec = ImPlot3DSpec(),
    )
    fun plotMesh(
        labelId: String,
        vtxXs: DoubleArray,
        vtxYs: DoubleArray,
        vtxZs: DoubleArray,
        idxs: IntArray,
        spec: ImPlot3DSpec = ImPlot3DSpec(),
    )
    fun plotText(text: String, x: Double, y: Double, z: Double, angle: Double = 0.0, pixOffset: ImVec2 = ImVec2(0f, 0f))
    fun plotDummy(labelId: String, spec: ImPlot3DSpec = ImPlot3DSpec())

    // ==================== Plot utils ====================
    fun plotToPixels(point: ImPlot3DPoint): ImVec2
    fun plotToPixels(x: Double, y: Double, z: Double): ImVec2
    fun pixelsToPlotRay(pix: ImVec2): ImPlot3DRay
    fun pixelsToPlotRay(x: Double, y: Double): ImPlot3DRay
    fun pixelsToPlotPlane(pix: ImVec2, plane: Int, mask: Boolean = true): ImPlot3DPoint
    fun pixelsToPlotPlane(x: Double, y: Double, plane: Int, mask: Boolean = true): ImPlot3DPoint
    fun getPlotRectPos(): ImVec2
    fun getPlotRectSize(): ImVec2
    fun getPlotDrawList(): ImDrawList

    // ==================== Style ====================
    fun getStyle(): ImPlot3DStyle
    fun setStyle(style: ImPlot3DStyle)
    fun styleColorsAuto(dst: ImPlot3DStyle? = null): ImPlot3DStyle?
    fun styleColorsDark(dst: ImPlot3DStyle? = null): ImPlot3DStyle?
    fun styleColorsLight(dst: ImPlot3DStyle? = null): ImPlot3DStyle?
    fun styleColorsClassic(dst: ImPlot3DStyle? = null): ImPlot3DStyle?
    fun pushStyleColor(idx: Int, color: ImVec4)
    fun pushStyleColor(idx: Int, color: Int)
    fun popStyleColor(count: Int = 1)
    fun pushStyleVar(idx: Int, value: Float)
    fun pushStyleVar(idx: Int, value: Int)
    fun pushStyleVar(idx: Int, value: ImVec2)
    fun popStyleVar(count: Int = 1)
    fun getStyleColor(idx: Int): ImVec4
    fun getStyleColorU32(idx: Int): Int
    fun nextMarker(): Int

    // ==================== Colormaps ====================
    fun addColormap(name: String, cols: Array<ImVec4>, qual: Boolean = true): Int
    fun addColormap(name: String, cols: IntArray, qual: Boolean = true): Int
    fun getColormapCount(): Int
    fun getColormapName(cmap: Int): String
    fun getColormapIndex(name: String): Int
    fun pushColormap(cmap: Int)
    fun pushColormap(name: String)
    fun popColormap(count: Int = 1)
    fun nextColormapColor(): ImVec4
    fun getColormapSize(cmap: Int = ImPlot3DColormap.AUTO): Int
    fun getColormapColor(idx: Int, cmap: Int = ImPlot3DColormap.AUTO): ImVec4
    fun sampleColormap(t: Float, cmap: Int = ImPlot3DColormap.AUTO): ImVec4

    // ==================== Demo ====================
    fun showDemoWindow(pOpen: BooleanArray? = null)
    fun showAllDemos()
    fun showStyleEditor()
    fun showStyleSelector(label: String): Boolean
    fun showColormapSelector(label: String): Boolean
    fun showMetricsWindow(pOpen: BooleanArray? = null)
    fun showAboutWindow(pOpen: BooleanArray? = null)

    // ==================== Built-in meshes ====================
    fun cubeVertices(): Array<ImPlot3DPoint>
    fun cubeIndices(): IntArray
    fun sphereVertices(): Array<ImPlot3DPoint>
    fun sphereIndices(): IntArray
    fun duckVertices(): Array<ImPlot3DPoint>
    fun duckIndices(): IntArray

    // ==================== Point math ====================
    fun pointAdd(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint
    fun pointSub(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint
    fun pointMul(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint
    fun pointDiv(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint
    fun pointMulScalar(a: ImPlot3DPoint, scalar: Double): ImPlot3DPoint
    fun pointDivScalar(a: ImPlot3DPoint, scalar: Double): ImPlot3DPoint
    fun pointNeg(a: ImPlot3DPoint): ImPlot3DPoint
    fun pointDot(a: ImPlot3DPoint, b: ImPlot3DPoint): Double
    fun pointCross(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint
    fun pointLength(a: ImPlot3DPoint): Double
    fun pointLengthSquared(a: ImPlot3DPoint): Double
    fun pointNormalized(a: ImPlot3DPoint): ImPlot3DPoint
    fun pointIsNaN(a: ImPlot3DPoint): Boolean
    fun pointEq(a: ImPlot3DPoint, b: ImPlot3DPoint): Boolean

    // ==================== Quat math ====================
    fun quatFromAngleAxis(angle: Double, axis: ImPlot3DPoint): ImPlot3DQuat
    fun quatFromTwoVectors(v0: ImPlot3DPoint, v1: ImPlot3DPoint): ImPlot3DQuat
    fun quatFromElAz(elevation: Double, azimuth: Double): ImPlot3DQuat
    fun quatMul(a: ImPlot3DQuat, b: ImPlot3DQuat): ImPlot3DQuat
    fun quatRotatePoint(q: ImPlot3DQuat, p: ImPlot3DPoint): ImPlot3DPoint
    fun quatNormalized(q: ImPlot3DQuat): ImPlot3DQuat
    fun quatConjugate(q: ImPlot3DQuat): ImPlot3DQuat
    fun quatInverse(q: ImPlot3DQuat): ImPlot3DQuat
    fun quatLength(q: ImPlot3DQuat): Double
    fun quatDot(a: ImPlot3DQuat, b: ImPlot3DQuat): Double
    fun quatSlerp(q1: ImPlot3DQuat, q2: ImPlot3DQuat, t: Double): ImPlot3DQuat
    fun quatEq(a: ImPlot3DQuat, b: ImPlot3DQuat): Boolean
}

/** A quaternion for 3D rotations. */
data class ImPlot3DQuat(val x: Double, val y: Double, val z: Double, val w: Double) {
    companion object {
        fun fromAngleAxis(angle: Double, axis: ImPlot3DPoint): ImPlot3DQuat = ImPlot3D.quatFromAngleAxis(angle, axis)
        fun fromTwoVectors(v0: ImPlot3DPoint, v1: ImPlot3DPoint): ImPlot3DQuat = ImPlot3D.quatFromTwoVectors(v0, v1)
        fun fromElAz(elevation: Double, azimuth: Double): ImPlot3DQuat = ImPlot3D.quatFromElAz(elevation, azimuth)
        fun slerp(q1: ImPlot3DQuat, q2: ImPlot3DQuat, t: Double): ImPlot3DQuat = ImPlot3D.quatSlerp(q1, q2, t)
    }
}

// =========================================================================
// Point / quat math helpers (mirror ImPlot3DPoint/ImPlot3DQuat methods)
// =========================================================================

fun ImPlot3DPoint.plus(other: ImPlot3DPoint): ImPlot3DPoint = ImPlot3D.pointAdd(this, other)
fun ImPlot3DPoint.minus(other: ImPlot3DPoint): ImPlot3DPoint = ImPlot3D.pointSub(this, other)
fun ImPlot3DPoint.times(other: ImPlot3DPoint): ImPlot3DPoint = ImPlot3D.pointMul(this, other)
fun ImPlot3DPoint.div(other: ImPlot3DPoint): ImPlot3DPoint = ImPlot3D.pointDiv(this, other)
fun ImPlot3DPoint.times(scalar: Double): ImPlot3DPoint = ImPlot3D.pointMulScalar(this, scalar)
fun ImPlot3DPoint.div(scalar: Double): ImPlot3DPoint = ImPlot3D.pointDivScalar(this, scalar)
fun ImPlot3DPoint.unaryMinus(): ImPlot3DPoint = ImPlot3D.pointNeg(this)
fun ImPlot3DPoint.dot(other: ImPlot3DPoint): Double = ImPlot3D.pointDot(this, other)
fun ImPlot3DPoint.cross(other: ImPlot3DPoint): ImPlot3DPoint = ImPlot3D.pointCross(this, other)
fun ImPlot3DPoint.length(): Double = ImPlot3D.pointLength(this)
fun ImPlot3DPoint.lengthSquared(): Double = ImPlot3D.pointLengthSquared(this)
fun ImPlot3DPoint.normalized(): ImPlot3DPoint = ImPlot3D.pointNormalized(this)
fun ImPlot3DPoint.isNaN(): Boolean = ImPlot3D.pointIsNaN(this)
fun ImPlot3DPoint.eq(other: ImPlot3DPoint): Boolean = ImPlot3D.pointEq(this, other)

// =========================================================================
// Enums (values match ImPlot3D's implot3d.h)
// =========================================================================

object ImPlot3DCol {
    const val TITLE_TEXT = 0
    const val INLAY_TEXT = 1
    const val FRAME_BG = 2
    const val PLOT_BG = 3
    const val PLOT_BORDER = 4
    const val LEGEND_BG = 5
    const val LEGEND_BORDER = 6
    const val LEGEND_TEXT = 7
    const val AXIS_TEXT = 8
    const val AXIS_GRID = 9
    const val AXIS_TICK = 10
    const val AXIS_BG = 11
    const val AXIS_BG_HOVERED = 12
    const val AXIS_BG_ACTIVE = 13
    const val COUNT = 14
}

object ImPlot3DStyleVar {
    const val LINE_WEIGHT = 0
    const val MARKER = 1
    const val MARKER_SIZE = 2
    const val FILL_ALPHA = 3
    const val PLOT_DEFAULT_SIZE = 4
    const val PLOT_MIN_SIZE = 5
    const val PLOT_PADDING = 6
    const val LABEL_PADDING = 7
    const val VIEW_SCALE_FACTOR = 8
    const val LEGEND_PADDING = 9
    const val LEGEND_INNER_PADDING = 10
    const val LEGEND_SPACING = 11
}

object ImPlot3DMarker {
    const val NONE = -2
    const val AUTO = -1
    const val CIRCLE = 0
    const val SQUARE = 1
    const val DIAMOND = 2
    const val UP = 3
    const val DOWN = 4
    const val LEFT = 5
    const val RIGHT = 6
    const val CROSS = 7
    const val PLUS = 8
    const val ASTERISK = 9
}

object ImPlot3DItemFlags {
    const val NONE = 0
    const val NO_LEGEND = 1 shl 0
    const val NO_FIT = 1 shl 1
}

object ImPlot3DFlags {
    const val NONE = 0
    const val NO_TITLE = 1 shl 0
    const val NO_LEGEND = 1 shl 1
    const val NO_MOUSE_TEXT = 1 shl 2
    const val NO_CLIP = 1 shl 3
    const val NO_MENUS = 1 shl 4
    const val EQUAL = 1 shl 5
    const val NO_ROTATE = 1 shl 6
    const val NO_PAN = 1 shl 7
    const val NO_ZOOM = 1 shl 8
    const val NO_INPUTS = 1 shl 9
    const val CANVAS_ONLY = NO_TITLE or NO_LEGEND or NO_MOUSE_TEXT
}

object ImPlot3DLineFlags {
    const val NONE = 0
    const val NO_LEGEND = ImPlot3DItemFlags.NO_LEGEND
    const val NO_FIT = ImPlot3DItemFlags.NO_FIT
    const val SEGMENTS = 1 shl 10
    const val LOOP = 1 shl 11
    const val SKIP_NAN = 1 shl 12
}

object ImPlot3DAxisFlags {
    const val NONE = 0
    const val NO_LABEL = 1 shl 0
    const val NO_GRID_LINES = 1 shl 1
    const val NO_TICK_MARKS = 1 shl 2
    const val NO_TICK_LABELS = 1 shl 3
    const val LOCK_MIN = 1 shl 4
    const val LOCK_MAX = 1 shl 5
    const val AUTO_FIT = 1 shl 6
    const val INVERT = 1 shl 7
    const val PAN_STRETCH = 1 shl 8
    const val LOCK = LOCK_MIN or LOCK_MAX
    const val NO_DECORATIONS = NO_LABEL or NO_GRID_LINES or NO_TICK_LABELS
}

object ImAxis3D {
    const val X = 0
    const val Y = 1
    const val Z = 2
    const val COUNT = 3
}

object ImPlane3D {
    const val YZ = 0
    const val XZ = 1
    const val XY = 2
    const val COUNT = 3
}

object ImPlot3DScale {
    const val LINEAR = 0
    const val LOG10 = 1
    const val SYMLOG = 2
}

object ImPlot3DColormap {
    const val AUTO = -1
    const val DEEP = 0
    const val DARK = 1
    const val PASTEL = 2
    const val PAIRED = 3
    const val VIRIDIS = 4
    const val PLASMA = 5
    const val HOT = 6
    const val COOL = 7
    const val PINK = 8
    const val JET = 9
    const val TWILIGHT = 10
    const val RDBU = 11
    const val BRBG = 12
    const val PIYG = 13
    const val SPECTRAL = 14
    const val GREYS = 15
}

object ImPlot3DLegendFlags {
    const val NONE = 0
    const val NO_BUTTONS = 1 shl 0
    const val NO_HIGHLIGHT_ITEM = 1 shl 1
    const val HORIZONTAL = 1 shl 2
}

object ImPlot3DLocation {
    const val CENTER = 0
    const val NORTH = 1 shl 0
    const val SOUTH = 1 shl 1
    const val WEST = 1 shl 2
    const val EAST = 1 shl 3
    const val NORTH_WEST = NORTH or WEST
    const val NORTH_EAST = NORTH or EAST
    const val SOUTH_WEST = SOUTH or WEST
    const val SOUTH_EAST = SOUTH or EAST
}

object ImPlot3DCond {
    const val NONE = 0
    const val ALWAYS = 1
    const val ONCE = 2
}