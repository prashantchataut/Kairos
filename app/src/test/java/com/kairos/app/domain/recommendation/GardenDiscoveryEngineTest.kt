package com.kairos.app.domain.recommendation

import org.junit.Assert.*
import org.junit.Test

class GardenDiscoveryEngineTest {
    private fun item(id: Long, score: Double) = RankedCandidate(
        RecommendationCandidate(id,DailyContentType.VOCABULARY,"reflection","noun"),score,"",ScoreBreakdown())
    private fun visit(id: Long, at: Long, type: DailyContentType = DailyContentType.VOCABULARY) = CandidateHistory(at,id,type,"reflection","noun")
    @Test fun `unseen discovery wins even when a repeat ranks higher`() {
        val context=RecommendationContext("local",1,100,recentSelections=listOf(visit(1,99)))
        assertEquals(2L,GardenDiscoveryEngine.choose(listOf(item(1,10.0),item(2,1.0)),context)?.candidate?.id)
    }
    @Test fun `an exhausted catalog returns the least recently seen word`() {
        val context=RecommendationContext("local",1,100,recentSelections=listOf(visit(1,99),visit(2,20),visit(2,80),visit(3,30)))
        assertEquals(3L,GardenDiscoveryEngine.choose(listOf(item(1,10.0),item(2,2.0),item(3,1.0)),context)?.candidate?.id)
    }
    @Test fun `quote IDs do not exclude words with matching IDs`() {
        val context=RecommendationContext("local",1,100,recentSelections=listOf(visit(1,99,DailyContentType.QUOTE)))
        assertEquals(1L,GardenDiscoveryEngine.choose(listOf(item(1,10.0),item(2,1.0)),context)?.candidate?.id)
        assertNull(GardenDiscoveryEngine.choose(emptyList(),context))
    }
}
