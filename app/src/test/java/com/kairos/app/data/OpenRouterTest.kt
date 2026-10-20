package com.kairos.app.data

import com.kairos.app.data.ai.AiError
import com.kairos.app.data.ai.OpenRouter
import com.kairos.app.engine.Curator
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class OpenRouterTest {
    private val server = MockWebServer()
    private lateinit var ai: OpenRouter

    @Before fun setUp() { server.start(); ai = OpenRouter(keys = null, baseUrl = server.url("/api/v1").toString().trimEnd('/'), keyOverride = "sk-or-test") }
    @After fun tearDown() = server.shutdown()

    private fun reply(content: String) = MockResponse().setBody(
        """{"choices":[{"message":{"role":"assistant","content":${Json.encodeToString(kotlinx.serialization.json.JsonPrimitive.serializer(), kotlinx.serialization.json.JsonPrimitive(content))}}}]}"""
    )

    @Test fun `sends schema, model and auth, and parses content`() = runBlocking {
        server.enqueue(reply("""{"words":[],"quotes":[],"sparks":["Call a friend you miss."],"prompts":[]}"""))
        val out = ai.completeJson("anthropic/claude-haiku-5.5", "sys", "user", "kairos_batch", Curator.schema)
        assertEquals("Call a friend you miss.", out["sparks"]!!.jsonArray[0].jsonPrimitive.content)
        val req = server.takeRequest()
        assertEquals("/api/v1/chat/completions", req.path)
        assertEquals("Bearer sk-or-test", req.getHeader("Authorization"))
        val body = Json.parseToJsonElement(req.body.readUtf8()).jsonObject
        assertEquals("anthropic/claude-haiku-5.5", body["model"]!!.jsonPrimitive.content)
        assertEquals("json_schema", body["response_format"]!!.jsonObject["type"]!!.jsonPrimitive.content)
    }

    @Test fun `json wrapped in fences still parses`() = runBlocking {
        server.enqueue(reply("Here you go:\n```json\n{\"words\":[],\"quotes\":[],\"sparks\":[],\"prompts\":[\"Hi there friend?\"]}\n```"))
        val out = ai.completeJson("m", "s", "u", "n", Curator.schema)
        assertTrue(out.containsKey("prompts"))
    }

    @Test fun `401 becomes a bad key error`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":{"message":"No auth"}}"""))
        try { ai.completeJson("m", "s", "u", "n", Curator.schema); fail() } catch (e: AiError.BadKey) { }
    }

    @Test fun `402 becomes out of credits`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(402))
        try { ai.completeJson("m", "s", "u", "n", Curator.schema); fail() } catch (e: AiError.NoCredits) { }
    }

    @Test fun `validate hits the key endpoint`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"data":{"label":"x"}}"""))
        assertNull(ai.validate("sk-or-good"))
        assertEquals("/api/v1/key", server.takeRequest().path)
        server.enqueue(MockResponse().setResponseCode(401))
        assertTrue(ai.validate("sk-or-bad") is AiError.BadKey)
    }
}
