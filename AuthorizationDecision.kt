package com.pratik.axon.core.security

data class AuthorizationDecision(
    val allowed: Boolean,
    val reason: String = "",
    val requiresConfirmation: Boolean = false,
    val riskLevel: RiskLevel = RiskLevel.SAFE,
    val missingPermissions: List<String> = emptyList()
)