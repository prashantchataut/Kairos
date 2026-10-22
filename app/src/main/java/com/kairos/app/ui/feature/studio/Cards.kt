package com.kairos.app.ui.feature.studio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.ui.art.drawGrain
import com.kairos.app.ui.components.FitText
import com.kairos.app.ui.components.drawMark
import com.kairos.app.ui.theme.Chunky
import com.kairos.app.ui.theme.Hand
import com.kairos.app.ui.theme.Sans
import com.kairos.app.ui.theme.Serif
import com.kairos.app.ui.theme.SerifText
import kotlin.math.abs
import kotlin.random.Random

data class CardContent(
    val isWord: Boolean,
    val headline: String,
    val pronunciation: String = "",
    val partOfSpeech: String = "",
    val body: String = "",
    val example: String = "",
    val byline: String = "",
)

/** Share-card templates, each drawn from a reference in issue #606. */
enum class Template(val label: String) {
    Note("Note"), Poster("Poster"), Bold("Bold"), Blunt("Blunt"), Stickers("Stickers"), Stage("Stage"),
    Starry("Starry"), Ring("Ring"), Envelope("Envelope"), Split("Split"), Garden("Garden"), Leaf("Leaf"),
    Swiss("Swiss"), Glow("Glow"), Bloom("Bloom"), Portal("Portal"),
    BlurBloom("Blur bloom"), Halftone("Halftone"), Eclipse("Eclipse"), Breathe("Breathe"),
    Cyanotype("Cyanotype"), Pixel("Pixel"), Window("Window"), Specimen("Specimen"),
}

/** The three shapes a card is designed for. Feed pages taller than a story use the story design. */
enum class CardShape { Square, Pin, Story }

enum class Format(val label: String, val ratio: Float) { Pin("Pin", 2f / 3f), Story("Story", 9f / 16f), Square("Square", 1f) }

/** Colour moods for the cards. Each template reads them its own way. */
enum class Hue(val label: String, val a: Color, val b: Color, val pastel: Color, val ink: Color, val bold: Color) {
    Sky("Sky", Color(0xFF3B82F0), Color(0xFFA9CBFA), Color(0xFFDCEAFD), Color(0xFF14306E), Color(0xFF2A14C2)),
    Mint("Mint", Color(0xFF16A37A), Color(0xFFA8E6CF), Color(0xFFD5F0E6), Color(0xFF0E4A38), Color(0xFF0B7A55)),
    Lavender("Lavender", Color(0xFF7A5CF0), Color(0xFFD2C4FF), Color(0xFFE7E0FB), Color(0xFF2E2470), Color(0xFF6A22D9)),
    Peach("Peach", Color(0xFFF07F4F), Color(0xFFFFC9AE), Color(0xFFFCE5D8), Color(0xFF6B2C12), Color(0xFFF0501E)),
    Ink("Ink", Color(0xFF0E1726), Color(0xFF3A4A68), Color(0xFFE9EDF3), Color(0xFF0E1726), Color(0xFF111111)),
}

private val serif = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold)
private val serifText = TextStyle(fontFamily = SerifText)
private val sans = TextStyle(fontFamily = Sans)
private val hand = TextStyle(fontFamily = Hand, fontWeight = FontWeight.Bold)
private val chunky = TextStyle(fontFamily = Chunky, fontWeight = FontWeight.Black)
private val plain = TextStyle(fontFamily = FontFamily.SansSerif)

private val CardContent.small get() = if (isWord) body else byline
private val CardContent.meta get() = listOf(pronunciation, partOfSpeech).filter { it.isNotBlank() }.joinToString(" · ")

/** Grain drawn over a background but under the content, so paper looks printed and text stays crisp. */
private fun Modifier.grain(alpha: Float) = drawBehind { drawGrain(alpha) }

/**
 * Every card is laid out on a 360dp-wide artboard; the studio scales it and the exporter renders
 * it at 1080px. Height depends on the format (360 square, 540 pin, 640 story). Templates are built
 * from zones (art, text, footer) in a column, and text always fits its own zone via [FitText], so
 * nothing overlaps in any format.
 */
@Composable
fun ShareCard(c: CardContent, t: Template, h: Hue, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.clipToBounds()) {
        val ratio = maxHeight / maxWidth
        val shape = when { ratio < 1.2f -> CardShape.Square; ratio < 1.62f -> CardShape.Pin; else -> CardShape.Story }
        val square = shape == CardShape.Square
        when (t) {
            Template.Note -> NoteCard(c, h, square)
            Template.Poster -> PosterCard(c, h, square)
            Template.Bold -> BoldCard(c, h)
            Template.Blunt -> BluntCard(c, h)
            Template.Stickers -> StickersCard(c, h)
            Template.Stage -> StageCard(c, h)
            Template.Starry -> StarryCard(c, shape)
            Template.Ring -> RingCard(c, h)
            Template.Envelope -> EnvelopeCard(c, h)
            Template.Split -> SplitCard(c, h)
            Template.Garden -> GardenCard(c, h, shape)
            Template.Leaf -> LeafCard(c, h)
            Template.Swiss -> SwissCard(c, h, square)
            Template.Glow -> GlowCard(c, h, shape)
            Template.Bloom -> BloomCard(c, h, shape)
            Template.Portal -> PortalCard(c, h, shape)
            Template.BlurBloom -> BlurBloomCard(c, h, shape)
            Template.Halftone -> HalftoneCard(c, h, shape)
            Template.Eclipse -> EclipseCard(c, h, shape)
            Template.Breathe -> BreatheCard(c, h)
            Template.Cyanotype -> CyanotypeCard(c, h)
            Template.Pixel -> PixelCard(c, h, shape)
            Template.Window -> WindowCard(c, h, shape)
            Template.Specimen -> SpecimenCard(c, h, shape)
        }
    }
}

@Composable
private fun Brand(color: Color, modifier: Modifier = Modifier, size: Int = 15) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(size.dp)) { drawMark(color, 0f) }
        Spacer(Modifier.width(6.dp))
        Text("Kairos", style = serif.copy(fontSize = (size + 2).sp), color = color)
    }
}

/** The small line under a headline: pronunciation and part of speech for words, the author for thoughts. */
@Composable
private fun Small(c: CardContent, color: Color, modifier: Modifier = Modifier, style: TextStyle = sans, align: TextAlign = TextAlign.Start) {
    val text = if (c.isWord) c.meta else c.byline.let { if (it.isBlank()) "" else "— $it" }
    if (text.isNotBlank()) Text(text, style = style.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = align), color = color, modifier = modifier, maxLines = 1)
}

