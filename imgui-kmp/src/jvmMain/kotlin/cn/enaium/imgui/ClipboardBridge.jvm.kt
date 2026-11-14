package cn.enaium.imgui

/** JVM: forward clipboard callbacks through the JNI trampoline. */
internal actual fun installClipboardBridge(
    setText: ((String) -> Unit)?,
    getText: (() -> String?)?,
) {
    clipboardBridgeSetText = setText
    clipboardBridgeGetText = getText
    Jni.setClipboardFunctions()
}
