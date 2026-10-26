package com.samuel.farmos.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.ClinicalSign
import com.samuel.farmos.data.FinanceCategory
import com.samuel.farmos.data.FinanceRecord
import com.samuel.farmos.data.FinanceType
import com.samuel.farmos.data.HealthCase
import com.samuel.farmos.data.HealthCaseStatus
import com.samuel.farmos.data.ProductionRecord
import com.samuel.farmos.data.ProductionType
import com.samuel.farmos.data.Task
import com.samuel.farmos.data.TaskCategory
import java.util.Calendar
import java.util.Locale

sealed class ParsedVoiceAction {
    data class Milk(val animalId: String?, val liters: Double) : ParsedVoiceAction()
    data class Health(val animalId: String?, val issue: String, val sign: ClinicalSign) : ParsedVoiceAction()
    data class Expense(val amount: Double, val category: FinanceCategory, val note: String) : ParsedVoiceAction()
    data class NewTask(val title: String, val category: TaskCategory) : ParsedVoiceAction()
    data class Unknown(val rawText: String) : ParsedVoiceAction()
}

class VoiceInputHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onListeningState: (Boolean) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Voice recognition is not available on this device")
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onListeningState(true)
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    onListeningState(false)
                }

                override fun onError(error: Int) {
                    onListeningState(false)
                    val message = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timed out."
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                        else -> "Could not recognize speech ($error)."
                    }
                    onError(message)
                }

                override fun onResults(results: Bundle?) {
                    onListeningState(false)
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull() ?: ""
                    if (spokenText.isNotBlank()) {
                        onResult(spokenText)
                    } else {
                        onError("No speech detected.")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your farm log (e.g. '12 liters milk for Bella' or 'Spent 40000 on feed')")
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            onListeningState(false)
            onError(e.localizedMessage ?: "Failed to start listening")
        }
    }

    fun stopListening() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        onListeningState(false)
    }

    companion object {
        fun parseSpeech(text: String, animals: List<Animal>): ParsedVoiceAction {
            val lower = text.lowercase()

            // 1. Try to find a matching animal
            val matchedAnimal = animals.firstOrNull { animal ->
                (animal.name.isNotBlank() && lower.contains(animal.name.lowercase())) ||
                (animal.tagNumber.isNotBlank() && lower.contains(animal.tagNumber.lowercase()))
            }

            // 2. Milk detection
            if (lower.contains("milk") || lower.contains("liter") || lower.contains("litres") || lower.contains("litres")) {
                val numberRegex = Regex("""(\d+(\.\d+)?)""")
                val match = numberRegex.find(lower)
                val liters = match?.value?.toDoubleOrNull() ?: 10.0
                return ParsedVoiceAction.Milk(matchedAnimal?.id, liters)
            }

            // 3. Health detection
            if (lower.contains("sick") || lower.contains("cough") || lower.contains("fever") ||
                lower.contains("limp") || lower.contains("wound") || lower.contains("diarrhea")
            ) {
                val sign = when {
                    lower.contains("cough") -> ClinicalSign.COUGHING
                    lower.contains("fever") -> ClinicalSign.FEVER
                    lower.contains("limp") -> ClinicalSign.LAMENESS
                    lower.contains("diarrhea") -> ClinicalSign.DIARRHEA
                    lower.contains("wound") -> ClinicalSign.WOUND
                    else -> ClinicalSign.LETHARGY
                }
                return ParsedVoiceAction.Health(matchedAnimal?.id, text, sign)
            }

            // 4. Expense detection
            if (lower.contains("bought") || lower.contains("spent") || lower.contains("cost") ||
                lower.contains("paid") || lower.contains("expense") || lower.contains("shillings") || lower.contains("ugx")
            ) {
                val numberRegex = Regex("""(\d[\d,]*)""")
                val match = numberRegex.find(lower.replace(",", ""))
                val amount = match?.value?.toDoubleOrNull() ?: 0.0

                val category = when {
                    lower.contains("feed") || lower.contains("bran") || lower.contains("silage") -> FinanceCategory.FEED
                    lower.contains("vet") || lower.contains("medicine") || lower.contains("drug") -> FinanceCategory.MEDICINE
                    lower.contains("worker") || lower.contains("labor") || lower.contains("salary") -> FinanceCategory.LABOR
                    lower.contains("tool") || lower.contains("equipment") -> FinanceCategory.EQUIPMENT
                    else -> FinanceCategory.OTHER
                }
                return ParsedVoiceAction.Expense(amount, category, text)
            }

            // 5. Task detection
            if (lower.contains("task") || lower.contains("clean") || lower.contains("feed") ||
                lower.contains("vaccinate") || lower.contains("repair")
            ) {
                val category = when {
                    lower.contains("feed") -> TaskCategory.FEEDING
                    lower.contains("vaccin") || lower.contains("treat") -> TaskCategory.HEALTH
                    lower.contains("clean") || lower.contains("repair") -> TaskCategory.MAINTENANCE
                    else -> TaskCategory.OTHER
                }
                return ParsedVoiceAction.NewTask(text.replaceFirstChar { it.uppercase() }, category)
            }

            return ParsedVoiceAction.Unknown(text)
        }
    }
}
