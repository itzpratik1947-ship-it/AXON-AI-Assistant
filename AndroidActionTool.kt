package com.pratik.axon.platform.android.device

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.pratik.axon.core.capability.AxonTool
import com.pratik.axon.core.model.AxonResult
import java.util.Locale
import com.pratik.axon.core.capability.Capability
import com.pratik.axon.core.security.RiskLevel

class AndroidActionTool(
    private val context: Context
) : AxonTool {

    override fun getName(): String {
        return "Android Actions"
    }
    
    override fun getRiskLevel(): RiskLevel {
    return RiskLevel.LOW
}

override fun getCapability(): Capability {
    return Capability(
        id = getName(),
        name = getName(),
        description = "Opens supported Android system settings and actions.",
        riskLevel = RiskLevel.LOW
    )
}

    override fun canHandle(command: String): Boolean {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        return text.contains("open wifi settings") ||
                text.contains("open wi-fi settings") ||
                text.contains("open wi fi settings") ||
                text.contains("go to wifi settings") ||
                text.contains("open sound settings") ||
                text.contains("open audio settings") ||
                text.contains("go to sound settings") ||
                text.contains("open brightness settings") ||
                text.contains("open display settings") ||
                text.contains("go to brightness settings") ||
                text == "open settings" ||
                text == "open phone settings" ||
                text == "open android settings"
    }

    override fun execute(command: String): AxonResult {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        if (
            text.contains("open wifi settings") ||
            text.contains("open wi-fi settings") ||
            text.contains("open wi fi settings") ||
            text.contains("go to wifi settings")
        ) {

            return openSettings(
                Settings.ACTION_WIFI_SETTINGS,
                "Opening Wi-Fi settings.",
                "I couldn't open Wi-Fi settings."
            )
        }

        if (
            text.contains("open sound settings") ||
            text.contains("open audio settings") ||
            text.contains("go to sound settings")
        ) {

            return openSettings(
                Settings.ACTION_SOUND_SETTINGS,
                "Opening sound settings.",
                "I couldn't open the sound settings."
            )
        }

        if (
            text.contains("open brightness settings") ||
            text.contains("open display settings") ||
            text.contains("go to brightness settings")
        ) {

            return openSettings(
                Settings.ACTION_DISPLAY_SETTINGS,
                "Opening brightness settings.",
                "I couldn't open the brightness settings."
            )
        }

        if (
            text == "open settings" ||
            text == "open phone settings" ||
            text == "open android settings"
        ) {

            return openSettings(
                Settings.ACTION_SETTINGS,
                "Opening settings.",
                "I couldn't open settings."
            )
        }

        return AxonResult(
            success = false,
            message = "I couldn't understand that Android action."
        )
    }

    private fun openSettings(
        action: String,
        successMessage: String,
        errorMessage: String
    ): AxonResult {

        return try {

            val intent =
                Intent(action)

            context.startActivity(intent)

            AxonResult(
                success = true,
                message = successMessage
            )

        } catch (e: Exception) {

            AxonResult(
                success = false,
                message = errorMessage
            )
        }
    }
}