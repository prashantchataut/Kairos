package com.kairos.app.ui.learn

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.engine.content.Topics
import com.kairos.app.engine.daily.Mood
import com.kairos.app.engine.learn.Lookup
import com.kairos.app.engine.learn.LookupResult
import com.kairos.app.ui.components.DetailTopBar
import com.kairos.app.ui.components.EmptyState
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.components.HeartButton
import com.kairos.app.ui.components.PillLabel
import com.kairos.app.ui.components.kairosViewModel
import com.kairos.app.ui.components.shareText
import com.kairos.app.ui.glass.ButtonTone
import com.kairos.app.ui.glass.GlassCard
import com.kairos.app.ui.glass.GlassIconButton
import com.kairos.app.ui.glass.KButton
import com.kairos.app.ui.glass.KChip
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.nav.Routes
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Space
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LookupViewModel(private val c: AppContainer) : ViewModel() {
    private val _result = MutableStateFlow<Lookup?>(null)
    val result = _result.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()
    private val _adopted = MutableStateFlow<Set<String>>(emptySet())
    val adopted = _adopted.asStateFlow()

    fun search(q: String) {
        if (q.isBlank()) return
        viewModelScope.launch {
            _loading.value = true
            _result.value = c.dictionary.lookup(q)
            _loading.value = false
        }
    }

    fun adopt(r: LookupResult, then: (String) -> Unit) = viewModelScope.launch {
        val id = c.learn.adopt(r)
        _adopted.value = _adopted.value + (r.word + r.definition)
        then(id)
    }
}

@Composable
fun LookupScreen() {
    val vm = kairosViewModel { LookupViewModel(it) }
    val result by vm.result.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val adopted by vm.adopted.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val focus = LocalFocusManager.current
    var query by remember { mutableStateOf("") }
    val colors = Kairos.colors

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = Space.screen, end = Space.screen, bottom = Space.huge)) {
        item { DetailTopBar(onBack = nav::back, title = "Look up a word", modifier = Modifier.padding(horizontal = 0.dp)) }
        item {
            Spacer(Modifier.height(Space.md))
            SearchField(query, { query = it }, "Any English word", onSubmit = { focus.clearFocus(); vm.search(query) })
            Spacer(Modifier.height(Space.md))
            KButton(if (loading) "Looking…" else "Look it up", { focus.clearFocus(); vm.search(query) }, enabled = query.isNotBlank() && !loading, modifier = Modifier.fillMaxWidth())
        }
        when (val r = result) {
            null -> item {
                EmptyState(null, "Curious about a word?", "Found something lovely in a book or a conversation? Look it up and plant it in your garden. Kairos will help you keep it.")
            }
            is Lookup.NotFound -> item { EmptyState(Mood.Cloudy, "Couldn't find that one", "Check the spelling, or try the simplest form of the word.") }
            is Lookup.Offline -> item { EmptyState(Mood.Cloudy, "The dictionary is out of reach", r.message) }
            is Lookup.Found -> items(r.results) { res ->
                val isAdopted = (res.word + res.definition) in adopted
                GlassCard(Modifier.fillMaxWidth().padding(top = Space.md)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(res.word, style = Kairos.type.title, color = colors.ink, modifier = Modifier.weight(1f))
                            PillLabel(res.partOfSpeech)
                        }
                        if (res.phonetic.isNotBlank()) Text(res.phonetic, style = Kairos.type.label, color = colors.inkSoft)
                        Spacer(Modifier.height(Space.sm))
                        Text(res.definition, style = Kairos.type.body, color = colors.ink)
                        if (res.example.isNotBlank()) {
                            Spacer(Modifier.height(Space.sm))
                            Text("“${res.example}”", style = Kairos.type.quote.copy(fontSize = 16.sp, fontStyle = FontStyle.Italic), color = colors.inkSoft)
                        }
                        Spacer(Modifier.height(Space.md))
                        KButton(
                            if (isAdopted) "In your garden" else "Plant this meaning",
                            { if (!isAdopted) vm.adopt(res) { id -> nav.open(Routes.word(id)) } },
                            tone = if (isAdopted) ButtonTone.Glass else ButtonTone.Pond,
                            icon = if (isAdopted) Icons.Rounded.Check else null,
                            enabled = !isAdopted,
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(Space.xl))
            Text("Definitions from the Free Dictionary API (dictionaryapi.dev). Only the word you search is sent.", style = Kairos.type.caption, color = colors.inkFaint)
        }
    }
}

class QuotesViewModel(private val c: AppContainer) : ViewModel() {
    val quotes = c.quotes.observe().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun toggle(id: String) = viewModelScope.launch { c.quotes.toggleSave(id) }
}

@Composable
fun QuotesScreen() {
    val vm = kairosViewModel { QuotesViewModel(it) }
    val quotes by vm.quotes.collectAsStateWithLifecycle()
    val nav = LocalNavigator.current
    val context = LocalContext.current
    var theme by remember { mutableStateOf<String?>(null) }
    var savedOnly by remember { mutableStateOf(false) }
    val colors = Kairos.colors
    val shown = quotes.filter { (theme == null || theme in it.quote.tags) && (!savedOnly || it.saved) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = Space.huge)) {
        item { DetailTopBar(onBack = nav::back, title = "Thought library") }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = Space.screen, vertical = Space.md), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                KChip("Kept", savedOnly, { savedOnly = !savedOnly })
                KChip("All themes", theme == null, { theme = null })
                Topics.quoteThemes.forEach { t -> KChip(t.label, theme == t.id, { theme = if (theme == t.id) null else t.id }) }
            }
        }
        if (shown.isEmpty()) item { EmptyState(null, "Nothing kept yet", "Tap the heart on a thought that lands. Kept thoughts teach Kairos what to bring you.") }
        items(shown, key = { it.quote.id }) { q ->
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = Space.screen, vertical = Space.xs).animateItem()) {
                Column {
                    Text(q.quote.text, style = Kairos.type.quote.copy(fontSize = 19.sp, lineHeight = 27.sp), color = colors.ink)
                    Spacer(Modifier.height(Space.sm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("— ${q.quote.author}", style = Kairos.type.label, color = colors.inkSoft)
                            Eyebrow(q.quote.tags.joinToString(" · ") { Topics.quoteLabel(it) }, color = colors.inkFaint)
                        }
                        GlassIconButton(Icons.Rounded.Share, "Share", { shareText(context, "“${q.quote.text}” — ${q.quote.author}\n\nvia Kairos") }, size = 42.dp, blur = false)
                        Spacer(Modifier.padding(4.dp))
                        HeartButton(q.saved, { vm.toggle(q.quote.id) }, size = 42.dp, label = "Keep thought")
                    }
                }
            }
        }
    }
}
