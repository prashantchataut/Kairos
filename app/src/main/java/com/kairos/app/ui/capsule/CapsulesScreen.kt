package com.kairos.app.ui.capsule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Markunread
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.core.design.GlassCard
import com.kairos.app.core.design.InkButton
import com.kairos.app.core.design.KairosMark
import com.kairos.app.core.design.KairosShape
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.KairosType
import com.kairos.app.core.design.WatercolorBloom
import com.kairos.app.core.design.staggerIn
import com.kairos.app.core.util.KairosDates
import com.kairos.app.data.Capsule
import com.kairos.app.domain.capsule.CapsuleEngine
import com.kairos.app.core.design.EmptyState
import com.kairos.app.ui.common.Page
import com.kairos.app.ui.common.PageTitle
import java.time.Instant

@Composable
fun CapsulesScreen(
    state: CapsulesUi,
    onCompose: () -> Unit,
    onOpen: (Capsule) -> Unit,
) {
    val colors = KairosTheme.colors
    val vault = state.vault
    Page {
        item {
            PageTitle(
                eyebrow = "letters to later",
                title = "Capsules",
                subtitle = when {
                    vault.all.isEmpty() -> "nothing sealed yet — that's allowed"
                    vault.ready.isNotEmpty() -> "${vault.ready.size} waiting for you"
                    else -> "${vault.sealed.size} sealed · ${vault.opened.size} opened"
                },
                trailing = {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(colors.ink)
                            .clickable(onClick = onCompose),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "New capsule", tint = colors.paper)
                    }
                },
            )
        }

        if (vault.all.isEmpty()) {
            item {
                EmptyState(
                    title = "Write to the person you'll be",
                    body = "A photo, a sentence, a promise. Seal it with a date, and Kairos will keep it until then.",
                    mood = com.kairos.app.core.design.KaiMood.Curious,
                ) {
                    InkButton(text = "Seal your first capsule", onClick = onCompose)
                }
            }
        } else {
            if (vault.ready.isNotEmpty()) {
                item {
                    ReadyCard(capsule = vault.ready.first(), now = state.now, onOpen = { onOpen(vault.ready.first()) })
                }
            }

            val next = CapsuleEngine.nextUnlock(vault.sealed, state.now)
            if (next != null) {
                item {
                    NextUnlockCard(capsule = next, now = state.now)
                }
            }

            if (vault.sealed.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("SEALED", style = KairosType.eyebrow, color = colors.inkFaint)
                        Spacer(Modifier.weight(1f))
                        Text("still waiting", style = KairosType.handSmall, color = colors.inkFaint)
                    }
                }
                items((vault.sealed.size + 1) / 2) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        vault.sealed.getOrNull(row * 2)?.let { capsule ->
                            CapsuleCard(
                                capsule = capsule,
                                now = state.now,
                                onClick = { onOpen(capsule) },
                                modifier = Modifier.weight(1f).staggerIn(row.coerceAtMost(5), 50),
                            )
                        } ?: Spacer(Modifier.weight(1f))
                        vault.sealed.getOrNull(row * 2 + 1)?.let { capsule ->
                            CapsuleCard(
                                capsule = capsule,
                                now = state.now,
                                onClick = { onOpen(capsule) },
                                modifier = Modifier.weight(1f).staggerIn(row.coerceAtMost(5), 50),
                            )
                        } ?: Spacer(Modifier.weight(1f))
                    }
                }
            }

            if (vault.opened.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("OPENED", style = KairosType.eyebrow, color = colors.inkFaint)
                        Spacer(Modifier.weight(1f))
                        Text("read again any time", style = KairosType.handSmall, color = colors.inkFaint)
                    }
                }
                items((vault.opened.size + 1) / 2) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        vault.opened.getOrNull(row * 2)?.let { capsule ->
                            CapsuleCard(
                                capsule = capsule,
                                now = state.now,
                                onClick = { onOpen(capsule) },
                                modifier = Modifier.weight(1f),
                            )
                        } ?: Spacer(Modifier.weight(1f))
                        vault.opened.getOrNull(row * 2 + 1)?.let { capsule ->
                            CapsuleCard(
                                capsule = capsule,
                                now = state.now,
                                onClick = { onOpen(capsule) },
                                modifier = Modifier.weight(1f),
                            )
                        } ?: Spacer(Modifier.weight(1f))
                    }
                }
            }

            item {
                InkButton(
                    text = "Seal something new",
                    onClick = onCompose,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.AutoAwesome,
                )
            }
        }
    }
}

