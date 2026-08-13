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

package cn.enaium.imgui.example

/**
 * Parses `--frames N` (exit after N frames, for headless CI runs) and
 * dispatches to the requested example.
 */
private fun parseArgs(args: Array<String>): Pair<String, Int> {
    var example = "renderer"
    var frames = Int.MAX_VALUE
    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "--renderer" -> example = "renderer"
            "--gpu" -> example = "gpu"
            "--frames" -> {
                if (i + 1 < args.size) {
                    frames = args[i + 1].toIntOrNull() ?: Int.MAX_VALUE
                    i++
                }
            }
        }
        i++
    }
    return example to frames
}

fun main(args: Array<String>) {
    val (example, frames) = parseArgs(args)
    println("imgui-kmp example: $example (frames=$frames)")
    when (example) {
        "gpu" -> runSdlGpuExample(frames)
        else -> runSdlRendererExample(frames)
    }
}
