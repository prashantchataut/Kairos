package com.kairos.app.domain.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.kairos.app.R
import kotlin.math.min
import kotlin.random.Random

/**
 * The share studio.
 *
 * A poster is drawn with Android's own canvas rather than a screenshot of the UI, so it is
 * resolution-independent, can be exported at print size, and never leaks the app's chrome.
 * Four art directions share one layout grid: kicker, word, phonetic, meaning, signature.
 */
enum class PosterStyle(val id: String, val label: String, val blurb: String) {
    Wash("wash", "Wash", "pigment on wet paper"),
    Paper("paper", "Paper", "a card torn from a notebook"),
    Dusk("dusk", "Dusk", "ink and lamplight"),
    Bloom("bloom", "Bloom", "one enormous flower of colour"),
}

enum class PosterRatio(val id: String, val label: String, val w: Int, val h: Int) {
    Portrait("portrait", "4:5", 4, 5),
    Square("square", "1:1", 1, 1),
    Story("story", "9:16", 9, 16),
}

data class PosterPalette(
    val id: String,
    val label: String,
    val top: Int,
    val bottom: Int,
    val bloomA: Int,
    val bloomB: Int,
    val ink: Int,
    val accent: Int,
)

object PosterPalettes {
    val Apricot = PosterPalette(
        "apricot", "Apricot",
        top = 0xFFFFF6EC.toInt(), bottom = 0xFFFFE2D0.toInt(),
        bloomA = 0xFFFFB07C.toInt(), bloomB = 0xFFF58FB4.toInt(),
        ink = 0xFF2A2130.toInt(), accent = 0xFFE4694C.toInt(),
    )
    val Mint = PosterPalette(
        "mint", "Mint",
        top = 0xFFF2FCF7.toInt(), bottom = 0xFFD9F2E8.toInt(),
        bloomA = 0xFF7FDCBE.toInt(), bloomB = 0xFFA5C4FF.toInt(),
        ink = 0xFF1D2B29.toInt(), accent = 0xFF2C9377.toInt(),
    )
    val Dusk = PosterPalette(
        "dusk", "Dusk",
        top = 0xFF1A1530.toInt(), bottom = 0xFF2B1E3F.toInt(),
        bloomA = 0xFF8C79FF.toInt(), bloomB = 0xFFFF87A5.toInt(),
        ink = 0xFFFFF7EC.toInt(), accent = 0xFFFFC98B.toInt(),
    )
    val Butter = PosterPalette(
        "butter", "Butter",
        top = 0xFFFFFBEF.toInt(), bottom = 0xFFFFEBC4.toInt(),
        bloomA = 0xFFFFD37E.toInt(), bloomB = 0xFFFF9E7A.toInt(),
        ink = 0xFF2F2418.toInt(), accent = 0xFFC1752A.toInt(),
    )

    val all = listOf(Apricot, Mint, Dusk, Butter)
    fun byId(id: String): PosterPalette = all.firstOrNull { it.id == id } ?: Apricot
}

data class PosterContent(
    val kicker: String,
    val title: String,
    val phonetic: String?,
    val body: String,
    val note: String?,
    val signature: String,
)

/**
 * @param widthPx export width; height follows [ratio]. 1080 is a good share size, 2160 prints.
 */
object PosterEngine {

    fun render(
        context: Context,
        style: PosterStyle,
        palette: PosterPalette,
        ratio: PosterRatio,
        content: PosterContent,
        widthPx: Int = 1080,
        seed: Int = 11,
    ): Bitmap {
        val width = widthPx
        val height = (width * ratio.h / ratio.w.toFloat()).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val w = width.toFloat()
        val h = height.toFloat()

        val serif = ResourcesCompat.getFont(context, R.font.instrument_serif) ?: Typeface.SERIF
        val serifItalic = ResourcesCompat.getFont(context, R.font.instrument_serif_italic) ?: Typeface.SERIF
        val sans = ResourcesCompat.getFont(context, R.font.poppins_regular) ?: Typeface.SANS_SERIF
        val sansMedium = ResourcesCompat.getFont(context, R.font.poppins_medium) ?: Typeface.SANS_SERIF
        val hand = ResourcesCompat.getFont(context, R.font.caveat) ?: Typeface.SANS_SERIF

        when (style) {
            PosterStyle.Wash -> drawWash(canvas, w, h, palette, serif, sans, sansMedium, hand, content, seed)
            PosterStyle.Paper -> drawPaper(canvas, w, h, palette, serif, sans, sansMedium, hand, content, seed)
            PosterStyle.Dusk -> drawDusk(canvas, w, h, palette, serif, sans, sansMedium, hand, content, seed)
            PosterStyle.Bloom -> drawBloom(canvas, w, h, palette, serif, sans, sansMedium, hand, content, seed)
        }

        drawGrain(canvas, w, h, seed, if (isDark(palette)) 0.05f else 0.07f)
        val signatureY = if (style == PosterStyle.Paper) 0.855f else 0.945f
        drawSignature(canvas, w, h, palette, sansMedium, content.signature, signatureY)
        return bitmap
    }

