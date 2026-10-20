package com.kairos.app.ui.feature.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import com.kairos.app.engine.Topics
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.Chip
import com.kairos.app.ui.components.Empty
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.Progress
import com.kairos.app.ui.components.Segmented
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.nav.shared
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Radius
import com.kairos.app.ui.theme.Tint
import com.kairos.app.ui.theme.tint
import com.kairos.app.ui.theme.tintInk
import com.kairos.app.util.appViewModel

fun categoryTint(category: String) = when (category) {
    "feelings" -> Tint.Rose; "mind" -> Tint.Lavender; "nature" -> Tint.Mint; "connection" -> Tint.Peach
    "craft" -> Tint.Butter; "wonder" -> Tint.Sky; "courage" -> Tint.Mint; "science" -> Tint.Sky; else -> Tint.Lavender
}

fun themeTint(theme: String) = when (theme) {
    "hope", "joy" -> Tint.Butter; "courage" -> Tint.Peach; "kindness" -> Tint.Rose; "growth" -> Tint.Mint
    "calm" -> Tint.Sky; else -> Tint.Lavender
}

private val Suggestions = listOf("the ocean", "kindness", "light and colour", "cosy evenings", "starting over", "music", "the night sky")

@Composable
fun LearnScreen(openWord: (String) -> Unit, openQuote: (String) -> Unit, openPractice: () -> Unit, openAiSettings: () -> Unit) {
    val vm = appViewModel { LearnViewModel(it) }
    val s by vm.state.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()
    var section by rememberSaveable { mutableIntStateOf(0) }
    val p = K.colors

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Gap.gutter, end = Gap.gutter, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(Gap.md),
        verticalItemSpacing = Gap.md,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Column(
                Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = Gap.xl)
            ) {
                Text("Learn", style = K.type.display, color = p.ink)
                Text(
                    if (s.total == 0) "Your words will gather here." else "${s.total} words written for you, ${s.remembered} remembered for good.",
                    style = K.type.body, color = p.inkSoft,
                )
                Spacer(Modifier.height(Gap.xl))
                ExploreBox(s, vm::explore, openAiSettings)
                Spacer(Modifier.height(Gap.lg))
                if (s.due > 0 || s.saved > 0) {
                    ReviewBanner(s.due, s.saved, openPractice)
                    Spacer(Modifier.height(Gap.lg))
                }
                Segmented(listOf("Words", "Thoughts"), section, { section = it }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(Gap.md))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                    items(if (section == 0) Filter.entries.toList() else listOf(Filter.All, Filter.Saved)) { f ->
                        Chip(f.label, f == filter, { vm.filter.value = f })
                    }
                }
                Spacer(Modifier.height(Gap.xs))
            }
        }
        if (section == 0) {
            if (s.words.isEmpty()) item(span = StaggeredGridItemSpan.FullLine) {
                Empty(
                    if (filter == Filter.All) "No words yet" else "Nothing here yet",
                    if (filter == Filter.Saved) "Save a word you love and it lands here, ready for practice." else "Ask for words about anything above, or open Today.",
                )
            }
            items(s.words, key = { it.id }) { w -> WordTile(w, fresh = w.id in s.explore.freshIds, onClick = { openWord(w.id) }, modifier = Modifier.animateItem()) }
        } else {
            if (s.quotes.isEmpty()) item(span = StaggeredGridItemSpan.FullLine) { Empty("No thoughts yet", "Quotes from Today collect here.") }
            items(s.quotes, key = { it.id }) { q -> QuoteTile(q, onClick = { openQuote(q.id) }, modifier = Modifier.animateItem()) }
        }
    }
}

