package com.inhealion.generator.device.internal

import com.inhealion.generator.device.modbus.SerialPortBluetooth.Companion.WRITE_ESCAPE_BYTES
import com.intelligt.modbus.jlibmodbus.utils.DataUtils
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.min

class Lfov(
    fileName: String,
    private val content: ByteArray,
    private val maxFileNameSize: Int,
    private val maxPayloadSize: Int,
    private val isEncrypted: Boolean,
) : Iterator<IntArray> {

    private var position = 0
    private val truncatedFileName = truncatedFileName(fileName, maxFileNameSize)

    override fun hasNext() = (position < content.size).also {
        log("hasNext: $it")
    }

    override fun next(): IntArray {
        log("position: $position")
        if (position >= content.size) {
            throw NoSuchElementException()
        }

        var idx = position
        var sum = 0
        while (sum < min(maxPayloadSize - truncatedFileName.length - 4 - 1, content.size - position)) {
            sum += if (content[idx] in WRITE_ESCAPE_BYTES) 2 else 1
            idx++
        }
        var payloadSz = idx - position
        payloadSz = if (payloadSz > 16) (payloadSz / 16) * 16 else payloadSz

        var pktSz: Int = 1 + truncatedFileName.length + 4 + payloadSz
        val isPacketSizeOdd = pktSz % 2 == 1
        if (isPacketSizeOdd) pktSz += 1

        log("Allocate buffer: $position, $payloadSz, $pktSz, $maxPayloadSize, ${content.size}")
        val output = ByteBuffer.allocate(pktSz).apply { order(ByteOrder.LITTLE_ENDIAN) }

        output.put((if (isPacketSizeOdd) truncatedFileName.length + 1 else truncatedFileName.length).toByte())
        output.put(truncatedFileName.toByteArray(Charsets.US_ASCII))
        if (isPacketSizeOdd) output.put(0)

        var ptrFlags = position
        if ((content.size - position) <= payloadSz) {
            ptrFlags = ptrFlags.or(1.shl(31))
        }
        if (isEncrypted) {
            ptrFlags = ptrFlags.or(1.shl(30))
        }
        output.putInt(ptrFlags)
        output.put(content.copyOfRange(position, position + payloadSz))
        position += payloadSz

        val result = output.array()
        return DataUtils.BeToIntArray(result.copyOfRange(0, pktSz))
    }

    private fun log(message: String, prefix: String = "Lfov > ") {
        if (LOG_ENABLED)
            Timber.d("$prefix$message")
    }

    companion object {
        private const val LOG_ENABLED = true

        fun truncatedFileName(fileName: String, maxFileNameSize: Int, withoutExtension: Boolean = false) =
            File(fileName).run {
                if (nameWithoutExtension.length > maxFileNameSize) {
                    val ext = extension.substring(1, 3)
                    val name = nameWithoutExtension.substring(0, maxFileNameSize)
                    if (withoutExtension) name else "$name.$ext"
                } else {
                    if (withoutExtension) nameWithoutExtension else fileName
                }
            }
    }
}
