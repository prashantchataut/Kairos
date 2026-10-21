package com.kairos.app.ui.feature.studio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.ui.art.Paints
import com.kairos.app.ui.art.drawGrain
import com.kairos.app.ui.art.painted
import com.kairos.app.ui.components.drawMark
import com.kairos.app.ui.theme.Sans
import com.kairos.app.ui.theme.Serif
import com.kairos.app.ui.theme.SerifText
import kotlin.math.abs

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
enum class Template(val label: String) { Poster("Poster"), Bold("Bold"), Swiss("Swiss"), Glow("Glow"), Bloom("Bloom"), Painted("Painted"), Paper("Paper") }
enum class Format(val label: String, val ratio: Float) { Pin("Pin", 2f / 3f), Story("Story", 9f / 16f), Square("Square", 1f) }

/** Colour moods for the cards. Each template reads them its own way. */
enum class Hue(val label: String, val a: Color, val b: Color, val pastel: Color, val ink: Color, val bold: Color) {
    Sky("Sky", Color(0xFF3B82F0), Color(0xFFA9CBFA), Color(0xFFDCEAFD), Color(0xFF14306E), Color(0xFF2A14C2)),
    Mint("Mint", Color(0xFF16A37A), Color(0xFFA8E6CF), Color(0xFFD5F0E6), Color(0xFF0E4A38), Color(0xFF0B7A55)),
    Lavender("Lavender", Color(0xFF7A5CF0), Color(0xFFD2C4FF), Color(0xFFE7E0FB), Color(0xFF2E2470), Color(0xFF6A22D9)),
    Peach("Peach", Color(0xFFF07F4F), Color(0xFFFFC9AE), Color(0xFFFCE5D8), Color(0xFF6B2C12), Color(0xFFF0501E)),
    Ink("Ink", Color(0xFF0E1726), Color(0xFF3A4A68), Color(0xFFE9EDF3), Color(0xFF0E1726), Color(0xFF111111)),
}

private fun fit(base: TextUnit, len: Int, at: Int, min: Float = 0.5f): TextUnit = if (len <= at) base else (base.value * (at.toFloat() / len).coerceAtLeast(min)).sp

private val serif = TextStyle(fontFamily = Serif)
private val serifText = TextStyle(fontFamily = SerifText)
private val sans = TextStyle(fontFamily = Sans)

/** Every card is laid out on a 360dp-wide artboard; the studio scales it and the exporter renders it at 1080px. */
@Composable
fun ShareCard(c: CardContent, t: Template, h: Hue, modifier: Modifier = Modifier) {
    Box(modifier.clipToBounds()) {
        when (t) {
            Template.Poster -> PosterCard(c, h)
            Template.Bold -> BoldCard(c, h)
            Template.Swiss -> SwissCard(c, h)
            Template.Glow -> GlowCard(c, h)
            Template.Bloom -> BloomCard(c, h)
            Template.Painted -> PaintedCard(c, h)
            Template.Paper -> PaperCard(c, h)
        }
    }
}

@Composable
private fun BoxScope.Brand(color: Color, align: Alignment = Alignment.BottomStart, padding: Dp = 26.dp) {
    Row(Modifier.align(align).padding(padding), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(15.dp)) { drawMark(color, 0f) }
        Spacer(Modifier.width(6.dp))
        Text("Kairos", style = serif.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold), color = color)
    }
}

private fun DrawScope.blob(c: Offset, r: Float, color: Color) =
    drawCircle(Brush.radialGradient(listOf(color, color.copy(alpha = 0f)), c, r), r, c)

