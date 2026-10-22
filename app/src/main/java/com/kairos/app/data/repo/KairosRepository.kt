package com.kairos.app.data.repo

import com.kairos.app.data.ai.AiError
import com.kairos.app.data.ai.Task
import com.kairos.app.data.ai.Writer
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
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
    private val writer: Writer,
) {
    private val dayLock = Mutex()
    private val genLock = Mutex()
    private val _status = MutableStateFlow<DayStatus>(DayStatus.Working)
    val status: StateFlow<DayStatus> = _status.asStateFlow()
    private val _generating = MutableStateFlow(false)
    val generating: StateFlow<Boolean> = _generating.asStateFlow()

    val hasKey get() = keys.hasKey
    val awake get() = keys.awake

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
            loved = (words.filter { it.engaged }.sortedByDescending { it.dwellMs + it.shares * 20_000L + (if (it.saved) 30_000L else 0L) }).take(12).map { it.word },
            notForMe = words.filter { it.taste < 0 }.takeLast(12).map { it.word },
            tooEasy = words.filter { it.difficultyVote < 0 || it.mastered }.takeLast(10).map { it.word },
            tooHard = words.filter { it.difficultyVote > 0 }.takeLast(10).map { it.word },
            struggling = words.filter { it.lapses >= 2 }.take(6).map { it.word },
            mastered = words.count { it.mastered },
            lovedAuthors = quotes.filter { (it.saved || it.taste > 0) && it.author.isNotBlank() }.map { it.author }.distinct().take(8),
            lovedThemes = quotes.filter { it.engaged }.groupingBy { it.theme }.eachCount().entries.sortedByDescending { it.value }.take(3).map { it.key },
            // The newest words are what the model is most likely to repeat; older ones are caught locally.
            avoidWords = words.sortedByDescending { it.generatedAt }.take(120).map { it.word },
            avoidQuotes = quotes.sortedByDescending { it.generatedAt }.take(60).map { it.text.take(60) },
            avoidSparks = db.extras().recentTexts("spark", 30),
            focus = focus,
        )
    }

    /**
     * Asks the curator for fresh words, thoughts and books, as separate small tasks that run in
     * parallel, and stores whatever survives validation. One failing task doesn't sink the others.
     */
    suspend fun generate(words: Int = 6, quotes: Int = 6, sparks: Int = 4, prompts: Int = 3, books: Int = 0, focus: String? = null): Batch = genLock.withLock {
        if (!keys.awake.value) throw AiError.NoKey()
        _generating.value = true
        try {
            val profile = profile(focus)
            val knownWords = db.words().allWords().map { it.lowercase() }.toSet()
            val knownQuotes = db.quotes().all().map { it.id }.toSet()
            val parts = coroutineScope {
                val w = if (words > 0) async { runCatching { writer.completeJson(Task.Words, Curator.wordsSystem, Curator.wordsMessage(profile, words), "kairos_words", Curator.wordsSchema, 400 + words * 260) } } else null
                val t = if (quotes + sparks + prompts > 0) async { runCatching { writer.completeJson(Task.Thoughts, Curator.thoughtsSystem, Curator.thoughtsMessage(profile, quotes, sparks, prompts), "kairos_thoughts", Curator.thoughtsSchema, 400 + quotes * 160 + (sparks + prompts) * 60, localUser = Curator.localThoughtsMessage(profile, quotes, sparks, prompts)) } } else null
                val b = if (books > 0) async {
                    val avoid = db.books().all().map { "${it.title} (${it.author})" }.takeLast(60)
                    runCatching { writer.completeJson(Task.Books, Curator.booksSystem, Curator.booksMessage(profile, books, avoid), "kairos_books", Curator.booksSchema, 400 + books * 380) }
                } else null
                listOfNotNull(w, t, b).map { it.await() }
            }
            val ok = parts.mapNotNull { it.getOrNull() }
            if (ok.isEmpty()) throw (parts.firstNotNullOfOrNull { it.exceptionOrNull() } as? AiError ?: AiError.Malformed())
            val merged = buildJsonObject { ok.forEach { o -> o.forEach { (k, v) -> put(k, v) } } }
            val batch = Curator.parse(merged, knownWords, knownQuotes)
            if (batch.words.isEmpty() && batch.quotes.isEmpty() && batch.books.isEmpty()) throw AiError.Malformed()
            db.words().insertAll(batch.words)
            db.quotes().insertAll(batch.quotes)
            db.books().insertAll(batch.books)
            db.extras().insertAll(batch.sparks.map { ExtraEntity(kind = "spark", text = it) } + batch.prompts.map { ExtraEntity(kind = "prompt", text = it) })
            batch
        } finally {
            _generating.value = false
        }
    }

    /**
     * Keeps the pool full and fresh. There is no cap on how much someone can read: whenever unseen
     * words, thoughts or books run low, another small batch is written. Unseen items older than two
     * weeks are retired, so what waits for you always reflects your latest taste.
     */
    suspend fun topUpPool(refresh: Boolean = false): Boolean {
        if (!keys.awake.value) return false
        if (refresh) {
            val before = System.currentTimeMillis() - 14L * 86_400_000L
            db.words().retireStale(before)
            db.quotes().retireStale(before)
        }
        val needWords = db.words().unseenCount() < 8
        val needQuotes = db.quotes().unseenCount() < 8
        val needSparks = db.extras().unusedCount("spark") < 3
        val needBooks = db.books().unseenCount() < 4
        if (!needWords && !needQuotes && !needSparks && !needBooks) return false
        generate(
            words = if (needWords) 8 else 0, quotes = if (needQuotes) 8 else if (needSparks) 2 else 0,
            sparks = if (needSparks) 4 else 0, prompts = if (needSparks) 3 else 0, books = if (needBooks) 4 else 0,
        )
        return true
    }

    /** "Show me words about…": a focused batch that lands straight in the library. */
    suspend fun explore(topic: String): List<WordEntity> {
        val batch = generate(words = 6, quotes = 0, sparks = 0, prompts = 0, focus = topic.trim().take(80))
        // The person has seen these now: they live in the library, not in Today's surprise pool.
        val day = today()
        batch.words.forEach { w -> db.words().get(w.id)?.let { db.words().update(it.copy(timesShown = 1, lastShownDay = day)) } }
        addMoment(MomentKind.Explored, topic, oncePerDay = false)
        return batch.words
    }

    /** Endless discovery: unseen words ranked by the recommender and diversified, generating more as it runs low. */
    suspend fun discoverWords(): List<com.kairos.app.engine.Pick<WordEntity>> {
        if (db.words().unseenCount() < 6 && keys.awake.value) runCatching { generate(words = 8, quotes = 0, sparks = 0, prompts = 0) }
        val ranked = DailyEngine.scoreWords(db.words().unseen(), taste(), today(), stream = 11)
        return DailyEngine.diversify(ranked) { it.item.category }
    }

    suspend fun discoverQuotes(): List<com.kairos.app.engine.Pick<QuoteEntity>> {
        if (db.quotes().unseenCount() < 6 && keys.awake.value) runCatching { generate(words = 0, quotes = 8, sparks = 0, prompts = 0) }
        val mood = Mood.of(db.moods().latestFor(today())?.mood)
        val ranked = DailyEngine.scoreQuotes(db.quotes().unseen(), taste(), today(), mood, DayPart.of(LocalTime.now().hour), shuffle = 9)
        return DailyEngine.diversify(ranked) { it.item.theme }
    }

    /** Marks a discovered word or thought as seen, so it moves from the pool into the library. */
    suspend fun markWordSeen(id: String) { db.words().get(id)?.takeIf { it.timesShown == 0 }?.let { db.words().update(it.copy(timesShown = 1, lastShownDay = today())) } }
    suspend fun markQuoteSeen(id: String) { db.quotes().get(id)?.takeIf { it.timesShown == 0 }?.let { db.quotes().update(it.copy(timesShown = 1, lastShownDay = today())) } }

    // --- Feed -----------------------------------------------------------------------------------

    /**
     * The next stretch of the Feed: words, thoughts, affirmations and book bites, ranked by the
     * recommender (which now also weighs dwell time and shares) and woven so the same kind never
     * repeats twice, with a breathing pause every so often. Pools that run low are refilled first.
     */
    suspend fun feedBatch(exclude: Set<String>, size: Int = 14, only: String? = null): List<FeedItem> {
        if (keys.awake.value && (db.words().unseenCount() < 8 || db.quotes().unseenCount() < 10)) {
            runCatching { generate(words = 6, quotes = 10, sparks = 0, prompts = 0, books = if (db.books().unseenCount() < 2) 3 else 0) }
        }
        val day = today()
        val t = taste()
        val mood = Mood.of(db.moods().latestFor(day)?.mood)
        val part = DayPart.of(LocalTime.now().hour)
        // Unseen first; when they run out, things met long enough ago come back.
        val words = (db.words().unseen().ifEmpty { db.words().all().filter { it.taste >= 0 } })
            .let { DailyEngine.scoreWords(it, t, day, stream = 21 + exclude.size) }.filter { "w:${it.item.id}" !in exclude }
        val allQuotes = (db.quotes().unseen().ifEmpty { db.quotes().all().filter { it.taste >= 0 } })
            .let { DailyEngine.scoreQuotes(it, t, day, mood, part, shuffle = 31 + exclude.size) }.filter { "q:${it.item.id}" !in exclude }
        val affirmations = allQuotes.filter { it.item.kind == "affirmation" }.toMutableList()
        val thoughts = allQuotes.filter { it.item.kind != "affirmation" }.toMutableList()
        val books = db.books().all().filter { it.taste >= 0 && "b:${it.id}" !in exclude }.sortedBy { it.dwellMs }.toMutableList()
        val wq = words.toMutableList()
        val pattern = if (only != null) listOf(only) else listOf("w", "q", "a", "w", "q", "b", "w", "a", "q", "p")
        val out = ArrayList<FeedItem>()
        var i = exclude.size
        var guard = 0
        while (out.size < size && guard++ < size * 3) {
            val kind = pattern[i++ % pattern.size]
            val item: FeedItem? = when (kind) {
                "w" -> wq.removeFirstOrNull()?.let { FeedItem.Word(it.item, it.reason) }
                "q" -> thoughts.removeFirstOrNull()?.let { FeedItem.Thought(it.item, it.reason) }
                "a" -> (affirmations.removeFirstOrNull() ?: thoughts.removeFirstOrNull())?.let { FeedItem.Thought(it.item, it.reason) }
                "b" -> books.removeFirstOrNull()?.let { FeedItem.Book(it) }
                else -> FeedItem.Pause("p:${exclude.size + out.size}")
            }
            if (item != null) out += item
            if (wq.isEmpty() && thoughts.isEmpty() && affirmations.isEmpty() && books.isEmpty()) break
        }
        return out
    }

    /** Time spent on a Feed page. Short glances don't count; long reads count a lot. */
    suspend fun recordDwell(item: FeedItem, ms: Long) {
        if (ms < 1200) return
        val capped = ms.coerceAtMost(60_000)
        when (item) {
            is FeedItem.Word -> db.words().addDwell(item.word.id, capped, today())
            is FeedItem.Thought -> db.quotes().addDwell(item.quote.id, capped, today())
            is FeedItem.Book -> db.books().addDwell(item.book.id, capped)
            is FeedItem.Pause -> {}
        }
    }

    suspend fun recordShare(item: FeedItem) {
        when (item) {
            is FeedItem.Word -> db.words().addShare(item.word.id)
            is FeedItem.Thought -> db.quotes().addShare(item.quote.id)
            else -> {}
        }
        addMoment(MomentKind.Shared, item.key, oncePerDay = false)
    }

    // --- Books ----------------------------------------------------------------------------------

    fun observeBooks() = db.books().observeAll()
    fun observeBook(id: String) = db.books().observe(id)

    suspend fun moreBooks(n: Int = 4): Int = generate(words = 0, quotes = 0, sparks = 0, prompts = 0, books = n).books.size

    suspend fun toggleBookSaved(id: String): Boolean {
        val b = db.books().get(id) ?: return false
        val saved = !b.saved
        db.books().update(b.copy(saved = saved, savedAt = if (saved) System.currentTimeMillis() else null))
        return saved
    }

    suspend fun markBookRead(id: String) { db.books().get(id)?.let { db.books().update(it.copy(timesShown = it.timesShown + 1, lastShownDay = today())) } }
    suspend fun setBookTaste(id: String, value: Int) { db.books().get(id)?.let { db.books().update(it.copy(taste = value)) } }

    // --- Today ----------------------------------------------------------------------------------

    private suspend fun taste(): Taste {
        val s = prefs.current()
        val salt = if (s.salt == 0L) prefs.ensureSalt() else s.salt
        val words = db.words().all()
        val quotes = db.quotes().all()
        return Taste(
            wordInterests = s.wordInterests, quoteThemes = s.quoteThemes, level = s.level, salt = salt,
            // Saving, liking, sharing and lingering all count as "more like this".
            savedWordCategories = words.filter { it.engaged }.groupingBy { it.category }.eachCount(),
            savedQuoteThemes = quotes.filter { it.engaged }.groupingBy { it.theme }.eachCount(),
            savedKinds = quotes.filter { it.engaged }.groupingBy { it.kind }.eachCount(),
            savedLanguages = words.filter { (it.saved || it.taste > 0) && it.language.isNotBlank() }.groupingBy { it.language }.eachCount(),
            lovedAuthors = quotes.filter { (it.saved || it.taste > 0) && it.author.isNotBlank() }.map { it.author }.toSet(),
            recentCategories = words.filter { it.lastShownDay != null }.sortedByDescending { it.lastShownDay }.take(5).map { it.category },
            recentThemes = quotes.filter { it.lastShownDay != null }.sortedByDescending { it.lastShownDay }.take(5).map { it.theme },
            recentKinds = quotes.filter { it.lastShownDay != null }.sortedByDescending { it.lastShownDay }.take(3).map { it.kind },
        )
    }

    /** Prepares today's picks, generating first if the pool is empty. Safe to call repeatedly. */
    suspend fun ensureDay(day: Long = today()): DayStatus = dayLock.withLock {
        if (db.days().get(day) != null) return@withLock DayStatus.Ready.also { _status.value = it }
        _status.value = DayStatus.Working
        try {
            if (db.words().unseenCount() == 0 || db.quotes().unseenCount() == 0) {
                if (!keys.awake.value && db.words().all().isEmpty()) return@withLock DayStatus.NeedsKey.also { _status.value = it }
                // A small first batch opens Today fast; the refill worker writes the rest in the background.
                if (keys.awake.value) runCatching { generate(words = 3, quotes = 3, sparks = 2, prompts = 1) }.onFailure { if (db.words().all().isEmpty()) throw it }
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
        if (db.quotes().unseenCount() == 0) runCatching { generate(words = 0, quotes = 6, sparks = 0, prompts = 0) }
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

    /**
     * Due words first, then words met but never practised. If nothing qualifies (everything is
     * scheduled for later), it still offers a free round from the soonest-due words, so practice is
     * never a dead end once you have words.
     */
    suspend fun practiceDeck(size: Int = 10): List<WordEntity> {
        val dao = db.words()
        val deck = dao.due(today()).take(size).toMutableList()
        if (deck.size < size) deck += dao.freshForPractice(size - deck.size)
        if (deck.size < 4) deck += dao.upcoming(size - deck.size)
        if (deck.isEmpty()) deck += dao.met(size)
        return deck.distinctBy { it.id }.take(size).shuffled()
    }

    /** True when this round is extra practice (nothing was actually due). */
    suspend fun nothingDue() = db.words().due(today()).isEmpty() && db.words().freshForPractice(1).isEmpty()

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

/** What the Feed shows: one page each. */
sealed interface FeedItem {
    val key: String
    data class Word(val word: WordEntity, val reason: String) : FeedItem { override val key get() = "w:${word.id}" }
    data class Thought(val quote: QuoteEntity, val reason: String) : FeedItem { override val key get() = "q:${quote.id}" }
    data class Book(val book: com.kairos.app.data.db.BookEntity) : FeedItem { override val key get() = "b:${book.id}" }
    data class Pause(override val key: String) : FeedItem
}
