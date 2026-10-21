package com.kairos.app.ui.art

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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kairos.app.engine.Mood
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * How Kai looks: a body outline described as a polar function plus a face. Every look is the same
 * function with different numbers, so any look can morph into any other.
 *
 * radius(θ) = 1 + depth·cos(lobes·θ + phase) + wobble·(irregular bumps) − flat·(bottom flattening)
 */
@Immutable
data class KaiLook(
    val lobes: Int,
    val depth: Float,
    val wobble: Float = 0f,
    val flat: Float = 0f,
    val phase: Float = 0f,
    val body: Color,
    /** 1 = open oval eyes, 0 = closed happy arcs. */
    val eyes: Float = 1f,
    /** 0 = wide awake, 1 = heavy lids. */
    val lid: Float = 0f,
    /** Where Kai looks, −1..1 on each axis. */
    val gaze: Offset = Offset.Zero,
    /** −1 frown … 0 flat … 1 grin. */
    val smile: Float = 0.4f,
    /** 0 hides the mouth. */
    val mouth: Float = 1f,
    val blush: Float = 0f,
    /** 1 = a small round "o" mouth (surprise). */
    val wow: Float = 0f,
)

object KaiLooks {
    val Curator = KaiLook(lobes = 8, depth = 0.055f, body = Candy.Cobalt, smile = 0.45f, blush = 0.35f)
    val Radiant = KaiLook(lobes = 8, depth = 0.085f, phase = 0.2f, body = Candy.Sun, eyes = 0f, smile = 1f, blush = 0.7f)
    val Good = KaiLook(lobes = 6, depth = 0.1f, body = Candy.Mint, smile = 0.7f, blush = 0.5f)
    val Okay = KaiLook(lobes = 3, depth = 0.05f, wobble = 0.04f, phase = 0.5f, body = Candy.Sky, smile = 0.08f, blush = 0.2f)
    val Low = KaiLook(lobes = 5, depth = 0.07f, flat = 1f, phase = 0.9f, body = Candy.Lilac, lid = 0.75f, gaze = Offset(0f, 0.6f), smile = -0.2f, blush = 0.15f)
    val Rough = KaiLook(lobes = 5, depth = 0.06f, wobble = 0.11f, phase = 1.7f, body = Candy.Bubblegum, gaze = Offset(-0.2f, 0.9f), smile = -0.6f, lid = 0.3f)
    val Thinking = KaiLook(lobes = 8, depth = 0.055f, body = Candy.Cobalt, gaze = Offset(0.7f, -0.8f), smile = 0.1f, blush = 0.2f)
    val Delighted = KaiLook(lobes = 8, depth = 0.075f, body = Candy.Cobalt, eyes = 0f, smile = 1f, blush = 0.8f)
    val Wow = KaiLook(lobes = 8, depth = 0.07f, body = Candy.Cobalt, smile = 0f, wow = 1f, blush = 0.4f)
    val Sad = KaiLook(lobes = 8, depth = 0.045f, flat = 0.5f, body = Candy.Cobalt, lid = 0.4f, gaze = Offset(0f, 0.9f), smile = -0.5f)
    val Sleepy = KaiLook(lobes = 8, depth = 0.05f, flat = 0.3f, body = Candy.Cobalt, lid = 0.95f, smile = 0f, mouth = 0.6f, blush = 0.3f)
}

fun Mood.look(): KaiLook = when (this) {
    Mood.Radiant -> KaiLooks.Radiant
    Mood.Good -> KaiLooks.Good
    Mood.Okay -> KaiLooks.Okay
    Mood.Low -> KaiLooks.Low
    Mood.Rough -> KaiLooks.Rough
}

fun KaiLook.tinted(c: Color) = copy(body = c)

private fun lerpF(a: Float, b: Float, t: Float) = a + (b - a) * t

private fun radius(l: KaiLook, th: Float): Float {
    var r = 1f + l.depth * cos(l.lobes * th + l.phase + PI.toFloat() / 2f * l.lobes) +
        l.wobble * (0.6f * sin(3f * th + 1.3f) + 0.4f * cos(5f * th + 0.4f))
    val down = sin(th)
    if (down > 0f) r -= l.flat * down.pow(6) * 0.2f
    return r
}

