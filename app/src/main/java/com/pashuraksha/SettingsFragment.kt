package com.pashuraksha

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.pashuraksha.data.SessionData
import com.pashuraksha.databinding.FragmentSettingsBinding
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    // Guard against spinner triggering recreate on initial setup
    private var isInitialSetup = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SessionData.init(requireContext())

        val languages = arrayOf("English", "Hindi", "Marathi", "Telugu", "Punjabi")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, languages)
        binding.languageSpinner.adapter = adapter

        // Restore saved language selection
        val savedLang = SessionData.getLanguage()
        val savedLangName = when (savedLang) {
            "hi" -> "Hindi"
            "mr" -> "Marathi"
            "te" -> "Telugu"
            "pa" -> "Punjabi"
            else -> "English"
        }
        val savedIndex = languages.indexOf(savedLangName)
        if (savedIndex >= 0) {
            binding.languageSpinner.setSelection(savedIndex)
        }

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Skip the initial programmatic selection to prevent infinite recreate loop
                if (isInitialSetup) {
                    isInitialSetup = false
                    return
                }

                val selectedLanguage = languages[position]
                val langCode = when (selectedLanguage) {
                    "Hindi" -> "hi"
                    "Marathi" -> "mr"
                    "Telugu" -> "te"
                    "Punjabi" -> "pa"
                    else -> "en"
                }

                // Only recreate if language actually changed
                if (langCode != SessionData.getLanguage()) {
                    SessionData.saveLanguage(langCode)
                    val locale = Locale(langCode)
                    setLocale(requireContext(), locale)
                    activity?.recreate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setLocale(context: Context, locale: Locale) {
        val configuration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
        } else {
            configuration.locale = locale
        }
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
