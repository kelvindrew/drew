package com.example.kasa.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.kasa.theme.tokens.IconStyleType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.theme.AppAestheticTheme
import com.example.kasa.theme.KasaTheme
import com.example.kasa.theme.tokens.KasaThemeDefinition

/**
 * Carte adaptative qui adopte les formes, bordures, ombres et effets de relief
 * propres à chaque Design System (Kawaii, Japanese, Isometric, Futuristic, Luxury).
 */
@Composable
fun ThemedCard(
    modifier: Modifier = Modifier,
    containerColor: Color = KasaTheme.colors.surface,
    contentColor: Color = KasaTheme.colors.onSurface,
    shape: Shape = KasaTheme.shapes.cardShape,
    border: BorderStroke? = KasaTheme.borders.cardBorder,
    elevation: Dp = KasaTheme.shadows.cardElevation,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = KasaTheme.definition
    val isIsometric = theme.borders.is3dExtruded
    val isFuturistic = theme.borders.isNeonGlow
    val isLuxury = theme.id == "LUXURY"

    val baseModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    if (isIsometric) {
        // Rendu 3D Isométrique avec biseau inférieur physique
        Box(modifier = baseModifier) {
            // Bloc d'extrusion 3D inférieur
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = theme.borders.extrusionDepth)
                    .clip(shape)
                    .background(theme.borders.extrusionColor)
            )

            // Dalle isométrique supérieure
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                border = border,
                elevation = CardDefaults.cardElevation(defaultElevation = elevation)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    content = content
                )
            }
        }
    } else {
        Card(
            modifier = baseModifier.fillMaxWidth(),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Accent supérieur subtil pour le mode Futuriste (HUD laser line)
                if (isFuturistic) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        KasaTheme.colors.primary,
                                        KasaTheme.colors.secondary,
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
                // Accent supérieur subtil pour le mode Luxury (filet doré)
                if (isLuxury) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        KasaTheme.colors.primary.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                content()
            }
        }
    }
}

/**
 * Bouton haut de gamme intégrant le langage tactile de chaque thème.
 */
