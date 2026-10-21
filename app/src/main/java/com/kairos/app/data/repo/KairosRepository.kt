package com.kairos.app.data.repo

import com.kairos.app.data.ai.AiError
import com.kairos.app.data.ai.OpenRouter
import com.kairos.app.data.ai.SecureKeyStore
import com.kairos.app.data.db.DayEntity
import com.kairos.app.data.db.ExtraEntity
import com.kairos.app.data.db.KairosDatabase
import com.kairos.app.data.db.MomentEntity
import com.kairos.app.data.db.MoodEntity
import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import com.kairos.app.data.prefs.UserPrefs
import com.kairos.app.engine.Batch
import com.kairos.app.engine.Curator
import com.kairos.app.engine.DailyEngine
import com.kairos.app.engine.DayPart
import com.kairos.app.engine.Mood
import com.kairos.app.engine.Profile
import com.kairos.app.engine.Recall
import com.kairos.app.engine.Srs
import com.kairos.app.engine.SrsState
import com.kairos.app.engine.Taste
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
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
    val spark: String,
    val sparkDone: Boolean,
)

/** What happened the last time Kairos tried to prepare today. */
sealed interface DayStatus {
    data object Working : DayStatus
    data object Ready : DayStatus
    data object NeedsKey : DayStatus
    data class Failed(val message: String) : DayStatus
}

enum class MomentKind(val label: String) {
    Mood("Checked in"), Spark("Did a tiny good thing"), Practice("Practised words"),
    SavedWord("Kept a word"), SavedQuote("Kept a thought"), Letter("Wrote to future you"),
    Opened("Opened a letter"), Shared("Shared a card"), Explored("Explored a topic"),
}

fun today(): Long = LocalDate.now().toEpochDay()

