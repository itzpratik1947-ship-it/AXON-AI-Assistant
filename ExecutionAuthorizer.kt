package com.pratik.axon.core.security

interface ExecutionAuthorizer {

    fun authorize(
        riskLevel: RiskLevel,
        permissions: List<String>,
        confirmationGranted: Boolean
    ): AuthorizationDecision
}