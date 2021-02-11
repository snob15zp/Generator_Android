package com.inhealion.generator.device.modbus

import android.bluetooth.BluetoothAdapter
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.juul.kable.peripheral
import kotlinx.coroutines.runBlocking

class SerialPortBluetooth(sp: SerialParameters) : SerialPort(sp) {
    override fun write(b: Int) {
        TODO("Not yet implemented")
    }

    override fun write(bytes: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun open() = runBlocking {
        val bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(serialParameters.device)
        val peripheral = peripheral(bluetoothDevice)

        peripheral.connect()
    }

    override fun read(): Int {
        TODO("Not yet implemented")
    }

    override fun read(b: ByteArray?, off: Int, len: Int): Int {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun isOpened(): Boolean {
        TODO("Not yet implemented")
    }

}
