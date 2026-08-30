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

#include "TrieAutoComplete.h"
#include "Notifications.h"

#include "text_editor_extras_c.h"

extern "C" {

// =========================================================================
// TrieAutoComplete
// =========================================================================

te_autocomplete* te_autocomplete_create(void) {
    return reinterpret_cast<te_autocomplete*>(new TrieAutoComplete());
}

void te_autocomplete_destroy(te_autocomplete* ac) {
    delete reinterpret_cast<TrieAutoComplete*>(ac);
}

void te_autocomplete_connect(te_autocomplete* ac, te_editor* editor) {
    reinterpret_cast<TrieAutoComplete*>(ac)->Connect(
        reinterpret_cast<TextEditor*>(editor));
}

void te_autocomplete_disconnect(te_autocomplete* ac) {
    reinterpret_cast<TrieAutoComplete*>(ac)->Disconnect();
}

bool te_autocomplete_is_connected(te_autocomplete* ac) {
    return reinterpret_cast<TrieAutoComplete*>(ac)->IsConnected();
}

// =========================================================================
// Notifications
// =========================================================================

te_notifications* te_notifications_create(void) {
    return reinterpret_cast<te_notifications*>(new Notifications());
}

void te_notifications_destroy(te_notifications* notifications) {
    delete reinterpret_cast<Notifications*>(notifications);
}

void te_notifications_add(te_notifications* notifications, int type, const char* message, int dismiss_time_ms) {
    reinterpret_cast<Notifications*>(notifications)->Add(
        static_cast<Notifications::Type>(type),
        message != nullptr ? message : "",
        dismiss_time_ms);
}

void te_notifications_render(te_notifications* notifications, float pos_x, float pos_y) {
    reinterpret_cast<Notifications*>(notifications)->Render(ImVec2(pos_x, pos_y));
}

// =========================================================================
// Memory
// =========================================================================

void te_string_array_free(char** arr, size_t count) {
    if (arr == nullptr) {
        return;
    }
    for (size_t i = 0; i < count; i++) {
        te_string_free(arr[i]);
    }
    std::free(arr);
}

} // extern "C"