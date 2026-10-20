package com.kairos.app.design.paint

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.kairos.app.design.Kairos
import com.kairos.app.design.Mood
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * The watercolour engine.
 *
 * Pigment is drawn as layered radial gradients clipped by an irregular,
 * seeded outline — never a perfect circle. Every blob gets a wet rim (a thin,
 * slightly darker stroke) so it reads as paint that dried, not a vector shape.
 * Paper grain sits on top at 4% so flat colour never looks like a UI panel.
 */

private var grainCache: ImageBitmap? = null

fun grainBitmap(): ImageBitmap {
    grainCache?.let { return it }
    val size = 128
    val pixels = IntArray(size * size)
    val rnd = Random(7)
    for (i in pixels.indices) {
        val v = rnd.nextInt(0, 255)
        val a = (rnd.nextFloat() * 26).toInt()
        pixels[i] = (a shl 24) or (v shl 16) or (v shl 8) or v
    }
    val bmp = android.graphics.Bitmap.createBitmap(pixels, size, size, android.graphics.Bitmap.Config.ARGB_8888)
    return bmp.asImageBitmap().also { grainCache = it }
}

@Composable
fun GrainOverlay(modifier: Modifier = Modifier, alpha: Float = 0.05f) {
    val brush = remember { ShaderBrush(ImageShader(grainBitmap(), TileMode.Repeated, TileMode.Repeated)) }
    Canvas(modifier) {
        drawRect(brush, alpha = alpha)
    }
}

/** A closed organic outline. Seeded, so a card's wash is stable across recompositions. */
fun blobPath(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    seed: Int,
    points: Int = 9,
    wobble: Float = 0.22f,
): Path {
    val rnd = Random(seed)
    val radii = FloatArray(points) { 1f + (rnd.nextFloat() - 0.5f) * 2f * wobble }
    val angles = FloatArray(points) { (it.toFloat() / points) * 2f * PI.toFloat() + (rnd.nextFloat() - 0.5f) * 0.25f }
    val pts = List(points) { i ->
        Offset(
            center.x + cos(angles[i]) * radiusX * radii[i],
            center.y + sin(angles[i]) * radiusY * radii[i],
        )
    }
    val path = Path()
    path.moveTo(pts[0].x, pts[0].y)
    for (i in pts.indices) {
        val p0 = pts[(i - 1 + pts.size) % pts.size]
        val p1 = pts[i]
        val p2 = pts[(i + 1) % pts.size]
        val p3 = pts[(i + 2) % pts.size]
        val c1 = Offset(p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f)
        val c2 = Offset(p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f)
        path.cubicTo(c1.x, c1.y, c2.x, c2.y, p2.x, p2.y)
    }
    path.close()
    return path
}

fun DrawScope.paintBlob(
    center: Offset,
    radiusX: Float,
    radiusY: Float,
    color: Color,
    seed: Int,
    alpha: Float = 0.5f,
    rim: Float = 0.16f,
    rotation: Float = 0f,
) {
    val path = blobPath(center, radiusX, radiusY, seed)
    rotate(rotation, pivot = center) {
        drawPath(
            path,
            Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = alpha),
                    color.copy(alpha = alpha * 0.82f),
                    color.copy(alpha = alpha * 0.34f),
                    Color.Transparent,
                ),
                center = center,
                radius = maxOf(radiusX, radiusY) * 1.12f,
            ),
        )
        // wet edge: pigment collects at the boundary while drying
        drawPath(
            path,
            color.copy(alpha = alpha * rim),
            style = Stroke(width = maxOf(radiusX, radiusY) * 0.05f),
        )
    }
}

/**
 * The app's ground. Three drifting washes + grain, tinted by the active mood.
 * Motion is deliberately slow (30–46s loops) — it should be noticed, not watched.
 */
