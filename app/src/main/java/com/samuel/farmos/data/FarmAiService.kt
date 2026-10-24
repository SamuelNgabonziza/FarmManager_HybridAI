package com.samuel.farmos.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class FarmAiImage(
    val mediaType: String,
    val base64Data: String
)

class FarmAiNotConfiguredException(message: String) : Exception(message)
class FarmAiRefusalException(message: String) : Exception(message)
class FarmAiEmptyAnswerException :
    Exception("The assistant returned an empty answer. Please try rephrasing your question.")

interface FarmAiService {
    fun streamAnswer(
        farmContext: String,
        question: String,
        image: FarmAiImage?
    ): Flow<String>
}

/**
 * Google Gemini AI Service implementation for FarmOS.
 */
class GeminiFarmAiService(
    private val apiKey: String,
    private val model: String = "gemini-flash-latest"
) : FarmAiService {

    companion object {
        private const val TAG = "GeminiFarmAiService"
        private const val SYSTEM_PROMPT = """
            You are the intelligent AI Farm & Business Advisor inside FarmOS, an Android app used by farmers
            to oversee their entire farm operation.

            You have complete visibility into the farm's current records provided in the context, including:
            - Livestock (tag numbers, names, species, breeds, gender, statuses)
            - Health Cases (illnesses, symptoms, treatments, active vs. resolved cases)
            - Breeding (insemination, pregnancy checks, expected delivery dates)
            - Feeding (feed types, quantities, schedules, costs)
            - Production (milk, eggs, wool, harvest yields, dates)
            - Finances (income, expenses, cash flow, categories, profitability)
            - Tasks (scheduled farm work, overdue tasks, priority operations)

            Your Role & Advising Power:
            1. Comprehensive Oversight: Always examine the relationships across all records (e.g., how feeding affects milk yield, how veterinary costs impact profit margins, upcoming breeding due dates).
            2. Actionable Farm & Financial Advice: Provide concrete, practical agricultural and financial recommendations. Point out cost-saving opportunities, flag overdue animal health/vaccination needs, highlight high-producing livestock, and warn of financial imbalances.
            3. Grounded in Farm Data: Always reference real figures, animal tags, dates, and amounts from the context. If records lack certain details, give sound general farming/financial guidance and clarify what data to log next in FarmOS.
            4. Formatting & Communication Style (NO Raw Markdown):
               - Do NOT use raw markdown formatting symbols such as asterisks (do not use **bold** or *italic*), hashes (###), underscores (__), or backticks.
               - Write clean, natural, elegant text with clean paragraphs.
               - Use standard bullet characters (•) for lists instead of asterisks.
               - Write section headings in clean plain text or Capitalized Letters so the response looks natural and polished on mobile screens, exactly like the official Gemini app.
        """
    }

    override fun streamAnswer(
        farmContext: String,
        question: String,
        image: FarmAiImage?
    ): Flow<String> = flow {
        if (apiKey.isBlank()) {
            throw FarmAiNotConfiguredException(
                "Gemini API key is not configured. Please set your Gemini API key in Settings or local.properties."
            )
        }

        val urlString = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.setRequestProperty("Accept", "text/event-stream")
        conn.connectTimeout = 30000
        conn.readTimeout = 60000
        conn.doOutput = true
        conn.doInput = true

        val requestJson = JSONObject()

        // System prompt
        val systemInstruction = JSONObject()
        val systemParts = JSONArray()
        systemParts.put(JSONObject().put("text", SYSTEM_PROMPT))
        systemInstruction.put("parts", systemParts)
        requestJson.put("system_instruction", systemInstruction)

        // Contents
        val contents = JSONArray()
        val userTurn = JSONObject()
        userTurn.put("role", "user")
        val parts = JSONArray()

        val fullPrompt = if (farmContext.isNotBlank()) {
            "Here are the current farm records for context:\n$farmContext\n\nFarmer's Question:\n$question"
        } else {
            question
        }
        parts.put(JSONObject().put("text", fullPrompt))

        if (image != null) {
            val inlineData = JSONObject()
            inlineData.put("mime_type", image.mediaType)
            inlineData.put("data", image.base64Data)
            parts.put(JSONObject().put("inline_data", inlineData))
        }

        userTurn.put("parts", parts)
        contents.put(userTurn)
        requestJson.put("contents", contents)

        val os = conn.outputStream
        val writer = OutputStreamWriter(os, "UTF-8")
        writer.write(requestJson.toString())
        writer.flush()
        writer.close()
        os.close()

        val responseCode = conn.responseCode
        if (responseCode !in 200..299) {
            val errorStream = conn.errorStream
            val errorBody = errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            Log.e(TAG, "Gemini API error ($responseCode): $errorBody")
            val errorMsg = try {
                val errObj = JSONObject(errorBody).getJSONObject("error")
                errObj.optString("message", "Gemini API returned error $responseCode")
            } catch (e: Exception) {
                "Gemini API returned error $responseCode: $errorBody"
            }
            throw Exception(errorMsg)
        }

        val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
        var line: String? = reader.readLine()
        while (line != null) {
            if (line.startsWith("data: ")) {
                val dataContent = line.substring(6).trim()
                if (dataContent.isNotEmpty() && dataContent != "[DONE]") {
                    try {
                        val chunkObj = JSONObject(dataContent)
                        val candidates = chunkObj.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            if (content != null) {
                                val chunkParts = content.optJSONArray("parts")
                                if (chunkParts != null) {
                                    for (i in 0 until chunkParts.length()) {
                                        val part = chunkParts.getJSONObject(i)
                                        val text = part.optString("text", "")
                                        if (text.isNotEmpty()) {
                                            emit(text)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse SSE data: $dataContent", e)
                    }
                }
            }
            line = reader.readLine()
        }
        reader.close()
        conn.disconnect()
    }.flowOn(Dispatchers.IO)
}

fun Throwable.toUserMessage(): String {
    if (this is FarmAiNotConfiguredException) return message ?: "The AI assistant is not available."
    if (this is FarmAiRefusalException) return message ?: "The assistant declined to answer."
    if (this is FarmAiEmptyAnswerException) return message ?: "The assistant returned an empty answer."
    val msg = localizedMessage ?: message ?: "Something went wrong. Please try again."
    if (msg.contains("API key", ignoreCase = true)) {
        return "Please verify your Gemini API key in Settings."
    }
    return msg
}
