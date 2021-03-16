package com.inhealion.generator.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.inhealion.generator.presentation.device.ImportAction
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class ImportService : Service(), ImportStateListener {

    private val localBroadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }

    private val importManager: ImportManager by inject { parametersOf(this) }
    private lateinit var notificationManager: ImportNotificationManager

    override fun onCreate() {
        super.onCreate()
        println("SSS > onCreate $this")
        notificationManager = ImportNotificationManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("SSS > onStartCommand $this")
        startForeground(
            notificationManager.id,
            notificationManager.create()
        )
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        importManager.cancel()
        println("SSS > onDestroy $this")
    }

    override fun onBind(intent: Intent?): IBinder {
        println("SSS > onBind $intent")
        return ImportServiceBinder()
    }

    override fun onStateChanged(importState: ImportState) {
        localBroadcastManager.sendBroadcast(Intent(IMPORT_BROADCAST_ACTION)
            .apply { putExtra(KEY_IMPORT_STATE, importState) })
    }

    inner class ImportServiceBinder : Binder()

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
