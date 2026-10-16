
package com.pratik.axon.platform.android.personal

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class ReminderScheduler(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        const val CHANNEL_ID = "axon_reminders"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_REMINDER_TEXT = "reminder_text"
    }

    fun scheduleReminder(
        reminderText: String,
        triggerAtMillis: Long
    ): Boolean {
        if (triggerAtMillis <= System.currentTimeMillis()) {
            return false
        }

        createNotificationChannel()

        val reminderId = (
            System.currentTimeMillis() % Int.MAX_VALUE
        ).toInt()

        val intent = Intent(
            appContext,
            ReminderReceiver::class.java
        ).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_TEXT, reminderText)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            appContext.getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

        return try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AXON reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for AXON scheduled reminders"
            }

            val manager = appContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }
}
