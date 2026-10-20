package com.kairos.app.ui.studio

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.core.util.Clock
import com.kairos.app.core.util.KairosDates
import com.kairos.app.data.AppState
import com.kairos.app.data.KairosRepository
import com.kairos.app.data.catalog.Quote
import com.kairos.app.data.catalog.QuoteCatalog
import com.kairos.app.data.catalog.VocabCatalog
import com.kairos.app.data.catalog.Word
import com.kairos.app.domain.quote.QuoteEngine
import com.kairos.app.domain.share.PosterContent
import com.kairos.app.domain.share.PosterEngine
import com.kairos.app.domain.share.PosterPalette
import com.kairos.app.domain.share.PosterPalettes
import com.kairos.app.domain.share.PosterRatio
import com.kairos.app.domain.share.PosterStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class StudioSource(val label: String) { Word("a word"), Quote("a thought") }

data class StudioDesign(
    val style: PosterStyle = PosterStyle.Wash,
    val palette: PosterPalette = PosterPalettes.Apricot,
    val ratio: PosterRatio = PosterRatio.Portrait,
    val source: StudioSource = StudioSource.Word,
    val wordId: String? = null,
    val quoteId: String? = null,
)

data class StudioUi(
    val design: StudioDesign = StudioDesign(),
    val word: Word = VocabCatalog.all.first(),
    val quote: Quote = QuoteCatalog.all.first(),
    val content: PosterContent = PosterContent("", "", null, "", null, ""),
    val bitmap: Bitmap? = null,
    val rendering: Boolean = true,
    val wordOptions: List<Word> = emptyList(),
    val quoteOptions: List<Quote> = emptyList(),
)

class StudioViewModel(
    private val repository: KairosRepository,
    private val clock: Clock,
    private val appContext: Context,
) : ViewModel() {

    private val design = MutableStateFlow(StudioDesign())
    private val bitmap = MutableStateFlow<Bitmap?>(null)
    private val rendering = MutableStateFlow(true)
    private var renderJob: Job? = null

    private val selection: kotlinx.coroutines.flow.Flow<Picked> =
        combine(repository.state, design) { app: AppState, cfg: StudioDesign ->
            buildSelection(app, cfg)
        }

    private data class RenderState(val bitmap: Bitmap?, val rendering: Boolean)

    private val renderState = combine(bitmap, rendering) { bmp, isRendering ->
        RenderState(bmp, isRendering)
    }

    private val designAndPicked = combine(selection, design) { picked, cfg -> picked to cfg }

    val state: StateFlow<StudioUi> = combine(designAndPicked, renderState) { (picked, cfg), render ->
        StudioUi(
            design = cfg,
            word = picked.word,
            quote = picked.quote,
            content = content(picked.word, picked.quote, cfg.source),
            bitmap = render.bitmap,
            rendering = render.rendering,
            wordOptions = picked.words,
            quoteOptions = picked.quotes,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StudioUi(),
    )

    init {
        viewModelScope.launch {
            combine(selection, design) { picked, cfg -> Triple(picked.word, picked.quote, cfg) }
                .distinctUntilChanged()
                .collect { (word, quote, cfg) ->
                    scheduleRender(cfg, content(word, quote, cfg.source))
                }
        }
    }

    private data class Picked(
        val word: Word,
        val quote: Quote,
        val words: List<Word>,
        val quotes: List<Quote>,
    )

    private fun buildSelection(app: AppState, cfg: StudioDesign): Picked {
        val today = clock.today()
        val taste = QuoteEngine.taste(app.profile, app.quoteSignals)
        val defaultQuote = QuoteEngine.forDay(today, taste).quote
        val quote = QuoteCatalog.byId[cfg.quoteId] ?: defaultQuote
        val pool = VocabCatalog.forThemes(app.profile.interests)
        val defaultWord = pool.firstOrNull { app.memories[it.id]?.saved == true } ?: pool.first()
        val word = VocabCatalog.byId[cfg.wordId] ?: defaultWord
        return Picked(
            word = word,
            quote = quote,
            words = pool.take(40),
            quotes = QuoteCatalog.all.take(40),
        )
    }

    private fun content(word: Word, quote: Quote, source: StudioSource): PosterContent =
        if (source == StudioSource.Word) {
            PosterContent(
                kicker = word.partOfSpeech,
                title = word.term,
                phonetic = word.ipa,
                body = word.definition,
                note = word.example,
                signature = KairosDates.monthDayYear(clock.today()),
            )
        } else {
            PosterContent(
                kicker = "a thought on ${quote.theme}",
                title = quote.text,
                phonetic = null,
                body = if (quote.original) "Kairos" else quote.author,
                note = null,
                signature = KairosDates.monthDayYear(clock.today()),
            )
        }

    private fun scheduleRender(cfg: StudioDesign, content: PosterContent) {
        renderJob?.cancel()
        rendering.value = true
        renderJob = viewModelScope.launch {
            delay(120)
            val result = withContext(Dispatchers.Default) {
                PosterEngine.render(
                    context = appContext,
                    style = cfg.style,
                    palette = cfg.palette,
                    ratio = cfg.ratio,
                    content = content,
                    widthPx = 900,
                )
            }
            bitmap.value = result
            rendering.value = false
        }
    }

    fun setStyle(value: PosterStyle) { design.value = design.value.copy(style = value) }
    fun setPalette(value: PosterPalette) { design.value = design.value.copy(palette = value) }
    fun setRatio(value: PosterRatio) { design.value = design.value.copy(ratio = value) }
    fun setSource(value: StudioSource) { design.value = design.value.copy(source = value) }
    fun selectWord(id: String) { design.value = design.value.copy(wordId = id, source = StudioSource.Word) }
    fun selectQuote(id: String) { design.value = design.value.copy(quoteId = id, source = StudioSource.Quote) }

    fun shuffleWord() {
        val options = state.value.wordOptions
        if (options.isEmpty()) return
        val current = state.value.word.id
        val index = options.indexOfFirst { it.id == current }
        selectWord(options[(index + 1).coerceAtLeast(0) % options.size].id)
    }

    fun shuffleQuote() {
        val options = state.value.quoteOptions
        if (options.isEmpty()) return
        val current = state.value.quote.id
        val index = options.indexOfFirst { it.id == current }
        selectQuote(options[(index + 1).coerceAtLeast(0) % options.size].id)
    }

    fun suggestedName(): String = when (state.value.design.source) {
        StudioSource.Word -> "kairos-${state.value.word.term.lowercase()}"
        StudioSource.Quote -> "kairos-thought"
    }
}
