package com.kairos.app.ui.art

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kairos.app.engine.critters.Body
import com.kairos.app.engine.critters.Eyes
import com.kairos.app.engine.critters.Genome
import com.kairos.app.engine.critters.Mouth
import com.kairos.app.engine.critters.Pattern
import com.kairos.app.engine.critters.Rarity
import com.kairos.app.engine.critters.Topper
import com.kairos.app.ui.theme.Kairos
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Twelve pigment hues — the paint box every Puddling is mixed from. */
val CritterHues = listOf(
    Color(0xFF9CC4FF), Color(0xFFFFB3A1), Color(0xFF9FE3C4), Color(0xFFC9B3FF),
    Color(0xFFFFD98A), Color(0xFFFFA8C5), Color(0xFF8FD8E8), Color(0xFFB8E08A),
    Color(0xFFFFC48A), Color(0xFFB0B8FF), Color(0xFFE0B3E8), Color(0xFFA8D0B8),
)

private val Ink = Color(0xFF1B2340)

@Composable
fun CritterView(
    genome: Genome,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    alive: Boolean = true,
    name: String? = null,
) {
    val reduced = Kairos.reducedMotion || !alive
    val t = rememberInfiniteTransition(label = "critter")
    val bob by t.animateFloat(0f, 1f, infiniteRepeatable(tween((1600 * genome.squish).toInt(), easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bob")
    val blink by t.animateFloat(
        1f, 1f,
        infiniteRepeatable(keyframes { durationMillis = 3600 + (genome.seed % 900).toInt().let(::abs); 1f at 0; 1f at 3300; 0.1f at 3380; 1f at 3480 }),
        label = "blink",
    )
    val shimmer by t.animateFloat(0f, 1f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "shimmer")
    Canvas(
        modifier
            .size(size)
            .semantics { contentDescription = (name ?: "A Puddling") + ", ${genome.rarity.label.lowercase()}" },
    ) {
        drawCritter(genome, if (reduced) 0.5f else bob, if (reduced) 1f else blink, if (reduced) 0.2f else shimmer)
    }
}

fun DrawScope.drawCritter(g: Genome, bob: Float, blink: Float, shimmer: Float) {
    val w = size.width
    val r = w * 0.3f
    val cx = w / 2f
    val cy = size.height * 0.58f + (bob - 0.5f) * w * 0.04f
    val squash = 1f + (bob - 0.5f) * 0.06f * g.squish
    val base = CritterHues[g.hue % CritterHues.size]
    val accent = CritterHues[g.accentHue % CritterHues.size]

    drawOval(
        Brush.radialGradient(listOf(Ink.copy(alpha = 0.14f), Color.Transparent), center = Offset(cx, size.height * 0.92f), radius = r),
        topLeft = Offset(cx - r, size.height * 0.88f), size = Size(2 * r, r * 0.22f),
    )

    if (g.rarity == Rarity.Shiny) {
        drawCircle(
            Brush.radialGradient(listOf(Color(0xFFFFF1A8).copy(alpha = 0.6f), Color.Transparent), center = Offset(cx, cy), radius = r * 1.7f),
            radius = r * 1.7f, center = Offset(cx, cy),
        )
    }

    // toppers sit behind the body so ears tuck in naturally
    drawTopper(g.topper, cx, cy, r, squash, base, accent)

    val body = bodyPath(g.body, cx, cy, r, squash)
    val fill = if (g.rarity == Rarity.Shiny) {
        Brush.sweepGradient(listOf(base, accent, Color(0xFFFFF1A8), base), center = Offset(cx, cy))
    } else {
        Brush.radialGradient(listOf(Color.White.copy(alpha = 0.9f), base, base.darken(0.1f)), center = Offset(cx - r * 0.35f, cy - r * 0.5f), radius = r * 2.1f)
    }
    drawPath(body, fill)

    when (g.pattern) {
        Pattern.Belly -> drawOval(Color.White.copy(alpha = 0.55f), topLeft = Offset(cx - r * 0.45f, cy + r * 0.05f), size = Size(r * 0.9f, r * 0.7f))
        Pattern.Spots -> listOf(Offset(-0.55f, -0.35f), Offset(0.6f, -0.1f), Offset(-0.2f, 0.55f), Offset(0.35f, 0.5f)).forEach {
            drawCircle(accent.darken(0.08f).copy(alpha = 0.75f), r * 0.11f, Offset(cx + it.x * r, cy + it.y * r))
        }
        Pattern.Stripes -> for (s in listOf(-1f, 1f)) repeat(2) { i ->
            drawLine(accent.darken(0.15f), Offset(cx + s * r * 0.78f, cy + r * (0.05f + i * 0.16f)), Offset(cx + s * r * 0.55f, cy + r * (0.08f + i * 0.16f)), strokeWidth = r * 0.06f, cap = StrokeCap.Round)
        }
        Pattern.Blush, Pattern.Plain -> Unit
    }

    drawPath(body, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.85f), Color.Transparent), start = Offset(cx - r, cy - r), end = Offset(cx + r * 0.3f, cy + r)), style = Stroke(r * 0.05f))
    drawArc(Color.White.copy(alpha = 0.8f), 205f, 45f, false, topLeft = Offset(cx - r * 0.75f, cy - r * 0.75f), size = Size(r * 1.05f, r * 1.05f), style = Stroke(r * 0.08f, cap = StrokeCap.Round))

    // face
    val eyeY = cy - r * 0.02f
    for (s in listOf(-1f, 1f)) {
        val c = Offset(cx + s * r * 0.36f, eyeY)
        when (g.eyes) {
            Eyes.Happy -> {
                val p = Path().apply { moveTo(c.x - r * 0.12f, c.y + r * 0.04f); quadraticTo(c.x, c.y - r * 0.14f, c.x + r * 0.12f, c.y + r * 0.04f) }
                drawPath(p, Ink, style = Stroke(r * 0.07f, cap = StrokeCap.Round))
            }
            Eyes.Sleepy -> drawLine(Ink, Offset(c.x - r * 0.11f, c.y), Offset(c.x + r * 0.11f, c.y + r * 0.02f), strokeWidth = r * 0.065f, cap = StrokeCap.Round)
            Eyes.Dots -> {
                val h = r * 0.16f * blink
                drawOval(Ink, topLeft = Offset(c.x - r * 0.065f, c.y - h / 2), size = Size(r * 0.13f, h))
            }
            Eyes.Sparkle -> {
                val h = r * 0.24f * blink
                drawOval(Ink, topLeft = Offset(c.x - r * 0.1f, c.y - h / 2), size = Size(r * 0.2f, h))
                if (blink > 0.5f) {
                    drawCircle(Color.White, r * 0.05f, Offset(c.x + r * 0.03f, c.y - h * 0.2f))
                    drawCircle(Color.White.copy(alpha = 0.8f), r * 0.025f, Offset(c.x - r * 0.04f, c.y + h * 0.18f))
                }
            }
        }
        val blushAlpha = if (g.pattern == Pattern.Blush) 0.7f else 0.4f
        drawOval(Color(0xFFFF9E8F).copy(alpha = blushAlpha), topLeft = Offset(c.x + s * r * 0.1f - r * 0.12f, c.y + r * 0.16f), size = Size(r * 0.24f, r * 0.12f))
    }
    val my = cy + r * 0.24f
    when (g.mouth) {
        Mouth.Smile -> drawPath(Path().apply { moveTo(cx - r * 0.12f, my); quadraticTo(cx, my + r * 0.13f, cx + r * 0.12f, my) }, Ink, style = Stroke(r * 0.055f, cap = StrokeCap.Round))
        Mouth.Cat -> drawPath(
            Path().apply {
                moveTo(cx - r * 0.15f, my)
                quadraticTo(cx - r * 0.075f, my + r * 0.1f, cx, my)
                quadraticTo(cx + r * 0.075f, my + r * 0.1f, cx + r * 0.15f, my)
            },
            Ink, style = Stroke(r * 0.05f, cap = StrokeCap.Round),
        )
        Mouth.Oh -> drawOval(Ink, topLeft = Offset(cx - r * 0.05f, my - r * 0.02f), size = Size(r * 0.1f, r * 0.12f))
        Mouth.Grin -> {
            val p = Path().apply { moveTo(cx - r * 0.14f, my); quadraticTo(cx, my + r * 0.24f, cx + r * 0.14f, my); close() }
            drawPath(p, Ink)
        }
    }

    if (g.rarity >= Rarity.Rare) {
        val n = if (g.rarity == Rarity.Shiny) 3 else 1
        repeat(n) { i ->
            val a = shimmer * 2f * PI.toFloat() + i * 2.1f
            val tw = 0.5f + 0.5f * sin(shimmer * 4f * PI.toFloat() + i * 1.7f)
            drawSparkle(Offset(cx + cos(a) * r * 1.25f, cy - r * 0.4f + sin(a) * r * 0.8f), r * 0.14f * tw, Color(0xFFFFC857))
        }
    }
}

