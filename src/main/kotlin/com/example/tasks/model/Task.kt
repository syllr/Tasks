package com.example.tasks.model

import java.time.Instant

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isProjectLevel: Boolean = true
)
