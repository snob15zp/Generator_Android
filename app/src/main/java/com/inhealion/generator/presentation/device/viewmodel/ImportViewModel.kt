package com.inhealion.generator.presentation.device.viewmodel

import androidx.lifecycle.viewModelScope
import com.inhealion.generator.R
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.device.Generator
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.model.ErrorDialogData
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import com.inhealion.generator.utils.StringProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.lang.Exception

class ImportViewModel(
    val importAction: ImportAction,
    private val deviceRepository: DeviceRepository,
    private val connectionFactory: DeviceConnectionFactory,
    private val stringProvider: StringProvider
) : BaseViewModel<Nothing>() {

    val showDiscovery = ActionLiveData()

    fun import() {
        val device = runBlocking { deviceRepository.get() }.valueOrNull()
        if (device == null) {
            showDiscovery.sendAction()
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                connectionFactory.connect(device.address).use {

                }
            } catch (e: Throwable) {
                Timber.e(e, "Unable to import data")
                val deviceName = device.name ?: getString(R.string.device_unknown)
                error.postValue(
                    ErrorDialogData(
                        getString(R.string.connection_error_title),contextgetString(R.string.connection_error_message, deviceName)
                    )
                )
            }
        }
    }
}
