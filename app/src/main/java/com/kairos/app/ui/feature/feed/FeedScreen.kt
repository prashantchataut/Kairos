package com.kairos.app.ui.feature.feed

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.repo.FeedItem
import com.kairos.app.ui.art.Bit
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.art.burst
import com.kairos.app.ui.art.rememberBurst
import com.kairos.app.ui.components.BookCover
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.feature.studio.CardContent
import com.kairos.app.ui.feature.studio.Hue
import com.kairos.app.ui.feature.studio.ShareCard
import com.kairos.app.ui.feature.studio.Template
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.Motion
import com.kairos.app.util.appViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs

/** What a feed page says, in the shape the card designs understand. */
fun FeedItem.card(): CardContent? = when (this) {
    is FeedItem.Word -> CardContent(true, word.word, word.pronunciation, word.partOfSpeech, word.definition, word.example)
    is FeedItem.Thought -> CardContent(false, quote.text, byline = if (quote.isLine) "" else quote.author)
    is FeedItem.Book -> null
    is FeedItem.Pause -> null
}

/** Each page wears a card design chosen from its key, so the same item always looks the same. */
fun FeedItem.template(): Template {
    val pool = when (this) {
        is FeedItem.Thought -> if (quote.kind == "affirmation") listOf(Template.Blunt, Template.Glow, Template.Ring, Template.Leaf, Template.Note, Template.Stickers, Template.Halftone, Template.Breathe)
        else listOf(Template.Note, Template.Eclipse, Template.Stage, Template.Split, Template.Garden, Template.Portal, Template.Bold, Template.Envelope, Template.Cyanotype, Template.Starry)
        else -> listOf(Template.BlurBloom, Template.Poster, Template.Swiss, Template.Bloom, Template.Pixel, Template.Window, Template.Specimen, Template.Note)
    }
    return pool[abs(key.hashCode()) % pool.size]
}

fun FeedItem.hue(): Hue = Hue.entries[abs(key.hashCode() / 7) % Hue.entries.size]

