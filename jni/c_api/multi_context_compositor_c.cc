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

#include "imgui_multicontext_compositor.h"

#include "multi_context_compositor_c.h"

extern "C" {

mcc_compositor* mcc_create(void) {
    return reinterpret_cast<mcc_compositor*>(new ImGuiMultiContextCompositor());
}

void mcc_destroy(mcc_compositor* compositor) {
    delete reinterpret_cast<ImGuiMultiContextCompositor*>(compositor);
}

void mcc_add_context(mcc_compositor* compositor, imgui_context* ctx) {
    ImGuiMultiContextCompositor_AddContext(
        reinterpret_cast<ImGuiMultiContextCompositor*>(compositor),
        reinterpret_cast<ImGuiContext*>(ctx));
}

void mcc_remove_context(mcc_compositor* compositor, imgui_context* ctx) {
    ImGuiMultiContextCompositor_RemoveContext(
        reinterpret_cast<ImGuiMultiContextCompositor*>(compositor),
        reinterpret_cast<ImGuiContext*>(ctx));
}

int mcc_get_context_count(mcc_compositor* compositor) {
    return reinterpret_cast<ImGuiMultiContextCompositor*>(compositor)->Contexts.Size;
}

void mcc_pre_new_frame_update_all(mcc_compositor* compositor) {
    ImGuiMultiContextCompositor_PreNewFrameUpdateAll(
        reinterpret_cast<ImGuiMultiContextCompositor*>(compositor));
}

void mcc_post_new_frame_update_one(mcc_compositor* compositor, imgui_context* ctx) {
    ImGuiMultiContextCompositor_PostNewFrameUpdateOne(
        reinterpret_cast<ImGuiMultiContextCompositor*>(compositor),
        reinterpret_cast<ImGuiContext*>(ctx));
}

void mcc_post_end_frame_update_all(mcc_compositor* compositor) {
    ImGuiMultiContextCompositor_PostEndFrameUpdateAll(
        reinterpret_cast<ImGuiMultiContextCompositor*>(compositor));
}

void mcc_show_debug_window(mcc_compositor* compositor) {
    ImGuiMultiContextCompositor_ShowDebugWindow(
        reinterpret_cast<ImGuiMultiContextCompositor*>(compositor));
}

} // extern "C"