/** A word card shows the word big and the meaning under it; a thought card shows the thought. */
@Composable
private fun ColumnScope.Words(
    c: CardContent, ink: Color, soft: Color, bigStyle: TextStyle, bodyStyle: TextStyle,
    align: TextAlign = TextAlign.Start, maxBig: Int = 72, maxQuote: Int = 34, weight: Float = 1f,
) {
    val h = if (align == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start
    if (c.isWord) {
        // The word sits on its pronunciation line; the meaning hangs from it.
        FitText(c.headline, bigStyle, ink, Modifier.fillMaxWidth().weight(weight * 0.55f), max = maxBig.sp, min = 22.sp, lineHeight = 1.0f, align = align, contentAlignment = Alignment.BottomStart.let { if (h == Alignment.CenterHorizontally) Alignment.BottomCenter else it })
        Small(c, soft, Modifier.fillMaxWidth(), align = align)
        Spacer(Modifier.height(10.dp))
        FitText(c.body, bodyStyle, ink, Modifier.fillMaxWidth().weight(weight * 0.45f), max = 19.sp, min = 10.sp, lineHeight = 1.3f, align = align, contentAlignment = if (h == Alignment.CenterHorizontally) Alignment.TopCenter else Alignment.TopStart)
    } else {
        FitText(c.headline, bigStyle, ink, Modifier.fillMaxWidth().weight(weight), max = maxQuote.sp, min = 12.sp, lineHeight = 1.18f, align = align, contentAlignment = if (h == Alignment.CenterHorizontally) Alignment.Center else Alignment.CenterStart)
        Spacer(Modifier.height(12.dp))
        Small(c, soft, Modifier.fillMaxWidth(), align = align)
    }
}

// 1. Note (n10): cream paper, a cobalt serif, nothing else.
@Composable
private fun NoteCard(c: CardContent, h: Hue, square: Boolean) {
    val ink = if (h == Hue.Sky) Color(0xFF1F56C9) else h.a
    Column(Modifier.fillMaxSize().background(Color(0xFFFBF6EE)).grain(0.05f).padding(horizontal = 34.dp, vertical = if (square) 28.dp else 56.dp)) {
        Spacer(Modifier.weight(0.25f))
        Words(c, ink, ink.copy(alpha = 0.7f), serif.copy(fontWeight = FontWeight.Normal, letterSpacing = (-0.02).sp), serifText, TextAlign.Center, weight = 1.6f)
        Spacer(Modifier.weight(0.25f))
        Brand(ink.copy(alpha = 0.6f), Modifier.align(Alignment.CenterHorizontally), 13)
    }
}

// 2. Poster (r18): grainy out-of-focus sky and meadow, a dictionary header, the word huge at the bottom.
@Composable
private fun PosterCard(c: CardContent, h: Hue, square: Boolean) {
    val (top, low) = when (h) {
        Hue.Sky -> Color(0xFF7FB2F0) to Color(0xFF4E8F3A)
        Hue.Mint -> Color(0xFF8FD9C4) to Color(0xFF1F6E4C)
        Hue.Lavender -> Color(0xFFB8A6F5) to Color(0xFFD9708F)
        Hue.Peach -> Color(0xFFFFC28F) to Color(0xFFD9542B)
        Hue.Ink -> Color(0xFF2A3A6B) to Color(0xFF0B0F1E)
    }
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.linearGradient(listOf(top, lerp(top, low, 0.55f), low), Offset.Zero, Offset(size.width * 0.7f, size.height)))
            fun blob(o: Offset, r: Float, col: Color) = drawCircle(Brush.radialGradient(listOf(col, col.copy(alpha = 0f)), o, r), r, o)
            blob(Offset(size.width * 0.75f, size.height * 0.35f), size.width * 0.75f, lerp(low, Color.Black, 0.15f).copy(alpha = 0.7f))
            blob(Offset(size.width * 0.15f, size.height * 0.6f), size.width * 0.6f, lerp(low, Color.White, 0.25f).copy(alpha = 0.6f))
            blob(Offset(size.width * 0.05f, size.height * 0.08f), size.width * 0.55f, lerp(top, Color.White, 0.3f).copy(alpha = 0.8f))
            drawGrain(0.24f)
        }
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(if (c.isWord) "${c.headline.lowercase()} · ${c.partOfSpeech}" else c.byline.ifBlank { "a thought" }, style = sans.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = Color.White, maxLines = 1)
                    Spacer(Modifier.height(3.dp))
                    if (c.isWord) FitText(c.body, sans, Color.White.copy(alpha = 0.92f), Modifier.fillMaxWidth(0.9f).height(if (square) 34.dp else 52.dp), max = 12.sp, min = 8.sp, lineHeight = 1.3f)
                    else Text("for today", style = sans.copy(fontSize = 12.sp), color = Color.White.copy(alpha = 0.9f))
                }
                PixelFlower(Modifier.size(30.dp))
            }
            Spacer(Modifier.weight(if (c.isWord) 0.9f else 0.4f))
            if (c.isWord) FitText(c.headline, serif.copy(letterSpacing = (-0.03).sp), Color.White, Modifier.fillMaxWidth().weight(0.6f), max = 110.sp, min = 30.sp, lineHeight = 0.95f)
            else FitText(c.headline, serif, Color.White, Modifier.fillMaxWidth().weight(1f), max = 36.sp, min = 14.sp, lineHeight = 1.08f)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Kairos", style = sans.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.8f)))
                Spacer(Modifier.width(10.dp))
                Text(if (c.isWord) "a word for today" else "a thought for today", style = sans.copy(fontSize = 11.sp), color = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}

@Composable
private fun PixelFlower(modifier: Modifier) = Canvas(modifier) {
    val u = size.width / 7f
    val px = listOf(3 to 0, 3 to 1, 0 to 3, 1 to 3, 5 to 3, 6 to 3, 3 to 5, 3 to 6, 1 to 1, 5 to 1, 1 to 5, 5 to 5, 2 to 2, 4 to 2, 2 to 4, 4 to 4, 3 to 3, 2 to 3, 4 to 3, 3 to 2, 3 to 4)
    px.forEach { (x, y) -> drawRect(Color(0xFFFFE45C), Offset(x * u, y * u), Size(u, u)) }
}

// 3. Bold (r22): one saturated colour, white lowercase grotesk, lines staggered to different indents.
@Composable
private fun BoldCard(c: CardContent, h: Hue) {
    val text = if (c.isWord) "${c.headline.lowercase()}: ${c.body.lowercase().trimEnd('.')}." else c.headline.lowercase().trimEnd('.') + "."
    Column(Modifier.fillMaxSize().background(h.bold).padding(26.dp)) {
        Staggered(text, sans.copy(fontWeight = FontWeight.Medium, letterSpacing = (-0.04).sp), Color.White, Modifier.fillMaxWidth().weight(1f))
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Brand(Color.White, Modifier.weight(1f))
            if (!c.isWord && c.byline.isNotBlank()) Text(c.byline.lowercase(), style = sans.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium), color = Color.White.copy(alpha = 0.85f), maxLines = 1)
        }
    }
}

/** Lines of two to four words, each starting at a different indent, sized to fill the box. */
@Composable
private fun Staggered(text: String, style: TextStyle, color: Color, modifier: Modifier) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val words = text.split(" ").filter { it.isNotBlank() }
    val indents = listOf(0f, 0.22f, 0.08f, 0.3f, 0.14f, 0.04f, 0.26f, 0.1f, 0.18f)
    BoxWithConstraints(modifier) {
        val w = constraints.maxWidth.toFloat()
        val hgt = constraints.maxHeight.toFloat()
        fun sizeFor(lines: List<String>): Float {
            var s = 72f
            while (s > 10f) {
                val st = style.copy(fontSize = s.sp, lineHeight = (s * 1.04f).sp)
                val widest = lines.mapIndexed { i, l -> indents[i % indents.size] * w + measurer.measure(l, st, maxLines = 1, softWrap = false).size.width }.maxOrNull() ?: 0f
                val tall = lines.size * with(density) { (s * 1.04f).sp.toPx() }
                if (widest <= w && tall <= hgt) break
                s -= 1.5f
            }
            return s
        }
        // Try two to five words a line and keep whichever lets the type be biggest.
        val (lines, size) = remember(text, w, hgt) {
            (2..5).map { per -> words.chunked(per).map { it.joinToString(" ") } }.map { it to sizeFor(it) }.maxBy { it.second }
        }
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            lines.forEachIndexed { i, l ->
                Text(l, style = style.copy(fontSize = size.sp, lineHeight = (size * 1.04f).sp), color = color, maxLines = 1, softWrap = false,
                    modifier = Modifier.padding(start = with(density) { (indents[i % indents.size] * w).toDp() }))
            }
        }
    }
}

// 4. Blunt (n13, n14): flat butter or acid lime, plain grotesk, the text is the whole design.
@Composable
private fun BluntCard(c: CardContent, h: Hue) {
    val (bg, ink) = when (h) {
        Hue.Sky, Hue.Ink -> Color(0xFFD7FF2E) to Color(0xFF1B5CF0)
        Hue.Peach -> Color(0xFFFFF27A) to Color(0xFF111111)
        Hue.Mint -> Color(0xFFB8F5D0) to Color(0xFF0B3D2A)
        Hue.Lavender -> Color(0xFFE5DBFF) to Color(0xFF3A1FB8)
    }
    val text = if (c.isWord) "${c.headline.lowercase()}:\n${c.body.lowercase()}" else c.headline.lowercase()
    Column(Modifier.fillMaxSize().background(bg).padding(26.dp)) {
        FitText(text, plain.copy(letterSpacing = (-0.03).sp), ink, Modifier.fillMaxWidth().weight(1f), max = 80.sp, min = 14.sp, lineHeight = 1.02f)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Brand(ink, Modifier.weight(1f), 13)
            if (!c.isWord && c.byline.isNotBlank()) Text(c.byline.lowercase(), style = plain.copy(fontSize = 13.sp), color = ink, maxLines = 1)
        }
    }
}

// 5. Stickers (n07): paper and shiny stickers. Stickers live only in the margin band; the text has
//    its own box in the middle, so they never touch.
@Composable
private fun StickersCard(c: CardContent, h: Hue) {
    val ink = Color(0xFF111111)
    Box(Modifier.fillMaxSize().background(Color(0xFFF2EFE8)).grain(0.07f)) {
        Canvas(Modifier.fillMaxSize()) {
            val spots = listOf(
                Offset(0.1f, 0.08f), Offset(0.5f, 0.06f), Offset(0.88f, 0.09f), Offset(0.07f, 0.35f), Offset(0.93f, 0.37f),
                Offset(0.08f, 0.63f), Offset(0.93f, 0.62f), Offset(0.12f, 0.91f), Offset(0.48f, 0.94f), Offset(0.86f, 0.91f),
                Offset(0.3f, 0.05f), Offset(0.7f, 0.95f),
            )
            val r = size.minDimension * 0.04f
            spots.forEachIndexed { i, o -> sticker(i + h.ordinal, Offset(o.x * size.width, o.y * size.height), r * (if (i % 3 == 0) 1.2f else 0.9f)) }
        }
        Column(Modifier.fillMaxSize().padding(horizontal = 66.dp, vertical = 52.dp), verticalArrangement = Arrangement.Center) {
            if (c.isWord) {
                FitText(c.headline, sans.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.04).sp), ink, Modifier.fillMaxWidth().weight(0.5f), max = 56.sp, min = 18.sp, lineHeight = 0.95f, contentAlignment = Alignment.BottomStart)
                Small(c, ink.copy(alpha = 0.6f), Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                FitText(c.body, sans.copy(fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic), ink, Modifier.fillMaxWidth().weight(0.5f), max = 20.sp, min = 10.sp, lineHeight = 1.15f)
            } else {
                FitText(c.headline, sans.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.04).sp), ink, Modifier.fillMaxWidth().weight(1f), max = 40.sp, min = 12.sp, lineHeight = 0.98f)
                Small(c, ink.copy(alpha = 0.6f), Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        }
    }
}

