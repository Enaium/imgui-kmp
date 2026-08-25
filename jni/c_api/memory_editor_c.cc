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

#include "imgui_memory_editor.h"
#include "memory_editor_c.h"

extern "C" {

me_editor* me_create(void) {
    return reinterpret_cast<me_editor*>(new MemoryEditor());
}

void me_destroy(me_editor* editor) {
    delete reinterpret_cast<MemoryEditor*>(editor);
}

void me_draw_window(me_editor* editor, const char* title, void* data, uint32_t size, uint64_t base_display_addr) {
    reinterpret_cast<MemoryEditor*>(editor)->DrawWindow(title, data, size, static_cast<size_t>(base_display_addr));
}

void me_draw_contents(me_editor* editor, void* data, uint32_t size, uint64_t base_display_addr) {
    reinterpret_cast<MemoryEditor*>(editor)->DrawContents(data, size, static_cast<size_t>(base_display_addr));
}

bool me_is_open(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->Open;
}

void me_set_open(me_editor* editor, bool value) {
    reinterpret_cast<MemoryEditor*>(editor)->Open = value;
}

bool me_is_read_only(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->ReadOnly;
}

void me_set_read_only(me_editor* editor, bool value) {
    reinterpret_cast<MemoryEditor*>(editor)->ReadOnly = value;
}

int me_get_cols(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->Cols;
}

void me_set_cols(me_editor* editor, int value) {
    reinterpret_cast<MemoryEditor*>(editor)->Cols = value;
}

bool me_is_opt_show_options(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->OptShowOptions;
}

void me_set_opt_show_options(me_editor* editor, bool value) {
    reinterpret_cast<MemoryEditor*>(editor)->OptShowOptions = value;
}

bool me_is_opt_show_data_preview(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->OptShowDataPreview;
}

void me_set_opt_show_data_preview(me_editor* editor, bool value) {
    reinterpret_cast<MemoryEditor*>(editor)->OptShowDataPreview = value;
}

bool me_is_opt_show_hex_ii(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->OptShowHexII;
}

void me_set_opt_show_hex_ii(me_editor* editor, bool value) {
    reinterpret_cast<MemoryEditor*>(editor)->OptShowHexII = value;
}

bool me_is_opt_show_ascii(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->OptShowAscii;
}

void me_set_opt_show_ascii(me_editor* editor, bool value) {
    reinterpret_cast<MemoryEditor*>(editor)->OptShowAscii = value;
}

bool me_is_opt_grey_out_zeroes(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->OptGreyOutZeroes;
}

void me_set_opt_grey_out_zeroes(me_editor* editor, bool value) {
    reinterpret_cast<MemoryEditor*>(editor)->OptGreyOutZeroes = value;
}

bool me_is_opt_upper_case_hex(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->OptUpperCaseHex;
}

void me_set_opt_upper_case_hex(me_editor* editor, bool value) {
    reinterpret_cast<MemoryEditor*>(editor)->OptUpperCaseHex = value;
}

int me_get_opt_mid_cols_count(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->OptMidColsCount;
}

void me_set_opt_mid_cols_count(me_editor* editor, int value) {
    reinterpret_cast<MemoryEditor*>(editor)->OptMidColsCount = value;
}

int me_get_opt_addr_digits_count(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->OptAddrDigitsCount;
}

void me_set_opt_addr_digits_count(me_editor* editor, int value) {
    reinterpret_cast<MemoryEditor*>(editor)->OptAddrDigitsCount = value;
}

float me_get_opt_footer_extra_height(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->OptFooterExtraHeight;
}

void me_set_opt_footer_extra_height(me_editor* editor, float value) {
    reinterpret_cast<MemoryEditor*>(editor)->OptFooterExtraHeight = value;
}

uint32_t me_get_highlight_color(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->HighlightColor;
}

void me_set_highlight_color(me_editor* editor, uint32_t value) {
    reinterpret_cast<MemoryEditor*>(editor)->HighlightColor = value;
}

bool me_is_mouse_hovered(me_editor* editor) {
    return reinterpret_cast<MemoryEditor*>(editor)->MouseHovered;
}

uint64_t me_mouse_hovered_addr(me_editor* editor) {
    return static_cast<uint64_t>(reinterpret_cast<MemoryEditor*>(editor)->MouseHoveredAddr);
}

} // extern "C"
