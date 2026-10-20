package com.kairos.app.domain.futuremessage

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CapsulePolicyTest {
    @Test fun `letters open at the boundary and never before`() {
        assertFalse(CapsulePolicy.canOpen(1001,1000))
        assertTrue(CapsulePolicy.canOpen(1000,1000))
        assertTrue(CapsulePolicy.canOpen(999,1000))
    }
    @Test fun `date picker day is interpreted in Kathmandu not UTC`() {
        val at=CapsulePolicy.deliveryAt(LocalDate.of(2026,10,21),ZoneId.of("Asia/Kathmandu"))
        assertEquals("2026-10-20T18:15:00Z",java.time.Instant.ofEpochMilli(at).toString())
    }
    @Test fun `DST dates use calendar days rather than 24 hour arithmetic`() {
        val zone=ZoneId.of("America/New_York")
        val a=CapsulePolicy.deliveryAt(LocalDate.of(2026,3,8),zone)
        val b=CapsulePolicy.deliveryAt(LocalDate.of(2026,3,9),zone)
        assertEquals(23*60*60*1000L,b-a)
    }
    @Test fun `sealing requires content future date and bounded attachments`() {
        assertNotNull(CapsulePolicy.validate(" ",2000,1000,0))
        assertNotNull(CapsulePolicy.validate("hello",1000,1000,0))
        assertNotNull(CapsulePolicy.validate("hello",2000,1000,7))
        assertNotNull(CapsulePolicy.validate("a".repeat(20001),2000,1000,0))
        assertNull(CapsulePolicy.validate("hello",2000,1000,6))
    }
}
