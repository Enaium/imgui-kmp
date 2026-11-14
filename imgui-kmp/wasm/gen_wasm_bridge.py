#!/usr/bin/env python3
"""Generates the wasmJs bridge (`actual object Jni`) + the JS externals from the
JVM JNI declarations and the C API headers.

The JVM `Jni` object is a flat, JNI-shaped mapping of the C API
(`imgui-kmp/src/jvmMain/.../Jni.jvm.kt`). The shared adapters in
`src/jniMain` are written against that shape, so the wasm bridge has to expose
the same Kotlin signatures; this script emits bodies that marshal Kotlin
values onto the Emscripten heap and call the generated shims/exported C
functions.

Marshaling:
  Long            -> 32-bit wasm pointer (`.toInt()` / `.toLong()`)
  Boolean         -> i32 (0/1)
  String/String?  -> UTF-8 buffer (allocated here, freed after the call)
  *Array          -> heap buffer, copied in (and back out for out-parameters)
  BooleanArray?   -> 1-byte buffer, copied back
  FloatArray ret  -> scratch buffer sized from the C return type (vec2/vec4)
"""

import re
import sys
from pathlib import Path

def load_struct_specials() -> dict:
    import json
    f = Path("imgui-kmp/wasm/implot3d_points.json")
    return {e["jvm"]: e for e in json.loads(f.read_text())} if f.is_file() else {}


STRUCT_SPECIALS = load_struct_specials()


OUT = Path("imgui-kmp/src/wasmJsMain/kotlin/cn/enaium/imgui")
PACKAGE = "cn.enaium.imgui"
C_PREFIX = "imgui_"
JVM_SRC = Path("imgui-kmp/src/jvmMain/kotlin/cn/enaium/imgui/Jni.jvm.kt")
HEADER = Path("jni/c_api/imgui_c.h")
HEADERS = [HEADER]

# JVM bridge name -> C API name, where the acronym rules cannot infer it.
OVERRIDES = {
    "vSliderFloat": "imgui_vslider_float",
    "vSliderInt": "imgui_vslider_int",
    "getIO": "imgui_get_io",
    "showIDStackToolWindow": "imgui_show_id_stack_tool_window",
    "setNextWindowDockID": "imgui_set_next_window_dock_id",
    "logToTTY": "imgui_log_to_tty",
    "textLinkOpenURL": "imgui_text_link_open_url",
    "getItemID": "imgui_get_item_id",
    "getID": "imgui_get_id",
    "colorConvertRGBtoHSV": "imgui_color_convert_rgb_to_hsv",
    "colorConvertHSVtoRGB": "imgui_color_convert_hsv_to_rgb",
    "getDragDropPayload": "imgui_get_drag_drop_payload_type",
    "fontsAddFontDefaultConfig": "imgui_font_atlas_add_font_default_cfg",
    "fontsAddFontFromFileTTFConfig": "imgui_font_atlas_add_font_from_file_ttf_cfg",
    "drawListGetVtxCurrentIdx": "imgui_draw_list_vtx_current_idx",
}

# Functions without a direct C API counterpart: hand-written bodies below.
# C parameters derived from a Kotlin argument (count of an array, ...).
TRAILING_ARGS = {
    "addColormap": ["cols.size"],
    "plotMesh": ["vtxXs.size", "idxs.size"],
    "addColormapVec4": ["cols.size"],
    "addColormapU32": ["cols.size"],
}

SPECIAL = {
    "getSelectedNodes",
    "getSelectedLinks",
    "getActionContextNodes",
    "getActionContextLinks",
    "getOrderedNodeIds",
    *STRUCT_SPECIALS,
    "getPlotLimits",
    "getPlotMousePos",
    "pixelsToPlot",
    "getPlotSelection",
    "listBox",
    "combo",
    "dockBuilderSplitNode",
    "setDragDropPayload",
    "acceptDragDropPayload",
    "inputText",
    "inputTextMultiline",
    "inputTextWithHint",
    "isMousePosValid",
    "drawListAddPolyline",
    "drawListCopyVtx",
    "drawListCopyIdx",
    "fontsGetTexDataAsRGBA32",
    "setClipboardFunctions",
}

VALUE_STRUCTS = {"imgui_vec2": ["x", "y"], "imgui_vec4": ["x", "y", "z", "w"]}


def snake(name: str) -> str:
    # Acronym-aware snake_case: "getIO" -> "get_io", "showIDStack" -> "show_id_stack".
    s = re.sub(r"([A-Z]+)([A-Z][a-z])", r"\1_\2", name)
    s = re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", s)
    s = s.lower()
    if s.startswith("fonts_"):
        s = "font_atlas_" + s[len("fonts_"):]
    return C_PREFIX + s


def strip_comments(text: str) -> str:
    return re.sub(r"//[^\n]*", "", re.sub(r"/\*.*?\*/", "", text, flags=re.S))


def parse_c_api() -> dict:
    text = strip_comments(HEADER.read_text())
    funcs = {}
    for m in re.finditer(r"\b(" + re.escape(C_PREFIX) + r"\w+)\s*\(([^;{]*?)\)\s*;", text, re.S):
        name, params = m.group(1), m.group(2)
        # return type is the token sequence before the name on the same statement
        start = text.rfind(";", 0, m.start())
        head = text[start + 1:m.start()].strip()
        ret = " ".join(head.split())
        params = params.strip()
        plist = []
        if params and params != "void":
            depth, cur = 0, ""
            for ch in params:
                if ch in "([":
                    depth += 1
                elif ch in ")]":
                    depth -= 1
                if ch == "," and depth == 0:
                    plist.append(cur.strip())
                    cur = ""
                else:
                    cur += ch
            if cur.strip():
                plist.append(cur.strip())
        ptypes = []
        for p in plist:
            pm = re.match(r"(.*?)\b([A-Za-z_]\w*)\s*$", p, re.S)
            ptypes.append(" ".join(pm.group(1).split()) if pm else p)
        funcs[name] = {"params": ptypes, "ret": ret}
    return funcs


