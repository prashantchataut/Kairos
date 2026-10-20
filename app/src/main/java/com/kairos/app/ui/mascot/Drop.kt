package com.kairos.app.ui.mascot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.kairos.app.engine.Mood
import com.kairos.app.ui.components.squishClickable
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Pigment
import com.kairos.app.ui.theme.Wash
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Drop's faces. Each mood maps to one, and taps briefly switch to [Giggle]. */
enum class Expression { Happy, Giggle, Calm, Okay, Sad, Worried, Wow, Wink, Sleepy }

fun Mood.expression() = when (this) {
    Mood.Radiant -> Expression.Giggle
    Mood.Good -> Expression.Happy
    Mood.Okay -> Expression.Okay
    Mood.Low -> Expression.Sad
    Mood.Rough -> Expression.Worried
}

/**
 * Drop — Kairos' mascot. A watercolour water-drop with a glassy highlight, drawn entirely in
 * code on a 100×110 grid so it stays crisp at any size. It bobs, blinks on its own, squishes
 * when poked and giggles back.
 */
@Composable
fun DropMascot(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    wash: Wash = Wash.Cerulean,
    expression: Expression = Expression.Happy,
    animate: Boolean = true,
    lookX: Float = 0f,
    interactive: Boolean = true,
    onPoke: (() -> Unit)? = null,
) {
    val reduced = Kairos.reducedMotion
    val live = animate && !reduced
    val dark = Kairos.colors.isDark
    val body by animateColorAsState(wash.light, tween(600), label = "dropBody")

    val bob = if (live) {
        rememberInfiniteTransition(label = "bob").animateFloat(
            0f, 2f * PI.toFloat(), infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Restart), label = "bobT",
        ).value
    } else 0f

    var blink by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(live) {
        if (!live) return@LaunchedEffect
        val rnd = Random(System.nanoTime())
        while (true) {
            delay(2200L + rnd.nextLong(2800))
            val anim = Animatable(1f)
            anim.animateTo(0.08f, tween(70)) { blink = value }
            anim.animateTo(1f, tween(110)) { blink = value }
            if (rnd.nextFloat() < 0.18f) { // the occasional double blink
                delay(120)
                anim.animateTo(0.08f, tween(70)) { blink = value }
                anim.animateTo(1f, tween(110)) { blink = value }
            }
        }
    }

    val squish = remember { Animatable(0f) }
    var giggling by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val face = if (giggling) Expression.Giggle else expression

    val tapModifier = if (interactive) {
        Modifier.squishClickable(pressedScale = 0.9f, label = "Poke Drop") {
            onPoke?.invoke()
            scope.launch {
                giggling = true
                squish.snapTo(1f)
                squish.animateTo(0f, androidx.compose.animation.core.spring(dampingRatio = 0.3f, stiffness = 300f))
                delay(500)
                giggling = false
            }
        }
    } else Modifier

    Canvas(
        modifier
            .size(size, size * 1.1f)
            .semantics { contentDescription = "Drop, the Kairos mascot, looking ${face.name.lowercase()}" }
            .then(tapModifier)
    ) {
        val u = this.size.width / 100f
        val lift = sin(bob) * 3.2f * u
        // Squash and stretch: taller at the top of the bob, wider when landing, wobbly after a poke.
        val stretch = sin(bob) * 0.025f + squish.value * 0.12f * sin(squish.value * 14f)
        scale(1f - stretch, 1f + stretch, pivot = Offset(50f * u, 108f * u)) {
            translate(top = -lift) {
                drawDrop(u, body, face, blink, lookX, dark)
            }
        }
        // Ground shadow breathes with the bob.
        val shadowW = (34f - sin(bob) * 4f) * u
        drawOval(
            color = Color(0xFF1E2B4A).copy(alpha = if (dark) 0.25f else 0.10f),
            topLeft = Offset(50f * u - shadowW / 2, 106f * u),
            size = Size(shadowW, 4.5f * u),
        )
    }
}

