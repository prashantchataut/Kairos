package com.kairos.app.data.repository

import androidx.room.withTransaction
import com.kairos.app.data.InitialContentData
import com.kairos.app.data.local.dao.IdiomDao
import com.kairos.app.data.local.dao.PhraseDao
import com.kairos.app.data.local.dao.ProverbDao
import com.kairos.app.data.local.dao.QuoteDao
import com.kairos.app.data.local.dao.UserDao
import com.kairos.app.data.local.dao.VocabularyDao
import com.kairos.app.data.local.database.KairosDatabase
import com.kairos.app.data.local.entity.AchievementEntity
import com.kairos.app.data.local.entity.UserProfileEntity
import com.kairos.app.data.local.entity.UserStatsEntity
import com.kairos.app.data.local.preferences.PreferencesManager
import com.kairos.app.domain.common.ErrorType
import com.kairos.app.domain.common.Result
import com.kairos.app.domain.common.runSuspendCatching
import com.kairos.app.domain.identity.KairosAchievements
import com.kairos.app.domain.repository.OnboardingPreferences
import com.kairos.app.domain.repository.OnboardingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val vocabularyDao: VocabularyDao,
    private val quoteDao: QuoteDao,
    private val proverbDao: ProverbDao,
    private val idiomDao: IdiomDao,
    private val phraseDao: PhraseDao,
    private val userDao: UserDao,
    private val database: KairosDatabase
) : OnboardingRepository {

    override suspend fun completeSetup(preferences: OnboardingPreferences): Result<Unit> =
        runSuspendCatching(
            errorType = ErrorType.DATABASE,
            errorMessage = "Kairos could not finish local setup."
        ) {
            val now = System.currentTimeMillis()
            val categories = preferences.wisdomCategories
                .map(String::lowercase)
                .filter(String::isNotBlank)
                .toSet()
                .ifEmpty { DEFAULT_WISDOM_CATEGORIES }

            val wordCategories = preferences.wordCategories
                .map(String::lowercase)
                .filter(String::isNotBlank)
                .toSet()
                .ifEmpty { DEFAULT_WORD_CATEGORIES }

            database.withTransaction {
                userDao.insertUserProfile(
                    UserProfileEntity(
                        id = 1,
                        displayName = "Reader",
                        joinedAt = now
                    )
                )
                userDao.insertUserStats(
                    UserStatsEntity(
                        id = 1,
                        lastResetDate = now
                    )
                )
                userDao.insertAchievements(
                    KairosAchievements.allAchievements.map { achievement ->
                        AchievementEntity.fromDomain(
                            id = achievement.id,
                            name = achievement.name,
                            description = achievement.description,
                            category = achievement.category.id,
                            rarity = achievement.rarity.id,
                            iconName = achievement.iconName,
                            requirement = achievement.requirement,
                            celebrationMessage = achievement.celebrationMessage,
                            rewardPoints = achievement.rewardPoints
                        )
                    }
                )
                // Idempotent catalog seeding: the Room lifecycle callback already
                // seeds the full catalog on fresh installs. Inserting again with
                // auto-generated ids would duplicate rows (quotes, proverbs, idioms,
                // phrases, and vocabulary all overlap with the callback seed).
                if (vocabularyDao.countAll() == 0) {
                    vocabularyDao.insertWords(InitialContentData.vocabularyWords)
                }
                if (quoteDao.countAll() == 0) {
                    quoteDao.insertQuotes(InitialContentData.quotes)
                }
                if (proverbDao.countAll() == 0) {
                    proverbDao.insertProverbs(InitialContentData.proverbs)
                }
                if (idiomDao.countAll() == 0) {
                    idiomDao.insertIdioms(InitialContentData.idioms)
                }
                if (phraseDao.countAll() == 0) {
                    phraseDao.insertPhrases(InitialContentData.phrases)
                }
            }

            // Preference writes follow the successful Room transaction. On retry,
            // all inserts are idempotent and setup remains safe after process death.
            preferencesManager.setFirstLaunchTime(now)
            preferencesManager.setUserId(LOCAL_USER_ID)
            preferencesManager.setVocabularyDifficulty(preferences.vocabularyDifficulty.coerceIn(1, 5))
            preferencesManager.setSelectedWisdomCategories(categories)
            preferencesManager.setPreferredWordCategories(wordCategories)
            preferencesManager.setPracticeSessionSize(preferences.practiceSessionSize.coerceIn(3, 20))
            preferencesManager.setOnboardingCompleted(true)
        }

    private companion object {
        const val LOCAL_USER_ID = "local"
        val DEFAULT_WISDOM_CATEGORIES = setOf("wisdom", "life", "motivation")
        val DEFAULT_WORD_CATEGORIES = setOf(
            "self-improvement", "communication", "mindfulness", "reflection"
        )
    }
}
