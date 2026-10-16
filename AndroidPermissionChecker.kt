package com.pratik.axon.platform.android.security

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import com.pratik.axon.core.security.PermissionChecker

class AndroidPermissionChecker(
    private val context: Context
) : PermissionChecker {

    override fun hasPermission(
        permission: String
    ): Boolean {

        if (permission == android.Manifest.permission.WRITE_SETTINGS) {
            return Settings.System.canWrite(context)
        }

        return context.checkSelfPermission(
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}