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

internal actual object Jni {
    init {
        cn.enaium.imgui.NativeLoader.load()
    }

    // ---- Context ----
    actual external fun createEditor(): Long
    actual external fun destroyEditor(ctx: Long)
    actual external fun getCurrentEditor(): Long
    actual external fun setCurrentEditor(ctx: Long)

    // ---- Begin/End ----
    actual external fun begin(id: String, sizeX: Float, sizeY: Float)
    actual external fun end()

    // ---- Nodes and pins ----
    actual external fun beginNode(id: Long)
    actual external fun beginPin(id: Long, kind: Int)
    actual external fun pinRect(aX: Float, aY: Float, bX: Float, bY: Float)
    actual external fun pinPivotRect(aX: Float, aY: Float, bX: Float, bY: Float)
    actual external fun pinPivotSize(sizeX: Float, sizeY: Float)
    actual external fun pinPivotScale(scaleX: Float, scaleY: Float)
    actual external fun pinPivotAlignment(alignmentX: Float, alignmentY: Float)
    actual external fun endPin()
    actual external fun group(sizeX: Float, sizeY: Float)
    actual external fun endNode()

    // ---- Group hints ----
    actual external fun beginGroupHint(nodeId: Long): Boolean
    actual external fun getGroupMin(): FloatArray
    actual external fun getGroupMax(): FloatArray
    actual external fun endGroupHint()

    // ---- Draw list ----
    actual external fun getNodeBackgroundDrawList(nodeId: Long): Long

    // ---- Links ----
    actual external fun link(
        id: Long,
        startPinId: Long,
        endPinId: Long,
        colorR: Float,
        colorG: Float,
        colorB: Float,
        colorA: Float,
        thickness: Float,
    ): Boolean

    actual external fun flow(linkId: Long, direction: Int)

    // ---- Create new link / node ----
    actual external fun beginCreate(colorR: Float, colorG: Float, colorB: Float, colorA: Float, thickness: Float): Boolean

    actual external fun queryNewLink(
        startPinId: LongArray,
        endPinId: LongArray,
        colorR: Float,
        colorG: Float,
        colorB: Float,
        colorA: Float,
        thickness: Float,
    ): Boolean

    actual external fun queryNewNode(
        pinId: LongArray,
        colorR: Float,
        colorG: Float,
        colorB: Float,
        colorA: Float,
        thickness: Float,
    ): Boolean

    actual external fun acceptNewItem(colorR: Float, colorG: Float, colorB: Float, colorA: Float, thickness: Float): Boolean
    actual external fun rejectNewItem(colorR: Float, colorG: Float, colorB: Float, colorA: Float, thickness: Float)
    actual external fun endCreate()

    // ---- Delete nodes / links ----
    actual external fun beginDelete(): Boolean

    actual external fun queryDeletedLink(linkId: LongArray, startPinId: LongArray?, endPinId: LongArray?): Boolean
    actual external fun queryDeletedNode(nodeId: LongArray): Boolean
    actual external fun acceptDeletedItem(deleteDependencies: Boolean)
    actual external fun rejectDeletedItem()
    actual external fun endDelete()

    // ---- Node / group geometry ----
    actual external fun setNodePosition(nodeId: Long, positionX: Float, positionY: Float)
    actual external fun getNodePosition(nodeId: Long): FloatArray
    actual external fun getNodeSize(nodeId: Long): FloatArray
    actual external fun setGroupSize(nodeId: Long, sizeX: Float, sizeY: Float)
    actual external fun centerNodeOnScreen(nodeId: Long)
    actual external fun setNodeZPosition(nodeId: Long, z: Float)
    actual external fun getNodeZPosition(nodeId: Long): Float

    // ---- Suspend / resume ----
    actual external fun suspend()
    actual external fun resume()
    actual external fun isSuspended(): Boolean

    actual external fun isActive(): Boolean

    // ---- Selection ----
    actual external fun hasSelectionChanged(): Boolean
    actual external fun getSelectedObjectCount(): Int
    actual external fun getSelectedNodes(size: Int): LongArray
    actual external fun getSelectedLinks(size: Int): LongArray
    actual external fun isNodeSelected(nodeId: Long): Boolean
    actual external fun isLinkSelected(linkId: Long): Boolean
    actual external fun clearSelection()
    actual external fun selectNode(nodeId: Long, append: Boolean)
    actual external fun selectLink(linkId: Long, append: Boolean)
    actual external fun deselectNode(nodeId: Long)
    actual external fun deselectLink(linkId: Long)

    // ---- Deletion requests ----
    actual external fun deleteNode(nodeId: Long): Boolean
    actual external fun deleteLink(linkId: Long): Boolean

    // ---- Links queries ----
    actual external fun hasAnyLinks(id: Long): Boolean
    actual external fun breakLinks(id: Long): Int

    // ---- Navigation ----
    actual external fun navigateToContent(duration: Float)
    actual external fun navigateToSelection(zoomIn: Boolean, duration: Float)

    // ---- Context menus ----
    actual external fun showNodeContextMenu(nodeId: LongArray): Boolean
    actual external fun showPinContextMenu(pinId: LongArray): Boolean
    actual external fun showLinkContextMenu(linkId: LongArray): Boolean
    actual external fun showBackgroundContextMenu(): Boolean

    // ---- Shortcuts ----
    actual external fun enableShortcuts(enable: Boolean)
    actual external fun areShortcutsEnabled(): Boolean

    actual external fun beginShortcut(): Boolean
    actual external fun acceptCut(): Boolean
    actual external fun acceptCopy(): Boolean
    actual external fun acceptPaste(): Boolean
    actual external fun acceptDuplicate(): Boolean
    actual external fun acceptCreateNode(): Boolean
    actual external fun getActionContextSize(): Int
    actual external fun getActionContextNodes(size: Int): LongArray
    actual external fun getActionContextLinks(size: Int): LongArray
    actual external fun endShortcut()

    actual external fun getCurrentZoom(): Float

    // ---- Hover / click queries ----
    actual external fun getHoveredNode(): Long
    actual external fun getHoveredPin(): Long
    actual external fun getHoveredLink(): Long
    actual external fun getDoubleClickedNode(): Long
    actual external fun getDoubleClickedPin(): Long
    actual external fun getDoubleClickedLink(): Long
    actual external fun isBackgroundClicked(): Boolean
    actual external fun isBackgroundDoubleClicked(): Boolean
    actual external fun getBackgroundClickButtonIndex(): Int
    actual external fun getBackgroundDoubleClickButtonIndex(): Int
    actual external fun getLinkPins(linkId: Long, startPinId: LongArray?, endPinId: LongArray?): Boolean
    actual external fun pinHadAnyLinks(pinId: Long): Boolean

    // ---- Coordinates ----
    actual external fun getScreenSize(): FloatArray
    actual external fun screenToCanvas(x: Float, y: Float): FloatArray
    actual external fun canvasToScreen(x: Float, y: Float): FloatArray

    // ---- Ordered node ids ----
    actual external fun getNodeCount(): Int
    actual external fun getOrderedNodeIds(size: Int): LongArray

    // ---- Style ----
    actual external fun pushStyleColor(idx: Int, r: Float, g: Float, b: Float, a: Float)
    actual external fun popStyleColor(count: Int)
    actual external fun pushStyleVarFloat(idx: Int, value: Float)
    actual external fun pushStyleVarVec2(idx: Int, x: Float, y: Float)
    actual external fun pushStyleVarVec4(idx: Int, x: Float, y: Float, z: Float, w: Float)
    actual external fun popStyleVar(count: Int)
}
