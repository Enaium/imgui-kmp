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

package cn.enaium.imgui.example.implot3d

import cn.enaium.imgui.ImGui
import cn.enaium.imgui.ImGuiCond
import cn.enaium.imgui.ImGuiWindowFlags
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import cn.enaium.imgui.example.common.SdlRendererApp
import cn.enaium.imgui.extensions.implot3d.ImPlot3D
import cn.enaium.imgui.extensions.implot3d.ImPlot3DAxisFlags
import cn.enaium.imgui.extensions.implot3d.ImPlot3DCol
import cn.enaium.imgui.extensions.implot3d.ImPlot3DCond
import cn.enaium.imgui.extensions.implot3d.ImPlot3DContext
import cn.enaium.imgui.extensions.implot3d.ImPlot3DFlags
import cn.enaium.imgui.extensions.implot3d.ImPlot3DItemFlags
import cn.enaium.imgui.extensions.implot3d.ImPlot3DMarker
import cn.enaium.imgui.extensions.implot3d.ImPlot3DSpec
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * The ImPlot3D bindings, laid out like the upstream ShowDemoWindow: a menu
 * bar and a tab bar ("Plots"/"Axes"/"Tools"), with each demo behind a tree
 * node. The data generation and most widget interactions mirror the official
 * implot3d_demo.cpp so the two demos behave the same way:
 *
 * - Line Plots: two animated/parametric curves
 * - Scatter Plots: two randomized point clouds
 * - Surface Plots: an animated wave, solid/colormap fill, flags
 * - Mesh Plots: Duck / Sphere / Cube with editable colors and flags
 * - Box Scale / Box Rotation: axis lines with animated rotation
 * - Markers and Text: every marker style, plus rotated text
 * - NaN Values: how NaNs are rendered
 *
 * The ImPlot3D context is created once up front (before the frame loop) and
 * destroyed on close. Run with `./gradlew :examples:implot3d:jvmRun` (JVM) or
 * the per-target native binaries; pass `--frames N` / `IMGUI_KMP_FRAMES=N` to
 * exit after N frames (headless CI runs).
 */
fun runImPlot3DExample(frames: Int = Int.MAX_VALUE) {
    var demo: ImPlot3DDemo? = null
    SdlRendererApp.run(
        title = "imgui-kmp implot3d example",
        frames = frames,
        init = { demo = ImPlot3DDemo() },
        draw = { frame -> demo?.draw(frame) },
        close = { demo?.close() },
    )
}

private class ImPlot3DDemo {

    // A single ImPlot3D context lives for the whole app: ImPlot3D acts on the
    // current context, so creating it once here keeps every plot in one state.
    private val context: ImPlot3DContext = ImPlot3D.createContext()

    private val showAbout = BooleanArray(1)

    // ---------------------------------------------------------------
    // Line Plots — same data as the upstream demo
    // ---------------------------------------------------------------
    private val lineXs1 = FloatArray(1001)
    private val lineYs1 = FloatArray(1001)
    private val lineZs1 = FloatArray(1001)
    private val lineXs2 = DoubleArray(20)
    private val lineYs2 = DoubleArray(20)
    private val lineZs2 = DoubleArray(20)

    // ---------------------------------------------------------------
    // Scatter Plots — same seeded data as the upstream demo
    // ---------------------------------------------------------------
    private val scatterRandom = Random(0)
    private val scatterXs1 = FloatArray(100)
    private val scatterYs1 = FloatArray(100)
    private val scatterZs1 = FloatArray(100)
    private val scatterXs2 = FloatArray(50)
    private val scatterYs2 = FloatArray(50)
    private val scatterZs2 = FloatArray(50)

