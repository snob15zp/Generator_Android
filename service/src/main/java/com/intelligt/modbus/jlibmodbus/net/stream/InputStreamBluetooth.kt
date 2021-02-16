package com.intelligt.modbus.jlibmodbus.net.stream

import com.intelligt.modbus.jlibmodbus.exception.ModbusChecksumException
import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.intelligt.modbus.jlibmodbus.utils.CRC16
import java.io.IOException

class InputStreamBluetooth(private val serial: SerialPort) : InputStreamSerial(serial) {
    private var crc = CRC16.INITIAL_VALUE


    @Throws(IOException::class, ModbusChecksumException::class)
    override fun frameCheck() {
        val c_crc = getCrc()
        val r_crc = readShortLE()
        if (c_crc != r_crc) {
            throw ModbusChecksumException(r_crc, c_crc)
        }
    }

    override fun frameInit() {
        crc = CRC16.INITIAL_VALUE
    }

    @Throws(IOException::class)
    override fun read(): Int {
        val b = serial.read()
        crc = CRC16.calc(crc, b.toByte())
        return b
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray?, off: Int, len: Int): Int {
        val c = serial.read(b, off, len)
        crc = CRC16.calc(crc, b, off, len)
        return c
    }

    private fun getCrc(): Int {
        return crc
    }
}
