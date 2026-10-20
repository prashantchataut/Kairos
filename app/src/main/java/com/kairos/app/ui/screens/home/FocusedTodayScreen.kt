package com.kairos.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.ui.components.kairos.*
import com.kairos.app.ui.components.painted.PaintedWelcome
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.theme.SerifFamily
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Home is an invitation, rather than a list of overdue obligations. */
@Composable
fun FocusedTodayScreen(
    onNavigateToCapsules: () -> Unit = {},
    onNavigateToGames: () -> Unit = {},
    onNavigateToVocabulary: () -> Unit,
    onNavigateToQuotes: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToNewEntry: (String) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isLoading, state.error, state.wordOfTheDay, state.dailyQuote) {
        if (!state.isLoading && state.error == null) viewModel.onDailyContentVisible()
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            Modifier.widthIn(max = 720.dp).fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    KairosMark(Modifier.size(42.dp))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("Kairos", style = MaterialTheme.typography.titleLarge)
                        Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    KairosIconButton(KairosIcons.Outlined.Notifications, "Notification settings", onNavigateToNotificationSettings)
                    KairosIconButton(KairosIcons.Outlined.Person, "Profile", onNavigateToProfile)
                }
            }
            item { PaintedWelcome(onCapsules = onNavigateToCapsules, onPlay = onNavigateToGames) }
            if (state.userName.isNotBlank()) {
                item { Text("Welcome back, ${state.userName}.", style = MaterialTheme.typography.titleMedium) }
            }
            when {
                state.isLoading -> item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                state.error != null -> item {
                    Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = viewModel::retry) { Text("Try again") }
                }
                else -> {
                    if (state.wordOfTheDay.isNotBlank()) item {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.extraLarge) {
                            Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("A word to carry", style = MaterialTheme.typography.labelLarge)
                                    TextButton(onClick = viewModel::toggleWordSaved) { Text(if (state.wordSaved) "Saved" else "Save word") }
                                }
                                Text(state.wordOfTheDay, style = MaterialTheme.typography.displayMedium, fontFamily = SerifFamily)
                                if (state.wordPronunciation.isNotBlank()) Text(state.wordPronunciation, style = MaterialTheme.typography.bodyMedium)
                                Text(state.wordDefinition, style = MaterialTheme.typography.bodyLarge)
                                if (state.wordExampleSentence.isNotBlank()) Text(state.wordExampleSentence, style = MaterialTheme.typography.bodyMedium)
                                Button(onClick = onNavigateToVocabulary) { Text("Spend a minute learning") }
                            }
                        }
                    }
                    if (state.dailyQuote.isNotBlank()) item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Something to sit with", style = MaterialTheme.typography.titleMedium)
                            Text("“${state.dailyQuote}”", style = MaterialTheme.typography.headlineMedium, fontFamily = SerifFamily)
                            Text(state.dailyQuoteAuthor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = viewModel::toggleQuoteSaved) { Text(if (state.quoteSaved) "Saved" else "Keep this thought") }
                                TextButton(onClick = { onNavigateToNewEntry("“${state.dailyQuote}”\n\nWhat this means to me today:\n") }) { Text("Reflect") }
                            }
                        }
                    }
                    if (state.wordOfTheDay.isBlank() && state.dailyQuote.isBlank()) item {
                        Text("Your library is ready to explore.", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = onNavigateToQuotes) { Text("Open the library") }
                    }
                }
            }
            item {
                HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Leave a little of today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                        Text("A sentence is enough.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = onNavigateToJournal) { Text("Write") }
                }
            }
        }
    }
}
