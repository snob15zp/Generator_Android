package com.inhealion.generator.service

import android.os.Parcelable
import android.util.Base64
import androidx.annotation.Keep
import com.inhealion.generator.R
import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.device.ErrorCodes
import com.inhealion.generator.device.Generator
import com.inhealion.generator.device.internal.GenG070V1.Companion.MAX_FILENAME_SIZE
import com.inhealion.generator.device.internal.Lfov
import com.inhealion.generator.networking.ApiError
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.utils.ApiErrorStringProvider
import com.inhealion.generator.utils.StringProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.single
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.coroutines.CoroutineContext

class ImportManager(
    private val api: GeneratorApiCoroutinesClient,
    private val connectionFactory: DeviceConnectionFactory,
    private val stringProvider: StringProvider,
    private val apiErrorStringProvider: ApiErrorStringProvider
) : CoroutineScope {

    var listener: ImportStateListener? = null

    private var generator: Generator? = null
    private var isCanceled = false

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    fun import(importAction: ImportAction) {
        isCanceled = false
        postState(ImportState.Downloading)
        val files = runBlocking(coroutineContext) { download(importAction) } ?: return
        importToDevice(importAction, files)
    }

    fun cancel() {
        isCanceled = true
        postState(ImportState.Canceling)
        generator?.cancel()
    }

    private fun close() {
        isCanceled = false
        coroutineContext.cancel()
        try {
            generator?.close()
        } catch (e: Exception) {
            //Ignore
            Timber.w(e, "Close generator error")
        }
    }

    private fun importToDevice(importAction: ImportAction, files: List<FileData>) {
        var state: ImportState? = null
        val localFiles = files.toMutableList()
        try {
            if (importAction is ImportAction.UpdateFirmware) {
                localFiles.firstOrNull { it.name.endsWith(".${FileType.MCU.extension}") }
                    ?.let {
                        localFiles.remove(it)
                        if (!importMcuFirmware(importAction.address, it)) return
                    }
            }

            postState(ImportState.Connecting)
            connectionFactory.connect(importAction.address).let { localGenerator ->
                generator = localGenerator

                val totalSize = localFiles.sumOf { it.content.size }
                var importedSize = 0
                launch(coroutineContext) {
                    localGenerator.fileImportProgress.collect {
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
                    localGenerator.eraseByExt(FileType.FREQUENCY.extension)
                    localGenerator.eraseByExt(FileType.PLAYLIST.extension)
                }
                FileType.values().filter { it != FileType.MCU }.forEach {
                    if (!importByExt(localGenerator, localFiles, it.extension)) {
                        return
                    }
                }
            }
            println("RRR > done")
            state = ImportState.Success
        } catch (e: Exception) {
            Timber.e(e, "Unable to import data")
            state = ImportState.Failed(
                stringProvider.getString(
                    R.string.connection_error_message,
                    importAction.address
                ), e
            )
        } finally {
            try {
                generator?.transmitDone()
                generator?.close()
            } catch (e: Exception) {
                // Ignore
            }

            coroutineContext.cancel()
            state?.let { postState(it) }
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
            postState(ImportState.Failed(message, e))
            null
        }

    private fun postState(state: ImportState) {
        if (isCanceled && state is ImportState.Importing) return
        listener?.onStateChanged(state)
    }

    private fun importMcuFirmware(address: String, data: FileData): Boolean {
        postState(ImportState.Connecting)
        connectionFactory.connect(address).let { localGenerator ->
            generator = localGenerator
            postState(ImportState.Importing(0, FileType.MCU))
            if (!importMcuFirmwareData(localGenerator, data.content)) {
                return false
            }
            localGenerator.reboot()
            localGenerator.close()
        }
        postState(ImportState.Rebooting)
        runBlocking { awaitDeviceConnection(address) }
        return true
    }

    private fun importByExt(generator: Generator, files: List<FileData>, ext: String): Boolean {
        files.filter { it.name.endsWith(".$ext") }
            .forEach {
                val result = generator.putFile(it.name, it.content, it.isEncrypted)
                if (result != ErrorCodes.NO_ERROR) {
                    if (result == ErrorCodes.CANCELED) {
                        cancelAndClose(FileType.fromFileName(it.name), generator)
                    } else {
                        postState(ImportState.Failed(stringProvider.getString(R.string.error_file_transfer)))
                    }
                    return false
                }
            }
        return true
    }

    private fun cancelAndClose(fileType: FileType, generator: Generator) {
        try {
            generator.eraseByExt(fileType.extension)
        } catch (e: Exception) {
            //Ignore
        }
        postState(ImportState.Canceled)
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
        launch(coroutineContext) {
            generator.fileImportProgress.collect {
                postState(ImportState.Importing((importedSize * 100) / totalSize, FileType.MCU))
                importedSize += it.progress
            }
        }

        val result = generator.putFile(FileType.MCU.fullName, buffer.toByteArray(), false)
        return if (result != ErrorCodes.NO_ERROR) {
            if (result == ErrorCodes.CANCELED) {
                cancelAndClose(FileType.MCU, generator)
            } else {
                postState(ImportState.Failed(stringProvider.getString(R.string.error_file_transfer)))
            }
            false
        } else {
            true
        }
    }

    private suspend fun downloadFirmware(version: String): List<FileData> {
        val path = api.downloadFirmware(version).firstOrNull() ?: return emptyList()
        return getFiles(path)
    }

    private suspend fun downloadFolder(folderId: String): List<FileData> {
        val folder = api.fetchFolder(folderId).single()
        val path = api.downloadFolder(folderId).firstOrNull() ?: return emptyList()
        val files = getFiles(path).sortedBy { it.name }
        val playList = createPlaylist(files)
        return files.mapIndexed { index, fileData ->
            FileData(
                "${index}.txt",
                fileData.content,
                folder.isEncrypted
            )
        } + playList
    }

    private fun createPlaylist(files: List<FileData>): FileData {
        val buffer = ByteArray(files.size * MAX_FILENAME_SIZE) { 0x20 }
        var idx: Int
        files.forEachIndexed { fileIdx, fileData ->
            idx = 0
            Lfov.truncatedFileName(fileData.name, MAX_FILENAME_SIZE, true)
                .toByteArray(Charsets.UTF_8)
                .forEach { buffer[fileIdx * MAX_FILENAME_SIZE + idx++] = it }
        }
        return FileData(FileType.PLAYLIST.fullName, buffer, false)
    }

    private fun getFiles(path: String) =
        File(path).listFiles()
            //  Exclude pls file. It will creating programmatically.
            ?.filter { !it.name.endsWith(FileType.PLAYLIST.extension) }
            ?.map {
                val name = when (val type = FileType.fromFileName(it.name)) {
                    FileType.FREQUENCY,
                    FileType.UNKNOWN -> it.name
                    else -> type.fullName
                }
                FileData(name, it.readBytes(), false)
            } ?: emptyList()

    fun reset() {
        postState(ImportState.Idle)
        close()
        listener?.onStateChanged(ImportState.Idle)
    }

    companion object {
        private val FILES_IMPORT_ORDER = listOf("rbf", "srec", "bin", "txt", "pls")
    }
}

class FileData(val name: String, val content: ByteArray, val isEncrypted: Boolean)

enum class FileType(val extension: String, val fileName: String) {
    MCU("bf", "fw"),
    FPGA("rbf", "FPGA"),
    BATTERY_CALIBRATION("srec", "bq28z610"),
    USB_CHARGER("bin", "tps65987"),
    FREQUENCY("txt", ""),
    PLAYLIST("pls", "freq"),
    UNKNOWN("unknown", "");

    val fullName = "$fileName.$extension"

    companion object {
        fun fromFileName(fileName: String): FileType =
            values().firstOrNull { File(fileName).extension == it.extension } ?: UNKNOWN
    }
}

@Keep
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
    data class Failed(val message: String, val error: Throwable? = null) : ImportState()

    @Parcelize
    object Canceling : ImportState()

    @Parcelize
    object Canceled : ImportState()

    @Parcelize
    object Success : ImportState()

    val isActive: Boolean get() = this is Connecting || this is Downloading || this is Rebooting || this is Importing || this is Canceling
}

interface ImportStateListener {
    fun onStateChanged(importState: ImportState)
}
