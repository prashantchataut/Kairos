package com.kairos.app.ui.garden

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import com.kairos.app.ui.animation.rememberKairosPressScale
import com.kairos.app.ui.animation.kairosScale
import com.kairos.app.ui.theme.KairosTypography

/** Small semantic palette shared by the rebuilt product surfaces. */
object GardenInk {
    val forest = Color(0xFF275D4D)
    val paper = Color(0xFFF8F5ED)
    val ink = Color(0xFF253E35)
    val muted = Color(0xFF58685E)
    val sage = Color(0xFFDCE9D8)
    val peach = Color(0xFFF2D2BA)
    val gold = Color(0xFFE9CB83)
    val night = Color(0xFF14251F)
}

@Composable
fun GardenTheme(content: @Composable () -> Unit) {
    val dark = com.kairos.app.ui.theme.isDarkTheme()
    val colors = if (dark) darkColorScheme(
        primary = Color(0xFFACD5B7), onPrimary = GardenInk.night,
        primaryContainer = GardenInk.forest, onPrimaryContainer = Color(0xFFE7F1DF),
        secondaryContainer = Color(0xFF654A36), onSecondaryContainer = GardenInk.peach,
        background = GardenInk.night, onBackground = Color(0xFFEAF0E4),
        surface = Color(0xFF20352C), onSurface = Color(0xFFEAF0E4),
        surfaceVariant = Color(0xFF2C4438), onSurfaceVariant = Color(0xFFBDCBBD),
        outlineVariant = Color(0xFF496052)
    ) else lightColorScheme(
        primary = GardenInk.forest, onPrimary = GardenInk.paper,
        primaryContainer = GardenInk.sage, onPrimaryContainer = GardenInk.ink,
        secondaryContainer = GardenInk.peach, onSecondaryContainer = GardenInk.ink,
        background = GardenInk.paper, onBackground = GardenInk.ink,
        surface = Color(0xFFFFFCF6), onSurface = GardenInk.ink,
        surfaceVariant = Color(0xFFEEEDE2), onSurfaceVariant = GardenInk.muted,
        outlineVariant = Color(0xFFD1D8C9)
    )
    MaterialTheme(colorScheme = colors, typography = KairosTypography, content = content)
}

/** Original seed-and-orbit mark; drawn at any density without a bitmap. */
@Composable
fun SeedMark(modifier: Modifier = Modifier) {
    val ink = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val u = size.minDimension
        rotate(-24f) {
            drawOval(ink.copy(alpha = .6f), Offset(u * .06f, u * .24f), Size(u * .88f, u * .5f), style = Stroke(u * .045f))
        }
        drawLine(ink, Offset(u * .48f, u * .77f), Offset(u * .48f, u * .36f), u * .065f, StrokeCap.Round)
        rotate(-35f, Offset(u * .39f, u * .40f)) {
            drawOval(ink, Offset(u * .21f, u * .28f), Size(u * .3f, u * .18f))
        }
        rotate(35f, Offset(u * .59f, u * .31f)) {
            drawOval(ink, Offset(u * .46f, u * .20f), Size(u * .30f, u * .18f))
        }
        drawCircle(ink, u * .06f, Offset(u * .90f, u * .34f))
    }
}

/** Layered pigment, a hand-painted stem and a small friendly seed. No looping work. */
@Composable
fun GardenPainting(modifier: Modifier = Modifier, letter: Boolean = false) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        for (i in 0..14) {
            val spread = i * .005f
            drawOval(GardenInk.sage.copy(alpha = .09f), Offset(w * (.08f - spread), h * (.12f - spread)), Size(w * (.83f + spread), h * (.77f + spread)))
            drawOval(GardenInk.peach.copy(alpha = .065f), Offset(w * (.42f - spread), h * (.38f - spread)), Size(w * (.56f + spread), h * (.55f + spread)))
        }
        if (letter) {
            rotate(-9f) {
                drawRoundRect(GardenInk.paper, Offset(w * .15f, h * .28f), Size(w * .7f, h * .48f), androidx.compose.ui.geometry.CornerRadius(14f))
                val flap = Path().apply { moveTo(w*.15f,h*.28f); lineTo(w*.5f,h*.55f); lineTo(w*.85f,h*.28f) }
                drawPath(flap, GardenInk.forest.copy(alpha=.35f), style=Stroke(3f))
                drawCircle(GardenInk.forest, w*.08f, Offset(w*.5f,h*.55f))
                drawLine(GardenInk.paper, Offset(w*.48f,h*.59f),Offset(w*.52f,h*.51f),3f,StrokeCap.Round)
            }
        } else {
            val stem = Path().apply { moveTo(w*.52f,h*.85f); cubicTo(w*.42f,h*.64f,w*.58f,h*.48f,w*.5f,h*.22f) }
            drawPath(stem, GardenInk.forest, style=Stroke(w*.018f,cap=StrokeCap.Round))
            for (i in 0..3) {
                val y = h*(.34f+i*.12f); val left = i%2==0
                rotate(if(left) 34f else -34f,Offset(w*.5f,y)) {
                    drawOval(GardenInk.forest.copy(alpha=.65f), Offset(if(left) w*.26f else w*.5f,y-h*.045f), Size(w*.25f,h*.10f))
                }
            }
            drawOval(GardenInk.gold,Offset(w*.35f,h*.69f),Size(w*.33f,h*.23f))
            drawCircle(GardenInk.ink,w*.012f,Offset(w*.46f,h*.79f))
            drawCircle(GardenInk.ink,w*.012f,Offset(w*.57f,h*.79f))
            drawArc(GardenInk.ink,0f,180f,false,Offset(w*.48f,h*.80f),Size(w*.07f,h*.035f),style=Stroke(w*.009f,cap=StrokeCap.Round))
        }
    }
}

/** A press starts feedback immediately and remains interruptible. */
@Composable
fun Modifier.gardenPress(onClick: () -> Unit): Modifier {
    val interactions = remember { MutableInteractionSource() }
    val scale = rememberKairosPressScale(interactions, .985f)
    return this.kairosScale(scale).clickable(interactionSource = interactions, indication = null, role = Role.Button, onClick = onClick)
}
