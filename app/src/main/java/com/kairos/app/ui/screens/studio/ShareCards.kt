package com.kairos.app.ui.screens.studio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.ui.brand.KairosStamp
import com.kairos.app.ui.brand.drawKairosMark
import com.kairos.app.ui.brand.drawSparkle
import com.kairos.app.ui.mascot.DropMascot
import com.kairos.app.ui.mascot.Expression
import com.kairos.app.ui.paint.paperGrain
import com.kairos.app.ui.paint.watercolorPool
import com.kairos.app.ui.theme.CaveatFamily
import com.kairos.app.ui.theme.FrauncesFamily
import com.kairos.app.ui.theme.NunitoFamily
import com.kairos.app.ui.theme.Wash

/** What goes on a card. Quotes use [headline] for the text and [byline] for the author. */
data class CardContent(
    val isWord: Boolean,
    val headline: String,
    val pronunciation: String = "",
    val partOfSpeech: String = "",
    val body: String = "",
    val example: String = "",
    val byline: String = "",
    val seed: Int,
)

enum class CardTemplate(val label: String) {
    Bloom("Bloom"), Dictionary("Dictionary"), Postcard("Postcard"), Night("Night pool"), Polaroid("Polaroid"), Sticker("Sticker")
}

enum class CardFormat(val label: String, val ratio: Float) { Pin("Pin 2:3", 2f / 3f), Story("Story 9:16", 9f / 16f), Square("Square", 1f) }

/** Palettes are pigment sets; each template uses them in its own way. */
enum class CardPalette(val label: String, val washes: List<Color>, val paper: Color, val ink: Color) {
    Sky("Sky", listOf(Color(0xFF8EC5FF), Color(0xFFCDB8FF), Color(0xFFA4E7CD)), Color(0xFFF6F9FF), Color(0xFF1E2B4A)),
    Peach("Peach", listOf(Color(0xFFFFC4A3), Color(0xFFFFB4C8), Color(0xFFFFE59C)), Color(0xFFFFF8F1), Color(0xFF4A2433)),
    Meadow("Meadow", listOf(Color(0xFFA4E7CD), Color(0xFFFFE59C), Color(0xFF8EC5FF)), Color(0xFFF5FBF4), Color(0xFF173B33)),
    Lavender("Lavender", listOf(Color(0xFFCDB8FF), Color(0xFF8EC5FF), Color(0xFFFFB4C8)), Color(0xFFF8F5FF), Color(0xFF2E2056)),
    Cobalt("Cobalt", listOf(Color(0xFF3461E8), Color(0xFF8EC5FF), Color(0xFF7A5CE0)), Color(0xFFEEF3FF), Color(0xFF13205A)),
}

private fun scaled(base: TextUnit, len: Int, longAt: Int, min: Float = 0.55f): TextUnit {
    if (len <= longAt) return base
    val f = (longAt.toFloat() / len).coerceAtLeast(min)
    return (base.value * f).sp
}

/**
 * A share card at a fixed design size (360dp wide). The studio renders it scaled to fit the
 * preview, and the exporter draws the same composable into a bitmap, so what you see is what
 * gets shared.
 */
@Composable
fun ShareCard(content: CardContent, template: CardTemplate, palette: CardPalette, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(0.dp))) {
        when (template) {
            CardTemplate.Bloom -> Bloom(content, palette)
            CardTemplate.Dictionary -> Dictionary(content, palette)
            CardTemplate.Postcard -> Postcard(content, palette)
            CardTemplate.Night -> NightPool(content, palette)
            CardTemplate.Polaroid -> Polaroid(content, palette)
            CardTemplate.Sticker -> Sticker(content, palette)
        }
    }
}

@Composable
private fun PaintedPaper(palette: CardPalette, seed: Int, modifier: Modifier = Modifier, layout: Int = 0, dark: Boolean = false) {
    Canvas(modifier.fillMaxSize()) {
        drawRect(if (dark) Color(0xFF0B1238) else palette.paper)
        val w = size.width
        val h = size.height
        val spots = when (layout) {
            0 -> listOf(Offset(w * 0.15f, h * 0.12f) to 0.62f, Offset(w * 0.95f, h * 0.55f) to 0.55f, Offset(w * 0.2f, h * 0.98f) to 0.5f)
            1 -> listOf(Offset(w * 0.9f, h * 0.05f) to 0.5f, Offset(w * 0.05f, h * 0.7f) to 0.42f)
            else -> listOf(Offset(w * 0.5f, h * 0.42f) to 0.75f, Offset(w * 0.1f, h * 0.05f) to 0.35f, Offset(w * 0.95f, h * 0.95f) to 0.4f)
        }
        spots.forEachIndexed { i, (c, r) ->
            watercolorPool(c, w * r, palette.washes[i % palette.washes.size], seed + i * 17, strength = if (dark) 1.25f else 1.15f)
        }
        paperGrain(if (dark) 0.6f else 1f)
    }
}

