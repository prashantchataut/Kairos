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
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.db.BookEntity
import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import com.kairos.app.engine.Topics
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.components.BookCover
import com.kairos.app.ui.components.Chip
import com.kairos.app.ui.components.DashedBox
import com.kairos.app.ui.components.Empty
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.LanguageStamp
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.Squiggle
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.nav.shared
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Display
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radius
import com.kairos.app.ui.theme.Serif
import com.kairos.app.ui.theme.Tint
import com.kairos.app.ui.theme.candy
import com.kairos.app.ui.theme.tint
import com.kairos.app.ui.theme.tintInk
import com.kairos.app.util.appViewModel

fun categoryTint(category: String) = when (category) {
    "feelings" -> Tint.Rose; "mind" -> Tint.Lavender; "nature" -> Tint.Mint; "connection" -> Tint.Peach
    "craft" -> Tint.Butter; "wonder" -> Tint.Sky; "courage" -> Tint.Mint; "science" -> Tint.Sky; "wisdom" -> Tint.Butter; else -> Tint.Lavender
}

fun themeTint(theme: String) = when (theme) {
    "hope", "joy", "lightness" -> Tint.Butter; "courage" -> Tint.Peach; "kindness", "love" -> Tint.Rose; "growth" -> Tint.Mint
    "calm", "comfort" -> Tint.Sky; else -> Tint.Lavender
}

private val Suggestions = listOf("old Greek ideas", "the ocean", "kindness", "light and colour", "cosy evenings", "starting over", "music", "the night sky")

private const val WORDS = 0
private const val THOUGHTS = 1
private const val BOOKS = 2

@Composable
fun LearnScreen(openWord: (String) -> Unit, openQuote: (String) -> Unit, openPractice: () -> Unit, openAiSettings: () -> Unit, openBook: (String) -> Unit = {}, startSection: Int = WORDS) {
    val vm = appViewModel { LearnViewModel(it) }
    val s by vm.state.collectAsStateWithLifecycle()
    val d by vm.discover.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()
    var section by rememberSaveable { mutableIntStateOf(startSection) }
    val p = K.colors

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Gap.gutter, end = Gap.gutter, bottom = 130.dp),
        horizontalArrangement = Arrangement.spacedBy(Gap.md),
        verticalItemSpacing = Gap.md,
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            Header(s, section, { section = it }, vm::explore, openAiSettings)
        }
        item(span = StaggeredGridItemSpan.FullLine) {
            Column {
                if (section != BOOKS && (s.due > 0 || s.saved > 0)) {
                    PracticeBanner(s.due, s.saved, openPractice)
                    Spacer(Modifier.height(Gap.lg))
                }
                when (section) {
                    WORDS -> FreshWords(d, { vm.seenWord(it); openWord(it) }, vm::refreshDiscover)
                    THOUGHTS -> FreshThoughts(d, { vm.seenQuote(it); openQuote(it) }, vm::refreshDiscover)
                    else -> {}
                }
                if (section != BOOKS) {
                    Spacer(Modifier.height(Gap.xl))
                    Text(if (section == WORDS) "Your library" else "Thoughts you've met", style = K.type.display.copy(fontSize = 24.sp), color = p.ink)
                    Spacer(Modifier.height(Gap.sm))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                        items(if (section == WORDS) Filter.entries.toList() else listOf(Filter.All, Filter.Saved)) { f -> Chip(f.label, f == filter, { vm.filter.value = f }) }
                    }
                }
            }
        }
        when (section) {
            WORDS -> {
                if (s.words.isEmpty()) item(span = StaggeredGridItemSpan.FullLine) {
                    Empty(if (filter == Filter.All) "No words yet" else "Nothing here yet", if (filter == Filter.Saved) "Save a word you love and it lands here, ready for practice." else "Open one from Fresh for you above, or ask Kai for words about anything.")
                }
                itemsIndexed(s.words, key = { _, w -> w.id }) { i, w -> WordTile(w, i, fresh = w.id in s.explore.freshIds, onClick = { openWord(w.id) }, modifier = Modifier.animateItem()) }
            }
            THOUGHTS -> {
                if (s.quotes.isEmpty()) item(span = StaggeredGridItemSpan.FullLine) { Empty("No thoughts yet", "Thoughts from Today and Fresh for you collect here.") }
                itemsIndexed(s.quotes, key = { _, q -> q.id }) { i, q -> QuoteTile(q, i, onClick = { openQuote(q.id) }, modifier = Modifier.animateItem()) }
            }
            else -> item(span = StaggeredGridItemSpan.FullLine) { Books(s.books, d, vm::moreBooks, openBook) }
        }
    }
}

