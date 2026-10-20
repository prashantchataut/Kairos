package com.kairos.app.ui.feature.studio

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.kairos.app.ui.components.drawMark
import com.kairos.app.ui.theme.Sans
import com.kairos.app.ui.theme.Serif

data class CardContent(
    val isWord: Boolean,
    val headline: String,
    val pronunciation: String = "",
    val partOfSpeech: String = "",
    val body: String = "",
    val example: String = "",
    val byline: String = "",
)

enum class Template(val label: String) { Sky("Sky"), Paper("Paper"), Note("Note"), Night("Night"), Glass("Glass") }
enum class Format(val label: String, val ratio: Float) { Pin("Pin", 2f / 3f), Story("Story", 9f / 16f), Square("Square", 1f) }

/** Colour moods for the cards. Each template reads them differently. */
enum class Hue(val label: String, val a: Color, val b: Color, val pastel: Color, val ink: Color) {
    Sky("Sky", Color(0xFF3B82F0), Color(0xFFA9CBFA), Color(0xFFDCEAFD), Color(0xFF14306E)),
    Mint("Mint", Color(0xFF16A37A), Color(0xFFA8E6CF), Color(0xFFD5F0E6), Color(0xFF0E4A38)),
    Lavender("Lavender", Color(0xFF6E58E8), Color(0xFFC9BEFA), Color(0xFFE7E0FB), Color(0xFF2E2470)),
    Peach("Peach", Color(0xFFF07F4F), Color(0xFFFCC9AE), Color(0xFFFCE5D8), Color(0xFF6B2C12)),
    Ink("Ink", Color(0xFF0E1726), Color(0xFF3A4A68), Color(0xFFE9EDF3), Color(0xFF0E1726)),
}

private fun fit(base: TextUnit, len: Int, at: Int, min: Float = 0.55f): TextUnit = if (len <= at) base else (base.value * (at.toFloat() / len).coerceAtLeast(min)).sp

private val serif = TextStyle(fontFamily = Serif)
private val sans = TextStyle(fontFamily = Sans)

/** Every card is laid out on a 360dp-wide artboard; the studio scales it, the exporter renders it at 1080px. */
@Composable
fun ShareCard(c: CardContent, t: Template, h: Hue, modifier: Modifier = Modifier) {
    Box(modifier) {
        when (t) {
            Template.Sky -> SkyCard(c, h)
            Template.Paper -> PaperCard(c, h)
            Template.Note -> NoteCard(c, h)
            Template.Night -> NightCard(c, h)
            Template.Glass -> GlassCard(c, h)
        }
    }
}

@Composable
private fun BoxScope.Brand(color: Color, align: Alignment = Alignment.BottomStart) {
    Row(Modifier.align(align).padding(26.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(14.dp)) { drawMark(color, 0f) }
        Spacer(Modifier.width(6.dp))
        Text("Kairos", style = serif.copy(fontSize = 17.sp), color = color)
    }
}

@Composable
private fun WordBlock(c: CardContent, ink: Color, soft: Color, center: Boolean, big: TextUnit = 64.sp) {
    val align = if (center) TextAlign.Center else TextAlign.Start
    Text(c.headline, style = serif.copy(fontSize = fit(big, c.headline.length, 9), lineHeight = fit(big, c.headline.length, 9) * 1.04f), color = ink, textAlign = align, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    Text("${c.pronunciation}   ${c.partOfSpeech}", style = sans.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium), color = soft, textAlign = align, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(22.dp))
    Text(c.body, style = sans.copy(fontSize = 18.sp, lineHeight = 26.sp), color = ink, textAlign = align, modifier = Modifier.fillMaxWidth())
    if (c.example.isNotBlank()) {
        Spacer(Modifier.height(14.dp))
        Text("“${c.example}”", style = serif.copy(fontStyle = FontStyle.Italic, fontSize = 17.sp, lineHeight = 23.sp), color = soft, textAlign = align, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun QuoteBlock(c: CardContent, ink: Color, soft: Color, center: Boolean) {
    val align = if (center) TextAlign.Center else TextAlign.Start
    Text("“${c.headline}”", style = serif.copy(fontStyle = FontStyle.Italic, fontSize = fit(32.sp, c.headline.length, 80, 0.6f), lineHeight = fit(32.sp, c.headline.length, 80, 0.6f) * 1.22f), color = ink, textAlign = align, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(20.dp))
    Text(c.byline, style = sans.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium), color = soft, textAlign = align, modifier = Modifier.fillMaxWidth())
}

/** Sky: the app's own hero, a blue gradient fading to white. */
@Composable
private fun SkyCard(c: CardContent, h: Hue) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to h.a, 0.55f to h.b, 1f to Color(0xFFF6F8FB)))) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.3f), Color.Transparent), Offset(size.width * 0.85f, size.height * 0.1f), size.width * 0.6f), size.width * 0.6f, Offset(size.width * 0.85f, size.height * 0.1f))
        }
        Column(Modifier.fillMaxSize().padding(horizontal = 30.dp, vertical = 70.dp), verticalArrangement = Arrangement.Center) {
            if (c.isWord) WordBlock(c, Color.White, Color.White.copy(alpha = 0.78f), center = true) else QuoteBlock(c, Color.White, Color.White.copy(alpha = 0.8f), center = true)
        }
        Brand(h.ink, Alignment.BottomCenter)
    }
}

