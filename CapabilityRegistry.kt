package com.pratik.axon.core.capability

class CapabilityRegistry {

    private val capabilities =
        mutableMapOf<String, Capability>()

    fun registerCapability(
        capability: Capability
    ) {
        capabilities[capability.id] =
            capability
    }

    fun getCapability(
        id: String
    ): Capability? {
        return capabilities[id]
    }

    fun getAllCapabilities(): List<Capability> {
        return capabilities.values.toList()
    }

    fun hasCapability(
        id: String
    ): Boolean {
        return capabilities.containsKey(id)
    }

    fun clear() {
        capabilities.clear()
    }
}