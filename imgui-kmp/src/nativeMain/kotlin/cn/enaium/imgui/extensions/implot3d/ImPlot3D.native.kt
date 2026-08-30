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

package cn.enaium.imgui.extensions.implot3d

import cn.enaium.imgui.ImDrawList
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import cn.enaium.imgui.NativeImDrawList
import imgui.*
import kotlinx.cinterop.*

// =========================================================================
// Native (cinterop) actual implementations
// =========================================================================

internal class NativeImPlot3DContext(internal val ptr: CPointer<implot3d_context>?) : ImPlot3DContext {
    override fun close() {
        implot3d_destroy_context(ptr)
    }
}

/** Fills an implot3d_spec from the Kotlin [ImPlot3DSpec]; passes null when empty. */
private inline fun <T> withSpec(spec: ImPlot3DSpec, block: (CPointer<implot3d_spec>?) -> T): T {
    if (spec == ImPlot3DSpec()) return block(null)
    return memScoped {
        val c = alloc<implot3d_spec>()
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

private inline fun <T> withThreeArrays(
    xs: DoubleArray,
    ys: DoubleArray,
    zs: DoubleArray,
    block: (CPointer<DoubleVar>?, CPointer<DoubleVar>?, CPointer<DoubleVar>?, Int) -> T,
): T {
    val count = minOf(xs.size, ys.size, zs.size)
    if (count == 0) {
        return block(null, null, null, 0)
    }
    return memScoped {
        val xArr = allocArray<DoubleVar>(count)
        val yArr = allocArray<DoubleVar>(count)
        val zArr = allocArray<DoubleVar>(count)
        for (i in 0 until count) {
            xArr[i] = xs[i]
            yArr[i] = ys[i]
            zArr[i] = zs[i]
        }
        block(xArr, yArr, zArr, count)
    }
}

private inline fun <T> withArray(values: DoubleArray, block: (CPointer<DoubleVar>?, Int) -> T): T {
    if (values.isEmpty()) {
        return block(null, 0)
    }
    return memScoped {
        val arr = allocArray<DoubleVar>(values.size)
        for (i in values.indices) {
            arr[i] = values[i]
        }
        block(arr, values.size)
    }
}

private inline fun <T> withUIntArray(values: IntArray, block: (CPointer<UIntVar>?, Int) -> T): T {
    if (values.isEmpty()) {
        return block(null, 0)
    }
    return memScoped {
        val arr = allocArray<UIntVar>(values.size)
        for (i in values.indices) {
            arr[i] = values[i].toUInt()
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

private inline fun <T> withPoint(value: ImPlot3DPoint, block: (CValue<implot3d_point>) -> T): T = memScoped {
    val p = alloc<implot3d_point>()
    p.x = value.x
    p.y = value.y
    p.z = value.z
    block(p.readValue())
}

private inline fun <T> withTwoPoints(
    a: ImPlot3DPoint,
    b: ImPlot3DPoint,
    block: (CValue<implot3d_point>, CValue<implot3d_point>) -> T,
): T = memScoped {
    val av = alloc<implot3d_point>()
    av.x = a.x
    av.y = a.y
    av.z = a.z
    val bv = alloc<implot3d_point>()
    bv.x = b.x
    bv.y = b.y
    bv.z = b.z
    block(av.readValue(), bv.readValue())
}

private inline fun <T> withQuat(value: ImPlot3DQuat, block: (CValue<implot3d_quat>) -> T): T = memScoped {
    val q = alloc<implot3d_quat>()
    q.x = value.x
    q.y = value.y
    q.z = value.z
    q.w = value.w
    block(q.readValue())
}

private inline fun <T> withTwoQuats(
    a: ImPlot3DQuat,
    b: ImPlot3DQuat,
    block: (CValue<implot3d_quat>, CValue<implot3d_quat>) -> T,
): T = memScoped {
    val av = alloc<implot3d_quat>()
    av.x = a.x
    av.y = a.y
    av.z = a.z
    av.w = a.w
    val bv = alloc<implot3d_quat>()
    bv.x = b.x
    bv.y = b.y
    bv.z = b.z
    bv.w = b.w
    block(av.readValue(), bv.readValue())
}

/** Reads an implot3d_style into a Kotlin [ImPlot3DStyle]. */
private fun readStyle(c: CPointer<implot3d_style>): ImPlot3DStyle {
    val s = c.pointed
    val colors = Array(ImPlot3DCol.COUNT) { i ->
        val v = s.colors[i]
        ImVec4(v.x, v.y, v.z, v.w)
    }
    return ImPlot3DStyle(
        lineWeight = s.line_weight,
        marker = s.marker,
        markerSize = s.marker_size,
        fillAlpha = s.fill_alpha,
        plotDefaultSize = ImVec2(s.plot_default_size.x, s.plot_default_size.y),
        plotMinSize = ImVec2(s.plot_min_size.x, s.plot_min_size.y),
        plotPadding = ImVec2(s.plot_padding.x, s.plot_padding.y),
        labelPadding = ImVec2(s.label_padding.x, s.label_padding.y),
        viewScaleFactor = s.view_scale_factor,
        legendPadding = ImVec2(s.legend_padding.x, s.legend_padding.y),
        legendInnerPadding = ImVec2(s.legend_inner_padding.x, s.legend_inner_padding.y),
        legendSpacing = ImVec2(s.legend_spacing.x, s.legend_spacing.y),
        colors = colors,
        colormap = s.colormap,
    )
}

/** Fills an implot3d_style from a Kotlin [ImPlot3DStyle]. */
private fun fillStyle(c: CPointer<implot3d_style>, s: ImPlot3DStyle) {
    val st = c.pointed
    st.line_weight = s.lineWeight
    st.marker = s.marker
    st.marker_size = s.markerSize
    st.fill_alpha = s.fillAlpha
    st.plot_default_size.x = s.plotDefaultSize.x
    st.plot_default_size.y = s.plotDefaultSize.y
    st.plot_min_size.x = s.plotMinSize.x
    st.plot_min_size.y = s.plotMinSize.y
    st.plot_padding.x = s.plotPadding.x
    st.plot_padding.y = s.plotPadding.y
    st.label_padding.x = s.labelPadding.x
    st.label_padding.y = s.labelPadding.y
    st.view_scale_factor = s.viewScaleFactor
    st.legend_padding.x = s.legendPadding.x
    st.legend_padding.y = s.legendPadding.y
    st.legend_inner_padding.x = s.legendInnerPadding.x
    st.legend_inner_padding.y = s.legendInnerPadding.y
    st.legend_spacing.x = s.legendSpacing.x
    st.legend_spacing.y = s.legendSpacing.y
    for (i in 0 until ImPlot3DCol.COUNT) {
        st.colors[i].x = s.colors[i].x
        st.colors[i].y = s.colors[i].y
        st.colors[i].z = s.colors[i].z
        st.colors[i].w = s.colors[i].w
    }
    st.colormap = s.colormap
}

// =========================================================================
// actual object
// =========================================================================

actual object ImPlot3D {
    // ==================== Context ====================

    actual fun createContext(): ImPlot3DContext {
        val ptr = implot3d_create_context()
            ?: error("implot3d_create_context returned null")
        return NativeImPlot3DContext(ptr)
    }

    actual fun destroyContext(context: ImPlot3DContext?) {
        if (context != null) {
            implot3d_destroy_context((context as NativeImPlot3DContext).ptr)
        } else {
            implot3d_destroy_context(null)
        }
    }

    actual fun getCurrentContext(): ImPlot3DContext? {
        val ptr = implot3d_get_current_context()
        return if (ptr != null) NativeImPlot3DContext(ptr) else null
    }

    actual fun setCurrentContext(context: ImPlot3DContext?) {
        implot3d_set_current_context((context as? NativeImPlot3DContext)?.ptr)
    }

    // ==================== Begin/End plot ====================

    actual fun beginPlot(titleId: String, size: ImVec2, flags: Int): Boolean = withVec2(size) {
        implot3d_begin_plot(titleId, it, flags)
    }

    actual fun endPlot() = implot3d_end_plot()

    // ==================== Setup ====================

    actual fun setupAxis(axis: Int, label: String?, flags: Int) = implot3d_setup_axis(axis, label, flags)

    actual fun setupAxisLimits(axis: Int, vMin: Double, vMax: Double, cond: Int) =
        implot3d_setup_axis_limits(axis, vMin, vMax, cond)

    actual fun setupAxisTicks(axis: Int, values: DoubleArray, labels: Array<String>?, tickCount: Int, keepDefault: Boolean) = withArray(values) { vArr, _ ->
        val n = if (tickCount >= 0) tickCount else values.size
        if (labels != null) {
            withStringArray(labels) { labelArr ->
                implot3d_setup_axis_ticks_values(axis, vArr, n, labelArr, keepDefault)
            }
        } else {
            implot3d_setup_axis_ticks_values(axis, vArr, n, null, keepDefault)
        }
    }

    actual fun setupAxisTicks(axis: Int, vMin: Double, vMax: Double, tickCount: Int, labels: Array<String>?, keepDefault: Boolean) =
        if (labels != null) {
            withStringArray(labels) { labelArr ->
                implot3d_setup_axis_ticks_limits(axis, vMin, vMax, tickCount, labelArr, keepDefault)
            }
        } else {
            implot3d_setup_axis_ticks_limits(axis, vMin, vMax, tickCount, null, keepDefault)
        }

    actual fun setupAxisScale(axis: Int, scale: Int) = implot3d_setup_axis_scale(axis, scale)

    actual fun setupAxisLimitsConstraints(axis: Int, vMin: Double, vMax: Double) =
        implot3d_setup_axis_limits_constraints(axis, vMin, vMax)

    actual fun setupAxisZoomConstraints(axis: Int, zoomMin: Double, zoomMax: Double) =
        implot3d_setup_axis_zoom_constraints(axis, zoomMin, zoomMax)

    actual fun setupAxes(xLabel: String?, yLabel: String?, zLabel: String?, xFlags: Int, yFlags: Int, zFlags: Int) =
        implot3d_setup_axes(xLabel, yLabel, zLabel, xFlags, yFlags, zFlags)

    actual fun setupAxesLimits(xMin: Double, xMax: Double, yMin: Double, yMax: Double, zMin: Double, zMax: Double, cond: Int) =
        implot3d_setup_axes_limits(xMin, xMax, yMin, yMax, zMin, zMax, cond)

    actual fun setupBoxRotation(elevation: Double, azimuth: Double, animate: Boolean, cond: Int) =
        implot3d_setup_box_rotation_angles(elevation, azimuth, animate, cond)

    actual fun setupBoxRotation(rotation: ImPlot3DQuat, animate: Boolean, cond: Int) = withQuat(rotation) {
        implot3d_setup_box_rotation_quat(it, animate, cond)
    }

    actual fun setupBoxInitialRotation(elevation: Double, azimuth: Double) =
        implot3d_setup_box_initial_rotation_angles(elevation, azimuth)

    actual fun setupBoxInitialRotation(rotation: ImPlot3DQuat) = withQuat(rotation) {
        implot3d_setup_box_initial_rotation_quat(it)
    }

    actual fun setupBoxScale(x: Double, y: Double, z: Double) = implot3d_setup_box_scale(x, y, z)

    actual fun setupLegend(location: Int, flags: Int) = implot3d_setup_legend(location, flags)

    // ==================== Plot items (double data arrays) ====================

    actual fun plotScatter(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, spec: ImPlot3DSpec) = withSpec(spec) { s ->
        withThreeArrays(xs, ys, zs) { xp, yp, zp, count ->
            implot3d_plot_scatter(labelId, xp, yp, zp, count, s)
        }
    }

    actual fun plotLine(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, spec: ImPlot3DSpec) = withSpec(spec) { s ->
        withThreeArrays(xs, ys, zs) { xp, yp, zp, count ->
            implot3d_plot_line(labelId, xp, yp, zp, count, s)
        }
    }

    actual fun plotTriangle(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, spec: ImPlot3DSpec) = withSpec(spec) { s ->
        withThreeArrays(xs, ys, zs) { xp, yp, zp, count ->
            implot3d_plot_triangle(labelId, xp, yp, zp, count, s)
        }
    }

    actual fun plotQuad(labelId: String, xs: DoubleArray, ys: DoubleArray, zs: DoubleArray, spec: ImPlot3DSpec) = withSpec(spec) { s ->
        withThreeArrays(xs, ys, zs) { xp, yp, zp, count ->
            implot3d_plot_quad(labelId, xp, yp, zp, count, s)
        }
    }

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
    ) = withSpec(spec) { s ->
        withThreeArrays(xs, ys, zs) { xp, yp, zp, _ ->
            implot3d_plot_surface(labelId, xp, yp, zp, xCount, yCount, scaleMin, scaleMax, s)
        }
    }

    actual fun plotMesh(
        labelId: String,
        vtxXs: DoubleArray,
        vtxYs: DoubleArray,
        vtxZs: DoubleArray,
        idxs: IntArray,
        spec: ImPlot3DSpec,
    ) = withSpec(spec) { s ->
        withThreeArrays(vtxXs, vtxYs, vtxZs) { xp, yp, zp, vtxCount ->
            withUIntArray(idxs) { idxp, idxCount ->
                implot3d_plot_mesh(labelId, xp, yp, zp, idxp, vtxCount, idxCount, s)
            }
        }
    }

    actual fun plotText(text: String, x: Double, y: Double, z: Double, angle: Double, pixOffset: ImVec2) = withVec2(pixOffset) {
        implot3d_plot_text(text, x, y, z, angle, it)
    }

    actual fun plotDummy(labelId: String, spec: ImPlot3DSpec) = withSpec(spec) {
        implot3d_plot_dummy(labelId, it)
    }

    // ==================== Plot utils ====================

    actual fun plotToPixels(point: ImPlot3DPoint): ImVec2 = withPoint(point) {
        implot3d_plot_to_pixels_point(it).useContents { ImVec2(x, y) }
    }

    actual fun plotToPixels(x: Double, y: Double, z: Double): ImVec2 =
        implot3d_plot_to_pixels_xyz(x, y, z).useContents { ImVec2(this.x, this.y) }

    actual fun pixelsToPlotRay(pix: ImVec2): ImPlot3DRay = withVec2(pix) {
        implot3d_pixels_to_plot_ray_vec2(it).useContents {
            ImPlot3DRay(
                ImPlot3DPoint(origin.x, origin.y, origin.z),
                ImPlot3DPoint(direction.x, direction.y, direction.z),
            )
        }
    }

    actual fun pixelsToPlotRay(x: Double, y: Double): ImPlot3DRay =
        implot3d_pixels_to_plot_ray_xy(x, y).useContents {
            ImPlot3DRay(
                ImPlot3DPoint(origin.x, origin.y, origin.z),
                ImPlot3DPoint(direction.x, direction.y, direction.z),
            )
        }

    actual fun pixelsToPlotPlane(pix: ImVec2, plane: Int, mask: Boolean): ImPlot3DPoint = withVec2(pix) {
        implot3d_pixels_to_plot_plane_vec2(it, plane, mask).useContents { ImPlot3DPoint(x, y, z) }
    }

    actual fun pixelsToPlotPlane(x: Double, y: Double, plane: Int, mask: Boolean): ImPlot3DPoint =
        implot3d_pixels_to_plot_plane_xy(x, y, plane, mask).useContents { ImPlot3DPoint(x, y, z) }

    actual fun getPlotRectPos(): ImVec2 =
        implot3d_get_plot_rect_pos().useContents { ImVec2(x, y) }

    actual fun getPlotRectSize(): ImVec2 =
        implot3d_get_plot_rect_size().useContents { ImVec2(x, y) }

    actual fun getPlotDrawList(): ImDrawList = NativeImDrawList(implot3d_get_plot_draw_list())

    // ==================== Style ====================

    actual fun getStyle(): ImPlot3DStyle = memScoped {
        val c = alloc<implot3d_style>()
        implot3d_get_style(c.ptr)
        readStyle(c.ptr)
    }

    actual fun setStyle(style: ImPlot3DStyle) = memScoped {
        val c = alloc<implot3d_style>()
        fillStyle(c.ptr, style)
        implot3d_set_style(c.ptr)
    }

    /**
     * Applies ImPlot3D's auto color scheme and returns the resulting style.
     * When [dst] is provided, the C style is initialized from its fields, the auto
     * colors are written into it, and the updated [dst] is returned. When [dst] is
     * null, a fresh style is built, the auto colors are applied, and that new style
     * is returned.
     */
    actual fun styleColorsAuto(dst: ImPlot3DStyle?): ImPlot3DStyle? = memScoped {
        val c = alloc<implot3d_style>()
        fillStyle(c.ptr, dst ?: ImPlot3DStyle())
        implot3d_style_colors_auto(c.ptr)
        readStyle(c.ptr)
    }

    /**
     * Applies ImPlot3D's dark color scheme and returns the resulting style.
     * See [styleColorsAuto] for the [dst] semantics.
     */
    actual fun styleColorsDark(dst: ImPlot3DStyle?): ImPlot3DStyle? = memScoped {
        val c = alloc<implot3d_style>()
        fillStyle(c.ptr, dst ?: ImPlot3DStyle())
        implot3d_style_colors_dark(c.ptr)
        readStyle(c.ptr)
    }

    /**
     * Applies ImPlot3D's light color scheme and returns the resulting style.
     * See [styleColorsAuto] for the [dst] semantics.
     */
    actual fun styleColorsLight(dst: ImPlot3DStyle?): ImPlot3DStyle? = memScoped {
        val c = alloc<implot3d_style>()
        fillStyle(c.ptr, dst ?: ImPlot3DStyle())
        implot3d_style_colors_light(c.ptr)
        readStyle(c.ptr)
    }

    /**
     * Applies ImPlot3D's classic color scheme and returns the resulting style.
     * See [styleColorsAuto] for the [dst] semantics.
     */
    actual fun styleColorsClassic(dst: ImPlot3DStyle?): ImPlot3DStyle? = memScoped {
        val c = alloc<implot3d_style>()
        fillStyle(c.ptr, dst ?: ImPlot3DStyle())
        implot3d_style_colors_classic(c.ptr)
        readStyle(c.ptr)
    }

    actual fun pushStyleColor(idx: Int, color: ImVec4) = withVec4(color) {
        implot3d_push_style_color_vec4(idx, it)
    }

    actual fun pushStyleColor(idx: Int, color: Int) = implot3d_push_style_color_u32(idx, color.toUInt())

    actual fun popStyleColor(count: Int) = implot3d_pop_style_color(count)

    actual fun pushStyleVar(idx: Int, value: Float) = implot3d_push_style_var_float(idx, value)

    actual fun pushStyleVar(idx: Int, value: Int) = implot3d_push_style_var_int(idx, value)

    actual fun pushStyleVar(idx: Int, value: ImVec2) = withVec2(value) {
        implot3d_push_style_var_vec2(idx, it)
    }

    actual fun popStyleVar(count: Int) = implot3d_pop_style_var(count)

    actual fun getStyleColor(idx: Int): ImVec4 =
        implot3d_get_style_color_vec4(idx).useContents { ImVec4(x, y, z, w) }

    actual fun getStyleColorU32(idx: Int): Int = implot3d_get_style_color_u32(idx).toInt()

    actual fun nextMarker(): Int = implot3d_next_marker()

    // ==================== Colormaps ====================

    actual fun addColormap(name: String, cols: Array<ImVec4>, qual: Boolean): Int = memScoped {
        if (cols.isEmpty()) {
            implot3d_add_colormap_vec4(name, null, 0, qual)
        } else {
            val arr = allocArray<imgui_vec4>(cols.size)
            for (i in cols.indices) {
                val v = cols[i]
                arr[i].x = v.x
                arr[i].y = v.y
                arr[i].z = v.z
                arr[i].w = v.w
            }
            implot3d_add_colormap_vec4(name, arr, cols.size, qual)
        }
    }

    actual fun addColormap(name: String, cols: IntArray, qual: Boolean): Int = memScoped {
        if (cols.isEmpty()) {
            implot3d_add_colormap_u32(name, null, 0, qual)
        } else {
            val arr = allocArray<UIntVar>(cols.size)
            for (i in cols.indices) {
                arr[i] = cols[i].toUInt()
            }
            implot3d_add_colormap_u32(name, arr, cols.size, qual)
        }
    }

    actual fun getColormapCount(): Int = implot3d_get_colormap_count()

    actual fun getColormapName(cmap: Int): String = implot3d_get_colormap_name(cmap)?.toKString() ?: ""

    actual fun getColormapIndex(name: String): Int = implot3d_get_colormap_index(name)

    actual fun pushColormap(cmap: Int) = implot3d_push_colormap(cmap)

    actual fun pushColormap(name: String) = implot3d_push_colormap_name(name)

    actual fun popColormap(count: Int) = implot3d_pop_colormap(count)

    actual fun nextColormapColor(): ImVec4 =
        implot3d_next_colormap_color().useContents { ImVec4(x, y, z, w) }

    actual fun getColormapSize(cmap: Int): Int = implot3d_get_colormap_size(cmap)

    actual fun getColormapColor(idx: Int, cmap: Int): ImVec4 =
        implot3d_get_colormap_color(idx, cmap).useContents { ImVec4(x, y, z, w) }

    actual fun sampleColormap(t: Float, cmap: Int): ImVec4 =
        implot3d_sample_colormap(t, cmap).useContents { ImVec4(x, y, z, w) }

    // ==================== Demo ====================

    actual fun showDemoWindow(pOpen: BooleanArray?) = memScoped {
        if (pOpen != null) {
            val b = alloc<BooleanVar>()
            b.value = pOpen[0]
            implot3d_show_demo_window(b.ptr)
            pOpen[0] = b.value
        } else {
            implot3d_show_demo_window(null)
        }
    }

    actual fun showAllDemos() = implot3d_show_all_demos()

    actual fun showStyleEditor() = implot3d_show_style_editor()

    actual fun showStyleSelector(label: String): Boolean = implot3d_show_style_selector(label)

    actual fun showColormapSelector(label: String): Boolean = implot3d_show_colormap_selector(label)

    actual fun showMetricsWindow(pOpen: BooleanArray?) = memScoped {
        if (pOpen != null) {
            val b = alloc<BooleanVar>()
            b.value = pOpen[0]
            implot3d_show_metrics_window(b.ptr)
            pOpen[0] = b.value
        } else {
            implot3d_show_metrics_window(null)
        }
    }

    actual fun showAboutWindow(pOpen: BooleanArray?) = memScoped {
        if (pOpen != null) {
            val b = alloc<BooleanVar>()
            b.value = pOpen[0]
            implot3d_show_about_window(b.ptr)
            pOpen[0] = b.value
        } else {
            implot3d_show_about_window(null)
        }
    }

    // ==================== Built-in meshes ====================

    actual fun cubeVertices(): Array<ImPlot3DPoint> {
        val p = implot3d_cube_vtx() ?: return emptyArray()
        val count = implot3d_cube_vtx_count()
        return Array(count) { i -> p[i].let { ImPlot3DPoint(it.x, it.y, it.z) } }
    }

    actual fun cubeIndices(): IntArray {
        val p = implot3d_cube_idx() ?: return IntArray(0)
        val count = implot3d_cube_idx_count()
        return IntArray(count) { i -> p[i].toInt() }
    }

    actual fun sphereVertices(): Array<ImPlot3DPoint> {
        val p = implot3d_sphere_vtx() ?: return emptyArray()
        val count = implot3d_sphere_vtx_count()
        return Array(count) { i -> p[i].let { ImPlot3DPoint(it.x, it.y, it.z) } }
    }

    actual fun sphereIndices(): IntArray {
        val p = implot3d_sphere_idx() ?: return IntArray(0)
        val count = implot3d_sphere_idx_count()
        return IntArray(count) { i -> p[i].toInt() }
    }

    actual fun duckVertices(): Array<ImPlot3DPoint> {
        val p = implot3d_duck_vtx() ?: return emptyArray()
        val count = implot3d_duck_vtx_count()
        return Array(count) { i -> p[i].let { ImPlot3DPoint(it.x, it.y, it.z) } }
    }

    actual fun duckIndices(): IntArray {
        val p = implot3d_duck_idx() ?: return IntArray(0)
        val count = implot3d_duck_idx_count()
        return IntArray(count) { i -> p[i].toInt() }
    }

    // ==================== Point math ====================

    actual fun pointAdd(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint = withTwoPoints(a, b) { av, bv ->
        implot3d_point_add(av, bv).useContents { ImPlot3DPoint(x, y, z) }
    }

    actual fun pointSub(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint = withTwoPoints(a, b) { av, bv ->
        implot3d_point_sub(av, bv).useContents { ImPlot3DPoint(x, y, z) }
    }

    actual fun pointMul(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint = withTwoPoints(a, b) { av, bv ->
        implot3d_point_mul(av, bv).useContents { ImPlot3DPoint(x, y, z) }
    }

    actual fun pointDiv(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint = withTwoPoints(a, b) { av, bv ->
        implot3d_point_div(av, bv).useContents { ImPlot3DPoint(x, y, z) }
    }

    actual fun pointMulScalar(a: ImPlot3DPoint, scalar: Double): ImPlot3DPoint = withPoint(a) {
        implot3d_point_mul_double(it, scalar).useContents { ImPlot3DPoint(x, y, z) }
    }

    actual fun pointDivScalar(a: ImPlot3DPoint, scalar: Double): ImPlot3DPoint = withPoint(a) {
        implot3d_point_div_double(it, scalar).useContents { ImPlot3DPoint(x, y, z) }
    }

    actual fun pointNeg(a: ImPlot3DPoint): ImPlot3DPoint = withPoint(a) {
        implot3d_point_neg(it).useContents { ImPlot3DPoint(x, y, z) }
    }

    actual fun pointDot(a: ImPlot3DPoint, b: ImPlot3DPoint): Double = withTwoPoints(a, b) { av, bv ->
        implot3d_point_dot(av, bv)
    }

    actual fun pointCross(a: ImPlot3DPoint, b: ImPlot3DPoint): ImPlot3DPoint = withTwoPoints(a, b) { av, bv ->
        implot3d_point_cross(av, bv).useContents { ImPlot3DPoint(x, y, z) }
    }

    actual fun pointLength(a: ImPlot3DPoint): Double = withPoint(a) {
        implot3d_point_length(it)
    }

    actual fun pointLengthSquared(a: ImPlot3DPoint): Double = withPoint(a) {
        implot3d_point_length_squared(it)
    }

    actual fun pointNormalized(a: ImPlot3DPoint): ImPlot3DPoint = withPoint(a) {
        implot3d_point_normalized(it).useContents { ImPlot3DPoint(x, y, z) }
    }

    actual fun pointIsNaN(a: ImPlot3DPoint): Boolean = withPoint(a) {
        implot3d_point_is_nan(it)
    }

    actual fun pointEq(a: ImPlot3DPoint, b: ImPlot3DPoint): Boolean = withTwoPoints(a, b) { av, bv ->
        implot3d_point_eq(av, bv)
    }

    // ==================== Quat math ====================

    actual fun quatFromAngleAxis(angle: Double, axis: ImPlot3DPoint): ImPlot3DQuat = withPoint(axis) {
        implot3d_quat_from_angle_axis(angle, it).useContents { ImPlot3DQuat(x, y, z, w) }
    }

    actual fun quatFromTwoVectors(v0: ImPlot3DPoint, v1: ImPlot3DPoint): ImPlot3DQuat = withTwoPoints(v0, v1) { av, bv ->
        implot3d_quat_from_two_vectors(av, bv).useContents { ImPlot3DQuat(x, y, z, w) }
    }

    actual fun quatFromElAz(elevation: Double, azimuth: Double): ImPlot3DQuat =
        implot3d_quat_from_el_az(elevation, azimuth).useContents { ImPlot3DQuat(x, y, z, w) }

    actual fun quatMul(a: ImPlot3DQuat, b: ImPlot3DQuat): ImPlot3DQuat = withTwoQuats(a, b) { av, bv ->
        implot3d_quat_mul(av, bv).useContents { ImPlot3DQuat(x, y, z, w) }
    }

    actual fun quatRotatePoint(q: ImPlot3DQuat, p: ImPlot3DPoint): ImPlot3DPoint = memScoped {
        val qv = alloc<implot3d_quat>()
        qv.x = q.x
        qv.y = q.y
        qv.z = q.z
        qv.w = q.w
        val pv = alloc<implot3d_point>()
        pv.x = p.x
        pv.y = p.y
        pv.z = p.z
        implot3d_quat_rotate_point(qv.readValue(), pv.readValue()).useContents { ImPlot3DPoint(x, y, z) }
    }

    actual fun quatNormalized(q: ImPlot3DQuat): ImPlot3DQuat = withQuat(q) {
        implot3d_quat_normalized(it).useContents { ImPlot3DQuat(x, y, z, w) }
    }

    actual fun quatConjugate(q: ImPlot3DQuat): ImPlot3DQuat = withQuat(q) {
        implot3d_quat_conjugate(it).useContents { ImPlot3DQuat(x, y, z, w) }
    }

    actual fun quatInverse(q: ImPlot3DQuat): ImPlot3DQuat = withQuat(q) {
        implot3d_quat_inverse(it).useContents { ImPlot3DQuat(x, y, z, w) }
    }

    actual fun quatLength(q: ImPlot3DQuat): Double = withQuat(q) {
        implot3d_quat_length(it)
    }

    actual fun quatDot(a: ImPlot3DQuat, b: ImPlot3DQuat): Double = withTwoQuats(a, b) { av, bv ->
        implot3d_quat_dot(av, bv)
    }

    actual fun quatSlerp(q1: ImPlot3DQuat, q2: ImPlot3DQuat, t: Double): ImPlot3DQuat = withTwoQuats(q1, q2) { av, bv ->
        implot3d_quat_slerp(av, bv, t).useContents { ImPlot3DQuat(x, y, z, w) }
    }

    actual fun quatEq(a: ImPlot3DQuat, b: ImPlot3DQuat): Boolean = withTwoQuats(a, b) { av, bv ->
        implot3d_quat_eq(av, bv)
    }
}