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

#ifndef MULTI_CONTEXT_COMPOSITOR_C_H
#define MULTI_CONTEXT_COMPOSITOR_C_H

#include <stdbool.h>
#include <stdint.h>

#include "imgui_c.h"

#ifdef __cplusplus
extern "C" {
#endif

// =========================================================================
// Types
// =========================================================================

typedef struct mcc_compositor mcc_compositor;

// =========================================================================
// Lifecycle
// =========================================================================

mcc_compositor* mcc_create(void);
void mcc_destroy(mcc_compositor* compositor);

// =========================================================================
// Context management
// =========================================================================

void mcc_add_context(mcc_compositor* compositor, imgui_context* ctx);
void mcc_remove_context(mcc_compositor* compositor, imgui_context* ctx);
int mcc_get_context_count(mcc_compositor* compositor);

// =========================================================================
// Frame integration (see header usage block in imgui_club)
// =========================================================================

// Call at a shared sync point before calling NewFrame() on any context.
void mcc_pre_new_frame_update_all(mcc_compositor* compositor);

// Call after calling NewFrame() on a given context.
void mcc_post_new_frame_update_one(mcc_compositor* compositor, imgui_context* ctx);

// Call at a shared sync point after calling EndFrame() on all contexts.
void mcc_post_end_frame_update_all(mcc_compositor* compositor);

// =========================================================================
// Debug display
// =========================================================================

void mcc_show_debug_window(mcc_compositor* compositor);

#ifdef __cplusplus
}
#endif

#endif // MULTI_CONTEXT_COMPOSITOR_C_H
