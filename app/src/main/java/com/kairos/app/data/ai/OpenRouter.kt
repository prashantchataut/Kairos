package com.kairos.app.data.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Why a generation call failed, in words the UI can show as-is. */
sealed class AiError(message: String) : Exception(message) {
    class NoKey : AiError("Add your OpenRouter key in Settings to start.")
    class BadKey : AiError("OpenRouter didn't accept that key. Check it in Settings.")
    class NoCredits : AiError("Your OpenRouter account is out of credits.")
    class RateLimited : AiError("OpenRouter is busy right now. Kairos will try again shortly.")
    class Offline : AiError("You're offline. Kairos will catch up when you're back.")
    class Model(detail: String) : AiError("The model couldn't answer: $detail")
    class Malformed : AiError("The model's answer didn't make sense. Trying again usually helps.")
}

/** Minimal OpenRouter chat-completions client with structured (JSON schema) output. */
class OpenRouter(
    private val keys: SecureKeyStore?,
    private val baseUrl: String = "https://openrouter.ai/api/v1",
    private val keyOverride: String? = null,
) {
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaJson = "application/json".toMediaType()

    private fun key(): String = keyOverride ?: keys?.apiKey() ?: throw AiError.NoKey()

    private fun Request.Builder.common(key: String) = this
        .header("Authorization", "Bearer $key")
        .header("HTTP-Referer", "https://github.com/prashantchataut/Kairos")
        .header("X-Title", "Kairos")

    /** Checks a key without spending credits. Returns null when valid, or the reason it isn't. */
    suspend fun validate(candidate: String): AiError? = withContext(Dispatchers.IO) {
        val req = Request.Builder().url("$baseUrl/key").common(candidate.trim()).get().build()
        try {
            http.newCall(req).execute().use { r ->
                when (r.code) {
                    in 200..299 -> null
                    401, 403 -> AiError.BadKey()
                    402 -> AiError.NoCredits()
                    429 -> AiError.RateLimited()
                    else -> AiError.Model("HTTP ${r.code}")
                }
            }
        } catch (_: IOException) {
            AiError.Offline()
        }
    }

    /**
     * Sends one system + user turn and returns the parsed JSON object the model produced.
     * [schema] is a JSON Schema for the reply; models that ignore it still get JSON mode.
     */
    suspend fun completeJson(
        model: String,
        system: String,
        user: String,
        schemaName: String,
        schema: JsonObject,
        temperature: Double = 0.9,
    ): JsonObject = withContext(Dispatchers.IO) {
        val body = buildJsonObject {
            put("model", model)
            put("temperature", temperature)
            put("max_tokens", 6000)
            put("messages", buildJsonArray {
                add(buildJsonObject { put("role", "system"); put("content", system) })
                add(buildJsonObject { put("role", "user"); put("content", user) })
            })
            put("response_format", buildJsonObject {
                put("type", "json_schema")
                put("json_schema", buildJsonObject {
                    put("name", schemaName)
                    put("strict", true)
                    put("schema", schema)
                })
            })
            put("plugins", buildJsonArray { add(buildJsonObject { put("id", "response-healing") }) })
        }
        val req = Request.Builder()
            .url("$baseUrl/chat/completions")
            .common(key())
            .post(body.toString().toRequestBody(mediaJson))
            .build()
        val text = try {
            http.newCall(req).execute().use { r ->
                val raw = r.body?.string().orEmpty()
                when (r.code) {
                    in 200..299 -> raw
                    401, 403 -> throw AiError.BadKey()
                    402 -> throw AiError.NoCredits()
                    429 -> throw AiError.RateLimited()
                    else -> throw AiError.Model(errorMessage(raw) ?: "HTTP ${r.code}")
                }
            }
        } catch (e: IOException) {
            throw AiError.Offline()
        }
        val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrElse { throw AiError.Malformed() }
        root["error"]?.let { throw AiError.Model(it.jsonObject["message"]?.jsonPrimitive?.contentOrNull ?: "unknown error") }
        val choice = root["choices"]?.jsonArray?.firstOrNull()?.jsonObject ?: throw AiError.Malformed()
        choice["error"]?.let { throw AiError.Model(it.jsonObject["message"]?.jsonPrimitive?.contentOrNull ?: "unknown error") }
        val content = choice["message"]?.jsonObject?.get("content")?.jsonPrimitive?.contentOrNull ?: throw AiError.Malformed()
        parseLenient(content) ?: throw AiError.Malformed()
    }

    private fun errorMessage(raw: String): String? = runCatching {
        json.parseToJsonElement(raw).jsonObject["error"]?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
    }.getOrNull()

    /** Accepts bare JSON, or JSON wrapped in prose / code fences. */
    fun parseLenient(content: String): JsonObject? {
        val trimmed = content.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return runCatching { json.parseToJsonElement(trimmed.substring(start, end + 1)) as? JsonObject }.getOrNull()
    }

}
