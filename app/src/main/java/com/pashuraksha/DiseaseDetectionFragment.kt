package com.pashuraksha

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
<<<<<<< HEAD
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.speech.tts.TextToSpeech
import java.util.Locale
import com.pashuraksha.data.SessionData
=======
import android.speech.tts.TextToSpeech
import java.util.Locale
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
import com.pashuraksha.databinding.FragmentDiseaseDetectionBinding

class DiseaseDetectionFragment : Fragment() {

    private var _binding: FragmentDiseaseDetectionBinding? = null
    private val binding get() = _binding!!
    private val diseaseDetectionViewModel: DiseaseDetectionViewModel by viewModels()

    private var selectedImageUri: Uri? = null
    private lateinit var textToSpeech: TextToSpeech

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            selectedImageUri = it.data?.data
            selectedImageUri?.let { uri ->
                binding.imageView.setImageURI(uri)
            }
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        it?.let {
            val uri = getImageUri(it)
            selectedImageUri = uri
            binding.imageView.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiseaseDetectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

<<<<<<< HEAD
        SessionData.init(requireContext())

=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        textToSpeech = TextToSpeech(requireContext()) {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.getDefault()
            }
        }

        binding.captureImageButton.setOnClickListener { openCamera() }
        binding.uploadImageButton.setOnClickListener { openGallery() }
        binding.analyzeImageButton.setOnClickListener { analyzeImage() }
        binding.findVetButton.setOnClickListener { findNearestVet() }
        binding.speakResultButton.setOnClickListener { speakDiagnosisResult() }
<<<<<<< HEAD
        binding.aiAdviceButton.setOnClickListener { getLocalAiAdvice() }
=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e

        observeViewModel()
    }

    private fun openCamera() {
        takePicture.launch(null)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun analyzeImage() {
        selectedImageUri?.let {
            binding.progressBar.visibility = View.VISIBLE
            binding.diagnosticCard.visibility = View.GONE
<<<<<<< HEAD
            // Use software rendering to avoid hardware bitmap limitations
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(requireContext().contentResolver, it)
                ) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
            }
            diseaseDetectionViewModel.analyzeImageWithGemini(bitmap, requireContext())
=======
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, it))
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
            }
            diseaseDetectionViewModel.analyzeImageWithGemini(bitmap)
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        } ?: run {
            Toast.makeText(requireContext(), "Please select or capture an image first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findNearestVet() {
        val gmmIntentUri = Uri.parse("geo:0,0?q=veterinarian near me")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(requireContext(), "Google Maps app not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        diseaseDetectionViewModel.diagnosisResult.observe(viewLifecycleOwner) {
            it?.let {
                binding.progressBar.visibility = View.GONE
                binding.diagnosticCard.visibility = View.VISIBLE
                binding.diseaseTextView.text = "Disease: ${it.disease}"
                binding.confidenceTextView.text = "Confidence: ${it.confidence}%"
                binding.confidenceProgressBar.progress = it.confidence
                binding.symptomsTextView.text = "Symptoms: ${it.symptomsVisible}"
                binding.recommendationsTextView.text = "Recommendations: ${it.recommendations}"
                binding.urgencyTextView.text = "Urgency Level: ${it.urgencyLevel}"
                binding.speakResultButton.visibility = View.VISIBLE
<<<<<<< HEAD

                // Persist diagnosis result to SessionData for ReportFragment
                SessionData.saveDiagnosisResult(
                    disease = it.disease,
                    confidence = it.confidence,
                    symptoms = it.symptomsVisible,
                    recommendations = it.recommendations,
                    urgency = it.urgencyLevel
                )
=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
            }
        }

        diseaseDetectionViewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
<<<<<<< HEAD
                binding.recommendationsTextView.text = "AI Advice: $it" // show in UI instead of just toast
=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
            }
        }
    }

<<<<<<< HEAD
    private fun getLocalAiAdvice() {
        val currentDiag = diseaseDetectionViewModel.diagnosisResult.value
        if (currentDiag == null) {
            Toast.makeText(requireContext(), "Please analyze an image first", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        binding.recommendationsTextView.text = "Recommendations: Warming up Neural Engine..."
        
        val prompt = "A farmer's cow has symptoms like ${currentDiag.symptomsVisible}. I suspect ${currentDiag.disease}. Tell me exactly what to do."
        diseaseDetectionViewModel.getAiAdviceOffline(requireContext(), prompt)
    }

=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
    private fun getImageUri(bitmap: Bitmap): Uri {
        val path = MediaStore.Images.Media.insertImage(requireContext().contentResolver, bitmap, "PASHU_RAKSHA_IMG", null)
        return Uri.parse(path)
    }

    private fun speakDiagnosisResult() {
        val diagnosisText = "Disease: ${diseaseDetectionViewModel.diagnosisResult.value?.disease}. " +
                "Confidence: ${diseaseDetectionViewModel.diagnosisResult.value?.confidence} percent. " +
                "Symptoms: ${diseaseDetectionViewModel.diagnosisResult.value?.symptomsVisible}. " +
                "Recommendations: ${diseaseDetectionViewModel.diagnosisResult.value?.recommendations}. " +
                "Urgency Level: ${diseaseDetectionViewModel.diagnosisResult.value?.urgencyLevel}."
        textToSpeech.speak(diagnosisText, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroyView() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroyView()
        _binding = null
    }
}
