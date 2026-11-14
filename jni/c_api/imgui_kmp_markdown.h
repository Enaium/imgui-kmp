/* Hand-written Emscripten glue for the ImGuiMarkdown callbacks.
 *
 * C function pointers cannot target Kotlin on the wasm boundary, so the
 * trampolines only record the callback payload into a bounded queue; the
 * Kotlin side drains it after md_render and dispatches to the registered
 * lambdas. The queue restarts once everything has been consumed. */
#pragma once
#include <stdint.h>
#include <string.h>
#include "markdown_c.h"

typedef struct {
    int kind;    /* 0 = link, 1 = tooltip, 2 = image */
    int config;
    int is_image;
    char text[256];
    char link[256];
    char icon[128];
} imgui_kmp_md_event;

#define IMGUI_KMP_MD_EVENT_CAP 64
static imgui_kmp_md_event imgui_kmp_md_events[IMGUI_KMP_MD_EVENT_CAP];
static int imgui_kmp_md_event_count;
static int imgui_kmp_md_event_drained;
static md_image_data imgui_kmp_md_image;

static void imgui_kmp_md_copy(char* dst, size_t cap, const char* src, int len) {
    if (!src) { dst[0] = 0; return; }
    size_t n = (len > 0) ? (size_t)len : strlen(src);
    if (n >= cap) n = cap - 1;
    memcpy(dst, src, n);
    dst[n] = 0;
}

static void imgui_kmp_md_record(const md_link_data* d, int kind, const char* icon) {
    if (imgui_kmp_md_event_drained >= imgui_kmp_md_event_count) {
        imgui_kmp_md_event_count = 0;
        imgui_kmp_md_event_drained = 0;
    }
    if (imgui_kmp_md_event_count >= IMGUI_KMP_MD_EVENT_CAP) return;
    imgui_kmp_md_event* e = &imgui_kmp_md_events[imgui_kmp_md_event_count++];
    e->kind = kind;
    e->config = d ? (int)(uintptr_t)d->user_data : 0;
    e->is_image = (d && d->is_image) ? 1 : 0;
    imgui_kmp_md_copy(e->text, sizeof(e->text), d ? d->text : 0, d ? d->text_length : 0);
    imgui_kmp_md_copy(e->link, sizeof(e->link), d ? d->link : 0, d ? d->link_length : 0);
    imgui_kmp_md_copy(e->icon, sizeof(e->icon), icon, 0);
}

static void imgui_kmp_md_on_link(const md_link_data* d) { imgui_kmp_md_record(d, 0, 0); }
static void imgui_kmp_md_on_tooltip(const md_link_data* d, const char* icon) { imgui_kmp_md_record(d, 1, icon); }
static md_image_data imgui_kmp_md_on_image(const md_link_data* d) {
    imgui_kmp_md_record(d, 2, 0);
    return imgui_kmp_md_image;
}

extern "C" {

void imgui_kmp_md_install(md_config* config, int kind) {
    void* user = (void*)(uintptr_t)config;
    if (kind == 0) md_set_link_callback(config, imgui_kmp_md_on_link, user);
    else if (kind == 1) md_set_tooltip_callback(config, imgui_kmp_md_on_tooltip, user);
    else md_set_image_callback(config, imgui_kmp_md_on_image, user);
}

void imgui_kmp_md_uninstall(md_config* config, int kind) {
    if (kind == 0) md_set_link_callback(config, nullptr, nullptr);
    else if (kind == 1) md_set_tooltip_callback(config, nullptr, nullptr);
    else md_set_image_callback(config, nullptr, nullptr);
}

/* Returns the number of events queued since the previous drain. */
int imgui_kmp_md_drain(void) {
    int n = imgui_kmp_md_event_count - imgui_kmp_md_event_drained;
    imgui_kmp_md_event_drained = imgui_kmp_md_event_count;
    return n;
}

int imgui_kmp_md_event_total(void) { return imgui_kmp_md_event_count; }
int imgui_kmp_md_event_kind(int i) { return imgui_kmp_md_events[i].kind; }
int imgui_kmp_md_event_config(int i) { return imgui_kmp_md_events[i].config; }
int imgui_kmp_md_event_is_image(int i) { return imgui_kmp_md_events[i].is_image; }
const char* imgui_kmp_md_event_text(int i) { return imgui_kmp_md_events[i].text; }
const char* imgui_kmp_md_event_link(int i) { return imgui_kmp_md_events[i].link; }
const char* imgui_kmp_md_event_icon(int i) { return imgui_kmp_md_events[i].icon; }

/* Image data is static: recorded events only reach Kotlin after the render
 * pass, so a produced image shows from the next frame on. */
void imgui_kmp_md_set_image(int tex, float w, float h) {
    memset(&imgui_kmp_md_image, 0, sizeof(imgui_kmp_md_image));
    imgui_kmp_md_image.is_valid = true;
    imgui_kmp_md_image.user_texture_id = (uint64_t)(uintptr_t)(unsigned int)tex;
    imgui_kmp_md_image.size = imgui_vec2{w, h};
    imgui_kmp_md_image.uv0 = imgui_vec2{0, 0};
    imgui_kmp_md_image.uv1 = imgui_vec2{1, 1};
    imgui_kmp_md_image.tint_col = imgui_vec4{1, 1, 1, 1};
}

}  /* extern "C" */
