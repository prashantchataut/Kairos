package com.kairos.app.screens

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import com.kairos.app.KairosApp
import com.kairos.app.data.db.CapsuleEntity
import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import com.kairos.app.data.prefs.ThemeMode
import com.kairos.app.data.repo.today
import com.kairos.app.ui.feature.capsule.CapsuleHome
import com.kairos.app.ui.feature.detail.WordScreen
import com.kairos.app.ui.feature.learn.LearnScreen
import com.kairos.app.ui.feature.onboarding.OnboardingScreen
import com.kairos.app.ui.feature.studio.CardContent
import com.kairos.app.ui.feature.studio.CardThumbnail
import com.kairos.app.ui.feature.studio.Hue
import com.kairos.app.ui.feature.studio.Template
import com.kairos.app.ui.feature.today.TodayScreen
import com.kairos.app.ui.feature.you.CuratorScreen
import com.kairos.app.ui.feature.you.YouScreen
import com.kairos.app.ui.nav.BottomBar
import com.kairos.app.ui.theme.KairosTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Renders real screens over sample curator output, for design review (app/screenshots). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h860dp-xxhdpi")
class ScreenshotTest {
    @get:Rule val compose = createComposeRule()
    private val app get() = ApplicationProvider.getApplicationContext<KairosApp>()

    private fun w(word: String, pos: String, say: String, def: String, ex: String, origin: String, syn: String, cat: String, lvl: Int, hook: String, shown: Int = 1, saved: Boolean = false, due: Long? = null, interval: Int = 0) =
        WordEntity(word.lowercase(), word, pos, say, def, ex, origin, syn, cat, lvl, hook, generatedAt = 1, saved = saved, timesShown = shown, lastShownDay = if (shown > 0) today() - 1 else null, dueDay = due, intervalDays = interval)

    private fun seed() = runBlocking {
        val c = app.container
        c.prefs.completeOnboarding("Prashant", setOf("nature", "wonder", "feelings"), setOf("calm", "hope"), 3)
        c.db.words().insertAll(listOf(
            w("Apricity", "noun", "uh-PRIS-ih-tee", "The warmth of the sun on a cold winter day.", "We sat on the steps, soaking up the apricity.", "From Latin apricus, warmed by the sun.", "sunniness, warmth", "nature", 4, "You keep saving quiet weather words.", shown = 0),
            w("Sonder", "noun", "SON-der", "Realising every stranger has a life as full as yours.", "Stuck in traffic, she felt a wave of sonder.", "Coined in 2012 by John Koenig.", "empathy, awareness", "feelings", 3, "", saved = true, due = today()),
            w("Komorebi", "noun", "koh-moh-REH-bee", "Sunlight filtering through the leaves of trees.", "They picnicked in the soft komorebi.", "Japanese: tree, leak, sun.", "dappled light", "nature", 4, "", saved = true, interval = 30, due = today() + 20),
            w("Lucid", "adjective", "LOO-sid", "Clear and easy to understand.", "Her notes were lucid and calm.", "Latin lucidus, full of light.", "clear, coherent", "mind", 2, ""),
            w("Kindred", "adjective", "KIN-drid", "Alike in spirit; feeling like family.", "They knew at once they were kindred souls.", "Old English cynn, family.", "like-minded", "connection", 2, "", due = today()),
            w("Sprezzatura", "noun", "spret-suh-TOOR-uh", "Making something hard look effortless.", "The dancer's sprezzatura hid years of training.", "Italian, coined by Castiglione in 1528.", "ease, grace", "craft", 5, ""),
            w("Halcyon", "adjective", "HAL-see-un", "Calm, golden and peaceful, usually about the past.", "Those halcyon summers at the lake.", "Greek halkyon, a bird said to calm the sea.", "idyllic, serene", "wonder", 3, ""),
        ))
        c.db.quotes().insertAll(listOf(
            QuoteEntity("q1", "Nature does not hurry, yet everything is accomplished.", "Lao Tzu", "calm", "From the Taoist tradition of the Tao Te Ching.", generatedAt = 1),
            QuoteEntity("q2", "Hope is the thing with feathers that perches in the soul.", "Emily Dickinson", "hope", "Opening line of her 1861 poem.", generatedAt = 1, saved = true, timesShown = 1),
            QuoteEntity("q3", "Almost everything will work again if you unplug it for a few minutes, including you.", "Anne Lamott", "rest", "", generatedAt = 1, timesShown = 1),
        ))
        c.db.extras().insertAll(listOf(com.kairos.app.data.db.ExtraEntity(kind = "spark", text = "Step outside and find three different shades of green.")))
        c.kairos.ensureDay()
        val now = System.currentTimeMillis()
        c.db.capsules().insert(CapsuleEntity(title = "Before the move", letter = "Hi you.", createdAt = now - 86_400_000L * 40, opensAt = now + 86_400_000L * 160, tint = "Lavender", moodAtSeal = "Good"))
        c.db.capsules().insert(CapsuleEntity(title = "", letter = "Hello", createdAt = now - 86_400_000L * 10, opensAt = now + 86_400_000L * 400, tint = "Mint"))
        c.db.capsules().insert(CapsuleEntity(title = "New year, new desk", letter = "Hello", createdAt = now - 86_400_000L * 365, opensAt = now - 1000, tint = "Peach"))
    }

