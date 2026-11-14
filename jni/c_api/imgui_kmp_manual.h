/* Hand-written Emscripten glue that has no mechanical counterpart.
 * Included by the generated imgui_kmp_shim.cpp. */
#pragma once
#include "implot3d_c.h"

/* implot3d_style is 76 floats on the Kotlin side (see jni_bridge.cpp's
 * read_style3d_to_floats): 19 scalars, 14 RGBA colors, 1 colormap. */
#define IMGUI_KMP_STYLE3D_FLOATS 76

static void imgui_kmp_style3d_to_floats(const implot3d_style& style, float* out) {
    int i = 0;
    out[i++] = style.line_weight;
    out[i++] = (float)style.marker;
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
    out[i++] = (float)style.colormap;
}

static void imgui_kmp_style3d_from_floats(implot3d_style& style, const float* in) {
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

extern "C" {

void imgui_kmp_implot3d_getStyle(float* out) {
    implot3d_style style;
    implot3d_get_style(&style);
    imgui_kmp_style3d_to_floats(style, out);
}

/* kind: 0 auto, 1 dark, 2 light, 3 classic. A null `in` edits the current
 * style in place (the Kotlin API passes the same rules as the JNI macro). */
void imgui_kmp_i3d_style_colors(int kind, const float* in, float* out) {
    implot3d_style style;
    if (in) imgui_kmp_style3d_from_floats(style, in);
    implot3d_style* dst = in ? &style : nullptr;
    switch (kind) {
        case 0: implot3d_style_colors_auto(dst); break;
        case 1: implot3d_style_colors_dark(dst); break;
        case 2: implot3d_style_colors_light(dst); break;
        default: implot3d_style_colors_classic(dst); break;
    }
    if (out && in) imgui_kmp_style3d_to_floats(style, out);
}

void imgui_kmp_implot3d_setStyle(const float* in) {
    implot3d_style style;
    imgui_kmp_style3d_from_floats(style, in);
    implot3d_set_style(&style);
}

}  /* extern "C" */
