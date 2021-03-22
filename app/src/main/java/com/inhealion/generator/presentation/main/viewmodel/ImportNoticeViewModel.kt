package com.inhealion.generator.presentation.main.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.events.ImportStateEventDelegate
import com.inhealion.generator.model.UiImportState
import com.inhealion.generator.service.ImportState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class ImportNoticeViewModel(
    private val importStateEventDelegate: ImportStateEventDelegate
) : ViewModel() {

    val importState = MutableLiveData<UiImportState>()

    private var inProgress = false

    init {
        viewModelScope.launch {
            importStateEventDelegate.observe().collect {
                if (!inProgress.xor(it.isActive)) return@collect
                importState.value = if (it.isActive) {
                    UiImportState.InProgress
                } else {
                    when (it) {
                        ImportState.Finished -> UiImportState.Success
                        is ImportState.Error -> UiImportState.Failed(it.message)
                        else -> null
                    }
                }
                inProgress = it.isActive
            }
        }
    }
}
