package com.pratik.axon.core.model

data class AxonResult(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any?> = emptyMap(),
    val error: AxonError? = null
)

data class AxonError(
    val code: String,
    val message: String
)