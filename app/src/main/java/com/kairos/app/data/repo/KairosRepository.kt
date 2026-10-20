package com.kairos.app.data.repo

import com.kairos.app.data.content.ContentLoader
import com.kairos.app.data.content.Spark
import com.kairos.app.data.db.DayEntity
import com.kairos.app.data.db.JoyDropEntity
import com.kairos.app.data.db.KairosDatabase
import com.kairos.app.data.db.MoodEntity
import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import com.kairos.app.data.prefs.UserPrefs
import com.kairos.app.engine.DailyEngine
import com.kairos.app.engine.DayPart
import com.kairos.app.engine.Mood
import com.kairos.app.engine.Recall
import com.kairos.app.engine.Srs
import com.kairos.app.engine.SrsState
import com.kairos.app.engine.Taste
import com.kairos.app.ui.theme.Wash
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.LocalTime

data class Today(
    val day: Long,
    val word: WordEntity,
    val wordReason: String,
    val quote: QuoteEntity,
    val quoteReason: String,
    val spark: Spark?,
    val sparkDone: Boolean,
)

/** Where the person's good moments get recorded. */
enum class JoySource(val label: String, val wash: Wash) {
    Mood("Checked in", Wash.Cerulean),
    Spark("Did a tiny good thing", Wash.Butter),
    Practice("Practised words", Wash.Mint),
    SavedWord("Kept a word", Wash.Lilac),
    SavedQuote("Kept a thought", Wash.Peach),
    Capsule("Wrote to future self", Wash.Rose),
    Opened("Opened a letter", Wash.Rose),
    Shared("Shared something lovely", Wash.Cerulean),
}

fun today(): Long = LocalDate.now().toEpochDay()

