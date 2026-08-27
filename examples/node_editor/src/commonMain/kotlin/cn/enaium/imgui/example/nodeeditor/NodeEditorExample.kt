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

package cn.enaium.imgui.example.nodeeditor

import cn.enaium.imgui.ImGui
import cn.enaium.imgui.ImGuiCond
import cn.enaium.imgui.ImGuiWindowFlags
import cn.enaium.imgui.ImVec2
import cn.enaium.imgui.ImVec4
import cn.enaium.imgui.example.common.SdlRendererApp
import cn.enaium.imgui.extensions.nodeeditor.NePinKind
import cn.enaium.imgui.extensions.nodeeditor.NeStyleColor
import cn.enaium.imgui.extensions.nodeeditor.NodeEditor
import cn.enaium.imgui.extensions.nodeeditor.NodeEditorContext
import kotlin.math.max

/**
 * A blueprints-style node graph built on the imgui-node-editor bindings:
 * draggable nodes with typed input/output pins, link creation by dragging
 * between pins, link/node deletion (Del key or context menu), background and
 * node context menus and editor navigation.
 *
 * Run with `./gradlew :examples:node_editor:jvmRun` (JVM) or the per-target
 * native binaries; pass `--frames N` / `IMGUI_KMP_FRAMES=N` to exit after N
 * frames (headless CI runs).
 */
fun runNodeEditorExample(frames: Int = Int.MAX_VALUE) {
    var demo: NodeEditorDemo? = null
    SdlRendererApp.run(
        title = "imgui-kmp node editor example",
        frames = frames,
        init = { demo = NodeEditorDemo() },
        draw = { frame -> demo?.draw(frame) },
        close = { demo?.close() },
    )
}

/** Pin type colors, following the upstream blueprints example. */
private val FLOW = ImVec4(1f, 0.69f, 0f, 1f)
private val BOOL = ImVec4(0.9f, 0.31f, 0f, 1f)
private val INT = ImVec4(0f, 0.6f, 1f, 1f)
private val FLOAT = ImVec4(0.5f, 0.8f, 0f, 1f)
private val STRING = ImVec4(1f, 0.6f, 0.9f, 1f)

private class NodeEditorDemo {

    private data class PinSpec(val name: String, val kind: Int, val color: ImVec4)
    private data class Pin(val id: Long, val name: String, val kind: Int, val color: ImVec4)
    private data class Node(val id: Long, val title: String, val inputs: List<Pin>, val outputs: List<Pin>)
    private data class Link(val id: Long, val startPin: Long, val endPin: Long, val color: ImVec4)

    private val editor: NodeEditorContext = NodeEditor.createEditor()
    private val nodes = mutableListOf<Node>()
    private val links = mutableListOf<Link>()

    // imgui-node-editor stringifies raw id values ("%p") for its invisible
    // hit-area items, and Node/Pin/Link ids share one ImGui id scope — so the
    // numeric id space must be shared across ALL object types, otherwise e.g.
    // node 1 and pin 1 both become "0x1" and imgui reports an ID conflict
    // whenever the mouse hovers a node. (Same pattern as the upstream
    // blueprints example, which uses a single GetNextId() counter.)
    private var nextId = 1L

    init {
        NodeEditor.setCurrentEditor(editor)

        // A small built-in graph: OnStart -> Print -> SetVariable, with an
        // arithmetic branch feeding SetVariable's value.
        val onStart = addNode("On Start", listOf(), listOf(PinSpec("Flow", NePinKind.OUTPUT, FLOW)), ImVec2(80f, 140f))
        val print = addNode(
            "Print",
            listOf(PinSpec("Flow", NePinKind.INPUT, FLOW), PinSpec("Text", NePinKind.INPUT, STRING)),
            listOf(PinSpec("Flow", NePinKind.OUTPUT, FLOW)),
            ImVec2(340f, 80f),
        )
        val setVar = addNode(
            "Set Variable",
            listOf(
                PinSpec("Flow", NePinKind.INPUT, FLOW),
                PinSpec("Name", NePinKind.INPUT, STRING),
                PinSpec("Value", NePinKind.INPUT, INT),
            ),
            listOf(PinSpec("Flow", NePinKind.OUTPUT, FLOW), PinSpec("Result", NePinKind.OUTPUT, INT)),
            ImVec2(640f, 160f),
        )
        val addInt = addNode(
            "Add Int",
            listOf(PinSpec("A", NePinKind.INPUT, INT), PinSpec("B", NePinKind.INPUT, INT)),
            listOf(PinSpec("Result", NePinKind.OUTPUT, INT)),
            ImVec2(340f, 380f),
        )
        addLink(pinOf(onStart, "Flow", NePinKind.OUTPUT)!!, pinOf(print, "Flow", NePinKind.INPUT)!!, FLOW)
        addLink(pinOf(print, "Flow", NePinKind.OUTPUT)!!, pinOf(setVar, "Flow", NePinKind.INPUT)!!, FLOW)
        addLink(pinOf(addInt, "Result", NePinKind.OUTPUT)!!, pinOf(setVar, "Value", NePinKind.INPUT)!!, INT)
    }