class KairosRepository(
    private val db: KairosDatabase,
    private val prefs: UserPrefs,
    private val keys: SecureKeyStore,
    private val ai: OpenRouter,
) {
    private val dayLock = Mutex()
    private val genLock = Mutex()
    private val _status = MutableStateFlow<DayStatus>(DayStatus.Working)
    val status: StateFlow<DayStatus> = _status.asStateFlow()
    private val _generating = MutableStateFlow(false)
    val generating: StateFlow<Boolean> = _generating.asStateFlow()

    val hasKey get() = keys.hasKey

    // --- The curator loop -----------------------------------------------------------------------

    suspend fun profile(focus: String? = null): Profile {
        val s = prefs.current()
        val words = db.words().all()
        val quotes = db.quotes().all()
        return Profile(
            name = s.name,
            interests = s.wordInterests,
            themes = s.quoteThemes,
            level = s.level,
            recentMoods = db.moods().recent(7).mapNotNull { Mood.of(it.mood) },
            dayPart = DayPart.of(LocalTime.now().hour),
            loved = (words.filter { it.saved || it.taste > 0 }.sortedByDescending { it.savedAt ?: it.generatedAt }).take(12).map { it.word },
            notForMe = words.filter { it.taste < 0 }.takeLast(12).map { it.word },
            tooEasy = words.filter { it.difficultyVote < 0 || it.mastered }.takeLast(10).map { it.word },
            tooHard = words.filter { it.difficultyVote > 0 }.takeLast(10).map { it.word },
            struggling = words.filter { it.lapses >= 2 }.take(6).map { it.word },
            mastered = words.count { it.mastered },
            lovedAuthors = quotes.filter { it.saved || it.taste > 0 }.map { it.author }.distinct().take(8),
            lovedThemes = quotes.filter { it.saved || it.taste > 0 }.groupingBy { it.theme }.eachCount().entries.sortedByDescending { it.value }.take(3).map { it.key },
            avoidWords = words.map { it.word }.takeLast(300),
            avoidQuotes = quotes.sortedByDescending { it.generatedAt }.take(60).map { it.text.take(60) },
            avoidSparks = db.extras().recentTexts("spark", 30),
            focus = focus,
        )
    }

    /** Asks the curator for a fresh batch and stores whatever survives validation. */
    suspend fun generate(words: Int = 8, quotes: Int = 6, sparks: Int = 4, prompts: Int = 3, focus: String? = null): Batch = genLock.withLock {
        if (!keys.hasKey.value) throw AiError.NoKey()
        _generating.value = true
        try {
            val profile = profile(focus)
            val model = prefs.current().aiModel
            val raw = ai.completeJson(
                model = model,
                system = Curator.system,
                user = Curator.userMessage(profile, words, quotes, sparks, prompts),
                schemaName = "kairos_batch",
                schema = Curator.schema,
            )
            val batch = Curator.parse(
                raw,
                knownWords = db.words().allWords().map { it.lowercase() }.toSet(),
                knownQuoteIds = db.quotes().all().map { it.id }.toSet(),
            )
            if (batch.words.isEmpty() && batch.quotes.isEmpty() && words + quotes > 0) throw AiError.Malformed()
            db.words().insertAll(batch.words)
            db.quotes().insertAll(batch.quotes)
            db.extras().insertAll(batch.sparks.map { ExtraEntity(kind = "spark", text = it) } + batch.prompts.map { ExtraEntity(kind = "prompt", text = it) })
            batch
        } finally {
            _generating.value = false
        }
    }

    /** Keeps a few unseen items ready so Today opens instantly. Returns true if it generated. */
    suspend fun topUpPool(): Boolean {
        if (!keys.hasKey.value) return false
        val needWords = db.words().unseenCount() < 4
        val needQuotes = db.quotes().unseenCount() < 3
        val needSparks = db.extras().unusedCount("spark") < 2
        if (!needWords && !needQuotes && !needSparks) return false
        generate(words = if (needWords) 8 else 2, quotes = if (needQuotes) 6 else 1, sparks = 4, prompts = 3)
        return true
    }

    /** "Show me words about…": a focused batch that lands straight in the library. */
    suspend fun explore(topic: String): List<WordEntity> {
        val batch = generate(words = 6, quotes = 1, sparks = 0, prompts = 0, focus = topic.trim().take(80))
        // The person has seen these now: they live in the library, not in Today's surprise pool.
        val day = today()
        batch.words.forEach { w -> db.words().get(w.id)?.let { db.words().update(it.copy(timesShown = 1, lastShownDay = day)) } }
        addMoment(MomentKind.Explored, topic, oncePerDay = false)
        return batch.words
    }

    // --- Today ----------------------------------------------------------------------------------

    private suspend fun taste(): Taste {
        val s = prefs.current()
        val salt = if (s.salt == 0L) prefs.ensureSalt() else s.salt
        val words = db.words().all()
        val quotes = db.quotes().all()
        return Taste(
            wordInterests = s.wordInterests, quoteThemes = s.quoteThemes, level = s.level, salt = salt,
            savedWordCategories = words.filter { it.saved || it.taste > 0 }.groupingBy { it.category }.eachCount(),
            savedQuoteThemes = quotes.filter { it.saved || it.taste > 0 }.groupingBy { it.theme }.eachCount(),
        )
    }

    /** Prepares today's picks, generating first if the pool is empty. Safe to call repeatedly. */
    suspend fun ensureDay(day: Long = today()): DayStatus = dayLock.withLock {
        if (db.days().get(day) != null) return@withLock DayStatus.Ready.also { _status.value = it }
        _status.value = DayStatus.Working
        try {
            if (db.words().unseenCount() == 0 || db.quotes().unseenCount() == 0) {
                if (!keys.hasKey.value && db.words().all().isEmpty()) return@withLock DayStatus.NeedsKey.also { _status.value = it }
                if (keys.hasKey.value) runCatching { generate() }.onFailure { if (db.words().all().isEmpty()) throw it }
            }
            val words = db.words().all()
            val quotes = db.quotes().all()
            if (words.isEmpty() || quotes.isEmpty()) throw AiError.Malformed()
            val taste = taste()
            val yesterday = db.days().get(day - 1)
            val mood = Mood.of(db.moods().latestFor(day)?.mood)
            val word = DailyEngine.pickWord(words, taste, day, setOfNotNull(yesterday?.wordId))!!
            val quote = DailyEngine.pickQuote(quotes, taste, day, mood, DayPart.of(LocalTime.now().hour), setOfNotNull(yesterday?.quoteId))!!
            val spark = db.extras().nextUnused("spark")
            spark?.let { db.extras().markUsed(it.id, day) }
            db.words().update(word.item.copy(timesShown = word.item.timesShown + 1, lastShownDay = day))
            db.quotes().update(quote.item.copy(timesShown = quote.item.timesShown + 1, lastShownDay = day))
            db.days().upsert(
                DayEntity(day, word.item.id, quote.item.id, spark?.text.orEmpty(), wordReason = word.reason, quoteReason = quote.reason)
            )
            DayStatus.Ready.also { _status.value = it }
        } catch (e: AiError) {
            (if (e is AiError.NoKey) DayStatus.NeedsKey else DayStatus.Failed(e.message ?: "Something went wrong.")).also { _status.value = it }
        }
    }

    fun observeToday(day: Long): Flow<Today?> = db.days().observe(day).flatMapLatest { d ->
        // Emit null straight away while the day isn't prepared, so screens can show their state.
        if (d == null) return@flatMapLatest flowOf(null)
        combine(db.words().observe(d.wordId), db.quotes().observe(d.quoteId)) { w, q ->
            if (w == null || q == null) null else Today(day, w, d.wordReason, q, d.quoteReason, d.spark, d.sparkDone)
        }
    }

    suspend fun shuffleQuote(day: Long = today()) = dayLock.withLock {
        val d = db.days().get(day) ?: return@withLock
        if (db.quotes().unseenCount() == 0) runCatching { generate(words = 0, quotes = 5, sparks = 0, prompts = 0) }
        val mood = Mood.of(db.moods().latestFor(day)?.mood)
        val pick = DailyEngine.pickQuote(db.quotes().all(), taste(), day, mood, DayPart.of(LocalTime.now().hour), setOf(d.quoteId), d.quoteShuffles + 1) ?: return@withLock
        db.quotes().update(pick.item.copy(timesShown = pick.item.timesShown + 1, lastShownDay = day))
        db.days().upsert(d.copy(quoteId = pick.item.id, quoteReason = pick.reason, quoteShuffles = d.quoteShuffles + 1))
    }

    /** Swaps today's word, e.g. after "not for me" or "too easy". */
    suspend fun swapWord(day: Long = today()) = dayLock.withLock {
        val d = db.days().get(day) ?: return@withLock
        if (db.words().unseenCount() == 0) runCatching { generate(words = 6, quotes = 0, sparks = 0, prompts = 0) }
        val pick = DailyEngine.pickWord(db.words().all(), taste(), day, setOf(d.wordId)) ?: return@withLock
        db.words().update(pick.item.copy(timesShown = pick.item.timesShown + 1, lastShownDay = day))
        db.days().upsert(d.copy(wordId = pick.item.id, wordReason = pick.reason))
    }

    // --- Feedback signals -----------------------------------------------------------------------

    fun observeWords() = db.words().observeAll()
    fun observeWord(id: String) = db.words().observe(id)
    fun observeDueCount() = db.words().observeDueCount(today())
    fun observeUnseenWords() = db.words().observeUnseenCount()

    suspend fun toggleWordSaved(id: String): Boolean {
        val w = db.words().get(id) ?: return false
        val saved = !w.saved
        db.words().update(w.copy(saved = saved, savedAt = if (saved) System.currentTimeMillis() else null, dueDay = if (saved && w.dueDay == null) today() else w.dueDay))
        if (saved) addMoment(MomentKind.SavedWord, w.word, oncePerDay = false)
        return saved
    }

    /** +1 more like this, -1 not for me. */
    suspend fun setWordTaste(id: String, value: Int) {
        val w = db.words().get(id) ?: return
        db.words().update(w.copy(taste = value))
    }

    /** -1 too easy, +1 too hard. */
    suspend fun setWordDifficulty(id: String, value: Int) {
        val w = db.words().get(id) ?: return
        db.words().update(w.copy(difficultyVote = value))
        // Three votes the same way nudges the level the curator writes at.
        val votes = db.words().all().map { it.difficultyVote }.filter { it != 0 }.takeLast(5)
        if (votes.size >= 3 && votes.takeLast(3).all { it == value }) {
            val s = prefs.current()
            prefs.setInterests(s.wordInterests, s.quoteThemes, (s.level - value).coerceIn(1, 5))
        }
    }

    fun observeQuotes() = db.quotes().observeAll()
    fun observeQuote(id: String) = db.quotes().observe(id)

    suspend fun toggleQuoteSaved(id: String): Boolean {
        val q = db.quotes().get(id) ?: return false
        val saved = !q.saved
        db.quotes().update(q.copy(saved = saved, savedAt = if (saved) System.currentTimeMillis() else null))
        if (saved) addMoment(MomentKind.SavedQuote, q.author, oncePerDay = false)
        return saved
    }

    suspend fun setQuoteTaste(id: String, value: Int) {
        val q = db.quotes().get(id) ?: return
        db.quotes().update(q.copy(taste = value))
    }

    // --- Practice -------------------------------------------------------------------------------

    suspend fun practiceDeck(size: Int = 10): List<WordEntity> {
        val dao = db.words()
        val deck = dao.due(today()).take(size).toMutableList()
        if (deck.size < size) deck += dao.freshForPractice(size - deck.size)
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
        db.words().update(fresh.copy(reps = next.reps, ease = next.ease, intervalDays = next.intervalDays, lapses = next.lapses, dueDay = today() + next.intervalDays))
    }

    // --- Mood, sparks, moments ------------------------------------------------------------------

    fun observeMood(day: Long) = db.moods().observeLatestFor(day)
    fun observeMoods(from: Long, to: Long) = db.moods().observeRange(from, to)

    suspend fun logMood(mood: Mood) {
        db.moods().insert(MoodEntity(epochDay = today(), mood = mood.name, createdAt = System.currentTimeMillis()))
        addMoment(MomentKind.Mood, mood.label)
    }

    suspend fun completeSpark(day: Long = today()) {
        val d = db.days().get(day) ?: return
        if (d.sparkDone) return
        db.days().upsert(d.copy(sparkDone = true))
        addMoment(MomentKind.Spark, d.spark, oncePerDay = false)
    }

    fun observeMoments() = db.moments().observeAll()
    fun observeActiveDays(from: Long, to: Long) = db.moments().observeActiveDays(from, to)

    suspend fun addMoment(kind: MomentKind, label: String, oncePerDay: Boolean = true) {
        val day = today()
        if (oncePerDay && db.moments().countFor(day, kind.name) > 0) return
        db.moments().insert(MomentEntity(createdAt = System.currentTimeMillis(), epochDay = day, kind = kind.name, label = label))
    }

    /** A writing prompt for the capsule composer, generated with the rest of the batch. */
    suspend fun nextPrompt(): String? = db.extras().nextUnused("prompt")?.also { db.extras().markUsed(it.id, today()) }?.text
}
