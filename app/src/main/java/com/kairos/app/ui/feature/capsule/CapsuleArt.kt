package com.kairos.app.ui.feature.capsule

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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kairos.app.data.repo.AttachmentKind
import com.kairos.app.ui.art.KaiLook
import com.kairos.app.ui.art.drawBloom
import com.kairos.app.ui.art.drawGrain
import com.kairos.app.ui.art.drawKai
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Tint
import kotlin.random.Random

fun tintOf(name: String) = runCatching { Tint.valueOf(name) }.getOrDefault(Tint.Sky)

/** Folder silhouette with a tab on the top left (r06, r11). */
val FolderShape = GenericShape { size, _ ->
    val w = size.width; val h = size.height; val r = size.minDimension * 0.1f
    val tabW = w * 0.42f; val tabH = h * 0.11f
    moveTo(0f, r + tabH); lineTo(0f, h - r); quadraticTo(0f, h, r, h); lineTo(w - r, h); quadraticTo(w, h, w, h - r)
    lineTo(w, tabH + r); quadraticTo(w, tabH, w - r, tabH); lineTo(tabW + r * 0.8f, tabH)
    quadraticTo(tabW, tabH, tabW - r * 0.3f, tabH * 0.4f); quadraticTo(tabW - r * 0.6f, 0f, tabW - r * 1.4f, 0f)
    lineTo(r, 0f); quadraticTo(0f, 0f, 0f, r); close()
}

/** The night sky of the time capsule (r06): cobalt into near-black, a few stars, and grain. */
fun Modifier.nightSky(): Modifier = composed {
    val p = K.colors
    drawBehind {
        drawRect(Brush.verticalGradient(p.night))
        val r = Random(7)
        repeat(42) {
            val o = Offset(r.nextFloat() * size.width, r.nextFloat() * size.height * 0.7f)
            drawCircle(Color.White.copy(alpha = 0.15f + r.nextFloat() * 0.45f), (0.6f + r.nextFloat() * 1.4f) * density, o)
        }
        drawCircle(Brush.radialGradient(listOf(Color(0xFF4D7BFF).copy(alpha = 0.35f), Color.Transparent), Offset(size.width * 0.85f, size.height * 0.08f), size.width * 0.7f), size.width * 0.7f, Offset(size.width * 0.85f, size.height * 0.08f))
        drawGrain(0.06f)
    }
}

/**
 * A glass folder with letters peeking out of the top (r06, r11). [paper] colours the letters (the
 * envelope colour the person chose); [rise] lifts them out; [glow] makes a ready letter shine;
 * [sticker] puts a little die-cut Kai (the mood when it was sealed) on the front; [sealed] adds the
 * wax bloom seal.
 */
