package com.kairos.app.ui.kit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Washes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/** Pip's moods double as the mood check-in scale on Today. */
enum class PipMood(val label: String, val line: String) {
    Glowing("Glowing", "Look at you shine."),
    Happy("Happy", "Good to see you smiling."),
    Calm("Calm", "Nice and steady."),
    Meh("Meh", "Meh is allowed. Let's find one small good thing."),
    Low("Low", "Thanks for telling me. Go gently today."),
    Sleepy("Sleepy", "Soft day, slow pace. That's fine."),
    Curious("Curious", "Ooh, let's learn something."),
    Proud("Proud", "Pip is proud of you."),
}

fun PipMood.wash(w: Washes): Color = when (this) {
    PipMood.Glowing -> w.butter
    PipMood.Happy -> w.sky
    PipMood.Calm -> w.mint
    PipMood.Meh -> lerp(w.sky, w.lilac, 0.5f)
    PipMood.Low -> w.lilac
    PipMood.Sleepy -> lerp(w.lilac, w.sky, 0.3f)
    PipMood.Curious -> w.rose
    PipMood.Proud -> w.peach
}

private val EyeInk = Color(0xFF1D2640)

/**
 * Pip — a little watercolor droplet. Breathes, blinks on its own schedule, wobbles like
 * liquid, and squashes when poked. All idle motion stops under reduced motion.
 */
@Composable
fun Pip(
    mood: PipMood,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    tint: Color? = null,
    poke: Int = 0,
    lookX: Float = 0f,
) {
    val k = Kairos.colors
    val reduce = Kairos.reduceMotion
    val target = tint ?: mood.wash(Washes(
        sky = Color(0xFF8FC1F7), mint = Color(0xFF8EDDC0), lilac = Color(0xFFBBA8F6),
        peach = Color(0xFFFFB59A), butter = Color(0xFFFFD45E), rose = Color(0xFFFF97AB),
    ))
    val color by animateColorAsState(target, tween(600), label = "pipColor")

    val blink = remember { Animatable(1f) }
    val squash = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val idle = if (reduce) remember { mutableFloatStateOf(0f) } else {
        rememberInfiniteTransition(label = "pip").animateFloat(
            0f, (2 * PI).toFloat(),
            infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart),
            label = "breath",
        )
    }

    LaunchedEffect(reduce, mood) {
        if (reduce || mood == PipMood.Sleepy) return@LaunchedEffect
        val rnd = Random(System.nanoTime())
        while (true) {
            delay(2200L + rnd.nextLong(2800))
            blink.animateTo(0.08f, tween(70))
            blink.animateTo(1f, tween(110))
            if (rnd.nextInt(4) == 0) {
                delay(140)
                blink.animateTo(0.08f, tween(60))
                blink.animateTo(1f, tween(100))
            }
        }
    }
    LaunchedEffect(poke) {
        if (poke == 0 || reduce) return@LaunchedEffect
        scope.launch {
            squash.snapTo(0f)
            squash.animateTo(0.22f, tween(90))
            squash.animateTo(0f, androidx.compose.animation.core.spring(dampingRatio = 0.32f, stiffness = 380f))
        }
    }

    Canvas(
        modifier
            .size(size)
            .semantics { contentDescription = "Pip, feeling ${mood.label.lowercase()}" },
    ) {
        val t = idle.value
        val breathe = if (reduce) 0f else sin(t) * 0.025f
        val sq = squash.value
        val sx = 1f + breathe * 0.6f + sq * 0.9f
        val sy = 1f - breathe * 0.4f - sq
        drawPip(mood, color, k.isDark, t, sx, sy, blink.value, lookX)
    }
}

