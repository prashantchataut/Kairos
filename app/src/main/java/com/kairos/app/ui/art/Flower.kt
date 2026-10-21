package com.kairos.app.ui.art

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.graphics.drawscope.scale
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.random.Random

/**
 * Flowers that grow the way they do in r28: a stem draws itself upward, then capsule petals filled
 * with sweep gradients pop onto it. Every flower is seeded from an id, so a word always grows the
 * same flower.
 */
enum class Head { Tulip, Hyacinth, Daisy, Reed, Berries }

data class FlowerSpec(val head: Head, val bend: Float, val lean: Float, val height: Float, val colors: List<Color>)

object Petals {
    val PinkViolet = listOf(Color(0xFFFF9AD5), Color(0xFFC43CF5), Color(0xFFFF7BC4), Color(0xFFFF9AD5))
    val CyanLilac = listOf(Color(0xFF12D3FF), Color(0xFFD2C2FF), Color(0xFF55B8FF), Color(0xFF12D3FF))
    val SunOrange = listOf(Color(0xFFFFE65C), Color(0xFFFF7A1A), Color(0xFFFFB53D), Color(0xFFFFE65C))
    val Lilac = listOf(Color(0xFFF4E0FF), Color(0xFF9257FF), Color(0xFFC9A6FF), Color(0xFFF4E0FF))
    val Green = listOf(Color(0xFF8BF5B3), Color(0xFF0FB463), Color(0xFF52DC8E), Color(0xFF8BF5B3))
    val Coral = listOf(Color(0xFFFFC2A8), Color(0xFFFF5A3C), Color(0xFFFF9068), Color(0xFFFFC2A8))
    val all = listOf(PinkViolet, CyanLilac, SunOrange, Lilac, Coral)
}

fun flowerSpec(seed: Int): FlowerSpec {
    val r = Random(seed)
    val head = Head.entries[r.nextInt(Head.entries.size)]
    val colors = when (head) {
        Head.Daisy -> Petals.SunOrange
        Head.Berries -> Petals.Green
        else -> Petals.all[r.nextInt(Petals.all.size)]
    }
    return FlowerSpec(head, bend = r.nextFloat() * 2f - 1f, lean = r.nextFloat() * 1.2f - 0.6f, height = 0.5f + r.nextFloat() * 0.3f, colors = colors)
}

private fun DrawScope.capsule(center: Offset, w: Float, h: Float, colors: List<Color>, s: Float) {
    if (s <= 0.001f) return
    scale(s, s, center) {
        drawRoundRect(Brush.sweepGradient(colors, center), Offset(center.x - w / 2, center.y - h / 2), Size(w, h), CornerRadius(w / 2, w / 2))
    }
}

private fun pop(bloom: Float, k: Int, of: Int): Float {
    val start = k * 0.5f / of
    return ((bloom - start) / (1f - start * 0.6f)).coerceIn(0f, 1.15f)
}

/**
 * One flower rooted at [base], [height] px tall. [grow] draws the stem 0..1; [bloom] opens the head
 * 0..1 (springs may overshoot a little past 1).
 */
fun DrawScope.drawFlower(spec: FlowerSpec, base: Offset, height: Float, grow: Float, bloom: Float, stem: Color = Color.White) {
    val h = height * spec.height
    val top = Offset(base.x + spec.lean * h * 0.22f, base.y - h)
    val path = Path().apply {
        moveTo(base.x, base.y)
        cubicTo(base.x + spec.bend * h * 0.18f, base.y - h * 0.35f, top.x - spec.bend * h * 0.16f, base.y - h * 0.7f, top.x, top.y)
    }
    val pm = PathMeasure().apply { setPath(path, false) }
    val len = pm.length * grow.coerceIn(0f, 1f)
    if (len <= 1f) return
    val seg = Path()
    pm.getSegment(0f, len, seg, true)
    val sw = h * 0.034f
    drawPath(seg, stem, style = Stroke(sw, cap = StrokeCap.Round))
    if (bloom <= 0.001f) return
    val tip = pm.getPosition(len)
    val tan = pm.getTangent(len)
    val angle = Math.toDegrees(atan2(tan.y.toDouble(), tan.x.toDouble())).toFloat() + 90f
    val u = h * 0.2f
    rotate(angle, tip) {
        when (spec.head) {
            Head.Tulip -> listOf(-30f, 30f, 0f).forEachIndexed { k, a ->
                rotate(a, tip) { capsule(Offset(tip.x, tip.y - u * 1.05f), u * 0.95f, u * 2.1f, spec.colors, pop(bloom, k, 3)) }
            }
            Head.Hyacinth -> for (k in 0 until 3) {
                val c = Offset(tip.x + (k - 1) * u * 0.18f, tip.y - u * 0.55f - k * u * 0.78f)
                rotate(-12f, c) { capsule(c, u * 2.1f, u * 0.92f, spec.colors, pop(bloom, k, 3)) }
            }
            Head.Daisy -> {
                val s = pop(bloom, 0, 1)
                val c = Offset(tip.x, tip.y - u * 1.1f)
                scale(s, s, c) {
                    drawCircle(Color.White, u * 1.15f, c)
                    drawCircle(Brush.sweepGradient(spec.colors, c), u * 0.72f, c)
                }
            }
            Head.Reed -> {
                capsule(Offset(tip.x, tip.y - u * 1.5f), u * 0.82f, u * 3f, spec.colors, pop(bloom, 0, 2))
                rotate(-24f, tip) { capsule(Offset(tip.x - u * 0.2f, tip.y - u * 0.55f), u * 0.75f, u * 1.6f, spec.colors, pop(bloom, 1, 2)) }
            }
            Head.Berries -> {
                val spots = listOf(Offset(0f, -1.6f), Offset(-0.9f, -1.1f), Offset(0.9f, -1.15f), Offset(0f, -0.65f), Offset(-0.8f, -0.25f), Offset(0.85f, -0.3f), Offset(0f, -2.4f))
                spots.forEachIndexed { k, o ->
                    val c = Offset(tip.x + o.x * u * 0.75f, tip.y + o.y * u * 0.75f)
                    val s = pop(bloom, k, spots.size)
                    if (s > 0f) scale(s, s, c) { drawCircle(Brush.sweepGradient(spec.colors, c), u * 0.42f, c) }
                }
            }
        }
    }
}

/**
 * A row of flowers, one per seed, growing in sequence (stem first, then petals). With [grow] false,
 * or under calm motion, they are simply there.
 */
@Composable
fun Garden(seeds: List<Int>, modifier: Modifier = Modifier, grow: Boolean = true, stem: Color = Color.White) {
    val calm = K.calm
    val stems = remember(seeds) { seeds.map { Animatable(if (grow && !calm) 0f else 1f) } }
    val heads = remember(seeds) { seeds.map { Animatable(if (grow && !calm) 0f else 1f) } }
    LaunchedEffect(seeds, grow, calm) {
        if (!grow || calm) return@LaunchedEffect
        seeds.indices.forEach { i ->
            launch {
                delay(i * 140L)
                stems[i].animateTo(1f, Motion.enter(720))
                heads[i].animateTo(1f, Motion.bloom())
            }
        }
    }
    Canvas(modifier) {
        val n = seeds.size
        if (n == 0) return@Canvas
        seeds.forEachIndexed { i, s ->
            val spec = flowerSpec(s)
            val x = size.width * (i + 0.5f) / n + (Random(s).nextFloat() - 0.5f) * size.width / n * 0.4f
            drawFlower(spec, Offset(x, size.height), size.height * 0.98f, stems[i].value, heads[i].value, stem)
        }
    }
}
