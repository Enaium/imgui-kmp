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

internal object Jni {
    init {
        cn.enaium.imgui.NativeLoader.load()
    }

    external fun create(): Long
    external fun destroy(ptr: Long)

    external fun drawWindow(e: Long, title: String, data: ByteArray, size: Int, baseAddr: Long)
    external fun drawContents(e: Long, data: ByteArray, size: Int, baseAddr: Long)

    // Settings
    external fun isOpen(e: Long): Boolean
    external fun setOpen(e: Long, value: Boolean)
    external fun isReadOnly(e: Long): Boolean
    external fun setReadOnly(e: Long, value: Boolean)
    external fun getCols(e: Long): Int
    external fun setCols(e: Long, value: Int)
    external fun isOptShowOptions(e: Long): Boolean
    external fun setOptShowOptions(e: Long, value: Boolean)
    external fun isOptShowDataPreview(e: Long): Boolean
    external fun setOptShowDataPreview(e: Long, value: Boolean)
    external fun isOptShowHexII(e: Long): Boolean
    external fun setOptShowHexII(e: Long, value: Boolean)
    external fun isOptShowAscii(e: Long): Boolean
    external fun setOptShowAscii(e: Long, value: Boolean)
    external fun isOptGreyOutZeroes(e: Long): Boolean
    external fun setOptGreyOutZeroes(e: Long, value: Boolean)
    external fun isOptUpperCaseHex(e: Long): Boolean
    external fun setOptUpperCaseHex(e: Long, value: Boolean)
    external fun getOptMidColsCount(e: Long): Int
    external fun setOptMidColsCount(e: Long, value: Int)
    external fun getOptAddrDigitsCount(e: Long): Int
    external fun setOptAddrDigitsCount(e: Long, value: Int)
    external fun getOptFooterExtraHeight(e: Long): Float
    external fun setOptFooterExtraHeight(e: Long, value: Float)
    external fun getHighlightColor(e: Long): Int
    external fun setHighlightColor(e: Long, value: Int)

    // Public read-only data
    external fun isMouseHovered(e: Long): Boolean
    external fun mouseHoveredAddr(e: Long): Long
}

// =========================================================================
// JVM/Android actual implementations
// =========================================================================

internal class JvmMemoryEditorInstance(internal val ptr: Long) : MemoryEditorInstance {
    override fun close() {
        Jni.destroy(ptr)
    }
}

