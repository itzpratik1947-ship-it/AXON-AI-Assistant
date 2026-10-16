package com.pratik.axon.core.engine

import com.pratik.axon.core.model.AxonIntent
import com.pratik.axon.core.model.AxonResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BuiltInIntentHandler(
    private val intentEngine: AxonIntentEngine
) {

    fun handle(
        command: String
    ): AxonResult? {

        return when (
            intentEngine.detectIntent(command)
        ) {

            AxonIntent.GREETING -> {
                AxonResult(
                    success = true,
                    message = "Yes Master."
                )
            }

            AxonIntent.TIME -> {
                val time =
                    SimpleDateFormat(
                        "h:mm a",
                        Locale.getDefault()
                    ).format(Date())

                AxonResult(
                    success = true,
                    message =
                        "The current time is $time."
                )
            }

            AxonIntent.DATE -> {
                val date =
                    SimpleDateFormat(
                        "EEEE, d MMMM yyyy",
                        Locale.getDefault()
                    ).format(Date())

                AxonResult(
                    success = true,
                    message =
                        "Today is $date."
                )
            }

            AxonIntent.IDENTITY -> {
                AxonResult(
                    success = true,
                    message =
                        "I am AXON, your personal AI assistant."
                )
            }

            else -> null
        }
    }
}