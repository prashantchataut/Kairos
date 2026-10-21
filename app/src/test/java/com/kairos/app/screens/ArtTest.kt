package com.kairos.app.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.kairos.app.data.prefs.ThemeMode
import com.kairos.app.ui.art.DandelionMark
import com.kairos.app.ui.art.SkyBox
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.KairosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Renders the art library for design review (app/screenshots/art_*.png). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h860dp-xxhdpi")
class ArtTest {
    @get:Rule val compose = createComposeRule()

    private fun shoot(name: String, content: @Composable () -> Unit) {
        compose.setContent { KairosTheme(ThemeMode.Light, calmMotion = true) { content() } }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/art_$name.png")
    }

    @Test fun mark() = shoot("mark") {
        Column(Modifier.background(K.colors.paper).padding(16.dp)) {
            SkyBox(Modifier.fillMaxWidth().height(260.dp), fadeToPaper = false) {
                Row(Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    DandelionMark(150.dp, Color.White)
                    DandelionMark(64.dp, Color.White)
                    DandelionMark(28.dp, Color.White)
                }
            }
            Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(0f, 0.35f, 0.7f, 1f).forEach { r -> DandelionMark(80.dp, K.colors.ink, release = r) }
            }
            Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(0.2f, 0.5f, 0.8f, 1f).forEach { r -> DandelionMark(80.dp, K.colors.accent, rays = r, release = 0f) }
            }
            DandelionMark(160.dp, K.colors.accent, withStem = true, modifier = Modifier.padding(top = 8.dp))
        }
    }

    @Test fun icon() = shoot("icon") {
        Row(Modifier.background(Color(0xFF20242C)).padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(160.dp, 72.dp, 48.dp).forEach { sz ->
                Box(Modifier.size(sz).clip(androidx.compose.foundation.shape.CircleShape)) {
                    androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(com.kairos.app.R.mipmap.ic_launcher_background), null, Modifier.matchParentSize().graphicsLayer { scaleX = 1.5f; scaleY = 1.5f }, contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                    androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(com.kairos.app.R.drawable.ic_launcher_foreground), null, Modifier.matchParentSize().graphicsLayer { scaleX = 1.5f; scaleY = 1.5f })
                }
            }
        }
    }
}
