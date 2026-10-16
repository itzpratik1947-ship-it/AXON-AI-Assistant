package com.pratik.axon.platform.android.device

import android.content.Context
import android.media.AudioManager
import com.pratik.axon.core.capability.AxonTool
import com.pratik.axon.core.model.AxonResult
import java.util.Locale
import kotlin.math.roundToInt
import com.pratik.axon.core.capability.Capability
import com.pratik.axon.core.security.RiskLevel

class VolumeTool(
    private val context: Context
) : AxonTool {

    override fun getName(): String {
        return "Volume"
    }
    
    override fun getRiskLevel(): RiskLevel {
    return RiskLevel.LOW
}

override fun getCapability(): Capability {
    return Capability(
        id = getName(),
        name = getName(),
        description = "Reads and controls media volume.",
        riskLevel = RiskLevel.LOW
    )
}

    override fun canHandle(command: String): Boolean {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        return text == "mute" ||
                text == "unmute" ||
                text.contains("mute") ||
                text.contains("unmute") ||
                text.contains("volume")
    }

    override fun execute(command: String): AxonResult {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        val audioManager =
            context.getSystemService(
                Context.AUDIO_SERVICE
            ) as AudioManager

        // 📊 GET CURRENT VOLUME
        if (
            text.contains("what is the volume") ||
            text.contains("what's the volume") ||
            text.contains("volume level") ||
            text.contains("current volume") ||
            text.contains("volume percentage") ||
            text.contains("how loud")
        ) {

            val currentVolume =
                audioManager.getStreamVolume(
                    AudioManager.STREAM_MUSIC
                )

            val maxVolume =
                audioManager.getStreamMaxVolume(
                    AudioManager.STREAM_MUSIC
                )

            val volumePercent =
                if (maxVolume > 0) {
                    (
                        currentVolume.toDouble() /
                                maxVolume.toDouble() *
                                100.0
                    ).roundToInt()
                } else {
                    0
                }

            return AxonResult(
                success = true,
                message = "The current volume level is $volumePercent percent."
            )
        }

        // 🔊 Unmute
        if (
            text == "unmute" ||
            text.contains("unmute") ||
            text.contains("turn on sound") ||
            text.contains("turn sound on")
        ) {

            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_UNMUTE,
                AudioManager.FLAG_SHOW_UI
            )

            return AxonResult(
                success = true,
                message = "Volume unmuted."
            )
        }

        // 🔇 Mute
        if (
            text == "mute" ||
            text.contains("mute") ||
            text.contains("turn off sound") ||
            text.contains("turn sound off")
        ) {

            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_MUTE,
                AudioManager.FLAG_SHOW_UI
            )

            return AxonResult(
                success = true,
                message = "Volume muted."
            )
        }

        // 🎯 Set volume percentage
        if (
            text.contains("set volume") ||
            text.contains("volume to") ||
            text.contains("volume at")
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

                val maxVolume =
                    audioManager.getStreamMaxVolume(
                        AudioManager.STREAM_MUSIC
                    )

                val targetVolume =
                    ((safePercent / 100.0) * maxVolume)
                        .roundToInt()

                audioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    targetVolume,
                    AudioManager.FLAG_SHOW_UI
                )

                return AxonResult(
                    success = true,
                    message = "Volume set to ${safePercent.toInt()} percent."
                )
            }
        }

        // 🔊 Increase volume
        if (
            text.contains("increase") &&
            text.contains("volume") ||
            text.contains("turn up") &&
            text.contains("volume") ||
            text.contains("raise") &&
            text.contains("volume") ||
            text.contains("volume up")
        ) {

            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )

            return AxonResult(
                success = true,
                message = "Volume increased."
            )
        }

        // 🔉 Decrease volume
        if (
            text.contains("decrease") &&
            text.contains("volume") ||
            text.contains("turn down") &&
            text.contains("volume") ||
            text.contains("lower") &&
            text.contains("volume") ||
            text.contains("volume down")
        ) {

            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )

            return AxonResult(
                success = true,
                message = "Volume decreased."
            )
        }

        return AxonResult(
            success = false,
            message = "I couldn't understand the volume command."
        )
    }
}