package com.pashuraksha.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import java.io.File

/**
 * AiEngineManager — The intelligent routing brain of PashuRaksha.
 *
 * Implements a 3-tier fallback architecture:
 *   Tier 1 (Online):  OpenRouter API (Gemini) — best quality
 *   Tier 2 (Offline): Local GGUF model (llama.cpp) — good quality, no internet
 *   Tier 3 (Fallback): Rule-based engine from CSV — always works, zero crashes
 *
 * The app NEVER crashes. Every tier has try-catch protection.
 */
object AiEngineManager {

    private const val TAG = "AiEngineManager"

    // The model file name stored in context.filesDir
    const val MODEL_FILENAME = "model.gguf"

    // Configurable download URL — change this to your actual hosted URL
    const val MODEL_DOWNLOAD_URL = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q2_k.gguf"

    enum class AiMode {
        ONLINE,         // 🟢 OpenRouter API
        OFFLINE_LLM,    // 🟡 Local GGUF model
        RULE_BASED      // 🔴 CSV rule engine fallback
    }

    /**
     * Determines the current best available AI mode.
     */
    fun getCurrentMode(context: Context): AiMode {
        return when {
            isOnline(context) -> AiMode.ONLINE
            isOfflineModelAvailable(context) -> AiMode.OFFLINE_LLM
            else -> AiMode.RULE_BASED
        }
    }

    /**
     * Returns a human-readable status string + emoji for the UI.
     */
    fun getStatusText(context: Context): Pair<String, String> {
        return when (getCurrentMode(context)) {
            AiMode.ONLINE -> Pair("🟢", "Online AI Active")
            AiMode.OFFLINE_LLM -> Pair("🟡", "Offline AI Ready")
            AiMode.RULE_BASED -> Pair("🔴", "Basic Mode • No AI")
        }
    }

    /**
     * Main entry point: generate a response using the best available engine.
     * NEVER throws — always returns a usable string.
     */
    suspend fun generateResponse(
        context: Context,
        userMessage: String,
        history: List<Pair<String, String>> = emptyList()
    ): String {
        val mode = getCurrentMode(context)
        Log.d(TAG, "AI Mode: $mode for query: ${userMessage.take(50)}...")

        return when (mode) {
            AiMode.ONLINE -> {
                try {
                    val result = GeminiClient.ask(context, userMessage, history)
                    if (result.isNotBlank()) result
                    else fallbackToOffline(context, userMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Online AI failed, falling back", e)
                    fallbackToOffline(context, userMessage)
                }
            }
            AiMode.OFFLINE_LLM -> {
                try {
                    generateOfflineLLM(context, userMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Offline LLM failed, using rule-based", e)
                    generateRuleBased(context, userMessage)
                }
            }
            AiMode.RULE_BASED -> {
                generateRuleBased(context, userMessage)
            }
        }
    }

    /**
     * Fallback chain: try offline LLM first, then rule-based.
     */
    private suspend fun fallbackToOffline(context: Context, userMessage: String): String {
        return if (isOfflineModelAvailable(context)) {
            try {
                val result = generateOfflineLLM(context, userMessage)
                "$result\n\n(offline mode — network issue)"
            } catch (e: Exception) {
                Log.e(TAG, "Offline LLM also failed", e)
                val rb = generateRuleBased(context, userMessage)
                "$rb\n\n(offline mode — check internet for AI answers)"
            }
        } else {
            val rb = generateRuleBased(context, userMessage)
            "$rb\n\n(offline mode — check internet for AI answers)"
        }
    }

    /**
     * Tier 2: Local GGUF model inference.
     */
    private suspend fun generateOfflineLLM(context: Context, userMessage: String): String {
        LocalLLMManager.initModel(context)

        if (!LocalLLMManager.isInitialized) {
            throw RuntimeException("Model failed to initialize")
        }

        val prompt = """
        You are Pashu Doctor, a friendly livestock veterinarian for Indian rural farmers.
        Farmer says: "$userMessage"
        Respond in a helpful, warm, and structured manner with emojis.
        Keep it concise. Include home care steps and when to call vet.
        """.trimIndent()

        return LocalLLMManager.generateResponse(prompt)
    }

    /**
     * Tier 3: Rule-based engine — ALWAYS works, NEVER crashes.
     */
    private fun generateRuleBased(context: Context, userMessage: String): String {
        return try {
            GeminiClient.ask(context, userMessage, emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Even rule-based had issue", e)
            "I need more details to help. Try describing symptoms like:\n\n" +
            "• \"My cow has fever and not eating\"\n" +
            "• \"Buffalo has swollen udder\"\n" +
            "• \"Goat is limping and has mouth sores\"\n\n" +
            "Or tap 📷 Scan Animal for image-based diagnosis."
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Utility functions
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    /**
     * Checks if the offline GGUF model file exists in filesDir.
     */
    fun isOfflineModelAvailable(context: Context): Boolean {
        val modelFile = File(context.filesDir, MODEL_FILENAME)
        // Must exist and be reasonably large (>100MB for a real model)
        return modelFile.exists() && modelFile.length() > 100_000_000L
    }

    /**
     * Returns model file size in MB, or 0 if not present.
     */
    fun getModelSizeMB(context: Context): Long {
        val modelFile = File(context.filesDir, MODEL_FILENAME)
        return if (modelFile.exists()) modelFile.length() / (1024 * 1024) else 0
    }

    /**
     * Deletes the downloaded model to free storage.
     */
    fun deleteModel(context: Context): Boolean {
        val modelFile = File(context.filesDir, MODEL_FILENAME)
        LocalLLMManager.close()
        return modelFile.delete()
    }

    /**
     * Checks network connectivity.
     */
    fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager
            val n = cm.activeNetwork ?: return false
            cm.getNetworkCapabilities(n)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (_: Throwable) { false }
    }
}
