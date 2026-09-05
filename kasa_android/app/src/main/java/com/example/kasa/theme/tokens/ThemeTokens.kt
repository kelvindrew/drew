package com.example.kasa.theme.tokens

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Palette de couleurs sémantiques pour un thème spécifique (Light ou Dark).
 */
data class ThemeColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val accent: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceSecondary: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val border: Color,
    val borderActive: Color,
    val glowColor: Color = Color.Transparent,
    val success: Color = Color(0xFF10B981),
    val warning: Color = Color(0xFFF59E0B),
    val error: Color = Color(0xFFEF4444),
    val isDark: Boolean = false
)

/**
 * Formes géométriques et rayons de courbure pour chaque thème.
 */
data class ThemeShapes(
    val small: CornerBasedShape = RoundedCornerShape(8.dp),
    val medium: CornerBasedShape = RoundedCornerShape(12.dp),
    val large: CornerBasedShape = RoundedCornerShape(18.dp),
    val cardShape: CornerBasedShape = RoundedCornerShape(16.dp),
    val buttonShape: CornerBasedShape = RoundedCornerShape(14.dp),
    val badgeShape: CornerBasedShape = RoundedCornerShape(8.dp),
    val inputShape: CornerBasedShape = RoundedCornerShape(12.dp),
    val bottomNavShape: CornerBasedShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
)

/**
 * Définition des bordures et effets de relief.
 */
data class ThemeBorders(
    val hairline: Dp = 0.5.dp,
    val regular: Dp = 1.dp,
    val thick: Dp = 2.dp,
    val cardBorder: BorderStroke? = null,
    val cardBorderActive: BorderStroke? = null,
    val buttonBorder: BorderStroke? = null,
    val is3dExtruded: Boolean = false,
    val extrusionDepth: Dp = 0.dp,
    val extrusionColor: Color = Color.Transparent,
    val isNeonGlow: Boolean = false
)

/**
 * Définition des ombres et élévations.
 */
data class ThemeShadows(
    val cardElevation: Dp = 2.dp,
    val buttonElevation: Dp = 2.dp,
    val floatingElevation: Dp = 6.dp,
    val shadowColor: Color = Color(0x1A000000),
    val ambientShadowColor: Color = Color(0x0D000000),
    val spotShadowColor: Color = Color(0x1F000000)
)

/**
 * Typographie et hiérarchie éditoriale adaptée à l'esprit du thème.
 */
data class ThemeTypographyTokens(
    val displayLarge: TextStyle,
    val headlineMedium: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val labelLarge: TextStyle,
    val labelSmall: TextStyle,
    val numericFontFamily: FontFamily = FontFamily.SansSerif,
    val letterSpacingDisplay: Float = 0f
)

/**
 * Comportements de micro-animations et transitions.
 */
data class ThemeMotion(
    val fastDuration: Int = 150,
    val normalDuration: Int = 280,
    val slowDuration: Int = 450,
    val buttonSpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    val pageTransition: AnimationSpec<Float> = tween(
        durationMillis = 280,
        easing = FastOutSlowInEasing
    )
)

/**
 * Décorations visuelles et identité narrative.
 */
data class ThemeDecorations(
    val emoji: String,
    val name: String,
    val subtitle: String,
    val tagText: String,
    val visualSignature: String
)

/**
 * Styles morphologiques d'icônes spécifiques à chaque univers de design.
 */
enum class IconStyleType {
    KAWAII_BUBBLE,
    JAPANESE_STAMP,
    ISOMETRIC_3D,
    FUTURISTIC_HUD,
    LUXURY_BEZEL
}

/**
 * Tokens régissant la morphologie, les contours et l'apparence des icônes pour chaque thème.
 */
data class ThemeIconTokens(
    val styleType: IconStyleType,
    val containerShape: Shape,
    val containerBorder: BorderStroke? = null,
    val selectedBorder: BorderStroke? = null,
    val containerBackground: Color,
    val selectedContainerBackground: Color,
    val iconTint: Color,
    val selectedIconTint: Color,
    val appIconKey: String
)

/**
 * Définition globale et centralisée d'un thème dans le design system KASA.
 */
data class KasaThemeDefinition(
    val id: String,
    val colors: ThemeColors,
    val shapes: ThemeShapes,
    val borders: ThemeBorders,
    val shadows: ThemeShadows,
    val typography: ThemeTypographyTokens,
    val motion: ThemeMotion,
    val decorations: ThemeDecorations,
    val icons: ThemeIconTokens
)
