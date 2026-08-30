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

package cn.enaium.imgui.extensions.colortextedit

/**
 * A trie-based autocomplete addon for a [ColorTextEditEditor] (the
 * `TrieAutoComplete` class in ImGuiColorTextEdit's extras folder). Connect
 * it to an editor to enable word-completion driven by the language keywords
 * and the identifiers present in the document.
 *
 * close() calls [TrieAutoComplete.destroy].
 */
interface TrieAutoCompleteInstance : AutoCloseable

/**
 * A "toast" notification stack (the `Notifications` class in
 * ImGuiColorTextEdit's extras folder). Add notifications and render them
 * every frame; older entries fade out and collapse.
 *
 * close() calls [Notifications.destroy].
 */
interface NotificationsInstance : AutoCloseable

/** Kotlin bindings for the ImGuiColorTextEdit extras. */
expect object TrieAutoComplete {
    fun create(): TrieAutoCompleteInstance
    fun destroy(instance: TrieAutoCompleteInstance? = null)

    /** Connects this autocomplete to [editor], replacing the previous connection. */
    fun connect(instance: TrieAutoCompleteInstance, editor: ColorTextEditEditor)

    /** Disconnects from the editor (if connected). */
    fun disconnect(instance: TrieAutoCompleteInstance)

    fun isConnected(instance: TrieAutoCompleteInstance): Boolean
}

/** Kotlin bindings for the ImGuiColorTextEdit extras. */
expect object Notifications {
    fun create(): NotificationsInstance
    fun destroy(instance: NotificationsInstance? = null)

    /**
     * Pushes a notification onto the stack. [type] is one of [TeNotification];
     * [dismissTimeMs] is how long the message stays fully visible before
     * fading out (default 4000 ms in the C++ class).
     */
    fun add(instance: NotificationsInstance, type: Int, message: String, dismissTimeMs: Int = 4000)

    /**
     * Renders the notification stack. [posX]/[posY] is the bottom-right
     * anchor in viewport space. Call once per frame after drawing the UI.
     */
    fun render(instance: NotificationsInstance, posX: Float, posY: Float)
}

// =========================================================================
// Enums (values match Notifications::Type)
// =========================================================================

object TeNotification {
    const val SUCCESS = 0
    const val WARNING = 1
    const val ERROR = 2
    const val INFO = 3
}