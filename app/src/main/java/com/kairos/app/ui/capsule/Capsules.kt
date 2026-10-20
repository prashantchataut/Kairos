package com.kairos.app.ui.capsule

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.db.AttachmentEntity
import com.kairos.app.data.db.CapsuleEntity
import com.kairos.app.ui.art.darken
import com.kairos.app.ui.art.drawDrop
import com.kairos.app.ui.art.drawSparkle
import com.kairos.app.ui.components.EmptyState
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.components.ScreenHeader
import com.kairos.app.ui.components.SectionTitle
import com.kairos.app.ui.components.clickableNoRipple
import com.kairos.app.ui.components.enter
import com.kairos.app.ui.components.kairosViewModel
import com.kairos.app.ui.components.screenPadding
import com.kairos.app.ui.glass.GlassCard
import com.kairos.app.ui.glass.GlassIconButton
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.KButton
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.nav.Routes
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Six wax seals. The stamp you choose is the first thing future-you sees. */
val StampTints = listOf(Color(0xFFE5566B), Color(0xFF3B6FEA), Color(0xFF2FA36B), Color(0xFF8A5CF0), Color(0xFFE8963A), Color(0xFFD45FA0))
val StampNames = listOf("Heart", "Star", "Leaf", "Moon", "Sun", "Drop")

data class CapsuleCard(val entity: CapsuleEntity, val attachments: List<AttachmentEntity>)

