package com.pratik.axon.platform.android.apps

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.pratik.axon.core.capability.AxonTool
import com.pratik.axon.core.model.AxonResult
import java.util.Locale
import com.pratik.axon.core.capability.Capability
import com.pratik.axon.core.security.RiskLevel

class WebsiteTool(
    private val context: Context
) : AxonTool {

    override fun getName(): String {
        return "Website"
    }
    
    override fun getRiskLevel(): RiskLevel {
    return RiskLevel.SAFE
}

override fun getCapability(): Capability {
    return Capability(
        id = getName(),
        name = getName(),
        description = "Opens websites and web links.",
        riskLevel = RiskLevel.SAFE
    )
}

    override fun canHandle(command: String): Boolean {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        val hasWebsitePattern =
            text.contains(".com") ||
            text.contains(".in") ||
            text.contains(".org") ||
            text.contains(".net") ||
            text.contains(".io") ||
            text.contains(" dot com") ||
            text.contains(" dot in") ||
            text.contains(" dot org") ||
            text.contains(" dot net") ||
            text.contains(" dot io") ||
            Regex(
                """\b[a-z0-9-]+\s+(com|in|org|net|io)\b"""
            ).containsMatchIn(text) ||
            text.contains("http://") ||
            text.contains("https://")

        val hasWebsiteCommand =
            text.startsWith("open ") ||
            text.startsWith("go to ") ||
            text.startsWith("visit ")

        return hasWebsiteCommand &&
                hasWebsitePattern
    }

    override fun execute(command: String): AxonResult {

        return try {

            var url =
                command
                    .lowercase(Locale.getDefault())
                    .trim()

            // Remove command prefix
            url =
                url.replaceFirst(
                    Regex("^open\\s+"),
                    ""
                )

            url =
                url.replaceFirst(
                    Regex("^go to\\s+"),
                    ""
                )

            url =
                url.replaceFirst(
                    Regex("^visit\\s+"),
                    ""
                )

            url = url.trim()

            // Convert spoken "dot com" etc.
            url =
                url.replace(
                    Regex("\\s+dot\\s+"),
                    "."
                )

            // Convert speech recognition such as:
            // "google com" → "google.com"
            url =
                url.replace(
                    Regex("""\s+(com|in|org|net|io)\b"""),
                    ".$1"
                )

            // Remove spaces accidentally inserted by speech recognition
            url =
                url.replace(
                    Regex("\\s+"),
                    ""
                )

            // Add HTTPS if necessary
            if (
                !url.startsWith("http://") &&
                !url.startsWith("https://")
            ) {

                url =
                    "https://$url"
            }

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )

            context.startActivity(intent)

            AxonResult(
                success = true,
                message = "Opening the website."
            )

        } catch (e: Exception) {

            AxonResult(
                success = false,
                message = "I couldn't open that website."
            )
        }
    }
}