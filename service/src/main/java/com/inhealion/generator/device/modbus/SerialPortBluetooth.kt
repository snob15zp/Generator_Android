package com.inhealion.generator.device.modbus

import android.bluetooth.BluetoothAdapter
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.juul.kable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger

@ExperimentalCoroutinesApi
class SerialPortBluetooth(
    sp: SerialParameters,
    private val writeCharacteristic: Characteristic,
    private val readCharacteristic: Characteristic
) : SerialPort(sp) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var peripheral: Peripheral? = null

    private val commandChannel = Channel<Command>()
    private val stateFlow = MutableStateFlow(DeviceState.DISCONNECTED)

    private val buffer = ByteBuffer.allocate(1024)
    private var writePosition = AtomicInteger(0)
    private var readPosition = 0

    override fun write(b: Int) {
        commandChannel.offer(Command.Write(ByteBuffer.allocate(1).put(b.toByte())))
        waitFor(DeviceState.WRITE)
    }

    override fun write(bytes: ByteArray) {
        println("TTT > start write to device, ${bytes.size}")
        if (!isOpened) {
            throw IOException("Port not opened")
        }
        commandChannel.offer(
            Command.Write(
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
        val peripheral = scope.peripheral(bluetoothDevice)

        println("TTT > waiting for connect...")
        runBlocking(scope.coroutineContext) { peripheral.connect() }
        observeStateHandler(peripheral).launchIn(scope)
        observeCharacteristicNotifications(peripheral).launchIn(scope)
        observeCommandChannel(peripheral).launchIn(scope)

        this.peripheral = peripheral

        println("TTT > connected")
    }

    private fun observeCharacteristicNotifications(peripheral: Peripheral) =
        peripheral.observe(writeCharacteristic).onEach {
            println("TTT > read data from read characteristics: ${it.toByteString()}")
            buffer.put(it)
            writePosition.addAndGet(it.size)
            stateFlow.emit(DeviceState.READ)
        }


    private fun observeCommandChannel(peripheral: Peripheral) = commandChannel.consumeAsFlow().onEach {
        when (it) {
            is Command.Write -> kotlin.runCatching {
                println("TTT > write byte array ${it.data.array().toByteString()}")
                peripheral.write(writeCharacteristic, it.data.array(), WriteType.WithoutResponse)
                stateFlow.emit(DeviceState.WRITE)
            }
            else -> Unit
        }
    }

    override fun read(): Int {
        if (!isOpened) {
            throw IOException("Port not opened")
        }

        if (writePosition.get() == 0) waitFor(DeviceState.READ, readTimeout.toLong())

        val data = if (readPosition < writePosition.get()) {
            buffer[readPosition++].toInt()
        } else {
            throw IOException("Read timeout")
        }

        clearReadBufferIfNeeded()
        return data
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (!isOpened) {
            throw IOException("Port not opened")
        }

        if (writePosition.get() == 0) waitFor(DeviceState.READ, readTimeout.toLong())

        if (readPosition < writePosition.get()) {
            buffer.array().copyInto(b, off, readPosition, readPosition + len)
            readPosition += len
        } else {
            throw IOException("Read timeout")
        }
        clearReadBufferIfNeeded()
        return len
    }


    private fun clearReadBufferIfNeeded() {
        if (readPosition == writePosition.get()) {
            println("TTT > read end")
            readPosition = 0
            writePosition.set(0)
            buffer.clear()
        }
    }

    override fun close() {
        buffer.clear()

        runBlocking {
            scope.cancel()
            peripheral?.disconnect()
            peripheral = null
        }
    }

    override fun isOpened() = peripheral != null

    private fun waitFor(state: DeviceState, timeout: Long = 5000) = runBlocking {
        withTimeout(timeout) { stateFlow.filter { it == state }.first() }
    }

    private fun observeStateHandler(peripheral: Peripheral) =
        peripheral.state.onEach { state ->
            when (state) {
                is State.Disconnected -> close()
                else -> Unit
            }
            Timber.d("Device state changed: $state")
        }

    enum class DeviceState {
        CONNECTED, DISCONNECTED, WRITE, READ
    }

    sealed class Command {
        data class Write(val data: ByteBuffer) : Command()
        object Read : Command()
    }

    companion object {
        private const val READ_TIMEOUT = 30000L
        private const val END_OF_STREAM = -1
    }
}