/** The face and body of Kai between two looks. Pure drawing; animation lives in [Kai]. */
fun DrawScope.drawKai(
    a: KaiLook, b: KaiLook, t: Float,
    center: Offset = Offset(size.width / 2, size.height / 2),
    r: Float = size.minDimension * 0.4f,
    blink: Float = 1f,
    breathe: Float = 0f,
    shadow: Boolean = true,
    ink: Color = Candy.Navy,
) {
    val body = lerp(a.body, b.body, t)
    val sx = 1f + breathe * 0.025f
    val sy = 1f - breathe * 0.02f
    if (shadow) {
        drawOval(
            Brush.radialGradient(listOf(Candy.Navy.copy(alpha = 0.16f), Color.Transparent), Offset(center.x, center.y + r * 1.16f), r * 0.75f),
            Offset(center.x - r * 0.75f, center.y + r * 1.06f), Size(r * 1.5f, r * 0.2f),
        )
    }
    val n = 120
    val pts = Array(n) { i ->
        val th = (i.toFloat() / n) * 2f * PI.toFloat()
        val rr = r * lerpF(radius(a, th), radius(b, th), t)
        Offset(center.x + rr * cos(th) * sx, center.y + rr * sin(th) * sy)
    }
    val path = Path().apply {
        fun mid(p: Offset, q: Offset) = Offset((p.x + q.x) / 2, (p.y + q.y) / 2)
        val start = mid(pts[0], pts[1])
        moveTo(start.x, start.y)
        for (i in 1..n) {
            val p = pts[i % n]
            val m = mid(p, pts[(i + 1) % n])
            quadraticTo(p.x, p.y, m.x, m.y)
        }
        close()
    }
    val hi = lerp(body, Color.White, 0.45f)
    val lo = lerp(body, Candy.Navy, 0.18f)
    drawPath(path, Brush.radialGradient(0f to hi, 0.5f to body, 1f to lo, center = Offset(center.x - r * 0.35f, center.y - r * 0.45f), radius = r * 1.75f))
    // Soft top-left sheen, like the glossy characters in r04 and r09.
    drawOval(
        Brush.radialGradient(listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0f)), Offset(center.x - r * 0.38f, center.y - r * 0.56f), r * 0.32f),
        Offset(center.x - r * 0.7f, center.y - r * 0.74f), Size(r * 0.64f, r * 0.36f),
    )

    val gaze = Offset(lerpF(a.gaze.x, b.gaze.x, t), lerpF(a.gaze.y, b.gaze.y, t))
    val eyes = lerpF(a.eyes, b.eyes, t)
    val lid = lerpF(a.lid, b.lid, t)
    val smile = lerpF(a.smile, b.smile, t)
    val mouth = lerpF(a.mouth, b.mouth, t)
    val blush = lerpF(a.blush, b.blush, t)
    val wow = lerpF(a.wow, b.wow, t)
    val faceY = center.y - r * 0.04f + gaze.y * r * 0.07f
    val faceX = center.x + gaze.x * r * 0.1f

    if (blush > 0.01f) for (s in listOf(-1f, 1f)) {
        drawOval(Candy.Bubblegum.copy(alpha = 0.42f * blush), Offset(faceX + s * r * 0.52f - r * 0.12f, faceY + r * 0.16f), Size(r * 0.24f, r * 0.13f))
    }

    val eyeW = r * 0.19f
    val eyeH = r * 0.33f
    for (s in listOf(-1f, 1f)) {
        val ex = faceX + s * r * 0.27f
        val ey = faceY - r * 0.08f
        if (eyes > 0.02f) {
            val h = eyeH * blink.coerceIn(0.08f, 1f) * (1f - lid * 0.55f)
            val top = ey - eyeH / 2 + (eyeH - h) * (0.35f + lid * 0.4f)
            drawOval(ink.copy(alpha = eyes), Offset(ex - eyeW / 2, top), Size(eyeW, h))
            if (blink > 0.5f) drawCircle(Color.White.copy(alpha = 0.95f * eyes), r * 0.042f, Offset(ex + eyeW * 0.16f + gaze.x * r * 0.02f, top + h * 0.26f))
        }
        if (eyes < 0.98f) {
            val w = eyeW * 0.85f
            val arc = Path().apply {
                moveTo(ex - w, ey + eyeH * 0.12f)
                quadraticTo(ex, ey - eyeH * 0.42f, ex + w, ey + eyeH * 0.12f)
            }
            drawPath(arc, ink.copy(alpha = 1f - eyes), style = Stroke(r * 0.075f, cap = StrokeCap.Round))
        }
    }

    val my = faceY + r * 0.27f
    if (wow > 0.02f) {
        drawOval(ink.copy(alpha = wow), Offset(faceX - r * 0.07f, my - r * 0.06f), Size(r * 0.14f, r * 0.16f))
    }
    if (mouth > 0.02f && wow < 0.98f) {
        val w = r * (0.14f + 0.08f * max(0f, smile))
        val alpha = mouth * (1f - wow)
        val p = Path().apply {
            moveTo(faceX - w, my)
            quadraticTo(faceX, my + smile * r * 0.2f, faceX + w, my)
        }
        if (smile > 0.75f) {
            p.close()
            drawPath(p, ink.copy(alpha = alpha))
            // A small coral tongue makes the grin read as joy rather than a wedge.
            drawOval(Candy.Coral.copy(alpha = alpha), Offset(faceX - w * 0.42f, my + smile * r * 0.06f), Size(w * 0.84f, r * 0.07f))
        } else {
            drawPath(p, ink.copy(alpha = alpha), style = Stroke(r * 0.062f, cap = StrokeCap.Round))
        }
    }
}

