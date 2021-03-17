package com.inhealion.generator.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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

    fun bind(progress: Int?, status: String?) {
        progress?.let { builder.setProgress(100, progress, false) } ?: builder.setProgress(100, 0, true)
        builder.setContentText(status)
        manager.notify(FOREGROUND_ID, builder.build())
    }

    fun notifyFinish(title: String, message: String) {
        val bigText = NotificationCompat.BigTextStyle()
        bigText.bigText(message)
        bigText.setBigContentTitle(title)

        builder
            .setContentText("Finished")
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
                NotificationManager.IMPORTANCE_HIGH
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
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.inhealion.generator.intent.SHOW_IMPORT"
            putExtras(ImportFragmentArgs(importAction).toBundle())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        setContentIntent(pendingIntent)
    }
}
