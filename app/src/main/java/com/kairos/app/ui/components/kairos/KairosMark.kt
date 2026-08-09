package com.kairos.app.ui.components.kairos

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.animation.KairosDurations
import com.kairos.app.ui.animation.KairosEasing
import com.kairos.app.ui.animation.rememberKairosReducedMotion

private val KairosFaceInk = Color(0xFF101828)

private fun lighten(color: Color, amount: Float): Color = Color(
    red = color.red + (1f - color.red) * amount,
    green = color.green + (1f - color.green) * amount,
    blue = color.blue + (1f - color.blue) * amount,
    alpha = color.alpha
)

/**
 * The Kairos companion mark — a soft organic "moment" form.
 *
 * A rounded, jelly-like body with two calm eyes and a subtle smile reads as a
 * friendly emotional companion rather than a clinical symbol; the small spark
 * floating at the top-right carries the "moment in time" idea. The body is a
 * translucent gradient so the mark feels volumetric, alive, and premium at
 * every size from 24px navigation to the splash screen.
 */
@Composable
fun KairosMark(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    accent: Color = MaterialTheme.colorScheme.primary,
    revealed: Boolean = true
) {
    val reducedMotion = rememberKairosReducedMotion()
    val reveal by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (reducedMotion) KairosDurations.Micro else 520,
            easing = KairosEasing.EaseOutExpo
        ),
        label = "kairos-mark-reveal"
    )

    val transition = rememberInfiniteTransition(label = "kairos-mark-breathe")
    val breathe by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "kairos-mark-breathe-scale"
    )
    val idleScale = if (reducedMotion) 1f else 1f + breathe * 0.025f

    Canvas(
        modifier = modifier.graphicsLayer {
            alpha = reveal
            scaleX = (0.86f + 0.14f * reveal) * idleScale
            scaleY = (0.86f + 0.14f * reveal) * idleScale
        }
    ) {
        val min = size.minDimension
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Soft aura behind the body.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.30f * reveal), Color.Transparent),
                center = Offset(cx, cy),
                radius = min * 0.62f
            ),
            radius = min * 0.62f,
            center = Offset(cx, cy)
        )

        // Body — rounded, translucent, volumetric.
        val bodyInset = min * 0.10f
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    lighten(tint, 0.10f).copy(alpha = 0.96f * reveal),
                    tint.copy(alpha = 0.88f * reveal)
                ),
                startY = bodyInset,
                endY = size.height - bodyInset
            ),
            topLeft = Offset(bodyInset, bodyInset),
            size = Size(min - bodyInset * 2f, min - bodyInset * 2f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(min * 0.30f)
        )

        // Eyes — calm, rounded.
        val eyeY = min * 0.44f
        val eyeW = min * 0.115f
        val eyeH = min * 0.155f
        listOf(0.36f, 0.64f).forEach { fx ->
            val eyeCx = size.width * fx
            drawOval(
                color = KairosFaceInk.copy(alpha = 0.88f * reveal),
                topLeft = Offset(eyeCx - eyeW / 2f, eyeY - eyeH / 2f),
                size = Size(eyeW, eyeH)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f * reveal),
                radius = eyeW * 0.20f,
                center = Offset(eyeCx - eyeW * 0.22f, eyeY - eyeH * 0.22f)
            )
        }

        // Subtle calm smile.
        drawArc(
            color = KairosFaceInk.copy(alpha = 0.70f * reveal),
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(cx - min * 0.11f, min * 0.47f),
            size = Size(min * 0.22f, min * 0.17f),
            style = Stroke(width = min * 0.032f, cap = StrokeCap.Round)
        )

        // The moment spark — a small four-point star.
        val sparkX = size.width * 0.86f
        val sparkY = size.height * 0.17f
        val sparkLen = min * 0.10f
        drawLine(
            color = accent.copy(alpha = 0.95f * reveal),
            start = Offset(sparkX - sparkLen, sparkY),
            end = Offset(sparkX + sparkLen, sparkY),
            strokeWidth = min * 0.030f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = accent.copy(alpha = 0.95f * reveal),
            start = Offset(sparkX, sparkY - sparkLen),
            end = Offset(sparkX, sparkY + sparkLen),
            strokeWidth = min * 0.030f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun KairosWordmark(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KairosMark(
            modifier = Modifier.size(34.dp),
            tint = tint,
            accent = accent
        )
        Text(
            text = "Kairos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = tint
        )
    }
}
