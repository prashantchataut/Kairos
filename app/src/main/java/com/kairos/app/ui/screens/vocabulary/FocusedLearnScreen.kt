package com.kairos.app.ui.screens.vocabulary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.ui.components.painted.PocketGarden
import com.kairos.app.ui.theme.SerifFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusedLearnScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSession: (SessionMode) -> Unit,
    viewModel: VocabularyListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val refreshMessage by viewModel.refreshMessage.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 720.dp).fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item {
                Column(Modifier.statusBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Words take root.", style = MaterialTheme.typography.displayMedium)
                    Text("Meet something new, then give it time to stay.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primaryContainer) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PocketGarden(Modifier.fillMaxWidth().height(120.dp), blooms = (state.learnedCount % 9).coerceAtLeast(1))
                        Text(if (state.dueReviewCount > 0) "${state.dueReviewCount} words ready to revisit" else "Your next little discovery", style = MaterialTheme.typography.headlineSmall)
                        Text("${state.learnedCount} learned • ${state.savedCount} saved", style = MaterialTheme.typography.bodyMedium)
                        Button(onClick = { onNavigateToSession(SessionMode.MIXED) }, modifier = Modifier.fillMaxWidth()) { Text("Begin a small session") }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onNavigateToSession(SessionMode.REVIEW_DUE) }) { Text("Revisit words") }
                            TextButton(onClick = { onNavigateToSession(SessionMode.NEW_WORDS) }) { Text("Meet new words") }
                        }
                    }
                }
            }
            item {
                OutlinedTextField(value = state.query, onValueChange = viewModel::setQuery, label = { Text("Find a word or meaning") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VocabularyFilter.entries.forEach { filter -> FilterChip(selected = state.currentFilter == filter.key, onClick = { viewModel.setFilter(filter.key) }, label = { Text(filter.label) }, modifier = Modifier.heightIn(min = 48.dp)) }
                }
            }
            if (state.isLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            state.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error); TextButton(onClick = viewModel::retry) { Text("Try again") } } }
            if (!state.isLoading && state.words.isEmpty()) item { Text("No words here yet. Try a different search or filter.") }
            items(state.words, key = { it.id }) { word ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { onNavigateToDetail(word.id) }, modifier = Modifier.weight(1f)) {
                            Text(word.word, style = MaterialTheme.typography.headlineSmall, fontFamily = SerifFamily, modifier = Modifier.fillMaxWidth())
                        }
                        TextButton(onClick = { viewModel.toggleFavorite(word.id) }) { Text(if (word.isFavorite) "Saved" else "Save") }
                    }
                    Text(word.definition, style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = { onNavigateToDetail(word.id) }) { Text("Explore this word") }
                    HorizontalDivider()
                }
            }
            item {
                TextButton(onClick = viewModel::refreshOnlineContent, enabled = !refreshing) { Text(if (refreshing) "Checking for new words…" else "Check for new words") }
                refreshMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}