    // ---------------------------------------------------------------
    // Surface Plots — animated wave, same grid as the upstream demo
    // ---------------------------------------------------------------
    private val surfaceN = 20
    private val surfaceXs = FloatArray(surfaceN * surfaceN)
    private val surfaceYs = FloatArray(surfaceN * surfaceN)
    private val surfaceZs = FloatArray(surfaceN * surfaceN)
    private val surfaceSelectedFill = IntArray(1) { 1 }
    private val surfaceSelColormap = IntArray(1) { 5 }
    private val surfaceSolidColor = FloatArray(4) { floatArrayOf(0.8f, 0.8f, 0.2f, 0.6f)[it] }
    private val surfaceCustomRange = BooleanArray(1)
    private val surfaceRangeMin = FloatArray(1) { -1f }
    private val surfaceRangeMax = FloatArray(1) { 1f }
    private val surfaceFlags = IntArray(1) { ImPlot3DSurfaceFlags.NO_MARKERS }

    // ---------------------------------------------------------------
    // Mesh Plots — Duck / Sphere / Cube
    // ---------------------------------------------------------------
    private val meshId = IntArray(1)
    private val meshLineColor = FloatArray(4) { floatArrayOf(0.5f, 0.5f, 0.2f, 0.6f)[it] }
    private val meshFillColor = FloatArray(4) { floatArrayOf(0.8f, 0.8f, 0.2f, 0.6f)[it] }
    private val meshMarkerColor = FloatArray(4) { floatArrayOf(0.5f, 0.5f, 0.2f, 0.6f)[it] }
    private val meshFlags = IntArray(1) { ImPlot3DItemFlags.NONE }
    private val meshVtxXs: DoubleArray
    private val meshVtxYs: DoubleArray
    private val meshVtxZs: DoubleArray
    private val meshIdx: IntArray

    // ---------------------------------------------------------------
    // Box rotation
    // ---------------------------------------------------------------
    private val rotElevation = FloatArray(1) { 45f }
    private val rotAzimuth = FloatArray(1) { -135f }
    private val rotAnimate = BooleanArray(1)
    private val rotInitElevation = FloatArray(1) { 45f }
    private val rotInitAzimuth = FloatArray(1) { -135f }
    private val boxScale = FloatArray(1) { 1f }

    // ---------------------------------------------------------------
    // Markers and Text
    // ---------------------------------------------------------------
    private val mkSize = FloatArray(1) { ImPlot3D.getStyle().markerSize }
    private val mkWeight = FloatArray(1) { ImPlot3D.getStyle().lineWeight }

    // ---------------------------------------------------------------
    // NaN demo
    // ---------------------------------------------------------------
    private val nanXs = DoubleArray(20)
    private val nanYs = DoubleArray(20)
    private val nanZs = DoubleArray(20)

