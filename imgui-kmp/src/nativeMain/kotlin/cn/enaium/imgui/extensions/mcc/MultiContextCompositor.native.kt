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

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package cn.enaium.imgui.extensions.mcc

import cn.enaium.imgui.ImGuiContext
import cn.enaium.imgui.NativeImGuiContext
import imgui.*
import imgui.mcc_compositor
import kotlinx.cinterop.CPointer

// =========================================================================
// Native (cinterop) actual implementations
// =========================================================================

internal class NativeMccInstance(internal val ptr: CPointer<mcc_compositor>?) : MccInstance {
    override fun close() {
        mcc_destroy(ptr)
    }
}

actual object MultiContextCompositor {
    actual fun create(): MccInstance {
        val ptr = mcc_create() ?: error("mcc_create returned null")
        return NativeMccInstance(ptr)
    }

    actual fun destroy(e: MccInstance?) {
        if (e != null) {
            mcc_destroy((e as NativeMccInstance).ptr)
        }
    }

    actual fun addContext(e: MccInstance, context: ImGuiContext) =
        mcc_add_context((e as NativeMccInstance).ptr, (context as NativeImGuiContext).ptr)

    actual fun removeContext(e: MccInstance, context: ImGuiContext) =
        mcc_remove_context((e as NativeMccInstance).ptr, (context as NativeImGuiContext).ptr)

    actual fun getContextCount(e: MccInstance): Int =
        mcc_get_context_count((e as NativeMccInstance).ptr)

    actual fun preNewFrameUpdateAll(e: MccInstance) =
        mcc_pre_new_frame_update_all((e as NativeMccInstance).ptr)

    actual fun postNewFrameUpdateOne(e: MccInstance, context: ImGuiContext) =
        mcc_post_new_frame_update_one((e as NativeMccInstance).ptr, (context as NativeImGuiContext).ptr)

    actual fun postEndFrameUpdateAll(e: MccInstance) =
        mcc_post_end_frame_update_all((e as NativeMccInstance).ptr)

    actual fun showDebugWindow(e: MccInstance) =
        mcc_show_debug_window((e as NativeMccInstance).ptr)
}
