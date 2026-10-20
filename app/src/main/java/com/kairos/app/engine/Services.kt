package com.kairos.app.engine

import com.kairos.app.data.db.CritterEntity
import com.kairos.app.data.db.CustomWordEntity
import com.kairos.app.data.db.DailyPickEntity
import com.kairos.app.data.db.DewEntity
import com.kairos.app.data.db.KairosDatabase
import com.kairos.app.data.db.KindnessEntity
import com.kairos.app.data.db.MoodEntity
import com.kairos.app.data.db.QuoteStateEntity
import com.kairos.app.data.db.WordProgressEntity
import com.kairos.app.data.prefs.SettingsStore
import com.kairos.app.engine.content.ContentRepository
import com.kairos.app.engine.content.Kindness
import com.kairos.app.engine.content.Quote
import com.kairos.app.engine.content.Word
import com.kairos.app.engine.content.toWord
import com.kairos.app.engine.critters.CritterGenetics
import com.kairos.app.engine.critters.DewReason
import com.kairos.app.engine.critters.Hatchery
import com.kairos.app.engine.daily.DailyEngine
import com.kairos.app.engine.daily.Mood
import com.kairos.app.engine.daily.Pick
import com.kairos.app.engine.daily.QuoteSignal
import com.kairos.app.engine.daily.WordSignal
import com.kairos.app.engine.learn.Exercise
import com.kairos.app.engine.learn.Grade
import com.kairos.app.engine.learn.LookupResult
import com.kairos.app.engine.learn.SessionBuilder
import com.kairos.app.engine.learn.Srs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.util.Locale
import kotlin.random.Random

fun todayEpoch(): Long = LocalDate.now().toEpochDay()

data class Today(
    val day: Long,
    val word: Pick<Word>,
    val quote: Pick<Quote>,
    val rerolls: Int,
) {
    val rerollsLeft get() = (MAX_REROLLS - rerolls).coerceAtLeast(0)

    companion object {
        const val MAX_REROLLS = 3
    }
}

/** Owns "today": picks once, freezes the choice, and lets mood gently retune the thought. */
class TodayService(
    private val content: ContentRepository,
    private val db: KairosDatabase,
    private val settings: SettingsStore,
) {
    private val lock = Mutex()

    suspend fun ensureToday(day: Long = todayEpoch()): Today = lock.withLock {
        db.daily().get(day)?.let { existing -> resolve(existing)?.let { return it } }
        val created = pick(day, reroll = 0, mood = db.moods().latest(day)?.mood?.let(Mood::of))
        db.daily().upsert(created)
        resolve(created)!!
    }

    fun observe(day: Long = todayEpoch()): Flow<Today?> = db.daily().observe(day).map { it?.let { e -> resolve(e) } }

    suspend fun reroll(word: Boolean): Today = lock.withLock {
        val day = todayEpoch()
        val current = db.daily().get(day) ?: pick(day, 0, null)
        if (current.rerolls >= Today.MAX_REROLLS) return@withLock resolve(current)!!
        val fresh = pick(day, current.rerolls + 1, db.moods().latest(day)?.mood?.let(Mood::of), avoid = setOf(current.wordId, current.quoteId))
        val next = if (word) {
            current.copy(wordId = fresh.wordId, wordReason = fresh.wordReason, rerolls = current.rerolls + 1)
        } else {
            current.copy(quoteId = fresh.quoteId, quoteReason = fresh.quoteReason, rerolls = current.rerolls + 1)
        }
        db.daily().upsert(next)
        resolve(next)!!
    }

    /** A low mood swaps in a gentler thought, unless the user already kept today's one. */
    suspend fun retuneForMood(mood: Mood) = lock.withLock {
        val day = todayEpoch()
        val current = db.daily().get(day) ?: return@withLock
        if (db.quotes().get(current.quoteId)?.saved == true) return@withLock
        val s = settings.current()
        val quotes = content.quotes()
        val signals = db.quotes().all().associate { it.quoteId to QuoteSignal(it.saved, it.timesShown) }
        val pick = DailyEngine.pickQuote(quotes, signals, s.quoteThemes, mood, day, s.salt, recentQuotes(day), reroll = 50 + mood.ordinal) ?: return@withLock
        db.daily().upsert(current.copy(quoteId = pick.item.id, quoteReason = pick.reason))
    }

    private suspend fun recentQuotes(day: Long) = db.daily().since(day - 90).filter { it.epochDay != day }.map { it.quoteId }.toSet()

    private suspend fun pick(day: Long, reroll: Int, mood: Mood?, avoid: Set<String> = emptySet()): DailyPickEntity {
        val s = settings.current()
        val salt = settings.ensureSalt()
        val words = content.words()
        val quotes = content.quotes()
        val recent = db.daily().since(day - 120).filter { it.epochDay != day }
        val wordSignals = db.words().all().associate { it.wordId to WordSignal(it.saved, it.timesSeen, it.inDeck) }
        val quoteSignals = db.quotes().all().associate { it.quoteId to QuoteSignal(it.saved, it.timesShown) }
        val w = DailyEngine.pickWord(words, wordSignals, s.wordInterests, s.level, day, salt, recent.map { it.wordId }.toSet() + avoid, reroll)
        val q = DailyEngine.pickQuote(quotes, quoteSignals, s.quoteThemes, mood, day, salt, recent.map { it.quoteId }.toSet() + avoid, reroll)
        return DailyPickEntity(
            epochDay = day,
            wordId = w?.item?.id ?: words.first().id,
            quoteId = q?.item?.id ?: quotes.first().id,
            wordReason = w?.reason ?: "",
            quoteReason = q?.reason ?: "",
            rerolls = reroll,
        )
    }

    private suspend fun resolve(e: DailyPickEntity): Today? {
        val word = content.words().firstOrNull { it.id == e.wordId } ?: return null
        val quote = content.quotes().firstOrNull { it.id == e.quoteId } ?: return null
        return Today(e.epochDay, Pick(word, e.wordReason), Pick(quote, e.quoteReason), e.rerolls)
    }
}

