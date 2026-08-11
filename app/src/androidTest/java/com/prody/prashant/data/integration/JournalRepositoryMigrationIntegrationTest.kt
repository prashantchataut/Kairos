package com.prody.prashant.data.integration

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.prody.prashant.data.local.database.ProdyDatabase
import com.prody.prashant.data.local.entity.JournalEntryEntity
import com.prody.prashant.data.repository.JournalRepositoryImpl
import com.prody.prashant.domain.common.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JournalRepositoryMigrationIntegrationTest {

    private val dbName = "journal_migration_integration_test"

    @get:Rule
    val migrationHelper = MigrationTestHelper(
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation(),
        ProdyDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    private var migratedDb: ProdyDatabase? = null

    @After
    fun tearDown() {
        migratedDb?.close()
    }

    @Test
    fun migration17To20_keepsLegacyRow_andRepositoryPersistsNewEntry() = runBlocking {
        migrationHelper.createDatabase(dbName, 17).apply {
            execSQL(
                """
                INSERT INTO journal_entries (
                    id, userId, title, content, mood, moodIntensity, buddhaResponse, tags,
                    isBookmarked, wordCount, createdAt, updatedAt, aiEmotionLabel, aiThemes,
                    aiInsight, aiSummary, aiInsightGenerated, aiSnippet, aiQuestion, aiSuggestion,
                    aiContentHash, attachedPhotos, attachedVideos, voiceRecordingUri,
                    voiceRecordingDuration, templateId, syncStatus, lastSyncedAt, serverVersion, isDeleted
                ) VALUES (
                    1, 'default_user', 'Legacy entry', 'Migrated content', 'Calm', 3, NULL, '[]',
                    0, 2, 1000, 1000, NULL, NULL,
                    NULL, NULL, 0, NULL, NULL, NULL,
                    NULL, '[]', '[]', NULL,
                    0, NULL, 'synced', NULL, 1, 0
                )
                """.trimIndent()
            )
            close()
        }

        migrationHelper.runMigrationsAndValidate(
            dbName,
            20,
            true,
            ProdyDatabase.MIGRATION_17_18,
            ProdyDatabase.MIGRATION_18_19,
            ProdyDatabase.MIGRATION_19_20
        )

        migratedDb = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ProdyDatabase::class.java,
            dbName
        )
            .addMigrations(
                ProdyDatabase.MIGRATION_17_18,
                ProdyDatabase.MIGRATION_18_19,
                ProdyDatabase.MIGRATION_19_20
            )
            .build()

        val repository = JournalRepositoryImpl(migratedDb!!.journalDao())

        val legacyEntries = repository.getAllEntries().first()
        assertTrue(legacyEntries.any { it.id == 1L && it.content == "Migrated content" })

        val saveResult = repository.saveEntry(
            JournalEntryEntity(
                content = "New entry after migration",
                mood = "Hopeful",
                createdAt = 2000L,
                userId = "default_user"
            )
        )

        assertTrue(saveResult is Result.Success)
        val savedId = (saveResult as Result.Success).data
        assertTrue(savedId > 0)

        val allEntriesAfterSave = repository.getAllEntries().first()
        assertEquals(2, allEntriesAfterSave.size)
    }
}
