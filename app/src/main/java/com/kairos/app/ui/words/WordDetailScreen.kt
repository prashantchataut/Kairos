package com.kairos.app.ui.words

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.core.design.BackButton
import com.kairos.app.core.design.GlassCard
import com.kairos.app.core.design.GlassIconButton
import com.kairos.app.core.design.InkButton
import com.kairos.app.core.design.KairosShape
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.KairosType
import com.kairos.app.core.design.ProgressRing
import com.kairos.app.core.design.Tag
import com.kairos.app.core.util.KairosDates
import com.kairos.app.data.WordMemory
import com.kairos.app.data.catalog.VocabCatalog
import com.kairos.app.domain.vocab.SrsEngine
import com.kairos.app.ui.common.Speaker
import com.kairos.app.ui.common.rememberSpeaker
import java.time.Instant

@Composable
fun WordDetailScreen(
    wordId: String,
    memory: WordMemory,
    onBack: () -> Unit,
    onToggleSave: () -> Unit,
    onShare: () -> Unit,
    onPractice: () -> Unit,
) {
    val word = VocabCatalog.byId[wordId] ?: return
    val colors = KairosTheme.colors
    val context = LocalContext.current
    val speaker = rememberSpeaker(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onClick = onBack)
            Spacer(Modifier.weight(1f))
            GlassIconButton(
                icon = if (memory.saved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                onClick = onToggleSave,
                contentDescription = "Save word",
                tint = if (memory.saved) colors.blossom else colors.ink,
            )
            Spacer(Modifier.width(10.dp))
            GlassIconButton(
                icon = Icons.Rounded.IosShare,
                onClick = onShare,
                contentDescription = "Make a poster",
            )
        }

        Spacer(Modifier.height(30.dp))
        Text(
            text = word.partOfSpeech.uppercase(),
            style = KairosType.eyebrow,
            color = colors.inkFaint,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = word.term,
            style = MaterialTheme.typography.displayLarge,
            color = colors.ink,
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .clip(KairosShape.pill)
                    .background(colors.glassFillStrong)
                    .clickable { speaker.speak(word.term) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = word.ipa,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkSoft,
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Rounded.VolumeUp,
                    contentDescription = "Hear the word",
                    tint = colors.inkSoft,
                    modifier = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            if (word.family.isNotBlank()) {
                Tag(text = "family: ${word.family}")
            }
        }

        Spacer(Modifier.height(26.dp))
        Text(
            text = word.definition,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 29.sp),
            color = colors.ink,
        )

        Spacer(Modifier.height(22.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        ) {
            Column {
                Text(
                    text = "IN A SENTENCE",
                    style = KairosType.eyebrow,
                    color = colors.inkFaint,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "“${word.example}”",
                    style = KairosType.quote.copy(fontSize = 23.sp, lineHeight = 31.sp, fontStyle = FontStyle.Italic),
                    color = colors.ink,
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KairosShape.medium)
                .background(colors.paperDeep.copy(alpha = 0.6f))
                .padding(18.dp),
        ) {
            Text(
                text = "WHERE IT COMES FROM",
                style = KairosType.eyebrow,
                color = colors.inkFaint,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = word.etymology,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSoft,
            )
        }

        if (word.synonyms.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "kin",
                    style = KairosType.handSmall,
                    color = colors.inkFaint,
                )
                word.synonyms.forEach { Tag(text = it) }
            }
        }

        Spacer(Modifier.height(24.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    progress = SrsEngine.mastery(memory),
                    size = 54.dp,
                    stroke = 5.dp,
                    brush = Brush.linearGradient(listOf(colors.mint, colors.periwinkle)),
                ) {
                    Text(
                        text = "${(SrsEngine.mastery(memory) * 100).toInt()}",
                        style = KairosType.eyebrow.copy(fontSize = 10.sp),
                        color = colors.inkSoft,
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            memory.seen == 0 -> "Not met yet"
                            memory.dueAt <= System.currentTimeMillis() -> "Ready for a review"
                            else -> "Next review ${KairosDates.coarseDistance(Instant.now(), Instant.ofEpochMilli(memory.dueAt))}"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.ink,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = when {
                            memory.seen == 0 -> "one card, about twenty seconds"
                            memory.lapses > 0 -> "${memory.lapses} wobble${if (memory.lapses == 1) "" else "s"} so far — that's normal"
                            else -> "seen ${memory.seen} time${if (memory.seen == 1) "" else "s"}"
                        },
                        style = KairosType.handSmall,
                        color = colors.inkSoft,
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        InkButton(
            text = "Practise this word",
            onClick = onPractice,
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Rounded.ArrowForward,
        )

        Spacer(Modifier.height(40.dp))
    }
}
