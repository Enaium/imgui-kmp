package cn.enaium.imgui.example.club

import cn.enaium.imgui.example.common.AppFrameDriver
import kotlinx.browser.window

/**
 * Browser entry point. The host page (index.html) loads the Emscripten SDL and
 * imgui modules before this module runs; frames are driven from
 * requestAnimationFrame so the canvas keeps painting.
 */
private val browserDriver: AppFrameDriver = { step, done ->
    fun tick() {
        if (step()) window.requestAnimationFrame { tick() } else done()
    }
    window.requestAnimationFrame { tick() }
}

fun main() {
    runClubExample(driver = browserDriver)
}