@Composable
private fun ReadyCard(capsule: Capsule, now: Long, onOpen: () -> Unit) {
    val colors = KairosTheme.colors
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        elevation = 18.dp,
    ) {
        Box {
            WatercolorBloom(
                modifier = Modifier.size(240.dp).align(Alignment.TopEnd),
                color = colors.butter,
                seed = 9,
                alpha = 0.45f,
            )
            Column(modifier = Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.apricot.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Markunread,
                            contentDescription = null,
                            tint = colors.apricot,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "READY TO OPEN",
                            style = KairosType.eyebrow,
                            color = colors.inkFaint,
                        )
                        Text(
                            text = "written ${KairosDates.monthDayYear(Instant.ofEpochMilli(capsule.createdAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate())}",
                            style = KairosType.handSmall,
                            color = colors.inkSoft,
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = capsule.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(16.dp))
                InkButton(text = "Break the seal", onClick = onOpen, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun NextUnlockCard(capsule: Capsule, now: Long) {
    val colors = KairosTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KairosShape.card)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1B1630), Color(0xFF34224A)),
                ),
            )
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0x22FFD9A0)),
                contentAlignment = Alignment.Center,
            ) {
                KairosMark(modifier = Modifier.size(26.dp), tint = Color(0xFFFFE1B5))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NEXT ONE OPENS IN",
                    style = KairosType.eyebrow,
                    color = Color(0xFFB9A7D6),
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = KairosDates.coarseDistance(
                        Instant.ofEpochMilli(now),
                        Instant.ofEpochMilli(capsule.unlockAt),
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFFFF3E2),
                )
            }
            Text(
                text = KairosDates.monthDay(
                    Instant.ofEpochMilli(capsule.unlockAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                ),
                style = KairosType.handSmall,
                color = Color(0xFFB9A7D6),
            )
        }
    }
}

@Composable
private fun CapsuleCard(
    capsule: Capsule,
    now: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KairosTheme.colors
    val opened = capsule.openedAt != null
    val locked = !opened && capsule.unlockAt > now
    val bg = if (locked) {
        Brush.linearGradient(listOf(Color(0xFF241D3B), Color(0xFF3A2A52)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFDF7EC), Color(0xFFF4E8D8)))
    }
    val ink = if (locked) Color(0xFFFFF2E2) else colors.ink
    val soft = if (locked) Color(0xFFB4A2CF) else colors.inkSoft

    Box(
        modifier = modifier
            .aspectRatio(0.82f)
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .clickable(onClick = onClick),
    ) {
        if (locked) {
            // A scatter of quiet stars.
            androidx.compose.foundation.Canvas(Modifier.matchParentSize()) {
                val random = kotlin.random.Random(capsule.id.hashCode())
                repeat(26) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f + random.nextFloat() * 0.18f),
                        radius = 0.8f + random.nextFloat() * 1.4f,
                        center = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height * 0.8f),
                    )
                }
            }
        } else {
            WatercolorBloom(
                modifier = Modifier.size(160.dp).align(Alignment.BottomEnd),
                color = colors.apricot,
                seed = capsule.id.hashCode(),
                alpha = 0.30f,
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = KairosDates.monthDayYear(
                        Instant.ofEpochMilli(capsule.unlockAt).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    ),
                    style = KairosType.eyebrow.copy(fontSize = 9.sp),
                    color = soft,
                    modifier = Modifier.weight(1f),
                )
                if (locked) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = soft,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (locked) Color(0x33FFB98C) else colors.apricot.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                KairosMark(
                    modifier = Modifier.size(17.dp),
                    tint = if (locked) Color(0xFFFFD9B0) else colors.apricot,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = capsule.title.ifBlank { "an untitled letter" },
                style = MaterialTheme.typography.titleSmall,
                color = ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = when {
                    opened -> "opened ${KairosDates.relativeDay(
                        Instant.ofEpochMilli(capsule.openedAt!!).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                        Instant.ofEpochMilli(now).atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                    )}"
                    locked -> "opens in ${KairosDates.coarseDistance(Instant.ofEpochMilli(now), Instant.ofEpochMilli(capsule.unlockAt))}"
                    else -> "ready"
                },
                style = KairosType.handSmall.copy(fontSize = 15.sp),
                color = soft,
            )
        }
    }
}
