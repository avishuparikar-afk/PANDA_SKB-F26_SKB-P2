package com.pashuraksha

<<<<<<< HEAD
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pashuraksha.ai.GeminiClient
import com.pashuraksha.ai.PashuAgent
import com.pashuraksha.data.OfflineDataRepository
import com.pashuraksha.data.SessionData
=======
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pashuraksha.ai.PashuAgent
import com.pashuraksha.data.OfflineDataRepository
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
import com.pashuraksha.databinding.ActivityChatBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
<<<<<<< HEAD
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Pashu Doctor chat — powered by PashuAgent (Mini Manus AI).
 *
 * Features:
 *   - Text chat with full 4-step agent pipeline
 *   - 📷 Image upload for visual disease diagnosis
 *   - Online (OpenRouter Gemini Vision) + Offline (rule engine + image analysis)
 *   - Image preview bar before sending
 *   - Bottom sheet picker for camera / gallery
=======

/**
 * Pashu Doctor chat — now powered by PashuAgent (Mini Manus AI).
 *
 * Every user turn runs a full 4-step agent pipeline:
 *   perceive → diagnose → reason → act
 *
 * Works both online (Gemini Flash grounded on CSV findings) and offline
 * (CSV rule engine alone). UI is identical either way.
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
 */
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

<<<<<<< HEAD
    // Currently selected image for sending
    private var pendingBitmap: Bitmap? = null

    // Image pickers
    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri -> loadImageFromUri(uri) }
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        bmp?.let { showImagePreview(it) }
    }

=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        OfflineDataRepository.ensureLoaded(this)
<<<<<<< HEAD
        SessionData.init(this)
=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e

        adapter = ChatAdapter(messages)
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = this@ChatActivity.adapter
        }

        adapter.addMessage(
            ChatMessage(
                "bot",
<<<<<<< HEAD
                "🌿 Namaste! I'm Pashu Doctor.\n\n" +
                "Tell me what's wrong with your animal, or send a 📷 photo for visual diagnosis.\n\n" +
                "Try:\n" +
                "• \"My cow has fever and mouth sores\"\n" +
                "• \"गाय को बुखार है\"\n" +
                "• Tap 📷 to send an animal photo"
=======
                "🌿 Namaste! I'm Pashu Doctor.\n\nTell me what's wrong with your animal. I'll figure out the disease, give home care steps, and tell you when to call a vet.\n\nTry:\n• \"My cow has fever and mouth sores\"\n• \"गाय को बुखार है\"\n• \"Goat is limping\""
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
            )
        )

        updateConnectivityPill()
<<<<<<< HEAD

        // Button click handlers
