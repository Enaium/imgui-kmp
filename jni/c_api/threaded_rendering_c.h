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

#ifndef THREADED_RENDERING_C_H
#define THREADED_RENDERING_C_H

#include <stdbool.h>
#include <stdint.h>

#include "imgui_c.h"

#ifdef __cplusplus
extern "C" {
#endif

// =========================================================================
// Types
// =========================================================================

typedef struct trs_snapshot trs_snapshot;   // ImDrawDataSnapshot
typedef struct trs_texture_queue trs_texture_queue; // ImTextureQueue

// Mirrors ImTextureStatus (imgui.h).
enum {
    trs_texture_status_ok = 0,
    trs_texture_status_destroyed = 1,
    trs_texture_status_want_create = 2,
    trs_texture_status_want_updates = 3,
    trs_texture_status_want_destroy = 4,
};

// =========================================================================
// ImDrawDataSnapshot
//
// The snapshot owns a deep copy of the frame's draw lists so the render thread
// can draw while the update thread builds the next frame. Only the fields the
// renderer backend needs are exposed; rendering goes through the same accessors
// as imgui_draw_data_*.
// =========================================================================

trs_snapshot* trs_snapshot_create(void);
void trs_snapshot_destroy(trs_snapshot* snapshot);

// Efficient snapshot by swapping buffers: `src` is unusable afterwards. Pass
// the imgui_draw_data* obtained from imgui_get_draw_data() on the update thread.
void trs_snapshot_snap_using_swap(trs_snapshot* snapshot, imgui_draw_data* src, double current_time);

// Releases all owned draw lists. Call after joining threads, before destroying
// the ImGui context (required since 1.92.0).
void trs_snapshot_clear(trs_snapshot* snapshot);

float trs_snapshot_get_memory_compact_timer(trs_snapshot* snapshot);
void trs_snapshot_set_memory_compact_timer(trs_snapshot* snapshot, float seconds);

// --- Snapshot draw data accessors (render thread) ---

bool trs_snapshot_is_valid(trs_snapshot* snapshot);
int trs_snapshot_get_frame_count(trs_snapshot* snapshot);
int trs_snapshot_get_total_idx_count(trs_snapshot* snapshot);
int trs_snapshot_get_total_vtx_count(trs_snapshot* snapshot);
imgui_vec2 trs_snapshot_get_display_pos(trs_snapshot* snapshot);
imgui_vec2 trs_snapshot_get_display_size(trs_snapshot* snapshot);
imgui_vec2 trs_snapshot_get_framebuffer_scale(trs_snapshot* snapshot);
int trs_snapshot_get_cmd_lists_count(trs_snapshot* snapshot);
imgui_draw_list* trs_snapshot_get_cmd_list(trs_snapshot* snapshot, int index);

// =========================================================================
// ImTextureQueue
//
// All functions must be called under the same user-provided mutex (Kotlin side:
// synchronized on the queue instance). The backend texture-update callback is
// NOT exposed through Kotlin: it must remain on the native side because it
// touches GPU resources. Use trs_texture_queue_set_update_callback to register
// a C function pointer from your native backend code.
// =========================================================================

trs_texture_queue* trs_texture_queue_create(void);
void trs_texture_queue_destroy(trs_texture_queue* queue);

void trs_texture_queue_set_in_flight_frames(trs_texture_queue* queue, int frames);
int trs_texture_queue_get_in_flight_frames(trs_texture_queue* queue);

// Registers the backend texture handler (e.g. ImGui_ImplDX11_UpdateTexture).
typedef void (*trs_update_texture_fn)(void* tex);
void trs_texture_queue_set_update_callback(trs_texture_queue* queue, trs_update_texture_fn fn);

// (Update thread) Call before NewFrame(). Acknowledges retired destroys.
void trs_texture_queue_pre_new_frame(trs_texture_queue* queue);

// (Update thread) Call after Render(), before publishing the snapshot.
void trs_texture_queue_queue_requests(trs_texture_queue* queue, imgui_draw_data* draw_data);

// (Render thread) Process staged requests before rendering the snapshot.
// Pass null draw_data only via shutdown().
void trs_texture_queue_process_requests(trs_texture_queue* queue, imgui_draw_data* draw_data);

// Shutdown. Call after joining threads.
void trs_texture_queue_shutdown(trs_texture_queue* queue);

#ifdef __cplusplus
}
#endif

#endif // THREADED_RENDERING_C_H
