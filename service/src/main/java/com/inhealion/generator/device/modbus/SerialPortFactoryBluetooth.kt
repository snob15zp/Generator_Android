package com.inhealion.generator.device.modbus

import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPortAbstractFactory
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException
import com.juul.kable.Characteristic

class SerialPortFactoryBluetooth(
    private val writeCharacteristic: Characteristic,
    private val readCharacteristic: Characteristic
) :
    SerialPortAbstractFactory(SerialPortFactoryBluetooth::class.java.canonicalName, "bluetooth") {

    @Throws(SerialPortException::class)
    fun createSerialImpl(sp: SerialParameters) = SerialPortBluetooth(sp, writeCharacteristic, readCharacteristic)

    fun getPortIdentifiersImpl() = emptyList<String>()
}
