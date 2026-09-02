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

package cn.enaium.imgui.example.docking

import cn.enaium.imgui.ImGui
import cn.enaium.imgui.ImGuiChildFlags
import cn.enaium.imgui.ImGuiConfigFlags
import cn.enaium.imgui.ImGuiDir
import cn.enaium.imgui.ImGuiDockNodeFlags
import cn.enaium.imgui.ImGuiStyleVar
import cn.enaium.imgui.ImGuiWindowFlags
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import cn.enaium.imgui.example.common.SdlRendererApp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * A dockable, IDE-style layout built on the docking bindings:
 *
 * - A full-screen host window owns a [ImGui.dockSpace] and a menu bar.
 * - [ImGui.dockBuilderAddNode]/[ImGui.dockBuilderSplitNode]/
 *   [ImGui.dockBuilderDockWindow]/[ImGui.dockBuilderFinish] lay out four
 *   panels on first run: Hierarchy (left), Inspector (right), Console
 *   (bottom) and Viewport (center).
 * - The View menu toggles each panel (panels can also be closed from their
 *   tab bar), Layout resets the tree and toggles DockSpace flags, and the
 *   panels themselves are freely re-dockable / floatable at runtime.
 *
 * Requires `ImGuiConfigFlags.DOCKING_ENABLE`, set in [runDockingExample].
 *
 * Run with `./gradlew :examples:docking:jvmRun` (JVM) or the per-target
 * native binaries; pass `--frames N` / `IMGUI_KMP_FRAMES=N` to exit after N
 * frames (headless CI runs).
 */
fun runDockingExample(frames: Int = Int.MAX_VALUE) {
    var demo: DockingDemo? = null
    SdlRendererApp.run(
        title = "imgui-kmp docking example",
        frames = frames,
        init = {
            val io = ImGui.getIO()
            io.configFlags = io.configFlags or ImGuiConfigFlags.DOCKING_ENABLE
            demo = DockingDemo()
        },
        draw = { frame -> demo?.draw(frame) },
        close = { demo?.close() },
    )
}

private class DockingDemo {

    // ---- panels ----
    private val showHierarchy = BooleanArray(1) { true }
    private val showViewport = BooleanArray(1) { true }
    private val showInspector = BooleanArray(1) { true }
    private val showConsole = BooleanArray(1) { true }
    private val showDemoWindow = BooleanArray(1)
    private val showMetrics = BooleanArray(1)
    private val showStyleEditor = BooleanArray(1)

    // ---- dock layout state ----
    private var layoutBuilt = false
    private var resetRequested = false
    private var centralNodeId = 0

    // DockSpace flags, toggled from the Layout menu.
    private var optPassthruCentral = BooleanArray(1) { false }
    private var optNoDockingSplit = BooleanArray(1) { false }
    private var optNoResize = BooleanArray(1) { false }
    private var optAutoHideTabBar = BooleanArray(1) { false }
    private var optNoUndocking = BooleanArray(1) { false }

    // ---- demo content ----
    private val sceneObjects = arrayOf("Camera", "Cube", "Sphere", "Light", "Ground")
    private val selected = IntArray(1) { 1 }
    private val objectColor = floatArrayOf(0.6f, 0.7f, 0.9f, 1f)
    private val rotation = floatArrayOf(0f, 0f, 0f)
    private val scale = floatArrayOf(1f, 1f, 1f)
    private val animate = BooleanArray(1) { true }

    private val logLines = ArrayList<Pair<String, Int>>()
    private val rng = Random(42)
    private var lastLogFrame = -1

    init {
        logLines += "[imgui-kmp] docking example started" to 0
        logLines += "drag tab bars to re-dock, drag windows out to float" to 1
    }

    fun draw(frame: Int) {
        // Panels are created once and keep their dock id for the session; the
        // layout tree is rebuilt when requested (first frame / menu action).
        if (resetRequested) {
            ImGui.dockBuilderRemoveNode(ImGui.getID("MainDockSpace"))
            layoutBuilt = false
            resetRequested = false
        }
        if (!layoutBuilt) {
            buildDefaultLayout()
            layoutBuilt = true
        }

        drawHostWindow()

        if (showHierarchy[0]) drawHierarchy()
        if (showViewport[0]) drawViewport(frame)
        if (showInspector[0]) drawInspector()
        if (showConsole[0]) drawConsole(frame)

        if (showDemoWindow[0]) ImGui.showDemoWindow(showDemoWindow)
        if (showMetrics[0]) ImGui.showMetricsWindow(showMetrics)
        if (showStyleEditor[0]) {
            ImGui.begin("Style Editor", showStyleEditor)
            ImGui.showStyleEditor()
            ImGui.end()
        }
    }

    fun close() = Unit

