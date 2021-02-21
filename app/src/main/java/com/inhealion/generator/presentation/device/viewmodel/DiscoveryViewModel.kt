package com.inhealion.generator.presentation.device.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.device.bluetooth.BleDeviceScanner
import com.inhealion.generator.device.model.BleDevice
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.model.State
import com.inhealion.generator.model.onFailure
import com.inhealion.generator.model.onSuccess
import com.inhealion.generator.presentation.device.adapter.DeviceUiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class DiscoveryViewModel(
    private val scanner: BleDeviceScanner,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    val state = MutableLiveData<State>()
    val finish = ActionLiveData()

    private val list = mutableListOf<BleDevice>()

    fun start() {
        state.postValue(State.InProgress)
        list.clear()

        scanner.scan()
            .onEach {
                if (!list.contains(it)) list.add(it)
                state.postValue(State.Success(list))
            }
            .launchIn(viewModelScope)
    }

    fun saveDevice(device: DeviceUiModel) {
        viewModelScope.launch {
            deviceRepository.save(BleDevice(device.name, device.address))
                .onSuccess { finish.sendAction() }
                .onFailure { state.postValue(State.Failure(it.message!!)) }
        }
    }
}