package com.kairos.app.ui.art

import android.graphics.Bitmap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import java.util.Random

/**
 * Film grain: one small tile of light and dark specks, repeated. It is what makes the gradients in
 * the references (r10, r18, r24, r25, r34) feel printed rather than digital.
 */
object Grain {
    private const val SIZE = 160
    private var tile: ImageBitmap? = null
    private var brush: ShaderBrush? = null

    private fun make(): ImageBitmap {
        val rnd = Random(606)
        val px = IntArray(SIZE * SIZE) {
            val light = rnd.nextBoolean()
            val a = (rnd.nextFloat().let { it * it } * 255).toInt()
            val v = if (light) 255 else 0
            (a shl 24) or (v shl 16) or (v shl 8) or v
        }
        return Bitmap.createBitmap(px, SIZE, SIZE, Bitmap.Config.ARGB_8888).asImageBitmap()
    }

    fun brush(): ShaderBrush = brush ?: ShaderBrush(ImageShader(tile ?: make().also { tile = it }, TileMode.Repeated, TileMode.Repeated)).also { brush = it }
}

fun DrawScope.drawGrain(alpha: Float) {
    if (alpha > 0f) drawRect(Grain.brush(), alpha = alpha)
}

/** Grain over whatever this element draws. 6–12% for art, never more than 6% behind body text. */
fun Modifier.grain(alpha: Float = 0.08f): Modifier = drawWithContent { drawContent(); drawGrain(alpha) }
