package com.kairos.app.ui.screens.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.ui.animation.rememberKairosReducedMotion
import com.kairos.app.ui.components.kairos.KairosAppBackground
import com.kairos.app.ui.components.kairos.KairosMark
import com.kairos.app.ui.components.painted.PocketGarden

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val state by viewModel.completionState.collectAsStateWithLifecycle()
    var page by rememberSaveable { mutableIntStateOf(0) }
    var thoughts by rememberSaveable { mutableStateOf("life,wisdom") }
    var words by rememberSaveable { mutableStateOf("mindfulness,communication") }
    var difficulty by rememberSaveable { mutableIntStateOf(3) }
    var session by rememberSaveable { mutableIntStateOf(5) }
    val saving = state is OnboardingCompletionState.Saving
    val reduced = rememberKairosReducedMotion()
    LaunchedEffect(state) { if (state is OnboardingCompletionState.Completed) onComplete() }
    BackHandler(page > 0 && !saving) { page-- }
    KairosAppBackground {
        Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 640.dp).fillMaxSize().padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    KairosMark(Modifier.size(40.dp))
                    Text("${page + 1} of 3", style = MaterialTheme.typography.labelLarge)
                }
                AnimatedContent(page, transitionSpec = { fadeIn(tween(if (reduced) 0 else 240)) togetherWith fadeOut(tween(if (reduced) 0 else 140)) }, modifier = Modifier.weight(1f), label = "welcome-page") { step ->
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        when (step) {
                            0 -> {
                                PocketGarden(Modifier.fillMaxWidth().height(240.dp))
                                Text("Your day has room for small wonders.", style = MaterialTheme.typography.displayMedium)
                                Text("Kairos is a place to learn, keep a thought, and leave something kind for your future self.", style = MaterialTheme.typography.bodyLarge)
                                Text("No overdue list. No race to catch up. A word, a memory, or a little play is a good place to start.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            1 -> {
                                Text("What draws you in?", style = MaterialTheme.typography.displayMedium)
                                Text("Pick the thoughts and words you would like to meet. You can change these in Settings.")
                                Text("Thoughts worth keeping", style = MaterialTheme.typography.titleLarge)
                                InterestChips(listOf("life", "wisdom", "creativity", "growth", "motivation", "communication"), thoughts) { thoughts = it }
                                Text("Words for your world", style = MaterialTheme.typography.titleLarge)
                                InterestChips(listOf("mindfulness", "communication", "emotion", "learning", "reflection", "business", "academic"), words) { words = it }
                            }
                            else -> {
                                PocketGarden(Modifier.fillMaxWidth().height(160.dp), blooms = 5)
                                Text("Find your own pace.", style = MaterialTheme.typography.displayMedium)
                                Text("A few minutes can be enough. These choices shape your learning, not an obligation.")
                                Text("Vocabulary", style = MaterialTheme.typography.titleLarge)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(1 to "Everyday", 3 to "Curious", 5 to "Challenging").forEach { (value, label) ->
                                        FilterChip(selected = difficulty == value, onClick = { difficulty = value }, label = { Text(label) })
                                    }
                                }
                                Text("Words in a session", style = MaterialTheme.typography.titleLarge)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(3, 5, 10).forEach { count -> FilterChip(selected = session == count, onClick = { session = count }, label = { Text("$count words") }) }
                                }
                                Text("Letters and progress stay on this device. Keep important originals and use Settings to export your data.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                (state as? OnboardingCompletionState.Error)?.let { Text(it.message, color = MaterialTheme.colorScheme.error) }
                if (saving) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (page > 0) OutlinedButton(onClick = { page-- }, enabled = !saving) { Text("Back") }
                    Button(onClick = {
                        if (page < 2) { page++; viewModel.clearError() }
                        else viewModel.completeOnboarding(difficulty, thoughts.split(',').filter(String::isNotBlank).toSet(), words.split(',').filter(String::isNotBlank).toSet(), session)
                    }, enabled = !saving, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) { Text(if (page == 2) "Open my Kairos" else "Continue") }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestChips(options: List<String>, value: String, onChange: (String) -> Unit) {
    val selected = value.split(',').filter(String::isNotBlank).toSet()
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { key ->
            FilterChip(selected = key in selected, onClick = {
                val next = if (key in selected) selected - key else selected + key
                onChange(next.joinToString(","))
            }, label = { Text(key.replaceFirstChar(Char::uppercase)) }, modifier = Modifier.heightIn(min = 48.dp))
        }
    }
}
