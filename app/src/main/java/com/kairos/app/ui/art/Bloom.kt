package com.kairos.app.ui.art

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import com.kairos.app.ui.theme.Candy

/**
 * The moment bloom, Kairos' mark: eight soft petals around a round centre, drawn in one colour. A
 * sun that is also a flower; the eight rays of the original logo grown into petals. The petal at
 * one o'clock is a full-length petal where the others alternate long and short, like the hand of a
 * clock: chronos is clock time, kairos is the one moment that matters.
 */
object Bloom {
    const val PETALS = 8
    /** The petal that points to the moment (45°, just past twelve). */
    const val MOMENT = 1

    fun petalLength(i: Int) = if (i % 2 == 0 || i == MOMENT) 1f else 0.8f

    /** A petal pointing straight up from the centre: narrow at the base, round at the tip. */
    fun petal(r: Float, length: Float = 1f): Path {
        val r0 = r * 0.24f
        val tip = r0 + (r - r0) * length
        val w = r * 0.205f
        val l = tip - r0
        return Path().apply {
            moveTo(0f, -r0)
            cubicTo(w * 0.42f, -r0 - l * 0.18f, w, -tip + w * 1.9f, w, -tip + w)
            cubicTo(w, -tip + w * 0.45f, w * 0.55f, -tip, 0f, -tip)
            cubicTo(-w * 0.55f, -tip, -w, -tip + w * 0.45f, -w, -tip + w)
            cubicTo(-w, -tip + w * 1.9f, -w * 0.42f, -r0 - l * 0.18f, 0f, -r0)
            close()
        }
    }
}

/**
 * Draws the bloom. [unfold] gives each petal's growth 0..1 (for the onboarding wake-up), [core] the
 * centre's scale. Pass [moment] = null for a single-colour mark.
 */
fun DrawScope.drawBloom(
    center: Offset = Offset(size.width / 2, size.height / 2),
    radius: Float = size.minDimension / 2,
    petal: Color = Color.White,
    moment: Color? = null,
    core: Color? = petal,
    unfold: (Int) -> Float = { 1f },
    core01: Float = 1f,
    shaded: Boolean = false,
) {
    for (i in 0 until Bloom.PETALS) {
        val u = unfold(i).coerceIn(0f, 1.2f)
        if (u <= 0.001f) continue
        val c = if (i == Bloom.MOMENT && moment != null) moment else petal
        val path = Bloom.petal(radius, Bloom.petalLength(i))
        rotate(i * 45f - (1f - u.coerceAtMost(1f)) * 28f, center) {
            translate(center.x, center.y) {
                scale(u, u, Offset.Zero) {
                    if (shaded) {
                        drawPath(path, Brush.verticalGradient(listOf(lerp(c, Color.White, 0.25f), c, lerp(c, Candy.Navy, 0.08f)), startY = -radius, endY = -radius * 0.2f))
                    } else drawPath(path, c)
                }
            }
        }
    }
    if (core != null && core01 > 0.001f) {
        val cr = radius * 0.26f * core01
        if (shaded) drawCircle(Brush.radialGradient(listOf(lerp(core, Color.White, 0.35f), core, lerp(core, Candy.Navy, 0.08f)), Offset(center.x - cr * 0.3f, center.y - cr * 0.3f), cr * 1.6f), cr, center)
        else drawCircle(core, cr, center)
    }
}
