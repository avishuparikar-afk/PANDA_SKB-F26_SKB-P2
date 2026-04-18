package com.pashuraksha.ai

import android.content.Context
import android.util.Log
import com.pashuraksha.BuildConfig
import com.pashuraksha.data.OfflineDataRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * AI backend for the chatbot.
 *
 *   online  → OpenRouter API (Gemini 2.0 Flash via openrouter.ai)
 *   offline → comprehensive rule engine + keyword matcher over CSV data
 *
 * No Retrofit, no heavy deps — just HttpURLConnection. Keeps APK small.
 *
 * The API key comes from local.properties → BuildConfig.OPENROUTER_API_KEY
 * so it never leaks into git.
 */
object GeminiClient {

    private const val TAG = "GeminiClient"

    private const val OPENROUTER_ENDPOINT =
        "https://openrouter.ai/api/v1/chat/completions"

    // Model to use — Gemini 2.0 Flash via OpenRouter (fast, cheap, multimodal)
    private const val MODEL = "google/gemini-2.0-flash-001"

    private const val SYSTEM_PROMPT = """
You are Pashu Doctor AI, a friendly livestock health advisor for Indian rural
farmers. Answer in the language the farmer uses (Hindi, Marathi, or English).
Keep answers SHORT, PRACTICAL, and STRUCTURED:
  • Likely issue
  • 2-3 home care steps
  • When to call vet

Avoid jargon. If the farmer describes an emergency (sudden death, blood
discharge, severe bloat, breathing failure) tell them to CALL A VET NOW.
"""

    /**
     * Ask the AI. Blocks — call from a background coroutine / thread.
     * Returns a user-facing string (never throws to the caller).
     */
    fun ask(ctx: Context, userMessage: String, history: List<Pair<String, String>>): String {
        val apiKey = BuildConfig.OPENROUTER_API_KEY

        // If key missing or placeholder → fall straight to offline
        if (apiKey.isBlank() || apiKey.startsWith("YOUR_")) {
            return offlineAnswer(ctx, userMessage)
        }

        return try {
            callOpenRouter(apiKey, userMessage, history)
        } catch (t: Throwable) {
            Log.e(TAG, "OpenRouter call failed, falling back to offline", t)
            val fallback = offlineAnswer(ctx, userMessage)
            "$fallback\n\n(offline mode — check internet for AI answers)"
        }
    }

    // -----------------------------------------------------------------
    // Online — OpenRouter API (OpenAI-compatible chat completion format)
    // -----------------------------------------------------------------
    private fun callOpenRouter(
        apiKey: String,
        userMessage: String,
        history: List<Pair<String, String>>
    ): String {
        val url = URL(OPENROUTER_ENDPOINT)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15000
            readTimeout = 30000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("HTTP-Referer", "https://pashuraksha.app")
            setRequestProperty("X-Title", "PashuRaksha")
        }

        // Build OpenAI-style messages array
        val messages = JSONArray()

