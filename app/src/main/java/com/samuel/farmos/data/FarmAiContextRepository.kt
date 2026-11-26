package com.samuel.farmos.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FarmAiContextRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    companion object {
        const val MAX_CONTEXT_CHARS = 60000
        const val MAX_RECORDS_PER_COLLECTION = 500
    }

    /**
     * Builds the structured farm intelligence summary handed to the assistant.
     */
    suspend fun loadContext(uid: String, farmId: String): String {
        val farmRef = db.collection("users").document(uid).collection("farms").document(farmId)
        val farmSnap = farmRef.get().await()
        val farmName = farmSnap.getString("farmName") ?: "Unknown Farm"

        val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        val sb = StringBuilder()
        sb.append("=== FARM REAL-TIME RECORDS & INTELLIGENCE ===\n")
        sb.append("Farm Name: ").append(farmName).append("\n")
        sb.append("Today's Date: ").append(todayStr).append("\n\n")

        // 1. Fetch Animals first so we can map ID -> Tag & Name
        val animalDocs = farmRef.collection("animals").limit(MAX_RECORDS_PER_COLLECTION.toLong()).get().await()
        val animalMap = mutableMapOf<String, String>()
        val animalsByStatus = mutableMapOf<String, Int>()
        val animalsBySpecies = mutableMapOf<String, Int>()

        val animalLines = mutableListOf<String>()
        for (doc in animalDocs.documents) {
            val data = doc.data ?: continue
            val tag = data["tagNumber"]?.toString()?.takeIf { it.isNotBlank() } ?: "No-Tag"
            val name = data["name"]?.toString()?.takeIf { it.isNotBlank() }
            val species = data["species"]?.toString() ?: "ANIMAL"
            val status = data["status"]?.toString() ?: "HEALTHY"

            val animalDisplay = if (name != null) "Tag #$tag ($name, $species)" else "Tag #$tag ($species)"
            animalMap[doc.id] = animalDisplay

            animalsByStatus[status] = (animalsByStatus[status] ?: 0) + 1
            animalsBySpecies[species] = (animalsBySpecies[species] ?: 0) + 1

            animalLines.add(formatMap(data, dateFormat, animalMap))
        }

        // 2. Executive Farm Overview
        sb.append("--- EXECUTIVE OVERVIEW ---\n")
        sb.append("Total Livestock: ").append(animalDocs.size())
        if (animalsBySpecies.isNotEmpty()) {
            sb.append(" (").append(animalsBySpecies.entries.joinToString { "${it.key}: ${it.value}" }).append(")")
        }
        sb.append("\n")
        if (animalsByStatus.isNotEmpty()) {
            sb.append("Herd Health Status: ").append(animalsByStatus.entries.joinToString { "${it.key}: ${it.value}" }).append("\n")
        }
        sb.append("\n")

        // 3. Animals Section
        sb.append("=== ANIMALS ===\n")
        animalLines.forEach { sb.append(it).append("\n") }
        sb.append("\n")

        // 4. Remaining Collections (Health, Tasks, Feeding, Breeding, Production, Finances)
        val otherCollections = listOf("healthCases", "tasks", "feedingRecords", "breedingRecords", "productionRecords", "financeRecords")
        for (colName in otherCollections) {
            sb.append("=== ").append(colName.uppercase()).append(" ===\n")
            val docs = farmRef.collection(colName).limit(MAX_RECORDS_PER_COLLECTION.toLong()).get().await()
            for (doc in docs.documents) {
                val data = doc.data ?: continue
                sb.append(formatMap(data, dateFormat, animalMap)).append("\n")
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

    private fun formatMap(
        data: Map<String, Any?>,
        dateFormat: SimpleDateFormat,
        animalMap: Map<String, String>
    ): String {
        return data.entries.joinToString(", ") { (k, v) ->
            val formattedValue = when {
                (k.endsWith("Millis") || k.endsWith("Date")) && v is Number && v.toLong() > 1_000_000_000L -> {
                    dateFormat.format(Date(v.toLong()))
                }
                (k == "linkedAnimalId" || k == "animalId") && v is String && animalMap.containsKey(v) -> {
                    animalMap[v] ?: v
                }
                else -> v.toString()
            }
            "$k=$formattedValue"
        }
    }
}
