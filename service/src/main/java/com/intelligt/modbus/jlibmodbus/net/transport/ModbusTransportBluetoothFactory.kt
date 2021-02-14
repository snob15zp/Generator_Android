package com.intelligt.modbus.jlibmodbus.net.transport

import com.intelligt.modbus.jlibmodbus.serial.SerialPort

object ModbusTransportBluetoothFactory {
    fun createBluetooth(serial: SerialPort): ModbusTransport = ModbusTransportBluetooth(serial)
}
