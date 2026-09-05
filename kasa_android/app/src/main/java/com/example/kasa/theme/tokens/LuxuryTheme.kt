package com.example.kasa.theme.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object LuxuryTheme {

    // Palette Haute Couture Light (Albâtre / Ivoire & Or Champagne Brossé)
    private val LightPrimary = Color(0xFF9E782F)       // Or Champagne Noble
    private val LightOnPrimary = Color(0xFFFFFFFF)
    private val LightPrimaryContainer = Color(0xFFF7F2E7) // Voile Champagne Délicat
    private val LightOnPrimaryContainer = Color(0xFF4A360E)
    private val LightSecondary = Color(0xFF1C1D21)     // Noir Onyx Profond
    private val LightOnSecondary = Color(0xFFFFFFFF)
    private val LightSecondaryContainer = Color(0xFFEDECE9)
    private val LightOnSecondaryContainer = Color(0xFF141518)
    private val LightAccent = Color(0xFFB38E44)        // Or Brossé Bijouterie
    private val LightBackground = Color(0xFFFAF9F6)    // Albâtre Pur
    private val LightOnBackground = Color(0xFF141518)  // Onyx Écriture
    private val LightSurface = Color(0xFFFFFFFF)       // Papier Soie Blanc
    private val LightOnSurface = Color(0xFF141518)
    private val LightSurfaceSecondary = Color(0xFFF4F2ED)
    private val LightSurfaceVariant = Color(0xFFECEAE3)
    private val LightOnSurfaceVariant = Color(0xFF6E6B64)
    private val LightBorder = Color(0xFFE2DDD2)        // Filet Hairline Délicat
    private val LightBorderActive = Color(0xFFB38E44)

    // Palette Haute Couture Dark (Onyx Impérial & Or Lumineux)
    private val DarkPrimary = Color(0xFFD4AF37)        // Or Champagne Brillant
    private val DarkOnPrimary = Color(0xFF1F1703)
    private val DarkPrimaryContainer = Color(0xFF3B2E0B)
    private val DarkOnPrimaryContainer = Color(0xFFFFF0CA)
    private val DarkSecondary = Color(0xFFE2DFD7)      // Platine Soie
    private val DarkOnSecondary = Color(0xFF16181D)
    private val DarkSecondaryContainer = Color(0xFF26282E)
    private val DarkOnSecondaryContainer = Color(0xFFE2DFD7)
    private val DarkAccent = Color(0xFFE5C158)         // Or Pur 24k Accent
    private val DarkBackground = Color(0xFF0C0D0F)     // Onyx Impérial Absolu
    private val DarkOnBackground = Color(0xFFF7F5F0)   // Ivoire Noble
    private val DarkSurface = Color(0xFF15171B)        // Charbon Velouté
    private val DarkOnSurface = Color(0xFFF7F5F0)
    private val DarkSurfaceSecondary = Color(0xFF1C1E24)
    private val DarkSurfaceVariant = Color(0xFF24272F)
    private val DarkOnSurfaceVariant = Color(0xFFA6A39A)
    private val DarkBorder = Color(0xFF2D3039)
    private val DarkBorderActive = Color(0xFFD4AF37)

    private val shapes = ThemeShapes(
        small = RoundedCornerShape(2.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(6.dp),
        cardShape = RoundedCornerShape(4.dp),
        buttonShape = RoundedCornerShape(3.dp),
        badgeShape = RoundedCornerShape(2.dp),
        inputShape = RoundedCornerShape(3.dp),
        bottomNavShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
    )

    private val typography = ThemeTypographyTokens(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Light,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 1.2.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
            fontSize = 23.sp,
            lineHeight = 30.sp,
            letterSpacing = 0.8.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Light,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 1.0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            letterSpacing = 0.6.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.4.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            letterSpacing = 0.3.sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            letterSpacing = 1.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.2.sp
        ),
        letterSpacingDisplay = 1.2f
    )

    private val motion = ThemeMotion(
        fastDuration = 250,
        normalDuration = 450,
        slowDuration = 650,
        pageTransition = tween(
            durationMillis = 400,
            easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
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
                glowColor = Color(0x26D4AF37),
                success = Color(0xFF68B684),
                warning = Color(0xFFD4AF37),
                error = Color(0xFFD9534F),
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
                glowColor = Color(0x1AB38E44),
                success = Color(0xFF2E7D4E),
                warning = Color(0xFFB38E44),
                error = Color(0xFFC0392B),
                isDark = false
            )
        }

        val borders = ThemeBorders(
            hairline = 0.5.dp,
            regular = 0.75.dp,
            thick = 1.dp,
            cardBorder = BorderStroke(0.5.dp, colors.border),
            cardBorderActive = BorderStroke(0.75.dp, colors.borderActive),
            buttonBorder = BorderStroke(0.75.dp, colors.primary),
            is3dExtruded = false
        )

        val shadows = ThemeShadows(
            cardElevation = 2.dp,
            buttonElevation = 1.dp,
            floatingElevation = 4.dp,
            shadowColor = if (isDark) Color(0x40000000) else Color(0x0F000000)
        )

        val iconTokens = ThemeIconTokens(
            styleType = IconStyleType.LUXURY_BEZEL,
            containerShape = CircleShape,
            containerBorder = BorderStroke(0.75.dp, colors.border),
            selectedBorder = BorderStroke(1.dp, colors.primary),
            containerBackground = if (isDark) colors.surfaceSecondary else colors.surfaceVariant.copy(alpha = 0.5f),
            selectedContainerBackground = if (isDark) colors.primaryContainer else colors.primary.copy(alpha = 0.15f),
            iconTint = colors.onSurfaceVariant,
            selectedIconTint = colors.primary,
            appIconKey = "gold"
        )

        return KasaThemeDefinition(
            id = "LUXURY",
            colors = colors,
            shapes = shapes,
            borders = borders,
            shadows = shadows,
            typography = typography,
            motion = motion,
            decorations = ThemeDecorations(
                emoji = "💎",
                name = "Luxury Design",
                subtitle = "Quiet Luxury, Or Champagne & Onyx",
                tagText = "Haute Couture",
                visualSignature = "Hairline Borders • Champagne Gold • Editorial Serif"
            ),
            icons = iconTokens
        )
    }
}
