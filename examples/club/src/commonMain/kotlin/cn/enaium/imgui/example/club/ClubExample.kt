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

package cn.enaium.imgui.example.club

import cn.enaium.imgui.ImFontConfig
import cn.enaium.imgui.ImGui
import cn.enaium.imgui.ImGuiCol
import cn.enaium.imgui.ImGuiCond
import cn.enaium.imgui.ImGuiWindowFlags
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import cn.enaium.imgui.backends.sdl.ImGuiSdlBackend
import cn.enaium.imgui.backends.sdl.ImGuiSdlRendererBackend
import cn.enaium.imgui.extensions.mcc.MultiContextCompositor
import cn.enaium.imgui.extensions.memoryeditor.MemoryEditor
import cn.enaium.sdl.SDL
import cn.enaium.sdl.SDLColor
import cn.enaium.sdl.SDLInitFlags
import cn.enaium.sdl.SDLWindowFlags
import kotlin.random.Random

/**
 * The imgui_club extensions, demonstrated together:
 *
 * - [MemoryEditor] — a hex editor editing a live byte buffer.
 * - [MultiContextCompositor] — a second, always-on-top ImGui context rendered
 *   over the main one in the same window, with the compositor managing input
 *   routing between the two.
 *
 * The threaded-rendering (ImTextureQueue) extension is not demoed here: its
 * texture-update callbacks must live on the native/GPU side, so it cannot be
 * driven from Kotlin.
 *
 * Because the compositor drives two ImGui contexts, this example owns its own
 * frame loop instead of using [cn.enaium.imgui.example.common.SdlRendererApp].
 * Run with `./gradlew :examples:club:jvmRun` (JVM) or the per-target native
 * binaries; pass `--frames N` / `IMGUI_KMP_FRAMES=N` to exit after N frames.
 */