    init {
        // Line Plots
        for (i in 0..1000) {
            val x = i * 0.001f
            lineXs1[i] = x
            lineYs1[i] = 0.5f + 0.5f * cos(50 * (x + ImGui.getTime().toFloat() / 10))
            lineZs1[i] = 0.5f + 0.5f * sin(50 * (x + ImGui.getTime().toFloat() / 10))
        }
        for (i in 0 until 20) {
            lineXs2[i] = i * 1.0 / 19.0
            lineYs2[i] = lineXs2[i] * lineXs2[i]
            lineZs2[i] = lineXs2[i] * lineYs2[i]
        }

        // Scatter Plots (seed 0, matching the C rand() after srand(0) on the
        // platforms the upstream demo targets; values differ slightly across
        // libc implementations but stay in the same shape).
        for (i in 0 until 100) {
            scatterXs1[i] = i * 0.01f
            scatterYs1[i] = scatterXs1[i] + 0.1f * scatterRandom.nextFloat()
            scatterZs1[i] = scatterXs1[i] + 0.1f * scatterRandom.nextFloat()
        }
        for (i in 0 until 50) {
            scatterXs2[i] = 0.25f + 0.2f * scatterRandom.nextFloat()
            scatterYs2[i] = 0.50f + 0.2f * scatterRandom.nextFloat()
            scatterZs2[i] = 0.75f + 0.2f * scatterRandom.nextFloat()
        }

        // Surface: z = sin(2t + sqrt(x^2 + y^2)) over [-1,1]^2
        val minVal = -1.0f
        val maxVal = 1.0f
        val step = (maxVal - minVal) / (surfaceN - 1)
        for (i in 0 until surfaceN) {
            for (j in 0 until surfaceN) {
                val idx = i * surfaceN + j
                surfaceXs[idx] = minVal + j * step
                surfaceYs[idx] = minVal + i * step
            }
        }

        // Mesh data from the built-in mesh buffers
        val meshes = listOf(
            ImPlot3D.duckVertices() to ImPlot3D.duckIndices(),
            ImPlot3D.sphereVertices() to ImPlot3D.sphereIndices(),
            ImPlot3D.cubeVertices() to ImPlot3D.cubeIndices(),
        )
        val (vtx, idx) = meshes[0]
        meshVtxXs = DoubleArray(vtx.size) { vtx[it].x }
        meshVtxYs = DoubleArray(vtx.size) { vtx[it].y }
        meshVtxZs = DoubleArray(vtx.size) { vtx[it].z }
        meshIdx = idx

        // NaN demo
        for (i in 0 until 20) {
            nanXs[i] = i / 19.0
            nanYs[i] = if (i == 4 || i == 9 || i == 14) Double.NaN else sin(10 * nanXs[i])
            nanZs[i] = cos(10 * nanXs[i])
        }
    }

