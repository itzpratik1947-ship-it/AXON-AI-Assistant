package com.pratik.axon.core.language

import java.util.Locale

class AxonSpeechNormalizer {

    fun normalize(command: String): String {

        var text =
            command
                .lowercase(Locale.getDefault())
                .trim()
                .replace(Regex("\\s+"), " ")

        if (text.isEmpty()) {
            return text
        }

        /*
         * Common SpeechRecognizer mistakes.
         * These corrections are intentionally limited
         * to words that are useful for AXON commands.
         */

        val corrections = mapOf(

            // Volume
            "make it loader" to "make it louder",
            "make it louder" to "make it louder",
            "turn the volume dawn" to "turn the volume down",
            "turn volume dawn" to "turn volume down",
            "volume dawn" to "volume down",
            "sound dawn" to "sound down",

            // Brightness
            "make the screen brighter" to "make the screen brighter",
            "make screen brighter" to "make screen brighter",
            "make the screen darker" to "make the screen darker",
            "make screen darker" to "make screen darker",

            // Flashlight
            "turn on flash light" to "turn on flashlight",
            "turn off flash light" to "turn off flashlight",
            "switch on flash light" to "switch on flashlight",
            "switch off flash light" to "switch off flashlight",

            // Wi-Fi
            "turn on wi fi" to "turn on wifi",
            "turn off wi fi" to "turn off wifi",
            "switch on wi fi" to "switch on wifi",
            "switch off wi fi" to "switch off wifi",

            // Common words
            "turn it of" to "turn it off",
            "turn it on" to "turn it on",
            "open what sapp" to "open whatsapp",
            "open what's app" to "open whatsapp",
            
            "my favorite programming language is hotline" to
            "my favorite programming language is Kotlin",
            "my favourite programming language is hotline" to
            "my favourite programming language is Kotlin",
            "my main project is action" to
            "my main project is AXON"
        )

        for ((wrong, correct) in corrections) {
            if (text == wrong) {
                text = correct
                break
            }
        }

        return text
    }
}