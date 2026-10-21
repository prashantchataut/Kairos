package com.kairos.app.ui.art

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.K
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Particle shapes, from the confetti in r03 and the petals of the bloom. */
enum class Bit { Petal, Dot, Squiggle, Heart, Spark, Seed }

internal data class Particle(val kind: Bit, val angle: Float, val speed: Float, val spin: Float, val size: Float, val color: Color, val delay: Float)

/**
 * One-shot particle burst. Call [fire] with an origin in the element's own coordinates; draw it with
 * [Modifier.burst]. Calm motion never fires.
 */
@Stable
class BurstState internal constructor(private val calm: Boolean) {
    internal val progress = Animatable(1f)
    internal var origin by mutableStateOf(Offset.Unspecified)
    internal var particles by mutableStateOf(emptyList<Particle>())
    internal var gravity = 1f

    suspend fun fire(
        at: Offset,
        kinds: List<Bit> = listOf(Bit.Petal, Bit.Dot, Bit.Squiggle, Bit.Spark),
        colors: List<Color> = listOf(Candy.Coral, Candy.Sun, Candy.Bubblegum, Candy.Mint, Candy.Sky, Candy.Lilac),
        count: Int = 18,
        spread: Float = 360f,
        direction: Float = -90f,
        power: Float = 1f,
        gravity: Float = 1f,
        durationMs: Int = 950,
    ) {
        if (calm) return
        val r = Random(System.nanoTime())
        origin = at
        this.gravity = gravity
        particles = List(count) {
            val a = direction + (r.nextFloat() - 0.5f) * spread
            Particle(
                kind = kinds[r.nextInt(kinds.size)],
                angle = a * (PI.toFloat() / 180f),
                speed = (0.55f + r.nextFloat() * 0.75f) * power,
                spin = (r.nextFloat() - 0.5f) * 720f,
                size = 0.7f + r.nextFloat() * 0.7f,
                color = colors[r.nextInt(colors.size)],
                delay = r.nextFloat() * 0.12f,
            )
        }
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMs, easing = LinearEasing))
    }
}

@Composable
fun rememberBurst(): BurstState {
    val calm = K.calm
    return remember(calm) { BurstState(calm) }
}

/** Draws [state]'s particles over this element (they may fly outside its bounds). */
fun Modifier.burst(state: BurstState): Modifier = composed {
    drawWithContent {
        drawContent()
        val p = state.progress.value
        if (p >= 1f || state.origin == Offset.Unspecified) return@drawWithContent
        drawParticles(state, p)
    }
}

private fun DrawScope.drawParticles(state: BurstState, p: Float) {
    val unit = density
    state.particles.forEach { pt ->
        val t = ((p - pt.delay) / (1f - pt.delay)).coerceIn(0f, 1f)
        if (t <= 0f) return@forEach
        // Fast out, then gravity pulls it down and it fades.
        val ease = 1f - (1f - t) * (1f - t)
        val dist = pt.speed * 150f * unit * ease
        val x = state.origin.x + cos(pt.angle) * dist
        val y = state.origin.y + sin(pt.angle) * dist + state.gravity * 120f * unit * t * t
        val alpha = if (t < 0.7f) 1f else (1f - (t - 0.7f) / 0.3f)
        val s = pt.size * unit * (if (t < 0.15f) t / 0.15f else 1f)
        translate(x, y) {
            rotate(pt.spin * t, Offset.Zero) { drawBit(pt.kind, s, pt.color.copy(alpha = pt.color.alpha * alpha)) }
        }
    }
}

private fun DrawScope.drawBit(kind: Bit, s: Float, c: Color) {
    when (kind) {
        Bit.Dot -> drawCircle(c, 4f * s, Offset.Zero)
        Bit.Petal -> drawPath(Bloom.petal(12f * s).also { it.translate(Offset(0f, 7f * s)) }, c)
        Bit.Squiggle -> {
            val path = Path().apply {
                moveTo(-8f * s, 0f)
                cubicTo(-4f * s, -6f * s, 0f, 6f * s, 4f * s, 0f)
                cubicTo(6f * s, -3f * s, 8f * s, -2f * s, 9f * s, 0f)
            }
            drawPath(path, c, style = Stroke(2.6f * s, cap = StrokeCap.Round))
        }
        Bit.Heart -> {
            val w = 6f * s
            val path = Path().apply {
                moveTo(0f, w * 0.9f)
                cubicTo(-w * 1.6f, -w * 0.2f, -w * 0.7f, -w * 1.5f, 0f, -w * 0.5f)
                cubicTo(w * 0.7f, -w * 1.5f, w * 1.6f, -w * 0.2f, 0f, w * 0.9f)
                close()
            }
            drawPath(path, c)
        }
        Bit.Spark -> {
            val w = 6f * s
            val path = Path().apply {
                moveTo(0f, -w); quadraticTo(w * 0.18f, -w * 0.18f, w, 0f); quadraticTo(w * 0.18f, w * 0.18f, 0f, w)
                quadraticTo(-w * 0.18f, w * 0.18f, -w, 0f); quadraticTo(-w * 0.18f, -w * 0.18f, 0f, -w); close()
            }
            drawPath(path, c)
        }
        Bit.Seed -> {
            // A dandelion seed (r25): a stalk with a little fan of filaments.
            val l = 11f * s
            drawLine(c, Offset(0f, l * 0.5f), Offset(0f, -l * 0.4f), 1.4f * s, StrokeCap.Round)
            for (k in -2..2) {
                val a = (-90f + k * 26f) * (PI.toFloat() / 180f)
                drawLine(c, Offset(0f, -l * 0.4f), Offset(cos(a) * l * 0.55f, -l * 0.4f + sin(a) * l * 0.55f), 1.1f * s, StrokeCap.Round)
            }
            drawCircle(c, 1.6f * s, Offset(0f, l * 0.55f))
        }
    }
}
