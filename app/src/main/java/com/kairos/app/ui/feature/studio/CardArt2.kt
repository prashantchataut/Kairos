package com.kairos.app.ui.feature.studio

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import com.kairos.app.ui.art.drawGrain
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/*
 * Art for the round-three card designs (m01..m12). Each helper draws only inside the rect it is
 * given; layouts decide where rects go so art and text never collide.
 */

/** m02: a huge soft five-petal flower, blurred by stacking translucent radial blooms, with a hot centre. */
fun DrawScope.blurBloom(box: Rect, petal: Color, heart: Color) {
    val c = box.center
    val r = minOf(box.width, box.height) * 0.5f
    for (i in 0 until 5) {
        val a = (i * 72f - 90f) * (PI.toFloat() / 180f)
        val pc = Offset(c.x + cos(a) * r * 0.42f, c.y + sin(a) * r * 0.42f)
        drawCircle(Brush.radialGradient(listOf(petal, petal.copy(alpha = 0.85f), petal.copy(alpha = 0f)), pc, r * 0.62f), r * 0.62f, pc)
    }
    drawCircle(Brush.radialGradient(listOf(petal, petal.copy(alpha = 0f)), c, r * 0.7f), r * 0.7f, c)
    for (i in 0 until 5) {
        val a = (i * 72f - 54f) * (PI.toFloat() / 180f)
        val pc = Offset(c.x + cos(a) * r * 0.12f, c.y + sin(a) * r * 0.12f)
        drawCircle(Brush.radialGradient(listOf(heart, heart.copy(alpha = 0.9f), heart.copy(alpha = 0f)), pc, r * 0.2f), r * 0.2f, pc)
    }
    drawCircle(Brush.radialGradient(listOf(heart, heart.copy(alpha = 0f)), c, r * 0.24f), r * 0.24f, c)
}

/** m11, m12: a flower drawn as a halftone dot field, dot size following an invisible soft shape. */
fun DrawScope.halftoneFlower(box: Rect, dot: Color, seed: Int) {
    val r = Random(seed)
    // The shape: a few soft blobs (heads) and a curved stem with two leaves, as a density function.
    val heads = List(3) { k -> Offset(box.left + box.width * (0.3f + k * 0.22f + r.nextFloat() * 0.06f), box.top + box.height * (0.18f + (k % 2) * 0.16f + r.nextFloat() * 0.05f)) }
    val headR = box.width * 0.17f
    fun density(x: Float, y: Float): Float {
        var d = 0f
        // Daisy heads: petals as an angular rhythm around each centre, a dense disc in the middle.
        heads.forEachIndexed { k, h ->
            val dx = x - h.x; val dy = y - h.y
            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
            val rr = headR * (if (k == 1) 1.15f else 0.85f)
            val ang = kotlin.math.atan2(dy, dx)
            val petal = rr * (0.55f + 0.45f * kotlin.math.abs(cos(ang * 4f + k)))
            if (dist < rr * 0.28f) d = maxOf(d, 1f)
            else if (dist < petal) d = maxOf(d, 0.55f + 0.45f * (1f - dist / petal))
        }
        val base = Offset(box.left + box.width * 0.5f, box.bottom)
        heads.forEach { h ->
            if (y > h.y + headR * 0.3f) {
                val t = ((y - h.y) / (base.y - h.y)).coerceIn(0f, 1f)
                val sx = h.x + (base.x - h.x) * t + sin(t * 3f) * box.width * 0.04f
                d = maxOf(d, (1f - kotlin.math.abs(x - sx) / (box.width * 0.016f)).coerceIn(0f, 1f) * 0.85f)
            }
        }
        listOf(Offset(box.left + box.width * 0.38f, box.top + box.height * 0.7f), Offset(box.left + box.width * 0.62f, box.top + box.height * 0.62f)).forEach { l ->
            val dx = (x - l.x) / (box.width * 0.12f); val dy = (y - l.y) / (box.height * 0.03f)
            d = maxOf(d, (1f - (dx * dx + dy * dy)).coerceIn(0f, 1f) * 0.8f)
        }
        return d
    }
    val step = box.width / 52f
    var y = box.top
    while (y < box.bottom) {
        var x = box.left
        while (x < box.right) {
            val d = density(x, y)
            if (d > 0.04f) drawCircle(dot.copy(alpha = 0.55f + 0.45f * d), step * 0.5f * (0.25f + 0.75f * d), Offset(x, y))
            x += step
        }
        y += step
    }
}

