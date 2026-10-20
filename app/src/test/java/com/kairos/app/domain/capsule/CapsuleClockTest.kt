package com.kairos.app.domain.capsule

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class CapsuleClockTest {
    @Test fun pickerDayArrivesAtNineInKathmandu() {
        val selected = Instant.parse("2026-10-24T00:00:00Z").toEpochMilli()
        val delivery = CapsuleClock.deliveryOnSelectedDay(selected, ZoneId.of("Asia/Kathmandu"))
        assertEquals(Instant.parse("2026-10-24T03:15:00Z").toEpochMilli(), delivery)
    }
    @Test fun negativeOffsetDoesNotDeliverPreviousCalendarDay() {
        val selected = Instant.parse("2026-10-24T00:00:00Z").toEpochMilli()
        val delivery = CapsuleClock.deliveryOnSelectedDay(selected, ZoneId.of("America/Los_Angeles"))
        assertEquals(Instant.parse("2026-10-24T16:00:00Z").toEpochMilli(), delivery)
    }
    @Test fun revealOpensOnlyAtTheChosenInstant() {
        assertFalse(CapsuleClock.canOpen(1_000, 999))
        assertTrue(CapsuleClock.canOpen(1_000, 1_000))
        assertTrue(CapsuleClock.canOpen(1_000, 1_001))
    }
}
