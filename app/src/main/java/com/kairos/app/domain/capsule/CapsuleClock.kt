package com.kairos.app.domain.capsule

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** Material date pickers encode a calendar day in UTC, not a delivery instant. */
object CapsuleClock {
    fun deliveryOnSelectedDay(utcDateMillis: Long, zone: ZoneId): Long =
        Instant.ofEpochMilli(utcDateMillis).atZone(ZoneOffset.UTC).toLocalDate()
            .atTime(9, 0).atZone(zone).toInstant().toEpochMilli()

    fun canOpen(deliveryAt: Long, now: Long): Boolean = now >= deliveryAt
}