def parse_body(body: str) -> list:
    decls = []
    i = 0
    while True:
        m = re.compile(r"\n\s*actual external fun ").search(body, i)
        if not m:
            break
        j = body.index("(", m.end())
        depth, p = 0, j
        while True:
            if body[p] == "(":
                depth += 1
            elif body[p] == ")":
                depth -= 1
                if depth == 0:
                    break
            p += 1
        rest = body[p + 1:]
        rm = re.match(r"\s*:\s*([^\n]+)", rest)
        ret = " ".join(rm.group(1).split()) if rm else "Unit"
        end = p + 1 + (rm.end() if rm else 0)
        name = body[m.end():j].strip()
        params = body[j + 1:p]
        plist = []
        depth2, cur = 0, ""
        for ch in params:
            if ch in "(<[":
                depth2 += 1
            elif ch in ")>]":
                depth2 -= 1
            if ch == "," and depth2 == 0:
                plist.append(cur.strip())
                cur = ""
            else:
                cur += ch
        if cur.strip():
            plist.append(cur.strip())
        parsed = []
        for pdecl in plist:
            nm, ty = pdecl.split(":", 1)
            parsed.append((nm.strip(), ty.strip()))
        decls.append({"name": name, "params": parsed, "ret": ret})
        i = end
    return decls


def wrapped_params(func: dict) -> list:
    """C parameter list with value structs flattened (empty if not wrapped)."""
    out = []
    for t in func["params"]:
        if t in VALUE_STRUCTS:
            out.extend("float" for _ in VALUE_STRUCTS[t])
        elif t == "uint64_t":
            out.append("uint32_t")
        else:
            out.append(t)
    return out


def needs_wrapper(cname: str, func: dict) -> bool:
    if func["ret"] in VALUE_STRUCTS or func["ret"] == "uint64_t":
        return True
    return any(t in VALUE_STRUCTS or t == "uint64_t" for t in func["params"])



_K_SHAPE = {
    "Double": "D", "Float": "F", "Int": "I", "Boolean": "B", "Long": "L",
    "String": "S", "String?": "S",
    "FloatArray": "FA", "FloatArray?": "FA", "IntArray": "IA", "ByteArray": "PA",
    "LongArray": "LA", "LongArray?": "LA", "DoubleArray": "DA", "DoubleArray?": "DA",
    "BooleanArray": "BA", "BooleanArray?": "BA",
    "Array<String>": "SA", "Array<String>?": "SA",
}

_SCALARS = {"double": "D", "float": "F", "int": "I", "uint32_t": "I", "int32_t": "I",
            "bool": "B", "uint64_t": "L", "int64_t": "L"}
_POINTERS = {"double": "DA", "float": "FA", "int": "IA", "uint32_t": "IA", "int32_t": "IA",
             "uint64_t": "LA", "int64_t": "LA", "bool": "BA", "char": "S"}


def c_shape(t: str) -> str:
    """Coarse shape of a C parameter, used to align it with the Kotlin one."""
    s = " ".join(t.split())
    if s.count("*") >= 2:
        return "SA"
    if s in ("const char*", "char*"):
        return "S"
    if s.startswith("const "):
        s = s[len("const "):]
    if s == "imgui_vec2":
        return "FF"
    if s == "imgui_vec4":
        return "FFFF"
    if s.endswith("*"):
        return _POINTERS.get(s[:-1].strip(), "P")
    return _SCALARS.get(s, "P")


def flat_structs(func: dict) -> tuple:
    """(flattened params, struct return fields count)."""
    params = []
    for t in func["params"]:
        if t in VALUE_STRUCTS:
            params.extend("float" for _ in VALUE_STRUCTS[t])
        elif t == "uint64_t":
            params.append("u32")
        else:
            params.append(t)
    ret_fields = (
        len(VALUE_STRUCTS[func["ret"]]) if func["ret"] in VALUE_STRUCTS
        else (1 if func["ret"] == "uint64_t" else 0)
    )
    return params, ret_fields


def externals_for(decls, c_api):
    """Dedup external declarations keyed by C name."""
    out = {}

    def add(name, params, ret):
        sig = (tuple(params), ret)
        if name in out and out[name] != sig:
            # A hand-written special may report a different return type than
            # the header implies; the header wins.
            return
        out[name] = sig

    for d in decls:
        cname = OVERRIDES.get(d["name"], snake(d["name"]))
        if d["name"] in SPECIAL and cname not in c_api:
            # Shim-only specials (clipboard install) have no C header entry.
            continue
        func = c_api[cname]
        wparams, ret_fields = flat_structs(func)
        kparams, kret = [], kret_type(d["ret"], func)
        # Kotlin-facing param types map 1:1; pointers/structs become Int/Float.
        for t in wparams:
            t = t.replace("const ", "").strip()
            if t in ("float",):
                kparams.append("Float")
            elif t in ("double",):
                kparams.append("Double")
            else:
                kparams.append("Int")
        out_ret = kret_type(d["ret"], func)
        add(cname, kparams, out_ret)
        if needs_wrapper(cname, func):
            # shim wrappers add one Int out-parameter per returned struct field
            # gen_wasm_shim.py names wrappers imgui_kmp_ + the C name with the
            # imgui_ prefix dropped (implot_ names stay intact).
            shim = "imgui_kmp_" + (cname[len("imgui_"):] if cname.startswith("imgui_") else cname)
            struct_ret = func["ret"] in VALUE_STRUCTS or func["ret"] == "uint64_t"
            add(shim, kparams + ["Int"] * ret_fields, "Unit" if struct_ret else kret_type(d["ret"], func))
    # Hand-written implot3d struct wrappers (jni/c_api/imgui_kmp_structs.h).
    for _name, _e in STRUCT_SPECIALS.items():
        _k = [p.split(":", 1)[1].strip() for p in _e["kparams"].split(",")]
        _k = [{"Double": "Double", "Float": "Float", "Int": "Int", "Boolean": "Int",
               "Long": "Int", "String": "Int", "String?": "Int"}.get(x, "Int") for x in _k]
        if _e["out"]:
            add("imgui_kmp_i3d_" + _name, _k + ["Int"], "Unit")
        else:
            _ret = {"bool": "Int", "double": "Double", "float": "Float"}.get(_e.get("cret", ""), "Unit")
            add("imgui_kmp_i3d_" + _name, _k, _ret)

    # ImPlot3D mesh primitives (pointer + count pairs) used by the specials.
    for _mesh in ("cube_vtx", "cube_idx", "sphere_vtx", "sphere_idx", "duck_vtx", "duck_idx"):
        add(f"implot3d_{_mesh}", [], "Int")
        add(f"implot3d_{_mesh}_count", [], "Int")

    # implot3d_style packing (jni/c_api/imgui_kmp_manual.h).
    add("imgui_kmp_implot3d_getStyle", ["Int"], "Unit")
    add("imgui_kmp_implot3d_setStyle", ["Int"], "Unit")
    add("imgui_kmp_i3d_style_colors", ["Int", "Int", "Int"], "Unit")

    # C functions the hand-written special bodies call directly.
    add("imgui_list_box", ["Int", "Int", "Int", "Int"], "Int")
    add("imgui_combo", ["Int", "Int", "Int", "Int"], "Int")
    add("imgui_dock_builder_split_node", ["Int", "Int", "Float", "Int", "Int"], "Unit")
    add("imgui_set_drag_drop_payload", ["Int", "Int", "Int", "Int"], "Int")
    add("imgui_accept_drag_drop_payload", ["Int", "Int", "Int"], "Int")
    add("imgui_draw_list_get_vtx_data", ["Int"], "Int")
    add("imgui_draw_list_get_idx_data", ["Int"], "Int")
    add("imgui_font_atlas_get_tex_data_as_rgba32", ["Int", "Int", "Int", "Int", "Int"], "Unit")
    add("imgui_get_drag_drop_payload_type", [], "Int")
    add("imgui_kmp_install_clipboard", [], "Unit")
    add("imgui_input_text", ["Int", "Int", "Int", "Int"], "Int")
    add("imgui_kmp_input_text_multiline", ["Int", "Int", "Int", "Float", "Float", "Int"], "Int")
    add("imgui_input_text_with_hint", ["Int", "Int", "Int", "Int", "Int"], "Int")
    add("imgui_is_mouse_pos_valid", ["Int"], "Int")
    add("imgui_draw_list_add_polyline", ["Int", "Int", "Int", "Int", "Int", "Float"], "Unit")
    return out


