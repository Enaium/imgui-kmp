package cn.enaium.imgui.example.image

import cn.enaium.imgui.example.common.AppFrameDriver
import kotlinx.browser.window

private val browserDriver: AppFrameDriver = { step, done ->
    fun tick() {
        if (step()) window.requestAnimationFrame { tick() } else done()
    }
    window.requestAnimationFrame { tick() }
}

fun main() {
    runImageExample(driver = browserDriver)
}
