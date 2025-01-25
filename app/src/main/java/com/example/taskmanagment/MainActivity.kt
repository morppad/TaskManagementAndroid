package com.example.taskmanagment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.taskmanagment.data.services.AppNavigation
import com.example.taskmanagment.ui.theme.TaskManagmentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskManagmentTheme {
                AppNavigation() // Навигация приложения
            }
        }
    }
}