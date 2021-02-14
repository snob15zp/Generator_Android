package com.intelligt.modbus.jlibmodbus.net.transport

import com.intelligt.modbus.jlibmodbus.msg.base.ModbusMessage
import com.intelligt.modbus.jlibmodbus.net.stream.InputStreamRTU
import com.intelligt.modbus.jlibmodbus.net.stream.OutputStreamRTU
import com.intelligt.modbus.jlibmodbus.serial.SerialPort

internal class ModbusTransportBluetooth(private val serial: SerialPort) :
    ModbusTransportSerial(InputStreamRTU(serial), OutputStreamRTU(serial)) {

    override fun sendImpl(msg: ModbusMessage) {
        msg.write(serial.outputStream)
    }
}
