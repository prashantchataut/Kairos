package com.kairos.app.ui.feature.studio

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/*
 * Flat, hand-made-feeling art for the share cards, drawn in code so it scales to 1080px and never
 * needs assets. Every helper draws inside the box it is given; templates decide where boxes go, so
 * art and text never share space unless a template means them to.
 */

fun DrawScope.stipple(rect: Rect, colors: List<Color>, density: Float, seed: Int) {
    val r = Random(seed)
    val n = (rect.width * rect.height / (density * density * 9f)).toInt().coerceAtMost(9000)
    repeat(n) {
        val c = colors[r.nextInt(colors.size)]
        drawCircle(c, (0.5f + r.nextFloat()) * density * 0.9f, Offset(rect.left + r.nextFloat() * rect.width, rect.top + r.nextFloat() * rect.height))
    }
}

fun starPath(c: Offset, outer: Float, inner: Float, points: Int = 5, round: Float = 0f, rotation: Float = -90f): Path {
    val p = Path()
    val pts = (0 until points * 2).map { i ->
        val a = (rotation + i * 180f / points) * (PI.toFloat() / 180f)
        val rad = if (i % 2 == 0) outer else inner
        Offset(c.x + cos(a) * rad, c.y + sin(a) * rad)
    }
    if (round <= 0f) {
        p.moveTo(pts[0].x, pts[0].y); pts.drop(1).forEach { p.lineTo(it.x, it.y) }; p.close()
        return p
    }
    // Rounded corners: start at the midpoint of the first edge, curve through each point.
    fun mid(a: Offset, b: Offset) = Offset((a.x + b.x) / 2, (a.y + b.y) / 2)
    val start = mid(pts.last(), pts[0])
    p.moveTo(start.x, start.y)
    pts.forEachIndexed { i, pt -> val n = mid(pt, pts[(i + 1) % pts.size]); p.quadraticTo(pt.x, pt.y, n.x, n.y) }
    p.close()
    return p
}

fun DrawScope.sparkle(c: Offset, r: Float, color: Color) {
    val p = Path().apply {
        moveTo(c.x, c.y - r); quadraticTo(c.x + r * 0.16f, c.y - r * 0.16f, c.x + r, c.y); quadraticTo(c.x + r * 0.16f, c.y + r * 0.16f, c.x, c.y + r)
        quadraticTo(c.x - r * 0.16f, c.y + r * 0.16f, c.x - r, c.y); quadraticTo(c.x - r * 0.16f, c.y - r * 0.16f, c.x, c.y - r); close()
    }
    drawPath(p, color)
}

/** A flat five-petal flower (r26, n16). */
fun DrawScope.daisy(c: Offset, r: Float, petal: Color, centre: Color, petals: Int = 6, tilt: Float = 0f) {
    rotate(tilt, c) {
        for (i in 0 until petals) {
            val a = (i * 360f / petals) * (PI.toFloat() / 180f)
            val pc = Offset(c.x + cos(a) * r * 0.55f, c.y + sin(a) * r * 0.55f)
            drawOval(petal, Offset(pc.x - r * 0.42f, pc.y - r * 0.42f), Size(r * 0.84f, r * 0.84f))
        }
        drawCircle(centre, r * 0.36f, c)
    }
}

// --- Stickers (n07) -------------------------------------------------------------------------

