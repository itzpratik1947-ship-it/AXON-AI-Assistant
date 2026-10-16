package com.pratik.axon.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

import com.pratik.axon.R

import com.pratik.axon.core.capability.AxonToolRegistry
import com.pratik.axon.core.capability.AxonToolRouter

import com.pratik.axon.core.engine.RequestProcessor
import com.pratik.axon.core.engine.AxonOrchestrator
import com.pratik.axon.core.engine.ResponseComposer
import com.pratik.axon.core.engine.BuiltInIntentHandler

import com.pratik.axon.core.language.AxonSpeechNormalizer

import com.pratik.axon.core.memory.MemoryCommandDetector
import com.pratik.axon.core.memory.MemoryManager
import com.pratik.axon.core.memory.MemoryQueryDetector

import com.pratik.axon.platform.android.personal.PersonalAssistantTool


import com.pratik.axon.platform.android.apps.AppLauncherTool
import com.pratik.axon.platform.android.apps.WebsiteTool
import com.pratik.axon.features.wikipedia.WikipediaTool
import com.pratik.axon.core.engine.ActionPlanner

import com.pratik.axon.platform.android.communication.CommunicationTool

import com.pratik.axon.features.calculator.CalculatorTool

import com.pratik.axon.platform.android.wake.AxonWakeService

import com.pratik.axon.platform.android.device.AndroidActionTool
import com.pratik.axon.platform.android.device.BrightnessTool
import com.pratik.axon.platform.android.device.DeviceInfoTool
import com.pratik.axon.platform.android.device.FlashlightTool
import com.pratik.axon.platform.android.device.VolumeTool
import com.pratik.axon.platform.android.device.WifiTool

import com.pratik.axon.platform.android.memory.MemoryDatabase
import com.pratik.axon.platform.android.memory.MemoryRepository

import com.pratik.axon.platform.android.security.AxonAuthentication
import com.pratik.axon.platform.android.security.AxonSecurityManager
import com.pratik.axon.platform.android.security.HighRiskConfirmationManager

import com.pratik.axon.core.security.RiskLevel
import com.pratik.axon.platform.android.security.AndroidAuthorizationPolicy
import com.pratik.axon.platform.android.security.AndroidPermissionChecker


class MainActivity : FragmentActivity() {

    private val speechNormalizer =
        AxonSpeechNormalizer()

    // =========================================================
    // 🎙️ SPEECH
    // =========================================================

    private lateinit var speechRecognizer: SpeechRecognizer

    private var recognizerStartPending =
        false

    private val speechRestartHandler =
        Handler(
            Looper.getMainLooper()
        )

    private val speechRestartRunnable =
        Runnable {

            recognizerStartPending =
                false

            if (
                !isFinishing &&
                !isDestroyed &&
                !axonIsSpeaking &&
                !axonIsListening
            ) {

                startListening()
            }
        }

    // =========================================================
    // 🔐 AXON AUTHENTICATION
    // =========================================================

    private lateinit var axonAuthentication:
            AxonAuthentication

    // =========================================================
    // 🛡️ AXON SECURITY
    // =========================================================

    private lateinit var axonSecurityManager:
            AxonSecurityManager

    private lateinit var highRiskConfirmationManager:
            HighRiskConfirmationManager

    // =========================================================
    // 🎨 UI
    // =========================================================

    private lateinit var messageText:
            TextView

    private lateinit var statusText:
            TextView

    // =========================================================
    // 🔊 TEXT TO SPEECH
    // =========================================================

    private lateinit var textToSpeech:
            TextToSpeech

    // =========================================================
    // 🛠️ AXON TOOL SYSTEM
    // =========================================================

    private lateinit var toolRegistry:
            AxonToolRegistry

    private lateinit var toolRouter:
            AxonToolRouter

    private lateinit var requestProcessor:
            RequestProcessor

    private lateinit var axonOrchestrator:
            AxonOrchestrator
            
    private val actionPlanner =
    ActionPlanner()        

    // =========================================================
    // 🧠 AXON MEMORY
    // =========================================================

    private lateinit var memoryManager:
            MemoryManager

    private val memoryCommandDetector =
        MemoryCommandDetector()

    private val memoryQueryDetector =
        MemoryQueryDetector()

    // =========================================================
    // 🔐 PERMISSIONS
    // =========================================================

    private val microphonePermissionCode =
        100

    private val notificationPermissionCode =
        200

    // =========================================================
    // ☁️ CLOUD AI WORKER
    // =========================================================

    private val workerUrl =
        "https://axon-backend-v14.itz-pratik1947.workers.dev/"

    // =========================================================
    // 🔐 PENDING SECURE COMMAND
    // =========================================================

    private var pendingCommand:
            String? = null

