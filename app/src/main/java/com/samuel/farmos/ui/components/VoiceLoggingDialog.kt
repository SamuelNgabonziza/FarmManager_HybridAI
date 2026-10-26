package com.samuel.farmos.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.samuel.farmos.data.Animal
import com.samuel.farmos.ui.screens.FarmUi
import com.samuel.farmos.util.ParsedVoiceAction
import com.samuel.farmos.util.VoiceInputHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLoggingDialog(
    animals: List<Animal>,
    onDismiss: () -> Unit,
    onActionParsed: (ParsedVoiceAction) -> Unit
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (!granted) {
            errorMessage = "Microphone permission is required for voice logging."
        }
    }

    val voiceHelper = remember {
        VoiceInputHelper(
            context = context,
            onResult = { text ->
                recognizedText = text
                val parsed = VoiceInputHelper.parseSpeech(text, animals)
                onActionParsed(parsed)
            },
            onError = { err ->
                errorMessage = err
            },
            onListeningState = { listening ->
                isListening = listening
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.stopListening()
        }
    }

    val pulseAnim = remember { Animatable(1f) }
    LaunchedEffect(isListening) {
        if (isListening) {
            pulseAnim.animateTo(
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseAnim.snapTo(1f)
        }
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hands-Free Voice Log 🎙️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.green
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = FarmUi.textMuted)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Pulsing Mic Circle
                Box(contentAlignment = Alignment.Center) {
                    if (isListening) {
                        Surface(
                            shape = CircleShape,
                            color = FarmUi.green.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(96.dp)
                                .scale(pulseAnim.value)
                        ) {}
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (isListening) FarmUi.green else FarmUi.iconMint,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (isListening) (if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White) else FarmUi.green,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = when {
                        isListening -> "Listening... Speak now"
                        recognizedText.isNotBlank() -> "\"$recognizedText\""
                        errorMessage != null -> errorMessage ?: ""
                        else -> "Tap the button below and speak your log"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isListening) FontWeight.Bold else FontWeight.Normal,
                    color = if (errorMessage != null) FarmUi.coral else FarmUi.ink,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Examples:\n• \"14 liters milk for Bella\"\n• \"Spent 50000 on dairy meal\"\n• \"Cow #GT-37 is coughing\"\n• \"Task: Clean the water troughs tomorrow\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = FarmUi.textMuted,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FarmUi.cardBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        errorMessage = null
                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (isListening) {
                                voiceHelper.stopListening()
                            } else {
                                voiceHelper.startListening()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) FarmUi.coral else FarmUi.green,
                        contentColor = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White
                    )
                ) {
                    Text(
                        text = if (isListening) "Stop Listening" else "Start Speaking",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
