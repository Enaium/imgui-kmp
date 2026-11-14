package cn.enaium.imgui.example.font

/**
 * A browser cannot read the host's font files, so the system-font probes of
 * the desktop builds report "absent" and the demo falls back to the bundled
 * fonts it creates itself.
 */
internal actual fun isMac(): Boolean = false

internal actual fun isWindows(): Boolean = false

internal actual fun fileExists(path: String): Boolean = false
