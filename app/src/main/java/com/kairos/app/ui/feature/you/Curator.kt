package com.kairos.app.ui.feature.you

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.ai.AiError
import com.kairos.app.data.prefs.DEFAULT_MODEL
import com.kairos.app.ui.art.Bit
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.art.burst
import com.kairos.app.ui.art.rememberBurst
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.SecondaryButton
import com.kairos.app.ui.components.SkyTail
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.feature.detail.TopBar
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radius
import com.kairos.app.util.appContainer
import kotlinx.coroutines.launch

data class ModelChoice(val id: String, val name: String, val note: String)

val Models = listOf(
    ModelChoice(DEFAULT_MODEL, "Claude Haiku 5.5", "Recommended. Lovely writing, very cheap."),
    ModelChoice("google/gemini-3.8-flash", "Gemini 3.8 Flash", "Fast and capable."),
    ModelChoice("deepseek/deepseek-v4.1-flash", "DeepSeek V4.1 Flash", "The most affordable."),
    ModelChoice("openai/gpt-5.6-luna", "GPT-5.6 Luna", "Balanced all-rounder."),
    ModelChoice("anthropic/claude-sonnet-5.5", "Claude Sonnet 5.5", "Richest writing, costs more."),
)

/** Kai's room: the key ritual, a fresh batch on demand, the model and what Kai can see. */
@Composable
fun CuratorScreen(onBack: () -> Unit) {
    val c = appContainer()
    val p = K.colors
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val hasKey by c.keys.hasKey.collectAsStateWithLifecycle()
    val awake by c.keys.awake.collectAsStateWithLifecycle()
    val settings by c.prefs.settings.collectAsStateWithLifecycle(initialValue = null)
    val pool by c.kairos.observeUnseenWords().collectAsStateWithLifecycle(initialValue = 0)
    val generating by c.kairos.generating.collectAsStateWithLifecycle()
    var message by remember { mutableStateOf<String?>(null) }
    var phase by remember { mutableStateOf(KeyPhase.Idle) }
    var custom by remember { mutableStateOf("") }
    val burst = rememberBurst()
    val s = settings ?: return
    val look = when {
        generating -> KaiLooks.Thinking
        awake && message != null -> KaiLooks.Delighted
        awake -> KaiLooks.Curator
        phase == KeyPhase.Checking -> KaiLooks.Thinking
        phase == KeyPhase.Failed -> KaiLooks.Sad
        phase == KeyPhase.Done -> KaiLooks.Delighted
        phase == KeyPhase.Typing -> KaiLooks.Wow
        else -> KaiLooks.Sleepy
    }
    Column(Modifier.fillMaxSize().background(p.canvas).verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding().padding(bottom = Gap.huge)) {
        Column(Modifier.fillMaxWidth().sky().padding(bottom = SkyTail), horizontalAlignment = Alignment.CenterHorizontally) {
            TopBar(onBack, onSky = true)
            Box(Modifier.size(150.dp).burst(burst), contentAlignment = Alignment.Center) { Kai(look, size = 150.dp) }
            Spacer(Modifier.height(Gap.md))
            AnimatedContent(awake, transitionSpec = { (fadeIn(Motion.fade(300)) + scaleIn(Motion.bloom(), initialScale = 0.9f)) togetherWith fadeOut(Motion.fade(150)) }, label = "title") { awake ->
                Column(Modifier.padding(horizontal = Gap.xl), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (awake) "Kai is awake" else "Kai is asleep", style = K.type.shout.copy(fontSize = 38.sp), color = p.onSky, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(Gap.sm))
                    Text(
                        if (awake) "Writing words, thoughts and small good things for ${s.name.ifBlank { "you" }}, learning from what you save, skip and practise."
                        else "I write everything with AI through OpenRouter. Give me a key and I'll get to work.",
                        style = K.type.body, color = p.onSkySoft, textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(Gap.xl))
            if (!awake) {
                KeyRitual({ phase = it }, { message = "Hello! Your first words are on their way." }, Modifier.padding(horizontal = Gap.gutter), onSky = true)
            } else {
                Row(Modifier.padding(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalAlignment = Alignment.CenterVertically) {
                    PrimaryButton(if (generating) "Writing…" else "Write a fresh batch", {
                        scope.launch {
                            message = runCatching { c.kairos.generate() }.fold(
                                { r ->
                                    launch { burst.fire(with(density) { Offset(75.dp.toPx(), 75.dp.toPx()) }, kinds = listOf(Bit.Petal, Bit.Spark, Bit.Squiggle), count = 24, power = 1.2f) }
                                    "Done: ${r.words.size} new words and ${r.quotes.size} thoughts."
                                },
                                { (it as? AiError)?.message ?: "Something went wrong. Try again in a moment." },
                            )
                        }
                    }, Modifier.weight(1f), icon = Icons.Sparkle, enabled = !generating, onSky = true)
                }
            }
            AnimatedContent(message, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "msg") { m ->
                if (m != null) Text(m, style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = p.onSky, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = Gap.md, start = Gap.gutter, end = Gap.gutter))
            }
            if (awake) Text("$pool unseen words waiting for you", style = K.type.caption, color = p.onSkySoft, modifier = Modifier.padding(top = Gap.sm))
        }

        Column(Modifier.padding(horizontal = Gap.gutter)) {
            if (c.keys.builtIn) {
                Card(Modifier.fillMaxWidth()) {
                    Text("Kai writes for free", style = K.type.title, color = p.ink)
                    Spacer(Modifier.height(Gap.xs))
                    Text("Kairos comes with its own AI: Mercury 3 for words, because it's very fast, and Gemini for thoughts and books, because it's careful with real quotes and real books. You don't need a key.", style = K.type.body, color = p.inkSoft)
                    if (!hasKey) {
                        Spacer(Modifier.height(Gap.lg))
                        Text("Prefer your own model? Add an OpenRouter key and Kai will use it first.", style = K.type.label, color = p.ink)
                        Spacer(Modifier.height(Gap.md))
                        KeyRitual({ phase = it }, { message = "Using your key from now on." }, showGetKey = true)
                    }
                }
                Spacer(Modifier.height(Gap.xl))
            }
            Text("Which mind should Kai write with?", style = K.type.title, color = p.ink)
            Spacer(Modifier.height(Gap.xs))
            Text(if (c.keys.builtIn) "Only used with your own OpenRouter key." else "All of them go through your OpenRouter key.", style = K.type.caption, color = p.inkSoft)
            Spacer(Modifier.height(Gap.md))
            Models.forEach { m -> ModelRow(m, s.aiModel == m.id) { scope.launch { c.prefs.setAiModel(m.id) } } }
            Spacer(Modifier.height(Gap.sm))
            val isCustom = Models.none { it.id == s.aiModel }
            Text(if (isCustom) "Using ${s.aiModel}" else "Or any OpenRouter model id", style = K.type.caption, color = p.inkFaint)
            Spacer(Modifier.height(Gap.sm))
            Row(Modifier.fillMaxWidth().height(50.dp).clip(CircleShape).background(p.surface).border(1.dp, p.line, CircleShape).padding(start = Gap.lg, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    if (custom.isEmpty()) Text("provider/model", style = K.type.label, color = p.inkFaint)
                    BasicTextField(custom, { custom = it.trim() }, singleLine = true, textStyle = K.type.label.copy(color = p.ink), cursorBrush = androidx.compose.ui.graphics.SolidColor(p.accent), modifier = Modifier.fillMaxWidth())
                }
                if (custom.contains('/')) Text("Use", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = p.accent, modifier = Modifier.pressable { scope.launch { c.prefs.setAiModel(custom); custom = "" } }.padding(Gap.md))
            }
            Spacer(Modifier.height(Gap.xl))
            Card(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Kai(KaiLooks.Curator.copy(gaze = Offset(-0.5f, -0.3f)), size = 44.dp, shadow = false)
                    Spacer(Modifier.width(Gap.md))
                    Text("What I can see", style = K.type.title, color = p.ink)
                }
                Spacer(Modifier.height(Gap.md))
                Text("Your name, interests, level, recent moods, and the words and thoughts you saved, skipped or found too easy or hard. That goes to OpenRouter and the model you picked, so I can write your next ones. Letters in your capsule never leave this phone.", style = K.type.body, color = p.inkSoft)
                if (hasKey) {
                    Spacer(Modifier.height(Gap.lg))
                    SecondaryButton("Remove my key", { c.keys.setApiKey(null); message = null }, icon = Icons.Trash)
                }
            }
        }
    }
}

@Composable
private fun ModelRow(m: ModelChoice, on: Boolean, onClick: () -> Unit) {
    val p = K.colors
    val bg by animateColorAsState(if (on) p.accentSoft else p.surface, Motion.fade(), label = "mbg")
    val rim by animateColorAsState(if (on) p.accent else p.line, Motion.fade(), label = "mrim")
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp).pressable(scale = 0.98f, onClick = onClick).clip(Radius.tile).background(bg).border(1.5.dp, rim, Radius.tile).padding(horizontal = Gap.lg, vertical = Gap.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(m.name, style = K.type.label.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), color = p.ink)
            Text(m.note, style = K.type.caption, color = p.inkSoft)
        }
        AnimatedContent(on, transitionSpec = { scaleIn(Motion.bloom(), initialScale = 0.4f) + fadeIn() togetherWith fadeOut() }, label = "mcheck") { o ->
            Box(Modifier.size(26.dp).clip(CircleShape).background(if (o) p.accent else Color.Transparent).border(1.5.dp, if (o) p.accent else p.line, CircleShape), contentAlignment = Alignment.Center) {
                if (o) Icon(Icons.Check, null, tint = p.onAccent, modifier = Modifier.size(15.dp))
            }
        }
    }
}
