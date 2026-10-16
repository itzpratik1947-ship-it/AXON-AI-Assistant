package com.pratik.axon.providers.ai

class ModelRegistry {

    private val providers =
        mutableMapOf<String, AiModelProvider>()

    fun registerProvider(
        provider: AiModelProvider
    ) {
        providers[provider.getId()] =
            provider
    }

    fun getProvider(
        id: String
    ): AiModelProvider? {
        return providers[id]
    }

    fun getAllProviders(): List<AiModelProvider> {
        return providers.values.toList()
    }

    fun hasProvider(
        id: String
    ): Boolean {
        return providers.containsKey(id)
    }

    fun getAvailableProviders(): List<AiModelProvider> {

        return providers.values.filter {
            it.isAvailable()
        }
    }

    fun clear() {
        providers.clear()
    }
}