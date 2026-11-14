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

package cn.enaium.imgui.extensions.markdown

// JNI bridge
// =========================================================================


// Per-config callback registries, keyed by the raw config pointer. Callbacks
// are stored in Kotlin and dispatched from [MarkdownJvmBridge] when the C
// trampolines fire. Entries are removed when a callback is deactivated so
// released configs do not leak.
private val linkRegistries = mutableMapOf<Long, (MarkdownLinkData) -> Unit>()
private val tooltipRegistries = mutableMapOf<Long, (MarkdownLinkData, String) -> Unit>()
private val imageRegistries = mutableMapOf<Long, (MarkdownLinkData) -> MarkdownImageData>()

/** Dispatches one callback to the registry of the config that produced it. */
internal fun markdownDispatchLink(configPtr: Long, text: String?, link: String?, isImage: Boolean) {
    linkRegistries[configPtr]?.invoke(MarkdownLinkData(text, link, isImage))
}

internal fun markdownDispatchTooltip(
    configPtr: Long,
    text: String?,
    link: String?,
    isImage: Boolean,
    linkIcon: String?,
) {
    tooltipRegistries[configPtr]?.invoke(MarkdownLinkData(text, link, isImage), linkIcon ?: "")
}

internal fun markdownDispatchImage(
    configPtr: Long,
    text: String?,
    link: String?,
    isImage: Boolean,
): MarkdownImageData = imageRegistries[configPtr]?.invoke(MarkdownLinkData(text, link, isImage))
    ?: MarkdownImageData()

/**
 * The JVM trampolines in jni_bridge.cpp fire synchronously during the render
 * call; Emscripten cannot call back into Kotlin, so the wasm build drains a
 * queue of recorded callbacks here instead. No-op on the JVM.
 */
internal expect fun drainMarkdownEvents()

// =========================================================================
// JVM/Android actual implementations
// =========================================================================

internal class JniMarkdownConfigHandle(internal val ptr: Long) : MarkdownConfigHandle {
    override fun close() {
        Jni.destroy(ptr)
    }
}

actual object Markdown {
    actual fun create(): MarkdownConfigHandle {
        val ptr = Jni.create()
        require(ptr != 0L) { "md_create returned null" }
        return JniMarkdownConfigHandle(ptr)
    }

    actual fun destroy(config: MarkdownConfigHandle?) {
        if (config != null) {
            Jni.destroy((config as JniMarkdownConfigHandle).ptr)
        }
    }

    actual fun setLinkIcon(config: MarkdownConfigHandle, icon: String) {
        Jni.setLinkIcon((config as JniMarkdownConfigHandle).ptr, icon)
    }

    actual fun setHeading(config: MarkdownConfigHandle, level: Int, font: Long, separator: Boolean) {
        Jni.setHeading((config as JniMarkdownConfigHandle).ptr, level, font, separator)
    }

    actual fun setFormatFlags(config: MarkdownConfigHandle, flags: Int) {
        Jni.setFormatFlags((config as JniMarkdownConfigHandle).ptr, flags)
    }

    actual fun setLinkCallback(config: MarkdownConfigHandle, callback: ((MarkdownLinkData) -> Unit)?) {
        val ptr = (config as JniMarkdownConfigHandle).ptr
        if (callback != null) {
            linkRegistries[ptr] = callback
        } else {
            linkRegistries.remove(ptr)
        }
        Jni.setLinkCallback(ptr, callback != null)
    }

    actual fun setTooltipCallback(config: MarkdownConfigHandle, callback: ((MarkdownLinkData, String) -> Unit)?) {
        val ptr = (config as JniMarkdownConfigHandle).ptr
        if (callback != null) {
            tooltipRegistries[ptr] = callback
        } else {
            tooltipRegistries.remove(ptr)
        }
        Jni.setTooltipCallback(ptr, callback != null)
    }

    actual fun setImageCallback(config: MarkdownConfigHandle, callback: ((MarkdownLinkData) -> MarkdownImageData)?) {
        val ptr = (config as JniMarkdownConfigHandle).ptr
        if (callback != null) {
            imageRegistries[ptr] = callback
        } else {
            imageRegistries.remove(ptr)
        }
        Jni.setImageCallback(ptr, callback != null)
    }

    actual fun render(config: MarkdownConfigHandle, markdown: String) {
        Jni.render((config as JniMarkdownConfigHandle).ptr, markdown)
        drainMarkdownEvents()
    }
}