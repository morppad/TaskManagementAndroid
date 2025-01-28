package com.example.taskmanagment.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.taskmanagment.data.models.User
import com.example.taskmanagment.data.services.fetchUsers
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.taskmanagment.data.services.addUser
import com.example.taskmanagment.data.services.deleteUser
import com.example.taskmanagment.data.services.getCurrentTimestamp
import com.example.taskmanagment.data.services.updateUser
import kotlinx.datetime.format.Padding

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ManageUsersScreen() {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var isDialogOpen by remember { mutableStateOf(false) }
    var isDeleteDialogOpen by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Загрузка списка пользователей
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                users = fetchUsers()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error fetching users: ${e.message}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedUser = null // Новый пользователь
                isDialogOpen = true
            }) {
                Text("+") // Кнопка добавления
            }
        }
    ) {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            if (users.isEmpty()) {
                item {
                    Text(
                        text = "No users available.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                items(users) { user ->
                    UserCard(
                        user = user,
                        onClick = {
                            selectedUser = user // Редактирование выбранного пользователя
                            isDialogOpen = true
                        },
                        onDelete = {
                            selectedUser = user
                            isDeleteDialogOpen = true // Открываем диалог удаления
                        }
                    )
                }
            }
        }
    }

    // Диалог для добавления/редактирования пользователя
    if (isDialogOpen) {
        UserDialog(
            user = selectedUser,
            existingUsers = users.map { it.username },
            onDismiss = { isDialogOpen = false },
            onSave = { newUser ->
                coroutineScope.launch {
                    try {
                        if (selectedUser == null) {
                            addUser(newUser) // Добавление
                        } else {
                            updateUser(newUser) // Обновление
                        }
                        users = fetchUsers() // Обновление списка пользователей
                        isDialogOpen = false
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error saving user: ${e.message}")
                    }
                }
            }
        )
    }

    // Диалог подтверждения удаления пользователя
    if (isDeleteDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteDialogOpen = false },
            title = { Text("Delete Confirmation") },
            text = { Text("Are you sure you want to delete this user?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        try {
                            deleteUser(selectedUser?.id.toString())
                            users = fetchUsers() // Обновляем список после удаления
                            snackbarHostState.showSnackbar("User deleted successfully.")
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Error deleting user: ${e.message}")
                        } finally {
                            isDeleteDialogOpen = false
                        }
                    }
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UserCard(user: User, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp) // Общий отступ
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween // Пространство между элементами
        ) {
            // Кнопка удаления
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete user",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.width(16.dp)) // Промежуток между кнопкой и блоком данных

            // Данные пользователя
            Column(modifier = Modifier.weight(1f)) { // Вес занимает оставшееся пространство
                Text(
                    text = "Name: ${user.username}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Email: ${user.email}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Role: ${user.role}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@Composable
fun UserDialog(
    user: User?,
    existingUsers: List<String>,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var username by remember { mutableStateOf(user?.username ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var role by remember { mutableStateOf(user?.role ?: "") }
    var password by remember { mutableStateOf("") } // Пароль только для нового пользователя
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) } // Для управления состоянием DropdownMenu
    val roles = listOf("User", "Manager")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Add User" else "Edit User") },
        text = {
            Column {
                // Поле ввода имени пользователя
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorMessage = if (existingUsers.contains(it) && user?.username != it) {
                            "Username already exists"
                        } else {
                            null
                        }
                    },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Поле ввода Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Выпадающий список для выбора роли
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        label = { Text("Role") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { roleOption ->
                            DropdownMenuItem(
                                text = { Text(roleOption) },
                                onClick = {
                                    role = roleOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Поле ввода пароля (только для нового пользователя)
                if (user == null) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isBlank() || email.isBlank() || (user == null && password.isBlank()) || role.isBlank()) {
                        errorMessage = "All fields are required."
                    } else {
                        val newUser = user?.copy(
                            username = username,
                            email = email,
                            role = role
                        ) ?: User(
                            username = username,
                            email = email,
                            role = role,
                            password = BCrypt.withDefaults().hashToString(12, password.toCharArray()),
                            createdAt = getCurrentTimestamp()
                        )
                        onSave(newUser)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
