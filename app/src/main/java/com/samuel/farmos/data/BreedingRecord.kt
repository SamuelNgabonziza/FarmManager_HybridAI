package com.samuel.farmos.data

enum class BreedingEventType { HEAT, INSEMINATION, PREGNANCY_CHECK, BIRTH, ABORTION }

object GestationPeriod {
    private const val COW_DAYS = 283
    private const val GOAT_DAYS = 150
    private const val SHEEP_DAYS = 152

    fun daysFor(species: Species): Int = when (species) {
        Species.COW -> COW_DAYS
        Species.GOAT -> GOAT_DAYS
        Species.SHEEP -> SHEEP_DAYS
        else -> 150
    }

    fun birthEventLabel(species: Species): String = when (species) {
        Species.COW -> "calving"
        Species.GOAT -> "kidding"
        Species.SHEEP -> "lambing"
        else -> "birth"
    }
}

data class BreedingRecord(
    val id: String = "",
    val linkedAnimalId: String = "",
    val eventType: BreedingEventType = BreedingEventType.HEAT,
    val eventDateMillis: Long = System.currentTimeMillis(),
    val expectedDueDateMillis: Long? = null,
    val notes: String = ""
)
