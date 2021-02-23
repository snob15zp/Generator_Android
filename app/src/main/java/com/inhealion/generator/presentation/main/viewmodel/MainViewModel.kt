package com.inhealion.generator.presentation.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.networking.account.AccountStore
import kotlinx.coroutines.launch

class MainViewModel(
    private val accountStore: AccountStore,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    val showLogin = ActionLiveData()
    val showDeviceConnection = ActionLiveData()
    val showFolders = ActionLiveData()

    fun navigate() {
        viewModelScope.launch {
            when {
                accountStore.load() == null -> showLogin.sendAction()
                deviceRepository.get().valueOrNull() == null -> showDeviceConnection.sendAction()
                else -> showFolders.sendAction()
            }
        }
    }
}
