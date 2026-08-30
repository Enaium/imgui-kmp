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

#include "TextEditor.h"
#include "text_editor_c.h"

namespace {
    // The header may expose classes either at global scope or inside a
    // namespace; detect which one is in effect and alias it uniformly.
    // (TextEditor (Johan Goossens) keeps the classes at global scope as of
    // v1.92.9, so `TextEditor` resolves globally below.)

    const TextEditor::Language* language_of_kind(int kind) {
        switch (kind) {
            case te_language_c: return TextEditor::Language::C();
            case te_language_cpp: return TextEditor::Language::Cpp();
            case te_language_cs: return TextEditor::Language::Cs();
            case te_language_angelscript: return TextEditor::Language::AngelScript();
            case te_language_lua: return TextEditor::Language::Lua();
            case te_language_python: return TextEditor::Language::Python();
            case te_language_glsl: return TextEditor::Language::Glsl();
            case te_language_hlsl: return TextEditor::Language::Hlsl();
            case te_language_json: return TextEditor::Language::Json();
            case te_language_markdown: return TextEditor::Language::Markdown();
            case te_language_sql: return TextEditor::Language::Sql();
            default: return nullptr;
        }
    }

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
// Lifecycle
// =========================================================================

te_editor* te_create(void) {
    return reinterpret_cast<te_editor*>(new TextEditor());
}

void te_destroy(te_editor* editor) {
    delete reinterpret_cast<TextEditor*>(editor);
}

// =========================================================================
// Text
// =========================================================================

void te_set_text(te_editor* editor, const char* text) {
    reinterpret_cast<TextEditor*>(editor)->SetText(text != nullptr ? text : "");
}

char* te_get_text(te_editor* editor) {
    return dup_string(reinterpret_cast<TextEditor*>(editor)->GetText());
}

void te_clear_text(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->ClearText();
}

bool te_is_empty(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsEmpty();
}

uint64_t te_get_line_count(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->GetLineCount();
}

char* te_get_line_text(te_editor* editor, uint64_t line) {
    return dup_string(reinterpret_cast<TextEditor*>(editor)->GetLineText(line));
}

// =========================================================================
// Rendering
// =========================================================================

bool te_render(te_editor* editor, const char* title, float size_x, float size_y, int child_flags, int window_flags) {
    return reinterpret_cast<TextEditor*>(editor)->Render(
        title != nullptr ? title : "", ImVec2(size_x, size_y), static_cast<ImGuiChildFlags>(child_flags),
        static_cast<ImGuiWindowFlags>(window_flags));
}

void te_set_focus(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->SetFocus();
}

// =========================================================================
// Configuration
// =========================================================================

#define TE_IMPL_GET(fn)                    \
    uint64_t te_get_tab_size(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->fn(); }

void te_set_tab_size(te_editor* editor, uint64_t value) {
    reinterpret_cast<TextEditor*>(editor)->SetTabSize(value);
}
uint64_t te_get_tab_size(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->GetTabSize();
}
void te_set_insert_spaces_on_tabs(te_editor* editor, bool value) {
    reinterpret_cast<TextEditor*>(editor)->SetInsertSpacesOnTabs(value);
}
bool te_is_insert_spaces_on_tabs(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsInsertSpacesOnTabs();
}
void te_set_line_spacing(te_editor* editor, float value) {
    reinterpret_cast<TextEditor*>(editor)->SetLineSpacing(value);
}
float te_get_line_spacing(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->GetLineSpacing();
}
void te_set_word_wrap_enabled(te_editor* editor, bool value) {
    reinterpret_cast<TextEditor*>(editor)->SetWordWrapEnabled(value);
}
bool te_is_word_wrap_enabled(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsWordWrapEnabled();
}
void te_set_read_only_enabled(te_editor* editor, bool value) {
    reinterpret_cast<TextEditor*>(editor)->SetReadOnlyEnabled(value);
}
bool te_is_read_only_enabled(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsReadOnlyEnabled();
}
void te_set_carets_visible(te_editor* editor, bool value) {
    reinterpret_cast<TextEditor*>(editor)->SetCaretsVisible(value);
}
bool te_is_carets_visible(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsCaretsVisible();
}
void te_set_auto_indent_enabled(te_editor* editor, bool value) {
    reinterpret_cast<TextEditor*>(editor)->SetAutoIndentEnabled(value);
}
bool te_is_auto_indent_enabled(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsAutoIndentEnabled();
}
void te_set_show_whitespaces_enabled(te_editor* editor, bool value) {
    reinterpret_cast<TextEditor*>(editor)->SetShowWhitespacesEnabled(value);
}
bool te_is_show_whitespaces_enabled(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsShowWhitespacesEnabled();
}
void te_set_show_line_numbers_enabled(te_editor* editor, bool value) {
    reinterpret_cast<TextEditor*>(editor)->SetShowLineNumbersEnabled(value);
}
bool te_is_show_line_numbers_enabled(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsShowLineNumbersEnabled();
}
void te_set_show_minimap_enabled(te_editor* editor, bool value) {
    reinterpret_cast<TextEditor*>(editor)->SetShowMiniMapEnabled(value);
}
bool te_is_show_minimap_enabled(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsShowMiniMapEnabled();
}
void te_set_show_matching_brackets(te_editor* editor, bool value) {
    reinterpret_cast<TextEditor*>(editor)->SetShowMatchingBrackets(value);
}
bool te_is_showing_matching_brackets(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsShowingMatchingBrackets();
}
void te_set_complete_paired_glyphs(te_editor* editor, bool value) {
    reinterpret_cast<TextEditor*>(editor)->SetCompletePairedGlyphs(value);
}
bool te_is_completing_paired_glyphs(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsCompletingPairedGlyphs();
}
void te_set_line_folding_enabled(te_editor* editor, bool value) {
    reinterpret_cast<TextEditor*>(editor)->SetLineFoldingEnabled(value);
}
bool te_is_line_folding_enabled(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsLineFoldingEnabled();
}
void te_set_overwrite_enabled(te_editor* editor, bool value) {
    reinterpret_cast<TextEditor*>(editor)->SetOverwriteEnabled(value);
}
bool te_is_overwrite_enabled(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsOverwriteEnabled();
}
void te_set_middle_mouse_scroll_mode(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->SetMiddleMouseScrollMode();
}
void te_set_middle_mouse_pan_mode(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->SetMiddleMousePanMode();
}
bool te_is_middle_mouse_pan_mode(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->IsMiddleMousePanMode();
}
void te_set_text_left_margin(te_editor* editor, uint64_t value) {
    reinterpret_cast<TextEditor*>(editor)->SetTextLeftMargin(value);
}
uint64_t te_get_text_left_margin(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->GetTextLeftMargin();
}

// =========================================================================
// Language & palette
// =========================================================================

void te_set_language(te_editor* editor, int language_kind) {
    reinterpret_cast<TextEditor*>(editor)->SetLanguage(language_of_kind(language_kind));
}

char* te_get_language_name(te_editor* editor) {
    return dup_string(reinterpret_cast<TextEditor*>(editor)->GetLanguageName());
}

bool te_get_palette_color(te_editor* editor, int color, uint32_t* out) {
    if (out == nullptr || color < 0 || color >= te_color_count) {
        return false;
    }
    const auto& palette = reinterpret_cast<TextEditor*>(editor)->GetPalette();
    *out = static_cast<uint32_t>(palette.at(static_cast<size_t>(color)));
    return true;
}

void te_set_palette_color(te_editor* editor, int color, uint32_t value) {
    if (color < 0 || color >= te_color_count) {
        return;
    }
    TextEditor* te = reinterpret_cast<TextEditor*>(editor);
    auto palette = te->GetPalette();
    palette.at(static_cast<size_t>(color)) = value;
    te->SetPalette(palette);
}

void te_set_default_dark_palette(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->SetPalette(TextEditor::GetDarkPalette());
}

void te_set_default_light_palette(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->SetPalette(TextEditor::GetLightPalette());
}

// =========================================================================
// Clipboard / undo
// =========================================================================

void te_cut(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->Cut();
}
void te_copy(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->Copy();
}
void te_paste(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->Paste();
}
void te_undo(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->Undo();
}
void te_redo(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->Redo();
}
bool te_can_undo(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->CanUndo();
}
bool te_can_redo(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->CanRedo();
}

// =========================================================================
// Selection
// =========================================================================

void te_select_all(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->SelectAll();
}
void te_select_line(te_editor* editor, uint64_t line) {
    reinterpret_cast<TextEditor*>(editor)->SelectLine(line);
}
bool te_any_cursor_has_selection(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->AnyCursorHasSelection();
}
char* te_get_selected_text(te_editor* editor) {
    TextEditor* te = reinterpret_cast<TextEditor*>(editor);
    if (!te->AnyCursorHasSelection()) {
        return nullptr;
    }
    const auto sel = te->GetCurrentCursorSelection();
    return dup_string(te->GetSectionText(sel.start, sel.end));
}

// =========================================================================
// Find / replace
// =========================================================================

void te_select_first_occurrence_of(te_editor* editor, const char* text, bool case_sensitive, bool whole_word) {
    reinterpret_cast<TextEditor*>(editor)->SelectFirstOccurrenceOf(text != nullptr ? text : "", case_sensitive, whole_word);
}
void te_select_next_occurrence_of(te_editor* editor, const char* text, bool case_sensitive, bool whole_word) {
    reinterpret_cast<TextEditor*>(editor)->SelectNextOccurrenceOf(text != nullptr ? text : "", case_sensitive, whole_word);
}
void te_replace_text_in_current_cursor(te_editor* editor, const char* text) {
    reinterpret_cast<TextEditor*>(editor)->ReplaceTextInCurrentCursor(text != nullptr ? text : "");
}
void te_replace_text_in_all_cursors(te_editor* editor, const char* text) {
    reinterpret_cast<TextEditor*>(editor)->ReplaceTextInAllCursors(text != nullptr ? text : "");
}

// =========================================================================
// Cursor / scrolling
// =========================================================================

uint64_t te_get_number_of_cursors(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->GetNumberOfCursors();
}
char* te_get_cursor_text(te_editor* editor, uint64_t cursor) {
    return dup_string(reinterpret_cast<TextEditor*>(editor)->GetCursorText(cursor));
}
void te_scroll_to_line(te_editor* editor, uint64_t line, int alignment) {
    reinterpret_cast<TextEditor*>(editor)->ScrollToLine(line, static_cast<TextEditor::Scroll>(alignment));
}
void te_set_cursor_pos(te_editor* editor, uint64_t line, uint64_t index) {
    TextEditor::DocPos pos;
    pos.line = line;
    pos.index = index;
    reinterpret_cast<TextEditor*>(editor)->SetCursor(pos);
}
void te_get_cursor_pos(te_editor* editor, uint64_t* out_line, uint64_t* out_index) {
    const auto pos = reinterpret_cast<TextEditor*>(editor)->GetCursorPosition(0);
    if (out_line != nullptr) {
        *out_line = pos.line;
    }
    if (out_index != nullptr) {
        *out_index = pos.index;
    }
}
float te_get_line_height(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->GetLineHeight();
}
float te_get_glyph_width(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->GetGlyphWidth();
}

// =========================================================================
// Markers
// =========================================================================

void te_add_marker(te_editor* editor, uint64_t line, uint32_t line_number_color, uint32_t text_color, const char* line_number_tooltip, const char* text_tooltip) {
    reinterpret_cast<TextEditor*>(editor)->AddMarker(line, line_number_color, text_color, line_number_tooltip != nullptr ? line_number_tooltip : "",
                                                    text_tooltip != nullptr ? text_tooltip : "");
}
void te_clear_markers(te_editor* editor) {
    reinterpret_cast<TextEditor*>(editor)->ClearMarkers();
}
bool te_has_markers(te_editor* editor) {
    return reinterpret_cast<TextEditor*>(editor)->HasMarkers();
}

// =========================================================================
// Memory
// =========================================================================

char* te_string_dup(const char* s) {
    return dup_string(s != nullptr ? s : "");
}

void te_string_free(char* s) {
    std::free(s);
}

} // extern "C"