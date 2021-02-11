package com.inhealion.generator.device.modbus

import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPortAbstractFactory
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException

class SerialPortFactoryBluetooth :
    SerialPortAbstractFactory(SerialPortFactoryBluetooth::class.java.canonicalName, "bluetooth") {

    @Throws(SerialPortException::class)
    fun createSerialImpl(sp: SerialParameters) = SerialPortBluetooth(sp)

    fun getPortIdentifiersImpl() = emptyList<String>()
}