    fun close() {
        NodeEditor.destroyEditor(editor)
    }

    fun draw(frame: Int) {
        // Full-screen window hosting the toolbar and the editor canvas.
        ImGui.setNextWindowPos(ImVec2(0f, 0f), ImGuiCond.ALWAYS)
        ImGui.setNextWindowSize(ImGui.getIO().displaySize, ImGuiCond.ALWAYS)
        ImGui.begin(
            "node editor",
            flags = ImGuiWindowFlags.NO_TITLE_BAR or ImGuiWindowFlags.NO_RESIZE or
                ImGuiWindowFlags.NO_MOVE or ImGuiWindowFlags.NO_COLLAPSE or
                ImGuiWindowFlags.NO_SCROLLBAR or ImGuiWindowFlags.NO_SAVED_SETTINGS,
        )

        // ---- toolbar ----
        if (ImGui.button("Navigate to content")) NodeEditor.navigateToContent()
        ImGui.sameLine()
        if (ImGui.button("Navigate to selection")) NodeEditor.navigateToSelection()
        ImGui.sameLine()
        ImGui.text(
            "nodes: ${nodes.size}  links: ${links.size}  selected: ${NodeEditor.getSelectedObjectCount()}  zoom: ${NodeEditor.getCurrentZoom()}",
        )

        // ---- editor canvas ----
        NodeEditor.begin("canvas")
        if (frame == 0) {
            NodeEditor.navigateToContent()
        }

        nodes.forEach(::drawNode)
        links.forEach(::drawLink)
        handleLinkCreation()
        handleDeletion()
        handleContextMenus()

        NodeEditor.end()
        ImGui.end()
    }

    // =====================================================================
    // Graph state
    // =====================================================================

    private fun addNode(title: String, inputs: List<PinSpec>, outputs: List<PinSpec>, pos: ImVec2): Long {
        val nodeId = nextId++
        val inputPins = inputs.map { Pin(nextId++, it.name, it.kind, it.color) }
        val outputPins = outputs.map { Pin(nextId++, it.name, it.kind, it.color) }
        nodes.add(Node(nodeId, title, inputPins, outputPins))
        NodeEditor.setNodePosition(nodeId, pos)
        return nodeId
    }

    private fun addLink(startPin: Long, endPin: Long, color: ImVec4) {
        links.add(Link(nextId++, startPin, endPin, color))
    }

    private fun findPin(id: Long): Pin? = nodes.asSequence()
        .flatMap { it.inputs.asSequence() + it.outputs.asSequence() }
        .firstOrNull { it.id == id }

    private fun pinOf(nodeId: Long, name: String, kind: Int): Long? =
        nodes.find { it.id == nodeId }
            ?.let { (if (kind == NePinKind.INPUT) it.inputs else it.outputs).firstOrNull { p -> p.name == name } }
            ?.id

    // =====================================================================
    // Drawing
    // =====================================================================

    private fun drawNode(node: Node) {
        NodeEditor.beginNode(node.id)
        // imgui-node-editor does not scope user content under the node id, so
        // push a per-node scope: otherwise pins/tables with the same label
        // across nodes trip imgui's ID-conflict detector.
        ImGui.pushId("node${node.id}")
        ImGui.text(node.title)
        // A table with outer_size.x = 0 right-aligns to the canvas right edge
        // (the canvas scrolls, so NO_HOST_EXTEND_X is ignored), making the
        // measured node width = canvas width - node x: dragging would stretch
        // the node instead of moving it. Size the table from its content so
        // the width is position-independent.
        val inputWidth = node.inputs.maxOfOrNull { ImGui.calcTextSize("  ${it.name}").x } ?: 0f
        val outputWidth = node.outputs.maxOfOrNull { ImGui.calcTextSize("${it.name}  ").x } ?: 0f
        val tableWidth = maxOf(ImGui.calcTextSize(node.title).x + 16f, inputWidth + outputWidth + 24f)
        ImGui.beginTable("pins", 2, outerSize = ImVec2(tableWidth, 0f))
        val rows = max(node.inputs.size, node.outputs.size)
        for (i in 0 until rows) {
            ImGui.tableNextRow()
            ImGui.tableNextColumn()
            node.inputs.getOrNull(i)?.let { drawPin(it, rightAligned = false) }
            ImGui.tableNextColumn()
            node.outputs.getOrNull(i)?.let { drawPin(it, rightAligned = true) }
        }
        ImGui.endTable()
        ImGui.popId()
        NodeEditor.endNode()
    }

    // Matches the upstream simple-example: no explicit PinRect/PinPivotRect, so
    // the editor resolves the pin rect to the label's item rect and the pivot
    // to a point at its center (PivotSize defaults to 0). A point pivot keeps
    // this pin's link anchor fixed when the connected node is dragged — an
    // explicit full-rect pivot would make ImRect_ClosestLine remap the anchor
    // y to the other node's range and slide it.
    private fun drawPin(pin: Pin, rightAligned: Boolean) {
        NodeEditor.beginPin(pin.id, pin.kind)
        NodeEditor.pushStyleColor(NeStyleColor.PIN_RECT, pin.color)
        NodeEditor.pushStyleColor(NeStyleColor.PIN_RECT_BORDER, pin.color)
        ImGui.text(if (rightAligned) pin.name + "  " else "  " + pin.name)
        NodeEditor.popStyleColor(2)
        NodeEditor.endPin()
    }

