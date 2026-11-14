package cn.enaium.imgui.example.image

import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray
import org.khronos.webgl.toInt8Array

// Published by @enaium/sdl-image-kmp's sdl_image_glue.js.
private external fun sdl_image_kmp_Load(bytes: Int8Array, type: String): Int
private external fun sdl_image_kmp_SurfaceWidth(surface: Int): Int
private external fun sdl_image_kmp_SurfaceHeight(surface: Int): Int
private external fun sdl_image_kmp_SurfacePixels(surface: Int): Int
private external fun sdl_image_kmp_SurfaceFree(surface: Int)
private external fun sdlImageKmpHeapBytes(ptr: Int, len: Int): Int8Array

internal actual fun decodePng(bytes: ByteArray): DecodedImage? {
    val surface = sdl_image_kmp_Load(bytes.toInt8Array(), "")
    if (surface == 0) return null
    try {
        val width = sdl_image_kmp_SurfaceWidth(surface)
        val height = sdl_image_kmp_SurfaceHeight(surface)
        if (width <= 0 || height <= 0) return null
        val heap = sdlImageKmpHeapBytes(sdl_image_kmp_SurfacePixels(surface), width * height * 4)
        val raw = heap.toByteArray()
        // The module hands the surface back as A,B,G,R bytes (alpha first,
        // blue before red): reverse each pixel into the R,G,B,A order the
        // renderer upload expects (SDL_PIXELFORMAT_ABGR8888 on little-endian
        // is the same byte order as RGBA32, hence the straight reverse).
        val pixels = ByteArray(raw.size)
        var i = 0
        while (i < raw.size) {
            pixels[i] = raw[i + 3]
            pixels[i + 1] = raw[i + 2]
            pixels[i + 2] = raw[i + 1]
            pixels[i + 3] = raw[i]
            i += 4
        }
        return DecodedImage(pixels, width, height)
    } finally {
        sdl_image_kmp_SurfaceFree(surface)
    }
}
