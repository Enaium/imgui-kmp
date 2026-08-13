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

#include <jni.h>

#include <cstring>
#include <string>
#include <unordered_map>
#include <vector>

#include "imgui.h"
#include "imgui_internal.h"
#include "imgui_c.h"
#include "implot_c.h"

// =========================================================================
// Helpers
// =========================================================================

static std::string jstring_to_string(JNIEnv* env, jstring str) {
    if (str == nullptr) {
        return std::string();
    }
    const char* chars = env->GetStringUTFChars(str, nullptr);
    if (chars == nullptr) {
        return std::string();
    }
    std::string out(chars);
    env->ReleaseStringUTFChars(str, chars);
    return out;
}

static jstring string_to_jstring(JNIEnv* env, const std::string& str) {
    return env->NewStringUTF(str.c_str());
}

// imgui keeps the InputText() buffer pointer alive while the item is being
// edited, so buffers must survive between frames. They are keyed by label and
// released as soon as the widget reports it is no longer active.
static std::unordered_map<std::string, std::vector<char>> g_input_buffers;

static std::vector<char>& get_input_buffer(const char* label) {
    return g_input_buffers[label];
}

static void release_input_buffer_if_idle(const char* label) {
    auto it = g_input_buffers.find(label);
    if (it == g_input_buffers.end()) {
        return;
    }
    if (!imgui_is_item_active()) {
        g_input_buffers.erase(it);
    }
}