@Composable
fun FeedScreen(openStudio: (String, String) -> Unit, openBook: (String) -> Unit, openLibrary: () -> Unit, openWord: (String) -> Unit) {
    val vm = appViewModel { FeedViewModel(it) }
    val s by vm.state.collectAsStateWithLifecycle()
    val pager = rememberPagerState(pageCount = { s.items.size + 1 })
    LaunchedEffect(pager) { snapshotFlow { pager.settledPage }.distinctUntilChanged().collect { vm.settle(it) } }
    Box(Modifier.fillMaxSize().background(Candy.Navy)) {
        VerticalPager(pager, Modifier.fillMaxSize(), beyondViewportPageCount = 1, key = { if (it < s.items.size) s.items[it].key else "end" }) { page ->
            // Pages scale down and dim as they leave, and rise into place as they arrive.
            val off = (pager.currentPage - page) + pager.currentPageOffsetFraction
            Box(
                Modifier.fillMaxSize().graphicsLayer {
                    val o = off.coerceIn(-1f, 1f)
                    val sc = lerp(1f, 0.9f, abs(o))
                    scaleX = sc; scaleY = sc
                    alpha = lerp(1f, 0.5f, abs(o))
                    translationY = o * size.height * 0.08f
                    clip = true
                    shape = RoundedCornerShape((abs(o) * 40).dp)
                },
            ) {
                if (page < s.items.size) {
                    val item = s.items[page]
                    FeedPage(item, onLike = { vm.like(item) }, onLess = { vm.less(item) }, onShare = {
                        vm.shared(item)
                        when (item) { is FeedItem.Word -> openStudio("word", item.word.id); is FeedItem.Thought -> openStudio("quote", item.quote.id); else -> {} }
                    }, onOpen = {
                        when (item) { is FeedItem.Book -> openBook(item.book.id); is FeedItem.Word -> openWord(item.word.id); else -> {} }
                    })
                } else EndPage(s.loading, s.awake, vm::more)
            }
        }
        // Top: filters and the way into the library, on a soft shade for legibility.
        Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent))).windowInsetsPadding(WindowInsets.statusBars).padding(top = Gap.sm, bottom = Gap.xl)) {
            Row(Modifier.padding(horizontal = Gap.gutter), verticalAlignment = Alignment.CenterVertically) {
                Text("Feed", style = K.type.shout.copy(fontSize = 28.sp), color = Color.White, modifier = Modifier.weight(1f))
                Row(
                    Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape).pressable(label = "Library", onClick = openLibrary).padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Learn, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Library", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                }
            }
            Spacer(Modifier.height(Gap.sm))
            LazyRow(contentPadding = PaddingValues(horizontal = Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.sm)) {
                items(FeedFilter.entries.toList()) { f ->
                    val on = f == s.filter
                    Text(
                        f.label, style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = if (on) Candy.Navy else Color.White,
                        modifier = Modifier.pressable(scale = 0.92f) { vm.setFilter(f) }.clip(CircleShape)
                            .background(if (on) Color.White else Color.White.copy(alpha = 0.16f)).padding(horizontal = 14.dp, vertical = 7.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedPage(item: FeedItem, onLike: () -> Unit, onLess: () -> Unit, onShare: () -> Unit, onOpen: () -> Unit) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val burst = rememberBurst()
    val heart = remember { Animatable(0f) }
    var heartAt by remember { mutableStateOf(Offset.Zero) }
    var told by remember { mutableStateOf(false) }
    val liked = item.liked
    BoxWithConstraints(
        Modifier.fillMaxSize().burst(burst).pointerInput(item.key) {
            detectTapGestures(
                onDoubleTap = { o ->
                    heartAt = o
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (!liked) onLike()
                    scope.launch { heart.snapTo(0f); heart.animateTo(1f, tween(900)) }
                    scope.launch { burst.fire(o, kinds = listOf(Bit.Heart, Bit.Petal, Bit.Spark), colors = listOf(Candy.Coral, Candy.Bubblegum, Candy.Sun, Color.White), count = 18, power = 1.2f) }
                },
                onTap = { onOpen() },
            )
        },
    ) {
        val card = item.card()
        when {
            card != null -> {
                // The card design, laid out on a 360dp artboard the height of this screen.
                val ratio = maxWidth / maxHeight
                val outer = LocalDensity.current
                androidx.compose.runtime.CompositionLocalProvider(LocalDensity provides Density(outer.density * maxWidth.value / 360f, 1f)) {
                    ShareCard(card, item.template(), item.hue(), Modifier.size(360.dp, (360f / ratio).dp))
                }
            }
            item is FeedItem.Book -> BookPage(item)
            else -> BreathePage()
        }
        // The big heart from a double tap.
        if (heart.value in 0.001f..0.999f) {
            val t = heart.value
            Icon(
                Icons.HeartOn, null, tint = Color.White,
                modifier = Modifier.graphicsLayer {
                    translationX = heartAt.x - 48.dp.toPx(); translationY = heartAt.y - 48.dp.toPx() - t * 60.dp.toPx()
                    val sc = if (t < 0.25f) t / 0.25f * 1.25f else 1.25f - (t - 0.25f) * 0.4f
                    scaleX = sc; scaleY = sc; alpha = if (t < 0.7f) 1f else (1f - t) / 0.3f
                }.size(96.dp),
            )
        }
        if (item !is FeedItem.Pause) {
            // Right rail: like, share, less like this.
            Column(Modifier.align(Alignment.BottomEnd).padding(end = Gap.md, bottom = 150.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Gap.lg)) {
                Rail(if (liked) Icons.HeartOn else Icons.Heart, if (liked) "Kept" else "Keep", if (liked) Candy.Coral else Color.White) { onLike() }
                if (item !is FeedItem.Book) Rail(Icons.Share, "Share", Color.White, onShare)
                Rail(Icons.ThumbDown, if (told) "Noted" else "Less", Color.White) { told = true; onLess() }
            }
            val reason = when (item) { is FeedItem.Word -> item.reason; is FeedItem.Thought -> item.reason; is FeedItem.Book -> item.book.hook; else -> "" }
            if (reason.isNotBlank()) {
                Row(
                    Modifier.align(Alignment.BottomStart).padding(start = Gap.gutter, end = 90.dp, bottom = 108.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.32f)).padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Kai(KaiLooks.Curator, size = 20.dp, shadow = false, alive = false, description = null)
                    Spacer(Modifier.width(6.dp))
                    Text(reason, style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = Color.White, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun Rail(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.pressable(label = label, scale = 0.86f, onClick = onClick)) {
        Box(Modifier.size(50.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.28f)).border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
    }
}

/** A book bite: the cover on a soft sky, the line, and the start of the summary. */
@Composable
private fun BookPage(item: FeedItem.Book) {
    val b = item.book
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2B3D8F), Color(0xFF6C7BE0), Color(0xFFF2B8C6)))).padding(horizontal = Gap.xl).padding(top = 130.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("A book worth knowing", style = K.type.label.copy(fontWeight = FontWeight.SemiBold), color = Color.White.copy(alpha = 0.8f))
        Spacer(Modifier.height(Gap.lg))
        BookCover(b.title, b.author, b.id, Modifier.size(width = 160.dp, height = 232.dp).graphicsLayer { rotationZ = -4f })
        Spacer(Modifier.height(Gap.xl))
        Text(b.title, style = K.type.shout.copy(fontSize = 30.sp, lineHeight = 32.sp), color = Color.White, textAlign = TextAlign.Center)
        Text(b.author, style = K.type.label, color = Color.White.copy(alpha = 0.8f))
        Spacer(Modifier.height(Gap.lg))
        Text(b.line.ifBlank { b.summary }, style = K.type.quote.copy(fontSize = 20.sp, lineHeight = 26.sp), color = Color.White, textAlign = TextAlign.Center, maxLines = 5)
        Spacer(Modifier.height(Gap.md))
        Text("Tap for the three-minute version", style = K.type.caption.copy(fontWeight = FontWeight.SemiBold), color = Color.White.copy(alpha = 0.8f))
    }
}

/** A pause (m08): a deep blue orb that slowly grows and shrinks; breathe in as it grows. */
@Composable
private fun BreathePage() {
    val calm = K.calm
    val breath = if (!calm) rememberInfiniteTransition(label = "breath").animateFloat(0f, 1f, infiniteRepeatable(tween(5000), RepeatMode.Reverse), label = "b") else null
    Box(Modifier.fillMaxSize().background(Color(0xFFF2F6FD)), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val t = breath?.value ?: 0.5f
            val r = size.minDimension * (0.28f + 0.16f * t)
            drawCircle(Brush.radialGradient(listOf(Color(0xFF0B1A6E), Color(0xFF1E5BFF), Color(0xFF7FB2FF).copy(alpha = 0.6f), Color.Transparent), center, r * 1.6f), r * 1.6f, center)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val t = breath?.value ?: 0.5f
            Text(if (breath == null) "[breathe]" else if (t > 0.5f) "[breathe out]" else "[breathe in]", style = K.type.serifLarge.copy(fontSize = 22.sp, fontStyle = FontStyle.Normal), color = Color.White)
        }
        Text("A pause, between things.", style = K.type.label, color = Candy.Navy.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 130.dp))
    }
}

@Composable
private fun EndPage(loading: Boolean, awake: Boolean, more: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF3D7BFA), Color(0xFF8EB9FF)))), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Kai(if (loading) KaiLooks.Thinking else if (awake) KaiLooks.Curator else KaiLooks.Sleepy, size = 120.dp)
        Spacer(Modifier.height(Gap.lg))
        Text(if (loading) "Writing more for you…" else if (awake) "That's everything for now" else "Kai is asleep", style = K.type.shout.copy(fontSize = 28.sp), color = Color.White)
        Spacer(Modifier.height(Gap.sm))
        Text(if (awake) "Keep scrolling or tap for more." else "Wake Kai in You → Kai to fill your feed.", style = K.type.body, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.pressable(onClick = more))
    }
}
