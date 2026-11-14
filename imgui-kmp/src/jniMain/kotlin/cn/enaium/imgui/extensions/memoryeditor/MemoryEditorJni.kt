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

package cn.enaium.imgui.extensions.memoryeditor

// =========================================================================
// JNI bridge
// =========================================================================


// =========================================================================
// JVM/Android actual implementations
// =========================================================================

internal class JniMemoryEditorInstance(internal val ptr: Long) : MemoryEditorInstance {
    override fun close() {
        Jni.destroy(ptr)
    }
}

actual object MemoryEditor {
    actual fun create(): MemoryEditorInstance {
        Jni.create().let { ptr ->
            require(ptr != 0L) { "me_create returned null" }
            return JniMemoryEditorInstance(ptr)
        }
    }

    actual fun destroy(e: MemoryEditorInstance?) {
        if (e != null) {
            Jni.destroy((e as JniMemoryEditorInstance).ptr)
        }
    }

    actual fun drawWindow(e: MemoryEditorInstance, title: String, data: ByteArray, baseAddr: Long) {
        val editor = e as JniMemoryEditorInstance
        Jni.drawWindow(editor.ptr, title, data, data.size, baseAddr)
    }

    actual fun drawContents(e: MemoryEditorInstance, data: ByteArray, baseAddr: Long) {
        val editor = e as JniMemoryEditorInstance
        Jni.drawContents(editor.ptr, data, data.size, baseAddr)
    }

    actual fun isOpen(e: MemoryEditorInstance): Boolean =
        Jni.isOpen((e as JniMemoryEditorInstance).ptr)

    actual fun setOpen(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOpen((e as JniMemoryEditorInstance).ptr, value)

    actual fun isReadOnly(e: MemoryEditorInstance): Boolean =
        Jni.isReadOnly((e as JniMemoryEditorInstance).ptr)

    actual fun setReadOnly(e: MemoryEditorInstance, value: Boolean) =
        Jni.setReadOnly((e as JniMemoryEditorInstance).ptr, value)

    actual fun getCols(e: MemoryEditorInstance): Int =
        Jni.getCols((e as JniMemoryEditorInstance).ptr)

    actual fun setCols(e: MemoryEditorInstance, value: Int) =
        Jni.setCols((e as JniMemoryEditorInstance).ptr, value)

    actual fun isOptShowOptions(e: MemoryEditorInstance): Boolean =
        Jni.isOptShowOptions((e as JniMemoryEditorInstance).ptr)

    actual fun setOptShowOptions(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOptShowOptions((e as JniMemoryEditorInstance).ptr, value)

    actual fun isOptShowDataPreview(e: MemoryEditorInstance): Boolean =
        Jni.isOptShowDataPreview((e as JniMemoryEditorInstance).ptr)

    actual fun setOptShowDataPreview(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOptShowDataPreview((e as JniMemoryEditorInstance).ptr, value)

    actual fun isOptShowHexII(e: MemoryEditorInstance): Boolean =
        Jni.isOptShowHexII((e as JniMemoryEditorInstance).ptr)

    actual fun setOptShowHexII(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOptShowHexII((e as JniMemoryEditorInstance).ptr, value)

    actual fun isOptShowAscii(e: MemoryEditorInstance): Boolean =
        Jni.isOptShowAscii((e as JniMemoryEditorInstance).ptr)

    actual fun setOptShowAscii(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOptShowAscii((e as JniMemoryEditorInstance).ptr, value)

    actual fun isOptGreyOutZeroes(e: MemoryEditorInstance): Boolean =
        Jni.isOptGreyOutZeroes((e as JniMemoryEditorInstance).ptr)

    actual fun setOptGreyOutZeroes(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOptGreyOutZeroes((e as JniMemoryEditorInstance).ptr, value)

    actual fun isOptUpperCaseHex(e: MemoryEditorInstance): Boolean =
        Jni.isOptUpperCaseHex((e as JniMemoryEditorInstance).ptr)

    actual fun setOptUpperCaseHex(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOptUpperCaseHex((e as JniMemoryEditorInstance).ptr, value)

    actual fun getOptMidColsCount(e: MemoryEditorInstance): Int =
        Jni.getOptMidColsCount((e as JniMemoryEditorInstance).ptr)

    actual fun setOptMidColsCount(e: MemoryEditorInstance, value: Int) =
        Jni.setOptMidColsCount((e as JniMemoryEditorInstance).ptr, value)

    actual fun getOptAddrDigitsCount(e: MemoryEditorInstance): Int =
        Jni.getOptAddrDigitsCount((e as JniMemoryEditorInstance).ptr)

    actual fun setOptAddrDigitsCount(e: MemoryEditorInstance, value: Int) =
        Jni.setOptAddrDigitsCount((e as JniMemoryEditorInstance).ptr, value)

    actual fun getOptFooterExtraHeight(e: MemoryEditorInstance): Float =
        Jni.getOptFooterExtraHeight((e as JniMemoryEditorInstance).ptr)

    actual fun setOptFooterExtraHeight(e: MemoryEditorInstance, value: Float) =
        Jni.setOptFooterExtraHeight((e as JniMemoryEditorInstance).ptr, value)

    actual fun getHighlightColor(e: MemoryEditorInstance): Int =
        Jni.getHighlightColor((e as JniMemoryEditorInstance).ptr)

    actual fun setHighlightColor(e: MemoryEditorInstance, value: Int) =
        Jni.setHighlightColor((e as JniMemoryEditorInstance).ptr, value)

    actual fun isMouseHovered(e: MemoryEditorInstance): Boolean =
        Jni.isMouseHovered((e as JniMemoryEditorInstance).ptr)

    actual fun mouseHoveredAddr(e: MemoryEditorInstance): Long =
        Jni.mouseHoveredAddr((e as JniMemoryEditorInstance).ptr)
}
