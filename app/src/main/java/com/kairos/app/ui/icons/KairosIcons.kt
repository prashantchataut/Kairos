package com.kairos.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Kairos' own icon set: a 24dp grid, 1.9dp round strokes, softened corners. Drawn to sit next to
 * Nunito, so they share its rounded terminals. Filled glyphs are used only for "on" states.
 */
object KairosIcons {
    private fun stroke(name: String, vararg paths: String) = ImageVector.Builder(
        name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
    ).apply {
        paths.forEach {
            addPath(
                pathData = addPathNodes(it),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

    private fun fill(name: String, vararg paths: String) = ImageVector.Builder(
        name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
    ).apply {
        paths.forEach { addPath(pathData = addPathNodes(it), fill = SolidColor(Color.Black)) }
    }.build()

    private const val HEART = "M12 19.5s-7-4.3-7-9.6C5 7.4 6.9 5.5 9.2 5.5c1.3 0 2.3.6 2.8 1.6.5-1 1.5-1.6 2.8-1.6 2.3 0 4.2 1.9 4.2 4.4 0 5.3-7 9.6-7 9.6z"

    val Today = stroke("Today",
        "M8 12a4 4 0 1 0 8 0a4 4 0 1 0 -8 0",
        "M12 3.5v1.6", "M12 18.9v1.6", "M3.5 12h1.6", "M18.9 12h1.6",
        "M6 6l1.1 1.1", "M16.9 16.9L18 18", "M6 18l1.1-1.1", "M16.9 7.1L18 6")
    val Words = stroke("Words",
        "M12 6.5C10 5 7 4.6 4 5v13c3-.4 6 0 8 1.5 2-1.5 5-1.9 8-1.5V5c-3-.4-6 0-8 1.5z", "M12 6.5V19")
    val Capsule = stroke("Capsule",
        "M5.5 6h13A1.5 1.5 0 0 1 20 7.5v9a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 16.5v-9A1.5 1.5 0 0 1 5.5 6z",
        "M4.6 7.2L12 13l7.4-5.8")
    val Me = stroke("Me",
        "M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0", "M9.2 10v.4", "M14.8 10v.4", "M8.8 14.2c1.8 2 4.6 2 6.4 0")
    val Back = stroke("Back", "M14.5 5.5L8 12l6.5 6.5")
    val Close = stroke("Close", "M6.5 6.5l11 11", "M17.5 6.5l-11 11")
    val Plus = stroke("Plus", "M12 5.5v13", "M5.5 12h13")
    val Heart = stroke("Heart", HEART)
    val HeartFilled = fill("HeartFilled", HEART)
    val Share = stroke("Share",
        "M12 4v10.5", "M8.2 7.6L12 4l3.8 3.6",
        "M8 11H6.5A1.5 1.5 0 0 0 5 12.5v6A1.5 1.5 0 0 0 6.5 20h11a1.5 1.5 0 0 0 1.5-1.5v-6a1.5 1.5 0 0 0-1.5-1.5H16")
    val Search = stroke("Search", "M5 10.5a5.5 5.5 0 1 0 11 0a5.5 5.5 0 1 0 -11 0", "M15 15l4.5 4.5")
    val Sparkle = fill("Sparkle", "M12 3c.6 4.5 1.9 6.3 6.5 9-4.6 2.7-5.9 4.5-6.5 9-.6-4.5-1.9-6.3-6.5-9 4.6-2.7 5.9-4.5 6.5-9z")
    val Lock = stroke("Lock",
        "M7.5 11V8.5a4.5 4.5 0 0 1 9 0V11",
        "M6.5 11h11a1.5 1.5 0 0 1 1.5 1.5v6a1.5 1.5 0 0 1-1.5 1.5h-11A1.5 1.5 0 0 1 5 18.5v-6A1.5 1.5 0 0 1 6.5 11z",
        "M12 14.5v2")
    val Image = stroke("Image",
        "M5.5 5h13A1.5 1.5 0 0 1 20 6.5v11a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 17.5v-11A1.5 1.5 0 0 1 5.5 5z",
        "M4.5 16l4.5-4.5 3.5 3.5 2.5-2.5 4.5 4.5", "M14.6 9.2h.1")
    val Clip = stroke("Clip",
        "M16.5 8.5l-6.8 6.8a1.8 1.8 0 0 0 2.5 2.5l7-7a3.5 3.5 0 0 0-5-5l-7.2 7.2a5.2 5.2 0 0 0 7.4 7.4l5.6-5.6")
    val Mic = stroke("Mic", "M9 7a3 3 0 0 1 6 0v4a3 3 0 0 1-6 0z", "M6 11a6 6 0 0 0 12 0", "M12 17v3")
    val Send = stroke("Send", "M4.5 11.5L19.5 4.5l-5 15-3-6.5z", "M11.5 13L15 9.5")
    val Calendar = stroke("Calendar",
        "M5.5 6h13A1.5 1.5 0 0 1 20 7.5v11a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 18.5v-11A1.5 1.5 0 0 1 5.5 6z",
        "M4 10h16", "M8.5 4v3.5", "M15.5 4v3.5")
    val Bell = stroke("Bell", "M6.5 16.5V11a5.5 5.5 0 0 1 11 0v5.5l1.5 1.5H5z", "M10 20.5h4")
    val Sliders = stroke("Sliders",
        "M4.5 8h8", "M17.5 8h2", "M4.5 16h2", "M11.5 16h8",
        "M12.5 8a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0", "M6.5 16a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0")
    val Check = stroke("Check", "M5.5 12.5l4 4 9-9")
    val Shuffle = stroke("Shuffle", "M18.5 9A7 7 0 0 0 5.6 8", "M5.5 15a7 7 0 0 0 12.9 1", "M18.5 4.5V9H14", "M5.5 19.5V15H10")
    val Download = stroke("Download", "M12 4.5v10", "M8 11l4 4 4-4", "M5 19.5h14")
    val Speaker = stroke("Speaker", "M5 10v4h3l4.5 3.5v-11L8 10z", "M15.5 9.5a3.5 3.5 0 0 1 0 5", "M17.8 7.2a6.8 6.8 0 0 1 0 9.6")
    val Chevron = stroke("Chevron", "M10 6l6 6-6 6")
    val Palette = stroke("Palette",
        "M12 4a8 8 0 0 0 0 16c1.1 0 1.6-.8 1.3-1.7-.4-1.1.3-2.3 1.5-2.3H17a3 3 0 0 0 3-3c0-5-3.6-9-8-9z",
        "M8.3 11.2h.1", "M10.8 7.8h.1", "M15 8.2h.1")
    val Quote = fill("Quote",
        "M6 17c-1.7 0-2.5-1.3-2.5-3 0-3.2 2-6 5-7l.6 1.1C7.4 9 6.6 10.3 6.5 11.5c1.6 0 2.7 1.1 2.7 2.7S7.8 17 6 17z",
        "M15 17c-1.7 0-2.5-1.3-2.5-3 0-3.2 2-6 5-7l.6 1.1c-1.7.9-2.5 2.2-2.6 3.4 1.6 0 2.7 1.1 2.7 2.7S16.8 17 15 17z")
    val Cards = stroke("Cards",
        "M8 5.5h9.5A1.5 1.5 0 0 1 19 7v11",
        "M5.5 9h8a1.5 1.5 0 0 1 1.5 1.5v8a1.5 1.5 0 0 1-1.5 1.5h-8A1.5 1.5 0 0 1 4 18.5v-8A1.5 1.5 0 0 1 5.5 9z")
    val Trash = stroke("Trash", "M5 7h14", "M10 4.5h4", "M7 7l.8 11.5a1.5 1.5 0 0 0 1.5 1.5h5.4a1.5 1.5 0 0 0 1.5-1.5L17 7")
    val Play = fill("Play", "M8.5 5.8v12.4a.8.8 0 0 0 1.2.7l9.6-6.2a.8.8 0 0 0 0-1.4L9.7 5.1a.8.8 0 0 0-1.2.7z")
    val Stop = fill("Stop", "M8 7h8a1 1 0 0 1 1 1v8a1 1 0 0 1-1 1H8a1 1 0 0 1-1-1V8a1 1 0 0 1 1-1z")
    val Leaf = stroke("Leaf", "M5 19c0-8 5-13 14-14-1 9-6 14-14 14z", "M5 19l7-7")
    val File = stroke("File",
        "M7 3.5h7l4.5 4.5v11A1.5 1.5 0 0 1 17 20.5H7A1.5 1.5 0 0 1 5.5 19V5A1.5 1.5 0 0 1 7 3.5z", "M13.5 3.5V8.5h5")
    val Moon = stroke("Moon", "M19 14.5A7.5 7.5 0 0 1 9.5 5a7.5 7.5 0 1 0 9.5 9.5z")
    val Clock = stroke("Clock", "M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0", "M12 7.5V12l3 2")
    val Bookmark = stroke("Bookmark", "M7 4.5h10a1 1 0 0 1 1 1v14l-6-4-6 4v-14a1 1 0 0 1 1-1z")
    val Edit = stroke("Edit", "M5 19l1-4L15.5 5.5a2.1 2.1 0 0 1 3 3L9 18z", "M13.5 7.5l3 3")
    val Grid = stroke("Grid",
        "M5 4.5h5a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-.5.5H5a.5.5 0 0 1-.5-.5V5a.5.5 0 0 1 .5-.5z",
        "M14 4.5h5a.5.5 0 0 1 .5.5v3a.5.5 0 0 1-.5.5h-5a.5.5 0 0 1-.5-.5V5a.5.5 0 0 1 .5-.5z",
        "M14 11.5h5a.5.5 0 0 1 .5.5v7a.5.5 0 0 1-.5.5h-5a.5.5 0 0 1-.5-.5v-7a.5.5 0 0 1 .5-.5z",
        "M5 14.5h5a.5.5 0 0 1 .5.5v4a.5.5 0 0 1-.5.5H5a.5.5 0 0 1-.5-.5v-4a.5.5 0 0 1 .5-.5z")
}
