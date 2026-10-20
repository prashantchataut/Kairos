package com.kairos.app.ui.components.kairos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/** Original vector artwork. Layered translucent pigment stays crisp at any density. */
@Composable
fun PaintedGarden(modifier: Modifier = Modifier, growth: Float = 1f) {
    val ink = MaterialTheme.colorScheme.onSurface
    val leaf = MaterialTheme.colorScheme.primary
    val wash = MaterialTheme.colorScheme.primaryContainer
    Canvas(modifier.semantics { contentDescription = "A smiling seedling on a little painted island" }) {
        val w = size.width; val h = size.height
        fun point(x: Float, y: Float) = Offset(x * w, y * h)
        // Uneven pigment edges, layered like acrylic on wet paper.
        repeat(18) { layer ->
            drawOval(wash.copy(alpha = .05f), point(.06f + layer * .002f, .12f + layer * .003f), Size(w * (.88f - layer * .004f), h * (.78f - layer * .005f)))
        }
        drawCircle(Color(0xFFF3C78F).copy(alpha = .65f), w * .09f, point(.78f, .22f))
        val island = Path().apply {
            moveTo(w * .12f, h * .8f)
            cubicTo(w * .25f, h * .59f, w * .72f, h * .65f, w * .89f, h * .8f)
            cubicTo(w * .8f, h * 1.02f, w * .22f, h * .98f, w * .12f, h * .8f)
        }
        drawPath(island, Color(0xFFA7C7BA))
        repeat(24) { i ->
            val x = .19f + (i * 37 % 64) / 100f
            drawCircle(Color.White.copy(alpha = .24f), w * .009f, point(x, .79f + sin(i.toFloat()) * .04f))
        }
        val top = .65f - growth.coerceIn(0f, 1f) * .26f
        drawLine(leaf, point(.5f, .79f), point(.5f, top), w * .023f, StrokeCap.Round)
        val leftLeaf = Path().apply {
            moveTo(w * .5f, h * (top + .08f))
            cubicTo(w * .26f, h * (top + .12f), w * .29f, h * (top - .14f), w * .5f, h * (top + .08f))
        }
        val rightLeaf = Path().apply {
            moveTo(w * .5f, h * top)
            cubicTo(w * .5f, h * (top - .22f), w * .78f, h * (top - .17f), w * .5f, h * top)
        }
        drawPath(leftLeaf, leaf.copy(alpha = .78f)); drawPath(rightLeaf, leaf.copy(alpha = .9f))
        drawOval(Color(0xFFF5DDC3), point(.37f, .64f), Size(w * .26f, h * .22f))
        drawCircle(ink, w * .009f, point(.455f, .72f)); drawCircle(ink, w * .009f, point(.545f, .72f))
        drawArc(ink, 0f, 180f, false, point(.478f, .735f), Size(w * .044f, h * .025f), style = Stroke(w * .008f, cap = StrokeCap.Round))
        drawCircle(Color(0xFFDC9C95).copy(alpha = .6f), w * .017f, point(.42f, .75f))
        drawCircle(Color(0xFFDC9C95).copy(alpha = .6f), w * .017f, point(.58f, .75f))
    }
}