// 6. Stage (n08): pink curtains, a stippled sky, a chunky storybook serif, pines along the bottom.
@Composable
private fun StageCard(c: CardContent, h: Hue) {
    val curtainColor = when (h) { Hue.Mint -> Color(0xFF6FBF8E); Hue.Lavender -> Color(0xFFB89BEA); Hue.Peach -> Color(0xFFF2A0A0); Hue.Ink -> Color(0xFF8C2F3F); else -> Color(0xFFF28FA8) }
    val ink = Color(0xFF3A2620)
    Box(Modifier.fillMaxSize().background(Color(0xFFFFF6DA))) {
        Canvas(Modifier.fillMaxSize()) {
            stipple(Rect(0f, 0f, size.width, size.height), listOf(Color(0xFFA9CFF2), Color(0xFFC8E2F7), Color.White), density * 1.3f, 8)
            drawRect(Color(0xFF8A5A3C), Offset.Zero, Size(size.width, size.height * 0.03f))
            val cw = size.width * 0.17f
            curtain(true, cw, size.height * 0.92f, curtainColor)
            curtain(false, cw, size.height * 0.92f, curtainColor)
            val pineH = size.height * 0.1f
            val r = Random(4)
            var x = size.width * 0.22f
            while (x < size.width * 0.8f) { pine(Offset(x, size.height + pineH * 0.1f), pineH * (0.7f + r.nextFloat() * 0.5f), Color(0xFF6FAA5E)); x += size.width * (0.07f + r.nextFloat() * 0.05f) }
        }
        Column(Modifier.fillMaxSize().padding(start = 68.dp, end = 68.dp, top = 30.dp, bottom = 60.dp), verticalArrangement = Arrangement.Center) {
            if (c.isWord) {
                FitText(c.headline.lowercase(), chunky, ink, Modifier.fillMaxWidth().weight(0.55f), max = 60.sp, min = 18.sp, lineHeight = 0.95f, align = TextAlign.Center, contentAlignment = Alignment.BottomCenter)
                Small(c, ink.copy(alpha = 0.7f), Modifier.fillMaxWidth(), align = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                FitText(c.body.lowercase(), chunky.copy(fontWeight = FontWeight.Normal), ink, Modifier.fillMaxWidth().weight(0.45f), max = 22.sp, min = 10.sp, lineHeight = 1.05f, align = TextAlign.Center, contentAlignment = Alignment.TopCenter)
            } else {
                FitText(c.headline.lowercase(), chunky, ink, Modifier.fillMaxWidth().weight(1f), max = 46.sp, min = 12.sp, lineHeight = 0.95f, align = TextAlign.Center)
                Small(c, ink.copy(alpha = 0.7f), Modifier.fillMaxWidth().padding(top = 6.dp), align = TextAlign.Center)
            }
        }
    }
}

// 7. Starry (n09): a sunset sky with hand-lettered words; the sleeping star in a flowery meadow.
//    Story and pin stack sky over meadow; square puts the star's hill on the left and words on the right.
@Composable
private fun StarryCard(c: CardContent, shape: CardShape) {
    val ink = Color(0xFF7A4B12)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF9FB8F0), Color(0xFFF2B8C6), Color(0xFFFFD9A8))))) {
        ArtAndText(shape, artFraction = when (shape) { CardShape.Story -> 0.48f; CardShape.Pin -> 0.45f; else -> 0.38f }, artFirst = false,
            art = { m -> Box(m) {
                Canvas(Modifier.fillMaxSize()) {
                    val top = if (shape == CardShape.Square) size.height * 0.45f else size.height * 0.15f
                    val meadow = Path().apply { moveTo(0f, top + size.height * 0.1f); cubicTo(size.width * 0.3f, top, size.width * 0.7f, top + size.height * 0.15f, size.width, top + size.height * 0.05f); lineTo(size.width, size.height); lineTo(0f, size.height); close() }
                    drawPath(meadow, Brush.verticalGradient(listOf(Color(0xFFB8E06A), Color(0xFF7DC24A)), startY = top))
                    val r = Random(9)
                    repeat(60) {
                        val o = Offset(r.nextFloat() * size.width, top + (size.height - top) * (0.15f + r.nextFloat() * 0.85f))
                        daisy(o, density * (2.5f + r.nextFloat() * 2.5f), listOf(Color.White, Color(0xFFFFE27A), Color(0xFFFFB8D0))[r.nextInt(3)], Color(0xFFF2A21F), petals = 5)
                    }
                    val sr = minOf(size.width, size.height - top) * 0.44f
                    sleepyStar(Offset(size.width * 0.5f, top + (size.height - top) * 0.52f), sr, -14f)
                }
                Brand(Color(0xFF2E5A1C), Modifier.align(Alignment.BottomStart).padding(14.dp), 11)
            } },
            text = {
                Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = if (shape == CardShape.Square) 24.dp else 6.dp)) {
                    if (c.isWord) {
                        FitText(c.headline.uppercase(), hand, ink, Modifier.fillMaxWidth().weight(0.45f), max = 54.sp, min = 16.sp, lineHeight = 0.95f, contentAlignment = Alignment.BottomStart)
                        FitText(c.body, hand.copy(fontWeight = FontWeight.Medium), ink, Modifier.fillMaxWidth().weight(0.55f), max = 26.sp, min = 11.sp, lineHeight = 1.0f)
                    } else {
                        FitText(c.headline.uppercase(), hand, ink, Modifier.fillMaxWidth().weight(1f), max = 40.sp, min = 11.sp, lineHeight = 1.0f, contentAlignment = Alignment.CenterStart)
                        Small(c, ink.copy(alpha = 0.75f), Modifier.fillMaxWidth(), style = hand)
                    }
                }
            })
    }
}

/**
 * Lays out an art zone and a text zone for a card shape: stacked for story and pin (art takes
 * [artFraction] of the height), side by side for square (art takes [artFraction] of the width).
 */
@Composable
private fun ArtAndText(shape: CardShape, artFraction: Float, artFirst: Boolean = true, squareSideBySide: Boolean = true, art: @Composable (Modifier) -> Unit, text: @Composable () -> Unit) {
    if (shape == CardShape.Square && squareSideBySide) {
        Row(Modifier.fillMaxSize()) {
            if (artFirst) { art(Modifier.fillMaxHeight().weight(artFraction)); Box(Modifier.fillMaxHeight().weight(1f - artFraction)) { text() } }
            else { Box(Modifier.fillMaxHeight().weight(1f - artFraction)) { text() }; art(Modifier.fillMaxHeight().weight(artFraction)) }
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            if (artFirst) { art(Modifier.fillMaxWidth().weight(artFraction)); Box(Modifier.fillMaxWidth().weight(1f - artFraction)) { text() } }
            else { Box(Modifier.fillMaxWidth().weight(1f - artFraction)) { text() }; art(Modifier.fillMaxWidth().weight(artFraction)) }
        }
    }
}

