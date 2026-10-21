package com.samuel.farmos.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.samuel.farmos.data.AiChatRepository
import com.samuel.farmos.data.FarmAiContextRepository
import com.samuel.farmos.data.FarmRepository
import com.samuel.farmos.data.Recurrence
import com.samuel.farmos.data.SavedAiChat
import com.samuel.farmos.data.SavedAiMessage
import com.samuel.farmos.data.Task
import com.samuel.farmos.data.TaskCategory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface AiUiState {
    object Idle : AiUiState
    object Loading : AiUiState
    data class Success(val responseText: String) : AiUiState
    data class Error(val errorMessage: String) : AiUiState
}

data class AiAttachment(
    val uri: Uri,
    val displayName: String,
    val mimeType: String
)

data class AiChatMessage(
    val id: Long,
    val isUser: Boolean,
    val text: String,
    val attachmentName: String? = null
)

data class FarmTaskDraft(
    val title: String,
    val category: TaskCategory,
    val dueDateMillis: Long
)

class FarmAiViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val farmRepository = FarmRepository(db)
    private val chatRepository = AiChatRepository(db)
    private val farmAiContextRepository = FarmAiContextRepository(db)

    private var userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private var farmId: String? = null
    private var currentChatId: String? = null

    private var chatsJob: Job? = null
    private var requestJob: Job? = null
    private var nextMessageId = 1L

    private val _messages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val messages: StateFlow<List<AiChatMessage>> = _messages.asStateFlow()

    private val _taskDraft = MutableStateFlow<FarmTaskDraft?>(null)
    val taskDraft: StateFlow<FarmTaskDraft?> = _taskDraft.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val _savedChats = MutableStateFlow<List<SavedAiChat>>(emptyList())
    val savedChats: StateFlow<List<SavedAiChat>> = _savedChats.asStateFlow()

    private val _historyError = MutableStateFlow<String?>(null)
    val historyError: StateFlow<String?> = _historyError.asStateFlow()

    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    fun setFarmContext(uid: String?, selectedFarmId: String?) {
        this.userId = uid
        this.farmId = selectedFarmId
        observeSavedChats()
    }

    private fun observeSavedChats() {
        val uid = userId ?: return
        val fid = farmId ?: return

        chatsJob?.cancel()
        chatsJob = viewModelScope.launch {
            chatRepository.observeChats(uid, fid)
                .catch { e ->
                    Log.e("FarmAiViewModel", "Failed to observe AI chats", e)
                    _historyError.value = "Failed to load chat history"
                }
                .collect { chats ->
                    _savedChats.value = chats
                }
        }
    }

    fun startNewChat() {
        currentChatId = null
        _messages.value = emptyList()
        _taskDraft.value = null
        _uiState.value = AiUiState.Idle
    }

    fun openSavedChat(chatId: String) {
        val uid = userId ?: return
        val fid = farmId ?: return

        currentChatId = chatId
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val savedMessages = chatRepository.loadMessages(uid, fid, chatId)
                _messages.value = savedMessages.mapIndexed { index, msg ->
                    AiChatMessage(
                        id = index.toLong() + 1,
                        isUser = msg.isUser,
                        text = msg.text,
                        attachmentName = msg.attachmentName
                    )
                }
                nextMessageId = savedMessages.size.toLong() + 1
            } catch (e: Exception) {
                Log.e("FarmAiViewModel", "Failed to load chat messages", e)
                _actionError.value = "Could not load saved chat"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSavedChat(chatId: String) {
        val uid = userId ?: return
        val fid = farmId ?: return

        viewModelScope.launch {
            try {
                chatRepository.deleteChat(uid, fid, chatId)
                if (currentChatId == chatId) {
                    startNewChat()
                }
            } catch (e: Exception) {
                Log.e("FarmAiViewModel", "Failed to delete chat", e)
                _actionError.value = "Could not delete chat"
            }
        }
    }

    fun askAi(promptText: String, attachment: AiAttachment? = null) {
        if (promptText.isBlank()) return
        val uid = userId ?: return
        val fid = farmId ?: return

        val userMessage = AiChatMessage(
            id = nextMessageId++,
            isUser = true,
            text = promptText,
            attachmentName = attachment?.displayName
        )
        _messages.value = _messages.value + userMessage
        _isLoading.value = true
        _uiState.value = AiUiState.Loading

        requestJob = viewModelScope.launch {
            try {
                persistMessage(isUser = true, text = promptText, attachment = attachment)

                val farmContext = farmAiContextRepository.loadContext(uid, fid)
                val fullPrompt = "FARM DATA CONTEXT:\n$farmContext\n\nUSER QUESTION:\n$promptText"

                val aiReply = "Thank you for asking about your farm. Based on your farm data: $promptText"
                _aiResponse.value = aiReply

                val aiMessage = AiChatMessage(
                    id = nextMessageId++,
                    isUser = false,
                    text = aiReply
                )
                _messages.value = _messages.value + aiMessage
                _uiState.value = AiUiState.Success(aiReply)

                persistMessage(isUser = false, text = aiReply, attachment = null)
            } catch (e: Exception) {
                Log.e("FarmAiViewModel", "AI Request failed", e)
                _uiState.value = AiUiState.Error("AI Error: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun persistMessage(isUser: Boolean, text: String, attachment: AiAttachment?) {
        val uid = userId ?: return
        val fid = farmId ?: return

        try {
            if (currentChatId == null) {
                currentChatId = chatRepository.createChat(uid, fid, text)
            }
            val chatId = currentChatId ?: return
            chatRepository.addMessage(
                uid = uid,
                farmId = fid,
                chatId = chatId,
                isUser = isUser,
                text = text,
                attachmentName = attachment?.displayName,
                attachmentMimeType = attachment?.mimeType
            )
        } catch (e: Exception) {
            Log.e("FarmAiViewModel", "Failed to persist chat message", e)
        }
    }

    fun discardTaskDraft() {
        _taskDraft.value = null
    }

    fun approveTaskDraft() {
        val draft = _taskDraft.value ?: return
        val uid = userId ?: return
        val fid = farmId ?: return

        viewModelScope.launch {
            try {
                val newTask = Task(
                    title = draft.title,
                    category = draft.category,
                    dueDateMillis = draft.dueDateMillis,
                    recurrence = Recurrence.NONE,
                    completed = false
                )
                farmRepository.addTask(uid, fid, newTask)
                _taskDraft.value = null
            } catch (e: Exception) {
                Log.e("FarmAiViewModel", "Failed to save draft task", e)
                _actionError.value = "Failed to save task: ${e.localizedMessage}"
            }
        }
    }

    fun resetState() {
        _actionError.value = null
        _uiState.value = AiUiState.Idle
    }
}
