package com.netflixppstreaming.util

import java.security.MessageDigest
import java.util.*

object MeshUtils {

    fun generateChunkHash(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data)
        return bytesToHex(hashBytes)
    }

    fun splitIntoChunks(data: ByteArray, chunkSize: Int = 10 * 1024 * 1024): List<ByteArray> {
        val chunks = mutableListOf<ByteArray>()
        var offset = 0

        while (offset < data.size) {
            val length = minOf(chunkSize, data.size - offset)
            val chunk = data.copyOfRange(offset, offset + length)
            chunks.add(chunk)
            offset += length
        }

        return chunks
    }

    fun mergeChunks(chunks: List<ByteArray>): ByteArray {
        val totalSize = chunks.sumOf { it.size }
        val result = ByteArray(totalSize)
        var offset = 0

        chunks.forEach { chunk ->
            System.arraycopy(chunk, 0, result, offset, chunk.size)
            offset += chunk.size
        }

        return result
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }

    fun generatePeerId(): String {
        return UUID.randomUUID().toString().substring(0, 8)
    }

    fun calculateTransferSpeed(startTime: Long, endTime: Long, bytesTransferred: Long): Double {
        val timeTaken = (endTime - startTime) / 1000.0 // in seconds
        return if (timeTaken > 0) bytesTransferred / timeTaken else 0.0
    }
}