// 8. Ring (n11): sand paper, soft watercolour rings, handwriting inside the centre.
@Composable
private fun RingCard(c: CardContent, h: Hue) {
    val rings = when (h) {
        Hue.Sky -> listOf(Color(0xFF7F9FCF), Color(0xFFA7C1E0), Color(0xFFD9B0B0), Color(0xFFCC7A86))
        Hue.Mint, Hue.Ink -> listOf(Color(0xFF8A9A4F), Color(0xFFB4BC86), Color(0xFFD9A8A0), Color(0xFFC86F72))
        Hue.Lavender -> listOf(Color(0xFF9787C7), Color(0xFFC2B4E0), Color(0xFFD9A8C0), Color(0xFFD0708F))
        Hue.Peach -> listOf(Color(0xFFD98F5C), Color(0xFFEDB98F), Color(0xFFE8B49C), Color(0xFFD26B6B))
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFFDCD4C6)).grain(0.12f), contentAlignment = Alignment.Center) {
        val d = minOf(maxWidth, maxHeight) * 0.9f
        Canvas(Modifier.fillMaxSize()) {
            val r = minOf(size.width, size.height) * 0.45f
            watercolourRings(center, r, rings, 11)
            listOf(Offset(-0.75f, -0.7f), Offset(0.85f, -0.35f), Offset(-0.88f, 0.4f), Offset(-0.6f, 0.8f)).forEach { o ->
                daisy(Offset(center.x + o.x * r, center.y + o.y * r), density * 6f, Color.White.copy(alpha = 0.9f), Color.White, petals = 5)
            }
        }
        val inner = d * 0.5f
        Column(Modifier.size(inner, inner), verticalArrangement = Arrangement.Center) {
            val text = if (c.isWord) "${c.headline} —\n${c.body}" else c.headline
            FitText(text, hand.copy(fontWeight = FontWeight.Bold, shadow = androidx.compose.ui.graphics.Shadow(Color(0x66402020), Offset(0f, 2f), 8f)), Color.White, Modifier.fillMaxWidth().weight(1f), max = 30.sp, min = 10.sp, lineHeight = 1.05f, align = TextAlign.Center, contentAlignment = Alignment.Center)
            if (!c.isWord && c.byline.isNotBlank()) Text(c.byline, style = hand.copy(fontSize = 13.sp, textAlign = TextAlign.Center), color = Color.White.copy(alpha = 0.85f), modifier = Modifier.fillMaxWidth(), maxLines = 1)
        }
        Brand(Color(0xFF6B5E4E), Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp), 11)
    }
}

// 9. Envelope (n12): lined paper and an illustrated envelope holding a letter with the words on it.
@Composable
private fun EnvelopeCard(c: CardContent, h: Hue) {
    val (env, paper, ink) = when (h) {
        Hue.Sky -> Triple(Color(0xFF8FB0D9), Color(0xFFF7E3C4), Color(0xFF2B3F6B))
        Hue.Lavender -> Triple(Color(0xFFB4A5DB), Color(0xFFF4D6DE), Color(0xFF4B2E6E))
        Hue.Peach -> Triple(Color(0xFFE9A07A), Color(0xFFFFF0D6), Color(0xFF7A2E1A))
        else -> Triple(Color(0xFF8FA65E), Color(0xFFE8B3B0), Color(0xFF6E1F2A))
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFFFBF3E4))) {
        val wPx = constraints.maxWidth.toFloat()
        val hPx = constraints.maxHeight.toFloat()
        val boxH = minOf(hPx * 0.78f, wPx * 0.95f)
        val box = Rect(wPx * 0.06f, (hPx - boxH) / 2f, wPx * 0.94f, (hPx + boxH) / 2f)
        val letter = envelopeLetterRect(box)
        Canvas(Modifier.fillMaxSize()) {
            var y = size.height * 0.06f
            while (y < size.height) { dashed(y, Color(0xFFD9CBB0)); y += size.height * 0.07f }
            envelopeBack(box, env)
            drawRoundRect(paper, letter.topLeft, letter.size, CornerRadius(4f * density))
        }
        val dens = LocalDensity.current
        // The writable part of the letter ends where the envelope's pocket begins.
        val textTop = letter.top + letter.height * 0.07f
        val textBottom = box.top + box.height * 0.5f
        Column(
            Modifier.offset(with(dens) { (letter.left + letter.width * 0.08f).toDp() }, with(dens) { textTop.toDp() })
                .size(with(dens) { (letter.width * 0.84f).toDp() }, with(dens) { (textBottom - textTop).toDp() }),
            verticalArrangement = Arrangement.Center,
        ) {
            val text = if (c.isWord) "${c.headline}.\n${c.body}" else c.headline
            FitText(text, hand, ink, Modifier.fillMaxWidth().weight(1f), max = 30.sp, min = 9.sp, lineHeight = 1.0f, align = TextAlign.Center)
            if (!c.isWord && c.byline.isNotBlank()) Text(c.byline, style = hand.copy(fontSize = 12.sp, textAlign = TextAlign.Center), color = ink.copy(alpha = 0.75f), modifier = Modifier.fillMaxWidth(), maxLines = 1)
        }
        Canvas(Modifier.fillMaxSize()) { envelopeFront(box, lerp(env, Color.White, 0.08f), lerp(env, Color.Black, 0.4f), Color(0xFFF2A5B4)) }
        Brand(ink.copy(alpha = 0.6f), Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp), 11)
    }
}

// 10. Split (n15): sky above, cream below, puzzle pieces along the edges; one line in each half.
@Composable
private fun SplitCard(c: CardContent, h: Hue) {
    val sky = when (h) { Hue.Peach -> Color(0xFFE88B5A); Hue.Lavender -> Color(0xFF8C7AE0); Hue.Mint -> Color(0xFF3FA78A); else -> Color(0xFF4F8FD9) }
    val ink = Color(0xFF151515)
    val (top, bottom) = if (c.isWord) c.headline to c.body else splitThought(c.headline)
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f)) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Brush.verticalGradient(listOf(sky, lerp(sky, Color.White, 0.35f))))
                cloudPuff(Offset(size.width * 0.25f, size.height * 0.6f), size.width * 0.55f, Color.White.copy(alpha = 0.6f))
                cloudPuff(Offset(size.width * 0.8f, size.height * 0.85f), size.width * 0.4f, Color.White.copy(alpha = 0.5f))
                val s = size.width * 0.12f
                listOf(0.18f, 0.5f, 0.82f).forEachIndexed { i, x -> rotate(listOf(-12f, 8f, 20f)[i], Offset(size.width * x, s * 0.9f)) { drawPath(puzzlePath(Offset(size.width * x, s * 0.9f), s), Color(0xFFFDF5DC)) } }
                drawGrain(0.1f)
            }
            Box(Modifier.fillMaxSize().padding(start = 26.dp, end = 26.dp, top = 60.dp, bottom = 12.dp), contentAlignment = Alignment.Center) {
                FitText(top, serifText.copy(letterSpacing = (-0.03).sp), ink, Modifier.fillMaxSize(), max = if (c.isWord) 54.sp else 26.sp, min = 11.sp, lineHeight = 1.1f, align = TextAlign.Center)
            }
        }
        Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFFFDF5DC))) {
            Canvas(Modifier.fillMaxSize()) {
                val s = size.width * 0.12f
                listOf(0.18f, 0.5f, 0.82f).forEachIndexed { i, x -> rotate(listOf(16f, -6f, -18f)[i], Offset(size.width * x, size.height - s * 0.9f)) { drawPath(puzzlePath(Offset(size.width * x, size.height - s * 0.9f), s), lerp(sky, Color.White, 0.2f)) } }
                drawGrain(0.08f)
            }
            Column(Modifier.fillMaxSize().padding(start = 26.dp, end = 26.dp, top = 12.dp, bottom = 60.dp), verticalArrangement = Arrangement.Center) {
                FitText(bottom, serifText.copy(letterSpacing = (-0.02).sp), ink, Modifier.fillMaxWidth().weight(1f), max = if (c.isWord) 20.sp else 26.sp, min = 10.sp, lineHeight = 1.15f, align = TextAlign.Center)
                if (!c.isWord && c.byline.isNotBlank()) Text("— ${c.byline}", style = sans.copy(fontSize = 11.sp, textAlign = TextAlign.Center), color = ink.copy(alpha = 0.65f), modifier = Modifier.fillMaxWidth(), maxLines = 1)
            }
        }
    }
}

/** Splits a thought into two halves at the most natural break (a full stop, a comma, or the middle). */
private fun splitThought(t: String): Pair<String, String> {
    if (t.split(" ").size < 6) return t to ""
    val mid = t.length / 2
    val candidates = Regex("[.;:,—] ").findAll(t).map { it.range.first + 1 }.toList()
    val cut = candidates.minByOrNull { abs(it - mid) }?.takeIf { abs(it - mid) < t.length / 3 }
        ?: t.indexOf(' ', mid).takeIf { it > 0 } ?: return t to ""
    return t.substring(0, cut).trim() to t.substring(cut).trim()
}

