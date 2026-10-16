package com.pratik.axon.platform.android.device

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.pratik.axon.core.capability.AxonTool
import com.pratik.axon.core.model.AxonResult
import java.util.Locale
import kotlin.math.roundToInt
import com.pratik.axon.core.capability.Capability
import com.pratik.axon.core.security.RiskLevel
import android.Manifest

class BrightnessTool(
    private val context: Context
) : AxonTool {

    override fun getName(): String {
        return "Brightness"
    }
    
    override fun getRiskLevel(): RiskLevel {
    return RiskLevel.LOW
}

override fun getCapability(): Capability {
    return Capability(
        id = getName(),
        name = getName(),
        description = "Reads and controls screen brightness.",
        requiredPermissions = listOf(
            Manifest.permission.WRITE_SETTINGS
        ),
        riskLevel = RiskLevel.LOW
    )
}

    override fun canHandle(command: String): Boolean {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        return text.contains("brightness") &&
                !text.contains("settings")
    }

    override fun execute(command: String): AxonResult {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        // 🔐 Check permission
        if (!Settings.System.canWrite(context)) {

            return try {

                val intent =
                    Intent(
                        Settings.ACTION_MANAGE_WRITE_SETTINGS
                    )

                intent.data =
                    Uri.parse(
                        "package:${context.packageName}"
                    )

                context.startActivity(intent)

                AxonResult(
                    success = false,
                    message = "Please allow AXON to modify system settings, then try again."
                )

            } catch (e: Exception) {

                AxonResult(
                    success = false,
                    message = "I need permission to control screen brightness."
                )
            }
        }

        // 📊 GET CURRENT BRIGHTNESS
        if (
            text.contains("what is the brightness") ||
            text.contains("what's the brightness") ||
            text.contains("brightness level") ||
            text.contains("current brightness") ||
            text.contains("brightness percentage") ||
            text.contains("how bright") ||
            text.contains("about brightness") ||
            text.contains("about the brightness") ||
            text.contains("brightness status") ||
            text == "brightness" ||
            text == "the brightness"
        ) {

            val brightness =
                Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    0
                )

            val brightnessPercent =
                (brightness / 255.0 * 100.0 / 0.85)
                    .coerceIn(0.0, 100.0)
                    .roundToInt()

            return AxonResult(
                success = true,
                message = "The current brightness level is $brightnessPercent percent."
            )
        }

        // 🎯 SET BRIGHTNESS TO X PERCENT
        if (
            text.contains("set brightness") ||
            text.contains("brightness to") ||
            text.contains("brightness at")
        ) {

            val percentRegex =
                Regex("""(\d+(?:\.\d+)?)\s*%?""")

            val match =
                percentRegex.find(text)

            if (match != null) {

                val percent =
                    match.groupValues[1].toDouble()

                val safePercent =
                    percent.coerceIn(0.0, 100.0)

                // Device calibration:
                // Android scale ≈ 85% of user-facing scale
                val brightnessValue =
                    (safePercent * 0.85 / 100.0 * 255.0)
                        .toInt()

                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
                )

                return AxonResult(
                    success = true,
                    message = "Brightness set to ${safePercent.toInt()} percent."
                )
            }
        }

        // 🔆 INCREASE BRIGHTNESS
        if (
            text.contains("increase") &&
            text.contains("brightness") ||
            text.contains("turn up") &&
            text.contains("brightness") ||
            text.contains("raise") &&
            text.contains("brightness") ||
            text.contains("brightness up")
        ) {

            val current =
                Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    128
                )

            val newValue =
                (current + 25)
                    .coerceAtMost(255)

            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                newValue
            )

            return AxonResult(
                success = true,
                message = "Brightness increased."
            )
        }

        // 🔅 DECREASE BRIGHTNESS
        if (
            text.contains("decrease") &&
            text.contains("brightness") ||
            text.contains("turn down") &&
            text.contains("brightness") ||
            text.contains("lower") &&
            text.contains("brightness") ||
            text.contains("brightness down")
        ) {

            val current =
                Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    128
                )

            val newValue =
                (current - 25)
                    .coerceAtLeast(0)

            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                newValue
            )

            return AxonResult(
                success = true,
                message = "Brightness decreased."
            )
        }

        return AxonResult(
            success = false,
            message = "I couldn't understand the brightness command."
        )
    }
}