/** A shiny sticker: holo sequin, jelly flower, chrome star, gem heart, disco ball or a sparkle cluster. */
fun DrawScope.sticker(kind: Int, c: Offset, r: Float) {
    when (kind % 7) {
        0 -> { // sequin
            drawCircle(Brush.sweepGradient(listOf(Color(0xFFFFC9D8), Color(0xFFD7F7C9), Color(0xFFFFE3A1), Color(0xFFC9D8FF), Color(0xFFFFC9D8)), c), r, c)
            drawCircle(Color.White.copy(alpha = 0.9f), r * 0.28f, c)
            drawCircle(Color.White.copy(alpha = 0.6f), r, c, style = Stroke(r * 0.12f))
        }
        1 -> { // jelly flower
            daisy(c, r * 1.1f, Color(0xFFFF8FC8).copy(alpha = 0.9f), Color(0xFFE0337A), petals = 5)
            drawCircle(Color.White.copy(alpha = 0.55f), r * 0.18f, Offset(c.x - r * 0.35f, c.y - r * 0.45f))
        }
        2 -> { // chrome star
            val p = starPath(c, r * 1.15f, r * 0.5f, round = 0.2f)
            drawPath(p, Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFF9AA0A8), Color(0xFFE8EAED), Color(0xFF6C727A)), Offset(c.x - r, c.y - r), Offset(c.x + r, c.y + r)))
        }
        3 -> { // gem heart
            val p = Path().apply {
                moveTo(c.x, c.y + r); cubicTo(c.x - r * 1.5f, c.y - r * 0.1f, c.x - r * 0.7f, c.y - r * 1.3f, c.x, c.y - r * 0.45f)
                cubicTo(c.x + r * 0.7f, c.y - r * 1.3f, c.x + r * 1.5f, c.y - r * 0.1f, c.x, c.y + r); close()
            }
            drawPath(p, Brush.radialGradient(listOf(Color(0xFFFF6FA8), Color(0xFFD1004F)), Offset(c.x - r * 0.3f, c.y - r * 0.3f), r * 1.4f))
            drawCircle(Color.White.copy(alpha = 0.7f), r * 0.16f, Offset(c.x - r * 0.4f, c.y - r * 0.35f))
        }
        4 -> { // disco ball
            drawCircle(Brush.radialGradient(listOf(Color(0xFFF2F2F2), Color(0xFF8D8F94)), Offset(c.x - r * 0.3f, c.y - r * 0.3f), r * 1.3f), r, c)
            val tile = r / 3.2f
            var y = c.y - r
            while (y < c.y + r) {
                var x = c.x - r
                while (x < c.x + r) {
                    if ((x + tile / 2 - c.x).let { it * it } + (y + tile / 2 - c.y).let { it * it } < r * r * 0.9f) drawRect(Color.White.copy(alpha = if (((x + y) / tile).toInt() % 3 == 0) 0.7f else 0.18f), Offset(x + 1f, y + 1f), Size(tile - 2f, tile - 2f))
                    x += tile
                }
                y += tile
            }
        }
        5 -> { // sparkle cluster
            sparkle(c, r, Color(0xFFE8E4FF)); sparkle(Offset(c.x + r * 0.9f, c.y + r * 0.6f), r * 0.55f, Color(0xFFDCD6FF)); sparkle(Offset(c.x - r * 0.6f, c.y + r * 0.9f), r * 0.4f, Color(0xFFEDEAFF))
        }
        else -> { // yellow star
            drawPath(starPath(c, r * 1.1f, r * 0.5f, round = 0.3f), Color(0xFFFFD83D))
            drawPath(starPath(c, r * 1.1f, r * 0.5f, round = 0.3f), Color(0xFFE0A800), style = Stroke(r * 0.08f))
        }
    }
}

// --- Stage (n08) ----------------------------------------------------------------------------

fun DrawScope.curtain(left: Boolean, w: Float, h: Float, color: Color) {
    val x0 = if (left) 0f else size.width - w
    val p = Path().apply {
        if (left) {
            moveTo(0f, 0f); lineTo(w, 0f); cubicTo(w * 0.75f, h * 0.3f, w * 1.05f, h * 0.6f, w * 0.7f, h); lineTo(0f, h); close()
        } else {
            moveTo(size.width, 0f); lineTo(size.width - w, 0f); cubicTo(size.width - w * 0.75f, h * 0.3f, size.width - w * 1.05f, h * 0.6f, size.width - w * 0.7f, h); lineTo(size.width, h); close()
        }
    }
    drawPath(p, color)
    // Hatched folds.
    val r = Random(if (left) 3 else 5)
    repeat(26) {
        val fx = x0 + r.nextFloat() * w * 0.85f
        drawLine(lerp(color, Color(0xFF5A1E28), 0.45f).copy(alpha = 0.6f), Offset(fx, 0f), Offset(fx + (r.nextFloat() - 0.5f) * w * 0.25f, h * (0.5f + r.nextFloat() * 0.5f)), strokeWidth = density * 0.9f)
    }
}

