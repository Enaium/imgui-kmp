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

actual object DrawDataSnapshot {
    actual fun create(): SnapshotInstance = throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    actual fun destroy(s: SnapshotInstance?) {
        throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    }
    actual fun snapUsingSwap(s: SnapshotInstance, src: ImDrawData, currentTime: Double) {
        throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    }
    actual fun clear(s: SnapshotInstance) {
        throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    }
    actual fun getMemoryCompactTimer(s: SnapshotInstance): Float = throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    actual fun setMemoryCompactTimer(s: SnapshotInstance, seconds: Float) {
        throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    }
    actual fun isValid(s: SnapshotInstance): Boolean = throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    actual fun getFrameCount(s: SnapshotInstance): Int = throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    actual fun getTotalIdxCount(s: SnapshotInstance): Int = throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    actual fun getTotalVtxCount(s: SnapshotInstance): Int = throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    actual fun getDisplayPos(s: SnapshotInstance): ImVec2 = throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    actual fun getDisplaySize(s: SnapshotInstance): ImVec2 = throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    actual fun getFramebufferScale(s: SnapshotInstance): ImVec2 = throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    actual fun getCmdListsCount(s: SnapshotInstance): Int = throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
    actual fun getCmdList(s: SnapshotInstance, index: Int): ImDrawList = throw UnsupportedOperationException("DrawDataSnapshot is not available on the wasm target")
}
