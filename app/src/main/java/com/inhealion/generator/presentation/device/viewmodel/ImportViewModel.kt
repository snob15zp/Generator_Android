package com.inhealion.generator.presentation.device.viewmodel

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
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.File

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

    fun import() {
        val device = runBlocking { deviceRepository.get() }.valueOrNull()
        if (device == null) {
            showDiscovery.sendAction()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            currentAction.postValue(stringProvider.getString(R.string.action_download))
            postState(State.InProgress())
            val files = try {
                when (importAction) {
                    is ImportAction.ImportFolder -> importFolder(importAction.folderId)
                    is ImportAction.UpdateFirmware -> importFirmware(importAction.version)
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
                connectionFactory.connect(device.address).use { generator ->
                    currentAction.postValue(stringProvider.getString(R.string.action_import))

                    generator.eraseByExt("txt")
                    generator.eraseByExt("pls")

                    val totalSize = files.values.sumOf { it.size }
                    var importedSize = 0
                    generator.fileImportProgress.onEach {
                        importedSize += it
                        postState(State.InProgress((importedSize * 100) / totalSize))
                    }.launchIn(viewModelScope)

                    importFwFile(generator, files) ?: return@launch

                    importByExt(generator, files, "rbf") ?: return@launch
                    importByExt(generator, files, "srec") ?: return@launch
                    importByExt(generator, files, "bin") ?: return@launch

                    importByExt(generator, files, "txt") ?: return@launch
                    importByExt(generator, files, "pls") ?: return@launch

                    println("RRR > finished")
                    postState(State.success())
                }
                println("RRR > done")
            } catch (e: Exception) {
                Timber.e(e, "Unable to import data")
                val deviceName = device.name ?: stringProvider.getString(R.string.device_unknown)
                postState(State.Failure(stringProvider.getString(R.string.connection_error_message, deviceName)))
            }
        }
    }

    fun cancel() {

    }

    private fun importByExt(generator: Generator, files: Map<String, ByteArray>, ext: String): Int? {
        var total = 0
        files.filter { it.key.endsWith(".$ext") }
            .forEach { (name, data) ->
                if (generator.putFile(name, data) != ErrorCodes.NO_ERROR) {
                    postState(State.Failure(stringProvider.getString(R.string.error_file_transfer)))
                    return null
                }
                total++
            }
        return total
    }

    private fun importFwFile(generator: Generator, files: Map<String, ByteArray>): Int? {
        var total = 0
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        files.filter { it.key.endsWith(".bf") }.forEach { (name, data) ->
            parser.setInput(ByteArrayInputStream(data), null)
            var eventType = parser.eventType
            while(eventType != XmlPullParser.END_DOCUMENT) {
                when(eventType) {
                    XmlPullParser.START_TAG -> Unit
                    XmlPullParser.END_TAG -> Unit
                    XmlPullParser.TEXT -> Unit
                }
                eventType = parser.next()
            }
        }

        return total
    }

    private suspend fun importFirmware(version: String): Map<String, ByteArray> {
        return mapOf()
    }

    private suspend fun importFolder(folderId: String): Map<String, ByteArray> {
        val files = mutableMapOf<String, ByteArray>()
        val path = api.downloadFolder(folderId).firstOrNull() ?: return emptyMap()
        File(path).listFiles()?.forEach { files[it.name] = it.readBytes() }
        return files
    }
}
