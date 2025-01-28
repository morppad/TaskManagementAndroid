package com.example.taskmanagment.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taskmanagment.data.models.Task
import com.example.taskmanagment.data.models.User
import com.example.taskmanagment.data.services.addTask
import com.example.taskmanagment.data.services.fetchAllTasks
import com.example.taskmanagment.data.services.fetchUsers
import com.example.taskmanagment.data.services.getCurrentTimestamp
import com.example.taskmanagment.data.services.updateTask
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ManageTasksScreen(user_id: String, navController: NavController) {
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isCreateDialogOpen by remember { mutableStateOf(false) }
    var isAssignDialogOpen by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Загрузка задач и пользователей
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                tasks = fetchAllTasks()
                users = fetchUsers()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error loading data: ${e.message}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { isCreateDialogOpen = true }) {
                Text("+") // Кнопка создания задачи
            }
        }
    ) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
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
                    ManagerTaskCard(
                        task = task,
                        onClick = {
                            navController.navigate("adminTaskDetails?task_id=${task.id}&user_id=$user_id")
                        },
                        onAssignClick = {
                            selectedTask = task
                            isAssignDialogOpen = true
                        }
                    )
                }
            }
        }
    }

    // Диалог для создания задачи
    if (isCreateDialogOpen) {
        CreateTaskDialog(
            users = users,
            onDismiss = { isCreateDialogOpen = false },
            onCreate = { newTask ->
                coroutineScope.launch {
                    try {
                        addTask(newTask) // Функция для добавления задачи
                        tasks = fetchAllTasks() // Обновление списка задач
                        snackbarHostState.showSnackbar("Task created successfully")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error creating task: ${e.message}")
                    } finally {
                        isCreateDialogOpen = false
                    }
                }
            }
        )
    }

    // Диалог для назначения задачи
    if (isAssignDialogOpen) {
        AssignTaskDialog(
            task = selectedTask,
            users = users,
            onDismiss = { isAssignDialogOpen = false },
            onAssign = { user ->
                isAssignDialogOpen = false
                coroutineScope.launch {
                    try {
                        if (selectedTask != null) {
                            val updatedTask = selectedTask!!.copy(user_id = user.id!!)
                            updateTask(updatedTask) // Обновление задачи с новым пользователем
                            tasks = fetchAllTasks() // Обновление списка задач
                            snackbarHostState.showSnackbar("Task assigned to ${user.username}")
                        }

                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error assigning task: ${e.message}")
                    }
                }
            }
        )
    }
}

@Composable
fun ManagerTaskCard(task: Task, onClick: () -> Unit, onAssignClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onAssignClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Assign")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.description ?: "No description available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Assigned to: ${task.user_id.let { "User ID: $it" } ?: "No user assigned"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CreateTaskDialog(
    users: List<User>,
    onDismiss: () -> Unit,
    onCreate: (Task) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("Priority") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Assign to:")
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp) // Ограничение высоты списка пользователей
                ) {
                    items(users) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedUser = user },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedUser == user,
                                onClick = { selectedUser = user }
                            )
                            Text(
                                text = user.username,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || priority.isBlank() || selectedUser == null) {
                        errorMessage = "All fields are required"
                    } else {
                        onCreate(
                            Task(
                                title = title,
                                description = description,
                                priority = priority,
                                user_id = selectedUser!!.id!!,
                                dueDate = null, // Можно добавить поле для даты
                                createdAt = getCurrentTimestamp(),
                                updatedAt = getCurrentTimestamp()
                            )
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun AssignTaskDialog(
    task: Task?,
    users: List<User>,
    onDismiss: () -> Unit,
    onAssign: (User) -> Unit
) {
    var selectedUser by remember { mutableStateOf<User?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Task") },
        text = {
            Column {
                Text(
                    text = "Task: ${task?.title ?: "No task selected"}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    items(users) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedUser = user },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedUser == user,
                                onClick = { selectedUser = user }
                            )
                            Text(
                                text = user.username,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedUser != null) {
                        onAssign(selectedUser!!)
                    }
                },
                enabled = selectedUser != null
            ) {
                Text("Assign")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
