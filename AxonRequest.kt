package com.pratik.axon.core.model

data class AxonRequest(
    val text: String,
    val source: RequestSource = RequestSource.USER,
    val confirmationGranted: Boolean = false
)

enum class RequestSource {
    USER,
    SYSTEM,
    EVENT
}