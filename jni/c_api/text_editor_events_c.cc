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

#include <cstdlib>
#include <cstring>
#include <string>
#include <vector>

#include "TextEditor.h"
#include "text_editor_events_c.h"

namespace {
    char* dup_string(const std::string& s) {
        char* out = static_cast<char*>(std::malloc(s.size() + 1));
        if (out != nullptr) {
            std::memcpy(out, s.c_str(), s.size() + 1);
        }
        return out;
    }
}

extern "C" {

// =========================================================================
// Change / transaction callbacks
// =========================================================================

void te_set_transaction_callback(te_editor* editor, te_transaction_callback_fn fn, void* user_data) {
    TextEditor* te = reinterpret_cast<TextEditor*>(editor);
    if (fn == nullptr) {
        te->SetTransactionCallback(nullptr);
        return;
    }
    te->SetTransactionCallback(
        [fn, user_data](const std::vector<TextEditor::Change>& changes) {
            te_change_batch batch;
            batch.count = changes.size();
            batch.changes = nullptr;
            if (batch.count > 0) {
                batch.changes = static_cast<te_text_change*>(std::calloc(batch.count, sizeof(te_text_change)));
                for (size_t i = 0; i < batch.count; i++) {
                    const auto& c = changes[i];
                    batch.changes[i].insert = c.insert ? true : false;
                    batch.changes[i].start_line = c.start.line;
                    batch.changes[i].start_index = c.start.index;
                    batch.changes[i].end_line = c.end.line;
                    batch.changes[i].end_index = c.end.index;
                    batch.changes[i].text = dup_string(c.text);
                }
            }
            fn(&batch, user_data);
            // The C side owns the batch (boundings may or may not free it);
            // the bindings must call te_change_batch_free. We do NOT free here
            // to let JNI convert it first.
        });
}

void te_set_change_callback(te_editor* editor, te_change_callback_fn fn, int delay_ms, void* user_data) {
    TextEditor* te = reinterpret_cast<TextEditor*>(editor);
    if (fn == nullptr) {
        te->SetChangeCallback(nullptr);
        return;
    }
    te->SetChangeCallback([fn, user_data]() { fn(user_data); }, delay_ms);
}

void te_change_batch_free(te_change_batch* batch) {
    if (batch == nullptr) {
        return;
    }
    if (batch->changes != nullptr) {
        for (size_t i = 0; i < batch->count; i++) {
            std::free(batch->changes[i].text);
        }
        std::free(batch->changes);
    }
    batch->changes = nullptr;
    batch->count = 0;
}

// =========================================================================
// Async autocomplete
// =========================================================================

void te_set_auto_complete_suggestions(te_editor* editor, const char* const* values, uint32_t count) {
    TextEditor* te = reinterpret_cast<TextEditor*>(editor);
    std::vector<std::string> suggestions;
    suggestions.reserve(count);
    for (size_t i = 0; i < count; i++) {
        suggestions.emplace_back(values[i] != nullptr ? values[i] : "");
    }
    te->SetAutoCompleteSuggestions(suggestions);
}

// =========================================================================
// Popup / hover callbacks
// =========================================================================

void te_set_line_number_context_menu_callback(te_editor* editor, te_popup_callback_fn fn, void* user_data) {
    TextEditor* te = reinterpret_cast<TextEditor*>(editor);
    if (fn == nullptr) {
        te->SetLineNumberContextMenuCallback(nullptr);
        return;
    }
    te->SetLineNumberContextMenuCallback(
        [fn, user_data](TextEditor::PopupData& data) {
            te_popup_data pd;
            pd.line = data.pos.line;
            pd.index = data.pos.index;
            pd.user_data = user_data;
            fn(&pd, user_data);
        });
}

void te_clear_line_number_context_menu_callback(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->ClearLineNumberContextMenuCallback();
}

void te_set_text_context_menu_callback(te_editor* editor, te_popup_callback_fn fn, void* user_data) {
    TextEditor* te = reinterpret_cast<TextEditor*>(editor);
    if (fn == nullptr) {
        te->SetTextContextMenuCallback(nullptr);
        return;
    }
    te->SetTextContextMenuCallback(
        [fn, user_data](TextEditor::PopupData& data) {
            te_popup_data pd;
            pd.line = data.pos.line;
            pd.index = data.pos.index;
            pd.user_data = user_data;
            fn(&pd, user_data);
        });
}

void te_clear_text_context_menu_callback(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->ClearTextContextMenuCallback();
}

void te_set_text_hover_callback(te_editor* editor, te_popup_callback_fn fn, void* user_data) {
    TextEditor* te = reinterpret_cast<TextEditor*>(editor);
    if (fn == nullptr) {
        te->SetTextHoverCallback(nullptr);
        return;
    }
    te->SetTextHoverCallback(
        [fn, user_data](TextEditor::PopupData& data) {
            te_popup_data pd;
            pd.line = data.pos.line;
            pd.index = data.pos.index;
            pd.user_data = user_data;
            fn(&pd, user_data);
        });
}

void te_clear_text_hover_callback(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->ClearTextHoverCallback();
}

// =========================================================================
// Mouse queries
// =========================================================================

bool te_is_mouse_pos_over_glyph(te_editor* editor, float x, float y) {
    return reinterpret_cast<TextEditor*>(editor)->IsMousePosOverGlyph(ImVec2(x, y));
}

void te_get_doc_pos_at_mouse_pos(te_editor* editor, float x, float y, uint64_t* out_line, uint64_t* out_index) {
    const auto pos = reinterpret_cast<TextEditor*>(editor)->GetDocPosAtMousePos(ImVec2(x, y));
    if (out_line != nullptr) {
        *out_line = pos.line;
    }
    if (out_index != nullptr) {
        *out_index = pos.index;
    }
}

} // extern "C"