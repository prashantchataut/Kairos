package com.kairos.app.ui.art

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kairos.app.engine.daily.Mood
import com.kairos.app.ui.theme.Kairos
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val Ink = Color(0xFF1B2340)
private val Blush = Color(0xFFFF9E8F)

fun moodColor(mood: Mood?): Color = when (mood) {
    Mood.Stormy -> Color(0xFFB8B3DE)
    Mood.Cloudy -> Color(0xFFC7D4EA)
    Mood.Okay, null -> Color(0xFFB9D5FF)
    Mood.Sunny -> Color(0xFFFFDE94)
    Mood.Glowing -> Color(0xFFFFBFB0)
}

/**
 * Kiri — a little mist sprite who lives in the pond and feels things with you.
 * A wobbling liquid body, blinking eyes, and a mouth that eases between moods.
 */
@Composable
fun Kiri(
    mood: Mood?,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    alive: Boolean = true,
) {
    val reduced = Kairos.reducedMotion || !alive
    val t = rememberInfiniteTransition(label = "kiri")
    val phase by t.animateFloat(0f, (2 * PI).toFloat(), infiniteRepeatable(tween(5200, easing = LinearEasing)), label = "phase")
    val bob by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob")
    val blink by t.animateFloat(
        1f, 1f,
        infiniteRepeatable(keyframes { durationMillis = 4200; 1f at 0; 1f at 3900; 0.08f at 3990; 1f at 4100 }),
        label = "blink",
    )
    val sparkle by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing)), label = "sparkle")

    val body by animateColorAsState(moodColor(mood), tween(700), label = "body")
    val smile by animateFloatAsState(
        when (mood) { Mood.Stormy -> -0.8f; Mood.Cloudy -> -0.25f; Mood.Okay, null -> 0.35f; Mood.Sunny -> 0.8f; Mood.Glowing -> 1f },
        tween(600), label = "smile",
    )
    val open by animateFloatAsState(if (mood == Mood.Glowing) 1f else 0f, tween(500), label = "open")
    val happyEyes by animateFloatAsState(if (mood == Mood.Glowing || mood == Mood.Sunny) 1f else 0f, tween(500), label = "eyes")

    Canvas(
        modifier
            .size(size)
            .semantics { contentDescription = "Kiri, feeling ${mood?.label?.lowercase() ?: "curious"}" },
    ) {
        val p = if (reduced) 0f else phase
        val b = if (reduced) 0.5f else bob
        val bl = if (reduced) 1f else blink
        drawKiri(body, p, b, bl, smile, open, happyEyes, mood, if (reduced) 0.3f else sparkle)
    }
}

