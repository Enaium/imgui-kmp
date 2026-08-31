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

/*
 * C API for imgui_markdown (enkisoftware/imgui_markdown), a single-header
 * Markdown renderer for Dear ImGui.
 *
 * A md_config holds a MarkdownConfig: link/tooltip/image callbacks (C
 * function pointers + a user_data pointer forwarded to all callbacks), the
 * link icon text, three heading font formats (font + separator) and the
 * format flags. Rendering uses the library's default format callback so the
 * Kotlin side does not need to implement text formatting; the three callbacks
 * remain optional hooks.
 */

#ifndef MARKDOWN_C_H
#define MARKDOWN_C_H

#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>

#include "imgui_c.h"

#ifdef __cplusplus
extern "C" {
#endif

// =========================================================================
// Types
// =========================================================================

typedef struct md_config md_config;

// Data passed to link/image callbacks (ImGui::MarkdownLinkCallbackData)
typedef struct md_link_data {
    const char* text;    // text between square brackets [] (may be null)
    int text_length;
    const char* link;    // text between brackets () (may be null)
    int link_length;
    void* user_data;
    bool is_image;       // true if '!' precedes the link syntax
} md_link_data;

// Image payload an image callback may return (ImGui::MarkdownImageData)
typedef struct md_image_data {
    bool is_valid;
    bool use_link_callback;
    uint64_t user_texture_id; // ImTextureID
    imgui_vec2 size;
    imgui_vec2 uv0;
    imgui_vec2 uv1;
    imgui_vec4 tint_col;
    imgui_vec4 border_col;
    imgui_vec4 bg_col;
} md_image_data;

// Callback signatures
typedef void (*md_link_callback_fn)(const md_link_data* data);
typedef void (*md_tooltip_callback_fn)(const md_link_data* data, const char* link_icon);
typedef md_image_data (*md_image_callback_fn)(const md_link_data* data);

// =========================================================================
// Lifecycle / config
// =========================================================================

md_config* md_create(void);
void md_destroy(md_config* config);

// link icon text shown in tooltips (copied)
void md_set_link_icon(md_config* config, const char* icon);

// Heading font + separator for heading level 1..3; font is an ImFont*
// obtained from the running context (0 = default font).
void md_set_heading(md_config* config, int level, uint64_t font, bool separator);

// Format flags (ImGuiMarkdownFormatFlags) bitmask directly.
void md_set_format_flags(md_config* config, int flags);

// Registers callbacks; pass null to disable. All callbacks receive the
// config's user_data. Image callback must fill and return md_image_data.
void md_set_link_callback(md_config* config, md_link_callback_fn fn, void* user_data);
void md_set_tooltip_callback(md_config* config, md_tooltip_callback_fn fn, void* user_data);
void md_set_image_callback(md_config* config, md_image_callback_fn fn, void* user_data);

// =========================================================================
// Render
// =========================================================================

void md_render(md_config* config, const char* markdown, uint32_t length);

#ifdef __cplusplus
}
#endif

#endif // MARKDOWN_C_H