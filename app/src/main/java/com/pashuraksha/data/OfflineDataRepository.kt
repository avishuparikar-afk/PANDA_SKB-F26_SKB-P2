package com.pashuraksha.data

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Loads the bundled CSV datasets from app/src/main/assets/data/ at runtime.
 *
 * Everything is offline — no network, no database required. Perfect for rural
 * devices with spotty connectivity. CSVs can be hot-swapped later without
 * touching Kotlin code.
 *
 * Singleton — load once, reuse everywhere (MVVM ViewModels included).
 */
object OfflineDataRepository {

    private var diseases: List<Disease> = emptyList()
    private var symptoms: List<Symptom> = emptyList()
    private var rules: List<SymptomRule> = emptyList()
    private var remedies: List<RemedyAction> = emptyList()
    private var loaded = false

    fun ensureLoaded(ctx: Context) {
        if (loaded) return
        diseases = loadDiseases(ctx)
        symptoms = loadSymptoms(ctx)
        rules = loadRules(ctx)
        remedies = loadRemedies(ctx)
        loaded = true
    }

    fun getDiseases(): List<Disease> = diseases
    fun getSymptoms(): List<Symptom> = symptoms
    fun getRemedies(): List<RemedyAction> = remedies

    fun findDiseaseByName(name: String): Disease? =
        diseases.firstOrNull { it.name.equals(name, ignoreCase = true) }

    // ---------------------------------------------------------------------
    //  Offline inference engine
    //  Rule-based: no ML required. Replace with a TFLite interpreter later
    //  without changing callers — same DiagnosisResult contract.
    // ---------------------------------------------------------------------
    fun diagnose(selectedSymptoms: Set<String>, animal: String? = null): List<DiagnosisResult> {
        if (selectedSymptoms.isEmpty()) return emptyList()

        val relevantRules = if (animal != null)
            rules.filter { it.animal.equals(animal, ignoreCase = true) }
        else
            rules

        val scored = relevantRules.mapNotNull { rule ->
            val matched = rule.requiredSymptoms.intersect(selectedSymptoms)
            if (matched.isEmpty()) return@mapNotNull null

            val coverage = matched.size.toFloat() / rule.requiredSymptoms.size.toFloat()
            val finalConfidence = (rule.confidence * coverage).toInt()
            val disease = findDiseaseByName(rule.suggestedDisease) ?: return@mapNotNull null

            DiagnosisResult(
                disease = disease,
                confidence = finalConfidence,
                matchedSymptoms = matched.toList()
            )
        }
        // Highest confidence first, dedupe by disease
        return scored.sortedByDescending { it.confidence }
            .distinctBy { it.disease.name }
            .take(5)
    }

    // ---------------------------------------------------------------------
    //  CSV parsers — tolerant of trailing newlines, empty lines, missing cols
    // ---------------------------------------------------------------------

    private fun readAsset(ctx: Context, path: String): List<List<String>> {
        return try {
            ctx.assets.open(path).use { input ->
                BufferedReader(InputStreamReader(input)).useLines { lines ->
                    lines.filter { it.isNotBlank() }
                        .drop(1) // header row
                        .map { it.split(",").map(String::trim) }
                        .toList()
                }
            }
        } catch (t: Throwable) {
            emptyList()
        }
    }

    private fun loadDiseases(ctx: Context): List<Disease> =
        readAsset(ctx, "data/diseases.csv").mapNotNull { row ->
            if (row.size < 9) return@mapNotNull null
            Disease(
                id = row[0],
                animal = row[1],
                name = row[2],
                category = row[3],
                urgency = Urgency.fromString(row[4]),
                symptoms = row[5].split("|").map(String::trim).filter { it.isNotEmpty() },
                homeCare = row[6].split("|").map(String::trim).filter { it.isNotEmpty() },
                vetAdvice = row[7],
                icon = row[8]
            )
        }

    private fun loadSymptoms(ctx: Context): List<Symptom> =
        readAsset(ctx, "data/symptoms.csv").mapNotNull { row ->
            if (row.size < 6) return@mapNotNull null
            Symptom(
                id = row[0],
                key = row[1],
                labelEn = row[2],
                labelHi = row[3],
                labelMr = row[4],
                icon = row[5]
            )
        }

    private fun loadRules(ctx: Context): List<SymptomRule> =
        readAsset(ctx, "data/symptom_rules.csv").mapNotNull { row ->
            if (row.size < 6) return@mapNotNull null
            SymptomRule(
                id = row[0],
                requiredSymptoms = row[1].split("+").map(String::trim).filter { it.isNotEmpty() }.toSet(),
                suggestedDisease = row[2],
                confidence = row[3].toIntOrNull() ?: 50,
                urgency = Urgency.fromString(row[4]),
                animal = row[5]
            )
        }

    private fun loadRemedies(ctx: Context): List<RemedyAction> =
        readAsset(ctx, "data/remedies.csv").mapNotNull { row ->
            if (row.size < 6) return@mapNotNull null
            RemedyAction(
                id = row[0],
                key = row[1],
                labelEn = row[2],
                labelHi = row[3],
                labelMr = row[4],
                icon = row[5]
            )
        }
}
