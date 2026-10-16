package com.pratik.axon.core.security

interface PermissionChecker {

    fun hasPermission(
        permission: String
    ): Boolean

    fun hasPermissions(
        permissions: List<String>
    ): Boolean {
        return permissions.all {
            hasPermission(it)
        }
    }
}