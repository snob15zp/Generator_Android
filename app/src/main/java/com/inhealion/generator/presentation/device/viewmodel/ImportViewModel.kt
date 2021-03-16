package com.inhealion.generator.presentation.device.viewmodel

import android.content.Context
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.R
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.device.ErrorCodes
import com.inhealion.generator.device.Generator
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.model.State
import com.inhealion.generator.networking.ApiError
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import com.inhealion.generator.service.ImportService
import com.inhealion.generator.utils.ApiErrorStringProvider
import com.inhealion.generator.utils.StringProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.io.File
import java.util.*

class ImportViewModel(
    val importAction: ImportAction,
    private val deviceRepository: DeviceRepository,
) : BaseViewModel<Any>() {

    val showDiscovery = ActionLiveData()
    val currentAction = MutableLiveData<String>()
    val currentProgress = MutableLiveData<Int>()

    var isCanceled: Boolean = false

    fun import() {}

    fun cancel() {
    }
}
