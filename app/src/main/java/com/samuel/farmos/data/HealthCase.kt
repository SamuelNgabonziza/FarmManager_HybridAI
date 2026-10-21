package com.samuel.farmos.data

enum class ClinicalSign { FEVER, LOSS_OF_APPETITE, DIARRHEA, COUGHING, LETHARGY, WOUND, LAMENESS, OTHER }
enum class HealthCaseStatus { ACTIVE, RECOVERED, CRITICAL, CLOSED }

data class HealthCase(
    val id: String = "",
    val linkedAnimalId: String = "",
    val mainComplaint: String = "",
    val durationDays: Int? = null,
    val temperatureCelsius: Double? = null,
    val appetite: Appetite = Appetite.NORMAL,
    val waterIntake: WaterIntake = WaterIntake.NORMAL,
    val clinicalSigns: List<ClinicalSign> = emptyList(),
    val notes: String = "",
    val status: HealthCaseStatus = HealthCaseStatus.ACTIVE,
    val aiAnalysis: String? = null,
    val createdDateMillis: Long = System.currentTimeMillis()
)
