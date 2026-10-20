package com.kairos.app.ui.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kairos.app.container
import com.kairos.app.data.content.Themes
import com.kairos.app.notify.DailyNudge
import com.kairos.app.ui.kit.AcrylicCard
import com.kairos.app.ui.kit.InkButton
import com.kairos.app.ui.kit.Pip
import com.kairos.app.ui.kit.PipMood
import com.kairos.app.ui.kit.Segmented
import com.kairos.app.ui.kit.SoftChip
import com.kairos.app.ui.kit.squishClick
import com.kairos.app.ui.kit.rise
import com.kairos.app.ui.theme.Kairos
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pager = rememberPagerState { 4 }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val k = Kairos.colors

    var name by rememberSaveable { mutableStateOf("") }
    var wordThemes by rememberSaveable { mutableStateOf(setOf("feelings", "wonder", "nature")) }
    var quoteThemes by rememberSaveable { mutableStateOf(setOf("hope", "calm", "growth")) }
    var level by rememberSaveable { mutableIntStateOf(1) }
    var reminders by rememberSaveable { mutableStateOf(false) }
    var hour by rememberSaveable { mutableIntStateOf(9) }
    var poke by remember { mutableIntStateOf(0) }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        reminders = granted
    }

    BackHandler(enabled = pager.currentPage > 0) { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }

    fun finish() {
        scope.launch {
            val c = context.container
            c.settings.completeOnboarding(name, wordThemes, quoteThemes, level + 1)
            c.settings.setReminders(reminders, hour, 0)
            if (reminders) DailyNudge.schedule(context, hour, 0)
            onDone()
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        Dots(pager, Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp))
        HorizontalPager(
            state = pager,
            modifier = Modifier.weight(1f),
            userScrollEnabled = true,
            beyondViewportPageCount = 1,
        ) { page ->
            val offset = (pager.currentPage - page) + pager.currentPageOffsetFraction
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = 1f - offset.absoluteValue.coerceIn(0f, 1f) * 0.6f
                    },
            ) {
                when (page) {
                    0 -> Welcome(offset, poke) { poke++ }
                    1 -> NamePage(offset, name, { name = it.take(24) }) { scope.launch { pager.animateScrollToPage(2) } }
                    2 -> TastePage(wordThemes, { wordThemes = it }, quoteThemes, { quoteThemes = it }, level, { level = it })
                    3 -> RemindPage(offset, reminders, hour, onToggle = { on ->
                        if (on && Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS) else reminders = on
                    }, onHour = { hour = it })
                }
            }
        }
        val last = pager.currentPage == 3
        InkButton(
            text = when (pager.currentPage) {
                0 -> "Hi, Pip!"
                1 -> if (name.isBlank()) "Skip for now" else "Nice to meet you"
                2 -> "Looks good"
                else -> "Let's make today lovely"
            },
            onClick = { if (last) finish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
            icon = Icons.AutoMirrored.Rounded.ArrowForward,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun Dots(pager: PagerState, modifier: Modifier = Modifier) {
    val k = Kairos.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(pager.pageCount) { i ->
            val w by animateDpAsState(if (i == pager.currentPage) 26.dp else 8.dp, label = "dot")
            Box(
                Modifier
                    .height(8.dp)
                    .width(w)
                    .clip(CircleShape)
                    .background(if (i == pager.currentPage) k.ink else k.ink.copy(alpha = 0.18f)),
            )
        }
    }
}

@Composable
private fun PageColumn(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { content() }
}

@Composable
private fun Welcome(offset: Float, poke: Int, onPoke: () -> Unit) {
    val k = Kairos.colors
    PageColumn {
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier
                .graphicsLayer { translationX = offset * 220f }
                .rise(0)
                .squishClick(pressedScale = 0.94f, onClick = onPoke),
        ) {
            Pip(PipMood.Glowing, size = 220.dp, poke = poke)
        }
        Spacer(Modifier.height(12.dp))
        Text("Hi, I'm Pip.", style = MaterialTheme.typography.displayMedium, color = k.ink, modifier = Modifier.rise(1))
        Spacer(Modifier.height(12.dp))
        Text(
            "Kairos is a small, hopeful corner of your phone. A word to learn, a thought to keep, letters to future you, and a few games — whenever you want a lovelier minute.",
            style = MaterialTheme.typography.bodyLarge,
            color = k.inkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.rise(2),
        )
        Spacer(Modifier.height(8.dp))
        Text("(You can poke me.)", style = MaterialTheme.typography.bodySmall, color = k.inkFaint, modifier = Modifier.rise(3))
    }
}

