package com.pratik.axon.core.security

class CoreConfirmationPolicy : ConfirmationPolicy {

    override fun requiresConfirmation(
        riskLevel: RiskLevel
    ): Boolean {

        return when (riskLevel) {

            RiskLevel.SAFE,
            RiskLevel.LOW,
            RiskLevel.MEDIUM -> false

            RiskLevel.HIGH,
            RiskLevel.CRITICAL -> true
        }
    }
}