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

#include "imgui.h"
#include "imgui_internal.h"

#include "imgui_threaded_rendering.h"

#include "threaded_rendering_c.h"

static ImDrawData* to_draw_data(imgui_draw_data* data) {
    return reinterpret_cast<ImDrawData*>(data);
}

static ImVec2 to_vec2(imgui_vec2 v) {
    return ImVec2(v.x, v.y);
}

static imgui_vec2 from_vec2(ImVec2 v) {
    imgui_vec2 out;
    out.x = v.x;
    out.y = v.y;
    return out;
}

extern "C" {

// ---- ImDrawDataSnapshot ----

trs_snapshot* trs_snapshot_create(void) {
    return reinterpret_cast<trs_snapshot*>(new ImDrawDataSnapshot());
}

void trs_snapshot_destroy(trs_snapshot* snapshot) {
    // ~ImDrawDataSnapshot() calls Clear() itself.
    delete reinterpret_cast<ImDrawDataSnapshot*>(snapshot);
}

void trs_snapshot_snap_using_swap(trs_snapshot* snapshot, imgui_draw_data* src, double current_time) {
    reinterpret_cast<ImDrawDataSnapshot*>(snapshot)->SnapUsingSwap(to_draw_data(src), current_time);
}

void trs_snapshot_clear(trs_snapshot* snapshot) {
    reinterpret_cast<ImDrawDataSnapshot*>(snapshot)->Clear();
}

float trs_snapshot_get_memory_compact_timer(trs_snapshot* snapshot) {
    return reinterpret_cast<ImDrawDataSnapshot*>(snapshot)->MemoryCompactTimer;
}

void trs_snapshot_set_memory_compact_timer(trs_snapshot* snapshot, float seconds) {
    reinterpret_cast<ImDrawDataSnapshot*>(snapshot)->MemoryCompactTimer = seconds;
}

bool trs_snapshot_is_valid(trs_snapshot* snapshot) {
    return reinterpret_cast<ImDrawDataSnapshot*>(snapshot)->DrawData.Valid;
}

int trs_snapshot_get_frame_count(trs_snapshot* snapshot) {
    return reinterpret_cast<ImDrawDataSnapshot*>(snapshot)->DrawData.FrameCount;
}

int trs_snapshot_get_total_idx_count(trs_snapshot* snapshot) {
    return reinterpret_cast<ImDrawDataSnapshot*>(snapshot)->DrawData.TotalIdxCount;
}

int trs_snapshot_get_total_vtx_count(trs_snapshot* snapshot) {
    return reinterpret_cast<ImDrawDataSnapshot*>(snapshot)->DrawData.TotalVtxCount;
}

imgui_vec2 trs_snapshot_get_display_pos(trs_snapshot* snapshot) {
    return from_vec2(reinterpret_cast<ImDrawDataSnapshot*>(snapshot)->DrawData.DisplayPos);
}

imgui_vec2 trs_snapshot_get_display_size(trs_snapshot* snapshot) {
    return from_vec2(reinterpret_cast<ImDrawDataSnapshot*>(snapshot)->DrawData.DisplaySize);
}

imgui_vec2 trs_snapshot_get_framebuffer_scale(trs_snapshot* snapshot) {
    return from_vec2(reinterpret_cast<ImDrawDataSnapshot*>(snapshot)->DrawData.FramebufferScale);
}

int trs_snapshot_get_cmd_lists_count(trs_snapshot* snapshot) {
    return reinterpret_cast<ImDrawDataSnapshot*>(snapshot)->DrawData.CmdLists.Size;
}

imgui_draw_list* trs_snapshot_get_cmd_list(trs_snapshot* snapshot, int index) {
    ImDrawData& dd = reinterpret_cast<ImDrawDataSnapshot*>(snapshot)->DrawData;
    if (index < 0 || index >= dd.CmdLists.Size) {
        return nullptr;
    }
    return reinterpret_cast<imgui_draw_list*>(dd.CmdLists[index]);
}

// ---- ImTextureQueue ----

trs_texture_queue* trs_texture_queue_create(void) {
    return reinterpret_cast<trs_texture_queue*>(new ImTextureQueue());
}

void trs_texture_queue_destroy(trs_texture_queue* queue) {
    delete reinterpret_cast<ImTextureQueue*>(queue);
}

void trs_texture_queue_set_in_flight_frames(trs_texture_queue* queue, int frames) {
    reinterpret_cast<ImTextureQueue*>(queue)->InFlightFrames = frames;
}

int trs_texture_queue_get_in_flight_frames(trs_texture_queue* queue) {
    return reinterpret_cast<ImTextureQueue*>(queue)->InFlightFrames;
}

void trs_texture_queue_set_update_callback(trs_texture_queue* queue, trs_update_texture_fn fn) {
    // Store the user fn in a static trampoline table entry keyed by queue.
    // Simpler approach: ImTextureQueue::UpdateTexFunc has exactly the signature
    // void(ImTextureData*) which matches trs_update_texture_fn modulo pointer
    // type; reinterpret is safe because ImTextureData is an opaque handle to
    // backend code anyway.
    reinterpret_cast<ImTextureQueue*>(queue)->UpdateTexFunc =
        reinterpret_cast<void (*)(ImTextureData*)>(fn);
}

void trs_texture_queue_pre_new_frame(trs_texture_queue* queue) {
    reinterpret_cast<ImTextureQueue*>(queue)->PreNewFrame();
}

void trs_texture_queue_queue_requests(trs_texture_queue* queue, imgui_draw_data* draw_data) {
    reinterpret_cast<ImTextureQueue*>(queue)->QueueRequests(to_draw_data(draw_data));
}

void trs_texture_queue_process_requests(trs_texture_queue* queue, imgui_draw_data* draw_data) {
    reinterpret_cast<ImTextureQueue*>(queue)->ProcessRequests(to_draw_data(draw_data));
}

void trs_texture_queue_shutdown(trs_texture_queue* queue) {
    reinterpret_cast<ImTextureQueue*>(queue)->Shutdown();
}

} // extern "C"
