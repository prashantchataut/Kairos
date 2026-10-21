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
import kotlin.random.Random

/**
 * Film grain (kairos-taste §2). Monochrome noise with both light and dark specks so it reads on
 * any colour; it turns flat gradients into something airbrushed and warm (#606-10/18/24/25).
 */
object Grain {
    @Volatile private var tile: ImageBitmap? = null

    fun tile(): ImageBitmap = tile ?: synchronized(this) {
        tile ?: run {
            val size = 192
            val rnd = Random(1717)
            val px = IntArray(size * size) {
                val v = rnd.nextFloat()
                when {
                    v < 0.22f -> (((rnd.nextFloat() * 70).toInt() + 30) shl 24) // dark speck
                    v > 0.80f -> (((rnd.nextFloat() * 70).toInt() + 30) shl 24) or 0xFFFFFF // light speck
                    else -> 0
                }
            }
            Bitmap.createBitmap(px, size, size, Bitmap.Config.ARGB_8888).asImageBitmap().also { tile = it }
        }
    }

    val brush by lazy { ShaderBrush(ImageShader(tile(), TileMode.Repeated, TileMode.Repeated)) }
}

fun DrawScope.grain(alpha: Float = 0.08f) = drawRect(Grain.brush, alpha = alpha)

/** Grain over whatever this element draws. */
fun Modifier.grain(alpha: Float = 0.08f): Modifier = drawWithContent { drawContent(); grain(alpha) }
