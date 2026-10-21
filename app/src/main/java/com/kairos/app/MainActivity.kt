package com.kairos.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.prefs.Settings
import com.kairos.app.ui.nav.KairosNav
import com.kairos.app.ui.theme.KairosTheme
import com.kairos.app.work.Notifier

class MainActivity : ComponentActivity() {
    private var deepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLink = intent?.getStringExtra(Notifier.EXTRA_ROUTE)
        val prefs = (application as KairosApp).container.prefs
        var ready = false
        splash.setKeepOnScreenCondition { !ready }
        setContent {
            val settings by prefs.settings.collectAsStateWithLifecycle(initialValue = null as Settings?)
            val s = settings ?: return@setContent
            ready = true
            KairosTheme(mode = s.themeMode, calmMotion = s.reducedMotion) {
                KairosNav(onboarded = s.onboarded, deepLink = deepLink) { deepLink = null }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLink = intent.getStringExtra(Notifier.EXTRA_ROUTE)
    }
}