/** Poster (r18, "cards"): a grainy, out-of-focus sky and meadow, a dictionary header, the word huge at the bottom. */
@Composable
private fun PosterCard(c: CardContent, h: Hue) {
    val (top, low) = when (h) {
        Hue.Sky -> Color(0xFF7FB2F0) to Color(0xFF4E8F3A)
        Hue.Mint -> Color(0xFF8FD9C4) to Color(0xFF1F6E4C)
        Hue.Lavender -> Color(0xFFB8A6F5) to Color(0xFFD9708F)
        Hue.Peach -> Color(0xFFFFC28F) to Color(0xFFD9542B)
        Hue.Ink -> Color(0xFF2A3A6B) to Color(0xFF0B0F1E)
    }
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.linearGradient(listOf(top, lerp(top, low, 0.55f), low), Offset(0f, 0f), Offset(size.width * 0.7f, size.height)))
            blob(Offset(size.width * 0.75f, size.height * 0.35f), size.width * 0.75f, lerp(low, Color.Black, 0.15f).copy(alpha = 0.7f))
            blob(Offset(size.width * 0.15f, size.height * 0.6f), size.width * 0.6f, lerp(low, Color.White, 0.25f).copy(alpha = 0.6f))
            blob(Offset(size.width * 0.05f, size.height * 0.08f), size.width * 0.55f, lerp(top, Color.White, 0.3f).copy(alpha = 0.8f))
            drawGrain(0.24f)
        }
        Column(Modifier.align(Alignment.TopStart).padding(26.dp).fillMaxWidth(0.72f)) {
            if (c.isWord) {
                Text("${c.headline.lowercase()} · ${c.partOfSpeech}", style = sans.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(Modifier.height(3.dp))
                Text(c.body, style = sans.copy(fontSize = 13.sp, lineHeight = 17.sp), color = Color.White.copy(alpha = 0.92f))
            } else {
                Text(c.byline, style = sans.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(Modifier.height(3.dp))
                Text("a thought for today", style = sans.copy(fontSize = 13.sp), color = Color.White.copy(alpha = 0.9f))
            }
        }
        // A little pixel flower, like the one dotting the t in r18.
        Canvas(Modifier.align(Alignment.TopEnd).padding(top = 30.dp, end = 30.dp).size(36.dp)) {
            val u = size.width / 7f
            val px = listOf(3 to 0, 3 to 1, 0 to 3, 1 to 3, 5 to 3, 6 to 3, 3 to 5, 3 to 6, 1 to 1, 5 to 1, 1 to 5, 5 to 5, 2 to 2, 4 to 2, 2 to 4, 4 to 4, 3 to 3, 2 to 3, 4 to 3, 3 to 2, 3 to 4)
            px.forEach { (x, y) -> drawRect(Color(0xFFFFE45C), Offset(x * u, y * u), Size(u, u)) }
        }
        Column(Modifier.align(Alignment.BottomStart).padding(start = 22.dp, end = 22.dp, bottom = 58.dp)) {
            if (c.isWord) {
                val fs = fit(108.sp, c.headline.length, 6, 0.36f)
                Text(c.headline, style = serif.copy(fontSize = fs, lineHeight = fs * 0.92f, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.03).sp), color = Color.White, maxLines = 2)
            } else {
                val fs = fit(34.sp, c.headline.length, 70, 0.55f)
                Text(c.headline, style = serif.copy(fontSize = fs, lineHeight = fs * 1.08f, fontWeight = FontWeight.SemiBold), color = Color.White)
            }
        }
        Row(Modifier.align(Alignment.BottomStart).padding(horizontal = 24.dp, vertical = 24.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Kairos", style = sans.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), color = Color.White)
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.8f)))
            Spacer(Modifier.width(10.dp))
            Text(if (c.isWord) "a word for today" else "a thought for today", style = sans.copy(fontSize = 12.sp), color = Color.White.copy(alpha = 0.9f))
        }
    }
}