@Composable
private fun Header(s: LearnState, section: Int, onSection: (Int) -> Unit, onExplore: (String) -> Unit, openAiSettings: () -> Unit) {
    val p = K.colors
    Column(Modifier.bleed(Gap.gutter).fillMaxWidth().sky().windowInsetsPadding(WindowInsets.statusBars).padding(top = Gap.lg, bottom = Gap.xxl)) {
        Column(Modifier.padding(horizontal = Gap.gutter)) {
            AnimatedContent(section, transitionSpec = { (fadeIn(Motion.fade(240))) togetherWith fadeOut(Motion.fade(120)) }, label = "title") { sec ->
                Column {
                    Text(listOf("Your words", "Your thoughts", "Your books")[sec], style = K.type.shout.copy(fontSize = 40.sp), color = p.onSky)
                    Text(
                        when (sec) {
                            WORDS -> if (s.total == 0) "They'll gather here, one day at a time." else "${s.total} written just for you, ${s.remembered} remembered for good."
                            THOUGHTS -> "${s.quotes.size} lines worth keeping, from writers, poets, proverbs and Kai."
                            else -> "Micro-summaries of books worth knowing. Three minutes each."
                        },
                        style = K.type.body, color = p.onSkySoft,
                    )
                }
            }
            Spacer(Modifier.height(Gap.lg))
            Tabs(section, onSection)
            if (section == WORDS) {
                Spacer(Modifier.height(Gap.lg))
                AskKai(s, onExplore, openAiSettings)
            }
        }
        if (section == WORDS) {
            Spacer(Modifier.height(Gap.md))
            AnimatedContent(s.explore.message, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "exMsg") { m ->
                if (m != null) Text(m, style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = p.onSky, modifier = Modifier.padding(horizontal = Gap.gutter))
                else LazyRow(contentPadding = PaddingValues(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                    items(Suggestions) { sug ->
                        Text(
                            sug, style = K.type.label, color = p.onSky,
                            modifier = Modifier.pressable(scale = 0.92f) { if (s.hasKey) onExplore(sug) else openAiSettings() }
                                .clip(CircleShape).background(p.glassOnSky).border(1.dp, p.glassOnSkyRim, CircleShape).padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Glass tabs on the sky with a white pill that slides between Words, Thoughts and Books. */
@Composable
private fun Tabs(section: Int, onSection: (Int) -> Unit) {
    val p = K.colors
    val x by animateFloatAsState(section.toFloat(), Motion.soft(), label = "tab")
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth().height(48.dp).clip(CircleShape).background(p.glassOnSky).border(1.dp, p.glassOnSkyRim, CircleShape).padding(4.dp)) {
        val w = maxWidth / 3
        Box(Modifier.graphicsLayer { translationX = x * w.toPx() }.width(w).fillMaxSize().clip(CircleShape).background(Color.White))
        Row(Modifier.fillMaxSize()) {
            listOf("Words", "Thoughts", "Books").forEachIndexed { i, label ->
                Box(Modifier.weight(1f).fillMaxSize().pressable(scale = 0.95f) { onSection(i) }, contentAlignment = Alignment.Center) {
                    Text(label, style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = if (i == section) Candy.Navy else p.onSky)
                }
            }
        }
    }
}

/** "Ask Kai for words about…": a white field with Kai, who perks up as you type and thinks while the words are written. */
@Composable
private fun AskKai(s: LearnState, onExplore: (String) -> Unit, openAiSettings: () -> Unit) {
    val p = K.colors
    var text by rememberSaveable { mutableStateOf("") }
    val focus = LocalFocusManager.current
    fun go() { if (s.hasKey) { onExplore(text); focus.clearFocus(); text = "" } else openAiSettings() }
    Row(
        Modifier.fillMaxWidth().height(62.dp).shadow(20.dp, CircleShape, ambientColor = p.shadow, spotColor = p.shadow).clip(CircleShape).background(p.surface).padding(start = 6.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Kai(when { s.explore.running -> KaiLooks.Thinking; text.isNotBlank() -> KaiLooks.Wow; !s.hasKey -> KaiLooks.Sleepy; else -> KaiLooks.Curator.copy(gaze = Offset(0.8f, 0f)) }, size = 50.dp, shadow = false)
        Spacer(Modifier.width(Gap.sm))
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) Text(if (s.explore.running) "Writing words about ${s.explore.topic}…" else "Ask me for words about…", style = K.type.body, color = p.inkFaint, maxLines = 1, overflow = TextOverflow.Ellipsis)
            BasicTextField(
                text, { text = it.take(80) }, singleLine = true, enabled = !s.explore.running,
                textStyle = K.type.body.copy(color = p.ink), cursorBrush = SolidColor(p.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { go() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        val ready = text.isNotBlank()
        val sc by animateFloatAsState(if (ready) 1f else 0.85f, Motion.bloom(), label = "go")
        Box(
            Modifier.size(44.dp).graphicsLayer { scaleX = sc; scaleY = sc }.pressable(enabled = !s.explore.running && (ready || !s.hasKey), label = "Ask Kai") { go() }
                .clip(CircleShape).background(if (ready) p.accent else p.surfaceSoft),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.ArrowUp, null, tint = if (ready) p.onAccent else p.inkFaint, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
private fun PracticeBanner(due: Int, saved: Int, onStart: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().shadow(18.dp, Radius.card).clip(Radius.card).background(Candy.Navy).pressable(scale = 0.985f, onClick = onStart).padding(Gap.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(width = 76.dp, height = 70.dp)) {
            listOf(Candy.Mint to -12f, Candy.Lilac to 8f, Candy.Sun to -2f).forEachIndexed { i, (c, r) ->
                Box(Modifier.align(Alignment.Center).size(width = 46.dp, height = 60.dp).graphicsLayer { rotationZ = r; translationX = (i - 1) * 11f * density }.clip(RoundedCornerShape(10.dp)).background(c))
            }
            Text("Aa", style = K.type.serifLarge.copy(fontSize = 20.sp), color = Candy.Navy, modifier = Modifier.align(Alignment.Center))
        }
        Spacer(Modifier.width(Gap.lg))
        Column(Modifier.weight(1f)) {
            Text(if (due > 0) "$due ${if (due == 1) "word wants" else "words want"} a hello" else "Play with your words", style = K.type.title, color = Color.White)
            Text(if (due > 0) "Spaced out so they stick. Two minutes." else "$saved saved, ready whenever you are.", style = K.type.caption, color = Color.White.copy(alpha = 0.7f))
        }
        Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Icon(Icons.Arrow, null, tint = Candy.Navy, modifier = Modifier.size(20.dp)) }
    }
}

// --- Fresh for you -----------------------------------------------------------------------------

@Composable
private fun FreshHeader(title: String, sub: String, loading: Boolean, onMore: () -> Unit) {
    val p = K.colors
    Row(verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(title, style = K.type.display.copy(fontSize = 24.sp), color = p.ink)
            Text(sub, style = K.type.caption, color = p.inkSoft)
        }
        Text(if (loading) "Writing…" else "Refresh", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = p.accent, modifier = Modifier.pressable(enabled = !loading, onClick = onMore).padding(Gap.sm))
    }
}

/** An endless strip of unseen words, ranked for you and kept varied. Opening one moves it into your library. */
@Composable
private fun FreshWords(d: Discover, open: (String) -> Unit, more: () -> Unit) {
    FreshHeader("Fresh for you", "No limits: Kai writes more as you go.", d.loading, more)
    Spacer(Modifier.height(Gap.md))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Gap.md), contentPadding = PaddingValues(end = Gap.gutter)) {
        items(d.words, key = { it.item.id }) { pick -> FreshWordCard(pick.item, pick.reason, Modifier.animateItem()) { open(pick.item.id) } }
        item { MoreCard(d.loading, more) }
    }
}

@Composable
private fun FreshThoughts(d: Discover, open: (String) -> Unit, more: () -> Unit) {
    FreshHeader("Fresh for you", "Real lines, proverbs and ones Kai wrote for you.", d.loading, more)
    Spacer(Modifier.height(Gap.md))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Gap.md), contentPadding = PaddingValues(end = Gap.gutter)) {
        items(d.quotes, key = { it.item.id }) { pick -> FreshThoughtCard(pick.item, Modifier.animateItem()) { open(pick.item.id) } }
        item { MoreCard(d.loading, more) }
    }
}

@Composable
private fun FreshWordCard(w: WordEntity, reason: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val p = K.colors
    val t = categoryTint(w.category)
    Column(
        modifier.size(width = 168.dp, height = 210.dp).pressable(scale = 0.95f, onClick = onClick).clip(Radius.tile).background(p.candy(t)).padding(Gap.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (w.language.isNotBlank()) w.language else Topics.wordLabel(w.category), style = K.type.caption.copy(fontWeight = FontWeight.Bold), color = Color.White.copy(alpha = 0.9f), modifier = Modifier.weight(1f))
            Icon(Icons.Sparkle, null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.weight(1f))
        com.kairos.app.ui.components.FitText(w.word, K.type.hero.copy(fontWeight = FontWeight.SemiBold), Color.White, Modifier.fillMaxWidth().height(64.dp), max = 40.sp, min = 18.sp, lineHeight = 1f, contentAlignment = Alignment.BottomStart)
        Text(w.pronunciation, style = K.type.caption, color = Color.White.copy(alpha = 0.85f), maxLines = 1)
        Spacer(Modifier.height(Gap.sm))
        Text(reason, style = K.type.caption, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun FreshThoughtCard(q: QuoteEntity, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val p = K.colors
    val t = themeTint(q.theme)
    Column(modifier.size(width = 220.dp, height = 210.dp).pressable(scale = 0.95f, onClick = onClick).clip(Radius.tile).background(p.tint(t)).padding(Gap.lg)) {
        Text(if (q.isLine) "for you" else Topics.themeLabel(q.theme), style = K.type.caption.copy(fontWeight = FontWeight.Bold), color = p.tintInk(t))
        Spacer(Modifier.height(Gap.sm))
        com.kairos.app.ui.components.FitText(q.text, K.type.quote, p.ink, Modifier.fillMaxWidth().weight(1f), max = 22.sp, min = 12.sp, lineHeight = 1.2f)
        Spacer(Modifier.height(Gap.sm))
        Text(if (q.isLine) "written for you" else q.author, style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = p.tintInk(t), maxLines = 1)
    }
}

@Composable
private fun MoreCard(loading: Boolean, more: () -> Unit) {
    val p = K.colors
    DashedBox(p.inkFaint, Modifier.size(width = 140.dp, height = 210.dp).pressable(enabled = !loading, onClick = more)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Kai(if (loading) KaiLooks.Thinking else KaiLooks.Curator, size = 54.dp, shadow = false)
            Spacer(Modifier.height(Gap.sm))
            Text(if (loading) "Writing more…" else "More, please", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = p.ink)
        }
    }
}

// --- Library tiles -----------------------------------------------------------------------------

/**
 * Library tiles take turns between four typographic personalities (after r05's bento): a pastel tile
 * with the word in Fraunces, a night tile with the word in italic, a white tile with a big grotesk word
 * and a squiggle, and a bold candy tile. No pictures: the word is the picture.
 */
@Composable
private fun WordTile(w: WordEntity, index: Int, fresh: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    val t = categoryTint(w.category)
    val style = (index + w.word.length) % 4
    val (bg, ink, soft) = when (style) {
        0 -> Triple(p.tint(t), p.ink, p.tintInk(t))
        1 -> Triple(Candy.Navy, Color.White, Color.White.copy(alpha = 0.7f))
        2 -> Triple(p.surface, p.ink, p.inkSoft)
        else -> Triple(p.candy(t), Color.White, Color.White.copy(alpha = 0.85f))
    }
    Column(
        modifier.fillMaxWidth().pressable(scale = 0.96f, haptic = false, onClick = onClick).clip(Radius.tile).background(bg)
            .then(if (style == 2) Modifier.border(1.dp, p.line, Radius.tile) else Modifier).padding(Gap.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(Topics.wordLabel(w.category), style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = soft, modifier = Modifier.weight(1f))
            when {
                w.saved -> Icon(Icons.HeartOn, null, tint = if (style == 0 || style == 2) Candy.Coral else Color.White, modifier = Modifier.size(14.dp))
                fresh -> Icon(Icons.Sparkle, null, tint = soft, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(if (style == 2) Gap.md else Gap.xl))
        val wordStyle = when (style) {
            1 -> K.type.hero.copy(fontStyle = FontStyle.Italic)
            2 -> K.type.shout.copy(letterSpacing = (-0.04).sp)
            3 -> K.type.display.copy(fontWeight = FontWeight.ExtraBold, fontFamily = Display)
            else -> K.type.hero.copy(fontWeight = FontWeight.SemiBold)
        }
        val size = when { w.word.length > 12 -> 22.sp; w.word.length > 9 -> 26.sp; else -> 32.sp }
        Text(w.word, style = wordStyle.copy(fontSize = size, lineHeight = size * 1.02f), color = ink, modifier = Modifier.shared("word-${w.id}"))
        if (style == 2) Squiggle(p.candy(t), Modifier.padding(top = 4.dp).size(width = 70.dp, height = 8.dp))
        Spacer(Modifier.height(4.dp))
        Text(w.partOfSpeech, style = K.type.caption.copy(fontFamily = Serif, fontStyle = FontStyle.Italic, fontSize = 13.sp), color = soft)
        Spacer(Modifier.height(Gap.sm))
        Text(w.definition, style = K.type.label.copy(fontWeight = FontWeight.Normal), color = ink.copy(alpha = 0.85f), maxLines = 4, overflow = TextOverflow.Ellipsis)
        if (w.language.isNotBlank() || w.inPractice) {
            Spacer(Modifier.height(Gap.md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (w.language.isNotBlank()) LanguageStamp(w.language, soft)
                Spacer(Modifier.weight(1f))
                if (w.inPractice) Text(if (w.mastered) "Remembered" else "Learning", style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = soft)
            }
        }
    }
}

@Composable
private fun QuoteTile(q: QuoteEntity, index: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = K.colors
    val t = themeTint(q.theme)
    val dark = index % 3 == 1
    val bg = if (dark) Candy.Navy else p.tint(t)
    val ink = if (dark) Color.White else p.ink
    Column(modifier.fillMaxWidth().pressable(scale = 0.96f, haptic = false, onClick = onClick).clip(Radius.tile).background(bg).padding(Gap.lg)) {
        Text("“", style = K.type.hero.copy(fontSize = 54.sp, lineHeight = 36.sp), color = p.candy(t))
        Text(q.text, style = (if (q.isLine) K.type.body.copy(fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 23.sp) else K.type.quote.copy(fontSize = 19.sp, lineHeight = 25.sp)), color = ink)
        Spacer(Modifier.height(Gap.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (q.isLine) "written for you" else q.author, style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = if (dark) Color.White.copy(alpha = 0.7f) else p.tintInk(t), modifier = Modifier.weight(1f), maxLines = 2)
            if (q.saved) Icon(Icons.HeartOn, null, tint = Candy.Coral, modifier = Modifier.size(14.dp))
        }
    }
}

// --- Books -------------------------------------------------------------------------------------

/** Books (n17): a glass shelf of typographic covers, then the full list with summaries. */
@Composable
private fun Books(books: List<BookEntity>, d: Discover, more: () -> Unit, open: (String) -> Unit) {
    val p = K.colors
    Column {
        Text(if (books.isEmpty()) "Your shelf is waiting" else "${books.size} books worth knowing", style = K.type.shout.copy(fontSize = 34.sp, lineHeight = 36.sp), color = p.ink)
        Spacer(Modifier.height(Gap.lg))
        Box(Modifier.fillMaxWidth().height(200.dp)) {
            // The glass shelf the covers stand on.
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(70.dp).clip(RoundedCornerShape(16.dp)).background(Brush.verticalGradient(listOf(p.accent.copy(alpha = 0.12f), p.accent.copy(alpha = 0.22f)))))
            LazyRow(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(Gap.md), contentPadding = PaddingValues(horizontal = Gap.md), verticalAlignment = Alignment.Bottom) {
                items(books.take(12), key = { it.id }) { b ->
                    BookCover(b.title, b.author, b.id, Modifier.padding(bottom = 20.dp).size(width = 110.dp, height = 160.dp).shadow(10.dp, RoundedCornerShape(6.dp)).pressable(scale = 0.95f) { open(b.id) })
                }
                item {
                    DashedBox(p.inkFaint, Modifier.padding(bottom = 20.dp).size(width = 110.dp, height = 160.dp).pressable(enabled = !d.writingBooks, onClick = more)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Kai(if (d.writingBooks) KaiLooks.Thinking else KaiLooks.Curator, size = 44.dp, shadow = false)
                            Text(if (d.writingBooks) "Choosing…" else "More books", style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = p.ink)
                        }
                    }
                }
            }
        }
        d.bookMessage?.let { Spacer(Modifier.height(Gap.sm)); Text(it, style = K.type.label, color = p.inkSoft) }
        if (books.isEmpty()) {
            Spacer(Modifier.height(Gap.lg))
            PrimaryButton(if (d.writingBooks) "Choosing books for you…" else "Find me books", more, Modifier.fillMaxWidth(), icon = Icons.Sparkle, enabled = !d.writingBooks, dark = true)
        }
        Spacer(Modifier.height(Gap.xl))
        books.forEach { b -> BookRow(b) { open(b.id) }; Spacer(Modifier.height(Gap.md)) }
    }
}

@Composable
private fun BookRow(b: BookEntity, onClick: () -> Unit) {
    val p = K.colors
    Row(Modifier.fillMaxWidth().pressable(scale = 0.98f, onClick = onClick).clip(Radius.tile).background(p.surface).border(1.dp, p.line, Radius.tile).padding(Gap.md), verticalAlignment = Alignment.CenterVertically) {
        BookCover(b.title, b.author, b.id, Modifier.size(width = 62.dp, height = 90.dp))
        Spacer(Modifier.width(Gap.lg))
        Column(Modifier.weight(1f)) {
            Text(b.title, style = K.type.title, color = p.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(b.author + if (b.year > 0) ", ${b.year}" else "", style = K.type.caption, color = p.inkSoft, maxLines = 1)
            Spacer(Modifier.height(Gap.xs))
            Text(b.summary, style = K.type.label.copy(fontWeight = FontWeight.Normal), color = p.ink.copy(alpha = 0.8f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(Gap.xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${b.minutes} min · ${Topics.bookLabel(b.category)}", style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = p.accent)
                Spacer(Modifier.weight(1f))
                if (b.saved) Icon(Icons.HeartOn, null, tint = Candy.Coral, modifier = Modifier.size(14.dp))
            }
        }
    }
}

/** Lets a full-width block ignore the grid's side padding, so the sky reaches the screen edges. */
private fun Modifier.bleed(by: androidx.compose.ui.unit.Dp) = layout { m, c ->
    val extra = by.roundToPx() * 2
    val pl = m.measure(c.copy(minWidth = c.minWidth + extra, maxWidth = c.maxWidth + extra))
    layout(c.maxWidth, pl.height) { pl.place(-by.roundToPx(), 0) }
}
