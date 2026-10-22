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
            w("Akrasia", "noun", "uh-KRAY-zee-uh", "Acting against your own better judgement.", "Akrasia won again: one more episode at 2am.", "Greek a- (without) + kratos (power).", "weakness of will", "wisdom", 4, "", shown = 1).copy(language = "Greek"),
            w("Saudade", "noun", "sow-DAH-djee", "A tender longing for something or someone you love and have lost.", "The old song filled her with saudade for summers at her grandmother's.", "Portuguese, from Latin solitas, solitude.", "longing, yearning", "feelings", 4, "", shown = 0).copy(language = "Portuguese"),
            w("Halcyon", "adjective", "HAL-see-un", "Calm, golden and peaceful, usually about the past.", "Those halcyon summers at the lake.", "Greek halkyon, a bird said to calm the sea.", "idyllic, serene", "wonder", 3, ""),
        ))
        c.db.quotes().insertAll(listOf(
            QuoteEntity("q1", "Nature does not hurry, yet everything is accomplished.", "Lao Tzu", "calm", "From the Taoist tradition of the Tao Te Ching.", generatedAt = 1),
            QuoteEntity("q2", "Hope is the thing with feathers that perches in the soul.", "Emily Dickinson", "hope", "Opening line of her 1861 poem.", generatedAt = 1, saved = true, timesShown = 1),
            QuoteEntity("q4", "I'm allowed to go slowly and still get somewhere.", "", "comfort", "", generatedAt = 1, kind = "affirmation"),
            QuoteEntity("q5", "you don't have to finish the whole map to be on the way.", "", "hope", "", generatedAt = 1, kind = "line"),
            QuoteEntity("q3", "Almost everything will work again if you unplug it for a few minutes, including you.", "Anne Lamott", "rest", "", generatedAt = 1, timesShown = 1),
        ))
        c.db.books().insertAll(listOf(
            com.kairos.app.data.db.BookEntity("b1", "Four Thousand Weeks", "Oliver Burkeman", 2021, "A book about time that argues against productivity culture. The average life is about four thousand weeks, and you will never get everything done. Freedom starts when you choose what to neglect.", "You will never clear the list, and that's the point.\nChoose what to neglect on purpose.\nRest is not a reward for finishing.", "The day will never arrive when you finally have everything under control.", "You keep saving thoughts about rest and time.", "living", 3, generatedAt = 1),
            com.kairos.app.data.db.BookEntity("b2", "Braiding Sweetgrass", "Robin Wall Kimmerer", 2013, "A botanist and member of the Potawatomi Nation braids science with Indigenous wisdom. Plants are teachers, and gratitude is a way of seeing.", "Reciprocity: the land gives, we give back.\nNaming things teaches you to love them.", "", "", "nature", 4, generatedAt = 1),
            com.kairos.app.data.db.BookEntity("b3", "Stoner", "John Williams", 1965, "A quiet novel about an ordinary life: a farm boy who becomes a literature professor. Its tenderness is in its plainness.", "A small life can be a full one.\nLove of a subject can carry you.", "", "", "story", 3, generatedAt = 1),
        ))
        c.db.extras().insertAll(listOf(com.kairos.app.data.db.ExtraEntity(kind = "spark", text = "Step outside and find three different shades of green.")))
        c.kairos.ensureDay()
        val now = System.currentTimeMillis()
        val moodRun = listOf("Good", "Okay", "Radiant", "Good", "Low", "Okay", "Good", "Radiant", "Rough", "Okay", "Good", "Good", "Radiant")
        moodRun.forEachIndexed { i, m -> c.db.moods().insert(com.kairos.app.data.db.MoodEntity(epochDay = today() - (moodRun.size - i), mood = m, createdAt = now - 86_400_000L * (moodRun.size - i))) }
        listOf("SavedWord" to 1, "Practice" to 2, "Letter" to 3, "Spark" to 5).forEach { (k, d) -> c.db.moments().insert(com.kairos.app.data.db.MomentEntity(createdAt = now - 86_400_000L * d, epochDay = today() - d, kind = k, label = k)) }
        c.db.capsules().insert(CapsuleEntity(title = "Before the move", letter = "Hi you.", createdAt = now - 86_400_000L * 40, opensAt = now + 86_400_000L * 160, tint = "Lavender", moodAtSeal = "Good"))
        c.db.capsules().insert(CapsuleEntity(title = "", letter = "Hello", createdAt = now - 86_400_000L * 10, opensAt = now + 86_400_000L * 400, tint = "Mint"))
        c.db.capsules().insert(CapsuleEntity(title = "The first spring here", letter = "Hi you. I'm writing this on the balcony, the radio is on and the neighbours are arguing about tomatoes. I hope the new job turned out kinder than you feared. I hope you still walk the long way home.\n\nWhatever happened, I'm proud of you for trying.", createdAt = now - 86_400_000L * 200, opensAt = now - 86_400_000L * 3, openedAt = now - 86_400_000L, tint = "Rose", moodAtSeal = "Okay"))
        c.db.capsules().insert(CapsuleEntity(title = "New year, new desk", letter = "Hello", createdAt = now - 86_400_000L * 365, opensAt = now - 1000, tint = "Peach"))
    }

    private fun shoot(name: String, dark: Boolean = false, seeded: Boolean = true, bar: Int? = null, waits: Int = 12, content: @Composable () -> Unit) {
        if (seeded) seed()
        compose.setContent {
            KairosTheme(if (dark) ThemeMode.Dark else ThemeMode.Light, calmMotion = true) {
                SharedTransitionLayout {
                    Box(Modifier.fillMaxSize().background(com.kairos.app.ui.theme.K.colors.canvas)) {
                        content()
                        if (bar != null) BottomBar(bar, {}, {}, false, Modifier.align(androidx.compose.ui.Alignment.BottomCenter), night = bar == 2)
                    }
                }
            }
        }
        repeat(waits) { Thread.sleep(250); compose.mainClock.advanceTimeBy(500); compose.waitForIdle() }
        compose.onRoot().captureRoboImage("screenshots/$name.png")
    }

    @Test fun a_today() = shoot("01_today", bar = 0) { TodayScreen({}, {}, { _, _ -> }, {}, {}, {}, {}) }
    @Config(qualifiers = "w400dp-h1900dp-xxhdpi")
    @Test fun a_todayFull() = shoot("01b_today_full", bar = 0) { TodayScreen({}, {}, { _, _ -> }, {}, {}, {}, {}) }
    @Test fun b_todayDark() = shoot("02_today_dark", dark = true, bar = 0) { TodayScreen({}, {}, { _, _ -> }, {}, {}, {}, {}) }
    @Test fun c_learn() = shoot("03_learn", bar = 1) { LearnScreen({}, {}, {}, {}) }
    @Test fun d_word() = shoot("04_word") { WordScreen("sonder", {}, {}, {}) }
    @Config(qualifiers = "w400dp-h1500dp-xxhdpi")
    @Test fun c2_learnFull() = shoot("03b_learn_full", bar = 1) { LearnScreen({}, {}, {}, {}) }
    @Config(qualifiers = "w400dp-h1300dp-xxhdpi")
    @Test fun c3_books() = shoot("03c_books", bar = 1) { LearnScreen({}, {}, {}, {}, startSection = 2) }
    @Test fun c4_book() = shoot("04c_book") { com.kairos.app.ui.feature.detail.BookScreen("b1") {} }
    @Test fun d3_akrasia() = shoot("04d_akrasia") { WordScreen("akrasia", {}, {}, {}) }
    @Test fun d2_quote() = shoot("04b_quote") { com.kairos.app.ui.feature.detail.QuoteScreen("q1", {}, {}) }
    @Test fun e_capsule() = shoot("05_capsule", bar = 2) { CapsuleHome({}, {}) }
    @Test fun e2_compose() = shoot("05b_compose") { com.kairos.app.ui.feature.capsule.ComposeLetter({}, {}) }
    @Test fun e3_sealed() = shoot("05c_letter_sealed") { com.kairos.app.ui.feature.capsule.LetterScreen(1, {}, {}) }
    @Test fun e4_ready() = shoot("05d_letter_ready") { com.kairos.app.ui.feature.capsule.LetterScreen(4, {}, {}) }
    @Test fun k_practice() = shoot("11_practice") { com.kairos.app.ui.feature.practice.PracticeScreen {} }
    @Test fun l_studio() = shoot("13_studio") { com.kairos.app.ui.feature.studio.StudioScreen("word", "apricity") {} }
    @Config(qualifiers = "w400dp-h1300dp-xxhdpi")
    @Test fun e5_opened() = shoot("05e_letter_opened") { com.kairos.app.ui.feature.capsule.LetterScreen(3, {}, {}) }
    @Test fun b1_feedEngine() { seed(); val items = runBlocking { app.container.kairos.feedBatch(emptySet()) }; println("FEED " + items.map { it.key }); org.junit.Assert.assertTrue(items.isNotEmpty()) }
    @Test fun b2_feed() = shoot("02b_feed", bar = 1, waits = 30) { com.kairos.app.ui.feature.feed.FeedScreen({ _, _ -> }, {}, {}, {}) }
    @Test fun k2_play() = shoot("11b_play") { com.kairos.app.ui.feature.play.PlayScreen {} }
    @Test fun f_you() = shoot("06_you", bar = 3) { YouScreen({}, {}) }
    @Test fun f2_interests() = shoot("06b_interests") { com.kairos.app.ui.feature.you.InterestsScreen {} }
    @Config(qualifiers = "w400dp-h1700dp-xxhdpi")
    @Test fun f3_youFull() = shoot("06c_you_full", bar = 3) { YouScreen({}, {}) }
    @Test fun g_curator() = shoot("07_curator") { CuratorScreen {} }
    @Test fun g_curatorAwake() { app.container.keys.setApiKey("sk-or-test-key-for-screens"); try { shoot("07b_curator_awake") { CuratorScreen {} } } finally { app.container.keys.setApiKey(null) } }
    @Test fun h_onboarding() = shoot("08_onboarding", seeded = false) { OnboardingScreen({}) }
    @Test fun i_needsKey() = shoot("09_today_no_key", seeded = false, bar = 0) { TodayScreen({}, {}, { _, _ -> }, {}, {}, {}, {}) }

    private val cardWord = CardContent(true, "Apricity", "uh-PRIS-ih-tee", "noun", "The warmth of the sun on a cold winter day.", "We sat on the steps, soaking up the apricity.")
    private val cardQuote = CardContent(false, "Nature does not hurry, yet everything is accomplished.", byline = "Lao Tzu")

    private fun cards(name: String, card: CardContent, ratio: Float, width: Int) {
        val hues = Hue.entries
        compose.setContent {
            KairosTheme(ThemeMode.Light) {
                Column(Modifier.background(Color(0xFFE9EDF3)).padding(3.dp)) {
                    Template.entries.chunked(if (width < 80) 5 else 4).forEachIndexed { r, row ->
                        Row { row.forEachIndexed { i, t -> CardThumbnail(card, t, hues[(r * 5 + i) % hues.size], width.dp, ratio = ratio, modifier = Modifier.padding(2.dp)) } }
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/$name.png")
    }

    private val longQuote = CardContent(false, "Maybe it will all work out so well that you'll laugh at how much you worried that it wouldn't.", byline = "")

    @Config(qualifiers = "w400dp-h1300dp-xxhdpi")
    @Test fun j_cardsWord() = cards("10_cards_word", cardWord, 2f / 3f, 94)

    @Config(qualifiers = "w400dp-h1300dp-xxhdpi")
    @Test fun j_cardsQuote() = cards("10b_cards_quote", cardQuote, 2f / 3f, 94)

    @Config(qualifiers = "w400dp-h1300dp-xxhdpi")
    @Test fun j_cardsStory() = cards("10c_cards_story", longQuote, 9f / 16f, 74)

    @Config(qualifiers = "w400dp-h1300dp-xxhdpi")
    @Test fun j_cardsSquare() = cards("10d_cards_square", cardWord, 1f, 94)

    @Config(qualifiers = "w400dp-h1300dp-xxhdpi")
    @Test fun j_cardsStoryWord() = cards("10e_cards_story_word", cardWord, 9f / 16f, 74)

    @Config(qualifiers = "w400dp-h1300dp-xxhdpi")
    @Test fun j_cardsSquareQuote() = cards("10f_cards_square_quote", longQuote, 1f, 94)
}
