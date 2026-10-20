package com.kairos.app.ui.garden

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.ui.theme.SerifFamily

@Composable
fun GardenWelcome(onContinue: () -> Unit) {
    GardenTheme {
        Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background) {
            Column(Modifier.statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(32.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(24.dp)) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment=Alignment.CenterVertically) { SeedMark(Modifier.size(48.dp));Text("kairos",fontFamily=SerifFamily,fontSize=32.sp,modifier=Modifier.padding(start=12.dp)) }
                GardenPainting(Modifier.fillMaxWidth().height(240.dp))
                Text("Keep something\ngood.",fontFamily=SerifFamily,fontSize=44.sp,lineHeight=50.sp)
                Text("A new word to get lost in. A thought to carry. A letter to the person you're becoming.",style=MaterialTheme.typography.bodyLarge)
                Button(onClick=onContinue,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp)) { Text("Plant a little beginning") }
                Text("Start privately on this device. No account needed.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Small things become a life.",fontFamily=SerifFamily,fontStyle=FontStyle.Italic,modifier=Modifier.padding(top=16.dp))
            }
        }
    }
}
