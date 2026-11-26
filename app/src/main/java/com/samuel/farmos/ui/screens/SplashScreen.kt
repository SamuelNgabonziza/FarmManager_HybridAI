package com.samuel.farmos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.samuel.farmos.ui.theme.FarmOsTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: (signedIn: Boolean) -> Unit) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        val signedIn = runCatching { FirebaseAuth.getInstance().currentUser != null }.getOrDefault(false)
        onFinished(signedIn)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0D5E4A)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D5E4A)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .background(Color(0xFF287864), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🐄", fontSize = 42.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "🐐", fontSize = 22.sp)
                            Text(text = "🐑", fontSize = 22.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "FarmOS",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Modern Farm Management",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFE2F0EA)
                )
            }
        }
    }
}

private const val SPLASH_DURATION_MS = 900L

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    FarmOsTheme { SplashScreen(onFinished = {}) }
}
