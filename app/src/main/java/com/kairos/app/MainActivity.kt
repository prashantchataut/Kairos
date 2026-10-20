package com.kairos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kairos.app.ui.KairosRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as KairosApp).container
        setContent {
            KairosRoot(container = container)
        }
    }

    override fun onStop() {
        super.onStop()
        (application as KairosApp).container.flush()
    }
}