    // =========================================================
    // 🎙️ WAKE / COMMAND MODE
    // =========================================================
    //
    // TRUE:
    // AXON waits for the wake word.
    //
    // FALSE:
    // AXON accepts commands.
    //
    // =========================================================

    private var waitingForWakeWord =
        true

    // =========================================================
    // 🎙️ LISTENING STATE
    // =========================================================

    private var axonIsListening =
        false

    // =========================================================
    // 🔊 SPEAKING STATE
    // =========================================================

    private var axonIsSpeaking =
        false

    // =========================================================
    // 🔐 HIGH-RISK CONFIRMATION
    // =========================================================

    private var waitingForHighRiskConfirmation =
        false

    // =========================================================
    // ⏱️ COMMAND MODE TIMEOUT
    // =========================================================

    private val commandModeTimeout =
        Handler(
            Looper.getMainLooper()
        )

    private val commandModeTimeoutRunnable =
        Runnable {

            if (
                !axonIsSpeaking &&
                !waitingForWakeWord
            ) {

                waitingForWakeWord =
                    true

                statusText.text =
                    "LISTENING..."

                messageText.text =
                    "Waiting for AXON..."
            }
        }

    // =========================================================
    // ⏱️ HIGH-RISK CONFIRMATION TIMEOUT
    // =========================================================

    private val highRiskConfirmationTimeout =
        Handler(
            Looper.getMainLooper()
        )

    private val highRiskConfirmationTimeoutRunnable =
        Runnable {

            if (
                waitingForHighRiskConfirmation
            ) {

                waitingForHighRiskConfirmation =
                    false

                pendingCommand =
                    null

                statusText.text =
                    "LISTENING..."

                messageText.text =
                    "Confirmation timed out."

                scheduleListeningRestart()
            }
        }

