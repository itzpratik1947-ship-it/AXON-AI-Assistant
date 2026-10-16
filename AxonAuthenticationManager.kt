package com.pratik.axon.platform.android.security

import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class AxonAuthenticationManager(
    private val activity: FragmentActivity
) {

    fun authenticate(
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {

        val executor =
            ContextCompat.getMainExecutor(activity)

        val biometricManager =
            BiometricManager.from(activity)

        val authenticators =
            androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val canAuthenticate =
            biometricManager.canAuthenticate(
                authenticators
            )

        if (
            canAuthenticate !=
            BiometricManager.BIOMETRIC_SUCCESS
        ) {
            onFailure()
            return
        }

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("AXON Authentication")
                .setSubtitle(
                    "Authenticate to use this private command."
                )
                .setAllowedAuthenticators(
                    authenticators
                )
                .build()

        val biometricPrompt =
            BiometricPrompt(
                activity,
                executor,
                object :
                    BiometricPrompt.AuthenticationCallback() {

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
                        super.onAuthenticationError(
                            errorCode,
                            errString
                        )

                        onFailure()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()

                        // Keep the prompt available for another attempt.
                    }
                }
            )

        biometricPrompt.authenticate(
            promptInfo
        )
    }
}