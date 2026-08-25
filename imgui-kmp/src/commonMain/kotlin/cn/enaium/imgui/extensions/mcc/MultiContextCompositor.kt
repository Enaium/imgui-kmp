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

/**
 * A multi-context compositor instance wrapping imgui_club's ImGuiMultiContextCompositor;
 * close() calls [MultiContextCompositor.destroy].
 */
interface MccInstance : AutoCloseable

/**
 * Kotlin bindings for imgui_club's ImGuiMultiContextCompositor, inside the
 * cn.enaium.imgui.extensions.mcc package.
 *
 * Manages z-order, input routing and drag-and-drop between multiple Dear ImGui
 * contexts rendered simultaneously.
 *
 * Frame integration order (per frame):
 * ```
 * MultiContextCompositor.preNewFrameUpdateAll(e)
 * for each context ctx:
 *     ImGui.setCurrentContext(ctx)
 *     ImGui.newFrame()
 *     MultiContextCompositor.postNewFrameUpdateOne(e, ctx)
 * // ... submit widgets per context ...
 * for each context ctx:
 *     ImGui.setCurrentContext(ctx)
 *     ImGui.render() // EndFrame()
 * MultiContextCompositor.postEndFrameUpdateAll(e)
 * ```
 */
expect object MultiContextCompositor {
    fun create(): MccInstance
    fun destroy(e: MccInstance? = null)

    /** Add a context to be managed by this compositor. */
    fun addContext(e: MccInstance, context: ImGuiContext)

    /** Remove a context from this compositor. */
    fun removeContext(e: MccInstance, context: ImGuiContext)

    /** Number of contexts currently managed by this compositor. */
    fun getContextCount(e: MccInstance): Int

    /** Call at a shared sync point before calling NewFrame() on any context. */
    fun preNewFrameUpdateAll(e: MccInstance)

    /** Call after calling NewFrame() on the given context. */
    fun postNewFrameUpdateOne(e: MccInstance, context: ImGuiContext)

    /** Call at a shared sync point after calling EndFrame()/Render() on all contexts. */
    fun postEndFrameUpdateAll(e: MccInstance)

    /** Debug display showing the state of the compositor. */
    fun showDebugWindow(e: MccInstance)
}
