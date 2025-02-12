package com.example.taskmanagment.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@Composable
fun ManagerScreen(navController: NavController) {
    val showLogoutDialog = remember { mutableStateOf(false) }

    // Обработка кнопки "Назад"
    BackHandler {
        showLogoutDialog.value = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Manager Panel",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Карточка для управления пользователями
        Button(
            onClick = { navController.navigate("manageUsers") },
            colors = ButtonDefaults.buttonColors(Color(0xFF171717)),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(64.dp)

        ) {
            Text(text = "Manage Users", color = MaterialTheme.colorScheme.onPrimaryContainer)
        }

        // Карточка для управления задачами
        Button(
            onClick = { navController.navigate("manageTasks") },
            colors = ButtonDefaults.buttonColors(Color(0xFF171717)),
            shape = MaterialTheme.shapes.small,

            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(64.dp)
        ) {
            Text(text = "Manage Tasks", color = MaterialTheme.colorScheme.onSecondaryContainer)
        }


        // Кнопка выхода из аккаунта
        Button(
            onClick = { showLogoutDialog.value = true },
            colors = ButtonDefaults.buttonColors(Color(0xFF171717)),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(64.dp)
        ) {
            Text(text = "Logout", color = MaterialTheme.colorScheme.onError)
        }
    }

    // Диалог подтверждения выхода
    if (showLogoutDialog.value) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog.value = false },
            title = { Text("Logout Confirmation") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog.value = false
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