@Composable
private fun ExploreBox(s: LearnState, onExplore: (String) -> Unit, openAiSettings: () -> Unit) {
    val p = K.colors
    var text by rememberSaveable { mutableStateOf("") }
    val focus = LocalFocusManager.current
    fun go() { if (s.hasKey) { onExplore(text); focus.clearFocus(); text = "" } else openAiSettings() }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(Radius.card)
            .sky()
            .padding(Gap.lg)
    ) {
        Text("Ask for words about…", style = K.type.label, color = p.onSky)
        Spacer(Modifier.height(Gap.md))
        Row(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(CircleShape)
                .background(p.surface)
                .padding(start = Gap.lg, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f)) {
                if (text.isEmpty()) Text(if (s.explore.running) "Writing words about ${s.explore.topic}…" else "anything you're curious about", style = K.type.body, color = p.inkFaint, maxLines = 1)
                BasicTextField(
                    text, { text = it.take(80) }, singleLine = true, enabled = !s.explore.running,
                    textStyle = K.type.body.copy(color = p.ink), cursorBrush = SolidColor(p.accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { go() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(
                Modifier
                    .size(40.dp)
                    .pressable(enabled = !s.explore.running && (text.isNotBlank() || !s.hasKey), label = "Generate words") { go() }
                    .clip(CircleShape)
                    .background(if (text.isNotBlank()) p.accent else p.surfaceSoft),
                contentAlignment = Alignment.Center,
            ) {
                if (s.explore.running) CircularProgressIndicator(Modifier.size(20.dp), color = p.accent, strokeWidth = 2.dp)
                else Icon(Icons.ArrowUp, null, tint = if (text.isNotBlank()) p.onAccent else p.inkFaint, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(Gap.md))
        AnimatedContent(s.explore.message, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "exMsg") { m ->
            if (m != null) Text(m, style = K.type.caption, color = p.onSky)
            else LazyRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                items(Suggestions) { sug ->
                    Text(
                        sug, style = K.type.caption, color = p.onSky,
                        modifier = Modifier
                            .pressable { if (s.hasKey) onExplore(sug) else openAiSettings() }
                            .clip(CircleShape)
                            .background(p.glassOnSky)
                            .border(1.dp, p.glassOnSkyRim, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewBanner(due: Int, saved: Int, onStart: () -> Unit) {
    val p = K.colors
    Card(Modifier.fillMaxWidth(), padding = Gap.lg, onClick = onStart) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (due > 0) "$due ${if (due == 1) "word" else "words"} to revisit" else "Practise your saved words", style = K.type.title, color = p.ink)
                Text(if (due > 0) "Spaced out so they stick" else "$saved saved and waiting", style = K.type.caption, color = p.inkSoft)
            }
            PrimaryButton("Start", onStart, icon = Icons.Cards)
        }
        if (due > 0) {
            Spacer(Modifier.height(Gap.md))
            Progress(1f / (1 + due), Modifier.fillMaxWidth())
        }
    }
}

/** Pastel tile, tinted by topic. Heights follow the content, which gives the board its rhythm. */
@Composable
private fun WordTile(w: WordEntity, fresh: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    val t = categoryTint(w.category)
    val glow by animateFloatAsState(if (fresh) 1f else 0f, label = "fresh")
    Column(
        modifier
            .fillMaxWidth()
            .pressable(scale = 0.97f, haptic = false, onClick = onClick)
            .clip(Radius.tile)
            .background(p.tint(t))
            .padding(Gap.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(Topics.wordLabel(w.category), style = K.type.caption, color = p.tintInk(t), modifier = Modifier.weight(1f))
            if (w.saved) Icon(Icons.HeartOn, null, tint = p.tintInk(t), modifier = Modifier.size(14.dp))
            else if (fresh) Icon(Icons.Sparkle, null, tint = p.tintInk(t), modifier = Modifier.size(14.dp).graphicsLayer { alpha = glow })
        }
        Spacer(Modifier.height(Gap.lg))
        Text(w.word, style = K.type.serifLarge.copy(fontSize = if (w.word.length > 11) 26.sp else 32.sp, lineHeight = 34.sp), color = p.ink, modifier = Modifier.shared("word-${w.id}"))
        Text(w.partOfSpeech, style = K.type.caption.copy(fontStyle = FontStyle.Italic), color = p.inkSoft)
        Spacer(Modifier.height(Gap.sm))
        Text(w.definition, style = K.type.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal), color = p.ink.copy(alpha = 0.78f), maxLines = 4, overflow = TextOverflow.Ellipsis)
        if (w.inPractice) {
            Spacer(Modifier.height(Gap.md))
            Text(if (w.mastered) "Remembered" else "Learning", style = K.type.caption, color = p.tintInk(t), modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = if (p.isDark) 0.08f else 0.55f)).padding(horizontal = 9.dp, vertical = 4.dp))
        }
    }
}

@Composable
private fun QuoteTile(q: QuoteEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    Column(
        modifier
            .fillMaxWidth()
            .pressable(scale = 0.97f, haptic = false, onClick = onClick)
            .clip(Radius.tile)
            .background(p.surface)
            .border(1.dp, p.line, Radius.tile)
            .padding(Gap.lg)
    ) {
        Icon(Icons.Quote, null, tint = p.tintInk(themeTint(q.theme)), modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(Gap.sm))
        Text(q.text, style = K.type.quote.copy(fontSize = 19.sp, lineHeight = 25.sp), color = p.ink)
        Spacer(Modifier.height(Gap.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(q.author, style = K.type.caption, color = p.inkSoft, modifier = Modifier.weight(1f), maxLines = 2)
            if (q.saved) Icon(Icons.HeartOn, null, tint = p.negative, modifier = Modifier.size(14.dp))
        }
    }
}

