package com.kairos.app.ui.screens.studio

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.repo.JoySource
import com.kairos.app.ui.components.GlassButton
import com.kairos.app.ui.components.IconBubble
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.Segmented
import com.kairos.app.ui.components.SelectChip
import com.kairos.app.ui.components.squishClickable
import com.kairos.app.ui.icons.KairosIcons
import com.kairos.app.ui.navigation.Scene
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Space
import com.kairos.app.ui.theme.Wash
import com.kairos.app.util.CardExporter
import com.kairos.app.util.appContainer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/** Cards are laid out on a 360dp-wide artboard and exported at 1080px (3x). */
private const val ARTBOARD_DP = 360f
private const val EXPORT_PX = 1080f

@Composable
fun StudioScreen(kind: String, id: String, onBack: () -> Unit) {
    val c = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val contentFlow = remember(kind, id) {
        if (kind == "word") c.kairos.observeWord(id).map { w ->
            w?.let { CardContent(true, it.word, it.pronunciation, it.partOfSpeech, it.definition, it.example, seed = it.id.hashCode()) }
        } else c.kairos.observeQuote(id).map { q ->
            q?.let { CardContent(false, it.text, byline = it.author, seed = it.id.hashCode()) }
        }
    }
    val content by contentFlow.collectAsStateWithLifecycle(initialValue = null)
    var paletteIdx by rememberSaveable { mutableStateOf(0) }
    var formatIdx by rememberSaveable { mutableStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    val pager = rememberPagerState(pageCount = { CardTemplate.entries.size })
    // Plain map: written during composition, read only on click, so it must not be snapshot state.
    val layers = remember { HashMap<Int, GraphicsLayer>() }
    val palette = CardPalette.entries[paletteIdx]
    val format = CardFormat.entries[formatIdx]
    Scene(Wash.Peach, Wash.Lilac, Wash.Cerulean, Wash.Rose, Wash.Butter)

    fun export(share: Boolean) {
        val layer = layers[pager.currentPage] ?: return
        val card = content ?: return
        if (busy) return
        busy = true
        scope.launch {
            runCatching {
                val bitmap = layer.toImageBitmap().asAndroidBitmap()
                if (share) {
                    CardExporter.share(context, bitmap, card.headline.take(24), if (card.isWord) "${card.headline}: ${card.body}" else "“${card.headline}” — ${card.byline}")
                    c.kairos.addJoy(JoySource.Shared, card.headline.take(40))
                } else {
                    val ok = CardExporter.saveToGallery(context, bitmap, card.headline.take(24))
                    Toast.makeText(context, if (ok) "Saved to Pictures/Kairos" else "Use Share to save on this Android version", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { Toast.makeText(context, "Couldn't make the image. Try again.", Toast.LENGTH_SHORT).show() }
            busy = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding()
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = Space.gutter, vertical = Space.sm), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(KairosIcons.Back, "Back", onBack, blur = true)
            Spacer(Modifier.width(Space.md))
            Column(Modifier.weight(1f)) {
                Text("Card studio", style = Kairos.type.title, color = Kairos.colors.ink)
                Text("Swipe for styles", style = Kairos.type.caption, color = Kairos.colors.inkSoft)
            }
        }
        val card = content
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            if (card != null) {
                val availW = maxWidth - 72.dp
                val availH = maxHeight - Space.xl
                // Fit the artboard into the space available, keeping its aspect ratio.
                val fitW = minOf(availW, availH * format.ratio)
                val scale = with(LocalDensity.current) { fitW.toPx() } / EXPORT_PX
                HorizontalPager(
                    pager,
                    contentPadding = PaddingValues(horizontal = 36.dp),
                    pageSpacing = Space.lg,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    val offset = ((pager.currentPage - page) + pager.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
                    val layer = rememberGraphicsLayer()
                    layers[page] = layer
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val cardH = (ARTBOARD_DP / format.ratio)
                        Box(
                            Modifier
                                .graphicsLayer {
                                    val s = lerp(1f, 0.86f, offset)
                                    scaleX = scale * s; scaleY = scale * s
                                    rotationZ = lerp(0f, if (page > pager.currentPage) 4f else -4f, offset)
                                    alpha = lerp(1f, 0.6f, offset)
                                }
                        ) {
                            Artboard(
                                widthDp = ARTBOARD_DP, heightDp = cardH,
                                modifier = Modifier
                                    .shadow(28.dp, androidx.compose.foundation.shape.RoundedCornerShape(10.dp), ambientColor = Kairos.colors.glassShadow, spotColor = Kairos.colors.glassShadow)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                                    .drawWithContent {
                                        layer.record { this@drawWithContent.drawContent() }
                                        drawLayer(layer)
                                    },
                            ) {
                                ShareCard(card, CardTemplate.entries[page], palette, Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
        // Template dots.
        Row(Modifier.fillMaxWidth().padding(vertical = Space.sm), horizontalArrangement = Arrangement.Center) {
            CardTemplate.entries.forEachIndexed { i, _ ->
                val w by animateDpAsState(if (i == pager.currentPage) 22.dp else 8.dp, label = "dot")
                val col by animateColorAsState(if (i == pager.currentPage) Kairos.colors.accent else Kairos.colors.inkFaint.copy(alpha = 0.4f), label = "dotc")
                Box(Modifier.padding(3.dp).height(8.dp).width(w).clip(CircleShape).background(col))
            }
        }
        Text(
            CardTemplate.entries[pager.currentPage].label,
            style = Kairos.type.label, color = Kairos.colors.ink,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(Space.md))
        LazyRow(contentPadding = PaddingValues(horizontal = Space.gutter), horizontalArrangement = Arrangement.spacedBy(Space.md)) {
            items(CardPalette.entries.toList()) { p ->
                val selected = p.ordinal == paletteIdx
                val ring by animateFloatAsState(if (selected) 1f else 0f, label = "ring")
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.squishClickable(label = p.label) { paletteIdx = p.ordinal }) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .border((1 + 2 * ring).dp, if (selected) Kairos.colors.ink else Kairos.colors.glassRimTop, CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Brush.sweepGradient(p.washes + p.washes.first()))
                    )
                    Text(p.label, style = Kairos.type.caption, color = if (selected) Kairos.colors.ink else Kairos.colors.inkSoft)
                }
            }
        }
        Spacer(Modifier.height(Space.md))
        Segmented(CardFormat.entries.map { it.label }, formatIdx, { formatIdx = it }, Modifier.padding(horizontal = Space.gutter).fillMaxWidth())
        Spacer(Modifier.height(Space.md))
        Row(Modifier.padding(horizontal = Space.gutter, vertical = Space.sm), horizontalArrangement = Arrangement.spacedBy(Space.md)) {
            GlassButton("Save", { export(share = false) }, Modifier.weight(1f), icon = KairosIcons.Download)
            PrimaryButton(if (busy) "Painting…" else "Share", { export(share = true) }, Modifier.weight(1.4f), icon = KairosIcons.Share, enabled = !busy && content != null)
        }
    }
}

/**
 * Lays its content out at exactly [widthDp]×[heightDp] artboard units, with a density chosen so the
 * artboard is [EXPORT_PX] pixels wide. Text scale is pinned to 1 so cards look the same everywhere.
 */
@Composable
private fun Artboard(widthDp: Float, heightDp: Float, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val outer = LocalDensity.current
    val artDensity = Density(EXPORT_PX / widthDp, 1f)
    // Size in the outer density that equals EXPORT_PX pixels.
    val wOuter = with(outer) { EXPORT_PX.toDp() }
    val hOuter = with(outer) { (EXPORT_PX * heightDp / widthDp).toDp() }
    val layoutDirection = LocalLayoutDirection.current
    Box(modifier.requiredSize(wOuter, hOuter)) {
        CompositionLocalProvider(LocalDensity provides artDensity, LocalLayoutDirection provides layoutDirection) {
            Box(Modifier.fillMaxSize()) { content() }
        }
    }
}

