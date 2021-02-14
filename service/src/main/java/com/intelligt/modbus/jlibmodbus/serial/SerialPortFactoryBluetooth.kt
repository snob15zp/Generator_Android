package com.intelligt.modbus.jlibmodbus.serial

import com.inhealion.generator.device.modbus.SerialPortBluetooth
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPortAbstractFactory
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException
import com.juul.kable.Characteristic

class SerialPortFactoryBluetooth(
    private val writeCharacteristic: Characteristic
) :
    SerialPortAbstractFactory(SerialPortFactoryBluetooth::class.java.canonicalName, "bluetooth") {

    @Throws(SerialPortException::class)
    override fun createSerialImpl(sp: SerialParameters) = SerialPortBluetooth(sp, writeCharacteristic)

    override fun getPortIdentifiersImpl() = emptyList<String>()
}