    // ------------------------------------------------------------------ styles

    private fun drawWash(
        canvas: Canvas, w: Float, h: Float, palette: PosterPalette,
        serif: Typeface, sans: Typeface, sansMedium: Typeface, hand: Typeface,
        content: PosterContent, seed: Int,
    ) {
        fillVertical(canvas, w, h, palette.top, palette.bottom)
        bloom(canvas, w * 0.12f, h * 0.10f, w * 0.85f, palette.bloomA, 0.55f)
        bloom(canvas, w * 0.95f, h * 0.30f, w * 0.70f, palette.bloomB, 0.42f)
        bloom(canvas, w * 0.30f, h * 1.02f, w * 0.90f, palette.bloomA, 0.30f)
        if (isDark(palette)) {
            fillVertical(canvas, w, h, 0x00000000, 0x33000000)
        }

        val margin = w * 0.10f
        val top = h * 0.13f
        drawKicker(canvas, margin, top, content.kicker, sansMedium, palette.accent)

        val titleSize = fitTitle(content.title, w - margin * 2, h * 0.34f, w * 0.19f, serif, w)
        val titleLayout = layout(
            content.title, serif, titleSize, (w - margin * 2).toInt(),
            palette.ink, lineSpacing = 1.02f, align = Layout.Alignment.ALIGN_NORMAL,
        )
        val titleTop = h * 0.22f
        canvas.save()
        canvas.translate(margin, titleTop)
        titleLayout.draw(canvas)
        canvas.restore()

        var cursor = titleTop + titleLayout.height + h * 0.035f
        if (content.phonetic != null) {
            cursor = drawPhoneticPill(canvas, margin, cursor, content.phonetic, sansMedium, palette) + h * 0.03f
        }

        val bodySize = w * 0.038f
        val bodyLayout = layout(
            content.body, sans, bodySize, (w - margin * 2 - w * 0.06f).toInt(),
            palette.ink, alpha = 0.82f, lineSpacing = 1.45f,
        )
        canvas.save()
        canvas.translate(margin, cursor)
        bodyLayout.draw(canvas)
        canvas.restore()
        cursor += bodyLayout.height + h * 0.035f

        content.note?.let { note ->
            val noteLayout = layout(note, hand, w * 0.06f, (w - margin * 2).toInt(), palette.accent, lineSpacing = 1.0f)
            canvas.save()
            canvas.translate(margin, min(cursor, h * 0.82f))
            noteLayout.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawPaper(
        canvas: Canvas, w: Float, h: Float, palette: PosterPalette,
        serif: Typeface, sans: Typeface, sansMedium: Typeface, hand: Typeface,
        content: PosterContent, seed: Int,
    ) {
        fillVertical(canvas, w, h, palette.top, palette.bottom)
        bloom(canvas, w * 0.9f, h * 0.05f, w * 0.8f, palette.bloomA, 0.35f)
        bloom(canvas, w * 0.05f, h * 0.95f, w * 0.8f, palette.bloomB, 0.30f)

        // A second sheet peeks out behind, rotated the other way.
        val back = RectF(w * 0.095f, h * 0.10f, w * 0.94f, h * 0.915f)
        val backPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.bloomA
            alpha = 55
            setShadowLayer(w * 0.02f, 0f, w * 0.012f, 0x33000000)
        }
        canvas.save()
        canvas.rotate(2.4f, w / 2f, h / 2f)
        canvas.drawRoundRect(back, w * 0.04f, w * 0.04f, backPaint)
        canvas.restore()

        val card = RectF(w * 0.075f, h * 0.085f, w * 0.925f, h * 0.90f)
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFDF8F0.toInt()
            setShadowLayer(w * 0.02f, 0f, w * 0.015f, 0x40000000)
        }
        canvas.save()
        canvas.rotate(-1.6f, w / 2f, h / 2f)
        canvas.drawRoundRect(card, w * 0.035f, w * 0.035f, cardPaint)
        canvas.restore()

        // The writing is straight even though the paper is not.
        val margin = w * 0.135f
        val contentWidth = (w - margin * 2).toInt()
        drawKicker(canvas, margin, h * 0.16f, content.kicker, sansMedium, palette.accent)

        val quoteMode = content.phonetic == null
        val titleTop = if (quoteMode) h * 0.30f else h * 0.245f
        val titleSize = fitTitle(content.title, contentWidth.toFloat(), h * 0.26f, w * 0.17f, serif, w)
        val titleLayout = layout(content.title, serif, titleSize, contentWidth, palette.ink, lineSpacing = 1.0f)
        canvas.save()
        canvas.translate(margin, titleTop)
        titleLayout.draw(canvas)
        canvas.restore()

        var cursor = titleTop + titleLayout.height + h * 0.03f
        if (content.phonetic != null) {
            cursor = drawPhoneticPill(canvas, margin, cursor, content.phonetic, sansMedium, palette) + h * 0.03f
        }
        val bodyLayout = layout(
            content.body, sans, if (quoteMode) w * 0.043f else w * 0.037f, contentWidth,
            palette.ink, alpha = 0.8f, lineSpacing = 1.5f,
        )
        canvas.save()
        canvas.translate(margin, cursor)
        bodyLayout.draw(canvas)
        canvas.restore()

        content.note?.let { note ->
            val noteLayout = layout(note, hand, w * 0.062f, contentWidth, palette.accent, lineSpacing = 1.0f)
            canvas.save()
            canvas.translate(margin, h * 0.72f)
            noteLayout.draw(canvas)
            canvas.restore()
        }

        // A tiny stitched rule, like a notebook margin.
        val rule = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.accent
            alpha = 60
            strokeWidth = w * 0.0025f
        }
        canvas.drawLine(margin * 0.55f, h * 0.14f, margin * 0.55f, h * 0.85f, rule)
    }

    private fun drawDusk(
        canvas: Canvas, w: Float, h: Float, palette: PosterPalette,
        serif: Typeface, sans: Typeface, sansMedium: Typeface, hand: Typeface,
        content: PosterContent, seed: Int,
    ) {
        fillVertical(canvas, w, h, palette.top, palette.bottom)
        bloom(canvas, w * 0.2f, h * 0.18f, w * 1.0f, palette.bloomA, 0.55f)
        bloom(canvas, w * 0.85f, h * 0.62f, w * 0.9f, palette.bloomB, 0.40f)
        bloom(canvas, w * 0.45f, h * 1.02f, w * 0.7f, palette.accent, 0.22f)

        // Stars: a few quiet points of light.
        val random = Random(seed * 13 + 5)
        val star = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        repeat(70) {
            star.alpha = 20 + random.nextInt(70)
            canvas.drawCircle(random.nextFloat() * w, random.nextFloat() * h * 0.7f, w * (0.001f + random.nextFloat() * 0.0025f), star)
        }

        val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = w * 0.003f
            color = palette.ink
            alpha = 45
        }
        canvas.drawRoundRect(
            RectF(w * 0.06f, h * 0.055f, w * 0.94f, h * 0.90f),
            w * 0.03f, w * 0.03f, frame,
        )

        val margin = w * 0.115f
        val contentWidth = (w - margin * 2).toInt()
        drawKicker(canvas, margin, h * 0.12f, content.kicker, sansMedium, palette.accent)

        val titleSize = fitTitle(content.title, contentWidth.toFloat(), h * 0.30f, w * 0.20f, serif, w)
        val titleLayout = layout(content.title, serif, titleSize, contentWidth, palette.ink, lineSpacing = 1.0f)
        canvas.save()
        canvas.translate(margin, h * 0.28f)
        titleLayout.draw(canvas)
        canvas.restore()

        var cursor = h * 0.28f + titleLayout.height + h * 0.035f
        if (content.phonetic != null) {
            cursor = drawPhoneticPill(canvas, margin, cursor, content.phonetic, sansMedium, palette) + h * 0.03f
        }
        val bodyLayout = layout(
            content.body, sans, w * 0.038f, contentWidth, palette.ink, alpha = 0.86f, lineSpacing = 1.5f,
        )
        canvas.save()
        canvas.translate(margin, cursor)
        bodyLayout.draw(canvas)
        canvas.restore()

        content.note?.let { note ->
            val noteLayout = layout(note, hand, w * 0.062f, contentWidth, palette.accent, lineSpacing = 1.0f)
            canvas.save()
            canvas.translate(margin, h * 0.72f)
            noteLayout.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawBloom(
        canvas: Canvas, w: Float, h: Float, palette: PosterPalette,
        serif: Typeface, sans: Typeface, sansMedium: Typeface, hand: Typeface,
        content: PosterContent, seed: Int,
    ) {
        fillVertical(canvas, w, h, palette.top, palette.bottom)

        // A huge soft bloom, then a crisp ring, then the word laid over it.
        bloom(canvas, w * 0.5f, h * 0.38f, w * 0.95f, palette.bloomA, 0.65f)
        bloom(canvas, w * 0.62f, h * 0.46f, w * 0.6f, palette.bloomB, 0.5f)

        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = w * 0.006f
            color = palette.accent
            alpha = 55
        }
        canvas.drawCircle(w * 0.5f, h * 0.38f, w * 0.36f, ring)
        canvas.drawCircle(w * 0.5f, h * 0.38f, w * 0.30f, ring.apply { alpha = 30 })

        val margin = w * 0.10f
        drawKicker(canvas, margin, h * 0.10f, content.kicker, sansMedium, palette.accent)

        val quoteMode = content.phonetic == null
        val titleTop = if (quoteMode) h * 0.36f else h * 0.52f
        val titleSize = fitTitle(content.title, w - margin * 2, h * 0.24f, w * 0.18f, serif, w)
        val titleLayout = layout(content.title, serif, titleSize, (w - margin * 2).toInt(), palette.ink, lineSpacing = 1.0f)
        canvas.save()
        canvas.translate(margin, titleTop)
        titleLayout.draw(canvas)
        canvas.restore()

        var cursor = titleTop + titleLayout.height + h * 0.025f
        if (content.phonetic != null) {
            cursor = drawPhoneticPill(canvas, margin, cursor, content.phonetic, sansMedium, palette) + h * 0.025f
        }
        val bodyLayout = layout(
            content.body, sans, w * 0.036f, (w - margin * 2 - w * 0.05f).toInt(),
            palette.ink, alpha = 0.82f, lineSpacing = 1.45f,
        )
        canvas.save()
        canvas.translate(margin, cursor)
        bodyLayout.draw(canvas)
        canvas.restore()

        content.note?.let { note ->
            val noteLayout = layout(note, hand, w * 0.058f, (w - margin * 2).toInt(), palette.accent, lineSpacing = 1.0f)
            canvas.save()
            canvas.translate(margin, h * 0.80f)
            noteLayout.draw(canvas)
            canvas.restore()
        }
    }

    // ------------------------------------------------------------------ pieces

    private fun drawKicker(canvas: Canvas, x: Float, y: Float, text: String, typeface: Typeface, color: Int) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.color = color
            textSize = x * 0.30f
            letterSpacing = 0.22f
        }
        canvas.drawText(text.uppercase(), x, y, paint)
    }

    private fun drawPhoneticPill(
        canvas: Canvas, x: Float, y: Float, text: String, typeface: Typeface, palette: PosterPalette,
    ): Float {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.color = palette.ink
            textSize = x * 0.34f
        }
        val padH = paint.textSize * 0.8f
        val padV = paint.textSize * 0.55f
        val width = paint.measureText(text) + padH * 2
        val height = paint.textSize + padV * 2
        val rect = RectF(x, y, x + width, y + height)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.ink
            alpha = 20
        }
        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = x * 0.008f
            color = palette.ink
            alpha = 40
        }
        canvas.drawRoundRect(rect, height / 2f, height / 2f, fill)
        canvas.drawRoundRect(rect, height / 2f, height / 2f, stroke)
        canvas.drawText(text, x + padH, y + padV + paint.textSize * 0.82f, paint)
        return y + height
    }

    private fun drawSignature(
        canvas: Canvas, w: Float, h: Float, palette: PosterPalette,
        typeface: Typeface, signature: String, yFraction: Float,
    ) {
        val y = h * yFraction
        val markSize = w * 0.045f
        val left = w * 0.09f
        drawMark(canvas, left, y - markSize * 0.86f, markSize, palette.ink)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            color = palette.ink
            alpha = 190
            textSize = w * 0.030f
            letterSpacing = 0.08f
        }
        canvas.drawText("Kairos", left + markSize * 1.45f, y - markSize * 0.14f, paint)
        val date = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            color = palette.ink
            alpha = 130
            textSize = w * 0.026f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(signature, w * 0.91f, y - markSize * 0.14f, date)
    }

    private fun drawMark(canvas: Canvas, x: Float, y: Float, size: Float, color: Int) {
        val s = size / 24f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        canvas.drawRoundRect(
            RectF(x + 6.5f * s, y + 4f * s, x + 10.5f * s, y + 20f * s),
            2f * s, 2f * s, paint,
        )
        val upper = Path().apply {
            moveTo(x + 10.4f * s, y + 11.4f * s)
            cubicTo(x + 12.4f * s, y + 8.4f * s, x + 15f * s, y + 6.5f * s, x + 18.5f * s, y + 5.5f * s)
            cubicTo(x + 19.2f * s, y + 5.3f * s, x + 19.8f * s, y + 6f * s, x + 19.5f * s, y + 6.7f * s)
            cubicTo(x + 18.2f * s, y + 9.8f * s, x + 15.6f * s, y + 11.7f * s, x + 12.2f * s, y + 12.7f * s)
            cubicTo(x + 11.3f * s, y + 13f * s, x + 10.1f * s, y + 12.3f * s, x + 10.4f * s, y + 11.4f * s)
            close()
        }
        canvas.drawPath(upper, paint)
        val lower = Path().apply {
            moveTo(x + 10.4f * s, y + 12.6f * s)
            cubicTo(x + 12.4f * s, y + 15.6f * s, x + 15f * s, y + 17.5f * s, x + 18.5f * s, y + 18.5f * s)
            cubicTo(x + 19.2f * s, y + 18.7f * s, x + 19.8f * s, y + 18f * s, x + 19.5f * s, y + 17.3f * s)
            cubicTo(x + 18.2f * s, y + 14.2f * s, x + 15.6f * s, y + 12.3f * s, x + 12.2f * s, y + 11.3f * s)
            cubicTo(x + 11.3f * s, y + 11f * s, x + 10.1f * s, y + 11.7f * s, x + 10.4f * s, y + 12.6f * s)
            close()
        }
        canvas.drawPath(lower, paint)
    }

    private fun bloom(canvas: Canvas, cx: Float, cy: Float, radius: Float, color: Int, alpha: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx, cy, radius,
                intArrayOf(
                    withAlpha(color, alpha),
                    withAlpha(color, alpha * 0.42f),
                    withAlpha(color, 0f),
                ),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawCircle(cx, cy, radius, paint)
    }

    private fun drawGrain(canvas: Canvas, w: Float, h: Float, seed: Int, alpha: Float) {
        val random = Random(seed * 7919 + 13)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        repeat((w * h / 900f).toInt().coerceIn(1200, 5200)) {
            paint.color = if (random.nextBoolean()) Color.WHITE else Color.BLACK
            paint.alpha = (random.nextFloat() * 255 * alpha).toInt()
            canvas.drawCircle(
                random.nextFloat() * w,
                random.nextFloat() * h,
                w * (0.0008f + random.nextFloat() * 0.0022f),
                paint,
            )
        }
    }

    private fun fillVertical(canvas: Canvas, w: Float, h: Float, top: Int, bottom: Int) {
        val paint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, h, top, bottom, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, w, h, paint)
    }

    private fun fitTitle(
        text: String, width: Float, maxHeight: Float, startSize: Float, typeface: Typeface, w: Float,
    ): Float {
        var size = startSize
        while (size > w * 0.055f) {
            val layout = layout(text, typeface, size, width.toInt(), Color.BLACK, lineSpacing = 1.0f)
            if (layout.height <= maxHeight && layout.lineCount <= 3) return size
            size *= 0.94f
        }
        return size
    }

    private fun layout(
        text: String,
        typeface: Typeface,
        size: Float,
        width: Int,
        color: Int,
        alpha: Float = 1f,
        lineSpacing: Float = 1.2f,
        align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    ): StaticLayout {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            textSize = size
            this.color = color
            this.alpha = (alpha * 255).toInt().coerceIn(0, 255)
        }
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(1))
            .setAlignment(align)
            .setLineSpacing(0f, lineSpacing)
            .setIncludePad(false)
            .build()
    }

    private fun withAlpha(color: Int, alpha: Float): Int =
        Color.argb((alpha.coerceIn(0f, 1f) * 255).toInt(), Color.red(color), Color.green(color), Color.blue(color))

    private fun isDark(palette: PosterPalette): Boolean =
        (Color.red(palette.top) + Color.green(palette.top) + Color.blue(palette.top)) < 200
}
