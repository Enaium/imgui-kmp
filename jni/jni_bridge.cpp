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
#include <cstdlib>
#include <string>
#include <unordered_map>
#include <vector>

#include "imgui.h"
#include "imgui_internal.h"
#include "imgui_c.h"
#include "implot_c.h"
#include "implot3d_c.h"
#include "text_editor_c.h"
#include "text_editor_extras_c.h"
#include "text_editor_events_c.h"
#include "markdown_c.h"

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

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setCurrentContext(JNIEnv*, jclass, jlong ctx) {
    imgui_set_current_context(reinterpret_cast<imgui_context*>(ctx));
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

// =========================================================================
// Docking
// =========================================================================

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_dockSpace(JNIEnv*, jclass, jint id, jfloat size_x, jfloat size_y, jint flags) {
    return (jint)imgui_dock_space((int)id, size_x, size_y, (int)flags);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_setNextWindowDockID(JNIEnv*, jclass, jint dock_id, jint cond) {
    imgui_set_next_window_dock_id((int)dock_id, (int)cond);
}

extern "C" JNIEXPORT jint JNICALL Java_cn_enaium_imgui_Jni_dockBuilderAddNode(JNIEnv*, jclass, jint node_id, jint flags) {
    return (jint)imgui_dock_builder_add_node((int)node_id, (int)flags);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_dockBuilderRemoveNode(JNIEnv*, jclass, jint node_id) {
    imgui_dock_builder_remove_node((int)node_id);
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_dockBuilderSplitNode(JNIEnv*, jclass, jint node_id, jint split_dir, jfloat ratio) {
    int id_at_dir = 0;
    int id_at_opposite_dir = 0;
    imgui_dock_builder_split_node((int)node_id, (int)split_dir, ratio, &id_at_dir, &id_at_opposite_dir);
    return (jlong)(unsigned int)id_at_dir | ((jlong)(unsigned int)id_at_opposite_dir << 32);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_dockBuilderDockWindow(JNIEnv* env, jclass, jstring window_name, jint node_id) {
    std::string name = jstring_to_string(env, window_name);
    imgui_dock_builder_dock_window(name.c_str(), (int)node_id);
}

extern "C" JNIEXPORT void JNICALL Java_cn_enaium_imgui_Jni_dockBuilderFinish(JNIEnv*, jclass, jint node_id) {
    imgui_dock_builder_finish((int)node_id);
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
static const char* jstring_or_null(JNIEnv* env, jstring s, std::string& out) {
    if (s == nullptr) return nullptr;
    out = jstring_to_string(env, s);
    return out.c_str();
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_fontsAddFontDefaultConfig(
    JNIEnv* env, jclass, jlong atlas, jstring name, jboolean merge_mode, jboolean pixel_snap_h,
    jint oversample_h, jint oversample_v, jfloat size_pixels,
    jfloat glyph_offset_x, jfloat glyph_offset_y, jfloat glyph_min_advance_x, jfloat glyph_max_advance_x,
    jfloat rasterizer_multiply, jfloat rasterizer_density, jfloat extra_size_scale) {
    std::string name_str;
    return reinterpret_cast<jlong>(imgui_font_atlas_add_font_default_cfg(
        reinterpret_cast<imgui_font_atlas*>(atlas),
        jstring_or_null(env, name, name_str), merge_mode == JNI_TRUE, pixel_snap_h == JNI_TRUE,
        oversample_h, oversample_v, size_pixels, glyph_offset_x, glyph_offset_y,
        glyph_min_advance_x, glyph_max_advance_x, rasterizer_multiply, rasterizer_density, extra_size_scale));
}

extern "C" JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_Jni_fontsAddFontFromFileTTFConfig(
    JNIEnv* env, jclass, jlong atlas, jstring path, jstring name, jboolean merge_mode, jboolean pixel_snap_h,
    jint oversample_h, jint oversample_v, jfloat size_pixels,
    jfloat glyph_offset_x, jfloat glyph_offset_y, jfloat glyph_min_advance_x, jfloat glyph_max_advance_x,
    jfloat rasterizer_multiply, jfloat rasterizer_density, jfloat extra_size_scale) {
    std::string path_str = jstring_to_string(env, path);
    std::string name_str;
    return reinterpret_cast<jlong>(imgui_font_atlas_add_font_from_file_ttf_cfg(
        reinterpret_cast<imgui_font_atlas*>(atlas), path_str.c_str(),
        jstring_or_null(env, name, name_str), merge_mode == JNI_TRUE, pixel_snap_h == JNI_TRUE,
        oversample_h, oversample_v, size_pixels, glyph_offset_x, glyph_offset_y,
        glyph_min_advance_x, glyph_max_advance_x, rasterizer_multiply, rasterizer_density, extra_size_scale));
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

// =========================================================================
// Node editor (ax::NodeEditor)
// =========================================================================

#include "node_editor_c.h"
#include "file_dialog_c.h"
#include "memory_editor_c.h"
#include "multi_context_compositor_c.h"
#include "threaded_rendering_c.h"

static std::vector<int64_t> jlong_to_id_vector(JNIEnv* env, jlongArray arr) {
    std::vector<int64_t> out;
    if (arr != nullptr) {
        jsize len = env->GetArrayLength(arr);
        jlong* elems = env->GetLongArrayElements(arr, nullptr);
        out.assign(elems, elems + len);
        env->ReleaseLongArrayElements(arr, elems, JNI_ABORT);
    }
    return out;
}

static void write_id_back(JNIEnv* env, jlongArray arr, int64_t value) {
    if (arr == nullptr) {
        return;
    }
    jlong elem = static_cast<jlong>(value);
    env->SetLongArrayRegion(arr, 0, 1, &elem);
}

static void set_float_array(JNIEnv* env, jfloatArray arr, const float* values, int count) {
    if (arr != nullptr) {
        env->SetFloatArrayRegion(arr, 0, count, values);
    }
}

extern "C" {

// ---- Context ----

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_createEditor(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(ne_create_editor());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_destroyEditor(JNIEnv*, jclass, jlong ctx) {
    ne_destroy_editor(reinterpret_cast<ne_context*>(ctx));
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getCurrentEditor(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(ne_get_current_editor());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_setCurrentEditor(JNIEnv*, jclass, jlong ctx) {
    ne_set_current_editor(reinterpret_cast<ne_context*>(ctx));
}

// ---- Begin/End ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_begin(JNIEnv* env, jclass, jstring id, jfloat size_x, jfloat size_y) {
    std::string s = jstring_to_string(env, id);
    ne_begin(s.c_str(), size_x, size_y);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_end(JNIEnv*, jclass) {
    ne_end();
}

// ---- Nodes and pins ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_beginNode(JNIEnv*, jclass, jlong id) {
    ne_begin_node(static_cast<int64_t>(id));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_beginPin(JNIEnv*, jclass, jlong id, jint kind) {
    ne_begin_pin(static_cast<int64_t>(id), kind);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_pinRect(JNIEnv*, jclass, jfloat a_x, jfloat a_y, jfloat b_x, jfloat b_y) {
    ne_pin_rect(a_x, a_y, b_x, b_y);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_pinPivotRect(JNIEnv*, jclass, jfloat a_x, jfloat a_y, jfloat b_x, jfloat b_y) {
    ne_pin_pivot_rect(a_x, a_y, b_x, b_y);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_pinPivotSize(JNIEnv*, jclass, jfloat w, jfloat h) {
    ne_pin_pivot_size(w, h);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_pinPivotScale(JNIEnv*, jclass, jfloat sx, jfloat sy) {
    ne_pin_pivot_scale(sx, sy);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_pinPivotAlignment(JNIEnv*, jclass, jfloat ax, jfloat ay) {
    ne_pin_pivot_alignment(ax, ay);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_endPin(JNIEnv*, jclass) {
    ne_end_pin();
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_group(JNIEnv*, jclass, jfloat size_x, jfloat size_y) {
    ne_group(size_x, size_y);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_endNode(JNIEnv*, jclass) {
    ne_end_node();
}

// ---- Group hints ----

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_beginGroupHint(JNIEnv*, jclass, jlong node_id) {
    return ne_begin_group_hint(static_cast<int64_t>(node_id)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getGroupMin(JNIEnv* env, jclass) {
    imgui_vec2 v = ne_get_group_min();
    float vals[2] = {v.x, v.y};
    jfloatArray out = env->NewFloatArray(2);
    set_float_array(env, out, vals, 2);
    return out;
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getGroupMax(JNIEnv* env, jclass) {
    imgui_vec2 v = ne_get_group_max();
    float vals[2] = {v.x, v.y};
    jfloatArray out = env->NewFloatArray(2);
    set_float_array(env, out, vals, 2);
    return out;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_endGroupHint(JNIEnv*, jclass) {
    ne_end_group_hint();
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getNodeBackgroundDrawList(JNIEnv*, jclass, jlong node_id) {
    return reinterpret_cast<jlong>(ne_get_node_background_draw_list(static_cast<int64_t>(node_id)));
}

// ---- Links ----

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_link(JNIEnv*, jclass, jlong id, jlong start_pin_id, jlong end_pin_id, jfloat r, jfloat g, jfloat b, jfloat a, jfloat thickness) {
    imgui_vec4 color{r, g, b, a};
    return ne_link(static_cast<int64_t>(id), static_cast<int64_t>(start_pin_id), static_cast<int64_t>(end_pin_id), color, thickness) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_flow(JNIEnv*, jclass, jlong link_id, jint direction) {
    ne_flow(static_cast<int64_t>(link_id), direction);
}

// ---- Create new link / node ----

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_beginCreate(JNIEnv*, jclass, jfloat r, jfloat g, jfloat b, jfloat a, jfloat thickness) {
    imgui_vec4 color{r, g, b, a};
    return ne_begin_create(color, thickness) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_queryNewLink(JNIEnv* env, jclass, jlongArray start_pin_id, jlongArray end_pin_id, jfloat r, jfloat g, jfloat b, jfloat a, jfloat thickness) {
    int64_t start = 0;
    int64_t end = 0;
    imgui_vec4 color{r, g, b, a};
    bool ok = ne_query_new_link_styled(&start, &end, color, thickness);
    write_id_back(env, start_pin_id, start);
    write_id_back(env, end_pin_id, end);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_queryNewNode(JNIEnv* env, jclass, jlongArray pin_id, jfloat r, jfloat g, jfloat b, jfloat a, jfloat thickness) {
    int64_t pin = 0;
    imgui_vec4 color{r, g, b, a};
    bool ok = ne_query_new_node_styled(&pin, color, thickness);
    write_id_back(env, pin_id, pin);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_acceptNewItem(JNIEnv*, jclass, jfloat r, jfloat g, jfloat b, jfloat a, jfloat thickness) {
    imgui_vec4 color{r, g, b, a};
    return ne_accept_new_item_ex(color, thickness) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_rejectNewItem(JNIEnv*, jclass, jfloat r, jfloat g, jfloat b, jfloat a, jfloat thickness) {
    imgui_vec4 color{r, g, b, a};
    ne_reject_new_item_ex(color, thickness);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_endCreate(JNIEnv*, jclass) {
    ne_end_create();
}

// ---- Delete nodes / links ----

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_beginDelete(JNIEnv*, jclass) {
    return ne_begin_delete() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_queryDeletedLink(JNIEnv* env, jclass, jlongArray link_id, jlongArray start_pin_id, jlongArray end_pin_id) {
    int64_t link = 0;
    int64_t start = 0;
    int64_t end = 0;
    bool ok = ne_query_deleted_link(&link, &start, &end);
    write_id_back(env, link_id, link);
    // Optional pin arrays: only written when the query succeeded and the
    // caller provided them.
    if (ok) {
        if (start_pin_id != nullptr && env->GetArrayLength(start_pin_id) > 0) {
            write_id_back(env, start_pin_id, start);
        }
        if (end_pin_id != nullptr && env->GetArrayLength(end_pin_id) > 0) {
            write_id_back(env, end_pin_id, end);
        }
    }
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_queryDeletedNode(JNIEnv* env, jclass, jlongArray node_id) {
    int64_t node = 0;
    bool ok = ne_query_deleted_node(&node);
    write_id_back(env, node_id, node);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_acceptDeletedItem(JNIEnv*, jclass, jboolean delete_dependencies) {
    ne_accept_deleted_item(delete_dependencies == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_rejectDeletedItem(JNIEnv*, jclass) {
    ne_reject_deleted_item();
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_endDelete(JNIEnv*, jclass) {
    ne_end_delete();
}

// ---- Node / group geometry ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_setNodePosition(JNIEnv*, jclass, jlong node_id, jfloat x, jfloat y) {
    ne_set_node_position(static_cast<int64_t>(node_id), x, y);
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getNodePosition(JNIEnv* env, jclass, jlong node_id) {
    imgui_vec2 v = ne_get_node_position(static_cast<int64_t>(node_id));
    float vals[2] = {v.x, v.y};
    jfloatArray out = env->NewFloatArray(2);
    set_float_array(env, out, vals, 2);
    return out;
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getNodeSize(JNIEnv* env, jclass, jlong node_id) {
    imgui_vec2 v = ne_get_node_size(static_cast<int64_t>(node_id));
    float vals[2] = {v.x, v.y};
    jfloatArray out = env->NewFloatArray(2);
    set_float_array(env, out, vals, 2);
    return out;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_setGroupSize(JNIEnv*, jclass, jlong node_id, jfloat x, jfloat y) {
    ne_set_group_size(static_cast<int64_t>(node_id), x, y);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_centerNodeOnScreen(JNIEnv*, jclass, jlong node_id) {
    ne_center_node_on_screen(static_cast<int64_t>(node_id));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_setNodeZPosition(JNIEnv*, jclass, jlong node_id, jfloat z) {
    ne_set_node_z_position(static_cast<int64_t>(node_id), z);
}

JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getNodeZPosition(JNIEnv*, jclass, jlong node_id) {
    return ne_get_node_z_position(static_cast<int64_t>(node_id));
}

// ---- Suspend / resume ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_suspend(JNIEnv*, jclass) {
    ne_suspend();
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_resume(JNIEnv*, jclass) {
    ne_resume();
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_isSuspended(JNIEnv*, jclass) {
    return ne_is_suspended() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_isActive(JNIEnv*, jclass) {
    return ne_is_active() ? JNI_TRUE : JNI_FALSE;
}

// ---- Selection ----

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_hasSelectionChanged(JNIEnv*, jclass) {
    return ne_has_selection_changed() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getSelectedObjectCount(JNIEnv*, jclass) {
    return ne_get_selected_object_count();
}

static jlongArray fill_id_array(JNIEnv* env, int capacity, int (*fill)(int64_t*, int, int*)) {
    if (capacity <= 0) {
        return env->NewLongArray(0);
    }
    std::vector<int64_t> ids(capacity, 0);
    int count = 0;
    fill(ids.data(), capacity, &count);
    if (count < 0) {
        count = 0;
    }
    if (count > capacity) {
        count = capacity;
    }
    jlongArray out = env->NewLongArray(count);
    if (count > 0) {
        env->SetLongArrayRegion(out, 0, count, reinterpret_cast<const jlong*>(ids.data()));
    }
    return out;
}

JNIEXPORT jlongArray JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getSelectedNodes(JNIEnv* env, jclass, jint size) {
    return fill_id_array(env, size, +[](int64_t* ids, int cap, int* count) -> int {
        ne_get_selected_nodes(ids, cap, count);
        return *count;
    });
}

JNIEXPORT jlongArray JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getSelectedLinks(JNIEnv* env, jclass, jint size) {
    return fill_id_array(env, size, +[](int64_t* ids, int cap, int* count) -> int {
        ne_get_selected_links(ids, cap, count);
        return *count;
    });
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_isNodeSelected(JNIEnv*, jclass, jlong node_id) {
    return ne_is_node_selected(static_cast<int64_t>(node_id)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_isLinkSelected(JNIEnv*, jclass, jlong link_id) {
    return ne_is_link_selected(static_cast<int64_t>(link_id)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_clearSelection(JNIEnv*, jclass) {
    ne_clear_selection();
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_selectNode(JNIEnv*, jclass, jlong node_id, jboolean append) {
    ne_select_node(static_cast<int64_t>(node_id), append == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_selectLink(JNIEnv*, jclass, jlong link_id, jboolean append) {
    ne_select_link(static_cast<int64_t>(link_id), append == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_deselectNode(JNIEnv*, jclass, jlong node_id) {
    ne_deselect_node(static_cast<int64_t>(node_id));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_deselectLink(JNIEnv*, jclass, jlong link_id) {
    ne_deselect_link(static_cast<int64_t>(link_id));
}

// ---- Deletion requests ----

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_deleteNode(JNIEnv*, jclass, jlong node_id) {
    return ne_delete_node(static_cast<int64_t>(node_id)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_deleteLink(JNIEnv*, jclass, jlong link_id) {
    return ne_delete_link(static_cast<int64_t>(link_id)) ? JNI_TRUE : JNI_FALSE;
}

// ---- Links queries ----

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_hasAnyLinks(JNIEnv*, jclass, jlong id) {
    return ne_has_any_links(static_cast<int64_t>(id)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_breakLinks(JNIEnv*, jclass, jlong id) {
    return ne_break_links(static_cast<int64_t>(id));
}

// ---- Navigation ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_navigateToContent(JNIEnv*, jclass, jfloat duration) {
    ne_navigate_to_content(duration);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_navigateToSelection(JNIEnv*, jclass, jboolean zoom_in, jfloat duration) {
    ne_navigate_to_selection(zoom_in == JNI_TRUE, duration);
}

// ---- Context menus ----

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_showNodeContextMenu(JNIEnv* env, jclass, jlongArray node_id) {
    auto ids = jlong_to_id_vector(env, node_id);
    if (ids.empty()) {
        return JNI_FALSE;
    }
    bool ok = ne_show_node_context_menu(&ids[0]);
    write_id_back(env, node_id, ids[0]);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_showPinContextMenu(JNIEnv* env, jclass, jlongArray pin_id) {
    auto ids = jlong_to_id_vector(env, pin_id);
    if (ids.empty()) {
        return JNI_FALSE;
    }
    bool ok = ne_show_pin_context_menu(&ids[0]);
    write_id_back(env, pin_id, ids[0]);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_showLinkContextMenu(JNIEnv* env, jclass, jlongArray link_id) {
    auto ids = jlong_to_id_vector(env, link_id);
    if (ids.empty()) {
        return JNI_FALSE;
    }
    bool ok = ne_show_link_context_menu(&ids[0]);
    write_id_back(env, link_id, ids[0]);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_showBackgroundContextMenu(JNIEnv*, jclass) {
    return ne_show_background_context_menu() ? JNI_TRUE : JNI_FALSE;
}

// ---- Shortcuts ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_enableShortcuts(JNIEnv*, jclass, jboolean enable) {
    ne_enable_shortcuts(enable == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_areShortcutsEnabled(JNIEnv*, jclass) {
    return ne_are_shortcuts_enabled() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_beginShortcut(JNIEnv*, jclass) {
    return ne_begin_shortcut() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_acceptCut(JNIEnv*, jclass) {
    return ne_accept_cut() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_acceptCopy(JNIEnv*, jclass) {
    return ne_accept_copy() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_acceptPaste(JNIEnv*, jclass) {
    return ne_accept_paste() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_acceptDuplicate(JNIEnv*, jclass) {
    return ne_accept_duplicate() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_acceptCreateNode(JNIEnv*, jclass) {
    return ne_accept_create_node() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getActionContextSize(JNIEnv*, jclass) {
    return ne_get_action_context_size();
}

JNIEXPORT jlongArray JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getActionContextNodes(JNIEnv* env, jclass, jint size) {
    return fill_id_array(env, size, +[](int64_t* ids, int cap, int* count) -> int {
        ne_get_action_context_nodes(ids, cap, count);
        return *count;
    });
}

JNIEXPORT jlongArray JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getActionContextLinks(JNIEnv* env, jclass, jint size) {
    return fill_id_array(env, size, +[](int64_t* ids, int cap, int* count) -> int {
        ne_get_action_context_links(ids, cap, count);
        return *count;
    });
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_endShortcut(JNIEnv*, jclass) {
    ne_end_shortcut();
}

JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getCurrentZoom(JNIEnv*, jclass) {
    return ne_get_current_zoom();
}

// ---- Hover / click queries ----

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getHoveredNode(JNIEnv*, jclass) {
    return static_cast<jlong>(ne_get_hovered_node());
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getHoveredPin(JNIEnv*, jclass) {
    return static_cast<jlong>(ne_get_hovered_pin());
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getHoveredLink(JNIEnv*, jclass) {
    return static_cast<jlong>(ne_get_hovered_link());
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getDoubleClickedNode(JNIEnv*, jclass) {
    return static_cast<jlong>(ne_get_double_clicked_node());
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getDoubleClickedPin(JNIEnv*, jclass) {
    return static_cast<jlong>(ne_get_double_clicked_pin());
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getDoubleClickedLink(JNIEnv*, jclass) {
    return static_cast<jlong>(ne_get_double_clicked_link());
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_isBackgroundClicked(JNIEnv*, jclass) {
    return ne_is_background_clicked() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_isBackgroundDoubleClicked(JNIEnv*, jclass) {
    return ne_is_background_double_clicked() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getBackgroundClickButtonIndex(JNIEnv*, jclass) {
    return ne_get_background_click_button_index();
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getBackgroundDoubleClickButtonIndex(JNIEnv*, jclass) {
    return ne_get_background_double_click_button_index();
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getLinkPins(JNIEnv* env, jclass, jlong link_id, jlongArray start_pin_id, jlongArray end_pin_id) {
    int64_t start = 0;
    int64_t end = 0;
    bool ok = ne_get_link_pins(static_cast<int64_t>(link_id), &start, &end);
    if (ok) {
        if (start_pin_id != nullptr && env->GetArrayLength(start_pin_id) > 0) {
            write_id_back(env, start_pin_id, start);
        }
        if (end_pin_id != nullptr && env->GetArrayLength(end_pin_id) > 0) {
            write_id_back(env, end_pin_id, end);
        }
    }
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_pinHadAnyLinks(JNIEnv*, jclass, jlong pin_id) {
    return ne_pin_had_any_links(static_cast<int64_t>(pin_id)) ? JNI_TRUE : JNI_FALSE;
}

// ---- Coordinates ----

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getScreenSize(JNIEnv* env, jclass) {
    imgui_vec2 v = ne_get_screen_size();
    float vals[2] = {v.x, v.y};
    jfloatArray out = env->NewFloatArray(2);
    set_float_array(env, out, vals, 2);
    return out;
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_screenToCanvas(JNIEnv* env, jclass, jfloat x, jfloat y) {
    imgui_vec2 v = ne_screen_to_canvas(x, y);
    float vals[2] = {v.x, v.y};
    jfloatArray out = env->NewFloatArray(2);
    set_float_array(env, out, vals, 2);
    return out;
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_canvasToScreen(JNIEnv* env, jclass, jfloat x, jfloat y) {
    imgui_vec2 v = ne_canvas_to_screen(x, y);
    float vals[2] = {v.x, v.y};
    jfloatArray out = env->NewFloatArray(2);
    set_float_array(env, out, vals, 2);
    return out;
}

// ---- Ordered node ids ----

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getNodeCount(JNIEnv*, jclass) {
    return ne_get_node_count();
}

JNIEXPORT jlongArray JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_getOrderedNodeIds(JNIEnv* env, jclass, jint size) {
    return fill_id_array(env, size, +[](int64_t* ids, int cap, int* count) -> int {
        ne_get_ordered_node_ids(ids, cap, count);
        return *count;
    });
}

// ---- Style ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_pushStyleColor(JNIEnv*, jclass, jint idx, jfloat r, jfloat g, jfloat b, jfloat a) {
    imgui_vec4 color{r, g, b, a};
    ne_push_style_color(idx, color);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_popStyleColor(JNIEnv*, jclass, jint count) {
    ne_pop_style_color(count);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_pushStyleVarFloat(JNIEnv*, jclass, jint idx, jfloat value) {
    ne_push_style_var_float(idx, value);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_pushStyleVarVec2(JNIEnv*, jclass, jint idx, jfloat x, jfloat y) {
    ne_push_style_var_vec2(idx, x, y);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_pushStyleVarVec4(JNIEnv*, jclass, jint idx, jfloat x, jfloat y, jfloat z, jfloat w) {
    ne_push_style_var_vec4(idx, x, y, z, w);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_nodeeditor_Jni_popStyleVar(JNIEnv*, jclass, jint count) {
    ne_pop_style_var(count);
}

// =========================================================================
// File dialog (ImGuiFileDialog)
// =========================================================================

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_create(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(igfd_create());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_destroy(JNIEnv*, jclass, jlong ptr) {
    igfd_destroy(reinterpret_cast<igfd_dialog*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_openDialog(JNIEnv* env, jclass, jlong ptr, jstring key, jstring title, jstring filters, jstring path, jstring file_name, jstring file_path_name, jint count_selection_max, jint flags) {
    std::string key_s = jstring_to_string(env, key);
    std::string title_s = jstring_to_string(env, title);
    std::string filters_s = jstring_to_string(env, filters);
    std::string path_s = jstring_to_string(env, path);
    std::string file_name_s = jstring_to_string(env, file_name);
    std::string file_path_name_s = jstring_to_string(env, file_path_name);
    igfd_open_dialog(
        reinterpret_cast<igfd_dialog*>(ptr),
        key_s.c_str(),
        title_s.c_str(),
        filters_s.empty() ? nullptr : filters_s.c_str(),
        path_s.c_str(),
        file_name_s.c_str(),
        file_path_name_s.c_str(),
        count_selection_max,
        flags);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_displayDialog(JNIEnv* env, jclass, jlong ptr, jstring key, jint window_flags, jfloat min_x, jfloat min_y, jfloat max_x, jfloat max_y) {
    std::string key_s = jstring_to_string(env, key);
    imgui_vec2 min_size{min_x, min_y};
    imgui_vec2 max_size{max_x, max_y};
    return igfd_display_dialog(reinterpret_cast<igfd_dialog*>(ptr), key_s.c_str(), window_flags, min_size, max_size) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_closeDialog(JNIEnv* env, jclass, jlong ptr, jstring key) {
    std::string key_s = jstring_to_string(env, key);
    igfd_close_dialog(reinterpret_cast<igfd_dialog*>(ptr), key_s.c_str());
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_isOk(JNIEnv*, jclass, jlong ptr) {
    return igfd_is_ok(reinterpret_cast<igfd_dialog*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_wasKeyOpenedThisFrame(JNIEnv* env, jclass, jlong ptr, jstring key) {
    std::string key_s = jstring_to_string(env, key);
    return igfd_was_key_opened_this_frame(reinterpret_cast<igfd_dialog*>(ptr), key_s.c_str()) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_wasOpenedThisFrame(JNIEnv*, jclass, jlong ptr) {
    return igfd_was_opened_this_frame(reinterpret_cast<igfd_dialog*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_isKeyOpened(JNIEnv* env, jclass, jlong ptr, jstring key) {
    std::string key_s = jstring_to_string(env, key);
    return igfd_is_key_opened(reinterpret_cast<igfd_dialog*>(ptr), key_s.c_str()) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_isOpened(JNIEnv*, jclass, jlong ptr) {
    return igfd_is_opened(reinterpret_cast<igfd_dialog*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

static jstring string_or_null_to_jstring(JNIEnv* env, char* str) {
    if (str == nullptr) {
        return nullptr;
    }
    jstring out = env->NewStringUTF(str);
    igfd_string_free(str);
    return out;
}

JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_getFilePathName(JNIEnv* env, jclass, jlong ptr, jint mode) {
    return string_or_null_to_jstring(env, igfd_get_file_path_name(reinterpret_cast<igfd_dialog*>(ptr), mode));
}

JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_getCurrentFileName(JNIEnv* env, jclass, jlong ptr, jint mode) {
    return string_or_null_to_jstring(env, igfd_get_current_file_name(reinterpret_cast<igfd_dialog*>(ptr), mode));
}


JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_getCurrentPath(JNIEnv* env, jclass, jlong ptr) {
    return string_or_null_to_jstring(env, igfd_get_current_path(reinterpret_cast<igfd_dialog*>(ptr)));
}

JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_getCurrentFilter(JNIEnv* env, jclass, jlong ptr) {
    return string_or_null_to_jstring(env, igfd_get_current_filter(reinterpret_cast<igfd_dialog*>(ptr)));
}

JNIEXPORT jobjectArray JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_getSelection(JNIEnv* env, jclass, jlong ptr, jint mode) {
    int count = 0;
    char** flat = igfd_get_selection(reinterpret_cast<igfd_dialog*>(ptr), mode, &count);
    if (flat == nullptr || count == 0) {
        igfd_selection_free(flat, count);
        return nullptr;
    }
    jclass string_class = env->FindClass("java/lang/String");
    jobjectArray out = env->NewObjectArray(static_cast<jsize>(count * 2), string_class, nullptr);
    for (int i = 0; i < count * 2; i++) {
        jstring item = flat[i] != nullptr ? env->NewStringUTF(flat[i]) : env->NewStringUTF("");
        env->SetObjectArrayElement(out, i, item);
        env->DeleteLocalRef(item);
    }
    igfd_selection_free(flat, count);
    return out;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_setFileStyle(JNIEnv* env, jclass, jlong ptr, jint flags, jstring filter, jfloat r, jfloat g, jfloat b, jfloat a, jstring icon) {
    std::string filter_s = jstring_to_string(env, filter);
    std::string icon_s = jstring_to_string(env, icon);
    igfd_set_file_style(
        reinterpret_cast<igfd_dialog*>(ptr),
        static_cast<unsigned int>(flags),
        filter_s.empty() ? nullptr : filter_s.c_str(),
        r, g, b, a,
        icon_s.empty() ? nullptr : icon_s.c_str());
}

JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_getFileStyle(JNIEnv* env, jclass, jlong ptr, jint flags, jstring filter, jfloatArray out_color) {
    std::string filter_s = jstring_to_string(env, filter);
    float color[4] = {0.0f, 0.0f, 0.0f, 0.0f};
    char* icon = nullptr;
    if (!igfd_get_file_style(reinterpret_cast<igfd_dialog*>(ptr), static_cast<unsigned int>(flags), filter_s.empty() ? nullptr : filter_s.c_str(), color, &icon)) {
        if (icon != nullptr) {
            igfd_string_free(icon);
        }
        return nullptr;
    }
    if (out_color != nullptr) {
        set_float_array(env, out_color, color, 4);
    }
    jstring icon_str = icon != nullptr ? env->NewStringUTF(icon) : nullptr;
    if (icon != nullptr) {
        igfd_string_free(icon);
    }
    return icon_str;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_filedialog_Jni_clearFilesStyle(JNIEnv*, jclass, jlong ptr) {
    igfd_clear_files_style(reinterpret_cast<igfd_dialog*>(ptr));
}

// =========================================================================
// Memory editor (imgui_club)
// =========================================================================

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_create(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(me_create());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_destroy(JNIEnv*, jclass, jlong ptr) {
    me_destroy(reinterpret_cast<me_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_drawWindow(JNIEnv* env, jclass, jlong ptr, jstring title, jbyteArray data, jint size, jlong base_addr) {
    std::string title_s = jstring_to_string(env, title);
    jbyte* bytes = data != nullptr ? env->GetByteArrayElements(data, nullptr) : nullptr;
    me_draw_window(
        reinterpret_cast<me_editor*>(ptr),
        title_s.c_str(),
        bytes,
        static_cast<size_t>(size),
        static_cast<uint64_t>(base_addr));
    if (bytes != nullptr) {
        env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);
    }
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_drawContents(JNIEnv* env, jclass, jlong ptr, jbyteArray data, jint size, jlong base_addr) {
    jbyte* bytes = data != nullptr ? env->GetByteArrayElements(data, nullptr) : nullptr;
    me_draw_contents(
        reinterpret_cast<me_editor*>(ptr),
        bytes,
        static_cast<size_t>(size),
        static_cast<uint64_t>(base_addr));
    if (bytes != nullptr) {
        env->ReleaseByteArrayElements(data, bytes, JNI_ABORT);
    }
}

static me_editor* to_me(JNIEnv*, jlong ptr) {
    return reinterpret_cast<me_editor*>(ptr);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_isOpen(JNIEnv*, jclass, jlong e) {
    return me_is_open(to_me(nullptr, e)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_setOpen(JNIEnv*, jclass, jlong e, jboolean value) {
    me_set_open(to_me(nullptr, e), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_isReadOnly(JNIEnv*, jclass, jlong e) {
    return me_is_read_only(to_me(nullptr, e)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_setReadOnly(JNIEnv*, jclass, jlong e, jboolean value) {
    me_set_read_only(to_me(nullptr, e), value == JNI_TRUE);
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_getCols(JNIEnv*, jclass, jlong e) {
    return me_get_cols(to_me(nullptr, e));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_setCols(JNIEnv*, jclass, jlong e, jint value) {
    me_set_cols(to_me(nullptr, e), value);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_isOptShowOptions(JNIEnv*, jclass, jlong e) {
    return me_is_opt_show_options(to_me(nullptr, e)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_setOptShowOptions(JNIEnv*, jclass, jlong e, jboolean value) {
    me_set_opt_show_options(to_me(nullptr, e), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_isOptShowDataPreview(JNIEnv*, jclass, jlong e) {
    return me_is_opt_show_data_preview(to_me(nullptr, e)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_setOptShowDataPreview(JNIEnv*, jclass, jlong e, jboolean value) {
    me_set_opt_show_data_preview(to_me(nullptr, e), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_isOptShowHexII(JNIEnv*, jclass, jlong e) {
    return me_is_opt_show_hex_ii(to_me(nullptr, e)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_setOptShowHexII(JNIEnv*, jclass, jlong e, jboolean value) {
    me_set_opt_show_hex_ii(to_me(nullptr, e), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_isOptShowAscii(JNIEnv*, jclass, jlong e) {
    return me_is_opt_show_ascii(to_me(nullptr, e)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_setOptShowAscii(JNIEnv*, jclass, jlong e, jboolean value) {
    me_set_opt_show_ascii(to_me(nullptr, e), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_isOptGreyOutZeroes(JNIEnv*, jclass, jlong e) {
    return me_is_opt_grey_out_zeroes(to_me(nullptr, e)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_setOptGreyOutZeroes(JNIEnv*, jclass, jlong e, jboolean value) {
    me_set_opt_grey_out_zeroes(to_me(nullptr, e), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_isOptUpperCaseHex(JNIEnv*, jclass, jlong e) {
    return me_is_opt_upper_case_hex(to_me(nullptr, e)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_setOptUpperCaseHex(JNIEnv*, jclass, jlong e, jboolean value) {
    me_set_opt_upper_case_hex(to_me(nullptr, e), value == JNI_TRUE);
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_getOptMidColsCount(JNIEnv*, jclass, jlong e) {
    return me_get_opt_mid_cols_count(to_me(nullptr, e));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_setOptMidColsCount(JNIEnv*, jclass, jlong e, jint value) {
    me_set_opt_mid_cols_count(to_me(nullptr, e), value);
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_getOptAddrDigitsCount(JNIEnv*, jclass, jlong e) {
    return me_get_opt_addr_digits_count(to_me(nullptr, e));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_setOptAddrDigitsCount(JNIEnv*, jclass, jlong e, jint value) {
    me_set_opt_addr_digits_count(to_me(nullptr, e), value);
}

JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_getOptFooterExtraHeight(JNIEnv*, jclass, jlong e) {
    return me_get_opt_footer_extra_height(to_me(nullptr, e));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_setOptFooterExtraHeight(JNIEnv*, jclass, jlong e, jfloat value) {
    me_set_opt_footer_extra_height(to_me(nullptr, e), value);
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_getHighlightColor(JNIEnv*, jclass, jlong e) {
    return static_cast<jint>(me_get_highlight_color(to_me(nullptr, e)));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_setHighlightColor(JNIEnv*, jclass, jlong e, jint value) {
    me_set_highlight_color(to_me(nullptr, e), static_cast<uint32_t>(value));
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_isMouseHovered(JNIEnv*, jclass, jlong e) {
    return me_is_mouse_hovered(to_me(nullptr, e)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_memoryeditor_Jni_mouseHoveredAddr(JNIEnv*, jclass, jlong e) {
    return static_cast<jlong>(me_mouse_hovered_addr(to_me(nullptr, e)));
}

} // extern "C" (node editor / file dialog / memory editor additions)

extern "C" {

// =========================================================================
// Multi-context compositor (imgui_club)
// =========================================================================

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_mcc_Jni_create(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(mcc_create());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_mcc_Jni_destroy(JNIEnv*, jclass, jlong ptr) {
    mcc_destroy(reinterpret_cast<mcc_compositor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_mcc_Jni_addContext(JNIEnv*, jclass, jlong ptr, jlong ctx) {
    mcc_add_context(reinterpret_cast<mcc_compositor*>(ptr), reinterpret_cast<imgui_context*>(ctx));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_mcc_Jni_removeContext(JNIEnv*, jclass, jlong ptr, jlong ctx) {
    mcc_remove_context(reinterpret_cast<mcc_compositor*>(ptr), reinterpret_cast<imgui_context*>(ctx));
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_mcc_Jni_getContextCount(JNIEnv*, jclass, jlong ptr) {
    return mcc_get_context_count(reinterpret_cast<mcc_compositor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_mcc_Jni_preNewFrameUpdateAll(JNIEnv*, jclass, jlong ptr) {
    mcc_pre_new_frame_update_all(reinterpret_cast<mcc_compositor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_mcc_Jni_postNewFrameUpdateOne(JNIEnv*, jclass, jlong ptr, jlong ctx) {
    mcc_post_new_frame_update_one(reinterpret_cast<mcc_compositor*>(ptr), reinterpret_cast<imgui_context*>(ctx));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_mcc_Jni_postEndFrameUpdateAll(JNIEnv*, jclass, jlong ptr) {
    mcc_post_end_frame_update_all(reinterpret_cast<mcc_compositor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_mcc_Jni_showDebugWindow(JNIEnv*, jclass, jlong ptr) {
    mcc_show_debug_window(reinterpret_cast<mcc_compositor*>(ptr));
}

// =========================================================================
// Threaded rendering (imgui_club): ImDrawDataSnapshot
// =========================================================================

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_snapshotCreate(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(trs_snapshot_create());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_snapshotDestroy(JNIEnv*, jclass, jlong ptr) {
    trs_snapshot_destroy(reinterpret_cast<trs_snapshot*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_snapUsingSwap(JNIEnv*, jclass, jlong ptr, jlong src, jdouble current_time) {
    trs_snapshot_snap_using_swap(reinterpret_cast<trs_snapshot*>(ptr), reinterpret_cast<imgui_draw_data*>(src), current_time);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_snapshotClear(JNIEnv*, jclass, jlong ptr) {
    trs_snapshot_clear(reinterpret_cast<trs_snapshot*>(ptr));
}

JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_getMemoryCompactTimer(JNIEnv*, jclass, jlong ptr) {
    return trs_snapshot_get_memory_compact_timer(reinterpret_cast<trs_snapshot*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_setMemoryCompactTimer(JNIEnv*, jclass, jlong ptr, jfloat seconds) {
    trs_snapshot_set_memory_compact_timer(reinterpret_cast<trs_snapshot*>(ptr), seconds);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_isValid(JNIEnv*, jclass, jlong ptr) {
    return trs_snapshot_is_valid(reinterpret_cast<trs_snapshot*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_getFrameCount(JNIEnv*, jclass, jlong ptr) {
    return trs_snapshot_get_frame_count(reinterpret_cast<trs_snapshot*>(ptr));
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_getTotalIdxCount(JNIEnv*, jclass, jlong ptr) {
    return trs_snapshot_get_total_idx_count(reinterpret_cast<trs_snapshot*>(ptr));
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_getTotalVtxCount(JNIEnv*, jclass, jlong ptr) {
    return trs_snapshot_get_total_vtx_count(reinterpret_cast<trs_snapshot*>(ptr));
}


JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_getDisplayPos(JNIEnv* env, jclass, jlong ptr) {
    imgui_vec2 v = trs_snapshot_get_display_pos(reinterpret_cast<trs_snapshot*>(ptr));
    float vals[2] = {v.x, v.y};
    jfloatArray out = env->NewFloatArray(2);
    set_float_array(env, out, vals, 2);
    return out;
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_getDisplaySize(JNIEnv* env, jclass, jlong ptr) {
    imgui_vec2 v = trs_snapshot_get_display_size(reinterpret_cast<trs_snapshot*>(ptr));
    float vals[2] = {v.x, v.y};
    jfloatArray out = env->NewFloatArray(2);
    set_float_array(env, out, vals, 2);
    return out;
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_getFramebufferScale(JNIEnv* env, jclass, jlong ptr) {
    imgui_vec2 v = trs_snapshot_get_framebuffer_scale(reinterpret_cast<trs_snapshot*>(ptr));
    float vals[2] = {v.x, v.y};
    jfloatArray out = env->NewFloatArray(2);
    set_float_array(env, out, vals, 2);
    return out;
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_getCmdListsCount(JNIEnv*, jclass, jlong ptr) {
    return trs_snapshot_get_cmd_lists_count(reinterpret_cast<trs_snapshot*>(ptr));
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_getCmdList(JNIEnv*, jclass, jlong ptr, jint index) {
    return reinterpret_cast<jlong>(trs_snapshot_get_cmd_list(reinterpret_cast<trs_snapshot*>(ptr), index));
}

// =========================================================================
// Threaded rendering (imgui_club): ImTextureQueue
// =========================================================================

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_queueCreate(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(trs_texture_queue_create());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_queueDestroy(JNIEnv*, jclass, jlong ptr) {
    trs_texture_queue_destroy(reinterpret_cast<trs_texture_queue*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_setInFlightFrames(JNIEnv*, jclass, jlong ptr, jint frames) {
    trs_texture_queue_set_in_flight_frames(reinterpret_cast<trs_texture_queue*>(ptr), frames);
}


JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_getInFlightFrames(JNIEnv*, jclass, jlong ptr) {
    return trs_texture_queue_get_in_flight_frames(reinterpret_cast<trs_texture_queue*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_preNewFrame(JNIEnv*, jclass, jlong ptr) {
    trs_texture_queue_pre_new_frame(reinterpret_cast<trs_texture_queue*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_queueRequests(JNIEnv*, jclass, jlong ptr, jlong draw_data) {
    trs_texture_queue_queue_requests(reinterpret_cast<trs_texture_queue*>(ptr), reinterpret_cast<imgui_draw_data*>(draw_data));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_processRequests(JNIEnv*, jclass, jlong ptr, jlong draw_data) {
    trs_texture_queue_process_requests(reinterpret_cast<trs_texture_queue*>(ptr), reinterpret_cast<imgui_draw_data*>(draw_data));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_threadedrendering_Jni_shutdown(JNIEnv*, jclass, jlong ptr) {
    trs_texture_queue_shutdown(reinterpret_cast<trs_texture_queue*>(ptr));
}

} // extern "C" (multi-context compositor / threaded rendering additions)
// =========================================================================
// ImPlot3D
// =========================================================================

// Fills an implot3d_spec C struct from a float spec array encoded as
// [set_flag, value(s)] for each of the 11 optional groups: line_color(4),
// line_weight(1), fill_color(4), fill_alpha(1), marker(1), marker_size(1),
// marker_line_color(4), marker_fill_color(4), offset(1), stride(1), flags(1).
// 11 flags + 23 values = 34 floats; a null array keeps every library default.
#define IMPLOT3D_SPEC_FLOAT_COUNT 34 // 11 set-flags + 23 value slots

static implot3d_spec decode_spec3d(const jfloat* data) {
    implot3d_spec spec;
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

// ImPlot3DStyle float-array layout (76 floats, identical in ImPlot3D.jvm.kt):
// lineWeight, marker, markerSize, fillAlpha,
// plotDefaultSize.x/y, plotMinSize.x/y, plotPadding.x/y, labelPadding.x/y,
// legendPadding.x/y, legendInnerPadding.x/y, legendSpacing.x/y,
// viewScaleFactor, colors[14] (x/y/z/w interleaved), colormap.
#define IMPLOT3D_STYLE_FLOAT_COUNT 76

static void read_style3d_to_floats(const implot3d_style& style, jfloat* out) {
    int i = 0;
    out[i++] = style.line_weight;
    out[i++] = (jfloat)style.marker;
    out[i++] = style.marker_size;
    out[i++] = style.fill_alpha;
    out[i++] = style.plot_default_size.x;
    out[i++] = style.plot_default_size.y;
    out[i++] = style.plot_min_size.x;
    out[i++] = style.plot_min_size.y;
    out[i++] = style.plot_padding.x;
    out[i++] = style.plot_padding.y;
    out[i++] = style.label_padding.x;
    out[i++] = style.label_padding.y;
    out[i++] = style.legend_padding.x;
    out[i++] = style.legend_padding.y;
    out[i++] = style.legend_inner_padding.x;
    out[i++] = style.legend_inner_padding.y;
    out[i++] = style.legend_spacing.x;
    out[i++] = style.legend_spacing.y;
    out[i++] = style.view_scale_factor;
    for (int c = 0; c < 14; c++) {
        out[i++] = style.colors[c].x;
        out[i++] = style.colors[c].y;
        out[i++] = style.colors[c].z;
        out[i++] = style.colors[c].w;
    }
    out[i++] = (jfloat)style.colormap;
}

static void write_style3d_from_floats(implot3d_style& style, const jfloat* in) {
    int i = 0;
    style.line_weight = in[i++];
    style.marker = (int)in[i++];
    style.marker_size = in[i++];
    style.fill_alpha = in[i++];
    style.plot_default_size.x = in[i++];
    style.plot_default_size.y = in[i++];
    style.plot_min_size.x = in[i++];
    style.plot_min_size.y = in[i++];
    style.plot_padding.x = in[i++];
    style.plot_padding.y = in[i++];
    style.label_padding.x = in[i++];
    style.label_padding.y = in[i++];
    style.legend_padding.x = in[i++];
    style.legend_padding.y = in[i++];
    style.legend_inner_padding.x = in[i++];
    style.legend_inner_padding.y = in[i++];
    style.legend_spacing.x = in[i++];
    style.legend_spacing.y = in[i++];
    style.view_scale_factor = in[i++];
    for (int c = 0; c < 14; c++) {
        style.colors[c].x = in[i++];
        style.colors[c].y = in[i++];
        style.colors[c].z = in[i++];
        style.colors[c].w = in[i++];
    }
    style.colormap = (int)in[i++];
}

static jfloatArray vec2_to_jfloat_array(JNIEnv* env, imgui_vec2 v) {
    jfloatArray out = env->NewFloatArray(2);
    jfloat values[2] = {v.x, v.y};
    env->SetFloatArrayRegion(out, 0, 2, values);
    return out;
}

static jfloatArray vec4_to_jfloat_array(JNIEnv* env, imgui_vec4 v) {
    jfloatArray out = env->NewFloatArray(4);
    jfloat values[4] = {v.x, v.y, v.z, v.w};
    env->SetFloatArrayRegion(out, 0, 4, values);
    return out;
}

static jdoubleArray point3d_to_jdouble_array(JNIEnv* env, implot3d_point p) {
    jdoubleArray out = env->NewDoubleArray(3);
    jdouble values[3] = {p.x, p.y, p.z};
    env->SetDoubleArrayRegion(out, 0, 3, values);
    return out;
}

static jdoubleArray quat3d_to_jdouble_array(JNIEnv* env, implot3d_quat q) {
    jdoubleArray out = env->NewDoubleArray(4);
    jdouble values[4] = {q.x, q.y, q.z, q.w};
    env->SetDoubleArrayRegion(out, 0, 4, values);
    return out;
}

static jdoubleArray mesh_vtx_to_jdouble_array(JNIEnv* env, const implot3d_point* vtx, int count) {
    jdoubleArray out = env->NewDoubleArray((jsize)count * 3);
    jdouble values[3];
    for (int i = 0; i < count; i++) {
        values[0] = vtx[i].x;
        values[1] = vtx[i].y;
        values[2] = vtx[i].z;
        env->SetDoubleArrayRegion(out, i * 3, 3, values);
    }
    return out;
}

static jintArray mesh_idx_to_jint_array(JNIEnv* env, const unsigned int* idx, int count) {
    jintArray out = env->NewIntArray(count);
    env->SetIntArrayRegion(out, 0, count, reinterpret_cast<const jint*>(idx));
    return out;
}

extern "C" {

// ---- Context ----

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_createContext(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(implot3d_create_context());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_destroyContext(JNIEnv*, jclass, jlong ptr) {
    implot3d_destroy_context(reinterpret_cast<implot3d_context*>(ptr));
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_getCurrentContext(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(implot3d_get_current_context());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setCurrentContext(JNIEnv*, jclass, jlong ctx) {
    implot3d_set_current_context(reinterpret_cast<implot3d_context*>(ctx));
}

// ---- Begin/End plot ----

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_beginPlot(JNIEnv* env, jclass, jstring title_id, jfloat size_x, jfloat size_y, jint flags) {
    std::string title = jstring_to_string(env, title_id);
    imgui_vec2 size;
    size.x = size_x;
    size.y = size_y;
    return implot3d_begin_plot(title.c_str(), size, flags) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_endPlot(JNIEnv*, jclass) {
    implot3d_end_plot();
}

// ---- Setup ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupAxis(JNIEnv* env, jclass, jint axis, jstring label, jint flags) {
    std::string label_str = jstring_to_string(env, label);
    implot3d_setup_axis(axis, label != nullptr && !label_str.empty() ? label_str.c_str() : nullptr, flags);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupAxisLimits(JNIEnv*, jclass, jint axis, jdouble v_min, jdouble v_max, jint cond) {
    implot3d_setup_axis_limits(axis, v_min, v_max, cond);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupAxisTicksValues(JNIEnv* env, jclass, jint axis, jdoubleArray values_arr, jobjectArray labels, jint tick_count, jboolean keep_default) {
    std::vector<std::string> strings;
    std::vector<const char*> cstrings;
    if (labels != nullptr) {
        get_string_array(env, labels, strings, cstrings);
    }
    jdouble* values = env->GetDoubleArrayElements(values_arr, nullptr);
    implot3d_setup_axis_ticks_values(axis, values, tick_count, labels != nullptr ? cstrings.data() : nullptr, keep_default == JNI_TRUE);
    env->ReleaseDoubleArrayElements(values_arr, values, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupAxisTicksLimits(JNIEnv* env, jclass, jint axis, jdouble v_min, jdouble v_max, jint tick_count, jobjectArray labels, jboolean keep_default) {
    std::vector<std::string> strings;
    std::vector<const char*> cstrings;
    if (labels != nullptr) {
        get_string_array(env, labels, strings, cstrings);
    }
    implot3d_setup_axis_ticks_limits(axis, v_min, v_max, tick_count, labels != nullptr ? cstrings.data() : nullptr, keep_default == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupAxisScale(JNIEnv*, jclass, jint axis, jint scale) {
    implot3d_setup_axis_scale(axis, scale);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupAxisLimitsConstraints(JNIEnv*, jclass, jint axis, jdouble v_min, jdouble v_max) {
    implot3d_setup_axis_limits_constraints(axis, v_min, v_max);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupAxisZoomConstraints(JNIEnv*, jclass, jint axis, jdouble zoom_min, jdouble zoom_max) {
    implot3d_setup_axis_zoom_constraints(axis, zoom_min, zoom_max);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupAxes(JNIEnv* env, jclass, jstring x_label, jstring y_label, jstring z_label, jint x_flags, jint y_flags, jint z_flags) {
    std::string x = jstring_to_string(env, x_label);
    std::string y = jstring_to_string(env, y_label);
    std::string z = jstring_to_string(env, z_label);
    implot3d_setup_axes(x.empty() ? nullptr : x.c_str(), y.empty() ? nullptr : y.c_str(), z.empty() ? nullptr : z.c_str(), x_flags, y_flags, z_flags);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupAxesLimits(JNIEnv*, jclass, jdouble x_min, jdouble x_max, jdouble y_min, jdouble y_max, jdouble z_min, jdouble z_max, jint cond) {
    implot3d_setup_axes_limits(x_min, x_max, y_min, y_max, z_min, z_max, cond);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupBoxRotationAngles(JNIEnv*, jclass, jdouble elevation, jdouble azimuth, jboolean animate, jint cond) {
    implot3d_setup_box_rotation_angles(elevation, azimuth, animate == JNI_TRUE, cond);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupBoxRotationQuat(JNIEnv*, jclass, jdouble x, jdouble y, jdouble z, jdouble w, jboolean animate, jint cond) {
    implot3d_quat rotation;
    rotation.x = x;
    rotation.y = y;
    rotation.z = z;
    rotation.w = w;
    implot3d_setup_box_rotation_quat(rotation, animate == JNI_TRUE, cond);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupBoxInitialRotationAngles(JNIEnv*, jclass, jdouble elevation, jdouble azimuth) {
    implot3d_setup_box_initial_rotation_angles(elevation, azimuth);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupBoxInitialRotationQuat(JNIEnv*, jclass, jdouble x, jdouble y, jdouble z, jdouble w) {
    implot3d_quat rotation;
    rotation.x = x;
    rotation.y = y;
    rotation.z = z;
    rotation.w = w;
    implot3d_setup_box_initial_rotation_quat(rotation);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupBoxScale(JNIEnv*, jclass, jdouble x, jdouble y, jdouble z) {
    implot3d_setup_box_scale(x, y, z);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setupLegend(JNIEnv*, jclass, jint location, jint flags) {
    implot3d_setup_legend(location, flags);
}

// ---- Plot items (double data arrays) ----

#define IMPLOT3D_PLOT_ITEM_BODY(name)                                                                  \
    std::string label = jstring_to_string(env, label_id);                                             \
    jdouble* xs = env->GetDoubleArrayElements(xs_arr, nullptr);                                       \
    jdouble* ys = env->GetDoubleArrayElements(ys_arr, nullptr);                                       \
    jdouble* zs = env->GetDoubleArrayElements(zs_arr, nullptr);                                       \
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr; \
    implot3d_spec spec = decode_spec3d(spec_data);                                                     \
    name(label.c_str(), xs, ys, zs, count, &spec);                                                     \
    env->ReleaseDoubleArrayElements(xs_arr, xs, JNI_ABORT);                                           \
    env->ReleaseDoubleArrayElements(ys_arr, ys, JNI_ABORT);                                           \
    env->ReleaseDoubleArrayElements(zs_arr, zs, JNI_ABORT);                                           \
    if (spec_arr != nullptr) {                                                                         \
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);                                \
    }

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_plotScatter(JNIEnv* env, jclass, jstring label_id, jdoubleArray xs_arr, jdoubleArray ys_arr, jdoubleArray zs_arr, jint count, jfloatArray spec_arr) {
    IMPLOT3D_PLOT_ITEM_BODY(implot3d_plot_scatter)
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_plotLine(JNIEnv* env, jclass, jstring label_id, jdoubleArray xs_arr, jdoubleArray ys_arr, jdoubleArray zs_arr, jint count, jfloatArray spec_arr) {
    IMPLOT3D_PLOT_ITEM_BODY(implot3d_plot_line)
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_plotTriangle(JNIEnv* env, jclass, jstring label_id, jdoubleArray xs_arr, jdoubleArray ys_arr, jdoubleArray zs_arr, jint count, jfloatArray spec_arr) {
    IMPLOT3D_PLOT_ITEM_BODY(implot3d_plot_triangle)
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_plotQuad(JNIEnv* env, jclass, jstring label_id, jdoubleArray xs_arr, jdoubleArray ys_arr, jdoubleArray zs_arr, jint count, jfloatArray spec_arr) {
    IMPLOT3D_PLOT_ITEM_BODY(implot3d_plot_quad)
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_plotSurface(JNIEnv* env, jclass, jstring label_id, jdoubleArray xs_arr, jdoubleArray ys_arr, jdoubleArray zs_arr, jint x_count, jint y_count, jdouble scale_min, jdouble scale_max, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jdouble* xs = env->GetDoubleArrayElements(xs_arr, nullptr);
    jdouble* ys = env->GetDoubleArrayElements(ys_arr, nullptr);
    jdouble* zs = env->GetDoubleArrayElements(zs_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot3d_spec spec = decode_spec3d(spec_data);
    implot3d_plot_surface(label.c_str(), xs, ys, zs, x_count, y_count, scale_min, scale_max, &spec);
    env->ReleaseDoubleArrayElements(xs_arr, xs, JNI_ABORT);
    env->ReleaseDoubleArrayElements(ys_arr, ys, JNI_ABORT);
    env->ReleaseDoubleArrayElements(zs_arr, zs, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

// vtx/idxs counts are derived from the array lengths on this side.
JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_plotMesh(JNIEnv* env, jclass, jstring label_id, jdoubleArray vtx_xs_arr, jdoubleArray vtx_ys_arr, jdoubleArray vtx_zs_arr, jintArray idxs_arr, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jdouble* vtx_xs = env->GetDoubleArrayElements(vtx_xs_arr, nullptr);
    jdouble* vtx_ys = env->GetDoubleArrayElements(vtx_ys_arr, nullptr);
    jdouble* vtx_zs = env->GetDoubleArrayElements(vtx_zs_arr, nullptr);
    jint* idxs = env->GetIntArrayElements(idxs_arr, nullptr);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot3d_spec spec = decode_spec3d(spec_data);
    jsize vtx_count = env->GetArrayLength(vtx_xs_arr);
    jsize idx_count = env->GetArrayLength(idxs_arr);
    implot3d_plot_mesh(label.c_str(), vtx_xs, vtx_ys, vtx_zs, reinterpret_cast<const unsigned int*>(idxs), (int)vtx_count, (int)idx_count, &spec);
    env->ReleaseDoubleArrayElements(vtx_xs_arr, vtx_xs, JNI_ABORT);
    env->ReleaseDoubleArrayElements(vtx_ys_arr, vtx_ys, JNI_ABORT);
    env->ReleaseDoubleArrayElements(vtx_zs_arr, vtx_zs, JNI_ABORT);
    env->ReleaseIntArrayElements(idxs_arr, idxs, JNI_ABORT);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_plotText(JNIEnv* env, jclass, jstring text, jdouble x, jdouble y, jdouble z, jdouble angle, jfloat pix_x, jfloat pix_y) {
    std::string text_str = jstring_to_string(env, text);
    imgui_vec2 offset;
    offset.x = pix_x;
    offset.y = pix_y;
    implot3d_plot_text(text_str.c_str(), x, y, z, angle, offset);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_plotDummy(JNIEnv* env, jclass, jstring label_id, jfloatArray spec_arr) {
    std::string label = jstring_to_string(env, label_id);
    jfloat* spec_data = spec_arr != nullptr ? env->GetFloatArrayElements(spec_arr, nullptr) : nullptr;
    implot3d_spec spec = decode_spec3d(spec_data);
    implot3d_plot_dummy(label.c_str(), &spec);
    if (spec_arr != nullptr) {
        env->ReleaseFloatArrayElements(spec_arr, spec_data, JNI_ABORT);
    }
}

// ---- Plot utils ----

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_plotToPixelsPoint(JNIEnv* env, jclass, jdouble x, jdouble y, jdouble z) {
    implot3d_point point;
    point.x = x;
    point.y = y;
    point.z = z;
    return vec2_to_jfloat_array(env, implot3d_plot_to_pixels_point(point));
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_plotToPixelsXyz(JNIEnv* env, jclass, jdouble x, jdouble y, jdouble z) {
    return vec2_to_jfloat_array(env, implot3d_plot_to_pixels_xyz(x, y, z));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pixelsToPlotRayVec2(JNIEnv* env, jclass, jfloat pix_x, jfloat pix_y) {
    imgui_vec2 pix;
    pix.x = pix_x;
    pix.y = pix_y;
    implot3d_ray ray = implot3d_pixels_to_plot_ray_vec2(pix);
    jdoubleArray out = env->NewDoubleArray(6);
    jdouble values[6] = {ray.origin.x, ray.origin.y, ray.origin.z, ray.direction.x, ray.direction.y, ray.direction.z};
    env->SetDoubleArrayRegion(out, 0, 6, values);
    return out;
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pixelsToPlotRayXy(JNIEnv* env, jclass, jdouble x, jdouble y) {
    implot3d_ray ray = implot3d_pixels_to_plot_ray_xy(x, y);
    jdoubleArray out = env->NewDoubleArray(6);
    jdouble values[6] = {ray.origin.x, ray.origin.y, ray.origin.z, ray.direction.x, ray.direction.y, ray.direction.z};
    env->SetDoubleArrayRegion(out, 0, 6, values);
    return out;
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pixelsToPlotPlaneVec2(JNIEnv* env, jclass, jfloat pix_x, jfloat pix_y, jint plane, jboolean mask) {
    imgui_vec2 pix;
    pix.x = pix_x;
    pix.y = pix_y;
    return point3d_to_jdouble_array(env, implot3d_pixels_to_plot_plane_vec2(pix, plane, mask == JNI_TRUE));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pixelsToPlotPlaneXy(JNIEnv* env, jclass, jdouble x, jdouble y, jint plane, jboolean mask) {
    return point3d_to_jdouble_array(env, implot3d_pixels_to_plot_plane_xy(x, y, plane, mask == JNI_TRUE));
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_getPlotRectPos(JNIEnv* env, jclass) {
    return vec2_to_jfloat_array(env, implot3d_get_plot_rect_pos());
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_getPlotRectSize(JNIEnv* env, jclass) {
    return vec2_to_jfloat_array(env, implot3d_get_plot_rect_size());
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_getPlotDrawList(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(implot3d_get_plot_draw_list());
}

// ---- Style ----

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_getStyle(JNIEnv* env, jclass) {
    implot3d_style style;
    implot3d_get_style(&style);
    jfloatArray out = env->NewFloatArray(IMPLOT3D_STYLE_FLOAT_COUNT);
    jfloat values[IMPLOT3D_STYLE_FLOAT_COUNT];
    read_style3d_to_floats(style, values);
    env->SetFloatArrayRegion(out, 0, IMPLOT3D_STYLE_FLOAT_COUNT, values);
    return out;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_setStyle(JNIEnv* env, jclass, jfloatArray style_arr) {
    jfloat* data = env->GetFloatArrayElements(style_arr, nullptr);
    implot3d_style style;
    write_style3d_from_floats(style, data);
    implot3d_set_style(&style);
    env->ReleaseFloatArrayElements(style_arr, data, JNI_ABORT);
}

// A null dst array edits the current style in place; a non-null one is
// decoded, filled and written back so the caller sees the result.
#define IMPLOT3D_STYLE_COLORS_BODY(call)                                                               \
    if (dst_arr == nullptr) {                                                                          \
        call(nullptr);                                                                                 \
        return nullptr;                                                                                \
    }                                                                                                  \
    jfloat* data = env->GetFloatArrayElements(dst_arr, nullptr);                                       \
    implot3d_style style;                                                                              \
    write_style3d_from_floats(style, data);                                                            \
    call(&style);                                                                                      \
    read_style3d_to_floats(style, data);                                                               \
    env->ReleaseFloatArrayElements(dst_arr, data, 0);                                                  \
    return dst_arr;

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_styleColorsAuto(JNIEnv* env, jclass, jfloatArray dst_arr) {
    IMPLOT3D_STYLE_COLORS_BODY(implot3d_style_colors_auto)
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_styleColorsDark(JNIEnv* env, jclass, jfloatArray dst_arr) {
    IMPLOT3D_STYLE_COLORS_BODY(implot3d_style_colors_dark)
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_styleColorsLight(JNIEnv* env, jclass, jfloatArray dst_arr) {
    IMPLOT3D_STYLE_COLORS_BODY(implot3d_style_colors_light)
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_styleColorsClassic(JNIEnv* env, jclass, jfloatArray dst_arr) {
    IMPLOT3D_STYLE_COLORS_BODY(implot3d_style_colors_classic)
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pushStyleColorVec4(JNIEnv*, jclass, jint idx, jfloat r, jfloat g, jfloat b, jfloat a) {
    imgui_vec4 color;
    color.x = r;
    color.y = g;
    color.z = b;
    color.w = a;
    implot3d_push_style_color_vec4(idx, color);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pushStyleColorU32(JNIEnv*, jclass, jint idx, jint color) {
    implot3d_push_style_color_u32(idx, (uint32_t)color);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_popStyleColor(JNIEnv*, jclass, jint count) {
    implot3d_pop_style_color(count);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pushStyleVarFloat(JNIEnv*, jclass, jint idx, jfloat val) {
    implot3d_push_style_var_float(idx, val);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pushStyleVarInt(JNIEnv*, jclass, jint idx, jint val) {
    implot3d_push_style_var_int(idx, val);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pushStyleVarVec2(JNIEnv*, jclass, jint idx, jfloat x, jfloat y) {
    imgui_vec2 val;
    val.x = x;
    val.y = y;
    implot3d_push_style_var_vec2(idx, val);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_popStyleVar(JNIEnv*, jclass, jint count) {
    implot3d_pop_style_var(count);
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_getStyleColor(JNIEnv* env, jclass, jint idx) {
    return vec4_to_jfloat_array(env, implot3d_get_style_color_vec4(idx));
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_getStyleColorU32(JNIEnv*, jclass, jint idx) {
    return (jint)implot3d_get_style_color_u32(idx);
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_nextMarker(JNIEnv*, jclass) {
    return implot3d_next_marker();
}

// ---- Colormaps ----

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_addColormapVec4(JNIEnv* env, jclass, jstring name, jfloatArray cols_arr, jboolean qual) {
    std::string name_str = jstring_to_string(env, name);
    jfloat* cols = env->GetFloatArrayElements(cols_arr, nullptr);
    jsize size = env->GetArrayLength(cols_arr);
    jint result = implot3d_add_colormap_vec4(name_str.c_str(), reinterpret_cast<const imgui_vec4*>(cols), (int)(size / 4), qual == JNI_TRUE);
    env->ReleaseFloatArrayElements(cols_arr, cols, JNI_ABORT);
    return result;
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_addColormapU32(JNIEnv* env, jclass, jstring name, jintArray cols_arr, jboolean qual) {
    std::string name_str = jstring_to_string(env, name);
    jint* cols = env->GetIntArrayElements(cols_arr, nullptr);
    jsize size = env->GetArrayLength(cols_arr);
    jint result = implot3d_add_colormap_u32(name_str.c_str(), reinterpret_cast<const uint32_t*>(cols), (int)size, qual == JNI_TRUE);
    env->ReleaseIntArrayElements(cols_arr, cols, JNI_ABORT);
    return result;
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_getColormapCount(JNIEnv*, jclass) {
    return implot3d_get_colormap_count();
}

JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_getColormapName(JNIEnv* env, jclass, jint cmap) {
    const char* name = implot3d_get_colormap_name(cmap);
    return name != nullptr ? env->NewStringUTF(name) : nullptr;
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_getColormapIndex(JNIEnv* env, jclass, jstring name) {
    std::string name_str = jstring_to_string(env, name);
    return implot3d_get_colormap_index(name_str.c_str());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pushColormap(JNIEnv*, jclass, jint cmap) {
    implot3d_push_colormap(cmap);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pushColormapName(JNIEnv* env, jclass, jstring name) {
    std::string name_str = jstring_to_string(env, name);
    implot3d_push_colormap_name(name_str.c_str());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_popColormap(JNIEnv*, jclass, jint count) {
    implot3d_pop_colormap(count);
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_nextColormapColor(JNIEnv* env, jclass) {
    return vec4_to_jfloat_array(env, implot3d_next_colormap_color());
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_getColormapSize(JNIEnv*, jclass, jint cmap) {
    return implot3d_get_colormap_size(cmap);
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_getColormapColor(JNIEnv* env, jclass, jint idx, jint cmap) {
    return vec4_to_jfloat_array(env, implot3d_get_colormap_color(idx, cmap));
}

JNIEXPORT jfloatArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_sampleColormap(JNIEnv* env, jclass, jfloat t, jint cmap) {
    return vec4_to_jfloat_array(env, implot3d_sample_colormap(t, cmap));
}

// ---- Demo ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_showDemoWindow(JNIEnv* env, jclass, jbooleanArray p_open) {
    jboolean* elems = p_open != nullptr ? env->GetBooleanArrayElements(p_open, nullptr) : nullptr;
    implot3d_show_demo_window(reinterpret_cast<bool*>(elems));
    if (elems != nullptr) {
        env->ReleaseBooleanArrayElements(p_open, elems, 0);
    }
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_showAllDemos(JNIEnv*, jclass) {
    implot3d_show_all_demos();
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_showStyleEditor(JNIEnv*, jclass) {
    implot3d_show_style_editor();
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_showStyleSelector(JNIEnv* env, jclass, jstring label) {
    std::string label_str = jstring_to_string(env, label);
    return implot3d_show_style_selector(label_str.c_str()) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_showColormapSelector(JNIEnv* env, jclass, jstring label) {
    std::string label_str = jstring_to_string(env, label);
    return implot3d_show_colormap_selector(label_str.c_str()) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_showMetricsWindow(JNIEnv* env, jclass, jbooleanArray p_open) {
    jboolean* elems = p_open != nullptr ? env->GetBooleanArrayElements(p_open, nullptr) : nullptr;
    implot3d_show_metrics_window(reinterpret_cast<bool*>(elems));
    if (elems != nullptr) {
        env->ReleaseBooleanArrayElements(p_open, elems, 0);
    }
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_showAboutWindow(JNIEnv* env, jclass, jbooleanArray p_open) {
    jboolean* elems = p_open != nullptr ? env->GetBooleanArrayElements(p_open, nullptr) : nullptr;
    implot3d_show_about_window(reinterpret_cast<bool*>(elems));
    if (elems != nullptr) {
        env->ReleaseBooleanArrayElements(p_open, elems, 0);
    }
}

// ---- Built-in meshes ----

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_cubeVtx(JNIEnv* env, jclass) {
    return mesh_vtx_to_jdouble_array(env, implot3d_cube_vtx(), implot3d_cube_vtx_count());
}

JNIEXPORT jintArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_cubeIdx(JNIEnv* env, jclass) {
    return mesh_idx_to_jint_array(env, implot3d_cube_idx(), implot3d_cube_idx_count());
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_sphereVtx(JNIEnv* env, jclass) {
    return mesh_vtx_to_jdouble_array(env, implot3d_sphere_vtx(), implot3d_sphere_vtx_count());
}

JNIEXPORT jintArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_sphereIdx(JNIEnv* env, jclass) {
    return mesh_idx_to_jint_array(env, implot3d_sphere_idx(), implot3d_sphere_idx_count());
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_duckVtx(JNIEnv* env, jclass) {
    return mesh_vtx_to_jdouble_array(env, implot3d_duck_vtx(), implot3d_duck_vtx_count());
}

JNIEXPORT jintArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_duckIdx(JNIEnv* env, jclass) {
    return mesh_idx_to_jint_array(env, implot3d_duck_idx(), implot3d_duck_idx_count());
}

// ---- Point math ----

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointAdd(JNIEnv* env, jclass, jdouble ax, jdouble ay, jdouble az, jdouble bx, jdouble by, jdouble bz) {
    return point3d_to_jdouble_array(env, implot3d_point_add(implot3d_point_make(ax, ay, az), implot3d_point_make(bx, by, bz)));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointSub(JNIEnv* env, jclass, jdouble ax, jdouble ay, jdouble az, jdouble bx, jdouble by, jdouble bz) {
    return point3d_to_jdouble_array(env, implot3d_point_sub(implot3d_point_make(ax, ay, az), implot3d_point_make(bx, by, bz)));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointMul(JNIEnv* env, jclass, jdouble ax, jdouble ay, jdouble az, jdouble bx, jdouble by, jdouble bz) {
    return point3d_to_jdouble_array(env, implot3d_point_mul(implot3d_point_make(ax, ay, az), implot3d_point_make(bx, by, bz)));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointDiv(JNIEnv* env, jclass, jdouble ax, jdouble ay, jdouble az, jdouble bx, jdouble by, jdouble bz) {
    return point3d_to_jdouble_array(env, implot3d_point_div(implot3d_point_make(ax, ay, az), implot3d_point_make(bx, by, bz)));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointMulScalar(JNIEnv* env, jclass, jdouble x, jdouble y, jdouble z, jdouble scalar) {
    return point3d_to_jdouble_array(env, implot3d_point_mul_double(implot3d_point_make(x, y, z), scalar));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointDivScalar(JNIEnv* env, jclass, jdouble x, jdouble y, jdouble z, jdouble scalar) {
    return point3d_to_jdouble_array(env, implot3d_point_div_double(implot3d_point_make(x, y, z), scalar));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointNeg(JNIEnv* env, jclass, jdouble x, jdouble y, jdouble z) {
    return point3d_to_jdouble_array(env, implot3d_point_neg(implot3d_point_make(x, y, z)));
}

JNIEXPORT jdouble JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointDot(JNIEnv*, jclass, jdouble ax, jdouble ay, jdouble az, jdouble bx, jdouble by, jdouble bz) {
    return implot3d_point_dot(implot3d_point_make(ax, ay, az), implot3d_point_make(bx, by, bz));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointCross(JNIEnv* env, jclass, jdouble ax, jdouble ay, jdouble az, jdouble bx, jdouble by, jdouble bz) {
    return point3d_to_jdouble_array(env, implot3d_point_cross(implot3d_point_make(ax, ay, az), implot3d_point_make(bx, by, bz)));
}

JNIEXPORT jdouble JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointLength(JNIEnv*, jclass, jdouble x, jdouble y, jdouble z) {
    return implot3d_point_length(implot3d_point_make(x, y, z));
}

JNIEXPORT jdouble JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointLengthSquared(JNIEnv*, jclass, jdouble x, jdouble y, jdouble z) {
    return implot3d_point_length_squared(implot3d_point_make(x, y, z));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointNormalized(JNIEnv* env, jclass, jdouble x, jdouble y, jdouble z) {
    return point3d_to_jdouble_array(env, implot3d_point_normalized(implot3d_point_make(x, y, z)));
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointIsNaN(JNIEnv*, jclass, jdouble x, jdouble y, jdouble z) {
    return implot3d_point_is_nan(implot3d_point_make(x, y, z)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_pointEq(JNIEnv*, jclass, jdouble ax, jdouble ay, jdouble az, jdouble bx, jdouble by, jdouble bz) {
    return implot3d_point_eq(implot3d_point_make(ax, ay, az), implot3d_point_make(bx, by, bz)) ? JNI_TRUE : JNI_FALSE;
}

// ---- Quat math ----

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_quatFromAngleAxis(JNIEnv* env, jclass, jdouble angle, jdouble ax, jdouble ay, jdouble az) {
    return quat3d_to_jdouble_array(env, implot3d_quat_from_angle_axis(angle, implot3d_point_make(ax, ay, az)));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_quatFromTwoVectors(JNIEnv* env, jclass, jdouble ax, jdouble ay, jdouble az, jdouble bx, jdouble by, jdouble bz) {
    return quat3d_to_jdouble_array(env, implot3d_quat_from_two_vectors(implot3d_point_make(ax, ay, az), implot3d_point_make(bx, by, bz)));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_quatFromElAz(JNIEnv* env, jclass, jdouble elevation, jdouble azimuth) {
    return quat3d_to_jdouble_array(env, implot3d_quat_from_el_az(elevation, azimuth));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_quatMul(JNIEnv* env, jclass, jdouble ax, jdouble ay, jdouble az, jdouble aw, jdouble bx, jdouble by, jdouble bz, jdouble bw) {
    return quat3d_to_jdouble_array(env, implot3d_quat_mul(implot3d_quat_make(ax, ay, az, aw), implot3d_quat_make(bx, by, bz, bw)));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_quatRotatePoint(JNIEnv* env, jclass, jdouble qx, jdouble qy, jdouble qz, jdouble qw, jdouble px, jdouble py, jdouble pz) {
    return point3d_to_jdouble_array(env, implot3d_quat_rotate_point(implot3d_quat_make(qx, qy, qz, qw), implot3d_point_make(px, py, pz)));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_quatNormalized(JNIEnv* env, jclass, jdouble x, jdouble y, jdouble z, jdouble w) {
    return quat3d_to_jdouble_array(env, implot3d_quat_normalized(implot3d_quat_make(x, y, z, w)));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_quatConjugate(JNIEnv* env, jclass, jdouble x, jdouble y, jdouble z, jdouble w) {
    return quat3d_to_jdouble_array(env, implot3d_quat_conjugate(implot3d_quat_make(x, y, z, w)));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_quatInverse(JNIEnv* env, jclass, jdouble x, jdouble y, jdouble z, jdouble w) {
    return quat3d_to_jdouble_array(env, implot3d_quat_inverse(implot3d_quat_make(x, y, z, w)));
}

JNIEXPORT jdouble JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_quatLength(JNIEnv*, jclass, jdouble x, jdouble y, jdouble z, jdouble w) {
    return implot3d_quat_length(implot3d_quat_make(x, y, z, w));
}

JNIEXPORT jdouble JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_quatDot(JNIEnv*, jclass, jdouble ax, jdouble ay, jdouble az, jdouble aw, jdouble bx, jdouble by, jdouble bz, jdouble bw) {
    return implot3d_quat_dot(implot3d_quat_make(ax, ay, az, aw), implot3d_quat_make(bx, by, bz, bw));
}

JNIEXPORT jdoubleArray JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_quatSlerp(JNIEnv* env, jclass, jdouble ax, jdouble ay, jdouble az, jdouble aw, jdouble bx, jdouble by, jdouble bz, jdouble bw, jdouble t) {
    return quat3d_to_jdouble_array(env, implot3d_quat_slerp(implot3d_quat_make(ax, ay, az, aw), implot3d_quat_make(bx, by, bz, bw), t));
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_implot3d_Jni_quatEq(JNIEnv*, jclass, jdouble ax, jdouble ay, jdouble az, jdouble aw, jdouble bx, jdouble by, jdouble bz, jdouble bw) {
    return implot3d_quat_eq(implot3d_quat_make(ax, ay, az, aw), implot3d_quat_make(bx, by, bz, bw)) ? JNI_TRUE : JNI_FALSE;
}

} // extern "C" (ImPlot3D additions)

// =========================================================================
// ColorTextEdit additions
// =========================================================================

static jstring te_string_to_jstring(JNIEnv* env, char* str) {
    if (str == nullptr) {
        return nullptr;
    }
    jstring out = env->NewStringUTF(str);
    te_string_free(str);
    return out;
}

extern "C" {

// ---- Lifecycle ----

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_create(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(te_create());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_destroy(JNIEnv*, jclass, jlong ptr) {
    te_destroy(reinterpret_cast<te_editor*>(ptr));
}

// ---- Text ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setText(JNIEnv* env, jclass, jlong ptr, jstring text) {
    std::string text_s = jstring_to_string(env, text);
    te_set_text(reinterpret_cast<te_editor*>(ptr), text_s.c_str());
}

JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getText(JNIEnv* env, jclass, jlong ptr) {
    return te_string_to_jstring(env, te_get_text(reinterpret_cast<te_editor*>(ptr)));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_clearText(JNIEnv*, jclass, jlong ptr) {
    te_clear_text(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isEmpty(JNIEnv*, jclass, jlong ptr) {
    return te_is_empty(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getLineCount(JNIEnv*, jclass, jlong ptr) {
    return static_cast<jlong>(te_get_line_count(reinterpret_cast<te_editor*>(ptr)));
}

JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getLineText(JNIEnv* env, jclass, jlong ptr, jlong line) {
    return te_string_to_jstring(env, te_get_line_text(reinterpret_cast<te_editor*>(ptr), static_cast<size_t>(line)));
}

// ---- Rendering ----

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_render(JNIEnv* env, jclass, jlong ptr, jstring title, jfloat size_x, jfloat size_y, jint child_flags, jint window_flags) {
    std::string title_s = jstring_to_string(env, title);
    return te_render(reinterpret_cast<te_editor*>(ptr), title_s.c_str(), size_x, size_y, child_flags, window_flags) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setFocus(JNIEnv*, jclass, jlong ptr) {
    te_set_focus(reinterpret_cast<te_editor*>(ptr));
}

// ---- Configuration ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setTabSize(JNIEnv*, jclass, jlong ptr, jlong value) {
    te_set_tab_size(reinterpret_cast<te_editor*>(ptr), static_cast<size_t>(value));
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getTabSize(JNIEnv*, jclass, jlong ptr) {
    return static_cast<jlong>(te_get_tab_size(reinterpret_cast<te_editor*>(ptr)));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setInsertSpacesOnTabs(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_insert_spaces_on_tabs(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isInsertSpacesOnTabs(JNIEnv*, jclass, jlong ptr) {
    return te_is_insert_spaces_on_tabs(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setLineSpacing(JNIEnv*, jclass, jlong ptr, jfloat value) {
    te_set_line_spacing(reinterpret_cast<te_editor*>(ptr), value);
}

JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getLineSpacing(JNIEnv*, jclass, jlong ptr) {
    return te_get_line_spacing(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setWordWrapEnabled(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_word_wrap_enabled(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isWordWrapEnabled(JNIEnv*, jclass, jlong ptr) {
    return te_is_word_wrap_enabled(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setReadOnlyEnabled(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_read_only_enabled(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isReadOnlyEnabled(JNIEnv*, jclass, jlong ptr) {
    return te_is_read_only_enabled(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setCaretsVisible(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_carets_visible(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isCaretsVisible(JNIEnv*, jclass, jlong ptr) {
    return te_is_carets_visible(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setAutoIndentEnabled(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_auto_indent_enabled(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isAutoIndentEnabled(JNIEnv*, jclass, jlong ptr) {
    return te_is_auto_indent_enabled(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setShowWhitespacesEnabled(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_show_whitespaces_enabled(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isShowWhitespacesEnabled(JNIEnv*, jclass, jlong ptr) {
    return te_is_show_whitespaces_enabled(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setShowLineNumbersEnabled(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_show_line_numbers_enabled(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isShowLineNumbersEnabled(JNIEnv*, jclass, jlong ptr) {
    return te_is_show_line_numbers_enabled(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setShowMiniMapEnabled(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_show_minimap_enabled(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isShowMiniMapEnabled(JNIEnv*, jclass, jlong ptr) {
    return te_is_show_minimap_enabled(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setShowMatchingBrackets(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_show_matching_brackets(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isShowingMatchingBrackets(JNIEnv*, jclass, jlong ptr) {
    return te_is_showing_matching_brackets(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setCompletePairedGlyphs(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_complete_paired_glyphs(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isCompletingPairedGlyphs(JNIEnv*, jclass, jlong ptr) {
    return te_is_completing_paired_glyphs(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setLineFoldingEnabled(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_line_folding_enabled(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isLineFoldingEnabled(JNIEnv*, jclass, jlong ptr) {
    return te_is_line_folding_enabled(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setOverwriteEnabled(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_overwrite_enabled(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isOverwriteEnabled(JNIEnv*, jclass, jlong ptr) {
    return te_is_overwrite_enabled(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setMiddleMouseScrollMode(JNIEnv*, jclass, jlong ptr) {
    te_set_middle_mouse_scroll_mode(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setMiddleMousePanMode(JNIEnv*, jclass, jlong ptr) {
    te_set_middle_mouse_pan_mode(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isMiddleMousePanMode(JNIEnv*, jclass, jlong ptr) {
    return te_is_middle_mouse_pan_mode(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setTextLeftMargin(JNIEnv*, jclass, jlong ptr, jlong value) {
    te_set_text_left_margin(reinterpret_cast<te_editor*>(ptr), static_cast<size_t>(value));
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getTextLeftMargin(JNIEnv*, jclass, jlong ptr) {
    return static_cast<jlong>(te_get_text_left_margin(reinterpret_cast<te_editor*>(ptr)));
}

// ---- Language & palette ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setLanguage(JNIEnv*, jclass, jlong ptr, jint language) {
    te_set_language(reinterpret_cast<te_editor*>(ptr), language);
}

JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getLanguageName(JNIEnv* env, jclass, jlong ptr) {
    return te_string_to_jstring(env, te_get_language_name(reinterpret_cast<te_editor*>(ptr)));
}

JNIEXPORT jint JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getPaletteColor(JNIEnv*, jclass, jlong ptr, jint color) {
    uint32_t out = 0;
    te_get_palette_color(reinterpret_cast<te_editor*>(ptr), color, &out);
    return static_cast<jint>(out);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setPaletteColor(JNIEnv*, jclass, jlong ptr, jint color, jint value) {
    te_set_palette_color(reinterpret_cast<te_editor*>(ptr), color, static_cast<uint32_t>(value));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setDefaultDarkPalette(JNIEnv*, jclass, jlong ptr) {
    te_set_default_dark_palette(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setDefaultLightPalette(JNIEnv*, jclass, jlong ptr) {
    te_set_default_light_palette(reinterpret_cast<te_editor*>(ptr));
}

// ---- Clipboard / undo ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_cut(JNIEnv*, jclass, jlong ptr) {
    te_cut(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_copy(JNIEnv*, jclass, jlong ptr) {
    te_copy(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_paste(JNIEnv*, jclass, jlong ptr) {
    te_paste(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_undo(JNIEnv*, jclass, jlong ptr) {
    te_undo(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_redo(JNIEnv*, jclass, jlong ptr) {
    te_redo(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_canUndo(JNIEnv*, jclass, jlong ptr) {
    return te_can_undo(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_canRedo(JNIEnv*, jclass, jlong ptr) {
    return te_can_redo(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

// ---- Selection ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_selectAll(JNIEnv*, jclass, jlong ptr) {
    te_select_all(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_selectLine(JNIEnv*, jclass, jlong ptr, jlong line) {
    te_select_line(reinterpret_cast<te_editor*>(ptr), static_cast<size_t>(line));
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_anyCursorHasSelection(JNIEnv*, jclass, jlong ptr) {
    return te_any_cursor_has_selection(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getSelectedText(JNIEnv* env, jclass, jlong ptr) {
    return te_string_to_jstring(env, te_get_selected_text(reinterpret_cast<te_editor*>(ptr)));
}

// ---- Find / replace ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_selectFirstOccurrenceOf(JNIEnv* env, jclass, jlong ptr, jstring text, jboolean case_sensitive, jboolean whole_word) {
    std::string text_s = jstring_to_string(env, text);
    te_select_first_occurrence_of(reinterpret_cast<te_editor*>(ptr), text_s.c_str(), case_sensitive == JNI_TRUE, whole_word == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_selectNextOccurrenceOf(JNIEnv* env, jclass, jlong ptr, jstring text, jboolean case_sensitive, jboolean whole_word) {
    std::string text_s = jstring_to_string(env, text);
    te_select_next_occurrence_of(reinterpret_cast<te_editor*>(ptr), text_s.c_str(), case_sensitive == JNI_TRUE, whole_word == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_replaceTextInCurrentCursor(JNIEnv* env, jclass, jlong ptr, jstring text) {
    std::string text_s = jstring_to_string(env, text);
    te_replace_text_in_current_cursor(reinterpret_cast<te_editor*>(ptr), text_s.c_str());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_replaceTextInAllCursors(JNIEnv* env, jclass, jlong ptr, jstring text) {
    std::string text_s = jstring_to_string(env, text);
    te_replace_text_in_all_cursors(reinterpret_cast<te_editor*>(ptr), text_s.c_str());
}

// ---- Cursor / scrolling ----

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getNumberOfCursors(JNIEnv*, jclass, jlong ptr) {
    return static_cast<jlong>(te_get_number_of_cursors(reinterpret_cast<te_editor*>(ptr)));
}

JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getCursorText(JNIEnv* env, jclass, jlong ptr, jlong cursor) {
    return te_string_to_jstring(env, te_get_cursor_text(reinterpret_cast<te_editor*>(ptr), static_cast<size_t>(cursor)));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_scrollToLine(JNIEnv*, jclass, jlong ptr, jlong line, jint alignment) {
    te_scroll_to_line(reinterpret_cast<te_editor*>(ptr), static_cast<size_t>(line), alignment);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setCursorPos(JNIEnv*, jclass, jlong ptr, jlong line, jlong index) {
    te_set_cursor_pos(reinterpret_cast<te_editor*>(ptr), static_cast<size_t>(line), static_cast<size_t>(index));
}

JNIEXPORT jlongArray JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getCursorPos(JNIEnv* env, jclass, jlong ptr) {
    uint64_t line = 0;
    uint64_t index = 0;
    te_get_cursor_pos(reinterpret_cast<te_editor*>(ptr), &line, &index);
    jlongArray out = env->NewLongArray(2);
    if (out == nullptr) {
        return nullptr;
    }
    jlong values[2] = {static_cast<jlong>(line), static_cast<jlong>(index)};
    env->SetLongArrayRegion(out, 0, 2, values);
    return out;
}

JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getLineHeight(JNIEnv*, jclass, jlong ptr) {
    return te_get_line_height(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT jfloat JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getGlyphWidth(JNIEnv*, jclass, jlong ptr) {
    return te_get_glyph_width(reinterpret_cast<te_editor*>(ptr));
}

// ---- Markers ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_addMarker(JNIEnv* env, jclass, jlong ptr, jlong line, jint line_number_color, jint text_color, jstring line_number_tooltip, jstring text_tooltip) {
    std::string line_number_tooltip_s = jstring_to_string(env, line_number_tooltip);
    std::string text_tooltip_s = jstring_to_string(env, text_tooltip);
    te_add_marker(reinterpret_cast<te_editor*>(ptr), static_cast<size_t>(line), static_cast<uint32_t>(line_number_color),
                  static_cast<uint32_t>(text_color), line_number_tooltip_s.empty() ? nullptr : line_number_tooltip_s.c_str(),
                  text_tooltip_s.empty() ? nullptr : text_tooltip_s.c_str());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_clearMarkers(JNIEnv*, jclass, jlong ptr) {
    te_clear_markers(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_hasMarkers(JNIEnv*, jclass, jlong ptr) {
    return te_has_markers(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

} // extern "C" (ColorTextEdit additions)

// =========================================================================
// ColorTextEdit extras additions
// =========================================================================

extern "C" {

// ---- TrieAutoComplete ----

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_autocompleteCreate(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(te_autocomplete_create());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_autocompleteDestroy(JNIEnv*, jclass, jlong ac) {
    te_autocomplete_destroy(reinterpret_cast<te_autocomplete*>(ac));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_autocompleteConnect(JNIEnv*, jclass, jlong ac, jlong editor_ptr) {
    te_autocomplete_connect(reinterpret_cast<te_autocomplete*>(ac), reinterpret_cast<te_editor*>(editor_ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_autocompleteDisconnect(JNIEnv*, jclass, jlong ac) {
    te_autocomplete_disconnect(reinterpret_cast<te_autocomplete*>(ac));
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_autocompleteIsConnected(JNIEnv*, jclass, jlong ac) {
    return te_autocomplete_is_connected(reinterpret_cast<te_autocomplete*>(ac)) ? JNI_TRUE : JNI_FALSE;
}

// ---- Notifications ----

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_notificationsCreate(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(te_notifications_create());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_notificationsDestroy(JNIEnv*, jclass, jlong notifications) {
    te_notifications_destroy(reinterpret_cast<te_notifications*>(notifications));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_notificationsAdd(JNIEnv* env, jclass, jlong notifications, jint type, jstring message, jint dismiss_time_ms) {
    std::string message_s = jstring_to_string(env, message);
    te_notifications_add(reinterpret_cast<te_notifications*>(notifications), type, message_s.c_str(), dismiss_time_ms);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_notificationsRender(JNIEnv*, jclass, jlong notifications, jfloat pos_x, jfloat pos_y) {
    te_notifications_render(reinterpret_cast<te_notifications*>(notifications), pos_x, pos_y);
}

} // extern "C" (ColorTextEdit extras additions)

// =========================================================================
// TextEditor events additions
// =========================================================================

// TextEditor event callbacks are C function pointers plus an opaque user_data.
// The JNI setters install the static trampolines below (user_data carries the
// raw editor pointer) and forward to the Kotlin TextEditorEventsJvmBridge
// object, which dispatches into per-editor registries. The class reference
// and method IDs are cached once from a JNI entry point so the trampolines
// only need an attached JNIEnv.

static JavaVM* g_te_jvm = nullptr;
static jclass g_te_bridge_class = nullptr;
static jmethodID g_te_notify_transaction = nullptr;
static jmethodID g_te_notify_change = nullptr;
static jmethodID g_te_notify_line_number_popup = nullptr;
static jmethodID g_te_notify_text_popup = nullptr;
static jmethodID g_te_notify_hover_popup = nullptr;

// Provides a JNIEnv for the current thread, attaching it to the JVM first if
// needed: callbacks may fire on a thread that is not yet attached.
class ThreadLocalJNIEnv {
public:
    JNIEnv* env = nullptr;
    bool attached = false;

    explicit ThreadLocalJNIEnv(JavaVM* jvm) : jvm_(jvm) {
        if (jvm_ == nullptr) {
            return;
        }
        JNIEnv* check = nullptr;
        if (jvm_->GetEnv(reinterpret_cast<void**>(&check), JNI_VERSION_1_6) == JNI_OK) {
            env = check;
            return;
        }
        JavaVMAttachArgs args = {};
        args.version = JNI_VERSION_1_6;
        // Desktop JDK's JavaVM::AttachCurrentThread takes void**; the Android
        // NDK's takes JNIEnv**. Cast accordingly so both toolchains accept it.
#ifdef __ANDROID__
        if (jvm_->AttachCurrentThread(&env, &args) == JNI_OK) {
#else
        if (jvm_->AttachCurrentThread(reinterpret_cast<void**>(&env), &args) == JNI_OK) {
#endif
            attached = true;
        }
    }

    ~ThreadLocalJNIEnv() {
        if (attached && jvm_ != nullptr) {
            jvm_->DetachCurrentThread();
        }
    }

private:
    JavaVM* jvm_;
};

static void ensure_te_bridge(JNIEnv* env) {
    if (g_te_bridge_class != nullptr) {
        return;
    }
    if (g_te_jvm == nullptr) {
        env->GetJavaVM(&g_te_jvm);
    }
    jclass local = env->FindClass("cn/enaium/imgui/extensions/colortextedit/TextEditorEventsJvmBridge");
    if (local == nullptr) {
        return; // pending exception; bridge not reachable
    }
    g_te_bridge_class = static_cast<jclass>(env->NewGlobalRef(local));
    env->DeleteLocalRef(local);
    g_te_notify_transaction = env->GetStaticMethodID(
        g_te_bridge_class, "notifyTransaction", "(J[Z[J[J[J[J[Ljava/lang/String;)V");
    g_te_notify_change = env->GetStaticMethodID(g_te_bridge_class, "notifyChange", "(J)V");
    g_te_notify_line_number_popup = env->GetStaticMethodID(g_te_bridge_class, "notifyLineNumberPopup", "(JJJ)V");
    g_te_notify_text_popup = env->GetStaticMethodID(g_te_bridge_class, "notifyTextPopup", "(JJJ)V");
    g_te_notify_hover_popup = env->GetStaticMethodID(g_te_bridge_class, "notifyHoverPopup", "(JJJ)V");
}

extern "C" {

// ---- Callback trampolines (C-linkage so they can be installed as te_* callbacks) ----

static void te_jni_notify_transaction(const te_change_batch* batch, void* user_data) {
    ThreadLocalJNIEnv helper(g_te_jvm);
    if (helper.env != nullptr && g_te_bridge_class != nullptr) {
        jlong editor_ptr = reinterpret_cast<jlong>(user_data);
        jsize count = static_cast<jsize>(batch->count);

        jbooleanArray inserts = helper.env->NewBooleanArray(count);
        jlongArray start_lines = helper.env->NewLongArray(count);
        jlongArray start_indexes = helper.env->NewLongArray(count);
        jlongArray end_lines = helper.env->NewLongArray(count);
        jlongArray end_indexes = helper.env->NewLongArray(count);
        jclass string_class = helper.env->FindClass("java/lang/String");
        jobjectArray texts = helper.env->NewObjectArray(count, string_class, nullptr);
        helper.env->DeleteLocalRef(string_class);

        for (jsize i = 0; i < count; i++) {
            const te_text_change& change = batch->changes[i];
            jboolean insert = change.insert ? JNI_TRUE : JNI_FALSE;
            jlong start_line = static_cast<jlong>(change.start_line);
            jlong start_index = static_cast<jlong>(change.start_index);
            jlong end_line = static_cast<jlong>(change.end_line);
            jlong end_index = static_cast<jlong>(change.end_index);
            jstring text = helper.env->NewStringUTF(change.text != nullptr ? change.text : "");
            helper.env->SetBooleanArrayRegion(inserts, i, 1, &insert);
            helper.env->SetLongArrayRegion(start_lines, i, 1, &start_line);
            helper.env->SetLongArrayRegion(start_indexes, i, 1, &start_index);
            helper.env->SetLongArrayRegion(end_lines, i, 1, &end_line);
            helper.env->SetLongArrayRegion(end_indexes, i, 1, &end_index);
            helper.env->SetObjectArrayElement(texts, i, text);
            helper.env->DeleteLocalRef(text);
        }

        helper.env->CallStaticVoidMethod(g_te_bridge_class, g_te_notify_transaction, editor_ptr, inserts,
                                         start_lines, start_indexes, end_lines, end_indexes, texts);

        helper.env->DeleteLocalRef(inserts);
        helper.env->DeleteLocalRef(start_lines);
        helper.env->DeleteLocalRef(start_indexes);
        helper.env->DeleteLocalRef(end_lines);
        helper.env->DeleteLocalRef(end_indexes);
        helper.env->DeleteLocalRef(texts);
    }
    // The C side allocates the batch and hands it to the callback; the
    // binding must release it after converting the data.
    te_change_batch_free(const_cast<te_change_batch*>(batch));
}

static void te_jni_notify_change(void* user_data) {
    ThreadLocalJNIEnv helper(g_te_jvm);
    if (helper.env == nullptr || g_te_bridge_class == nullptr) {
        return;
    }
    helper.env->CallStaticVoidMethod(g_te_bridge_class, g_te_notify_change, reinterpret_cast<jlong>(user_data));
}

static void te_jni_notify_line_number_popup(const te_popup_data* data, void* user_data) {
    ThreadLocalJNIEnv helper(g_te_jvm);
    if (helper.env == nullptr || g_te_bridge_class == nullptr) {
        return;
    }
    helper.env->CallStaticVoidMethod(g_te_bridge_class, g_te_notify_line_number_popup,
                                     reinterpret_cast<jlong>(user_data), static_cast<jlong>(data->line),
                                     static_cast<jlong>(data->index));
}

static void te_jni_notify_text_popup(const te_popup_data* data, void* user_data) {
    ThreadLocalJNIEnv helper(g_te_jvm);
    if (helper.env == nullptr || g_te_bridge_class == nullptr) {
        return;
    }
    helper.env->CallStaticVoidMethod(g_te_bridge_class, g_te_notify_text_popup,
                                     reinterpret_cast<jlong>(user_data), static_cast<jlong>(data->line),
                                     static_cast<jlong>(data->index));
}

static void te_jni_notify_hover_popup(const te_popup_data* data, void* user_data) {
    ThreadLocalJNIEnv helper(g_te_jvm);
    if (helper.env == nullptr || g_te_bridge_class == nullptr) {
        return;
    }
    helper.env->CallStaticVoidMethod(g_te_bridge_class, g_te_notify_hover_popup,
                                     reinterpret_cast<jlong>(user_data), static_cast<jlong>(data->line),
                                     static_cast<jlong>(data->index));
}

// ---- Change / transaction callbacks ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setTransactionCallback(JNIEnv* env, jclass, jlong editor_ptr, jboolean activate) {
    ensure_te_bridge(env);
    te_editor* editor = reinterpret_cast<te_editor*>(editor_ptr);
    if (activate == JNI_TRUE) {
        te_set_transaction_callback(editor, te_jni_notify_transaction, reinterpret_cast<void*>(editor_ptr));
    } else {
        te_set_transaction_callback(editor, nullptr, nullptr);
    }
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setChangeCallback(JNIEnv* env, jclass, jlong editor_ptr, jboolean activate, jint delay_ms) {
    ensure_te_bridge(env);
    te_editor* editor = reinterpret_cast<te_editor*>(editor_ptr);
    if (activate == JNI_TRUE) {
        te_set_change_callback(editor, te_jni_notify_change, delay_ms, reinterpret_cast<void*>(editor_ptr));
    } else {
        te_set_change_callback(editor, nullptr, 0, nullptr);
    }
}

// ---- Async autocomplete ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setAutoCompleteSuggestions(JNIEnv* env, jclass, jlong editor_ptr, jobjectArray values) {
    jsize count = values != nullptr ? env->GetArrayLength(values) : 0;
    std::vector<std::string> owning;
    std::vector<const char*> c_values;
    owning.reserve(static_cast<size_t>(count));
    c_values.reserve(static_cast<size_t>(count));
    for (jsize i = 0; i < count; i++) {
        jstring value = static_cast<jstring>(env->GetObjectArrayElement(values, i));
        if (value == nullptr) {
            owning.emplace_back();
            c_values.push_back(owning.back().c_str());
            continue;
        }

        const char* chars = env->GetStringUTFChars(value, nullptr);
        owning.emplace_back(chars != nullptr ? chars : "");
        if (chars != nullptr) {
            env->ReleaseStringUTFChars(value, chars);
        }
        c_values.push_back(owning.back().c_str());
        env->DeleteLocalRef(value);
    }
    te_set_auto_complete_suggestions(reinterpret_cast<te_editor*>(editor_ptr), c_values.data(), static_cast<size_t>(count));
}

// ---- Popup / hover callbacks ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setLineNumberContextMenuCallback(JNIEnv* env, jclass, jlong editor_ptr, jboolean activate) {
    ensure_te_bridge(env);
    te_editor* editor = reinterpret_cast<te_editor*>(editor_ptr);
    if (activate == JNI_TRUE) {
        te_set_line_number_context_menu_callback(editor, te_jni_notify_line_number_popup, reinterpret_cast<void*>(editor_ptr));
    } else {
        te_set_line_number_context_menu_callback(editor, nullptr, nullptr);
    }
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setTextContextMenuCallback(JNIEnv* env, jclass, jlong editor_ptr, jboolean activate) {
    ensure_te_bridge(env);
    te_editor* editor = reinterpret_cast<te_editor*>(editor_ptr);
    if (activate == JNI_TRUE) {
        te_set_text_context_menu_callback(editor, te_jni_notify_text_popup, reinterpret_cast<void*>(editor_ptr));
    } else {
        te_set_text_context_menu_callback(editor, nullptr, nullptr);
    }
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setTextHoverCallback(JNIEnv* env, jclass, jlong editor_ptr, jboolean activate) {
    ensure_te_bridge(env);
    te_editor* editor = reinterpret_cast<te_editor*>(editor_ptr);
    if (activate == JNI_TRUE) {
        te_set_text_hover_callback(editor, te_jni_notify_hover_popup, reinterpret_cast<void*>(editor_ptr));
    } else {
        te_set_text_hover_callback(editor, nullptr, nullptr);
    }
}

// ---- Mouse queries ----

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isMousePosOverGlyph(JNIEnv*, jclass, jlong editor_ptr, jfloat x, jfloat y) {
    return te_is_mouse_pos_over_glyph(reinterpret_cast<te_editor*>(editor_ptr), x, y) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getDocPosAtMousePos(JNIEnv* env, jclass, jlong editor_ptr, jfloat x, jfloat y, jlongArray out) {
    uint64_t line = 0;
    uint64_t index = 0;
    te_get_doc_pos_at_mouse_pos(reinterpret_cast<te_editor*>(editor_ptr), x, y, &line, &index);
    if (out == nullptr) {
        return;
    }
    jlong values[2] = {static_cast<jlong>(line), static_cast<jlong>(index)};
    env->SetLongArrayRegion(out, 0, 2, values);
}

} // extern "C" (TextEditor events additions)

// =========================================================================
// ColorTextEdit completion additions
// =========================================================================

// Autocomplete configuration and line-data hook callbacks are C function
// pointers plus an opaque user_data. The JNI setters below install the
// static trampolines (user_data carries the raw editor pointer) and forward
// to the Kotlin ColorTextEditJvmBridge object, which dispatches into the
// per-editor registries. The class reference, method IDs and field IDs are
// cached once from a JNI entry point so the trampolines only need an
// attached JNIEnv (see ThreadLocalJNIEnv above).

static JavaVM* g_te_completion_jvm = nullptr;
static jclass g_te_completion_bridge_class = nullptr;
static jmethodID g_te_completion_notify_autocomplete = nullptr;
static jmethodID g_te_completion_notify_insertor = nullptr;
static jmethodID g_te_completion_notify_deletor = nullptr;
static jmethodID g_te_completion_notify_iterate = nullptr;
static jclass g_te_completion_result_class = nullptr;
static jfieldID g_te_completion_suggestions_field = nullptr;
static jfieldID g_te_completion_suggestions_promise_field = nullptr;
static jfieldID g_te_completion_labels_promise_field = nullptr;
static jclass g_te_completion_list_class = nullptr;
static jmethodID g_te_completion_list_size = nullptr;
static jmethodID g_te_completion_list_get = nullptr;

static void ensure_te_completion_bridge(JNIEnv* env) {
    if (g_te_completion_bridge_class != nullptr) {
        return;
    }
    if (g_te_completion_jvm == nullptr) {
        env->GetJavaVM(&g_te_completion_jvm);
    }
    jclass local = env->FindClass("cn/enaium/imgui/extensions/colortextedit/ColorTextEditJvmBridge");
    if (local == nullptr) {
        return; // pending exception; bridge not reachable
    }
    g_te_completion_bridge_class = static_cast<jclass>(env->NewGlobalRef(local));
    env->DeleteLocalRef(local);
    g_te_completion_notify_autocomplete = env->GetStaticMethodID(
        g_te_completion_bridge_class, "notifyAutocomplete",
        "(JLjava/lang/String;JJJJZZZZ)Lcn/enaium/imgui/extensions/colortextedit/AutocompleteResult;");
    g_te_completion_notify_insertor = env->GetStaticMethodID(g_te_completion_bridge_class, "notifyInsertor", "(JJ)J");
    g_te_completion_notify_deletor = env->GetStaticMethodID(g_te_completion_bridge_class, "notifyDeletor", "(JJJ)V");
    g_te_completion_notify_iterate = env->GetStaticMethodID(g_te_completion_bridge_class, "notifyIterateUserData", "(JJJ)V");

    jclass result_local = env->FindClass("cn/enaium/imgui/extensions/colortextedit/AutocompleteResult");
    if (result_local != nullptr) {
        g_te_completion_result_class = static_cast<jclass>(env->NewGlobalRef(result_local));
        env->DeleteLocalRef(result_local);
        g_te_completion_suggestions_field = env->GetFieldID(g_te_completion_result_class, "suggestions", "Ljava/util/List;");
        g_te_completion_suggestions_promise_field = env->GetFieldID(g_te_completion_result_class, "suggestionsPromise", "Z");
    }
    jclass list_local = env->FindClass("java/util/List");
    if (list_local != nullptr) {
        g_te_completion_list_class = static_cast<jclass>(env->NewGlobalRef(list_local));
        env->DeleteLocalRef(list_local);
        g_te_completion_list_size = env->GetMethodID(g_te_completion_list_class, "size", "()I");
        g_te_completion_list_get = env->GetMethodID(g_te_completion_list_class, "get", "(I)Ljava/lang/Object;");
    }
}

// Builds a 2-element jlongArray from a uint64_t pair (position results).
static jlongArray te_completion_pos_array(JNIEnv* env, uint64_t first, uint64_t second) {
    jlongArray out = env->NewLongArray(2);
    if (out != nullptr) {
        jlong values[2] = {static_cast<jlong>(first), static_cast<jlong>(second)};
        env->SetLongArrayRegion(out, 0, 2, values);
    }
    return out;
}

// Copies a UTF-8 string into a malloc'd buffer. te_autocomplete_result
// documents suggestions as malloc'd copies, so the trampoline allocates
// fresh buffers per callback invocation.
static char* te_completion_dup_cstr(const char* s) {
    if (s == nullptr) {
        return nullptr;
    }
    const size_t len = std::strlen(s);
    char* copy = static_cast<char*>(std::malloc(len + 1));
    if (copy != nullptr) {
        std::memcpy(copy, s, len + 1);
    }
    return copy;
}

extern "C" {

// ---- Callback trampolines (C-linkage so they can be installed as te_* callbacks) ----


static void te_jni_autocomplete(const te_autocomplete_state* state, te_autocomplete_result* out) {
    out->suggestions = nullptr;
    out->suggestion_count = 0;
    // default to "promised": on ANY failure below the C++ keeps the session
    // alive instead of deactivating the popup on an empty non-promised result
    out->suggestions_promise = true;
    ThreadLocalJNIEnv helper(g_te_completion_jvm);
    if (helper.env == nullptr || g_te_completion_bridge_class == nullptr || g_te_completion_notify_autocomplete == nullptr) {
        return;
    }
    jstring search_term = helper.env->NewStringUTF(state->search_term != nullptr ? state->search_term : "");
    jobject result = helper.env->CallStaticObjectMethod(
        g_te_completion_bridge_class, g_te_completion_notify_autocomplete,
        reinterpret_cast<jlong>(state->user_data),
        search_term,
        static_cast<jlong>(state->search_term_start_line),
        static_cast<jlong>(state->search_term_start_index),
        static_cast<jlong>(state->search_term_end_line),
        static_cast<jlong>(state->search_term_end_index),
        state->in_identifier ? JNI_TRUE : JNI_FALSE,
        state->in_number ? JNI_TRUE : JNI_FALSE,
        state->in_comment ? JNI_TRUE : JNI_FALSE,
        state->in_string ? JNI_TRUE : JNI_FALSE);
    helper.env->DeleteLocalRef(search_term);
    if (helper.env->ExceptionCheck()) {
        // a Kotlin exception must not poison the JVM nor kill the popup;
        // the promise stays true so the session waits for the next update
        helper.env->ExceptionClear();
    }
    if (result == nullptr || g_te_completion_result_class == nullptr) {
        helper.env->DeleteLocalRef(result);
        return;
    }
    jobject list = helper.env->GetObjectField(result, g_te_completion_suggestions_field);
    if (list != nullptr && g_te_completion_list_class != nullptr && g_te_completion_list_size != nullptr && g_te_completion_list_get != nullptr) {
        jint count = helper.env->CallIntMethod(list, g_te_completion_list_size);
        if (count > 0) {
            out->suggestions = static_cast<char**>(std::malloc(sizeof(char*) * static_cast<size_t>(count)));
            if (out->suggestions != nullptr) {
                for (jint i = 0; i < count; i++) {
                    jstring item = static_cast<jstring>(helper.env->CallObjectMethod(list, g_te_completion_list_get, i));
                    if (item == nullptr) {
                        out->suggestions[i] = nullptr;
                        continue;
                    }
                    const char* chars = helper.env->GetStringUTFChars(item, nullptr);
                    out->suggestions[i] = chars != nullptr ? te_completion_dup_cstr(chars) : nullptr;
                    if (chars != nullptr) {
                        helper.env->ReleaseStringUTFChars(item, chars);
                    }
                    helper.env->DeleteLocalRef(item);
                }
            }
        }
        out->suggestion_count = static_cast<size_t>(count);
        helper.env->DeleteLocalRef(list);
    }
    out->suggestions_promise = helper.env->GetBooleanField(result, g_te_completion_suggestions_promise_field) == JNI_TRUE;
    helper.env->DeleteLocalRef(result);
}

static void* te_jni_insertor(uint64_t line, void* user_data) {
    ThreadLocalJNIEnv helper(g_te_completion_jvm);
    if (helper.env == nullptr || g_te_completion_bridge_class == nullptr || g_te_completion_notify_insertor == nullptr) {
        return nullptr;
    }
    jlong result = helper.env->CallStaticLongMethod(g_te_completion_bridge_class, g_te_completion_notify_insertor,
                                                    reinterpret_cast<jlong>(user_data), static_cast<jlong>(line));
    return result == 0 ? nullptr : reinterpret_cast<void*>(static_cast<uintptr_t>(result));
}

static void te_jni_deletor(uint64_t line, void* data, void* user_data) {
    ThreadLocalJNIEnv helper(g_te_completion_jvm);
    if (helper.env == nullptr || g_te_completion_bridge_class == nullptr || g_te_completion_notify_deletor == nullptr) {
        return;
    }
    helper.env->CallStaticVoidMethod(g_te_completion_bridge_class, g_te_completion_notify_deletor,
                                     reinterpret_cast<jlong>(user_data), static_cast<jlong>(line),
                                     reinterpret_cast<jlong>(data));
}

static void te_jni_iterate_user_data(uint64_t line, void* data, void* user_data) {
    ThreadLocalJNIEnv helper(g_te_completion_jvm);
    if (helper.env == nullptr || g_te_completion_bridge_class == nullptr || g_te_completion_notify_iterate == nullptr) {
        return;
    }
    helper.env->CallStaticVoidMethod(g_te_completion_bridge_class, g_te_completion_notify_iterate,
                                     reinterpret_cast<jlong>(user_data), static_cast<jlong>(line),
                                     reinterpret_cast<jlong>(data));
}

// ---- Autocomplete configuration ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setAutoCompleteConfig(JNIEnv* env, jclass, jlong ptr, jboolean activate, jboolean trigger_on_typing, jboolean trigger_on_shortcut, jboolean trigger_in_comments, jboolean trigger_in_strings, jboolean auto_insert_single_suggestions, jint trigger_delay_ms, jlong suggestion_width) {
    ensure_te_completion_bridge(env);
    te_editor* editor = reinterpret_cast<te_editor*>(ptr);
    if (activate == JNI_TRUE) {
        te_set_auto_complete_config(editor, te_jni_autocomplete, reinterpret_cast<void*>(ptr),
                                       trigger_on_typing == JNI_TRUE, trigger_on_shortcut == JNI_TRUE,
                                       trigger_in_comments == JNI_TRUE, trigger_in_strings == JNI_TRUE,
                                       auto_insert_single_suggestions == JNI_TRUE, trigger_delay_ms,
                                       static_cast<unsigned int>(suggestion_width));
    } else {
        te_set_auto_complete_config(editor, nullptr, nullptr, false, false, false, false, false, 0, 0);
    }
}

// ---- Additional text queries and edits ----

JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getSectionText(JNIEnv* env, jclass, jlong ptr, jlong start_line, jlong start_index, jlong end_line, jlong end_index) {
    return te_string_to_jstring(env, te_get_section_text(reinterpret_cast<te_editor*>(ptr),
                                                          static_cast<uint64_t>(start_line), static_cast<uint64_t>(start_index),
                                                          static_cast<uint64_t>(end_line), static_cast<uint64_t>(end_index)));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_replaceSectionText(JNIEnv* env, jclass, jlong ptr, jlong start_line, jlong start_index, jlong end_line, jlong end_index, jstring text) {
    std::string text_s = jstring_to_string(env, text);
    te_replace_section_text(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(start_line),
                            static_cast<uint64_t>(start_index), static_cast<uint64_t>(end_line),
                            static_cast<uint64_t>(end_index), text_s.c_str());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_selectionToLowerCase(JNIEnv*, jclass, jlong ptr) {
    te_selection_to_lower_case(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_selectionToUpperCase(JNIEnv*, jclass, jlong ptr) {
    te_selection_to_upper_case(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_stripTrailingWhitespaces(JNIEnv*, jclass, jlong ptr) {
    te_strip_trailing_whitespaces(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_tabsToSpaces(JNIEnv*, jclass, jlong ptr) {
    te_tabs_to_spaces(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_spacesToTabs(JNIEnv*, jclass, jlong ptr) {
    te_spaces_to_tabs(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_indentLines(JNIEnv*, jclass, jlong ptr) {
    te_indent_lines(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_deindentLines(JNIEnv*, jclass, jlong ptr) {
    te_deindent_lines(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_moveUpLines(JNIEnv*, jclass, jlong ptr) {
    te_move_up_lines(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_moveDownLines(JNIEnv*, jclass, jlong ptr) {
    te_move_down_lines(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_toggleComments(JNIEnv*, jclass, jlong ptr) {
    te_toggle_comments(reinterpret_cast<te_editor*>(ptr));
}

// ---- Additional selection / cursor API ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_selectLines(JNIEnv*, jclass, jlong ptr, jlong start, jlong end) {
    te_select_lines(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(start), static_cast<uint64_t>(end));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_selectRegion(JNIEnv*, jclass, jlong ptr, jlong start_line, jlong start_index, jlong end_line, jlong end_index) {
    te_select_region(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(start_line), static_cast<uint64_t>(start_index),
                     static_cast<uint64_t>(end_line), static_cast<uint64_t>(end_index));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_selectToBrackets(JNIEnv*, jclass, jlong ptr, jboolean include_brackets) {
    te_select_to_brackets(reinterpret_cast<te_editor*>(ptr), include_brackets == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_growSelections(JNIEnv*, jclass, jlong ptr) {
    te_grow_selections(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_shrinkSelections(JNIEnv*, jclass, jlong ptr) {
    te_shrink_selections(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_addNextOccurrence(JNIEnv*, jclass, jlong ptr, jboolean whole_word) {
    te_add_next_occurrence(reinterpret_cast<te_editor*>(ptr), whole_word == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_selectAllOccurrences(JNIEnv*, jclass, jlong ptr, jboolean whole_word) {
    te_select_all_occurrences(reinterpret_cast<te_editor*>(ptr), whole_word == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_clearCursors(JNIEnv*, jclass, jlong ptr) {
    te_clear_cursors(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT jlongArray JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getCursorPosition(JNIEnv* env, jclass, jlong ptr, jlong cursor) {
    uint64_t line = 0;
    uint64_t index = 0;
    te_get_cursor_position(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(cursor), &line, &index);
    return te_completion_pos_array(env, line, index);
}

JNIEXPORT jlongArray JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getCursorSelection(JNIEnv* env, jclass, jlong ptr, jlong cursor) {
    uint64_t start_line = 0;
    uint64_t start_index = 0;
    uint64_t end_line = 0;
    uint64_t end_index = 0;
    te_get_cursor_selection(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(cursor),
                            &start_line, &start_index, &end_line, &end_index);
    jlongArray out = env->NewLongArray(4);
    if (out != nullptr) {
        jlong values[4] = {static_cast<jlong>(start_line), static_cast<jlong>(start_index),
                           static_cast<jlong>(end_line), static_cast<jlong>(end_index)};
        env->SetLongArrayRegion(out, 0, 4, values);
    }
    return out;
}

// ---- Word / find query ----

JNIEXPORT jstring JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getWordAtMousePos(JNIEnv* env, jclass, jlong ptr, jfloat x, jfloat y) {
    return te_string_to_jstring(env, te_get_word_at_mouse_pos(reinterpret_cast<te_editor*>(ptr), x, y));
}

JNIEXPORT jlongArray JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_findWordStart(JNIEnv* env, jclass, jlong ptr, jlong line, jlong index, jboolean whole_word) {
    uint64_t out_line = 0;
    uint64_t out_index = 0;
    te_find_word_start(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(line), static_cast<uint64_t>(index),
                       whole_word == JNI_TRUE, &out_line, &out_index);
    return te_completion_pos_array(env, out_line, out_index);
}

JNIEXPORT jlongArray JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_findWordEnd(JNIEnv* env, jclass, jlong ptr, jlong line, jlong index, jboolean whole_word) {
    uint64_t out_line = 0;
    uint64_t out_index = 0;
    te_find_word_end(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(line), static_cast<uint64_t>(index),
                     whole_word == JNI_TRUE, &out_line, &out_index);
    return te_completion_pos_array(env, out_line, out_index);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_hasFindString(JNIEnv*, jclass, jlong ptr) {
    return te_has_find_string(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_findNext(JNIEnv*, jclass, jlong ptr) {
    te_find_next(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_findAll(JNIEnv*, jclass, jlong ptr) {
    te_find_all(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_openFindReplaceWindow(JNIEnv*, jclass, jlong ptr) {
    te_open_find_replace_window(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_closeFindReplaceWindow(JNIEnv*, jclass, jlong ptr) {
    te_close_find_replace_window(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setFindButtonLabel(JNIEnv* env, jclass, jlong ptr, jstring label) {
    std::string label_s = jstring_to_string(env, label);
    te_set_find_button_label(reinterpret_cast<te_editor*>(ptr), label_s.c_str());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setFindAllButtonLabel(JNIEnv* env, jclass, jlong ptr, jstring label) {
    std::string label_s = jstring_to_string(env, label);
    te_set_find_all_button_label(reinterpret_cast<te_editor*>(ptr), label_s.c_str());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setReplaceButtonLabel(JNIEnv* env, jclass, jlong ptr, jstring label) {
    std::string label_s = jstring_to_string(env, label);
    te_set_replace_button_label(reinterpret_cast<te_editor*>(ptr), label_s.c_str());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setReplaceAllButtonLabel(JNIEnv* env, jclass, jlong ptr, jstring label) {
    std::string label_s = jstring_to_string(env, label);
    te_set_replace_all_button_label(reinterpret_cast<te_editor*>(ptr), label_s.c_str());
}

// ---- Visibility / folding ----

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isMousePosOverTextArea(JNIEnv*, jclass, jlong ptr, jfloat x, jfloat y) {
    return te_is_mouse_pos_over_text_area(reinterpret_cast<te_editor*>(ptr), x, y) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isDocPosVisible(JNIEnv*, jclass, jlong ptr, jlong line, jlong index) {
    return te_is_doc_pos_visible(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(line), static_cast<uint64_t>(index)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isLineFoldable(JNIEnv*, jclass, jlong ptr, jlong line) {
    return te_is_line_foldable(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(line)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isLineFolded(JNIEnv*, jclass, jlong ptr, jlong line) {
    return te_is_line_folded(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(line)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isLineVisible(JNIEnv*, jclass, jlong ptr, jlong line) {
    return te_is_line_visible(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(line)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isLineHidden(JNIEnv*, jclass, jlong ptr, jlong line) {
    return te_is_line_hidden(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(line)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_foldAroundLine(JNIEnv*, jclass, jlong ptr, jlong line) {
    te_fold_around_line(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(line));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_unfoldAroundLine(JNIEnv*, jclass, jlong ptr, jlong line) {
    te_unfold_around_line(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(line));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_toggleAtLine(JNIEnv*, jclass, jlong ptr, jlong line) {
    te_toggle_at_line(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(line));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_unfoldAll(JNIEnv*, jclass, jlong ptr) {
    te_unfold_all(reinterpret_cast<te_editor*>(ptr));
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getFirstVisibleRow(JNIEnv*, jclass, jlong ptr) {
    return static_cast<jlong>(te_get_first_visible_row(reinterpret_cast<te_editor*>(ptr)));
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getFirstVisibleColumn(JNIEnv*, jclass, jlong ptr) {
    return static_cast<jlong>(te_get_first_visible_column(reinterpret_cast<te_editor*>(ptr)));
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getLastVisibleRow(JNIEnv*, jclass, jlong ptr) {
    return static_cast<jlong>(te_get_last_visible_row(reinterpret_cast<te_editor*>(ptr)));
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getLastVisibleColumn(JNIEnv*, jclass, jlong ptr) {
    return static_cast<jlong>(te_get_last_visible_column(reinterpret_cast<te_editor*>(ptr)));
}

// ---- Coordinate transforms ----

JNIEXPORT jlongArray JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_docPosToVisPos(JNIEnv* env, jclass, jlong ptr, jlong line, jlong index) {
    uint64_t row = 0;
    uint64_t column = 0;
    te_doc_pos_to_vis_pos(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(line), static_cast<uint64_t>(index), &row, &column);
    return te_completion_pos_array(env, row, column);
}

JNIEXPORT jlongArray JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_visPosToDocPos(JNIEnv* env, jclass, jlong ptr, jlong row, jlong column) {
    uint64_t line = 0;
    uint64_t index = 0;
    te_vis_pos_to_doc_pos(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(row), static_cast<uint64_t>(column), &line, &index);
    return te_completion_pos_array(env, line, index);
}

// ---- Undo state ----

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getUndoIndex(JNIEnv*, jclass, jlong ptr) {
    return static_cast<jlong>(te_get_undo_index(reinterpret_cast<te_editor*>(ptr)));
}

// ---- Remaining configuration toggles ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setShowSpacesEnabled(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_show_spaces_enabled(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isShowSpacesEnabled(JNIEnv*, jclass, jlong ptr) {
    return te_is_show_spaces_enabled(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setShowTabsEnabled(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_show_tabs_enabled(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isShowTabsEnabled(JNIEnv*, jclass, jlong ptr) {
    return te_is_show_tabs_enabled(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setShowScrollbarMiniMapEnabled(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_show_scrollbar_minimap_enabled(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isShowScrollbarMiniMapEnabled(JNIEnv*, jclass, jlong ptr) {
    return te_is_show_scrollbar_minimap_enabled(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setShowPanScrollIndicatorEnabled(JNIEnv*, jclass, jlong ptr, jboolean value) {
    te_set_show_pan_scroll_indicator_enabled(reinterpret_cast<te_editor*>(ptr), value == JNI_TRUE);
}

JNIEXPORT jboolean JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_isShowPanScrollIndicatorEnabled(JNIEnv*, jclass, jlong ptr) {
    return te_is_show_pan_scroll_indicator_enabled(reinterpret_cast<te_editor*>(ptr)) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setMiniMapColumns(JNIEnv*, jclass, jlong ptr, jlong value) {
    te_set_minimap_columns(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(value));
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getMiniMapColumns(JNIEnv*, jclass, jlong ptr) {
    return static_cast<jlong>(te_get_minimap_columns(reinterpret_cast<te_editor*>(ptr)));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setLineNumberLeftMargin(JNIEnv*, jclass, jlong ptr, jlong value) {
    te_set_line_number_left_margin(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(value));
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getLineNumberLeftMargin(JNIEnv*, jclass, jlong ptr) {
    return static_cast<jlong>(te_get_line_number_left_margin(reinterpret_cast<te_editor*>(ptr)));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setDecorationLeftMargin(JNIEnv*, jclass, jlong ptr, jlong value) {
    te_set_decoration_left_margin(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(value));
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getDecorationLeftMargin(JNIEnv*, jclass, jlong ptr) {
    return static_cast<jlong>(te_get_decoration_left_margin(reinterpret_cast<te_editor*>(ptr)));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setLineBreakConfig(JNIEnv* env, jclass, jlong ptr, jstring break_after, jstring break_before, jboolean use_unicode_annex14) {
    std::string break_after_s = jstring_to_string(env, break_after);
    std::string break_before_s = jstring_to_string(env, break_before);
    te_set_line_break_config(reinterpret_cast<te_editor*>(ptr), break_after_s.c_str(), break_before_s.c_str(),
                             use_unicode_annex14 == JNI_TRUE);
}

// ---- Line data hooks ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setInsertor(JNIEnv* env, jclass, jlong ptr, jboolean activate) {
    ensure_te_completion_bridge(env);
    te_editor* editor = reinterpret_cast<te_editor*>(ptr);
    if (activate == JNI_TRUE) {
        te_set_insertor(editor, te_jni_insertor, reinterpret_cast<void*>(ptr));
    } else {
        te_set_insertor(editor, nullptr, nullptr);
    }
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setDeletor(JNIEnv* env, jclass, jlong ptr, jboolean activate) {
    ensure_te_completion_bridge(env);
    te_editor* editor = reinterpret_cast<te_editor*>(ptr);
    if (activate == JNI_TRUE) {
        te_set_deletor(editor, te_jni_deletor, reinterpret_cast<void*>(ptr));
    } else {
        te_set_deletor(editor, nullptr, nullptr);
    }
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setUserData(JNIEnv*, jclass, jlong ptr, jlong line, jlong data) {
    te_set_user_data(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(line),
                     reinterpret_cast<void*>(static_cast<uintptr_t>(data)));
}

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getUserData(JNIEnv*, jclass, jlong ptr, jlong line) {
    return reinterpret_cast<jlong>(te_get_user_data(reinterpret_cast<te_editor*>(ptr), static_cast<uint64_t>(line)));
}

// Static configuration (no editor instance)
JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setDefaultPalette(JNIEnv*, jclass, jint text, jint keyword, jint number, jint string, jint comment, jint background, jint cursor, jint selection) {
    te_set_default_palette(static_cast<uint32_t>(text), static_cast<uint32_t>(keyword), static_cast<uint32_t>(number),
                           static_cast<uint32_t>(string), static_cast<uint32_t>(comment), static_cast<uint32_t>(background),
                           static_cast<uint32_t>(cursor), static_cast<uint32_t>(selection));
}

JNIEXPORT jlongArray JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_getDefaultPalette(JNIEnv* env, jclass) {
    uint32_t text, keyword, number, string, comment, background, cursor, selection;
    te_get_default_palette(&text, &keyword, &number, &string, &comment, &background, &cursor, &selection);
    jlong values[8] = {text, keyword, number, string, comment, background, cursor, selection};
    jlongArray out = env->NewLongArray(8);
    if (out != nullptr) {
        env->SetLongArrayRegion(out, 0, 8, values);
    }
    return out;
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setImGuiContext(JNIEnv*, jclass, jlong im_gui_context) {
    te_set_im_gui_context(static_cast<uint64_t>(im_gui_context));
}

} // extern "C" (ColorTextEdit completion additions)

// =========================================================================
// Custom tokenizer (LSP semantic tokens)
// =========================================================================

// The te_tokenizer_fn trampoline forwards to the Kotlin
// LanguageTokenizerJvmBridge object, keyed by the editor pointer.
static JavaVM* g_tk_jvm = nullptr;
static jclass g_tk_bridge_class = nullptr;
static jmethodID g_tk_tokenize = nullptr;

static void ensure_tk_bridge(JNIEnv* env) {
    if (g_tk_bridge_class != nullptr) return;
    if (g_tk_jvm == nullptr) env->GetJavaVM(&g_tk_jvm);
    jclass local = env->FindClass("cn/enaium/imgui/extensions/colortextedit/LanguageTokenizerJvmBridge");
    if (local == nullptr) return;
    g_tk_bridge_class = static_cast<jclass>(env->NewGlobalRef(local));
    env->DeleteLocalRef(local);
    g_tk_tokenize = env->GetStaticMethodID(g_tk_bridge_class, "tokenize", "(JJLjava/lang/String;)I");
}

static int64_t te_tk_cb(void* user_data, int64_t line, const char* text, uint32_t length) {
    ThreadLocalJNIEnv helper(g_tk_jvm);
    if (helper.env == nullptr || g_tk_bridge_class == nullptr || g_tk_tokenize == nullptr) {
        return -1;
    }
    std::string owned(text, length);
    jstring jtext = helper.env->NewStringUTF(owned.c_str());
    jint idx = helper.env->CallStaticIntMethod(g_tk_bridge_class, g_tk_tokenize,
                                               reinterpret_cast<jlong>(user_data),
                                               static_cast<jlong>(line), jtext);
    if (helper.env->ExceptionCheck()) {
        helper.env->ExceptionClear();
        helper.env->DeleteLocalRef(jtext);
        return -1; // colorizer must not be interrupted by a Kotlin exception
    }
    helper.env->DeleteLocalRef(jtext);
    return idx;
}

extern "C" {
JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_colortextedit_Jni_setCustomTokenizer(JNIEnv* env, jclass, jlong ptr, jboolean activate) {
    ensure_tk_bridge(env);
    te_editor* editor = reinterpret_cast<te_editor*>(ptr);
    if (activate == JNI_TRUE) {
        te_set_custom_tokenizer(editor, te_tk_cb, reinterpret_cast<void*>(ptr));
    } else {
        te_set_custom_tokenizer(editor, nullptr, nullptr);
    }
}
} // extern "C" (custom tokenizer)

// Markdown additions
// =========================================================================

// Markdown link/tooltip/image callbacks are C function pointers plus an
// opaque user_data. The JNI setters below install the static trampolines
// (user_data carries the raw md_config pointer encoded as a jlong) and
// forward to the Kotlin MarkdownJvmBridge object, which dispatches into
// per-config registries. The class reference, method IDs and field IDs are
// cached once from a JNI entry point so the trampolines only need an
// attached JNIEnv.

static JavaVM* g_md_jvm = nullptr;
static jclass g_md_bridge_class = nullptr;
static jmethodID g_md_notify_link = nullptr;
static jmethodID g_md_notify_tooltip = nullptr;
static jmethodID g_md_notify_image = nullptr;
static jclass g_md_image_data_class = nullptr;
static jclass g_md_vec2_class = nullptr;
static jclass g_md_vec4_class = nullptr;
static jfieldID g_md_is_valid = nullptr;
static jfieldID g_md_use_link_callback = nullptr;
static jfieldID g_md_user_texture_id = nullptr;
static jfieldID g_md_size = nullptr;
static jfieldID g_md_uv0 = nullptr;
static jfieldID g_md_uv1 = nullptr;
static jfieldID g_md_tint_col = nullptr;
static jfieldID g_md_border_col = nullptr;
static jfieldID g_md_bg_col = nullptr;
static jfieldID g_md_vec2_x = nullptr;
static jfieldID g_md_vec2_y = nullptr;
static jfieldID g_md_vec4_x = nullptr;
static jfieldID g_md_vec4_y = nullptr;
static jfieldID g_md_vec4_z = nullptr;
static jfieldID g_md_vec4_w = nullptr;

static void ensure_md_bridge(JNIEnv* env) {
    if (g_md_bridge_class != nullptr) {
        return;
    }
    if (g_md_jvm == nullptr) {
        env->GetJavaVM(&g_md_jvm);
    }
    jclass local = env->FindClass("cn/enaium/imgui/extensions/markdown/MarkdownJvmBridge");
    if (local == nullptr) {
        return; // pending exception; bridge not reachable
    }
    g_md_bridge_class = static_cast<jclass>(env->NewGlobalRef(local));
    env->DeleteLocalRef(local);
    g_md_notify_link = env->GetStaticMethodID(g_md_bridge_class, "notifyLink", "(JLjava/lang/String;Ljava/lang/String;Z)V");
    g_md_notify_tooltip = env->GetStaticMethodID(g_md_bridge_class, "notifyTooltip", "(JLjava/lang/String;Ljava/lang/String;ZLjava/lang/String;)V");
    g_md_notify_image = env->GetStaticMethodID(g_md_bridge_class, "notifyImage", "(JLjava/lang/String;Ljava/lang/String;Z)Lcn/enaium/imgui/extensions/markdown/MarkdownImageData;");

    jclass image_local = env->FindClass("cn/enaium/imgui/extensions/markdown/MarkdownImageData");
    if (image_local != nullptr) {
        g_md_image_data_class = static_cast<jclass>(env->NewGlobalRef(image_local));
        env->DeleteLocalRef(image_local);
        g_md_is_valid = env->GetFieldID(g_md_image_data_class, "isValid", "Z");
        g_md_use_link_callback = env->GetFieldID(g_md_image_data_class, "useLinkCallback", "Z");
        g_md_user_texture_id = env->GetFieldID(g_md_image_data_class, "userTextureId", "J");
        g_md_size = env->GetFieldID(g_md_image_data_class, "size", "Lcn/enaium/imgui/ImVec2;");
        g_md_uv0 = env->GetFieldID(g_md_image_data_class, "uv0", "Lcn/enaium/imgui/ImVec2;");
        g_md_uv1 = env->GetFieldID(g_md_image_data_class, "uv1", "Lcn/enaium/imgui/ImVec2;");
        g_md_tint_col = env->GetFieldID(g_md_image_data_class, "tintCol", "Lcn/enaium/imgui/ImVec4;");
        g_md_border_col = env->GetFieldID(g_md_image_data_class, "borderCol", "Lcn/enaium/imgui/ImVec4;");
        g_md_bg_col = env->GetFieldID(g_md_image_data_class, "bgCol", "Lcn/enaium/imgui/ImVec4;");
    }
    jclass vec2_local = env->FindClass("cn/enaium/imgui/ImVec2");
    if (vec2_local != nullptr) {
        g_md_vec2_class = static_cast<jclass>(env->NewGlobalRef(vec2_local));
        env->DeleteLocalRef(vec2_local);
        g_md_vec2_x = env->GetFieldID(g_md_vec2_class, "x", "F");
        g_md_vec2_y = env->GetFieldID(g_md_vec2_class, "y", "F");
    }
    jclass vec4_local = env->FindClass("cn/enaium/imgui/ImVec4");
    if (vec4_local != nullptr) {
        g_md_vec4_class = static_cast<jclass>(env->NewGlobalRef(vec4_local));
        env->DeleteLocalRef(vec4_local);
        g_md_vec4_x = env->GetFieldID(g_md_vec4_class, "x", "F");
        g_md_vec4_y = env->GetFieldID(g_md_vec4_class, "y", "F");
        g_md_vec4_z = env->GetFieldID(g_md_vec4_class, "z", "F");
        g_md_vec4_w = env->GetFieldID(g_md_vec4_class, "w", "F");
    }
}

// Converts a (possibly non-NUL-terminated) text/link span of the given length
// into a jstring; a null span becomes a null jstring.
static jstring md_span_to_jstring(JNIEnv* env, const char* str, int length) {
    if (str == nullptr) {
        return nullptr;
    }
    if (length <= 0) {
        return env->NewStringUTF("");
    }
    std::string copy(str, static_cast<size_t>(length));
    return env->NewStringUTF(copy.c_str());
}

extern "C" {

// ---- Callback trampolines (C-linkage so they can be installed as md_* callbacks) ----

static void md_jni_link(const md_link_data* d) {
    ThreadLocalJNIEnv helper(g_md_jvm);
    if (helper.env == nullptr || g_md_bridge_class == nullptr || g_md_notify_link == nullptr) {
        return;
    }
    jstring text = md_span_to_jstring(helper.env, d->text, d->text_length);
    jstring link = md_span_to_jstring(helper.env, d->link, d->link_length);
    helper.env->CallStaticVoidMethod(g_md_bridge_class, g_md_notify_link,
                                     reinterpret_cast<jlong>(d->user_data), text, link,
                                     d->is_image ? JNI_TRUE : JNI_FALSE);
    helper.env->DeleteLocalRef(text);
    helper.env->DeleteLocalRef(link);
}

static void md_jni_tooltip(const md_link_data* d, const char* link_icon) {
    ThreadLocalJNIEnv helper(g_md_jvm);
    if (helper.env == nullptr || g_md_bridge_class == nullptr || g_md_notify_tooltip == nullptr) {
        return;
    }
    jstring text = md_span_to_jstring(helper.env, d->text, d->text_length);
    jstring link = md_span_to_jstring(helper.env, d->link, d->link_length);
    jstring icon = link_icon != nullptr ? helper.env->NewStringUTF(link_icon) : nullptr;
    helper.env->CallStaticVoidMethod(g_md_bridge_class, g_md_notify_tooltip,
                                     reinterpret_cast<jlong>(d->user_data), text, link,
                                     d->is_image ? JNI_TRUE : JNI_FALSE, icon);
    helper.env->DeleteLocalRef(text);
    helper.env->DeleteLocalRef(link);
    helper.env->DeleteLocalRef(icon);
}

static md_image_data md_jni_image(const md_link_data* d) {
    ThreadLocalJNIEnv helper(g_md_jvm);
    md_image_data out = {};
    if (helper.env == nullptr || g_md_bridge_class == nullptr || g_md_notify_image == nullptr) {
        return out;
    }
    jstring text = md_span_to_jstring(helper.env, d->text, d->text_length);
    jstring link = md_span_to_jstring(helper.env, d->link, d->link_length);
    jobject obj = helper.env->CallStaticObjectMethod(g_md_bridge_class, g_md_notify_image,
                                                     reinterpret_cast<jlong>(d->user_data), text, link,
                                                     d->is_image ? JNI_TRUE : JNI_FALSE);
    helper.env->DeleteLocalRef(text);
    helper.env->DeleteLocalRef(link);
    if (obj != nullptr) {
        if (g_md_is_valid != nullptr && g_md_vec2_x != nullptr && g_md_vec4_x != nullptr) {
            out.is_valid = helper.env->GetBooleanField(obj, g_md_is_valid) == JNI_TRUE;
            out.use_link_callback = helper.env->GetBooleanField(obj, g_md_use_link_callback) == JNI_TRUE;
            out.user_texture_id = static_cast<uint64_t>(helper.env->GetLongField(obj, g_md_user_texture_id));

            jobject size = helper.env->GetObjectField(obj, g_md_size);
            if (size != nullptr) {
                out.size.x = helper.env->GetFloatField(size, g_md_vec2_x);
                out.size.y = helper.env->GetFloatField(size, g_md_vec2_y);
                helper.env->DeleteLocalRef(size);
            }
            jobject uv0 = helper.env->GetObjectField(obj, g_md_uv0);
            if (uv0 != nullptr) {
                out.uv0.x = helper.env->GetFloatField(uv0, g_md_vec2_x);
                out.uv0.y = helper.env->GetFloatField(uv0, g_md_vec2_y);
                helper.env->DeleteLocalRef(uv0);
            }
            jobject uv1 = helper.env->GetObjectField(obj, g_md_uv1);
            if (uv1 != nullptr) {
                out.uv1.x = helper.env->GetFloatField(uv1, g_md_vec2_x);
                out.uv1.y = helper.env->GetFloatField(uv1, g_md_vec2_y);
                helper.env->DeleteLocalRef(uv1);
            }
            jobject tint = helper.env->GetObjectField(obj, g_md_tint_col);
            if (tint != nullptr) {
                out.tint_col.x = helper.env->GetFloatField(tint, g_md_vec4_x);
                out.tint_col.y = helper.env->GetFloatField(tint, g_md_vec4_y);
                out.tint_col.z = helper.env->GetFloatField(tint, g_md_vec4_z);
                out.tint_col.w = helper.env->GetFloatField(tint, g_md_vec4_w);
                helper.env->DeleteLocalRef(tint);
            }
            jobject border = helper.env->GetObjectField(obj, g_md_border_col);
            if (border != nullptr) {
                out.border_col.x = helper.env->GetFloatField(border, g_md_vec4_x);
                out.border_col.y = helper.env->GetFloatField(border, g_md_vec4_y);
                out.border_col.z = helper.env->GetFloatField(border, g_md_vec4_z);
                out.border_col.w = helper.env->GetFloatField(border, g_md_vec4_w);
                helper.env->DeleteLocalRef(border);
            }
            jobject bg = helper.env->GetObjectField(obj, g_md_bg_col);
            if (bg != nullptr) {
                out.bg_col.x = helper.env->GetFloatField(bg, g_md_vec4_x);
                out.bg_col.y = helper.env->GetFloatField(bg, g_md_vec4_y);
                out.bg_col.z = helper.env->GetFloatField(bg, g_md_vec4_z);
                out.bg_col.w = helper.env->GetFloatField(bg, g_md_vec4_w);
                helper.env->DeleteLocalRef(bg);
            }
        }
        helper.env->DeleteLocalRef(obj);
    }
    return out;
}

// ---- Lifecycle / config ----

JNIEXPORT jlong JNICALL Java_cn_enaium_imgui_extensions_markdown_Jni_create(JNIEnv*, jclass) {
    return reinterpret_cast<jlong>(md_create());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_markdown_Jni_destroy(JNIEnv*, jclass, jlong config_ptr) {
    md_destroy(reinterpret_cast<md_config*>(config_ptr));
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_markdown_Jni_setLinkIcon(JNIEnv* env, jclass, jlong config_ptr, jstring icon) {
    std::string icon_str = jstring_to_string(env, icon);
    md_set_link_icon(reinterpret_cast<md_config*>(config_ptr), icon_str.c_str());
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_markdown_Jni_setHeading(JNIEnv*, jclass, jlong config_ptr, jint level, jlong font, jboolean separator) {
    md_set_heading(reinterpret_cast<md_config*>(config_ptr), level, static_cast<uint64_t>(font), separator == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_markdown_Jni_setFormatFlags(JNIEnv*, jclass, jlong config_ptr, jint flags) {
    md_set_format_flags(reinterpret_cast<md_config*>(config_ptr), flags);
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_markdown_Jni_setLinkCallback(JNIEnv* env, jclass, jlong config_ptr, jboolean activate) {
    ensure_md_bridge(env);
    md_config* config = reinterpret_cast<md_config*>(config_ptr);
    if (activate == JNI_TRUE) {
        md_set_link_callback(config, md_jni_link, reinterpret_cast<void*>(config_ptr));
    } else {
        md_set_link_callback(config, nullptr, nullptr);
    }
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_markdown_Jni_setTooltipCallback(JNIEnv* env, jclass, jlong config_ptr, jboolean activate) {
    ensure_md_bridge(env);
    md_config* config = reinterpret_cast<md_config*>(config_ptr);
    if (activate == JNI_TRUE) {
        md_set_tooltip_callback(config, md_jni_tooltip, reinterpret_cast<void*>(config_ptr));
    } else {
        md_set_tooltip_callback(config, nullptr, nullptr);
    }
}

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_markdown_Jni_setImageCallback(JNIEnv* env, jclass, jlong config_ptr, jboolean activate) {
    ensure_md_bridge(env);
    md_config* config = reinterpret_cast<md_config*>(config_ptr);
    if (activate == JNI_TRUE) {
        md_set_image_callback(config, md_jni_image, reinterpret_cast<void*>(config_ptr));
    } else {
        md_set_image_callback(config, nullptr, nullptr);
    }
}

// ---- Render ----

JNIEXPORT void JNICALL Java_cn_enaium_imgui_extensions_markdown_Jni_render(JNIEnv* env, jclass, jlong config_ptr, jstring markdown) {
    std::string markdown_str = jstring_to_string(env, markdown);
    md_render(reinterpret_cast<md_config*>(config_ptr), markdown_str.c_str(), markdown_str.size());
}

} // extern "C" (Markdown additions)
