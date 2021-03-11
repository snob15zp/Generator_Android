package com.inhealion.generator.service

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class ImportService : JobIntentService(), ImportStateListener {

    private val importManager: ImportManager by inject { parametersOf(this) }

    override fun onHandleWork(intent: Intent) {
        importManager.import(intent.getParcelableExtra("")!!)
    }

    override fun onStateChanged(importState: ImportState) {
        sendBroadcast(Intent().apply { putExtra(KEY_IMPORT_STATE, importState) })
    }

    companion object {
        const val KEY_IMPORT_STATE = "KEY_IMPORT_STATE"
        private const val JOB_ID = 1000

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, ImportService::class.java, JOB_ID, intent)
        }
    }
}
