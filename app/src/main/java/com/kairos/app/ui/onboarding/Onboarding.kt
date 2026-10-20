package com.kairos.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import com.kairos.app.container
import com.kairos.app.engine.Critter
import com.kairos.app.engine.content.Topics
import com.kairos.app.engine.daily.Mood
import com.kairos.app.engine.notify.Scheduler
import com.kairos.app.ui.art.Kiri
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.glass.ButtonTone
import com.kairos.app.ui.glass.GlassCard
import com.kairos.app.ui.glass.GlassIconButton
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.KButton
import com.kairos.app.ui.glass.KChip
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.you.Egg
import com.kairos.app.ui.you.HatchOverlay
import com.kairos.app.ui.you.ToggleRow
import kotlinx.coroutines.launch

private const val STEPS = 5

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen() {
    val nav = LocalNavigator.current
    val context = LocalContext.current
    val c = context.container
    val scope = rememberCoroutineScope()
    val colors = Kairos.colors

    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var words by remember { mutableStateOf(setOf("feelings", "wonder")) }
    var themes by remember { mutableStateOf(setOf("hope", "calm")) }
    var level by remember { mutableIntStateOf(2) }
    var nudge by remember { mutableStateOf(false) }
    var hatched by remember { mutableStateOf<Critter?>(null) }
    var forward by remember { mutableStateOf(true) }

    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> nudge = granted }

    fun next() { forward = true; step = (step + 1).coerceAtMost(STEPS - 1) }
    fun back() { forward = false; step = (step - 1).coerceAtLeast(0) }
    BackHandler(step > 0) { back() }

    fun finish() = scope.launch {
        c.settings.completeOnboarding(name, words, themes, level)
        c.settings.ensureSalt()
        if (nudge) {
            c.settings.setReminders(true, 9 * 60)
            Scheduler.scheduleNudge(context, 9 * 60)
        }
        hatched = c.pond.hatch("first")
        if (hatched == null) nav.finishOnboarding()
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).windowInsetsPadding(WindowInsets.ime)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = Space.lg, vertical = Space.sm), verticalAlignment = Alignment.CenterVertically) {
                if (step > 0) GlassIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", ::back) else Spacer(Modifier.size(48.dp))
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(STEPS) { i ->
                        val w by animateDpAsState(if (i == step) 22.dp else 8.dp, Motion.snappy(), label = "dot")
                        val col by animateColorAsState(if (i <= step) colors.pond else colors.ink.copy(alpha = 0.15f), label = "dotColor")
                        Box(Modifier.size(width = w, height = 8.dp).background(col, Radii.pill))
                    }
                }
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
            AnimatedContent(
                step,
                transitionSpec = {
                    val dir = if (forward) 1 else -1
                    (slideInHorizontally(tween(420, easing = Motion.Move)) { dir * it / 4 } + fadeIn(tween(300)))
                        .togetherWith(slideOutHorizontally(tween(300)) { -dir * it / 5 } + fadeOut(tween(180)))
                },
                label = "onboarding",
                modifier = Modifier.weight(1f),
            ) { s ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = Space.screen),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    when (s) {
                        0 -> {
                            Spacer(Modifier.height(Space.xxl))
                            Kiri(Mood.Glowing, size = 220.dp)
                            Spacer(Modifier.height(Space.xl))
                            Text("Hi, I'm Kiri.", style = Kairos.type.hero, color = colors.ink, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(Space.md))
                            Text(
                                "Kairos is a little pond for good days. Each day: one lovely word, one thought worth keeping, a kind thing to try — and letters you can send to future you.",
                                style = Kairos.type.bodyRegular.copy(fontSize = 18.sp, lineHeight = 27.sp), color = colors.inkSoft, textAlign = TextAlign.Center,
                            )
                        }
                        1 -> {
                            Spacer(Modifier.height(Space.xxl))
                            Kiri(Mood.Sunny, size = 150.dp)
                            Spacer(Modifier.height(Space.xl))
                            Text("What should I call you?", style = Kairos.type.display, color = colors.ink, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(Space.xl))
                            GlassCard(Modifier.fillMaxWidth(), blur = true, weight = GlassWeight.Frosted, shape = Radii.pill) {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    if (name.isEmpty()) Text("Your name", style = Kairos.type.title, color = colors.inkFaint)
                                    BasicTextField(
                                        name, { name = it.take(24) }, singleLine = true,
                                        textStyle = Kairos.type.title.copy(color = colors.ink, textAlign = TextAlign.Center),
                                        cursorBrush = SolidColor(colors.pond),
                                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Your name" },
                                    )
                                }
                            }
                            Spacer(Modifier.height(Space.md))
                            Text("Only used here, on your phone. Skip it if you like.", style = Kairos.type.bodyRegular, color = colors.inkSoft, textAlign = TextAlign.Center)
                        }
                        2 -> {
                            Spacer(Modifier.height(Space.xl))
                            Text("What makes you curious?", style = Kairos.type.display, color = colors.ink, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(Space.sm))
                            Text("I'll lean toward these. Saving things you love teaches me even more.", style = Kairos.type.bodyRegular, color = colors.inkSoft, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(Space.xl))
                            Eyebrow("Words about")
                            Spacer(Modifier.height(Space.sm))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                                Topics.wordInterests.forEach { t -> KChip(t.label, t.id in words, { words = if (t.id in words) words - t.id else words + t.id }) }
                            }
                            Spacer(Modifier.height(Space.xl))
                            Eyebrow("Thoughts on")
                            Spacer(Modifier.height(Space.sm))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                                Topics.quoteThemes.forEach { t -> KChip(t.label, t.id in themes, { themes = if (t.id in themes) themes - t.id else themes + t.id }) }
                            }
                            Spacer(Modifier.height(Space.xl))
                            Eyebrow("How deep should the words go?")
                            Spacer(Modifier.height(Space.sm))
                            Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                                listOf(1 to "Everyday", 2 to "Rich", 3 to "Rare").forEach { (l, label) -> KChip(label, level == l, { level = l }) }
                            }
                        }
                        3 -> {
                            Spacer(Modifier.height(Space.xxl))
                            Kiri(Mood.Okay, size = 150.dp)
                            Spacer(Modifier.height(Space.xl))
                            Text("A gentle rhythm", style = Kairos.type.display, color = colors.ink, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(Space.sm))
                            Text("No streaks to break, no guilt. Just one optional nudge each morning.", style = Kairos.type.bodyRegular, color = colors.inkSoft, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(Space.xl))
                            GlassCard(Modifier.fillMaxWidth(), blur = true, weight = GlassWeight.Frosted) {
                                ToggleRow("Morning nudge at 9:00", "Today's word and a thought. You can change the time later.", nudge) { on ->
                                    if (on && Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS) else nudge = on
                                }
                            }
                            Spacer(Modifier.height(Space.md))
                            Text("Letters to future you will always tell you when they arrive.", style = Kairos.type.caption, color = colors.inkSoft, textAlign = TextAlign.Center)
                        }
                        else -> {
                            Spacer(Modifier.height(Space.xxl))
                            Egg(Modifier.size(150.dp, 180.dp), wobble = true)
                            Spacer(Modifier.height(Space.xl))
                            Text("One more thing…", style = Kairos.type.display, color = colors.ink, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(Space.sm))
                            Text(
                                "This egg is yours. Inside is a Puddling — a tiny pond friend. Collect dew by learning, writing and being kind to yourself, and more eggs will come.",
                                style = Kairos.type.bodyRegular, color = colors.inkSoft, textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Spacer(Modifier.height(Space.xl))
                }
            }
            Box(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(Space.screen)) {
                when (step) {
                    STEPS - 1 -> KButton("Hatch my egg", { finish() }, modifier = Modifier.fillMaxWidth())
                    1 -> Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                        KButton("Skip", ::next, tone = ButtonTone.Glass, modifier = Modifier.width(120.dp))
                        KButton("Next", ::next, modifier = Modifier.weight(1f), enabled = true)
                    }
                    else -> KButton(if (step == 0) "Let's begin" else "Next", ::next, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        hatched?.let { HatchOverlay(it, onDone = { nav.finishOnboarding() }) }
    }
}

