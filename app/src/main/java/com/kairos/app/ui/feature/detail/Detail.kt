package com.kairos.app.ui.feature.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.repo.today
import com.kairos.app.engine.Srs
import com.kairos.app.engine.Topics
import com.kairos.app.ui.art.Bit
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.art.burst
import com.kairos.app.ui.art.drawFlower
import com.kairos.app.ui.art.drawGrain
import com.kairos.app.ui.art.flowerSpec
import com.kairos.app.ui.art.rememberBurst
import com.kairos.app.ui.components.Card
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.RoundIcon
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.feature.learn.themeTint
import com.kairos.app.ui.feature.today.WordFeedback
import com.kairos.app.ui.nav.shared
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.candy
import com.kairos.app.ui.theme.tint
import com.kairos.app.ui.theme.tintInk
import com.kairos.app.util.appContainer
import com.kairos.app.util.rememberSpeaker
import kotlinx.coroutines.launch

@Composable
fun TopBar(onBack: () -> Unit, onSky: Boolean, modifier: Modifier = Modifier, actions: @Composable () -> Unit = {}) {
    Row(
        modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = Gap.gutter, vertical = Gap.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Gap.sm),
    ) {
        RoundIcon(Icons.Back, "Back", onBack, onSky = onSky)
        Spacer(Modifier.weight(1f))
        actions()
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter)) {
        Text(title, style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = K.colors.accent)
        Spacer(Modifier.height(Gap.sm))
        content()
    }
}

