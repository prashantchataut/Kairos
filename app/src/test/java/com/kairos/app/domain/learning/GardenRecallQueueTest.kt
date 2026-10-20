package com.kairos.app.domain.learning

import org.junit.Assert.*
import org.junit.Test

class GardenRecallQueueTest {
    @Test fun `due reviews precede saved words and future reviews wait`() {
        val seeds=listOf(
            GardenRecallQueue.Seed(1,true,false,null),
            GardenRecallQueue.Seed(2,false,true,101),
            GardenRecallQueue.Seed(3,false,true,99),
            GardenRecallQueue.Seed(4,false,false,null)
        )
        assertEquals(listOf(3L,1L,4L),GardenRecallQueue.select(seeds,100))
    }
    @Test fun `queue is stable bounded and has no duplicate words`() {
        val seeds=(1L..30L).map { GardenRecallQueue.Seed(it,false,false,null) }
        assertEquals((1L..10L).toList(),GardenRecallQueue.select(seeds.reversed()+seeds,0))
        assertTrue(GardenRecallQueue.select(seeds,0,-1).isEmpty())
        assertEquals(20,GardenRecallQueue.select(seeds,0,999).size)
    }
}
