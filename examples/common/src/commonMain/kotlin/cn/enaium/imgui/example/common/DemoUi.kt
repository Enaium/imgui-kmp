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
import cn.enaium.imgui.ImGuiColorEditFlags
import cn.enaium.imgui.ImGuiComboFlags
import cn.enaium.imgui.ImGuiCond
import cn.enaium.imgui.ImGuiDataType
import cn.enaium.imgui.ImGuiDir
import cn.enaium.imgui.ImGuiDragDropFlags
import cn.enaium.imgui.ImGuiHoveredFlags
import cn.enaium.imgui.ImGuiInputTextFlags
import cn.enaium.imgui.ImGuiItemFlags
import cn.enaium.imgui.ImGuiKey
import cn.enaium.imgui.ImGuiMouseButton
import cn.enaium.imgui.ImGuiPopupFlags
import cn.enaium.imgui.ImGuiSelectableFlags
import cn.enaium.imgui.ImGuiSliderFlags
import cn.enaium.imgui.ImGuiStyleVar
import cn.enaium.imgui.ImGuiTabItemFlags
import cn.enaium.imgui.ImGuiTableBgTarget
import cn.enaium.imgui.ImGuiTableColumnFlags
import cn.enaium.imgui.ImGuiTableFlags
import cn.enaium.imgui.ImGuiTableRowFlags
import cn.enaium.imgui.ImGuiTreeNodeFlags
import cn.enaium.imgui.ImGuiWindowFlags
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import cn.enaium.imgui.extensions.implot.ImPlot
import cn.enaium.imgui.extensions.implot.ImPlotAxis
import cn.enaium.imgui.extensions.implot.ImPlotAxisFlags
import cn.enaium.imgui.extensions.implot.ImPlotBin
import cn.enaium.imgui.extensions.implot.ImPlotCol
import cn.enaium.imgui.extensions.implot.ImPlotColormap
import cn.enaium.imgui.extensions.implot.ImPlotCond
import cn.enaium.imgui.extensions.implot.ImPlotDragToolFlags
import cn.enaium.imgui.extensions.implot.ImPlotFlags
import cn.enaium.imgui.extensions.implot.ImPlotLocation
import cn.enaium.imgui.extensions.implot.ImPlotMarker
import cn.enaium.imgui.extensions.implot.ImPlotScale
import cn.enaium.imgui.extensions.implot.ImPlotSpec
import cn.enaium.imgui.extensions.implot.ImPlotStyleVar
import cn.enaium.imgui.extensions.implot.ImPlotSubplotFlags
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.random.Random

/**
 * A widget showcase covering the bound ImGui and ImPlot APIs: a menu bar, a
 * sidebar-driven ImGui demo window, an ImPlot demo window and the built-in
 * demo/metrics/style windows reachable from the menu.
 */
class DemoUi {

    // =====================================================================
    // Shared state
    // =====================================================================

    /** The font atlas texture id, wired up by the app after upload. Used by the image demos. */
    var fontTextureId: Long = 0

    /** Auto-advances through every demo section (handy for CI/headless runs). */
    var autoCycle = false

    private val showImGuiDemo = BooleanArray(1)
    private val showImPlotDemo = BooleanArray(1)
    private val showMetrics = BooleanArray(1)
    private val showAbout = BooleanArray(1)
    private val showDebugLog = BooleanArray(1)
    private val showImPlotMetrics = BooleanArray(1)

    private val clearColor = floatArrayOf(0.07f, 0.07f, 0.09f, 1f)

    // =====================================================================
    // ImGui demo state
    // =====================================================================

    private var imguiSection = 0
    private var plotSection = 0

    private val checked = BooleanArray(1) { true }
    private val comboIndex = IntArray(1) { 1 }
    private val comboItems = arrayOf("Apple", "Banana", "Cherry", "Durian", "Elderberry")
    private val listBoxIndex = IntArray(1)
    private val listBoxItems = arrayOf("One", "Two", "Three", "Four", "Five")
    private val radioValue = IntArray(1) { 1 }
    private val checkFlags = IntArray(1) { 0 }
    private val querySelected = IntArray(1)

    private val sliderF = FloatArray(1) { 0.5f }
    private val sliderF2 = FloatArray(2) { 0.3f }
    private val sliderF3 = FloatArray(3) { 0.2f }
    private val sliderF4 = FloatArray(4) { 0.1f }
    private val sliderI = IntArray(1) { 50 }
    private val sliderI2 = IntArray(2) { 3 }
    private val sliderI3 = IntArray(3) { 4 }
    private val sliderI4 = IntArray(4) { 1 }
    private val angleRad = FloatArray(1)
    private val vSliderF = FloatArray(1) { 0.6f }
    private val vSliderI = IntArray(1) { 5 }
    private val sliderS64 = LongArray(1) { 42 }
    private val dragF = FloatArray(1) { 0.5f }
    private val dragF2 = FloatArray(2) { 0.5f }
    private val dragF3 = FloatArray(3) { 1f }
    private val dragF4 = FloatArray(4) { 1f }
    private val dragI = IntArray(1) { 42 }
    private val dragI2 = IntArray(2) { 42 }
    private val dragI3 = IntArray(3) { 42 }
    private val dragI4 = IntArray(4) { 42 }
    private val dragRangeMin = FloatArray(1) { 0.25f }
    private val dragRangeMax = FloatArray(1) { 0.75f }
    private val dragIntRangeMin = IntArray(1) { 5 }
    private val dragIntRangeMax = IntArray(1) { 100 }
    private val dragS64 = LongArray(1) { 42 }

    private val inputF = FloatArray(1) { 0.5f }
    private val inputF2 = FloatArray(2) { 0.5f }
    private val inputF3 = FloatArray(3) { 0.5f }
    private val inputF4 = FloatArray(4) { 0.5f }
    private val inputI = IntArray(1) { 42 }
    private val inputI2 = IntArray(2) { 42 }
    private val inputI3 = IntArray(3) { 42 }
    private val inputI4 = IntArray(4) { 42 }
    private val inputD = DoubleArray(1) { 3.14159 }
    private var inputTextBuf = "Edit me..."
    private var inputMultilineBuf = "Hello, world!\nThis is a multiline input.\n"
    private var inputHintBuf = "hint text"
    private var inputPasswordBuf = "secret"
    private val inputReadOnlyBuf = BooleanArray(1)

    private val colorEdit4 = floatArrayOf(0.4f, 0.7f, 0.9f, 1f)
    private val colorEdit3 = floatArrayOf(0.4f, 0.7f, 0.9f)
    private val colorPicker4 = floatArrayOf(0.2f, 0.6f, 0.4f, 1f)
    private val colorPicker3 = floatArrayOf(0.2f, 0.6f, 0.4f)
    private val rgbIn = FloatArray(3) { 0.4f }
    private val hsvIn = FloatArray(3) { 0.5f }
    private val rgbOutR = FloatArray(1)
    private val rgbOutG = FloatArray(1)
    private val rgbOutB = FloatArray(1)
    private val hsvOutH = FloatArray(1)
    private val hsvOutS = FloatArray(1)
    private val hsvOutV = FloatArray(1)

    private val treeSelect = IntArray(1)

    private val childFixed = FloatArray(1) { 120f }
    private val childAuto = FloatArray(1) { 120f }

    private val modalOpen = BooleanArray(1) { true }
    private val tabClose = BooleanArray(1) { true }

    private val dropCount = IntArray(1)
    private var dropPayload = ""

    private val progress = FloatArray(1)

    private val mouseBtns = BooleanArray(3)
    private val cursorIdx = IntArray(1)
    private val keyIdx = IntArray(1) { 2 }
    private val keyNames = arrayOf("A", "B", "C", "D", "Up Arrow", "Enter", "Escape")

    private val scrollX = FloatArray(1)
    private val scrollY = FloatArray(1)
    private val scrollTrack = BooleanArray(1) { true }

    private val plotStyleCheck = BooleanArray(1) { true }

    // =====================================================================
    // ImPlot demo state
    // =====================================================================

    private val rng = Random(42)
    private val xData = FloatArray(100) { it / 99.0f }
    private val sinData = FloatArray(100) { (sin(it / 99.0 * 2.0 * PI) * 0.5 + 0.5).toFloat() }
    private val cosData = FloatArray(100) { (cos(it / 99.0 * 2.0 * PI) * 0.5 + 0.5).toFloat() }
    private val phaseData = FloatArray(100) { (sin(it / 99.0 * 2.0 * PI + PI / 4.0) * 0.5 + 0.5).toFloat() }
    private val scatterXs = FloatArray(100) { rng.nextFloat() }
    private val scatterYs = FloatArray(100) { rng.nextFloat() }
    private val bubbleXs = FloatArray(100) { rng.nextFloat() }
    private val bubbleYs = FloatArray(100) { rng.nextFloat() }
    private val bubbleSizes = FloatArray(100) { rng.nextFloat() * 0.02f + 0.005f }
    private val histogramData = FloatArray(1000) { rng.nextFloat() * 100f }
    private val histogram2DXs = FloatArray(1000) { gaussian() }
    private val histogram2DYs = FloatArray(1000) { gaussian() }
    private val heatmapData = FloatArray(30 * 30) { rng.nextFloat() }
    private val digitalData = FloatArray(100) { if ((it / 5) % 2 == 0) 1f else 0f }
    private val errorNeg = FloatArray(10) { 0.1f }
    private val errorPos = FloatArray(10) { 0.15f }
    private val errorXs = FloatArray(10) { it * 0.1f }
    private val errorYs = FloatArray(10) { (sin(it * 0.6) * 0.3 + 0.5).toFloat() }
    private val stairXs = FloatArray(10) { it * 0.1f }
    private val stairYs = FloatArray(10) { (cos(it * 0.7) * 0.4 + 0.5).toFloat() }
    private val pieLabels = arrayOf("Frogs", "Hogs", "Dogs", "Logs")
    private val pieValues = floatArrayOf(0.3f, 0.1f, 0.2f, 0.4f)
    private val barGroupValues = floatArrayOf(0.4f, 0.7f, 0.2f, 0.8f, 0.5f, 0.9f, 0.3f, 0.6f)
    private val barGroupLabels = arrayOf("A", "B", "C", "D")
    private val barXs = FloatArray(10) { it * 0.1f }
    private val barYs = FloatArray(10) { (sin(it * 0.8) * 0.3 + 0.5).toFloat() }
    private val logXs = FloatArray(100) { it / 99.0f * 100.0f }
    private val logYs = FloatArray(100) { exp(it / 99.0f * 4.0f) }
    private val timeYs = FloatArray(24) { (sin(it / 24.0 * 2.0 * PI) * 10.0 + 50.0).toFloat() }
    private val multiXs = FloatArray(1001) { it / 1000.0f * 100.0f }
    private val multiYs = FloatArray(1001) { (sin(it / 1000.0 * 2.0 * PI) * 3.0 + 1.0).toFloat() }
    private val multiXs2 = FloatArray(1001) { it / 1000.0f * 100.0f }
    private val multiYs2 = FloatArray(1001) { (cos(it / 1000.0 * 2.0 * PI) * 0.2 + 0.5).toFloat() }
    private val multiYs3 = FloatArray(1001) { (sin(it / 1000.0 * 2.0 * PI + 0.5) * 100.0 + 200.0).toFloat() }

    private val x2Axis = BooleanArray(1) { true }
    private val y2Axis = BooleanArray(1) { true }
    private val y3Axis = BooleanArray(1) { true }

    private val dragPointX = doubleArrayOf(0.5)
    private val dragPointY = doubleArrayOf(0.5)
    private val dragLineXVal = doubleArrayOf(0.2)
    private val dragLineYVal = doubleArrayOf(0.8)
    private val dragRectXMin = doubleArrayOf(0.2)
    private val dragRectYMin = doubleArrayOf(0.3)
    private val dragRectXMax = doubleArrayOf(0.7)
    private val dragRectYMax = doubleArrayOf(0.8)

