package com.example.operator.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Интерактивный компас: вращение стрелки пальцем задаёт направление с точностью
 * до градуса (0 = север, 90 = восток, по часовой стрелке).
 */
class CompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var angleDegrees: Float = 0f
        set(value) {
            field = ((value % 360) + 360) % 360
            invalidate()
            onAngleChanged?.invoke(field)
        }

    var onAngleChanged: ((Float) -> Unit)? = null

    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2C2C2E")
        style = Paint.Style.FILL
    }
    private val paintRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6B00")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val paintArrow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6B00")
        style = Paint.Style.FILL
    }
    private val paintArrowTail = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#555555")
        style = Paint.Style.FILL
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val paintTextSmall = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#AAAAAA")
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }
    private val paintTick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#555555")
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private var lastTouchAngle = 0f
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = (minOf(w, h) / 2f) * 0.85f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (radius <= 0f) return

        canvas.drawCircle(centerX, centerY, radius, paintCircle)
        canvas.drawCircle(centerX, centerY, radius, paintRing)

        // Деления по кругу (каждые 10°), крупные — на N/E/S/W.
        for (i in 0 until 36) {
            val angle = Math.toRadians(i * 10.0)
            val isMajor = i % 9 == 0
            val tickLen = if (isMajor) radius * 0.15f else radius * 0.08f
            val x1 = centerX + (radius - tickLen) * sin(angle).toFloat()
            val y1 = centerY - (radius - tickLen) * cos(angle).toFloat()
            val x2 = centerX + radius * 0.95f * sin(angle).toFloat()
            val y2 = centerY - radius * 0.95f * cos(angle).toFloat()
            paintTick.strokeWidth = if (isMajor) 3f else 1.5f
            paintTick.color = if (isMajor) Color.parseColor("#FF6B00") else Color.parseColor("#444444")
            canvas.drawLine(x1, y1, x2, y2, paintTick)
        }

        // Метки сторон света. Север подсвечен красным — стандарт компасов.
        val labelRadius = radius * 0.72f
        val cardinals = mapOf(0f to "N", 90f to "E", 180f to "S", 270f to "W")
        cardinals.forEach { (angle, label) ->
            val rad = Math.toRadians(angle.toDouble())
            val x = centerX + labelRadius * sin(rad).toFloat()
            val y = centerY - labelRadius * cos(rad).toFloat() + paintText.textSize / 3
            paintText.color = if (label == "N") Color.parseColor("#FF4444") else Color.WHITE
            canvas.drawText(label, x, y, paintText)
        }

        // Стрелка компаса.
        canvas.save()
        canvas.rotate(angleDegrees, centerX, centerY)

        val arrowLength = radius * 0.55f
        val arrowWidth = radius * 0.08f

        val pathArrow = Path().apply {
            moveTo(centerX, centerY - arrowLength)
            lineTo(centerX - arrowWidth, centerY)
            lineTo(centerX + arrowWidth, centerY)
            close()
        }
        canvas.drawPath(pathArrow, paintArrow)

        val pathTail = Path().apply {
            moveTo(centerX, centerY + arrowLength * 0.6f)
            lineTo(centerX - arrowWidth * 0.7f, centerY)
            lineTo(centerX + arrowWidth * 0.7f, centerY)
            close()
        }
        canvas.drawPath(pathTail, paintArrowTail)

        canvas.drawCircle(centerX, centerY, arrowWidth * 0.8f, paintArrow)
        canvas.restore()

        val degreeText = "${angleDegrees.toInt()}°"
        canvas.drawText(degreeText, centerX, centerY + radius + paintTextSmall.textSize * 1.5f, paintTextSmall)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val dx = event.x - centerX
        val dy = event.y - centerY

        val touchAngle = (Math.toDegrees(atan2(dx.toDouble(), -dy.toDouble())).toFloat() + 360) % 360

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchAngle = touchAngle
            }
            MotionEvent.ACTION_MOVE -> {
                var delta = touchAngle - lastTouchAngle
                if (delta > 180) delta -= 360
                if (delta < -180) delta += 360
                angleDegrees += delta
                lastTouchAngle = touchAngle
            }
            MotionEvent.ACTION_UP -> performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    /** Программная установка угла с анимацией (снэп к N/E/S/W). */
    fun setAngleAnimated(targetAngle: Float) {
        // Кратчайший путь до цели (иначе анимация иногда "проматывала" бы почти полный круг).
        var delta = (targetAngle - angleDegrees) % 360
        if (delta > 180) delta -= 360
        if (delta < -180) delta += 360

        val animator = ValueAnimator.ofFloat(angleDegrees, angleDegrees + delta)
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { angleDegrees = it.animatedValue as Float }
        animator.start()
    }
}
