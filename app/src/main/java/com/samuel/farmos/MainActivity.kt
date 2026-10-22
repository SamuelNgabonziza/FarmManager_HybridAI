package com.samuel.farmos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.samuel.farmos.notifications.NotificationHelper
import com.samuel.farmos.ui.navigation.AppNavigation
import com.samuel.farmos.ui.theme.FarmOsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Hand the system splash screen over to the app theme. This must run
        // before super.onCreate(), otherwise the launch theme is never applied.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Draw behind the system bars. The Compose theme supplies the matching
        // icon colours, so the app has no visible status/navigation bar gutter.
        enableEdgeToEdge()

        // Keep the splash on screen only while the first frame is still being
        // composed; the condition is re-evaluated before every draw, so it has
        // to stay cheap.
        var contentReady = false
        splashScreen.setKeepOnScreenCondition { !contentReady }

        NotificationHelper.createNotificationChannel(this)
        setContent {
            FarmOsTheme {
                AppNavigation(onContentReady = { contentReady = true })
            }
        }
    }
}
