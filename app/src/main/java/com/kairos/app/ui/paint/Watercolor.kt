package com.kairos.app.ui.paint

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Wash
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Acrylic-watercolour painting, done procedurally.
 *
 * A pigment pool is an irregular closed curve filled in several translucent layers (so the
 * colour pools darker towards the middle, like wet paint), with a slightly darker rim where
 * pigment collects as water dries, plus a faint bleed halo. A soft blur and a paper-grain
 * overlay finish the look. Nothing here is a bitmap asset — every wash is unique to its seed.
 */

/** An irregular, smooth blob outline built from a Catmull-Rom loop through wobbly points. */
fun blobPath(center: Offset, radius: Float, seed: Int, wobble: Float = 0.18f, phase: Float = 0f, points: Int = 9): Path {
    val rnd = Random(seed)
    val offsets = FloatArray(points) { rnd.nextFloat() * 2f - 1f }
    val freq = 2 + rnd.nextInt(3)
    val pts = List(points) { i ->
        val a = (i.toFloat() / points) * 2f * PI.toFloat()
        val r = radius * (1f + wobble * (0.55f * sin(a * freq + seed + phase) + 0.45f * offsets[i] * cos(phase * 0.7f + i)))
        Offset(center.x + cos(a) * r, center.y + sin(a) * r)
    }
    return Path().apply {
        moveTo(pts[0].x, pts[0].y)
        for (i in pts.indices) {
            val p0 = pts[(i - 1 + points) % points]
            val p1 = pts[i]
            val p2 = pts[(i + 1) % points]
            val p3 = pts[(i + 2) % points]
            cubicTo(
                p1.x + (p2.x - p0.x) / 6f, p1.y + (p2.y - p0.y) / 6f,
                p2.x - (p3.x - p1.x) / 6f, p2.y - (p3.y - p1.y) / 6f,
                p2.x, p2.y,
            )
        }
        close()
    }
}

private val PigmentInk = Color(0xFF1E2B4A)

/** Paints one pigment pool. [strength] scales opacity, so the same call works for washes and accents. */
fun DrawScope.watercolorPool(
    center: Offset,
    radius: Float,
    color: Color,
    seed: Int,
    phase: Float = 0f,
    strength: Float = 1f,
    edge: Boolean = true,
) {
    // Bleed: the water that travelled past the pigment.
    drawCircle(
        brush = Brush.radialGradient(
            listOf(color.copy(alpha = 0.22f * strength), Color.Transparent),
            center = center, radius = radius * 1.45f,
        ),
        radius = radius * 1.45f, center = center,
    )
    // Body: three offset layers, each a little smaller, so the pool reads as built-up glazes.
    val rnd = Random(seed * 31)
    for (layer in 0 until 3) {
        val r = radius * (1f - layer * 0.17f)
        val c = Offset(center.x + (rnd.nextFloat() - 0.5f) * radius * 0.25f, center.y + (rnd.nextFloat() - 0.5f) * radius * 0.25f)
        val path = blobPath(c, r, seed + layer * 13, wobble = 0.16f + layer * 0.04f, phase = phase * (1f + layer * 0.3f))
        drawPath(
            path,
            brush = Brush.radialGradient(
                listOf(color.copy(alpha = (0.30f + layer * 0.06f) * strength), color.copy(alpha = 0.16f * strength)),
                center = c, radius = r * 1.1f,
            ),
        )
        if (edge && layer == 0) {
            // Pigment rim: slightly darker, the tell-tale of real watercolour drying.
            drawPath(
                path,
                color = lerp(color, PigmentInk, 0.18f).copy(alpha = 0.20f * strength),
                style = Stroke(width = radius * 0.022f),
            )
        }
    }
    // Granulation: a few darker flecks where pigment settled into the paper.
    repeat(14) {
        val a = rnd.nextFloat() * 2f * PI.toFloat()
        val d = rnd.nextFloat() * radius * 0.85f
        drawCircle(
            color = lerp(color, PigmentInk, 0.25f).copy(alpha = 0.10f * strength),
            radius = radius * (0.006f + rnd.nextFloat() * 0.012f),
            center = Offset(center.x + cos(a) * d, center.y + sin(a) * d),
        )
    }
}

