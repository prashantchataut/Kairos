package com.kairos.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.kairos.app.MainActivity
import com.kairos.app.R
import com.kairos.app.container

/** Home-screen word of the day, painted on a watercolor card. */
class WordWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = runCatching { context.container.today.ensureToday() }.getOrNull()
        val word = today?.word?.item
        val ink = ColorProvider(Color(0xFF1B2340))
        val soft = ColorProvider(Color(0xFF545D80))
        provideContent {
            Column(
                GlanceModifier
                    .fillMaxSize()
                    .background(ImageProvider(R.drawable.widget_bg))
                    .padding(18.dp)
                    .clickable(actionStartActivity<MainActivity>()),
            ) {
                Text("WORD OF THE DAY", style = TextStyle(color = soft, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                Spacer(GlanceModifier.height(6.dp))
                Text(word?.word ?: "Kairos", style = TextStyle(color = ink, fontSize = 26.sp, fontWeight = FontWeight.Bold))
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    word?.let { "(${it.pos}) ${it.definition}" } ?: "Open Kairos to meet today's word.",
                    style = TextStyle(color = ink, fontSize = 14.sp),
                    maxLines = 3,
                )
                if (word != null && word.example.isNotBlank()) {
                    Spacer(GlanceModifier.height(6.dp))
                    Text("“${word.example}”", style = TextStyle(color = soft, fontSize = 12.sp, fontStyle = FontStyle.Italic), maxLines = 2)
                }
            }
        }
    }
}

class WordWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = WordWidget()
}
