package com.kairos.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kairos.app.engine.Mood
import com.kairos.app.ui.mascot.DropMascot
import com.kairos.app.ui.mascot.expression
import com.kairos.app.ui.theme.Kairos
import com.kairos.app.ui.theme.Motion

/** Five little Drops, one per mood. The chosen one grows; the others step back. */
@Composable
fun MoodPicker(selected: Mood?, onPick: (Mood) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Mood.entries.forEach { mood ->
            val isSel = mood == selected
            val scale by animateFloatAsState(
                when {
                    selected == null -> 1f
                    isSel -> 1.18f
                    else -> 0.86f
                },
                Motion.squish(), label = "moodScale",
            )
            val alpha by animateFloatAsState(if (selected == null || isSel) 1f else 0.55f, label = "moodAlpha")
            Column(
                Modifier
                    .squishClickable(role = Role.RadioButton, label = mood.label) { onPick(mood) }
                    .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DropMascot(
                    size = 50.dp, wash = mood.wash, expression = mood.expression(),
                    animate = isSel, interactive = false,
                )
                Spacer(Modifier.height(4.dp))
                Text(mood.label, style = Kairos.type.caption, color = if (isSel) Kairos.colors.ink else Kairos.colors.inkSoft)
            }
        }
    }
}
