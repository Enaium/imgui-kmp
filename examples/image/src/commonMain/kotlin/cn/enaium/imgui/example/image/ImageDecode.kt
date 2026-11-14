package cn.enaium.imgui.example.image

/** Pixels decoded from an encoded image, ready for texture upload. */
internal class DecodedImage(val pixels: ByteArray, val width: Int, val height: Int)

/**
 * Decodes an encoded image (PNG here) into RGBA pixels.
 *
 * The desktop targets wrap the bytes in an SDL memory stream and let
 * SDL_image decode it; the browser calls SDL_image's own byte-buffer entry
 * point (its Emscripten module has no filesystem, and sdl-kmp's wasm IO
 * bindings cannot carry the 64-bit stream handle).
 */
internal expect fun decodePng(bytes: ByteArray): DecodedImage?
