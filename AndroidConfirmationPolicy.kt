package com.pratik.axon.platform.android.security

import com.pratik.axon.core.security.ConfirmationPolicy
import com.pratik.axon.core.security.CoreConfirmationPolicy
import com.pratik.axon.core.security.RiskLevel

class AndroidConfirmationPolicy : ConfirmationPolicy {

    private val corePolicy =
        CoreConfirmationPolicy()

    override fun requiresConfirmation(
        riskLevel: RiskLevel
    ): Boolean {

        return corePolicy.requiresConfirmation(
            riskLevel
        )
    }
}