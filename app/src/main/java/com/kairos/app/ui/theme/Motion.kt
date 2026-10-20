package com.kairos.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset

/** Motion tokens. See DESIGN.md → Motion. */
object Motion {
    /** Strong ease-out for entrances — fast start, long gentle settle. */
    val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val EaseInSoft = CubicBezierEasing(0.4f, 0f, 1f, 1f)

    fun <T> ui() = spring<T>(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)
    fun <T> press() = spring<T>(dampingRatio = 0.7f, stiffness = Spring.StiffnessHigh)
    fun <T> bouncy() = spring<T>(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)
    fun <T> enter(durationMs: Int = 520, delayMs: Int = 0) =
        tween<T>(durationMillis = durationMs, delayMillis = delayMs, easing = EaseOutExpo)
    fun <T> exit(durationMs: Int = 180) = tween<T>(durationMillis = durationMs, easing = EaseInSoft)

    val offsetUi = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntOffset(1, 1))
}
