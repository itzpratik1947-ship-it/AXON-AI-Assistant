package com.pratik.axon.platform.android.memory

import com.pratik.axon.core.memory.MemoryImportance
import com.pratik.axon.core.memory.MemoryItem
import com.pratik.axon.core.memory.MemorySensitivity
import com.pratik.axon.core.memory.MemoryType
import com.pratik.axon.core.memory.MemoryStore

class MemoryRepository(
    private val memoryDao: MemoryDao
) : MemoryStore {

    // =====================================================
    // 💾 SAVE MEMORY
    // =====================================================
    
    override fun save(
        memory: MemoryItem
    ) {

        val entity =
            MemoryEntity(
                id = memory.id,
                type = memory.type.name,
                key = memory.key,
                value = memory.value,
                sensitivity = memory.sensitivity.name,
                importance = memory.importance.name,
                createdAt = memory.createdAt,
                updatedAt = memory.updatedAt
            )

        memoryDao.insert(
            entity
        )
    }

    // =====================================================
    // 🔎 FIND MEMORY
    // =====================================================

    fun findByKey(
        key: String
    ): MemoryItem? {

        val entity =
            memoryDao.getByKey(
                key
            )

        return entity?.let {
            MemoryItem(
                id = it.id,
                type = MemoryType.valueOf(
                    it.type
                ),
                key = it.key,
                value = it.value,
                sensitivity =
                    MemorySensitivity.valueOf(
                        it.sensitivity
                    ),
                importance =
                    MemoryImportance.valueOf(
                        it.importance
                    ),
                createdAt = it.createdAt,
                updatedAt = it.updatedAt
            )
        }
    }

    // =====================================================
    // 📚 GET ALL MEMORIES
    // =====================================================

    override fun getAll(): List<MemoryItem> {

        return memoryDao
            .getAll()
            .map {

                MemoryItem(
                    id = it.id,
                    type = MemoryType.valueOf(
                        it.type
                    ),
                    key = it.key,
                    value = it.value,
                    sensitivity =
                        MemorySensitivity.valueOf(
                            it.sensitivity
                        ),
                    importance =
                        MemoryImportance.valueOf(
                            it.importance
                        ),
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            }
    }

    // =====================================================
    // ✏️ UPDATE MEMORY
    // =====================================================

    override fun update(
        memory: MemoryItem
    ) {

        val entity =
            MemoryEntity(
                id = memory.id,
                type = memory.type.name,
                key = memory.key,
                value = memory.value,
                sensitivity = memory.sensitivity.name,
                importance = memory.importance.name,
                createdAt = memory.createdAt,
                updatedAt = memory.updatedAt
            )

        memoryDao.update(
            entity
        )
    }

    // =====================================================
    // 🗑️ DELETE MEMORY
    // =====================================================

    fun delete(
        memory: MemoryItem
    ) {

        val entity =
            MemoryEntity(
                id = memory.id,
                type = memory.type.name,
                key = memory.key,
                value = memory.value,
                sensitivity = memory.sensitivity.name,
                importance = memory.importance.name,
                createdAt = memory.createdAt,
                updatedAt = memory.updatedAt
            )

        memoryDao.delete(
            entity
        )
    }

    // =====================================================
    // 🧹 DELETE ALL MEMORIES
    // =====================================================

    fun deleteAll() {

        memoryDao.deleteAll()
    }
        override fun get(
        key: String
    ): MemoryItem? {

        return findByKey(
            key
        )
    }

    override fun delete(
        key: String
    ) {

        val memory =
            findByKey(
                key
            )

        if (memory != null) {
            delete(
                memory
            )
        }
    }

    override fun clear() {

        deleteAll()
    }
}