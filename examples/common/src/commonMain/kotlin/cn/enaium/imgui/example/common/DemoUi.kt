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

package cn.enaium.imgui.example.common

import cn.enaium.imgui.ImGui
import cn.enaium.imgui.ImGuiChildFlags
import cn.enaium.imgui.ImGuiCol
import cn.enaium.imgui.ImGuiComboFlags
import cn.enaium.imgui.ImGuiCond
import cn.enaium.imgui.ImGuiInputTextFlags
import cn.enaium.imgui.ImGuiMouseButton
import cn.enaium.imgui.ImGuiSelectableFlags
import cn.enaium.imgui.ImGuiTableFlags
import cn.enaium.imgui.ImGuiWindowFlags
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import cn.enaium.imgui.extensions.implot.ImPlot
import cn.enaium.imgui.extensions.implot.ImPlotAxis
import cn.enaium.imgui.extensions.implot.ImPlotColormap
import cn.enaium.imgui.extensions.implot.ImPlotCond
import cn.enaium.imgui.extensions.implot.ImPlotFlags
import cn.enaium.imgui.extensions.implot.ImPlotLocation
import cn.enaium.imgui.extensions.implot.ImPlotMarker
import cn.enaium.imgui.extensions.implot.ImPlotSpec
import kotlin.math.sin

/**
 * The demo UI shared by the renderer and GPU examples: a menu bar, a widget
 * playground window, an ImPlot dashboard and the built-in demo windows.
 */
class DemoUi {

    private val sliderValue = FloatArray(1) { 0.5f }
    private val intValue = IntArray(1) { 42 }
    private val checked = BooleanArray(1) { true }
    private val showDemo = BooleanArray(1) { false }
    private val showImPlotDemo = BooleanArray(1) { false }
    private val comboIndex = IntArray(1) { 1 }
    private val comboItems = arrayOf("Apple", "Banana", "Cherry", "Durian", "Elderberry")
    private var textInput = "imgui-kmp"
    private val progress = FloatArray(1) { 0.0f }
    private val clearColor = floatArrayOf(0.07f, 0.07f, 0.09f, 1f)

    // New binding coverage state
    private val dragF2 = FloatArray(2) { 0.5f }
    private val dragF3 = FloatArray(3) { 1f }
    private val dragI2 = IntArray(2) { 5 }
    private val dragRangeMin = FloatArray(1) { 0.25f }
    private val dragRangeMax = FloatArray(1) { 0.75f }
    private val inputF = FloatArray(1) { 0.5f }
    private val inputI = IntArray(1) { 10 }
    private val inputD = DoubleArray(1) { 3.14159 }
    private val sliderF2 = FloatArray(2) { 0.3f }
    private val sliderI2 = IntArray(2) { 3 }
    private val colorEdit = floatArrayOf(0.4f, 0.7f, 0.9f, 1f)
    private val colorPicker = floatArrayOf(0.2f, 0.6f, 0.4f)
    private val angleRad = FloatArray(1) { 0f }
    private val listBoxIndex = IntArray(1) { 0 }
    private val listBoxItems = arrayOf("One", "Two", "Three", "Four")
    private val checkFlags = IntArray(1) { 0 }
    private val vSliderH = FloatArray(1) { 0.6f }

    private val lineXs = FloatArray(256) { it.toFloat() }
    private val lineYs = FloatArray(256) { 0f }
    private val scatterYs = FloatArray(64) { 0f }
    private val bars = FloatArray(12) { (it % 5) + 1f }
    private val histogramValues = FloatArray(512) { (kotlin.random.Random.nextInt(100)).toFloat() }
    private val stemsY = FloatArray(32) { 0f }
    private val errorNeg = FloatArray(32) { 0f }
    private val errorPos = FloatArray(32) { 0f }

    fun draw(frame: Int) {
        // Fill the animated plot data once per frame.
        for (i in lineYs.indices) {
            val t = i / 255.0
            lineYs[i] = (sin(2.0 * kotlin.math.PI * (2.0 * t + frame / 240.0)) * 0.5 + 0.5).toFloat()
        }
        for (i in scatterYs.indices) {
            val t = i / 63.0
            scatterYs[i] = (sin(2.0 * kotlin.math.PI * (3.0 * t + frame / 180.0)) * 0.45 + 0.5).toFloat()
        }
        progress[0] = (frame % 240) / 240f

        for (i in stemsY.indices) {
            stemsY[i] = (sin(2.0 * kotlin.math.PI * (i / 31.0 + frame / 200.0)) * 0.5 + 0.5).toFloat()
            errorNeg[i] = 0.05f
            errorPos[i] = 0.05f
        }

        drawMenuBar()
        drawWidgetsWindow()
        drawPlotWindow()
        drawLogWindow()

        if (showDemo[0]) ImGui.showDemoWindow(showDemo)
        if (showImPlotDemo[0]) ImPlot.showDemoWindow(showImPlotDemo)
    }

