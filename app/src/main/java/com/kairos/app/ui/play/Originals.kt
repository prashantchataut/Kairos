package com.kairos.app.ui.play

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.container
import com.kairos.app.data.db.GameScoreEntity
import com.kairos.app.engine.content.Word
import com.kairos.app.engine.critters.DewReason
import com.kairos.app.engine.daily.Mood
import com.kairos.app.ui.art.Kiri
import com.kairos.app.ui.art.drawSparkle
import com.kairos.app.ui.components.DetailTopBar
import com.kairos.app.ui.components.EmptyState
import com.kairos.app.ui.components.Eyebrow
import com.kairos.app.ui.components.clickableNoRipple
import com.kairos.app.ui.glass.ButtonTone
import com.kairos.app.ui.glass.GlassCard
import com.kairos.app.ui.glass.GlassWeight
import com.kairos.app.ui.glass.KButton
import com.kairos.app.ui.glass.liquidGlass
import com.kairos.app.ui.nav.LocalNavigator
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion
import com.kairos.app.ui.theme.Radii
import com.kairos.app.ui.theme.Space
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Shared shell: plays a game, then records the score and pays a little dew. */
@Composable
fun OriginalGameScreen(id: String) {
    val nav = LocalNavigator.current
    val c = LocalContext.current.container
    val scope = rememberCoroutineScope()
    val game = Original.of(id)
    var result by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var round by remember { mutableIntStateOf(0) }
    var words by remember { mutableStateOf<List<Word>?>(null) }
    LaunchedEffect(round) {
        val all = c.learn.allWords()
        val deckIds = c.db.words().all().filter { it.inDeck }.map { it.wordId }.toSet()
        val playable = all.filter { it.word.length in 3..9 && it.word.all { ch -> ch.isLetter() } }
        val fromDeck = playable.filter { it.id in deckIds }.shuffled()
        words = (fromDeck + playable.shuffled()).distinctBy { it.id }
    }

    fun finish(score: Int) {
        scope.launch {
            val prev = c.db.games().score(id) ?: GameScoreEntity(id)
            val best = maxOf(prev.best, score)
            c.db.games().upsertScore(prev.copy(best = best, plays = prev.plays + 1, lastPlayedAt = System.currentTimeMillis()))
            c.pond.award(DewReason.Game, (score / 10).coerceIn(1, 10))
            result = score to best
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            DetailTopBar(onBack = nav::back, title = game?.title ?: "Arcade")
            val w = words
            when {
                game == null -> EmptyState(null, "Game not found", "This game has wandered off.")
                w == null -> Unit
                else -> key(round) {
                    when (game) {
                        Original.DropletSpell -> DropletSpell(w, ::finish)
                        Original.LanternDrift -> LanternDrift(::finish)
                        Original.BloomMatch -> BloomMatch(w, ::finish)
                    }
                }
            }
        }
        val r = result
        AnimatedVisibility(r != null, enter = fadeIn(tween(300)) + scaleIn(tween(400, easing = Motion.Enter), initialScale = 0.92f), exit = fadeOut()) {
            if (r != null) ResultCard(r.first, r.second, onAgain = { result = null; round++ }, onDone = nav::back)
        }
    }
}

@Composable
private fun key(k: Any, content: @Composable () -> Unit) = androidx.compose.runtime.key(k) { content() }

