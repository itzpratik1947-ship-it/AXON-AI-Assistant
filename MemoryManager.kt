package com.pratik.axon.core.memory

import java.util.UUID

class MemoryManager(
    private val memoryStore: MemoryStore
) {

    private val memoryCommandDetector =
        MemoryCommandDetector()

    private val memoryContentParser =
        MemoryContentParser()

    // =====================================================
    // 💾 SAVE A MEMORY
    // =====================================================

    fun saveMemory(
        type: MemoryType,
        key: String,
        value: String,
        sensitivity: MemorySensitivity =
            MemorySensitivity.NORMAL,
        importance: MemoryImportance =
            MemoryImportance.NORMAL
    ): MemoryItem {

        val existingMemory =
            memoryStore.get(key)

        if (
            existingMemory != null
        ) {

            val updatedMemory =
                existingMemory.copy(
                    type = type,
                    value = value,
                    sensitivity = sensitivity,
                    importance = importance,
                    updatedAt =
                        System.currentTimeMillis()
                )

            memoryStore.update(
                updatedMemory
            )

            return updatedMemory
        }

        val now =
            System.currentTimeMillis()

        val newMemory =
            MemoryItem(
                id = "memory_" +
                        UUID.randomUUID()
                            .toString(),
                type = type,
                key = key,
                value = value,
                sensitivity = sensitivity,
                importance = importance,
                createdAt = now,
                updatedAt = now
            )

        memoryStore.save(
            newMemory
        )

        return newMemory
    }

    // =====================================================
    // 🧠 PROCESS MEMORY COMMAND
    // =====================================================

    fun processMemoryCommand(
        command: String
    ): MemoryItem? {

        if (
            !memoryCommandDetector
                .isMemoryCommand(command)
        ) {
            return null
        }

        val content =
            memoryCommandDetector
                .extractMemoryContent(
                    command
                )

        val parsedMemory =
            memoryContentParser.parse(
                content
            )
                ?: return null

        return saveMemory(
            type = parsedMemory.type,
            key = parsedMemory.key,
            value = parsedMemory.value
        )
    }

    // =====================================================
    // 🔎 FIND MEMORY
    // =====================================================

    fun findMemory(
        key: String
    ): MemoryItem? {

        return memoryStore.get(
            key
        )
    }

    // =====================================================
    // 📚 GET ALL MEMORIES
    // =====================================================

    fun getAllMemories():
        List<MemoryItem> {

        return memoryStore.getAll()
    }

    // =====================================================
    // 🗑️ FORGET ONE MEMORY
    // =====================================================

    fun forgetMemory(
        memory: MemoryItem
    ) {

        memoryStore.delete(
            memory.key
        )
    }

    // =====================================================
    // 🗑️ FORGET MEMORY BY KEY
    // =====================================================

    fun forgetMemoryByKey(
        key: String
    ): Boolean {

        val memory =
            memoryStore.get(
                key
            )

        if (
            memory == null
        ) {
            return false
        }

        memoryStore.delete(
            key
        )

        return true
    }

    // =====================================================
    // 🧹 FORGET EVERYTHING
    // =====================================================

    fun forgetAllMemories() {

        memoryStore.clear()
    }
}