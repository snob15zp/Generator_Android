package com.intelligt.modbus.jlibmodbus.net.stream

import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.intelligt.modbus.jlibmodbus.utils.CRC16
import java.io.IOException

class OutputStreamBluetooth(private val serial: SerialPort) : OutputStreamSerial(serial) {

    @Throws(IOException::class)
    override fun flush() {
        writeShortLE(CRC16.calc(toByteArray()))
        super.flush()
    }
}
