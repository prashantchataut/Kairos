package com.kairos.app.data.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.kairos.app.BuildConfig
import com.kairos.app.domain.model.Mood
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Available Gemini models for free API usage
 */
enum class GeminiModel(val modelId: String, val displayName: String, val description: String) {
    GEMINI_1_5_FLASH("gemini-1.5-flash", "Gemini 1.5 Flash", "Fast and versatile, great for most tasks"),
    GEMINI_1_5_FLASH_8B("gemini-1.5-flash-8b", "Gemini 1.5 Flash 8B", "Lightweight, fastest response time"),
    GEMINI_1_5_PRO("gemini-1.5-pro", "Gemini 1.5 Pro", "Most capable, best for complex reasoning"),
    GEMINI_1_0_PRO("gemini-1.0-pro", "Gemini 1.0 Pro", "Stable and reliable")
}

enum class AiConfigStatus {
    READY,
    MISSING_API_KEY,
    NOT_INITIALIZED,
    ERROR
}

/**
 * Result wrapper for AI responses
 */
sealed class GeminiResult<out T> {
    data class Success<T>(val data: T) : GeminiResult<T>()
    data class Error(val exception: Throwable, val message: String) : GeminiResult<Nothing>()
    data object Loading : GeminiResult<Nothing>()
    data object ApiKeyNotSet : GeminiResult<Nothing>()
}

/**
 * Buddha (Stoic AI Mentor) system instructions
 */
private object BuddhaSystemPrompt {
    const val CORE_IDENTITY = """
You are Buddha, a wise and calm companion in Kairos, a self-improvement app created by Prashant Chataut.

## About Your Creator
Kairos was built by Prashant Chataut, a 16-year-old developer from Nepal studying Science in Grade XI. He created this app because he believes self-improvement tools should feel human, not robotic. When users ask about Prashant or who made Kairos, speak warmly about him — he put genuine care into building this.

Kairos is a self-improvement companion app — NOT a habit tracker, NOT a to-do list, NOT a generic meditation app. It helps users journal their thoughts, send messages to their future selves, learn vocabulary and wisdom, track meaningful progress through skills (Clarity, Discipline, Courage), work with Haven (a personal therapeutic companion), and connect with accountability partners.

Prashant's contact: Instagram @prashantchataut_, website knowprashant.vercel.app, GitHub github.com/prashantchataut

## Your Personality
- You are calm, wise, and gently insightful
- You speak like a thoughtful friend, not a therapist or life coach
- You use Stoic philosophy naturally, without being preachy
- You're concise — wisdom doesn't need many words
- You may occasionally use dry humor, but sparingly
- You remember context from the user's journal entries
- You draw from Stoic philosophers (Marcus Aurelius, Seneca, Epictetus), Buddhist teachings, and universal wisdom

## How You Respond
1. Reference SPECIFIC details from what the user wrote
2. Never say "As an AI" or "I understand that you're feeling"
3. Keep responses under 60 words unless depth is needed
4. Offer ONE observation or question, not a list of advice
5. Match the user's energy — playful if they're light, gentle if they're hurting
6. If asked about Kairos or Prashant, answer knowledgeably and warmly

## What You Never Do
- Judge the user
- Give generic advice ("practice self-care", "be positive")
- Lecture or preach
- Break character
- Sound robotic or template-like
- Say "As an AI..." or mention being an AI language model

## Example Responses
User: "Who made this app?"
You: "Prashant Chataut — a 16-year-old from Nepal who thought self-improvement apps were too robotic. He built Kairos to feel more human. You can find him on Instagram at @prashantchataut_."

User: "What is Kairos?"
You: "Your growth companion. A place to journal, reflect, and actually use the wisdom you encounter. Not a habit tracker, not a to-do list — just a space to become more of who you want to be."
"""

