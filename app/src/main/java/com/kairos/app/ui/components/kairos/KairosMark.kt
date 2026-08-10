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
import androidx.compose.ui.geometry.CornerRadius
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

/**
 * The Kairos mark — "the moment between".
 *
 * A rounded tile holding a single flowing K whose two strokes converge into a
 * filled dot: two forms meeting, a moment captured. The dot carries a short
 * motion trail, so the mark reads as both a letter and a symbol of
 * transformation. One silhouette, monochrome-safe, legible at 24px, and
 * volumetric enough for App Store scale.
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
    val idleScale = if (reducedMotion) 1f else 1f + breathe * 0.02f

    Canvas(
        modifier = modifier.graphicsLayer {
            alpha = reveal
            scaleX = (0.86f + 0.14f * reveal) * idleScale
            scaleY = (0.86f + 0.14f * reveal) * idleScale
        }
    ) {
        val min = size.minDimension
        val tileInset = min * 0.02f
        val tileSize = min - tileInset * 2f
        val r = tileSize * 0.30f

        // Tile — rounded square, volumetric vertical gradient.
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    lighten(accent, 0.14f),
                    accent,
                    darken(accent, 0.22f)
                ),
                startY = tileInset,
                endY = size.height - tileInset
            ),
            topLeft = Offset(tileInset, tileInset),
            size = Size(tileSize, tileSize),
            cornerRadius = CornerRadius(r, r)
        )

        // A soft inner highlight along the top edge.
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.28f * reveal), Color.Transparent),
                startY = tileInset,
                endY = tileInset + tileSize * 0.42f
            ),
            topLeft = Offset(tileInset, tileInset),
            size = Size(tileSize, tileSize),
            cornerRadius = CornerRadius(r, r)
        )

        // The flowing K — strokes in relative tile coordinates.
        val u = tileSize / 100f
        val ox = tileInset
        val oy = tileInset
        val strokeW = u * 8.5f
        val ink = Color.White

        // Vertical stem.
        drawLine(
            color = ink.copy(alpha = 0.96f * reveal),
            start = Offset(ox + u * 30f, oy + u * 26f),
            end = Offset(ox + u * 30f, oy + u * 74f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Upper arm — sweeps from the stem toward the upper right.
        drawLine(
            color = ink.copy(alpha = 0.96f * reveal),
            start = Offset(ox + u * 30f, oy + u * 36f),
            end = Offset(ox + u * 66f, oy + u * 25f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Lower arm — converges toward the moment dot.
        drawLine(
            color = ink.copy(alpha = 0.96f * reveal),
            start = Offset(ox + u * 30f, oy + u * 48f),
            end = Offset(ox + u * 58f, oy + u * 71f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // The moment — a filled dot where the movement gathers.
        drawCircle(
            color = Color.White.copy(alpha = 0.98f * reveal),
            radius = u * 7.5f,
            center = Offset(ox + u * 74f, oy + u * 78f)
        )

        // Motion trail from the dot — momentum, a moment captured in motion.
        drawLine(
            color = Color.White.copy(alpha = 0.55f * reveal),
            start = Offset(ox + u * 78f, oy + u * 82f),
            end = Offset(ox + u * 88f, oy + u * 90f),
            strokeWidth = strokeW * 0.55f,
            cap = StrokeCap.Round
        )
    }
}

private fun lighten(color: Color, amount: Float): Color = Color(
    red = color.red + (1f - color.red) * amount,
    green = color.green + (1f - color.green) * amount,
    blue = color.blue + (1f - color.blue) * amount,
    alpha = color.alpha
)

private fun darken(color: Color, amount: Float): Color = Color(
    red = color.red * (1f - amount),
    green = color.green * (1f - amount),
    blue = color.blue * (1f - amount),
    alpha = color.alpha
)

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
