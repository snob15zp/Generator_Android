package com.inhealion.generator.device.modbus

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Environment
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPort
import com.juul.kable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import okio.ByteString.Companion.toByteString
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class SerialPortBluetooth(
    sp: SerialParameters,
    private val writeCharacteristic: Characteristic,
    private val context: Context
) : SerialPort(sp) {
    private val logFile = File(context.getExternalFilesDir("InHealion"), "modbus.log")
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var peripheral: Peripheral? = null

    private val commandChannel = Channel<Command>()
    private val stateFlow = MutableStateFlow(DeviceState.DISCONNECTED)

    private val buffer = ByteBuffer.allocate(MAX_BUFFER_SIZE)
    private var writePosition = AtomicInteger(0)
    private var readPosition = 0

    override fun write(b: Int) = Unit

    override fun write(bytes: ByteArray) {
        clearBufer()
        log("start write to device, ${bytes.size}")
        if (!isOpened) {
            throw IOException("Port not opened")
        }
        commandChannel.trySend(
            Command.Write(
                mutableListOf<Byte>(0x1b, 0x00, 0x1b, 0x01).apply {
                    addAll(2, escapeBytes(bytes))
                }
            )
        )
        waitFor(DeviceState.WRITE)
        log("finish write to device")
    }

    override fun open() {
        clearBufer()

        val bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(serialParameters.device)
        val peripheral = scope.peripheral(bluetoothDevice)

        log("waiting for connect...")
        runBlocking(scope.coroutineContext) { peripheral.connect() }
        observeStateHandler(peripheral).launchIn(scope)
        observeCharacteristicNotifications(peripheral).launchIn(scope)
        observeCommandChannel(peripheral).launchIn(scope)

        this.peripheral = peripheral

        log("connected")
    }

    private fun observeCharacteristicNotifications(peripheral: Peripheral) =
        peripheral.observe(writeCharacteristic).onEach {
            log("read data from read characteristics: ${it.toByteString()}")
            val data = unescapeBytes(it)
            logToFile("READ", data)
            log("read data ${data.size}, ${buffer.position()} ${writePosition.get()}, $readPosition")
            if (data.size > MAX_BUFFER_SIZE - buffer.position()) {
                throw IOException("Unexpected data size ${data.size}, ${buffer.position()}")
            }
            buffer.put(data)
            writePosition.addAndGet(data.size)
            stateFlow.emit(DeviceState.READ)
        }


    private fun observeCommandChannel(peripheral: Peripheral) = commandChannel.consumeAsFlow().onEach {
        when (it) {
            is Command.Write -> kotlin.runCatching {
                val bytes = it.data.toByteArray()
                logToFile("WRITE", bytes)
                log("write byte array ${bytes.toByteString(0, bytes.size)}")
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
            log("read end")
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

    private fun logToFile(type: String, data: ByteArray) {
        if (!LOG_TO_FILE_ENABLED) return

        if (logFile.exists() && logFile.length() > MAX_LOG_FILE_SIZE) {
            logFile.delete()
        }
        val date = SimpleDateFormat("yyyy-MM-DD HH:mm:ss.SSS", Locale.ROOT).format(Date())
        val value = data.joinToString("") { it.toUByte().toString(16).padStart(2, '0') }
        logFile.appendText("$date $type:$value\r\n")
    }

    enum class DeviceState {
        CONNECTED, DISCONNECTED, WRITE, READ
    }

    sealed class Command {
        data class Write(val data: List<Byte>) : Command()
        object Read : Command()
    }

    private fun log(message: String, prefix: String = "SPBle > ") {
        if (LOG_ENABLED)
            Timber.d("$prefix$message")
    }

    companion object {
        private const val LOG_ENABLED = true
        private const val LOG_TO_FILE_ENABLED = true

        const val MAX_BUFFER_SIZE = 512
        const val MAX_LOG_FILE_SIZE = 10 * 1024 * 1024
        val WRITE_ESCAPE_BYTES = arrayOf<Byte>(0x1b, 0x25)
        const val ESCAPE_CTRL: Byte = 0x1b
    }
}
