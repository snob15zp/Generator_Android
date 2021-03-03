package com.inhealion.generator.presentation.main.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.inhealion.generator.lifecyle.SingleLiveData
import com.inhealion.generator.model.MessageDialogData
import com.inhealion.generator.model.State
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow

abstract class BaseViewModel<T : Any> : ViewModel() {
    private val _state = MutableSharedFlow<State<T>>()
    val state:LiveData<State<T>> get() = _state.asLiveData()


    protected suspend fun postState(state: State<T>) = _state.emit(state)
}
