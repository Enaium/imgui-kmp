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
#include <unordered_map>
#include <memory>

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
// Autocomplete configuration
// =========================================================================

// TextEditor stores the AutoCompleteConfig pointer; keep one persistent
// instance active (a C API is allowed one configured editor at a time).
namespace {
    TextEditor::AutoCompleteConfig g_ac_config;
    te_autocomplete_callback_fn g_ac_fn = nullptr;
    void* g_ac_user_data = nullptr;

    void ac_callback(TextEditor::AutoCompleteState& state) {
        if (g_ac_fn == nullptr) {
            return;
        }
        te_autocomplete_state s;
        s.search_term = state.searchTerm.c_str();
        s.search_term_start_line = state.searchTermStart.line;
        s.search_term_start_index = state.searchTermStart.index;
        s.search_term_end_line = state.searchTermEnd.line;
        s.search_term_end_index = state.searchTermEnd.index;
        s.in_identifier = state.inIdentifier ? true : false;
        s.in_number = state.inNumber ? true : false;
        s.in_comment = state.inComment ? true : false;
        s.in_string = state.inString ? true : false;
        s.user_data = g_ac_user_data;

        te_autocomplete_result out{};
        out.suggestions = nullptr;
        out.suggestion_count = 0;
        g_ac_fn(&s, &out);

        state.suggestions.clear();
        for (size_t i = 0; i < out.suggestion_count; i++) {
            if (out.suggestions != nullptr && out.suggestions[i] != nullptr) {
                state.suggestions.emplace_back(out.suggestions[i]);
            }
        }
        state.suggestionsPromise = out.suggestions_promise ? true : false;
    }

    // Line-data callback registry (insertor/deletor), keyed by editor pointer.
    struct LineDataHooks {
        te_insertor_fn insertor = nullptr;
        void* insertor_ud = nullptr;
        te_deletor_fn deletor = nullptr;
        void* deletor_ud = nullptr;
    };
    LineDataHooks g_hooks;
    const void* g_hooks_editor = nullptr;
}

void te_set_auto_complete_config(te_editor* editor, te_autocomplete_callback_fn fn,
                                 void* user_data, bool trigger_on_typing, bool trigger_on_shortcut,
                                 bool trigger_in_comments, bool trigger_in_strings,
                                 bool auto_insert_single_suggestions, int trigger_delay_ms,
                                 unsigned int suggestion_width) {
    TextEditor* te = reinterpret_cast<TextEditor*>(editor);
    if (fn == nullptr) {
        te->SetAutoCompleteConfig(nullptr);
        g_ac_fn = nullptr;
        g_ac_user_data = nullptr;
        return;
    }
    g_ac_fn = fn;
    g_ac_user_data = user_data;
    auto& cfg = g_ac_config;
    cfg.triggerOnTyping = trigger_on_typing ? true : false;
    cfg.triggerOnShortcut = trigger_on_shortcut ? true : false;
    cfg.triggerInComments = trigger_in_comments ? true : false;
    cfg.triggerInStrings = trigger_in_strings ? true : false;
    cfg.autoInsertSingleSuggestions = auto_insert_single_suggestions ? true : false;
    cfg.triggerDelay = std::chrono::milliseconds(trigger_delay_ms);
    cfg.suggestionWidth = suggestion_width;
    cfg.callback = ac_callback;
    cfg.userData = user_data;
    te->SetAutoCompleteConfig(&cfg);
}

// =========================================================================
// Additional text queries and edits
// =========================================================================

char* te_get_section_text(te_editor* editor, uint64_t start_line, uint64_t start_index, uint64_t end_line, uint64_t end_index) {
    TextEditor::DocPos a{static_cast<size_t>(start_line), static_cast<size_t>(start_index)};
    TextEditor::DocPos b{static_cast<size_t>(end_line), static_cast<size_t>(end_index)};
    return dup_string(reinterpret_cast<TextEditor*>(editor)->GetSectionText(a, b));
}

void te_replace_section_text(te_editor* editor, uint64_t start_line, uint64_t start_index, uint64_t end_line, uint64_t end_index, const char* text) {
    TextEditor::DocPos a{static_cast<size_t>(start_line), static_cast<size_t>(start_index)};
    TextEditor::DocPos b{static_cast<size_t>(end_line), static_cast<size_t>(end_index)};
    reinterpret_cast<TextEditor*>(editor)->ReplaceSectionText(a, b, text != nullptr ? text : "");
}