def kret_type(kotlin_ret: str, func: dict):
    """wasm-level return type for an external."""
    if kotlin_ret == "Unit":
        return "Unit"
    if kotlin_ret == "Boolean":
        return "Int"
    if kotlin_ret == "Int":
        return "Int"
    if kotlin_ret == "Float":
        return "Float"
    if kotlin_ret == "Double":
        return "Double"
    if kotlin_ret == "Long":
        return "Int"
    if kotlin_ret in ("String", "String?"):
        return "Int"
    return "Int"


def parse_jvm_decls() -> dict:
    """{object name: declarations} for every `internal actual object Jni*`."""
    src = JVM_SRC.read_text()
    out = {}
    for m in re.finditer(r"internal actual object (Jni\w*) \{", src):
        start = m.end()
        depth, k = 1, start
        while depth:
            if src[k] == "{":
                depth += 1
            elif src[k] == "}":
                depth -= 1
            k += 1
        out[m.group(1)] = parse_body(src[start:k - 1])
    return out


def emit(objects, c_api, externals):
    lines = []
    w = lines.append

    w("/* Generated by imgui-kmp/wasm/gen_wasm_bridge.py - do not edit. */")
    w(f"package {PACKAGE}")
    w("")
    w("import org.khronos.webgl.Float32Array")
    w("import org.khronos.webgl.Int32Array")
    w("import org.khronos.webgl.Int8Array")
    w("import org.khronos.webgl.toFloat32Array")
    w("import org.khronos.webgl.toInt32Array")
    w("import org.khronos.webgl.toInt8Array")
    w("")
    w("// ==========================================================================")
    w("// JS glue (imgui_kmp_glue.js) + Emscripten module exports")
    w("// ==========================================================================")
    w("")
    for name, (params, ret) in sorted(externals.items()):
        sig = ", ".join(f"p{i}: {t}" for i, t in enumerate(params))
        r = "" if ret == "Unit" else f": {ret}"
        w(f"private external fun {name}({sig}){r}")
    w("")
    w("private external fun imguiKmpMalloc(bytes: Int): Int")
    w("private external fun imguiKmpFree(ptr: Int)")
    w("private external fun imguiKmpAllocString(value: String?): Int")
    w("private external fun imguiKmpReadString(ptr: Int): String?")
    w("private external fun imguiKmpSetHeapF32(ptr: Int, values: Float32Array)")
    w("private external fun imguiKmpSetHeapI32(ptr: Int, values: Int32Array)")
    w("private external fun imguiKmpSetHeapU8(ptr: Int, values: Int8Array)")
    w("private external fun imguiKmpReadF32(ptr: Int, index: Int): Float")
    w("private external fun imguiKmpReadI32(ptr: Int, index: Int): Int")
    w("private external fun imguiKmpReadU8(ptr: Int, index: Int): Int")
    w("private external fun imguiKmpReadU16(ptr: Int, index: Int): Int")
    w("")
    w("private fun readDoubleAt(ptr: Int, byteOffset: Int): Double =")
    w("    Double.fromBits(imguiKmpReadI32(ptr + byteOffset, 0).toUInt().toLong() or (imguiKmpReadI32(ptr + byteOffset + 4, 0).toLong() shl 32))")
    w("")
    w("private fun readUtf8(ptr: Int, max: Int): String {")
    w("    var end = 0")
    w("    while (end < max && imguiKmpReadU8(ptr, end) != 0) end++")
    w("    return ByteArray(end) { imguiKmpReadU8(ptr, it).toByte() }.decodeToString()")
    w("}")
    w("")
    for obj, decls in objects.items():
        w(f"internal actual object {obj} {{")
        for d in decls:
            w(emit_body(d, c_api))
        w("}")
    return "\n".join(lines) + "\n"


