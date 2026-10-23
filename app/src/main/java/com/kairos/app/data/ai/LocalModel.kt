package com.kairos.app.data.ai

import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject

/** Whether the on-device model can be used on this phone. */
enum class LocalStatus { Checking, Unavailable, Downloadable, Downloading, Ready }

/**
 * Gemini Nano, running on the phone through ML Kit's GenAI Prompt API (supported Pixel, Samsung and
 * other recent devices; nothing to download on most, a one-time system download on some). It is Kai's
 * offline fallback: it writes original lines, affirmations, sparks and prompts, which need taste rather
 * than facts. Real quotes, books and etymologies stay with the cloud models, because a small model
 * misremembers them.
 */
class LocalModel {
    private val model: GenerativeModel? = runCatching { Generation.getClient() }.getOrNull()
    private val _status = MutableStateFlow(LocalStatus.Checking)
    val status: StateFlow<LocalStatus> = _status.asStateFlow()
    private val _progress = MutableStateFlow(0L)
    val downloadedBytes: StateFlow<Long> = _progress.asStateFlow()

    suspend fun refresh(): LocalStatus {
        val m = model ?: return LocalStatus.Unavailable.also { _status.value = it }
        val s = runCatching {
            when (m.checkStatus()) {
                FeatureStatus.AVAILABLE -> LocalStatus.Ready
                FeatureStatus.DOWNLOADABLE -> LocalStatus.Downloadable
                FeatureStatus.DOWNLOADING -> LocalStatus.Downloading
                else -> LocalStatus.Unavailable
            }
        }.getOrDefault(LocalStatus.Unavailable)
        _status.value = s
        if (s == LocalStatus.Ready) runCatching { m.warmup() }
        return s
    }

    /** Asks the system to fetch the model; status moves to Ready when it's done. */
    suspend fun download() {
        val m = model ?: return
        _status.value = LocalStatus.Downloading
        runCatching {
            m.download().collect { st ->
                when (st) {
                    is DownloadStatus.DownloadProgress -> _progress.value = st.totalBytesDownloaded
                    DownloadStatus.DownloadCompleted -> _status.value = LocalStatus.Ready
                    is DownloadStatus.DownloadFailed -> _status.value = LocalStatus.Downloadable
                    else -> {}
                }
            }
        }.onFailure { _status.value = LocalStatus.Downloadable }
    }

    /** One prompt, JSON out. The prompt must stay small: Nano accepts under 4000 tokens. */
    suspend fun completeJson(system: String, user: String): JsonObject {
        val m = model ?: throw AiError.Model("On-device model isn't available on this phone.")
        if (_status.value != LocalStatus.Ready && refresh() != LocalStatus.Ready) throw AiError.Model("On-device model isn't ready.")
        val prompt = "$system\n\n$user\n\nAnswer with one JSON object only, no prose."
        val res = runCatching {
            m.generateContent(generateContentRequest(TextPart(prompt)) { temperature = 0.8f; maxOutputTokens = 900 })
        }.getOrElse { throw AiError.Model(it.message ?: "on-device error") }
        val text = res.candidates.firstOrNull()?.text ?: throw AiError.Malformed()
        return OpenRouter(null, keyOverride = "-").parseLenient(text) ?: throw AiError.Malformed()
    }

    companion object {
        /** A compact brief for Nano: only things a small model can write well. */
        val system = """
            You are Kai, a warm, modern friend inside a small daily app. Write short original lines that
            make one person feel seen, lighter or braver: comfort, rest, self-kindness, quiet hope, humour.
            Plain words, concrete images, no clichés, no exclamation marks, no emojis.
            Never write quotes attributed to real people.
            JSON shape: {"quotes":[{"text":"","author":"","kind":"line or affirmation","source":"","theme":"","note":""}],"sparks":[""],"prompts":[""]}
        """.trimIndent()
    }
}
