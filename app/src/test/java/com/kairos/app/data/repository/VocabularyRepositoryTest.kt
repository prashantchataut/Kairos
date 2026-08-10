package com.kairos.app.data.repository

import com.kairos.app.data.ai.GeminiService
import com.kairos.app.data.local.dao.VocabularyDao
import com.kairos.app.data.local.dao.VocabularyLearningDao
import com.kairos.app.data.local.entity.VocabularyEntity
import com.kairos.app.data.network.NetworkConnectivityManager
import com.kairos.app.domain.common.Result
import com.kairos.app.domain.learning.SpacedRepetitionEngine
import com.kairos.app.domain.repository.VocabularyRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for VocabularyRepositoryImpl.
 *
 * Tests verify correct interaction between repository and DAO layer,
 * ensuring proper data flow and error handling.
 */
class VocabularyRepositoryTest {

    private lateinit var repository: VocabularyRepository
    private val vocabularyDao: VocabularyDao = mockk(relaxed = true)
    private val vocabularyLearningDao: VocabularyLearningDao = mockk(relaxed = true)
    private val spacedRepetitionEngine: SpacedRepetitionEngine = mockk(relaxed = true)
    private val geminiService: GeminiService = mockk(relaxed = true)
    private val networkConnectivityManager: NetworkConnectivityManager = mockk(relaxed = true)

    @Before
    fun setup() {
        repository = VocabularyRepositoryImpl(
            vocabularyDao,
            vocabularyLearningDao,
            spacedRepetitionEngine,
            geminiService,
            networkConnectivityManager
        )
    }

    @Test
    fun `getAllWords returns flow of words`() = runTest {
        // Given
        val mockWords = listOf(
            VocabularyEntity(
                id = 1,
                word = "Serendipity",
                definition = "Happy accident",
                isLearned = false
            ),
            VocabularyEntity(
                id = 2,
                word = "Ephemeral",
                definition = "Lasting a short time",
                isLearned = true,
                learnedAt = System.currentTimeMillis()
            )
        )
        coEvery { vocabularyDao.getAllCuratedVocabulary() } returns flowOf(mockWords)

        // When
        val result = repository.getAllWords().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Serendipity", result[0].word)
    }

    @Test
    fun `markAsLearned updates word status`() = runTest {
        // Given
        val wordId = 1L
        coEvery { vocabularyDao.markAsLearned(wordId, any()) } returns Unit

        // When
        val result = repository.markAsLearned(wordId)

        // Then
        assertTrue(result is Result.Success)
        coVerify { vocabularyDao.markAsLearned(wordId, any()) }
    }

    @Test
    fun `getWordOfTheDay returns a random word`() = runTest {
        // Given
        val mockWord = VocabularyEntity(
            id = 1,
            word = "Stoic",
            definition = "Enduring pain without complaint",
            isLearned = false,
            shownAsDaily = false
        )
        coEvery { vocabularyDao.getWordOfTheDay() } returns mockWord

        // When
        val result = repository.getWordOfTheDay()

        // Then
        assertTrue(result is Result.Success)
        assertEquals("Stoic", (result as Result.Success).data.word)
    }

    @Test
    fun `getWordOfTheDay falls back to random unlearned word when no unshown words`() = runTest {
        // Given
        val mockWord = VocabularyEntity(
            id = 2,
            word = "Resilience",
            definition = "The capacity to recover quickly from difficulties",
            isLearned = false
        )
        coEvery { vocabularyDao.getWordOfTheDay() } returns null
        coEvery { vocabularyDao.getRandomUnlearnedWord() } returns mockWord

        // When
        val result = repository.getWordOfTheDay()

        // Then
        assertTrue(result is Result.Success)
        assertEquals("Resilience", (result as Result.Success).data.word)
    }

    @Test
    fun `getLearnedWords returns only learned words`() = runTest {
        // Given
        val learnedWords = listOf(
            VocabularyEntity(
                id = 1,
                word = "Wisdom",
                definition = "The quality of having experience and good judgment",
                isLearned = true,
                learnedAt = System.currentTimeMillis()
            )
        )
        coEvery { vocabularyDao.getLearnedWords() } returns flowOf(learnedWords)

        // When
        val result = repository.getLearnedWords().first()

        // Then
        assertEquals(1, result.size)
        assertTrue(result.all { it.isLearned })
    }

    @Test
    fun `searchVocabulary returns matching words`() = runTest {
        // Given
        val searchQuery = "wisdom"
        val matchingWords = listOf(
            VocabularyEntity(
                id = 1,
                word = "Wisdom",
                definition = "The quality of having experience and good judgment",
                isLearned = false
            )
        )
        coEvery { vocabularyDao.searchVocabulary(searchQuery) } returns flowOf(matchingWords)

        // When
        val result = repository.searchVocabulary(searchQuery).first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Wisdom", result[0].word)
    }

    @Test
    fun `updateFavoriteStatus updates word favorite status`() = runTest {
        // Given
        val wordId = 1L
        val isFavorite = true
        coEvery { vocabularyDao.updateFavoriteStatus(wordId, isFavorite) } returns Unit

        // When
        val result = repository.updateFavoriteStatus(wordId, isFavorite)

        // Then
        assertTrue(result is Result.Success)
        coVerify { vocabularyDao.updateFavoriteStatus(wordId, isFavorite) }
    }

    @Test
    fun `markAsShownDaily marks word as shown for daily feature`() = runTest {
        // Given
        val wordId = 1L
        coEvery { vocabularyDao.markAsShownDaily(wordId, any()) } returns Unit

        // When
        val result = repository.markAsShownDaily(wordId)

        // Then
        assertTrue(result is Result.Success)
        coVerify { vocabularyDao.markAsShownDaily(wordId, any()) }
    }

    @Test
    fun `getWordById returns word when exists`() = runTest {
        // Given
        val wordId = 1L
        val mockWord = VocabularyEntity(
            id = wordId,
            word = "Contemplation",
            definition = "The action of looking thoughtfully at something for a long time",
            isLearned = false
        )
        coEvery { vocabularyDao.getWordById(wordId) } returns mockWord

        // When
        val result = repository.getWordById(wordId)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("Contemplation", (result as Result.Success).data.word)
    }

    @Test
    fun `insertWord returns inserted word id`() = runTest {
        // Given
        val newWord = VocabularyEntity(
            word = "Mindfulness",
            definition = "The quality or state of being conscious or aware of something",
            category = "philosophy"
        )
        val expectedId = 1L
        coEvery { vocabularyDao.insertWord(newWord) } returns expectedId

        // When
        val result = repository.insertWord(newWord)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(expectedId, (result as Result.Success).data)
    }
}
