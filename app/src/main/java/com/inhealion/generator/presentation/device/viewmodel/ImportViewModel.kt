package com.inhealion.generator.presentation.device.viewmodel

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.R
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.device.Generator
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.model.ErrorDialogData
import com.inhealion.generator.model.State
import com.inhealion.generator.networking.ApiError
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import com.inhealion.generator.utils.ApiErrorStringProvider
import com.inhealion.generator.utils.StringProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.io.path.Path

class ImportViewModel(
    val importAction: ImportAction,
    private val deviceRepository: DeviceRepository,
    private val api: GeneratorApiCoroutinesClient,
    private val connectionFactory: DeviceConnectionFactory,
    private val stringProvider: StringProvider,
    private val apiErrorStringProvider: ApiErrorStringProvider
) : BaseViewModel<Nothing>() {

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
            state.postValue(State.InProgress)
            val files = try {
                when (importAction) {
                    is ImportAction.ImportFolder -> importFolder(importAction.folderId)
                    ImportAction.UpdateFirmware -> emptyMap()
                }
            } catch (e: ApiError) {
                state.postValue(State.Failure(apiErrorStringProvider.getErrorMessage(e)))
                return@launch
            } catch (e: Exception) {
                state.postValue(State.Failure(stringProvider.getString(R.string.download_folder_error)))
                return@launch
            }

            try {
                currentAction.postValue(stringProvider.getString(R.string.action_connectiong))
                connectionFactory.connect(device.address).use { generator ->
                    currentAction.postValue(stringProvider.getString(R.string.action_download))
                }
            } catch (e: Exception) {
                Timber.e(e, "Unable to import data")
                val deviceName = device.name ?: stringProvider.getString(R.string.device_unknown)
                state.postValue(State.Failure(stringProvider.getString(R.string.connection_error_message, deviceName)))
            }
        }
    }

    private suspend fun importFolder(folderId: String): Map<String, ByteArray> {
        val files = mutableMapOf<String, ByteArray>()
        val path = api.downloadFolder(folderId).firstOrNull() ?: return emptyMap()
        File(path).listFiles()?.forEach { files[it.name] = it.readBytes() }
        return files
    }
}
