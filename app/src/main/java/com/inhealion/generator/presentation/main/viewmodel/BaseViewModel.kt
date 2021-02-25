package com.inhealion.generator.presentation.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inhealion.generator.lifecyle.SingleLiveData
import com.inhealion.generator.model.ErrorDialogData
import com.inhealion.generator.model.State

abstract class BaseViewModel<T : Any> : ViewModel() {
    val error = SingleLiveData<ErrorDialogData>()
    val state = MutableLiveData<State<T>>()
}
