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

/**
 * A plot item style specification, mirroring ImPlot's ImPlotSpec.
 * A null field keeps the library default.
 */
data class ImPlotSpec(
    val lineColor: ImVec4? = null,
    val lineWeight: Float? = null,
    val fillColor: ImVec4? = null,
    val fillAlpha: Float? = null,
    val marker: Int? = null,
    val markerSize: Float? = null,
    val markerLineColor: ImVec4? = null,
    val markerFillColor: ImVec4? = null,
    val size: Float? = null,
    val offset: Int? = null,
    val stride: Int? = null,
    val flags: Int? = null,
)

/** An ImPlot context; close() calls [ImPlot.destroyContext]. */
interface ImPlotContext : AutoCloseable

/** Kotlin bindings for ImPlot, inside the cn.enaium.imgui.extensions.implot package. */
expect object ImPlot {
    fun createContext(): ImPlotContext
    fun destroyContext(context: ImPlotContext? = null)
    fun getCurrentContext(): ImPlotContext?

    /** Binds the current ImGui context to ImPlot (needed when both are used together). */
    fun setImGuiContext(context: ImGuiContext)

    fun showDemoWindow(pOpen: BooleanArray? = null)

    // ==================== Begin/End plot + setup ====================
    fun beginPlot(titleId: String, size: ImVec2 = ImVec2(-1f, 0f), flags: Int = 0): Boolean
    fun endPlot()
    fun setupAxes(xLabel: String?, yLabel: String?, xFlags: Int = 0, yFlags: Int = 0)
    fun setupAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, cond: Int = ImPlotCond.ONCE)
    fun setupAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int = ImPlotCond.ONCE)
    fun setupLegend(location: Int, flags: Int = 0)
    fun setupFinish()

    fun setupAxis(axis: Int, label: String? = null, flags: Int = 0)
    fun setupAxisFormat(axis: Int, fmt: String)
    fun setupAxisLimitsConstraints(axis: Int, vMin: Double, vMax: Double)
    fun setupAxisZoomConstraints(axis: Int, zMin: Double, zMax: Double)
    fun setupAxisLinks(axis: Int, linkMin: DoubleArray? = null, linkMax: DoubleArray? = null)
    fun setupAxisScale(axis: Int, scale: Int)
    fun setupAxisTicks(axis: Int, values: DoubleArray, labels: Array<String>? = null, tickCount: Int = -1, keepDefault: Boolean = false)
    fun setupMouseText(location: Int, flags: Int = 0)

    // ==================== SetNext (before BeginPlot) ====================
    fun setNextAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, cond: Int = ImPlotCond.ONCE)
    fun setNextAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int = ImPlotCond.ONCE)

    // ==================== Plot items ====================
    fun plotLine(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec = ImPlotSpec())
    fun plotLine(labelId: String, values: FloatArray, xScale: Double = 1.0, xStart: Double = 0.0, spec: ImPlotSpec = ImPlotSpec())
    fun plotScatter(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec = ImPlotSpec())
    fun plotScatter(labelId: String, values: FloatArray, xScale: Double = 1.0, xStart: Double = 0.0, spec: ImPlotSpec = ImPlotSpec())
    fun plotStairs(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec = ImPlotSpec())
    fun plotBars(labelId: String, xs: FloatArray, ys: FloatArray, barSize: Double, spec: ImPlotSpec = ImPlotSpec())
    fun plotBars(labelId: String, values: FloatArray, barSize: Double = 0.67, shift: Double = 0.0, spec: ImPlotSpec = ImPlotSpec())
    fun plotHistogram(
        labelId: String,
        values: FloatArray,
        bins: Int = ImPlotBin.STURGES,
        barScale: Double = 1.0,
        rangeMin: Double = 0.0,
        rangeMax: Double = 0.0,
        spec: ImPlotSpec = ImPlotSpec(),
    ): Double
    fun plotInfLines(labelId: String, values: FloatArray, spec: ImPlotSpec = ImPlotSpec())
    fun plotShaded(labelId: String, xs: FloatArray, ys: FloatArray, yRef: Double = 0.0, spec: ImPlotSpec = ImPlotSpec())
    fun plotText(text: String, x: Double, y: Double, pixOffset: ImVec2 = ImVec2(0f, 0f))
    fun plotDummy(labelId: String, spec: ImPlotSpec = ImPlotSpec())

    // ==================== Advanced plot items ====================
    fun plotBarGroups(labels: Array<String>, values: FloatArray, itemCount: Int, groupCount: Int, groupSize: Double = 0.67, shift: Double = 0.0, spec: ImPlotSpec = ImPlotSpec())
    fun plotErrorBars(labelId: String, xs: FloatArray, ys: FloatArray, neg: FloatArray, pos: FloatArray, spec: ImPlotSpec = ImPlotSpec())
    fun plotStems(labelId: String, xs: FloatArray, ys: FloatArray, ref: Double = 0.0, spec: ImPlotSpec = ImPlotSpec())
    fun plotHeatmap(
        labelId: String,
        values: FloatArray,
        rows: Int,
        cols: Int,
        scaleMin: Double = 0.0,
        scaleMax: Double = 0.0,
        labelFormat: String = "%.1f",
        boundsMinX: Double = 0.0,
        boundsMinY: Double = 0.0,
        boundsMaxX: Double = 1.0,
        boundsMaxY: Double = 1.0,
        spec: ImPlotSpec = ImPlotSpec(),
    )
    fun plotHistogram2D(
        labelId: String,
        xs: FloatArray,
        ys: FloatArray,
        xBins: Int = ImPlotBin.STURGES,
        yBins: Int = ImPlotBin.STURGES,
        rangeXMin: Double = 0.0,
        rangeXMax: Double = 0.0,
        rangeYMin: Double = 0.0,
        rangeYMax: Double = 0.0,
        spec: ImPlotSpec = ImPlotSpec(),
    ): Double
    fun plotDigital(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec = ImPlotSpec())
    fun plotPieChart(labels: Array<String>, values: FloatArray, x: Double, y: Double, radius: Double, labelFormat: String = "%.1f", angle0: Double = 90.0, spec: ImPlotSpec = ImPlotSpec())
    fun plotBubbles(labelId: String, xs: FloatArray, ys: FloatArray, sizes: FloatArray, spec: ImPlotSpec = ImPlotSpec())
    fun plotPolygon(labelId: String, xs: FloatArray, ys: FloatArray, spec: ImPlotSpec = ImPlotSpec())
    fun plotImage(
        labelId: String,
        texId: Long,
        xMin: Double,
        yMin: Double,
        xMax: Double,
        yMax: Double,
        uvMin: ImVec2 = ImVec2(0f, 0f),
        uvMax: ImVec2 = ImVec2(1f, 1f),
        tintCol: ImVec4 = ImVec4(1f, 1f, 1f, 1f),
        spec: ImPlotSpec = ImPlotSpec(),
    )

    // ==================== Subplots ====================
    fun beginSubplots(titleId: String, rows: Int, cols: Int, size: ImVec2, flags: Int = 0): Boolean
    fun endSubplots()

    // ==================== Drag tools / annotations / tags ====================
    fun dragPoint(id: Int, x: DoubleArray, y: DoubleArray, col: ImVec4 = ImVec4(0f, 0f, 0f, 0f), size: Float = 4f, flags: Int = 0): Boolean
    fun dragLineX(id: Int, x: DoubleArray, col: ImVec4 = ImVec4(0f, 0f, 0f, 0f), thickness: Float = 1f, flags: Int = 0): Boolean
    fun dragLineY(id: Int, y: DoubleArray, col: ImVec4 = ImVec4(0f, 0f, 0f, 0f), thickness: Float = 1f, flags: Int = 0): Boolean
    fun dragRect(id: Int, xMin: DoubleArray, yMin: DoubleArray, xMax: DoubleArray, yMax: DoubleArray, col: ImVec4 = ImVec4(0f, 0f, 0f, 0f), flags: Int = 0): Boolean
    fun annotation(x: Double, y: Double, col: ImVec4, pixOffset: ImVec2 = ImVec2(0f, 0f), clamp: Boolean = false, round: Boolean = false, fmt: String? = null)
    fun tagX(x: Double, col: ImVec4, round: Boolean = false, fmt: String? = null)
    fun tagY(y: Double, col: ImVec4, round: Boolean = false, fmt: String? = null)

    // ==================== Queries / coordinates ====================
    fun getPlotLimits(): DoubleArray
    fun getPlotMousePos(): DoubleArray
    fun pixelsToPlot(pixX: Float, pixY: Float): DoubleArray
    fun plotToPixels(x: Double, y: Double): ImVec2
    fun getPlotDrawList(): ImDrawList
    fun nextColormapColor(): ImVec4

    // ==================== Style ====================
    fun pushStyleColor(idx: Int, color: ImVec4)
    fun popStyleColor(count: Int = 1)
    fun pushStyleVar(idx: Int, value: Float)
    fun pushStyleVarFloat(idx: Int, value: Float)
    fun pushStyleVarInt(idx: Int, value: Int)
    fun pushStyleVarVec2(idx: Int, value: ImVec2)
    fun popStyleVar(count: Int = 1)
    fun pushColormap(cmap: Int)
    fun popColormap(count: Int = 1)

    // ==================== Colormap ====================
    fun getColormapCount(): Int
    fun getColormapName(idx: Int): String
    fun getColormapColor(idx: Int, cmap: Int = ImPlotColormap.AUTO): ImVec4
    fun sampleColormap(t: Float, cmap: Int = ImPlotColormap.AUTO): ImVec4
    fun colormapButton(label: String, size: ImVec2 = ImVec2(0f, 0f), cmap: Int = ImPlotColormap.AUTO): Boolean
    fun colormapScale(label: String, scaleMin: Double, scaleMax: Double, size: ImVec2 = ImVec2(0f, 0f), fmt: String = "%g", flags: Int = 0, cmap: Int = ImPlotColormap.AUTO)
    fun colormapSlider(label: String, t: FloatArray, out: FloatArray? = null, fmt: String = "", cmap: Int = ImPlotColormap.AUTO): Boolean
    fun colormapIcon(cmap: Int = ImPlotColormap.AUTO)

    // ==================== Color maps (misc) ====================
    fun addColormap(name: String, cols: FloatArray): Int
    fun itemIcon(col: Int)
    fun getLastItemColor(): Int

    // ==================== Plot utils ====================
    fun setAxis(axis: Int)
    fun setAxes(xAxis: Int, yAxis: Int)
    fun getPlotSelection(): DoubleArray
    fun pushPlotClipRect(expand: Float = 0f)
    fun popPlotClipRect()

    // ==================== Drag and drop ====================
    fun beginDragDropSourcePlot(flags: Int = 0): Boolean
    fun beginDragDropSourceAxis(axis: Int, flags: Int = 0): Boolean
    fun beginDragDropSourceItem(labelId: String, flags: Int = 0): Boolean
    fun endDragDropSource()
    fun beginDragDropTargetPlot(): Boolean
    fun beginDragDropTargetAxis(axis: Int): Boolean
    fun beginDragDropTargetLegend(): Boolean
    fun endDragDropTarget()

    // ==================== Legend popup ====================
    fun beginLegendPopup(labelId: String, mouseButton: Int = 1): Boolean
    fun endLegendPopup()

    // ==================== Input mapping / tools ====================
    fun getInputMap(): Long
    fun showInputMapSelector(label: String): Boolean
    fun showMetricsWindow(pOpen: BooleanArray? = null)
    fun showStyleEditor()
    fun showStyleSelector(label: String): Boolean
    fun showColormapSelector(label: String): Boolean

    // ==================== Queries ====================
    fun isPlotHovered(): Boolean
    fun isPlotSelected(): Boolean
    fun isAxisHovered(axis: Int): Boolean
    fun getPlotPos(): ImVec2
    fun getPlotSize(): ImVec2
}

