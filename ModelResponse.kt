package com.pratik.axon.providers.ai

data class ModelResponse(
    val success: Boolean,
    val text: String,
    val providerId: String,
    val error: String? = null,
    val usage: ModelUsage? = null
)

data class ModelUsage(
    val inputTokens: Int? = null,
    val outputTokens: Int? = null
)