class CapsulesViewModel(c: AppContainer) : ViewModel() {
    val capsules = combine(c.capsules.observeAll(), c.capsules.observeAllAttachments()) { caps, atts ->
        val byId = atts.groupBy { it.capsuleId }
        caps.map { CapsuleCard(it, byId[it.id].orEmpty()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

fun formatDate(ms: Long, pattern: String = "d MMM yyyy"): String =
    Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))

fun untilText(ms: Long, now: Long): String {
    val diff = (ms - now).coerceAtLeast(0)
    val mins = diff / 60_000
    val hours = mins / 60
    val days = hours / 24
    return when {
        days >= 365 -> "${days / 365} year${if (days / 365 > 1) "s" else ""}, ${(days % 365) / 30} months"
        days >= 60 -> "${days / 30} months"
        days >= 2 -> "$days days"
        hours >= 1 -> "${hours}h ${mins % 60}m"
        mins >= 1 -> "$mins min"
        else -> "moments"
    }
}

@Composable
fun rememberNow(tickMs: Long = 30_000): Long {
    val now = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(tickMs) {
        while (true) {
            delay(tickMs)
            now.longValue = System.currentTimeMillis()
        }
    }
    return now.longValue
}

@Composable
fun CapsulesScreen() {
    val vm = kairosViewModel { CapsulesViewModel(it) }
    val list by vm.capsules.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val now = rememberNow()
    val all = list ?: return
    val ready = all.filter { it.entity.openedAt == null && it.entity.unlockAt <= now }
    val sealed = all.filter { it.entity.openedAt == null && it.entity.unlockAt > now }
    val opened = all.filter { it.entity.openedAt != null }.sortedByDescending { it.entity.openedAt }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = screenPadding()) {
        item {
            ScreenHeader("Time capsule", "Letters to\nfuture you", trailing = {
                GlassIconButton(Icons.Rounded.Add, "Write a new letter", { nav.open(Routes.CAPSULE_NEW) }, size = 56.dp)
            })
        }
        if (all.isEmpty()) {
            item {
                EmptyState(
                    null,
                    "Send a little light forward",
                    "Write to yourself a week, a month, or a year from now. Add photos, a voice note, anything. It stays sealed until the day arrives.",
                    action = "Write my first letter",
                    onAction = { nav.open(Routes.CAPSULE_NEW) },
                )
            }
            item { CapsuleIdeas() }
        } else {
            item {
                KButton("Write a letter", { nav.open(Routes.CAPSULE_NEW) }, icon = Icons.Rounded.Add, modifier = Modifier.fillMaxWidth().padding(bottom = Space.sm).enter(0))
            }
        }
        if (ready.isNotEmpty()) {
            item { SectionTitle("Arrived — ready to open") }
            items(ready, key = { it.entity.id }) { c -> ReadyCard(c) { nav.open(Routes.capsule(c.entity.id)) } }
        }
        if (sealed.isNotEmpty()) {
            item { SectionTitle("Sealed and travelling") }
            items(sealed, key = { it.entity.id }) { c -> SealedCard(c, now) { nav.open(Routes.capsule(c.entity.id)) } }
        }
        if (opened.isNotEmpty()) {
            item { SectionTitle("Opened letters") }
            items(opened.chunked(2), key = { row -> row.first().entity.id }) { row ->
                Row(Modifier.padding(bottom = Space.md), horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    row.forEach { c -> FolderCard(c, Modifier.weight(1f)) { nav.open(Routes.capsule(c.entity.id)) } }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CapsuleIdeas() {
    val colors = Kairos.colors
    GlassCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
            Eyebrow("Ideas for a first letter")
            listOf(
                "What does an ordinary Tuesday look like for you right now?",
                "A worry you hope future-you has forgotten about.",
                "Three songs you can't stop playing.",
                "A photo of your view, today.",
            ).forEach { Text("· $it", style = Kairos.type.bodyRegular, color = colors.inkSoft) }
        }
    }
}

@Composable
private fun ReadyCard(c: CapsuleCard, onClick: () -> Unit) {
    val colors = Kairos.colors
    val t = rememberInfiniteTransition(label = "ready")
    val glow by t.animateFloat(0.4f, 1f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow")
    val reduced = Kairos.reducedMotion
    GlassCard(Modifier.fillMaxWidth().padding(bottom = Space.md), blur = true, weight = GlassWeight.Frosted, shape = Radii.sheet, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 108.dp, height = 80.dp).graphicsLayer { val g = if (reduced) 1f else glow; scaleX = 0.96f + g * 0.04f; scaleY = scaleX }) {
                Envelope(c.entity.stamp, open = 0f, glow = if (reduced) 0.7f else glow, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(Space.lg))
            Column(Modifier.weight(1f)) {
                Eyebrow("From ${formatDate(c.entity.createdAt)}", color = colors.pond)
                Text(c.entity.title, style = Kairos.type.heading, color = colors.ink, maxLines = 2)
                Text("Tap to break the seal", style = Kairos.type.bodyRegular, color = colors.inkSoft)
            }
        }
    }
}

@Composable
private fun SealedCard(c: CapsuleCard, now: Long, onClick: () -> Unit) {
    val colors = Kairos.colors
    val total = (c.entity.unlockAt - c.entity.createdAt).coerceAtLeast(1)
    val progress = ((now - c.entity.createdAt).toFloat() / total).coerceIn(0f, 1f)
    GlassCard(Modifier.fillMaxWidth().padding(bottom = Space.md), onClick = onClick, contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Envelope(c.entity.stamp, open = 0f, glow = 0f, modifier = Modifier.size(width = 84.dp, height = 62.dp))
            Spacer(Modifier.width(Space.lg))
            Column(Modifier.weight(1f)) {
                Text(c.entity.title, style = Kairos.type.heading.copy(fontSize = 17.sp), color = colors.ink, maxLines = 1)
                Text("Opens in ${untilText(c.entity.unlockAt, now)} · ${formatDate(c.entity.unlockAt)}", style = Kairos.type.bodyRegular.copy(fontSize = 14.sp), color = colors.inkSoft)
                Spacer(Modifier.height(Space.sm))
                Canvas(Modifier.fillMaxWidth().height(6.dp)) {
                    drawRoundRect(colors.ink.copy(alpha = 0.08f), cornerRadius = CornerRadius(size.height))
                    drawRoundRect(Brush.horizontalGradient(listOf(colors.lilac, colors.pond)), size = Size(size.width * progress, size.height), cornerRadius = CornerRadius(size.height))
                }
                AttachmentSummary(c.attachments)
            }
        }
    }
}

@Composable
private fun AttachmentSummary(atts: List<AttachmentEntity>) {
    if (atts.isEmpty()) return
    val colors = Kairos.colors
    Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        val images = atts.count { it.kind == "Image" || it.kind == "Video" }
        val voice = atts.count { it.kind == "Audio" }
        val files = atts.count { it.kind == "File" }
        if (images > 0) Chiplet(Icons.Rounded.Image, "$images")
        if (voice > 0) Chiplet(Icons.Rounded.Mic, "$voice")
        if (files > 0) Chiplet(Icons.Rounded.AttachFile, "$files")
        Text("inside", style = Kairos.type.caption, color = colors.inkFaint)
    }
}

@Composable
private fun Chiplet(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Kairos.colors.inkSoft, modifier = Modifier.size(14.dp))
        Text(text, style = Kairos.type.caption, color = Kairos.colors.inkSoft)
    }
}

