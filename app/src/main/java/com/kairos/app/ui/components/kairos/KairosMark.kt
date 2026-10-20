package com.kairos.app.ui.components.kairos

import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap

/** The seed-hourglass: a small moment with room to grow. */
@Composable
fun KairosMark(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    accent: Color = MaterialTheme.colorScheme.primary,
    revealed: Boolean = true
) {
    Canvas(modifier) {
        if (!revealed) return@Canvas
        val u = size.minDimension / 100f
        drawRoundRect(accent, cornerRadius = CornerRadius(30 * u))
        val seed = Path().apply {
            moveTo(29*u, 29*u)
            cubicTo(72*u, 26*u, 76*u, 55*u, 51*u, 76*u)
            cubicTo(27*u, 61*u, 23*u, 43*u, 29*u, 29*u)
            close()
        }
        drawPath(seed, Color.White)
        drawLine(accent, Offset(37*u, 39*u), Offset(57*u, 60*u), 5*u, StrokeCap.Round)
        drawCircle(accent, 5*u, Offset(48*u, 49*u))
    }
}

@Composable
fun KairosWordmark(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    androidx.compose.foundation.layout.Row(modifier, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        KairosMark(Modifier.size(34.dp), tint = tint, accent = accent)
        androidx.compose.material3.Text("Kairos", style = MaterialTheme.typography.titleLarge, color = tint)
    }
}
