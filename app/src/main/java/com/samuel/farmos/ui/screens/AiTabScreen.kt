package com.samuel.farmos.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samuel.farmos.ui.components.FarmCameraCaptureDialog
import com.samuel.farmos.ui.viewmodel.AiAttachment
import com.samuel.farmos.ui.viewmodel.AiChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTabScreen(
    messages: List<AiChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String, AiAttachment?) -> Unit,
    onBack: () -> Unit
) {
    var promptText by remember { mutableStateOf("") }
    var attachment by remember { mutableStateOf<AiAttachment?>(null) }
    var showCameraDialog by remember { mutableStateOf(false) }

    if (showCameraDialog) {
        FarmCameraCaptureDialog(
            onPhotoCaptured = { uri: Uri, fileName: String ->
                attachment = AiAttachment(uri = uri, displayName = fileName, mimeType = "image/jpeg")
                showCameraDialog = false
            },
            onDismiss = { showCameraDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Farm Assistant", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = FarmUi.topAppBarColors()
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = FarmUi.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "🤖", style = MaterialTheme.typography.displayMedium)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Ask me anything about your farm operations, animals, or tasks!",
                                        color = FarmUi.textMuted,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    items(messages) { msg ->
                        val isUser = msg.isUser
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) FarmUi.surfaceMint else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FarmUi.ink
                                    )
                                    if (msg.attachmentName != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "📷 ${msg.attachmentName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FarmUi.green
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = FarmUi.green)
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        attachment?.let { att ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Attachment: ${att.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FarmUi.green,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { attachment = null }) {
                                    Text("✕", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showCameraDialog = true }) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = "Camera", tint = FarmUi.green)
                            }

                            OutlinedTextField(
                                value = promptText,
                                onValueChange = { promptText = it },
                                placeholder = { Text("Ask Farm Assistant...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (promptText.isNotBlank() && !isLoading) {
                                        onSendMessage(promptText.trim(), attachment)
                                        promptText = ""
                                        attachment = null
                                    }
                                },
                                enabled = promptText.isNotBlank() && !isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = if (promptText.isNotBlank()) FarmUi.green else FarmUi.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
