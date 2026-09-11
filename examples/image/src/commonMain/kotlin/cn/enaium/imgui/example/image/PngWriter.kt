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

package cn.enaium.imgui.example.image

/**
 * A tiny PNG encoder producing an 8-bit RGBA PNG (color type 6) with stored
 * (uncompressed) deflate blocks. It exists because the SDL surface bindings
 * expose pixels only as read snapshots — there is no cross-platform way to
 * *write* pixels into an [cn.enaium.sdl.SDLSurface] for
 * [cn.enaium.sdl.image.SDLImage.savePNG]. The bytes are produced directly,
 * so the alpha channel is guaranteed opaque (255).
 *
 * The output is a valid PNG: signature, IHDR, IDAT (zlib with stored
 * blocks), IEND. Each scanline is prefixed with filter type 0 (None).
 */
internal fun writePng(
    pixels: ByteArray,
    width: Int,
    height: Int,
): ByteArray {
    require(pixels.size >= width * height * 4) { "pixel buffer too small" }

    val crcTable = IntArray(256) { n ->
        var c = n
        repeat(8) {
            c = if (c and 1 != 0) 0xEDB88320.toInt() xor (c ushr 1) else c ushr 1
        }
        c
    }
    fun crc32(vararg chunks: ByteArray): Int {
        var c = 0xFFFFFFFF.toInt()
        for (chunk in chunks) {
            for (b in chunk) {
                c = crcTable[(c xor b.toInt()) and 0xFF] xor (c ushr 8)
            }
        }
        return c xor 0xFFFFFFFF.toInt()
    }
    fun be32(v: Int): ByteArray = byteArrayOf((v ushr 24).toByte(), (v ushr 16).toByte(), (v ushr 8).toByte(), v.toByte())
    fun chunk(type: String, data: ByteArray): ByteArray {
        val typeBytes = type.encodeToByteArray()
        val crc = crc32(typeBytes, data)
        return be32(data.size) + typeBytes + data + be32(crc)
    }

    // IHDR: width, height, bit depth 8, color type 6 (RGBA), compression 0,
    // filter 0, interlace 0.
    val ihdr = be32(width) + be32(height) + byteArrayOf(8, 6, 0, 0, 0)

    // Raw scanlines with filter byte 0; adler32 over (filter + pixels).
    val stride = width * 4
    val raw = ByteArray((stride + 1) * height)
    var a1 = 1
    var a2 = 0
    for (y in 0 until height) {
        val out = y * (stride + 1)
        raw[out] = 0 // filter None
        for (x in 0 until stride) {
            val b = pixels[y * stride + x]
            raw[out + 1 + x] = b
        }
    }
    for (b in raw) {
        a1 = (a1 + (b.toInt() and 0xFF)) % 65521
        a2 = (a2 + a1) % 65521
    }
    val adler = ((a2 shl 16) or a1).toInt()

    // zlib stream: header (deflate, 32K window) + stored (uncompressed)
    // deflate blocks (each ≤ 65535 bytes: 1-byte header + LEN + NLEN) +
    // adler32 checksum.
    val maxBlock = 65535
    val blockCount = (raw.size + maxBlock - 1) / maxBlock
    val idatData = ByteArray(raw.size + 6 + blockCount * 5)
    var off = 0
    idatData[off++] = 0x78
    idatData[off++] = 0x01
    var remaining = raw.size
    var block = 0
    while (remaining > 0) {
        val len = minOf(remaining, maxBlock)
        val finalFlag = if (block == blockCount - 1) 1 else 0
        idatData[off++] = finalFlag.toByte() // BFINAL=0/1, BTYPE=00 (stored)
        idatData[off++] = (len and 0xFF).toByte()
        idatData[off++] = ((len shr 8) and 0xFF).toByte()
        val nlen = len.inv()
        idatData[off++] = (nlen and 0xFF).toByte()
        idatData[off++] = ((nlen shr 8) and 0xFF).toByte()
        raw.copyInto(idatData, off, raw.size - remaining, raw.size - remaining + len)
        off += len
        remaining -= len
        block++
    }
    idatData[off++] = (adler ushr 24).toByte()
    idatData[off++] = (adler ushr 16).toByte()
    idatData[off++] = (adler ushr 8).toByte()
    idatData[off] = adler.toByte()

    return PNG_SIGNATURE + chunk("IHDR", ihdr) + chunk("IDAT", idatData) + chunk("IEND", ByteArray(0))
}

private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)