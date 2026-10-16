package com.pratik.axon.core.security

data class ExecutionAuthorizationResult(
    val decision: ExecutionDecision,
    val authorization: AuthorizationDecision
)