    private fun dockFlags(): Int {
        var flags = ImGuiDockNodeFlags.NONE
        if (optPassthruCentral[0]) flags = flags or ImGuiDockNodeFlags.PASSTHRU_CENTRAL_NODE
        if (optNoDockingSplit[0]) flags = flags or ImGuiDockNodeFlags.NO_DOCKING_SPLIT
        if (optNoResize[0]) flags = flags or ImGuiDockNodeFlags.NO_RESIZE
        if (optAutoHideTabBar[0]) flags = flags or ImGuiDockNodeFlags.AUTO_HIDE_TAB_BAR
        if (optNoUndocking[0]) flags = flags or ImGuiDockNodeFlags.NO_UNDOCKING
        return flags
    }

    /**
     * Builds the default tree with the DockBuilder API:
     *
     * ```
     *              root
     *      ┌───────┴───────┐
     *   Hierarchy        rest
     *              ┌──────┴───────┐
     *           Inspector      rest
     *                    ┌───────┴───────┐
     *                  Viewport      Console
     * ```
     */
    private fun buildDefaultLayout() {
        val root = ImGui.getID("MainDockSpace")
        ImGui.dockBuilderRemoveNode(root)
        ImGui.dockBuilderAddNode(root, ImGuiDockNodeFlags.DOCK_SPACE)

        var rest = root
        val (hierarchy, afterLeft) = ImGui.dockBuilderSplitNode(rest, ImGuiDir.LEFT, 0.22f)
        rest = afterLeft
        val (inspector, afterRight) = ImGui.dockBuilderSplitNode(rest, ImGuiDir.RIGHT, 0.25f)
        rest = afterRight
        val (console, afterBottom) = ImGui.dockBuilderSplitNode(rest, ImGuiDir.DOWN, 0.32f)
        val viewport = afterBottom
        centralNodeId = viewport

        ImGui.dockBuilderDockWindow("Hierarchy", hierarchy)
        ImGui.dockBuilderDockWindow("Inspector", inspector)
        ImGui.dockBuilderDockWindow("Viewport", viewport)
        ImGui.dockBuilderDockWindow("Console", console)
        ImGui.dockBuilderFinish(root)
    }

