package com.samuel.farmos.data

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.InferenceSource
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.APINotConfiguredException
import com.google.firebase.ai.type.ContentBlockedException
import com.google.firebase.ai.type.FirebaseAIOnDeviceNotAvailableException
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.RequestTimeoutException
import com.google.firebase.ai.type.ServerException
import com.google.firebase.ai.type.ServiceConnectionHandshakeFailedException
import com.google.firebase.ai.type.ServiceDisabledException
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Result from [HybridFarmAdvisorService] containing both the generated advice text
 * and metadata describing which inference mode/status ran.
 */
data class FarmAdviceResult(
    val adviceText: String,
    val executionMode: String,
    val statusDetail: String
)

/**
 * FarmOS AI Advisor powered by Firebase AI Logic hybrid inference
 * ([InferenceMode.PREFER_ON_DEVICE] with a cloud `gemini-2.5-flash` fallback).
 */
class HybridFarmAdvisorService(
    private val cloudModelName: String = "gemini-3.5-flash-lite",
    private val secondaryCloudModelName: String = "gemini-3.8-flash"
) : FarmAiService {

    companion object {
        private const val TAG = "HybridFarmAdvisor"
        private const val SYSTEM_PROMPT = """
            You are FarmOS Hybrid AI, an expert livestock veterinarian, dairy nutritionist, and farm business advisor.
            This farm is focused on LIVESTOCK & ANIMALS (cattle, goats, sheep, poultry, pigs) — do NOT give crop-farming advice unless asked.

            You have visibility into the farm's real-time records in the context (Livestock, Health Cases, Breeding, Feeding, Production, Finances, Tasks).

            How to Answer (Google Gemini Research Style):
            1. Start with a concise 1-2 sentence direct answer / takeaway.
            2. Organize the rest with clear CAPITALIZED SECTION HEADINGS and concise bullet points (•) containing real veterinary/agricultural benchmarks (quantities, litres, ratios, THI thresholds, withdrawal/quarantine steps, UGX/cost efficiency) and live web research when online.
            3. When an animal or feed photo is attached, describe visible signs/body condition and give immediate practical next steps.
            4. Ground your advice in the farm's actual records when available (never ask for or expose passwords/PINs); if records are empty, give practical researched livestock guidance and state what to log next.
            5. Formatting Rules (NO Raw Markdown):
               - Do NOT use asterisks (**bold** or *italic*), hashes (###), underscores (__), or backticks.
               - Use clean paragraphs, CAPITALIZED headings, and • bullet points so it reads cleanly like Google Gemini.
        """
    }

    private val aiClient by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
    }

    @OptIn(PublicPreviewAPI::class)
    private val hybridModel by lazy {
        aiClient.generativeModel(
            modelName = cloudModelName,
            systemInstruction = content { text(SYSTEM_PROMPT) },
            onDeviceConfig = OnDeviceConfig(mode = InferenceMode.PREFER_ON_DEVICE)
        )
    }

    private val cloudFallbackModel by lazy {
        aiClient.generativeModel(
            modelName = cloudModelName,
            systemInstruction = content { text(SYSTEM_PROMPT) },
            tools = listOf(Tool.googleSearch())
        )
    }

    private val secondaryCloudFallbackModel by lazy {
        aiClient.generativeModel(
            modelName = secondaryCloudModelName,
            systemInstruction = content { text(SYSTEM_PROMPT) },
            tools = listOf(Tool.googleSearch())
        )
    }

    @Volatile
    private var onDeviceUnavailableForSession: Boolean = false

    /**
     * Generates farm advice using hybrid inference (preferring on-device, falling back to cloud Gemini)
     * and returns both the advice text and the actual inference source/status.
     */
    @OptIn(PublicPreviewAPI::class)
    suspend fun getAdvice(
        farmContext: String,
        question: String,
        image: FarmAiImage? = null
    ): FarmAdviceResult = withContext(Dispatchers.IO) {
        val inputContent = buildInputContent(farmContext, question, image)
        var hybridFailure: Throwable? = null

        // 1. Try hybrid model (PREFER_ON_DEVICE). Firebase AI Logic reports whether
        //    the response actually came from InferenceSource.ON_DEVICE or IN_CLOUD.
        try {
            val response = hybridModel.generateContent(inputContent)
            val text = response.text?.trim().orEmpty()
            if (text.isNotEmpty()) {
                val (mode, detail) = mapInferenceSource(response.inferenceSource, isFallback = false)
                return@withContext FarmAdviceResult(
                    adviceText = text,
                    executionMode = mode,
                    statusDetail = detail
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            hybridFailure = e
            Log.w(TAG, "Hybrid PREFER_ON_DEVICE failed, falling back to cloud Gemini ($cloudModelName)", e)
        }

        // 2. Cloud Gemini model fallback
        try {
            val fallbackResponse = cloudFallbackModel.generateContent(inputContent)
            val fallbackText = fallbackResponse.text?.trim().orEmpty()
            if (fallbackText.isEmpty()) {
                throw FarmAiEmptyAnswerException()
            }
            val (_, detail) = mapInferenceSource(fallbackResponse.inferenceSource, isFallback = true)
            FarmAdviceResult(
                adviceText = fallbackText,
                executionMode = "CLOUD_FALLBACK",
                statusDetail = detail
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: FarmAiEmptyAnswerException) {
            throw e
        } catch (e: Exception) {
            throw Exception(mapUserErrorMessage(e, hybridFailure), e)
        }
    }

    override fun streamAnswer(
        farmContext: String,
        question: String,
        image: FarmAiImage?
    ): Flow<String> = streamAnswerWithStatus(farmContext, question, image) { _, _ -> }

    /**
     * Streams farm advice while reporting real-time [executionMode] (`ON_DEVICE`, `IN_CLOUD`,
     * or `CLOUD_FALLBACK`) and [statusDetail] transitions to the caller without crashing.
     */
    @OptIn(PublicPreviewAPI::class)
    fun streamAnswerWithStatus(
        farmContext: String,
        question: String,
        image: FarmAiImage?,
        onStatus: (executionMode: String, statusDetail: String) -> Unit
    ): Flow<String> = flow {
        val inputContent = buildInputContent(farmContext, question, image)
        var emittedAny = false
        var hybridFailure: Throwable? = null

        onStatus(
            if (onDeviceUnavailableForSession) "CLOUD_FALLBACK" else "ON_DEVICE",
            if (onDeviceUnavailableForSession) {
                "On-device AI unavailable on this device — using Cloud Gemini ($cloudModelName)…"
            } else {
                "Checking On-Device AI (PREFER_ON_DEVICE)…"
            }
        )

        if (!onDeviceUnavailableForSession) {
            try {
                withTimeout(8_000L) {
                    hybridModel.generateContentStream(inputContent).collect { chunk ->
                        val text = chunk.text
                        if (!text.isNullOrEmpty()) {
                            if (!emittedAny) {
                                val (mode, detail) = mapInferenceSource(chunk.inferenceSource, isFallback = false)
                                onStatus(mode, detail)
                            }
                            emittedAny = true
                            emit(text)
                        }
                    }
                }
                if (emittedAny) return@flow
            } catch (e: CancellationException) {
                if (e !is kotlinx.coroutines.TimeoutCancellationException) throw e
                hybridFailure = e
                onDeviceUnavailableForSession = true
                Log.w(TAG, "Hybrid stream timed out waiting for on-device model, switching to cloud Gemini", e)
                if (emittedAny) return@flow
                onStatus(
                    "CLOUD_FALLBACK",
                    "On-device unavailable — switching to Cloud Gemini ($cloudModelName)…"
                )
            } catch (e: Exception) {
                hybridFailure = e
                if (e is FirebaseAIOnDeviceNotAvailableException) {
                    onDeviceUnavailableForSession = true
                }
                Log.w(TAG, "Hybrid stream failed, falling back to cloud Gemini stream", e)
                if (emittedAny) {
                    throw Exception(mapUserErrorMessage(e, null), e)
                }
                val reason = when (e) {
                    is FirebaseAIOnDeviceNotAvailableException -> "On-device model unavailable"
                    else -> "On-device inference unavailable"
                }
                onStatus(
                    "CLOUD_FALLBACK",
                    "$reason — switching to Cloud Gemini ($cloudModelName)…"
                )
            }
        }

        if (!emittedAny) {
            onStatus(
                "CLOUD_FALLBACK",
                "Streaming via Cloud Gemini fallback ($cloudModelName)"
            )
        }

        try {
            var emittedFallback = false
            cloudFallbackModel.generateContentStream(inputContent).collect { chunk ->
                val text = chunk.text
                if (!text.isNullOrEmpty()) {
                    if (!emittedFallback) {
                        val (_, detail) = mapInferenceSource(chunk.inferenceSource, isFallback = true)
                        onStatus("CLOUD_FALLBACK", detail)
                        emittedFallback = true
                    }
                    emit(text)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Primary cloud fallback ($cloudModelName) failed, trying $secondaryCloudModelName", e)
            try {
                var emittedSecondary = false
                secondaryCloudFallbackModel.generateContentStream(inputContent).collect { chunk ->
                    val text = chunk.text
                    if (!text.isNullOrEmpty()) {
                        if (!emittedSecondary) {
                            onStatus("CLOUD_FALLBACK", "Running via Cloud Gemini fallback ($secondaryCloudModelName)")
                            emittedSecondary = true
                        }
                        emit(text)
                    }
                }
            } catch (secondaryError: CancellationException) {
                throw secondaryError
            } catch (secondaryError: Exception) {
                throw Exception(mapUserErrorMessage(secondaryError, hybridFailure), secondaryError)
            }
        }
    }.flowOn(Dispatchers.IO)

    @OptIn(PublicPreviewAPI::class)
    private fun mapInferenceSource(
        source: InferenceSource,
        isFallback: Boolean
    ): Pair<String, String> = when (source) {
        InferenceSource.ON_DEVICE ->
            "ON_DEVICE" to "Running On-Device via Firebase AI Logic (PREFER_ON_DEVICE)"
        InferenceSource.IN_CLOUD ->
            if (isFallback) {
                "CLOUD_FALLBACK" to "Running via Cloud Gemini fallback ($cloudModelName)"
            } else {
                "IN_CLOUD" to "Running In-Cloud via Firebase AI Logic ($cloudModelName)"
            }
        else ->
            if (isFallback) {
                "CLOUD_FALLBACK" to "Running via Cloud Gemini fallback ($cloudModelName)"
            } else {
                "IN_CLOUD" to "Running via Firebase AI Logic ($cloudModelName)"
            }
    }

    @OptIn(PublicPreviewAPI::class)
    private fun mapUserErrorMessage(error: Throwable, hybridFailure: Throwable?): String {
        val causeChain = generateSequence<Throwable>(error) { it.cause }.toList()
        val isNetworkOffline = causeChain.any { t ->
            t is RequestTimeoutException ||
                t is ServiceConnectionHandshakeFailedException ||
                t is UnknownHostException ||
                t is ConnectException ||
                t is SocketTimeoutException ||
                t is IOException ||
                listOf("unable to resolve host", "failed to connect", "network", "offline", "unreachable", "no address", "timeout", "unavailable")
                    .any { kw -> (t.message ?: "").contains(kw, ignoreCase = true) }
        }
        val offlineOnDeviceMissing = hybridFailure is FirebaseAIOnDeviceNotAvailableException ||
            error is FirebaseAIOnDeviceNotAvailableException

        return when {
            isNetworkOffline ->
                "Hey you went offline, retry when you get connected!"
            error is QuotaExceededException ->
                "AI quota exceeded or service is busy. Please wait a moment and try again."
            error is PromptBlockedException || error is ContentBlockedException ->
                "This request or response was blocked by safety filters. Please rephrase your question."
            error is APINotConfiguredException || error is ServiceDisabledException ->
                "Firebase AI Logic is not enabled for this project. Enable it in Firebase Console."
            error is ServerException -> {
                val detail = error.localizedMessage?.takeIf { it.isNotBlank() }
                if (detail != null) "Cloud AI error: $detail"
                else "Hey you went offline, retry when you get connected!"
            }
            offlineOnDeviceMissing ->
                "Hey you went offline, retry when you get connected!"
            else ->
                error.localizedMessage ?: error.message ?: "Unable to get AI advice. Please try again."
        }
    }

    private fun buildInputContent(
        farmContext: String,
        question: String,
        image: FarmAiImage?
    ) = content {
        val fullPrompt = if (farmContext.isNotBlank()) {
            "Here are the current farm records for context:\n$farmContext\n\nFarmer's Question:\n$question"
        } else {
            question
        }
        text(fullPrompt)
        if (image != null) {
            val bytes = Base64.decode(image.base64Data, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                image(bitmap)
            }
        }
    }
}
