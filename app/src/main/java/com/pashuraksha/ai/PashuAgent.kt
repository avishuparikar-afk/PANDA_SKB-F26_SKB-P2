package com.pashuraksha.ai

import android.content.Context
import com.pashuraksha.data.DiagnosisResult
import com.pashuraksha.data.OfflineDataRepository
import com.pashuraksha.data.Urgency

/**
<<<<<<< HEAD
 * PashuAgent -- the Mini Manus AI for livestock.
 *
 * Pipeline:
 *   1. PERCEIVE  -- collect symptoms / image / context from the farmer
 *   2. DIAGNOSE  -- run Gemini Vision (online) OR rule engine (offline)
 *   3. REASON    -- calculate urgency, outbreak risk, herd impact
 *   4. ACT       -- produce a structured action plan the farmer can execute
 *
 * Now routes through AiEngineManager for intelligent online/offline switching.
 */
object PashuAgent {

=======
 * PashuAgent — the Mini Manus AI for livestock.
 *
 * Unlike a chatbot that just answers, this agent runs a full pipeline end-to-end:
 *
 *   1. PERCEIVE  — collect symptoms / image / context from the farmer
 *   2. DIAGNOSE  — run Gemini Vision (online) OR rule engine (offline)
 *   3. REASON    — calculate urgency, outbreak risk, herd impact
 *   4. ACT       — produce a structured action plan the farmer can execute
 *
 * The agent auto-chooses online vs offline. Output is always the same
 * AgentResult structure so the UI never has to branch.
 *
 * This is the "architecture slide" for judges:
 *
 *   UI  →  PashuAgent  →  [ GeminiClient | Rule Engine ]
 *                     →  Knowledge Base (CSVs)
 *                     →  AgentResult { diagnosis, plan, risk }
 */
object PashuAgent {

    /**
     * The complete end-to-end output of one agent run.
     * UI renders this directly — no further logic needed.
     */
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
    data class AgentResult(
        val diagnosis: String,
        val confidence: Int,
        val urgency: Urgency,
<<<<<<< HEAD
        val urgencyLabel: String,
        val homeCareSteps: List<String>,
        val vetAdvice: String,
        val outbreakRisk: String,
        val mode: String,
        val rawAnswer: String
    )

    suspend fun run(
=======
        val urgencyLabel: String,    // "Safe" / "Watch" / "Urgent" / "Emergency"
        val homeCareSteps: List<String>,
        val vetAdvice: String,
        val outbreakRisk: String,    // "Low" / "Medium" / "High"
        val mode: String,            // "online-ai" or "offline-edge"
        val rawAnswer: String        // full text for chat display
    )

