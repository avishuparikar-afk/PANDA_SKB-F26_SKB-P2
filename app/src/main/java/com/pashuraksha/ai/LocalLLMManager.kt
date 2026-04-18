package com.pashuraksha.ai

import android.content.Context
import android.util.Log
import de.kherud.llama.InferenceParameters
import de.kherud.llama.LlamaModel
import de.kherud.llama.ModelParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object LocalLLMManager {

    private const val TAG = "LocalLLMManager"
    private var model: LlamaModel? = null
    var isInitialized = false
        private set
    private var nativeLibsLoaded = false

    private fun ensureNativeLibs() {
        if (nativeLibsLoaded) return
        try {
            System.loadLibrary("llama")
            System.loadLibrary("jllama")
            nativeLibsLoaded = true
            Log.d(TAG, "Native llama libraries loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Could not load native libraries", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load native libraries", e)
        }
    }

    suspend fun initModel(context: Context) = withContext(Dispatchers.IO) {
        if (isInitialized && model != null) return@withContext
        ensureNativeLibs()
        if (!nativeLibsLoaded) return@withContext
        try {
            val modelFile = File(context.filesDir, AiEngineManager.MODEL_FILENAME)
            if (!modelFile.exists() || modelFile.length() < 100_000_000L) return@withContext
            val params = ModelParameters()
                .setModelFilePath(modelFile.absolutePath)
                .setNThreads(2)
                .setNCtx(512)
            model = LlamaModel(params)
            isInitialized = true
            Log.d(TAG, "LlamaModel loaded and ready!")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing local LLM", e)
            isInitialized = false
            model = null
        }
    }

    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        if (!isInitialized || model == null) {
            return@withContext "Offline AI is not ready. Please download the model first."
        }
        try {
            val imStart = "<" + "|im_start|" + ">"
            val imEnd = "<" + "|im_end|" + ">"
            val eot = "<" + "|endoftext|" + ">"
            val chatMlPrompt = "${imStart}user\n${prompt}${imEnd}\n${imStart}assistant\n"
            val inferParams = InferenceParameters(chatMlPrompt)
                .setTemperature(0.2f)
                .setNPredict(150)
            val sb = StringBuilder()
            val iterator = model!!.generate(inferParams).iterator()
            while (iterator.hasNext()) {
                val output = iterator.next()
                sb.append(output.text)
                val text = sb.toString()
                if (text.contains(imEnd) || text.contains(eot)) break
            }
            val finalResponse = sb.toString()
                .replace(imEnd, "")
                .replace(eot, "")
                .trim()
            Log.d(TAG, "Inference complete.")
            return@withContext finalResponse
        } catch (e: Exception) {
            Log.e(TAG, "Inference error", e)
            return@withContext "Error generating local AI response: ${e.message}"
        }
    }

    fun close() {
        model?.close()
        model = null
        isInitialized = false
        Log.d(TAG, "LlamaModel closed.")
    }
}
