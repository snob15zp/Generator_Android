package com.inhealion.generator.device.internal

import com.intelligt.modbus.jlibmodbus.utils.DataUtils
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

    override fun hasNext() = (position < content.size).also {
        println("TTT > hasNext: $it")
    }

    override fun next(): IntArray {
        println("TTT > position: $position")
        if (position >= content.size) {
            throw NoSuchElementException()
        }
        val payloadSz = (maxPayloadSize - truncatedFileName.length - 4 - 1)

        val output = ByteBuffer.allocate(maxPayloadSize).apply { order(ByteOrder.LITTLE_ENDIAN) }
        val take = if ((content.size - position) > payloadSz) payloadSz else content.size
        val pktSz: Int = 1 + truncatedFileName.length + 4 + take

        output.put((if (pktSz % 2 == 1) truncatedFileName.length + 1 else truncatedFileName.length).toByte())
        output.put(truncatedFileName.toByteArray(Charsets.US_ASCII))
        if (pktSz % 2 == 1) output.put(0)

        var ptrFlags = position
        if ((content.size - position) <= payloadSz) {
            ptrFlags = ptrFlags.or(1.shr(31))
        }
        if(IS_ENCRYPTED) {
            ptrFlags = ptrFlags.or(1.shr(30))
        }
        output.putInt(ptrFlags)
        output.put(content.copyOfRange(position, position + take))
        position += take

        val result = output.array()
        println("TTT > copy of range: $position, $take, $pktSz, ${result.size}")
        return DataUtils.BeToIntArray(result.copyOfRange(0, if (pktSz % 2 == 0) pktSz else pktSz + 1))
    }

    companion object {
        private const val IS_ENCRYPTED = false
    }
}