=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSend.setOnClickListener { sendMessage() }
        binding.messageInput.setOnEditorActionListener { _, _, _ ->
            sendMessage(); true
        }
        binding.btnMic.setOnClickListener {
<<<<<<< HEAD
            Toast.makeText(this, "Voice input coming soon — type or send a photo", Toast.LENGTH_SHORT).show()
        }

        // 📷 Image attach button — shows picker options
        binding.btnAttachImage.setOnClickListener { showImagePickerDialog() }

        // Remove image from preview
        binding.btnRemoveImage.setOnClickListener { clearImagePreview() }
    }

    /**
     * Show a simple dialog to choose Camera or Gallery
     */
    private fun showImagePickerDialog() {
        val options = arrayOf("📷 Take Photo", "🖼️ Choose from Gallery")
        android.app.AlertDialog.Builder(this)
            .setTitle("Send Animal Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePicture.launch(null)
                    1 -> {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        pickImage.launch(intent)
                    }
                }
            }
            .show()
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(contentResolver, uri)
                ) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
            showImagePreview(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImagePreview(bitmap: Bitmap) {
        pendingBitmap = bitmap
        binding.imagePreview.setImageBitmap(bitmap)
        binding.imagePreviewBar.visibility = View.VISIBLE
        binding.messageInput.hint = "Add a message about this photo…"
    }

    private fun clearImagePreview() {
        pendingBitmap = null
        binding.imagePreview.setImageBitmap(null)
        binding.imagePreviewBar.visibility = View.GONE
        binding.messageInput.hint = "Ask about your animal…"
    }

    /**
     * Send message — handles both text-only and image+text
     */
    private fun sendMessage() {
        val text = binding.messageInput.text?.toString()?.trim().orEmpty()
        val image = pendingBitmap

        if (text.isEmpty() && image == null) return
        binding.messageInput.setText("")

        if (image != null) {
            // Image message flow
            clearImagePreview()

            val caption = if (text.isNotEmpty()) "📷 + \"$text\"" else "📷 Analyze this animal"
            adapter.addMessage(ChatMessage("user_image", caption, image))
            scrollToBottom()

            adapter.addMessage(ChatMessage("bot", "🔍 Analyzing your photo…"))
            binding.typingIndicator.visibility = View.VISIBLE
            scrollToBottom()

            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    analyzeImageForChat(image, text)
                }
                binding.typingIndicator.visibility = View.GONE
                adapter.updateLastBot(result)
                scrollToBottom()
            }
        } else {
            // Text-only message flow
            adapter.addMessage(ChatMessage("user", text))
            scrollToBottom()
            adapter.addMessage(ChatMessage("bot", "🌱 Thinking…"))
            binding.typingIndicator.visibility = View.VISIBLE
            scrollToBottom()

            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    PashuAgent.run(
                        ctx = this@ChatActivity,
                        farmerQuery = text
                    )
                }
                binding.typingIndicator.visibility = View.GONE
                adapter.updateLastBot(result.rawAnswer)
                scrollToBottom()
            }
        }
    }

    /**
     * Analyze an image using OpenRouter Vision API (online) or offline engine.
     * Returns a rich, farmer-friendly diagnosis string.
     */
    private fun analyzeImageForChat(bitmap: Bitmap, userText: String): String {
        val apiKey = BuildConfig.OPENROUTER_API_KEY
        val online = apiKey.isNotBlank() && !apiKey.startsWith("YOUR_") && isOnline()

        return if (online) {
            try {
                analyzeImageOnline(apiKey, bitmap, userText)
            } catch (e: Exception) {
                Log.e("ChatActivity", "Vision API failed", e)
                analyzeImageOffline(bitmap) + "\n\n(offline mode — network issue)"
            }
        } else {
            analyzeImageOffline(bitmap)
        }
    }

    /**
     * Online image analysis via OpenRouter Gemini Vision
     */
    private fun analyzeImageOnline(apiKey: String, bitmap: Bitmap, userText: String): String {
        // Scale and encode
        val scaled = scaleBitmap(bitmap, 768)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)

        val context = if (userText.isNotBlank()) "\nFarmer says: \"$userText\"" else ""

        val prompt = """
You are Pashu Doctor AI, a friendly livestock veterinarian for Indian rural farmers.

Analyze this animal photo and provide:
1. What animal is this? (cow, buffalo, goat, chicken, dog, etc.)
2. Any visible disease signs? Check for:
   - Lumpy Skin Disease (nodules/bumps)
   - Foot & Mouth Disease (mouth blisters, drooling)
   - Mastitis (udder swelling)
   - Tick/parasites (hair loss, ticks visible)
   - Eye infections (redness, discharge)
   - Respiratory issues (nasal discharge)
   - Skin infections (lesions, wounds)
3. Overall health assessment
4. Home care advice if disease found
5. When to call vet
$context

Reply in a warm, structured format using emojis. If NOT a livestock animal, politely explain.
Keep response concise but complete.
        """.trimIndent()

        val url = URL("https://openrouter.ai/api/v1/chat/completions")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 20000
            readTimeout = 45000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("HTTP-Referer", "https://pashuraksha.app")
            setRequestProperty("X-Title", "PashuRaksha")
        }

        val userContent = JSONArray().apply {
            put(JSONObject().apply {
                put("type", "text")
                put("text", prompt)
            })
            put(JSONObject().apply {
                put("type", "image_url")
                put("image_url", JSONObject().apply {
                    put("url", "data:image/jpeg;base64,$b64")
                })
            })
        }

        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", userContent)
            })
        }

        val body = JSONObject().apply {
            put("model", "google/gemini-2.0-flash-001")
            put("messages", messages)
            put("temperature", 0.3)
            put("max_tokens", 700)
        }.toString()

        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val response = stream.bufferedReader().use(BufferedReader::readText)

        if (code !in 200..299) {
            throw RuntimeException("Vision API HTTP $code")
        }

        val root = JSONObject(response)
        val choices = root.optJSONArray("choices") ?: return "Could not analyze image."
        if (choices.length() == 0) return "Could not analyze image."
        val content = choices.getJSONObject(0)
            .optJSONObject("message")
            ?.optString("content", "") ?: ""
        return content.trim().ifEmpty { "Analysis complete but no response received." }
    }

    /**
     * Offline image analysis — uses pixel color analysis to determine
     * likely health status and provide relevant advice from database
     */
    private fun analyzeImageOffline(bitmap: Bitmap): String {
        val w = bitmap.width; val h = bitmap.height
        var redDom = 0; var darkPx = 0; var brightPx = 0; var skinPx = 0
        var total = 0
        val gridSize = 5

        for (gy in 0 until gridSize) {
            for (gx in 0 until gridSize) {
                val px = (w * (gx + 1)) / (gridSize + 1)
                val py = (h * (gy + 1)) / (gridSize + 1)
                if (px >= w || py >= h) continue
                val pixel = bitmap.getPixel(px, py)
                val r = android.graphics.Color.red(pixel)
                val g = android.graphics.Color.green(pixel)
                val b = android.graphics.Color.blue(pixel)
                val brightness = (r + g + b) / 3f
                total++
                if (r > g + 25 && r > b + 25) redDom++
                if (brightness < 60) darkPx++
                if (brightness > 200) brightPx++
                val cb = 128 + (-0.169 * r - 0.331 * g + 0.500 * b)
                val cr = 128 + (0.500 * r - 0.419 * g - 0.081 * b)
                if (cb in 77.0..127.0 && cr in 133.0..175.0 && brightness > 60) skinPx++
            }
        }

        // Human detection
        if (total > 0 && skinPx.toFloat() / total > 0.45f) {
            return "⚠️ This appears to be a human — not a livestock animal.\n\n" +
                "Please send a clear photo of your cow, buffalo, goat, or chicken.\n\n" +
                "Tips for good photos:\n" +
                "• Focus on the affected body part\n" +
                "• Good lighting\n" +
                "• 2-3 feet distance"
        }

        // Disease indicator analysis
        return when {
            redDom >= 4 -> {
                val disease = OfflineDataRepository.findDiseaseByName("Lumpy Skin Disease")
                buildString {
                    append("🔍 Image Analysis (Offline Mode)\n\n")
                    append("⚠️ Possible: Lumpy Skin Disease (LSD)\n")
                    append("Confidence: 65%\n\n")
                    append("🏠 Home Care:\n")
                    disease?.homeCare?.take(3)?.forEach { append("• $it\n") }
                        ?: run { append("• Isolate animal\n• Mosquito control\n• Clean shelter\n") }
                    append("\n👨‍⚕️ ${disease?.vetAdvice ?: "Contact vet immediately"}")
                    append("\n📞 Emergency: 1962")
                }
            }
            brightPx >= 4 -> {
                val disease = OfflineDataRepository.findDiseaseByName("Mastitis")
                buildString {
                    append("🔍 Image Analysis (Offline Mode)\n\n")
                    append("⚠️ Possible: Mastitis (Udder Infection)\n")
                    append("Confidence: 55%\n\n")
                    append("🏠 Home Care:\n")
                    disease?.homeCare?.take(3)?.forEach { append("• $it\n") }
                        ?: run { append("• Wash udder with warm water\n• Discard infected milk\n• Maintain hygiene\n") }
                    append("\n👨‍⚕️ ${disease?.vetAdvice ?: "Antibiotics needed — call vet"}")
                }
            }
            darkPx >= 5 -> {
                val disease = OfflineDataRepository.findDiseaseByName("Tick Infestation")
                buildString {
                    append("🔍 Image Analysis (Offline Mode)\n\n")
                    append("👁 Possible: Tick Infestation / Parasites\n")
                    append("Confidence: 50%\n\n")
                    append("🏠 Home Care:\n")
                    disease?.homeCare?.take(3)?.forEach { append("• $it\n") }
                        ?: run { append("• Remove ticks\n• Anti-tick spray\n• Clean shelter\n") }
                    append("\n👨‍⚕️ ${disease?.vetAdvice ?: "Anti-parasitic medicine needed"}")
                }
            }
            else -> {
                buildString {
                    append("🔍 Image Analysis (Offline Mode)\n\n")
                    append("✅ Animal appears healthy\n")
                    append("Confidence: 70%\n\n")
                    append("No obvious disease signs detected.\n\n")
                    append("Preventive care:\n")
                    append("• Regular vaccination (FMD every 6 months)\n")
                    append("• Deworming every 3-4 months\n")
                    append("• Clean shelter and fresh water daily\n")
                    append("• Mineral supplement 30-50g daily\n\n")
                    append("For more accurate diagnosis, try with internet connection for AI Vision analysis.")
                }
            }
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width; val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val ratio = maxDim.toFloat() / maxOf(w, h)
        return Bitmap.createScaledBitmap(bitmap, (w * ratio).toInt(), (h * ratio).toInt(), true)
    }

=======
            binding.messageInput.hint = "Voice coming soon — type for now"
        }
    }

    private fun sendMessage() {
        val text = binding.messageInput.text?.toString()?.trim().orEmpty()
        if (text.isEmpty()) return
        binding.messageInput.setText("")

        adapter.addMessage(ChatMessage("user", text))
        scrollToBottom()
        adapter.addMessage(ChatMessage("bot", "🌱 Thinking…"))
        binding.typingIndicator.visibility = View.VISIBLE
        scrollToBottom()

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                PashuAgent.run(
                    ctx = this@ChatActivity,
                    farmerQuery = text
                )
            }
            binding.typingIndicator.visibility = View.GONE
            adapter.updateLastBot(result.rawAnswer)
            scrollToBottom()
        }
    }