    // =========================================================
    // 🚀 ON CREATE
    // =========================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_main
        )

        // =====================================================
        // 🔐 AUTHENTICATION
        // =====================================================

        axonAuthentication =
            AxonAuthentication(
                this
            )

        // =====================================================
        // 🛡️ SECURITY MANAGER
        // =====================================================

        axonSecurityManager =
            AxonSecurityManager(
                AndroidAuthorizationPolicy(
                    AndroidPermissionChecker(this)
                )
            )

        highRiskConfirmationManager =
            HighRiskConfirmationManager()

        // =====================================================
        // 🛠️ AXON TOOL SYSTEM
        // =====================================================

        toolRegistry =
            AxonToolRegistry()

        toolRegistry.registerTool(
            FlashlightTool(this)
        )

        toolRegistry.registerTool(
            VolumeTool(this)
        )

        toolRegistry.registerTool(
            BrightnessTool(this)
        )

        toolRegistry.registerTool(
            CalculatorTool()
        )

        toolRegistry.registerTool(
            DeviceInfoTool(this)
        )

        toolRegistry.registerTool(
            WifiTool(this)
        )

        toolRegistry.registerTool(
            AndroidActionTool(this)
        )

        toolRegistry.registerTool(
            CommunicationTool(this)
        )

        toolRegistry.registerTool(
            WebsiteTool(this)
        )

        toolRegistry.registerTool(
            AppLauncherTool(this)
        )
        
        toolRegistry.registerTool(
            WikipediaTool()
        )
        
        
        toolRegistry.registerTool(
            PersonalAssistantTool(this)
        )

        

        // =====================================================
        // 🧭 TOOL ROUTER
        // =====================================================

        toolRouter =
            AxonToolRouter(
                registry = toolRegistry,
                authorization = axonSecurityManager
            )

        // =====================================================
        // 🧠 REQUEST PROCESSOR
        // =====================================================

        requestProcessor =
            RequestProcessor(
                toolRouter = toolRouter,
                builtInIntentHandler =
                    BuiltInIntentHandler(
                        com.pratik.axon.core.engine.AxonIntentEngine()
                    )
            )

        // =====================================================
        // 🧠 AXON ORCHESTRATOR
        // =====================================================

        axonOrchestrator =
            AxonOrchestrator(
                requestProcessor = requestProcessor,
                responseComposer = ResponseComposer()
            )

        // =====================================================
        // 🧠 MEMORY SYSTEM
        // =====================================================

        val memoryDatabase =
            MemoryDatabase.getDatabase(
                this
            )

        val memoryRepository =
            MemoryRepository(
                memoryDatabase.memoryDao()
            )

        memoryManager =
            MemoryManager(
                memoryRepository
            )

        // =====================================================
        // 🎨 UI
        // =====================================================

        messageText =
            findViewById(
                R.id.axonMessage
            )

        statusText =
            findViewById(
                R.id.axonStatus
            )

        // =====================================================
        // 🔊 TEXT TO SPEECH
        // =====================================================

        textToSpeech =
            TextToSpeech(
                this
            ) { status ->

                if (
                    status ==
                    TextToSpeech.SUCCESS
                ) {

                    Handler(
                        Looper.getMainLooper()
                    ).post {

                        if (
                            ::textToSpeech.isInitialized
                        ) {

                            textToSpeech.language =
                                Locale(
                                    "en",
                                    "IN"
                                )

                            textToSpeech.setSpeechRate(
                                0.95f
                            )
                        }
                    }
                }
            }

        // =====================================================
        // 🔊 TTS COMPLETION LISTENER
        // =====================================================

        textToSpeech.setOnUtteranceProgressListener(
            object :
                UtteranceProgressListener() {

                override fun onStart(
                    utteranceId: String?
                ) {

                    axonIsSpeaking =
                        true

                    cancelScheduledListening()

                    runOnUiThread {

                        if (
                            !isFinishing &&
                            !isDestroyed
                        ) {

                            statusText.text =
                                "SPEAKING..."
                        }
                    }
                }

                override fun onDone(
                    utteranceId: String?
                ) {

                    axonIsSpeaking =
                        false

                    runOnUiThread {

                        if (
                            !isFinishing &&
                            !isDestroyed
                        ) {

                            statusText.text =
                                "LISTENING..."

                            if (
                                !waitingForWakeWord
                            ) {

                                resetCommandModeTimeout()
                            }

                            scheduleListeningRestart()
                        }
                    }
                }

                override fun onError(
                    utteranceId: String?
                ) {

                    axonIsSpeaking =
                        false

                    runOnUiThread {

                        if (
                            !isFinishing &&
                            !isDestroyed
                        ) {

                            statusText.text =
                                "SYSTEM ONLINE"

                            if (
                                !waitingForWakeWord
                            ) {

                                resetCommandModeTimeout()
                            }

                            scheduleListeningRestart()
                        }
                    }
                }
            }
        )

        // =====================================================
        // 🎙️ SPEECH RECOGNIZER
        // =====================================================

        createSpeechRecognizer()

        // =====================================================
        // 🔐 MICROPHONE PERMISSION
        // =====================================================

        requestMicrophonePermission()

        // =====================================================
        // 🎙️ START LISTENING IF ALREADY PERMITTED
        // =====================================================

        if (
            checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {

            waitingForWakeWord =
                true

            scheduleListeningRestart()
        }
    }

    // =========================================================
    // 🎙️ CREATE SPEECH RECOGNIZER
    // =========================================================

    private fun createSpeechRecognizer() {

        if (
            !SpeechRecognizer
                .isRecognitionAvailable(this)
        ) {

            statusText.text =
                "Speech recognition unavailable."

            return
        }

        speechRecognizer =
            SpeechRecognizer
                .createSpeechRecognizer(
                    this
                )

        speechRecognizer.setRecognitionListener(
            object :
                RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {

                    recognizerStartPending =
                        false

                    axonIsListening =
                        true

                    statusText.text =
                        "LISTENING..."
                }

                override fun onBeginningOfSpeech() {

                    axonIsListening =
                        true

                    statusText.text =
                        "HEARING YOU..."
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

                    statusText.text =
                        "THINKING..."
                }

                override fun onError(
                    error: Int
                ) {

                    axonIsListening =
                        false

                    recognizerStartPending =
                        false

                    when (error) {

                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {

                            statusText.text =
                                "LISTENING..."

                            scheduleListeningRestart()
                        }

                        SpeechRecognizer.ERROR_AUDIO -> {

                            statusText.text =
                                "SYSTEM ONLINE"

                            messageText.text =
                                "Audio recording error."

                            scheduleListeningRestart()
                        }

                        SpeechRecognizer.ERROR_CLIENT -> {

                            statusText.text =
                                "SYSTEM ONLINE"

                            messageText.text =
                                "Speech recognizer client error."

                            recreateSpeechRecognizer()
                        }

                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {

                            statusText.text =
                                "MICROPHONE PERMISSION DENIED"

                            messageText.text =
                                "Microphone permission is required."
                        }

                        SpeechRecognizer.ERROR_NETWORK -> {

                            statusText.text =
                                "SYSTEM ONLINE"

                            messageText.text =
                                "Network error."

                            scheduleListeningRestart()
                        }

                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {

                            statusText.text =
                                "SYSTEM ONLINE"

                            messageText.text =
                                "Speech recognition network timeout."

                            scheduleListeningRestart()
                        }

                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {

                            statusText.text =
                                "SYSTEM ONLINE"

                            messageText.text =
                                "Speech recognizer is busy."

                            recreateSpeechRecognizer()
                        }

                        SpeechRecognizer.ERROR_SERVER -> {

                            statusText.text =
                                "SYSTEM ONLINE"

                            messageText.text =
                                "Speech recognition server error."

                            scheduleListeningRestart()
                        }

                        else -> {

                            statusText.text =
                                "SYSTEM ONLINE"

                            messageText.text =
                                "Speech error: $error"

                            scheduleListeningRestart()
                        }
                    }
                }

                override fun onResults(
                    results: Bundle?
                ) {

                    axonIsListening =
                        false

                    recognizerStartPending =
                        false

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    if (
                        matches.isNullOrEmpty()
                    ) {

                        statusText.text =
                            "LISTENING..."

                        scheduleListeningRestart()

                        return
                    }

                    val spokenText =
                        matches[0]
                            .trim()

                    if (
                        spokenText.isEmpty()
                    ) {

                        scheduleListeningRestart()

                        return
                    }

                    // =================================================
                    // 🔵 COMMAND MODE
                    // =================================================

                    if (
                        !waitingForWakeWord
                    ) {

                        val normalizedText =
                            speechNormalizer.normalize(
                                spokenText
                            )

                        messageText.text =
                            normalizedText

                        // =================================================
                        // 🔐 HIGH-RISK CONFIRMATION MODE
                        // =================================================

                        if (
                            waitingForHighRiskConfirmation
                        ) {

                            if (
                                highRiskConfirmationManager
                                    .isConfirmation(
                                        normalizedText
                                    )
                            ) {

                                val command =
                                    pendingCommand

                                waitingForHighRiskConfirmation =
                                    false

                                highRiskConfirmationTimeout
                                    .removeCallbacks(
                                        highRiskConfirmationTimeoutRunnable
                                    )

                                pendingCommand =
                                    null

                                if (
                                    command != null
                                ) {

                                    executeAxonCommand(
                                        command,
                                        confirmationGranted = true
                                    )
                                }

                                return
                            }

                            if (
                                highRiskConfirmationManager
                                    .isCancellation(
                                        normalizedText
                                    )
                            ) {

                                waitingForHighRiskConfirmation =
                                    false

                                highRiskConfirmationTimeout
                                    .removeCallbacks(
                                        highRiskConfirmationTimeoutRunnable
                                    )

                                pendingCommand =
                                    null

                                messageText.text =
                                    "Action cancelled."

                                axonIsSpeaking =
                                    true

                                textToSpeech.speak(
                                    cleanTextForSpeech(
                                        "Action cancelled."
                                    ),
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "AXON_HIGH_RISK_CANCELLED"
                                )

                                return
                            }

                            messageText.text =
                                "Please say yes to confirm or no to cancel."

                            axonIsSpeaking =
                                true

                            textToSpeech.speak(
                                cleanTextForSpeech(
                                    "Please say yes to confirm or no to cancel."
                                ),
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "AXON_HIGH_RISK_REPEAT"
                            )

                            return
                        }

                        // =================================================
                        // 🔵 NORMAL COMMAND MODE
                        // =================================================

                        resetCommandModeTimeout()

                        processAxonCommand(
                            normalizedText
                        )

                        return
                    }

                    // =================================================
                    // 🟢 WAKE-WORD MODE
                    // =================================================

                    if (
                        isAxonWakeWord(
                            spokenText
                        )
                    ) {

                        startFollowUpCommandMode()

                        return
                    }

                    // =================================================
                    // 🚫 NOT THE WAKE WORD
                    // =================================================

                    scheduleListeningRestart()
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
    }

    // =========================================================
    // ⏱️ RESET COMMAND MODE TIMEOUT
    // =========================================================

    private fun resetCommandModeTimeout() {

        commandModeTimeout.removeCallbacks(
            commandModeTimeoutRunnable
        )

        commandModeTimeout.postDelayed(
            commandModeTimeoutRunnable,
            15000L
        )
    }

    // =========================================================
    // 🎙️ SCHEDULE LISTENING RESTART
    // =========================================================

    private fun scheduleListeningRestart() {

        if (
            isFinishing ||
            isDestroyed ||
            axonIsSpeaking ||
            axonIsListening
        ) {

            return
        }

        if (
            !::speechRecognizer.isInitialized
        ) {

            return
        }

        if (
            checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        if (
            recognizerStartPending
        ) {

            return
        }

        recognizerStartPending =
            true

        speechRestartHandler.removeCallbacks(
            speechRestartRunnable
        )

        speechRestartHandler.postDelayed(
            speechRestartRunnable,
            300L
        )
    }

    // =========================================================
    // 🛑 CANCEL SCHEDULED LISTENING
    // =========================================================

    private fun cancelScheduledListening() {

        recognizerStartPending =
            false

        speechRestartHandler.removeCallbacks(
            speechRestartRunnable
        )
    }

    // =========================================================
    // 🔄 RECREATE SPEECH RECOGNIZER
    // =========================================================

    private fun recreateSpeechRecognizer() {

        cancelScheduledListening()

        axonIsListening =
            false

        if (
            ::speechRecognizer.isInitialized
        ) {

            try {
                speechRecognizer.stopListening()
            } catch (_: Exception) {
            }

            try {
                speechRecognizer.destroy()
            } catch (_: Exception) {
            }
        }

        if (
            isFinishing ||
            isDestroyed
        ) {

            return
        }

        createSpeechRecognizer()

        scheduleListeningRestart()
    }

    // =========================================================
    // 🎙️ START FOLLOW-UP COMMAND MODE
    // =========================================================

    private fun startFollowUpCommandMode() {

        waitingForWakeWord =
            false

        commandModeTimeout.removeCallbacks(
            commandModeTimeoutRunnable
        )

        cancelScheduledListening()

        if (
            axonIsListening &&
            ::speechRecognizer.isInitialized
        ) {

            try {
                speechRecognizer.stopListening()
            } catch (_: Exception) {
            }

            axonIsListening =
                false
        }

        axonIsSpeaking =
            true

        statusText.text =
            "SPEAKING..."

        messageText.text =
            "Yes Master."

        textToSpeech.speak(
            cleanTextForSpeech(
                "Yes Master."
            ),
            TextToSpeech.QUEUE_FLUSH,
            null,
            "AXON_WAKE_RESPONSE"
        )
    }

    // =========================================================
    // 🧠 PROCESS AXON COMMAND
    // =========================================================

    private fun processAxonCommand(
        commandText: String
    ) {

        // =====================================================
        // 🧠 EXPLICIT MEMORY COMMAND
        // =====================================================

        if (
            memoryCommandDetector
                .isMemoryCommand(
                    commandText
                )
        ) {

            Thread {

                val savedMemory =
                    memoryManager
                        .processMemoryCommand(
                            commandText
                        )

                runOnUiThread {

                    if (
                        savedMemory != null
                    ) {

                        messageText.text =
                            "Memory saved."

                        axonIsSpeaking =
                            true

                        textToSpeech.speak(
                            cleanTextForSpeech(
                                "I will remember that."
                            ),
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "AXON_MEMORY_SAVED"
                        )

                    } else {

                        messageText.text =
                            "I couldn't understand what to remember."

                        axonIsSpeaking =
                            true

                        textToSpeech.speak(
                            cleanTextForSpeech(
                                "I couldn't understand what to remember."
                            ),
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "AXON_MEMORY_NOT_UNDERSTOOD"
                        )
                    }
                }

            }.start()

            return
        }

        // =====================================================
        // 🧠 MEMORY RETRIEVAL
        // =====================================================

        if (
            memoryQueryDetector
                .isMemoryQuery(
                    commandText
                )
        ) {

            val memoryKey =
                memoryQueryDetector
                    .getMemoryKey(
                        commandText
                    )

            if (
                memoryKey == null
            ) {

                messageText.text =
                    "I couldn't understand which memory you want."

                axonIsSpeaking =
                    true

                textToSpeech.speak(
                    cleanTextForSpeech(
                        "I couldn't understand which memory you want."
                    ),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "AXON_MEMORY_QUERY_ERROR"
                )

                return
            }

            Thread {

                val memory =
                    memoryManager
                        .findMemory(
                            memoryKey
                        )

                runOnUiThread {

                    if (
                        memory != null
                    ) {

                        val response =
                            when (
                                memory.key
                            ) {

                                "favorite_programming_language" ->
                                    "Your favorite programming language is ${memory.value}."

                                "main_project" ->
                                    "Your main project is ${memory.value}."

                                else ->
                                    "I remember ${memory.value}."
                            }

                        messageText.text =
                            response

                        axonIsSpeaking =
                            true

                        textToSpeech.speak(
                            cleanTextForSpeech(
                                response
                            ),
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "AXON_MEMORY_RETRIEVED"
                        )

                    } else {

                        messageText.text =
                            "I don't remember that yet."

                        axonIsSpeaking =
                            true

                        textToSpeech.speak(
                            cleanTextForSpeech(
                                "I don't remember that yet."
                            ),
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "AXON_MEMORY_NOT_FOUND"
                        )
                    }
                }

            }.start()

            return
        }

        // =====================================================
        // ❌ BLANK COMMAND
        // =====================================================

        if (
            commandText.isBlank()
        ) {

            statusText.text =
                "LISTENING..."

            scheduleListeningRestart()

            return
        }

        // =====================================================
        // 🛡️ SECURITY CHECK
        // =====================================================

        val capability =
            toolRouter
                .getCapabilityForCommand(
                    commandText
                )

        val authorization =
            if (
                capability != null
            ) {

                axonSecurityManager
                    .authorizeCapability(
                        command = commandText,
                        capabilityRiskLevel =
                            capability.riskLevel,
                        permissions =
                            capability.requiredPermissions
                    )

            } else {

                axonSecurityManager
                    .authorize(
                        commandText
                    )
            }

        if (
            !authorization.allowed &&
            !authorization.requiresConfirmation
        ) {

            messageText.text =
                authorization.reason.ifBlank {
                    "AXON is not authorized to perform this action."
                }

            statusText.text =
                "ACTION BLOCKED"

            scheduleListeningRestart()

            return
        }

        val riskLevel =
            authorization.riskLevel

        // =====================================================
        // 🟢 LOW RISK
        // =====================================================

        if (
            riskLevel ==
            RiskLevel.LOW
        ) {

            executeAxonCommand(
                commandText
            )

            return
        }

        // =====================================================
        // 🔐 PRIVATE / HIGH RISK
        // =====================================================

        pendingCommand =
            commandText

        statusText.text =
            "AUTHENTICATION REQUIRED"

        axonAuthentication.authenticate(

            onSuccess = {

                val command =
                    pendingCommand

                if (
                    command == null
                ) {

                    return@authenticate
                }

                // =================================================
                // 🔴 HIGH-RISK CONFIRMATION
                // =================================================

                if (
                    riskLevel ==
                    RiskLevel.HIGH
                ) {

                    waitingForHighRiskConfirmation =
                        true

                    highRiskConfirmationTimeout
                        .removeCallbacks(
                            highRiskConfirmationTimeoutRunnable
                        )

                    highRiskConfirmationTimeout
                        .postDelayed(
                            highRiskConfirmationTimeoutRunnable,
                            15000L
                        )

                    statusText.text =
                        "CONFIRMATION REQUIRED"

                    messageText.text =
                        highRiskConfirmationManager
                            .getConfirmationMessage(
                                command
                            )

                    axonIsSpeaking =
                        true

                    textToSpeech.speak(
                        cleanTextForSpeech(
                            highRiskConfirmationManager
                                .getConfirmationMessage(
                                    command
                                )
                        ),
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "AXON_HIGH_RISK_CONFIRMATION"
                    )

                    return@authenticate
                }

                // =================================================
                // 🟠 PRIVATE COMMAND
                // =================================================

                pendingCommand =
                    null

                executeAxonCommand(
                    command
                )
            },

            onFailure = {

                pendingCommand =
                    null

                waitingForHighRiskConfirmation =
                    false

                messageText.text =
                    "Authentication failed."

                statusText.text =
                    "LISTENING..."

                waitingForWakeWord =
                    true

                commandModeTimeout
                    .removeCallbacks(
                        commandModeTimeoutRunnable
                    )

                scheduleListeningRestart()
            }
        )
    }

    // =========================================================
    // 🧠 EXECUTE AXON COMMAND
    // =========================================================
    //
    // IMPORTANT:
    //
    // MainActivity no longer decides whether a command is:
    // - greeting
    // - time
    // - date
    // - identity
    //
    // It sends the command into AXON's core engine.
    //
    // =========================================================

    

private fun executeAxonCommand(
    command: String,
    confirmationGranted: Boolean = false
) {
    cancelScheduledListening()

    messageText.text = command
    statusText.text = "THINKING..."

    val request =
        com.pratik.axon.core.model.AxonRequest(
            text = command,
            confirmationGranted = confirmationGranted
        )

    // Detect explicit multi-step commands first.
    val plannedSteps =
        actionPlanner.createPlan(command)

    if (plannedSteps.isNotEmpty()) {
        // Validate the complete plan before running any step.
        val validationError =
            actionPlanner.validatePlan(
                plannedSteps,
                toolRegistry
            )

        if (validationError != null) {
            messageText.text = validationError
            statusText.text = "PLAN NOT EXECUTED"
            axonIsSpeaking = true

            textToSpeech.speak(
                cleanTextForSpeech(validationError),
                TextToSpeech.QUEUE_FLUSH,
                null,
                "AXON_PLAN_REJECTED"
            )

            return
        }

        // Run network-backed tasks away from the UI thread.
        Thread {
            val result =
    actionPlanner.executePlan(
        plannedSteps,
        toolRegistry,
        axonOrchestrator,
        confirmationGranted = confirmationGranted
    )

            runOnUiThread {
                if (isFinishing || isDestroyed) {
                    return@runOnUiThread
                }

                messageText.text = result.message

                statusText.text =
                    if (result.success) {
                        "TASK COMPLETED"
                    } else {
                        "TASK NEEDS ATTENTION"
                    }

                axonIsSpeaking = true

                textToSpeech.speak(
                    cleanTextForSpeech(result.message),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "AXON_PLAN_RESPONSE"
                )
            }
        }.start()

        return
    }

    // Preserve the existing Wikipedia background execution path.
    val isWikipediaRequest =
        toolRegistry.findTool(command) is WikipediaTool

    if (isWikipediaRequest) {
        Thread {
            val response =
                try {
                    axonOrchestrator
                        .process(request)
                        .message
                } catch (exception: Exception) {
                    "I couldn't access Wikipedia right now. " +
                        "Please check your internet connection and try again."
                }

            runOnUiThread {
                if (isFinishing || isDestroyed) {
                    return@runOnUiThread
                }

                messageText.text = response
                statusText.text = "SPEAKING..."
                axonIsSpeaking = true

                textToSpeech.speak(
                    cleanTextForSpeech(response),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "AXON_WIKIPEDIA_RESPONSE"
                )
            }
        }.start()

        return
    }

    // Keep the existing local-command path for single commands.
    val response =
        axonOrchestrator
            .process(request)
            .message

    messageText.text = response
    axonIsSpeaking = true
    statusText.text = "SPEAKING..."

    textToSpeech.speak(
        cleanTextForSpeech(response),
        TextToSpeech.QUEUE_FLUSH,
        null,
        "AXON_LOCAL_RESPONSE"
    )
}



    // =========================================================
    // 🔐 MICROPHONE PERMISSION
    // =========================================================

    private fun requestMicrophonePermission() {

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.M
        ) {

            if (
                checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {

                requestPermissions(
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO
                    ),
                    microphonePermissionCode
                )
            }
        }
    }

    // =========================================================
    // 🔐 PERMISSION RESULT
    // =========================================================

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        // =====================================================
        // 🎙️ MICROPHONE
        // =====================================================

        if (
            requestCode ==
            microphonePermissionCode
        ) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {

                waitingForWakeWord =
                    true

                statusText.text =
                    "LISTENING..."

                scheduleListeningRestart()

            } else {

                axonIsListening =
                    false

                recognizerStartPending =
                    false

                waitingForWakeWord =
                    true

                statusText.text =
                    "MICROPHONE PERMISSION DENIED"

                messageText.text =
                    "Microphone permission is required."
            }

            return
        }

        // =====================================================
        // 🔔 NOTIFICATION
        // =====================================================

        if (
            requestCode ==
            notificationPermissionCode
        ) {
            // Nothing required currently.
        }
    }

    // =========================================================
    // 🚫 WAKE SERVICE
    // =========================================================
    //
    // Kept for future use.
    //
    // NOT automatically started.
    //
    // =========================================================

    private fun startAxonWakeService() {

        if (
            checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        val intent =
            Intent(
                this,
                AxonWakeService::class.java
            )

        startForegroundService(
            intent
        )
    }

    // =========================================================
    // 🎙️ START LISTENING
    // =========================================================

    private fun startListening() {

        if (
            isFinishing ||
            isDestroyed
        ) {

            return
        }

        if (
            axonIsSpeaking
        ) {

            return
        }

        if (
            axonIsListening
        ) {

            return
        }

        if (
            recognizerStartPending
        ) {

            return
        }

        if (
            !::speechRecognizer.isInitialized
        ) {

            return
        }

        if (
            checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                microphonePermissionCode
            )

            return
        }

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
            1
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PARTIAL_RESULTS,
            false
        )

        recognizerStartPending =
            true

        try {

            speechRecognizer.startListening(
                intent
            )

        } catch (
            _: Exception
        ) {

            recognizerStartPending =
                false

            axonIsListening =
                false

            scheduleListeningRestart()
        }
    }

    // =========================================================
    // 🔔 NOTIFICATION PERMISSION
    // =========================================================

    private fun requestNotificationPermission() {

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {

                requestPermissions(
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    notificationPermissionCode
                )
            }
        }
    }

    // =========================================================
    // ☁️ CLOUD AI
    // =========================================================

    private fun askAxon(
        message: String
    ) {

        Thread {

            try {

                val url =
                    URL(workerUrl)

                val connection =
                    url.openConnection()
                        as HttpURLConnection

                connection.requestMethod =
                    "POST"

                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                connection.doOutput =
                    true

                val json =
                    JSONObject()

                json.put(
                    "message",
                    message
                )

                val requestBody =
                    json.toString()
                        .toByteArray(
                            Charsets.UTF_8
                        )

                connection.outputStream.use { output ->

                    output.write(
                        requestBody
                    )
                }

                val responseCode =
                    connection.responseCode

                val responseStream =
                    if (
                        responseCode in 200..299
                    ) {

                        connection.inputStream

                    } else {

                        connection.errorStream
                    }

                val responseText =
                    responseStream
                        .bufferedReader()
                        .use {
                            it.readText()
                        }

                connection.disconnect()

                val responseJson =
                    JSONObject(
                        responseText
                    )

                if (
                    responseJson.has(
                        "answer"
                    )
                ) {

                    val answer =
                        responseJson.getString(
                            "answer"
                        )

                    runOnUiThread {

                        messageText.text =
                            answer

                        axonIsSpeaking =
                            true

                        statusText.text =
                            "SPEAKING..."

                        textToSpeech.speak(
                            cleanTextForSpeech(
                                answer
                            ),
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "AXON_RESPONSE"
                        )
                    }

                } else {

                    runOnUiThread {

                        messageText.text =
                            "AXON couldn't generate a response."

                        statusText.text =
                            "LISTENING..."

                        scheduleListeningRestart()
                    }
                }

            } catch (
                e: Exception
            ) {

                runOnUiThread {

                    messageText.text =
                        "Connection error."

                    statusText.text =
                        "LISTENING..."

                    scheduleListeningRestart()
                }
            }

        }.start()
    }

    // =========================================================
    // 🗣️ CHECK AXON WAKE WORD
    // =========================================================

    
