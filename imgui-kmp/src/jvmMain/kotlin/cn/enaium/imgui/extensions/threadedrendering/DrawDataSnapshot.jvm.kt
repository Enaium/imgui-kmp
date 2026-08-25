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
import cn.enaium.imgui.ImDrawList
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.JvmImDrawData
import cn.enaium.imgui.JvmImDrawList

// =========================================================================
// JNI bridge
// =========================================================================

internal object Jni {
    init {
        cn.enaium.imgui.NativeLoader.load()
    }

    // Snapshot
    external fun snapshotCreate(): Long
    external fun snapshotDestroy(ptr: Long)
    external fun snapshotSnapUsingSwap(ptr: Long, src: Long, currentTime: Double)
    external fun snapshotClear(ptr: Long)
    external fun snapshotGetMemoryCompactTimer(ptr: Long): Float
    external fun snapshotSetMemoryCompactTimer(ptr: Long, seconds: Float)
    external fun snapshotIsValid(ptr: Long): Boolean
    external fun snapshotGetFrameCount(ptr: Long): Int
    external fun snapshotGetTotalIdxCount(ptr: Long): Int
    external fun snapshotGetTotalVtxCount(ptr: Long): Int
    external fun snapshotGetDisplayPos(ptr: Long): FloatArray
    external fun snapshotGetDisplaySize(ptr: Long): FloatArray
    external fun snapshotGetFramebufferScale(ptr: Long): FloatArray
    external fun snapshotGetCmdListsCount(ptr: Long): Int
    external fun snapshotGetCmdList(ptr: Long, index: Int): Long

    // ---- Texture queue ----

    init {
        cn.enaium.imgui.NativeLoader.load()
    }

    // Texture queue
    external fun queueCreate(): Long
    external fun queueDestroy(ptr: Long)
    external fun queueSetInFlightFrames(ptr: Long, frames: Int)
    external fun queueGetInFlightFrames(ptr: Long): Int
    external fun queuePreNewFrame(ptr: Long)
    external fun queueQueueRequests(ptr: Long, src: Long)
    external fun queueProcessRequests(ptr: Long, src: Long)
    external fun queueShutdown(ptr: Long)

}

// =========================================================================
// JVM/Android actual implementations
// =========================================================================

internal class JvmSnapshotInstance(internal val ptr: Long) : SnapshotInstance {
    override fun close() {
        Jni.snapshotDestroy(ptr)
    }
}

actual object DrawDataSnapshot {
    actual fun create(): SnapshotInstance {
        Jni.snapshotCreate().let { ptr ->
            require(ptr != 0L) { "trs_snapshot_create returned null" }
            return JvmSnapshotInstance(ptr)
        }
    }

    actual fun destroy(s: SnapshotInstance?) {
        if (s != null) {
            Jni.snapshotDestroy((s as JvmSnapshotInstance).ptr)
        }
    }

    actual fun snapUsingSwap(s: SnapshotInstance, src: ImDrawData, currentTime: Double) =
        Jni.snapshotSnapUsingSwap(
            (s as JvmSnapshotInstance).ptr,
            (src as JvmImDrawData).ptr,
            currentTime,
        )

    actual fun clear(s: SnapshotInstance) =
        Jni.snapshotClear((s as JvmSnapshotInstance).ptr)

    actual fun getMemoryCompactTimer(s: SnapshotInstance): Float =
        Jni.snapshotGetMemoryCompactTimer((s as JvmSnapshotInstance).ptr)

    actual fun setMemoryCompactTimer(s: SnapshotInstance, seconds: Float) =
        Jni.snapshotSetMemoryCompactTimer((s as JvmSnapshotInstance).ptr, seconds)

    actual fun isValid(s: SnapshotInstance): Boolean =
        Jni.snapshotIsValid((s as JvmSnapshotInstance).ptr)

    actual fun getFrameCount(s: SnapshotInstance): Int =
        Jni.snapshotGetFrameCount((s as JvmSnapshotInstance).ptr)

    actual fun getTotalIdxCount(s: SnapshotInstance): Int =
        Jni.snapshotGetTotalIdxCount((s as JvmSnapshotInstance).ptr)

    actual fun getTotalVtxCount(s: SnapshotInstance): Int =
        Jni.snapshotGetTotalVtxCount((s as JvmSnapshotInstance).ptr)

    actual fun getDisplayPos(s: SnapshotInstance): ImVec2 {
        val v = Jni.snapshotGetDisplayPos((s as JvmSnapshotInstance).ptr)
        return ImVec2(v[0], v[1])
    }

    actual fun getDisplaySize(s: SnapshotInstance): ImVec2 {
        val v = Jni.snapshotGetDisplaySize((s as JvmSnapshotInstance).ptr)
        return ImVec2(v[0], v[1])
    }

    actual fun getFramebufferScale(s: SnapshotInstance): ImVec2 {
        val v = Jni.snapshotGetFramebufferScale((s as JvmSnapshotInstance).ptr)
        return ImVec2(v[0], v[1])
    }

    actual fun getCmdListsCount(s: SnapshotInstance): Int =
        Jni.snapshotGetCmdListsCount((s as JvmSnapshotInstance).ptr)

    actual fun getCmdList(s: SnapshotInstance, index: Int): ImDrawList =
        JvmImDrawList(Jni.snapshotGetCmdList((s as JvmSnapshotInstance).ptr, index))
}
