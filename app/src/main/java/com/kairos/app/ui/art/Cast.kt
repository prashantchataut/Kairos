package com.kairos.app.ui.art

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.theme.K
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/** The cast's body shapes (#606-03 blobs, #606-09 mood shapes). */
enum class Body { Cloud, Bubble, Flower, Clover, Sun, Hex, Triangle }

/** Faces stay minimal: two white pills, sometimes a tiny mouth (#606-09). */
enum class Face { Open, Happy, Wow, Sleepy, Sad, Wink }

/**
 * A closed, smooth body outline centred at the origin with radius ~1, built from a polar radius
 * function and a Catmull-Rom loop so every shape has soft, hand-made curves.
 */
private fun bodyPath(body: Body, r: Float): Path {
    if (body == Body.Hex) return roundedPolygon(6, r * 0.98f, 0.32f, -90f)
    if (body == Body.Triangle) return roundedPolygon(3, r * 1.08f, 0.42f, -90f, yOffset = r * 0.14f)
    val n = 180
    val pts = List(n) { i ->
        val a = i.toFloat() / n * 2f * PI.toFloat() - PI.toFloat() / 2f
        val k = when (body) {
            Body.Cloud -> 0.82f + 0.18f * abs(sin(a * 3.5f)).pow(0.62f)
            Body.Bubble -> 0.92f + 0.08f * abs(sin(a * 2.5f)).pow(0.7f)
            Body.Flower -> 0.74f + 0.26f * ((cos(a * 5f) + 1f) / 2f).pow(0.8f)
            Body.Clover -> 0.7f + 0.3f * ((cos(a * 4f + PI.toFloat() / 4f) + 1f) / 2f).pow(0.9f)
            Body.Sun -> 0.9f + 0.1f * ((cos(a * 9f) + 1f) / 2f)
            Body.Hex, Body.Triangle -> 1f
        }
        Offset(cos(a) * r * k, sin(a) * r * k)
    }
    return Path().apply {
        moveTo(pts[0].x, pts[0].y)
        for (i in pts.indices) {
            val p0 = pts[(i - 1 + n) % n]; val p1 = pts[i]; val p2 = pts[(i + 1) % n]; val p3 = pts[(i + 2) % n]
            cubicTo(p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f, p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f, p2.x, p2.y)
        }
        close()
    }
}

/** The speech-bubble tail, drawn as its own shape so fill rules never punch a hole. */
private fun bubbleTail(r: Float) = Path().apply {
    moveTo(-r * 0.62f, r * 0.5f)
    quadraticTo(-r * 0.72f, r * 0.98f, -r * 1.02f, r * 1.12f)
    quadraticTo(-r * 0.46f, r * 1.06f, -r * 0.12f, r * 0.8f)
    close()
}

/** A polygon with generously rounded corners (#606-09's chunky hexagon and triangle). */
private fun roundedPolygon(sides: Int, r: Float, round: Float, rotationDeg: Float, yOffset: Float = 0f): Path {
    val pts = List(sides) { i ->
        val a = (rotationDeg + i * 360f / sides) * (PI / 180f).toFloat()
        Offset(cos(a) * r, sin(a) * r + yOffset)
    }
    return Path().apply {
        for (i in 0 until sides) {
            val prev = pts[(i - 1 + sides) % sides]; val cur = pts[i]; val next = pts[(i + 1) % sides]
            val a = Offset(cur.x + (prev.x - cur.x) * round, cur.y + (prev.y - cur.y) * round)
            val b = Offset(cur.x + (next.x - cur.x) * round, cur.y + (next.y - cur.y) * round)
            if (i == 0) moveTo(a.x, a.y) else lineTo(a.x, a.y)
            quadraticTo(cur.x, cur.y, b.x, b.y)
        }
        close()
    }
}

/**
 * Draws one character into the current scope (centre of the scope, radius [r]).
 * [blink] 1 = eyes open, 0 = closed. [look] shifts the eyes left/right (-1..1).
 */
