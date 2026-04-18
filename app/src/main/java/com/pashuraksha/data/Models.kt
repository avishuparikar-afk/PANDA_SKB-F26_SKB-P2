package com.pashuraksha.data

/**
 * Single disease record parsed from assets/data/diseases.csv
 */
data class Disease(
    val id: String,
    val animal: String,
    val name: String,
    val category: String,
    val urgency: Urgency,
    val symptoms: List<String>,
    val homeCare: List<String>,
    val vetAdvice: String,
    val icon: String
)

/**
 * Visual symptom tile (image-first UI) — from assets/data/symptoms.csv
 */
data class Symptom(
    val id: String,
    val key: String,
    val labelEn: String,
    val labelHi: String,
    val labelMr: String,
    val icon: String
)

/**
 * Rule-based offline inference row — from assets/data/symptom_rules.csv
 */
data class SymptomRule(
    val id: String,
    val requiredSymptoms: Set<String>,
    val suggestedDisease: String,
    val confidence: Int,
    val urgency: Urgency,
    val animal: String
)

/**
 * Action / remedy card — from assets/data/remedies.csv
 */
data class RemedyAction(
    val id: String,
    val key: String,
    val labelEn: String,
    val labelHi: String,
    val labelMr: String,
    val icon: String
)

enum class Urgency(val level: Int, val colorHex: String) {
    LOW(1, "#00FF88"),        // green — safe
    MEDIUM(2, "#FFC107"),     // yellow — caution
    HIGH(3, "#FF6B00"),       // orange — urgent
    CRITICAL(4, "#E53935");   // red — emergency

    companion object {
        fun fromString(s: String): Urgency = when (s.trim().lowercase()) {
            "low" -> LOW
            "medium" -> MEDIUM
            "high" -> HIGH
            "critical" -> CRITICAL
            else -> LOW
        }
    }
}

/**
 * Result returned by the offline inference engine.
 */
data class DiagnosisResult(
    val disease: Disease,
    val confidence: Int,
    val matchedSymptoms: List<String>
)
