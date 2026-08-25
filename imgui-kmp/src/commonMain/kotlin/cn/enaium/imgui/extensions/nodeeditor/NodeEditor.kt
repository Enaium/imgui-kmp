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

/**
 * An imgui-node-editor context; close() calls [NodeEditor.destroyEditor].
 */
interface NodeEditorContext : AutoCloseable

/**
 * Kotlin bindings for imgui-node-editor (thedmd/imgui-node-editor),
 * inside the cn.enaium.imgui.extensions.nodeeditor package.
 *
 * Node/Pin/Link ids are plain Long values (0 = invalid).
 */
expect object NodeEditor {
    // ==================== Context ====================
    fun createEditor(): NodeEditorContext
    fun destroyEditor(context: NodeEditorContext?)
    fun getCurrentEditor(): NodeEditorContext?
    fun setCurrentEditor(context: NodeEditorContext)

    // ==================== Begin/End ====================
    fun begin(id: String, size: ImVec2 = ImVec2(0f, 0f))
    fun end()

    // ==================== Nodes and pins ====================
    fun beginNode(id: Long)
    fun beginPin(id: Long, kind: Int)
    fun pinRect(a: ImVec2, b: ImVec2)
    fun pinPivotRect(a: ImVec2, b: ImVec2)
    fun pinPivotSize(size: ImVec2)
    fun pinPivotScale(scale: ImVec2)
    fun pinPivotAlignment(alignment: ImVec2)
    fun endPin()
    fun group(size: ImVec2)
    fun endNode()

    // ==================== Groups hints ====================
    fun beginGroupHint(nodeId: Long): Boolean
    fun getGroupMin(): ImVec2
    fun getGroupMax(): ImVec2
    fun endGroupHint()

    // ==================== Draw list ====================
    /** Returns a raw ImDrawList handle (0 when unavailable). */
    fun getNodeBackgroundDrawList(nodeId: Long): Long

    // ==================== Links ====================
    fun link(
        id: Long,
        startPinId: Long,
        endPinId: Long,
        color: ImVec4 = ImVec4(1f, 1f, 1f, 1f),
        thickness: Float = 1.0f,
    ): Boolean

    fun flow(linkId: Long, direction: Int = NeFlowDirection.FORWARD)

    // ==================== Create new link / node ====================
    fun beginCreate(color: ImVec4 = ImVec4(1f, 1f, 1f, 1f), thickness: Float = 1.0f): Boolean

    /**
     * Queries whether a new link is being dragged. [startPinId] and [endPinId] are
     * length-1 arrays receiving the hovered pin ids (0 when none). Returns true while valid.
     */
    fun queryNewLink(
        startPinId: LongArray,
        endPinId: LongArray,
        color: ImVec4 = ImVec4(1f, 1f, 1f, 1f),
        thickness: Float = 1.0f,
    ): Boolean

    fun queryNewNode(
        pinId: LongArray,
        color: ImVec4 = ImVec4(1f, 1f, 1f, 1f),
        thickness: Float = 1.0f,
    ): Boolean

    fun acceptNewItem(color: ImVec4? = null, thickness: Float = 1.0f): Boolean
    fun rejectNewItem(color: ImVec4? = null, thickness: Float = 1.0f)
    fun endCreate()

    // ==================== Delete nodes / links ====================
    fun beginDelete(): Boolean

    /** [linkId] receives the queried link id; [startPinId]/[endPinId] (optional length-1 arrays) receive its pins. */
    fun queryDeletedLink(linkId: LongArray, startPinId: LongArray? = null, endPinId: LongArray? = null): Boolean
    fun queryDeletedNode(nodeId: LongArray): Boolean
    fun acceptDeletedItem(deleteDependencies: Boolean = true)
    fun rejectDeletedItem()
    fun endDelete()

    // ==================== Node / group geometry ====================
    fun setNodePosition(nodeId: Long, editorPosition: ImVec2)
    fun getNodePosition(nodeId: Long): ImVec2
    fun getNodeSize(nodeId: Long): ImVec2
    fun setGroupSize(nodeId: Long, size: ImVec2)
    fun centerNodeOnScreen(nodeId: Long)
    fun setNodeZPosition(nodeId: Long, z: Float)
    fun getNodeZPosition(nodeId: Long): Float

    // ==================== Suspend / resume ====================
    fun suspendEditor()
    fun resumeEditor()
    fun isSuspended(): Boolean

    fun isActive(): Boolean

    // ==================== Selection ====================
    fun hasSelectionChanged(): Boolean
    fun getSelectedObjectCount(): Int

    /** Fills up to [size] node ids in selection order; returns the actually filled ids. */
    fun getSelectedNodes(size: Int): LongArray
    fun getSelectedLinks(size: Int): LongArray

    fun isNodeSelected(nodeId: Long): Boolean
    fun isLinkSelected(linkId: Long): Boolean
    fun clearSelection()
    fun selectNode(nodeId: Long, append: Boolean = false)
    fun selectLink(linkId: Long, append: Boolean = false)
    fun deselectNode(nodeId: Long)
    fun deselectLink(linkId: Long)

    // ==================== Deletion requests ====================
    fun deleteNode(nodeId: Long): Boolean
    fun deleteLink(linkId: Long): Boolean

    // ==================== Links queries ====================
    /** True when the node or pin (id shared namespace helper) has any connected link. */
    fun hasAnyLinks(id: Long): Boolean

    /** Breaks all links connected to this node or pin; returns broken link count. */
    fun breakLinks(id: Long): Int

    // ==================== Navigation ====================
    fun navigateToContent(duration: Float = -1.0f)
    fun navigateToSelection(zoomIn: Boolean = false, duration: Float = -1.0f)

    // ==================== Context menus ====================
    fun showNodeContextMenu(nodeId: LongArray): Boolean
    fun showPinContextMenu(pinId: LongArray): Boolean
    fun showLinkContextMenu(linkId: LongArray): Boolean
    fun showBackgroundContextMenu(): Boolean

    // ==================== Shortcuts ====================
    fun enableShortcuts(enable: Boolean)
    fun areShortcutsEnabled(): Boolean

    fun beginShortcut(): Boolean
    fun acceptCut(): Boolean
    fun acceptCopy(): Boolean
    fun acceptPaste(): Boolean
    fun acceptDuplicate(): Boolean
    fun acceptCreateNode(): Boolean
    fun getActionContextSize(): Int
    fun getActionContextNodes(size: Int): LongArray
    fun getActionContextLinks(size: Int): LongArray
    fun endShortcut()

    fun getCurrentZoom(): Float

    // ==================== Hover / click queries ====================
    fun getHoveredNode(): Long
    fun getHoveredPin(): Long
    fun getHoveredLink(): Long
    fun getDoubleClickedNode(): Long
    fun getDoubleClickedPin(): Long
    fun getDoubleClickedLink(): Long
    fun isBackgroundClicked(): Boolean
    fun isBackgroundDoubleClicked(): Boolean

    /** -1 if none. */
    fun getBackgroundClickButtonIndex(): Int

    /** -1 if none. */
    fun getBackgroundDoubleClickButtonIndex(): Int

    /** Pass null arrays for pins you are not interested in. Returns true when link exists. */
    fun getLinkPins(linkId: Long, startPinId: LongArray?, endPinId: LongArray?): Boolean

    fun pinHadAnyLinks(pinId: Long): Boolean

    // ==================== Coordinates ====================
    fun getScreenSize(): ImVec2
    fun screenToCanvas(pos: ImVec2): ImVec2
    fun canvasToScreen(pos: ImVec2): ImVec2

    // ==================== Ordered node ids ====================
    fun getNodeCount(): Int
    fun getOrderedNodeIds(size: Int): LongArray

    // ==================== Style ====================
    fun pushStyleColor(idx: Int, color: ImVec4)
    fun popStyleColor(count: Int = 1)
    fun pushStyleVarFloat(idx: Int, value: Float)
    fun pushStyleVarVec2(idx: Int, value: ImVec2)
    fun pushStyleVarVec4(idx: Int, value: ImVec4)
    fun popStyleVar(count: Int = 1)
}

