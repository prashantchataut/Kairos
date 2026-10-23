package com.kairos.app.screens

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.kairos.app.data.prefs.ThemeMode
import com.kairos.app.ui.art.KaiLooks
import com.kairos.app.ui.art.drawBloom
import com.kairos.app.ui.art.drawKai
import com.kairos.app.ui.components.drawMark
import com.kairos.app.ui.components.Wordmark
import com.kairos.app.ui.feature.studio.CardContent
import com.kairos.app.ui.feature.studio.CardThumbnail
import com.kairos.app.ui.feature.studio.Hue
import com.kairos.app.ui.feature.studio.Template
import com.kairos.app.ui.theme.Candy
import com.kairos.app.ui.theme.K
import com.kairos.app.ui.theme.KairosTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.PI
import kotlin.math.sin

/**
 * Renders the app's own screens, cards, Kai and the bloom as PNGs for the launch film
 * (video/launch). Everything comes from the production composables and draw functions, so the
 * film shows the app exactly as it is. Output: app/screenshots/film/ (copied into
 * video/launch/public/app by video/launch/scripts/import-app-assets.sh).
 *
 *   ./gradlew :app:testDebugUnitTest --tests 'com.kairos.app.screens.FilmAssetsTest' -Proborazzi.test.record=true
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h860dp-xxhdpi")
class FilmAssetsTest {
    @get:Rule val compose = createComposeRule()

    private fun out(name: String) = "screenshots/film/$name.png"

    private fun settle(waits: Int) = repeat(waits) { Thread.sleep(200); compose.mainClock.advanceTimeBy(500); compose.waitForIdle() }

    /** A real screen over the screenshot test's sample data, at 1200 x 2580. */
    private fun screen(name: String, seeded: Boolean = true, waits: Int = 12, content: @Composable () -> Unit) {
        if (seeded) ScreenshotTest().seed()
        compose.setContent {
            KairosTheme(ThemeMode.Light, calmMotion = true) {
                SharedTransitionLayout { Box(Modifier.fillMaxSize().background(K.colors.canvas)) { content() } }
            }
        }
        settle(waits)
        compose.onRoot().captureRoboImage(out(name))
    }

    /**
     * n frames of a drawing on a [dp] dp square, with t running 0..1. Each frame is captured twice, on
     * black ("_k") and on white ("_w"), so video/launch/scripts/import-app-assets.py can recover the
     * exact alpha (soft shadows and glossy highlights included).
     */
    private fun frames(name: String, n: Int, dp: Int, draw: DrawScope.(Float) -> Unit) {
        val t = mutableFloatStateOf(0f)
        val bg = mutableIntStateOf(0)
        compose.setContent { Canvas(Modifier.size(dp.dp).background(if (bg.intValue == 0) Color.Black else Color.White)) { draw(t.floatValue) } }
        for (i in 0 until n) {
            t.floatValue = if (n == 1) 1f else i / (n - 1f)
            val base = if (n == 1) name else "${name}_%02d".format(i)
            for (b in 0..1) { bg.intValue = b; compose.waitForIdle(); compose.onRoot().captureRoboImage(out(base + if (b == 0) "_k" else "_w")) }
        }
    }

    // ---- screens ------------------------------------------------------------------------------

    @Test fun s_today() = screen("today") { com.kairos.app.ui.feature.today.TodayScreen({}, {}, { _, _ -> }, {}, {}, {}, {}) }

    @Test fun s_feed() {
        screen("feed_0", waits = 30) { com.kairos.app.ui.feature.feed.FeedScreen({ _, _ -> }, {}, {}, {}) }
        for (i in 1..3) {
            compose.onRoot().performTouchInput { swipeUp() }
            settle(8)
            compose.onRoot().captureRoboImage(out("feed_$i"))
        }
    }

    @Test fun s_play() {
        screen("play_front") { com.kairos.app.ui.feature.play.PlayScreen {} }
        compose.onRoot().performTouchInput { click(center) }
        settle(8)
        compose.onRoot().captureRoboImage(out("play_back"))
    }

    @Test fun s_compose0() = screen("compose_write") { com.kairos.app.ui.feature.capsule.ComposeLetter({}, {}) }
    @Test fun s_compose1() = screen("compose_tuck") { com.kairos.app.ui.feature.capsule.ComposeLetter({}, {}, startStep = 1) }
    @Test fun s_compose2() = screen("compose_when") { com.kairos.app.ui.feature.capsule.ComposeLetter({}, {}, startStep = 2) }
    @Test fun s_letterSealed() = screen("letter_sealed") { com.kairos.app.ui.feature.capsule.LetterScreen(1, {}, {}) }
    @Test fun s_letterOpened() = screen("letter_opened") { com.kairos.app.ui.feature.capsule.LetterScreen(3, {}, {}) }
    @Test fun s_capsule() = screen("capsule") { com.kairos.app.ui.feature.capsule.CapsuleHome({}, {}) }
    @Test fun s_studio() = screen("studio") { com.kairos.app.ui.feature.studio.StudioScreen("word", "apricity") {} }
    @Test fun s_word() = screen("word_sonder") { com.kairos.app.ui.feature.detail.WordScreen("sonder", {}, {}, {}) }
    @Test fun s_onboarding() = screen("onboarding_hello", seeded = false) { com.kairos.app.ui.feature.onboarding.OnboardingScreen({}) }

    // ---- Kai, drawn by the app's drawKai --------------------------------------------------------

    @Test fun k_blink() = frames("kai_blink", 9, 220) { t ->
        val b = listOf(1f, 0.7f, 0.35f, 0.08f, 0.08f, 0.4f, 0.75f, 1f, 1f)[(t * 8).toInt()]
        drawKai(KaiLooks.Curator, KaiLooks.Curator, 1f, blink = b)
    }

    @Test fun k_breathe() = frames("kai_breathe", 16, 220) { t -> drawKai(KaiLooks.Curator, KaiLooks.Curator, 1f, breathe = sin(t * 2f * PI.toFloat())) }

    @Test fun k_delight() = frames("kai_delight", 12, 220) { t -> drawKai(KaiLooks.Curator, KaiLooks.Delighted, t) }

    @Test fun k_wow() = frames("kai_wow", 12, 220) { t -> drawKai(KaiLooks.Curator, KaiLooks.Wow, t) }

    @Test fun k_curator() = frames("kai_curator", 1, 220) { drawKai(KaiLooks.Curator, KaiLooks.Curator, 1f) }
    @Test fun k_radiant() = frames("kai_radiant", 1, 220) { drawKai(KaiLooks.Radiant, KaiLooks.Radiant, 1f) }
    @Test fun k_good() = frames("kai_good", 1, 220) { drawKai(KaiLooks.Good, KaiLooks.Good, 1f) }
    @Test fun k_okay() = frames("kai_okay", 1, 220) { drawKai(KaiLooks.Okay, KaiLooks.Okay, 1f) }
    @Test fun k_low() = frames("kai_low", 1, 220) { drawKai(KaiLooks.Low, KaiLooks.Low, 1f) }
    @Test fun k_thinking() = frames("kai_thinking", 1, 220) { drawKai(KaiLooks.Thinking, KaiLooks.Thinking, 1f) }

    // ---- the bloom and the logo -------------------------------------------------------------------

    /** The mark unfolding petal by petal (clockwise from the moment petal), then the core. */
    private fun DrawScope.unfold(t: Float, petal: Color) {
        val order = listOf(1, 2, 3, 4, 5, 6, 7, 0)
        drawBloom(
            radius = size.minDimension * 0.46f,
            petal = petal,
            unfold = { i -> val s = order.indexOf(i) * 0.075f; backOut(((t - s) / 0.42f).coerceIn(0f, 1f)) },
            core01 = backOut(((t - 0.62f) / 0.3f).coerceIn(0f, 1f)),
        )
    }

    private fun backOut(x: Float): Float { val c = 1.70158f; val y = x - 1f; return 1f + (c + 1f) * y * y * y + c * y * y }

    @Test fun b_unfoldWhite() = frames("bloom_white", 40, 300) { t -> unfold(t, Color.White) }
    @Test fun b_unfoldInk() = frames("bloom_ink", 40, 300) { t -> unfold(t, Candy.Navy) }

    @Test fun b_markWhite() = frames("mark_white", 1, 300) { drawMark(Color.White, 0f) }
    @Test fun b_markInk() = frames("mark_ink", 1, 300) { drawMark(Candy.Navy, 0f) }

    /** The wordmark exactly as the app sets it (Wordmark), scaled up 4x through density, on black and white. */
    @Config(qualifiers = "w900dp-h300dp-xxhdpi")
    @Test fun b_wordmark() {
        val which = mutableIntStateOf(0)
        compose.setContent {
            KairosTheme(ThemeMode.Light) {
                val d = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(d.density * 4f, d.fontScale)) {
                    val (ink, bg) = listOf(Color.White to Color.Black, Color.White to Color.White, Candy.Navy to Color.Black, Candy.Navy to Color.White)[which.intValue]
                    Box(Modifier.background(bg)) { Wordmark(color = ink) }
                }
            }
        }
        listOf("wordmark_white_k", "wordmark_white_w", "wordmark_ink_k", "wordmark_ink_w").forEachIndexed { n, name -> which.intValue = n; compose.waitForIdle(); compose.onRoot().captureRoboImage(out(name)) }
    }

    // ---- share cards: the studio's own templates, 1080 px wide ------------------------------------

    private val word = CardContent(true, "Saudade", "sow-DAH-djee", "noun", "A tender longing for something or someone you love and have lost.", "")
    private val apricity = CardContent(true, "Apricity", "uh-PRIS-ih-tee", "noun", "The warmth of the sun on a cold winter day.", "")
    private val thought = CardContent(false, "Some days the bravest thing is to stay soft.", byline = "")

    private val picks = listOf(
        Triple(Template.Bloom, Hue.Sky, word), Triple(Template.Bold, Hue.Sky, apricity), Triple(Template.Poster, Hue.Mint, thought),
        Triple(Template.Glow, Hue.Peach, apricity), Triple(Template.Eclipse, Hue.Ink, word), Triple(Template.Garden, Hue.Lavender, apricity),
        Triple(Template.Stage, Hue.Mint, thought), Triple(Template.Window, Hue.Sky, apricity), Triple(Template.BlurBloom, Hue.Sky, thought),
        Triple(Template.Starry, Hue.Ink, thought), Triple(Template.Breathe, Hue.Peach, thought), Triple(Template.Swiss, Hue.Sky, word),
    )

    private fun cards(shape: String, ratio: Float) {
        val i = mutableIntStateOf(0)
        compose.setContent {
            KairosTheme(ThemeMode.Light) {
                val (t, h, c) = picks[i.intValue]
                CardThumbnail(c, t, h, 360.dp, ratio = ratio)
            }
        }
        picks.forEachIndexed { n, (t, h, _) -> i.intValue = n; compose.waitForIdle(); compose.onRoot().captureRoboImage(out("card_${shape}_${t.name.lowercase()}_${h.name.lowercase()}")) }
    }

    @Config(qualifiers = "w400dp-h1300dp-xxhdpi") @Test fun c_pin() = cards("pin", 2f / 3f)
    @Config(qualifiers = "w400dp-h1300dp-xxhdpi") @Test fun c_story() = cards("story", 9f / 16f)
    @Config(qualifiers = "w400dp-h1300dp-xxhdpi") @Test fun c_square() = cards("square", 1f)
}
