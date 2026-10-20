package com.kairos.app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.core.design.GlassIconButton
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.KairosType
import com.kairos.app.core.design.pressScale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.remember

/** Standard page body: generous margins, room under the floating nav. */
@Composable
fun Page(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    bottomPadding: Dp = 132.dp,
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = state,
        contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 8.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        content = content,
    )
}

/** A page header: eyebrow, big serif line, optional trailing control. */
@Composable
fun PageTitle(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = eyebrow.uppercase(),
                style = KairosType.eyebrow,
                color = KairosTheme.colors.inkFaint,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall,
                color = KairosTheme.colors.ink,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = KairosType.handSmall,
                    color = KairosTheme.colors.inkSoft,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassIconButton(
        icon = Icons.Rounded.Close,
        onClick = onClick,
        modifier = modifier,
        contentDescription = "Close",
        size = 44.dp,
    )
}

/**
 * A tiny face for the daily check-in.
 *
 * Drawn, not emoji: five expressions that share one silhouette, so the row reads as a scale
 * rather than five unrelated stickers.
 */
enum class Mood(val id: String, val label: String) {
    Heavy("heavy", "heavy"),
    Low("low", "low"),
    Even("even", "even"),
    Good("good", "good"),
    Bright("bright", "bright"),
}

@Composable
fun MoodFace(
    mood: Mood,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KairosTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val tint = when (mood) {
        Mood.Heavy -> colors.periwinkle
        Mood.Low -> colors.periwinkle.copy(alpha = 0.8f)
        Mood.Even -> colors.butter
        Mood.Good -> colors.mint
        Mood.Bright -> colors.apricot
    }
    Box(
        modifier = modifier
            .size(if (selected) 54.dp else 46.dp)
            .pressScale(interaction, 0.9f)
            .clip(CircleShape)
            .background(if (selected) tint else colors.glassFillStrong)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(if (selected) 36.dp else 30.dp)) {
            val ink = Color(0xFF2A2233).copy(alpha = if (selected) 0.9f else 0.65f)
            val eyeY = size.height * 0.42f
            val eyeR = size.width * 0.055f
            val stroke = size.width * 0.06f
            drawCircle(ink, eyeR, Offset(size.width * 0.34f, eyeY))
            drawCircle(ink, eyeR, Offset(size.width * 0.66f, eyeY))
            val mouth = Path()
            when (mood) {
                Mood.Heavy -> {
                    mouth.moveTo(size.width * 0.32f, size.height * 0.72f)
                    mouth.quadraticBezierTo(
                        size.width * 0.5f, size.height * 0.60f,
                        size.width * 0.68f, size.height * 0.72f,
                    )
                }
                Mood.Low -> {
                    mouth.moveTo(size.width * 0.34f, size.height * 0.70f)
                    mouth.quadraticBezierTo(
                        size.width * 0.5f, size.height * 0.64f,
                        size.width * 0.66f, size.height * 0.70f,
                    )
                }
                Mood.Even -> {
                    mouth.moveTo(size.width * 0.36f, size.height * 0.68f)
                    mouth.lineTo(size.width * 0.64f, size.height * 0.68f)
                }
                Mood.Good -> {
                    mouth.moveTo(size.width * 0.34f, size.height * 0.64f)
                    mouth.quadraticBezierTo(
                        size.width * 0.5f, size.height * 0.78f,
                        size.width * 0.66f, size.height * 0.64f,
                    )
                }
                Mood.Bright -> {
                    mouth.moveTo(size.width * 0.32f, size.height * 0.62f)
                    mouth.quadraticBezierTo(
                        size.width * 0.5f, size.height * 0.84f,
                        size.width * 0.68f, size.height * 0.62f,
                    )
                }
            }
            drawPath(mouth, ink, style = Stroke(width = stroke, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun MoodRow(
    selected: String?,
    onSelect: (Mood) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Mood.entries.forEach { mood ->
            MoodFace(
                mood = mood,
                selected = selected == mood.id,
                onClick = { onSelect(mood) },
            )
        }
    }
}

/** Seven-day streak strip: filled stones for active days, a ring for today. */
@Composable
fun StreakStrip(
    week: List<com.kairos.app.domain.progress.Progress.DayMark>,
    modifier: Modifier = Modifier,
) {
    val colors = KairosTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        week.forEach { day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (day.isToday) 30.dp else 26.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                day.active && day.isToday -> colors.ink
                                day.active -> colors.apricot.copy(alpha = 0.85f)
                                day.isToday -> colors.glassFillStrong
                                else -> colors.hairline.copy(alpha = 0.55f)
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (day.isToday && !day.active) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.ink.copy(alpha = 0.4f)),
                        )
                    }
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    text = day.date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH),
                    style = KairosType.eyebrow.copy(letterSpacing = 0.sp, fontSize = 8.sp),
                    color = if (day.isToday) colors.ink else colors.inkFaint,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Soft section divider with a tiny dot in the middle. */
@Composable
fun DotDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(KairosTheme.colors.hairline),
        )
        Box(
            Modifier
                .padding(horizontal = 10.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(KairosTheme.colors.inkFaint),
        )
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(KairosTheme.colors.hairline),
        )
    }
}

/** Empty space that is actually a breath. */
@Composable
fun Breath(height: Dp = 8.dp) {
    Spacer(Modifier.height(height))
}

/** Text with a small trailing arrow, used for inline "open" affordances. */
@Composable
fun QuietAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        style = KairosType.handSmall,
        color = KairosTheme.colors.inkSoft,
        fontSize = 19.sp,
    )
}

/** Fixed-size spacer used in rows. */
@Composable
fun Gap(width: Dp = 8.dp) = Spacer(Modifier.width(width))
