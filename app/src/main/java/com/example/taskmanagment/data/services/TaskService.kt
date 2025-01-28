package com.example.taskmanagment.data.services


import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import com.example.taskmanagment.data.models.Task
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Columns.Companion.raw
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import java.io.File
import java.io.InputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun getCurrentTimestamp(): String {
    return Instant.now()
        .atZone(ZoneOffset.UTC)
        .format(DateTimeFormatter.ISO_INSTANT)
}
suspend fun fetchUserTasks(user_id: Int): List<Task> {
    return try {
        val tasks = supabaseClient.from("tasks")
            .select(raw("id, title, description, status, priority, due_date, user_id, created_at, updated_at")) {
                filter {
                    eq ("user_id", user_id)
                }
            }
            .decodeList<Task>()
        tasks
    } catch (e: Exception) {
        Log.e("TaskService", "Error fetching user tasks", e)
        emptyList()
    }
}
suspend fun fetchTaskById(task_id: Int): Task? {
    return try {
        supabaseClient.from("tasks")
            .select() {filter { eq("id", task_id) }}
            .decodeSingleOrNull<Task>() // Получаем одну задачу или null
    } catch (e: Exception) {
        Log.e("TaskService", "Error fetching task by ID: ${e.message}", e)
        null
    }
}

suspend fun addTask(task: Task): Boolean {
    return try {
        supabaseClient.from("tasks").insert(task)
        true
    } catch (e: Exception) {
        Log.e("TaskService", "Error adding task", e)
        false
    }
}

suspend fun updateTask(task: Task): Boolean {
    return try {
        val jsonTask = Json.encodeToJsonElement(task) // Преобразуем объект Task в JSON

        supabaseClient.from("tasks").update(jsonTask) {
            filter { eq("id", task.id!!) } // Указываем условие обновления

        }
        Log.d("TaskService", "Task updated successfully: $task")
        true
    } catch (e: Exception) {
        Log.e("TaskService", "Error updating task: ${e.message}", e)
        false
    }
}


suspend fun deleteTask(task_id: Int): Boolean {
    return try {
        supabaseClient.from("tasks").delete {
            filter { eq("id", task_id) }
        }
        true
    } catch (e: Exception) {
        Log.e("TaskService", "Error deleting task", e)
        false
    }
}

