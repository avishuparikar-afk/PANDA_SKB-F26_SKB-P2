package com.pashuraksha

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.pashuraksha.databinding.ActivityCosmicEnergyBinding

/**
 * Pashupatinath Mode - the WOW MVP moment.
 *
 * Flow:
 *  1. Deep space black background (activity bg).
 *  2. Cosmic Shiva video (res/raw/cosmic_shiva.mp4) fades in behind the particle animation.
 *     The video's background was crushed to pure black during ffmpeg preprocessing,
 *     so it visually blends into the app background — only the glowing cosmic Shiva
 *     silhouette and nebula particles are perceptible. No chroma-key needed.
 *  3. The existing SplashAnimationView renders energy lines, orb, Fibonacci spiral
 *     on top for an additional layer of motion.
 *  4. "Look up" tilt hint banner appears.
 *  5. Sanskrit blessing + tagline fade in.
 *  6. Auto-return to MainActivity.
 *
 * If the video fails to load for any reason, we silently fall back to the original
 * hand-drawn animation — the screen still works.
 */
class CosmicEnergyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCosmicEnergyBinding
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCosmicEnergyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Full screen immersive
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setupCosmicVideo()

        // Kick off the particle/energy line animation (overlay on top of video)
        binding.cosmicEnergyAnimationView.startCosmicEnergySequence()

        // 1.5s in: show "look up" hint banner
        mainHandler.postDelayed({
            binding.notificationBanner.animate().alpha(1f).setDuration(600).start()
        }, 1500)

        // 4.5s in: reveal Sanskrit blessing + tagline
        mainHandler.postDelayed({
            binding.shivaText.animate().alpha(1f).setDuration(800).start()
            binding.shivaSubtitle.animate().alpha(1f).setDuration(800).start()
        }, 4500)

        // 11s in: return to main
        mainHandler.postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }, 11000)
    }

    private fun setupCosmicVideo() {
        try {
            val uri = Uri.parse("android.resource://$packageName/${R.raw.cosmic_shiva}")
            binding.cosmicShivaVideo.setVideoURI(uri)

            binding.cosmicShivaVideo.setOnPreparedListener { mp: MediaPlayer ->
                // Loop the cosmic footage; silent (audio stripped during encode anyway)
                mp.isLooping = true
                mp.setVolume(0f, 0f)

                // Fade video in over 1.2s — feels more cinematic than a hard cut
                binding.cosmicShivaVideo.animate()
                    .alpha(0.95f)
                    .setDuration(1200)
                    .start()

                binding.cosmicShivaVideo.start()
            }

            binding.cosmicShivaVideo.setOnErrorListener { _, _, _ ->
                // Graceful fallback: hide video, particle animation still plays
                binding.cosmicShivaVideo.alpha = 0f
                true
            }
        } catch (t: Throwable) {
            binding.cosmicShivaVideo.alpha = 0f
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.cosmicShivaVideo.isPlaying) {
            binding.cosmicShivaVideo.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!binding.cosmicShivaVideo.isPlaying) {
            binding.cosmicShivaVideo.start()
        }
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        try {
            binding.cosmicShivaVideo.stopPlayback()
        } catch (_: Throwable) { /* no-op */ }
        super.onDestroy()
    }
}