// 11. Garden (n16): a chunky white serif above a row of flat flowers. Square: flowers on the right in a column.
@Composable
private fun GardenCard(c: CardContent, h: Hue, shape: CardShape) {
    val bg = when (h) { Hue.Sky -> Color(0xFF9FBDF5); Hue.Mint -> Color(0xFF8FD3AE); Hue.Peach -> Color(0xFFF5AE8E); Hue.Ink -> Color(0xFF2B2A4A); else -> Color(0xFFB9ABF5) }
    Box(Modifier.fillMaxSize().background(bg)) {
        ArtAndText(shape, artFraction = if (shape == CardShape.Square) 0.4f else 0.4f, artFirst = false,
            art = { m -> Canvas(m.padding(top = 8.dp, end = if (shape == CardShape.Square) 14.dp else 0.dp, start = if (shape == CardShape.Square) 0.dp else 14.dp)) {
                if (shape == CardShape.Square) {
                    flowerRow(Rect(0f, size.height * 0.08f, size.width, size.height * 0.52f), 3)
                    flowerRow(Rect(0f, size.height * 0.56f, size.width, size.height), 5)
                } else flowerRow(Rect(0f, 0f, size.width, size.height), 3)
            } },
            text = {
                Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 20.dp, top = 26.dp, bottom = 12.dp)) {
                    if (c.isWord) {
                        FitText(c.headline, chunky, Color.White, Modifier.fillMaxWidth().weight(0.5f), max = 62.sp, min = 16.sp, lineHeight = 0.95f, contentAlignment = Alignment.BottomStart)
                        Text(c.meta.uppercase(), style = serif.copy(fontSize = 10.sp, letterSpacing = 2.sp), color = Color.White, maxLines = 1)
                        Spacer(Modifier.height(6.dp))
                        FitText(c.body, chunky.copy(fontWeight = FontWeight.Normal), Color.White, Modifier.fillMaxWidth().weight(0.5f), max = 24.sp, min = 10.sp, lineHeight = 1.0f)
                    } else {
                        FitText(c.headline, chunky, Color.White, Modifier.fillMaxWidth().weight(1f), max = 40.sp, min = 11.sp, lineHeight = 0.98f, contentAlignment = Alignment.CenterStart)
                        if (c.byline.isNotBlank()) Text(c.byline.uppercase(), style = serif.copy(fontSize = 10.sp, letterSpacing = 2.sp), color = Color.White, maxLines = 1, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            })
    }
}

// 12. Leaf (n18): grass green, one pale leaf silhouette, small cobalt capitals in its middle.
@Composable
private fun LeafCard(c: CardContent, h: Hue) {
    val (bg, leaf, ink) = when (h) {
        Hue.Lavender -> Triple(Color(0xFF8C6FE0), Color(0xFFE8E2F2), Color(0xFF2A14C2))
        Hue.Peach -> Triple(Color(0xFFF08A5C), Color(0xFFF7E6DA), Color(0xFF1B3FD9))
        Hue.Ink -> Triple(Color(0xFF14213D), Color(0xFFD8DCE6), Color(0xFF1B3FD9))
        else -> Triple(Color(0xFF3BC170), Color(0xFFDCD9D6), Color(0xFF1B4FE0))
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(bg), contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth, maxHeight) * 0.86f
        Canvas(Modifier.size(side)) { drawPath(leafPath(Rect(0f, 0f, size.width, size.height)), leaf) }
        Column(Modifier.size(side * 0.56f, side * 0.4f).offset(y = -side * 0.04f), verticalArrangement = Arrangement.Center) {
            val text = (if (c.isWord) "${c.headline}\n${c.body}" else c.headline).uppercase()
            FitText(text, sans.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.2.sp), ink, Modifier.fillMaxWidth().weight(1f), max = 22.sp, min = 8.sp, lineHeight = 1.0f, align = TextAlign.Center)
            if (!c.isWord && c.byline.isNotBlank()) Text(c.byline.uppercase(), style = sans.copy(fontSize = 9.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium), color = ink, modifier = Modifier.fillMaxWidth(), maxLines = 1)
        }
        Brand(Color.White, Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp), 12)
    }
}

// 13. Swiss (r23): electric blue, organic white shapes on the right, a yellow seed; text in its own column.
@Composable
private fun SwissCard(c: CardContent, h: Hue, square: Boolean) {
    val bg = if (h == Hue.Sky) Color(0xFF0A1CF0) else h.bold
    Box(Modifier.fillMaxSize().background(bg)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val hh = size.height
            val top = Path().apply {
                moveTo(w * 0.58f, hh * 0.46f)
                cubicTo(w * 0.5f, hh * 0.34f, w * 0.54f, hh * 0.2f, w * 0.72f, hh * 0.16f)
                cubicTo(w * 0.86f, hh * 0.13f, w * 0.9f, 0f, w * 1.02f, 0f); lineTo(w * 1.02f, hh * 0.44f)
                cubicTo(w * 0.94f, hh * 0.47f, w * 0.8f, hh * 0.47f, w * 0.58f, hh * 0.46f); close()
            }
            val bottom = Path().apply {
                moveTo(w * 0.62f, hh * 0.54f)
                cubicTo(w * 0.8f, hh * 0.52f, w * 0.92f, hh * 0.56f, w * 1.02f, hh * 0.62f); lineTo(w * 1.02f, hh * 0.88f)
                cubicTo(w * 0.9f, hh * 0.86f, w * 0.64f, hh * 0.8f, w * 0.6f, hh * 0.66f); cubicTo(w * 0.58f, hh * 0.6f, w * 0.59f, hh * 0.56f, w * 0.62f, hh * 0.54f); close()
            }
            drawPath(top, Color.White); drawPath(bottom, Color.White)
            drawCircle(bg, w * 0.07f, Offset(w * 0.82f, hh * 0.7f), style = Stroke(1.5f * density))
            drawArc(bg, 200f, 120f, false, Offset(w * 0.64f, hh * 0.22f), Size(w * 0.22f, hh * 0.14f), style = Stroke(1.5f * density))
            drawCircle(Color(0xFFFFF000), w * 0.04f, Offset(w * 0.62f, hh * 0.5f))
        }
        Column(Modifier.fillMaxHeight().fillMaxWidth(0.53f).padding(start = 24.dp, top = 24.dp, bottom = 24.dp)) {
            Box(Modifier.size(14.dp).background(Color(0xFFFFF000)))
            Spacer(Modifier.height(10.dp))
            FitText(if (c.isWord) c.body else c.headline, sans, Color.White, Modifier.fillMaxWidth().weight(1f), max = if (c.isWord) 15.sp else 22.sp, min = 9.sp, lineHeight = 1.2f)
            Spacer(Modifier.height(10.dp))
            val big = if (c.isWord) c.headline else c.byline
            if (big.isNotBlank()) FitText(big, sans.copy(fontWeight = FontWeight.Medium, letterSpacing = (-0.03).sp), Color.White, Modifier.fillMaxWidth().height(if (square) 40.dp else 56.dp), max = 40.sp, min = 14.sp, lineHeight = 1.0f)
            if (c.isWord) Text(c.pronunciation, style = sans.copy(fontSize = 11.sp), color = Color.White.copy(alpha = 0.85f), maxLines = 1)
            Spacer(Modifier.height(8.dp))
            Brand(Color.White, size = 11)
        }
    }
}

// 14. Glow (r24): periwinkle into peach, stipple grain, a smiling sun. Square: sun left, words right.
@Composable
private fun GlowCard(c: CardContent, h: Hue, shape: CardShape) {
    val (top, low) = when (h) {
        Hue.Sky, Hue.Lavender -> Color(0xFF6B73E0) to Color(0xFFF2A57E)
        Hue.Mint -> Color(0xFF4FA59A) to Color(0xFFF5D98A)
        Hue.Peach -> Color(0xFFE77FA0) to Color(0xFFFFC27A)
        Hue.Ink -> Color(0xFF151A40) to Color(0xFF6B3D7A)
    }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, lerp(top, low, 0.5f), low))).grain(0.24f).padding(20.dp)) {
        ArtAndText(shape, artFraction = if (shape == CardShape.Square) 0.4f else if (shape == CardShape.Story) 0.44f else 0.4f,
            art = { m -> Canvas(m) {
                val sun = center
                val r = minOf(size.width, size.height) * 0.28f
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFE97A).copy(alpha = 0.9f), Color(0xFFFFE97A).copy(alpha = 0f)), sun, r * 2.6f), r * 2.6f, sun)
                drawCircle(Color(0xFFFFE15A), r, sun)
                val e = r * 0.14f
                drawCircle(Color(0xFF2A1E4A), e * 0.55f, Offset(sun.x - e * 2.2f, sun.y - e * 0.6f))
                drawCircle(Color(0xFF2A1E4A), e * 0.55f, Offset(sun.x + e * 2.2f, sun.y - e * 0.6f))
                drawArc(Color(0xFF2A1E4A), 20f, 140f, false, Offset(sun.x - e * 1.6f, sun.y), Size(e * 3.2f, e * 2f), style = Stroke(e * 0.5f, cap = StrokeCap.Round))
            } },
            text = {
                val align = if (shape == CardShape.Square) TextAlign.Start else TextAlign.Center
                Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
                    Words(c, Color.White, Color.White.copy(alpha = 0.88f), serif, sans, align, maxBig = 60, maxQuote = 30)
                    Spacer(Modifier.height(8.dp))
                    Brand(Color.White, Modifier.align(if (shape == CardShape.Square) Alignment.Start else Alignment.CenterHorizontally), 12)
                }
            })
    }
}

