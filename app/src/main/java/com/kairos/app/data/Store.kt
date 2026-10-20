package com.kairos.app.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Atomic, corruption-tolerant JSON persistence.
 *
 * Writes go to `state.json.tmp`, are fsynced, then renamed over `state.json`, so a crash
 * mid-write can never leave a half-document behind. A corrupt file is quarantined rather
 * than deleted — the user's letters are in there.
 */
class StateStore(private val root: File) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    val file: File get() = File(root, "state.json")

    fun load(): AppState {
        if (!file.exists()) return AppState()
        return runCatching {
            json.decodeFromString<AppState>(file.readText())
        }.getOrElse { error ->
            Log.e("KairosStore", "Could not read state; quarantining", error)
            runCatching {
                file.renameTo(File(root, "state.corrupt-${System.currentTimeMillis()}.json"))
            }
            AppState()
        }
    }

    fun save(state: AppState) {
        root.mkdirs()
        val tmp = File(root, "state.json.tmp")
        val bytes = json.encodeToString(AppState.serializer(), state).toByteArray()
        FileOutputStream(tmp).use { out ->
            out.write(bytes)
            out.flush()
            out.fd.sync()
        }
        runCatching {
            Files.move(
                tmp.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        }.onFailure {
            // Some Android filesystems refuse ATOMIC_MOVE across the same directory; fall back.
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    fun exportTo(target: File) {
        target.parentFile?.mkdirs()
        target.writeText(json.encodeToString(AppState.serializer(), load()))
    }
}

/**
 * Single source of truth. UI reads [state]; every mutation is a pure transform followed by a
 * coalesced write, so rapid taps (grading six cards) produce one disk write, not six.
 */
class KairosRepository(
    private val store: StateStore,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(store.load())
    val state: StateFlow<AppState> = _state

    private val writes = Channel<Unit>(Channel.CONFLATED)

    init {
        scope.launch {
            for (signal in writes) {
                delay(180)
                // Drain any extra signals that arrived during the debounce window.
                while (writes.tryReceive().isSuccess) Unit
                runCatching { store.save(_state.value) }
                    .onFailure { Log.e("KairosRepo", "save failed", it) }
            }
        }
    }

    fun update(transform: (AppState) -> AppState) {
        _state.update(transform)
        writes.trySend(Unit)
    }

    fun snapshot(): AppState = _state.value

    fun flush() {
        runCatching { store.save(_state.value) }
    }

    fun exportTo(file: File) = store.exportTo(file)

    // ---------------------------------------------------------------- profile

    fun updateProfile(transform: (Profile) -> Profile) = update { it.copy(profile = transform(it.profile)) }

    fun completeOnboarding(name: String, interests: Set<String>, themes: Set<String>, pace: Int, joinedOn: String) {
        updateProfile {
            it.copy(
                name = name.trim(),
                interests = interests,
                quoteThemes = themes,
                dailyPace = pace,
                onboarded = true,
                joinedOn = if (it.joinedOn.isBlank()) joinedOn else it.joinedOn,
            )
        }
    }

    // ---------------------------------------------------------------- vocabulary

    fun memory(wordId: String): WordMemory =
        _state.value.memories[wordId] ?: WordMemory(wordId = wordId)

    fun recordReview(wordId: String, memory: WordMemory, day: String) {
        update { state ->
            state.copy(
                memories = state.memories + (wordId to memory),
                days = state.days.touch(day) { it.copy(reviewed = it.reviewed + 1) },
            )
        }
    }

    fun toggleSaved(wordId: String) {
        update { state ->
            val current = state.memories[wordId] ?: WordMemory(wordId = wordId)
            state.copy(memories = state.memories + (wordId to current.copy(saved = !current.saved)))
        }
    }

    fun markMet(wordId: String, day: String) {
        update { state ->
            val current = state.memories[wordId] ?: WordMemory(wordId = wordId)
            state.copy(
                memories = state.memories + (wordId to current.copy(seen = current.seen + 1)),
                days = state.days.touch(day) { it.copy(wordsMet = it.wordsMet + 1) },
            )
        }
    }

    fun recordSession(reviewed: Int, correct: Int, durationMillis: Long) {
        update { state ->
            state.copy(
                sessionLog = (state.sessionLog + SessionRecord(
                    at = System.currentTimeMillis(),
                    reviewed = reviewed,
                    correct = correct,
                    durationMillis = durationMillis,
                )).takeLast(180),
            )
        }
    }

    // ---------------------------------------------------------------- quotes

    fun reactToQuote(quoteId: String, loved: Boolean, day: String) {
        update { state ->
            val signals = state.quoteSignals.filterNot { it.quoteId == quoteId } +
                QuoteSignal(quoteId, loved, System.currentTimeMillis())
            state.copy(
                quoteSignals = signals.takeLast(400),
                days = state.days.touch(day) { it.copy(quotesRead = it.quotesRead + 1) },
            )
        }
    }

    // ---------------------------------------------------------------- mood & days

    fun logMood(day: String, mood: String) {
        update { state ->
            state.copy(days = state.days.touch(day) { it.copy(mood = mood) })
        }
    }

    // ---------------------------------------------------------------- capsules

    fun sealCapsule(capsule: Capsule, day: String) {
        update { state ->
            state.copy(
                capsules = (state.capsules + capsule).sortedBy { it.unlockAt },
                days = state.days.touch(day) { it.copy(capsulesSealed = it.capsulesSealed + 1) },
            )
        }
    }

    fun openCapsule(id: String, at: Long) {
        update { state ->
            state.copy(
                capsules = state.capsules.map {
                    if (it.id == id && it.openedAt == null) it.copy(openedAt = at) else it
                },
            )
        }
    }

    fun deleteCapsule(id: String) {
        update { state -> state.copy(capsules = state.capsules.filterNot { it.id == id }) }
    }

    fun renameCapsule(id: String, title: String) {
        update { state ->
            state.copy(capsules = state.capsules.map { if (it.id == id) it.copy(title = title) else it })
        }
    }

    fun resetEverything() {
        _state.value = AppState()
        writes.trySend(Unit)
    }
}

private fun Map<String, DayLog>.touch(day: String, transform: (DayLog) -> DayLog): Map<String, DayLog> {
    val current = this[day] ?: DayLog(day = day)
    return this + (day to transform(current))
}