@Composable
private fun ResultCard(score: Int, best: Int, onAgain: () -> Unit, onDone: () -> Unit) {
    val colors = Kairos.colors
    Box(Modifier.fillMaxSize().background(colors.paper.copy(alpha = 0.7f)).clickableNoRipple { }, contentAlignment = Alignment.Center) {
        GlassCard(Modifier.padding(Space.xl).fillMaxWidth(), blur = true, weight = GlassWeight.Frosted, shape = Radii.sheet) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Kiri(if (score >= best && score > 0) Mood.Glowing else Mood.Sunny, size = 120.dp)
                Spacer(Modifier.height(Space.md))
                Eyebrow(if (score >= best && score > 0) "New best!" else "Nice run")
                Text("$score", style = Kairos.type.hero, color = colors.ink)
                Text("Best $best · a little dew for the pond", style = Kairos.type.bodyRegular, color = colors.inkSoft)
                Spacer(Modifier.height(Space.xl))
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    KButton("Done", onDone, tone = ButtonTone.Glass, modifier = Modifier.weight(1f))
                    KButton("Play again", onAgain, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Droplet Spell                                                        */
/* ------------------------------------------------------------------ */

private data class Drop(val id: Int, val ch: Char, val x: Float, val y: Float, val phase: Float)

@Composable
private fun DropletSpell(words: List<Word>, onEnd: (Int) -> Unit) {
    val colors = Kairos.colors
    val haptics = LocalHapticFeedback.current
    var index by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var score by remember { mutableIntStateOf(0) }
    var over by remember { mutableStateOf(false) }
    val word = words.getOrNull(index % words.size.coerceAtLeast(1))
    val target = word?.word?.lowercase().orEmpty()
    val picked = remember(index) { mutableStateListOf<Int>() }
    val drops = remember(index) {
        val r = Random(index * 977L + target.hashCode())
        val extra = "aeioulnrst".filter { it !in target }.toList().shuffled(r).take(3)
        val chars = (target.toList() + extra).shuffled(r)
        // scatter on a jittered grid so droplets never overlap
        val cols = 4
        chars.mapIndexed { i, ch ->
            val col = i % cols
            val row = i / cols
            Drop(i, ch, (col + 0.5f + (r.nextFloat() - 0.5f) * 0.4f) / cols, (row + 0.5f + (r.nextFloat() - 0.5f) * 0.3f) / 4f, r.nextFloat() * 6.28f)
        }
    }
    var timeLeft by remember(index) { mutableFloatStateOf(1f) }
    val shake = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(index, over) {
        if (over || target.isEmpty()) return@LaunchedEffect
        val total = 25_000L
        val start = System.currentTimeMillis()
        while (isActive && timeLeft > 0f) {
            delay(100)
            timeLeft = 1f - (System.currentTimeMillis() - start) / total.toFloat()
        }
        if (timeLeft <= 0f) {
            lives--
            if (lives <= 0) { over = true; onEnd(score) } else index++
        }
    }

    if (word == null) {
        EmptyState(null, "No words yet", "Plant a few words in your garden first.")
        return
    }
    val t = rememberInfiniteTransition(label = "drops")
    val bob by t.animateFloat(0f, 6.283f, infiniteRepeatable(tween(3200, easing = LinearEasing)), label = "bob")

    Column(Modifier.fillMaxSize().padding(horizontal = Space.screen)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(3) { i -> Icon(if (i < lives) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null, tint = colors.danger, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.weight(1f))
            Text("Score $score", style = Kairos.type.heading, color = colors.ink)
        }
        Spacer(Modifier.height(Space.md))
        GlassCard(Modifier.fillMaxWidth().graphicsLayer { translationX = shake.value }, blur = true, weight = GlassWeight.Frosted) {
            Column {
                Eyebrow("Clue · ${word.pos}")
                Spacer(Modifier.height(Space.sm))
                Text(word.definition, style = Kairos.type.body.copy(fontSize = 18.sp, lineHeight = 25.sp), color = colors.ink, maxLines = 4, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(Space.md))
                Canvas(Modifier.fillMaxWidth().height(6.dp)) {
                    drawRoundRect(colors.ink.copy(alpha = 0.08f), cornerRadius = CornerRadius(size.height))
                    drawRoundRect(if (timeLeft < 0.25f) colors.danger else colors.pond, size = Size(size.width * timeLeft.coerceIn(0f, 1f), size.height), cornerRadius = CornerRadius(size.height))
                }
            }
        }
        Spacer(Modifier.height(Space.lg))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
            target.forEachIndexed { i, ch ->
                val filled = i < picked.size
                Box(
                    Modifier.size(width = 34.dp, height = 44.dp).liquidGlass(Radii.small, GlassWeight.Frosted)
                        .background(if (filled) colors.sky.copy(alpha = 0.6f) else Color.Transparent, Radii.small),
                    contentAlignment = Alignment.Center,
                ) {
                    if (filled) Text(ch.uppercase(), style = Kairos.type.heading, color = colors.ink)
                }
            }
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(vertical = Space.lg)) {
            val wPx = constraints.maxWidth.toFloat()
            val hPx = constraints.maxHeight.toFloat()
            val density = LocalDensity.current
            val dropSize = 64.dp
            val dropPx = with(density) { dropSize.toPx() }
            drops.forEach { d ->
                val used = d.id in picked
                val alpha by animateFloatAsState(if (used) 0f else 1f, tween(220), label = "drop")
                val scale by animateFloatAsState(if (used) 1.6f else 1f, tween(260), label = "dropScale")
                if (alpha > 0.01f) {
                    val x = d.x * wPx - dropPx / 2
                    val y = d.y * hPx - dropPx / 2 + sin(bob + d.phase) * dropPx * 0.12f
                    Box(
                        Modifier
                            .offset { IntOffset(x.toInt(), y.toInt()) }
                            .size(dropSize)
                            .graphicsLayer { this.alpha = alpha; scaleX = scale; scaleY = scale }
                            .clickableNoRipple {
                                if (used || over) return@clickableNoRipple
                                val need = target.getOrNull(picked.size)
                                if (d.ch == need) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    picked.add(d.id)
                                    if (picked.size == target.length) {
                                        score += 10 + (timeLeft * 10).toInt()
                                        scope.launch { delay(380); index++ }
                                    }
                                } else {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    lives--
                                    scope.launch { shake.animateTo(0f, keyframes { durationMillis = 300; -18f at 50; 14f at 120; -8f at 190; 0f at 300 }) }
                                    if (lives <= 0) { over = true; onEnd(score) }
                                }
                            }
                            .semantics { contentDescription = "Letter ${d.ch}" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            val r = size.minDimension / 2
                            drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFFBCD6FF), Color(0xFF8FB4F5)), center = Offset(r * 0.7f, r * 0.6f), radius = r * 1.5f), r * 0.92f)
                            drawCircle(Color.White.copy(alpha = 0.85f), r * 0.14f, Offset(r * 0.62f, r * 0.55f))
                        }
                        Text(d.ch.uppercase(), style = Kairos.type.heading.copy(fontSize = 24.sp), color = Color(0xFF1B2340))
                    }
                }
            }
        }
        Text(
            "Tap the droplets in order to spell the word. Wrong droplets cost a heart.",
            style = Kairos.type.caption.copy(letterSpacing = 0.sp), color = colors.inkSoft, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = Space.md),
        )
    }
}

