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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.db.CapsuleWithAttachments
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.components.SkyTail
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.nav.shared
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.tint
import com.kairos.app.ui.theme.tintInk
import com.kairos.app.util.Fmt
import com.kairos.app.util.appContainer

@Composable
fun CapsuleHome(openCompose: () -> Unit, openLetter: (Long) -> Unit) {
    val c = appContainer()
    val all by c.capsules.observeAll().collectAsStateWithLifecycle(initialValue = null)
    val list = all ?: return
    val p = K.colors
    val now = System.currentTimeMillis()
    val ready = list.filter { it.capsule.openedAt == null && it.capsule.opensAt <= now }
    val sealed = list.filter { it.capsule.openedAt == null && it.capsule.opensAt > now }
    val opened = list.filter { it.capsule.openedAt != null }.sortedByDescending { it.capsule.openedAt }

    LazyVerticalGrid(
        GridCells.Fixed(2), Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(Gap.md), verticalArrangement = Arrangement.spacedBy(Gap.md),
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(Modifier.fillMaxWidth().sky().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = Gap.gutter).padding(top = Gap.xl, bottom = SkyTail)) {
                Text("Letters to", style = K.type.serifLarge.copy(fontSize = 48.sp, lineHeight = 50.sp), color = p.onSky)
                Text("future you", style = K.type.serifLarge.copy(fontSize = 48.sp, lineHeight = 50.sp), color = p.onSkySoft)
                Spacer(Modifier.height(Gap.md))
                Text(
                    if (list.isEmpty()) "Seal a letter, a photo or a voice note. Kairos keeps it closed until the day you choose."
                    else "${sealed.size} sealed, ${opened.size} opened. Everything stays on this phone.",
                    style = K.type.body, color = p.onSkySoft,
                )
                Spacer(Modifier.height(Gap.xl))
                PrimaryButton("Write a letter", openCompose, icon = Icons.Edit, dark = true)
            }
        }
        if (ready.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) { Label("Ready to open") }
            items(ready, key = { it.capsule.id }, span = { GridItemSpan(2) }) { ReadyCard(it) { openLetter(it.capsule.id) } }
        }
        if (sealed.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) { Label("Still sealed") }
            items(sealed, key = { it.capsule.id }) { Folder(it, Modifier.padding(start = if (sealed.indexOf(it) % 2 == 0) Gap.gutter else 0.dp, end = if (sealed.indexOf(it) % 2 == 1) Gap.gutter else 0.dp)) { openLetter(it.capsule.id) } }
        }
        if (opened.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) { Label("Opened") }
            items(opened, key = { it.capsule.id }, span = { GridItemSpan(2) }) { OpenedRow(it) { openLetter(it.capsule.id) } }
        }
        if (list.isEmpty()) item(span = { GridItemSpan(2) }) {
            Column(Modifier.padding(Gap.gutter)) {
                Card(Modifier.fillMaxWidth()) {
                    Text("Why write one?", style = K.type.title, color = K.colors.ink)
                    Spacer(Modifier.height(Gap.sm))
                    Text("Future you forgets what today felt like: the worries that passed, the small joys, who you were becoming. A letter keeps it.", style = K.type.body, color = K.colors.inkSoft)
                }
            }
        }
    }
}

@Composable
private fun Label(t: String) = Text(t, style = K.type.title, color = K.colors.ink, modifier = Modifier.padding(start = Gap.gutter, top = Gap.lg))

@Composable
private fun ReadyCard(item: CapsuleWithAttachments, onClick: () -> Unit) {
    val p = K.colors
    val c = item.capsule
    val calm = K.calm
    val bob by rememberInfiniteTransition(label = "bob").animateFloat(-3f, 3f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "b")
    Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Envelope(p.tint(tintOf(c.tint)), glow = 1f, modifier = Modifier.size(width = 96.dp, height = 66.dp).graphicsLayer { translationY = if (calm) 0f else bob }.shared("capsule-${c.id}"))
            Spacer(Modifier.width(Gap.lg))
            Column(Modifier.weight(1f)) {
                Text(c.title.ifBlank { "A letter from past you" }, style = K.type.title, color = p.ink)
                Text("Written ${Fmt.ago(c.createdAt)}. Tap to open.", style = K.type.caption, color = p.inkSoft)
            }
        }
    }
}


@Composable
private fun Folder(item: CapsuleWithAttachments, modifier: Modifier, onClick: () -> Unit) {
    val p = K.colors
    val c = item.capsule
    val t = tintOf(c.tint)
    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
            .pressable(onClick = onClick)
            .shared("capsule-${c.id}")
            .shadow(14.dp, FolderShape, ambientColor = p.shadow, spotColor = p.shadow)
            .clip(FolderShape)
            .background(Brush.verticalGradient(listOf(p.tint(t), p.surface)))
            .border(1.dp, p.line, FolderShape)
    ) {
        Column(Modifier.fillMaxSize().padding(Gap.lg), verticalArrangement = Arrangement.Bottom) {
            Icon(Icons.Lock, null, tint = p.tintInk(t), modifier = Modifier.size(18.dp))
            Spacer(Modifier.weight(1f))
            Text(Fmt.shortDate(c.opensAt), style = K.type.serifLarge.copy(fontSize = 28.sp, lineHeight = 30.sp), color = p.ink)
            Text(Fmt.until(c.opensAt) + if (item.attachments.isNotEmpty()) ", ${item.attachments.size} inside" else "", style = K.type.caption, color = p.inkSoft)
        }
    }
}

@Composable
private fun OpenedRow(item: CapsuleWithAttachments, onClick: () -> Unit) {
    val p = K.colors
    val c = item.capsule
    Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter), padding = Gap.lg, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Envelope(p.tint(tintOf(c.tint)), flap = 1f, modifier = Modifier.size(width = 48.dp, height = 34.dp))
            Spacer(Modifier.width(Gap.lg))
            Column(Modifier.weight(1f)) {
                Text(c.title.ifBlank { "Letter from ${Fmt.shortDate(c.createdAt)}" }, style = K.type.label, color = p.ink, maxLines = 1)
                Text("Opened ${Fmt.ago(c.openedAt ?: 0)}", style = K.type.caption, color = p.inkSoft)
            }
            Icon(Icons.Chevron, null, tint = p.inkFaint, modifier = Modifier.size(18.dp))
        }
    }
}
