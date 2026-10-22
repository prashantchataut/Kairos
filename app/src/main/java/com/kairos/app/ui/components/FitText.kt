package com.kairos.app.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Finds the largest font size (between [min] and [max]) at which [text] fits [maxWidthPx] x
 * [maxHeightPx] without breaking a word across lines. Line height scales with the size.
 */
fun fitSize(measurer: TextMeasurer, text: String, style: TextStyle, maxWidthPx: Int, maxHeightPx: Int, max: TextUnit, min: TextUnit, lineHeight: Float = 1.15f): TextUnit {
    if (maxWidthPx <= 0 || maxHeightPx <= 0) return min
    val longest = text.split(Regex("\\s+")).maxByOrNull { it.length }.orEmpty()
    fun fits(size: Float): Boolean {
        val st = style.copy(fontSize = size.sp, lineHeight = (size * lineHeight).sp)
        val word = measurer.measure(longest, st, maxLines = 1, softWrap = false)
        if (word.size.width > maxWidthPx) return false
        val all = measurer.measure(text, st, constraints = Constraints(maxWidth = maxWidthPx))
        return all.size.height <= maxHeightPx
    }
    var lo = min.value
    var hi = max.value
    if (fits(hi)) return max
    repeat(9) {
        val mid = (lo + hi) / 2
        if (fits(mid)) lo = mid else hi = mid
    }
    return lo.sp
}

/** Text that fills its box: as large as it can be up to [max], never overflowing, never clipped mid-word. */
@Composable
fun FitText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    max: TextUnit = 64.sp,
    min: TextUnit = 10.sp,
    lineHeight: Float = 1.15f,
    align: TextAlign = TextAlign.Start,
    contentAlignment: Alignment = Alignment.TopStart,
) {
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier, contentAlignment = contentAlignment) {
        val w = constraints.maxWidth
        val h = if (constraints.hasBoundedHeight) constraints.maxHeight else Int.MAX_VALUE / 4
        val size = remember(text, style, w, h, max, min, lineHeight) { fitSize(measurer, text, style.copy(textAlign = align), w, h, max, min, lineHeight) }
        Text(text, style = style.copy(fontSize = size, lineHeight = size * lineHeight, textAlign = align), color = color)
    }
}

