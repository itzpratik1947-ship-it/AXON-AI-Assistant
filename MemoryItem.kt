package com.pratik.axon.core.memory

// =========================================================
// 🧠 AXON MEMORY ITEM
// =========================================================
//
// Represents ONE long-term memory stored by AXON.
//
// Example:
// key   = favorite_programming_language
// value = Kotlin
//
// =========================================================

data class MemoryItem(

    // Unique ID for this memory
    val id: String,

    // What kind of memory this is
    val type: MemoryType,

    // What the memory is about
    val key: String,

    // Actual remembered information
    val value: String,

    // Security level of this memory
    val sensitivity: MemorySensitivity,

    // How important this memory is
    val importance: MemoryImportance,

    // Creation timestamp
    val createdAt: Long,

    // Last modification timestamp
    val updatedAt: Long
)

// =========================================================
// 🧠 MEMORY TYPE
// =========================================================

enum class MemoryType {

    PERSONAL,

    PREFERENCE,

    FACT
}

// =========================================================
// 🔐 MEMORY SENSITIVITY
// =========================================================

enum class MemorySensitivity {

    NORMAL,

    PRIVATE
}

// =========================================================
// ⭐ MEMORY IMPORTANCE
// =========================================================

enum class MemoryImportance {

    NORMAL,

    IMPORTANT
}