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

@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlin.experimental.ExperimentalNativeApi::class,
)

package cn.enaium.imgui.extensions.nodeeditor

import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import imgui.*
import kotlinx.cinterop.*

// =========================================================================
// Helpers
// =========================================================================

private inline fun <T> withVec4(value: ImVec4, block: (CValue<imgui_vec4>) -> T): T = memScoped {
    val v = alloc<imgui_vec4>()
    v.x = value.x
    v.y = value.y
    v.z = value.z
    v.w = value.w
    block(v.readValue())
}

/** Writes back a length-1 id array from an int64_t out-parameter. */
private fun writeIdBack(arr: LongArray?, ptr: CPointer<*>?) {
    if (arr != null && ptr != null) {
        @Suppress("UNCHECKED_CAST")
        val typed = ptr as CPointer<LongVar>
        arr[0] = typed[0]
    }
}

private fun vec2ToKotlin(v: CValue<imgui_vec2>): ImVec2 = v.useContents { ImVec2(x, y) }

private fun readIds(ptr: CPointer<LongVar>, size: Int): LongArray =
    LongArray(size) { i -> ptr[i] }

// =========================================================================
// Native (cinterop) actual implementations
// =========================================================================

internal class NativeNodeEditorContext(internal val ptr: CPointer<ne_context>?) : NodeEditorContext {
    override fun close() {
        ne_destroy_editor(ptr)
    }
}

