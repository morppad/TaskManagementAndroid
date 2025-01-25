package com.example.taskmanagment.data.services

import android.util.Log
import com.example.taskmanagment.data.models.Task
import com.example.taskmanagment.data.models.User
import io.github.jan.supabase.postgrest.from

suspend fun addUser(user: User): Boolean {
    return try {
        supabaseClient.from("users").insert(
            mapOf(
                "username" to user.username,
                "email" to user.email,
                "role" to user.role,
                "password_hash" to user.password,
                "created_at" to user.createdAt
            )
        )
        true
    } catch (e: Exception) {
        Log.e("UserService", "Error adding user: ${e.message}", e)
        false
    }
}

suspend fun updateUser(user: User): Boolean {
    return try {
        supabaseClient.from("users")
            .update(user) { filter { user.id?.let { eq("id", it) } } }
        Log.d("UserService", "User updated: $user")
        true
    } catch (e: Exception) {
        Log.e("UserService", "Error updating user: ${e.message}", e)
        false
    }
}
suspend fun fetchUsers(): List<User> {
    return try {
        // Запрос к таблице "users" для получения всех пользователей
        val users = supabaseClient.from("users")
            .select()
            .decodeList<User>() // Преобразование ответа в список объектов User
        Log.d("UserService", "Fetched users: $users")
        users
    } catch (e: Exception) {
        Log.e("UserService", "Error fetching users", e)
        emptyList() // Возврат пустого списка в случае ошибки
    }
}


suspend fun deleteUser(user_id: String): Boolean {
    return try {
        supabaseClient.from("users")
            .delete { filter { eq("id", user_id) } }
        Log.d("UserService", "User deleted: $user_id")
        true
    } catch (e: Exception) {
        Log.e("UserService", "Error deleting user: ${e.message}")
        false
    }
}


suspend fun fetchAllTasks(): List<Task> {
    return try {
        supabaseClient.from("tasks")
            .select()
            .decodeList<Task>()
    } catch (e: Exception) {
        Log.e("TaskService", "Error fetching tasks", e)
        emptyList()
    }
}

suspend fun deleteTask(task_id: String): Boolean {
    return try {
        supabaseClient.from("tasks")
            .delete { filter { eq("id", task_id) } }
        true
    } catch (e: Exception) {
        Log.e("TaskService", "Error deleting task", e)
        false
    }
}
