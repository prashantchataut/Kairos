package com.kairos.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ShareManager - Utility for sharing content from Kairos
 *
 * Features:
 * - Create shareable card images with message content
 * - Capture Compose views as bitmaps
 * - Launch system share sheet with images or text
 * - Support for different share card styles (future messages, quotes, achievements)
 *
 * Usage:
 * ```
 * shareManager.shareFutureMessage(context, message)
 * shareManager.shareQuote(context, quote, author)
 * shareManager.shareAchievement(context, achievement)
 * ```
 */
@Singleton
class ShareManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ShareManager"
        private const val SHARE_FOLDER = "share_images"
        private const val FILE_PROVIDER_AUTHORITY = "com.kairos.app.fileprovider"

        // Card dimensions (optimized for social media)
        private const val CARD_WIDTH = 1080
        private const val CARD_HEIGHT = 1920
        private const val CARD_PADDING = 80
        private const val CORNER_RADIUS = 48f

        // Brand colors
        private const val Kairos_GREEN = 0xFF36F97F.toInt()
        private const val CARD_BG_DARK = 0xFF0A1F1C.toInt()
        private const val CARD_BG_LIGHT = 0xFFF8F9FA.toInt()
        private const val TEXT_PRIMARY_DARK = 0xFFFFFFFF.toInt()
        private const val TEXT_PRIMARY_LIGHT = 0xFF1A1A1A.toInt()
        private const val TEXT_SECONDARY_DARK = 0xFFB8C4C2.toInt()
        private const val TEXT_SECONDARY_LIGHT = 0xFF6B7280.toInt()
    }

    /**
     * Share a future message as an image card.
     * Creates a beautiful shareable card with the message content and Kairos branding.
     *
     * @param title The message title
     * @param content The message content
     * @param deliveryDate When the message was delivered (or will be delivered)
     * @param isDarkTheme Whether to use dark theme styling
     * @return true if share was initiated successfully
     */
    fun shareFutureMessage(
        title: String,
        content: String,
        deliveryDate: Long,
        isDarkTheme: Boolean = true
    ): Boolean {
        return try {
            val bitmap = createFutureMessageCard(title, content, deliveryDate, isDarkTheme)
            val uri = saveBitmapToCache(bitmap, "future_message_${System.currentTimeMillis()}.png")

            if (uri != null) {
                launchShareSheet(
                    imageUri = uri,
                    text = "A message from my past self \n\n\"$content\"\n\nCreated with Kairos - Time Capsule",
                    title = "Share Time Capsule"
                )
                true
            } else {
                Log.e(TAG, "Failed to save bitmap to cache")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share future message", e)
            false
        }
    }

    /**
     * Share a quote as an image card.
     */
    fun shareQuote(
        quote: String,
        author: String,
        isDarkTheme: Boolean = true
    ): Boolean {
        return try {
            val bitmap = createQuoteCard(quote, author, isDarkTheme)
            val uri = saveBitmapToCache(bitmap, "quote_${System.currentTimeMillis()}.png")

            if (uri != null) {
                launchShareSheet(
                    imageUri = uri,
                    text = "\"$quote\"\n\n— $author\n\nShared via Kairos",
                    title = "Share Quote"
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share quote", e)
            false
        }
    }

    /**
     * Share a vocabulary word as an image card.
     */
    fun shareVocabularyWord(
        word: String,
        definition: String,
        pronunciation: String? = null,
        isDarkTheme: Boolean = true
    ): Boolean {
        return try {
            val bitmap = createVocabularyCard(word, definition, pronunciation, isDarkTheme)
            val uri = saveBitmapToCache(bitmap, "word_${System.currentTimeMillis()}.png")

            if (uri != null) {
                launchShareSheet(
                    imageUri = uri,
                    text = "$word - $definition\n\nLearn more words with Kairos",
                    title = "Share Word"
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share vocabulary word", e)
            false
        }
    }

    fun shareWordPrint(word: String, definition: String, pronunciation: String?, paper: com.kairos.app.ui.garden.WordPrintRenderer.Paper): Boolean {
        return try {
            val bitmap = com.kairos.app.ui.garden.WordPrintRenderer.render(word, definition, pronunciation, paper)
            val uri = try { saveBitmapToCache(bitmap, "word_print_${System.currentTimeMillis()}.png") }
                finally { bitmap.recycle() }
            if (uri == null) false else {
                launchShareSheet(uri, "$word — $definition\n\nA Kairos Word Print", "Share Word Print")
                true
            }
        } catch (e: Exception) { Log.e(TAG, "Word Print export failed", e); false }
    }

    /**
     * Share plain text without an image.
     */
    fun shareText(
        text: String,
        title: String = "Share"
    ): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share text", e)
            false
        }
    }

    /**
     * Launch the system share sheet with an image and optional text.
     */
    private fun launchShareSheet(
        imageUri: Uri,
        text: String? = null,
        title: String = "Share"
    ) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            text?.let { putExtra(Intent.EXTRA_TEXT, it) }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    /**
     * Save a bitmap to the cache directory and return its content URI.
     */
    private fun saveBitmapToCache(bitmap: Bitmap, filename: String): Uri? {
        return try {
            val cacheDir = File(context.cacheDir, SHARE_FOLDER)
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            // Clean up old share files (keep last 10)
            cleanupOldFiles(cacheDir, 10)

            val file = File(cacheDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save bitmap to cache", e)
            null
        }
    }

    /**
     * Clean up old share files to prevent cache bloat.
     */
    private fun cleanupOldFiles(directory: File, keepCount: Int) {
        try {
            val files = directory.listFiles()?.sortedByDescending { it.lastModified() }
            files?.drop(keepCount)?.forEach { it.delete() }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cleanup old files", e)
        }
    }

    // ==========================================================================
    // CARD CREATION METHODS
    // ==========================================================================

    /**
     * Create a shareable card for a future message.
     */
    private fun createFutureMessageCard(
        title: String,
        content: String,
        deliveryDate: Long,
        isDarkTheme: Boolean
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgColor = if (isDarkTheme) CARD_BG_DARK else CARD_BG_LIGHT
        val primaryTextColor = if (isDarkTheme) TEXT_PRIMARY_DARK else TEXT_PRIMARY_LIGHT
        val secondaryTextColor = if (isDarkTheme) TEXT_SECONDARY_DARK else TEXT_SECONDARY_LIGHT

        // Draw background
        canvas.drawColor(bgColor)

        // Draw decorative accent arc at top
        val accentPaint = Paint().apply {
            color = Kairos_GREEN
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawArc(
            RectF(-200f, -400f, CARD_WIDTH + 200f, 400f),
            0f, 180f, false, accentPaint
        )

        // Draw "TIME CAPSULE" label
        val labelPaint = Paint().apply {
            color = Kairos_GREEN
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("TIME CAPSULE", CARD_PADDING.toFloat(), 200f, labelPaint)

        // Draw date
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val dateText = "Delivered ${dateFormat.format(Date(deliveryDate))}"
        val datePaint = Paint().apply {
            color = secondaryTextColor
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText(dateText, CARD_PADDING.toFloat(), 260f, datePaint)

        // Draw title if present
        var yOffset = 400f
        if (title.isNotBlank()) {
            val titlePaint = Paint().apply {
                color = primaryTextColor
                textSize = 48f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            yOffset = drawWrappedText(canvas, title, CARD_PADDING.toFloat(), yOffset,
                CARD_WIDTH - CARD_PADDING * 2, titlePaint)
            yOffset += 40f
        }

        // Draw content
        val contentPaint = Paint().apply {
            color = primaryTextColor
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        drawWrappedText(canvas, "\"$content\"", CARD_PADDING.toFloat(), yOffset,
            CARD_WIDTH - CARD_PADDING * 2, contentPaint)

        // Draw Kairos branding at bottom
        drawKairosBranding(canvas, isDarkTheme)

        return bitmap
    }

    /**
     * Create a shareable card for a quote.
     */
    private fun createQuoteCard(
        quote: String,
        author: String,
        isDarkTheme: Boolean
    ): Bitmap {
        return com.kairos.app.ui.garden.WordPrintRenderer.render(
            "A thought to carry", "“$quote”\n— $author", "THE THOUGHT COLLECTION"
        )
    }

    /**
     * Create a shareable card for a vocabulary word.
     */
    private fun createVocabularyCard(
        word: String,
        definition: String,
        pronunciation: String?,
        isDarkTheme: Boolean
    ): Bitmap {
        return com.kairos.app.ui.garden.WordPrintRenderer.render(word, definition, pronunciation)
    }

    /**
     * Draw Kairos branding at the bottom of a card.
     */
    private fun drawKairosBranding(canvas: Canvas, isDarkTheme: Boolean) {
        val secondaryTextColor = if (isDarkTheme) TEXT_SECONDARY_DARK else TEXT_SECONDARY_LIGHT

        // Draw horizontal line
        val linePaint = Paint().apply {
            color = Kairos_GREEN
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawLine(
            CARD_PADDING.toFloat(),
            CARD_HEIGHT - 200f,
            CARD_WIDTH - CARD_PADDING.toFloat(),
            CARD_HEIGHT - 200f,
            linePaint
        )

        // Draw "Kairos" text
        val brandPaint = Paint().apply {
            color = Kairos_GREEN
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Kairos", CARD_PADDING.toFloat(), CARD_HEIGHT - 120f, brandPaint)

        // Draw tagline
        val taglinePaint = Paint().apply {
            color = secondaryTextColor
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("Your growth companion", CARD_PADDING.toFloat(), CARD_HEIGHT - 70f, taglinePaint)
    }

    /**
     * Draw text that wraps within a specified width.
     * Returns the Y position after the last line.
     */
    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Int,
        paint: Paint
    ): Float {
        val words = text.split(" ")
        var currentLine = StringBuilder()
        var yPos = startY
        val lineHeight = paint.textSize * 1.4f

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val testWidth = paint.measureText(testLine)

            if (testWidth > maxWidth && currentLine.isNotEmpty()) {
                canvas.drawText(currentLine.toString(), x, yPos, paint)
                yPos += lineHeight
                currentLine = StringBuilder(word)
            } else {
                currentLine = StringBuilder(testLine)
            }
        }

        // Draw the last line
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), x, yPos, paint)
            yPos += lineHeight
        }

        return yPos
    }
}
