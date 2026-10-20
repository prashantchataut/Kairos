package com.kairos.app.ui.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.core.design.GlassCard
import com.kairos.app.core.design.GlassIconButton
import com.kairos.app.core.design.InkButton
import com.kairos.app.core.design.KairosShape
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.KairosType
import com.kairos.app.ui.common.MoodRow
import androidx.compose.animation.togetherWith
import com.kairos.app.core.design.ProgressRing
import com.kairos.app.core.design.WatercolorBloom
import com.kairos.app.core.design.staggerIn
import com.kairos.app.core.design.settle
import com.kairos.app.ui.common.Gap
import com.kairos.app.ui.common.Page
import com.kairos.app.ui.common.StreakStrip
import com.kairos.app.domain.progress.Progress
import com.kairos.app.domain.vocab.SrsEngine

@Composable
fun TodayScreen(
    state: TodayUi,
    onOpenWord: (String) -> Unit,
    onStartReview: () -> Unit,
    onLove: (Boolean) -> Unit,
    onAnother: () -> Unit,
    onShare: () -> Unit,
    onMood: (String) -> Unit,
    onOpenYou: () -> Unit,
    onNewCapsule: () -> Unit,
    onOpenCapsule: () -> Unit,
) {
    val colors = KairosTheme.colors
    Page {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${state.weekday.uppercase()} · ${state.dateLabel.uppercase()}",
                        style = KairosType.eyebrow,
                        color = colors.inkFaint,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = buildAnnotatedString {
                            append(state.greeting)
                            append(", ")
                            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                append(state.profile.displayName)
                            }
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.ink,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "day ${state.dayNumber} · ${state.todayLine}",
                        style = KairosType.handSmall,
                        color = colors.inkSoft,
                    )
                }
                Gap(12.dp)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(colors.apricot, colors.blossom, colors.periwinkle)),
                        )
                        .clickable(onClick = onOpenYou),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.profile.displayName.take(1).uppercase(),
                        style = KairosType.hand,
                        color = Color.White,
                        fontSize = 22.sp,
                    )
                }
            }
        }

        item {
            QuoteCard(
                state = state,
                onLove = onLove,
                onAnother = onAnother,
                onShare = onShare,
                modifier = Modifier.staggerIn(0),
            )
        }

        item {
            WordOfDayCard(
                state = state,
                onOpen = { onOpenWord(state.word.id) },
                modifier = Modifier.staggerIn(1),
            )
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerIn(2),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = colors.apricot,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (state.streak == 0) "a fresh start" else "${state.streak}-day thread",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.ink,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = if (state.dueCount > 0) "${state.dueCount} due" else "all clear",
                            style = KairosType.handSmall,
                            color = colors.inkSoft,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    StreakStrip(week = state.week)
                    if (state.dueCount > 0) {
                        Spacer(Modifier.height(16.dp))
                        InkButton(
                            text = "Begin a ${state.profile.dailyPace.coerceIn(3, 12)}-card sitting",
                            onClick = onStartReview,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Rounded.AutoAwesome,
                        )
                    }
                }
            }
        }

        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .staggerIn(3),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            ) {
                Column {
                    Text(
                        text = "HOW IS TODAY SITTING?",
                        style = KairosType.eyebrow,
                        color = colors.inkFaint,
                    )
                    Spacer(Modifier.height(12.dp))
                    MoodRow(selected = state.mood, onSelect = { onMood(it.id) })
                    Spacer(Modifier.height(10.dp))
                    AnimatedContent(
                        targetState = state.mood,
                        label = "mood-line",
                    ) { mood ->
                        Text(
                            text = when (mood) {
                                "heavy" -> "noted. heavy days count as days."
                                "low" -> "noted. be a little softer with yourself."
                                "even" -> "noted. steady is underrated."
                                "good" -> "noted. let it stay a while."
                                "bright" -> "noted. go do something with it."
                                else -> "a small check-in, if you feel like it."
                            },
                            style = KairosType.handSmall,
                            color = colors.inkSoft,
                        )
                    }
                }
            }
        }

        item {
            CapsuleNudge(
                ready = state.readyCapsules,
                nextIn = state.nextCapsuleDays,
                onNew = onNewCapsule,
                onOpen = onOpenCapsule,
                modifier = Modifier.staggerIn(4),
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (state.streak >= 2) "you keep coming back. that's the whole trick." else "one small thing is enough today.",
                    style = KairosType.handSmall,
                    color = colors.inkFaint,
                )
            }
        }
    }
}

