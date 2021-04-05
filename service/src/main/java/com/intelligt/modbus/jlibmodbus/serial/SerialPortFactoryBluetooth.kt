package com.intelligt.modbus.jlibmodbus.serial

import android.content.Context
import com.inhealion.generator.device.modbus.SerialPortBluetooth
import com.juul.kable.Characteristic

class SerialPortFactoryBluetooth(
    private val context: Context,
    private val writeCharacteristic: Characteristic
) : SerialPortAbstractFactory(SerialPortFactoryBluetooth::class.java.canonicalName, "bluetooth") {

    @Throws(SerialPortException::class)
    override fun createSerialImpl(sp: SerialParameters) = SerialPortBluetooth(sp, writeCharacteristic, context)

    override fun getPortIdentifiersImpl() = emptyList<String>()
}