def emit_body(d, c_api) -> str:
    name, params, ret = d["name"], d["params"], d["ret"]
    ind = "    "
    if name in SPECIAL:
        return SPECIAL_BODIES[name](d, c_api, ind)
    cname = OVERRIDES.get(name, snake(name))
    func = c_api[cname]
    wparams, ret_fields = flat_structs(func)
    wrapped = needs_wrapper(cname, func)
    target = ("imgui_kmp_" + (cname[len("imgui_"):] if cname.startswith("imgui_") else cname)) if wrapped else cname

    pre, post, args, cleanup = [], [], [], []
    for idx, (pname, ptype) in enumerate(params):
        if ptype in ("Float", "Int", "Double"):
            args.append(pname)
        elif ptype == "Boolean":
            args.append(f"if ({pname}) 1 else 0")
        elif ptype == "Long":
            args.append(f"{pname}.toInt()")
        elif ptype in ("String", "String?"):
            pre.append(f"val s{idx} = imguiKmpAllocString({pname})")
            cleanup.append(f"imguiKmpFree(s{idx})")
            args.append(f"s{idx}")
        elif ptype in ("FloatArray", "IntArray", "ByteArray"):
            size = {"FloatArray": 4, "IntArray": 4, "ByteArray": 1}[ptype]
            setter = {"FloatArray": "imguiKmpSetHeapF32", "IntArray": "imguiKmpSetHeapI32", "ByteArray": "imguiKmpSetHeapU8"}[ptype]
            heap = {"FloatArray": "F32", "IntArray": "I32", "ByteArray": "U8"}[ptype]
            to_typed = {"FloatArray": "toFloat32Array", "IntArray": "toInt32Array", "ByteArray": "toInt8Array"}[ptype]
            reader = {"FloatArray": "imguiKmpReadF32", "IntArray": "imguiKmpReadI32", "ByteArray": "imguiKmpReadU8"}[ptype]
            cast = ".toByte()" if ptype == "ByteArray" else ""
            pre.append(f"val a{idx} = imguiKmpMalloc({pname}.size * {size})")
            pre.append(f"{setter}(a{idx}, {pname}.{to_typed}())")
            post.append(f"for (i in {pname}.indices) {pname}[i] = {reader}(a{idx}, i){cast}")
            cleanup.append(f"imguiKmpFree(a{idx})")
            args.append(f"a{idx}")
        elif ptype in ("Array<String>", "Array<String>?"):
            if ptype.endswith("?"):
                pre.append(f"val p{idx} = if ({pname} == null) 0 else imguiKmpMalloc({pname}.size * 4)")
                pre.append(f"val strs{idx} = if ({pname} == null) null else IntArray({pname}.size) {{ imguiKmpAllocString({pname}[it]) }}")
                pre.append(f"if ({pname} != null) imguiKmpSetHeapI32(p{idx}, strs{idx}!!.toInt32Array())")
                cleanup.append(f"if ({pname} != null) {{ strs{idx}!!.forEach {{ imguiKmpFree(it) }}; imguiKmpFree(p{idx}) }}")
            else:
                pre.append(f"val p{idx} = imguiKmpMalloc({pname}.size * 4)")
                pre.append(f"val strs{idx} = IntArray({pname}.size) {{ imguiKmpAllocString({pname}[it]) }}")
                pre.append(f"imguiKmpSetHeapI32(p{idx}, strs{idx}.toInt32Array())")
                cleanup.append(f"strs{idx}.forEach {{ imguiKmpFree(it) }}")
                cleanup.append(f"imguiKmpFree(p{idx})")
            args.append(f"p{idx}")
        elif ptype in ("DoubleArray?", "FloatArray?"):
            optional = True
            is_double = ptype.startswith("Double")
            size = 8 if is_double else 4
            pre.append(f"val a{idx} = if ({pname} == null) 0 else imguiKmpMalloc({pname}.size * {size})")
            if is_double:
                pre.append(
                    f"if ({pname} != null) imguiKmpSetHeapI32(a{idx}, IntArray({pname}.size * 2) {{ i -> "
                    f"val bits = {pname}[i / 2].toRawBits(); "
                    f"if (i % 2 == 0) bits.toInt() else (bits shr 32).toInt() }}.toInt32Array())"
                )
                post.append(
                    f"if ({pname} != null) for (i in {pname}.indices) {pname}[i] = Double.fromBits("
                    f"imguiKmpReadI32(a{idx}, i * 2).toUInt().toLong() or (imguiKmpReadI32(a{idx}, i * 2 + 1).toLong() shl 32))"
                )
            else:
                pre.append(f"if ({pname} != null) imguiKmpSetHeapF32(a{idx}, {pname}.toFloat32Array())")
                post.append(f"if ({pname} != null) for (i in {pname}.indices) {pname}[i] = imguiKmpReadF32(a{idx}, i)")
            cleanup.append(f"if ({pname} != null) imguiKmpFree(a{idx})")
            args.append(f"a{idx}")
        elif ptype == "LongArray":
            pre.append(f"val a{idx} = imguiKmpMalloc({pname}.size * 8)")
            pre.append(
                f"imguiKmpSetHeapI32(a{idx}, IntArray({pname}.size * 2) {{ i -> "
                f"val value = {pname}[i / 2]; if (i % 2 == 0) value.toInt() else (value shr 32).toInt() }}.toInt32Array())"
            )
            post.append(
                f"for (i in {pname}.indices) {pname}[i] = "
                f"imguiKmpReadI32(a{idx}, i * 2).toUInt().toLong() or (imguiKmpReadI32(a{idx}, i * 2 + 1).toLong() shl 32)"
            )
            cleanup.append(f"imguiKmpFree(a{idx})")
            args.append(f"a{idx}")
        elif ptype == "LongArray?":
            pre.append(f"val a{idx} = if ({pname} == null) 0 else imguiKmpMalloc({pname}.size * 8)")
            pre.append(
                f"if ({pname} != null) imguiKmpSetHeapI32(a{idx}, IntArray({pname}.size * 2) {{ i -> "
                f"val value = {pname}[i / 2]; if (i % 2 == 0) value.toInt() else (value shr 32).toInt() }}.toInt32Array())"
            )
            post.append(
                f"if ({pname} != null) for (i in {pname}.indices) {pname}[i] = "
                f"imguiKmpReadI32(a{idx}, i * 2).toUInt().toLong() or (imguiKmpReadI32(a{idx}, i * 2 + 1).toLong() shl 32)"
            )
            cleanup.append(f"if ({pname} != null) imguiKmpFree(a{idx})")
            args.append(f"a{idx}")
        elif ptype == "DoubleArray":
            pre.append(f"val a{idx} = imguiKmpMalloc({pname}.size * 8)")
            pre.append(
                f"imguiKmpSetHeapI32(a{idx}, IntArray({pname}.size * 2) {{ i -> "
                f"val bits = {pname}[i / 2].toRawBits(); "
                f"if (i % 2 == 0) bits.toInt() else (bits shr 32).toInt() }}.toInt32Array())"
            )
            post.append(
                f"for (i in {pname}.indices) {pname}[i] = Double.fromBits("
                f"imguiKmpReadI32(a{idx}, i * 2).toUInt().toLong() or (imguiKmpReadI32(a{idx}, i * 2 + 1).toLong() shl 32))"
            )
            cleanup.append(f"imguiKmpFree(a{idx})")
            args.append(f"a{idx}")
        elif ptype in ("BooleanArray", "BooleanArray?"):
            optional = ptype.endswith("?")
            if optional:
                pre.append(f"val b{idx} = if ({pname} == null) 0 else imguiKmpMalloc(1)")
                pre.append(f"if ({pname} != null) imguiKmpSetHeapU8(b{idx}, ByteArray(1) {{ if ({pname}[0]) 1 else 0 }}.toInt8Array())")
                post.append(f"if ({pname} != null) {pname}[0] = imguiKmpReadU8(b{idx}, 0) != 0")
                cleanup.append(f"if ({pname} != null) imguiKmpFree(b{idx})")
            else:
                pre.append(f"val b{idx} = imguiKmpMalloc(1)")
                pre.append(f"imguiKmpSetHeapU8(b{idx}, ByteArray(1) {{ if ({pname}[0]) 1 else 0 }}.toInt8Array())")
                post.append(f"{pname}[0] = imguiKmpReadU8(b{idx}, 0) != 0")
                cleanup.append(f"imguiKmpFree(b{idx})")
            args.append(f"b{idx}")
        else:
            raise SystemExit(f"{name}: unhandled param type {ptype}")

    # The C declaration's parameter order is authoritative. When it is a
    # permutation of the Kotlin one (implot_setup_axis_ticks puts tick_count
    # before labels), reorder the arguments accordingly - passing positionally
    # would hand a pointer over as an int and vice versa.
    k_shapes = [_K_SHAPE.get(t, "P") for _, t in params]
    c_shapes = [c_shape(t) for t in func["params"]]
    if c_shapes != k_shapes and sorted(c_shapes) == sorted(k_shapes):
        used = [False] * len(args)
        reordered = []
        for shape in c_shapes:
            for i, ks in enumerate(k_shapes):
                if not used[i] and ks == shape:
                    used[i] = True
                    reordered.append(args[i])
                    break
            else:
                raise SystemExit(f"{name}: no Kotlin argument matches C parameter shape {shape}")
        args = reordered

    # C parameters derived from Kotlin arguments (e.g. array lengths).
    args.extend(TRAILING_ARGS.get(name, []))

    # return handling
    ret_lines = []
    scratch = []
    if ret == "ImVec2":
        scratch.append("val r = imguiKmpMalloc(2 * 4)")
        args.extend(["r", "r + 4"])
        cleanup.insert(0, "imguiKmpFree(r)")
        wrapper_or_direct = target
        ret_lines = [
            f"{wrapper_or_direct}({', '.join(args)})",
            "val vx = imguiKmpReadF32(r, 0)",
            "val vy = imguiKmpReadF32(r, 1)",
            "return ImVec2(vx, vy)",
        ]
        sig = ", ".join(f"{p}: {t}" for p, t in params)
        body = [f"{ind}actual fun {name}({sig}): ImVec2 {{"]
        for l in pre + scratch:
            body.append(f"{ind * 2}{l}")
        body.append(f"{ind * 2}try {{")
        for l in ret_lines:
            body.append(f"{ind * 3}{l}")
        body.append(f"{ind * 2}}} finally {{")
        for l in post + cleanup:
            body.append(f"{ind * 3}{l}")
        body.append(f"{ind * 2}}}")
        body.append(f"{ind}}}")
        return "\n".join(body)

    if ret in ("FloatArray",):
        if func["ret"] in VALUE_STRUCTS:
            # struct return -> shim writes the fields through out-parameters
            n = len(VALUE_STRUCTS[func["ret"]])
        else:
            # `void f(float* out_x, ...)` style getters
            n = sum(1 for t in func["params"] if t.strip() == "float*")
        if n == 0:
            raise SystemExit(f"{name}: cannot size FloatArray return")
        scratch.append(f"val r = imguiKmpMalloc({n} * 4)")
        args.extend(f"r + {4 * i}" for i in range(n))
        cleanup.insert(0, "imguiKmpFree(r)")
        ret_lines = [
            f"{target}({', '.join(args)})",
            f"return FloatArray({n}) {{ imguiKmpReadF32(r, it) }}",
        ]
    elif ret == "Long" and func["ret"] == "uint64_t":
        scratch.append("val r = imguiKmpMalloc(4)")
        args.append("r")
        cleanup.insert(0, "imguiKmpFree(r)")
        ret_lines = [
            f"{target}({', '.join(args)})",
            "return imguiKmpReadI32(r, 0).toUInt().toLong()",
        ]
    elif ret == "Long":
        ret_lines = [f"return {target}({', '.join(args)}).toLong()"]
    elif ret == "String":
        ret_lines = [f'return imguiKmpReadString({target}({', '.join(args)})) ?: ""']
    elif ret == "String?":
        ret_lines = [f"return imguiKmpReadString({target}({', '.join(args)}))"]
    elif ret == "Boolean":
        ret_lines = [f"return {target}({', '.join(args)}) != 0"]
    elif ret in ("Unit",):
        ret_lines = [f"{target}({', '.join(args)})"]
    else:
        ret_lines = [f"return {target}({', '.join(args)})"]

    sig = ", ".join(f"{p}: {t}" for p, t in params)
    body = [f"{ind}actual fun {name}({sig})" + ("" if ret in ("Unit",) else f": {ret}") + " {"]
    for l in pre + scratch:
        body.append(f"{ind * 2}{l}")
    if post or cleanup:
        body.append(f"{ind * 2}try {{")
        for l in ret_lines:
            body.append(f"{ind * 3}{l}")
        body.append(f"{ind * 2}}} finally {{")
        for l in post + cleanup:
            body.append(f"{ind * 3}{l}")
        body.append(f"{ind * 2}}}")
    else:
        for l in ret_lines:
            body.append(f"{ind * 2}{l}")
    body.append(f"{ind}}}")
    return "\n".join(body)


