package cn.enaium.imgui.example.image

/**
 * A browser has no writable filesystem for the demo's generated PNG: the
 * bytes are dropped and the SDL_image windows report that the file is absent.
 */
internal actual fun writeFile(path: String, bytes: ByteArray) {
}