    private fun drawLink(link: Link) {
        NodeEditor.link(link.id, link.startPin, link.endPin, link.color, 2.0f)
    }

    // =====================================================================
    // Interactions
    // =====================================================================

    private fun handleLinkCreation() {
        if (NodeEditor.beginCreate()) {
            val startPin = LongArray(1)
            val endPin = LongArray(1)
            if (NodeEditor.queryNewLink(startPin, endPin)) {
                val start = findPin(startPin[0])
                val end = findPin(endPin[0])
                val valid = start != null && end != null && start.kind != end.kind &&
                    links.none {
                        (it.startPin == startPin[0] && it.endPin == endPin[0]) ||
                            (it.startPin == endPin[0] && it.endPin == startPin[0])
                    }
                if (valid) {
                    if (NodeEditor.acceptNewItem()) {
                        addLink(startPin[0], endPin[0], start.color)
                    }
                } else {
                    NodeEditor.rejectNewItem()
                }
            }
        }
        NodeEditor.endCreate()
    }

    private fun handleDeletion() {
        if (NodeEditor.beginDelete()) {
            val linkId = LongArray(1)
            while (NodeEditor.queryDeletedLink(linkId)) {
                if (linkId[0] != 0L) {
                    NodeEditor.acceptDeletedItem()
                    links.removeAll { it.id == linkId[0] }
                } else {
                    NodeEditor.rejectDeletedItem()
                }
            }
            val nodeId = LongArray(1)
            while (NodeEditor.queryDeletedNode(nodeId)) {
                if (nodeId[0] != 0L) {
                    NodeEditor.acceptDeletedItem()
                    val node = nodes.find { it.id == nodeId[0] }
                    if (node != null) {
                        val pinIds = (node.inputs + node.outputs).map { it.id }.toSet()
                        links.removeAll { it.startPin in pinIds || it.endPin in pinIds }
                        nodes.remove(node)
                    }
                } else {
                    NodeEditor.rejectDeletedItem()
                }
            }
        }
        NodeEditor.endDelete()
    }

    private fun handleContextMenus() {
        val nodeId = LongArray(1)
        if (NodeEditor.showNodeContextMenu(nodeId)) {
            ImGui.openPopup("node context menu")
        }
        val linkId = LongArray(1)
        if (NodeEditor.showLinkContextMenu(linkId)) {
            ImGui.openPopup("link context menu")
        }
        if (NodeEditor.showBackgroundContextMenu()) {
            ImGui.openPopup("background context menu")
        }

        // Popups must be submitted while the editor is suspended, otherwise
        // they are swallowed by the canvas.
        NodeEditor.suspendEditor()
        if (ImGui.beginPopup("node context menu")) {
            nodes.find { it.id == nodeId[0] }?.let { node ->
                ImGui.text(node.title)
                ImGui.separator()
                if (ImGui.menuItem("Delete node")) {
                    NodeEditor.deleteNode(node.id)
                }
            }
            ImGui.endPopup()
        }
        if (ImGui.beginPopup("link context menu")) {
            if (ImGui.menuItem("Delete link")) {
                NodeEditor.deleteLink(linkId[0])
            }
            ImGui.endPopup()
        }
        if (ImGui.beginPopup("background context menu")) {
            val pos = NodeEditor.screenToCanvas(ImGui.getMousePos())
            if (ImGui.menuItem("Add On Start")) {
                addNode("On Start", listOf(), listOf(PinSpec("Flow", NePinKind.OUTPUT, FLOW)), pos)
            }
            if (ImGui.menuItem("Add Print")) {
                addNode(
                    "Print",
                    listOf(PinSpec("Flow", NePinKind.INPUT, FLOW), PinSpec("Text", NePinKind.INPUT, STRING)),
                    listOf(PinSpec("Flow", NePinKind.OUTPUT, FLOW)),
                    pos,
                )
            }
            if (ImGui.menuItem("Add Set Variable")) {
                addNode(
                    "Set Variable",
                    listOf(
                        PinSpec("Flow", NePinKind.INPUT, FLOW),
                        PinSpec("Name", NePinKind.INPUT, STRING),
                        PinSpec("Value", NePinKind.INPUT, INT),
                    ),
                    listOf(PinSpec("Flow", NePinKind.OUTPUT, FLOW), PinSpec("Result", NePinKind.OUTPUT, INT)),
                    pos,
                )
            }
            if (ImGui.menuItem("Add Add Int")) {
                addNode(
                    "Add Int",
                    listOf(PinSpec("A", NePinKind.INPUT, INT), PinSpec("B", NePinKind.INPUT, INT)),
                    listOf(PinSpec("Result", NePinKind.OUTPUT, INT)),
                    pos,
                )
            }
            ImGui.endPopup()
        }
        NodeEditor.resumeEditor()
    }
}
