package com.kairos.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.prefs.Settings
import com.kairos.app.ui.glass.LocalHazeState
import com.kairos.app.ui.navigation.KairosNavHost
import com.kairos.app.ui.navigation.LocalScene
import com.kairos.app.ui.navigation.SceneController
import com.kairos.app.ui.paint.WatercolorBackground
import com.kairos.app.ui.theme.KairosTheme
import com.kairos.app.work.Notifier
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

class MainActivity : ComponentActivity() {
    private var deepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLink = intent?.getStringExtra(Notifier.EXTRA_ROUTE)
        val prefs = (application as KairosApp).container.prefs
        var loaded = false
        splash.setKeepOnScreenCondition { !loaded }
        setContent {
            val settings by prefs.settings.collectAsStateWithLifecycle(initialValue = null as Settings?)
            val s = settings ?: return@setContent
            loaded = true
            KairosTheme(themeMode = s.themeMode, reducedMotion = s.reducedMotion) {
                val haze = remember { HazeState() }
                val scene = remember { SceneController() }
                Box(Modifier.fillMaxSize()) {
                    WatercolorBackground(scene.washes, Modifier.haze(haze))
                    CompositionLocalProvider(LocalHazeState provides haze, LocalScene provides scene) {
                        KairosNavHost(onboarded = s.onboarded, deepLink = deepLink, onDeepLinkHandled = { deepLink = null })
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLink = intent.getStringExtra(Notifier.EXTRA_ROUTE)
    }
}
