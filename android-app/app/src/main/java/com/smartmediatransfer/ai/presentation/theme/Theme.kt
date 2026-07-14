package com.smartmediatransfer.ai.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ElectricBlue = Color(0xFF2563EB)
val DeepPurple = Color(0xFF7C3AED)
val Turquoise = Color(0xFF06B6D4)
val DarkBackground = Color(0xFF0F111A)
val SurfaceDark = Color(0xFF1E1E24)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = DeepPurple,
    tertiary = Turquoise,
    background = DarkBackground,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    secondary = DeepPurple,
    tertiary = Turquoise,
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun SmartMediaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