/** A glass action on a picture: icon over label, a pop and an optional burst when it turns on. */
@Composable
private fun GlassAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, active: Boolean = false, celebrate: Boolean = false) {
    val calm = K.calm
    val scope = rememberCoroutineScope()
    val burst = rememberBurst()
    val pop = remember { Animatable(1f) }
    val density = LocalDensity.current
    Column(
        modifier
            .height(78.dp)
            .burst(burst)
            .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
            .pressable(label = label, scale = 0.93f) {
                if (celebrate && !active && !calm) {
                    scope.launch { pop.snapTo(0.85f); pop.animateTo(1f, Motion.bloom()) }
                    scope.launch { burst.fire(with(density) { Offset(50.dp.toPx(), 30.dp.toPx()) }, kinds = listOf(Bit.Heart, Bit.Petal, Bit.Dot), colors = listOf(Candy.Coral, Candy.Bubblegum, Candy.Sun), count = 14) }
                }
                onClick()
            }
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.22f))
            .border(1.dp, Color.White.copy(alpha = 0.5f), androidx.compose.foundation.shape.RoundedCornerShape(22.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = if (active) Color(0xFFFFD1DC) else Color.White, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(Gap.sm))
        Text(label, style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordScreen(id: String, onBack: () -> Unit, onShare: () -> Unit, onPractice: () -> Unit) {
    val c = appContainer()
    val word by c.kairos.observeWord(id).collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val speaker = rememberSpeaker()
    var told by remember { mutableStateOf<String?>(null) }
    val w = word ?: return
    val p = K.colors
    val t = com.kairos.app.ui.feature.learn.categoryTint(w.category)
    val deep = androidx.compose.ui.graphics.lerp(p.candy(t), Candy.Navy, 0.3f)
    val mid = androidx.compose.ui.graphics.lerp(p.candy(t), Candy.Navy, 0.12f)
    Column(Modifier.fillMaxSize().background(p.canvas).verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = Gap.huge)) {
        // A typographic specimen: the word large over a ghost of itself (layered type, r09).
        Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(0f to deep, 0.86f to mid, 1f to p.canvas))) {
            Canvas(Modifier.matchParentSize()) { drawGrain(0.07f) }
            Text(
                w.word.lowercase(), style = K.type.hero.copy(fontSize = 190.sp, lineHeight = 180.sp, fontStyle = FontStyle.Italic), color = Color.White.copy(alpha = 0.1f), maxLines = 1, softWrap = false,
                modifier = Modifier.align(Alignment.Center).graphicsLayer { rotationZ = -8f; translationY = -20f * density },
            )
            Column {
                TopBar(onBack, onSky = true)
                Spacer(Modifier.height(Gap.xl))
                Row(Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${Topics.wordLabel(w.category)} · ${Topics.levels[(w.level - 1).coerceIn(0, 4)]}", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White,
                        modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).padding(horizontal = 12.dp, vertical = 5.dp),
                    )
                    if (w.language.isNotBlank()) { Spacer(Modifier.width(Gap.sm)); com.kairos.app.ui.components.LanguageStamp(w.language, Color.White) }
                }
                Spacer(Modifier.height(Gap.md))
                com.kairos.app.ui.components.FitText(
                    w.word, K.type.hero.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.03).sp), Color.White,
                    Modifier.fillMaxWidth().height(110.dp).padding(horizontal = Gap.gutter).shared("word-${w.id}"), max = 88.sp, min = 36.sp, lineHeight = 1f,
                    align = TextAlign.Center, contentAlignment = Alignment.Center,
                )
                Spacer(Modifier.height(Gap.sm))
                Row(
                    Modifier.align(Alignment.CenterHorizontally).clip(CircleShape).background(Color.White).pressable(label = "Listen") { speaker.say(w.word) }.padding(start = 14.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(w.pronunciation, style = K.type.label, color = Candy.Navy)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Speaker, null, tint = deep, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(w.partOfSpeech, style = K.type.label.copy(fontFamily = com.kairos.app.ui.theme.Serif, fontStyle = FontStyle.Italic), color = Candy.Navy.copy(alpha = 0.7f))
                }
                Spacer(Modifier.height(Gap.lg))
                Text(w.definition, style = K.type.body.copy(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium), color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = Gap.xl))
                Spacer(Modifier.height(Gap.xl))
                Row(Modifier.padding(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
                    GlassAction(Icons.Speaker, "Listen", { speaker.say(w.word) }, Modifier.weight(1f))
                    GlassAction(if (w.saved) Icons.HeartOn else Icons.Heart, if (w.saved) "Saved" else "Save", { scope.launch { c.kairos.toggleWordSaved(w.id) } }, Modifier.weight(1f), active = w.saved, celebrate = true)
                    GlassAction(Icons.Share, "Card", onShare, Modifier.weight(1f))
                }
                Spacer(Modifier.height(Gap.xxl))
            }
        }
        Spacer(Modifier.height(Gap.lg))
        if (w.synonymList.isNotEmpty()) {
            InfoCard("Close to") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalArrangement = Arrangement.spacedBy(Gap.sm)) {
                    w.synonymList.forEach { Text(it, style = K.type.label, color = p.ink, modifier = Modifier.clip(CircleShape).background(p.surfaceSoft).padding(horizontal = 12.dp, vertical = 6.dp)) }
                }
            }
            Spacer(Modifier.height(Gap.md))
        }
        InfoCard("In a sentence") { Text("“${w.example}”", style = K.type.quote.copy(fontSize = 24.sp, lineHeight = 31.sp), color = p.ink) }
        if (w.origin.isNotBlank()) {
            Spacer(Modifier.height(Gap.md))
            InfoCard("Where it comes from") { Text(w.origin, style = K.type.body, color = p.ink) }
        }
        Spacer(Modifier.height(Gap.md))
        // Remembering it: a flower that grows with every successful review.
        Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Remembering it", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = p.accent)
                    Spacer(Modifier.height(Gap.sm))
                    val status = when {
                        w.mastered -> "In full bloom. Remembered for good; a light check-in comes back now and then."
                        w.inPractice -> ((w.dueDay ?: today()) - today()).toInt().let { if (it <= 0) "Ready to water now." else "Next watering in ${Srs.formatInterval(it)}." }
                        else -> "Still a seed. Save it and it starts to grow."
                    }
                    Text(status, style = K.type.body, color = p.ink)
                }
                Spacer(Modifier.width(Gap.md))
                val growth = when { w.mastered -> 1f; w.inPractice -> (0.35f + w.reps * 0.13f).coerceAtMost(0.9f); else -> 0.12f }
                Box(Modifier.size(width = 72.dp, height = 96.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp)).background(Candy.Navy)) {
                    Canvas(Modifier.matchParentSize()) {
                        drawFlower(flowerSpec(w.id.hashCode()), Offset(size.width / 2, size.height), size.height * 1.1f, grow = growth, bloom = if (growth >= 0.6f) ((growth - 0.6f) / 0.4f).coerceIn(0f, 1f) else 0f)
                    }
                }
            }
            if (w.hook.isNotBlank()) {
                Spacer(Modifier.height(Gap.md))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Kai(KaiLooks.Curator, size = 28.dp, shadow = false, alive = false, description = null)
                    Spacer(Modifier.width(Gap.sm))
                    Text(w.hook, style = K.type.label, color = p.inkSoft)
                }
            }
            Spacer(Modifier.height(Gap.lg))
            PrimaryButton("Practise now", onPractice, icon = Icons.Cards, dark = true)
        }
        Spacer(Modifier.height(Gap.md))
        Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Kai(if (told != null) KaiLooks.Delighted else KaiLooks.Curator.copy(gaze = Offset(0.5f, 0.3f)), size = 48.dp, shadow = false)
                Spacer(Modifier.width(Gap.md))
                AnimatedContent(told, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "told") { t ->
                    Text(t ?: "Tell Kai what you think", style = K.type.title, color = p.ink)
                }
            }
            Spacer(Modifier.height(Gap.md))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Gap.sm), verticalArrangement = Arrangement.spacedBy(Gap.sm)) {
                WordFeedback.entries.forEach { fb ->
                    Row(
                        Modifier.pressable(scale = 0.92f) {
                            scope.launch {
                                when (fb) {
                                    WordFeedback.TooEasy -> c.kairos.setWordDifficulty(w.id, -1)
                                    WordFeedback.TooHard -> c.kairos.setWordDifficulty(w.id, 1)
                                    WordFeedback.More -> c.kairos.setWordTaste(w.id, 1)
                                    WordFeedback.NotForMe -> c.kairos.setWordTaste(w.id, -1)
                                }
                                told = if (fb == WordFeedback.NotForMe) "Got it. Words like this won't come back." else fb.reply
                            }
                        }.clip(CircleShape).background(p.surfaceSoft).padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(26.dp).clip(CircleShape).background(p.surface), contentAlignment = Alignment.Center) { Text(fb.glyph, style = K.type.label, color = p.ink) }
                        Spacer(Modifier.width(8.dp))
                        Text(fb.label, style = K.type.label, color = p.ink)
                    }
                }
            }
        }
    }
}

