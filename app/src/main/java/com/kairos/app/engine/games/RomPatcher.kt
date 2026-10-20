package com.kairos.app.engine.games

import java.util.zip.CRC32

class PatchException(message: String) : Exception(message)

/**
 * Applies IPS, BPS and UPS patches — the formats fan-game creators use to share ROM hacks
 * without distributing anyone's copyrighted game. You bring your own base ROM; Kairos patches
 * it locally and never uploads anything.
 */
object RomPatcher {
    const val MAX_BYTES = 64 * 1024 * 1024

    enum class Format { IPS, BPS, UPS }

    fun detect(patch: ByteArray): Format? = when {
        patch.startsWith("PATCH") -> Format.IPS
        patch.startsWith("BPS1") -> Format.BPS
        patch.startsWith("UPS1") -> Format.UPS
        else -> null
    }

    fun apply(source: ByteArray, patch: ByteArray): ByteArray = when (detect(patch)) {
        Format.IPS -> ips(source, patch)
        Format.BPS -> bps(source, patch)
        Format.UPS -> ups(source, patch)
        null -> throw PatchException("This file isn't an IPS, BPS or UPS patch.")
    }

    /** For BPS/UPS the patch records which base ROM it expects; returns that CRC32 if present. */
    fun expectedSourceCrc(patch: ByteArray): Long? = when (detect(patch)) {
        Format.BPS, Format.UPS -> if (patch.size >= 12) u32(patch, patch.size - 12) else null
        else -> null
    }

    fun crc32(bytes: ByteArray): Long = CRC32().apply { update(bytes) }.value

    // ---- IPS ----
    fun ips(source: ByteArray, patch: ByteArray): ByteArray {
        var out = source.copyOf()
        var p = 5
        var truncate = -1
        while (true) {
            if (p + 3 > patch.size) throw PatchException("The IPS patch ended unexpectedly.")
            if (patch[p] == 'E'.code.toByte() && patch[p + 1] == 'O'.code.toByte() && patch[p + 2] == 'F'.code.toByte()) {
                p += 3
                if (p + 3 <= patch.size) truncate = u24(patch, p)
                break
            }
            val offset = u24(patch, p); p += 3
            val size = u16(patch, p); p += 2
            if (size > 0) {
                out = ensure(out, offset + size)
                System.arraycopy(patch, p, out, offset, size)
                p += size
            } else {
                val run = u16(patch, p); p += 2
                val value = patch[p]; p += 1
                out = ensure(out, offset + run)
                java.util.Arrays.fill(out, offset, offset + run, value)
            }
        }
        if (truncate in 1 until out.size) out = out.copyOf(truncate)
        return out
    }

    // ---- BPS ----
    fun bps(source: ByteArray, patch: ByteArray): ByteArray {
        verifyFooter(patch)
        val r = Reader(patch, 4)
        val sourceSize = r.varint()
        val targetSize = r.varint()
        val metaSize = r.varint()
        r.pos += metaSize.toInt()
        if (source.size.toLong() != sourceSize) throw PatchException("This patch expects a base ROM of $sourceSize bytes, yours is ${source.size}.")
        checkSourceCrc(source, patch)
        if (targetSize > MAX_BYTES) throw PatchException("The patched game would be too large to handle here.")
        val target = ByteArray(targetSize.toInt())
        var out = 0
        var srcRel = 0
        var tgtRel = 0
        val end = patch.size - 12
        while (r.pos < end) {
            val data = r.varint()
            val command = (data and 3).toInt()
            val length = ((data shr 2) + 1).toInt()
            when (command) {
                0 -> { System.arraycopy(source, out, target, out, length); out += length }
                1 -> { System.arraycopy(patch, r.pos, target, out, length); r.pos += length; out += length }
                2 -> {
                    val d = r.varint()
                    srcRel += (if (d and 1L == 1L) -1 else 1) * (d shr 1).toInt()
                    System.arraycopy(source, srcRel, target, out, length)
                    srcRel += length; out += length
                }
                else -> {
                    val d = r.varint()
                    tgtRel += (if (d and 1L == 1L) -1 else 1) * (d shr 1).toInt()
                    repeat(length) { target[out++] = target[tgtRel++] } // may overlap by design
                }
            }
        }
        val expected = u32(patch, patch.size - 8)
        if (crc32(target) != expected) throw PatchException("The patched result didn't verify. The patch file may be damaged.")
        return target
    }

