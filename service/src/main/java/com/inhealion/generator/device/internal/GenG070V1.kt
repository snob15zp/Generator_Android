package com.inhealion.generator.device.internal

import android.content.Context
import com.inhealion.generator.device.ErrorCodes
import com.inhealion.generator.device.FileImport
import com.inhealion.generator.device.Generator
import com.inhealion.service.BuildConfig
import com.intelligt.modbus.jlibmodbus.Modbus
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory
import com.intelligt.modbus.jlibmodbus.msg.base.ModbusFileRecord
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPortFactoryBluetooth
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils
import com.intelligt.modbus.jlibmodbus.utils.DataUtils
import com.intelligt.modbus.jlibmodbus.utils.ModbusExceptionCode
import com.juul.kable.characteristicOf
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Phaser
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class GenG070V1(address: String, context: Context) : Generator {
    override var ready: Boolean = false
        private set

    override var version: String? = null
        private set

    override var serial: ByteArray? = null
        private set


    private val modbusMasterRTU: ModbusMaster

    private val _fileImportProgress = Channel<FileImport>()
    override val fileImportProgress: Flow<FileImport> get() = _fileImportProgress.consumeAsFlow()

    private val canceled = AtomicBoolean(false)
    private val cancelPhaser = Phaser(1)

    init {
        Modbus.setLogLevel(if (BuildConfig.DEBUG) Modbus.LogLevel.LEVEL_DEBUG else Modbus.LogLevel.LEVEL_RELEASE)

        val serialParameters = SerialParameters().apply {
            device = address
        }
        val writeCharacteristic = characteristicOf(SERVICE_UUID, WRITE_CHARACTERISTICS_UUID)
        SerialUtils.setSerialPortFactory(SerialPortFactoryBluetooth(context, writeCharacteristic))
        modbusMasterRTU = ModbusMasterFactory.createModbusMasterRTU(serialParameters)
        if (!tryToInit()) {
            throw IOException("Unable to init device")
        }
    }

    override fun tryToInit(): Boolean {
        try {
            log("connect")
            canceled.set(false)

            modbusMasterRTU.setResponseTimeout(750)
            modbusMasterRTU.connect()
            version = readVersion() ?: return false

            log("read serial")
            serial = modbusMasterRTU.readInputRegisters(SERVER_ADDRESS, SERIAL_REGISTER_ADDR, 6)
                .map { it.toByte() }
                .toByteArray()
            ready = true
            return true
        } catch (e: Exception) {
            Timber.e(e, "Unable to init device")
            close()
            ready = false
            return false
        }
    }

    private fun readVersion(): String? {
        repeat(3) {
            try {
                log("read version $it")
                val versionData = modbusMasterRTU.readInputRegisters(SERVER_ADDRESS, VERSION_REGISTER_ADDR, 3)
                return "${versionData[0]}.${versionData[1]}.${versionData[2]}"

            } catch (e: Exception) {
                log("Error: unable to read version, ${e.message}")
                //Ignore exception
            }
        }
        return null
    }

    override fun eraseAll(): ErrorCodes {
        TODO("Not yet implemented")
    }

    override fun eraseByExt(ext: String): ErrorCodes {
        log("Erase by extension $ext")
        var buffer = ext.toByteArray(Charsets.US_ASCII)
        if (buffer.size % 2 == 1) buffer = buffer.plus(0)

        val data = DataUtils.LeToIntArray(buffer)
        modbusMasterRTU.setResponseTimeout(30000)
        try {
            return eraseByExt(data)
        } catch (e: ModbusProtocolException) {
            when (e.exception) {
                ModbusExceptionCode.SLAVE_DEVICE_FAILURE,
                ModbusExceptionCode.SLAVE_DEVICE_BUSY -> try {
                    Timber.w("Device is busy, try send command again")
                    return eraseByExt(data)
                } catch (e: Exception) {
                    Timber.w("Second attempt to erase failed")
                }
                else -> Unit.also { Timber.e(e, "Unable to erase files by extension $ext") }
            }
            return ErrorCodes.FATAL_ERROR
        } catch (e: Exception) {
            Timber.e(e, "Unable to erase files by extension $ext")
            return ErrorCodes.FATAL_ERROR
        }
    }

    private fun eraseByExt(data: IntArray): ErrorCodes {
        modbusMasterRTU.writeMultipleRegisters(SERVER_ADDRESS, ERASE_FILENAME_REG_ADDR, data)
        modbusMasterRTU.writeSingleCoil(SERVER_ADDRESS, ERASE_FILENAME_COIL_ADDR, true)
        log("Erase by extension success")
        return ErrorCodes.NO_ERROR
    }

    override fun putFile(fileName: String, content: ByteArray, isEncrypted: Boolean): ErrorCodes {
        return try {
            modbusMasterRTU.setResponseTimeout(30000)
            log("write file $fileName, ${content.size}")
            Lfov(fileName, content, MAX_FILENAME_SIZE, MAX_ITEM_SIZE, isEncrypted).forEach {
                if (canceled.getAndSet(false)) {
                    log("canceled")
                    return ErrorCodes.CANCELED
                }
                log("---- write chunk ${it.size}")
                runBlocking { writeChunk(it) }
                _fileImportProgress.offer(
                    FileImport(fileName, it.size * 2 - it[0].shr(8).and(0xff) - 4 - 1)
                )
            }
            ErrorCodes.NO_ERROR
        } catch (e: Exception) {
            Timber.e(e, "Unable to send file $fileName")
            ErrorCodes.FATAL_ERROR
        }
    }

    override fun reboot(): Boolean {
        return try {
            modbusMasterRTU.setResponseTimeout(1500)
            modbusMasterRTU.writeSingleRegister(SERVER_ADDRESS, 0x20, 1.shl(7))
            true
        } catch (e: Exception) {
            Timber.e(e, "Unable to reboot")
            false
        }
    }

    override fun transmitDone() {
        try {
            modbusMasterRTU.setResponseTimeout(1500)
            modbusMasterRTU.writeSingleRegister(SERVER_ADDRESS, 0x20, 1.shl(8))
        } catch (e: Exception) {
            Timber.w(e, "Unable to send TransmitDone")
        }
    }

    override fun cancel() {
        canceled.set(true)
    }

    override fun close() {
        try {
            _fileImportProgress.close()
            modbusMasterRTU.disconnect()
        } catch (e: Exception) {
            Timber.w(e, "Close operation is failed")
        }
    }

    private suspend fun writeChunk(data: IntArray) {
        repeat(3) {
            try {
                log("---- writeFileRecord $it")
                modbusMasterRTU.writeFileRecord(SERVER_ADDRESS, ModbusFileRecord(0, 0, data))
                return
            } catch (e: ModbusProtocolException) {
                Timber.w("writeFileRecord error: $e")
                when (e.exception) {
                    ModbusExceptionCode.SLAVE_DEVICE_FAILURE,
                    ModbusExceptionCode.SLAVE_DEVICE_BUSY -> delay(1000)
                    else -> throw e
                }
            }
        }
    }

    private fun log(message: String, prefix: String = "GenG070V1 > ") {
        if (LOG_ENABLED)
            Timber.d("$prefix$message")
    }

    companion object {
        private const val LOG_ENABLED = true
        private const val SERVICE_UUID = "49535343-fe7d-4ae5-8fa9-9fafd205e455"
        private const val READ_CHARACTERISTICS_UUID = "49535343-8841-43f4-a8d4-ecbe34729bb3"
        private const val WRITE_CHARACTERISTICS_UUID = "49535343-1e4d-4bd9-ba61-23c647249616"


        /**
         * Максимальный передаваемый юнит
         **/
        private const val MAX_ITEM_SIZE = 128

        /**
         * Максимальный размер названия файла в плэйлисте
         **/
        const val MAX_FILENAME_SIZE = 64

        /**
         * Адрес по-умолчанию
         **/
        const val SERVER_ADDRESS: Int = 0x0A

        /**
         * Адрес регистра версии
         **/
        const val VERSION_REGISTER_ADDR: Int = 0x10

        /**
         * Адрес регистра версии
         **/
        const val SERIAL_REGISTER_ADDR: Int = 0x30


        /**
         * Адрес названия файла для удаления
         **/
        const val ERASE_FILENAME_REG_ADDR: Int = 0x38

        /**
         * Адрес бита очистки flash
         **/
        const val ERASE_ALL_COIL_ADDR: Int = 0x80

        /**
         * Адрес бита удаления файла
         **/
        const val ERASE_FILENAME_COIL_ADDR: Int = 0x81
    }
}
