package com.samuel.farmos.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuel.farmos.data.SavedAiChat
import com.samuel.farmos.ui.components.FarmCameraCaptureDialog
import com.samuel.farmos.ui.components.FarmSnackbarProvider
import com.samuel.farmos.ui.components.LocalFarmSnackbar
import com.samuel.farmos.ui.theme.FarmOsTheme
import com.samuel.farmos.ui.viewmodel.AiAttachment
import com.samuel.farmos.ui.viewmodel.AiChatMessage

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun AiTabScreen(
    farmName: String? = null,
    messages: List<AiChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String, AiAttachment?) -> Unit,
    onBack: () -> Unit,
    savedChats: List<SavedAiChat> = emptyList(),
    onStartNewChat: () -> Unit = {},
    onOpenSavedChat: (String) -> Unit = {},
    onDeleteSavedChat: (String) -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()

    // Adaptive Theme Colors (Light sage vs Deep forest green)
    val screenBg = if (isDark) Color(0xFF18261F) else Color(0xFFF7FBF5)
    val topBarTitleColor = if (isDark) Color.White else Color(0xFF1E4D2B)
    val topBarSubtitleColor = if (isDark) Color(0xFFA0B4A8) else Color(0xFF637D6C)
    val topBarIconTint = if (isDark) Color(0xFF80E3A7) else Color(0xFF26733A)

    val squircleBg = if (isDark) Color(0xFF224A35) else Color(0xFFDFF4E4)
    val squircleIconTint = if (isDark) Color(0xFF70DC9E) else Color(0xFF26733A)
    val helpTitleColor = if (isDark) Color(0xFF70DC9E) else Color(0xFF1E4D2B)
    val helpSubtitleColor = if (isDark) Color(0xFFE2EFE7) else Color(0xFF4A6353)
    val tryAskingColor = if (isDark) Color.White else Color(0xFF1E4D2B)

    val chipBg = if (isDark) Color(0xFF134235) else Color(0xFFFFFFFF)
    val chipBorder = if (isDark) Color(0xFF1E634F) else Color(0xFFCBE5D3)
    val chipTextColor = if (isDark) Color(0xFF6DE0B2) else Color(0xFF1E6B38)

    val inputCapsuleBg = if (isDark) Color(0xFF16251E) else Color(0xFFFFFFFF)
    val inputCapsuleBorder = if (isDark) Color(0xFF23382C) else Color(0xFFDCE8DE)
    val inputFieldBg = if (isDark) Color(0xFF1B2C23) else Color(0xFFF7FBF5)
    val inputFieldBorder = if (isDark) Color(0xFF5E8B73) else Color(0xFF76B88D)
    val inputPlaceholderColor = if (isDark) Color(0xFF9FB2A7) else Color(0xFF88A090)
    val inputTextColor = if (isDark) Color.White else Color(0xFF14291B)
    val paperclipTint = if (isDark) Color(0xFF70DC9E) else Color(0xFF26733A)

    val sendButtonBg = if (isDark) Color(0xFF477559) else Color(0xFF26733A)
    val sendButtonActiveBg = if (isDark) Color(0xFF70DC9E) else Color(0xFF26733A)
    val sendIconTint = if (isDark) Color(0xFF1D452C) else Color.White
    val sendIconActiveTint = if (isDark) Color(0xFF123620) else Color.White

    val disclaimerColor = if (isDark) Color(0xFF8BA295) else Color(0xFF6B8273)

    val userBubbleBg = if (isDark) Color(0xFF70C493) else Color(0xFFD6F2DE)
    val userBubbleText = Color(0xFF0F361C)
    val assistantBubbleBg = if (isDark) Color(0xFF283D31) else Color(0xFFFFFFFF)
    val assistantBubbleBorder = if (isDark) Color(0x3380E3A7) else Color(0xFFDFECE2)
    val assistantBubbleText = if (isDark) Color(0xFFF0FDF4) else Color(0xFF16281D)

    val sheetBg = if (isDark) Color(0xFF1C2B22) else Color(0xFFFFFFFF)
    val sheetCardBg = if (isDark) Color(0xFF23352A) else Color(0xFFF3F8F4)
    val sheetCardBorder = if (isDark) Color(0xFF2B4032) else Color(0xFFE0EFE4)

    var promptText by remember { mutableStateOf("") }
    var attachment by remember { mutableStateOf<AiAttachment?>(null) }
    var showCameraDialog by remember { mutableStateOf(false) }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var chatSearch by remember { mutableStateOf("") }
    var chatToDelete by remember { mutableStateOf<SavedAiChat?>(null) }
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
            if (mimeType.startsWith("image/") || mimeType == "text/plain" || mimeType == "application/pdf") {
                attachment = AiAttachment(uri, uri.lastPathSegment ?: "Document", mimeType)
            } else {
                snackbar.show("This file type cannot be read yet. Choose a photo or a document.")
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

    // Confirmation dialog for deleting saved chat
    if (chatToDelete != null) {
        AlertDialog(
            onDismissRequest = { chatToDelete = null },
            title = { Text("Delete chat?", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1E4D2B)) },
            text = {
                Text(
                    "Are you sure you want to delete \"${chatToDelete?.title?.ifBlank { "New chat" }}\"? This cannot be undone.",
                    color = if (isDark) Color(0xFFA0B4A8) else Color(0xFF637D6C)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    chatToDelete?.let { onDeleteSavedChat(it.id) }
                    chatToDelete = null
                }) {
                    Text("Delete", color = FarmUi.coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { chatToDelete = null }) {
                    Text("Cancel", color = topBarIconTint)
                }
            },
            containerColor = sheetBg,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Dynamic farm subtitle per user account
    val displayFarm = farmName?.takeIf { it.isNotBlank() } ?: "FarmOS"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFE5A93C),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Farm Assistant",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = topBarTitleColor
                            )
                            Text(
                                displayFarm,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 13.sp,
                                color = topBarSubtitleColor
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = topBarIconTint)
                    }
                },
                actions = {
                    // History icon
                    IconButton(onClick = { showHistorySheet = true }) {
                        Icon(Icons.Default.History, contentDescription = "Chat history", tint = topBarIconTint)
                    }
                    // "+ New Chat" only shown when messages exist
                    if (messages.isNotEmpty()) {
                        IconButton(onClick = {
                            onStartNewChat()
                            promptText = ""
                            attachment = null
                        }) {
                            Icon(Icons.Default.AddComment, contentDescription = "New chat", tint = topBarIconTint)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBg,
                    titleContentColor = topBarTitleColor
                )
            )
        },
        containerColor = screenBg
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
                                .padding(top = 36.dp, bottom = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(26.dp),
                                color = squircleBg,
                                modifier = Modifier.size(76.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = squircleIconTint,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "What can I help with?",
                                color = helpTitleColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                            Spacer(Modifier.height(14.dp))
                            Text(
                                text = "Ask about livestock, crops, feed, or\nmanaging your farm.",
                                color = helpSubtitleColor,
                                fontSize = 17.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                            Spacer(Modifier.height(36.dp))

                            // "Try asking" aligned left
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Try asking",
                                    color = tryAskingColor,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    val suggestions = listOf(
                                        "Dairy cow feed calculation",
                                        "Diagnose treatment",
                                        "Crop pest control advice",
                                        "Breeding schedule tips",
                                        "Farm finance management"
                                    )
                                    items(suggestions) { suggestion ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = chipBg,
                                            border = BorderStroke(1.dp, chipBorder),
                                            modifier = Modifier.clickable { promptText = suggestion }
                                        ) {
                                            Text(
                                                suggestion,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                                color = chipTextColor,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    val isUser = msg.isUser
                    val currentBubbleBg = if (isUser) userBubbleBg else assistantBubbleBg
                    val currentBubbleText = if (isUser) userBubbleText else assistantBubbleText

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.92f),
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (isUser) 18.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 18.dp
                            ),
                            color = currentBubbleBg,
                            border = if (!isUser) BorderStroke(1.dp, assistantBubbleBorder) else null
                        ) {
                            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                val displayText = if (!isUser) {
                                    msg.text
                                        .replace(Regex("""\*\*(.*?)\*\*"""), "$1")
                                        .replace(Regex("""\*(.*?)\*"""), "$1")
                                        .replace(Regex("""(?m)^#{1,6}\s*"""), "")
                                        .replace(Regex("""(?m)^\*\s+"""), "• ")
                                } else {
                                    msg.text
                                }
                                Text(
                                    text = displayText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 15.sp,
                                    color = currentBubbleText
                                )
                                msg.attachmentName?.let {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "📷 $it",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isUser) userBubbleText else topBarIconTint,
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
                            shape = RoundedCornerShape(18.dp),
                            color = if (isDark) Color(0xFF2F4538) else Color(0xFFE8F5EC),
                            border = BorderStroke(1.dp, assistantBubbleBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = topBarIconTint
                                )
                                Text(
                                    "Thinking...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) Color(0xFFB8D5C4) else Color(0xFF2B5B3B)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Input Area (Capsule + Disclaimer)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                attachment?.let { att ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Attachment: ${att.displayName}",
                            modifier = Modifier.weight(1f),
                            color = topBarIconTint,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = { attachment = null }) {
                            Text("Remove", color = FarmUi.coral)
                        }
                    }
                }

                // Outer rounded capsule
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = inputCapsuleBg,
                    border = BorderStroke(1.dp, inputCapsuleBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        // Paperclip icon
                        IconButton(onClick = { showAttachmentOptions = true }) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = "Add attachment",
                                tint = paperclipTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Pill input field with border
                        OutlinedTextField(
                            value = promptText,
                            onValueChange = { promptText = it },
                            placeholder = { Text("Write a message...", color = inputPlaceholderColor, fontSize = 15.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(25.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = inputFieldBorder,
                                unfocusedBorderColor = inputFieldBorder,
                                focusedContainerColor = inputFieldBg,
                                unfocusedContainerColor = inputFieldBg,
                                cursorColor = topBarIconTint,
                                focusedTextColor = inputTextColor,
                                unfocusedTextColor = inputTextColor
                            )
                        )

                        Spacer(Modifier.width(8.dp))

                        // Circular send button
                        val isEnabled = promptText.isNotBlank() && !isLoading
                        Surface(
                            shape = CircleShape,
                            color = if (isEnabled) sendButtonActiveBg else sendButtonBg,
                            modifier = Modifier.size(46.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (isEnabled) {
                                        onSendMessage(promptText.trim(), attachment)
                                        promptText = ""
                                        attachment = null
                                    }
                                },
                                enabled = isEnabled
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (isEnabled) sendIconActiveTint else sendIconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Left-indented 2-line disclaimer
                Text(
                    text = "Farm Assistant can make mistakes. Check\nimportant farm decisions.",
                    color = disclaimerColor,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, bottom = 4.dp)
                )
            }
        }
    }

    // "Your chats" Bottom Sheet
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = sheetBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Your chats",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = topBarTitleColor
                        )
                        Text(
                            displayFarm,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 14.sp,
                            color = topBarIconTint
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0xFF263D2F) else Color(0xFFE5F5E9),
                        modifier = Modifier.clickable {
                            onStartNewChat()
                            showHistorySheet = false
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = topBarIconTint, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "New chat",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = topBarIconTint
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                OutlinedTextField(
                    value = chatSearch,
                    onValueChange = { chatSearch = it },
                    placeholder = { Text("Search chats", color = inputPlaceholderColor) },
                    leadingIcon = {
                        Icon(Icons.Default.History, contentDescription = null, tint = topBarIconTint, modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = inputCapsuleBorder,
                        unfocusedBorderColor = inputCapsuleBorder,
                        focusedContainerColor = inputFieldBg,
                        unfocusedContainerColor = inputFieldBg,
                        cursorColor = topBarIconTint,
                        focusedTextColor = inputTextColor,
                        unfocusedTextColor = inputTextColor
                    )
                )

                Spacer(Modifier.height(10.dp))
                Text(
                    "Hold a chat for delete options",
                    color = disclaimerColor,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filteredChats = savedChats.filter {
                        chatSearch.isBlank() || it.title.contains(chatSearch, ignoreCase = true)
                    }

                    if (filteredChats.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (chatSearch.isNotBlank()) "No matching chats found." else "No saved chats yet.",
                                    color = disclaimerColor,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        items(filteredChats, key = { it.id }) { chat ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = sheetCardBg),
                                border = BorderStroke(1.dp, sheetCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            onOpenSavedChat(chat.id)
                                            showHistorySheet = false
                                        },
                                        onLongClick = {
                                            chatToDelete = chat
                                        }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = topBarIconTint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = chat.title.ifBlank { "New chat" },
                                            color = inputTextColor,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "Saved to $displayFarm",
                                            color = disclaimerColor,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // "Add to chat" Bottom Sheet (Attachments)
    if (showAttachmentOptions) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentOptions = false },
            containerColor = sheetBg,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    "Add to chat",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = topBarTitleColor
                )
                Spacer(Modifier.height(16.dp))

                AttachmentOptionRow(
                    icon = Icons.Default.PhotoCamera,
                    iconTint = topBarIconTint,
                    title = "Take a photo",
                    subtitle = "Use your camera; the photo is saved in Pictures/FarmOS",
                    textColor = inputTextColor,
                    subtitleColor = disclaimerColor,
                    onClick = {
                        showAttachmentOptions = false
                        showCameraDialog = true
                    }
                )

                AttachmentOptionRow(
                    icon = Icons.Default.Image,
                    iconTint = topBarIconTint,
                    title = "Photos",
                    subtitle = "Choose an image from your photo library",
                    textColor = inputTextColor,
                    subtitleColor = disclaimerColor,
                    onClick = {
                        showAttachmentOptions = false
                        photoPicker.launch("image/*")
                    }
                )

                AttachmentOptionRow(
                    icon = Icons.Default.Description,
                    iconTint = topBarIconTint,
                    title = "Documents",
                    subtitle = "Choose a PDF or text document",
                    textColor = inputTextColor,
                    subtitleColor = disclaimerColor,
                    onClick = {
                        showAttachmentOptions = false
                        documentPicker.launch(arrayOf("text/plain", "application/pdf"))
                    }
                )

                AttachmentOptionRow(
                    icon = Icons.Default.Folder,
                    iconTint = Color(0xFFE5A93C),
                    trailingIcon = Icons.Default.Cloud,
                    trailingIconTint = if (isDark) Color(0xFFD0DFD6) else Color(0xFF7A9485),
                    title = "Browse device or Drive",
                    subtitle = "Browse phone storage or a connected cloud drive",
                    textColor = inputTextColor,
                    subtitleColor = disclaimerColor,
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
    iconTint: Color,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    trailingIconTint: Color = Color.White,
    title: String,
    subtitle: String,
    textColor: Color,
    subtitleColor: Color,
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
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = textColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    fontSize = 13.sp,
                    color = subtitleColor
                )
            }
            if (trailingIcon != null) {
                Icon(
                    trailingIcon,
                    contentDescription = null,
                    tint = trailingIconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Preview(name = "Light Mode Preview", showBackground = true)
@Composable
fun AiTabScreenLightPreview() {
    FarmOsTheme(darkTheme = false) {
        FarmSnackbarProvider(hostState = remember { SnackbarHostState() }) {
            AiTabScreen(
                farmName = "sams",
                messages = emptyList(),
                isLoading = false,
                onSendMessage = { _, _ -> },
                onBack = {}
            )
        }
    }
}

@Preview(name = "Dark Mode Preview", showBackground = true)
@Composable
fun AiTabScreenDarkPreview() {
    FarmOsTheme(darkTheme = true) {
        FarmSnackbarProvider(hostState = remember { SnackbarHostState() }) {
            AiTabScreen(
                farmName = "SHIELD",
                messages = emptyList(),
                isLoading = false,
                onSendMessage = { _, _ -> },
                onBack = {}
            )
        }
    }
}
