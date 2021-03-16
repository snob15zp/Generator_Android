package com.inhealion.generator.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.inhealion.generator.presentation.device.ImportAction
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class ImportService : Service(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    private val importManager: ImportManager by inject()
    private lateinit var notificationManager: ImportNotificationManager

    private val binder = ImportServiceBinder()

    override fun onCreate() {
        super.onCreate()
        println("SSS > onCreate $this")
        notificationManager = ImportNotificationManager(this)
        importManager.listener = binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val importAction = intent.getParcelableExtra<ImportAction>(KEY_EXTRA_IMPORT_ACTION) ?: return START_NOT_STICKY
        println("SSS > onStartCommand $this")
        launch {
            delay(10000)
            importManager.import(importAction)
        }
        startForeground(
            notificationManager.id,
            notificationManager.create(importAction)
        )
        return START_STICKY
    }

    override fun onDestroy() {
        importManager.listener = null
        importManager.reset()
        println("SSS > onDestroy $this")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder {
        println("SSS > onBind $intent")
        return binder
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    inner class ImportServiceBinder : Binder(), ImportStateListener {
        var isActive: Boolean = false
            internal set

        private val _importState = ConflatedBroadcastChannel(importManager.currentState)
        val importState: Flow<ImportState> get() = _importState.openSubscription().consumeAsFlow()

        override fun onStateChanged(importState: ImportState) {
            isActive = importState.isActive
            _importState.offer(importState)

            if (!importState.isActive) stop()
        }
    }

    companion object {
        const val IMPORT_BROADCAST_ACTION = "IMPORT_BROADCAST_ACTION"
        const val KEY_IMPORT_STATE = "KEY_IMPORT_STATE"
        const val KEY_EXTRA_IMPORT_ACTION = "KEY_EXTRA_IMPORT_ACTION"
        private const val KEY_EXTRA_FORCE_START = "KEY_EXTRA_FORCE_START"
        private const val JOB_ID = 1000

        fun start() {

        }
    }

}
