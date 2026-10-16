
package com.pratik.axon.features.wikipedia

import com.pratik.axon.core.capability.AxonTool
import com.pratik.axon.core.capability.Capability
import com.pratik.axon.core.model.AxonError
import com.pratik.axon.core.model.AxonResult
import com.pratik.axon.core.security.RiskLevel
import com.pratik.axon.providers.web.WikipediaProvider
import java.util.Locale

class WikipediaTool(
    private val wikipediaProvider: WikipediaProvider =
        WikipediaProvider()
) : AxonTool {

    override fun getName(): String {
        return "Wikipedia"
    }

    override fun getRiskLevel(): RiskLevel {
        return RiskLevel.LOW
    }

    override fun getCapability(): Capability {
        return Capability(
            id = getName(),
            name = getName(),
            description =
                "Search Wikipedia and retrieve article summaries.",
            actions = listOf(
                "search_wikipedia",
                "read_wikipedia_summary"
            ),
            requiredPermissions = emptyList(),
            riskLevel = RiskLevel.LOW
        )
    }

    override fun canHandle(
        command: String
    ): Boolean {

        val text =
            command.lowercase(Locale.ROOT)
                .trim()
                .replace(Regex("[.!?]+$"), "")

        if (text.isBlank()) {
            return false
        }

        // Explicit Wikipedia requests.
        if (
            text.contains("wikipedia") ||
            text.contains("wiki pedia")
        ) {
            return true
        }

        // General knowledge questions. This tool is registered
        // after the existing device and calculator tools.
        val knowledgePrefixes = listOf(
            "who is ",
            "who was ",
            "who were ",
            "what is ",
            "what are ",
            "what was ",
            "what were ",
            "tell me about ",
            "explain ",
            "give me information about ",
            "give me information on "
        )

        val isKnowledgeQuestion =
            knowledgePrefixes.any {
                text.startsWith(it)
            }

        if (!isKnowledgeQuestion) {
            return false
        }

        // Let AXON's own identity and greeting handler handle
        // questions about AXON itself.
        if (
            text.contains("who are you") ||
            text.contains("what are you") ||
            text.contains("what is axon") ||
            text.contains("who is axon")
        ) {
            return false
        }

        return true
    }

    override fun execute(
        command: String
    ): AxonResult {

        val topic = extractTopic(command)

        if (topic.isBlank()) {
            return AxonResult(
                success = false,
                message =
                    "Please tell me which topic to search on Wikipedia.",
                error = AxonError(
                    code = "WIKIPEDIA_TOPIC_MISSING",
                    message = "No search topic was provided."
                )
            )
        }

        return try {

            val article =
                wikipediaProvider.findArticle(topic)

            if (article == null) {
                AxonResult(
                    success = false,
                    message =
                        "I couldn't find a suitable Wikipedia article about $topic.",
                    error = AxonError(
                        code = "WIKIPEDIA_ARTICLE_NOT_FOUND",
                        message =
                            "No usable Wikipedia summary was found."
                    )
                )
            } else {

                val summary =
                    shortenForSpeech(article.extract)

                val description =
                    article.description
                        ?.takeIf { it.isNotBlank() }
                        ?.let { "$it. " }
                        ?: ""

                val response =
                    "${article.title}. $description$summary"

                AxonResult(
                    success = true,
                    message = response,
                    data = mapOf(
                        "source" to "Wikipedia",
                        "title" to article.title,
                        "summary" to article.extract,
                        "url" to article.pageUrl
                    )
                )
            }

        } catch (exception: Exception) {

            AxonResult(
                success = false,
                message =
                    "I couldn't access Wikipedia right now. Please check your internet connection and try again.",
                error = AxonError(
                    code = "WIKIPEDIA_REQUEST_FAILED",
                    message =
                        exception.message
                            ?: "An unknown Wikipedia request error occurred."
                )
            )
        }
    }

    private fun extractTopic(
        command: String
    ): String {

        var text = command.trim()

        // Remove common request prefixes.
        val prefixes = listOf(
            "search wikipedia for ",
            "search wikipedia ",
            "look up on wikipedia ",
            "look up ",
            "find on wikipedia ",
            "find wikipedia information about ",
            "what does wikipedia say about ",
            "what does wikipedia say on ",
            "what does wikipedia say about ",
            "tell me about ",
            "give me information about ",
            "give me information on ",
            "search for ",
            "search ",
            "explain ",
            "who is ",
            "who was ",
            "who were ",
            "what is ",
            "what are ",
            "what was ",
            "what were "
        )

        val lowerText =
            text.lowercase(Locale.ROOT)

        for (prefix in prefixes) {
            if (lowerText.startsWith(prefix)) {
                text =
                    text.substring(prefix.length).trim()
                break
            }
        }

        // Remove trailing Wikipedia wording.
        val suffixes = listOf(
            " on wikipedia",
            " from wikipedia",
            " wikipedia"
        )

        val lowerTopic =
            text.lowercase(Locale.ROOT)

        for (suffix in suffixes) {
            if (lowerTopic.endsWith(suffix)) {
                text =
                    text.dropLast(suffix.length).trim()
                break
            }
        }

        return text
            .trim()
            .trim(',', '.', '?', '!')
    }

    private fun shortenForSpeech(
        text: String,
        maxLength: Int = 700
    ): String {

        val cleaned =
            text.replace(
                Regex("\\s+"),
                " "
            ).trim()

        if (cleaned.length <= maxLength) {
            return cleaned
        }

        val sentenceBoundary =
            cleaned.lastIndexOf(
                ". ",
                maxLength
            )

        val cutPosition =
            if (sentenceBoundary >= maxLength / 2) {
                sentenceBoundary + 1
            } else {
                cleaned.lastIndexOf(' ', maxLength)
                    .takeIf { it > 0 }
                    ?: maxLength
            }

        return cleaned.substring(
            0,
            cutPosition
        ).trim() + " The summary has been shortened."
    }
}
