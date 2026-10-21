package com.kairos.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.kairos.app.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The real launcher, monochrome and notification drawables, masked the way launchers mask them. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w420dp-h180dp-xxhdpi")
class IconTest {
    @get:Rule val compose = createComposeRule()

    @Test fun icons() {
        compose.setContent {
            Row(Modifier.background(Color(0xFFE9EDF3)).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Adaptive(RoundedCornerShape(30.dp))
                Adaptive(CircleShape)
                Box(Modifier.size(96.dp).clip(CircleShape).background(Color(0xFF2B3A55)), contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.ic_launcher_monochrome), null, Modifier.size(140.dp), colorFilter = ColorFilter.tint(Color(0xFFCFE0FF)))
                }
                Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF2F5BFF)), contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.ic_notification), null, Modifier.size(24.dp))
                }
            }
        }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/12_icons.png")
    }

    @androidx.compose.runtime.Composable
    private fun Adaptive(shape: Shape) {
        Box(Modifier.size(108.dp).clip(shape)) {
            Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.size(108.dp))
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(108.dp))
        }
    }
}
