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
<<<<<<< HEAD
import com.pashuraksha.data.SessionData
=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
import com.pashuraksha.databinding.FragmentSettingsBinding
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

<<<<<<< HEAD
    // Guard against spinner triggering recreate on initial setup
    private var isInitialSetup = true

=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
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

<<<<<<< HEAD
        SessionData.init(requireContext())

=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        val languages = arrayOf("English", "Hindi", "Marathi", "Telugu", "Punjabi")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, languages)
        binding.languageSpinner.adapter = adapter

<<<<<<< HEAD
        // Restore saved language selection
        val savedLang = SessionData.getLanguage()
        val savedLangName = when (savedLang) {
=======
        // Set current language selection
        val currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else {
            resources.configuration.locale
        }
        val currentLang = when (currentLocale.language) {
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
            "hi" -> "Hindi"
            "mr" -> "Marathi"
            "te" -> "Telugu"
            "pa" -> "Punjabi"
            else -> "English"
        }
<<<<<<< HEAD
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
=======
        binding.languageSpinner.setSelection(languages.indexOf(currentLang))

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = languages[position]
                val locale = when (selectedLanguage) {
                    "Hindi" -> Locale("hi")
                    "Marathi" -> Locale("mr")
                    "Telugu" -> Locale("te")
                    "Punjabi" -> Locale("pa")
                    else -> Locale("en")
                }
                setLocale(requireContext(), locale)
                activity?.recreate() // Recreate activity to apply language change
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
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
