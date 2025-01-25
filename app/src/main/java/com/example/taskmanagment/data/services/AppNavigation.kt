package com.example.taskmanagment.data.services

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskmanagment.data.models.User
import com.example.taskmanagment.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var currentUserId by remember { mutableStateOf<String?>(null) } // ID текущего пользователя
    var currentUserRole by remember { mutableStateOf<String?>(null) } // Роль текущего пользователя

    NavHost(navController = navController, startDestination = "login") {
        // Экран входа
        composable("login") {
            LoginScreen(
                onLoginSuccess = { user_id, role ->
                    currentUserId = user_id
                    currentUserRole = role
                    when (role) {
                        "user" -> {
                            navController.navigate("tasks?user_id=$user_id") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        "manager" -> {
                            navController.navigate("manager") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        else -> {
                            navController.navigate("error") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }
                },
                onSwitchToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // Экран регистрации
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onSwitchToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // Экран задач для пользователя
        composable("tasks?user_id={user_id}") { backStackEntry ->
            val user_id = backStackEntry.arguments?.getString("user_id")
            if (user_id != null) {
                TasksListScreen(user_id = user_id)
            } else {
                Text("Error: User ID is missing")
            }
        }

        // Экран менеджера
        composable("manager") {
            ManagerScreen(navController)
        }

        // Экран управления пользователями
        composable("manageUsers") {
            ManageUsersScreen()
        }

        // Экран управления задачами
        composable("manageTasks") {
            ManageTasksScreen(navController)
        }

        // Экран с деталями задачи и комментариями администратора
        composable("adminTaskDetails?task_id={task_id}") { backStackEntry ->
            val task_id = backStackEntry.arguments?.getString("task_id")?.toIntOrNull()
            val admin_id = currentUserId?.toIntOrNull() // Берем ID текущего пользователя
            if (task_id != null && admin_id != null) {
                AdminCommentsScreen(task_id = task_id, admin_id = admin_id, navController = navController)
            } else {
                Text("Error: Task ID or Admin ID is missing")
            }
        }

        // Экран ошибки
        composable("error") {
            ErrorScreen(message = "Unknown role or navigation error.")
        }
    }
}




@Composable
fun ErrorScreen(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}
