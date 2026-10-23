package com.samuel.farmos.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.SavedAiChat
import com.samuel.farmos.ui.components.FarmCameraCaptureDialog
import com.samuel.farmos.ui.components.FarmDeleteButton
import com.samuel.farmos.ui.components.FarmSnackbarProvider
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
            ModalDrawerSheet(
                drawerContainerColor = FarmUi.surface,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text("Your chats", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FarmUi.green)
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onStartNewChat()
                                scope.launch { drawerState.close() }
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = FarmUi.green.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = FarmUi.green)
                            Spacer(Modifier.width(8.dp))
                            Text("New chat", fontWeight = FontWeight.Bold, color = FarmUi.green)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = chatSearch,
                        onValueChange = { chatSearch = it },
                        placeholder = { Text("Search chats") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )
                    Spacer(Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savedChats.filter { chatSearch.isBlank() || it.title.contains(chatSearch, ignoreCase = true) }, key = { it.id }) { chat ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = FarmUi.background),
                                border = BorderStroke(1.dp, FarmUi.cardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = chat.title.ifBlank { "New chat" },
                                        color = FarmUi.ink,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                onOpenSavedChat(chat.id)
                                                scope.launch { drawerState.close() }
                                            }
                                    )
                                    FarmDeleteButton(
                                        description = "chat",
                                        onDelete = { onDeleteSavedChat(chat.id) }
                                    )
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
                            Text("Farm Assistant", fontWeight = FontWeight.Bold, color = FarmUi.green)
                            Text("FarmOS", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FarmUi.green)
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.History, contentDescription = "Chat history", tint = FarmUi.green)
                        }
                        IconButton(onClick = { onStartNewChat(); promptText = ""; attachment = null }) {
                            Icon(Icons.Default.Add, contentDescription = "New chat", tint = FarmUi.green)
                        }
                    },
                    colors = FarmUi.topAppBarColors()
                )
            },
            containerColor = FarmUi.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 36.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSystemInDarkTheme()) Color(0xFF284833) else Color(0xFFDFF2E4),
                                    modifier = Modifier.size(68.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = FarmUi.green,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "What can I help with?",
                                    color = FarmUi.ink,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Ask about livestock, crops, feed, or managing your farm.",
                                    color = FarmUi.textMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(20.dp))
                                Text(
                                    "Try asking",
                                    color = FarmUi.textMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = FarmUi.surface,
                                    border = BorderStroke(1.dp, FarmUi.cardBorder),
                                    modifier = Modifier.clickable { promptText = "Dairy cow feed calculation" }
                                ) {
                                    Text(
                                        "Dairy cow feed calculation",
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        color = FarmUi.green,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = FarmUi.surface,
                                    border = BorderStroke(1.dp, FarmUi.cardBorder),
                                    modifier = Modifier.clickable { promptText = "Diagnose symptoms" }
                                ) {
                                    Text(
                                        "Diagnose symptoms",
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        color = FarmUi.green,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.isUser
                        val userBg = if (isSystemInDarkTheme()) Color(0xFF284833) else Color(0xFFDFF2E4)
                        val assistantBg = FarmUi.surface

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.92f),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(containerColor = if (isUser) userBg else assistantBg),
                                border = if (!isUser) BorderStroke(1.dp, FarmUi.cardBorder) else null
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FarmUi.ink
                                    )
                                    msg.attachmentName?.let {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            "📷 $it",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FarmUi.green,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = FarmUi.surface,
                                border = BorderStroke(1.dp, FarmUi.cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = FarmUi.green
                                    )
                                    Text("Thinking...", style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
                                }
                            }
                        }
                    }
                }

                // Bottom Input
                Surface(
                    color = FarmUi.surface,
                    border = BorderStroke(1.dp, FarmUi.cardBorder),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        attachment?.let { att ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Attachment: ${att.displayName}",
                                    modifier = Modifier.weight(1f),
                                    color = FarmUi.green,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                TextButton(onClick = { attachment = null }) {
                                    Text("Remove", color = FarmUi.coral)
                                }
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { showAttachmentOptions = true }) {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = "Add attachment",
                                    tint = FarmUi.green
                                )
                            }
                            OutlinedTextField(
                                value = promptText,
                                onValueChange = { promptText = it },
                                placeholder = { Text("Write a message...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                colors = FarmUi.textFieldColors()
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (promptText.isNotBlank()) FarmUi.green else FarmUi.cardBorder,
                                modifier = Modifier.size(44.dp)
                            ) {
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
                                        Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = if (promptText.isNotBlank()) (if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White) else FarmUi.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAttachmentOptions) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentOptions = false },
            containerColor = FarmUi.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
                Text("Add to chat", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FarmUi.ink)
                Spacer(Modifier.height(16.dp))

                AttachmentOptionRow(
                    icon = Icons.Default.CameraAlt,
                    title = "Take a photo",
                    subtitle = "Capture a picture right now",
                    onClick = {
                        showAttachmentOptions = false
                        showCameraDialog = true
                    }
                )
                AttachmentOptionRow(
                    icon = Icons.Default.Image,
                    title = "Photos",
                    subtitle = "Choose an image from your photo library",
                    onClick = {
                        showAttachmentOptions = false
                        photoPicker.launch("image/*")
                    }
                )
                AttachmentOptionRow(
                    icon = Icons.Default.Description,
                    title = "Documents",
                    subtitle = "Choose a PDF or text document",
                    onClick = {
                        showAttachmentOptions = false
                        documentPicker.launch(arrayOf("text/plain", "application/pdf"))
                    }
                )
                AttachmentOptionRow(
                    icon = Icons.Default.Cloud,
                    title = "Browse device or Drive",
                    subtitle = "Browse phone storage or connected cloud drive",
                    onClick = {
                        showAttachmentOptions = false
                        documentPicker.launch(arrayOf("*/*"))
                    }
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun AttachmentOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        color = Color.Transparent
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = FarmUi.iconMint,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = FarmUi.green, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium, color = FarmUi.ink)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AiTabScreenPreview() {
    FarmOsTheme {
        FarmSnackbarProvider(hostState = remember { SnackbarHostState() }) {
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
}