    /**
     * Main entry point. Blocks — call from a coroutine on Dispatchers.IO.
     *
     * @param ctx          Android context (needed to read CSV assets)
     * @param farmerQuery  what the farmer typed OR symptom tags from a scan
     * @param symptomKeys  optional symptoms pre-selected in the UI (image-first)
     * @param animal       optional animal type: cow, buffalo, goat, chicken, dog…
     * @param herdSize     optional — used for outbreak risk scoring
     */
    fun run(
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        ctx: Context,
        farmerQuery: String,
        symptomKeys: Set<String> = emptySet(),
        animal: String? = null,
        herdSize: Int = 1,
        previousInfectedCount: Int = 0
    ): AgentResult {

        OfflineDataRepository.ensureLoaded(ctx)

<<<<<<< HEAD
        // STEP 1 -- PERCEIVE
        val extractedSymptoms = symptomKeys.toMutableSet()
        extractedSymptoms.addAll(extractSymptomsFromText(ctx, farmerQuery))

        // STEP 2 -- DIAGNOSE
        val offlineResults = OfflineDataRepository.diagnose(extractedSymptoms, animal)

        // Determine mode using AiEngineManager
        val currentMode = AiEngineManager.getCurrentMode(ctx)

        // STEP 3 -- REASON
=======
        // STEP 1 — PERCEIVE: merge farmer text with explicit symptom tags
        val extractedSymptoms = symptomKeys.toMutableSet()
        extractedSymptoms.addAll(extractSymptomsFromText(ctx, farmerQuery))

        // STEP 2 — DIAGNOSE: offline rule engine always runs (fast, reliable baseline)
        val offlineResults = OfflineDataRepository.diagnose(extractedSymptoms, animal)

        // Decide mode
        val online = try {
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as android.net.ConnectivityManager
            val n = cm.activeNetwork
            n != null && cm.getNetworkCapabilities(n)
                ?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (_: Throwable) { false }

        // STEP 3 — REASON: compute urgency + outbreak risk
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        val top = offlineResults.firstOrNull()
        val urgency = top?.disease?.urgency ?: Urgency.LOW
        val outbreakRisk = calculateOutbreakRisk(
            urgency, herdSize, previousInfectedCount, extractedSymptoms.size
        )

<<<<<<< HEAD
        // STEP 4 -- ACT: Use AiEngineManager for intelligent routing
        val textAnswer = try {
            when (currentMode) {
                AiEngineManager.AiMode.ONLINE -> {
                    askGeminiWithGrounding(ctx, farmerQuery, top, urgency, outbreakRisk)
                }
                AiEngineManager.AiMode.OFFLINE_LLM -> {
                    generateOfflineLLMAnswer(ctx, farmerQuery, top, urgency, outbreakRisk)
                }
                AiEngineManager.AiMode.RULE_BASED -> {
                    buildOfflineAnswer(top, urgency, outbreakRisk)
                }
            }
        } catch (e: Exception) {
            // Ultimate fallback -- NEVER crash
            buildOfflineAnswer(top, urgency, outbreakRisk)
        }

        val modeLabel = when (currentMode) {
            AiEngineManager.AiMode.ONLINE -> "online-ai"
            AiEngineManager.AiMode.OFFLINE_LLM -> "offline-llm"
            AiEngineManager.AiMode.RULE_BASED -> "offline-edge"
        }

        return AgentResult(
            diagnosis = top?.disease?.name ?: "Unknown -- need more info",
=======
        // STEP 4 — ACT: build the final answer
        // If online + we have an API key, ask Gemini to produce a farmer-friendly
        // treatment plan using our rule-engine findings as grounding context.
        val textAnswer = if (online) {
            askGeminiWithGrounding(ctx, farmerQuery, top, urgency, outbreakRisk)
        } else {
            buildOfflineAnswer(top, urgency, outbreakRisk)
        }

        return AgentResult(
            diagnosis = top?.disease?.name ?: "Unknown — need more info",
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
            confidence = top?.confidence ?: 0,
            urgency = urgency,
            urgencyLabel = urgencyLabel(urgency),
            homeCareSteps = top?.disease?.homeCare ?: emptyList(),
            vetAdvice = top?.disease?.vetAdvice ?: "",
            outbreakRisk = outbreakRisk,
<<<<<<< HEAD
            mode = modeLabel,
=======
            mode = if (online) "online-ai" else "offline-edge",
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
            rawAnswer = textAnswer
        )
    }

    // -----------------------------------------------------------------
<<<<<<< HEAD
    //  Offline LLM answer generation
    // -----------------------------------------------------------------
    private suspend fun generateOfflineLLMAnswer(
        ctx: Context,
        farmerQuery: String,
        top: DiagnosisResult?,
        urgency: Urgency,
        outbreakRisk: String
    ): String {
        LocalLLMManager.initModel(ctx)

        if (!LocalLLMManager.isInitialized) {
            return buildOfflineAnswer(top, urgency, outbreakRisk)
        }

        val grounding = if (top != null) {
            "Context: I detected ${top.disease.name} (${top.confidence}% match). " +
            "Home care: ${top.disease.homeCare.joinToString(", ")}. " +
            "Vet advice: ${top.disease.vetAdvice}."
        } else ""

        val prompt = buildString {
            append("You are Pashu Doctor, a friendly livestock vet for Indian farmers.\n")
            if (grounding.isNotBlank()) append("$grounding\n")
            append("Farmer says: \"$farmerQuery\"\n")
            append("Respond helpfully with home care steps and vet advice. Use emojis.")
        }

        return LocalLLMManager.generateResponse(prompt)
    }

    // -----------------------------------------------------------------
    //  Symptom extraction
=======
    //  STEP 1 helpers — crude NLP for offline symptom extraction
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
    // -----------------------------------------------------------------
    private fun extractSymptomsFromText(ctx: Context, text: String): Set<String> {
        val t = text.lowercase()
        return OfflineDataRepository.getSymptoms()
            .filter { sym ->
                t.contains(sym.key.replace("_", " ")) ||
                t.contains(sym.labelEn.lowercase()) ||
                t.contains(sym.labelHi) ||
                t.contains(sym.labelMr)
            }
            .map { it.key }
            .toSet()
    }

    // -----------------------------------------------------------------
<<<<<<< HEAD
    //  Outbreak risk scoring
=======
    //  STEP 3 — outbreak risk scoring (simple, explainable)
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
    // -----------------------------------------------------------------
    private fun calculateOutbreakRisk(
        urgency: Urgency,
        herdSize: Int,
        previousInfectedCount: Int,
        symptomCount: Int
    ): String {
        var score = 0
<<<<<<< HEAD
        score += urgency.level * 10
        score += (previousInfectedCount * 15)
        score += if (symptomCount >= 3) 10 else 0
=======
        score += urgency.level * 10                // severity 10-40
        score += (previousInfectedCount * 15)      // spread signal
        score += if (symptomCount >= 3) 10 else 0  // multiple symptoms
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        if (herdSize >= 10 && previousInfectedCount >= 2) score += 20

        return when {
            score >= 60 -> "High"
            score >= 30 -> "Medium"
            else        -> "Low"
        }
    }

    // -----------------------------------------------------------------
<<<<<<< HEAD
    //  Answer formatting
=======
    //  STEP 4 — answer formatting
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
    // -----------------------------------------------------------------
    private fun buildOfflineAnswer(
        top: DiagnosisResult?,
        urgency: Urgency,
        outbreakRisk: String
    ): String {
        if (top == null) {
<<<<<<< HEAD
            return "I need more information to help. Please describe symptoms -- fever, mouth sores, swelling, diarrhea, limping -- or tap Scan Animal to take a photo."
        }
        val d = top.disease
        return buildString {
            append("Likely: ${d.name}  (${top.confidence}% match)\n\n")
            append("Urgency: ${urgencyLabel(urgency)}\n")
            append("Outbreak risk: $outbreakRisk\n\n")
            append("Home care:\n")
            d.homeCare.take(3).forEach { append("  * $it\n") }
            append("\nVet: ${d.vetAdvice}")
            if (urgency == Urgency.CRITICAL) {
                append("\n\nCALL VET NOW -- this is an emergency.")
=======
            return "I need more information to help. Please describe symptoms — fever, mouth sores, swelling, diarrhea, limping — or tap 📷 Scan Animal to take a photo."
        }
        val d = top.disease
        return buildString {
            append("🔍 Likely: ${d.name}  (${top.confidence}% match)\n\n")
            append("⚠️ Urgency: ${urgencyLabel(urgency)}\n")
            append("📊 Outbreak risk: $outbreakRisk\n\n")
            append("🏠 Home care:\n")
            d.homeCare.take(3).forEach { append("  • $it\n") }
            append("\n👨‍⚕️ Vet: ${d.vetAdvice}")
            if (urgency == Urgency.CRITICAL) {
                append("\n\n🚨 CALL VET NOW — this is an emergency.")
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
            }
        }
    }

    private fun askGeminiWithGrounding(
        ctx: Context,
        farmerQuery: String,
        top: DiagnosisResult?,
        urgency: Urgency,
        outbreakRisk: String
    ): String {
<<<<<<< HEAD
        val grounding = if (top != null) {
            "My offline knowledge base suggests: ${top.disease.name} (${top.confidence}% match). " +
            "Urgency: ${urgencyLabel(urgency)}. Outbreak risk: $outbreakRisk. " +
            "Home care from CSV: ${top.disease.homeCare.joinToString("; ")}. " +
            "Vet advice from CSV: ${top.disease.vetAdvice}."
        } else ""

        val prompt = buildString {
            append(grounding)
            append("\n\nFarmer says: \"$farmerQuery\"\n\n")
            append("Respond in the farmer's language. Keep it short, warm, and structured:\n")
            append("* Likely issue (1 line)\n")
            append("* 2-3 home care steps\n")
            append("* When to call vet")
        }
=======
        // Ground Gemini with our offline findings — reduces hallucination
        val grounding = if (top != null) {
            """
            My offline knowledge base suggests: ${top.disease.name} (${top.confidence}% match).
            Urgency: ${urgencyLabel(urgency)}. Outbreak risk: $outbreakRisk.
            Home care from CSV: ${top.disease.homeCare.joinToString("; ")}.
            Vet advice from CSV: ${top.disease.vetAdvice}.
            """.trimIndent()
        } else ""

        val prompt = """
            $grounding

            Farmer says: "$farmerQuery"

            Respond in the farmer's language. Keep it short, warm, and structured:
            • Likely issue (1 line)
            • 2-3 home care steps (with 🏠 emoji)
            • When to call vet (with 👨‍⚕️ emoji)
            """.trimIndent()
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e

        return GeminiClient.ask(ctx, prompt, emptyList())
    }

    private fun urgencyLabel(u: Urgency): String = when (u) {
        Urgency.LOW      -> "Safe"
        Urgency.MEDIUM   -> "Watch"
        Urgency.HIGH     -> "Urgent"
        Urgency.CRITICAL -> "Emergency"
    }
}
