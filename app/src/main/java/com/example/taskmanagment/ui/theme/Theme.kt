package com.example.taskmanagment.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.unit.dp

// Цветовая схема в стиле Notion
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),       // Белый для текста
    secondary = Color(0xFFAAAAAA),     // Светло-серый для второстепенных элементов
    tertiary = Color(0xFF666666),      // Темно-серый для акцентов
    background = Color(0xFF000000),    // Черный фон
    surface = Color(0xFF111111),       // Темно-серые поверхности
    onPrimary = Color(0xFF000000),     // Черный текст на белом фоне
    onSecondary = Color(0xFFFFFFFF),   // Белый текст на сером фоне
    onTertiary = Color(0xFFFFFFFF),    // Белый текст на темно-сером фоне
    onBackground = Color(0xFFFFFFFF),  // Белый текст на черном фоне
    onSurface = Color(0xFFFFFFFF)      // Белый текст на поверхностях
)
val CustomShapes = Shapes()
@Composable
fun TaskManagmentTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography, // Используем стандартную типографику
        shapes = CustomShapes,         // Используем стандартные формы
        content = content
    )
}
