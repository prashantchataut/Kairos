package com.kairos.app

import com.kairos.app.data.Capsule
import com.kairos.app.domain.capsule.CapsuleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class CapsuleEngineTest {

    private val now = 1_700_000_000_000L
    private val hour = 3_600_000L
    private val zone = ZoneId.of("Asia/Kathmandu")

    private fun capsule(id: String, unlockAt: Long, openedAt: Long? = null) = Capsule(
        id = id,
        title = "For later",
        body = "Hello, future me.",
        createdAt = now - 10 * 24 * hour,
        unlockAt = unlockAt,
        openedAt = openedAt,
    )

    @Test
    fun `a capsule opens exactly at its unlock moment`() {
        val c = capsule("a", now + hour)
        assertFalse(CapsuleEngine.canOpen(c, now))
        assertTrue(CapsuleEngine.canOpen(c, now + hour))
    }

    @Test
    fun `validation rejects empty letters and near dates`() {
        assertFalse(CapsuleEngine.validate(CapsuleEngine.Draft("", "", now + 5 * hour), now).ok)
        assertFalse(
            CapsuleEngine.validate(
                CapsuleEngine.Draft("Hi", "Body", now + 10 * 60 * 1000L),
                now,
            ).ok,
        )
        assertTrue(
            CapsuleEngine.validate(
                CapsuleEngine.Draft("Hi", "Body", now + 24 * hour),
                now,
            ).ok,
        )
    }

    @Test
    fun `presets are all in the future and respect the one hour lead`() {
        val presets = CapsuleEngine.presets(now, zone)
        assertEquals(5, presets.size)
        assertTrue(presets.all { it.at > now + CapsuleEngine.MIN_LEAD_MS })
        assertEquals(presets.size, presets.map { it.id }.toSet().size)
    }

    @Test
    fun `vault separates ready sealed and opened`() {
        val ready = capsule("ready", now - hour)
        val sealed = capsule("sealed", now + 24 * hour)
        val opened = capsule("opened", now - 48 * hour, openedAt = now - 47 * hour)
        val vault = CapsuleEngine.vault(listOf(sealed, opened, ready), now)
        assertEquals(listOf("ready"), vault.ready.map { it.id })
        assertEquals(listOf("sealed"), vault.sealed.map { it.id })
        assertEquals(listOf("opened"), vault.opened.map { it.id })
    }

    @Test
    fun `next unlock ignores opened capsules`() {
        val opened = capsule("opened", now + hour, openedAt = now)
        val later = capsule("later", now + 10 * hour)
        assertEquals("later", CapsuleEngine.nextUnlock(listOf(opened, later), now)?.id)
        assertNull(CapsuleEngine.nextUnlock(listOf(opened), now))
    }
}
