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

package cn.enaium.imgui.extensions.memoryeditor

import kotlinx.cinterop.*
import imgui.*

// =========================================================================
// Native (cinterop) actual implementations
// =========================================================================

internal class NativeMemoryEditorInstance(internal val ptr: CPointer<me_editor>?) : MemoryEditorInstance {
    override fun close() {
        me_destroy(ptr)
    }
}

/**
 * Pins [data] for the duration of [block] and passes a pointer to its first byte plus its
 * size. An empty array is passed as null/0.
 */
private inline fun <T> withData(
    data: ByteArray,
    block: (CPointer<UByteVar>?, ULong) -> T,
): T {
    if (data.isEmpty()) return block(null, 0u)
    return data.usePinned { pinned ->
        block(pinned.addressOf(0).reinterpret<UByteVar>(), data.size.toULong())
    }
}

actual object MemoryEditor {
    actual fun create(): MemoryEditorInstance {
        val ptr = me_create() ?: error("me_create returned null")
        return NativeMemoryEditorInstance(ptr)
    }

    actual fun destroy(e: MemoryEditorInstance?) {
        if (e != null) {
            me_destroy((e as NativeMemoryEditorInstance).ptr)
        }
    }

    actual fun drawWindow(e: MemoryEditorInstance, title: String, data: ByteArray, baseAddr: Long) =
        withData(data) { ptr, size ->
            me_draw_window((e as NativeMemoryEditorInstance).ptr, title, ptr, size, baseAddr.toULong())
        }

    actual fun drawContents(e: MemoryEditorInstance, data: ByteArray, baseAddr: Long) =
        withData(data) { ptr, size ->
            me_draw_contents((e as NativeMemoryEditorInstance).ptr, ptr, size, baseAddr.toULong())
        }

    actual fun isOpen(e: MemoryEditorInstance): Boolean =
        me_is_open((e as NativeMemoryEditorInstance).ptr)

    actual fun setOpen(e: MemoryEditorInstance, value: Boolean) =
        me_set_open((e as NativeMemoryEditorInstance).ptr, value)

    actual fun isReadOnly(e: MemoryEditorInstance): Boolean =
        me_is_read_only((e as NativeMemoryEditorInstance).ptr)

    actual fun setReadOnly(e: MemoryEditorInstance, value: Boolean) =
        me_set_read_only((e as NativeMemoryEditorInstance).ptr, value)

    actual fun getCols(e: MemoryEditorInstance): Int =
        me_get_cols((e as NativeMemoryEditorInstance).ptr)

    actual fun setCols(e: MemoryEditorInstance, value: Int) =
        me_set_cols((e as NativeMemoryEditorInstance).ptr, value)

    actual fun isOptShowOptions(e: MemoryEditorInstance): Boolean =
        me_is_opt_show_options((e as NativeMemoryEditorInstance).ptr)

    actual fun setOptShowOptions(e: MemoryEditorInstance, value: Boolean) =
        me_set_opt_show_options((e as NativeMemoryEditorInstance).ptr, value)

    actual fun isOptShowDataPreview(e: MemoryEditorInstance): Boolean =
        me_is_opt_show_data_preview((e as NativeMemoryEditorInstance).ptr)

    actual fun setOptShowDataPreview(e: MemoryEditorInstance, value: Boolean) =
        me_set_opt_show_data_preview((e as NativeMemoryEditorInstance).ptr, value)

    actual fun isOptShowHexII(e: MemoryEditorInstance): Boolean =
        me_is_opt_show_hex_ii((e as NativeMemoryEditorInstance).ptr)

    actual fun setOptShowHexII(e: MemoryEditorInstance, value: Boolean) =
        me_set_opt_show_hex_ii((e as NativeMemoryEditorInstance).ptr, value)

    actual fun isOptShowAscii(e: MemoryEditorInstance): Boolean =
        me_is_opt_show_ascii((e as NativeMemoryEditorInstance).ptr)

    actual fun setOptShowAscii(e: MemoryEditorInstance, value: Boolean) =
        me_set_opt_show_ascii((e as NativeMemoryEditorInstance).ptr, value)

    actual fun isOptGreyOutZeroes(e: MemoryEditorInstance): Boolean =
        me_is_opt_grey_out_zeroes((e as NativeMemoryEditorInstance).ptr)

    actual fun setOptGreyOutZeroes(e: MemoryEditorInstance, value: Boolean) =
        me_set_opt_grey_out_zeroes((e as NativeMemoryEditorInstance).ptr, value)

    actual fun isOptUpperCaseHex(e: MemoryEditorInstance): Boolean =
        me_is_opt_upper_case_hex((e as NativeMemoryEditorInstance).ptr)

    actual fun setOptUpperCaseHex(e: MemoryEditorInstance, value: Boolean) =
        me_set_opt_upper_case_hex((e as NativeMemoryEditorInstance).ptr, value)

    actual fun getOptMidColsCount(e: MemoryEditorInstance): Int =
        me_get_opt_mid_cols_count((e as NativeMemoryEditorInstance).ptr)

    actual fun setOptMidColsCount(e: MemoryEditorInstance, value: Int) =
        me_set_opt_mid_cols_count((e as NativeMemoryEditorInstance).ptr, value)

    actual fun getOptAddrDigitsCount(e: MemoryEditorInstance): Int =
        me_get_opt_addr_digits_count((e as NativeMemoryEditorInstance).ptr)

    actual fun setOptAddrDigitsCount(e: MemoryEditorInstance, value: Int) =
        me_set_opt_addr_digits_count((e as NativeMemoryEditorInstance).ptr, value)

    actual fun getOptFooterExtraHeight(e: MemoryEditorInstance): Float =
        me_get_opt_footer_extra_height((e as NativeMemoryEditorInstance).ptr)

    actual fun setOptFooterExtraHeight(e: MemoryEditorInstance, value: Float) =
        me_set_opt_footer_extra_height((e as NativeMemoryEditorInstance).ptr, value)

    actual fun getHighlightColor(e: MemoryEditorInstance): Int =
        me_get_highlight_color((e as NativeMemoryEditorInstance).ptr).toInt()

    actual fun setHighlightColor(e: MemoryEditorInstance, value: Int) =
        me_set_highlight_color((e as NativeMemoryEditorInstance).ptr, value.toUInt())

    actual fun isMouseHovered(e: MemoryEditorInstance): Boolean =
        me_is_mouse_hovered((e as NativeMemoryEditorInstance).ptr)

    actual fun mouseHoveredAddr(e: MemoryEditorInstance): Long =
        me_mouse_hovered_addr((e as NativeMemoryEditorInstance).ptr).toLong()
}
