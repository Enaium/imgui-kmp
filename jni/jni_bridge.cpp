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

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_showAboutWindow(JNIEnv* env, jclass, jbooleanArray p_open) {
    jboolean* elems = p_open != nullptr ? env->GetBooleanArrayElements(p_open, nullptr) : nullptr;
    imgui_show_about_window(reinterpret_cast<bool*>(elems));
    if (elems != nullptr) {
        env->ReleaseBooleanArrayElements(p_open, elems, 0);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_showMetricsWindow(JNIEnv* env, jclass, jbooleanArray p_open) {
    jboolean* elems = p_open != nullptr ? env->GetBooleanArrayElements(p_open, nullptr) : nullptr;
    imgui_show_metrics_window(reinterpret_cast<bool*>(elems));
    if (elems != nullptr) {
        env->ReleaseBooleanArrayElements(p_open, elems, 0);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_showDebugLogWindow(JNIEnv* env, jclass, jbooleanArray p_open) {
    jboolean* elems = p_open != nullptr ? env->GetBooleanArrayElements(p_open, nullptr) : nullptr;
    imgui_show_debug_log_window(reinterpret_cast<bool*>(elems));
    if (elems != nullptr) {
        env->ReleaseBooleanArrayElements(p_open, elems, 0);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_showUserGuide(JNIEnv*, jclass) {
    imgui_show_user_guide();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_showIDStackToolWindow(JNIEnv* env, jclass, jbooleanArray p_open) {
    jboolean* elems = p_open != nullptr ? env->GetBooleanArrayElements(p_open, nullptr) : nullptr;
    imgui_show_id_stack_tool_window(reinterpret_cast<bool*>(elems));
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

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setWindowSize(JNIEnv*, jclass, jfloat w, jfloat h, jint cond) {
    imgui_vec2 size;
    size.x = w;
    size.y = h;
    imgui_set_window_size(size, cond);
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

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginPopupContextItem(JNIEnv* env, jclass, jstring str_id, jint popup_flags) {
    std::string str_id_str = jstring_to_string(env, str_id);
    return imgui_begin_popup_context_item(str_id_str.empty() ? nullptr : str_id_str.c_str(), popup_flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginPopupContextWindow(JNIEnv* env, jclass, jstring str_id, jint popup_flags) {
    std::string str_id_str = jstring_to_string(env, str_id);
    return imgui_begin_popup_context_window(str_id_str.empty() ? nullptr : str_id_str.c_str(), popup_flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginItemTooltip(JNIEnv*, jclass) {
    return imgui_begin_item_tooltip() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_openPopupOnItemClick(JNIEnv* env, jclass, jstring str_id, jint popup_flags) {
    std::string str_id_str = jstring_to_string(env, str_id);
    imgui_open_popup_on_item_click(str_id_str.empty() ? nullptr : str_id_str.c_str(), popup_flags);
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
// Drag and drop
// =========================================================================

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginDragDropSource(JNIEnv*, jclass, jint flags) {
    return imgui_begin_drag_drop_source(flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_setDragDropPayload(JNIEnv* env, jclass, jstring type, jbyteArray data, jint cond) {
    std::string type_str = jstring_to_string(env, type);
    jbyte* elems = data != nullptr ? env->GetByteArrayElements(data, nullptr) : nullptr;
    jsize size = data != nullptr ? env->GetArrayLength(data) : 0;
    bool result = imgui_set_drag_drop_payload(type_str.c_str(), elems, (int)size, cond);
    if (elems != nullptr) {
        env->ReleaseByteArrayElements(data, elems, JNI_ABORT);
    }
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endDragDropSource(JNIEnv*, jclass) {
    imgui_end_drag_drop_source();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginDragDropTarget(JNIEnv*, jclass) {
    return imgui_begin_drag_drop_target() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jbyteArray JNICALL Java_cn_enaium_imgui_Jni_acceptDragDropPayload(JNIEnv* env, jclass, jstring type, jint flags) {
    std::string type_str = jstring_to_string(env, type);
    int size = 0;
    const void* data = imgui_accept_drag_drop_payload(type_str.c_str(), flags, &size);
    if (data == nullptr || size <= 0) {
        return nullptr;
    }
    jbyteArray out = env->NewByteArray(size);
    env->SetByteArrayRegion(out, 0, size, reinterpret_cast<const jbyte*>(data));
    return out;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endDragDropTarget(JNIEnv*, jclass) {
    imgui_end_drag_drop_target();
}

extern "C" JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_Jni_getDragDropPayload(JNIEnv* env, jclass) {
    const char* type = imgui_get_drag_drop_payload_type();
    return type == nullptr ? nullptr : string_to_jstring(env, type);
}

// =========================================================================
// Images
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_image(JNIEnv*, jclass, jlong tex_id, jfloat size_x, jfloat size_y, jfloat uv0_x, jfloat uv0_y, jfloat uv1_x, jfloat uv1_y, jfloat tint_r, jfloat tint_g, jfloat tint_b, jfloat tint_a, jfloat border_r, jfloat border_g, jfloat border_b, jfloat border_a) {
    imgui_vec2 size;
    size.x = size_x;
    size.y = size_y;
    imgui_vec2 uv0;
    uv0.x = uv0_x;
    uv0.y = uv0_y;
    imgui_vec2 uv1;
    uv1.x = uv1_x;
    uv1.y = uv1_y;
    imgui_vec4 tint;
    tint.x = tint_r;
    tint.y = tint_g;
    tint.z = tint_b;
    tint.w = tint_a;
    imgui_vec4 border;
    border.x = border_r;
    border.y = border_g;
    border.z = border_b;
    border.w = border_a;
    imgui_image((uint64_t)tex_id, size, uv0, uv1, tint, border);
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_imageButton(JNIEnv*, jclass, jlong tex_id, jfloat size_x, jfloat size_y, jfloat uv0_x, jfloat uv0_y, jfloat uv1_x, jfloat uv1_y, jint frame_padding, jfloat bg_r, jfloat bg_g, jfloat bg_b, jfloat bg_a, jfloat tint_r, jfloat tint_g, jfloat tint_b, jfloat tint_a) {
    imgui_vec2 size;
    size.x = size_x;
    size.y = size_y;
    imgui_vec2 uv0;
    uv0.x = uv0_x;
    uv0.y = uv0_y;
    imgui_vec2 uv1;
    uv1.x = uv1_x;
    uv1.y = uv1_y;
    imgui_vec4 bg;
    bg.x = bg_r;
    bg.y = bg_g;
    bg.z = bg_b;
    bg.w = bg_a;
    imgui_vec4 tint;
    tint.x = tint_r;
    tint.y = tint_g;
    tint.z = tint_b;
    tint.w = tint_a;
    return imgui_image_button((uint64_t)tex_id, size, uv0, uv1, frame_padding, bg, tint) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_imageWithBg(JNIEnv*, jclass, jlong tex_id, jfloat size_x, jfloat size_y, jfloat bg_r, jfloat bg_g, jfloat bg_b, jfloat bg_a, jfloat uv0_x, jfloat uv0_y, jfloat uv1_x, jfloat uv1_y) {
    imgui_vec2 size;
    size.x = size_x;
    size.y = size_y;
    imgui_vec4 bg;
    bg.x = bg_r;
    bg.y = bg_g;
    bg.z = bg_b;
    bg.w = bg_a;
    imgui_vec2 uv0;
    uv0.x = uv0_x;
    uv0.y = uv0_y;
    imgui_vec2 uv1;
    uv1.x = uv1_x;
    uv1.y = uv1_y;
    imgui_image_with_bg((uint64_t)tex_id, size, bg, uv0, uv1);
}

// =========================================================================
// List boxes
// =========================================================================

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_beginListBox(JNIEnv* env, jclass, jstring label, jfloat w, jfloat h) {
    std::string label_str = jstring_to_string(env, label);
    imgui_vec2 size;
    size.x = w;
    size.y = h;
    return imgui_begin_list_box(label_str.c_str(), size) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_endListBox(JNIEnv*, jclass) {
    imgui_end_list_box();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_listBox(JNIEnv* env, jclass, jstring label, jintArray current_item, jobjectArray items) {
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
    bool result = imgui_list_box(label_str.c_str(), &current, item_ptrs.data(), (int)count);
    elems[0] = current;
    env->ReleaseIntArrayElements(current_item, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

// =========================================================================
// Multi select
// =========================================================================

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_beginMultiSelect(JNIEnv*, jclass, jint flags, jint selection_size, jint items_count) {
    return reinterpret_cast<jlong>(imgui_begin_multi_select(flags, selection_size, items_count));
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_endMultiSelect(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(imgui_end_multi_select());
}

// =========================================================================
// Logging
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_logToClipboard(JNIEnv*, jclass, jint auto_open_depth) {
    imgui_log_to_clipboard(auto_open_depth);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_logToFile(JNIEnv* env, jclass, jint auto_open_depth, jstring filename) {
    std::string filename_str = jstring_to_string(env, filename);
    imgui_log_to_file(auto_open_depth, filename_str.empty() ? nullptr : filename_str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_logToTTY(JNIEnv*, jclass, jint auto_open_depth) {
    imgui_log_to_tty(auto_open_depth);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_logFinish(JNIEnv*, jclass) {
    imgui_log_finish();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_logText(JNIEnv* env, jclass, jstring text) {
    std::string text_str = jstring_to_string(env, text);
    imgui_log_text(text_str.c_str());
}

// =========================================================================
// .ini settings persistence
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_saveIniSettingsToDisk(JNIEnv* env, jclass, jstring ini_filename) {
    std::string filename_str = jstring_to_string(env, ini_filename);
    imgui_save_ini_settings_to_disk(filename_str.empty() ? nullptr : filename_str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_loadIniSettingsFromDisk(JNIEnv* env, jclass, jstring ini_filename) {
    std::string filename_str = jstring_to_string(env, ini_filename);
    imgui_load_ini_settings_from_disk(filename_str.empty() ? nullptr : filename_str.c_str());
}

extern "C" JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_Jni_saveIniSettingsToMemory(JNIEnv* env, jclass) {
    const char* data = imgui_save_ini_settings_to_memory();
    if (data == nullptr) {
        return nullptr;
    }
    return string_to_jstring(env, data);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_loadIniSettingsFromMemory(JNIEnv* env, jclass, jstring ini_data) {
    std::string data_str = jstring_to_string(env, ini_data);
    imgui_load_ini_settings_from_memory(data_str.c_str());
}

// =========================================================================
// Scissor rect / text wrapping
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_pushClipRect(JNIEnv*, jclass, jfloat min_x, jfloat min_y, jfloat max_x, jfloat max_y, jboolean intersect_with_current_clip_rect) {
    imgui_vec2 min;
    min.x = min_x;
    min.y = min_y;
    imgui_vec2 max;
    max.x = max_x;
    max.y = max_y;
    imgui_push_clip_rect(min, max, intersect_with_current_clip_rect == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_popClipRect(JNIEnv*, jclass) {
    imgui_pop_clip_rect();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_pushTextWrapPos(JNIEnv*, jclass, jfloat wrap_local_pos_x) {
    imgui_push_text_wrap_pos(wrap_local_pos_x);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_popTextWrapPos(JNIEnv*, jclass) {
    imgui_pop_text_wrap_pos();
}

// =========================================================================
// Widgets
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_text(JNIEnv* env, jclass, jstring text) {
    std::string str = jstring_to_string(env, text);
    imgui_text(str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_textWrapped(JNIEnv* env, jclass, jstring text) {
    std::string str = jstring_to_string(env, text);
    imgui_text_wrapped(str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_textUnformatted(JNIEnv* env, jclass, jstring text) {
    std::string str = jstring_to_string(env, text);
    imgui_text_unformatted(str.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_textLink(JNIEnv* env, jclass, jstring text) {
    std::string str = jstring_to_string(env, text);
    return imgui_text_link(str.c_str()) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_textLinkOpenURL(JNIEnv* env, jclass, jstring label, jstring url) {
    std::string label_str = jstring_to_string(env, label);
    std::string url_str = jstring_to_string(env, url);
    return imgui_text_link_open_url(label_str.c_str(), url_str.empty() ? nullptr : url_str.c_str()) ? JNI_TRUE : JNI_FALSE;
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

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_alignTextToFramePadding(JNIEnv*, jclass) {
    imgui_align_text_to_frame_padding();
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

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_arrowButton(JNIEnv* env, jclass, jstring str_id, jint dir) {
    std::string id = jstring_to_string(env, str_id);
    return imgui_arrow_button(id.c_str(), dir) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_checkboxFlags(JNIEnv* env, jclass, jstring label, jintArray flags, jint flags_value) {
    std::string label_str = jstring_to_string(env, label);
    jint* elems = env->GetIntArrayElements(flags, nullptr);
    bool result = imgui_checkbox_flags(label_str.c_str(), elems, flags_value);
    env->ReleaseIntArrayElements(flags, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_pushItemFlag(JNIEnv*, jclass, jint flag, jboolean enabled) {
    imgui_push_item_flag(flag, enabled == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_popItemFlag(JNIEnv*, jclass) {
    imgui_pop_item_flag();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_shortcut(JNIEnv*, jclass, jint key_chord, jint flags) {
    return imgui_shortcut(key_chord, flags) ? JNI_TRUE : JNI_FALSE;
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

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_dragFloat(JNIEnv* env, jclass, jstring label, jfloatArray v, jfloat v_speed, jfloat v_min, jfloat v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jfloat* elems = env->GetFloatArrayElements(v, nullptr);
    bool result = imgui_drag_float(label_str.c_str(), elems, v_speed, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_dragFloat2(JNIEnv* env, jclass, jstring label, jfloatArray v, jfloat v_speed, jfloat v_min, jfloat v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jfloat* elems = env->GetFloatArrayElements(v, nullptr);
    bool result = imgui_drag_float2(label_str.c_str(), elems, v_speed, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_dragFloat3(JNIEnv* env, jclass, jstring label, jfloatArray v, jfloat v_speed, jfloat v_min, jfloat v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jfloat* elems = env->GetFloatArrayElements(v, nullptr);
    bool result = imgui_drag_float3(label_str.c_str(), elems, v_speed, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_dragFloat4(JNIEnv* env, jclass, jstring label, jfloatArray v, jfloat v_speed, jfloat v_min, jfloat v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jfloat* elems = env->GetFloatArrayElements(v, nullptr);
    bool result = imgui_drag_float4(label_str.c_str(), elems, v_speed, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_dragFloatRange2(JNIEnv* env, jclass, jstring label, jfloatArray v_current_min, jfloatArray v_current_max, jfloat v_speed, jfloat v_min, jfloat v_max, jstring format, jstring format_max, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    std::string format_max_str = jstring_to_string(env, format_max);
    jfloat* min_elems = env->GetFloatArrayElements(v_current_min, nullptr);
    jfloat* max_elems = env->GetFloatArrayElements(v_current_max, nullptr);
    bool result = imgui_drag_float_range2(label_str.c_str(), min_elems, max_elems, v_speed, v_min, v_max, format_str.c_str(), format_max_str.empty() ? nullptr : format_max_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v_current_min, min_elems, 0);
    env->ReleaseFloatArrayElements(v_current_max, max_elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_dragInt(JNIEnv* env, jclass, jstring label, jintArray v, jfloat v_speed, jint v_min, jint v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jint* elems = env->GetIntArrayElements(v, nullptr);
    bool result = imgui_drag_int(label_str.c_str(), elems, v_speed, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseIntArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_dragInt2(JNIEnv* env, jclass, jstring label, jintArray v, jfloat v_speed, jint v_min, jint v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jint* elems = env->GetIntArrayElements(v, nullptr);
    bool result = imgui_drag_int2(label_str.c_str(), elems, v_speed, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseIntArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_dragInt3(JNIEnv* env, jclass, jstring label, jintArray v, jfloat v_speed, jint v_min, jint v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jint* elems = env->GetIntArrayElements(v, nullptr);
    bool result = imgui_drag_int3(label_str.c_str(), elems, v_speed, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseIntArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_dragInt4(JNIEnv* env, jclass, jstring label, jintArray v, jfloat v_speed, jint v_min, jint v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jint* elems = env->GetIntArrayElements(v, nullptr);
    bool result = imgui_drag_int4(label_str.c_str(), elems, v_speed, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseIntArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_dragIntRange2(JNIEnv* env, jclass, jstring label, jintArray v_current_min, jintArray v_current_max, jfloat v_speed, jint v_min, jint v_max, jstring format, jstring format_max, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    std::string format_max_str = jstring_to_string(env, format_max);
    jint* min_elems = env->GetIntArrayElements(v_current_min, nullptr);
    jint* max_elems = env->GetIntArrayElements(v_current_max, nullptr);
    bool result = imgui_drag_int_range2(label_str.c_str(), min_elems, max_elems, v_speed, v_min, v_max, format_str.c_str(), format_max_str.empty() ? nullptr : format_max_str.c_str(), flags);
    env->ReleaseIntArrayElements(v_current_min, min_elems, 0);
    env->ReleaseIntArrayElements(v_current_max, max_elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_sliderFloat2(JNIEnv* env, jclass, jstring label, jfloatArray v, jfloat v_min, jfloat v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jfloat* elems = env->GetFloatArrayElements(v, nullptr);
    bool result = imgui_slider_float2(label_str.c_str(), elems, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_sliderFloat3(JNIEnv* env, jclass, jstring label, jfloatArray v, jfloat v_min, jfloat v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jfloat* elems = env->GetFloatArrayElements(v, nullptr);
    bool result = imgui_slider_float3(label_str.c_str(), elems, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_sliderFloat4(JNIEnv* env, jclass, jstring label, jfloatArray v, jfloat v_min, jfloat v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jfloat* elems = env->GetFloatArrayElements(v, nullptr);
    bool result = imgui_slider_float4(label_str.c_str(), elems, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_sliderInt2(JNIEnv* env, jclass, jstring label, jintArray v, jint v_min, jint v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jint* elems = env->GetIntArrayElements(v, nullptr);
    bool result = imgui_slider_int2(label_str.c_str(), elems, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseIntArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_sliderInt3(JNIEnv* env, jclass, jstring label, jintArray v, jint v_min, jint v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jint* elems = env->GetIntArrayElements(v, nullptr);
    bool result = imgui_slider_int3(label_str.c_str(), elems, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseIntArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_sliderInt4(JNIEnv* env, jclass, jstring label, jintArray v, jint v_min, jint v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jint* elems = env->GetIntArrayElements(v, nullptr);
    bool result = imgui_slider_int4(label_str.c_str(), elems, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseIntArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_sliderAngle(JNIEnv* env, jclass, jstring label, jfloatArray v_rad, jfloat v_degrees_min, jfloat v_degrees_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jfloat* elems = env->GetFloatArrayElements(v_rad, nullptr);
    bool result = imgui_slider_angle(label_str.c_str(), elems, v_degrees_min, v_degrees_max, format_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v_rad, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_vSliderFloat(JNIEnv* env, jclass, jstring label, jfloat w, jfloat h, jfloatArray v, jfloat v_min, jfloat v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    imgui_vec2 size;
    size.x = w;
    size.y = h;
    jfloat* elems = env->GetFloatArrayElements(v, nullptr);
    bool result = imgui_vslider_float(label_str.c_str(), size, elems, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_vSliderInt(JNIEnv* env, jclass, jstring label, jfloat w, jfloat h, jintArray v, jint v_min, jint v_max, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    imgui_vec2 size;
    size.x = w;
    size.y = h;
    jint* elems = env->GetIntArrayElements(v, nullptr);
    bool result = imgui_vslider_int(label_str.c_str(), size, elems, v_min, v_max, format_str.c_str(), flags);
    env->ReleaseIntArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_sliderScalar(JNIEnv* env, jclass, jstring label, jint data_type, jlongArray v, jlongArray v_min, jlongArray v_max, jstring format) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jlong* elems = env->GetLongArrayElements(v, nullptr);
    jlong* min_elems = env->GetLongArrayElements(v_min, nullptr);
    jlong* max_elems = env->GetLongArrayElements(v_max, nullptr);
    bool result = imgui_slider_scalar(label_str.c_str(), data_type, reinterpret_cast<int64_t*>(elems), reinterpret_cast<int64_t*>(min_elems), reinterpret_cast<int64_t*>(max_elems), format_str.c_str());
    env->ReleaseLongArrayElements(v, elems, 0);
    env->ReleaseLongArrayElements(v_min, min_elems, 0);
    env->ReleaseLongArrayElements(v_max, max_elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_dragScalar(JNIEnv* env, jclass, jstring label, jint data_type, jlongArray v, jfloat v_speed, jlongArray v_min, jlongArray v_max, jstring format) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jlong* elems = env->GetLongArrayElements(v, nullptr);
    jlong* min_elems = env->GetLongArrayElements(v_min, nullptr);
    jlong* max_elems = env->GetLongArrayElements(v_max, nullptr);
    bool result = imgui_drag_scalar(label_str.c_str(), data_type, reinterpret_cast<int64_t*>(elems), v_speed, reinterpret_cast<int64_t*>(min_elems), reinterpret_cast<int64_t*>(max_elems), format_str.c_str());
    env->ReleaseLongArrayElements(v, elems, 0);
    env->ReleaseLongArrayElements(v_min, min_elems, 0);
    env->ReleaseLongArrayElements(v_max, max_elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_inputFloat(JNIEnv* env, jclass, jstring label, jfloatArray v, jfloat step, jfloat step_fast, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jfloat* elems = env->GetFloatArrayElements(v, nullptr);
    bool result = imgui_input_float(label_str.c_str(), elems, step, step_fast, format_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_inputFloat2(JNIEnv* env, jclass, jstring label, jfloatArray v, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jfloat* elems = env->GetFloatArrayElements(v, nullptr);
    bool result = imgui_input_float2(label_str.c_str(), elems, format_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_inputFloat3(JNIEnv* env, jclass, jstring label, jfloatArray v, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jfloat* elems = env->GetFloatArrayElements(v, nullptr);
    bool result = imgui_input_float3(label_str.c_str(), elems, format_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_inputFloat4(JNIEnv* env, jclass, jstring label, jfloatArray v, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jfloat* elems = env->GetFloatArrayElements(v, nullptr);
    bool result = imgui_input_float4(label_str.c_str(), elems, format_str.c_str(), flags);
    env->ReleaseFloatArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_inputInt(JNIEnv* env, jclass, jstring label, jintArray v, jint step, jint step_fast, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    jint* elems = env->GetIntArrayElements(v, nullptr);
    bool result = imgui_input_int(label_str.c_str(), elems, step, step_fast, flags);
    env->ReleaseIntArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_inputInt2(JNIEnv* env, jclass, jstring label, jintArray v, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    jint* elems = env->GetIntArrayElements(v, nullptr);
    bool result = imgui_input_int2(label_str.c_str(), elems, flags);
    env->ReleaseIntArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_inputInt3(JNIEnv* env, jclass, jstring label, jintArray v, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    jint* elems = env->GetIntArrayElements(v, nullptr);
    bool result = imgui_input_int3(label_str.c_str(), elems, flags);
    env->ReleaseIntArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_inputInt4(JNIEnv* env, jclass, jstring label, jintArray v, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    jint* elems = env->GetIntArrayElements(v, nullptr);
    bool result = imgui_input_int4(label_str.c_str(), elems, flags);
    env->ReleaseIntArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_inputDouble(JNIEnv* env, jclass, jstring label, jdoubleArray v, jdouble step, jdouble step_fast, jstring format, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string format_str = jstring_to_string(env, format);
    jdouble* elems = env->GetDoubleArrayElements(v, nullptr);
    bool result = imgui_input_double(label_str.c_str(), elems, step, step_fast, format_str.c_str(), flags);
    env->ReleaseDoubleArrayElements(v, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_colorEdit3(JNIEnv* env, jclass, jstring label, jfloatArray col, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    jfloat* elems = env->GetFloatArrayElements(col, nullptr);
    bool result = imgui_color_edit3(label_str.c_str(), elems, flags);
    env->ReleaseFloatArrayElements(col, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_colorEdit4(JNIEnv* env, jclass, jstring label, jfloatArray col, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    jfloat* elems = env->GetFloatArrayElements(col, nullptr);
    bool result = imgui_color_edit4(label_str.c_str(), elems, flags);
    env->ReleaseFloatArrayElements(col, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_colorPicker3(JNIEnv* env, jclass, jstring label, jfloatArray col, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    jfloat* elems = env->GetFloatArrayElements(col, nullptr);
    bool result = imgui_color_picker3(label_str.c_str(), elems, flags);
    env->ReleaseFloatArrayElements(col, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_colorPicker4(JNIEnv* env, jclass, jstring label, jfloatArray col, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    jfloat* elems = env->GetFloatArrayElements(col, nullptr);
    bool result = imgui_color_picker4(label_str.c_str(), elems, flags);
    env->ReleaseFloatArrayElements(col, elems, 0);
    return result ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_colorButton(JNIEnv* env, jclass, jstring desc_id, jfloat r, jfloat g, jfloat b, jfloat a, jint flags, jfloat w, jfloat h) {
    std::string id = jstring_to_string(env, desc_id);
    imgui_vec4 col;
    col.x = r;
    col.y = g;
    col.z = b;
    col.w = a;
    imgui_vec2 size;
    size.x = w;
    size.y = h;
    return imgui_color_button(id.c_str(), col, flags, size) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setColorEditOptions(JNIEnv*, jclass, jint flags) {
    imgui_set_color_edit_options(flags);
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_colorConvertFloat4ToU32(JNIEnv*, jclass, jfloat r, jfloat g, jfloat b, jfloat a) {
    imgui_vec4 in;
    in.x = r;
    in.y = g;
    in.z = b;
    in.w = a;
    return (jint)imgui_color_convert_float4_to_u32(in);
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_colorConvertU32ToFloat4(JNIEnv* env, jclass, jint in) {
    imgui_vec4 c = imgui_color_convert_u32_to_float4((uint32_t)in);
    jfloatArray out = env->NewFloatArray(4);
    jfloat values[4] = {c.x, c.y, c.z, c.w};
    env->SetFloatArrayRegion(out, 0, 4, values);
    return out;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_colorConvertRGBtoHSV(JNIEnv* env, jclass, jfloat r, jfloat g, jfloat b, jfloatArray out_h, jfloatArray out_s, jfloatArray out_v) {
    jfloat* h = env->GetFloatArrayElements(out_h, nullptr);
    jfloat* s = env->GetFloatArrayElements(out_s, nullptr);
    jfloat* v = env->GetFloatArrayElements(out_v, nullptr);
    imgui_color_convert_rgb_to_hsv(r, g, b, h, s, v);
    env->ReleaseFloatArrayElements(out_h, h, 0);
    env->ReleaseFloatArrayElements(out_s, s, 0);
    env->ReleaseFloatArrayElements(out_v, v, 0);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_colorConvertHSVtoRGB(JNIEnv* env, jclass, jfloat h, jfloat s, jfloat v, jfloatArray out_r, jfloatArray out_g, jfloatArray out_b) {
    jfloat* r = env->GetFloatArrayElements(out_r, nullptr);
    jfloat* g = env->GetFloatArrayElements(out_g, nullptr);
    jfloat* b = env->GetFloatArrayElements(out_b, nullptr);
    imgui_color_convert_hsv_to_rgb(h, s, v, r, g, b);
    env->ReleaseFloatArrayElements(out_r, r, 0);
    env->ReleaseFloatArrayElements(out_g, g, 0);
    env->ReleaseFloatArrayElements(out_b, b, 0);
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

extern "C" JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_Jni_inputTextMultiline(JNIEnv* env, jclass, jstring label, jstring buf, jfloat w, jfloat h, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string initial = jstring_to_string(env, buf);
    std::string key = "##imgui_kmp_ml_" + label_str;

    std::vector<char>& buffer = g_input_buffers[key];
    size_t needed = initial.size() + 2048;
    if (buffer.size() < needed) {
        buffer.resize(needed);
    }
    std::memcpy(buffer.data(), initial.c_str(), initial.size() + 1);

    imgui_vec2 size;
    size.x = w;
    size.y = h;
    imgui_input_text_multiline(label_str.c_str(), buffer.data(), (int)buffer.size(), size, flags);

    jstring out = string_to_jstring(env, buffer.data());
    release_input_buffer_if_idle(key.c_str());
    return out;
}

extern "C" JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_Jni_inputTextWithHint(JNIEnv* env, jclass, jstring label, jstring hint, jstring buf, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    std::string hint_str = jstring_to_string(env, hint);
    std::string initial = jstring_to_string(env, buf);
    std::string key = "##imgui_kmp_hint_" + label_str;

    std::vector<char>& buffer = g_input_buffers[key];
    size_t needed = initial.size() + 512;
    if (buffer.size() < needed) {
        buffer.resize(needed);
    }
    std::memcpy(buffer.data(), initial.c_str(), initial.size() + 1);

    imgui_input_text_with_hint(label_str.c_str(), hint_str.c_str(), buffer.data(), (int)buffer.size(), flags);

    jstring out = string_to_jstring(env, buffer.data());
    release_input_buffer_if_idle(key.c_str());
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

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_treeNodeEx(JNIEnv* env, jclass, jstring label, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    return imgui_tree_node_ex(label_str.c_str(), flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_treeNodeGetOpen(JNIEnv* env, jclass, jstring str_id) {
    std::string id = jstring_to_string(env, str_id);
    return imgui_tree_node_get_open(id.c_str()) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_treePush(JNIEnv* env, jclass, jstring str_id) {
    std::string id = jstring_to_string(env, str_id);
    imgui_tree_push(id.empty() ? nullptr : id.c_str());
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
// State queries
// =========================================================================

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isItemFocused(JNIEnv*, jclass) {
    return imgui_is_item_focused() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isItemVisible(JNIEnv*, jclass) {
    return imgui_is_item_visible() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isItemEdited(JNIEnv*, jclass) {
    return imgui_is_item_edited() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isItemActivated(JNIEnv*, jclass) {
    return imgui_is_item_activated() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isItemDeactivated(JNIEnv*, jclass) {
    return imgui_is_item_deactivated() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isItemDeactivatedAfterEdit(JNIEnv*, jclass) {
    return imgui_is_item_deactivated_after_edit() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isItemToggledOpen(JNIEnv*, jclass) {
    return imgui_is_item_toggled_open() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isItemToggledSelection(JNIEnv*, jclass) {
    return imgui_is_item_toggled_selection() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isAnyItemHovered(JNIEnv*, jclass) {
    return imgui_is_any_item_hovered() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isAnyItemActive(JNIEnv*, jclass) {
    return imgui_is_any_item_active() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isAnyItemFocused(JNIEnv*, jclass) {
    return imgui_is_any_item_focused() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_getItemID(JNIEnv*, jclass) {
    return imgui_get_item_id();
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_getItemFlags(JNIEnv*, jclass) {
    return imgui_get_item_flags();
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getItemRectMin(JNIEnv* env, jclass) {
    imgui_vec2 v = imgui_get_item_rect_min();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getItemRectMax(JNIEnv* env, jclass) {
    imgui_vec2 v = imgui_get_item_rect_max();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getItemRectSize(JNIEnv* env, jclass) {
    imgui_vec2 v = imgui_get_item_rect_size();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isWindowAppearing(JNIEnv*, jclass) {
    return imgui_is_window_appearing() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isWindowCollapsed(JNIEnv*, jclass) {
    return imgui_is_window_collapsed() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isRectVisible(JNIEnv*, jclass, jfloat w, jfloat h) {
    imgui_vec2 size;
    size.x = w;
    size.y = h;
    return imgui_is_rect_visible(size) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isPopupOpen(JNIEnv* env, jclass, jstring str_id, jint flags) {
    std::string id = jstring_to_string(env, str_id);
    return imgui_is_popup_open(id.c_str(), flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getWindowPos(JNIEnv* env, jclass) {
    imgui_vec2 v = imgui_get_window_pos();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getWindowSize(JNIEnv* env, jclass) {
    imgui_vec2 v = imgui_get_window_size();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getWindowWidth(JNIEnv*, jclass) {
    return imgui_get_window_width();
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getWindowHeight(JNIEnv*, jclass) {
    return imgui_get_window_height();
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getWindowContentRegionMax(JNIEnv* env, jclass) {
    imgui_vec2 v = imgui_get_window_content_region_max();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getWindowContentRegionMin(JNIEnv* env, jclass) {
    imgui_vec2 v = imgui_get_window_content_region_min();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_getWindowDrawList(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(imgui_get_window_draw_list());
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_getForegroundDrawList(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(imgui_get_foreground_draw_list());
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_getBackgroundDrawList(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(imgui_get_background_draw_list());
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isKeyDown(JNIEnv*, jclass, jint key) {
    return imgui_is_key_down(key) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isKeyPressed(JNIEnv*, jclass, jint key, jboolean repeat) {
    return imgui_is_key_pressed(key, repeat == JNI_TRUE) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isKeyReleased(JNIEnv*, jclass, jint key) {
    return imgui_is_key_released(key) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isMouseDown(JNIEnv*, jclass, jint button) {
    return imgui_is_mouse_down(button) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isMouseClicked(JNIEnv*, jclass, jint button, jboolean repeat) {
    return imgui_is_mouse_clicked(button, repeat == JNI_TRUE) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isMouseReleased(JNIEnv*, jclass, jint button) {
    return imgui_is_mouse_released(button) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isMouseDoubleClicked(JNIEnv*, jclass, jint button) {
    return imgui_is_mouse_double_clicked(button) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isMouseDragging(JNIEnv*, jclass, jint button, jfloat lock_threshold) {
    return imgui_is_mouse_dragging(button, lock_threshold) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isAnyMouseDown(JNIEnv*, jclass) {
    return imgui_is_any_mouse_down() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_isMousePosValid(JNIEnv*, jclass, jboolean has_pos, jfloat x, jfloat y) {
    if (has_pos == JNI_TRUE) {
        imgui_vec2 pos;
        pos.x = x;
        pos.y = y;
        return imgui_is_mouse_pos_valid(&pos) ? JNI_TRUE : JNI_FALSE;
    }
    return imgui_is_mouse_pos_valid(nullptr) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getMousePos(JNIEnv* env, jclass) {
    imgui_vec2 v = imgui_get_mouse_pos();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getMouseDragDelta(JNIEnv* env, jclass, jint button, jfloat lock_threshold) {
    imgui_vec2 v = imgui_get_mouse_drag_delta(button, lock_threshold);
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_resetMouseDragDelta(JNIEnv*, jclass, jint button) {
    imgui_reset_mouse_drag_delta(button);
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_getMouseCursor(JNIEnv*, jclass) {
    return imgui_get_mouse_cursor();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setMouseCursor(JNIEnv*, jclass, jint cursor) {
    imgui_set_mouse_cursor(cursor);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setKeyboardFocusHere(JNIEnv*, jclass, jint offset) {
    imgui_set_keyboard_focus_here(offset);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextFrameWantCaptureKeyboard(JNIEnv*, jclass, jboolean want_capture_keyboard) {
    imgui_set_next_frame_want_capture_keyboard(want_capture_keyboard == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextFrameWantCaptureMouse(JNIEnv*, jclass, jboolean want_capture_mouse) {
    imgui_set_next_frame_want_capture_mouse(want_capture_mouse == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setClipboardText(JNIEnv* env, jclass, jstring text) {
    std::string str = jstring_to_string(env, text);
    imgui_set_clipboard_text(str.c_str());
}

extern "C" JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_Jni_getClipboardText(JNIEnv* env, jclass) {
    const char* text = imgui_get_clipboard_text();
    return text != nullptr ? string_to_jstring(env, text) : nullptr;
}

extern "C" JNIEXPORT jdouble JNICALL Java_cn_enaium_imgui_Jni_getTime(JNIEnv*, jclass) {
    return imgui_get_time();
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getCursorPos(JNIEnv* env, jclass) {
    imgui_vec2 v = imgui_get_cursor_pos();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getCursorScreenPos(JNIEnv* env, jclass) {
    imgui_vec2 v = imgui_get_cursor_screen_pos();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getCursorStartPos(JNIEnv* env, jclass) {
    imgui_vec2 v = imgui_get_cursor_start_pos();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setCursorPosX(JNIEnv*, jclass, jfloat local_x) {
    imgui_set_cursor_pos_x(local_x);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setCursorScreenPos(JNIEnv*, jclass, jfloat x, jfloat y) {
    imgui_vec2 pos;
    pos.x = x;
    pos.y = y;
    imgui_set_cursor_screen_pos(pos);
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getContentRegionAvail(JNIEnv* env, jclass) {
    imgui_vec2 v = imgui_get_content_region_avail();
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getScrollX(JNIEnv*, jclass) {
    return imgui_get_scroll_x();
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getScrollY(JNIEnv*, jclass) {
    return imgui_get_scroll_y();
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getScrollMaxX(JNIEnv*, jclass) {
    return imgui_get_scroll_max_x();
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getScrollMaxY(JNIEnv*, jclass) {
    return imgui_get_scroll_max_y();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setScrollHereX(JNIEnv*, jclass, jfloat center_x_ratio) {
    imgui_set_scroll_here_x(center_x_ratio);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setScrollHereY(JNIEnv*, jclass, jfloat center_y_ratio) {
    imgui_set_scroll_here_y(center_y_ratio);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setScrollFromPosX(JNIEnv*, jclass, jfloat local_x, jfloat center_x_ratio) {
    imgui_set_scroll_from_pos_x(local_x, center_x_ratio);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setScrollFromPosY(JNIEnv*, jclass, jfloat local_y, jfloat center_y_ratio) {
    imgui_set_scroll_from_pos_y(local_y, center_y_ratio);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setScrollX(JNIEnv*, jclass, jfloat scroll_x) {
    imgui_set_scroll_x(scroll_x);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setScrollY(JNIEnv*, jclass, jfloat scroll_y) {
    imgui_set_scroll_y(scroll_y);
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_getFrameCount(JNIEnv*, jclass) {
    return imgui_get_frame_count();
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getFrameHeight(JNIEnv*, jclass) {
    return imgui_get_frame_height();
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getFrameHeightWithSpacing(JNIEnv*, jclass) {
    return imgui_get_frame_height_with_spacing();
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getFontSize(JNIEnv*, jclass) {
    return imgui_get_font_size();
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_getFont(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(imgui_get_font());
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_getMainViewport(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(imgui_get_main_viewport());
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_getStyleColorVec4(JNIEnv* env, jclass, jint idx) {
    imgui_vec4 v = imgui_get_style_color_vec4(idx);
    jfloatArray out = env->NewFloatArray(4);
    jfloat values[4] = {v.x, v.y, v.z, v.w};
    env->SetFloatArrayRegion(out, 0, 4, values);
    return out;
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getCursorPosX(JNIEnv*, jclass) {
    return imgui_get_cursor_pos_x();
}

extern "C" JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_Jni_getKeyName(JNIEnv* env, jclass, jint key) {
    return string_to_jstring(env, imgui_get_key_name(key));
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getTextLineHeight(JNIEnv*, jclass) {
    return imgui_get_text_line_height();
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getTextLineHeightWithSpacing(JNIEnv*, jclass) {
    return imgui_get_text_line_height_with_spacing();
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_getID(JNIEnv* env, jclass, jstring str_id) {
    std::string id = jstring_to_string(env, str_id);
    return imgui_get_id(id.c_str());
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_getColorU32(JNIEnv*, jclass, jint idx, jfloat alpha_mul) {
    return imgui_get_color_u32(idx, alpha_mul);
}

extern "C" JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_Jni_getStyleColorName(JNIEnv* env, jclass, jint idx) {
    return string_to_jstring(env, imgui_get_style_color_name(idx));
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_calcTextSize(JNIEnv* env, jclass, jstring text, jboolean hide_text_after_double_hash, jfloat wrap_width) {
    std::string text_str = jstring_to_string(env, text);
    imgui_vec2 v = imgui_calc_text_size(text_str.c_str(), hide_text_after_double_hash == JNI_TRUE, wrap_width);
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_calcItemWidth(JNIEnv*, jclass) {
    return imgui_calc_item_width();
}

// =========================================================================
// Columns (legacy multi-column layout)
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_columns(JNIEnv* env, jclass, jint count, jstring id, jboolean border) {
    std::string id_str = jstring_to_string(env, id);
    imgui_columns(count, id_str.empty() ? nullptr : id_str.c_str(), border == JNI_TRUE);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_nextColumn(JNIEnv*, jclass) {
    imgui_next_column();
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_getColumnIndex(JNIEnv*, jclass) {
    return imgui_get_column_index();
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getColumnOffset(JNIEnv*, jclass, jint column_index) {
    return imgui_get_column_offset(column_index);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setColumnOffset(JNIEnv*, jclass, jint column_index, jfloat offset_x) {
    imgui_set_column_offset(column_index, offset_x);
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_getColumnWidth(JNIEnv*, jclass, jint column_index) {
    return imgui_get_column_width(column_index);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setColumnWidth(JNIEnv*, jclass, jint column_index, jfloat width) {
    imgui_set_column_width(column_index, width);
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_getColumnsCount(JNIEnv*, jclass) {
    return imgui_get_columns_count();
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

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_tableHeader(JNIEnv* env, jclass, jstring label) {
    std::string label_str = jstring_to_string(env, label);
    imgui_table_header(label_str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_tableAngledHeadersRow(JNIEnv*, jclass) {
    imgui_table_angled_headers_row();
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_tableGetColumnCount(JNIEnv*, jclass) {
    return imgui_table_get_column_count();
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_tableGetColumnFlags(JNIEnv*, jclass, jint column_n) {
    return imgui_table_get_column_flags(column_n);
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_tableGetColumnIndex(JNIEnv*, jclass) {
    return imgui_table_get_column_index();
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_tableGetRowIndex(JNIEnv*, jclass) {
    return imgui_table_get_row_index();
}

extern "C" JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_Jni_tableGetColumnName(JNIEnv* env, jclass, jint column_n) {
    return string_to_jstring(env, imgui_table_get_column_name(column_n));
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_tableGetSortSpecs(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(imgui_table_get_sort_specs());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_tableSetBgColor(JNIEnv*, jclass, jint target, jint color, jint column_n) {
    imgui_table_set_bg_color(target, (uint32_t)color, column_n);
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_tabItemButton(JNIEnv* env, jclass, jstring label, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    return imgui_tab_item_button(label_str.c_str(), flags) ? JNI_TRUE : JNI_FALSE;
}

// =========================================================================
// Style
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_styleColorsDark(JNIEnv*, jclass) {
    imgui_style_colors_dark();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_styleColorsLight(JNIEnv*, jclass) {
    imgui_style_colors_light();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_styleColorsClassic(JNIEnv*, jclass) {
    imgui_style_colors_classic();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_Jni_showStyleSelector(JNIEnv* env, jclass, jstring label) {
    std::string label_str = jstring_to_string(env, label);
    return imgui_show_style_selector(label_str.c_str()) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_showFontSelector(JNIEnv* env, jclass, jstring label) {
    std::string label_str = jstring_to_string(env, label);
    imgui_show_font_selector(label_str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_showStyleEditor(JNIEnv*, jclass) {
    imgui_show_style_editor();
}

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

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextItemOpen(JNIEnv*, jclass, jboolean is_open, jint cond) {
    imgui_set_next_item_open(is_open == JNI_TRUE, cond);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextItemAllowOverlap(JNIEnv*, jclass) {
    imgui_set_next_item_allow_overlap();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextItemSelectionUserData(JNIEnv*, jclass, jlong selection_user_data) {
    imgui_set_next_item_selection_user_data((int64_t)selection_user_data);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextItemShortcut(JNIEnv*, jclass, jint key_chord, jint flags) {
    imgui_set_next_item_shortcut(key_chord, flags);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextWindowCollapsed(JNIEnv*, jclass, jboolean collapsed, jint cond) {
    imgui_set_next_window_collapsed(collapsed == JNI_TRUE, cond);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextWindowContentSize(JNIEnv*, jclass, jfloat w, jfloat h) {
    imgui_vec2 size;
    size.x = w;
    size.y = h;
    imgui_set_next_window_content_size(size);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextWindowFocus(JNIEnv*, jclass) {
    imgui_set_next_window_focus();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextWindowScroll(JNIEnv*, jclass, jfloat x, jfloat y) {
    imgui_vec2 scroll;
    scroll.x = x;
    scroll.y = y;
    imgui_set_next_window_scroll(scroll);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextWindowSizeConstraints(JNIEnv*, jclass, jfloat min_w, jfloat min_h, jfloat max_w, jfloat max_h) {
    imgui_vec2 size_min;
    size_min.x = min_w;
    size_min.y = min_h;
    imgui_vec2 size_max;
    size_max.x = max_w;
    size_max.y = max_h;
    imgui_set_next_window_size_constraints(size_min, size_max);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setItemTooltip(JNIEnv* env, jclass, jstring text) {
    std::string str = jstring_to_string(env, text);
    imgui_set_item_tooltip(str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setItemDefaultFocus(JNIEnv*, jclass) {
    imgui_set_item_default_focus();
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setTabItemClosed(JNIEnv* env, jclass, jstring tab_or_docked_window_label) {
    std::string label = jstring_to_string(env, tab_or_docked_window_label);
    imgui_set_tab_item_closed(label.c_str());
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

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_ioGetDisplaySize(JNIEnv* env, jclass, jlong io) {
    imgui_vec2 v = imgui_io_get_display_size(reinterpret_cast<imgui_io*>(io));
    jfloatArray out = env->NewFloatArray(2);
    jfloat data[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, data);
    return out;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_Jni_ioGetDisplayFramebufferScale(JNIEnv* env, jclass, jlong io) {
    imgui_vec2 v = imgui_io_get_display_framebuffer_scale(reinterpret_cast<imgui_io*>(io));
    jfloatArray out = env->NewFloatArray(2);
    jfloat data[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, data);
    return out;
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_ioGetDeltaTime(JNIEnv*, jclass, jlong io) {
    return imgui_io_get_delta_time(reinterpret_cast<imgui_io*>(io));
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_ioGetConfigFlags(JNIEnv*, jclass, jlong io) {
    return imgui_io_get_config_flags(reinterpret_cast<imgui_io*>(io));
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_ioGetBackendFlags(JNIEnv*, jclass, jlong io) {
    return imgui_io_get_backend_flags(reinterpret_cast<imgui_io*>(io));
}

extern "C" JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_Jni_ioGetIniFilename(JNIEnv* env, jclass, jlong io) {
    const char* path = imgui_io_get_ini_filename(reinterpret_cast<imgui_io*>(io));
    return path == nullptr ? nullptr : env->NewStringUTF(path);
}

extern "C" JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_Jni_ioGetFontGlobalScale(JNIEnv*, jclass, jlong io) {
    return imgui_io_get_font_global_scale(reinterpret_cast<imgui_io*>(io));
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
// Draw list primitives
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_drawListAddLine(JNIEnv*, jclass, jlong list, jfloat p1_x, jfloat p1_y, jfloat p2_x, jfloat p2_y, jint col, jfloat thickness) {
    imgui_vec2 p1;
    p1.x = p1_x;
    p1.y = p1_y;
    imgui_vec2 p2;
    p2.x = p2_x;
    p2.y = p2_y;
    imgui_draw_list_add_line(reinterpret_cast<imgui_draw_list*>(list), p1, p2, (uint32_t)col, thickness);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_drawListAddRect(JNIEnv*, jclass, jlong list, jfloat min_x, jfloat min_y, jfloat max_x, jfloat max_y, jint col, jfloat rounding, jint flags, jfloat thickness) {
    imgui_vec2 min;
    min.x = min_x;
    min.y = min_y;
    imgui_vec2 max;
    max.x = max_x;
    max.y = max_y;
    imgui_draw_list_add_rect(reinterpret_cast<imgui_draw_list*>(list), min, max, (uint32_t)col, rounding, flags, thickness);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_drawListAddRectFilled(JNIEnv*, jclass, jlong list, jfloat min_x, jfloat min_y, jfloat max_x, jfloat max_y, jint col, jfloat rounding, jint flags) {
    imgui_vec2 min;
    min.x = min_x;
    min.y = min_y;
    imgui_vec2 max;
    max.x = max_x;
    max.y = max_y;
    imgui_draw_list_add_rect_filled(reinterpret_cast<imgui_draw_list*>(list), min, max, (uint32_t)col, rounding, flags);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_drawListAddCircle(JNIEnv*, jclass, jlong list, jfloat x, jfloat y, jfloat radius, jint col, jint num_segments, jfloat thickness) {
    imgui_vec2 center;
    center.x = x;
    center.y = y;
    imgui_draw_list_add_circle(reinterpret_cast<imgui_draw_list*>(list), center, radius, (uint32_t)col, num_segments, thickness);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_drawListAddCircleFilled(JNIEnv*, jclass, jlong list, jfloat x, jfloat y, jfloat radius, jint col, jint num_segments) {
    imgui_vec2 center;
    center.x = x;
    center.y = y;
    imgui_draw_list_add_circle_filled(reinterpret_cast<imgui_draw_list*>(list), center, radius, (uint32_t)col, num_segments);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_drawListAddText(JNIEnv* env, jclass, jlong list, jfloat x, jfloat y, jint col, jstring text) {
    std::string text_str = jstring_to_string(env, text);
    imgui_vec2 pos;
    pos.x = x;
    pos.y = y;
    imgui_draw_list_add_text(reinterpret_cast<imgui_draw_list*>(list), pos, (uint32_t)col, text_str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_drawListAddQuad(JNIEnv*, jclass, jlong list, jfloat p1_x, jfloat p1_y, jfloat p2_x, jfloat p2_y, jfloat p3_x, jfloat p3_y, jfloat p4_x, jfloat p4_y, jint col, jfloat thickness) {
    imgui_vec2 p1;
    p1.x = p1_x;
    p1.y = p1_y;
    imgui_vec2 p2;
    p2.x = p2_x;
    p2.y = p2_y;
    imgui_vec2 p3;
    p3.x = p3_x;
    p3.y = p3_y;
    imgui_vec2 p4;
    p4.x = p4_x;
    p4.y = p4_y;
    imgui_draw_list_add_quad(reinterpret_cast<imgui_draw_list*>(list), p1, p2, p3, p4, (uint32_t)col, thickness);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_drawListAddTriangle(JNIEnv*, jclass, jlong list, jfloat p1_x, jfloat p1_y, jfloat p2_x, jfloat p2_y, jfloat p3_x, jfloat p3_y, jint col, jfloat thickness) {
    imgui_vec2 p1;
    p1.x = p1_x;
    p1.y = p1_y;
    imgui_vec2 p2;
    p2.x = p2_x;
    p2.y = p2_y;
    imgui_vec2 p3;
    p3.x = p3_x;
    p3.y = p3_y;
    imgui_draw_list_add_triangle(reinterpret_cast<imgui_draw_list*>(list), p1, p2, p3, (uint32_t)col, thickness);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_drawListAddPolyline(JNIEnv* env, jclass, jlong list, jfloatArray points, jint col, jboolean closed, jfloat thickness) {
    jsize count = env->GetArrayLength(points) / 2;
    jfloat* elems = env->GetFloatArrayElements(points, nullptr);
    std::vector<imgui_vec2> pts;
    pts.reserve(count);
    for (jsize i = 0; i < count; i++) {
        imgui_vec2 p;
        p.x = elems[i * 2];
        p.y = elems[i * 2 + 1];
        pts.push_back(p);
    }
    env->ReleaseFloatArrayElements(points, elems, JNI_ABORT);
    imgui_draw_list_add_polyline(reinterpret_cast<imgui_draw_list*>(list), pts.data(), (int)count, (uint32_t)col, closed == JNI_TRUE, thickness);
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

// Builds a stable array of C strings (one per element) out of a Java string
// array. The returned pointers remain valid for the lifetime of `strings`.
static void get_string_array(JNIEnv* env, jobjectArray arr, std::vector<std::string>& strings, std::vector<const char*>& out) {
    int n = env->GetArrayLength(arr);
    strings.reserve(n);
    out.reserve(n);
    for (int i = 0; i < n; i++) {
        jstring str = (jstring)env->GetObjectArrayElement(arr, i);
        strings.emplace_back(jstring_to_string(env, str));
        out.push_back(strings.back().c_str());
        env->DeleteLocalRef(str);
    }
}

// =========================================================================
// ImPlot: advanced plot items
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotBarGroups(JNIEnv* env, jclass, jobjectArray labels, jfloatArray values_arr, jint item_count, jint group_count, jdouble group_size, jdouble shift, jfloatArray spec_arr) {
    std::vector<std::string> strings;
    std::vector<const char*> cstrings;
    get_string_array(env, labels, strings, cstrings);
    jfloat* values = env->GetFloatArrayElements(values_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_bar_groups(cstrings.data(), values, item_count, group_count, group_size, shift, &spec);
    env->ReleaseFloatArrayElements(values_arr, values, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotErrorBars(JNIEnv* env, jclass, jstring label_id, jfloatArray xs_arr, jfloatArray ys_arr, jfloatArray neg_arr, jfloatArray pos_arr, jint count, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* xs = env->GetFloatArrayElements(xs_arr, nullptr);
    jfloat* ys = env->GetFloatArrayElements(ys_arr, nullptr);
    jfloat* neg = env->GetFloatArrayElements(neg_arr, nullptr);
    jfloat* pos = env->GetFloatArrayElements(pos_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_error_bars(label.c_str(), xs, ys, neg, pos, count, &spec);
    env->ReleaseFloatArrayElements(xs_arr, xs, JNI_ABORT);
    env->ReleaseFloatArrayElements(ys_arr, ys, JNI_ABORT);
    env->ReleaseFloatArrayElements(neg_arr, neg, JNI_ABORT);
    env->ReleaseFloatArrayElements(pos_arr, pos, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotStems(JNIEnv* env, jclass, jstring label_id, jfloatArray xs_arr, jfloatArray ys_arr, jint count, jdouble ref, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* xs = env->GetFloatArrayElements(xs_arr, nullptr);
    jfloat* ys = env->GetFloatArrayElements(ys_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_stems(label.c_str(), xs, ys, count, ref, &spec);
    env->ReleaseFloatArrayElements(xs_arr, xs, JNI_ABORT);
    env->ReleaseFloatArrayElements(ys_arr, ys, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotHeatmap(JNIEnv* env, jclass, jstring label_id, jfloatArray values_arr, jint rows, jint cols, jdouble scale_min, jdouble scale_max, jstring label_format, jdouble bounds_min_x, jdouble bounds_min_y, jdouble bounds_max_x, jdouble bounds_max_y, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    std::string fmt = jstring_to_string(env, label_format);
    jfloat* values = env->GetFloatArrayElements(values_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_heatmap(label.c_str(), values, rows, cols, scale_min, scale_max, fmt.empty() ? nullptr : fmt.c_str(), bounds_min_x, bounds_min_y, bounds_max_x, bounds_max_y, &spec);
    env->ReleaseFloatArrayElements(values_arr, values, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT jdouble JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotHistogram2D(JNIEnv* env, jclass, jstring label_id, jfloatArray xs_arr, jfloatArray ys_arr, jint count, jint x_bins, jint y_bins, jdouble range_x_min, jdouble range_x_max, jdouble range_y_min, jdouble range_y_max, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* xs = env->GetFloatArrayElements(xs_arr, nullptr);
    jfloat* ys = env->GetFloatArrayElements(ys_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    jdouble result = implot_plot_histogram_2d(label.c_str(), xs, ys, count, x_bins, y_bins, range_x_min, range_x_max, range_y_min, range_y_max, &spec);
    env->ReleaseFloatArrayElements(xs_arr, xs, JNI_ABORT);
    env->ReleaseFloatArrayElements(ys_arr, ys, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
    return result;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotDigital(JNIEnv* env, jclass, jstring label_id, jfloatArray xs_arr, jfloatArray ys_arr, jint count, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* xs = env->GetFloatArrayElements(xs_arr, nullptr);
    jfloat* ys = env->GetFloatArrayElements(ys_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_digital(label.c_str(), xs, ys, count, &spec);
    env->ReleaseFloatArrayElements(xs_arr, xs, JNI_ABORT);
    env->ReleaseFloatArrayElements(ys_arr, ys, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotPieChart(JNIEnv* env, jclass, jobjectArray labels, jfloatArray values_arr, jint count, jdouble x, jdouble y, jdouble radius, jstring label_format, jdouble angle0, jfloatArray spec_arr) {
    std::vector<std::string> strings;
    std::vector<const char*> cstrings;
    get_string_array(env, labels, strings, cstrings);
    std::string fmt = jstring_to_string(env, label_format);
    jfloat* values = env->GetFloatArrayElements(values_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_pie_chart(cstrings.data(), values, count, x, y, radius, fmt.empty() ? nullptr : fmt.c_str(), angle0, &spec);
    env->ReleaseFloatArrayElements(values_arr, values, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotBubbles(JNIEnv* env, jclass, jstring label_id, jfloatArray xs_arr, jfloatArray ys_arr, jfloatArray sizes_arr, jint count, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* xs = env->GetFloatArrayElements(xs_arr, nullptr);
    jfloat* ys = env->GetFloatArrayElements(ys_arr, nullptr);
    jfloat* sizes = env->GetFloatArrayElements(sizes_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_bubbles(label.c_str(), xs, ys, sizes, count, &spec);
    env->ReleaseFloatArrayElements(xs_arr, xs, JNI_ABORT);
    env->ReleaseFloatArrayElements(ys_arr, ys, JNI_ABORT);
    env->ReleaseFloatArrayElements(sizes_arr, sizes, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotPolygon(JNIEnv* env, jclass, jstring label_id, jfloatArray xs_arr, jfloatArray ys_arr, jint count, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* xs = env->GetFloatArrayElements(xs_arr, nullptr);
    jfloat* ys = env->GetFloatArrayElements(ys_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    implot_plot_polygon(label.c_str(), xs, ys, count, &spec);
    env->ReleaseFloatArrayElements(xs_arr, xs, JNI_ABORT);
    env->ReleaseFloatArrayElements(ys_arr, ys, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotImage(JNIEnv* env, jclass, jstring label_id, jlong tex_id, jdouble x_min, jdouble y_min, jdouble x_max, jdouble y_max, jfloat uv_min_x, jfloat uv_min_y, jfloat uv_max_x, jfloat uv_max_y, jfloat tint_r, jfloat tint_g, jfloat tint_b, jfloat tint_a, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot_spec spec = decode_spec(spec_data);
    imgui_vec2 uv_min;
    uv_min.x = uv_min_x;
    uv_min.y = uv_min_y;
    imgui_vec2 uv_max;
    uv_max.x = uv_max_x;
    uv_max.y = uv_max_y;
    imgui_vec4 tint_col;
    tint_col.x = tint_r;
    tint_col.y = tint_g;
    tint_col.z = tint_b;
    tint_col.w = tint_a;
    implot_plot_image(label.c_str(), (uint64_t)tex_id, x_min, y_min, x_max, y_max, uv_min, uv_max, tint_col, &spec);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

// =========================================================================
// ImPlot: axis setup
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setupAxis(JNIEnv* env, jclass, jint axis, jstring label, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    implot_setup_axis(axis, label != nullptr && !label_str.empty() ? label_str.c_str() : nullptr, flags);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setupAxisFormat(JNIEnv* env, jclass, jint axis, jstring fmt) {
    std::string fmt_str = jstring_to_string(env, fmt);
    implot_setup_axis_format(axis, fmt_str.c_str());
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setupAxisLimitsConstraints(JNIEnv*, jclass, jint axis, jdouble v_min, jdouble v_max) {
    implot_setup_axis_limits_constraints(axis, v_min, v_max);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setupAxisZoomConstraints(JNIEnv*, jclass, jint axis, jdouble z_min, jdouble z_max) {
    implot_setup_axis_zoom_constraints(axis, z_min, z_max);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setupAxisLinks(JNIEnv* env, jclass, jint axis, jdoubleArray link_min_arr, jdoubleArray link_max_arr) {
    jdouble* link_min = link_min_arr != nullptr ? env->GetDoubleArrayElements(link_min_arr, nullptr) : nullptr;
    jdouble* link_max = link_max_arr != nullptr ? env->GetDoubleArrayElements(link_max_arr, nullptr) : nullptr;
    implot_setup_axis_links(axis, link_min, link_max);
    if (link_min_arr != nullptr) {
        env->ReleaseDoubleArrayElements(link_min_arr, link_min, JNI_ABORT);
    }
    if (link_max_arr != nullptr) {
        env->ReleaseDoubleArrayElements(link_max_arr, link_max, JNI_ABORT);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setupAxisScale(JNIEnv*, jclass, jint axis, jint scale) {
    implot_setup_axis_scale(axis, scale);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setupAxisTicks(JNIEnv* env, jclass, jint axis, jdoubleArray values_arr, jobjectArray labels, jint tick_count, jboolean keep_default) {
    std::vector<std::string> strings;
    std::vector<const char*> cstrings;
    if (labels != nullptr) {
        get_string_array(env, labels, strings, cstrings);
    }
    jdouble* values = env->GetDoubleArrayElements(values_arr, nullptr);
    implot_setup_axis_ticks(axis, values, tick_count, labels != nullptr ? cstrings.data() : nullptr, keep_default == JNI_TRUE);
    env->ReleaseDoubleArrayElements(values_arr, values, JNI_ABORT);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setupMouseText(JNIEnv*, jclass, jint location, jint flags) {
    implot_setup_mouse_text(location, flags);
}

// =========================================================================
// ImPlot: subplots
// =========================================================================

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_beginSubplots(JNIEnv* env, jclass, jstring title_id, jint rows, jint cols, jfloat size_x, jfloat size_y, jint flags) {
    std::string title = jstring_to_string(env, title_id);
    imgui_vec2 size;
    size.x = size_x;
    size.y = size_y;
    return implot_begin_subplots(title.c_str(), rows, cols, size, flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_endSubplots(JNIEnv*, jclass) {
    implot_end_subplots();
}

// =========================================================================
// ImPlot: drag tools / annotations / tags
// =========================================================================

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_dragPoint(JNIEnv* env, jclass, jint id, jdoubleArray x_arr, jdoubleArray y_arr, jfloat col_r, jfloat col_g, jfloat col_b, jfloat col_a, jfloat size, jint flags) {
    jdouble* x = env->GetDoubleArrayElements(x_arr, nullptr);
    jdouble* y = env->GetDoubleArrayElements(y_arr, nullptr);
    imgui_vec4 col;
    col.x = col_r;
    col.y = col_g;
    col.z = col_b;
    col.w = col_a;
    jboolean changed = implot_drag_point(id, x, y, col, size, flags) ? JNI_TRUE : JNI_FALSE;
    env->ReleaseDoubleArrayElements(x_arr, x, 0);
    env->ReleaseDoubleArrayElements(y_arr, y, 0);
    return changed;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_dragLineX(JNIEnv* env, jclass, jint id, jdoubleArray x_arr, jfloat col_r, jfloat col_g, jfloat col_b, jfloat col_a, jfloat thickness, jint flags) {
    jdouble* x = env->GetDoubleArrayElements(x_arr, nullptr);
    imgui_vec4 col;
    col.x = col_r;
    col.y = col_g;
    col.z = col_b;
    col.w = col_a;
    jboolean changed = implot_drag_line_x(id, x, col, thickness, flags) ? JNI_TRUE : JNI_FALSE;
    env->ReleaseDoubleArrayElements(x_arr, x, 0);
    return changed;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_dragLineY(JNIEnv* env, jclass, jint id, jdoubleArray y_arr, jfloat col_r, jfloat col_g, jfloat col_b, jfloat col_a, jfloat thickness, jint flags) {
    jdouble* y = env->GetDoubleArrayElements(y_arr, nullptr);
    imgui_vec4 col;
    col.x = col_r;
    col.y = col_g;
    col.z = col_b;
    col.w = col_a;
    jboolean changed = implot_drag_line_y(id, y, col, thickness, flags) ? JNI_TRUE : JNI_FALSE;
    env->ReleaseDoubleArrayElements(y_arr, y, 0);
    return changed;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_dragRect(JNIEnv* env, jclass, jint id, jdoubleArray x_min_arr, jdoubleArray y_min_arr, jdoubleArray x_max_arr, jdoubleArray y_max_arr, jfloat col_r, jfloat col_g, jfloat col_b, jfloat col_a, jint flags) {
    jdouble* x_min = env->GetDoubleArrayElements(x_min_arr, nullptr);
    jdouble* y_min = env->GetDoubleArrayElements(y_min_arr, nullptr);
    jdouble* x_max = env->GetDoubleArrayElements(x_max_arr, nullptr);
    jdouble* y_max = env->GetDoubleArrayElements(y_max_arr, nullptr);
    imgui_vec4 col;
    col.x = col_r;
    col.y = col_g;
    col.z = col_b;
    col.w = col_a;
    jboolean changed = implot_drag_rect(id, x_min, y_min, x_max, y_max, col, flags) ? JNI_TRUE : JNI_FALSE;
    env->ReleaseDoubleArrayElements(x_min_arr, x_min, 0);
    env->ReleaseDoubleArrayElements(y_min_arr, y_min, 0);
    env->ReleaseDoubleArrayElements(x_max_arr, x_max, 0);
    env->ReleaseDoubleArrayElements(y_max_arr, y_max, 0);
    return changed;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_annotation(JNIEnv* env, jclass, jdouble x, jdouble y, jfloat col_r, jfloat col_g, jfloat col_b, jfloat col_a, jfloat pix_x, jfloat pix_y, jboolean clamp, jboolean round, jstring fmt) {
    imgui_vec4 col;
    col.x = col_r;
    col.y = col_g;
    col.z = col_b;
    col.w = col_a;
    imgui_vec2 pix_offset;
    pix_offset.x = pix_x;
    pix_offset.y = pix_y;
    std::string fmt_str = jstring_to_string(env, fmt);
    implot_annotation(x, y, col, pix_offset, clamp == JNI_TRUE, round == JNI_TRUE, fmt != nullptr ? fmt_str.c_str() : nullptr);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_tagX(JNIEnv* env, jclass, jdouble x, jfloat col_r, jfloat col_g, jfloat col_b, jfloat col_a, jboolean round, jstring fmt) {
    imgui_vec4 col;
    col.x = col_r;
    col.y = col_g;
    col.z = col_b;
    col.w = col_a;
    std::string fmt_str = jstring_to_string(env, fmt);
    implot_tag_x(x, col, round == JNI_TRUE, fmt != nullptr ? fmt_str.c_str() : nullptr);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_tagY(JNIEnv* env, jclass, jdouble y, jfloat col_r, jfloat col_g, jfloat col_b, jfloat col_a, jboolean round, jstring fmt) {
    imgui_vec4 col;
    col.x = col_r;
    col.y = col_g;
    col.z = col_b;
    col.w = col_a;
    std::string fmt_str = jstring_to_string(env, fmt);
    implot_tag_y(y, col, round == JNI_TRUE, fmt != nullptr ? fmt_str.c_str() : nullptr);
}

// =========================================================================
// ImPlot: queries / coordinates
// =========================================================================

extern "C" JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_getPlotLimits(JNIEnv* env, jclass) {
    double x_min, y_min, x_max, y_max;
    implot_get_plot_limits(&x_min, &y_min, &x_max, &y_max);
    jdoubleArray out = env->NewDoubleArray(4);
    jdouble values[4] = {x_min, y_min, x_max, y_max};
    env->SetDoubleArrayRegion(out, 0, 4, values);
    return out;
}

extern "C" JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_getPlotMousePos(JNIEnv* env, jclass) {
    double x, y;
    implot_get_plot_mouse_pos(&x, &y);
    jdoubleArray out = env->NewDoubleArray(2);
    jdouble values[2] = {x, y};
    env->SetDoubleArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_pixelsToPlot(JNIEnv* env, jclass, jfloat pix_x, jfloat pix_y) {
    double x, y;
    implot_pixels_to_plot(pix_x, pix_y, &x, &y);
    jdoubleArray out = env->NewDoubleArray(2);
    jdouble values[2] = {x, y};
    env->SetDoubleArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_plotToPixels(JNIEnv* env, jclass, jdouble x, jdouble y) {
    imgui_vec2 v = implot_plot_to_pixels(x, y);
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_getPlotDrawList(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(implot_get_plot_draw_list());
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_nextColormapColor(JNIEnv* env, jclass) {
    imgui_vec4 v = implot_next_colormap_color();
    jfloatArray out = env->NewFloatArray(4);
    jfloat values[4] = {v.x, v.y, v.z, v.w};
    env->SetFloatArrayRegion(out, 0, 4, values);
    return out;
}

// =========================================================================
// ImPlot: colormap
// =========================================================================

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_getColormapCount(JNIEnv*, jclass) {
    return implot_get_colormap_count();
}

extern "C" JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_getColormapName(JNIEnv* env, jclass, jint idx) {
    const char* name = implot_get_colormap_name(idx);
    return name != nullptr ? env->NewStringUTF(name) : nullptr;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_getColormapColor(JNIEnv* env, jclass, jint idx, jint cmap) {
    imgui_vec4 v = implot_get_colormap_color(idx, cmap);
    jfloatArray out = env->NewFloatArray(4);
    jfloat values[4] = {v.x, v.y, v.z, v.w};
    env->SetFloatArrayRegion(out, 0, 4, values);
    return out;
}

extern "C" JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_sampleColormap(JNIEnv* env, jclass, jfloat t, jint cmap) {
    imgui_vec4 v = implot_sample_colormap(t, cmap);
    jfloatArray out = env->NewFloatArray(4);
    jfloat values[4] = {v.x, v.y, v.z, v.w};
    env->SetFloatArrayRegion(out, 0, 4, values);
    return out;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_colormapButton(JNIEnv* env, jclass, jstring label, jfloat size_x, jfloat size_y, jint cmap) {
    std::string label_str = jstring_to_string(env, label);
    imgui_vec2 size;
    size.x = size_x;
    size.y = size_y;
    return implot_colormap_button(label_str.c_str(), size, cmap) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_colormapScale(JNIEnv* env, jclass, jstring label, jdouble scale_min, jdouble scale_max, jfloat size_x, jfloat size_y, jstring fmt, jint flags, jint cmap) {
    std::string label_str = jstring_to_string(env, label);
    std::string fmt_str = jstring_to_string(env, fmt);
    imgui_vec2 size;
    size.x = size_x;
    size.y = size_y;
    implot_colormap_scale(label_str.c_str(), scale_min, scale_max, size, fmt_str.empty() ? nullptr : fmt_str.c_str(), flags, cmap);
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_colormapSlider(JNIEnv* env, jclass, jstring label, jfloatArray t_arr, jfloatArray out_arr, jstring fmt, jint cmap) {
    std::string label_str = jstring_to_string(env, label);
    std::string fmt_str = jstring_to_string(env, fmt);
    jfloat* t = env->GetFloatArrayElements(t_arr, nullptr);
    jfloat* out = out_arr != nullptr ? env->GetFloatArrayElements(out_arr, nullptr) : nullptr;
    imgui_vec4 out_val;
    jboolean changed = implot_colormap_slider(label_str.c_str(), t, out != nullptr ? &out_val : nullptr, fmt_str.empty() ? nullptr : fmt_str.c_str(), cmap) ? JNI_TRUE : JNI_FALSE;
    if (out != nullptr) {
        out[0] = out_val.x;
        out[1] = out_val.y;
        out[2] = out_val.z;
        out[3] = out_val.w;
    }
    env->ReleaseFloatArrayElements(t_arr, t, 0);
    if (out_arr != nullptr) {
        env->ReleaseFloatArrayElements(out_arr, out, 0);
    }
    return changed;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_colormapIcon(JNIEnv*, jclass, jint cmap) {
    implot_colormap_icon(cmap);
}

// =========================================================================
// ImPlot: color maps (misc)
// =========================================================================

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_addColormap(JNIEnv* env, jclass, jstring name, jfloatArray cols_arr) {
    std::string name_str = jstring_to_string(env, name);
    jfloat* cols = env->GetFloatArrayElements(cols_arr, nullptr);
    jsize size = env->GetArrayLength(cols_arr);
    jint result = implot_add_colormap(name_str.c_str(), cols, (int)(size / 3));
    env->ReleaseFloatArrayElements(cols_arr, cols, JNI_ABORT);
    return result;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_itemIcon(JNIEnv*, jclass, jint col) {
    implot_item_icon((uint32_t)col);
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_getLastItemColor(JNIEnv*, jclass) {
    return (jint)implot_get_last_item_color();
}

// =========================================================================
// ImPlot: plot utils
// =========================================================================

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setAxis(JNIEnv*, jclass, jint axis) {
    implot_set_axis(axis);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_setAxes(JNIEnv*, jclass, jint x_axis, jint y_axis) {
    implot_set_axes(x_axis, y_axis);
}

extern "C" JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_getPlotSelection(JNIEnv* env, jclass) {
    double x_min, y_min, x_max, y_max;
    implot_get_plot_selection(&x_min, &y_min, &x_max, &y_max);
    jdoubleArray out = env->NewDoubleArray(4);
    jdouble values[4] = {x_min, y_min, x_max, y_max};
    env->SetDoubleArrayRegion(out, 0, 4, values);
    return out;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_pushPlotClipRect(JNIEnv*, jclass, jfloat expand) {
    implot_push_plot_clip_rect(expand);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_popPlotClipRect(JNIEnv*, jclass) {
    implot_pop_plot_clip_rect();
}

// =========================================================================
// ImPlot: drag and drop
// =========================================================================

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_beginDragDropSourcePlot(JNIEnv*, jclass, jint flags) {
    return implot_begin_drag_drop_source_plot(flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_beginDragDropSourceAxis(JNIEnv*, jclass, jint axis, jint flags) {
    return implot_begin_drag_drop_source_axis(axis, flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_beginDragDropSourceItem(JNIEnv* env, jclass, jstring label_id, jint flags) {
    std::string label = jstring_to_string(env, label_id);
    return implot_begin_drag_drop_source_item(label.c_str(), flags) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_endDragDropSource(JNIEnv*, jclass) {
    implot_end_drag_drop_source();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_beginDragDropTargetPlot(JNIEnv*, jclass) {
    return implot_begin_drag_drop_target_plot() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_beginDragDropTargetAxis(JNIEnv*, jclass, jint axis) {
    return implot_begin_drag_drop_target_axis(axis) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_beginDragDropTargetLegend(JNIEnv*, jclass) {
    return implot_begin_drag_drop_target_legend() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_endDragDropTarget(JNIEnv*, jclass) {
    implot_end_drag_drop_target();
}

// =========================================================================
// ImPlot: legend popup
// =========================================================================

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_beginLegendPopup(JNIEnv* env, jclass, jstring label_id, jint mouse_button) {
    std::string label = jstring_to_string(env, label_id);
    return implot_begin_legend_popup(label.c_str(), mouse_button) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_endLegendPopup(JNIEnv*, jclass) {
    implot_end_legend_popup();
}

// =========================================================================
// ImPlot: input mapping / tools
// =========================================================================

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_getInputMap(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(implot_get_input_map());
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_showInputMapSelector(JNIEnv* env, jclass, jstring label) {
    std::string label_str = jstring_to_string(env, label);
    return implot_show_input_map_selector(label_str.c_str()) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_showMetricsWindow(JNIEnv* env, jclass, jbooleanArray p_open) {
    jboolean* elems = p_open != nullptr ? env->GetBooleanArrayElements(p_open, nullptr) : nullptr;
    implot_show_metrics_window(reinterpret_cast<bool*>(elems));
    if (elems != nullptr) {
        env->ReleaseBooleanArrayElements(p_open, elems, 0);
    }
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_showStyleEditor(JNIEnv*, jclass) {
    implot_show_style_editor();
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_showStyleSelector(JNIEnv* env, jclass, jstring label) {
    std::string label_str = jstring_to_string(env, label);
    return implot_show_style_selector(label_str.c_str()) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot_Jni_showColormapSelector(JNIEnv* env, jclass, jstring label) {
    std::string label_str = jstring_to_string(env, label);
    return implot_show_colormap_selector(label_str.c_str()) ? JNI_TRUE : JNI_FALSE;
}
