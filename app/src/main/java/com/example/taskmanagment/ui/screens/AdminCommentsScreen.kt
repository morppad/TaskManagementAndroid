package com.example.taskmanagment.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taskmanagment.data.models.Comment
import com.example.taskmanagment.data.models.Task
import com.example.taskmanagment.data.services.addComment
import com.example.taskmanagment.data.services.fetchCommentsForTask
import com.example.taskmanagment.data.services.fetchTaskById
import com.example.taskmanagment.data.services.getCurrentTimestamp
import com.example.taskmanagment.data.services.updateTask
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AdminCommentsScreen(task_id: Int, admin_id: Int, navController: NavController) {
    var task by remember { mutableStateOf<Task?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newComment by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(task_id) {
        coroutineScope.launch {
            try {
                task = fetchTaskById(task_id)
                comments = fetchCommentsForTask(task_id)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error loading data: ${e.message}")
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        task?.let {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Task Details", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))

                // Заголовок
                OutlinedTextField(
                    value = it.title,
                    onValueChange = { newTitle -> task = task?.copy(title = newTitle) },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Описание
                OutlinedTextField(
                    value = it.description ?: "",
                    onValueChange = { newDesc -> task = task?.copy(description = newDesc) },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Кнопки
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        coroutineScope.launch {
                            if (task != null) {
                                updateTask(task!!)
                                snackbarHostState.showSnackbar("Task updated successfully")
                            }
                        }
                    }) {
                        Text("Save Changes")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Секция комментариев
                Text("Comments", style = MaterialTheme.typography.headlineMedium)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 16.dp)
                ) {
                    items(comments) { comment ->
                        CommentItem(comment = comment)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Добавление комментария
                OutlinedTextField(
                    value = newComment,
                    onValueChange = { newComment = it },
                    label = { Text("Add a comment") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val comment = Comment(
                                    id = 0,
                                    task_id = task_id,
                                    user_id = admin_id,
                                    content = newComment,
                                    createdAt = getCurrentTimestamp()
                                )
                                if (addComment(comment)) {
                                    comments = fetchCommentsForTask(task_id)
                                    newComment = ""
                                    snackbarHostState.showSnackbar("Comment added successfully")
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error adding comment: ${e.message}")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Post Comment")
                }
            }
        } ?: Text("Loading task...", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "User ID: ${comment.user_id}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = comment.createdAt,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
