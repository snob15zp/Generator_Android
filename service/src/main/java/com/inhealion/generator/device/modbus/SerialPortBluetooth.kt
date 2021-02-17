package com.inhealion.generator.device.modbus

import android.bluetooth.BluetoothAdapter
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.juul.kable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.nio.ByteBuffer

class SerialPortBluetooth(
    sp: SerialParameters,
    private val writeCharacteristic: Characteristic,
    private val readCharacteristic: Characteristic
) : SerialPort(sp) {
    private var peripheral: Peripheral? = null
    private val scope = GlobalScope
    private var connectionJob: Job? = null

    private val operationChannel = Channel<Operation>()
    private val stateFlow = MutableStateFlow(DeviceState.DISCONNECTED)

    override fun write(b: Int) {
        operationChannel.offer(Operation.Write(ByteBuffer.allocate(1).put(b.toByte())))
        waitFor(DeviceState.WRITE)
    }

    override fun write(bytes: ByteArray) {
        operationChannel.offer(Operation.Write(ByteBuffer.wrap(bytes)))
        waitFor(DeviceState.WRITE)
    }

    override fun open() {
        val bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(serialParameters.device)
        peripheral = scope.peripheral(bluetoothDevice)
        try {
            connectionJob = scope.launch {
                peripheral?.run {
                    connect()
                    launch { connectionStateHandlerJob(this@run) }
                    launch {
                        observe(writeCharacteristic).collect {
                            println("TTT > read data from write characteristics: ${it.toByteString()}")
                        }
                    }
                    launch {
                        observe(writeCharacteristic).collect {
                            println("TTT > read data from read characteristics: ${it.toByteString()}")
                        }
                    }
                    launch {
                        operationChannel.consumeEach {
                            when (it) {
                                is Operation.Write ->
                                    kotlin.runCatching {
                                        println("TTT > write byte array ${it.data.toByteString()}")
                                        write(writeCharacteristic, it.data.array(), WriteType.WithoutResponse)
                                        this@SerialPortBluetooth.stateFlow.emit(DeviceState.WRITE)
                                    }
                            }
                        }
                    }
                }
            }
            waitFor(DeviceState.CONNECTED)
        } catch (e: Exception) {
            Timber.e(e, "Connection error")
        }
    }

    override fun read(): Int {
        throw NotImplementedError()
    }

    override fun read(b: ByteArray, off: Int, len: Int) = runBlocking(scope.coroutineContext) {
        throw NotImplementedError()
    }

    override fun close() {
        runBlocking {
            peripheral?.disconnect()
            peripheral = null
        }
    }

    override fun isOpened(): Boolean {
        return peripheral != null
    }

    private fun waitFor(state: DeviceState) = runBlocking {
        withTimeout(10000) { stateFlow.filter { it == state }.first() }
    }

    private suspend fun connectionStateHandlerJob(peripheral: Peripheral) =
        peripheral.state.collect { state ->
            when (state) {
                State.Connected -> this.stateFlow.emit(DeviceState.CONNECTED)
                is State.Disconnected -> {
                    this.stateFlow.emit(DeviceState.DISCONNECTED)
                    connectionJob?.cancel()
                }
                else -> Unit
            }

            Timber.d("Device state changed: $state")
        }

    enum class DeviceState {
        CONNECTED, DISCONNECTED, WRITE, READ
    }

    sealed class Operation {
        data class Write(val data: ByteBuffer) : Operation()
    }

    companion object {
        private const val END_OF_STREAM = -1
    }
}
