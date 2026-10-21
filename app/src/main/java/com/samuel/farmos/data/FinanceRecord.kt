package com.samuel.farmos.data

enum class FinanceType { INCOME, EXPENSE }
enum class FinanceCategory { MILK_SALES, LIVESTOCK_SALES, FEED, MEDICINE, EQUIPMENT, LABOR, OTHER }

data class FinanceRecord(
    val id: String = "",
    val type: FinanceType = FinanceType.EXPENSE,
    val category: FinanceCategory = FinanceCategory.OTHER,
    val amountUgx: Double = 0.0,
    val dateMillis: Long = System.currentTimeMillis(),
    val description: String = "",
    val linkedAnimalId: String? = null
)
