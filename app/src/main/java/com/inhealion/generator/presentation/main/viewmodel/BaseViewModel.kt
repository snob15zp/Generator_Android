package com.inhealion.generator.presentation.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.inhealion.generator.model.State
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow

abstract class BaseViewModel<T : Any> : ViewModel() {
    private val stateChannel = ConflatedBroadcastChannel<State<T>>()

    private val _state = stateChannel.asFlow()
    val state: LiveData<State<T>> get() = _state.asLiveData()

    protected fun postState(state: State<T>) = stateChannel.offer(state)
}
