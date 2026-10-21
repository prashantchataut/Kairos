package com.kairos.app.ui.art

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The Kairos mark: a dandelion clock. Kairos is the right moment, and a dandelion is the moment you
 * make a wish and let go; its seeds carry what you keep into the future (the time capsule) and
 * take root (the words you learn). One seed is always drifting free (#606-25).
 *
 * [release] 0 = all seeds on the clock; 1 = the free seed has floated away to the top right.
 * [rays] fades the rays in from the centre for the reveal animation (0..1).
 */
fun DrawScope.dandelion(
    color: Color,
    release: Float = 1f,
    rays: Float = 1f,
    withStem: Boolean = false,
    freeSeed: Boolean = true,
) {
    val s = size.minDimension
    val c = Offset(size.width / 2f, size.height / 2f + if (withStem) -s * 0.12f else 0f)
    val r = s * (if (withStem) 0.30f else 0.34f)
    // Small sizes keep the eight-ray heritage mark; larger sizes become a fluffy clock.
    val small = s < 110f
    val count = if (small) 8 else 16
    val w = s * if (small) 0.06f else 0.024f
    val gapAngle = -52f
    for (i in 0 until count) {
        val deg = i * 360f / count - 90f
        // Leave a gap at the top right where the free seed came from.
        if (freeSeed && kotlin.math.abs(angleDiff(deg, gapAngle)) < (if (small) 30f else 17f)) continue
        val a = deg * (PI / 180f).toFloat()
        val long = small || i % 2 == 0
        val len = r * rays * (if (long) 1f else 0.8f)
        if (len <= 0f) continue
        val end = Offset(c.x + cos(a) * len, c.y + sin(a) * len)
        val start = Offset(c.x + cos(a) * r * 0.14f, c.y + sin(a) * r * 0.14f)
        drawLine(color, start, end, strokeWidth = w, cap = StrokeCap.Round)
        val puff = ((rays - 0.5f) / 0.5f).coerceIn(0f, 1f)
        if (puff > 0f) seedHead(end, a, r * 0.16f * puff, w, color, small)
    }
    drawCircle(color, r * if (small) 0.16f else 0.12f, c)
    if (withStem) {
        val stem = Path().apply {
            moveTo(c.x, c.y + r * 0.12f)
            quadraticTo(c.x - s * 0.06f, c.y + r * 1.0f, c.x + s * 0.02f, size.height * 0.98f)
        }
        drawPath(stem, color, style = Stroke(w * if (small) 1f else 1.3f, cap = StrokeCap.Round))
    }
    if (freeSeed) {
        val ga = gapAngle * (PI / 180f).toFloat()
        val home = Offset(c.x + cos(ga) * r, c.y + sin(ga) * r)
        val away = Offset(c.x + cos(ga) * r * 1.55f + r * 0.18f, c.y + sin(ga) * r * 1.55f - r * 0.12f)
        val t = release
        // Drifts out on a soft arc, tilting as it goes.
        val p = Offset(
            home.x + (away.x - home.x) * t + sin(t * PI.toFloat()) * r * 0.12f,
            home.y + (away.y - home.y) * t - sin(t * PI.toFloat()) * r * 0.10f,
        )
        seed(p, r * if (small) 0.62f else 0.5f, ga + 0.35f * t, w * if (small) 0.85f else 1.1f, color)
    }
}

private fun angleDiff(a: Float, b: Float): Float {
    var d = (a - b) % 360f
    if (d > 180f) d -= 360f
    if (d < -180f) d += 360f
    return d
}

/** Where a ray ends: a round seed-head with a few fine hairs, like the fluff of a dandelion clock. */
private fun DrawScope.seedHead(tip: Offset, angle: Float, len: Float, w: Float, color: Color, small: Boolean) {
    if (len <= 0f) return
    if (small) {
        drawCircle(color, w * 0.95f, tip)
        return
    }
    drawCircle(color, w * 1.05f, tip)
    for (k in -1..1) {
        val a = angle + k * 0.5f
        drawLine(color, tip, Offset(tip.x + cos(a) * len * 0.8f, tip.y + sin(a) * len * 0.8f), strokeWidth = w * 0.4f, cap = StrokeCap.Round)
    }
}

/** A single dandelion seed: a stalk with a fanned crown, pointing along [angle]. */
fun DrawScope.seed(at: Offset, length: Float, angle: Float, w: Float, color: Color) {
    translate(at.x, at.y) {
        rotate(angle * 180f / PI.toFloat() + 90f, pivot = Offset.Zero) {
            drawLine(color, Offset(0f, length * 0.5f), Offset(0f, -length * 0.25f), strokeWidth = w, cap = StrokeCap.Round)
            drawCircle(color, w * 0.9f, Offset(0f, length * 0.5f))
            val o = Offset(0f, -length * 0.25f)
            for (k in -4..4) {
                val a = (-90f + k * 17f) * (PI / 180f).toFloat()
                val tip = Offset(o.x + cos(a) * length * 0.42f, o.y + sin(a) * length * 0.42f)
                drawLine(color, o, tip, strokeWidth = w * 0.5f, cap = StrokeCap.Round)
                drawCircle(color, w * 0.55f, tip)
            }
        }
    }
}

@Composable
fun DandelionMark(size: Dp, color: Color, modifier: Modifier = Modifier, release: Float = 1f, rays: Float = 1f, withStem: Boolean = false) {
    Canvas(modifier.size(size)) { dandelion(color, release, rays, withStem) }
}
