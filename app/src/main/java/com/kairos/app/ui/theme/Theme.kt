package com.kairos.app.ui.theme

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class ThemeMode { System, Light, Dark }

/** True when the person turned animations off system-wide, or inside Kairos. */
val LocalReduceMotion = staticCompositionLocalOf { false }

@Composable
fun KairosTheme(
    mode: ThemeMode = ThemeMode.System,
    reduceMotionPref: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val k = if (dark) DarkKairos else LightKairos
    val context = LocalContext.current
    val systemReduce = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val scheme = if (dark) {
        darkColorScheme(
            primary = k.lagoon, onPrimary = k.onLagoon, background = k.paper, onBackground = k.ink,
            surface = k.paperDeep, onSurface = k.ink, onSurfaceVariant = k.inkSoft,
            secondary = k.washes.mint, tertiary = k.washes.lilac, error = k.danger,
            surfaceContainerHigh = k.paperDeep, outline = k.inkFaint,
        )
    } else {
        lightColorScheme(
            primary = k.lagoon, onPrimary = k.onLagoon, background = k.paper, onBackground = k.ink,
            surface = k.paper, onSurface = k.ink, onSurfaceVariant = k.inkSoft,
            secondary = k.washes.mint, tertiary = k.washes.lilac, error = k.danger,
            surfaceContainerHigh = k.paperDeep, outline = k.inkFaint,
        )
    }
    CompositionLocalProvider(
        LocalKairosColors provides k,
        LocalReduceMotion provides (systemReduce || reduceMotionPref),
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = KairosTypography,
            shapes = Shapes(
                small = RoundedCornerShape(14.dp),
                medium = RoundedCornerShape(22.dp),
                large = RoundedCornerShape(28.dp),
                extraLarge = RoundedCornerShape(36.dp),
            ),
            content = content,
        )
    }
}

object Kairos {
    val colors: KairosColors
        @Composable @ReadOnlyComposable get() = LocalKairosColors.current
    val reduceMotion: Boolean
        @Composable @ReadOnlyComposable get() = LocalReduceMotion.current
}
