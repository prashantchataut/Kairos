package com.kairos.app.ui.screens.learn

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.kairos.app.container
import com.kairos.app.data.content.Themes
import com.kairos.app.data.content.Word
import com.kairos.app.data.db.WordProgress
import com.kairos.app.data.today
import com.kairos.app.engine.SpacedRepetition
import com.kairos.app.ui.kit.AcrylicCard
import com.kairos.app.ui.kit.CornerWash
import com.kairos.app.ui.kit.GlassIconButton
import com.kairos.app.ui.kit.KairosTopBar
import com.kairos.app.ui.kit.Tag
import com.kairos.app.ui.kit.pushedContentPadding
import com.kairos.app.ui.kit.rememberSpeaker
import com.kairos.app.ui.kit.rise
import com.kairos.app.ui.kit.shareText
import com.kairos.app.ui.kit.squishClick
import com.kairos.app.ui.screens.today.HeartButton
import com.kairos.app.ui.theme.Kairos
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WordDetailScreen(id: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val c = context.container
    val k = Kairos.colors
    val scope = rememberCoroutineScope()
    val speaker = rememberSpeaker()
    var word by remember { mutableStateOf<Word?>(null) }
    var related by remember { mutableStateOf<List<Word>>(emptyList()) }
    val progress by remember(id) { c.learn.observeWord(id) }.collectAsState(initial = null)
    val p: WordProgress? = progress

    LaunchedEffect(id) {
        val all = c.learn.words()
        val w = all.firstOrNull { it.id == id }
        word = w
        related = all.filter { it.theme == w?.theme && it.id != id }.shuffled(kotlin.random.Random(id.hashCode())).take(4)
    }

    Column(Modifier.fillMaxSize()) {
        KairosTopBar("", onBack) {
            word?.let { w ->
                GlassIconButton(Icons.Rounded.IosShare, "Share", {
                    shareText(context, "${w.word} (${w.pos}) — ${w.definition}\n“${w.example}”\n\nfrom Kairos")
                })
            }
        }
        val w = word ?: return@Column
        LazyColumn(contentPadding = pushedContentPadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Column(Modifier.rise(0)) {
                    Text(w.word, style = MaterialTheme.typography.displayLarge, color = k.ink)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("/${w.phonetic}/", style = MaterialTheme.typography.titleMedium, color = k.inkSoft)
                        Box(
                            Modifier.size(48.dp).clip(CircleShape).squishClick(pressedScale = 0.85f) { speaker.say(w.word) },
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.AutoMirrored.Rounded.VolumeUp, "Hear it", tint = k.lagoon) }
                        Spacer(Modifier.weight(1f))
                        HeartButton(p?.saved == true) { scope.launch { c.learn.toggleWordSaved(w.id) } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Tag(w.pos, color = k.washes.sky)
                        Tag(Themes.label(w.theme), color = k.washes.mint)
                        Tag(listOf("gentle", "curious", "brainy")[(w.level - 1).coerceIn(0, 2)], color = k.washes.butter)
                    }
                }
            }
            item {
                AcrylicCard(Modifier.fillMaxWidth().rise(1), tint = k.washes.mint) {
                    CornerWash(k.washes.sky, Modifier.matchParentSize(), seed = 8)
                    Column(Modifier.padding(22.dp)) {
                        Text("Meaning", style = MaterialTheme.typography.labelLarge, color = k.inkSoft)
                        Spacer(Modifier.height(6.dp))
                        Text(w.definition, style = MaterialTheme.typography.headlineSmall, color = k.ink)
                        Spacer(Modifier.height(18.dp))
                        Text("In a sentence", style = MaterialTheme.typography.labelLarge, color = k.inkSoft)
                        Spacer(Modifier.height(6.dp))
                        Text("“${w.example}”", style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic), color = k.ink)
                    }
                }
            }
            item {
                val g = SpacedRepetition.growth(p)
                AcrylicCard(Modifier.fillMaxWidth().rise(2)) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        GrowthGlyph(g, Modifier.size(52.dp))
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(g.label, style = MaterialTheme.typography.titleLarge, color = k.ink)
                            Text(
                                when {
                                    p == null || p.lastReviewedEpochDay < 0 -> "Not practised yet. It'll show up in your next session."
                                    p.dueEpochDay <= today() -> "Ready to water today."
                                    else -> "Next watering " + LocalDate.ofEpochDay(p.dueEpochDay).format(DateTimeFormatter.ofPattern("EEE d MMM")) + "."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = k.inkSoft,
                            )
                        }
                    }
                }
            }
            if (related.isNotEmpty()) {
                item { Text("More ${w.theme} words", style = MaterialTheme.typography.titleLarge, color = k.ink, modifier = Modifier.padding(top = 8.dp)) }
                items(related, key = { it.id }) { r ->
                    AcrylicCard(Modifier.fillMaxWidth(), elevation = 4.dp) {
                        Column(Modifier.padding(16.dp)) {
                            Text(r.word, style = MaterialTheme.typography.titleLarge, color = k.ink)
                            Text(r.definition, style = MaterialTheme.typography.bodyMedium, color = k.inkSoft)
                        }
                    }
                }
            }
        }
    }
}
