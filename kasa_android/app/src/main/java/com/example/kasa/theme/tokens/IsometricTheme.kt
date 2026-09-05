package com.example.kasa.theme.tokens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object IsometricTheme {

    // Palette Isométrique 3D Light
    private val LightPrimary = Color(0xFF2563EB)       // Cobalt Isométrique
    private val LightOnPrimary = Color(0xFFFFFFFF)
    private val LightPrimaryContainer = Color(0xFFDBEAFE) // Facette Bleue Claire
    private val LightOnPrimaryContainer = Color(0xFF1E40AF)
    private val LightSecondary = Color(0xFF0D9488)     // Teal Géométrique
    private val LightOnSecondary = Color(0xFFFFFFFF)
    private val LightSecondaryContainer = Color(0xFFCCFBF1)
    private val LightOnSecondaryContainer = Color(0xFF115E59)
    private val LightAccent = Color(0xFFF59E0B)        // Ambre de Chantier 3D
    private val LightBackground = Color(0xFFEEF2F6)    // Grille Architecte Neutre
    private val LightOnBackground = Color(0xFF0F172A)  // Encre Structurelle
    private val LightSurface = Color(0xFFFFFFFF)       // Dalle Supérieure Pure
    private val LightOnSurface = Color(0xFF0F172A)
    private val LightSurfaceSecondary = Color(0xFFE2E8F0) // Dalle de Niveau 2
    private val LightSurfaceVariant = Color(0xFFE0E7FF) // Facette Biseautée
    private val LightOnSurfaceVariant = Color(0xFF475569)
    private val LightBorder = Color(0xFFCBD5E1)        // Bordure Géométrique
    private val LightBorderActive = Color(0xFF2563EB)
    private val LightExtrusion = Color(0xFF94A3B8)     // Facette 3D Inférieure

    // Palette Isométrique 3D Dark
    private val DarkPrimary = Color(0xFF38BDF8)        // Cyan Isométrique Brillant
    private val DarkOnPrimary = Color(0xFF082F49)
    private val DarkPrimaryContainer = Color(0xFF0C4A6E)
    private val DarkOnPrimaryContainer = Color(0xFFBAE6FD)
    private val DarkSecondary = Color(0xFF818CF8)      // Indigo Isométrique
    private val DarkOnSecondary = Color(0xFF1E1B4B)
    private val DarkSecondaryContainer = Color(0xFF312E81)
    private val DarkOnSecondaryContainer = Color(0xFFC7D2FE)
    private val DarkAccent = Color(0xFFFBBF24)
    private val DarkBackground = Color(0xFF0B132B)     // Abyssal Blueprint
    private val DarkOnBackground = Color(0xFFF1F5F9)
    private val DarkSurface = Color(0xFF1C2541)        // Bloc Isométrique Élevé
    private val DarkOnSurface = Color(0xFFF1F5F9)
    private val DarkSurfaceSecondary = Color(0xFF233054)
    private val DarkSurfaceVariant = Color(0xFF2D3C66)
    private val DarkOnSurfaceVariant = Color(0xFF94A3B8)
    private val DarkBorder = Color(0xFF334155)
    private val DarkBorderActive = Color(0xFF38BDF8)
    private val DarkExtrusion = Color(0xFF070B18)     // Ombre Portée 3D du Bloc

    private val shapes = ThemeShapes(
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(10.dp),
        large = RoundedCornerShape(14.dp),
        cardShape = RoundedCornerShape(10.dp),
        buttonShape = RoundedCornerShape(8.dp),
        badgeShape = RoundedCornerShape(4.dp),
        inputShape = RoundedCornerShape(8.dp),
        bottomNavShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    )

    private val typography = ThemeTypographyTokens(
        displayLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.3).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            lineHeight = 30.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            lineHeight = 25.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
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
            lineHeight = 18.sp,
            letterSpacing = 0.4.sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    )

    private val motion = ThemeMotion(
        fastDuration = 120,
        normalDuration = 220,
        slowDuration = 350,
        buttonSpring = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
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
                glowColor = Color(0x3338BDF8),
                success = Color(0xFF34D399),
                warning = Color(0xFFFBBF24),
                error = Color(0xFFF87171),
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
                glowColor = Color(0x202563EB),
                success = Color(0xFF059669),
                warning = Color(0xFFD97706),
                error = Color(0xFFDC2626),
                isDark = false
            )
        }

        val borders = ThemeBorders(
            hairline = 1.dp,
            regular = 1.5.dp,
            thick = 2.dp,
            cardBorder = BorderStroke(1.dp, colors.border),
            cardBorderActive = BorderStroke(1.5.dp, colors.borderActive),
            buttonBorder = BorderStroke(1.5.dp, colors.borderActive),
            is3dExtruded = true,
            extrusionDepth = 4.dp,
            extrusionColor = if (isDark) DarkExtrusion else LightExtrusion
        )

        val shadows = ThemeShadows(
            cardElevation = 5.dp,
            buttonElevation = 4.dp,
            floatingElevation = 8.dp,
            shadowColor = if (isDark) Color(0x66000000) else Color(0x260F172A),
            ambientShadowColor = if (isDark) Color(0x40000000) else Color(0x1A0F172A),
            spotShadowColor = if (isDark) Color(0x80000000) else Color(0x330F172A)
        )

        val iconTokens = ThemeIconTokens(
            styleType = IconStyleType.ISOMETRIC_3D,
            containerShape = RoundedCornerShape(8.dp),
            containerBorder = BorderStroke(1.dp, colors.border),
            selectedBorder = BorderStroke(1.5.dp, colors.primary),
            containerBackground = if (isDark) colors.surfaceSecondary else colors.surfaceVariant,
            selectedContainerBackground = colors.primary,
            iconTint = colors.onSurfaceVariant,
            selectedIconTint = colors.onPrimary,
            appIconKey = "default"
        )

        return KasaThemeDefinition(
            id = "ISOMETRIC",
            colors = colors,
            shapes = shapes,
            borders = borders,
            shadows = shadows,
            typography = typography,
            motion = motion,
            decorations = ThemeDecorations(
                emoji = "🔷",
                name = "Isometric Design",
                subtitle = "Profondeur 3D, Dalles & Reliefs",
                tagText = "3D & Geometric",
                visualSignature = "3D Extrusion • Bevel Borders • Mechanical Press"
            ),
            icons = iconTokens
        )
    }
}
