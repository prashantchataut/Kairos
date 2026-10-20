package com.kairos.app.domain.futuremessage

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class CapsuleDeliveryDateTest {
    @Test fun `Nepal picker day arrives at nine locally without shifting date`() {
        val picker = Instant.parse("2026-10-21T00:00:00Z").toEpochMilli()
        assertEquals(Instant.parse("2026-10-21T03:15:00Z").toEpochMilli(), CapsuleDeliveryDate.fromPicker(picker, ZoneId.of("Asia/Kathmandu")))
    }
    @Test fun `negative timezone preserves chosen calendar day`() {
        val picker = Instant.parse("2026-10-21T00:00:00Z").toEpochMilli()
        val local = Instant.ofEpochMilli(CapsuleDeliveryDate.fromPicker(picker, ZoneId.of("America/Los_Angeles"))).atZone(ZoneId.of("America/Los_Angeles"))
        assertEquals(21, local.dayOfMonth)
        assertEquals(9, local.hour)
    }
    @Test fun `daylight savings uses offset on delivery day`() {
        val picker = Instant.parse("2026-11-01T00:00:00Z").toEpochMilli()
        assertEquals(Instant.parse("2026-11-01T14:00:00Z").toEpochMilli(), CapsuleDeliveryDate.fromPicker(picker, ZoneId.of("America/New_York")))
    }
}
