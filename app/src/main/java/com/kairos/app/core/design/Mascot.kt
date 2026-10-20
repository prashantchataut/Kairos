package com.kairos.app.core.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate

/**
 * Kai — the drop that keeps the day.
 *
 * A watercolour droplet with a sprout, drawn in a 100×100 space so it scales cleanly.
 * Kai is the app's voice when a screen has nothing to show yet: curious, never scolding.
 */
enum class KaiMood { Happy, Curious, Sleepy, Proud, Wink }

@Composable
fun Kai(
    modifier: Modifier = Modifier,
    mood: KaiMood = KaiMood.Happy,
    tint: Color = KairosTheme.colors.apricot,
    deep: Color = KairosTheme.colors.blossom,
    animated: Boolean = true,
) {
    val colors = KairosTheme.colors
    val reduce = KairosTheme.reduceMotion || !animated
    val float = rememberFloat(amplitude = 4f, durationMillis = 5200)

    val blinkPhase = if (reduce) {
        1f
    } else {
        val transition = rememberInfiniteTransition(label = "blink")
        val t by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4600, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "blink-clock",
        )
        // Eyes close only for a 120ms window inside each 4.6s cycle.
        if (t > 0.965f) 0.12f else 1f
    }

    Canvas(modifier = modifier) {
        val s = size.minDimension / 100f
        translate(top = float * s) {
            // Ground shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.10f),
                topLeft = Offset(22f * s, 90f * s),
                size = Size(56f * s, 8f * s),
            )

            // Sprout
            val sprout = Path().apply {
                moveTo(50f * s, 20f * s)
                cubicTo(
                    49f * s, 12f * s,
                    52f * s, 7f * s,
                    55f * s, 5f * s,
                )
                cubicTo(
                    55f * s, 10f * s,
                    53f * s, 15f * s,
                    50f * s, 20f * s,
                )
                close()
            }
            drawPath(sprout, colors.mint)
            val leaf = Path().apply {
                moveTo(50f * s, 21f * s)
                cubicTo(
                    44f * s, 17f * s,
                    40f * s, 11f * s,
                    39f * s, 4f * s,
                )
                cubicTo(
                    45f * s, 5f * s,
                    49f * s, 11f * s,
                    50f * s, 21f * s,
                )
                close()
            }
            drawPath(leaf, colors.mint.copy(alpha = 0.82f))

            // Body
            val body = Path().apply {
                moveTo(50f * s, 17f * s)
                cubicTo(72f * s, 15f * s, 92f * s, 30f * s, 93f * s, 52f * s)
                cubicTo(94f * s, 71f * s, 82f * s, 88f * s, 59f * s, 92f * s)
                cubicTo(38f * s, 96f * s, 15f * s, 86f * s, 9f * s, 66f * s)
                cubicTo(4f * s, 47f * s, 12f * s, 27f * s, 30f * s, 19f * s)
                cubicTo(37f * s, 16f * s, 44f * s, 17f * s, 50f * s, 17f * s)
                close()
            }
            drawPath(
                body,
                Brush.linearGradient(
                    colors = listOf(tint, deep),
                    start = Offset(30f * s, 20f * s),
                    end = Offset(70f * s, 95f * s),
                ),
            )
            // Wet edge
            drawPath(body, Color.White.copy(alpha = 0.30f), style = Stroke(width = 1.2f * s))

            // Highlight
            val shine = Path().apply {
                moveTo(30f * s, 34f * s)
                cubicTo(36f * s, 27f * s, 46f * s, 26f * s, 52f * s, 30f * s)
                cubicTo(45f * s, 31f * s, 37f * s, 36f * s, 34f * s, 42f * s)
                close()
            }
            drawPath(shine, Color.White.copy(alpha = 0.26f))

            // Blush
            drawOval(
                color = colors.rose.copy(alpha = 0.42f),
                topLeft = Offset(22f * s, 62f * s),
                size = Size(15f * s, 9f * s),
            )
            drawOval(
                color = colors.rose.copy(alpha = 0.42f),
                topLeft = Offset(63f * s, 62f * s),
                size = Size(15f * s, 9f * s),
            )

            val ink = Color(0xFF2A2233).copy(alpha = 0.86f)
            val eyeY = 56f * s
            val open = blinkPhase

            when (mood) {
                KaiMood.Happy, KaiMood.Curious -> {
                    drawOval(
                        color = ink,
                        topLeft = Offset(34f * s, eyeY - 5f * s * open),
                        size = Size(9f * s, 10f * s * open),
                    )
                    drawOval(
                        color = ink,
                        topLeft = Offset(58f * s, eyeY - 5f * s * open),
                        size = Size(9f * s, 10f * s * open),
                    )
                    // catchlights
                    drawCircle(Color.White.copy(alpha = 0.9f), radius = 1.5f * s, center = Offset(37f * s, eyeY - 2f * s))
                    drawCircle(Color.White.copy(alpha = 0.9f), radius = 1.5f * s, center = Offset(61f * s, eyeY - 2f * s))
                }

                KaiMood.Sleepy -> {
                    drawArc(
                        color = ink,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(33f * s, eyeY - 3f * s),
                        size = Size(11f * s, 8f * s),
                        style = Stroke(width = 2.6f * s, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = ink,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(57f * s, eyeY - 3f * s),
                        size = Size(11f * s, 8f * s),
                        style = Stroke(width = 2.6f * s, cap = StrokeCap.Round),
                    )
                }

                KaiMood.Proud -> {
                    drawArc(
                        color = ink,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(33f * s, eyeY - 1f * s),
                        size = Size(11f * s, 8f * s),
                        style = Stroke(width = 2.8f * s, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = ink,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(57f * s, eyeY - 1f * s),
                        size = Size(11f * s, 8f * s),
                        style = Stroke(width = 2.8f * s, cap = StrokeCap.Round),
                    )
                }

                KaiMood.Wink -> {
                    drawOval(
                        color = ink,
                        topLeft = Offset(34f * s, eyeY - 5f * s),
                        size = Size(9f * s, 10f * s),
                    )
                    drawArc(
                        color = ink,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(57f * s, eyeY - 2f * s),
                        size = Size(11f * s, 8f * s),
                        style = Stroke(width = 2.6f * s, cap = StrokeCap.Round),
                    )
                }
            }

            // Mouth
            val mouth = Path().apply {
                moveTo(43f * s, 71f * s)
                cubicTo(
                    46f * s, 77f * s,
                    55f * s, 77f * s,
                    58f * s, 71f * s,
                )
            }
            drawPath(
                mouth,
                ink,
                style = Stroke(width = 2.4f * s, cap = StrokeCap.Round),
            )
        }
    }
}