void te_selection_to_lower_case(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->SelectionToLowerCase(); }
void te_selection_to_upper_case(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->SelectionToUpperCase(); }
void te_strip_trailing_whitespaces(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->StripTrailingWhitespaces(); }
void te_tabs_to_spaces(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->TabsToSpaces(); }
void te_spaces_to_tabs(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->SpacesToTabs(); }
void te_indent_lines(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->IndentLines(); }
void te_deindent_lines(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->DeindentLines(); }
void te_move_up_lines(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->MoveUpLines(); }
void te_move_down_lines(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->MoveDownLines(); }
void te_toggle_comments(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->ToggleComments(); }

// =========================================================================
// Additional selection / cursor API
// =========================================================================

void te_select_lines(te_editor* editor, uint64_t start, uint64_t end) { reinterpret_cast<TextEditor*>(editor)->SelectLines(start, end); }
void te_select_region(te_editor* editor, uint64_t start_line, uint64_t start_index, uint64_t end_line, uint64_t end_index) {
    TextEditor::DocPos a{static_cast<size_t>(start_line), static_cast<size_t>(start_index)};
    TextEditor::DocPos b{static_cast<size_t>(end_line), static_cast<size_t>(end_index)};
    reinterpret_cast<TextEditor*>(editor)->SelectRegion(a, b);
}
void te_select_to_brackets(te_editor* editor, bool include_brackets) { reinterpret_cast<TextEditor*>(editor)->SelectToBrackets(include_brackets); }
void te_grow_selections(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->GrowSelections(); }
void te_shrink_selections(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->ShrinkSelections(); }
void te_add_next_occurrence(te_editor* editor, bool whole_word) { reinterpret_cast<TextEditor*>(editor)->AddNextOccurrence(whole_word); }
void te_select_all_occurrences(te_editor* editor, bool whole_word) { reinterpret_cast<TextEditor*>(editor)->SelectAllOccurrences(whole_word); }
void te_clear_cursors(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->ClearCursors(); }

void te_get_cursor_position(te_editor* editor, uint64_t cursor, uint64_t* out_line, uint64_t* out_index) {
    const auto pos = reinterpret_cast<TextEditor*>(editor)->GetCursorPosition(cursor);
    if (out_line != nullptr) *out_line = pos.line;
    if (out_index != nullptr) *out_index = pos.index;
}
void te_get_cursor_selection(te_editor* editor, uint64_t cursor, uint64_t* out_start_line, uint64_t* out_start_index, uint64_t* out_end_line, uint64_t* out_end_index) {
    const auto sel = reinterpret_cast<TextEditor*>(editor)->GetCursorSelection(cursor);
    if (out_start_line != nullptr) *out_start_line = sel.start.line;
    if (out_start_index != nullptr) *out_start_index = sel.start.index;
    if (out_end_line != nullptr) *out_end_line = sel.end.line;
    if (out_end_index != nullptr) *out_end_index = sel.end.index;
}

// =========================================================================
// Word / find query
// =========================================================================

char* te_get_word_at_mouse_pos(te_editor* editor, float x, float y) {
    return dup_string(reinterpret_cast<TextEditor*>(editor)->GetWordAtMousePos(ImVec2(x, y)));
}
void te_find_word_start(te_editor* editor, uint64_t line, uint64_t index, bool whole_word, uint64_t* out_line, uint64_t* out_index) {
    TextEditor::DocPos p{static_cast<size_t>(line), static_cast<size_t>(index)};
    const auto r = reinterpret_cast<TextEditor*>(editor)->FindWordStart(p, whole_word);
    if (out_line != nullptr) *out_line = r.line;
    if (out_index != nullptr) *out_index = r.index;
}
void te_find_word_end(te_editor* editor, uint64_t line, uint64_t index, bool whole_word, uint64_t* out_line, uint64_t* out_index) {
    TextEditor::DocPos p{static_cast<size_t>(line), static_cast<size_t>(index)};
    const auto r = reinterpret_cast<TextEditor*>(editor)->FindWordEnd(p, whole_word);
    if (out_line != nullptr) *out_line = r.line;
    if (out_index != nullptr) *out_index = r.index;
}
bool te_has_find_string(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->HasFindString(); }
void te_find_next(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->FindNext(); }
void te_find_all(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->FindAll(); }
void te_open_find_replace_window(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->OpenFindReplaceWindow(); }
void te_close_find_replace_window(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->CloseFindReplaceWindow(); }
void te_set_find_button_label(te_editor* editor, const char* label) { reinterpret_cast<TextEditor*>(editor)->SetFindButtonLabel(label != nullptr ? label : ""); }
void te_set_find_all_button_label(te_editor* editor, const char* label) { reinterpret_cast<TextEditor*>(editor)->SetFindAllButtonLabel(label != nullptr ? label : ""); }
void te_set_replace_button_label(te_editor* editor, const char* label) { reinterpret_cast<TextEditor*>(editor)->SetReplaceButtonLabel(label != nullptr ? label : ""); }
void te_set_replace_all_button_label(te_editor* editor, const char* label) { reinterpret_cast<TextEditor*>(editor)->SetReplaceAllButtonLabel(label != nullptr ? label : ""); }

