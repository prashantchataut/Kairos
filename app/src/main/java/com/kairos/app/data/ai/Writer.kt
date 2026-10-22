package com.kairos.app.data.ai

import com.kairos.app.BuildConfig
import kotlinx.serialization.json.JsonObject

/** The kinds of writing Kai does. Each prefers the provider that does it best. */
enum class Task { Words, Thoughts, Books }

/** One place Kai can write from: a provider, a model and a client. */
class Provider(val id: String, val label: String, val model: String, val client: OpenRouter)

/**
 * Picks who writes. If the person added their own OpenRouter key, that comes first (they chose a
 * model on purpose). Otherwise Kai uses the app's built-in providers: Mercury 3 (Inception's
 * diffusion model, very fast) for words, and Gemini Flash for thoughts and books, where getting a
 * real quote or a real book exactly right matters more than speed. Each falls back to the other.
 */
class Writer(private val keys: SecureKeyStore, val local: LocalModel? = null, private val model: suspend () -> String) {
    private val mercury = BuiltInKeys.mercury.takeIf { it.isNotBlank() }?.let {
        Provider("mercury", "Mercury 3", "mercury-3", OpenRouter(null, "https://api.inceptionlabs.ai/v1", it, openRouter = false, reasoningEffort = "low", maxTokensField = "max_completion_tokens"))
    }
    private val gemini = BuiltInKeys.gemini.takeIf { it.isNotBlank() }?.let {
        Provider("gemini", "Gemini Flash", "gemini-3.8-flash", OpenRouter(null, "https://generativelanguage.googleapis.com/v1beta/openai", it, openRouter = false, reasoningEffort = "low"))
    }

    /** True when the app can write without the person bringing a key. */
    val hasBuiltIn get() = mercury != null || gemini != null

    private suspend fun chain(task: Task): List<Provider> {
        val own = keys.apiKey()?.let { Provider("openrouter", "OpenRouter", model(), OpenRouter(keys)) }
        val builtIn = when (task) {
            Task.Words -> listOfNotNull(mercury, gemini)
            Task.Thoughts, Task.Books -> listOfNotNull(gemini, mercury)
        }
        return listOfNotNull(own) + builtIn
    }

    /**
     * Writes with the first provider that answers; remembers the last error if all of them fail.
     * For thoughts, the on-device model is the last resort (and the only one when offline): given
     * [localUser], it writes original lines and affirmations, never attributed quotes.
     */
    suspend fun completeJson(task: Task, system: String, user: String, schemaName: String, schema: JsonObject, maxTokens: Int, localUser: String? = null): JsonObject {
        val providers = chain(task)
        val canLocal = task == Task.Thoughts && localUser != null && local != null && local.status.value == LocalStatus.Ready
        if (providers.isEmpty() && !canLocal) throw AiError.NoKey()
        var last: AiError = AiError.Malformed()
        for (p in providers) {
            try {
                return p.client.completeJson(p.model, system, user, schemaName, schema, temperature = 0.85, maxTokens = maxTokens)
            } catch (e: AiError) {
                last = e
                // The person's own key failing for account reasons should be shown, not hidden.
                if (p.id == "openrouter" && (e is AiError.BadKey || e is AiError.NoCredits) && !hasBuiltIn && !canLocal) throw e
            }
        }
        if (canLocal) return local!!.completeJson(LocalModel.system, localUser!!)
        throw last
    }
}

/**
 * The app's own keys, injected at build time from local.properties or CI secrets (never committed).
 * They are stored XOR-masked so they aren't sitting in the APK as plain text, but anything shipped
 * inside an app can be extracted by someone determined: keep spending limits on these keys, or put
 * a small proxy in front of them later.
 */
object BuiltInKeys {
    private fun unmask(hex: String): String {
        if (hex.isBlank()) return ""
        val mask = "kairos-the-right-moment".toByteArray()
        val bytes = hex.chunked(2).map { it.toInt(16).toByte() }
        return String(ByteArray(bytes.size) { (bytes[it].toInt() xor mask[it % mask.size].toInt()).toByte() })
    }
    val mercury: String by lazy { unmask(BuildConfig.MERCURY_KEY_MASKED) }
    val gemini: String by lazy { unmask(BuildConfig.GEMINI_KEY_MASKED) }
}