fun runClubExample(frames: Int = Int.MAX_VALUE) {
    SDL.setMainReady()
    // Fall back to the dummy video driver (headless CI runners, SSH sessions).
    if (!SDL.init(SDLInitFlags.VIDEO or SDLInitFlags.EVENTS)) {
        SDL.setHint("SDL_VIDEO_DRIVER", "dummy")
        if (SDL.init(SDLInitFlags.VIDEO or SDLInitFlags.EVENTS)) {
            println("video init fell back to the dummy driver — running headless")
        } else {
            error("SDL_Init failed: ${SDL.error()}")
        }
    }
    println("SDL ${SDL.version()} (${SDL.revision()})")
    println("Video driver: ${SDL.getCurrentVideoDriver()}")

    SDL.createWindow(
        title = "imgui-kmp club example",
        width = 1280,
        height = 800,
        flags = SDLWindowFlags.RESIZABLE or SDLWindowFlags.HIGH_PIXEL_DENSITY,
    ).use { window ->
        SDL.createRenderer(window).use { renderer ->
            // Two ImGui contexts: the main demo UI and an overlay context that
            // the compositor stacks on top of it.
            val mainContext = ImGui.createContext()
            val overlayContext = ImGui.createContext()
            try {
                val backend = ImGuiSdlRendererBackend(renderer)

                // Each context needs its own platform backend (it captures the
                // context's IO) and its own font atlas texture.
                ImGui.setCurrentContext(mainContext)
                val mainImgui = ImGuiSdlBackend(window)
                mainImgui.init()
                val mainFontTexture = uploadFontTexture(
                    backend,
                    maxOf(mainImgui.framebufferScale.x, mainImgui.framebufferScale.y, 1f),
                )

                ImGui.setCurrentContext(overlayContext)
                val overlayImgui = ImGuiSdlBackend(window)
                overlayImgui.init()
                val overlayFontTexture = uploadFontTexture(
                    backend,
                    maxOf(overlayImgui.framebufferScale.x, overlayImgui.framebufferScale.y, 1f),
                )

                ImGui.setCurrentContext(mainContext)

                val compositor = MultiContextCompositor.create()
                MultiContextCompositor.addContext(compositor, mainContext)
                MultiContextCompositor.addContext(compositor, overlayContext)

                val memoryEditor = MemoryEditor.create()
                var data = ByteArray(128) { (it * 7).toByte() }
                val readOnly = BooleanArray(1)
                val cols = IntArray(1) { 16 }
                val midCols = IntArray(1) { 4 }
                val showHexII = BooleanArray(1)
                val showAscii = BooleanArray(1) { true }
                val upperCaseHex = BooleanArray(1) { true }
                val showDataPreview = BooleanArray(1)
                val greyOutZeroes = BooleanArray(1) { true }
                val showMccDebug = BooleanArray(1)
                val overlaySlider = FloatArray(1) { 0.5f }
                val overlayCheck = BooleanArray(1) { true }
                // Cross-context drag & drop state: which swatch the overlay
                // context received, and which note the main context received.
                val overlayColor = IntArray(1) { -1 }
                val mainNote = arrayOfNulls<String>(1)

                var running = true
                var frameCount = 0
                while (running && frameCount < frames) {
                    // ---- events (fed to both contexts; the compositor routes) ----
                    while (true) {
                        val event = SDL.pollEvent() ?: break
                        when (event) {
                            is cn.enaium.sdl.SDLEvent.Quit -> running = false
                            is cn.enaium.sdl.SDLEvent.Window ->
                                if (event.type == cn.enaium.sdl.SDLWindowEventType.CLOSE_REQUESTED) running = false
                            else -> {
                                mainImgui.processEvent(event)
                                overlayImgui.processEvent(event)
                            }
                        }
                    }
                    // ---- compositor frame protocol ----
                    MultiContextCompositor.preNewFrameUpdateAll(compositor)

                    ImGui.setCurrentContext(mainContext)
                    mainImgui.newFrame()
                    MultiContextCompositor.postNewFrameUpdateOne(compositor, mainContext)
                    drawMain(memoryEditor, data, readOnly, cols, midCols, showHexII, showAscii,
                        upperCaseHex, showDataPreview, greyOutZeroes, showMccDebug, compositor,
                        frameCount, mainNote) { newData ->
                        data = newData
                    }

                    ImGui.setCurrentContext(overlayContext)
                    overlayImgui.newFrame()
                    MultiContextCompositor.postNewFrameUpdateOne(compositor, overlayContext)
                    drawOverlay(overlayFontTexture, overlaySlider, overlayCheck, showMccDebug, compositor,
                        frameCount, overlayColor) {
                        // The overlay context can mutate the main context's data:
                        // shared state across contexts.
                        data = ByteArray(128) { Random.nextInt(256).toByte() }
                    }

                    ImGui.setCurrentContext(mainContext)
                    ImGui.render()
                    ImGui.setCurrentContext(overlayContext)
                    ImGui.render()
                    MultiContextCompositor.postEndFrameUpdateAll(compositor)

                    // ---- render: main context first, overlay on top ----
                    renderer.drawColor = SDLColor(18, 18, 24, 255)
                    renderer.clear()
                    ImGui.setCurrentContext(mainContext)
                    backend.renderDrawData(ImGui.getDrawData())
                    ImGui.setCurrentContext(overlayContext)
                    backend.renderDrawData(ImGui.getDrawData())
                    renderer.present()
                    frameCount++
                }

                memoryEditor.close()
                compositor.close()
                backend.close()
            } finally {
                ImGui.destroyContext(mainContext)
                ImGui.destroyContext(overlayContext)
            }
        }
    }
    SDL.quit()
}

/**
 * Builds the font atlas of the current context and uploads it to the renderer.
 * [density] is the framebuffer scale (1.0 on a standard display, 2.0 Retina):
 * the atlas is baked at `sizePixels * density` physical px so text stays crisp
 * when the backend renders at the framebuffer scale.
 */
private fun uploadFontTexture(backend: ImGuiSdlRendererBackend, density: Float): Long {
    val fonts = ImGui.getIO().fonts
    fonts.addFontDefault(
        ImFontConfig(
            sizePixels = 13f * density,
            rasterizerDensity = density,
        ),
    )
    check(fonts.build()) { "font atlas build failed" }
    val texData = fonts.getTexDataAsRGBA32()
    val textureId = backend.uploadFontTexture(texData.pixels, texData.width, texData.height)
    fonts.setTexID(textureId)
    return textureId
}
/** A draggable color swatch used by the cross-context drag & drop demo. */
private data class Swatch(val index: Int, val color: ImVec4, val name: String)

private val SWATCHES = listOf(
    Swatch(0, ImVec4(0.86f, 0.22f, 0.22f, 1f), "Red"),
    Swatch(1, ImVec4(0.30f, 0.80f, 0.25f, 1f), "Green"),
    Swatch(2, ImVec4(0.25f, 0.55f, 0.95f, 1f), "Blue"),
    Swatch(3, ImVec4(0.95f, 0.75f, 0.15f, 1f), "Yellow"),
)