    /** Exposes the clear color edited from the UI. */
    val backgroundColor: ImVec4
        get() = ImVec4(clearColor[0], clearColor[1], clearColor[2], clearColor[3])

    private fun drawMenuBar() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("Quit", "ESC")) {
                    // handled by the run loop's Quit event only; ignored here
                }
                ImGui.endMenu()
            }
            if (ImGui.beginMenu("View")) {
                ImGui.checkbox("Show ImGui demo", showDemo)
                ImGui.checkbox("Show ImPlot demo", showImPlotDemo)
                ImGui.endMenu()
            }
            ImGui.endMainMenuBar()
        }
    }

    private fun drawWidgetsWindow() {
        if (ImGui.begin(
                "Widgets",
                flags = ImGuiWindowFlags.NO_SAVED_SETTINGS,
            )
        ) {
            ImGui.text("Hello, imgui-kmp ${ImGui.getVersion()}")
            ImGui.text("FPS: 60.0")
            ImGui.separator()

            ImGui.sliderFloat("float", sliderValue, 0f, 1f, "%.3f")
            ImGui.sliderInt("int", intValue, 0, 100)
            ImGui.checkbox("checked", checked)

            ImGui.separatorText("Drag")
            ImGui.dragFloat("drag float", dragF2, 0.01f)
            ImGui.dragFloat2("drag float2", dragF2, 0.01f)
            ImGui.dragFloat3("drag float3", dragF3, 0.01f)
            ImGui.dragInt2("drag int2", dragI2, 1f)
            ImGui.dragFloatRange2("drag range", dragRangeMin, dragRangeMax, 0.01f, 0f, 1f)

            ImGui.separatorText("Input")
            ImGui.inputFloat("input float", inputF)
            ImGui.inputInt("input int", inputI)
            ImGui.inputDouble("input double", inputD)
            val multiLine = ImGui.inputTextMultiline("multiline", textInput)
            if (multiLine != null) textInput = multiLine

            ImGui.separatorText("Slider")
            ImGui.sliderFloat2("slider float2", sliderF2, 0f, 1f)
            ImGui.sliderInt2("slider int2", sliderI2, 0, 10)
            ImGui.sliderAngle("angle", angleRad)
            ImGui.vSliderFloat("vslider", ImVec2(18f, 60f), vSliderH, 0f, 1f)

            ImGui.separatorText("Color")
            ImGui.colorEdit4("color edit4", colorEdit)
            ImGui.colorEdit3("color edit3", colorPicker)
            ImGui.colorPicker3("color picker", colorPicker)

            ImGui.separatorText("List")
            ImGui.listBox("list", listBoxIndex, listBoxItems)
            ImGui.checkboxFlags("flags bit0", checkFlags, 1 shl 0)

            ImGui.separatorText("Query")
            if (ImGui.isItemHovered()) ImGui.text("(item hovered)")
            ImGui.text("frame: ${ImGui.getFrameCount()}, time: ${ImGui.getTime()}")
            ImGui.text("mouse: ${ImGui.getMousePos().x}, ${ImGui.getMousePos().y}")

            ImGui.separatorText("Collapsing header")
            ImGui.combo("fruit", comboIndex, comboItems)
            if (ImGui.button("randomize", ImVec2(0f, 0f))) {
                comboIndex[0] = kotlin.random.Random.nextInt(comboItems.size)
            }
            ImGui.sameLine()
            if (ImGui.button("reset")) {
                sliderValue[0] = 0.5f
                intValue[0] = 42
                checked[0] = true
                comboIndex[0] = 1
                textInput = "imgui-kmp"
            }

            val inputResult = ImGui.inputText("text", textInput, ImGuiInputTextFlags.NONE)
            if (inputResult != null) {
                textInput = inputResult
            }

            ImGui.progressBar(progress[0], ImVec2(-1f, 0f), formatPercent(progress[0]))

            ImGui.separatorText("Tabs")
            if (ImGui.beginTabBar("tabs")) {
                if (ImGui.beginTabItem("Tab A")) {
                    ImGui.text("Content of tab A")
                    ImGui.endTabItem()
                }
                if (ImGui.beginTabItem("Tab B")) {
                    ImGui.text("Content of tab B")
                    ImGui.endTabItem()
                }
                ImGui.endTabBar()
            }

            ImGui.separatorText("Table")
            if (ImGui.beginTable(
                    "table",
                    3,
                    ImGuiTableFlags.BORDERS or ImGuiTableFlags.ROW_BG,
                )
            ) {
                ImGui.tableSetupColumn("Name")
                ImGui.tableSetupColumn("Value")
                ImGui.tableSetupColumn("Notes")
                ImGui.tableHeadersRow()
                for (row in 0 until 3) {
                    ImGui.tableNextRow()
                    ImGui.tableNextColumn()
                    ImGui.text("row $row")
                    ImGui.tableNextColumn()
                    ImGui.text("${sliderValue[0] + row}")
                    ImGui.tableNextColumn()
                    ImGui.text("note $row")
                }
                ImGui.endTable()
            }

            ImGui.separatorText("Collapsing header")
            if (ImGui.collapsingHeader("Details")) {
                ImGui.text("Hidden details revealed")
            }

            ImGui.separatorText("Style")
            ImGui.pushStyleColor(ImGuiCol.BUTTON, ImVec4(0.4f, 0.2f, 0.6f, 1f))
            ImGui.button("colored button", ImVec2(0f, 0f))
            ImGui.popStyleColor()

            ImGui.separatorText("Popup")
            if (ImGui.button("open popup", ImVec2(0f, 0f))) {
                ImGui.openPopup("my popup")
            }
            if (ImGui.beginPopup("my popup")) {
                ImGui.text("popup content")
                ImGui.separator()
                if (ImGui.menuItem("close")) {
                    ImGui.closeCurrentPopup()
                }
                ImGui.endPopup()
            }
        }
        ImGui.end()
    }

    private fun drawPlotWindow() {
        if (ImGui.begin("Plots", flags = ImGuiWindowFlags.NO_SAVED_SETTINGS)) {
            if (ImPlot.beginPlot(
                    "Signals",
                    ImVec2(-1f, 260f),
                    ImPlotFlags.CROSSHAIRS,
                )
            ) {
                ImPlot.setupAxes("time", "amplitude")
                ImPlot.setupAxesLimits(0.0, 256.0, 0.0, 1.0, ImPlotCond.ONCE)
                ImPlot.setupLegend(ImPlotLocation.NORTH_EAST)
                ImPlot.setupFinish()
                ImPlot.plotLine("sin", lineXs, lineYs, ImPlotSpec(lineWeight = 2f))
                ImPlot.plotScatter(
                    "scatter",
                    lineXs.copyOfRange(0, 64),
                    scatterYs,
                    ImPlotSpec(marker = ImPlotMarker.CIRCLE, markerSize = 4f),
                )
                ImPlot.endPlot()
            }

            ImPlot.pushColormap(ImPlotColormap.PLASMA)
            if (ImPlot.beginPlot("Bars", ImVec2(-1f, 200f))) {
                ImPlot.setupAxes("bucket", "count")
                ImPlot.setupFinish()
                ImPlot.plotBars("bars", bars, barSize = 0.6)
                ImPlot.endPlot()
            }
            ImPlot.popColormap()

            if (ImPlot.beginPlot("Histogram", ImVec2(-1f, 200f))) {
                ImPlot.setupAxes(null, "count")
                ImPlot.setupFinish()
                ImPlot.plotHistogram("distribution", histogramValues)
                ImPlot.endPlot()
            }

            if (ImPlot.beginPlot("Stems", ImVec2(-1f, 180f))) {
                ImPlot.setupAxes("index", "value")
                ImPlot.setupFinish()
                ImPlot.plotStems("stems", lineXs.copyOfRange(0, 32), stemsY, 0.0)
                ImPlot.endPlot()
            }

            if (ImPlot.beginPlot("Error bars", ImVec2(-1f, 180f))) {
                ImPlot.setupAxes("index", "value")
                ImPlot.setupFinish()
                ImPlot.plotErrorBars("err", lineXs.copyOfRange(0, 32), scatterYs, errorNeg, errorPos)
                ImPlot.endPlot()
            }

            if (ImPlot.beginPlot("Bar groups", ImVec2(-1f, 180f))) {
                ImPlot.setupAxes(null, null)
                ImPlot.setupFinish()
                ImPlot.plotBarGroups(arrayOf("A", "B", "C"), bars.copyOfRange(0, 9), 3, 3)
                ImPlot.endPlot()
            }

            if (ImPlot.beginPlot("Heatmap", ImVec2(-1f, 200f))) {
                ImPlot.setupFinish()
                ImPlot.plotHeatmap("heat", histogramValues, 16, 32, 0.0, 100.0)
                ImPlot.endPlot()
            }

            if (ImPlot.beginPlot("Polygon", ImVec2(-1f, 160f))) {
                ImPlot.setupAxes("x", "y")
                ImPlot.setupFinish()
                ImPlot.plotPolygon("poly", lineXs.copyOfRange(0, 12), scatterYs.copyOfRange(0, 12))
                ImPlot.endPlot()
            }
        }
        ImGui.end()
    }

    private fun drawLogWindow() {
        if (ImGui.begin(
                "Log",
                flags = ImGuiWindowFlags.NO_SAVED_SETTINGS,
            )
        ) {
            ImGui.textColored(ImVec4(0.5f, 0.9f, 0.5f, 1f), "info: imgui-kmp up and running")
            ImGui.textColored(ImVec4(0.9f, 0.8f, 0.3f, 1f), "warn: nothing to warn about")
            ImGui.textColored(ImVec4(0.9f, 0.4f, 0.4f, 1f), "error: just kidding")
        }
        ImGui.end()
    }

    private fun formatPercent(value: Float): String {
        val percent = (value * 100).toInt()
        return "$percent%"
    }

    fun close() {
        // nothing to release
    }
}
