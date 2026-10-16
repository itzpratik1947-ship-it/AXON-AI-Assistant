package com.pratik.axon.core.memory

import java.util.Locale

class MemoryContentParser {

    fun parse(
        content: String
    ): MemoryItem? {

        val text =
            content
                .trim()
                .replace(
                    Regex("\\s+"),
                    " "
                )

        if (text.isEmpty()) {
            return null
        }

        val lower =
            text.lowercase(
                Locale.getDefault()
            )

        // =====================================================
        // ⭐ FAVORITE PROGRAMMING LANGUAGE
        // =====================================================

        if (
            lower.contains(
                "favorite programming language"
            ) ||
            lower.contains(
                "favourite programming language"
            )
        ) {

            val value =
                extractValueAfterPhrase(
                    text,
                    listOf(
                        "my favorite programming language is",
                        "my favourite programming language is",
                        "favorite programming language is",
                        "favourite programming language is",
                        "my favorite programming language",
                        "my favourite programming language",
                        "favorite programming language",
                        "favourite programming language"
                    )
                )

            if (
                !value.isNullOrBlank()
            ) {

                val correctedValue =
                    correctKnownMemoryValue(
                        "favorite_programming_language",
                        value
                    )

                return MemoryItem(
                    id = "",
                    type = MemoryType.PREFERENCE,
                    key = "favorite_programming_language",
                    value = correctedValue,
                    sensitivity = MemorySensitivity.NORMAL,
                    importance = MemoryImportance.IMPORTANT,
                    createdAt = 0L,
                    updatedAt = 0L
                )
            }
        }

        // =====================================================
        // 🚀 MAIN PROJECT
        // =====================================================

        if (
            lower.contains(
                "main project"
            )
        ) {

            val value =
                extractValueAfterPhrase(
                    text,
                    listOf(
                        "my main project is",
                        "main project is",
                        "my main project",
                        "main project"
                    )
                )

            if (
                !value.isNullOrBlank()
            ) {

                val correctedValue =
                    correctKnownMemoryValue(
                        "main_project",
                        value
                    )

                return MemoryItem(
                    id = "",
                    type = MemoryType.PERSONAL,
                    key = "main_project",
                    value = correctedValue,
                    sensitivity = MemorySensitivity.NORMAL,
                    importance = MemoryImportance.IMPORTANT,
                    createdAt = 0L,
                    updatedAt = 0L
                )
            }
        }

        // =====================================================
        // 🌙 PREFERRED THEME
        // =====================================================

        if (
            lower.contains(
                "i prefer dark mode"
            )
        ) {

            return MemoryItem(
                id = "",
                type = MemoryType.PREFERENCE,
                key = "preferred_theme",
                value = "dark mode",
                sensitivity = MemorySensitivity.NORMAL,
                importance = MemoryImportance.NORMAL,
                createdAt = 0L,
                updatedAt = 0L
            )
        }

        // =====================================================
        // 📍 HOME LOCATION
        // =====================================================

        if (
            lower.startsWith(
                "i live in "
            )
        ) {

            val value =
                text.substring(
                    "i live in ".length
                ).trim()

            if (
                value.isNotBlank()
            ) {

                return MemoryItem(
                    id = "",
                    type = MemoryType.PERSONAL,
                    key = "home_location",
                    value = value,
                    sensitivity = MemorySensitivity.PRIVATE,
                    importance = MemoryImportance.IMPORTANT,
                    createdAt = 0L,
                    updatedAt = 0L
                )
            }
        }

        return null
    }

    // =========================================================
    // 🔎 EXTRACT VALUE
    // =========================================================

    private fun extractValueAfterPhrase(
        text: String,
        phrases: List<String>
    ): String? {

        val lower =
            text.lowercase(
                Locale.getDefault()
            )

        for (phrase in phrases) {

            if (
                lower.startsWith(
                    phrase
                )
            ) {

                return text
                    .substring(
                        phrase.length
                    )
                    .trim()
                    .trim(
                        '.',
                        '!',
                        '?',
                        ','
                    )
            }
        }

        return null
    }

    // =========================================================
    // 🧠 CONTEXT-SPECIFIC SPEECH CORRECTIONS
    // =========================================================

    private fun correctKnownMemoryValue(
        key: String,
        value: String
    ): String {

        val normalized =
            value
                .lowercase(
                    Locale.getDefault()
                )
                .trim()

        return when (key) {

            "favorite_programming_language" -> {

                when {

                    normalized == "hotline" ->
                        "Kotlin"

                    normalized == "cotton" ->
                        "Kotlin"

                    normalized == "coatlin" ->
                        "Kotlin"

                    normalized == "kotlin" ->
                        "Kotlin"

                    else ->
                        value.trim()
                }
            }

            "main_project" -> {

                when {

                    normalized == "action" ->
                        "AXON"

                    normalized == "axon" ->
                        "AXON"

                    normalized == "ax on" ->
                        "AXON"

                    normalized == "acts on" ->
                        "AXON"

                    else ->
                        value.trim()
                }
            }

            else ->
                value.trim()
        }
    }
}