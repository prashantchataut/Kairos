package com.kairos.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Kairos Material theme.
 *
 * Dynamic color is opt-in so the editorial identity remains stable by default.
 * Legacy theme aliases are retained while persisted identifiers and older screens
 * migrate without breaking existing installations.
 */

private val LightColorScheme = lightColorScheme(
    primary = KairosPrimary,
    onPrimary = KairosTextOnPrimaryLight,
    primaryContainer = KairosIndigoContainerLight,
    onPrimaryContainer = KairosOnIndigoContainerLight,

    secondary = KairosClay,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = KairosClayContainerLight,
    onSecondaryContainer = KairosOnClayContainerLight,

    tertiary = Color(0xFF0090B2),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD2F2FF),
    onTertiaryContainer = Color(0xFF004E63),

    background = KairosBackgroundLight,
    onBackground = KairosTextPrimaryLight,
    surface = KairosSurfaceLight,
    onSurface = KairosTextPrimaryLight,
    surfaceVariant = KairosSurfaceVariantLight,
    onSurfaceVariant = KairosTextSecondaryLight,

    surfaceDim = Color(0xFFE3E8F3),
    surfaceBright = KairosSurfaceLight,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F5FB),
    surfaceContainer = Color(0xFFEDF1F9),
    surfaceContainerHigh = Color(0xFFE7ECF7),
    surfaceContainerHighest = Color(0xFFE0E6F2),

    error = KairosError,
    onError = KairosOnError,
    errorContainer = KairosErrorContainer,
    onErrorContainer = Color(0xFF7A1E18),

    outline = KairosOutlineLight,
    outlineVariant = KairosDividerLight,

    inverseSurface = Color(0xFF1E2636),
    inverseOnSurface = Color(0xFFE9EDF6),
    inversePrimary = Color(0xFF8FA6FF),

    scrim = Scrim,
    surfaceTint = KairosPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = KairosPrimaryDark,
    onPrimary = KairosTextOnPrimaryDark,
    primaryContainer = KairosIndigoContainerDark,
    onPrimaryContainer = KairosOnIndigoContainerDark,

    secondary = KairosSoftClay,
    onSecondary = Color(0xFF3A1606),
    secondaryContainer = KairosClayContainerDark,
    onSecondaryContainer = KairosOnClayContainerDark,

    tertiary = Color(0xFF5FD0E8),
    onTertiary = Color(0xFF003642),
    tertiaryContainer = Color(0xFF00536B),
    onTertiaryContainer = Color(0xFFB8EDFB),

    background = KairosBackgroundDark,
    onBackground = KairosTextPrimaryDark,
    surface = KairosSurfaceDark,
    onSurface = KairosTextPrimaryDark,
    surfaceVariant = KairosSurfaceVariantDark,
    onSurfaceVariant = KairosTextSecondaryDark,

    surfaceDim = Color(0xFF0B0E15),
    surfaceBright = Color(0xFF242C3D),
    surfaceContainerLowest = Color(0xFF080B11),
    surfaceContainerLow = Color(0xFF141A25),
    surfaceContainer = Color(0xFF1A2130),
    surfaceContainerHigh = Color(0xFF1F2837),
    surfaceContainerHighest = Color(0xFF242E3F),

    error = Color(0xFFFF8A80),
    onError = Color(0xFF5C140E),
    errorContainer = KairosErrorContainerDark,
    onErrorContainer = Color(0xFFFFDAD6),

    outline = KairosOutlineDark,
    outlineVariant = KairosDividerDark,

    inverseSurface = Color(0xFFE9EDF6),
    inverseOnSurface = Color(0xFF101828),
    inversePrimary = Color(0xFF2E5BFF),

    scrim = Scrim,
    surfaceTint = KairosPrimaryDark
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

@Composable
fun isDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
fun KairosTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            activity?.let {
                val insetsController = WindowCompat.getInsetsController(it.window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val havenColors = if (darkTheme) DarkHavenColors else LightHavenColors
    val kairosGlassColors = if (darkTheme) DarkKairosGlassColors else LightKairosGlassColors
    val liquidGlassColors = if (darkTheme) DarkKairosLiquidGlassColors else LightKairosLiquidGlassColors

    CompositionLocalProvider(
        LocalKairosGlassColors provides kairosGlassColors,
        LocalKairosLiquidGlassColors provides liquidGlassColors,
        LocalHavenColors provides havenColors,
        LocalStreakColors provides if (darkTheme) DarkStreakColors else LightStreakColors,
        LocalMoodColors provides if (darkTheme) DarkMoodColors else LightMoodColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KairosTypography,
            shapes = KairosShapes,
            content = content
        )
    }
}

@Composable
fun getTextPrimary(): Color {
    return if (isDarkTheme()) KairosTextPrimaryDark else KairosTextPrimaryLight
}