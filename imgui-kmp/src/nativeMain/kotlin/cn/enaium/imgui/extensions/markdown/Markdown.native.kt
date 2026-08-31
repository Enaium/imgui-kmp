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

@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlin.experimental.ExperimentalNativeApi::class,
)

package cn.enaium.imgui.extensions.markdown

import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import kotlinx.cinterop.*
import imgui.*

// =========================================================================
// Per-config callback registries, keyed by the raw `md_config` pointer value.
// The C side stores the key as opaque user_data and forwards it back through
// md_link_data.user_data; the static trampolines below look the Kotlin
// callback up here. File-private so both the actual object and the
// trampolines in this file can reach them.
// =========================================================================
private val linkCallbacks = mutableMapOf<Long, ((MarkdownLinkData) -> Unit)?>()
private val tooltipCallbacks = mutableMapOf<Long, ((MarkdownLinkData, String) -> Unit)?>()
private val imageCallbacks = mutableMapOf<Long, ((MarkdownLinkData) -> MarkdownImageData)?>()

/** Native (cinterop) implementation of [MarkdownConfigHandle]; [close] destroys the `md_config`. */
internal class NativeMarkdownConfigHandle(internal val ptr: CPointer<md_config>?) : MarkdownConfigHandle {
    override fun close() {
        md_destroy(ptr)
    }
}

/** Casts [config] to its native implementation and returns the raw `md_config` pointer. */
private fun ptr(config: MarkdownConfigHandle): CPointer<md_config>? =
    (config as NativeMarkdownConfigHandle).ptr

/** Uses the raw `md_config` pointer value as the registry key. */
private fun key(config: MarkdownConfigHandle): Long =
    ptr(config)?.rawValue?.toLong() ?: 0L

/** Decodes the config key from the opaque user_data token. */
private fun keyOf(userData: COpaquePointer?): Long =
    userData?.rawValue?.toLong() ?: 0L

/** Reads the stack-local link data synchronously (valid only inside the callback). */
private fun readLinkData(data: CPointer<md_link_data>?): MarkdownLinkData {
    val d = data?.pointed ?: return MarkdownLinkData(null, null, false)
    return MarkdownLinkData(
        text = d.text?.toKString(),
        link = d.link?.toKString(),
        isImage = d.is_image,
    )
}

// =========================================================================
// C trampolines (static; they dispatch through the registries above)
// =========================================================================

private fun linkTrampoline(data: CPointer<md_link_data>?) {
    linkCallbacks[keyOf(data?.pointed?.user_data)]?.invoke(readLinkData(data))
}

private fun tooltipTrampoline(data: CPointer<md_link_data>?, linkIcon: CPointer<ByteVar>?) {
    tooltipCallbacks[keyOf(data?.pointed?.user_data)]?.invoke(readLinkData(data), linkIcon?.toKString() ?: "")
}

/** Builds the `md_image_data` by-value struct from the Kotlin [MarkdownImageData]. */
private fun imageTrampoline(data: CPointer<md_link_data>?): CValue<md_image_data> = memScoped {
    val out = alloc<md_image_data>()
    val image = imageCallbacks[keyOf(data?.pointed?.user_data)]?.invoke(readLinkData(data))
    if (image != null) {
        out.is_valid = image.isValid
        out.use_link_callback = image.useLinkCallback
        out.user_texture_id = image.userTextureId.toULong()
        out.size.x = image.size.x
        out.size.y = image.size.y
        out.uv0.x = image.uv0.x
        out.uv0.y = image.uv0.y
        out.uv1.x = image.uv1.x
        out.uv1.y = image.uv1.y
        out.tint_col.x = image.tintCol.x
        out.tint_col.y = image.tintCol.y
        out.tint_col.z = image.tintCol.z
        out.tint_col.w = image.tintCol.w
        out.border_col.x = image.borderCol.x
        out.border_col.y = image.borderCol.y
        out.border_col.z = image.borderCol.z
        out.border_col.w = image.borderCol.w
        out.bg_col.x = image.bgCol.x
        out.bg_col.y = image.bgCol.y
        out.bg_col.z = image.bgCol.z
        out.bg_col.w = image.bgCol.w
    }
    out.readValue()
}

// =========================================================================
// Native (cinterop) actual implementations
// =========================================================================
actual object Markdown {
    actual fun create(): MarkdownConfigHandle {
        val p = md_create() ?: error("md_create returned null")
        return NativeMarkdownConfigHandle(p)
    }

    actual fun destroy(config: MarkdownConfigHandle?) {
        if (config != null) {
            md_destroy(ptr(config))
        }
    }

    actual fun setLinkIcon(config: MarkdownConfigHandle, icon: String) =
        md_set_link_icon(ptr(config), icon)

    actual fun setHeading(config: MarkdownConfigHandle, level: Int, font: Long, separator: Boolean) =
        md_set_heading(ptr(config), level, font.toULong(), separator)

    actual fun setFormatFlags(config: MarkdownConfigHandle, flags: Int) =
        md_set_format_flags(ptr(config), flags)

    actual fun setLinkCallback(config: MarkdownConfigHandle, callback: ((MarkdownLinkData) -> Unit)?) {
        val p = ptr(config)
        val k = key(config)
        if (callback != null) {
            linkCallbacks[k] = callback
            md_set_link_callback(p, staticCFunction(::linkTrampoline), k.toCPointer<ByteVar>())
        } else {
            linkCallbacks.remove(k)
            md_set_link_callback(p, null, null)
        }
    }

    actual fun setTooltipCallback(config: MarkdownConfigHandle, callback: ((MarkdownLinkData, String) -> Unit)?) {
        val p = ptr(config)
        val k = key(config)
        if (callback != null) {
            tooltipCallbacks[k] = callback
            md_set_tooltip_callback(p, staticCFunction(::tooltipTrampoline), k.toCPointer<ByteVar>())
        } else {
            tooltipCallbacks.remove(k)
            md_set_tooltip_callback(p, null, null)
        }
    }

    actual fun setImageCallback(config: MarkdownConfigHandle, callback: ((MarkdownLinkData) -> MarkdownImageData)?) {
        val p = ptr(config)
        val k = key(config)
        if (callback != null) {
            imageCallbacks[k] = callback
            md_set_image_callback(p, staticCFunction(::imageTrampoline), k.toCPointer<ByteVar>())
        } else {
            imageCallbacks.remove(k)
            md_set_image_callback(p, null, null)
        }
    }

    actual fun render(config: MarkdownConfigHandle, markdown: String) =
        // length is uint32_t (fixed width): toUInt() keeps it platform-neutral.
        md_render(ptr(config), markdown, markdown.encodeToByteArray().size.toUInt())
}
