package com.kairos.app.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import com.kairos.app.KairosApp
import com.kairos.app.data.prefs.ThemeMode
import com.kairos.app.ui.glass.LocalHazeState
import com.kairos.app.ui.navigation.LocalScene
import com.kairos.app.ui.navigation.SceneController
import com.kairos.app.ui.paint.WatercolorBackground
import com.kairos.app.ui.screens.capsule.CapsuleHomeScreen
import com.kairos.app.ui.screens.home.HomeScreen
import com.kairos.app.ui.screens.onboarding.OnboardingScreen
import com.kairos.app.ui.screens.studio.CardContent
import com.kairos.app.ui.screens.studio.CardPalette
import com.kairos.app.ui.screens.studio.CardTemplate
import com.kairos.app.ui.screens.studio.CardThumbnail
import com.kairos.app.ui.theme.KairosTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Renders real screens with the bundled content, for visual review (build/outputs/roborazzi). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h860dp-xxhdpi")
class ScreenshotTest {
    @get:Rule val compose = createComposeRule()

    private fun app() = ApplicationProvider.getApplicationContext<KairosApp>()

    private fun shoot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        runBlocking { app().container.kairos.syncContent() }
        compose.setContent {
            KairosTheme(themeMode = if (dark) ThemeMode.Dusk else ThemeMode.Daybreak, reducedMotion = true) {
                val haze = remember { HazeState() }
                val scene = remember { SceneController() }
                Box(Modifier.fillMaxSize()) {
                    WatercolorBackground(scene.washes, Modifier.haze(haze))
                    CompositionLocalProvider(LocalHazeState provides haze, LocalScene provides scene) { content() }
                }
            }
        }
        // Room and DataStore work on real background threads; give them real time.
        repeat(12) {
            Thread.sleep(250)
            compose.mainClock.advanceTimeBy(500)
            compose.waitForIdle()
        }
        compose.onRoot().captureRoboImage("screenshots/$name.png")
    }

    private fun home(tab: Int) = @Composable {
        HomeScreen(tab, { _, _ -> }, {}, {}, { _, _ -> }, {}, {}, {})
    }

    @Test fun today() {
        runBlocking { app().container.prefs.completeOnboarding("Prashant", setOf("wonder", "feelings"), setOf("hope", "joy"), 2) }
        shoot("01_today", content = home(0))
    }
    @Test fun todayDusk() {
        runBlocking { app().container.prefs.completeOnboarding("Prashant", setOf("nature"), setOf("calm"), 2) }
        shoot("02_today_dusk", dark = true, content = home(0))
    }
    @Test fun library() = shoot("03_library", content = home(1))
    @Test fun capsule() = shoot("04_capsule") { CapsuleHomeScreen({}, {}) }
    @Test fun me() = shoot("05_me", content = home(3))
    @Test fun onboarding() = shoot("06_onboarding") { OnboardingScreen {} }

    @Test fun shareCards() {
        val word = CardContent(true, "Petrichor", "PET-rih-kor", "noun", "The earthy smell of rain falling on dry ground.", "After the storm, the garden smelled of petrichor.", seed = 7)
        val quote = CardContent(false, "Hope is the thing with feathers that perches in the soul.", byline = "Emily Dickinson", seed = 11)
        compose.setContent {
            androidx.compose.foundation.layout.Column(Modifier) {
                CardTemplate.entries.chunked(2).forEachIndexed { row, pair ->
                    androidx.compose.foundation.layout.Row {
                        pair.forEachIndexed { col, t ->
                            val i = row * 2 + col
                            CardThumbnail(if (i % 2 == 0) word else quote, t, CardPalette.entries[i % CardPalette.entries.size], width = androidx.compose.ui.unit.Dp(200f))
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/07_cards.png")
    }
}
