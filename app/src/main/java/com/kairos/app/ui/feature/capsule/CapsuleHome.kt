package com.kairos.app.ui.feature.capsule

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.db.CapsuleWithAttachments
import com.kairos.app.engine.Mood
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.art.look
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.nav.shared
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.LightPalette
import com.kairos.app.ui.theme.Radius
import com.kairos.app.ui.theme.candy
import com.kairos.app.util.Fmt
import com.kairos.app.util.appContainer

/** A huge title whose last line dissolves into the night (r06, r12). */
@Composable
fun FadingTitle(first: String, second: String, modifier: Modifier = Modifier, size: Int = 56) {
    Column(modifier) {
        Text(first, style = K.type.shout.copy(fontSize = size.sp, lineHeight = (size * 0.98f).sp), color = Color.White)
        Text(
            second,
            style = K.type.shout.copy(
                fontSize = size.sp, lineHeight = (size * 0.98f).sp,
                brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.75f), Color.White.copy(alpha = 0.08f))),
            ),
        )
    }
}

@Composable
fun CapsuleHome(openCompose: () -> Unit, openLetter: (Long) -> Unit) {
    val c = appContainer()
    val all by c.capsules.observeAll().collectAsStateWithLifecycle(initialValue = null)
    val list = all ?: return
    val now = System.currentTimeMillis()
    val ready = list.filter { it.capsule.openedAt == null && it.capsule.opensAt <= now }
    val sealed = list.filter { it.capsule.openedAt == null && it.capsule.opensAt > now }.sortedBy { it.capsule.opensAt }
    val opened = list.filter { it.capsule.openedAt != null }.sortedByDescending { it.capsule.openedAt }

    LazyVerticalGrid(
        GridCells.Fixed(2), Modifier.fillMaxSize().nightSky(),
        contentPadding = PaddingValues(start = Gap.gutter, end = Gap.gutter, bottom = 130.dp),
        horizontalArrangement = Arrangement.spacedBy(Gap.lg), verticalArrangement = Arrangement.spacedBy(Gap.lg),
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(top = Gap.xl, bottom = Gap.md)) {
                FadingTitle("Letters to", "future you")
                Spacer(Modifier.height(Gap.md))
                Text(
                    if (list.isEmpty()) "Seal a letter, a photo or a voice note. I'll keep it closed until the day you choose."
                    else "${sealed.size} sealed, ${opened.size} opened. Everything stays on this phone.",
                    style = K.type.body, color = Color.White.copy(alpha = 0.75f),
                )
                Spacer(Modifier.height(Gap.xl))
                PrimaryButton("Write a letter", openCompose, icon = Icons.Edit, onSky = true)
            }
        }
        if (ready.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) { Label("Ready to open") }
            itemsIndexed(ready, key = { _, it -> it.capsule.id }, span = { _, _ -> GridItemSpan(2) }) { _, it -> ReadyCard(it) { openLetter(it.capsule.id) } }
        }
        if (sealed.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) { Label("Still sealed") }
            itemsIndexed(sealed, key = { _, it -> it.capsule.id }) { _, it -> FolderTile(it) { openLetter(it.capsule.id) } }
        }
        if (opened.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) { Label("Opened") }
            itemsIndexed(opened, key = { _, it -> it.capsule.id }, span = { _, _ -> GridItemSpan(2) }) { _, it -> OpenedRow(it) { openLetter(it.capsule.id) } }
        }
        if (list.isEmpty()) item(span = { GridItemSpan(2) }) { EmptyCapsule() }
    }
}

@Composable
private fun Label(t: String) = Text(t, style = K.type.title, color = Color.White, modifier = Modifier.padding(top = Gap.lg))

/** The letter's paper colour: the candy behind the envelope tint the person chose. */
private fun CapsuleWithAttachments.paper(): Color = LightPalette.candy(tintOf(capsule.tint))

