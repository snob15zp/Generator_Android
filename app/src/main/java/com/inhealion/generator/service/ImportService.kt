package com.inhealion.generator.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.inhealion.generator.events.ImportStateEventDelegate
import com.inhealion.generator.presentation.device.ImportAction
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class ImportService : Service(), ImportStateListener, CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    private var job: Job? = null

    private val importManager: ImportManager by inject()
    private lateinit var notificationManager: ImportNotificationManager

    private val importStateEventDelegate: ImportStateEventDelegate by inject()

    override fun onCreate() {
        super.onCreate()
        notificationManager = ImportNotificationManager(this)
        importManager.listener = this
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.getSerializableExtra(KEY_EXTRA_ACTION) as? Action ?: Action.START) {
            Action.START -> {
                val importAction =
                    intent.getParcelableExtra<ImportAction>(KEY_EXTRA_IMPORT_ACTION) ?: return START_NOT_STICKY
                startImport(importAction)
            }
            Action.CANCEL -> cancelImport()
        }
        return START_STICKY
    }


    override fun onDestroy() {
        job?.cancel()

        importManager.listener = null
        importManager.reset()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStateChanged(importState: ImportState) {
        importStateEventDelegate.offer(importState)
        notificationManager.bind(importState)
    }

    private fun cancelImport() {
        importManager.cancel()
    }

    private fun startImport(importAction: ImportAction) {
        job?.cancel()
        job = launch(coroutineContext) {
            importManager.reset()
            importManager.import(importAction)
            stop()
        }

        startForeground(
            notificationManager.id,
            notificationManager.create(importAction)
        )
    }

    private fun stop() {
        stopForeground(false)
        stopSelf()
    }

    enum class Action {
        START, CANCEL
    }

    companion object {
        private const val KEY_EXTRA_IMPORT_ACTION = "KEY_EXTRA_IMPORT_ACTION"
        private const val KEY_EXTRA_ACTION = "KEY_EXTRA_ACTION"

        fun stop(context: Context) = context.stopService(intent(context))

        fun start(context: Context, importAction: ImportAction) =
            context.startService(intent(context, importAction, Action.START))

        fun cancel(context: Context) = context.startService(intent(context, action = Action.CANCEL))

        private fun intent(context: Context, importAction: ImportAction? = null, action: Action? = null) =
            Intent(context, ImportService::class.java).apply {
                importAction?.let { putExtra(KEY_EXTRA_IMPORT_ACTION, it) }
                action?.let { putExtra(KEY_EXTRA_ACTION, it) }
            }
    }

}