private fun DrawScope.drawKiri(
    body: Color, phase: Float, bob: Float, blink: Float, smile: Float, open: Float, happyEyes: Float, mood: Mood?, sparkle: Float,
) {
    val w = size.width
    val r = w * 0.36f
    val cx = w / 2f
    val lift = (bob - 0.5f) * w * 0.05f
    val cy = size.height * 0.54f + lift
    val squash = 1f + (bob - 0.5f) * 0.05f

    // soft contact shadow on the water
    val shadowScale = 1f - (bob - 0.5f) * 0.18f
    drawOval(
        Brush.radialGradient(listOf(Ink.copy(alpha = 0.16f), Color.Transparent), center = Offset(cx, size.height * 0.93f), radius = r * 1.05f * shadowScale),
        topLeft = Offset(cx - r * shadowScale, size.height * 0.9f), size = Size(2 * r * shadowScale, r * 0.2f),
    )

    if (mood == Mood.Stormy || mood == Mood.Cloudy) {
        val n = if (mood == Mood.Stormy) 4 else 2
        repeat(n) { i ->
            val x = cx + (i - (n - 1) / 2f) * r * 0.55f
            val fall = ((sparkle + i * 0.27f) % 1f)
            val y = cy - r * 1.25f + fall * r * 0.5f
            drawDrop(Offset(x, y), r * 0.09f, Color(0xFF7FA6FF).copy(alpha = (1f - fall) * 0.8f))
        }
    }

    val path = Path()
    val steps = 72
    for (i in 0..steps) {
        val a = (i / steps.toFloat()) * 2f * PI.toFloat()
        val wob = 1f + 0.035f * sin(3 * a + phase) + 0.022f * sin(5 * a - phase * 1.3f)
        var x = cos(a) * r * wob * (2f - squash)
        var y = sin(a) * r * wob * squash
        if (y > 0) y *= 0.9f
        // a soft crown so Kiri reads as a mist-drop, not a ball
        if (y < 0) {
            val crown = (1f - kotlin.math.abs(cos(a))).let { it * it * it }
            y -= crown * r * 0.28f
            x *= 1f - crown * 0.12f
        }
        if (i == 0) path.moveTo(cx + x, cy + y) else path.lineTo(cx + x, cy + y)
    }
    path.close()

    drawPath(
        path,
        Brush.radialGradient(
            listOf(Color.White.copy(alpha = 0.95f), body, body.darken(0.12f)),
            center = Offset(cx - r * 0.35f, cy - r * 0.55f), radius = r * 2.1f,
        ),
    )
    drawPath(path, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0f)), start = Offset(cx - r, cy - r), end = Offset(cx + r * 0.4f, cy + r)), style = Stroke(width = r * 0.05f))
    drawPath(path, body.darken(0.25f).copy(alpha = 0.35f), style = Stroke(width = r * 0.02f))

    // glossy highlight
    drawArc(
        Color.White.copy(alpha = 0.85f), startAngle = 200f, sweepAngle = 50f, useCenter = false,
        topLeft = Offset(cx - r * 0.72f, cy - r * 0.78f), size = Size(r * 1.1f, r * 1.1f),
        style = Stroke(width = r * 0.09f, cap = StrokeCap.Round),
    )
    drawCircle(Color.White.copy(alpha = 0.9f), r * 0.05f, Offset(cx - r * 0.12f, cy - r * 0.78f))

    // face
    val eyeY = cy - r * 0.02f
    val eyeDx = r * 0.33f
    for (s in listOf(-1f, 1f)) {
        val c = Offset(cx + s * eyeDx, eyeY)
        if (happyEyes > 0.5f) {
            val arc = Path().apply {
                moveTo(c.x - r * 0.11f, c.y + r * 0.03f)
                quadraticTo(c.x, c.y - r * 0.13f * happyEyes, c.x + r * 0.11f, c.y + r * 0.03f)
            }
            drawPath(arc, Ink, style = Stroke(width = r * 0.06f, cap = StrokeCap.Round))
        } else {
            val ew = r * 0.12f
            val eh = r * 0.17f * blink
            drawOval(Ink, topLeft = Offset(c.x - ew / 2, c.y - eh / 2), size = Size(ew, eh))
            if (blink > 0.5f) drawCircle(Color.White, r * 0.03f, Offset(c.x + ew * 0.18f, c.y - eh * 0.22f))
        }
        drawOval(
            Blush.copy(alpha = 0.45f + 0.2f * happyEyes),
            topLeft = Offset(c.x + s * r * 0.12f - r * 0.12f, c.y + r * 0.15f), size = Size(r * 0.24f, r * 0.12f),
        )
    }
    val my = cy + r * 0.24f
    val mw = r * (0.13f + 0.05f * open)
    if (open > 0.05f) {
        val mouth = Path().apply {
            moveTo(cx - mw, my)
            quadraticTo(cx, my + r * 0.26f * open, cx + mw, my)
            close()
        }
        drawPath(mouth, Ink)
        drawOval(Color(0xFFFF8A8A), topLeft = Offset(cx - mw * 0.45f, my + r * 0.07f * open), size = Size(mw * 0.9f, r * 0.08f * open))
    } else {
        val mouth = Path().apply {
            moveTo(cx - mw, my)
            quadraticTo(cx, my + r * 0.16f * smile, cx + mw, my)
        }
        drawPath(mouth, Ink, style = Stroke(width = r * 0.055f, cap = StrokeCap.Round))
    }

    if (mood == Mood.Glowing || mood == Mood.Sunny) {
        val n = if (mood == Mood.Glowing) 4 else 2
        repeat(n) { i ->
            val a = sparkle * 2f * PI.toFloat() * 0.25f + i * (2f * PI.toFloat() / n)
            val pos = Offset(cx + cos(a) * r * 1.3f, cy - r * 0.2f + sin(a) * r * 0.9f)
            val tw = 0.6f + 0.4f * sin(sparkle * 2f * PI.toFloat() * 2 + i)
            drawSparkle(pos, r * 0.13f * tw, Color(0xFFFFC857))
        }
    }
}

fun DrawScope.drawSparkle(center: Offset, s: Float, color: Color, rotation: Float = 0f) {
    rotate(rotation, center) {
        val p = Path().apply {
            moveTo(center.x, center.y - s)
            quadraticTo(center.x + s * 0.12f, center.y - s * 0.12f, center.x + s, center.y)
            quadraticTo(center.x + s * 0.12f, center.y + s * 0.12f, center.x, center.y + s)
            quadraticTo(center.x - s * 0.12f, center.y + s * 0.12f, center.x - s, center.y)
            quadraticTo(center.x - s * 0.12f, center.y - s * 0.12f, center.x, center.y - s)
            close()
        }
        drawPath(p, color)
    }
}

fun DrawScope.drawDrop(center: Offset, s: Float, color: Color) {
    translate(center.x, center.y) {
        val p = Path().apply {
            moveTo(0f, -s * 1.6f)
            cubicTo(s * 0.4f, -s * 0.6f, s, -s * 0.1f, s, s * 0.35f)
            cubicTo(s, s * 0.95f, -s, s * 0.95f, -s, s * 0.35f)
            cubicTo(-s, -s * 0.1f, -s * 0.4f, -s * 0.6f, 0f, -s * 1.6f)
            close()
        }
        drawPath(p, color)
    }
}

fun Color.darken(f: Float) = Color(red * (1 - f), green * (1 - f), blue * (1 - f), alpha)
fun Color.lighten(f: Float) = Color(red + (1 - red) * f, green + (1 - green) * f, blue + (1 - blue) * f, alpha)
