package com.pashuraksha

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val scanLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var scanLinePosition = 0f // 0 to 1, representing top to bottom

    // Bounding box data
    private var boundingBoxes: List<RectF> = emptyList()
    private var boxLabels: List<String> = emptyList()
    private var boxScores: List<String> = emptyList()

    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f
        isFakeBoldText = true
    }

    private val scoreTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 26f
    }

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = ContextCompat.getColor(context, R.color.bioluminescent_green)

        scanLinePaint.style = Paint.Style.STROKE
        scanLinePaint.strokeWidth = 8f
        scanLinePaint.color = ContextCompat.getColor(context, R.color.bioluminescent_green)

        // Scanning line animation
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.addUpdateListener { animation ->
            scanLinePosition = animation.animatedValue as Float
            invalidate()
        }
        animator.duration = 2000 // 2 seconds for one sweep
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = LinearInterpolator()
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        // Draw corner brackets
        val cornerSize = 50f
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())

        // Top-left
        canvas.drawLine(rect.left, rect.top + cornerSize, rect.left, rect.top, paint)
        canvas.drawLine(rect.left, rect.top, rect.left + cornerSize, rect.top, paint)
        // Top-right
        canvas.drawLine(rect.right - cornerSize, rect.top, rect.right, rect.top, paint)
        canvas.drawLine(rect.right, rect.top, rect.right, rect.top + cornerSize, paint)
        // Bottom-left
        canvas.drawLine(rect.left, rect.bottom - cornerSize, rect.left, rect.bottom, paint)
        canvas.drawLine(rect.left, rect.bottom, rect.left + cornerSize, rect.bottom, paint)
        // Bottom-right
        canvas.drawLine(rect.right - cornerSize, rect.bottom, rect.right, rect.bottom, paint)
        canvas.drawLine(rect.right, rect.bottom, rect.right, rect.bottom - cornerSize, paint)

        // Draw scanning grid lines (simplified as horizontal lines for now)
        val gridLineCount = 10
        for (i in 0 until gridLineCount) {
            val y = height * (i.toFloat() / gridLineCount)
            paint.alpha = (70 * (1 - Math.abs(scanLinePosition - (i.toFloat() / gridLineCount)))).toInt() // Fade based on proximity to scan line
            canvas.drawLine(0f, y, width.toFloat(), y, paint)
        }

        // Draw horizontal glowing scan line
        val scanY = height * scanLinePosition
        canvas.drawLine(0f, scanY, width.toFloat(), scanY, scanLinePaint)

        // Draw bounding boxes for detected objects
        drawDetectedObjects(canvas)
    }

    private fun drawDetectedObjects(canvas: Canvas) {
        if (boundingBoxes.isEmpty()) return

        // Scale factors from camera resolution to view size
        // ML Kit returns coordinates in image space — we need to map to view space
        for (i in boundingBoxes.indices) {
            val box = boundingBoxes[i]
            val label = if (i < boxLabels.size) boxLabels[i] else "Animal #${i + 1}"
            val score = if (i < boxScores.size) boxScores[i] else ""

            // Determine color based on health score
            val boxColor = getBoxColor(score)
            boxPaint.color = boxColor
            labelBgPaint.color = boxColor

            // Scale box coordinates to view dimensions
            // ML Kit image is typically 480x640 (portrait) or similar
            // Scale proportionally to overlay view
            val scaleX = width.toFloat() / 480f  // approximate camera width
            val scaleY = height.toFloat() / 640f  // approximate camera height

            val scaledBox = RectF(
                box.left * scaleX,
                box.top * scaleY,
                box.right * scaleX,
                box.bottom * scaleY
            )

            // Clamp to view bounds
            scaledBox.left = scaledBox.left.coerceIn(4f, width - 4f)
            scaledBox.top = scaledBox.top.coerceIn(4f, height - 4f)
            scaledBox.right = scaledBox.right.coerceIn(scaledBox.left + 10f, width - 4f)
            scaledBox.bottom = scaledBox.bottom.coerceIn(scaledBox.top + 10f, height - 4f)

            // Draw the bounding box
            canvas.drawRoundRect(scaledBox, 8f, 8f, boxPaint)

            // Draw semi-transparent fill
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.argb(30, Color.red(boxColor), Color.green(boxColor), Color.blue(boxColor))
            }
            canvas.drawRoundRect(scaledBox, 8f, 8f, fillPaint)

            // Draw label background
            val labelText = label
            val labelWidth = labelTextPaint.measureText(labelText) + 20f
            val labelHeight = 42f
            val labelRect = RectF(
                scaledBox.left,
                scaledBox.top - labelHeight - 4f,
                scaledBox.left + labelWidth,
                scaledBox.top - 4f
            )
            // Ensure label stays visible
            if (labelRect.top < 0) {
                labelRect.offset(0f, -labelRect.top + 4f)
            }
            labelBgPaint.alpha = 200
            canvas.drawRoundRect(labelRect, 6f, 6f, labelBgPaint)

            // Draw label text
            canvas.drawText(labelText, labelRect.left + 10f, labelRect.bottom - 12f, labelTextPaint)

            // Draw score below the label
            if (score.isNotEmpty()) {
                val scoreWidth = scoreTextPaint.measureText(score) + 16f
                val scoreRect = RectF(
                    scaledBox.left,
                    labelRect.bottom + 2f,
                    scaledBox.left + scoreWidth,
                    labelRect.bottom + 34f
                )
                val scoreBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = Color.argb(180, 0, 0, 0)
                }
                canvas.drawRoundRect(scoreRect, 6f, 6f, scoreBg)
                canvas.drawText(score, scoreRect.left + 8f, scoreRect.bottom - 8f, scoreTextPaint)
            }

            // Draw corner highlights on the box
            val cornerLen = 20f
            val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 6f
                color = boxColor
            }
            // Top-left
            canvas.drawLine(scaledBox.left, scaledBox.top + cornerLen, scaledBox.left, scaledBox.top, cornerPaint)
            canvas.drawLine(scaledBox.left, scaledBox.top, scaledBox.left + cornerLen, scaledBox.top, cornerPaint)
            // Top-right
            canvas.drawLine(scaledBox.right - cornerLen, scaledBox.top, scaledBox.right, scaledBox.top, cornerPaint)
            canvas.drawLine(scaledBox.right, scaledBox.top, scaledBox.right, scaledBox.top + cornerLen, cornerPaint)
            // Bottom-left
            canvas.drawLine(scaledBox.left, scaledBox.bottom - cornerLen, scaledBox.left, scaledBox.bottom, cornerPaint)
            canvas.drawLine(scaledBox.left, scaledBox.bottom, scaledBox.left + cornerLen, scaledBox.bottom, cornerPaint)
            // Bottom-right
            canvas.drawLine(scaledBox.right - cornerLen, scaledBox.bottom, scaledBox.right, scaledBox.bottom, cornerPaint)
            canvas.drawLine(scaledBox.right, scaledBox.bottom, scaledBox.right, scaledBox.bottom - cornerLen, cornerPaint)
        }
    }

    private fun getBoxColor(score: String): Int {
        return when {
            score.contains("CHECK") || score.contains("⚠") -> Color.rgb(229, 57, 53)   // Red for alert
            score.contains("Watch") || score.contains("👁") -> Color.rgb(255, 152, 0)   // Orange for watch
            score.contains("Healthy") || score.contains("✅") -> Color.rgb(76, 175, 80) // Green for healthy
            else -> ContextCompat.getColor(context, R.color.bioluminescent_green)
        }
    }

    // Function to draw bounding boxes for detected objects
    fun drawBoundingBoxes(boxes: List<RectF>, labels: List<String>, healthScores: List<String>) {
        boundingBoxes = boxes
        boxLabels = labels
        boxScores = healthScores
        invalidate()
    }
}