/** Bold (r22, "great one"): one saturated colour, white grotesk, lines staggered to different indents. */
@Composable
private fun BoldCard(c: CardContent, h: Hue) {
    val lines = if (c.isWord) listOf(c.headline.lowercase()) + chunk(c.body.lowercase(), 3) else chunk(c.headline.lowercase().trimEnd('.') + ".", 2)
    val indents = listOf(0f, 0.32f, 0.12f, 0.42f, 0.2f, 0.05f, 0.36f, 0.16f)
    Box(Modifier.fillMaxSize().background(h.bold)) {
        Column(Modifier.align(Alignment.CenterStart).fillMaxWidth().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy((-2).dp)) {
            val longest = lines.maxOf { it.length }.coerceAtLeast(1)
            lines.take(9).forEachIndexed { i, l ->
                val big = c.isWord && i == 0
                val fs = if (big) fit(70.sp, l.length, 7, 0.5f) else fit(if (c.isWord) 30.sp else 44.sp, longest, if (c.isWord) 14 else 10, 0.45f)
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(
                        l, style = sans.copy(fontSize = fs, lineHeight = fs * 1.0f, fontWeight = FontWeight.Medium, letterSpacing = (-0.04).sp), color = Color.White,
                        maxLines = 1, softWrap = false,
                        modifier = Modifier.fillMaxWidth(1f).padding(start = (if (big) 0f else indents[i % indents.size] * 150f).dp),
                    )
                }
                if (big) Spacer(Modifier.height(12.dp))
            }
        }
        if (!c.isWord) Text(c.byline.lowercase(), style = sans.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium), color = Color.White.copy(alpha = 0.85f), modifier = Modifier.align(Alignment.BottomEnd).padding(26.dp))
        Brand(Color.White)
    }
}

private fun chunk(text: String, per: Int): List<String> = text.split(" ").filter { it.isNotBlank() }.chunked(per).map { it.joinToString(" ") }

/** Swiss (r23, "quotes eg"): electric blue, two white organic shapes almost touching, a yellow seed between them. */
@Composable
private fun SwissCard(c: CardContent, h: Hue) {
    val bg = if (h == Hue.Sky) Color(0xFF0A1CF0) else h.bold
    Box(Modifier.fillMaxSize().background(bg)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val hh = size.height
            val top = Path().apply {
                moveTo(w * 0.36f, hh * 0.47f)
                cubicTo(w * 0.2f, hh * 0.38f, w * 0.28f, hh * 0.18f, w * 0.55f, hh * 0.16f)
                cubicTo(w * 0.75f, hh * 0.14f, w * 0.85f, 0f, w * 1.05f, -hh * 0.02f)
                lineTo(w * 1.05f, hh * 0.42f)
                cubicTo(w * 0.95f, hh * 0.47f, w * 0.75f, hh * 0.47f, w * 0.36f, hh * 0.47f)
                close()
            }
            val bottom = Path().apply {
                moveTo(w * 0.46f, hh * 0.53f)
                cubicTo(w * 0.7f, hh * 0.5f, w * 0.9f, hh * 0.56f, w * 1.05f, hh * 0.64f)
                lineTo(w * 1.05f, hh * 1.05f)
                lineTo(w * 0.78f, hh * 1.05f)
                cubicTo(w * 0.7f, hh * 0.92f, w * 0.44f, hh * 0.86f, w * 0.42f, hh * 0.68f)
                cubicTo(w * 0.4f, hh * 0.6f, w * 0.42f, hh * 0.55f, w * 0.46f, hh * 0.53f)
                close()
            }
            drawPath(top, Color.White)
            drawPath(bottom, Color.White)
            drawArc(bg, 200f, 120f, false, Offset(w * 0.38f, hh * 0.2f), Size(w * 0.3f, hh * 0.2f), style = Stroke(1.5f * density))
            drawCircle(bg, w * 0.12f, Offset(w * 0.62f, hh * 0.72f), style = Stroke(1.5f * density))
            drawCircle(Color(0xFFFFF000), w * 0.055f, Offset(w * 0.5f, hh * 0.5f))
        }
        Column(Modifier.align(Alignment.TopStart).padding(26.dp).fillMaxWidth(0.52f)) {
            Box(Modifier.size(16.dp).background(Color(0xFFFFF000)))
            Spacer(Modifier.height(12.dp))
            Text(if (c.isWord) c.body else c.headline, style = sans.copy(fontSize = fit(16.sp, (if (c.isWord) c.body else c.headline).length, 80, 0.7f), lineHeight = 19.sp), color = Color.White)
        }
        Column(Modifier.align(Alignment.BottomStart).padding(start = 26.dp, bottom = 26.dp)) {
            Text(
                if (c.isWord) c.headline else c.byline, style = sans.copy(fontSize = fit(34.sp, (if (c.isWord) c.headline else c.byline).length, 10, 0.55f), lineHeight = 34.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.03).sp),
                color = Color.White,
            )
            if (c.isWord) Text(c.pronunciation, style = sans.copy(fontSize = 14.sp), color = Color.White.copy(alpha = 0.85f))
        }
        Brand(Color.White, Alignment.BottomEnd)
    }
}

