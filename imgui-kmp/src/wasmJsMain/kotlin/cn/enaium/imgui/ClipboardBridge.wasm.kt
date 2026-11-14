package cn.enaium.imgui

import org.khronos.webgl.toInt8Array

private external fun imguiKmpAllocString(value: String?): Int
private external fun imguiKmpReadString(ptr: Int): String?
private external fun imgui_kmp_clipboard_read(): Int
private external fun imgui_kmp_clipboard_write(text: Int)
private external fun imgui_kmp_install_clipboard()

private var setTextBridge: ((String) -> Unit)? = null
private var getTextBridge: (() -> String?)? = null

/**
 * wasm: ImGui's clipboard callbacks are C function pointers, which JS cannot
 * hand across the boundary; the shim keeps the clipboard in a static buffer
 * instead (see gen_wasm_shim.py). Copy/paste therefore works inside the
 * application; wiring the browser clipboard needs an async JS bridge.
 */
internal actual fun installClipboardBridge(
    setText: ((String) -> Unit)?,
    getText: (() -> String?)?,
) {
    setTextBridge = setText
    getTextBridge = getText
    imgui_kmp_install_clipboard()
}

/** Pushes [text] into the shim's clipboard buffer (mirrors ImGui's copy path). */
internal fun wasmClipboardSet(text: String) {
    val ptr = imguiKmpAllocString(text)
    imgui_kmp_clipboard_write(ptr)
}

/** Reads the shim's clipboard buffer (mirrors ImGui's paste path). */
internal fun wasmClipboardGet(): String? = imguiKmpReadString(imgui_kmp_clipboard_read())
