package com.example.kasa.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kasa.data.repository.KasaStorage
import com.example.kasa.util.AppIconManager
import com.example.kasa.theme.tokens.FuturisticTheme
import com.example.kasa.theme.tokens.IsometricTheme
import com.example.kasa.theme.tokens.JapaneseTheme
import com.example.kasa.theme.tokens.KawaiiTheme
import com.example.kasa.theme.tokens.KasaThemeDefinition
import com.example.kasa.theme.tokens.LuxuryTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val title: String, val icon: String) {
    SYSTEM("Système", "🌓"),
    LIGHT("Clair", "☀️"),
    DARK("Sombre", "🌙")
}

enum class UiCornerStyle(val title: String, val cardRadius: Dp, val buttonRadius: Dp, val icon: String) {
    ROUNDED("Doux & Arrondi", 18.dp, 24.dp, "🫧"),
    SHARP("Épuré & Sharp", 8.dp, 10.dp, "📏")
}

enum class ChatWallpaper(val title: String, val subtitle: String, val icon: String) {
    MINIMAL("Épuré", "Fond uni adaptatif", "🌟"),
    DOODLES("Doodles Coloc", "Motifs de maison & courses", "🍕"),
    GEOMETRIC("Constellation", "Grille géométrique discrète", "📐"),
    AURORA("Aurore Boréale", "Dégradé atmosphérique fluide", "🌌"),
    STARRY("Nuit Étoilée", "Ciel étoilé profond", "✨")
}

/**
 * Les 5 thèmes officiels du Design System KASA.
 * Chaque thème propose une identité visuelle complète (tokens, formes, typographie, ombres, bordures et motion).
 */
enum class AppAestheticTheme(
    val title: String,
    val subtitle: String,
    val previewEmoji: String
) {
    KAWAII(
        title = "Kawaii Design",
        subtitle = "Pastel doux & Formes marshmallow",
        previewEmoji = "🌸"
    ),

    JAPANESE(
        title = "Japanese Style",
        subtitle = "Minimalisme Zen, Washi & Ma",
        previewEmoji = "🎌"
    ),

    ISOMETRIC(
        title = "Isometric Design",
        subtitle = "Profondeur 3D, Dalles & Reliefs",
        previewEmoji = "🔷"
    ),

    FUTURISTIC(
        title = "Futuristic UI",
        subtitle = "HUD 2026, Spatial & Verre Dépoli",
        previewEmoji = "🚀"
    ),

    LUXURY(
        title = "Luxury Design",
        subtitle = "Quiet Luxury, Or Champagne & Onyx",
        previewEmoji = "💎"
    );

    fun getDefinition(isDark: Boolean): KasaThemeDefinition {
        return when (this) {
            KAWAII -> KawaiiTheme.create(isDark)
            JAPANESE -> JapaneseTheme.create(isDark)
            ISOMETRIC -> IsometricTheme.create(isDark)
            FUTURISTIC -> FuturisticTheme.create(isDark)
            LUXURY -> LuxuryTheme.create(isDark)
        }
    }

    // Propriétés de compatibilité directe pour les composants
    val lightPrimary: Color get() = getDefinition(false).colors.primary
    val lightPrimaryDark: Color get() = getDefinition(false).colors.primaryContainer
    val lightPrimaryLight: Color get() = getDefinition(false).colors.onPrimaryContainer
    val lightSecondary: Color get() = getDefinition(false).colors.secondary
    val lightSecondaryDark: Color get() = getDefinition(false).colors.secondaryContainer
    val lightBackground: Color get() = getDefinition(false).colors.background
    val lightSurface: Color get() = getDefinition(false).colors.surface
    val lightSurfaceVariant: Color get() = getDefinition(false).colors.surfaceVariant
    val lightTextPrimary: Color get() = getDefinition(false).colors.onBackground
    val lightTextSecondary: Color get() = getDefinition(false).colors.onSurfaceVariant
    val lightOutline: Color get() = getDefinition(false).colors.border

    val darkPrimary: Color get() = getDefinition(true).colors.primary
    val darkPrimaryDark: Color get() = getDefinition(true).colors.primaryContainer
    val darkPrimaryLight: Color get() = getDefinition(true).colors.onPrimaryContainer
    val darkSecondary: Color get() = getDefinition(true).colors.secondary
    val darkSecondaryDark: Color get() = getDefinition(true).colors.secondaryContainer
    val darkBackground: Color get() = getDefinition(true).colors.background
    val darkSurface: Color get() = getDefinition(true).colors.surface
    val darkSurfaceVariant: Color get() = getDefinition(true).colors.surfaceVariant
    val darkTextPrimary: Color get() = getDefinition(true).colors.onBackground
    val darkTextSecondary: Color get() = getDefinition(true).colors.onSurfaceVariant
    val darkOutline: Color get() = getDefinition(true).colors.border
}

