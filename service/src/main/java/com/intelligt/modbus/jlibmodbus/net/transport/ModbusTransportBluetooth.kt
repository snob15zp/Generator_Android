package com.intelligt.modbus.jlibmodbus.net.transport

import com.intelligt.modbus.jlibmodbus.net.stream.InputStreamBluetooth
import com.intelligt.modbus.jlibmodbus.net.stream.OutputStreamBluetooth
import com.intelligt.modbus.jlibmodbus.serial.SerialPort

internal class ModbusTransportBluetooth(serial: SerialPort) :
    ModbusTransportSerial(InputStreamBluetooth(serial), OutputStreamBluetooth(serial)) {
}
