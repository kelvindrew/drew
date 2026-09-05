package com.example.kasa.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kasa.theme.tokens.KawaiiTheme
import com.example.kasa.theme.tokens.KasaThemeDefinition
import com.example.kasa.theme.tokens.ThemeBorders
import com.example.kasa.theme.tokens.ThemeColors
import com.example.kasa.theme.tokens.ThemeDecorations
import com.example.kasa.theme.tokens.ThemeIconTokens
import com.example.kasa.theme.tokens.ThemeMotion
import com.example.kasa.theme.tokens.ThemeShadows
import com.example.kasa.theme.tokens.ThemeShapes
import com.example.kasa.theme.tokens.ThemeTypographyTokens

object KasaDimens {
    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 16.dp
    val spacingLg = 24.dp
    val spacingXl = 32.dp

    val radiusSm = 8.dp
    val radiusMd = 12.dp
    val radiusLg = 16.dp
    val radiusXl = 24.dp
}

val LocalKasaTheme = staticCompositionLocalOf<KasaThemeDefinition> {
    KawaiiTheme.create(false)
}

/**
 * Accesseur universel aux tokens du thème actif dans n'importe quel Composable.
 * Exemple: `KasaTheme.colors.primary`, `KasaTheme.shapes.cardShape`, `KasaTheme.borders.cardBorder`.
 */
object KasaTheme {
    val definition: KasaThemeDefinition
        @Composable
        get() = LocalKasaTheme.current

    val colors: ThemeColors
        @Composable
        get() = LocalKasaTheme.current.colors

    val shapes: ThemeShapes
        @Composable
        get() = LocalKasaTheme.current.shapes

    val borders: ThemeBorders
        @Composable
        get() = LocalKasaTheme.current.borders

    val shadows: ThemeShadows
        @Composable
        get() = LocalKasaTheme.current.shadows

    val typography: ThemeTypographyTokens
        @Composable
        get() = LocalKasaTheme.current.typography

    val motion: ThemeMotion
        @Composable
        get() = LocalKasaTheme.current.motion

    val decorations: ThemeDecorations
        @Composable
        get() = LocalKasaTheme.current.decorations

    val icons: ThemeIconTokens
        @Composable
        get() = LocalKasaTheme.current.icons

    val isDark: Boolean
        @Composable
        get() = LocalKasaTheme.current.colors.isDark
}

fun createMaterialColorScheme(themeDefinition: KasaThemeDefinition): ColorScheme {
    val c = themeDefinition.colors
    return if (c.isDark) {
        darkColorScheme(
            primary = c.primary,
            onPrimary = c.onPrimary,
            primaryContainer = c.primaryContainer,
            onPrimaryContainer = c.onPrimaryContainer,
            secondary = c.secondary,
            onSecondary = c.onSecondary,
            secondaryContainer = c.secondaryContainer,
            onSecondaryContainer = c.onSecondaryContainer,
            tertiary = c.accent,
            background = c.background,
            onBackground = c.onBackground,
            surface = c.surface,
            onSurface = c.onSurface,
            surfaceVariant = c.surfaceVariant,
            onSurfaceVariant = c.onSurfaceVariant,
            error = c.error,
            onError = c.background,
            outline = c.border
        )
    } else {
        lightColorScheme(
            primary = c.primary,
            onPrimary = c.onPrimary,
            primaryContainer = c.primaryContainer,
            onPrimaryContainer = c.onPrimaryContainer,
            secondary = c.secondary,
            onSecondary = c.onSecondary,
            secondaryContainer = c.secondaryContainer,
            onSecondaryContainer = c.onSecondaryContainer,
            tertiary = c.accent,
            background = c.background,
            onBackground = c.onBackground,
            surface = c.surface,
            onSurface = c.onSurface,
            surfaceVariant = c.surfaceVariant,
            onSurfaceVariant = c.onSurfaceVariant,
            error = c.error,
            onError = c.surface,
            outline = c.border
        )
    }
}

fun createMaterialTypography(tokens: ThemeTypographyTokens): Typography {
    return Typography(
        displayLarge = tokens.displayLarge,
        headlineMedium = tokens.headlineMedium,
        titleLarge = tokens.titleLarge,
        titleMedium = tokens.titleMedium,
        bodyLarge = tokens.bodyLarge,
        bodyMedium = tokens.bodyMedium,
        labelLarge = tokens.labelLarge,
        labelSmall = tokens.labelSmall
    )
}

fun createMaterialShapes(themeShapes: ThemeShapes): Shapes {
    return Shapes(
        small = themeShapes.small,
        medium = themeShapes.medium,
        large = themeShapes.large
    )
}

@Composable
fun KASATheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    aestheticTheme: AppAestheticTheme = AppAestheticTheme.KAWAII,
    cornerStyle: UiCornerStyle = UiCornerStyle.ROUNDED,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val themeDefinition = remember(aestheticTheme, isDark) {
        aestheticTheme.getDefinition(isDark)
    }

    val materialColors = remember(themeDefinition) {
        createMaterialColorScheme(themeDefinition)
    }

    val materialTypography = remember(themeDefinition) {
        createMaterialTypography(themeDefinition.typography)
    }

    val materialShapes = remember(themeDefinition) {
        createMaterialShapes(themeDefinition.shapes)
    }

    CompositionLocalProvider(
        LocalKasaTheme provides themeDefinition
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = materialTypography,
            shapes = materialShapes,
            content = content
        )
    }
}
