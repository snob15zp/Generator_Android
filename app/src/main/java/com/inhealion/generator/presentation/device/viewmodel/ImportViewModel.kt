package com.inhealion.generator.presentation.device.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.inhealion.generator.events.ImportStateEventDelegate
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.service.ImportService
import com.inhealion.generator.service.ImportState

class ImportViewModel(
    val importAction: ImportAction,
    private val importStateEventDelegate: ImportStateEventDelegate
) : ViewModel() {

    val importState = importStateEventDelegate.observe().asLiveData()

    fun cancel(context: Context) {
        ImportService.cancel(context)
    }

    fun stop(context: Context) {
        ImportService.stop(context)
        importStateEventDelegate.offer(ImportState.Idle)
    }
}
