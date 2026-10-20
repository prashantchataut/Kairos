package com.kairos.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import com.kairos.app.data.content.Taxonomy
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.Segmented
import com.kairos.app.ui.components.SelectChip
import com.kairos.app.ui.glass.GlassPanel
import com.kairos.app.ui.navigation.Scene
import com.kairos.app.ui.screens.words.DetailTopBar
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.theme.Wash
import com.kairos.app.util.appContainer
import kotlinx.coroutines.launch

val LevelLabels = listOf("Gentle", "Curious", "Ambitious")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopicPicker(
    interests: Set<String>,
    onInterests: (Set<String>) -> Unit,
    themes: Set<String>,
    onThemes: (Set<String>) -> Unit,
    level: Int,
    onLevel: (Int) -> Unit,
) {
    Text("Words about", style = Kairos.type.title, color = Kairos.colors.ink)
    Spacer(Modifier.height(Space.md))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
        Taxonomy.wordTopics.forEach { t ->
            SelectChip(t.label, t.key in interests, { onInterests(if (t.key in interests) interests - t.key else interests + t.key) })
        }
    }
    Spacer(Modifier.height(Space.xl))
    Text("Thoughts that feel like", style = Kairos.type.title, color = Kairos.colors.ink)
    Spacer(Modifier.height(Space.md))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
        Taxonomy.quoteThemes.forEach { t ->
            SelectChip(t.label, t.key in themes, { onThemes(if (t.key in themes) themes - t.key else themes + t.key) })
        }
    }
    Spacer(Modifier.height(Space.xl))
    Text("Word level", style = Kairos.type.title, color = Kairos.colors.ink)
    Spacer(Modifier.height(Space.xs))
    Text(
        listOf("Warm, everyday words that feel good to use.", "A mix of familiar and new.", "Rare, rich words to stretch with.")[level - 1],
        style = Kairos.type.label, color = Kairos.colors.inkSoft,
    )
    Spacer(Modifier.height(Space.md))
    Segmented(LevelLabels, level - 1, { onLevel(it + 1) }, Modifier.fillMaxWidth())
}

@Composable
fun InterestsScreen(onBack: () -> Unit) {
    val c = appContainer()
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf(emptySet<String>()) }
    var themes by remember { mutableStateOf(emptySet<String>()) }
    var level by remember { mutableIntStateOf(2) }
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val s = c.prefs.current()
        name = s.name; interests = s.wordInterests; themes = s.quoteThemes; level = s.level; loaded = true
    }
    Scene(Wash.Mint, Wash.Butter, Wash.Cerulean, Wash.Lilac, Wash.Peach)
    if (!loaded) return
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        DetailTopBar(onBack)
        Column(Modifier.padding(horizontal = Space.gutter)) {
            Text("Make it yours", style = Kairos.type.display, color = Kairos.colors.ink)
            Text("Changes shape tomorrow's picks. Today's stay as they are.", style = Kairos.type.body, color = Kairos.colors.inkSoft)
            Spacer(Modifier.height(Space.xl))
            GlassPanel(Modifier.fillMaxWidth(), blur = false) {
                Column(Modifier.padding(Space.xl)) {
                    Text("What should Drop call you?", style = Kairos.type.label, color = Kairos.colors.inkSoft)
                    Spacer(Modifier.height(Space.sm))
                    BasicTextField(
                        name, { name = it.take(24) }, singleLine = true,
                        textStyle = Kairos.type.title.copy(color = Kairos.colors.ink),
                        cursorBrush = SolidColor(Kairos.colors.accent),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.height(Space.xl))
            TopicPicker(interests, { interests = it }, themes, { themes = it }, level, { level = it })
            Spacer(Modifier.height(Space.xxl))
            PrimaryButton("Save", {
                scope.launch {
                    c.prefs.setName(name)
                    c.prefs.setInterests(interests, themes, level)
                    onBack()
                }
            }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(Space.huge))
        }
    }
}

