package com.kairos.app.domain.recommendation

import org.junit.Assert.*
import org.junit.Test

class DailyFeedbackEngineTest {
    private val now = 1_800_000_000_000L
    private fun signal(type: DailyContentType = DailyContentType.QUOTE, event: ContentInteractionType = ContentInteractionType.SAVED, time: Long = now) =
        FeedbackSignal(type, event, " Growth ", "Author", time)
    @Test fun `quote interests never leak into vocabulary`() {
        val profile = DailyFeedbackEngine.profile(DailyContentType.VOCABULARY, listOf(signal()), now)
        assertTrue(profile.categories.isEmpty())
    }
    @Test fun `signals halve in influence after thirty days`() {
        val fresh = DailyFeedbackEngine.profile(DailyContentType.QUOTE, listOf(signal()), now).categories.getValue("growth")
        val old = DailyFeedbackEngine.profile(DailyContentType.QUOTE, listOf(signal(time = now - 30L * 86_400_000)), now).categories.getValue("growth")
        assertEquals(fresh / 2, old, .0001)
    }
    @Test fun `unsave reverses a save at the same time`() {
        val profile = DailyFeedbackEngine.profile(DailyContentType.QUOTE, listOf(signal(), signal(event = ContentInteractionType.UNSAVED)), now)
        assertEquals(0.0, profile.categories.getValue("growth"), .0001)
    }
    @Test fun `future events are ignored and repeated feedback is bounded`() {
        val profile = DailyFeedbackEngine.profile(DailyContentType.QUOTE, List(100) { signal() } + signal(event = ContentInteractionType.LESS_LIKE_THIS, time = now + 1), now)
        assertEquals(10.0, profile.categories.getValue("growth"), .0001)
    }
    @Test fun `difficulty feedback is isolated to word family`() {
        val profile = DailyFeedbackEngine.profile(DailyContentType.QUOTE, listOf(signal(event = ContentInteractionType.TOO_HARD)), now)
        assertEquals(0, profile.difficultyDelta)
    }
}
