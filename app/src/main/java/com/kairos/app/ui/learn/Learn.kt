package com.kairos.app.ui.learn

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.engine.WordItem
import com.kairos.app.engine.learn.Stage
import com.kairos.app.ui.components.DetailTopBar
import com.kairos.app.ui.components.EmptyState
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.components.HeartButton
import com.kairos.app.ui.components.PillLabel
import com.kairos.app.ui.components.ScreenHeader
import com.kairos.app.ui.components.SectionTitle
import com.kairos.app.ui.components.clickableNoRipple
import com.kairos.app.ui.components.enter
import com.kairos.app.ui.components.kairosViewModel
import com.kairos.app.ui.components.screenPadding
import com.kairos.app.ui.components.shareText
import com.kairos.app.ui.glass.ButtonTone
import com.kairos.app.ui.glass.GlassCard
import com.kairos.app.ui.glass.GlassIconButton
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.KButton
import com.kairos.app.ui.glass.KChip
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.nav.Routes
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WordFilter(val label: String) { Deck("My garden"), Saved("Saved"), Fresh("Unmet"), All("Everything") }

class LearnViewModel(private val c: AppContainer) : ViewModel() {
    val words = c.learn.observeWords().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val due = c.learn.observeDueCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    fun toggle(id: String) = viewModelScope.launch { c.learn.toggleSave(id) }
}

@Composable
fun LearnScreen() {
    val vm = kairosViewModel { LearnViewModel(it) }
    val words by vm.words.collectAsStateWithLifecycle()
    val due by vm.due.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    var filter by remember { mutableStateOf(WordFilter.Deck) }
    var query by remember { mutableStateOf("") }
    val colors = Kairos.colors

    val deck = words.filter { it.inDeck }
    val stages = Stage.entries.associateWith { s -> deck.count { it.stage == s } }
    val shown = words
        .filter {
            when (filter) {
                WordFilter.Deck -> it.inDeck
                WordFilter.Saved -> it.saved
                WordFilter.Fresh -> (it.progress?.timesSeen ?: 0) == 0 && !it.inDeck
                WordFilter.All -> true
            }
        }
        .filter { query.isBlank() || it.word.word.contains(query.trim(), true) || it.word.definition.contains(query.trim(), true) }
        .sortedBy { it.word.word.lowercase() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = screenPadding()) {
        item("h") {
            ScreenHeader("Your word garden", "Learn", trailing = {
                GlassIconButton(Icons.Rounded.Search, "Look up any word", { nav.open(Routes.LOOKUP) })
            })
        }
        item("hero") {
            GlassCard(Modifier.fillMaxWidth().enter(0), blur = true, weight = GlassWeight.Frosted, shape = Radii.sheet) {
                Column {
                    Eyebrow(if (due > 0) "Ready to revisit" else "Garden is watered")
                    Spacer(Modifier.height(Space.sm))
                    Text(
                        when {
                            due > 0 -> "$due word${if (due > 1) "s are" else " is"} ready to come back to you"
                            deck.isEmpty() -> "Plant your first words"
                            else -> "Nothing due. Want a fresh handful?"
                        },
                        style = Kairos.type.title, color = colors.ink,
                    )
                    Spacer(Modifier.height(Space.lg))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                        Stage.entries.forEach { s -> StagePill(s, stages[s] ?: 0, Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(Space.lg))
                    KButton(if (due > 0) "Start a round" else "Learn something new", { nav.open(Routes.practice()) }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item("quotes") {
            GlassCard(Modifier.fillMaxWidth().padding(top = Space.md).enter(1), onClick = { nav.open(Routes.QUOTES) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).background(colors.lilac, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.FormatQuote, null, tint = colors.ink)
                    }
                    Spacer(Modifier.width(Space.md))
                    Column(Modifier.weight(1f)) {
                        Text("Thought library", style = Kairos.type.heading.copy(fontSize = 17.sp), color = colors.ink)
                        Text("Every quote, by theme. Keep the ones that land.", style = Kairos.type.bodyRegular, color = colors.inkSoft)
                    }
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = colors.inkSoft)
                }
            }
        }
        item("filters") {
            SectionTitle("Words")
            SearchField(query, { query = it }, "Search your words")
            Spacer(Modifier.height(Space.md))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                WordFilter.entries.forEach { f -> KChip(f.label, f == filter, { filter = f }) }
            }
            Spacer(Modifier.height(Space.md))
        }
        if (shown.isEmpty()) {
            item("empty") {
                EmptyState(
                    null,
                    when (filter) {
                        WordFilter.Deck -> "Your garden is empty"
                        WordFilter.Saved -> "Nothing saved yet"
                        else -> "No words match"
                    },
                    when (filter) {
                        WordFilter.Deck -> "Tap “Keep it” on today's word, or start a round to plant a few."
                        WordFilter.Saved -> "Tap the heart on any word you love. Saved words shape what Kairos picks for you."
                        else -> "Try a different search, or look the word up online."
                    },
                )
            }
        }
        items(shown, key = { it.word.id }) { item ->
            WordRow(item, onClick = { nav.open(Routes.word(item.word.id)) }, onSave = { vm.toggle(item.word.id) }, modifier = Modifier.padding(bottom = Space.sm).animateItem())
        }
    }
}

@Composable
fun SearchField(value: String, onChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier, onSubmit: (() -> Unit)? = null) {
    val colors = Kairos.colors
    Row(
        modifier.fillMaxWidth().liquidGlass(Radii.pill, GlassWeight.Frosted).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, null, tint = colors.inkSoft, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, style = Kairos.type.body, color = colors.inkFaint)
            BasicTextField(
                value, onChange, singleLine = true,
                textStyle = Kairos.type.body.copy(color = colors.ink),
                cursorBrush = SolidColor(colors.pond),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { onSubmit?.invoke() }),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = placeholder },
            )
        }
    }
}

