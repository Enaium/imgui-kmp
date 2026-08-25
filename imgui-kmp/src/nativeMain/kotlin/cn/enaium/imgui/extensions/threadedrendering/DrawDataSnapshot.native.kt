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
import cn.enaium.imgui.ImDrawList
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.NativeImDrawData
import cn.enaium.imgui.NativeImDrawList
import imgui.*
import kotlinx.cinterop.*

// =========================================================================
// Native (cinterop) actual implementations
// =========================================================================

internal class NativeSnapshotInstance(internal val ptr: CPointer<trs_snapshot>?) : SnapshotInstance {
    override fun close() {
        trs_snapshot_destroy(ptr)
    }
}

private fun drawDataPtr(src: ImDrawData?): CPointer<imgui_draw_data>? =
    (src as? NativeImDrawData)?.ptr

private fun vec2ToKotlin(v: CValue<imgui_vec2>): ImVec2 = v.useContents { ImVec2(x, y) }

actual object DrawDataSnapshot {
    actual fun create(): SnapshotInstance {
        val ptr = trs_snapshot_create() ?: error("trs_snapshot_create returned null")
        return NativeSnapshotInstance(ptr)
    }

    actual fun destroy(s: SnapshotInstance?) {
        if (s != null) {
            trs_snapshot_destroy((s as NativeSnapshotInstance).ptr)
        }
    }

    actual fun snapUsingSwap(s: SnapshotInstance, src: ImDrawData, currentTime: Double) =
        trs_snapshot_snap_using_swap(
            (s as NativeSnapshotInstance).ptr,
            drawDataPtr(src),
            currentTime,
        )

    actual fun clear(s: SnapshotInstance) =
        trs_snapshot_clear((s as NativeSnapshotInstance).ptr)

    actual fun getMemoryCompactTimer(s: SnapshotInstance): Float =
        trs_snapshot_get_memory_compact_timer((s as NativeSnapshotInstance).ptr)

    actual fun setMemoryCompactTimer(s: SnapshotInstance, seconds: Float) =
        trs_snapshot_set_memory_compact_timer((s as NativeSnapshotInstance).ptr, seconds)

    actual fun isValid(s: SnapshotInstance): Boolean =
        trs_snapshot_is_valid((s as NativeSnapshotInstance).ptr)

    actual fun getFrameCount(s: SnapshotInstance): Int =
        trs_snapshot_get_frame_count((s as NativeSnapshotInstance).ptr)

    actual fun getTotalIdxCount(s: SnapshotInstance): Int =
        trs_snapshot_get_total_idx_count((s as NativeSnapshotInstance).ptr)

    actual fun getTotalVtxCount(s: SnapshotInstance): Int =
        trs_snapshot_get_total_vtx_count((s as NativeSnapshotInstance).ptr)

    actual fun getDisplayPos(s: SnapshotInstance): ImVec2 =
        vec2ToKotlin(trs_snapshot_get_display_pos((s as NativeSnapshotInstance).ptr))

    actual fun getDisplaySize(s: SnapshotInstance): ImVec2 =
        vec2ToKotlin(trs_snapshot_get_display_size((s as NativeSnapshotInstance).ptr))

    actual fun getFramebufferScale(s: SnapshotInstance): ImVec2 =
        vec2ToKotlin(trs_snapshot_get_framebuffer_scale((s as NativeSnapshotInstance).ptr))

    actual fun getCmdListsCount(s: SnapshotInstance): Int =
        trs_snapshot_get_cmd_lists_count((s as NativeSnapshotInstance).ptr)

    actual fun getCmdList(s: SnapshotInstance, index: Int): ImDrawList =
        NativeImDrawList(trs_snapshot_get_cmd_list((s as NativeSnapshotInstance).ptr, index))
}
