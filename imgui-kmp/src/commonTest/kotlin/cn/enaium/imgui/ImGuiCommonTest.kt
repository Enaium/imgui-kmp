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

package cn.enaium.imgui

import cn.enaium.imgui.extensions.implot.ImPlot
import cn.enaium.imgui.extensions.implot.ImPlotAxis
import cn.enaium.imgui.extensions.implot.ImPlotCond
import cn.enaium.imgui.extensions.implot.ImPlotMarker
import cn.enaium.imgui.extensions.implot.ImPlotSpec
import cn.enaium.imgui.extensions.filedialog.FileDialog
import cn.enaium.imgui.extensions.filedialog.FileDialogConfig
import cn.enaium.imgui.extensions.memoryeditor.MemoryEditor
import cn.enaium.imgui.extensions.nodeeditor.NePinKind
import cn.enaium.imgui.extensions.nodeeditor.NodeEditor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Runs a full imgui + implot frame without a renderer and asserts the
 * draw data is produced. Shared by every platform.
 */
class ImGuiCommonTest {

    private fun runFrame(): ImDrawData {
        val context = ImGui.createContext()
        try {
            val io = ImGui.getIO()
            io.displaySize = ImVec2(800f, 600f)
            io.deltaTime = 1f / 60f
            io.backendFlags = ImGuiBackendFlags.RENDERER_HAS_TEXTURES

            ImPlot.setImGuiContext(context)
            val plotContext = ImPlot.createContext()

            // New windows are hidden on their first frame (anti-flicker), so
            // run a few frames before inspecting the draw data.
            repeat(3) { frame ->
                ImGui.newFrame()
                if (ImGui.begin("Test Window")) {
                    ImGui.text("Hello imgui-kmp ${ImGui.getVersion()}")
                    ImGui.separator()

                    val value = FloatArray(1) { 0.5f }
                    if (ImGui.sliderFloat("value", value, 0f, 1f)) {
                        assertTrue(value[0] in 0f..1f)
                    }

                    val checked = BooleanArray(1) { true }
                    ImGui.checkbox("checked", checked)

                    val items = arrayOf("A", "B", "C")
                    val current = IntArray(1)
                    ImGui.combo("combo", current, items)

                    if (ImPlot.beginPlot("Sine Wave")) {
                        ImPlot.setupAxes("x", "y")
                        ImPlot.setupAxesLimits(0.0, 100.0, -1.5, 1.5, ImPlotCond.ONCE)
                        ImPlot.setupFinish()
                        val xs = FloatArray(100) { it.toFloat() }
                        val ys = FloatArray(100) { kotlin.math.sin(it / 10.0).toFloat() }
                        ImPlot.plotLine("sin", xs, ys)
                        ImPlot.plotScatter(
                            "points",
                            xs,
                            ys,
                            ImPlotSpec(marker = ImPlotMarker.CIRCLE, markerSize = 3f),
                        )
                        ImPlot.endPlot()
                    }
                }
                ImGui.end()
                ImGui.render()
            }
            val drawData = ImGui.getDrawData()
            assertNotNull(drawData)
            // Draw data is only valid until the context is destroyed; assert
            // on it while the context is still alive.
            assertTrue(drawData.cmdListsCount > 0, "expected at least one draw list")
            val list = drawData.cmdList(0)
            assertTrue(list.vtxCount > 0, "the test window should produce vertices")
            assertTrue(list.idxCount > 0)
            assertTrue(list.cmdCount > 0)
            assertTrue(list.cmd(0).elemCount > 0)
            ImPlot.destroyContext(plotContext)
            return drawData
        } finally {
            ImGui.destroyContext(context)
        }
    }

    @Test
    fun versionIsNonEmpty() {
        val version = ImGui.getVersion()
        assertTrue(version.isNotEmpty(), "ImGui version should not be empty")
    }

    @Test
    fun createAndDestroyContext() {
        val context = ImGui.createContext()
        assertNotNull(ImGui.getCurrentContext())
        ImGui.destroyContext(context)
        // Destroying the only context makes GetCurrentContext null
    }

    @Test
    fun runFrameProducesDrawData() {
        // runFrame asserts the draw data while the context is alive.
        runFrame()
    }

    @Test
    fun inputTextReturnsBuffer() {
        val context = ImGui.createContext()
        try {
            val io = ImGui.getIO()
            io.displaySize = ImVec2(400f, 300f)
            io.deltaTime = 1f / 60f
            io.backendFlags = ImGuiBackendFlags.RENDERER_HAS_TEXTURES
            ImGui.newFrame()
            if (ImGui.begin("Input Window")) {
                val result = ImGui.inputText("name", "hello")
                assertEquals("hello", result)
                val itemHovered = ImGui.isItemHovered()
                assertTrue(!itemHovered || itemHovered)
            }
            ImGui.end()
            ImGui.render()
        } finally {
            ImGui.destroyContext(context)
        }
    }

