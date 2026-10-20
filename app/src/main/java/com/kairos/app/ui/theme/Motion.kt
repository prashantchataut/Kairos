package com.kairos.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.IntOffset

/**
 * One motion family. Everything springs — "Squish" is the playful one reserved for
 * touch feedback and the mascot, "Glide" is for layout and screen changes.
 */
object Motion {
    val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    const val Quick = 160
    const val Medium = 320
    const val Slow = 520

    fun <T> squish() = spring<T>(dampingRatio = 0.52f, stiffness = 420f)
    fun <T> glide() = spring<T>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 260f)
    fun <T> settle() = spring<T>(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow)
    fun <T> fade(duration: Int = Medium) = tween<T>(duration, easing = Emphasized)
    val glideOffset = spring(dampingRatio = 0.86f, stiffness = 300f, visibilityThreshold = IntOffset(1, 1))
}

/** True when the person (or the system animator scale) asked for less motion. */
val LocalReducedMotion = staticCompositionLocalOf { false }
