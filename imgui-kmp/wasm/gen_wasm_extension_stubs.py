#!/usr/bin/env python3
"""Generates wasmJs `actual` declarations for the enabled extensions.

Project-specific policy: on wasm the extensions that need platform services
(filesystem, threads, file dialogs) are not supported; their actuals throw
`UnsupportedOperationException` with a clear message instead of silently doing
nothing. Extensions ported to wasm later get hand-written actuals and are
removed from this generator's output.
"""

import re
import sys
from pathlib import Path

COMMON = Path("imgui-kmp/src/commonMain/kotlin/cn/enaium/imgui/extensions")
OUT = Path("imgui-kmp/src/wasmJsMain/kotlin/cn/enaium/imgui/extensions")

# Extensions not available on wasm (file dialogs need std::filesystem/system(),
# the threaded-rendering helpers need worker threads).
# Extensions with a generated wasm bridge (src/jniMain + Jni.wasm.kt).
PORTED = {"implot", "memoryeditor", "mcc", "nodeeditor", "markdown", "implot3d"}


def split_params(params: str) -> list:
    out, depth, cur = [], 0, ""
    for ch in params:
        if ch in "([{":
            depth += 1
        elif ch in ")}]":
            depth -= 1
        if ch == "," and depth == 0:
            out.append(cur.strip())
            cur = ""
        else:
            cur += ch
    if cur.strip():
        out.append(cur.strip())
    return out


def strip_default(param: str) -> str:
    param = " ".join(param.split())
    depth = 0
    for i, ch in enumerate(param):
        if ch in "([{":
            depth += 1
        elif ch in ")}]":
            depth -= 1
        elif ch == "=" and depth == 0:
            return param[:i].rstrip()
    return param.rstrip()


def parse_members(body: str) -> list:
    """Members (fun/val/var) of an expect object body, defaults stripped."""
    body = re.sub(r"/\*.*?\*/", "", body, flags=re.S)
    body = re.sub(r"//[^\n]*", "", body)
    # Mask string literals (same length) so braces/brackets/parens inside
    # default values like " \t{[( " do not skew the depth tracking.
    body = re.sub(r'"[^"]*"', lambda m: '"' + " " * (len(m.group(0)) - 2) + '"', body)
    members, i = [], 0
    pat = re.compile(
        r"(?:^|\n)\s*((?:override\s+)?)(?:fun\s+(\w+)\s*\(|(val|var)\s+(\w+)\s*:)"
    )
    while True:
        m = pat.search(body, i)
        if not m:
            break
        override_prefix = m.group(1)
        if m.group(2) or m.group(4) is None:
            # function: find the opening paren at/after the match
            j = body.find("(", m.start())
            if j < 0:
                break
            d, k = 0, j
            while k < len(body):
                if body[k] == "(":
                    d += 1
                elif body[k] == ")":
                    d -= 1
                    if d == 0:
                        break
                k += 1
            if k >= len(body):
                break
            name = body[j:].split("(")[0]
            name = body[m.start():j].split()[-1]
            rest = body[k + 1:]
            rm = re.match(r"\s*:\s*([^\n={]+)", rest)
            ret = " ".join(rm.group(1).split()) if rm else "Unit"
            members.append(("fun", name, [strip_default(x) for x in split_params(body[j + 1:k])], ret, override_prefix))
            i = k + 1 + (rm.end() if rm else 0)
        else:
            kind, name = m.group(3), m.group(4)
            line_end = body.find("\n", m.start())
            line = body[m.start(): line_end if line_end > 0 else len(body)]
            tm = re.match(r"\s*(?:val|var)\s+\w+\s*:\s*(.+)", line)
            if tm:
                members.append((kind, name, tm.group(1).strip()))
            i = line_end if line_end > 0 else len(body)
    return members


def stub_member(m, msg: str) -> str:
    if m[0] == "fun":
        _, name, params, ret, override_prefix = m
        sig = ", ".join(params)
        body = f'throw UnsupportedOperationException("{msg}")'
        mods = "actual " + (override_prefix or "")
        if ret == "Unit":
            return f"    {mods}fun {name}({sig}) {{\n        {body}\n    }}"
        return f"    {mods}fun {name}({sig}): {ret} = {body}"
    kind, name, ty = m
    if kind == "val":
        return f'    actual val {name}: {ty} get() = throw UnsupportedOperationException("{msg}")'
    return (
        f'    actual var {name}: {ty}\n'
        f'        get() = throw UnsupportedOperationException("{msg}")\n'
        f'        set(_) = throw UnsupportedOperationException("{msg}")'
    )


def main() -> int:
    generated = 0
    for stale in OUT.glob("*"):
        if stale.is_dir() and stale.name in PORTED:
            # Only remove files this generator owns: the stubs mirror the
            # commonMain expect files one-to-one; hand-written glue such as
            # Jni.wasm.kt or the callback bridges stays.
            owned = {src.stem + ".kt" for src in (COMMON / stale.name).glob("*.kt")}
            for f in stale.glob("*.kt"):
                if f.name in owned:
                    f.unlink()
            try:
                stale.rmdir()
            except OSError:
                pass
    for src in sorted(COMMON.glob("*/*.kt")):
        ext = src.parent.name
        if ext in PORTED:
            continue
        text = src.read_text()
        pkg = re.search(r"^package (.+)$", text, re.M).group(1)
        license = text.split("package ")[0]
        imports = [l.strip() for l in text.splitlines() if l.startswith("import ")]
        out = [license, f"package {pkg}", ""] + imports + [""]
        # A stubbed extension has no generated bridge; drop a stale one.
        stale_bridge = OUT / ext / "Jni.wasm.kt"
        if stale_bridge.is_file():
            stale_bridge.unlink()
        msg = f"{src.stem} is not available on the wasm target"
        decls = []
        for m in re.finditer(
            r"expect (object|class) (\w+)\s*(?:\(([^)]*)\))?\s*(?::([^{]*))?\{", text
        ):
            start = m.end()
            depth, k = 1, start
            while k < len(text) and depth:
                if text[k] == "{":
                    depth += 1
                elif text[k] == "}":
                    depth -= 1
                k += 1
            decls.append((m.group(1), m.group(2), m.group(3), m.group(4), text[start:k - 1]))
        for kind, name, ctor, supertypes, body in decls:
            if kind == "object":
                out.append(f"actual object {name} {{")
            else:
                params = [strip_default(p.strip()) for p in split_params(ctor or "")]
                supers = ": " + supertypes.strip() if supertypes and supertypes.strip() else ""
                out.append(f"actual class {name}({', '.join(params)}){supers} {{")
            for member in parse_members(body):
                out.append(stub_member(member, msg))
            out.append("}")
            out.append("")
        target = OUT / ext / src.name
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text("\n".join(out))
        generated += 1
        print("stubbed", target.relative_to(Path(".")))
    print("files:", generated)
    return 0


if __name__ == "__main__":
    sys.exit(main())
