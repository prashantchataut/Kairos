package com.kairos.app.ui.feature.you

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.ai.AiError
import com.kairos.app.data.prefs.DEFAULT_MODEL
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.SecondaryButton
import com.kairos.app.ui.components.Status
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.components.SkyTail
import com.kairos.app.ui.components.sky
import com.kairos.app.ui.feature.detail.TopBar
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Radius
import com.kairos.app.util.appContainer
import com.kairos.app.work.RefillWorker
import kotlinx.coroutines.launch

data class ModelChoice(val id: String, val name: String, val note: String)

val Models = listOf(
    ModelChoice(DEFAULT_MODEL, "Claude Haiku 5.5", "Recommended. Lovely writing, very cheap."),
    ModelChoice("google/gemini-3.8-flash", "Gemini 3.8 Flash", "Fast and capable."),
    ModelChoice("deepseek/deepseek-v4.1-flash", "DeepSeek V4.1 Flash", "The most affordable."),
    ModelChoice("openai/gpt-5.6-luna", "GPT-5.6 Luna", "Balanced all-rounder."),
    ModelChoice("anthropic/claude-sonnet-5.5", "Claude Sonnet 5.5", "Richest writing, costs more."),
)

/** Key entry, validation and model choice. Shared by onboarding and settings. */
@Composable
fun KeyField(onConnected: () -> Unit, modifier: Modifier = Modifier, compact: Boolean = false) {
    val c = appContainer()
    val p = K.colors
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var key by remember { mutableStateOf("") }
    var checking by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().height(54.dp).clip(Radius.tile).background(p.surface).border(1.dp, if (error != null) p.negative else p.line, Radius.tile).padding(horizontal = Gap.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Key, null, tint = p.inkSoft, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Gap.md))
            Box(Modifier.weight(1f)) {
                if (key.isEmpty()) Text("sk-or-…", style = K.type.body, color = p.inkFaint)
                BasicTextField(key, { key = it.trim(); error = null }, singleLine = true, textStyle = K.type.body.copy(color = p.ink), cursorBrush = SolidColor(p.accent), visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            }
        }
        error?.let { Spacer(Modifier.height(Gap.sm)); Text(it, style = K.type.caption, color = p.negative) }
        Spacer(Modifier.height(Gap.md))
        Row(horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalAlignment = Alignment.CenterVertically) {
            PrimaryButton(if (checking) "Checking…" else "Connect", {
                checking = true
                scope.launch {
                    val problem = c.ai.validate(key)
                    checking = false
                    if (problem == null) { c.keys.setApiKey(key); key = ""; RefillWorker.enqueue(context); onConnected() }
                    else error = problem.message
                }
            }, enabled = key.length > 10 && !checking, dark = true)
            if (!compact) SecondaryButton("Get a free key", { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://openrouter.ai/keys")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) })
        }
    }
}

