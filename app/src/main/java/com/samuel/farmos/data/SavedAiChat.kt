package com.samuel.farmos.data

data class SavedAiChat(
    val id: String = "",
    val title: String = ""
)

data class SavedAiMessage(
    val id: String = "",
    val isUser: Boolean = true,
    val text: String = "",
    val attachmentName: String? = null,
    val attachmentMimeType: String? = null,
    val timestampMillis: Long = System.currentTimeMillis()
)
