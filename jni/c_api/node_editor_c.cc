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

#include "imgui_node_editor.h"

#include "node_editor_c.h"

namespace ed = ax::NodeEditor;

static ImVec2 to_vec2(imgui_vec2 v) {
    return ImVec2(v.x, v.y);
}

static imgui_vec2 from_vec2(ImVec2 v) {
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

static ImVec4 to_vec4(imgui_vec4 v) {
    return ImVec4(v.x, v.y, v.z, v.w);
}

static ed::PinKind to_pin_kind(int kind) {
    return kind == ne_pin_kind_output ? ed::PinKind::Output : ed::PinKind::Input;
}

static ed::FlowDirection to_flow_direction(int direction) {
    return direction == 1 ? ed::FlowDirection::Backward : ed::FlowDirection::Forward;
}

static ed::NodeId to_node_id(int64_t id) {
    return ed::NodeId(static_cast<uintptr_t>(id));
}

static ed::PinId to_pin_id(int64_t id) {
    return ed::PinId(static_cast<uintptr_t>(id));
}

static ed::LinkId to_link_id(int64_t id) {
    return ed::LinkId(static_cast<uintptr_t>(id));
}

static int64_t from_node_id(ed::NodeId id) {
    return static_cast<int64_t>(id.Get());
}

static int64_t from_pin_id(ed::PinId id) {
    return static_cast<int64_t>(id.Get());
}

static int64_t from_link_id(ed::LinkId id) {
    return static_cast<int64_t>(id.Get());
}

extern "C" {

ne_context* ne_create_editor(void) {
    return reinterpret_cast<ne_context*>(ed::CreateEditor());
}

void ne_destroy_editor(ne_context* ctx) {
    ed::DestroyEditor(reinterpret_cast<ed::EditorContext*>(ctx));
}

ne_context* ne_get_current_editor(void) {
    return reinterpret_cast<ne_context*>(ed::GetCurrentEditor());
}

void ne_set_current_editor(ne_context* ctx) {
    ed::SetCurrentEditor(reinterpret_cast<ed::EditorContext*>(ctx));
}

const char* ne_get_style_color_name(int color_index) {
    return ed::GetStyleColorName(static_cast<ed::StyleColor>(color_index));
}

void ne_push_style_color(int color_index, imgui_vec4 color) {
    ed::PushStyleColor(static_cast<ed::StyleColor>(color_index), to_vec4(color));
}

void ne_pop_style_color(int count) {
    ed::PopStyleColor(count);
}

void ne_push_style_var_float(int var_index, float value) {
    ed::PushStyleVar(static_cast<ed::StyleVar>(var_index), value);
}

void ne_push_style_var_vec2(int var_index, float x, float y) {
    ed::PushStyleVar(static_cast<ed::StyleVar>(var_index), ImVec2(x, y));
}

void ne_push_style_var_vec4(int var_index, float x, float y, float z, float w) {
    ed::PushStyleVar(static_cast<ed::StyleVar>(var_index), ImVec4(x, y, z, w));
}

void ne_pop_style_var(int count) {
    ed::PopStyleVar(count);
}

void ne_begin(const char* id, float size_x, float size_y) {
    ed::Begin(id, ImVec2(size_x, size_y));
}

void ne_end(void) {
    ed::End();
}

void ne_begin_node(ne_node_id id) {
    ed::BeginNode(to_node_id(id));
}

void ne_begin_pin(ne_pin_id id, int kind) {
    ed::BeginPin(to_pin_id(id), to_pin_kind(kind));
}

void ne_end_pin(void) {
    ed::EndPin();
}

void ne_end_node(void) {
    ed::EndNode();
}

void ne_group(float size_x, float size_y) {
    ed::Group(ImVec2(size_x, size_y));
}

void ne_pin_rect(float a_x, float a_y, float b_x, float b_y) {
    ed::PinRect(ImVec2(a_x, a_y), ImVec2(b_x, b_y));
}

void ne_pin_pivot_rect(float a_x, float a_y, float b_x, float b_y) {
    ed::PinPivotRect(ImVec2(a_x, a_y), ImVec2(b_x, b_y));
}

void ne_pin_pivot_size(float w, float h) {
    ed::PinPivotSize(ImVec2(w, h));
}

void ne_pin_pivot_scale(float sx, float sy) {
    ed::PinPivotScale(ImVec2(sx, sy));
}

void ne_pin_pivot_alignment(float ax, float ay) {
    ed::PinPivotAlignment(ImVec2(ax, ay));
}

bool ne_begin_group_hint(ne_node_id node_id) {
    return ed::BeginGroupHint(to_node_id(node_id));
}

imgui_vec2 ne_get_group_min(void) {
    return from_vec2(ed::GetGroupMin());
}

imgui_vec2 ne_get_group_max(void) {
    return from_vec2(ed::GetGroupMax());
}

imgui_draw_list* ne_get_hint_foreground_draw_list(void) {
    return reinterpret_cast<imgui_draw_list*>(ed::GetHintForegroundDrawList());
}

imgui_draw_list* ne_get_hint_background_draw_list(void) {
    return reinterpret_cast<imgui_draw_list*>(ed::GetHintBackgroundDrawList());
}

void ne_end_group_hint(void) {
    ed::EndGroupHint();
}

imgui_draw_list* ne_get_node_background_draw_list(ne_node_id node_id) {
    return reinterpret_cast<imgui_draw_list*>(ed::GetNodeBackgroundDrawList(to_node_id(node_id)));
}

bool ne_link(ne_link_id id, ne_pin_id start_pin_id, ne_pin_id end_pin_id, imgui_vec4 color, float thickness) {
    return ed::Link(to_link_id(id), to_pin_id(start_pin_id), to_pin_id(end_pin_id), to_vec4(color), thickness);
}

void ne_flow(ne_link_id link_id, int direction) {
    ed::Flow(to_link_id(link_id), to_flow_direction(direction));
}

bool ne_begin_create(imgui_vec4 color, float thickness) {
    return ed::BeginCreate(to_vec4(color), thickness);
}

bool ne_query_new_link(ne_pin_id* out_start_id, ne_pin_id* out_end_id) {
    ed::PinId start;
    ed::PinId end;
    if (!ed::QueryNewLink(&start, &end)) {
        return false;
    }
    *out_start_id = from_pin_id(start);
    *out_end_id = from_pin_id(end);
    return true;
}

bool ne_query_new_link_styled(ne_pin_id* out_start_id, ne_pin_id* out_end_id, imgui_vec4 color, float thickness) {
    ed::PinId start;
    ed::PinId end;
    if (!ed::QueryNewLink(&start, &end, to_vec4(color), thickness)) {
        return false;
    }
    *out_start_id = from_pin_id(start);
    *out_end_id = from_pin_id(end);
    return true;
}

bool ne_query_new_node(ne_pin_id* out_pin_id) {
    ed::PinId pin;
    if (!ed::QueryNewNode(&pin)) {
        return false;
    }
    *out_pin_id = from_pin_id(pin);
    return true;
}

bool ne_query_new_node_styled(ne_pin_id* out_pin_id, imgui_vec4 color, float thickness) {
    ed::PinId pin;
    if (!ed::QueryNewNode(&pin, to_vec4(color), thickness)) {
        return false;
    }
    *out_pin_id = from_pin_id(pin);
    return true;
}

bool ne_accept_new_item(void) {
    return ed::AcceptNewItem();
}

bool ne_accept_new_item_ex(imgui_vec4 color, float thickness) {
    return ed::AcceptNewItem(to_vec4(color), thickness);
}

void ne_reject_new_item(void) {
    ed::RejectNewItem();
}

void ne_reject_new_item_ex(imgui_vec4 color, float thickness) {
    ed::RejectNewItem(to_vec4(color), thickness);
}

void ne_end_create(void) {
    ed::EndCreate();
}

bool ne_begin_delete(void) {
    return ed::BeginDelete();
}

bool ne_query_deleted_link(ne_link_id* out_link_id, ne_pin_id* out_start_id, ne_pin_id* out_end_id) {
    ed::LinkId link;
    ed::PinId start;
    ed::PinId end;
    if (!ed::QueryDeletedLink(&link, &start, &end)) {
        return false;
    }
    *out_link_id = from_link_id(link);
    *out_start_id = from_pin_id(start);
    *out_end_id = from_pin_id(end);
    return true;
}

bool ne_query_deleted_node(ne_node_id* out_node_id) {
    ed::NodeId node;
    if (!ed::QueryDeletedNode(&node)) {
        return false;
    }
    *out_node_id = from_node_id(node);
    return true;
}

bool ne_accept_deleted_item(bool delete_dependencies) {
    return ed::AcceptDeletedItem(delete_dependencies);
}

void ne_reject_deleted_item(void) {
    ed::RejectDeletedItem();
}

void ne_end_delete(void) {
    ed::EndDelete();
}

void ne_set_node_position(ne_node_id node_id, float x, float y) {
    ed::SetNodePosition(to_node_id(node_id), ImVec2(x, y));
}

void ne_set_group_size(ne_node_id node_id, float x, float y) {
    ed::SetGroupSize(to_node_id(node_id), ImVec2(x, y));
}

imgui_vec2 ne_get_node_position(ne_node_id node_id) {
    return from_vec2(ed::GetNodePosition(to_node_id(node_id)));
}

imgui_vec2 ne_get_node_size(ne_node_id node_id) {
    return from_vec2(ed::GetNodeSize(to_node_id(node_id)));
}

void ne_center_node_on_screen(ne_node_id node_id) {
    ed::CenterNodeOnScreen(to_node_id(node_id));
}

void ne_set_node_z_position(ne_node_id node_id, float z) {
    ed::SetNodeZPosition(to_node_id(node_id), z);
}

float ne_get_node_z_position(ne_node_id node_id) {
    return ed::GetNodeZPosition(to_node_id(node_id));
}

void ne_suspend(void) {
    ed::Suspend();
}

void ne_resume(void) {
    ed::Resume();
}

bool ne_is_suspended(void) {
    return ed::IsSuspended();
}

bool ne_is_active(void) {
    return ed::IsActive();
}

bool ne_has_selection_changed(void) {
    return ed::HasSelectionChanged();
}

int ne_get_selected_object_count(void) {
    return ed::GetSelectedObjectCount();
}

void ne_get_selected_nodes(int64_t* out_ids, int capacity, int* out_count) {
    // GetSelectedNodes takes the buffer size and returns the number filled.
    *out_count = ed::GetSelectedNodes(reinterpret_cast<ed::NodeId*>(out_ids), capacity);
}

void ne_get_selected_links(int64_t* out_ids, int capacity, int* out_count) {
    *out_count = ed::GetSelectedLinks(reinterpret_cast<ed::LinkId*>(out_ids), capacity);
}

bool ne_is_node_selected(ne_node_id node_id) {
    return ed::IsNodeSelected(to_node_id(node_id));
}

bool ne_is_link_selected(ne_link_id link_id) {
    return ed::IsLinkSelected(to_link_id(link_id));
}

void ne_clear_selection(void) {
    ed::ClearSelection();
}

void ne_select_node(ne_node_id node_id, bool append) {
    ed::SelectNode(to_node_id(node_id), append);
}

void ne_select_link(ne_link_id link_id, bool append) {
    ed::SelectLink(to_link_id(link_id), append);
}

void ne_deselect_node(ne_node_id node_id) {
    ed::DeselectNode(to_node_id(node_id));
}

void ne_deselect_link(ne_link_id link_id) {
    ed::DeselectLink(to_link_id(link_id));
}

bool ne_delete_node(ne_node_id node_id) {
    return ed::DeleteNode(to_node_id(node_id));
}

bool ne_delete_link(ne_link_id link_id) {
    return ed::DeleteLink(to_link_id(link_id));
}

bool ne_has_any_links(int64_t id) {
    // NodeId and PinId are distinct C++ types wrapping uintptr_t; dispatch by
    // trying the node overload first is not possible in C, so we expose two
    // entry points plus this unified one used when the caller does not know
    // which kind the id is. Node ids and pin ids never collide in practice,
    // but the editor API requires the right type, so callers should prefer
    // the typed variants.
    return ed::HasAnyLinks(ed::NodeId(static_cast<uintptr_t>(id))) ||
           ed::HasAnyLinks(ed::PinId(static_cast<uintptr_t>(id)));
}

bool ne_has_any_links_pin(ne_pin_id pin_id) {
    return ed::HasAnyLinks(to_pin_id(pin_id));
}

int ne_break_links(int64_t id) {
    int broken = ed::BreakLinks(ed::NodeId(static_cast<uintptr_t>(id)));
    if (broken == 0) {
        broken = ed::BreakLinks(ed::PinId(static_cast<uintptr_t>(id)));
    }
    return broken;
}

int ne_break_links_pin(ne_pin_id pin_id) {
    return ed::BreakLinks(to_pin_id(pin_id));
}

bool ne_pin_had_any_links(ne_pin_id pin_id) {
    return ed::PinHadAnyLinks(to_pin_id(pin_id));
}

bool ne_get_link_pins(ne_link_id link_id, ne_pin_id* out_start_id, ne_pin_id* out_end_id) {
    ed::PinId start;
    ed::PinId end;
    if (!ed::GetLinkPins(to_link_id(link_id), &start, &end)) {
        return false;
    }
    *out_start_id = from_pin_id(start);
    *out_end_id = from_pin_id(end);
    return true;
}

void ne_navigate_to_content(float duration) {
    ed::NavigateToContent(duration);
}

void ne_navigate_to_selection(bool zoom_in, float duration) {
    ed::NavigateToSelection(zoom_in, duration);
}

bool ne_show_node_context_menu(ne_node_id* inout_node_id) {
    ed::NodeId node = to_node_id(*inout_node_id);
    if (!ed::ShowNodeContextMenu(&node)) {
        return false;
    }
    *inout_node_id = from_node_id(node);
    return true;
}

bool ne_show_pin_context_menu(ne_pin_id* inout_pin_id) {
    ed::PinId pin = to_pin_id(*inout_pin_id);
    if (!ed::ShowPinContextMenu(&pin)) {
        return false;
    }
    *inout_pin_id = from_pin_id(pin);
    return true;
}

bool ne_show_link_context_menu(ne_link_id* inout_link_id) {
    ed::LinkId link = to_link_id(*inout_link_id);
    if (!ed::ShowLinkContextMenu(&link)) {
        return false;
    }
    *inout_link_id = from_link_id(link);
    return true;
}

bool ne_show_background_context_menu(void) {
    return ed::ShowBackgroundContextMenu();
}

void ne_enable_shortcuts(bool enable) {
    ed::EnableShortcuts(enable);
}

bool ne_are_shortcuts_enabled(void) {
    return ed::AreShortcutsEnabled();
}

bool ne_begin_shortcut(void) {
    return ed::BeginShortcut();
}

bool ne_accept_cut(void) {
    return ed::AcceptCut();
}

bool ne_accept_copy(void) {
    return ed::AcceptCopy();
}

bool ne_accept_paste(void) {
    return ed::AcceptPaste();
}

bool ne_accept_duplicate(void) {
    return ed::AcceptDuplicate();
}

bool ne_accept_create_node(void) {
    return ed::AcceptCreateNode();
}

int ne_get_action_context_size(void) {
    return ed::GetActionContextSize();
}

void ne_get_action_context_nodes(int64_t* out_ids, int capacity, int* out_count) {
    *out_count = ed::GetActionContextNodes(reinterpret_cast<ed::NodeId*>(out_ids), capacity);
}

void ne_get_action_context_links(int64_t* out_ids, int capacity, int* out_count) {
    *out_count = ed::GetActionContextLinks(reinterpret_cast<ed::LinkId*>(out_ids), capacity);
}

void ne_end_shortcut(void) {
    ed::EndShortcut();
}

float ne_get_current_zoom(void) {
    return ed::GetCurrentZoom();
}

ne_node_id ne_get_hovered_node(void) {
    return from_node_id(ed::GetHoveredNode());
}

ne_pin_id ne_get_hovered_pin(void) {
    return from_pin_id(ed::GetHoveredPin());
}

ne_link_id ne_get_hovered_link(void) {
    return from_link_id(ed::GetHoveredLink());
}

ne_node_id ne_get_double_clicked_node(void) {
    return from_node_id(ed::GetDoubleClickedNode());
}

ne_pin_id ne_get_double_clicked_pin(void) {
    return from_pin_id(ed::GetDoubleClickedPin());
}

ne_link_id ne_get_double_clicked_link(void) {
    return from_link_id(ed::GetDoubleClickedLink());
}

bool ne_is_background_clicked(void) {
    return ed::IsBackgroundClicked();
}

bool ne_is_background_double_clicked(void) {
    return ed::IsBackgroundDoubleClicked();
}

int ne_get_background_click_button_index(void) {
    return ed::GetBackgroundClickButtonIndex();
}

int ne_get_background_double_click_button_index(void) {
    return ed::GetBackgroundDoubleClickButtonIndex();
}

imgui_vec2 ne_get_screen_size(void) {
    return from_vec2(ed::GetScreenSize());
}

imgui_vec2 ne_screen_to_canvas(float x, float y) {
    return from_vec2(ed::ScreenToCanvas(ImVec2(x, y)));
}

imgui_vec2 ne_canvas_to_screen(float x, float y) {
    return from_vec2(ed::CanvasToScreen(ImVec2(x, y)));
}

int ne_get_node_count(void) {
    return ed::GetNodeCount();
}

void ne_get_ordered_node_ids(int64_t* out_ids, int capacity, int* out_count) {
    *out_count = ed::GetOrderedNodeIds(reinterpret_cast<ed::NodeId*>(out_ids), capacity);
}

} // extern "C"