class KairosRepository(
    private val db: KairosDatabase,
    private val prefs: UserPrefs,
    private val content: ContentLoader,
) {
    private val dayLock = Mutex()

    /** Inserts bundled content that isn't in the database yet. Safe to call on every launch. */
    suspend fun syncContent() {
        val settings = prefs.current()
        if (settings.contentVersion >= ContentLoader.VERSION && db.words().count() > 0) return
        db.words().insertAll(content.words().map { it.toEntity() })
        db.quotes().insertAll(content.quotes().map { it.toEntity() })
        prefs.setContentVersion(ContentLoader.VERSION)
    }

    private suspend fun taste(): Taste {
        val s = prefs.current()
        val salt = if (s.salt == 0L) prefs.ensureSalt() else s.salt
        val words = db.words().all()
        val quotes = db.quotes().all()
        return Taste(
            wordInterests = s.wordInterests,
            quoteThemes = s.quoteThemes,
            level = s.level,
            salt = salt,
            savedWordCategories = words.filter { it.saved }.groupingBy { it.category }.eachCount(),
            savedQuoteThemes = quotes.filter { it.saved }.groupingBy { it.theme }.eachCount(),
        )
    }

    /** Returns today's picks, choosing them the first time the day is requested. */
    suspend fun ensureDay(day: Long = today()): DayEntity = dayLock.withLock {
        db.days().get(day)?.let { return it }
        syncContent()
        val taste = taste()
        val words = db.words().all()
        val quotes = db.quotes().all()
        val yesterday = db.days().get(day - 1)
        val mood = Mood.of(db.moods().latestFor(day)?.mood)
        val word = DailyEngine.pickWord(words, taste, day, setOfNotNull(yesterday?.wordId))!!
        val quote = DailyEngine.pickQuote(quotes, taste, day, mood, DayPart.of(LocalTime.now().hour), setOfNotNull(yesterday?.quoteId))!!
        val spark = DailyEngine.pickSpark(content.sparks, day, taste.salt)
        db.words().update(word.item.copy(timesShown = word.item.timesShown + 1, lastShownDay = day))
        db.quotes().update(quote.item.copy(timesShown = quote.item.timesShown + 1, lastShownDay = day))
        val entity = DayEntity(
            epochDay = day, wordId = word.item.id, quoteId = quote.item.id, sparkId = spark?.id ?: "",
            wordReason = word.reason, quoteReason = quote.reason,
        )
        db.days().upsert(entity)
        entity
    }

    fun observeToday(day: Long): Flow<Today?> = db.days().observe(day).filterNotNull().flatMapLatest { d ->
        combine(db.words().observe(d.wordId), db.quotes().observe(d.quoteId)) { w, q ->
            if (w == null || q == null) null
            else Today(day, w, d.wordReason, q, d.quoteReason, content.sparks.firstOrNull { it.id == d.sparkId }, d.sparkDone)
        }
    }

    /** "Another thought": picks a different quote for today, steered by the current mood. */
    suspend fun shuffleQuote(day: Long = today()) = dayLock.withLock {
        val d = db.days().get(day) ?: return@withLock
        val mood = Mood.of(db.moods().latestFor(day)?.mood)
        val pick = DailyEngine.pickQuote(
            db.quotes().all(), taste(), day, mood, DayPart.of(LocalTime.now().hour),
            exclude = setOf(d.quoteId), shuffle = d.quoteShuffles + 1,
        ) ?: return@withLock
        db.quotes().update(pick.item.copy(timesShown = pick.item.timesShown + 1, lastShownDay = day))
        db.days().upsert(d.copy(quoteId = pick.item.id, quoteReason = pick.reason, quoteShuffles = d.quoteShuffles + 1))
    }

    // --- Words ---------------------------------------------------------------------------------

    fun observeWords() = db.words().observeAll()
    fun observeWord(id: String) = db.words().observe(id)
    fun observeDueCount() = db.words().observeDueCount(today())

    suspend fun toggleWordSaved(id: String): Boolean {
        val w = db.words().get(id) ?: return false
        val saved = !w.saved
        // Saving also enrols the word in practice so it actually gets remembered.
        db.words().update(
            w.copy(saved = saved, savedAt = if (saved) System.currentTimeMillis() else null,
                dueDay = if (saved && w.dueDay == null) today() else w.dueDay)
        )
        if (saved) addJoy(JoySource.SavedWord, w.word, oncePerDay = false)
        return saved
    }

    /** Builds a practice deck: everything due, topped up with saved/seen words, then brand-new ones. */
    suspend fun practiceDeck(size: Int = 10): List<WordEntity> {
        val dao = db.words()
        val due = dao.due(today())
        val deck = due.take(size).toMutableList()
        if (deck.size < size) deck += dao.freshForPractice(size - deck.size)
        if (deck.size < size) deck += dao.unseen(size - deck.size)
        return deck.distinctBy { it.id }.shuffled()
    }

    suspend fun quizOptions(word: WordEntity): List<WordEntity> {
        val sameKind = db.words().distractors(word.partOfSpeech, word.id, 3)
        val extra = if (sameKind.size < 3) db.words().anyDistractors(word.id, 3 - sameKind.size) else emptyList()
        return (sameKind + extra + word).distinctBy { it.id }.shuffled()
    }

    suspend fun grade(word: WordEntity, recall: Recall) {
        val fresh = db.words().get(word.id) ?: return
        val next = Srs.review(SrsState(fresh.reps, fresh.ease, fresh.intervalDays, fresh.lapses), recall)
        db.words().update(
            fresh.copy(
                reps = next.reps, ease = next.ease, intervalDays = next.intervalDays, lapses = next.lapses,
                dueDay = today() + next.intervalDays,
                timesShown = fresh.timesShown + if (fresh.timesShown == 0) 1 else 0,
            )
        )
    }

    // --- Quotes --------------------------------------------------------------------------------

    fun observeQuotes() = db.quotes().observeAll()
    fun observeQuote(id: String) = db.quotes().observe(id)

    suspend fun toggleQuoteSaved(id: String): Boolean {
        val q = db.quotes().get(id) ?: return false
        val saved = !q.saved
        db.quotes().setSaved(id, saved, if (saved) System.currentTimeMillis() else null)
        if (saved) addJoy(JoySource.SavedQuote, q.author, oncePerDay = false)
        return saved
    }

    // --- Mood, sparks, joy ---------------------------------------------------------------------

    fun observeMood(day: Long) = db.moods().observeLatestFor(day)
    fun observeMoods(from: Long, to: Long) = db.moods().observeRange(from, to)

    suspend fun logMood(mood: Mood, note: String = "") {
        val day = today()
        db.moods().insert(MoodEntity(epochDay = day, mood = mood.name, note = note, createdAt = System.currentTimeMillis()))
        addJoy(JoySource.Mood, mood.label, wash = mood.wash)
    }

    suspend fun completeSpark(day: Long = today()) {
        val d = db.days().get(day) ?: return
        if (d.sparkDone) return
        db.days().upsert(d.copy(sparkDone = true))
        addJoy(JoySource.Spark, content.sparks.firstOrNull { it.id == d.sparkId }?.text ?: "", oncePerDay = false)
    }

    fun observeJoy() = db.joy().observeAll()
    fun observeDaysShownUp() = db.joy().observeDaysShownUp()
    fun observeActiveDays(from: Long, to: Long) = db.joy().observeActiveDays(from, to)

    suspend fun addJoy(source: JoySource, label: String, oncePerDay: Boolean = true, wash: Wash = source.wash) {
        val day = today()
        if (oncePerDay && db.joy().countFor(day, source.name) > 0) return
        db.joy().insert(
            JoyDropEntity(createdAt = System.currentTimeMillis(), epochDay = day, source = source.name, wash = wash.name, label = label)
        )
    }

    fun capsulePrompt(day: Long = today()): String =
        content.prompts.let { if (it.isEmpty()) "What do you hope is true when you read this?" else it[(day % it.size).toInt()] }

    companion object {
        fun empty(): Flow<Today?> = flowOf(null)
    }
}