/** A thought on a grainy periwinkle-to-peach glow with a smiling sun (r24). */
@Composable
fun QuoteScreen(id: String, onBack: () -> Unit, onShare: () -> Unit) {
    val c = appContainer()
    val quote by c.kairos.observeQuote(id).collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val q = quote ?: return
    val p = K.colors
    val t = themeTint(q.theme)
    Column(Modifier.fillMaxSize().background(p.canvas).verticalScroll(rememberScrollState()).navigationBarsPadding().padding(bottom = Gap.huge)) {
        Box(
            Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF7D86E8), Color(0xFFA79BEA), Color(0xFFF2A587)))),
        ) {
            Canvas(Modifier.matchParentSize()) {
                val sun = Offset(size.width * 0.78f, size.height * 0.2f)
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFE27A).copy(alpha = 0.85f), Color(0xFFFFE27A).copy(alpha = 0f)), sun, size.width * 0.3f), size.width * 0.3f, sun)
                drawCircle(Color(0xFFFFE27A), size.width * 0.08f, sun)
                // A tiny smile on the sun.
                val sw = size.width * 0.03f
                drawArc(Candy.Navy, 20f, 140f, false, Offset(sun.x - sw, sun.y - sw * 0.2f), androidx.compose.ui.geometry.Size(sw * 2, sw * 1.4f), style = androidx.compose.ui.graphics.drawscope.Stroke(size.width * 0.006f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                drawGrain(0.14f)
            }
            Column {
                TopBar(onBack, onSky = true)
                Spacer(Modifier.height(Gap.huge))
                Text(q.text, style = K.type.quote.copy(fontSize = 32.sp, lineHeight = 40.sp), color = Color.White, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth().padding(horizontal = Gap.xl))
                Spacer(Modifier.height(Gap.lg))
                Row(Modifier.padding(horizontal = Gap.xl), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(26.dp).height(2.dp).background(Color.White))
                    Spacer(Modifier.width(Gap.sm))
                    Text(q.author, style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                }
                Spacer(Modifier.height(Gap.xxl))
                Row(Modifier.padding(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
                    GlassAction(if (q.saved) Icons.HeartOn else Icons.Heart, if (q.saved) "Kept" else "Keep", { scope.launch { c.kairos.toggleQuoteSaved(q.id) } }, Modifier.weight(1f), active = q.saved, celebrate = true)
                    GlassAction(Icons.Share, "Make a card", onShare, Modifier.weight(1f))
                }
                Spacer(Modifier.height(Gap.xl))
            }
        }
        Spacer(Modifier.height(Gap.lg))
        if (q.note.isNotBlank()) { InfoCard("About it") { Text(q.note, style = K.type.body, color = p.ink) }; Spacer(Modifier.height(Gap.md)) }
        Card(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter)) {
            Text("A thought about", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = p.accent)
            Spacer(Modifier.height(Gap.sm))
            Text(Topics.themeLabel(q.theme), style = K.type.label.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), color = p.tintInk(t), modifier = Modifier.clip(CircleShape).background(p.tint(t)).padding(horizontal = 14.dp, vertical = 7.dp))
            Spacer(Modifier.height(Gap.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                QuoteChip("More like this", "♥", q.taste > 0) { scope.launch { c.kairos.setQuoteTaste(q.id, if (q.taste > 0) 0 else 1) } }
                QuoteChip("Not for me", "✕", q.taste < 0) { scope.launch { c.kairos.setQuoteTaste(q.id, if (q.taste < 0) 0 else -1) } }
            }
        }
    }
}

@Composable
private fun QuoteChip(text: String, glyph: String, on: Boolean, onClick: () -> Unit) {
    val p = K.colors
    Row(
        Modifier.pressable(scale = 0.92f, onClick = onClick).clip(CircleShape).background(if (on) p.ink else p.surfaceSoft).padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(26.dp).clip(CircleShape).background(if (on) p.canvas.copy(alpha = 0.15f) else p.surface), contentAlignment = Alignment.Center) { Text(glyph, style = K.type.label, color = if (on) p.canvas else p.ink) }
        Spacer(Modifier.width(8.dp))
        Text(text, style = K.type.label, color = if (on) p.canvas else p.ink)
    }
}
