package com.kairos.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Markunread
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.core.design.GlassPill
import com.kairos.app.core.design.InkButton
import com.kairos.app.core.design.Kai
import com.kairos.app.core.design.KaiMood
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.KairosType
import com.kairos.app.core.design.MarkTile
import com.kairos.app.core.design.breathe
import com.kairos.app.core.design.staggerIn
import com.kairos.app.data.catalog.QuoteCatalog
import com.kairos.app.data.catalog.VocabCatalog
import com.kairos.app.ui.common.Gap

@Composable
fun OnboardingScreen(
    state: OnboardingUi,
    onName: (String) -> Unit,
    onToggleInterest: (String) -> Unit,
    onToggleQuoteTheme: (String) -> Unit,
    onPace: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
) {
    val colors = KairosTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .width(if (index == state.step) 26.dp else 8.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index <= state.step) colors.ink else colors.hairline,
                            ),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "Kairos",
                style = KairosType.handSmall,
                color = colors.inkFaint,
            )
        }

        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                val forward = targetState > initialState
                (slideInHorizontally(tween(380)) { if (forward) it / 3 else -it / 3 } + fadeIn(tween(300)))
                    .togetherWith(
                        slideOutHorizontally(tween(260)) { if (forward) -it / 4 else it / 4 } + fadeOut(tween(200)),
                    )
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            label = "onboarding-step",
        ) { step ->
            when (step) {
                0 -> WelcomeStep()
                1 -> GrowStep(state = state, onName = onName, onToggle = onToggleInterest)
                else -> ReadStep(state = state, onToggle = onToggleQuoteTheme, onPace = onPace)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.step > 0) {
                Text(
                    text = "back",
                    style = KairosType.handSmall,
                    color = colors.inkSoft,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.glassFill)
                        .clickable(onClick = onBack)
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                )
                Spacer(Modifier.width(10.dp))
            }
            if (state.step < 2) {
                InkButton(
                    text = "Continue",
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.ArrowForward,
                )
            } else {
                InkButton(
                    text = "Step inside",
                    onClick = onFinish,
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.AutoAwesome,
                )
            }
        }
    }}

@Composable
private fun WelcomeStep() {
    val colors = KairosTheme.colors
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val minHeight = this.maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = minHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
        Spacer(Modifier.height(22.dp))
        MarkTile(size = 92.dp, animated = true, modifier = Modifier.breathe())
        Spacer(Modifier.height(22.dp))
        Text(
            text = "Kairos",
            style = MaterialTheme.typography.displayLarge,
            color = colors.ink,
            fontSize = 62.sp,
        )
        Text(
            text = "a small moment, kept",
            style = KairosType.hand,
            color = colors.inkSoft,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "One word. One thought. One letter to the person you're becoming.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.inkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 18.dp),
        )
        Spacer(Modifier.height(14.dp))
        Kai(
            modifier = Modifier
                .size(148.dp)
                .staggerIn(0),
            mood = KaiMood.Happy,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MiniNote(Icons.Rounded.MenuBook, "words that stick")
            MiniNote(Icons.Rounded.AutoAwesome, "a daily thought")
            MiniNote(Icons.Rounded.Markunread, "letters to later")
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "no account · no feed · nothing leaves this phone",
            style = KairosType.handSmall,
            color = colors.inkFaint,
        )
        Spacer(Modifier.height(22.dp))
        }
    }
}

@Composable
private fun MiniNote(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    val colors = KairosTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(96.dp)) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.glassFillStrong),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.inkSoft, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = KairosType.handSmall.copy(fontSize = 15.sp),
            color = colors.inkFaint,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp,
        )
    }
}

@Composable
private fun GrowStep(state: OnboardingUi, onName: (String) -> Unit, onToggle: (String) -> Unit) {
    val colors = KairosTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(18.dp))
        Text("FIRST, A NAME", style = KairosType.eyebrow, color = colors.inkFaint)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "What should we\ncall you?",
            style = MaterialTheme.typography.displayMedium,
            color = colors.ink,
        )
        Spacer(Modifier.height(18.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                .background(colors.glassFillStrong)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            if (state.name.isEmpty()) {
                Text(
                    text = "your name, a nickname, anything",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkFaint,
                )
            }
            BasicTextField(
                value = state.name,
                onValueChange = onName,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = com.kairos.app.core.design.KairosDisplay,
                    fontSize = 26.sp,
                    color = colors.ink,
                ),
                cursorBrush = SolidColor(colors.apricot),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(30.dp))
        Text("WHAT SHOULD WE GROW?", style = KairosType.eyebrow, color = colors.inkFaint)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "pick a few shelves — your daily word comes from here",
            style = KairosType.handSmall,
            color = colors.inkSoft,
        )
        Spacer(Modifier.height(14.dp))
        ChipGrid(
            items = VocabCatalog.themes,
            selected = state.interests,
            onClick = onToggle,
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ReadStep(state: OnboardingUi, onToggle: (String) -> Unit, onPace: (Int) -> Unit) {
    val colors = KairosTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(18.dp))
        Text("AND WHAT SHOULD WE READ?", style = KairosType.eyebrow, color = colors.inkFaint)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Which thoughts\nfind you?",
            style = MaterialTheme.typography.displayMedium,
            color = colors.ink,
        )
        Spacer(Modifier.height(16.dp))
        ChipGrid(
            items = QuoteCatalog.themes,
            selected = state.quoteThemes,
            onClick = onToggle,
        )
        Spacer(Modifier.height(26.dp))
        Text("HOW MUCH IS ENOUGH?", style = KairosType.eyebrow, color = colors.inkFaint)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "a sitting of ${state.pace} cards · about ${state.pace * 2} minutes",
            style = KairosType.handSmall,
            color = colors.inkSoft,
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(3, 5, 8, 12).forEach { pace ->
                GlassPill(selected = state.pace == pace, onClick = { onPace(pace) }) {
                    Text(
                        text = "$pace",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.pace == pace) colors.paper else colors.inkSoft,
                    )
                }
            }
        }
        Spacer(Modifier.height(26.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Kai(modifier = Modifier.size(72.dp), mood = KaiMood.Wink)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "you can change all of this later. nothing here is a commitment.",
                style = KairosType.handSmall,
                color = colors.inkFaint,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ChipGrid(
    items: List<Pair<String, String>>,
    selected: Set<String>,
    onClick: (String) -> Unit,
) {
    val colors = KairosTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (id, label) ->
                    GlassPill(
                        selected = id in selected,
                        onClick = { onClick(id) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (id in selected) colors.paper else colors.inkSoft,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
