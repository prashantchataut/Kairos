package com.kairos.app.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.takahirom.roborazzi.captureRoboImage
import com.kairos.app.data.prefs.ThemeMode
import com.kairos.app.ui.art.CloudBank
import com.kairos.app.ui.art.Garden
import com.kairos.app.ui.art.Kai
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.art.Paints
import com.kairos.app.ui.art.drawBloom
import com.kairos.app.ui.art.drawCloud
import com.kairos.app.ui.art.grain
import com.kairos.app.ui.art.painted
import com.kairos.app.ui.components.KairosMark
import com.kairos.app.ui.components.KairosSlider
import com.kairos.app.ui.components.SliderBubble
import com.kairos.app.ui.components.Wordmark
import com.kairos.app.ui.components.at
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.KairosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The design system on one sheet: characters, the mark, type, the slider and the art. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h1400dp-xxhdpi")
class DesignSystemTest {
    @get:Rule val compose = createComposeRule()

    @Test fun sheet() {
        compose.setContent {
            KairosTheme(ThemeMode.Light, calmMotion = true) {
                val p = K.colors
                Column(Modifier.background(p.canvas).padding(16.dp)) {
                    val looks = listOf("Curator" to KaiLooks.Curator, "Radiant" to KaiLooks.Radiant, "Good" to KaiLooks.Good, "Okay" to KaiLooks.Okay, "Low" to KaiLooks.Low, "Rough" to KaiLooks.Rough, "Delighted" to KaiLooks.Delighted, "Wow" to KaiLooks.Wow, "Sleepy" to KaiLooks.Sleepy, "Sad" to KaiLooks.Sad)
                    looks.chunked(5).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            row.forEach { (n, l) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Kai(l, size = 70.dp)
                                    Text(n, style = K.type.caption, color = p.inkSoft)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Launcher icon preview.
                        Canvas(Modifier.size(108.dp).clip(RoundedCornerShape(28.dp))) {
                            drawRect(Brush.verticalGradient(listOf(Color(0xFF2F5BFF), Color(0xFF5E9BFF), Color(0xFFBFDBFF))))
                            drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.45f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.52f), radius = size.width * 0.5f))
                            drawBloom(androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.5f), size.width * 0.33f, shaded = true)
                            drawCloud(size.width * 0.02f, size.height * 1.02f, size.width * 0.62f, Color.White, Color(0xFFE3EEFF))
                            drawCloud(size.width * 0.42f, size.height * 1.05f, size.width * 0.66f, Color.White, Color(0xFFE3EEFF))
                        }
                        KairosMark(56.dp, tinted = true)
                        Box(Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(p.skyTop), contentAlignment = Alignment.Center) { KairosMark(36.dp) }
                        KairosMark(28.dp, color = p.ink, mono = true)
                        Wordmark()
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Apricity", style = K.type.hero, color = p.ink)
                    Text("Hello, Prashant.", style = K.type.shout, color = p.ink)
                    Text("Nature does not hurry, yet everything is accomplished.", style = K.type.quote, color = p.ink)
                    Text("Interface text in Bricolage Grotesque, set at sixteen.", style = K.type.body, color = p.inkSoft)
                    Spacer(Modifier.height(8.dp))
                    var v by remember { mutableFloatStateOf(0.5f) }
                    val levelColors = listOf(Candy.Mint, Candy.Sky, Candy.Cobalt, Candy.Lilac, Candy.Bubblegum)
                    KairosSlider(v, { v = it }, "Word level", { "Rich" }, stops = 5, colors = levelColors, bubble = { f -> SliderBubble("Rich", levelColors.at(f)) })
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFF0E1440))) {
                        Garden(listOf(11, 23, 35, 47, 59, 61, 73), Modifier.fillMaxWidth().height(120.dp).align(Alignment.BottomCenter), grow = false)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(Paints.Garden, Paints.Blush, Paints.Dusk, Paints.Honey).forEachIndexed { i, pal ->
                            Box(Modifier.size(width = 86.dp, height = 110.dp).clip(RoundedCornerShape(20.dp)).painted(100 + i, pal), contentAlignment = Alignment.Center) {
                                Text("sonder", style = K.type.serifLarge.copy(fontSize = 22.sp), color = Color.White)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(28.dp)).background(Brush.verticalGradient(listOf(p.skyTop, p.skyMid))).grain(0.06f)) {
                        CloudBank(Modifier.fillMaxWidth().height(110.dp).align(Alignment.BottomCenter), Color.White.copy(alpha = 0.9f), Color(0xFFDDE9FF))
                        Text("Clouds hold the headline", style = K.type.shout.copy(fontSize = 26.sp), color = p.ink, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
                    }
                    Spacer(Modifier.width(1.dp))
                }
            }
        }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/00_design_system.png")
    }
}