data class WordItem(val word: Word, val progress: WordProgressEntity?) {
    val saved get() = progress?.saved == true
    val inDeck get() = progress?.inDeck == true
    val stage get() = Srs.stage(progress)
}

class LearnService(
    private val content: ContentRepository,
    private val db: KairosDatabase,
    private val pond: PondService,
) {
    private val dao = db.words()

    fun observeWords(): Flow<List<WordItem>> = combine(
        flow { emit(content.words()) },
        dao.observeCustom(),
        dao.observeAll(),
    ) { catalog, custom, progress ->
        val p = progress.associateBy { it.wordId }
        (custom.map { it.toWord() } + catalog).map { WordItem(it, p[it.id]) }
    }

    fun observeWord(id: String): Flow<WordItem?> = combine(observeWords(), dao.observe(id)) { all, _ -> all.firstOrNull { it.word.id == id } }

    private suspend fun progress(id: String) = dao.get(id) ?: WordProgressEntity(wordId = id)

    suspend fun toggleSave(id: String): Boolean {
        val p = progress(id)
        val saved = !p.saved
        dao.upsert(p.copy(saved = saved, inDeck = p.inDeck || saved, dueEpochDay = if (!p.inDeck && saved) todayEpoch() else p.dueEpochDay))
        if (saved) pond.award(DewReason.Saved)
        return saved
    }

    suspend fun addToDeck(id: String) {
        val p = progress(id)
        if (!p.inDeck) dao.upsert(p.copy(inDeck = true, dueEpochDay = todayEpoch()))
    }

    suspend fun markSeen(id: String) {
        val p = progress(id)
        dao.upsert(p.copy(timesSeen = p.timesSeen + 1))
    }

    suspend fun meetTodayWord(id: String) {
        addToDeck(id)
        pond.award(DewReason.WordMet)
    }

    suspend fun review(id: String, grade: Grade) {
        dao.upsert(Srs.review(progress(id), grade, todayEpoch(), System.currentTimeMillis()))
        pond.award(DewReason.Review)
    }

    suspend fun allWords(): List<Word> = dao.custom().map { it.toWord() } + content.words()

    suspend fun dueCount(): Int {
        val today = todayEpoch()
        return dao.all().count { Srs.isDue(it, today) }
    }

    fun observeDueCount(): Flow<Int> = dao.observeAll().map { all -> val t = todayEpoch(); all.count { Srs.isDue(it, t) } }

    suspend fun buildSession(size: Int = 8, focus: List<String>? = null): List<Exercise> {
        val words = allWords()
        val byId = words.associateBy { it.id }
        val progress = dao.all().associateBy { it.wordId }
        val today = todayEpoch()
        val due = if (focus != null) focus.mapNotNull { byId[it] } else progress.values.filter { Srs.isDue(it, today) }.mapNotNull { byId[it.wordId] }
        val fresh = if (focus != null) emptyList() else {
            val deckFresh = progress.values.filter { it.inDeck && it.reps == 0 }.mapNotNull { byId[it.wordId] }
            val newOnes = words.filter { !it.custom && progress[it.id]?.inDeck != true }.shuffled().take(4)
            (deckFresh + newOnes).distinctBy { it.id }
        }
        return SessionBuilder.build(due, fresh, words.filter { !it.custom }, progress, size, Random(System.nanoTime()))
    }

    suspend fun finishSession(correct: Int, total: Int) {
        if (total > 0) pond.award(DewReason.Session)
    }

    suspend fun adopt(result: LookupResult): String {
        val id = "u:" + result.word.lowercase(Locale.ROOT)
        dao.upsertCustom(
            CustomWordEntity(id, result.word, result.phonetic, result.partOfSpeech, result.definition, result.example, System.currentTimeMillis()),
        )
        val p = progress(id)
        dao.upsert(p.copy(saved = true, inDeck = true, dueEpochDay = todayEpoch()))
        return id
    }

    suspend fun removeCustom(id: String) = dao.deleteCustom(id)
}

