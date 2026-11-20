package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = OnEmeraldPrimary,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = OnEmeraldPrimaryContainer,
    secondary = SlateSecondary,
    onSecondary = OnSlateSecondary,
    secondaryContainer = SlateSecondaryContainer,
    onSecondaryContainer = OnSlateSecondaryContainer,
    tertiary = GoldTertiary,
    onTertiary = OnGoldTertiary,
    tertiaryContainer = GoldTertiaryContainer,
    onTertiaryContainer = OnGoldTertiaryContainer,
    background = AlabasterBackground,
    onBackground = ObsidianOnBackground,
    surface = SurfaceWhite,
    onSurface = ObsidianOnBackground,
    surfaceVariant = SurfaceVariantCool,
    onSurfaceVariant = OnSurfaceVariantSlate,
    outline = OutlineSubtle,
    outlineVariant = OutlineVariantLight,
    error = ErrorCrimson,
    onError = OnErrorWhite,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmeraldPrimary,
    onPrimary = DarkOnEmeraldPrimary,
    primaryContainer = DarkEmeraldPrimaryContainer,
    onPrimaryContainer = DarkOnEmeraldPrimaryContainer,
    secondary = DarkSlateSecondary,
    onSecondary = DarkOnSlateSecondary,
    secondaryContainer = DarkSlateSecondaryContainer,
    onSecondaryContainer = DarkOnSlateSecondaryContainer,
    tertiary = DarkGoldTertiary,
    onTertiary = DarkOnGoldTertiary,
    tertiaryContainer = DarkGoldTertiaryContainer,
    onTertiaryContainer = DarkOnGoldTertiaryContainer,
    background = ObsidianBackground,
    onBackground = IvoryOnBackground,
    surface = ObsidianSurface,
    onSurface = IvoryOnBackground,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

val CardWiseShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CardWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CardWiseTypography,
        shapes = CardWiseShapes,
        content = content
    )
}