    private fun drawHostWindow() {
        val io = ImGui.getIO()
        ImGui.pushStyleVarVec2(ImGuiStyleVar.WINDOW_PADDING, ImVec2(0f, 0f))
        ImGui.setNextWindowPos(ImVec2(0f, 0f))
        ImGui.setNextWindowSize(io.displaySize)
        val hostFlags = ImGuiWindowFlags.MENU_BAR or
            ImGuiWindowFlags.NO_DOCKING or
            ImGuiWindowFlags.NO_TITLE_BAR or
            ImGuiWindowFlags.NO_RESIZE or
            ImGuiWindowFlags.NO_MOVE or
            ImGuiWindowFlags.NO_COLLAPSE or
            ImGuiWindowFlags.NO_BRING_TO_FRONT_ON_FOCUS or
            ImGuiWindowFlags.NO_NAV_FOCUS
        ImGui.begin("DockSpaceHost", null, hostFlags)

        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("View")) {
                if (ImGui.menuItem("Hierarchy", selected = showHierarchy[0])) showHierarchy[0] = !showHierarchy[0]
                if (ImGui.menuItem("Viewport", selected = showViewport[0])) showViewport[0] = !showViewport[0]
                if (ImGui.menuItem("Inspector", selected = showInspector[0])) showInspector[0] = !showInspector[0]
                if (ImGui.menuItem("Console", selected = showConsole[0])) showConsole[0] = !showConsole[0]
                ImGui.separator()
                if (ImGui.menuItem("ImGui Demo", selected = showDemoWindow[0])) showDemoWindow[0] = !showDemoWindow[0]
                if (ImGui.menuItem("Metrics", selected = showMetrics[0])) showMetrics[0] = !showMetrics[0]
                if (ImGui.menuItem("Style Editor", selected = showStyleEditor[0])) showStyleEditor[0] = !showStyleEditor[0]
                ImGui.endMenu()
            }
            if (ImGui.beginMenu("Layout")) {
                if (ImGui.menuItem("Reset layout")) resetRequested = true
                ImGui.separator()
                if (ImGui.menuItem("Passthru central node", selected = optPassthruCentral[0])) {
                    optPassthruCentral[0] = !optPassthruCentral[0]
                }
                if (ImGui.menuItem("No docking split", selected = optNoDockingSplit[0])) {
                    optNoDockingSplit[0] = !optNoDockingSplit[0]
                }
                if (ImGui.menuItem("No resize", selected = optNoResize[0])) {
                    optNoResize[0] = !optNoResize[0]
                }
                if (ImGui.menuItem("Auto-hide tab bar", selected = optAutoHideTabBar[0])) {
                    optAutoHideTabBar[0] = !optAutoHideTabBar[0]
                }
                if (ImGui.menuItem("No undocking", selected = optNoUndocking[0])) {
                    optNoUndocking[0] = !optNoUndocking[0]
                }
                ImGui.endMenu()
            }
            ImGui.endMenuBar()
        }

        // The DockSpace id must match the one used by the DockBuilder root.
        ImGui.dockSpace(ImGui.getID("MainDockSpace"), ImVec2(0f, 0f), dockFlags())
        ImGui.end()
        ImGui.popStyleVar()
    }

    // ---- panels ----

    private fun drawHierarchy() {
        ImGui.begin("Hierarchy", showHierarchy)
        ImGui.textUnformatted("Scene objects")
        ImGui.separator()
        for (i in sceneObjects.indices) {
            if (ImGui.selectable(sceneObjects[i], selected[0] == i)) {
                selected[0] = i
            }
        }
        ImGui.end()
    }

    private fun drawViewport(frame: Int) {
        ImGui.begin("Viewport", showViewport)
        if (ImGui.button("Play")) {
            logLines += "[viewport] play" to 0
        }
        ImGui.sameLine()
        if (ImGui.button("Stop")) {
            logLines += "[viewport] stop" to 0
        }
        ImGui.sameLine()
        ImGui.checkbox("animate", animate)
        ImGui.separator()

        // A child region standing in for the 3D view: draw a small
        // checkerboard grid and a spinning marker using the window draw list.
        if (ImGui.beginChild("ViewportCanvas", ImVec2(0f, 0f), ImGuiChildFlags.NONE)) {
            val p0 = ImGui.getCursorScreenPos()
            val size = ImGui.getWindowSize()
            val dl = ImGui.getWindowDrawList()
            val cell = 32f
            var y = p0.y
            var row = 0
            while (y < p0.y + size.y) {
                var x = p0.x
                var col = 0
                while (x < p0.x + size.x) {
                    val dark = ((row + col) % 2) == 0
                    dl.DrawRectFilled(
                        ImVec2(x, y),
                        ImVec2(minOf(x + cell, p0.x + size.x), minOf(y + cell, p0.y + size.y)),
                        if (dark) 0xFF23232B.toInt() else 0xFF2A2A33.toInt(),
                    )
                    x += cell
                    col++
                }
                y += cell
                row++
            }
            // A spinning marker in the center of the canvas.
            val cx = p0.x + size.x / 2f
            val cy = p0.y + size.y / 2f
            val t = ImGui.getTime().toFloat()
            val angle = t * 2f
            val radius = if (animate[0]) 40f + 20f * sin(t * 1.5f) else 40f
            val accent = if (animate[0]) 0xFF4FC3F7.toInt() else 0xFF9E9E9E.toInt()
            val points = Array(6) { i ->
                val a = angle + i * (PI.toFloat() * 2f / 6f)
                ImVec2(cx + radius * cos(a), cy + radius * sin(a))
            }
            dl.DrawPolyline(points, accent, closed = true, thickness = 3f)
            dl.DrawCircleFilled(ImVec2(cx, cy), 8f, accent)
            ImGui.textUnformatted("frame $frame")
        }
        ImGui.endChild()
        ImGui.end()
    }

    private fun drawInspector() {
        ImGui.begin("Inspector", showInspector)
        ImGui.textUnformatted(sceneObjects[selected[0]])
        ImGui.separator()
        ImGui.colorEdit4("Color", objectColor)
        ImGui.dragFloat3("Rotation", rotation, 0.5f, -180f, 180f)
        ImGui.dragFloat3("Scale", scale, 0.01f, 0.1f, 10f)
        ImGui.checkbox("Animate", animate)
        ImGui.separator()
        ImGui.textWrapped("This panel and the others can be dragged by their tab bar to any edge, or out of the window to float.")
        ImGui.end()
    }

    private fun drawConsole(frame: Int) {
        if (frame != lastLogFrame && rng.nextInt(60) == 0) {
            lastLogFrame = frame
            val level = rng.nextInt(3)
            logLines += when (level) {
                0 -> "[info] tick ${ImGui.getFrameCount()}" to 0
                1 -> "[warn] random warning" to 1
                else -> "[error] simulated error" to 2
            }
        }
        while (logLines.size > 200) logLines.removeAt(0)

        ImGui.begin("Console", showConsole)
        if (ImGui.button("Clear")) logLines.clear()
        ImGui.separator()
        if (ImGui.beginChild("ConsoleScroll", ImVec2(0f, 0f), ImGuiChildFlags.BORDERS)) {
            for ((text, level) in logLines) {
                val color = when (level) {
                    1 -> ImVec4(0.95f, 0.8f, 0.3f, 1f)
                    2 -> ImVec4(0.9f, 0.35f, 0.35f, 1f)
                    else -> ImVec4(0.7f, 0.85f, 0.7f, 1f)
                }
                ImGui.textColored(color, text)
            }
            if (ImGui.getScrollY() >= ImGui.getScrollMaxY() - 4f) {
                ImGui.setScrollHereY(1f)
            }
        }
        ImGui.endChild()
        ImGui.end()
    }
}
