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
    // The open moment becomes a living seed: a stem and two welcoming leaves.
    Canvas(modifier) {
        val u = size.minDimension / 100f
        drawRoundRect(accent, cornerRadius = CornerRadius(28*u), size = Size(100*u, 100*u))
        drawCircle(Color.White.copy(alpha = .12f), 38*u, Offset(28*u, 24*u))
        drawLine(Color.White, Offset(50*u, 77*u), Offset(50*u, 43*u), 6*u, androidx.compose.ui.graphics.StrokeCap.Round)
        val left = androidx.compose.ui.graphics.Path().apply {
            moveTo(50*u, 56*u); cubicTo(20*u, 60*u, 18*u, 23*u, 50*u, 43*u); close()
        }
        val right = androidx.compose.ui.graphics.Path().apply {
            moveTo(50*u, 43*u); cubicTo(48*u, 13*u, 86*u, 20*u, 50*u, 43*u); close()
        }
        drawPath(left, Color.White.copy(alpha = .85f)); drawPath(right, Color.White)
        drawArc(Color.White, 10f, 160f, false, Offset(26*u, 60*u), Size(48*u, 26*u), style = Stroke(4*u, cap = androidx.compose.ui.graphics.StrokeCap.Round))
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