    private val colormapT = FloatArray(1) { 0.5f }
    private val colormapOut = FloatArray(4)

    private val nowEpoch = 1_700_000_000.0

    // =====================================================================
    // Frame entry point
    // =====================================================================

    fun draw(frame: Int) {
        progress[0] = (frame % 240) / 240f

        // Headless/CI helper: cycle through every demo section.
        if (autoCycle && frame > 0 && frame % 3 == 0) {
            imguiSection = (imguiSection + 1) % 19
            plotSection = (plotSection + 1) % 16
        }

        drawMenuBar()
        drawDemoWindow()
        drawPlotDemoWindow()

        if (showImGuiDemo[0]) ImGui.showDemoWindow(showImGuiDemo)
        if (showImPlotDemo[0]) ImPlot.showDemoWindow(showImPlotDemo)
        if (showMetrics[0]) ImGui.showMetricsWindow(showMetrics)
        if (showAbout[0]) ImGui.showAboutWindow(showAbout)
        if (showDebugLog[0]) ImGui.showDebugLogWindow(showDebugLog)
        if (showImPlotMetrics[0]) ImPlot.showMetricsWindow(showImPlotMetrics)
    }

    /** Exposes the clear color edited from the UI. */
    val backgroundColor: ImVec4
        get() = ImVec4(clearColor[0], clearColor[1], clearColor[2], clearColor[3])

    fun close() {
        // nothing to release
    }

    // =====================================================================
    // Menu bar
    // =====================================================================

    private fun drawMenuBar() {
        if (ImGui.beginMainMenuBar()) {
            if (ImGui.beginMenu("File")) {
                ImGui.menuItem("(demo menu)", enabled = false)
                ImGui.separator()
                if (ImGui.menuItem("Quit", "ESC")) {
                    // handled by the run loop's Quit event only; ignored here
                }
                ImGui.endMenu()
            }
            if (ImGui.beginMenu("Edit")) {
                ImGui.menuItem("Undo", "CTRL+Z", enabled = false)
                ImGui.menuItem("Redo", "CTRL+Y", enabled = false)
                ImGui.separator()
                ImGui.menuItem("Cut", "CTRL+X", enabled = false)
                ImGui.menuItem("Copy", "CTRL+C", enabled = false)
                ImGui.menuItem("Paste", "CTRL+V", enabled = false)
                ImGui.endMenu()
            }
            if (ImGui.beginMenu("View")) {
                ImGui.menuItem("ImGui demo", selected = showImGuiDemo[0])
                if (ImGui.isItemClicked(ImGuiMouseButton.LEFT)) showImGuiDemo[0] = !showImGuiDemo[0]
                ImGui.menuItem("ImPlot demo", selected = showImPlotDemo[0])
                if (ImGui.isItemClicked(ImGuiMouseButton.LEFT)) showImPlotDemo[0] = !showImPlotDemo[0]
                ImGui.separator()
                ImGui.menuItem("Metrics", selected = showMetrics[0])
                if (ImGui.isItemClicked(ImGuiMouseButton.LEFT)) showMetrics[0] = !showMetrics[0]
                ImGui.menuItem("Debug log", selected = showDebugLog[0])
                if (ImGui.isItemClicked(ImGuiMouseButton.LEFT)) showDebugLog[0] = !showDebugLog[0]
                ImGui.menuItem("About", selected = showAbout[0])
                if (ImGui.isItemClicked(ImGuiMouseButton.LEFT)) showAbout[0] = !showAbout[0]
                ImGui.endMenu()
            }
            if (ImGui.beginMenu("Help")) {
                if (ImGui.menuItem("User guide")) ImGui.showUserGuide()
                if (ImGui.menuItem("ImPlot metrics")) showImPlotMetrics[0] = !showImPlotMetrics[0]
                ImGui.separator()
                ImGui.menuItem("Repository: https://github.com/Enaium/imgui-kmp", enabled = false)
                ImGui.endMenu()
            }
            ImGui.endMainMenuBar()
        }
    }

    // =====================================================================
    // ImGui demo window
    // =====================================================================

    private fun drawDemoWindow() {
        // Fixed initial size: with an auto-sized window the height-0 child
        // windows below collapse (and the dynamic font lags on frame 1),
        // leaving the UI squeezed into the top-left corner.
        ImGui.setNextWindowSize(ImVec2(700f, 560f), ImGuiCond.FIRST_USE_EVER)
        if (!ImGui.begin("imgui-kmp Demo")) {
            ImGui.end()
            return
        }
        ImGui.text("Version ${ImGui.getVersion()}, frame ${ImGui.getFrameCount()}, ${fmt((ImGui.getTime() * 1000.0) % 1000.0, 2)} ms")
        ImGui.textColored(ImVec4(0.5f, 0.9f, 0.5f, 1f), "Every widget below is driven through the Kotlin bindings.")
        ImGui.separator()

        if (ImGui.beginChild("demo_sidebar", ImVec2(190f, 0f), ImGuiChildFlags.BORDERS)) {
            val sections = arrayOf(
                "About",
                "Widgets/Basic",
                "Widgets/Sliders & Drags",
                "Widgets/Inputs",
                "Widgets/Colors",
                "Widgets/Text",
                "Widgets/Trees",
                "Widgets/Combos & Lists",
                "Widgets/Progress & Misc",
                "Layout",
                "Tables",
                "Tabs",
                "Popups & Modals",
                "Drag & Drop",
                "Query & Disabled",
                "Keyboard & Mouse",
                "Scrolling & Drawing",
                "Style",
                "Logging & I/O",
            )
            for (i in sections.indices) {
                if (ImGui.selectable(sections[i], imguiSection == i)) imguiSection = i
            }
        }
        ImGui.endChild()
        ImGui.sameLine()

        if (ImGui.beginChild("demo_content", ImVec2(0f, 0f), ImGuiChildFlags.BORDERS)) {
            when (imguiSection) {
                0 -> drawSectionAbout()
                1 -> drawSectionBasic()
                2 -> drawSectionSlidersDrags()
                3 -> drawSectionInputs()
                4 -> drawSectionColors()
                5 -> drawSectionText()
                6 -> drawSectionTrees()
                7 -> drawSectionCombosLists()
                8 -> drawSectionProgressMisc()
                9 -> drawSectionLayout()
                10 -> drawSectionTables()
                11 -> drawSectionTabs()
                12 -> drawSectionPopups()
                13 -> drawSectionDragDrop()
                14 -> drawSectionQuery()
                15 -> drawSectionKeyboardMouse()
                16 -> drawSectionScrollingDrawing()
                17 -> drawSectionStyle()
                18 -> drawSectionLoggingIo()
            }
        }
        ImGui.endChild()
        ImGui.end()
    }

    private fun drawSectionAbout() {
        ImGui.separatorText("About imgui-kmp")
        ImGui.textWrapped(
            "Kotlin Multiplatform bindings for Dear ImGui and ImPlot, running on top of SDL3 " +
                "via the sdl-kmp backends. This window reimplements the classic demo " +
                "screens with the bound API to exercise every widget.",
        )
        ImGui.textLinkOpenURL("Project repository", "https://github.com/Enaium/imgui-kmp")
        ImGui.separatorText("Bindings status")
        ImGui.bulletText("ImGui ${ImGui.getVersion()} core, windows, widgets, tables, popups, drag & drop")
        ImGui.bulletText("ImPlot plots, axes, subplots, drag tools, colormaps")
        ImGui.bulletText("ImGuiSdlBackend / ImGuiSdlRendererBackend / ImGuiSdlGpuBackend in cn.enaium.imgui.backends.sdl")
        ImGui.separatorText("Context")
        val io = ImGui.getIO()
        ImGui.labelText("Display size", "${io.displaySize.x} x ${io.displaySize.y}")
        ImGui.labelText("Frame count", "${ImGui.getFrameCount()}")
        ImGui.labelText("Delta time", "${fmt(io.deltaTime * 1000.0, 4)} ms")
        ImGui.labelText("Font size", "${fmt(ImGui.getFontSize().toDouble(), 1)} px")
        ImGui.labelText("Want capture mouse", "${io.wantCaptureMouse}")
        ImGui.labelText("Want capture keyboard", "${io.wantCaptureKeyboard}")
        ImGui.labelText("Want text input", "${io.wantTextInput}")
    }

    private fun drawSectionBasic() {
        ImGui.separatorText("Buttons")
        if (ImGui.button("Button")) ImGui.setTooltip("Clicked!")
        ImGui.sameLine()
        if (ImGui.smallButton("Small button")) {}
        ImGui.sameLine()
        if (ImGui.arrowButton("arrow_left", ImGuiDir.LEFT)) {}
        ImGui.sameLine()
        if (ImGui.arrowButton("arrow_right", ImGuiDir.RIGHT)) {}
        ImGui.sameLine()
        if (ImGui.invisibleButton("invisible", ImVec2(80f, 20f))) {}
        ImGui.sameLine()
        ImGui.text("invisible button")

        ImGui.separatorText("Toggles")
        ImGui.checkbox("Checkbox", checked)
        ImGui.checkboxFlags("CheckboxFlags: A", checkFlags, 1 shl 0)
        ImGui.sameLine()
        ImGui.checkboxFlags("B", checkFlags, 1 shl 1)
        ImGui.sameLine()
        ImGui.checkboxFlags("C", checkFlags, 1 shl 2)
        for (i in 0 until 4) {
            if (i > 0) ImGui.sameLine()
            if (ImGui.radioButton("Radio $i", radioValue[0] == i)) radioValue[0] = i
        }

        ImGui.separatorText("Selectables")
        ImGui.beginGroup()
        for (i in 0 until 3) {
            if (ImGui.selectable("Selectable $i", querySelected[0] == i)) {
                querySelected[0] = i
            }
        }
        ImGui.endGroup()
        ImGui.sameLine()
        ImGui.beginGroup()
        for (i in 3 until 6) {
            if (ImGui.selectable("Selectable $i", querySelected[0] == i)) {
                querySelected[0] = i
            }
        }
        ImGui.endGroup()
        ImGui.sameLine()
        ImGui.text("Selected index: ${querySelected[0]}")

        ImGui.separatorText("Combo & ListBox")
        ImGui.combo("combo", comboIndex, comboItems)
        ImGui.sameLine()
        if (ImGui.button("randomize")) {
            comboIndex[0] = Random.nextInt(comboItems.size)
        }
        ImGui.listBox("listbox", listBoxIndex, listBoxItems)
    }

