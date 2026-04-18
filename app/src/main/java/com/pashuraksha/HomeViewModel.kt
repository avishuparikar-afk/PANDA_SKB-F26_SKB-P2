package com.pashuraksha

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
<<<<<<< HEAD
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

class HomeViewModel : ViewModel() {

    private val _greeting    = MutableLiveData<String>()
    private val _villageName = MutableLiveData<String>()
    private val _weather     = MutableLiveData<String?>()

    val greeting: LiveData<String>    = _greeting
    val villageName: LiveData<String> = _villageName
    val weather: LiveData<String?>    = _weather

    init {
        _greeting.value    = buildGreeting()
        _villageName.value = "Sakoli, Bhandara • Maharashtra"
        fetchWeather()
    }

    private fun buildGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "🌅 Suprabhat, Kisaan! (Good Morning)"
            hour < 17 -> "☀️ Namaskar, Kisaan!"
            hour < 21 -> "🌇 Shubbh Sandhya, Kisaan!"
            else      -> "🌙 Shubbh Ratri, Kisaan!"
        }
    }

    private fun fetchWeather() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    // Nagpur coordinates — free API, no key needed for basic call
                    val url = URL("https://api.open-meteo.com/v1/forecast?latitude=21.15&longitude=79.09&current_weather=true&temperature_unit=celsius")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 5000
                    conn.readTimeout   = 5000
                    val text = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(text)
                    val cw   = json.getJSONObject("current_weather")
                    val temp = cw.getDouble("temperature").toInt()
                    val wc   = cw.getInt("weathercode")
                    val desc = weatherDesc(wc)
                    "$temp°C · $desc"
                } catch (t: Throwable) {
                    "38°C · Hot & Sunny" // Typical April Nagpur fallback
                }
            }
            _weather.value = result
        }
    }

    private fun weatherDesc(code: Int): String = when (code) {
        0            -> "Clear Sky"
        1, 2, 3      -> "Partly Cloudy"
        45, 48       -> "Foggy"
        51,53,55     -> "Drizzle"
        61,63,65     -> "Rain"
        71,73,75     -> "Snow"
        80,81,82     -> "Showers"
        95           -> "Thunderstorm"
        else         -> "Mixed"
    }
=======
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {

    private val _greeting = MutableLiveData<String>().apply {
        value = "नमस्ते किसान 🙏"
    }
    val greeting: LiveData<String> = _greeting

    private val _currentTime = MutableLiveData<String>()
    val currentTime: LiveData<String> = _currentTime

    private val _currentDate = MutableLiveData<String>()
    val currentDate: LiveData<String> = _currentDate

    private val _villageName = MutableLiveData<String>().apply {
        value = "Sample Village"
    }
    val villageName: LiveData<String> = _villageName

    private val _weather = MutableLiveData<String>().apply {
        value = "25°C, Sunny"
    }
    val weather: LiveData<String> = _weather

    init {
        updateDateTime()
    }

    private fun updateDateTime() {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        _currentTime.value = timeFormat.format(calendar.time)
        _currentDate.value = dateFormat.format(calendar.time)
    }

    // TODO: Implement actual live clock and date updates, and OpenWeatherMap API integration
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
}
