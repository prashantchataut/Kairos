package com.kairos.app.ui.feature.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import com.kairos.app.engine.Topics
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.art.Paints
import com.kairos.app.ui.art.painted
import com.kairos.app.ui.components.Chip
import com.kairos.app.ui.components.Empty
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.Segmented
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.nav.shared
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radius
import com.kairos.app.ui.theme.Tint
import com.kairos.app.ui.theme.candy
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
        contentPadding = PaddingValues(bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(Gap.md),
        verticalItemSpacing = Gap.md,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Column(Modifier.fillMaxWidth().sky().windowInsetsPadding(WindowInsets.statusBars).padding(top = Gap.lg, bottom = Gap.xxl)) {
                Column(Modifier.padding(horizontal = Gap.gutter)) {
                    Text("Your words", style = K.type.shout.copy(fontSize = 40.sp), color = p.onSky)
                    Text(
                        when {
                            s.total == 0 -> "They'll gather here, one day at a time."
                            s.remembered == 0 -> "${s.total} written just for you. None remembered for good yet, but soon."
                            else -> "${s.total} written just for you, ${s.remembered} remembered for good."
                        },
                        style = K.type.body, color = p.onSkySoft,
                    )
                    Spacer(Modifier.height(Gap.xl))
                    AskKai(s, vm::explore, openAiSettings)
                }
                Spacer(Modifier.height(Gap.md))
                AnimatedContent(s.explore.message, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "exMsg") { m ->
                    if (m != null) Text(m, style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = p.onSky, modifier = Modifier.padding(horizontal = Gap.gutter))
                    else LazyRow(contentPadding = PaddingValues(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                        items(Suggestions) { sug ->
                            Text(
                                sug, style = K.type.label, color = p.onSky,
                                modifier = Modifier
                                    .pressable(scale = 0.92f) { if (s.hasKey) vm.explore(sug) else openAiSettings() }
                                    .clip(CircleShape).background(p.glassOnSky).border(1.dp, p.glassOnSkyRim, CircleShape)
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            }
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            Column(Modifier.padding(horizontal = Gap.gutter)) {
                if (s.due > 0 || s.saved > 0) {
                    PracticeBanner(s.due, s.saved, openPractice)
                    Spacer(Modifier.height(Gap.lg))
                }
                Segmented(listOf("Words", "Thoughts"), section, { section = it }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(Gap.md))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                    items(if (section == 0) Filter.entries.toList() else listOf(Filter.All, Filter.Saved)) { f ->
                        Chip(f.label, f == filter, { vm.filter.value = f })
                    }
                }
            }
        }
        if (section == 0) {
            if (s.words.isEmpty()) item(span = StaggeredGridItemSpan.FullLine) {
                Empty(
                    if (filter == Filter.All) "No words yet" else "Nothing here yet",
                    if (filter == Filter.Saved) "Save a word you love and it lands here, ready for practice." else "Ask Kai for words about anything above, or open Today.",
                )
            }
            items(s.words, key = { it.id }) { w -> WordTile(w, fresh = w.id in s.explore.freshIds, onClick = { openWord(w.id) }, modifier = Modifier.animateItem().padding(start = if (s.words.indexOf(w) % 2 == 0) Gap.gutter else 0.dp, end = if (s.words.indexOf(w) % 2 == 1) Gap.gutter else 0.dp)) }
        } else {
            if (s.quotes.isEmpty()) item(span = StaggeredGridItemSpan.FullLine) { Empty("No thoughts yet", "Thoughts from Today collect here.") }
            items(s.quotes, key = { it.id }) { q -> QuoteTile(q, onClick = { openQuote(q.id) }, modifier = Modifier.animateItem().padding(start = if (s.quotes.indexOf(q) % 2 == 0) Gap.gutter else 0.dp, end = if (s.quotes.indexOf(q) % 2 == 1) Gap.gutter else 0.dp)) }
        }
    }
}

/** "Ask Kai for words about…": a white field with Kai, who thinks while the words are written. */
@Composable
private fun AskKai(s: LearnState, onExplore: (String) -> Unit, openAiSettings: () -> Unit) {
    val p = K.colors
    var text by rememberSaveable { mutableStateOf("") }
    val focus = LocalFocusManager.current
    fun go() { if (s.hasKey) { onExplore(text); focus.clearFocus(); text = "" } else openAiSettings() }
    Row(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(20.dp, CircleShape, ambientColor = p.shadow, spotColor = p.shadow)
            .clip(CircleShape)
            .background(p.surface)
            .padding(start = 6.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Kai(
            when { s.explore.running -> KaiLooks.Thinking; text.isNotBlank() -> KaiLooks.Wow; !s.hasKey -> KaiLooks.Sleepy; else -> KaiLooks.Curator.copy(gaze = Offset(0.8f, 0f)) },
            size = 52.dp, shadow = false,
        )
        Spacer(Modifier.width(Gap.sm))
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) Text(if (s.explore.running) "Writing words about ${s.explore.topic}…" else "Ask me for words about…", style = K.type.body, color = p.inkFaint, maxLines = 1, overflow = TextOverflow.Ellipsis)
            BasicTextField(
                text, { text = it.take(80) }, singleLine = true, enabled = !s.explore.running,
                textStyle = K.type.body.copy(color = p.ink), cursorBrush = SolidColor(p.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { go() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        val ready = text.isNotBlank()
        val sc by animateFloatAsState(if (ready) 1f else 0.85f, Motion.bloom(), label = "go")
        Box(
            Modifier
                .size(46.dp)
                .graphicsLayer { scaleX = sc; scaleY = sc }
                .pressable(enabled = !s.explore.running && (ready || !s.hasKey), label = "Ask Kai") { go() }
                .clip(CircleShape)
                .background(if (ready) p.accent else p.surfaceSoft),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.ArrowUp, null, tint = if (ready) p.onAccent else p.inkFaint, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
private fun PracticeBanner(due: Int, saved: Int, onStart: () -> Unit) {
    val p = K.colors
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(18.dp, Radius.card, ambientColor = p.shadow, spotColor = p.shadow)
            .clip(Radius.card)
            .background(Candy.Navy)
            .pressable(scale = 0.985f, onClick = onStart)
            .padding(Gap.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(width = 76.dp, height = 70.dp)) {
            listOf(Candy.Mint to -12f, Candy.Lilac to 8f, Candy.Sun to -2f).forEachIndexed { i, (c, r) ->
                Box(Modifier.align(Alignment.Center).size(width = 46.dp, height = 60.dp).graphicsLayer { rotationZ = r; translationX = (i - 1) * 11f * density }.clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp)).background(c))
            }
            Text("Aa", style = K.type.serifLarge.copy(fontSize = 20.sp), color = Candy.Navy, modifier = Modifier.align(Alignment.Center))
        }
        Spacer(Modifier.width(Gap.lg))
        Column(Modifier.weight(1f)) {
            Text(if (due > 0) "$due ${if (due == 1) "word wants" else "words want"} a hello" else "Play with your saved words", style = K.type.title, color = Color.White)
            Text(if (due > 0) "Spaced out so they stick. Two minutes." else "$saved saved and ready.", style = K.type.caption, color = Color.White.copy(alpha = 0.7f))
        }
        Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            Icon(Icons.Arrow, null, tint = Candy.Navy, modifier = Modifier.size(20.dp))
        }
    }
}

/** A word tile: a little gouache painting on top (r19), the word in white over it, the meaning below. */
@Composable
private fun WordTile(w: WordEntity, fresh: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    val t = categoryTint(w.category)
    Column(
        modifier
            .fillMaxWidth()
            .pressable(scale = 0.96f, haptic = false, onClick = onClick)
            .shadow(10.dp, Radius.tile, ambientColor = p.shadow, spotColor = p.shadow)
            .clip(Radius.tile)
            .background(p.surface)
    ) {
        Box(Modifier.fillMaxWidth().height(if (w.word.length > 9) 104.dp else 120.dp).painted(w.id.hashCode(), Paints.forCategory(w.category), detail = 0.7f)) {
            Box(Modifier.matchParentSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f)))))
            Row(Modifier.align(Alignment.TopStart).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(Topics.wordLabel(w.category), style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = Color.White, modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.22f)).padding(horizontal = 8.dp, vertical = 3.dp))
            }
            if (w.saved || fresh) Box(Modifier.align(Alignment.TopEnd).padding(8.dp).size(26.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Icon(if (w.saved) Icons.HeartOn else Icons.Sparkle, null, tint = if (w.saved) Candy.Coral else p.accent, modifier = Modifier.size(14.dp))
            }
            Text(
                w.word, style = K.type.serifLarge.copy(fontSize = if (w.word.length > 10) 24.sp else 30.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold), color = Color.White,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, end = 10.dp, bottom = 8.dp).shared("word-${w.id}"),
            )
        }
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp)) {
            Text(w.partOfSpeech, style = K.type.caption.copy(fontFamily = com.kairos.app.ui.theme.Serif, fontStyle = FontStyle.Italic, fontSize = 13.sp), color = p.inkSoft)
            Spacer(Modifier.height(2.dp))
            Text(w.definition, style = K.type.label.copy(fontWeight = FontWeight.Normal), color = p.ink, maxLines = 4, overflow = TextOverflow.Ellipsis)
            if (w.inPractice) {
                Spacer(Modifier.height(Gap.sm))
                Text(if (w.mastered) "Remembered" else "Learning", style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = p.tintInk(t), modifier = Modifier.clip(CircleShape).background(p.tint(t)).padding(horizontal = 9.dp, vertical = 3.dp))
            }
        }
    }
}

@Composable
private fun QuoteTile(q: QuoteEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    val t = themeTint(q.theme)
    Column(
        modifier
            .fillMaxWidth()
            .pressable(scale = 0.96f, haptic = false, onClick = onClick)
            .clip(Radius.tile)
            .background(p.tint(t))
            .padding(Gap.lg)
    ) {
        Text("“", style = K.type.hero.copy(fontSize = 54.sp, lineHeight = 36.sp), color = p.candy(t))
        Text(q.text, style = K.type.quote.copy(fontSize = 19.sp, lineHeight = 25.sp), color = p.ink)
        Spacer(Modifier.height(Gap.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(q.author, style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = p.tintInk(t), modifier = Modifier.weight(1f), maxLines = 2)
            if (q.saved) Icon(Icons.HeartOn, null, tint = Candy.Coral, modifier = Modifier.size(14.dp))
        }
    }
}
