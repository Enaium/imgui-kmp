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

@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlin.experimental.ExperimentalNativeApi::class,
)

package cn.enaium.imgui.extensions.colortextedit

import kotlinx.cinterop.*
import imgui.*

// =========================================================================
// Native (cinterop) actual implementations
// =========================================================================
internal class NativeTrieAutoCompleteInstance(internal val ptr: CPointer<te_autocomplete>?) : TrieAutoCompleteInstance {
    override fun close() {
        te_autocomplete_destroy(ptr)
    }
}

internal class NativeNotificationsInstance(internal val ptr: CPointer<te_notifications>?) : NotificationsInstance {
    override fun close() {
        te_notifications_destroy(ptr)
    }
}

/** Casts [editor] to its native implementation and returns the raw `te_editor` pointer. */
private fun ptr(editor: ColorTextEditEditor): CPointer<te_editor>? =
    (editor as NativeColorTextEditEditor).ptr

/** Casts [instance] to its native implementation and returns the raw `te_autocomplete` pointer. */
private fun ptr(instance: TrieAutoCompleteInstance): CPointer<te_autocomplete>? =
    (instance as NativeTrieAutoCompleteInstance).ptr

/** Casts [instance] to its native implementation and returns the raw `te_notifications` pointer. */
private fun ptr(instance: NotificationsInstance): CPointer<te_notifications>? =
    (instance as NativeNotificationsInstance).ptr

actual object TrieAutoComplete {
    actual fun create(): TrieAutoCompleteInstance {
        val p = te_autocomplete_create() ?: error("te_autocomplete_create returned null")
        return NativeTrieAutoCompleteInstance(p)
    }

    actual fun destroy(instance: TrieAutoCompleteInstance?) {
        if (instance != null) {
            te_autocomplete_destroy(ptr(instance))
        }
    }

    actual fun connect(instance: TrieAutoCompleteInstance, editor: ColorTextEditEditor) =
        te_autocomplete_connect(ptr(instance), ptr(editor))

    actual fun disconnect(instance: TrieAutoCompleteInstance) =
        te_autocomplete_disconnect(ptr(instance))

    actual fun isConnected(instance: TrieAutoCompleteInstance): Boolean =
        te_autocomplete_is_connected(ptr(instance))
}

actual object Notifications {
    actual fun create(): NotificationsInstance {
        val p = te_notifications_create() ?: error("te_notifications_create returned null")
        return NativeNotificationsInstance(p)
    }

    actual fun destroy(instance: NotificationsInstance?) {
        if (instance != null) {
            te_notifications_destroy(ptr(instance))
        }
    }

    actual fun add(instance: NotificationsInstance, type: Int, message: String, dismissTimeMs: Int) =
        te_notifications_add(ptr(instance), type, message, dismissTimeMs)

    actual fun render(instance: NotificationsInstance, posX: Float, posY: Float) =
        te_notifications_render(ptr(instance), posX, posY)
}