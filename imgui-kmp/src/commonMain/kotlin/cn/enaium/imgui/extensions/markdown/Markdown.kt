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

import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4

/**
 * Data passed to link/image callbacks (mirrors MarkdownLinkCallbackData).
 */
data class MarkdownLinkData(
    val text: String?,
    val link: String?,
    val isImage: Boolean,
)

/** Image payload an image callback may return (mirrors MarkdownImageData). */
data class MarkdownImageData(
    val isValid: Boolean = false,
    val useLinkCallback: Boolean = false,
    val userTextureId: Long = 0,
    val size: ImVec2 = ImVec2(100f, 100f),
    val uv0: ImVec2 = ImVec2(0f, 0f),
    val uv1: ImVec2 = ImVec2(1f, 1f),
    val tintCol: ImVec4 = ImVec4(1f, 1f, 1f, 1f),
    val borderCol: ImVec4 = ImVec4(0f, 0f, 0f, 0f),
    val bgCol: ImVec4 = ImVec4(0f, 0f, 0f, 0f),
)

/** A Markdown renderer configuration; close() calls [Markdown.destroy]. */
interface MarkdownConfigHandle : AutoCloseable

/**
 * Kotlin bindings for imgui_markdown (enkisoftware/imgui_markdown),
 * inside the cn.enaium.imgui.extensions.markdown package.
 *
 * Render Markdown with [render]; configure link icon, heading fonts and
 * optional link/tooltip/image callbacks via the returned handle.
 */
expect object Markdown {
    fun create(): MarkdownConfigHandle
    fun destroy(config: MarkdownConfigHandle? = null)

    /** Link icon text shown in link tooltips (copied into the config). */
    fun setLinkIcon(config: MarkdownConfigHandle, icon: String)

    /**
     * Heading font + separator for heading [level] (1..3). [font] is an
     * ImFont handle (e.g. from ImGui's font atlas; 0 = default font).
     */
    fun setHeading(config: MarkdownConfigHandle, level: Int, font: Long, separator: Boolean)

    /** Format flags bitmask (ImGuiMarkdownFormatFlags). */
    fun setFormatFlags(config: MarkdownConfigHandle, flags: Int)

    /**
     * Registers a link callback (invoked when a link is clicked); null to
     * disable. Only one of link/tooltip/image callbacks may be active per
     * config (they share the same underlying user data).
     */
    fun setLinkCallback(config: MarkdownConfigHandle, callback: ((MarkdownLinkData) -> Unit)?)

    /** Registers a tooltip callback; null to disable. */
    fun setTooltipCallback(config: MarkdownConfigHandle, callback: ((MarkdownLinkData, String) -> Unit)?)

    /** Registers an image callback; null to disable. */
    fun setImageCallback(config: MarkdownConfigHandle, callback: ((MarkdownLinkData) -> MarkdownImageData)?)

    /** Renders [markdown] into the current ImGui window. */
    fun render(config: MarkdownConfigHandle, markdown: String)
}

// =========================================================================
// Enums (values match ImGuiMarkdownFormatFlags)
// =========================================================================

object MdFormatFlags {
    const val NONE = 0
    const val DISCARD_EXTRA_NEW_LINES = 1 shl 0
    const val NO_NEW_LINE_BEFORE_HEADING = 1 shl 1
    const val SEPARATOR_DOES_NOT_ADVANCE = 1 shl 2
    const val COMMON_MARK_ALL =
        DISCARD_EXTRA_NEW_LINES or NO_NEW_LINE_BEFORE_HEADING or SEPARATOR_DOES_NOT_ADVANCE
}