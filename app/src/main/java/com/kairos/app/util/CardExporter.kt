package com.kairos.app.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Writes rendered share cards to the share sheet or the gallery. */
object CardExporter {
    private fun fileName(slug: String) = "kairos-${slug.lowercase().replace(Regex("[^a-z0-9]+"), "-").take(32)}-${System.currentTimeMillis() / 1000}.png"

    suspend fun share(context: Context, bitmap: Bitmap, slug: String, text: String) {
        val file = withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "cards").apply { mkdirs(); listFiles()?.forEach { it.delete() } }
            File(dir, fileName(slug)).also { f -> f.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } }
        }
        val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
        val send = Intent(Intent.ACTION_SEND)
            .setType("image/png")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .putExtra(Intent.EXTRA_TEXT, text)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(send, "Share your card").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Saves into Pictures/Kairos. Returns false on devices older than Android 10, where we only share. */
    suspend fun saveToGallery(context: Context, bitmap: Bitmap, slug: String): Boolean = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@withContext false
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName(slug))
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Kairos")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@withContext false
        runCatching {
            resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }.isSuccess
    }
}
