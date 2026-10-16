package com.pratik.axon.platform.android.security

import com.pratik.axon.core.capability.Capability
import com.pratik.axon.core.security.AuthorizationDecision
import com.pratik.axon.core.security.CapabilityAuthorization
import com.pratik.axon.core.security.ExecutionAuthorizer
import com.pratik.axon.core.security.RiskLevel

class AxonSecurityManager(
    private val executionAuthorizer: ExecutionAuthorizer
) : CapabilityAuthorization {

    fun getSecurityLevel(
        command: String
    ): AxonSecurityIntent {

        val text =
            command.lowercase().trim()

        return when {

            text.contains("money") ||
            text.contains("payment") ||
            text.contains("transfer") ||
            text.contains("bank") ||
            text.contains("financial") ||
            text.contains("delete everything") ||
            text.contains("factory reset") ||
            text.contains("change password") ||
            text.contains("change pin") ||
            text.contains("install unknown app") ||
            text.contains("change permission") ||
            text.contains("accessibility") ||
            text.contains("device admin") -> {

                AxonSecurityIntent.HIGH_RISK
            }

            text.contains("message") ||
            text.contains("messages") ||
            text.contains("private file") ||
            text.contains("private files") ||
            text.contains("photo") ||
            text.contains("photos") ||
            text.contains("contact") ||
            text.contains("contacts") ||
            text.contains("email") ||
            text.contains("location") ||
            text.contains("history") ||
            text.contains("notification") ||
            text.contains("personal information") ||
            text.contains("call") -> {

                AxonSecurityIntent.PRIVATE
            }

            else -> {

                AxonSecurityIntent.LOW_RISK
            }
        }
    }

    fun getRiskLevel(
        command: String
    ): RiskLevel {

        return when (getSecurityLevel(command)) {

            AxonSecurityIntent.LOW_RISK ->
                RiskLevel.LOW

            AxonSecurityIntent.PRIVATE ->
                RiskLevel.MEDIUM

            AxonSecurityIntent.HIGH_RISK ->
                RiskLevel.HIGH
        }
    }

    fun authorize(
    command: String,
    permissions: List<String> = emptyList(),
    confirmationGranted: Boolean = false
): AuthorizationDecision {

    return executionAuthorizer.authorize(
        riskLevel = getRiskLevel(command),
        permissions = permissions,
        confirmationGranted = confirmationGranted
    )
}

    fun authorizeCapability(
    command: String,
    capabilityRiskLevel: RiskLevel,
    permissions: List<String> = emptyList(),
    confirmationGranted: Boolean = false
): AuthorizationDecision {

    val commandRiskLevel =
        getRiskLevel(command)

    val effectiveRiskLevel =
        if (commandRiskLevel.ordinal >=
            capabilityRiskLevel.ordinal
        ) {
            commandRiskLevel
        } else {
            capabilityRiskLevel
        }

    return executionAuthorizer.authorize(
        riskLevel = effectiveRiskLevel,
        permissions = permissions,
        confirmationGranted = confirmationGranted
    )
}

    fun requiresConfirmation(
        command: String
    ): Boolean {

        return authorize(
            command
        ).requiresConfirmation
    }

    override fun authorize(
    command: String,
    capability: Capability,
    confirmationGranted: Boolean
): AuthorizationDecision {

    return authorizeCapability(
        command = command,
        capabilityRiskLevel = capability.riskLevel,
        permissions = capability.requiredPermissions,
        confirmationGranted = confirmationGranted
    )
}
}