package com.kairos.app.domain.futuremessage

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** Material's picker encodes calendar days at UTC midnight. Deliver at 9 am locally. */
object CapsuleDeliveryDate {
    fun fromPicker(utcMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
            .atTime(9, 0).atZone(zone).toInstant().toEpochMilli()
}