    private fun drawSectionSlidersDrags() {
        ImGui.separatorText("Sliders")
        ImGui.sliderFloat("slider float", sliderF, 0f, 1f, "%.3f")
        ImGui.sliderFloat2("slider float2", sliderF2, 0f, 1f, "%.3f", ImGuiSliderFlags.LOGARITHMIC)
        ImGui.sliderFloat3("slider float3", sliderF3, 0f, 1f)
        ImGui.sliderFloat4("slider float4", sliderF4, 0f, 1f)
        ImGui.sliderInt("slider int", sliderI, 0, 100)
        ImGui.sliderInt2("slider int2", sliderI2, 0, 10)
        ImGui.sliderInt3("slider int3", sliderI3, 0, 10)
        ImGui.sliderInt4("slider int4", sliderI4, 0, 10)
        ImGui.sliderAngle("slider angle", angleRad, -360f, 360f)
        ImGui.sliderScalar("slider S64", ImGuiDataType.S64, sliderS64, longArrayOf(0), longArrayOf(100), "%lld")
        ImGui.vSliderFloat("vslider float", ImVec2(18f, 80f), vSliderF, 0f, 1f)
        ImGui.sameLine()
        ImGui.vSliderInt("vslider int", ImVec2(18f, 80f), vSliderI, 0, 10)

        ImGui.separatorText("Drags")
        ImGui.dragFloat("drag float", dragF, 0.01f, 0f, 1f)
        ImGui.dragFloat2("drag float2", dragF2, 0.01f, 0f, 1f)
        ImGui.dragFloat3("drag float3", dragF3, 0.01f, 0f, 2f)
        ImGui.dragFloat4("drag float4", dragF4, 0.01f, 0f, 2f)
        ImGui.dragInt("drag int", dragI, 1f, 0, 100)
        ImGui.dragInt2("drag int2", dragI2, 1f, 0, 100)
        ImGui.dragInt3("drag int3", dragI3, 1f, 0, 100)
        ImGui.dragInt4("drag int4", dragI4, 1f, 0, 100)
        ImGui.dragFloatRange2("drag range float", dragRangeMin, dragRangeMax, 0.01f, 0f, 1f)
        ImGui.dragIntRange2("drag range int", dragIntRangeMin, dragIntRangeMax, 1f, 0, 100)
        ImGui.dragScalar("drag S64", ImGuiDataType.S64, dragS64, 1f, longArrayOf(0), longArrayOf(100), "%lld")

        ImGui.separatorText("Variations")
        ImGui.text("SliderFlags.LOGARITHMIC and NO_INPUT on the same widget:")
        ImGui.dragFloat("wrap-around drag", dragF, 0.01f, 0f, 1f, "%.3f", ImGuiSliderFlags.WRAP_AROUND)
        ImGui.pushItemFlag(ImGuiItemFlags.DISABLED, true)
        ImGui.sliderFloat("disabled slider", sliderF, 0f, 1f)
        ImGui.popItemFlag()
    }

    private fun drawSectionInputs() {
        ImGui.separatorText("InputText")
        val input1 = ImGui.inputText("input text", inputTextBuf)
        if (input1 != null) inputTextBuf = input1
        val input2 = ImGui.inputTextWithHint(
            "input with hint",
            "type something",
            inputHintBuf,
            ImGuiInputTextFlags.ENTER_RETURNS_TRUE,
        )
        if (input2 != null) inputHintBuf = input2
        val input3 = ImGui.inputTextMultiline("input multiline", inputMultilineBuf)
        if (input3 != null) inputMultilineBuf = input3
        val input4 = ImGui.inputTextWithHint("password", "********", inputPasswordBuf, ImGuiInputTextFlags.CHARS_NO_BLANK)
        if (input4 != null) inputPasswordBuf = input4
        ImGui.text("password content: \"$inputPasswordBuf\"")
        ImGui.checkbox("read-only", inputReadOnlyBuf)
        val roFlags = if (inputReadOnlyBuf[0]) ImGuiInputTextFlags.READ_ONLY else ImGuiInputTextFlags.NONE
        val input5 = ImGui.inputText("read-only", inputTextBuf, roFlags)
        if (input5 != null) inputTextBuf = input5

        ImGui.separatorText("InputScalar")
        ImGui.inputFloat("input float", inputF, 0.1f, 1.0f, "%.3f")
        ImGui.inputFloat2("input float2", inputF2)
        ImGui.inputFloat3("input float3", inputF3)
        ImGui.inputFloat4("input float4", inputF4)
        ImGui.inputInt("input int", inputI, 1, 10)
        ImGui.inputInt2("input int2", inputI2)
        ImGui.inputInt3("input int3", inputI3)
        ImGui.inputInt4("input int4", inputI4)
        ImGui.inputDouble("input double", inputD, 0.01, 0.1, "%.6f")
    }

    private fun drawSectionColors() {
        ImGui.separatorText("ColorEdit")
        ImGui.colorEdit4("color edit4", colorEdit4)
        ImGui.colorEdit3("color edit3", colorEdit3)
        ImGui.colorEdit4("color edit4 no alpha", colorEdit4, ImGuiColorEditFlags.NO_ALPHA)
        ImGui.colorEdit3("color edit3 uint8", colorEdit3, ImGuiColorEditFlags.UINT8)
        ImGui.colorEdit4("color edit4 alpha bar", colorEdit4, ImGuiColorEditFlags.ALPHA_BAR)
        ImGui.colorEdit4("color edit4 hdr", colorEdit4, ImGuiColorEditFlags.HDR or ImGuiColorEditFlags.FLOAT)

        ImGui.separatorText("ColorPicker")
        ImGui.colorPicker4("color picker4", colorPicker4, ImGuiColorEditFlags.ALPHA_BAR)
        ImGui.sameLine()
        ImGui.colorPicker4("color picker4 (wheel)", colorPicker4, ImGuiColorEditFlags.PICKER_HUE_WHEEL)
        ImGui.colorPicker3("color picker3", colorPicker3)
        ImGui.colorButton("color button", ImVec4(colorPicker3[0], colorPicker3[1], colorPicker3[2], 1f))

        ImGui.separatorText("Color conversion")
        ImGui.inputFloat3("RGB in", rgbIn)
        ImGui.colorConvertRGBtoHSV(rgbIn[0], rgbIn[1], rgbIn[2], hsvOutH, hsvOutS, hsvOutV)
        ImGui.text("HSV: ${fmt(hsvOutH[0].toDouble(), 3)} ${fmt(hsvOutS[0].toDouble(), 3)} ${fmt(hsvOutV[0].toDouble(), 3)}")
        ImGui.inputFloat3("HSV in", hsvIn)
        ImGui.colorConvertHSVtoRGB(hsvIn[0], hsvIn[1], hsvIn[2], rgbOutR, rgbOutG, rgbOutB)
        ImGui.text("RGB: ${fmt(rgbOutR[0].toDouble(), 3)} ${fmt(rgbOutG[0].toDouble(), 3)} ${fmt(rgbOutB[0].toDouble(), 3)}")
        val u32 = ImGui.colorConvertFloat4ToU32(ImVec4(0.2f, 0.4f, 0.6f, 1f))
        val back = ImGui.colorConvertU32ToFloat4(u32)
        ImGui.text("u32 roundtrip: ${hex8(u32)} -> ${fmt(back.x.toDouble(), 2)},${fmt(back.y.toDouble(), 2)},${fmt(back.z.toDouble(), 2)}")
        ImGui.setColorEditOptions(ImGuiColorEditFlags.UINT8 or ImGuiColorEditFlags.DISPLAY_RGB)
        ImGui.text("setColorEditOptions applied (UINT8 + RGB display)")
    }

    private fun drawSectionText() {
        ImGui.separatorText("Text")
        ImGui.text("Hello, imgui-kmp ${ImGui.getVersion()}!")
        ImGui.textColored(ImVec4(0.4f, 0.8f, 1f, 1f), "textColored: cyan")
        ImGui.textDisabled("textDisabled")
        ImGui.textWrapped(
            "textWrapped: this is a very long text that will wrap around the window " +
                "content region automatically instead of extending forever.",
        )
        ImGui.textUnformatted("textUnformatted: no format parsing, e.g. \"%%d\" stays literal.")
        if (ImGui.textLink("textLink: clickable text")) {
            ImGui.setTooltip("Link clicked!")
        }
        ImGui.textLinkOpenURL("textLinkOpenURL: external link", "https://github.com/Enaium/imgui-kmp")
        ImGui.labelText("labelText", "value")
        ImGui.bulletText("bulletText")
        ImGui.bullet()

        ImGui.separatorText("Measurements")
        val size = ImGui.calcTextSize("measured text")
        ImGui.labelText("calcTextSize", "${fmt(size.x.toDouble(), 1)} x ${fmt(size.y.toDouble(), 1)}")
        ImGui.labelText("getTextLineHeight", "${fmt(ImGui.getTextLineHeight().toDouble(), 1)}")
        ImGui.labelText("getFrameHeight", "${fmt(ImGui.getFrameHeight().toDouble(), 1)}")
        ImGui.labelText("getStyleColorName", ImGui.getStyleColorName(ImGuiCol.BUTTON))
        ImGui.labelText("getColorU32", hex8(ImGui.getColorU32(ImGuiCol.BUTTON)))
        ImGui.labelText("getID", hex8(ImGui.getID("unique-id")))
        ImGui.labelText("calcItemWidth", "${fmt(ImGui.calcItemWidth().toDouble(), 1)}")

        ImGui.separatorText("Clipping & wrapping")
        ImGui.pushTextWrapPos(200f)
        ImGui.text("pushTextWrapPos(200f): this text is wrapped at 200 pixels.")
        ImGui.popTextWrapPos()
        ImGui.text("back to normal")
        ImGui.dummy(ImVec2(50f, 20f))
        ImGui.sameLine()
        ImGui.text("dummy(50, 20) on the same line")
    }

    private fun drawSectionTrees() {
        ImGui.separatorText("Tree nodes")
        for (i in 0 until 3) {
            if (ImGui.treeNode("Child $i")) {
                ImGui.text("leaf A")
                ImGui.treePush("sub")
                ImGui.text("leaf B inside push")
                ImGui.treePop()
                ImGui.treePop()
            }
        }

        ImGui.separatorText("TreeNodeEx")
        for (i in 0 until 3) {
            val flags = when (i) {
                1 -> ImGuiTreeNodeFlags.DEFAULT_OPEN or ImGuiTreeNodeFlags.FRAMED
                2 -> ImGuiTreeNodeFlags.SELECTED or ImGuiTreeNodeFlags.FRAMED
                else -> ImGuiTreeNodeFlags.NONE
            }
            if (ImGui.treeNodeEx("ex $i", flags)) {
                ImGui.text("content of ex $i")
                ImGui.treePop()
            }
        }

        ImGui.separatorText("CollapsingHeader")
        if (ImGui.collapsingHeader("collapsing header")) {
            ImGui.text("revealed!")
        }
        if (ImGui.collapsingHeader("collapsing header (framed)", ImGuiTreeNodeFlags.FRAMED)) {
            ImGui.text("also revealed")
        }

        ImGui.separatorText("Selection tree")
        ImGui.pushId("tree-select")
        if (ImGui.treeNode("Selectable leaves")) {
            for (i in 0 until 4) {
                if (ImGui.treeNode("node $i")) {
                    if (ImGui.selectable("select me", treeSelect[0] == i)) treeSelect[0] = i
                    ImGui.treePop()
                }
            }
            ImGui.treePop()
        }
        ImGui.popId()
        ImGui.text("treeNodeGetOpen(\"node 0\"): ${ImGui.treeNodeGetOpen("node 0")}")
    }

    private fun drawSectionCombosLists() {
        ImGui.separatorText("Combo")
        ImGui.combo("combo (closed)", comboIndex, comboItems)
        if (ImGui.beginCombo("combo (custom preview)", comboItems[comboIndex[0]], ImGuiComboFlags.NONE)) {
            for (i in comboItems.indices) {
                if (ImGui.selectable(comboItems[i], comboIndex[0] == i)) {
                    comboIndex[0] = i
                }
            }
            ImGui.endCombo()
        }
        if (ImGui.beginCombo(
                "combo (large)",
                comboItems[comboIndex[0]],
                ImGuiComboFlags.HEIGHT_LARGE,
            )
        ) {
            for (i in comboItems.indices) {
                if (ImGui.selectable(comboItems[i], comboIndex[0] == i)) {
                    comboIndex[0] = i
                }
            }
            ImGui.endCombo()
        }

        ImGui.separatorText("ListBox")
        ImGui.listBox("listbox", listBoxIndex, listBoxItems)
        if (ImGui.beginListBox("listbox (custom)", ImVec2(-1f, 96f))) {
            for (i in listBoxItems.indices) {
                if (ImGui.selectable(listBoxItems[i], listBoxIndex[0] == i)) {
                    listBoxIndex[0] = i
                }
            }
            ImGui.endListBox()
        }

        ImGui.separatorText("Selectables in two columns")
        ImGui.columns(2, "selcols")
        for (i in 0 until 6) {
            ImGui.selectable("item $i", querySelected[0] == i)
            if (querySelected[0] == i) ImGui.setItemDefaultFocus()
            ImGui.nextColumn()
        }
        ImGui.columns(1)
    }