// =========================================================================
// Visibility / folding / coordinate transforms
// =========================================================================

bool te_is_mouse_pos_over_text_area(te_editor* editor, float x, float y) { return reinterpret_cast<TextEditor*>(editor)->IsMousePosOverTextArea(ImVec2(x, y)); }
bool te_is_doc_pos_visible(te_editor* editor, uint64_t line, uint64_t index) { return reinterpret_cast<TextEditor*>(editor)->IsDocPosVisible(TextEditor::DocPos{static_cast<size_t>(line), static_cast<size_t>(index)}); }
bool te_is_line_foldable(te_editor* editor, uint64_t line) { return reinterpret_cast<TextEditor*>(editor)->IsLineFoldable(line); }
bool te_is_line_folded(te_editor* editor, uint64_t line) { return reinterpret_cast<TextEditor*>(editor)->IsLineFolded(line); }
bool te_is_line_visible(te_editor* editor, uint64_t line) { return reinterpret_cast<TextEditor*>(editor)->IsLineVisible(line); }
bool te_is_line_hidden(te_editor* editor, uint64_t line) { return reinterpret_cast<TextEditor*>(editor)->IsLineHidden(line); }
void te_fold_around_line(te_editor* editor, uint64_t line) { reinterpret_cast<TextEditor*>(editor)->FoldAroundLine(line); }
void te_unfold_around_line(te_editor* editor, uint64_t line) { reinterpret_cast<TextEditor*>(editor)->UnfoldAroundLine(line); }
void te_toggle_at_line(te_editor* editor, uint64_t line) { reinterpret_cast<TextEditor*>(editor)->ToggleAtLine(line); }
void te_unfold_all(te_editor* editor) { reinterpret_cast<TextEditor*>(editor)->UnfoldAll(); }
uint64_t te_get_first_visible_row(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->GetFirstVisibleRow(); }
uint64_t te_get_first_visible_column(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->GetFirstVisibleColumn(); }
uint64_t te_get_last_visible_row(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->GetLastVisibleRow(); }
uint64_t te_get_last_visible_column(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->GetLastVisibleColumn(); }

void te_doc_pos_to_vis_pos(te_editor* editor, uint64_t line, uint64_t index, uint64_t* out_row, uint64_t* out_column) {
    const auto v = reinterpret_cast<TextEditor*>(editor)->DocPos2VisPos(TextEditor::DocPos{static_cast<size_t>(line), static_cast<size_t>(index)});
    if (out_row != nullptr) *out_row = v.row;
    if (out_column != nullptr) *out_column = v.column;
}
void te_vis_pos_to_doc_pos(te_editor* editor, uint64_t row, uint64_t column, uint64_t* out_line, uint64_t* out_index) {
    const auto p = reinterpret_cast<TextEditor*>(editor)->VisPos2DocPos(TextEditor::VisPos{static_cast<size_t>(row), static_cast<size_t>(column)});
    if (out_line != nullptr) *out_line = p.line;
    if (out_index != nullptr) *out_index = p.index;
}

// =========================================================================
// Undo state / static configuration
// =========================================================================

uint64_t te_get_undo_index(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->GetUndoIndex(); }

void te_set_default_palette(uint32_t text, uint32_t keyword, uint32_t number, uint32_t string,
                            uint32_t comment, uint32_t background, uint32_t cursor, uint32_t selection) {
    auto pal = TextEditor::GetDefaultPalette();
    const size_t c = static_cast<size_t>(TextEditor::Color::count);
    auto set = [&](int idx, uint32_t v) { if (idx >= 0 && idx < static_cast<int>(c)) pal.at(static_cast<size_t>(idx)) = v; };
    set(static_cast<int>(TextEditor::Color::text), text);
    set(static_cast<int>(TextEditor::Color::keyword), keyword);
    set(static_cast<int>(TextEditor::Color::number), number);
    set(static_cast<int>(TextEditor::Color::string), string);
    set(static_cast<int>(TextEditor::Color::comment), comment);
    set(static_cast<int>(TextEditor::Color::background), background);
    set(static_cast<int>(TextEditor::Color::cursor), cursor);
    set(static_cast<int>(TextEditor::Color::selection), selection);
    TextEditor::SetDefaultPalette(pal);
}