        // System message
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", SYSTEM_PROMPT.trim())
        })

        // Replay last ~6 history turns for context
        history.takeLast(6).forEach { (role, text) ->
            messages.put(JSONObject().apply {
                put("role", if (role == "user") "user" else "assistant")
                put("content", text)
            })
        }

        // Current user turn
        messages.put(JSONObject().apply {
            put("role", "user")
            put("content", userMessage)
        })

        val body = JSONObject().apply {
            put("model", MODEL)
            put("messages", messages)
            put("temperature", 0.6)
            put("max_tokens", 512)
        }.toString()

        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val response = stream.bufferedReader().use(BufferedReader::readText)

        if (code !in 200..299) {
            Log.e(TAG, "OpenRouter HTTP $code: $response")
            throw RuntimeException("OpenRouter HTTP $code")
        }

        // Parse OpenAI-style response: choices[0].message.content
        val root = JSONObject(response)
        val choices = root.optJSONArray("choices") ?: return "…"
        if (choices.length() == 0) return "…"
        val content = choices.getJSONObject(0)
            .optJSONObject("message")
            ?.optString("content", "") ?: ""
        return content.trim().ifEmpty { "I didn't catch that. Please ask again." }
    }

    // -----------------------------------------------------------------
    // Offline — comprehensive rule-based engine
    // -----------------------------------------------------------------
    private fun offlineAnswer(ctx: Context, userMessage: String): String {
        val msg = userMessage.lowercase()

        // 1. Try CSV chatbot Q&A first (exact topic matches)
        val csvAnswer = matchChatbotCsv(ctx, msg)
        if (csvAnswer != null) return csvAnswer

        // 2. Try symptom-based offline diagnosis engine
        val diagnosisAnswer = matchSymptomDiagnosis(msg)
        if (diagnosisAnswer != null) return diagnosisAnswer

        // 3. Try common keyword responses (greetings, emergencies, general queries)
        val keywordAnswer = matchCommonKeywords(msg)
        if (keywordAnswer != null) return keywordAnswer

        // 4. Final fallback
        return buildString {
            append("I need more details to help. Try describing symptoms like:\n\n")
            append("• \"My cow has fever and not eating\"\n")
            append("• \"Buffalo has swollen udder\"\n")
            append("• \"Goat is limping and has mouth sores\"\n")
            append("• \"गाय को बुखार है\"\n\n")
            append("Or tap 📷 Scan Animal for image-based diagnosis.")
        }
    }

    /**
     * Match against chatbot.csv — scored keyword matching
     */
    private fun matchChatbotCsv(ctx: Context, msg: String): String? {
        val rows = readChatbotCsv(ctx)
        var bestRow: Array<String>? = null
        var bestScore = 0

        for (row in rows) {
            if (row.size < 3) continue
            val keywords = row[1].split("|").map { it.trim().lowercase() }
            val hits = keywords.count { it.isNotEmpty() && msg.contains(it) }
            if (hits > bestScore) {
                bestScore = hits
                bestRow = row
            }
        }

        return if (bestRow != null && bestScore > 0) bestRow[2] else null
    }

    /**
     * Match symptoms from the text against OfflineDataRepository
     * Supports English, Hindi, and Marathi symptom names
     */
    private fun matchSymptomDiagnosis(msg: String): String? {
        val hits = OfflineDataRepository.getSymptoms()
            .filter { sym ->
                msg.contains(sym.key.replace("_", " ")) ||
                msg.contains(sym.labelEn.lowercase()) ||
                msg.contains(sym.labelHi) ||
                msg.contains(sym.labelMr)
            }
            .map { it.key }
            .toSet()

        if (hits.isEmpty()) return null

        val results = OfflineDataRepository.diagnose(hits)
        if (results.isEmpty()) return null

        val top = results.first()
        return buildString {
            append("🔍 Possible: ${top.disease.name} (${top.confidence}% match)\n\n")
            if (results.size > 1) {
                append("Also consider: ${results[1].disease.name} (${results[1].confidence}%)\n\n")
            }
            append("🏠 Home care:\n")
            top.disease.homeCare.take(3).forEach { append("  • $it\n") }
            append("\n👨‍⚕️ Vet: ${top.disease.vetAdvice}")
            if (top.disease.urgency.level >= 3) {
                append("\n\n🚨 URGENT — Contact your vet immediately!")
            }
        }
    }

    /**
     * Common keyword-based responses for greetings, emergencies,
     * and general livestock farming queries
     */
    private fun matchCommonKeywords(msg: String): String? {
        // Emergency keywords
        val emergencyWords = listOf("death", "dying", "dead", "blood", "bleeding",
            "choking", "poison", "snake", "मरना", "खून", "मौत", "जहर")
        if (emergencyWords.any { msg.contains(it) }) {
            return "🚨 EMERGENCY!\n\n" +
                "1. Keep the animal calm and isolated\n" +
                "2. Do NOT give any oral medicine if choking\n" +
                "3. Call Vet Helpline NOW: 1962\n" +
                "4. Note the time symptoms started\n\n" +
                "⚡ Every minute counts — call a vet immediately!"
        }

        // Greetings
        val greetWords = listOf("hello", "hi", "namaste", "namaskar", "help",
            "helo", "hey", "नमस्ते", "नमस्कार", "मदद")
        if (greetWords.any { msg.contains(it) }) {
            return "🌿 Namaste! I'm Pashu Doctor.\n\n" +
                "Tell me your animal's symptoms and I'll help:\n" +
                "• What animal? (cow, buffalo, goat…)\n" +
                "• What symptoms? (fever, limping, swelling…)\n" +
                "• How long has this been happening?\n\n" +
                "Or use 📷 Scan Animal for image diagnosis."
        }

        // Vaccination queries
        val vaccWords = listOf("vaccine", "vaccination", "tika", "टीका", "vaccin")
        if (vaccWords.any { msg.contains(it) }) {
            return "💉 Vaccination Schedule:\n\n" +
                "🐄 Cow / Buffalo:\n" +
                "  • FMD: Every 6 months\n" +
                "  • HS (Hemorrhagic Septicemia): Before monsoon\n" +
                "  • BQ (Black Quarter): Before monsoon\n" +
                "  • Brucellosis: 4-8 months age (once)\n\n" +
                "🐐 Goat:\n" +
                "  • PPR: Every year\n" +
                "  • ET: Every 6 months\n" +
                "  • Goat Pox: Every year\n\n" +
                "🐔 Poultry:\n" +
                "  • Ranikhet: Day 7, 21, and every 4 months\n" +
                "  • Marek's: Day 1\n\n" +
                "Contact your nearest Animal Husbandry office for free vaccines."
        }

        // Feed/nutrition queries
        val feedWords = listOf("feed", "food", "diet", "nutrition", "eat", "khana",
            "चारा", "खाना", "दाना", "ahar")
        if (feedWords.any { msg.contains(it) }) {
            return "🌾 Feeding Guide:\n\n" +
                "🐄 Dairy Cow (daily):\n" +
                "  • Green fodder: 25-30 kg\n" +
                "  • Dry fodder: 5-6 kg\n" +
                "  • Concentrate: 1 kg per 2.5L milk\n" +
                "  • Mineral mix: 30-50g\n" +
                "  • Clean water: 50-60L\n\n" +
                "Tips:\n" +
                "  • Feed at regular times (morning & evening)\n" +
                "  • Add jaggery water for weak animals\n" +
                "  • Never feed wet grain — causes bloat"
        }

        // Breeding queries
        val breedWords = listOf("breed", "pregnant", "calf", "heat", "mating",
            "गर्भ", "बच्चा", "pal", "prajan")
        if (breedWords.any { msg.contains(it) }) {
            return "🐮 Breeding Guide:\n\n" +
                "Heat Signs:\n" +
                "  • Restlessness, mounting others\n" +
                "  • Mucus discharge, swollen vulva\n" +
                "  • Reduced milk, frequent bellowing\n\n" +
                "Best Practice:\n" +
                "  • AI (Artificial Insemination) within 12-18 hrs of heat\n" +
                "  • Pregnancy check at 60-90 days\n" +
                "  • Gestation: Cow = 280 days, Buffalo = 310 days\n" +
                "  • Stop milking 2 months before delivery"
        }

        return null
    }

    private fun readChatbotCsv(ctx: Context): List<Array<String>> {
        return try {
            ctx.assets.open("data/chatbot.csv").use { input ->
                BufferedReader(InputStreamReader(input)).useLines { lines ->
                    lines.filter { it.isNotBlank() }
                        .drop(1)
                        .map { it.split(",").map(String::trim).toTypedArray() }
                        .toList()
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }
}
