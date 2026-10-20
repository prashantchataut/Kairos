package com.kairos.app.core.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.sin

/**
 * Motion rules for Kairos.
 *
 * Nothing snaps. Everything settles. Durations stay under 420ms for navigation and under
 * 700ms for expressive moments (sealing a capsule, revealing a poster), and every ambient
 * loop stops when the system asks for reduced motion.
 */
object KairosMotion {
    const val press = 110
    const val state = 240
    const val page = 340
    const val expressive = 620

    fun <T> soft() = spring<T>(dampingRatio = 0.78f, stiffness = 260f)
    fun <T> snappy() = spring<T>(dampingRatio = 0.62f, stiffness = 620f)
    fun <T> bouncy() = spring<T>(dampingRatio = 0.45f, stiffness = 420f)

    val enter = fadeIn(tween(page, easing = FastOutSlowInEasing)) +
        slideInVertically(tween(page, easing = FastOutSlowInEasing)) { it / 18 }
    val exit = fadeOut(tween(state)) + slideOutVertically(tween(state)) { -it / 24 }

    val popIn = fadeIn(tween(state)) + scaleIn(tween(state), initialScale = 0.9f)
    val popOut = fadeOut(tween(160)) + scaleOut(tween(160), targetScale = 0.94f)
}

/** Gentle vertical drift for decorative objects. */
@Composable
fun rememberFloat(
    amplitude: Float = 6f,
    durationMillis: Int = 4200,
    phase: Float = 0f,
): Float {
    if (KairosTheme.reduceMotion) return 0f
    val transition = rememberInfiniteTransition(label = "float")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "float-phase",
    )
    return sin(t + phase) * amplitude
}

/** Slow breathing used by the mark and by empty-state mascots. */
@Composable
fun Modifier.breathe(
    enabled: Boolean = true,
    minScale: Float = 0.985f,
    maxScale: Float = 1.015f,
    durationMillis: Int = 3600,
): Modifier = composed {
    if (!enabled || KairosTheme.reduceMotion) return@composed this
    val transition = rememberInfiniteTransition(label = "breathe")
    val scale by transition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe-scale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Runs a one-shot intro animation and returns 0..1. */
@Composable
fun rememberIntro(durationMillis: Int = 900, delayMillis: Int = 0): Float {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        anim.animateTo(1f, tween(durationMillis, delayMillis, easing = FastOutSlowInEasing))
    }
    return anim.value
}

/** Fades content in from below on first composition. */
@Composable
fun Modifier.staggerIn(index: Int, baseDelay: Int = 70): Modifier {
    val progress = rememberIntro(durationMillis = 520, delayMillis = index * baseDelay)
    return this
        .alpha(progress.coerceIn(0f, 1f))
        .graphicsLayer {
            translationY = (1f - progress) * 28f
        }
}

/** A soft, single-shot settle used when a card changes identity. */
@Composable
fun Modifier.settle(key: Any): Modifier {
    val anim = remember(key) { Animatable(0f) }
    LaunchedEffect(key) { anim.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 320f)) }
    return this.graphicsLayer {
        val p = anim.value
        alpha = 0.35f + 0.65f * p
        scaleX = 0.97f + 0.03f * p
        scaleY = 0.97f + 0.03f * p
    }
}

/** A deterministic shimmer sweep, used while media loads. */
@Composable
fun Modifier.shimmer(enabled: Boolean = true): Modifier = composed {
    if (!enabled || KairosTheme.reduceMotion) return@composed this
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-x",
    )
    drawWithContent {
        drawContent()
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                colors = listOf(
                    androidx.compose.ui.graphics.Color.Transparent,
                    androidx.compose.ui.graphics.Color.White.copy(alpha = 0.22f),
                    androidx.compose.ui.graphics.Color.Transparent,
                ),
                startX = x * size.width,
                endX = x * size.width + size.width * 0.55f,
            ),
        )
    }
}

/** Standard spring for animated numbers. */
val NumberSpring = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 180f)
