package com.pratik.axon.core.memory

import java.util.Locale

class MemoryQueryDetector {

    // =====================================================
    // 🔎 MEMORY RETRIEVAL
    // =====================================================

    fun isMemoryQuery(
        command: String
    ): Boolean {

        val text =
            normalize(
                command
            )

        return isFavoriteProgrammingLanguageQuery(
            text
        ) ||
        isMainProjectQuery(
            text
        ) ||
        isAllMemoriesQuery(
            text
        )
    }

    // =====================================================
    // 🗑️ MEMORY DELETE
    // =====================================================

    fun isForgetMemoryCommand(
        command: String
    ): Boolean {

        val text =
            normalize(
                command
            )

        return (
            text.contains(
                "forget my favorite programming language"
            ) ||
            text.contains(
                "forget my favourite programming language"
            ) ||
            text.contains(
                "forget favorite programming language"
            ) ||
            text.contains(
                "forget favourite programming language"
            ) ||
            text.contains(
                "forget my main project"
            ) ||
            text.contains(
                "forget my project"
            )
        )
    }

    // =====================================================
    // 🧹 FORGET EVERYTHING
    // =====================================================

    fun isForgetAllMemoriesCommand(
        command: String
    ): Boolean {

        val text =
            normalize(
                command
            )

        return (
            text.contains(
                "forget everything you remember"
            ) ||
            text.contains(
                "forget everything you know about me"
            ) ||
            text.contains(
                "forget all my memories"
            ) ||
            text.contains(
                "forget all memories"
            ) ||
            text.contains(
                "delete all my memories"
            ) ||
            text.contains(
                "delete all memories"
            )
        )
    }

    // =====================================================
    // 🔑 GET MEMORY KEY
    // =====================================================

    fun getMemoryKey(
        command: String
    ): String? {

        val text =
            normalize(
                command
            )

        if (
            text.contains(
                "favorite programming language"
            ) ||
            text.contains(
                "favourite programming language"
            )
        ) {
            return "favorite_programming_language"
        }

        if (
            text.contains(
                "main project"
            ) ||
            text.contains(
                "my project"
            )
        ) {
            return "main_project"
        }

        return null
    }

    // =====================================================
    // 📚 ALL MEMORY QUERY
    // =====================================================

    fun isAllMemoriesQuery(
        command: String
    ): Boolean {

        val text =
            normalize(
                command
            )

        return (
            text == "what do you remember about me" ||
            text == "what do you remember about me?" ||
            text == "what memories do you have" ||
            text == "what memories do you have?" ||
            text == "what do you remember" ||
            text == "show my memories" ||
            text == "show me my memories" ||
            text == "list my memories"
        )
    }

    // =====================================================
    // ⭐ FAVORITE LANGUAGE QUERY
    // =====================================================

    private fun isFavoriteProgrammingLanguageQuery(
        text: String
    ): Boolean {

        return (
            text.contains(
                "what is my favorite programming language"
            ) ||
            text.contains(
                "what is my favourite programming language"
            ) ||
            text.contains(
                "what's my favorite programming language"
            ) ||
            text.contains(
                "what's my favourite programming language"
            )
        )
    }

    // =====================================================
    // 🚀 MAIN PROJECT QUERY
    // =====================================================

    private fun isMainProjectQuery(
        text: String
    ): Boolean {

        return (
            text.contains(
                "what is my main project"
            ) ||
            text.contains(
                "what's my main project"
            ) ||
            text.contains(
                "what is my project"
            ) ||
            text.contains(
                "what's my project"
            )
        )
    }

    // =====================================================
    // 🧹 NORMALIZE
    // =====================================================

    private fun normalize(
        command: String
    ): String {

        return command
            .lowercase(
                Locale.getDefault()
            )
            .trim()
            .replace(
                Regex("\\s+"),
                " "
            )
            .replace(
                Regex("[?!,]"),
                ""
            )
    }
}