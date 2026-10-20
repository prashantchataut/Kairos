package com.kairos.app.ui.screens.capsule

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.repo.CapsuleDraft
import com.kairos.app.data.repo.DraftAttachment
import com.kairos.app.engine.Mood
import com.kairos.app.ui.theme.Wash
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

enum class OpenIn(val label: String, val months: Long) {
    Month("1 month", 1), Season("3 months", 3), HalfYear("6 months", 6), Year("1 year", 12), FiveYears("5 years", 60)
}

data class ComposeState(
    val title: String = "",
    val letter: String = "",
    val openDate: LocalDate = LocalDate.now().plusMonths(12),
    val preset: OpenIn? = OpenIn.Year,
    val mood: Mood? = null,
    val wash: Wash = Wash.Lilac,
    val attachments: List<DraftAttachment> = emptyList(),
    val sealing: Boolean = false,
    val sealed: Boolean = false,
    val prompt: String = "",
) {
    val canSeal get() = (letter.isNotBlank() || attachments.isNotEmpty()) && !sealing
    val dirty get() = letter.isNotBlank() || title.isNotBlank() || attachments.isNotEmpty()
}

class CapsuleComposeViewModel(private val c: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(ComposeState(prompt = c.kairos.capsulePrompt()))
    val state = _state.asStateFlow()

    fun setTitle(v: String) = _state.update { it.copy(title = v.take(60)) }
    fun setLetter(v: String) = _state.update { it.copy(letter = v) }
    fun setPreset(p: OpenIn) = _state.update { it.copy(preset = p, openDate = LocalDate.now().plusMonths(p.months)) }
    fun setDate(d: LocalDate) = _state.update { it.copy(preset = null, openDate = d) }
    fun setMood(m: Mood) = _state.update { it.copy(mood = m, wash = m.wash) }
    fun setWash(w: Wash) = _state.update { it.copy(wash = w) }
    fun usePrompt() = _state.update {
        val sep = if (it.letter.isBlank()) "" else "\n\n"
        it.copy(letter = it.letter + sep + it.prompt + "\n")
    }
    fun nextPrompt() = _state.update { it.copy(prompt = c.content.prompts.random()) }

    fun addUris(uris: List<Uri>) {
        val described = uris.mapNotNull { runCatching { c.capsules.describe(it) }.getOrNull() }
        _state.update { s -> s.copy(attachments = (s.attachments + described).distinctBy { it.uri }.take(12)) }
    }

    fun addVoice(uri: Uri, durationMs: Long, size: Long) {
        val secs = durationMs / 1000
        val a = DraftAttachment(uri, "Voice note ${secs / 60}:${"%02d".format(secs % 60)}.m4a", "audio/mp4", size)
        _state.update { it.copy(attachments = it.attachments + a) }
    }

    fun remove(a: DraftAttachment) = _state.update { s -> s.copy(attachments = s.attachments - a) }

    fun seal() {
        val s = _state.value
        if (!s.canSeal) return
        _state.update { it.copy(sealing = true) }
        // Letters open at 9 in the morning of the chosen day — a nice moment to read them.
        val opensAt = s.openDate.atTime(LocalTime.of(9, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModelScope.launch {
            c.capsules.seal(CapsuleDraft(s.title, s.letter, opensAt, s.mood?.name, s.wash, s.attachments))
            _state.update { it.copy(sealed = true) }
        }
    }
}