@Composable
private fun BoxScope.Stamp(ink: Color, align: Alignment = Alignment.BottomEnd, size: Int = 64) {
    KairosStamp(size.dp, ink, Modifier.align(align).padding(18.dp).rotate(-12f))
}

private val serif = TextStyle(fontFamily = FrauncesFamily)
private val sans = TextStyle(fontFamily = NunitoFamily)

/** Bloom: big word floating in watercolour pools — the default, most "Pinterest" look. */
@Composable
private fun Bloom(c: CardContent, p: CardPalette) {
    Box(Modifier.fillMaxSize()) {
        PaintedPaper(p, c.seed, layout = 0)
        Column(Modifier.fillMaxSize().padding(horizontal = 30.dp, vertical = 36.dp), verticalArrangement = Arrangement.Center) {
            if (c.isWord) {
                Text(c.partOfSpeech.lowercase(), style = serif.copy(fontStyle = FontStyle.Italic, fontSize = 16.sp), color = p.ink.copy(alpha = 0.7f))
                Text(c.headline.lowercase(), style = serif.copy(fontWeight = FontWeight(600), fontSize = scaled(58.sp, c.headline.length, 9), lineHeight = scaled(62.sp, c.headline.length, 9), letterSpacing = (-1).sp), color = p.ink)
                Spacer(Modifier.height(6.dp))
                Text(c.pronunciation, style = sans.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold), color = p.ink.copy(alpha = 0.65f))
                Spacer(Modifier.height(22.dp))
                Text(c.body, style = sans.copy(fontSize = 19.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold), color = p.ink)
                if (c.example.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Text("“${c.example}”", style = serif.copy(fontStyle = FontStyle.Italic, fontSize = 15.sp, lineHeight = 22.sp), color = p.ink.copy(alpha = 0.75f))
                }
            } else {
                Text("“", style = serif.copy(fontSize = 90.sp, lineHeight = 60.sp, fontWeight = FontWeight(600)), color = p.washes[0])
                Text(c.headline, style = serif.copy(fontStyle = FontStyle.Italic, fontSize = scaled(30.sp, c.headline.length, 90, 0.62f), lineHeight = scaled(40.sp, c.headline.length, 90, 0.62f)), color = p.ink)
                Spacer(Modifier.height(20.dp))
                Text("— ${c.byline}", style = sans.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold), color = p.ink.copy(alpha = 0.75f))
            }
        }
        Stamp(p.ink)
    }
}

/** Dictionary: an old-book entry on cream, with a single watercolour blot. */
@Composable
private fun Dictionary(c: CardContent, p: CardPalette) {
    Box(Modifier.fillMaxSize().background(Color(0xFFFBF7EE))) {
        Canvas(Modifier.fillMaxSize()) {
            watercolorPool(Offset(size.width * 0.82f, size.height * 0.2f), size.width * 0.3f, p.washes[0], c.seed, strength = 1.2f)
            paperGrain()
        }
        Column(Modifier.fillMaxSize().padding(34.dp), verticalArrangement = Arrangement.Center) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(p.ink.copy(alpha = 0.4f)))
            Spacer(Modifier.height(3.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(p.ink.copy(alpha = 0.4f)))
            Spacer(Modifier.height(28.dp))
            if (c.isWord) {
                Text(c.headline.lowercase(), style = serif.copy(fontSize = scaled(46.sp, c.headline.length, 10), fontWeight = FontWeight(700)), color = p.ink)
                Text("${c.pronunciation}   ${c.partOfSpeech}", style = serif.copy(fontStyle = FontStyle.Italic, fontSize = 15.sp), color = p.ink.copy(alpha = 0.7f))
                Spacer(Modifier.height(20.dp))
                Row {
                    Text("1.", style = serif.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold), color = p.ink)
                    Spacer(Modifier.width(10.dp))
                    Text(c.body, style = serif.copy(fontSize = 18.sp, lineHeight = 27.sp), color = p.ink)
                }
                if (c.example.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Text(c.example, style = serif.copy(fontStyle = FontStyle.Italic, fontSize = 15.sp, lineHeight = 22.sp), color = p.ink.copy(alpha = 0.7f), modifier = Modifier.padding(start = 24.dp))
                }
            } else {
                Text(c.headline, style = serif.copy(fontSize = scaled(26.sp, c.headline.length, 100, 0.65f), lineHeight = scaled(36.sp, c.headline.length, 100, 0.65f)), color = p.ink)
                Spacer(Modifier.height(18.dp))
                Text(c.byline, style = serif.copy(fontStyle = FontStyle.Italic, fontSize = 16.sp), color = p.ink.copy(alpha = 0.75f))
            }
            Spacer(Modifier.height(28.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(p.ink.copy(alpha = 0.4f)))
        }
        Text("from the kairos book of good words", style = serif.copy(fontStyle = FontStyle.Italic, fontSize = 11.sp), color = p.ink.copy(alpha = 0.55f), modifier = Modifier.align(Alignment.BottomStart).padding(28.dp))
        Stamp(p.ink, size = 58)
    }
}