@Composable
fun ThemedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    iconEmoji: String? = null,
    isPrimary: Boolean = true,
    enabled: Boolean = true
) {
    ThemedButton(
        onClick = onClick,
        modifier = modifier,
        isPrimary = isPrimary,
        enabled = enabled
    ) {
        if (iconEmoji != null) {
            Text(iconEmoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = if (KasaTheme.definition.id == "LUXURY") 1.2.sp else 0.sp
        )
    }
}

@Composable
fun ThemedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val theme = KasaTheme.definition
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = theme.motion.buttonSpring,
        label = "button_scale"
    )

    val containerColor = if (isPrimary) {
        KasaTheme.colors.primary
    } else {
        KasaTheme.colors.surfaceVariant
    }

    val contentColor = if (isPrimary) {
        KasaTheme.colors.onPrimary
    } else {
        KasaTheme.colors.onSurfaceVariant
    }

    val shape = KasaTheme.shapes.buttonShape
    val border = if (isPrimary) {
        KasaTheme.borders.buttonBorder
    } else {
        BorderStroke(1.dp, KasaTheme.colors.border)
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .offset(y = if (theme.borders.is3dExtruded && isPressed) 2.dp else 0.dp),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = border,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = KasaTheme.shadows.buttonElevation,
            pressedElevation = if (theme.borders.is3dExtruded) 1.dp else KasaTheme.shadows.buttonElevation
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

/**
 * Badge de statut adaptatif personnalisé selon le thème.
 */
@Composable
fun ThemedBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = KasaTheme.colors.primary,
    textColor: Color = KasaTheme.colors.onPrimary,
    shape: Shape = KasaTheme.shapes.badgeShape
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Carte de prévisualisation visuelle réelle d'un thème pour le sélecteur.
 */
@Composable
fun ThemedPreviewCard(
    themeOption: AppAestheticTheme,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val def = remember(themeOption, isDark) { themeOption.getDefinition(isDark) }
    val colors = def.colors
    val shapes = def.shapes
    val borders = def.borders

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = shapes.cardShape,
        color = colors.surface,
        border = if (isSelected) {
            BorderStroke(2.dp, colors.primary)
        } else {
            BorderStroke(1.dp, colors.border)
        },
        shadowElevation = if (isSelected) 6.dp else 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Emoji + Title + Active Check / Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(shapes.small)
                            .background(colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(def.decorations.emoji, fontSize = 24.sp)
                    }

                    Column {
                        Text(
                            text = def.decorations.name,
                            style = def.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = def.decorations.subtitle,
                            style = def.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = colors.primary
                    ) {
                        Text(
                            text = "Actif ✓",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = colors.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Mini Aperçu Réel du Design System (Micro Dashboard Box)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = shapes.medium,
                color = colors.background,
                border = BorderStroke(0.75.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Exemple de budget",
                            style = def.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "1 450,00 $",
                            style = def.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }

                    // Mini Bouton Themé Démo
                    Surface(
                        shape = shapes.buttonShape,
                        color = colors.primary,
                        shadowElevation = def.shadows.buttonElevation
                    ) {
                        Text(
                            text = "Démo",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = colors.onPrimary,
                            style = def.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Aperçu du style d'icônes & de l'icône de launcher associée
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Icônes :",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    val previewIcons = listOf("🏠", "🛒", "💬", "📊")
                    previewIcons.forEachIndexed { index, ic ->
                        val isFirst = index == 0
                        Surface(
                            shape = def.icons.containerShape,
                            color = if (isFirst) def.icons.selectedContainerBackground else def.icons.containerBackground,
                            border = if (isFirst) def.icons.selectedBorder else def.icons.containerBorder,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(ic, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Badge icône launcher associée
                val launcherEmoji = when (def.icons.appIconKey) {
                    "sunset" -> "🌅 Rose"
                    "emerald" -> "🌿 Zen"
                    "default" -> "🏡 3D"
                    "dark" -> "🌙 Cyber"
                    "gold" -> "👑 Or"
                    else -> "📱 App"
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(0.5.dp, colors.border)
                ) {
                    Text(
                        text = "App : $launcherEmoji",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Signature du design system (caractéristiques)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = def.decorations.visualSignature,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )

                // Pastilles de couleurs du thème
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(colors.primary))
                    Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(colors.secondary))
                    Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(colors.accent))
                }
            }
        }
    }
}

private val LuxuryGoldBorder = BorderStroke(
    1.dp,
    Brush.linearGradient(
        listOf(Color(0xFFFBF2B7), Color(0xFFD4AF37), Color(0xFF996515), Color(0xFFFBF2B7))
    )
)

/**
 * Conteneur d'icône (ou d'émoji) qui adopte fidèlement le langage visuel du thème actif.
 * - KAWAII : Bulle marshmallow moelleuse, fond crème/pastel, ombre douce.
 * - JAPANESE : Sceau Hanko/Kamon d'estampe zen, cadre géométrique fin, bordure calligraphique.
 * - ISOMETRIC : Dalle 3D en relief physique avec socle d'extrusion inférieur.
 * - FUTURISTIC : Cockpit cybernétique biseauté, halo néon luminescent, accents HUD.
 * - LUXURY : Médaillon haute joaillerie avec cerclage d'or champagne brossé.
 */
@Composable
fun ThemedIconBadge(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    isSelected: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val icons = KasaTheme.icons
    val colors = KasaTheme.colors
    val isDark = KasaTheme.isDark

    when (icons.styleType) {
        IconStyleType.KAWAII_BUBBLE -> {
            // Marshmallow Soft Bubble
            Surface(
                modifier = modifier.size(size),
                shape = CircleShape,
                color = if (isSelected) icons.selectedContainerBackground else icons.containerBackground,
                border = if (isSelected) icons.selectedBorder else icons.containerBorder,
                shadowElevation = if (isSelected) 4.dp else 1.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                    content = content
                )
            }
        }

        IconStyleType.JAPANESE_STAMP -> {
            // Hanko / Kamon Japanese Stamp
            Surface(
                modifier = modifier.size(size),
                shape = RoundedCornerShape(4.dp),
                color = if (isSelected) icons.selectedContainerBackground else icons.containerBackground,
                border = if (isSelected) icons.selectedBorder else icons.containerBorder,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                    content = content
                )
            }
        }

        IconStyleType.ISOMETRIC_3D -> {
            // Isometric 3D Extruded Tile
            val depth = if (isSelected) 3.dp else 2.dp
            val extrusionColor = if (isDark) Color(0xFF0F172A) else Color(0xFF94A3B8)

            Box(modifier = modifier.size(size + depth)) {
                // Socle d'extrusion 3D inférieur
                Box(
                    modifier = Modifier
                        .size(size)
                        .offset(y = depth)
                        .clip(RoundedCornerShape(8.dp))
                        .background(extrusionColor)
                )

                // Dalle isométrique supérieure
                Surface(
                    modifier = Modifier.size(size),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) icons.selectedContainerBackground else icons.containerBackground,
                    border = if (isSelected) icons.selectedBorder else icons.containerBorder,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                        content = content
                    )
                }
            }
        }

        IconStyleType.FUTURISTIC_HUD -> {
            // Futuristic Cybernetic HUD Frame with Angled Cut Corners
            val hudShape = CutCornerShape(7.dp)
            val glowBorder = if (isSelected) {
                BorderStroke(1.5.dp, colors.primary)
            } else {
                BorderStroke(1.dp, colors.border)
            }

            Surface(
                modifier = modifier
                    .size(size)
                    .then(
                        if (isSelected) Modifier.shadow(6.dp, hudShape, ambientColor = colors.primary, spotColor = colors.primary)
                        else Modifier
                    ),
                shape = hudShape,
                color = if (isSelected) icons.selectedContainerBackground else icons.containerBackground,
                border = glowBorder
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                    content = content
                )
            }
        }

        IconStyleType.LUXURY_BEZEL -> {
            // High Jewelry Gold Medallion
            Surface(
                modifier = modifier.size(size),
                shape = CircleShape,
                color = if (isSelected) icons.selectedContainerBackground else icons.containerBackground,
                border = if (isSelected) LuxuryGoldBorder else icons.containerBorder,
                shadowElevation = if (isSelected) 3.dp else 1.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                    content = content
                )
            }
        }
    }
}

/**
 * Composant d'icône dédié à la barre de navigation inférieure (BottomBar).
 * Met en valeur l'onglet sélectionné avec l'identité morphologique du thème et gère l'animation.
 */
@Composable
fun ThemedNavIcon(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    badge: (@Composable BoxScope.() -> Unit)? = null,
    icon: @Composable () -> Unit
) {
    val icons = KasaTheme.icons

    val iconBox: @Composable () -> Unit = {
        ThemedIconBadge(
            size = if (isSelected) 36.dp else 32.dp,
            isSelected = isSelected
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (isSelected) icons.selectedIconTint else icons.iconTint
            ) {
                icon()
            }
        }
    }

    if (badge != null) {
        BadgedBox(badge = badge) {
            iconBox()
        }
    } else {
        iconBox()
    }
}

/**
 * Bouton d'action supérieur (TopAppBar / Actions) habillé avec le design d'icône du thème actif.
 */
@Composable
fun ThemedActionIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        ThemedIconBadge(
            size = 36.dp,
            isSelected = false
        ) {
            CompositionLocalProvider(
                LocalContentColor provides KasaTheme.icons.iconTint
            ) {
                content()
            }
        }
    }
}
