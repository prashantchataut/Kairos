package com.kairos.app.design

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.view.HapticFeedbackConstants

/** Spacing on a 4dp grid. Group tight, separate generous. */
object Space {
    val hair: Dp = 2.dp
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 22.dp
    val xl: Dp = 30.dp
    val xxl: Dp = 44.dp
    val huge: Dp = 64.dp
    val gutter: Dp = 20.dp
}

/** One radius family, scaled by size — nothing is "just rounded". */
object Radius {
    val chip: Dp = 13.dp
    val control: Dp = 18.dp
    val card: Dp = 26.dp
    val hero: Dp = 32.dp
    val sheet: Dp = 36.dp
    val pill: Dp = 100.dp
}

/**
 * Motion.
 *
 * Springs, not keyframes: every animation starts from what is on screen and can
 * be grabbed mid-flight. Only the mascot is allowed to overshoot — everything
 * else lands critically damped.
 */
data class KairosMotion(
    val reduced: Boolean,
    val quick: Int,
    val base: Int,
    val slow: Int,
    val lazy: Int,
) {
    /** Settle — sheets, cards, layout changes. */
    fun <T> settle(): FiniteAnimationSpec<T> =
        if (reduced) tween(1) else spring(dampingRatio = 1f, stiffness = 520f)

    /** Soft — larger surfaces, page-level moves. */
    fun <T> soft(): FiniteAnimationSpec<T> =
        if (reduced) tween(1) else spring(dampingRatio = 1f, stiffness = 260f)

    /** Playful — mascot, seals, confetti. Overshoot lives here and nowhere else. */
    fun <T> playful(): FiniteAnimationSpec<T> =
        if (reduced) tween(1) else spring(dampingRatio = 0.55f, stiffness = 380f)

    fun fadeIn(): FiniteAnimationSpec<Float> = tween(if (reduced) 1 else base, easing = Standard)
    fun fadeOut(): FiniteAnimationSpec<Float> = tween(if (reduced) 1 else quick, easing = Accelerate)
}

val Standard: Easing = CubicBezierEasing(0.22f, 0.61f, 0.36f, 1f)
val Accelerate: Easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
val Decelerate: Easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)

@Composable
fun rememberMotion(): KairosMotion {
    val context = LocalContext.current
    val reduced = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
    return remember(reduced) {
        KairosMotion(reduced = reduced, quick = 140, base = 240, slow = 420, lazy = 620)
    }
}

/**
 * Haptics with a personality: a tick for navigation, a soft knock for
 * selection, a short pattern for a sealed capsule. Silenced by user setting.
 */
class KairosHaptics(private val enabled: Boolean) {
    private var view: android.view.View? = null

    fun attach(v: android.view.View) { view = v }

    fun tick() = fire(HapticFeedbackConstants.KEYBOARD_TAP)
    fun soft() = fire(HapticFeedbackConstants.CLOCK_TICK)
    fun select() = fire(HapticFeedbackConstants.CONTEXT_CLICK)
    fun success() = fire(HapticFeedbackConstants.CONFIRM)
    fun warn() = fire(HapticFeedbackConstants.LONG_PRESS)

    private fun fire(kind: Int) {
        if (!enabled) return
        runCatching { view?.performHapticFeedback(kind) }
    }
}

@Composable
fun rememberHaptics(enabled: Boolean): KairosHaptics {
    val view = LocalView.current
    val h = remember(enabled) { KairosHaptics(enabled) }
    remember(h) { h.attach(view); h }
    return h
}

/** Shared-element-ish page transition used by the custom navigator. */
val PageSpring = spring<Float>(dampingRatio = 0.92f, stiffness = 320f)
val ContentSpring = spring<Float>(dampingRatio = 1f, stiffness = 480f)
