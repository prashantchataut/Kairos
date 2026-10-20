package com.kairos.app.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.util.UUID

/**
 * Attachments for capsules.
 *
 * A capsule must survive the photo picker's temporary grant expiring, so files are copied into
 * the app's private storage at seal time. The stored path is relative, which keeps exports and
 * device restores portable.
 */
class AttachmentStore(private val context: Context) {

    private val root: File get() = File(context.filesDir, "capsules").apply { mkdirs() }

    fun import(capsuleId: String, uri: Uri): Attachment? {
        val mime = context.contentResolver.getType(uri) ?: "image/*"
        val kind = when {
            mime.startsWith("image/") -> "image"
            mime.startsWith("audio/") -> "audio"
            else -> "file"
        }
        val extension = when {
            mime.contains("png") -> "png"
            mime.contains("webp") -> "webp"
            mime.contains("jpeg") || mime.contains("jpg") -> "jpg"
            mime.contains("heic") -> "heic"
            mime.contains("mpeg") || mime.contains("mp3") -> "mp3"
            mime.contains("mp4") -> "mp4"
            else -> "bin"
        }
        val displayName = displayName(uri) ?: "attachment.$extension"
        val dir = File(root, capsuleId).apply { mkdirs() }
        val target = File(dir, "${UUID.randomUUID()}.$extension")
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            Attachment(
                relativePath = "capsules/$capsuleId/${target.name}",
                mimeType = mime,
                displayName = displayName,
                kind = kind,
            )
        }.getOrNull()
    }

    fun file(attachment: Attachment): File = File(context.filesDir, attachment.relativePath)

    fun deleteFor(capsuleId: String) {
        File(root, capsuleId).deleteRecursively()
    }

    private fun displayName(uri: Uri): String? =
        runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            }
        }.getOrNull()
}
