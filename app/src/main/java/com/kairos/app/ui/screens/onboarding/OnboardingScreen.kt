package com.kairos.app.ui.screens.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kairos.app.ui.brand.KairosMark
import com.kairos.app.ui.components.GlassButton
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.mascot.DropMascot
import com.kairos.app.ui.mascot.Expression
import com.kairos.app.ui.navigation.Scene
import com.kairos.app.ui.screens.settings.TopicPicker
import com.kairos.app.ui.screens.studio.CardContent
import com.kairos.app.ui.screens.studio.CardPalette
import com.kairos.app.ui.screens.studio.CardTemplate
import com.kairos.app.ui.screens.studio.CardThumbnail
import com.kairos.app.ui.screens.capsule.Envelope
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.theme.Wash
import com.kairos.app.util.appContainer
import kotlinx.coroutines.launch

private const val STEPS = 4

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val c = appContainer()
    val scope = rememberCoroutineScope()
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var interests by rememberSaveable { mutableStateOf(setOf("feelings", "wonder", "nature")) }
    var themes by rememberSaveable { mutableStateOf(setOf("hope", "joy", "calm")) }
    var level by rememberSaveable { mutableIntStateOf(2) }
    val focus = LocalFocusManager.current
    BackHandler(enabled = step > 0) { step-- }
    when (step) {
        0 -> Scene(Wash.Cerulean, Wash.Lilac, Wash.Peach, Wash.Mint, Wash.Butter)
        1 -> Scene(Wash.Butter, Wash.Peach, Wash.Rose, Wash.Butter, Wash.Mint)
        2 -> Scene(Wash.Mint, Wash.Cerulean, Wash.Butter, Wash.Mint, Wash.Lilac)
        else -> Scene(Wash.Lilac, Wash.Rose, Wash.Cerulean, Wash.Lilac, Wash.Peach)
    }

    fun next() {
        focus.clearFocus()
        if (step < STEPS - 1) step++
        else scope.launch {
            c.prefs.completeOnboarding(name, interests, themes, level)
            c.kairos.syncContent()
            onDone()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(Modifier.fillMaxWidth().padding(Space.gutter), verticalAlignment = Alignment.CenterVertically) {
            KairosMark(30.dp, ripple = false)
            Spacer(Modifier.weight(1f))
            repeat(STEPS) { i ->
                val w by animateDpAsState(if (i == step) 24.dp else 8.dp, Motion.squish(), label = "obDot")
                val col by animateColorAsState(if (i <= step) Kairos.colors.accent else Kairos.colors.inkFaint.copy(alpha = 0.3f), label = "obDotC")
                Box(Modifier.padding(horizontal = 3.dp).height(8.dp).width(w).clip(CircleShape).background(col))
            }
        }
        AnimatedContent(
            step,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally(Motion.glideOffset) { it / 3 * dir } + fadeIn()) togetherWith
                    (slideOutHorizontally(Motion.glideOffset) { -it / 3 * dir } + fadeOut())
            },
            modifier = Modifier.weight(1f),
            label = "onboarding",
        ) { s ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Space.gutter),
                horizontalAlignment = if (s == 2) Alignment.Start else Alignment.CenterHorizontally,
                verticalArrangement = if (s == 2) Arrangement.Top else Arrangement.Center,
            ) {
                when (s) {
                    0 -> Welcome()
                    1 -> NameStep(name, { name = it.take(24) }, ::next)
                    2 -> {
                        Text("What do you love?", style = Kairos.type.display, color = Kairos.colors.ink)
                        Text("Pick a few. Saving things later teaches Kairos even more.", style = Kairos.type.body, color = Kairos.colors.inkSoft)
                        Spacer(Modifier.height(Space.xl))
                        TopicPicker(interests, { interests = it }, themes, { themes = it }, level, { level = it })
                        Spacer(Modifier.height(Space.xl))
                    }
                    else -> Tour(name)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(Space.gutter), horizontalArrangement = Arrangement.spacedBy(Space.md)) {
            if (step > 0) GlassButton("Back", { step-- }, icon = KairosIcons.Back)
            PrimaryButton(
                when (step) {
                    0 -> "Say hi to Drop"
                    1 -> if (name.isBlank()) "Skip for now" else "Nice to meet you"
                    2 -> "These are my things"
                    else -> "Start my first day"
                },
                ::next,
                Modifier.weight(1f),
                enabled = step != 2 || (interests.isNotEmpty() && themes.isNotEmpty()),
            )
        }
    }
}