@Composable
private fun FolderCard(c: CapsuleCard, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = Kairos.colors
    Box(modifier.aspectRatio(1.15f).clickableNoRipple(onClick)) {
        Canvas(Modifier.fillMaxSize()) { drawFolder(StampTints[c.entity.stamp % StampTints.size], colors.isDark) }
        Column(Modifier.align(Alignment.BottomStart).padding(14.dp)) {
            Text(formatDate(c.entity.createdAt, "d MMM yyyy"), style = Kairos.type.caption, color = colors.inkSoft)
            Text(c.entity.title, style = Kairos.type.label, color = colors.ink, maxLines = 2, textAlign = TextAlign.Start)
        }
    }
}

/** A watercolor glass folder, like an archive drawer you can thumb through. */
private fun DrawScope.drawFolder(tint: Color, dark: Boolean) {
    val w = size.width
    val h = size.height
    val r = 18.dp.toPx()
    val tab = Path().apply {
        moveTo(0f, r)
        quadraticTo(0f, 0f, r, 0f)
        lineTo(w * 0.36f, 0f)
        quadraticTo(w * 0.42f, 0f, w * 0.46f, h * 0.08f)
        lineTo(w * 0.5f, h * 0.14f)
        lineTo(w - r, h * 0.14f)
        quadraticTo(w, h * 0.14f, w, h * 0.14f + r)
        lineTo(w, h - r)
        quadraticTo(w, h, w - r, h)
        lineTo(r, h)
        quadraticTo(0f, h, 0f, h - r)
        close()
    }
    drawPath(tab, Brush.verticalGradient(listOf(tint.copy(alpha = 0.32f), tint.copy(alpha = 0.18f))))
    // paper peeking out
    drawRoundRect(Color.White.copy(alpha = if (dark) 0.25f else 0.85f), topLeft = Offset(w * 0.1f, h * 0.2f), size = Size(w * 0.8f, h * 0.5f), cornerRadius = CornerRadius(10.dp.toPx()))
    // front pocket
    val front = Path().apply {
        moveTo(0f, h * 0.38f + r)
        quadraticTo(0f, h * 0.38f, r, h * 0.38f)
        lineTo(w - r, h * 0.34f)
        quadraticTo(w, h * 0.34f, w, h * 0.34f + r)
        lineTo(w, h - r)
        quadraticTo(w, h, w - r, h)
        lineTo(r, h)
        quadraticTo(0f, h, 0f, h - r)
        close()
    }
    drawPath(front, Brush.verticalGradient(listOf(Color.White.copy(alpha = if (dark) 0.22f else 0.75f), tint.copy(alpha = 0.3f))))
    drawPath(front, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.1f))), style = Stroke(1.dp.toPx()))
}

/**
 * The envelope: watercolor paper body, a folding flap ([open] 0..1), and a wax seal stamped
 * with one of six marks. [glow] adds the warm halo of a letter that has arrived.
 */
@Composable
fun Envelope(stamp: Int, open: Float, glow: Float, modifier: Modifier = Modifier, sealScale: Float = 1f, sealCrack: Float = 0f) {
    val colors = Kairos.colors
    Canvas(modifier) { drawEnvelope(stamp, open, glow, colors.isDark, sealScale, sealCrack) }
}

fun DrawScope.drawEnvelope(stamp: Int, open: Float, glow: Float, dark: Boolean, sealScale: Float = 1f, sealCrack: Float = 0f) {
    val w = size.width
    val h = size.height
    val r = h * 0.1f
    val paper = if (dark) Color(0xFFE9E3F5) else Color(0xFFFFFBF4)
    val tint = StampTints[stamp % StampTints.size]
    if (glow > 0f) {
        drawRoundRect(
            Brush.radialGradient(listOf(Color(0xFFFFD27A).copy(alpha = 0.55f * glow), Color.Transparent), center = Offset(w / 2, h / 2), radius = w * 0.75f),
            topLeft = Offset(-w * 0.2f, -h * 0.3f), size = Size(w * 1.4f, h * 1.6f),
        )
    }
    // body
    drawRoundRect(Brush.verticalGradient(listOf(paper, paper.darken(0.06f))), cornerRadius = CornerRadius(r))
    drawRoundRect(tint.copy(alpha = 0.12f), cornerRadius = CornerRadius(r))
    // side folds
    val folds = Path().apply {
        moveTo(0f, h); lineTo(w * 0.5f, h * 0.48f); lineTo(w, h)
    }
    drawPath(folds, tint.copy(alpha = 0.18f), style = Stroke(h * 0.012f))
    // flap: rotates up around the top edge as it opens
    val flapTipY = h * (0.56f - 1.1f * open)
    val flap = Path().apply {
        moveTo(r * 0.3f, 0f)
        lineTo(w / 2, flapTipY)
        lineTo(w - r * 0.3f, 0f)
        close()
    }
    drawPath(flap, Brush.verticalGradient(listOf(paper.darken(0.04f), paper.darken(0.1f)), startY = 0f, endY = h * 0.56f))
    drawPath(flap, tint.copy(alpha = 0.16f))
    drawPath(flap, Color.White.copy(alpha = 0.7f), style = Stroke(h * 0.012f))

    if (open < 0.3f) {
        val c = Offset(w / 2, h * 0.54f)
        val sr = h * 0.17f * sealScale
        if (sealCrack <= 0f) {
            drawWaxSeal(c, sr, tint, stamp)
        } else {
            // two halves drifting apart as the seal breaks
            val d = sealCrack * sr * 1.4f
            rotate(-18f * sealCrack, Offset(c.x - d, c.y)) { drawWaxSeal(Offset(c.x - d, c.y + d * 0.4f), sr * (1f - sealCrack * 0.3f), tint.copy(alpha = 1f - sealCrack), stamp) }
            rotate(22f * sealCrack, Offset(c.x + d, c.y)) { drawWaxSeal(Offset(c.x + d, c.y + d * 0.6f), sr * (1f - sealCrack * 0.3f), tint.copy(alpha = 1f - sealCrack), stamp) }
        }
    }
}

