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

package cn.enaium.imgui.example.image

import cn.enaium.imgui.ImGui
import cn.enaium.imgui.ImGuiCond
import cn.enaium.imgui.ImTextureID
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.backends.sdl.ImGuiSdlBackend
import cn.enaium.imgui.backends.sdl.ImGuiSdlRendererBackend
import cn.enaium.imgui.backends.sdl.toImTextureID
import cn.enaium.sdl.SDL
import cn.enaium.sdl.SDLGPU
import cn.enaium.sdl.SDLInitFlags
import cn.enaium.sdl.SDLPixelFormat
import cn.enaium.sdl.SDLTextureAccess
import cn.enaium.sdl.SDLWindowFlags
import cn.enaium.sdl.image.SDLImage
import kotlin.math.max

/**
 * Three imgui windows showing [ImTextureID] with different texture sources,
 * rendered side by side through the SDL 2D renderer backend:
 *
 *  1. "Texture" — a gradient triangle rasterized in Kotlin and uploaded
 *     straight into an [cn.enaium.sdl.SDLTexture] (programmatic creation).
 *  2. "GPU texture" — the same triangle saved as a PNG and loaded straight
 *     into an [cn.enaium.sdl.SDLGPUTexture] with [SDLImage.loadGPUTexture]
 *     (sdl-image-kmp's GPU loading path); the pixels are read back and
 *     displayed.
 *  3. "SDL_image" — the PNG decoded with [SDLImage.load] and displayed. Only
 *     this window uses the CPU-surface path.
 *
 * The GPU texture in window 2 is loaded from the PNG by sdl-image-kmp; the
 * file itself is generated in Kotlin (see [writePng]) so the example carries
 * no binary assets.
 *
 * All three are opaque [ImTextureID]s to imgui — the core bindings never see
 * the SDL objects. The windows are placed explicitly (not stacked) so the
 * texture sizes never collapse the layout.
 *
 * Run with `./gradlew :examples:image:jvmRun` (JVM) or the per-target native
 * binaries; the GPU texture window needs a real GPU (headless runs show a
 * hint there). Pass `--frames N` / `IMGUI_KMP_FRAMES=N` to limit frames.
 */
const val TRIANGLE_SIZE = 256

fun runImageExample(frames: Int = Int.MAX_VALUE) {
    SDL.setMainReady()
    if (!SDL.init(SDLInitFlags.VIDEO or SDLInitFlags.EVENTS)) {
        SDL.setHint("SDL_VIDEO_DRIVER", "dummy")
        if (SDL.init(SDLInitFlags.VIDEO or SDLInitFlags.EVENTS)) {
            println("video init fell back to the dummy driver — running headless")
        } else {
            error("SDL_Init failed: ${SDL.error()}")
        }
    }

    SDL.createWindow(
        title = "imgui-kmp image example",
        width = 1280,
        height = 800,
        flags = SDLWindowFlags.RESIZABLE or SDLWindowFlags.HIGH_PIXEL_DENSITY,
    ).use { window ->
        // A GPU device is used only to create the window-2 GPU texture;
        // headless sessions have no GPU backend, so its absence is tolerated.
        val device = SDLGPU.createDevice()

        SDL.createRenderer(window).use { renderer ->
            val context = ImGui.createContext()
            try {
                val imgui = ImGuiSdlBackend(window)
                val backend = ImGuiSdlRendererBackend(renderer)
                imgui.init()

                val fonts = ImGui.getIO().fonts
                val density = maxOf(imgui.framebufferScale.x, imgui.framebufferScale.y, 1f)
                fonts.addFontDefault(
                    cn.enaium.imgui.ImFontConfig(
                        sizePixels = 13f * density,
                        rasterizerDensity = density,
                    ),
                )
                check(fonts.build()) { "font atlas build failed" }
                val texData = fonts.getTexDataAsRGBA32()
                fonts.setTexID(backend.uploadFontTexture(texData.pixels, texData.width, texData.height))

                val demo = ImageDemo(renderer, backend, device)
                demo.init()

                var running = true
                var frameCount = 0
                while (running && frameCount < frames) {
                    // ---- events ----
                    while (true) {
                        val event = SDL.pollEvent() ?: break
                        when (event) {
                            is cn.enaium.sdl.SDLEvent.Quit -> running = false
                            is cn.enaium.sdl.SDLEvent.Window ->
                                if (event.type == cn.enaium.sdl.SDLWindowEventType.CLOSE_REQUESTED) running = false
                            else -> imgui.processEvent(event)
                        }
                    }

                    // ---- imgui frame ----
                    imgui.newFrame()
                    demo.draw()
                    ImGui.render()

                    renderer.drawColor = cn.enaium.sdl.SDLColor(18, 18, 24, 255)
                    renderer.clear()
                    backend.renderDrawData(ImGui.getDrawData())
                    renderer.present()
                    frameCount++
                }

                demo.close()
                backend.close()
            } finally {
                ImGui.destroyContext(context)
            }
        }
        device?.close()
    }
    SDL.quit()
}

