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
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/*
 * Glue between the Emscripten-compiled imgui module (imgui_wasm.js) and
 * Kotlin/Wasm. Instantiates the module and publishes every generated shim /
 * C API function as a global, so the Kotlin `external` declarations in
 * wasmJsMain can call them directly.
 *
 * Marshaling conventions (see imgui_kmp_shim.c and gen_wasm_shim.py):
 *   - scalars pass straight through (numbers); opaque handles and pointers are
 *     plain 32-bit integers;
 *   - `imgui_vec2`/`imgui_vec4` parameters are flattened into floats by the
 *     shim, struct returns become out-parameters written into heap buffers;
 *   - strings are allocated with imguiKmpAllocString / read back with
 *     imguiKmpReadString (the caller frees them);
 *   - arrays are copied through imguiKmpHeapU8/F32/32 views over the heap;
 *   - the clipboard setters are replaced by a static buffer inside the shim,
 *     installed by imguiKmpInstallClipboard().
 *
 * The host page loads this module and calls initImguiKmp() BEFORE the Kotlin
 * module runs, exactly like sdl-kmp's wasm glue.
 */

import { IMGUI_KMP_EXPORTS } from './imgui_kmp_exports.js';

export async function initImguiKmp(options = {}) {
    const mod = await import('./imgui_wasm.js');
    const load = typeof mod.default === 'function' ? mod.default : mod;
    const Module = await load(options);

    const malloc = Module._malloc;
    const free = Module._free;

    globalThis.imguiKmpMalloc = (bytes) => malloc(bytes);
    globalThis.imguiKmpFree = (ptr) => { if (ptr) free(ptr); };

    globalThis.imguiKmpAllocString = (s) => {
        if (s === null || s === undefined) return 0;
        const size = Module.lengthBytesUTF8(s) + 1;
        const p = malloc(size);
        Module.stringToUTF8(s, p, size);
        return p;
    };
    globalThis.imguiKmpReadString = (ptr) => (ptr ? Module.UTF8ToString(ptr) : null);

    /* Element getters: Kotlin/Wasm cannot index JS typed arrays, so scalar
     * reads go through the heap directly. */
    globalThis.imguiKmpReadF32 = (ptr, index) => Module.HEAPF32[(ptr >> 2) + index];
    globalThis.imguiKmpReadI32 = (ptr, index) => Module.HEAP32[(ptr >> 2) + index];
    globalThis.imguiKmpReadU8 = (ptr, index) => Module.HEAPU8[ptr + index];
    globalThis.imguiKmpReadU16 = (ptr, index) => Module.HEAPU16[ptr / 2 + index];

    /* Bulk set: typed arrays are copied into the heap before the call. */
    globalThis.imguiKmpSetHeapU8 = (ptr, bytes) => Module.HEAPU8.set(bytes, ptr);
    globalThis.imguiKmpSetHeapF32 = (ptr, values) => Module.HEAPF32.set(values, ptr >> 2);
    globalThis.imguiKmpSetHeapI32 = (ptr, values) => Module.HEAP32.set(values, ptr >> 2);

    for (const name of IMGUI_KMP_EXPORTS) {
        globalThis[name] = Module['_' + name];
    }

    globalThis.imguiKmpInstallClipboard = () => Module._imgui_kmp_install_clipboard();

    return Module;
}
