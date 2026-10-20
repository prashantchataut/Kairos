package com.kairos.app.data

import com.kairos.app.data.content.ContentRepository
import com.kairos.app.data.content.Joy
import com.kairos.app.data.content.Quote
import com.kairos.app.data.content.Word
import com.kairos.app.data.db.DailyPick
import com.kairos.app.data.db.JoyLog
import com.kairos.app.data.db.KairosDatabase
import com.kairos.app.data.db.MoodLog
import com.kairos.app.data.db.QuoteState
import com.kairos.app.data.db.WordProgress
import com.kairos.app.data.prefs.SettingsStore
import com.kairos.app.engine.DailyEngine
import com.kairos.app.engine.Grade
import com.kairos.app.engine.SpacedRepetition
import com.kairos.app.engine.Taste
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

fun today(): Long = LocalDate.now().toEpochDay()

data class TodayPicks(
    val word: Word,
    val wordWhy: String,
    val quote: Quote,
    val quoteWhy: String,
    val joy: Joy,
)

/** The quote + vocabulary engine, wired to storage. */
class LearnRepository(
    private val db: KairosDatabase,
    private val content: ContentRepository,
    private val settings: SettingsStore,
) {
    val wordProgress: Flow<List<WordProgress>> = db.words().observeAll()
    val quoteStates: Flow<List<QuoteState>> = db.quotes().observeAll()

    suspend fun words() = content.words()
    suspend fun quotes() = content.quotes()
    suspend fun joys() = content.joys()
    fun observeWord(id: String) = db.words().observe(id)

    private suspend fun taste(): Taste {
        val s = settings.current()
        val recent = db.days().recentPicks(30)
        return Taste(
            wordThemes = s.wordThemes,
            quoteThemes = s.quoteThemes,
            level = s.level,
            words = db.words().all().associateBy { it.wordId },
            quotes = db.quotes().all().associateBy { it.quoteId },
            recentWordIds = recent.map { it.wordId }.toSet(),
            recentQuoteIds = recent.map { it.quoteId }.toSet(),
            recentJoyIds = recent.take(7).map { it.joyId }.toSet(),
        )
    }

    /** Today's picks. Chosen once per local date, then stored so they never shuffle. */
    suspend fun todayPicks(day: Long = today()): TodayPicks {
        val words = content.words()
        val quotes = content.quotes()
        val joys = content.joys()
        val existing = db.days().pick(day)
        val taste = taste()
        val pick = existing ?: run {
            val w = DailyEngine.pickWord(words, taste, day)
            val q = DailyEngine.pickQuote(quotes, taste, day)
            val j = DailyEngine.pickJoy(joys, taste.recentJoyIds, null, day)
            DailyPick(day, w.id, q.id, j.id).also { db.days().insertPick(it) }
        }
        val word = words.first { it.id == pick.wordId }
        val quote = quotes.first { it.id == pick.quoteId }
        markWordSeen(word.id)
        markQuoteSeen(quote.id)
        val tasteForWhy = taste.copy(recentWordIds = emptySet())
        return TodayPicks(
            word = word,
            wordWhy = DailyEngine.whyWord(word, tasteForWhy, words).headline,
            quote = quote,
            quoteWhy = DailyEngine.whyQuote(quote, tasteForWhy, quotes).headline,
            joy = joys.firstOrNull { it.id == pick.joyId } ?: joys.first(),
        )
    }

    /** "Another idea" for the small joy — mood-aware, and remembered for the rest of the day. */
    suspend fun shuffleJoy(mood: String?, salt: Int, day: Long = today()): Joy {
        val joys = content.joys()
        val recent = db.days().recentPicks(7).map { it.joyId }.toSet()
        val current = db.days().pick(day)
        val joy = DailyEngine.pickJoy(joys.filter { it.id != current?.joyId }, recent, mood, day, salt)
        if (current != null) db.days().upsertPick(current.copy(joyId = joy.id))
        return joy
    }

    private suspend fun markWordSeen(id: String) {
        val p = db.words().get(id) ?: WordProgress(id)
        if (!p.seen) db.words().upsert(p.copy(seen = true))
    }

    private suspend fun markQuoteSeen(id: String) {
        val s = db.quotes().get(id) ?: QuoteState(id)
        if (!s.seen) db.quotes().upsert(s.copy(seen = true))
    }

    suspend fun toggleWordSaved(id: String): Boolean {
        val p = db.words().get(id) ?: WordProgress(id)
        val next = p.copy(saved = !p.saved)
        db.words().upsert(next)
        return next.saved
    }

    suspend fun toggleQuoteSaved(id: String): Boolean {
        val s = db.quotes().get(id) ?: QuoteState(id)
        val next = s.copy(saved = !s.saved, seen = true)
        db.quotes().upsert(next)
        return next.saved
    }

    suspend fun review(wordId: String, grade: Grade, day: Long = today()): WordProgress {
        val p = db.words().get(wordId) ?: WordProgress(wordId)
        val next = SpacedRepetition.review(p, grade, day)
        db.words().upsert(next)
        return next
    }

    suspend fun progress(id: String) = db.words().get(id)

    // ---- mood + joy jar ----
    fun observeMood(day: Long = today()) = db.days().observeMood(day)
    fun observeWeekMoods(day: Long = today()) = db.days().observeMoods(day - 6, day)
    suspend fun setMood(mood: String, day: Long = today()) = db.days().setMood(MoodLog(day, mood, System.currentTimeMillis()))
    fun observeJoyCount() = db.days().observeJoyCount()
    fun observeJoysToday(day: Long = today()) = db.days().observeJoysOn(day)
    suspend fun logJoy(joyId: String, day: Long = today()) = db.days().logJoy(JoyLog(joyId = joyId, epochDay = day, at = System.currentTimeMillis()))

    /** Gentle streak: consecutive days with any mood check-in or joy, ending today or yesterday. */
    suspend fun glowDays(day: Long = today()): Int {
        val days = db.days().activeDays().toSet()
        var d = if (day in days) day else day - 1
        var n = 0
        while (d in days) { n++; d-- }
        return n
    }
}
