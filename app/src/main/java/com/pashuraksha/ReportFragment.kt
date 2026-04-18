package com.pashuraksha

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pashuraksha.data.SessionData
import com.pashuraksha.databinding.FragmentReportBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Health Report — pulls REAL data from SessionData.
 *
 * Shows actual scan results, immunity gap calculations, and disease detection
 * results from the current session. Updates dynamically every time user navigates here.
 * If no data collected yet, shows clear actionable guidance.
 */
class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentReportBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SessionData.init(requireContext())
        populateReport()
    }

    /**
     * Re-populate every time the fragment becomes visible.
     * This ensures data saved from Scan/Immunity/Disease tabs shows up
     * immediately when user navigates to Report.
     */
    override fun onResume() {
        super.onResume()
        if (_binding != null) populateReport()
    }

    private fun populateReport() {
        val today = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        binding.reportDateTextView.text = "📅 Report Date: $today"

        // ── Quick Stat Cards ─────────────────────────────────────────
        val hasScan = SessionData.hasScanData()
        val hasImm = SessionData.hasImmunityData()
        val hasDiag = SessionData.hasDiagnosisData()

        if (hasScan) {
            val total = SessionData.getScanTotal()
            val healthy = SessionData.getScanHealthy()
            val healthyPct = if (total > 0) "%.0f".format(healthy * 100f / total) else "0"
            binding.statScannedCount.text = "$total"
            binding.statHealthyPct.text = "${healthyPct}%"
        } else {
            binding.statScannedCount.text = "0"
            binding.statHealthyPct.text = "—"
        }

        // ── Village / Location ───────────────────────────────────────
        if (hasImm) {
            val village = SessionData.getImmVillage()
            val district = SessionData.getImmDistrict()
            binding.reportVillageTextView.text = "📍 Village: $village, $district"
        } else {
            binding.reportVillageTextView.text = "📍 Village: — (use Immunity tab to set)"
        }

        // ── Scan Results ─────────────────────────────────────────────
        if (hasScan) {
            val total = SessionData.getScanTotal()
            val healthy = SessionData.getScanHealthy()
            val watch = SessionData.getScanWatch()
            val alert = SessionData.getScanAlert()

            val healthyPct = if (total > 0) "%.1f".format(healthy * 100f / total) else "0"
            val watchPct = if (total > 0) "%.1f".format(watch * 100f / total) else "0"
            val alertPct = if (total > 0) "%.1f".format(alert * 100f / total) else "0"

            binding.reportTotalCattleTextView.text = "🐄 Total Animals Scanned: $total"
            binding.reportHealthyTextView.text = "✅ Healthy: $healthy ($healthyPct%)"
            binding.reportWatchTextView.text = "👁 Watch: $watch ($watchPct%)"
            binding.reportAlertTextView.text = "⚠️ Alert: $alert ($alertPct%)"

            val scanTime = SessionData.getScanTimestamp()
            if (scanTime.isNotBlank()) {
                binding.reportTotalCattleTextView.text =
                    "🐄 Total Animals Scanned: $total  (at $scanTime)"
            }
        } else {
            binding.reportTotalCattleTextView.text = "🐄 No scan data yet — open the Scan tab and scan your herd"
            binding.reportHealthyTextView.text = "✅ Healthy: —"
            binding.reportWatchTextView.text = "👁 Watch: —"
            binding.reportAlertTextView.text = "⚠️ Alert: —"
        }

        // ── Immunity Gap ─────────────────────────────────────────────
        if (hasImm) {
            val gap = SessionData.getImmGap()
            val vaccinated = SessionData.getImmVaccinated()
            val total = SessionData.getImmTotal()
            val days = SessionData.getImmOutbreakDays()

            val riskLevel = when {
                gap > 60f -> "🔴 HIGH RISK"
                gap > 30f -> "🟡 MEDIUM RISK"
                else -> "🟢 LOW RISK"
            }
            binding.reportImmunityTextView.text =
                "💉 Herd Immunity Gap: ${gap.toInt()}%  $riskLevel\n" +
                "   Vaccinated: $vaccinated / $total\n" +
                "   Outbreak risk window: $days days"
        } else {
            binding.reportImmunityTextView.text =
                "💉 No immunity data yet — open the Immunity tab and enter your herd's vaccination details"
        }

        // ── Disease Detection ────────────────────────────────────────
        if (hasDiag) {
            val disease = SessionData.getDiagDisease()
            val confidence = SessionData.getDiagConfidence()
            val urgency = SessionData.getDiagUrgency()
            val symptoms = SessionData.getDiagSymptoms()

            val emoji = when (urgency.lowercase()) {
                "emergency" -> "🚨"
                "high" -> "🔴"
                "medium" -> "🟡"
                else -> "🟢"
            }
            binding.reportDiseaseTextView.text =
                "🦠 Last Diagnosis: $disease\n" +
                "   Confidence: $confidence%\n" +
                "   Urgency: $emoji $urgency\n" +
                "   Symptoms: $symptoms"
        } else {
            binding.reportDiseaseTextView.text =
                "🦠 No disease analysis yet — use the Diagnose tab to photograph a sick animal"
        }

        // ── Recommendations (Smart, data-driven) ─────────────────────
        binding.reportRecommendationTextView.text = buildRecommendations(hasScan, hasImm, hasDiag)
    }

    /**
     * Generates smart, context-aware recommendations based on ALL collected data.
     * Combines scan findings + immunity gaps + disease results into an actionable plan.
     */
    private fun buildRecommendations(hasScan: Boolean, hasImm: Boolean, hasDiag: Boolean): String {
        val lines = mutableListOf<String>()
        lines.add("📋 Action Plan:")

        var hasActions = false

        // Scan-based recommendations
        if (hasScan) {
            val alert = SessionData.getScanAlert()
            val watch = SessionData.getScanWatch()
            val total = SessionData.getScanTotal()
            val healthy = SessionData.getScanHealthy()

            if (alert > 0) {
                lines.add("🚨 $alert animal(s) in ALERT — isolate immediately and monitor temperature")
                hasActions = true
            }
            if (watch > 0) {
                lines.add("👁 $watch animal(s) on WATCH — re-scan within 24 hours")
                hasActions = true
            }
            if (total > 0 && healthy == total) {
                lines.add("✅ All $total animals healthy — maintain regular monitoring schedule")
                hasActions = true
            }
        }

        // Immunity-based recommendations
        if (hasImm) {
            val gap = SessionData.getImmGap()
            val unvaccinated = SessionData.getImmTotal() - SessionData.getImmVaccinated()
            val days = SessionData.getImmOutbreakDays()

            if (gap > 50f) {
                lines.add("💉 CRITICAL: ${gap.toInt()}% immunity gap — contact district veterinary officer urgently")
                hasActions = true
            } else if (gap > 25f) {
                lines.add("💉 Schedule vaccination for $unvaccinated unvaccinated animals within $days days")
                hasActions = true
            } else {
                lines.add("💉 Good immunity coverage — next vaccination check in ${days * 2} days")
                hasActions = true
            }
        }

        // Disease-based recommendations
        if (hasDiag) {
            val disease = SessionData.getDiagDisease()
            val urgency = SessionData.getDiagUrgency()
            val recs = SessionData.getDiagRecommendations()

            if (urgency.lowercase() == "emergency") {
                lines.add("🚨 EMERGENCY — $disease detected! Call vet helpline 1962 NOW")
                hasActions = true
            } else if (disease.lowercase() != "healthy") {
                lines.add("🦠 $disease detected — follow treatment protocol:")
                recs.split("\n").filter { it.isNotBlank() }.take(3).forEach {
                    lines.add("   $it")
                }
                hasActions = true
            }
        }

        // General advice if no specific actions
        if (!hasActions) {
            lines.add("")
            lines.add("Start by:")
            lines.add("  1. Open Scan tab → scan your herd")
            lines.add("  2. Open Immunity tab → enter vaccination data")
            lines.add("  3. Open Diagnose tab → photograph sick animals")
        }

        // Always show emergency contact
        lines.add("")
        lines.add("📞 Emergency Vet Helpline: 1962 (Govt. Animal Helpline)")

        return lines.joinToString("\n")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
