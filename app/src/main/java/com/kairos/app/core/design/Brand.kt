package com.kairos.app.core.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.core.design.KairosTheme

/**
 * The Kairos mark.
 *
 * A bookmark ribbon whose two arms grow into leaves, with the spark of a moment kept above it.
 * Read one way it is a "K"; read another it is something being marked, and something growing.
 */
@Composable
fun KairosMark(
    modifier: Modifier = Modifier,
    tint: Color = KairosTheme.colors.ink,
    animated: Boolean = false,
) {
    val grow = remember { Animatable(if (animated) 0f else 1f) }
    if (animated) {
        LaunchedEffect(Unit) {
            grow.animateTo(1f, tween(1100, delayMillis = 120, easing = FastOutSlowInEasing))
        }
    }
    Canvas(modifier = modifier) {
        val s = size.minDimension / 24f
        val center = Offset(size.width / 2f, size.height / 2f)
        val origin = Offset(center.x - 12f * s, center.y - 12f * s)
        fun p(x: Float, y: Float) = Offset(origin.x + x * s, origin.y + y * s)

        val alpha = grow.value.coerceIn(0f, 1f)
        val stemScale = 0.6f + 0.4f * alpha

        // Ribbon stem
        drawRoundRect(
            color = tint.copy(alpha = alpha),
            topLeft = p(6.5f, 4f + (1f - stemScale) * 8f),
            size = Size(4f * s, 16f * s * stemScale),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * s, 2f * s),
        )

        // Leaves swing out from the stem as the mark grows.
        rotate(degrees = -10f + 10f * alpha, pivot = p(9f, 12f)) {
            val upper = Path().apply {
                moveTo(p(10.4f, 11.4f).x, p(10.4f, 11.4f).y)
                cubicTo(
                    p(12.4f, 8.4f).x, p(12.4f, 8.4f).y,
                    p(15.0f, 6.5f).x, p(15.0f, 6.5f).y,
                    p(18.5f, 5.5f).x, p(18.5f, 5.5f).y,
                )
                cubicTo(
                    p(19.2f, 5.3f).x, p(19.2f, 5.3f).y,
                    p(19.8f, 6.0f).x, p(19.8f, 6.0f).y,
                    p(19.5f, 6.7f).x, p(19.5f, 6.7f).y,
                )
                cubicTo(
                    p(18.2f, 9.8f).x, p(18.2f, 9.8f).y,
                    p(15.6f, 11.7f).x, p(15.6f, 11.7f).y,
                    p(12.2f, 12.7f).x, p(12.2f, 12.7f).y,
                )
                cubicTo(
                    p(11.3f, 13.0f).x, p(11.3f, 13.0f).y,
                    p(10.1f, 12.3f).x, p(10.1f, 12.3f).y,
                    p(10.4f, 11.4f).x, p(10.4f, 11.4f).y,
                )
                close()
            }
            drawPath(upper, tint.copy(alpha = alpha))
        }

        rotate(degrees = 10f - 10f * alpha, pivot = p(9f, 12f)) {
            val lower = Path().apply {
                moveTo(p(10.4f, 12.6f).x, p(10.4f, 12.6f).y)
                cubicTo(
                    p(12.4f, 15.6f).x, p(12.4f, 15.6f).y,
                    p(15.0f, 17.5f).x, p(15.0f, 17.5f).y,
                    p(18.5f, 18.5f).x, p(18.5f, 18.5f).y,
                )
                cubicTo(
                    p(19.2f, 18.7f).x, p(19.2f, 18.7f).y,
                    p(19.8f, 18.0f).x, p(19.8f, 18.0f).y,
                    p(19.5f, 17.3f).x, p(19.5f, 17.3f).y,
                )
                cubicTo(
                    p(18.2f, 14.2f).x, p(18.2f, 14.2f).y,
                    p(15.6f, 12.3f).x, p(15.6f, 12.3f).y,
                    p(12.2f, 11.3f).x, p(12.2f, 11.3f).y,
                )
                cubicTo(
                    p(11.3f, 11.0f).x, p(11.3f, 11.0f).y,
                    p(10.1f, 11.7f).x, p(10.1f, 11.7f).y,
                    p(10.4f, 12.6f).x, p(10.4f, 12.6f).y,
                )
                close()
            }
            drawPath(lower, tint.copy(alpha = alpha))
        }

        // Spark of the moment.
        val sparkAlpha = ((alpha - 0.55f) / 0.45f).coerceIn(0f, 1f)
        drawSpark(center = p(20.2f, 2.6f), radius = 1.7f * s, color = tint.copy(alpha = sparkAlpha))
    }
}

internal fun DrawScope.drawSpark(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        cubicTo(
            center.x + radius * 0.18f, center.y - radius * 0.22f,
            center.x + radius * 0.22f, center.y - radius * 0.18f,
            center.x + radius, center.y,
        )
        cubicTo(
            center.x + radius * 0.22f, center.y + radius * 0.18f,
            center.x + radius * 0.18f, center.y + radius * 0.22f,
            center.x, center.y + radius,
        )
        cubicTo(
            center.x - radius * 0.18f, center.y + radius * 0.22f,
            center.x - radius * 0.22f, center.y + radius * 0.18f,
            center.x - radius, center.y,
        )
        cubicTo(
            center.x - radius * 0.22f, center.y - radius * 0.18f,
            center.x - radius * 0.18f, center.y - radius * 0.22f,
            center.x, center.y - radius,
        )
        close()
    }
    drawPath(path, color)
}

/** The mark on a warm watercolour tile. Used for splash, onboarding and the app icon preview. */
@Composable
fun MarkTile(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    animated: Boolean = false,
    tint: Color = Color(0xFFFFFBF4),
) {
    val colors = KairosTheme.colors
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        listOf(colors.apricot, colors.blossom, colors.periwinkle),
                        start = Offset.Zero,
                        end = Offset(this.size.width, this.size.height),
                    ),
                )
                drawBloom(
                    color = colors.butter,
                    seed = 4,
                    alpha = 0.55f,
                    points = listOf(Offset(0.2f, 0.25f) to 1f, Offset(0.8f, 0.8f) to 0.8f),
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.30f), Color.Transparent),
                        endY = this.size.height * 0.6f,
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        KairosMark(
            modifier = Modifier.size(size * 0.62f),
            tint = tint,
            animated = animated,
        )
    }
}

/** Wordmark: serif name, handwriting tagline. */
@Composable
fun KairosWordmark(
    modifier: Modifier = Modifier,
    tagline: String? = null,
    color: Color = KairosTheme.colors.ink,
    size: Dp = 34.dp,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "Kairos",
            color = color,
            fontSize = size.value.sp,
            fontFamily = KairosDisplay,
            letterSpacing = (-0.4).sp,
            lineHeight = (size.value * 1.05f).sp,
        )
        if (tagline != null) {
            Text(
                text = tagline,
                color = color.copy(alpha = 0.62f),
                style = KairosType.handSmall,
                fontStyle = FontStyle.Normal,
            )
        }
    }
}

/** A small inline lockup used in headers. */
@Composable
fun MarkLockup(modifier: Modifier = Modifier, tint: Color = KairosTheme.colors.ink) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        KairosMark(modifier = Modifier.size(22.dp), tint = tint)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Kairos",
            color = tint,
            fontFamily = KairosDisplay,
            fontSize = 22.sp,
            letterSpacing = (-0.2).sp,
        )
    }
}
