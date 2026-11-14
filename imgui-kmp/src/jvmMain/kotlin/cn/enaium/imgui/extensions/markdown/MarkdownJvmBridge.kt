package cn.enaium.imgui.extensions.markdown

/**
 * Static callbacks invoked from the C trampolines in jni_bridge.cpp
 * (JVM class: cn.enaium.imgui.extensions.markdown.MarkdownJvmBridge).
 * They dispatch to the per-config registries in the shared adapter layer.
 */
internal object MarkdownJvmBridge {
    @JvmStatic
    fun notifyLink(configPtr: Long, text: String?, link: String?, isImage: Boolean) {
        markdownDispatchLink(configPtr, text, link, isImage)
    }

    @JvmStatic
    fun notifyTooltip(configPtr: Long, text: String?, link: String?, isImage: Boolean, linkIcon: String?) {
        markdownDispatchTooltip(configPtr, text, link, isImage, linkIcon)
    }

    @JvmStatic
    fun notifyImage(configPtr: Long, text: String?, link: String?, isImage: Boolean): MarkdownImageData =
        markdownDispatchImage(configPtr, text, link, isImage)
}

internal actual fun drainMarkdownEvents() {}
