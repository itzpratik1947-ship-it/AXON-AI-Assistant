package com.pratik.axon.platform.android.wake

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
private lateinit var wakeDetector: AxonWakeDetector
private lateinit var wakeRecognizer: AxonWakeRecognizer

class AxonWakeService : Service() {

    companion object {
        private const val CHANNEL_ID = "AXON_WAKE_CHANNEL"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {

    super.onCreate()

    createNotificationChannel()

    wakeDetector =
        AxonWakeDetector()

    wakeRecognizer =
        AxonWakeRecognizer(
            this,
            wakeDetector
        ) {

            // Wake word detected.
            // Actual screen wake + command listening
            // will be added in the next stage.

        }
}

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val notification = createNotification()

        startForeground(
    NOTIFICATION_ID,
    notification
)

wakeRecognizer.start()

return START_STICKY
    }

    private fun createNotificationChannel() {

        val channel = NotificationChannel(
            CHANNEL_ID,
            "AXON Wake Service",
            NotificationManager.IMPORTANCE_LOW
        )

        channel.description =
            "Keeps AXON ready for wake detection."

        val notificationManager =
            getSystemService(
                NotificationManager::class.java
            )

        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {

        return Notification.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle("AXON is ready")
            .setContentText("AXON wake service is active.")
            .setSmallIcon(
                android.R.drawable.ic_btn_speak_now
            )
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {

    if (
        ::wakeRecognizer.isInitialized
    ) {

        wakeRecognizer.stop()
    }

    super.onDestroy()
}

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }
}