package com.kairos.app.ui.words

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.core.design.GlassCard
import com.kairos.app.core.design.GlassIconButton
import com.kairos.app.core.design.GlassPill
import com.kairos.app.core.design.InkButton
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.KairosType
import com.kairos.app.core.design.ProgressRing
import com.kairos.app.core.design.staggerIn
import com.kairos.app.core.design.EmptyState
import com.kairos.app.ui.common.Page
import com.kairos.app.ui.common.PageTitle
import com.kairos.app.ui.common.QuietAction

@Composable
fun WordsScreen(
    state: WordsUi,
    onQuery: (String) -> Unit,
    onFilter: (WordFilter) -> Unit,
    onStart: () -> Unit,
    onOpen: (String) -> Unit,
    onToggleSave: (String) -> Unit,
) {
    val colors = KairosTheme.colors
    Page {
        item {
            PageTitle(
                eyebrow = "the garden",
                title = "Words",
                subtitle = "${state.metCount} of ${state.total} met · ${state.learnedCount} yours",
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KairosShapePill)
                    .background(colors.glassFillStrong)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = colors.inkFaint,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (state.query.isEmpty()) {
                        Text(
                            text = "search a word or a meaning",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.inkFaint,
                        )
                    }
                    BasicTextField(
                        value = state.query,
                        onValueChange = onQuery,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = colors.ink,
                            fontSize = 15.sp,
                            fontFamily = com.kairos.app.core.design.KairosSans,
                        ),
                        cursorBrush = SolidColor(colors.ink),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WordFilter.entries.forEach { filter ->
                    val count = when (filter) {
                        WordFilter.All -> state.total
                        WordFilter.New -> state.total - state.metCount
                        WordFilter.Saved -> state.savedCount
                        WordFilter.Due -> state.dueCount
                    }
                    GlassPill(
                        selected = state.filter == filter,
                        onClick = { onFilter(filter) },
                    ) {
                        Text(
                            text = "${filter.label} ${if (count > 0) count else ""}".trim(),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.filter == filter) colors.paper else colors.inkSoft,
                        )
                    }
                }
            }
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerIn(0),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            ) {
                Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.dueCount > 0) "TODAY'S SITTING" else "A GENTLE SITTING",
                            style = KairosType.eyebrow,
                            color = colors.inkFaint,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = when {
                                state.dueCount > 0 -> "${state.dueCount} ${if (state.dueCount == 1) "word is" else "words are"} ready"
                                state.metCount == 0 -> "Meet your first word"
                                else -> "Nothing is due. Meet something new?"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.ink,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "about ${state.pace * 2} minutes · ${state.pace} cards",
                            style = KairosType.handSmall,
                            color = colors.inkSoft,
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    ProgressRing(
                        progress = if (state.total == 0) 0f else state.metCount / state.total.toFloat(),
                        size = 54.dp,
                        stroke = 5.dp,
                        brush = Brush.linearGradient(listOf(colors.mint, colors.periwinkle)),
                    ) {
                        Text(
                            text = "${state.metCount}",
                            style = KairosType.eyebrow.copy(fontSize = 10.sp),
                            color = colors.inkSoft,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                InkButton(
                    text = "Begin the sitting",
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Rounded.ArrowForward,
                )
                }
            }
        }

        if (state.items.isEmpty()) {
            item {
                EmptyState(
                    title = "No words here yet",
                    body = "Try a different search, or clear the filter. The garden is bigger than it looks.",
                    mood = com.kairos.app.core.design.KaiMood.Curious,
                )
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${state.items.size} words",
                        style = KairosType.eyebrow,
                        color = colors.inkFaint,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "due first",
                        style = KairosType.handSmall,
                        color = colors.inkFaint,
                    )
                }
            }
            items(state.items.size) { index ->
                val item = state.items[index]
                WordRow(
                    item = item,
                    onClick = { onOpen(item.word.id) },
                    onSave = { onToggleSave(item.word.id) },
                    modifier = Modifier.staggerIn(index.coerceAtMost(6), baseDelay = 40),
                )
            }
        }
    }
}

@Composable
private fun WordRow(
    item: WordItem,
    onClick: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KairosTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(
                    when {
                        item.due -> colors.apricot
                        item.memory.seen > 0 -> colors.mint.copy(alpha = 0.7f)
                        else -> colors.hairline
                    },
                ),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.word.term,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.word.ipa,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkFaint,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.word.definition,
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        if (item.memory.seen > 0) {
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.hairline.copy(alpha = 0.6f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(item.mastery.coerceIn(0.04f, 1f))
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(colors.mint),
                )
            }
            Spacer(Modifier.width(10.dp))
        }
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .clickable(onClick = onSave),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (item.memory.saved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = if (item.memory.saved) "Saved" else "Save",
                tint = if (item.memory.saved) colors.blossom else colors.inkFaint,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

private val KairosShapePill = RoundedCornerShape(100.dp)
