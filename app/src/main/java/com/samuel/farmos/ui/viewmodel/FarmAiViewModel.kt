package com.samuel.farmos.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.samuel.farmos.data.AiChatRepository
import com.samuel.farmos.data.FarmAiContextRepository
import com.samuel.farmos.data.FarmAiEmptyAnswerException
import com.samuel.farmos.data.FarmAiImage
import com.samuel.farmos.data.FarmAiService
import com.samuel.farmos.data.FarmRepository
import com.samuel.farmos.data.HybridFarmAdvisorService
import com.samuel.farmos.data.Recurrence
import com.samuel.farmos.data.SavedAiChat
import com.samuel.farmos.data.SavedAiMessage
import com.samuel.farmos.data.Task
import com.samuel.farmos.data.TaskCategory
import com.samuel.farmos.data.toUserMessage
import com.samuel.farmos.notifications.TaskReminderScheduler
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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

    private val aiService = HybridFarmAdvisorService()

    private val _executionMode = MutableStateFlow("HYBRID_READY")
    val executionMode: StateFlow<String> = _executionMode.asStateFlow()

    private val _statusDetail = MutableStateFlow("Hybrid AI ready (On-Device preferred, Cloud fallback)")
    val statusDetail: StateFlow<String> = _statusDetail.asStateFlow()

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

        requestJob?.cancel()

        requestJob = viewModelScope.launch {
            try {
                persistMessage(isUser = true, text = promptText, attachment = attachment)

                var contextFailed = false
                val farmContext = farmAiContextRepository.loadContextOrEmpty(uid, fid) {
                    contextFailed = true
                }

                val image = attachment?.takeIf { it.mimeType.startsWith("image/") }?.let { loadImage(it) }
                val documentText = attachment?.takeIf { it.mimeType == "text/plain" }?.let { loadTextDocument(it) }
                val promptForModel = if (documentText.isNullOrBlank()) promptText else
                    "$promptText\n\nAttached text document (${attachment?.displayName}):\n${documentText}"

                val replyId = nextMessageId++
                val reply = StringBuilder()
                _messages.value = _messages.value + AiChatMessage(
                    id = replyId,
                    isUser = false,
                    text = ""
                )

                aiService.streamAnswerWithStatus(
                    farmContext = farmContext,
                    question = promptForModel,
                    image = image
                ) { mode, detail ->
                    _executionMode.value = mode
                    _statusDetail.value = detail
                }.collect { chunk ->
                    reply.append(chunk)
                    _messages.value = _messages.value.map { message ->
                        if (message.id == replyId) {
                            message.copy(text = reply.toString())
                        } else {
                            message
                        }
                    }
                }

                val answer = reply.toString().trim()
                if (answer.isEmpty()) {
                    throw FarmAiEmptyAnswerException()
                }

                _aiResponse.value = answer
                _uiState.value = AiUiState.Success(answer)

                persistMessage(isUser = false, text = answer, attachment = null)

                if (contextFailed) {
                    _actionError.value = "Farm records could not be loaded, so the answer may not match your data."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("FarmAiViewModel", "AI Request failed", e)
                val message = e.toUserMessage()
                // Remove empty streaming placeholder bubble if no text arrived
                _messages.value = _messages.value.filterNot { !it.isUser && it.text.isBlank() }
                _executionMode.value = "HYBRID_READY"
                _statusDetail.value = "Hybrid AI ready (On-Device preferred, Cloud fallback)"
                _uiState.value = AiUiState.Error(message)
                _actionError.value = message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadImage(attachment: AiAttachment): FarmAiImage? {
        return withContext(Dispatchers.IO) {
            if (!attachment.mimeType.startsWith("image/")) return@withContext null
            try {
                val resolver = getApplication<Application>().contentResolver
                val bytes = resolver.openInputStream(attachment.uri)?.use { it.readBytes() }
                    ?: return@withContext null

                val mediaType = attachment.mimeType
                    ?.takeIf { it.startsWith("image/") }
                    ?: "image/jpeg"

                FarmAiImage(
                    mediaType = mediaType,
                    base64Data = Base64.encodeToString(bytes, Base64.NO_WRAP)
                )
            } catch (e: Exception) {
                Log.e("FarmAiViewModel", "Failed to read attachment", e)
                null
            }
        }
    }

    private suspend fun loadTextDocument(attachment: AiAttachment): String? = withContext(Dispatchers.IO) {
        if (attachment.mimeType != "text/plain") return@withContext null
        try {
            getApplication<Application>().contentResolver.openInputStream(attachment.uri)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText().take(12_000) }
        } catch (e: Exception) {
            Log.e("FarmAiViewModel", "Failed to read attached text file", e)
            null
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
                val taskId = farmRepository.addTask(uid, fid, newTask)
                if (newTask.dueDateMillis > System.currentTimeMillis()) {
                    TaskReminderScheduler.schedule(getApplication(), newTask.copy(id = taskId))
                }
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