fun DrawScope.pine(base: Offset, h: Float, color: Color) {
    val w = h * 0.5f
    for (tier in 0 until 3) {
        val top = base.y - h + tier * h * 0.25f
        val bottom = top + h * 0.45f
        val tw = w * (0.5f + tier * 0.25f)
        val p = Path().apply { moveTo(base.x, top); lineTo(base.x + tw / 2, bottom); lineTo(base.x - tw / 2, bottom); close() }
        drawPath(p, color)
    }
    drawRect(lerp(color, Color(0xFF4A3524), 0.6f), Offset(base.x - h * 0.03f, base.y - h * 0.08f), Size(h * 0.06f, h * 0.08f))
}

// --- Starry (n09) ---------------------------------------------------------------------------

/** The sleeping star from n09: a soft five-point star with closed eyes and blush. */
fun DrawScope.sleepyStar(c: Offset, r: Float, tilt: Float) {
    rotate(tilt, c) {
        val p = starPath(c, r, r * 0.52f, round = 0.4f)
        drawPath(p, Brush.radialGradient(listOf(Color(0xFFFFE27A), Color(0xFFF5B800)), Offset(c.x - r * 0.2f, c.y - r * 0.2f), r * 1.2f))
        drawPath(p, Color(0xFFB98400), style = Stroke(r * 0.03f))
        val ink = Color(0xFF7A3B12)
        for (s in listOf(-1f, 1f)) {
            val ex = c.x + s * r * 0.2f
            val arc = Path().apply { moveTo(ex - r * 0.1f, c.y); quadraticTo(ex, c.y + r * 0.07f, ex + r * 0.1f, c.y) }
            drawPath(arc, ink, style = Stroke(r * 0.035f, cap = StrokeCap.Round))
            drawCircle(Color(0xFFFF6F61).copy(alpha = 0.55f), r * 0.06f, Offset(ex + s * r * 0.02f, c.y + r * 0.13f))
        }
    }
}

// --- Ring (n11) -----------------------------------------------------------------------------

fun DrawScope.watercolourRings(c: Offset, r: Float, colors: List<Color>, seed: Int) {
    val rnd = Random(seed)
    colors.forEachIndexed { i, col ->
        val rad = r * (1f - i * (0.8f / colors.size))
        repeat(14) {
            val j = (rnd.nextFloat() - 0.5f) * r * 0.05f
            drawCircle(col.copy(alpha = 0.2f), rad + j, Offset(c.x + j * 0.6f, c.y - j * 0.4f))
        }
    }
}

// --- Envelope (n12) -------------------------------------------------------------------------

/** Envelope back, letter, and front pocket; returns the rect of the letter's writable area. */
fun envelopeLetterRect(box: Rect): Rect {
    val lw = box.width * 0.72f
    return Rect(box.center.x - lw / 2, box.top + box.height * 0.08f, box.center.x + lw / 2, box.top + box.height * 0.6f)
}

fun DrawScope.envelopeBack(box: Rect, color: Color) {
    val w = box.width * 0.86f
    val l = box.center.x - w / 2
    val top = box.top + box.height * 0.3f
    drawRoundRect(color, Offset(l, top), Size(w, box.bottom - top), CornerRadius(w * 0.05f))
    // The open flap, a rounded arch above the pocket.
    val arch = Path().apply { moveTo(l + w * 0.12f, top + 1f); cubicTo(l + w * 0.18f, box.top - box.height * 0.05f, l + w * 0.82f, box.top - box.height * 0.05f, l + w * 0.88f, top + 1f); close() }
    drawPath(arch, lerp(color, Color.Black, 0.06f))
}