/* ------------------------------------------------------------------ */
/* Lantern Drift                                                        */
/* ------------------------------------------------------------------ */

private class Reed(var x: Float, val gapY: Float, val gap: Float, var passed: Boolean = false)
private class Fly(var x: Float, val y: Float, var taken: Boolean = false, val phase: Float)

private class DriftWorld {
    var w = 1f
    var h = 1f
    var y = 0.4f
    var vy = 0f
    var holding = false
    var speed = 0.32f
    var spawn = 0f
    var elapsed = 0f
    var score = 0
    var alive = true
    val reeds = mutableListOf<Reed>()
    val flies = mutableListOf<Fly>()
    val random = Random(System.nanoTime())

    fun step(dt: Float) {
        if (!alive) return
        elapsed += dt
        vy += (if (holding) -1.9f else 1.25f) * dt
        vy = vy.coerceIn(-0.9f, 0.9f)
        y += vy * dt
        speed = 0.32f + elapsed * 0.004f
        spawn -= dt
        if (spawn <= 0f) {
            val gap = (0.36f - elapsed * 0.0015f).coerceAtLeast(0.26f)
            val gy = 0.22f + random.nextFloat() * (0.6f - gap / 2)
            reeds += Reed(1.15f, gy, gap)
            if (random.nextFloat() < 0.75f) flies += Fly(1.15f + 0.22f, gy + (random.nextFloat() - 0.5f) * gap * 0.5f, phase = random.nextFloat() * 6f)
            spawn = (1.55f - elapsed * 0.004f).coerceAtLeast(1.05f)
        }
        val aspect = w / h
        reeds.forEach { it.x -= speed * dt / aspect.coerceAtLeast(0.3f) }
        flies.forEach { it.x -= speed * dt / aspect.coerceAtLeast(0.3f) }
        reeds.removeAll { it.x < -0.2f }
        flies.removeAll { it.x < -0.2f || it.taken }

        val lx = 0.28f
        val r = 0.035f
        if (y < 0.02f || y > 0.95f) alive = false
        reeds.forEach { reed ->
            val half = 0.045f
            if (lx + r * 0.6f > reed.x - half && lx - r * 0.6f < reed.x + half) {
                if (y - r < reed.gapY - reed.gap / 2 || y + r > reed.gapY + reed.gap / 2) alive = false
            }
            if (!reed.passed && reed.x < lx) { reed.passed = true; score += 1 }
        }
        flies.forEach { f ->
            val dx = (f.x - lx) * aspect
            val dy = f.y - y
            if (dx * dx + dy * dy < 0.004f) { f.taken = true; score += 3 }
        }
    }
}

