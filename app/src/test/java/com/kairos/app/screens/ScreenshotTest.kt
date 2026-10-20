package com.kairos.app.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kairos.app.core.design.KairosTheme
import com.kairos.app.core.design.WatercolorBackdrop
import com.kairos.app.core.util.KairosDates
import com.kairos.app.data.Capsule
import com.kairos.app.data.DayLog
import com.kairos.app.data.Profile
import com.kairos.app.data.WordMemory
import com.kairos.app.data.catalog.QuoteCatalog
import com.kairos.app.data.catalog.VocabCatalog
import com.kairos.app.domain.capsule.CapsuleEngine
import com.kairos.app.domain.progress.Progress
import com.kairos.app.domain.share.PosterContent
import com.kairos.app.domain.share.PosterEngine
import com.kairos.app.domain.share.PosterPalettes
import com.kairos.app.domain.share.PosterRatio
import com.kairos.app.domain.share.PosterStyle
import com.kairos.app.ui.capsule.CapsuleDetailScreen
import com.kairos.app.ui.capsule.CapsulesScreen
import com.kairos.app.ui.capsule.CapsulesUi
import com.kairos.app.ui.onboarding.OnboardingScreen
import com.kairos.app.ui.onboarding.OnboardingUi
import com.kairos.app.ui.studio.StudioDesign
import com.kairos.app.ui.studio.StudioScreen
import com.kairos.app.ui.studio.StudioSource
import com.kairos.app.ui.studio.StudioUi
import com.kairos.app.ui.today.TodayScreen
import com.kairos.app.ui.today.TodayUi
import com.kairos.app.ui.words.ReviewUi
import com.kairos.app.ui.words.ReviewScreen
import com.kairos.app.ui.words.WordDetailScreen
import com.kairos.app.ui.words.WordFilter
import com.kairos.app.ui.words.WordItem
import com.kairos.app.ui.words.WordsScreen
import com.kairos.app.ui.words.WordsUi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xhdpi")
class ScreenshotTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val today = LocalDate.of(2026, 10, 20)
    private val now = System.currentTimeMillis()

    private fun capture(name: String, width: Int = 822, height: Int = 1782) {
        shadowOf(Looper.getMainLooper()).idle()
        val decor: View = rule.activity.window.decorView
        decor.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
        )
        decor.layout(0, 0, width, height)
        shadowOf(Looper.getMainLooper()).idle()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        decor.draw(Canvas(bitmap))
        val dir = File("build/screenshots").apply { mkdirs() }
        FileOutputStream(File(dir, "$name.png")).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun screen(dark: Boolean = false, content: @Composable () -> Unit) {
        rule.setContent {
            KairosTheme(darkTheme = dark, accentName = "Blossom", reduceMotion = true) {
                WatercolorBackdrop {
                    Box(Modifier.fillMaxSize()) { content() }
                }
            }
        }
    }

    private fun todayState(): TodayUi {
        val week = Progress.week(
            mapOf(
                today.toString() to DayLog(today.toString(), reviewed = 2, mood = "good"),
                today.minusDays(1).toString() to DayLog(today.minusDays(1).toString(), reviewed = 1),
                today.minusDays(2).toString() to DayLog(today.minusDays(2).toString(), reviewed = 3),
            ),
            today,
        )
        return TodayUi(
            profile = Profile(name = "YG", onboarded = true, dailyPace = 5, joinedOn = "2026-10-01"),
            greeting = "Good evening",
            dateLabel = "October 20",
            weekday = "Tuesday",
            dayNumber = 20,
            quote = QuoteCatalog.byId.getValue("st-01"),
            quoteReason = "picked for your stillness shelf",
            quoteAlternatives = 6,
            word = VocabCatalog.byId.getValue("wd-021"),
            wordMemory = WordMemory("wd-021", intervalDays = 6, seen = 4, repetitions = 3, saved = true),
            streak = 3,
            week = week,
            mood = "good",
            dueCount = 4,
            learnedCount = 12,
            savedCount = 9,
            readyCapsules = 1,
            nextCapsuleDays = "3 months",
            todayLine = "4 words ready for a look",
        )
    }

    @Test
    fun todayLight() {
        screen {
            TodayScreen(
                state = todayState(),
                onOpenWord = {}, onStartReview = {}, onLove = {}, onAnother = {},
                onShare = {}, onMood = {}, onOpenYou = {}, onNewCapsule = {}, onOpenCapsule = {},
            )
        }
        capture("today")
    }

    @Test
    fun todayDark() {
        screen(dark = true) {
            TodayScreen(
                state = todayState(),
                onOpenWord = {}, onStartReview = {}, onLove = {}, onAnother = {},
                onShare = {}, onMood = {}, onOpenYou = {}, onNewCapsule = {}, onOpenCapsule = {},
            )
        }
        capture("today-dark")
    }

    @Test
    fun onboarding() {
        screen {
            OnboardingScreen(
                state = OnboardingUi(step = 0),
                onName = {}, onToggleInterest = {}, onToggleQuoteTheme = {},
                onPace = {}, onNext = {}, onBack = {}, onFinish = {},
            )
        }
        capture("onboarding")
    }

    @Test
    fun words() {
        val items = listOf("wd-021", "wd-001", "wd-051", "wd-062", "wd-011", "wd-032").mapIndexed { index, id ->
            val word = VocabCatalog.byId.getValue(id)
            WordItem(
                word = word,
                memory = WordMemory(id, intervalDays = index * 4, seen = if (index == 0) 0 else index + 1, saved = index % 3 == 0),
                mastery = (index / 6f).coerceIn(0f, 1f),
                due = index == 1 || index == 4,
            )
        }
        screen {
            WordsScreen(
                state = WordsUi(
                    items = items,
                    dueCount = 4,
                    savedCount = 9,
                    learnedCount = 12,
                    metCount = 34,
                    total = 96,
                    pace = 5,
                    filter = WordFilter.All,
                ),
                onQuery = {}, onFilter = {}, onStart = {}, onOpen = {}, onToggleSave = {},
            )
        }
        capture("words")
    }

    @Test
    fun wordDetail() {
        screen {
            WordDetailScreen(
                wordId = "wd-011",
                memory = WordMemory("wd-011", intervalDays = 9, seen = 5, repetitions = 3, saved = true),
                onBack = {}, onToggleSave = {}, onShare = {}, onPractice = {},
            )
        }
        capture("word-detail")
    }

    @Test
    fun review() {
        val card = com.kairos.app.domain.vocab.SessionBuilder.Card(
            word = VocabCatalog.byId.getValue("wd-002"),
            memory = WordMemory("wd-002"),
            reason = "new for you",
            isNew = true,
        )
        screen {
            ReviewScreen(
                session = ReviewUi(cards = listOf(card), revealed = true),
                preview = { "3 days" },
                onReveal = {}, onGrade = {}, onClose = {}, onSpeak = {},
            )
        }
        capture("review")
    }

    private fun capsules(): List<Capsule> = listOf(
        Capsule(
            id = "c1",
            title = "For the version of me who finally rests",
            body = "You were tired, and you kept going anyway. I hope you're reading this from somewhere with a window.",
            createdAt = now - 30L * 86_400_000L,
            unlockAt = now - 86_400_000L,
            mood = "hopeful",
        ),
        Capsule(
            id = "c2",
            title = "The flat on the quiet street",
            body = "Remember the light in the kitchen at 6pm.",
            createdAt = now - 5L * 86_400_000L,
            unlockAt = now + 12L * 86_400_000L,
            mood = "tender",
        ),
        Capsule(
            id = "c3",
            title = "A promise about the guitar",
            body = "Learn three songs. Badly is fine.",
            createdAt = now - 90L * 86_400_000L,
            unlockAt = now + 300L * 86_400_000L,
            mood = "determined",
        ),
        Capsule(
            id = "c4",
            title = "The first week here",
            body = "It smelled like paint and rain.",
            createdAt = now - 400L * 86_400_000L,
            unlockAt = now - 380L * 86_400_000L,
            openedAt = now - 379L * 86_400_000L,
        ),
    )

    @Test
    fun capsuleVault() {
        val list = capsules()
        screen {
            CapsulesScreen(
                state = CapsulesUi(
                    now = now,
                    vault = CapsuleEngine.vault(list, now),
                    presets = CapsuleEngine.presets(now, java.time.ZoneId.systemDefault()),
                ),
                onCompose = {},
                onOpen = {},
            )
        }
        capture("capsules")
    }

    @Test
    fun capsuleDetail() {
        screen {
            CapsuleDetailScreen(
                capsule = capsules()[1],
                onBack = {}, onOpen = {}, onDelete = {},
            )
        }
        capture("capsule-detail")
    }

    @Test
    fun studio() {
        val word = VocabCatalog.byId.getValue("wd-011")
        val content = PosterContent(
            kicker = word.partOfSpeech,
            title = word.term,
            phonetic = word.ipa,
            body = word.definition,
            note = word.example,
            signature = KairosDates.monthDayYear(today),
        )
        val poster = PosterEngine.render(
            context = rule.activity,
            style = PosterStyle.Wash,
            palette = PosterPalettes.Apricot,
            ratio = PosterRatio.Portrait,
            content = content,
            widthPx = 760,
        )
        screen {
            StudioScreen(
                state = StudioUi(
                    design = StudioDesign(style = PosterStyle.Wash, palette = PosterPalettes.Apricot, source = StudioSource.Word),
                    word = word,
                    quote = QuoteCatalog.all.first(),
                    content = content,
                    bitmap = poster,
                    rendering = false,
                    wordOptions = VocabCatalog.all.take(14),
                ),
                onStyle = {}, onPalette = {}, onRatio = {}, onSource = {},
                onWord = {}, onQuote = {}, onShuffle = {}, onSave = {}, onShare = {},
                status = null,
            )
        }
        capture("studio")
    }

    @Test
    fun posters() {
        rule.setContent { Box(Modifier.fillMaxSize()) }
        val dir = File("build/screenshots").apply { mkdirs() }
        val word = VocabCatalog.byId.getValue("wd-011")
        val quote = QuoteCatalog.byId.getValue("st-03")
        PosterStyle.entries.forEachIndexed { index, style ->
            val palette = PosterPalettes.all[index % PosterPalettes.all.size]
            val content = if (index % 2 == 0) {
                PosterContent(
                    kicker = word.partOfSpeech,
                    title = word.term,
                    phonetic = word.ipa,
                    body = word.definition,
                    note = word.example,
                    signature = "October 20, 2026",
                )
            } else {
                PosterContent(
                    kicker = "a thought on stillness",
                    title = quote.text,
                    phonetic = null,
                    body = quote.author,
                    note = null,
                    signature = "October 20, 2026",
                )
            }
            val bitmap = PosterEngine.render(
                context = rule.activity,
                style = style,
                palette = palette,
                ratio = PosterRatio.Portrait,
                content = content,
                widthPx = 1080,
            )
            FileOutputStream(File(dir, "poster-${style.id}.png")).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }
}