SPECIAL_BODIES = {}

_STRING_LIST = """actual fun %(name)s(label: String, currentItem: IntArray, items: Array<String>): Boolean {
    val labelPtr = imguiKmpAllocString(label)
    val itemPtrs = imguiKmpMalloc(items.size * 4)
    val strings = IntArray(items.size) { imguiKmpAllocString(items[it]) }
    val current = imguiKmpMalloc(4)
    try {
        imguiKmpSetHeapI32(itemPtrs, strings.toInt32Array())
        imguiKmpSetHeapI32(current, currentItem.toInt32Array())
        return %(cname)s(labelPtr, current, itemPtrs, items.size) != 0
    } finally {
        currentItem[0] = imguiKmpReadI32(current, 0)
        imguiKmpFree(current)
        for (s in strings) imguiKmpFree(s)
        imguiKmpFree(itemPtrs)
        imguiKmpFree(labelPtr)
    }
}"""
SPECIAL_BODIES["listBox"] = lambda d, c_api, ind: ind + (_STRING_LIST % {"name": "listBox", "cname": "imgui_list_box"}).replace("\n", "\n" + ind)
SPECIAL_BODIES["combo"] = lambda d, c_api, ind: ind + (_STRING_LIST % {"name": "combo", "cname": "imgui_combo"}).replace("\n", "\n" + ind)