/** m09: a blurred eclipse, a warm ring around a cool core. */
fun DrawScope.eclipse(c: Offset, r: Float, ring: Color, rim: Color, core: Color) {
    drawCircle(Brush.radialGradient(listOf(ring.copy(alpha = 0f), ring.copy(alpha = 0.95f), rim.copy(alpha = 0.9f), rim.copy(alpha = 0f)), c, r * 1.25f), r * 1.25f, c)
    val cc = Offset(c.x + r * 0.22f, c.y - r * 0.12f)
    drawCircle(Brush.radialGradient(listOf(core, core.copy(alpha = 0.9f), core.copy(alpha = 0f)), cc, r * 0.85f), r * 0.85f, cc)
}

/** m08: the breathing orb, deep at the core and fading out like light. */
fun DrawScope.breathOrb(c: Offset, r: Float, deep: Color, light: Color) {
    drawCircle(Brush.radialGradient(listOf(deep, lerp(deep, light, 0.5f), light.copy(alpha = 0.55f), light.copy(alpha = 0f)), c, r), r, c)
}

/** m07: cyanotype scratches and light leaks over blue. */
fun DrawScope.scratches(seed: Int, color: Color) {
    val r = Random(seed)
    repeat(60) {
        val x = r.nextFloat() * size.width; val y = r.nextFloat() * size.height
        val len = size.width * (0.02f + r.nextFloat() * 0.12f)
        val a = (r.nextFloat() - 0.5f) * 1.2f
        drawLine(color.copy(alpha = 0.1f + r.nextFloat() * 0.25f), Offset(x, y), Offset(x + cos(a) * len, y + sin(a) * len), strokeWidth = density * (0.4f + r.nextFloat()))
    }
    repeat(6) {
        val o = Offset(r.nextFloat() * size.width, r.nextFloat() * size.height)
        drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.12f), Color.Transparent), o, size.width * 0.2f), size.width * 0.2f, o)
    }
}

/** m06: a pixel-art object (an apple, a heart or a flower) on a grid, chosen from the seed. */
fun DrawScope.pixelObject(box: Rect, color: Color, seed: Int) {
    val apple = listOf(
        "......##....", ".....##.....", "..######.##.", ".##########.", "############", "############",
        "############", "############", ".##########.", ".##########.", "..########..", "...##..##...",
    )
    val heart = listOf(
        "............", ".###....###.", "#####..#####", "############", "############", "############",
        ".##########.", "..########..", "...######...", "....####....", ".....##.....", "............",
    )
    val flower = listOf(
        "....####....", "...######...", ".##.####.##.", "####.##.####", "######.#####", ".####..####.",
        "....####....", ".....##.....", "..##.##.....", "...####.##..", ".....####...", ".....##.....",
    )
    val grid = listOf(apple, heart, flower)[seed.mod(3)]
    val cell = minOf(box.width, box.height) / 12f
    val ox = box.center.x - cell * 6; val oy = box.center.y - cell * 6
    val r = Random(seed)
    grid.forEachIndexed { y, row -> row.forEachIndexed { x, ch ->
        if (ch == '#' && r.nextFloat() > 0.04f) drawRect(color, Offset(ox + x * cell, oy + y * cell), Size(cell * 1.02f, cell * 1.02f))
    } }
}

