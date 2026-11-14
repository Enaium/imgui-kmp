package cn.enaium.imgui.extensions.markdown

// Published by imgui_kmp_markdown.h (see gen_wasm_shim.py's export list).
internal external fun imgui_kmp_md_install(config: Int, kind: Int)
internal external fun imgui_kmp_md_uninstall(config: Int, kind: Int)
internal external fun imgui_kmp_md_drain(): Int
internal external fun imgui_kmp_md_event_total(): Int
internal external fun imgui_kmp_md_event_kind(index: Int): Int
internal external fun imgui_kmp_md_event_config(index: Int): Int
internal external fun imgui_kmp_md_event_is_image(index: Int): Int
internal external fun imgui_kmp_md_event_text(index: Int): String?
internal external fun imgui_kmp_md_event_link(index: Int): String?
internal external fun imgui_kmp_md_event_icon(index: Int): String?
internal external fun imgui_kmp_md_set_image(tex: Int, width: Float, height: Float)

/**
 * Drains the callbacks recorded by the C trampolines during the last
 * [Markdown.render] and dispatches them to the registered lambdas. Image
 * results reach the C side for the *next* frame (their payload is produced
 * here, after the render pass has finished).
 */
internal actual fun drainMarkdownEvents() {
    val count = imgui_kmp_md_drain()
    if (count <= 0) return
    val total = imgui_kmp_md_event_total()
    for (index in (total - count) until total) {
        val config = imgui_kmp_md_event_config(index).toLong()
        val text = imgui_kmp_md_event_text(index)
        val link = imgui_kmp_md_event_link(index)
        val isImage = imgui_kmp_md_event_is_image(index) != 0
        when (imgui_kmp_md_event_kind(index)) {
            0 -> markdownDispatchLink(config, text, link, isImage)
            1 -> markdownDispatchTooltip(config, text, link, isImage, imgui_kmp_md_event_icon(index))
            else -> {
                val data = markdownDispatchImage(config, text, link, isImage)
                if (data.isValid) {
                    imgui_kmp_md_set_image(data.userTextureId.toInt(), data.size.x, data.size.y)
                }
            }
        }
    }
}