private fun isAxonWakeWord(
    spokenText: String
): Boolean {

    val normalized = spokenText
        .lowercase(Locale.ROOT)
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    val exactWakeWords = setOf(
        "axon",
        "hey axon",
        "hello axon",
        "hi axon",
        "hey hello axon",
        "hello axen",
        "hello axson",
        "hello ackson",
        "hello axe on",
        "hello ax on",
        "hello action",
        "hello exam",
        "hey action",
        "hey exam",
        "hey axen",
        "hey axson",
        "hey ax on",
        "hey axe on",
        "ackson",
        "axson",
        "axen",
        "ax on",
        "axe on"
    )

    if (normalized in exactWakeWords) {
        return true
    }

    // Handle phrases where the recognizer adds extra words.
    val words = normalized.split(" ")

    val hasGreeting = words.any {
        it in setOf("hello", "hey", "hi")
    }

    val hasAxonVariant = words.any {
        it in setOf(
            "axon",
            "axen",
            "axson",
            "ackson",
            "action",
            "exam"
        )
    }

    return hasAxonVariant &&
        (hasGreeting || words.size <= 2)
}


    // =========================================================
    // 🔊 CLEAN TEXT FOR TTS
    // =========================================================

    private fun cleanTextForSpeech(
        text: String
    ): String {

        var cleaned =
            text

        cleaned =
            cleaned.replace(
                Regex(
                    "(?m)^\\s*#{1,6}\\s*"
                ),
                ""
            )

        cleaned =
            cleaned.replace(
                Regex(
                    "\\[([^\\]]+)\\]\\([^\\)]+\\)"
                ),
                "$1"
            )

        cleaned =
            cleaned.replace(
                "**",
                ""
            )

        cleaned =
            cleaned.replace(
                "*",
                ""
            )

        cleaned =
            cleaned.replace(
                "`",
                ""
            )

        cleaned =
            cleaned.replace(
                Regex(
                    "(?m)^\\s*[-•]\\s+"
                ),
                ""
            )

        cleaned =
            cleaned.replace(
                Regex(
                    "\\n{3,}"
                ),
                "\n\n"
            )

        return cleaned.trim()
    }

    // =========================================================
    // 🧹 CLEANUP
    // =========================================================

    override fun onDestroy() {

        // =====================================================
        // ⏱️ CANCEL TIMEOUTS
        // =====================================================

        commandModeTimeout.removeCallbacks(
            commandModeTimeoutRunnable
        )

        highRiskConfirmationTimeout.removeCallbacks(
            highRiskConfirmationTimeoutRunnable
        )

        cancelScheduledListening()

        // =====================================================
        // 🎙️ RESET STATES
        // =====================================================

        axonIsListening =
            false

        waitingForWakeWord =
            true

        pendingCommand =
            null

        waitingForHighRiskConfirmation =
            false

        axonIsSpeaking =
            false

        // =====================================================
        // 🎙️ SPEECH CLEANUP
        // =====================================================

        if (
            ::speechRecognizer.isInitialized
        ) {

            try {
                speechRecognizer.stopListening()
            } catch (_: Exception) {
            }

            try {
                speechRecognizer.destroy()
            } catch (_: Exception) {
            }
        }

        // =====================================================
        // 🔊 TTS CLEANUP
        // =====================================================

        if (
            ::textToSpeech.isInitialized
        ) {

            textToSpeech.stop()
            textToSpeech.shutdown()
        }

        super.onDestroy()
    }
}