fun DrawScope.character(body: Body, color: Color, r: Float, face: Face, blink: Float = 1f, look: Float = 0f, center: Offset = Offset(size.width / 2, size.height / 2)) {
    translate(center.x, center.y) {
        val path = bodyPath(body, r)
        if (body == Body.Bubble) drawPath(bubbleTail(r), color)
        drawPath(path, color)
        // A whisper of airbrushed light and grain keeps the flat shape tactile (#606-24).
        clipPath(path) {
            drawCircle(
                androidx.compose.ui.graphics.Brush.radialGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.Transparent), Offset(-r * 0.4f, -r * 0.5f), r * 1.1f,
                ),
                r * 1.6f, Offset.Zero,
            )
            drawCircle(
                androidx.compose.ui.graphics.Brush.radialGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.12f)), Offset(-r * 0.2f, -r * 0.3f), r * 1.5f,
                ),
                r * 1.6f, Offset.Zero,
            )
            translate(-center.x, -center.y) { grain(0.06f) }
        }
        val eyeY = if (body == Body.Triangle) r * 0.26f else -r * 0.04f
        val gap = r * 0.17f
        val ew = r * 0.18f
        val eh = r * 0.34f
        val lx = look * r * 0.1f
        fun pill(x: Float, h: Float = eh, w: Float = ew) {
            val hh = (h * blink).coerceAtLeast(w * 0.5f)
            drawRoundRect(Color.White, Offset(x - w / 2 + lx, eyeY - hh / 2), Size(w, hh), CornerRadius(w / 2))
        }
        fun arc(x: Float, down: Boolean) {
            val rw = ew * 0.62f
            drawArc(
                Color.White,
                startAngle = if (down) 15f else 195f, sweepAngle = 150f, useCenter = false,
                topLeft = Offset(x - rw + lx, eyeY - rw), size = Size(rw * 2, rw * 2),
                style = Stroke(ew * 0.55f, cap = StrokeCap.Round),
            )
        }
        when (face) {
            Face.Open -> { pill(-gap); pill(gap) }
            Face.Wow -> { pill(-gap, eh * 1.25f, ew * 1.25f); pill(gap, eh * 1.25f, ew * 1.25f) }
            Face.Happy -> { arc(-gap, false); arc(gap, false) }
            Face.Sleepy -> { arc(-gap, true); arc(gap, true) }
            Face.Wink -> { arc(-gap, false); pill(gap) }
            Face.Sad -> {
                rotate(-14f, Offset(-gap, eyeY)) { pill(-gap, eh * 0.8f) }
                rotate(14f, Offset(gap, eyeY)) { pill(gap, eh * 0.8f) }
            }
        }
        // Mood faces (#606-09) get a small mouth; the cast (#606-03) doesn't.
        if (body == Body.Hex || body == Body.Triangle) {
            val my = eyeY + r * 0.3f
            val smile = when (face) { Face.Sad -> -1f; Face.Sleepy, Face.Open -> 0.4f; else -> 1f }
            val m = Path().apply {
                moveTo(-r * 0.12f + lx, my)
                quadraticTo(lx, my + r * 0.12f * smile, r * 0.12f + lx, my)
            }
            drawPath(m, Color.White, style = Stroke(ew * 0.7f, cap = StrokeCap.Round))
        }
    }
}

/**
 * A living cast member: bobs, blinks on its own, looks around, and squishes when poked.
 * [entrance] pops it in from small with overshoot the first time it composes.
 */
@Composable
fun Buddy(
    body: Body,
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    face: Face = Face.Open,
    seed: Int = body.ordinal,
    entrance: Boolean = false,
    interactive: Boolean = true,
    description: String? = null,
) {
    val calm = K.calm
    val rnd = remember(seed) { Random(seed) }
    val period = remember(seed) { 2600 + rnd.nextInt(900) }
    val bob by if (calm) remember { mutableFloatStateOf(0f) } else rememberInfiniteTransition(label = "bob").animateFloat(
        0f, 2f * PI.toFloat(), infiniteRepeatable(tween(period, easing = LinearEasing), RepeatMode.Restart), label = "b",
    )
    var blink by remember { mutableFloatStateOf(1f) }
    var look by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(calm, seed) {
        if (calm) return@LaunchedEffect
        val r = Random(seed * 7 + 3)
        while (true) {
            delay(1800L + r.nextLong(2600))
            val a = Animatable(1f)
            a.animateTo(0.05f, tween(70)) { blink = value }
            a.animateTo(1f, tween(120)) { blink = value }
            if (r.nextFloat() < 0.35f) {
                val l = Animatable(look)
                l.animateTo(if (look == 0f) (if (r.nextBoolean()) 1f else -1f) else 0f, spring(0.8f, 300f)) { look = value }
            }
        }
    }
    val pop = remember { Animatable(if (entrance && !calm) 0.55f else 1f) }
    LaunchedEffect(Unit) { if (pop.value < 1f) pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 320f)) }
    val squish = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var giggle by remember { mutableFloatStateOf(0f) }
    val tap = if (interactive) Modifier.pressable(scale = 0.92f, label = description ?: "Poke") {
        scope.launch {
            giggle = 1f
            squish.snapTo(1f)
            squish.animateTo(0f, spring(dampingRatio = 0.28f, stiffness = 260f))
            delay(350); giggle = 0f
        }
    } else Modifier
    Canvas(
        modifier
            .size(size)
            .semantics { if (description != null) contentDescription = description }
            .then(tap)
    ) {
        val r = this.size.minDimension * 0.4f
        val lift = sin(bob) * r * 0.06f
        val stretch = sin(bob) * 0.03f + squish.value * 0.14f * sin(squish.value * 12f)
        val tilt = sin(bob * 0.5f) * 3f
        scale(pop.value, pivot = center) {
            scale(1f - stretch, 1f + stretch, pivot = Offset(center.x, center.y + r)) {
                rotate(tilt, center) {
                    character(body, color, r, if (giggle > 0f) Face.Happy else face, blink, look, Offset(center.x, center.y - lift))
                }
            }
        }
    }
}