// 15. Bloom (r16, r17): warm paper, quiet type, one inflated flower. Square: flower on the right.
@Composable
private fun BloomCard(c: CardContent, h: Hue, shape: CardShape) {
    val petal = when (h) { Hue.Sky -> Color(0xFF8FB8FF); Hue.Mint -> Color(0xFFFFD36B); Hue.Lavender -> Color(0xFFC9A6FF); Hue.Peach -> Color(0xFFFF9A84); Hue.Ink -> Color(0xFFFF8FB0) }
    val ink = Color(0xFF1A1310)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF5F1EC), Color(0xFFFBEFE6))))) {
        ArtAndText(shape, artFraction = if (shape == CardShape.Square) 0.45f else 0.55f, artFirst = false,
            art = { m -> Canvas(m) { inflatedFlower(petal) } },
            text = {
                val align = if (shape == CardShape.Square) TextAlign.Start else TextAlign.Center
                Column(Modifier.fillMaxSize().padding(start = 26.dp, end = if (shape == CardShape.Square) 6.dp else 26.dp, top = 24.dp, bottom = if (shape == CardShape.Square) 24.dp else 4.dp), horizontalAlignment = if (shape == CardShape.Square) Alignment.Start else Alignment.CenterHorizontally) {
                    Canvas(Modifier.size(18.dp)) { drawMark(ink, 0f) }
                    Spacer(Modifier.height(10.dp))
                    Words(c, ink, ink.copy(alpha = 0.65f), sans.copy(fontWeight = FontWeight.Normal, letterSpacing = (-0.02).sp), sans, align, maxBig = 42, maxQuote = 24)
                }
            })
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.inflatedFlower(petal: Color) {
    val w = size.width; val hh = size.height
    val base = Offset(w * 0.5f, hh * 1.02f)
    val head = Offset(w * 0.52f, hh * 0.38f)
    val stem = Path().apply { moveTo(base.x, base.y); cubicTo(w * 0.62f, hh * 0.85f, w * 0.4f, hh * 0.6f, head.x, head.y) }
    drawPath(stem, Brush.linearGradient(listOf(Color(0xFF2E9E47), Color(0xFF5ACB5E)), base, head), style = Stroke(minOf(w, hh) * 0.035f, cap = StrokeCap.Round))
    listOf(Triple(0.44f, 0.82f, -1f), Triple(0.58f, 0.72f, 1f)).forEach { (x, y, s) ->
        val o = Offset(w * x, hh * y)
        val lw = minOf(w, hh * 1.2f)
        val leaf = Path().apply {
            moveTo(o.x, o.y)
            cubicTo(o.x + s * lw * 0.1f, o.y - hh * 0.12f, o.x + s * lw * 0.28f, o.y - hh * 0.1f, o.x + s * lw * 0.32f, o.y - hh * 0.2f)
            cubicTo(o.x + s * lw * 0.24f, o.y + hh * 0.02f, o.x + s * lw * 0.1f, o.y + hh * 0.03f, o.x, o.y); close()
        }
        drawPath(leaf, Brush.linearGradient(listOf(Color(0xFF1F8A3E), Color(0xFF6BD36E)), o, Offset(o.x + s * lw * 0.3f, o.y - hh * 0.16f)))
    }
    val pr = minOf(w * 0.3f, hh * 0.32f)
    for (i in 0 until 8) {
        rotate(i * 45f + 22f, head) {
            val len = pr * (if (i % 2 == 0) 1f else 0.85f)
            val pw = pr * 0.45f
            val path = Path().apply {
                moveTo(head.x, head.y)
                cubicTo(head.x + len * 0.3f, head.y - pw, head.x + len * 0.9f, head.y - pw * 1.2f, head.x + len, head.y)
                cubicTo(head.x + len * 0.9f, head.y + pw * 1.2f, head.x + len * 0.3f, head.y + pw, head.x, head.y); close()
            }
            drawPath(path, Brush.linearGradient(listOf(Color(0xFFB8F0DD), lerp(petal, Color.White, 0.2f), petal), head, Offset(head.x + len, head.y)))
        }
    }
    drawCircle(Brush.radialGradient(listOf(Color(0xFFFF8A6B), Color(0xFFD9342B)), Offset(head.x - pr * 0.06f, head.y - pr * 0.06f), pr * 0.3f), pr * 0.2f, head)
}

// 16. Portal (n01): muted teal, grain, a dark tilted portal with light through it. Square: portal left.
@Composable
private fun PortalCard(c: CardContent, h: Hue, shape: CardShape) {
    val (bg, glow) = when (h) {
        Hue.Sky -> Color(0xFF2E4A6B) to Color(0xFFBFD9FF)
        Hue.Lavender -> Color(0xFF3E3566) to Color(0xFFE2D4FF)
        Hue.Peach -> Color(0xFF6B3E3A) to Color(0xFFFFD9B8)
        Hue.Ink -> Color(0xFF151B26) to Color(0xFFE0E8F5)
        Hue.Mint -> Color(0xFF2E5357) to Color(0xFFFFE6CC)
    }
    Box(Modifier.fillMaxSize().background(bg).grain(0.18f).padding(22.dp)) {
        Text("KAIROS", style = sans.copy(fontSize = 10.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Medium), color = Color.White.copy(alpha = 0.7f), modifier = Modifier.align(Alignment.TopEnd))
        ArtAndText(shape, artFraction = if (shape == CardShape.Square) 0.45f else 0.56f,
            art = { m -> Canvas(m) {
                val ow = minOf(size.width * 0.7f, size.height * 0.68f)
                portal(Rect(Offset(center.x - ow / 2, center.y - ow * 0.6f), Size(ow, ow * 1.2f)), bg, glow)
            } },
            text = {
                Column(Modifier.fillMaxSize().padding(top = if (shape == CardShape.Square) 30.dp else 6.dp, start = if (shape == CardShape.Square) 8.dp else 0.dp)) {
                    Words(c, Color.White, Color.White.copy(alpha = 0.7f), serif.copy(fontWeight = FontWeight.Normal), sans, TextAlign.Start, maxBig = 54, maxQuote = 24)
                }
            })
    }
}

// --- Round three: designs from m01..m12 ----------------------------------------------------------

// 17. Blur bloom (m02): a huge soft flower, a big serif headline with an italic last word, and a small
//     dictionary box. Story: flower top, words bottom. Pin: flower top-right, words bottom-left. Square:
//     flower left, words right.
@Composable
private fun BlurBloomCard(c: CardContent, h: Hue, shape: CardShape) {
    val (petal, heart) = when (h) {
        Hue.Sky -> Color(0xFFBFD6FF) to Color(0xFF2F5BFF)
        Hue.Mint -> Color(0xFFC9F0DC) to Color(0xFF16A37A)
        Hue.Lavender -> Color(0xFFE3D7FF) to Color(0xFF7A3CF0)
        Hue.Ink -> Color(0xFFE5E5E5) to Color(0xFF111111)
        Hue.Peach -> Color(0xFFF7C9D2) to Color(0xFFFF3B1F)
    }
    val ink = Color(0xFF241A16)
    Box(Modifier.fillMaxSize().background(Color(0xFFFBF8F3)).grain(0.06f)) {
        ArtAndText(shape, artFraction = if (shape == CardShape.Square) 0.48f else 0.56f,
            art = { m -> Canvas(m) {
                val r = minOf(size.width, size.height) * 0.48f
                val c0 = if (shape == CardShape.Pin) Offset(size.width * 0.58f, size.height * 0.52f) else center
                blurBloom(Rect(c0.x - r, c0.y - r, c0.x + r, c0.y + r), petal, heart)
            } },
            text = {
                Column(Modifier.fillMaxSize().padding(start = 24.dp, end = 22.dp, top = if (shape == CardShape.Square) 26.dp else 4.dp, bottom = 22.dp)) {
                    // The last word in italic, like "It Takes Courage to *Bloom*."
                    val words = c.headline.trimEnd('.').split(" ")
                    val head = if (words.size > 1) words.dropLast(1).joinToString(" ") + " " else ""
                    val tail = words.last() + if (c.headline.endsWith(".")) "." else ""
                    val annotated = androidx.compose.ui.text.buildAnnotatedString {
                        append(head)
                        pushStyle(androidx.compose.ui.text.SpanStyle(fontStyle = FontStyle.Italic)); append(tail); pop()
                    }
                    FitAnnotated(annotated, serif.copy(fontWeight = FontWeight.Normal, letterSpacing = (-0.02).sp), ink, Modifier.fillMaxWidth().weight(1f), max = if (c.isWord) 72.sp else 34.sp, min = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        val box = if (c.isWord) "${c.headline.lowercase()} (${c.partOfSpeech.take(1)}): ${c.body.trimEnd('.').lowercase()}." else c.byline.ifBlank { "Kairos" }
                        Text(box, style = sans.copy(fontSize = 8.5.sp, lineHeight = 11.sp), color = ink, maxLines = 4,
                            modifier = Modifier.weight(1f, fill = false).widthIn(max = 160.dp).border(0.8.dp, ink, RoundedCornerShape(5.dp)).padding(horizontal = 6.dp, vertical = 4.dp))
                        Spacer(Modifier.weight(1f))
                        Brand(ink, size = 11)
                    }
                }
            })
    }
}

/** Like FitText, for styled (annotated) text. */
@Composable
private fun FitAnnotated(text: androidx.compose.ui.text.AnnotatedString, style: TextStyle, color: Color, modifier: Modifier, max: androidx.compose.ui.unit.TextUnit, min: androidx.compose.ui.unit.TextUnit) {
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier, contentAlignment = Alignment.BottomStart) {
        val w = constraints.maxWidth; val hh = constraints.maxHeight
        val size = remember(text, w, hh) {
            var s = max.value
            while (s > min.value) {
                val st = style.copy(fontSize = s.sp, lineHeight = (s * 1.02f).sp)
                val m = measurer.measure(text, st, constraints = androidx.compose.ui.unit.Constraints(maxWidth = w))
                val longest = text.text.split(" ").maxByOrNull { it.length }.orEmpty()
                if (m.size.height <= hh && measurer.measure(longest, st, maxLines = 1, softWrap = false).size.width <= w) break
                s -= 1f
            }
            s
        }
        Text(text, style = style.copy(fontSize = size.sp, lineHeight = (size * 1.02f).sp), color = color)
    }
}

