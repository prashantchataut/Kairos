package com.kairos.app.design.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Hand-built icon set — 24dp grid, 1.9dp round strokes, no library glyphs.
 * Line icons for verbs, filled shapes only for emotional states (heart,
 * sparkle, play). That split is deliberate: it keeps the UI quiet and lets
 * the accent colour do the talking.
 */
object KairosIcons {

    private fun stroke(
        name: String,
        width: Float = 1.9f,
        block: PathBuilder.() -> Unit,
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = width,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) { block() }
    }.build()

    private fun filled(name: String, block: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) { block() }
        }.build()

    val Back = stroke("back") {
        moveTo(14.5f, 5.5f); lineTo(8f, 12f); lineTo(14.5f, 18.5f)
    }

    val Chevron = stroke("chevron") {
        moveTo(9.5f, 5.5f); lineTo(16f, 12f); lineTo(9.5f, 18.5f)
    }

    val ChevronDown = stroke("chevronDown") {
        moveTo(5.5f, 9.5f); lineTo(12f, 16f); lineTo(18.5f, 9.5f)
    }

    val Close = stroke("close") {
        moveTo(6.5f, 6.5f); lineTo(17.5f, 17.5f)
        moveTo(17.5f, 6.5f); lineTo(6.5f, 17.5f)
    }

    val Plus = stroke("plus") {
        moveTo(12f, 5.5f); lineTo(12f, 18.5f)
        moveTo(5.5f, 12f); lineTo(18.5f, 12f)
    }

    val Search = stroke("search") {
        moveTo(11f, 4.5f)
        curveTo(14.6f, 4.5f, 17.5f, 7.4f, 17.5f, 11f)
        curveTo(17.5f, 14.6f, 14.6f, 17.5f, 11f, 17.5f)
        curveTo(7.4f, 17.5f, 4.5f, 14.6f, 4.5f, 11f)
        curveTo(4.5f, 7.4f, 7.4f, 4.5f, 11f, 4.5f)
        close()
        moveTo(15.6f, 15.6f); lineTo(19.5f, 19.5f)
    }

    val Today = stroke("today") {
        moveTo(12f, 3.5f); lineTo(12f, 6.5f)
        moveTo(12f, 17.5f); lineTo(12f, 20.5f)
        moveTo(3.5f, 12f); lineTo(6.5f, 12f)
        moveTo(17.5f, 12f); lineTo(20.5f, 12f)
        moveTo(6f, 6f); lineTo(8.1f, 8.1f)
        moveTo(15.9f, 15.9f); lineTo(18f, 18f)
        moveTo(18f, 6f); lineTo(15.9f, 8.1f)
        moveTo(8.1f, 15.9f); lineTo(6f, 18f)
        moveTo(12f, 8.2f)
        curveTo(14.1f, 8.2f, 15.8f, 9.9f, 15.8f, 12f)
        curveTo(15.8f, 14.1f, 14.1f, 15.8f, 12f, 15.8f)
        curveTo(9.9f, 15.8f, 8.2f, 14.1f, 8.2f, 12f)
        curveTo(8.2f, 9.9f, 9.9f, 8.2f, 12f, 8.2f)
        close()
    }

    val Learn = stroke("learn") {
        moveTo(12f, 6.6f)
        curveTo(10.2f, 5.2f, 7.6f, 4.8f, 4.5f, 5.3f)
        lineTo(4.5f, 17.6f)
        curveTo(7.6f, 17.1f, 10.2f, 17.5f, 12f, 18.9f)
        curveTo(13.8f, 17.5f, 16.4f, 17.1f, 19.5f, 17.6f)
        lineTo(19.5f, 5.3f)
        curveTo(16.4f, 4.8f, 13.8f, 5.2f, 12f, 6.6f)
        close()
        moveTo(12f, 6.6f); lineTo(12f, 18.9f)
    }

    val Capsule = stroke("capsule") {
        moveTo(6.5f, 9.5f); lineTo(17.5f, 9.5f)
        lineTo(17.5f, 18.2f)
        curveTo(17.5f, 19.5f, 16.5f, 20.5f, 15.2f, 20.5f)
        lineTo(8.8f, 20.5f)
        curveTo(7.5f, 20.5f, 6.5f, 19.5f, 6.5f, 18.2f)
        close()
        moveTo(8.2f, 6.2f); lineTo(15.8f, 6.2f)
        lineTo(15.8f, 9.5f)
        moveTo(9.2f, 6.2f); lineTo(9.2f, 4.4f)
        lineTo(14.8f, 4.4f); lineTo(14.8f, 6.2f)
        moveTo(12f, 12.5f); lineTo(12f, 16.5f)
        moveTo(10.2f, 14.4f); lineTo(12f, 16.6f); lineTo(13.8f, 14.4f)
    }

    val Play = stroke("play") {
        moveTo(7f, 6.8f)
        curveTo(7f, 5.6f, 8.3f, 4.8f, 9.4f, 5.4f)
        lineTo(19f, 10.6f)
        curveTo(20f, 11.2f, 20f, 12.8f, 19f, 13.4f)
        lineTo(9.4f, 18.6f)
        curveTo(8.3f, 19.2f, 7f, 18.4f, 7f, 17.2f)
        close()
    }

    val Gamepad = stroke("gamepad") {
        moveTo(8.4f, 7.5f); lineTo(15.6f, 7.5f)
        curveTo(18.2f, 7.5f, 20f, 9.6f, 20f, 12.2f)
        curveTo(20f, 15.6f, 18.4f, 18f, 16.4f, 18f)
        curveTo(15.2f, 18f, 14.6f, 17.2f, 13.6f, 16.2f)
        lineTo(10.4f, 16.2f)
        curveTo(9.4f, 17.2f, 8.8f, 18f, 7.6f, 18f)
        curveTo(5.6f, 18f, 4f, 15.6f, 4f, 12.2f)
        curveTo(4f, 9.6f, 5.8f, 7.5f, 8.4f, 7.5f)
        close()
        moveTo(8.2f, 11.9f); lineTo(10.8f, 11.9f)
        moveTo(9.5f, 10.6f); lineTo(9.5f, 13.2f)
        moveTo(15f, 11.3f); lineTo(15.05f, 11.3f)
        moveTo(16.9f, 12.9f); lineTo(16.95f, 12.9f)
    }

    val You = stroke("you") {
        moveTo(12f, 4.6f)
        curveTo(14f, 4.6f, 15.6f, 6.2f, 15.6f, 8.2f)
        curveTo(15.6f, 10.2f, 14f, 11.8f, 12f, 11.8f)
        curveTo(10f, 11.8f, 8.4f, 10.2f, 8.4f, 8.2f)
        curveTo(8.4f, 6.2f, 10f, 4.6f, 12f, 4.6f)
        close()
        moveTo(5f, 20f)
        curveTo(5f, 16.2f, 8.1f, 14.2f, 12f, 14.2f)
        curveTo(15.9f, 14.2f, 19f, 16.2f, 19f, 20f)
    }

    val Heart = filled("heart") {
        moveTo(12f, 20f)
        curveTo(10.6f, 18.8f, 3.8f, 14.4f, 3.8f, 9.6f)
        curveTo(3.8f, 6.9f, 5.9f, 4.8f, 8.4f, 4.8f)
        curveTo(9.9f, 4.8f, 11.3f, 5.6f, 12f, 6.9f)
        curveTo(12.7f, 5.6f, 14.1f, 4.8f, 15.6f, 4.8f)
        curveTo(18.1f, 4.8f, 20.2f, 6.9f, 20.2f, 9.6f)
        curveTo(20.2f, 14.4f, 13.4f, 18.8f, 12f, 20f)
        close()
    }

    val HeartOutline = stroke("heartOutline") {
        moveTo(12f, 19.6f)
        curveTo(10.4f, 18.2f, 4.3f, 14.1f, 4.3f, 9.7f)
        curveTo(4.3f, 7.2f, 6.2f, 5.2f, 8.6f, 5.2f)
        curveTo(10f, 5.2f, 11.3f, 5.9f, 12f, 7.1f)
        curveTo(12.7f, 5.9f, 14f, 5.2f, 15.4f, 5.2f)
        curveTo(17.8f, 5.2f, 19.7f, 7.2f, 19.7f, 9.7f)
        curveTo(19.7f, 14.1f, 13.6f, 18.2f, 12f, 19.6f)
        close()
    }

    val Check = stroke("check", 2.1f) {
        moveTo(5f, 12.6f); lineTo(9.8f, 17.2f); lineTo(19f, 6.8f)
    }

    val Lock = stroke("lock") {
        moveTo(6.8f, 10.5f); lineTo(17.2f, 10.5f)
        lineTo(17.2f, 19.2f)
        curveTo(17.2f, 20f, 16.5f, 20.7f, 15.7f, 20.7f)
        lineTo(8.3f, 20.7f)
        curveTo(7.5f, 20.7f, 6.8f, 20f, 6.8f, 19.2f)
        close()
        moveTo(8.9f, 10.5f); lineTo(8.9f, 8.1f)
        curveTo(8.9f, 6.2f, 10.3f, 4.6f, 12f, 4.6f)
        curveTo(13.7f, 4.6f, 15.1f, 6.2f, 15.1f, 8.1f)
        lineTo(15.1f, 10.5f)
        moveTo(12f, 14.3f); lineTo(12f, 16.9f)
    }

    val Unlock = stroke("unlock") {
        moveTo(6.8f, 10.5f); lineTo(17.2f, 10.5f)
        lineTo(17.2f, 19.2f)
        curveTo(17.2f, 20f, 16.5f, 20.7f, 15.7f, 20.7f)
        lineTo(8.3f, 20.7f)
        curveTo(7.5f, 20.7f, 6.8f, 20f, 6.8f, 19.2f)
        close()
        moveTo(8.9f, 10.5f); lineTo(8.9f, 7.2f)
        curveTo(8.9f, 5.6f, 10.3f, 4.6f, 12f, 4.6f)
        curveTo(13.4f, 4.6f, 14.6f, 5.4f, 15f, 6.6f)
    }

    val Download = stroke("download") {
        moveTo(12f, 4.2f); lineTo(12f, 15.2f)
        moveTo(7.8f, 11.2f); lineTo(12f, 15.4f); lineTo(16.2f, 11.2f)
        moveTo(4.8f, 19.4f); lineTo(19.2f, 19.4f)
    }

    val Trash = stroke("trash") {
        moveTo(5.2f, 7.4f); lineTo(18.8f, 7.4f)
        moveTo(9.4f, 7.4f); lineTo(9.4f, 5.2f)
        curveTo(9.4f, 4.7f, 9.8f, 4.3f, 10.3f, 4.3f)
        lineTo(13.7f, 4.3f)
        curveTo(14.2f, 4.3f, 14.6f, 4.7f, 14.6f, 5.2f)
        lineTo(14.6f, 7.4f)
        moveTo(7f, 7.4f); lineTo(7.9f, 19.4f)
        curveTo(7.9f, 20f, 8.4f, 20.5f, 9f, 20.5f)
        lineTo(15f, 20.5f)
        curveTo(15.6f, 20.5f, 16.1f, 20f, 16.1f, 19.4f)
        lineTo(17f, 7.4f)
        moveTo(10.6f, 11f); lineTo(10.6f, 17f)
        moveTo(13.4f, 11f); lineTo(13.4f, 17f)
    }

    val Sliders = stroke("sliders") {
        moveTo(4f, 7.5f); lineTo(20f, 7.5f)
        moveTo(4f, 16.5f); lineTo(20f, 16.5f)
        moveTo(9f, 5.2f); lineTo(9f, 9.8f)
        moveTo(15.5f, 14.2f); lineTo(15.5f, 18.8f)
    }

    val Moon = filled("moon") {
        moveTo(20f, 14.6f)
        curveTo(18.9f, 15.2f, 17.6f, 15.5f, 16.3f, 15.5f)
        curveTo(11.9f, 15.5f, 8.4f, 12f, 8.4f, 7.6f)
        curveTo(8.4f, 6.3f, 8.7f, 5.1f, 9.3f, 4f)
        curveTo(5.7f, 5.1f, 3f, 8.4f, 3f, 12.3f)
        curveTo(3f, 17f, 6.8f, 20.8f, 11.5f, 20.8f)
        curveTo(15.5f, 20.8f, 18.9f, 18.2f, 20f, 14.6f)
        close()
    }

    val Sun = stroke("sun") {
        moveTo(12f, 7.8f)
        curveTo(14.3f, 7.8f, 16.2f, 9.7f, 16.2f, 12f)
        curveTo(16.2f, 14.3f, 14.3f, 16.2f, 12f, 16.2f)
        curveTo(9.7f, 16.2f, 7.8f, 14.3f, 7.8f, 12f)
        curveTo(7.8f, 9.7f, 9.7f, 7.8f, 12f, 7.8f)
        close()
        moveTo(12f, 3.4f); lineTo(12f, 5f)
        moveTo(12f, 19f); lineTo(12f, 20.6f)
        moveTo(3.4f, 12f); lineTo(5f, 12f)
        moveTo(19f, 12f); lineTo(20.6f, 12f)
        moveTo(5.9f, 5.9f); lineTo(7f, 7f)
        moveTo(17f, 17f); lineTo(18.1f, 18.1f)
    }

    val Bell = stroke("bell") {
        moveTo(12f, 3.8f)
        curveTo(15.4f, 3.8f, 18.2f, 6.6f, 18.2f, 10f)
        lineTo(18.2f, 13.4f)
        lineTo(19.8f, 16.4f)
        lineTo(4.2f, 16.4f)
        lineTo(5.8f, 13.4f)
        lineTo(5.8f, 10f)
        curveTo(5.8f, 6.6f, 8.6f, 3.8f, 12f, 3.8f)
        close()
        moveTo(10f, 19.4f)
        curveTo(10.5f, 20.2f, 11.2f, 20.6f, 12f, 20.6f)
        curveTo(12.8f, 20.6f, 13.5f, 20.2f, 14f, 19.4f)
    }

    val Sparkle = filled("sparkle") {
        moveTo(12f, 2.8f)
        curveTo(12.9f, 7.4f, 16.6f, 11.1f, 21.2f, 12f)
        curveTo(16.6f, 12.9f, 12.9f, 16.6f, 12f, 21.2f)
        curveTo(11.1f, 16.6f, 7.4f, 12.9f, 2.8f, 12f)
        curveTo(7.4f, 11.1f, 11.1f, 7.4f, 12f, 2.8f)
        close()
    }

    val SparkleSmall = filled("sparkleSmall") {
        moveTo(12f, 6.4f)
        curveTo(12.5f, 9f, 14.6f, 11.1f, 17.2f, 11.6f)
        curveTo(14.6f, 12.1f, 12.5f, 14.2f, 12f, 16.8f)
        curveTo(11.5f, 14.2f, 9.4f, 12.1f, 6.8f, 11.6f)
        curveTo(9.4f, 11.1f, 11.5f, 9f, 12f, 6.4f)
        close()
    }

    val PlayFilled = filled("playFilled") {
        moveTo(8.4f, 5.6f)
        curveTo(8.4f, 4.9f, 9.2f, 4.4f, 9.9f, 4.8f)
        lineTo(18.9f, 10.4f)
        curveTo(19.5f, 10.9f, 19.5f, 11.7f, 18.9f, 12.2f)
        lineTo(9.9f, 17.8f)
        curveTo(9.2f, 18.2f, 8.4f, 17.7f, 8.4f, 17f)
        close()
    }

    val Pause = filled("pause") {
        moveTo(7.5f, 5.5f); lineTo(10.2f, 5.5f); lineTo(10.2f, 18.5f); lineTo(7.5f, 18.5f); close()
        moveTo(13.8f, 5.5f); lineTo(16.5f, 5.5f); lineTo(16.5f, 18.5f); lineTo(13.8f, 18.5f); close()
    }

    val Calendar = stroke("calendar") {
        moveTo(4.8f, 7.2f); lineTo(19.2f, 7.2f)
        lineTo(19.2f, 19.4f)
        curveTo(19.2f, 20f, 18.7f, 20.5f, 18.1f, 20.5f)
        lineTo(5.9f, 20.5f)
        curveTo(5.3f, 20.5f, 4.8f, 20f, 4.8f, 19.4f)
        close()
        moveTo(8.6f, 4.4f); lineTo(8.6f, 8.2f)
        moveTo(15.4f, 4.4f); lineTo(15.4f, 8.2f)
        moveTo(4.8f, 11.6f); lineTo(19.2f, 11.6f)
    }

    val Image = stroke("image") {
        moveTo(6f, 4.8f); lineTo(18f, 4.8f)
        curveTo(19f, 4.8f, 19.7f, 5.5f, 19.7f, 6.5f)
        lineTo(19.7f, 17.5f)
        curveTo(19.7f, 18.5f, 19f, 19.2f, 18f, 19.2f)
        lineTo(6f, 19.2f)
        curveTo(5f, 19.2f, 4.3f, 18.5f, 4.3f, 17.5f)
        lineTo(4.3f, 6.5f)
        curveTo(4.3f, 5.5f, 5f, 4.8f, 6f, 4.8f)
        close()
        moveTo(8.8f, 9.4f)
        curveTo(9.6f, 9.4f, 10.2f, 10f, 10.2f, 10.8f)
        curveTo(10.2f, 11.6f, 9.6f, 12.2f, 8.8f, 12.2f)
        curveTo(8f, 12.2f, 7.4f, 11.6f, 7.4f, 10.8f)
        curveTo(7.4f, 10f, 8f, 9.4f, 8.8f, 9.4f)
        close()
        moveTo(4.6f, 16.4f); lineTo(9.6f, 12.4f); lineTo(13.2f, 15.4f); lineTo(16f, 13.2f); lineTo(19.4f, 16.2f)
    }

    val Paperclip = stroke("paperclip") {
        moveTo(17.6f, 10.8f); lineTo(11.4f, 17f)
        curveTo(9.4f, 19f, 6.2f, 19f, 4.2f, 17f)
        curveTo(2.2f, 15f, 2.2f, 11.8f, 4.2f, 9.8f)
        lineTo(12.2f, 1.8f)
        curveTo(13.5f, 0.5f, 15.6f, 0.5f, 16.9f, 1.8f)
        curveTo(18.2f, 3.1f, 18.2f, 5.2f, 16.9f, 6.5f)
        lineTo(8.6f, 14.8f)
        curveTo(8f, 15.4f, 7f, 15.4f, 6.4f, 14.8f)
        curveTo(5.8f, 14.2f, 5.8f, 13.2f, 6.4f, 12.6f)
        lineTo(13.4f, 5.6f)
    }

    val Mic = stroke("mic") {
        moveTo(12f, 3.6f)
        curveTo(13.5f, 3.6f, 14.6f, 4.8f, 14.6f, 6.3f)
        lineTo(14.6f, 11.7f)
        curveTo(14.6f, 13.2f, 13.5f, 14.4f, 12f, 14.4f)
        curveTo(10.5f, 14.4f, 9.4f, 13.2f, 9.4f, 11.7f)
        lineTo(9.4f, 6.3f)
        curveTo(9.4f, 4.8f, 10.5f, 3.6f, 12f, 3.6f)
        close()
        moveTo(6.2f, 11.4f)
        curveTo(6.2f, 14.6f, 8.8f, 17.2f, 12f, 17.2f)
        curveTo(15.2f, 17.2f, 17.8f, 14.6f, 17.8f, 11.4f)
        moveTo(12f, 17.2f); lineTo(12f, 20.4f)
    }

    val Clock = stroke("clock") {
        moveTo(12f, 4f)
        curveTo(16.4f, 4f, 20f, 7.6f, 20f, 12f)
        curveTo(20f, 16.4f, 16.4f, 20f, 12f, 20f)
        curveTo(7.6f, 20f, 4f, 16.4f, 4f, 12f)
        curveTo(4f, 7.6f, 7.6f, 4f, 12f, 4f)
        close()
        moveTo(12f, 7.4f); lineTo(12f, 12.2f); lineTo(15.4f, 14.2f)
    }

    val Sprout = stroke("sprout") {
        moveTo(12f, 20.4f); lineTo(12f, 11.6f)
        moveTo(12f, 11.6f)
        curveTo(12f, 8f, 9.2f, 5.2f, 5.6f, 5.2f)
        curveTo(5.6f, 8.8f, 8.4f, 11.6f, 12f, 11.6f)
        close()
        moveTo(12f, 13.4f)
        curveTo(12f, 10.4f, 14.3f, 8.1f, 17.3f, 8.1f)
        curveTo(17.3f, 11.1f, 15f, 13.4f, 12f, 13.4f)
        close()
    }

    val Dots = filled("dots") {
        moveTo(6f, 12f)
        curveTo(6f, 13.1f, 5.1f, 14f, 4f, 14f)
        curveTo(2.9f, 14f, 2f, 13.1f, 2f, 12f)
        curveTo(2f, 10.9f, 2.9f, 10f, 4f, 10f)
        curveTo(5.1f, 10f, 6f, 10.9f, 6f, 12f)
        close()
        moveTo(14f, 12f)
        curveTo(14f, 13.1f, 13.1f, 14f, 12f, 14f)
        curveTo(10.9f, 14f, 10f, 13.1f, 10f, 12f)
        curveTo(10f, 10.9f, 10.9f, 10f, 12f, 10f)
        curveTo(13.1f, 10f, 14f, 10.9f, 14f, 12f)
        close()
        moveTo(22f, 12f)
        curveTo(22f, 13.1f, 21.1f, 14f, 20f, 14f)
        curveTo(18.9f, 14f, 18f, 13.1f, 18f, 12f)
        curveTo(18f, 10.9f, 18.9f, 10f, 20f, 10f)
        curveTo(21.1f, 10f, 22f, 10.9f, 22f, 12f)
        close()
    }

    val Send = filled("send") {
        moveTo(3.4f, 10.6f)
        lineTo(19.2f, 4.1f)
        curveTo(20.1f, 3.7f, 20.9f, 4.6f, 20.5f, 5.4f)
        lineTo(14f, 21.2f)
        curveTo(13.6f, 22.1f, 12.3f, 21.9f, 12.2f, 20.9f)
        lineTo(11.1f, 14.3f)
        lineTo(4.4f, 13.2f)
        curveTo(3.4f, 13.1f, 3.3f, 11.8f, 4.2f, 11.4f)
        close()
    }

    val ArrowRight = stroke("arrowRight") {
        moveTo(4.5f, 12f); lineTo(19f, 12f)
        moveTo(13.6f, 6.6f); lineTo(19f, 12f); lineTo(13.6f, 17.4f)
    }

    val Refresh = stroke("refresh") {
        moveTo(19.4f, 12f)
        curveTo(19.4f, 16.1f, 16.1f, 19.4f, 12f, 19.4f)
        curveTo(7.9f, 19.4f, 4.6f, 16.1f, 4.6f, 12f)
        curveTo(4.6f, 7.9f, 7.9f, 4.6f, 12f, 4.6f)
        curveTo(14.5f, 4.6f, 16.7f, 5.8f, 18f, 7.7f)
        moveTo(18.8f, 3.8f); lineTo(18.8f, 8.2f); lineTo(14.4f, 8.2f)
    }

    val Filter = stroke("filter") {
        moveTo(4f, 6.2f); lineTo(20f, 6.2f)
        lineTo(13.9f, 13.2f); lineTo(13.9f, 19.4f); lineTo(10.1f, 17.4f); lineTo(10.1f, 13.2f)
        close()
    }

    val Edit = stroke("edit") {
        moveTo(4.5f, 19.5f); lineTo(5.2f, 15.4f); lineTo(15.6f, 5f)
        curveTo(16.4f, 4.2f, 17.7f, 4.2f, 18.5f, 5f)
        lineTo(19f, 5.5f)
        curveTo(19.8f, 6.3f, 19.8f, 7.6f, 19f, 8.4f)
        lineTo(8.6f, 18.8f)
        close()
    }

    val Share = stroke("share") {
        moveTo(17.5f, 8.4f)
        curveTo(19.2f, 8.4f, 20.6f, 7f, 20.6f, 5.3f)
        curveTo(20.6f, 3.6f, 19.2f, 2.2f, 17.5f, 2.2f)
        curveTo(15.8f, 2.2f, 14.4f, 3.6f, 14.4f, 5.3f)
        curveTo(14.4f, 7f, 15.8f, 8.4f, 17.5f, 8.4f)
        close()
        moveTo(6.5f, 15.1f)
        curveTo(8.2f, 15.1f, 9.6f, 13.7f, 9.6f, 12f)
        curveTo(9.6f, 10.3f, 8.2f, 8.9f, 6.5f, 8.9f)
        curveTo(4.8f, 8.9f, 3.4f, 10.3f, 3.4f, 12f)
        curveTo(3.4f, 13.7f, 4.8f, 15.1f, 6.5f, 15.1f)
        close()
        moveTo(17.5f, 21.8f)
        curveTo(19.2f, 21.8f, 20.6f, 20.4f, 20.6f, 18.7f)
        curveTo(20.6f, 17f, 19.2f, 15.6f, 17.5f, 15.6f)
        curveTo(15.8f, 15.6f, 14.4f, 17f, 14.4f, 18.7f)
        curveTo(14.4f, 20.4f, 15.8f, 21.8f, 17.5f, 21.8f)
        close()
        moveTo(9.4f, 13.4f); lineTo(14.7f, 17.3f)
        moveTo(14.7f, 6.7f); lineTo(9.4f, 10.6f)
    }

    val Bookmark = stroke("bookmark") {
        moveTo(6.5f, 4.4f); lineTo(17.5f, 4.4f)
        curveTo(18.2f, 4.4f, 18.7f, 4.9f, 18.7f, 5.6f)
        lineTo(18.7f, 20.2f)
        lineTo(12f, 15.6f)
        lineTo(5.3f, 20.2f)
        lineTo(5.3f, 5.6f)
        curveTo(5.3f, 4.9f, 5.8f, 4.4f, 6.5f, 4.4f)
        close()
    }

    val Leaf = filled("leaf") {
        moveTo(20.4f, 3.6f)
        curveTo(12.4f, 3.6f, 6f, 7.2f, 6f, 13.2f)
        curveTo(6f, 14.6f, 6.3f, 15.8f, 6.9f, 16.9f)
        curveTo(8.2f, 18.6f, 10.3f, 19.4f, 12.6f, 19.4f)
        curveTo(18.6f, 19.4f, 20.4f, 11.6f, 20.4f, 3.6f)
        close()
        moveTo(4.2f, 21f)
        curveTo(5.4f, 16.2f, 9f, 12.6f, 13.8f, 10.2f)
    }

    val Info = stroke("info") {
        moveTo(12f, 4f)
        curveTo(16.4f, 4f, 20f, 7.6f, 20f, 12f)
        curveTo(20f, 16.4f, 16.4f, 20f, 12f, 20f)
        curveTo(7.6f, 20f, 4f, 16.4f, 4f, 12f)
        curveTo(4f, 7.6f, 7.6f, 4f, 12f, 4f)
        close()
        moveTo(12f, 11f); lineTo(12f, 16.2f)
        moveTo(12f, 7.9f); lineTo(12.05f, 7.9f)
    }
}