/** A noise tile that makes flat colour feel like cold-press paper. Generated once per process. */
object PaperGrain {
    private var cached: ImageBitmap? = null

    fun tile(): ImageBitmap = cached ?: run {
        val size = 160
        val rnd = Random(7)
        val pixels = IntArray(size * size) {
            val v = rnd.nextFloat()
            when {
                v > 0.985f -> 0x22000000
                v > 0.94f -> 0x0F000000
                v < 0.05f -> 0x18FFFFFF
                else -> 0x00000000
            }
        }
        Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888).asImageBitmap().also { cached = it }
    }
}

fun DrawScope.paperGrain(alpha: Float = 1f) {
    drawRect(brush = ShaderBrush(ImageShader(PaperGrain.tile(), TileMode.Repeated, TileMode.Repeated)), alpha = alpha)
}

/** Where pools sit on screen, as fractions of the canvas. */
private data class PoolSlot(val x: Float, val y: Float, val r: Float, val seed: Int, val driftX: Float, val driftY: Float)

private val Slots = listOf(
    PoolSlot(0.05f, 0.06f, 0.62f, 11, 0.06f, 0.04f),
    PoolSlot(0.98f, 0.20f, 0.52f, 23, -0.05f, 0.05f),
    PoolSlot(0.30f, 0.55f, 0.42f, 37, 0.04f, -0.05f),
    PoolSlot(0.92f, 0.78f, 0.55f, 41, -0.06f, -0.03f),
    PoolSlot(0.08f, 0.98f, 0.50f, 53, 0.05f, -0.04f),
)

/**
 * The living paper behind every screen. Pools drift and breathe slowly; when the scene changes
 * (a new tab, a mood), each pool's pigment cross-fades so the page seems to re-wet in a new colour.
 */
@Composable
fun WatercolorBackground(
    washes: List<Wash>,
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
) {
    val palette = Kairos.colors
    val reduced = Kairos.reducedMotion
    val colors: List<State<Color>> = Slots.indices.map { i ->
        animateColorAsState(
            washes[i % washes.size].color(palette.isDark),
            animationSpec = tween(900), label = "wash$i",
        )
    }
    val time = if (reduced) remember { mutableFloatStateOf(0.6f) } else {
        rememberInfiniteTransition(label = "paper").animateFloat(
            0f, (2 * PI).toFloat(),
            infiniteRepeatable(tween(48_000, easing = LinearEasing), RepeatMode.Restart),
            label = "t",
        )
    }
    val blurPx = with(LocalDensity.current) { androidx.compose.ui.unit.Dp(14f).toPx() }
    Box(modifier.fillMaxSize()) {
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        renderEffect = BlurEffect(blurPx, blurPx, TileMode.Clamp)
                    }
                },
        ) {
            drawRect(Brush.verticalGradient(listOf(palette.paper, palette.paperDeep)))
            val t = time.value
            val base = min(size.width, size.height * 0.75f)
            Slots.forEachIndexed { i, s ->
                val c = Offset(
                    size.width * (s.x + s.driftX * sin(t + i * 1.3f)),
                    size.height * (s.y + s.driftY * cos(t * 1.2f + i)),
                )
                watercolorPool(
                    center = c,
                    radius = base * s.r * (1f + 0.04f * sin(t * 2f + i)),
                    color = colors[i].value,
                    seed = s.seed,
                    phase = t,
                    strength = intensity * if (palette.isDark) 0.9f else 1f,
                )
            }
        }
        Canvas(Modifier.fillMaxSize()) { paperGrain(if (palette.isDark) 0.5f else 0.9f) }
    }
}

/** A single static pool, for card corners and illustrations. */
@Composable
fun WashSpot(color: Color, seed: Int, modifier: Modifier = Modifier, strength: Float = 1f) {
    Canvas(modifier) {
        watercolorPool(Offset(size.width / 2, size.height / 2), min(size.width, size.height) / 2.6f, color, seed, strength = strength)
    }
}

fun Size.minDim() = min(width, height)
