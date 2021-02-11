package com.inhealion.generator.device.internal

import com.inhealion.generator.device.ErrorCodes
import com.inhealion.generator.device.Generator
import com.inhealion.generator.device.modbus.SerialPortFactoryBluetooth
import com.inhealion.service.BuildConfig
import com.intelligt.modbus.jlibmodbus.Modbus
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterRTU
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters
import com.intelligt.modbus.jlibmodbus.serial.SerialPortAbstractFactory
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils
import kotlinx.coroutines.flow.Flow
import java.io.ByteArrayInputStream

class GenG070V1 : Generator {
    override val ready: Boolean
        get() = TODO("Not yet implemented")

    override val version: String
        get() = TODO("Not yet implemented")

    override val serial: ByteArray
        get() = TODO("Not yet implemented")

    private val modbusMasterRTU: ModbusMaster

    init {
        Modbus.setLogLevel(if (BuildConfig.DEBUG) Modbus.LogLevel.LEVEL_DEBUG else Modbus.LogLevel.LEVEL_RELEASE)

        val serialParameters = SerialParameters()
        SerialUtils.setSerialPortFactory(SerialPortFactoryBluetooth())
        modbusMasterRTU = ModbusMasterFactory.createModbusMasterRTU(serialParameters)
    }

    override fun tryToInit(): Boolean {
        modbusMasterRTU.connect()

        return true
    }

    override fun eraseAll(): ErrorCodes {
        TODO("Not yet implemented")
    }

    override fun eraseByExt(ext: String): ErrorCodes {
        TODO("Not yet implemented")
    }

    override fun putFile(fileName: String, content: ByteArrayInputStream): ErrorCodes {
        TODO("Not yet implemented")
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

    @ExperimentalUnsignedTypes
    companion object {
        /**
         * Максимальный размер названия файла
         **/
        const val MaxFilenameSz = 12


        /**
         * Адрес по-умолчанию
         **/
        const val DefaultAddress: UByte = 10u

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
