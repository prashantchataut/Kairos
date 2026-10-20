package com.kairos.app.design.paint

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.kairos.app.design.Kairos
import kotlinx.coroutines.delay
import kotlin.math.sin

enum class MomoMood { Idle, Happy, Sleepy, Curious, Proud, Wobbly }

/**
 * Momo — a drop of pigment that lives in the app.
 *
 * Deliberately not an illustration: it is drawn from the same blob function as
 * the watercolour washes, so it always belongs to the surface it sits on.
 * It breathes, blinks, and reacts to what happened on screen.
 */
@Composable
fun Momo(
    modifier: Modifier = Modifier,
    mood: MomoMood = MomoMood.Idle,
    bodyColor: Color? = null,
    blush: Color? = null,
    showFace: Boolean = true,
) {
    val c = Kairos.colors
    val motion = Kairos.motion
    val body = bodyColor ?: c.accent
    val cheek = blush ?: c.peach

    val breath = if (!motion.reduced) {
        val t = rememberInfiniteTransition(label = "momo")
        t.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Reverse),
            label = "breath",
        ).value
    } else 0.5f

    var blinking by remember { mutableStateOf(false) }
    LaunchedEffect(motion.reduced) {
        if (motion.reduced) return@LaunchedEffect
        while (true) {
            delay(2600L + (0..1800).random())
            blinking = true
            delay(120)
            blinking = false
        }
    }

    val react by animateFloatAsState(
        targetValue = when (mood) {
            MomoMood.Happy -> 1f
            MomoMood.Proud -> 0.7f
            MomoMood.Curious -> 0.35f
            MomoMood.Sleepy -> -0.25f
            else -> 0f
        },
        animationSpec = motion.playful(),
        label = "react",
    )

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f + h * 0.02f
        val squash = 1f + breath * 0.035f + react * 0.05f
        val stretch = 1f - breath * 0.025f + react * 0.03f

        // ground shadow
        drawOval(
            color = Color.Black.copy(alpha = if (c.dark) 0.18f else 0.08f),
            topLeft = Offset(w * 0.2f, h * 0.84f),
            size = Size(w * 0.6f, h * 0.1f),
        )

        scale(scaleX = squash, scaleY = stretch, pivot = Offset(cx, h * 0.86f)) {
            val bodyPath = blobPath(
                center = Offset(cx, cy),
                radiusX = w * 0.38f,
                radiusY = h * 0.37f,
                seed = 31,
                points = 11,
                wobble = 0.08f,
            )
            drawPath(
                bodyPath,
                Brush.radialGradient(
                    colors = listOf(
                        body.copy(alpha = 0.98f),
                        body.copy(alpha = 0.92f),
                        body.copy(alpha = 0.86f),
                    ),
                    center = Offset(cx - w * 0.1f, cy - h * 0.12f),
                    radius = w * 0.55f,
                ),
            )
            // wet rim
            drawPath(bodyPath, body.copy(alpha = 0.35f), style = Stroke(w * 0.012f))

            // gloss
            drawOval(
                color = Color.White.copy(alpha = if (c.dark) 0.16f else 0.34f),
                topLeft = Offset(cx - w * 0.19f, cy - h * 0.24f),
                size = Size(w * 0.22f, h * 0.13f),
            )

            if (showFace) {
                val eyeY = cy - h * 0.02f
                val eyeDx = w * 0.115f
                val eyeR = w * 0.036f
                val lid = if (blinking) 0.15f else 1f
                for (side in listOf(-1f, 1f)) {
                    drawOval(
                        color = c.ink.copy(alpha = 0.9f),
                        topLeft = Offset(cx + side * eyeDx - eyeR, eyeY - eyeR * 1.25f * lid),
                        size = Size(eyeR * 2f, eyeR * 2.5f * lid),
                    )
                    if (!blinking) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.9f),
                            radius = eyeR * 0.32f,
                            center = Offset(cx + side * eyeDx - eyeR * 0.3f, eyeY - eyeR * 0.55f),
                        )
                    }
                    // blush
                    drawCircle(
                        color = cheek.copy(alpha = 0.35f),
                        radius = w * 0.045f,
                        center = Offset(cx + side * (eyeDx + w * 0.075f), eyeY + h * 0.055f),
                    )
                }
                // mouth: a small curve that changes with mood
                val mouth = Path()
                val my = eyeY + h * 0.085f
                val mw = w * 0.07f
                when (mood) {
                    MomoMood.Happy, MomoMood.Proud -> {
                        mouth.moveTo(cx - mw, my)
                        mouth.quadraticBezierTo(cx, my + h * 0.055f, cx + mw, my)
                    }
                    MomoMood.Sleepy -> {
                        mouth.moveTo(cx - mw * 0.8f, my + h * 0.012f)
                        mouth.quadraticBezierTo(cx, my - h * 0.01f, cx + mw * 0.8f, my + h * 0.012f)
                    }
                    MomoMood.Curious -> {
                        mouth.addOval(
                            androidx.compose.ui.geometry.Rect(
                                cx - w * 0.022f, my - h * 0.01f,
                                cx + w * 0.022f, my + h * 0.035f,
                            )
                        )
                    }
                    else -> {
                        mouth.moveTo(cx - mw * 0.75f, my)
                        mouth.quadraticBezierTo(cx, my + h * 0.032f, cx + mw * 0.75f, my)
                    }
                }
                drawPath(mouth, c.ink.copy(alpha = 0.85f), style = Stroke(w * 0.018f))
            }

            // a tiny leaf sprout on the head — the "kairos" wink
            if (mood == MomoMood.Proud || mood == MomoMood.Happy) {
                val sprout = Path()
                val sx = cx + w * 0.03f
                val sy = cy - h * 0.38f
                sprout.moveTo(sx, sy + h * 0.06f)
                sprout.quadraticBezierTo(sx + w * 0.01f, sy - h * 0.02f, sx + w * 0.075f, sy - h * 0.035f)
                sprout.quadraticBezierTo(sx + w * 0.055f, sy + h * 0.045f, sx, sy + h * 0.06f)
                drawPath(sprout, c.mint)
            }
        }
    }
}

