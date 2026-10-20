package com.kairos.app.domain.futuremessage

import java.time.LocalDate
import java.time.ZoneId

/** Shared date gate. Delivery workers are a convenience; the timestamp is authority. */
object CapsulePolicy {
    const val MAX_ATTACHMENTS = 6
    const val MAX_ATTACHMENT_BYTES = 25L * 1024 * 1024
    fun canOpen(deliveryAt: Long, now: Long): Boolean = deliveryAt <= now
    fun deliveryAt(date: LocalDate, zone: ZoneId): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()
    fun validate(content: String, deliveryAt: Long, now: Long, attachments: Int): String? = when {
        content.isBlank() -> "Write a little something for your future self."
        content.length > 20000 -> "Keep this letter under 20,000 characters."
        deliveryAt <= now -> "Choose a day in the future."
        attachments !in 0..MAX_ATTACHMENTS -> "Each capsule holds up to six attachments."
        else -> null
    }
}
