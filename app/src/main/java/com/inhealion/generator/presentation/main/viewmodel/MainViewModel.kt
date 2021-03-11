package com.inhealion.generator.presentation.main.viewmodel

import androidx.lifecycle.*
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.service.SharedPrefManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.internal.ChannelFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val accountStore: AccountStore,
    private val deviceRepository: DeviceRepository,
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    val action = MutableLiveData<Action>()

    fun navigate() {
        viewModelScope.launch {
            action.value = when {
                accountStore.load() == null -> Action.ShowLogin
                (deviceRepository.get().valueOrNull() == null && !sharedPrefManager.isDiscoveryWasShown) -> {
                    sharedPrefManager.isDiscoveryWasShown = true
                    Action.ShowDeviceConnection
                }
                else -> Action.ShowFolders
            }
        }
    }


    sealed class Action {
        object ShowLogin : Action()
        object ShowDeviceConnection : Action()
        object ShowFolders : Action()
    }

}
