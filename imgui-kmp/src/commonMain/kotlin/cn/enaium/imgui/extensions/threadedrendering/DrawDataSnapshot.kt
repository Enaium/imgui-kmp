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

/**
 * A draw data snapshot instance wrapping imgui_club's ImDrawDataSnapshot;
 * close() calls [DrawDataSnapshot.destroy].
 */
interface SnapshotInstance : AutoCloseable

/**
 * Kotlin bindings for imgui_club's ImDrawDataSnapshot (threaded rendering), inside the
 * cn.enaium.imgui.extensions.threadedrendering package.
 *
 * The snapshot owns a deep copy of the frame's draw lists so the render thread can draw
 * while the update thread builds the next frame.
 */
expect object DrawDataSnapshot {
    fun create(): SnapshotInstance
    fun destroy(s: SnapshotInstance? = null)

    /**
     * Efficient snapshot by swapping buffers: [src] is unusable afterwards.
     * Pass the draw data obtained from [cn.enaium.imgui.ImGui.getDrawData] on the update thread.
     */
    fun snapUsingSwap(s: SnapshotInstance, src: ImDrawData, currentTime: Double)

    /**
     * Releases all owned draw lists. Call after joining threads, before destroying the ImGui
     * context (required since 1.92.0).
     */
    fun clear(s: SnapshotInstance)

    /** Discard unused data after this many seconds (default 20.0). */
    fun getMemoryCompactTimer(s: SnapshotInstance): Float
    fun setMemoryCompactTimer(s: SnapshotInstance, seconds: Float)

    // ==================== Snapshot draw data accessors (render thread) ====================

    fun isValid(s: SnapshotInstance): Boolean
    fun getFrameCount(s: SnapshotInstance): Int
    fun getTotalIdxCount(s: SnapshotInstance): Int
    fun getTotalVtxCount(s: SnapshotInstance): Int
    fun getDisplayPos(s: SnapshotInstance): ImVec2
    fun getDisplaySize(s: SnapshotInstance): ImVec2
    fun getFramebufferScale(s: SnapshotInstance): ImVec2
    fun getCmdListsCount(s: SnapshotInstance): Int
    fun getCmdList(s: SnapshotInstance, index: Int): ImDrawList
}