/** Postcard: a stamp-edged card on a painted desk, with a handwritten note. */
@Composable
private fun Postcard(c: CardContent, p: CardPalette) {
    Box(Modifier.fillMaxSize()) {
        PaintedPaper(p, c.seed, layout = 2)
        Column(
            Modifier
                .align(Alignment.Center)
                .padding(26.dp)
                .rotate(-2.5f)
                .background(Color.White, RoundedCornerShape(6.dp))
                .border(6.dp, Color.White, RoundedCornerShape(6.dp))
                .border(7.dp, p.washes[1].copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("greetings from kairos", style = TextStyle(fontFamily = CaveatFamily, fontSize = 20.sp), color = p.ink.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
                Canvas(Modifier.size(34.dp)) { drawKairosMark(Offset.Zero, size.minDimension, ripple = false) }
            }
            Spacer(Modifier.height(10.dp))
            if (c.isWord) {
                Text(c.headline, style = serif.copy(fontSize = scaled(40.sp, c.headline.length, 10), fontWeight = FontWeight(600)), color = p.ink)
                Text(c.pronunciation, style = sans.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold), color = p.ink.copy(alpha = 0.6f))
                Spacer(Modifier.height(12.dp))
                Text(c.body, style = sans.copy(fontSize = 16.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold), color = p.ink)
                Spacer(Modifier.height(14.dp))
                Text("thought of you when I learned this one ♡", style = TextStyle(fontFamily = CaveatFamily, fontSize = 22.sp, lineHeight = 26.sp), color = p.washes[0].let { lerp(it, p.ink, 0.45f) })
            } else {
                Text(c.headline, style = TextStyle(fontFamily = CaveatFamily, fontSize = scaled(30.sp, c.headline.length, 80, 0.6f), lineHeight = scaled(34.sp, c.headline.length, 80, 0.6f)), color = p.ink)
                Spacer(Modifier.height(12.dp))
                Text("— ${c.byline}", style = sans.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = p.ink.copy(alpha = 0.7f))
            }
        }
        Stamp(p.ink, align = Alignment.TopEnd, size = 70)
    }
}

/** Night pool: deep cobalt with glowing pigment and white type, for dark feeds. */
@Composable
private fun NightPool(c: CardContent, p: CardPalette) {
    Box(Modifier.fillMaxSize()) {
        PaintedPaper(p, c.seed, layout = 1, dark = true)
        Canvas(Modifier.fillMaxSize()) {
            listOf(0.1f to 0.12f, 0.82f to 0.08f, 0.93f to 0.22f, 0.12f to 0.86f, 0.86f to 0.9f).forEachIndexed { i, (x, y) ->
                drawSparkle(Offset(size.width * x, size.height * y), (4 + i % 3 * 3).dp.toPx(), Color.White.copy(alpha = 0.8f))
            }
        }
        Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            if (c.isWord) {
                Text(c.headline, style = serif.copy(fontSize = scaled(52.sp, c.headline.length, 9), fontWeight = FontWeight(500)), color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(c.pronunciation + "  ·  " + c.partOfSpeech, style = sans.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold), color = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.height(26.dp))
                Text(c.body, style = sans.copy(fontSize = 18.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold), color = Color.White, textAlign = TextAlign.Center)
            } else {
                Text(c.headline, style = serif.copy(fontStyle = FontStyle.Italic, fontSize = scaled(28.sp, c.headline.length, 90, 0.62f), lineHeight = scaled(38.sp, c.headline.length, 90, 0.62f)), color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Text(c.byline, style = sans.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = Color.White.copy(alpha = 0.75f))
            }
        }
        Stamp(Color.White, align = Alignment.BottomCenter, size = 60)
    }
}

