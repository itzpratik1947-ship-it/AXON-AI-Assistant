package com.pratik.axon.platform.android.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.pratik.axon.core.capability.AxonTool
import com.pratik.axon.core.model.AxonResult
import java.util.Locale
import com.pratik.axon.core.capability.Capability
import com.pratik.axon.core.security.RiskLevel

class AppLauncherTool(
    private val context: Context
) : AxonTool {

    override fun getName(): String {
        return "App Launcher"
    }
    
    override fun getRiskLevel(): RiskLevel {
    return RiskLevel.MEDIUM
}

override fun getCapability(): Capability {
    return Capability(
        id = getName(),
        name = getName(),
        description = "Launches installed applications.",
        riskLevel = RiskLevel.MEDIUM
    )
}

    override fun canHandle(command: String): Boolean {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        return text.startsWith("open ") ||
                text.startsWith("launch ") ||
                text.startsWith("start ")
    }

    override fun execute(command: String): AxonResult {

        var appName =
            command
                .lowercase(Locale.getDefault())
                .trim()

        // Remove command prefix
        appName =
            appName
                .replaceFirst(
                    Regex("^open\\s+"),
                    ""
                )
                .replaceFirst(
                    Regex("^launch\\s+"),
                    ""
                )
                .replaceFirst(
                    Regex("^start\\s+"),
                    ""
                )
                .replaceFirst(
                    Regex("^please\\s+"),
                    ""
                )
                .trim()

        if (appName.isEmpty()) {
            return AxonResult(
                success = false,
                message = "Please tell me which app to open."
            )
        }

        val packageManager =
            context.packageManager

        val intent =
            Intent(
                Intent.ACTION_MAIN,
                null
            )

        intent.addCategory(
            Intent.CATEGORY_LAUNCHER
        )

        val apps =
            packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_ALL
            )

        // =====================================================
        // 🎯 EXACT APP NAME MATCH
        // =====================================================

        for (app in apps) {

            val label =
                app.loadLabel(
                    packageManager
                )
                    .toString()
                    .lowercase(Locale.getDefault())
                    .trim()

            if (label == appName) {

                val launchIntent =
                    packageManager
                        .getLaunchIntentForPackage(
                            app.activityInfo.packageName
                        )

                if (launchIntent != null) {

                    context.startActivity(
                        launchIntent
                    )

                    return AxonResult(
                        success = true,
                        message = "Opening $label."
                    )
                }
            }
        }

        // =====================================================
        // 🔎 PARTIAL APP NAME MATCH
        // =====================================================

        for (app in apps) {

            val label =
                app.loadLabel(
                    packageManager
                )
                    .toString()
                    .lowercase(Locale.getDefault())
                    .trim()

            if (
                label.contains(appName) ||
                appName.contains(label)
            ) {

                val launchIntent =
                    packageManager
                        .getLaunchIntentForPackage(
                            app.activityInfo.packageName
                        )

                if (launchIntent != null) {

                    context.startActivity(
                        launchIntent
                    )

                    return AxonResult(
                        success = true,
                        message = "Opening $label."
                    )
                }
            }
        }

        return AxonResult(
            success = false,
            message = "I couldn't find an app called $appName."
        )
    }
}