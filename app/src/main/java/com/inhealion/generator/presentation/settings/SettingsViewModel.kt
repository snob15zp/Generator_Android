package com.inhealion.generator.presentation.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.data.repository.UserRepository
import com.inhealion.generator.device.model.BleDevice
import com.inhealion.generator.model.onSuccess
import com.inhealion.generator.networking.api.model.UserProfile
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val device = MutableLiveData<BleDevice>()

    val userProfile = MutableLiveData<UserProfile>()

    fun loadDeviceInfo() {
        viewModelScope.launch {
            device.postValue(deviceRepository.get().valueOrNull())
        }
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            userRepository.get()
                .onSuccess { user ->
                    user?.profile?.let { userProfile.postValue(it) }
                }
        }
    }

}