actual object MemoryEditor {
    actual fun create(): MemoryEditorInstance {
        Jni.create().let { ptr ->
            require(ptr != 0L) { "me_create returned null" }
            return JvmMemoryEditorInstance(ptr)
        }
    }

    actual fun destroy(e: MemoryEditorInstance?) {
        if (e != null) {
            Jni.destroy((e as JvmMemoryEditorInstance).ptr)
        }
    }

    actual fun drawWindow(e: MemoryEditorInstance, title: String, data: ByteArray, baseAddr: Long) {
        val editor = e as JvmMemoryEditorInstance
        Jni.drawWindow(editor.ptr, title, data, data.size, baseAddr)
    }

    actual fun drawContents(e: MemoryEditorInstance, data: ByteArray, baseAddr: Long) {
        val editor = e as JvmMemoryEditorInstance
        Jni.drawContents(editor.ptr, data, data.size, baseAddr)
    }

    actual fun isOpen(e: MemoryEditorInstance): Boolean =
        Jni.isOpen((e as JvmMemoryEditorInstance).ptr)

    actual fun setOpen(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOpen((e as JvmMemoryEditorInstance).ptr, value)

    actual fun isReadOnly(e: MemoryEditorInstance): Boolean =
        Jni.isReadOnly((e as JvmMemoryEditorInstance).ptr)

    actual fun setReadOnly(e: MemoryEditorInstance, value: Boolean) =
        Jni.setReadOnly((e as JvmMemoryEditorInstance).ptr, value)

    actual fun getCols(e: MemoryEditorInstance): Int =
        Jni.getCols((e as JvmMemoryEditorInstance).ptr)

    actual fun setCols(e: MemoryEditorInstance, value: Int) =
        Jni.setCols((e as JvmMemoryEditorInstance).ptr, value)

    actual fun isOptShowOptions(e: MemoryEditorInstance): Boolean =
        Jni.isOptShowOptions((e as JvmMemoryEditorInstance).ptr)

    actual fun setOptShowOptions(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOptShowOptions((e as JvmMemoryEditorInstance).ptr, value)

    actual fun isOptShowDataPreview(e: MemoryEditorInstance): Boolean =
        Jni.isOptShowDataPreview((e as JvmMemoryEditorInstance).ptr)

    actual fun setOptShowDataPreview(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOptShowDataPreview((e as JvmMemoryEditorInstance).ptr, value)

    actual fun isOptShowHexII(e: MemoryEditorInstance): Boolean =
        Jni.isOptShowHexII((e as JvmMemoryEditorInstance).ptr)

    actual fun setOptShowHexII(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOptShowHexII((e as JvmMemoryEditorInstance).ptr, value)

    actual fun isOptShowAscii(e: MemoryEditorInstance): Boolean =
        Jni.isOptShowAscii((e as JvmMemoryEditorInstance).ptr)

    actual fun setOptShowAscii(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOptShowAscii((e as JvmMemoryEditorInstance).ptr, value)

    actual fun isOptGreyOutZeroes(e: MemoryEditorInstance): Boolean =
        Jni.isOptGreyOutZeroes((e as JvmMemoryEditorInstance).ptr)

    actual fun setOptGreyOutZeroes(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOptGreyOutZeroes((e as JvmMemoryEditorInstance).ptr, value)

    actual fun isOptUpperCaseHex(e: MemoryEditorInstance): Boolean =
        Jni.isOptUpperCaseHex((e as JvmMemoryEditorInstance).ptr)

    actual fun setOptUpperCaseHex(e: MemoryEditorInstance, value: Boolean) =
        Jni.setOptUpperCaseHex((e as JvmMemoryEditorInstance).ptr, value)

    actual fun getOptMidColsCount(e: MemoryEditorInstance): Int =
        Jni.getOptMidColsCount((e as JvmMemoryEditorInstance).ptr)

    actual fun setOptMidColsCount(e: MemoryEditorInstance, value: Int) =
        Jni.setOptMidColsCount((e as JvmMemoryEditorInstance).ptr, value)

    actual fun getOptAddrDigitsCount(e: MemoryEditorInstance): Int =
        Jni.getOptAddrDigitsCount((e as JvmMemoryEditorInstance).ptr)

    actual fun setOptAddrDigitsCount(e: MemoryEditorInstance, value: Int) =
        Jni.setOptAddrDigitsCount((e as JvmMemoryEditorInstance).ptr, value)

    actual fun getOptFooterExtraHeight(e: MemoryEditorInstance): Float =
        Jni.getOptFooterExtraHeight((e as JvmMemoryEditorInstance).ptr)

    actual fun setOptFooterExtraHeight(e: MemoryEditorInstance, value: Float) =
        Jni.setOptFooterExtraHeight((e as JvmMemoryEditorInstance).ptr, value)

    actual fun getHighlightColor(e: MemoryEditorInstance): Int =
        Jni.getHighlightColor((e as JvmMemoryEditorInstance).ptr)

    actual fun setHighlightColor(e: MemoryEditorInstance, value: Int) =
        Jni.setHighlightColor((e as JvmMemoryEditorInstance).ptr, value)

    actual fun isMouseHovered(e: MemoryEditorInstance): Boolean =
        Jni.isMouseHovered((e as JvmMemoryEditorInstance).ptr)

    actual fun mouseHoveredAddr(e: MemoryEditorInstance): Long =
        Jni.mouseHoveredAddr((e as JvmMemoryEditorInstance).ptr)
}
