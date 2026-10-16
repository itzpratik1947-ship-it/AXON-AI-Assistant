package com.pratik.axon.core.security

import com.pratik.axon.core.capability.Capability

class ExecutionAuthorizationGateway(
    private val capabilityAuthorization: CapabilityAuthorization
) {

    fun authorize(
        command: String,
        capability: Capability
    ): ExecutionAuthorizationResult {

        val authorization =
            capabilityAuthorization.authorize(
                command = command,
                capability = capability
            )

        val decision =
            when {
                !authorization.allowed &&
                        authorization.requiresConfirmation ->
                    ExecutionDecision.CONFIRMATION_REQUIRED

                !authorization.allowed ->
                    ExecutionDecision.BLOCKED

                else ->
                    ExecutionDecision.ALLOWED
            }

        return ExecutionAuthorizationResult(
            decision = decision,
            authorization = authorization
        )
    }
}