// 18. Halftone (m11, m12): a flower made of dots on cobalt. Story/pin: flower above, words below.
//     Square: words left, flower right.
@Composable
private fun HalftoneCard(c: CardContent, h: Hue, shape: CardShape) {
    val bg = when (h) { Hue.Sky, Hue.Ink -> Color(0xFF1F4FE0); Hue.Mint -> Color(0xFF0E7A55); Hue.Lavender -> Color(0xFF5B2FD9); Hue.Peach -> Color(0xFFE5482D) }
    Box(Modifier.fillMaxSize().background(bg)) {
        Canvas(Modifier.fillMaxSize()) {
            val step = size.width / 34f
            var x = 0f
            while (x < size.width) { drawLine(Color.White.copy(alpha = 0.06f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f); x += step }
            var y = 0f
            while (y < size.height) { drawLine(Color.White.copy(alpha = 0.06f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f); y += step }
        }
        ArtAndText(shape, artFraction = if (shape == CardShape.Square) 0.5f else 0.6f, artFirst = shape != CardShape.Square,
            art = { m -> Canvas(m.padding(12.dp)) { halftoneFlower(Rect(0f, 0f, size.width, size.height), Color.White, abs(c.headline.hashCode())) } },
            text = {
                Column(Modifier.fillMaxSize().padding(22.dp)) {
                    Words(c, Color.White, Color.White.copy(alpha = 0.75f), sans.copy(fontWeight = FontWeight.Medium, letterSpacing = (-0.03).sp), sans, TextAlign.Start, maxBig = 54, maxQuote = 28)
                    Spacer(Modifier.height(8.dp))
                    Brand(Color.White, size = 11)
                }
            })
    }
}

// 19. Eclipse (m09): a blurred eclipse on green; the words cascade down the card in pieces, joined by
//     thin hand-drawn lines; a small bordered caption at the bottom.
@Composable
private fun EclipseCard(c: CardContent, h: Hue, shape: CardShape) {
    val (bg, ring, core) = when (h) {
        Hue.Sky -> Triple(Color(0xFF1FA868), Color(0xFFFF5A3C), Color(0xFF43A6F0))
        Hue.Mint -> Triple(Color(0xFF2E7DD9), Color(0xFFFFB13C), Color(0xFF9CF2C8))
        Hue.Lavender -> Triple(Color(0xFF6A3FD1), Color(0xFFFF6F91), Color(0xFFFFE07A))
        Hue.Peach -> Triple(Color(0xFFF08A5C), Color(0xFF3C5AFF), Color(0xFFFFF0C8))
        Hue.Ink -> Triple(Color(0xFF121826), Color(0xFFFF5A3C), Color(0xFF43A6F0))
    }
    val text = if (c.isWord) "${c.headline} is ${c.body.trimEnd('.').replaceFirstChar { it.lowercase() }}." else c.headline
    val words = text.split(" ")
    val pieces = when { words.size <= 4 -> words.map { listOf(it) }; words.size <= 10 -> words.chunked(2); words.size <= 18 -> words.chunked(3); else -> words.chunked(4) }.map { it.joinToString(" ") }.take(7)
    BoxWithConstraints(Modifier.fillMaxSize().background(bg).grain(0.12f)) {
        val wPx = constraints.maxWidth.toFloat(); val hPx = constraints.maxHeight.toFloat()
        val n = pieces.size
        val pad = wPx * 0.08f
        val top = hPx * 0.08f
        val bottom = hPx * 0.84f
        val slot = (bottom - top) / n
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val pieceWidth = wPx * 0.5f
        val style = serifText.copy(letterSpacing = (-0.02).sp)
        val fs = remember(text, wPx, hPx) {
            var s = 40f
            while (s > 10f) {
                val st = style.copy(fontSize = s.sp)
                val ok = pieces.all { p -> measurer.measure(p, st, maxLines = 1, softWrap = false).size.width <= pieceWidth } && with(density) { (s * 1.25f).sp.toPx() } <= slot * 0.9f
                if (ok) break
                s -= 1f
            }
            s
        }
        // Pieces zig-zag down the left side, where the eclipse isn't.
        val xs = pieces.indices.map { i -> pad + (wPx - pad * 2 - pieceWidth) * listOf(0f, 0.32f, 0.06f, 0.42f, 0.12f, 0.48f, 0.04f)[i] }
        Canvas(Modifier.fillMaxSize()) {
            val r = minOf(size.width, size.height) * (if (shape == CardShape.Story) 0.34f else 0.3f)
            eclipse(Offset(size.width * 0.74f, size.height * 0.3f), r * 0.85f, ring, ring, core)
            // Hand lines from the end of one piece to the start of the next.
            for (i in 0 until n - 1) {
                val y1 = top + slot * (i + 0.65f); val y2 = top + slot * (i + 1.2f)
                val x1 = xs[i] + pieceWidth * 0.3f; val x2 = xs[i + 1] + pieceWidth * 0.1f
                val p = Path().apply { moveTo(x1, y1); cubicTo(x1, (y1 + y2) / 2, x2, (y1 + y2) / 2, x2, y2 - slot * 0.35f) }
                drawPath(p, Color.White.copy(alpha = 0.7f), style = Stroke(density.density * 0.8f))
            }
        }
        pieces.forEachIndexed { i, piece ->
            Text(piece, style = style.copy(fontSize = fs.sp, fontStyle = if (i % 2 == 1) FontStyle.Italic else FontStyle.Normal), color = Color.White, maxLines = 1, softWrap = false,
                modifier = Modifier.offset(with(density) { xs[i].toDp() }, with(density) { (top + slot * i).toDp() }))
        }
        Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.Bottom) {
            val cap = if (c.isWord) c.meta else c.byline.ifBlank { "Kairos" }
            if (cap.isNotBlank()) Text(cap, style = sans.copy(fontSize = 10.sp), color = Color.White, modifier = Modifier.border(0.8.dp, Color.White, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
            Spacer(Modifier.weight(1f))
            Brand(Color.White, size = 11)
        }
    }
}

// 20. Breathe (m08): a deep blue orb on white, the words small inside it, like [breathe].
@Composable
private fun BreatheCard(c: CardContent, h: Hue) {
    val (deep, light) = when (h) {
        Hue.Sky, Hue.Ink -> Color(0xFF0B1A6E) to Color(0xFF3D8BFF)
        Hue.Mint -> Color(0xFF0B4A3A) to Color(0xFF3DD9A0)
        Hue.Lavender -> Color(0xFF2A0B6E) to Color(0xFFA67FFF)
        Hue.Peach -> Color(0xFF7A1A0B) to Color(0xFFFF8A5C)
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFFF3F7FD)), contentAlignment = Alignment.Center) {
        val d = minOf(maxWidth, maxHeight)
        Canvas(Modifier.fillMaxSize()) { breathOrb(center, minOf(size.width, size.height) * 0.48f, deep, light) }
        Column(Modifier.size(d * 0.42f), verticalArrangement = Arrangement.Center) {
            val text = if (c.isWord) "[${c.headline.lowercase()}]\n${c.body.lowercase()}" else c.headline.lowercase()
            FitText(text, serifText, Color.White, Modifier.fillMaxWidth().weight(1f), max = 22.sp, min = 8.sp, lineHeight = 1.25f, align = TextAlign.Center, contentAlignment = Alignment.Center)
        }
        Brand(deep.copy(alpha = 0.6f), Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp), 11)
    }
}

