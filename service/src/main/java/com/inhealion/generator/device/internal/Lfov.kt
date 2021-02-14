package com.inhealion.generator.device.internal

import java.io.File
import java.nio.ByteBuffer

class Lfov(
    fileName: String,
    private val content: ByteArray,
    private val maxFileNameSize: Int,
    private val maxPayloadSize: Int,
) : Iterator<IntArray> {

    private var position = 0
    private val truncatedFileName = File(fileName).run {
        if (nameWithoutExtension.length > maxFileNameSize) {
            val ext = extension.substring(1, 3)
            val name = nameWithoutExtension.substring(0, maxFileNameSize)
            "$name.$ext"
        } else {
            fileName
        }
    }

    override fun hasNext() = position < content.size

    override fun next(): IntArray {
        if (position >= content.size) {
            throw NoSuchElementException()
        }

        val output = ByteBuffer.allocate(maxPayloadSize)

        val payloadSz = (maxPayloadSize - truncatedFileName.length - 4 - 1);
        output.put(truncatedFileName.toByteArray(Charsets.US_ASCII))

        var ptrFlags = position
        if ((content.size - position) <= payloadSz) {
            ptrFlags = ptrFlags.or(1.shr(if (IS_ENCRYPTED) 30 else 31))
        }
        output.putInt(ptrFlags)

        val take = if ((content.size - position) > payloadSz) payloadSz else content.size
        output.put(content.copyOfRange(position, take))
        if ((output.position() % 2) == 1) {
            output.put(0)
        }
        position += take
        return output.asIntBuffer().array().copyOf(output.position())
    }

    companion object {
        private const val IS_ENCRYPTED = false
    }
}
