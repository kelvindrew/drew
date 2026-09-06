package com.example.kasa.theme.tokens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object KawaiiTheme {

    // Palette Pastel Light
    private val LightPrimary = Color(0xFFFF6B9D)       // Soft Strawberry Rose
    private val LightOnPrimary = Color(0xFFFFFFFF)
    private val LightPrimaryContainer = Color(0xFFFFE3EC) // Whipped Cream Pink
    private val LightOnPrimaryContainer = Color(0xFF8A1845)
    private val LightSecondary = Color(0xFF9D7BFF)     // Dreamy Lavender
    private val LightOnSecondary = Color(0xFFFFFFFF)
    private val LightSecondaryContainer = Color(0xFFF0EBFF)
    private val LightOnSecondaryContainer = Color(0xFF4C2A9E)
    private val LightAccent = Color(0xFF48CFAD)        // Pastel Mint
    private val LightBackground = Color(0xFFFFF9FA)    // Soft Vanilla Milk
    private val LightOnBackground = Color(0xFF2E1B24)  // Deep Soft Plum
    private val LightSurface = Color(0xFFFFFFFF)
    private val LightOnSurface = Color(0xFF2E1B24)
    private val LightSurfaceSecondary = Color(0xFFFFF0F4)
    private val LightSurfaceVariant = Color(0xFFFCE7EE)
    private val LightOnSurfaceVariant = Color(0xFF7A5868)
    private val LightBorder = Color(0xFFFFD1DF)        // Pastel Marshmallow Border
    private val LightBorderActive = Color(0xFFFF6B9D)

    // Palette Pastel Dark
    private val DarkPrimary = Color(0xFFFF85AE)        // Bright Pastel Pink
    private val DarkOnPrimary = Color(0xFF38071B)
    private val DarkPrimaryContainer = Color(0xFF6B1838)
    private val DarkOnPrimaryContainer = Color(0xFFFFE0EB)
    private val DarkSecondary = Color(0xFFB89EFF)      // Luminous Lavender
    private val DarkOnSecondary = Color(0xFF261250)
    private val DarkSecondaryContainer = Color(0xFF442A7C)
    private val DarkOnSecondaryContainer = Color(0xFFF1EBFF)
    private val DarkAccent = Color(0xFF6EE7B7)         // Vivid Mint Light
    private val DarkBackground = Color(0xFF1E1018)     // Midnight Plum
    private val DarkOnBackground = Color(0xFFFDF0F5)
    private val DarkSurface = Color(0xFF2B1824)        // Warm Deep Blackberry
    private val DarkOnSurface = Color(0xFFFDF0F5)
    private val DarkSurfaceSecondary = Color(0xFF381F2F)
    private val DarkSurfaceVariant = Color(0xFF45273B)
    private val DarkOnSurfaceVariant = Color(0xFFD4AFC3)
    private val DarkBorder = Color(0xFF5A2E4B)
    private val DarkBorderActive = Color(0xFFFF85AE)

    private val shapes = ThemeShapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
        cardShape = RoundedCornerShape(20.dp),
        buttonShape = RoundedCornerShape(18.dp),
        badgeShape = CircleShape,
        inputShape = RoundedCornerShape(16.dp),
        bottomNavShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    )

    private val typography = ThemeTypographyTokens(
        displayLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.2).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 23.sp,
            lineHeight = 29.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            lineHeight = 25.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 18.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    )

    private val motion = ThemeMotion(
        fastDuration = 180,
        normalDuration = 320,
        slowDuration = 480,
        buttonSpring = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
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
                glowColor = Color(0x33FF85AE),
                success = Color(0xFF34D399),
                warning = Color(0xFFFBBF24),
                error = Color(0xFFFB7185),
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
                glowColor = Color(0x26FF6B9D),
                success = Color(0xFF10B981),
                warning = Color(0xFFF59E0B),
                error = Color(0xFFF43F5E),
                isDark = false
            )
        }

        val borders = ThemeBorders(
            hairline = 1.dp,
            regular = 1.2.dp,
            thick = 2.dp,
            cardBorder = BorderStroke(1.dp, colors.border),
            cardBorderActive = BorderStroke(1.5.dp, colors.borderActive),
            buttonBorder = null,
            is3dExtruded = false
        )

        val shadows = ThemeShadows(
            cardElevation = 3.dp,
            buttonElevation = 2.dp,
            floatingElevation = 6.dp,
            shadowColor = if (isDark) Color(0x33000000) else Color(0x14FF8BAE)
        )

        val iconTokens = ThemeIconTokens(
            styleType = IconStyleType.KAWAII_BUBBLE,
            containerShape = CircleShape,
            containerBorder = BorderStroke(1.dp, colors.border.copy(alpha = 0.6f)),
            selectedBorder = BorderStroke(2.dp, colors.primary),
            containerBackground = if (isDark) colors.surfaceSecondary else colors.primaryContainer.copy(alpha = 0.35f),
            selectedContainerBackground = colors.primary,
            iconTint = colors.onSurfaceVariant,
            selectedIconTint = colors.onPrimary,
            appIconKey = "sunset"
        )

        return KasaThemeDefinition(
            id = "KAWAII",
            colors = colors,
            shapes = shapes,
            borders = borders,
            shadows = shadows,
            typography = typography,
            motion = motion,
            decorations = ThemeDecorations(
                emoji = "🌸",
                name = "Doux & Convivial",
                subtitle = "Pastel doux & Formes marshmallow",
                tagText = "Chaleureux & Doux",
                visualSignature = "Formes Marshmallow • Tons Pastel • Rebonds Doux"
            ),
            icons = iconTokens
        )
    }
}
