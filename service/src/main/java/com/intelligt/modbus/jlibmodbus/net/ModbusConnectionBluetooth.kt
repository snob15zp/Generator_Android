package com.intelligt.modbus.jlibmodbus.net

import com.intelligt.modbus.jlibmodbus.net.transport.ModbusTransportBluetooth
import com.intelligt.modbus.jlibmodbus.serial.SerialPort

internal class ModbusConnectionBluetooth (serial: SerialPort) :
    ModbusConnectionSerial(serial, ModbusTransportBluetooth(serial))