@Composable
private fun LanternDrift(onEnd: (Int) -> Unit) {
    val colors = Kairos.colors
    val haptics = LocalHapticFeedback.current
    val world = remember { DriftWorld() }
    var frame by remember { mutableLongStateOf(0L) }
    var started by remember { mutableStateOf(false) }
    var ended by remember { mutableStateOf(false) }
    LaunchedEffect(started) {
        if (!started) return@LaunchedEffect
        var last = 0L
        while (isActive && world.alive) {
            withFrameNanos { now ->
                if (last != 0L) world.step(((now - last) / 1e9f).coerceAtMost(0.05f))
                last = now
                frame = now
            }
        }
        if (!ended) {
            ended = true
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(500)
            onEnd(world.score)
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = Space.lg)
            .padding(bottom = Space.lg)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .liquidGlass(Radii.sheet, GlassWeight.Clear)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    if (!started) started = true
                    world.holding = true
                    waitForUpOrCancellation()
                    world.holding = false
                }
            }
            .semantics { contentDescription = "Lantern Drift. Press and hold to rise, release to drift down." },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (frame < 0) return@Canvas // reading the frame clock invalidates this draw every tick
            world.w = size.width
            world.h = size.height
            val w = size.width
            val h = size.height
            drawRect(Brush.verticalGradient(listOf(Color(0xFF1E2F5E), Color(0xFF4B4486), Color(0xFFD99A8A))))
            val t = world.elapsed
            repeat(24) { i ->
                val sx = ((i * 0.137f + 0.05f) - t * 0.01f * (1 + i % 3)).mod(1f) * w
                val sy = (i * 0.071f % 0.55f) * h
                drawCircle(Color(0xFFFFF3C4).copy(alpha = 0.35f + 0.3f * sin(t * 2 + i)), 1.6f + (i % 3), Offset(sx, sy))
            }
            fun hills(color: Color, top: Float, speed: Float, freq: Float) {
                val p = Path().apply {
                    moveTo(0f, h)
                    var x = 0f
                    while (x <= w + 8) {
                        lineTo(x, h * top + sin((x / w) * freq + t * speed) * h * 0.035f)
                        x += 8f
                    }
                    lineTo(w, h); close()
                }
                drawPath(p, color)
            }
            hills(Color(0xFF34407A), 0.8f, 0.4f, 7f)
            world.reeds.forEach { reed ->
                val x = reed.x * w
                val half = 0.045f * w.coerceAtMost(h)
                val top = (reed.gapY - reed.gap / 2) * h
                val bottom = (reed.gapY + reed.gap / 2) * h
                val reedBrush = Brush.horizontalGradient(listOf(Color(0xFF3E7C66), Color(0xFF64B08B), Color(0xFF3E7C66)), startX = x - half, endX = x + half)
                drawRoundRect(reedBrush, topLeft = Offset(x - half, -20f), size = Size(half * 2, top + 20f), cornerRadius = CornerRadius(half))
                drawRoundRect(reedBrush, topLeft = Offset(x - half, bottom), size = Size(half * 2, h - bottom + 20f), cornerRadius = CornerRadius(half))
                drawOval(Color(0xFF8B5E3C), topLeft = Offset(x - half * 0.7f, top - half * 1.6f), size = Size(half * 1.4f, half * 2.6f))
                drawOval(Color(0xFF8B5E3C), topLeft = Offset(x - half * 0.7f, bottom - half * 1f), size = Size(half * 1.4f, half * 2.6f))
            }
            world.flies.forEach { f ->
                val c = Offset(f.x * w, f.y * h + sin(t * 3 + f.phase) * 6f)
                drawCircle(Brush.radialGradient(listOf(Color(0xCCFFF2A8), Color.Transparent), center = c, radius = 26f), 26f, c)
                drawCircle(Color(0xFFFFF6C8), 5f, c)
            }
            hills(Color(0xFF1C2650), 0.9f, 0.8f, 11f)
            // the lantern
            val lc = Offset(0.28f * w, world.y * h)
            val lr = 0.035f * h
            drawCircle(Brush.radialGradient(listOf(Color(0x88FFD27A), Color.Transparent), center = lc, radius = lr * 3.2f), lr * 3.2f, lc)
            val tilt = (world.vy * 25f).coerceIn(-15f, 15f)
            rotate(tilt, lc) {
                drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFFFE9A6), Color(0xFFF2994A)), startY = lc.y - lr, endY = lc.y + lr), topLeft = Offset(lc.x - lr * 0.75f, lc.y - lr), size = Size(lr * 1.5f, lr * 2f), cornerRadius = CornerRadius(lr * 0.5f))
                drawRoundRect(Color(0xFF7A4B2A), topLeft = Offset(lc.x - lr * 0.5f, lc.y - lr * 1.15f), size = Size(lr, lr * 0.2f), cornerRadius = CornerRadius(4f))
                drawCircle(Color.White.copy(alpha = 0.7f), lr * 0.18f, Offset(lc.x - lr * 0.3f, lc.y - lr * 0.4f))
            }
        }
        val shownScore = if (frame >= 0) world.score else 0
        Text("$shownScore", style = Kairos.type.display, color = Color.White, modifier = Modifier.align(Alignment.TopCenter).padding(top = Space.xl))
        if (!started) {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Hold to rise", style = Kairos.type.title, color = Color.White)
                Text("Let go to drift down. Gather fireflies.", style = Kairos.type.bodyRegular, color = Color.White.copy(alpha = 0.85f))
            }
        }
    }
}