// 21. Cyanotype (m07): scratched blue, cream heavy capitals spread across each row.
@Composable
private fun CyanotypeCard(c: CardContent, h: Hue) {
    val bg = when (h) { Hue.Sky, Hue.Ink -> Color(0xFF1E5BC8); Hue.Mint -> Color(0xFF14695A); Hue.Lavender -> Color(0xFF3B2A9E); Hue.Peach -> Color(0xFFB8432A) }
    val ink = Color(0xFFF5EFA8)
    val text = (if (c.isWord) "${c.headline} ${c.body.trimEnd('.')}" else c.headline.trimEnd('.')).uppercase()
    val words = text.split(" ").filter { it.isNotBlank() }
    val per = when { words.size <= 6 -> 2; words.size <= 12 -> 3; else -> 4 }
    val rows = words.chunked(per)
    Box(Modifier.fillMaxSize().background(bg)) {
        Canvas(Modifier.fillMaxSize()) { scratches(abs(text.hashCode()), Color.White) }
        Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.SpaceBetween) {
            val measurer = rememberTextMeasurer()
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val w = constraints.maxWidth; val hh = constraints.maxHeight
                val density = LocalDensity.current
                val st = chunky.copy(fontFamily = Sans, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                val fs = remember(text, w, hh) {
                    var s = 44f
                    while (s > 8f) {
                        val ok = rows.all { r -> measurer.measure(r.joinToString(" "), st.copy(fontSize = s.sp), maxLines = 1, softWrap = false).size.width <= w * 0.96f } &&
                            rows.size * with(density) { (s * 1.15f).sp.toPx() } <= hh
                        if (ok) break
                        s -= 1f
                    }
                    s
                }
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
                    rows.forEach { r ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = if (r.size > 1) Arrangement.SpaceBetween else Arrangement.Start) {
                            r.forEach { word -> Text(word, style = st.copy(fontSize = fs.sp), color = ink, maxLines = 1, softWrap = false) }
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Brand(ink, Modifier.weight(1f), 11)
                if (!c.isWord && c.byline.isNotBlank()) Text(c.byline.uppercase(), style = sans.copy(fontSize = 10.sp, letterSpacing = 1.sp), color = ink, maxLines = 1)
            }
        }
    }
}

// 22. Pixel (m06): kraft paper, a giant pixel object, small typewriter-ish captions. Square: words left.
@Composable
private fun PixelCard(c: CardContent, h: Hue, shape: CardShape) {
    val obj = when (h) { Hue.Sky -> Color(0xFF2F5BFF); Hue.Mint -> Color(0xFF2E9E5A); Hue.Lavender -> Color(0xFF8C5BF0); Hue.Ink -> Color(0xFF151515); Hue.Peach -> Color(0xFFFF9124) }
    val ink = Color(0xFF2A2018)
    Box(Modifier.fillMaxSize().background(Color(0xFFB39A7C)).grain(0.14f)) {
        ArtAndText(shape, artFraction = if (shape == CardShape.Square) 0.5f else 0.58f, artFirst = false,
            art = { m -> Canvas(m.padding(14.dp)) { pixelObject(Rect(0f, 0f, size.width, size.height), obj, abs(c.headline.hashCode())) } },
            text = {
                Column(Modifier.fillMaxSize().padding(22.dp)) {
                    Text(if (c.isWord) "A WORD, KEPT" else "A THOUGHT, KEPT", style = sans.copy(fontSize = 9.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Medium), color = ink)
                    Spacer(Modifier.height(10.dp))
                    Words(c, ink, ink.copy(alpha = 0.7f), sans.copy(fontWeight = FontWeight.Medium, letterSpacing = (-0.04).sp), sans, TextAlign.Start, maxBig = 52, maxQuote = 26)
                }
            })
        Brand(ink, Modifier.align(Alignment.BottomEnd).padding(16.dp), 10)
    }
}

// 23. Window (m01): a pink card with an oval window onto a dream landscape; centred serif below.
//     Square: window on the left, words on the right.
@Composable
private fun WindowCard(c: CardContent, h: Hue, shape: CardShape) {
    val (bg, sky) = when (h) {
        Hue.Sky -> Color(0xFFDCE8FB) to listOf(Color(0xFF8FB8F5), Color(0xFFDCE8FB))
        Hue.Mint -> Color(0xFFDDF2E6) to listOf(Color(0xFF9FD9C0), Color(0xFFF2E3C8))
        Hue.Lavender -> Color(0xFFEDE4FB) to listOf(Color(0xFFC6B2F5), Color(0xFFF7D7E4))
        Hue.Peach -> Color(0xFFF7E0E4) to listOf(Color(0xFFF2A0B8), Color(0xFFF9D9C9))
        Hue.Ink -> Color(0xFF1C2033) to listOf(Color(0xFF3B3F7A), Color(0xFFE59AB4))
    }
    val ink = if (h == Hue.Ink) Color.White else Color(0xFF2A1F2D)
    Box(Modifier.fillMaxSize().background(bg).grain(0.05f)) {
        ArtAndText(shape, artFraction = if (shape == CardShape.Square) 0.42f else 0.5f,
            art = { m -> Canvas(m.padding(start = 20.dp, end = if (shape == CardShape.Square) 4.dp else 20.dp, top = 24.dp, bottom = 8.dp)) {
                val ow = minOf(size.width, size.height * 0.66f)
                val oh = minOf(size.height, ow * 1.45f)
                dreamWindow(Rect(center.x - ow / 2, center.y - oh / 2, center.x + ow / 2, center.y + oh / 2), sky)
            } },
            text = {
                val align = if (shape == CardShape.Square) TextAlign.Start else TextAlign.Center
                Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp), horizontalAlignment = if (shape == CardShape.Square) Alignment.Start else Alignment.CenterHorizontally) {
                    Words(c, ink, ink.copy(alpha = 0.65f), serifText, serifText, align, maxBig = 48, maxQuote = 24)
                    Spacer(Modifier.height(8.dp))
                    Canvas(Modifier.size(16.dp)) { drawMark(ink, 0f) }
                }
            })
    }
}

// 24. Specimen (m05): black, a winding glowing stem with line flowers and tiny fig labels; the words
//     sit in pieces beside the stem. Square: words gathered on the right of the stem.
@Composable
private fun SpecimenCard(c: CardContent, h: Hue, shape: CardShape) {
    val text = if (c.isWord) c.headline else c.headline
    val pieces = text.split(" ").let { w -> when { w.size <= 4 -> w; w.size <= 8 -> w.chunked(2).map { it.joinToString(" ") }; else -> w.chunked((w.size + 3) / 4).map { it.joinToString(" ") } } }.take(4)
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF0B0B0D))) {
        val wPx = constraints.maxWidth.toFloat(); val hPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current
        // The stem lives on the right; the words keep their own column on the left.
        val stem = if (shape == CardShape.Square) Rect(wPx * 0.5f, hPx * 0.08f, wPx * 0.98f, hPx * 0.92f) else Rect(wPx * 0.5f, hPx * 0.1f, wPx * 0.98f, hPx * 0.8f)
        Canvas(Modifier.fillMaxSize()) {
            specimenStem(stem, abs(text.hashCode()))
        }
        // Words in the corners the stem leaves open, alternating sides, sized to share one size.
        val measurer = rememberTextMeasurer()
        val st = sans.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
        val colW = wPx * 0.42f
        val fs = remember(text, wPx, hPx) {
            var s = 26f
            while (s > 8f && pieces.any { measurer.measure(it.uppercase(), st.copy(fontSize = s.sp), maxLines = 1, softWrap = false).size.width > colW }) s -= 1f
            s
        }
        val spots = pieces.indices.map { Offset(wPx * 0.06f, hPx * (0.1f + it * (if (shape == CardShape.Square) 0.15f else 0.17f))) }
        pieces.forEachIndexed { i, p ->
            Text(p.uppercase(), style = st.copy(fontSize = fs.sp, letterSpacing = 1.sp), color = Color.White, maxLines = 1, softWrap = false,
                modifier = Modifier.offset(with(density) { spots[i].x.toDp() }, with(density) { spots[i].y.toDp() }))
        }
        Column(Modifier.align(Alignment.BottomStart).padding(18.dp)) {
            val small = if (c.isWord) c.body else c.byline
            if (small.isNotBlank()) Text(small, style = sans.copy(fontSize = 10.sp, lineHeight = 13.sp), color = Color.White.copy(alpha = 0.7f), maxLines = 3, modifier = Modifier.widthIn(max = 220.dp))
            Spacer(Modifier.height(6.dp))
            Brand(Color.White, size = 10)
        }
    }
}

/** A card scaled down by density so its 360dp layout stays intact (previews and thumbnails). */
@Composable
fun CardThumbnail(c: CardContent, t: Template, h: Hue, width: Dp, ratio: Float = 2f / 3f, modifier: Modifier = Modifier) {
    val outer = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(outer.density * width.value / 360f, 1f)) {
        ShareCard(c, t, h, modifier.size(360.dp, (360f / ratio).dp))
    }
}
