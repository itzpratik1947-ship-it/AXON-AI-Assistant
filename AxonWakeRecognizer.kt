package com.pratik.axon.platform.android.wake

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class AxonWakeRecognizer(
    private val context: Context,
    private val wakeDetector: AxonWakeDetector,
    private val onWakeWordDetected: () -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null

    private var isRunning = false

    private var sessionId = 0

    private var consecutiveFailures = 0

    private val handler =
        Handler(Looper.getMainLooper())

    companion object {

        private const val NORMAL_RESTART_DELAY = 1500L

        private const val FAILURE_RESTART_DELAY = 5000L

        private const val MAX_CONSECUTIVE_FAILURES = 5
    }

    // =========================================================
    // ▶️ START
    // =========================================================

    fun start() {

        if (isRunning) {
            return
        }

        if (
            !SpeechRecognizer.isRecognitionAvailable(
                context
            )
        ) {
            return
        }

        isRunning = true

        wakeDetector.start()

        consecutiveFailures = 0

        scheduleListening(
            500L
        )
    }

    // =========================================================
    // ⏹️ STOP
    // =========================================================

    fun stop() {

        isRunning = false

        sessionId++

        wakeDetector.stop()

        handler.removeCallbacksAndMessages(
            null
        )

        destroyRecognizer()
    }

    // =========================================================
    // ⏱️ SCHEDULE LISTENING
    // =========================================================

    private fun scheduleListening(
        delay: Long
    ) {

        if (!isRunning) {
            return
        }

        handler.postDelayed({

            if (isRunning) {
                startListeningSession()
            }

        }, delay)
    }

    // =========================================================
    // 🎙️ START ONE LISTENING SESSION
    // =========================================================

    private fun startListeningSession() {

        if (!isRunning) {
            return
        }

        destroyRecognizer()

        val currentSession =
            ++sessionId

        val recognizer =
            SpeechRecognizer
                .createSpeechRecognizer(
                    context
                )

        speechRecognizer =
            recognizer

        recognizer.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {
                }

                override fun onBeginningOfSpeech() {
                }

                override fun onRmsChanged(
                    rmsdB: Float
                ) {
                }

                override fun onBufferReceived(
                    buffer: ByteArray?
                ) {
                }

                override fun onEndOfSpeech() {
                }

                override fun onError(
                    error: Int
                ) {

                    if (
                        !isCurrentSession(
                            currentSession
                        )
                    ) {
                        return
                    }

                    handleRecognitionFailure()
                }

                override fun onResults(
                    results: Bundle?
                ) {

                    if (
                        !isCurrentSession(
                            currentSession
                        )
                    ) {
                        return
                    }

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer
                                .RESULTS_RECOGNITION
                        )

                    var wakeDetected =
                        false

                    if (
                        !matches.isNullOrEmpty()
                    ) {

                        for (
                            text in matches
                        ) {

                            if (
                                wakeDetector
                                    .processText(
                                        text
                                    )
                            ) {

                                wakeDetected =
                                    true

                                break
                            }
                        }
                    }

                    destroyRecognizer()

                    if (!isRunning) {
                        return
                    }

                    if (wakeDetected) {

                        consecutiveFailures = 0

                        onWakeWordDetected()

                        return
                    }

                    consecutiveFailures = 0

                    scheduleListening(
                        NORMAL_RESTART_DELAY
                    )
                }

                override fun onPartialResults(
                    partialResults: Bundle?
                ) {
                }

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {
                }
            }
        )

        val intent =
            Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "en-IN"
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_MAX_RESULTS,
            3
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PARTIAL_RESULTS,
            false
        )

        try {

            recognizer.startListening(
                intent
            )

        } catch (
            exception: Exception
        ) {

            if (
                isCurrentSession(
                    currentSession
                )
            ) {

                handleRecognitionFailure()
            }
        }
    }

    // =========================================================
    // ⚠️ HANDLE FAILURE
    // =========================================================

    private fun handleRecognitionFailure() {

        if (!isRunning) {
            return
        }

        consecutiveFailures++

        destroyRecognizer()

        if (
            consecutiveFailures >=
            MAX_CONSECUTIVE_FAILURES
        ) {

            consecutiveFailures = 0

            scheduleListening(
                FAILURE_RESTART_DELAY
            )

        } else {

            scheduleListening(
                NORMAL_RESTART_DELAY
            )
        }
    }

    // =========================================================
    // 🔍 SESSION CHECK
    // =========================================================

    private fun isCurrentSession(
        currentSession: Int
    ): Boolean {

        return isRunning &&
                currentSession ==
                sessionId
    }

    // =========================================================
    // 🧹 DESTROY RECOGNIZER
    // =========================================================

    private fun destroyRecognizer() {

        val recognizer =
            speechRecognizer

        speechRecognizer =
            null

        if (recognizer != null) {

            try {
                recognizer.stopListening()
            } catch (_: Exception) {
            }

            try {
                recognizer.cancel()
            } catch (_: Exception) {
            }

            try {
                recognizer.destroy()
            } catch (_: Exception) {
            }
        }
    }
}