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
 * C API for TextEditor event/async APIs — the hooks a language-server
 * integration needs (LspBridge in the extras folder uses these):
 *
 *   - transaction change callback (per-transaction text edits)
 *   - change callback (delayed, summary notification)
 *   - async autocomplete suggestions injection
 *   - hover / line-number context-menu / text context-menu popup callbacks
 *   - mouse position queries (DocPos at mouse, over-glyph test)
 *
 * Callbacks are C function pointers + an opaque user_data pointer. The
 * JVM/native bindings wrap these into per-editor Kotlin callbacks.
 */

#ifndef TEXT_EDITOR_EVENTS_C_H
#define TEXT_EDITOR_EVENTS_C_H

#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>

#include "imgui_c.h"
#include "text_editor_c.h"

#ifdef __cplusplus
extern "C" {
#endif

// =========================================================================
// Types
// =========================================================================

// A single text change inside a transaction (TextEditor::Change)
typedef struct te_text_change {
    bool insert;         // true = insert, false = delete
    uint64_t start_line; // zero-based
    uint64_t start_index;
    uint64_t end_line;
    uint64_t end_index;
    char* text;          // UTF-8, malloc'd (free with te_string_free)
} te_text_change;

// A set of changes belonging to one transaction
typedef struct te_change_batch {
    te_text_change* changes;
    size_t count;
} te_change_batch;

// Popup/hover callback data (TextEditor::PopupData)
typedef struct te_popup_data {
    uint64_t line;  // zero-based DocPos
    uint64_t index;
    void* user_data;
} te_popup_data;

// Callback signatures
typedef void (*te_transaction_callback_fn)(const te_change_batch* batch, void* user_data);
typedef void (*te_change_callback_fn)(void* user_data);
typedef void (*te_popup_callback_fn)(const te_popup_data* data, void* user_data);

// =========================================================================
// Change / transaction callbacks
// =========================================================================

// Pass a non-null fn to enable, null to disable. delay_ms applies to the
// summary change callback only (0 = every change).
void te_set_transaction_callback(te_editor* editor, te_transaction_callback_fn fn, void* user_data);
void te_set_change_callback(te_editor* editor, te_change_callback_fn fn, int delay_ms, void* user_data);

// Frees a change batch array (each text field + the array itself).
void te_change_batch_free(te_change_batch* batch);

// =========================================================================
// Async autocomplete
// =========================================================================

// Injects suggestions produced asynchronously (LSP lookup thread).
// values: array of UTF-8 strings; the strings are copied.
void te_set_auto_complete_suggestions(te_editor* editor, const char* const* values, size_t count);

// =========================================================================
// Popup / hover callbacks
// =========================================================================

void te_set_line_number_context_menu_callback(te_editor* editor, te_popup_callback_fn fn, void* user_data);
void te_clear_line_number_context_menu_callback(te_editor* editor);
void te_set_text_context_menu_callback(te_editor* editor, te_popup_callback_fn fn, void* user_data);
void te_clear_text_context_menu_callback(te_editor* editor);
void te_set_text_hover_callback(te_editor* editor, te_popup_callback_fn fn, void* user_data);
void te_clear_text_hover_callback(te_editor* editor);

// =========================================================================
// Mouse queries
// =========================================================================

bool te_is_mouse_pos_over_glyph(te_editor* editor, float x, float y);
void te_get_doc_pos_at_mouse_pos(te_editor* editor, float x, float y, uint64_t* out_line, uint64_t* out_index);

#ifdef __cplusplus
}
#endif

#endif // TEXT_EDITOR_EVENTS_C_H