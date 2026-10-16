package com.pratik.axon.core.capability

import com.pratik.axon.core.model.AxonResult
import com.pratik.axon.core.security.RiskLevel

interface AxonTool {

    fun getName(): String

    fun canHandle(command: String): Boolean

    fun execute(command: String): AxonResult
    
    fun getRiskLevel(): RiskLevel {
        return RiskLevel.SAFE
    }

    fun getCapability(): Capability {
    return Capability(
        id = getName(),
        name = getName(),
        description = "AXON capability: ${getName()}",
        riskLevel = getRiskLevel()
    )
}
}