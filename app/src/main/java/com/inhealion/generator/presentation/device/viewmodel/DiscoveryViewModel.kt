package com.inhealion.generator.presentation.device.viewmodel

import androidx.lifecycle.viewModelScope
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.device.bluetooth.BleDeviceScanner
import com.inhealion.generator.device.model.BleDevice
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.model.State
import com.inhealion.generator.model.onFailure
import com.inhealion.generator.model.onSuccess
import com.inhealion.generator.presentation.device.adapter.DeviceUiModel
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class DiscoveryViewModel(
    private val scanner: BleDeviceScanner,
    private val deviceRepository: DeviceRepository
) : BaseViewModel<List<BleDevice>>() {

    val finish = ActionLiveData()

    var isDeviceSelected = false
        private set

    private val list = mutableListOf<BleDevice>()

    fun start() {
        list.clear()
        isDeviceSelected = false

        viewModelScope.launch {
            postState(State.InProgress())
            scanner.scan()
                .onEach {
                    if (!list.contains(it)) list.add(it)
                    postState(State.Success(list))
                }.launchIn(viewModelScope)
        }
    }

    fun saveDevice(device: DeviceUiModel) {
        viewModelScope.launch {
            deviceRepository.save(BleDevice(device.name, device.address))
                .onSuccess {
                    isDeviceSelected = true
                    finish.sendAction()
                }
                .onFailure { postState(State.Failure(it.message!!)) }
        }
    }
}
