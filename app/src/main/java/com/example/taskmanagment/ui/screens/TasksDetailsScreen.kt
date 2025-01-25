package com.example.taskmanagment.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taskmanagment.data.models.Comment
import com.example.taskmanagment.data.models.Task
import com.example.taskmanagment.data.models.User
import com.example.taskmanagment.data.services.addComment
import com.example.taskmanagment.data.services.fetchCommentsForTask
import com.example.taskmanagment.data.services.fetchUserTasks
import com.example.taskmanagment.data.services.getCurrentTimestamp
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TaskDetailScreen(
    task: Task,
    userId: Int,
    isAdmin: Boolean,
    users: List<User>, // Список пользователей
    onUpdateTask: ((Task) -> Unit)? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }


    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Информация о задаче
            Text("Task Details", style = MaterialTheme.typography.headlineMedium)
            Text("Title: ${task.title}", style = MaterialTheme.typography.bodyLarge)
            Text("Description: ${task.description ?: "No description"}", style = MaterialTheme.typography.bodyLarge)
            Text("Priority: ${task.priority}", style = MaterialTheme.typography.bodyLarge)
            Text("Due Date: ${task.dueDate ?: "No due date"}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Для администратора: кнопка редактирования задачи
            if (isAdmin) {
                Button(onClick = { onUpdateTask?.invoke(task) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Edit Task")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

        }
    }
}

