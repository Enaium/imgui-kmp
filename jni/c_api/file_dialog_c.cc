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

#include "ImGuiFileDialog.h"

#include <cstring>

#include "file_dialog_c.h"

static ImVec2 to_vec2(imgui_vec2 v) {
    return ImVec2(v.x, v.y);
}

extern "C" {

igfd_dialog* igfd_create(void) {
    return reinterpret_cast<igfd_dialog*>(IGFD_Create());
}

void igfd_destroy(igfd_dialog* dialog) {
    IGFD_Destroy(reinterpret_cast<ImGuiFileDialog*>(dialog));
}

void igfd_open_dialog(
    igfd_dialog* dialog,
    const char* key,
    const char* title,
    const char* filters,
    const char* path,
    const char* file_name,
    const char* file_path_name,
    int32_t count_selection_max,
    int flags) {
    IGFD_FileDialog_Config config = IGFD_FileDialog_Config_Get();
    config.path = path;
    config.fileName = file_name;
    config.filePathName = file_path_name;
    config.countSelectionMax = count_selection_max;
    config.flags = flags;
    IGFD_OpenDialog(reinterpret_cast<ImGuiFileDialog*>(dialog), key, title, filters, config);
}

bool igfd_display_dialog(
    igfd_dialog* dialog,
    const char* key,
    int window_flags,
    imgui_vec2 min_size,
    imgui_vec2 max_size) {
    return IGFD_DisplayDialog(
               reinterpret_cast<ImGuiFileDialog*>(dialog),
               key,
               window_flags,
               to_vec2(min_size),
               to_vec2(max_size))
        ? true
        : false;
}
void igfd_close_dialog(igfd_dialog* dialog, const char* key) {
    // key is kept in the C ABI for symmetry with open/display; the underlying
    // IGFD C API closes whatever dialog is bound to this context.
    (void)key;
    IGFD_CloseDialog(reinterpret_cast<ImGuiFileDialog*>(dialog));
}

bool igfd_is_ok(igfd_dialog* dialog) {
    return IGFD_IsOk(reinterpret_cast<ImGuiFileDialog*>(dialog)) ? true : false;
}

bool igfd_was_key_opened_this_frame(igfd_dialog* dialog, const char* key) {
    return IGFD_WasKeyOpenedThisFrame(reinterpret_cast<ImGuiFileDialog*>(dialog), key) ? true : false;
}

bool igfd_was_opened_this_frame(igfd_dialog* dialog) {
    return IGFD_WasOpenedThisFrame(reinterpret_cast<ImGuiFileDialog*>(dialog)) ? true : false;
}

bool igfd_is_key_opened(igfd_dialog* dialog, const char* key) {
    return IGFD_IsKeyOpened(reinterpret_cast<ImGuiFileDialog*>(dialog), key) ? true : false;
}

bool igfd_is_opened(igfd_dialog* dialog) {
    return IGFD_IsOpened(reinterpret_cast<ImGuiFileDialog*>(dialog)) ? true : false;
}

char* igfd_get_file_path_name(igfd_dialog* dialog, int result_mode) {
    return IGFD_GetFilePathName(reinterpret_cast<ImGuiFileDialog*>(dialog), result_mode);
}

char* igfd_get_current_file_name(igfd_dialog* dialog, int result_mode) {
    return IGFD_GetCurrentFileName(reinterpret_cast<ImGuiFileDialog*>(dialog), result_mode);
}

char* igfd_get_current_path(igfd_dialog* dialog) {
    return IGFD_GetCurrentPath(reinterpret_cast<ImGuiFileDialog*>(dialog));
}

char* igfd_get_current_filter(igfd_dialog* dialog) {
    return IGFD_GetCurrentFilter(reinterpret_cast<ImGuiFileDialog*>(dialog));
}

char** igfd_get_selection(igfd_dialog* dialog, int result_mode, size_t* out_count) {
    IGFD_Selection selection = IGFD_GetSelection(reinterpret_cast<ImGuiFileDialog*>(dialog), result_mode);
    if (selection.count == 0 || selection.table == nullptr) {
        IGFD_Selection_DestroyContent(&selection);
        *out_count = 0;
        return nullptr;
    }
    // Flatten into one malloc'd buffer of 2*count string pointers (name, pathName, ...).
    const size_t count = selection.count;
    char** flat = static_cast<char**>(malloc(sizeof(char*) * count * 2));
    if (flat == nullptr) {
        IGFD_Selection_DestroyContent(&selection);
        *out_count = 0;
        return nullptr;
    }
    for (size_t i = 0; i < count; i++) {
        flat[i * 2 + 0] = selection.table[i].fileName ? strdup(selection.table[i].fileName) : nullptr;
        flat[i * 2 + 1] = selection.table[i].filePathName ? strdup(selection.table[i].filePathName) : nullptr;
    }
    *out_count = count;
    IGFD_Selection_DestroyContent(&selection);
    return flat;
}

void igfd_selection_free(char** selection, size_t count) {
    if (selection == nullptr) {
        return;
    }
    for (size_t i = 0; i < count * 2; i++) {
        free(selection[i]);
    }
    free(selection);
}

void igfd_string_free(char* str) {
    free(str);
}

void igfd_set_file_style(
    igfd_dialog* dialog,
    unsigned int flags,
    const char* filter,
    float r,
    float g,
    float b,
    float a,
    const char* icon_text) {
    IGFD_SetFileStyle2(reinterpret_cast<ImGuiFileDialog*>(dialog), flags, filter, r, g, b, a, icon_text, nullptr);
}
bool igfd_get_file_style(
    igfd_dialog* dialog,
    unsigned int flags,
    const char* filter,
    float* out_color,
    char** out_icon_text) {
    ImVec4 color;
    char* icon = nullptr;
    ImFont* font = nullptr;
    if (!IGFD_GetFileStyle(reinterpret_cast<ImGuiFileDialog*>(dialog), flags, filter, &color, &icon, &font)) {
        return false;
    }
    out_color[0] = color.x;
    out_color[1] = color.y;
    out_color[2] = color.z;
    out_color[3] = color.w;
    *out_icon_text = icon; // caller frees with igfd_string_free
    return true;
}

void igfd_clear_files_style(igfd_dialog* dialog) {
    IGFD_ClearFilesStyle(reinterpret_cast<ImGuiFileDialog*>(dialog));
}

} // extern "C"
