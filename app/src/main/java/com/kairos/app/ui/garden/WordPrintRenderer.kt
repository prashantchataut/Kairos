package com.kairos.app.ui.garden

import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.util.Random

/** Original printable botanical composition, independent of screen resolution. */
object WordPrintRenderer {
    enum class Paper(val label: String, val background: Int, val ink: Int, val pigment: Int) {
        HERBARIUM("Herbarium", 0xFFF8F5ED.toInt(), 0xFF275D4D.toInt(), 0xFFAAC7A2.toInt()),
        APRICOT("Apricot", 0xFFF6E5D8.toInt(), 0xFF643D2D.toInt(), 0xFFE3AB86.toInt()),
        MIDNIGHT("Night garden", 0xFF192F28.toInt(), 0xFFE8EAD5.toInt(), 0xFF688F76.toInt())
    }

    fun render(word: String, definition: String, pronunciation: String?, paper: Paper = Paper.HERBARIUM): Bitmap {
        val bitmap = Bitmap.createBitmap(1080, 1350, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap); canvas.drawColor(paper.background)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val random = Random(82)
        // Pigment is translucent and layered, with repeatable grain rather than runtime noise.
        repeat(30) {
            paint.color = paper.pigment; paint.alpha = 8
            val x = 720f + random.nextFloat()*180f; val y = 280f + random.nextFloat()*130f
            canvas.drawOval(x-210,y-210,x+210,y+230,paint)
        }
        repeat(1600) {
            paint.color = paper.ink; paint.alpha = 9
            canvas.drawCircle(random.nextFloat()*1080, random.nextFloat()*1350, .8f, paint)
        }
        paint.alpha = 255; paint.color = paper.ink; paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE
        val stem = Path().apply { moveTo(820f,560f); cubicTo(710f,440f,840f,350f,795f,230f) }
        canvas.drawPath(stem,paint); paint.style=Paint.Style.FILL
        repeat(5) { i ->
            val y=275f+i*48; canvas.save(); canvas.rotate(if(i%2==0) 30f else -30f,795f,y)
            paint.alpha=155;canvas.drawOval(if(i%2==0) 695f else 795f,y-22,if(i%2==0) 795f else 895f,y+22,paint);canvas.restore()
        }
        paint.alpha=255
        fun text(value: String, x: Float, y: Float, size: Float, serif: Boolean = false) {
            paint.textSize=size;paint.typeface=Typeface.create(if(serif) "serif" else "sans-serif",Typeface.NORMAL)
            canvas.drawText(value,x,y,paint)
        }
        text("KAIROS  /  WORD PRINTS",80f,112f,25f)
        text("A little word. A wider world.",80f,165f,27f,true)
        var titleSize=110f
        paint.typeface=Typeface.create("serif",Typeface.NORMAL)
        while (titleSize>36f) { paint.textSize=titleSize; if(paint.measureText(word)<=920f) break; titleSize-=2 }
        text(word,80f,700f,titleSize,true)
        text(pronunciation.orEmpty().take(75),84f,762f,28f)
        val tp=TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color=paper.ink; textSize=36f;typeface=Typeface.create("serif",Typeface.NORMAL) }
        val layout=StaticLayout.Builder.obtain(definition,0,definition.length,tp,900)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(10f,1f).setMaxLines(7)
            .setEllipsize(android.text.TextUtils.TruncateAt.END).build()
        canvas.save();canvas.translate(84f,815f);layout.draw(canvas);canvas.restore()
        paint.strokeWidth=1.5f;canvas.drawLine(80f,1190f,1000f,1190f,paint)
        paint.style=Paint.Style.STROKE;paint.strokeWidth=3f
        canvas.save();canvas.rotate(-25f,112f,1252f);canvas.drawOval(80f,1234f,144f,1270f,paint);canvas.restore()
        canvas.drawLine(110f,1272f,110f,1230f,paint);paint.style=Paint.Style.FILL
        canvas.drawOval(110f,1224f,132f,1237f,paint)
        text("kairos",164f,1264f,42f,true)
        text("KEEP SOMETHING GOOD.",630f,1260f,23f)
        return bitmap
    }
}
