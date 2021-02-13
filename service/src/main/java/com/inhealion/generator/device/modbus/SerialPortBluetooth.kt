package com.inhealion.generator.device.modbus

import android.bluetooth.BluetoothAdapter
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.juul.kable.Characteristic
import com.juul.kable.Peripheral
import com.juul.kable.State
import com.juul.kable.peripheral
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class SerialPortBluetooth(
    sp: SerialParameters,
    private val writeCharacteristic: Characteristic,
    private val readCharacteristic: Characteristic
) : SerialPort(sp) {
    private var peripheral: Peripheral? = null

    override fun write(b: Int) {
        runBlocking {
            peripheral?.write(writeCharacteristic, byteArrayOf(b.toByte()))
        }
    }

    override fun write(bytes: ByteArray) {
        runBlocking {
            peripheral?.write(writeCharacteristic, bytes)
        }
    }

    override fun open() = runBlocking {
        val bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(serialParameters.device)
        peripheral = peripheral(bluetoothDevice)
            .apply {
                connect()
                GlobalScope.launch {
                    state.collect {
                        when (it) {
                            is State.Disconnected -> {
                                Timber.d("Device disconnected ${it.status}")
                                peripheral = null
                            }
                            else -> Timber.d("Device state changed: $it")
                        }
                    }
                }
            }
    }

    override fun read() = runBlocking {
        val data = peripheral?.read(readCharacteristic)
        if (data?.isNotEmpty() == true) {
            return@runBlocking data[0].toInt()
        } else {
            END_OF_STREAM
        }
    }

    override fun read(b: ByteArray, off: Int, len: Int) = runBlocking {
        val data = peripheral?.read(readCharacteristic)
        if (data?.isNotEmpty() == true) {
            data.copyInto(b, 0, off, off + len)
            return@runBlocking data.size
        } else {
            return@runBlocking END_OF_STREAM
        }
    }

    override fun close() {
        runBlocking {
            peripheral?.disconnect()
            peripheral = null
        }
    }

    override fun isOpened() = peripheral != null

    companion object {
        private const val END_OF_STREAM = -1
    }

}
