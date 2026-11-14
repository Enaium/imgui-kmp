package cn.enaium.imgui.example.image

import cn.enaium.sdl.SDLIO
import cn.enaium.sdl.image.SDLImage

internal actual fun decodePng(bytes: ByteArray): DecodedImage? {
    val stream = SDLIO.fromConstMem(bytes) ?: return null
    val surface = SDLImage.loadIO(stream, closeIO = true) ?: return null
    return surface.use {
        DecodedImage(it.pixels.copyOf(), it.width, it.height)
    }
}
