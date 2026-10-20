package com.kairos.app.ui.screens.learn

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.content.Quote
import com.kairos.app.data.content.Themes
import com.kairos.app.data.content.Word
import com.kairos.app.data.db.QuoteState
import com.kairos.app.data.db.WordProgress
import com.kairos.app.data.today
import com.kairos.app.engine.Growth
import com.kairos.app.engine.SpacedRepetition
import com.kairos.app.ui.kit.AcrylicCard
import com.kairos.app.ui.kit.CornerWash
import com.kairos.app.ui.kit.EmptyNote
import com.kairos.app.ui.kit.InkButton
import com.kairos.app.ui.kit.PipMood
import com.kairos.app.ui.kit.Segmented
import com.kairos.app.ui.kit.SoftButton
import com.kairos.app.ui.kit.SoftChip
import com.kairos.app.ui.kit.Tag
import com.kairos.app.ui.kit.kairosViewModel
import com.kairos.app.ui.kit.rise
import com.kairos.app.ui.kit.squishClick
import com.kairos.app.ui.kit.tabContentPadding
import com.kairos.app.ui.screens.today.HeartButton
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LearnUi(
    val words: List<Word> = emptyList(),
    val quotes: List<Quote> = emptyList(),
    val progress: Map<String, WordProgress> = emptyMap(),
    val quoteStates: Map<String, QuoteState> = emptyMap(),
) {
    fun growth(id: String) = SpacedRepetition.growth(progress[id])
    val garden: Map<Growth, Int> get() = Growth.entries.associateWith { g -> words.count { growth(it.id) == g } }
    val due: Int get() = progress.values.count { SpacedRepetition.isDue(it, today()) }
}

class LearnViewModel(private val c: AppContainer) : ViewModel() {
    private val words = MutableStateFlow<List<Word>>(emptyList())
    private val quotes = MutableStateFlow<List<Quote>>(emptyList())

    val ui = combine(words, quotes, c.learn.wordProgress, c.learn.quoteStates) { w, q, p, s ->
        LearnUi(w, q, p.associateBy { it.wordId }, s.associateBy { it.quoteId })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LearnUi())

    init {
        viewModelScope.launch {
            words.value = c.learn.words()
            quotes.value = c.learn.quotes()
        }
    }

    fun toggleWord(id: String) = viewModelScope.launch { c.learn.toggleWordSaved(id) }
    fun toggleQuote(id: String) = viewModelScope.launch { c.learn.toggleQuoteSaved(id) }
}

