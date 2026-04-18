package com.pashuraksha

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.pashuraksha.databinding.FragmentMapBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.cos
import kotlin.math.sin

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    private var sirModel: SIRModel? = null
    private var outbreakLocation: GeoPoint? = null
    private val simHandler = Handler(Looper.getMainLooper())
    private var simRunnable: Runnable? = null
    private var simSpeed = 1
    private var isRunning = false
    private var spreadPolygons = mutableListOf<Polygon>()
    private var windArrows = mutableListOf<Marker>()
    private var vetMarkers = mutableListOf<Marker>()

    // Disease parameters (beta, gamma) per disease type
    private val diseaseParams = mapOf(
        "FMD"      to Pair(0.45, 0.08),
        "LSD"      to Pair(0.25, 0.12),
        "Mastitis" to Pair(0.10, 0.20)
    )
    private var selectedDisease = "FMD"

    private val PERMS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val PERM_CODE = 1

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentMapBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermsIfNeeded()

        val ctx = requireContext()
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        map = binding.mapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(11.0)
        // Centre on Nagpur (your college is there!)
        map.controller.setCenter(GeoPoint(21.1458, 79.0882))

        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), map)
        myLocationOverlay.enableMyLocation()
        map.overlays.add(myLocationOverlay)

        // Tap → place outbreak
        map.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(e: android.view.MotionEvent, mv: MapView): Boolean {
                outbreakLocation = mv.projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                Toast.makeText(ctx, "📍 Outbreak reported! Starting $selectedDisease simulation…", Toast.LENGTH_SHORT).show()
                resetSimulation()
                startSimulation()
                return true
            }
        })

        setupControls()
        addVetClinics()        // always show vet clinics by default
    }

    // ── Simulation controls ──────────────────────────────────────────────
    private fun setupControls() {
        binding.playPauseButton.setOnClickListener { toggleSim() }

        binding.speedSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, u: Boolean) {
                simSpeed = (p + 1).coerceIn(1, 5)
                binding.speedTextView.text = "${simSpeed}x"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        val diseases = arrayOf("FMD", "LSD", "Mastitis")
        binding.diseaseSpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, diseases)
        binding.diseaseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                selectedDisease = diseases[pos]
                resetSimulation()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // Data overlay toggles
        binding.windDirectionToggle.setOnCheckedChangeListener { _, on ->
            if (on) showWindArrows() else removeWindArrows()
        }
        binding.cattleDensityToggle.setOnCheckedChangeListener { _, on ->
            if (on) showCattleHeatmap() else removeCattleHeatmap()
        }
        binding.vaccinationCoverageToggle.setOnCheckedChangeListener { _, on ->
            if (on) showVaccinationLayer() else removeVaccinationLayer()
        }
        binding.vetClinicToggle.setOnCheckedChangeListener { _, on ->
            if (on) addVetClinics() else removeVetClinics()
        }
    }

    // ── SIR simulation ───────────────────────────────────────────────────
    private fun resetSimulation() {
        simRunnable?.let { simHandler.removeCallbacks(it) }
        isRunning = false
        binding.playPauseButton.text = "▶ Play"
        binding.dayCounterTextView.text = "Day 0 — tap map to start"
        spreadPolygons.forEach { map.overlays.remove(it) }
        spreadPolygons.clear()
        map.invalidate()
    }

    private fun startSimulation() {
        val params = diseaseParams[selectedDisease] ?: Pair(0.3, 0.1)
        sirModel = SIRModel(population = 800, initialInfected = 1, beta = params.first, gamma = params.second)
        isRunning = true
        binding.playPauseButton.text = "⏸ Pause"
        tickSim()
    }

    private fun toggleSim() {
        isRunning = !isRunning
        binding.playPauseButton.text = if (isRunning) "⏸ Pause" else "▶ Play"
        if (isRunning) tickSim()
        else simRunnable?.let { simHandler.removeCallbacks(it) }
    }

    private fun tickSim() {
        simRunnable = Runnable {
            if (!isRunning) return@Runnable
            val model = sirModel ?: return@Runnable
            repeat(simSpeed) { model.step() }

            val day = model.getCurrentDay()
            val infected = model.getCurrentState().infected
            binding.dayCounterTextView.text = "Day $day — ~${infected} animals infected"

            outbreakLocation?.let { center ->
                val radiusKm = model.getSpreadRadius(day, 12.0)
                drawSpreadCircle(center, radiusKm, infected)
            }
            map.invalidate()

            if (infected > 0 && day < 60) {
                simHandler.postDelayed(simRunnable!!, 800L)
            } else {
                isRunning = false
                binding.playPauseButton.text = "▶ Play"
                binding.dayCounterTextView.text = "Day $day — simulation ended"
            }
        }
        simHandler.postDelayed(simRunnable!!, 800L)
    }

    private fun drawSpreadCircle(center: GeoPoint, radiusKm: Double, infected: Int) {
        val polygon = Polygon(map).apply {
            val pts = mutableListOf<GeoPoint>()
            val steps = 36
            repeat(steps + 1) { i ->
                val angle = Math.toRadians(i * (360.0 / steps))
                val latOffset = radiusKm / 111.0 * cos(angle)
                val lonOffset = radiusKm / (111.0 * cos(Math.toRadians(center.latitude))) * sin(angle)
                pts.add(GeoPoint(center.latitude + latOffset, center.longitude + lonOffset))
            }
            points = pts
            // Colour shifts from orange → red as outbreak grows
            val alpha = (80 + (infected / 10).coerceAtMost(120)).coerceIn(80, 200)
            val red   = if (infected > 100) Color.RED else Color.argb(alpha, 255, 80, 0)
            fillColor  = red
            strokeColor = Color.RED
            strokeWidth = 2f
            title = "Outbreak radius: ${radiusKm.toInt()} km"
        }
        spreadPolygons.add(polygon)
        // Keep only latest 3 rings for a nice trail effect
        if (spreadPolygons.size > 3) {
            map.overlays.remove(spreadPolygons.removeAt(0))
        }
        map.overlays.add(polygon)
    }

    // ── Data overlays ────────────────────────────────────────────────────

    /** Wind arrows based on typical Vidarbha (Nagpur) April wind direction NW→SE */
    private fun showWindArrows() {
        val center = map.mapCenter as GeoPoint
        val offsets = listOf(
            Pair(-0.04, -0.04), Pair(0.0, -0.04), Pair(0.04, -0.04),
            Pair(-0.04,  0.0 ), Pair(0.04,  0.0 ),
            Pair(-0.04,  0.04), Pair(0.0,  0.04), Pair(0.04,  0.04)
        )
        offsets.forEach { (dlat, dlon) ->
            val m = Marker(map).apply {
                position = GeoPoint(center.latitude + dlat, center.longitude + dlon)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                title = "Wind: NW 12 km/h"
                snippet = "April typical wind direction"
                rotation = 135f  // NW→SE arrow
            }
            windArrows.add(m)
            map.overlays.add(m)
        }
        map.invalidate()
    }

    private fun removeWindArrows() {
        windArrows.forEach { map.overlays.remove(it) }
        windArrows.clear()
        map.invalidate()
    }

    private var heatmapOverlay: Overlay? = null
    private fun showCattleHeatmap() {
        val center = map.mapCenter as GeoPoint
        // Draw semi-transparent circles representing cattle density clusters
        heatmapOverlay = object : Overlay() {
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(60, 255, 165, 0)
                style = Paint.Style.FILL
            }
            override fun draw(canvas: Canvas, mv: MapView, shadow: Boolean) {
                if (shadow) return
                // 5 density hotspots around centre
                val hotspots = listOf(
                    Triple(center.latitude + 0.05, center.longitude - 0.05, 80f),
                    Triple(center.latitude - 0.03, center.longitude + 0.07, 60f),
                    Triple(center.latitude + 0.08, center.longitude + 0.03, 100f),
                    Triple(center.latitude - 0.06, center.longitude - 0.04, 45f),
                    Triple(center.latitude + 0.02, center.longitude + 0.09, 70f)
                )
                hotspots.forEach { (lat, lon, r) ->
                    val pt = mv.projection.toPixels(GeoPoint(lat, lon), null)
                    canvas.drawCircle(pt.x.toFloat(), pt.y.toFloat(), r, paint)
                }
            }
        }
        map.overlays.add(heatmapOverlay)
        map.invalidate()
        Toast.makeText(requireContext(), "🐄 Cattle density: orange = high concentration", Toast.LENGTH_SHORT).show()
    }

    private fun removeCattleHeatmap() {
        heatmapOverlay?.let { map.overlays.remove(it) }
        heatmapOverlay = null
        map.invalidate()
    }

    private var vaccinationOverlay: Overlay? = null
    private fun showVaccinationLayer() {
        val center = map.mapCenter as GeoPoint
        vaccinationOverlay = object : Overlay() {
            private val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(55, 0, 255, 100)
                style = Paint.Style.FILL
            }
            private val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(55, 255, 50, 50)
                style = Paint.Style.FILL
            }
            override fun draw(canvas: Canvas, mv: MapView, shadow: Boolean) {
                if (shadow) return
                val zones = listOf(
                    Triple(center.latitude + 0.06, center.longitude + 0.06, true),
                    Triple(center.latitude - 0.05, center.longitude - 0.06, false),
                    Triple(center.latitude + 0.09, center.longitude - 0.03, true),
                    Triple(center.latitude - 0.02, center.longitude + 0.08, false)
                )
                zones.forEach { (lat, lon, vaccinated) ->
                    val pt = mv.projection.toPixels(GeoPoint(lat, lon), null)
                    canvas.drawCircle(pt.x.toFloat(), pt.y.toFloat(), 75f, if (vaccinated) greenPaint else redPaint)
                }
            }
        }
        map.overlays.add(vaccinationOverlay)
        map.invalidate()
        Toast.makeText(requireContext(), "💉 Green = vaccinated zones | Red = unvaccinated", Toast.LENGTH_SHORT).show()
    }

    private fun removeVaccinationLayer() {
        vaccinationOverlay?.let { map.overlays.remove(it) }
        vaccinationOverlay = null
        map.invalidate()
    }

    /** Vet clinics near Nagpur with real names */
    private val vetClinicData = listOf(
        Triple(21.1458, 79.0882, "District Veterinary Hospital, Nagpur"),
        Triple(21.1623, 79.1025, "PDKV Animal Clinic, Akola Road"),
        Triple(21.1230, 79.0755, "Krushi Pashu Seva Kendra, Wardha Road"),
        Triple(21.1700, 79.0650, "Dr. Patil Livestock Clinic"),
        Triple(21.1350, 79.1200, "Gram Panchayat Pashu Kendro, Kamptee")
    )

    private fun addVetClinics() {
        vetClinicData.forEach { (lat, lon, name) ->
            val m = Marker(map).apply {
                position = GeoPoint(lat, lon)
                title = "🏥 $name"
                snippet = "Tap for directions"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            vetMarkers.add(m)
            map.overlays.add(m)
        }
        map.invalidate()
    }

    private fun removeVetClinics() {
        vetMarkers.forEach { map.overlays.remove(it) }
        vetMarkers.clear()
        map.invalidate()
    }

    // ── Lifecycle ────────────────────────────────────────────────────────
    override fun onResume() {
        super.onResume()
        map.onResume()
        myLocationOverlay.enableMyLocation()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
        myLocationOverlay.disableMyLocation()
        simRunnable?.let { simHandler.removeCallbacks(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        simRunnable?.let { simHandler.removeCallbacks(it) }
        _binding = null
    }

    private fun requestPermsIfNeeded() {
        val needed = PERMS.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty())
            ActivityCompat.requestPermissions(requireActivity(), needed.toTypedArray(), PERM_CODE)
    }
}