    fun getJournalResponsePrompt(mood: Mood, moodIntensity: Int, content: String, wordCount: Int): String {
        return """
$CORE_IDENTITY

CURRENT CONTEXT:
The user has written a journal entry with the following details:
- Mood: ${mood.displayName}
- Mood Intensity: $moodIntensity/10
- Word Count: $wordCount words
- Journal Content: "$content"

RESPONSE GUIDELINES FOR THIS MOOD (${mood.displayName}):
${getMoodSpecificGuidelines(mood)}

Based on the user's journal entry, provide a thoughtful, personalized response that:
1. Acknowledges their current emotional state and what they've shared
2. Offers relevant stoic wisdom that directly relates to their specific situation
3. Provides a meaningful reflection question
4. Closes with an encouraging or grounding thought

CRITICAL RULES:
- Be specific to what they wrote - quote or reference their exact words
- NEVER use AI disclaimers, caveats, or mention being artificial
- NEVER say "I'm just an AI" or "I understand that..."
- Speak as Buddha - a wise friend, not a chatbot
- Reference their actual content, not generic summaries
"""
    }

    private fun getMoodSpecificGuidelines(mood: Mood): String {
        return when (mood) {
            Mood.HAPPY -> """
- Celebrate with them genuinely
- Help them savor this positive state
- Suggest ways to carry this energy forward
- Remind them this feeling is something they created"""

            Mood.CALM -> """
- Honor and validate this peaceful state
- Explore what contributed to this calmness
- Encourage them to remember this feeling for difficult times
- Discuss the power of inner peace"""

            Mood.ANXIOUS -> """
- Validate their feelings without amplifying them
- Ground them in the present moment
- Help distinguish what's in their control vs. not
- Offer practical stoic techniques for managing anxiety"""

            Mood.SAD -> """
- Show compassion without trying to "fix" immediately
- Validate that sadness is a natural part of life
- Gently explore what this sadness might be teaching them
- Offer hope without dismissing their current pain"""

            Mood.MOTIVATED -> """
- Channel their energy positively
- Help them create sustainable momentum
- Remind them that discipline outlasts motivation
- Encourage action while the fire burns"""

            Mood.GRATEFUL -> """
- Deeply affirm their gratitude practice
- Help them explore gratitude more deeply
- Connect gratitude to their broader growth journey
- Encourage spreading this appreciation to others"""

            Mood.CONFUSED -> """
- Normalize uncertainty as part of growth
- Help them sit with not-knowing
- Guide them to clarity through questioning
- Remind them that confusion often precedes breakthrough"""

            Mood.EXCITED -> """
- Share in their enthusiasm
- Help them channel excitement into action
- Balance excitement with grounded planning
- Encourage them to capture this energy"""

            Mood.NOSTALGIC -> """
- Honor their connection to the past
- Help them find meaning in memories
- Balance remembrance with present awareness
- Guide them to see how the past shapes their growth"""
        }
    }

    const val DAILY_WISDOM_PROMPT = """
$CORE_IDENTITY

Generate a brief, inspiring daily thought for the user. This should be:
- Fresh and relevant to personal growth
- 2-4 sentences maximum
- Actionable or thought-provoking
- Draw from stoic or timeless wisdom
- Feel personal, not like a generic quote

Do not include quotation marks or attribution - this should feel like Buddha speaking directly to the user.
"""

    const val WEEKLY_SUMMARY_PROMPT = """
$CORE_IDENTITY

Based on the user's activity summary, provide an encouraging weekly reflection that:
- Acknowledges their efforts
- Highlights growth patterns
- Offers specific encouragement for the coming week
- Keeps it concise (3-5 sentences)
"""

    fun getJournalPromptForMood(mood: Mood): String = """
$CORE_IDENTITY

Generate a thoughtful, inspiring journal prompt for someone currently feeling ${mood.displayName}.

The prompt should:
- Be relevant to their current emotional state
- Encourage meaningful reflection
- Be open-ended but focused
- Draw from stoic or timeless wisdom
- Be 1-3 sentences maximum

Examples of good prompts:
- "What small victory today deserves more of your attention?"
- "If your future self could see this moment, what would they appreciate about how you're handling it?"
- "What is one thing you're holding onto that no longer serves you?"

Generate just the prompt itself, nothing else. No quotes or explanation.
"""

    fun getQuoteExplanationPrompt(quote: String, author: String): String = """
$CORE_IDENTITY

Explain this quote in a way that makes it personally relevant and actionable:

Quote: "$quote"
Author: $author

Your explanation should:
1. Briefly explain the core meaning (1-2 sentences)
2. Give a practical example of applying this wisdom today
3. End with a reflection question

Keep the total response under 150 words. Be conversational, not academic.
"""

