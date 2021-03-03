package com.inhealion.generator.device.internal

import com.intelligt.modbus.jlibmodbus.utils.DataUtils
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

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

        var idx = position
        var payloadSz = 0
        while (payloadSz < min(maxPayloadSize - truncatedFileName.length - 4 - 1, content.size - position)) {
            payloadSz += if(content[idx] == 0x25.toByte() || content[idx] == 0x1b.toByte()) 2 else 1
            idx++
        }

        val output = ByteBuffer.allocate(maxPayloadSize).apply { order(ByteOrder.LITTLE_ENDIAN) }
        val pktSz: Int = 1 + truncatedFileName.length + 4 + payloadSz

        output.put((if (pktSz % 2 == 1) truncatedFileName.length + 1 else truncatedFileName.length).toByte())
        output.put(truncatedFileName.toByteArray(Charsets.US_ASCII))
        if (pktSz % 2 == 1) output.put(0)

        var ptrFlags = position
        if ((content.size - position) <= payloadSz) {
            ptrFlags = ptrFlags.or(1.shl(31))
        }
        if (IS_ENCRYPTED) {
            ptrFlags = ptrFlags.or(1.shl(30))
        }
        output.putInt(ptrFlags)
        output.put(content.copyOfRange(position, position + payloadSz))
        position += payloadSz

        val result = output.array()
        println("TTT > copy of range: $position, $payloadSz, $pktSz, ${result.size}")
        return DataUtils.BeToIntArray(result.copyOfRange(0, if (pktSz % 2 == 0) pktSz else pktSz + 1))
    }

    companion object {
        private const val IS_ENCRYPTED = false
    }
}