@Composable
private fun StagePill(stage: Stage, count: Int, modifier: Modifier = Modifier) {
    val colors = Kairos.colors
    Column(
        modifier.liquidGlass(Radii.card, GlassWeight.Clear).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GrowthGlyph(stage, Modifier.size(30.dp))
        Text("$count", style = Kairos.type.heading, color = colors.ink)
        Text(stage.label, style = Kairos.type.caption.copy(letterSpacing = 0.sp), color = colors.inkSoft)
    }
}

/** Seed → sprout → bloom → rooted tree. Painted, not iconified. */
@Composable
fun GrowthGlyph(stage: Stage, modifier: Modifier = Modifier) {
    val leaf = Color(0xFF6CC28A)
    val stem = Color(0xFF4E9E68)
    val petal = Color(0xFFFF9FB5)
    val soil = Color(0xFFC9A27E)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawOval(soil.copy(alpha = 0.7f), topLeft = Offset(w * 0.2f, h * 0.82f), size = Size(w * 0.6f, h * 0.14f))
        when (stage) {
            Stage.Seed -> drawOval(Color(0xFFB5865C), topLeft = Offset(w * 0.4f, h * 0.68f), size = Size(w * 0.2f, h * 0.16f))
            Stage.Sprout -> {
                drawLine(stem, Offset(w / 2, h * 0.86f), Offset(w / 2, h * 0.5f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
                drawOval(leaf, topLeft = Offset(w * 0.22f, h * 0.42f), size = Size(w * 0.3f, h * 0.16f))
                drawOval(leaf, topLeft = Offset(w * 0.5f, h * 0.36f), size = Size(w * 0.3f, h * 0.16f))
            }
            Stage.Bloom -> {
                drawLine(stem, Offset(w / 2, h * 0.86f), Offset(w / 2, h * 0.38f), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
                drawOval(leaf, topLeft = Offset(w * 0.2f, h * 0.56f), size = Size(w * 0.3f, h * 0.15f))
                repeat(5) { i ->
                    val a = i / 5f * 6.283f
                    drawCircle(petal, w * 0.1f, Offset(w / 2 + kotlin.math.cos(a) * w * 0.13f, h * 0.3f + kotlin.math.sin(a) * w * 0.13f))
                }
                drawCircle(Color(0xFFFFD45E), w * 0.08f, Offset(w / 2, h * 0.3f))
            }
            Stage.Rooted -> {
                drawLine(Color(0xFF8B6B4E), Offset(w / 2, h * 0.88f), Offset(w / 2, h * 0.5f), strokeWidth = w * 0.1f, cap = StrokeCap.Round)
                drawCircle(leaf, w * 0.26f, Offset(w / 2, h * 0.34f))
                drawCircle(leaf.copy(alpha = 0.85f), w * 0.18f, Offset(w * 0.3f, h * 0.45f))
                drawCircle(leaf.copy(alpha = 0.85f), w * 0.18f, Offset(w * 0.7f, h * 0.45f))
                drawCircle(Color.White.copy(alpha = 0.5f), w * 0.05f, Offset(w * 0.42f, h * 0.26f))
            }
        }
    }
}

@Composable
private fun WordRow(item: WordItem, onClick: () -> Unit, onSave: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Kairos.colors
    GlassCard(modifier.fillMaxWidth(), onClick = onClick, contentPadding = PaddingValues(start = 18.dp, end = 10.dp, top = 14.dp, bottom = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GrowthGlyph(item.stage, Modifier.size(34.dp))
            Spacer(Modifier.width(Space.md))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(item.word.word, style = Kairos.type.title.copy(fontSize = 21.sp, lineHeight = 25.sp), color = colors.ink)
                    Spacer(Modifier.width(8.dp))
                    Text(item.word.pos, style = Kairos.type.caption.copy(fontStyle = FontStyle.Italic, letterSpacing = 0.sp), color = colors.inkSoft, modifier = Modifier.padding(bottom = 3.dp))
                }
                Text(item.word.definition, style = Kairos.type.bodyRegular.copy(fontSize = 14.sp, lineHeight = 19.sp), color = colors.inkSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            HeartButton(item.saved, onSave, size = 42.dp, label = "Save ${item.word.word}")
        }
    }
}

class WordViewModel(private val c: AppContainer, private val id: String) : ViewModel() {
    val item = c.learn.observeWord(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    fun toggle() = viewModelScope.launch { c.learn.toggleSave(id) }
    fun plant() = viewModelScope.launch { c.learn.addToDeck(id) }
    fun speak(w: String) = c.speaker.say(w)
    fun remove(done: () -> Unit) = viewModelScope.launch { c.learn.removeCustom(id); done() }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordDetailScreen(id: String) {
    val vm = kairosViewModel(key = "word-$id") { WordViewModel(it, id) }
    val item by vm.item.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val context = LocalContext.current
    val colors = Kairos.colors
    val w = item?.word

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = Space.huge)) {
        item {
            DetailTopBar(onBack = nav::back, actions = {
                if (w != null) {
                    if (w.custom) GlassIconButton(Icons.Rounded.Delete, "Remove word", { vm.remove { nav.back() } })
                    GlassIconButton(Icons.Rounded.Share, "Share", { shareText(context, "${w.word} (${w.pos}) — ${w.definition}\n“${w.example}”\n\nvia Kairos") })
                }
            })
        }
        if (w == null || item == null) return@LazyColumn
        val it = item!!
        item {
            Column(Modifier.padding(horizontal = Space.screen).animateContentSize()) {
                Spacer(Modifier.height(Space.xl))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PillLabel(w.pos)
                    Spacer(Modifier.width(Space.sm))
                    PillLabel(it.stage.label + " · " + it.stage.blurb)
                }
                Spacer(Modifier.height(Space.lg))
                Text(w.word, style = if (w.word.length > 11) Kairos.type.display else Kairos.type.hero.copy(fontSize = 64.sp, lineHeight = 66.sp), color = colors.ink, modifier = Modifier.enter(0))
                if (w.phonetic.isNotBlank()) {
                    Spacer(Modifier.height(Space.md))
                    Row(
                        Modifier.liquidGlass(Radii.pill, GlassWeight.Frosted).clickableNoRipple { vm.speak(w.word) }.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(w.phonetic, style = Kairos.type.body, color = colors.inkSoft)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, "Hear it", tint = colors.pond, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(Space.xl))
                GlassCard(Modifier.fillMaxWidth().enter(1), blur = true, weight = GlassWeight.Frosted, shape = Radii.sheet) {
                    Column {
                        Eyebrow("Meaning")
                        Spacer(Modifier.height(Space.sm))
                        Text(w.definition, style = Kairos.type.body.copy(fontSize = 19.sp, lineHeight = 28.sp), color = colors.ink)
                        if (w.example.isNotBlank()) {
                            Spacer(Modifier.height(Space.lg))
                            Eyebrow("In the wild")
                            Spacer(Modifier.height(Space.sm))
                            Text("“${w.example}”", style = Kairos.type.quote.copy(fontStyle = FontStyle.Italic, fontSize = 19.sp), color = colors.inkSoft)
                        }
                        if (w.origin.isNotBlank()) {
                            Spacer(Modifier.height(Space.lg))
                            Eyebrow("Where it comes from")
                            Spacer(Modifier.height(Space.sm))
                            Text(w.origin, style = Kairos.type.bodyRegular, color = colors.ink)
                        }
                    }
                }
                if (w.synonyms.isNotEmpty()) {
                    SectionTitle("Close cousins")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                        w.synonyms.forEach { s -> PillLabel(s) }
                    }
                }
                SectionTitle("How it's growing")
                GrowthCard(it)
                Spacer(Modifier.height(Space.xl))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                    HeartButton(it.saved, vm::toggle, size = 56.dp, label = "Save word")
                    if (it.inDeck) {
                        KButton("Practice this word", { nav.open(Routes.practice(listOf(w.id))) }, modifier = Modifier.weight(1f))
                    } else {
                        KButton("Plant in my garden", vm::plant, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun GrowthCard(item: WordItem) {
    val colors = Kairos.colors
    val p = item.progress
    val target = when (item.stage) { Stage.Seed -> 0.1f; Stage.Sprout -> 0.38f; Stage.Bloom -> 0.7f; Stage.Rooted -> 1f }
    val fill by animateFloatAsState(target, tween(900), label = "growth")
    GlassCard(Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Stage.entries.forEach { s ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        GrowthGlyph(s, Modifier.size(if (s == item.stage) 44.dp else 30.dp))
                        Text(s.label, style = Kairos.type.caption.copy(letterSpacing = 0.sp), color = if (s == item.stage) colors.ink else colors.inkFaint)
                    }
                }
            }
            Spacer(Modifier.height(Space.md))
            Canvas(Modifier.fillMaxWidth().height(10.dp)) {
                drawRoundRect(colors.ink.copy(alpha = 0.08f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2))
                drawRoundRect(
                    Brush.horizontalGradient(listOf(colors.mint, colors.pond)),
                    size = Size(size.width * fill, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2),
                )
            }
            Spacer(Modifier.height(Space.md))
            Text(
                when {
                    p == null || !p.inDeck -> "Not planted yet. Plant it and Kairos will bring it back right before you'd forget."
                    p.reps == 0 -> "Planted. Your first review is ready whenever you are."
                    else -> "Reviewed ${p.reps} time${if (p.reps > 1) "s" else ""}. Next visit in ${p.intervalDays} day${if (p.intervalDays != 1) "s" else ""}."
                },
                style = Kairos.type.bodyRegular, color = colors.inkSoft,
            )
        }
    }
}