@Composable
fun LearnScreen(onOpenWord: (String) -> Unit, onPractice: () -> Unit) {
    val vm = kairosViewModel { LearnViewModel(it) }
    val ui by vm.ui.collectAsState()
    val k = Kairos.colors
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var theme by rememberSaveable { mutableStateOf<String?>(null) }

    val filtered = remember(ui.words, query, theme) {
        ui.words.filter {
            (theme == null || it.theme == theme) &&
                (query.isBlank() || it.word.contains(query, true) || it.definition.contains(query, true))
        }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = tabContentPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Text("Learn", style = MaterialTheme.typography.displaySmall, color = k.ink, modifier = Modifier.rise(0)) }
        item { GardenCard(ui, onPractice, Modifier.rise(1)) }
        item { Segmented(listOf("Words", "Thoughts", "Saved"), tab, { tab = it }, Modifier.fillMaxWidth().rise(2)) }

        when (tab) {
            0 -> {
                item { SearchField(query, { query = it }) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { SoftChip("All", theme == null, { theme = null }) }
                        items(Themes.wordThemes) { t -> SoftChip(Themes.label(t), theme == t, { theme = if (theme == t) null else t }, selectedColor = k.washes.mint) }
                    }
                }
                if (filtered.isEmpty()) {
                    item { EmptyNote("No words match", "Try a different spelling or theme.", mood = PipMood.Curious) }
                }
                items(filtered, key = { it.id }) { w ->
                    WordRow(w, ui.growth(w.id), ui.progress[w.id]?.saved == true, { vm.toggleWord(w.id) }, { onOpenWord(w.id) }, Modifier.animateItem())
                }
            }
            1 -> item { QuoteDeck(ui, onSave = vm::toggleQuote) }
            else -> {
                val savedWords = ui.words.filter { ui.progress[it.id]?.saved == true }
                val savedQuotes = ui.quotes.filter { ui.quoteStates[it.id]?.saved == true }
                if (savedWords.isEmpty() && savedQuotes.isEmpty()) {
                    item {
                        EmptyNote(
                            "Nothing saved yet",
                            "Tap the heart on any word or thought. Saving also teaches Kairos what you love.",
                            mood = PipMood.Calm,
                        )
                    }
                }
                if (savedWords.isNotEmpty()) item { Text("Words you love", style = MaterialTheme.typography.titleLarge, color = k.ink) }
                items(savedWords, key = { "sw" + it.id }) { w ->
                    WordRow(w, ui.growth(w.id), true, { vm.toggleWord(w.id) }, { onOpenWord(w.id) }, Modifier.animateItem())
                }
                if (savedQuotes.isNotEmpty()) item { Text("Thoughts you kept", style = MaterialTheme.typography.titleLarge, color = k.ink) }
                items(savedQuotes, key = { "sq" + it.id }) { q ->
                    AcrylicCard(Modifier.fillMaxWidth().animateItem(), tint = k.washes.lilac) {
                        Row(Modifier.padding(18.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(q.text, style = MaterialTheme.typography.titleMedium, color = k.ink)
                                Spacer(Modifier.height(6.dp))
                                Text("— ${q.author}", style = MaterialTheme.typography.bodySmall, color = k.inkSoft)
                            }
                            HeartButton(true) { vm.toggleQuote(q.id) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GardenCard(ui: LearnUi, onPractice: () -> Unit, modifier: Modifier = Modifier) {
    val k = Kairos.colors
    AcrylicCard(modifier.fillMaxWidth(), tint = k.washes.mint) {
        CornerWash(k.washes.butter, Modifier.matchParentSize(), seed = 4, at = Offset(0.92f, 0.2f), scale = 0.45f)
        Column(Modifier.padding(20.dp)) {
            Text("Your word garden", style = MaterialTheme.typography.titleLarge, color = k.ink)
            Text(
                "Words grow each time you practise. Blooms are words you'll keep for weeks.",
                style = MaterialTheme.typography.bodySmall, color = k.inkSoft,
            )
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Growth.entries.forEach { g ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        GrowthGlyph(g, Modifier.size(38.dp))
                        Text("${ui.garden[g] ?: 0}", style = MaterialTheme.typography.titleLarge, color = k.ink)
                        Text(g.label, style = MaterialTheme.typography.labelSmall, color = k.inkSoft)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            InkButton(
                if (ui.due > 0) "Water ${ui.due} word${if (ui.due == 1) "" else "s"}" else "Practise a few words",
                onPractice,
                icon = Icons.Rounded.Spa,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Seed, sprout, bud, bloom — drawn, not emoji. */
@Composable
fun GrowthGlyph(g: Growth, modifier: Modifier = Modifier) {
    val k = Kairos.colors
    val stem = Color(0xFF4FAE8A)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val ground = h * 0.86f
        drawLine(k.ink.copy(alpha = 0.2f), Offset(w * 0.2f, ground), Offset(w * 0.8f, ground), 2.dp.toPx(), StrokeCap.Round)
        when (g) {
            Growth.Seed -> drawOval(Color(0xFFB08A5A), Offset(w * 0.38f, ground - h * 0.2f), Size(w * 0.24f, h * 0.18f))
            Growth.Sprout -> {
                drawLine(stem, Offset(w / 2, ground), Offset(w / 2, h * 0.45f), 3.dp.toPx(), StrokeCap.Round)
                drawOval(stem, Offset(w * 0.5f, h * 0.36f), Size(w * 0.26f, h * 0.14f))
                drawOval(stem, Offset(w * 0.24f, h * 0.44f), Size(w * 0.26f, h * 0.14f))
            }
            Growth.Bud -> {
                drawLine(stem, Offset(w / 2, ground), Offset(w / 2, h * 0.32f), 3.dp.toPx(), StrokeCap.Round)
                drawOval(stem, Offset(w * 0.5f, h * 0.52f), Size(w * 0.24f, h * 0.12f))
                drawOval(k.washes.rose, Offset(w * 0.38f, h * 0.14f), Size(w * 0.24f, h * 0.24f))
            }
            Growth.Bloom -> {
                drawLine(stem, Offset(w / 2, ground), Offset(w / 2, h * 0.36f), 3.dp.toPx(), StrokeCap.Round)
                drawOval(stem, Offset(w * 0.5f, h * 0.56f), Size(w * 0.24f, h * 0.12f))
                val c = Offset(w / 2, h * 0.28f)
                for (i in 0 until 5) {
                    val a = Math.toRadians(i * 72.0 - 90).toFloat()
                    drawCircle(k.washes.peach, w * 0.12f, Offset(c.x + kotlin.math.cos(a) * w * 0.13f, c.y + kotlin.math.sin(a) * w * 0.13f))
                }
                drawCircle(k.washes.butter, w * 0.09f, c)
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQuery: (String) -> Unit) {
    val k = Kairos.colors
    AcrylicCard(Modifier.fillMaxWidth(), shape = CircleShape, elevation = 4.dp) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Search, null, tint = k.inkSoft)
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = k.ink),
                cursorBrush = SolidColor(k.lagoon),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (query.isEmpty()) Text("Search words or meanings", style = MaterialTheme.typography.bodyLarge, color = k.inkFaint)
                    inner()
                },
            )
            if (query.isNotEmpty()) {
                Icon(Icons.Rounded.Close, "Clear", tint = k.inkSoft, modifier = Modifier.clip(CircleShape).squishClick { onQuery("") })
            }
        }
    }
}

@Composable
fun WordRow(w: Word, growth: Growth, saved: Boolean, onSave: () -> Unit, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val k = Kairos.colors
    AcrylicCard(modifier.fillMaxWidth().squishClick(pressedScale = 0.98f, onClick = onOpen), elevation = 6.dp) {
        Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            GrowthGlyph(growth, Modifier.size(34.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(w.word, style = MaterialTheme.typography.titleLarge, color = k.ink)
                    Spacer(Modifier.width(8.dp))
                    Tag(w.pos, color = k.washes.sky)
                }
                Text(w.definition, style = MaterialTheme.typography.bodyMedium, color = k.inkSoft, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            HeartButton(saved, onSave)
        }
    }
}

/** A stack of thought cards: drag right to keep, left to pass. Buttons do the same. */
@Composable
private fun QuoteDeck(ui: LearnUi, onSave: (String) -> Unit) {
    val k = Kairos.colors
    var theme by rememberSaveable { mutableStateOf<String?>(null) }
    var index by rememberSaveable { mutableIntStateOf(0) }
    val deck = remember(ui.quotes, theme) { ui.quotes.filter { theme == null || it.theme == theme }.shuffled(kotlin.random.Random(today())) }
    val scope = rememberCoroutineScope()
    val dx = remember { Animatable(0f) }

    fun advance(save: Boolean) {
        val q = deck.getOrNull(index) ?: return
        scope.launch {
            if (save && ui.quoteStates[q.id]?.saved != true) onSave(q.id)
            dx.animateTo(if (save) 1400f else -1400f, Motion.enter(320))
            index++
            dx.snapTo(0f)
        }
    }

    Column {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            item { SoftChip("All", theme == null, { theme = null; index = 0 }) }
            items(Themes.quoteThemes) { t -> SoftChip(Themes.label(t), theme == t, { theme = t; index = 0 }, selectedColor = k.washes.lilac) }
        }
        if (index >= deck.size) {
            EmptyNote("That's the whole deck", "Come back tomorrow, or pick another theme.", mood = PipMood.Proud)
            SoftButton("Shuffle again", { index = 0 }, modifier = Modifier.align(Alignment.CenterHorizontally))
            return@Column
        }
        Box(Modifier.fillMaxWidth().height(380.dp), contentAlignment = Alignment.Center) {
            for (depth in 2 downTo 0) {
                val q = deck.getOrNull(index + depth) ?: continue
                val top = depth == 0
                val washes = listOf(k.washes.lilac, k.washes.sky, k.washes.peach, k.washes.mint, k.washes.rose)
                AcrylicCard(
                    Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .graphicsLayer {
                            val lift = (dx.value / 900f).coerceIn(-1f, 1f)
                            val d = if (top) 0f else depth - kotlin.math.abs(lift)
                            translationY = d * 22f
                            scaleX = 1f - d * 0.05f
                            scaleY = 1f - d * 0.05f
                            if (top) {
                                translationX = dx.value
                                rotationZ = dx.value / 40f
                            }
                        }
                        .then(
                            if (top) Modifier.pointerInput(index) {
                                detectDragGestures(
                                    onDragEnd = {
                                        when {
                                            dx.value > 260f -> advance(true)
                                            dx.value < -260f -> advance(false)
                                            else -> scope.launch { dx.animateTo(0f, spring(0.6f, 400f)) }
                                        }
                                    },
                                ) { change, drag ->
                                    change.consume()
                                    scope.launch { dx.snapTo(dx.value + drag.x) }
                                }
                            } else Modifier,
                        ),
                    tint = washes[(q.id.hashCode() and 0x7fffffff) % washes.size],
                    elevation = if (top) 16.dp else 6.dp,
                ) {
                    CornerWash(washes[((q.id.hashCode() and 0x7fffffff) + 2) % washes.size], Modifier.matchParentSize(), seed = q.id.hashCode(), at = Offset(0.85f, 0.85f), scale = 0.5f)
                    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Tag(Themes.label(q.theme), color = k.washes.butter)
                        Text(
                            q.text,
                            style = if (q.text.length < 80) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                            color = k.ink,
                        )
                        Text("— ${q.author}", style = MaterialTheme.typography.titleSmall, color = k.inkSoft)
                    }
                    if (top) {
                        val keep = (dx.value / 260f).coerceIn(0f, 1f)
                        if (keep > 0f) {
                            Icon(
                                Icons.Rounded.Favorite, null, tint = k.danger,
                                modifier = Modifier.align(Alignment.TopEnd).padding(20.dp).size(36.dp).graphicsLayer { alpha = keep; scaleX = 0.6f + keep * 0.4f; scaleY = scaleX },
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
            SoftButton("Pass", { advance(false) })
            InkButton("Keep it", { advance(true) }, icon = Icons.Rounded.Favorite)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "${index + 1} of ${deck.size} · swipe right to keep",
            style = MaterialTheme.typography.bodySmall,
            color = k.inkFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
