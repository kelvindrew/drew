package com.example.kasa.theme.tokens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object FuturisticTheme {

    // Palette Futuristic Tech Dark (HUD 2026 / Obsidian / Cyber Cyan)
    private val DarkPrimary = Color(0xFF00E5FF)        // Cyber Cyan HUD
    private val DarkOnPrimary = Color(0xFF02161E)
    private val DarkPrimaryContainer = Color(0xFF004D5A)
    private val DarkOnPrimaryContainer = Color(0xFFB2F5FF)
    private val DarkSecondary = Color(0xFF8B5CF6)      // Electric Pulse Violet
    private val DarkOnSecondary = Color(0xFF1E0A3C)
    private val DarkSecondaryContainer = Color(0xFF3F1D78)
    private val DarkOnSecondaryContainer = Color(0xFFE5D9FC)
    private val DarkAccent = Color(0xFF00F5A0)         // Laser Telemetry Green
    private val DarkBackground = Color(0xFF080B11)     // Obsidian Space Black
    private val DarkOnBackground = Color(0xFFE2E8F0)   // Crisp Star White
    private val DarkSurface = Color(0xFF101726)        // Glassmorphic Space Panel
    private val DarkOnSurface = Color(0xFFE2E8F0)
    private val DarkSurfaceSecondary = Color(0xFF182238)
    private val DarkSurfaceVariant = Color(0xFF1F2D4A)
    private val DarkOnSurfaceVariant = Color(0xFF94A3B8)
    private val DarkBorder = Color(0xFF1E2E4E)        // Tech HUD Line
    private val DarkBorderActive = Color(0xFF00E5FF)

    // Palette Futuristic Tech Light (Laboratoire Titane & Spatial Clean)
    private val LightPrimary = Color(0xFF0284C7)       // Laser Quantum Blue
    private val LightOnPrimary = Color(0xFFFFFFFF)
    private val LightPrimaryContainer = Color(0xFFE0F2FE)
    private val LightOnPrimaryContainer = Color(0xFF034469)
    private val LightSecondary = Color(0xFF7C3AED)     // Quantum Violet
    private val LightOnSecondary = Color(0xFFFFFFFF)
    private val LightSecondaryContainer = Color(0xFFEDE9FE)
    private val LightOnSecondaryContainer = Color(0xFF3B1080)
    private val LightAccent = Color(0xFF059669)        // Orbital Green
    private val LightBackground = Color(0xFFF1F5F9)    // Titanium Lab White
    private val LightOnBackground = Color(0xFF0B132B)  // Carbon Dark
    private val LightSurface = Color(0xFFFFFFFF)       // Clean Ceramic Panel
    private val LightOnSurface = Color(0xFF0B132B)
    private val LightSurfaceSecondary = Color(0xFFE2E8F0)
    private val LightSurfaceVariant = Color(0xFFE2EBF8)
    private val LightOnSurfaceVariant = Color(0xFF475569)
    private val LightBorder = Color(0xFFCBD5E1)        // Fine Laser Grid
    private val LightBorderActive = Color(0xFF0284C7)

    private val shapes = ThemeShapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(6.dp),
        large = RoundedCornerShape(10.dp),
        cardShape = RoundedCornerShape(8.dp),
        buttonShape = RoundedCornerShape(6.dp),
        badgeShape = RoundedCornerShape(3.dp),
        inputShape = RoundedCornerShape(6.dp),
        bottomNavShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
    )

    private val typography = ThemeTypographyTokens(
        displayLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = 0.5.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 23.sp,
            lineHeight = 29.sp,
            letterSpacing = 0.4.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.3.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            letterSpacing = 0.2.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.1.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 19.sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            letterSpacing = 1.0.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.8.sp
        ),
        numericFontFamily = FontFamily.Monospace
    )

    private val motion = ThemeMotion(
        fastDuration = 140,
        normalDuration = 220,
        slowDuration = 320,
        pageTransition = tween(durationMillis = 200, easing = LinearEasing)
    )

    fun create(isDark: Boolean): KasaThemeDefinition {
        val colors = if (isDark) {
            ThemeColors(
                primary = DarkPrimary,
                onPrimary = DarkOnPrimary,
                primaryContainer = DarkPrimaryContainer,
                onPrimaryContainer = DarkOnPrimaryContainer,
                secondary = DarkSecondary,
                onSecondary = DarkOnSecondary,
                secondaryContainer = DarkSecondaryContainer,
                onSecondaryContainer = DarkOnSecondaryContainer,
                accent = DarkAccent,
                background = DarkBackground,
                onBackground = DarkOnBackground,
                surface = DarkSurface,
                onSurface = DarkOnSurface,
                surfaceSecondary = DarkSurfaceSecondary,
                surfaceVariant = DarkSurfaceVariant,
                onSurfaceVariant = DarkOnSurfaceVariant,
                border = DarkBorder,
                borderActive = DarkBorderActive,
                glowColor = Color(0x4000E5FF),
                success = Color(0xFF00F5A0),
                warning = Color(0xFFFFB300),
                error = Color(0xFFFF3366),
                isDark = true
            )
        } else {
            ThemeColors(
                primary = LightPrimary,
                onPrimary = LightOnPrimary,
                primaryContainer = LightPrimaryContainer,
                onPrimaryContainer = LightOnPrimaryContainer,
                secondary = LightSecondary,
                onSecondary = LightOnSecondary,
                secondaryContainer = LightSecondaryContainer,
                onSecondaryContainer = LightOnSecondaryContainer,
                accent = LightAccent,
                background = LightBackground,
                onBackground = LightOnBackground,
                surface = LightSurface,
                onSurface = LightOnSurface,
                surfaceSecondary = LightSurfaceSecondary,
                surfaceVariant = LightSurfaceVariant,
                onSurfaceVariant = LightOnSurfaceVariant,
                border = LightBorder,
                borderActive = LightBorderActive,
                glowColor = Color(0x330284C7),
                success = Color(0xFF059669),
                warning = Color(0xFFD97706),
                error = Color(0xFFE11D48),
                isDark = false
            )
        }

        val borders = ThemeBorders(
            hairline = 0.75.dp,
            regular = 1.dp,
            thick = 1.5.dp,
            cardBorder = BorderStroke(1.dp, colors.border),
            cardBorderActive = BorderStroke(1.5.dp, colors.borderActive),
            buttonBorder = BorderStroke(1.dp, colors.primary),
            is3dExtruded = false,
            isNeonGlow = true
        )

        val shadows = ThemeShadows(
            cardElevation = 3.dp,
            buttonElevation = 2.dp,
            floatingElevation = 6.dp,
            shadowColor = if (isDark) Color(0x4D00E5FF) else Color(0x1A0284C7)
        )

        val iconTokens = ThemeIconTokens(
            styleType = IconStyleType.FUTURISTIC_HUD,
            containerShape = RoundedCornerShape(topStart = 10.dp, bottomEnd = 10.dp, topEnd = 2.dp, bottomStart = 2.dp),
            containerBorder = BorderStroke(1.dp, colors.border),
            selectedBorder = BorderStroke(1.5.dp, colors.primary),
            containerBackground = if (isDark) colors.surfaceSecondary.copy(alpha = 0.8f) else colors.surfaceVariant.copy(alpha = 0.6f),
            selectedContainerBackground = colors.primaryContainer.copy(alpha = 0.6f),
            iconTint = colors.onSurfaceVariant,
            selectedIconTint = colors.primary,
            appIconKey = "dark"
        )

        return KasaThemeDefinition(
            id = "FUTURISTIC",
            colors = colors,
            shapes = shapes,
            borders = borders,
            shadows = shadows,
            typography = typography,
            motion = motion,
            decorations = ThemeDecorations(
                emoji = "🚀",
                name = "Futuristic UI",
                subtitle = "HUD 2026, Spatial & Verre Dépoli",
                tagText = "Tech & Cyber",
                visualSignature = "HUD Panels • Cyan Glow • Data Monospace"
            ),
            icons = iconTokens
        )
    }
}