@Composable
fun CuratorScreen(onBack: () -> Unit) {
    val c = appContainer()
    val p = K.colors
    val scope = rememberCoroutineScope()
    val hasKey by c.keys.hasKey.collectAsStateWithLifecycle()
    val settings by c.prefs.settings.collectAsStateWithLifecycle(initialValue = null)
    val pool by c.kairos.observeUnseenWords().collectAsStateWithLifecycle(initialValue = 0)
    val generating by c.kairos.generating.collectAsStateWithLifecycle()
    var message by remember { mutableStateOf<String?>(null) }
    var custom by remember { mutableStateOf("") }
    val s = settings ?: return
    Column(Modifier.fillMaxSize().background(p.canvas).verticalScroll(rememberScrollState()).imePadding().navigationBarsPadding().padding(bottom = Gap.huge)) {
        Column(Modifier.fillMaxWidth().sky().padding(bottom = SkyTail)) {
            TopBar(onBack, onSky = true)
            Column(Modifier.padding(horizontal = Gap.gutter)) {
                Spacer(Modifier.height(Gap.md))
                Text("Your curator", style = K.type.serifLarge, color = p.onSky)
                Spacer(Modifier.height(Gap.sm))
                Text("Kairos writes every word, quote and small good thing for you with AI, learning from what you save, skip and practise.", style = K.type.body, color = p.onSkySoft)
            }
        }
        Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("OpenRouter", style = K.type.title, color = p.ink, modifier = Modifier.weight(1f))
                Status(if (hasKey) "Connected" else "Not connected", if (hasKey) p.positive else p.warning)
            }
            Spacer(Modifier.height(Gap.sm))
            Text("Your key is encrypted on this phone and only ever sent to OpenRouter.", style = K.type.caption, color = p.inkSoft)
            Spacer(Modifier.height(Gap.lg))
            AnimatedContent(hasKey, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "key") { connected ->
                if (connected) Row(horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                    PrimaryButton(if (generating) "Writing…" else "Write a fresh batch", {
                        scope.launch {
                            message = runCatching { c.kairos.generate() }.fold({ "Added ${it.words.size} words and ${it.quotes.size} quotes." }, { (it as? AiError)?.message ?: "Something went wrong." })
                        }
                    }, icon = Icons.Sparkle, enabled = !generating, dark = true)
                    SecondaryButton("Remove key", { c.keys.setApiKey(null) })
                } else KeyField(onConnected = { message = "Connected. Your first words are on their way." })
            }
            message?.let { Spacer(Modifier.height(Gap.md)); Text(it, style = K.type.label, color = p.accent) }
            if (hasKey) { Spacer(Modifier.height(Gap.md)); Text("$pool unseen words waiting for you.", style = K.type.caption, color = p.inkFaint) }
        }
        Spacer(Modifier.height(Gap.md))
        Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter)) {
            Text("Model", style = K.type.title, color = p.ink)
            Spacer(Modifier.height(Gap.md))
            Models.forEach { m ->
                val on = s.aiModel == m.id
                Row(Modifier.fillMaxWidth().pressable(scale = 0.99f) { scope.launch { c.prefs.setAiModel(m.id) } }.padding(vertical = Gap.sm), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(20.dp).clip(CircleShape).border(if (on) 6.dp else 1.5.dp, if (on) p.accent else p.line, CircleShape))
                    Spacer(Modifier.width(Gap.md))
                    Column(Modifier.weight(1f)) {
                        Text(m.name, style = K.type.label, color = p.ink)
                        Text(m.note, style = K.type.caption, color = p.inkSoft)
                    }
                }
            }
            Spacer(Modifier.height(Gap.sm))
            val isCustom = Models.none { it.id == s.aiModel }
            Text(if (isCustom) "Using custom model ${s.aiModel}" else "Or use any OpenRouter model id", style = K.type.caption, color = p.inkFaint)
            Spacer(Modifier.height(Gap.sm))
            Row(Modifier.fillMaxWidth().height(48.dp).clip(Radius.tile).background(p.surfaceSoft).padding(start = Gap.lg, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    if (custom.isEmpty()) Text("provider/model", style = K.type.label, color = p.inkFaint)
                    BasicTextField(custom, { custom = it.trim() }, singleLine = true, textStyle = K.type.label.copy(color = p.ink), cursorBrush = SolidColor(p.accent), modifier = Modifier.fillMaxWidth())
                }
                if (custom.contains('/')) Text("Use", style = K.type.label, color = p.accent, modifier = Modifier.pressable { scope.launch { c.prefs.setAiModel(custom); custom = "" } }.padding(Gap.md))
            }
        }
        Spacer(Modifier.height(Gap.md))
        Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter)) {
            Text("What the curator sees", style = K.type.title, color = p.ink)
            Spacer(Modifier.height(Gap.sm))
            Text("Your name, interests, level, recent moods, and the words and quotes you saved, skipped or found too easy or hard. That's sent to OpenRouter and your chosen model to write your next words. Letters in your capsule never leave this phone.", style = K.type.body, color = p.inkSoft)
        }
        if (generating) Box(Modifier.fillMaxWidth().padding(Gap.lg), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(24.dp), color = p.accent, strokeWidth = 2.dp) }
    }
}