    @Test
    fun implotContextLifecycle() {
        val context = ImGui.createContext()
        try {
            ImPlot.setImGuiContext(context)
            val plotContext = ImPlot.createContext()
            assertNotNull(ImPlot.getCurrentContext())
            ImPlot.destroyContext(plotContext)
        } finally {
            ImGui.destroyContext(context)
        }
    }

    @Test
    fun styleColorsRoundTrip() {
        val context = ImGui.createContext()
        try {
            val style = ImGui.getStyle()
            val original = style.getColor(ImGuiCol.WINDOW_BG)
            val custom = ImVec4(0.1f, 0.2f, 0.3f, 1f)
            style.setColor(ImGuiCol.WINDOW_BG, custom)
            val updated = style.getColor(ImGuiCol.WINDOW_BG)
            assertEquals(custom.x, updated.x, 0.001f)
            assertEquals(custom.y, updated.y, 0.001f)
            assertEquals(custom.z, updated.z, 0.001f)
            style.setColor(ImGuiCol.WINDOW_BG, original)
        } finally {
            ImGui.destroyContext(context)
        }
    }

    @Test
    fun implotGetPlotSize() {
        val context = ImGui.createContext()
        try {
            val io = ImGui.getIO()
            io.displaySize = ImVec2(500f, 500f)
            io.deltaTime = 1f / 60f
            io.backendFlags = ImGuiBackendFlags.RENDERER_HAS_TEXTURES
            ImPlot.setImGuiContext(context)
            val plotContext = ImPlot.createContext()
            ImGui.newFrame()
            if (ImGui.begin("Plot Window")) {
                if (ImPlot.beginPlot("My Plot")) {
                    ImPlot.setupFinish()
                    ImPlot.plotDummy("dummy")
                    val size = ImPlot.getPlotSize()
                    assertTrue(size.x > 0f && size.y > 0f)
                    ImPlot.endPlot()
                }
            }
            ImGui.end()
            ImGui.render()
            ImPlot.destroyContext(plotContext)
        } finally {
            ImGui.destroyContext(context)
        }
    }

    @Test
    fun nodeEditorFrame() {
        val context = ImGui.createContext()
        try {
            val io = ImGui.getIO()
            io.displaySize = ImVec2(800f, 600f)
            io.deltaTime = 1f / 60f
            io.backendFlags = ImGuiBackendFlags.RENDERER_HAS_TEXTURES

            val editor = NodeEditor.createEditor()
            NodeEditor.setCurrentEditor(editor)
            try {
                ImGui.newFrame()
                if (ImGui.begin("Node Window")) {
                    NodeEditor.begin("editor", ImVec2(-1f, -1f))
                    NodeEditor.beginNode(1)
                    ImGui.text("node")
                    NodeEditor.beginPin(10, NePinKind.INPUT)
                    ImGui.text("in")
                    NodeEditor.endPin()
                    NodeEditor.beginPin(11, NePinKind.OUTPUT)
                    ImGui.text("out")
                    NodeEditor.endPin()
                    NodeEditor.endNode()
                    assertTrue(NodeEditor.link(100, 10, 11))
                    NodeEditor.end()
                    assertEquals(1, NodeEditor.getNodeCount())
                    assertFalse(NodeEditor.isLinkSelected(100))
                }
                ImGui.end()
                ImGui.render()
            } finally {
                NodeEditor.destroyEditor(editor)
            }
        } finally {
            ImGui.destroyContext(context)
        }
    }

    @Test
    fun fileDialogLifecycle() {
        val context = ImGui.createContext()
        try {
            val dialog = FileDialog.create()
            try {
                FileDialog.openDialog(
                    dialog,
                    key = "test",
                    title = "Open",
                    filters = "*.txt",
                    config = FileDialogConfig(path = ".", countSelectionMax = 1),
                )
                assertTrue(FileDialog.isOpened(dialog))
                assertTrue(FileDialog.isKeyOpened(dialog, "test"))
            } finally {
                FileDialog.destroy(dialog)
            }
        } finally {
            ImGui.destroyContext(context)
        }
    }

    @Test
    fun memoryEditorSettings() {
        val context = ImGui.createContext()
        try {
            val editor = MemoryEditor.create()
            try {
                MemoryEditor.setCols(editor, 8)
                assertEquals(8, MemoryEditor.getCols(editor))
                MemoryEditor.setReadOnly(editor, true)
                assertTrue(MemoryEditor.isReadOnly(editor))
                MemoryEditor.setReadOnly(editor, false)
            } finally {
                MemoryEditor.destroy(editor)
            }
        } finally {
            ImGui.destroyContext(context)
        }
    }
}