/** Glow (r24, "great bg for sharing cards"): periwinkle into peach, stippled grain, a smiling sun. */
@Composable
private fun GlowCard(c: CardContent, h: Hue) {
    val (top, low) = when (h) {
        Hue.Sky, Hue.Lavender -> Color(0xFF6B73E0) to Color(0xFFF2A57E)
        Hue.Mint -> Color(0xFF4FA59A) to Color(0xFFF5D98A)
        Hue.Peach -> Color(0xFFE77FA0) to Color(0xFFFFC27A)
        Hue.Ink -> Color(0xFF151A40) to Color(0xFF6B3D7A)
    }
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(listOf(top, lerp(top, low, 0.5f), low)))
            val sun = Offset(size.width * 0.5f, size.height * 0.3f)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFE97A).copy(alpha = 0.9f), Color(0xFFFFE97A).copy(alpha = 0f)), sun, size.width * 0.42f), size.width * 0.42f, sun)
            drawCircle(Color(0xFFFFE15A), size.width * 0.13f, sun)
            val e = size.width * 0.018f
            drawCircle(Color(0xFF2A1E4A), e * 0.55f, Offset(sun.x - e * 2.2f, sun.y - e * 0.6f))
            drawCircle(Color(0xFF2A1E4A), e * 0.55f, Offset(sun.x + e * 2.2f, sun.y - e * 0.6f))
            drawArc(Color(0xFF2A1E4A), 20f, 140f, false, Offset(sun.x - e * 1.6f, sun.y), Size(e * 3.2f, e * 2f), style = Stroke(e * 0.5f, cap = StrokeCap.Round))
            drawGrain(0.26f)
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = 30.dp, end = 30.dp, bottom = 70.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (c.isWord) {
                val fs = fit(64.sp, c.headline.length, 8, 0.5f)
                Text(c.headline, style = serif.copy(fontSize = fs, lineHeight = fs, fontWeight = FontWeight.SemiBold), color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text("${c.pronunciation} · ${c.partOfSpeech}", style = sans.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium), color = Color.White.copy(alpha = 0.9f))
                Spacer(Modifier.height(16.dp))
                Text(c.body, style = sans.copy(fontSize = 17.sp, lineHeight = 24.sp), color = Color.White, textAlign = TextAlign.Center)
            } else {
                val fs = fit(30.sp, c.headline.length, 80, 0.6f)
                Text(c.headline, style = serif.copy(fontStyle = FontStyle.Italic, fontSize = fs, lineHeight = fs * 1.2f), color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Text(c.byline, style = sans.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold), color = Color.White.copy(alpha = 0.9f))
            }
        }
        Brand(Color.White, Alignment.BottomCenter)
    }
}

