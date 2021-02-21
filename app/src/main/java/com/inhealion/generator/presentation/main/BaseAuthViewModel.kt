package com.inhealion.generator.presentation.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inhealion.generator.data.repository.UserRepository
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.model.State
import com.inhealion.generator.networking.account.AccountStore
import kotlinx.coroutines.runBlocking

abstract class BaseAuthViewModel(
    private val accountStore: AccountStore,
    private val userRepository: UserRepository
) : ViewModel() {

    val state = MutableLiveData<State>()
    val showLoginForm = ActionLiveData()

    protected fun verifyAuthorization(): Boolean {
        val user = runBlocking { userRepository.get() }.valueOrNull()
        return if (accountStore.load() == null || user?.id == null) {
            showLoginForm.sendAction()
            state.postValue(State.Idle)
            false
        } else {
            true
        }
    }
}
