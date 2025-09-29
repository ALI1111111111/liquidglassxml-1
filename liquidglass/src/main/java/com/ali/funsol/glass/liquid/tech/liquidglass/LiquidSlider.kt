package com.ali.funsol.glass.liquid.tech.liquidglass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import kotlin.math.roundToInt

class LiquidSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : GlassView(context, attrs, defStyle) {

    var value: Float = 0.5f
        set(value) {
            field = value.coerceIn(0f, 1f)
            onValueChangedListener?.onValueChanged(field)
            invalidate()
        }

    var onValueChangedListener: OnValueChangedListener? = null

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(100, 255, 255, 255)
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private var trackY = 0f
    private var velocityTracker: VelocityTracker? = null
    private val thumbView: GlassView

    // --- Physics-based Animations ---
    private val scaleXProperty = object : FloatPropertyCompat<GlassView>("layerScaleX") {
        override fun getValue(view: GlassView): Float = view.layerScaleX
        override fun setValue(view: GlassView, value: Float) {
            view.layerScaleX = value
        }
    }

    private val scaleYProperty = object : FloatPropertyCompat<GlassView>("layerScaleY") {
        override fun getValue(view: GlassView): Float = view.layerScaleY
        override fun setValue(view: GlassView, value: Float) {
            view.layerScaleY = value
        }
    }

    private val springScaleX: SpringAnimation by lazy {
        SpringAnimation(thumbView, scaleXProperty).setSpring(
            SpringForce(1f).apply {
                stiffness = SpringForce.STIFFNESS_MEDIUM
                dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            }
        )
    }

    private val springScaleY: SpringAnimation by lazy {
        SpringAnimation(thumbView, scaleYProperty).setSpring(
            SpringForce(1f).apply {
                stiffness = SpringForce.STIFFNESS_MEDIUM
                dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            }
        )
    }

    init {
        thumbView = GlassView(context).apply {
            cornerRadius = 999f
            blurRadius = 10f
            refractionIntensity = 0.05f
        }
        addView(thumbView)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.LiquidSlider)
            value = a.getFloat(R.styleable.LiquidSlider_value, value)
            a.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        trackY = h / 2f
        thumbView.layoutParams.width = (h * 1.5f).toInt()
        thumbView.layoutParams.height = h
        thumbView.requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw track
        canvas.drawLine(thumbView.width / 2f, trackY, width - thumbView.width / 2f, trackY, trackPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                velocityTracker?.clear()
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000)
                val velocityX = velocityTracker?.xVelocity ?: 0f

                val scaleFactor = 1f + (Math.abs(velocityX) / 2000f).coerceAtMost(0.5f)
                springScaleX.animateToFinalPosition(scaleFactor)
                springScaleY.animateToFinalPosition(1f / scaleFactor) // Squish effect

                thumbView.refractionIntensity = (0.05f + (Math.abs(velocityX) / 5000f)).coerceAtMost(0.1f)
                thumbView.blurRadius = (10f + (Math.abs(velocityX) / 100f)).coerceAtMost(25f)

                val newValue = (event.x - thumbView.width / 2f) / (width - thumbView.width)
                this.value = newValue
                thumbView.x = (width - thumbView.width) * this.value
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.recycle()
                velocityTracker = null
                springScaleX.animateToFinalPosition(1f)
                springScaleY.animateToFinalPosition(1f)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    interface OnValueChangedListener {
        fun onValueChanged(value: Float)
    }
}
