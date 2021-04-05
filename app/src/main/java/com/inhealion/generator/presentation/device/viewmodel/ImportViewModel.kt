package com.inhealion.generator.presentation.device.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.events.ImportStateEventDelegate
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.service.ImportService
import com.inhealion.generator.service.ImportState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ImportViewModel(
    val importAction: ImportAction,
    private val importStateEventDelegate: ImportStateEventDelegate
) : ViewModel() {

    val importState = importStateEventDelegate.observe().asLiveData()

    fun cancel(context: Context) {
        ImportService.stop(context)
        importStateEventDelegate.offer(ImportState.Canceled)
    }
}
