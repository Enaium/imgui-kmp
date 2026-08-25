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

package cn.enaium.imgui.extensions.threadedrendering

import cn.enaium.imgui.ImDrawData

/**
 * A texture queue instance wrapping imgui_club's ImTextureQueue;
 * close() calls [TextureQueue.destroy].
 */
interface TextureQueueInstance : AutoCloseable

/**
 * Kotlin bindings for imgui_club's ImTextureQueue (threaded rendering), inside the
 * cn.enaium.imgui.extensions.threadedrendering package.
 *
 * Threading convention: ALL functions must be called under the same user-provided lock
 * (Kotlin side: `synchronized(queue)`).
 *
 * Backend texture-update callbacks are NOT exposed through Kotlin: they must remain on the
 * native side because they touch GPU resources. Register a C function pointer from your
 * native backend code via `trs_texture_queue_set_update_callback`.
 */
expect object TextureQueue {
    fun create(): TextureQueueInstance
    fun destroy(q: TextureQueueInstance? = null)

    fun setInFlightFrames(q: TextureQueueInstance, frames: Int)
    fun getInFlightFrames(q: TextureQueueInstance): Int

    /** (Update thread) Call before NewFrame(). Acknowledges retired destroys. */
    fun preNewFrame(q: TextureQueueInstance)

    /** (Update thread) Call after Render(), before publishing the snapshot. */
    fun queueRequests(q: TextureQueueInstance, src: ImDrawData)

    /**
     * (Render thread) Process staged requests before rendering the snapshot.
     * Pass null only when draining during shutdown (see [shutdown]).
     */
    fun processRequests(q: TextureQueueInstance, src: ImDrawData? = null)

    /** Shutdown. Call after joining threads. */
    fun shutdown(q: TextureQueueInstance)
}

// =========================================================================
// Enums (values match ImTextureStatus in imgui.h)
// =========================================================================

object TrsTextureStatus {
    const val OK: Int = 0
    const val DESTROYED: Int = 1
    const val WANT_CREATE: Int = 2
    const val WANT_UPDATES: Int = 3
    const val WANT_DESTROY: Int = 4
}
