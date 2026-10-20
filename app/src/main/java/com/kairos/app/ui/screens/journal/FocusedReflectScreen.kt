package com.kairos.app.ui.screens.journal

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
import com.kairos.app.ui.theme.SerifFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FocusedReflectScreen(onNavigateToNewEntry: () -> Unit, onNavigateToDetail: (Long) -> Unit, onNavigateToHistory: () -> Unit, viewModel: JournalViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 720.dp).fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            item {
                Column(Modifier.statusBarsPadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("How did today feel?", style = MaterialTheme.typography.displayMedium)
                    Text("You do not need to turn it into a lesson.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.secondaryContainer) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            Text("What is one small thing you want to remember?", style = MaterialTheme.typography.headlineMedium, fontFamily = SerifFamily)
                            Button(onClick = onNavigateToNewEntry) { Text("Make a little space") }
                        }
                    }
                    OutlinedTextField(value = state.query, onValueChange = viewModel::setQuery, label = { Text("Find a memory") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        FilterChip(selected = state.showBookmarkedOnly, onClick = viewModel::toggleBookmarkFilter, label = { Text("Bookmarked") }, modifier = Modifier.heightIn(min = 48.dp))
                        TextButton(onClick = onNavigateToHistory) { Text("History") }
                    }
                }
            }
            if (state.isLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            state.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error); TextButton(onClick = viewModel::retry) { Text("Try again") } } }
            if (!state.isLoading && state.entries.isEmpty()) item { Text(if (state.query.isBlank()) "Your first page can be a single sentence." else "No memories match this search.") }
            items(state.entries, key = { it.id }) { entry ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date(entry.createdAt)), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(entry.title.ifBlank { entry.content.lineSequence().firstOrNull().orEmpty().take(90) }, style = MaterialTheme.typography.headlineSmall, fontFamily = SerifFamily)
                    Text(entry.content.take(180), style = MaterialTheme.typography.bodyLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onNavigateToDetail(entry.id) }) { Text("Open memory") }
                        TextButton(onClick = { viewModel.toggleBookmark(entry.id) }) { Text(if (entry.isBookmarked) "Bookmarked" else "Bookmark") }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