@Composable
private fun QuoteCard(
    state: TodayUi,
    onLove: (Boolean) -> Unit,
    onAnother: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KairosTheme.colors
    val heartScale by animateFloatAsState(
        targetValue = if (state.quoteLoved) 1.06f else 1f,
        animationSpec = tween(180),
        label = "heart",
    )
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        elevation = 20.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            WatercolorBloom(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.TopEnd),
                color = colors.blossom,
                seed = 5,
                alpha = 0.35f,
            )
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "A THOUGHT ON ${state.quote.theme.uppercase()}",
                        style = KairosType.eyebrow,
                        color = colors.inkFaint,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(18.dp))
                AnimatedContent(
                    targetState = state.quote.id,
                    transitionSpec = {
                        (androidx.compose.animation.fadeIn(tween(260)) +
                            androidx.compose.animation.slideInVertically(tween(320)) { it / 8 })
                            .togetherWith(androidx.compose.animation.fadeOut(tween(160)))
                    },
                    label = "quote",
                ) { _ ->
                    Column {
                        Text(
                            text = state.quote.text,
                            style = KairosType.quote,
                            color = colors.ink,
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "— ${state.quote.author}",
                            style = KairosType.hand,
                            color = colors.inkSoft,
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassIconButton(
                        icon = if (state.quoteLoved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        onClick = { onLove(!state.quoteLoved) },
                        size = 44.dp,
                        tint = if (state.quoteLoved) colors.blossom else colors.ink,
                        filled = false,
                        contentDescription = "Love this thought",
                    )
                    Spacer(Modifier.width(10.dp))
                    GlassIconButton(
                        icon = Icons.Rounded.Shuffle,
                        onClick = onAnother,
                        size = 44.dp,
                        contentDescription = "Another thought",
                    )
                    Spacer(Modifier.width(10.dp))
                    GlassIconButton(
                        icon = Icons.Rounded.IosShare,
                        onClick = onShare,
                        size = 44.dp,
                        contentDescription = "Make a poster",
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = state.quoteReason,
                        style = KairosType.handSmall,
                        color = colors.inkFaint,
                        modifier = Modifier.width(120.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                        lineHeight = 18.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun WordOfDayCard(state: TodayUi, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val colors = KairosTheme.colors
    val mastery = SrsEngine.mastery(state.wordMemory)
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TODAY'S WORD",
                    style = KairosType.eyebrow,
                    color = colors.inkFaint,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = state.word.term,
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.ink,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.word.ipa,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkFaint,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = state.word.partOfSpeech,
                        style = KairosType.handSmall,
                        color = colors.inkFaint,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = state.word.definition,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkSoft,
                    maxLines = 2,
                )
            }
            Gap(14.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ProgressRing(
                    progress = mastery,
                    size = 58.dp,
                    stroke = 5.dp,
                    brush = Brush.linearGradient(listOf(colors.mint, colors.periwinkle)),
                ) {
                    Text(
                        text = if (mastery > 0f) "${(mastery * 100).toInt()}" else "new",
                        style = KairosType.eyebrow.copy(fontSize = 9.sp),
                        color = colors.inkSoft,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (state.wordMemory.saved) "saved" else "learn",
                    style = KairosType.handSmall,
                    color = colors.inkSoft,
                )
            }
        }
    }
}

@Composable
private fun CapsuleNudge(
    ready: Int,
    nextIn: String?,
    onNew: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KairosTheme.colors
    val isReady = ready > 0
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(KairosShape.card)
            .background(
                if (isReady) {
                    Brush.linearGradient(listOf(Color(0xFF241C3B), Color(0xFF3A2650)))
                } else {
                    Brush.linearGradient(listOf(colors.glassFillStrong, colors.glassFill))
                },
            )
            .clickable(onClick = if (isReady) onOpen else onNew)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (isReady) Color(0x33FFD9A0) else colors.apricot.copy(alpha = 0.25f),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isReady) Icons.Rounded.AutoAwesome else Icons.Rounded.HourglassEmpty,
                    contentDescription = null,
                    tint = if (isReady) Color(0xFFFFD9A0) else colors.apricot,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isReady) "A letter is ready" else "Write to future you",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isReady) Color(0xFFFFF4E4) else colors.ink,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = when {
                        isReady -> "$ready ${if (ready == 1) "capsule has" else "capsules have"} come due. Open when you're ready."
                        nextIn != null -> "next one opens $nextIn"
                        else -> "a photo, a thought, a promise. seal it for later."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isReady) Color(0xFFCBB9E6) else colors.inkSoft,
                )
            }
            Icon(
                imageVector = Icons.Rounded.WbSunny,
                contentDescription = null,
                tint = if (isReady) Color(0xFFFFD9A0) else colors.inkFaint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
