package com.pratik.axon.core.capability

import com.pratik.axon.core.model.AxonParameter

data class CapabilityInvocation(
    val capabilityId: String,
    val action: String,
    val parameters: List<AxonParameter> = emptyList(),
    val requiredPermissions: List<String> = emptyList()
)