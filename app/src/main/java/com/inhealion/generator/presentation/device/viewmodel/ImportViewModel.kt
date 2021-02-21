package com.inhealion.generator.presentation.device.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.model.State
import com.inhealion.generator.presentation.device.ImportAction
import kotlinx.coroutines.runBlocking

class ImportViewModel(
    val importAction: ImportAction,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    val showDiscovery = ActionLiveData()
    val state = MutableLiveData<State>()


    fun import() {
        val device = runBlocking { deviceRepository.get() }.valueOrNull()
        if (device == null) {
            showDiscovery.sendAction()
            return
        }
    }

}
