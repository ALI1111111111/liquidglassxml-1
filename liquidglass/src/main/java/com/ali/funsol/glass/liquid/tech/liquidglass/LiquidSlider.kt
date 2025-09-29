package com.ali.funsol.glass.liquid.tech.liquidglass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import kotlin.math.roundToInt

class LiquidSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val glassView: GlassView
    private val seekBar: SeekBar

    var onValueChanged: ((Float) -> Unit)? = null

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(100, 255, 255, 255)
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private var trackY = 0f
    private var velocityTracker: VelocityTracker? = null

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
        SpringAnimation(glassView, scaleXProperty).setSpring(
            SpringForce(1f).apply {
                stiffness = SpringForce.STIFFNESS_MEDIUM
                dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            }
        )
    }

    private val springScaleY: SpringAnimation by lazy {
        SpringAnimation(glassView, scaleYProperty).setSpring(
            SpringForce(1f).apply {
                stiffness = SpringForce.STIFFNESS_MEDIUM
                dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            }
        )
    }

    init {
        // Pass the AttributeSet down to the child GlassView
        glassView = GlassView(context, attrs, defStyle).apply {
            // Defaults can be set here, but XML attributes will override them
            cornerRadius = 999f
            blurRadius = 10f
            refractionIntensity = 0.05f
        }
        addView(glassView)

        seekBar = SeekBar(context)
        addView(seekBar)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                onValueChanged?.invoke(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.LiquidSlider)
            seekBar.progress = a.getInt(R.styleable.LiquidSlider_value, seekBar.progress)
            a.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        trackY = h / 2f
        glassView.layoutParams.width = (h * 1.5f).toInt()
        glassView.layoutParams.height = h
        glassView.requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw track
        canvas.drawLine(glassView.width / 2f, trackY, width - glassView.width / 2f, trackY, trackPaint)
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

                glassView.refractionIntensity = (0.05f + (Math.abs(velocityX) / 5000f)).coerceAtMost(0.1f)
                glassView.blurRadius = (10f + (Math.abs(velocityX) / 100f)).coerceAtMost(25f)

                val newValue = (event.x - glassView.width / 2f) / (width - glassView.width)
                seekBar.progress = (newValue * seekBar.max).roundToInt()
                glassView.x = (width - glassView.width) * newValue
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
}
