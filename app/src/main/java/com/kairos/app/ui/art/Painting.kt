package com.kairos.app.ui.art

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Brushstroke paintings (r19, "vocabs bg"): loose gouache tulips over a field of leafy strokes,
 * seeded from the word so each word gets its own painting, with a palette chosen by topic.
 */
data class PaintPalette(val ground: Color, val leaves: List<Color>, val blooms: List<Color>)

object Paints {
    val Garden = PaintPalette(Color(0xFF1F8A6E), listOf(Color(0xFF2E9E83), Color(0xFF9ED4C4), Color(0xFF136B55), Color(0xFF5FBFA2)), listOf(Color(0xFFF2C94C), Color(0xFFE86A3A), Color(0xFF5B7CE6), Color(0xFFF6D6C8), Color(0xFFFFF1D6)))
    val Blush = PaintPalette(Color(0xFFE77E9C), listOf(Color(0xFFF2A3B8), Color(0xFFFFC7B5), Color(0xFFD4607F), Color(0xFFF7D2DA)), listOf(Color(0xFFFFE08A), Color(0xFFFF8A65), Color(0xFFB79CFF), Color(0xFFFFF4E8), Color(0xFFFF5E7A)))
    val Dusk = PaintPalette(Color(0xFF4655D6), listOf(Color(0xFF6D7BEF), Color(0xFF9DB4FF), Color(0xFF3442B8), Color(0xFF8FD6D0)), listOf(Color(0xFFC6B2FF), Color(0xFFFFE6A3), Color(0xFF6FE0C6), Color(0xFFFFFFFF), Color(0xFFFF9BC5)))
    val Peach = PaintPalette(Color(0xFFF08A4B), listOf(Color(0xFFFFB27A), Color(0xFFFFD6A8), Color(0xFFD9692F), Color(0xFF2E9E83)), listOf(Color(0xFFFFE070), Color(0xFFFF6F91), Color(0xFF6C8CFF), Color(0xFFFFF4DE), Color(0xFFFFC4D6)))
    val Honey = PaintPalette(Color(0xFFE5A92E), listOf(Color(0xFFF5CB63), Color(0xFF9DB35A), Color(0xFFC98A1D), Color(0xFFFFE7A8)), listOf(Color(0xFFFF7347), Color(0xFFFFF4D6), Color(0xFF5F7FE8), Color(0xFFFFB6C8), Color(0xFF3FA88A)))
    val Sky = PaintPalette(Color(0xFF3C72E6), listOf(Color(0xFF6FA3FF), Color(0xFFA8CCFF), Color(0xFF2A55C2), Color(0xFF9EE0F0)), listOf(Color(0xFFFFE27A), Color(0xFFFFFFFF), Color(0xFFFF8E6E), Color(0xFFC9B6FF), Color(0xFFFFD0E0)))
    val Meadow = PaintPalette(Color(0xFF2FA36B), listOf(Color(0xFF7FD48C), Color(0xFFC9E86B), Color(0xFF1E7D50), Color(0xFFA7E3C2)), listOf(Color(0xFFFFE15A), Color(0xFFFF7A59), Color(0xFFFFFFFF), Color(0xFF7D8CFF), Color(0xFFFFB0CC)))
    val Plum = PaintPalette(Color(0xFFAA4579), listOf(Color(0xFFD06A9C), Color(0xFFF2A7C9), Color(0xFF7E2D5A), Color(0xFF2E9E83)), listOf(Color(0xFFFFE08A), Color(0xFF8FD3FF), Color(0xFFFFF0F6), Color(0xFFFF8A5C), Color(0xFFC6B2FF)))

    fun forCategory(category: String) = when (category) {
        "nature" -> Garden; "feelings" -> Blush; "mind" -> Dusk; "connection" -> Peach; "craft" -> Honey
        "wonder" -> Sky; "courage" -> Meadow; "science" -> Sky; "culture" -> Plum; else -> Garden
    }
}

private class Stroke2(val path: Path, val color: Color, val width: Float, val bristles: List<Pair<Path, Color>>)

private fun stroke(r: Random, start: Offset, angle: Float, len: Float, curve: Float): Path {
    val a = angle * (PI.toFloat() / 180f)
    val end = Offset(start.x + cos(a) * len, start.y + sin(a) * len)
    val mid = Offset((start.x + end.x) / 2 - sin(a) * curve, (start.y + end.y) / 2 + cos(a) * curve)
    return Path().apply { moveTo(start.x, start.y); quadraticTo(mid.x, mid.y, end.x, end.y) }
}