@Composable
private fun NamePage(offset: Float, name: String, onName: (String) -> Unit, onNext: () -> Unit) {
    val k = Kairos.colors
    val focus = LocalFocusManager.current
    PageColumn {
        Box(Modifier.graphicsLayer { translationX = offset * 180f }) {
            Pip(PipMood.Curious, size = 150.dp, lookX = if (name.isEmpty()) 0f else 0.6f)
        }
        Spacer(Modifier.height(8.dp))
        Text("What should I call you?", style = MaterialTheme.typography.headlineMedium, color = k.ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        AcrylicCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
            BasicTextField(
                value = name,
                onValueChange = onName,
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(color = k.ink, textAlign = TextAlign.Center),
                cursorBrush = SolidColor(k.lagoon),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focus.clearFocus(); onNext() }),
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.Center) {
                        if (name.isEmpty()) Text("Your name", style = MaterialTheme.typography.headlineSmall, color = k.inkFaint)
                        inner()
                    }
                },
            )
        }
        Spacer(Modifier.height(10.dp))
        Text("Only stays on this phone.", style = MaterialTheme.typography.bodySmall, color = k.inkFaint)
    }
}

@Composable
private fun TastePage(
    wordThemes: Set<String>, onWords: (Set<String>) -> Unit,
    quoteThemes: Set<String>, onQuotes: (Set<String>) -> Unit,
    level: Int, onLevel: (Int) -> Unit,
) {
    val k = Kairos.colors
    PageColumn {
        Pip(PipMood.Happy, size = 110.dp)
        Text("What sparks you?", style = MaterialTheme.typography.headlineMedium, color = k.ink)
        Text("Pick a few. Saving things later teaches me more.", style = MaterialTheme.typography.bodyMedium, color = k.inkSoft, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        Text("Words about…", style = MaterialTheme.typography.titleMedium, color = k.ink, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Themes.wordThemes.forEach { t ->
                SoftChip(Themes.label(t), t in wordThemes, onClick = { onWords(if (t in wordThemes) wordThemes - t else wordThemes + t) }, selectedColor = k.washes.mint)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Thoughts about…", style = MaterialTheme.typography.titleMedium, color = k.ink, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Themes.quoteThemes.forEach { t ->
                SoftChip(Themes.label(t), t in quoteThemes, onClick = { onQuotes(if (t in quoteThemes) quoteThemes - t else quoteThemes + t) }, selectedColor = k.washes.lilac)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Word difficulty", style = MaterialTheme.typography.titleMedium, color = k.ink, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Segmented(listOf("Gentle", "Curious", "Brainy"), level, onLevel, Modifier.fillMaxWidth())
    }
}

@Composable
private fun RemindPage(offset: Float, on: Boolean, hour: Int, onToggle: (Boolean) -> Unit, onHour: (Int) -> Unit) {
    val k = Kairos.colors
    PageColumn {
        Box(Modifier.graphicsLayer { translationX = offset * 180f }) { Pip(if (on) PipMood.Glowing else PipMood.Sleepy, size = 150.dp) }
        Text("A gentle hello?", style = MaterialTheme.typography.headlineMedium, color = k.ink)
        Text(
            "One notification a day with today's word. Plus a ping when a time capsule unseals. Never more.",
            style = MaterialTheme.typography.bodyMedium, color = k.inkSoft, textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        AcrylicCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(Icons.Rounded.NotificationsActive, null, tint = k.ink)
                    Spacer(Modifier.width(10.dp))
                    Text("Daily hello", style = MaterialTheme.typography.titleMedium, color = k.ink, modifier = Modifier.weight(1f))
                    Switch(
                        checked = on, onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(checkedTrackColor = k.lagoon, checkedThumbColor = k.onLagoon),
                    )
                }
                if (on) {
                    Spacer(Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(7, 9, 12, 18, 21).forEach { h ->
                            SoftChip(formatHour(h), h == hour, { onHour(h) }, selectedColor = k.washes.sky)
                        }
                    }
                }
            }
        }
    }
}

fun formatHour(h: Int) = when {
    h == 0 -> "12 am"
    h < 12 -> "$h am"
    h == 12 -> "12 pm"
    else -> "${h - 12} pm"
}
