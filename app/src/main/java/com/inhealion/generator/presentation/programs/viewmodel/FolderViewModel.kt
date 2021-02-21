package com.inhealion.generator.presentation.programs.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.data.repository.UserRepository
import com.inhealion.generator.model.*
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.api.model.Folder
import com.inhealion.generator.networking.api.model.UserProfile
import com.inhealion.generator.networking.ApiError
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.presentation.main.BaseAuthViewModel
import com.inhealion.generator.utils.ApiErrorHandler
import kotlinx.coroutines.launch

class FolderViewModel(
    accountStore: AccountStore,
    private val userRepository: UserRepository,
    private val generatorApiCoroutinesClient: GeneratorApiCoroutinesClient,
    private val apiErrorHandler: ApiErrorHandler
) : BaseAuthViewModel(accountStore, userRepository) {

    val userProfile = MutableLiveData<UserProfile>()
    val folders = MutableLiveData<List<Folder>>()


    fun load() {
        if (!verifyAuthorization()) {
            return
        }

        viewModelScope.launch {
            state.postValue(State.InProgress)

            val user = userRepository.get().valueOrNull() ?: return@launch
            generatorApiCoroutinesClient.fetchUserProfile(user.id!!)
                .mapSuccess {
                    userProfile.postValue(it)
                    generatorApiCoroutinesClient.fetchFolders(it.id)
                }
                .onSuccess {
                    folders.postValue(it)
                    state.postValue(State.success())
                }
                .onFailure {
                    if (it is ApiError.Unauthorized) {
                        verifyAuthorization()
                    } else {
                        state.postValue(State.Failure(apiErrorHandler.getErrorMessage(it)))
                    }
                }
        }
    }
}

