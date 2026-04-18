package com.pashuraksha

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
<<<<<<< HEAD
import com.pashuraksha.data.SessionData
=======
import com.github.mikephil.charting.utils.ColorTemplate
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
import com.pashuraksha.databinding.FragmentImmunityBinding

class ImmunityFragment : Fragment() {

    private var _binding: FragmentImmunityBinding? = null
    private val binding get() = _binding!!
<<<<<<< HEAD
    private val vm: ImmunityViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentImmunityBinding.inflate(i, c, false)
=======
    private val immunityViewModel: ImmunityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImmunityBinding.inflate(inflater, container, false)
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

<<<<<<< HEAD
        SessionData.init(requireContext())

        val districts = listOf(
            "Pune","Nashik","Nagpur","Mumbai","Aurangabad",
            "Solapur","Kolhapur","Amravati","Latur","Akola",
            "Jalgaon","Satara","Sangli","Raigad","Yavatmal","Wardha"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, districts)
        binding.districtAutoCompleteTextView.setAdapter(adapter)

        binding.calculateButton.setOnClickListener { calculateImmunityGap() }
=======
        // Setup District Dropdown
        val districts = arrayOf("Pune", "Nashik", "Nagpur", "Mumbai", "Aurangabad") // Example districts
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, districts)
        binding.districtAutoCompleteTextView.setAdapter(adapter)

        binding.calculateButton.setOnClickListener {
            calculateImmunityGap()
        }

