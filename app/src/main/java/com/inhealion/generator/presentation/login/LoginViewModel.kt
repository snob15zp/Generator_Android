package com.inhealion.generator.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.model.State
import com.inhealion.generator.model.onFailure
import com.inhealion.generator.model.onSuccess
import com.inhealion.generator.service.AuthorizationManager
import com.inhealion.generator.utils.ApiErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authorizationManager: AuthorizationManager,
    private val apiErrorHandler: ApiErrorHandler
) : ViewModel() {

    private val _state = MutableStateFlow<State>(State.Idle)
    val state = _state.asLiveData(viewModelScope.coroutineContext)

    fun signIn(login: String, password: String) {
        viewModelScope.launch {
            _state.emit(State.InProgress)
            authorizationManager.signIn(login, password)
                .onSuccess { _state.emit(State.Success(it)) }
                .onFailure { _state.emit(State.Failure(apiErrorHandler.getErrorMessage(it))) }
        }

    }

}
