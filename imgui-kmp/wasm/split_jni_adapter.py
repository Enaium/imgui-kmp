#!/usr/bin/env python3
"""Splits an extension's JVM-only bridge into the shared jniMain layers.

The JVM/Android and wasm targets share `src/jniMain` (it depends on commonMain),
so every extension bridge is split into:

  jniMain/.../<ext>/Jni.kt        expect object Jni    (declarations)
  jniMain/.../<ext>/<Ext>Jni.kt   shared adapters      (actual object + helpers)
  jvmMain/.../<ext>/Jni.jvm.kt    actual object Jni    (JNI externals)

Usage: split_jni_adapter.py <jvm-file> <ext-dir> <ClassPrefix>
e.g.   split_jni_adapter.py imgui-kmp/src/jvmMain/kotlin/cn/enaium/imgui/extensions/nodeeditor/NodeEditor.jvm.kt \
           imgui-kmp/src/jniMain/kotlin/cn/enaium/imgui/extensions/nodeeditor nodeeditor
"""
import re
import sys
from pathlib import Path


def object_span(lines, start):
    depth = 0
    for i in range(start, len(lines)):
        depth += lines[i].count("{") - lines[i].count("}")
        if depth == 0 and i > start:
            return i
    raise SystemExit("unbalanced braces")


def main() -> int:
    src_path = Path(sys.argv[1])
    out_dir = Path(sys.argv[2])
    name = sys.argv[3]

    text = src_path.read_text()
    lines = text.splitlines(keepends=True)
    spans = []
    i = 0
    while i < len(lines):
        m = re.match(r"internal object (Jni\w*) \{", lines[i])
        if m:
            end = object_span(lines, i)
            spans.append((i, end, m.group(1)))
            i = end + 1
        else:
            i += 1
    if not spans:
        raise SystemExit(f"{src_path}: no internal object Jni*")
    head = "".join(lines[:spans[0][0]])
    blocks = ["".join(lines[a:b + 1]) for a, b, _ in spans]
    rest_lines = list(lines)
    for (a, b, _), block in zip(spans, blocks):
        for k in range(a, b + 1):
            rest_lines[k] = ""
    rest = "".join(rest_lines[spans[0][1] + 1:])
    jni = "".join(blocks).splitlines(keepends=True)

    out_dir.mkdir(parents=True, exist_ok=True)
    jvm_out = src_path.parent / "Jni.jvm.kt"

    actual = "".join(jni)
    actual = re.sub(r"(internal )object (Jni\w*)", r"\1actual object \2", actual)
    actual = actual.replace("    external fun ", "    actual external fun ")
    jvm_out.write_text(head + actual)

    expect = re.sub(r"(internal )object (Jni\w*)", r"\1expect object \2", "".join(jni))
    expect = expect.replace(
        """    init {
        cn.enaium.imgui.NativeLoader.load()
    }
""",
        "",
    )
    expect = expect.replace("external fun", "fun")
    if "NativeLoader" in expect:
        raise SystemExit(f"{src_path}: NativeLoader in the expect object")
    (out_dir / "Jni.kt").write_text(head + expect)

    # Rename the platform helper classes (JvmImGuiContext -> JniImGuiContext)
    # but leave Kotlin's own JVM annotations alone.
    shared = re.sub(
        r"\bJvm(?!(?:Static|Field|Name|Overloads|Synthetic|Default|Inline|Suppress))(?=[A-Z])",
        "Jni",
        rest,
    )
    (out_dir / f"{name}Jni.kt").write_text(head + shared)

    src_path.unlink()
    print(f"{src_path.name}: {len(jni)} lines of bridge split")
    return 0


if __name__ == "__main__":
    sys.exit(main())