    private fun drawSectionProgressMisc() {
        ImGui.separatorText("ProgressBar")
        ImGui.progressBar(progress[0], ImVec2(-1f, 0f), "${(progress[0] * 100).toInt()}%")
        ImGui.progressBar(progress[0], ImVec2(-1f, 0f))
        ImGui.progressBar(0f, ImVec2(-1f, 0f), "0.0")
        ImGui.progressBar(1f, ImVec2(-1f, 0f), "1.0")

        ImGui.separatorText("Misc")
        ImGui.button("Button", ImVec2(120f, 0f))
        ImGui.sameLine()
        ImGui.button("Same line", ImVec2(160f, 0f))
        ImGui.button("Default size", ImVec2(0f, 0f))
        ImGui.sameLine()
        ImGui.button("Custom height", ImVec2(0f, 40f))
        ImGui.spacing()
        ImGui.newLine()
        ImGui.text("after spacing + newLine")

        ImGui.separatorText("Shortcut")
        if (ImGui.shortcut(ImGuiKey.MOD_CTRL or ImGuiKey.A)) {
            ImGui.text("Ctrl+A pressed!")
        } else {
            ImGui.textDisabled("Press Ctrl+A...")
        }

        ImGui.separatorText("Clipboard")
        if (ImGui.button("Set clipboard text")) {
            ImGui.setClipboardText("imgui-kmp clipboard")
        }
        ImGui.sameLine()
        ImGui.text("Current: \"${ImGui.getClipboardText()}\"")
    }

    private fun drawSectionLayout() {
        ImGui.separatorText("Child windows")
        if (ImGui.beginChild("child fixed", ImVec2(childFixed[0], 100f), ImGuiChildFlags.BORDERS)) {
            ImGui.text("fixed size child")
            ImGui.sliderFloat("width", childFixed, 100f, 300f)
        }
        ImGui.endChild()
        if (ImGui.beginChild(
                "child auto",
                ImVec2(childAuto[0], 100f),
                ImGuiChildFlags.BORDERS or ImGuiChildFlags.AUTO_RESIZE_X,
            )
        ) {
            ImGui.text("auto resize child")
            ImGui.sliderFloat("width", childAuto, 100f, 300f)
        }
        ImGui.endChild()

        ImGui.separatorText("Groups")
        ImGui.text("Two groups of buttons:")
        ImGui.beginGroup()
        ImGui.button("A", ImVec2(90f, 30f))
        ImGui.button("B", ImVec2(90f, 30f))
        ImGui.button("C", ImVec2(90f, 30f))
        ImGui.endGroup()
        ImGui.sameLine()
        ImGui.beginGroup()
        ImGui.button("D", ImVec2(90f, 30f))
        ImGui.button("E", ImVec2(90f, 30f))
        ImGui.endGroup()
        ImGui.text("after groups")

        ImGui.separatorText("SameLine & spacing")
        ImGui.text("Left")
        ImGui.sameLine(0f, 40f)
        ImGui.text("offset 40")
        ImGui.sameLine()
        ImGui.text("immediate")
        ImGui.dummy(ImVec2(10f, 10f))

        ImGui.separatorText("Indent / unindent")
        ImGui.indent(40f)
        ImGui.text("indented by 40")
        ImGui.unindent(40f)
        ImGui.text("back")

        ImGui.separatorText("Item width")
        ImGui.pushItemWidth(200f)
        ImGui.sliderFloat("##w200", sliderF, 0f, 1f)
        ImGui.popItemWidth()
        ImGui.pushItemWidth(80f)
        ImGui.sliderFloat("##w80", sliderF, 0f, 1f)
        ImGui.popItemWidth()
        ImGui.setNextItemWidth(120f)
        ImGui.sliderFloat("##w120", sliderF, 0f, 1f)
        ImGui.setNextItemWidth(-0.5f)
        ImGui.sliderFloat("##w050", sliderF, 0f, 1f)

        ImGui.separatorText("Columns (legacy)")
        ImGui.columns(3, "cols3")
        ImGui.text("col ${ImGui.getColumnIndex()}")
        ImGui.nextColumn()
        ImGui.setColumnWidth(1, 100f)
        ImGui.text("col ${ImGui.getColumnIndex()} (width 100)")
        ImGui.nextColumn()
        ImGui.text("col ${ImGui.getColumnIndex()}")
        ImGui.columns(1)

        ImGui.separatorText("Misc layout")
        ImGui.alignTextToFramePadding()
        ImGui.text("aligned to frame padding")
        ImGui.sameLine()
        ImGui.button("SameLine button")
        ImGui.labelText("getWindowWidth", "${fmt(ImGui.getWindowWidth().toDouble(), 1)}")
        ImGui.labelText("getWindowHeight", "${fmt(ImGui.getWindowHeight().toDouble(), 1)}")
    }

    private fun drawSectionTables() {
        ImGui.separatorText("Basic table")
        if (ImGui.beginTable("table1", 3, ImGuiTableFlags.BORDERS or ImGuiTableFlags.ROW_BG)) {
            ImGui.tableSetupColumn("Name", ImGuiTableColumnFlags.WIDTH_FIXED, 100f)
            ImGui.tableSetupColumn("Value")
            ImGui.tableSetupColumn("Notes", ImGuiTableColumnFlags.WIDTH_STRETCH)
            ImGui.tableHeadersRow()
            for (row in 0 until 4) {
                ImGui.tableNextRow()
                ImGui.tableNextColumn()
                ImGui.text("row $row")
                ImGui.tableNextColumn()
                ImGui.text("${sliderF[0] + row}")
                ImGui.tableNextColumn()
                ImGui.text("note $row")
            }
            ImGui.endTable()
        }

        ImGui.separatorText("Resizable / reorderable / hideable")
        if (ImGui.beginTable(
                "table2",
                3,
                ImGuiTableFlags.BORDERS or ImGuiTableFlags.RESIZABLE or
                    ImGuiTableFlags.REORDERABLE or ImGuiTableFlags.HIDEABLE or
                    ImGuiTableFlags.ROW_BG,
            )
        ) {
            ImGui.tableSetupColumn("A", ImGuiTableColumnFlags.WIDTH_FIXED, 120f)
            ImGui.tableSetupColumn("B", ImGuiTableColumnFlags.WIDTH_FIXED, 120f)
            ImGui.tableSetupColumn("C")
            ImGui.tableHeadersRow()
            for (row in 0 until 5) {
                ImGui.tableNextRow()
                for (col in 0 until 3) {
                    ImGui.tableNextColumn()
                    ImGui.text("cell $col/$row")
                }
            }
            ImGui.endTable()
        }

        ImGui.separatorText("Angled headers")
        if (ImGui.beginTable("table3", 4, ImGuiTableFlags.BORDERS or ImGuiTableFlags.ROW_BG)) {
            for (i in 0 until 4) {
                ImGui.tableSetupColumn("column $i", ImGuiTableColumnFlags.ANGLED_HEADER)
            }
            ImGui.tableAngledHeadersRow()
            ImGui.tableHeadersRow()
            for (row in 0 until 3) {
                ImGui.tableNextRow()
                for (col in 0 until 4) {
                    ImGui.tableNextColumn()
                    ImGui.text("r${row}c$col")
                }
            }
            ImGui.endTable()
        }

        ImGui.separatorText("Scroll freeze")
        if (ImGui.beginTable("table4", 4, ImGuiTableFlags.BORDERS or ImGuiTableFlags.ROW_BG)) {
            ImGui.tableSetupColumn("frozen col")
            ImGui.tableSetupColumn("b")
            ImGui.tableSetupColumn("c")
            ImGui.tableSetupColumn("d")
            ImGui.tableSetupScrollFreeze(1, 1)
            ImGui.tableHeadersRow()
            for (row in 0 until 20) {
                ImGui.tableNextRow()
                for (col in 0 until 4) {
                    ImGui.tableNextColumn()
                    ImGui.text("r${row}c$col")
                }
            }
            ImGui.endTable()
        }

        ImGui.separatorText("Row background color")
        if (ImGui.beginTable("table5", 2, ImGuiTableFlags.BORDERS or ImGuiTableFlags.ROW_BG)) {
            ImGui.tableSetupColumn("Row")
            ImGui.tableSetupColumn("Color")
            ImGui.tableHeadersRow()
            for (row in 0 until 4) {
                ImGui.tableNextRow(0, ImGuiTableRowFlags.HEADERS)
                ImGui.tableSetBgColor(ImGuiTableBgTarget.ROW_BG0, ImGui.getColorU32(ImGuiCol.HEADER, 0.2f))
                ImGui.tableNextColumn()
                ImGui.text("row $row")
                ImGui.tableNextColumn()
                ImGui.text("highlighted")
            }
            ImGui.endTable()
        }

        ImGui.separatorText("Table queries")
        if (ImGui.beginTable("table6", 2, ImGuiTableFlags.BORDERS)) {
            ImGui.tableSetupColumn("Query")
            ImGui.tableSetupColumn("Value")
            ImGui.tableHeadersRow()
            ImGui.tableNextRow()
            ImGui.tableNextColumn()
            ImGui.text("tableGetColumnCount")
            ImGui.tableNextColumn()
            ImGui.text("${ImGui.tableGetColumnCount()}")
            ImGui.tableNextRow()
            ImGui.tableNextColumn()
            ImGui.text("tableGetColumnName(1)")
            ImGui.tableNextColumn()
            ImGui.text(ImGui.tableGetColumnName(1))
            ImGui.tableNextRow()
            ImGui.tableNextColumn()
            ImGui.text("tableGetColumnIndex")
            ImGui.tableNextColumn()
            ImGui.text("${ImGui.tableGetColumnIndex()}")
            ImGui.endTable()
        }

        ImGui.separatorText("tableHeader")
        if (ImGui.beginTable("table7", 2, ImGuiTableFlags.BORDERS or ImGuiTableFlags.SIZING_FIXED_SAME)) {
            ImGui.tableSetupColumn("manual")
            ImGui.tableSetupColumn("manual")
            ImGui.tableHeader("A")
            ImGui.tableNextColumn()
            ImGui.tableHeader("B")
            ImGui.tableNextRow()
            ImGui.tableNextColumn()
            ImGui.text("a")
            ImGui.tableNextColumn()
            ImGui.text("b")
            ImGui.endTable()
        }
    }

    private fun drawSectionTabs() {
        ImGui.separatorText("TabBar")
        if (ImGui.beginTabBar("tabs")) {
            if (ImGui.beginTabItem("Tab A")) {
                ImGui.text("Content of tab A")
                ImGui.endTabItem()
            }
            if (ImGui.beginTabItem("Tab B")) {
                ImGui.text("Content of tab B")
                ImGui.endTabItem()
            }
            if (ImGui.beginTabItem("Tab C (closeable)", tabClose)) {
                ImGui.text("Close via the X button; state survives in tabClose.")
                ImGui.endTabItem()
            }
            ImGui.tabItemButton("+", ImGuiTabItemFlags.TRAILING or ImGuiTabItemFlags.NO_PUSH_ID)
            ImGui.endTabBar()
        }
        if (!tabClose[0]) {
            ImGui.text("Tab C was closed.")
            if (ImGui.button("Reset tab state")) tabClose[0] = true
        }

        ImGui.separatorText("tabItemButton + setTabItemClosed")
        if (ImGui.beginTabBar("tabs2")) {
            if (ImGui.tabItemButton("+", ImGuiTabItemFlags.TRAILING or ImGuiTabItemFlags.NO_PUSH_ID)) {
                ImGui.setTabItemClosed("Tab A")
                ImGui.text("closed Tab A")
            }
            if (ImGui.beginTabItem("Tab A")) {
                ImGui.text("Tab A (can be closed by the + button above)")
                ImGui.endTabItem()
            }
            ImGui.endTabBar()
        }
    }

