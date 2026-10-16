package com.pratik.axon.platform.android.security

class HighRiskConfirmationManager {

    fun getConfirmationMessage(
        command: String
    ): String {

        return "This is a high-risk action. " +
                "Please confirm if you want me to continue."
    }

    fun isConfirmation(
        response: String
    ): Boolean {

        val text =
            response
                .lowercase()
                .trim()

        return text == "yes" ||
                text == "confirm" ||
                text == "confirmed" ||
                text == "proceed" ||
                text == "continue"
    }

    fun isCancellation(
        response: String
    ): Boolean {

        val text =
            response
                .lowercase()
                .trim()

        return text == "no" ||
                text == "cancel" ||
                text == "stop" ||
                text == "don't" ||
                text == "do not"
    }
}