/**
 * Kai, the curator. Morphs to [look] with the bloom spring (a little hop on the way), blinks every few
 * seconds and breathes. Calm motion keeps expressions but drops the movement.
 */
@Composable
fun Kai(
    look: KaiLook,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    shadow: Boolean = true,
    alive: Boolean = true,
    description: String? = "Kai, your curator",
) {
    val calm = K.calm
    var from by remember { mutableStateOf(look) }
    var to by remember { mutableStateOf(look) }
    val t = remember { Animatable(1f) }
    val hop = remember { Animatable(0f) }
    val blink = remember { Animatable(1f) }
    LaunchedEffect(look) {
        if (look == to) return@LaunchedEffect
        from = interpolate(from, to, t.value)
        to = look
        t.snapTo(0f)
        if (calm) { t.snapTo(1f); return@LaunchedEffect }
        launch { hop.snapTo(0f); hop.animateTo(1f, tween(420, easing = LinearEasing)); hop.snapTo(0f) }
        t.animateTo(1f, Motion.bloom())
    }
    LaunchedEffect(alive, calm) {
        if (!alive || calm) return@LaunchedEffect
        while (true) {
            delay(Random.nextLong(2400, 5200))
            blink.animateTo(0.08f, tween(70))
            blink.animateTo(1f, tween(120))
            if (Random.nextInt(5) == 0) { delay(140); blink.animateTo(0.08f, tween(60)); blink.animateTo(1f, tween(110)) }
        }
    }
    val breathe = if (alive && !calm) rememberInfiniteTransition(label = "kai").animateFloat(-1f, 1f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "breathe") else null
    Canvas(modifier.size(size).then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)) {
        val h = hop.value
        val lift = sin(h * PI.toFloat()) * this.size.minDimension * 0.08f
        val squash = sin(h * PI.toFloat() * 2f) * 0.06f
        withTransform({
            translate(0f, -lift)
            scale(1f - squash, 1f + squash, Offset(this.size.width / 2, this.size.height * 0.9f))
        }) {
            drawKai(from, to, t.value.coerceIn(0f, 1.15f), blink = blink.value, breathe = breathe?.value ?: 0f, shadow = shadow)
        }
    }
}

fun interpolate(a: KaiLook, b: KaiLook, t: Float): KaiLook {
    if (t >= 1f) return b
    if (t <= 0f) return a
    // Lobe counts can't be mixed, so keep whichever shape dominates and blend everything else.
    val base = if (t < 0.5f) a else b
    return base.copy(
        depth = lerpF(a.depth, b.depth, t), wobble = lerpF(a.wobble, b.wobble, t), flat = lerpF(a.flat, b.flat, t),
        body = lerp(a.body, b.body, t), eyes = lerpF(a.eyes, b.eyes, t), lid = lerpF(a.lid, b.lid, t),
        gaze = Offset(lerpF(a.gaze.x, b.gaze.x, t), lerpF(a.gaze.y, b.gaze.y, t)), smile = lerpF(a.smile, b.smile, t),
        mouth = lerpF(a.mouth, b.mouth, t), blush = lerpF(a.blush, b.blush, t), wow = lerpF(a.wow, b.wow, t),
    )
}
