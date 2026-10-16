package com.pratik.axon.platform.android.memory

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MemoryDao {

    // Save a new memory
    @Insert
    fun insert(
        memory: MemoryEntity
    )

    // Get all stored memories
    @Query(
        "SELECT * FROM axon_memory ORDER BY updatedAt DESC"
    )
    fun getAll(): List<MemoryEntity>

    // Find a memory using its key
    @Query(
        "SELECT * FROM axon_memory WHERE `key` = :key LIMIT 1"
    )
    fun getByKey(
        key: String
    ): MemoryEntity?

    // Update an existing memory
    @Update
    fun update(
        memory: MemoryEntity
    )

    // Delete a memory
    @Delete
    fun delete(
        memory: MemoryEntity
    )

    // Delete everything
    @Query(
        "DELETE FROM axon_memory"
    )
    fun deleteAll()
}