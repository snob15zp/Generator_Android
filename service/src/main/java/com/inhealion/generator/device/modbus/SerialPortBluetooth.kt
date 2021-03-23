package com.inhealion.generator.device.modbus

import android.bluetooth.BluetoothAdapter
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.juul.kable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger

class SerialPortBluetooth(
    sp: SerialParameters,
    private val writeCharacteristic: Characteristic,
    private val readCharacteristic: Characteristic
) : SerialPort(sp) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var peripheral: Peripheral? = null

    private val commandChannel = Channel<Command>()
    private val stateFlow = MutableStateFlow(DeviceState.DISCONNECTED)

    private val buffer = ByteBuffer.allocate(512)
    private var writePosition = AtomicInteger(0)
    private var readPosition = 0

    override fun write(b: Int) = Unit

    override fun write(bytes: ByteArray) {
        clearBufer()
        println("TTT > start write to device, ${bytes.size}")
        if (!isOpened) {
            throw IOException("Port not opened")
        }
        commandChannel.offer(
            Command.Write(
                mutableListOf<Byte>(0x1b, 0x00, 0x1b, 0x01).apply {
                    addAll(2, escapeBytes(bytes))
                }
            )
        )
        waitFor(DeviceState.WRITE)
        println("TTT > finish write to device")
    }

    override fun open() {
        clearBufer()

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
            val data = unescapeBytes(it)
            println("TTT > read data ${data.size}, ${buffer.position()} ${writePosition.get()}, $readPosition")
            buffer.put(data)
            writePosition.addAndGet(data.size)
            stateFlow.emit(DeviceState.READ)
        }


    private fun observeCommandChannel(peripheral: Peripheral) = commandChannel.consumeAsFlow().onEach {
        when (it) {
            is Command.Write -> kotlin.runCatching {
                val bytes = it.data.toByteArray()
                println("TTT > write byte array ${bytes.toByteString(0, bytes.size)}")
                peripheral.write(writeCharacteristic, bytes, WriteType.WithoutResponse)
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

    private fun escapeBytes(bytes: ByteArray): List<Byte> {
        val escapeBytes = mutableListOf<Byte>()
        bytes.forEach {
            if (it in WRITE_ESCAPE_BYTES) {
                escapeBytes.add(ESCAPE_CTRL)
                escapeBytes.add((it - 1).toByte())
            } else {
                escapeBytes.add(it)
            }
        }
        return escapeBytes
    }

    private fun unescapeBytes(bytes: ByteArray): ByteArray {
        val unescapeBytes = mutableListOf<Byte>()
        var idx = 0
        while (idx < bytes.size) {
            if (bytes[idx] == ESCAPE_CTRL) {
                idx++
                unescapeBytes.add((bytes[idx] + 1).toByte())
            } else {
                unescapeBytes.add(bytes[idx])
            }
            idx++
        }
        return unescapeBytes.toByteArray()
    }


    private fun clearReadBufferIfNeeded() {
        if (readPosition >= writePosition.get()) {
            println("TTT > read end")
            readPosition = 0
            writePosition.set(0)
            buffer.clear()
        }
    }

    override fun close() {
        runBlocking {
            peripheral?.disconnect()
            peripheral = null
            scope.cancel()
        }
        clearBufer()
    }

    override fun isOpened() = peripheral != null

    private fun clearBufer() {
        readPosition = 0
        writePosition.set(0)
        buffer.clear()
    }

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
        data class Write(val data: List<Byte>) : Command()
        object Read : Command()
    }

    companion object {
        val WRITE_ESCAPE_BYTES = arrayOf<Byte>(0x1b, 0x25)
        const val ESCAPE_CTRL: Byte = 0x1b
    }
}