>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        observeViewModel()
    }

    private fun calculateImmunityGap() {
<<<<<<< HEAD
        val village = binding.villageNameEditText.text.toString().trim()
        val district = binding.districtAutoCompleteTextView.text.toString().trim()
        val totalStr = binding.totalCattleEditText.text.toString().trim()

        if (village.isEmpty() || district.isEmpty() || totalStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        val total = totalStr.toIntOrNull()
        if (total == null || total <= 0) {
            Toast.makeText(requireContext(), "Enter a valid cattle count", Toast.LENGTH_SHORT).show()
            return
        }
        val type = when (binding.cattleTypeRadioGroup.checkedRadioButtonId) {
            R.id.radioCow     -> "Cow"
            R.id.radioBuffalo -> "Buffalo"
            R.id.radioMixed   -> "Mixed"
            else -> {
                Toast.makeText(requireContext(), "Select cattle type", Toast.LENGTH_SHORT).show()
                return
            }
        }
        vm.calculateImmunityGap(village, district, total, type)
    }

    private fun observeViewModel() {
        vm.immunityGapPercentage.observe(viewLifecycleOwner) { gap ->
            if (gap != null) {
                binding.resultLayout.visibility = View.VISIBLE
                updatePieChart(gap)
                updateRiskAssessment(gap)

                // Persist immunity result to SessionData for ReportFragment
                val village = binding.villageNameEditText.text.toString().trim()
                val district = binding.districtAutoCompleteTextView.text.toString().trim()
                val total = binding.totalCattleEditText.text.toString().toIntOrNull() ?: 0
                val vaccinated = vm.vaccinatedCount.value ?: 0
                val days = vm.outbreakDays.value ?: 60
                SessionData.saveImmunityResult(village, district, total, vaccinated, gap, days)
=======
        val villageName = binding.villageNameEditText.text.toString()
        val district = binding.districtAutoCompleteTextView.text.toString()
        val totalCattleStr = binding.totalCattleEditText.text.toString()

        if (villageName.isEmpty() || district.isEmpty() || totalCattleStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val totalCattle = totalCattleStr.toIntOrNull()
        if (totalCattle == null || totalCattle <= 0) {
            Toast.makeText(requireContext(), "Please enter a valid total cattle count", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCattleType = when (binding.cattleTypeRadioGroup.checkedRadioButtonId) {
            R.id.radioCow -> "Cow"
            R.id.radioBuffalo -> "Buffalo"
            R.id.radioMixed -> "Mixed"
            else -> {
                Toast.makeText(requireContext(), "Please select a cattle type", Toast.LENGTH_SHORT).show()
                return
            }
        }

        immunityViewModel.calculateImmunityGap(villageName, district, totalCattle, selectedCattleType)
    }

    private fun observeViewModel() {
        immunityViewModel.immunityGapPercentage.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.resultLayout.visibility = View.VISIBLE
                updatePieChart(it)
                updateRiskAssessment(it)
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
            } else {
                binding.resultLayout.visibility = View.GONE
            }
        }

<<<<<<< HEAD
        vm.vaccinatedCount.observe(viewLifecycleOwner) { vaccinated ->
            val total = binding.totalCattleEditText.text.toString().toIntOrNull() ?: 0
            if (total > 0) {
                binding.riskLevelTextView.text =
                    "Vaccinated: $vaccinated / $total animals"
            }
        }

        vm.outbreakDays.observe(viewLifecycleOwner) { days ->
            binding.outbreakProbabilityTextView.text =
                "⚠️ Estimated outbreak risk window: within $days days"
        }

        vm.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
        }
    }

    private fun updatePieChart(gap: Float) {
        val entries = arrayListOf(PieEntry(gap, "Gap"), PieEntry(100f - gap, "Immune"))
        val dataSet = PieDataSet(entries, "Immunity Gap").apply {
            colors = listOf(
                resources.getColor(R.color.sacred_orange, null),
                resources.getColor(R.color.bioluminescent_green, null)
            )
            valueTextColor = resources.getColor(R.color.white, null)
            valueTextSize = 14f
        }
        binding.immunityPieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            setCenterText("${gap.toInt()}%\nGap")
            setCenterTextColor(resources.getColor(R.color.white, null))
            setCenterTextSize(18f)
            legend.isEnabled = false
            setUsePercentValues(false)
            animateY(800)
            invalidate()
        }
    }

    private fun updateRiskAssessment(gap: Float) {
        val (riskLabel, fmd, lsd, mas) = when {
            gap > 60f -> listOf("HIGH 🔴", "HIGH 🔴", "MEDIUM 🟡", "LOW 🟢")
            gap > 30f -> listOf("MEDIUM 🟡", "MEDIUM 🟡", "LOW 🟢", "LOW 🟢")
            else      -> listOf("LOW 🟢", "LOW 🟢", "LOW 🟢", "LOW 🟢")
        }
        binding.riskLevelTextView.text = "Risk Level: $riskLabel"
        binding.fmdRiskTextView.text   = "• FMD Risk: $fmd"
        binding.lsdRiskTextView.text   = "• LSD Risk: $lsd"
        binding.mastitisRiskTextView.text = "• Mastitis Risk: $mas"
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
=======
        immunityViewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updatePieChart(gapPercentage: Float) {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(gapPercentage, "Gap"))
        entries.add(PieEntry(100f - gapPercentage, "Immune"))

        val dataSet = PieDataSet(entries, "Immunity Gap")
        dataSet.colors = listOf(resources.getColor(R.color.sacred_orange, null), resources.getColor(R.color.bioluminescent_green, null))
        dataSet.valueTextColor = resources.getColor(R.color.white, null)
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)
        binding.immunityPieChart.data = data
        binding.immunityPieChart.description.isEnabled = false
        binding.immunityPieChart.setCenterText("${gapPercentage.toInt()}%")
        binding.immunityPieChart.setCenterTextColor(resources.getColor(R.color.white, null))
        binding.immunityPieChart.setCenterTextSize(24f)
        binding.immunityPieChart.legend.isEnabled = false
        binding.immunityPieChart.setUsePercentValues(false)
        binding.immunityPieChart.invalidate()
    }

    private fun updateRiskAssessment(gapPercentage: Float) {
        val riskLevel: String
        val outbreakProbability: String
        val fmdRisk: String
        val lsdRisk: String
        val mastitisRisk: String

        when {
            gapPercentage > 60 -> {
                riskLevel = "HIGH 🔴"
                outbreakProbability = "Outbreak probability in next 21 days: 78%"
                fmdRisk = "• FMD Risk: HIGH 🔴"
                lsdRisk = "• LSD Risk: MEDIUM 🟡"
                mastitisRisk = "• Mastitis Risk: LOW 🟢"
            }
            gapPercentage > 30 -> {
                riskLevel = "MEDIUM 🟡"
                outbreakProbability = "Outbreak probability in next 21 days: 45%"
                fmdRisk = "• FMD Risk: MEDIUM 🟡"
                lsdRisk = "• LSD Risk: LOW 🟢"
                mastitisRisk = "• Mastitis Risk: LOW 🟢"
            }
            else -> {
                riskLevel = "LOW 🟢"
                outbreakProbability = "Outbreak probability in next 21 days: 15%"
                fmdRisk = "• FMD Risk: LOW 🟢"
                lsdRisk = "• LSD Risk: LOW 🟢"
                mastitisRisk = "• Mastitis Risk: LOW 🟢"
            }
        }

        binding.riskLevelTextView.text = "Risk Level: $riskLevel"
        binding.outbreakProbabilityTextView.text = outbreakProbability
        binding.fmdRiskTextView.text = fmdRisk
        binding.lsdRiskTextView.text = lsdRisk
        binding.mastitisRiskTextView.text = mastitisRisk

        // TODO: Populate action recommendations dynamically
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
}