@Composable
fun WatercolorBackdrop(
    modifier: Modifier = Modifier,
    seed: Int = 11,
    animated: Boolean = true,
    intensity: Float = 1f,
    accent: Color? = null,
) {
    val c = Kairos.colors
    val motion = Kairos.motion
    val drift = if (animated && !motion.reduced) {
        val t = rememberInfiniteTransition(label = "wash")
        t.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(36000, easing = LinearEasing), RepeatMode.Restart),
            label = "phase",
        ).value
    } else 0f

    val a = accent ?: c.accent
    val b = c.accentPartner
    val third = when (c.mood) {
        Mood.Sky -> c.mint
        Mood.Peach -> c.lilac
        Mood.Mint -> c.butter
        Mood.Lilac -> c.sky
    }
    val base = if (c.dark) c.canvas else c.canvas

    Canvas(modifier) {
        drawRect(
            Brush.linearGradient(
                listOf(base, c.canvasTint, base),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            )
        )
        val w = size.width
        val h = size.height
        paintBlob(
            center = Offset(w * 0.18f + sin(drift) * w * 0.05f, h * 0.14f + cos(drift * 0.8f) * h * 0.02f),
            radiusX = w * 0.62f, radiusY = h * 0.28f,
            color = a, seed = seed, alpha = 0.20f * intensity,
        )
        paintBlob(
            center = Offset(w * 0.92f - cos(drift * 0.7f) * w * 0.06f, h * 0.42f + sin(drift) * h * 0.03f),
            radiusX = w * 0.48f, radiusY = h * 0.24f,
            color = b, seed = seed + 3, alpha = 0.16f * intensity,
            rotation = 18f,
        )
        paintBlob(
            center = Offset(w * 0.34f + sin(drift * 0.5f) * w * 0.04f, h * 0.92f),
            radiusX = w * 0.72f, radiusY = h * 0.26f,
            color = third, seed = seed + 7, alpha = 0.14f * intensity,
            rotation = -12f,
        )
        drawRect(
            ShaderBrush(ImageShader(grainBitmap(), TileMode.Repeated, TileMode.Repeated)),
            alpha = if (c.dark) 0.035f else 0.045f,
        )
    }
}

/** A single wash used behind hero cards and capsules. */
@Composable
fun Wash(
    modifier: Modifier = Modifier,
    color: Color,
    seed: Int,
    alpha: Float = 0.5f,
    animated: Boolean = true,
) {
    val motion = Kairos.motion
    val t = if (animated && !motion.reduced) {
        val tr = rememberInfiniteTransition(label = "wash-single")
        tr.animateFloat(
            0f, 1f,
            infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
            label = "breath",
        ).value
    } else 0.5f

    Canvas(modifier) {
        val s = 0.94f + t * 0.08f
        paintBlob(
            center = Offset(size.width * 0.5f, size.height * 0.52f),
            radiusX = size.width * 0.55f * s,
            radiusY = size.height * 0.6f * s,
            color = color, seed = seed, alpha = alpha,
        )
    }
}

/** A hand-painted rule — used instead of 1px dividers in editorial places. */
@Composable
fun BrushRule(modifier: Modifier = Modifier, color: Color, seed: Int = 4) {
    Canvas(modifier) {
        val rnd = Random(seed)
        val path = Path()
        val y = size.height / 2
        path.moveTo(0f, y)
        var x = 0f
        while (x < size.width) {
            val nx = (x + size.width / 7f).coerceAtMost(size.width)
            path.cubicTo(
                x + (nx - x) * 0.3f, y + (rnd.nextFloat() - 0.5f) * size.height * 0.5f,
                x + (nx - x) * 0.7f, y + (rnd.nextFloat() - 0.5f) * size.height * 0.5f,
                nx, y,
            )
            x = nx
        }
        drawPath(path, color, style = Stroke(width = size.height * 0.55f))
    }
}

/** Confetti-free celebration: pigment flecks that bloom outward once. */
fun DrawScope.paintFlecks(
    progress: Float,
    center: Offset,
    colors: List<Color>,
    seed: Int = 1,
    count: Int = 14,
) {
    if (progress <= 0f || progress >= 1f) return
    val rnd = Random(seed)
    repeat(count) { i ->
        val angle = rnd.nextFloat() * 2f * PI.toFloat()
        val dist = (0.15f + rnd.nextFloat() * 0.85f) * progress * size.minDimension * 0.42f
        val r = (1f - progress) * (6f + rnd.nextFloat() * 12f)
        val color = colors[i % colors.size]
        drawCircle(
            color = color.copy(alpha = (1f - progress) * 0.55f),
            radius = r,
            center = center + Offset(cos(angle) * dist, sin(angle) * dist),
        )
    }
}

/** Size helper for drawing inside a Canvas of unknown size. */
val DrawScope.unit: Float get() = size.minDimension
