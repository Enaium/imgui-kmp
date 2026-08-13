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
 * Helper header for Kotlin/Native cinterop.
 * Provides struct definitions for opaque types that are only forward-declared
 * in the main C API headers. These definitions are used only by cinterop to
 * generate proper Kotlin types; the actual struct layout is defined in the
 * C++ implementation.
 */
#ifndef CINTEROP_HELPERS_H_
#define CINTEROP_HELPERS_H_

#include "imgui_c.h"
#include "implot_c.h"

/* Dummy struct definitions for cinterop type generation */
struct imgui_context { void* impl; };
struct imgui_io { void* impl; };
struct imgui_style { void* impl; };
struct imgui_draw_data { void* impl; };
struct imgui_draw_list { void* impl; };
struct imgui_draw_cmd { void* impl; };
struct imgui_font { void* impl; };
struct imgui_font_atlas { void* impl; };
struct implot_context { void* impl; };

#endif /* CINTEROP_HELPERS_H_ */
