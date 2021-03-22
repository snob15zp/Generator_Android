package com.inhealion.generator.presentation.main.viewmodel

import androidx.lifecycle.*
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.events.ImportStateEventDelegate
import com.inhealion.generator.model.UiImportState
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.service.ImportState
import com.inhealion.generator.service.SharedPrefManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(
    private val accountStore: AccountStore,
    private val deviceRepository: DeviceRepository,
    private val sharedPrefManager: SharedPrefManager,
    importStateEventDelegate: ImportStateEventDelegate
) : ImportNoticeViewModel(importStateEventDelegate) {

    val action = MutableLiveData<Action>()

    fun navigate() {
        viewModelScope.launch {
            val account = accountStore.load()
            action.value = when {
                account == null -> Action.ShowLogin
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
