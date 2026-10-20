package com.kairos.app.ui.screens.capsule

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kairos.app.data.repo.AttachmentKind
import com.kairos.app.ui.components.squishClickable
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Space
import kotlin.random.Random

/** A tilted polaroid for photos; a little tape-stuck card for everything else. */
@Composable
fun AttachmentChip(
    kind: AttachmentKind,
    model: Any,
    name: String,
    tilt: Float,
    modifier: Modifier = Modifier,
    width: Dp = 112.dp,
    playing: Boolean = false,
    onClick: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
) {
    val palette = Kairos.colors
    Box(modifier.rotate(tilt)) {
        Column(
            Modifier
                .width(width)
                .shadow(10.dp, RoundedCornerShape(6.dp), ambientColor = palette.glassShadow, spotColor = palette.glassShadow)
                .background(Color(0xFFFFFCF6), RoundedCornerShape(6.dp))
                .then(if (onClick != null) Modifier.squishClickable(onClick = onClick) else Modifier)
                .padding(start = 7.dp, end = 7.dp, top = 7.dp, bottom = 10.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(width - 14.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFE9EEF8)),
                contentAlignment = Alignment.Center,
            ) {
                when (kind) {
                    AttachmentKind.Image -> AsyncImage(model, name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    AttachmentKind.Audio -> VoiceWave(name, playing)
                    AttachmentKind.Video -> Icon(KairosIcons.Play, null, tint = Color(0xFF3461E8), modifier = Modifier.size(34.dp))
                    AttachmentKind.File -> Icon(KairosIcons.File, null, tint = Color(0xFF55627F), modifier = Modifier.size(34.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                if (kind == AttachmentKind.Image) "" else name,
                style = Kairos.type.hand.copy(fontSize = Kairos.type.label.fontSize * 1.15f, lineHeight = Kairos.type.label.lineHeight),
                color = Color(0xFF1E2B4A), maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        // Washi tape.
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-8).dp)
                .rotate(-tilt * 1.5f)
                .size(width = 44.dp, height = 16.dp)
                .background(palette.washes[(name.hashCode() and 0x7fffffff) % palette.washes.size].copy(alpha = 0.75f))
        )
        if (onRemove != null) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-10).dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(palette.ink)
                    .squishClickable(label = "Remove $name", onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) { Icon(KairosIcons.Close, "Remove", tint = palette.paper, modifier = Modifier.size(16.dp)) }
        }
    }
}

@Composable
private fun VoiceWave(seed: String, playing: Boolean) {
    val bars = remember(seed) { Random(seed.hashCode()).let { r -> List(14) { 0.25f + r.nextFloat() * 0.75f } } }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.size(width = 72.dp, height = 36.dp)) {
            val gap = size.width / bars.size
            bars.forEachIndexed { i, b ->
                val h = size.height * b
                val x = gap * i + gap / 2
                drawLine(
                    Color(0xFF3461E8).copy(alpha = if (playing) 1f else 0.7f),
                    Offset(x, (size.height - h) / 2), Offset(x, (size.height + h) / 2),
                    strokeWidth = gap * 0.5f, cap = StrokeCap.Round,
                )
            }
        }
        Spacer(Modifier.height(Space.xs))
        Icon(if (playing) KairosIcons.Stop else KairosIcons.Play, null, tint = Color(0xFF3461E8), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun <T> remember(key: Any, calc: () -> T): T = androidx.compose.runtime.remember(key) { calc() }

fun tiltFor(i: Int): Float = listOf(-6f, 4f, -2f, 7f, -5f, 3f)[i % 6]