fun DrawScope.drawGlassFolder(
    paper: Color,
    rise: Float = 0f,
    glow: Float = 0f,
    sticker: KaiLook? = null,
    sealed: Boolean = true,
    papers: Int = 2,
    seal: Float = 1f,
) {
    val w = size.width; val h = size.height
    if (glow > 0.01f) drawCircle(Brush.radialGradient(listOf(paper.copy(alpha = 0.55f * glow), Color.Transparent), Offset(w / 2, h * 0.5f), w * 0.8f), w * 0.8f, Offset(w / 2, h * 0.5f))
    // Back sheet with the tab.
    val back = FolderShape.createOutline(Size(w, h * 0.92f), layoutDirection, this)
    translate(0f, h * 0.08f) {
        drawOutline(back, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.1f))))
        drawOutline(back, Color.White.copy(alpha = 0.35f), style = Stroke(1.2f * density))
    }
    // Letters peeking out.
    val tilts = listOf(-7f, 5f, -2f)
    for (i in 0 until papers.coerceIn(0, 3)) {
        val c = if (i == papers - 1) paper else lerp(paper, Color.White, 0.55f + i * 0.15f)
        val top = h * (0.12f + i * 0.05f) - rise * h * 0.55f
        rotate(tilts[i] * (1f - rise * 0.6f), Offset(w * 0.5f, h * 0.6f)) {
            drawRoundRect(c, Offset(w * (0.14f + i * 0.04f), top), Size(w * (0.66f - i * 0.03f), h * 0.6f), CornerRadius(w * 0.05f))
            val lineC = if (c.luminance() > 0.7f) Candy.Navy.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.65f)
            for (k in 1..3) drawLine(lineC, Offset(w * (0.22f + i * 0.04f), top + h * (0.08f + k * 0.07f)), Offset(w * (0.62f - (k % 2) * 0.12f), top + h * (0.08f + k * 0.07f)), strokeWidth = h * 0.022f)
        }
    }
    // Front pocket: frosted glass with a bright rim on top.
    val front = Path().apply {
        moveTo(0f, h * 0.4f); lineTo(w, h * 0.36f); lineTo(w, h * 0.93f)
        quadraticTo(w, h, w * 0.93f, h); lineTo(w * 0.07f, h); quadraticTo(0f, h, 0f, h * 0.93f); close()
    }
    drawPath(front, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.42f), Color.White.copy(alpha = 0.16f)), startY = h * 0.36f, endY = h))
    drawPath(front, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.2f)), startY = h * 0.36f, endY = h * 0.7f), style = Stroke(1.4f * density))
    if (sealed && seal > 0.01f) {
        val c = Offset(w * 0.5f, h * 0.68f)
        val r = w * 0.11f
        withTransform({ scale(seal, seal, c) }) {
            drawCircle(Candy.Coral.copy(alpha = 0.95f), r, c)
            drawCircle(lerp(Candy.Coral, Color.Black, 0.12f), r * 0.78f, c, style = Stroke(r * 0.08f))
            drawBloom(c, r * 0.62f, petal = Color.White.copy(alpha = 0.92f), moment = null, core = Color.White)
        }
    }
    if (sticker != null) {
        val s = w * 0.2f
        val c = Offset(w * 0.8f, h * 0.84f)
        drawCircle(Color.White, s * 0.62f, c)
        drawKai(sticker, sticker, 1f, center = c, r = s * 0.44f, shadow = false)
    }
}

private fun Color.luminance() = 0.2126f * red + 0.7152f * green + 0.0722f * blue

@Composable
fun GlassFolder(paper: Color, modifier: Modifier = Modifier, rise: Float = 0f, glow: Float = 0f, sticker: KaiLook? = null, sealed: Boolean = true, papers: Int = 2, seal: Float = 1f) {
    Canvas(modifier) { drawGlassFolder(paper, rise, glow, sticker, sealed, papers, seal) }
}

/** A small glass folder with a letter peeking out, for peeks and badges. */
@Composable
fun MiniFolder(paper: Color, modifier: Modifier = Modifier) = GlassFolder(paper, modifier, sealed = false, papers = 1)

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
                .background(Color.White)
                .padding(6.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(width - 12.dp).clip(RoundedCornerShape(11.dp)).background(Color(0xFFEEF2FB)), contentAlignment = Alignment.Center) {
                when (kind) {
                    AttachmentKind.Image -> AsyncImage(model, name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    AttachmentKind.Audio -> Icon(if (playing) Icons.Stop else Icons.Mic, null, tint = Candy.Cobalt, modifier = Modifier.size(30.dp))
                    AttachmentKind.Video -> Icon(Icons.Play, null, tint = Candy.Cobalt, modifier = Modifier.size(30.dp))
                    AttachmentKind.File -> Icon(Icons.File, null, tint = Candy.Navy.copy(alpha = 0.6f), modifier = Modifier.size(30.dp))
                }
            }
            if (kind != AttachmentKind.Image) {
                Spacer(Modifier.height(6.dp))
                Text(name, style = K.type.caption, color = Candy.Navy, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
        if (onRemove != null) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(4.dp).size(26.dp).pressable(label = "Remove $name", onClick = onRemove).clip(RoundedCornerShape(13.dp)).background(Candy.Navy.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Close, "Remove", tint = Color.White, modifier = Modifier.size(14.dp)) }
        }
    }
}

fun tiltFor(i: Int) = listOf(-4f, 3f, -2f, 5f, -3f, 2f)[i % 6]
