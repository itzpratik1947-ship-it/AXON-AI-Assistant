package com.pratik.axon.core.security

import com.pratik.axon.core.capability.Capability

interface CapabilityAuthorization {

    fun authorize(
        command: String,
        capability: Capability,
        confirmationGranted: Boolean = false
    ): AuthorizationDecision
}