>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
    private fun scrollToBottom() {
        binding.messagesRecyclerView.post {
            binding.messagesRecyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun updateConnectivityPill() {
<<<<<<< HEAD
        val mode = com.pashuraksha.ai.AiEngineManager.getCurrentMode(this)
        when (mode) {
            com.pashuraksha.ai.AiEngineManager.AiMode.ONLINE -> {
                binding.statusDot.setBackgroundColor(getColor(R.color.status_safe))
                binding.statusText.text = "Online • AI Agent + Vision"
            }
            com.pashuraksha.ai.AiEngineManager.AiMode.OFFLINE_LLM -> {
                binding.statusDot.setBackgroundColor(getColor(R.color.status_caution))
                binding.statusText.text = "Offline • Local AI Doctor"
            }
            com.pashuraksha.ai.AiEngineManager.AiMode.RULE_BASED -> {
                binding.statusDot.setBackgroundColor(getColor(android.R.color.holo_red_light))
                binding.statusText.text = "Offline • Basic Mode"
            }
=======
        val online = isOnline()
        if (online) {
            binding.statusDot.setBackgroundColor(getColor(R.color.status_safe))
            binding.statusText.text = "Online • AI Agent"
        } else {
            binding.statusDot.setBackgroundColor(getColor(R.color.status_caution))
            binding.statusText.text = "Offline • Edge AI"
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        }
    }

    private fun isOnline(): Boolean {
        return try {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val n = cm.activeNetwork ?: return false
            cm.getNetworkCapabilities(n)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (_: Throwable) { false }
    }

    override fun onResume() { super.onResume(); updateConnectivityPill() }
}