data class QuoteItem(val quote: Quote, val saved: Boolean)

class QuoteService(private val content: ContentRepository, private val db: KairosDatabase, private val pond: PondService) {
    fun observe(): Flow<List<QuoteItem>> = combine(flow { emit(content.quotes()) }, db.quotes().observeAll()) { quotes, states ->
        val s = states.associateBy { it.quoteId }
        quotes.map { QuoteItem(it, s[it.id]?.saved == true) }
    }

    suspend fun toggleSave(id: String): Boolean {
        val s = db.quotes().get(id) ?: QuoteStateEntity(id)
        db.quotes().upsert(s.copy(saved = !s.saved))
        if (!s.saved) pond.award(DewReason.Saved)
        return !s.saved
    }

    suspend fun markShown(id: String, day: Long) {
        val s = db.quotes().get(id) ?: QuoteStateEntity(id)
        if (s.lastShownDay != day) db.quotes().upsert(s.copy(timesShown = s.timesShown + 1, lastShownDay = day))
    }
}

data class Critter(val entity: CritterEntity) {
    val genome = CritterGenetics.genome(entity.seed)
}

data class PondState(val dew: Int, val critters: List<Critter>) {
    val eggsReady get() = Hatchery.eggsReady(dew, critters.size)
    val progress get() = Hatchery.progressToNext(dew)
    val buddy get() = critters.firstOrNull { it.entity.isBuddy } ?: critters.firstOrNull()
}

/** Dew in, critters out. Every award is capped per day so the pond rewards care, not grinding. */
class PondService(private val db: KairosDatabase) {
    private val dao = db.critters()
    private val lock = Mutex()

    fun observe(): Flow<PondState> = combine(dao.observeDew(), dao.observeAll()) { dew, critters -> PondState(dew, critters.map(::Critter)) }

    suspend fun award(reason: DewReason, amount: Int = reason.amount): Int = lock.withLock {
        val day = todayEpoch()
        val already = dao.dewFor(day, reason.key)
        val grant = amount.coerceAtMost(reason.dailyCap - already).coerceAtLeast(0)
        if (grant > 0) dao.addDew(DewEntity(amount = grant, reason = reason.key, at = System.currentTimeMillis(), epochDay = day))
        grant
    }

    suspend fun hatch(origin: String = "egg"): Critter? = lock.withLock {
        val count = dao.count()
        if (origin != "first" && Hatchery.eggsReady(dao.dew(), count) <= 0) return@withLock null
        if (origin == "first" && count > 0) return@withLock null
        val seed = Random.nextLong()
        val entity = CritterEntity(
            id = "c$seed", seed = seed, name = CritterGenetics.name(seed), hatchedAt = System.currentTimeMillis(),
            isBuddy = count == 0, origin = origin,
        )
        dao.insert(entity)
        Critter(entity)
    }

    suspend fun setBuddy(id: String) = dao.setBuddy(id)
    suspend fun rename(id: String, name: String) = dao.rename(id, name.trim().take(18))
    fun observeRecentDew(days: Int = 7) = dao.observeDewSince(todayEpoch() - days)
}

class MoodService(private val db: KairosDatabase, private val today: TodayService, private val pond: PondService) {
    fun observeToday(): Flow<Mood?> = db.moods().observeLatest(todayEpoch()).map { it?.mood?.let(Mood::of) }
    fun observeWeek(): Flow<List<MoodEntity>> = db.moods().observeSince(todayEpoch() - 6)
    fun observeSince(days: Int): Flow<List<MoodEntity>> = db.moods().observeSince(todayEpoch() - days)

    suspend fun checkIn(mood: Mood, note: String = "") {
        db.moods().insert(MoodEntity(epochDay = todayEpoch(), at = System.currentTimeMillis(), mood = mood.ordinal, note = note))
        pond.award(DewReason.Checkin)
        if (mood.isLow) today.retuneForMood(mood)
    }
}

class KindnessService(private val content: ContentRepository, private val db: KairosDatabase, private val pond: PondService) {
    /** Today's deck: a deterministic shuffle so the cards don't reshuffle every launch. */
    suspend fun deck(day: Long = todayEpoch(), size: Int = 7): List<Kindness> =
        content.kindness().shuffled(Random(day * 31 + 7)).take(size)

    fun observeDoneToday(): Flow<Set<String>> = db.kindness().observeSince(todayEpoch()).map { list -> list.map { it.activityId }.toSet() }
    fun observeTotal(): Flow<Int> = db.kindness().observeCount()

    suspend fun done(id: String) {
        db.kindness().insert(KindnessEntity(activityId = id, epochDay = todayEpoch(), at = System.currentTimeMillis()))
        pond.award(DewReason.Kindness)
    }
}
