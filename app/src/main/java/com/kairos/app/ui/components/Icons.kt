package com.kairos.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Kairos icons: 24dp grid, 1.7dp round strokes; nav icons also have solid "selected" forms. */
object Icons {
    private fun build(name: String, fill: Boolean, vararg paths: String) = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        paths.forEach {
            if (fill) addPath(addPathNodes(it), fill = SolidColor(Color.Black))
            else addPath(addPathNodes(it), stroke = SolidColor(Color.Black), strokeLineWidth = 1.7f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round)
        }
    }.build()
    private fun s(name: String, vararg p: String) = build(name, false, *p)
    private fun f(name: String, vararg p: String) = build(name, true, *p)

    private const val HOME = "M4.5 10.2 12 4.5l7.5 5.7V19a1.5 1.5 0 0 1-1.5 1.5H6A1.5 1.5 0 0 1 4.5 19z"
    private const val BOOK = "M12 6.6C10.2 5.2 7.4 4.7 4.5 5v13.2c2.9-.3 5.7.2 7.5 1.6 1.8-1.4 4.6-1.9 7.5-1.6V5c-2.9-.3-5.7.2-7.5 1.6z"
    private const val CLOCK_RING = "M3.8 12a8.2 8.2 0 1 0 16.4 0a8.2 8.2 0 1 0 -16.4 0"
    private const val PERSON_HEAD = "M8.6 8.2a3.4 3.4 0 1 0 6.8 0a3.4 3.4 0 1 0 -6.8 0"
    private const val PERSON_BODY = "M5 19.6c.9-3.3 3.7-5.3 7-5.3s6.1 2 7 5.3"
    private const val HEART = "M12 19.4s-7-4.3-7-9.5C5 7.4 6.9 5.6 9.2 5.6c1.3 0 2.3.6 2.8 1.6.5-1 1.5-1.6 2.8-1.6 2.3 0 4.2 1.8 4.2 4.3 0 5.2-7 9.5-7 9.5z"

    val Today = s("Today", HOME, "M9.8 20.5v-5h4.4v5")
    val TodayOn = f("TodayOn", HOME)
    val Learn = s("Learn", BOOK, "M12 6.6v13.2")
    val LearnOn = f("LearnOn", BOOK)
    val Capsule = s("Capsule", CLOCK_RING, "M12 7.6V12l3 1.8")
    val CapsuleOn = f("CapsuleOn", "M12 3.8a8.2 8.2 0 1 1 0 16.4a8.2 8.2 0 1 1 0-16.4zm-.85 3.8v4.88l3.4 2.04.87-1.46-2.57-1.54V7.6z")
    val You = s("You", PERSON_HEAD, PERSON_BODY)
    val YouOn = f("YouOn", PERSON_HEAD, "M4.9 20c.8-3.6 3.6-5.9 7.1-5.9s6.3 2.3 7.1 5.9z")

    val Plus = s("Plus", "M12 5.5v13", "M5.5 12h13")
    val Close = s("Close", "M6.5 6.5l11 11", "M17.5 6.5l-11 11")
    val Back = s("Back", "M14.5 5.5 8 12l6.5 6.5")
    val Chevron = s("Chevron", "M10 6.5 15.5 12 10 17.5")
    val Heart = s("Heart", HEART)
    val HeartOn = f("HeartOn", HEART)
    val Share = s("Share", "M12 4v10", "M8.3 7.5 12 4l3.7 3.5", "M8 10.5H6.8A1.8 1.8 0 0 0 5 12.3v5.9A1.8 1.8 0 0 0 6.8 20h10.4a1.8 1.8 0 0 0 1.8-1.8v-5.9a1.8 1.8 0 0 0-1.8-1.8H16")
    val Speaker = s("Speaker", "M5 10v4h3l4.5 3.5v-11L8 10z", "M15.6 9.6a3.4 3.4 0 0 1 0 4.8", "M17.9 7.3a6.6 6.6 0 0 1 0 9.4")
    val Sparkle = f("Sparkle", "M12 3.2c.55 4.3 1.9 6.1 6.3 8.8-4.4 2.7-5.75 4.5-6.3 8.8-.55-4.3-1.9-6.1-6.3-8.8 4.4-2.7 5.75-4.5 6.3-8.8z")
    val Shuffle = s("Shuffle", "M18.5 9A7 7 0 0 0 5.6 8", "M5.5 15a7 7 0 0 0 12.9 1", "M18.5 4.5V9H14", "M5.5 19.5V15H10")
    val Search = s("Search", "M5 10.5a5.5 5.5 0 1 0 11 0a5.5 5.5 0 1 0 -11 0", "M15 15l4.5 4.5")
    val Arrow = s("Arrow", "M5 12h14", "M13 6l6 6-6 6")
    val ArrowUp = s("ArrowUp", "M12 19V5", "M6 11l6-6 6 6")
    val Check = s("Check", "M5.5 12.5l4 4 9-9")
    val Cards = s("Cards", "M8 5.5h9.5A1.5 1.5 0 0 1 19 7v11", "M5.5 9h8a1.5 1.5 0 0 1 1.5 1.5v8a1.5 1.5 0 0 1-1.5 1.5h-8A1.5 1.5 0 0 1 4 18.5v-8A1.5 1.5 0 0 1 5.5 9z")
    val Unlock = s("Unlock", "M7.5 11V8.5a4.5 4.5 0 0 1 8.6-1.9", "M6.5 11h11a1.5 1.5 0 0 1 1.5 1.5v6a1.5 1.5 0 0 1-1.5 1.5h-11A1.5 1.5 0 0 1 5 18.5v-6A1.5 1.5 0 0 1 6.5 11z")
    val Paste = s("Paste", "M9 4.5h6v2.5H9z", "M15 5.5h1.5A1.5 1.5 0 0 1 18 7v12a1.5 1.5 0 0 1-1.5 1.5h-9A1.5 1.5 0 0 1 6 19V7a1.5 1.5 0 0 1 1.5-1.5H9")
    val Lock = s("Lock", "M7.5 11V8.5a4.5 4.5 0 0 1 9 0V11", "M6.5 11h11a1.5 1.5 0 0 1 1.5 1.5v6a1.5 1.5 0 0 1-1.5 1.5h-11A1.5 1.5 0 0 1 5 18.5v-6A1.5 1.5 0 0 1 6.5 11z")
    val Image = s("Image", "M5.5 5h13A1.5 1.5 0 0 1 20 6.5v11a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 17.5v-11A1.5 1.5 0 0 1 5.5 5z", "M4.5 16l4.5-4.5 3.5 3.5 2.5-2.5 4.5 4.5", "M14.6 9.2h.1")
    val Folder = s("Folder", "M4 7.5A1.5 1.5 0 0 1 5.5 6h3.8l2 2h7.2A1.5 1.5 0 0 1 20 9.5v8a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 17.5z")
    val Mic = s("Mic", "M9 7a3 3 0 0 1 6 0v4a3 3 0 0 1-6 0z", "M6 11a6 6 0 0 0 12 0", "M12 17v3")
    val Calendar = s("Calendar", "M5.5 6h13A1.5 1.5 0 0 1 20 7.5v11a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 4 18.5v-11A1.5 1.5 0 0 1 5.5 6z", "M4 10h16", "M8.5 4v3.5", "M15.5 4v3.5")
    val Bell = s("Bell", "M6.5 16.5V11a5.5 5.5 0 0 1 11 0v5.5l1.5 1.5H5z", "M10 20.5h4")
    val Key = s("Key", "M4 12a3.6 3.6 0 1 0 7.2 0a3.6 3.6 0 1 0 -7.2 0", "M11.2 12H20", "M17 12v3", "M20 12v2.4", "M7.6 12h.01")
    val Sliders = s("Sliders", "M4.5 8h8", "M17.5 8h2", "M4.5 16h2", "M11.5 16h8", "M12.5 8a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0", "M6.5 16a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0")
    val Moon = s("Moon", "M19 14.5A7.5 7.5 0 0 1 9.5 5a7.5 7.5 0 1 0 9.5 9.5z")
    val Download = s("Download", "M12 4.5v10", "M8 11l4 4 4-4", "M5 19.5h14")
    val Edit = s("Edit", "M5 19l1-4L15.5 5.5a2.1 2.1 0 0 1 3 3L9 18z", "M13.5 7.5l3 3")
    val Trash = s("Trash", "M5 7h14", "M10 4.5h4", "M7 7l.8 11.5a1.5 1.5 0 0 0 1.5 1.5h5.4a1.5 1.5 0 0 0 1.5-1.5L17 7")
    val Play = f("Play", "M8.5 5.8v12.4a.8.8 0 0 0 1.2.7l9.6-6.2a.8.8 0 0 0 0-1.4L9.7 5.1a.8.8 0 0 0-1.2.7z")
    val Stop = f("Stop", "M8 7h8a1 1 0 0 1 1 1v8a1 1 0 0 1-1 1H8a1 1 0 0 1-1-1V8a1 1 0 0 1 1-1z")
    val File = s("File", "M7 3.5h7l4.5 4.5v11A1.5 1.5 0 0 1 17 20.5H7A1.5 1.5 0 0 1 5.5 19V5A1.5 1.5 0 0 1 7 3.5z", "M13.5 3.5V8.5h5")
    val Leaf = s("Leaf", "M5 19c0-8 5-13 14-14-1 9-6 14-14 14z", "M5 19l7-7")
    val Smile = s("Smile", "M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0", "M9.2 10v.4", "M14.8 10v.4", "M8.8 14.2c1.8 2 4.6 2 6.4 0")
    val Quote = f("Quote", "M6 17c-1.7 0-2.5-1.3-2.5-3 0-3.2 2-6 5-7l.6 1.1C7.4 9 6.6 10.3 6.5 11.5c1.6 0 2.7 1.1 2.7 2.7S7.8 17 6 17z", "M15 17c-1.7 0-2.5-1.3-2.5-3 0-3.2 2-6 5-7l.6 1.1c-1.7.9-2.5 2.2-2.6 3.4 1.6 0 2.7 1.1 2.7 2.7S16.8 17 15 17z")
    val ThumbUp = s("ThumbUp", "M7.5 10.5v9H5a1 1 0 0 1-1-1v-7a1 1 0 0 1 1-1z", "M7.5 10.5 11 4a2 2 0 0 1 2.4 2.3L13 9.5h5a2 2 0 0 1 2 2.3l-1 6A2 2 0 0 1 17 19.5H7.5")
    val ThumbDown = s("ThumbDown", "M7.5 13.5v-9H5a1 1 0 0 0-1 1v7a1 1 0 0 0 1 1z", "M7.5 13.5 11 20a2 2 0 0 0 2.4-2.3l-.4-3.2h5a2 2 0 0 0 2-2.3l-1-6A2 2 0 0 0 17 4.5H7.5")
}