// =========================================================================
// Enums (values match imgui_node_editor.h)
// =========================================================================

object NePinKind {
    const val INPUT = 0
    const val OUTPUT = 1
}

object NeFlowDirection {
    const val FORWARD = 0
    const val BACKWARD = 1
}

object NeCanvasSizeMode {
    const val FIT_VERTICAL_VIEW = 0
    const val FIT_HORIZONTAL_VIEW = 1
    const val CENTER_ONLY = 2
}

object NeSaveReasonFlags {
    const val NONE = 0x00000000
    const val NAVIGATION = 0x00000001
    const val POSITION = 0x00000002
    const val SIZE = 0x00000004
    const val SELECTION = 0x00000008
    const val ADD_NODE = 0x00000010
    const val REMOVE_NODE = 0x00000020
    const val USER = 0x00000040
}

object NeStyleColor {
    const val BG = 0
    const val GRID = 1
    const val NODE_BG = 2
    const val NODE_BORDER = 3
    const val HOV_NODE_BORDER = 4
    const val SEL_NODE_BORDER = 5
    const val NODE_SEL_RECT = 6
    const val NODE_SEL_RECT_BORDER = 7
    const val HOV_LINK_BORDER = 8
    const val SEL_LINK_BORDER = 9
    const val HIGHLIGHT_LINK_BORDER = 10
    const val LINK_SEL_RECT = 11
    const val LINK_SEL_RECT_BORDER = 12
    const val PIN_RECT = 13
    const val PIN_RECT_BORDER = 14
    const val FLOW = 15
    const val FLOW_MARKER = 16
    const val GROUP_BG = 17
    const val GROUP_BORDER = 18
    const val COUNT = 19
}

object NeStyleVar {
    const val NODE_PADDING = 0
    const val NODE_ROUNDING = 1
    const val NODE_BORDER_WIDTH = 2
    const val HOVERED_NODE_BORDER_WIDTH = 3
    const val SELECTED_NODE_BORDER_WIDTH = 4
    const val PIN_ROUNDING = 5
    const val PIN_BORDER_WIDTH = 6
    const val LINK_STRENGTH = 7
    const val SOURCE_DIRECTION = 8
    const val TARGET_DIRECTION = 9
    const val SCROLL_DURATION = 10
    const val FLOW_MARKER_DISTANCE = 11
    const val FLOW_SPEED = 12
    const val FLOW_DURATION = 13
    const val PIVOT_ALIGNMENT = 14
    const val PIVOT_SIZE = 15
    const val PIVOT_SCALE = 16
    const val PIN_CORNERS = 17
    const val PIN_RADIUS = 18
    const val PIN_ARROW_SIZE = 19
    const val PIN_ARROW_WIDTH = 20
    const val GROUP_ROUNDING = 21
    const val GROUP_BORDER_WIDTH = 22
    const val HIGHLIGHT_CONNECTED_LINKS = 23
    const val SNAP_LINK_TO_PIN_DIR = 24
    const val HOVERED_NODE_BORDER_OFFSET = 25
    const val SELECTED_NODE_BORDER_OFFSET = 26
    const val COUNT = 27
}
