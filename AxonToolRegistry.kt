package com.pratik.axon.core.capability

class AxonToolRegistry {

    private val tools =
        mutableListOf<AxonTool>()

    private val capabilityRegistry =
        CapabilityRegistry()

    fun registerTool(
        tool: AxonTool
    ) {
        tools.add(tool)

        capabilityRegistry.registerCapability(
            tool.getCapability()
        )
    }

    fun getTools(): List<AxonTool> {
        return tools.toList()
    }

    fun findTool(
        command: String
    ): AxonTool? {

        for (tool in tools) {

            if (tool.canHandle(command)) {
                return tool
            }
        }

        return null
    }
    
    fun getCapabilityForTool(
    tool: AxonTool
): Capability? {

    return capabilityRegistry.getCapability(
        tool.getName()
    )
}

    fun getCapabilities(): List<Capability> {
        return capabilityRegistry.getAllCapabilities()
    }

    fun getCapability(
        id: String
    ): Capability? {
        return capabilityRegistry.getCapability(id)
    }

    fun hasCapability(
        id: String
    ): Boolean {
        return capabilityRegistry.hasCapability(id)
    }

    fun clear() {
        tools.clear()
        capabilityRegistry.clear()
    }
}