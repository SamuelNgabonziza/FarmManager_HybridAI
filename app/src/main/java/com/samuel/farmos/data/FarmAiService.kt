package com.samuel.farmos.data

import kotlinx.coroutines.flow.Flow

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

fun Throwable.toUserMessage(): String {
    if (this is FarmAiNotConfiguredException) return message ?: "The AI assistant is not available."
    if (this is FarmAiRefusalException) return message ?: "The assistant declined to answer."
    if (this is FarmAiEmptyAnswerException) return message ?: "The assistant returned an empty answer."
    return localizedMessage ?: message ?: "Something went wrong. Please try again."
}