fun DrawScope.envelopeFront(box: Rect, color: Color, sprig: Color, flower: Color) {
    val w = box.width * 0.86f
    val l = box.center.x - w / 2
    val top = box.top + box.height * 0.52f
    val p = Path().apply {
        moveTo(l, top); lineTo(box.center.x, box.top + box.height * 0.78f); lineTo(l + w, top); lineTo(l + w, box.bottom - w * 0.04f)
        quadraticTo(l + w, box.bottom, l + w * 0.96f, box.bottom); lineTo(l + w * 0.04f, box.bottom); quadraticTo(l, box.bottom, l, box.bottom - w * 0.04f); close()
    }
    drawPath(p, color)
    drawLine(lerp(color, Color.Black, 0.15f), Offset(l, top), Offset(box.center.x, box.top + box.height * 0.78f), strokeWidth = density * 1.2f)
    drawLine(lerp(color, Color.Black, 0.15f), Offset(l + w, top), Offset(box.center.x, box.top + box.height * 0.78f), strokeWidth = density * 1.2f)
    // Little sprigs with flowers in the corners.
    listOf(Offset(l + w * 0.14f, box.bottom - box.height * 0.12f) to -30f, Offset(l + w * 0.86f, box.bottom - box.height * 0.12f) to 210f).forEach { (o, a) ->
        rotate(a, o) {
            drawLine(sprig, o, Offset(o.x + w * 0.16f, o.y), strokeWidth = density * 1.6f, cap = StrokeCap.Round)
            for (k in 1..3) drawOval(sprig, Offset(o.x + w * 0.04f * k, o.y - w * 0.035f), Size(w * 0.035f, w * 0.02f))
            drawCircle(flower, w * 0.018f, Offset(o.x + w * 0.17f, o.y))
        }
    }
}

// --- Split (n15) ----------------------------------------------------------------------------

/** A jigsaw piece with a knob on the right and the top, and a hole on the left. */
fun puzzlePath(c: Offset, s: Float): Path {
    val h = s / 2
    val k = s * 0.18f
    return Path().apply {
        moveTo(c.x - h, c.y - h)
        lineTo(c.x - k, c.y - h); cubicTo(c.x - k * 1.6f, c.y - h - k * 2f, c.x + k * 1.6f, c.y - h - k * 2f, c.x + k, c.y - h)
        lineTo(c.x + h, c.y - h); lineTo(c.x + h, c.y - k); cubicTo(c.x + h + k * 2f, c.y - k * 1.6f, c.x + h + k * 2f, c.y + k * 1.6f, c.x + h, c.y + k)
        lineTo(c.x + h, c.y + h); lineTo(c.x - h, c.y + h); lineTo(c.x - h, c.y + k)
        cubicTo(c.x - h + k * 2f, c.y + k * 1.6f, c.x - h + k * 2f, c.y - k * 1.6f, c.x - h, c.y - k); close()
    }
}

fun DrawScope.cloudPuff(c: Offset, w: Float, color: Color) {
    drawCircle(color, w * 0.22f, Offset(c.x - w * 0.22f, c.y))
    drawCircle(color, w * 0.3f, Offset(c.x, c.y - w * 0.12f))
    drawCircle(color, w * 0.2f, Offset(c.x + w * 0.26f, c.y + w * 0.02f))
    drawRoundRect(color, Offset(c.x - w * 0.44f, c.y), Size(w * 0.88f, w * 0.2f), CornerRadius(w * 0.1f))
}

// --- Garden (n16) ---------------------------------------------------------------------------

