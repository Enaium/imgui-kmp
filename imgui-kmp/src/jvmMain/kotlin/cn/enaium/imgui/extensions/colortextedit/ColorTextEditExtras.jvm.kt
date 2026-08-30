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

// =========================================================================
// JVM/Android actual implementations
// =========================================================================

internal class JvmTrieAutoCompleteInstance(internal val ptr: Long) : TrieAutoCompleteInstance {
    override fun close() {
        Jni.autocompleteDestroy(ptr)
    }
}

internal class JvmNotificationsInstance(internal val ptr: Long) : NotificationsInstance {
    override fun close() {
        Jni.notificationsDestroy(ptr)
    }
}

actual object TrieAutoComplete {
    actual fun create(): TrieAutoCompleteInstance {
        val ptr = Jni.autocompleteCreate()
        require(ptr != 0L) { "te_autocomplete_create returned null" }
        return JvmTrieAutoCompleteInstance(ptr)
    }

    actual fun destroy(instance: TrieAutoCompleteInstance?) {
        if (instance != null) {
            Jni.autocompleteDestroy((instance as JvmTrieAutoCompleteInstance).ptr)
        }
    }

    actual fun connect(instance: TrieAutoCompleteInstance, editor: ColorTextEditEditor) {
        Jni.autocompleteConnect(
            (instance as JvmTrieAutoCompleteInstance).ptr,
            (editor as JvmColorTextEditEditor).ptr,
        )
    }

    actual fun disconnect(instance: TrieAutoCompleteInstance) {
        Jni.autocompleteDisconnect((instance as JvmTrieAutoCompleteInstance).ptr)
    }

    actual fun isConnected(instance: TrieAutoCompleteInstance): Boolean =
        Jni.autocompleteIsConnected((instance as JvmTrieAutoCompleteInstance).ptr)
}

actual object Notifications {
    actual fun create(): NotificationsInstance {
        val ptr = Jni.notificationsCreate()
        require(ptr != 0L) { "te_notifications_create returned null" }
        return JvmNotificationsInstance(ptr)
    }

    actual fun destroy(instance: NotificationsInstance?) {
        if (instance != null) {
            Jni.notificationsDestroy((instance as JvmNotificationsInstance).ptr)
        }
    }

    actual fun add(instance: NotificationsInstance, type: Int, message: String, dismissTimeMs: Int) {
        Jni.notificationsAdd((instance as JvmNotificationsInstance).ptr, type, message, dismissTimeMs)
    }

    actual fun render(instance: NotificationsInstance, posX: Float, posY: Float) {
        Jni.notificationsRender((instance as JvmNotificationsInstance).ptr, posX, posY)
    }
}