/** m01: an oval window onto a soft dream landscape: pink sky, a cloud, rolling green hills. */
fun DrawScope.dreamWindow(box: Rect, sky: List<Color>) {
    val oval = Path().apply { addOval(box) }
    clipPath(oval) {
        drawRect(Brush.verticalGradient(sky, startY = box.top, endY = box.bottom), box.topLeft, box.size)
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFF8FA3).copy(alpha = 0.7f), Color.Transparent), Offset(box.center.x, box.top + box.height * 0.18f), box.width * 0.5f), box.width * 0.5f, Offset(box.center.x, box.top + box.height * 0.18f))
        cloudPuff(Offset(box.left + box.width * 0.3f, box.top + box.height * 0.42f), box.width * 0.42f, Color.White.copy(alpha = 0.9f))
        val far = Path().apply { moveTo(box.left, box.top + box.height * 0.62f); cubicTo(box.left + box.width * 0.3f, box.top + box.height * 0.5f, box.left + box.width * 0.6f, box.top + box.height * 0.58f, box.right, box.top + box.height * 0.52f); lineTo(box.right, box.bottom); lineTo(box.left, box.bottom); close() }
        drawPath(far, Color(0xFF9C8CA0))
        val mid = Path().apply { moveTo(box.left, box.top + box.height * 0.74f); cubicTo(box.left + box.width * 0.35f, box.top + box.height * 0.6f, box.left + box.width * 0.7f, box.top + box.height * 0.76f, box.right, box.top + box.height * 0.66f); lineTo(box.right, box.bottom); lineTo(box.left, box.bottom); close() }
        drawPath(mid, Brush.verticalGradient(listOf(Color(0xFFA6D86A), Color(0xFF5C9E3A)), startY = box.top + box.height * 0.62f, endY = box.bottom))
        val near = Path().apply { moveTo(box.left, box.top + box.height * 0.86f); cubicTo(box.left + box.width * 0.4f, box.top + box.height * 0.76f, box.left + box.width * 0.6f, box.top + box.height * 0.9f, box.right, box.top + box.height * 0.82f); lineTo(box.right, box.bottom); lineTo(box.left, box.bottom); close() }
        drawPath(near, Brush.verticalGradient(listOf(Color(0xFF8CCB5A), Color(0xFF3E7D2A)), startY = box.top + box.height * 0.76f, endY = box.bottom))
        drawGrain(0.18f)
    }
    drawOval(Color.White.copy(alpha = 0.6f), box.topLeft, box.size, style = Stroke(density * 1.2f))
}

/**
 * m05: a winding pastel stem with glowing line flowers and tiny "fig." labels. Returns the anchor
 * points along the stem where words can sit (kept clear of the flowers).
 */
fun DrawScope.specimenStem(box: Rect, seed: Int): List<Offset> {
    val path = Path().apply {
        moveTo(box.left + box.width * 0.1f, box.bottom)
        cubicTo(box.left - box.width * 0.05f, box.top + box.height * 0.6f, box.left + box.width * 0.5f, box.top + box.height * 0.85f, box.left + box.width * 0.4f, box.top + box.height * 0.5f)
        cubicTo(box.left + box.width * 0.3f, box.top + box.height * 0.2f, box.left + box.width * 0.8f, box.top + box.height * 0.35f, box.left + box.width * 0.75f, box.top + box.height * 0.08f)
    }
    drawPath(path, Brush.linearGradient(listOf(Color(0xFFFFF3A8), Color(0xFFA8F0E0), Color(0xFFD7B8FF), Color(0xFFFFB8D9)), Offset(box.left, box.bottom), Offset(box.right, box.top)), style = Stroke(density * 3f, cap = StrokeCap.Round))
    val pm = PathMeasure().apply { setPath(path, false) }
    val len = pm.length
    val flowerColors = listOf(Color(0xFFFF8FB8), Color(0xFF6FB8FF), Color(0xFFFFE07A), Color(0xFFFFFFFF), Color(0xFFB8F5C8))
    listOf(0.22f, 0.48f, 0.74f, 1f).forEachIndexed { k, f ->
        val p = pm.getPosition(len * f)
        val col = flowerColors[(k + seed).mod(flowerColors.size)]
        val rr = box.width * (if (k == 3) 0.09f else 0.045f)
        drawCircle(Brush.radialGradient(listOf(col.copy(alpha = 0.6f), Color.Transparent), p, rr * 2.4f), rr * 2.4f, p)
        for (i in 0 until 10) {
            val a = i * 36f * (PI.toFloat() / 180f)
            drawLine(col, p, Offset(p.x + cos(a) * rr, p.y + sin(a) * rr), strokeWidth = density * 1.2f, cap = StrokeCap.Round)
        }
        drawCircle(Color.White, rr * 0.25f, p)
        // A tiny leaf pair below each flower.
        val q = pm.getPosition(len * (f - 0.06f).coerceAtLeast(0f))
        rotate(-30f, q) { drawOval(Color(0xFF2E9E6A), Offset(q.x, q.y - rr * 0.25f), Size(rr * 1.2f, rr * 0.5f)) }
        drawLine(Color.White.copy(alpha = 0.4f), p, Offset(p.x + box.width * 0.08f, p.y + box.height * 0.03f), strokeWidth = density * 0.6f)
    }
    return listOf(0.12f, 0.36f, 0.62f, 0.88f).map { pm.getPosition(len * it) }
}
