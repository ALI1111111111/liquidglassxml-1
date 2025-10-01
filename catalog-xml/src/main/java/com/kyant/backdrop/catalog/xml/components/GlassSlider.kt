package com.kyant.backdrop.catalog.xml.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.kyant.backdrop.catalog.xml.R
import com.kyant.backdrop.xml.BackdropEffect
import com.kyant.backdrop.xml.HighlightType
import com.kyant.backdrop.xml.LiquidGlassView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * An enhanced glass slider with momentum, interactive animations, and smooth glass effects.
 */
class GlassSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val trackView: LiquidGlassView
    private val thumbView: LiquidGlassView
    private val progressPaint: Paint
    private val trackPaint: Paint
    
    var value: Float = 0f
        set(newValue) {
            field = newValue.coerceIn(valueFrom, valueTo)
            updateThumbPosition()
            onValueChangeListener?.invoke(field)
        }
    
    var valueFrom: Float = 0f
    var valueTo: Float = 100f
    var onValueChangeListener: ((Float) -> Unit)? = null
    
    private var thumbX = 0f
    private var isDragging = false
    private var dragStartX = 0f
    private var velocityTracker: VelocityTracker? = null
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    
    private val momentumAnimator = ValueAnimator().apply {
        duration = 400
        interpolator = DecelerateInterpolator()
    }
    
    private val scaleAnimator = ValueAnimator().apply {
        duration = 200
        interpolator = DecelerateInterpolator()
    }
    
    private var currentScale = 1f
    private var currentProgress = 0f

    init {
        // Read custom attributes
        context.obtainStyledAttributes(attrs, R.styleable.GlassSlider).apply {
            valueFrom = getFloat(R.styleable.GlassSlider_valueFrom, 0f)
            valueTo = getFloat(R.styleable.GlassSlider_valueTo, 100f)
            value = getFloat(R.styleable.GlassSlider_value, 0f)
            recycle()
        }

        val density = resources.displayMetrics.density
        
        // Setup track (matching Compose: 20-36% gray background)
        trackView = LiquidGlassView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (6 * density).toInt())
            setCornerRadius(3 * density)
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                addVibrancyEffect()
                addBlurEffect(2 * density)
            }
            
            // ALWAYS draw track background (matching Compose: light frosted)
            onDrawSurface = { canvas ->
                // Light theme: 10-15% gray for frosted glass appearance
                val trackPaint = android.graphics.Paint().apply {
                    color = 0xFF787878.toInt() // Gray color
                    alpha = 26 // 10% opacity (frosted glass, not solid)
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), trackPaint)
            }
            
            setRefraction(4 * density, 6 * density, 0.05f)
            setHighlight(2.5f, 1.5f, HighlightType.SPECULAR)
            
            // Add inner shadow for depth
            setInnerShadow(com.kyant.backdrop.xml.DefaultInnerShadow(
                elevation = 4 * density,
                color = 0x0D000000.toInt(), // 5% black
                offsetX = 0f,
                offsetY = 0f
            ))
            
            animationDuration = 0
        }
        addView(trackView)
        
        // Setup thumb (tracker)
        thumbView = LiquidGlassView(context).apply {
            layoutParams = LayoutParams((40 * density).toInt(), (24 * density).toInt())
            setCornerRadius(12 * density)
            
            // Modern effects on API 31+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                addVibrancyEffect()
                addBlurEffect(8 * density)
            }
            
            setRefraction(6 * density, 12 * density, 0.1f)
            setHighlight(2.5f, 1.5f, HighlightType.AMBIENT)
            animationDuration = 150
            
            // Add shadow (matching Compose: 4dp, 5% opacity)
            setShadow(com.kyant.backdrop.xml.DefaultShadow(
                elevation = 4 * density,
                color = 0x0D000000.toInt(), // 5% black
                offsetX = 0f,
                offsetY = 2 * density
            ))
            
            // Add inner shadow for depth (matching Compose)
            setInnerShadow(com.kyant.backdrop.xml.DefaultInnerShadow(
                elevation = 8 * density,
                color = 0x1A000000.toInt(), // 10% black
                offsetX = 0f,
                offsetY = 0f
            ))
            
            // CRITICAL: Draw white surface like Compose - light frosted glass
            onDrawSurface = { canvas ->
                // Light opacity for frosted glass effect - highlight does the rim light
                val surfaceAlpha = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    51 // 20% white (blur + highlight + inner shadow create the effect)
                } else {
                    64 // 25% white (no blur, needs slightly more)
                }
                
                val surfacePaint = android.graphics.Paint().apply {
                    color = 0xFFFFFFFF.toInt()
                    alpha = surfaceAlpha
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), surfacePaint)
            }
        }
        addView(thumbView)
        
        // Setup paints
        val accentColor = ContextCompat.getColor(context, android.R.color.holo_blue_light)
        progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        
        trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33787878
            style = Paint.Style.FILL
        }
        
        setWillNotDraw(false)
        updateThumbPosition()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateThumbPosition()
    }
    
    private fun updateThumbPosition() {
        if (width == 0) return
        
        val density = resources.displayMetrics.density
        val thumbWidth = 40 * density
        val thumbHeight = 24 * density
        val trackPadding = thumbWidth / 2f
        val availableWidth = width - trackPadding * 2
        
        val fraction = (value - valueFrom) / (valueTo - valueFrom)
        thumbX = trackPadding + fraction * availableWidth
        
        thumbView.x = thumbX - thumbWidth / 2f
        thumbView.y = (height - thumbHeight) / 2f
        
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val density = resources.displayMetrics.density
        val trackHeight = 6 * density
        val trackY = (height - trackHeight) / 2f
        val thumbWidth = 40 * density
        val trackPadding = thumbWidth / 2f
        
        // Draw track background
        val trackRect = RectF(
            trackPadding,
            trackY,
            width - trackPadding,
            trackY + trackHeight
        )
        canvas.drawRoundRect(trackRect, 3 * density, 3 * density, trackPaint)
        
        // Draw progress
        val progressRect = RectF(
            trackPadding,
            trackY,
            thumbX,
            trackY + trackHeight
        )
        canvas.drawRoundRect(progressRect, 3 * density, 3 * density, progressPaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if touch is near thumb
                val thumbWidth = 40 * resources.displayMetrics.density
                if (abs(event.x - thumbX) <= thumbWidth) {
                    isDragging = true
                    dragStartX = event.x
                    velocityTracker = VelocityTracker.obtain()
                    velocityTracker?.addMovement(event)
                    animatePress(true)
                    parent.requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    velocityTracker?.addMovement(event)
                    updateValueFromPosition(event.x)
                    return true
                }
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    velocityTracker?.computeCurrentVelocity(1000)
                    val velocity = velocityTracker?.xVelocity ?: 0f
                    
                    // Apply momentum if velocity is significant
                    if (abs(velocity) > 500) {
                        applyMomentum(velocity)
                    }
                    
                    velocityTracker?.recycle()
                    velocityTracker = null
                    isDragging = false
                    animatePress(false)
                    parent.requestDisallowInterceptTouchEvent(false)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
    
    private fun updateValueFromPosition(x: Float) {
        val density = resources.displayMetrics.density
        val thumbWidth = 40 * density
        val trackPadding = thumbWidth / 2f
        val availableWidth = width - trackPadding * 2
        
        val fraction = ((x - trackPadding) / availableWidth).coerceIn(0f, 1f)
        value = valueFrom + fraction * (valueTo - valueFrom)
    }
    
    private fun applyMomentum(velocity: Float) {
        momentumAnimator.cancel()
        
        val density = resources.displayMetrics.density
        val thumbWidth = 40 * density
        val trackPadding = thumbWidth / 2f
        val availableWidth = width - trackPadding * 2
        
        val momentum = velocity * 0.1f // Damping factor
        val currentFraction = (value - valueFrom) / (valueTo - valueFrom)
        val targetFraction = (currentFraction + momentum / availableWidth).coerceIn(0f, 1f)
        val targetValue = valueFrom + targetFraction * (valueTo - valueFrom)
        
        momentumAnimator.setFloatValues(value, targetValue)
        momentumAnimator.addUpdateListener { animation ->
            value = animation.animatedValue as Float
        }
        momentumAnimator.start()
    }
    
    private fun animatePress(pressed: Boolean) {
        scaleAnimator.cancel()
        
        val targetScale = if (pressed) 1.5f else 1f
        val targetProgress = if (pressed) 1f else 0f
        
        scaleAnimator.setFloatValues(currentScale, targetScale)
        scaleAnimator.addUpdateListener { animation ->
            currentScale = animation.animatedValue as Float
            currentProgress = animation.animatedFraction * (if (pressed) 1f else (1f - currentProgress))
            
            // Scale with velocity squash/stretch effect
            val scaleX = currentScale / (1f - currentProgress * 0.15f)
            val scaleY = currentScale * (1f - currentProgress * 0.15f)
            thumbView.scaleX = scaleX
            thumbView.scaleY = scaleY
            
            // Update glass effects based on interaction
            val density = resources.displayMetrics.density
            val refractionHeight = 6f + 6f * currentProgress
            val refractionAmount = (12f + thumbView.height / 2f) * currentProgress
            thumbView.setRefraction(
                refractionHeight * density,
                refractionAmount,
                0.1f + 0.05f * currentProgress
            )
            
            // Change highlight type and intensity on press
            thumbView.setHighlight(2.5f, 1.5f, HighlightType.AMBIENT)
            
            // Update inner shadow for pressed state (API 31+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val shadowAlpha = (currentProgress * 255).toInt().coerceIn(0, 255)
                thumbView.setInnerShadow(com.kyant.backdrop.xml.DefaultInnerShadow(
                    elevation = 4 * density * currentProgress,
                    color = (shadowAlpha shl 24) or 0x000000
                ))
            }
            
            // Update surface opacity (more transparent when pressed)
            thumbView.alpha = 1f - currentProgress * 0.2f
        }
        scaleAnimator.start()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        momentumAnimator.cancel()
        scaleAnimator.cancel()
        velocityTracker?.recycle()
    }
}