/** Bloom (r16, r17): warm paper and an inflated flower; small, quiet type. */
@Composable
private fun BloomCard(c: CardContent, h: Hue) {
    val petal = when (h) { Hue.Sky -> Color(0xFF8FB8FF); Hue.Mint -> Color(0xFFFFD36B); Hue.Lavender -> Color(0xFFC9A6FF); Hue.Peach -> Color(0xFFFF9A84); Hue.Ink -> Color(0xFFFF8FB0) }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFF5F1EC), Color(0xFFFBEFE6))))) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width; val hh = size.height
            val base = Offset(w * 0.5f, hh * 1.02f)
            val head = Offset(w * 0.52f, hh * 0.62f)
            val stem = Path().apply { moveTo(base.x, base.y); cubicTo(w * 0.62f, hh * 0.88f, w * 0.38f, hh * 0.76f, head.x, head.y) }
            drawPath(stem, Brush.linearGradient(listOf(Color(0xFF2E9E47), Color(0xFF5ACB5E)), base, head), style = Stroke(w * 0.035f, cap = StrokeCap.Round))
            // Two ribbon leaves.
            listOf(Triple(0.42f, 0.86f, -1f), Triple(0.6f, 0.8f, 1f)).forEach { (x, y, s) ->
                val o = Offset(w * x, hh * y)
                val leaf = Path().apply {
                    moveTo(o.x, o.y)
                    cubicTo(o.x + s * w * 0.1f, o.y - hh * 0.08f, o.x + s * w * 0.3f, o.y - hh * 0.06f, o.x + s * w * 0.34f, o.y - hh * 0.12f)
                    cubicTo(o.x + s * w * 0.26f, o.y + hh * 0.01f, o.x + s * w * 0.1f, o.y + hh * 0.02f, o.x, o.y)
                    close()
                }
                drawPath(leaf, Brush.linearGradient(listOf(Color(0xFF1F8A3E), Color(0xFF6BD36E)), o, Offset(o.x + s * w * 0.3f, o.y - hh * 0.1f)))
            }
            // Eight inflated petals with a mint underside and a red seed, like the Microsoft flowers.
            for (i in 0 until 8) {
                rotate(i * 45f - 90f + 22f, head) {
                    val len = w * (if (i % 2 == 0) 0.25f else 0.21f)
                    val pw = w * 0.11f
                    val path = Path().apply {
                        moveTo(head.x, head.y)
                        cubicTo(head.x + len * 0.3f, head.y - pw, head.x + len * 0.9f, head.y - pw * 1.2f, head.x + len, head.y)
                        cubicTo(head.x + len * 0.9f, head.y + pw * 1.2f, head.x + len * 0.3f, head.y + pw, head.x, head.y)
                        close()
                    }
                    drawPath(path, Brush.linearGradient(listOf(Color(0xFFB8F0DD), lerp(petal, Color.White, 0.2f), petal), head, Offset(head.x + len, head.y)))
                }
            }
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFF8A6B), Color(0xFFD9342B)), Offset(head.x - w * 0.015f, head.y - w * 0.015f), w * 0.07f), w * 0.05f, head)
        }
        Column(Modifier.align(Alignment.TopCenter).padding(top = 44.dp, start = 30.dp, end = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.size(22.dp)) { drawMark(Color(0xFF1A1310), 0f) }
            Spacer(Modifier.height(12.dp))
            if (c.isWord) {
                val fs = fit(40.sp, c.headline.length, 10, 0.55f)
                Text(c.headline, style = sans.copy(fontSize = fs, lineHeight = fs, fontWeight = FontWeight.Normal, letterSpacing = (-0.02).sp), color = Color(0xFF1A1310), textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(c.body, style = sans.copy(fontSize = 14.sp, lineHeight = 19.sp), color = Color(0xFF1A1310).copy(alpha = 0.75f), textAlign = TextAlign.Center)
            } else {
                val fs = fit(24.sp, c.headline.length, 70, 0.6f)
                Text(c.headline, style = sans.copy(fontSize = fs, lineHeight = fs * 1.22f), color = Color(0xFF1A1310), textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Text(c.byline, style = sans.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold), color = Color(0xFF1A1310).copy(alpha = 0.7f))
            }
        }
    }
}

