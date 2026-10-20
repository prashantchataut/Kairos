package com.kairos.app.ui.screens.quotes

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.ui.theme.SerifFamily

private data class Reading(val id: Long, val text: String, val detail: String, val saved: Boolean, val toggle: () -> Unit)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusedLibraryScreen(initialTab: WisdomTab = WisdomTab.QUOTES, viewModel: QuotesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(initialTab.ordinal) }
    var query by rememberSaveable { mutableStateOf("") }
    var savedOnly by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val readings = when (WisdomTab.entries[tab]) {
        WisdomTab.QUOTES -> state.quotes.map { q -> Reading(q.id, q.content, q.author, q.isFavorite) { viewModel.toggleQuoteFavorite(q) } }
        WisdomTab.PROVERBS -> state.proverbs.map { q -> Reading(q.id, q.content, "${q.meaning}\n${q.origin}", q.isFavorite) { viewModel.toggleProverbFavorite(q) } }
        WisdomTab.IDIOMS -> state.idioms.map { q -> Reading(q.id, q.phrase, "${q.meaning}\n${q.exampleSentence}", q.isFavorite) { viewModel.toggleIdiomFavorite(q) } }
        WisdomTab.PHRASES -> state.phrases.map { q -> Reading(q.id, q.phrase, "${q.meaning}\n${q.exampleSentence}", q.isFavorite) { viewModel.togglePhraseFavorite(q) } }
    }.filter { (!savedOnly || it.saved) && (query.isBlank() || it.text.contains(query, true) || it.detail.contains(query, true)) }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 720.dp).fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item {
                Column(Modifier.statusBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("The keeping shelf", style = MaterialTheme.typography.displayMedium)
                    Text("Words from other lives. A little space to find your own meaning.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WisdomTab.entries.forEachIndexed { index, item -> FilterChip(selected = tab == index, onClick = { tab = index }, label = { Text(item.title) }, modifier = Modifier.heightIn(min = 48.dp)) }
                    }
                    OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search this shelf") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    FilterChip(selected = savedOnly, onClick = { savedOnly = !savedOnly }, label = { Text("Saved thoughts") }, modifier = Modifier.heightIn(min = 48.dp))
                }
            }
            if (state.isLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            state.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error); TextButton(onClick = viewModel::retry) { Text("Try again") } } }
            if (!state.isLoading && readings.isEmpty()) item { Text(if (savedOnly) "Keep a thought with Save and it will be waiting here." else "Nothing found. Try a different word or shelf.") }
            items(readings, key = { "$tab-${it.id}" }) { reading ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(reading.text, style = MaterialTheme.typography.headlineMedium, fontFamily = SerifFamily)
                    Text(reading.detail.trim(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = reading.toggle) { Text(if (reading.saved) "Saved" else "Save") }
                        TextButton(onClick = {
                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "${reading.text}\n${reading.detail}\n\nKept in Kairos") }, "Share a thought"))
                        }) { Text("Share") }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
