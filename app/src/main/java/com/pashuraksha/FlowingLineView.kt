package com.pashuraksha

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat

class FlowingLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private var phase = 0f

    init {
        paint.color = ContextCompat.getColor(context, R.color.bioluminescent_green)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.alpha = 180 // Semi-transparent

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.addUpdateListener { animation ->
            phase = animation.animatedValue as Float
            invalidate()
        }
        animator.duration = 5000 // 5 seconds for one cycle
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        path.reset()

        // Create a flowing sine wave like path
        val amplitude = height / 4f
        val frequency = 2f // Number of waves across the screen
        val startY = height / 2f

        path.moveTo(0f, startY)

        for (i in 0..width) {
            val x = i.toFloat()
            val y = startY + amplitude * Math.sin(2 * Math.PI * frequency * (x / width + phase)).toFloat()
            path.lineTo(x, y)
        }

        canvas.drawPath(path, paint)
    }
}