// =========================================================================
// Enums (values match ImPlot's implot.h)
// =========================================================================

object ImPlotAxis {
    const val X1 = 0
    const val X2 = 1
    const val X3 = 2
    const val Y1 = 3
    const val Y2 = 4
    const val Y3 = 5
}

object ImPlotCond {
    const val NONE = 0
    const val ALWAYS = 1 shl 0
    const val ONCE = 1 shl 1
}

object ImPlotCol {
    const val FRAME_BG = 0
    const val PLOT_BG = 1
    const val PLOT_BORDER = 2
    const val LEGEND_BG = 3
    const val LEGEND_BORDER = 4
    const val LEGEND_TEXT = 5
    const val TITLE_TEXT = 6
    const val INLAY_TEXT = 7
    const val AXIS_TEXT = 8
    const val AXIS_GRID = 9
    const val AXIS_TICK = 10
    const val AXIS_BG = 11
    const val AXIS_BG_HOVERED = 12
    const val AXIS_BG_ACTIVE = 13
    const val SELECTION = 14
    const val CROSSHAIRS = 15
}

object ImPlotFlags {
    const val NONE = 0
    const val NO_TITLE = 1 shl 0
    const val NO_LEGEND = 1 shl 1
    const val NO_MOUSE_TEXT = 1 shl 2
    const val NO_INPUT = 1 shl 3
    const val NO_MENU = 1 shl 4
    const val NO_BOX_SELECT = 1 shl 5
    const val NO_FRAME = 1 shl 6
    const val EQUAL = 1 shl 7
    const val CROSSHAIRS = 1 shl 8
    const val NO_CHILD = 1 shl 9
    const val NO_HIGHLIGHT = 1 shl 10
    const val NO_SNAP = 1 shl 11
    const val NO_MOUSE_POS = 1 shl 12
}

