package com.kairos.app.ui.words

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.core.design.GlassCard
import com.kairos.app.core.design.GlassIconButton
import com.kairos.app.core.design.InkButton
import com.kairos.app.core.design.Kai
import com.kairos.app.core.design.KaiMood
import com.kairos.app.core.design.KairosShape
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.KairosType
import com.kairos.app.core.design.pressScale
import com.kairos.app.core.design.staggerIn
import com.kairos.app.data.Grade

@Composable
fun ReviewScreen(
    session: ReviewUi,
    preview: (Grade) -> String,
    onReveal: () -> Unit,
    onGrade: (Grade) -> Unit,
    onClose: () -> Unit,
    onSpeak: (String) -> Unit,
) {
    if (session.finished) {
        ReviewComplete(session = session, onClose = onClose)
        return
    }
    val card = session.card ?: return
    val colors = KairosTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconButton(
                icon = Icons.Rounded.Close,
                onClick = onClose,
                contentDescription = "Close sitting",
                size = 44.dp,
            )
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(colors.hairline.copy(alpha = 0.55f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(session.progress.coerceIn(0.02f, 1f))
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(listOf(colors.apricot, colors.blossom)),
                        ),
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = "${session.index + 1}/${session.cards.size}",
                style = KairosType.handSmall,
                color = colors.inkSoft,
            )
        }

        Spacer(Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            FlipCard(
                revealed = session.revealed,
                onReveal = onReveal,
                front = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(KairosShape.large)
                            .background(colors.glassFillStrong)
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = card.reason.uppercase(),
                            style = KairosType.eyebrow,
                            color = colors.inkFaint,
                        )
                        Spacer(Modifier.height(26.dp))
                        Text(
                            text = card.word.term,
                            style = MaterialTheme.typography.displayLarge,
                            color = colors.ink,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = card.word.ipa,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.inkSoft,
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(colors.glassFill)
                                    .clickable { onSpeak(card.word.term) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Rounded.VolumeUp,
                                    contentDescription = "Hear it",
                                    tint = colors.inkSoft,
                                    modifier = Modifier.size(17.dp),
                                )
                            }
                        }
                        Spacer(Modifier.height(30.dp))
                        Text(
                            text = "say it out loud, then tap",
                            style = KairosType.handSmall,
                            color = colors.inkFaint,
                        )
                    }
                },
                back = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(KairosShape.large)
                            .background(colors.glassFillStrong)
                            .padding(26.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = card.word.term,
                            style = MaterialTheme.typography.headlineLarge,
                            color = colors.ink,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${card.word.partOfSpeech} · ${card.word.ipa}",
                            style = KairosType.handSmall,
                            color = colors.inkFaint,
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = card.word.definition,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 28.sp),
                            color = colors.ink,
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "“${card.word.example}”",
                            style = KairosType.quote.copy(fontSize = 20.sp, lineHeight = 27.sp, fontStyle = FontStyle.Italic),
                            color = colors.inkSoft,
                        )
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = card.word.etymology,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.inkFaint,
                        )
                    }
                },
            )
        }

        AnimatedVisibility(
            visible = session.revealed,
            enter = fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 4 },
            exit = fadeOut(tween(120)),
        ) {
            Column {
                Text(
                    text = "how did that feel?",
                    style = KairosType.handSmall,
                    color = colors.inkFaint,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Grade.entries.forEach { grade ->
                        GradeButton(
                            grade = grade,
                            preview = preview(grade),
                            onClick = { onGrade(grade) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(26.dp))
            }
        }
        if (!session.revealed) {
            Spacer(Modifier.height(34.dp))
        }
    }
}

@Composable
private fun FlipCard(
    revealed: Boolean,
    onReveal: () -> Unit,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
) {
    val density = LocalDensity.current.density
    val rotation by animateFloatAsState(
        targetValue = if (revealed) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 300f),
        label = "flip",
    )
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .pressScale(interaction, 0.99f)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = { if (!revealed) onReveal() },
            ),
    ) {
        if (rotation <= 90f) {
            front()
        } else {
            Box(Modifier.graphicsLayer { rotationY = 180f }) { back() }
        }
    }
}

@Composable
private fun GradeButton(
    grade: Grade,
    preview: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = KairosTheme.colors
    val tint = when (grade) {
        Grade.Again -> colors.periwinkle
        Grade.Hard -> colors.butter
        Grade.Good -> colors.mint
        Grade.Easy -> colors.apricot
    }
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .pressScale(interaction, 0.94f)
            .clip(RoundedCornerShape(18.dp))
            .background(tint.copy(alpha = 0.22f))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = grade.label,
            style = MaterialTheme.typography.labelLarge,
            color = colors.ink,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = preview,
            style = KairosType.eyebrow.copy(fontSize = 9.sp, letterSpacing = 0.2.sp),
            color = colors.inkSoft,
        )
    }
}

@Composable
private fun ReviewComplete(session: ReviewUi, onClose: () -> Unit) {
    val colors = KairosTheme.colors
    val reviewed = session.grades.size
    val accuracy = if (reviewed == 0) 0 else ((reviewed - session.againCount) * 100) / reviewed
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Kai(modifier = Modifier.size(170.dp), mood = if (accuracy >= 70) KaiMood.Proud else KaiMood.Happy)
        Spacer(Modifier.height(14.dp))
        Text(
            text = if (accuracy >= 70) "That settled in." else "That was the hard part.",
            style = MaterialTheme.typography.displaySmall,
            color = colors.ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "$reviewed cards · $accuracy% first-time recall · ${session.newCount} new",
            style = KairosType.handSmall,
            color = colors.inkSoft,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(26.dp))
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .staggerIn(0),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        ) {
            Column {
                Row {
                    StatBlock("met", "$reviewed", Modifier.weight(1f))
                    StatBlock("fresh", "${session.newCount}", Modifier.weight(1f))
                    StatBlock("wobbles", "${session.againCount}", Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "they'll come back when they're about to slip — not before.",
                    style = KairosType.handSmall,
                    color = colors.inkFaint,
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        InkButton(text = "Back to the garden", onClick = onClose, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun StatBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            color = KairosTheme.colors.ink,
        )
        Text(
            text = label,
            style = KairosType.eyebrow.copy(fontSize = 9.sp),
            color = KairosTheme.colors.inkFaint,
        )
    }
}
