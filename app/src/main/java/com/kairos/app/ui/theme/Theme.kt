package com.kairos.app.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import com.kairos.app.data.prefs.ThemeMode

object K {
    val colors: Palette @Composable @ReadOnlyComposable get() = LocalPalette.current
    val type: Typography @Composable @ReadOnlyComposable get() = LocalTypography.current
    val calm: Boolean @Composable @ReadOnlyComposable get() = LocalCalmMotion.current
}

/** One motion family: soft springs for movement, a single ease for fades. */
object Motion {
    val Ease = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    fun <T> soft() = spring<T>(dampingRatio = 0.85f, stiffness = 380f)
    fun <T> press() = spring<T>(dampingRatio = 0.7f, stiffness = 900f)
    fun <T> settle() = spring<T>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
    fun <T> fade(ms: Int = 260) = tween<T>(ms, easing = Ease)
    val offset = spring(dampingRatio = 0.9f, stiffness = 380f, visibilityThreshold = IntOffset(1, 1))
}

val LocalCalmMotion = staticCompositionLocalOf { false }

@Composable
fun KairosTheme(mode: ThemeMode = ThemeMode.System, calmMotion: Boolean = false, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val p = if (dark) DarkPalette else LightPalette
    val context = LocalContext.current
    val systemCalm = remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val scheme = if (dark) {
        darkColorScheme(primary = p.accent, onPrimary = p.onAccent, background = p.canvas, surface = p.surface, onSurface = p.ink, onBackground = p.ink, onSurfaceVariant = p.inkSoft, outline = p.line, surfaceContainerHigh = p.surface)
    } else {
        lightColorScheme(primary = p.accent, onPrimary = p.onAccent, background = p.canvas, surface = p.surface, onSurface = p.ink, onBackground = p.ink, onSurfaceVariant = p.inkSoft, outline = p.line, surfaceContainerHigh = p.surface)
    }
    CompositionLocalProvider(
        LocalPalette provides p,
        LocalTypography provides DefaultTypography,
        LocalCalmMotion provides (calmMotion || systemCalm),
    ) { MaterialTheme(colorScheme = scheme, content = content) }
}
