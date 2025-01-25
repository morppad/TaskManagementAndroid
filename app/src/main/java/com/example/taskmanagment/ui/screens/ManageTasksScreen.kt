package com.example.taskmanagment.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taskmanagment.data.models.Task
import com.example.taskmanagment.data.services.fetchAllTasks
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ManageTasksScreen(navController: NavController) {
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Загрузка задач
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                tasks = fetchAllTasks()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error fetching tasks: ${e.message}")
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
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
                        onEditClick = {
                            navController.navigate("adminTaskDetails?task_id=${task.id}")
                        },
                        onCommentsClick = {
                            navController.navigate("adminTaskDetails?task_id=${task.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ManagerTaskCard(task: Task, onEditClick: () -> Unit, onCommentsClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.description ?: "No description",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onEditClick) {
                    Text("Edit Task")
                }
                Button(onClick = onCommentsClick) {
                    Text("Comments")
                }
            }
        }
    }
}
