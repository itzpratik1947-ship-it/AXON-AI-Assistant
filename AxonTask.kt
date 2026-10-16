package com.pratik.axon.core.taskstate

import com.pratik.axon.core.model.AxonRequest
import com.pratik.axon.core.model.AxonResult

data class AxonTask(
    val id: String,
    val request: AxonRequest,
    val status: TaskStatus = TaskStatus.CREATED,
    val result: AxonResult? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
    val message: String = ""
)