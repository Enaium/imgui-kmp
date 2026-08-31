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

internal object Jni {
    init {
        cn.enaium.imgui.NativeLoader.load()
    }

    external fun create(): Long
    external fun destroy(configPtr: Long)
    external fun setLinkIcon(configPtr: Long, icon: String)
    external fun setHeading(configPtr: Long, level: Int, font: Long, separator: Boolean)
    external fun setFormatFlags(configPtr: Long, flags: Int)
    external fun setLinkCallback(configPtr: Long, activate: Boolean)
    external fun setTooltipCallback(configPtr: Long, activate: Boolean)
    external fun setImageCallback(configPtr: Long, activate: Boolean)
    external fun render(configPtr: Long, markdown: String)
}

// Per-config callback registries, keyed by the raw config pointer. Callbacks
// are stored in Kotlin and dispatched from [MarkdownJvmBridge] when the C
// trampolines fire. Entries are removed when a callback is deactivated so
// released configs do not leak.
private val linkRegistries = mutableMapOf<Long, (MarkdownLinkData) -> Unit>()
private val tooltipRegistries = mutableMapOf<Long, (MarkdownLinkData, String) -> Unit>()
private val imageRegistries = mutableMapOf<Long, (MarkdownLinkData) -> MarkdownImageData>()

/**
 * Static callbacks invoked from the C trampolines in jni_bridge.cpp
 * (JVM class: cn.enaium.imgui.extensions.markdown.MarkdownJvmBridge).
 * Dispatches to the per-config registries keyed by the config pointer.
 */
internal object MarkdownJvmBridge {
    @JvmStatic
    fun notifyLink(configPtr: Long, text: String?, link: String?, isImage: Boolean) {
        linkRegistries[configPtr]?.invoke(MarkdownLinkData(text, link, isImage))
    }

    @JvmStatic
    fun notifyTooltip(configPtr: Long, text: String?, link: String?, isImage: Boolean, linkIcon: String?) {
        tooltipRegistries[configPtr]?.invoke(MarkdownLinkData(text, link, isImage), linkIcon ?: "")
    }

    @JvmStatic
    fun notifyImage(configPtr: Long, text: String?, link: String?, isImage: Boolean): MarkdownImageData =
        imageRegistries[configPtr]?.invoke(MarkdownLinkData(text, link, isImage)) ?: MarkdownImageData()
}

// =========================================================================
// JVM/Android actual implementations
// =========================================================================

internal class JvmMarkdownConfigHandle(internal val ptr: Long) : MarkdownConfigHandle {
    override fun close() {
        Jni.destroy(ptr)
    }
}

actual object Markdown {
    actual fun create(): MarkdownConfigHandle {
        val ptr = Jni.create()
        require(ptr != 0L) { "md_create returned null" }
        return JvmMarkdownConfigHandle(ptr)
    }

    actual fun destroy(config: MarkdownConfigHandle?) {
        if (config != null) {
            Jni.destroy((config as JvmMarkdownConfigHandle).ptr)
        }
    }

    actual fun setLinkIcon(config: MarkdownConfigHandle, icon: String) {
        Jni.setLinkIcon((config as JvmMarkdownConfigHandle).ptr, icon)
    }

    actual fun setHeading(config: MarkdownConfigHandle, level: Int, font: Long, separator: Boolean) {
        Jni.setHeading((config as JvmMarkdownConfigHandle).ptr, level, font, separator)
    }

    actual fun setFormatFlags(config: MarkdownConfigHandle, flags: Int) {
        Jni.setFormatFlags((config as JvmMarkdownConfigHandle).ptr, flags)
    }

    actual fun setLinkCallback(config: MarkdownConfigHandle, callback: ((MarkdownLinkData) -> Unit)?) {
        val ptr = (config as JvmMarkdownConfigHandle).ptr
        if (callback != null) {
            linkRegistries[ptr] = callback
        } else {
            linkRegistries.remove(ptr)
        }
        Jni.setLinkCallback(ptr, callback != null)
    }

    actual fun setTooltipCallback(config: MarkdownConfigHandle, callback: ((MarkdownLinkData, String) -> Unit)?) {
        val ptr = (config as JvmMarkdownConfigHandle).ptr
        if (callback != null) {
            tooltipRegistries[ptr] = callback
        } else {
            tooltipRegistries.remove(ptr)
        }
        Jni.setTooltipCallback(ptr, callback != null)
    }

    actual fun setImageCallback(config: MarkdownConfigHandle, callback: ((MarkdownLinkData) -> MarkdownImageData)?) {
        val ptr = (config as JvmMarkdownConfigHandle).ptr
        if (callback != null) {
            imageRegistries[ptr] = callback
        } else {
            imageRegistries.remove(ptr)
        }
        Jni.setImageCallback(ptr, callback != null)
    }

    actual fun render(config: MarkdownConfigHandle, markdown: String) {
        Jni.render((config as JvmMarkdownConfigHandle).ptr, markdown)
    }
}