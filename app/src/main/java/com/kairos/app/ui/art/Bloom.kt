package com.kairos.app.ui.art

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import com.kairos.app.ui.theme.Glaze
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Two flower families: translucent glazes (#606-29) and capsule petals with sweep gradients (#606-28). */
enum class BloomKind { Glaze, Capsule, Daisy, Clover }

private val glazeSets = listOf(
    listOf(Glaze.Coral, Glaze.Butter, Glaze.Cyan),
    listOf(Glaze.Rose, Glaze.Violet, Glaze.Cyan),
    listOf(Glaze.Butter, Glaze.Coral, Glaze.Rose),
    listOf(Glaze.Cyan, Glaze.Violet, Glaze.Rose),
    listOf(Glaze.Mint, Glaze.Cyan, Glaze.Violet),
)

private val stemGreen = Color(0xFF3FA65A)

/** Cubic ease-out for staged progress. */
private fun ease(x: Float) = 1f - (1f - x.coerceIn(0f, 1f)).let { it * it * it }

/** Overshoot (back) ease for petal pops. */
private fun back(x: Float): Float {
    val t = x.coerceIn(0f, 1f) - 1f
    val s = 1.9f
    return 1f + t * t * ((s + 1) * t + s)
}

private fun stage(p: Float, from: Float, to: Float) = ((p - from) / (to - from)).coerceIn(0f, 1f)

/**
 * Draws a flower into [size]. [progress] 0..1 grows it: the stem draws first, then petals pop
 * in sequence and open. [sway] (radians-ish, small) bends it in the breeze.
 */
fun DrawScope.bloom(kind: BloomKind, seed: Int, progress: Float, sway: Float = 0f, stemColor: Color = stemGreen, hueShift: Float = 0f) {
    val rnd = Random(seed)
    val w = size.width
    val h = size.height
    val base = Offset(w * 0.5f, h)
    val headR = w * (0.32f + rnd.nextFloat() * 0.05f)
    val head = Offset(w * (0.5f + (rnd.nextFloat() - 0.5f) * 0.16f) + sway * w * 0.3f, headR * 1.15f + h * 0.02f)
    // Stem: a soft S-curve that draws itself.
    val stem = Path().apply {
        moveTo(base.x, base.y)
        cubicTo(base.x + w * (rnd.nextFloat() - 0.5f) * 0.4f, h * 0.7f, head.x - sway * w * 0.2f + w * (rnd.nextFloat() - 0.5f) * 0.3f, h * 0.45f, head.x, head.y + headR * 0.4f)
    }
    val stemP = ease(stage(progress, 0f, 0.4f))
    if (stemP > 0f) {
        val pm = PathMeasure().apply { setPath(stem, false) }
        val part = Path()
        pm.getSegment(0f, pm.length * stemP, part, true)
        drawPath(part, stemColor, style = Stroke(w * 0.035f, cap = StrokeCap.Round))
        // A leaf unfurls halfway up.
        val leafP = back(stage(progress, 0.25f, 0.55f))
        if (leafP > 0f) {
            val at = pm.getPosition(pm.length * 0.45f)
            val dir = if (rnd.nextBoolean()) 1f else -1f
            translate(at.x, at.y) {
                rotate(dir * (35f + 20f * sway), Offset.Zero) {
                    val lw = w * 0.22f * leafP
                    val leaf = Path().apply {
                        moveTo(0f, 0f)
                        quadraticTo(dir * lw * 0.5f, -lw * 0.45f, dir * lw, 0f)
                        quadraticTo(dir * lw * 0.5f, lw * 0.3f, 0f, 0f)
                        close()
                    }
                    drawPath(leaf, Brush.linearGradient(listOf(lerp(stemColor, Color.White, 0.25f), stemColor), Offset.Zero, Offset(dir * lw, 0f)))
                }
            }
        }
    }
    val petalP = stage(progress, 0.35f, 1f)
    if (petalP <= 0f) return
    when (kind) {
        BloomKind.Glaze -> glazeHead(head, headR, rnd, petalP, hueShift)
        BloomKind.Capsule -> capsuleHead(head, headR, rnd, petalP)
        BloomKind.Daisy -> daisyHead(head, headR, rnd, petalP)
        BloomKind.Clover -> cloverHead(head, headR, rnd, petalP)
    }
}

/** #606-29: overlapping translucent petals that mix like glazes, opening in two layers. */
private fun DrawScope.glazeHead(c: Offset, r: Float, rnd: Random, p: Float, hueShift: Float) {
    val set = glazeSets[rnd.nextInt(glazeSets.size)]
    val layers = listOf(9 to 1f, 7 to 0.72f)
    layers.forEachIndexed { li, (count, scale) ->
        val lp = back(stage(p, li * 0.25f, 0.75f + li * 0.25f))
        if (lp <= 0f) return@forEachIndexed
        val spread = 0.35f + 0.65f * ease(stage(p, li * 0.25f, 1f))
        for (i in 0 until count) {
            val a = (i.toFloat() / count) * 360f * spread + li * 20f + rnd.nextFloat() * 8f - 90f - 180f * spread + 180f
            val len = r * scale * lp * (0.9f + rnd.nextFloat() * 0.25f)
            val wid = len * 0.48f
            rotate(a, c) {
                val petal = Path().apply {
                    moveTo(c.x, c.y)
                    cubicTo(c.x - wid, c.y - len * 0.35f, c.x - wid * 0.7f, c.y - len * 0.95f, c.x, c.y - len)
                    cubicTo(c.x + wid * 0.7f, c.y - len * 0.95f, c.x + wid, c.y - len * 0.35f, c.x, c.y)
                    close()
                }
                val inner = set[(i + li) % set.size]
                val outer = set[(i + li + 1 + (hueShift * 3).toInt()) % set.size]
                drawPath(petal, Brush.linearGradient(listOf(inner.copy(alpha = 0.72f), outer.copy(alpha = 0.42f)), c, Offset(c.x, c.y - len)))
                drawPath(petal, Color.White.copy(alpha = 0.25f), style = Stroke(r * 0.015f))
            }
        }
    }
    val core = back(stage(p, 0.5f, 1f))
    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF3B0), Glaze.Coral.copy(alpha = 0.9f)), c, r * 0.18f), r * 0.16f * core, c)
}

