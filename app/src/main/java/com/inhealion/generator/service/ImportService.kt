package com.inhealion.generator.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.inhealion.generator.R
import com.inhealion.generator.presentation.activity.MainActivity
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.device.ImportFragmentArgs
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
    private var importAction: ImportAction? = null

    private val binder = ImportServiceBinder()

    override fun onCreate() {
        super.onCreate()
        println("SSS > onCreate $this")
        notificationManager = ImportNotificationManager(this)
        importManager.listener = binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val importAction = intent.getParcelableExtra<ImportAction>(KEY_EXTRA_IMPORT_ACTION) ?: return START_NOT_STICKY
        this.importAction = importAction
        println("SSS > onStartCommand $this")
        launch {
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

    private fun stop(importState: ImportState) {
        stopForeground(false)
        stopSelf()

        val importAction = this.importAction ?: return
        val intent = Intent(this@ImportService, MainActivity::class.java).apply {
            action = "com.inhealion.generator.intent.SHOW_IMPORT"
            putExtras(ImportFragmentArgs(importAction, importState).toBundle())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK.or(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT))
        }
        //startActivity(intent)
    }

    inner class ImportServiceBinder : Binder(), ImportStateListener {
        var isActive: Boolean = false
            internal set

        private val _importState = ConflatedBroadcastChannel(importManager.currentState)
        val importState: Flow<ImportState> get() = _importState.openSubscription().consumeAsFlow()

        override fun onStateChanged(importState: ImportState) {
            isActive = importState.isActive
            _importState.offer(importState)
            val (progress, status) = when (importState) {
                ImportState.Idle -> null to getString(R.string.action_idle)
                ImportState.Connecting -> null to getString(R.string.action_connecting)
                ImportState.Downloading -> null to getString(R.string.action_download)
                ImportState.Rebooting -> null to getString(R.string.action_reboot)
                is ImportState.Importing -> importState.progress to getString(R.string.action_import)
                is ImportState.Finished -> null to getString(R.string.import_success)
                is ImportState.Error -> null to importState.message
                else -> null to null
            }
            notificationManager.bind(progress, status)
            if (!importState.isActive) {
                when (importState) {
                    is ImportState.Error -> getString(R.string.error_dialog_title) to importState.message
                    is ImportState.Finished -> getString(R.string.done) to getString(R.string.import_success)
                    else -> null
                }?.let {
                    notificationManager.notifyFinish(it.first, it.second)
                }
                stop(importState)
            }
        }
    }

    companion object {
        private const val KEY_EXTRA_IMPORT_ACTION = "KEY_EXTRA_IMPORT_ACTION"

        fun stop(context: Context) = context.stopService(intent(context))

        fun start(context: Context, importAction: ImportAction) = context.startService(intent(context, importAction))

        fun intent(context: Context, importAction: ImportAction? = null) =
            Intent(context, ImportService::class.java).apply {
                importAction?.let { putExtra(KEY_EXTRA_IMPORT_ACTION, it) }
            }
    }

}
