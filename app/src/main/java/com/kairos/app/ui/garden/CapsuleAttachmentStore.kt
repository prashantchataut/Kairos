package com.kairos.app.ui.garden

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.kairos.app.domain.futuremessage.CapsulePolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Copies picked media now, while the temporary picker grant is still valid. */
@Singleton
class CapsuleAttachmentStore @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun keep(uri: Uri): String = withContext(Dispatchers.IO) {
        val directory = File(context.filesDir, "capsules").apply { mkdirs() }
        val mime = context.contentResolver.getType(uri).orEmpty()
        require(mime.startsWith("image/") || mime.startsWith("video/") || mime.startsWith("audio/") || mime == "application/pdf") {
            "Choose an image, video, audio recording or PDF."
        }
        val extension = "." + (android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
            ?: error("This file format isn't supported. Choose another keepsake."))
        val file = File(directory, UUID.randomUUID().toString() + extension)
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(8192); var total = 0L
                    while (true) {
                        val count = input.read(buffer); if (count < 0) break
                        total += count
                        require(total <= CapsulePolicy.MAX_ATTACHMENT_BYTES) { "Attachments must be 25 MB or smaller." }
                        output.write(buffer, 0, count)
                    }
                    require(total > 0) { "This file is empty. Please choose another." }
                }
            } ?: error("This attachment could not be read. Please choose it again.")
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file).toString()
        } catch (e: Exception) { file.delete(); throw e }
    }

    suspend fun discard(uri: String) = withContext(Dispatchers.IO) {
        val parsed = Uri.parse(uri)
        if (parsed.authority == "${context.packageName}.fileprovider") {
            val root = File(context.filesDir, "capsules").canonicalFile
            val candidate = File(root, parsed.lastPathSegment.orEmpty()).canonicalFile
            if (candidate.parentFile == root) candidate.delete()
        }
    }
}
