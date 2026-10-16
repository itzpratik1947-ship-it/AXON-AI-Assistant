package com.pratik.axon.core.taskstate

import com.pratik.axon.core.model.AxonResult

data class TaskEvent(
    val taskId: String,
    val type: TaskEventType,
    val status: TaskStatus,
    val result: AxonResult? = null,
    val message: String = ""
)

enum class TaskEventType {
    CREATED,
    STARTED,
    WAITING,
    COMPLETED,
    FAILED,
    CANCELLED
}