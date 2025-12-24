package com.neobuk.app.data.models

import java.util.Date
import java.util.UUID

enum class TaskStatus {
    TODO, IN_PROGRESS, DONE
}

enum class TaskPriority {
    NORMAL, HIGH
}

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dueDate: Long? = null, // Timestamp
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val relatedLink: String? = null, // e.g., "Invoice #123", "Customer: John"
    val assignedTo: String? = null // Future staff assignment, default hidden
)
