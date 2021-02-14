package com.intelligt.modbus.jlibmodbus.net

import com.intelligt.modbus.jlibmodbus.serial.SerialPort

object ModbusConnectionBluetoothFactory {
    fun createConnection(serial: SerialPort): ModbusConnection = ModbusConnectionBluetooth(serial)
}