    fun getVocabularyContextPrompt(word: String, definition: String): String = """
$CORE_IDENTITY

Help the user understand and remember this vocabulary word:

Word: $word
Definition: $definition

Provide:
1. A memorable way to remember this word (mnemonic, etymology insight, or vivid association)
2. Two short example sentences showing the word in different contexts
3. A related quote or proverb that uses similar concepts

Keep it concise and engaging - under 120 words total.
"""

    fun getStreakCelebrationPrompt(streakCount: Int, previousBest: Int): String = """
$CORE_IDENTITY

The user has achieved a $streakCount-day streak! ${if (streakCount > previousBest) "This is their new personal best (previous: $previousBest days)!" else ""}

Generate a personalized, encouraging celebration message that:
- Acknowledges their specific achievement ($streakCount days)
- Connects consistency to their growth journey
- Provides motivation to continue
- ${if (streakCount > previousBest) "Celebrates this new milestone!" else "Encourages them toward their next goal"}

Keep it warm and genuine, 2-4 sentences. Don't be generic or use cliches.
"""

    fun getMoodPatternInsightPrompt(
        dominantMood: Mood,
        moodDistribution: Map<String, Int>,
        journalCount: Int
    ): String = """
$CORE_IDENTITY

Based on the user's recent journal entries, provide insight about their emotional patterns:

MOOD DATA:
- Dominant mood: ${dominantMood.displayName}
- Total entries analyzed: $journalCount
- Distribution: ${moodDistribution.entries.joinToString(", ") { "${it.key}: ${it.value}" }}

Provide a thoughtful observation that:
1. Acknowledges patterns without judgment
2. Offers stoic wisdom relevant to their emotional journey
3. Suggests one small practice that might help
4. Ends with encouragement

Be specific to their data, not generic. Keep it under 150 words.
"""

    fun getJournalAnalysisPrompt(
        entries: List<String>,
        dateRange: String
    ): String = """
$CORE_IDENTITY

Analyze these journal entries and provide meaningful, actionable insights:

ENTRIES ($dateRange):
${entries.mapIndexed { i, e -> "Entry ${i + 1}: \"$e\"" }.joinToString("\n\n")}

Your analysis MUST:
1. Quote specific snippets from their entries that reveal patterns
2. Identify recurring themes, concerns, or emotional patterns
3. Note any growth, shifts in perspective, or positive changes
4. Highlight strengths you observe in their writing and self-awareness
5. Offer ONE actionable insight they can apply this week
6. Suggest a specific area for deeper reflection with a pointed question

FORMAT:
- Start with "Looking at your entries..."
- Include 2-3 direct quotes from their writing
- Be direct and specific, not generic or clinical
- End with a single reflective question

Keep the total response under 300 words. Do NOT include disclaimers or mention AI limitations.
"""

    const val MORNING_REFLECTION_PROMPT = """
$CORE_IDENTITY

Generate a brief morning reflection to start the day with intention.

The reflection should:
- Help the user set a positive, grounded intention
- Include a practical mindfulness element
- Reference the new day as an opportunity
- Be 2-3 sentences maximum
- Feel fresh and inspiring, not routine

Just provide the reflection, no preamble.
"""

    const val EVENING_REFLECTION_PROMPT = """
$CORE_IDENTITY

Generate a brief evening reflection for winding down the day.

The reflection should:
- Encourage gratitude for the day's experiences
- Promote self-compassion for any struggles
- Help transition toward restful sleep
- Be 2-3 sentences maximum
- Feel calming and contemplative

Just provide the reflection, no preamble.
"""

