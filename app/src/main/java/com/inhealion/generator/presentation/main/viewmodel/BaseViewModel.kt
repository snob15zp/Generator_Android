package com.inhealion.generator.presentation.main.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inhealion.generator.lifecyle.SingleLiveData
import com.inhealion.generator.model.ErrorDialogData
import com.inhealion.generator.model.State
import com.inhealion.generator.utils.StringProvider
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
abstract class BaseViewModel<T : Any> : ViewModel(), KoinComponent {
    val error = SingleLiveData<ErrorDialogData>()
    val state = MutableLiveData<State<T>>()

    private val stringProvider: StringProvider by inject()

    protected fun getString(@StringRes id: Int) = stringProvider.getString(id)

    protected fun getString(@StringRes id: Int, vararg args: Any) = stringProvider.getString(id, *args)
}
