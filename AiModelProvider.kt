package com.pratik.axon.providers.ai

interface AiModelProvider {

    fun getId(): String

    fun getName(): String

    fun isAvailable(): Boolean

    fun generate(
        request: ModelRequest
    ): ModelResponse
}