package com.inhealion.generator.service

import android.os.Parcelable
import android.util.Base64
import com.inhealion.generator.R
import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.device.ErrorCodes
import com.inhealion.generator.device.Generator
import com.inhealion.generator.networking.ApiError
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.utils.ApiErrorStringProvider
import com.inhealion.generator.utils.StringProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

class ImportManager(
    private val listener: ImportStateListener,
    private val api: GeneratorApiCoroutinesClient,
    private val connectionFactory: DeviceConnectionFactory,
    private val stringProvider: StringProvider,
    private val apiErrorStringProvider: ApiErrorStringProvider
) : CoroutineScope {

    var currentState: ImportState = ImportState.Idle
        private set

    private lateinit var generator: Generator

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()


    fun import(importAction: ImportAction) {
        if (currentState !is ImportState.Idle) error("Illegal state $currentState")

        postState(ImportState.Downloading)
        val files = runBlocking { download(importAction) } ?: return
        importToDevice(importAction, files)
    }

    fun cancel() {
        postState(ImportState.Canceled)
        coroutineContext.cancel()
        try {
            generator.close()
        } catch (e: Exception) {
            //Ignore
            Timber.w(e, "Close generator error")
        }
    }

    private fun importToDevice(importAction: ImportAction, files: Map<String, ByteArray>) {
        try {
            if (importAction is ImportAction.UpdateFirmware) {
                if (!importMcuFirmware(importAction.address, files)) return
            }

            postState(ImportState.Connecting)
            generator = connectionFactory.connect(importAction.address)

            val totalSize = files.filter { !it.key.endsWith(".bf") }.values.sumOf { it.size }
            var importedSize = 0
            launch {
                generator.fileImportProgress.onEach {
                    postState(
                        ImportState.Importing(
                            if (totalSize == 0) 0 else (importedSize * 100) / totalSize,
                            FileType.fromFileName(it.fileName)
                        )
                    )
                    importedSize += it.progress
                }
            }

            if (importAction is ImportAction.ImportFolder) {
                generator.eraseByExt("txt")
                generator.eraseByExt("pls")
            }

            FileType.values().filter { it != FileType.MCU }.forEach {
                if (!importByExt(generator, files, it.extension)) {
                    postState(ImportState.Error(stringProvider.getString(R.string.error_file_transfer)))
                    return
                }
            }
            println("RRR > done")
            postState(ImportState.Finished)
        } catch (e: Exception) {
            Timber.e(e, "Unable to import data")
            postState(
                ImportState.Error(stringProvider.getString(R.string.connection_error_message, importAction.address), e)
            )
        } finally {
            generator.close()
        }

    }

    private suspend fun download(importAction: ImportAction) =
        try {
            when (importAction) {
                is ImportAction.ImportFolder -> downloadFolder(importAction.folderId)
                is ImportAction.UpdateFirmware -> downloadFirmware(importAction.version)
            }
        } catch (e: Exception) {
            val message = when (e) {
                is ApiError -> apiErrorStringProvider.getErrorMessage(e)
                else -> stringProvider.getString(R.string.download_folder_error)
            }
            postState(ImportState.Error(message, e))
            null
        }

    private fun postState(state: ImportState) {
        currentState = state
        listener.onStateChanged(state)
    }

    private fun importMcuFirmware(address: String, files: Map<String, ByteArray>): Boolean {
        postState(ImportState.Connecting)
        generator = connectionFactory.connect(address)

        postState(ImportState.Importing(0, FileType.MCU))
        val data = files.entries.firstOrNull { it.key.endsWith(".bf") }?.value ?: return true
        if (!importMcuFirmwareData(generator, data)) {
            postState(ImportState.Error(stringProvider.getString(R.string.error_file_transfer)))
            return false
        }
        generator.reboot()
        generator.close()

        postState(ImportState.Rebooting)
        runBlocking { awaitDeviceConnection(address) }
        return true
    }

    private fun importByExt(generator: Generator, files: Map<String, ByteArray>, ext: String): Boolean {
        files.filter { it.key.endsWith(".$ext") }
            .forEach { (name, data) ->
                if (generator.putFile(name, data) != ErrorCodes.NO_ERROR) {
                    return false
                }
            }
        return true
    }

    private suspend fun awaitDeviceConnection(address: String) {
        var generator: Generator? = null
        repeat(5) {
            delay(10000)
            try {
                generator = connectionFactory.connect(address)
                return
            } catch (e: Exception) {
                //ignore
            } finally {
                runCatching { generator?.close() }
            }
        }
    }

    private fun importMcuFirmwareData(generator: Generator, data: ByteArray): Boolean {
        val strData = String(data)
        val version = "<version>(\\d+)\\.(\\d+)\\.(\\d+)</version>".toRegex().find(strData)?.groupValues

        val calendar = Calendar.getInstance()
        val buffer = mutableListOf(
            0x80.toByte(),
            version?.get(1)?.toByteOrNull() ?: (calendar.get(Calendar.YEAR) - 2000).toByte(),
            version?.get(2)?.toByteOrNull() ?: calendar.get(Calendar.MONTH).toByte(),
            version?.get(3)?.toByteOrNull() ?: calendar.get(Calendar.DATE).toByte()
        )

        "<chunk>(.*)</chunk>".toRegex().findAll(strData).forEach {
            buffer.addAll(Generator.romBAPrepareMCUFirmware(Base64.decode(it.groupValues[1], Base64.DEFAULT)))
        }

        val totalSize = buffer.size
        var importedSize = 0
        launch {
            generator.fileImportProgress.onEach {
                postState(ImportState.Importing((importedSize * 100) / totalSize, FileType.MCU))
                importedSize += it.progress
            }
        }

        return generator.putFile("fw.bf", buffer.toByteArray()) == ErrorCodes.NO_ERROR
    }

    private suspend fun downloadFirmware(version: String): Map<String, ByteArray> {
        val path = api.downloadFirmware(version).firstOrNull() ?: return emptyMap()
        return getFiles(path)
    }

    private suspend fun downloadFolder(folderId: String): Map<String, ByteArray> {
        val path = api.downloadFolder(folderId).firstOrNull() ?: return emptyMap()
        return getFiles(path)
    }

    private fun getFiles(path: String) =
        File(path).listFiles()?.map { it.name to it.readBytes() }?.toMap() ?: emptyMap()


    companion object {
        private val FILES_IMPORT_ORDER = listOf("rbf", "srec", "bin", "txt", "pls")
    }

}

enum class FileType(val extension: String) {
    MCU("bf"),
    FPGA("rbf"),
    BATTERY_CALIBRATION("srec"),
    USB_CHARGER("bin"),
    FREQUENCY("txt"),
    PLAYLIST("pls"),
    UNKNOWN("unknown");

    companion object {
        fun fromFileName(fileName: String): FileType =
            values().firstOrNull { fileName.endsWith(it.extension) } ?: UNKNOWN
    }
}

sealed class ImportState : Parcelable {
    @Parcelize
    object Idle : ImportState()

    @Parcelize
    object Connecting : ImportState()

    @Parcelize
    object Downloading : ImportState()

    @Parcelize
    object Rebooting : ImportState()

    @Parcelize
    data class Importing(val progress: Int, val fileType: FileType) : ImportState()

    @Parcelize
    data class Error(val message: String, val error: Throwable? = null) : ImportState()

    @Parcelize
    object Canceled : ImportState()

    @Parcelize
    object Finished : ImportState()
}

interface ImportStateListener {
    fun onStateChanged(importState: ImportState)
}
