package com.inhealion.generator.device.internal

import com.inhealion.generator.device.modbus.SerialPortBluetooth.Companion.WRITE_ESCAPE_BYTES
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
        var sum = 0
        while (sum < min(maxPayloadSize - truncatedFileName.length - 4 - 1, content.size - position)) {
            sum += if (content[idx] in WRITE_ESCAPE_BYTES) 2 else 1
            idx++
        }
        val payloadSz = idx - position
        var pktSz: Int = 1 + truncatedFileName.length + 4 + payloadSz
        val isPacketSizeOdd = pktSz % 2 == 1
        if (isPacketSizeOdd) pktSz += 1

        println("TTT > Allocate buffer: $position, $payloadSz, $pktSz, $maxPayloadSize, ${content.size}")
        val output = ByteBuffer.allocate(pktSz).apply { order(ByteOrder.LITTLE_ENDIAN) }

        output.put((if (isPacketSizeOdd) truncatedFileName.length + 1 else truncatedFileName.length).toByte())
        output.put(truncatedFileName.toByteArray(Charsets.US_ASCII))
        if (isPacketSizeOdd) output.put(0)

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
        return DataUtils.BeToIntArray(result.copyOfRange(0, pktSz))
    }

    companion object {
        private const val IS_ENCRYPTED = false
    }
}