object ThemeManager {
    private var storage: KasaStorage? = null

    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _aestheticTheme = MutableStateFlow(AppAestheticTheme.KAWAII)
    val aestheticTheme: StateFlow<AppAestheticTheme> = _aestheticTheme.asStateFlow()

    private val _chatWallpaper = MutableStateFlow(ChatWallpaper.MINIMAL)
    val chatWallpaper: StateFlow<ChatWallpaper> = _chatWallpaper.asStateFlow()

    private val _cornerStyle = MutableStateFlow(UiCornerStyle.ROUNDED)
    val cornerStyle: StateFlow<UiCornerStyle> = _cornerStyle.asStateFlow()

    fun init(context: Context) {
        val s = KasaStorage(context)
        storage = s

        try {
            val savedMode = s.getThemeMode()
            _themeMode.value = AppThemeMode.valueOf(savedMode)
        } catch (e: Exception) {
            _themeMode.value = AppThemeMode.SYSTEM
        }

        try {
            val savedTheme = s.getAestheticTheme()
            _aestheticTheme.value = when (savedTheme) {
                "KAWAII" -> AppAestheticTheme.KAWAII
                "JAPANESE" -> AppAestheticTheme.JAPANESE
                "ISOMETRIC" -> AppAestheticTheme.ISOMETRIC
                "FUTURISTIC" -> AppAestheticTheme.FUTURISTIC
                "LUXURY" -> AppAestheticTheme.LUXURY
                // Mappings de migration transparente :
                "SAKURA" -> AppAestheticTheme.KAWAII
                "EMERALD", "BOREAL_GOLD" -> AppAestheticTheme.JAPANESE
                "AMOLED_NEON", "DEEP_OCEAN" -> AppAestheticTheme.FUTURISTIC
                "CAFE_LATTE", "SUNSET" -> AppAestheticTheme.LUXURY
                else -> AppAestheticTheme.KAWAII
            }
        } catch (e: Exception) {
            _aestheticTheme.value = AppAestheticTheme.KAWAII
        }

        try {
            val savedWallpaper = s.getChatWallpaper()
            _chatWallpaper.value = ChatWallpaper.valueOf(savedWallpaper)
        } catch (e: Exception) {
            _chatWallpaper.value = ChatWallpaper.MINIMAL
        }

        try {
            val savedStyle = s.getCornerStyle()
            _cornerStyle.value = UiCornerStyle.valueOf(savedStyle)
        } catch (e: Exception) {
            _cornerStyle.value = UiCornerStyle.ROUNDED
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        storage?.setThemeMode(mode.name)
    }

    fun setAestheticTheme(theme: AppAestheticTheme, context: Context? = null, syncAppIcon: Boolean = false) {
        _aestheticTheme.value = theme
        storage?.setAestheticTheme(theme.name)
        // Activity-alias switching disabled during in-app theme changes to prevent Android from killing the app process
    }

    fun getNextAestheticTheme(): AppAestheticTheme {
        val themes = AppAestheticTheme.entries
        val currentIndex = themes.indexOf(_aestheticTheme.value)
        val nextIndex = if (currentIndex in 0 until themes.size - 1) currentIndex + 1 else 0
        return themes[nextIndex]
    }

    fun cycleAestheticTheme(context: Context? = null, syncAppIcon: Boolean = false): AppAestheticTheme {
        val nextTheme = getNextAestheticTheme()
        setAestheticTheme(nextTheme, context, syncAppIcon = false)
        return nextTheme
    }

    fun setChatWallpaper(wallpaper: ChatWallpaper) {
        _chatWallpaper.value = wallpaper
        storage?.setChatWallpaper(wallpaper.name)
    }

    fun setCornerStyle(style: UiCornerStyle) {
        _cornerStyle.value = style
        storage?.setCornerStyle(style.name)
    }

    fun getThemeDefinition(theme: AppAestheticTheme, isDark: Boolean): KasaThemeDefinition {
        return theme.getDefinition(isDark)
    }
}
