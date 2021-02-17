package com.intelligt.modbus.jlibmodbus.serial

import com.inhealion.generator.device.modbus.SerialPortBluetooth
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException
import com.juul.kable.Characteristic

class SerialPortFactoryBluetooth(
    private val writeCharacteristic: Characteristic,
    private val readCharacteristic: Characteristic
) :
    SerialPortAbstractFactory(SerialPortFactoryBluetooth::class.java.canonicalName, "bluetooth") {

    @Throws(SerialPortException::class)
    override fun createSerialImpl(sp: SerialParameters) = SerialPortBluetooth(sp, writeCharacteristic, readCharacteristic)

    override fun getPortIdentifiersImpl() = emptyList<String>()
}
