package com.pratik.axon.platform.android.device

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class AxonTorchService : Service() {

    companion object {

        const val ACTION_TORCH_ON =
            "com.pratik.axon.ACTION_TORCH_ON"

        const val ACTION_TORCH_OFF =
            "com.pratik.axon.ACTION_TORCH_OFF"

        private const val CHANNEL_ID =
            "axon_torch_channel"

        private const val NOTIFICATION_ID = 3001
    }

    private var cameraManager: CameraManager? = null
    private var torchCameraId: String? = null

    override fun onCreate() {
        super.onCreate()

        cameraManager =
            getSystemService(
                CAMERA_SERVICE
            ) as CameraManager

        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {

            ACTION_TORCH_ON -> {

                startTorchForeground()

                val success =
                    turnTorchOn()

                if (!success) {
                    stopTorch()
                    stopSelf()
                }
            }

            ACTION_TORCH_OFF -> {

                turnTorchOff()
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun startTorchForeground() {

        val notification =
            createNotification()

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
        ) {

            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo
                    .FOREGROUND_SERVICE_TYPE_CAMERA
            )

        } else {

            startForeground(
                NOTIFICATION_ID,
                notification
            )
        }
    }

    private fun turnTorchOn(): Boolean {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.M
        ) {

            if (
                checkSelfPermission(
                    android.Manifest.permission.CAMERA
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return try {

            val manager =
                cameraManager
                    ?: return false

            val cameraId =
                findTorchCameraId()
                    ?: return false

            manager.setTorchMode(
                cameraId,
                true
            )

            torchCameraId = cameraId

            true

        } catch (
            _: SecurityException
        ) {

            false

        } catch (
            _: Exception
        ) {

            false
        }
    }

    private fun turnTorchOff() {

        try {

            val manager =
                cameraManager
                    ?: return

            val cameraId =
                torchCameraId
                    ?: findTorchCameraId()
                    ?: return

            manager.setTorchMode(
                cameraId,
                false
            )

        } catch (
            _: Exception
        ) {
        }

        torchCameraId = null
    }

    private fun findTorchCameraId(): String? {

        val manager =
            cameraManager
                ?: return null

        return try {

            for (
                id in manager.cameraIdList
            ) {

                val characteristics =
                    manager.getCameraCharacteristics(
                        id
                    )

                val hasFlash =
                    characteristics.get(
                        CameraCharacteristics
                            .FLASH_INFO_AVAILABLE
                    ) == true

                val lensFacing =
                    characteristics.get(
                        CameraCharacteristics
                            .LENS_FACING
                    )

                if (
                    hasFlash &&
                    lensFacing ==
                    CameraCharacteristics
                        .LENS_FACING_BACK
                ) {

                    return id
                }
            }

            null

        } catch (
            _: Exception
        ) {

            null
        }
    }

    private fun stopTorch() {

        turnTorchOff()

        stopForeground(
            STOP_FOREGROUND_REMOVE
        )
    }

    private fun createNotificationChannel() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "AXON Flashlight",
                    NotificationManager
                        .IMPORTANCE_LOW
                )

            channel.description =
                "Controls the AXON flashlight."

            val notificationManager =
                getSystemService(
                    NotificationManager::class.java
                )

            notificationManager.createNotificationChannel(
                channel
            )
        }
    }

    private fun createNotification(): Notification {

        return NotificationCompat
            .Builder(
                this,
                CHANNEL_ID
            )
            .setContentTitle(
                "AXON Flashlight"
            )
            .setContentText(
                "Flashlight is currently on."
            )
            .setSmallIcon(
                android.R.drawable.ic_menu_view
            )
            .setOngoing(true)
            .setCategory(
                NotificationCompat.CATEGORY_SERVICE
            )
            .build()
    }
    
    override fun onTaskRemoved(
    rootIntent: Intent?
) {
    android.util.Log.d(
        "AXON_TORCH",
        "AXON task removed. Torch service is still running."
    )

    try {
        if (torchCameraId != null) {
            cameraManager?.setTorchMode(
                torchCameraId!!,
                true
            )

            android.util.Log.d(
                "AXON_TORCH",
                "Torch re-applied after task removal."
            )
        }
    } catch (e: Exception) {
        android.util.Log.e(
            "AXON_TORCH",
            "Failed to re-apply torch: ${e.message}"
        )
    }

    super.onTaskRemoved(rootIntent)
}



    override fun onDestroy() {

    android.util.Log.d(
        "AXON_TORCH",
        "AxonTorchService.onDestroy() called."
    )

    turnTorchOff()

    cameraManager = null

    super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }
}