package com.pratik.axon.platform.android.security

import com.pratik.axon.core.security.AuthorizationDecision
import com.pratik.axon.core.security.AuthorizationPolicy
import com.pratik.axon.core.security.CoreAuthorizationPolicy
import com.pratik.axon.core.security.PermissionChecker
import com.pratik.axon.core.security.RiskLevel
import com.pratik.axon.core.security.CoreConfirmationPolicy
import com.pratik.axon.core.security.ExecutionAuthorizer

class AndroidAuthorizationPolicy(
    permissionChecker: PermissionChecker
) : AuthorizationPolicy, ExecutionAuthorizer {

    private val corePolicy =
        CoreAuthorizationPolicy(
            permissionChecker = permissionChecker,
            confirmationPolicy = CoreConfirmationPolicy()
        )

    override fun authorize(
    riskLevel: RiskLevel,
    permissions: List<String>,
    confirmationGranted: Boolean
): AuthorizationDecision {

    return corePolicy.authorize(
        riskLevel = riskLevel,
        permissions = permissions,
        confirmationGranted = confirmationGranted
    )
}
}