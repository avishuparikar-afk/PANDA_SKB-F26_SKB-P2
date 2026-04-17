package com.avish.pashupatinath.logic

import com.avish.pashupatinath.data.AnimalRecord

enum class Disease(val displayName: String, val symptoms: List<String>) {
    LUMPY_SKIN("Lumpy Skin Disease", listOf("Skin lesions", "Fever", "Reduced milk")),
    FOOT_AND_MOUTH("Foot & Mouth Disease", listOf("Fever", "Blisters", "Weight loss")),
    MASTITIS("Mastitis", listOf("Reduced milk", "Swelling", "Fever"))
}

object HealthEngine {
    fun diagnose(symptoms: List<String>): Pair<Disease?, Int> {
        if (symptoms.isEmpty()) return null to 0
        
        var bestMatch: Disease? = null
        var maxConfidence = 0
        
        for (disease in Disease.values()) {
            val matchCount = disease.symptoms.count { it in symptoms }
            val confidence = (matchCount.toFloat() / disease.symptoms.size * 100).toInt()
            if (confidence > maxConfidence) {
                maxConfidence = confidence
                bestMatch = disease
            }
        }
        
        return bestMatch to (maxConfidence.coerceAtLeast(10))
    }

    fun calculateRiskScore(symptoms: List<String>): Int {
        return (symptoms.size * 25).coerceAtMost(100)
    }

    fun getOutbreakRisk(recentRecords: List<AnimalRecord>): RiskLevel {
        val count = recentRecords.size
        return when {
            count >= 3 -> RiskLevel.HIGH
            count >= 2 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }
}

enum class RiskLevel(val label: String, val color: Long) {
    LOW("Low", 0xFF4CAF50),
    MEDIUM("Medium", 0xFFFFC107),
    HIGH("High", 0xFFF44336)
}
