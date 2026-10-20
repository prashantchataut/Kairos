package com.kairos.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kairos.app.data.prefs.Settings
import com.kairos.app.ui.nav.KairosRoot
import com.kairos.app.ui.theme.KairosTheme

class MainActivity : ComponentActivity() {
    companion object { const val EXTRA_ROUTE = "route" }

    private val pendingRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingRoute.value = intent?.getStringExtra(EXTRA_ROUTE)
        val store = container.settings
        var ready = false
        splash.setKeepOnScreenCondition { !ready }
        setContent {
            val settings by store.settings.collectAsState(initial = null)
            val s: Settings? = settings
            if (s != null) {
                ready = true
                KairosTheme(mode = s.theme, reduceMotionPref = s.reduceMotion) {
                    KairosRoot(
                        settings = s,
                        pendingRoute = pendingRoute.value,
                        onRouteConsumed = { pendingRoute.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(EXTRA_ROUTE)?.let { pendingRoute.value = it }
    }
}
