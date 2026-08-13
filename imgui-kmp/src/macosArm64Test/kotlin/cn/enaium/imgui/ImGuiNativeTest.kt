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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Native (cinterop) test: renders a few frames and reads back the
 * draw data through the cinterop bindings.
 */
class ImGuiNativeTest {

    @Test
    fun runFramesWithDrawData() {
        val context = ImGui.createContext()
        try {
            val io = ImGui.getIO()
            io.displaySize = ImVec2(800f, 600f)
            io.deltaTime = 1f / 60f
            io.backendFlags = ImGuiBackendFlags.RENDERER_HAS_TEXTURES

            ImPlot.setImGuiContext(context)
            val plotContext = ImPlot.createContext()

            repeat(10) { frame ->
                ImGui.newFrame()
                if (ImGui.begin("Frame #$frame", flags = ImGuiWindowFlags.NO_SAVED_SETTINGS)) {
                    ImGui.text("hello native ${ImGui.getVersion()}")
                    val value = FloatArray(1) { 0.25f }
                    ImGui.sliderFloat("slider", value, 0f, 1f)
                    if (ImPlot.beginPlot("plot")) {
                        ImPlot.setupFinish()
                        ImPlot.plotDummy("legend entry")
                        ImPlot.endPlot()
                    }
                }
                ImGui.end()
                ImGui.render()

                val drawData = ImGui.getDrawData()
                if (drawData.cmdListsCount > 0) {
                    val list = drawData.cmdList(0)
                    assertTrue(list.vtxCount >= 0)
                    if (list.vtxCount > 0) {
                        val verts = list.copyVtx(0, list.vtxCount)
                        assertEquals(list.vtxCount, verts.positions.size / 2)
                        assertEquals(list.vtxCount, verts.uvs.size / 2)
                        assertEquals(list.vtxCount, verts.colors.size)
                        val idx = list.copyIdx(0, list.idxCount)
                        assertEquals(list.idxCount, idx.size)
                    }
                }
            }
            ImPlot.destroyContext(plotContext)
        } finally {
            ImGui.destroyContext(context)
        }
    }
}
