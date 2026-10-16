package com.pratik.axon.platform.android.memory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "axon_memory"
)
data class MemoryEntity(

    @PrimaryKey
    val id: String,

    val type: String,

    val key: String,

    val value: String,

    val sensitivity: String,

    val importance: String,

    val createdAt: Long,

    val updatedAt: Long
)