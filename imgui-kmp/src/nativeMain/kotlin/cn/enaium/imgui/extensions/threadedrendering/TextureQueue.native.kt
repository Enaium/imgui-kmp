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

package cn.enaium.imgui.extensions.threadedrendering

import cn.enaium.imgui.ImDrawData
import cn.enaium.imgui.NativeImDrawData
import imgui.*
import kotlinx.cinterop.*

// =========================================================================
// Native (cinterop) actual implementations
// =========================================================================

internal class NativeTextureQueueInstance(internal val ptr: CPointer<trs_texture_queue>?) :
    TextureQueueInstance {
    override fun close() {
        trs_texture_queue_destroy(ptr)
    }
}

private fun drawDataPtr(src: ImDrawData?): CPointer<imgui_draw_data>? =
    (src as? NativeImDrawData)?.ptr

actual object TextureQueue {
    actual fun create(): TextureQueueInstance {
        val ptr = trs_texture_queue_create() ?: error("trs_texture_queue_create returned null")
        return NativeTextureQueueInstance(ptr)
    }

    actual fun destroy(q: TextureQueueInstance?) {
        if (q != null) {
            trs_texture_queue_destroy((q as NativeTextureQueueInstance).ptr)
        }
    }

    actual fun setInFlightFrames(q: TextureQueueInstance, frames: Int) =
        trs_texture_queue_set_in_flight_frames((q as NativeTextureQueueInstance).ptr, frames)

    actual fun getInFlightFrames(q: TextureQueueInstance): Int =
        trs_texture_queue_get_in_flight_frames((q as NativeTextureQueueInstance).ptr)

    actual fun preNewFrame(q: TextureQueueInstance) =
        trs_texture_queue_pre_new_frame((q as NativeTextureQueueInstance).ptr)

    actual fun queueRequests(q: TextureQueueInstance, src: ImDrawData) =
        trs_texture_queue_queue_requests(
            (q as NativeTextureQueueInstance).ptr,
            drawDataPtr(src),
        )

    actual fun processRequests(q: TextureQueueInstance, src: ImDrawData?) =
        trs_texture_queue_process_requests(
            (q as NativeTextureQueueInstance).ptr,
            drawDataPtr(src),
        )

    actual fun shutdown(q: TextureQueueInstance) =
        trs_texture_queue_shutdown((q as NativeTextureQueueInstance).ptr)
}