    private fun drawSectionPopups() {
        ImGui.separatorText("OpenPopup + BeginPopup")
        if (ImGui.button("Open popup")) {
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

        ImGui.separatorText("BeginPopupContextItem")
        ImGui.selectable("Right-click me")
        if (ImGui.beginPopupContextItem("ctx1")) {
            ImGui.text("context menu on item")
            if (ImGui.menuItem("do thing")) {
                ImGui.closeCurrentPopup()
            }
            ImGui.endPopup()
        }

        ImGui.separatorText("BeginPopupContextWindow")
        ImGui.text("Right-click inside this window region:")
        if (ImGui.beginChild("ctx_region", ImVec2(-1f, 80f), ImGuiChildFlags.BORDERS)) {
            ImGui.text("(empty region)")
        }
        ImGui.endChild()
        if (ImGui.beginPopupContextWindow("ctx2", ImGuiPopupFlags.MOUSE_BUTTON_RIGHT)) {
            ImGui.text("window context menu")
            ImGui.endPopup()
        }

        ImGui.separatorText("OpenPopupOnItemClick")
        ImGui.button("Click me to open popup")
        ImGui.openPopupOnItemClick("popup3", ImGuiPopupFlags.MOUSE_BUTTON_LEFT)
        if (ImGui.beginPopup("popup3")) {
            ImGui.text("popup opened on left click")
            ImGui.endPopup()
        }

        ImGui.separatorText("Modal")
        if (ImGui.button("Open modal")) {
            ImGui.openPopup("modal1")
        }
        if (ImGui.beginPopupModal("modal1", modalOpen, ImGuiWindowFlags.ALWAYS_AUTO_RESIZE)) {
            ImGui.text("This is a modal window.")
            ImGui.text("It blocks input to the rest of the app.")
            ImGui.separator()
            if (ImGui.button("Close", ImVec2(120f, 0f))) {
                ImGui.closeCurrentPopup()
            }
            ImGui.endPopup()
        }

        ImGui.separatorText("Tooltips")
        ImGui.button("Hover for setTooltip")
        ImGui.setTooltip("setTooltip on the button")
        ImGui.button("Hover for setItemTooltip")
        ImGui.setItemTooltip("setItemTooltip on the button")
        ImGui.button("Hover for beginItemTooltip")
        if (ImGui.beginItemTooltip()) {
            ImGui.text("beginItemTooltip")
            ImGui.separator()
            ImGui.text("multi-line tooltip")
            ImGui.endTooltip()
        }
        ImGui.button("Hover for beginTooltip")
        if (ImGui.beginTooltip()) {
            ImGui.text("beginTooltip")
            ImGui.endTooltip()
        }
    }

    private fun drawSectionDragDrop() {
        ImGui.separatorText("Drag source")
        ImGui.button("Drag me")
        if (ImGui.beginDragDropSource(ImGuiDragDropFlags.SOURCE_NO_PREVIEW_TOOLTIP)) {
            ImGui.setDragDropPayload("DND_DEMO", "demo payload".encodeToByteArray())
            ImGui.text("Payload: demo")
            ImGui.endDragDropSource()
        }

        ImGui.separatorText("Drop target")
        ImGui.text("Drop the button into the box:")
        if (ImGui.beginChild("dnd_target", ImVec2(-1f, 80f), ImGuiChildFlags.BORDERS)) {
            if (ImGui.beginDragDropTarget()) {
                val payload = ImGui.acceptDragDropPayload("DND_DEMO")
                if (payload != null) {
                    dropCount[0]++
                    dropPayload = payload.decodeToString()
                }
                ImGui.endDragDropTarget()
            }
            ImGui.text("Drops: ${dropCount[0]}")
            if (dropCount[0] > 0) ImGui.text("Last payload: \"$dropPayload\"")
        }
        ImGui.endChild()
        ImGui.text("getDragDropPayload: \"${ImGui.getDragDropPayload()}\"")
    }

    private fun drawSectionQuery() {
        ImGui.separatorText("Item queries")
        ImGui.button("hover me")
        ImGui.text("isItemHovered: ${ImGui.isItemHovered()}")
        ImGui.text("isItemActive: ${ImGui.isItemActive()}")
        ImGui.text("isItemClicked(0): ${ImGui.isItemClicked(ImGuiMouseButton.LEFT)}")
        ImGui.text("isItemFocused: ${ImGui.isItemFocused()}")
        ImGui.text("isItemEdited: ${ImGui.isItemEdited()}")
        ImGui.text("isItemActivated: ${ImGui.isItemActivated()}")
        ImGui.text("isItemDeactivated: ${ImGui.isItemDeactivated()}")
        ImGui.text("isItemToggledOpen: ${ImGui.isItemToggledOpen()}")
        ImGui.text("getItemRectMin/Max/Size: ${ImGui.getItemRectMin()} / ${ImGui.getItemRectMax()} / ${ImGui.getItemRectSize()}")
        ImGui.text("isAnyItemHovered: ${ImGui.isAnyItemHovered()}")
        ImGui.text("isAnyItemActive: ${ImGui.isAnyItemActive()}")
        ImGui.text("isAnyItemFocused: ${ImGui.isAnyItemFocused()}")

        ImGui.separatorText("Window queries")
        ImGui.text("isWindowHovered: ${ImGui.isWindowHovered()}")
        ImGui.text("isWindowFocused: ${ImGui.isWindowFocused()}")
        ImGui.text("isWindowAppearing: ${ImGui.isWindowAppearing()}")
        ImGui.text("isWindowCollapsed: ${ImGui.isWindowCollapsed()}")
        ImGui.text("isRectVisible(100,100): ${ImGui.isRectVisible(ImVec2(100f, 100f))}")
        ImGui.text("getWindowPos: ${ImGui.getWindowPos()}")
        ImGui.text("getWindowSize: ${ImGui.getWindowSize()}")
        ImGui.text("getWindowContentRegionMin/Max: ${ImGui.getWindowContentRegionMin()} / ${ImGui.getWindowContentRegionMax()}")
        ImGui.text("getContentRegionAvail: ${ImGui.getContentRegionAvail()}")

        ImGui.separatorText("Disabled")
        ImGui.beginDisabled()
        ImGui.button("disabled button")
        ImGui.sliderFloat("disabled slider", sliderF, 0f, 1f)
        ImGui.endDisabled()
        ImGui.text("isItemHovered (ALLOW_WHEN_DISABLED): ${ImGui.isItemHovered(ImGuiHoveredFlags.ALLOW_WHEN_DISABLED)}")

        ImGui.separatorText("pushItemFlag")
        ImGui.pushItemFlag(ImGuiItemFlags.DISABLED, true)
        ImGui.button("disabled via flag")
        ImGui.popItemFlag()
        ImGui.pushItemFlag(ImGuiItemFlags.NO_TAB_STOP, true)
        ImGui.button("no tab stop")
        ImGui.popItemFlag()
        ImGui.text("getItemFlags: ${hex8(ImGui.getItemFlags())}")
    }

    private fun drawSectionKeyboardMouse() {
        ImGui.separatorText("Mouse")
        checkbox("Mouse Left", mouseBtns, 0)
        ImGui.sameLine()
        checkbox("Mouse Middle", mouseBtns, 1)
        ImGui.sameLine()
        checkbox("Mouse Right", mouseBtns, 2)
        ImGui.text("isMouseDown(0): ${ImGui.isMouseDown(ImGuiMouseButton.LEFT)}")
        ImGui.text("isMouseClicked(0): ${ImGui.isMouseClicked(ImGuiMouseButton.LEFT)}")
        ImGui.text("isMouseDoubleClicked(0): ${ImGui.isMouseDoubleClicked(ImGuiMouseButton.LEFT)}")
        ImGui.text("isMouseReleased(1): ${ImGui.isMouseReleased(ImGuiMouseButton.RIGHT)}")
        ImGui.text("isMouseDragging(0): ${ImGui.isMouseDragging(ImGuiMouseButton.LEFT)}")
        ImGui.text("isAnyMouseDown: ${ImGui.isAnyMouseDown()}")
        ImGui.text("getMousePos: ${ImGui.getMousePos()}")
        ImGui.text("getMouseDragDelta(0): ${ImGui.getMouseDragDelta(ImGuiMouseButton.LEFT)}")
        if (ImGui.button("Reset drag delta")) {
            ImGui.resetMouseDragDelta(ImGuiMouseButton.LEFT)
        }

        ImGui.separatorText("Mouse cursor")
        if (ImGui.combo("cursor", cursorIdx, arrayOf("Arrow", "TextInput", "ResizeAll", "ResizeNS", "ResizeEW", "ResizeNESW", "ResizeNWSE", "Hand", "Wait", "Progress", "NotAllowed"))) {
            ImGui.setMouseCursor(cursorIdx[0])
        }
        ImGui.text("getMouseCursor: ${ImGui.getMouseCursor()}")

        ImGui.separatorText("Keyboard")
        ImGui.combo("key", keyIdx, keyNames)
        val key = when (keyIdx[0]) {
            0 -> ImGuiKey.A
            1 -> ImGuiKey.B
            2 -> ImGuiKey.C
            3 -> ImGuiKey.D
            4 -> ImGuiKey.UP_ARROW
            5 -> ImGuiKey.ENTER
            else -> ImGuiKey.ESCAPE
        }
        ImGui.text("isKeyDown: ${ImGui.isKeyDown(key)}")
        ImGui.text("isKeyPressed: ${ImGui.isKeyPressed(key)}")
        ImGui.text("isKeyReleased: ${ImGui.isKeyReleased(key)}")
        ImGui.text("getKeyName: ${ImGui.getKeyName(key)}")
        ImGui.text(
            "Keys down (A-F): ${(0..5).filter { ImGui.isKeyDown(ImGuiKey.A + it) }.joinToString { (it + 65).toChar().toString() }}",
        )
    }

    private fun drawSectionScrollingDrawing() {
        ImGui.separatorText("Scrolling")
        ImGui.sliderFloat("scroll Y", scrollY, 0f, 1f)
        ImGui.sliderFloat("scroll X", scrollX, 0f, 1f)
        ImGui.setScrollX(scrollX[0] * ImGui.getScrollMaxX())
        ImGui.setScrollY(scrollY[0] * ImGui.getScrollMaxY())
        ImGui.text("getScrollMaxX/Y: ${ImGui.getScrollMaxX()} / ${ImGui.getScrollMaxY()}")
        ImGui.text("getScrollX/Y: ${ImGui.getScrollX()} / ${ImGui.getScrollY()}")
        if (ImGui.button("Scroll to bottom")) {
            ImGui.setScrollHereY(1f)
        }
        ImGui.sameLine()
        if (ImGui.button("Scroll to top")) {
            ImGui.setScrollHereY(0f)
        }

        ImGui.separatorText("Custom drawing (getWindowDrawList)")
        val p = ImGui.getCursorScreenPos()
        val dl = ImGui.getWindowDrawList()
        val base = ImGui.getColorU32(ImGuiCol.TEXT, 1f)
        dl.DrawLine(ImVec2(p.x, p.y), ImVec2(p.x + 100f, p.y + 50f), base, 1f)
        dl.DrawRect(ImVec2(p.x + 110f, p.y), ImVec2(p.x + 200f, p.y + 50f), base, 4f)
        dl.DrawRectFilled(ImVec2(p.x + 210f, p.y), ImVec2(p.x + 300f, p.y + 50f), ImGui.getColorU32(ImGuiCol.PLOT_HISTOGRAM, 0.5f))
        dl.DrawCircle(ImVec2(p.x + 40f, p.y + 90f), 20f, base, 0, 1.5f)
        dl.DrawCircleFilled(ImVec2(p.x + 90f, p.y + 90f), 20f, ImGui.getColorU32(ImGuiCol.PLOT_LINES))
        dl.DrawTriangle(
            ImVec2(p.x + 150f, p.y + 70f),
            ImVec2(p.x + 170f, p.y + 110f),
            ImVec2(p.x + 130f, p.y + 110f),
            base,
            1.5f,
        )
        dl.DrawQuad(
            ImVec2(p.x + 200f, p.y + 70f),
            ImVec2(p.x + 220f, p.y + 70f),
            ImVec2(p.x + 240f, p.y + 110f),
            ImVec2(p.x + 200f, p.y + 110f),
            base,
        )
        dl.DrawPolyline(
            arrayOf(
                ImVec2(p.x + 280f, p.y + 70f),
                ImVec2(p.x + 300f, p.y + 110f),
                ImVec2(p.x + 320f, p.y + 70f),
            ),
            base,
            closed = true,
            thickness = 2f,
        )
        dl.DrawText(ImVec2(p.x + 40f, p.y + 130f), "DrawText!", base)
        ImGui.dummy(ImVec2(340f, 160f))
    }

    private fun drawSectionStyle() {
        ImGui.separatorText("Style selectors")
        ImGui.showStyleSelector("Style selector")
        ImGui.showFontSelector("Font selector")
        if (ImGui.button("Style editor (window)")) {
            ImGui.showStyleEditor()
        }

        ImGui.separatorText("pushStyleColor")
        ImGui.pushStyleColor(ImGuiCol.BUTTON, ImVec4(0.4f, 0.2f, 0.6f, 1f))
        ImGui.pushStyleColor(ImGuiCol.BUTTON_HOVERED, ImVec4(0.5f, 0.3f, 0.7f, 1f))
        ImGui.pushStyleColor(ImGuiCol.BUTTON_ACTIVE, ImVec4(0.6f, 0.4f, 0.8f, 1f))
        ImGui.button("colored button")
        ImGui.popStyleColor(3)
        ImGui.text("getStyleColorVec4(BUTTON): ${ImGui.getStyleColorVec4(ImGuiCol.BUTTON)}")

        ImGui.separatorText("pushStyleVar")
        ImGui.pushStyleVarFloat(ImGuiStyleVar.FRAME_ROUNDING, 8f)
        ImGui.pushStyleVarVec2(ImGuiStyleVar.FRAME_PADDING, ImVec2(10f, 10f))
        ImGui.button("rounded + padded button")
        ImGui.popStyleVar(2)
        ImGui.pushStyleVarFloat(ImGuiStyleVar.GRAB_ROUNDING, 0f)
        ImGui.sliderFloat("square grab", sliderF, 0f, 1f)
        ImGui.popStyleVar()
        ImGui.pushStyleVarFloat(ImGuiStyleVar.SEPARATOR_TEXT_BORDER_SIZE, 2f)
        ImGui.separatorText("thick separator text border")
        ImGui.popStyleVar()

        ImGui.separatorText("styleColors")
        ImGui.text("Note: resetting the palette recolors the whole app.")
        if (ImGui.button("Dark")) ImGui.styleColorsDark()
        ImGui.sameLine()
        if (ImGui.button("Light")) ImGui.styleColorsLight()
        ImGui.sameLine()
        if (ImGui.button("Classic")) ImGui.styleColorsClassic()

        ImGui.separatorText("Clear color (shared with the renderer)")
        ImGui.colorEdit4("clear color", clearColor)
    }

    private fun drawSectionLoggingIo() {
        ImGui.separatorText("Logging")
        ImGui.text("ImGui can log output to the clipboard, a file or TTY.")
        if (ImGui.button("Log to TTY")) {
            ImGui.logToTTY()
            ImGui.logText("imgui-kmp demo log message\n")
            ImGui.logFinish()
        }
        ImGui.sameLine()
        if (ImGui.button("Log to clipboard")) {
            ImGui.logToClipboard()
            ImGui.logText("imgui-kmp demo log message\n")
            ImGui.logFinish()
        }
        ImGui.sameLine()
        if (ImGui.button("Log to file")) {
            ImGui.logToFile()
            ImGui.logText("imgui-kmp demo log message\n")
            ImGui.logFinish()
        }

        ImGui.separatorText("IO")
        val io = ImGui.getIO()
        ImGui.labelText("deltaTime", fmt(io.deltaTime.toDouble(), 4))
        ImGui.labelText("displaySize", "${io.displaySize.x} x ${io.displaySize.y}")
        ImGui.labelText("displayFramebufferScale", "${io.displayFramebufferScale.x} x ${io.displayFramebufferScale.y}")
        ImGui.labelText("fontGlobalScale", fmt(io.fontGlobalScale.toDouble(), 2))
        ImGui.labelText("backendFlags", hex8(io.backendFlags))
        ImGui.labelText("configFlags", hex8(io.configFlags))
        ImGui.labelText("iniFilename", "${io.iniFilename}")

        ImGui.separatorText(".ini settings")
        if (ImGui.button("Save to disk")) {
            ImGui.saveIniSettingsToDisk()
        }
        ImGui.sameLine()
        if (ImGui.button("Load from disk")) {
            ImGui.loadIniSettingsFromDisk()
        }
        ImGui.sameLine()
        if (ImGui.button("Save to memory")) {
            val ini = ImGui.saveIniSettingsToMemory()
            ImGui.loadIniSettingsFromMemory(ini ?: "")
        }

        ImGui.separatorText("Other windows")
        if (ImGui.button("About")) showAbout[0] = !showAbout[0]
        ImGui.sameLine()
        if (ImGui.button("Metrics")) showMetrics[0] = !showMetrics[0]
        ImGui.sameLine()
        if (ImGui.button("Debug log")) showDebugLog[0] = !showDebugLog[0]
    }

    // =====================================================================
    // ImPlot demo window
    // =====================================================================

    private fun drawPlotDemoWindow() {
        ImGui.setNextWindowSize(ImVec2(600f, 480f), ImGuiCond.FIRST_USE_EVER)
        if (!ImGui.begin("ImPlot Demo")) {
            ImGui.end()
            return
        }
        ImGui.text("ImPlot ${ImPlot.getColormapCount()} built-in colormaps; plots below use the bound API.")
        ImGui.separator()

        if (ImGui.beginChild("plot_sidebar", ImVec2(190f, 0f), ImGuiChildFlags.BORDERS)) {
            val sections = arrayOf(
                "Line & Scatter",
                "Stairs & Shaded",
                "Bars & Stems",
                "Error Bars",
                "Histograms & Heatmap",
                "Digital & Pie & Bubbles",
                "Polygon & Bar Groups",
                "Text & Dummy & Image",
                "Axes & Scales",
                "Multi-Axis",
                "Subplots",
                "Drag Tools & Annotations",
                "Colormaps",
                "Queries & Coordinates",
                "Legend & Drag & Drop",
                "Style",
            )
            for (i in sections.indices) {
                if (ImGui.selectable(sections[i], plotSection == i)) plotSection = i
            }
        }
        ImGui.endChild()
        ImGui.sameLine()

        if (ImGui.beginChild("plot_content", ImVec2(0f, 0f), ImGuiChildFlags.BORDERS)) {
            when (plotSection) {
                0 -> drawPlotLineScatter()
                1 -> drawPlotStairsShaded()
                2 -> drawPlotBarsStems()
                3 -> drawPlotErrorBars()
                4 -> drawPlotHistograms()
                5 -> drawPlotDigitalPieBubbles()
                6 -> drawPlotPolygonGroups()
                7 -> drawPlotTextImage()
                8 -> drawPlotAxesScales()
                9 -> drawPlotMultiAxis()
                10 -> drawPlotSubplots()
                11 -> drawPlotDragTools()
                12 -> drawPlotColormaps()
                13 -> drawPlotQueries()
                14 -> drawPlotLegendDragDrop()
                15 -> drawPlotStyle()
            }
        }
        ImGui.endChild()
        ImGui.end()
    }

    private fun plotWithAxes(
        title: String,
        height: Float = 220f,
        xLabel: String? = null,
        yLabel: String? = null,
        flags: Int = ImPlotFlags.NONE,
        block: () -> Unit,
    ) {
        if (ImPlot.beginPlot(title, ImVec2(-1f, height), flags)) {
            ImPlot.setupAxes(xLabel, yLabel)
            // NOTE: no SetupFinish() here on purpose — the per-section block
            // may call more setup functions (SetupAxesLimits, SetupAxisTicks,
            // ...) which would be rejected after the setup phase is locked.
            block()
            ImPlot.endPlot()
        }
    }

    private fun drawPlotLineScatter() {
        ImGui.separatorText("Line plots")
        ImGui.text("Drag with the mouse to pan, scroll to zoom, double-click to reset.")
        plotWithAxes("Sine waves", 200f, "time", "amplitude", ImPlotFlags.CROSSHAIRS) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("sin", sinData, spec = ImPlotSpec(lineWeight = 2f))
            ImPlot.plotLine("cos", cosData, spec = ImPlotSpec(lineColor = ImVec4(1f, 0.5f, 0f, 1f), lineWeight = 1.5f))
            ImPlot.plotLine("sin+45deg", phaseData, spec = ImPlotSpec(lineColor = ImVec4(0f, 1f, 0.5f, 1f)))
        }

        ImGui.separatorText("Scatter")
        plotWithAxes("Scatter plot", 200f, "x", "y") {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotScatter("random", scatterXs, scatterYs, ImPlotSpec(marker = ImPlotMarker.CIRCLE, markerSize = 4f))
            ImPlot.plotScatter(
                "second",
                sinData.copyOfRange(0, 50),
                cosData.copyOfRange(0, 50),
                ImPlotSpec(marker = ImPlotMarker.DIAMOND),
            )
        }
    }

