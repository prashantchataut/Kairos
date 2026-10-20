package com.kairos.app.data.repo

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.kairos.app.data.db.AttachmentEntity
import com.kairos.app.data.db.CapsuleEntity
import com.kairos.app.data.db.KairosDatabase
import com.kairos.app.work.CapsuleScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

enum class AttachmentKind { Image, Video, Audio, File;
    companion object {
        fun of(mime: String) = when {
            mime.startsWith("image/") -> Image
            mime.startsWith("video/") -> Video
            mime.startsWith("audio/") -> Audio
            else -> File
        }
    }
}

/** An attachment picked in the composer but not sealed yet. */
data class DraftAttachment(val uri: Uri, val name: String, val mime: String, val size: Long) {
    val kind get() = AttachmentKind.of(mime)
}

data class CapsuleDraft(
    val title: String,
    val letter: String,
    val opensAt: Long,
    val mood: String?,
    val tint: String,
    val attachments: List<DraftAttachment>,
)

/**
 * The time capsule. Letters and attachments live only in app-private storage; attachments are
 * copied at seal time so a letter keeps working even if the original photo is later deleted.
 */
class CapsuleRepository(
    private val context: Context,
    private val db: KairosDatabase,
    private val kairos: KairosRepository,
) {
    private val root get() = File(context.filesDir, "capsules")

    fun observeAll() = db.capsules().observeAll()
    fun observe(id: Long) = db.capsules().observe(id)

    fun describe(uri: Uri): DraftAttachment {
        val resolver = context.contentResolver
        var name = "Attachment"
        var size = 0L
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                c.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { name = c.getString(it) ?: name }
                c.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 }?.let { size = c.getLong(it) }
            }
        }
        val mime = resolver.getType(uri) ?: "application/octet-stream"
        return DraftAttachment(uri, name, mime, size)
    }

    suspend fun seal(draft: CapsuleDraft): Long = withContext(Dispatchers.IO) {
        val id = db.capsules().insert(
            CapsuleEntity(
                title = draft.title.trim(),
                letter = draft.letter.trim(),
                createdAt = System.currentTimeMillis(),
                opensAt = draft.opensAt,
                moodAtSeal = draft.mood,
                tint = draft.tint,
            )
        )
        val dir = File(root, id.toString()).apply { mkdirs() }
        val stored = draft.attachments.mapNotNull { a ->
            val ext = a.name.substringAfterLast('.', "").take(8).let { if (it.isBlank()) "" else ".$it" }
            val target = File(dir, UUID.randomUUID().toString() + ext)
            runCatching {
                context.contentResolver.openInputStream(a.uri)?.use { input ->
                    target.outputStream().use { input.copyTo(it) }
                } ?: return@mapNotNull null
                AttachmentEntity(
                    capsuleId = id, kind = a.kind.name, path = target.absolutePath,
                    displayName = a.name, mime = a.mime, sizeBytes = target.length(),
                )
            }.getOrNull()
        }
        if (stored.isNotEmpty()) db.capsules().insertAttachments(stored)
        CapsuleScheduler.schedule(context, id, draft.opensAt)
        kairos.addMoment(MomentKind.Letter, draft.title.ifBlank { "A letter" }, oncePerDay = false)
        id
    }

    suspend fun open(id: Long) {
        val c = db.capsules().get(id)?.capsule ?: return
        if (c.openedAt != null || c.opensAt > System.currentTimeMillis()) return
        db.capsules().update(c.copy(openedAt = System.currentTimeMillis()))
        kairos.addMoment(MomentKind.Opened, c.title.ifBlank { "A letter" }, oncePerDay = false)
    }

    suspend fun saveReply(id: Long, reply: String) {
        val c = db.capsules().get(id)?.capsule ?: return
        db.capsules().update(c.copy(reply = reply.trim()))
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        CapsuleScheduler.cancel(context, id)
        File(root, id.toString()).deleteRecursively()
        db.capsules().delete(id)
    }

    /** Re-creates unlock notifications, e.g. after the app was updated. */
    suspend fun rescheduleAll() {
        db.capsules().sealed().forEach { CapsuleScheduler.schedule(context, it.id, it.opensAt) }
    }
}
