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

internal expect object Jni {

    fun create(): Long
    fun destroy(ptr: Long)

    fun drawWindow(e: Long, title: String, data: ByteArray, size: Int, baseAddr: Long)
    fun drawContents(e: Long, data: ByteArray, size: Int, baseAddr: Long)

    // Settings
    fun isOpen(e: Long): Boolean
    fun setOpen(e: Long, value: Boolean)
    fun isReadOnly(e: Long): Boolean
    fun setReadOnly(e: Long, value: Boolean)
    fun getCols(e: Long): Int
    fun setCols(e: Long, value: Int)
    fun isOptShowOptions(e: Long): Boolean
    fun setOptShowOptions(e: Long, value: Boolean)
    fun isOptShowDataPreview(e: Long): Boolean
    fun setOptShowDataPreview(e: Long, value: Boolean)
    fun isOptShowHexII(e: Long): Boolean
    fun setOptShowHexII(e: Long, value: Boolean)
    fun isOptShowAscii(e: Long): Boolean
    fun setOptShowAscii(e: Long, value: Boolean)
    fun isOptGreyOutZeroes(e: Long): Boolean
    fun setOptGreyOutZeroes(e: Long, value: Boolean)
    fun isOptUpperCaseHex(e: Long): Boolean
    fun setOptUpperCaseHex(e: Long, value: Boolean)
    fun getOptMidColsCount(e: Long): Int
    fun setOptMidColsCount(e: Long, value: Int)
    fun getOptAddrDigitsCount(e: Long): Int
    fun setOptAddrDigitsCount(e: Long, value: Int)
    fun getOptFooterExtraHeight(e: Long): Float
    fun setOptFooterExtraHeight(e: Long, value: Float)
    fun getHighlightColor(e: Long): Int
    fun setHighlightColor(e: Long, value: Int)

    // Public read-only data
    fun isMouseHovered(e: Long): Boolean
    fun mouseHoveredAddr(e: Long): Long
}
