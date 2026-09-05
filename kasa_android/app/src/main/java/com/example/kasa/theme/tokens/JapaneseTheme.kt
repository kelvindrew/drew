package com.example.kasa.theme.tokens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object JapaneseTheme {

    // Palette Naturelle Japonaise (Light - Washi & Encre Sumi)
    private val LightPrimary = Color(0xFFC23829)       // Vermillon Shuu-iro (utilisé avec parcimonie)
    private val LightOnPrimary = Color(0xFFFFFFFF)
    private val LightPrimaryContainer = Color(0xFFF9E8E6) // Voile Washi Vermillon
    private val LightOnPrimaryContainer = Color(0xFF6E1810)
    private val LightSecondary = Color(0xFF2C5E43)     // Bambou Naturel
    private val LightOnSecondary = Color(0xFFFFFFFF)
    private val LightSecondaryContainer = Color(0xFFE8F0EB)
    private val LightOnSecondaryContainer = Color(0xFF133623)
    private val LightAccent = Color(0xFF283655)        // Indigo Japonais Profond
    private val LightBackground = Color(0xFFF9F7F2)    // Papier Washi artisanal
    private val LightOnBackground = Color(0xFF1E1E1E)  // Charbon Sumi
    private val LightSurface = Color(0xFFFFFFFF)
    private val LightOnSurface = Color(0xFF1E1E1E)
    private val LightSurfaceSecondary = Color(0xFFF3EFE8)
    private val LightSurfaceVariant = Color(0xFFEBE6DD)
    private val LightOnSurfaceVariant = Color(0xFF635F59)
    private val LightBorder = Color(0xFFE2DDD5)        // Ligne d'encre délavée
    private val LightBorderActive = Color(0xFFC23829)

    // Palette Japonaise Dark (Encre de Chine & Bois Cèdre)
    private val DarkPrimary = Color(0xFFE04F40)        // Vermillon Nocturne
    private val DarkOnPrimary = Color(0xFF200503)
    private val DarkPrimaryContainer = Color(0xFF5A150F)
    private val DarkOnPrimaryContainer = Color(0xFFFFDAD6)
    private val DarkSecondary = Color(0xFF5FA87F)     // Bambou de Lune
    private val DarkOnSecondary = Color(0xFF042111)
    private val DarkSecondaryContainer = Color(0xFF1A402B)
    private val DarkOnSecondaryContainer = Color(0xFFD3EEDF)
    private val DarkAccent = Color(0xFF7A97D1)        // Indigo Céleste
    private val DarkBackground = Color(0xFF111112)    // Sumi Noir Absolu
    private val DarkOnBackground = Color(0xFFF3F0EA)  // Ivoire Doux
    private val DarkSurface = Color(0xFF1B1B1D)       // Cèdre Sombre
    private val DarkOnSurface = Color(0xFFF3F0EA)
    private val DarkSurfaceSecondary = Color(0xFF242427)
    private val DarkSurfaceVariant = Color(0xFF303034)
    private val DarkOnSurfaceVariant = Color(0xFFA8A49C)
    private val DarkBorder = Color(0xFF323236)
    private val DarkBorderActive = Color(0xFFE04F40)

    private val shapes = ThemeShapes(
        small = RoundedCornerShape(3.dp),
        medium = RoundedCornerShape(5.dp),
        large = RoundedCornerShape(8.dp),
        cardShape = RoundedCornerShape(6.dp),
        buttonShape = RoundedCornerShape(4.dp),
        badgeShape = RoundedCornerShape(2.dp),
        inputShape = RoundedCornerShape(4.dp),
        bottomNavShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
    )

    private val typography = ThemeTypographyTokens(
        displayLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 30.sp,
            lineHeight = 38.sp,
            letterSpacing = 0.8.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.4.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            letterSpacing = 0.2.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 23.sp,
            letterSpacing = 0.15.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            letterSpacing = 0.1.sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            letterSpacing = 0.6.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp
        ),
        letterSpacingDisplay = 0.8f
    )

    private val motion = ThemeMotion(
        fastDuration = 220,
        normalDuration = 400,
        slowDuration = 600,
        pageTransition = tween(durationMillis = 350, easing = FastOutSlowInEasing)
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
                glowColor = Color.Transparent,
                success = Color(0xFF5FA87F),
                warning = Color(0xFFD49B4B),
                error = Color(0xFFE04F40),
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
                glowColor = Color.Transparent,
                success = Color(0xFF2C5E43),
                warning = Color(0xFFB57A2C),
                error = Color(0xFFC23829),
                isDark = false
            )
        }

        val borders = ThemeBorders(
            hairline = 0.75.dp,
            regular = 1.dp,
            thick = 1.5.dp,
            cardBorder = BorderStroke(0.75.dp, colors.border),
            cardBorderActive = BorderStroke(1.dp, colors.borderActive),
            buttonBorder = BorderStroke(0.75.dp, colors.border),
            is3dExtruded = false
        )

        val shadows = ThemeShadows(
            cardElevation = 0.dp, // Zen simplicity
            buttonElevation = 0.dp,
            floatingElevation = 3.dp,
            shadowColor = if (isDark) Color(0x33000000) else Color(0x0A000000)
        )

        val iconTokens = ThemeIconTokens(
            styleType = IconStyleType.JAPANESE_STAMP,
            containerShape = RoundedCornerShape(4.dp),
            containerBorder = BorderStroke(0.75.dp, colors.border),
            selectedBorder = BorderStroke(1.2.dp, colors.primary),
            containerBackground = if (isDark) colors.surfaceSecondary else colors.surfaceVariant.copy(alpha = 0.5f),
            selectedContainerBackground = if (isDark) colors.primaryContainer else colors.primaryContainer.copy(alpha = 0.7f),
            iconTint = colors.onSurfaceVariant,
            selectedIconTint = colors.primary,
            appIconKey = "emerald"
        )

        return KasaThemeDefinition(
            id = "JAPANESE",
            colors = colors,
            shapes = shapes,
            borders = borders,
            shadows = shadows,
            typography = typography,
            motion = motion,
            decorations = ThemeDecorations(
                emoji = "🎌",
                name = "Japanese Style",
                subtitle = "Minimalisme Zen, Washi & Ma",
                tagText = "Minimal & Balanced",
                visualSignature = "Ma Space • Sumi Ink Borders • Zen Calm"
            ),
            icons = iconTokens
        )
    }
}