private fun pipBody(c: Offset, r: Float, t: Float): Path {
    val path = Path()
    val n = 72
    for (i in 0..n) {
        val a = (i % n) / n.toFloat() * 2f * PI.toFloat() - PI.toFloat() / 2f // start at top
        // The droplet tip: a soft peak at the top that leans slightly, like a drop just landed.
        val fromTop = ((a + PI.toFloat() / 2f + 0.18f) + PI.toFloat()).mod(2f * PI.toFloat()) - PI.toFloat()
        val tip = 0.32f * exp(-(fromTop * fromTop) / 0.06f)
        val wobble = 0.025f * sin(3f * a + t) + 0.018f * sin(5f * a - t * 1.3f)
        val flat = if (sin(a) > 0) 0.08f * sin(a) * sin(a) else 0f // a sat-down bottom
        val rr = r * (1f + tip + wobble - flat)
        val x = c.x + cos(a) * rr * 1.06f
        val y = c.y + sin(a) * rr
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun DrawScope.drawPip(
    mood: PipMood, color: Color, dark: Boolean, t: Float,
    sx: Float, sy: Float, blink: Float, lookX: Float,
) {
    val s = size.minDimension
    val r = s * 0.34f
    val c = Offset(size.width / 2f, size.height * 0.56f)
    val ground = c.y + r * 0.98f

    // contact shadow, shrinks as Pip stretches upward
    drawOval(
        color = Color.Black.copy(alpha = if (dark) 0.35f else 0.10f),
        topLeft = Offset(c.x - r * 0.9f * sx, ground - r * 0.08f),
        size = Size(r * 1.8f * sx, r * 0.2f),
    )

    scale(sx, sy, pivot = Offset(c.x, ground)) {
        val body = pipBody(c, r, t)
        drawPath(
            body,
            Brush.verticalGradient(
                0f to lerp(color, Color.White, 0.45f),
                0.55f to color,
                1f to lerp(color, Color(0xFF1D2640), 0.18f),
                startY = c.y - r * 1.3f, endY = c.y + r,
            ),
        )
        // pigment pooling at the rim
        drawPath(body, lerp(color, Color(0xFF1D2640), 0.25f).copy(alpha = 0.45f), style = Stroke(s * 0.018f))
        // glossy highlights
        rotate(-28f, Offset(c.x - r * 0.45f, c.y - r * 0.45f)) {
            drawOval(
                Color.White.copy(alpha = 0.55f),
                topLeft = Offset(c.x - r * 0.62f, c.y - r * 0.58f),
                size = Size(r * 0.34f, r * 0.2f),
            )
        }
        drawCircle(Color.White.copy(alpha = 0.7f), r * 0.05f, Offset(c.x - r * 0.18f, c.y - r * 0.68f))

        translate(left = lookX * r * 0.08f) { drawFace(mood, c, r, blink) }
    }

    if (mood == PipMood.Sleepy) {
        val z = Color(0xFF8C97B2)
        drawZ(Offset(c.x + r * 1.05f, c.y - r * 1.05f), r * 0.16f, z)
        drawZ(Offset(c.x + r * 1.32f, c.y - r * 1.38f), r * 0.11f, z.copy(alpha = 0.7f))
    }
    if (mood == PipMood.Glowing || mood == PipMood.Proud) {
        val spark = Color(0xFFFFC93C)
        drawSparkle(Offset(c.x - r * 1.15f, c.y - r * 0.9f), r * 0.16f * (0.85f + 0.15f * sin(t * 2)), spark)
        drawSparkle(Offset(c.x + r * 1.2f, c.y - r * 0.4f), r * 0.11f * (0.85f + 0.15f * cos(t * 2)), spark)
    }
}

private fun DrawScope.drawFace(mood: PipMood, c: Offset, r: Float, blink: Float) {
    val eyeY = c.y - r * 0.02f
    val eyeDx = r * 0.36f
    val stroke = Stroke(width = r * 0.085f, cap = StrokeCap.Round)
    val left = Offset(c.x - eyeDx, eyeY)
    val right = Offset(c.x + eyeDx, eyeY)

    fun ovalEye(o: Offset, w: Float = r * 0.15f, h: Float = r * 0.21f) {
        val hh = h * blink
        drawOval(EyeInk, Offset(o.x - w / 2, o.y - hh / 2), Size(w, hh))
        if (blink > 0.5f) drawCircle(Color.White, w * 0.22f, Offset(o.x + w * 0.16f, o.y - hh * 0.2f))
    }
    fun happyEye(o: Offset) {
        val p = Path().apply {
            moveTo(o.x - r * 0.12f, o.y + r * 0.04f)
            quadraticTo(o.x, o.y - r * 0.14f, o.x + r * 0.12f, o.y + r * 0.04f)
        }
        drawPath(p, EyeInk, style = stroke)
    }
    fun lineEye(o: Offset) = drawLine(EyeInk, Offset(o.x - r * 0.11f, o.y), Offset(o.x + r * 0.11f, o.y), stroke.width, StrokeCap.Round)

    when (mood) {
        PipMood.Glowing, PipMood.Proud, PipMood.Happy -> { happyEye(left); happyEye(right) }
        PipMood.Sleepy -> { lineEye(left); lineEye(right) }
        PipMood.Curious -> { ovalEye(left, r * 0.17f, r * 0.24f); ovalEye(right, r * 0.13f, r * 0.17f) }
        else -> { ovalEye(left); ovalEye(right) }
    }

    // cheeks
    val blush = Color(0xFFFF8FA3).copy(alpha = if (mood == PipMood.Low) 0.25f else 0.5f)
    drawOval(blush, Offset(left.x - r * 0.2f, eyeY + r * 0.14f), Size(r * 0.24f, r * 0.12f))
    drawOval(blush, Offset(right.x - r * 0.04f, eyeY + r * 0.14f), Size(r * 0.24f, r * 0.12f))

    val my = eyeY + r * 0.26f
    when (mood) {
        PipMood.Glowing -> {
            val p = Path().apply {
                moveTo(c.x - r * 0.17f, my - r * 0.02f)
                quadraticTo(c.x, my + r * 0.3f, c.x + r * 0.17f, my - r * 0.02f)
                close()
            }
            drawPath(p, EyeInk)
            drawOval(Color(0xFFFF8FA3), Offset(c.x - r * 0.07f, my + r * 0.07f), Size(r * 0.14f, r * 0.08f))
        }
        PipMood.Happy, PipMood.Proud -> {
            val p = Path().apply {
                moveTo(c.x - r * 0.14f, my)
                quadraticTo(c.x, my + r * 0.18f, c.x + r * 0.14f, my)
            }
            drawPath(p, EyeInk, style = stroke)
        }
        PipMood.Calm -> {
            val p = Path().apply {
                moveTo(c.x - r * 0.09f, my)
                quadraticTo(c.x, my + r * 0.08f, c.x + r * 0.09f, my)
            }
            drawPath(p, EyeInk, style = stroke)
        }
        PipMood.Meh -> drawLine(EyeInk, Offset(c.x - r * 0.09f, my + r * 0.03f), Offset(c.x + r * 0.09f, my + r * 0.03f), stroke.width, StrokeCap.Round)
        PipMood.Low -> {
            val p = Path().apply {
                moveTo(c.x - r * 0.11f, my + r * 0.08f)
                quadraticTo(c.x, my - r * 0.04f, c.x + r * 0.11f, my + r * 0.08f)
            }
            drawPath(p, EyeInk, style = stroke)
        }
        PipMood.Sleepy, PipMood.Curious -> drawOval(EyeInk, Offset(c.x - r * 0.05f, my - r * 0.01f), Size(r * 0.1f, r * 0.12f))
    }
}

private fun DrawScope.drawZ(o: Offset, s: Float, c: Color) {
    val p = Path().apply {
        moveTo(o.x - s / 2, o.y - s / 2); lineTo(o.x + s / 2, o.y - s / 2)
        lineTo(o.x - s / 2, o.y + s / 2); lineTo(o.x + s / 2, o.y + s / 2)
    }
    drawPath(p, c, style = Stroke(s * 0.22f, cap = StrokeCap.Round))
}

/** Four-point sparkle, the app's little "joy" glyph. */
fun DrawScope.drawSparkle(o: Offset, s: Float, c: Color) {
    val p = Path().apply {
        moveTo(o.x, o.y - s)
        quadraticTo(o.x, o.y, o.x + s, o.y)
        quadraticTo(o.x, o.y, o.x, o.y + s)
        quadraticTo(o.x, o.y, o.x - s, o.y)
        quadraticTo(o.x, o.y, o.x, o.y - s)
        close()
    }
    drawPath(p, c)
}
