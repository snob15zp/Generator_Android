package com.inhealion.generator.device.modbus

import android.bluetooth.BluetoothAdapter
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.juul.kable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import okhttp3.internal.toHexString
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

@ExperimentalCoroutinesApi
class SerialPortBluetooth(
    sp: SerialParameters,
    private val writeCharacteristic: Characteristic,
    private val readCharacteristic: Characteristic
) : SerialPort(sp) {
    private var peripheral: Peripheral? = null
    private val scope = GlobalScope
    private var connectionJob: Job? = null

    private val operationChannel = BroadcastChannel<Operation>(Channel.CONFLATED)
    private val stateFlow = MutableStateFlow(DeviceState.DISCONNECTED)

    private var notificationInputStream: InputStream? = null

    override fun write(b: Int) {
        operationChannel.offer(Operation.Write(ByteBuffer.allocate(1).put(b.toByte())))
        waitFor(DeviceState.WRITE)
    }

    override fun write(bytes: ByteArray) {
        println("TTT > start write to device")
        if (!isOpened) {
            throw IOException("Port not opened")
        }
        operationChannel.offer(
            Operation.Write(
                ByteBuffer.allocate(bytes.size + 4)
                    .putShort(0x1b00)
                    .put(bytes)
                    .putShort(0x1b01)
            )
        )
        waitFor(DeviceState.WRITE)
        println("TTT > finish write to device")
    }

    override fun open() {
        val bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(serialParameters.device)
        peripheral = scope.peripheral(bluetoothDevice)
        connectionJob = scope.launch {
            peripheral?.run {
                connect()
                launch { connectionStateHandler(this@run) }
                launch { observeCharacteristicNotifications(this@run) }
                launch { operationHandler(this@run) }
            }
        }
        println("TTT > waiting for connect...")
        waitFor(DeviceState.CONNECTED, 10000)
        println("TTT > connected")
    }

    private suspend fun observeCharacteristicNotifications(peripheral: Peripheral) =
        peripheral.observe(writeCharacteristic).collect {
            println("TTT > read data from read characteristics: ${it.toByteString()}")
            notificationInputStream = ByteArrayInputStream(it)
            this@SerialPortBluetooth.stateFlow.emit(DeviceState.READ)
        }

    private suspend fun operationHandler(peripheral: Peripheral) = operationChannel.consumeEach {
        when (it) {
            is Operation.Write -> kotlin.runCatching {
                println("TTT > write byte array ${it.data.array().toByteString()}")
                peripheral.write(writeCharacteristic, it.data.array(), WriteType.WithoutResponse)
                this@SerialPortBluetooth.stateFlow.emit(DeviceState.WRITE)
            }
            else -> Unit
        }
    }

    override fun read() = readInternal { it.read() }

    override fun read(b: ByteArray, off: Int, len: Int) = readInternal { it.read(b, off, len) }

    private fun readInternal(readAction: (InputStream) -> Int): Int {
        println("TTT > start read from device")
        if (!isOpened) {
            throw IOException("Port not opened")
        }
        if (notificationInputStream?.available() ?: 0 == 0) waitFor(DeviceState.READ)
        notificationInputStream?.run {
            val data = readAction(this)
            println("TTT > read data ${data.toHexString()}")
            if (data == END_OF_STREAM) {
                this.close()
                notificationInputStream = null
                throw IOException("Read timeout")
            }
            return data
        } ?: throw IOException("Read timeout")
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

    private fun waitFor(state: DeviceState, timeout: Long = 5000) = runBlocking {
        withTimeout(timeout) { stateFlow.filter { it == state }.first() }
    }

    private suspend fun connectionStateHandler(peripheral: Peripheral) =
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
        object Read : Operation()
    }

    companion object {
        private const val END_OF_STREAM = -1
    }
}