actual object NodeEditor {
    actual fun createEditor(): NodeEditorContext {
        val ptr = ne_create_editor()
            ?: error("ne_create_editor returned null")
        return NativeNodeEditorContext(ptr)
    }

    actual fun destroyEditor(context: NodeEditorContext?) {
        if (context != null) {
            ne_destroy_editor((context as NativeNodeEditorContext).ptr)
        } else {
            ne_destroy_editor(null)
        }
    }

    actual fun getCurrentEditor(): NodeEditorContext? {
        val ptr = ne_get_current_editor()
        return if (ptr != null) NativeNodeEditorContext(ptr) else null
    }

    actual fun setCurrentEditor(context: NodeEditorContext) {
        ne_set_current_editor((context as NativeNodeEditorContext).ptr)
    }

    actual fun begin(id: String, size: ImVec2) = ne_begin(id, size.x, size.y)

    actual fun end() = ne_end()

    actual fun beginNode(id: Long) = ne_begin_node(id)
    actual fun beginPin(id: Long, kind: Int) = ne_begin_pin(id, kind)

    actual fun pinRect(a: ImVec2, b: ImVec2) = ne_pin_rect(a.x, a.y, b.x, b.y)

    actual fun pinPivotRect(a: ImVec2, b: ImVec2) = ne_pin_pivot_rect(a.x, a.y, b.x, b.y)

    actual fun pinPivotSize(size: ImVec2) = ne_pin_pivot_size(size.x, size.y)

    actual fun pinPivotScale(scale: ImVec2) = ne_pin_pivot_scale(scale.x, scale.y)

    actual fun pinPivotAlignment(alignment: ImVec2) =
        ne_pin_pivot_alignment(alignment.x, alignment.y)

    actual fun endPin() = ne_end_pin()
    actual fun group(size: ImVec2) = ne_group(size.x, size.y)
    actual fun endNode() = ne_end_node()

    actual fun beginGroupHint(nodeId: Long): Boolean = ne_begin_group_hint(nodeId)
    actual fun getGroupMin(): ImVec2 = vec2ToKotlin(ne_get_group_min())
    actual fun getGroupMax(): ImVec2 = vec2ToKotlin(ne_get_group_max())
    actual fun endGroupHint() = ne_end_group_hint()

    actual fun getNodeBackgroundDrawList(nodeId: Long): Long =
        ne_get_node_background_draw_list(nodeId)?.rawValue?.toLong() ?: 0L

    actual fun link(
        id: Long,
        startPinId: Long,
        endPinId: Long,
        color: ImVec4,
        thickness: Float,
    ): Boolean = withVec4(color) { c ->
        ne_link(id, startPinId, endPinId, c, thickness)
    }

    actual fun flow(linkId: Long, direction: Int) = ne_flow(linkId, direction)

    actual fun beginCreate(color: ImVec4, thickness: Float): Boolean = withVec4(color) { c ->
        ne_begin_create(c, thickness)
    }

    actual fun queryNewLink(
        startPinId: LongArray,
        endPinId: LongArray,
        color: ImVec4,
        thickness: Float,
    ): Boolean = withVec4(color) { c ->
        memScoped {
            val start = allocArray<LongVar>(1)
            val end = allocArray<LongVar>(1)
            start[0] = startPinId.getOrElse(0) { 0L }
            end[0] = endPinId.getOrElse(0) { 0L }
            val result = ne_query_new_link_styled(start, end, c, thickness)
            writeIdBack(startPinId, start)
            writeIdBack(endPinId, end)
            result
        }
    }

    actual fun queryNewNode(pinId: LongArray, color: ImVec4, thickness: Float): Boolean =
        withVec4(color) { c ->
            memScoped {
                val pin = allocArray<LongVar>(1)
                pin[0] = pinId.getOrElse(0) { 0L }
                val result = ne_query_new_node_styled(pin, c, thickness)
                writeIdBack(pinId, pin)
                result
            }
        }

    actual fun acceptNewItem(color: ImVec4?, thickness: Float): Boolean = when (color) {
        null -> ne_accept_new_item()
        else -> withVec4(color) { c -> ne_accept_new_item_ex(c, thickness) }
    }

    actual fun rejectNewItem(color: ImVec4?, thickness: Float): Unit = when (color) {
        null -> ne_reject_new_item()
        else -> withVec4(color) { c -> ne_reject_new_item_ex(c, thickness) }
    }

    actual fun endCreate() = ne_end_create()

    actual fun beginDelete(): Boolean = ne_begin_delete()

    actual fun queryDeletedLink(
        linkId: LongArray,
        startPinId: LongArray?,
        endPinId: LongArray?,
    ): Boolean = memScoped {
        val link = alloc<ne_link_idVar>()
        link.value = linkId.getOrElse(0) { 0L }
        val start = alloc<ne_pin_idVar>()
        val end = alloc<ne_pin_idVar>()
        start.value = 0L
        end.value = 0L
        val result = ne_query_deleted_link(link.ptr, start.ptr, end.ptr)
        writeIdBack(linkId, link.ptr)
        startPinId?.let { it[0] = start.value }
        endPinId?.let { it[0] = end.value }
        result
    }

    actual fun queryDeletedNode(nodeId: LongArray): Boolean = memScoped {
        val node = alloc<ne_node_idVar>()
        node.value = nodeId.getOrElse(0) { 0L }
        val result = ne_query_deleted_node(node.ptr)
        writeIdBack(nodeId, node.ptr)
        result
    }

    actual fun acceptDeletedItem(deleteDependencies: Boolean): Unit =
        ne_accept_deleted_item(deleteDependencies).let { }

    actual fun rejectDeletedItem() = ne_reject_deleted_item()
    actual fun endDelete() = ne_end_delete()

    actual fun setNodePosition(nodeId: Long, editorPosition: ImVec2) =
        ne_set_node_position(nodeId, editorPosition.x, editorPosition.y)

    actual fun getNodePosition(nodeId: Long): ImVec2 = vec2ToKotlin(ne_get_node_position(nodeId))
    actual fun getNodeSize(nodeId: Long): ImVec2 = vec2ToKotlin(ne_get_node_size(nodeId))

    actual fun setGroupSize(nodeId: Long, size: ImVec2) =
        ne_set_group_size(nodeId, size.x, size.y)

    actual fun centerNodeOnScreen(nodeId: Long) = ne_center_node_on_screen(nodeId)
    actual fun setNodeZPosition(nodeId: Long, z: Float) = ne_set_node_z_position(nodeId, z)
    actual fun getNodeZPosition(nodeId: Long): Float = ne_get_node_z_position(nodeId)

    actual fun suspendEditor() = ne_suspend()
    actual fun resumeEditor() = ne_resume()
    actual fun isSuspended(): Boolean = ne_is_suspended()

    actual fun isActive(): Boolean = ne_is_active()

    actual fun hasSelectionChanged(): Boolean = ne_has_selection_changed()
    actual fun getSelectedObjectCount(): Int = ne_get_selected_object_count()
    actual fun getSelectedNodes(size: Int): LongArray = memScoped {
        val arr = allocArray<LongVar>(size)
        val count = alloc<IntVar>()
        ne_get_selected_nodes(arr, size, count.ptr)
        readIds(arr, minOf(count.value, size))
    }

    actual fun getSelectedLinks(size: Int): LongArray = memScoped {
        val arr = allocArray<LongVar>(size)
        val count = alloc<IntVar>()
        ne_get_selected_links(arr, size, count.ptr)
        readIds(arr, minOf(count.value, size))
    }

    actual fun isNodeSelected(nodeId: Long): Boolean = ne_is_node_selected(nodeId)
    actual fun isLinkSelected(linkId: Long): Boolean = ne_is_link_selected(linkId)
    actual fun clearSelection() = ne_clear_selection()
    actual fun selectNode(nodeId: Long, append: Boolean) = ne_select_node(nodeId, append)
    actual fun selectLink(linkId: Long, append: Boolean) = ne_select_link(linkId, append)
    actual fun deselectNode(nodeId: Long) = ne_deselect_node(nodeId)
    actual fun deselectLink(linkId: Long) = ne_deselect_link(linkId)

    actual fun deleteNode(nodeId: Long): Boolean = ne_delete_node(nodeId)
    actual fun deleteLink(linkId: Long): Boolean = ne_delete_link(linkId)

    actual fun hasAnyLinks(id: Long): Boolean = ne_has_any_links(id)
    actual fun breakLinks(id: Long): Int = ne_break_links(id)

    actual fun navigateToContent(duration: Float) = ne_navigate_to_content(duration)
    actual fun navigateToSelection(zoomIn: Boolean, duration: Float) = ne_navigate_to_selection(zoomIn, duration)

    actual fun showNodeContextMenu(nodeId: LongArray): Boolean = memScoped {
        val node = alloc<ne_node_idVar>()
        node.value = nodeId.getOrElse(0) { 0L }
        val result = ne_show_node_context_menu(node.ptr)
        writeIdBack(nodeId, node.ptr)
        result
    }

    actual fun showPinContextMenu(pinId: LongArray): Boolean = memScoped {
        val pin = alloc<ne_pin_idVar>()
        pin.value = pinId.getOrElse(0) { 0L }
        val result = ne_show_pin_context_menu(pin.ptr)
        writeIdBack(pinId, pin.ptr)
        result
    }

    actual fun showLinkContextMenu(linkId: LongArray): Boolean = memScoped {
        val link = alloc<ne_link_idVar>()
        link.value = linkId.getOrElse(0) { 0L }
        val result = ne_show_link_context_menu(link.ptr)
        writeIdBack(linkId, link.ptr)
        result
    }

    actual fun showBackgroundContextMenu(): Boolean = ne_show_background_context_menu()

    actual fun enableShortcuts(enable: Boolean) = ne_enable_shortcuts(enable)
    actual fun areShortcutsEnabled(): Boolean = ne_are_shortcuts_enabled()

    actual fun beginShortcut(): Boolean = ne_begin_shortcut()
    actual fun acceptCut(): Boolean = ne_accept_cut()
    actual fun acceptCopy(): Boolean = ne_accept_copy()
    actual fun acceptPaste(): Boolean = ne_accept_paste()
    actual fun acceptDuplicate(): Boolean = ne_accept_duplicate()
    actual fun acceptCreateNode(): Boolean = ne_accept_create_node()
    actual fun getActionContextSize(): Int = ne_get_action_context_size()
    actual fun getActionContextNodes(size: Int): LongArray = memScoped {
        val arr = allocArray<LongVar>(size)
        val count = alloc<IntVar>()
        ne_get_action_context_nodes(arr, size, count.ptr)
        readIds(arr, minOf(count.value, size))
    }

    actual fun getActionContextLinks(size: Int): LongArray = memScoped {
        val arr = allocArray<LongVar>(size)
        val count = alloc<IntVar>()
        ne_get_action_context_links(arr, size, count.ptr)
        readIds(arr, minOf(count.value, size))
    }

    actual fun endShortcut() = ne_end_shortcut()

    actual fun getCurrentZoom(): Float = ne_get_current_zoom()

    actual fun getHoveredNode(): Long = ne_get_hovered_node()
    actual fun getHoveredPin(): Long = ne_get_hovered_pin()
    actual fun getHoveredLink(): Long = ne_get_hovered_link()
    actual fun getDoubleClickedNode(): Long = ne_get_double_clicked_node()
    actual fun getDoubleClickedPin(): Long = ne_get_double_clicked_pin()
    actual fun getDoubleClickedLink(): Long = ne_get_double_clicked_link()
    actual fun isBackgroundClicked(): Boolean = ne_is_background_clicked()
    actual fun isBackgroundDoubleClicked(): Boolean = ne_is_background_double_clicked()
    actual fun getBackgroundClickButtonIndex(): Int = ne_get_background_click_button_index()
    actual fun getBackgroundDoubleClickButtonIndex(): Int = ne_get_background_double_click_button_index()

    actual fun getLinkPins(linkId: Long, startPinId: LongArray?, endPinId: LongArray?): Boolean = memScoped {
        val start = alloc<LongVar>()
        val end = alloc<LongVar>()
        start.value = 0L
        end.value = 0L
        val result = ne_get_link_pins(linkId, start.ptr, end.ptr)
        startPinId?.let { it[0] = start.value }
        endPinId?.let { it[0] = end.value }
        result
    }

    actual fun pinHadAnyLinks(pinId: Long): Boolean = ne_pin_had_any_links(pinId)

    actual fun getScreenSize(): ImVec2 = vec2ToKotlin(ne_get_screen_size())

    actual fun screenToCanvas(pos: ImVec2): ImVec2 =
        vec2ToKotlin(ne_screen_to_canvas(pos.x, pos.y))

    actual fun canvasToScreen(pos: ImVec2): ImVec2 =
        vec2ToKotlin(ne_canvas_to_screen(pos.x, pos.y))

    actual fun getNodeCount(): Int = ne_get_node_count()
    actual fun getOrderedNodeIds(size: Int): LongArray = memScoped {
        val arr = allocArray<LongVar>(size)
        val count = alloc<IntVar>()
        ne_get_ordered_node_ids(arr, size, count.ptr)
        readIds(arr, minOf(count.value, size))
    }

    actual fun pushStyleColor(idx: Int, color: ImVec4) = withVec4(color) { c ->
        ne_push_style_color(idx, c)
    }

    actual fun popStyleColor(count: Int) = ne_pop_style_color(count)
    actual fun pushStyleVarFloat(idx: Int, value: Float) = ne_push_style_var_float(idx, value)
    actual fun pushStyleVarVec2(idx: Int, value: ImVec2) =
        ne_push_style_var_vec2(idx, value.x, value.y)

    actual fun pushStyleVarVec4(idx: Int, value: ImVec4) =
        ne_push_style_var_vec4(idx, value.x, value.y, value.z, value.w)

    actual fun popStyleVar(count: Int) = ne_pop_style_var(count)
}
