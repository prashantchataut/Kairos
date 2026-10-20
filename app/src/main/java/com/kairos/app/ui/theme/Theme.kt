package com.kairos.app.ui.theme

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/** True when the user (or the system's "remove animations" switch) asked for calmer motion. */
val LocalReducedMotion = staticCompositionLocalOf { false }

object Kairos {
    val colors: KairosColors
        @Composable @ReadOnlyComposable get() = LocalKairosColors.current
    val type: KairosType
        @Composable @ReadOnlyComposable get() = LocalKairosType.current
    val reducedMotion: Boolean
        @Composable @ReadOnlyComposable get() = LocalReducedMotion.current
}

enum class ThemeMode { System, Light, Dark }

@Composable
fun KairosTheme(
    mode: ThemeMode = ThemeMode.System,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val colors = if (dark) DarkColors else LightColors
    val context = LocalContext.current
    val systemReduced = remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
    val type = remember { KairosType() }

    val scheme = if (dark) {
        darkColorScheme(
            primary = colors.pond, onPrimary = colors.onPond,
            secondary = colors.pondDeep, background = colors.paper, onBackground = colors.ink,
            surface = colors.paper, onSurface = colors.ink, surfaceVariant = colors.paperDeep,
            onSurfaceVariant = colors.inkSoft, outline = colors.inkFaint, error = colors.danger,
            surfaceContainerHigh = colors.paperDeep, surfaceContainer = colors.paperDeep,
            surfaceContainerHighest = colors.paperDeep,
        )
    } else {
        lightColorScheme(
            primary = colors.pond, onPrimary = colors.onPond,
            secondary = colors.pondDeep, background = colors.paper, onBackground = colors.ink,
            surface = colors.paper, onSurface = colors.ink, surfaceVariant = colors.paperDeep,
            onSurfaceVariant = colors.inkSoft, outline = colors.inkFaint, error = colors.danger,
            surfaceContainerHigh = colors.paperDeep, surfaceContainer = colors.paperDeep,
            surfaceContainerHighest = colors.paperDeep,
        )
    }
    val m3Type = Typography(
        displayLarge = type.hero, displayMedium = type.display, headlineMedium = type.title,
        titleLarge = type.heading, titleMedium = type.label, bodyLarge = type.bodyRegular,
        bodyMedium = type.body, labelLarge = type.label, labelMedium = type.caption, labelSmall = type.caption,
    )

    CompositionLocalProvider(
        LocalKairosColors provides colors,
        LocalKairosType provides type,
        LocalReducedMotion provides (reduceMotion || systemReduced),
    ) {
        MaterialTheme(colorScheme = scheme, typography = m3Type, content = content)
    }
}