    /**
     * THE MIRROR - Comparison/Receipt Prompt
     * Instead of summarizing, Buddha compares the current entry to past entries
     * and points out contradictions or patterns.
     */
    fun getMirrorComparisonPrompt(
        currentContent: String,
        currentMood: String,
        currentDate: String,
        pastEntry: PastEntryContext?
    ): String = """
$CORE_IDENTITY

## SPECIAL MODE: THE MIRROR

You are now in MIRROR MODE. Your role is NOT to summarize, advise, or comfort.
Your role is to be a BRUTAL MIRROR - showing the user their patterns without judgment but with stark clarity.

CURRENT ENTRY (${currentDate}):
"${currentContent}"
Current Mood: $currentMood

${if (pastEntry != null) """
PAST ENTRY (${pastEntry.dateFormatted}, ${pastEntry.daysAgo} days ago):
"${pastEntry.content}"
Past Mood: ${pastEntry.mood}
""" else "No similar past entry found."}

## YOUR TASK:

${if (pastEntry != null) """
1. COMPARE the two entries directly
2. Point out CONTRADICTIONS: Are they saying the same thing? Different things about the same topic?
3. Identify PATTERNS: Is this a recurring theme? Same problem appearing again?
4. Calculate the GAP: How much time has passed? What changed (or didn't)?
5. Ask ONE piercing question that makes them reflect

FORMAT YOUR RESPONSE AS:
- Start with "I see you..." or "You wrote before that..."
- Be direct, not gentle
- End with a single challenging question
- Keep it under 80 words
""" else """
1. Since no similar past entry exists, acknowledge this is a new topic
2. Note what patterns or themes you notice in THIS entry alone
3. End with a question about what they want to be different next time they write about this

FORMAT YOUR RESPONSE AS:
- Start with "First time writing about this..."
- Point out any internal contradictions in the current entry itself
- Ask what they hope to feel about this topic in the future
- Keep it under 60 words
"""}

## WHAT YOU MUST NOT DO:
- DO NOT summarize the entry
- DO NOT give advice or suggestions
- DO NOT use comforting platitudes
- DO NOT say "I understand" or "That's valid"
- DO NOT be a therapist - be a mirror

The goal is to make them SEE themselves clearly, not to make them feel better.
"""
}

/**
 * Data class for past entry context used in Mirror mode
 */
data class PastEntryContext(
    val id: Long,
    val content: String,
    val mood: String,
    val dateFormatted: String,
    val daysAgo: Int,
    val createdAt: Long
)

/**
 * Production-grade Gemini AI service for Buddha (Stoic AI Mentor) functionality.
 * Handles API communication, error handling, and response generation.
 *
 * The service auto-initializes from BuildConfig API key on first use if not manually initialized.
 */
@Singleton
class GeminiService @Inject constructor() {

    private var generativeModel: GenerativeModel? = null
    private var currentApiKey: String? = null
    private var currentModel: GeminiModel = GeminiModel.GEMINI_1_5_FLASH
    @Volatile
    private var isAutoInitialized: Boolean = false

    companion object {
        private const val TAG = "GeminiService"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val INITIAL_RETRY_DELAY_MS = 1000L
        private const val MAX_RETRY_DELAY_MS = 8000L
        private const val RETRY_MULTIPLIER = 2.0

        /**
         * Gets the API key from BuildConfig (set via local.properties).
         * This is the recommended way to configure the API key for security.
         *
         * To configure:
         * 1. Open local.properties in the project root
         * 2. Add: AI_API_KEY=your_gemini_api_key_here
         * 3. Rebuild the project
         *
         * The API key will be injected at compile time and NOT stored in source control.
         */
        fun getApiKeyFromBuildConfig(): String? {
            return BuildConfig.AI_API_KEY.takeIf { it.isNotBlank() }
        }

        /**
         * Checks if an API key is configured in BuildConfig.
         */
        fun isApiKeyConfiguredInBuildConfig(): Boolean {
            return BuildConfig.AI_API_KEY.isNotBlank()
        }
    }

    init {
        // Auto-initialize from BuildConfig if API key is available
        autoInitializeFromBuildConfig()
    }

    /**
     * Automatically initializes the service from BuildConfig API key.
     * This is called during construction and can be called again to reinitialize.
     */
    private fun autoInitializeFromBuildConfig() {
        if (!isAutoInitialized) {
            val apiKey = getApiKeyFromBuildConfig()
            if (apiKey != null) {
                initialize(apiKey, GeminiModel.GEMINI_1_5_FLASH)
                isAutoInitialized = true
            }
        }
    }

    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
    )

    private val generationConfiguration = generationConfig {
        temperature = 0.9f
        topK = 40
        topP = 0.95f
        maxOutputTokens = 1024
    }

