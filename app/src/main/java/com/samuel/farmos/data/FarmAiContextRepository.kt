package com.samuel.farmos.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FarmAiContextRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    companion object {
        const val MAX_CONTEXT_CHARS = 60000
        const val MAX_RECORDS_PER_COLLECTION = 500
    }

    /**
     * Builds the farm summary handed to the assistant.
     *
     * Throws when the farm's records cannot be read, so a failure is not
     * mistaken for a farm with no records. A caller that wants a
     * best-effort summary can use [loadContextOrEmpty].
     */
    suspend fun loadContext(uid: String, farmId: String): String {
        val farmRef = db.collection("users").document(uid).collection("farms").document(farmId)
        val farmSnap = farmRef.get().await()
        val farmName = farmSnap.getString("farmName") ?: "Unknown Farm"

        val sb = StringBuilder()
        sb.append("Farm Name: ").append(farmName).append("\n\n")

        val collections = listOf("animals", "tasks", "healthCases", "feeding", "breeding", "production", "finances")
        for (colName in collections) {
            sb.append("=== ").append(colName.uppercase()).append(" ===\n")
            val docs = farmRef.collection(colName).limit(MAX_RECORDS_PER_COLLECTION.toLong()).get().await()
            for (doc in docs.documents) {
                val data = doc.data ?: continue
                sb.append(formatMap(data)).append("\n")
            }
            sb.append("\n")
        }

        val result = sb.toString()
        val trimmed = if (result.length > MAX_CONTEXT_CHARS) {
            result.take(MAX_CONTEXT_CHARS)
        } else {
            result
        }
        if (trimmed.length != result.length) {
            Log.w("FarmAiContext", "Context truncated from ${result.length} to ${trimmed.length} chars")
        }
        return trimmed
    }

    /**
     * The farm summary, or an empty string when the records cannot be read.
     *
     * The question is still answered - just without the farm's records - and
     * the failure is reported through [onError] so the UI can say so.
     */
    suspend fun loadContextOrEmpty(
        uid: String,
        farmId: String,
        onError: (Throwable) -> Unit
    ): String {
        return try {
            loadContext(uid, farmId)
        } catch (e: Exception) {
            Log.e("FarmAiContext", "Failed to load farm context", e)
            onError(e)
            ""
        }
    }

    private fun formatMap(data: Map<String, Any?>): String {
        return data.entries.joinToString(", ") { (k, v) -> "${k}=${v}" }
    }
}
