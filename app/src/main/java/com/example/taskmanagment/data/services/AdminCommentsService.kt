package com.example.taskmanagment.data.services

import android.util.Log
import com.example.taskmanagment.data.models.Comment
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

// Получение всех комментариев для задачи
suspend fun fetchCommentsForTask(task_id: Int): List<Comment> {
    return try {
        supabaseClient.from("comments")
            .select(Columns.raw("id, task_id, user_id, content, created_at"))
            {filter { eq("task_id", task_id) }}
            .decodeList<Comment>()
    } catch (e: Exception) {
        Log.e("AdminCommentsService", "Error fetching comments: ${e.message}")
        emptyList()
    }
}

suspend fun addComment(comment: Comment): Boolean {
    return try {
        val sanitizedComment = buildJsonObject {
            put("content", comment.content)
            put("user_id", comment.user_id)
            put("task_id", comment.task_id)
            put("created_at", comment.createdAt)
        }

        supabaseClient.from("comments")
            .insert(Json.encodeToJsonElement(sanitizedComment)) // Исключаем поле "id"
        true
    } catch (e: Exception) {
        Log.e("AdminCommentsService", "Error adding comment", e)
        false
    }
}


// Удаление комментария
suspend fun deleteComment(comment_id: Int): Boolean {
    return try {
        supabaseClient.from("comments").delete {
            filter { eq("id", comment_id) }
        }
        true
    } catch (e: Exception) {
        Log.e("AdminCommentsService", "Error deleting comment: ${e.message}")
        false
    }
}

// Редактирование комментария
suspend fun updateComment(comment_id: Int, newContent: String): Boolean {
    return try {
        supabaseClient.from("comments").update(
            mapOf("content" to newContent)
        ) {
            filter { eq("id", comment_id) }
        }
        true
    } catch (e: Exception) {
        Log.e("AdminCommentsService", "Error updating comment: ${e.message}")
        false
    }
}
