package com.kairos.app.ui.theme

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.kairos.app.data.prefs.ThemeMode

object Kairos {
    val colors: KairosPalette
        @Composable @ReadOnlyComposable get() = LocalKairosPalette.current
    val type: KairosType
        @Composable @ReadOnlyComposable get() = LocalKairosType.current
    val reducedMotion: Boolean
        @Composable @ReadOnlyComposable get() = LocalReducedMotion.current
}

@Composable
fun KairosTheme(
    themeMode: ThemeMode = ThemeMode.System,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Daybreak -> false
        ThemeMode.Dusk -> true
    }
    val palette = if (dark) DuskPalette else DaybreakPalette
    val context = LocalContext.current
    val systemReduced = remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    // Material components (text fields, date picker) inherit the same ink and accent.
    val scheme = if (dark) {
        darkColorScheme(
            primary = palette.accent, onPrimary = palette.onAccent,
            background = palette.paper, surface = palette.card, onSurface = palette.ink,
            onBackground = palette.ink, onSurfaceVariant = palette.inkSoft,
            surfaceContainerHigh = palette.paperDeep, outline = palette.inkFaint,
        )
    } else {
        lightColorScheme(
            primary = palette.accent, onPrimary = palette.onAccent,
            background = palette.paper, surface = palette.paper, onSurface = palette.ink,
            onBackground = palette.ink, onSurfaceVariant = palette.inkSoft,
            surfaceContainerHigh = palette.paperDeep, outline = palette.inkFaint,
        )
    }
    CompositionLocalProvider(
        LocalKairosPalette provides palette,
        LocalKairosType provides DefaultKairosType,
        LocalReducedMotion provides (reducedMotion || systemReduced),
    ) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