void te_get_default_palette(uint32_t* out_text, uint32_t* out_keyword, uint32_t* out_number, uint32_t* out_string,
                            uint32_t* out_comment, uint32_t* out_background, uint32_t* out_cursor, uint32_t* out_selection) {
    const auto& pal = TextEditor::GetDefaultPalette();
    auto get = [&](int idx) -> uint32_t { return pal.at(static_cast<size_t>(idx)); };
    auto setp = [&](uint32_t* dst, int idx) { if (dst != nullptr) *dst = get(idx); };
    setp(out_text, static_cast<int>(TextEditor::Color::text));
    setp(out_keyword, static_cast<int>(TextEditor::Color::keyword));
    setp(out_number, static_cast<int>(TextEditor::Color::number));
    setp(out_string, static_cast<int>(TextEditor::Color::string));
    setp(out_comment, static_cast<int>(TextEditor::Color::comment));
    setp(out_background, static_cast<int>(TextEditor::Color::background));
    setp(out_cursor, static_cast<int>(TextEditor::Color::cursor));
    setp(out_selection, static_cast<int>(TextEditor::Color::selection));
}

void te_set_im_gui_context(uint64_t im_gui_context) {
    TextEditor::SetImGuiContext(reinterpret_cast<ImGuiContext*>(im_gui_context));
}

// =========================================================================
// Remaining configuration toggles
// =========================================================================

void te_set_show_spaces_enabled(te_editor* editor, bool value) { reinterpret_cast<TextEditor*>(editor)->SetShowSpacesEnabled(value); }
bool te_is_show_spaces_enabled(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->IsShowSpacesEnabled(); }
void te_set_show_tabs_enabled(te_editor* editor, bool value) { reinterpret_cast<TextEditor*>(editor)->SetShowTabsEnabled(value); }
bool te_is_show_tabs_enabled(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->IsShowTabsEnabled(); }
void te_set_show_scrollbar_minimap_enabled(te_editor* editor, bool value) { reinterpret_cast<TextEditor*>(editor)->SetShowScrollbarMiniMapEnabled(value); }
bool te_is_show_scrollbar_minimap_enabled(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->IsShowScrollbarMiniMapEnabled(); }
void te_set_show_pan_scroll_indicator_enabled(te_editor* editor, bool value) { reinterpret_cast<TextEditor*>(editor)->SetShowPanScrollIndicatorEnabled(value); }
bool te_is_show_pan_scroll_indicator_enabled(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->IsShowPanScrollIndicatorEnabled(); }
void te_set_minimap_columns(te_editor* editor, uint64_t value) { reinterpret_cast<TextEditor*>(editor)->SetMiniMapColumns(value); }
uint64_t te_get_minimap_columns(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->GetMiniMapColumns(); }
void te_set_line_number_left_margin(te_editor* editor, uint64_t value) { reinterpret_cast<TextEditor*>(editor)->SetLineNumberLeftMargin(value); }
uint64_t te_get_line_number_left_margin(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->GetLineNumberLeftMargin(); }
void te_set_decoration_left_margin(te_editor* editor, uint64_t value) { reinterpret_cast<TextEditor*>(editor)->SetDecorationLeftMargin(value); }
uint64_t te_get_decoration_left_margin(te_editor* editor) { return reinterpret_cast<TextEditor*>(editor)->GetDecorationLeftMargin(); }
void te_set_line_break_config(te_editor* editor, const char* break_after, const char* break_before, bool use_unicode_annex14) {
    TextEditor::LineBreakConfig cfg;
    cfg.useUnicodeAnnex14 = use_unicode_annex14 ? true : false;
    if (break_after != nullptr) cfg.breakAfter = break_after;
    if (break_before != nullptr) cfg.breakBefore = break_before;
    reinterpret_cast<TextEditor*>(editor)->SetLineBreakConfig(cfg);
}

// =========================================================================
// Line data hooks (insertor/deletor/user data)
// =========================================================================

namespace {
    void* insertor_cb(size_t line) {
        if (g_hooks.insertor != nullptr && g_hooks_editor != nullptr) {
            return g_hooks.insertor(line, g_hooks.insertor_ud);
        }
        return nullptr;
    }
    void deletor_cb(size_t line, void* data) {
        if (g_hooks.deletor != nullptr && g_hooks_editor != nullptr) {
            g_hooks.deletor(line, data, g_hooks.deletor_ud);
        }
    }
    void iterate_ud_cb(size_t line, void* data) {
        // iterate is a query callback dispatched inline below; not stored here.
        (void)line; (void)data;
    }
}

