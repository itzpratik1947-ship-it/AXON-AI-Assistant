package com.pratik.axon.core.memory

import java.util.Locale

class MemoryCommandDetector {

    // =====================================================
    // 🧠 DETECT EXPLICIT MEMORY COMMAND
    // =====================================================

    fun isMemoryCommand(
        command: String
    ): Boolean {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        return text.startsWith("remember that ") ||
                text.startsWith("remember this ") ||
                text.startsWith("don't forget ") ||
                text.startsWith("do not forget ")
    }

    // =====================================================
    // ✂️ EXTRACT MEMORY CONTENT
    // =====================================================

    fun extractMemoryContent(
        command: String
    ): String {

        var text =
            command.trim()

        val prefixes =
            listOf(
                "remember that ",
                "remember this ",
                "don't forget ",
                "do not forget "
            )

        for (prefix in prefixes) {

            if (
                text.lowercase(
                    Locale.getDefault()
                ).startsWith(prefix)
            ) {

                text =
                    text.substring(
                        prefix.length
                    )

                break
            }
        }

        return text.trim()
    }
}