package com.intelligt.modbus.jlibmodbus.net.stream

import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.intelligt.modbus.jlibmodbus.utils.CRC16
import java.io.IOException

class OutputStreamBluetooth(private val serial: SerialPort) : OutputStreamSerial(serial) {

    override fun write(b: ByteArray?) {
        super.write(b)
        serial.write(b)
    }

    override fun write(b: Int) {
        super.write(b)
        serial.write(b)
    }

    @Throws(IOException::class)
    override fun flush() {
        writeShortLE(CRC16.calc(toByteArray()))
        super.flush()
    }
}
