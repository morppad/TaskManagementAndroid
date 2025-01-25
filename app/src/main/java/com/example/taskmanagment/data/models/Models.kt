package com.example.taskmanagment.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int? = null,
    val username: String,
    val email: String,
    val role: String = "user",
    @SerialName("password_hash") val password: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class Task(
    val id: Int,
    val title: String,
    val description: String?,
    val status: String = "pending",
    val priority: String = "normal",
    @SerialName("due_date")val dueDate: String?, // Nullable
    @SerialName("user_id")val user_id: Int, // Nullable
    @SerialName("created_at")val createdAt: String?, // Nullable
    @SerialName("updated_at")val updatedAt: String? // Nullable
)

@Serializable
data class Comment(
    val id: Int,
    val content: String,
    @SerialName("user_id")val user_id: Int,
    @SerialName("task_id")val task_id: Int,
    @SerialName("created_at")val createdAt: String
)
