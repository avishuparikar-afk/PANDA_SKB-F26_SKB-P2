package com.pashuraksha

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.sin

class SplashAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    // Trishul properties
    private var trishulAlpha = 0f
    private var trishulScale = 0.5f

    // Mandala properties
    private var mandalaRotation = 0f
    private var mandalaAlpha = 0f

    // Cosmic Energy Sequence properties
    private var energyLinePhase = 0f
    private var orbScale = 0f
    private var fibonacciSpiralPhase = 0f
    private var shivaSilhouetteAlpha = 0f
    private var shivaSilhouetteScale = 0f

    private val energyLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = ContextCompat.getColor(context, R.color.sacred_orange)
    }

    private val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.bioluminescent_green)
    }

    private val shivaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    init {
        paint.style = Paint.Style.FILL
        paint.color = ContextCompat.getColor(context, R.color.bioluminescent_green)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        // Draw Mandala (sacred geometry)
        paint.color = ContextCompat.getColor(context, R.color.sacred_orange)
        paint.alpha = (mandalaAlpha * 255).toInt()
        canvas.save()
        canvas.rotate(mandalaRotation, centerX, centerY)
        // Simple mandala: a few concentric circles and radiating lines
        for (i in 0 until 3) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawCircle(centerX, centerY, (width / 8f) * (i + 1), paint)
        }
        for (i in 0 until 12) {
            canvas.drawLine(centerX, centerY, centerX + width / 8f * 3, centerY, paint)
            canvas.rotate(30f, centerX, centerY)
        }
        canvas.restore()

        // Draw Trishul (Shiva silhouette)
        paint.color = ContextCompat.getColor(context, R.color.bioluminescent_green)
        paint.alpha = (trishulAlpha * 255).toInt()
        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.scale(trishulScale, trishulScale)
        // Draw a simplified Trishul shape
        path.reset()
        path.moveTo(0f, -150f)
        path.lineTo(-50f, -50f)
        path.lineTo(-100f, -100f)
        path.lineTo(-100f, 0f)
        path.lineTo(-20f, 0f)
        path.lineTo(-20f, 150f)
        path.lineTo(20f, 150f)
        path.lineTo(20f, 0f)
        path.lineTo(100f, 0f)
        path.lineTo(100f, -100f)
        path.lineTo(50f, -50f)
        path.close()
        canvas.drawPath(path, paint)
        canvas.restore()

        // Cosmic Energy Sequence Drawing
        // 1. Golden/orange energy particle lines shoot UPWARD
        energyLinePaint.alpha = (energyLinePhase * 255).toInt()
        canvas.save()
        // Simulate lines from bottom of screen converging to a point
        val startY = height.toFloat()
        val endY = centerY - height / 4f
        val controlX1 = centerX - width / 4f
        val controlY1 = startY - height / 4f
        val controlX2 = centerX + width / 4f
        val controlY2 = startY - height / 4f

        path.reset()
        path.moveTo(0f, startY)
        path.cubicTo(controlX1, controlY1, centerX, endY, centerX, endY)
        canvas.drawPath(path, energyLinePaint)

        path.reset()
        path.moveTo(width.toFloat(), startY)
        path.cubicTo(controlX2, controlY2, centerX, endY, centerX, endY)
        canvas.drawPath(path, energyLinePaint)
        canvas.restore()

        // 2. Glowing orb
        orbPaint.alpha = (orbScale * 255).toInt()
        canvas.drawCircle(centerX, centerY - height / 4f, 50f * orbScale, orbPaint)

        // 3. Fibonacci spiral around the orb
        paint.color = ContextCompat.getColor(context, R.color.cosmic_violet)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.alpha = (fibonacciSpiralPhase * 255).toInt()
        canvas.save()
        canvas.translate(centerX, centerY - height / 4f)
        val maxRadius = 100f
        val angleStep = 0.1f
        path.reset()
        path.moveTo(0f, 0f)
        for (i in 0 until (fibonacciSpiralPhase * 100).toInt()) {
            val angle = i * angleStep
            val radius = maxRadius * (angle / (100 * angleStep))
            val x = radius * cos(angle)
            val y = radius * sin(angle)
            path.lineTo(x, y)
        }
        canvas.drawPath(path, paint)
        canvas.restore()

        // 4. Lord Shiva cosmic silhouette
        shivaPaint.alpha = (shivaSilhouetteAlpha * 255).toInt()
        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.scale(shivaSilhouetteScale, shivaSilhouetteScale)
        // Simplified Shiva silhouette (e.g., a triangle for body, circle for head)
        path.reset()
        path.moveTo(0f, -200f) // Head
        path.addCircle(0f, -200f, 50f, Path.Direction.CW)
        path.moveTo(0f, -150f)
        path.lineTo(-100f, 100f)
        path.lineTo(100f, 100f)
        path.close()
        canvas.drawPath(path, shivaPaint)
        canvas.restore()
    }

    fun startInitialAnimation() {
        val trishulFadeIn = ObjectAnimator.ofFloat(this, "trishulAlpha", 0f, 1f)
        trishulFadeIn.duration = 1500
        trishulFadeIn.interpolator = AccelerateDecelerateInterpolator()

        val trishulScaleUp = ObjectAnimator.ofFloat(this, "trishulScale", 0.5f, 1f)
        trishulScaleUp.duration = 1500
        trishulScaleUp.interpolator = AccelerateDecelerateInterpolator()

        val mandalaFadeIn = ObjectAnimator.ofFloat(this, "mandalaAlpha", 0f, 0.3f)
        mandalaFadeIn.duration = 2000

        val mandalaSpin = ObjectAnimator.ofFloat(this, "mandalaRotation", 0f, 360f)
        mandalaSpin.duration = 4000
        mandalaSpin.repeatCount = ObjectAnimator.INFINITE
        mandalaSpin.interpolator = LinearInterpolator()

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(trishulFadeIn, trishulScaleUp, mandalaFadeIn, mandalaSpin)
        animatorSet.start()
    }

    fun startCosmicEnergySequence() {
        val animatorSet = AnimatorSet()

        // Second 0-2: Energy lines shoot upward
        val energyLineAnimator = ObjectAnimator.ofFloat(this, "energyLinePhase", 0f, 1f)
        energyLineAnimator.duration = 2000
        energyLineAnimator.interpolator = AccelerateDecelerateInterpolator()

        // Second 2-4: Orb forms, Fibonacci spiral draws
        val orbAnimator = ObjectAnimator.ofFloat(this, "orbScale", 0f, 1f)
        orbAnimator.duration = 2000
        orbAnimator.startDelay = 2000
        orbAnimator.interpolator = AccelerateDecelerateInterpolator()

        val fibonacciAnimator = ObjectAnimator.ofFloat(this, "fibonacciSpiralPhase", 0f, 1f)
        fibonacciAnimator.duration = 2000
        fibonacciAnimator.startDelay = 2000
        fibonacciAnimator.interpolator = LinearInterpolator()

        // Second 6-8: Shiva silhouette appears
        val shivaFadeIn = ObjectAnimator.ofFloat(this, "shivaSilhouetteAlpha", 0f, 1f)
        shivaFadeIn.duration = 1000
        shivaFadeIn.startDelay = 6000

        val shivaScaleUp = ObjectAnimator.ofFloat(this, "shivaSilhouetteScale", 0f, 1f)
        shivaScaleUp.duration = 1000
        shivaScaleUp.startDelay = 6000

        animatorSet.playTogether(energyLineAnimator, orbAnimator, fibonacciAnimator, shivaFadeIn, shivaScaleUp)
        animatorSet.start()
    }

    // Setter methods for ObjectAnimator
    fun setTrishulAlpha(value: Float) {
        trishulAlpha = value
        invalidate()
    }

    fun setTrishulScale(value: Float) {
        trishulScale = value
        invalidate()
    }

    fun setMandalaRotation(value: Float) {
        mandalaRotation = value
        invalidate()
    }

    fun setMandalaAlpha(value: Float) {
        mandalaAlpha = value
        invalidate()
    }

    fun setEnergyLinePhase(value: Float) {
        energyLinePhase = value
        invalidate()
    }

    fun setOrbScale(value: Float) {
        orbScale = value
        invalidate()
    }

    fun setFibonacciSpiralPhase(value: Float) {
        fibonacciSpiralPhase = value
        invalidate()
    }

    fun setShivaSilhouetteAlpha(value: Float) {
        shivaSilhouetteAlpha = value
        invalidate()
    }

    fun setShivaSilhouetteScale(value: Float) {
        shivaSilhouetteScale = value
        invalidate()
    }
}
