package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SafetyOrange,
    onPrimary = Color.Black,
    secondary = CardCarbon,
    onSecondary = TextLight,
    tertiary = SafetyYellow,
    background = DarkCarbon,
    onBackground = TextLight,
    surface = CardCarbon,
    onSurface = TextLight,
    error = SafetyRed
)

private val LightColorScheme = lightColorScheme(
    primary = SafetyOrange,
    onPrimary = Color.White,
    secondary = Color(0xFFECEFF1),
    onSecondary = Color(0xFF263238),
    tertiary = Color(0xFFF57C00),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    error = SafetyRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark theme by default for industrial look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
