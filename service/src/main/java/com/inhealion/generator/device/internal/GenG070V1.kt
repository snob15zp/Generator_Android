package com.inhealion.generator.device.internal

import com.inhealion.generator.device.ErrorCodes
import com.inhealion.generator.device.Generator
import com.inhealion.generator.device.modbus.SerialPortFactoryBluetooth
import com.inhealion.service.BuildConfig
import com.intelligt.modbus.jlibmodbus.Modbus
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory
import com.intelligt.modbus.jlibmodbus.msg.base.ModbusFileRecord
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils
import com.juul.kable.characteristicOf
import kotlinx.coroutines.flow.Flow
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer

class GenG070V1(address: String) : Generator {
    override val ready: Boolean
        get() = TODO("Not yet implemented")

    override val version: String
        get() = TODO("Not yet implemented")

    override val serial: ByteArray
        get() = TODO("Not yet implemented")

    private val modbusMasterRTU: ModbusMaster

    init {
        Modbus.setLogLevel(if (BuildConfig.DEBUG) Modbus.LogLevel.LEVEL_DEBUG else Modbus.LogLevel.LEVEL_RELEASE)

        val serialParameters = SerialParameters().apply {
            device = address
        }
        val readCharacteristic = characteristicOf(SERVICE_UUID, READ_CHARACTERISTICS_UUID)
        val writeCharacteristic = characteristicOf(SERVICE_UUID, WRITE_CHARACTERISTICS_UUID)
        SerialUtils.setSerialPortFactory(SerialPortFactoryBluetooth(writeCharacteristic, readCharacteristic))
        modbusMasterRTU = ModbusMasterFactory.createModbusMasterRTU(serialParameters)
    }

    override fun tryToInit(): Boolean {
        return true
    }

    override fun eraseAll(): ErrorCodes {
        TODO("Not yet implemented")
    }

    override fun eraseByExt(ext: String): ErrorCodes {
        TODO("Not yet implemented")
    }

    override fun putFile(fileName: String, content: ByteArrayInputStream): ErrorCodes {
        try {
            val data = content.readBytes().asList().chunked(224)

            modbusMasterRTU.writeFileRecord(DefaultAddress, ModbusFileRecord(0, 0, data))

            return ErrorCodes.NO_ERROR
        } catch (e: Exception) {
            return ErrorCodes.FATAL_ERROR
        }
    }

    override val putFilePart: Flow<Triple<String, Int, Int>>
        get() = TODO("Not yet implemented")

    override fun disconnect() = modbusMasterRTU.disconnect()

    override fun bootloaderUploadMcuFwChunk(chunk: ByteArray): Boolean {
        TODO("Not yet implemented")
    }

    override fun bootloaderRunMcuFw() {
        TODO("Not yet implemented")
    }

    override fun bootloaderReset() {
        TODO("Not yet implemented")
    }

    companion object {
        private const val SERVICE_UUID = "49535343-fe7d-4ae5-8fa9-9fafd205e455"
        private const val READ_CHARACTERISTICS_UUID = "49535343-8841-43f4-a8d4-ecbe34729bb3"
        private const val WRITE_CHARACTERISTICS_UUID = "49535343-1e4d-4bd9-ba61-23c647249616"


        /**
         * Максимальный передаваемый юнит
        **/
        private const val MaxItemSize = 242

        /**
         * Максимальный размер названия файла
         **/
        const val MaxFilenameSz = 12


        /**
         * Адрес по-умолчанию
         **/
        const val DefaultAddress: Int = 0x0A

        /**
         * Адрес регистра версии
         **/
        const val VersionRegisterAddr: UShort = 16u

        /**
         * Адрес регистра версии
         **/
        const val SerialRegisterAddr: UShort = 48u


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
