package com.kairos.app.ui.screens.games

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.components.kairos.*
import com.kairos.app.ui.animation.rememberKairosReducedMotion
import com.kairos.app.ui.theme.SerifFamily
import java.time.LocalDate

/** A small original daily word game. Progress persists locally; no network or account needed. */
@Composable
fun PocketGardenScreen(onBack: () -> Unit, cloudPost: CloudPostViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    val cloud by cloudPost.state.collectAsState()
    var sceneIndex by androidx.compose.runtime.saveable.rememberSaveable { mutableIntStateOf(-1) }
    var responseIndex by androidx.compose.runtime.saveable.rememberSaveable { mutableIntStateOf(-1) }
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("pocket_garden", Context.MODE_PRIVATE) }
    val day = LocalDate.now().toEpochDay()
    val puzzle = remember(day) { GardenPuzzles.forDay(day) }
    var selected by remember(day) { mutableStateOf<Int?>(null) }
    var completed by remember(day) { mutableStateOf(preferences.getLong("last_bloom", Long.MIN_VALUE) == day) }
    var blooms by remember { mutableIntStateOf(preferences.getInt("blooms", 0)) }
    val reducedMotion = rememberKairosReducedMotion()
    val growth by animateFloatAsState(if (completed) 1f else .15f, tween(if (reducedMotion) 0 else 400), label = "seedling-growth")
    val uriHandler = LocalUriHandler.current
    Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        TextButton(onClick = onBack) { Text("Back to today") }
        Text("The playroom", style = MaterialTheme.typography.displaySmall, fontFamily = SerifFamily)
        Text("A small adventure is time well spent.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        KairosReadingSurface(contentPadding = PaddingValues(0.dp)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Pocket Garden", style = MaterialTheme.typography.headlineMedium)
                Text("An original Kairos game • Included offline", style = MaterialTheme.typography.bodySmall)
                PaintedGarden(Modifier.fillMaxWidth().height(220.dp), growth)
                Text("$blooms blooms collected", style = MaterialTheme.typography.titleMedium)
                if (completed) {
                    Text("Your garden grew today. Come back tomorrow for another word seed.")
                    Text(puzzle.explanation)
                } else {
                    Text(puzzle.question, style = MaterialTheme.typography.titleLarge)
                    puzzle.options.forEachIndexed { index, option ->
                        OutlinedButton(onClick = {
                            selected = index
                            if (index == puzzle.answer && !completed) {
                                val nextBlooms = blooms + 1
                                if (preferences.edit().putLong("last_bloom", day).putInt("blooms", nextBlooms).commit()) {
                                    blooms = nextBlooms; completed = true
                                }
                            }
                        }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(option) }
                    }
                    if (selected != null && !completed) {
                        Text(if (selected == puzzle.answer) "Could not save your bloom. Tap the answer to try again." else "Give it another try. Think of ${puzzle.hint}.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        HorizontalDivider()
        Text("Cloud Post", style = MaterialTheme.typography.headlineSmall)
        Text("An original, gentle story about a snail, a sleepy star, and a letter. Download once, then play offline.")
        when {
            cloud.busy -> CircularProgressIndicator(Modifier.size(24.dp))
            cloud.story == null -> Button(onClick = cloudPost::download) { Text("Download Cloud Post • 2 KB") }
            else -> {
                val story = requireNotNull(cloud.story)
                AnimatedContent(targetState = sceneIndex to responseIndex, label = "story-page") { (page, answer) ->
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        when {
                            page < 0 -> {
                                Text(story.opening)
                                Button(onClick = { sceneIndex = 0 }) { Text("Begin the story") }
                            }
                            page >= story.scenes.size -> {
                                Text(story.ending)
                                TextButton(onClick = { sceneIndex = -1; responseIndex = -1 }) { Text("Read again") }
                            }
                            else -> {
                                val scene = story.scenes[page]
                                Text(scene.text, style = MaterialTheme.typography.titleMedium)
                                if (answer < 0) scene.choices.forEachIndexed { i, choice ->
                                    OutlinedButton(onClick = { responseIndex = i }, modifier = Modifier.fillMaxWidth()) { Text(choice) }
                                } else {
                                    Text(scene.responses[answer])
                                    Button(onClick = { responseIndex = -1; sceneIndex++ }) { Text("Continue") }
                                }
                            }
                        }
                    }
                }
                TextButton(onClick = { cloudPost.remove(); sceneIndex = -1; responseIndex = -1 }) { Text("Remove download") }
            }
        }
        cloud.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text("Beyond the garden", style = MaterialTheme.typography.headlineSmall)
        Text("Pokémon fan games", style = MaterialTheme.typography.titleLarge)
        Text("Visit the creators for releases, instructions and Android compatibility. Some fan games require a separate emulator. Kairos does not include game ROMs.")
        OutlinedButton(onClick = { uriHandler.openUri("https://www.pokecommunity.com/forums/fan-games.29/") }, modifier = Modifier.fillMaxWidth()) { Text("Browse creator releases") }
        Text("Installed games stay in their own Android apps. Choose downloads from the original creator and check their requirements before installing.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.navigationBarsPadding())
    }
}

data class GardenPuzzle(val question: String, val options: List<String>, val answer: Int, val hint: String, val explanation: String)

object GardenPuzzles {
    val all = listOf(
        GardenPuzzle("Which word means a fortunate discovery made by chance?", listOf("Serendipity", "Certainty", "Routine"), 0, "finding something lovely unexpectedly", "Serendipity is a happy discovery you weren't looking for."),
        GardenPuzzle("What does ‘resilient’ describe?", listOf("Never feeling tired", "Recovering after difficulty", "Always being right"), 1, "a plant growing again after rain", "Resilience is the ability to recover and adapt after difficulty."),
        GardenPuzzle("Which word means lasting only a short time?", listOf("Enduring", "Vast", "Ephemeral"), 2, "a blossom that lasts a day", "Ephemeral things are brief, like a rainbow or a passing moment."),
        GardenPuzzle("What is a ‘haven’?", listOf("A place of safety", "A long journey", "A difficult choice"), 0, "somewhere you can rest", "A haven is a place of shelter, safety, or refuge."),
        GardenPuzzle("What does ‘inquisitive’ mean?", listOf("Easily annoyed", "Eager to learn", "Very quiet"), 1, "asking questions about the world", "An inquisitive person is curious and interested in learning."),
        GardenPuzzle("Which word describes peaceful stillness?", listOf("Haste", "Commotion", "Tranquillity"), 2, "a quiet pond", "Tranquillity is a calm, peaceful state."),
        GardenPuzzle("What does ‘nurture’ mean?", listOf("Care for and encourage growth", "Finish in a hurry", "Hide from view"), 0, "tending a seedling", "To nurture is to care for something and help it develop.")
    )
    fun forDay(epochDay: Long): GardenPuzzle = all[Math.floorMod(epochDay, all.size.toLong()).toInt()]
}
