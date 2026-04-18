package com.pashuraksha

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.pashuraksha.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

                binding.splashAnimationView.startInitialAnimation()

        // Animate app name and subtitle
        binding.appNameTextView.animate().alpha(1f).setDuration(1000).start()
        binding.subtitleTextView.animate().alpha(1f).setDuration(1000).setStartDelay(500).start()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java) // Assuming MainActivity is the Home screen
            startActivity(intent)
            finish()
        }, 3000) // 3 seconds delay
    }
}
