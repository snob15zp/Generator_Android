package com.inhealion.generator.presentation.main.viewmodel

import androidx.lifecycle.*
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.events.ImportStateEventDelegate
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.service.ImportState
import com.inhealion.generator.service.SharedPrefManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class MainViewModel(
    private val accountStore: AccountStore,
    private val deviceRepository: DeviceRepository,
    private val sharedPrefManager: SharedPrefManager,
    private val importStateEventDelegate: ImportStateEventDelegate
) : ViewModel() {

    val action = MutableLiveData<Action>()
    val importState = MutableLiveData<UiImportState>()

    private var inProgress = false

    init {
        viewModelScope.launch {
            importStateEventDelegate.observe().collect {
                importState.value = when (it) {
                    is ImportState.Importing,
                    ImportState.Rebooting,
                    ImportState.Connecting,
                    ImportState.Downloading ->
                        if (!inProgress) UiImportState.InProgress else return@collect
                    ImportState.Finished -> UiImportState.Success
                    is ImportState.Error -> UiImportState.Failed(it.message)
                    else -> null
                }
                inProgress = it.isActive
            }
        }
    }

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

    sealed class UiImportState {
        object InProgress : UiImportState()
        object Success : UiImportState()
        data class Failed(val message: String) : UiImportState()
    }

}
