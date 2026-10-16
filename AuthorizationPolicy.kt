package com.pratik.axon.core.security

interface AuthorizationPolicy {

    fun authorize(
        riskLevel: RiskLevel,
        permissions: List<String> = emptyList(),
        confirmationGranted: Boolean = false
    ): AuthorizationDecision
}