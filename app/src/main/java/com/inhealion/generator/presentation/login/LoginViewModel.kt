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

    var isLoginSuccess: Boolean = false
        private set

    fun signIn(login: String, password: String) {
        viewModelScope.launch {
            postState(State.InProgress())
            authorizationManager.signIn(login, password)
                .catch {
                    isLoginSuccess = false
                    postState(State.Failure(apiErrorStringProvider.getErrorMessage(it)))
                }
                .collect {
                    isLoginSuccess = true
                    postState(State.Success(it))
                }
        }
    }
}
