package com.pratik.axon.platform.android.security

// =========================================================
// 🛡️ AXON SECURITY INTENT
// =========================================================

enum class AxonSecurityIntent {

    // No AXON authentication required
    LOW_RISK,

    // AXON authentication required
    PRIVATE,

    // AXON authentication + future explicit confirmation
    HIGH_RISK
}