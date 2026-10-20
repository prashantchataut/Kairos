package com.kairos.app.ui.kit

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.theme.Kairos
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** One paint bloom: where it sits (fractions of the canvas), how big, and its wobble signature. */
@Immutable
data class Bloom(
    val color: Color,
    val x: Float,
    val y: Float,
    val radius: Float,
    val seed: Int,
    val drift: Float = 1f,
)

/** Builds an organic, slightly lumpy closed shape — the outline of a puddle of wet paint. */
fun bloomPath(center: Offset, radius: Float, seed: Int, time: Float, points: Int = 56): Path {
    val rnd = Random(seed)
    val p1 = rnd.nextFloat() * 6.28f
    val p2 = rnd.nextFloat() * 6.28f
    val p3 = rnd.nextFloat() * 6.28f
    val a1 = 0.05f + rnd.nextFloat() * 0.06f
    val a2 = 0.03f + rnd.nextFloat() * 0.04f
    val path = Path()
    for (i in 0..points) {
        val t = (i % points) / points.toFloat() * 2f * PI.toFloat()
        val r = radius * (1f +
            a1 * sin(2f * t + p1 + time * 0.6f) +
            a2 * sin(3f * t + p2 - time * 0.4f) +
            0.02f * sin(7f * t + p3 + time))
        val x = center.x + cos(t) * r
        val y = center.y + sin(t) * r * 0.92f
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

/**
 * Paints a watercolor bloom: pale wet centre, pigment pooling at the edge (the tell-tale
 * darker rim of real watercolor), and a thin dried edge line.
 */
fun DrawScope.paintBloom(
    color: Color,
    center: Offset,
    radius: Float,
    seed: Int,
    time: Float,
    strength: Float = 1f,
    blend: BlendMode = BlendMode.SrcOver,
) {
    val path = bloomPath(center, radius, seed, time)
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            0f to color.copy(alpha = 0.30f * strength),
            0.62f to color.copy(alpha = 0.42f * strength),
            0.9f to color.copy(alpha = 0.58f * strength),
            1.05f to color.copy(alpha = 0.70f * strength),
            center = center,
            radius = radius * 1.08f,
        ),
        blendMode = blend,
    )
    drawPath(
        path = path,
        color = color.copy(alpha = 0.35f * strength),
        style = Stroke(width = 1.4.dp.toPx()),
        blendMode = blend,
    )
}

private fun grainBitmap(dark: Boolean): ImageBitmap {
    val size = 160
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val rnd = Random(7)
    val pixels = IntArray(size * size) {
        val v = rnd.nextInt(256)
        val a = rnd.nextInt(if (dark) 10 else 16)
        val c = if (dark) 255 else 40
        (a shl 24) or ((if (v > 128) c else 255 - c) shl 16) or ((if (v > 128) c else 255 - c) shl 8) or (if (v > 128) c else 255 - c)
    }
    bmp.setPixels(pixels, 0, size, 0, 0, size, size)
    return bmp.asImageBitmap()
}

fun defaultBlooms(washes: List<Color>, variant: Int = 0): List<Bloom> {
    val rnd = Random(31 + variant * 17)
    val layout = listOf(
        Triple(0.08f, 0.06f, 0.62f),
        Triple(0.92f, 0.22f, 0.55f),
        Triple(0.18f, 0.55f, 0.50f),
        Triple(0.88f, 0.72f, 0.60f),
        Triple(0.40f, 1.02f, 0.58f),
    )
    return layout.mapIndexed { i, (x, y, r) ->
        Bloom(
            color = washes[(i + variant) % washes.size],
            x = x + (rnd.nextFloat() - 0.5f) * 0.08f,
            y = y + (rnd.nextFloat() - 0.5f) * 0.06f,
            radius = r,
            seed = 100 + i * 13 + variant,
            drift = 0.6f + rnd.nextFloat() * 0.8f,
        )
    }
}

@Composable
fun rememberPaintTime(periodMs: Int = 48_000): State<Float> {
    if (Kairos.reduceMotion) return remember { mutableFloatStateOf(1.3f) }
    val transition = rememberInfiniteTransition(label = "paint")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(periodMs, easing = LinearEasing), RepeatMode.Restart),
        label = "paintTime",
    )
}

/**
 * The app's paper: slow-drifting watercolor blooms glazed with multiply (light) or screen
 * (dark) so overlapping washes mix like real pigment, softened, then a fine paper grain.
 */
@Composable
fun WatercolorBackdrop(
    modifier: Modifier = Modifier,
    blooms: List<Bloom>? = null,
    variant: Int = 0,
) {
    val k = Kairos.colors
    val targets = blooms ?: remember(k, variant) { defaultBlooms(k.washes.all, variant) }
    // When the variant changes (e.g. a new tab), each bloom flows to its new place and colour
    // instead of cutting — the paper re-wets.
    val flow = spring<Float>(dampingRatio = 1f, stiffness = 14f)
    val list = targets.mapIndexed { i, b ->
        val x by animateFloatAsState(b.x, flow, label = "bx$i")
        val y by animateFloatAsState(b.y, flow, label = "by$i")
        val r by animateFloatAsState(b.radius, flow, label = "br$i")
        val c by animateColorAsState(b.color, tween(1400), label = "bc$i")
        b.copy(x = x, y = y, radius = r, color = c)
    }
    val time = rememberPaintTime()
    val grain = remember(k.isDark) { grainBitmap(k.isDark) }
    val blend = if (k.isDark) BlendMode.Screen else BlendMode.Multiply
    Box(modifier.fillMaxSize()) {
        Canvas(
            Modifier
                .fillMaxSize()
                .then(if (Build.VERSION.SDK_INT >= 31) Modifier.blur(10.dp) else Modifier),
        ) {
            drawRect(k.paper)
            val t = time.value
            list.forEach { b ->
                val w = size.width
                val h = size.height
                val c = Offset(
                    b.x * w + sin(t * b.drift + b.seed) * w * 0.05f,
                    b.y * h + cos(t * b.drift * 0.8f + b.seed) * h * 0.025f,
                )
                paintBloom(b.color, c, b.radius * w, b.seed, t, if (k.isDark) 0.55f else 0.85f, blend)
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            drawRect(ShaderBrush(ImageShader(grain, TileMode.Repeated, TileMode.Repeated)))
        }
    }
}

/** A single small bloom used inside cards as a wash of colour in one corner. */
@Composable
fun CornerWash(color: Color, modifier: Modifier = Modifier, seed: Int = 3, at: Offset = Offset(0.85f, 0.15f), scale: Float = 0.75f) {
    val dark = Kairos.colors.isDark
    Canvas(modifier.fillMaxSize()) {
        paintBloom(
            color = color,
            center = Offset(size.width * at.x, size.height * at.y),
            radius = size.minDimension * scale,
            seed = seed,
            time = 0.8f,
            strength = if (dark) 0.6f else 0.75f,
            blend = if (dark) BlendMode.Screen else BlendMode.Multiply,
        )
    }
}

fun Size.bloomRadius(f: Float) = minDimension * f
