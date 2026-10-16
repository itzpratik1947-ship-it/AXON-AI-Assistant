package com.pratik.axon.core.security

interface ConfirmationPolicy {

    fun requiresConfirmation(
        riskLevel: RiskLevel
    ): Boolean
}