@Composable
private fun Welcome() {
    DropMascot(size = 170.dp, expression = Expression.Happy)
    Spacer(Modifier.height(Space.xxl))
    Text("A small good moment,\nevery day.", style = Kairos.type.display.copy(fontSize = Kairos.type.display.fontSize * 1.1f), color = Kairos.colors.ink, textAlign = TextAlign.Center)
    Spacer(Modifier.height(Space.md))
    Text(
        "One lovely word, one thought worth keeping, one tiny kind thing to do, and letters to future you.",
        style = Kairos.type.body, color = Kairos.colors.inkSoft, textAlign = TextAlign.Center,
    )
}

@Composable
private fun NameStep(name: String, onName: (String) -> Unit, onDone: () -> Unit) {
    DropMascot(size = 130.dp, wash = Wash.Butter, expression = if (name.isBlank()) Expression.Wow else Expression.Giggle)
    Spacer(Modifier.height(Space.xl))
    Text("Hi! I'm Drop.", style = Kairos.type.display, color = Kairos.colors.ink, textAlign = TextAlign.Center)
    Text("What should I call you?", style = Kairos.type.body, color = Kairos.colors.inkSoft, textAlign = TextAlign.Center)
    Spacer(Modifier.height(Space.xl))
    Box(
        Modifier
            .fillMaxWidth()
            .liquidGlass(CircleShape, blur = true, elevation = 10.dp, strong = true)
            .padding(horizontal = Space.xl, vertical = Space.lg),
        contentAlignment = Alignment.Center,
    ) {
        if (name.isEmpty()) Text("Your name", style = Kairos.type.title, color = Kairos.colors.inkFaint)
        BasicTextField(
            name, onName, singleLine = true,
            textStyle = Kairos.type.title.copy(color = Kairos.colors.ink, textAlign = TextAlign.Center),
            cursorBrush = SolidColor(Kairos.colors.accent),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Tour(name: String) {
    Text(if (name.isBlank()) "Here's what's inside" else "Here's what's inside, $name", style = Kairos.type.display, color = Kairos.colors.ink, textAlign = TextAlign.Center)
    Spacer(Modifier.height(Space.xl))
    Box(Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .graphicsLayer { rotationZ = -6f }
                .size(150.dp, 225.dp)
                .liquidGlass(com.kairos.app.ui.theme.Radii.Small, blur = false, elevation = 14.dp)
        ) { MiniCard() }
        Envelope(Wash.Rose.light, glow = 0.8f, modifier = Modifier.size(140.dp, 96.dp).align(Alignment.CenterEnd))
        DropMascot(size = 80.dp, wash = Wash.Mint, expression = Expression.Wink, modifier = Modifier.align(Alignment.BottomCenter))
    }
    Spacer(Modifier.height(Space.xl))
    TourLine("Today", "A word and a thought picked for you, and a tiny good thing to try.")
    TourLine("Card studio", "Turn any word into a painted card worth sharing.")
    TourLine("Time capsule", "Seal letters, photos and voice notes until a day you choose.")
    TourLine("Joy jar", "Each good moment is a drop of paint. No streaks to lose, ever.")
}

@Composable
private fun MiniCard() {
    Box(Modifier.fillMaxSize()) {
        CardThumbnail(
            CardContent(true, "Petrichor", "PET-ri-kor", "noun", "The earthy smell of rain on dry ground.", seed = 7),
            CardTemplate.Bloom, CardPalette.Sky, width = 150.dp,
        )
    }
}

@Composable
private fun TourLine(title: String, body: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = Space.sm), verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = 6.dp).size(10.dp).clip(CircleShape).background(Kairos.colors.accent))
        Spacer(Modifier.width(Space.md))
        Column {
            Text(title, style = Kairos.type.label, color = Kairos.colors.ink)
            Text(body, style = Kairos.type.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = Kairos.colors.inkSoft)
        }
    }
}