private fun build(seed: Int, size: Size, pal: PaintPalette, detail: Float): List<Stroke2> {
    val r = Random(seed)
    val w = size.width
    val h = size.height
    val unit = minOf(w, h)
    val out = ArrayList<Stroke2>()
    fun add(path: Path, c: Color, width: Float, bristle: Boolean = true) {
        val bristles = if (!bristle) emptyList() else List(2) {
            val off = (r.nextFloat() - 0.5f) * width * 0.5f
            Path().apply { addPath(path, Offset(off, off * 0.3f)) } to lerp(c, Color.White, 0.4f).copy(alpha = 0.22f)
        }
        out += Stroke2(path, c, width, bristles)
    }
    // 1. Broad washes give the ground tonal movement.
    repeat(9) {
        val c = pal.leaves[r.nextInt(pal.leaves.size)].copy(alpha = 0.45f + r.nextFloat() * 0.25f)
        val start = Offset(r.nextFloat() * w, h * (0.2f + r.nextFloat() * 0.9f))
        add(stroke(r, start, -90f + (r.nextFloat() - 0.5f) * 110f, unit * (0.45f + r.nextFloat() * 0.4f), unit * (r.nextFloat() - 0.5f) * 0.2f), c, unit * (0.2f + r.nextFloat() * 0.12f), bristle = false)
    }
    // 2. Leaves and stems: confident, mostly upright strokes.
    repeat((24 * detail).toInt()) {
        val c = pal.leaves[r.nextInt(pal.leaves.size)].copy(alpha = 0.8f + r.nextFloat() * 0.2f)
        val start = Offset(r.nextFloat() * w, h * (0.35f + r.nextFloat() * 0.75f))
        add(stroke(r, start, -90f + (r.nextFloat() - 0.5f) * 60f, unit * (0.25f + r.nextFloat() * 0.3f), unit * (r.nextFloat() - 0.5f) * 0.14f), c, unit * (0.06f + r.nextFloat() * 0.05f))
    }
    // 3. Tulips: a cup of fat overlapping strokes, then a lighter highlight on top.
    repeat((7 * detail).toInt().coerceAtLeast(4)) {
        val c = pal.blooms[r.nextInt(pal.blooms.size)]
        val center = Offset(r.nextFloat() * w, h * (0.08f + r.nextFloat() * 0.7f))
        val rad = unit * (0.09f + r.nextFloat() * 0.06f)
        repeat(4) { k ->
            val a = -90f + (k - 1.5f) * 17f + (r.nextFloat() - 0.5f) * 10f
            val start = Offset(center.x + (k - 1.5f) * rad * 0.18f, center.y + rad * 0.7f)
            add(stroke(r, start, a, rad * (1.25f + r.nextFloat() * 0.35f), rad * (k - 1.5f) * 0.18f), lerp(c, Color.Black, r.nextFloat() * 0.12f), rad * (0.62f + r.nextFloat() * 0.18f))
        }
        add(stroke(r, Offset(center.x - rad * 0.15f, center.y + rad * 0.3f), -95f, rad * 0.9f, rad * 0.1f), lerp(c, Color.White, 0.45f).copy(alpha = 0.8f), rad * 0.26f, bristle = false)
    }
    // 4. A couple of sweeping light strokes across the top, like the sun in r19.
    repeat(2) {
        val c = pal.blooms[r.nextInt(pal.blooms.size)].copy(alpha = 0.35f)
        add(stroke(r, Offset(-w * 0.1f + r.nextFloat() * w * 0.3f, r.nextFloat() * h * 0.18f), -8f + r.nextFloat() * 20f, w * (0.5f + r.nextFloat() * 0.4f), unit * 0.06f), c, unit * 0.1f, bristle = false)
    }
    return out
}

/** Paints a seeded brushstroke picture behind this element's content. */
fun Modifier.painted(seed: Int, palette: PaintPalette, detail: Float = 1f, grainAlpha: Float = 0.1f): Modifier = drawWithCache {
    val strokes = build(seed, size, palette, detail)
    onDrawBehind {
        drawRect(palette.ground)
        strokes.forEach { s ->
            drawPath(s.path, s.color, style = Stroke(s.width, cap = StrokeCap.Round))
            s.bristles.forEach { (p, c) -> drawPath(p, c, style = Stroke(s.width * 0.14f, cap = StrokeCap.Round)) }
        }
        drawGrain(grainAlpha)
    }
}
