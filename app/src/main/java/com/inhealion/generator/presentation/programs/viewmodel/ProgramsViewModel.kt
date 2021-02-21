package com.inhealion.generator.presentation.programs.viewmodel

import androidx.lifecycle.viewModelScope
import com.inhealion.generator.data.repository.UserRepository
import com.inhealion.generator.model.State
import com.inhealion.generator.model.onFailure
import com.inhealion.generator.model.onSuccess
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.networking.api.model.Folder
import com.inhealion.generator.presentation.main.BaseAuthViewModel
import com.inhealion.generator.utils.ApiErrorHandler
import kotlinx.coroutines.launch

class ProgramsViewModel(
    val folder: Folder,
    accountStore: AccountStore,
    userRepository: UserRepository,
    private val generatorApiCoroutinesClient: GeneratorApiCoroutinesClient,
    private val apiErrorHandler: ApiErrorHandler
) : BaseAuthViewModel(accountStore, userRepository) {

    fun load() {
        viewModelScope.launch {
            state.postValue(State.InProgress)

            generatorApiCoroutinesClient.fetchPrograms(folder.id)
                .onSuccess {
                    state.postValue(State.Success(it))
                }
                .onFailure {
                    state.postValue(State.Failure(apiErrorHandler.getErrorMessage(it), it))
                }
        }
    }

    fun import() {
        viewModelScope.launch {
            state.postValue(State.InProgress)
            generatorApiCoroutinesClient.downloadFolder(folder.id)
        }
    }
}