// =========================================================================
// Context / main frame
// =========================================================================

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_createContext(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(imgui_create_context());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_destroyContext(JNIEnv*, jclass, jlong ptr) {
    imgui_destroy_context(reinterpret_cast<imgui_context*>(ptr));
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_getCurrentContext(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(imgui_get_current_context());
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_getIO(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(imgui_get_io());
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_getStyle(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(imgui_get_style());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_newFrame(JNIEnv*, jclass) {
    imgui_new_frame();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_render(JNIEnv*, jclass) {
    imgui_render();
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_getDrawData(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(imgui_get_draw_data());
}

extern "C" JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_Jni_getVersion(JNIEnv* env, jclass) {
    return string_to_jstring(env, imgui_get_version());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_showDemoWindow(JNIEnv* env, jclass, jbooleanArray p_open) {
    jboolean* elems = p_open != nullptr ? env->GetBooleanArrayElements(p_open, nullptr) : nullptr;
    imgui_show_demo_window(reinterpret_cast<bool*>(elems));
    if (elems != nullptr) {
        env->ReleaseBooleanArrayElements(p_open, elems, 0);
    }
}

// =========================================================================
// Windows
// =========================================================================

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_begin(JNIEnv* env, jclass, jstring name, jbooleanArray p_open, jint flags) {
    std::string name_str = jstring_to_string(env, name);
    jboolean* elems = p_open != nullptr ? env->GetBooleanArrayElements(p_open, nullptr) : nullptr;
    bool result = imgui_begin(name_str.c_str(), reinterpret_cast<bool*>(elems), flags);
    if (elems != nullptr) {
        env->ReleaseBooleanArrayElements(p_open, elems, 0);
    }
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_end(JNIEnv*, jclass) {
    imgui_end();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginChild(JNIEnv* env, jclass, jstring str_id, jfloat size_x, jfloat size_y, jint child_flags, jint window_flags) {
    std::string id = jstring_to_string(env, str_id);
    imgui_vec2 size;
    size.x = size_x;
    size.y = size_y;
    return imgui_begin_child(id.c_str(), size, child_flags, window_flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endChild(JNIEnv*, jclass) {
    imgui_end_child();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextWindowPos(JNIEnv*, jclass, jfloat x, jfloat y, jint cond, jfloat pivot_x, jfloat pivot_y) {
    imgui_vec2 pos;
    pos.x = x;
    pos.y = y;
    imgui_vec2 pivot;
    pivot.x = pivot_x;
    pivot.y = pivot_y;
    imgui_set_next_window_pos(pos, cond, pivot);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextWindowSize(JNIEnv*, jclass, jfloat w, jfloat h, jint cond) {
    imgui_vec2 size;
    size.x = w;
    size.y = h;
    imgui_set_next_window_size(size, cond);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextWindowBgAlpha(JNIEnv*, jclass, jfloat alpha) {
    imgui_set_next_window_bg_alpha(alpha);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_beginDisabled(JNIEnv*, jclass, jboolean disabled) {
    imgui_begin_disabled(disabled == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endDisabled(JNIEnv*, jclass) {
    imgui_end_disabled();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginMainMenuBar(JNIEnv*, jclass) {
    return imgui_begin_main_menu_bar() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endMainMenuBar(JNIEnv*, jclass) {
    imgui_end_main_menu_bar();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginMenuBar(JNIEnv*, jclass) {
    return imgui_begin_menu_bar() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endMenuBar(JNIEnv*, jclass) {
    imgui_end_menu_bar();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginMenu(JNIEnv* env, jclass, jstring label, jboolean enabled) {
    std::string label_str = jstring_to_string(env, label);
    return imgui_begin_menu(label_str.c_str(), enabled == JNI_TRUE) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endMenu(JNIEnv*, jclass) {
    imgui_end_menu();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_menuItem(JNIEnv* env, jclass, jstring label, jstring shortcut, jboolean selected, jboolean enabled) {
    std::string label_str = jstring_to_string(env, label);
    std::string shortcut_str = jstring_to_string(env, shortcut);
    return imgui_menu_item(label_str.c_str(), shortcut_str.empty() ? nullptr : shortcut_str.c_str(), selected == JNI_TRUE, enabled == JNI_TRUE) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginTabBar(JNIEnv* env, jclass, jstring str_id, jint flags) {
    std::string id = jstring_to_string(env, str_id);
    return imgui_begin_tab_bar(id.c_str(), flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endTabBar(JNIEnv*, jclass) {
    imgui_end_tab_bar();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginTabItem(JNIEnv* env, jclass, jstring label, jbooleanArray p_open, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    jboolean* elems = p_open != nullptr ? env->GetBooleanArrayElements(p_open, nullptr) : nullptr;
    bool result = imgui_begin_tab_item(label_str.c_str(), reinterpret_cast<bool*>(elems), flags);
    if (elems != nullptr) {
        env->ReleaseBooleanArrayElements(p_open, elems, 0);
    }
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endTabItem(JNIEnv*, jclass) {
    imgui_end_tab_item();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginTooltip(JNIEnv*, jclass) {
    return imgui_begin_tooltip() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endTooltip(JNIEnv*, jclass) {
    imgui_end_tooltip();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setTooltip(JNIEnv* env, jclass, jstring text) {
    std::string text_str = jstring_to_string(env, text);
    imgui_set_tooltip(text_str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_openPopup(JNIEnv* env, jclass, jstring str_id, jint popup_flags) {
    std::string id = jstring_to_string(env, str_id);
    imgui_open_popup(id.c_str(), popup_flags);
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginPopup(JNIEnv* env, jclass, jstring str_id, jint flags) {
    std::string id = jstring_to_string(env, str_id);
    return imgui_begin_popup(id.c_str(), flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginPopupModal(JNIEnv* env, jclass, jstring name, jbooleanArray p_open, jint flags) {
    std::string name_str = jstring_to_string(env, name);
    jboolean* elems = p_open != nullptr ? env->GetBooleanArrayElements(p_open, nullptr) : nullptr;
    bool result = imgui_begin_popup_modal(name_str.c_str(), reinterpret_cast<bool*>(elems), flags);
    if (elems != nullptr) {
        env->ReleaseBooleanArrayElements(p_open, elems, 0);
    }
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endPopup(JNIEnv*, jclass) {
    imgui_end_popup();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_closeCurrentPopup(JNIEnv*, jclass) {
    imgui_close_current_popup();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginCombo(JNIEnv* env, jclass, jstring label, jstring preview_value, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string preview_str = jstring_to_string(env, preview_value);
    return imgui_begin_combo(label_str.c_str(), preview_str.c_str(), flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endCombo(JNIEnv*, jclass) {
    imgui_end_combo();
}

// =========================================================================
// Widgets
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_text(JNIEnv* env, jclass, jstring text) {
    std::string str = jstring_to_string(env, text);
    imgui_text(str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_textColored(JNIEnv* env, jclass, jfloat r, jfloat g, jfloat b, jfloat a, jstring text) {
    std::string str = jstring_to_string(env, text);
    imgui_vec4 color;
    color.x = r;
    color.y = g;
    color.z = b;
    color.w = a;
    imgui_text_colored(color, str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_textDisabled(JNIEnv* env, jclass, jstring text) {
    std::string str = jstring_to_string(env, text);
    imgui_text_disabled(str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_labelText(JNIEnv* env, jclass, jstring label, jstring text) {
    std::string label_str = jstring_to_string(env, label);
    std::string text_str = jstring_to_string(env, text);
    imgui_label_text(label_str.c_str(), text_str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_bulletText(JNIEnv* env, jclass, jstring text) {
    std::string str = jstring_to_string(env, text);
    imgui_bullet_text(str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_bullet(JNIEnv*, jclass) {
    imgui_bullet();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_separator(JNIEnv*, jclass) {
    imgui_separator();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_separatorText(JNIEnv* env, jclass, jstring text) {
    std::string str = jstring_to_string(env, text);
    imgui_separator_text(str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_sameLine(JNIEnv*, jclass, jfloat offset_from_start_x, jfloat spacing) {
    imgui_same_line(offset_from_start_x, spacing);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_newLine(JNIEnv*, jclass) {
    imgui_new_line();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_spacing(JNIEnv*, jclass) {
    imgui_spacing();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_dummy(JNIEnv*, jclass, jfloat w, jfloat h) {
    imgui_vec2 size;
    size.x = w;
    size.y = h;
    imgui_dummy(size);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_indent(JNIEnv*, jclass, jfloat indent_w) {
    imgui_indent(indent_w);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_unindent(JNIEnv*, jclass, jfloat indent_w) {
    imgui_unindent(indent_w);
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_button(JNIEnv* env, jclass, jstring label, jfloat w, jfloat h) {
    std::string label_str = jstring_to_string(env, label);
    imgui_vec2 size;
    size.x = w;
    size.y = h;
    return imgui_button(label_str.c_str(), size) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_smallButton(JNIEnv* env, jclass, jstring label) {
    std::string label_str = jstring_to_string(env, label);
    return imgui_small_button(label_str.c_str()) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_checkbox(JNIEnv* env, jclass, jstring label, jbooleanArray v) {
    std::string label_str = jstring_to_string(env, label);
    jboolean* elems = env->GetBooleanArrayElements(v, nullptr);
    bool result = imgui_checkbox(label_str.c_str(), reinterpret_cast<bool*>(elems));
    env->ReleaseBooleanArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_sliderFloat(JNIEnv* env, jclass, jstring label, jfloatArray v, jfloat v_min, jfloat v_max, jstring format) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jfloat* elems = env->GetFloatArrayElements(v, nullptr);
    bool result = imgui_slider_float(label_str.c_str(), elems, v_min, v_max, format_str.c_str());
    env->ReleaseFloatArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_sliderInt(JNIEnv* env, jclass, jstring label, jintArray v, jint v_min, jint v_max, jstring format) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jint* elems = env->GetIntArrayElements(v, nullptr);
    bool result = imgui_slider_int(label_str.c_str(), elems, v_min, v_max, format_str.c_str());
    env->ReleaseIntArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_Jni_inputText(JNIEnv* env, jclass, jstring label, jstring buf, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string initial = jstring_to_string(env, buf);

    std::vector<char>& buffer = get_input_buffer(label_str.c_str());
    size_t needed = initial.size() + 512;
    if (buffer.size() < needed) {
        buffer.resize(needed);
    }
    std::memcpy(buffer.data(), initial.c_str(), initial.size() + 1);

    imgui_input_text(label_str.c_str(), buffer.data(), (int)buffer.size(), flags);

    jstring out = string_to_jstring(env, buffer.data());
    release_input_buffer_if_idle(label_str.c_str());
    return out;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_combo(JNIEnv* env, jclass, jstring label, jintArray current_item, jobjectArray items) {
    std::string label_str = jstring_to_string(env, label);
    jsize count = env->GetArrayLength(items);

    std::vector<std::string> item_strs;
    item_strs.reserve(count);
    std::vector<const char*> item_ptrs;
    item_ptrs.reserve(count);
    for (jsize i = 0; i < count; i++) {
        jstring item = (jstring)env->GetObjectArrayElement(items, i);
        item_strs.push_back(jstring_to_string(env, item));
        env->DeleteLocalRef(item);
        item_ptrs.push_back(item_strs.back().c_str());
    }

    jint* elems = env->GetIntArrayElements(current_item, nullptr);
    int current = elems[0];
    bool result = imgui_combo(label_str.c_str(), &current, item_ptrs.data(), (int)count);
    elems[0] = current;
    env->ReleaseIntArrayElements(current_item, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_selectable(JNIEnv* env, jclass, jstring label, jboolean selected, jint flags, jfloat w, jfloat h) {
    std::string label_str = jstring_to_string(env, label);
    imgui_vec2 size;
    size.x = w;
    size.y = h;
    return imgui_selectable(label_str.c_str(), selected == JNI_TRUE, flags, size) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_radioButton(JNIEnv* env, jclass, jstring label, jboolean active) {
    std::string label_str = jstring_to_string(env, label);
    return imgui_radio_button(label_str.c_str(), active == JNI_TRUE) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_progressBar(JNIEnv* env, jclass, jfloat fraction, jfloat w, jfloat h, jstring overlay) {
    imgui_vec2 size;
    size.x = w;
    size.y = h;
    std::string overlay_str = jstring_to_string(env, overlay);
    imgui_progress_bar(fraction, size, overlay_str.empty() ? nullptr : overlay_str.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_collapsingHeader(JNIEnv* env, jclass, jstring label, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    return imgui_collapsing_header(label_str.c_str(), flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_treeNode(JNIEnv* env, jclass, jstring label) {
    std::string label_str = jstring_to_string(env, label);
    return imgui_tree_node(label_str.c_str()) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_treePop(JNIEnv*, jclass) {
    imgui_tree_pop();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_invisibleButton(JNIEnv* env, jclass, jstring str_id, jfloat w, jfloat h, jint flags) {
    std::string id = jstring_to_string(env, str_id);
    imgui_vec2 size;
    size.x = w;
    size.y = h;
    return imgui_invisible_button(id.c_str(), size, flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_beginGroup(JNIEnv*, jclass) {
    imgui_begin_group();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endGroup(JNIEnv*, jclass) {
    imgui_end_group();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setCursorPos(JNIEnv*, jclass, jfloat x, jfloat y) {
    imgui_vec2 pos;
    pos.x = x;
    pos.y = y;
    imgui_set_cursor_pos(pos);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_pushId(JNIEnv* env, jclass, jstring str_id) {
    std::string id = jstring_to_string(env, str_id);
    imgui_push_id(id.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_popId(JNIEnv*, jclass) {
    imgui_pop_id();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isItemHovered(JNIEnv*, jclass, jint flags) {
    return imgui_is_item_hovered(flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isItemActive(JNIEnv*, jclass) {
    return imgui_is_item_active() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isItemClicked(JNIEnv*, jclass, jint mouse_button) {
    return imgui_is_item_clicked(mouse_button) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isWindowHovered(JNIEnv*, jclass, jint flags) {
    return imgui_is_window_hovered(flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isWindowFocused(JNIEnv*, jclass, jint flags) {
    return imgui_is_window_focused(flags) ? JNI_TRUE : JNI_FALSE;
}

// =========================================================================
// Tables
// =========================================================================

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginTable(JNIEnv* env, jclass, jstring str_id, jint column, jint flags, jfloat outer_w, jfloat outer_h, jfloat inner_width) {
    std::string id = jstring_to_string(env, str_id);
    imgui_vec2 outer_size;
    outer_size.x = outer_w;
    outer_size.y = outer_h;
    return imgui_begin_table(id.c_str(), column, flags, outer_size, inner_width) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endTable(JNIEnv*, jclass) {
    imgui_end_table();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_tableNextRow(JNIEnv*, jclass, jint min_row_height, jint flags) {
    imgui_table_next_row(min_row_height, flags);
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_tableNextColumn(JNIEnv*, jclass) {
    return imgui_table_next_column() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_tableSetColumnIndex(JNIEnv*, jclass, jint column_n) {
    return imgui_table_set_column_index(column_n) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_tableSetupColumn(JNIEnv* env, jclass, jstring label, jint flags, jfloat init_width_or_weight, jint user_id) {
    std::string label_str = jstring_to_string(env, label);
    imgui_table_setup_column(label_str.c_str(), flags, init_width_or_weight, user_id);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_tableSetupScrollFreeze(JNIEnv*, jclass, jint cols, jint rows) {
    imgui_table_setup_scroll_freeze(cols, rows);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_tableHeadersRow(JNIEnv*, jclass) {
    imgui_table_headers_row();
}

// =========================================================================
// Style
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_pushStyleColorVec4(JNIEnv*, jclass, jint idx, jfloat r, jfloat g, jfloat b, jfloat a) {
    imgui_vec4 color;
    color.x = r;
    color.y = g;
    color.z = b;
    color.w = a;
    imgui_push_style_color_vec4(idx, color);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_pushStyleColorU32(JNIEnv*, jclass, jint idx, jint color) {
    imgui_push_style_color_u32(idx, (uint32_t)color);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_popStyleColor(JNIEnv*, jclass, jint count) {
    imgui_pop_style_color(count);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_pushStyleVarFloat(JNIEnv*, jclass, jint idx, jfloat val) {
    imgui_push_style_var_float(idx, val);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_pushStyleVarVec2(JNIEnv*, jclass, jint idx, jfloat x, jfloat y) {
    imgui_vec2 val;
    val.x = x;
    val.y = y;
    imgui_push_style_var_vec2(idx, val);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_popStyleVar(JNIEnv*, jclass, jint count) {
    imgui_pop_style_var(count);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_pushFont(JNIEnv*, jclass, jlong font) {
    imgui_push_font(reinterpret_cast<imgui_font*>(font));
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_popFont(JNIEnv*, jclass) {
    imgui_pop_font();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_pushItemWidth(JNIEnv*, jclass, jfloat item_width) {
    imgui_push_item_width(item_width);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_popItemWidth(JNIEnv*, jclass) {
    imgui_pop_item_width();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextItemWidth(JNIEnv*, jclass, jfloat item_width) {
    imgui_set_next_item_width(item_width);
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_styleGetColor(JNIEnv* env, jclass, jlong style, jint idx) {
    imgui_vec4 color = imgui_style_get_color(reinterpret_cast<imgui_style*>(style), idx);
    jfloatArray out = env->NewFloatArray(4);
    jfloat values[4] = {color.x, color.y, color.z, color.w};
    env->SetFloatArrayRegion(out, 0, 4, values);
    return out;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_styleSetColor(JNIEnv*, jclass, jlong style, jint idx, jfloat r, jfloat g, jfloat b, jfloat a) {
    imgui_vec4 color;
    color.x = r;
    color.y = g;
    color.z = b;
    color.w = a;
    imgui_style_set_color(reinterpret_cast<imgui_style*>(style), idx, color);
}

// =========================================================================
// IO
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_ioSetDisplaySize(JNIEnv*, jclass, jlong io, jfloat w, jfloat h) {
    imgui_io_set_display_size(reinterpret_cast<imgui_io*>(io), w, h);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_ioSetDisplayFramebufferScale(JNIEnv*, jclass, jlong io, jfloat sx, jfloat sy) {
    imgui_io_set_display_framebuffer_scale(reinterpret_cast<imgui_io*>(io), sx, sy);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_ioSetDeltaTime(JNIEnv*, jclass, jlong io, jfloat dt) {
    imgui_io_set_delta_time(reinterpret_cast<imgui_io*>(io), dt);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_ioSetConfigFlags(JNIEnv*, jclass, jlong io, jint flags) {
    imgui_io_set_config_flags(reinterpret_cast<imgui_io*>(io), flags);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_ioSetBackendFlags(JNIEnv*, jclass, jlong io, jint flags) {
    imgui_io_set_backend_flags(reinterpret_cast<imgui_io*>(io), flags);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_ioSetIniFilename(JNIEnv* env, jclass, jlong io, jstring path) {
    std::string path_str = jstring_to_string(env, path);
    imgui_io_set_ini_filename(reinterpret_cast<imgui_io*>(io), path_str.empty() ? nullptr : path_str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_ioSetFontGlobalScale(JNIEnv*, jclass, jlong io, jfloat scale) {
    imgui_io_set_font_global_scale(reinterpret_cast<imgui_io*>(io), scale);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_ioAddMousePosEvent(JNIEnv*, jclass, jlong io, jfloat x, jfloat y) {
    imgui_io_add_mouse_pos_event(reinterpret_cast<imgui_io*>(io), x, y);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_ioAddMouseButtonEvent(JNIEnv*, jclass, jlong io, jint button, jboolean down) {
    imgui_io_add_mouse_button_event(reinterpret_cast<imgui_io*>(io), button, down == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_ioAddMouseWheelEvent(JNIEnv*, jclass, jlong io, jfloat x, jfloat y) {
    imgui_io_add_mouse_wheel_event(reinterpret_cast<imgui_io*>(io), x, y);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_ioAddKeyEvent(JNIEnv*, jclass, jlong io, jint key, jboolean down) {
    imgui_io_add_key_event(reinterpret_cast<imgui_io*>(io), key, down == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_ioAddInputCharacter(JNIEnv*, jclass, jlong io, jint c) {
    imgui_io_add_input_character(reinterpret_cast<imgui_io*>(io), (uint32_t)c);
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_ioWantCaptureMouse(JNIEnv*, jclass, jlong io) {
    return imgui_io_want_capture_mouse(reinterpret_cast<imgui_io*>(io)) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_ioWantCaptureKeyboard(JNIEnv*, jclass, jlong io) {
    return imgui_io_want_capture_keyboard(reinterpret_cast<imgui_io*>(io)) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_ioWantTextInput(JNIEnv*, jclass, jlong io) {
    return imgui_io_want_text_input(reinterpret_cast<imgui_io*>(io)) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_ioGetFonts(JNIEnv*, jclass, jlong io) {
    return reinterpret_cast<jlong>(imgui_io_get_fonts(reinterpret_cast<imgui_io*>(io)));
}

// =========================================================================
// Fonts
// =========================================================================

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_fontsAddFontFromFileTTF(JNIEnv* env, jclass, jlong atlas, jstring path, jfloat size_px) {
    std::string path_str = jstring_to_string(env, path);
    return reinterpret_cast<jlong>(imgui_font_atlas_add_font_from_file_ttf(reinterpret_cast<imgui_font_atlas*>(atlas), path_str.c_str(), size_px));
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_fontsAddFontDefault(JNIEnv*, jclass, jlong atlas) {
    return reinterpret_cast<jlong>(imgui_font_atlas_add_font_default(reinterpret_cast<imgui_font_atlas*>(atlas)));
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_fontsBuild(JNIEnv*, jclass, jlong atlas) {
    return imgui_font_atlas_build(reinterpret_cast<imgui_font_atlas*>(atlas)) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jbyteArray JNICALL Java_cn_enaium_imgui_Jni_fontsGetTexDataAsRGBA32(JNIEnv* env, jclass, jlong atlas, jintArray out_dims) {
    const unsigned char* pixels = nullptr;
    int width = 0, height = 0, bpp = 0;
    imgui_font_atlas_get_tex_data_as_rgba32(reinterpret_cast<imgui_font_atlas*>(atlas), &pixels, &width, &height, &bpp);

    jint dims[3] = {width, height, bpp};
    env->SetIntArrayRegion(out_dims, 0, 3, dims);

    jsize size = (jsize)width * height * bpp;
    jbyteArray out = env->NewByteArray(size);
    if (size > 0) {
        env->SetByteArrayRegion(out, 0, size, reinterpret_cast<const jbyte*>(pixels));
    }
    return out;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_fontsSetTexId(JNIEnv*, jclass, jlong atlas, jlong tex_id) {
    imgui_font_atlas_set_tex_id(reinterpret_cast<imgui_font_atlas*>(atlas), (uint64_t)tex_id);
}

// =========================================================================
// Draw data
// =========================================================================

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_drawDataGetDisplayPos(JNIEnv* env, jclass, jlong data) {
    imgui_vec2 v = imgui_draw_data_get_display_pos(reinterpret_cast<imgui_draw_data*>(data));
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_drawDataGetDisplaySize(JNIEnv* env, jclass, jlong data) {
    imgui_vec2 v = imgui_draw_data_get_display_size(reinterpret_cast<imgui_draw_data*>(data));
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_drawDataGetFramebufferScale(JNIEnv* env, jclass, jlong data) {
    imgui_vec2 v = imgui_draw_data_get_framebuffer_scale(reinterpret_cast<imgui_draw_data*>(data));
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_drawDataGetCmdListsCount(JNIEnv*, jclass, jlong data) {
    return imgui_draw_data_get_cmd_lists_count(reinterpret_cast<imgui_draw_data*>(data));
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_drawDataGetCmdList(JNIEnv*, jclass, jlong data, jint index) {
    return reinterpret_cast<jlong>(imgui_draw_data_get_cmd_list(reinterpret_cast<imgui_draw_data*>(data), index));
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_drawListGetVtxCount(JNIEnv*, jclass, jlong list) {
    return imgui_draw_list_get_vtx_count(reinterpret_cast<imgui_draw_list*>(list));
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_drawListGetIdxCount(JNIEnv*, jclass, jlong list) {
    return imgui_draw_list_get_idx_count(reinterpret_cast<imgui_draw_list*>(list));
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_drawListGetCmdCount(JNIEnv*, jclass, jlong list) {
    return imgui_draw_list_get_cmd_count(reinterpret_cast<imgui_draw_list*>(list));
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_drawListGetCmd(JNIEnv*, jclass, jlong list, jint index) {
    return reinterpret_cast<jlong>(imgui_draw_list_get_cmd(reinterpret_cast<imgui_draw_list*>(list), index));
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_drawListCopyVtx(JNIEnv* env, jclass, jlong list, jint vtx_offset, jint vtx_count, jfloatArray positions, jfloatArray uvs, jintArray colors) {
    const imgui_draw_vert* data = imgui_draw_list_get_vtx_data(reinterpret_cast<imgui_draw_list*>(list));
    if (data == nullptr) {
        return;
    }
    data += vtx_offset;

    jfloat* pos = env->GetFloatArrayElements(positions, nullptr);
    jfloat* uv = env->GetFloatArrayElements(uvs, nullptr);
    jint* col = env->GetIntArrayElements(colors, nullptr);
    for (int i = 0; i < vtx_count; i++) {
        pos[i * 2 + 0] = data[i].pos_x;
        pos[i * 2 + 1] = data[i].pos_y;
        uv[i * 2 + 0] = data[i].uv_x;
        uv[i * 2 + 1] = data[i].uv_y;
        col[i] = (jint)data[i].col;
    }
    env->ReleaseFloatArrayElements(positions, pos, 0);
    env->ReleaseFloatArrayElements(uvs, uv, 0);
    env->ReleaseIntArrayElements(colors, col, 0);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_drawListCopyIdx(JNIEnv* env, jclass, jlong list, jint idx_offset, jint idx_count, jintArray out) {
    const uint16_t* data = imgui_draw_list_get_idx_data(reinterpret_cast<imgui_draw_list*>(list));
    if (data == nullptr) {
        return;
    }
    data += idx_offset;

    jint* elems = env->GetIntArrayElements(out, nullptr);
    for (int i = 0; i < idx_count; i++) {
        elems[i] = data[i];
    }
    env->ReleaseIntArrayElements(out, elems, 0);
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_drawCmdGetClipRect(JNIEnv* env, jclass, jlong cmd) {
    imgui_vec4 r = imgui_draw_cmd_get_clip_rect(reinterpret_cast<imgui_draw_cmd*>(cmd));
    jfloatArray out = env->NewFloatArray(4);
    jfloat values[4] = {r.x, r.y, r.z, r.w};
    env->SetFloatArrayRegion(out, 0, 4, values);
    return out;
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_drawCmdGetTexId(JNIEnv*, jclass, jlong cmd) {
    return (jlong)imgui_draw_cmd_get_tex_id(reinterpret_cast<imgui_draw_cmd*>(cmd));
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_drawCmdGetVtxOffset(JNIEnv*, jclass, jlong cmd) {
    return (jint)imgui_draw_cmd_get_vtx_offset(reinterpret_cast<imgui_draw_cmd*>(cmd));
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_drawCmdGetIdxOffset(JNIEnv*, jclass, jlong cmd) {
    return (jint)imgui_draw_cmd_get_idx_offset(reinterpret_cast<imgui_draw_cmd*>(cmd));
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_drawCmdGetElemCount(JNIEnv*, jclass, jlong cmd) {
    return (jint)imgui_draw_cmd_get_elem_count(reinterpret_cast<imgui_draw_cmd*>(cmd));
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_drawCmdHasUserCallback(JNIEnv*, jclass, jlong cmd) {
    return imgui_draw_cmd_has_user_callback(reinterpret_cast<imgui_draw_cmd*>(cmd)) ? JNI_TRUE : JNI_FALSE;
}

// =========================================================================
// ImPlot
// =========================================================================

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_createContext(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(implot_create_context());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_destroyContext(JNIEnv*, jclass, jlong ptr) {
    implot_destroy_context(reinterpret_cast<implot_context*>(ptr));
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_getCurrentContext(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(implot_get_current_context());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setImGuiContext(JNIEnv*, jclass, jlong ctx) {
    implot_set_im_gui_context(reinterpret_cast<imgui_context*>(ctx));
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_showDemoWindow(JNIEnv* env, jclass, jbooleanArray p_open) {
    jboolean* elems = p_open != nullptr ? env->GetBooleanArrayElements(p_open, nullptr) : nullptr;
    implot_show_demo_window(reinterpret_cast<bool*>(elems));
    if (elems != nullptr) {
        env->ReleaseBooleanArrayElements(p_open, elems, 0);
    }
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_beginPlot(JNIEnv* env, jclass, jstring title_id, jfloat size_x, jfloat size_y, jint flags) {
    std::string title = jstring_to_string(env, title_id);
    imgui_vec2 size;
    size.x = size_x;
    size.y = size_y;
    return implot_begin_plot(title.c_str(), size, flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_endPlot(JNIEnv*, jclass) {
    implot_end_plot();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setupAxes(JNIEnv* env, jclass, jstring x_label, jstring y_label, jint x_flags, jint y_flags) {
    std::string x = jstring_to_string(env, x_label);
    std::string y = jstring_to_string(env, y_label);
    implot_setup_axes(x.empty() ? nullptr : x.c_str(), y.empty() ? nullptr : y.c_str(), x_flags, y_flags);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setupAxesLimits(JNIEnv*, jclass, jdouble x_min, jdouble x_max, jdouble y_min, jdouble y_max, jint cond) {
    implot_setup_axes_limits(x_min, x_max, y_min, y_max, cond);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setupAxisLimits(JNIEnv*, jclass, jint axis, jdouble v_min, jdouble v_max, jint cond) {
    implot_setup_axis_limits(axis, v_min, v_max, cond);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setupLegend(JNIEnv*, jclass, jint location, jint flags) {
    implot_setup_legend(location, flags);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setupFinish(JNIEnv*, jclass) {
    implot_setup_finish();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setNextAxesLimits(JNIEnv*, jclass, jdouble x_min, jdouble x_max, jdouble y_min, jdouble y_max, jint cond) {
    implot_set_next_axes_limits(x_min, x_max, y_min, y_max, cond);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setNextAxisLimits(JNIEnv*, jclass, jint axis, jdouble v_min, jdouble v_max, jint cond) {
    implot_set_next_axis_limits(axis, v_min, v_max, cond);
}

// Fills an implot_spec C struct from a float spec array encoded as
// [line_color4?, line_weight?, fill_color4?, fill_alpha?, marker?,
//  marker_size?, marker_line_color4?, marker_fill_color4?, size?,
//  offset?, stride?, flags?] — each entry prefixed by a "set" flag.
static implot_spec decode_spec(const jfloat* data) {
    implot_spec spec;
    memset(&spec, 0, sizeof(spec));
    if (data == nullptr) {
        return spec;
    }
    int i = 0;
    if (data[i++] != 0.0f) {
        spec.line_color_set = 1;
        for (int j = 0; j < 4; j++) spec.line_color[j] = data[i++];
    } else {
        i += 4;
    }
    if (data[i++] != 0.0f) {
        spec.line_weight_set = 1;
        spec.line_weight = data[i++];
    } else {
        i += 1;
    }
    if (data[i++] != 0.0f) {
        spec.fill_color_set = 1;
        for (int j = 0; j < 4; j++) spec.fill_color[j] = data[i++];
    } else {
        i += 4;
    }
    if (data[i++] != 0.0f) {
        spec.fill_alpha_set = 1;
        spec.fill_alpha = data[i++];
    } else {
        i += 1;
    }
    if (data[i++] != 0.0f) {
        spec.marker_set = 1;
        spec.marker = (int)data[i++];
    } else {
        i += 1;
    }
    if (data[i++] != 0.0f) {
        spec.marker_size_set = 1;
        spec.marker_size = data[i++];
    } else {
        i += 1;
    }
    if (data[i++] != 0.0f) {
        spec.marker_line_color_set = 1;
        for (int j = 0; j < 4; j++) spec.marker_line_color[j] = data[i++];
    } else {
        i += 4;
    }
    if (data[i++] != 0.0f) {
        spec.marker_fill_color_set = 1;
        for (int j = 0; j < 4; j++) spec.marker_fill_color[j] = data[i++];
    } else {
        i += 4;
    }
    if (data[i++] != 0.0f) {
        spec.size_set = 1;
        spec.size = data[i++];
    } else {
        i += 1;
    }
    if (data[i++] != 0.0f) {
        spec.offset_set = 1;
        spec.offset = (int)data[i++];
    } else {
        i += 1;
    }
    if (data[i++] != 0.0f) {
        spec.stride_set = 1;
        spec.stride = (int)data[i++];
    } else {
        i += 1;
    }
    if (data[i++] != 0.0f) {
        spec.flags_set = 1;
        spec.flags = (int)data[i++];
    }
    return spec;
}

#define IMPLOT_SPEC_FLOAT_COUNT 57 // 12 set-flags + 45 value slots

#define IMPLOT_PLOT_ITEM_BODY(name)                                                                        \
    std::string label = jstring_to_string(env, label_id);                                                  \
    jfloat* xs = env->GetFloatArrayElements(xs_arr, nullptr);                                              \
    jfloat* ys = env->GetFloatArrayElements(ys_arr, nullptr);                                              \
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;     \
    implot_spec spec = decode_spec(spec_data);                                                              \
    name(label.c_str(), xs, ys, count, &spec);                                                               \
    env->ReleaseFloatArrayElements(xs_arr, xs, JNI_ABORT);                                                  \
    env->ReleaseFloatArrayElements(ys_arr, ys, JNI_ABORT);                                                  \
    if (spec_arr != nullptr) {                                                                              \
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);                                     \
    }

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotLine(JNIEnv* env, jclass, jstring label_id, jfloatArray xs_arr, jfloatArray ys_arr, jint count, jfloatArray spec_arr) {
    IMPLOT_PLOT_ITEM_BODY(implot_plot_line)
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotLineValues(JNIEnv* env, jclass, jstring label_id, jfloatArray values_arr, jint count, jdouble xscale, jdouble xstart, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* values = env->GetFloatArrayElements(values_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_line_values(label.c_str(), values, count, xscale, xstart, &spec);
    env->ReleaseFloatArrayElements(values_arr, values, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotScatter(JNIEnv* env, jclass, jstring label_id, jfloatArray xs_arr, jfloatArray ys_arr, jint count, jfloatArray spec_arr) {
    IMPLOT_PLOT_ITEM_BODY(implot_plot_scatter)
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotScatterValues(JNIEnv* env, jclass, jstring label_id, jfloatArray values_arr, jint count, jdouble xscale, jdouble xstart, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* values = env->GetFloatArrayElements(values_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_scatter_values(label.c_str(), values, count, xscale, xstart, &spec);
    env->ReleaseFloatArrayElements(values_arr, values, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotStairs(JNIEnv* env, jclass, jstring label_id, jfloatArray xs_arr, jfloatArray ys_arr, jint count, jfloatArray spec_arr) {
    IMPLOT_PLOT_ITEM_BODY(implot_plot_stairs)
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotBars(JNIEnv* env, jclass, jstring label_id, jfloatArray xs_arr, jfloatArray ys_arr, jint count, jdouble bar_size, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* xs = env->GetFloatArrayElements(xs_arr, nullptr);
    jfloat* ys = env->GetFloatArrayElements(ys_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_bars(label.c_str(), xs, ys, count, bar_size, &spec);
    env->ReleaseFloatArrayElements(xs_arr, xs, JNI_ABORT);
    env->ReleaseFloatArrayElements(ys_arr, ys, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotBarsValues(JNIEnv* env, jclass, jstring label_id, jfloatArray values_arr, jint count, jdouble bar_size, jdouble shift, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* values = env->GetFloatArrayElements(values_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_bars_values(label.c_str(), values, count, bar_size, shift, &spec);
    env->ReleaseFloatArrayElements(values_arr, values, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT jdouble JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotHistogram(JNIEnv* env, jclass, jstring label_id, jfloatArray values_arr, jint count, jint bins, jdouble bar_scale, jdouble range_min, jdouble range_max, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* values = env->GetFloatArrayElements(values_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    jdouble result = implot_plot_histogram(label.c_str(), values, count, bins, bar_scale, range_min, range_max, &spec);
    env->ReleaseFloatArrayElements(values_arr, values, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
    return result;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotInfLines(JNIEnv* env, jclass, jstring label_id, jfloatArray values_arr, jint count, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* values = env->GetFloatArrayElements(values_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_inf_lines(label.c_str(), values, count, &spec);
    env->ReleaseFloatArrayElements(values_arr, values, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotShaded(JNIEnv* env, jclass, jstring label_id, jfloatArray xs_arr, jfloatArray ys_arr, jint count, jdouble yref, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* xs = env->GetFloatArrayElements(xs_arr, nullptr);
    jfloat* ys = env->GetFloatArrayElements(ys_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_shaded(label.c_str(), xs, ys, count, yref, &spec);
    env->ReleaseFloatArrayElements(xs_arr, xs, JNI_ABORT);
    env->ReleaseFloatArrayElements(ys_arr, ys, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotText(JNIEnv* env, jclass, jstring text, jdouble x, jdouble y, jfloat pix_x, jfloat pix_y) {
    std::string text_str = jstring_to_string(env, text);
    imgui_vec2 offset;
    offset.x = pix_x;
    offset.y = pix_y;
    implot_plot_text(text_str.c_str(), x, y, offset);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotDummy(JNIEnv* env, jclass, jstring label_id, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_dummy(label.c_str(), &spec);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_pushStyleColorVec4(JNIEnv*, jclass, jint idx, jfloat r, jfloat g, jfloat b, jfloat a) {
    imgui_vec4 color;
    color.x = r;
    color.y = g;
    color.z = b;
    color.w = a;
    implot_push_style_color_vec4(idx, color);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_pushStyleColorU32(JNIEnv*, jclass, jint idx, jint color) {
    implot_push_style_color_u32(idx, (uint32_t)color);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_popStyleColor(JNIEnv*, jclass, jint count) {
    implot_pop_style_color(count);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_pushStyleVarFloat(JNIEnv*, jclass, jint idx, jfloat val) {
    implot_push_style_var_float(idx, val);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_pushStyleVarInt(JNIEnv*, jclass, jint idx, jint val) {
    implot_push_style_var_int(idx, val);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_pushStyleVarVec2(JNIEnv*, jclass, jint idx, jfloat x, jfloat y) {
    imgui_vec2 val;
    val.x = x;
    val.y = y;
    implot_push_style_var_vec2(idx, val);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_popStyleVar(JNIEnv*, jclass, jint count) {
    implot_pop_style_var(count);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_pushColormap(JNIEnv*, jclass, jint cmap) {
    implot_push_colormap(cmap);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_popColormap(JNIEnv*, jclass, jint count) {
    implot_pop_colormap(count);
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_isPlotHovered(JNIEnv*, jclass) {
    return implot_is_plot_hovered() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_isPlotSelected(JNIEnv*, jclass) {
    return implot_is_plot_selected() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_isAxisHovered(JNIEnv*, jclass, jint axis) {
    return implot_is_axis_hovered(axis) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_getPlotPos(JNIEnv* env, jclass) {
    imgui_vec2 v = implot_get_plot_pos();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_getPlotSize(JNIEnv* env, jclass) {
    imgui_vec2 v = implot_get_plot_size();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}
