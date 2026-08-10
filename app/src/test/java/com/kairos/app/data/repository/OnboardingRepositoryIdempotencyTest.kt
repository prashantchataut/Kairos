package com.kairos.app.data.repository

import com.kairos.app.data.local.dao.IdiomDao
import com.kairos.app.data.local.dao.PhraseDao
import com.kairos.app.data.local.dao.ProverbDao
import com.kairos.app.data.local.dao.QuoteDao
import com.kairos.app.data.local.dao.UserDao
import com.kairos.app.data.local.dao.VocabularyDao
import com.kairos.app.data.local.database.KairosDatabase
import com.kairos.app.data.local.preferences.PreferencesManager
import com.kairos.app.domain.repository.OnboardingPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Onboarding must be idempotent: the Room lifecycle callback already seeds the
 * catalog on fresh installs, so completeSetup must never insert duplicate rows
 * (auto-generated ids would otherwise double every overlapping entry).
 */
class OnboardingRepositoryIdempotencyTest {

    private fun preferences() = OnboardingPreferences(
        vocabularyDifficulty = 3,
        wisdomCategories = setOf("wisdom", "life"),
        wordCategories = setOf("self-improvement", "reflection"),
        practiceSessionSize = 5
    )

    private fun mockedDatabase(): KairosDatabase {
        val db = mockk<KairosDatabase>(relaxed = true)
        // Room's withTransaction() executes the block on the transaction
        // executor; run it inline so the seeded content actually inserts.
        every { db.transactionExecutor } returns java.util.concurrent.Executor { it.run() }
        every { db.beginTransaction() } just runs
        every { db.setTransactionSuccessful() } just runs
        every { db.endTransaction() } just runs
        return db
    }

    private fun mockedPreferences(): PreferencesManager {
        val prefs = mockk<PreferencesManager>()
        coEvery { prefs.setFirstLaunchTime(any()) } just runs
        coEvery { prefs.setUserId(any()) } just runs
        coEvery { prefs.setVocabularyDifficulty(any()) } just runs
        coEvery { prefs.setSelectedWisdomCategories(any()) } just runs
        coEvery { prefs.setPreferredWordCategories(any()) } just runs
        coEvery { prefs.setPracticeSessionSize(any()) } just runs
        coEvery { prefs.setOnboardingCompleted(any()) } just runs
        return prefs
    }

    @Test
    fun `completeSetup skips catalog inserts when tables are already seeded`() = runTest {
        val vocabDao = mockk<VocabularyDao>(relaxed = true)
        val quoteDao = mockk<QuoteDao>(relaxed = true)
        val proverbDao = mockk<ProverbDao>(relaxed = true)
        val idiomDao = mockk<IdiomDao>(relaxed = true)
        val phraseDao = mockk<PhraseDao>(relaxed = true)
        val userDao = mockk<UserDao>(relaxed = true)

        coEvery { vocabDao.countAll() } returns 47
        coEvery { quoteDao.countAll() } returns 77
        coEvery { proverbDao.countAll() } returns 24
        coEvery { idiomDao.countAll() } returns 32
        coEvery { phraseDao.countAll() } returns 28
        coEvery { userDao.insertUserProfile(any()) } just runs
        coEvery { userDao.insertUserStats(any()) } just runs
        coEvery { userDao.insertAchievements(any()) } just runs

        val prefs = mockedPreferences()
        val repo = OnboardingRepositoryImpl(
            preferencesManager = prefs,
            vocabularyDao = vocabDao,
            quoteDao = quoteDao,
            proverbDao = proverbDao,
            idiomDao = idiomDao,
            phraseDao = phraseDao,
            userDao = userDao,
            database = mockedDatabase()
        )

        val result = repo.completeSetup(preferences())
        assertTrue("setup should succeed", result.isSuccess)

        coVerify(exactly = 0) { vocabDao.insertWords(any()) }
        coVerify(exactly = 0) { quoteDao.insertQuotes(any()) }
        coVerify(exactly = 0) { proverbDao.insertProverbs(any()) }
        coVerify(exactly = 0) { idiomDao.insertIdioms(any()) }
        coVerify(exactly = 0) { phraseDao.insertPhrases(any()) }
        coVerify(exactly = 1) { userDao.insertUserProfile(any()) }
        coVerify(exactly = 1) { prefs.setOnboardingCompleted(true) }
    }

    @Test
    fun `completeSetup seeds catalog when tables are empty`() = runTest {
        val vocabDao = mockk<VocabularyDao>(relaxed = true)
        val quoteDao = mockk<QuoteDao>(relaxed = true)
        val proverbDao = mockk<ProverbDao>(relaxed = true)
        val idiomDao = mockk<IdiomDao>(relaxed = true)
        val phraseDao = mockk<PhraseDao>(relaxed = true)
        val userDao = mockk<UserDao>(relaxed = true)

        coEvery { vocabDao.countAll() } returns 0
        coEvery { quoteDao.countAll() } returns 0
        coEvery { proverbDao.countAll() } returns 0
        coEvery { idiomDao.countAll() } returns 0
        coEvery { phraseDao.countAll() } returns 0
        coEvery { vocabDao.insertWords(any()) } just runs
        coEvery { quoteDao.insertQuotes(any()) } just runs
        coEvery { proverbDao.insertProverbs(any()) } just runs
        coEvery { idiomDao.insertIdioms(any()) } just runs
        coEvery { phraseDao.insertPhrases(any()) } just runs
        coEvery { userDao.insertUserProfile(any()) } just runs
        coEvery { userDao.insertUserStats(any()) } just runs
        coEvery { userDao.insertAchievements(any()) } just runs

        val prefs = mockedPreferences()
        val repo = OnboardingRepositoryImpl(
            preferencesManager = prefs,
            vocabularyDao = vocabDao,
            quoteDao = quoteDao,
            proverbDao = proverbDao,
            idiomDao = idiomDao,
            phraseDao = phraseDao,
            userDao = userDao,
            database = mockedDatabase()
        )

        val result = repo.completeSetup(preferences())
        assertTrue("setup should succeed", result.isSuccess)

        coVerify(exactly = 1) { vocabDao.insertWords(any()) }
        coVerify(exactly = 1) { quoteDao.insertQuotes(any()) }
        coVerify(exactly = 1) { proverbDao.insertProverbs(any()) }
        coVerify(exactly = 1) { idiomDao.insertIdioms(any()) }
        coVerify(exactly = 1) { phraseDao.insertPhrases(any()) }
        coVerify(exactly = 1) { prefs.setOnboardingCompleted(true) }
    }
}
