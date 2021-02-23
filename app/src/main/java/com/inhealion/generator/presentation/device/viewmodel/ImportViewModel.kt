package com.inhealion.generator.presentation.device.viewmodel

import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import kotlinx.coroutines.runBlocking

class ImportViewModel(
    val importAction: ImportAction,
    private val deviceRepository: DeviceRepository
) : BaseViewModel<Nothing>() {

    val showDiscovery = ActionLiveData()

    fun import() {
        val device = runBlocking { deviceRepository.get() }.valueOrNull()
        if (device == null) {
            showDiscovery.sendAction()
            return
        }
    }

}
