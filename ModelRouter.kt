package com.pratik.axon.providers.ai

class ModelRouter {

    private val providers =
        mutableListOf<AiModelProvider>()

    fun registerProvider(
        provider: AiModelProvider
    ) {
        providers.add(provider)
    }

    fun getProviders(): List<AiModelProvider> {
        return providers.toList()
    }

    fun findAvailableProvider(): AiModelProvider? {

        for (provider in providers) {

            if (provider.isAvailable()) {
                return provider
            }
        }

        return null
    }

    fun generate(
        request: ModelRequest
    ): ModelResponse {

        val provider =
            findAvailableProvider()

        return if (provider != null) {

            provider.generate(request)

        } else {

            ModelResponse(
                success = false,
                text = "",
                providerId = "none",
                error = "No AI model provider is currently available."
            )
        }
    }

    fun clear() {
        providers.clear()
    }
}