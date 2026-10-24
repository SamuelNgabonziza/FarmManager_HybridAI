package com.samuel.farmos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.samuel.farmos.notifications.NotificationHelper
import com.samuel.farmos.ui.navigation.AppNavigation
import com.samuel.farmos.ui.theme.FarmOsTheme

class MainActivity : ComponentActivity() {

    private var targetDestination by mutableStateOf<String?>(null)

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        var contentReady = false
        splashScreen.setKeepOnScreenCondition { !contentReady }

        NotificationHelper.createNotificationChannel(this)
        requestNotificationPermissionIfNeeded()

        targetDestination = intent?.getStringExtra("destination")

        setContent {
            FarmOsTheme {
                AppNavigation(
                    initialDestination = targetDestination,
                    onContentReady = { contentReady = true }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        targetDestination = intent.getStringExtra("destination")
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
