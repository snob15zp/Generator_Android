package com.inhealion.generator.presentation.device.viewmodel

import androidx.lifecycle.viewModelScope
import com.inhealion.generator.R
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.device.bluetooth.BleDeviceScanner
import com.inhealion.generator.device.model.BleDevice
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.model.State
import com.inhealion.generator.model.onFailure
import com.inhealion.generator.model.onSuccess
import com.inhealion.generator.presentation.device.adapter.DeviceUiModel
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import com.inhealion.generator.utils.ApiErrorStringProvider
import com.inhealion.generator.utils.StringProvider
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.lang.IllegalStateException


class DiscoveryViewModel(
    private val scanner: BleDeviceScanner,
    private val deviceRepository: DeviceRepository,
    private val stringProvider: StringProvider
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
                .catch {
                    Timber.e(it, "Unable to scan devices")
                    val message = when (it) {
                        is IllegalStateException -> stringProvider.getString(R.string.error_bluetooth_disabled)
                        else -> stringProvider.getString(R.string.error_unknown)
                    }
                    postState(State.Failure(message, it))
                }
                .collect {
                    if (!list.contains(it)) list.add(it)
                    postState(State.Success(list))
                }
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
