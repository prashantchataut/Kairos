package com.kairos.app.data.capsule

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.kairos.app.data.db.Capsule
import com.kairos.app.data.db.CapsuleAttachment
import com.kairos.app.data.db.KairosDatabase
import com.kairos.app.notify.CapsuleScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Something picked in the composer, before it is copied into the capsule. */
data class PendingAttachment(
    val uri: Uri,
    val name: String,
    val mime: String,
    val bytes: Long,
    val kind: String,
)

/**
 * Time capsules. Attachments are copied into app-private storage when sealed, so they stay
 * with the letter even if the original photo is deleted, and nothing can peek before the date.
 */
class CapsuleRepository(
    private val context: Context,
    private val db: KairosDatabase,
    private val scheduler: CapsuleScheduler,
) {
    val capsules: Flow<List<Capsule>> = db.capsules().observeAll()
    val attachmentCounts = db.capsules().observeAttachmentCounts()
    fun observe(id: Long) = db.capsules().observe(id)
    fun observeAttachments(id: Long) = db.capsules().observeAttachments(id)

    fun describe(uri: Uri, fallbackKind: String? = null): PendingAttachment {
        val cr = context.contentResolver
        var name = "attachment"
        var size = 0L
        cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                name = c.getString(0) ?: name
                size = if (c.isNull(1)) 0L else c.getLong(1)
            }
        }
        val mime = cr.getType(uri) ?: "application/octet-stream"
        val kind = fallbackKind ?: when {
            mime.startsWith("image/") -> "image"
            mime.startsWith("audio/") -> "audio"
            mime.startsWith("video/") -> "video"
            else -> "file"
        }
        return PendingAttachment(uri, name, mime, size, kind)
    }

    /** A fresh file for a voice note recorded inside the composer. */
    fun newVoiceFile(): File {
        val dir = File(context.cacheDir, "voice").apply { mkdirs() }
        return File(dir, "voice-${System.currentTimeMillis()}.m4a")
    }

    suspend fun seal(
        title: String,
        body: String,
        unlockAt: Long,
        mood: String?,
        paper: Int,
        attachments: List<PendingAttachment>,
    ): Long = withContext(Dispatchers.IO) {
        val folder = File(context.filesDir, "capsules/${UUID.randomUUID()}").apply { mkdirs() }
        val copied = attachments.mapIndexed { i, a ->
            val safe = a.name.replace(Regex("[^A-Za-z0-9._-]"), "_").takeLast(60)
            val out = File(folder, "${i}_$safe")
            context.contentResolver.openInputStream(a.uri)?.use { input ->
                out.outputStream().use { input.copyTo(it) }
            }
            CapsuleAttachment(capsuleId = 0, kind = a.kind, path = out.absolutePath, name = a.name, mime = a.mime, bytes = out.length())
        }
        val id = db.capsules().insertWithAttachments(
            Capsule(
                title = title.trim().ifEmpty { "A letter to future me" },
                body = body,
                createdAt = System.currentTimeMillis(),
                unlockAt = unlockAt,
                moodAtWriting = mood,
                paper = paper,
            ),
            copied,
        )
        scheduler.schedule(id, title, unlockAt)
        id
    }

    suspend fun open(id: Long) {
        val c = db.capsules().get(id) ?: return
        if (System.currentTimeMillis() >= c.unlockAt) db.capsules().markOpened(id, System.currentTimeMillis())
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        db.capsules().attachments(id).forEach { File(it.path).delete() }
        db.capsules().attachments(id).firstOrNull()?.let { File(it.path).parentFile?.delete() }
        db.capsules().deleteAttachments(id)
        db.capsules().delete(id)
        scheduler.cancel(id)
    }

    /** Re-arms notifications, e.g. after an app update cleared WorkManager. */
    suspend fun rescheduleAll() {
        db.capsules().pendingAfter(System.currentTimeMillis()).forEach { scheduler.schedule(it.id, it.title, it.unlockAt) }
    }
}
