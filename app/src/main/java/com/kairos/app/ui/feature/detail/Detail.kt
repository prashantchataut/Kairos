package com.kairos.app.ui.feature.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.repo.today
import com.kairos.app.engine.Srs
import com.kairos.app.engine.Topics
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.Chip
import com.kairos.app.ui.components.GlassAction
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.RoundIcon
import com.kairos.app.ui.components.Status
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.feature.today.WordFeedback
import com.kairos.app.ui.nav.shared
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.util.appContainer
import com.kairos.app.util.rememberSpeaker
import kotlinx.coroutines.launch

@Composable
fun TopBar(onBack: () -> Unit, onSky: Boolean, modifier: Modifier = Modifier, actions: @Composable () -> Unit = {}) {
    Row(
        modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = Gap.gutter, vertical = Gap.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Gap.sm),
    ) {
        RoundIcon(Icons.Back, "Back", onBack, onSky = onSky)
        Spacer(Modifier.weight(1f))
        actions()
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter)) {
        Text(title, style = K.type.caption, color = K.colors.inkFaint)
        Spacer(Modifier.height(Gap.sm))
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordScreen(id: String, onBack: () -> Unit, onShare: () -> Unit, onPractice: () -> Unit) {
    val c = appContainer()
    val word by c.kairos.observeWord(id).collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val speaker = rememberSpeaker()
    var told by remember { mutableStateOf<String?>(null) }
    val w = word ?: return
    val p = K.colors
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = Gap.huge)) {
        Column(Modifier.fillMaxWidth().sky().padding(bottom = Gap.xxl)) {
            TopBar(onBack, onSky = true)
            Spacer(Modifier.height(Gap.lg))
            Text("${Topics.wordLabel(w.category)}, ${Topics.levels[(w.level - 1).coerceIn(0, 4)].lowercase()}", style = K.type.label, color = p.onSkySoft, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(Gap.sm))
            val size = when { w.word.length <= 8 -> 72.sp; w.word.length <= 12 -> 56.sp; else -> 42.sp }
            Text(w.word, style = K.type.hero.copy(fontSize = size, lineHeight = size * 1.05f), color = p.onSky, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = Gap.gutter).shared("word-${w.id}"))
            Spacer(Modifier.height(Gap.sm))
            Text("${w.pronunciation}  ·  ${w.partOfSpeech}", style = K.type.label, color = p.onSkySoft, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(Gap.xl))
            Row(Modifier.padding(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
                GlassAction(Icons.Speaker, "Listen", { speaker.say(w.word) }, Modifier.weight(1f))
                GlassAction(if (w.saved) Icons.HeartOn else Icons.Heart, if (w.saved) "Saved" else "Save", { scope.launch { c.kairos.toggleWordSaved(w.id) } }, Modifier.weight(1f), active = w.saved)
                GlassAction(Icons.Share, "Share", onShare, Modifier.weight(1f))
            }
        }
        InfoCard("Meaning") {
            Text(w.definition, style = K.type.body.copy(fontSize = 19.sp, lineHeight = 28.sp), color = p.ink)
            if (w.synonymList.isNotEmpty()) {
                Spacer(Modifier.height(Gap.md))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalArrangement = Arrangement.spacedBy(Gap.sm)) {
                    w.synonymList.forEach { Text(it, style = K.type.caption, color = p.inkSoft, modifier = Modifier.clip(CircleShape).background(p.surfaceSoft).padding(horizontal = 10.dp, vertical = 5.dp)) }
                }
            }
        }
        Spacer(Modifier.height(Gap.md))
        InfoCard("In a sentence") { Text("“${w.example}”", style = K.type.quote.copy(fontSize = 22.sp, lineHeight = 29.sp), color = p.ink) }
        if (w.origin.isNotBlank()) {
            Spacer(Modifier.height(Gap.md))
            InfoCard("Where it comes from") { Text(w.origin, style = K.type.body, color = p.ink) }
        }
        Spacer(Modifier.height(Gap.md))
        InfoCard("Remembering it") {
            val status = when {
                w.mastered -> "Remembered for good. A light check-in comes back now and then."
                w.inPractice -> ((w.dueDay ?: today()) - today()).toInt().let { if (it <= 0) "Ready to review now." else "Next review in ${Srs.formatInterval(it)}." }
                else -> "Not in practice yet. Saving a word adds it."
            }
            Text(status, style = K.type.body, color = p.ink)
            if (w.hook.isNotBlank()) { Spacer(Modifier.height(Gap.md)); Status(w.hook, p.positive) }
            Spacer(Modifier.height(Gap.lg))
            PrimaryButton("Practise now", onPractice, icon = Icons.Cards)
        }
        Spacer(Modifier.height(Gap.md))
        InfoCard("Teach your curator") {
            AnimatedContent(told, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "told") { t ->
                if (t != null) Text(t, style = K.type.label, color = p.accent)
                else FlowRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalArrangement = Arrangement.spacedBy(Gap.sm)) {
                    WordFeedback.entries.forEach { fb ->
                        Chip(fb.label, false, {
                            scope.launch {
                                when (fb) {
                                    WordFeedback.TooEasy -> c.kairos.setWordDifficulty(w.id, -1)
                                    WordFeedback.TooHard -> c.kairos.setWordDifficulty(w.id, 1)
                                    WordFeedback.More -> c.kairos.setWordTaste(w.id, 1)
                                    WordFeedback.NotForMe -> c.kairos.setWordTaste(w.id, -1)
                                }
                                told = if (fb == WordFeedback.NotForMe) "Got it. Words like this won't come back." else fb.reply
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteScreen(id: String, onBack: () -> Unit, onShare: () -> Unit) {
    val c = appContainer()
    val quote by c.kairos.observeQuote(id).collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val q = quote ?: return
    val p = K.colors
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = Gap.huge)) {
        Column(Modifier.fillMaxWidth().sky().padding(bottom = Gap.xxl)) {
            TopBar(onBack, onSky = true)
            Spacer(Modifier.height(Gap.xl))
            Text("“${q.text}”", style = K.type.quote.copy(fontSize = 32.sp, lineHeight = 40.sp), color = p.onSky, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = Gap.xl))
            Spacer(Modifier.height(Gap.lg))
            Text(q.author, style = K.type.label, color = p.onSkySoft, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(Gap.xl))
            Row(Modifier.padding(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
                GlassAction(if (q.saved) Icons.HeartOn else Icons.Heart, if (q.saved) "Saved" else "Save", { scope.launch { c.kairos.toggleQuoteSaved(q.id) } }, Modifier.weight(1f), active = q.saved)
                GlassAction(Icons.Share, "Make a card", onShare, Modifier.weight(1f))
            }
        }
        if (q.note.isNotBlank()) InfoCard("About it") { Text(q.note, style = K.type.body, color = p.ink) }
        Spacer(Modifier.height(Gap.md))
        InfoCard("Theme") { Text(Topics.themeLabel(q.theme), style = K.type.body, color = p.ink) }
        Spacer(Modifier.height(Gap.md))
        Box(Modifier.padding(horizontal = Gap.gutter)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                Chip("More like this", q.taste > 0, { scope.launch { c.kairos.setQuoteTaste(q.id, if (q.taste > 0) 0 else 1) } }, leading = Icons.ThumbUp)
                Chip("Not for me", q.taste < 0, { scope.launch { c.kairos.setQuoteTaste(q.id, if (q.taste < 0) 0 else -1) } }, leading = Icons.ThumbDown)
            }
        }
    }
}