object ImPlotAxisFlags {
    const val NONE = 0
    const val NO_LABEL = 1 shl 0
    const val NO_GRID_LINES = 1 shl 1
    const val NO_TICK_MARKS = 1 shl 2
    const val NO_TICK_LABELS = 1 shl 3
    const val NO_INITIAL_FIT = 1 shl 4
    const val NO_MENUS = 1 shl 5
    const val NO_SIDE_SWITCH = 1 shl 6
    const val NO_HIGHLIGHT = 1 shl 7
    const val OPPOSITE = 1 shl 8
    const val FOREGROUND = 1 shl 9
    const val INVERT = 1 shl 10
    const val AUTO_FIT = 1 shl 11
    const val RANGE_FIT = 1 shl 12
    const val PAN_STRETCH = 1 shl 13
    const val LOCK_MIN = 1 shl 14
    const val LOCK_MAX = 1 shl 15
    const val LOCK = LOCK_MIN or LOCK_MAX
}

object ImPlotMarker {
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

object ImPlotColormap {
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

object ImPlotLocation {
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

object ImPlotBin {
    const val SQRT = -1
    const val STURGES = -2
    const val RICE = -3
    const val SCOTT = -4
}

object ImPlotItemFlags {
    const val NONE = 0
    const val NO_LEGEND = 1 shl 0
    const val NO_FIT = 1 shl 1
}

object ImPlotStyleVar {
    const val PLOT_DEFAULT_SIZE = 0
    const val PLOT_MIN_SIZE = 1
    const val PLOT_BORDER_SIZE = 2
    const val MINOR_ALPHA = 3
    const val MAJOR_TICK_LEN = 4
    const val MINOR_TICK_LEN = 5
    const val MAJOR_TICK_SIZE = 6
    const val MINOR_TICK_SIZE = 7
    const val MAJOR_GRID_SIZE = 8
    const val MINOR_GRID_SIZE = 9
    const val PLOT_PADDING = 10
    const val LABEL_PADDING = 11
    const val LEGEND_PADDING = 12
    const val LEGEND_INNER_PADDING = 13
    const val LEGEND_SPACING = 14
    const val MOUSE_POS_PADDING = 15
    const val ANNOTATION_PADDING = 16
    const val FIT_PADDING = 17
    const val DIGITAL_PADDING = 18
    const val DIGITAL_SPACING = 19
}

object ImPlotScale {
    const val LINEAR = 0
    const val TIME = 1
    const val LOG10 = 2
    const val SYMLOG = 3
}

object ImPlotSubplotFlags {
    const val NONE = 0
    const val NO_TITLE = 1 shl 0
    const val NO_LEGEND = 1 shl 1
    const val NO_MENUS = 1 shl 2
    const val NO_RESIZE = 1 shl 3
    const val NO_ALIGN = 1 shl 4
    const val SHARE_ITEMS = 1 shl 5
    const val LINK_ROWS = 1 shl 6
    const val LINK_COLS = 1 shl 7
    const val LINK_ALL_X = 1 shl 8
    const val LINK_ALL_Y = 1 shl 9
    const val COL_MAJOR = 1 shl 10
}

object ImPlotDragToolFlags {
    const val NONE = 0
    const val NO_CURSORS = 1 shl 0
    const val NO_FIT = 1 shl 1
    const val NO_INPUTS = 1 shl 2
    const val DELAYED = 1 shl 3
}

object ImPlotMouseTextFlags {
    const val NONE = 0
    const val NO_AUX_AXES = 1 shl 0
    const val NO_FORMAT = 1 shl 1
    const val SHOW_ALWAYS = 1 shl 2
}

object ImPlotColormapScaleFlags {
    const val NONE = 0
    const val NO_LABEL = 1 shl 0
    const val OPPOSITE = 1 shl 1
    const val INVERT = 1 shl 2
}
