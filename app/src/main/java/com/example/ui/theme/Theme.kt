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
    val lightSubtitle: String,
    val previewCanvas: Color,
    val previewSurface: Color,
    val previewAccent: Color,
    val lightPreviewCanvas: Color,
    val lightPreviewSurface: Color,
    val lightPreviewAccent: Color,
    val prefersLightMode: Boolean = false
) {
    MIDNIGHT_WOLF(
        displayName = "Midnight Wolf",
        subtitle = "Navy, Silver & Gold",
        lightSubtitle = "Porcelain White, Navy & Gold",
        previewCanvas = DeepMidnightNavy,
        previewSurface = NavySurfaceElevated,
        previewAccent = BrightGold,
        lightPreviewCanvas = LightAcademicBg,
        lightPreviewSurface = LightAcademicSurfaceVariant,
        lightPreviewAccent = NavySurfaceHigh
    ),
    OBSIDIAN_GOLD(
        displayName = "Obsidian Gold",
        subtitle = "Obsidian, Platinum & Gold",
        lightSubtitle = "Warm Ivory, Cream & Sovereign Gold",
        previewCanvas = ObsidianBg,
        previewSurface = ObsidianElevated,
        previewAccent = ChampagneGold,
        lightPreviewCanvas = IvoryBg,
        lightPreviewSurface = IvorySurfaceVariant,
        lightPreviewAccent = IvoryGoldAccent
    ),
    ARCTIC_TITANIUM(
        displayName = "Arctic Titanium",
        subtitle = "Steel Navy & Ice Silver",
        lightSubtitle = "Glacier Sky, Ice White & Sapphire",
        previewCanvas = ArcticBg,
        previewSurface = ArcticElevated,
        previewAccent = TitaniumCyan,
        lightPreviewCanvas = SkyBg,
        lightPreviewSurface = SkySurfaceVariant,
        lightPreviewAccent = SkyAccent
    ),
    EMERALD_SCHOLAR(
        displayName = "Emerald Scholar",
        subtitle = "Forest Navy & Emerald",
        lightSubtitle = "Soft Mint, Parchment & Jade",
        previewCanvas = EmeraldBg,
        previewSurface = EmeraldElevated,
        previewAccent = EmeraldSuccess,
        lightPreviewCanvas = MintBg,
        lightPreviewSurface = MintSurfaceVariant,
        lightPreviewAccent = MintAccent
    ),
    PEARL_IVORY(
        displayName = "Pearl Ivory (Light)",
        subtitle = "Warm Cream, Pearl & Bronze Gold",
        lightSubtitle = "Warm Cream, Pearl & Bronze Gold",
        previewCanvas = Color(0xFF1D1810),
        previewSurface = Color(0xFF2D2518),
        previewAccent = BrightGold,
        lightPreviewCanvas = IvoryBg,
        lightPreviewSurface = IvoryHigh,
        lightPreviewAccent = IvoryGoldAccent,
        prefersLightMode = true
    ),
    PLATINUM_SKY(
        displayName = "Platinum Sky (Light)",
        subtitle = "Crisp Platinum, Silver & Royal Blue",
        lightSubtitle = "Crisp Platinum, Silver & Royal Blue",
        previewCanvas = Color(0xFF12192E),
        previewSurface = Color(0xFF1F2B4D),
        previewAccent = TitaniumCyan,
        lightPreviewCanvas = PlatinumBg,
        lightPreviewSurface = PlatinumHigh,
        lightPreviewAccent = PlatinumAccent,
        prefersLightMode = true
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

private val PearlIvoryDarkScheme = darkColorScheme(
    primary = BrightGold,
    onPrimary = OnGold,
    primaryContainer = GoldContainer,
    onPrimaryContainer = BrightGold,
    secondary = Color(0xFFE5DAC1),
    onSecondary = Color(0xFF1D1810),
    secondaryContainer = Color(0xFF332B1D),
    onSecondaryContainer = CrispWhite,
    tertiary = BurnishedGold,
    onTertiary = OnGold,
    background = Color(0xFF17130C),
    onBackground = CrispWhite,
    surface = Color(0xFF211C13),
    onSurface = CrispWhite,
    surfaceVariant = Color(0xFF2B2418),
    onSurfaceVariant = Color(0xFFD8CEB8),
    surfaceContainerLowest = Color(0xFF17130C),
    surfaceContainerLow = Color(0xFF211C13),
    surfaceContainer = Color(0xFF2B2418),
    surfaceContainerHigh = Color(0xFF382F20),
    surfaceContainerHighest = Color(0xFF453B29),
    outline = Color(0xFF5C4E37),
    error = CoralError,
    onError = Color(0xFF17130C)
)

private val PlatinumSkyDarkScheme = darkColorScheme(
    primary = TitaniumCyan,
    onPrimary = OnTitanium,
    primaryContainer = TitaniumContainer,
    onPrimaryContainer = TitaniumCyan,
    secondary = PlatinumSilver,
    onSecondary = Color(0xFF0D1426),
    secondaryContainer = Color(0xFF212E52),
    onSecondaryContainer = CrispWhite,
    tertiary = BrightGold,
    onTertiary = OnGold,
    background = Color(0xFF0B1122),
    onBackground = CrispWhite,
    surface = Color(0xFF121B33),
    onSurface = CrispWhite,
    surfaceVariant = Color(0xFF1A2545),
    onSurfaceVariant = PlatinumSilver,
    surfaceContainerLowest = Color(0xFF0B1122),
    surfaceContainerLow = Color(0xFF121B33),
    surfaceContainer = Color(0xFF1A2545),
    surfaceContainerHigh = Color(0xFF223059),
    surfaceContainerHighest = Color(0xFF2B3C6E),
    outline = Color(0xFF3B4F8A),
    error = CoralError,
    onError = Color(0xFF0B1122)
)

private fun lightSchemeForPreset(preset: WolfThemePreset): ColorScheme = when (preset) {
    WolfThemePreset.MIDNIGHT_WOLF -> lightColorScheme(
        primary = NavySurfaceHigh,
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

    WolfThemePreset.OBSIDIAN_GOLD,
    WolfThemePreset.PEARL_IVORY -> lightColorScheme(
        primary = IvoryGoldAccent,
        onPrimary = CrispWhite,
        primaryContainer = Color(0xFFFCEFC7),
        onPrimaryContainer = Color(0xFF3D2A00),
        secondary = Color(0xFF5A4E38),
        onSecondary = CrispWhite,
        secondaryContainer = IvorySurfaceVariant,
        onSecondaryContainer = IvoryText,
        tertiary = BurnishedGold,
        onTertiary = OnGold,
        background = IvoryBg,
        onBackground = IvoryText,
        surface = IvorySurface,
        onSurface = IvoryText,
        surfaceVariant = IvorySurfaceVariant,
        onSurfaceVariant = Color(0xFF5F5542),
        surfaceContainerLowest = IvorySurface,
        surfaceContainerLow = IvoryBg,
        surfaceContainer = IvorySurfaceVariant,
        surfaceContainerHigh = IvoryHigh,
        surfaceContainerHighest = Color(0xFFDDD1B8),
        outline = IvoryOutline,
        outlineVariant = Color(0xFFE5DCC8),
        error = Color(0xFFDC2626),
        onError = CrispWhite
    )

    WolfThemePreset.ARCTIC_TITANIUM -> lightColorScheme(
        primary = SkyAccent,
        onPrimary = CrispWhite,
        primaryContainer = Color(0xFFD6EEFF),
        onPrimaryContainer = Color(0xFF062E4A),
        secondary = Color(0xFF335C7D),
        onSecondary = CrispWhite,
        secondaryContainer = SkySurfaceVariant,
        onSecondaryContainer = Color(0xFF0A2236),
        tertiary = BurnishedGold,
        onTertiary = OnGold,
        background = SkyBg,
        onBackground = Color(0xFF0A2236),
        surface = SkySurface,
        onSurface = Color(0xFF0A2236),
        surfaceVariant = SkySurfaceVariant,
        onSurfaceVariant = Color(0xFF3C5C78),
        surfaceContainerLowest = SkySurface,
        surfaceContainerLow = SkyBg,
        surfaceContainer = SkySurfaceVariant,
        surfaceContainerHigh = SkyHigh,
        surfaceContainerHighest = Color(0xFFC0DCEF),
        outline = SkyOutline,
        outlineVariant = Color(0xFFD7E8F5),
        error = Color(0xFFDC2626),
        onError = CrispWhite
    )

    WolfThemePreset.EMERALD_SCHOLAR -> lightColorScheme(
        primary = MintAccent,
        onPrimary = CrispWhite,
        primaryContainer = Color(0xFFD1FAE5),
        onPrimaryContainer = Color(0xFF023B29),
        secondary = Color(0xFF386656),
        onSecondary = CrispWhite,
        secondaryContainer = MintSurfaceVariant,
        onSecondaryContainer = Color(0xFF06291E),
        tertiary = LightGoldPrimary,
        onTertiary = CrispWhite,
        background = MintBg,
        onBackground = Color(0xFF06291E),
        surface = MintSurface,
        onSurface = Color(0xFF06291E),
        surfaceVariant = MintSurfaceVariant,
        onSurfaceVariant = Color(0xFF365E51),
        surfaceContainerLowest = MintSurface,
        surfaceContainerLow = MintBg,
        surfaceContainer = MintSurfaceVariant,
        surfaceContainerHigh = MintHigh,
        surfaceContainerHighest = Color(0xFFB7DDD0),
        outline = MintOutline,
        outlineVariant = Color(0xFFD3ECE3),
        error = Color(0xFFDC2626),
        onError = CrispWhite
    )

    WolfThemePreset.PLATINUM_SKY -> lightColorScheme(
        primary = PlatinumAccent,
        onPrimary = CrispWhite,
        primaryContainer = Color(0xFFDFE6FF),
        onPrimaryContainer = Color(0xFF102052),
        secondary = Color(0xFF536185),
        onSecondary = CrispWhite,
        secondaryContainer = PlatinumSurfaceVariant,
        onSecondaryContainer = Color(0xFF11182E),
        tertiary = LightGoldPrimary,
        onTertiary = CrispWhite,
        background = PlatinumBg,
        onBackground = Color(0xFF11182E),
        surface = PlatinumSurface,
        onSurface = Color(0xFF11182E),
        surfaceVariant = PlatinumSurfaceVariant,
        onSurfaceVariant = Color(0xFF4C5775),
        surfaceContainerLowest = PlatinumSurface,
        surfaceContainerLow = PlatinumBg,
        surfaceContainer = PlatinumSurfaceVariant,
        surfaceContainerHigh = PlatinumHigh,
        surfaceContainerHighest = Color(0xFFC8D0EA),
        outline = PlatinumOutline,
        outlineVariant = Color(0xFFDCE1F0),
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
            WolfThemePreset.PEARL_IVORY -> PearlIvoryDarkScheme
            WolfThemePreset.PLATINUM_SKY -> PlatinumSkyDarkScheme
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