def _special(name, body):
    SPECIAL_BODIES[name] = (lambda d, c_api, ind: (ind + body.replace("\n", "\n" + ind)).rstrip())


_special("drawListCopyVtx", """actual fun drawListCopyVtx(list: Long, vtxOffset: Int, vtxCount: Int, positions: FloatArray, uvs: FloatArray, colors: IntArray) {
    // imgui_draw_vert: {float pos_x, pos_y, uv_x, uv_y; uint32_t col} = 20 bytes
    val base = imgui_draw_list_get_vtx_data(list.toInt())
    for (i in 0 until vtxCount) {
        val o = (vtxOffset + i) * 5
        positions[i * 2] = imguiKmpReadF32(base, o)
        positions[i * 2 + 1] = imguiKmpReadF32(base, o + 1)
        uvs[i * 2] = imguiKmpReadF32(base, o + 2)
        uvs[i * 2 + 1] = imguiKmpReadF32(base, o + 3)
        colors[i] = imguiKmpReadI32(base, o + 4)
    }
}""")

_special("drawListCopyIdx", """actual fun drawListCopyIdx(list: Long, idxOffset: Int, idxCount: Int, out: IntArray) {
    val base = imgui_draw_list_get_idx_data(list.toInt())
    for (i in 0 until idxCount) {
        out[i] = imguiKmpReadU16(base, idxOffset + i)
    }
}""")

_special("fontsGetTexDataAsRGBA32", """actual fun fontsGetTexDataAsRGBA32(atlas: Long, outDims: IntArray): ByteArray {
    val scratch = imguiKmpMalloc(16)
    try {
        imgui_font_atlas_get_tex_data_as_rgba32(atlas.toInt(), scratch, scratch + 4, scratch + 8, scratch + 12)
        val pixels = imguiKmpReadI32(scratch, 0)
        val width = imguiKmpReadI32(scratch, 1)
        val height = imguiKmpReadI32(scratch, 2)
        val bpp = imguiKmpReadI32(scratch, 3)
        val size = width * height * bpp
        outDims[0] = width
        outDims[1] = height
        outDims[2] = bpp
        return ByteArray(size) { imguiKmpReadU8(pixels, it).toByte() }
    } finally {
        imguiKmpFree(scratch)
    }
}""")

_special("dockBuilderSplitNode", """actual fun dockBuilderSplitNode(nodeId: Int, splitDir: Int, ratio: Float): Long {
    val out = imguiKmpMalloc(8)
    try {
        imgui_dock_builder_split_node(nodeId, splitDir, ratio, out, out + 4)
        val atDir = imguiKmpReadI32(out, 0)
        val opposite = imguiKmpReadI32(out, 1)
        return atDir.toUInt().toLong() or (opposite.toUInt().toLong() shl 32)
    } finally {
        imguiKmpFree(out)
    }
}""")

_special("setDragDropPayload", """actual fun setDragDropPayload(type: String, data: ByteArray, cond: Int): Boolean {
    val label = imguiKmpAllocString(type)
    val payload = imguiKmpMalloc(data.size)
    imguiKmpSetHeapU8(payload, data.toInt8Array())
    try {
        return imgui_set_drag_drop_payload(label, payload, data.size, cond) != 0
    } finally {
        imguiKmpFree(payload)
        imguiKmpFree(label)
    }
}""")

_special("acceptDragDropPayload", """actual fun acceptDragDropPayload(type: String, flags: Int): ByteArray? {
    val label = imguiKmpAllocString(type)
    val size = imguiKmpMalloc(4)
    try {
        val ptr = imgui_accept_drag_drop_payload(label, flags, size)
        val length = imguiKmpReadI32(size, 0)
        if (ptr == 0 || length <= 0) return null
        return ByteArray(length) { imguiKmpReadU8(ptr, it).toByte() }
    } finally {
        imguiKmpFree(size)
        imguiKmpFree(label)
    }
}""")

_special("inputText", """actual fun inputText(label: String, buf: String, flags: Int): String? {
    val labelPtr = imguiKmpAllocString(label)
    val initial = buf.encodeToByteArray()
    val size = initial.size + 4096
    val buffer = imguiKmpMalloc(size)
    imguiKmpSetHeapU8(buffer, initial.toInt8Array())
    try {
        return if (imgui_input_text(labelPtr, buffer, size, flags) != 0) readUtf8(buffer, size) else null
    } finally {
        imguiKmpFree(buffer)
        imguiKmpFree(labelPtr)
    }
}""")

_special("inputTextMultiline", """actual fun inputTextMultiline(label: String, buf: String, w: Float, h: Float, flags: Int): String? {
    val labelPtr = imguiKmpAllocString(label)
    val initial = buf.encodeToByteArray()
    val size = initial.size + 4096
    val buffer = imguiKmpMalloc(size)
    imguiKmpSetHeapU8(buffer, initial.toInt8Array())
    try {
        return if (imgui_kmp_input_text_multiline(labelPtr, buffer, size, w, h, flags) != 0) readUtf8(buffer, size) else null
    } finally {
        imguiKmpFree(buffer)
        imguiKmpFree(labelPtr)
    }
}""")

