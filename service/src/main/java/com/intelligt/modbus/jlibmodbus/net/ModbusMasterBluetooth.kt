package com.intelligt.modbus.jlibmodbus.net

import com.intelligt.modbus.jlibmodbus.master.ModbusMasterSerial
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils

class ModbusMasterBluetooth(serial: SerialPort) :
    ModbusMasterSerial(ModbusConnectionBluetoothFactory.createConnection(serial)) {

    constructor(parameters: SerialParameters) : this(SerialUtils.createSerial(parameters))
}
