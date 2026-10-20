package com.kairos.app.ui.screens.capsule

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.db.CapsuleWithAttachments
import com.kairos.app.ui.components.EmptyState
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.squishClickable
import com.kairos.app.ui.glass.GlassPanel
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.navigation.Scene
import com.kairos.app.ui.navigation.sharedElement
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.theme.Wash
import com.kairos.app.util.Fmt
import com.kairos.app.util.appContainer
import kotlin.math.PI

val FolderShape = GenericShape { size, _ -> addPath(folderPath(size.width, size.height, size.minDimension * 0.12f)) }

fun washOf(name: String) = runCatching { Wash.valueOf(name) }.getOrDefault(Wash.Lilac)

@Composable
fun CapsuleHomeScreen(openCompose: () -> Unit, openCapsule: (Long) -> Unit) {
    val c = appContainer()
    val all by c.capsules.observeAll().collectAsStateWithLifecycle(initialValue = null)
    Scene(Wash.Lilac, Wash.Rose, Wash.Cerulean, Wash.Peach, Wash.Lilac)
    val now = System.currentTimeMillis()
    val list = all ?: return
    val ready = list.filter { it.capsule.openedAt == null && it.capsule.opensAt <= now }
    val sealed = list.filter { it.capsule.openedAt == null && it.capsule.opensAt > now }
    val opened = list.filter { it.capsule.openedAt != null }.sortedByDescending { it.capsule.openedAt }

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = Space.gutter, end = Space.gutter, bottom = 210.dp),
            horizontalArrangement = Arrangement.spacedBy(Space.md),
            verticalArrangement = Arrangement.spacedBy(Space.md),
        ) {
            item(span = { GridItemSpan(2) }) {
                Column(Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(top = Space.xl, bottom = Space.sm)) {
                    Text("Letters to", style = Kairos.type.hero.copy(fontSize = Kairos.type.display.fontSize * 1.3f, lineHeight = Kairos.type.display.lineHeight * 1.2f), color = Kairos.colors.ink)
                    Text("future you", style = Kairos.type.hero.copy(fontSize = Kairos.type.display.fontSize * 1.3f, lineHeight = Kairos.type.display.lineHeight * 1.2f), color = Kairos.colors.inkFaint)
                    Spacer(Modifier.height(Space.sm))
                    Text(
                        when {
                            list.isEmpty() -> "Seal a letter, a photo, a voice, a hope. Kairos keeps it closed until the day you choose."
                            else -> "${sealed.size} sealed, ${opened.size} opened. Nothing here leaves your phone."
                        },
                        style = Kairos.type.body, color = Kairos.colors.inkSoft,
                    )
                }
            }
            if (list.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    EmptyState(
                        "Your first letter is waiting",
                        "Tell future you what today felt like. You'll be surprised how much you forget.",
                    )
                }
            }
            if (ready.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) { Label("Ready to open") }
                items(ready, key = { it.capsule.id }, span = { GridItemSpan(2) }) { ReadyCard(it) { openCapsule(it.capsule.id) } }
            }
            if (sealed.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) { Label("Still sealed") }
                items(sealed, key = { it.capsule.id }) { FolderCard(it) { openCapsule(it.capsule.id) } }
            }
            if (opened.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) { Label("Opened") }
                items(opened, key = { it.capsule.id }, span = { GridItemSpan(2) }) { OpenedRow(it) { openCapsule(it.capsule.id) } }
            }
        }
        PrimaryButton(
            "Write a letter", openCompose,
            icon = KairosIcons.Edit,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 96.dp),
        )
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = Kairos.type.title, color = Kairos.colors.ink, modifier = Modifier.padding(top = Space.lg))
}

@Composable
private fun ReadyCard(item: CapsuleWithAttachments, onClick: () -> Unit) {
    val c = item.capsule
    val wash = washOf(c.wash).color(Kairos.colors.isDark)
    val t by rememberInfiniteTransition(label = "orbit").animateFloat(
        0f, 2f * PI.toFloat(), infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart), label = "o",
    )
    val float by rememberInfiniteTransition(label = "float").animateFloat(
        -4f, 4f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "f",
    )
    GlassPanel(Modifier.fillMaxWidth().squishClickable(onClick = onClick)) {
        Row(Modifier.padding(Space.xl), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 120.dp, height = 90.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) { orbitSparkles(t, androidx.compose.ui.graphics.Color(0xFFFFE59C)) }
                Envelope(
                    wash, glow = 1f,
                    modifier = Modifier
                        .size(width = 96.dp, height = 66.dp)
                        .graphicsLayer { translationY = float }
                        .sharedElement("capsule-${c.id}"),
                )
            }
            Spacer(Modifier.width(Space.lg))
            Column(Modifier.weight(1f)) {
                Text(c.title.ifBlank { "A letter from past you" }, style = Kairos.type.title, color = Kairos.colors.ink)
                Spacer(Modifier.height(Space.xs))
                Text("Written ${Fmt.ago(c.createdAt)}. Tap to break the seal.", style = Kairos.type.label, color = Kairos.colors.inkSoft)
            }
        }
    }
}

@Composable
private fun FolderCard(item: CapsuleWithAttachments, onClick: () -> Unit) {
    val c = item.capsule
    val palette = Kairos.colors
    val wash = washOf(c.wash).color(palette.isDark)
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1.05f)
            .squishClickable(onClick = onClick)
            .sharedElement("capsule-${c.id}")
            .liquidGlass(FolderShape, blur = false, tint = wash.copy(alpha = if (palette.isDark) 0.35f else 0.42f), elevation = 10.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(Space.lg), verticalArrangement = Arrangement.Bottom) {
            Icon(KairosIcons.Lock, null, tint = palette.ink.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            Spacer(Modifier.weight(1f))
            Text(Fmt.shortDate(c.opensAt), style = Kairos.type.title, color = palette.ink)
            Text(
                Fmt.until(c.opensAt) + if (item.attachments.isNotEmpty()) ", ${item.attachments.size} inside" else "",
                style = Kairos.type.caption, color = palette.inkSoft, textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun OpenedRow(item: CapsuleWithAttachments, onClick: () -> Unit) {
    val c = item.capsule
    val palette = Kairos.colors
    GlassPanel(Modifier.fillMaxWidth().squishClickable(pressedScale = 0.98f, onClick = onClick), blur = false) {
        Row(Modifier.padding(Space.lg), verticalAlignment = Alignment.CenterVertically) {
            Envelope(washOf(c.wash).color(palette.isDark), flap = 1f, modifier = Modifier.size(width = 52.dp, height = 36.dp))
            Spacer(Modifier.width(Space.lg))
            Column(Modifier.weight(1f)) {
                Text(c.title.ifBlank { "A letter from ${Fmt.date(c.createdAt)}" }, style = Kairos.type.label, color = palette.ink, maxLines = 1)
                Text("Written ${Fmt.shortDate(c.createdAt)}, opened ${Fmt.ago(c.openedAt ?: 0)}", style = Kairos.type.caption, color = palette.inkSoft)
            }
            Icon(KairosIcons.Chevron, null, tint = palette.inkFaint)
        }
    }
}
