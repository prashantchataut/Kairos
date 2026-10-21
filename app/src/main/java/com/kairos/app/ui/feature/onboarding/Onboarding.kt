package com.kairos.app.ui.feature.onboarding

import com.kairos.app.ui.theme.canvas
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.KairosMark
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.SecondaryButton
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.feature.studio.CardContent
import com.kairos.app.ui.feature.studio.CardThumbnail
import com.kairos.app.ui.feature.studio.Hue
import com.kairos.app.ui.feature.studio.Template
import com.kairos.app.ui.feature.you.KeyField
import com.kairos.app.ui.feature.you.TastePicker
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.util.appContainer
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val c = appContainer()
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var interests by rememberSaveable { mutableStateOf(setOf("feelings", "wonder", "nature")) }
    var themes by rememberSaveable { mutableStateOf(setOf("hope", "calm", "joy")) }
    var level by rememberSaveable { mutableIntStateOf(3) }
    val p = K.colors
    BackHandler(step > 0) { step-- }

    fun finish() = scope.launch { c.prefs.completeOnboarding(name, interests, themes, level); onDone() }
    fun next() { focus.clearFocus(); if (step < 3) step++ else finish() }

    Column(Modifier.fillMaxSize().background(p.canvas).sky().windowInsetsPadding(WindowInsets.statusBars).navigationBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(Gap.gutter), verticalAlignment = Alignment.CenterVertically) {
            KairosMark(22.dp)
            Spacer(Modifier.weight(1f))
            repeat(4) { i ->
                val w by animateDpAsState(if (i == step) 22.dp else 6.dp, Motion.soft(), label = "d")
                val col by animateColorAsState(if (i <= step) p.onSky else p.onSky.copy(alpha = 0.35f), label = "dc")
                Box(Modifier.padding(horizontal = 3.dp).height(6.dp).width(w).clip(CircleShape).background(col))
            }
        }
        AnimatedContent(
            step, Modifier.weight(1f),
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally(Motion.offset) { it / 4 * dir } + fadeIn()) togetherWith (slideOutHorizontally(Motion.offset) { -it / 4 * dir } + fadeOut())
            },
            label = "ob",
        ) { s ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Gap.gutter), verticalArrangement = if (s == 0 || s == 1) Arrangement.Center else Arrangement.Top) {
                when (s) {
                    0 -> Welcome()
                    1 -> NameStep(name, { name = it.take(24) }, ::next)
                    2 -> {
                        Spacer(Modifier.height(Gap.lg))
                        Text("What do you love?", style = K.type.serifLarge, color = p.onSky)
                        Text("Your curator starts here, then learns from everything you save and skip.", style = K.type.body, color = p.onSkySoft)
                        Spacer(Modifier.height(Gap.xl))
                        Card(Modifier.fillMaxWidth()) { TastePicker(interests, { interests = it }, themes, { themes = it }, level, { level = it }) }
                        Spacer(Modifier.height(Gap.xl))
                    }
                    else -> {
                        Spacer(Modifier.height(Gap.lg))
                        Text("Meet your curator", style = K.type.serifLarge, color = p.onSky)
                        Text("Kairos uses AI to write every word, quote and small good thing just for you. Connect a free OpenRouter key to switch it on.", style = K.type.body, color = p.onSkySoft)
                        Spacer(Modifier.height(Gap.xl))
                        Card(Modifier.fillMaxWidth()) {
                            Text("OpenRouter key", style = K.type.title, color = p.ink)
                            Text("Encrypted on this phone. Only ever sent to OpenRouter.", style = K.type.caption, color = p.inkSoft)
                            Spacer(Modifier.height(Gap.lg))
                            KeyField(onConnected = { finish() })
                        }
                        Spacer(Modifier.height(Gap.lg))
                        Text("1. Open openrouter.ai/keys and sign in.\n2. Create a key and copy it.\n3. Paste it above. A few cents lasts months.", style = K.type.label, color = p.inkSoft)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
            if (step > 0) SecondaryButton("Back", { step-- })
            if (step < 3) PrimaryButton(
                when (step) { 0 -> "Begin"; 1 -> if (name.isBlank()) "Skip" else "Continue"; else -> "Continue" },
                ::next, Modifier.weight(1f), dark = true, enabled = step != 2 || interests.isNotEmpty(),
            ) else SecondaryButton("I'll do this later", { finish() }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Welcome() {
    val p = K.colors
    val calm = K.calm
    val bloom = remember { Animatable(if (calm) 1f else 0f) }
    LaunchedEffect(Unit) { bloom.animateTo(1f, Motion.soft()) }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        KairosMark(72.dp, modifier = Modifier.graphicsLayer { scaleX = bloom.value; scaleY = bloom.value; rotationZ = (1f - bloom.value) * -90f })
        Spacer(Modifier.height(Gap.xl))
        Text("Kairos", style = K.type.hero.copy(fontSize = 84.sp, lineHeight = 86.sp), color = p.onSky)
        Text("the right moment", style = K.type.quote.copy(fontSize = 22.sp), color = p.onSkySoft)
        Spacer(Modifier.height(Gap.xxl))
        Box(Modifier.height(210.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.graphicsLayer { rotationZ = -7f; translationX = -110f; alpha = bloom.value }.clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))) {
                CardThumbnail(CardContent(true, "Apricity", "uh-PRIS-ih-tee", "noun", "The warmth of the sun on a cold day."), Template.Note, Hue.Peach, 120.dp)
            }
            Box(Modifier.graphicsLayer { rotationZ = 6f; translationX = 110f; alpha = bloom.value }.clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))) {
                CardThumbnail(CardContent(false, "Nature does not hurry, yet everything is accomplished.", byline = "Lao Tzu"), Template.Night, Hue.Lavender, 120.dp)
            }
            Box(Modifier.graphicsLayer { translationY = (1f - bloom.value) * 60f }.clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))) {
                CardThumbnail(CardContent(true, "Sonder", "SON-der", "noun", "Realising every stranger has a life as full as yours."), Template.Paper, Hue.Sky, 136.dp)
            }
        }
        Spacer(Modifier.height(Gap.xl))
        Text("A word, a thought and a small good thing, written for you every day.", style = K.type.body.copy(fontSize = 18.sp, lineHeight = 26.sp), color = p.ink, textAlign = TextAlign.Center)
    }
}

@Composable
private fun NameStep(name: String, onName: (String) -> Unit, done: () -> Unit) {
    val p = K.colors
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("What should we call you?", style = K.type.serifLarge, color = p.onSky, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Gap.xl))
        Box(Modifier.fillMaxWidth().clip(CircleShape).background(p.surface).border(1.dp, p.line, CircleShape).padding(horizontal = Gap.xl, vertical = Gap.lg), contentAlignment = Alignment.Center) {
            if (name.isEmpty()) Text("Your name", style = K.type.title, color = p.inkFaint)
            BasicTextField(
                name, onName, singleLine = true,
                textStyle = K.type.title.copy(color = p.ink, textAlign = TextAlign.Center),
                cursorBrush = SolidColor(p.accent),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { done() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(Gap.md))
        Text("Only used to greet you.", style = K.type.caption, color = p.inkSoft)
    }
}

