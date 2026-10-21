package com.kairos.app.ui.feature.studio

import com.kairos.app.ui.theme.canvas
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.repo.MomentKind
import com.kairos.app.ui.components.Icons
import com.kairos.app.ui.components.PrimaryButton
import com.kairos.app.ui.components.RoundIcon
import com.kairos.app.ui.components.SecondaryButton
import com.kairos.app.ui.components.Segmented
import com.kairos.app.ui.components.pressable
import com.kairos.app.ui.theme.Gap
import com.kairos.app.ui.theme.K
import com.kairos.app.util.CardExporter
import com.kairos.app.util.appContainer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private const val ART_DP = 360f
private const val EXPORT_PX = 1080f

@Composable
fun StudioScreen(kind: String, id: String, onBack: () -> Unit) {
    val c = appContainer()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val flow = remember(kind, id) {
        if (kind == "word") c.kairos.observeWord(id).map { w -> w?.let { CardContent(true, it.word, it.pronunciation, it.partOfSpeech, it.definition, it.example) } }
        else c.kairos.observeQuote(id).map { q -> q?.let { CardContent(false, it.text, byline = it.author) } }
    }
    val content by flow.collectAsStateWithLifecycle(initialValue = null)
    var hue by rememberSaveable { mutableIntStateOf(0) }
    var format by rememberSaveable { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    val pager = rememberPagerState(pageCount = { Template.entries.size })
    // Plain map: written during composition, read only on tap — must not be snapshot state.
    val layers = remember { HashMap<Int, GraphicsLayer>() }
    val p = K.colors
    val fmt = Format.entries[format]

    fun export(share: Boolean) {
        val layer = layers[pager.currentPage] ?: return
        val card = content ?: return
        if (busy) return
        busy = true
        scope.launch {
            runCatching {
                val bmp = layer.toImageBitmap().asAndroidBitmap()
                if (share) {
                    CardExporter.share(context, bmp, card.headline.take(24), if (card.isWord) "${card.headline}: ${card.body}" else "“${card.headline}” — ${card.byline}")
                    c.kairos.addMoment(MomentKind.Shared, card.headline.take(40))
                } else {
                    val ok = CardExporter.saveToGallery(context, bmp, card.headline.take(24))
                    Toast.makeText(context, if (ok) "Saved to Pictures/Kairos" else "Use Share to save on this Android version", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { Toast.makeText(context, "Couldn't make the image. Try again.", Toast.LENGTH_SHORT).show() }
            busy = false
        }
    }

    Column(Modifier.fillMaxSize().background(p.canvas).windowInsetsPadding(WindowInsets.statusBars).navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter, vertical = Gap.sm), verticalAlignment = Alignment.CenterVertically) {
            RoundIcon(Icons.Back, "Back", onBack)
            Spacer(Modifier.width(Gap.md))
            Column(Modifier.weight(1f)) {
                Text("Make a card", style = K.type.title, color = p.ink)
                Text("Swipe for styles", style = K.type.caption, color = p.inkSoft)
            }
        }
        val card = content
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            if (card != null) {
                val fitW = minOf(maxWidth - 80.dp, (maxHeight - Gap.xl) * fmt.ratio)
                val scale = with(LocalDensity.current) { fitW.toPx() } / EXPORT_PX
                HorizontalPager(pager, contentPadding = PaddingValues(horizontal = 40.dp), pageSpacing = Gap.lg, modifier = Modifier.fillMaxSize()) { page ->
                    val off = ((pager.currentPage - page) + pager.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
                    val layer = rememberGraphicsLayer()
                    layers[page] = layer
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(Modifier.graphicsLayer { val s = lerp(1f, 0.88f, off); scaleX = scale * s; scaleY = scale * s; alpha = lerp(1f, 0.55f, off) }) {
                            Artboard(ART_DP / fmt.ratio, Modifier.shadow(30.dp, RoundedCornerShape(36.dp), ambientColor = p.shadow, spotColor = p.shadow).clip(RoundedCornerShape(36.dp)).drawWithContent {
                                layer.record { this@drawWithContent.drawContent() }
                                drawLayer(layer)
                            }) { ShareCard(card, Template.entries[page], Hue.entries[hue], Modifier.fillMaxSize()) }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = Gap.sm), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Template.entries.forEachIndexed { i, _ ->
                val w by animateDpAsState(if (i == pager.currentPage) 20.dp else 6.dp, label = "dot")
                val col by animateColorAsState(if (i == pager.currentPage) p.ink else p.inkFaint.copy(alpha = 0.4f), label = "dotc")
                Box(Modifier.padding(3.dp).height(6.dp).width(w).clip(CircleShape).background(col))
            }
            Spacer(Modifier.width(Gap.md))
            Text(Template.entries[pager.currentPage].label, style = K.type.label, color = p.ink)
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = Gap.gutter, vertical = Gap.sm), horizontalArrangement = Arrangement.spacedBy(Gap.md), verticalAlignment = Alignment.CenterVertically) {
            Hue.entries.forEach { h ->
                val on = h.ordinal == hue
                Box(
                    Modifier.size(36.dp).pressable(label = h.label) { hue = h.ordinal }
                        .border(if (on) 2.dp else 1.dp, if (on) p.ink else p.line, CircleShape).padding(4.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(h.a, h.b)))
                )
            }
            Spacer(Modifier.weight(1f))
        }
        Segmented(Format.entries.map { it.label }, format, { format = it }, Modifier.padding(horizontal = Gap.gutter).fillMaxWidth())
        Row(Modifier.padding(Gap.gutter), horizontalArrangement = Arrangement.spacedBy(Gap.md)) {
            SecondaryButton("Save", { export(false) }, Modifier.weight(1f), icon = Icons.Download)
            PrimaryButton(if (busy) "Preparing…" else "Share", { export(true) }, Modifier.weight(1.4f), icon = Icons.Share, enabled = !busy && content != null, dark = true)
        }
    }
}

/** Lays content out on a 360dp-wide artboard whose density makes it exactly 1080px wide. */
@Composable
private fun Artboard(heightDp: Float, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val outer = LocalDensity.current
    val w = with(outer) { EXPORT_PX.toDp() }
    val h = with(outer) { (EXPORT_PX * heightDp / ART_DP).toDp() }
    Box(modifier.requiredSize(w, h)) {
        CompositionLocalProvider(LocalDensity provides Density(EXPORT_PX / ART_DP, 1f)) { Box(Modifier.fillMaxSize()) { content() } }
    }
}