@Composable
private fun ReadyCard(item: CapsuleWithAttachments, onClick: () -> Unit) {
    val c = item.capsule
    val calm = K.calm
    val bob = if (!calm) rememberInfiniteTransition(label = "bob").animateFloat(-4f, 4f, infiniteRepeatable(tween(1700), RepeatMode.Reverse), label = "b") else null
    val glow = if (!calm) rememberInfiniteTransition(label = "glow").animateFloat(0.6f, 1f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "g") else null
    Row(
        Modifier
            .fillMaxWidth()
            .clip(Radius.card)
            .background(Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.06f))))
            .border(1.dp, Color.White.copy(alpha = 0.3f), Radius.card)
            .pressable(scale = 0.98f, onClick = onClick)
            .padding(Gap.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassFolder(
            Candy.Sun, Modifier.size(width = 110.dp, height = 92.dp).graphicsLayer { translationY = (bob?.value ?: 0f) * density }.shared("capsule-${c.id}"),
            rise = 0.15f, glow = glow?.value ?: 1f, sticker = Mood.of(c.moodAtSeal)?.look(), papers = 2,
        )
        Spacer(Modifier.width(Gap.lg))
        Column(Modifier.weight(1f)) {
            Text(c.title.ifBlank { "A letter from past you" }, style = K.type.title, color = Color.White)
            Spacer(Modifier.height(Gap.xs))
            Text("Written ${Fmt.ago(c.createdAt)}. It's time.", style = K.type.label, color = Color.White.copy(alpha = 0.72f))
            Spacer(Modifier.height(Gap.md))
            Text("Open it", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Candy.Navy, modifier = Modifier.clip(CircleShape).background(Candy.Sun).padding(horizontal = 14.dp, vertical = 7.dp))
        }
    }
}

/** A sealed letter as a glass folder (r11), its date under it in large type. */
@Composable
private fun FolderTile(item: CapsuleWithAttachments, onClick: () -> Unit) {
    val c = item.capsule
    Column(Modifier.fillMaxWidth().pressable(scale = 0.96f, onClick = onClick)) {
        GlassFolder(
            item.paper(), Modifier.fillMaxWidth().aspectRatio(1.12f).shared("capsule-${c.id}"),
            sticker = Mood.of(c.moodAtSeal)?.look(), papers = (1 + item.attachments.size).coerceAtMost(3),
        )
        Spacer(Modifier.height(Gap.sm))
        Text(Fmt.shortDate(c.opensAt), style = K.type.display.copy(fontSize = 24.sp), color = Color.White)
        Text(
            Fmt.until(c.opensAt).replaceFirstChar { it.uppercase() } + if (item.attachments.isNotEmpty()) " · ${item.attachments.size} inside" else "",
            style = K.type.caption, color = Color.White.copy(alpha = 0.65f),
        )
    }
}

@Composable
private fun OpenedRow(item: CapsuleWithAttachments, onClick: () -> Unit) {
    val c = item.capsule
    Row(
        Modifier.fillMaxWidth().clip(Radius.tile).background(Color.White.copy(alpha = 0.08f)).pressable(scale = 0.98f, onClick = onClick).padding(Gap.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassFolder(item.paper(), Modifier.size(width = 56.dp, height = 46.dp), rise = 0.35f, sealed = false, papers = 1)
        Spacer(Modifier.width(Gap.lg))
        Column(Modifier.weight(1f)) {
            Text(c.title.ifBlank { "Letter from ${Fmt.shortDate(c.createdAt)}" }, style = K.type.label.copy(fontSize = 16.sp), color = Color.White, maxLines = 1)
            Text("Opened ${Fmt.ago(c.openedAt ?: 0)}", style = K.type.caption, color = Color.White.copy(alpha = 0.6f))
        }
        Icon(Icons.Chevron, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun EmptyCapsule() {
    Column(Modifier.fillMaxWidth().padding(top = Gap.xl), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(width = 220.dp, height = 190.dp), contentAlignment = Alignment.BottomCenter) {
            Kai(KaiLooks.Curator.copy(gaze = Offset0), size = 96.dp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.dp))
            GlassFolder(Candy.Lilac, Modifier.size(width = 220.dp, height = 150.dp), sealed = false, papers = 0)
        }
        Spacer(Modifier.height(Gap.lg))
        Text("Why write one?", style = K.type.title, color = Color.White)
        Spacer(Modifier.height(Gap.sm))
        Text(
            "Future you forgets what today felt like: the worries that passed, the small joys, who you were becoming. A letter keeps it.",
            style = TextStyle.Default.merge(K.type.body), color = Color.White.copy(alpha = 0.75f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

private val Offset0 = androidx.compose.ui.geometry.Offset(0f, 0.6f)