/** The app mark: a painted pebble with a K cut out of it. */
@Composable
fun KairosMark(
    modifier: Modifier = Modifier,
    tint: Color? = null,
    paper: Color? = null,
    animated: Boolean = false,
) {
    val c = Kairos.colors
    val inkColor = paper ?: if (c.dark) c.canvas else Color.White
    val pigment = tint ?: c.accent
    val motion = Kairos.motion
    val spin = if (animated && !motion.reduced) {
        val t = rememberInfiniteTransition(label = "mark")
        t.animateFloat(0f, 6.283f, infiniteRepeatable(tween(24000), RepeatMode.Restart), label = "turn").value
    } else 0f

    Canvas(modifier) {
        val s = size.minDimension
        val center = Offset(size.width / 2, size.height / 2)
        paintBlob(
            center = center,
            radiusX = s * 0.44f,
            radiusY = s * 0.44f,
            color = pigment,
            seed = 12,
            alpha = 0.95f,
            rim = 0.2f,
            rotation = spin * 57.3f,
        )
        paintBlob(
            center = center + Offset(s * 0.06f, s * 0.05f),
            radiusX = s * 0.3f,
            radiusY = s * 0.3f,
            color = c.accentPartner,
            seed = 44,
            alpha = 0.5f,
        )
        rotate(-8f, pivot = center) {
            val stemX = center.x - s * 0.13f
            val top = center.y - s * 0.19f
            val bottom = center.y + s * 0.19f
            val stroke = Stroke(width = s * 0.075f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(inkColor, Offset(stemX, top), Offset(stemX, bottom), strokeWidth = stroke.width, cap = stroke.cap)
            // arm + leg curl into a leaf
            val arm = Path().apply {
                moveTo(stemX + s * 0.005f, center.y)
                quadraticBezierTo(center.x + s * 0.02f, center.y - s * 0.06f, center.x + s * 0.17f, top + s * 0.03f)
            }
            val leg = Path().apply {
                moveTo(stemX + s * 0.005f, center.y)
                quadraticBezierTo(center.x + s * 0.03f, center.y + s * 0.05f, center.x + s * 0.17f, bottom - s * 0.03f)
            }
            drawPath(arm, inkColor, style = stroke)
            drawPath(leg, inkColor, style = stroke)
            // the moment: a dot of pigment at the tip
            drawCircle(inkColor, radius = s * 0.038f, center = Offset(center.x + s * 0.185f, top + s * 0.035f))
        }
    }
}
