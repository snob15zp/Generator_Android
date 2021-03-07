package com.inhealion.generator.presentation.device.viewmodel

import android.content.Context
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.R
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.device.ErrorCodes
import com.inhealion.generator.device.Generator
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.model.State
import com.inhealion.generator.networking.ApiError
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import com.inhealion.generator.utils.ApiErrorStringProvider
import com.inhealion.generator.utils.StringProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.io.File
import java.util.*

class ImportViewModel(
    val importAction: ImportAction,
    private val deviceRepository: DeviceRepository,
    private val api: GeneratorApiCoroutinesClient,
    private val connectionFactory: DeviceConnectionFactory,
    private val stringProvider: StringProvider,
    private val apiErrorStringProvider: ApiErrorStringProvider
) : BaseViewModel<Any>() {

    val showDiscovery = ActionLiveData()
    val currentAction = MutableLiveData<String>()
    val currentProgress = MutableLiveData<Int>()

    var importJob: Job? = null

    var isCanceled: Boolean = false

    fun import() {
        val device = runBlocking { deviceRepository.get() }.valueOrNull()
        if (device == null) {
            showDiscovery.sendAction()
            return
        }

        importJob?.cancel()
        isCanceled = false
        importJob = viewModelScope.launch(Dispatchers.IO) {
            currentAction.postValue(stringProvider.getString(R.string.action_download))
            postState(State.InProgress())
            val files = try {
                when (importAction) {
                    is ImportAction.ImportFolder -> downloadFolder(importAction.folderId)
                    is ImportAction.UpdateFirmware -> downloadFirmware(importAction.version)
                }
            } catch (e: ApiError) {
                postState(State.Failure(apiErrorStringProvider.getErrorMessage(e)))
                return@launch
            } catch (e: Exception) {
                postState(State.Failure(stringProvider.getString(R.string.download_folder_error)))
                return@launch
            }

            try {
                currentAction.postValue(stringProvider.getString(R.string.action_connecting))

                if (importAction is ImportAction.UpdateFirmware) {
                    if (!importMcuFirmware(device.address, files)) return@launch
                }

                connectionFactory.connect(device.address).use { generator ->
                    currentAction.postValue(stringProvider.getString(R.string.action_import))

                    val totalSize = files.filter { !it.key.endsWith(".bf") }.values.sumOf { it.size }
                    var importedSize = 0
                    generator.fileImportProgress.onEach {
                        if (isCanceled) kotlin.runCatching { generator.close() }
                        importedSize += it
                        postState(State.InProgress((importedSize * 100) / totalSize))
                    }.launchIn(viewModelScope)

                    if (importAction is ImportAction.ImportFolder) {
                        generator.eraseByExt("txt")
                        generator.eraseByExt("pls")
                    }

                    FILES_IMPORT_ORDER.forEach {
                        if (!importByExt(generator, files, it)) {
                            postState(State.Failure(stringProvider.getString(R.string.error_file_transfer)))
                            return@launch
                        }
                    }
                    println("RRR > finished")
                }
                println("RRR > done")
                postState(State.success())
            } catch (e: Exception) {
                Timber.e(e, "Unable to import data")
                val deviceName = device.name ?: stringProvider.getString(R.string.device_unknown)
                postState(State.Failure(stringProvider.getString(R.string.connection_error_message, deviceName)))
            }
        }
    }

    fun cancel() {
        isCanceled = true
        importJob?.cancel()
    }

    private suspend fun importMcuFirmware(address: String, files: Map<String, ByteArray>): Boolean {
        connectionFactory.connect(address).use { generator ->
            currentAction.postValue(stringProvider.getString(R.string.action_import_mcu_firmware))
            val data = files.entries.firstOrNull { it.key.endsWith(".bf") }?.value ?: return true
            if (!importMcuFirmwareData(generator, data)) {
                postState(State.Failure(stringProvider.getString(R.string.error_file_transfer)))
                return false
            }
            generator.reboot()
        }
        currentAction.postValue(stringProvider.getString(R.string.action_reboot))
        postState(State.InProgress())
        awaitDeviceConnection(address)
        currentAction.postValue(stringProvider.getString(R.string.action_connecting))
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
        generator.fileImportProgress.onEach {
            if (isCanceled) kotlin.runCatching { generator.close() }
            importedSize += it
            postState(State.InProgress((importedSize * 100) / totalSize))
        }.launchIn(viewModelScope)

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