/** Owns the three demo textures and draws the three side-by-side windows. */
private class ImageDemo(
    private val renderer: cn.enaium.sdl.SDLRenderer,
    private val backend: ImGuiSdlRendererBackend,
    private val device: cn.enaium.sdl.SDLGPUDevice?,
) {
    private var textureId: ImTextureID = ImTextureID.fromLong(0)
    private var gpuTextureId: ImTextureID = ImTextureID.fromLong(0)
    private var imageId: ImTextureID = ImTextureID.fromLong(0)
    private var gpuAvailable = false
    private val pngPath = "gradient-triangle.png"
    private val ownedTextures = mutableListOf<cn.enaium.sdl.SDLTexture>()

    fun init() {
        // 1. Programmatic SDL_Texture: rasterize the gradient triangle in
        //    Kotlin and upload it straight into a renderer texture.
        val pixels = gradientTrianglePixels(TRIANGLE_SIZE)
        textureId = uploadPixelsToRendererTexture(pixels, TRIANGLE_SIZE, TRIANGLE_SIZE)

        // The triangle as a PNG file on disk for the loader window.
        writeGradientTrianglePng(pixels, TRIANGLE_SIZE)

        // 2. GPU texture: load the PNG straight into an SDL_GPUTexture with
        //    sdl-image-kmp's loadGPUTexture (it acquires its own copy pass
        //    when none is given). The imgui 2D renderer backend cannot
        //    sample an SDL_GPUTexture directly, so the pixels are read back
        //    with download() and displayed through a renderer texture.
        if (device != null) {
            val loaded = SDLImage.loadGPUTexture(device, file = pngPath)
            if (loaded != null) {
                val readback = loaded.texture.download(loaded.width, loaded.height)
                if (readback != null) {
                    gpuTextureId = uploadPixelsToRendererTexture(readback, loaded.width, loaded.height)
                    gpuAvailable = true
                } else {
                    println("GPU texture readback failed: ${SDL.error()}")
                }
                loaded.texture.close()
            } else {
                println("SDLImage.loadGPUTexture failed: ${SDLImage.error()}")
            }
        }

        // 3. SDL_image: decode the PNG to a surface and display it. Only this
        //    window loads an image from disk.
        SDLImage.load(pngPath)?.use { surface ->
            val locked = surface.lock()
            check(locked) { "surface lock failed: ${SDL.error()}" }
            imageId = uploadPixelsToRendererTexture(surface.pixels, surface.width, surface.height)
            surface.unlock()
        } ?: error("SDLImage.load failed: ${SDLImage.error()}")
    }

    fun draw() {
        val windowW = 320f
        val windowH = 420f

        ImGui.setNextWindowPos(ImVec2(20f, 20f), ImGuiCond.FIRST_USE_EVER)
        ImGui.setNextWindowSize(ImVec2(windowW, windowH), ImGuiCond.FIRST_USE_EVER)
        if (ImGui.begin("Texture")) {
            ImGui.textWrapped("Gradient triangle rasterized in Kotlin, uploaded straight into an SDL_Texture.")
            ImGui.image(textureId, ImVec2(TRIANGLE_SIZE.toFloat(), TRIANGLE_SIZE.toFloat()))
            ImGui.text("ImTextureID 0x${textureId.value.toString(16)}")
        }
        ImGui.end()

        ImGui.setNextWindowPos(ImVec2(360f, 20f), ImGuiCond.FIRST_USE_EVER)
        ImGui.setNextWindowSize(ImVec2(windowW, windowH), ImGuiCond.FIRST_USE_EVER)
        if (ImGui.begin("GPU texture")) {
            if (gpuAvailable) {
                ImGui.textWrapped("Triangle loaded straight into a GPU texture with SDLImage.loadGPUTexture.")
                ImGui.image(gpuTextureId, ImVec2(TRIANGLE_SIZE.toFloat(), TRIANGLE_SIZE.toFloat()))
                ImGui.text("ImTextureID 0x${gpuTextureId.value.toString(16)}")
            } else {
                ImGui.textWrapped("No SDL_GPU backend available (headless run); the GPU texture window is skipped.")
            }
        }
        ImGui.end()

        ImGui.setNextWindowPos(ImVec2(700f, 20f), ImGuiCond.FIRST_USE_EVER)
        ImGui.setNextWindowSize(ImVec2(windowW, windowH), ImGuiCond.FIRST_USE_EVER)
        if (ImGui.begin("SDL_image")) {
            ImGui.textWrapped("Image decoded from disk with SDLImage.load and displayed.")
            ImGui.image(imageId, ImVec2(TRIANGLE_SIZE.toFloat(), TRIANGLE_SIZE.toFloat()))
            ImGui.text("ImTextureID 0x${imageId.value.toString(16)}")
        }
        ImGui.end()
    }

    fun close() {
        ownedTextures.forEach { backend.unregisterTexture(it) }
    }

    private fun uploadPixelsToRendererTexture(
        pixels: ByteArray,
        width: Int,
        height: Int,
    ): ImTextureID {
        // RGBA32 is SDL3's platform-neutral name for "R,G,B,A byte order in
        // memory" (ABGR8888 on little-endian, RGBA8888 on big-endian). The
        // input pixels, the GPU readback and the PNG surface from SDL_image
        // all use that same byte order, so no channel swizzle is needed.
        val texture = renderer.createTexture(
            format = SDLPixelFormat.RGBA32,
            access = SDLTextureAccess.STATIC,
            width = width,
            height = height,
        )
        texture.update(rect = null, pixels = pixels, pitch = width * 4)
        texture.blendMode = cn.enaium.sdl.SDLBlendMode.BLEND
        texture.scaleMode = cn.enaium.sdl.SDLScaleMode.LINEAR
        backend.registerTexture(texture)
        ownedTextures += texture
        return texture.toImTextureID()
    }
}

