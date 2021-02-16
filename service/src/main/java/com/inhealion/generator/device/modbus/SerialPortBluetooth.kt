package com.inhealion.generator.device.modbus

import android.bluetooth.BluetoothAdapter
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.juul.kable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.io.IOException
import java.util.*

class SerialPortBluetooth(
    sp: SerialParameters,
    private val writeCharacteristic: Characteristic
) : SerialPort(sp) {
    private var peripheral: Peripheral? = null
    private val scope = GlobalScope

    private var connectionStateJob: Job? = null

    override fun write(b: Int) {
        runBlocking(scope.coroutineContext) {
            peripheral?.connect()
            println("TTT > write byte to $peripheral: $b")
            peripheral?.write(writeCharacteristic, byteArrayOf(b.toByte()), WriteType.WithoutResponse)
            println("TTT > write byte finished")

        }
    }

    override fun write(bytes: ByteArray) {
        runBlocking(scope.coroutineContext) {
            peripheral?.connect()
            println("TTT > write data to $peripheral: ${bytes.contentToString()}")
            peripheral?.write(writeCharacteristic, bytes, WriteType.WithoutResponse)
            println("TTT > write data finished")
        }
    }

    override fun open() {
        val bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(serialParameters.device)
        peripheral = scope.peripheral(bluetoothDevice)
        //connectionStateJob = scope.launch { connectionStateHandlerJob() }
    }


    override fun read() = runBlocking(scope.coroutineContext) {
        withTimeoutOrNull(1000) {
            peripheral?.connect()

            val data = peripheral
                ?.observe(writeCharacteristic)
                ?.first()

            println("TTT > read data from $peripheral: ${data.contentToString()}")
            if (data?.isNotEmpty() == true) {
                data[0].toInt()
            } else {
                throw ModbusIOException("Read timeout")
            }
        } ?: throw ModbusIOException("Read timeout")
    }

    override fun read(b: ByteArray, off: Int, len: Int) = runBlocking(scope.coroutineContext) {
        throw NotImplementedError()
    }

    override fun close() {
        runBlocking {
            connectionStateJob?.cancel()
            peripheral = null
        }
    }

    override fun isOpened(): Boolean {
        return peripheral != null
    }

    private suspend fun connectionStateHandlerJob() = peripheral?.let {
        it.state.collect { state ->
            Timber.d("Device state changed: $state")
        }
    }

    companion object {
        private const val END_OF_STREAM = -1
    }

}
