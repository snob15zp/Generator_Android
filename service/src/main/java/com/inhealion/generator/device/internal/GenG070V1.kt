package com.inhealion.generator.device.internal

import com.inhealion.generator.device.ErrorCodes
import com.inhealion.generator.device.Generator
import com.inhealion.service.BuildConfig
import com.intelligt.modbus.jlibmodbus.Modbus
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory
import com.intelligt.modbus.jlibmodbus.msg.base.ModbusFileRecord
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPortFactoryBluetooth
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils
import com.juul.kable.characteristicOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.IOException

class GenG070V1(address: String) : Generator {
    override var ready: Boolean = false
        private set

    override var version: String? = null
        private set

    override var serial: ByteArray? = null
        private set


    private val modbusMasterRTU: ModbusMaster

    private val _fileImportProgress = MutableSharedFlow<Int>()
    override val fileImportProgress: Flow<Int> get() = _fileImportProgress

    init {
        Modbus.setLogLevel(if (BuildConfig.DEBUG) Modbus.LogLevel.LEVEL_DEBUG else Modbus.LogLevel.LEVEL_RELEASE)

        val serialParameters = SerialParameters().apply {
            device = address
        }
        val writeCharacteristic = characteristicOf(SERVICE_UUID, WRITE_CHARACTERISTICS_UUID)
        val readCharacteristic = characteristicOf(SERVICE_UUID, READ_CHARACTERISTICS_UUID)
        SerialUtils.setSerialPortFactory(SerialPortFactoryBluetooth(writeCharacteristic, readCharacteristic))
        modbusMasterRTU = ModbusMasterFactory.createModbusMasterRTU(serialParameters)
        if (!tryToInit()) {
            throw IOException("Unable to init device")
        }
    }

    override fun tryToInit(): Boolean {
        try {
            println("TTT > connect")
            modbusMasterRTU.setResponseTimeout(750)
            modbusMasterRTU.connect()
            repeat(2) {
                try {
                    println("TTT > read version $it")
                    val versionData = modbusMasterRTU.readInputRegisters(SERVER_ADDRESS, VERSION_REGISTER_ADDR, 3)
                    this.version = "${versionData[0]}.${versionData[1]}.${versionData[2]}"

                } catch (e: Exception) {
                    println("TTT > Error: unable to read version")
                    //Ignore exception
                }
            }
            if (this.version == null) {
                return false
            }

            println("TTT > read serial")
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

    override fun eraseAll(): ErrorCodes {
        TODO("Not yet implemented")
    }

    override fun eraseByExt(ext: String): ErrorCodes {
        TODO("Not yet implemented")
    }

    override fun putFile(fileName: String, content: ByteArray): ErrorCodes {
        return try {
            modbusMasterRTU.setResponseTimeout(30000)
            var sending = 0
            println("TTT > write file $fileName, ${content.size}")
            Lfov(fileName, content, MAX_FILENAME_SIZE, MAX_ITEM_SIZE).forEach {
                println("TTT > ---- write chunk ${it.size}")
                modbusMasterRTU.writeFileRecord(SERVER_ADDRESS, ModbusFileRecord(0, 0, it))
                sending += it.size
                _fileImportProgress.tryEmit((sending.toFloat() / content.size * 100).toInt())
            }
            ErrorCodes.NO_ERROR
        } catch (e: Exception) {
            Timber.e(e, "Unable to send file $fileName")
            ErrorCodes.FATAL_ERROR
        }
    }

    override fun close() = modbusMasterRTU.disconnect()

    companion object {
        private const val SERVICE_UUID = "49535343-fe7d-4ae5-8fa9-9fafd205e455"
        private const val READ_CHARACTERISTICS_UUID = "49535343-8841-43f4-a8d4-ecbe34729bb3"
        private const val WRITE_CHARACTERISTICS_UUID = "49535343-1e4d-4bd9-ba61-23c647249616"


        /**
         * Максимальный передаваемый юнит
         **/
        private const val MAX_ITEM_SIZE = 240

        /**
         * Максимальный размер названия файла
         **/
        const val MAX_FILENAME_SIZE = 12


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
        const val EraseFilenameRegAddr: UShort = 56u

        /**
         * Адрес бита очистки flash
         **/
        const val EraseAllCoilAddr: UShort = 128u

        /**
         * Адрес бита удаления файла
         **/
        const val EraseFilenameCoilAddr: UShort = 129u
    }
}