fun DrawScope.flowerRow(box: Rect, seed: Int) {
    val stem = Color(0xFF3D8A4A)
    val n = 3
    val cell = box.width / n
    val r = Random(seed)
    for (i in 0 until n) {
        val cx = box.left + cell * (i + 0.5f)
        when (i) {
            0 -> for (k in 0 until 3) {
                val x = cx + (k - 1) * cell * 0.28f
                val top = box.top + box.height * (0.12f + r.nextFloat() * 0.25f)
                drawLine(stem, Offset(x, box.bottom), Offset(x, top), strokeWidth = cell * 0.05f, cap = StrokeCap.Round)
                drawOval(stem, Offset(x, box.bottom - box.height * 0.35f), Size(cell * 0.18f, cell * 0.08f))
                daisy(Offset(x, top), cell * 0.16f, Color(0xFFFFF2A8), Color(0xFFF2A21F), petals = 7, tilt = r.nextFloat() * 40f)
            }
            1 -> {
                drawLine(stem, Offset(cx, box.bottom), Offset(cx - cell * 0.1f, box.top + box.height * 0.3f), strokeWidth = cell * 0.05f, cap = StrokeCap.Round)
                daisy(Offset(cx - cell * 0.1f, box.top + box.height * 0.32f), cell * 0.32f, Color(0xFF8E7CE0), Color(0xFFF2B81F), petals = 9, tilt = 15f)
                daisy(Offset(cx + cell * 0.18f, box.top + box.height * 0.72f), cell * 0.18f, Color(0xFF9F8FE8), Color(0xFFF2B81F), petals = 8)
            }
            else -> for (k in 0 until 3) {
                val x = cx + (k - 1) * cell * 0.3f
                val top = box.top + box.height * (0.08f + k % 2 * 0.12f)
                drawLine(stem, Offset(x, box.bottom), Offset(x, top + cell * 0.14f), strokeWidth = cell * 0.045f, cap = StrokeCap.Round)
                val bell = Path().apply {
                    moveTo(x - cell * 0.1f, top); lineTo(x - cell * 0.06f, top + cell * 0.06f); lineTo(x, top - cell * 0.01f); lineTo(x + cell * 0.06f, top + cell * 0.06f); lineTo(x + cell * 0.1f, top)
                    lineTo(x + cell * 0.08f, top + cell * 0.2f); quadraticTo(x, top + cell * 0.26f, x - cell * 0.08f, top + cell * 0.2f); close()
                }
                drawPath(bell, Color(0xFF7FA8E8))
            }
        }
    }
}

// --- Leaf (n18) -----------------------------------------------------------------------------

fun leafPath(box: Rect): Path {
    val w = box.width; val h = box.height
    fun p(x: Float, y: Float) = Offset(box.left + x * w, box.top + y * h)
    return Path().apply {
        val a = p(0.62f, 0.98f); moveTo(a.x, a.y)
        listOf(
            listOf(p(0.5f, 0.8f), p(0.18f, 0.86f), p(0.02f, 0.62f)),
            listOf(p(0.2f, 0.62f), p(0.12f, 0.42f), p(0.0f, 0.3f)),
            listOf(p(0.22f, 0.32f), p(0.28f, 0.18f), p(0.36f, 0.0f)),
            listOf(p(0.46f, 0.18f), p(0.5f, 0.24f), p(0.62f, 0.04f)),
            listOf(p(0.66f, 0.22f), p(0.76f, 0.24f), p(0.98f, 0.18f)),
            listOf(p(0.86f, 0.36f), p(0.9f, 0.48f), p(1.0f, 0.58f)),
            listOf(p(0.8f, 0.64f), p(0.66f, 0.74f), p(0.7f, 0.98f)),
        ).forEach { (c1, c2, e) -> cubicTo(c1.x, c1.y, c2.x, c2.y, e.x, e.y) }
        close()
    }
}

// --- Portal (n01) ---------------------------------------------------------------------------

fun DrawScope.portal(box: Rect, bg: Color, glow: Color) {
    rotate(-18f, box.center) {
        drawOval(lerp(bg, Color.Black, 0.35f), Offset(box.left, box.top), box.size)
        drawOval(Brush.radialGradient(listOf(glow.copy(alpha = 0.35f), Color.Transparent), box.center, box.width * 0.6f), Offset(box.left, box.top), box.size)
    }
    val star = Offset(box.center.x + box.width * 0.08f, box.center.y - box.height * 0.05f)
    drawCircle(Brush.radialGradient(listOf(glow.copy(alpha = 0.8f), glow.copy(alpha = 0f)), star, box.width * 0.32f), box.width * 0.32f, star)
    sparkle(star, box.width * 0.13f, Color(0xFFFFF4DA))
    sparkle(Offset(star.x + box.width * 0.2f, star.y - box.height * 0.2f), box.width * 0.06f, Color(0xFFCFE3D7))
    sparkle(Offset(star.x - box.width * 0.22f, star.y + box.height * 0.18f), box.width * 0.04f, Color(0xFFCFE3D7))
}

/** A dashed rule. */
fun DrawScope.dashed(y: Float, color: Color) =
    drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = density * 0.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(density * 5f, density * 4f)))

