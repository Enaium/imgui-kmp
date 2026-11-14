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

package cn.enaium.imgui.extensions.nodeeditor

import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4

// =========================================================================
// JNI bridge (symbols: Java_cn_enaium_imgui_extensions_nodeeditor_Jni_)
// =========================================================================


// =========================================================================
// JVM/Android actual implementations
// =========================================================================

internal class JniNodeEditorContext(internal val ptr: Long) : NodeEditorContext {
    override fun close() {
        Jni.destroyEditor(ptr)
    }
}

private val DEFAULT_COLOR = ImVec4(1f, 1f, 1f, 1f)

actual object NodeEditor {
    actual fun createEditor(): NodeEditorContext = JniNodeEditorContext(Jni.createEditor())

    actual fun destroyEditor(context: NodeEditorContext?) {
        Jni.destroyEditor(if (context != null) (context as JniNodeEditorContext).ptr else 0L)
    }

    actual fun getCurrentEditor(): NodeEditorContext? {
        val ptr = Jni.getCurrentEditor()
        return if (ptr != 0L) JniNodeEditorContext(ptr) else null
    }

    actual fun setCurrentEditor(context: NodeEditorContext) {
        Jni.setCurrentEditor((context as JniNodeEditorContext).ptr)
    }

    actual fun begin(id: String, size: ImVec2) = Jni.begin(id, size.x, size.y)
    actual fun end() = Jni.end()

    actual fun beginNode(id: Long) = Jni.beginNode(id)
    actual fun beginPin(id: Long, kind: Int) = Jni.beginPin(id, kind)
    actual fun pinRect(a: ImVec2, b: ImVec2) = Jni.pinRect(a.x, a.y, b.x, b.y)
    actual fun pinPivotRect(a: ImVec2, b: ImVec2) = Jni.pinPivotRect(a.x, a.y, b.x, b.y)
    actual fun pinPivotSize(size: ImVec2) = Jni.pinPivotSize(size.x, size.y)
    actual fun pinPivotScale(scale: ImVec2) = Jni.pinPivotScale(scale.x, scale.y)
    actual fun pinPivotAlignment(alignment: ImVec2) = Jni.pinPivotAlignment(alignment.x, alignment.y)
    actual fun endPin() = Jni.endPin()
    actual fun group(size: ImVec2) = Jni.group(size.x, size.y)
    actual fun endNode() = Jni.endNode()

    actual fun beginGroupHint(nodeId: Long): Boolean = Jni.beginGroupHint(nodeId)

    actual fun getGroupMin(): ImVec2 = Jni.getGroupMin().let { ImVec2(it[0], it[1]) }

    actual fun getGroupMax(): ImVec2 = Jni.getGroupMax().let { ImVec2(it[0], it[1]) }

    actual fun endGroupHint() = Jni.endGroupHint()

    actual fun getNodeBackgroundDrawList(nodeId: Long): Long = Jni.getNodeBackgroundDrawList(nodeId)

    actual fun link(id: Long, startPinId: Long, endPinId: Long, color: ImVec4, thickness: Float): Boolean =
        Jni.link(id, startPinId, endPinId, color.x, color.y, color.z, color.w, thickness)

    actual fun flow(linkId: Long, direction: Int) = Jni.flow(linkId, direction)

    actual fun beginCreate(color: ImVec4, thickness: Float): Boolean =
        Jni.beginCreate(color.x, color.y, color.z, color.w, thickness)

    actual fun queryNewLink(startPinId: LongArray, endPinId: LongArray, color: ImVec4, thickness: Float): Boolean =
        Jni.queryNewLink(startPinId, endPinId, color.x, color.y, color.z, color.w, thickness)

    actual fun queryNewNode(pinId: LongArray, color: ImVec4, thickness: Float): Boolean =
        Jni.queryNewNode(pinId, color.x, color.y, color.z, color.w, thickness)

    actual fun acceptNewItem(color: ImVec4?, thickness: Float): Boolean {
        val c = color ?: DEFAULT_COLOR
        return Jni.acceptNewItem(c.x, c.y, c.z, c.w, thickness)
    }

    actual fun rejectNewItem(color: ImVec4?, thickness: Float) {
        val c = color ?: DEFAULT_COLOR
        Jni.rejectNewItem(c.x, c.y, c.z, c.w, thickness)
    }

    actual fun endCreate() = Jni.endCreate()

    actual fun beginDelete(): Boolean = Jni.beginDelete()

    actual fun queryDeletedLink(linkId: LongArray, startPinId: LongArray?, endPinId: LongArray?): Boolean =
        Jni.queryDeletedLink(linkId, startPinId, endPinId)

    actual fun queryDeletedNode(nodeId: LongArray): Boolean = Jni.queryDeletedNode(nodeId)

    actual fun acceptDeletedItem(deleteDependencies: Boolean) = Jni.acceptDeletedItem(deleteDependencies)

    actual fun rejectDeletedItem() = Jni.rejectDeletedItem()

    actual fun endDelete() = Jni.endDelete()

    actual fun setNodePosition(nodeId: Long, editorPosition: ImVec2) =
        Jni.setNodePosition(nodeId, editorPosition.x, editorPosition.y)

    actual fun getNodePosition(nodeId: Long): ImVec2 =
        Jni.getNodePosition(nodeId).let { ImVec2(it[0], it[1]) }

    actual fun getNodeSize(nodeId: Long): ImVec2 =
        Jni.getNodeSize(nodeId).let { ImVec2(it[0], it[1]) }

    actual fun setGroupSize(nodeId: Long, size: ImVec2) = Jni.setGroupSize(nodeId, size.x, size.y)

    actual fun centerNodeOnScreen(nodeId: Long) = Jni.centerNodeOnScreen(nodeId)

    actual fun setNodeZPosition(nodeId: Long, z: Float) = Jni.setNodeZPosition(nodeId, z)

    actual fun getNodeZPosition(nodeId: Long): Float = Jni.getNodeZPosition(nodeId)

    actual fun suspendEditor() = Jni.suspend()
    actual fun resumeEditor() = Jni.resume()
    actual fun isSuspended(): Boolean = Jni.isSuspended()

    actual fun isActive(): Boolean = Jni.isActive()

    actual fun hasSelectionChanged(): Boolean = Jni.hasSelectionChanged()
    actual fun getSelectedObjectCount(): Int = Jni.getSelectedObjectCount()

    actual fun getSelectedNodes(size: Int): LongArray = Jni.getSelectedNodes(size)
    actual fun getSelectedLinks(size: Int): LongArray = Jni.getSelectedLinks(size)

    actual fun isNodeSelected(nodeId: Long): Boolean = Jni.isNodeSelected(nodeId)
    actual fun isLinkSelected(linkId: Long): Boolean = Jni.isLinkSelected(linkId)
    actual fun clearSelection() = Jni.clearSelection()
    actual fun selectNode(nodeId: Long, append: Boolean) = Jni.selectNode(nodeId, append)
    actual fun selectLink(linkId: Long, append: Boolean) = Jni.selectLink(linkId, append)
    actual fun deselectNode(nodeId: Long) = Jni.deselectNode(nodeId)
    actual fun deselectLink(linkId: Long) = Jni.deselectLink(linkId)

    actual fun deleteNode(nodeId: Long): Boolean = Jni.deleteNode(nodeId)
    actual fun deleteLink(linkId: Long): Boolean = Jni.deleteLink(linkId)

    actual fun hasAnyLinks(id: Long): Boolean = Jni.hasAnyLinks(id)
    actual fun breakLinks(id: Long): Int = Jni.breakLinks(id)

    actual fun navigateToContent(duration: Float) = Jni.navigateToContent(duration)
    actual fun navigateToSelection(zoomIn: Boolean, duration: Float) = Jni.navigateToSelection(zoomIn, duration)

    actual fun showNodeContextMenu(nodeId: LongArray): Boolean = Jni.showNodeContextMenu(nodeId)
    actual fun showPinContextMenu(pinId: LongArray): Boolean = Jni.showPinContextMenu(pinId)
    actual fun showLinkContextMenu(linkId: LongArray): Boolean = Jni.showLinkContextMenu(linkId)
    actual fun showBackgroundContextMenu(): Boolean = Jni.showBackgroundContextMenu()

    actual fun enableShortcuts(enable: Boolean) = Jni.enableShortcuts(enable)
    actual fun areShortcutsEnabled(): Boolean = Jni.areShortcutsEnabled()

    actual fun beginShortcut(): Boolean = Jni.beginShortcut()
    actual fun acceptCut(): Boolean = Jni.acceptCut()
    actual fun acceptCopy(): Boolean = Jni.acceptCopy()
    actual fun acceptPaste(): Boolean = Jni.acceptPaste()
    actual fun acceptDuplicate(): Boolean = Jni.acceptDuplicate()
    actual fun acceptCreateNode(): Boolean = Jni.acceptCreateNode()
    actual fun getActionContextSize(): Int = Jni.getActionContextSize()
    actual fun getActionContextNodes(size: Int): LongArray = Jni.getActionContextNodes(size)
    actual fun getActionContextLinks(size: Int): LongArray = Jni.getActionContextLinks(size)
    actual fun endShortcut() = Jni.endShortcut()

    actual fun getCurrentZoom(): Float = Jni.getCurrentZoom()

    actual fun getHoveredNode(): Long = Jni.getHoveredNode()
    actual fun getHoveredPin(): Long = Jni.getHoveredPin()
    actual fun getHoveredLink(): Long = Jni.getHoveredLink()
    actual fun getDoubleClickedNode(): Long = Jni.getDoubleClickedNode()
    actual fun getDoubleClickedPin(): Long = Jni.getDoubleClickedPin()
    actual fun getDoubleClickedLink(): Long = Jni.getDoubleClickedLink()
    actual fun isBackgroundClicked(): Boolean = Jni.isBackgroundClicked()
    actual fun isBackgroundDoubleClicked(): Boolean = Jni.isBackgroundDoubleClicked()
    actual fun getBackgroundClickButtonIndex(): Int = Jni.getBackgroundClickButtonIndex()
    actual fun getBackgroundDoubleClickButtonIndex(): Int = Jni.getBackgroundDoubleClickButtonIndex()
    actual fun getLinkPins(linkId: Long, startPinId: LongArray?, endPinId: LongArray?): Boolean =
        Jni.getLinkPins(linkId, startPinId, endPinId)

    actual fun pinHadAnyLinks(pinId: Long): Boolean = Jni.pinHadAnyLinks(pinId)

    actual fun getScreenSize(): ImVec2 = Jni.getScreenSize().let { ImVec2(it[0], it[1]) }

    actual fun screenToCanvas(pos: ImVec2): ImVec2 =
        Jni.screenToCanvas(pos.x, pos.y).let { ImVec2(it[0], it[1]) }

    actual fun canvasToScreen(pos: ImVec2): ImVec2 =
        Jni.canvasToScreen(pos.x, pos.y).let { ImVec2(it[0], it[1]) }

    actual fun getNodeCount(): Int = Jni.getNodeCount()
    actual fun getOrderedNodeIds(size: Int): LongArray = Jni.getOrderedNodeIds(size)

    actual fun pushStyleColor(idx: Int, color: ImVec4) =
        Jni.pushStyleColor(idx, color.x, color.y, color.z, color.w)

    actual fun popStyleColor(count: Int) = Jni.popStyleColor(count)
    actual fun pushStyleVarFloat(idx: Int, value: Float) = Jni.pushStyleVarFloat(idx, value)
    actual fun pushStyleVarVec2(idx: Int, value: ImVec2) = Jni.pushStyleVarVec2(idx, value.x, value.y)
    actual fun pushStyleVarVec4(idx: Int, value: ImVec4) =
        Jni.pushStyleVarVec4(idx, value.x, value.y, value.z, value.w)

    actual fun popStyleVar(count: Int) = Jni.popStyleVar(count)
}