/** Main context: the memory editor plus info about the compositor setup. */
private fun drawMain(
    memoryEditor: cn.enaium.imgui.extensions.memoryeditor.MemoryEditorInstance,
    data: ByteArray,
    readOnly: BooleanArray,
    cols: IntArray,
    midCols: IntArray,
    showHexII: BooleanArray,
    showAscii: BooleanArray,
    upperCaseHex: BooleanArray,
    showDataPreview: BooleanArray,
    greyOutZeroes: BooleanArray,
    showMccDebug: BooleanArray,
    compositor: cn.enaium.imgui.extensions.mcc.MccInstance,
    frameCount: Int,
    mainNote: Array<String?>,
    onRandomize: (ByteArray) -> Unit,
) {
    // On the first frame the window width is not established yet, so
    // textWrapped() wraps at a near-zero width and the window briefly explodes
    // to thousands of pixels tall before settling. Give it a sane initial size.
    ImGui.setNextWindowSize(ImVec2(620f, 720f), ImGuiCond.ONCE)
    ImGui.begin("imgui_club demo")

    // ---- Cross-context drag & drop (kept above the tall memory editor so the
    // sources stay visible on small displays) ----
    ImGui.separatorText("Cross-context Drag & Drop")
    ImGui.textWrapped(
        "Drag a swatch onto the overlay window: it lives in a different ImGui " +
            "context, and the compositor replicates the drag payload across " +
            "contexts so the drop lands there.",
    )
    SWATCHES.forEach { swatch ->
        ImGui.pushId("swatch${swatch.index}")
        ImGui.pushStyleColor(ImGuiCol.BUTTON, swatch.color)
        ImGui.button(swatch.name, ImVec2(64f, 0f))
        ImGui.popStyleColor()
        if (ImGui.beginDragDropSource()) {
            ImGui.setDragDropPayload("COLOR", byteArrayOf(swatch.index.toByte()))
            ImGui.text("Dropping ${swatch.name} on the overlay")
            ImGui.endDragDropSource()
        }
        ImGui.popId()
        ImGui.sameLine()
    }
    ImGui.newLine()
    ImGui.textWrapped(
        "The overlay context can also drag text back here. Drop it on the " +
            "button below.",
    )
    ImGui.button("Drop a note here", ImVec2(160f, 0f))
    if (ImGui.beginDragDropTarget()) {
        ImGui.acceptDragDropPayload("NOTE")?.let { bytes ->
            mainNote[0] = bytes.decodeToString()
        }
        ImGui.endDragDropTarget()
    }
    mainNote[0]?.let { ImGui.text("Received from overlay: \"$it\"") }

    ImGui.separatorText("Memory Editor")
    ImGui.textWrapped(
        "A hex editor from imgui_club, editing the buffer below in place. " +
            "Edit bytes directly, or use the right-click options menu.",
    )

    // The memory editor has its own right-click options menu that toggles the
    // same flags. Mirror the editor state into the controls below (read), draw
    // them, then push the (possibly changed) values back — otherwise the
    // per-frame push would revert whatever the built-in options menu changed.
    readOnly[0] = MemoryEditor.isReadOnly(memoryEditor)
    cols[0] = MemoryEditor.getCols(memoryEditor)
    midCols[0] = MemoryEditor.getOptMidColsCount(memoryEditor)
    showHexII[0] = MemoryEditor.isOptShowHexII(memoryEditor)
    showAscii[0] = MemoryEditor.isOptShowAscii(memoryEditor)
    upperCaseHex[0] = MemoryEditor.isOptUpperCaseHex(memoryEditor)
    showDataPreview[0] = MemoryEditor.isOptShowDataPreview(memoryEditor)
    greyOutZeroes[0] = MemoryEditor.isOptGreyOutZeroes(memoryEditor)

    ImGui.checkbox("read-only", readOnly)
    ImGui.sameLine()
    if (ImGui.button("Randomize data")) {
        onRandomize(ByteArray(128) { Random.nextInt(256).toByte() })
    }
    ImGui.sliderInt("cols", cols, 4, 32)
    ImGui.sliderInt("mid-cols", midCols, 0, 8)
    ImGui.checkbox("hex II", showHexII)
    ImGui.sameLine()
    ImGui.checkbox("ascii", showAscii)
    ImGui.sameLine()
    ImGui.checkbox("upper-case hex", upperCaseHex)
    ImGui.checkbox("data preview", showDataPreview)
    ImGui.sameLine()
    ImGui.checkbox("grey out zeroes", greyOutZeroes)

    MemoryEditor.setReadOnly(memoryEditor, readOnly[0])
    MemoryEditor.setCols(memoryEditor, cols[0])
    MemoryEditor.setOptMidColsCount(memoryEditor, midCols[0])
    MemoryEditor.setOptShowHexII(memoryEditor, showHexII[0])
    MemoryEditor.setOptShowAscii(memoryEditor, showAscii[0])
    MemoryEditor.setOptUpperCaseHex(memoryEditor, upperCaseHex[0])
    MemoryEditor.setOptShowDataPreview(memoryEditor, showDataPreview[0])
    MemoryEditor.setOptGreyOutZeroes(memoryEditor, greyOutZeroes[0])

    MemoryEditor.drawContents(memoryEditor, data, baseAddr = 0x1000)

    ImGui.separatorText("Multi-Context Compositor")
    ImGui.textWrapped(
        "This window runs in context 1. The floating overlay in the top-right " +
            "corner is a second ImGui context composited over this one; click it to " +
            "route input to it. Contexts managed by the compositor: " +
            "${MultiContextCompositor.getContextCount(compositor)}.",
    )
    ImGui.checkbox("Show compositor debug overlay", showMccDebug)

    ImGui.text("frame: $frameCount")
    ImGui.end()
}

