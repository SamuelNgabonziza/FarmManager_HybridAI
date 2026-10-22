package com.samuel.farmos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.samuel.farmos.ui.theme.FarmOsTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun SplashScreen(onFinished: (signedIn: Boolean) -> Unit) {
    val authResult = remember { CompletableDeferred<Boolean>() }

    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            authResult.complete(firebaseAuth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
            authResult.complete(false)
        }
    }

    LaunchedEffect(Unit) {
        val resolved = withTimeoutOrNull(SPLASH_TIMEOUT_MS) { authResult.await() }
        onFinished(resolved ?: (FirebaseAuth.getInstance().currentUser != null))
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF00664D)) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF005B45), Color(0xFF007C5A), Color(0xFF008463)))
            ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(
                    modifier = Modifier.size(118.dp).background(Color(0xFF2B907A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "\uD83C\uDF3E", fontSize = 58.sp)
                }
                Box(Modifier.height(48.dp))
                Text(
                    text = "FarmOS",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Box(Modifier.height(24.dp))
                Text(
                    text = "Modern Farm Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 28.sp,
                    color = Color(0xFFE5F1EA)
                )
            }
        }
    }
}

private const val SPLASH_TIMEOUT_MS = 3_000L

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    FarmOsTheme { SplashScreen(onFinished = {}) }
}