    private fun drawPlotStairsShaded() {
        ImGui.separatorText("Stairs")
        plotWithAxes("Stairs", 180f, "x", "y") {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotStairs("stairs", stairXs, stairYs, spec = ImPlotSpec(lineWeight = 2f))
        }

        ImGui.separatorText("Shaded")
        plotWithAxes("Shaded", 180f, "x", "y") {
            ImPlot.setupAxesLimits(0.0, 1.0, -0.2, 1.2, ImPlotCond.ONCE)
            ImPlot.plotShaded("shaded", xData, sinData, 0.0, ImPlotSpec(fillAlpha = 0.5f))
            ImPlot.plotLine("line", xData, sinData)
        }
    }

    private fun drawPlotBarsStems() {
        ImGui.separatorText("Bars")
        plotWithAxes("Bars", 180f, "x", "y") {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotBars("bars", barXs, barYs, 0.05, ImPlotSpec(fillColor = ImVec4(0.4f, 0.7f, 1f, 1f)))
        }
        plotWithAxes("Bars (categorical)", 180f, null, null) {
            ImPlot.setupAxesLimits(0.0, 9.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotBars("bars", barYs, barSize = 0.7, shift = 0.0)
        }

        ImGui.separatorText("Stems")
        plotWithAxes("Stems", 160f, "index", "value") {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotStems("stems", xData.copyOfRange(0, 10), barYs, 0.0, ImPlotSpec(marker = ImPlotMarker.CIRCLE, markerSize = 4f))
        }
    }

    private fun drawPlotErrorBars() {
        ImGui.separatorText("Error bars")
        plotWithAxes("Error bars", 200f, "x", "y") {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotErrorBars("err", errorXs, errorYs, errorNeg, errorPos)
            ImPlot.plotLine("line", errorXs, errorYs)
        }
    }

    private fun drawPlotHistograms() {
        ImGui.separatorText("Histogram")
        plotWithAxes("Histogram", 180f, null, "count") {
            ImPlot.plotHistogram("distribution", histogramData, 60)
        }
        ImPlot.pushColormap(ImPlotColormap.PLASMA)
        plotWithAxes("Histogram (colormap)", 180f, null, "count") {
            ImPlot.plotHistogram("distribution", histogramData, ImPlotBin.STURGES)
        }
        ImPlot.popColormap()

        ImGui.separatorText("Histogram 2D")
        plotWithAxes("Histogram 2D", 220f, "x", "y") {
            ImPlot.setupAxesLimits(-3.0, 3.0, -3.0, 3.0, ImPlotCond.ONCE)
            ImPlot.plotHistogram2D("hist2d", histogram2DXs, histogram2DYs, 30, 30, -3.0, 3.0, -3.0, 3.0)
        }

        ImGui.separatorText("Heatmap")
        plotWithAxes("Heatmap", 220f, null, null) {
            ImPlot.setupAxesLimits(0.0, 30.0, 0.0, 30.0, ImPlotCond.ONCE)
            ImPlot.plotHeatmap("heat", heatmapData, 30, 30, 0.0, 1.0, "%.0f")
        }
    }

    private fun drawPlotDigitalPieBubbles() {
        ImGui.separatorText("Digital")
        plotWithAxes("Digital", 160f, "time", "value") {
            ImPlot.setupAxesLimits(0.0, 100.0, -0.1, 1.1, ImPlotCond.ONCE)
            ImPlot.plotDigital("digital", xData, digitalData)
        }

        ImGui.separatorText("Pie chart")
        plotWithAxes("Pie", 200f, null, null) {
            ImPlot.plotPieChart(pieLabels, pieValues, 0.5, 0.5, 0.45, "%.1f", 90.0)
        }

        ImGui.separatorText("Bubbles")
        plotWithAxes("Bubbles", 220f, "x", "y") {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotBubbles("bubbles", bubbleXs, bubbleYs, bubbleSizes)
        }
    }

    private fun drawPlotPolygonGroups() {
        ImGui.separatorText("Polygon")
        plotWithAxes("Polygon", 200f, "x", "y") {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotPolygon(
                "poly",
                scatterXs.copyOfRange(0, 12),
                scatterYs.copyOfRange(0, 12),
                ImPlotSpec(fillColor = ImVec4(0.4f, 0.6f, 1f, 0.4f)),
            )
        }

        ImGui.separatorText("Bar groups")
        plotWithAxes("Bar groups", 200f, null, null) {
            ImPlot.setupAxesLimits(0.0, 3.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotBarGroups(barGroupLabels, barGroupValues, 2, 4, 0.9, 0.0)
        }

        ImGui.separatorText("Infinite lines")
        plotWithAxes("Infinite lines", 160f, null, null) {
            ImPlot.setupAxesLimits(0.0, 10.0, 0.0, 10.0, ImPlotCond.ONCE)
            ImPlot.plotInfLines("v lines", floatArrayOf(2f, 4f, 6f, 8f))
            ImPlot.plotInfLines(
                "h lines",
                floatArrayOf(3f, 7f),
                ImPlotSpec(lineColor = ImVec4(1f, 0.4f, 0.4f, 1f)),
            )
        }
    }

    private fun drawPlotTextImage() {
        ImGui.separatorText("Text")
        plotWithAxes("Text", 160f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotText("top-left", 0.1, 0.9)
            ImPlot.plotText("bottom-right", 0.8, 0.1)
            ImPlot.plotText("with offset", 0.5, 0.5, ImVec2(20f, -8f))
            ImPlot.plotDummy("dummy legend entry")
        }

        ImGui.separatorText("Image")
        if (fontTextureId != 0L) {
            plotWithAxes("Font atlas image", 220f, null, null) {
                ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
                ImPlot.plotImage(
                    "font atlas",
                    fontTextureId,
                    0.0, 0.0, 1.0, 1.0,
                    tintCol = ImVec4(1f, 1f, 1f, 1f),
                )
            }
        } else {
            ImGui.textDisabled("fontTextureId not set; image demo skipped")
        }

        ImGui.separatorText("ImGui image widgets")
        if (fontTextureId != 0L) {
            ImGui.image(fontTextureId, ImVec2(64f, 64f))
            ImGui.sameLine()
            ImGui.imageWithBg(fontTextureId, ImVec2(64f, 64f), ImVec4(0.2f, 0.2f, 0.2f, 1f))
            ImGui.sameLine()
            if (ImGui.imageButton(fontTextureId, ImVec2(64f, 64f))) {
                ImGui.setTooltip("font texture clicked")
            }
        }
    }

    private fun drawPlotAxesScales() {
        ImGui.separatorText("Log scale")
        plotWithAxes("Log scale", 200f, "x", "y") {
            ImPlot.setupAxis(ImPlotAxis.X1, null, ImPlotAxisFlags.LOCK)
            ImPlot.setupAxis(ImPlotAxis.Y1, null, ImPlotAxisFlags.LOCK)
            ImPlot.setupAxisScale(ImPlotAxis.Y1, ImPlotScale.LOG10)
            ImPlot.setupAxesLimits(0.0, 100.0, 1.0, 100.0, ImPlotCond.ONCE)
            ImPlot.plotLine("exp", logXs, logYs)
        }

        ImGui.separatorText("Time scale")
        plotWithAxes("Time scale", 200f, "time of day", "temperature") {
            ImPlot.setupAxisScale(ImPlotAxis.X1, ImPlotScale.TIME)
            ImPlot.setupAxesLimits(nowEpoch, nowEpoch + 23 * 3600.0, 0.0, 100.0, ImPlotCond.ONCE)
            ImPlot.plotLine("temp", timeYs, xScale = 3600.0, xStart = nowEpoch)
        }

        ImGui.separatorText("Custom ticks")
        plotWithAxes("Custom ticks", 180f, "category", null) {
            ImPlot.setupAxisTicks(ImPlotAxis.X1, doubleArrayOf(1.0, 2.0, 3.0), arrayOf("low", "mid", "high"))
            ImPlot.setupAxesLimits(0.0, 4.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotBars("bars", barYs.copyOfRange(0, 3), barSize = 0.6, shift = 1.0)
        }

        ImGui.separatorText("Axis constraints")
        plotWithAxes("Constrained zoom", 180f, "x", "y") {
            ImPlot.setupAxisLimitsConstraints(ImPlotAxis.X1, 0.0, 1.0)
            ImPlot.setupAxisZoomConstraints(ImPlotAxis.X1, 0.1, 2.0)
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("data", sinData)
            ImPlot.plotScatter("points", scatterXs, scatterYs)
        }

        ImGui.separatorText("Inverted axis")
        plotWithAxes("Inverted Y", 160f, null, null) {
            ImPlot.setupAxis(ImPlotAxis.Y1, null, ImPlotAxisFlags.INVERT)
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("line", sinData)
        }

        ImGui.separatorText("Plot flags")
        plotWithAxes("Flags: NO_LEGEND", 140f, null, null, ImPlotFlags.NO_LEGEND) {
            ImPlot.plotLine("hidden legend", sinData)
        }
        plotWithAxes("Flags: NO_TITLE", 140f, null, null, ImPlotFlags.NO_TITLE) {
            ImPlot.plotLine("no title", sinData)
        }
    }

    private fun drawPlotMultiAxis() {
        ImGui.separatorText("Multiple axes")
        checkbox("X-Axis 2", x2Axis, 0)
        ImGui.sameLine()
        checkbox("Y-Axis 2", y2Axis, 0)
        ImGui.sameLine()
        checkbox("Y-Axis 3", y3Axis, 0)
        ImGui.text("You can drag axes to the opposite side of the plot.")

        if (ImPlot.beginPlot("Multi-Axis Plot", ImVec2(-1f, 240f))) {
            ImPlot.setupAxes("X-Axis 1", "Y-Axis 1")
            ImPlot.setupAxesLimits(0.0, 100.0, 0.0, 10.0)
            if (x2Axis[0]) {
                ImPlot.setupAxis(ImPlotAxis.X2, "X-Axis 2", ImPlotAxisFlags.AUX_DEFAULT)
                ImPlot.setupAxisLimits(ImPlotAxis.X2, 0.0, 100.0)
            }
            if (y2Axis[0]) {
                ImPlot.setupAxis(ImPlotAxis.Y2, "Y-Axis 2", ImPlotAxisFlags.AUX_DEFAULT)
                ImPlot.setupAxisLimits(ImPlotAxis.Y2, 0.0, 1.0)
            }
            if (y3Axis[0]) {
                ImPlot.setupAxis(ImPlotAxis.Y3, "Y-Axis 3", ImPlotAxisFlags.AUX_DEFAULT)
                ImPlot.setupAxisLimits(ImPlotAxis.Y3, 0.0, 300.0)
            }
            ImPlot.setupFinish()
            ImPlot.plotLine("f(x) = x", multiXs, multiXs)
            if (x2Axis[0]) {
                ImPlot.setAxes(ImPlotAxis.X2, ImPlotAxis.Y1)
                ImPlot.plotLine("f(x) = sin(x)*3+1", multiXs2, multiYs)
            }
            if (y2Axis[0]) {
                ImPlot.setAxes(ImPlotAxis.X1, ImPlotAxis.Y2)
                ImPlot.plotLine("f(x) = cos(x)*.2+.5", multiXs, multiYs2)
            }
            if (x2Axis[0] && y3Axis[0]) {
                ImPlot.setAxes(ImPlotAxis.X2, ImPlotAxis.Y3)
                ImPlot.plotLine("f(x) = sin(x+.5)*100+200", multiXs2, multiYs3)
            }
            ImPlot.setAxes(ImPlotAxis.X1, ImPlotAxis.Y1)
            ImPlot.endPlot()
        }
    }

    private fun drawPlotSubplots() {
        ImGui.separatorText("Subplots")
        if (ImPlot.beginSubplots("sub", 2, 2, ImVec2(-1f, 360f), ImPlotSubplotFlags.NONE)) {
            for (row in 0 until 2) {
                for (col in 0 until 2) {
                    if (ImPlot.beginPlot("sub $row/$col", ImVec2(-1f, 0f))) {
                        ImPlot.setupAxes("x", "y")
                        ImPlot.setupFinish()
                        ImPlot.plotLine("sin", sinData)
                        if ((row + col) % 2 == 1) ImPlot.plotScatter("scatter", scatterXs, scatterYs)
                        ImPlot.endPlot()
                    }
                }
            }
            ImPlot.endSubplots()
        }

        ImGui.separatorText("Subplots with shared items")
        if (ImPlot.beginSubplots("sub2", 1, 2, ImVec2(-1f, 200f), ImPlotSubplotFlags.SHARE_ITEMS)) {
            if (ImPlot.beginPlot("left", ImVec2(-1f, 0f))) {
                ImPlot.setupFinish()
                ImPlot.plotLine("shared", sinData)
                ImPlot.endPlot()
            }
            if (ImPlot.beginPlot("right", ImVec2(-1f, 0f))) {
                ImPlot.setupFinish()
                ImPlot.plotLine("shared", cosData)
                ImPlot.endPlot()
            }
            ImPlot.endSubplots()
        }
    }

    private fun drawPlotDragTools() {
        ImGui.separatorText("Drag tools")
        plotWithAxes("Drag point", 160f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("line", sinData)
            ImPlot.dragPoint(0, dragPointX, dragPointY, ImVec4(1f, 0.5f, 0.2f, 1f), 8f)
        }

        plotWithAxes("Drag lines & rect", 200f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("line", sinData)
            ImPlot.dragLineX(1, dragLineXVal, ImVec4(0.2f, 0.7f, 1f, 1f), 2f)
            ImPlot.dragLineY(2, dragLineYVal, ImVec4(1f, 0.7f, 0.2f, 1f), 2f)
            ImPlot.dragRect(3, dragRectXMin, dragRectYMin, dragRectXMax, dragRectYMax, ImVec4(1f, 1f, 1f, 0.8f))
        }

        ImGui.separatorText("Annotations & tags")
        plotWithAxes("Annotations", 180f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("line", sinData)
            ImPlot.annotation(0.25, 0.7, ImVec4(1f, 0.4f, 0.4f, 1f), fmt = "%.2f")
            ImPlot.annotation(
                0.7, 0.3,
                ImVec4(0.4f, 1f, 0.4f, 1f),
                ImVec2(10f, 10f),
                clamp = true,
                round = false,
                fmt = "clamped",
            )
            ImPlot.tagX(0.5, ImVec4(0.9f, 0.9f, 0.2f, 1f), fmt = "%.2f")
            ImPlot.tagY(0.6, ImVec4(0.4f, 0.8f, 1f, 1f), fmt = "%.2f")
        }

        ImGui.separatorText("Drag tool flags")
        plotWithAxes("Delayed drag", 160f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("line", sinData)
            ImPlot.dragPoint(4, dragPointX, dragPointY, ImVec4(1f, 1f, 1f, 1f), 6f, ImPlotDragToolFlags.DELAYED)
        }
    }

    private fun drawPlotColormaps() {
        ImGui.separatorText("Colormaps")
        ImGui.text("${ImPlot.getColormapCount()} built-in colormaps:")
        for (i in 0 until ImPlot.getColormapCount()) {
            ImPlot.colormapButton(ImPlot.getColormapName(i), ImVec2(-1f, 0f), i)
        }

        ImGui.separatorText("Colormap slider & scale")
        ImPlot.colormapSlider("t = ${fmt(colormapT[0].toDouble(), 2)}", colormapT, colormapOut)
        ImGui.text("out color: ${fmt(colormapOut[0].toDouble(), 2)}, ${fmt(colormapOut[1].toDouble(), 2)}, ${fmt(colormapOut[2].toDouble(), 2)}, ${fmt(colormapOut[3].toDouble(), 2)}")
        ImPlot.pushColormap(ImPlotColormap.HOT)
        ImPlot.colormapScale("##scale", 0.0, 1.0, ImVec2(-1f, 60f))
        ImPlot.popColormap()

        ImGui.separatorText("Colormap queries")
        ImGui.text("sampleColormap(0.0): ${ImPlot.sampleColormap(0f)}")
        ImGui.text("sampleColormap(0.5): ${ImPlot.sampleColormap(0.5f)}")
        ImGui.text("sampleColormap(1.0): ${ImPlot.sampleColormap(1f)}")
        ImGui.text("getColormapColor(3): ${ImPlot.getColormapColor(3)}")
        ImGui.text("getColormapName(5): ${ImPlot.getColormapName(5)}")

        ImGui.separatorText("Custom colormap")
        if (ImGui.button("Add custom colormap")) {
            ImPlot.addColormap(
                "my_custom",
                floatArrayOf(
                    1f, 0f, 0f, 1f,
                    0f, 1f, 0f, 1f,
                    0f, 0f, 1f, 1f,
                ),
            )
        }
        ImPlot.colormapIcon(ImPlotColormap.AUTO)
        ImGui.sameLine()
        ImPlot.itemIcon(ImGui.colorConvertFloat4ToU32(ImVec4(0.8f, 0.2f, 0.2f, 1f)))
        ImGui.sameLine()
        ImGui.text("colormapIcon / itemIcon")
        ImGui.text("getLastItemColor: ${hex8(ImPlot.getLastItemColor())}")

        ImGui.separatorText("Plot with pushed colormap")
        ImPlot.pushColormap(ImPlotColormap.PLASMA)
        plotWithAxes("Plasma colormap", 160f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("line", sinData)
            ImPlot.plotScatter("points", scatterXs, scatterYs)
        }
        ImPlot.popColormap()
    }

    private fun drawPlotQueries() {
        ImGui.separatorText("Query")
        plotWithAxes("Mouse & limits query", 200f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("line", sinData)
            val mouse = ImPlot.getPlotMousePos()
            val limits = ImPlot.getPlotLimits()
            ImGui.text("isPlotHovered: ${ImPlot.isPlotHovered()}")
            ImGui.text("isAxisHovered(X1): ${ImPlot.isAxisHovered(ImPlotAxis.X1)}")
            ImGui.text("isPlotSelected: ${ImPlot.isPlotSelected()}")
            ImGui.text("getPlotMousePos: (${fmt(mouse[0], 2)}, ${fmt(mouse[1], 2)})")
            ImGui.text("getPlotLimits: [${fmt(limits[0], 2)}, ${fmt(limits[1], 2)}, ${fmt(limits[2], 2)}, ${fmt(limits[3], 2)}]")
            val pixels = ImPlot.plotToPixels(mouse[0], mouse[1])
            ImGui.text("plotToPixels: (${pixels.x.toInt()}, ${pixels.y.toInt()})")
            val plotPos = ImPlot.getPlotPos()
            val plotSize = ImPlot.getPlotSize()
            ImGui.text("getPlotPos: (${plotPos.x.toInt()}, ${plotPos.y.toInt()}) getPlotSize: (${plotSize.x.toInt()}, ${plotSize.y.toInt()})")
            ImPlot.plotText("plot text", 0.5, 0.5)
        }

        ImGui.separatorText("Box selection")
        plotWithAxes("Drag a rectangle to select", 180f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("line", sinData)
            ImPlot.plotScatter("points", scatterXs, scatterYs)
            if (ImPlot.isPlotSelected()) {
                val sel = ImPlot.getPlotSelection()
                ImGui.text("selection: [${fmt(sel[0], 2)}, ${fmt(sel[1], 2)}, ${fmt(sel[2], 2)}, ${fmt(sel[3], 2)}]")
            } else {
                ImGui.textDisabled("no selection")
            }
        }

        ImGui.separatorText("Draw list & clip rect")
        plotWithAxes("PushPlotClipRect", 160f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.pushPlotClipRect(20f)
            ImPlot.plotLine("inside clip", sinData)
            ImPlot.popPlotClipRect()
            ImPlot.plotLine("outside clip", cosData)
        }
    }

    private fun drawPlotLegendDragDrop() {
        ImGui.separatorText("Legend")
        plotWithAxes("Legend options", 200f, null, null, ImPlotFlags.CROSSHAIRS) {
            ImPlot.setupLegend(ImPlotLocation.NORTH_EAST)
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("line 1", sinData, spec = ImPlotSpec(lineColor = ImVec4(1f, 0.2f, 0.2f, 1f)))
            ImPlot.plotLine("line 2", cosData, spec = ImPlotSpec(lineColor = ImVec4(0.2f, 1f, 0.2f, 1f)))
            ImPlot.plotLine("line 3", phaseData, spec = ImPlotSpec(lineColor = ImVec4(0.2f, 0.4f, 1f, 1f)))
            ImPlot.plotDummy("dummy")
        }

        ImGui.separatorText("Legend popup")
        plotWithAxes("Right-click a legend entry", 160f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("popup line", sinData)
            if (ImPlot.beginLegendPopup("popup line")) {
                ImGui.checkbox("custom option", plotStyleCheck)
                ImPlot.endLegendPopup()
            }
        }

        ImGui.separatorText("Drag & drop between plots")
        ImGui.text("Drag from one plot into the other.")
        plotWithAxes("Source plot", 150f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotDummy("draggable item", ImPlotSpec(marker = ImPlotMarker.CIRCLE, markerSize = 8f))
            if (ImPlot.beginDragDropSourceItem("draggable item")) {
                ImGui.setDragDropPayload("IMPLOT_DND", "plot payload".encodeToByteArray())
                ImGui.text("dragging")
                ImPlot.endDragDropSource()
            }
        }
        plotWithAxes("Target plot", 150f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("target line", sinData)
            if (ImPlot.beginDragDropTargetPlot()) {
                val payload = ImGui.acceptDragDropPayload("IMPLOT_DND")
                if (payload != null) {
                    dropCount[0]++
                    dropPayload = payload.decodeToString()
                }
                ImPlot.endDragDropTarget()
            }
        }
        ImGui.text("Drops into target: ${dropCount[0]}${if (dropPayload.isNotEmpty()) " (\"$dropPayload\")" else ""}")

        ImGui.separatorText("Mouse text")
        plotWithAxes("Custom mouse text location", 150f, null, null) {
            ImPlot.setupMouseText(ImPlotLocation.SOUTH_WEST)
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("line", sinData)
        }

        ImGui.separatorText("SetNext axes limits")
        plotWithAxes("setNextAxesLimits", 150f, null, null) {
            ImPlot.setNextAxesLimits(0.1, 0.9, 0.1, 0.9, ImPlotCond.ONCE)
            ImPlot.plotLine("line", sinData)
        }
    }

    private fun drawPlotStyle() {
        ImGui.separatorText("Style selectors")
        ImPlot.showStyleSelector("Plot style")
        ImPlot.showColormapSelector("Colormap")
        ImPlot.showInputMapSelector("Input map")
        if (ImGui.button("Style editor (window)")) {
            ImPlot.showStyleEditor()
        }
        if (ImGui.button("Metrics (window)")) {
            showImPlotMetrics[0] = true
        }

        ImGui.separatorText("pushStyleColor")
        ImPlot.pushStyleColor(ImPlotCol.PLOT_BG, ImVec4(0.05f, 0.05f, 0.1f, 1f))
        ImPlot.pushStyleColor(ImPlotCol.FRAME_BG, ImVec4(0.1f, 0.1f, 0.15f, 1f))
        ImPlot.pushStyleColor(ImPlotCol.AXIS_GRID, ImVec4(0.3f, 0.3f, 0.5f, 0.5f))
        plotWithAxes("Styled plot", 160f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("line", sinData)
        }
        ImPlot.popStyleColor(3)

        ImGui.separatorText("pushStyleVar")
        ImPlot.pushStyleVarFloat(ImPlotStyleVar.PLOT_BORDER_SIZE, 3f)
        ImPlot.pushStyleVarVec2(ImPlotStyleVar.PLOT_PADDING, ImVec2(20f, 20f))
        plotWithAxes("Styled by vars", 160f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("line", sinData)
        }
        ImPlot.popStyleVar(2)

        ImGui.separatorText("nextColormapColor")
        plotWithAxes("Next colormap color", 140f, null, null) {
            ImPlot.setupAxesLimits(0.0, 1.0, 0.0, 1.0, ImPlotCond.ONCE)
            ImPlot.plotLine("line", sinData)
            val color = ImPlot.nextColormapColor()
            ImGui.text("nextColormapColor: ${fmt(color.x.toDouble(), 2)} ${fmt(color.y.toDouble(), 2)} ${fmt(color.z.toDouble(), 2)} ${fmt(color.w.toDouble(), 2)}")
            ImGui.sameLine()
            ImGui.colorButton("next color", color)
        }
    }

    // =====================================================================
    // Small helpers
    // =====================================================================

    /** Cheap Gaussian-ish sample in [-3, 3] (sum of three uniforms). */
    private fun gaussian(): Float = (rng.nextFloat() + rng.nextFloat() + rng.nextFloat() - 1.5f) * 2.0f

    /** Portable number formatting (Kotlin/Native has no String.format). */
    private fun fmt(value: Double, digits: Int): String {
        val factor = 10.0.pow(digits)
        val v = (value * factor).roundToLong() / factor
        val text = v.toString()
        val dot = text.indexOf('.')
        return if (dot < 0) "$text.${"0".repeat(digits)}" else text.padEnd(dot + 1 + digits, '0')
    }

    /** Formats an Int as 0xRRGGBBAA. */
    private fun hex8(value: Int): String = "0x" + value.toUInt().toString(16).uppercase().padStart(8, '0')

    private fun checkbox(label: String, value: BooleanArray, index: Int) {
        val single = BooleanArray(1) { value[index] }
        if (ImGui.checkbox(label, single)) value[index] = single[0]
    }
}
