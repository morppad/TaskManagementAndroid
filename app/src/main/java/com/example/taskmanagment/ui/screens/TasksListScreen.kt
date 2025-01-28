package com.example.taskmanagment.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taskmanagment.data.models.Task
import com.example.taskmanagment.data.services.fetchUserTasks
import com.example.taskmanagment.data.services.updateTask
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TasksListScreen(user_id: String, navController: NavController) {
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Состояние для отображения диалога подтверждения выхода
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Обработка системной кнопки "Назад"
    BackHandler {
        showLogoutDialog = true
    }

    // Загрузка задач
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                tasks = fetchUserTasks(user_id.toInt()) // Загрузка задач для пользователя
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error fetching tasks: ${e.message}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // Избегаем перекрытия контента клавиатурой
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Tasks",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (tasks.isEmpty()) {
                    item {
                        Text(
                            text = "No tasks available.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    items(tasks) { task ->
                        TaskCard(
                            task = task,
                            onClick = {
                                navController.navigate("userTaskDetails?task_id=${task.id}&user_id=$user_id")
                            },
                            onComplete = {
                                coroutineScope.launch {
                                    try {
                                        updateTaskStatus(task, "Completed", snackbarHostState)
                                        tasks = fetchUserTasks(user_id.toInt()) // Обновляем список задач
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Error updating task: ${e.message}")
                                    }
                                }
                            },
                            onNeedHelp = {
                                coroutineScope.launch {
                                    try {
                                        updateTaskStatus(task, "Need Help", snackbarHostState)
                                        tasks = fetchUserTasks(user_id.toInt()) // Обновляем список задач
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Error updating task: ${e.message}")
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Кнопка выхода из аккаунта
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Logout")
            }
        }
    }

    // Диалог подтверждения выхода
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout Confirmation") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    // Возврат на экран входа
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TaskCard(task: Task, onClick: () -> Unit, onComplete: () -> Unit, onNeedHelp: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick), // Сохраняем кликабельность для открытия деталей и комментариев
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок задачи
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Описание задачи
            Text(
                text = task.description ?: "No description",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Кнопки действий
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onComplete) {
                    Text("Complete")
                }
                Button(
                    onClick = onNeedHelp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Need Help")
                }
            }
        }
    }
}

// Функция для обновления статуса задачи
suspend fun updateTaskStatus(task: Task, status: String, snackbarHostState: SnackbarHostState) {
    try {
        val updatedTask = task.copy(status = status)
        updateTask(updatedTask) // Обновление задачи через API
        snackbarHostState.showSnackbar("Task marked as $status")
    } catch (e: Exception) {
        snackbarHostState.showSnackbar("Error updating task: ${e.message}")
        Log.e("TaskCard", "Error updating task", e)
    }
}