/** Polaroid: a painted "photo" of the word, with the meaning handwritten underneath. */
@Composable
private fun Polaroid(c: CardContent, p: CardPalette) {
    Box(Modifier.fillMaxSize().background(lerp(p.paper, p.washes[0], 0.25f))) {
        Canvas(Modifier.fillMaxSize()) { paperGrain() }
        Column(
            Modifier
                .align(Alignment.Center)
                .padding(30.dp)
                .rotate(3f)
                .background(Color.White, RoundedCornerShape(4.dp))
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 22.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(2.dp)), contentAlignment = Alignment.Center) {
                PaintedPaper(p, c.seed, layout = 2)
                Text(
                    if (c.isWord) c.headline.lowercase() else "“${c.headline.split(' ').take(4).joinToString(" ")}…”",
                    style = serif.copy(fontSize = if (c.isWord) scaled(44.sp, c.headline.length, 9) else 26.sp, fontWeight = FontWeight(600), fontStyle = if (c.isWord) FontStyle.Normal else FontStyle.Italic),
                    color = p.ink, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                if (c.isWord) c.body else c.headline,
                style = TextStyle(fontFamily = CaveatFamily, fontSize = scaled(23.sp, (if (c.isWord) c.body else c.headline).length, 90, 0.7f), lineHeight = scaled(26.sp, (if (c.isWord) c.body else c.headline).length, 90, 0.7f)),
                color = p.ink,
            )
            if (!c.isWord) Text("— ${c.byline}", style = TextStyle(fontFamily = CaveatFamily, fontSize = 18.sp), color = p.ink.copy(alpha = 0.7f))
        }
        Box(Modifier.align(Alignment.TopCenter).offset(y = 46.dp).rotate(-4f).size(width = 90.dp, height = 26.dp).background(p.washes[1].copy(alpha = 0.7f)))
        Stamp(p.ink, size = 60)
    }
}

/** Sticker: Drop holds up the word on a chunky glossy sticker. The cutest one. */
@Composable
private fun Sticker(c: CardContent, p: CardPalette) {
    Box(Modifier.fillMaxSize()) {
        PaintedPaper(p, c.seed, layout = 1)
        Column(Modifier.fillMaxSize().padding(26.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .border(5.dp, Color.White, RoundedCornerShape(30.dp))
                    .background(Brush.verticalGradient(listOf(Color.White, p.paper)), RoundedCornerShape(30.dp))
                    .padding(horizontal = 24.dp, vertical = 22.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (c.isWord) c.headline else c.headline,
                        style = sans.copy(
                            fontSize = if (c.isWord) scaled(42.sp, c.headline.length, 9) else scaled(24.sp, c.headline.length, 80, 0.62f),
                            lineHeight = if (c.isWord) scaled(46.sp, c.headline.length, 9) else scaled(31.sp, c.headline.length, 80, 0.62f),
                            fontWeight = FontWeight.Black,
                        ),
                        color = p.ink, textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (c.isWord) c.body else "— ${c.byline}",
                        style = sans.copy(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold),
                        color = p.ink.copy(alpha = 0.72f), textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            DropMascot(size = 110.dp, wash = Wash.entries[(c.seed and 0x7fffffff) % Wash.entries.size], expression = Expression.Giggle, animate = false, interactive = false)
            Box(Modifier.size(10.dp).clip(CircleShape))
        }
        Stamp(p.ink, size = 58)
    }
}

/** A share card shrunk to [width] by lowering density, so its 360dp layout stays intact. */
@Composable
fun CardThumbnail(content: CardContent, template: CardTemplate, palette: CardPalette, width: androidx.compose.ui.unit.Dp, ratio: Float = 2f / 3f, modifier: Modifier = Modifier) {
    val outer = androidx.compose.ui.platform.LocalDensity.current
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(outer.density * width.value / 360f, 1f),
    ) {
        ShareCard(content, template, palette, modifier.size(360.dp, (360f / ratio).dp))
    }
}
