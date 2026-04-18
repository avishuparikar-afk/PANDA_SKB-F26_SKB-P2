package com.pashuraksha.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Central session store that tracks real results from every feature.
 * Persisted via SharedPreferences so data survives screen rotations
 * and is available in ReportFragment.
 *
 * This is what makes the Report screen "real" — connected to actual
 * scan, diagnosis, and immunity data entered/computed by the farmer.
 */
object SessionData {

    private const val PREF_NAME = "pashu_raksha_session"

    private lateinit var prefs: SharedPreferences

    fun init(ctx: Context) {
        if (!::prefs.isInitialized) {
            prefs = ctx.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    // ── Scan Results ────────────────────────────────────────────────────

    fun saveScanResult(
        totalAnimals: Int,
        healthyCount: Int,
        watchCount: Int,
        alertCount: Int
    ) {
        prefs.edit()
            .putInt("scan_total", totalAnimals)
            .putInt("scan_healthy", healthyCount)
            .putInt("scan_watch", watchCount)
            .putInt("scan_alert", alertCount)
            .putString("scan_timestamp", nowString())
            .putBoolean("has_scan", true)
            .apply()
    }

    fun hasScanData(): Boolean = prefs.getBoolean("has_scan", false)
    fun getScanTotal(): Int = prefs.getInt("scan_total", 0)
    fun getScanHealthy(): Int = prefs.getInt("scan_healthy", 0)
    fun getScanWatch(): Int = prefs.getInt("scan_watch", 0)
    fun getScanAlert(): Int = prefs.getInt("scan_alert", 0)
    fun getScanTimestamp(): String = prefs.getString("scan_timestamp", "") ?: ""

    // ── Immunity Gap ────────────────────────────────────────────────────

    fun saveImmunityResult(
        village: String,
        district: String,
        totalCattle: Int,
        vaccinatedCount: Int,
        gapPercent: Float,
        outbreakDays: Int
    ) {
        prefs.edit()
            .putString("imm_village", village)
            .putString("imm_district", district)
            .putInt("imm_total", totalCattle)
            .putInt("imm_vaccinated", vaccinatedCount)
            .putFloat("imm_gap", gapPercent)
            .putInt("imm_outbreak_days", outbreakDays)
            .putString("imm_timestamp", nowString())
            .putBoolean("has_immunity", true)
            .apply()
    }

    fun hasImmunityData(): Boolean = prefs.getBoolean("has_immunity", false)
    fun getImmVillage(): String = prefs.getString("imm_village", "") ?: ""
    fun getImmDistrict(): String = prefs.getString("imm_district", "") ?: ""
    fun getImmTotal(): Int = prefs.getInt("imm_total", 0)
    fun getImmVaccinated(): Int = prefs.getInt("imm_vaccinated", 0)
    fun getImmGap(): Float = prefs.getFloat("imm_gap", 0f)
    fun getImmOutbreakDays(): Int = prefs.getInt("imm_outbreak_days", 0)

    // ── Disease Detection ───────────────────────────────────────────────

    fun saveDiagnosisResult(
        disease: String,
        confidence: Int,
        symptoms: String,
        recommendations: String,
        urgency: String
    ) {
        prefs.edit()
            .putString("diag_disease", disease)
            .putInt("diag_confidence", confidence)
            .putString("diag_symptoms", symptoms)
            .putString("diag_recommendations", recommendations)
            .putString("diag_urgency", urgency)
            .putString("diag_timestamp", nowString())
            .putBoolean("has_diagnosis", true)
            .apply()
    }

    fun hasDiagnosisData(): Boolean = prefs.getBoolean("has_diagnosis", false)
    fun getDiagDisease(): String = prefs.getString("diag_disease", "") ?: ""
    fun getDiagConfidence(): Int = prefs.getInt("diag_confidence", 0)
    fun getDiagSymptoms(): String = prefs.getString("diag_symptoms", "") ?: ""
    fun getDiagRecommendations(): String = prefs.getString("diag_recommendations", "") ?: ""
    fun getDiagUrgency(): String = prefs.getString("diag_urgency", "") ?: ""

    // ── Language Preference ─────────────────────────────────────────────

    fun saveLanguage(langCode: String) {
        prefs.edit().putString("language", langCode).apply()
    }

    fun getLanguage(): String = prefs.getString("language", "en") ?: "en"

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun nowString(): String =
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
}