/** Overlay context: a small always-on-top window rendered over the main UI. */
private fun drawOverlay(
    fontTextureId: Long,
    slider: FloatArray,
    check: BooleanArray,
    showMccDebug: BooleanArray,
    compositor: cn.enaium.imgui.extensions.mcc.MccInstance,
    frameCount: Int,
    overlayColor: IntArray,
    onRandomize: () -> Unit,
) {
    // Anchor the top-right corner to the window edge.
    val display = ImGui.getIO().displaySize
    ImGui.setNextWindowPos(ImVec2(display.x - 8f, 8f), ImGuiCond.ALWAYS, pivot = ImVec2(1f, 0f))
    ImGui.begin(
        "overlay",
        flags = ImGuiWindowFlags.NO_DECORATION or ImGuiWindowFlags.ALWAYS_AUTO_RESIZE or
            ImGuiWindowFlags.NO_SAVED_SETTINGS,
    )
    ImGui.separatorText("Overlay context")
    ImGui.text("A second ImGui context on top.")
    ImGui.text("frame: $frameCount")
    ImGui.image(fontTextureId, ImVec2(48f, 48f))
    ImGui.sameLine()
    ImGui.textWrapped("This overlay renders with its own font atlas texture (id $fontTextureId).")
    ImGui.sliderFloat("slider", slider, 0f, 1f)
    ImGui.checkbox("checkbox", check)
    if (ImGui.button("Randomize main data")) {
        onRandomize()
    }

    ImGui.separatorText("Drop target (main context)")
    val received = if (overlayColor[0] in SWATCHES.indices) SWATCHES[overlayColor[0]] else null
    ImGui.pushStyleColor(ImGuiCol.BUTTON, received?.color ?: ImVec4(0.30f, 0.30f, 0.30f, 1f))
    ImGui.button("drop color here", ImVec2(160f, 40f))
    ImGui.popStyleColor()
    if (ImGui.beginDragDropTarget()) {
        ImGui.acceptDragDropPayload("COLOR")?.let { bytes ->
            if (bytes.isNotEmpty()) overlayColor[0] = bytes[0].toInt() and 0xFF
        }
        ImGui.endDragDropTarget()
    }
    if (received != null) {
        ImGui.text("color: ${received.name}")
    } else {
        ImGui.textDisabled("no color received yet")
    }

    ImGui.separatorText("Drag source (to main context)")
    ImGui.pushId("noteSource")
    ImGui.button("drag a note", ImVec2(160f, 0f))
    if (ImGui.beginDragDropSource()) {
        ImGui.setDragDropPayload("NOTE", "hello from the overlay".encodeToByteArray())
        ImGui.text("Note: hello from the overlay")
        ImGui.endDragDropSource()
    }
    ImGui.popId()
    ImGui.end()

    if (showMccDebug[0]) {
        MultiContextCompositor.showDebugWindow(compositor)
    }
}
