
package com.pratik.axon.platform.android.personal

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.pratik.axon.R
import com.pratik.axon.app.MainActivity

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val reminderId = intent.getIntExtra(
            ReminderScheduler.EXTRA_REMINDER_ID,
            System.currentTimeMillis().toInt()
        )

        val reminderText = intent.getStringExtra(
            ReminderScheduler.EXTRA_REMINDER_TEXT
        ) ?: "Your scheduled reminder"

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(
                    ReminderScheduler.CHANNEL_ID,
                    "AXON reminders",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(
                context,
                ReminderScheduler.CHANNEL_ID
            )
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
                .setPriority(Notification.PRIORITY_HIGH)
        }

        val notification = builder
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("AXON reminder")
            .setContentText(reminderText)
            .setStyle(
                Notification.BigTextStyle()
                    .bigText(reminderText)
            )
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(reminderId, notification)
    }
}
