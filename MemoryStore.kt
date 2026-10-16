package com.pratik.axon.core.memory

interface MemoryStore {

    fun save(
        memory: MemoryItem
    )

    fun get(
        key: String
    ): MemoryItem?

    fun getAll(): List<MemoryItem>
    
        fun update(
        memory: MemoryItem
    )

    fun delete(
        key: String
    )

    fun clear()
}