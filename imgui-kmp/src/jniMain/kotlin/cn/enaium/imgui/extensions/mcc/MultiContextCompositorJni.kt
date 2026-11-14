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

package cn.enaium.imgui.extensions.mcc

import cn.enaium.imgui.ImGuiContext
import cn.enaium.imgui.JniImGuiContext

// =========================================================================
// JNI bridge
// =========================================================================


// =========================================================================
// JVM/Android actual implementations
// =========================================================================

internal class JniMccInstance(internal val ptr: Long) : MccInstance {
    override fun close() {
        Jni.destroy(ptr)
    }
}

actual object MultiContextCompositor {
    actual fun create(): MccInstance = JniMccInstance(Jni.create())

    actual fun destroy(e: MccInstance?) {
        Jni.destroy(if (e != null) (e as JniMccInstance).ptr else 0L)
    }

    actual fun addContext(e: MccInstance, context: ImGuiContext) =
        Jni.addContext((e as JniMccInstance).ptr, (context as JniImGuiContext).ptr)

    actual fun removeContext(e: MccInstance, context: ImGuiContext) =
        Jni.removeContext((e as JniMccInstance).ptr, (context as JniImGuiContext).ptr)

    actual fun getContextCount(e: MccInstance): Int =
        Jni.getContextCount((e as JniMccInstance).ptr)

    actual fun preNewFrameUpdateAll(e: MccInstance) =
        Jni.preNewFrameUpdateAll((e as JniMccInstance).ptr)

    actual fun postNewFrameUpdateOne(e: MccInstance, context: ImGuiContext) =
        Jni.postNewFrameUpdateOne((e as JniMccInstance).ptr, (context as JniImGuiContext).ptr)

    actual fun postEndFrameUpdateAll(e: MccInstance) =
        Jni.postEndFrameUpdateAll((e as JniMccInstance).ptr)

    actual fun showDebugWindow(e: MccInstance) =
        Jni.showDebugWindow((e as JniMccInstance).ptr)
}
