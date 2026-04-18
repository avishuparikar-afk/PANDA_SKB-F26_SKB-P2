package com.pashuraksha

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
<<<<<<< HEAD
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.pashuraksha.ai.AiEngineManager
import com.pashuraksha.ai.ModelDownloadManager
import com.pashuraksha.data.OfflineDataRepository
import com.pashuraksha.data.SessionData
import com.pashuraksha.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Redesigned home with AI Status indicator and Offline AI download.
=======
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pashuraksha.data.OfflineDataRepository
import com.pashuraksha.databinding.FragmentHomeBinding

/**
 * Redesigned home — premium rural-tech aesthetic.
 * Hero card: Scan Animal. Quick actions grid. Pashupatinath featured row.
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()
<<<<<<< HEAD
    private var isDownloading = false
=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

<<<<<<< HEAD
        SessionData.init(requireContext())
        OfflineDataRepository.ensureLoaded(requireContext())

        // Observe ViewModel data
=======
        // Preload CSV datasets on first landing
        OfflineDataRepository.ensureLoaded(requireContext())

        // Observe ViewModel data (only fields that still exist in new layout)
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        homeViewModel.greeting.observe(viewLifecycleOwner) {
            binding.greetingTextView.text = it
        }
        homeViewModel.villageName.observe(viewLifecycleOwner) {
            binding.villageNameTextView.text = it
        }
        homeViewModel.weather.observe(viewLifecycleOwner) {
            binding.weatherTextView.text = "☀️  ${it ?: "25°C"}"
        }

        // Hero + quick-action click routing
        binding.btnStartScan.setOnClickListener {
            safeNavigate(R.id.scanFragment)
        }
        binding.btnImmunityCalculator.setOnClickListener {
            safeNavigate(R.id.immunityFragment)
        }
        binding.btnOutbreakMap.setOnClickListener {
            safeNavigate(R.id.mapFragment)
        }
        binding.btnHealthReport.setOnClickListener {
            safeNavigate(R.id.reportFragment)
        }

<<<<<<< HEAD
        // Chatbot
=======
        // Chatbot — deep link to disease detection (chat UI added via MainActivity intent)
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        binding.btnChatbot.setOnClickListener {
            val intent = Intent(requireContext(), ChatActivity::class.java)
            startActivity(intent)
        }

<<<<<<< HEAD
        // Download Offline AI button
        binding.btnDownloadModel.setOnClickListener {
            startModelDownload()
        }

        // Update AI status
        updateAiStatus()
    }

    override fun onResume() {
        super.onResume()
        updateAiStatus()
    }

    /**
     * Updates the AI status indicator and download card visibility.
     */
    private fun updateAiStatus() {
        val ctx = context ?: return
        val mode = AiEngineManager.getCurrentMode(ctx)
        val (emoji, text) = AiEngineManager.getStatusText(ctx)

        binding.aiStatusEmoji.text = emoji
        binding.aiStatusText.text = text

        when (mode) {
            AiEngineManager.AiMode.ONLINE -> {
                binding.aiStatusDetail.text = "Powered by Gemini AI via OpenRouter"
                // Show download card if model not yet downloaded
                binding.downloadAiCard.visibility =
                    if (AiEngineManager.isOfflineModelAvailable(ctx)) View.GONE else View.VISIBLE
                if (AiEngineManager.isOfflineModelAvailable(ctx)) {
                    binding.downloadAiCard.visibility = View.GONE
                } else {
                    binding.downloadAiCard.visibility = View.VISIBLE
                    binding.btnDownloadModel.text = "Download Offline AI"
                    binding.btnDownloadModel.isEnabled = true
                }
            }
            AiEngineManager.AiMode.OFFLINE_LLM -> {
                val sizeMb = AiEngineManager.getModelSizeMB(ctx)
                binding.aiStatusDetail.text = "Local AI Doctor (${sizeMb}MB)"
                binding.downloadAiCard.visibility = View.GONE
            }
            AiEngineManager.AiMode.RULE_BASED -> {
                binding.aiStatusDetail.text = "Download AI for smarter diagnosis"
                binding.downloadAiCard.visibility = View.VISIBLE
                binding.btnDownloadModel.text = "Download Offline AI"
                binding.btnDownloadModel.isEnabled = true
            }
        }
    }

    /**
     * Starts the model download with progress tracking.
     */
    private fun startModelDownload() {
        if (isDownloading) return
        val ctx = context ?: return

        if (!AiEngineManager.isOnline(ctx)) {
            Toast.makeText(ctx, "Need internet to download. Connect to Wi-Fi first.", Toast.LENGTH_LONG).show()
            return
        }

        isDownloading = true
        binding.btnDownloadModel.isEnabled = false
        binding.btnDownloadModel.text = "Downloading..."
        binding.downloadProgressBar.visibility = View.VISIBLE
        binding.downloadProgressText.visibility = View.VISIBLE
        binding.downloadProgressBar.progress = 0

        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                ModelDownloadManager.downloadModel(
                    context = ctx,
                    onProgress = { progress ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            binding.downloadProgressBar.progress = progress.percent
                            val downloadedMB = progress.bytesDownloaded / (1024 * 1024)
                            val totalMB = progress.totalBytes / (1024 * 1024)
                            binding.downloadProgressText.text =
                                "${downloadedMB}MB / ${totalMB}MB (${progress.percent}%)"
                        }
                    }
                )
            }

            isDownloading = false

            if (success) {
                Toast.makeText(ctx, "Offline AI Ready! You can now use AI without internet.", Toast.LENGTH_LONG).show()
                binding.downloadProgressBar.visibility = View.GONE
                binding.downloadProgressText.visibility = View.GONE
                updateAiStatus()
            } else {
                Toast.makeText(ctx, "Download failed. Please try again with stable internet.", Toast.LENGTH_LONG).show()
                binding.btnDownloadModel.isEnabled = true
                binding.btnDownloadModel.text = "Retry Download"
                binding.downloadProgressBar.visibility = View.GONE
                binding.downloadProgressText.visibility = View.GONE
            }
=======
        // Pashupatinath Mode - the wow moment
        binding.btnPashupatinathMode.setOnClickListener {
            val intent = Intent(requireContext(), CosmicEnergyActivity::class.java)
            startActivity(intent)
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        }
    }

    private fun safeNavigate(destId: Int) {
        try {
            findNavController().navigate(destId)
        } catch (_: Throwable) { /* silently ignore if not in graph yet */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
