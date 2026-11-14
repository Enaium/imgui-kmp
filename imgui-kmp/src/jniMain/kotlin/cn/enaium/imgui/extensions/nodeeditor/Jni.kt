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

internal expect object Jni {

    // ---- Context ----
    fun createEditor(): Long
    fun destroyEditor(ctx: Long)
    fun getCurrentEditor(): Long
    fun setCurrentEditor(ctx: Long)

    // ---- Begin/End ----
    fun begin(id: String, sizeX: Float, sizeY: Float)
    fun end()

    // ---- Nodes and pins ----
    fun beginNode(id: Long)
    fun beginPin(id: Long, kind: Int)
    fun pinRect(aX: Float, aY: Float, bX: Float, bY: Float)
    fun pinPivotRect(aX: Float, aY: Float, bX: Float, bY: Float)
    fun pinPivotSize(sizeX: Float, sizeY: Float)
    fun pinPivotScale(scaleX: Float, scaleY: Float)
    fun pinPivotAlignment(alignmentX: Float, alignmentY: Float)
    fun endPin()
    fun group(sizeX: Float, sizeY: Float)
    fun endNode()

    // ---- Group hints ----
    fun beginGroupHint(nodeId: Long): Boolean
    fun getGroupMin(): FloatArray
    fun getGroupMax(): FloatArray
    fun endGroupHint()

    // ---- Draw list ----
    fun getNodeBackgroundDrawList(nodeId: Long): Long

    // ---- Links ----
    fun link(
        id: Long,
        startPinId: Long,
        endPinId: Long,
        colorR: Float,
        colorG: Float,
        colorB: Float,
        colorA: Float,
        thickness: Float,
    ): Boolean

    fun flow(linkId: Long, direction: Int)

    // ---- Create new link / node ----
    fun beginCreate(colorR: Float, colorG: Float, colorB: Float, colorA: Float, thickness: Float): Boolean

    fun queryNewLink(
        startPinId: LongArray,
        endPinId: LongArray,
        colorR: Float,
        colorG: Float,
        colorB: Float,
        colorA: Float,
        thickness: Float,
    ): Boolean

    fun queryNewNode(
        pinId: LongArray,
        colorR: Float,
        colorG: Float,
        colorB: Float,
        colorA: Float,
        thickness: Float,
    ): Boolean

    fun acceptNewItem(colorR: Float, colorG: Float, colorB: Float, colorA: Float, thickness: Float): Boolean
    fun rejectNewItem(colorR: Float, colorG: Float, colorB: Float, colorA: Float, thickness: Float)
    fun endCreate()

    // ---- Delete nodes / links ----
    fun beginDelete(): Boolean

    fun queryDeletedLink(linkId: LongArray, startPinId: LongArray?, endPinId: LongArray?): Boolean
    fun queryDeletedNode(nodeId: LongArray): Boolean
    fun acceptDeletedItem(deleteDependencies: Boolean)
    fun rejectDeletedItem()
    fun endDelete()

    // ---- Node / group geometry ----
    fun setNodePosition(nodeId: Long, positionX: Float, positionY: Float)
    fun getNodePosition(nodeId: Long): FloatArray
    fun getNodeSize(nodeId: Long): FloatArray
    fun setGroupSize(nodeId: Long, sizeX: Float, sizeY: Float)
    fun centerNodeOnScreen(nodeId: Long)
    fun setNodeZPosition(nodeId: Long, z: Float)
    fun getNodeZPosition(nodeId: Long): Float

    // ---- Suspend / resume ----
    fun suspend()
    fun resume()
    fun isSuspended(): Boolean

    fun isActive(): Boolean

    // ---- Selection ----
    fun hasSelectionChanged(): Boolean
    fun getSelectedObjectCount(): Int
    fun getSelectedNodes(size: Int): LongArray
    fun getSelectedLinks(size: Int): LongArray
    fun isNodeSelected(nodeId: Long): Boolean
    fun isLinkSelected(linkId: Long): Boolean
    fun clearSelection()
    fun selectNode(nodeId: Long, append: Boolean)
    fun selectLink(linkId: Long, append: Boolean)
    fun deselectNode(nodeId: Long)
    fun deselectLink(linkId: Long)

    // ---- Deletion requests ----
    fun deleteNode(nodeId: Long): Boolean
    fun deleteLink(linkId: Long): Boolean

    // ---- Links queries ----
    fun hasAnyLinks(id: Long): Boolean
    fun breakLinks(id: Long): Int

    // ---- Navigation ----
    fun navigateToContent(duration: Float)
    fun navigateToSelection(zoomIn: Boolean, duration: Float)

    // ---- Context menus ----
    fun showNodeContextMenu(nodeId: LongArray): Boolean
    fun showPinContextMenu(pinId: LongArray): Boolean
    fun showLinkContextMenu(linkId: LongArray): Boolean
    fun showBackgroundContextMenu(): Boolean

    // ---- Shortcuts ----
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

    // ---- Hover / click queries ----
    fun getHoveredNode(): Long
    fun getHoveredPin(): Long
    fun getHoveredLink(): Long
    fun getDoubleClickedNode(): Long
    fun getDoubleClickedPin(): Long
    fun getDoubleClickedLink(): Long
    fun isBackgroundClicked(): Boolean
    fun isBackgroundDoubleClicked(): Boolean
    fun getBackgroundClickButtonIndex(): Int
    fun getBackgroundDoubleClickButtonIndex(): Int
    fun getLinkPins(linkId: Long, startPinId: LongArray?, endPinId: LongArray?): Boolean
    fun pinHadAnyLinks(pinId: Long): Boolean

    // ---- Coordinates ----
    fun getScreenSize(): FloatArray
    fun screenToCanvas(x: Float, y: Float): FloatArray
    fun canvasToScreen(x: Float, y: Float): FloatArray

    // ---- Ordered node ids ----
    fun getNodeCount(): Int
    fun getOrderedNodeIds(size: Int): LongArray

    // ---- Style ----
    fun pushStyleColor(idx: Int, r: Float, g: Float, b: Float, a: Float)
    fun popStyleColor(count: Int)
    fun pushStyleVarFloat(idx: Int, value: Float)
    fun pushStyleVarVec2(idx: Int, x: Float, y: Float)
    fun pushStyleVarVec4(idx: Int, x: Float, y: Float, z: Float, w: Float)
    fun popStyleVar(count: Int)
}
