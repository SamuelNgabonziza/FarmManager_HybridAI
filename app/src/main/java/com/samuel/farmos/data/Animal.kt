package com.samuel.farmos.data

enum class Species { COW, GOAT, SHEEP, PIG, POULTRY, OTHER }
enum class Gender { FEMALE, MALE }
enum class AnimalStatus { HEALTHY, SICK, PREGNANT, SOLD, DECEASED, QUARANTINED }

object BreedSuggestions {
    val cowBreeds = listOf("Holstein Friesian", "Jersey", "Ankole", "Boran", "Ayrshire", "Guernsey", "Zebu")
    val goatBreeds = listOf("Boer", "Saanen", "Toggenburg", "Kalahari Red", "Local/Mixed")
    val sheepBreeds = listOf("Dorper", "Merino", "Blackhead Persian", "Local/Mixed")

    fun forSpecies(species: Species): List<String> = when (species) {
        Species.COW -> cowBreeds
        Species.GOAT -> goatBreeds
        Species.SHEEP -> sheepBreeds
        else -> emptyList()
    }
}

data class Animal(
    val id: String = "",
    val species: Species = Species.COW,
    val tagNumber: String = "",
    val name: String = "",
    val breed: String = "",
    val gender: Gender = Gender.FEMALE,
    val isCastrated: Boolean? = null,
    val birthDate: Long? = null,
    val weightKg: Double? = null,
    val status: AnimalStatus = AnimalStatus.HEALTHY
)
