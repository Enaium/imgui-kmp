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

package cn.enaium.imgui.extensions.markdown

// JNI bridge
// =========================================================================

internal expect object Jni {

    fun create(): Long
    fun destroy(configPtr: Long)
    fun setLinkIcon(configPtr: Long, icon: String)
    fun setHeading(configPtr: Long, level: Int, font: Long, separator: Boolean)
    fun setFormatFlags(configPtr: Long, flags: Int)
    fun setLinkCallback(configPtr: Long, activate: Boolean)
    fun setTooltipCallback(configPtr: Long, activate: Boolean)
    fun setImageCallback(configPtr: Long, activate: Boolean)
    fun render(configPtr: Long, markdown: String)
}
