package com.kairos.app.ui.garden

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.kairos.app.data.auth.UserIdProvider
import com.kairos.app.data.local.dao.QuoteDao
import com.kairos.app.data.local.preferences.PreferencesManager
import com.kairos.app.domain.common.ErrorType
import com.kairos.app.domain.common.Result
import com.kairos.app.domain.model.DailyPlan
import com.kairos.app.domain.repository.*
import com.kairos.app.notification.NotificationScheduler
import com.kairos.app.util.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class GardenViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private val vocabulary = mockk<VocabularyRepository>(relaxed=true)
    private val quotes = mockk<QuoteDao>(relaxed=true)
    private val capsules = mockk<FutureMessageRepository>(relaxed=true)
    private val daily = mockk<DailyPlanRepository>(relaxed=true)
    private val user = mockk<UserIdProvider>()
    private val attachments = mockk<CapsuleAttachmentStore>(relaxed=true)
    private val scheduler = mockk<NotificationScheduler>(relaxed=true)
    private val preferences = mockk<PreferencesManager>()
    private val onboarding = mockk<OnboardingRepository>(relaxed=true)

    private fun model(state: SavedStateHandle = SavedStateHandle()): GardenViewModel {
        every { vocabulary.getAllWords() } returns flowOf(emptyList())
        every { quotes.getAllQuotes() } returns flowOf(emptyList())
        every { capsules.observePendingMessages() } returns flowOf(emptyList())
        every { capsules.observeDeliveredMessages() } returns flowOf(emptyList())
        every { preferences.onboardingCompleted } returns flowOf(true)
        every { user.getCurrentUserId() } returns "local"
        coEvery { daily.getOrCreateDailyPlan(any(),any(),any()) } returns DailyPlan(LocalDate.now(),null,null,2)
        coEvery { capsules.getMessagesReadyForDelivery(any()) } returns Result.Success(emptyList())
        return GardenViewModel(vocabulary,quotes,capsules,daily,user,attachments,scheduler,preferences,onboarding,state)
    }

    @Test fun `invalid letter never reaches storage`() = runTest {
        val vm=model();advanceUntilIdle()
        vm.seal("A note"," ",LocalDate.now().plusDays(1));advanceUntilIdle()
        coVerify(exactly=0) { capsules.createMessage(any()) }
        assertNotNull(vm.error.value)
        assertFalse(vm.sealed.value)
    }

    @Test fun `failed save keeps attachment draft and allows retry`() = runTest {
        val picked=mockk<Uri>()
        coEvery { attachments.keep(picked) } returns "content://kairos/capsules/kept.png"
        coEvery { capsules.createMessage(any()) } returns Result.Error(Exception("disk full"),"Could not seal",ErrorType.DATABASE)
        val state=SavedStateHandle();val vm=model(state);advanceUntilIdle()
        vm.addAttachments(listOf(picked));advanceUntilIdle()
        vm.seal("A note","Remember this day",LocalDate.now().plusDays(1));advanceUntilIdle()
        assertEquals(1,vm.draftFiles.value.size)
        assertFalse(vm.sealed.value)
        assertFalse(vm.sealing.value)
        assertEquals(1,state.get<ArrayList<String>>("capsule_files")?.size)
        verify(exactly=0) { scheduler.scheduleFutureMessage(any(),any(),any(),any()) }
    }

    @Test fun `notification failure cannot undo saved letter or create duplicate`() = runTest {
        coEvery { capsules.createMessage(any()) } returns Result.Success(42L)
        every { scheduler.scheduleFutureMessage(any(),any(),any(),any()) } throws SecurityException("denied")
        val state=SavedStateHandle();val vm=model(state);advanceUntilIdle()
        vm.seal("A note","Remember this day",LocalDate.now().plusDays(1));advanceUntilIdle()
        vm.seal("A note","Remember this day",LocalDate.now().plusDays(1));advanceUntilIdle()
        coVerify(exactly=1) { capsules.createMessage(any()) }
        assertTrue(vm.sealed.value)
        assertEquals(true,state.get<Boolean>("capsule_sealed"))
        assertTrue(vm.draftFiles.value.isEmpty())
    }
}
