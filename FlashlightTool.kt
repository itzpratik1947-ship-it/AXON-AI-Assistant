package com.pratik.axon.platform.android.device

import com.pratik.axon.core.capability.AxonTool
import com.pratik.axon.core.model.AxonResult
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import java.util.Locale
import android.Manifest
import com.pratik.axon.core.capability.Capability
import com.pratik.axon.core.security.RiskLevel

class FlashlightTool(
    private val context: Context
) : AxonTool {

    override fun getName(): String {
        return "Flashlight"
    }
    
    override fun getRiskLevel(): RiskLevel {
    return RiskLevel.LOW
}

override fun getCapability(): Capability {
    return Capability(
        id = getName(),
        name = getName(),
        description = "Controls the device flashlight.",
        requiredPermissions = listOf(Manifest.permission.CAMERA),
        riskLevel = RiskLevel.LOW
    )
}

    override fun canHandle(command: String): Boolean {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        return text.contains("flashlight") ||
                text.contains("torch")
    }

    override fun execute(command: String): AxonResult {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        val turnOn =
            text.contains("turn on flashlight") ||
            text.contains("switch on flashlight") ||
            text.contains("switch the flashlight on") ||
            text.contains("enable flashlight") ||
            text.contains("turn on torch") ||
            text.contains("switch on torch") ||
            text.contains("switch the torch on") ||
            text.contains("enable torch") ||
            text == "flashlight on" ||
            text == "torch on"

        val turnOff =
            text.contains("turn off flashlight") ||
            text.contains("switch off flashlight") ||
            text.contains("switch the flashlight off") ||
            text.contains("disable flashlight") ||
            text.contains("turn off torch") ||
            text.contains("switch off torch") ||
            text.contains("switch the torch off") ||
            text.contains("disable torch") ||
            text == "flashlight off" ||
            text == "torch off"

        if (!turnOn && !turnOff) {
            if (!turnOn && !turnOff) {
    return AxonResult(
        success = false,
        message = "I couldn't understand the flashlight command."
    )
}
        }

        return try {

            val cameraManager =
                context.getSystemService(
                    Context.CAMERA_SERVICE
                ) as CameraManager

            var cameraId: String? = null

            for (id in cameraManager.cameraIdList) {

                val characteristics =
                    cameraManager.getCameraCharacteristics(id)

                val hasFlash =
                    characteristics.get(
                        CameraCharacteristics.FLASH_INFO_AVAILABLE
                    ) == true

                val lensFacing =
                    characteristics.get(
                        CameraCharacteristics.LENS_FACING
                    )

                if (
                    hasFlash &&
                    lensFacing ==
                    CameraCharacteristics.LENS_FACING_BACK
                ) {
                    cameraId = id
                    break
                }
            }

            if (cameraId == null) {
                if (cameraId == null) {
    return AxonResult(
        success = false,
        message = "This device does not have a compatible flashlight."
    )
}
            }

            cameraManager.setTorchMode(
                cameraId,
                turnOn
            )

            if (turnOn) {
    AxonResult(
        success = true,
        message = "Flashlight turned on."
    )
} else {
    AxonResult(
        success = true,
        message = "Flashlight turned off."
    )
}

    } catch (e: SecurityException) {

    AxonResult(
        success = false,
        message = "AXON does not have permission to control the flashlight."
    )

} catch (e: IllegalArgumentException) {

    AxonResult(
        success = false,
        message = "The flashlight is not available right now."
    )

} catch (e: Exception) {

    AxonResult(
        success = false,
        message = "I couldn't control the flashlight."
    )
}
    }
}