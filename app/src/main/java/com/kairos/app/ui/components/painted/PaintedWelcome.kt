package com.kairos.app.ui.components.painted

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.components.kairos.KairosGlassSurface

/** A tiny hand-painted landscape. Deterministic pigment flecks avoid per-frame allocation. */
@Composable
fun PocketGarden(modifier: Modifier = Modifier, blooms: Int = 3) {
    val sky = MaterialTheme.colorScheme.primaryContainer
    val pond = MaterialTheme.colorScheme.primary
    val leaf = MaterialTheme.colorScheme.tertiary
    val petal = MaterialTheme.colorScheme.secondary
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawOval(sky.copy(alpha = .65f), Offset(w * .05f, h * .1f), Size(w * .9f, h * .78f))
        // Pigment pools have uneven translucent edges, like acrylic on wet paper.
        repeat(38) { i ->
            val x = ((i * 67 + 13) % 101) / 101f
            val y = ((i * 31 + 7) % 97) / 97f
            drawCircle(sky.copy(alpha = .10f), w * (.06f + (i % 4) * .01f), Offset(w * x, h * (.2f + y * .55f)))
        }
        drawOval(leaf.copy(alpha = .22f), Offset(w * .08f, h * .60f), Size(w * .84f, h * .26f))
        drawOval(pond.copy(alpha = .24f), Offset(w * .36f, h * .7f), Size(w * .48f, h * .14f))
        // Our seed companion: a rounded, sleepy little cloud with sprout ears.
        drawOval(Color.White.copy(alpha = .92f), Offset(w * .36f, h * .35f), Size(w * .30f, h * .36f))
        val stem = Path().apply { moveTo(w * .51f, h * .37f); quadraticBezierTo(w * .48f, h * .21f, w * .57f, h * .20f) }
        drawPath(stem, leaf, style = androidx.compose.ui.graphics.drawscope.Stroke(w * .012f))
        rotate(-25f, Offset(w * .55f, h * .23f)) {
            drawOval(leaf, Offset(w * .52f, h * .17f), Size(w * .12f, h * .08f))
        }
        listOf(.46f, .56f).forEach { x -> drawCircle(pond, w * .009f, Offset(w * x, h * .52f)) }
        drawArc(pond, 0f, 180f, false, Offset(w * .49f, h * .55f), Size(w * .04f, h * .025f), style = androidx.compose.ui.graphics.drawscope.Stroke(w * .006f))
        repeat(blooms.coerceIn(0, 9)) { i ->
            val x = w * (.16f + (i % 3) * .32f)
            val y = h * (.63f + (i / 3) * .07f)
            drawLine(leaf, Offset(x, y), Offset(x, y + h * .14f), w * .009f)
            repeat(5) { n ->
                rotate(n * 72f, Offset(x, y)) { drawOval(petal.copy(alpha = .8f), Offset(x - w * .022f, y - h * .065f), Size(w * .044f, h * .07f)) }
            }
            drawCircle(Color.White, w * .012f, Offset(x, y))
        }
    }
}

@Composable
fun PaintedWelcome(onCapsules: () -> Unit, onPlay: () -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PocketGarden(Modifier.fillMaxWidth().height(180.dp))
        Text("A little room to grow.", style = MaterialTheme.typography.headlineLarge)
        Text("Learn a word. Leave a memory. Make time for play.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WelcomeDoor("Time capsule", "A letter for later", onCapsules, Modifier.weight(1f))
            WelcomeDoor("Play garden", "Small adventures", onPlay, Modifier.weight(1f))
        }
    }
}

@Composable
private fun WelcomeDoor(title: String, subtitle: String, action: () -> Unit, modifier: Modifier) {
    KairosGlassSurface(modifier = modifier, shape = RoundedCornerShape(24.dp), onClick = action, contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