/** Paper: a quiet dictionary page, the word in black serif. */
@Composable
private fun PaperCard(c: CardContent, h: Hue) {
    Box(Modifier.fillMaxSize().background(Color(0xFFF4F2EC))) {
        Column(Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 70.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            if (c.isWord) {
                Text(c.headline.lowercase(), style = serif.copy(fontSize = fit(56.sp, c.headline.length, 10)), color = Color(0xFF111111), textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(c.pronunciation, style = sans.copy(fontSize = 13.sp), color = Color(0xFF333333), modifier = Modifier.clip(CircleShape).background(Color.White).padding(horizontal = 12.dp, vertical = 5.dp))
                Spacer(Modifier.height(22.dp))
                Text("(${c.partOfSpeech.take(4)}.) ${c.body}", style = sans.copy(fontSize = 17.sp, lineHeight = 25.sp), color = Color(0xFF222222), textAlign = TextAlign.Center)
            } else QuoteBlock(c, Color(0xFF151515), Color(0xFF555555), center = true)
        }
        Box(Modifier.align(Alignment.TopCenter).padding(top = 34.dp).width(28.dp).height(3.dp).clip(CircleShape).background(h.a))
        Brand(Color(0xFF333333), Alignment.BottomCenter)
    }
}

/** Note: a soft pastel note, like a tile from a notes app. */
@Composable
private fun NoteCard(c: CardContent, h: Hue) {
    Box(Modifier.fillMaxSize().background(Color(0xFFF6F8FB)).padding(18.dp)) {
        Column(Modifier.fillMaxSize().clip(RoundedCornerShape(30.dp)).background(h.pastel).padding(28.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(if (c.isWord) "Word of the day" else "Thought of the day", style = sans.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold), color = h.ink.copy(alpha = 0.7f))
            Column {
                if (c.isWord) WordBlock(c, h.ink, h.ink.copy(alpha = 0.65f), center = false, big = 60.sp) else QuoteBlock(c, h.ink, h.ink.copy(alpha = 0.65f), center = false)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(14.dp)) { drawMark(h.ink, 0f) }
                Spacer(Modifier.width(6.dp))
                Text("Kairos", style = serif.copy(fontSize = 17.sp), color = h.ink)
            }
        }
    }
}

/** Night: deep navy with one luminous glow. */
@Composable
private fun NightCard(c: CardContent, h: Hue) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0B1230), Color(0xFF05081A))))) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(listOf(h.a.copy(alpha = 0.55f), Color.Transparent), Offset(size.width * 0.5f, size.height * 0.42f), size.width * 0.75f), size.width * 0.75f, Offset(size.width * 0.5f, size.height * 0.42f))
        }
        Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 70.dp), verticalArrangement = Arrangement.Center) {
            if (c.isWord) WordBlock(c, Color.White, Color.White.copy(alpha = 0.68f), center = true) else QuoteBlock(c, Color.White, Color.White.copy(alpha = 0.7f), center = true)
        }
        Brand(Color.White.copy(alpha = 0.85f), Alignment.BottomCenter)
    }
}

/** Glass: soft colour light behind a frosted card. */
@Composable
private fun GlassCard(c: CardContent, h: Hue) {
    Box(Modifier.fillMaxSize().background(Color(0xFFEFF3F9))) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(listOf(h.a.copy(alpha = 0.75f), Color.Transparent), Offset(size.width * 0.15f, size.height * 0.2f), size.width * 0.7f), size.width * 0.7f, Offset(size.width * 0.15f, size.height * 0.2f))
            drawCircle(Brush.radialGradient(listOf(h.b, Color.Transparent), Offset(size.width * 0.95f, size.height * 0.85f), size.width * 0.75f), size.width * 0.75f, Offset(size.width * 0.95f, size.height * 0.85f))
        }
        Column(
            Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White.copy(alpha = 0.55f))
                .border(1.dp, Brush.verticalGradient(listOf(Color.White, Color.White.copy(alpha = 0.2f))), RoundedCornerShape(30.dp))
                .padding(28.dp)
        ) {
            if (c.isWord) WordBlock(c, h.ink, h.ink.copy(alpha = 0.65f), center = false, big = 56.sp) else QuoteBlock(c, h.ink, h.ink.copy(alpha = 0.65f), center = false)
        }
        Brand(h.ink)
    }
}

/** A card scaled down by density so its 360dp layout stays intact (onboarding, previews). */
@Composable
fun CardThumbnail(c: CardContent, t: Template, h: Hue, width: Dp, ratio: Float = 2f / 3f, modifier: Modifier = Modifier) {
    val outer = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(outer.density * width.value / 360f, 1f)) {
        ShareCard(c, t, h, modifier.size(360.dp, (360f / ratio).dp))
    }
}
