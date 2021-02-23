package com.inhealion.generator.presentation.programs.viewmodel

import androidx.lifecycle.viewModelScope
import com.inhealion.generator.data.repository.UserRepository
import com.inhealion.generator.model.*
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.api.model.Folder
import com.inhealion.generator.networking.api.model.UserProfile
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import com.inhealion.generator.utils.ApiErrorHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FolderViewModel(
    private val userRepository: UserRepository,
    private val generatorApiCoroutinesClient: GeneratorApiCoroutinesClient,
    private val apiErrorHandler: ApiErrorHandler
) : BaseViewModel<Pair<UserProfile, List<Folder>>>() {

    private var folders: List<Folder>? = null

    fun load() {
        viewModelScope.launch {
            state.postValue(State.InProgress)

            val user = userRepository.get().valueOrNull() ?: run {
                state.value = State.Unauthorized
                return@launch
            }
            generatorApiCoroutinesClient.fetchUserProfile(user.id!!)
                .flatMapConcat { userProfile ->
                    generatorApiCoroutinesClient.fetchFolders(userProfile.id).map { userProfile to it }
                }
                .catch { state.postValue(State.apiError(it, apiErrorHandler)) }
                .collect {
                    folders = it.second
                    state.postValue(State.Success(it))
                }
        }
    }

    fun getFolder(id: String) = folders?.firstOrNull { it.id == id }
}

