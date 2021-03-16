package com.inhealion.generator.presentation.device.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import com.inhealion.generator.service.ImportService
import com.inhealion.generator.service.ImportState
import kotlinx.coroutines.flow.*

class ImportViewModel(
    context: Context,
    val importAction: ImportAction
) : BaseViewModel<Any>() {

    private var importService: ImportService.ImportServiceBinder? = null

    private val _importState = MediatorLiveData<ImportState>()
    val importState: LiveData<ImportState> = _importState

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            println("SSS > onServiceConnected")

            importService = service as ImportService.ImportServiceBinder
            _importState.addSource(service.importState.asLiveData()) { _importState.value = it }
            startImportIfNeeded(context, service)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            println("SSS > onServiceDisconnected")
            dispose(context)
        }
    }

    fun bind(context: Context){
        context.bindService(Intent(context, ImportService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun dispose(context: Context) {
        importService?.let { _importState.removeSource(it.importState.asLiveData()) }
        importService = null
        context.unbindService(serviceConnection)
    }

    private fun startImportIfNeeded(context: Context, service: ImportService.ImportServiceBinder) {
        if (!service.isActive) {
            context.startService(Intent(context, ImportService::class.java)
                .apply { putExtra(ImportService.KEY_EXTRA_IMPORT_ACTION, importAction) })
        }
    }
}
