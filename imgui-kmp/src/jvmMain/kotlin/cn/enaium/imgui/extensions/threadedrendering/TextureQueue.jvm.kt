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
import cn.enaium.imgui.JvmImDrawData

// =========================================================================
// JNI bridge
// =========================================================================


// =========================================================================
// JVM/Android actual implementations
// =========================================================================

internal class JvmTextureQueueInstance(internal val ptr: Long) : TextureQueueInstance {
    override fun close() {
        Jni.queueDestroy(ptr)
    }
}

actual object TextureQueue {
    actual fun create(): TextureQueueInstance {
        Jni.queueCreate().let { ptr ->
            require(ptr != 0L) { "trs_texture_queue_create returned null" }
            return JvmTextureQueueInstance(ptr)
        }
    }

    actual fun destroy(q: TextureQueueInstance?) {
        if (q != null) {
            Jni.queueDestroy((q as JvmTextureQueueInstance).ptr)
        }
    }

    actual fun setInFlightFrames(q: TextureQueueInstance, frames: Int) =
        Jni.queueSetInFlightFrames((q as JvmTextureQueueInstance).ptr, frames)

    actual fun getInFlightFrames(q: TextureQueueInstance): Int =
        Jni.queueGetInFlightFrames((q as JvmTextureQueueInstance).ptr)

    actual fun preNewFrame(q: TextureQueueInstance) =
        Jni.queuePreNewFrame((q as JvmTextureQueueInstance).ptr)

    actual fun queueRequests(q: TextureQueueInstance, src: ImDrawData) =
        Jni.queueQueueRequests(
            (q as JvmTextureQueueInstance).ptr,
            (src as JvmImDrawData).ptr,
        )

    actual fun processRequests(q: TextureQueueInstance, src: ImDrawData?) =
        Jni.queueProcessRequests(
            (q as JvmTextureQueueInstance).ptr,
            (src as? JvmImDrawData)?.ptr ?: 0L,
        )

    actual fun shutdown(q: TextureQueueInstance) =
        Jni.queueShutdown((q as JvmTextureQueueInstance).ptr)
}
