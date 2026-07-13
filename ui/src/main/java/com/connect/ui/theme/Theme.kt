package com.connect.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

val AppleBlue = Color(0xFF007AFF)
val AppleGreen = Color(0xFF34C759)
val AppleRed = Color(0xFFFF3B30)

val DarkColorScheme = darkColorScheme(
    primary = AppleBlue,
    secondary = AppleGreen,
    tertiary = AppleRed,
    background = Color(0xFF000000), // Pure Black for OLED
    surface = Color(0xFF1C1C1E), // Elevated surface
    surfaceVariant = Color(0xFF2C2C2E), // Secondary elevated
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

val LightColorScheme = lightColorScheme(
    primary = AppleBlue,
    secondary = AppleGreen,
    tertiary = AppleRed,
    background = Color(0xFFF2F2F7), // Light gray background
    surface = Color(0xFFFFFFFF), // Pure white surface
    surfaceVariant = Color(0xFFE5E5EA),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun ConnectPremiumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
