package com.pratik.axon.platform.android.device

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import com.pratik.axon.core.capability.AxonTool
import com.pratik.axon.core.model.AxonResult
import java.util.Locale
import com.pratik.axon.core.capability.Capability
import com.pratik.axon.core.security.RiskLevel

class WifiTool(
    private val context: Context
) : AxonTool {

    override fun getName(): String {
        return "Wi-Fi"
    }
    override fun getRiskLevel(): RiskLevel {
    return RiskLevel.LOW
}

override fun getCapability(): Capability {
    return Capability(
        id = getName(),
        name = getName(),
        description = "Reads Wi-Fi state and opens Wi-Fi settings.",
        riskLevel = RiskLevel.LOW
    )
}

    override fun canHandle(command: String): Boolean {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        return text.contains("wifi") ||
                text.contains("wi-fi") ||
                text.contains("wi fi") ||
                text.contains("why fi") ||
                text.contains("whyfy")
    }

    override fun execute(command: String): AxonResult {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        if (text.contains("settings")) {
            return openWifiSettings()
        }

        val turnOn =
            text.contains("turn on wifi") ||
            text.contains("turn on wi-fi") ||
            text.contains("turn on wi fi") ||
            text.contains("switch wifi on") ||
            text.contains("switch wi-fi on") ||
            text.contains("switch wi fi on") ||
            text.contains("enable wifi") ||
            text.contains("enable wi-fi") ||
            text.contains("enable wi fi")

        val turnOff =
            text.contains("turn off wifi") ||
            text.contains("turn off wi-fi") ||
            text.contains("turn off wi fi") ||
            text.contains("switch wifi off") ||
            text.contains("switch wi-fi off") ||
            text.contains("switch wi fi off") ||
            text.contains("disable wifi") ||
            text.contains("disable wi-fi") ||
            text.contains("disable wi fi")

        if (turnOn || turnOff) {
            return openWifiSettings()
        }

        return getWifiStatus()
    }

    private fun getWifiStatus(): AxonResult {

        return try {

            val connectivityManager =
                context.getSystemService(
                    Context.CONNECTIVITY_SERVICE
                ) as ConnectivityManager

            for (network in connectivityManager.allNetworks) {

                val capabilities =
                    connectivityManager
                        .getNetworkCapabilities(network)

                if (
                    capabilities != null &&
                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                    )
                ) {
                    return AxonResult(
                        success = true,
                        message = "Wi-Fi is connected."
                    )
                }
            }

            AxonResult(
                success = true,
                message = "Wi-Fi is not connected."
            )

        } catch (e: Exception) {

            AxonResult(
                success = false,
                message = "I couldn't check the Wi-Fi status."
            )
        }
    }

    private fun openWifiSettings(): AxonResult {

        return try {

            val intent =
                Intent(Settings.ACTION_WIFI_SETTINGS)

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(intent)

            AxonResult(
                success = true,
                message = "Opening Wi-Fi settings."
            )

        } catch (e: Exception) {

            AxonResult(
                success = false,
                message = "I couldn't open Wi-Fi settings."
            )
        }
    }
}