package com.reciperoulette.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE94560),
    secondary = Color(0xFF0F3460),
    tertiary = Color(0xFF16213E),
    background = Color(0xFF1A1A2E),
    surface = Color(0xFF16213E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun RecipeRouletteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}