/**
 * Rasterizes a [size]x[size] gradient triangle: three vertices colored red,
 * green and blue are barycentrically interpolated (edge-function weights).
 * The triangle is opaque (alpha 255).
 */
private fun gradientTrianglePixels(size: Int): ByteArray {
    val pixels = ByteArray(size * size * 4)
    val v0 = floatArrayOf(size * 0.5f, size * 0.1f) // top
    val v1 = floatArrayOf(size * 0.1f, size * 0.9f) // bottom-left
    val v2 = floatArrayOf(size * 0.9f, size * 0.9f) // bottom-right
    val c0 = floatArrayOf(1f, 0f, 0f) // red
    val c1 = floatArrayOf(0f, 1f, 0f) // green
    val c2 = floatArrayOf(0f, 0f, 1f) // blue

    fun edge(a: FloatArray, b: FloatArray, p: FloatArray): Float =
        (p[0] - a[0]) * (b[1] - a[1]) - (p[1] - a[1]) * (b[0] - a[0])

    val area = edge(v0, v1, v2)
    for (y in 0 until size) {
        for (x in 0 until size) {
            val p = floatArrayOf(x + 0.5f, y + 0.5f)
            val w0 = edge(v1, v2, p) / area
            val w1 = edge(v2, v0, p) / area
            val w2 = edge(v0, v1, p) / area
            val inside = w0 >= 0f && w1 >= 0f && w2 >= 0f
            val r = if (inside) w0 * c0[0] + w1 * c1[0] + w2 * c2[0] else 0.12f
            val g = if (inside) w0 * c0[1] + w1 * c1[1] + w2 * c2[1] else 0.12f
            val b = if (inside) w0 * c0[2] + w1 * c1[2] + w2 * c2[2] else 0.12f
            val i = (y * size + x) * 4
            pixels[i] = (r * 255f).toInt().coerceIn(0, 255).toByte()
            pixels[i + 1] = (g * 255f).toInt().coerceIn(0, 255).toByte()
            pixels[i + 2] = (b * 255f).toInt().coerceIn(0, 255).toByte()
            pixels[i + 3] = 255.toByte()
        }
    }
    return pixels
}

/**
 * Saves [pixels] (R,G,B,A byte order) as a PNG next to the app. The PNG is
 * encoded in pure Kotlin ([writePng]) — the SDL surface bindings expose
 * pixels only as read snapshots, so there is no cross-platform way to write
 * into an [cn.enaium.sdl.SDLSurface] for [SDLImage.savePNG]. The output is
 * guaranteed opaque (alpha 255).
 */
private fun writeGradientTrianglePng(pixels: ByteArray, size: Int) {
    writeFile("gradient-triangle.png", writePng(pixels, size, size))
}