    // ---- UPS ----
    fun ups(source: ByteArray, patch: ByteArray): ByteArray {
        verifyFooter(patch)
        val r = Reader(patch, 4)
        val inSize = r.varint()
        val outSize = r.varint()
        if (source.size.toLong() != inSize) throw PatchException("This patch expects a base ROM of $inSize bytes, yours is ${source.size}.")
        checkSourceCrc(source, patch)
        if (outSize > MAX_BYTES) throw PatchException("The patched game would be too large to handle here.")
        val target = ByteArray(outSize.toInt())
        System.arraycopy(source, 0, target, 0, minOf(source.size, target.size))
        var offset = 0L
        val end = patch.size - 12
        while (r.pos < end) {
            offset += r.varint()
            while (true) {
                val x = patch[r.pos++]
                if (x.toInt() == 0) { offset++; break }
                if (offset < outSize) {
                    val i = offset.toInt()
                    val base = if (offset < inSize) source[i].toInt() else 0
                    target[i] = (base xor x.toInt()).toByte()
                }
                offset++
            }
        }
        val expected = u32(patch, patch.size - 8)
        if (crc32(target) != expected) throw PatchException("The patched result didn't verify. The patch file may be damaged.")
        return target
    }

    private fun checkSourceCrc(source: ByteArray, patch: ByteArray) {
        val expected = u32(patch, patch.size - 12)
        val actual = crc32(source)
        if (expected != actual) {
            throw PatchException(
                "This patch was made for a different base ROM (it expects CRC32 %08X, yours is %08X). Check the version the creator asks for — e.g. FireRed v1.0, not v1.1."
                    .format(expected, actual),
            )
        }
    }

    private fun verifyFooter(patch: ByteArray) {
        if (patch.size < 16) throw PatchException("The patch file is too small to be valid.")
        val expected = u32(patch, patch.size - 4)
        val actual = CRC32().apply { update(patch, 0, patch.size - 4) }.value
        if (expected != actual) throw PatchException("The patch file is damaged (checksum mismatch). Try downloading it again.")
    }

    private class Reader(val b: ByteArray, var pos: Int) {
        fun varint(): Long {
            var data = 0L
            var shift = 1L
            while (true) {
                val x = b[pos++].toInt() and 0xFF
                data += (x and 0x7F) * shift
                if (x and 0x80 != 0) break
                shift = shift shl 7
                data += shift
            }
            return data
        }
    }

    private fun ensure(b: ByteArray, size: Int): ByteArray {
        if (size > MAX_BYTES) throw PatchException("The patched game would be too large to handle here.")
        return if (size <= b.size) b else b.copyOf(size)
    }

    private fun ByteArray.startsWith(s: String) = size >= s.length && s.indices.all { this[it] == s[it].code.toByte() }
    private fun u16(b: ByteArray, i: Int) = ((b[i].toInt() and 0xFF) shl 8) or (b[i + 1].toInt() and 0xFF)
    private fun u24(b: ByteArray, i: Int) = ((b[i].toInt() and 0xFF) shl 16) or ((b[i + 1].toInt() and 0xFF) shl 8) or (b[i + 2].toInt() and 0xFF)
    private fun u32(b: ByteArray, i: Int): Long =
        (b[i].toLong() and 0xFF) or ((b[i + 1].toLong() and 0xFF) shl 8) or ((b[i + 2].toLong() and 0xFF) shl 16) or ((b[i + 3].toLong() and 0xFF) shl 24)
}
