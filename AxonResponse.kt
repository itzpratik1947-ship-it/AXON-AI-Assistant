package com.pratik.axon.core.model

data class AxonResponse(
    val message: String,
    val success: Boolean = true,
    val data: Map<String, Any?> = emptyMap()
)