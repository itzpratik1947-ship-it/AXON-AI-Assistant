package com.pratik.axon.platform.android.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class AxonAuthentication(
    private val activity: FragmentActivity
) {

    fun authenticate(
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {

        val biometricManager = BiometricManager.from(activity)

        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val canAuthenticate =
            biometricManager.canAuthenticate(authenticators)

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            onFailure()
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    onFailure()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Keep the authentication screen active.
                    // Do not unlock AXON.
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("AXON Authentication")
            .setSubtitle("Verify your identity to continue")
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}