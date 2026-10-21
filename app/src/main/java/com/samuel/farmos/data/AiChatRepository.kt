package com.samuel.farmos.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AiChatRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private fun chatsCollection(uid: String, farmId: String) =
        db.collection("users").document(uid).collection("farms").document(farmId).collection("aiChats")

    fun observeChats(uid: String, farmId: String): Flow<List<SavedAiChat>> = callbackFlow {
        val listener = chatsCollection(uid, farmId)
            .orderBy("title", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val chats = snapshot?.documents?.mapNotNull { doc ->
                    val title = doc.getString("title") ?: "Chat"
                    SavedAiChat(id = doc.id, title = title)
                } ?: emptyList()
                trySend(chats)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createChat(uid: String, farmId: String, firstPrompt: String): String {
        val docRef = chatsCollection(uid, farmId).document()
        val title = if (firstPrompt.length > 30) firstPrompt.take(30) + "..." else firstPrompt
        val data = mapOf(
            "title" to title,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(data).await()
        return docRef.id
    }

    suspend fun addMessage(
        uid: String,
        farmId: String,
        chatId: String,
        isUser: Boolean,
        text: String,
        attachmentName: String? = null,
        attachmentMimeType: String? = null
    ) {
        val messagesColl = chatsCollection(uid, farmId).document(chatId).collection("messages")
        val docRef = messagesColl.document()
        val msgData = mutableMapOf<String, Any>(
            "id" to docRef.id,
            "isUser" to isUser,
            "text" to text,
            "timestampMillis" to System.currentTimeMillis()
        )
        attachmentName?.let { msgData["attachmentName"] = it }
        attachmentMimeType?.let { msgData["attachmentMimeType"] = it }
        docRef.set(msgData).await()
    }

    suspend fun loadMessages(uid: String, farmId: String, chatId: String): List<SavedAiMessage> {
        val snapshot = chatsCollection(uid, farmId)
            .document(chatId)
            .collection("messages")
            .orderBy("timestampMillis", Query.Direction.ASCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            SavedAiMessage(
                id = doc.id,
                isUser = doc.getBoolean("isUser") ?: true,
                text = doc.getString("text") ?: "",
                attachmentName = doc.getString("attachmentName"),
                attachmentMimeType = doc.getString("attachmentMimeType"),
                timestampMillis = doc.getLong("timestampMillis") ?: System.currentTimeMillis()
            )
        }
    }

    suspend fun deleteChat(uid: String, farmId: String, chatId: String) {
        chatsCollection(uid, farmId).document(chatId).delete().await()
    }
}
