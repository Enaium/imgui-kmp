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
#include "imgui_markdown.h"

#include <cstring>
#include <string>

#include "markdown_c.h"

namespace {
    struct md_state {
        ImGui::MarkdownConfig config;
        std::string link_icon; // owned copy; config.linkIcon points into it
        md_link_callback_fn fn_link = nullptr;
        md_tooltip_callback_fn fn_tooltip = nullptr;
        md_image_callback_fn fn_image = nullptr;
        void* user_data = nullptr;
    };

    void link_cb(ImGui::MarkdownLinkCallbackData data) {
        md_state* s = static_cast<md_state*>(data.userData);
        if (s == nullptr || s->fn_link == nullptr) {
            return;
        }
        md_link_data out;
        out.text = data.text;
        out.text_length = data.textLength;
        out.link = data.link;
        out.link_length = data.linkLength;
        out.user_data = s->user_data;
        out.is_image = data.isImage ? true : false;
        s->fn_link(&out);
    }

    void tooltip_cb(ImGui::MarkdownTooltipCallbackData data) {
        md_state* s = static_cast<md_state*>(data.linkData.userData);
        if (s == nullptr || s->fn_tooltip == nullptr) {
            return;
        }
        md_link_data out;
        out.text = data.linkData.text;
        out.text_length = data.linkData.textLength;
        out.link = data.linkData.link;
        out.link_length = data.linkData.linkLength;
        out.user_data = s->user_data;
        out.is_image = data.linkData.isImage ? true : false;
        s->fn_tooltip(&out, data.linkIcon);
    }

    ImGui::MarkdownImageData image_cb(ImGui::MarkdownLinkCallbackData data) {
        md_state* s = static_cast<md_state*>(data.userData);
        ImGui::MarkdownImageData out;
        if (s == nullptr || s->fn_image == nullptr) {
            return out;
        }
        md_link_data in;
        in.text = data.text;
        in.text_length = data.textLength;
        in.link = data.link;
        in.link_length = data.linkLength;
        in.user_data = s->user_data;
        in.is_image = data.isImage ? true : false;
        const md_image_data r = s->fn_image(&in);
        out.isValid = r.is_valid ? true : false;
        out.useLinkCallback = r.use_link_callback ? true : false;
        out.user_texture_id = r.user_texture_id;
        out.size = ImVec2(r.size.x, r.size.y);
        out.uv0 = ImVec2(r.uv0.x, r.uv0.y);
        out.uv1 = ImVec2(r.uv1.x, r.uv1.y);
        out.tint_col = ImVec4(r.tint_col.x, r.tint_col.y, r.tint_col.z, r.tint_col.w);
        out.border_col = ImVec4(r.border_col.x, r.border_col.y, r.border_col.z, r.border_col.w);
        out.bg_col = ImVec4(r.bg_col.x, r.bg_col.y, r.bg_col.z, r.bg_col.w);
        return out;
    }
}

extern "C" {

md_config* md_create(void) {
    md_state* s = new md_state();
    s->config.linkCallback = link_cb;
    s->config.tooltipCallback = tooltip_cb;
    s->config.imageCallback = image_cb;
    s->config.userData = s;
    return reinterpret_cast<md_config*>(s);
}

void md_destroy(md_config* config) {
    delete reinterpret_cast<md_state*>(config);
}

void md_set_link_icon(md_config* config, const char* icon) {
    md_state* s = reinterpret_cast<md_state*>(config);
    s->link_icon = icon != nullptr ? icon : "";
    s->config.linkIcon = s->link_icon.c_str();
}

void md_set_heading(md_config* config, int level, uint64_t font, bool separator) {
    md_state* s = reinterpret_cast<md_state*>(config);
    if (level < 0 || level >= ImGui::MarkdownConfig::NUMHEADINGS) {
        return;
    }
    s->config.headingFormats[level].font = reinterpret_cast<ImFont*>(font);
    s->config.headingFormats[level].separator = separator ? true : false;
}

void md_set_format_flags(md_config* config, int flags) {
    reinterpret_cast<md_state*>(config)->config.formatFlags =
        static_cast<ImGuiMarkdownFormatFlags>(flags);
}

void md_set_link_callback(md_config* config, md_link_callback_fn fn, void* user_data) {
    md_state* s = reinterpret_cast<md_state*>(config);
    s->fn_link = fn;
    s->user_data = user_data;
}

void md_set_tooltip_callback(md_config* config, md_tooltip_callback_fn fn, void* user_data) {
    md_state* s = reinterpret_cast<md_state*>(config);
    s->fn_tooltip = fn;
    s->user_data = user_data;
}

void md_set_image_callback(md_config* config, md_image_callback_fn fn, void* user_data) {
    md_state* s = reinterpret_cast<md_state*>(config);
    s->fn_image = fn;
    s->user_data = user_data;
}

void md_render(md_config* config, const char* markdown, uint32_t length) {
    md_state* s = reinterpret_cast<md_state*>(config);
    ImGui::Markdown(markdown != nullptr ? markdown : "", length, s->config);
}

} // extern "C"