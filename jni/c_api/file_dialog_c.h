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

#ifndef FILE_DIALOG_C_H
#define FILE_DIALOG_C_H

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

typedef struct igfd_dialog igfd_dialog;

// =========================================================================
// Lifecycle
// =========================================================================

igfd_dialog* igfd_create(void);
void igfd_destroy(igfd_dialog* dialog);

// =========================================================================
// Open / display / close
// =========================================================================

void igfd_open_dialog(
    igfd_dialog* dialog,
    const char* key,
    const char* title,
    const char* filters,
    const char* path,
    const char* file_name,
    const char* file_path_name,
    int32_t count_selection_max,
    int flags);

bool igfd_display_dialog(
    igfd_dialog* dialog,
    const char* key,
    int window_flags,
    imgui_vec2 min_size,
    imgui_vec2 max_size);

void igfd_close_dialog(igfd_dialog* dialog, const char* key);

// =========================================================================
// State queries
// =========================================================================

bool igfd_is_ok(igfd_dialog* dialog);
bool igfd_was_key_opened_this_frame(igfd_dialog* dialog, const char* key);
bool igfd_was_opened_this_frame(igfd_dialog* dialog);
bool igfd_is_key_opened(igfd_dialog* dialog, const char* key);
bool igfd_is_opened(igfd_dialog* dialog);

// =========================================================================
// Results (caller frees with igfd_string_free / igfd_selection_free)
// =========================================================================

char* igfd_get_file_path_name(igfd_dialog* dialog, int result_mode);
char* igfd_get_current_file_name(igfd_dialog* dialog, int result_mode);
char* igfd_get_current_path(igfd_dialog* dialog);
char* igfd_get_current_filter(igfd_dialog* dialog);

// Returns a flat array of 2*count strings: [name0, pathName0, name1, ...].
char** igfd_get_selection(igfd_dialog* dialog, int result_mode, size_t* out_count);
void igfd_selection_free(char** selection, size_t count);
void igfd_string_free(char* str);

// =========================================================================
// File styles
// =========================================================================

void igfd_set_file_style(
    igfd_dialog* dialog,
    unsigned int flags,
    const char* filter,
    float r,
    float g,
    float b,
    float a,
    const char* icon_text);

bool igfd_get_file_style(
    igfd_dialog* dialog,
    unsigned int flags,
    const char* filter,
    float* out_color,
    char** out_icon_text);

void igfd_clear_files_style(igfd_dialog* dialog);

#ifdef __cplusplus
}
#endif

#endif // FILE_DIALOG_C_H
