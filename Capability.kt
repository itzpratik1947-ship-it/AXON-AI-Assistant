package com.pratik.axon.core.capability

import com.pratik.axon.core.security.RiskLevel

data class Capability(
    val id: String,
    val name: String,
    val description: String,
    val actions: List<String> = emptyList(),
    val requiredPermissions: List<String> = emptyList(),
    val riskLevel: RiskLevel = RiskLevel.SAFE
)