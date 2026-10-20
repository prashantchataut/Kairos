package com.kairos.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kairos.app.data.prefs.Settings
import com.kairos.app.engine.notify.Notify
import com.kairos.app.ui.nav.KairosRoot
import com.kairos.app.ui.theme.KairosTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import androidx.lifecycle.lifecycleScope

class MainActivity : ComponentActivity() {
    private val settings = MutableStateFlow<Settings?>(null)
    private var pendingRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        splash.setKeepOnScreenCondition { settings.value == null }
        container.settings.settings.onEach { settings.value = it }.launchIn(lifecycleScope)
        pendingRoute = intent?.getStringExtra(Notify.EXTRA_ROUTE)

        setContent {
            val s by settings.filterNotNull().collectAsStateWithLifecycle(initialValue = null)
            val current = s ?: return@setContent
            KairosTheme(mode = current.theme, reduceMotion = current.reduceMotion) {
                KairosRoot(current, pendingRoute) { pendingRoute = null }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(Notify.EXTRA_ROUTE)?.let { pendingRoute = it }
    }
}
