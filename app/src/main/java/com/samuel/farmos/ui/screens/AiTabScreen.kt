package com.samuel.farmos.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.samuel.farmos.data.SavedAiChat
import com.samuel.farmos.ui.components.FarmCameraCaptureDialog
import com.samuel.farmos.ui.components.LocalFarmSnackbar
import com.samuel.farmos.ui.theme.FarmOsTheme
import com.samuel.farmos.ui.viewmodel.AiAttachment
import com.samuel.farmos.ui.viewmodel.AiChatMessage
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AiTabScreen(
    messages: List<AiChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String, AiAttachment?) -> Unit,
    onBack: () -> Unit,
    savedChats: List<SavedAiChat> = emptyList(),
    onStartNewChat: () -> Unit = {},
    onOpenSavedChat: (String) -> Unit = {},
    onDeleteSavedChat: (String) -> Unit = {}
) {
    var promptText by remember { mutableStateOf("") }
    var attachment by remember { mutableStateOf<AiAttachment?>(null) }
    var showCameraDialog by remember { mutableStateOf(false) }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var chatSearch by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbar = LocalFarmSnackbar.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            attachment = AiAttachment(uri, uri.lastPathSegment ?: "Photo", context.contentResolver.getType(uri) ?: "image/jpeg")
        }
    }
    val documentPicker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) {
            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            if (mimeType.startsWith("image/") || mimeType == "text/plain") {
                attachment = AiAttachment(uri, uri.lastPathSegment ?: "Document", mimeType)
            } else {
                snackbar.show("This file type cannot be read yet. Choose a photo or a plain text document.")
            }
        }
    }

    if (showCameraDialog) {
        FarmCameraCaptureDialog(
            onPhotoCaptured = { uri: Uri, fileName: String ->
                attachment = AiAttachment(uri = uri, displayName = fileName, mimeType = "image/jpeg")
                showCameraDialog = false
            },
            onDismiss = { showCameraDialog = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = FarmUi.surface) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Your chats", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = FarmUi.green)
                    TextButton(onClick = { onStartNewChat(); scope.launch { drawerState.close() } }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = FarmUi.green)
                        Text("New Chat", modifier = Modifier.padding(start = 8.dp))
                    }
                    OutlinedTextField(
                        value = chatSearch,
                        onValueChange = { chatSearch = it },
                        placeholder = { Text("Search chats") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )
                    LazyColumn(Modifier.weight(1f).padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(savedChats.filter { chatSearch.isBlank() || it.title.contains(chatSearch, ignoreCase = true) }, key = { it.id }) { chat ->
                            Card(shape = FarmUi.cardShape, colors = CardDefaults.cardColors(containerColor = FarmUi.surfaceMint)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(
                                        onClick = { onOpenSavedChat(chat.id); scope.launch { drawerState.close() } },
                                        modifier = Modifier.weight(1f)
                                    ) { Text(chat.title.ifBlank { "New chat" }, color = FarmUi.ink, maxLines = 2) }
                                    TextButton(onClick = { onDeleteSavedChat(chat.id) }) { Text("⋮", color = FarmUi.textMuted) }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("✨ Farm Assistant", fontWeight = FontWeight.Bold)
                            Text("FarmOS", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                        }
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.History, contentDescription = "Chat history", tint = FarmUi.green) }
                        IconButton(onClick = { onStartNewChat(); promptText = ""; attachment = null }) { Icon(Icons.Default.Add, contentDescription = "New chat", tint = FarmUi.green) }
                    },
                    colors = FarmUi.topAppBarColors()
                )
            },
            containerColor = FarmUi.background
        ) { innerPadding ->
            Column(Modifier.fillMaxSize().padding(innerPadding)) {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Column(Modifier.fillMaxWidth().padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✦", style = MaterialTheme.typography.displayMedium, color = FarmUi.green)
                                Spacer(Modifier.height(8.dp))
                                Text("What can I help with?", color = FarmUi.green, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                Text("Ask about livestock, crops, feed, or managing your farm.", color = FarmUi.textMuted, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(14.dp))
                                Text("Try asking", color = FarmUi.textMuted, style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(onClick = { promptText = "Dairy cow feed calculation" }) { Text("Dairy cow feed calculation") }
                                }
                                TextButton(onClick = { promptText = "Diagnose symptoms" }) { Text("Diagnose symptoms") }
                            }
                        }
                    }
                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.isUser
                        Box(Modifier.fillMaxWidth(), contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart) {
                            Card(
                                modifier = Modifier.fillMaxWidth(if (isUser) 0.78f else 0.9f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isUser) FarmUi.surfaceMint else FarmUi.surface)
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(msg.text, style = MaterialTheme.typography.bodyMedium, color = FarmUi.ink)
                                    msg.attachmentName?.let { Text("📷 $it", style = MaterialTheme.typography.labelSmall, color = FarmUi.green) }
                                }
                            }
                        }
                    }
                    if (isLoading) item { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = FarmUi.green) } }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                    colors = CardDefaults.cardColors(containerColor = FarmUi.surface)
                ) {
                    Column(Modifier.padding(10.dp)) {
                        attachment?.let { att ->
                            Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Attachment: ${att.displayName}", modifier = Modifier.weight(1f), color = FarmUi.green, style = MaterialTheme.typography.bodySmall)
                                TextButton(onClick = { attachment = null }) { Text("Remove") }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showAttachmentOptions = true }) { Icon(Icons.Default.AddAPhoto, contentDescription = "Add to chat", tint = FarmUi.green) }
                            OutlinedTextField(
                                value = promptText,
                                onValueChange = { promptText = it },
                                placeholder = { Text("Write a message...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = FarmUi.pillShape,
                                colors = FarmUi.textFieldColors()
                            )
                            Spacer(Modifier.width(6.dp))
                            IconButton(onClick = {
                                if (promptText.isNotBlank() && !isLoading) {
                                    onSendMessage(promptText.trim(), attachment)
                                    promptText = ""
                                    attachment = null
                                }
                            }, enabled = promptText.isNotBlank() && !isLoading) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = if (promptText.isNotBlank()) FarmUi.green else FarmUi.textMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAttachmentOptions) {
        androidx.compose.material3.ModalBottomSheet(onDismissRequest = { showAttachmentOptions = false }, containerColor = FarmUi.surface) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text("Add to chat", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = { showAttachmentOptions = false; showCameraDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = FarmUi.green)
                    Text("Take a photo", modifier = Modifier.padding(start = 12.dp))
                }
                TextButton(onClick = { showAttachmentOptions = false; photoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Photos\nChoose an image from your photo library")
                }
                TextButton(onClick = { showAttachmentOptions = false; documentPicker.launch(arrayOf("text/plain", "application/pdf")) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Documents\nChoose a PDF or text document")
                }
                TextButton(onClick = { showAttachmentOptions = false; documentPicker.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Browse device or Drive\nBrowse phone storage or connected cloud drive")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AiTabScreenPreview() {
    FarmOsTheme {
        AiTabScreen(
            messages = listOf(
                AiChatMessage(id = 1L, isUser = false, text = "Hello! How can I help you on your farm today?"),
                AiChatMessage(id = 2L, isUser = true, text = "How much feed should I give to my dairy cows?")
            ),
            isLoading = false,
            onSendMessage = { _, _ -> },
            onBack = {}
        )
    }
}
