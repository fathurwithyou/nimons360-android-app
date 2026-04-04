package com.eggheadengineers.nimons360.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    // Primary
    primary                = Primary,
    onPrimary              = OnPrimary,
    primaryContainer       = PrimaryLight,
    onPrimaryContainer     = PrimaryDark,
    inversePrimary         = InversePrimary,

    // Secondary
    secondary              = Accent,
    onSecondary            = OnAccent,
    secondaryContainer     = AccentLight,
    onSecondaryContainer   = OnAccentContainer,

    // Tertiary
    tertiary               = Tertiary,
    onTertiary             = OnTertiary,
    tertiaryContainer      = TertiaryContainer,
    onTertiaryContainer    = OnTertiaryContainer,

    // Error
    error                  = ErrorColor,
    onError                = OnError,
    errorContainer         = ErrorContainer,
    onErrorContainer       = OnErrorContainer,

    // Background
    background             = Background,
    onBackground           = OnBackground,

    // Surface hierarchy
    surface                = Surface,
    onSurface              = OnSurface,
    surfaceVariant         = SurfaceVariant,
    onSurfaceVariant       = OnSurfaceVariant,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow    = SurfaceContainerLow,
    surfaceContainer       = SurfaceContainer,
    surfaceContainerHigh   = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,

    // Inverse
    inverseSurface         = InverseSurface,
    inverseOnSurface       = InverseOnSurface,

    // Outline: use outlineVariant at 15% opacity for ghost borders
    outline                = Outline,
    outlineVariant         = OutlineVariant,

    scrim                  = Scrim,
)

@Composable
fun Nimons360Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = AppTypography,
        content     = content,
    )
}