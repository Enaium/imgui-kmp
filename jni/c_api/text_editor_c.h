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
 * C API for ImGuiColorTextEdit (v1.92.9+, https://github.com/goossens/ImGuiColorTextEdit).
 *
 * Binds the TextEditor class: text access, rendering, language selection,
 * palette, configuration toggles, clipboard/undo, selection, find/replace,
 * scrolling and markers. Callbacks (change/transaction/decoration/caret/
 * context-menu) and the Document/Config internals are not exposed in this
 * subset.
 */

#ifndef TEXT_EDITOR_C_H
#define TEXT_EDITOR_C_H

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

typedef struct te_editor te_editor;

// Built-in languages (TextEditor::Language::C/Cpp/Cs/AngelScript/Lua/...)
enum te_language_kind {
    te_language_c = 0,
    te_language_cpp,
    te_language_cs,
    te_language_angelscript,
    te_language_lua,
    te_language_python,
    te_language_glsl,
    te_language_hlsl,
    te_language_json,
    te_language_markdown,
    te_language_sql,
};

// Palette color indices (TextEditor::Color)
enum te_color {
    te_color_text = 0,
    te_color_keyword,
    te_color_declaration,
    te_color_number,
    te_color_string,
    te_color_punctuation,
    te_color_preprocessor,
    te_color_identifier,
    te_color_known_identifier,
    te_color_comment,
    te_color_background,
    te_color_cursor,
    te_color_selection,
    te_color_whitespace,
    te_color_matching_bracket_background,
    te_color_matching_bracket_active,
    te_color_matching_bracket_level1,
    te_color_matching_bracket_level2,
    te_color_matching_bracket_level3,
    te_color_matching_bracket_error,
    te_color_line_number,
    te_color_current_line_number,
    te_color_count,
};

// Scroll alignment for ScrollToLine
enum te_scroll_alignment {
    te_scroll_align_top = 0,
    te_scroll_align_middle,
    te_scroll_align_bottom,
};

// =========================================================================
// Lifecycle
// =========================================================================

te_editor* te_create(void);
void te_destroy(te_editor* editor);

// =========================================================================
// Text
// =========================================================================

void te_set_text(te_editor* editor, const char* text);
char* te_get_text(te_editor* editor);   // caller must te_string_free
void te_clear_text(te_editor* editor);
bool te_is_empty(te_editor* editor);
uint64_t te_get_line_count(te_editor* editor);
char* te_get_line_text(te_editor* editor, uint64_t line); // caller frees

// =========================================================================
// Rendering
// =========================================================================

bool te_render(te_editor* editor, const char* title, float size_x, float size_y, int child_flags, int window_flags);
void te_set_focus(te_editor* editor);

// =========================================================================
// Configuration
// =========================================================================

void te_set_tab_size(te_editor* editor, uint64_t value);
uint64_t te_get_tab_size(te_editor* editor);
void te_set_insert_spaces_on_tabs(te_editor* editor, bool value);
bool te_is_insert_spaces_on_tabs(te_editor* editor);
void te_set_line_spacing(te_editor* editor, float value);
float te_get_line_spacing(te_editor* editor);
void te_set_word_wrap_enabled(te_editor* editor, bool value);
bool te_is_word_wrap_enabled(te_editor* editor);
void te_set_read_only_enabled(te_editor* editor, bool value);
bool te_is_read_only_enabled(te_editor* editor);
void te_set_carets_visible(te_editor* editor, bool value);
bool te_is_carets_visible(te_editor* editor);
void te_set_auto_indent_enabled(te_editor* editor, bool value);
bool te_is_auto_indent_enabled(te_editor* editor);
void te_set_show_whitespaces_enabled(te_editor* editor, bool value);
bool te_is_show_whitespaces_enabled(te_editor* editor);
void te_set_show_line_numbers_enabled(te_editor* editor, bool value);
bool te_is_show_line_numbers_enabled(te_editor* editor);
void te_set_show_minimap_enabled(te_editor* editor, bool value);
bool te_is_show_minimap_enabled(te_editor* editor);
void te_set_show_matching_brackets(te_editor* editor, bool value);
bool te_is_showing_matching_brackets(te_editor* editor);
void te_set_complete_paired_glyphs(te_editor* editor, bool value);
bool te_is_completing_paired_glyphs(te_editor* editor);
void te_set_line_folding_enabled(te_editor* editor, bool value);
bool te_is_line_folding_enabled(te_editor* editor);
void te_set_overwrite_enabled(te_editor* editor, bool value);
bool te_is_overwrite_enabled(te_editor* editor);
void te_set_middle_mouse_scroll_mode(te_editor* editor);
void te_set_middle_mouse_pan_mode(te_editor* editor);
bool te_is_middle_mouse_pan_mode(te_editor* editor);
void te_set_text_left_margin(te_editor* editor, uint64_t value);
uint64_t te_get_text_left_margin(te_editor* editor);

// =========================================================================
// Language & palette
// =========================================================================

void te_set_language(te_editor* editor, int language_kind); // te_language_kind, -1 to clear
char* te_get_language_name(te_editor* editor);              // caller frees

// Palette: colors are packed 0xRRGGBBAA (ImU32). get writes count into out_count.
bool te_get_palette_color(te_editor* editor, int color, uint32_t* out);
void te_set_palette_color(te_editor* editor, int color, uint32_t value);
void te_set_default_dark_palette(te_editor* editor);
void te_set_default_light_palette(te_editor* editor);

// =========================================================================
// Clipboard / undo
// =========================================================================

void te_cut(te_editor* editor);
void te_copy(te_editor* editor);
void te_paste(te_editor* editor);
void te_undo(te_editor* editor);
void te_redo(te_editor* editor);
bool te_can_undo(te_editor* editor);
bool te_can_redo(te_editor* editor);

// =========================================================================
// Selection
// =========================================================================

void te_select_all(te_editor* editor);
void te_select_line(te_editor* editor, uint64_t line);
bool te_any_cursor_has_selection(te_editor* editor);
char* te_get_selected_text(te_editor* editor); // caller frees; main cursor

// =========================================================================
// Find / replace
// =========================================================================

void te_select_first_occurrence_of(te_editor* editor, const char* text, bool case_sensitive, bool whole_word);
void te_select_next_occurrence_of(te_editor* editor, const char* text, bool case_sensitive, bool whole_word);
void te_replace_text_in_current_cursor(te_editor* editor, const char* text);
void te_replace_text_in_all_cursors(te_editor* editor, const char* text);

// =========================================================================
// Cursor / scrolling
// =========================================================================

uint64_t te_get_number_of_cursors(te_editor* editor);
char* te_get_cursor_text(te_editor* editor, uint64_t cursor); // caller frees
void te_scroll_to_line(te_editor* editor, uint64_t line, int alignment);
void te_set_cursor_pos(te_editor* editor, uint64_t line, uint64_t index);
void te_get_cursor_pos(te_editor* editor, uint64_t* out_line, uint64_t* out_index);
float te_get_line_height(te_editor* editor);
float te_get_glyph_width(te_editor* editor);

// =========================================================================
// Markers
// =========================================================================

void te_add_marker(te_editor* editor, uint64_t line, uint32_t line_number_color, uint32_t text_color, const char* line_number_tooltip, const char* text_tooltip);
void te_clear_markers(te_editor* editor);
bool te_has_markers(te_editor* editor);

// =========================================================================
// Autocomplete configuration
// =========================================================================

// Mirrors TextEditor::AutoCompleteState (search context passed to the callback)
typedef struct te_autocomplete_state {
    const char* search_term; // UTF-8, valid for the callback duration
    uint64_t search_term_start_line;
    uint64_t search_term_start_index;
    uint64_t search_term_end_line;
    uint64_t search_term_end_index;
    bool in_identifier;
    bool in_number;
    bool in_comment;
    bool in_string;
    void* user_data;
} te_autocomplete_state;

// Output buffer the callback fills (mirrors AutoCompleteState suggestions)
typedef struct te_autocomplete_result {
    char** suggestions; // array of UTF-8 strings (malloc'd copies)
    uint32_t suggestion_count;
    bool suggestions_promise; // set if async lookup deferred via te_set_auto_complete_suggestions
} te_autocomplete_result;

typedef void (*te_autocomplete_callback_fn)(const te_autocomplete_state* state, te_autocomplete_result* out);

// Configure and activate autocomplete; pass null fn to deactivate. Config is
// copied inside. trigger_on_typing/shortcut/comments/strings, auto_insert,
// trigger_delay_ms, suggestion_width map to AutoCompleteConfig fields.
void te_set_auto_complete_config(te_editor* editor, te_autocomplete_callback_fn fn,
                                 void* user_data, bool trigger_on_typing, bool trigger_on_shortcut,
                                 bool trigger_in_comments, bool trigger_in_strings,
                                 bool auto_insert_single_suggestions, int trigger_delay_ms,
                                 unsigned int suggestion_width);

// =========================================================================
// Additional text queries and edits
// =========================================================================

char* te_get_section_text(te_editor* editor, uint64_t start_line, uint64_t start_index, uint64_t end_line, uint64_t end_index); // caller frees
void te_replace_section_text(te_editor* editor, uint64_t start_line, uint64_t start_index, uint64_t end_line, uint64_t end_index, const char* text);
void te_selection_to_lower_case(te_editor* editor);
void te_selection_to_upper_case(te_editor* editor);
void te_strip_trailing_whitespaces(te_editor* editor);
void te_tabs_to_spaces(te_editor* editor);
void te_spaces_to_tabs(te_editor* editor);
void te_indent_lines(te_editor* editor);
void te_deindent_lines(te_editor* editor);
void te_move_up_lines(te_editor* editor);
void te_move_down_lines(te_editor* editor);
void te_toggle_comments(te_editor* editor);

// =========================================================================
// Additional selection / cursor API
// =========================================================================

void te_select_lines(te_editor* editor, uint64_t start, uint64_t end);
void te_select_region(te_editor* editor, uint64_t start_line, uint64_t start_index, uint64_t end_line, uint64_t end_index);
void te_select_to_brackets(te_editor* editor, bool include_brackets);
void te_grow_selections(te_editor* editor);
void te_shrink_selections(te_editor* editor);
void te_add_next_occurrence(te_editor* editor, bool whole_word);
void te_select_all_occurrences(te_editor* editor, bool whole_word);
void te_clear_cursors(te_editor* editor);

// Get a specific cursor's position / selection (index < GetNumberOfCursors)
void te_get_cursor_position(te_editor* editor, uint64_t cursor, uint64_t* out_line, uint64_t* out_index);
void te_get_cursor_selection(te_editor* editor, uint64_t cursor, uint64_t* out_start_line, uint64_t* out_start_index, uint64_t* out_end_line, uint64_t* out_end_index);

// =========================================================================
// Word / find query
// =========================================================================

char* te_get_word_at_mouse_pos(te_editor* editor, float x, float y); // caller frees
void te_find_word_start(te_editor* editor, uint64_t line, uint64_t index, bool whole_word, uint64_t* out_line, uint64_t* out_index);
void te_find_word_end(te_editor* editor, uint64_t line, uint64_t index, bool whole_word, uint64_t* out_line, uint64_t* out_index);
bool te_has_find_string(te_editor* editor);
void te_find_next(te_editor* editor);
void te_find_all(te_editor* editor);
void te_open_find_replace_window(te_editor* editor);
void te_close_find_replace_window(te_editor* editor);
void te_set_find_button_label(te_editor* editor, const char* label);
void te_set_find_all_button_label(te_editor* editor, const char* label);
void te_set_replace_button_label(te_editor* editor, const char* label);
void te_set_replace_all_button_label(te_editor* editor, const char* label);

// =========================================================================
// Visibility / folding / coordinate transforms
// =========================================================================

bool te_is_mouse_pos_over_text_area(te_editor* editor, float x, float y);
bool te_is_doc_pos_visible(te_editor* editor, uint64_t line, uint64_t index);
bool te_is_line_foldable(te_editor* editor, uint64_t line);
bool te_is_line_folded(te_editor* editor, uint64_t line);
bool te_is_line_visible(te_editor* editor, uint64_t line);
bool te_is_line_hidden(te_editor* editor, uint64_t line);
void te_fold_around_line(te_editor* editor, uint64_t line);
void te_unfold_around_line(te_editor* editor, uint64_t line);
void te_toggle_at_line(te_editor* editor, uint64_t line);
void te_unfold_all(te_editor* editor);
uint64_t te_get_first_visible_row(te_editor* editor);
uint64_t te_get_first_visible_column(te_editor* editor);
uint64_t te_get_last_visible_row(te_editor* editor);
uint64_t te_get_last_visible_column(te_editor* editor);

// DocPos <-> VisPos (row/column) transforms
void te_doc_pos_to_vis_pos(te_editor* editor, uint64_t line, uint64_t index, uint64_t* out_row, uint64_t* out_column);
void te_vis_pos_to_doc_pos(te_editor* editor, uint64_t row, uint64_t column, uint64_t* out_line, uint64_t* out_index);

// =========================================================================
// Undo state / static configuration
// =========================================================================

uint64_t te_get_undo_index(te_editor* editor);

// Static palettes (affect all editors)
void te_set_default_palette(uint32_t text, uint32_t keyword, uint32_t number, uint32_t string,
                            uint32_t comment, uint32_t background, uint32_t cursor, uint32_t selection);
void te_get_default_palette(uint32_t* out_text, uint32_t* out_keyword, uint32_t* out_number, uint32_t* out_string,
                            uint32_t* out_comment, uint32_t* out_background, uint32_t* out_cursor, uint32_t* out_selection);

// Attaches the editor to the current ImGui context (ImGui::SetCurrentContext)
void te_set_im_gui_context(uint64_t im_gui_context);

// =========================================================================
// Remaining configuration toggles
// =========================================================================

void te_set_show_spaces_enabled(te_editor* editor, bool value);
bool te_is_show_spaces_enabled(te_editor* editor);
void te_set_show_tabs_enabled(te_editor* editor, bool value);
bool te_is_show_tabs_enabled(te_editor* editor);
void te_set_show_scrollbar_minimap_enabled(te_editor* editor, bool value);
bool te_is_show_scrollbar_minimap_enabled(te_editor* editor);
void te_set_show_pan_scroll_indicator_enabled(te_editor* editor, bool value);
bool te_is_show_pan_scroll_indicator_enabled(te_editor* editor);
void te_set_minimap_columns(te_editor* editor, uint64_t value);
uint64_t te_get_minimap_columns(te_editor* editor);
void te_set_line_number_left_margin(te_editor* editor, uint64_t value);
uint64_t te_get_line_number_left_margin(te_editor* editor);
void te_set_decoration_left_margin(te_editor* editor, uint64_t value);
uint64_t te_get_decoration_left_margin(te_editor* editor);
void te_set_line_break_config(te_editor* editor, const char* break_after, const char* break_before, bool use_unicode_annex14);

// =========================================================================
// Line data hooks (insertor/deletor/user data)
// =========================================================================

typedef void* (*te_insertor_fn)(uint64_t line, void* user_data);
typedef void (*te_deletor_fn)(uint64_t line, void* data, void* user_data);
typedef void (*te_iterate_user_data_fn)(uint64_t line, void* data, void* user_data);

void te_set_insertor(te_editor* editor, te_insertor_fn fn, void* user_data);
void te_set_deletor(te_editor* editor, te_deletor_fn fn, void* user_data);
void te_set_user_data(te_editor* editor, uint64_t line, void* data);
void* te_get_user_data(te_editor* editor, uint64_t line);
void te_iterate_user_data(te_editor* editor, te_iterate_user_data_fn fn, void* user_data);

// =========================================================================
// Custom tokenizer (LSP semantic tokens)
// =========================================================================

// Installed as the language's customTokenizer. [text] is the token span
// (UTF-8, [length] codepoints... see note: length is byte length of text).
// Return a palette index (0..21) to color the span, or -1 to color it as
// plain text (the span is still consumed by the custom tokenizer).
typedef int64_t (*te_tokenizer_fn)(void* user_data, int64_t line, const char* text, size_t length);

// Activates a per-editor custom tokenizer: copies the editor's CURRENT
// language definition, installs [fn] as its customTokenizer and re-applies
// it (which also forces a re-colorize). Pass fn == nullptr to deactivate.
void te_set_custom_tokenizer(te_editor* editor, te_tokenizer_fn fn, void* user_data);

// =========================================================================
// Memory
// =========================================================================

char* te_string_dup(const char* s); // convenience; same allocator as te_get_text
void te_string_free(char* s);

#ifdef __cplusplus
}
#endif

#endif // TEXT_EDITOR_C_H