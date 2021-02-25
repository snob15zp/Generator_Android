package com.inhealion.generator.presentation.device.viewmodel

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.R
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.device.Generator
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.model.ErrorDialogData
import com.inhealion.generator.model.State
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import com.inhealion.generator.utils.StringProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.lang.Exception
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ImportViewModel(
    val importAction: ImportAction,
    private val deviceRepository: DeviceRepository,
    private val api: GeneratorApiCoroutinesClient,
    private val connectionFactory: DeviceConnectionFactory,
    private val stringProvider: StringProvider
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
            currentAction.postValue(stringProvider.getString(R.string.action_connectiong))

            state.postValue(State.InProgress)
            try {
                connectionFactory.connect(device.address).use { generator ->
                    currentAction.postValue(stringProvider.getString(R.string.action_download))

                    val files = mutableMapOf<String, ByteArray>()
                    when (importAction) {
                        is ImportAction.ImportFolder -> {
                            val inputStream = api.downloadFolder(importAction.folderId).firstOrNull() ?: return@launch

                            ZipInputStream(inputStream).use { zipInputStream ->
                                var zipEntry: ZipEntry? = zipInputStream.nextEntry
                                while (zipEntry != null) {

                                    files[zipEntry.name] = zipInputStream.readBytes()
                                    zipInputStream.closeEntry()

                                    zipEntry = zipInputStream.nextEntry
                                }
                            }

                            files.filter { it.key.endsWith(".txt") }
                                .forEach { generator.putFile(it.key, ByteArrayInputStream(it.value)) }

                            files.filter { it.key.endsWith(".pls") }
                                .forEach { generator.putFile(it.key, ByteArrayInputStream(it.value)) }

                        }
                        ImportAction.UpdateFirmware -> Unit
                    }
                }
            } catch (e: Throwable) {
                Timber.e(e, "Unable to import data")
                val deviceName = device.name ?: stringProvider.getString(R.string.device_unknown)
                state.postValue(State.Failure(stringProvider.getString(R.string.connection_error_message, deviceName)))
            }
        }
    }
}
