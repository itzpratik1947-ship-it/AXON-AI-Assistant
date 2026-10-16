package com.pratik.axon.core.security

class CoreAuthorizationPolicy(
    private val permissionChecker: PermissionChecker,
    private val confirmationPolicy: ConfirmationPolicy
) : AuthorizationPolicy, ExecutionAuthorizer {

    override fun authorize(
    riskLevel: RiskLevel,
    permissions: List<String>,
    confirmationGranted: Boolean
): AuthorizationDecision {
    
        val missingPermissions =
            permissions.filter {
                !permissionChecker.hasPermission(it)
            }

        if (missingPermissions.isNotEmpty()) {

            return AuthorizationDecision(
                allowed = false,
                reason = "Required permission is not available.",
                riskLevel = riskLevel,
                missingPermissions = missingPermissions
            )
        }

        return when (riskLevel) {

            RiskLevel.SAFE,
            RiskLevel.LOW -> {

                AuthorizationDecision(
                    allowed = true,
                    riskLevel = riskLevel
                )
            }

            RiskLevel.MEDIUM -> {

                AuthorizationDecision(
                    allowed = true,
                    reason = "Medium-risk operation authorized.",
                    riskLevel = riskLevel
                )
            }

            RiskLevel.HIGH,
RiskLevel.CRITICAL -> {

    if (confirmationGranted) {
        return AuthorizationDecision(
            allowed = true,
            reason = "High-risk operation confirmed.",
            riskLevel = riskLevel
        )
    }

    val requiresConfirmation =
        confirmationPolicy.requiresConfirmation(
            riskLevel
        )

    AuthorizationDecision(
        allowed = false,
        reason =
            "User confirmation or stronger authorization is required.",
        requiresConfirmation =
            requiresConfirmation,
        riskLevel = riskLevel
    )
}
        }
    }

    // Compatibility overload for existing callers/tests.
    
}