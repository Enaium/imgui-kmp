package cn.enaium.imgui.example.renderer

import kotlinx.browser.window

/**
 * Browser entry point: the page loads the Emscripten SDL and imgui modules
 * (sdl_kmp_glue.js / imgui_kmp_glue.js) before the Kotlin module runs, then
 * frames are driven from requestAnimationFrame so the canvas keeps painting.
 */
fun main() {
    runSdlRendererExample(
        driver = { step, done ->
            fun tick() {
                if (step()) window.requestAnimationFrame { tick() } else done()
            }
            window.requestAnimationFrame { tick() }
        },
    )
}
