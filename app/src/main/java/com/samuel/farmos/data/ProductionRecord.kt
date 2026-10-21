package com.samuel.farmos.data

enum class ProductionType { MILK, EGGS, MEAT, WOOL, HONEY, OTHER }

data class ProductionRecord(
    val id: String = "",
    val type: ProductionType = ProductionType.MILK,
    val linkedAnimalId: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val quantity: Double = 0.0
)