_special("inputTextWithHint", """actual fun inputTextWithHint(label: String, hint: String, buf: String, flags: Int): String? {
    val labelPtr = imguiKmpAllocString(label)
    val hintPtr = imguiKmpAllocString(hint)
    val initial = buf.encodeToByteArray()
    val size = initial.size + 4096
    val buffer = imguiKmpMalloc(size)
    imguiKmpSetHeapU8(buffer, initial.toInt8Array())
    try {
        return if (imgui_input_text_with_hint(labelPtr, hintPtr, buffer, size, flags) != 0) readUtf8(buffer, size) else null
    } finally {
        imguiKmpFree(buffer)
        imguiKmpFree(hintPtr)
        imguiKmpFree(labelPtr)
    }
}""")

_special("isMousePosValid", """actual fun isMousePosValid(hasPos: Boolean, x: Float, y: Float): Boolean {
    if (!hasPos) return imgui_is_mouse_pos_valid(0) != 0
    val pos = imguiKmpMalloc(8)
    try {
        imguiKmpSetHeapF32(pos, floatArrayOf(x, y).toFloat32Array())
        return imgui_is_mouse_pos_valid(pos) != 0
    } finally {
        imguiKmpFree(pos)
    }
}""")

_special("drawListAddPolyline", """actual fun drawListAddPolyline(list: Long, points: FloatArray, col: Int, closed: Boolean, thickness: Float) {
    // imgui_vec2 is {float x, y}: the interleaved floats already have that layout.
    val pts = imguiKmpMalloc(points.size * 4)
    imguiKmpSetHeapF32(pts, points.toFloat32Array())
    try {
        imgui_draw_list_add_polyline(list.toInt(), pts, points.size / 2, col, if (closed) 1 else 0, thickness)
    } finally {
        imguiKmpFree(pts)
    }
}""")

_special("setClipboardFunctions", """actual fun setClipboardFunctions() {
    imgui_kmp_install_clipboard()
}""")


def main() -> int:
    global HEADER, HEADERS, OUT, PACKAGE, JVM_SRC, OVERRIDES, C_PREFIX
    if len(sys.argv) >= 5:
        # <jvm-file> <c-header[,c-header...]> <out-dir> <package> [prefix]
        JVM_SRC = Path(sys.argv[1])
        HEADERS = [Path(h) for h in sys.argv[2].split(",")]
        OUT = Path(sys.argv[3])
        PACKAGE = sys.argv[4]
        C_PREFIX = sys.argv[5] if len(sys.argv) > 5 else "imgui_"
        if C_PREFIX == "md_":
            SPECIAL.update({"setLinkCallback", "setTooltipCallback", "setImageCallback", "render"})
        if C_PREFIX == "implot3d_":
            # implot3d_style is packed by hand (imgui_kmp_manual.h).
            SPECIAL.update({"getStyle", "setStyle", "styleColorsAuto", "styleColorsDark",
                            "styleColorsLight", "styleColorsClassic",
                            "cubeVtx", "cubeIdx", "sphereVtx", "sphereIdx", "duckVtx", "duckIdx"})
        OVERRIDES = {
            "plotHistogram2D": "implot_plot_histogram_2d",
            "plotHistogram2D3D": "implot3d_plot_histogram_2d",
            "pointIsNaN": "implot3d_point_is_nan",
            "getStyleColor": "implot3d_get_style_color_vec4",
            "setShowMiniMapEnabled": "te_set_show_minimap_enabled",
            "isShowMiniMapEnabled": "te_is_show_minimap_enabled",
            "setShowScrollbarMiniMapEnabled": "te_set_show_scrollbar_minimap_enabled",
            "isShowScrollbarMiniMapEnabled": "te_is_show_scrollbar_minimap_enabled",
            "setMiniMapColumns": "te_set_minimap_columns",
            "getMiniMapColumns": "te_get_minimap_columns",
            "queryNewLink": "ne_query_new_link_styled",
            "queryNewNode": "ne_query_new_node_styled",
            "beginCreate": "ne_begin_create",
            "beginDelete": "ne_begin_delete",
            "acceptNewItem": "ne_accept_new_item_ex",
            "rejectNewItem": "ne_reject_new_item_ex",
        }
    c_api = {}
    for header in HEADERS if isinstance(HEADERS, list) else [HEADER]:
        globals()["HEADER"] = header
        for name, entry in parse_c_api().items():
            c_api.setdefault(name, entry)
    objects = parse_jvm_decls()
    decls = [d for ds in objects.values() for d in ds]
    missing = []
    for d in decls:
        if d["name"] in SPECIAL:
            continue
        cname = OVERRIDES.get(d["name"], snake(d["name"]))
        if cname not in c_api:
            missing.append((d["name"], cname))
    if missing:
        print("UNRESOLVED:")
        for name, cname in missing:
            print(f"  {name} -> {cname}")
        return 2
    externals = externals_for(decls, c_api)
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / "Jni.wasm.kt").write_text(emit(objects, c_api, externals))
    print(f"bridge: {len(decls)} members, {len(externals)} externals")
    return 0


def _out_doubles(name: str, cname: str, jvm_params: str, call_args: str, count: int) -> None:
    body = f"""actual fun {name}({jvm_params}): DoubleArray {{
    val r = imguiKmpMalloc({count} * 8)
    try {{
        {cname}({call_args})
        return DoubleArray({count}) {{ readDoubleAt(r, it * 8) }}
    }} finally {{
        imguiKmpFree(r)
    }}
}}"""
    _special(name, body)


def _out_id_array(name: str, cname: str) -> None:
    body = f"""actual fun {name}(size: Int): LongArray {{
    val buf = imguiKmpMalloc(size * 8)
    val cnt = imguiKmpMalloc(4)
    try {{
        {cname}(buf, size, cnt)
        val n = minOf(imguiKmpReadI32(cnt, 0), size)
        return LongArray(n) {{
            imguiKmpReadI32(buf, it * 2).toUInt().toLong() or (imguiKmpReadI32(buf, it * 2 + 1).toLong() shl 32)
        }}
    }} finally {{
        imguiKmpFree(buf)
        imguiKmpFree(cnt)
    }}
}}"""
    _special(name, body)


