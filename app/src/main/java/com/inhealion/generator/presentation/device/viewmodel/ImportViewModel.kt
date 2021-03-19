package com.inhealion.generator.presentation.device.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.events.ImportStateEventDelegate
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import com.inhealion.generator.service.ImportService
import com.inhealion.generator.service.ImportState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ImportViewModel(
    val importAction: ImportAction,
    private val resultImportState: ImportState?,
    private val importStateEventDelegate: ImportStateEventDelegate
) : BaseViewModel<Any>() {
    val importState = MutableLiveData<ImportState>()

    init {
        viewModelScope.launch {
            importStateEventDelegate.observe().collect {
                importState.value = it
            }
        }
    }

    fun cancel(context: Context) {
        ImportService.stop(context)
        importStateEventDelegate.offer(ImportState.Canceled)
    }
}
