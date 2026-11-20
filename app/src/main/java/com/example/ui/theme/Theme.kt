package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class WolfThemePreset(
    val displayName: String,
    val subtitle: String,
    val previewCanvas: Color,
    val previewSurface: Color,
    val previewAccent: Color
) {
    MIDNIGHT_WOLF(
        displayName = "Midnight Wolf",
        subtitle = "Navy, Silver & Gold",
        previewCanvas = DeepMidnightNavy,
        previewSurface = NavySurfaceElevated,
        previewAccent = BrightGold
    ),
    OBSIDIAN_GOLD(
        displayName = "Obsidian Gold",
        subtitle = "Obsidian, Platinum & Gold",
        previewCanvas = ObsidianBg,
        previewSurface = ObsidianElevated,
        previewAccent = ChampagneGold
    ),
    ARCTIC_TITANIUM(
        displayName = "Arctic Titanium",
        subtitle = "Steel Navy & Ice Silver",
        previewCanvas = ArcticBg,
        previewSurface = ArcticElevated,
        previewAccent = TitaniumCyan
    ),
    EMERALD_SCHOLAR(
        displayName = "Emerald Scholar",
        subtitle = "Forest Navy & Emerald",
        previewCanvas = EmeraldBg,
        previewSurface = EmeraldElevated,
        previewAccent = EmeraldSuccess
    )
}

private val MidnightWolfDarkScheme = darkColorScheme(
    primary = BrightGold,
    onPrimary = OnGold,
    primaryContainer = GoldContainer,
    onPrimaryContainer = BrightGold,
    secondary = BrushedSilver,
    onSecondary = DeepMidnightNavy,
    secondaryContainer = NavyAccentContainer,
    onSecondaryContainer = CrispWhite,
    tertiary = BurnishedGold,
    onTertiary = OnGold,
    tertiaryContainer = NavySurfaceHigh,
    onTertiaryContainer = SilverAccent,
    background = DeepMidnightNavy,
    onBackground = CrispWhite,
    surface = NavyCanvas,
    onSurface = CrispWhite,
    surfaceVariant = NavySurface,
    onSurfaceVariant = BrushedSilver,
    surfaceContainerLowest = DeepMidnightNavy,
    surfaceContainerLow = NavyCanvas,
    surfaceContainer = NavySurface,
    surfaceContainerHigh = NavySurfaceElevated,
    surfaceContainerHighest = NavySurfaceHigh,
    outline = NavyOutline,
    outlineVariant = Color(0xFF1D3152),
    error = CoralError,
    onError = DeepMidnightNavy
)

private val ObsidianGoldDarkScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = OnGold,
    primaryContainer = ChampagneContainer,
    onPrimaryContainer = ChampagneGold,
    secondary = PlatinumSilver,
    onSecondary = ObsidianBg,
    secondaryContainer = ObsidianHigh,
    onSecondaryContainer = CrispWhite,
    tertiary = BrightGold,
    onTertiary = OnGold,
    tertiaryContainer = ObsidianElevated,
    onTertiaryContainer = PlatinumSilver,
    background = ObsidianBg,
    onBackground = CrispWhite,
    surface = Color(0xFF0B0E17),
    onSurface = CrispWhite,
    surfaceVariant = ObsidianSurface,
    onSurfaceVariant = PlatinumSilver,
    surfaceContainerLowest = ObsidianBg,
    surfaceContainerLow = Color(0xFF0B0E17),
    surfaceContainer = ObsidianSurface,
    surfaceContainerHigh = ObsidianElevated,
    surfaceContainerHighest = ObsidianHigh,
    outline = ObsidianOutline,
    outlineVariant = Color(0xFF232B3D),
    error = CoralError,
    onError = ObsidianBg
)

