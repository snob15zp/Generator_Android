package com.inhealion.generator.presentation.login

import androidx.lifecycle.viewModelScope
import com.inhealion.generator.model.State
import com.inhealion.generator.networking.api.model.User
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import com.inhealion.generator.service.AuthorizationManager
import com.inhealion.generator.utils.ApiErrorStringProvider
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authorizationManager: AuthorizationManager,
    private val apiErrorStringProvider: ApiErrorStringProvider
) : BaseViewModel<User>() {

    fun signIn(login: String, password: String) {
        viewModelScope.launch {
            state.postValue(State.InProgress)
            authorizationManager.signIn(login, password)
                .catch { state.postValue(State.Failure(apiErrorStringProvider.getErrorMessage(it))) }
                .collect { state.postValue(State.Success(it)) }
        }
    }
}