# NodeEditor fills id arrays through (out_ids, capacity, out_count).
_out_id_array("getSelectedNodes", "ne_get_selected_nodes")
_out_id_array("getSelectedLinks", "ne_get_selected_links")
_out_id_array("getActionContextNodes", "ne_get_action_context_nodes")
_out_id_array("getActionContextLinks", "ne_get_action_context_links")
_out_id_array("getOrderedNodeIds", "ne_get_ordered_node_ids")

def _struct_special_body(name: str):
    e = STRUCT_SPECIALS[name]
    params = [p.strip() for p in e["kparams"].split(",")]
    names = ", ".join(
        f"(if ({p.split(':')[0].strip()}) 1 else 0)"
        if p.split(":", 1)[1].strip().startswith("Boolean")
        else p.split(":")[0].strip()
        for p in params
    )

    def body(d, c_api, ind):
        ret = d["ret"]
        if e["out"]:
            n = len(e["out"])
            is_float = e["outType"] == "float"
            size = 4 if is_float else 8
            kot = "FloatArray" if is_float else "DoubleArray"
            read = (f"imguiKmpReadF32(r, it)" if is_float else "readDoubleAt(r, it * 8)")
            return (
                f"{ind}actual fun {name}({e['kparams']}): {kot} {{\n"
                f"{ind}    val r = imguiKmpMalloc({n} * {size})\n"
                f"{ind}    try {{\n"
                f"{ind}        imgui_kmp_i3d_{name}({names}, r)\n"
                f"{ind}        return {kot}({n}) {{ {read} }}\n"
                f"{ind}    }} finally {{\n"
                f"{ind}        imguiKmpFree(r)\n"
                f"{ind}    }}\n"
                f"{ind}}}"
            )
        suffix = " != 0" if ret == "Boolean" else ""
        return f"{ind}actual fun {name}({e['kparams']}) = imgui_kmp_i3d_{name}({names}){suffix}"

    return body


for _n in STRUCT_SPECIALS:
    SPECIAL_BODIES[_n] = _struct_special_body(_n)

def _style_colors(name: str, kind: int):
    _special(name, f"""actual fun {name}(dst: FloatArray?): FloatArray? {{
    if (dst == null) {{
        imgui_kmp_i3d_style_colors({kind}, 0, 0)
        return null
    }}
    val r = imguiKmpMalloc(76 * 4)
    try {{
        imguiKmpSetHeapF32(r, dst.toFloat32Array())
        imgui_kmp_i3d_style_colors({kind}, r, r)
        for (i in dst.indices) dst[i] = imguiKmpReadF32(r, i)
        return dst
    }} finally {{
        imguiKmpFree(r)
    }}
}}""")


def _mesh_primitives(name: str, ptr_c: str, count_c: str, double: bool):
    if double:
        body = f"""actual fun {name}(): DoubleArray {{
    val n = {count_c}()
    val p = {ptr_c}()
    return DoubleArray(n) {{ readDoubleAt(p, it * 8) }}
}}"""
    else:
        body = f"""actual fun {name}(): IntArray {{
    val n = {count_c}()
    val p = {ptr_c}()
    return IntArray(n) {{ imguiKmpReadI32(p, it) }}
}}"""
    _special(name, body)


_mesh_primitives("cubeVtx", "implot3d_cube_vtx", "implot3d_cube_vtx_count", True)
_mesh_primitives("cubeIdx", "implot3d_cube_idx", "implot3d_cube_idx_count", False)
_mesh_primitives("sphereVtx", "implot3d_sphere_vtx", "implot3d_sphere_vtx_count", True)
_mesh_primitives("sphereIdx", "implot3d_sphere_idx", "implot3d_sphere_idx_count", False)
_mesh_primitives("duckVtx", "implot3d_duck_vtx", "implot3d_duck_vtx_count", True)
_mesh_primitives("duckIdx", "implot3d_duck_idx", "implot3d_duck_idx_count", False)

_style_colors("styleColorsAuto", 0)
_style_colors("styleColorsDark", 1)
_style_colors("styleColorsLight", 2)
_style_colors("styleColorsClassic", 3)

_special("getStyle", """actual fun getStyle(): FloatArray {
    val r = imguiKmpMalloc(76 * 4)
    try {
        imgui_kmp_implot3d_getStyle(r)
        return FloatArray(76) { imguiKmpReadF32(r, it) }
    } finally {
        imguiKmpFree(r)
    }
}""")

_special("setStyle", """actual fun setStyle(style: FloatArray) {
    val r = imguiKmpMalloc(style.size * 4)
    try {
        imguiKmpSetHeapF32(r, style.toFloat32Array())
        imgui_kmp_implot3d_setStyle(r)
    } finally {
        imguiKmpFree(r)
    }
}""")

def _md_callback(name: str, kind: int):
    body = (
        f"actual fun {name}(configPtr: Long, activate: Boolean) {{\n"
        f"    if (activate) imgui_kmp_md_install(configPtr.toInt(), {kind})"
        f" else imgui_kmp_md_uninstall(configPtr.toInt(), {kind})\n"
        "}"
    )
    _special(name, body)


_special("render", """actual fun render(configPtr: Long, markdown: String) {
    // md_render takes an explicit UTF-8 byte length; Kotlin string length
    // counts UTF-16 units, so encode once and pass the byte count.
    val bytes = markdown.encodeToByteArray()
    val s = imguiKmpMalloc(bytes.size)
    try {
        imguiKmpSetHeapU8(s, bytes.toInt8Array())
        md_render(configPtr.toInt(), s, bytes.size)
    } finally {
        imguiKmpFree(s)
    }
}""")

_md_callback("setLinkCallback", 0)
_md_callback("setTooltipCallback", 1)
_md_callback("setImageCallback", 2)

# ImPlot reports several struct-like results through out-parameters, which the
# generic emitter cannot synthesise from the Kotlin declaration.
_out_doubles("getPlotLimits", "implot_get_plot_limits", "", "r, r + 8, r + 16, r + 24", 4)
_out_doubles("getPlotMousePos", "implot_get_plot_mouse_pos", "", "r, r + 8", 2)
_out_doubles("getPlotSelection", "implot_get_plot_selection", "", "r, r + 8, r + 16, r + 24", 4)
_out_doubles("pixelsToPlot", "implot_pixels_to_plot", "pixX: Float, pixY: Float", "pixX, pixY, r, r + 8", 2)


if __name__ == "__main__":
    sys.exit(main())
