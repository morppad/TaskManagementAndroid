package com.example.taskmanagment.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taskmanagment.data.models.Comment
import com.example.taskmanagment.data.models.Task
import com.example.taskmanagment.data.models.User
import com.example.taskmanagment.data.services.addComment
import com.example.taskmanagment.data.services.fetchCommentsForTask
import com.example.taskmanagment.data.services.fetchTaskById
import com.example.taskmanagment.data.services.getCurrentTimestamp
import com.example.taskmanagment.data.services.updateTask
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AdminTaskDetailsScreen(task_id: Int, user_id: Int, navController: NavController, users: List<User>) {
    var task by remember { mutableStateOf<Task?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var newComment by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    // Загрузка данных
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
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> EditTaskContent(
                        task = task,
                        onSave = { updatedTask ->
                            coroutineScope.launch {
                                try {
                                    if (updatedTask != null) {
                                        updateTask(updatedTask)
                                        snackbarHostState.showSnackbar("Task updated successfully")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error updating task: ${e.message}")
                                }
                            }
                        },
                        onCancel = { navController.popBackStack() }
                    )
                    1 -> UserCommentsContent(
                        task_id = task_id,
                        user_id = user_id,
                        comments = comments,
                        users = users,
                        newComment = newComment,
                        onNewCommentChange = { newComment = it },
                        onAddComment = {
                            coroutineScope.launch {
                                try {
                                    val comment = Comment(
                                        id = 0,
                                        task_id = task_id,
                                        user_id = user_id,
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
                        refreshComments = { fetchCommentsForTask(task_id) }
                    )
                }
            }

            // Индикатор точек
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { pageIndex ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(horizontal = 4.dp)
                            .background(
                                color = if (pagerState.currentPage == pageIndex) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }
        }
    }
}


@Composable
fun EditTaskContent(task: Task?, onSave: (Task?) -> Unit, onCancel: () -> Unit) {
    task?.let {
        Column(modifier = Modifier.padding(16.dp)) {
            var title by remember { mutableStateOf(it.title) }
            var description by remember { mutableStateOf(it.description ?: "") }
            var priority by remember { mutableStateOf(it.priority) }
            var dueDate by remember { mutableStateOf(it.dueDate ?: "") }

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

            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Due Date") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onCancel) {
                    Text("Cancel")
                }
                Button(onClick = { onSave(task.copy(title = title, description = description, priority = priority, dueDate = dueDate)) }) {
                    Text("Save Changes")
                }
            }
        }
    } ?: Text("Loading task...", modifier = Modifier.padding(16.dp))
}

@Composable
fun AdminCommentsContent(
    task_id: Int,
    user_id: Int,
    comments: List<Comment>,
    users: List<User>,
    newComment: String,
    onNewCommentChange: (String) -> Unit,
    onAddComment: () -> Unit,
    refreshComments: suspend () -> List<Comment>
) {
    var localComments by remember { mutableStateOf(comments) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Автообновление комментариев
    LaunchedEffect(task_id) {
        while (true) {
            coroutineScope.launch {
                try {
                    val updatedComments = refreshComments()
                    if (updatedComments.size > localComments.size) {
                        localComments = updatedComments
                        listState.animateScrollToItem(updatedComments.size - 1) // Автопрокрутка вниз
                    } else {
                        localComments = updatedComments
                    }
                } catch (e: Exception) {
                    // Лог ошибок обновления
                }
            }
            kotlinx.coroutines.delay(5000)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Comments", style = MaterialTheme.typography.headlineMedium)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp),
            state = listState
        ) {
            items(localComments) { comment ->
                val isCurrentUser = comment.user_id == user_id
                val username = users.find { it.id == comment.user_id }?.username ?: "Unknown User"
                UserCommentItem(comment, username, isCurrentUser)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newComment,
                onValueChange = onNewCommentChange,
                label = { Text("Add a comment") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onAddComment) {
                Text("Send")
            }
        }
    }
}

@Composable
fun AdminCommentItem(comment: Comment, username: String, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = comment.createdAt,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}