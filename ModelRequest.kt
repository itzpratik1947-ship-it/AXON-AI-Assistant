package com.pratik.axon.providers.ai

data class ModelRequest(
    val prompt: String,
    val systemInstruction: String? = null,
    val conversationId: String? = null,
    val maxOutputTokens: Int? = null
)