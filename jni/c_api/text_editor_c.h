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
// Memory
// =========================================================================

char* te_string_dup(const char* s); // convenience; same allocator as te_get_text
void te_string_free(char* s);

#ifdef __cplusplus
}
#endif

#endif // TEXT_EDITOR_C_H