private val ArcticTitaniumDarkScheme = darkColorScheme(
    primary = TitaniumCyan,
    onPrimary = OnTitanium,
    primaryContainer = TitaniumContainer,
    onPrimaryContainer = TitaniumCyan,
    secondary = BrushedSilver,
    onSecondary = ArcticBg,
    secondaryContainer = ArcticHigh,
    onSecondaryContainer = CrispWhite,
    tertiary = BrightGold,
    onTertiary = OnGold,
    tertiaryContainer = ArcticElevated,
    onTertiaryContainer = SilverAccent,
    background = ArcticBg,
    onBackground = CrispWhite,
    surface = Color(0xFF0B1A2C),
    onSurface = CrispWhite,
    surfaceVariant = ArcticSurface,
    onSurfaceVariant = BrushedSilver,
    surfaceContainerLowest = ArcticBg,
    surfaceContainerLow = Color(0xFF0B1A2C),
    surfaceContainer = ArcticSurface,
    surfaceContainerHigh = ArcticElevated,
    surfaceContainerHighest = ArcticHigh,
    outline = ArcticOutline,
    outlineVariant = Color(0xFF1E3755),
    error = CoralError,
    onError = ArcticBg
)

private val EmeraldScholarDarkScheme = darkColorScheme(
    primary = EmeraldSuccess,
    onPrimary = OnEmerald,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldSuccess,
    secondary = Color(0xFFC8E0DB),
    onSecondary = EmeraldBg,
    secondaryContainer = EmeraldHigh,
    onSecondaryContainer = CrispWhite,
    tertiary = BrightGold,
    onTertiary = OnGold,
    tertiaryContainer = EmeraldElevated,
    onTertiaryContainer = CrispWhite,
    background = EmeraldBg,
    onBackground = CrispWhite,
    surface = Color(0xFF081C1F),
    onSurface = CrispWhite,
    surfaceVariant = EmeraldSurface,
    onSurfaceVariant = Color(0xFFB7D1CC),
    surfaceContainerLowest = EmeraldBg,
    surfaceContainerLow = Color(0xFF081C1F),
    surfaceContainer = EmeraldSurface,
    surfaceContainerHigh = EmeraldElevated,
    surfaceContainerHighest = EmeraldHigh,
    outline = EmeraldOutline,
    outlineVariant = Color(0xFF1B4046),
    error = CoralError,
    onError = EmeraldBg
)

private fun lightSchemeForPreset(preset: WolfThemePreset): ColorScheme {
    val accent = when (preset) {
        WolfThemePreset.MIDNIGHT_WOLF -> NavySurfaceHigh
        WolfThemePreset.OBSIDIAN_GOLD -> Color(0xFF181E2E)
        WolfThemePreset.ARCTIC_TITANIUM -> Color(0xFF0C4A6E)
        WolfThemePreset.EMERALD_SCHOLAR -> Color(0xFF065F46)
    }
    return lightColorScheme(
        primary = accent,
        onPrimary = CrispWhite,
        primaryContainer = Color(0xFFFFF0C2),
        onPrimaryContainer = Color(0xFF3E2D00),
        secondary = LightGoldPrimary,
        onSecondary = CrispWhite,
        secondaryContainer = LightAcademicSurfaceVariant,
        onSecondaryContainer = LightAcademicText,
        tertiary = BurnishedGold,
        onTertiary = OnGold,
        background = LightAcademicBg,
        onBackground = LightAcademicText,
        surface = LightAcademicSurface,
        onSurface = LightAcademicText,
        surfaceVariant = LightAcademicSurfaceVariant,
        onSurfaceVariant = LightAcademicMuted,
        surfaceContainerLowest = CrispWhite,
        surfaceContainerLow = Color(0xFFF8FAFD),
        surfaceContainer = LightAcademicSurfaceVariant,
        surfaceContainerHigh = Color(0xFFDCE4F0),
        surfaceContainerHighest = Color(0xFFD0DAE8),
        outline = LightAcademicOutline,
        outlineVariant = Color(0xFFDCE3EE),
        error = Color(0xFFDC2626),
        onError = CrispWhite
    )
}

val WolfStudyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun WolfStudyTheme(
    darkTheme: Boolean = true,
    preset: WolfThemePreset = WolfThemePreset.MIDNIGHT_WOLF,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        when (preset) {
            WolfThemePreset.MIDNIGHT_WOLF -> MidnightWolfDarkScheme
            WolfThemePreset.OBSIDIAN_GOLD -> ObsidianGoldDarkScheme
            WolfThemePreset.ARCTIC_TITANIUM -> ArcticTitaniumDarkScheme
            WolfThemePreset.EMERALD_SCHOLAR -> EmeraldScholarDarkScheme
        }
    } else {
        lightSchemeForPreset(preset)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WolfStudyTypography,
        shapes = WolfStudyShapes,
        content = content
    )
}