/** Drop's outline: a round belly with a soft tip leaning right. */
fun dropPath(u: Float) = Path().apply {
    moveTo(60f * u, 6f * u)
    cubicTo(72f * u, 22f * u, 95f * u, 42f * u, 95f * u, 66f * u)
    cubicTo(95f * u, 89f * u, 76f * u, 104f * u, 50f * u, 104f * u)
    cubicTo(24f * u, 104f * u, 5f * u, 89f * u, 5f * u, 66f * u)
    cubicTo(5f * u, 40f * u, 34f * u, 26f * u, 60f * u, 6f * u)
    close()
}

private val Ink = Color(0xFF1E2B4A)

private fun DrawScope.drawDrop(u: Float, body: Color, face: Expression, blink: Float, lookX: Float, dark: Boolean) {
    val path = dropPath(u)
    // Body: wet-in-wet gradient, lighter where light lands, deeper at the belly edge.
    drawPath(
        path,
        Brush.radialGradient(
            0f to lerp(body, Color.White, 0.55f),
            0.55f to body,
            1f to lerp(body, Ink, 0.22f),
            center = Offset(40f * u, 52f * u), radius = 70f * u,
        ),
    )
    // Pigment blooms inside the body.
    drawCircle(lerp(body, Color.White, 0.35f).copy(alpha = 0.45f), 13f * u, Offset(68f * u, 86f * u))
    drawCircle(lerp(body, Ink, 0.1f).copy(alpha = 0.18f), 18f * u, Offset(30f * u, 88f * u))
    // Rim where the pigment dried.
    drawPath(path, lerp(body, Ink, 0.35f).copy(alpha = 0.55f), style = Stroke(width = 2.2f * u))
    // Glass highlight.
    rotate(-28f, pivot = Offset(30f * u, 48f * u)) {
        drawOval(Color.White.copy(alpha = 0.8f), topLeft = Offset(24f * u, 36f * u), size = Size(10f * u, 20f * u))
    }
    drawCircle(Color.White.copy(alpha = 0.9f), 2.6f * u, Offset(33f * u, 64f * u))

    // Cheeks.
    val blush = Pigment.Rose.copy(alpha = if (dark) 0.75f else 0.6f)
    drawOval(blush, topLeft = Offset(19f * u, 73f * u), size = Size(12f * u, 6.5f * u))
    drawOval(blush, topLeft = Offset(69f * u, 73f * u), size = Size(12f * u, 6.5f * u))

    val eyeL = Offset((38f + lookX * 3f) * u, 64f * u)
    val eyeR = Offset((62f + lookX * 3f) * u, 64f * u)
    val line = Stroke(width = 3.2f * u, cap = StrokeCap.Round)

    fun openEye(c: Offset, scaleY: Float = blink, big: Float = 1f) {
        val w = 7.4f * u * big
        val h = 10f * u * big * scaleY.coerceAtLeast(0.08f)
        drawOval(Ink, topLeft = Offset(c.x - w / 2, c.y - h / 2), size = Size(w, h))
        if (scaleY > 0.5f) drawCircle(Color.White, 1.7f * u * big, Offset(c.x + 1.2f * u, c.y - 2.2f * u * big))
    }
    fun happyEye(c: Offset) {
        drawPath(Path().apply {
            moveTo(c.x - 5f * u, c.y + 1.5f * u)
            quadraticTo(c.x, c.y - 6f * u, c.x + 5f * u, c.y + 1.5f * u)
        }, Ink, style = line)
    }
    fun closedEye(c: Offset) {
        drawPath(Path().apply {
            moveTo(c.x - 5f * u, c.y - 1f * u)
            quadraticTo(c.x, c.y + 4f * u, c.x + 5f * u, c.y - 1f * u)
        }, Ink, style = line)
    }
    fun mouth(build: Path.() -> Unit, fill: Boolean = false) {
        val p = Path().apply(build)
        if (fill) drawPath(p, Ink) else drawPath(p, Ink, style = line)
    }

    val m = Offset(50f * u, 78f * u)
    when (face) {
        Expression.Happy -> {
            openEye(eyeL); openEye(eyeR)
            mouth({ moveTo(m.x - 6f * u, m.y - 1f * u); quadraticTo(m.x, m.y + 6f * u, m.x + 6f * u, m.y - 1f * u) })
        }
        Expression.Giggle -> {
            happyEye(eyeL); happyEye(eyeR)
            mouth({
                moveTo(m.x - 7f * u, m.y - 2f * u)
                quadraticTo(m.x, m.y - 1f * u, m.x + 7f * u, m.y - 2f * u)
                quadraticTo(m.x + 6f * u, m.y + 8f * u, m.x, m.y + 8f * u)
                quadraticTo(m.x - 6f * u, m.y + 8f * u, m.x - 7f * u, m.y - 2f * u)
                close()
            }, fill = true)
            drawCircle(Pigment.Rose, 2.6f * u, Offset(m.x + 1f * u, m.y + 4.6f * u))
        }
        Expression.Calm, Expression.Sleepy -> {
            closedEye(eyeL); closedEye(eyeR)
            if (face == Expression.Sleepy) {
                drawCircle(Ink, 2.2f * u, m.copy(y = m.y + 1f * u), style = Stroke(1.8f * u))
            } else {
                mouth({ moveTo(m.x - 4f * u, m.y); quadraticTo(m.x, m.y + 3.5f * u, m.x + 4f * u, m.y) })
            }
        }
        Expression.Okay -> {
            openEye(eyeL); openEye(eyeR)
            mouth({ moveTo(m.x - 4.5f * u, m.y + 1f * u); quadraticTo(m.x, m.y + 2.6f * u, m.x + 4.5f * u, m.y + 0.5f * u) })
        }
        Expression.Sad -> {
            openEye(eyeL, scaleY = blink * 0.8f); openEye(eyeR, scaleY = blink * 0.8f)
            drawLine(Ink, Offset(eyeL.x - 5f * u, eyeL.y - 9f * u), Offset(eyeL.x + 3f * u, eyeL.y - 11f * u), 2.4f * u, StrokeCap.Round)
            drawLine(Ink, Offset(eyeR.x - 3f * u, eyeR.y - 11f * u), Offset(eyeR.x + 5f * u, eyeR.y - 9f * u), 2.4f * u, StrokeCap.Round)
            mouth({ moveTo(m.x - 5f * u, m.y + 3f * u); quadraticTo(m.x, m.y - 2f * u, m.x + 5f * u, m.y + 3f * u) })
        }
        Expression.Worried -> {
            openEye(eyeL); openEye(eyeR)
            mouth({
                moveTo(m.x - 6f * u, m.y + 2f * u)
                quadraticTo(m.x - 3f * u, m.y - 1f * u, m.x, m.y + 2f * u)
                quadraticTo(m.x + 3f * u, m.y + 5f * u, m.x + 6f * u, m.y + 2f * u)
            })
            // A tiny sweat drop.
            drawPath(Path().apply {
                moveTo(84f * u, 44f * u)
                quadraticTo(88f * u, 52f * u, 84f * u, 54f * u)
                quadraticTo(80f * u, 52f * u, 84f * u, 44f * u)
            }, Pigment.Cerulean.copy(alpha = 0.9f))
        }
        Expression.Wow -> {
            openEye(eyeL, big = 1.2f); openEye(eyeR, big = 1.2f)
            drawOval(Ink, topLeft = Offset(m.x - 3.5f * u, m.y - 1f * u), size = Size(7f * u, 8f * u))
        }
        Expression.Wink -> {
            happyEye(eyeL); openEye(eyeR)
            mouth({ moveTo(m.x - 6f * u, m.y - 1f * u); quadraticTo(m.x, m.y + 6f * u, m.x + 6f * u, m.y - 1f * u) })
        }
    }
}