/** #606-28: rounded capsule petals with sweep (conic) gradients around a bright disc. */
private fun DrawScope.capsuleHead(c: Offset, r: Float, rnd: Random, p: Float) {
    val palettes = listOf(
        listOf(Color(0xFF6FD3FF), Color(0xFFB4E8FF), Color(0xFF9B7BFF)),
        listOf(Color(0xFFFF8FC8), Color(0xFFD06BFF), Color(0xFFFFB3E0)),
        listOf(Color(0xFFB9A4FF), Color(0xFFE4DAFF), Color(0xFF8C6BFF)),
    )
    val pal = palettes[rnd.nextInt(palettes.size)]
    val count = 5 + rnd.nextInt(2)
    for (i in 0 until count) {
        val lp = back(stage(p, i * 0.08f, 0.5f + i * 0.08f))
        if (lp <= 0f) continue
        val a = i * 360f / count - 90f
        val len = r * 1.05f * lp
        val wid = r * 0.42f
        rotate(a, c) {
            val tl = Offset(c.x - wid / 2, c.y - len)
            drawRoundRect(
                Brush.sweepGradient(pal + pal.first(), Offset(c.x, c.y - len * 0.6f)),
                tl, Size(wid, len * 0.95f), androidx.compose.ui.geometry.CornerRadius(wid / 2),
            )
        }
    }
    val core = back(stage(p, 0.45f, 1f))
    drawCircle(Color.White, r * 0.42f * core, c)
    drawCircle(Brush.sweepGradient(listOf(Color(0xFFFFD84A), Color(0xFFFF8A3D), Color(0xFFFFD84A)), c), r * 0.3f * core, c)
}

/** A simple daisy: white petals, sunny centre. */
private fun DrawScope.daisyHead(c: Offset, r: Float, rnd: Random, p: Float) {
    val count = 12
    for (i in 0 until count) {
        val lp = back(stage(p, i * 0.03f, 0.55f + i * 0.03f))
        if (lp <= 0f) continue
        rotate(i * 360f / count + rnd.nextFloat() * 6f, c) {
            drawRoundRect(Color.White.copy(alpha = 0.95f), Offset(c.x - r * 0.12f, c.y - r * lp), Size(r * 0.24f, r * 0.75f * lp), androidx.compose.ui.geometry.CornerRadius(r * 0.12f))
        }
    }
    val core = back(stage(p, 0.4f, 1f))
    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFE27A), Color(0xFFFFB020)), c, r * 0.3f), r * 0.28f * core, c)
}

/** #606-28's green sphere clusters. */
private fun DrawScope.cloverHead(c: Offset, r: Float, rnd: Random, p: Float) {
    val count = 6
    for (i in 0 until count) {
        val lp = back(stage(p, i * 0.07f, 0.5f + i * 0.07f))
        if (lp <= 0f) continue
        val a = (i * 360f / count) * (PI / 180f).toFloat()
        val o = Offset(c.x + cos(a) * r * 0.5f, c.y + sin(a) * r * 0.5f)
        drawCircle(Brush.sweepGradient(listOf(Color(0xFF22C27A), Color(0xFF9BF0C2), Color(0xFF15965A), Color(0xFF22C27A)), o), r * 0.36f * lp, o)
    }
    drawCircle(Brush.sweepGradient(listOf(Color(0xFF22C27A), Color(0xFF9BF0C2), Color(0xFF22C27A)), c), r * 0.36f * back(stage(p, 0.4f, 1f)), c)
}

/**
 * A flower that grows when it first appears (stem draws, petals pop and open) and then sways.
 * In calmer-motion mode it is simply there, already open.
 */
@Composable
fun Bloom(kind: BloomKind, seed: Int, modifier: Modifier = Modifier, grow: Boolean = true, delayMs: Int = 0, durationMs: Int = 1400) {
    val calm = K.calm
    val progress = remember(seed) { Animatable(if (grow && !calm) 0f else 1f) }
    LaunchedEffect(seed) {
        if (progress.value < 1f) {
            kotlinx.coroutines.delay(delayMs.toLong())
            progress.animateTo(1f, tween(durationMs, easing = Motion.Ease))
        }
    }
    val t = rememberAmbientClock(5200 + (seed % 7) * 400)
    Canvas(modifier) { bloom(kind, seed, progress.value, sway = sin(t.value + seed) * 0.05f) }
}

/** Test hook: draw a glaze bloom at a fixed progress. */
internal fun bloomPreview(scope: DrawScope, progress: Float) = scope.bloom(BloomKind.Glaze, 7, progress)