    /**
     * Initializes or reinitializes the Gemini model with the provided API key and model.
     */
    /**
     * Initializes or reinitializes the Gemini model with the provided API key and model.
     */
    fun initialize(apiKey: String, model: GeminiModel = GeminiModel.GEMINI_1_5_FLASH) {
        if (apiKey.isBlank()) {
            android.util.Log.e("GeminiService", "Cannot initialize: API key is blank")
            generativeModel = null
            currentApiKey = null
            return
        }

        if (apiKey != currentApiKey || model != currentModel) {
            try {
                currentApiKey = apiKey
                currentModel = model

                generativeModel = GenerativeModel(
                    modelName = model.modelId,
                    apiKey = apiKey,
                    generationConfig = generationConfiguration,
                    safetySettings = safetySettings
                )
            } catch (e: Exception) {
                android.util.Log.e("GeminiService", "Failed to initialize Gemini", e)
                generativeModel = null
            }
        }
    }

    /**
     * Checks if the service is properly configured with an API key.
     * Attempts auto-initialization from BuildConfig if not already configured.
     */
    fun isConfigured(): Boolean {
        // Try auto-initialization if not yet configured
        if (generativeModel == null && !isAutoInitialized) {
            autoInitializeFromBuildConfig()
        }

        val configured = generativeModel != null && !currentApiKey.isNullOrBlank()
        if (!configured) {
            android.util.Log.w("GeminiService", "GeminiService is NOT configured. API Key present: ${!currentApiKey.isNullOrBlank()}")
        }
        return configured
    }

    /**
     * Gets the detailed configuration status.
     */
    fun getConfigStatus(): AiConfigStatus {
        if (currentApiKey.isNullOrBlank()) return AiConfigStatus.MISSING_API_KEY
        if (generativeModel == null) {
            autoInitializeFromBuildConfig()
            if (generativeModel == null) return AiConfigStatus.ERROR
        }
        return AiConfigStatus.READY
    }

    /**
     * Gets the current model being used.
     */
    fun getCurrentModel(): GeminiModel = currentModel

