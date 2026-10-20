package com.kairos.app.ui.garden

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kairos.app.ui.theme.SerifFamily

@Composable
fun GardenSetup(saving: Boolean, onComplete: (Set<String>,Int) -> Unit) {
    var interests by rememberSaveable { mutableStateOf(listOf("mindfulness", "reflection")) }
    var level by rememberSaveable { mutableIntStateOf(2) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        SeedMark(Modifier.padding(top=24.dp).size(48.dp))
        Text("What would you\nlike to grow?",fontFamily=SerifFamily,fontSize=36.sp,lineHeight=44.sp)
        Text("Choose a few things that speak to you. Your daily word and thought will start here.")
        listOf("mindfulness" to "A little more calm","communication" to "Words that connect","self-improvement" to "Room to grow","reflection" to "A deeper understanding").forEach { (key,label) ->
            FilterChip(selected=key in interests,onClick={interests=if(key in interests)interests-key else interests+key},label={Text(label)},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),enabled=!saving)
        }
        Text("How adventurous should the words be?",style=MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            listOf("Gentle" to 1,"Curious" to 2,"Adventurous" to 4).forEach { (label,value) -> FilterChip(selected=level==value,onClick={level=value},label={Text(label)},enabled=!saving) }
        }
        Button(onClick={onComplete(interests.toSet(),level)},enabled=!saving,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp)) { Text(if(saving) "Preparing your garden…" else "Step into Kairos") }
        Text("Nothing to catch up on. Just a little beginning.",style=MaterialTheme.typography.bodySmall)
    }
}
