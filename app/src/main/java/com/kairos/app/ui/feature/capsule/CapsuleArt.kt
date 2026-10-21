package com.kairos.app.ui.feature.capsule

import com.kairos.app.ui.theme.canvas
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kairos.app.data.repo.AttachmentKind
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.drawMark
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Tint

fun tintOf(name: String) = runCatching { Tint.valueOf(name) }.getOrDefault(Tint.Sky)

/** Folder silhouette for sealed letters (from the reference's date folders). */
val FolderShape = GenericShape { size, _ ->
    val w = size.width; val h = size.height; val r = size.minDimension * 0.1f
    val tabW = w * 0.42f; val tabH = h * 0.11f
    moveTo(0f, r + tabH); lineTo(0f, h - r); quadraticTo(0f, h, r, h); lineTo(w - r, h); quadraticTo(w, h, w, h - r)
    lineTo(w, tabH + r); quadraticTo(w, tabH, w - r, tabH); lineTo(tabW + r * 0.8f, tabH)
    quadraticTo(tabW, tabH, tabW - r * 0.3f, tabH * 0.4f); quadraticTo(tabW - r * 0.6f, 0f, tabW - r * 1.4f, 0f)
    lineTo(r, 0f); quadraticTo(0f, 0f, 0f, r); close()
}

/**
 * A clean envelope. [flap] 0 = closed, 1 = folded open; [crack] splits the seal; [rise] lifts the
 * letter out. Drawn flat: white paper, a tinted lining, a cobalt seal with the Kairos mark.
 */
@Composable
fun Envelope(lining: Color, modifier: Modifier = Modifier, flap: Float = 0f, crack: Float = 0f, rise: Float = 0f, glow: Float = 0f) {
    val p = K.colors
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val paper = if (p.isDark) Color(0xFFE9EEF7) else Color.White
        val shade = lerp(paper, Color(0xFF9FB0CC), 0.18f)
        if (glow > 0f) drawCircle(Brush.radialGradient(listOf(lining.copy(alpha = 0.7f * glow), Color.Transparent)), w * 0.85f, Offset(w / 2, h / 2))
        drawRoundRect(lining, size = Size(w, h), cornerRadius = CornerRadius(w * 0.05f))
        if (rise > 0f) {
            val lh = h * 0.92f
            val top = h * 0.06f - lh * 0.6f * rise
            drawRoundRect(Color.White, Offset(w * 0.08f, top), Size(w * 0.84f, lh), CornerRadius(w * 0.03f))
            for (i in 1..4) drawLine(Color(0xFFCBD7EA), Offset(w * 0.16f, top + lh * 0.17f * i), Offset(w * (0.84f - (i % 2) * 0.12f), top + lh * 0.17f * i), strokeWidth = w * 0.012f)
        }
        val pocket = Path().apply {
            moveTo(0f, h * 0.2f); lineTo(w / 2, h * 0.6f); lineTo(w, h * 0.2f); lineTo(w, h * 0.95f)
            quadraticTo(w, h, w * 0.95f, h); lineTo(w * 0.05f, h); quadraticTo(0f, h, 0f, h * 0.95f); close()
        }
        drawPath(pocket, Brush.verticalGradient(listOf(paper, shade)))
        val fold = 1f - 2f * flap
        val flapPath = Path().apply { moveTo(0f, 0f); lineTo(w, 0f); lineTo(w / 2, h * 0.56f * fold); close() }
        drawPath(flapPath, if (fold > 0) paper else shade)
        drawPath(flapPath, Color(0xFF9FB0CC).copy(alpha = 0.35f), style = Stroke(w * 0.006f))
        if (flap < 0.5f) {
            val c = Offset(w / 2, h * 0.53f); val r = w * 0.1f
            val split = crack * w * 0.14f
            listOf(-1f, 1f).forEach { side ->
                translate(side * split, split * 1.4f) {
                    drawArc(Color(0xFF2D6BFF), if (side < 0) 90f else -90f, 180f, true, Offset(c.x - r, c.y - r), Size(r * 2, r * 2), alpha = 1f - crack * 0.7f)
                }
            }
            if (crack < 0.05f) translate(c.x - r * 0.55f, c.y - r * 0.55f) {
                drawIntoMark(r * 1.1f)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawIntoMark(s: Float) {
    // drawMark sizes itself from the DrawScope, so give it an s×s scope for the duration.
    val old = drawContext.size
    drawContext.size = Size(s, s)
    drawMark(Color.White, 0f)
    drawContext.size = old
}

/** A photo or file, as a small white card with a soft shadow and a slight tilt. */
@Composable
fun AttachmentCard(kind: AttachmentKind, model: Any, name: String, tilt: Float, modifier: Modifier = Modifier, width: Dp = 112.dp, playing: Boolean = false, onClick: (() -> Unit)? = null, onRemove: (() -> Unit)? = null) {
    val p = K.colors
    Box(modifier.rotate(tilt)) {
        Column(
            Modifier
                .width(width)
                .then(if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier)
                .shadow(14.dp, RoundedCornerShape(16.dp), ambientColor = p.shadow, spotColor = p.shadow)
                .clip(RoundedCornerShape(16.dp))
                .background(p.surface)
                .padding(6.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(width - 12.dp).clip(RoundedCornerShape(11.dp)).background(p.surfaceSoft), contentAlignment = Alignment.Center) {
                when (kind) {
                    AttachmentKind.Image -> AsyncImage(model, name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    AttachmentKind.Audio -> Icon(if (playing) Icons.Stop else Icons.Mic, null, tint = p.accent, modifier = Modifier.size(30.dp))
                    AttachmentKind.Video -> Icon(Icons.Play, null, tint = p.accent, modifier = Modifier.size(30.dp))
                    AttachmentKind.File -> Icon(Icons.File, null, tint = p.inkSoft, modifier = Modifier.size(30.dp))
                }
            }
            if (kind != AttachmentKind.Image) {
                Spacer(Modifier.height(6.dp))
                Text(name, style = K.type.caption, color = p.ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
        if (onRemove != null) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(4.dp).size(26.dp).pressable(label = "Remove $name", onClick = onRemove).clip(RoundedCornerShape(13.dp)).background(p.ink.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Close, "Remove", tint = p.canvas, modifier = Modifier.size(14.dp)) }
        }
    }
}

fun tiltFor(i: Int) = listOf(-4f, 3f, -2f, 5f, -3f, 2f)[i % 6]
