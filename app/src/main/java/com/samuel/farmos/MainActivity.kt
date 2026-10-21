package com.samuel.farmos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.EdgeToEdge
import androidx.activity.compose.setContent
import com.samuel.farmos.notifications.NotificationHelper
import com.samuel.farmos.ui.navigation.AppNavigation
import com.samuel.farmos.ui.theme.FarmOsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdge.enable(this)
        NotificationHelper.createNotificationChannel(this)
        setContent {
            FarmOsTheme {
                AppNavigation()
            }
        }
    }
}
