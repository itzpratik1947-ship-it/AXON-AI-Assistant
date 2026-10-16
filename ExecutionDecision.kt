package com.pratik.axon.core.security

enum class ExecutionDecision {
    ALLOWED,
    AUTHENTICATION_REQUIRED,
    CONFIRMATION_REQUIRED,
    BLOCKED
}