package com.kairos.app.ui.screens.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.db.WordEntity
import com.kairos.app.data.repo.JoySource
import com.kairos.app.engine.Recall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CardMode { Flip, Quiz }

data class PracticeCard(val word: WordEntity, val mode: CardMode, val options: List<WordEntity> = emptyList())

data class PracticeState(
    val loading: Boolean = true,
    val cards: List<PracticeCard> = emptyList(),
    val index: Int = 0,
    val revealed: Boolean = false,
    val picked: String? = null,
    val results: Map<Recall, Int> = emptyMap(),
) {
    val current get() = cards.getOrNull(index)
    val done get() = !loading && index >= cards.size
    val progress get() = if (cards.isEmpty()) 0f else index.toFloat() / cards.size
}

class PracticeViewModel(private val c: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(PracticeState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = PracticeState()
        val deck = c.kairos.practiceDeck(10)
        // Words already reviewed at least once get a quiz now and then — recognition before recall.
        val cards = deck.mapIndexed { i, w ->
            if (w.reps >= 1 && i % 3 == 1) PracticeCard(w, CardMode.Quiz, c.kairos.quizOptions(w))
            else PracticeCard(w, CardMode.Flip)
        }
        _state.value = PracticeState(loading = false, cards = cards)
    }

    fun reveal() = _state.update { it.copy(revealed = true) }

    fun pick(optionId: String) {
        val s = _state.value
        val card = s.current ?: return
        if (s.picked != null) return
        _state.update { it.copy(picked = optionId, revealed = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(900)
            grade(if (optionId == card.word.id) Recall.Good else Recall.Again)
        }
    }

    fun grade(recall: Recall) {
        val s = _state.value
        val card = s.current ?: return
        viewModelScope.launch {
            c.kairos.grade(card.word, recall)
            val next = s.index + 1
            _state.update {
                it.copy(index = next, revealed = false, picked = null, results = it.results + (recall to (it.results[recall] ?: 0) + 1))
            }
            if (next >= s.cards.size) c.kairos.addJoy(JoySource.Practice, "${s.cards.size} words")
        }
    }
}