/* ------------------------------------------------------------------ */
/* Bloom Match                                                          */
/* ------------------------------------------------------------------ */

private data class MatchCard(val id: Int, val pair: Int, val text: String, val isWord: Boolean)

@Composable
private fun BloomMatch(words: List<Word>, onEnd: (Int) -> Unit) {
    val colors = Kairos.colors
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val cards = remember {
        val pick = words.shuffled().take(6)
        pick.flatMapIndexed { i, w ->
            listOf(
                MatchCard(i * 2, i, w.word, true),
                MatchCard(i * 2 + 1, i, w.definition.split(';', ',', '.').first().take(48), false),
            )
        }.shuffled()
    }
    val faceUp = remember { mutableStateListOf<Int>() }
    val matched = remember { mutableStateListOf<Int>() }
    var moves by remember { mutableIntStateOf(0) }
    val start = remember { System.currentTimeMillis() }
    var busy by remember { mutableStateOf(false) }

    if (cards.size < 4) {
        EmptyState(null, "Not enough words", "Plant a few more words to play.")
        return
    }

    fun tap(card: MatchCard) {
        if (busy || card.id in faceUp || card.pair in matched) return
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        faceUp.add(card.id)
        if (faceUp.size == 2) {
            moves++
            val (a, b) = faceUp.map { id -> cards.first { it.id == id } }
            if (a.pair == b.pair) {
                matched.add(a.pair)
                faceUp.clear()
                if (matched.size == cards.size / 2) {
                    val secs = ((System.currentTimeMillis() - start) / 1000).toInt()
                    scope.launch { delay(700); onEnd((130 - moves * 4 - secs / 2).coerceAtLeast(10)) }
                }
            } else {
                busy = true
                scope.launch { delay(850); faceUp.clear(); busy = false }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = Space.screen)) {
        Row {
            Text("Flips $moves", style = Kairos.type.heading, color = colors.ink, modifier = Modifier.weight(1f))
            Text("${matched.size} / ${cards.size / 2} blooms", style = Kairos.type.heading, color = colors.pond)
        }
        Spacer(Modifier.height(Space.md))
        cards.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = Space.sm), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                row.forEach { card ->
                    val shown = card.id in faceUp || card.pair in matched
                    val rot by animateFloatAsState(if (shown) 180f else 0f, tween(420, easing = FastOutSlowInEasing), label = "flip")
                    val isMatched = card.pair in matched
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(0.78f)
                            .graphicsLayer { rotationY = rot; cameraDistance = 12 * density }
                            .liquidGlass(Radii.card, GlassWeight.Frosted)
                            .border(2.dp, if (isMatched) colors.success.copy(alpha = 0.7f) else Color.Transparent, Radii.card)
                            .clickableNoRipple { tap(card) }
                            .semantics { contentDescription = if (shown) card.text else "Face-down card" },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (rot <= 90f) {
                            Canvas(Modifier.fillMaxSize().padding(14.dp)) {
                                val c = center
                                repeat(5) { k ->
                                    val a = k / 5f * 2f * PI.toFloat()
                                    drawCircle(colors.lilac.copy(alpha = 0.8f), size.minDimension * 0.16f, Offset(c.x + cos(a) * size.minDimension * 0.18f, c.y + sin(a) * size.minDimension * 0.18f))
                                }
                                drawCircle(colors.butter, size.minDimension * 0.12f, c)
                            }
                        } else {
                            Box(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }.background(if (card.isWord) colors.sky.copy(alpha = 0.35f) else colors.peach.copy(alpha = 0.3f), Radii.card).padding(8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    card.text,
                                    style = if (card.isWord) Kairos.type.heading.copy(fontSize = 16.sp) else Kairos.type.caption.copy(letterSpacing = 0.sp, fontSize = 12.sp, lineHeight = 15.sp),
                                    color = colors.ink, textAlign = TextAlign.Center, maxLines = 5, overflow = TextOverflow.Ellipsis,
                                )
                                if (isMatched) Canvas(Modifier.size(18.dp).align(Alignment.TopEnd)) { drawSparkle(center, size.minDimension / 2, Color(0xFFFFC857)) }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            "Match each word with its meaning. Matched pairs bloom.",
            style = Kairos.type.caption.copy(letterSpacing = 0.sp), color = colors.inkSoft, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = Space.md),
        )
    }
}