fun DrawScope.drawWaxSeal(c: Offset, r: Float, tint: Color, stamp: Int) {
    // wax spreads in an irregular blob
    val blob = Path()
    val n = 28
    for (i in 0..n) {
        val a = i / n.toFloat() * 2f * PI.toFloat()
        val k = 1f + 0.08f * sin(5 * a + stamp) + 0.05f * cos(3 * a)
        val p = Offset(c.x + cos(a) * r * k, c.y + sin(a) * r * k)
        if (i == 0) blob.moveTo(p.x, p.y) else blob.lineTo(p.x, p.y)
    }
    blob.close()
    drawPath(blob, Brush.radialGradient(listOf(tint.copy(alpha = 1f).lightenSeal(), tint, tint.darken(0.25f)), center = Offset(c.x - r * 0.3f, c.y - r * 0.3f), radius = r * 1.6f))
    drawCircle(tint.darken(0.18f), r * 0.66f, c, style = Stroke(r * 0.07f))
    val mark = Color.White.copy(alpha = 0.85f)
    when (stamp % 6) {
        0 -> {
            val p = Path().apply {
                moveTo(c.x, c.y + r * 0.32f)
                cubicTo(c.x - r * 0.5f, c.y, c.x - r * 0.3f, c.y - r * 0.4f, c.x, c.y - r * 0.15f)
                cubicTo(c.x + r * 0.3f, c.y - r * 0.4f, c.x + r * 0.5f, c.y, c.x, c.y + r * 0.32f)
                close()
            }
            drawPath(p, mark)
        }
        1 -> drawSparkle(c, r * 0.42f, mark)
        2 -> {
            drawOval(mark, topLeft = Offset(c.x - r * 0.18f, c.y - r * 0.38f), size = Size(r * 0.36f, r * 0.7f))
            drawLine(tint.darken(0.2f), Offset(c.x, c.y - r * 0.3f), Offset(c.x, c.y + r * 0.3f), strokeWidth = r * 0.05f, cap = StrokeCap.Round)
        }
        3 -> {
            drawCircle(mark, r * 0.34f, c)
            drawCircle(tint, r * 0.3f, Offset(c.x + r * 0.16f, c.y - r * 0.1f))
        }
        4 -> {
            drawCircle(mark, r * 0.18f, c)
            repeat(8) { i ->
                val a = i / 8f * 2f * PI.toFloat()
                drawLine(mark, Offset(c.x + cos(a) * r * 0.26f, c.y + sin(a) * r * 0.26f), Offset(c.x + cos(a) * r * 0.42f, c.y + sin(a) * r * 0.42f), strokeWidth = r * 0.07f, cap = StrokeCap.Round)
            }
        }
        else -> drawDrop(Offset(c.x, c.y + r * 0.05f), r * 0.22f, mark)
    }
    drawCircle(Color.White.copy(alpha = 0.45f), r * 0.12f, Offset(c.x - r * 0.45f, c.y - r * 0.45f))
}

private fun Color.lightenSeal() = Color(red + (1 - red) * 0.35f, green + (1 - green) * 0.35f, blue + (1 - blue) * 0.35f, alpha)
