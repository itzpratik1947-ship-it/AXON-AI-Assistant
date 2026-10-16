
package com.pratik.axon.core.engine

import com.pratik.axon.core.capability.AxonToolRegistry
import com.pratik.axon.core.model.AxonRequest
import com.pratik.axon.core.model.AxonResult
import java.util.Locale

/**
 * Plans and executes multi-command requests for AXON.
 *
 * Tools are discovered dynamically through AxonToolRegistry.
 * Each step is checked before any step is executed.
 * Actual execution still passes through Axon's orchestrator,
 * router, and existing security/authorization system.
 */
class ActionPlanner {

    private val commandStartWords = listOf(
        "calculate",
        "compute",
        "solve",
        "search",
        "search wikipedia",
        "look up",
        "lookup",
        "find",
        "tell me",
        "explain",
        "describe",
        "who is",
        "what is",
        "what are",
        "check",
        "show",
        "get",
        "read",
        "open",
        "launch",
        "start",
        "close",
        "stop",
        "turn on",
        "turn off",
        "switch on",
        "switch off",
        "enable",
        "disable",
        "activate",
        "deactivate",
        "increase",
        "decrease",
        "set",
        "change",
        "adjust",
        "connect",
        "disconnect",
        "play",
        "pause",
        "resume",
        "send",
        "call",
        "message",
        "create",
        "make",
        "visit",
        "browse",
        "navigate",
        "remind me",
        "remember",
        "forget",
        "what time",
        "what date",
        "tell the time"
    )

    /**
     * Splits a spoken request into ordered command steps.
     *
     * Returns an empty list when the request appears to contain
     * only one command, so MainActivity can use its normal path.
     */
    fun createPlan(command: String): List<String> {
        val text = command.trim()

        if (text.isBlank()) {
            return emptyList()
        }

        val starters = commandStartWords
            .sortedByDescending { it.length }
            .joinToString("|") {
                Regex.escape(it)
            }

        val separator = Regex(
            "(?i)\\s+(?:and\\s+then|then)\\s+" +
                "|\\s+and\\s+(?=(?:please\\s+)?(?:$starters)\\b)" +
                "|,\\s*(?=(?:please\\s+)?(?:$starters)\\b)" +
                "|\\s*;\\s*"
        )

        val steps = text
            .split(separator)
            .map { cleanStep(it) }
            .filter { it.isNotBlank() }

        return if (steps.size >= 2) {
            steps
        } else {
            emptyList()
        }
    }

    /**
     * Checks that every planned step has a registered tool.
     *
     * There is no fixed tool-name allowlist. New registered tools
     * can be considered automatically.
     *
     * Security-sensitive actions are still subject to the existing
     * authorization checks during execution.
     */
    fun validatePlan(
        steps: List<String>,
        registry: AxonToolRegistry
    ): String? {
        if (steps.size < 2) {
            return "Please give me at least two clear commands."
        }

        for ((index, step) in steps.withIndex()) {
            val tool = registry.findTool(step)

            if (tool == null) {
                return "I couldn't identify a supported tool for " +
                    "step ${index + 1}: $step. " +
                    "No steps were executed."
            }
        }

        return null
    }

    /**
     * Executes steps sequentially through the existing orchestrator.
     *
     * A failed step is recorded, and later independent steps are
     * still attempted. The orchestrator remains responsible for
     * routing, permissions, and authorization.
     */
    
fun executePlan(
    steps: List<String>,
    registry: AxonToolRegistry,
    orchestrator: AxonOrchestrator,
    confirmationGranted: Boolean = false
): AxonResult {
    val validationError = validatePlan(steps, registry)

    if (validationError != null) {
        return AxonResult(
            success = false,
            message = validationError,
            data = mapOf(
                "plannedSteps" to steps,
                "executedSteps" to 0,
                "allStepsSucceeded" to false
            )
        )
    }

    val spokenResults = mutableListOf<String>()
    var allSucceeded = true
    var executed = 0

    for (step in steps) {
        try {
            val response = orchestrator.process(
                AxonRequest(
                    text = step,
                    confirmationGranted = confirmationGranted
                )
            )

            executed++

            if (!response.success) {
                allSucceeded = false
            }

            val cleanMessage = cleanStep(response.message)

            if (cleanMessage.isNotBlank()) {
                spokenResults.add(cleanMessage)
            }
        } catch (exception: Exception) {
            allSucceeded = false

            spokenResults.add(
                "I couldn't complete: ${cleanStep(step)}."
            )
        }
    }

    val finalMessage = spokenResults
        .distinct()
        .joinToString(". ") { it.trim().trimEnd('.', ' ') }
        .let { if (it.isNotBlank()) "$it." else "Commands processed." }

    return AxonResult(
        success = allSucceeded,
        message = finalMessage,
        data = mapOf(
            "plannedSteps" to steps,
            "executedSteps" to executed,
            "allStepsSucceeded" to allSucceeded
        )
    )
}

private fun cleanStep(message: String): String {
    return message
        .replace(
            Regex(
                "(?i)\\bdebug\\s*:\\s*.*$",
                RegexOption.DOT_MATCHES_ALL
            ),
            ""
        )
        .replace(
            Regex(
                "(?i)\\bI calculated\\s+.*$",
                RegexOption.DOT_MATCHES_ALL
            ),
            ""
        )
        .replace(
            Regex(
                "(?im)^\\s*Step\\s+\\d+\\s*:\\s*",
            ),
            ""
        )
        .replace(
            Regex(
                "(?i)\\bAll\\s+\\d+\\s+commands\\s+completed\\.?\\s*",
            ),
            ""
        )
        .trim()
        .trimEnd('.', ' ')
}

}