/** Painted (r19): the word's own gouache painting with white type in the middle. */
@Composable
private fun PaintedCard(c: CardContent, h: Hue) {
    val pal = when (h) { Hue.Sky -> Paints.Sky; Hue.Mint -> Paints.Garden; Hue.Lavender -> Paints.Dusk; Hue.Peach -> Paints.Peach; Hue.Ink -> Paints.Plum }
    val shadow = Shadow(Color.Black.copy(alpha = 0.35f), Offset(0f, 3f), 16f)
    Box(Modifier.fillMaxSize().painted(abs(c.headline.hashCode()), pal, detail = 1.4f, grainAlpha = 0.12f)) {
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.42f), Color.Black.copy(alpha = 0.1f)))))
        Column(Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (c.isWord) {
                val fs = fit(76.sp, c.headline.length, 7, 0.45f)
                Text(c.headline, style = serif.copy(fontSize = fs, lineHeight = fs, fontWeight = FontWeight.SemiBold, shadow = shadow), color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Text(c.body, style = sans.copy(fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium, shadow = shadow), color = Color.White, textAlign = TextAlign.Center)
            } else {
                val fs = fit(32.sp, c.headline.length, 70, 0.6f)
                Text(c.headline, style = serif.copy(fontSize = fs, lineHeight = fs * 1.15f, fontWeight = FontWeight.SemiBold, shadow = shadow), color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Text(c.byline, style = sans.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, shadow = shadow), color = Color.White)
            }
        }
        Brand(Color.White, Alignment.BottomCenter)
    }
}

/** Paper (r08): a quiet dictionary page, for people who like it plain. */
@Composable
private fun PaperCard(c: CardContent, h: Hue) {
    Box(Modifier.fillMaxSize().background(Color(0xFFF1EEE4))) {
        Column(Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 70.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            if (c.isWord) {
                Text(c.headline.lowercase(), style = serif.copy(fontSize = fit(56.sp, c.headline.length, 10), fontWeight = FontWeight.SemiBold), color = Color(0xFF111111), textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(c.pronunciation, style = sans.copy(fontSize = 13.sp), color = Color(0xFF333333), modifier = Modifier.clip(CircleShape).background(Color.White).padding(horizontal = 12.dp, vertical = 5.dp))
                Spacer(Modifier.height(22.dp))
                Text("(${c.partOfSpeech.take(4)}.) ${c.body}", style = sans.copy(fontSize = 17.sp, lineHeight = 25.sp), color = Color(0xFF222222), textAlign = TextAlign.Center)
                if (c.example.isNotBlank()) {
                    Spacer(Modifier.height(14.dp))
                    Text("“${c.example}”", style = serifText.copy(fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 22.sp), color = Color(0xFF555555), textAlign = TextAlign.Center)
                }
            } else {
                val fs = fit(30.sp, c.headline.length, 80, 0.6f)
                Text("“${c.headline}”", style = serif.copy(fontStyle = FontStyle.Italic, fontSize = fs, lineHeight = fs * 1.22f), color = Color(0xFF151515), textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Text(c.byline, style = sans.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium), color = Color(0xFF555555), textAlign = TextAlign.Center)
            }
        }
        Box(Modifier.align(Alignment.TopCenter).padding(top = 34.dp).width(28.dp).height(3.dp).clip(CircleShape).background(h.a))
        Brand(Color(0xFF333333), Alignment.BottomCenter)
    }
}

/** A card scaled down by density so its 360dp layout stays intact (previews). */
@Composable
fun CardThumbnail(c: CardContent, t: Template, h: Hue, width: Dp, ratio: Float = 2f / 3f, modifier: Modifier = Modifier) {
    val outer = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(outer.density * width.value / 360f, 1f)) {
        ShareCard(c, t, h, modifier.size(360.dp, (360f / ratio).dp))
    }
}
