package com.prody.prashant.release

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.prody.prashant.data.local.database.ProdyDatabase
import com.prody.prashant.data.local.entity.JournalEntryEntity
import com.prody.prashant.data.network.NetworkState
import com.prody.prashant.data.network.NetworkStatus
import com.prody.prashant.data.network.NetworkType
import com.prody.prashant.data.onboarding.AiOnboardingManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReleaseGatingSmokeSuiteTest {

    private var db: ProdyDatabase? = null

    @After
    fun tearDown() {
        db?.close()
    }

    @Test
    fun startupSmoke_databaseBootstrapsWithCoreDaos() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ProdyDatabase::class.java
        ).build()

        assertTrue(db != null)
        assertTrue(db!!.journalDao() != null)
        assertTrue(db!!.userDao() != null)
    }

    @Test
    fun onboardingSmoke_hintStateTransitions() = runBlocking {
        val manager = AiOnboardingManager(ApplicationProvider.getApplicationContext())
        manager.resetAllHints()

        assertTrue(manager.shouldShowBuddhaGuide().first())

        manager.markBuddhaGuideShown()
        assertFalse(manager.shouldShowBuddhaGuide().first())
    }

    @Test
    fun syncRecoverySmoke_networkHeuristicsAllowWifiRecovery() {
        val recoveredState = NetworkState(
            status = NetworkStatus.AVAILABLE,
            type = NetworkType.WIFI,
            isMetered = false,
            isRoaming = false
        )

        assertTrue(recoveredState.isConnected)
        assertTrue(recoveredState.shouldSync)
    }

    @Test
    fun dataPersistenceSmoke_entrySurvivesReopen() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val dbName = "release_gating_persistence_smoke"

        Room.databaseBuilder(context, ProdyDatabase::class.java, dbName)
            .build()
            .use { initialDb ->
                val id = initialDb.journalDao().insertEntry(
                    JournalEntryEntity(
                        content = "persistence smoke",
                        mood = "Calm",
                        createdAt = 1234L,
                        userId = "default_user"
                    )
                )
                assertTrue(id > 0)
            }

        Room.databaseBuilder(context, ProdyDatabase::class.java, dbName)
            .build()
            .use { reopenedDb ->
                val entries = reopenedDb.journalDao().getAllEntriesSync()
                assertEquals(1, entries.size)
                assertEquals("persistence smoke", entries.first().content)
            }

        context.deleteDatabase(dbName)
    }
}
