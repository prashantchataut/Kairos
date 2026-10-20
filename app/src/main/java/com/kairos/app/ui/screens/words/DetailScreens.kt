package com.kairos.app.ui.screens.words

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.content.Taxonomy
import com.kairos.app.data.repo.today
import com.kairos.app.engine.Srs
import com.kairos.app.ui.components.GlassButton
import com.kairos.app.ui.components.HeartButton
import com.kairos.app.ui.components.IconBubble
import com.kairos.app.ui.components.Pill
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.glass.GlassPanel
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.navigation.Scene
import com.kairos.app.ui.navigation.sharedText
import com.kairos.app.ui.screens.today.categoryWash
import com.kairos.app.ui.screens.today.themeWash
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.theme.Wash
import com.kairos.app.util.appContainer
import com.kairos.app.util.rememberSpeaker
import kotlinx.coroutines.launch

@Composable
fun DetailTopBar(onBack: () -> Unit, modifier: Modifier = Modifier, actions: @Composable () -> Unit = {}) {
    Row(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = Space.gutter, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.sm),
    ) {
        IconBubble(KairosIcons.Back, "Back", onBack, blur = true)
        Spacer(Modifier.weight(1f))
        actions()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordDetailScreen(id: String, origin: String, onBack: () -> Unit, onShare: () -> Unit, onPractice: () -> Unit) {
    val c = appContainer()
    val word by c.kairos.observeWord(id).collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val speaker = rememberSpeaker()
    val w = word ?: return
    val palette = Kairos.colors
    val wash = categoryWash(w.category)
    Scene(wash, Wash.Cerulean, wash, Wash.Lilac, Wash.Butter)

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = Space.huge)
        ) {
            Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars).height(72.dp))
            Column(Modifier.padding(horizontal = Space.gutter)) {
                Text(Taxonomy.wordLabel(w.category), style = Kairos.type.label, color = palette.accent)
                Spacer(Modifier.height(Space.sm))
                Text(w.word, style = Kairos.type.hero, color = palette.ink, modifier = Modifier.sharedText("word-${w.id}-$origin"))
                Spacer(Modifier.height(Space.md))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill(w.pronunciation, icon = KairosIcons.Speaker, onClick = { speaker.say(w.word) })
                    Spacer(Modifier.width(Space.sm))
                    Text(w.partOfSpeech, style = Kairos.type.body.copy(fontStyle = FontStyle.Italic), color = palette.inkSoft)
                }
                Spacer(Modifier.height(Space.xl))
                Text(w.definition, style = Kairos.type.quote.copy(fontStyle = FontStyle.Normal), color = palette.ink)
            }
            Spacer(Modifier.height(Space.xl))
            GlassPanel(Modifier.padding(horizontal = Space.gutter).fillMaxWidth()) {
                Column(Modifier.padding(Space.xl)) {
                    Text("In a sentence", style = Kairos.type.label, color = palette.inkSoft)
                    Spacer(Modifier.height(Space.sm))
                    Text("“${w.example}”", style = Kairos.type.quote.copy(fontSize = Kairos.type.title.fontSize), color = palette.ink)
                }
            }
            if (w.origin.isNotBlank()) {
                Spacer(Modifier.height(Space.md))
                GlassPanel(Modifier.padding(horizontal = Space.gutter).fillMaxWidth(), blur = false) {
                    Column(Modifier.padding(Space.xl)) {
                        Text("Where it comes from", style = Kairos.type.label, color = palette.inkSoft)
                        Spacer(Modifier.height(Space.sm))
                        Text(w.origin, style = Kairos.type.body, color = palette.ink)
                    }
                }
            }
            if (w.synonymList.isNotEmpty()) {
                Spacer(Modifier.height(Space.xl))
                Text("Close cousins", style = Kairos.type.title, color = palette.ink, modifier = Modifier.padding(horizontal = Space.gutter))
                Spacer(Modifier.height(Space.sm))
                FlowRow(
                    Modifier.padding(horizontal = Space.gutter),
                    horizontalArrangement = Arrangement.spacedBy(Space.sm),
                    verticalArrangement = Arrangement.spacedBy(Space.sm),
                ) { w.synonymList.forEach { Pill(it) } }
            }
            Spacer(Modifier.height(Space.xl))
            GlassPanel(Modifier.padding(horizontal = Space.gutter).fillMaxWidth(), blur = false) {
                Column(Modifier.padding(Space.xl)) {
                    val status = when {
                        w.mastered -> "Remembered for good. Next check-in ${Srs.formatInterval(((w.dueDay ?: today()) - today()).toInt())} from now."
                        w.inPractice -> {
                            val days = ((w.dueDay ?: today()) - today()).toInt()
                            if (days <= 0) "Ready to review now." else "Next review in ${Srs.formatInterval(days)}."
                        }
                        else -> "Not in practice yet. Saving a word adds it."
                    }
                    Text("Remembering it", style = Kairos.type.label, color = palette.inkSoft)
                    Spacer(Modifier.height(Space.sm))
                    Text(status, style = Kairos.type.body, color = palette.ink)
                    Spacer(Modifier.height(Space.md))
                    Text("Try using “${w.word.lowercase()}” in a message today. Words stick when they leave the app.", style = Kairos.type.label, color = palette.inkSoft)
                    Spacer(Modifier.height(Space.lg))
                    PrimaryButton("Practise now", onPractice, icon = KairosIcons.Cards)
                }
            }
        }
        DetailTopBar(onBack) {
            HeartButton(w.saved, { scope.launch { c.kairos.toggleWordSaved(w.id) } })
            IconBubble(KairosIcons.Share, "Make a share card", onShare, blur = true)
        }
    }
}

@Composable
fun QuoteDetailScreen(id: String, onBack: () -> Unit, onShare: () -> Unit) {
    val c = appContainer()
    val quote by c.kairos.observeQuote(id).collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val q = quote ?: return
    val palette = Kairos.colors
    val wash = themeWash(q.theme)
    Scene(wash, Wash.Lilac, wash, Wash.Cerulean, Wash.Peach)
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = Space.gutter),
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars).height(96.dp))
            Icon(KairosIcons.Quote, null, tint = wash.color(palette.isDark), modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(Space.lg))
            Text(q.text, style = Kairos.type.quote.copy(fontSize = Kairos.type.display.fontSize, lineHeight = Kairos.type.display.lineHeight * 1.18f), color = palette.ink)
            Spacer(Modifier.height(Space.xl))
            Text("— ${q.author}", style = Kairos.type.title, color = palette.inkSoft)
            Spacer(Modifier.height(Space.sm))
            Text("A thought about ${Taxonomy.themeLabel(q.theme).lowercase()}", style = Kairos.type.label, color = palette.inkFaint)
            Spacer(Modifier.height(Space.huge))
            GlassButton("Make it a card", onShare, icon = KairosIcons.Palette, blur = true)
            Spacer(Modifier.height(Space.huge))
        }
        DetailTopBar(onBack) {
            HeartButton(q.saved, { scope.launch { c.kairos.toggleQuoteSaved(q.id) } })
            IconBubble(KairosIcons.Share, "Make a share card", onShare, blur = true)
        }
    }
}
