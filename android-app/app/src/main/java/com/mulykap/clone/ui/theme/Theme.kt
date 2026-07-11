package com.mulykap.clone.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MulykapLightRed,
    secondary = MulykapRed,
    tertiary = MulykapSurfaceLight,
    background = MulykapBlack,
    surface = MulykapDarkBlue,
    onPrimary = MulykapBlack,
    onSecondary = MulykapSurfaceLight,
    onBackground = MulykapSurfaceLight,
    onSurface = MulykapSurfaceLight
)

private val LightColorScheme = lightColorScheme(
    primary = MulykapRed,
    secondary = MulykapDarkBlue,
    tertiary = MulykapDarkRed,
    background = MulykapBackgroundLight,
    surface = MulykapSurfaceLight,
    onPrimary = MulykapSurfaceLight,
    onSecondary = MulykapSurfaceLight,
    onBackground = MulykapBlack,
    onSurface = MulykapBlack
)

@Composable
fun MulykapCloneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to enforce brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