void te_set_insertor(te_editor* editor, te_insertor_fn fn, void* user_data) {
    g_hooks.insertor = fn;
    g_hooks.insertor_ud = user_data;
    g_hooks_editor = editor;
    if (fn != nullptr) {
        reinterpret_cast<TextEditor*>(editor)->SetInsertor(insertor_cb);
    } else {
        reinterpret_cast<TextEditor*>(editor)->SetInsertor(nullptr);
    }
}

void te_set_deletor(te_editor* editor, te_deletor_fn fn, void* user_data) {
    g_hooks.deletor = fn;
    g_hooks.deletor_ud = user_data;
    g_hooks_editor = editor;
    if (fn != nullptr) {
        reinterpret_cast<TextEditor*>(editor)->SetDeletor(deletor_cb);
    } else {
        reinterpret_cast<TextEditor*>(editor)->SetDeletor(nullptr);
    }
}

void te_set_user_data(te_editor* editor, uint64_t line, void* data) { reinterpret_cast<TextEditor*>(editor)->SetUserData(line, data); }
void* te_get_user_data(te_editor* editor, uint64_t line) { return reinterpret_cast<TextEditor*>(editor)->GetUserData(line); }
void te_iterate_user_data(te_editor* editor, te_iterate_user_data_fn fn, void* user_data) {
    if (fn != nullptr) {
        reinterpret_cast<TextEditor*>(editor)->IterateUserData([fn, user_data](size_t line, void* data) {
            fn(line, data, user_data);
        });
    }
}

// =========================================================================
// Custom tokenizer (LSP semantic tokens)
// =========================================================================

namespace {
    struct te_tokenizer_state {
        te_tokenizer_fn fn = nullptr;
        void* user_data = nullptr;
        TextEditor::Language language;              // copy with tokenizer installed
        const TextEditor::Language* original = nullptr; // language to restore on clear
        const ImWchar* prev_end = nullptr;          // one-past codepoint of last span
        int64_t line = -1;
    };

    std::unordered_map<TextEditor*, std::shared_ptr<te_tokenizer_state>>& te_tokenizer_states() {
        static std::unordered_map<TextEditor*, std::shared_ptr<te_tokenizer_state>> states;
        return states;
    }

    // Converts an iterator span to a UTF-8 string.
    inline std::string te_iter_to_string(TextEditor::Iterator start, TextEditor::Iterator end) {
        std::string text;
        for (auto i = start; i < end; ++i) {
            char utf8[4];
            text.append(utf8, TextEditor::CodePoint::write(utf8, *i));
        }
        return text;
    }
}

// Language::customTokenizer trampoline. Line numbers are tracked linearly:
// the colorizer calls us with contiguous spans along each line, so a new line
// is detected whenever the start codepoint differs from our previous end.
static TextEditor::Iterator te_custom_tokenizer_cb(
    TextEditor::Iterator start, TextEditor::Iterator end, TextEditor::Color& color,
    te_tokenizer_state* st) {
    const ImWchar* first = start.operator->();
    const size_t length = static_cast<size_t>(end - start);
    if (st->prev_end == nullptr || first != st->prev_end) {
        st->line++;
    }

    const auto text = te_iter_to_string(start, end);
    int64_t idx = st->fn(st->user_data, st->line, text.c_str(), text.size());
    if (idx < 0) idx = static_cast<int64_t>(TextEditor::Color::text);
    color = static_cast<TextEditor::Color>(idx & 0xFF);
    st->prev_end = first + length;
    return end;
}

void te_set_custom_tokenizer(te_editor* editor, te_tokenizer_fn fn, void* user_data) {
    TextEditor* te = reinterpret_cast<TextEditor*>(editor);
    auto& states = te_tokenizer_states();
    if (fn == nullptr) {
        auto it = states.find(te);
        if (it != states.end()) {
            te->SetLanguage(it->second->original);
            states.erase(it);
        }
        return;
    }
    auto state = std::make_shared<te_tokenizer_state>();
    state->fn = fn;
    state->user_data = user_data;
    state->original = te->GetLanguage();
    state->language = *state->original;
    state->language.customTokenizer = [state](TextEditor::Iterator start, TextEditor::Iterator end, TextEditor::Color& color) {
        return te_custom_tokenizer_cb(start, end, color, state.get());
    };
    te->SetLanguage(&state->language);
    states[te] = state;
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