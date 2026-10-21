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
 * One motion family. [bloom] is the signature: a lively spring with about 10% overshoot for
 * characters, stickers, pop-ins and success. [soft] moves layout, [press] answers touch, [settle]
 * reveals content. Navigation stays quick and quiet; the delight budget is spent on rare moments.
 */
object Motion {
    val Ease = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    /** Entrances: fast start, long gentle landing (MD3 emphasised decelerate). */
    val Enter = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    /** Strong ease-out for UI. */
    val Out = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
    /** On-screen movement from one place to another. */
    val InOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)

    const val QUICK = 140
    const val STANDARD = 280
    const val SLOW = 520
    const val REVEAL = 900
    const val STAGGER = 55L

    fun <T> bloom() = spring<T>(dampingRatio = 0.58f, stiffness = 380f)
    fun <T> soft() = spring<T>(dampingRatio = 0.85f, stiffness = 380f)
    fun <T> press() = spring<T>(dampingRatio = 0.6f, stiffness = 900f)
    fun <T> settle() = spring<T>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
    fun <T> fade(ms: Int = 260) = tween<T>(ms, easing = Ease)
    fun <T> enter(ms: Int = STANDARD, delay: Int = 0) = tween<T>(ms, delay, easing = Enter)
    val offset = spring(dampingRatio = 0.9f, stiffness = 380f, visibilityThreshold = IntOffset(1, 1))
    val pop = spring(dampingRatio = 0.62f, stiffness = 420f, visibilityThreshold = IntOffset(1, 1))
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
