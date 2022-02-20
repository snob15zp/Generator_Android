package com.inhealion.generator.presentation.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.inhealion.generator.model.State
import kotlinx.coroutines.flow.MutableStateFlow

abstract class BaseViewModel<T : Any> : ViewModel() {
    private val stateChannel = MutableStateFlow<State<T>>(State.Idle)
    val state: LiveData<State<T>> get() = stateChannel.asLiveData()

    protected fun postState(state: State<T>) = stateChannel.tryEmit(state)
}
