package com.pratik.axon.platform.android.device

import com.pratik.axon.core.capability.AxonTool
import com.pratik.axon.core.model.AxonResult
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import java.util.Locale
import com.pratik.axon.core.capability.Capability
import com.pratik.axon.core.security.RiskLevel

class DeviceInfoTool(
    private val context: Context
) : AxonTool {

    override fun getName(): String {
        return "Device Information"
    }
    
    override fun getRiskLevel(): RiskLevel {
    return RiskLevel.SAFE
}

override fun getCapability(): Capability {
    return Capability(
        id = getName(),
        name = getName(),
        description = "Reads basic device information.",
        riskLevel = RiskLevel.SAFE
    )
}

    override fun canHandle(command: String): Boolean {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        return text.contains("battery") ||
                text.contains("charging") ||
                text.contains("device model") ||
                text.contains("phone model") ||
                text.contains("what phone") ||
                text.contains("which phone")
    }

    override fun execute(command: String): AxonResult {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        // 🔋 BATTERY LEVEL
        if (
            text.contains("battery") &&
            !text.contains("charging")
        ) {

            val batteryManager =
                context.getSystemService(
                    Context.BATTERY_SERVICE
                ) as BatteryManager

            val batteryLevel =
                batteryManager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
                )
return AxonResult(
    success = true,
    message = "Your battery level is $batteryLevel percent."
)
        }

        // ⚡ CHARGING STATUS
        if (
            text.contains("charging") ||
            text.contains("am i charging")
        ) {

            val batteryIntent =
                context.registerReceiver(
                    null,
                    android.content.IntentFilter(
                        android.content.Intent.ACTION_BATTERY_CHANGED
                    )
                )

            val status =
                batteryIntent?.getIntExtra(
                    BatteryManager.EXTRA_STATUS,
                    -1
                )

            return when (status) {

    BatteryManager.BATTERY_STATUS_CHARGING,
    BatteryManager.BATTERY_STATUS_FULL ->
        AxonResult(
            success = true,
            message = "The device is charging."
        )

    else ->
        AxonResult(
            success = true,
            message = "The device is not charging."
        )
}
        }

        // 📱 DEVICE MODEL
        if (
            text.contains("device model") ||
            text.contains("phone model") ||
            text.contains("what phone") ||
            text.contains("which phone")
        ) {

            return AxonResult(
    success = true,
    message = "Your device is ${Build.MANUFACTURER} ${Build.MODEL}."
)
        }

        return AxonResult(
    success = false,
    message = "I couldn't understand that device information command."
)
    }
}