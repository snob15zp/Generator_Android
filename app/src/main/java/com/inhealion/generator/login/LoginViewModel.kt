package com.inhealion.generator.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.manager.AuthorizationManager
import com.inhealion.generator.model.onFailure
import com.inhealion.generator.model.onSuccess
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.api.model.User
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authorizationManager: AuthorizationManager
) : ViewModel() {

    private val _state = MutableStateFlow<State>(State.Idle)
    val state = _state.asLiveData(viewModelScope.coroutineContext)

    fun signIn(login: String, password: String) {
        viewModelScope.launch {
            authorizationManager.signIn(login, password)
                .onSuccess { _state.emit(State.Success(it)) }
                .onFailure { _state.emit(State.Failure(it.message ?: "Something went wrong")) }
        }

    }

    sealed class State {
        data class Success(val user: User) : State()
        data class Failure(val error: String) : State()

        object Idle : State()
    }

}