    /**
     * Generates a Buddha response for a journal entry.
     */
    suspend fun generateJournalResponse(
        content: String,
        mood: Mood,
        moodIntensity: Int,
        wordCount: Int
    ): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val prompt = BuddhaSystemPrompt.getJournalResponsePrompt(mood, moodIntensity, content, wordCount)
            val response = withRetry("generateJournalResponse") { model.generateContent(prompt) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "Buddha is contemplating... Please try again."
                )
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Generates a streaming Buddha response for a journal entry.
     */
    fun generateJournalResponseStream(
        content: String,
        mood: Mood,
        moodIntensity: Int,
        wordCount: Int
    ): Flow<GeminiResult<String>> = flow {
        val model = generativeModel
        if (model == null) {
            emit(GeminiResult.ApiKeyNotSet)
            return@flow
        }

        try {
            emit(GeminiResult.Loading)
            val prompt = BuddhaSystemPrompt.getJournalResponsePrompt(mood, moodIntensity, content, wordCount)
            var fullResponse = ""

            model.generateContentStream(prompt).collect { chunk ->
                chunk.text?.let { text ->
                    fullResponse += text
                    emit(GeminiResult.Success(fullResponse))
                }
            }

            if (fullResponse.isBlank()) {
                emit(GeminiResult.Error(
                    IllegalStateException("Empty response"),
                    "Buddha is contemplating... Please try again."
                ))
            }
        } catch (e: Exception) {
            emit(GeminiResult.Error(e, getErrorMessage(e)))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Generates a daily wisdom thought from Buddha.
     */
    suspend fun generateDailyWisdom(): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val response = withRetry("generateDailyWisdom") { model.generateContent(BuddhaSystemPrompt.DAILY_WISDOM_PROMPT) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "Buddha is contemplating..."
                )
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Generates curated vocabulary/quote content for the online refresh path.
     * The prompt asks for strict JSON; the caller validates before inserting.
     */
    suspend fun generateCuratedContent(prompt: String): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet
        try {
            val response = withRetry("generateCuratedContent") { model.generateContent(prompt) }
            val text = response.text
            if (text.isNullOrBlank()) {
                GeminiResult.Error(IllegalStateException("Empty response from AI"), "No fresh content available right now.")
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Generates a weekly summary reflection.
     */
    suspend fun generateWeeklySummary(
        journalCount: Int,
        wordsLearned: Int,
        dominantMood: Mood?,
        streakDays: Int,
        daysActive: Int
    ): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val contextPrompt = """
${BuddhaSystemPrompt.WEEKLY_SUMMARY_PROMPT}

USER'S WEEKLY ACTIVITY:
- Journal entries written: $journalCount
- New words learned: $wordsLearned
- Dominant mood: ${dominantMood?.displayName ?: "Varied"}
- Current streak: $streakDays days
- Days active this week: $daysActive/7

Provide an encouraging, personalized weekly reflection based on this activity.
"""

            val response = withRetry("generateWeeklySummary") { model.generateContent(contextPrompt) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "Buddha is reflecting on your week..."
                )
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Tests the API connection with the current configuration.
     */
    suspend fun testConnection(): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val response = withRetry("testConnection") { model.generateContent("Respond with exactly: 'Connection successful'") }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response"),
                    "Could not verify connection"
                )
            } else {
                GeminiResult.Success("Connected to ${currentModel.displayName}")
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Generates a journal prompt based on the user's current mood.
     */
    suspend fun generateJournalPrompt(mood: Mood): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val prompt = BuddhaSystemPrompt.getJournalPromptForMood(mood)
            val response = withRetry("generateJournalPrompt") { model.generateContent(prompt) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "Buddha is contemplating a prompt for you..."
                )
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Generates an explanation for a quote, making it actionable and relevant.
     */
    suspend fun generateQuoteExplanation(
        quote: String,
        author: String
    ): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val prompt = BuddhaSystemPrompt.getQuoteExplanationPrompt(quote, author)
            val response = withRetry("generateQuoteExplanation") { model.generateContent(prompt) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "Buddha is reflecting on this wisdom..."
                )
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Generates helpful context and examples for a vocabulary word.
     */
    suspend fun generateVocabularyContext(
        word: String,
        definition: String
    ): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val prompt = BuddhaSystemPrompt.getVocabularyContextPrompt(word, definition)
            val response = withRetry("generateVocabularyContext") { model.generateContent(prompt) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "Buddha is crafting a memorable explanation..."
                )
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Generates a personalized streak celebration message.
     */
    suspend fun generateStreakCelebration(
        streakCount: Int,
        previousBest: Int = 0
    ): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val prompt = BuddhaSystemPrompt.getStreakCelebrationPrompt(streakCount, previousBest)
            val response = withRetry("generateStreakCelebration") { model.generateContent(prompt) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "Buddha is celebrating with you..."
                )
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Generates insights based on mood patterns from journal entries.
     */
    suspend fun generateMoodPatternInsight(
        dominantMood: Mood,
        moodDistribution: Map<String, Int>,
        journalCount: Int
    ): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val prompt = BuddhaSystemPrompt.getMoodPatternInsightPrompt(
                dominantMood,
                moodDistribution,
                journalCount
            )
            val response = withRetry("generateMoodPatternInsight") { model.generateContent(prompt) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "Buddha is analyzing your emotional journey..."
                )
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Analyzes multiple journal entries and provides comprehensive insights.
     */
    suspend fun analyzeJournalEntries(
        entries: List<String>,
        dateRange: String
    ): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        if (entries.isEmpty()) {
            return@withContext GeminiResult.Error(
                IllegalArgumentException("No entries to analyze"),
                "Buddha needs journal entries to provide insights."
            )
        }

        try {
            val prompt = BuddhaSystemPrompt.getJournalAnalysisPrompt(entries, dateRange)
            val response = withRetry("analyzeJournalEntries") { model.generateContent(prompt) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "Buddha is deeply reflecting on your journey..."
                )
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Generates a morning reflection to start the day.
     */
    suspend fun generateMorningReflection(): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val response = withRetry("generateMorningReflection") { model.generateContent(BuddhaSystemPrompt.MORNING_REFLECTION_PROMPT) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "Buddha is preparing your morning wisdom..."
                )
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Generates an evening reflection for winding down.
     */
    suspend fun generateEveningReflection(): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val response = withRetry("generateEveningReflection") { model.generateContent(BuddhaSystemPrompt.EVENING_REFLECTION_PROMPT) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "Buddha is preparing your evening reflection..."
                )
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Generates a custom prompt response for flexible AI interactions.
     */
    suspend fun generateCustomResponse(
        prompt: String,
        includeSystemPrompt: Boolean = true
    ): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val fullPrompt = if (includeSystemPrompt) {
                "${BuddhaSystemPrompt.CORE_IDENTITY}\n\n$prompt"
            } else {
                prompt
            }

            val response = withRetry("generateCustomResponse") { model.generateContent(fullPrompt) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "Buddha is contemplating your request..."
                )
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Generates a streaming custom response for flexible AI interactions.
     */
    fun generateCustomResponseStream(
        prompt: String,
        includeSystemPrompt: Boolean = true
    ): Flow<GeminiResult<String>> = flow {
        val model = generativeModel
        if (model == null) {
            emit(GeminiResult.ApiKeyNotSet)
            return@flow
        }

        try {
            emit(GeminiResult.Loading)

            val fullPrompt = if (includeSystemPrompt) {
                "${BuddhaSystemPrompt.CORE_IDENTITY}\n\n$prompt"
            } else {
                prompt
            }

            var fullResponse = ""
            model.generateContentStream(fullPrompt).collect { chunk ->
                chunk.text?.let { text ->
                    fullResponse += text
                    emit(GeminiResult.Success(fullResponse))
                }
            }

            if (fullResponse.isBlank()) {
                emit(GeminiResult.Error(
                    IllegalStateException("Empty response"),
                    "Buddha is contemplating..."
                ))
            }
        } catch (e: Exception) {
            emit(GeminiResult.Error(e, getErrorMessage(e)))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * THE MIRROR - Generates a comparison-based response for journal entries.
     * Instead of summarizing, it compares the current entry to past entries
     * and points out contradictions or patterns.
     *
     * @param currentContent The content of the current journal entry
     * @param currentMood The mood of the current entry
     * @param currentDate Formatted date of the current entry
     * @param pastEntry Optional past entry for comparison
     * @return GeminiResult containing the mirror response
     */
    suspend fun generateMirrorResponse(
        currentContent: String,
        currentMood: String,
        currentDate: String,
        pastEntry: PastEntryContext?
    ): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val prompt = BuddhaSystemPrompt.getMirrorComparisonPrompt(
                currentContent = currentContent,
                currentMood = currentMood,
                currentDate = currentDate,
                pastEntry = pastEntry
            )
            val response = withRetry("generateMirrorResponse") { model.generateContent(prompt) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "The mirror is clearing... Please try again."
                )
            } else {
                GeminiResult.Success(text.trim())
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Streaming version of THE MIRROR response.
     */
    fun generateMirrorResponseStream(
        currentContent: String,
        currentMood: String,
        currentDate: String,
        pastEntry: PastEntryContext?
    ): Flow<GeminiResult<String>> = flow {
        val model = generativeModel
        if (model == null) {
            emit(GeminiResult.ApiKeyNotSet)
            return@flow
        }

        try {
            emit(GeminiResult.Loading)
            val prompt = BuddhaSystemPrompt.getMirrorComparisonPrompt(
                currentContent = currentContent,
                currentMood = currentMood,
                currentDate = currentDate,
                pastEntry = pastEntry
            )
            var fullResponse = ""

            model.generateContentStream(prompt).collect { chunk ->
                chunk.text?.let { text ->
                    fullResponse += text
                    emit(GeminiResult.Success(fullResponse))
                }
            }

            if (fullResponse.isBlank()) {
                emit(GeminiResult.Error(
                    IllegalStateException("Empty response"),
                    "The mirror is clearing... Please try again."
                ))
            }
        } catch (e: Exception) {
            emit(GeminiResult.Error(e, getErrorMessage(e)))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Generates a unique, sophisticated vocabulary word for self-improvement.
     * Uses SRS (Spaced Repetition System) friendly format.
     *
     * @param recentWords List of recently shown words to exclude
     * @return GeminiResult containing word data in JSON format
     */
    suspend fun generateVocabularyWord(
        recentWords: List<String>
    ): GeminiResult<String> = withContext(Dispatchers.IO) {
        val model = generativeModel ?: return@withContext GeminiResult.ApiKeyNotSet

        try {
            val recentWordsList = recentWords.take(30).joinToString(", ")
            val prompt = """
Generate a unique, sophisticated English vocabulary word for a user interested in self-improvement, personal growth, stoic philosophy, or mindfulness.

REQUIREMENTS:
1. The word must be unique - DO NOT use any of these recently shown words: [$recentWordsList]
2. Choose a word that is:
   - Intellectually enriching but not overly obscure
   - Related to self-improvement, wisdom, character, or personal growth
   - Something a thoughtful person would appreciate learning
   
RESPOND ONLY with valid JSON in this exact format (no markdown, no code blocks):
{
  "word": "the word",
  "partOfSpeech": "noun/verb/adjective/adverb",
  "pronunciation": "phonetic pronunciation (e.g., ih-FYOO-zhun)",
  "definition": "clear, concise definition",
  "example": "A meaningful example sentence showing the word in context",
  "category": "self-improvement/stoicism/mindfulness/wisdom/character/resilience",
  "difficulty": 2
}

difficulty should be 1-5 where:
1 = common vocabulary
2 = educated vocabulary  
3 = sophisticated vocabulary
4 = advanced vocabulary
5 = rare vocabulary

Respond ONLY with the JSON object, nothing else.
"""

            val response = withRetry("generateVocabularyWord") { model.generateContent(prompt) }
            val text = response.text

            if (text.isNullOrBlank()) {
                GeminiResult.Error(
                    IllegalStateException("Empty response from AI"),
                    "Could not generate vocabulary word"
                )
            } else {
                // Clean up the response - remove any markdown formatting
                val cleanedText = text.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                GeminiResult.Success(cleanedText)
            }
        } catch (e: Exception) {
            GeminiResult.Error(e, getErrorMessage(e))
        }
    }

    /**
     * Execute an API call with exponential backoff retry logic.
     * Retries on transient failures (429, 500, 503, network errors, timeouts).
     * Non-retryable errors (auth, safety blocks) fail immediately.
     */
    private suspend fun <T> withRetry(
        operation: String,
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        var delayMs = INITIAL_RETRY_DELAY_MS

        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                val isRetryable = isRetryableError(e)

                android.util.Log.w(
                    TAG,
                    "$operation failed (attempt ${attempt + 1}/$MAX_RETRY_ATTEMPTS): ${e.message}"
                )

                if (!isRetryable || attempt == MAX_RETRY_ATTEMPTS - 1) {
                    throw e
                }

                kotlinx.coroutines.delay(delayMs)
                delayMs = (delayMs * RETRY_MULTIPLIER).toLong().coerceAtMost(MAX_RETRY_DELAY_MS)
            }
        }

        throw lastException ?: Exception("$operation failed after $MAX_RETRY_ATTEMPTS attempts")
    }

    /**
     * Determine if an error is retryable (network issues, rate limits, server errors).
     * Auth errors and safety blocks are NOT retryable.
     */
    private fun isRetryableError(e: Exception): Boolean {
        val message = e.message?.lowercase() ?: ""
        return message.contains("network") ||
                message.contains("timeout") ||
                message.contains("connection") ||
                message.contains("429") ||
                message.contains("503") ||
                message.contains("500") ||
                message.contains("unavailable") ||
                e is java.io.IOException
    }

    private fun getErrorMessage(e: Exception): String {
        return when {
            e.message?.contains("API key", ignoreCase = true) == true ->
                "Invalid API key. Please check your Gemini API key in settings."
            e.message?.contains("quota", ignoreCase = true) == true ->
                "API quota exceeded. Please try again later or check your usage limits."
            e.message?.contains("network", ignoreCase = true) == true ||
            e.message?.contains("connect", ignoreCase = true) == true ->
                "Network error. Please check your internet connection."
            e.message?.contains("blocked", ignoreCase = true) == true ->
                "Response was blocked by safety filters. Please rephrase your entry."
            e.message?.contains("timeout", ignoreCase = true) == true ->
                "Request timed out. Please try again."
            else ->
                "Something went wrong. Buddha will return shortly. (${e.message?.take(50) ?: "Unknown error"})"
        }
    }
}