private fun bodyPath(shape: Body, cx: Float, cy: Float, r: Float, squash: Float): Path {
    val path = Path()
    val steps = 64
    for (i in 0..steps) {
        val a = i / steps.toFloat() * 2f * PI.toFloat()
        val c = cos(a)
        val s = sin(a)
        var rx = r
        var ry = r
        var dx = 0f
        var dy = 0f
        when (shape) {
            Body.Blob -> { rx *= 1.05f + 0.04f * sin(3 * a); ry *= 0.95f }
            Body.Drop -> if (s < 0) { val k = (1 - abs(c)).let { it * it }; dy = -k * r * 0.55f; rx *= 1f - k * 0.35f }
            Body.Mochi -> { rx *= 1.22f; ry *= 0.82f; if (s > 0) ry *= 0.9f }
            Body.Bean -> { rx *= 0.86f; ry *= 1.12f }
            Body.Star -> { val k = 1f + 0.12f * cos(5 * (a + PI.toFloat() / 2)); rx *= k; ry *= k }
            Body.Bell -> { if (s > 0) rx *= 1.15f else rx *= 0.9f; ry *= 1.02f }
        }
        val x = cx + c * rx * (2f - squash) + dx
        val y = cy + s * ry * squash * (if (s > 0) 0.92f else 1f) + dy
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

private fun DrawScope.drawTopper(t: Topper, cx: Float, cy: Float, r: Float, squash: Float, base: Color, accent: Color) {
    val top = cy - r * squash
    when (t) {
        Topper.None -> Unit
        Topper.CatEars -> for (s in listOf(-1f, 1f)) {
            val p = Path().apply {
                moveTo(cx + s * r * 0.75f, top + r * 0.45f)
                lineTo(cx + s * r * 0.72f, top - r * 0.3f)
                lineTo(cx + s * r * 0.18f, top + r * 0.15f)
                close()
            }
            drawPath(p, base.darken(0.05f))
            drawPath(Path().apply { moveTo(cx + s * r * 0.62f, top + r * 0.3f); lineTo(cx + s * r * 0.62f, top - r * 0.08f); lineTo(cx + s * r * 0.36f, top + r * 0.16f); close() }, Color(0xFFFFB0B8))
        }
        Topper.BunnyEars -> for (s in listOf(-1f, 1f)) {
            drawOval(base.darken(0.04f), topLeft = Offset(cx + s * r * 0.35f - r * 0.17f, top - r * 0.85f), size = Size(r * 0.34f, r * 1.1f))
            drawOval(Color(0xFFFFC2CC), topLeft = Offset(cx + s * r * 0.35f - r * 0.08f, top - r * 0.68f), size = Size(r * 0.16f, r * 0.75f))
        }
        Topper.Sprout -> {
            drawLine(Color(0xFF5DAE6B), Offset(cx, top + r * 0.1f), Offset(cx, top - r * 0.35f), strokeWidth = r * 0.07f, cap = StrokeCap.Round)
            drawOval(Color(0xFF7FCB86), topLeft = Offset(cx - r * 0.42f, top - r * 0.55f), size = Size(r * 0.42f, r * 0.24f))
            drawOval(Color(0xFF9EDD9F), topLeft = Offset(cx, top - r * 0.62f), size = Size(r * 0.44f, r * 0.26f))
        }
        Topper.Antenna -> {
            drawLine(Ink.copy(alpha = 0.7f), Offset(cx + r * 0.1f, top + r * 0.1f), Offset(cx + r * 0.3f, top - r * 0.45f), strokeWidth = r * 0.05f, cap = StrokeCap.Round)
            drawCircle(accent, r * 0.13f, Offset(cx + r * 0.3f, top - r * 0.5f))
            drawCircle(Color.White.copy(alpha = 0.8f), r * 0.04f, Offset(cx + r * 0.26f, top - r * 0.55f))
        }
        Topper.Horn -> {
            val p = Path().apply { moveTo(cx - r * 0.16f, top + r * 0.15f); quadraticTo(cx, top - r * 0.65f, cx + r * 0.16f, top + r * 0.15f); close() }
            drawPath(p, Brush.verticalGradient(listOf(Color(0xFFFFF4D6), accent), startY = top - r * 0.5f, endY = top + r * 0.15f))
        }
        Topper.Crown -> {
            val p = Path().apply {
                moveTo(cx - r * 0.38f, top + r * 0.12f); lineTo(cx - r * 0.42f, top - r * 0.32f); lineTo(cx - r * 0.18f, top - r * 0.1f)
                lineTo(cx, top - r * 0.42f); lineTo(cx + r * 0.18f, top - r * 0.1f); lineTo(cx + r * 0.42f, top - r * 0.32f); lineTo(cx + r * 0.38f, top + r * 0.12f); close()
            }
            drawPath(p, Brush.verticalGradient(listOf(Color(0xFFFFE27A), Color(0xFFF2B33D)), startY = top - r * 0.4f, endY = top + r * 0.1f))
        }
        Topper.Petals -> repeat(5) { i ->
            val a = -PI.toFloat() / 2 + (i - 2) * 0.42f
            val pc = Offset(cx + cos(a) * r * 0.62f, top + r * 0.3f + sin(a) * r * 0.62f)
            drawCircle(accent.lighten(0.2f), r * 0.2f, pc)
        }
    }
}
