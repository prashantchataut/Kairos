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

/**
 * Motion tokens (kairos-taste §6). UI is crisp (Emil's strong ease-out, under 300ms); illustration
 * and celebration are playful (springs with 10–20% overshoot).
 */
object Motion {
    /** Strong ease-out for UI: cubic-bezier(0.23, 1, 0.32, 1). */
    val Ease = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
    /** On-screen movement: cubic-bezier(0.77, 0, 0.175, 1). */
    val Move = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)
    /** Material emphasized, for focus pulls and reveals. */
    val Emphasized = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    fun <T> soft() = spring<T>(dampingRatio = 0.86f, stiffness = 420f)
    fun <T> press() = spring<T>(dampingRatio = 0.7f, stiffness = 1000f)
    fun <T> settle() = spring<T>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
    /** Playful pop: ~15% overshoot. */
    fun <T> pop() = spring<T>(dampingRatio = 0.55f, stiffness = 380f)
    fun <T> fade(ms: Int = 240) = tween<T>(ms, easing = Ease)
    val offset = spring(dampingRatio = 0.86f, stiffness = 420f, visibilityThreshold = IntOffset(1, 1))
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
        darkColorScheme(primary = p.accent, onPrimary = p.onAccent, background = p.paper, surface = p.surface, onSurface = p.ink, onBackground = p.ink, onSurfaceVariant = p.inkSoft, outline = p.line, surfaceContainerHigh = p.surface)
    } else {
        lightColorScheme(primary = p.accent, onPrimary = p.onAccent, background = p.paper, surface = p.surface, onSurface = p.ink, onBackground = p.ink, onSurfaceVariant = p.inkSoft, outline = p.line, surfaceContainerHigh = p.surface)
    }
    CompositionLocalProvider(
        LocalPalette provides p,
        LocalTypography provides DefaultTypography,
        LocalCalmMotion provides (calmMotion || systemCalm),
    ) { MaterialTheme(colorScheme = scheme, content = content) }
}
