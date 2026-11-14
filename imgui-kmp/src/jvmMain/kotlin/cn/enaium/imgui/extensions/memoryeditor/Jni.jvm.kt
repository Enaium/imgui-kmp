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

internal actual object Jni {
    init {
        cn.enaium.imgui.NativeLoader.load()
    }

    actual external fun create(): Long
    actual external fun destroy(ptr: Long)

    actual external fun drawWindow(e: Long, title: String, data: ByteArray, size: Int, baseAddr: Long)
    actual external fun drawContents(e: Long, data: ByteArray, size: Int, baseAddr: Long)

    // Settings
    actual external fun isOpen(e: Long): Boolean
    actual external fun setOpen(e: Long, value: Boolean)
    actual external fun isReadOnly(e: Long): Boolean
    actual external fun setReadOnly(e: Long, value: Boolean)
    actual external fun getCols(e: Long): Int
    actual external fun setCols(e: Long, value: Int)
    actual external fun isOptShowOptions(e: Long): Boolean
    actual external fun setOptShowOptions(e: Long, value: Boolean)
    actual external fun isOptShowDataPreview(e: Long): Boolean
    actual external fun setOptShowDataPreview(e: Long, value: Boolean)
    actual external fun isOptShowHexII(e: Long): Boolean
    actual external fun setOptShowHexII(e: Long, value: Boolean)
    actual external fun isOptShowAscii(e: Long): Boolean
    actual external fun setOptShowAscii(e: Long, value: Boolean)
    actual external fun isOptGreyOutZeroes(e: Long): Boolean
    actual external fun setOptGreyOutZeroes(e: Long, value: Boolean)
    actual external fun isOptUpperCaseHex(e: Long): Boolean
    actual external fun setOptUpperCaseHex(e: Long, value: Boolean)
    actual external fun getOptMidColsCount(e: Long): Int
    actual external fun setOptMidColsCount(e: Long, value: Int)
    actual external fun getOptAddrDigitsCount(e: Long): Int
    actual external fun setOptAddrDigitsCount(e: Long, value: Int)
    actual external fun getOptFooterExtraHeight(e: Long): Float
    actual external fun setOptFooterExtraHeight(e: Long, value: Float)
    actual external fun getHighlightColor(e: Long): Int
    actual external fun setHighlightColor(e: Long, value: Int)

    // Public read-only data
    actual external fun isMouseHovered(e: Long): Boolean
    actual external fun mouseHoveredAddr(e: Long): Long
}