    fun draw(frame: Int) {
        if (showAbout[0]) {
            ImPlot3D.showAboutWindow(showAbout)
        }

        ImGui.setNextWindowPos(ImVec2(100f, 100f), ImGuiCond.FIRST_USE_EVER)
        ImGui.setNextWindowSize(ImVec2(600f, 750f), ImGuiCond.FIRST_USE_EVER)
        ImGui.begin("ImPlot3D Demo", flags = ImGuiWindowFlags.MENU_BAR)
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("Tools")) {
                if (ImGui.menuItem("About ImPlot3D")) showAbout[0] = true
                ImGui.endMenu()
            }
            ImGui.endMenuBar()
        }
        if (ImGui.beginTabBar("ImPlot3DDemoTabs")) {
            if (ImGui.beginTabItem("Plots")) {
                ImGui.separatorText("Plot Types")
                demoHeader("Line Plots") { demoLinePlots() }
                demoHeader("Scatter Plots") { demoScatterPlots() }
                demoHeader("Surface Plots") { demoSurfacePlots() }
                demoHeader("Mesh Plots") { demoMeshPlots() }
                demoHeader("NaN Values") { demoNaNValues() }
                ImGui.endTabItem()
            }
            if (ImGui.beginTabItem("Axes")) {
                demoHeader("Box Scale") { demoBoxScale() }
                demoHeader("Box Rotation") { demoBoxRotation() }
                ImGui.endTabItem()
            }
            if (ImGui.beginTabItem("Tools")) {
                demoHeader("Markers and Text") { demoMarkersAndText() }
                ImGui.endTabItem()
            }
            ImGui.endTabBar()
        }
        ImGui.text("frame: $frame")
        ImGui.end()
    }

    private inline fun demoHeader(label: String, demo: () -> Unit) {
        if (ImGui.treeNode(label)) {
            demo()
            ImGui.treePop()
        }
    }

    // ===============================================================
    // Plots / Line Plots
    // ===============================================================
    private fun demoLinePlots() {
        // Refresh the animated curve every frame like the upstream demo.
        for (i in 0..1000) {
            val x = lineXs1[i]
            lineYs1[i] = 0.5f + 0.5f * cos(50 * (x + ImGui.getTime().toFloat() / 10))
            lineZs1[i] = 0.5f + 0.5f * sin(50 * (x + ImGui.getTime().toFloat() / 10))
        }
        if (ImPlot3D.beginPlot("Line Plots")) {
            ImPlot3D.setupAxes("x", "y", "z")
            ImPlot3D.plotLine("f(x)", lineXs1.toDoubles(), lineYs1.toDoubles(), lineZs1.toDoubles())
            ImPlot3D.plotLine(
                "g(x)",
                lineXs2, lineYs2, lineZs2,
                ImPlot3DSpec(marker = ImPlot3DMarker.CIRCLE, flags = 1 shl 10), // ImPlot3DLineFlags_Segments
            )
            ImPlot3D.endPlot()
        }
    }

    // ===============================================================
    // Plots / Scatter Plots
    // ===============================================================
    private fun demoScatterPlots() {
        if (ImPlot3D.beginPlot("Scatter Plots")) {
            ImPlot3D.plotScatter(
                "Data 1",
                scatterXs1.toDoubles(), scatterYs1.toDoubles(), scatterZs1.toDoubles(),
            )
            ImPlot3D.plotScatter(
                "Data 2",
                scatterXs2.toDoubles(), scatterYs2.toDoubles(), scatterZs2.toDoubles(),
                ImPlot3DSpec(
                    marker = ImPlot3DMarker.SQUARE,
                    markerSize = 6f,
                    markerLineColor = ImPlot3D.getColormapColor(1),
                    markerFillColor = ImPlot3D.getColormapColor(1),
                    fillAlpha = 0.25f,
                ),
            )
            ImPlot3D.endPlot()
        }
    }

    // ===============================================================
    // Plots / Surface Plots
    // ===============================================================
    private fun demoSurfacePlots() {
        val t = ImGui.getTime().toFloat()
        for (i in 0 until surfaceN) {
            for (j in 0 until surfaceN) {
                val idx = i * surfaceN + j
                val x = surfaceXs[idx]
                val y = surfaceYs[idx]
                surfaceZs[idx] = sin(2 * t + sqrt((x * x + y * y).toDouble()).toFloat())
            }
        }

        val colormaps = arrayOf("Viridis", "Plasma", "Hot", "Cool", "Pink", "Jet", "Twilight", "RdBu", "BrBG", "PiYG", "Spectral", "Greys")
        ImGui.text("Fill color")
        ImGui.indent()
        if (ImGui.radioButton("Solid", surfaceSelectedFill[0] == 0)) surfaceSelectedFill[0] = 0
        if (surfaceSelectedFill[0] == 0) {
            ImGui.sameLine()
            ImGui.colorEdit4("##SurfaceSolidColor", surfaceSolidColor)
        }
        if (ImGui.radioButton("Colormap", surfaceSelectedFill[0] == 1)) surfaceSelectedFill[0] = 1
        if (surfaceSelectedFill[0] == 1) {
            ImGui.sameLine()
            ImGui.combo("##SurfaceColormap", surfaceSelColormap, colormaps)
        }
        ImGui.unindent()

        val fillFromColormap = surfaceSelectedFill[0] == 1
        if (!fillFromColormap) ImGui.beginDisabled()
        ImGui.checkbox("Custom range", surfaceCustomRange)
        ImGui.indent()
        if (!surfaceCustomRange[0]) ImGui.beginDisabled()
        ImGui.sliderFloat("Range min", surfaceRangeMin, -1f, surfaceRangeMax[0] - 0.01f)
        ImGui.sliderFloat("Range max", surfaceRangeMax, surfaceRangeMin[0] + 0.01f, 1f)
        if (!surfaceCustomRange[0]) ImGui.endDisabled()
        ImGui.unindent()
        if (!fillFromColormap) ImGui.endDisabled()

        ImGui.checkboxFlags("No Lines", surfaceFlags, ImPlot3DSurfaceFlags.NO_LINES)
        ImGui.checkboxFlags("No Fill", surfaceFlags, ImPlot3DSurfaceFlags.NO_FILL)

        if (fillFromColormap) ImPlot3D.pushColormap(colormaps[surfaceSelColormap[0]])
        if (ImPlot3D.beginPlot("Surface Plots", ImVec2(-1f, 0f), ImPlot3DFlags.NO_CLIP)) {
            ImPlot3D.setupAxesLimits(-1.0, 1.0, -1.0, 1.0, -1.5, 1.5)
            val spec = ImPlot3DSpec(
                fillAlpha = 0.8f,
                flags = surfaceFlags[0],
                marker = ImPlot3DMarker.SQUARE,
                lineColor = ImPlot3D.getColormapColor(1),
                fillColor = if (surfaceSelectedFill[0] == 0) {
                    ImVec4(surfaceSolidColor[0], surfaceSolidColor[1], surfaceSolidColor[2], surfaceSolidColor[3])
                } else {
                    null
                },
            )
            if (surfaceCustomRange[0]) {
                ImPlot3D.plotSurface(
                    "Wave Surface", surfaceXs.toDoubles(), surfaceYs.toDoubles(), surfaceZs.toDoubles(),
                    surfaceN, surfaceN, surfaceRangeMin[0].toDouble(), surfaceRangeMax[0].toDouble(), spec,
                )
            } else {
                ImPlot3D.plotSurface(
                    "Wave Surface", surfaceXs.toDoubles(), surfaceYs.toDoubles(), surfaceZs.toDoubles(),
                    surfaceN, surfaceN, 0.0, 0.0, spec,
                )
            }
            ImPlot3D.endPlot()
        }
        if (fillFromColormap) ImPlot3D.popColormap()
    }

    // ===============================================================
    // Plots / Mesh Plots
    // ===============================================================
    private fun demoMeshPlots() {
        ImGui.combo("Mesh", meshId, arrayOf("Duck", "Sphere", "Cube"))
        ImGui.colorEdit4("Line Color##Mesh", meshLineColor)
        ImGui.colorEdit4("Fill Color##Mesh", meshFillColor)
        ImGui.colorEdit4("Marker Color##Mesh", meshMarkerColor)
        ImGui.checkboxFlags("No Lines", meshFlags, 1 shl 10)   // ImPlot3DMeshFlags_NoLines
        ImGui.checkboxFlags("No Fill", meshFlags, 1 shl 11)    // ImPlot3DMeshFlags_NoFill
        ImGui.checkboxFlags("No Markers", meshFlags, 1 shl 12) // ImPlot3DMeshFlags_NoMarkers

        if (ImPlot3D.beginPlot("Mesh Plots")) {
            ImPlot3D.setupAxesLimits(-1.0, 1.0, -1.0, 1.0, -1.0, 1.0)
            val spec = ImPlot3DSpec(
                flags = meshFlags[0],
                fillColor = ImVec4(meshFillColor[0], meshFillColor[1], meshFillColor[2], meshFillColor[3]),
                lineColor = ImVec4(meshLineColor[0], meshLineColor[1], meshLineColor[2], meshLineColor[3]),
                marker = ImPlot3DMarker.SQUARE,
                markerSize = 3f,
                markerLineColor = ImVec4(meshMarkerColor[0], meshMarkerColor[1], meshMarkerColor[2], meshMarkerColor[3]),
                markerFillColor = ImVec4(meshMarkerColor[0], meshMarkerColor[1], meshMarkerColor[2], meshMarkerColor[3]),
            )
            when (meshId[0]) {
                0 -> plotMeshByName("Duck", ImPlot3D.duckVertices(), ImPlot3D.duckIndices(), spec)
                1 -> plotMeshByName("Sphere", ImPlot3D.sphereVertices(), ImPlot3D.sphereIndices(), spec)
                else -> plotMeshByName("Cube", ImPlot3D.cubeVertices(), ImPlot3D.cubeIndices(), spec)
            }
            ImPlot3D.endPlot()
        }
    }

    private fun plotMeshByName(label: String, vtx: Array<cn.enaium.imgui.extensions.implot3d.ImPlot3DPoint>, idx: IntArray, spec: ImPlot3DSpec) {
        ImPlot3D.plotMesh(
            label,
            DoubleArray(vtx.size) { vtx[it].x },
            DoubleArray(vtx.size) { vtx[it].y },
            DoubleArray(vtx.size) { vtx[it].z },
            idx,
            spec,
        )
    }

    // ===============================================================
    // Plots / NaN Values
    // ===============================================================
    private fun demoNaNValues() {
        if (ImPlot3D.beginPlot("NaN Values", ImVec2(-1f, 0f), ImPlot3DFlags.NO_LEGEND)) {
            ImPlot3D.setupAxes(null, null, null)
            ImPlot3D.plotLine("NaN", nanXs, nanYs, nanZs)
            ImPlot3D.endPlot()
        }
    }

    // ===============================================================
    // Axes / Box Scale
    // ===============================================================
    private fun demoBoxScale() {
        ImGui.sliderFloat("Scale", boxScale, 0.5f, 2f, "%.1f")
        if (ImPlot3D.beginPlot("##BoxScale")) {
            ImPlot3D.setupAxesLimits(-1.0, 1.0, -1.0, 1.0, -1.0, 1.0, ImPlot3DCond.ALWAYS)
            ImPlot3D.setupBoxScale(boxScale[0].toDouble(), boxScale[0].toDouble(), boxScale[0].toDouble())
            plotAxisLines()
            ImPlot3D.endPlot()
        }
    }

    // ===============================================================
    // Axes / Box Rotation
    // ===============================================================
    private fun demoBoxRotation() {
        ImGui.text("Rotation")
        var changed = false
        if (ImGui.sliderFloat("Elevation", rotElevation, -90f, 90f, "%.1f degrees")) changed = true
        if (ImGui.sliderFloat("Azimuth", rotAzimuth, -180f, 180f, "%.1f degrees")) changed = true
        ImGui.checkbox("Animate", rotAnimate)

        ImGui.text("Initial Rotation")
        ImGui.sliderFloat("Initial Elevation", rotInitElevation, -90f, 90f, "%.1f degrees")
        ImGui.sliderFloat("Initial Azimuth", rotInitAzimuth, -180f, 180f, "%.1f degrees")

        if (ImPlot3D.beginPlot("##BoxRotation")) {
            ImPlot3D.setupAxesLimits(-1.0, 1.0, -1.0, 1.0, -1.0, 1.0, ImPlot3DCond.ALWAYS)
            ImPlot3D.setupBoxInitialRotation(rotInitElevation[0].toDouble(), rotInitAzimuth[0].toDouble())
            val elevation = rotElevation[0].toDouble()
            val azimuth = rotAzimuth[0].toDouble()
            if (rotAnimate[0]) {
                ImPlot3D.setupBoxRotation(elevation, azimuth + ImGui.getTime() * 40.0, animate = true)
            } else if (changed) {
                ImPlot3D.setupBoxRotation(elevation, azimuth, cond = ImPlot3DCond.ALWAYS)
            }
            plotAxisLines()
            ImPlot3D.endPlot()
        }
    }

    private fun plotAxisLines() {
        val origin = DoubleArray(2)
        val xAxis = doubleArrayOf(1.0, 1.0)
        val yAxis = doubleArrayOf(1.0, 1.0)
        val zAxis = doubleArrayOf(1.0, 1.0)
        ImPlot3D.plotLine(
            "X-Axis",
            xAxis, origin, origin,
            ImPlot3DSpec(lineColor = ImVec4(0.8f, 0.2f, 0.2f, 1f)),
        )
        ImPlot3D.plotLine(
            "Y-Axis",
            origin, yAxis, origin,
            ImPlot3DSpec(lineColor = ImVec4(0.2f, 0.8f, 0.2f, 1f)),
        )
        ImPlot3D.plotLine(
            "Z-Axis",
            origin, origin, zAxis,
            ImPlot3DSpec(lineColor = ImVec4(0.2f, 0.2f, 0.8f, 1f)),
        )
    }

    // ===============================================================
    // Tools / Markers and Text
    // ===============================================================
    private fun demoMarkersAndText() {
        ImGui.dragFloat("Marker Size", mkSize, 0.1f, 2f, 10f, "%.2f px")
        ImGui.dragFloat("Marker Weight", mkWeight, 0.05f, 0.5f, 3f, "%.2f px")

        if (ImPlot3D.beginPlot("##MarkerStyles", ImVec2(-1f, 0f), ImPlot3DFlags.CANVAS_ONLY)) {
            ImPlot3D.setupAxes(null, null, null, ImPlot3DAxisFlags.NO_DECORATIONS, ImPlot3DAxisFlags.NO_DECORATIONS, ImPlot3DAxisFlags.NO_DECORATIONS)
            ImPlot3D.setupAxesLimits(-0.5, 1.5, -0.5, 1.5, 0.0, IMPLOT3D_MARKER_COUNT + 1.0)

            // Filled markers arranged in a ring
            var xs1 = 0.0
            var ys1 = 0.0
            var zs1 = IMPLOT3D_MARKER_COUNT.toDouble()
            for (m in 0 until IMPLOT3D_MARKER_COUNT) {
                val x1 = xs1 + cos(zs1 / IMPLOT3D_MARKER_COUNT * 2 * PI) * 0.5
                val y1 = ys1 + sin(zs1 / IMPLOT3D_MARKER_COUNT * 2 * PI) * 0.5
                ImPlot3D.plotLine(
                    "##Filled",
                    doubleArrayOf(xs1, x1), doubleArrayOf(ys1, y1), doubleArrayOf(zs1, zs1 - 1),
                    ImPlot3DSpec(marker = m, markerSize = mkSize[0], lineWeight = mkWeight[0]),
                )
                zs1 -= 1
            }

            // Open markers
            xs1 = 1.0
            ys1 = 1.0
            zs1 = IMPLOT3D_MARKER_COUNT.toDouble()
            for (m in 0 until IMPLOT3D_MARKER_COUNT) {
                val x1 = xs1 + cos(zs1 / IMPLOT3D_MARKER_COUNT * 2 * PI) * 0.5
                val y1 = ys1 - sin(zs1 / IMPLOT3D_MARKER_COUNT * 2 * PI) * 0.5
                ImPlot3D.plotLine(
                    "##Open",
                    doubleArrayOf(xs1, x1), doubleArrayOf(ys1, y1), doubleArrayOf(zs1, zs1 - 1),
                    ImPlot3DSpec(
                        marker = m,
                        markerSize = mkSize[0],
                        lineWeight = mkWeight[0],
                        fillColor = ImVec4(0f, 0f, 0f, 0f),
                    ),
                )
                zs1 -= 1
            }

            ImPlot3D.plotText("Filled Markers", 0.0, 0.0, 6.0)
            ImPlot3D.plotText("Open Markers", 1.0, 1.0, 6.0)
            ImPlot3D.pushStyleColor(ImPlot3DCol.INLAY_TEXT, ImVec4(1f, 0f, 1f, 1f))
            ImPlot3D.plotText("Rotated Text", 0.5, 0.5, 6.0, PI / 4)
            ImPlot3D.popStyleColor()
            ImPlot3D.endPlot()
        }
    }

    fun close() {
        context.close()
    }
}

private object ImPlot3DSurfaceFlags {
    const val NO_LINES = 1 shl 10
    const val NO_FILL = 1 shl 11
    const val NO_MARKERS = 1 shl 12
}

private const val IMPLOT3D_MARKER_COUNT = 10

/** Converts a [FloatArray] to [DoubleArray] (common code has no stdlib helper). */
private fun FloatArray.toDoubles(): DoubleArray = DoubleArray(size) { this[it].toDouble() }
