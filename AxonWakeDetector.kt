package com.pratik.axon.platform.android.wake

class AxonWakeDetector {

    private var isListening = false

    fun start() {
        isListening = true
    }

    fun stop() {
        isListening = false
    }

    fun isRunning(): Boolean {
        return isListening
    }

    fun processText(text: String): Boolean {

        if (!isListening) {
            return false
        }

        val normalized =
            text
                .lowercase()
                .trim()

        return normalized == "hello axon" ||
                normalized.contains("hello axon")
    }
}