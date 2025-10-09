package com.kyant.backdrop.catalog.xml.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import com.kyant.backdrop.xml.views.LiquidGlassContainer
import com.kyant.backdrop.xml.effects.*
import com.kyant.backdrop.xml.backdrop.XmlBackdrop
import kotlin.math.*

/**
 * A high-level liquid slider component that exactly replicates the Compose LiquidSlider.
 * Implements area-specific backdrop capture instead of using entire background.
 * 
 * Key features matching Compose version:
 * - Area-specific backdrop capture for both track and thumb
 * - Interactive drag animations with smooth value changes
 * - Proper thumb scaling and positioning
 * - Surface tinting and color effects
 * - Vibrancy, blur, and refraction effects on thumb
 */
class LiquidSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val trackContainer: LiquidGlassContainer
    private val thumbContainer: LiquidGlassContainer
    private val trackView: View
    private val thumbView: View
    
    // Slider value properties
    private var minValue = 0f
    private var maxValue = 1f
    private var currentValue = 0.5f
    private var onValueChangeListener: ((Float) -> Unit)? = null
    
    // Animation properties
    private var thumbScaleAnimation: ValueAnimator? = null
    private var valueAnimation: ValueAnimator? = null
    private var isDragging = false
    private var dragStartX = 0f
    
    // Visual properties - matching Compose API
    var tintColor = Color.TRANSPARENT
        set(value) {
            field = value
            updateSurfaceEffects()
        }
    var surfaceColor = Color.TRANSPARENT
        set(value) {
            field = value
            updateSurfaceEffects()
        }
    
    init {
        // Create track container with subtle glass effect
        trackContainer = LiquidGlassContainer(context)
        
        // Create track view
        trackView = View(context).apply {
            setBackgroundColor(Color.argb(40, 255, 255, 255))
        }
        
        // Create thumb container with strong glass effect
        thumbContainer = LiquidGlassContainer(context)
        
        // Create thumb view - transparent to show glass effects
        thumbView = View(context).apply {
            // Use semi-transparent white tint instead of solid background
            // This allows the backdrop and refraction effects to be visible
            setBackgroundColor(Color.argb(25, 255, 255, 255))
        }
        
        // Setup hierarchy
        trackContainer.addView(trackView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        
        thumbContainer.addView(thumbView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        
        addView(trackContainer)
        addView(thumbContainer)
        
        // Apply liquid glass effects - exactly matching Compose version
        applyLiquidGlassEffects()
        
        // Setup interaction - matching Compose behavior
        setupInteraction()
        
        // Set proper minimum dimensions
        minimumWidth = (200 * resources.displayMetrics.density).toInt()
        minimumHeight = (44 * resources.displayMetrics.density).toInt()
    }
    
    private fun applyLiquidGlassEffects() {
        val density = resources.displayMetrics.density
        
        // Track effects - subtle glass effect
        trackContainer.setCornerRadius(22f * density)
        trackContainer.setBlurEffect(BlurEffect(1f * density))
        trackContainer.setRefractionEffect(RefractionEffect(
            height = 6f * density,
            amount = 12f * density,
            hasDepthEffect = false
        ))
        
        // Thumb effects - strong dynamic glass effect exactly like Compose version
        // Compose uses: blur(8f.dp * (1f - progress)), refractionWithDispersion(6f.dp * progress, height/2 * progress)
        // For static state, we use the full effect values
        thumbContainer.setCornerRadius(16f * density)
        
        // Vibrancy effect - enhances the backdrop colors
        thumbContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
        
        // Blur effect - matching Compose blur(8f.dp.toPx())
        thumbContainer.setBlurEffect(BlurEffect(8f * density))
        
        // Refraction effect - matching Compose refraction(6f.dp.toPx(), height/2)
        thumbContainer.setRefractionEffect(RefractionEffect(
            height = 6f * density,
            amount = 16f * density,  // Approximately height/2 of 32dp thumb
            hasDepthEffect = true
        ))
        
        // Dispersion effect - matching Compose refractionWithDispersion
        thumbContainer.setDispersionEffect(DispersionEffect(
            height = 6f * density,
            amount = 8f * density
        ))
        
        // Highlight effect - ambient lighting
        thumbContainer.setHighlightEffect(HighlightEffect.ambient(alpha = 0.15f))
        
        // Shadow effect - subtle drop shadow
        thumbContainer.setShadowEffect(ShadowEffect(
            offsetX = 0f,
            offsetY = 2f * density,
            radius = 4f * density,
            color = Color.BLACK,
            alpha = 0.05f
        ))
        
        // Inner shadow for pressed state - starts at 0, animates on press
        // Will be updated dynamically in animateThumbScale
    }
    
    private fun setupInteraction() {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = true
                    dragStartX = event.x
                    animateThumbScale(true)
                    updateValueFromPosition(event.x)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDragging) {
                        updateValueFromPosition(event.x)
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                    animateThumbScale(false)
                    true
                }
                else -> false
            }
        }
        
        isClickable = true
        isFocusable = true
    }
    
    private fun updateValueFromPosition(x: Float) {
        val trackWidth = width - thumbContainer.width
        val thumbRadius = thumbContainer.width / 2f
        val clampedX = x.coerceIn(thumbRadius, width - thumbRadius)
        val progress = if (trackWidth > 0) (clampedX - thumbRadius) / trackWidth else 0f
        
        val newValue = minValue + (maxValue - minValue) * progress
        setValue(newValue, true)
    }
    
    private fun animateThumbScale(pressed: Boolean) {
        thumbScaleAnimation?.cancel()
        
        val density = resources.displayMetrics.density
        val targetScale = if (pressed) 1.15f else 1.0f
        val currentScale = thumbContainer.scaleX
        
        thumbScaleAnimation = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 200
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                
                // Interpolate scale
                val scale = currentScale + (targetScale - currentScale) * progress
                thumbContainer.scaleX = scale
                thumbContainer.scaleY = scale
                
                // Animate glass effects based on press state
                // Compose does: blur(8f.dp * (1f - progress)) and refraction(6f.dp * progress, height/2 * progress)
                if (pressed) {
                    // When pressing: reduce blur, increase refraction (more glass effect)
                    val blurRadius = (8f * density * (1f - progress)).coerceAtLeast(0.1f)  // 8dp -> 0.1dp (avoid 0)
                    val refractionHeight = 6f * density * progress    // 0dp -> 6dp
                    val refractionAmount = 16f * density * progress   // 0dp -> 16dp
                    
                    thumbContainer.setBlurEffect(BlurEffect(blurRadius))
                    thumbContainer.setRefractionEffect(RefractionEffect(
                        height = refractionHeight,
                        amount = refractionAmount,
                        hasDepthEffect = true
                    ))
                    thumbContainer.setInnerShadowEffect(InnerShadowEffect(
                        offsetX = 0f,
                        offsetY = 0f,
                        radius = 4f * density * progress,
                        alpha = progress * 0.1f
                    ))
                } else {
                    // When releasing: restore blur, reduce refraction
                    val blurRadius = (8f * density * progress).coerceAtLeast(0.1f)  // 0.1dp -> 8dp (avoid 0)
                    val refractionHeight = 6f * density * (1f - progress)  // 6dp -> 0dp
                    val refractionAmount = 16f * density * (1f - progress) // 16dp -> 0dp
                    
                    thumbContainer.setBlurEffect(BlurEffect(blurRadius))
                    thumbContainer.setRefractionEffect(RefractionEffect(
                        height = refractionHeight,
                        amount = refractionAmount,
                        hasDepthEffect = true
                    ))
                    thumbContainer.setInnerShadowEffect(InnerShadowEffect(
                        offsetX = 0f,
                        offsetY = 0f,
                        radius = 4f * density * (1f - progress),
                        alpha = (1f - progress) * 0.1f
                    ))
                }
            }
            start()
        }
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = maxOf(minimumHeight, (44 * resources.displayMetrics.density).toInt())
        
        // Measure track container
        val trackWidth = width
        val trackHeight = (8 * resources.displayMetrics.density).toInt()
        trackContainer.measure(
            MeasureSpec.makeMeasureSpec(trackWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(trackHeight, MeasureSpec.EXACTLY)
        )
        
        // Measure thumb container
        val thumbSize = (32 * resources.displayMetrics.density).toInt()
        thumbContainer.measure(
            MeasureSpec.makeMeasureSpec(thumbSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(thumbSize, MeasureSpec.EXACTLY)
        )
        
        setMeasuredDimension(width, height)
    }
    
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val width = r - l
        val height = b - t
        
        // Layout track in center
        val trackTop = (height - trackContainer.measuredHeight) / 2
        trackContainer.layout(
            0, trackTop,
            width, trackTop + trackContainer.measuredHeight
        )
        
        // Layout thumb based on current value
        updateThumbPosition()
    }
    
    private fun updateThumbPosition() {
        if (width <= 0) return
        
        val trackWidth = width - thumbContainer.measuredWidth
        val progress = if (maxValue > minValue) (currentValue - minValue) / (maxValue - minValue) else 0f
        val thumbX = (progress * trackWidth).toInt()
        val thumbY = (height - thumbContainer.measuredHeight) / 2
        
        thumbContainer.layout(
            thumbX, thumbY,
            thumbX + thumbContainer.measuredWidth,
            thumbY + thumbContainer.measuredHeight
        )
    }
    
    private fun updateSurfaceEffects() {
        // Apply surface tinting to thumb (matching Compose behavior)
        if (tintColor != Color.TRANSPARENT || surfaceColor != Color.TRANSPARENT) {
            val brightness = if (surfaceColor != Color.TRANSPARENT) 0.1f else 0f
            val saturation = if (tintColor != Color.TRANSPARENT) 1.2f else 1f
            thumbContainer.setColorFilterEffect(ColorFilterEffect(
                brightness = brightness,
                saturation = saturation
            ))
        } else {
            thumbContainer.setColorFilterEffect(ColorFilterEffect.vibrant())
        }
    }
    
    // Public API - matching Compose LiquidSlider
    
    /**
     * Sets the backdrop source for both track and thumb
     * In Compose, track uses its own LayerBackdrop and thumb combines both backdrops
     */
    fun setBackdropSource(backdrop: com.kyant.backdrop.xml.backdrop.XmlBackdrop) {
        trackContainer.setBackgroundSource(backdrop)
        thumbContainer.setBackgroundSource(backdrop)
    }
    
    /**
     * Sets the backdrop source from a LayerBackdropView
     */
    fun setBackdropSource(backdropView: com.kyant.backdrop.xml.backdrop.LayerBackdropView) {
        val backdrop = backdropView.getBackdrop()
        setBackdropSource(backdrop)
    }
    
    /**
     * Sets separate backdrop sources for track and thumb (advanced usage)
     */
    fun setBackdropSources(trackBackdrop: com.kyant.backdrop.xml.backdrop.XmlBackdrop, thumbBackdrop: com.kyant.backdrop.xml.backdrop.XmlBackdrop) {
        trackContainer.setBackgroundSource(trackBackdrop)
        thumbContainer.setBackgroundSource(thumbBackdrop)
    }
    
    /**
     * Sets the slider value range
     */
    fun setValueRange(min: Float, max: Float) {
        minValue = min
        maxValue = max
        setValue(currentValue.coerceIn(min, max), false)
    }
    
    /**
     * Sets the slider value
     */
    fun setValue(value: Float, notify: Boolean = true) {
        val newValue = value.coerceIn(minValue, maxValue)
        if (newValue != currentValue) {
            currentValue = newValue
            updateThumbPosition()
            if (notify) {
                onValueChangeListener?.invoke(currentValue)
            }
        }
    }
    
    /**
     * Gets the current slider value
     */
    fun getValue(): Float = currentValue
    
    /**
     * Sets the value change listener
     */
    fun setOnValueChangeListener(listener: (Float) -> Unit) {
        onValueChangeListener = listener
    }
}