package com.kairos.app.core.design

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * The paper everything else sits on.
 *
 * Three slow washes of pigment, a fibre grain, and a light from the top-left.
 * Nothing here is a solid colour: the background is always wet.
 */
@Composable
fun WatercolorBackdrop(
    modifier: Modifier = Modifier,
    accent: AccentSet = KairosTheme.colors.accent,
    intensity: Float = 1f,
    drift: Boolean = !KairosTheme.reduceMotion,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = KairosTheme.colors
    val grain = rememberGrainShader(colors.dark)
    val phase = if (drift) {
        val transition = rememberInfiniteTransition(label = "wash")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(52_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "wash-phase",
        ).value
    } else {
        0f
    }

    val washes = if (colors.dark) {
        listOf(
            accent.periwinkle.copy(alpha = 0.36f * intensity) to Offset(0.14f, 0.06f),
            accent.blossom.copy(alpha = 0.20f * intensity) to Offset(0.92f, 0.26f),
            accent.mint.copy(alpha = 0.14f * intensity) to Offset(0.30f, 0.98f),
            accent.apricot.copy(alpha = 0.10f * intensity) to Offset(1.0f, 0.92f),
        )
    } else {
        listOf(
            accent.apricot.copy(alpha = 0.50f * intensity) to Offset(0.10f, 0.04f),
            accent.blossom.copy(alpha = 0.38f * intensity) to Offset(0.94f, 0.20f),
            accent.periwinkle.copy(alpha = 0.36f * intensity) to Offset(0.24f, 0.92f),
            accent.mint.copy(alpha = 0.30f * intensity) to Offset(0.98f, 0.80f),
            accent.butter.copy(alpha = 0.18f * intensity) to Offset(0.55f, 0.42f),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.paper)
            .drawBehind {
                washes.forEachIndexed { index, (color, anchor) ->
                    val wobble = 0.035f * sin(phase + index * 1.7f)
                    val wobbleY = 0.030f * cos(phase * 0.8f + index)
                    val center = Offset(
                        x = (anchor.x + wobble) * size.width,
                        y = (anchor.y + wobbleY) * size.height,
                    )
                    val radius = size.minDimension * (0.62f + 0.08f * sin(phase * 0.6f + index))
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(color, color.copy(alpha = color.alpha * 0.35f), Color.Transparent),
                            center = center,
                            radius = radius,
                        ),
                        radius = radius,
                        center = center,
                    )
                }
                // Fibre grain keeps the gradients from looking like plastic.
                drawRect(brush = grain, alpha = if (colors.dark) 0.05f else 0.07f)
                // A quiet light from above.
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = if (colors.dark) 0.03f else 0.22f), Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.55f,
                    ),
                )
            },
        content = content,
    )
}

/** A 128px tile of translucent speckles, used as paper fibre. */
@Composable
private fun rememberGrainShader(dark: Boolean): ShaderBrush {
    val density = LocalDensity.current
    return remember(dark, density) {
        val side = 128
        val bitmap = ImageBitmap(side, side)
        val canvas = androidx.compose.ui.graphics.Canvas(bitmap)
        val paint = Paint().apply { isAntiAlias = true }
        val random = Random(0x4B414952)
        repeat(1900) {
            val x = random.nextFloat() * side
            val y = random.nextFloat() * side
            val radius = random.nextFloat() * 0.9f + 0.25f
            val alpha = random.nextFloat() * 0.5f + 0.1f
            paint.color = if (dark) {
                Color.White.copy(alpha = alpha * 0.5f)
            } else {
                Color(0xFF5B4636).copy(alpha = alpha * 0.55f)
            }
            canvas.drawCircle(Offset(x, y), radius, paint)
        }
        ShaderBrush(ImageShader(bitmap, TileMode.Repeated, TileMode.Repeated))
    }
}

/**
 * A single pigment bloom. Used for poster art, empty states and the seal.
 * [seed] makes each bloom stable across recompositions.
 */
@Composable
fun WatercolorBloom(
    modifier: Modifier = Modifier,
    color: Color,
    seed: Int = 1,
    alpha: Float = 0.7f,
) {
    val points = remember(seed) {
        val random = Random(seed)
        List(7) {
            Offset(random.nextFloat(), random.nextFloat()) to (random.nextFloat() * 0.45f + 0.55f)
        }
    }
    Canvas(modifier) {
        drawBloom(color = color, seed = seed, alpha = alpha, points = points)
    }
}

internal fun DrawScope.drawBloom(
    color: Color,
    seed: Int,
    alpha: Float,
    points: List<Pair<Offset, Float>>,
) {
    val random = Random(seed * 31 + 7)
    points.forEach { (anchor, scale) ->
        val center = Offset(anchor.x * size.width, anchor.y * size.height)
        val radius = size.minDimension * 0.34f * scale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = alpha * 0.55f),
                    color.copy(alpha = alpha * 0.22f),
                    Color.Transparent,
                ),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }
    // Pigment edges: a handful of tiny wet specks.
    repeat(26) {
        val x = random.nextFloat() * size.width
        val y = random.nextFloat() * size.height
        drawCircle(
            color = color.copy(alpha = alpha * (0.05f + random.nextFloat() * 0.08f)),
            radius = random.nextFloat() * size.minDimension * 0.012f + 0.6f,
            center = Offset(x, y),
        )
    }
}

/** A torn paper edge, drawn as the bottom of a card. */
internal fun DrawScope.drawTornEdge(color: Color, y: Float, seed: Int = 3) {
    val random = Random(seed)
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(0f, y)
        var x = 0f
        while (x < size.width) {
            x += size.width / 26f
            lineTo(x, y + (random.nextFloat() - 0.5f) * size.height * 0.02f)
        }
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(path, color)
}

/** Convenience for drawing a soft vignette over artwork. */
internal fun DrawScope.drawVignette(color: Color, strength: Float = 0.35f) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, color.copy(alpha = strength)),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = size.maxDimension * 0.75f,
        ),
    )
}

/** Draws a soft drop shadow without allocating a layer when elevation is zero. */
internal fun DrawScope.drawSoftShadow(
    shape: androidx.compose.ui.graphics.Shape,
    color: Color,
    elevation: Float,
    alpha: Float = 0.18f,
) {
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.color = color.copy(alpha = alpha).toArgb()
            setShadowLayer(elevation, 0f, elevation * 0.35f, this.color)
        }
        canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, paint)
    }
}

/** Size helper for artwork that must not depend on layout. */
internal fun aspectSize(width: Float, ratio: Float): Size = Size(width, width / ratio)
