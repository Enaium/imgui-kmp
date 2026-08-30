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
 * C API for the ImGuiColorTextEdit extras:
 *   - TrieAutoComplete (simple trie-based autocomplete)
 *   - Notifications   (header-only "toast" notification stack)
 *
 * LspBridge is NOT exposed: its `lsp/` sublibrary (messagehandler/types/
 * process) is not shipped with the submodule.
 */

#ifndef TEXT_EDITOR_EXTRAS_C_H
#define TEXT_EDITOR_EXTRAS_C_H

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

typedef struct te_autocomplete te_autocomplete;
typedef struct te_notifications te_notifications;

// Notification type (Notifications::Type)
enum te_notification_kind {
    te_notification_success = 0,
    te_notification_warning,
    te_notification_error,
    te_notification_info,
};

// =========================================================================
// TrieAutoComplete
// =========================================================================

te_autocomplete* te_autocomplete_create(void);
void te_autocomplete_destroy(te_autocomplete* ac);

// Connect/Disconnect to/from a TextEditor instance (mutually exclusive).
void te_autocomplete_connect(te_autocomplete* ac, te_editor* editor);
void te_autocomplete_disconnect(te_autocomplete* ac);
bool te_autocomplete_is_connected(te_autocomplete* ac);

// =========================================================================
// Notifications
// =========================================================================

te_notifications* te_notifications_create(void);
void te_notifications_destroy(te_notifications* notifications);

void te_notifications_add(te_notifications* notifications, int type, const char* message, int dismiss_time_ms);
void te_notifications_render(te_notifications* notifications, float pos_x, float pos_y);

// =========================================================================
// Memory
// =========================================================================

void te_string_array_free(char** arr, size_t count);

#ifdef __cplusplus
}
#endif

#endif // TEXT_EDITOR_EXTRAS_C_H