    private fun shoot(name: String, dark: Boolean = false, seeded: Boolean = true, bar: Int? = null, content: @Composable () -> Unit) {
        if (seeded) seed()
        compose.setContent {
            KairosTheme(if (dark) ThemeMode.Dark else ThemeMode.Light, calmMotion = true) {
                SharedTransitionLayout {
                    Box(Modifier.fillMaxSize().background(com.kairos.app.ui.theme.K.colors.canvas)) {
                        content()
                        if (bar != null) BottomBar(bar, {}, {}, false, Modifier.align(androidx.compose.ui.Alignment.BottomCenter))
                    }
                }
            }
        }
        repeat(12) { Thread.sleep(250); compose.mainClock.advanceTimeBy(500); compose.waitForIdle() }
        compose.onRoot().captureRoboImage("screenshots/$name.png")
    }

    @Test fun a_today() = shoot("01_today", bar = 0) { TodayScreen({}, {}, { _, _ -> }, {}, {}, {}, {}) }
    @Test fun b_todayDark() = shoot("02_today_dark", dark = true, bar = 0) { TodayScreen({}, {}, { _, _ -> }, {}, {}, {}, {}) }
    @Test fun c_learn() = shoot("03_learn", bar = 1) { LearnScreen({}, {}, {}, {}) }
    @Test fun d_word() = shoot("04_word") { WordScreen("sonder", {}, {}, {}) }
    @Test fun e_capsule() = shoot("05_capsule", bar = 2) { CapsuleHome({}, {}) }
    @Test fun f_you() = shoot("06_you", bar = 3) { YouScreen({}, {}) }
    @Test fun g_curator() = shoot("07_curator") { CuratorScreen {} }
    @Test fun h_onboarding() = shoot("08_onboarding", seeded = false) { OnboardingScreen({}) }
    @Test fun i_needsKey() = shoot("09_today_no_key", seeded = false, bar = 0) { TodayScreen({}, {}, { _, _ -> }, {}, {}, {}, {}) }

    @Test fun j_cards() {
        val word = CardContent(true, "Apricity", "uh-PRIS-ih-tee", "noun", "The warmth of the sun on a cold winter day.", "We sat on the steps, soaking up the apricity.")
        val quote = CardContent(false, "Nature does not hurry, yet everything is accomplished.", byline = "Lao Tzu")
        compose.setContent {
            KairosTheme(ThemeMode.Light) {
                Column(Modifier.background(Color(0xFFE9EDF3)).padding(8.dp)) {
                    Row { CardThumbnail(word, Template.Sky, Hue.Sky, 190.dp, modifier = Modifier.padding(4.dp)); CardThumbnail(word, Template.Paper, Hue.Sky, 190.dp, modifier = Modifier.padding(4.dp)) }
                    Row { CardThumbnail(word, Template.Note, Hue.Mint, 190.dp, modifier = Modifier.padding(4.dp)); CardThumbnail(quote, Template.Night, Hue.Lavender, 190.dp, modifier = Modifier.padding(4.dp)) }
                    Row { CardThumbnail(quote, Template.Glass, Hue.Peach, 190.dp, modifier = Modifier.padding(4.dp)); CardThumbnail(quote, Template.Note, Hue.Lavender, 190.dp, modifier = Modifier.padding(4.dp)) }
                }
            }
        }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/10_cards.png")
    }
}
