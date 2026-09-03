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
import cn.enaium.imgui.extensions.implot.ImPlotSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * JVM-specific test that runs several frames and verifies that the JNI
 * bridge produces consistent draw data (fonts included).
 */
class ImGuiJvmTest {

    @Test
    fun runFramesWithDrawData() {
        val context = ImGui.createContext()
        try {
            val io = ImGui.getIO()
            io.displaySize = ImVec2(800f, 600f)
            io.displayFramebufferScale = ImVec2(1f, 1f)
            io.deltaTime = 1f / 60f
            io.configFlags = ImGuiConfigFlags.NAV_ENABLE_KEYBOARD
            io.backendFlags = ImGuiBackendFlags.RENDERER_HAS_TEXTURES

            ImPlot.setImGuiContext(context)
            val plotContext = ImPlot.createContext()

            repeat(30) { frame ->
                ImGui.newFrame()
                if (ImGui.begin("Frame #$frame", flags = ImGuiWindowFlags.NO_SAVED_SETTINGS)) {
                    ImGui.text("hello jvm ${ImGui.getVersion()}")
                    ImGui.button("press me", ImVec2(120f, 0f))
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
                    assertTrue(list.idxCount >= 0)
                    if (list.vtxCount > 0) {
                        val verts = list.copyVtx(0, list.vtxCount)
                        assertEquals(list.vtxCount, verts.positions.size / 2)
                        assertEquals(list.vtxCount, verts.uvs.size / 2)
                        assertEquals(list.vtxCount, verts.colors.size)
                    }
                }
            }
            ImPlot.destroyContext(plotContext)
        } finally {
            ImGui.destroyContext(context)
        }
    }

    @Test
    fun fontAtlasProducesPixels() {
        val context = ImGui.createContext()
        try {
            val fonts = ImGui.getIO().fonts
            assertTrue(fonts.addFontDefault() != null)
            assertTrue(fonts.build())
            val tex = fonts.getTexDataAsRGBA32()
            assertTrue(tex.width > 0 && tex.height > 0)
            assertTrue(tex.pixels.isNotEmpty())
            assertTrue(tex.pixels.all { it == 0.toByte() } == false)
        } finally {
            ImGui.destroyContext(context)
        }
    }

    /**
     * Installs platform clipboard callbacks and verifies the full chain:
     * ImGui::SetClipboardText -> JNI -> ClipboardJvmBridge -> Kotlin setter,
     * and Kotlin getter -> ClipboardJvmBridge -> JNI -> ImGui::GetClipboardText.
     * This is what InputText paste and the ColorTextEdit clipboard ops use.
     */
    @Test
    fun clipboardRoundTripsThroughInstalledCallbacks() {
        val context = ImGui.createContext()
        try {
            val setTexts = ArrayList<String>()
            var getText: (() -> String?)? = null

            ImGui.setClipboardFunctions(
                setText = { text -> setTexts.add(text) },
                getText = { getText?.invoke() },
            )
            getText = { "clipboard-content" }

            ImGui.setClipboardText("hello clipboard")
            assertEquals(listOf("hello clipboard"), setTexts)

            assertEquals("clipboard-content", ImGui.getClipboardText())

            // Uninstalling must restore the no-op behavior.
            ImGui.setClipboardFunctions(null, null)
            ImGui.setClipboardText("ignored")
            assertEquals(listOf("hello clipboard"), setTexts, "setter should be detached")
            assertEquals(null, ImGui.getClipboardText())
        } finally {
            ImGui.destroyContext(context)
        }
    }
}
