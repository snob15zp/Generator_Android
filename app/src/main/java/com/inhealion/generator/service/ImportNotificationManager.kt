package com.inhealion.generator.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationManagerCompat
import com.inhealion.generator.R
import com.inhealion.generator.presentation.activity.ImportActivity
import com.inhealion.generator.presentation.activity.MainActivity
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.device.ImportFragmentArgs


private const val CHANNEL_ID = "ImportNotificationManager"
private const val FOREGROUND_ID = 0x1

class ImportNotificationManager(
    private val context: Context
) {
    private val manager = NotificationManagerCompat.from(context)
    private val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_import_notification)
        .setColor(context.getColor(R.color.colorAccent))
        .setProgress(100, 0, true)

    val id: Int = FOREGROUND_ID

    init {
        initChannel()
    }

    fun create(importAction: ImportAction): Notification =
        builder
            .setContentTitle(
                when (importAction) {
                    is ImportAction.UpdateFirmware -> context.getString(R.string.flash_firmware)
                    is ImportAction.ImportFolder -> context.getString(R.string.import_folder)
                }
            )
            .applyContentIntent(importAction)
            .build()

    fun bind(importState: ImportState) {
        when (importState) {
            is ImportState.Error -> context.getString(R.string.error_dialog_title) to importState.message
            is ImportState.Finished -> context.getString(R.string.done) to context.getString(R.string.import_success)
            else -> null
        }?.let {
            notifyFinish(it.first, it.second)
            return
        }

        val status = when (importState) {
            ImportState.Idle -> context.getString(R.string.action_idle)
            ImportState.Connecting -> context.getString(R.string.action_connecting)
            ImportState.Downloading -> context.getString(R.string.action_download)
            ImportState.Rebooting -> context.getString(R.string.action_reboot)
            is ImportState.Importing -> context.getString(R.string.action_import)
            else -> null
        }
        (importState as? ImportState.Importing)?.progress
            ?.let { builder.setProgress(100, it, false) }
            ?: builder.setProgress(100, 0, true)
        builder.setContentText(status)
        manager.notify(FOREGROUND_ID, builder.build())
    }

    private fun notifyFinish(title: String, message: String) {
        val bigText = NotificationCompat.BigTextStyle()
        bigText.bigText(message)
        bigText.setBigContentTitle(title)

        builder
            .setPriority(PRIORITY_HIGH)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_import_notification))
            .setStyle(bigText)
            .setAutoCancel(true)
            .setProgress(0, 0, false)
        manager.notify(FOREGROUND_ID, builder.build())
    }

    private fun initChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.action_import),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setSound(null, null)
                setShowBadge(true)
                enableVibration(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Apply intent to open [ImportActivity]
     */
    private fun NotificationCompat.Builder.applyContentIntent(importAction: ImportAction) = apply {
        val intent = Intent(context, ImportActivity::class.java).apply {
            putExtras(ImportFragmentArgs(importAction).toBundle())
            addFlags(FLAG_ACTIVITY_NEW_TASK.or(FLAG_ACTIVITY_REORDER_TO_FRONT))
        }
        val pendingIntent = PendingIntent.getActivities(
            context,
            0,
            arrayOf(Intent(context, MainActivity::class.java), intent),
            PendingIntent.FLAG_ONE_SHOT
        )
        setContentIntent(pendingIntent)
    }
}
