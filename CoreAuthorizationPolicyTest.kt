package com.pratik.axon.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreAuthorizationPolicyTest {

    private class TestPermissionChecker(
        private val grantedPermissions: Set<String> = emptySet()
    ) : PermissionChecker {

        override fun hasPermission(
            permission: String
        ): Boolean {
            return grantedPermissions.contains(
                permission
            )
        }
    }

    private class TestConfirmationPolicy(
        private val confirmationRequired: Boolean
    ) : ConfirmationPolicy {

        override fun requiresConfirmation(
            riskLevel: RiskLevel
        ): Boolean {
            return confirmationRequired
        }
    }

    @Test
    fun safeAction_isAllowed() {

        val policy =
            CoreAuthorizationPolicy(
                permissionChecker =
                    TestPermissionChecker(),
                confirmationPolicy =
                    TestConfirmationPolicy(
                        confirmationRequired = false
                    )
            )

        val decision =
            policy.authorize(
                riskLevel = RiskLevel.SAFE
            )

        assertTrue(
            decision.allowed
        )

        assertFalse(
            decision.requiresConfirmation
        )
    }

    @Test
    fun missingPermission_blocksAction() {

        val policy =
            CoreAuthorizationPolicy(
                permissionChecker =
                    TestPermissionChecker(),
                confirmationPolicy =
                    TestConfirmationPolicy(
                        confirmationRequired = false
                    )
            )

        val decision =
            policy.authorize(
                riskLevel = RiskLevel.MEDIUM,
                permissions =
                    listOf(
                        "android.permission.CAMERA"
                    )
            )

        assertFalse(
            decision.allowed
        )

        assertFalse(
            decision.requiresConfirmation
        )

        assertTrue(
            decision.missingPermissions.contains(
                "android.permission.CAMERA"
            )
        )
    }

    @Test
    fun confirmationRequired_blocksUntilConfirmed() {

        val policy =
            CoreAuthorizationPolicy(
                permissionChecker =
                    TestPermissionChecker(),
                confirmationPolicy =
                    TestConfirmationPolicy(
                        confirmationRequired = true
                    )
            )

        val decision =
            policy.authorize(
                riskLevel = RiskLevel.HIGH
            )

        assertFalse(
            decision.allowed
        )

        assertTrue(
            decision.requiresConfirmation
        )
    }

    @Test
    fun availablePermission_allowsAction() {

        val policy =
            CoreAuthorizationPolicy(
                permissionChecker =
                    TestPermissionChecker(
                        grantedPermissions =
                            setOf(
                                "android.permission.CAMERA"
                            )
                    ),
                confirmationPolicy =
                    TestConfirmationPolicy(
                        confirmationRequired = false
                    )
            )

        val decision =
            policy.authorize(
                riskLevel = RiskLevel.MEDIUM,
                permissions =
                    listOf(
                        "android.permission.CAMERA"
                    )
            )

        assertTrue(
            decision.allowed
        )

        assertFalse(
            decision.requiresConfirmation
        )

        assertTrue(
            decision.missingPermissions.isEmpty()
        )
    }
}