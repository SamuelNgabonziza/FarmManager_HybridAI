package com.samuel.farmos.data

enum class Appetite { POOR, NORMAL, VORACIOUS }
enum class WaterIntake { REDUCED, NORMAL, INCREASED }

data class FeedSuggestions(val suggestions: List<String> = emptyList())

data class FeedingRecord(
    val id: String = "",
    val linkedAnimalId: String? = null,
    val feedType: String = "",
    val quantityKg: Double = 0.0,
    val dateMillis: Long = System.currentTimeMillis(),
    val costUgx: Double? = null
)
